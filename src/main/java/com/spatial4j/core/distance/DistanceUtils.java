/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.distance;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;


/**
 * Various distance calculations and constants.
 * Originally from Lucene 3x's old spatial module. It has been modified here.
 */
public class DistanceUtils {

  //pre-compute some angles that are commonly used
  public static final double DEG_45_AS_RADS = Math.PI / 4;
  public static final double SIN_45_AS_RADS = Math.sin(DEG_45_AS_RADS);
  public static final double DEG_90_AS_RADS = Math.PI / 2;
  public static final double DEG_180_AS_RADS = Math.PI;
  public static final double DEG_225_AS_RADS = 5 * DEG_45_AS_RADS;
  public static final double DEG_270_AS_RADS = 3 * DEG_90_AS_RADS;

  public static final double DEGREES_TO_RADIANS =  Math.PI / 180;
  public static final double RADIANS_TO_DEGREES =  1 / DEGREES_TO_RADIANS;

  public static final double KM_TO_MILES = 0.621371192;
  public static final double MILES_TO_KM = 1 / KM_TO_MILES;//1.609

  /**
   * The International Union of Geodesy and Geophysics says the Earth's mean radius in KM is:
   *
   * [1] http://en.wikipedia.org/wiki/Earth_radius
   */
  public static final double EARTH_MEAN_RADIUS_KM = 6371.0087714;
  public static final double EARTH_EQUATORIAL_RADIUS_KM = 6378.1370;

  /** Equivalent to degrees2Dist(1, EARTH_MEAN_RADIUS_KM) */
  public static final double DEG_TO_KM = DEGREES_TO_RADIANS * EARTH_MEAN_RADIUS_KM;
  public static final double KM_TO_DEG = 1 / DEG_TO_KM;

  public static final double EARTH_MEAN_RADIUS_MI = EARTH_MEAN_RADIUS_KM * KM_TO_MILES;
  public static final double EARTH_EQUATORIAL_RADIUS_MI = EARTH_EQUATORIAL_RADIUS_KM * KM_TO_MILES;

  private DistanceUtils() {}

  /**
   * Calculate the p-norm (i.e. length) between two vectors
   *
   * @param vec1  The first vector
   * @param vec2  The second vector
   * @param power The power (2 for cartesian distance, 1 for manhattan, etc.)
   * @return The length.
   *         <p/>
   *         See http://en.wikipedia.org/wiki/Lp_space
   * @see #vectorDistance(double[], double[], double, double)
   */
  public static double vectorDistance(double[] vec1, double[] vec2, double power) {
    return vectorDistance(vec1, vec2, power, 1.0 / power);
  }

  /**
   * Calculate the p-norm (i.e. length) between two vectors
   *
   * @param vec1         The first vector
   * @param vec2         The second vector
   * @param power        The power (2 for cartesian distance, 1 for manhattan, etc.)
   * @param oneOverPower If you've precalculated oneOverPower and cached it, use this method to save one division operation over {@link #vectorDistance(double[], double[], double)}.
   * @return The length.
   */
  public static double vectorDistance(double[] vec1, double[] vec2, double power, double oneOverPower) {
    double result = 0;

    if (power == 0) {
      for (int i = 0; i < vec1.length; i++) {
        result += vec1[i] - vec2[i] == 0 ? 0 : 1;
      }

    } else if (power == 1.0) {
      for (int i = 0; i < vec1.length; i++) {
        result += vec1[i] - vec2[i];
      }
    } else if (power == 2.0) {
      result = Math.sqrt(distSquaredCartesian(vec1, vec2));
    } else if (power == Integer.MAX_VALUE || Double.isInfinite(power)) {//infinite norm?
      for (int i = 0; i < vec1.length; i++) {
        result = Math.max(result, Math.max(vec1[i], vec2[i]));
      }
    } else {
      for (int i = 0; i < vec1.length; i++) {
        result += Math.pow(vec1[i] - vec2[i], power);
      }
      result = Math.pow(result, oneOverPower);
    }
    return result;
  }

