/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape.jts;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.jts.DatelineRule;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import org.locationtech.spatial4j.context.jts.ValidationRule;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.io.ShapeReader;
import org.locationtech.spatial4j.shape.Circle;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.impl.ShapeFactoryImpl;
import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.util.GeometricShapeFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Enhances {@link ShapeFactoryImpl} with support for Polygons
 * using <a href="https://sourceforge.net/projects/jts-topo-suite/">JTS</a>.
 * To the extent possible, our {@link JtsGeometry} adds some amount of geodetic support over
 * vanilla JTS which only has a Euclidean (flat plane) model.
 */
public class JtsShapeFactory extends ShapeFactoryImpl {

  protected static final LinearRing[] EMPTY_HOLES = new LinearRing[0];

  protected final GeometryFactory geometryFactory;

  protected final boolean allowMultiOverlap;
  protected final boolean useJtsPoint;
  protected final boolean useJtsLineString;
  protected final boolean useJtsMulti;
  protected final DatelineRule datelineRule;
  protected final ValidationRule validationRule;
  protected final boolean autoIndex;

  /**
   * Called by {@link org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory#newSpatialContext()}.
   */
  public JtsShapeFactory(JtsSpatialContext ctx, JtsSpatialContextFactory factory) {
    super(ctx, factory);
    this.geometryFactory = factory.getGeometryFactory();

    this.allowMultiOverlap = factory.allowMultiOverlap;
    this.useJtsPoint = factory.useJtsPoint;
    this.useJtsLineString = factory.useJtsLineString;
    this.useJtsMulti = factory.useJtsMulti;
    this.datelineRule = factory.datelineRule;
    this.validationRule = factory.validationRule;
    this.autoIndex = factory.autoIndex;
  }

  /**
   * If geom might be a multi geometry of some kind, then might multiple
   * component geometries overlap? Strict OGC says this is invalid but we
   * can accept it by computing the union. Note: Our ShapeCollection mostly
   * doesn't care but it has a method related to this
   * {@link org.locationtech.spatial4j.shape.ShapeCollection#relateContainsShortCircuits()}.
   */
  public boolean isAllowMultiOverlap() {
    return allowMultiOverlap;
  }

  /**
   * Returns the rule used to handle geometry objects that have dateline (aka anti-meridian) crossing considerations.
   */
  public DatelineRule getDatelineRule() {
    return datelineRule;
  }

  /**
   * Returns the rule used to handle errors when creating a JTS {@link Geometry}, particularly after it has been
   * read from one of the {@link ShapeReader}s.
   */
  public ValidationRule getValidationRule() {
    return validationRule;
  }

  /**
   * If JtsGeometry shapes should be automatically "prepared" (i.e. optimized) when read via from a {@link ShapeReader}.
   *
   * @see org.locationtech.spatial4j.shape.jts.JtsGeometry#index()
   */
  public boolean isAutoIndex() {
    return autoIndex;
  }

  @Override
  public double normX(double x) {
    x = super.normX(x);
    return geometryFactory.getPrecisionModel().makePrecise(x);
  }

  @Override
  public double normY(double y) {
    y = super.normY(y);
    return geometryFactory.getPrecisionModel().makePrecise(y);
  }

  @Override
  public double normZ(double z) {
    z = super.normZ(z);
    return geometryFactory.getPrecisionModel().makePrecise(z);
  }

  @Override
  public double normDist(double d) {
    return geometryFactory.getPrecisionModel().makePrecise(d);
  }

