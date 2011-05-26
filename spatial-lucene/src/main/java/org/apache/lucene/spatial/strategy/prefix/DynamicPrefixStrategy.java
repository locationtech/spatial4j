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

package org.apache.lucene.spatial.strategy.prefix;

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
import org.apache.lucene.spatial.base.prefix.GeohashSpatialPrefixGrid;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialOperation;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.SpatialStrategy;
import org.apache.lucene.spatial.strategy.util.CachedDistanceValueSource;

import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class DynamicPrefixStrategy extends SpatialStrategy<SimpleSpatialFieldInfo> {

  private final Map<String, PrefixFieldCacheProvider> provider = new ConcurrentHashMap<String, PrefixFieldCacheProvider>();

  private final GeohashSpatialPrefixGrid grid;
  private final int expectedFieldsPerDocument;
  private int prefixGridScanLevel;//TODO how is this customized?

  public DynamicPrefixStrategy(GeohashSpatialPrefixGrid grid) {
    this(grid, 2 ); // array gets initalized with 2 slots
    prefixGridScanLevel = grid.getMaxLevels() - 4;//TODO this default constant is dependent on the prefix grid size
  }

  public DynamicPrefixStrategy(GeohashSpatialPrefixGrid grid, int expectedFieldsPerDocument) {
    this.grid = grid;
    this.expectedFieldsPerDocument = expectedFieldsPerDocument;
  }

  public void setPrefixGridScanLevel(int prefixGridScanLevel) {
    this.prefixGridScanLevel = prefixGridScanLevel;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()+"(prefixGridScanLevel:"+prefixGridScanLevel+",SPG:("+ grid +"))";
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
    SpatialPrefixGrid.Cell cell = grid.getCell(p.getX(), p.getY(), grid.getMaxLevels());
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
    PrefixFieldCacheProvider p = provider.get( fieldInfo.getFieldName() );
    if( p == null ) {
      p = new PrefixFieldCacheProvider(grid, fieldInfo.getFieldName(), expectedFieldsPerDocument );
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
    final SpatialOperation op = args.getOperation();
    if (! SpatialOperation.is(op, SpatialOperation.IsWithin, SpatialOperation.Intersects, SpatialOperation.BBoxWithin))
      throw new UnsupportedSpatialOperation(op);

    Shape qshape = args.getShape();
    //TODO does this logic really belong here? Wouldn't it be common to all strategies, and thus perhaps a
    // convenience method on SpatialArgs would get the appropriate query shape depending on other relevant arguments?
    if (op == SpatialOperation.BBoxWithin)
      qshape = qshape.getBoundingBox();

    return new DynamicPrefixFilter(
        fieldInfo.getFieldName(), grid,qshape, prefixGridScanLevel);
  }
}




