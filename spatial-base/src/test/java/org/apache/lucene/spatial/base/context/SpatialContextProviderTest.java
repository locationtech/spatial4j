package org.apache.lucene.spatial.base.context;

import static org.junit.Assert.assertEquals;

import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.junit.After;
import org.junit.Test;

/**
 * @author Chris Male
 */
public class SpatialContextProviderTest {

  @Test
  public void testGetContext_simpleSpatialContext() {
    System.setProperty("SpatialContextProvider", "org.apache.lucene.spatial.base.context.MockSpatialContext");

    SpatialContext spatialContext = SpatialContextProvider.getContext();
    assertEquals(MockSpatialContext.class, spatialContext.getClass());
  }

  @Test
  public void testGetContext_defaultBehavior() {
    SpatialContext spatialContext = SpatialContextProvider.getContext();
    assertEquals(SimpleSpatialContext.class, spatialContext.getClass());
  }

  @Test
  public void testGetContext_unknownContext() {
    System.setProperty("SpatialContextProvider", "org.apache.lucene.spatial.base.context.ContextDoesNotExist");

    SpatialContext spatialContext = SpatialContextProvider.getContext();
    assertEquals(SimpleSpatialContext.class, spatialContext.getClass());
  }

  @After
  public void tearDown() {
    System.getProperties().remove("SpatialContextProvider");
    SpatialContextProvider.clear();
  }
}
