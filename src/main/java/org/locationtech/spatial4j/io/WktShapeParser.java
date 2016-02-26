/*******************************************************************************
 * Copyright (c) 2015 ElasticSearch and MITRE, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

// A derivative of commit 14bc4dee08355048d6a94e33834b919a3999a06e
//  at https://github.com/chrismale/elasticsearch

package org.locationtech.spatial4j.io;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;

@Deprecated
public class WktShapeParser extends WKTReader {

  public WktShapeParser(SpatialContext ctx, SpatialContextFactory factory) {
    super(ctx,factory);
  }
}