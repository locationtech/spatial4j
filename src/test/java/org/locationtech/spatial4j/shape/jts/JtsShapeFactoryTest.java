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

import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.distance.GeodesicSphereDistCalc;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.impl.GeoCircle;
import org.locationtech.spatial4j.shape.impl.PointImpl;

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

  @Test
  public void testEmptyPoint() {
    JtsSpatialContextFactory jtsCtxFactory = new JtsSpatialContextFactory();
    JtsSpatialContext jtsCtx = jtsCtxFactory.newSpatialContext();
    GeometryFactory geometryFactory = jtsCtxFactory.getGeometryFactory();
    final org.locationtech.jts.geom.Point  point = geometryFactory.createPoint();//empty
    final Shape shape = jtsCtx.getShapeFactory().makeShapeFromGeometry(point); // don't throw
    assertTrue(shape.isEmpty());
  }

  @Test
  public void testCircleGeometryConversions() {
    // Hawaii (Far West)
    circleGeometryConversionTest(-155.84, 19.74, 50);
    // Nunavat (Far North)
    circleGeometryConversionTest(-83.10, 70.30, 100);
    // Sydney (South East)
    circleGeometryConversionTest(151.21, 33.87, 1);
  }

  private void circleGeometryConversionTest(double x, double y, double radiusKm) {
    Point circleCenter = new PointImpl(x, y, SpatialContext.GEO);
    double radiusDeg = DistanceUtils.dist2Degrees(radiusKm, DistanceUtils.EARTH_EQUATORIAL_RADIUS_KM);
    GeoCircle geoCircle = new GeoCircle(circleCenter, radiusDeg, SpatialContext.GEO);

    JtsShapeFactory shapeFactory = JtsSpatialContext.GEO.getShapeFactory();
    // Let's ensure the circle-to-polygon conversion is accurate accounting for geodesics.
    Geometry geometry = shapeFactory.getGeometryFrom(geoCircle);
    Assert.assertTrue(geometry instanceof Polygon);
    Polygon polygon = (Polygon) geometry;
    Coordinate[] coordinates = polygon.getExteriorRing().getCoordinates();
    int size = coordinates.length;
    Assert.assertTrue(size >= 100);
    GeodesicSphereDistCalc distCalc = new GeodesicSphereDistCalc.Haversine();
    double maxDeltaKm = radiusKm / 100; // allow 1% inaccuracy
    for (Coordinate coordinate : coordinates) {
      // Check distance from center of each point
      Point point = new PointImpl(coordinate.x, coordinate.y, SpatialContext.GEO);
      double distance = distCalc.distance(point, circleCenter);
      double distanceKm = DistanceUtils.degrees2Dist(distance, DistanceUtils.EARTH_MEAN_RADIUS_KM);
      Assert.assertEquals(String.format("Distance from point to center: %.2f km. Expected: %.2f km", distanceKm,
              radiusKm), radiusKm, distanceKm, maxDeltaKm);
    }
  }
}
