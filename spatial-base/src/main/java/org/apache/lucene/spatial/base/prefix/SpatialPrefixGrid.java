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

import org.apache.lucene.spatial.base.shape.Shape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementations should be threadsafe.
 */
public abstract class SpatialPrefixGrid {

  public static final char COVER = '*';
  public static final char INTERSECTS = '+';

  //TODO some sort of precision model should probably be here. Maybe via a spatial context.

  protected final int maxLevels;

  public SpatialPrefixGrid(int maxLevels) {
    assert maxLevels > 0;
    this.maxLevels = maxLevels;
  }

  /**
   * Returns a "minimal" set of cells at the same grid level that together cover the given shape -- about 4. This is a
   * loose definition in which the grid level chosen should be the lowest in which the shape area fills over half of the
   * area covered by the returned cells.
   * @param shape not null
   * @return not null, not-empty, no duplicates.
   */
  public abstract Collection<Cell> getCells(Shape shape);

  public abstract Cell getCell(double x, double y, int level, Cell parentNode);
  //TODO getCell x,y  with accuracy radius?

  public int getMaxLevels() {
    return maxLevels;
  }

  //TODO deprecate for byte[]
  public abstract Cell getCell(String token);
  //public abstract Cell getCell(byte[] token);

  public static abstract class Cell implements Comparable<Cell> {

    //public abstract byte[] getTokenByteArray();

    protected final String token;

    public Cell(String token) {
      this.token = token;
    }

    //TODO deprecate for byte[]
    public String getTokenString() {
      return token;
    }

    public int getLevel() {
      return this.token.length()-1;//assume ends with '*' or '-'
    }

    /**
     * Gets the cells at the next level that cover this cell.
     * @param node
     * @return A set of cells (no dups), 2 or more in size. Null if hits a depth/precision constraint.
     */
    public abstract Collection<Cell> getSubCells();

    /**
     * Get the shape for a given cell description -- probably a {@link org.apache.lucene.spatial.base.shape.BBox}.
     * Used for diagnostic purposes; might not be implemented.
     */
    public Shape getShape() {
      throw new UnsupportedOperationException("unimplemented");
    }

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
