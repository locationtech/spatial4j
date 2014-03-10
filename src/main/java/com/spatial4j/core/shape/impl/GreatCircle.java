package com.spatial4j.core.shape.impl;

import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;

/**
 * Created by Chris Pavlicek on 2/7/2014.
 */
public class GreatCircle {


  private Point a;
  private Point b;

  private Point3d equatorPlane = new Point3d(0,0,1);

  final Point3d a3d;
  final Point3d b3d;

  // TODO: inline vector class? or build another?
  final Point3d planeVector;

  final double invPlaneLength;

  final double angleDEG;

  double highestLongitude;

  public GreatCircle(Point a, Point b) {
    this.a = a;
    this.b = b;

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

    // Store points of the great circle
    a3d = new Point3d(a);
    b3d = new Point3d(b);

    // Vector as Point3d for simplicity.
    planeVector = Point3d.crossProductPoint(a3d,b3d);

    // Inverse of plane length
    invPlaneLength = 1/GreatCircle.vectorLength(planeVector);

    angleDEG = angleInDegCalc();
  }

  public GreatCircle(Point3d a, Point3d b) {
    // Store points of the great circle
    a3d = a;
    b3d = b;

    // Vector as Point3d for simplicity.
    planeVector = Point3d.crossProductPoint(a3d,b3d);

    // Inverse of plane length
    invPlaneLength = 1/GreatCircle.vectorLength(planeVector);
    angleDEG = angleInDegCalc();
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

  public Point3d getA() {
    return a3d;
  }

  public Point3d getB() {
    return b3d;
  }

  public Point getPointA() {
    return a;
  }

  public Point getPointB() {
    return b;
  }

  /** The magnitude of the vector. sqrt(x^2 + y^2 + z^2) */
  private static double vectorLength(Point3d p) {
    double x2 = p.getX() * p.getX();
    double y2 = p.getY() * p.getY();
    double z2 = p.getZ() * p.getZ();

    return Math.sqrt(x2 + y2 + z2);
  }

  public double getAngleInDegree() {
    return angleDEG;
  }

  /**
   * Returns a longitude where the great circle intersects the equator
   * Use this to calculate bounding box.
   * @return double
   */
  public double intersectionLongitude() {
    // Using Point (0,0,0) as the "normal" for the line Cross Product
    // This gives us the equation of a line

    // This is a on the equator.
    Point3d axis = new Point3d(1,0,0);

    Point3d line = Point3d.crossProductPoint(equatorPlane,planeVector);
    double dotProd = dotProduct(axis, line);
    double length = vectorLength(axis) * vectorLength(line);

    // Find the angle from the perfect line.
    double longitude = DistanceUtils.toDegrees(Math.acos(dotProd / length));

    // Adjust for Negative Values
    if((line.getY() < 0 && line.getX() < 0) || line.getY() < 0)
      longitude *= -1;

    return longitude;
  }



  /**
   * Returns the Angle of the GreatCircle
   * Always positive
   * @return double in Degrees
   */
  public double angleInDegCalc() {

    double dotProd = dotProduct(planeVector, equatorPlane);
    double distA = vectorLength(planeVector);
    equatorPlane.normalizePoint();
    double distB = vectorLength(equatorPlane);
    double prodDist = distA * distB;
    double angleInDeg = DistanceUtils.toDegrees(Math.acos(dotProd/prodDist));

    double longitudeIntersection = Math.abs(intersectionLongitude());

    if(longitudeIntersection > 90.0001) {
      longitudeIntersection = -180 + 90 + longitudeIntersection;
    } else {
      longitudeIntersection += 90;
    }

    Point3d p = new Point3d(longitudeIntersection,angleInDeg);

    double dist = distanceToPoint(p);

    if(dist >= 0.00001) {
      highestLongitude -= longitudeIntersection;
      angleInDeg *= -1;
    } else {
      highestLongitude = longitudeIntersection;
    }
    // Angle in Rad, convert to degrees
    return angleInDeg;
  }

}


