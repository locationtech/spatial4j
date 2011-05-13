package org.apache.lucene.spatial.strategy.prefix;

import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;

import java.util.Collection;

/**
 * @author Chris Male
 */
public class MockSpatialPrefixGrid extends SpatialPrefixGrid {

  public MockSpatialPrefixGrid() {
    super(null, 100);
  }

  @Override
  public Collection<Cell> getCells(Shape shape) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Cell getCell(double x, double y, int level) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Cell getCell(String token) {
    return new Cell(token) {

      @Override
      public Collection<Cell> getSubCells() {
        throw new UnsupportedOperationException();
      }

      @Override
      public Shape getShape() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public Point getPoint(String token) {
    throw new UnsupportedOperationException();
  }

}
