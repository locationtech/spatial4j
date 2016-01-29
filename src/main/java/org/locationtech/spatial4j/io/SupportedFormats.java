/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io;

import java.util.List;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.Shape;

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
