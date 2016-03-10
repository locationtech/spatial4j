package org.locationtech.spatial4j.io.jackson;

import org.locationtech.spatial4j.shape.Shape;

import com.vividsolutions.jts.geom.Geometry;

public class ObjectWithGeometry {
  public String name;
  public Geometry geo;
  public Shape shape;
}
