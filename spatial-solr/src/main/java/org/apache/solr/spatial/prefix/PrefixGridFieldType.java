package org.apache.solr.spatial.prefix;

import org.apache.lucene.spatial.base.distance.DistanceUtils;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.prefix.PrefixGridStrategy;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.spatial.SpatialFieldType;

import java.util.Map;

public abstract class PrefixGridFieldType extends SpatialFieldType<SimpleSpatialFieldInfo> {

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

    PrefixGridStrategy strat = initStrategy(maxLevels, degrees);
    final SpatialPrefixGrid grid = strat.getGrid();
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
    assert latLonOut[0] == 0;
    return latLonOut[1] * DistanceUtils.RADIANS_TO_DEGREES;
  }

  protected abstract PrefixGridStrategy initStrategy(Integer maxLevels, Double degrees);

  @Override
  protected SimpleSpatialFieldInfo getFieldInfo(SchemaField field) {
    return new SimpleSpatialFieldInfo(field.getName());
  }
}
