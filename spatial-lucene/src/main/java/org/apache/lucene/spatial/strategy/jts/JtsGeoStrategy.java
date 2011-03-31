/**
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

package org.apache.lucene.spatial.strategy.jts;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.SpatialStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

/**
 * Indexed field is WKB (store WKT)
 * <p/>
 * Maximum bytes for WKB is 32000, this will simplify geometry till there are fewer then 32K bytes
 *
 * TODO -- this is a good candidate for the new docvalues stuff when that lands:
 * https://svn.apache.org/repos/asf/lucene/dev/branches/docvalues/
 */
public class JtsGeoStrategy extends SpatialStrategy<SimpleSpatialFieldInfo> {

  private static final Logger logger = LoggerFactory.getLogger(JtsGeoStrategy.class);

  private final JtsShapeIO shapeIO;

  public JtsGeoStrategy(JtsShapeIO shapeIO) {
    this.shapeIO = shapeIO;
  }

  @Override
  public Fieldable createField(SimpleSpatialFieldInfo indexInfo, Shape shape, boolean index, boolean store) {
    Geometry geo = shapeIO.getGeometryFrom(shape);
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

  @Override
  public ValueSource makeValueSource(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Query makeQuery(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo) {
    Geometry geo = shapeIO.getGeometryFrom(args.getShape());
    GeometryTest tester = GeometryTestFactory.get(args.getOperation(), geo);

    GeometryOperationFilter filter = new GeometryOperationFilter(fieldInfo.getFieldName(), tester, shapeIO);
    return new FilteredQuery(new MatchAllDocsQuery(), filter);
  }

}