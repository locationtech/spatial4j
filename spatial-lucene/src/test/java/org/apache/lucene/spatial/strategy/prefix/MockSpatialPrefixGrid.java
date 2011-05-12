package org.apache.lucene.spatial.strategy.prefix;

import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.shape.Shape;

import java.util.Collection;

/**
 * @author Chris Male
 */
public class MockSpatialPrefixGrid extends SpatialPrefixGrid {

  public MockSpatialPrefixGrid() {
    super(1);
  }

  @Override
  public Collection<Cell> getCells(Shape shape) {
    throw new UnsupportedOperationException("TODO unimplemented");//TODO
  }

  @Override
  public Cell getCell(double x, double y, int level) {
    throw new UnsupportedOperationException("TODO unimplemented");//TODO
  }

  @Override
  public Cell getCell(String token) {
    return new Cell(token) {

      @Override
      public Collection<Cell> getSubCells() {
        throw new UnsupportedOperationException("TODO unimplemented");//TODO
      }
    };
  }

}
