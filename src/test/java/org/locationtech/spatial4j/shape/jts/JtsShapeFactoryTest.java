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

package org.locationtech.spatial4j.shape.jts;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.junit.Test;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;

import static org.junit.Assert.assertTrue;

public class JtsShapeFactoryTest {

  @Test
  public void testIndex() {

    JtsSpatialContextFactory ctxFactory = new JtsSpatialContextFactory();
    ctxFactory.autoIndex = true;

    Geometry g = ctxFactory.getGeometryFactory().createPoint(new Coordinate(0,0)).buffer(0.1);

    JtsSpatialContext ctx = ctxFactory.newSpatialContext();

    JtsGeometry jtsGeom1 = (JtsGeometry) ctx.getShapeFactory().makeShapeFromGeometry(g);
    assertTrue(jtsGeom1.isIndexed());

    JtsGeometry jtsGeom2 = ctx.getShapeFactory().makeShape(g);
    assertTrue(jtsGeom2.isIndexed());
  }
}
