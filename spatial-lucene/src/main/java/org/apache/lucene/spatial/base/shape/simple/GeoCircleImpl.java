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

package org.apache.lucene.spatial.base.shape.simple;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.shape.IntersectCase;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Rectangle;

/**
 * @author David Smiley - dsmiley@mitre.org
 */
public class GeoCircleImpl extends CircleImpl {
  private final GeoCircleImpl inverseCircle;//when distance reaches > 1/2 way around the world, cache the inverse.
  private final double distDEG;// [0 TO 180]

  public GeoCircleImpl(Point p, double dist, SpatialContext ctx) {
    super(p, dist, ctx);
    assert ctx.isGeo();

    //In the direction of latitude (N,S), distance is the same number of degrees.
    distDEG = ctx.getDistCalc().distanceToDegrees(distance);

    if (distDEG > 90) {
      double backDistance = ctx.getDistCalc().degreesToDistance(180 - distDEG) - Double.MIN_VALUE;
      inverseCircle = new GeoCircleImpl(ctx.makePoint(point.getX()+180,point.getY()+180),backDistance,ctx);
    } else {
      inverseCircle = null;
    }
  }

  @Override
  protected IntersectCase intersectRectanglePhase2(Rectangle r, IntersectCase bboxSect, SpatialContext ctx) {
    if (enclosingBox.getCrossesDateLine() || r.getCrossesDateLine()
        || enclosingBox.getWidth()==360 || r.getWidth()==360) {
      return intersectRectangleGeoCapable(r, bboxSect, ctx);
    } else {
      return super.intersectRectanglePhase2(r,bboxSect,ctx);
    }
  }

  /** Handles geospatial contexts that involve world wrap &/ pole wrap (and non-geo too). */
  private IntersectCase intersectRectangleGeoCapable(Rectangle r, IntersectCase bboxSect, SpatialContext ctx) {

    if (inverseCircle != null) {
      return inverseCircle.intersect(r,ctx).inverse();
    }

    //if a pole is wrapped, we have a separate algorithm
    if (ctx.isGeo() && enclosingBox.getWidth() == 360) {
      return intersectRectangleCircleWrapsPole(r, ctx);
    }

    //Rectangle wraps around the world longitudinally; there are no corners to test intersection
    if (ctx.isGeo() && r.getWidth() == 360) {
      return IntersectCase.INTERSECTS;
    }

    //do quick check to see if all corners are within this circle for CONTAINS
    int cornersIntersect = numCornersIntersect(r);
    if (cornersIntersect == 4) {
      //ensure r's x axis is within c's.  If it doesn't, r sneaks around the globe to touch the other side (intersect).
      IntersectCase xIntersect = r.intersect_xRange(enclosingBox.getMinX(),enclosingBox.getMaxX(),ctx);
      if (xIntersect == IntersectCase.WITHIN)
        return IntersectCase.CONTAINS;
      return IntersectCase.INTERSECTS;
    }

    //INTERSECT or OUTSIDE ?
    if (cornersIntersect > 0)
      return IntersectCase.INTERSECTS;

    //Now we check if one of the axis of the circle intersect with r.  If so we have
    // intersection.

    /* x axis intersects  */
    if ( r.intersect_yRange(point.getY(),point.getY(),ctx).intersects() // at y vertical
          && r.intersect_xRange(enclosingBox.getMinX(),enclosingBox.getMaxX(),ctx).intersects() )
      return IntersectCase.INTERSECTS;

    /* y axis intersects */
    if (r.intersect_xRange(point.getX(),point.getX(),ctx).intersects()) { // at x horizontal
      if (ctx.isGeo()) {
        double yTop = getCenter().getY()+ distDEG;
        assert yTop <= 90;
        double yBot = getCenter().getY()- distDEG;
        assert yBot <= 90;
        if (r.intersect_yRange(yBot,yTop,ctx).intersects())//back bottom
          return IntersectCase.INTERSECTS;
      } else {
        if (r.intersect_yRange(point.getY()-distance,point.getY()+distance,ctx).intersects())
          return IntersectCase.INTERSECTS;
      }
    }

    return IntersectCase.OUTSIDE;
  }

  private IntersectCase intersectRectangleCircleWrapsPole(Rectangle r, SpatialContext ctx) {

    //Check if r is within the pole wrap region:
    double yTop = point.getY()+ distDEG;
    if (yTop > 90) {
      double yTopOverlap = yTop - 90;
      assert yTopOverlap <= 90;
      if (r.getMinY() >= 90 - yTopOverlap)
        return IntersectCase.CONTAINS;
    } else {
      double yBot = point.getY() - distDEG;
      assert yBot < -90;
      double yBotOverlap = -90 - yBot;
      assert yBotOverlap <= 90;
      if (r.getMaxY() <= -90 + yBotOverlap)
        return IntersectCase.CONTAINS;
    }

    //If there are no corners to check intersection because r wraps completely...
    if (r.getWidth() == 360)
      return IntersectCase.INTERSECTS;

    //Check corners:
    int cornersIntersect = numCornersIntersect(r);
    // (It might be possible to reduce contains() calls within nCI() to exactly two, but this intersection
    //  code is complicated enough as it is.)
    if (cornersIntersect == 4) {//all
      double backX = ctx.normX(point.getX()+180);
      if (r.intersect_xRange(backX,backX,ctx).intersects())
        return IntersectCase.INTERSECTS;
      else
        return IntersectCase.CONTAINS;
    } else if (cornersIntersect == 0) {//none
      if (r.intersect_xRange(point.getX(),point.getX(),ctx).intersects())
        return IntersectCase.INTERSECTS;
      else
        return IntersectCase.OUTSIDE;
    } else//partial
      return IntersectCase.INTERSECTS;
  }

  /** Returns either 0 for none, 1 for some, or 4 for all. */
  private int numCornersIntersect(Rectangle r) {
    //We play some logic games to avoid calling contains() which can be expensive.
    boolean bool;//if true then all corners intersect, if false then no corners intersect
    // for partial, we exit early with 1 and ignore bool.
    bool = (contains(r.getMinX(),r.getMinY()));
    if (contains(r.getMinX(),r.getMaxY())) {
      if (!bool)
        return 1;//partial
    } else {
      if (bool)
        return 1;//partial
    }
    if (contains(r.getMaxX(),r.getMinY())) {
      if (!bool)
        return 1;//partial
    } else {
      if (bool)
        return 1;//partial
    }
    if (contains(r.getMaxX(),r.getMaxY())) {
      if (!bool)
        return 1;//partial
    } else {
      if (bool)
        return 1;//partial
    }
    return bool?4:0;
  }

  @Override
  public String toString() {
    //I'm deliberately making this look basic and not fully detailed with class name & misc fields.
    //Add distance in degrees, which is easier to recognize, and earth radius agnostic.
    String dStr = String.format("%.1f",distance);
    if (ctx.isGeo()) {
      double distDEG = ctx.getDistCalc().distanceToDegrees(distance);
      dStr += String.format("=%.1f\u00B0",distDEG);
    }
    return "Circle(" + point + ",d=" + dStr + ')';
  }
}
