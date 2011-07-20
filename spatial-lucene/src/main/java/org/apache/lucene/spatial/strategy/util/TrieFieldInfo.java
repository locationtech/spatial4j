package org.apache.lucene.spatial.strategy.util;

import org.apache.lucene.document.NumericField;
import org.apache.lucene.document.Field.Store;

/**
 * Hold some of the parameters used by solr...
 */
public class TrieFieldInfo {
  public int precisionStep = 8; // same as solr default
  public boolean store = true;
  public boolean index = true;
  public boolean omitNorms = true;
  public boolean omitTF = true;

  public void setPrecisionStep( int p ) {
    precisionStep = p;
    if (precisionStep<=0 || precisionStep>=64)
      precisionStep=Integer.MAX_VALUE;
  }

  public NumericField createDouble( String name, double v ) {
    NumericField f = new NumericField(
        name,
        precisionStep,
        store?Store.YES:Store.NO,
        index );
    f.setDoubleValue(v);
    f.setOmitNorms(omitNorms);
    return f;
  }
}
