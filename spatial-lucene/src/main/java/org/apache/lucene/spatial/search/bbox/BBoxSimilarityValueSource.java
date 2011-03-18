/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
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
package org.apache.lucene.spatial.search.bbox;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.cache.EntryCreator;
import org.apache.lucene.search.cache.CachedArray.DoubleValues;
import org.apache.lucene.search.function.DocValues;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * An implementation of the Lucene ValueSource model to support spatial relevance ranking.
 *
 */
public class BBoxSimilarityValueSource extends ValueSource
{
  private static final int HCODE = BBoxSimilarityValueSource.class.hashCode();

  /** The Logger. */
  private static Logger log = LoggerFactory.getLogger(BBoxSimilarityValueSource.class);

  private final BBoxFieldInfo field;
  private final BBoxSimilarity similarity;

  /**
   * Constructor.
   * @param queryEnvelope the query envelope
   * @param queryPower the query power (scoring algorithm)
   * @param targetPower the target power (scoring algorithm)
   */
  public BBoxSimilarityValueSource(BBoxSimilarity similarity, BBoxFieldInfo field)
  {
    this.similarity = similarity;
    this.field = field;
  }

  /**
   * Returns the ValueSource description.
   * @return the description
   */
  @Override
  public String description() {
    return "SpatialRankingValueSource("+similarity+")";
  }



  /**
   * Returns the DocValues used by the function query.
   * @param reader the index reader
   * @return the values
   */
  @Override
  public DocValues getValues(AtomicReaderContext context) throws IOException {
    IndexReader reader = context.reader;
    // How do we make sure to get the right entry creator?
    // what will solr do?
    final DoubleValues minX = FieldCache.DEFAULT.getDoubles( reader, field.minX, (EntryCreator<DoubleValues>)null );
    final DoubleValues minY = FieldCache.DEFAULT.getDoubles( reader, field.minY, (EntryCreator<DoubleValues>)null );
    final DoubleValues maxX = FieldCache.DEFAULT.getDoubles( reader, field.maxX, (EntryCreator<DoubleValues>)null );
    final DoubleValues maxY = FieldCache.DEFAULT.getDoubles( reader, field.maxY, (EntryCreator<DoubleValues>)null );

    return new DocValues() {
      @Override
      public float floatVal(int doc) {
        // make sure it has minX and area
        if( minX.valid.get( doc ) && maxX.valid.get( doc ) ) {
          Rectangle rect = new Rectangle(
              minX.values[doc],maxX.values[doc],
              minY.values[doc],maxY.values[doc]);
          similarity.score( rect );
        }
        return 0;
      }
      @Override
      public String toString(int doc) {
        return description()+"="+floatVal(doc);
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
    if (o.getClass() !=  BBoxSimilarityValueSource.class)
      return false;

    BBoxSimilarityValueSource other = (BBoxSimilarityValueSource)o;
    return similarity.equals( other.similarity );
  }

  /**
   * Returns the ValueSource hash code.
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return HCODE+similarity.hashCode();
  }
}
