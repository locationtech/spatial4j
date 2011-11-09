/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.spatial.prefix;

import org.apache.lucene.spatial.base.distance.DistanceUtils;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixTree;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.prefix.PrefixTreeStrategy;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.spatial.SpatialFieldType;

import java.util.Map;

public abstract class PrefixTreeFieldType extends SpatialFieldType<SimpleSpatialFieldInfo> {

  private static final double DEFAULT_MAX_DETAIL_KM = 0.001;//1m

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);

    String v;
    v = args.remove("maxLevels");
    Integer maxLevels = null;
    if (v != null) {
      maxLevels = Integer.valueOf(v);
    }
    v = args.remove("maxDetailKm");
    Double degrees = null;
    if (v != null) {
      if (maxLevels != null)
        throw new RuntimeException("should not specify both maxLevels & maxDetailKm");
      double dist = Double.parseDouble(v);
      degrees = maxDetailKm2Degrees(dist);
    } else if (maxLevels == null) {
      degrees = maxDetailKm2Degrees(DEFAULT_MAX_DETAIL_KM);
    }

    PrefixTreeStrategy strat = initStrategy(maxLevels, degrees);
    final SpatialPrefixTree grid = strat.getGrid();
    log.info("strat "+strat+" maxLevels: "+ grid.getMaxLevels());//TODO output field name & maxDetailKm

    strat.setIgnoreIncompatibleGeometry( ignoreIncompatibleGeometry );

    v = args.remove("distErrPct");
    if (v != null)
      strat.setDistErrPct(Double.parseDouble(v));

    spatialStrategy = strat;
  }

  private double maxDetailKm2Degrees(double dist) {
    double[] latLonOut = DistanceUtils.pointOnBearing(0, 0, dist, DistanceUtils.DEG_90_AS_RADS, null,
        DistanceUtils.EARTH_MEAN_RADIUS_KM);
    if( Math.abs(latLonOut[0]) > 0.0001 ) {
      throw new RuntimeException("Expect LatLonOut[0]==0, not: ["+latLonOut[0]+","+latLonOut[1]+"]");
      //assert latLonOut[0] == 0;
    }
    return latLonOut[1] * DistanceUtils.RADIANS_TO_DEGREES;
  }

  protected abstract PrefixTreeStrategy initStrategy(Integer maxLevels, Double degrees);

  @Override
  protected SimpleSpatialFieldInfo getFieldInfo(SchemaField field) {
    return new SimpleSpatialFieldInfo(field.getName());
  }
}
