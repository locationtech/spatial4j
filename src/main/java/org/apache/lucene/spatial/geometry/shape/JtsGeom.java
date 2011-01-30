/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.spatial.geometry.shape;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.predicate.RectangleIntersects;
import org.apache.lucene.queryParser.ParseException;

/**
 * A Geometry2D based on <a href="http://tsusiatsoftware.net/jts/main.html">JTS Topology Suite</a>.
 */
public class JtsGeom implements Geometry2D {
  static final GeometryFactory geometryFactory = new GeometryFactory();
  private final Geometry geometry;
  private transient final PreparedGeometry preparedGeometry;

  /**
   * Geometry should be normalized in order for equals() here to work consistently.
   */
  public JtsGeom(Geometry geometry) {
    if (geometry == null)
      throw new IllegalArgumentException();
    this.geometry = geometry;
    this.preparedGeometry = PreparedGeometryFactory.prepare(geometry);
  }

  /**
   * A factory method that parses a list of points comprising a simple polygon (closed and no holes).
   * The points are separated by
   * comma and the point is itself comma separated in latitude-longitude order. This implements the 1st draft of the
   * OpenSearch geo spec but it was made deprecated in the 2nd draft. It specified the order must be clockwise
   * but it does not matter to this implementation.
   * TODO - Note that support for spanning the date line is not available.
   */
  public static Geometry2D parsePolygon(String polygonArg) throws ParseException {
    //TODO support spanning the date-line!
    String[] latLons = polygonArg.split(",");
    if (latLons.length % 2 != 0 || latLons.length < 8)
      throw new ParseException("polygon should contain >= 4 pairs of comma separated lat,lons");
    Coordinate[] coordinates = new Coordinate[latLons.length / 2];
    LinearRing linearRing;
    try {
      for (int i = 0; i < coordinates.length; i++) {
        double lat = Double.parseDouble(latLons[i*2]);
        double lon = Double.parseDouble(latLons[i*2+1]);
        coordinates[i] = new Coordinate(lon,lat);//x,y
      }
      //TODO if last point != first then append a copy of first point to end?
      linearRing = geometryFactory.createLinearRing(coordinates);
    } catch (Exception e) {
      throw new ParseException(e.toString());
    }
    Geometry poly = geometryFactory.createPolygon(linearRing, null);
    if (!poly.isValid())
      throw new ParseException("Polygon is not valid. Is it intersecting itself?");
    poly.normalize();//for equalsExact() equality
    return new JtsGeom(poly);
  }

  /**
   * A factory method that parses the argument as Well Known Text (WKT).
   * TODO - Note that support for spanning the date line is not available.
   */
  public static Geometry2D parseGeometry(String geoArg) throws ParseException {
    Geometry geo;
    try {
      geo = new WKTReader(geometryFactory).read(geoArg);
    } catch (com.vividsolutions.jts.io.ParseException e) {
      throw new ParseException(e.toString());
    }
    geo.normalize();//for equalsExact() equality
    return new JtsGeom(geo);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JtsGeom that = (JtsGeom) o;
    return geometry.equalsExact(that.geometry);//fast equality for normalized geometries
  }

  @Override
  public int hashCode() {
    //FYI if geometry.equalsExact(that.geometry), then their envelopes are the same.
    return geometry.getEnvelopeInternal().hashCode();
  }

  @Override
  public String toString() {
    return geometry.toString();
  }

  @Override
  public void translate(Vector2D v) {
    throw new UnsupportedOperationException("TODO unimplemented");//TODO
  }

  @Override
  public boolean contains(Point2D p) {
    //TODO optimize implementation to use com.vividsolutions.jts.geom.prep.PreparedPolygonPredicate.isAnyTestComponentInTargetInterior()
    // because the current code path checks if the point is within twice, along with other unnecessary stuff.
    Point pt = geometryFactory.createPoint(new Coordinate(p.getX(), p.getY()));
    return preparedGeometry.contains(pt);
  }

  @Override
  public double area() {
    throw new UnsupportedOperationException("TODO unimplemented");//TODO
  }

  @Override
  public Point2D centroid() {
    throw new UnsupportedOperationException("TODO unimplemented");//TODO
  }

  @Override
  public Rectangle boundingRectangle() {
    Envelope env = geometry.getEnvelopeInternal();
    return new Rectangle(env.getMinX(),env.getMinY(),env.getMaxX(),env.getMaxY());
  }

  @Override
  /** The rectangle must not be a point or line or else a ClassCastException will occur. This is invoked by
   * {@link org.apache.lucene.spatial.geohash.GeoHashPrefixFilter} with a grid box so points/lines shouldn't happen.
   */
  public IntersectCase intersect(Rectangle r) {
    //See Geometry.intersects(Geometry) for some touch-points of classes involved
    Envelope rEnv = new Envelope(r.getMinX(),r.getMaxX(),r.getMinY(),r.getMaxY());
    Envelope gEnv = geometry.getEnvelopeInternal();

    if (!gEnv.intersects(rEnv))
        return IntersectCase.OUTSIDE;

    Polygon rGeo = (Polygon) geometryFactory.toGeometry(rEnv);//r must not be a point or line.
    //fast algorithm, short-circuit
    if (!RectangleIntersects.intersects (rGeo, geometry))
      return IntersectCase.OUTSIDE;
    //slower algorithm
    IntersectionMatrix matrix = geometry.relate(rGeo);
    assert ! matrix.isDisjoint();//since rectangle intersection was true, shouldn't be disjoint
    if (matrix.isCovers())
      return IntersectCase.CONTAINS;
    if (matrix.isCoveredBy())//TODO GeoHashPrefixFilter doesn't care about this distinction; remove this enum?
      return IntersectCase.WITHIN;
    assert matrix.isIntersects();
    return IntersectCase.INTERSECTS;
  }
}
