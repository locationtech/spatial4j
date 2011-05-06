/*
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

package org.apache.lucene.spatial.strategy.geohash;

import org.apache.lucene.spatial.base.shape.BBox;

import java.nio.charset.Charset;
import java.util.Collection;

/**
 * A node in a geospatial grid hierarchy as specified by a {@link GridReferenceSystem}.
 */
public class GridNode {

  final GridReferenceSystem refSys;
  final byte[] bytes;
  final BBox rect;

  GridNode(GridReferenceSystem refSys, byte[] bytes, BBox rect) {
    this.refSys = refSys;
    this.rect = rect;
    this.bytes = bytes;
    assert bytes.length <= refSys.maxLen;
    //assert this.rect.getMinY() <= this.rect.getMaxY() && this.rect.getMinX() <= this.rect.getMaxX();
  }

  public byte[] getBytes() {
    return bytes;
  }

  public int length() {
    return bytes.length;
  }

//  public Point2D getCentroid() {
//    return rect.centroid();
//  }

  public Collection<GridNode> getSubNodes() {
    return refSys.getSubNodes(this);
  }

  public BBox getRectangle() {
    return rect;
  }

  @Override
  public String toString() {
    return new String(bytes,UTF8)+" "+rect;
  }

  private static final Charset UTF8 = Charset.forName("UTF-8");

}