  /**
   * Return the coordinates of a vector that is the corner of a box (upper right or lower left), assuming a Rectangular
   * coordinate system.  Note, this does not apply for points on a sphere or ellipse (although it could be used as an approximation).
   *
   * @param center     The center point
   * @param result Holds the result, potentially resizing if needed.
   * @param distance   The d from the center to the corner
   * @param upperRight If true, return the coords for the upper right corner, else return the lower left.
   * @return The point, either the upperLeft or the lower right
   */
  public static double[] vectorBoxCorner(double[] center, double[] result, double distance, boolean upperRight) {
    if (result == null || result.length != center.length) {
      result = new double[center.length];
    }
    if (upperRight == false) {
      distance = -distance;
    }
    //We don't care about the power here,
    // b/c we are always in a rectangular coordinate system, so any norm can be used by
    //using the definition of sine
    distance = SIN_45_AS_RADS * distance; // sin(Pi/4) == (2^0.5)/2 == opp/hyp == opp/distance, solve for opp, similarly for cosine
    for (int i = 0; i < center.length; i++) {
      result[i] = center[i] + distance;
    }
    return result;
  }

  /**
   * Given a start point (startLat, startLon) and a bearing on a sphere, return the destination point.
   *
   * @param startLat The starting point latitude, in radians
   * @param startLon The starting point longitude, in radians
   * @param distanceRAD The distance to travel along the bearing in radians.
   * @param bearingRAD The bearing, in radians.  North is a 0, moving clockwise till radians(360).
   * @param ctx
   * @param reuse A preallocated object to hold the results.
   * @return The destination point, IN RADIANS.
   */
  public static Point pointOnBearingRAD(double startLat, double startLon, double distanceRAD, double bearingRAD, SpatialContext ctx, Point reuse) {
    /*
 	  lat2 = asin(sin(lat1)*cos(d/R) + cos(lat1)*sin(d/R)*cos(θ))
  	lon2 = lon1 + atan2(sin(θ)*sin(d/R)*cos(lat1), cos(d/R)−sin(lat1)*sin(lat2))
     */
    double cosAngDist = Math.cos(distanceRAD);
    double cosStartLat = Math.cos(startLat);
    double sinAngDist = Math.sin(distanceRAD);
    double sinStartLat = Math.sin(startLat);
    double lat2 = Math.asin(sinStartLat * cosAngDist +
            cosStartLat * sinAngDist * Math.cos(bearingRAD));
    double lon2 = startLon + Math.atan2(Math.sin(bearingRAD) * sinAngDist * cosStartLat,
            cosAngDist - sinStartLat * Math.sin(lat2));
    
    // normalize lon first
    if (lon2 > DEG_180_AS_RADS) {
      lon2 = -1.0 * (DEG_180_AS_RADS - (lon2 - DEG_180_AS_RADS));
    } else if (lon2 < -DEG_180_AS_RADS) {
      lon2 = (lon2 + DEG_180_AS_RADS) + DEG_180_AS_RADS;
    }

    // normalize lat - could flip poles
    if (lat2 > DEG_90_AS_RADS) {
      lat2 = DEG_90_AS_RADS - (lat2 - DEG_90_AS_RADS);
      if (lon2 < 0) {
        lon2 = lon2 + DEG_180_AS_RADS;
      } else {
        lon2 = lon2 - DEG_180_AS_RADS;
      }
    } else if (lat2 < -DEG_90_AS_RADS) {
      lat2 = -DEG_90_AS_RADS - (lat2 + DEG_90_AS_RADS);
      if (lon2 < 0) {
        lon2 = lon2 + DEG_180_AS_RADS;
      } else {
        lon2 = lon2 - DEG_180_AS_RADS;
      }
    }

    if (reuse == null) {
      return ctx.makePoint(lon2, lat2);
    } else {
      reuse.reset(lon2, lat2);//x y
      return reuse;
    }
  }

  /**
   * Puts in range -180 <= lon_deg <= +180.
   */
  public static double normLonDEG(double lon_deg) {
    if (lon_deg >= -180 && lon_deg <= 180)
      return lon_deg;//common case, and avoids slight double precision shifting
    double off = (lon_deg + 180) % 360;
    if (off < 0)
      return 180 + off;
    else if (off == 0 && lon_deg > 0)
      return 180;
    else
      return -180 + off;
  }

  /**
   * Puts in range -90 <= lat_deg <= 90.
   */
  public static double normLatDEG(double lat_deg) {
    if (lat_deg >= -90 && lat_deg <= 90)
      return lat_deg;//common case, and avoids slight double precision shifting
    double off = Math.abs((lat_deg + 90) % 360);
    return (off <= 180 ? off : 360-off) - 90;
  }

