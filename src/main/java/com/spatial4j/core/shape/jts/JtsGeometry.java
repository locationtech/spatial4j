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

package com.spatial4j.core.shape.jts;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.SpatialRelation;
import com.spatial4j.core.shape.impl.BufferedLineString;
import com.spatial4j.core.shape.impl.PointImpl;
import com.spatial4j.core.shape.impl.Range;
import com.spatial4j.core.shape.impl.RectangleImpl;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFilter;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Lineal;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Puntal;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;
import com.vividsolutions.jts.operation.valid.IsValidOp;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps a JTS {@link Geometry} (i.e. may be a polygon or basically anything).
 * JTS does a great deal of the hard work, but there is work here in handling
 * dateline wrap.
 */
public class JtsGeometry implements Shape {
  /** System property boolean that can disable auto validation in an assert. */
  public static final String SYSPROP_ASSERT_VALIDATE = "spatial4j.JtsGeometry.assertValidate";

  private final Geometry geom;//cannot be a direct instance of GeometryCollection as it doesn't support relate()
  private final boolean hasArea;
  private final Rectangle bbox;
  protected final JtsSpatialContext ctx;
  protected PreparedGeometry preparedGeometry;
  protected boolean validated = false;

  public JtsGeometry(Geometry geom, JtsSpatialContext ctx, boolean dateline180Check, boolean allowMultiOverlap) {
    this.ctx = ctx;
    //GeometryCollection isn't supported in relate()
    if (geom.getClass().equals(GeometryCollection.class))
      throw new IllegalArgumentException("JtsGeometry does not support GeometryCollection but does support its subclasses.");

    //NOTE: All this logic is fairly expensive. There are some short-circuit checks though.
    if (ctx.isGeo()) {
      //Unwraps the geometry across the dateline so it exceeds the standard geo bounds (-180 to +180).
      if (dateline180Check)
        unwrapDateline(geom);//potentially modifies geom
      //If given multiple overlapping polygons, fix it by union
      if (allowMultiOverlap)
        geom = unionGeometryCollection(geom);//returns same or new geom

      //Cuts an unwrapped geometry back into overlaid pages in the standard geo bounds.
      geom = cutUnwrappedGeomInto360(geom);//returns same or new geom
      assert geom.getEnvelopeInternal().getWidth() <= 360;
      assert ! geom.getClass().equals(GeometryCollection.class) : "GeometryCollection unsupported";//double check

      //Compute bbox
      bbox = computeGeoBBox(geom);
    } else {//not geo
      //If given multiple overlapping polygons, fix it by union
      if (allowMultiOverlap)
        geom = unionGeometryCollection(geom);//returns same or new geom

      Envelope env = geom.getEnvelopeInternal();
      bbox = new RectangleImpl(env.getMinX(), env.getMaxX(), env.getMinY(), env.getMaxY(), ctx);
    }
    geom.getEnvelopeInternal();//ensure envelope is cached internally, which is lazy evaluated. Keeps this thread-safe.

    this.geom = geom;
    assert assertValidate();//kinda expensive but caches valid state

    this.hasArea = !((geom instanceof Lineal) || (geom instanceof Puntal));
  }

  /** called via assertion */
  private boolean assertValidate() {
    String assertValidate = System.getProperty(SYSPROP_ASSERT_VALIDATE);
    if (assertValidate == null || Boolean.parseBoolean(assertValidate))
      validate();
    return true;
  }

  /**
   * Validates the shape, throwing a descriptive error if it isn't valid. Note that this
   * is usually called automatically by default, but that can be disabled.
   *
   * @throws InvalidShapeException with descriptive error if the shape isn't valid
   */
  public void validate() throws InvalidShapeException {
    if (!validated) {
      IsValidOp isValidOp = new IsValidOp(geom);
      if (!isValidOp.isValid())
        throw new InvalidShapeException(isValidOp.getValidationError().toString());
      validated = true;
    }
  }

  /**
   * Adds an index to this class internally to compute spatial relations faster. In JTS this
   * is called a {@link com.vividsolutions.jts.geom.prep.PreparedGeometry}.  This
   * isn't done by default because it takes some time to do the optimization, and it uses more
   * memory.  Calling this method isn't thread-safe so be careful when this is done. If it was
   * already indexed then nothing happens.
   */
  public void index() {
    if (preparedGeometry == null)
      preparedGeometry = PreparedGeometryFactory.prepare(geom);
  }

  @Override
  public boolean isEmpty() {
    return geom.isEmpty();
  }

