package org.apache.lucene.spatial.base.grid.jts;

import java.io.IOException;
import org.apache.lucene.spatial.base.BBox;
import org.apache.lucene.spatial.base.Shape;
import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.spatial.base.grid.LinearSpatialGrid;
import org.apache.lucene.spatial.base.jts.JtsEnvelope;
import org.apache.lucene.spatial.base.jts.WKTShapeReader;

import com.vividsolutions.jts.geom.Envelope;

public class JtsLinearSpatialGrid extends LinearSpatialGrid
{
  public JtsLinearSpatialGrid( double xmin, double xmax, double ymin, double ymax, int maxLevels )
  {
    super( xmin, xmax, ymin, ymax, maxLevels );
  }

  @Override
  protected BBox makeExtent( double xmin, double xmax, double ymin, double ymax )
  {
    return new JtsEnvelope( new Envelope( xmin, xmax, ymin, ymax) );
  }
}
