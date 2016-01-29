/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.shape;

import org.locationtech.spatial4j.context.SpatialContext;

public abstract class BaseShape<T extends SpatialContext> implements Shape {

  protected final T ctx;
  
  public BaseShape(T ctx) {
    this.ctx = ctx;
  }

  @Override
  public T getContext() {
    return ctx;
  }
}
