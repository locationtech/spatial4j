package org.apache.lucene.spatial.base.context;

import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.base.distance.DistanceUnits;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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

  @Test
  public void testGetContext_withArguments() {
    System.setProperty("SpatialContextProvider", "org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext");
    
    SimpleSpatialContext simpleSpatialContext = (SimpleSpatialContext) SpatialContextProvider.getContext(DistanceUnits.MILES);
    assertEquals(DistanceUnits.MILES, simpleSpatialContext.getUnits());
  }

  @After
  public void tearDown() {
    System.getProperties().remove("SpatialContextProvider");
    SpatialContextProvider.clear();
  }
}
