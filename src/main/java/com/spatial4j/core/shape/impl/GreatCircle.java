package com.spatial4j.core.shape.impl;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.SpatialRelation;

import java.util.ArrayList;

import static com.spatial4j.core.shape.SpatialRelation.CONTAINS;
import static com.spatial4j.core.shape.SpatialRelation.DISJOINT;
import static com.spatial4j.core.shape.SpatialRelation.INTERSECTS;

/**
 * Created by Chris Pavlicek on 2/7/2014.
 */
public class GreatCircle {

  // TODO: Remove?
  final static double precision = 0.000001;
  private static Point3d equatorPlane = new Point3d(0,0,1);

  // Fundamental
  double highestLongitude;
  double lowestLongitude;
  final double lonAtEquator;
  final double angleDEG;

  // TODO: inline vector class? or build another?
  final Point3d planeVector;

  final double invPlaneLength;

  public GreatCircle(Point3d a, Point3d b) {
    // Store points of the great circle
    Point3d a3d = a;
    Point3d b3d = b;

    // Vector as Point3d for simplicity.
    planeVector = Point3d.crossProductPoint(a3d,b3d);

    // Inverse of plane length
    invPlaneLength = 1/GreatCircle.vectorLength(planeVector);
    // Using Point (0,0,0) as the "normal" for the line Cross Product
    // This gives us the equation of a line

    // This is a on the equator.
    Point3d axis = new Point3d(1,0,0);


    // START LON OF INTERSECTION
    // Calculate Lon of Intersection
    Point3d line = Point3d.crossProductPoint(equatorPlane,planeVector);
    double dotProductAxisLine = dotProduct(axis, line);
    double length = vectorLength(axis) * vectorLength(line);

    // Find the angle from the perfect line.
    double longitude = DistanceUtils.toDegrees(Math.acos(dotProductAxisLine / length));

    Point3d intersection = new Point3d(longitude, 0);


    if(distanceToPoint(intersection) >= precision){
      lonAtEquator = longitude * -1;
    } else {
      lonAtEquator = longitude;
    }

    // END LON OF INTERSECTION

    // START ANGLE OF INTERSECTION
    double dotProd = dotProduct(planeVector, equatorPlane);
    double distA = vectorLength(planeVector);
    equatorPlane.normalizePoint();
    double distB = vectorLength(equatorPlane);
    double prodDist = distA * distB;
    double angleDEG = DistanceUtils.toDegrees(Math.acos(dotProd/prodDist));

    if(angleDEG > 90)
        angleDEG = 180 - angleDEG;

    if(angleDEG < -90)
        angleDEG = -180 - angleDEG;

    double longitudeIntersection = lonAtEquator;

    // TODO: Don't use precision
    if(longitudeIntersection > (90 + precision)) {
      longitudeIntersection = longitudeIntersection - 90;
    } else {
      longitudeIntersection += 90;
    }

    Point3d p = new Point3d(longitudeIntersection, angleDEG);

    double dist = distanceToPoint(p);

    // TODO: Don't use precision
    if(dist >= precision) {
      highestLongitude = longitudeIntersection + 180;
    } else {
      highestLongitude = longitudeIntersection;
    }

    if(highestLongitude >  180)
       highestLongitude = (360 - highestLongitude) * -1;


    double lowest = highestLongitude + 180;
    if(lowest >  180) {
      lowestLongitude = (360 - lowest) * -1;
    } else {
      lowestLongitude = lowest;
    }

    // Angle in Rad, convert to degrees
    this.angleDEG = angleDEG;
    // END ANGLE INTERSECTION

  }

  //TODO ? Use an Enum for quadrant?

  /* quadrants 1-4: NE, NW, SW, SE. */
  private static final int[] oppositeQuad= {-1,3,4,1,2};

  public static void cornerByQuadrant(Rectangle r, int cornerQuad, Point out) {
    double x = (cornerQuad == 1 || cornerQuad == 4) ? r.getMaxX() : r.getMinX();
    double y = (cornerQuad == 1 || cornerQuad == 2) ? r.getMaxY() : r.getMinY();
    out.reset(x, y);
  }

  /**
   * Returns the distance to the GreatCircle from the Point3d c.
   * Also known as the cross-track distance.
   * See Ref: http://mathworld.wolfram.com/Point-PlaneDistance.html
   * @param c
   * @return
   */
  public double distanceToPoint(Point3d c3d) {
    double height = GreatCircle.dotProduct(planeVector, c3d)*invPlaneLength;

    // opposite/hyp = Sin theta -> use asin of Height/1 (radians)
    // Gives radians of arc length.
    return Math.abs(DistanceUtils.toDegrees(Math.asin(height)));
  }

  /**
   * Returns the distance to the GreatCircle from the Point c.
   * Also known as the cross-track distance.
   * See Ref: http://mathworld.wolfram.com/Point-PlaneDistance.html
   * @param c
   * @return
   */
  public double distanceToPoint(Point c) {
    Point3d c3d = new Point3d(c);
    double height = GreatCircle.dotProduct(planeVector, c3d)*invPlaneLength;

    // opposite/hyp = Sin theta -> use asin of Height/1 (radians)
    // Gives radians of arc length.
    return Math.abs(DistanceUtils.toDegrees(Math.asin(height)));
  }

  /** the dot product of a vector and a point. (plane.x * point.x + plane.y * point.y + plane.z * point.z) */
  private static double dotProduct(Point3d vectorPlane, Point3d point) {
    return vectorPlane.getX()*point.getX() + vectorPlane.getY()*point.getY() + vectorPlane.getZ()*point.getZ();
  }

