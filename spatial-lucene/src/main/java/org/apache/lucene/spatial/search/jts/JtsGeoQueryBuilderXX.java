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

package org.apache.lucene.spatial.search.jts;

import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.shape.jts.JtsUtil;
import org.apache.lucene.spatial.search.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.search.SpatialQueryBuilder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class JtsGeoQueryBuilderXX implements SpatialQueryBuilder<SimpleSpatialFieldInfo> {

  private final GeometryFactory factory;

  public JtsGeoQueryBuilderXX(GeometryFactory factory) {
    this.factory = factory;
  }

  @Override
  public ValueSource makeValueSource(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Query makeQuery(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo) {
    Geometry geo = JtsUtil.getGeometryFrom(args.getShape(), factory);
    GeometryTest tester = GeometryTestFactory.get(args.getOperation(), geo);

    GeometryOperationFilter filter = new GeometryOperationFilter(fieldInfo.getFieldName(), tester, factory);
    return new FilteredQuery(new MatchAllDocsQuery(), filter);
  }

}