package org.apache.lucene.spatial.base.prefix.jts;

import org.apache.lucene.spatial.base.prefix.LinearPrefixGrid;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.jts.JtsEnvelope;

import com.vividsolutions.jts.geom.Envelope;

public class JtsLinearPrefixGrid extends LinearPrefixGrid {

  public JtsLinearPrefixGrid(double xmin, double xmax, double ymin, double ymax, int maxLevels) {
    super(xmin, xmax, ymin, ymax, maxLevels);
  }

  @Override
  protected BBox makeExtent(double xmin, double xmax, double ymin, double ymax) {
    return new JtsEnvelope(new Envelope(xmin, xmax, ymin, ymax));
  }
}
