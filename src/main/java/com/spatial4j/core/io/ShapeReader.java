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

package com.spatial4j.core.io;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

import com.spatial4j.core.shape.Shape;


public interface ShapeReader extends ShapeIO {
  /**
   * @param value -- the input value
   * @param error -- flag if we should throw an error or not (true will throw an error)
   * @return a shape or null, if we could not make one
   * @throws ParseException 
   */
  public Shape read(Object value, boolean error) throws IOException, ParseException;
  
  /**
   * Read an input stream
   */
  public Shape read(Reader reader) throws IOException, ParseException;
}
