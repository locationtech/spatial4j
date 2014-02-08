package com.spatial4j.core.shape.impl;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Point;

/**
 * Created by Chris Pavlicek on 2/7/2014.
 */
public class GreatCircle {


  private Point a;
  private Point b;

  public GreatCircle(Point a, Point b) {
    this.a = a;
    this.b = b;
  }

  public double distanceToPoint(Point c) {
    double distance = runDistance(c);
    double flipX = 0;
    if(distance > 90) {
      flipX = c.getX()-180;

      if(flipX < -180)
        flipX += 360;
      c.reset(flipX,-1.0*c.getY());
      distance = runDistance(c);
    }
    return distance;
  }

  private double runDistance (Point c) {
    Point3d a3d = new Point3d(a);
    Point3d b3d = new Point3d(b);
    Point3d c3d = new Point3d(c);

    Point3d plane = Point3d.crossProductPoint(a3d,b3d);

    double height = dotProd(plane,c3d)/planeRooted(plane);
    return Math.abs(DistanceUtils.toDegrees(Math.asin(height)));
  }

  private double dotProd(Point3d plane, Point3d point) {
    return plane.getX()*point.getX() + plane.getY()*point.getY() + plane.getZ()*point.getZ();
  }

  private double planeRooted(Point3d p) {
    double x2 = p.getX() * p.getX();
    double y2 = p.getY() * p.getY();
    double z2 = p.getZ() * p.getZ();

    return Math.sqrt(x2 + y2 + z2);
  }


}
