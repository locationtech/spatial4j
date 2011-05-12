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

package org.apache.lucene.spatial.base.prefix;

import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.SpatialContextProvider;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.simple.Point2D;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

public class QuadPrefixGrid extends SpatialPrefixGrid {

  private final double xmin;
  private final double xmax;
  private final double ymin;
  private final double ymax;
  private final double xmid;
  private final double ymid;

  private final double gridW;
  public final double gridH;

  final double[] levelW;
  final double[] levelH;
  final int[]    levelS; // side
  final int[]    levelN; // number

  private int minResolution = 6; // Go at least this deep
  private int resolution = 4; // how far down past the 'bbox level'

  private final SpatialContext shapeIO;

  public QuadPrefixGrid(
      double xmin, double xmax,
      double ymin, double ymax,
      int maxLevels, SpatialContext shapeIO) {
    super(maxLevels);
    this.xmin = xmin;
    this.xmax = xmax;
    this.ymin = ymin;
    this.ymax = ymax;
    this.shapeIO = shapeIO;

    levelW = new double[maxLevels];
    levelH = new double[maxLevels];
    levelS = new int[maxLevels];
    levelN = new int[maxLevels];

    gridW = xmax - xmin;
    gridH = ymax - ymin;
    this.xmid = xmin + gridW/2.0;
    this.ymid = ymin + gridH/2.0;
    levelW[0] = gridW/2.0;
    levelH[0] = gridH/2.0;
    levelS[0] = 2;
    levelN[0] = 4;

    for (int i = 1; i < maxLevels; i++) {
      levelW[i] = levelW[i - 1] / 2.0;
      levelH[i] = levelH[i - 1] / 2.0;
      levelS[i] = levelS[i - 1] * 2;
      levelN[i] = levelN[i - 1] * 4;
    }
  }

  public QuadPrefixGrid(
      double xmin, double xmax,
      double ymin, double ymax,
      int maxLevels) {
    this( xmin, xmax, ymin, ymax, maxLevels, SpatialContextProvider.getContext() );
  }

  public QuadPrefixGrid(
      double xmin,
      double xmax,
      double ymin,
      double ymax,
      int maxLevels,
      int minResolution,
      int resolution) {
    this(xmin, xmax, ymin, ymax, maxLevels, SpatialContextProvider.getContext() );
    this.minResolution = minResolution;
    this.resolution = resolution;
  }

  public QuadPrefixGrid() {
    this(-180, 180, -90, 90, 12, SpatialContextProvider.getContext());
  }

  public void printInfo() {
    NumberFormat nf = NumberFormat.getNumberInstance();
    nf.setMaximumFractionDigits(5);
    nf.setMinimumFractionDigits(5);
    nf.setMinimumIntegerDigits(3);

    for (int i = 0; i < maxLevels; i++) {
      System.out.println(i + "]\t" + nf.format(levelW[i]) + "\t" + nf.format(levelH[i]) + "\t" +
          levelS[i] + "\t" + (levelS[i] * levelS[i]));
    }
  }

  /**
   * Find a "reasonable" level of detail for a given shape.
   */
  private int getBestLevel(Shape geo) {
    BBox ext = geo.getBoundingBox();
    double w = ext.getWidth();
    double h = ext.getHeight();

    for (int i = 0; i < maxLevels; i++) {
      if(w > levelW[i] || h > levelH[i]) {
        return i;
      }
    }
    return maxLevels;
  }

  @Override
  public List<Cell> getCells(Shape shape) {
    List<Cell> cells = new ArrayList<Cell>();
    int bboxLevel = getBestLevel(shape);
    int maxLevel = Math.min(maxLevels, bboxLevel + resolution);
    if (maxLevel < minResolution) {
      maxLevel = minResolution;
    }

    build(xmid, ymid, 0, cells, new StringBuilder(), shape, maxLevel);
    return cells;
  }

  @Override
  public Cell getCell(double x, double y, int level) {
    List<Cell> cells = new ArrayList<Cell>(1);
    build(xmid, ymid, 0, cells, new StringBuilder(), new Point2D(x,y), level);
    assert cells.size()==1;
    return cells.get(0);
  }

  @Override
  public Cell getCell(String token) {
    return new QuadCell(token);
  }

