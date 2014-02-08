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
    }
    c.reset(flipX,-1.0*c.getY());
    distance = runDistance(c);
    return distance;
  }

  private double runDistance (Point c) {
    Point3d a3d = new Point3d(a);
    Point3d b3d = new Point3d(b);
    Point3d c3d = new Point3d(c);

    Point3d g = Point3d.crossProductPoint(a3d,b3d);
    Point3d f = Point3d.crossProductPoint(c3d,g);
    Point3d t = Point3d.crossProductPoint(g,f);

    t.normalizePoint();
    t.scalarProductPoint(DistanceUtils.EARTH_EQUATORIAL_RADIUS_KM);

    double lat = Math.asin(t.getZ() / DistanceUtils.EARTH_EQUATORIAL_RADIUS_KM);
    double lon = Math.atan2(t.getY(), t.getX());

    // Radians
    lat = DistanceUtils.toRadians(lat);
    lon = DistanceUtils.toRadians(lon);

    double lonPoint = DistanceUtils.toRadians(c.getX());
    double latPoint = DistanceUtils.toRadians(c.getY());

    // Error in distHaversineRAD
    double distInRAD = DistanceUtils.distHaversineRAD(lat,lon,latPoint,lonPoint);
    double distInKm = DistanceUtils.radians2Dist(distInRAD,DistanceUtils.EARTH_EQUATORIAL_RADIUS_KM);

    double distance = DistanceUtils.dist2Degrees(distInKm,DistanceUtils.EARTH_EQUATORIAL_RADIUS_KM);
    return distance;
  }


}
