package org.apache.lucene.spatial.pending.jts;

import com.vividsolutions.jts.geom.Geometry;


public interface GeometryTest {

  public boolean matches(Geometry geo);
}
