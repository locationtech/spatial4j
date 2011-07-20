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
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Implementations should be threadsafe and immutable once initialized.
 */
public abstract class SpatialPrefixTree {

  protected static final Charset UTF8 = Charset.forName("UTF-8");

  protected final int maxLevels;

  protected final SpatialContext ctx;

  public SpatialPrefixTree(SpatialContext ctx, int maxLevels) {
    assert maxLevels > 0;
    this.ctx = ctx;
    this.maxLevels = maxLevels;
  }

  public SpatialContext getSpatialContext() {
    return ctx;
  }

  public int getMaxLevels() {
    return maxLevels;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()+"(maxLevels:"+maxLevels+",ctx:"+ctx+")";
  }

  /**
   * See {@link org.apache.lucene.spatial.base.query.SpatialArgs#getDistPrecision()}.
   * A grid level looked up via {@link #getLevelForDistance(double)} is returned.
   *
   * @param shape
   * @param precision 0-0.5
   * @return 1-maxLevels
   */
  public int getMaxLevelForPrecision(Shape shape, double precision) {
    if (precision < 0 || precision > 0.5)
      throw new IllegalArgumentException("Precision "+precision+" must be between [0-0.5]");
    if (precision == 0 || shape instanceof Point)
      return maxLevels;
    double bboxArea = shape.getBoundingBox().getArea();
    if (bboxArea == 0)
      return maxLevels;
    double avgSideLenFromCenter = Math.sqrt(bboxArea)/2;
    return getLevelForDistance(avgSideLenFromCenter*precision);
  }

  /**
   * Returns the level of the smallest grid size with a side length that is greater or equal to the provided
   * distance.
   * @param dist >= 0
   * @return level [1-maxLevels]
   */
  public abstract int getLevelForDistance(double dist);

  //TODO double getDistanceForLevel(int level)

  private transient Cell worldCell;//cached

  /**
   * Returns the level 0 cell which encompasses all spatial data. Equivalent to {@link #getCell(String)} with "".
   * This cell is threadsafe, just like a spatial prefix grid is, although cells aren't
   * generally threadsafe.
   * TODO rename to getTopCell or is this fine?
   */
  public Cell getWorldCell() {
    if (worldCell == null)
      worldCell = getCell("");
    return worldCell;
  }

  /**
   * The cell for the specified token. The empty string should be equal to {@link #getWorldCell()}.
   * Precondition: Never called when token length > maxLevel.
   */
  public abstract Cell getCell(String token);

  public abstract Cell getCell(byte[] bytes, int offset, int len);

  public final Cell getCell(byte[] bytes, int offset, int len, Cell target) {
    if (target == null)
      return getCell(bytes, offset, len);
    target.reset(bytes, offset, len);
    return target;
  }

  protected Cell getCell(Point p, int level) {
    return getCells(p,level,false).get(0);
  }

  /**
   * Gets the intersecting & including cells for the specified shape, without exceeding detail level.
   * The result is a set of cells (no dups), sorted. Unmodifiable.
   * <p>
   * This implementation checks if shape is a Point and if so uses an implementation that
   * recursively calls {@link Cell#getSubCell(org.apache.lucene.spatial.base.shape.Point)}. Cell subclasses
   * ideally implement that method with a quick implementation, otherwise, subclasses should
   * override this method to invoke {@link #getCellsAltPoint(org.apache.lucene.spatial.base.shape.Point, int, boolean)}.
   * TODO consider another approach returning an iterator -- won't build up all cells in memory.
   */
  public List<Cell> getCells(Shape shape, int detailLevel, boolean inclParents) {
    if (detailLevel > maxLevels)
      throw new IllegalArgumentException("detailLevel > maxLevels");
    ArrayList<Cell> cells;
    if (shape instanceof Point) {
      //optimized point algorithm
      final int initialCapacity = inclParents ? 1 + detailLevel : 1;
      cells = new ArrayList<Cell>(initialCapacity);
      recursiveGetCells(getWorldCell(),(Point)shape,detailLevel,true,cells);
      assert cells.size() == initialCapacity;
    } else {
      cells = new ArrayList<Cell>(inclParents ? 1024 : 512);
      recursiveGetCells(getWorldCell(), shape, detailLevel, inclParents, cells);
    }
    if (inclParents) {
      Cell c = cells.remove(0);//remove getWorldCell()
      assert c.getLevel() == 0;
    }
    return cells;
  }

