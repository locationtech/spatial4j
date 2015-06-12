/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.shape.impl;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.SpatialRelation;

import java.util.Formatter;
import java.util.Locale;

/**
 * A circle as it exists on the surface of a sphere.
 */
public class GeoCircle extends CircleImpl {
  private GeoCircle inverseCircle;//when distance reaches > 1/2 way around the world, cache the inverse.
  private double horizAxisY;//see getYAxis

  public GeoCircle(Point p, double radiusDEG, SpatialContext ctx) {
    super(p, radiusDEG, ctx);
    assert ctx.isGeo();
    init();
  }

  @Override
  public void reset(double x, double y, double radiusDEG) {
    super.reset(x, y, radiusDEG);
    init();
  }

  private void init() {
    if (radiusDEG > 90) {
      //--spans more than half the globe
      assert enclosingBox.getWidth() == 360;
      double backDistDEG = 180 - radiusDEG;
      if (backDistDEG > 0) {
        double backRadius = 180 - radiusDEG;
        double backX = DistanceUtils.normLonDEG(getCenter().getX() + 180);
        double backY = DistanceUtils.normLatDEG(getCenter().getY() + 180);
        //Shrink inverseCircle as small as possible to avoid accidental overlap.
        // Note that this is tricky business to come up with a value small enough
        // but not too small or else numerical conditioning issues become a problem.
        backRadius -= Math.max(Math.ulp(Math.abs(backY)+backRadius), Math.ulp(Math.abs(backX)+backRadius));
        if (inverseCircle != null) {
          inverseCircle.reset(backX, backY, backRadius);
        } else {
          inverseCircle = new GeoCircle(ctx.makePoint(backX, backY), backRadius, ctx);
        }
      } else {
        inverseCircle = null;//whole globe
      }
      horizAxisY = getCenter().getY();//although probably not used
    } else {
      inverseCircle = null;
      double _horizAxisY = ctx.getDistCalc().calcBoxByDistFromPt_yHorizAxisDEG(getCenter(), radiusDEG, ctx);
      //some rare numeric conditioning cases can cause this to be barely beyond the box
      if (_horizAxisY > enclosingBox.getMaxY()) {
        horizAxisY = enclosingBox.getMaxY();
      } else if (_horizAxisY < enclosingBox.getMinY()) {
        horizAxisY = enclosingBox.getMinY();
      } else {
        horizAxisY = _horizAxisY;
      }
      //assert enclosingBox.relate_yRange(horizAxis,horizAxis,ctx).intersects();
    }
  }

  @Override
  protected double getYAxis() {
    return horizAxisY;
  }

