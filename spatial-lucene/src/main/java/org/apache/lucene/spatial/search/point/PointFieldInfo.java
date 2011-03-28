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

package org.apache.lucene.spatial.search.point;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldCache.DoubleParser;
import org.apache.lucene.search.cache.CachedArrayCreator;
import org.apache.lucene.search.cache.DoubleValuesCreator;
import org.apache.lucene.search.cache.CachedArray.DoubleValues;
import org.apache.lucene.spatial.search.SpatialFieldInfo;
import org.apache.lucene.util.NumericUtils;

/**
 * Fieldnames to store
 */
public class PointFieldInfo implements SpatialFieldInfo {
  
  public static final String SUFFIX_X = "__x";
  public static final String SUFFIX_Y = "__y";

  private final int precisionStep;
  private final DoubleParser parser;

  private final String xFieldName;
  private final String yFieldName;

  public PointFieldInfo() {
    this("point", NumericUtils.PRECISION_STEP_DEFAULT, null);
  }

  public PointFieldInfo(String fieldNamePrefix) {
    this(fieldNamePrefix, NumericUtils.PRECISION_STEP_DEFAULT, null);
  }

  public PointFieldInfo(String fieldNamePrefix, int precisionStep) {
    this(fieldNamePrefix, precisionStep, null);
  }

  public PointFieldInfo(String fieldNamePrefix, int precisionStep, DoubleParser parser) {
    xFieldName = fieldNamePrefix + SUFFIX_X;
    yFieldName = fieldNamePrefix + SUFFIX_Y;
    this.precisionStep = precisionStep;
    this.parser = parser;
  }

  public DoubleValues getXValues(IndexReader reader) throws IOException {
    return FieldCache.DEFAULT.getDoubles(reader, xFieldName,
        new DoubleValuesCreator(xFieldName, parser, CachedArrayCreator.CACHE_VALUES_AND_BITS));
  }

  public DoubleValues getYValues(IndexReader reader) throws IOException {
    return FieldCache.DEFAULT.getDoubles(reader, yFieldName,
        new DoubleValuesCreator(yFieldName, parser, CachedArrayCreator.CACHE_VALUES_AND_BITS));
  }

  public String getXFieldName() {
    return xFieldName;
  }

  public String getYFieldName() {
    return yFieldName;
  }

  public int getPrecisionStep() {
    return precisionStep;
  }

  public DoubleParser getParser() {
    return parser;
  }
}
