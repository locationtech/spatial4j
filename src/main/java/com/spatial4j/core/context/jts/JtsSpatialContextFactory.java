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

package com.spatial4j.core.context.jts;

import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.io.jts.JtsBinaryCodec;
import com.spatial4j.core.io.jts.JtsWktShapeParser;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;

import java.util.Map;

/**
 * See {@link SpatialContextFactory#makeSpatialContext(java.util.Map, ClassLoader)}.
 * <p/>
 * The following keys are looked up in the args map, in addition to those in the
 * superclass:
 * <DL>
 * <DT>datelineRule</DT>
 * <DD>width180(default)|ccwRect|none
 *  -- see {@link com.spatial4j.core.io.jts.JtsWktShapeParser.DatelineRule}</DD>
 * <DT>validationRule</DT>
 * <DD>error(default)|none|repairConvexHull|repairBuffer0
 *  -- see {@link com.spatial4j.core.io.jts.JtsWktShapeParser.ValidationRule}</DD>
 * <DT>autoIndex</DT>
 * <DD>true|false(default) -- see {@link JtsWktShapeParser#isAutoIndex()}</DD>
 * <DT>allowMultiOverlap</DT>
 * <DD>true|false(default) -- see {@link JtsSpatialContext#isAllowMultiOverlap()}</DD>
 * <DT>precisionModel</DT>
 * <DD>floating(default) | floating_single | fixed
 *  -- see {@link com.vividsolutions.jts.geom.PrecisionModel}.
 * If {@code fixed} then you must also provide {@code precisionScale}
 *  -- see {@link com.vividsolutions.jts.geom.PrecisionModel#getScale()}</DD>
 * </DL>
 */
public class JtsSpatialContextFactory extends SpatialContextFactory {

  protected static final PrecisionModel defaultPrecisionModel = new PrecisionModel();//floating

  //These 3 are JTS defaults for new GeometryFactory()
  public PrecisionModel precisionModel = defaultPrecisionModel;
  public int srid = 0;
  public CoordinateSequenceFactory coordinateSequenceFactory = CoordinateArraySequenceFactory.instance();

  //ignored if geo=false
  public JtsWktShapeParser.DatelineRule datelineRule = JtsWktShapeParser.DatelineRule.width180;

  public JtsWktShapeParser.ValidationRule validationRule = JtsWktShapeParser.ValidationRule.error;
  public boolean autoIndex = false;
  public boolean allowMultiOverlap = false;//ignored if geo=false

  //kinda advanced options:
  public boolean useJtsPoint = true;
  public boolean useJtsLineString = true;

  public JtsSpatialContextFactory() {
    super.wktShapeParserClass = JtsWktShapeParser.class;
    super.binaryCodecClass = JtsBinaryCodec.class;
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
