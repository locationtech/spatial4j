package org.apache.lucene.spatial.base.simple;

import org.apache.lucene.spatial.base.BBox;
import org.apache.lucene.spatial.base.IntersectCase;
import org.apache.lucene.spatial.base.Radius;
import org.apache.lucene.spatial.base.Shape;


public class Radius2D implements Radius
{
  private Point2D point;
  private double radius;

  public Radius2D(Point2D p, double r)
  {
    this.point = p;
    this.radius = r;
  }

  //-----------------------------------------------------
  //-----------------------------------------------------

  @Override
  public Point2D getPoint() {
    return point;
  }

  @Override
  public double getRadius() {
    return radius;
  }

  @Override
  public BBox getBoundingBox() {
    return new Rectangle(
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
