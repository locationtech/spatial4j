package org.apache.lucene.spatial.search;

public enum SpatialRelationship {
  WITHIN,
  CONTAINS,
  INTERSECTS,
  SIMILAR  // fuzzy geometry matching
};