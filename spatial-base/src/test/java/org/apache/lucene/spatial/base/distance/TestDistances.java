package org.apache.lucene.spatial.base.distance;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author David Smiley - dsmiley@mitre.org
 */
public class TestDistances {
  //See http://www.devx.com/Java/Article/31983/0/page/2

  private HaversineDistanceCalculator DC = new HaversineDistanceCalculator(DistanceUtils.EARTH_MEAN_RADIUS_KM);

  @Test
  public void truth() {
    assertTrue(true);//TODO
  }
}
