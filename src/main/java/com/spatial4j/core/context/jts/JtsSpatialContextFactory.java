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
import com.vividsolutions.jts.geom.GeometryFactory;

import java.util.Map;

/**
 * See {@link SpatialContextFactory#makeSpatialContext(java.util.Map,
 * ClassLoader)}.
 */
public class JtsSpatialContextFactory extends SpatialContextFactory {

  protected boolean autoValidate = true;
  protected boolean autoPrepare = false;
  protected boolean allowMultiOverlap = false;
  protected GeometryFactory geometryFactory;

  @Override
  protected void init(Map<String, String> args, ClassLoader classLoader) {
    super.init(args, classLoader);

    String autoValidateStr = args.get("autoValidate");
    if (autoValidateStr != null)
      this.autoValidate = Boolean.parseBoolean(autoValidateStr);

    String autoPrepareStr = args.get("autoPrepare");
    if (autoPrepareStr != null)
      this.autoPrepare = Boolean.parseBoolean(autoPrepareStr);

    String allowMultiOverlapStr = args.get("allowMultiOverlap");
    if (allowMultiOverlapStr != null)
      this.allowMultiOverlap = Boolean.parseBoolean(allowMultiOverlapStr);
  }

  public void setGeometryFactory(GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setAutoValidate(boolean autoValidate) {
    this.autoValidate = autoValidate;
  }

  public void setAutoPrepare(boolean autoPrepare) {
    this.autoPrepare = autoPrepare;
  }

  public void setAllowMultiOverlap(boolean allowMultiOverlap) {
    this.allowMultiOverlap = allowMultiOverlap;
  }

  @Override
  public JtsSpatialContext newSpatialContext() {
    return new JtsSpatialContext(this);
  }
}
