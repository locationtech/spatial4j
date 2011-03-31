package org.apache.lucene.spatial.strategy.util;

import java.io.IOException;

import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredDocIdSet;
import org.apache.lucene.search.function.DocValues;
import org.apache.lucene.search.function.ValueSource;

public class ValueSourceFilter extends Filter {

  final Filter startingFilter;
  final ValueSource source;
  final double min;
  final double max;
  
  public ValueSourceFilter( Filter startingFilter, ValueSource source, double min, double max )
  {
    if (startingFilter == null) {
      throw new IllegalArgumentException("please provide a non-null startingFilter; you can use QueryWrapperFilter(MatchAllDocsQuery) as a no-op filter");
    }
    this.startingFilter = startingFilter;
    this.source = source;
    this.min = min;
    this.max = max;
  }
  
  @Override
  public DocIdSet getDocIdSet(AtomicReaderContext context) throws IOException {
    final DocValues values = source.getValues( context );
    return new FilteredDocIdSet(startingFilter.getDocIdSet(context)) {
      @Override
      public boolean match(int doc) {
        double val = values.doubleVal( doc );
        return val > min && val < max;
      }
    };
  }
}