  /**
   * Gets a JTS {@link Geometry} for the given {@link Shape}. Some shapes hold a
   * JTS geometry whereas new ones must be created for the rest.
   * @param shape Not null
   * @return Not null
   */
  public Geometry getGeometryFrom(Shape shape) {
    if (shape instanceof JtsGeometry) {
      return ((JtsGeometry)shape).getGeom();
    }
    if (shape instanceof JtsPoint) {
      return ((JtsPoint) shape).getGeom();
    }
    if (shape instanceof Point) {
      Point point = (Point) shape;
      return geometryFactory.createPoint(new Coordinate(point.getX(),point.getY()));
    }
    if (shape instanceof Rectangle) {
      Rectangle r = (Rectangle)shape;
      if (r.getCrossesDateLine()) {
        Collection<Geometry> pair = new ArrayList<>(2);
        pair.add(geometryFactory.toGeometry(new Envelope(
                r.getMinX(), ctx.getWorldBounds().getMaxX(), r.getMinY(), r.getMaxY())));
        pair.add(geometryFactory.toGeometry(new Envelope(
                ctx.getWorldBounds().getMinX(), r.getMaxX(), r.getMinY(), r.getMaxY())));
        return geometryFactory.buildGeometry(pair);//a MultiPolygon or MultiLineString
      } else {
        return geometryFactory.toGeometry(new Envelope(r.getMinX(), r.getMaxX(), r.getMinY(), r.getMaxY()));
      }
    }
    if (shape instanceof Circle) {
      // FYI Some interesting code for this is here:
      //  http://docs.codehaus.org/display/GEOTDOC/01+How+to+Create+a+Geometry#01HowtoCreateaGeometry-CreatingaCircle
      //TODO This should ideally have a geodetic version
      Circle circle = (Circle)shape;
      if (circle.getBoundingBox().getCrossesDateLine())
        throw new IllegalArgumentException("Doesn't support dateline cross yet: "+circle);//TODO
      GeometricShapeFactory gsf = new GeometricShapeFactory(geometryFactory);
      gsf.setSize(circle.getBoundingBox().getWidth());
      gsf.setNumPoints(4*25);//multiple of 4 is best
      gsf.setCentre(new Coordinate(circle.getCenter().getX(), circle.getCenter().getY()));
      return gsf.createCircle();
    }
    //TODO add BufferedLineString
    throw new InvalidShapeException("can't make Geometry from: " + shape);
  }

  /** Should {@link #pointXY(double, double)} return {@link JtsPoint}? */
  public boolean useJtsPoint() {
    return useJtsPoint;
  }

  @Override
  public Point pointXY(double x, double y) {
    return pointXYZ(x, y, Coordinate.NULL_ORDINATE);
  }

  @Override
  public Point pointXYZ(double x, double y, double z) {
    if (!useJtsPoint())
      return super.pointXY(x, y);// ignore z
    //A Jts Point is fairly heavyweight!  TODO could/should we optimize this? SingleCoordinateSequence
    verifyX(x);
    verifyY(y);
    verifyZ(z);
    // verifyZ(z)?
    Coordinate coord = Double.isNaN(x) ? null : new Coordinate(x, y, z);
    return new JtsPoint(geometryFactory.createPoint(coord), (JtsSpatialContext) ctx);
  }

  /** Should {@link #lineString(java.util.List,double)} return {@link JtsGeometry}? */
  public boolean useJtsLineString() {
    //BufferedLineString doesn't yet do dateline cross, and can't yet be relate()'ed with a
    // JTS geometry
    return useJtsLineString;
  }

  @Override
  public Shape lineString(List<Point> points, double bufferDistance) {
    if (!useJtsLineString())
      return super.lineString(points, bufferDistance);
    //convert List<Point> to Coordinate[]
    Coordinate[] coords = new Coordinate[points.size()];
    for (int i = 0; i < coords.length; i++) {
      Point p = points.get(i);
      if (p instanceof JtsPoint) {
        JtsPoint jtsPoint = (JtsPoint) p;
        coords[i] = jtsPoint.getGeom().getCoordinate();
      } else {
        coords[i] = new Coordinate(p.getX(), p.getY());
      }
    }
    JtsGeometry shape = makeShape(geometryFactory.createLineString(coords));
    if(bufferDistance!=0) {
      return shape.getBuffered(bufferDistance, ctx);
    }
    return shape;
  }

  @Override
  public LineStringBuilder lineString() {
    if (!useJtsLineString())
      return super.lineString();
    return new JtsLineStringBuilder();
  }

  private class JtsLineStringBuilder extends CoordinatesAccumulator<JtsLineStringBuilder>
          implements LineStringBuilder {
    protected double bufDistance;

    public JtsLineStringBuilder() {
    }

    @Override
    public LineStringBuilder buffer(double distance) {
      this.bufDistance = distance;
      return this;
    }

    @Override
    public Shape build() {
      Geometry geom = buildLineStringGeom();
      if (bufDistance != 0.0) {
        geom = geom.buffer(bufDistance);
      }
      return makeShape(geom);
    }

    LineString buildLineStringGeom() {
      return geometryFactory.createLineString(getCoordsArray());
    }
  }

