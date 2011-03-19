package org.apache.solr.spatial;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.ShapeIO;
import org.apache.lucene.spatial.base.SpatialArgs;
import org.apache.lucene.document.Fieldable;
import org.apache.solr.common.SolrException;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.schema.*;
import org.apache.solr.search.QParser;
import java.io.IOException;

/**
 */
public abstract class SpatialFieldType extends FieldType 
{
  protected ShapeIO reader;
  

  @Override
  public Fieldable createField(SchemaField field, Object val, float boost)
  {
    Shape shape = (val instanceof Shape)?((Shape)val):reader.readShape( val.toString() );
    return createField(field, shape, boost);
  }

  @Override
  public Fieldable[] createFields(SchemaField field, Object val, float boost) 
  {
    Shape shape = (val instanceof Shape)?((Shape)val):reader.readShape( val.toString() );
    return createFields(field, shape, boost);
  }
  
  public abstract Fieldable createField(SchemaField field, Shape value, float boost);
  
  public Fieldable[] createFields(SchemaField field, Shape value, float boost) {
    Fieldable f = createField( field, value, boost);
    return f==null ? new Fieldable[]{} : new Fieldable[]{f};
  }
  

  @Override
  public Query getRangeQuery(QParser parser, SchemaField field, String part1, String part2, boolean minInclusive, boolean maxInclusive) {
    throw new UnsupportedOperationException();
  }

  @Override
  public final Query getFieldQuery(QParser parser, SchemaField field, String externalVal)
  {
    return getFieldQuery( parser, field, SpatialArgs.parse( externalVal, reader ) );
  }
  
  public abstract Query getFieldQuery(QParser parser, SchemaField field, SpatialArgs args );

  @Override
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    // TODO? WTB?
    writer.writeStr(name, f.stringValue(), false);
  }

  @Override
  public SortField getSortField(SchemaField field, boolean top) {
    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Sorting not supported on SpatialField: " + field.getName());
  }
}


