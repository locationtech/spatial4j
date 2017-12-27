/*******************************************************************************
 * Copyright (c) 2015 MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.context.jts;

/**
 * Indicates the algorithm used to process JTS Polygons and JTS LineStrings for detecting dateline
 * (aka anti-meridian) crossings. It only applies when geo=true.
 */
public enum DatelineRule {
  /** No polygon will cross the dateline. */
  none,

  /**
   * Adjacent points with an x (longitude) difference that spans more than half way around the
   * globe will be interpreted as going the other (shorter) way, and thus cross the dateline.
   */
  width180, // TODO is there a better name that doesn't have '180' in it?

  /**
   * For rectangular polygons, the point order is interpreted as being counter-clockwise (CCW).
   * However, non-rectangular polygons or other shapes aren't processed this way; they use the
   * {@link #width180} rule instead. The CCW rule is specified by OGC Simple Features
   * Specification v. 1.2.0 section 6.1.11.1.
   */
  ccwRect
}
