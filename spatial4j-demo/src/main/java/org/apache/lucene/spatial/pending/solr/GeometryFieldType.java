package org.apache.lucene.spatial.pending.solr;

import java.util.Map;

import org.apache.lucene.spatial.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.pending.JtsGeoStrategy;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.spatial.SpatialFieldType;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.distance.DistanceUnits;


/**
 * This is here because the dependency tree needs work!
 */
public class GeometryFieldType extends SpatialFieldType<SimpleSpatialFieldInfo> {

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);

    ctx = new JtsSpatialContext(DistanceUnits.KILOMETERS);
    spatialStrategy = new JtsGeoStrategy((JtsSpatialContext)ctx);
    spatialStrategy.setIgnoreIncompatibleGeometry( ignoreIncompatibleGeometry );
  }

  @Override
  protected SimpleSpatialFieldInfo getFieldInfo(SchemaField field) {
    return new SimpleSpatialFieldInfo(field.getName());
  }
}
