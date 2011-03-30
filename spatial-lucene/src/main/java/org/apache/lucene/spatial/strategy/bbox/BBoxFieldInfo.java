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

package org.apache.lucene.spatial.strategy.bbox;

import org.apache.lucene.spatial.strategy.SpatialFieldInfo;

/**
 * Fieldnames to store
 */
public class BBoxFieldInfo implements SpatialFieldInfo {

  public static final String SUFFIX_MINX = "__minX";
  public static final String SUFFIX_MAXX = "__maxX";
  public static final String SUFFIX_MINY = "__minY";
  public static final String SUFFIX_MAXY = "__maxY";
  public static final String SUFFIX_XDL  = "__xdl";

  public String minX = "envelope.minx";
  public String minY = "envelope.miny";
  public String maxX = "envelope.maxx";
  public String maxY = "envelope.maxy";

  // crosses dateline
  public String xdl = "envelope.xdl";

  public void setFieldsPrefix(String prefix) {
    minX = prefix + SUFFIX_MINX;
    maxX = prefix + SUFFIX_MAXX;
    minY = prefix + SUFFIX_MINY;
    maxY = prefix + SUFFIX_MAXY;
    xdl  = prefix + SUFFIX_XDL;
  }
}
