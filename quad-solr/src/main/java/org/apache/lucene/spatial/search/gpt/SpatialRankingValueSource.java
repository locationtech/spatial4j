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
package org.apache.lucene.spatial.search.gpt;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldCache.DocTerms;
import org.apache.lucene.search.function.DocValues;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.core.Extent;
import org.apache.lucene.util.BytesRef;
import org.opengis.geometry.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * An implementation of the Lucene ValueSource model to support spatial relevance ranking.
 * <p/>
 * 
 * The algorithm is implemented as envelope on envelope overlays rather than
 * complex polygon on complex polygon overlays.
 * 
 * <p/>
 * Spatial relevance scoring algorithm:
 * 
 * <br/>  queryArea = the area of the input query envelope
 * <br/>  targetArea = the area of the target envelope (per Lucene document)
 * <br/>  intersectionArea = the area of the intersection for the query/target envelopes
 * <br/>  queryPower = the weighting power associated with the query envelope (default = 1.0)
 * <br/>  targetPower =  the weighting power associated with the target envelope (default = 1.0)
 *   
 * <br/>  queryRatio  = intersectionArea / queryArea;
 * <br/>  targetRatio = intersectionArea / targetArea;
 * <br/>  queryFactor  = Math.pow(queryRatio,queryPower);
 * <br/>  targetFactor = Math.pow(targetRatio,targetPower);
 * <br/>  score = queryFactor * targetFactor;
 *      
 */
public class SpatialRankingValueSource extends ValueSource 
{  
  /** class variables ========================================================= */
  
  /** The class name hash code. */
  private static final int HCODE = SpatialRankingValueSource.class.hashCode();

  /** The Logger. */
  private static Logger log = LoggerFactory.getLogger(SpatialRankingValueSource.class);
  
  private final String field;
  private final ExtentRanking ranker;

  /**
   * Constructor.
   * @param queryEnvelope the query envelope
   * @param queryPower the query power (scoring algorithm)
   * @param targetPower the target power (scoring algorithm)
   */
  public SpatialRankingValueSource(Extent queryEnvelope, double queryPower, double targetPower, String field) 
  { 
    ranker = new ExtentRanking( queryEnvelope, queryPower, targetPower );
    this.field = field;
  }
  
  /**
   * Returns the ValueSource description.
   * @return the description
   */
  @Override
  public String description() {
    return "SpatialRankingValueSource("+ranker+")";
  }
  
  /**
   * Determines if this ValueSource is equal to another.
   * @param o the ValueSource to compare
   * @return <code>true</code> if the two objects are based upon the same query envelope
   */
  @Override
  public boolean equals(Object o) {
    if (o.getClass() !=  SpatialRankingValueSource.class) 
      return false;
    
    SpatialRankingValueSource other = (SpatialRankingValueSource)o;
    return ranker.getDelimiterQueryParameters().equals(other.ranker.getDelimiterQueryParameters());
  }
  

  /**
   * Returns the DocValues used by the function query. 
   * @param reader the index reader
   * @return the values
   */
  @Override
  public DocValues getValues(AtomicReaderContext context) throws IOException {
    IndexReader reader = context.reader;
    final DocTerms terms = FieldCache.DEFAULT.getTerms(reader,field);
    final BytesRef bytes = new BytesRef();
    return new DocValues() {
      @Override
      public float floatVal(int doc) {
        String v = terms.getTerm(doc, bytes).utf8ToString();
        if( v == null || v.length() < 2 ) {
          return 0;
        }
        
        String[] tokens = v.split(";");
        if (tokens.length == 6) {
          double tgtMinX = Double.valueOf(tokens[0]);
          double tgtMinY = Double.valueOf(tokens[1]);
          double tgtMaxX = Double.valueOf(tokens[2]);
          double tgtMaxY = Double.valueOf(tokens[3]);
          double tgtArea = Double.valueOf(tokens[4]);
          boolean tgtCrossedDateline = Boolean.valueOf(tokens[5]);
         
          
        }
        return 0;
      }
      @Override
      public String toString(int doc) {
        return description()+"="+floatVal(doc);
      }
//      Object getInnerArray() {
//        return arr;
//      }
    };
    
  }
  
  /**
   * Returns the ValueSource hash code.
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return HCODE+ranker.getDelimiterQueryParameters().hashCode();
  }

}
