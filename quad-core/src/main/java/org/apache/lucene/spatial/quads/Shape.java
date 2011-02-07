package org.apache.lucene.spatial.quads;

public interface Shape
{
  /**
   * Describe the relationship between the two objects.  For example
   *
   *   this is WITHIN other
   *   this CONTAINS other
   *   this is OUTSIDE other
   *   this INTERSECTS other
   *
   * The grid is useful for context -- it may include spatial reference
   */
  IntersectCase intersect( Shape other, SpatialGrid grid );

  ShapeExtent getExtent();
}
