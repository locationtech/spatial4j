package voyager.quads.utils.shapefile;


import java.io.File;
import java.io.IOException;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.filter.Filter;



/**
 */
public class ShapeReader
{
  final ShapefileDataStore store;

  public ShapeReader( File f ) throws IOException
  {
    store = new ShapefileDataStore ( f.toURL() );
  }


//  public void dispose() {
//    store.dispose();
//  }

  public SimpleFeatureType getSchema() throws IOException
  {
    return store.getSchema();
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

  public void read( FeatureVisitor visitor ) throws IOException
  {
    int idx = 0;
    FeatureReader<SimpleFeatureType, SimpleFeature> rrr = store.getFeatureReader();
    while( rrr.hasNext() ) {
      visitor.visit( rrr.next(), idx++ );
    }
  }


  public static SolrFieldMapper guess( SimpleFeatureType schema )
  {
    SolrFieldMapper mapper = new SolrFieldMapper();

    for( int i=0; i<schema.getAttributeCount(); i++ ) {
      AttributeDescriptor ad = schema.getDescriptor( i );
      AttributeType at = ad.getType();

      EntryFieldType t = EntryFieldType.forClass( at.getBinding() );
      String sfield = EntryUtils.getSolrFieldPrefix( t, ad.getMaxOccurs()>1 ) + ad.getName();

      mapper.register( new SolrFieldMapInfo( i, ad.getLocalName(), sfield ) );
    }

    return mapper;
  }


  public static void main( String[] args ) throws Exception {
    File dir = new File( "F:/workspace/lucene-spatial/data/" );
    File shp = new File( dir, "ikonos_2010/ikonos_2010.shp" );


    ShapeReader reader = new ShapeReader( shp );
    final float count = reader.getCount();
    reader.read( new FeatureVisitor() {
      @Override
      public void visit(SimpleFeature f, int idx) {
        // TODO Auto-generated method stub

        float per = idx/count;
        System.out.println( idx + " :: " + per );
      }
    });

    System.out.println( "done." );
  }

}


