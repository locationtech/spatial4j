/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io.jackson;

import org.locationtech.spatial4j.shape.Shape;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.locationtech.jts.geom.Geometry;

public class ShapesAsGeoJSONModule extends SimpleModule
{
  private static final long serialVersionUID = 1L;

  public ShapesAsGeoJSONModule()
  {
    super(PackageVersion.VERSION);
    // first deserializers
    addDeserializer(Geometry.class, new GeometryDeserializer());
    addDeserializer(Shape.class, new ShapeDeserializer());

    // then serializers:
    addSerializer(Geometry.class, new GeometryAsGeoJSONSerializer());
    addSerializer(Shape.class, new ShapeAsGeoJSONSerializer());
  }

  // will try to avoid duplicate registations (if MapperFeature enabled)
  @Override
  public String getModuleName() {
    return getClass().getSimpleName();
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return this == o;
  }
}
