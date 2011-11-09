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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO -- i think there is a more standard way to approach this problem
 */
public class SpatialContextProvider {
  static final Logger log = LoggerFactory.getLogger(SpatialContextProvider.class);

  private static SpatialContext instance = null;

  private SpatialContextProvider() {
  }

  @SuppressWarnings("unchecked")
  public static synchronized SpatialContext getContext() {
    if (instance != null) {
      return instance;
    }

    String cname = System.getProperty("SpatialContextProvider");
    if (cname != null) {
      try {
        Class<? extends SpatialContext> clazz = (Class<? extends SpatialContext>) Class.forName(cname);
        instance = clazz.newInstance();
        return instance;
      } catch (Exception e) {
        //don't log full stack trace
        log.warn("Using default SpatialContext because: " + e.toString());
      }
    }
    instance = new SimpleSpatialContext();
    return instance;
  }

  static synchronized void clear() {
    instance = null;
  }
}
