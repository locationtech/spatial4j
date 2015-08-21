/*******************************************************************************
 * Copyright (c) 2015 Voyager Search
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package com.spatial4j.core.io.jts;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.vividsolutions.jts.geom.Geometry;
import io.jeo.geom.Geom;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class JtsGeoJSONWriterTest {

    JtsGeoJSONWriter writer;

    @Before
    public void setUp() {
        writer = new JtsGeoJSONWriter(JtsSpatialContext.GEO, null);
    }

    @Test
    public void testWritePoint() throws IOException {
        String out = write(Geom.point(1, 2));
        assertEquals("{'type':'Point','coordinates':[1,2]}", out);
    }

    String write(Geometry geom) throws IOException {
        StringWriter w = new StringWriter();
        writer.write(w, geom);
        return w.toString().replaceAll("\"", "'").replaceAll("\\s*", "");
    }
}
