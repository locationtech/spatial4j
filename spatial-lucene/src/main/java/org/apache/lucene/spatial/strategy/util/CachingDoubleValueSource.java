package org.apache.lucene.spatial.strategy.util;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.search.function.DocValues;
import org.apache.lucene.search.function.ValueSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CachingDoubleValueSource extends ValueSource {

  final ValueSource source;
  final Map<Integer, Double> cache;

  public CachingDoubleValueSource( ValueSource source )
  {
    this.source = source;
    cache = new HashMap<Integer, Double>();
  }

  @Override
  public String description() {
    return "Cached["+source.description()+"]";
  }

  @Override
  public DocValues getValues(AtomicReaderContext context) throws IOException {
    final int base = context.docBase;
    final DocValues vals = source.getValues(context);
    return new DocValues() {

      @Override
      public double doubleVal(int doc) {
        Integer key = Integer.valueOf( base+doc );
        Double v = cache.get( key );
        if( v == null ) {
          v = Double.valueOf( vals.doubleVal(doc) );
          cache.put( key, v );
        }
        return v.doubleValue();
      }

      @Override
      public float floatVal(int doc) {
        return (float)doubleVal(doc);
      }

      @Override
      public String toString(int doc) {
        return doubleVal(doc)+"";
      }
    };
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) { return false; }
    if (obj == this) { return true; }
    if (obj.getClass() != getClass()) {
      return false;
    }
    CachingDoubleValueSource rhs = (CachingDoubleValueSource) obj;
    return source.equals( rhs.source );
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(61, 23).
      append(source).
      toHashCode();
  }
}
