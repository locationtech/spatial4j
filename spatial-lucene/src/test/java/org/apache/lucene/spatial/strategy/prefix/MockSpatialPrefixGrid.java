package org.apache.lucene.spatial.strategy.prefix;

import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.shape.Shape;

import java.util.List;

/**
 * @author Chris Male
 */
public class MockSpatialPrefixGrid implements SpatialPrefixGrid {

  public List<String> readCells(Shape geo) {
    return null;
  }

  public int getBestLevel(Shape geo) {
    return 0;
  }

  public Shape getCellShape(String seq) {
    return null;
  }
}