  private void recursiveGetCells(Cell cell, Shape shape, int detailLevel, boolean inclParents,
                                    Collection<Cell> result) {
    if (cell.isLeaf()) {//cell is within shape
      result.add(cell);
      return;
    }
    final Collection<Cell> subCells = cell.getSubCells(shape);
    if (cell.getLevel() == detailLevel - 1) {
      if (subCells.size() < cell.getSubCellsSize()) {
        if (inclParents)
          result.add(cell);
        for (Cell subCell : subCells) {
          subCell.setLeaf();
        }
        result.addAll(subCells);
      } else {//a bottom level (i.e. detail level) optimization where all boxes intersect, so use parent cell.
        cell.setLeaf();
        result.add(cell);
      }
    } else {
      if (inclParents)
        result.add(cell);
      for (Cell subCell : subCells) {
        recursiveGetCells(subCell, shape, detailLevel, inclParents, result);//tail call
      }
    }
  }

  private void recursiveGetCells(Cell cell, Point point, int detailLevel, boolean inclParents,
                                 Collection<Cell> result) {
    if (inclParents)
      result.add(cell);
    final Cell pCell = cell.getSubCell(point);
    if (cell.getLevel() == detailLevel - 1) {
      pCell.setLeaf();
      result.add(pCell);
    } else {
      recursiveGetCells(pCell, point, detailLevel, inclParents, result);//tail call
    }
  }

  /** Subclasses might override {@link #getCells(org.apache.lucene.spatial.base.shape.Shape, int, boolean)}
   * and check if the argument is a shape and if so, delegate
   * to this implementation, which calls {@link #getCell(org.apache.lucene.spatial.base.shape.Point, int)} and
   * then calls {@link #getCell(String)} repeatedly if inclParents is true.
   */
  protected final List<Cell> getCellsAltPoint(Point p, int detailLevel, boolean inclParents) {
    Cell cell = getCell(p,detailLevel);
    if (!inclParents)
      return Collections.singletonList(cell);
    String endToken = cell.getTokenString();
    assert endToken.length() == detailLevel;
    List<Cell> cells = new ArrayList<Cell>(detailLevel);
    for(int i = 1; i < detailLevel; i++) {
      cells.add(getCell(endToken.substring(0,i)));
    }
    cells.add(cell);
    return cells;
  }

  /**
   * Represents a grid cell. These are not necessarily threadsafe, although new Cell("") (world cell) must be.
   */
  public abstract class Cell implements Comparable<Cell> {
    public static final byte LEAF_BYTE = '+';//NOTE: must sort before letters & numbers

    /*
    Holds a byte[] and/or String representation of the cell. Both are lazy constructed from the other.
    Neither contains the trailing leaf byte.
     */
    private byte[] bytes;
    private int b_off;
    private int b_len;

    private String token;//this is the only part of equality

    protected IntersectCase shapeRel;//set in getSubCells(filter), and via setLeaf().

    protected Cell(String token) {
      this.token = token;
      if (token.length() > 0 && token.charAt(token.length()-1) == (char)LEAF_BYTE) {
        this.token = token.substring(0,token.length()-1);
        setLeaf();
      }

      if (getLevel() == 0)
        getShape();//ensure any lazy instantiation completes to make this threadsafe
    }

    protected Cell(byte[] bytes, int off, int len) {
      this.bytes = bytes;
      this.b_off = off;
      this.b_len = len;
      b_fixLeaf();
    }

    public void reset(byte[] bytes, int off, int len) {
      assert getLevel() != 0;
      token = null;
      shapeRel = null;
      this.bytes = bytes;
      this.b_off = off;
      this.b_len = len;
      b_fixLeaf();
    }

