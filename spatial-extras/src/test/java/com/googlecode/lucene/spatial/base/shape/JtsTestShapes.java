package com.googlecode.lucene.spatial.base.shape;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.distance.DistanceUnits;
import org.apache.lucene.spatial.base.shape.TestShapes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David Smiley - dsmiley@mitre.org
 */
public class JtsTestShapes extends TestShapes {
  private final Logger log = LoggerFactory.getLogger(getClass());

  protected SpatialContext getGeoContext() {
    log.warn("Not actually using JTS for geo context because it doesn't completely work yet.");
    return super.getGeoContext();
    //return new JtsSpatialContext(DistanceUnits.KILOMETERS);
  }

  protected SpatialContext getNonGeoContext() {
    return new JtsSpatialContext(DistanceUnits.EUCLIDEAN);
  }
}
