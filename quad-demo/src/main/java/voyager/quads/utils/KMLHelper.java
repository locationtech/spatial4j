package voyager.quads.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import voyager.quads.LevelMatchInfo;
import voyager.quads.MatchInfo;
import voyager.quads.SpatialGrid;
import voyager.quads.geometry.GeometryShape;
import voyager.quads.geometry.Shape;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import de.micromata.opengis.kml.v_2_2_0.ColorMode;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LineStyle;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.PolyStyle;
import de.micromata.opengis.kml.v_2_2_0.StyleMap;
import de.micromata.opengis.kml.v_2_2_0.StyleState;

public class KMLHelper
{
  public static void addStyles( Document document )
  {
    // Intersect
    document.createAndAddStyle().withId( "IntersectStyleNorm" )
      .withPolyStyle( new PolyStyle().withColor( "44787878" ).withColorMode( ColorMode.NORMAL ) )
      .withLineStyle( new LineStyle().withColor( "99FFFFFF" ).withColorMode( ColorMode.NORMAL ).withWidth( 3 ) );
    document.createAndAddStyle().withId( "IntersectStyleHI" )
      .withPolyStyle( new PolyStyle().withColor( "88787878" ).withColorMode( ColorMode.NORMAL ) )
      .withLineStyle( new LineStyle().withColor( "BB1400FF" ).withColorMode( ColorMode.NORMAL ).withWidth( 3 ) );
    StyleMap sm = document.createAndAddStyleMap().withId( "iii" );
    sm.createAndAddPair().withKey( StyleState.NORMAL ).withStyleUrl( "#IntersectStyleNorm" );
    sm.createAndAddPair().withKey( StyleState.HIGHLIGHT ).withStyleUrl( "#IntersectStyleHI" );

    // Covers
    document.createAndAddStyle().withId( "CoversStyleNorm" )
      .withPolyStyle( new PolyStyle().withColor( "993CDC14" ).withColorMode( ColorMode.NORMAL ) )
      .withLineStyle( new LineStyle().withColor( "99FFFFFF" ).withColorMode( ColorMode.NORMAL ).withWidth( 3 ) );
    document.createAndAddStyle().withId( "CoversStyleHI" )
      .withPolyStyle( new PolyStyle().withColor( "BB3CDC14" ).withColorMode( ColorMode.NORMAL ) )
      .withLineStyle( new LineStyle().withColor( "BB1400FF" ).withColorMode( ColorMode.NORMAL ).withWidth( 3 ) );
    sm = document.createAndAddStyleMap().withId( "ccc" );
    sm.createAndAddPair().withKey( StyleState.NORMAL ).withStyleUrl( "#CoversStyleNorm" );
    sm.createAndAddPair().withKey( StyleState.HIGHLIGHT ).withStyleUrl( "#CoversStyleHI" );

    // Covers
    document.createAndAddStyle().withId( "MaxStyleNorm" )
      .withPolyStyle( new PolyStyle().withColor( "991400E6" ).withColorMode( ColorMode.RANDOM ) )
      .withLineStyle( new LineStyle().withColor( "99FFFFFF" ).withColorMode( ColorMode.NORMAL ).withWidth( 3 ) );
    document.createAndAddStyle().withId( "MaxStyleHI" )
      .withPolyStyle( new PolyStyle().withColor( "BB1400E6" ).withColorMode( ColorMode.RANDOM ) )
      .withLineStyle( new LineStyle().withColor( "BB1400FF" ).withColorMode( ColorMode.NORMAL ).withWidth( 3 ) );
    sm = document.createAndAddStyleMap().withId( "mmm" );
    sm.createAndAddPair().withKey( StyleState.NORMAL ).withStyleUrl( "#MaxStyleNorm" );
    sm.createAndAddPair().withKey( StyleState.HIGHLIGHT ).withStyleUrl( "#MaxStyleHI" );
  }

  private static List<Coordinate> getCoords( Envelope r )
  {
    List<Coordinate> coords = new ArrayList<Coordinate>(5);
    coords.add( new Coordinate( r.getMinX(),r.getMinY() ) );
    coords.add( new Coordinate( r.getMaxX(),r.getMinY() ) );
    coords.add( new Coordinate( r.getMaxX(),r.getMaxY() ) );
    coords.add( new Coordinate( r.getMinX(),r.getMaxY() ) );
    coords.add( new Coordinate( r.getMinX(),r.getMinY() ) );
    return coords;
  }

  private static Placemark create( String key, String style, SpatialGrid grid )
  {
    Envelope r = grid.getRectangle( key );
    List<Coordinate> coords = getCoords( r );

    Placemark p = new Placemark().withName( key )
      .withDescription( r.toString() )
      .withStyleUrl( style );

    p.createAndSetPolygon()
        .withTessellate( true )
        .createAndSetOuterBoundaryIs()
          .createAndSetLinearRing().withCoordinates( coords );

    return p;
  }

