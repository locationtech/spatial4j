package org.apache.lucene.spatial.strategy.prefix;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Chris Male
 */
public class TestSimplePrefixGridSimilarity {

  @Test
  public void testSimplePrefixGridSimilarity() {
    PrefixGridSimilarity.SimplePrefixGridSimilarity gridSimilarity = new PrefixGridSimilarity.SimplePrefixGridSimilarity();
    assertEquals(1F, gridSimilarity.scoreGridSearch(3, 3), 0F);
    assertEquals(0.25, gridSimilarity.scoreGridSearch(3, 2), 0F);
    assertEquals(0.25, gridSimilarity.scoreGridSearch(2, 3), 0F);
    assertEquals(0.05, gridSimilarity.scoreGridSearch(1, 6), 0.001F);
  }
}
