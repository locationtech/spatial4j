/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.distance;


//import static org.locationtech.spatial4j.distance.DistanceUtils.toRadians;
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
