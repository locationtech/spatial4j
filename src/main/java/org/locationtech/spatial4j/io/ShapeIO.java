/*******************************************************************************
 * Copyright (c) 2015 VoyagerSearch
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.io;


public interface ShapeIO {
  public static final String WKT = "WKT";
  public static final String GeoJSON = "GeoJSON";
  public static final String POLY = "POLY";
  public static final String LEGACY = "LEGACY";

  /**
   * @return the format name
   */
  public String getFormatName();
}
