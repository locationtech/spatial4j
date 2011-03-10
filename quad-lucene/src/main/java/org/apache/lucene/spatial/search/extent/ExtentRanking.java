package org.apache.lucene.spatial.search.extent;

import org.apache.lucene.spatial.core.Extent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtentRanking
{
  /** The Logger. */
  private static Logger log = LoggerFactory.getLogger(SpatialRankingValueSource.class);

  /** Properties associated with the query envelope */
  private final Extent queryExtent;
  private final double queryArea;

  private final double targetPower;
  private final double queryPower;

  public ExtentRanking( Extent queryExtent, double queryPower, double targetPower )
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

  public ExtentRanking( Extent queryExtent )
  {
    this( queryExtent, 2.0, 0.5 );
  }

  public String getDelimiterQueryParameters() {
    return queryExtent.toString()+";"+queryPower+";"+targetPower;
  }

  public float calculate(Extent target) {
    if (target == null || queryArea <= 0) {
      return 0.f;
    }
    double targetArea = target.getArea();
    if( targetArea <= 0 ) {
      return 0.f;
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
    return (float)score;
  }
}
