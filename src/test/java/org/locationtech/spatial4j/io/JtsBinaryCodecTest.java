/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;

public class JtsBinaryCodecTest extends BinaryCodecTest {

  @Override
  public SpatialContext initContext() {
    JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
    factory.precisionModel = new PrecisionModel(PrecisionModel.FLOATING_SINGLE);
    return factory.newSpatialContext();
  }

  @Test
  public void testPoly() throws Exception {
    JtsSpatialContext ctx = (JtsSpatialContext)super.ctx;
    final JtsGeometry shape = ctx.makeShape(randomGeometry(randomIntBetween(3, 20)), false, false);
    assertRoundTrip(shape);
  }

  @Override
  protected Shape randomShape() {
    if (randomInt(3) == 0) {
      JtsSpatialContext ctx = (JtsSpatialContext)super.ctx;
      return ctx.makeShape(randomGeometry(randomIntBetween(3, 20)), false, false);
    } else {
      return super.randomShape();
    }
  }

  Geometry randomGeometry(int points) {
    //a circle
    JtsSpatialContext ctx = (JtsSpatialContext)super.ctx;
    GeometricShapeFactory gsf = new GeometricShapeFactory(ctx.getGeometryFactory());
    gsf.setCentre(new Coordinate(0, 0));
    gsf.setSize(180);//diameter
    gsf.setNumPoints(points);
    return gsf.createCircle();
  }

}
