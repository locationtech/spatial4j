package org.apache.lucene.spatial.quads;

import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.ProjCoordinate;
import org.osgeo.proj4j.proj.CylindricalEqualAreaProjection;
import org.osgeo.proj4j.proj.LinearProjection;
import org.osgeo.proj4j.proj.Projection;
import org.osgeo.proj4j.util.CRSCache;
import org.osgeo.proj4j.util.ProjectionUtil;


public class ProjectionFutzer
{
  public static void printInfo( Projection p )
  {
    System.out.println("--------------");
    System.out.println( p.getName() );
    System.out.println( "rectilinear: " + p.isRectilinear() );
    System.out.println( "equalArea: " + p.isEqualArea() );
    System.out.println( "lat: " + p.getMinLatitudeDegrees()  + " <=> " + p.getMaxLatitudeDegrees() );
    System.out.println( "lon: " + p.getMinLongitudeDegrees() + " <=> " + p.getMaxLongitudeDegrees() );

    ProjCoordinate min = new ProjCoordinate( p.getMinLongitude(), p.getMinLatitude() );
    ProjCoordinate max = new ProjCoordinate( p.getMaxLongitude(), p.getMaxLatitude() );

    ProjCoordinate t = new ProjCoordinate();
    System.out.println( "min: " + ProjectionUtil.toString( p.project( min, t ) ) );
    System.out.println( "max: " + ProjectionUtil.toString( p.project( max, t ) ) );
  }

  public static void main( String[] args )
  {
    printInfo( new LinearProjection() );
//    printInfo( new MercatorProjection() );
//    printInfo( new SinusoidalProjection() );
//    printInfo( new LarriveeProjection() );
//    printInfo( new LoximuthalProjection() );

    System.out.println();
    System.out.println( "CylindricalEqualAreaProjection" );
    printInfo( new CylindricalEqualAreaProjection() );

    CRSCache cache = new CRSCache();
    CoordinateReferenceSystem crs = cache.createFromName( "epsg:3785" );
    System.out.println();
    System.out.println( crs.getName() );
    printInfo( crs.getProjection() );

    crs = cache.createFromName( "epsg:4326" );
    System.out.println();
    System.out.println( crs.getName() );
    printInfo( crs.getProjection() );

    crs = cache.createFromName( "esri:54016" );
    System.out.println();
    System.out.println( crs.getName() );
    printInfo( crs.getProjection() );

  }
}
