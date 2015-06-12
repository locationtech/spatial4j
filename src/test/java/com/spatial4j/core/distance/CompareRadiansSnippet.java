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
package com.spatial4j.core.distance;


//import static com.spatial4j.core.distance.DistanceUtils.toRadians;
import static java.lang.Math.toRadians;

/**
 * On my machine, using 
 *  Math.toRadians: 2090
 *  DistanceUtils:   626
 */
public class CompareRadiansSnippet {
  
  public static void main(String[] args) {
    long start = System.currentTimeMillis();
    double x = 1.12345;
    for (int i=0; i<100000000; i++) {
      x += toRadians(x) - toRadians(x+1); 
    }    
    System.out.println(x); // need to use the result... otherwise JVM may skip everything
    System.out.println(System.currentTimeMillis()-start);
  }
}
