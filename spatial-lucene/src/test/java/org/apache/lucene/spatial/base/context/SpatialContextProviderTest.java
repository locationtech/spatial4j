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
