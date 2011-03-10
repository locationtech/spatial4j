package org.apache.lucene.spatial.search.jts;


import java.io.IOException;

import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.spatial.geometry.shape.Point2D;
import org.apache.lucene.spatial.search.SpatialOperation;
import org.apache.lucene.util.Bits;
import org.apache.solr.search.function.DocValues;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class UnionGeometryOperator implements SpatialOperation<GeometrySpatialArguments>
{
  @Override
  public GeometrySpatialResult execute( AtomicReaderContext readerContext, GeometrySpatialArguments args ) throws IOException
  {
    GeometrySpatialResult res = new GeometrySpatialResult();
    res.output = new Point(null,null); 
    for( int i=0; i<readerContext.reader.maxDoc(); i++ ) {
      if( args.possibleItems.get( i ) ) {
        Geometry g = readGeometry( readerContext, i );
        res.output = res.output.union( g );
      }
    }
    return res;
  }
  
  Geometry readGeometry(AtomicReaderContext readerContext, int i) 
  {
    // could read WKB from terms....
    return null;
  }
}