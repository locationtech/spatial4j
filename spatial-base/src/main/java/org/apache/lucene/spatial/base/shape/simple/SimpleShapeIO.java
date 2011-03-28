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

package org.apache.lucene.spatial.base.shape.simple;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.ShapeIO;
import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.spatial.base.shape.Point;

public class SimpleShapeIO implements ShapeIO {

  public Shape readShape(String value) throws InvalidShapeException {
    return readSimpleShape(value);
  }

  public static Shape readSimpleShape(String str) {
    if (str.length() < 1) {
      throw new InvalidShapeException(str);
    }

    StringTokenizer st = new StringTokenizer(str, " ");
    double p0 = Double.parseDouble(st.nextToken());
    double p1 = Double.parseDouble(st.nextToken());

    if (st.hasMoreTokens()) {
      double p2 = Double.parseDouble(st.nextToken());
      double p3 = Double.parseDouble(st.nextToken());
      return new Rectangle(p0, p2, p1, p3);
    }

    return new Point2D(p0, p1);
  }

  public String writeBBox(BBox bbox) {
    NumberFormat nf = NumberFormat.getInstance(Locale.US);
    nf.setGroupingUsed(false);
    nf.setMaximumFractionDigits(6);
    nf.setMinimumFractionDigits(6);
    
    return
      nf.format(bbox.getMinX()) + " " +
      nf.format(bbox.getMinY()) + " " +
      nf.format(bbox.getMaxX()) + " " +
      nf.format(bbox.getMaxY());
  }

  @Override
  public String toString(Shape shape) {
    if (Point.class.isInstance(shape)) {
      NumberFormat nf = NumberFormat.getInstance(Locale.US);
      nf.setGroupingUsed(false);
      nf.setMaximumFractionDigits(6);
      nf.setMinimumFractionDigits(6);
      Point point = (Point) shape;
      return nf.format(point.getX()) + " " + nf.format(point.getY());
    } else if (BBox.class.isInstance(shape)) {
      writeBBox((BBox) shape);
    }
    return shape.toString();
  }

  @Override
  public Shape readShape(byte[] bytes, int offset, int length)
      throws InvalidShapeException {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public byte[] toBytes(Shape shape) throws IOException {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public BBox makeBBox(double minX, double maxX, double minY, double maxY) {
    return new Rectangle( minX, maxX, minY, maxY );
  }

  @Override
  public Point makePoint(double x, double y) {
    return new Point2D(x,y);
  }
}