  /** The magnitude of the vector. sqrt(x^2 + y^2 + z^2) */
  private static double vectorLength(Point3d p) {
    double x2 = p.getX() * p.getX();
    double y2 = p.getY() * p.getY();
    double z2 = p.getZ() * p.getZ();

    return Math.sqrt(x2 + y2 + z2);
  }

  public double getAngleDEG() {
    return angleDEG;
  }


  public double getLonAtEquator() {
    return lonAtEquator;
  }

  public Point highestPoint(SpatialContext ctx) {
    return ctx.makePoint(highestLongitude,angleDEG);
  }


  public Point lowestPoint(SpatialContext ctx) {
    return ctx.makePoint(lowestLongitude,-1*angleDEG);
  }

  /**
   * Returns a ArrayList<Point3d>, of the normalized points for a great circle.
   * Object at index 0 represents point a, and index 1 represents point b.
   *
   * Example code:
   *
   * Point3d[] points = GreatCircle.get3dPointsForGreatCircle(a,b);
   * Point3d a3d = points[0];
   * Point3d b3d = points[1];
   * @param a
   * @param b
   * @return ArrayList<Point3d>
   */
  protected static Point3d[] get3dPointsForGreatCircle(Point a, Point b) {
    if(b.getY() == (-1*a.getY())) {

      double xA = a.getX() - 180;
      if(xA < -180) {
        xA += 360;
      } else if(xA == 180){
        xA = -180;
      }

      double xB = b.getX();

      if(xB == 180) {
        xB = -180;
      }

      if(xA == xB || (a.getX() == b.getX() && a.getY() == b.getY())) {
        throw new InvalidShapeException("Antipodal points ambiguous great circle");
      }
    }

    Point3d[] list = {new Point3d(a),new Point3d(b)};
    return list;
  }




  /*
   * Returns two rectangles. Index 0 will be the "Left" side and Index 1 will be the "Right" side.
   */
  private double[] originalRect = {0,0,0,0};
  private boolean relateRectIsSplit = false;
  private void splitRectange(Rectangle r, double longitude, boolean right) {

    if(right) {
      r.reset(longitude,originalRect[1],originalRect[2],originalRect[3]);
    } else {
      // Left
      r.reset(originalRect[0],longitude,originalRect[2],originalRect[3]);
    }
  }



  public SpatialRelation relate(Rectangle r, Point prC, Point scratch, double buf) {
    assert r.getCenter().equals(prC);

    // r spans over a moment.
    double splitPoint = 0;
    if((r.getMinX() < lowestLongitude && r.getMaxX() > lowestLongitude)) {
      relateRectIsSplit = true;
      setOriginalRect(r);
      splitRectange(r,lowestLongitude,false);
      splitPoint = lowestLongitude;
    } else if (r.getMinX() < highestLongitude && r.getMaxX() > highestLongitude) {
      relateRectIsSplit = true;
      setOriginalRect(r);
      splitRectange(r,highestLongitude,false);
      splitPoint = highestLongitude;
    }

    if(relateRectIsSplit) {
      // Relate the Left side
      SpatialRelation relationOne = getSpatialRelation(r, prC, scratch, buf);

      // Relate the Right side
      splitRectange(r,splitPoint,true);
      SpatialRelation relationTwo = getSpatialRelation(r, prC, scratch, buf);

      // Combine and return
      r.reset(originalRect[0],originalRect[1],originalRect[2],originalRect[3]);
      return relationOne.combine(relationTwo);
    } else {
      // Otherwise this will do.
      return getSpatialRelation(r, prC, scratch, buf);
    }
  }

  private SpatialRelation getSpatialRelation(Rectangle r, Point prC, Point scratch, double buf) {
    int cQuad = quadrant(prC);
    Point nearestP = scratch;
    cornerByQuadrant(r, oppositeQuad[cQuad], nearestP);
    boolean nearestContains = contains(nearestP, buf);

    if (nearestContains) {
      Point farthestP = scratch;
      nearestP = null;//just to be safe (same scratch object)
      cornerByQuadrant(r, cQuad, farthestP);
      boolean farthestContains = contains(farthestP, buf);
      if (farthestContains)
        return CONTAINS;
      return INTERSECTS;
    } else {// not nearestContains
      if (quadrant(nearestP) == cQuad)
        return DISJOINT;//out of buffer on same side as center
      return INTERSECTS;//nearest & farthest points straddle the line
    }
  }

  private void setOriginalRect(Rectangle r) {
    originalRect[0] = r.getMinX();
    originalRect[1] = r.getMaxX();
    originalRect[2] = r.getMinY();
    originalRect[3] = r.getMaxY();
  }

  public boolean contains(Point p,double buf) {
    return (distanceToPoint(p) <= buf);
  }

  public int quadrant(Point c) {
    //check vertical line case 1st
    double intercept = lonAtEquator;
    double angle = angleDEG;
    if (angle == 90) {
      //when slope is infinite, intercept is x intercept instead of y
      if(lowestLongitude < c.getX() || c.getX() > highestLongitude) {
        angle *= -1;
        intercept += 180;

        if(intercept > 180)
          intercept -= 360;
      }
      return c.getX() > lonAtEquator ? 1 : 2; //4 : 3 would work too
    }
    //(below will work for slope==0 horizontal line too)
    //is c above or below the line

    // Positive slope
    if(lowestLongitude < c.getX() || c.getX() > highestLongitude) {
        angle *= -1;
        intercept += 180;

        if(intercept > 180)
            intercept -= 360;
    }

    double yAtCinLine = Math.tan(angle) * c.getX() + lonAtEquator;
    boolean above = c.getY() >= yAtCinLine;
    if (angle > 0) {
      //if slope is a forward slash, then result is 2 | 4
      return above ? 2 : 4;
    } else {
      //if slope is a backward slash, then result is 1 | 3
      return above ? 1 : 3;
    }
  }
}


