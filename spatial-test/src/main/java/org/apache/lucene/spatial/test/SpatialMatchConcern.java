package org.apache.lucene.spatial.test;

public class SpatialMatchConcern {
  public final boolean orderIsImportant;
  public final boolean resultsAreSuperset; // if the strategy can not give exact answers, but used to limit results

  private SpatialMatchConcern( boolean order, boolean superset ) {
    this.orderIsImportant = order;
    this.resultsAreSuperset = superset;
  }

  public static final SpatialMatchConcern EXACT = new SpatialMatchConcern( true, false );
  public static final SpatialMatchConcern FILTER = new SpatialMatchConcern( false, false );
  public static final SpatialMatchConcern SUPERSET = new SpatialMatchConcern( false, true );
}
