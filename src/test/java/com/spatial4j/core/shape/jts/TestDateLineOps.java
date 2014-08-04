package com.spatial4j.core.shape.jts;

import static com.spatial4j.core.shape.jts.DateLineOps.unwrapDateline;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class TestDateLineOps {

    private static final GeometryFactory F = new GeometryFactory();

    @Test
    public void testPolygon() throws ParseException {
        double xmin = -180, xmax = 180;

        Geometry poly = fromWKT("POLYGON ((-179 0, 179 0, 178 3, -179 0))");
        Assert.assertEquals(DateLineOps.unwrapDateline(poly, xmin, xmax), 1);
        assertEquals("POLYGON ((-179 0, -181 0, -182 3, -179 0))",
                poly.toString());

        poly = DateLineOps.pageGeom(poly, xmin, xmax);
        assertEquals(
                "MULTIPOLYGON (((180 1, 180 0, 179 0, 178 3, 180 1)), ((-180 0, -180 1, -179 0, -180 0)))",
                poly.toString());
    }

    @Test
    public void testSphericalRingAKAAntarctica() throws ParseException {
        double xmin = -180, xmax = 180;
        final String wkt = "POLYGON ((-180 -1, -180 0, -90 0, 0 0, 90 0, 180 0, 180 -1, -180 -1))";
        Geometry poly = fromWKT(wkt);
        Assert.assertEquals(unwrapDateline(poly, xmin, xmax), 0);
        assertEquals(
                wkt,
                poly.toString());

        poly = DateLineOps.pageGeom(poly, xmin, xmax);
        assertEquals(
                wkt,
                poly.toString());
    }

    @Test
    public void testSegementizedSphericalRingAKAAntarctica()
            throws ParseException {
        double xmin = -180, xmax = 180;

        String segmentized = "POLYGON ((-180 -1, -180 0, -60 0, 60 0, 180 0, 180 -1, 60 -1, -60 -1, -180 -1))";

        Geometry poly = fromWKT(segmentized);
        Assert.assertEquals(unwrapDateline(poly, xmin, xmax), 0);
        assertEquals(segmentized, poly.toString());

        poly = DateLineOps.pageGeom(poly, xmin, xmax);
        assertEquals(segmentized, poly.toString());
    }

    @Test
    public void testShiftedPolygon() throws ParseException {
        double xmin = -180, xmax = 180;

        Geometry poly = fromWKT("POLYGON ((-539 0, -181 0, -182 3, -539 0))");
        Assert.assertEquals(1, unwrapDateline(poly, xmin, xmax));
        assertEquals("POLYGON ((-539 0, -541 0, -542 3, -539 0))",
                poly.toString());

        poly = DateLineOps.pageGeom(poly, xmin, xmax);
        assertEquals(
                "MULTIPOLYGON (((180 1, 180 0, 179 0, 178 3, 180 1)), ((-180 0, -180 1, -179 0, -180 0)))",
                poly.toString());
    }

    @Test
    public void testMultiPolygon() throws ParseException {
        double xmin = -180, xmax = 180;

        String nonsense = "((1 0, 0 0, 0 1, 1 0))";
        Geometry geom = fromWKT("MULTIPOLYGON (((-179 0, 179 0, 178 3, -179 0)), "
                + nonsense + ")");
        Assert.assertEquals(unwrapDateline(geom, xmin, xmax), 1);
        assertEquals("MULTIPOLYGON (((-179 0, -181 0, -182 3, -179 0)), "
                + nonsense + ")", geom.toString());

        geom = DateLineOps.pageGeom(geom, xmin, xmax);
        assertEquals(
                "MULTIPOLYGON (((180 1, 180 0, 179 0, 178 3, 180 1)), ((-180 0, -180 1, -179 0, -180 0)), "
                        + nonsense + ")", geom.toString());
    }

    @Test
    public void testGeometryCollection() throws ParseException {
        double xmin = -180, xmax = 180;

        String nonsense = "((1 0, 0 0, 0 1, 1 0))";
        Geometry geom = fromWKT("GEOMETRYCOLLECTION (POINT (0 0), MULTIPOLYGON (((-179 0, 179 0, 178 3, -179 0)), "
                + nonsense + "))");
        Assert.assertEquals(unwrapDateline(geom, xmin, xmax), 1);
        assertEquals(
                "GEOMETRYCOLLECTION (POINT (0 0), MULTIPOLYGON (((-179 0, -181 0, -182 3, -179 0)), "
                        + nonsense + "))", geom.toString());

        geom = DateLineOps.pageGeom(geom, xmin, xmax);
        assertEquals(
                "GEOMETRYCOLLECTION (POINT (0 0), MULTIPOLYGON (((180 1, 180 0, 179 0, 178 3, 180 1)), ((-180 0, -180 1, -179 0, -180 0)), "
                        + nonsense + "))", geom.toString());
    }

    @Test
    public void testNegativePage() {
        assertEquals(
                "MULTIPOLYGON (((179 1, 180 1, 180 0, 179 0, 179 1)), ((-180 0, -180 1, -179 1, -179 0, -180 0)))",
                DateLineOps
                        .pageGeom(F.toGeometry(new Envelope(-181, -179, 0, 1)),
                                -180, 180).toString());
    }

    @Test
    public void testCollectPolygonAndMultiPolygon() {
        GeometryFactory f = new GeometryFactory();
        Geometry p = box(0, 0, 1, 1);
        Geometry mp = f.createMultiPolygon(new Polygon[] { box(1, 1, 2, 2),
                box(2, 2, 3, 3) });

        mp = DateLineOps.collect(Arrays.asList(p, mp), f);
        Assert.assertEquals(3, mp.getNumGeometries());
        Assert.assertEquals("MultiPolygon", mp.getGeometryType());
    }

    @Test
    public void testCollectNestedGeometryCollection() {
        GeometryFactory f = new GeometryFactory();
        Geometry p = box(0, 0, 1, 1);
        Geometry mp = f.createMultiPolygon(new Polygon[] { box(1, 1, 2, 2),
                box(2, 2, 3, 3) });
        Geometry gc = f.createGeometryCollection(new Geometry[] { box(0, 0, 1,
                1) });

        mp = DateLineOps.collect(Arrays.asList(p, mp, gc), f);
        Assert.assertEquals(3, mp.getNumGeometries());
        Assert.assertEquals("GeometryCollection", mp.getGeometryType());
    }

    @Test
    public void testCollectSingletonMultiPolygon() {
        GeometryFactory f = new GeometryFactory();
        Geometry mp = f.createMultiPolygon(new Polygon[] { box(0, 0, 1, 1) });
        mp = DateLineOps.collect(Arrays.asList(mp), f);
        Assert.assertEquals("Polygon", mp.getGeometryType());
    }

    // @Test -- Too ambiguous to realistically account for?
    public void testWideRectangle() throws ParseException {
        double xmin = -180, xmax = 180;

        Geometry poly = box(-180, -1, 180, 1);
        Assert.assertEquals(unwrapDateline(poly, xmin, xmax), 1);
        assertEquals(
                "POLYGON ((-180 -1, -180 0, -90 0, 0 0, 90 0, 180 0, 180 -1, -180 -1))",
                poly.toString());

        poly = DateLineOps.pageGeom(poly, xmin, xmax);
        assertEquals(
                "POLYGON ((-180 -1, -180 0, -90 0, 0 0, 90 0, 180 0, 180 -1, -180 -1))",
                poly.toString());
    }

    private static Geometry fromWKT(String wkt) throws ParseException {
        return new WKTReader().read(wkt);
    }

   //Note: the arg order is inconsistent with both makeRect and ENVELOPE
    private static Polygon box(double xmin, double ymin, double xmax,
            double ymax) {
        return (Polygon) F.toGeometry(new Envelope(xmin, xmax, ymin, ymax));
    }
}