  /**
   * Calculates the bounding box of a circle, as specified by its center point
   * and distance.  <code>reuse</code> is an optional argument to store the
   * results to avoid object creation.
   */
  public static Rectangle calcBoxByDistFromPtDEG(double lat, double lon, double distDEG, SpatialContext ctx, Rectangle reuse) {
    //See http://janmatuschek.de/LatitudeLongitudeBoundingCoordinates Section 3.1, 3.2 and 3.3
    double minX; double maxX; double minY; double maxY;
    if (distDEG == 0) {
      minX = lon; maxX = lon; minY = lat; maxY = lat;
    } else if (distDEG >= 180) {//distance is >= opposite side of the globe
      minX = -180; maxX = 180; minY = -90; maxY = 90;
    } else {

      //--calc latitude bounds
      maxY = lat + distDEG;
      minY = lat - distDEG;

      if (maxY >= 90 || minY <= -90) {//touches either pole
        //we have special logic for longitude
        minX = -180; maxX = 180;//world wrap: 360 deg
        if (maxY <= 90 && minY >= -90) {//doesn't pass either pole: 180 deg
          minX = normLonDEG(lon - 90);
          maxX = normLonDEG(lon + 90);
        }
        if (maxY > 90)
          maxY = 90;
        if (minY < -90)
          minY = -90;
      } else {
        //--calc longitude bounds
        double lon_delta_deg = calcBoxByDistFromPt_deltaLonDEG(lat, lon, distDEG);

        minX = normLonDEG(lon - lon_delta_deg);
        maxX = normLonDEG(lon + lon_delta_deg);
      }
    }
    if (reuse == null) {
      return ctx.makeRectangle(minX, maxX, minY, maxY);
    } else {
      reuse.reset(minX, maxX, minY, maxY);
      return reuse;
    }
  }

  /**
   * The delta longitude of a point-distance. In other words, half the width of
   * the bounding box of a circle.
   */
  public static double calcBoxByDistFromPt_deltaLonDEG(double lat, double lon, double distDEG) {
    //http://gis.stackexchange.com/questions/19221/find-tangent-point-on-circle-furthest-east-or-west
    if (distDEG == 0)
      return 0;
    double lat_rad = toRadians(lat);
    double dist_rad = toRadians(distDEG);
    double result_rad = Math.asin(Math.sin(dist_rad) / Math.cos(lat_rad));

    if (!Double.isNaN(result_rad))
      return toDegrees(result_rad);
    return 90;
  }

  /**
   * The latitude of the horizontal axis (e.g. left-right line)
   * of a circle.  The horizontal axis of a circle passes through its furthest
   * left-most and right-most edges. On a 2D plane, this result is always
   * <code>from.getY()</code> but, perhaps surprisingly, on a sphere it is going
   * to be slightly different.
   */
  public static double calcBoxByDistFromPt_latHorizAxisDEG(double lat, double lon, double distDEG) {
    //http://gis.stackexchange.com/questions/19221/find-tangent-point-on-circle-furthest-east-or-west
    if (distDEG == 0)
      return lat;
    double lat_rad = toRadians(lat);
    double dist_rad = toRadians(distDEG);
    double result_rad = Math.asin( Math.sin(lat_rad) / Math.cos(dist_rad));
    if (!Double.isNaN(result_rad))
      return toDegrees(result_rad);
    if (lat > 0)
      return 90;
    if (lat < 0)
      return -90;
    return lat;
  }

  /**
   * The square of the cartesian Distance.  Not really a distance, but useful if all that matters is
   * comparing the result to another one.
   *
   * @param vec1 The first point
   * @param vec2 The second point
   * @return The squared cartesian distance
   */
  public static double distSquaredCartesian(double[] vec1, double[] vec2) {
    double result = 0;
    for (int i = 0; i < vec1.length; i++) {
      double v = vec1[i] - vec2[i];
      result += v * v;
    }
    return result;
  }

  /**
   *
   * @param lat1     The y coordinate of the first point, in radians
   * @param lon1     The x coordinate of the first point, in radians
   * @param lat2     The y coordinate of the second point, in radians
   * @param lon2     The x coordinate of the second point, in radians
   * @return The distance between the two points, as determined by the Haversine formula, in radians.
   */
  public static double distHaversineRAD(double lat1, double lon1, double lat2, double lon2) {
    //TODO investigate slightly different formula using asin() and min() http://www.movable-type.co.uk/scripts/gis-faq-5.1.html

    // Check for same position
    if (lat1 == lat2 && lon1 == lon2)
      return 0.0;
    double hsinX = Math.sin((lon1 - lon2) * 0.5);
    double hsinY = Math.sin((lat1 - lat2) * 0.5);
    double h = hsinY * hsinY +
            (Math.cos(lat1) * Math.cos(lat2) * hsinX * hsinX);
    return 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
  }

