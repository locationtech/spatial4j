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

package com.spatial4j.core.shape.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.*;

import com.spatial4j.core.math.VectorUtils;

// hmmm...search utilities for things in a list of vertices that would be globally useful??

/// TODO Rebecca:
/// TODO: Implement crosser object
/// TODO: implement missing vector utilities
/// TODO: Implement s2 style transformation utilities
/// TODO: needs to rethink some of the access modifiers in this class - particularly those that might
/// TODO: dpbelong in a generic search methods class??

/**
 * There are some additional methods in this class that have not yet been implemented due to some
 * possible organizational changes to support the large number of methods about to be added here!!
 */

/**
 * S2 Implementation of a Geodesic Line defined by a series of points
 * Used in Spatial4j for computing intersections between geodesics
 *
 * Represents a sequence of zero or more vertices connected by straight
 * edges (geodesics)       \
 *
 * Fast utility in 3D - easier to not convert between for performance reasons
 */
public class S2Polyline  {


}
