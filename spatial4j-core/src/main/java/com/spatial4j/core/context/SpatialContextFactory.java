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

package com.spatial4j.core.context;

import com.spatial4j.core.distance.CartesianDistCalc;
import com.spatial4j.core.distance.DistanceCalculator;
import com.spatial4j.core.distance.GeodesicSphereDistCalc;
import com.spatial4j.core.shape.Rectangle;

import java.util.Map;

/**
 * Factory for a {@link SpatialContext} based on configuration data.  Call
 * {@link #makeSpatialContext(java.util.Map, ClassLoader)}.
 * <p/>
 * The following keys are looked up in the args map:
 * <DL>
 * <DT>spatialContextFactory</DT>
 * <DD>com.spatial4j.core.context.SpatialContext or com.spatial4j.core
 * .context.jts.JtsSpatialContext</DD>
 * <DT>geo</DT>
 * <DD>true | false</DD>
 * <DT>distCalculator</DT>
 * <DD>haversine | lawOfCosines | vincentySphere | cartesian | cartesian^2
 * -- see {@link DistanceCalculator}</DD>
 * <DT>worldBounds</DT>
 * <DD>-180,180,-90,90 -- the string form of a {@link Rectangle} read by
 * {@link SpatialContext#readShape(String)}</DD>
 * </DL>
 */
public class SpatialContextFactory {

  protected Map<String, String> args;
  protected ClassLoader classLoader;
  
  protected boolean geo = true;
  protected DistanceCalculator calculator;
  protected Rectangle worldBounds;

  /**
   * Creates a new {@link SpatialContext} based on configuration in
   * <code>args</code>.  See the class definition for what keys are looked up
   * in it.
   * The factory class is looked up via "spatialContextFactory" in args
   * then falling back to a Java system property (with initial caps). If neither are specified
   * then {@link SpatialContextFactory} is chosen.
   *
   * @param args Non-null map of name-value pairs.
   * @param classLoader Optional, except when a class name is provided to an
   *                    argument.
   */
  public static SpatialContext makeSpatialContext(Map<String,String> args, ClassLoader classLoader) {
    SpatialContextFactory instance;
    String cname = args.get("spatialContextFactory");
    if (cname == null)
      cname = System.getProperty("SpatialContextFactory");
    if (cname == null)
      instance = new SpatialContextFactory();
    else {
      try {
        Class c = classLoader.loadClass(cname);
        instance = (SpatialContextFactory) c.newInstance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    instance.init(args,classLoader);
    return instance.newSpatialContext();
  }

  protected SpatialContextFactory() {
  }

  protected void init(Map<String, String> args, ClassLoader classLoader) {
    this.args = args;
    this.classLoader = classLoader;
    initUnits();
    initCalculator();
    initWorldBounds();
  }

  protected void initUnits() {
    String geoStr = args.get("geo");
    if (geoStr != null)
      geo = Boolean.valueOf(geoStr);
  }

  protected void initCalculator() {
    String calcStr = args.get("distCalculator");
    if (calcStr == null)
      return;
    if (calcStr.equalsIgnoreCase("haversine")) {
      calculator = new GeodesicSphereDistCalc.Haversine();
    } else if (calcStr.equalsIgnoreCase("lawOfCosines")) {
      calculator = new GeodesicSphereDistCalc.LawOfCosines();
    } else if (calcStr.equalsIgnoreCase("vincentySphere")) {
      calculator = new GeodesicSphereDistCalc.Vincenty();
    } else if (calcStr.equalsIgnoreCase("cartesian")) {
      calculator = new CartesianDistCalc();
    } else if (calcStr.equalsIgnoreCase("cartesian^2")) {
      calculator = new CartesianDistCalc(true);
    } else {
      throw new RuntimeException("Unknown calculator: "+calcStr);
    }
  }

  protected void initWorldBounds() {
    String worldBoundsStr = args.get("worldBounds");
    if (worldBoundsStr == null)
      return;
    
    //kinda ugly we do this just to read a rectangle.  TODO refactor
    SpatialContext simpleCtx = new SpatialContext(geo, calculator, null);
    worldBounds = (Rectangle) simpleCtx.readShape(worldBoundsStr);
  }

  /** Subclasses should simply construct the instance from the initialized configuration. */
  protected SpatialContext newSpatialContext() {
    return new SpatialContext(geo,calculator,worldBounds);
  }
}
