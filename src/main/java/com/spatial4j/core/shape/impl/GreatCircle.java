package com.spatial4j.core.shape.impl;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Point;

import java.util.ArrayList;

/**
 * Created by Chris Pavlicek on 2/7/2014.
 */
public class GreatCircle {

  // TODO: Remove?
  final static double precision = 0.000001;
  private static Point3d equatorPlane = new Point3d(0,0,1);

  // Fundamental
  double highestLongitude;
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

    lonAtEquator = Math.abs(longitude);
    // END LON OF INTERSECTION

    // START ANGLE OF INTERSECTION
    double dotProd = dotProduct(planeVector, equatorPlane);
    double distA = vectorLength(planeVector);
    equatorPlane.normalizePoint();
    double distB = vectorLength(equatorPlane);
    double prodDist = distA * distB;
    double angleDEG = DistanceUtils.toDegrees(Math.acos(dotProd/prodDist));

    double longitudeIntersection = lonAtEquator;

    // TODO: Don't use precision
    if(longitudeIntersection > (90 + precision)) {
      longitudeIntersection = -180 + 90 + longitudeIntersection;
    } else {
      longitudeIntersection += 90;
    }

    Point3d p = new Point3d(longitudeIntersection, angleDEG);

    double dist = distanceToPoint(p);

    // TODO: Don't use precision
    if(dist >= precision) {
      highestLongitude -= longitudeIntersection;
      angleDEG *= -1;
    } else {
      highestLongitude = longitudeIntersection;
    }
    // Angle in Rad, convert to degrees
    this.angleDEG = angleDEG;
    // END ANGLE INTERSECTION

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
    return ctx.makePoint(-1.0*highestLongitude,angleDEG);
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

}


