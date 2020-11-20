/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.context;

import org.locationtech.spatial4j.context.jts.DatelineRule;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import org.locationtech.spatial4j.context.jts.ValidationRule;
import org.locationtech.spatial4j.distance.CartesianDistCalc;
import org.locationtech.spatial4j.distance.GeodesicSphereDistCalc;
import org.locationtech.spatial4j.io.ShapeIO;
import org.locationtech.spatial4j.io.WKTReader;
import org.locationtech.spatial4j.shape.impl.RectangleImpl;
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
    Map<String,String> args = new HashMap<>();
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
    assertEquals(DatelineRule.ccwRect,
        ctx.getDatelineRule());
    assertEquals(ValidationRule.repairConvexHull,
        ctx.getValidationRule());

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
  public void testFormatsConfig() {
    JtsSpatialContext ctx = (JtsSpatialContext) call(
        "spatialContextFactory", JtsSpatialContextFactory.class.getName(),
        "readers", CustomWktShapeParser.class.getName());
    
    assertTrue( ctx.getFormats().getReader(ShapeIO.WKT) instanceof CustomWktShapeParser );
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

  public static class CustomWktShapeParser extends WKTReader {
    static boolean once = false;//cheap way to test it was created
    public CustomWktShapeParser(JtsSpatialContext ctx, JtsSpatialContextFactory factory) {
      super(ctx, factory);
      once = true;
    }
  }
}
