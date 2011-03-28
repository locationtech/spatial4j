package org.apache.lucene.spatial.search.jts;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.jts.JtsUtil;
import org.apache.lucene.spatial.search.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.search.SingleFieldSpatialIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

/**
 * @author Chris Male
 */
public class JTSGeoSpatialIndexer extends SingleFieldSpatialIndexer<SimpleSpatialFieldInfo> {

  private static final Logger logger = LoggerFactory.getLogger(JTSGeoSpatialIndexer.class);

  private final GeometryFactory geometryFactory;

  public JTSGeoSpatialIndexer(GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  @Override
  public Fieldable createField(SimpleSpatialFieldInfo indexInfo, Shape shape, boolean index, boolean store) {
    Geometry geo = JtsUtil.getGeometryFrom(shape, geometryFactory);
    String wkt = (store) ? geo.toText() : null;

    if (!index) {
      return new WKBField(indexInfo.getFieldName(), null, wkt);
    }

    WKBWriter writer = new WKBWriter();
    byte[] wkb = writer.write(geo);

    if (wkb.length > 32000) {
      long last = wkb.length;
      Envelope env = geo.getEnvelopeInternal();
      double mins = Math.min(env.getWidth(), env.getHeight());
      double div = 1000;
      while (true) {
        double tolerance = mins / div;
        if (logger.isInfoEnabled()) {
          logger.info("Simplifying long geometry: WKB.length=" + wkb.length + " tolerance=" + tolerance);
        }
        Geometry simple = TopologyPreservingSimplifier.simplify(geo, tolerance);
        wkb = writer.write(simple);
        if (wkb.length < 32000) {
          break;
        }
        if (wkb.length == last) {
          throw new InvalidShapeException("Can not simplify geometry smaller then max. " + last);
        }
        last = wkb.length;
        div *= .70;
      }
    }

    return new WKBField(indexInfo.getFieldName(), wkb, wkt);
  }
}
