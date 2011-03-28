package org.apache.lucene.spatial.search.index;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.ShapeIO;
import org.apache.lucene.spatial.search.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.search.SpatialQueryBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpatialIndexQueryBuilder implements SpatialQueryBuilder<SimpleSpatialFieldInfo> {

  private final Map<String, SpatialIndexProvider> provider = new ConcurrentHashMap<String, SpatialIndexProvider>();
  private final ShapeIO reader;

  public SpatialIndexQueryBuilder(ShapeIO reader) {
    this.reader = reader;
  }

  @Override
  public ValueSource makeValueSource(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Query makeQuery(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo) {
    if (args.getShape().getBoundingBox().getCrossesDateLine()) {
      throw new UnsupportedOperationException("Spatial Index does not (yet) support queries that cross the date line");
    }

    String name = fieldInfo.getFieldName();
    SpatialIndexProvider p = provider.get(name);
    if (p == null) {
      p = new STRTreeIndexProvider(30, name, reader);
      provider.put(name, p);
    }

    // just a filter wrapper for now...
    SpatialIndexFilter filter = new SpatialIndexFilter(p, args);
    return new FilteredQuery(new MatchAllDocsQuery(), filter);
  }
}