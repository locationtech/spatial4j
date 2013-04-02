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

package com.spatial4j.core.io;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;

import java.util.Arrays;

/**
 * Utilities for encoding and decoding geohashes. Based on
 * <a href="http://en.wikipedia.org/wiki/Geohash">http://en.wikipedia.org/wiki/Geohash</a>.
 */
public class GeohashUtils {

  private static final char[] BASE_32 = {'0', '1', '2', '3', '4', '5', '6',
      '7', '8', '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n',
      'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};//note: this is sorted

  private static final int[] BASE_32_IDX;//sparse array of indexes from '0' to 'z'

  public static final int MAX_PRECISION = 24;//DWS: I forget what level results in needless more precision but it's about this
  private static final int[] BITS = {16, 8, 4, 2, 1};

  static {
    BASE_32_IDX = new int[BASE_32[BASE_32.length-1] - BASE_32[0] + 1];
    assert BASE_32_IDX.length < 100;//reasonable length
    Arrays.fill(BASE_32_IDX,-500);
    for (int i = 0; i < BASE_32.length; i++) {
      BASE_32_IDX[BASE_32[i] - BASE_32[0]] = i;
    }
  }

  private GeohashUtils() {
  }

  /**
   * Encodes the given latitude and longitude into a geohash
   * the precision is set to maxPrecision (0) and the default
   * level is set to 12.
   *
   * @param latitude Latitude to encode
   * @param longitude Longitude to encode
   * @return Geohash encoding of the longitude and latitude
   */
  public static String encodeLatLon(double latitude, double longitude) {
    return encodeLatLon(latitude, longitude, 0d, 12);
  }

  /**
   * Encodes the given latitude and longitude into a geohash with the given length.
   * The precision is given by the level. 
   *
   * @param latitude Latitude to encode
   * @param longitude Longitude to encode
   * @param level length of the generated hash code
   * @return Geohash encoding of the longitude and latitude
   */
  public static String encodeLatLon(double latitude, double longitude, int precision) {
    return encodeLatLon(latitude, longitude, 0d, precision);
  }

  /**
   * Encodes the given latitude and longitude to a geohash. The length of the geohash
   * is limited by <code>maxLevels</code> and the <code>precision</code>. If the accuracy
   * of a geohash cell is less than the given <code>precision</code> then the geohash will
   * no longer be adjusted.
   * 
   * @param latitude latitude to encode
   * @param longitude longitude to encode
   * @param precision required precision in fractions of circumference  
   * @param maxlevels maximum length of the generated geohash. If set to 0
   *        {@value GeohashUtils.MAX_PRECISION} will be used
   * @return geohash for the given latitude and longitude
   */
  public static String encodeLatLon(final double latitudeDeg, final double longitudeDeg, final double precision, final int maxlevels) {
    assert precision>=0;

    final int levels = maxlevels<=0 ?MAX_PRECISION :maxlevels;
    final double latitude = DistanceUtils.toRadians(latitudeDeg);
    final double longitude = DistanceUtils.toRadians(longitudeDeg);

    double[] latInterval = {-DistanceUtils.DEG_90_AS_RADS, DistanceUtils.DEG_90_AS_RADS};
    double[] lngInterval = {-DistanceUtils.DEG_180_AS_RADS, DistanceUtils.DEG_180_AS_RADS};
    double size = 1;

    final StringBuilder geohash = new StringBuilder(maxlevels);
    boolean isEven = true;

    int bit = 0;
    int ch = 0;

    /* While cell size is below precision
     *       the geohash is to short
     */
    while ((size > precision && geohash.length()<levels)) {
      double mid;
      if (isEven) {
        mid = (lngInterval[0] + lngInterval[1]) / 2D;
        if (longitude > mid) {
          ch |= BITS[bit];
          lngInterval[0] = mid;
        } else {
          lngInterval[1] = mid;
        }
      } else {
        mid = (latInterval[0] + latInterval[1]) / 2D;
        if (latitude > mid) {
          ch |= BITS[bit];
          latInterval[0] = mid;
        } else {
          latInterval[1] = mid;
        }
      }

      isEven = !isEven;

      if (bit < 4) {
        bit++;
      } else {
        if(precision>0) {
          size = DistanceUtils.distHaversineRAD(latInterval[0], lngInterval[0], latInterval[1], lngInterval[1]);
        }
        geohash.append(BASE_32[ch]);
        bit = 0;
        ch = 0;
      }
    }

    return geohash.toString();
  }

  /**
   * Decodes the given geohash into a latitude and longitude
   *
   * @param geohash Geohash to deocde
   * @return Array with the latitude at index 0, and longitude at index 1
   */
  public static Point decode(String geohash, SpatialContext ctx) {
    Rectangle rect = decodeBoundary(geohash,ctx);
    double latitude = (rect.getMinY() + rect.getMaxY()) / 2D;
    double longitude = (rect.getMinX() + rect.getMaxX()) / 2D;
    return ctx.makePoint(longitude,latitude);
	}

  /** Returns min-max lat, min-max lon. */
  public static Rectangle decodeBoundary(String geohash, SpatialContext ctx) {
    double minY = -90, maxY = 90, minX = -180, maxX = 180;
    boolean isEven = true;

    for (int i = 0; i < geohash.length(); i++) {
      char c = geohash.charAt(i);
      if (c >= 'A' && c <= 'Z')
        c -= ('A' - 'a');
      final int cd = BASE_32_IDX[c - BASE_32[0]];//TODO check successful?

      for (int mask : BITS) {
        if (isEven) {
          if ((cd & mask) != 0) {
            minX = (minX + maxX) / 2D;
          } else {
            maxX = (minX + maxX) / 2D;
          }
        } else {
          if ((cd & mask) != 0) {
            minY = (minY + maxY) / 2D;
          } else {
            maxY = (minY + maxY) / 2D;
          }
        }
        isEven = !isEven;
      }

    }
    return ctx.makeRectangle(minX, maxX, minY, maxY);
  }

  /** Array of geohashes 1 level below the baseGeohash. Sorted. */
  public static String[] getSubGeohashes(String baseGeohash) {
    String[] hashes = new String[BASE_32.length];
    for (int i = 0; i < BASE_32.length; i++) {//note: already sorted
      char c = BASE_32[i];
      hashes[i] = baseGeohash+c;
    }
    return hashes;
  }

  public static double[] lookupDegreesSizeForHashLen(int hashLen) {
    return new double[]{hashLenToLatHeight[hashLen], hashLenToLonWidth[hashLen]};
  }

  /**
   * Return the shortest geohash length that will have a width & height >= specified arguments.
   */
  public static int lookupHashLenForWidthHeight(double lonErr, double latErr) {
    //loop through hash length arrays from beginning till we find one.
    for(int len = 1; len < MAX_PRECISION; len++) {
      double latHeight = hashLenToLatHeight[len];
      double lonWidth = hashLenToLonWidth[len];
      if (latHeight < latErr && lonWidth < lonErr)
        return len;
    }
    return MAX_PRECISION;
  }

  /** See the table at http://en.wikipedia.org/wiki/Geohash */
  private static final double[] hashLenToLatHeight, hashLenToLonWidth;
  static {
    hashLenToLatHeight = new double[MAX_PRECISION +1];
    hashLenToLonWidth = new double[MAX_PRECISION +1];
    hashLenToLatHeight[0] = 90*2;
    hashLenToLonWidth[0] = 180*2;
    boolean even = false;
    for(int i = 1; i <= MAX_PRECISION; i++) {
      hashLenToLatHeight[i] = hashLenToLatHeight[i-1]/(even?8:4);
      hashLenToLonWidth[i] = hashLenToLonWidth[i-1]/(even?4:8);
      even = ! even;
    }
  }

}