  public static Kml toKML(String name, SpatialGrid grid, List<String> tokens )
  {
    final Kml kml = KmlFactory.createKml();
    Document document = kml.createAndSetDocument()
      .withName( name ).withOpen(false);

    document.withDescription( tokens.size()+"" );

    addStyles( document );

    Folder folder = document.createAndAddFolder().withName( "tokens" );
    for( String token : tokens ) {
      String style = token.endsWith( "*" ) ? "#ccc" : "#mmm";
      folder.getFeature().add( create( token.substring(0,token.length()-1), style, grid ) );
    }
    return kml;
  }

  public static Kml toKML( MatchInfo info, String name, SpatialGrid grid, boolean showIntersects )
  {
    final Kml kml = KmlFactory.createKml();
    Document document = kml.createAndSetDocument()
      .withName( name ).withOpen(false);

    document.withDescription( info.getInfoString() );

    addStyles( document );

    for( LevelMatchInfo level : info.levels ) {
      if( level == null )
        continue;

      boolean addit = false;
      Folder folder = new Folder().withName( "Level "+level.level ).withOpen( false );
      if( showIntersects ) {
        for( String c : level.intersects ) {
          folder.getFeature().add( create( c, "#iii", grid ) );
          addit = true;
        }
      }
      for( String c : level.depth ) {
        folder.getFeature().add( create( c, "#mmm", grid ) );
        addit = true;
      }
      for( String c : level.covers ) {
        folder.getFeature().add( create( c, "#ccc", grid ) );
        addit = true;
      }
      if( addit ) {
//        document.createAndAddFolder().withName( "XXX"+level );
        document.getFeature().add(folder);
      }
    }

    return kml;
  }

