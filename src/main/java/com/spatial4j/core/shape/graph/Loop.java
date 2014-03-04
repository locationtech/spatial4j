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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.spatial4j.core.math.VectorUtils;

import com.spatial4j.core.shape.Vector3D;
import com.spatial4j.core.shape.Rectangle;

/**
 * A loop is a representation of a simple polygon on the surface of a sphere. Vertices
 * are represented as 3D direction cosine vectors (derived from the 3D geocentric point)
 * and are listed counter clockwise with an implicit closure between the last and
 * first vertex on the ring.
 *
 * A loop has:
 *      (1) At least 3 vertices
 *      (2) All vertices of unit length
 *      (3) No duplicate vertices
 *      (4) Non-adjacent edges cannot intersect
 *
 * Various loop modeling algorithms are modeled after the s2Loop implementation in the
 * s2Geometry project which is under Apache (ASL) license. More info on this project
 * can be found at:
 *
 * Link: https://code.google.com/p/s2-geometry-library/
 */
public class Loop {


}
