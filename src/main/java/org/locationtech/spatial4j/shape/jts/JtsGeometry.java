/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape.jts;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.distance.CartesianDistCalc;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.shape.*;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.impl.BBoxCalculator;
import org.locationtech.spatial4j.shape.impl.BufferedLineString;
import org.locationtech.spatial4j.shape.impl.RectangleImpl;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.operation.valid.IsValidOp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Wraps a JTS {@link Geometry} (i.e. may be a polygon or basically anything).
 * JTS does a great deal of the hard work, but there is work here in handling
 * dateline (aka anti-meridian) wrap.
 */
public class JtsGeometry extends BaseShape<JtsSpatialContext> {
  /** System property boolean that can disable auto validation in an assert. */
  public static final String SYSPROP_ASSERT_VALIDATE = "spatial4j.JtsGeometry.assertValidate";

  private final Geometry geom;//cannot be a direct instance of GeometryCollection as it doesn't support relate()
  private final boolean hasArea;
  private final Rectangle bbox;
  protected PreparedGeometry preparedGeometry;
  protected boolean validated = false;

  public JtsGeometry(Geometry geom, JtsSpatialContext ctx, boolean dateline180Check, boolean allowMultiOverlap) {
    super(ctx);
    //GeometryCollection isn't supported in relate()
    if (geom.getClass().equals(GeometryCollection.class)) {
      geom = narrowCollectionIfPossible((GeometryCollection)geom);
      if (geom == null) {
        throw new IllegalArgumentException("JtsGeometry does not support GeometryCollection but does support its subclasses.");
      }
    }

    //NOTE: All this logic is fairly expensive. There are some short-circuit checks though.
    if (geom.isEmpty()) {
      bbox = new RectangleImpl(Double.NaN, Double.NaN, Double.NaN, Double.NaN, this.ctx);
    } else if (ctx.isGeo()) {
      //Unwraps the geometry across the dateline so it exceeds the standard geo bounds (-180 to +180).
      if (dateline180Check)
        geom = unwrapDateline(geom);//returns same or new geom
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

  /**
   * Attempts to retype a geometry collection under the following circumstances, returning
   * null if the collection can not be retyped.
   * <ul>
   *    <li>Single object collections are collapsed down to the object.</li>
   *    <li>Homogenous collections are recast as the appropriate subclass.</li>
   * </ul>
   *
   * @see GeometryFactory#buildGeometry(Collection)
   */
  private Geometry narrowCollectionIfPossible(GeometryCollection gc) {
    List<Geometry> geoms = new ArrayList<>();
    for (int i = 0; i < gc.getNumGeometries(); i++) {
      geoms.add(gc.getGeometryN(i));
    }

    Geometry result = gc.getFactory().buildGeometry(geoms);
    return !result.getClass().equals(GeometryCollection.class) ? result : null;
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
   * Determines if the shape has been indexed.
   */
  boolean isIndexed() {
    return preparedGeometry != null;
  }

  /**
   * Adds an index to this class internally to compute spatial relations faster. In JTS this
   * is called a {@link org.locationtech.jts.geom.prep.PreparedGeometry}.  This
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
    return bbox.isEmpty(); // fast
  }

  /** Given {@code geoms} which has already been checked for being in world
   * bounds, return the minimal longitude range of the bounding box.
   */
  protected Rectangle computeGeoBBox(Geometry geoms) {
    final Envelope env = geoms.getEnvelopeInternal();//for minY & maxY (simple)
    if (ctx.isGeo() && env.getWidth() > 180 && geoms.getNumGeometries() > 1)  {
      // This is ShapeCollection's bbox algorithm
      BBoxCalculator bboxCalc = new BBoxCalculator(ctx);
      for (int i = 0; i < geoms.getNumGeometries(); i++ ) {
        Envelope envI = geoms.getGeometryN(i).getEnvelopeInternal();
        bboxCalc.expandXRange(envI.getMinX(), envI.getMaxX());
        if (bboxCalc.doesXWorldWrap())
          break; // can't grow any bigger
      }
      return new RectangleImpl(bboxCalc.getMinX(), bboxCalc.getMaxX(), env.getMinY(), env.getMaxY(), ctx);
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

  public SpatialRelation relate(final Circle circle) {
    SpatialRelation bboxR = bbox.relate(circle);
    if (bboxR == SpatialRelation.WITHIN || bboxR == SpatialRelation.DISJOINT)
      return bboxR;
    // The result could be anything still.

    final SpatialRelation[] result = {null};
    // Visit each geometry (this geom might contain others).
    geom.apply(new GeometryFilter() {

      // We use cartesian math.  It's a limitation/assumption when working with JTS.  When geo=true (i.e. we're using
      //   WGS84 instead of a projected coordinate system), the errors here will be pretty terrible east-west.  At
      //   60 degrees latitude, the circle will work as if it has half the width it should.
      //   Instead, consider converting the circle to a polygon first (not great but better), or projecting both first.
      //   Ideally, use Geo3D.
      final CartesianDistCalc calcSqd = CartesianDistCalc.INSTANCE_SQUARED;
      final double radiusSquared = circle.getRadius() * circle.getRadius();
      final Geometry ctrGeom = ctx.getGeometryFrom(circle.getCenter());

      @Override
      public void filter(Geometry geom) {
        if (result[0] == SpatialRelation.INTERSECTS || result[0] == SpatialRelation.CONTAINS) {
          // a previous filter(geom) call had a result that won't be changed no matter how this geom relates
          return;
        }

        if (geom instanceof Polygon) {
          Polygon polygon = (Polygon) geom;
          SpatialRelation rel = relateEnclosedRing((LinearRing) polygon.getExteriorRing());
          // if rel == INTERSECTS or WITHIN or DISJOINT; done.  But CONTAINS...
          if (rel == SpatialRelation.CONTAINS) {
            // if the poly outer ring contains the circle, check the holes. Could become DISJOINT or INTERSECTS.
            HOLE_LOOP: for (int i = 0; i < polygon.getNumInteriorRing(); i++){
              // TODO short-circuit based on the hole's bbox if it's disjoint or within the circle.
              switch (relateEnclosedRing((LinearRing) polygon.getInteriorRingN(i))) {
                case WITHIN:// fall through
                case INTERSECTS:
                  rel = SpatialRelation.INTERSECTS;
                  break HOLE_LOOP;
                case CONTAINS:
                  rel = SpatialRelation.DISJOINT;
                  break HOLE_LOOP;
                //case DISJOINT: break; // continue hole loop
              }
            }
          }
          result[0] = rel.combine(result[0]);
        } else if (geom instanceof LineString) {
          LineString lineString = (LineString) geom;
          SpatialRelation rel = relateLineString(lineString);
          result[0] = rel.combine(result[0]);
        } else if (geom instanceof org.locationtech.jts.geom.Point) {
          org.locationtech.jts.geom.Point point = (org.locationtech.jts.geom.Point) geom;
          SpatialRelation rel =
                  calcSqd.distance(circle.getCenter(), point.getX(), point.getY()) > radiusSquared
                          ? SpatialRelation.DISJOINT : SpatialRelation.WITHIN;
          result[0] = rel.combine(result[0]);
        }
        // else it's going to be some GeometryCollection and we'll visit the contents.
      }

      /** As if the ring is the outer ring of a polygon */
      SpatialRelation relateEnclosedRing(LinearRing ring) {
        SpatialRelation rel = relateLineString(ring);
        if (rel == SpatialRelation.DISJOINT
                && ctx.getGeometryFactory().createPolygon(ring, null).contains(ctrGeom)) {
          // If it contains the circle center point, then the result is CONTAINS
          rel = SpatialRelation.CONTAINS;
        }
        return rel;
      }

      SpatialRelation relateLineString(LineString lineString) {
        final CoordinateSequence seq = lineString.getCoordinateSequence();
        final boolean isRing = lineString instanceof LinearRing;
        int numOutside = 0;
        // Compare the coordinates:
        for (int i = 0, numComparisons = 0; i < seq.size(); i++) {
          if (i == 0 && isRing) {
            continue;
          }
          numComparisons++;
          boolean outside = calcSqd.distance(circle.getCenter(), seq.getX(i), seq.getY(i)) > radiusSquared;
          if (outside) {
            numOutside++;
          }
          // If the comparisons have a mix of outside/inside, then we can short-circuit INTERSECTS.
          if (numComparisons != numOutside && numOutside != 0) {
            assert numComparisons > 1;
            return SpatialRelation.INTERSECTS;
          }
        }
        // Either all vertices are outside or inside, by this stage.
        if (numOutside == 0) { // all inside
          return SpatialRelation.WITHIN.combine(result[0]);
        }
        // They are all outside.
        // Check the edges (line segments) to see if any are inside.
        for (int i = 1; i < seq.size(); i++) {
          boolean outside = calcSqd.distanceToLineSegment(
                  circle.getCenter(), seq.getX(i-1), seq.getY(i-1), seq.getX(i), seq.getY(i))
                  > radiusSquared;
          if (!outside) {
            return SpatialRelation.INTERSECTS;
          }
        }
        return SpatialRelation.DISJOINT;
      }
    });

    return result[0] == null ? SpatialRelation.DISJOINT : result[0];
  }

  public SpatialRelation relate(JtsGeometry jtsGeometry) {
    //don't bother checking bbox since geom.relate() does this already
    return relate(jtsGeometry.geom);
  }

  protected SpatialRelation relate(Geometry oGeom) {
    //see http://docs.geotools.org/latest/userguide/library/jts/dim9.html#preparedgeometry
    if (oGeom instanceof org.locationtech.jts.geom.Point) {
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
   * If <code>geom</code> spans the dateline (aka anti-meridian), then this modifies it to be a
   * valid JTS geometry that extends to the right of the standard -180 to +180
   * width such that some points are greater than +180 but some remain less.
   *
   * @return The same geometry or a new one if it was unwrapped
   */
  private static Geometry unwrapDateline(Geometry geom) {
    if (geom.getEnvelopeInternal().getWidth() < 180)
      return geom;//can't possibly cross the dateline

    // if a multi-geom:  (this is purely an optimization to avoid cloning more than we need to)
    if (geom instanceof GeometryCollection) {
      if (geom instanceof MultiPoint) {
        return geom; // always safe since no point crosses the dateline (on it is okay)
      }
      GeometryCollection gc = (GeometryCollection) geom;
      List<Geometry> list = new ArrayList<>(gc.getNumGeometries());
      boolean didUnwrap = false;
      for (int n = 0; n < gc.getNumGeometries(); n++) {
        Geometry geometryN = gc.getGeometryN(n);
        Geometry geometryUnwrapped = unwrapDateline(geometryN); // recursion
        list.add(geometryUnwrapped);
        didUnwrap |= (geometryUnwrapped != geometryN);
      }
      return !didUnwrap ? geom : geom.getFactory().buildGeometry(list);
    }

    // a geom (not multi):

    Geometry newGeom = geom.copy(); // clone

    final int[] crossings = {0};//an array so that an inner class can modify it.
    newGeom.apply(new GeometryFilter() {
      @Override
      public void filter(Geometry geom) {
        int cross;
        if (geom instanceof LineString) {//note: LinearRing extends LineString
          if (geom.getEnvelopeInternal().getWidth() < 180)
            return;//can't possibly cross the dateline
          cross = unwrapDateline((LineString) geom);
        } else if (geom instanceof Polygon) {
          if (geom.getEnvelopeInternal().getWidth() < 180)
            return;//can't possibly cross the dateline
          cross = unwrapDateline((Polygon) geom);
        } else {
          // The only other JTS subclass of Geometry is a Point, which can't cross anything.
          //  If the geom is something custom, we don't know what else to do but return.
          return;
        }
        crossings[0] = Math.max(crossings[0], cross);
      }
    });//geom.apply()

    if (crossings[0] > 0) {
      newGeom.geometryChanged();
      return newGeom;
    } else {
      return geom; // original
    }
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
