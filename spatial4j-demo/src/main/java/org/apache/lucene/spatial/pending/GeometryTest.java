package org.apache.lucene.spatial.pending;

import com.vividsolutions.jts.geom.Geometry;


public interface GeometryTest {

  public boolean matches(Geometry geo);
}
