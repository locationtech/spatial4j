package org.apache.lucene.spatial.search.bbox;

import org.apache.lucene.spatial.core.BBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The algorithm is implemented as envelope on envelope overlays rather than
 * complex polygon on complex polygon overlays.
 *
 * <p/>
 * Spatial relevance scoring algorithm:
 *
 * <br/>  queryArea = the area of the input query envelope
 * <br/>  targetArea = the area of the target envelope (per Lucene document)
 * <br/>  intersectionArea = the area of the intersection for the query/target envelopes
 * <br/>  queryPower = the weighting power associated with the query envelope (default = 1.0)
 * <br/>  targetPower =  the weighting power associated with the target envelope (default = 1.0)
 *
 * <br/>  queryRatio  = intersectionArea / queryArea;
 * <br/>  targetRatio = intersectionArea / targetArea;
 * <br/>  queryFactor  = Math.pow(queryRatio,queryPower);
 * <br/>  targetFactor = Math.pow(targetRatio,targetPower);
 * <br/>  score = queryFactor * targetFactor;
 *
 */
public class AreaSimilarity implements BBoxSimilarity
{
  /** The Logger. */
  private static Logger log = LoggerFactory.getLogger(AreaSimilarity.class);

  /** Properties associated with the query envelope */
  private final BBox queryExtent;
  private final double queryArea;

  private final double targetPower;
  private final double queryPower;

  public AreaSimilarity( BBox queryExtent, double queryPower, double targetPower )
  {
    this.queryExtent = queryExtent;
    this.queryArea = queryExtent.getArea();

    this.queryPower = queryPower;
    this.targetPower = targetPower;

//  if (this.qryMinX > queryExtent.getMaxX()) {
//    this.qryCrossedDateline = true;
//    this.qryArea = Math.abs(qryMaxX + 360.0 - qryMinX) * Math.abs(qryMaxY - qryMinY);
//  } else {
//    this.qryArea = Math.abs(qryMaxX - qryMinX) * Math.abs(qryMaxY - qryMinY);
//  }
  }

  public AreaSimilarity( BBox queryExtent )
  {
    this( queryExtent, 2.0, 0.5 );
  }


  public String getDelimiterQueryParameters() {
    return queryExtent.toString()+";"+queryPower+";"+targetPower;
  }

  @Override
  public double score(BBox target) {
    if (target == null || queryArea <= 0) {
      return 0;
    }
    double targetArea = target.getArea();
    if( targetArea <= 0 ) {
      return 0;
    }
    double score = 0;

    double top    = Math.min(queryExtent.getMaxY(),target.getMaxY());
    double bottom = Math.max(queryExtent.getMinY(),target.getMinY());
    double height = top - bottom;
    double width  = 0;

    // queries that cross the date line
    if( queryExtent.getCrossesDateLine() ) {
      // documents that cross the date line
      if( target.getCrossesDateLine() ) {
        double left  = Math.max(queryExtent.getMinX(),target.getMinX());
        double right = Math.min(queryExtent.getMaxX(),target.getMaxX());
        width = right + 360.0 - left;
      }
      else {
        double qryWestLeft  = Math.max(queryExtent.getMinX(), target.getMaxX());
        double qryWestRight = Math.min(target.getMaxX(),180.0);
        double qryWestWidth = qryWestRight - qryWestLeft;
        if (qryWestWidth > 0) {
          width = qryWestWidth;
        }
        else {
          double qryEastLeft  = Math.max(target.getMaxX(),-180.0);
          double qryEastRight = Math.min(queryExtent.getMaxX(),target.getMaxX());
          double qryEastWidth = qryEastRight - qryEastLeft;
          if (qryEastWidth > 0) {
            width = qryEastWidth;
          }
        }
      }
    }

    // queries that do not cross the date line
    else {

      if( target.getCrossesDateLine() ) {
        double tgtWestLeft  = Math.max(queryExtent.getMinX(), target.getMinX() );
        double tgtWestRight = Math.min(queryExtent.getMaxX(),180.0);
        double tgtWestWidth = tgtWestRight - tgtWestLeft;
        if (tgtWestWidth > 0) {
          width = tgtWestWidth;
        } else {
          double tgtEastLeft  = Math.max(queryExtent.getMinX(),-180.0);
          double tgtEastRight = Math.min(queryExtent.getMaxX(),target.getMaxX());
          double tgtEastWidth = tgtEastRight - tgtEastLeft;
          if (tgtEastWidth > 0) {
            width = tgtEastWidth;
          }
        }
      }
      else {
        double left  = Math.max(queryExtent.getMinX(),target.getMinX());
        double right = Math.min(queryExtent.getMaxX(),target.getMaxX());
        width = right - left;
      }
    }


    // calculate the score
    if ((width > 0) && (height > 0)) {
      double intersectionArea = width * height;
      double queryRatio  = intersectionArea / queryArea;
      double targetRatio = intersectionArea / targetArea;
      double queryFactor  = Math.pow(queryRatio,queryPower);
      double targetFactor = Math.pow(targetRatio,targetPower);
      score = queryFactor * targetFactor * 10000.0;

      if ( log.isTraceEnabled() ) {
        StringBuffer sb = new StringBuffer();
        sb.append("\nscore="+score);
        sb.append("\n  query="+queryExtent.toString() );
        sb.append("\n  target="+target.toString() );
        sb.append("\n  intersectionArea="+intersectionArea);
        sb.append(" queryArea="+queryArea+" targetArea="+targetArea);
        sb.append("\n  queryRatio="+queryRatio+" targetRatio="+targetRatio);
        sb.append("\n  queryFactor="+queryFactor+" targetFactor="+targetFactor);
        sb.append(" (queryPower="+queryPower+" targetPower="+targetPower+")");
        log.trace( sb.toString() );
      }
    }
    return score;
  }


  /**
   * Determines if this ValueSource is equal to another.
   * @param o the ValueSource to compare
   * @return <code>true</code> if the two objects are based upon the same query envelope
   */
  @Override
  public boolean equals(Object o) {
    if (o.getClass() !=  AreaSimilarity.class)
      return false;

    AreaSimilarity other = (AreaSimilarity)o;
    return getDelimiterQueryParameters().equals(other.getDelimiterQueryParameters());
  }

  /**
   * Returns the ValueSource hash code.
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return getDelimiterQueryParameters().hashCode();
  }
}
