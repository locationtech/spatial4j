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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;

/**
 * Implementations should be threadsafe.
 */
public abstract class SpatialPrefixGrid {

  public static final char COVER = '*';
  public static final char INTERSECTS = '+';

  //TODO some sort of precision model should probably be here. Maybe via a spatial context.

  protected final int maxLevels;

  protected final SpatialContext shapeIO;

  public SpatialPrefixGrid(SpatialContext shapeIO, int maxLevels) {
    assert maxLevels > 0;
    this.shapeIO = shapeIO;
    this.maxLevels = maxLevels;
  }

  public SpatialContext getShapeIO() {
    return shapeIO;
  }

  /**
   * Returns a "minimal" set of cells at the same grid level that together cover the given shape -- about 4. This is a
   * loose definition in which the grid level chosen should be the lowest in which the shape area fills over half of the
   * area covered by the returned cells.
   * @param shape not null
   * @return not null, not-empty, no duplicates, sorted.
   */
  public abstract Collection<Cell> getCells(Shape shape);

  public abstract Cell getCell(double x, double y, int level);
  //TODO getCell x,y  with accuracy radius?

  public int getMaxLevels() {
    return maxLevels;
  }

  //TODO deprecate for byte[]
  /** The cell for the specified token. Token should *not* end with INTERSECTS */
  public abstract Cell getCell(String token);
//
//  public Cell getCell(byte[] token) {
//    return getCell(new String(token,UTF8));
//  }

  protected static final Charset UTF8 = Charset.forName("UTF-8");

  /**
   * Decodes the token into a Point. The token must be at its full resolution (i.e. end
   * with a '+') otherwise null is returned.
   * @param token
   * @return possibly null
   */
  public abstract Point getPoint(String token);

  public static abstract class Cell implements Comparable<Cell> {

    protected final String token;

    public Cell(String token) {
      this.token = token;
    }

    //TODO deprecate for byte[]
    public String getTokenString() {
      return token;
    }


    public byte[] getBytes() {
      return token.getBytes(UTF8);
    }

    public int getLevel() {
      return this.token.length()-1;//assume ends with '*' or '-'
    }

    /**
     * Gets the cells at the next level that cover this cell.
     * @param node
     * @return A set of cells (no dups), 2 or more in size, sorted. Null if hits a depth/precision constraint.
     */
    public abstract Collection<Cell> getSubCells();

    //TODO should we ask that impls efficiently cache the shape?

    public abstract Shape getShape();

    @Override
    public int compareTo(Cell o) {
      return getTokenString().compareTo(o.getTokenString());
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof Cell))
        return false;
      return getTokenString().equals(((Cell)obj).getTokenString());
    }

    @Override
    public int hashCode() {
      return getTokenString().hashCode();
    }

    @Override
    public String toString() {
      return getTokenString();
    }
  }

  /** Transitional for legacy code. */
  @Deprecated
  public static List<String> cellsToTokenStrings(Collection<Cell> cells) {
    ArrayList<String> tokens = new ArrayList<String>(cells.size());
    for (Cell cell : cells) {
      tokens.add(cell.getTokenString());
    }
    return tokens;
  }
}
