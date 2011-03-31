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

package org.apache.lucene.spatial.strategy.point;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldCache.DoubleParser;
import org.apache.lucene.search.cache.CachedArrayCreator;
import org.apache.lucene.search.cache.DoubleValuesCreator;
import org.apache.lucene.search.cache.CachedArray.DoubleValues;
import org.apache.lucene.search.function.DocValues;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.simple.Point2D;

/**
 *
 * An implementation of the Lucene ValueSource model to support spatial relevance ranking.
 *
 */
public class DistanceValueSource extends ValueSource {

  private final PointFieldInfo fields;
  private final DistanceCalculator calculator;
  private final Point from;
  private final DoubleParser parser;

  /**
   * Constructor.
   * @param queryEnvelope the query envelope
   * @param queryPower the query power (scoring algorithm)
   * @param targetPower the target power (scoring algorithm)
   */
  public DistanceValueSource(Point from, DistanceCalculator calc, PointFieldInfo fields, DoubleParser parser) {
    this.from = from;
    this.fields = fields;
    this.calculator = calc;
    this.parser = parser;
  }

  /**
   * Returns the ValueSource description.
   * @return the description
   */
  @Override
  public String description() {
    return "DistanceValueSource("+calculator+")";
  }


  /**
   * Returns the DocValues used by the function query.
   * @param reader the index reader
   * @return the values
   */
  @Override
  public DocValues getValues(AtomicReaderContext context) throws IOException {
    IndexReader reader = context.reader;

    final DoubleValues ptX = FieldCache.DEFAULT.getDoubles(reader, fields.getFieldNameX(),
          new DoubleValuesCreator(fields.getFieldNameX(), parser, CachedArrayCreator.CACHE_VALUES_AND_BITS));
    final DoubleValues ptY = FieldCache.DEFAULT.getDoubles(reader, fields.getFieldNameY(),
        new DoubleValuesCreator(fields.getFieldNameY(), parser, CachedArrayCreator.CACHE_VALUES_AND_BITS));

    final int baseID = context.docBase;
    return new DocValues() {
      @Override
      public float floatVal(int doc) {
        return (float) doubleVal(doc);
      }

      @Override
      public double doubleVal(int doc) {
        // make sure it has minX and area
        if (ptX.valid.get(doc) && ptY.valid.get(doc)) {
          Point2D pt = new Point2D( ptX.values[doc],  ptY.values[doc] );
          return calculator.calculate(from, pt,baseID+doc);
        }
        return 0;
      }

      @Override
      public String toString(int doc) {
        return description() + "=" + floatVal(doc);
      }
    };
  }

  /**
   * Determines if this ValueSource is equal to another.
   * @param o the ValueSource to compare
   * @return <code>true</code> if the two objects are based upon the same query envelope
   */
  @Override
  public boolean equals(Object o) {
    if (o.getClass() !=  DistanceValueSource.class)
      return false;

    DistanceValueSource other = (DistanceValueSource) o;
    return calculator.equals(other.calculator);
  }

  @Override
  public int hashCode() {
    return DistanceValueSource.class.hashCode() + calculator.hashCode();
  }
}