  /**
   * Called after bounding box is intersected.
   * @param bboxSect INTERSECTS or CONTAINS from enclosingBox's intersection
   * @return DISJOINT, CONTAINS, or INTERSECTS (not WITHIN)
   */
  @Override
  protected SpatialRelation relateRectanglePhase2(Rectangle r, SpatialRelation bboxSect) {

    if (inverseCircle != null) {
      return inverseCircle.relate(r).inverse();
    }

    //if a pole is wrapped, we have a separate algorithm
    if (enclosingBox.getWidth() == 360) {
      return relateRectangleCircleWrapsPole(r, ctx);
    }

    //This is an optimization path for when there are no dateline or pole issues.
    if (!enclosingBox.getCrossesDateLine() && !r.getCrossesDateLine()) {
      return super.relateRectanglePhase2(r, bboxSect);
    }

    //Rectangle wraps around the world longitudinally creating a solid band; there are no corners to test intersection
    if (r.getWidth() == 360) {
      return SpatialRelation.INTERSECTS;
    }

    //do quick check to see if all corners are within this circle for CONTAINS
    int cornersIntersect = numCornersIntersect(r);
    if (cornersIntersect == 4) {
      //ensure r's x axis is within c's.  If it doesn't, r sneaks around the globe to touch the other side (intersect).
      SpatialRelation xIntersect = r.relateXRange(enclosingBox.getMinX(), enclosingBox.getMaxX());
      if (xIntersect == SpatialRelation.WITHIN)
        return SpatialRelation.CONTAINS;
      return SpatialRelation.INTERSECTS;
    }

    //INTERSECT or DISJOINT ?
    if (cornersIntersect > 0)
      return SpatialRelation.INTERSECTS;

    //Now we check if one of the axis of the circle intersect with r.  If so we have
    // intersection.

    /* x axis intersects  */
    if ( r.relateYRange(getYAxis(), getYAxis()).intersects() // at y vertical
          && r.relateXRange(enclosingBox.getMinX(), enclosingBox.getMaxX()).intersects() )
      return SpatialRelation.INTERSECTS;

    /* y axis intersects */
    if (r.relateXRange(getXAxis(), getXAxis()).intersects()) { // at x horizontal
      double yTop = getCenter().getY()+ radiusDEG;
      assert yTop <= 90;
      double yBot = getCenter().getY()- radiusDEG;
      assert yBot >= -90;
      if (r.relateYRange(yBot, yTop).intersects())//back bottom
        return SpatialRelation.INTERSECTS;
    }

    return SpatialRelation.DISJOINT;
  }

  private SpatialRelation relateRectangleCircleWrapsPole(Rectangle r, SpatialContext ctx) {
    //This method handles the case where the circle wraps ONE pole, but not both.  For both,
    // there is the inverseCircle case handled before now.  The only exception is for the case where
    // the circle covers the entire globe, and we'll check that first.
    if (radiusDEG == 180)//whole globe
      return SpatialRelation.CONTAINS;

    //Check if r is within the pole wrap region:
    double yTop = getCenter().getY() + radiusDEG;
    if (yTop > 90) {
      double yTopOverlap = yTop - 90;
      assert yTopOverlap <= 90;
      if (r.getMinY() >= 90 - yTopOverlap)
        return SpatialRelation.CONTAINS;
    } else {
      double yBot = point.getY() - radiusDEG;
      if (yBot < -90) {
        double yBotOverlap = -90 - yBot;
        assert yBotOverlap <= 90;
        if (r.getMaxY() <= -90 + yBotOverlap)
          return SpatialRelation.CONTAINS;
      } else {
        //This point is probably not reachable ??
        assert yTop == 90 || yBot == -90;//we simply touch a pole
        //continue
      }
    }

    //If there are no corners to check intersection because r wraps completely...
    if (r.getWidth() == 360)
      return SpatialRelation.INTERSECTS;

    //Check corners:
    int cornersIntersect = numCornersIntersect(r);
    // (It might be possible to reduce contains() calls within nCI() to exactly two, but this intersection
    //  code is complicated enough as it is.)
    double frontX = getCenter().getX();
    if (cornersIntersect == 4) {//all
      double backX = frontX <= 0 ? frontX + 180 : frontX - 180;
      if (r.relateXRange(backX, backX).intersects())
        return SpatialRelation.INTERSECTS;
      else
        return SpatialRelation.CONTAINS;
    } else if (cornersIntersect == 0) {//none
      if (r.relateXRange(frontX, frontX).intersects())
        return SpatialRelation.INTERSECTS;
      else
        return SpatialRelation.DISJOINT;
    } else//partial
      return SpatialRelation.INTERSECTS;
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
    //Add distance in km, which may be easier to recognize.
    double distKm = DistanceUtils.degrees2Dist(radiusDEG,  DistanceUtils.EARTH_MEAN_RADIUS_KM);
    //instead of String.format() so that we get consistent output no matter the locale
    String dStr = new Formatter(Locale.ROOT).format("%.1f\u00B0 %.2fkm", radiusDEG, distKm).toString();
    return "Circle(" + point + ", d=" + dStr + ')';
  }
}
