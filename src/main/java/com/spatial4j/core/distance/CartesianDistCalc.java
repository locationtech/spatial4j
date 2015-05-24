/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Contributors:
 *    Ryan McKinley - initial API and implementation
 *    David Smiley
 ******************************************************************************/

package com.spatial4j.core.distance;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;

/**
 * Calculates based on Euclidean / Cartesian 2d plane.
 */
public class CartesianDistCalc extends AbstractDistanceCalculator {

  private final boolean squared;

  public CartesianDistCalc() {
    this.squared = false;
  }

  /**
   * @param squared Set to true to have {@link #distance(com.spatial4j.core.shape.Point, com.spatial4j.core.shape.Point)}
   *                return the square of the correct answer. This is a
   *                performance optimization used when sorting in which the
   *                actual distance doesn't matter so long as the sort order is
   *                consistent.
   */
  public CartesianDistCalc(boolean squared) {
    this.squared = squared;
  }

  @Override
  public double distance(Point from, double toX, double toY) {
    double deltaX = from.getX() - toX;
    double deltaY = from.getY() - toY;
    double xSquaredPlusYSquared = deltaX*deltaX + deltaY*deltaY;

    if (squared)
      return xSquaredPlusYSquared;

    return Math.sqrt(xSquaredPlusYSquared);
  }

  @Override
  public boolean within(Point from, double toX, double toY, double distance) {
    double deltaX = from.getX() - toX;
    double deltaY = from.getY() - toY;
    return deltaX*deltaX + deltaY*deltaY <= distance*distance;
  }

  @Override
  public Point pointOnBearing(Point from, double distDEG, double bearingDEG, SpatialContext ctx, Point reuse) {
    if (distDEG == 0) {
      if (reuse == null)
        return from;
      reuse.reset(from.getX(), from.getY());
      return reuse;
    }
    double bearingRAD = DistanceUtils.toRadians(bearingDEG);
    double x = from.getX() + Math.sin(bearingRAD) * distDEG;
    double y = from.getY() + Math.cos(bearingRAD) * distDEG;
    if (reuse == null) {
      return ctx.makePoint(x, y);
    } else {
      reuse.reset(x, y);
      return reuse;
    }
  }

  @Override
  public Rectangle calcBoxByDistFromPt(Point from, double distDEG, SpatialContext ctx, Rectangle reuse) {
    double minX = from.getX() - distDEG;
    double maxX = from.getX() + distDEG;
    double minY = from.getY() - distDEG;
    double maxY = from.getY() + distDEG;
    if (reuse == null) {
      return ctx.makeRectangle(minX, maxX, minY, maxY);
    } else {
      reuse.reset(minX, maxX, minY, maxY);
      return reuse;
    }
  }

  @Override
  public double calcBoxByDistFromPt_yHorizAxisDEG(Point from, double distDEG, SpatialContext ctx) {
    return from.getY();
  }

  @Override
  public double area(Rectangle rect) {
    return rect.getArea(null);
  }

  @Override
  public double area(Circle circle) {
    return circle.getArea(null);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CartesianDistCalc that = (CartesianDistCalc) o;

    if (squared != that.squared) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return (squared ? 1 : 0);
  }
}
