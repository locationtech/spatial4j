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

package com.spatial4j.core.io;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Arrays;

import org.junit.Test;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.Shape;

/**
 * Tests for {@link ShapeFormat}
 */
public class ShapeFormatTest {
  
  public Shape testReadAndWriteTheSame(Shape shape, ShapeFormat format) throws IOException, ParseException {
    StringWriter str = new StringWriter();
    format.write(str, shape);
  //  System.out.println( "OUT: "+str.toString());
    
    Shape out = format.read(new StringReader(str.toString()));
    
    StringWriter copy = new StringWriter();
    format.write(copy, out);
    assertEquals(str.toString(), copy.toString());
    return out;
  }
  
  public void testCommon(SpatialContext ctx, String name) throws Exception {
    ShapeFormat format = ctx.getFormat(name);
    testReadAndWriteTheSame(ctx.makePoint(10, 20),format);
    testReadAndWriteTheSame(ctx.makeLineString(
        Arrays.asList(
            ctx.makePoint(1, 2),
            ctx.makePoint(3, 4),
            ctx.makePoint(5, 6)
        )),format);
    
    testReadAndWriteTheSame(ctx.makeRectangle(10, 20, 30, 40),format);
  }

  @Test
  public void testReadAndWriteTheSame() throws Exception {
//    testCommon(SpatialContext.GEO, GeoJSONFormat.FORMAT);
//    testCommon(JtsSpatialContext.GEO, GeoJSONFormat.FORMAT);
    
    testCommon(SpatialContext.GEO, WKTFormat.FORMAT);
    testCommon(JtsSpatialContext.GEO, WKTFormat.FORMAT);
  }
}
