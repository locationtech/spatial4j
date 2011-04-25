package org.apache.lucene.spatial.strategy;

public class SpatialMatchConcerns {
  public final boolean orderIsImportant;
  public final boolean resultsAreSuperset; // if the strategy can not give exact answers, but used to limit results

  private SpatialMatchConcerns( boolean order, boolean superset ) {
    this.orderIsImportant = order;
    this.resultsAreSuperset = superset;
  }

  public static final SpatialMatchConcerns EXACT = new SpatialMatchConcerns( true, false );
  public static final SpatialMatchConcerns FILTER = new SpatialMatchConcerns( false, false );
  public static final SpatialMatchConcerns SUPERSET = new SpatialMatchConcerns( false, true );
}
