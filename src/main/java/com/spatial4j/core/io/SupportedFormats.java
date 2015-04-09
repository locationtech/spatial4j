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

package com.spatial4j.core.io;

import java.util.List;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;

/**
 * Information about the formats a {@link SpatialContext} can read/write
 */
public class SupportedFormats {

  private final List<ShapeReader> readers;
  private final List<ShapeWriter> writers;

  private final ShapeReader wktReader;
  private final ShapeWriter wktWriter;
  
  private final ShapeReader geoJsonReader;
  private final ShapeWriter geoJsonWriter;
  
  public SupportedFormats(List<ShapeReader> readers, List<ShapeWriter> writers) {
    this.readers = readers;
    this.writers = writers;

    wktReader = getReader(ShapeIO.WKT);
    wktWriter = getWriter(ShapeIO.WKT);
    
    geoJsonReader = getReader(ShapeIO.GeoJSON);
    geoJsonWriter = getWriter(ShapeIO.GeoJSON);
  }
  
  public List<ShapeReader> getReaders() {
    return readers;
  }

  public List<ShapeWriter> getWriters() {
    return writers;
  }
  
  public ShapeReader getReader(String fmt) {
    for(ShapeReader f : readers) {
      if(fmt.equals(f.getFormatName())) {
        return f;
      }
    }
    return null;
  }

  public ShapeWriter getWriter(String fmt) {
    for(ShapeWriter f : writers) {
      if(fmt.equals(f.getFormatName())) {
        return f;
      }
    }
    return null;
  }
  
  public ShapeReader getWktReader() {
    return wktReader;
  }

  public ShapeWriter getWktWriter() {
    return wktWriter;
  }

  public ShapeReader getGeoJsonReader() {
    return geoJsonReader;
  }

  public ShapeWriter getGeoJsonWriter() {
    return geoJsonWriter;
  }

  public Shape read(String value) {
    for(ShapeReader format : readers) {
      Shape v = format.readIfSupported(value);
      if(v!=null) {
        return v;
      }
    }
    return null;
  }
}
