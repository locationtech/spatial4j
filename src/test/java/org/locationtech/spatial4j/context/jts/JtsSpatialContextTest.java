/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.context.jts;

import org.locationtech.spatial4j.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;
import io.jeo.geom.Geom;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class JtsSpatialContextTest {

    @Test
    public void testDatelineRule() {
        Polygon polygon = Geom.build().points(-179, -90, 179, -90, 179, 90, -179, 90).toPolygon();

        JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
        factory.datelineRule = DatelineRule.width180;

        JtsSpatialContext ctx = factory.newSpatialContext();
        JtsGeometry shp = ctx.makeShape((Polygon) polygon.clone());
        assertTrue(shp.getGeom() instanceof GeometryCollection);

        factory.datelineRule = DatelineRule.none;
        ctx = factory.newSpatialContext();
        shp = ctx.makeShape(polygon);
        assertTrue(shp.getGeom() instanceof Polygon);
    }
}
