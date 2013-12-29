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
import com.spatial4j.core.io.JtsWktShapeParser;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;

import java.util.Map;

/**
 * See {@link SpatialContextFactory#makeSpatialContext(java.util.Map,
 * ClassLoader)}.
 * <p/>
 * The following keys are looked up in the args map, in addition to those in the
 * superclass:
 * <DL>
 * <DT>autoValidate</DT>
 * <DD>true(default)|false -- see {@link JtsSpatialContext#isAutoValidate()}</DD>
 * <DT>autoPrepare</DT>
 * <DD>true|false(default) -- see {@link JtsSpatialContext#isAutoPrepare()}</DD>
 * <DT>allowMultiOverlap</DT>
 * <DD>true|false(default) -- see {@link JtsSpatialContext#isAllowMultiOverlap()}</DD>
 * <DT>precisionModel</DT>
 * <DD>floating(default) | floating_single | fixed
 * -- see {@link com.vividsolutions.jts.geom.PrecisionModel}.
 * If {@code fixed} then you must also provide {@code precisionScale}
 * -- see {@link com.vividsolutions.jts.geom.PrecisionModel#getScale()}</DD>
 * </DL>
 */
public class JtsSpatialContextFactory extends SpatialContextFactory {

  protected static final PrecisionModel defaultPrecisionModel = new PrecisionModel();//floating

  //These 3 are JTS defaults for new GeometryFactory()
  public PrecisionModel precisionModel = defaultPrecisionModel;
  public int srid = 0;
  public CoordinateSequenceFactory coordinateSequenceFactory = CoordinateArraySequenceFactory.instance();

  public boolean autoValidate = true;
  public boolean autoPrepare = false;
  public boolean allowMultiOverlap = false;
  
  public JtsSpatialContextFactory() {
    super.wktShapeParserClass = JtsWktShapeParser.class;
  }
  
  @Override
  protected void init(Map<String, String> args, ClassLoader classLoader) {
    super.init(args, classLoader);

    initField("autoValidate");
    initField("autoPrepare");
    initField("allowMultiOverlap");

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
    return new GeometryFactory(precisionModel, srid, coordinateSequenceFactory);
  }

  @Override
  public JtsSpatialContext newSpatialContext() {
    return new JtsSpatialContext(this);
  }
}
