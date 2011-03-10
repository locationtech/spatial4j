package org.apache.solr.spatial.demo.utils.geonames;

import java.sql.Date;

public class Geoname
{
  public int id;
  public String name; // name of geographical point (utf8) varchar(200)
  public String nameASCII; // name of geographical point in plain ascii characters, varchar(200)
  public String[] alternateNames; // alternatenames, comma separated varchar(5000)
  public double latitude;
  public double longitude;
  public char featureClass;
  public String featureCode; // 10
  public String countryCode; // 2
  public String[] countryCode2; // alternate country codes, comma separated, ISO-3166 2-letter country code, 60 characters
  public String adminCode1; // fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
  public String adminCode2; // code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80)
  public String adminCode3; // code for third level administrative division, varchar(20)
  public String adminCode4; // code for fourth level administrative division, varchar(20)
  public Long population;
  public Integer elevation; // in meters, integer
  public Integer gtopo30;   // average elevation of 30'x30' (ca 900mx900m) area in meters, integer
  public String timezone;
  public Date modified;  // date of last modification in yyyy-MM-dd format

  public static Geoname parse( String line )
  {
    String[] vals = line.split( "\t" );
    Geoname g = new Geoname();
    g.id = Integer.parseInt( vals[0] );
    g.name = vals[1];
    g.nameASCII = vals[2];
    g.alternateNames = vals[3].split( "," );
    g.latitude = Double.parseDouble( vals[4] );
    g.longitude = Double.parseDouble( vals[5] );
    g.featureClass = vals[6].length() > 0 ? vals[6].charAt(0) : 'S';
    g.featureCode = vals[7];
    g.countryCode = vals[8];
    g.countryCode2 = vals[9].split( "," );
    g.adminCode1 = vals[10];
    g.adminCode2 = vals[11];
    g.adminCode3 = vals[12];
    g.adminCode4 = vals[13];
    if( vals[14].length() > 0 ) {
      g.population = Long.decode( vals[14] );
    }
    if( vals[15].length() > 0 ) {
      g.elevation = Integer.decode( vals[15] );
    }
    if( vals[16].length() > 0 ) {
      g.gtopo30 = Integer.decode( vals[16] );
    }
    g.timezone = vals[17];
    if( vals[18].length() > 0 ) {
      g.modified = Date.valueOf( vals[18] );
    }
    return g;
  }
}
