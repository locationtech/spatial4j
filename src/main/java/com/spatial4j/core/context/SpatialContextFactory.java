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

import com.spatial4j.core.distance.CartesianDistCalc;
import com.spatial4j.core.distance.DistanceCalculator;
import com.spatial4j.core.distance.GeodesicSphereDistCalc;
import com.spatial4j.core.shape.Rectangle;

import java.lang.reflect.Field;
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
 * <DD>true (default)| false </DD>
 * <DT>distCalculator</DT>
 * <DD>haversine | lawOfCosines | vincentySphere | cartesian | cartesian^2
 * -- see {@link DistanceCalculator}</DD>
 * <DT>worldBounds</DT>
 * <DD>-180 180 -90 90 -- the string form of a {@link Rectangle} read by
 * {@link SpatialContext#readShape(String)}</DD>
 * <DT>normWrapLongitude</DT>
 * <DD>true | false (default) -- if longitudes
 * out of -180 to 180 range get wrapped back into this range instead of throwing an error when
 * they are read from WKT.</DD>
 * </DL>
 */
public class SpatialContextFactory {

  /** Set by {@link #makeSpatialContext(java.util.Map, ClassLoader)}. */
  protected Map<String, String> args;
  /** Set by {@link #makeSpatialContext(java.util.Map, ClassLoader)}. */
  protected ClassLoader classLoader;

  /* These fields are public to make it easy to set them without bothering with setters. */

  public boolean geo = true;
  public DistanceCalculator distCalc;
  public Rectangle worldBounds;

  public boolean normWrapLongitude = false;

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
    initField("geo");
    initCalculator();
    initWorldBounds();

    initField("normWrapLongitude");
  }

  /** Gets {@code name} from args and populates a field by the same name with the value. */
  protected void initField(String name) {
    //  note: java.beans API is more verbose to use correctly but would arguably be better
    Field field;
    try {
      field = getClass().getField(name);
    } catch (NoSuchFieldException e) {
      throw new Error(e);
    }
    String str = args.get(name);
    if (str != null) {
      //TODO support other primitive types as applicable
      assert field.getType() == Boolean.TYPE;
      Object o = Boolean.valueOf(str);
      try {
        field.set(this, o);
      } catch (IllegalAccessException e) {
        throw new Error(e);
      }
    }
  }

  protected void initCalculator() {
    String calcStr = args.get("distCalculator");
    if (calcStr == null)
      return;
    if (calcStr.equalsIgnoreCase("haversine")) {
      distCalc = new GeodesicSphereDistCalc.Haversine();
    } else if (calcStr.equalsIgnoreCase("lawOfCosines")) {
      distCalc = new GeodesicSphereDistCalc.LawOfCosines();
    } else if (calcStr.equalsIgnoreCase("vincentySphere")) {
      distCalc = new GeodesicSphereDistCalc.Vincenty();
    } else if (calcStr.equalsIgnoreCase("cartesian")) {
      distCalc = new CartesianDistCalc();
    } else if (calcStr.equalsIgnoreCase("cartesian^2")) {
      distCalc = new CartesianDistCalc(true);
    } else {
      throw new RuntimeException("Unknown calculator: "+calcStr);
    }
  }

  protected void initWorldBounds() {
    String worldBoundsStr = args.get("worldBounds");
    if (worldBoundsStr == null)
      return;
    
    //kinda ugly we do this just to read a rectangle.  TODO refactor
    SpatialContext simpleCtx = new SpatialContext(geo, distCalc, null);
    worldBounds = (Rectangle) simpleCtx.readShape(worldBoundsStr);
  }

  /** Subclasses should simply construct the instance from the initialized configuration. */
  public SpatialContext newSpatialContext() {
    return new SpatialContext(this);
  }
}
