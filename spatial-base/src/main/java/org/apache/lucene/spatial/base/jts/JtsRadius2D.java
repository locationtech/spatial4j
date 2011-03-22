package org.apache.lucene.spatial.base.jts;

import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.Point;
import org.apache.lucene.spatial.base.Radius;
import org.apache.lucene.spatial.base.Shape;


public class JtsRadius2D implements Radius
{
  private Point point;
  private double radius;

  public JtsRadius2D(Point p, double r)
  {
    this.point = p;
    this.radius = r;
  }

  //-----------------------------------------------------
  //-----------------------------------------------------

  @Override
  public Point getPoint() {
    return point;
  }

  @Override
  public double getRadius() {
    return radius;
  }

  @Override
  public JtsEnvelope getBoundingBox() {
    return new JtsEnvelope(
        point.getX()-radius, point.getX()+radius,
        point.getY()-radius, point.getY()+radius );
  }

  @Override
  public boolean hasArea() {
    return radius > 0;
  }

  @Override
  public IntersectCase intersect(Shape other, Object context)
  {
    // TODO... something better!
    return getBoundingBox().intersect(other, context);
  }
}