  @Override
  public PolygonBuilder polygon() {
    return new JtsPolygonBuilder();
  }

  private class JtsPolygonBuilder extends CoordinatesAccumulator<JtsPolygonBuilder>
          implements PolygonBuilder {

    List<LinearRing> holes;// lazy instantiated

    @Override
    public JtsHoleBuilder hole() {
      return new JtsHoleBuilder();
    }

    private class JtsHoleBuilder extends CoordinatesAccumulator<JtsHoleBuilder>
            implements PolygonBuilder.HoleBuilder {

      @Override
      public JtsPolygonBuilder endHole() {
        LinearRing linearRing = geometryFactory.createLinearRing(getCoordsArray());
        if (JtsPolygonBuilder.this.holes == null) {
          JtsPolygonBuilder.this.holes = new ArrayList<>(4);//short
        }
        JtsPolygonBuilder.this.holes.add(linearRing);
        return JtsPolygonBuilder.this;
      }
    }

    @Override
    public Shape build() {
      return makeShapeFromGeometry(buildPolygonGeom());
    }

    @Override
    public Shape buildOrRect() {
      Polygon geom = buildPolygonGeom();
      if (geom.isRectangle()) {
        return makeRectFromRectangularPoly(geom);
      }
      return makeShapeFromGeometry(geom);
    }

    Polygon buildPolygonGeom() {
      LinearRing outerRing = geometryFactory.createLinearRing(getCoordsArray());
      LinearRing[] holeRings = holes == null ? EMPTY_HOLES : holes.toArray(new LinearRing[this.holes.size()]);
      return geometryFactory.createPolygon(outerRing, holeRings);
    }

  } // class JtsPolygonBuilder

  private abstract class CoordinatesAccumulator<T extends CoordinatesAccumulator> {
    protected List<Coordinate> coordinates = new ArrayList<>();

    public T pointXY(double x, double y) {
      return pointXYZ(x, y, Coordinate.NULL_ORDINATE);
    }

    public T pointXYZ(double x, double y, double z) {
      verifyX(x);
      verifyY(y);
      coordinates.add(new Coordinate(x, y, z));
      return getThis();
    }

    public T pointLatLon(double latitude, double longitude) {
      return pointXYZ(longitude, latitude, Coordinate.NULL_ORDINATE);
    }
    // TODO would be be useful to add other ways of providing points?  e.g. point(Coordinate)?

    // TODO consider wrapping the List<Coordinate> in a custom CoordinateSequence and then (conditionally) use
    //  geometryFactory's coordinateSequenceFactory to create a new CS if configured to do so.
    //  Also consider instead natively storing the double[] and then auto-expanding on pointXY* as needed.
    protected Coordinate[] getCoordsArray() {
      return coordinates.toArray(new Coordinate[coordinates.size()]);
    }

    @SuppressWarnings("unchecked")
    protected T getThis() { return (T) this; }
  }

  /** Whether {@link #multiPoint()}, {@link #multiLineString()}, and {@link #multiPolygon()} should all use JTS's
   * subclasses of {@link GeometryCollection} instead of Spatial4j's basic impl.  The general {@link #multiShape(Class)}
   * will never use {@link GeometryCollection} because that class doesn't support relations. */
  public boolean useJtsMulti() {
    return useJtsMulti;
  }

  @Override
  public MultiPointBuilder multiPoint() {
    if (!useJtsMulti) {
      return super.multiPoint();
    }
    return new JtsMultiPointBuilder();
  }

  private class JtsMultiPointBuilder extends CoordinatesAccumulator<JtsMultiPointBuilder> implements MultiPointBuilder {
    @Override
    public Shape build() {
      return makeShape(geometryFactory.createMultiPoint(getCoordsArray()));
    }
  }

  @Override
  public MultiLineStringBuilder multiLineString() {
    if (!useJtsMulti) {
      return super.multiLineString();
    }
    return new JtsMultiLineStringBuilder();
  }

