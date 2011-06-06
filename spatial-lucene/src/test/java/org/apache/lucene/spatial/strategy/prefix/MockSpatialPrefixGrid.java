package org.apache.lucene.spatial.strategy.prefix;

import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.shape.Shape;

import java.util.Collection;


public class MockSpatialPrefixGrid extends SpatialPrefixGrid {

  public MockSpatialPrefixGrid() {
    super(null, 100);
  }

  @Override
  protected int getLevelForDistance(double dist) {
    return 5;
  }

  @Override
  public Cell getCell(String token) {
    return new Cell(token) {

      @Override
      public Collection<Cell> getSubCells(Shape shapeFilter) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Collection<Cell> getSubCells() {
        throw new UnsupportedOperationException();
      }

      @Override
      public int getSubCellsSize() {
        throw new UnsupportedOperationException();
      }

      @Override
      public Shape getShape() {
        throw new UnsupportedOperationException();
      }
    };
  }

}
