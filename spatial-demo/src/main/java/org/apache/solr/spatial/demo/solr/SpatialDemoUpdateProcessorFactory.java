package org.apache.solr.spatial.demo.solr;

import java.io.IOException;

import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.ShapeIO;
import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;


public class SpatialDemoUpdateProcessorFactory extends UpdateRequestProcessorFactory
{
  final ShapeIO reader = new JtsShapeIO();

  @Override
  public DemoUpdateProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next)
  {
    return new DemoUpdateProcessor(next);
  }


  class DemoUpdateProcessor extends UpdateRequestProcessor
  {
    public DemoUpdateProcessor(UpdateRequestProcessor next) {
      super(next);
    }

    @Override
    public void processAdd(AddUpdateCommand cmd) throws IOException
    {
      // This converts the 'geo' field to a shape once and will let the standard CopyField copy to relevant fields
      SolrInputField f = cmd.solrDoc.get( "geo" );
      if( f != null ) {
        if( f.getValueCount() > 1 ) {
          throw new RuntimeException( "multiple values found for 'geometry' field: "+f.getValue() );
        }
        if( !(f.getValue() instanceof Shape) ) {
          Shape shape = reader.readShape( f.getValue().toString() );
          f.setValue( shape, f.getBoost() );
        }
      }
      super.processAdd(cmd);
    }
  }
}
