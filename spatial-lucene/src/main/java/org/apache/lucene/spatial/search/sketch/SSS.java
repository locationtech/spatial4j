package org.apache.lucene.spatial.search.sketch;

import java.io.IOException;

import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.search.function.DocValues;
import org.apache.lucene.spatial.base.Point2D;
import org.apache.lucene.util.Bits;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * SUPER early sketches for a possible API.
 *
 * Mostly looking at the idea of well typed arguments and response
 *
 */
public class SSS
{
  //--------------------
  // High Level
  //--------------------

  public class SpatialResult
  {
    public Bits matches;     // which docs match... maybe DocIdSet?
    public DocValues values; // the score object, could be used for sorting
  }

  public class SpatialArguments
  {
    public boolean calculateMatches = true;
    public boolean calculateValues = false;
    public Bits onlyLookAt = null; // easy way to skip many docs
  }

  public interface LuceneSpatialOperator<A extends SpatialArguments>
  {
    public SpatialResult execute( AtomicReaderContext context, A args ) throws IOException;
  }

  //-------------------------------------------------
  // Simple Point Distance
  // This kind of interface could live in lucene core
  // min/max would avoid use of {frange}
  //-------------------------------------------------

  public class DistanceSpatialArguments extends SpatialArguments
  {
    public Point2D point = null;

    public Double min = null;
    public Double max = null;
  }

  public class SimpleDistanceOperator implements LuceneSpatialOperator<DistanceSpatialArguments>
  {
    @Override
    public SpatialResult execute( AtomicReaderContext readerContext, DistanceSpatialArguments args ) throws IOException
    {
      SpatialResult res = new SpatialResult();
      //...
      return res;
    }
  }


  //-------------------------------------------------
  // Simple Geometry Query
  //-------------------------------------------------

  public enum OPERATION {
    WITHIN,
    CONTAINS,
    INTERSECTS,
    SIMILAR  // fuzzy geometry matching
  };

  public class GeometrySpatialArguments extends SpatialArguments
  {
    public Geometry shape = null;
    public OPERATION op = OPERATION.WITHIN;
  }

  public class GeometryOperator implements LuceneSpatialOperator<GeometrySpatialArguments>
  {
    @Override
    public SpatialResult execute( AtomicReaderContext readerContext, GeometrySpatialArguments args ) throws IOException
    {
      SpatialResult res = new SpatialResult();
      //...
      return res;
    }
  }


  //-------------------------------------------------
  // Complex Geometry Query
  // outside the scope of lucene, but we should have
  // an API that makes it possible
  //-------------------------------------------------

  public class GeometrySpatialResult extends SpatialResult
  {
    public Geometry output;
  }

  public class UnionGeometryOperator implements LuceneSpatialOperator<GeometrySpatialArguments>
  {
    @Override
    public GeometrySpatialResult execute( AtomicReaderContext readerContext, GeometrySpatialArguments args ) throws IOException
    {
      GeometrySpatialResult res = new GeometrySpatialResult();
      res.output = new Point(null,null);
      for( int i=0; i<readerContext.reader.maxDoc(); i++ ) {
        if( args.onlyLookAt.get( i ) ) {
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


}