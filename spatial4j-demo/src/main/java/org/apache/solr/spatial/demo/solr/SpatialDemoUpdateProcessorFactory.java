package org.apache.solr.spatial.demo.solr;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.shape.Shape;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

import java.io.IOException;
import java.util.Collections;


public class SpatialDemoUpdateProcessorFactory extends UpdateRequestProcessorFactory
{
  private SpatialContext ctx;

  private String sourceFieldName;

  @Override
  public void init(NamedList args)
  {
    sourceFieldName = (String) args.get("shapeField");
  }

  @Override
  public DemoUpdateProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next)
  {
    if (ctx == null) {
      ctx = SpatialContextFactory.makeSpatialContext(Collections.<String, String>emptyMap(), req.getCore().getResourceLoader().getClassLoader());
    }
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
      SolrInputField f = cmd.solrDoc.get( sourceFieldName );
      if( f != null ) {
        if( f.getValueCount() > 1 ) {
          throw new RuntimeException( "multiple values found for 'geometry' field: "+f.getValue() );
        }
        if( !(f.getValue() instanceof Shape) ) {
          Shape shape = ctx.readShape( f.getValue().toString() );
          f.setValue( shape, f.getBoost() );
        }
      }
      super.processAdd(cmd);
    }
  }
}
