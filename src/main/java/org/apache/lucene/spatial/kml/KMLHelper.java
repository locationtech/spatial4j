package org.apache.lucene.spatial.kml;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.spatial.geometry.shape.Geometry2D;
import org.apache.lucene.spatial.geometry.shape.JtsGeom;
import org.apache.lucene.spatial.geometry.shape.Rectangle;
import org.apache.lucene.spatial.quads.LevelMatchInfo;
import org.apache.lucene.spatial.quads.MatchInfo;
import org.apache.lucene.spatial.quads.SpatialGrid;

import com.sun.org.apache.xml.internal.utils.StylesheetPIHandler;
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
import de.micromata.opengis.kml.v_2_2_0.Style;
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
  

  private static Placemark create( String key, String style, SpatialGrid grid )
  {
    Rectangle r = grid.getRectangle( key );
    
    List<Coordinate> coords = null;
    coords = new ArrayList<Coordinate>(5);
    coords.add( new Coordinate( r.getMinX(),r.getMinY() ) );
    coords.add( new Coordinate( r.getMaxX(),r.getMinY() ) );
    coords.add( new Coordinate( r.getMaxX(),r.getMaxY() ) );
    coords.add( new Coordinate( r.getMinX(),r.getMaxY() ) );
    coords.add( new Coordinate( r.getMinX(),r.getMinY() ) );
    
    Placemark p = new Placemark().withName( key )
      .withDescription( "descriptio..." )
      .withStyleUrl( style );
    
    p.createAndSetPolygon()
        .withTessellate( true )
        .createAndSetOuterBoundaryIs()
          .createAndSetLinearRing().withCoordinates( coords );
  
    return p;
  }
  
  public static Kml toKML( MatchInfo info, String name, SpatialGrid grid )
  {
    final Kml kml = KmlFactory.createKml();
    Document document = kml.createAndSetDocument()
      .withName( name ).withOpen(true);
    
    addStyles( document );
    
    for( LevelMatchInfo level : info.levels ) {
      Folder folder = document.createAndAddFolder().withName( "Level "+level.level ).withOpen(false);
      for( String c : level.intersects ) {
        folder.getFeature().add( create( c, "#iii", grid ) );
      }
      for( String c : level.depth ) {
        folder.getFeature().add( create( c, "#mmm", grid ) );
      }
      for( String c : level.covers ) {
        folder.getFeature().add( create( c, "#ccc", grid ) );
      }
    }
    
    return kml;
  }

  public static void main( String[] args ) throws Exception 
  {
    SpatialGrid grid = new SpatialGrid( 0, 10, 0, 10, 10 );
    grid = new SpatialGrid( -180, 180, -90-180, 90, 16 );
    grid.resolution = 4; // how far past the best fit to go

    Geometry2D shape = null;

    shape = new Rectangle( .2, .2, 10, 10 ); //.1, .4, .1, .4 ); //2.3,7.5,2.5,7.5 );

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
    shape = new JtsGeom( gsf.createCircle() );


  //  shape = JtsGeom.parseGeometry( "LINESTRING(2 2, 5 7, 8 1 )" );

    MatchInfo vals = grid.read( shape );
    vals.printInfo();
    
    File outfile = new File( "c:/temp/test.kml" );
    Kml kml = toKML( vals, "test", grid );
    kml.marshal(outfile);
    System.out.println( "done." );
  }
}
