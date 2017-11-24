/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.context.jts;

import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.io.GeoJSONReader;
import org.locationtech.spatial4j.io.LegacyShapeReader;
import org.locationtech.spatial4j.io.LegacyShapeWriter;
import org.locationtech.spatial4j.io.PolyshapeReader;
import org.locationtech.spatial4j.io.WKTReader;
import org.locationtech.spatial4j.io.jts.*;
import org.locationtech.spatial4j.shape.jts.JtsShapeFactory;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;

import java.util.Map;

/**
 * See {@link SpatialContextFactory#makeSpatialContext(java.util.Map, ClassLoader)}.
 * <p>
 * The following keys are looked up in the args map, in addition to those in the
 * superclass:
 * <DL>
 * <DT>datelineRule</DT>
 * <DD>width180(default)|ccwRect|none
 *  -- see {@link DatelineRule}</DD>
 * <DT>validationRule</DT>
 * <DD>error(default)|none|repairConvexHull|repairBuffer0
 *  -- see {@link ValidationRule}</DD>
 * <DT>autoIndex</DT>
 * <DD>true|false(default) -- see {@link JtsShapeFactory#isAutoIndex()}</DD>
 * <DT>allowMultiOverlap</DT>
 * <DD>true|false(default) -- see {@link JtsSpatialContext#isAllowMultiOverlap()}</DD>
 * <DT>precisionModel</DT>
 * <DD>floating(default) | floating_single | fixed
 *  -- see {@link org.locationtech.jts.geom.PrecisionModel}.
 * If {@code fixed} then you must also provide {@code precisionScale}
 *  -- see {@link org.locationtech.jts.geom.PrecisionModel#getScale()}</DD>
 * <DT>useJtsPoint, useJtsLineString, useJtsMulti</DT>
 * <DD>All default to true. See corresponding methods on {@link JtsShapeFactory}.</DD>
 * </DL>
 */
public class JtsSpatialContextFactory extends SpatialContextFactory {

  protected static final PrecisionModel defaultPrecisionModel = new PrecisionModel();//floating

  //These 3 are JTS defaults for new GeometryFactory()
  public PrecisionModel precisionModel = defaultPrecisionModel;
  public int srid = 0;
  public CoordinateSequenceFactory coordinateSequenceFactory = CoordinateArraySequenceFactory.instance();

  //ignored if geo=false
  public DatelineRule datelineRule = DatelineRule.width180;

  public ValidationRule validationRule = ValidationRule.error;
  public boolean autoIndex = false;
  public boolean allowMultiOverlap = false;//ignored if geo=false

  //kinda advanced options:
  public boolean useJtsPoint = true;
  public boolean useJtsLineString = true;
  public boolean useJtsMulti = true;

  public JtsSpatialContextFactory() {
    super.shapeFactoryClass = JtsShapeFactory.class;
    super.binaryCodecClass = JtsBinaryCodec.class;
  }

  @Override
  protected void checkDefaultFormats() {
    if (readers.isEmpty() ) {
      addReaderIfNoggitExists(GeoJSONReader.class);
      readers.add(WKTReader.class);
      readers.add(PolyshapeReader.class);
      readers.add(LegacyShapeReader.class);
    }
    if (writers.isEmpty()) {
      writers.add(JtsGeoJSONWriter.class);
      writers.add(JtsWKTWriter.class);
      writers.add(JtsPolyshapeWriter.class);
      writers.add(LegacyShapeWriter.class);
    }
  }
  
  @Override
  protected void init(Map<String, String> args, ClassLoader classLoader) {
    super.init(args, classLoader);

    initField("datelineRule");
    initField("validationRule");
    initField("autoIndex");
    initField("allowMultiOverlap");
    initField("useJtsPoint");
    initField("useJtsLineString");
    initField("useJtsMulti");

    String scaleStr = args.get("precisionScale");
    String modelStr = args.get("precisionModel");

    if (scaleStr != null) {
      if (modelStr != null && !modelStr.equals("fixed"))
        throw new RuntimeException("Since precisionScale was specified; precisionModel must be 'fixed' but got: "+modelStr);
      precisionModel = new PrecisionModel(Double.parseDouble(scaleStr));
    } else if (modelStr != null) {
      if (modelStr.equals("floating")) {
        precisionModel = new PrecisionModel(PrecisionModel.FLOATING);
      } else if (modelStr.equals("floating_single")) {
        precisionModel = new PrecisionModel(PrecisionModel.FLOATING_SINGLE);
      } else if (modelStr.equals("fixed")) {
        throw new RuntimeException("For fixed model, must specifiy 'precisionScale'");
      } else {
        throw new RuntimeException("Unknown precisionModel: "+modelStr);
      }
    }
  }
  
  public GeometryFactory getGeometryFactory() {
    if (precisionModel == null || coordinateSequenceFactory == null)
      throw new IllegalStateException("precision model or coord seq factory can't be null");
    return new GeometryFactory(precisionModel, srid, coordinateSequenceFactory);
  }

  @Override
  public JtsSpatialContext newSpatialContext() {
    return new JtsSpatialContext(this);
  }
}
