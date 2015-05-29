/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.spatial4j.core.context.jts;

/**
 * Indicates the algorithm used to process JTS Polygons and JTS LineStrings for detecting dateline
 * crossings. It only applies when geo=true.
 */
public enum DatelineRule {
  /** No polygon will cross the dateline. */
  none,

  /**
   * Adjacent points with an x (longitude) difference that spans more than half way around the
   * globe will be interpreted as going the other (shorter) way, and thus cross the dateline.
   */
  width180, // TODO is there a better name that doesn't have '180' in it?

  /**
   * For rectangular polygons, the point order is interpreted as being counter-clockwise (CCW).
   * However, non-rectangular polygons or other shapes aren't processed this way; they use the
   * {@link #width180} rule instead. The CCW rule is specified by OGC Simple Features
   * Specification v. 1.2.0 section 6.1.11.1.
   */
  ccwRect
}
