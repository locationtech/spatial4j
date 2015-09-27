package org.locationtech.spatial4j.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.spatial4j.core.shape.Shape;
import com.vividsolutions.jts.geom.Geometry;

public class Spatial4jModule extends SimpleModule
{
  private static final long serialVersionUID = 1L;

  public Spatial4jModule()
  {
    super(PackageVersion.VERSION);
    // first deserializers
    addDeserializer(Geometry.class, new GeometryDeserializer());

    // then serializers:
    addSerializer(Geometry.class, new GeometrySerializer());
    addSerializer(Shape.class, new ShapeSerializer());
  }

  // yes, will try to avoid duplicate registations (if MapperFeature enabled)
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