  /**
   * Calculates the distance between two lat-lon's using the Law of Cosines. Due to numeric conditioning
   * errors, it is not as accurate as the Haversine formula for small distances.  But with
   * double precision, it isn't that bad -- <a href="http://www.movable-type.co.uk/scripts/latlong.html">
   *   allegedly 1 meter</a>.
   * <p/>
   * See <a href="http://gis.stackexchange.com/questions/4906/why-is-law-of-cosines-more-preferable-than-haversine-when-calculating-distance-b">
   *  Why is law of cosines more preferable than haversine when calculating distance between two latitude-longitude points?</a>
   * <p/>
   * The arguments and return value are in radians.
   */
  public static double distLawOfCosinesRAD(double lat1, double lon1, double lat2, double lon2) {
    //TODO validate formula

    //(MIGRATED FROM org.apache.lucene.spatial.geometry.LatLng.arcDistance()) (Lucene 3x)
    // Imported from mq java client.  Variable references changed to match.

    // Check for same position
    if (lat1 == lat2 && lon1 == lon2)
      return 0.0;

    // Get the m_dLongitude difference. Don't need to worry about
    // crossing 180 since cos(x) = cos(-x)
    double dLon = lon2 - lon1;

    double a = DEG_90_AS_RADS - lat1;
    double c = DEG_90_AS_RADS - lat2;
    double cosB = (Math.cos(a) * Math.cos(c))
        + (Math.sin(a) * Math.sin(c) * Math.cos(dLon));

    // Find angle subtended (with some bounds checking) in radians
    if (cosB < -1.0)
      return Math.PI;
    else if (cosB >= 1.0)
      return 0;
    else
      return Math.acos(cosB);
  }

  /**
   * Calculates the great circle distance using the Vincenty Formula, simplified for a spherical model. This formula
   * is accurate for any pair of points. The equation
   * was taken from <a href="http://en.wikipedia.org/wiki/Great-circle_distance">Wikipedia</a>.
   * <p/>
   * The arguments are in radians, and the result is in radians.
   */
  public static double distVincentyRAD(double lat1, double lon1, double lat2, double lon2) {
    // Check for same position
    if (lat1 == lat2 && lon1 == lon2)
      return 0.0;

    double cosLat1 = Math.cos(lat1);
    double cosLat2 = Math.cos(lat2);
    double sinLat1 = Math.sin(lat1);
    double sinLat2 = Math.sin(lat2);
    double dLon = lon2 - lon1;
    double cosDLon = Math.cos(dLon);
    double sinDLon = Math.sin(dLon);

    double a = cosLat2 * sinDLon;
    double b = cosLat1*sinLat2 - sinLat1*cosLat2*cosDLon;
    double c = sinLat1*sinLat2 + cosLat1*cosLat2*cosDLon;
    
    return Math.atan2(Math.sqrt(a*a+b*b),c);
  }

  /**
   * Converts a distance in the units of the radius to degrees (360 degrees are
   * in a circle). A spherical earth model is assumed.
   */
  public static double dist2Degrees(double dist, double radius) {
    return toDegrees(dist2Radians(dist, radius));
  }

  /**
   * Converts <code>degrees</code> (1/360th of circumference of a circle) into a
   * distance as measured by the units of the radius.  A spherical earth model
   * is assumed.
   */
  public static double degrees2Dist(double degrees, double radius) {
    return radians2Dist(toRadians(degrees), radius);
  }

  /**
   * Converts a distance in the units of <code>radius</code> (e.g. kilometers)
   * to radians (multiples of the radius). A spherical earth model is assumed.
   */
  public static double dist2Radians(double dist, double radius) {
    return dist / radius;
  }

  /**
   * Converts <code>radians</code> (multiples of the <code>radius</code>) to
   * distance in the units of the radius (e.g. kilometers).
   */
  public static double radians2Dist(double radians, double radius) {
    return radians * radius;
  }

  /**
   * Same as {@link Math#toRadians(double)} but 3x faster (multiply vs. divide).
   * See CompareRadiansSnippet.java in tests.
   */
  public static double toRadians(double degrees) {
    return degrees * DEGREES_TO_RADIANS;
  }

  /**
   * Same as {@link Math#toDegrees(double)} but 3x faster (multiply vs. divide).
   * See CompareRadiansSnippet.java in tests.
   */
  public static double toDegrees(double radians) {
    return radians * RADIANS_TO_DEGREES;
  }

}
