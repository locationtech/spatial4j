/*******************************************************************************
 * Copyright (c) 2015 Voyager Search and MITRE
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

package org.locationtech.spatial4j.context.jts;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.Polygon;
import org.junit.Test;
import org.locationtech.spatial4j.shape.jts.JtsShapeFactory;
import org.locationtech.spatial4j.util.Geom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JtsSpatialContextTest {

    @Test
    public void testDatelineRule() {
        // rectangle enclosing the dateline
        Polygon polygon = Geom.build().points(-179, -90, 179, -90, 179, 90, -179, 90).toPolygon();

        JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
        factory.datelineRule = DatelineRule.width180;
        JtsSpatialContext ctx = factory.newSpatialContext();
        final Polygon polygonCloned = (Polygon) polygon.copy();
        JtsGeometry shp = ctx.makeShape(polygonCloned);
        assertEquals("shouldn't be modified after calling makeShape", polygon, polygonCloned);
        assertTrue(shp.getGeom() instanceof GeometryCollection);

        factory.datelineRule = DatelineRule.none;
        ctx = factory.newSpatialContext();
        shp = ctx.makeShape(polygon);
        assertTrue(shp.getGeom() instanceof Polygon);
    }

    @Test
    public void testDatelineRuleWithMultiPolygon() {
        JtsSpatialContext ctx = new JtsSpatialContextFactory().newSpatialContext();
        JtsShapeFactory shapeFactory = ctx.getShapeFactory();
        GeometryFactory geomFactory = shapeFactory.getGeometryFactory();

        // rectangle enclosing the dateline
        Polygon poly1Geom = Geom.build().points(-179, -90, 179, -90, 179, 90, -179, 90).toPolygon();
        // simple triangle
        Polygon poly2Geom = Geom.build().points(0, 0, 1, 1, 1, 0, 0, 0).toPolygon();

        GeometryCollection geomColl = geomFactory.createGeometryCollection(
                new Geometry[]{poly1Geom, poly2Geom});
        JtsGeometry jtsGeometry = shapeFactory.makeShape(geomColl);
        // one of them is split; other is unchanged
        assertEquals("MULTIPOLYGON (" +
                "((-180 -90, -180 90, -179 90, -179 -90, -180 -90)), " +
                "((179 90, 180 90, 180 -90, 179 -90, 179 90)), " +
                "((0 0, 1 1, 1 0, 0 0))" +
                ")", jtsGeometry.toString());
    }
}
