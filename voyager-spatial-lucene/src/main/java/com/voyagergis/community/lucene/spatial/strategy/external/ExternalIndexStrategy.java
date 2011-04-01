package com.voyagergis.community.lucene.spatial.strategy.external;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.SpatialStrategy;

public class ExternalIndexStrategy extends SpatialStrategy<SimpleSpatialFieldInfo> {

  private final Map<String, ExternalSpatialIndexProvider> provider = new ConcurrentHashMap<String, ExternalSpatialIndexProvider>();
  private final SpatialContext reader;

  public ExternalIndexStrategy(SpatialContext reader) {
    this.reader = reader;
  }

  @Override
  public Fieldable createField(SimpleSpatialFieldInfo fieldInfo, Shape shape, boolean index, boolean store) {
    BBox bbox = shape.getBoundingBox();
    if (bbox.getCrossesDateLine()) {
      throw new RuntimeException(this.getClass() + " does not support BBox crossing the date line");
    }
    String v = reader.toString(bbox);

    return new Field(fieldInfo.getFieldName(), v, store ? Field.Store.YES : Field.Store.NO, index ? Field.Index.NOT_ANALYZED : Field.Index.NO);
  }

  @Override
  public ValueSource makeValueSource(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Query makeQuery(SpatialArgs args, SimpleSpatialFieldInfo field) {
    Filter f = makeFilter(args, field);
    // TODO... could add in scoring here..
    return new ConstantScoreQuery( f );
  }

  @Override
  public Filter makeFilter(SpatialArgs args, SimpleSpatialFieldInfo field) {
    if (args.getShape().getBoundingBox().getCrossesDateLine()) {
      throw new UnsupportedOperationException("Spatial Index does not (yet) support queries that cross the date line");
    }

    String name = field.getFieldName();
    ExternalSpatialIndexProvider p = provider.get(name);
    if (p == null) {
      p = new STRTreeIndexProvider(30, name, reader);
      provider.put(name, p);
    }

    // just a filter wrapper for now...
    return new ExternalSpatialIndexFilter(p, args);
  }
}