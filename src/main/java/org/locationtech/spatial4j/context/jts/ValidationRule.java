/*******************************************************************************
 * Copyright (c) 2015 MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.context.jts;

import org.locationtech.spatial4j.io.ShapeReader;

/**
 * Indicates how JTS geometries (notably polygons but applies to other geometries too) are
 * validated (if at all) and repaired (if at all).  This setting usually only applies to
 * {@link ShapeReader}.
 */
public enum ValidationRule {
  /**
   * Geometries will not be validated (because it's kinda expensive to calculate). You may or may
   * not ultimately get an error at some point; results are undefined. However, note that
   * coordinates will still be validated for falling within the world boundaries.
   *
   * @see org.locationtech.jts.geom.Geometry#isValid()
   */
  none,

  /**
   * Geometries will be explicitly validated on creation, possibly resulting in an exception:
   * {@link org.locationtech.spatial4j.exception.InvalidShapeException}.
   */
  error,

  /**
   * Invalid Geometries are repaired by taking the convex hull. The result will very likely be a
   * larger shape that matches false-positives, but no false-negatives. See
   * {@link org.locationtech.jts.geom.Geometry#convexHull()}.
   */
  repairConvexHull,

  /**
   * Invalid polygons are repaired using the {@code buffer(0)} technique. From the <a
   * href="https://locationtech.github.io/jts/jts-faq.html">JTS FAQ</a>:
   * <p>
   * The buffer operation is fairly insensitive to topological invalidity, and the act of
   * computing the buffer can often resolve minor issues such as self-intersecting rings. However,
   * in some situations the computed result may not be what is desired (i.e. the buffer operation
   * may be "confused" by certain topologies, and fail to produce a result which is close to the
   * original. An example where this can happen is a "bow-tie: or "figure-8" polygon, with one
   * very small lobe and one large one. Depending on the orientations of the lobes, the buffer(0)
   * operation may keep the small lobe and discard the "valid" large lobe).
   * </p>
   */
  repairBuffer0
}
