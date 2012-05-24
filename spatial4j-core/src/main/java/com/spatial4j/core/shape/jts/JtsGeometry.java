/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.shape.jts;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.*;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.PointImpl;
import com.spatial4j.core.shape.impl.RectangleImpl;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.predicate.RectangleIntersects;

public class JtsGeometry implements Shape {
  public final Geometry geom;
  private final boolean hasArea;

  public JtsGeometry(Geometry geom) {
    this.geom = geom;
    this.hasArea = !((Lineal.class.isInstance(geom)) || (Puntal.class.isInstance(geom)));
  }

  //----------------------------------------
  //----------------------------------------

  @Override
  public boolean hasArea() {
    return hasArea;
  }

  @Override
  public Rectangle getBoundingBox() {
    Envelope env = geom.getEnvelopeInternal();
    return new RectangleImpl(env.getMinX(),env.getMaxX(),env.getMinY(),env.getMaxY());
  }

  @Override
  public JtsPoint getCenter() {
    return new JtsPoint(geom.getCentroid());
  }

  private GeometryFactory getGeometryFactory(SpatialContext context) {
    if(JtsSpatialContext.class.isInstance(context)) {
      return ((JtsSpatialContext) context).factory;
    }
    return new GeometryFactory();
  }

  @Override
  public SpatialRelation relate(Shape other, SpatialContext ctx) {
    if (other instanceof Point) {
      return relate((Point)other, ctx);
    }

    // Quick bbox test if this is disjoint
    Rectangle oBBox = other.getBoundingBox();
    if (!oBBox.hasArea()) {//TODO revisit the soundness of this logic
      throw new IllegalArgumentException("the query shape must cover some area (not a line)");
    }
    Envelope geomEnv = geom.getEnvelopeInternal();
    if (oBBox.getMinX() > geomEnv.getMaxX() ||
        oBBox.getMaxX() < geomEnv.getMinX() ||
        oBBox.getMinY() > geomEnv.getMaxY() ||
        oBBox.getMaxY() < geomEnv.getMinY()) {
      return SpatialRelation.DISJOINT;
    }

    if (other instanceof Circle) {
      return relate((Circle) other, ctx);
    }
    
    Polygon oPoly = null;
    if (other instanceof Rectangle) {
      Rectangle r = (Rectangle)other;
      Envelope env = new Envelope(r.getMinX(), r.getMaxX(), r.getMinY(), r.getMaxY());
      
      oPoly = (Polygon) getGeometryFactory(ctx).toGeometry(env);
      //otherwise continue below
    } else if (other instanceof JtsGeometry) {
      oPoly = (Polygon)((JtsGeometry)other).geom;
    } else {
      throw new IllegalArgumentException("Incompatible intersection of "+this+" with "+other);
    }

    //fast algorithm, short-circuit
    if (!RectangleIntersects.intersects(oPoly, geom)) {
      return SpatialRelation.DISJOINT;
    }

    //slower algorithm
    IntersectionMatrix matrix = geom.relate(oPoly);
    assert ! matrix.isDisjoint();//since rectangle intersection was true, shouldn't be disjoint
    if (matrix.isCovers()) {
      return SpatialRelation.CONTAINS;
    }

    if (matrix.isCoveredBy()) {
      return SpatialRelation.WITHIN;
    }

    assert matrix.isIntersects();
    return SpatialRelation.INTERSECTS;
  }

  public SpatialRelation relate(Point pt, SpatialContext ctx) {
    JtsPoint jtsPoint = (JtsPoint) (pt instanceof JtsPoint ? pt : ctx.makePoint(pt.getX(), pt.getY()));
    return geom.contains(jtsPoint.getJtsPoint()) ? SpatialRelation.INTERSECTS : SpatialRelation.DISJOINT;
  }

  public SpatialRelation relate(Circle circle, SpatialContext ctx) {
    //Note: a bbox quick test intersection may or may not have occurred before now but this logic should still work.

    //Test each point to see how many of them are outside of the circle.
    //TODO consider instead using geom.apply(CoordinateFilter) -- maybe faster since avoids Coordinate[] allocation
    Coordinate[] coords = geom.getCoordinates();
    int outside = 0;
    int i = 0;
    for (Coordinate coord : coords) {
      i++;
      SpatialRelation sect = circle.relate(new PointImpl(coord.x, coord.y), ctx);
      if (sect == SpatialRelation.DISJOINT)
        outside++;
      if (i != outside && outside != 0)//short circuit: partially outside, partially inside
        return SpatialRelation.INTERSECTS;
    }
    if (i == outside) {
      return (relate(circle.getCenter(), ctx) == SpatialRelation.DISJOINT)
          ? SpatialRelation.DISJOINT : SpatialRelation.CONTAINS;
    }
    assert outside == 0;
    return SpatialRelation.WITHIN;
  }

  @Override
  public String toString() {
    return geom.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JtsGeometry that = (JtsGeometry) o;
    return geom.equalsExact(that.geom);//fast equality for normalized geometries
  }

  @Override
  public int hashCode() {
    //FYI if geometry.equalsExact(that.geometry), then their envelopes are the same.
    return geom.getEnvelopeInternal().hashCode();
  }
}
