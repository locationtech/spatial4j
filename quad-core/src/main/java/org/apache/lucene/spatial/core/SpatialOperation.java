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
package org.apache.lucene.spatial.core;



/**
 * A clause that compares a stored geometry to a supplied geometry.
 */
public enum SpatialOperation
{
  BBOXIntersects( true ),
  Contains( true ),
  Intersects( true ),
  IsEqualTo( false ),
  IsDisjointTo( false ),
  IsWithin( true ),
  Overlaps( true ),

  // Point
  DistanceTo( true ),

  // Fuzzy
  SimilarTo( true ),
  ;

  public final boolean scoreIsMeaningful;

  private SpatialOperation( boolean v ) {
    scoreIsMeaningful = v;
  }
}
