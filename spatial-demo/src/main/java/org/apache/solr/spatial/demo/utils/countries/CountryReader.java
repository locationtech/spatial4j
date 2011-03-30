package org.apache.solr.spatial.demo.utils.countries;

import java.io.File;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.spatial.demo.utils.shapefile.ShapeReader;
import org.geotools.data.FeatureReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

public class CountryReader extends BasicReader<CountryInfo>
{
  @Override
  public CountryInfo read( SimpleFeature f )
  {
    CountryInfo c = new CountryInfo();
    c.geometry = (Geometry)f.getAttribute(0);
    c.id = (String)f.getAttribute( 2 ); // GMI_CNTRY
    c.name = (String)f.getAttribute( 6 );
    c.status = (String)f.getAttribute( 12 );
    c.sqKM = (Double)f.getAttribute( 14 );
    c.sqMI = (Double)f.getAttribute( 15 );

    Long v = (Long)f.getAttribute( 13 );
    if( v != null ) {
      c.population2005 = v.intValue();
    }

    System.out.println( c.id + " :: " + c.name );

    //0] the_geom :: class com.vividsolutions.jts.geom.MultiPolygon
    //1] FIPS_CNTRY :: class java.lang.String
    //2] GMI_CNTRY :: class java.lang.String
    //3] ISO_2DIGIT :: class java.lang.String
    //4] ISO_3DIGIT :: class java.lang.String
    //5] ISO_NUM :: class java.lang.Integer
    //6] CNTRY_NAME :: class java.lang.String
    //7] LONG_NAME :: class java.lang.String
    //8] ISOSHRTNAM :: class java.lang.String
    //9] UNSHRTNAM :: class java.lang.String
    //10] LOCSHRTNAM :: class java.lang.String
    //11] LOCLNGNAM :: class java.lang.String
    //12] STATUS :: class java.lang.String
    //13] POP2005 :: class java.lang.Long
    //14] SQKM :: class java.lang.Double
    //15] SQMI :: class java.lang.Double
    //16] COLORMAP :: class java.lang.Integer

    return c;
  }

  @Override
  public void index( SolrServer solr, File shp ) throws Exception
  {
    ShapeReader reader = new ShapeReader( shp );
    int total = reader.getCount();
    reader.describe( System.out );
    int count = 0;
    FeatureReader<SimpleFeatureType, SimpleFeature> iter = reader.getFeatures();
    while( iter.hasNext() ) {
      CountryInfo c = read( iter.next() );
      SolrInputDocument doc = new SolrInputDocument();
      doc.setField( "id", c.id );
      doc.setField( "name", c.name );
      doc.setField( "geo", c.geometry.toText() );
      doc.setField( "pop2005", c.population2005 );
      doc.setField( "source", "countries" );
      solr.add( doc );
      System.out.println( (++count)+"/"+total + " :: " +c.name );
    }
  }
}