  private class JtsMultiLineStringBuilder implements MultiLineStringBuilder {
    List<LineString> geoms = new ArrayList<>();

    @Override
    public LineStringBuilder lineString() {
      return new JtsLineStringBuilder();
    }

    @Override
    public MultiLineStringBuilder add(LineStringBuilder lineStringBuilder) {
      geoms.add(((JtsLineStringBuilder)lineStringBuilder).buildLineStringGeom());
      return this;
    }

    @Override
    public Shape build() {
      return makeShape(geometryFactory.createMultiLineString(geoms.toArray(new LineString[geoms.size()])));
    }
  }

  @Override
  public MultiPolygonBuilder multiPolygon() {
    if (!useJtsMulti) {
      return super.multiPolygon();
    }
    return new JtsMultiPolygonBuilder();
  }

  private class JtsMultiPolygonBuilder implements MultiPolygonBuilder {
    List<Polygon> geoms = new ArrayList<>();

    @Override
    public PolygonBuilder polygon() {
      return new JtsPolygonBuilder();
    }

    @Override
    public MultiPolygonBuilder add(PolygonBuilder polygonBuilder) {
      geoms.add(((JtsPolygonBuilder)polygonBuilder).buildPolygonGeom());
      return this;
    }

    @Override
    public Shape build() {
      return makeShape(geometryFactory.createMultiPolygon(geoms.toArray(new Polygon[geoms.size()])));
    }
  }

  @Override
  public <T extends Shape> MultiShapeBuilder<T> multiShape(Class<T> shapeClass) {
    if (!useJtsMulti()) {
      return super.multiShape(shapeClass);
    }
    return new JtsMultiShapeBuilder<>();
  }

  // TODO: once we have typed shapes for Polygons & LineStrings, this logic could move to the superclass
  // (not JTS specific) and the multi* builders could take a Shape
  private class JtsMultiShapeBuilder<T extends Shape> extends GeneralShapeMultiShapeBuilder<T> {
    @Override
    public Shape build() {
      Class<?> last = null;
      List<Geometry> geoms = new ArrayList<>(shapes.size());
      for(Shape s : shapes) {
        if (last != null && last != s.getClass()) {
          return super.build();
        }
        if (s instanceof JtsGeometry) {
          geoms.add(((JtsGeometry)s).getGeom());
        } else if (s instanceof JtsPoint) {
          geoms.add(((JtsPoint)s).getGeom());
        } else {
          return super.build();
        }
        last = s.getClass();
      }

      return makeShapeFromGeometry(geometryFactory.buildGeometry(geoms));
    }
  }

  /**
   * INTERNAL Usually creates a JtsGeometry, potentially validating, repairing, and indexing ("preparing"). This method
   * is intended for use by {@link ShapeReader} instances.
   *
   * If given a direct instance of {@link GeometryCollection} then it's contents will be
   * recursively converted and then the resulting list will be passed to
   * {@link SpatialContext#makeCollection(List)} and returned.
   *
   * If given a {@link org.locationtech.jts.geom.Point} then {@link SpatialContext#makePoint(double, double)}
   * is called, which will return a {@link JtsPoint} if {@link JtsSpatialContext#useJtsPoint()}; otherwise
   * a standard Spatial4j Point is returned.
   *
   * If given a {@link LineString} and if {@link JtsSpatialContext#useJtsLineString()} is true then
   * then the geometry's parts are exposed to call {@link SpatialContext#makeLineString(List)}.
   */
  // TODO should this be called always (consistent but sometimes not needed?)
  //   v.s. only from a ShapeReader (pre-ShapeFactory behavior)
  public Shape makeShapeFromGeometry(Geometry geom) {
    if (geom instanceof GeometryCollection) {
      // Direct instances of GeometryCollection can't be wrapped in JtsGeometry but can be expanded into
      //  a ShapeCollection.
      if (!useJtsMulti || geom.getClass() == GeometryCollection.class) {
        List<Shape> shapes = new ArrayList<>(geom.getNumGeometries());
        for (int i = 0; i < geom.getNumGeometries(); i++) {
          Geometry geomN = geom.getGeometryN(i);
          shapes.add(makeShapeFromGeometry(geomN));//recursion
        }
        return multiShape(shapes);
      }
    } else if (geom instanceof org.locationtech.jts.geom.Point) {
      org.locationtech.jts.geom.Point pt = (org.locationtech.jts.geom.Point) geom;
      return pointXY(pt.getX(), pt.getY());
    } else if (geom instanceof LineString) {
      if (!useJtsLineString()) {
        LineString lineString = (LineString) geom;
        List<Point> points = new ArrayList<>(lineString.getNumPoints());
        for (int i = 0; i < lineString.getNumPoints(); i++) {
          Coordinate coord = lineString.getCoordinateN(i);
          points.add(pointXY(coord.x, coord.y));
        }
        return lineString(points, 0);
      }
    }

    JtsGeometry jtsGeom;
    try {
      jtsGeom = makeShape(geom);
      if (getValidationRule() != ValidationRule.none)
        jtsGeom.validate();
    } catch (RuntimeException e) {
      // repair:
      if (getValidationRule() == ValidationRule.repairConvexHull) {
        jtsGeom = makeShape(geom.convexHull());
      } else if (getValidationRule() == ValidationRule.repairBuffer0) {
        jtsGeom = makeShape(geom.buffer(0));
      } else {
        // TODO there are other smarter things we could do like repairing inner holes and
        // subtracting
        // from outer repaired shell; but we needn't try too hard.
        throw e;
      }
    }
    return jtsGeom;
  }

