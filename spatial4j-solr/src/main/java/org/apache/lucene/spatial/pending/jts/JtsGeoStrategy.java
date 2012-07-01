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
package org.apache.lucene.spatial.pending.jts;

import org.apache.lucene.document.DerefBytesDocValuesField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Shape;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

/**
 * Put raw WKB in DocValues
 */
public class JtsGeoStrategy extends SpatialStrategy<SimpleSpatialFieldInfo> {

  private static final Logger logger = LoggerFactory.getLogger(JtsGeoStrategy.class);

  private final JtsSpatialContext context;
  private int max_wkb_length = 32000;

  public JtsGeoStrategy(JtsSpatialContext ctx) {
    super(ctx);
    this.context = ctx;
  }

  @Override
  public IndexableField createField(SimpleSpatialFieldInfo indexInfo, Shape shape, boolean index, boolean store) {
    Geometry geo = context.getGeometryFrom(shape);

    WKBWriter writer = new WKBWriter();
    BytesRef wkb = new BytesRef(writer.write(geo));

    if (max_wkb_length > 0 && wkb.length > max_wkb_length) {
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
        wkb = new BytesRef(writer.write(simple));
        if (wkb.length < max_wkb_length) {
          break;
        }
        if (wkb.length == last) {
          throw new InvalidShapeException("Can not simplify geometry smaller then max. " + last);
        }
        last = wkb.length;
        div *= .70;
      }
    }

    return new DerefBytesDocValuesField(indexInfo.getFieldName(), wkb);
  }

  @Override
  public Query makeQuery(org.apache.lucene.spatial.query.SpatialArgs args,
      SimpleSpatialFieldInfo fieldInfo) {
    Filter f = makeFilter(args, fieldInfo);
    // TODO... could add in scoring here..
    return new ConstantScoreQuery( f );
  }

  @Override
  public ValueSource makeValueSource(
      org.apache.lucene.spatial.query.SpatialArgs args,
      SimpleSpatialFieldInfo fieldInfo) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Filter makeFilter(org.apache.lucene.spatial.query.SpatialArgs args,
      SimpleSpatialFieldInfo field) {
    Geometry geo = context.getGeometryFrom(args.getShape());
    GeometryTest tester = GeometryTestFactory.get(args.getOperation(), geo);
    return new GeometryOperationFilter(field.getFieldName(), tester, context);
  }
}