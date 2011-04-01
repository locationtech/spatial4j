package org.apache.lucene.spatial.strategy.prefix;

/**
 * @author Chris Male
 */
public interface PrefixGridSimilarity {

  float scoreGridSearch(int bestResolution, int matchLength);

  // ================================================= Inner Classes =================================================

  public static class SimplePrefixGridSimilarity implements PrefixGridSimilarity {

    public float scoreGridSearch(int bestResolution, int matchLength) {
      int lengthDifference = Math.abs(bestResolution - matchLength);
      return (lengthDifference != 0) ? 1F / (float) lengthDifference : 1;
    }
  }
}