  private void build(
      double x,
      double y,
      int level,
      List<Cell> matches,
      StringBuilder str,
      Shape shape,
      int maxLevel) {
    double w = levelW[level] / 2;
    double h = levelH[level] / 2;

    // Z-Order
    // http://en.wikipedia.org/wiki/Z-order_%28curve%29
    checkBattenberg('A', x - w, y + h, level, matches, str, shape, maxLevel);
    checkBattenberg('B', x + w, y + h, level, matches, str, shape, maxLevel);
    checkBattenberg('C', x - w, y - h, level, matches, str, shape, maxLevel);
    checkBattenberg('D', x + w, y - h, level, matches, str, shape, maxLevel);

    // possibly consider hilbert curve
    // http://en.wikipedia.org/wiki/Hilbert_curve
    // http://blog.notdot.net/2009/11/Damn-Cool-Algorithms-Spatial-indexing-with-Quadtrees-and-Hilbert-Curves
    // if we actually use the range property in the query, this could be useful
  }

  private void checkBattenberg(
      char c,
      double cx,
      double cy,
      int level,
      List<Cell> matches,
      StringBuilder str,
      Shape shape,
      int maxLevel) {
    double w = levelW[level] / 2;
    double h = levelH[level] / 2;

    int strlen = str.length();
    BBox bBox = shapeIO.makeBBox(cx - w, cx + w, cy - h, cy + h);
    IntersectCase v = shape.intersect(bBox, shapeIO);
    if (IntersectCase.CONTAINS == v) {
      str.append(c);
      str.append(SpatialPrefixGrid.COVER);
      matches.add(new QuadCell(str.toString()));
    } else if (IntersectCase.OUTSIDE == v) {
      // nothing
    } else { // IntersectCase.WITHIN, IntersectCase.INTERSECTS
      if (IntersectCase.WITHIN == v) {
        str.append(Character.toLowerCase(c));
      }
      else {
        str.append(c);
      }

      int nextLevel = level+1;
      if (nextLevel >= maxLevel) {
        str.append(SpatialPrefixGrid.INTERSECTS);
        matches.add(new QuadCell(str.toString()));
      } else {
        build(cx, cy, nextLevel, matches, str, shape, maxLevel);
      }
    }
    str.setLength(strlen);
  }

  public static List<String> parseStrings(String cells) {
    ArrayList<String> tokens = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer(cells, "[], ");
    while (st.hasMoreTokens()) {
      tokens.add(st.nextToken());
    }
    return tokens;
  }

  public void setMinResolution(int minResolution) {
    this.minResolution = minResolution;
  }

  public void setResolution(int resolution) {
    this.resolution = resolution;
  }

  public int getMinResolution() {
    return minResolution;
  }

  public int getResolution() {
    return resolution;
  }

  class QuadCell extends Cell {

    public QuadCell(String token) {
      super(token);
    }

    @Override
    public Collection<Cell> getSubCells() {
      if (getLevel() >= maxLevels)
        return null;
      ArrayList<Cell> cells = new ArrayList<Cell>(4);
      cells.add(new QuadCell(getTokenString()+"A"));
      cells.add(new QuadCell(getTokenString()+"B"));
      cells.add(new QuadCell(getTokenString()+"C"));
      cells.add(new QuadCell(getTokenString()+"D"));
      return cells;
    }

    @Override
    public Shape getShape() {
      double xmin = QuadPrefixGrid.this.xmin;
      double ymin = QuadPrefixGrid.this.ymin;

      for (int i = 0; i < token.length() && i < maxLevels; i++) {
        char c = token.charAt(i);
        if ('A' == c || 'a' == c) {
          ymin += levelH[i];
        } else if ('B' == c || 'b' == c) {
          xmin += levelW[i];
          ymin += levelH[i];
        } else if ('C' == c || 'c' == c) {
          // nothing really
        }
        else if('D' == c || 'd' == c) {
          xmin += levelW[i];
        } else {
          throw new RuntimeException("unexpected char: " + c);
        }
      }
      int len = token.length() - 1;
      return shapeIO.makeBBox(xmin, xmin + levelW[len], ymin, ymin + levelH[len]);
    }
  }//QuadCell
}
