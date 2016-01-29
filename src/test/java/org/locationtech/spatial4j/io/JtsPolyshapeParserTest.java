package com.spatial4j.core.io;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.ShapeCollection;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import io.jeo.geom.Geom;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;

import static org.junit.Assert.assertTrue;

public class JtsPolyshapeParserTest  {
    
    @Test
    public void testUseMulti() throws IOException, ParseException {
        String ps = write(Geom.build().point(0,0).point(0,0).toMultiPoint());

        Shape shape = newContext(false).getFormats().getReader(ShapeIO.POLY).read(ps);
        assertTrue(shape instanceof ShapeCollection);

        shape = newContext(true).getFormats().getReader(ShapeIO.POLY).read(ps);
        assertTrue(shape instanceof JtsGeometry);
        assertTrue(((JtsGeometry)shape).getGeom() instanceof MultiPoint);
    }

    JtsSpatialContext newContext(boolean useMulti) {
        JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
        factory.useJtsMulti = useMulti;
        return factory.newSpatialContext();
    }

    String write(Geometry g) {
        Shape shp = JtsSpatialContext.GEO.getShapeFactory().makeShape(g);
        return JtsSpatialContext.GEO.getFormats().getWriter(ShapeIO.POLY).toString(shp);
    }
}
