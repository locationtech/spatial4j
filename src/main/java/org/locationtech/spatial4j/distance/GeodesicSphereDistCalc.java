/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.distance;

import org.locationtech.spatial4j.*;


import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.Circle;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Rectangle;

import java.io.FileWriter;
import java.io.IOException;

import static org.locationtech.spatial4j.distance.DistanceUtils.toDegrees;
import static org.locationtech.spatial4j.distance.DistanceUtils.toRadians;


/**
 * A base class for a Distance Calculator that assumes a spherical earth model.
 */
public abstract class GeodesicSphereDistCalc extends AbstractDistanceCalculator {

  private static final double radiusDEG = DistanceUtils.toDegrees(1);//in degrees



    public static boolean[] flags = new boolean[3];
    private static void writeToFile(){
        try
        {
            String filename= "pointOnBearing.txt";
            FileWriter fw = new FileWriter(filename,false); //the true will append the new data
            fw.write("pointOnBearing \n");
            int count = 0;
            for (boolean b :flags) {
                if (b) count ++;
                fw.write(b + " ");
            }
            fw.write("\nCoverage: " + (Double.toString((double) count/flags.length)) );
            fw.close();
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

  @Override
  public Point pointOnBearing(Point from, double distDEG, double bearingDEG, SpatialContext ctx, Point reuse) {
      //setup
      //teardown

    if (distDEG == 0) {

      if (reuse == null) {
        flags[0] = true;
          writeToFile();
        return from;
      }
      flags[1] = true;

      reuse.reset(from.getX(), from.getY());
        writeToFile();
      return reuse;
    }

    Point result = DistanceUtils.pointOnBearingRAD(
        toRadians(from.getY()), toRadians(from.getX()),
        toRadians(distDEG),
        toRadians(bearingDEG), ctx, reuse);//output result is in radians
    result.reset(toDegrees(result.getX()), toDegrees(result.getY()));
      flags[2] = true;
      writeToFile();
    return result;

  }


  @Override
  public Rectangle calcBoxByDistFromPt(Point from, double distDEG, SpatialContext ctx, Rectangle reuse) {
    return DistanceUtils.calcBoxByDistFromPtDEG(from.getY(), from.getX(), distDEG, ctx, reuse);
  }

  @Override
  public double calcBoxByDistFromPt_yHorizAxisDEG(Point from, double distDEG, SpatialContext ctx) {
    return DistanceUtils.calcBoxByDistFromPt_latHorizAxisDEG(from.getY(), from.getX(), distDEG);
  }

  @Override
  public double area(Rectangle rect) {
    //From http://mathforum.org/library/drmath/view/63767.html
    double lat1 = toRadians(rect.getMinY());
    double lat2 = toRadians(rect.getMaxY());
    return Math.PI / 180 * radiusDEG * radiusDEG *
            Math.abs(Math.sin(lat1) - Math.sin(lat2)) *
            rect.getWidth();
  }

  @Override
  public double area(Circle circle) {
    //formula is a simplified case of area(rect).
    double lat = toRadians(90 - circle.getRadius());
    return 2 * Math.PI * radiusDEG * radiusDEG * (1 - Math.sin(lat));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    return getClass().equals(obj.getClass());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public final double distance(Point from, double toX, double toY) {
    return toDegrees(distanceLatLonRAD(toRadians(from.getY()), toRadians(from.getX()), toRadians(toY), toRadians(toX)));
  }

  protected abstract double distanceLatLonRAD(double lat1, double lon1, double lat2, double lon2);

  public static class Haversine extends GeodesicSphereDistCalc {

    @Override
    protected double distanceLatLonRAD(double lat1, double lon1, double lat2, double lon2) {
      return DistanceUtils.distHaversineRAD(lat1,lon1,lat2,lon2);
    }

  }

  public static class LawOfCosines extends GeodesicSphereDistCalc {

    @Override
    protected double distanceLatLonRAD(double lat1, double lon1, double lat2, double lon2) {
      return DistanceUtils.distLawOfCosinesRAD(lat1, lon1, lat2, lon2);
    }

  }

  public static class Vincenty extends GeodesicSphereDistCalc {

    @Override
    protected double distanceLatLonRAD(double lat1, double lon1, double lat2, double lon2) {
      return DistanceUtils.distVincentyRAD(lat1, lon1, lat2, lon2);
    }
  }
}