  /**
   * INTERNAL
   * @see #makeShape(org.locationtech.jts.geom.Geometry)
   *
   * @param geom Non-null
   * @param dateline180Check if both this is true and {@link SpatialContext#isGeo()}, then JtsGeometry will check
   *                         for adjacent coordinates greater than 180 degrees longitude apart, and
   *                         it will do tricks to make that line segment (and the shape as a whole)
   *                         cross the dateline even though JTS doesn't have geodetic support.
   * @param allowMultiOverlap See {@link #isAllowMultiOverlap()}.
   */
  public JtsGeometry makeShape(Geometry geom, boolean dateline180Check, boolean allowMultiOverlap) {
    JtsGeometry jtsGeom = new JtsGeometry(geom, (JtsSpatialContext) ctx, dateline180Check, allowMultiOverlap);
    if (isAutoIndex()) {
      jtsGeom.index();
    }
    return jtsGeom;
  }

  /**
   * INTERNAL: Creates a {@link Shape} from a JTS {@link Geometry}. Generally, this shouldn't be
   * called when one of the other factory methods are available, such as for points. The caller
   * needs to have done some verification/normalization of the coordinates by now, if any.  Also,
   * note that direct instances of {@link GeometryCollection} isn't supported.
   *
   * Instead of calling this method, consider {@link #makeShapeFromGeometry(Geometry)}
   * which
   */
  public JtsGeometry makeShape(Geometry geom) {
    return makeShape(geom, datelineRule != DatelineRule.none, allowMultiOverlap);
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  /**
   * INTERNAL: Returns a Rectangle of the JTS {@link Envelope} (bounding box) of the given {@code geom}.  This asserts
   * that {@link Geometry#isRectangle()} is true.  This method reacts to the {@link DatelineRule} setting.
   * @param geom non-null
   * @return the equivalent Rectangle.
   */
  public Rectangle makeRectFromRectangularPoly(Geometry geom) {
    // TODO although, might want to never convert if there's a semantic difference (e.g.
    //  geodetically)? Should have a setting for that.
    assert geom.isRectangle();
    Envelope env = geom.getEnvelopeInternal();
    boolean crossesDateline = false;
    if (ctx.isGeo() && getDatelineRule() != DatelineRule.none) {
      if (getDatelineRule() == DatelineRule.ccwRect) {
        // If JTS says it is clockwise, then it's actually a dateline crossing rectangle.
        crossesDateline = !CGAlgorithms.isCCW(geom.getCoordinates());
      } else {
        crossesDateline = env.getWidth() > 180;
      }
    }
    if (crossesDateline)
      return rect(env.getMaxX(), env.getMinX(), env.getMinY(), env.getMaxY());
    else
      return rect(env.getMinX(), env.getMaxX(), env.getMinY(), env.getMaxY());
  }

}
