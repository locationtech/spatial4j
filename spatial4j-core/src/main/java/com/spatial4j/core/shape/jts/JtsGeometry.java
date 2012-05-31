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

public class JtsGeometry implements Shape {
  private final Geometry geom;
  private final boolean hasArea;
  private final Rectangle bbox;

  public JtsGeometry(Geometry geom) {
    this.geom = geom;

    this.hasArea = !((geom instanceof Lineal) || (geom instanceof Puntal));

    //note: getEnvelopeInternal() is lazy cached, so fetching now helps make this Geometry instance thread-safe
    //note: this bbox may be sub-optimal for dateline crossing as it may needlessly span the globe.
    //    TODO so consider using MultiShape's bounding box algorithm once it's geo smart.
    Envelope env = geom.getEnvelopeInternal();
    bbox = new RectangleImpl(env.getMinX(),env.getMaxX(),env.getMinY(),env.getMaxY());
  }

  public static SpatialRelation intersectionMatrixToSpatialRelation(IntersectionMatrix matrix) {
    SpatialRelation spatialRelation;
    if (matrix.isContains())
      spatialRelation = SpatialRelation.CONTAINS;
    else if (matrix.isCoveredBy())
      spatialRelation = SpatialRelation.WITHIN;
    else if (matrix.isDisjoint())
      spatialRelation = SpatialRelation.DISJOINT;
    else
      spatialRelation = SpatialRelation.INTERSECTS;
    return spatialRelation;
  }

  //----------------------------------------
  //----------------------------------------

  @Override
  public boolean hasArea() {
    return hasArea;
  }

  @Override
  public Rectangle getBoundingBox() {
    return bbox;
  }

  @Override
  public JtsPoint getCenter() {
    return new JtsPoint(geom.getCentroid());
  }

  @Override
  public SpatialRelation relate(Shape other, SpatialContext ctx) {
    if (other instanceof Point) {
      return relate((Point)other, ctx);
    }
    if (other instanceof Rectangle) {
      return relate((Rectangle) other, ctx);
    }
    if (other instanceof Circle) {
      return relate((Circle) other, ctx);
    }
    if (other instanceof JtsGeometry) {
      return relate((JtsGeometry) other);
    }
    return other.relate(this, ctx).transpose();
  }

  public SpatialRelation relate(Point pt, SpatialContext ctx) {
    //TODO if not jtsPoint, test against bbox to avoid JTS if disjoint
    JtsPoint jtsPoint = (JtsPoint) (pt instanceof JtsPoint ? pt : ctx.makePoint(pt.getX(), pt.getY()));
    return geom.disjoint(jtsPoint.getJtsPoint()) ? SpatialRelation.DISJOINT : SpatialRelation.CONTAINS;
  }

  public SpatialRelation relate(Rectangle rectangle, SpatialContext ctx) {
    SpatialRelation bboxR = bbox.relate(rectangle,ctx);
    if (bboxR == SpatialRelation.WITHIN || bboxR == SpatialRelation.DISJOINT)
      return bboxR;
    Geometry oGeom = ((JtsSpatialContext)ctx).getGeometryFrom(rectangle);
    return intersectionMatrixToSpatialRelation(geom.relate(oGeom));
  }

  public SpatialRelation relate(Circle circle, SpatialContext ctx) {
    SpatialRelation bboxR = bbox.relate(circle,ctx);
    if (bboxR == SpatialRelation.WITHIN || bboxR == SpatialRelation.DISJOINT)
      return bboxR;

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

  public SpatialRelation relate(JtsGeometry jtsGeometry) {
    Geometry oGeom = jtsGeometry.geom;
    //don't bother checking bbox since geom.relate() does this already
    return intersectionMatrixToSpatialRelation(geom.relate(oGeom));
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

  public Geometry getGeom() {
    return geom;
  }
}