    private void b_fixLeaf() {
      if (bytes[b_off + b_len - 1] == LEAF_BYTE) {
        b_len--;
        setLeaf();
      }
    }

    public IntersectCase getShapeRel() {
      return shapeRel;
    }

    public boolean isLeaf() {
      return shapeRel == IntersectCase.WITHIN || getLevel() == getMaxLevels();
    }

    public void setLeaf() {
      assert getLevel() != 0;
      shapeRel = IntersectCase.WITHIN;
    }

    /** Note: doesn't contain a trailing leaf byte. */
    public String getTokenString() {
      if (token == null)
        token = new String(bytes, b_off, b_len, UTF8);
      return token;
    }

    /** Note: doesn't contain a trailing leaf byte. */
    public byte[] getTokenBytes() {
      if (bytes != null) {
        if (b_off != 0 || b_len != bytes.length)
          throw new IllegalStateException("Not supported if byte[] needs to be recreated.");
      } else {
        bytes = token.getBytes(UTF8);
        b_off = 0;
        b_len = bytes.length;
      }
      return bytes;
    }

    public int getLevel() {
      return token != null ? token.length() : b_len;
    }

    //TODO add getParent() and update some algorithms to use this?
    //public Cell getParent();

    /**
     * Like {@link #getSubCells()} but with the results filtered by a shape. If that shape is a {@link Point} then it
     * must call {@link #getSubCell(org.apache.lucene.spatial.base.shape.Point)};
     * Precondition: Never called when getLevel() == maxLevel.
     * @param shapeFilter an optional filter for the returned cells.
     * @return A set of cells (no dups), sorted. Not Modifiable.
     */
    public Collection<Cell> getSubCells(Shape shapeFilter) {
      //Note: Higher-performing subclasses might override to consider the shape filter to generate fewer cells.
      if (shapeFilter instanceof Point) {
        return Collections.singleton(getSubCell((Point)shapeFilter));
      }
      Collection<Cell> cells = getSubCells();
      if (shapeFilter != null ) {
        ArrayList<Cell> copy = new ArrayList<Cell>(cells.size());//copy since cells contractually isn't modifiable
        for (Cell cell : cells) {
          IntersectCase rel = cell.getShape().intersect(shapeFilter,SpatialPrefixTree.this.ctx);
          if (rel == IntersectCase.OUTSIDE)
            continue;
          cell.shapeRel = rel;
          copy.add(cell);
        }
        cells = copy;
      }
      return cells;
    }

    /**
     * Performant implementations are expected to implement this efficiently by considering the current
     * cell's boundary.
     * Precondition: Never called when getLevel() == maxLevel.
     * Precondition: this.getShape().intersect(p) != OUTSIDE.
     * @param p
     * @return
     */
    public abstract Cell getSubCell(Point p);

    //TODO Cell getSubCell(byte b)

    /**
     * Gets the cells at the next grid cell level that cover this cell.
     * Precondition: Never called when getLevel() == maxLevel.
     * @return A set of cells (no dups), sorted. Not Modifiable.
     */
    protected abstract Collection<Cell> getSubCells();

    /** {@link #getSubCells()}.size() -- usually a constant. Should be >=2 */
    public abstract int getSubCellsSize();

    public abstract Shape getShape();

    public Point getCenter() {
      return getShape().getCenter();
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
      return getTokenString() + (isLeaf() ? (char)LEAF_BYTE :"");
    }

  }

  /** Will add the trailing leaf byte for leaves. This isn't particularly efficient. */
  public static List<String> cellsToTokenStrings(Collection<Cell> cells) {
    ArrayList<String> tokens = new ArrayList<String>((int)(cells.size()));
    for (Cell cell : cells) {
      final String token = cell.getTokenString();
      if (cell.isLeaf())
        tokens.add(token+(char)Cell.LEAF_BYTE);
      else
        tokens.add(token);
    }
    return tokens;
  }
}