  /** Given {@code geoms} which has already been checked for being in world
   * bounds, return the minimal longitude range of the bounding box.
   */
  protected Rectangle computeGeoBBox(Geometry geoms) {
    if (geoms.isEmpty())
      return new RectangleImpl(Double.NaN, Double.NaN, Double.NaN, Double.NaN, ctx);
    final Envelope env = geoms.getEnvelopeInternal();//for minY & maxY (simple)
    if (env.getWidth() > 180 && geoms.getNumGeometries() > 1)  {
      // This is ShapeCollection's bbox algorithm
      Range xRange = null;
      for (int i = 0; i < geoms.getNumGeometries(); i++ ) {
        Envelope envI = geoms.getGeometryN(i).getEnvelopeInternal();
        Range xRange2 = new Range.LongitudeRange(envI.getMinX(), envI.getMaxX());
        if (xRange == null) {
          xRange = xRange2;
        } else {
          xRange = xRange.expandTo(xRange2);
        }
        if (xRange == Range.LongitudeRange.WORLD_180E180W)
          break; // can't grow any bigger
      }
      return new RectangleImpl(xRange.getMin(), xRange.getMax(), env.getMinY(), env.getMaxY(), ctx);
    } else {
      return new RectangleImpl(env.getMinX(), env.getMaxX(), env.getMinY(), env.getMaxY(), ctx);
    }
  }

  @Override
  public JtsGeometry getBuffered(double distance, SpatialContext ctx) {
    //TODO doesn't work correctly across the dateline. The buffering needs to happen
    // when it's transiently unrolled, prior to being sliced.
    return this.ctx.makeShape(geom.buffer(distance), true, true);
  }

  @Override
  public boolean hasArea() {
    return hasArea;
  }

  @Override
  public double getArea(SpatialContext ctx) {
    double geomArea = geom.getArea();
    if (ctx == null || geomArea == 0)
      return geomArea;
    //Use the area proportional to how filled the bbox is.
    double bboxArea = getBoundingBox().getArea(null);//plain 2d area
    assert bboxArea >= geomArea;
    double filledRatio = geomArea / bboxArea;
    return getBoundingBox().getArea(ctx) * filledRatio;
    // (Future: if we know we use an equal-area projection then we don't need to
    //  estimate)
  }

  @Override
  public Rectangle getBoundingBox() {
    return bbox;
  }

  @Override
  public JtsPoint getCenter() {
    if (isEmpty()) //geom.getCentroid == null
      return new JtsPoint(ctx.getGeometryFactory().createPoint((Coordinate)null), ctx);
    return new JtsPoint(geom.getCentroid(), ctx);
  }

  @Override
  public SpatialRelation relate(Shape other) {
    if (other instanceof Point)
      return relate((Point)other);
    else if (other instanceof Rectangle)
      return relate((Rectangle) other);
    else if (other instanceof Circle)
      return relate((Circle) other);
    else if (other instanceof JtsGeometry)
      return relate((JtsGeometry) other);
    else if (other instanceof BufferedLineString)
      throw new UnsupportedOperationException("Can't use BufferedLineString with JtsGeometry");
    return other.relate(this).transpose();
  }

  public SpatialRelation relate(Point pt) {
    if (!getBoundingBox().relate(pt).intersects())
      return SpatialRelation.DISJOINT;
    Geometry ptGeom;
    if (pt instanceof JtsPoint)
      ptGeom = ((JtsPoint)pt).getGeom();
    else
      ptGeom = ctx.getGeometryFactory().createPoint(new Coordinate(pt.getX(), pt.getY()));
    return relate(ptGeom);//is point-optimized
  }

  public SpatialRelation relate(Rectangle rectangle) {
    SpatialRelation bboxR = bbox.relate(rectangle);
    if (bboxR == SpatialRelation.WITHIN || bboxR == SpatialRelation.DISJOINT)
      return bboxR;
    // FYI, the right answer could still be DISJOINT or WITHIN, but we don't know yet.
    return relate(ctx.getGeometryFrom(rectangle));
  }

  public SpatialRelation relate(Circle circle) {
    SpatialRelation bboxR = bbox.relate(circle);
    if (bboxR == SpatialRelation.WITHIN || bboxR == SpatialRelation.DISJOINT)
      return bboxR;

    //Test each point to see how many of them are outside of the circle.
    //TODO consider instead using geom.apply(CoordinateSequenceFilter) -- maybe faster since avoids Coordinate[] allocation
    Coordinate[] coords = geom.getCoordinates();
    int outside = 0;
    int i = 0;
    for (Coordinate coord : coords) {
      i++;
      SpatialRelation sect = circle.relate(new PointImpl(coord.x, coord.y, ctx));
      if (sect == SpatialRelation.DISJOINT)
        outside++;
      if (i != outside && outside != 0)//short circuit: partially outside, partially inside
        return SpatialRelation.INTERSECTS;
    }
    if (i == outside) {
      return (relate(circle.getCenter()) == SpatialRelation.DISJOINT)
          ? SpatialRelation.DISJOINT : SpatialRelation.CONTAINS;
    }
    assert outside == 0;
    return SpatialRelation.WITHIN;
  }

