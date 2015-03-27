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
import com.spatial4j.core.io.*;
import com.spatial4j.core.shape.Rectangle;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

//import org.slf4j.LoggerFactory;

/**
 * Factory for a {@link SpatialContext} based on configuration data.  Call
 * {@link #makeSpatialContext(java.util.Map, ClassLoader)} to construct one via String name-value
 * pairs. To construct one via code then create a factory instance, set the fields, then call
 * {@link #newSpatialContext()}.
 * <p/>
 * The following keys are looked up in the args map:
 * <DL>
 * <DT>spatialContextFactory</DT>
 * <DD>com.spatial4j.core.context.SpatialContext or
 * com.spatial4j.core.context.jts.JtsSpatialContext</DD>
 * <DT>geo</DT>
 * <DD>true (default)| false -- see {@link SpatialContext#isGeo()} </DD>
 * <DT>distCalculator</DT>
 * <DD>haversine | lawOfCosines | vincentySphere | cartesian | cartesian^2
 * -- see {@link DistanceCalculator}</DD>
 * <DT>worldBounds</DT>
 * <DD>{@code ENVELOPE(xMin, xMax, yMax, yMin)} -- see {@link SpatialContext#getWorldBounds()}</DD>
 * <DT>normWrapLongitude</DT>
 * <DD>true | false (default) -- see {@link SpatialContext#isNormWrapLongitude()}</DD>
 * <DT>wktShapeParserClass</DT>
 * <DD>Java class of the {@link com.spatial4j.core.io.WktShapeParser}</DD>
 * <DT>binaryCodecClass</DT>
 * <DD>Java class of the {@link com.spatial4j.core.io.BinaryCodec}</DD>
 * </DL>
 */
public class SpatialContextFactory {

  /** Set by {@link #makeSpatialContext(java.util.Map, ClassLoader)}. */
  protected Map<String, String> args;
  /** Set by {@link #makeSpatialContext(java.util.Map, ClassLoader)}. */
  protected ClassLoader classLoader;

  /* These fields are public to make it easy to set them without bothering with setters. */

  public boolean geo = true;
  public DistanceCalculator distCalc;//defaults in SpatialContext c'tor based on geo
  public Rectangle worldBounds;//defaults in SpatialContext c'tor based on geo

  public boolean normWrapLongitude = false;

  public Class<? extends BinaryCodec> binaryCodecClass = BinaryCodec.class;
  public final List<Class<? extends ShapeReader>> readers = new ArrayList<Class<? extends ShapeReader>>();
  public final List<Class<? extends ShapeWriter>> writers = new ArrayList<Class<? extends ShapeWriter>>();
  public boolean hasFormatConfig = false;

