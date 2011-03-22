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
import org.apache.lucene.util.NumericUtils;

/**
 * Fieldnames to store
 */
public class PointFieldInfo
{
  public static final String SUFFIX_X = "__x";
  public static final String SUFFIX_Y = "__y";

  public int precisionStep = NumericUtils.PRECISION_STEP_DEFAULT;
  public DoubleParser parser = null;

  public String fieldX = "point__x";
  public String fieldY = "point__y";

  public void setFieldsPrefix( String prefix )
  {
    fieldX = prefix + SUFFIX_X;
    fieldY = prefix + SUFFIX_Y;
  }

  public DoubleValues getXValues( IndexReader reader ) throws IOException
  {
    return FieldCache.DEFAULT.getDoubles( reader, fieldX,
        new DoubleValuesCreator( fieldX, parser, CachedArrayCreator.CACHE_VALUES_AND_BITS ) );
  }

  public DoubleValues getYValues( IndexReader reader ) throws IOException
  {
    return FieldCache.DEFAULT.getDoubles( reader, fieldY,
        new DoubleValuesCreator( fieldY, parser, CachedArrayCreator.CACHE_VALUES_AND_BITS ) );
  }
}
