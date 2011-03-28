package org.apache.lucene.spatial.search.index;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.ShapeIO;
import org.apache.lucene.spatial.base.shape.jts.JTSShapeIO;
import org.apache.lucene.spatial.search.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.search.SpatialIndexer;

public class IndexSpatialIndexer implements SpatialIndexer<SimpleSpatialFieldInfo> {

  private ShapeIO shapeIO;

  public IndexSpatialIndexer(ShapeIO shapeIO) {
    this.shapeIO = shapeIO;
  }

  public Fieldable[] createFields(SimpleSpatialFieldInfo fieldInfo, Shape shape, boolean index, boolean store) {
    BBox bbox = shape.getBoundingBox();
    if (bbox.getCrossesDateLine()) {
      throw new RuntimeException(this.getClass() + " does not support BBox crossing the date line");
    }
    String v = shapeIO.toString(bbox);

    Field f = new Field(fieldInfo.getFieldName(), v, store ? Field.Store.YES : Field.Store.NO, index ? Field.Index.NOT_ANALYZED : Field.Index.NO);
    return new Fieldable[]{f};
  }
}