  public SpatialContextFactory() {
  }

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
    if (classLoader == null)
      classLoader = SpatialContextFactory.class.getClassLoader();
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
    instance.init(args, classLoader);
    return instance.newSpatialContext();
  }

  protected void init(Map<String, String> args, ClassLoader classLoader) {
    this.args = args;
    this.classLoader = classLoader;

    initField("geo");

    initCalculator();

    //init wktParser before worldBounds because WB needs to be parsed
    initFormats();
    initWorldBounds();

    initField("normWrapLongitude");

    initField("binaryCodecClass");
  }

  /** Gets {@code name} from args and populates a field by the same name with the value. */
  protected void initField(String name) {
    //  note: java.beans API is more verbose to use correctly (?) but would arguably be better
    Field field;
    try {
      field = getClass().getField(name);
    } catch (NoSuchFieldException e) {
      throw new Error(e);
    }
    String str = args.get(name);
    if (str != null) {
      try {
        Object o;
        if (field.getType() == Boolean.TYPE) {
          o = Boolean.valueOf(str);
        } else if (field.getType() == Class.class) {
          try {
            o = classLoader.loadClass(str);
          } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
          }
        } else if (field.getType().isEnum()) {
          o = Enum.valueOf(field.getType().asSubclass(Enum.class), str);
        } else {
          throw new Error("unsupported field type: "+field.getType());//not plausible at runtime unless developing
        }
        field.set(this, o);
      } catch (IllegalAccessException e) {
        throw new Error(e);
      } catch (Exception e) {
        throw new RuntimeException(
            "Invalid value '"+str+"' on field "+name+" of type "+field.getType(), e);
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

  /**
   * By default, we support GeoJSON and WKT.  If any formats are specified in the config
   * we expect it to configure the readers and writers
   */
  protected void initFormats() {
    checkDefaultFormats();

    try {
      String val = args.get("readers");
      if (val != null) {
        readers.clear();
        for (String name : val.split(",")) {
          readers.add(Class.forName(name.trim(), false, classLoader).asSubclass(ShapeReader.class));
        }
      } else {//deprecated; a parameter from when this was a raw class
        val = args.get("wktShapeParserClass");
        if (val != null) {
          readers.clear();
          //LoggerFactory.getLogger(getClass()).warn("Using deprecated argument: wktShapeParserClass={}", val);
          readers.add(Class.forName(val.trim(), false, classLoader).asSubclass(ShapeReader.class));
        }
      }
      val = args.get("writers");
      if (val != null) {
        writers.clear();
        for (String name : val.split(",")) {
          writers.add(Class.forName(name.trim(), false, classLoader).asSubclass(ShapeWriter.class));
        }
      }
    } catch (ClassNotFoundException ex) {
      throw new RuntimeException("Unable to find format class", ex);
    }
  }

  // Makes sure we have the default readers
  protected void checkDefaultFormats() {
    if (readers.isEmpty()) {
      addReaderIfNoggitExists(GeoJSONReader.class);
      readers.add(WKTReader.class);
    }
    if (writers.isEmpty()) {
      writers.add(WKTWriter.class);
      writers.add(GeoJSONWriter.class);
    }
  }

  public void addReaderIfNoggitExists(Class<? extends ShapeReader> reader) {
    try {
      if (classLoader==null) {
        Class.forName("org.noggit.JSONParser");
      } else {
        Class.forName("org.noggit.JSONParser", true, classLoader);
      }
      readers.add(reader);
    } catch (ClassNotFoundException e) {
      //LoggerFactory.getLogger(getClass()).warn("Unable to support GeoJSON Without Noggit");
    }
  }

  protected void initWorldBounds() {
    String worldBoundsStr = args.get("worldBounds");
    if (worldBoundsStr == null)
      return;

    //kinda ugly we do this just to read a rectangle.  TODO refactor
    final SpatialContext ctx = newSpatialContext();
    worldBounds = (Rectangle) ctx.readShape(worldBoundsStr);//TODO use readShapeFromWkt
  }

  /** Subclasses should simply construct the instance from the initialized configuration. */
  public SpatialContext newSpatialContext() {
    return new SpatialContext(this);
  }

  public BinaryCodec makeBinaryCodec(SpatialContext ctx) {
    return makeClassInstance(binaryCodecClass, ctx, this);
  }

  public List<ShapeReader> makeReaders(SpatialContext ctx) {
    List<ShapeReader> registry = new ArrayList<ShapeReader>(readers.size());
    for (Class<? extends ShapeReader> clazz : readers) {
      try {
        registry.add(makeClassInstance(clazz, ctx, this));
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
    return Collections.unmodifiableList(registry);
  }

  public List<ShapeWriter> makeWriters(SpatialContext ctx) {
    List<ShapeWriter> registry = new ArrayList<ShapeWriter>(writers.size());
    for (Class<? extends ShapeWriter> clazz : writers) {
      try {
        registry.add(makeClassInstance(clazz, ctx, this));
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
    return Collections.unmodifiableList(registry);
  }


  @SuppressWarnings("unchecked")
  private <T> T makeClassInstance(Class<? extends T> clazz, Object... ctorArgs) {
    try {
      Constructor<?> empty = null;

      //can't simply lookup constructor by arg type because might be subclass type
      ctorLoop: for (Constructor<?> ctor : clazz.getConstructors()) {
        Class[] parameterTypes = ctor.getParameterTypes();
        if (parameterTypes.length == 0) {
          empty = ctor; // the empty constructor;
        }
        if (parameterTypes.length != ctorArgs.length)
          continue;
        for (int i = 0; i < ctorArgs.length; i++) {
          Object ctorArg = ctorArgs[i];
          if (!parameterTypes[i].isAssignableFrom(ctorArg.getClass()))
            continue ctorLoop;
        }
        return clazz.cast(ctor.newInstance(ctorArgs));
      }

      // If an empty constructor exists, use that
      if (empty != null) {
        return clazz.cast(empty.newInstance());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    throw new RuntimeException(clazz + " needs a constructor that takes: "
        + Arrays.toString(ctorArgs));
  }

}
