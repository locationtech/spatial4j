/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.context;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.distance.CartesianDistCalc;
import com.spatial4j.core.distance.GeodesicSphereDistCalc;
import com.spatial4j.core.io.jts.JtsWktShapeParser;
import com.spatial4j.core.shape.impl.RectangleImpl;
import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SpatialContextFactoryTest {
  public static final String PROP = "SpatialContextFactory";

  @After
  public void tearDown() {
    System.getProperties().remove(PROP);
  }
  
  private SpatialContext call(String... argsStr) {
    Map<String,String> args = new HashMap<String,String>();
    for (int i = 0; i < argsStr.length; i+=2) {
      String key = argsStr[i];
      String val = argsStr[i+1];
      args.put(key,val);
    }
    return SpatialContextFactory.makeSpatialContext(args, getClass().getClassLoader());
  }
  
  @Test
  public void testDefault() {
    SpatialContext ctx = SpatialContext.GEO;
    SpatialContext ctx2 = call();//default
    assertEquals(ctx.getClass(), ctx2.getClass());
    assertEquals(ctx.isGeo(), ctx2.isGeo());
    assertEquals(ctx.getDistCalc(),ctx2.getDistCalc());
    assertEquals(ctx.getWorldBounds(), ctx2.getWorldBounds());
  }
  
  @Test
  public void testCustom() {
    SpatialContext ctx = call("geo","false");
    assertTrue(!ctx.isGeo());
    assertEquals(new CartesianDistCalc(), ctx.getDistCalc());

    ctx = call("geo","false",
        "distCalculator","cartesian^2",
        "worldBounds","ENVELOPE(-100, 75, 200, 0)");//xMin, xMax, yMax, yMin
    assertEquals(new CartesianDistCalc(true),ctx.getDistCalc());
    assertEquals(new RectangleImpl(-100, 75, 0, 200, ctx), ctx.getWorldBounds());

    ctx = call("geo","true",
        "distCalculator","lawOfCosines");
    assertTrue(ctx.isGeo());
    assertEquals(new GeodesicSphereDistCalc.LawOfCosines(),
        ctx.getDistCalc());
  }

  @Test
  public void testJtsContextFactory() {
    JtsSpatialContext ctx = (JtsSpatialContext) call(
        "spatialContextFactory", JtsSpatialContextFactory.class.getName(),
        "geo", "true",
        "normWrapLongitude", "true",
        "precisionScale", "2.0",
        "wktShapeParserClass", CustomWktShapeParser.class.getName(),
        "datelineRule", "ccwRect",
        "validationRule", "repairConvexHull",
        "autoIndex", "true");
    assertTrue(ctx.isNormWrapLongitude());
    assertEquals(2.0, ctx.getGeometryFactory().getPrecisionModel().getScale(), 0.0);
    assertTrue(CustomWktShapeParser.once);//cheap way to test it was created
    assertEquals(JtsWktShapeParser.DatelineRule.ccwRect,
        ((JtsWktShapeParser)ctx.getWktShapeParser()).getDatelineRule());
    assertEquals(JtsWktShapeParser.ValidationRule.repairConvexHull,
        ((JtsWktShapeParser)ctx.getWktShapeParser()).getValidationRule());

    //ensure geo=false with worldbounds works -- fixes #72
    ctx = (JtsSpatialContext) call(
        "spatialContextFactory", JtsSpatialContextFactory.class.getName(),
        "geo", "false",//set to false
        "worldBounds", "ENVELOPE(-500,500,300,-300)",
        "normWrapLongitude", "true",
        "precisionScale", "2.0",
        "wktShapeParserClass", CustomWktShapeParser.class.getName(),
        "datelineRule", "ccwRect",
        "validationRule", "repairConvexHull",
        "autoIndex", "true");
    assertEquals(300, ctx.getWorldBounds().getMaxY(), 0.0);
  }
  
  @Test
  public void testSystemPropertyLookup() {
    System.setProperty(PROP,DSCF.class.getName());
    assertTrue(!call().isGeo());//DSCF returns this
  }

  public static class DSCF extends SpatialContextFactory {

    @Override
    public SpatialContext newSpatialContext() {
      geo = false;
      return new SpatialContext(this);
    }
  }

  public static class CustomWktShapeParser extends JtsWktShapeParser {
    static boolean once = false;//cheap way to test it was created
    public CustomWktShapeParser(JtsSpatialContext ctx, JtsSpatialContextFactory factory) {
      super(ctx, factory);
      once = true;
    }
  }
}