  public SpatialRelation relate(JtsGeometry jtsGeometry) {
    //don't bother checking bbox since geom.relate() does this already
    return relate(jtsGeometry.geom);
  }

  protected SpatialRelation relate(Geometry oGeom) {
    //see http://docs.geotools.org/latest/userguide/library/jts/dim9.html#preparedgeometry
    if (oGeom instanceof com.vividsolutions.jts.geom.Point) {
      if (preparedGeometry != null)
        return preparedGeometry.disjoint(oGeom) ? SpatialRelation.DISJOINT : SpatialRelation.CONTAINS;
      return geom.disjoint(oGeom) ? SpatialRelation.DISJOINT : SpatialRelation.CONTAINS;
    }
    if (preparedGeometry == null)
      return intersectionMatrixToSpatialRelation(geom.relate(oGeom));
    else if (preparedGeometry.covers(oGeom))
      return SpatialRelation.CONTAINS;
    else if (preparedGeometry.coveredBy(oGeom))
      return SpatialRelation.WITHIN;
    else if (preparedGeometry.intersects(oGeom))
      return SpatialRelation.INTERSECTS;
    return SpatialRelation.DISJOINT;
  }

  public static SpatialRelation intersectionMatrixToSpatialRelation(IntersectionMatrix matrix) {
    //As indicated in SpatialRelation javadocs, Spatial4j CONTAINS & WITHIN are
    // OGC's COVERS & COVEREDBY
    if (matrix.isCovers())
      return SpatialRelation.CONTAINS;
    else if (matrix.isCoveredBy())
      return SpatialRelation.WITHIN;
    else if (matrix.isDisjoint())
      return SpatialRelation.DISJOINT;
    return SpatialRelation.INTERSECTS;
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

  /**
   * If <code>geom</code> spans the dateline, then this modifies it to be a
   * valid JTS geometry that extends to the right of the standard -180 to +180
   * width such that some points are greater than +180 but some remain less.
   * Takes care to invoke {@link com.vividsolutions.jts.geom.Geometry#geometryChanged()}
   * if needed.
   *
   * @return The number of times the geometry spans the dateline.  >= 0
   */
  private static int unwrapDateline(Geometry geom) {
    if (geom.getEnvelopeInternal().getWidth() < 180)
      return 0;//can't possibly cross the dateline
    final int[] crossings = {0};//an array so that an inner class can modify it.
    geom.apply(new GeometryFilter() {
      @Override
      public void filter(Geometry geom) {
        int cross = 0;
        if (geom instanceof LineString) {//note: LinearRing extends LineString
          if (geom.getEnvelopeInternal().getWidth() < 180)
            return;//can't possibly cross the dateline
          cross = unwrapDateline((LineString) geom);
        } else if (geom instanceof Polygon) {
          if (geom.getEnvelopeInternal().getWidth() < 180)
            return;//can't possibly cross the dateline
          cross = unwrapDateline((Polygon) geom);
        } else
          return;
        crossings[0] = Math.max(crossings[0], cross);
      }
    });//geom.apply()

    return crossings[0];
  }

  /** See {@link #unwrapDateline(Geometry)}. */
  private static int unwrapDateline(Polygon poly) {
    LineString exteriorRing = poly.getExteriorRing();
    int cross = unwrapDateline(exteriorRing);
    if (cross > 0) {
      //TODO TEST THIS! Maybe bug if doesn't cross but is in another page?
      for(int i = 0; i < poly.getNumInteriorRing(); i++) {
        LineString innerLineString = poly.getInteriorRingN(i);
        unwrapDateline(innerLineString);
        for(int shiftCount = 0; ! exteriorRing.contains(innerLineString); shiftCount++) {
          if (shiftCount > cross)
            throw new IllegalArgumentException("The inner ring doesn't appear to be within the exterior: "
                +exteriorRing+" inner: "+innerLineString);
          shiftGeomByX(innerLineString, 360);
        }
      }
      poly.geometryChanged();
    }
    return cross;
  }

  /** See {@link #unwrapDateline(Geometry)}. */
  private static int unwrapDateline(LineString lineString) {
    CoordinateSequence cseq = lineString.getCoordinateSequence();
    int size = cseq.size();
    if (size <= 1)
      return 0;

    int shiftX = 0;//invariant: == shiftXPage*360
    int shiftXPage = 0;
    int shiftXPageMin = 0/* <= 0 */, shiftXPageMax = 0; /* >= 0 */
    double prevX = cseq.getX(0);
    for(int i = 1; i < size; i++) {
      double thisX_orig = cseq.getX(i);
      assert thisX_orig >= -180 && thisX_orig <= 180 : "X not in geo bounds";
      double thisX = thisX_orig + shiftX;
      if (prevX - thisX > 180) {//cross dateline from left to right
        thisX += 360;
        shiftX += 360;
        shiftXPage += 1;
        shiftXPageMax = Math.max(shiftXPageMax,shiftXPage);
      } else if (thisX - prevX > 180) {//cross dateline from right to left
        thisX -= 360;
        shiftX -= 360;
        shiftXPage -= 1;
        shiftXPageMin = Math.min(shiftXPageMin,shiftXPage);
      }
      if (shiftXPage != 0)
        cseq.setOrdinate(i, CoordinateSequence.X, thisX);
      prevX = thisX;
    }
    if (lineString instanceof LinearRing) {
      assert cseq.getCoordinate(0).equals(cseq.getCoordinate(size-1));
      assert shiftXPage == 0;//starts and ends at 0
    }
    assert shiftXPageMax >= 0 && shiftXPageMin <= 0;
    //Unfortunately we are shifting again; it'd be nice to be smarter and shift once
    shiftGeomByX(lineString, shiftXPageMin * -360);
    int crossings = shiftXPageMax - shiftXPageMin;
    if (crossings > 0)
      lineString.geometryChanged();
    return crossings;
  }

  private static void shiftGeomByX(Geometry geom, final int xShift) {
    if (xShift == 0)
      return;
    geom.apply(new CoordinateSequenceFilter() {
      @Override
      public void filter(CoordinateSequence seq, int i) {
        seq.setOrdinate(i, CoordinateSequence.X, seq.getX(i) + xShift );
      }

      @Override public boolean isDone() { return false; }

      @Override public boolean isGeometryChanged() { return true; }
    });
  }

  private static Geometry unionGeometryCollection(Geometry geom) {
    if (geom instanceof GeometryCollection) {
      return geom.union();
    }
    return geom;
  }

  /**
   * This "pages" through standard geo boundaries offset by multiples of 360
   * longitudinally that intersect geom, and the intersecting results of a page
   * and the geom are shifted into the standard -180 to +180 and added to a new
   * geometry that is returned.
   */
  private static Geometry cutUnwrappedGeomInto360(Geometry geom) {
    Envelope geomEnv = geom.getEnvelopeInternal();
    if (geomEnv.getMinX() >= -180 && geomEnv.getMaxX() <= 180)
      return geom;
    assert geom.isValid() : "geom";

    //TODO opt: support geom's that start at negative pages --
    // ... will avoid need to previously shift in unwrapDateline(geom).
    List<Geometry> geomList = new ArrayList<Geometry>();
    //page 0 is the standard -180 to 180 range
    for (int page = 0; true; page++) {
      double minX = -180 + page * 360;
      if (geomEnv.getMaxX() <= minX)
        break;
      Geometry rect = geom.getFactory().toGeometry(new Envelope(minX, minX + 360, -90, 90));
      assert rect.isValid() : "rect";
      Geometry pageGeom = rect.intersection(geom);//JTS is doing some hard work
      assert pageGeom.isValid() : "pageGeom";

      shiftGeomByX(pageGeom, page * -360);
      geomList.add(pageGeom);
    }
    return UnaryUnionOp.union(geomList);
  }

//  private static Geometry removePolyHoles(Geometry geom) {
//    //TODO this does a deep copy of geom even if no changes needed; be smarter
//    GeometryTransformer gTrans = new GeometryTransformer() {
//      @Override
//      protected Geometry transformPolygon(Polygon geom, Geometry parent) {
//        if (geom.getNumInteriorRing() == 0)
//          return geom;
//        return factory.createPolygon((LinearRing) geom.getExteriorRing(),null);
//      }
//    };
//    return gTrans.transform(geom);
//  }
//
//  private static Geometry snapAndClean(Geometry geom) {
//    return new GeometrySnapper(geom).snapToSelf(GeometrySnapper.computeOverlaySnapTolerance(geom), true);
//  }
}
