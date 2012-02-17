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

package com.googlecode.lucene.spatial.base.shape;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.predicate.RectangleIntersects;
import org.apache.lucene.spatial.base.shape.SpatialRelation;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.shape.*;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.simple.PointImpl;

public class JtsGeometry implements Shape {
  public final Geometry geo;
  private final boolean hasArea;

  public JtsGeometry(Geometry geo) {
    this.geo = geo;
    this.hasArea = !((Lineal.class.isInstance(geo)) || (Puntal.class.isInstance(geo)));
  }

  //----------------------------------------
  //----------------------------------------

  @Override
  public boolean hasArea() {
    return hasArea;
  }

  @Override
  public JtsEnvelope getBoundingBox() {
    return new JtsEnvelope(geo.getEnvelopeInternal());
  }

  @Override
  public JtsPoint getCenter() {
    return new JtsPoint(geo.getCentroid());
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
      Point pt = (Point)other;
      JtsPoint jtsPoint = (JtsPoint) (pt instanceof JtsPoint ? pt : ctx.makePoint(pt.getX(), pt.getY()));
      return geo.contains(jtsPoint.getJtsPoint()) ? SpatialRelation.INTERSECTS : SpatialRelation.DISJOINT;
    }

    Rectangle ext = other.getBoundingBox();
    if (!ext.hasArea()) {//TODO revisit the soundness of this logic
      throw new IllegalArgumentException("the query shape must cover some area (not a line)");
    }

    // Quick test if this is disjoint
    Envelope gEnv = geo.getEnvelopeInternal();
    if (ext.getMinX() > gEnv.getMaxX() ||
        ext.getMaxX() < gEnv.getMinX() ||
        ext.getMinY() > gEnv.getMaxY() ||
        ext.getMaxY() < gEnv.getMinY()) {
      return SpatialRelation.DISJOINT;
    }

    if (other instanceof Circle) {
      //Test each point to see how many of them are outside of the circle.
      Coordinate[] coords = geo.getCoordinates();
      int outside = 0;
      int i = 0;
      for (Coordinate coord : coords) {
        i++;
        SpatialRelation sect = other.relate(new PointImpl(coord.x, coord.y), ctx);
        if (sect == SpatialRelation.DISJOINT)
          outside++;
        if (i != outside && outside != 0)//short circuit: partially outside, partially inside
          return SpatialRelation.INTERSECTS;
      }
      if (i == outside) {
        return (relate(other.getCenter(), ctx) == SpatialRelation.DISJOINT)
            ? SpatialRelation.DISJOINT : SpatialRelation.CONTAINS;
      }
      assert outside == 0;
      return SpatialRelation.WITHIN;
    }
    
    Polygon qGeo = null;
    if (other instanceof Rectangle) {
      Envelope env;
      if (other instanceof JtsEnvelope) {
        env = ((JtsEnvelope)other).envelope;
      } else {
        Rectangle r = (Rectangle)other;
        env = new Envelope(r.getMinX(), r.getMaxX(), r.getMinY(), r.getMaxY());
      }
      qGeo = (Polygon) getGeometryFactory(ctx).toGeometry(env);
    } else if (other instanceof JtsGeometry) {
      qGeo = (Polygon)((JtsGeometry)other).geo;
    } else {
      throw new IllegalArgumentException("Incompatible intersection of "+this+" with "+other);
    }

    //fast algorithm, short-circuit
    if (!RectangleIntersects.intersects(qGeo, geo)) {
      return SpatialRelation.DISJOINT;
    }

    //slower algorithm
    IntersectionMatrix matrix = geo.relate(qGeo);
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

  @Override
  public String toString() {
    return geo.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JtsGeometry that = (JtsGeometry) o;
    return geo.equalsExact(that.geo);//fast equality for normalized geometries
  }

  @Override
  public int hashCode() {
    //FYI if geometry.equalsExact(that.geometry), then their envelopes are the same.
    return geo.getEnvelopeInternal().hashCode();
  }
}
