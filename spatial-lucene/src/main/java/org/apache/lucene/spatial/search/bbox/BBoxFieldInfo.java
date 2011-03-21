package org.apache.lucene.spatial.search.bbox;

import org.apache.lucene.util.NumericUtils;

/**
 * Fieldnames to store
 */
public class BBoxFieldInfo
{
  public static final String SUFFIX_MINX = "__minX";
  public static final String SUFFIX_MAXX = "__maxX";
  public static final String SUFFIX_MINY = "__minY";
  public static final String SUFFIX_MAXY = "__maxY";
  public static final String SUFFIX_XDL  = "__xdl";

  public int precisionStep = NumericUtils.PRECISION_STEP_DEFAULT;

  public String minX = "envelope.minx";
  public String minY = "envelope.miny";
  public String maxX = "envelope.maxx";
  public String maxY = "envelope.maxy";

  // crosses dateline
  public String xdl = "envelope.xdl";

  public void setFieldsPrefix( String prefix )
  {
    minX = prefix + SUFFIX_MINX;
    maxX = prefix + SUFFIX_MAXX;
    minY = prefix + SUFFIX_MINY;
    maxY = prefix + SUFFIX_MAXY;
    xdl  = prefix + SUFFIX_XDL;
  }
}
