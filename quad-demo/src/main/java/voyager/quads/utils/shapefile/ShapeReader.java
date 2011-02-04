package voyager.quads.utils.shapefile;


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.filter.Filter;


public class ShapeReader
{
  final ShapefileDataStore store;

  public ShapeReader( File f ) throws IOException
  {
    store = new ShapefileDataStore ( f.toURL() );
  }

  public SimpleFeatureType getSchema() throws IOException
  {
    return store.getSchema();
  }

  public void describe( PrintStream out ) throws IOException
  {
    SimpleFeatureType schema = store.getSchema();
    for( int i=0; i<schema.getAttributeCount(); i++ ) {
      AttributeDescriptor ad = schema.getDescriptor( i );
      AttributeType at = ad.getType();
      out.println( i+"] "+ad.getName() + " :: " + at.getBinding() );
    }
  }

  public int getCount() throws IOException
  {
    return store.getCount( new DefaultQuery() {
      @Override
      public Filter getFilter() {
        return Filter.INCLUDE;
      }
    });
  }

  public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatures() throws IOException
  {
    return store.getFeatureReader();
  }
}


