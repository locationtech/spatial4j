package com.spatial4j.core.shape;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.shape.impl.GreatCircle;
import com.spatial4j.core.shape.impl.PointImpl;
import org.junit.Test;



/**
 * Created by chris on 2/7/2014.
 */
public class GreatCircleTest extends RandomizedTest {

  //TODO: Robustness of calcs
  private static final double EPS = 0.0000001;
  private final SpatialContext ctx = new SpatialContextFactory()
  {{geo = true;}}.newSpatialContext();

  @Test
  public void distance() {
    // Great circle is equator
    dist(90,0,0,5,0,0,90); // north pole
    dist(45,0, 0, 5, 0, 0, 45); // 45 north of equator
    double random90 = randomDouble()*90;
    dist(random90,0, 0, 5, 0, 0, random90); // 45 north of equator

    // Dateline
    dist(90,0, 0, 0, 90 , 90, 0);
    dist(45,0, 0, 0, 10 , 45, 0);

    // Random Vertical to Long
    dist(random90,0, 0, 90, 0,0, random90);

    //
    dist(random90, 0, 0, 0, 90, random90, 0);
    dist(45, 0, 0, 90, 45, 0, 90);
    dist(45, 0, 0, 0,45, 45, 0);
    dist(0, 0, 0, 0,90, 0, 2);
    dist(2, 0, 0, 90,0, 90, 2);
    dist(90-random90, 0, 0, 90, random90, 0,90);
    dist(Math.abs(45-random90), 0, 0, 90, 45, 90,random90);
    dist(0, 0, 0, 0, 90, 180,0);

  }

  private Point testDistFlip(Point flip) {

    double flipX = flip.getX()-180;

    if(flipX < -180)
      flipX += 360;

    return ctx.makePoint(flipX, -1.0*flip.getY());
  }
  
  private void dist(double dist,double ax, double ay, double bx, double by, double cx, double cy) {
    Point a = ctx.makePoint(ax, ay);
    Point b = ctx.makePoint(bx, by);
    Point c = ctx.makePoint(cx, cy);

    GreatCircle circle = new GreatCircle(a, b);
    assertEquals(dist,circle.distanceToPoint(c),EPS);

    c = this.testDistFlip(c);
    circle = new GreatCircle(a, b);
    assertEquals(dist,circle.distanceToPoint(c),EPS);

    a = this.testDistFlip(a);
    circle = new GreatCircle(a, b);
    assertEquals(dist,circle.distanceToPoint(c),EPS);

    b = this.testDistFlip(b);
    circle = new GreatCircle(a, b);
    assertEquals(dist,circle.distanceToPoint(c),EPS);

  }
}
