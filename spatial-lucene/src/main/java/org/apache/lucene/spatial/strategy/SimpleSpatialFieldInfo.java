package org.apache.lucene.spatial.search;

/**
 * @author Chris Male
 */
public class SimpleSpatialFieldInfo implements SpatialFieldInfo {

  private final String fieldName;

  public SimpleSpatialFieldInfo(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getFieldName() {
    return fieldName;
  }
}
