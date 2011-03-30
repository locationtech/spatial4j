/**
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

package org.apache.lucene.spatial.strategy.geohash;

import org.apache.lucene.spatial.base.distance.DistanceUnits;
import org.apache.lucene.spatial.base.shape.ShapeIO;
import org.apache.lucene.spatial.base.shape.simple.SimpleShapeIO;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.Test;

/**
 * Tests for {@link GeoHashUtils}
 */
public class TestGeoHashUtils extends LuceneTestCase {
  ShapeIO shapeIO = new SimpleShapeIO( DistanceUnits.KILOMETERS );

  /**
   * Pass condition: lat=42.6, lng=-5.6 should be encoded as "ezs42e44yx96",
   * lat=57.64911 lng=10.40744 should be encoded as "u4pruydqqvj8"
   */
  @Test
  public void testEncode() {
    String hash = GeoHashUtils.encode(42.6, -5.6);
    assertEquals("ezs42e44yx96", hash);

    hash = GeoHashUtils.encode(57.64911, 10.40744);
    assertEquals("u4pruydqqvj8", hash);
  }

  /**
   * Pass condition: lat=52.3738007, lng=4.8909347 should be encoded and then
   * decoded within 0.00001 of the original value
   */
  @Test
  public void testDecodePreciseLongitudeLatitude() {
    String hash = GeoHashUtils.encode(52.3738007, 4.8909347);

    double[] latitudeLongitude = GeoHashUtils.decode(hash,shapeIO);

    assertEquals(52.3738007, latitudeLongitude[0], 0.00001D);
    assertEquals(4.8909347, latitudeLongitude[1], 0.00001D);
  }

  /**
   * Pass condition: lat=84.6, lng=10.5 should be encoded and then decoded
   * within 0.00001 of the original value
   */
  @Test
  public void testDecodeImpreciseLongitudeLatitude() {
    String hash = GeoHashUtils.encode(84.6, 10.5);

    double[] latitudeLongitude = GeoHashUtils.decode(hash,shapeIO);

    assertEquals(84.6, latitudeLongitude[0], 0.00001D);
    assertEquals(10.5, latitudeLongitude[1], 0.00001D);
  }

  /*
   * see https://issues.apache.org/jira/browse/LUCENE-1815 for details
   */
  @Test
  public void testDecodeEncode() {
    String geoHash = "u173zq37x014";
    assertEquals(geoHash, GeoHashUtils.encode(52.3738007, 4.8909347));
    double[] decode = GeoHashUtils.decode(geoHash,shapeIO);
    assertEquals(52.37380061d, decode[0], 0.000001d);
    assertEquals(4.8909343d, decode[1], 0.000001d);

    assertEquals(geoHash, GeoHashUtils.encode(decode[0], decode[1]));

    geoHash = "u173";
    decode = GeoHashUtils.decode("u173",shapeIO);
    geoHash = GeoHashUtils.encode(decode[0], decode[1]);
    assertEquals(decode[0], GeoHashUtils.decode(geoHash,shapeIO)[0], 0.000001d);
    assertEquals(decode[1], GeoHashUtils.decode(geoHash,shapeIO)[1], 0.000001d);
  }

  /** see the table at http://en.wikipedia.org/wiki/Geohash */
  @Test
  public void testHashLenToWidth() {
    double[] box = GeoHashUtils.lookupDegreesSizeForHashLen(3);
    assertEquals(1.40625,box[0],0.0001);
    assertEquals(1.40625,box[1],0.0001);
  }
}
