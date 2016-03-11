package org.locationtech.spatial4j.io.jackson;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.locationtech.spatial4j.io.GeneralGeoJSONTest;
import org.locationtech.spatial4j.io.ShapeIO;
import org.locationtech.spatial4j.io.jackson.ShapesAsGeoJSONModule;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This test compares the jackson JSONWriter to the standard GeoJSON Writer
 */
@Ignore
public class JacksonGeoJSONReaderTest extends GeneralGeoJSONTest {

  @Before
  @Override
  public void setUp() {
    super.setUp();
    
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new ShapesAsGeoJSONModule());

    reader = new JacksonShapeReader(mapper);
    writer = ctx.getFormats().getWriter(ShapeIO.GeoJSON);
    writerForTests = writer; //ctx.getFormats().getWriter(ShapeIO.GeoJSON);

    Assert.assertNotNull(reader);
    Assert.assertNotNull(writer);
    Assert.assertNotNull(writerForTests);
  }
}
