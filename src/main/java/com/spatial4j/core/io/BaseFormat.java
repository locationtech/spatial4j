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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;



public abstract class BaseFormat implements ShapeFormat {

  //TODO should reference proposed ShapeFactory instead of ctx, which is a point of indirection that
  // might optionally do data validation & normalization
  protected final SpatialContext ctx;
  
  public BaseFormat(SpatialContext ctx) {
    this.ctx = ctx;
  }

  public SpatialContext getCtx() {
    return ctx;
  }
  
  @Override
  public Shape read(Object value, boolean error) throws IOException, ParseException {
    Reader reader = null;
    if(value instanceof Reader) {
      reader = (Reader)value;
    }
    else if(value instanceof InputStream) {
      reader = new InputStreamReader((InputStream)value, "UTF-8");
    }
    else {
      String v = value.toString();
      if(!formatMatchs(v)) {
        if(error) {
          throw new ParseException("Invalid JSON", 0);
        }
        return null;
      }
      reader = new StringReader(v);
    }
    if(error) {
      return read(reader);
    }
    try {
      return read(reader);
    }
    catch(Exception ex) {} // Ignore it
    return null;
  }
  
  @Override
  public String toString(Shape shape) {
    try {
      StringWriter buffer = new StringWriter();
      write(buffer, shape);
      return buffer.toString();
    }
    catch(IOException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  public abstract boolean formatMatchs(String input);

}
