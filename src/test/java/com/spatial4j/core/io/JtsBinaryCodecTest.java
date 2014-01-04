/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.io;

import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.shape.Shape;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import org.junit.Test;

import java.util.Arrays;

public class JtsBinaryCodecTest extends BinaryCodecTest {

  @ParametersFactory
  public static Iterable<Object[]> parameters() {
    //try floats
    JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
    factory.precisionModel = new PrecisionModel(PrecisionModel.FLOATING_SINGLE);

    return Arrays.asList($$(
        $(JtsSpatialContext.GEO),//doubles
        $(factory.newSpatialContext())//floats
    ));
  }

  public JtsBinaryCodecTest(JtsSpatialContext ctx) {
    super(ctx);
  }

  @Test
  public void testPoly() {
    JtsSpatialContext ctx = (JtsSpatialContext)super.ctx;
    ctx.makeShape(randomGeometry(randomIntBetween(3, 20)), false, false);
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
