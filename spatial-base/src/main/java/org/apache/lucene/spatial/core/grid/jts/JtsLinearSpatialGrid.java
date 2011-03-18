package org.apache.lucene.spatial.core.grid.jts;

import java.io.IOException;
import org.apache.lucene.spatial.core.BBox;
import org.apache.lucene.spatial.core.Shape;
import org.apache.lucene.spatial.core.grid.LinearSpatialGrid;
import org.apache.lucene.spatial.core.jts.JtsEnvelope;
import com.vividsolutions.jts.geom.Envelope;

public class JtsLinearSpatialGrid extends LinearSpatialGrid
{
  public final WKTShapeReader reader = new WKTShapeReader();

  public JtsLinearSpatialGrid( double xmin, double xmax, double ymin, double ymax, int maxLevels )
  {
    super( xmin, xmax, ymin, ymax, maxLevels );
  }

  @Override
  protected BBox makeExtent( double xmin, double xmax, double ymin, double ymax )
  {
    return new JtsEnvelope( new Envelope( xmin, xmax, ymin, ymax) );
  }

  @Override
  public Shape readShape(String str) throws IOException {
    return reader.readShape( str );
  }
}
