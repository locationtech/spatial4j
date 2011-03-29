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

package org.apache.lucene.spatial.base.query;

import org.apache.lucene.spatial.base.exception.InvalidSpatialArgument;
import org.apache.lucene.spatial.base.shape.Shape;

public class SpatialArgs {

  private SpatialOperation operation;
  private Shape shape;
  private boolean cacheable = true;
  private boolean calculateScore = true;

  // Useful for 'distance' calculations
  private Double min;
  private Double max;

  public SpatialArgs(SpatialOperation operation) {
    this.operation = operation;
  }

  public SpatialArgs(SpatialOperation operation, Shape shape) {
    this.operation = operation;
    this.shape = shape;
  }

  /**
   * Check if the arguments make sense -- throw an exception if not
   */
  public void validate() throws InvalidSpatialArgument {
    if (operation.isTargetNeedsArea() && !shape.hasArea()) {
      throw new InvalidSpatialArgument(operation.name() + " only supports geometry with area");
    }
  }

  //------------------------------------------------
  // Getters & Setters
  //------------------------------------------------

  public SpatialOperation getOperation() {
    return operation;
  }

  public void setOperation(SpatialOperation operation) {
    this.operation = operation;
  }

  public Shape getShape() {
    return shape;
  }

  public void setShape(Shape shape) {
    this.shape = shape;
  }

  public boolean isCacheable() {
    return cacheable;
  }

  public void setCacheable(boolean cacheable) {
    this.cacheable = cacheable;
  }

  public boolean isCalculateScore() {
    return calculateScore;
  }

  public void setCalculateScore(boolean calculateScore) {
    this.calculateScore = calculateScore;
  }

  public Double getMin() {
    return min;
  }

  public void setMin(Double min) {
    this.min = min;
  }

  public Double getMax() {
    return max;
  }

  public void setMax(Double max) {
    this.max = max;
  }
}
