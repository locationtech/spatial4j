/*******************************************************************************
 * Copyright (c) 2015 ElasticSearch and MITRE, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Contributors:
 *    Chris Male - initial API and implementation
 *    David Smiley
 *    Ryan McKinley
 ******************************************************************************/

// A derivative of commit 14bc4dee08355048d6a94e33834b919a3999a06e
//  at https://github.com/chrismale/elasticsearch

package com.spatial4j.core.io;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.SpatialContextFactory;

@Deprecated
public class WktShapeParser extends WKTReader {

  /** This constructor is required by {@link com.spatial4j.core.context.SpatialContextFactory#makeWktShapeParser(com.spatial4j.core.context.SpatialContext)}. */
  public WktShapeParser(SpatialContext ctx, SpatialContextFactory factory) {
    super(ctx,factory);
  }
}