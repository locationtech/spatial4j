package org.apache.lucene.spatial.search.jts;

import org.apache.lucene.spatial.search.SpatialResult;

import com.vividsolutions.jts.geom.Geometry;

public class GeometrySpatialResult extends SpatialResult
{
  public Geometry output;
}