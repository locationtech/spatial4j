package org.apache.solr.spatial.demo.utils.countries;

import java.io.File;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.spatial.demo.utils.shapefile.ShapeReader;
import org.geotools.data.FeatureReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

public class StateReader extends BasicReader<BasicInfo>
{
  @Override
  public BasicInfo read( SimpleFeature f )
  {
    BasicInfo c = new BasicInfo();
    c.geometry = (Geometry)f.getAttribute(0);
    c.name = (String)f.getAttribute( 1 );
    c.fips = (String)f.getAttribute( 2 );
    c.population2005 = (Integer)f.getAttribute( 6 );


//    0] the_geom :: class com.vividsolutions.jts.geom.MultiPolygon
//    1] STATE_NAME :: class java.lang.String
//    2] STATE_FIPS :: class java.lang.String
//    3] SUB_REGION :: class java.lang.String
//    4] STATE_ABBR :: class java.lang.String
//    5] POP2000 :: class java.lang.Integer
//    6] POP2005 :: class java.lang.Integer
//    7] POP00_SQMI :: class java.lang.Double
//    8] POP05_SQMI :: class java.lang.Double
//    9] WHITE :: class java.lang.Integer
//    10] BLACK :: class java.lang.Integer
//    11] AMERI_ES :: class java.lang.Integer
//    12] ASIAN :: class java.lang.Integer
//    13] HAWN_PI :: class java.lang.Integer
//    14] OTHER :: class java.lang.Integer
//    15] MULT_RACE :: class java.lang.Integer
//    16] HISPANIC :: class java.lang.Integer
//    17] MALES :: class java.lang.Integer
//    18] FEMALES :: class java.lang.Integer
//    19] AGE_UNDER5 :: class java.lang.Integer
//    20] AGE_5_17 :: class java.lang.Integer
//    21] AGE_18_21 :: class java.lang.Integer
//    22] AGE_22_29 :: class java.lang.Integer
//    23] AGE_30_39 :: class java.lang.Integer
//    24] AGE_40_49 :: class java.lang.Integer
//    25] AGE_50_64 :: class java.lang.Integer
//    26] AGE_65_UP :: class java.lang.Integer
//    27] MED_AGE :: class java.lang.Double
//    28] MED_AGE_M :: class java.lang.Double
//    29] MED_AGE_F :: class java.lang.Double
//    30] HOUSEHOLDS :: class java.lang.Integer
//    31] AVE_HH_SZ :: class java.lang.Double
//    32] HSEHLD_1_M :: class java.lang.Integer
//    33] HSEHLD_1_F :: class java.lang.Integer
//    34] MARHH_CHD :: class java.lang.Integer
//    35] MARHH_NO_C :: class java.lang.Integer
//    36] MHH_CHILD :: class java.lang.Integer
//    37] FHH_CHILD :: class java.lang.Integer
//    38] FAMILIES :: class java.lang.Integer
//    39] AVE_FAM_SZ :: class java.lang.Double
//    40] HSE_UNITS :: class java.lang.Integer
//    41] VACANT :: class java.lang.Integer
//    42] OWNER_OCC :: class java.lang.Integer
//    43] RENTER_OCC :: class java.lang.Integer
//    44] NO_FARMS97 :: class java.lang.Long
//    45] AVG_SIZE97 :: class java.lang.Long
//    46] CROP_ACR97 :: class java.lang.Long
//    47] AVG_SALE97 :: class java.lang.Double
//    48] SQMI :: class java.lang.Integer
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
      BasicInfo c = read( iter.next() );
      SolrInputDocument doc = new SolrInputDocument();
      doc.setField( "id", c.fips );
      doc.setField( "name", c.name );
      doc.setField( "geo", c.geometry.toText() );
      doc.setField( "pop2005", c.population2005 );
      doc.setField( "source", "state" );
      solr.add( doc );
      System.out.println( (++count)+"/"+total + " :: " +c.name );
    }
  }
}
