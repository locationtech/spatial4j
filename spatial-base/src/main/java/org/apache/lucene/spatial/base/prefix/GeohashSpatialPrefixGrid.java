package org.apache.lucene.spatial.base.prefix;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A SpatialPrefixGrid based on Geohashes.  Uses {@link GeohashUtils} to do all the geohash work.
 */
public class GeohashSpatialPrefixGrid extends SpatialPrefixGrid {

  public GeohashSpatialPrefixGrid(SpatialContext shapeIO, int maxLevels) {
    super(shapeIO, maxLevels);
    int MAXP = getMaxLevelsPossible();
    if (maxLevels <= 0 || maxLevels > MAXP)
      throw new IllegalArgumentException("maxLen must be [1-"+MAXP+"] but got "+ maxLevels);
  }

  /** Any more than this and there's no point (double lat & lon are the same). */
  public static int getMaxLevelsPossible() { return GeohashUtils.MAX_PRECISION; }

  @Override
  public int getLevelForDistance(double dist) {
    final int level = GeohashUtils.lookupHashLenForWidthHeight(dist, dist);
    return Math.max(Math.min(level, maxLevels), 1);
  }

  @Override
  public Cell getCell(Point p, int level) {
    return new GhCell(GeohashUtils.encode(p.getY(),p.getX(), level));//args are lat,lon (y,x)
  }

  @Override
  public Cell getCell(String token) {
    return new GhCell(token);
  }

  @Override
  public Cell getCell(byte[] bytes, int offset, int len) {
    return new GhCell(bytes, offset, len);
  }

  @Override
  public List<Cell> getCells(Shape shape, int detailLevel, boolean inclParents) {
    if (shape instanceof Point)
      return super.getCellsAltPoint((Point) shape, detailLevel, inclParents);
    else
      return super.getCells(shape, detailLevel, inclParents);
  }

  class GhCell extends SpatialPrefixGrid.Cell {
    GhCell(String token) {
      super(token);
    }

    GhCell(byte[] bytes, int off, int len) {
      super(bytes, off, len);
    }

    @Override
    public void reset(byte[] bytes, int off, int len) {
      super.reset(bytes, off, len);
      shape = null;
    }

    @Override
    public Collection<SpatialPrefixGrid.Cell> getSubCells() {
      String[] hashes = GeohashUtils.getSubGeohashes(getGeohash());//sorted
      ArrayList<SpatialPrefixGrid.Cell> cells = new ArrayList<SpatialPrefixGrid.Cell>(hashes.length);
      for (String hash : hashes) {
        cells.add(new GhCell(hash));
      }
      return cells;
    }

    @Override
    public int getSubCellsSize() {
      return 32;//8x4
    }

    @Override
    public Cell getSubCell(Point p) {
      return GeohashSpatialPrefixGrid.this.getCell(p,getLevel()+1);//not performant!
    }

    private Shape shape;//cache

    @Override
    public Shape getShape() {
      if (shape == null) {
        if (getLevel() == getMaxLevels())
          shape = GeohashUtils.decode(getGeohash(), shapeIO);
        else
          shape = GeohashUtils.decodeBoundary(getGeohash(), shapeIO);
      }
      return shape;
    }

    private String getGeohash() {
      return getTokenString();
    }

  }

}
