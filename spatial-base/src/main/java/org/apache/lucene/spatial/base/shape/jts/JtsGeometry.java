/**
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

package org.apache.lucene.spatial.base.shape.jts;

import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Shape;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.Lineal;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Puntal;
import com.vividsolutions.jts.operation.predicate.RectangleIntersects;

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

  private GeometryFactory getGeometryFactory(Object context) {
    if(GeometryFactory.class.isInstance(context)) {
      return (GeometryFactory) context;
    }
    if(JtsShapeIO.class.isInstance(context)) {
      return ((JtsShapeIO) context).factory;
    }
    return new GeometryFactory();
  }

  @Override
  public IntersectCase intersect(Shape other, Object context) {
    BBox ext = other.getBoundingBox();
    if (!ext.hasSize()) {
      throw new IllegalArgumentException("the query shape must cover some area (not a point or line)");
    }

    // Quick test if this is outside
    Envelope gEnv = geo.getEnvelopeInternal();
    if (ext.getMinX() > gEnv.getMaxX() ||
        ext.getMaxX() < gEnv.getMinX() ||
        ext.getMinY() > gEnv.getMaxY() ||
        ext.getMaxY() < gEnv.getMinY()) {
      return IntersectCase.OUTSIDE;
    }

    Polygon qGeo = null;
    if (JtsEnvelope.class.isInstance(other)) {
      Envelope env = ((JtsEnvelope)other).envelope;
      qGeo = (Polygon) getGeometryFactory(context).toGeometry(env);
    } else if (JtsGeometry.class.isInstance(other)) {
      qGeo = (Polygon)((JtsGeometry)other).geo;
    } else if(BBox.class.isInstance(other)) {
      BBox e = (BBox)other;
      Envelope env = new Envelope(e.getMinX(), e.getMaxX(), e.getMinY(), e.getMaxY());
      qGeo = (Polygon) getGeometryFactory(context).toGeometry(env);
    } else {
      throw new IllegalArgumentException("this field only support intersectio with Extents or JTS Geometry");
    }

    //fast algorithm, short-circuit
    if (!RectangleIntersects.intersects(qGeo, geo)) {
      return IntersectCase.OUTSIDE;
    }

    //slower algorithm
    IntersectionMatrix matrix = geo.relate(qGeo);
    assert ! matrix.isDisjoint();//since rectangle intersection was true, shouldn't be disjoint
    if (matrix.isCovers()) {
      return IntersectCase.CONTAINS;
    }

    if (matrix.isCoveredBy()) {
      return IntersectCase.WITHIN;
    }

    assert matrix.isIntersects();
    return IntersectCase.INTERSECTS;
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
