package org.locationtech.spatial4j.io;

import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeCollection;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;
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
