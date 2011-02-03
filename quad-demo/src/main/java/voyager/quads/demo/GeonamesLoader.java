package voyager.quads.demo;

import java.io.File;
import java.io.IOException;

import voyager.quads.utils.geonames.Geoname;
import voyager.quads.utils.geonames.GeonamesReader;

public class GeonamesLoader
{
  public static void main( String[] args ) throws IOException
  {
   // File file = new File( "C:/Users/ryan/Downloads/US/US.txt" );
    File file = new File( "C:/Users/ryan/Downloads/MX/MX.txt" );
    GeonamesReader reader = new GeonamesReader( file );
    while( reader.hasNext() ) {
      Geoname name = reader.next();
      //System.out.println( name.name );

     // tree.insert( new Envelope( new Coordinate( name.longitude, name.latitude )), name.name );

      if( reader.getCount() > 10000 ) {
        break;
      }
    }

    System.out.println( "COUNT:"+reader.getCount() );



    File outfile = new File( "c:/temp/test.kml" );
//    Kml kml = KMLHelper.toKML( file.getName(), tree.getRoot() );
//    kml.marshal(outfile);
    System.out.println( "done." );


    System.out.println( "done." );
  }
}
