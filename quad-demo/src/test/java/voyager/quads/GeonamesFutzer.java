package voyager.quads;

import java.io.File;
import java.io.IOException;

import voyager.quads.strtree.ItemVisitor;
import voyager.quads.strtree.STRtree;
import voyager.quads.utils.KMLHelper;
import voyager.quads.utils.geonames.Geoname;
import voyager.quads.utils.geonames.GeonamesReader;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import de.micromata.opengis.kml.v_2_2_0.Kml;

public class GeonamesFutzer
{
  public static void main( String[] args ) throws IOException
  {
    STRtree tree = new STRtree( 10 );

   // File file = new File( "C:/Users/ryan/Downloads/US/US.txt" );
    File file = new File( "C:/Users/ryan/Downloads/MX/MX.txt" );
    GeonamesReader reader = new GeonamesReader( file );
    while( reader.hasNext() ) {
      Geoname name = reader.next();
      //System.out.println( name.name );

      tree.insert( new Envelope( new Coordinate( name.longitude, name.latitude )), name.name );

      if( reader.getCount() > 10000 ) {
        break;
      }
    }

    System.out.println( "COUNT:"+reader.getCount() );

    tree.query( new Envelope( -100, 120, -90, 95 ), new ItemVisitor() {
      @Override
      public void visitItem(String item) {
        System.out.println( "item: "+item );
      }
    });



    File outfile = new File( "c:/temp/test.kml" );
    Kml kml = KMLHelper.toKML( file.getName(), tree.getRoot() );
    kml.marshal(outfile);
    System.out.println( "done." );


    System.out.println( "done." );
  }
}
