package org.apache.lucene.spatial.base.context;

import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class SpatialContextProviderTest {

  @BeforeClass
  public static void setUp() {
    SpatialContextProvider.clear();
  }

  @Test
  public void testGetContext_simpleSpatialContext() {
    System.setProperty("SpatialContextProvider", MockSpatialContext.class.getName());

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
