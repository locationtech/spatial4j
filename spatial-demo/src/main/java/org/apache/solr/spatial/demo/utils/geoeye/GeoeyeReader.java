package org.apache.solr.spatial.demo.utils.geoeye;

import java.io.File;
import java.util.Date;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.spatial.demo.utils.shapefile.ShapeReader;
import org.geotools.data.FeatureReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

public class GeoeyeReader
{
  public static GeoeyeEntry read( SimpleFeature f )
  {
    GeoeyeEntry c = new GeoeyeEntry();
    c.geometry = (Geometry)f.getAttribute(0);
    c.imageID = (String)f.getAttribute( 1 );
    c.source = (String)f.getAttribute( 4 );
    c.sensorMode = (String)f.getAttribute( 5 );
    c.stripID = (String)f.getAttribute( 6 );
    c.sceneID = (String)f.getAttribute( 7 );
    c.collectionDate = (Date)f.getAttribute( 8 );
    c.imageURL = (String)f.getAttribute( 31 );
    c.metadataURL = (String)f.getAttribute( 33 );
//    c.longName = (String)f.getAttribute( 6 );
//    c.fips = (String)f.getAttribute( 1 );
//    c.status = (String)f.getAttribute( 12 );
//    c.sqKM = (Double)f.getAttribute( 14 );
//    c.sqMI = (Double)f.getAttribute( 15 );
//    c.population2005 = (Long)f.getAttribute( 13 );


//0] the_geom :: class com.vividsolutions.jts.geom.MultiPolygon
//1] IMAGE_ID :: class java.lang.String
//2] ORDER_ID :: class java.lang.String
//3] SOURCE_ABR :: class java.lang.String
//4] SOURCE :: class java.lang.String
//5] SENS_MODE :: class java.lang.String
//6] STRIP_ID :: class java.lang.String
//7] SCENE_ID :: class java.lang.String
//8] COLL_DATE :: class java.util.Date
//9] MONTH :: class java.lang.Long
//10] YEAR :: class java.lang.Long
//11] GSD :: class java.lang.Double
//12] SQKM :: class java.lang.Long
//13] SPATIALREF :: class java.lang.String
//14] RANKING :: class java.lang.Long
//15] ELEV_ANGLE :: class java.lang.Double
//16] AZIM_ANGLE :: class java.lang.Double
//17] CLOUDS :: class java.lang.Long
//18] SUN_ELEV :: class java.lang.Double
//19] SUN_ANGLE :: class java.lang.Double
//20] STEREO_ID :: class java.lang.String
//21] DATA_OWNER :: class java.lang.String
//22] UL_LAT :: class java.lang.Double
//23] UL_LON :: class java.lang.Double
//24] UR_LAT :: class java.lang.Double
//25] UR_LON :: class java.lang.Double
//26] LL_LAT :: class java.lang.Double
//27] LL_LON :: class java.lang.Double
//28] LR_LAT :: class java.lang.Double
//29] LR_LON :: class java.lang.Double
//30] GEORECTIFY :: class java.lang.Long
//31] IMAGE_URL :: class java.lang.String
//32] WORLD_URL :: class java.lang.String
//33] METADATA :: class java.lang.String
//34] PRODUCT :: class java.lang.String

    return c;
  }

  public static void indexItems( SolrServer solr, File shp ) throws Exception
  {
    ShapeReader reader = new ShapeReader( shp );
    int total = reader.getCount();
    reader.describe( System.out );
    int count = 0;
    FeatureReader<SimpleFeatureType, SimpleFeature> iter = reader.getFeatures();
    while( iter.hasNext() ) {
      GeoeyeEntry c = read( iter.next() );
      SolrInputDocument doc = new SolrInputDocument();
      doc.setField( "id", c.imageID );
      doc.setField( "name", c.imageID );
      doc.setField( "geo", c.geometry.toText() );

      doc.setField( "sourceC", c.source ); // source taken...
      doc.setField( "sensorMode", c.sensorMode );
      doc.setField( "stripID", c.stripID );
      doc.setField( "sceneID", c.sceneID );
      doc.setField( "collectionDate", c.collectionDate );
      doc.setField( "imageURL", c.imageURL );
      doc.setField( "metadataURL", c.metadataURL );

      doc.setField( "source", "geoeye" );

      solr.add( doc );
      System.out.println( (++count)+"/"+total + " :: " +c.imageID );
    }
  }
}
