package org.apache.lucene.spatial.search.geohash;

import java.io.StringReader;

import org.apache.lucene.analysis.ngram.EdgeNGramTokenizer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.search.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.search.SpatialIndexer;

public class GeohashSpatialIndexer extends SpatialIndexer<SimpleSpatialFieldInfo> {

  private final GridReferenceSystem gridReferenceSystem;
  
  public GeohashSpatialIndexer( GridReferenceSystem gridReferenceSystem ) {
    this.gridReferenceSystem = gridReferenceSystem;
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
    String hash = gridReferenceSystem.encodeXY(p.getX(), p.getY());
    if( index ) {
      Field f = new Field( fieldInfo.getFieldName(), hash, store?Store.YES:Store.NO, Index.ANALYZED_NO_NORMS );
      f.setTokenStream(
          new EdgeNGramTokenizer(new StringReader(hash), EdgeNGramTokenizer.Side.FRONT, 1, Integer.MAX_VALUE));
      return f;
    }
    if( store ) {
      return new Field( fieldInfo.getFieldName(), hash, Store.YES, Index.NO );
    }
    throw new UnsupportedOperationException( "index or store must be true" );
  }
}
