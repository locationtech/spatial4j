package org.apache.lucene.spatial.search.jts;

import org.apache.lucene.spatial.search.SpatialArgs;
import org.apache.lucene.spatial.search.SpatialRelationship;

import com.vividsolutions.jts.geom.Geometry;

public class GeometrySpatialArguments extends SpatialArgs
{
  public Geometry shape = null;
  public SpatialRelationship op = SpatialRelationship.WITHIN;
}