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
package org.apache.lucene.spatial.base;



/**
 * A clause that compares a stored geometry to a supplied geometry.
 */
public enum SpatialOperation
{
  // Geometry Operations
  BBoxIntersects( true, false, false ),
  BBoxWithin( true, false, false ),
  Contains( true, true, false ),
  Intersects( true, false, false ),
  IsEqualTo( false, false, false ),
  IsDisjointTo( false, false, false ),
  IsWithin( true, false, true ),
  Overlaps( true, false, true ),

  // Fuzzy Shape matching
  SimilarTo( true, false, false ),

  // Distance Calculation
  Distance( true, false, false ),
  ;

  public final boolean scoreIsMeaningful;
  public final boolean sourceNeedsArea;
  public final boolean targetNeedsArea;

  private SpatialOperation( boolean v, boolean sa, boolean ta ) {
    scoreIsMeaningful = v;
    sourceNeedsArea = sa;
    targetNeedsArea = ta;
  }
}
