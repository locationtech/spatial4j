package org.apache.lucene.spatial.strategy.util;

import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.queries.function.DocValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredDocIdSet;
import org.apache.lucene.util.Bits;

import java.io.IOException;

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
  public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs) throws IOException {
    final DocValues values = source.getValues( null, context );
    return new FilteredDocIdSet(startingFilter.getDocIdSet(context, acceptDocs)) {
      @Override
      public boolean match(int doc) {
        double val = values.doubleVal( doc );
        return val > min && val < max;
      }
    };
  }
}
