/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.spatial.strategy.geohash;

import org.apache.lucene.analysis.ngram.EdgeNGramTokenizer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.search.function.ValueSourceQuery;
import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.distance.EuclidianDistanceCalculator;
import org.apache.lucene.spatial.base.exception.UnsupportedSpatialOperation;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialOperation;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.PointDistanceShape;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.Shapes;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.SpatialStrategy;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class GeohashStrategy extends SpatialStrategy<SimpleSpatialFieldInfo> {

  private final Map<String, GeoHashFieldCacheProvider> provider = new ConcurrentHashMap<String, GeoHashFieldCacheProvider>();

  private final GridReferenceSystem gridReferenceSystem;
  private final int expectedFieldsPerDocument;
  private int prefixGridScanLevel;//TODO how is this customized?

  public GeohashStrategy( GridReferenceSystem gridReferenceSystem ) {
    this( gridReferenceSystem, 2 ); // array gets initalized with 2 slots
    prefixGridScanLevel = gridReferenceSystem.getMaxLevels() - 4;//TODO this default constant is dependent on the prefix grid size
  }

  public GeohashStrategy( GridReferenceSystem gridReferenceSystem, int expectedFieldsPerDocument ) {
    this.gridReferenceSystem = gridReferenceSystem;
    this.expectedFieldsPerDocument = expectedFieldsPerDocument;
  }

  @Override
  public Fieldable createField(SimpleSpatialFieldInfo fieldInfo, Shape shape, boolean index, boolean store) {
    if( !(shape instanceof Point) ) {
      if( ignoreIncompatibleGeometry ) {
        return null;
      }
      throw new UnsupportedOperationException( "geohash only support point type (for now)" );
    }

    Point p = (Point)shape;
    SpatialPrefixGrid.Cell cell = gridReferenceSystem.getCell(p.getX(), p.getY(), gridReferenceSystem.getMaxLevels());
    String token = cell.getTokenString();//includes trailing '+' by the way
    if( index ) {
      Field f = new Field( fieldInfo.getFieldName(), token, store?Store.YES:Store.NO, Index.ANALYZED_NO_NORMS );
      f.setTokenStream(
          new EdgeNGramTokenizer(new StringReader(token), EdgeNGramTokenizer.Side.FRONT, 1, Integer.MAX_VALUE));
      return f;
    }
    if( store ) {
      return new Field( fieldInfo.getFieldName(), token, Store.YES, Index.NO );
    }
    throw new UnsupportedOperationException( "index or store must be true" );
  }


  public ValueSource makeValueSource(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo, DistanceCalculator calc) {
    GeoHashFieldCacheProvider p = provider.get( fieldInfo.getFieldName() );
    if( p == null ) {
      p = new GeoHashFieldCacheProvider( gridReferenceSystem, fieldInfo.getFieldName(), expectedFieldsPerDocument );
    }
    Point point = args.getShape().getCenter();
    return new CachedDistanceValueSource(point, calc, p);
  }


  @Override
  public ValueSource makeValueSource(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo) {
    DistanceCalculator calc = new EuclidianDistanceCalculator();
    return makeValueSource(args, fieldInfo,calc);
  }

  @Override
  public Query makeQuery(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo) {
    Filter f = makeFilter(args, fieldInfo);

    ValueSource vs = makeValueSource(args, fieldInfo);
    return new FilteredQuery( new ValueSourceQuery( vs), f );
  }

  @Override
  public Filter makeFilter(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo) {
    if(!(( args.getOperation() == SpatialOperation.IsWithin ) ||
         ( args.getOperation() == SpatialOperation.Intersects ) ||
         ( args.getOperation() == SpatialOperation.BBoxWithin )) ){
      throw new UnsupportedSpatialOperation(args.getOperation());
    }

    Shape qshape = args.getShape();
    if (PointDistanceShape.class.isInstance(args.getShape())) {
      PointDistanceShape pDistGeo = (PointDistanceShape)qshape;

      if (args.getOperation() == SpatialOperation.BBoxWithin) {
        qshape = pDistGeo.getEnclosingBox1();
        Shape shape2 = pDistGeo.getEnclosingBox2();
        if (shape2 != null)
          qshape = new Shapes(Arrays.asList(qshape,shape2),gridReferenceSystem.getShapeIO());
      }
    }

    return new GeoHashPrefixFilter(
        fieldInfo.getFieldName(), gridReferenceSystem,qshape, prefixGridScanLevel);
  }
}




