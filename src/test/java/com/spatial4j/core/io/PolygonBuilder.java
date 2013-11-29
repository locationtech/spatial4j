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

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating a {@link com.spatial4j.core.shape.Shape} instance of a Polygon
 */
public class PolygonBuilder {

  private final JtsSpatialContext ctx;
  private final List<Coordinate> points = new ArrayList<Coordinate>();
  private final List<LinearRing> holes = new ArrayList<LinearRing>();

  public PolygonBuilder(JtsSpatialContext ctx) {
    this.ctx = ctx;
  }

  /**
   * Adds a point to the Polygon
   *
   * @param lon Longitude of the point
   * @param lat Latitude of the point
   * @return this
   */
  public PolygonBuilder point(double lon, double lat) {
    points.add(new Coordinate(lon, lat));
    return this;
  }

  /**
   * Starts a new hole in the Polygon
   *
   * @return PolygonHoleBuilder to create the new hole
   */
  public PolygonHoleBuilder newHole() {
    return new PolygonHoleBuilder(this);
  }

  /**
   * Registers the LinearRing representing a hole
   *
   * @param linearRing Hole to register
   * @return this
   */
  private PolygonBuilder addHole(LinearRing linearRing) {
    holes.add(linearRing);
    return this;
  }

  /**
   * Builds a {@link com.spatial4j.core.shape.Shape} instance representing the polygon
   *
   * @return Built polygon
   */
  public Shape build() {
    return new JtsGeometry(toPolygon(), ctx, true, true);
  }

  /**
   * Creates the raw {@link com.vividsolutions.jts.geom.Polygon}
   *
   * @return Built polygon
   */
  public Polygon toPolygon() {
    LinearRing ring = ctx.getGeometryFactory().createLinearRing(points.toArray(new Coordinate[points.size()]));
    LinearRing[] holes = this.holes.isEmpty() ? null : this.holes.toArray(new LinearRing[this.holes.size()]);
    return ctx.getGeometryFactory().createPolygon(ring, holes);
  }

  /**
   * Builder for defining a hole in a {@link com.vividsolutions.jts.geom.Polygon}
   */
  public class PolygonHoleBuilder {

    private final List<Coordinate> points = new ArrayList<Coordinate>();
    private final PolygonBuilder polygonBuilder;

    /**
     * Creates a new PolygonHoleBuilder
     *
     * @param polygonBuilder PolygonBuilder that the hole built by this builder
     *                       will be added to
     */
    private PolygonHoleBuilder(PolygonBuilder polygonBuilder) {
      this.polygonBuilder = polygonBuilder;
    }

    /**
     * Adds a point to the LinearRing
     *
     * @param lon Longitude of the point
     * @param lat Latitude of the point
     * @return this
     */
    public PolygonHoleBuilder point(double lon, double lat) {
      points.add(new Coordinate(lon, lat));
      return this;
    }

    /**
     * Ends the building of the hole
     *
     * @return PolygonBuilder to use to build the remainder of the Polygon.
     */
    public PolygonBuilder endHole() {
      return polygonBuilder.addHole(ctx.getGeometryFactory().createLinearRing(points.toArray(new Coordinate[points.size()])));
    }
  }
}
