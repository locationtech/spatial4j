package org.apache.lucene.spatial.strategy.util;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.NumericField;

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
    FieldType fieldType = new FieldType();
    fieldType.setStored(store);
    fieldType.setIndexed(index);
    fieldType.setOmitNorms(omitNorms);

    NumericField f = new NumericField(name, precisionStep, fieldType);
    f.setDoubleValue(v);
    return f;
  }
}
