package org.apache.lucene.spatial.search.index;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.ShapeIO;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.spatial.search.SpatialQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpatialIndexQueryBuilder implements SpatialQueryBuilder<String>
{
  private static Logger log = LoggerFactory.getLogger(SpatialIndexQueryBuilder.class);

  final Map<String, SpatialIndexProvider> provider
    = new ConcurrentHashMap<String, SpatialIndexProvider>();
  public final ShapeIO reader;

  public SpatialIndexQueryBuilder( ShapeIO reader )
  {
    this.reader = reader;
  }


  @Override
  public ValueSource makeValueSource(SpatialArgs args, String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Query makeQuery(SpatialArgs args, String name)
  {
    if(args.getShape().getBoundingBox().getCrossesDateLine()) {
      throw new UnsupportedOperationException( "Spatial Index does not (yet) support queries that cross the date line" );
    }

    SpatialIndexProvider p = provider.get( name );
    if( p == null ) {
      p = new STRTreeIndexProvider( 30, name, reader );
      provider.put( name, p );
    }

    // just a filter wrapper for now...
    SpatialIndexFilter filter = new SpatialIndexFilter( p, args );
    return new FilteredQuery( new MatchAllDocsQuery(), filter );
  }

  @Override
  public Fieldable[] createFields(String name, Shape shape, boolean index, boolean store )
  {
    BBox bbox = shape.getBoundingBox();
    if( bbox.getCrossesDateLine() ) {
      throw new RuntimeException( this.getClass() + " does not support BBox crossing the date line" );
    }
    String v = reader.toString( bbox );

    Field f = new Field( name, v, store?Store.YES:Store.NO, index?Index.NOT_ANALYZED:Index.NO );
    return new Fieldable[] { f };
  }

}