package com.spatial4j.core.shape.impl;

import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Point;

/**
 * Created by chris on 2/7/2014.
 */
public class Point3d {

  double x,y,z;

  /**
   * 3d Space Point
   * @param x
   * @param y
   * @param z
   */
  public Point3d(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Returns a Longitude, Latitude Point in a 3d Space
   * @param longitude
   * @param latitude
   */
  public Point3d(double longitude, double latitude) {

    double longitudeRAD = DistanceUtils.toRadians(longitude);
    double latitudeRAD = DistanceUtils.toRadians(latitude);

    this.x = Math.cos(latitudeRAD) * Math.cos(longitudeRAD);
    this.y = Math.cos(latitudeRAD) * Math.sin(longitudeRAD);
    this.z = Math.sin(latitudeRAD);
  }

  /**
   * Returns a New Point3d Object Using Spaitals Point
   * @param a
   */
  public Point3d(Point a) {
    this(a.getX(), a.getY());
  }

  public double getX() {
    return this.x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return this.y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getZ() {
    return this.z;
  }

  public void setZ(double z) {
    this.z = z;
  }

  /**
   * Computes the Cross Product of 2 Points and returns a Point
   * @param a
   * @param b
   * @return
   */
  public static Point3d crossProductPoint(Point3d a, Point3d b) {

    // Cross product of 3 x 1
    double newX = a.getY() * b.getZ() - a.getZ() * b.getY();
    double newY = a.getZ() * b.getX() - b.getZ() * a.getX();
    double newZ = a.getX() * b.getY() - b.getX() * a.getY();

    return new Point3d(newX,newY,newZ);
  }

  /**
   * normalizes a point
   */
  public void normalizePoint() {
    double length = Math.sqrt((getX()*getX()) + (getY()*getY()) + (getZ()*getZ()));
    setX(getX()/length);
    setY(getY()/length);
    setZ(getZ()/length);
    //return new Point3d(getX()/length,getY()/length,getY()/length);
  }

  /**
   * Scalar Product of Point3d and returns a new Point3d
   * @param scalar
   * @return
   */
  public Point3d scalarProductPoint(double scalar) {
    return new Point3d(getX()*scalar,getY()*scalar,getZ()*scalar);
  }
  
  public String toString() {
    return "X: " + this.getX() + "\n" + "Y: " + this.getY() + "\n" + "Z: "  + this.getZ() + "\n";
  }

  /**
   * Returns the Middle 3d point from a set of points.
   * @param a
   * @param b
   * @return
   */
  public static Point3d midPoint(Point3d a, Point3d b) {
    double x = (a.getX() + b.getX())/2;
    double y = (a.getY() + b.getY())/2;
    double z = (a.getZ() + b.getZ())/2;

    return new Point3d(x,y,z);
  }

  /**
   * Returns a Vector (A - B) (Directional)
   * @param a
   * @param b
   * @return AB Vector
   */
  public static Point3d vector(Point3d a, Point3d b) {
    return new Point3d(a.getX()-b.getX(),a.getY()-b.getY(),a.getZ()-b.getZ());
  }

}
