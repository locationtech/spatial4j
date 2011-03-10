package org.apache.lucene.spatial.search.bbox;

import org.apache.lucene.util.NumericUtils;

/**
 * Fieldnames to store
 */
public class BBoxFieldInfo
{
  public int precisionStep = NumericUtils.PRECISION_STEP_DEFAULT;

  public String minX = "envelope.minx";
  public String minY = "envelope.miny";
  public String maxX = "envelope.maxx";
  public String maxY = "envelope.maxy";

  // crosses dateline
  public String docXDL = "envelope.xdl";
}
