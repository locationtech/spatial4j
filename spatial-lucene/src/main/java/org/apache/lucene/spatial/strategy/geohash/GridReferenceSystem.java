package org.apache.lucene.spatial.strategy.geohash;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;

import java.util.*;

/**
 * A SpatialPrefixGrid based on Geohashes.  Uses {@link GeoHashUtils} to do all the geohash work.
 *
 * TODO at the moment it doesn't handle suffixes such as {@link SpatialPrefixGrid#INTERSECTS}. Only does full-resolution
 * points.
 */
public class GridReferenceSystem extends SpatialPrefixGrid {

  public GridReferenceSystem(SpatialContext shapeIO, int maxLevels) {
    super(shapeIO, maxLevels);
    int MAXP = getMaxLevelsPossible();
    if (maxLevels <= 0 || maxLevels > MAXP)
      throw new IllegalArgumentException("maxLen must be (0-"+MAXP+"] but got "+ maxLevels);
  }

  /** Any more than this and there's no point (double lat & lon are the same). */
  public static int getMaxLevelsPossible() { return GeoHashUtils.MAX_PRECISION; }

  @Override
  public Collection<Cell> getCells(Shape shape) {
    BBox r = shape.getBoundingBox();
    double width = r.getMaxX() - r.getMinX();
    double height = r.getMaxY() - r.getMinY();
    int len = GeoHashUtils.lookupHashLenForWidthHeight(width,height);
    len = Math.min(len,maxLevels-1);

    //TODO !! Bug: incomplete when at top level and covers more than 4 cells
    Set<Cell> cornerCells = new TreeSet<Cell>();
    cornerCells.add(getCell(r.getMinX(), r.getMinY(), len));
    cornerCells.add(getCell(r.getMinX(), r.getMaxY(), len));
    cornerCells.add(getCell(r.getMaxX(), r.getMaxY(), len));
    cornerCells.add(getCell(r.getMaxX(), r.getMinY(), len));

    return cornerCells;
  }

  @Override
  public Cell getCell(double x, double y, int level) {
    final String hash = GeoHashUtils.encode(y, x, level);
    return new GridNode(hash);
  }

  @Override
  public Cell getCell(String token) {
    return new GridNode(token);
  }

  @Override
  public Point getPoint(String token) {
    if (token.length() < maxLevels)
      return null;
    return GeoHashUtils.decode(token,shapeIO);
  }

  /**
   * A node in a geospatial grid hierarchy as specified by a {@link GridReferenceSystem}.
   */
  class GridNode extends SpatialPrefixGrid.Cell {
    public GridNode(String token) {
      super(token);
    }

    public Collection<Cell> getSubCells() {
      if (getLevel() >= GridReferenceSystem.this.getMaxLevels())
        return null;
      String[] hashes = GeoHashUtils.getSubGeoHashes(getGeohash());
      Arrays.sort(hashes);
      ArrayList<Cell> cells = new ArrayList<Cell>(hashes.length);
      for (String hash : hashes) {
        cells.add(new GridNode(hash));
      }
      return cells;
    }

    @Override
    public BBox getShape() {
      return GeoHashUtils.decodeBoundary(getGeohash(), shapeIO);// min-max lat, min-max lon
    }

    private String getGeohash() {
      return token;
//      if (getLevel() >= getMaxLevels())
//        return token.substring(0,token.length()-1);
//      else
//        return token;
    }

  }

}
