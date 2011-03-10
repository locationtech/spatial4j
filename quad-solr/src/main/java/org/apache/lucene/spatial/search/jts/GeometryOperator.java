package org.apache.lucene.spatial.search.jts;

import org.apache.lucene.spatial.search.SpatialOperation;
import org.apache.lucene.spatial.search.SpatialResult;


import java.io.IOException;

import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.spatial.geometry.shape.Point2D;
import org.apache.lucene.util.Bits;
import org.apache.solr.search.function.DocValues;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class GeometryOperator implements SpatialOperation<GeometrySpatialArguments>
{    
  @Override
  public SpatialResult execute( AtomicReaderContext readerContext, GeometrySpatialArguments args ) throws IOException
  {
    SpatialResult res = new SpatialResult();
    //...
    return res;
  }
}