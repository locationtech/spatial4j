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

package org.apache.lucene.spatial.base.distance;

/**
 * <p><font color="red"><b>NOTE:</b> This API is still in
 * flux and might change in incompatible ways in the next
 * release.</font>
 */
public class LatLng {
  private double lat;
  private double lng;
  private boolean normalized;

  public LatLng(double lat, double lng) {
    if (lat>90.0 || lat<-90.0) throw new IllegalArgumentException("Illegal latitude value " + lat);
    this.lat=lat;
    this.lng=lng;
  }

  public LatLng(LatLng ll) {
    this.lat=ll.getLat();
    this.lng=ll.getLng();
  }

  public LatLng copy() {
    return new LatLng(this);
  }

  public double getLat() {
    return this.lat;
  }

  public double getLng() {
    return this.lng;
  }

  public boolean isNormalized() {
    return
      normalized || (
          (lng>=-180) &&
          (lng<=180)
          );
  }

  public LatLng normalize() {
    if (isNormalized()) return this;

    double delta=0;
    if (lng<0) delta=360;
    if (lng>=0) delta=-360;

    double newLng=lng;
    while (newLng<=-180 || newLng>=180) {
      newLng+=delta;
    }

    LatLng ret=new LatLng(lat, newLng);
    ret.normalized=true;
    return ret;
  }



  /**
   * Calculates the distance between two lat/lng's in miles.
   * Imported from mq java client.
   *
   * @param ll2
   *            Second lat,lng position to calculate distance to.
   *
   * @return Returns the distance in miles.
   */
  public double arcDistance(LatLng ll2) {
    return arcDistance(ll2, DistanceUnits.MILES);
  }

  /**
   * Calculates the distance between two lat/lng's in miles or meters.
   * Imported from mq java client.  Variable references changed to match.
   *
   * @param ll2
   *            Second lat,lng position to calculate distance to.
   * @param lUnits
   *            Units to calculate distance, defaults to miles
   *
   * @return Returns the distance in meters or miles.
   */
  public double arcDistance(LatLng ll2, DistanceUnits lUnits) {
    LatLng ll1 = normalize();
    ll2 = ll2.normalize();

    double lat1 = ll1.getLat(), lng1 = ll1.getLng();
    double lat2 = ll2.getLat(), lng2 = ll2.getLng();

    // Check for same position
    if (lat1 == lat2 && lng1 == lng2)
      return 0.0;

    // Get the m_dLongitude difference. Don't need to worry about
    // crossing 180 since cos(x) = cos(-x)
    double dLon = lng2 - lng1;

    double a = radians(90.0 - lat1);
    double c = radians(90.0 - lat2);
    double cosB = (Math.cos(a) * Math.cos(c))
        + (Math.sin(a) * Math.sin(c) * Math.cos(radians(dLon)));

    double radius = (lUnits == DistanceUnits.MILES) ? 3963.205/* MILERADIUSOFEARTH */
    : 6378.160187/* KMRADIUSOFEARTH */;

    // Find angle subtended (with some bounds checking) in radians and
    // multiply by earth radius to find the arc distance
    if (cosB < -1.0)
      return 3.14159265358979323846/* PI */* radius;
    else if (cosB >= 1.0)
      return 0;
    else
      return Math.acos(cosB) * radius;
  }

  private double radians(double a) {
    return a * 0.01745329251994;
  }

  @Override
  public String toString() {
    return "[" + getLat() + "," + getLng() + "]";
  }
  
  
  @Override
  public int hashCode() {
    final int prime = 31;
    long temp;
    temp = Double.doubleToLongBits(lat);
    int result = prime  + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(lng);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (normalized ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (getClass() != obj.getClass())
      return false;
    LatLng other = (LatLng) obj;
    if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
      return false;
    if (Double.doubleToLongBits(lng) != Double.doubleToLongBits(other.lng))
      return false;
    if (normalized != other.normalized)
      return false;
    return true;
  }

}