  public static void main( String[] args ) throws Exception
  {
    SpatialGrid grid = new SpatialGrid( 0, 10, 0, 10, 10 );
    grid = new SpatialGrid( -180, 180, -90-180, 90, 16 ); // make it like WGS84


//    CRSCache cache = new CRSCache();
//    CoordinateReferenceSystem crs = cache.createFromName( "epsg:3785" ) ; //"EPSG:4326" ); //epsg:3785" );
//    System.out.println();
//    System.out.println( crs.getName() );

//    Projection projection = new CylindricalEqualAreaProjection();
//    grid = SpatialGridWithProjection.create( projection, 16 );
//
    grid.resolution = 4; // how far past the best fit to go

    grid.printInfo();


    Shape shape = null;


//    shape = new PointRadiusIndexible( 5,5, 5 );
//
//    shape = JtsGeom.parseGeometry( "MULTIPOINT((3.5 5.6), (4.8 9.5))" );
//    shape = JtsGeom.parseGeometry( "POLYGON((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2))" );
//    shape = JtsGeom.parseGeometry( "LINESTRING(3 4,5 7,8 2)" );


//    shape = new JTSIndexible( "POINT(6 8)" ); // very small display!
//    shape = new JTSIndexible( "POINT(3.5 5.6)" );

    GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre( new com.vividsolutions.jts.geom.Coordinate( 4,2 ) );
    gsf.setSize( 4 );
    shape = new GeometryShape( gsf.createCircle() );

    // USA -- http://openlayers.org/dev/examples/vector-formats.html
    shape = Shape.parse( "POLYGON((-125.22656679154 49.089853763581, -123.99609804154 46.453135013581, -123.99609804154 44.343760013581, -124.34766054154 42.585947513581, -124.17187929154 40.828135013581, -123.29297304154 39.246103763581, -122.76562929154 36.960947513581, -121.18359804154 34.851572513581, -118.89844179154 34.324228763581, -117.66797304154 33.093760013581, -114.85547304154 33.093760013581, -111.51562929154 31.687510013581, -108.52734804154 31.863291263581, -105.71484804154 31.511728763581, -104.13281679154 29.578135013581, -101.14453554154 29.929697513581, -100.44141054154 27.644541263581, -97.277348041542 25.886728763581, -96.046879291542 28.171885013581, -93.585941791542 29.402353763581, -90.421879291542 29.402353763581, -88.488285541542 29.050791263581, -88.312504291542 30.281260013581, -83.390629291542 29.929697513581, -82.160160541542 26.765635013581, -80.226566791542 24.832041263581, -79.171879291542 27.292978763581, -80.929691791542 31.511728763581, -75.304691791542 35.554697513581, -75.304691791542 37.664072513581, -72.316410541542 41.179697513581, -69.328129291542 41.882822513581, -69.855473041542 44.167978763581, -65.988285541542 45.046885013581, -67.921879291542 48.562510013581, -70.382816791542 47.507822513581, -70.910160541542 45.925791263581, -76.710941791542 45.222666263581, -79.523441791542 44.519541263581, -82.687504291542 46.277353763581, -84.972660541542 46.804697513581, -89.894535541542 47.859385013581, -95.167973041542 49.089853763581, -125.22656679154 49.089853763581))" );

//    // NZ
//    shape = grid.parse( "POLYGON((172.67870664597 -34.318349361419, 172.85448789597 -35.285146236419, 173.90917539597 -36.427724361419, 174.52440977097 -37.394521236419, 174.70019102097 -38.449208736419, 173.73339414597 -38.800771236419, 172.06347227097 -40.734364986419, 171.27245664597 -41.437489986419, 170.74511289597 -42.580068111419, 167.31737852097 -44.601552486419, 166.08690977097 -45.656239986419, 166.08690977097 -46.359364986419, 167.49315977097 -46.710927486419, 169.25097227097 -47.238271236419, 171.27245664597 -45.832021236419, 171.79980039597 -44.162099361419, 173.20605039597 -43.986318111419, 173.46972227097 -43.195302486419, 174.61230039597 -41.701161861419, 176.63378477097 -41.349599361419, 177.42480039597 -39.591786861419, 179.09472227097 -37.482411861419, 178.21581602097 -37.042958736419, 177.51269102097 -37.658193111419, 176.19433164597 -37.218739986419, 176.01855039597 -36.076161861419, 174.61230039597 -34.933583736419, 172.67870664597 -34.318349361419))" );
//
//    // NZ small
//    shape = grid.parse( "POLYGON((177.71347045897 -39.051589965823, 177.78762817382 -39.062576293948, 177.81784057616 -39.081802368167, 177.85079956054 -39.062576293948, 177.86453247069 -39.081802368167, 177.85903930663 -39.114761352542, 177.83706665038 -39.161453247073, 177.8123474121 -39.172439575198, 177.83706665038 -39.21913146973, 177.86178588866 -39.268569946292, 177.90847778319 -39.227371215823, 177.92495727538 -39.17518615723, 177.94692993163 -39.15321350098, 177.94692993163 -39.13124084473, 177.96066284179 -39.139480590823, 177.99636840819 -39.120254516605, 178.01284790038 -39.103775024417, 177.96066284179 -39.090042114261, 177.92221069335 -39.08729553223, 177.90847778319 -39.06532287598, 177.89474487304 -39.035110473636, 177.89749145507 -38.996658325198, 177.91122436522 -38.966445922855, 177.71347045897 -39.051589965823))" );
//
//    // SF
//    shape = grid.parse( "POLYGON((-122.497288225 37.683619017479, -122.50003480703 37.70833825576, -122.50827455313 37.727564329979, -122.50827455313 37.746790404198, -122.50758790762 37.759836668846, -122.51376771719 37.783182616112, -122.50758790762 37.792109007714, -122.48767518789 37.792109007714, -122.47531556875 37.812708372948, -122.46776246817 37.814768309471, -122.45814943106 37.809275145409, -122.43686342031 37.812708372948, -122.42313051016 37.809275145409, -122.4093976 37.814081663964, -122.39360475332 37.805155272362, -122.3826184252 37.793482298729, -122.37918519766 37.780436034081, -122.37918519766 37.766703123925, -122.37781190664 37.756403441307, -122.37712526113 37.746790404198, -122.37918519766 37.743357176659, -122.36888551504 37.733057494042, -122.36064576895 37.737864012596, -122.35240602285 37.73031091201, -122.3565258959 37.72413110244, -122.35858583242 37.715204710839, -122.37094545156 37.717264647362, -122.3743786791 37.72413110244, -122.37575197012 37.720011229393, -122.38124513418 37.714518065331, -122.36270570547 37.70215844619, -122.36407899649 37.696665282128, -122.37025880606 37.694605345604, -122.38742494375 37.711084837792, -122.38879823477 37.711084837792, -122.3826184252 37.693918700096, -122.38055848867 37.684305662987, -122.497288225 37.683619017479))" );


  //  shape = shape.project( projection, false );

  //  shape = new Rectangle( -170,-85, 170, 85 );

    MatchInfo vals = grid.read( shape ); //new GeometryShape( shape ) ); //new EnvelopeShape( shape.getEnvelopeInternal() ) );
    vals.printInfo( System.out );

//    StringBuilder str = new StringBuilder();
//    vals = new MatchInfo();
//    for( int i=0; i<10; i++ ) {
//      str.append( 'A' );
//      vals.getLevelInfo(i, true).intersects.add( str.toString() );
//    }
//    vals.printInfo();

    vals = MatchInfo.getMostlyWithinQueryTokens( vals.tokens );
    vals.printInfo( System.out );

    File outfile = new File( "c:/temp/test.kml" );
    Kml kml = toKML( vals, "test: 3785", grid, true );
    kml.marshal(outfile);
    System.out.println( "done." );
  }
}
