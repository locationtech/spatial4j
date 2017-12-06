/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.locationtech.spatial4j.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * Geometry module utility module.
 *
 * <p>
 * Class taken from http://github.com/jeo/jeo, Nov 2017 by Justin Deoliveira.  
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Geom {

    /**
     * static default factory
     */
    public final static GeometryFactory factory = new GeometryFactory();

    /**
     * Geometry type enumeration.
     */
    public enum Type {
        POINT(Point.class),
        LINESTRING(LineString.class),
        POLYGON(Polygon.class),
        MULTIPOINT(MultiPoint.class),
        MULTILINESTRING(MultiLineString.class),
        MULTIPOLYGON(MultiPolygon.class),
        GEOMETRY(Geometry.class),
        GEOMETRYCOLLECTION(GeometryCollection.class);
        
        private final Class<? extends Geometry> type;
        private final String name;
        private final String simpleName;
        
        Type(Class<? extends Geometry> type) {
            this.type = type;
            this.name = type.getSimpleName();
            this.simpleName = (name.startsWith("Multi") ? name.substring(5) : name);
        }
        
        /**
         * Return the {@code Geometry} class associated with this type.
         *
         * @return the {@code Geometry} class
         */
        public Class<? extends Geometry> getType() {
            return type;
        }
        
        /**
         * Equivalent to {@linkplain #getName()}.
         *
         * @return the name of this type
         */
        @Override
        public String toString() {
            return name;
        }
        
        /**
         * Return a name for this type that is suitable for text descriptions.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Get the 'simple name'. Returns the same value as {@linkplain #getName()}
         * except for MULTIPOINT, MULTILINESTRING and MULTIPOLYGON, for which it returns
         * the name without the 'Multi' prefix.
         *
         * @return the simple name
         */
        public String getSimpleName() {
            return simpleName;
        }
        
        /**
         * Get the {@code Geometries} for the given object.
         *
         * @param geom a JTS Geometry object
         *
         * @return the {@code Geometries} for the argument's class, or {@code null}
         *         if the argument is {@code null}
         */
        public static Type from(Geometry geom) {
            if (geom != null) {
                return from(geom.getClass());
            }
        
            return null;
        }
        
        /**
         * Get the {@code Geometries} for the given {@code Geometry} class.
         *
         * @param geomClass the class
         *
         * @return the constant for this class
         */
        public static Type from(Class<?> geomClass) {
            for (Type gt : Type.values()) {
                if (gt.type == geomClass) {
                    return gt;
                }
            }
            
            //no direct match look for a subclass
            Type match = null;
        
            for (Type gt : Type.values()) {
                if (gt == GEOMETRY || gt == GEOMETRYCOLLECTION) {
                    continue;
                }
                
                if (gt.type.isAssignableFrom(geomClass)) {
                    if (match == null) {
                        match = gt;
                    } else {
                        // more than one match
                        return null;
                    }
                }
            }
            
            if (match == null) {
                //no matches from concrete classes, try abstract classes
                if (GeometryCollection.class.isAssignableFrom(geomClass)) {
                    return GEOMETRYCOLLECTION;
                }
                if (Geometry.class.isAssignableFrom(geomClass)) {
                    return GEOMETRY;
                }
            }
            
            return match;
        }
        
        /**
         * Get the {@code Geometries} for the specified name.
         * 
         * @param name The name of the geometry, eg: "POINT"
         * 
         * @return The constant for the name.
         */
        public static Type from(String name) {
            for (Type gt : Type.values()) {
                if (gt.getName().equalsIgnoreCase(name)) {
                    return gt;
                }
            }
            return null;
        }
    }

    /**
     * Creates a new geometry builder.
     */
    public static GeomBuilder build() {
        return new GeomBuilder();
    }

    /**
     * Convenience method to build a Point geometry.
     */
    public static Point point(double x, double y) {
        return build().point(x, y).toPoint();
    }

    /**
     * Convenience method to build a LineString geometry.
     * 
     * @param ord Even number of ordinates forming coordinates for the line string.
     */
    public static LineString lineString(double... ord) {
        return build().points(ord).toLineString();
    }

    /**
     * Convenience method to build a Polygon geometry.
     * 
     * @param ord Even number of ordinates forming coordinates for the outer ring of the polygon.
     */
    public static Polygon polygon(double... ord) {
        return build().points(ord).toPolygon();
    }

    /**
     * Returns an iterable over the points of a multipoint.
     */
    public static Iterable<Point> iterate(MultiPoint mp) {
        return new GeometryIterable<Point>(mp);
    }

    /**
     * Returns an iterable over the lines of a multilinestring.
     */
    public static Iterable<LineString> iterate(MultiLineString ml) {
        return new GeometryIterable<LineString>(ml);
    }

    /**
     * Returns an iterable over the polygons of a multipolygon.
     */
    public static Iterable<Polygon> iterate(MultiPolygon mp) {
        return new GeometryIterable<Polygon>(mp);
    }
    
    /**
     * Returns an iterable over the geometries of a geometry collection.. 
     */
    public static Iterable<Geometry> iterate(GeometryCollection gc) {
        return new GeometryIterable<Geometry>(gc);
    }

    /**
     * Returns an iterable over the interior rings of a polygon.
     */
    public static Iterable<LineString> holes(final Polygon p) {
        return new Iterable<LineString>() {
            int i = 0;

            @Override
            public Iterator<LineString> iterator() {
                return new Iterator<LineString>() {
                    @Override
                    public boolean hasNext() {
                        return i < p.getNumInteriorRing();
                    }

                    @Override
                    public LineString next() {
                        return p.getInteriorRingN(i++);
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Returns the first point in a multi point, or <code>null</code> if the multi point is empty.
     */
    public static Point first(MultiPoint mp) {
        return mp.getNumGeometries() > 0 ? (Point) mp.getGeometryN(0) : null;
    }

    /**
     * Returns the first line in a multi line, or <code>null</code> if the multi line is empty.
     */
    public static LineString first(MultiLineString ml) {
        return ml.getNumGeometries() > 0 ? (LineString) ml.getGeometryN(0) : null;
    }

    /**
     * Returns the first polygon in a multi polygon, or <code>null</code> if the multi polygon is empty.
     */
    public static Polygon first(MultiPolygon mp) {
        return mp.getNumGeometries() > 0 ? (Polygon) mp.getGeometryN(0) : null;
    }

    /**
     * Returns the geometries of a geometry collection as an array.
     */
    public static <T extends Geometry> T[] array(GeometryCollection gc, T[] array) {
        for (int i =0 ; i < gc.getNumGeometries(); i++) {
            array[i] = (T) gc.getGeometryN(i);
        }
        return array;
    }

    /**
     * Retypes (ie. narrows) a geometry collection if possible.
     * <p>
     * If the geometry contains a single object it is narrowed to that object. If the geometry collection is
     * homogeneous it is narrowed to the appropriate sub collection type. Otherwise the collection is returned
     * as is.
     * </p>
     *
     * @see {@link GeometryFactory#buildGeometry(Collection)}
     */
    public static <T extends Geometry> T narrow(GeometryCollection gc) {

        if (gc.getNumGeometries() == 0) {
            return (T) gc;
        }

        List<Geometry> objects = new ArrayList<>(gc.getNumGeometries());
        for (Geometry g : iterate(gc)) {
            objects.add(g);
        }

        return (T) gc.getFactory().buildGeometry(objects);
    }

    /**
     * Recursively flattens a geometry collection into it's constituent geometry objects.
     */
    public static <T extends Geometry> List<T> flatten(GeometryCollection gc) {
        return flatten(Collections.singletonList((Geometry)gc));
    }

    /**
     * Recursively flattens a list of geometries into it's constituent geometry objects.
     */
    public static <T extends Geometry> List<T> flatten(List<Geometry> gl) {
        List<T> flat = new ArrayList<>();
        LinkedList<Geometry> q = new LinkedList<>();
        q.addAll(gl);

        while (!q.isEmpty()) {
            Geometry g = q.removeFirst();
            if (g instanceof GeometryCollection) {
                for (Geometry h : iterate((GeometryCollection)g)) {
                    q.addLast(h);
                }
            }
            else {
                flat.add((T) g);
            }
        }

        return flat;
    }

    /**
     * Recursively unwraps a geometry collection containing single object.
     */
    public static Geometry singlify(Geometry geom) {
        while (geom instanceof GeometryCollection && geom.getNumGeometries() == 1) {
            geom = geom.getGeometryN(0);
        }
        return geom;
    }

    /*
     * Private iterable class.
     */
    private static class GeometryIterable<T extends Geometry> implements Iterable<T> {

        GeometryCollection gc;

        GeometryIterable(GeometryCollection gc) {
            this.gc = gc;
        }

        @Override
        public Iterator<T> iterator() {
            return new GeometryIterator<T>(gc);
        }
    }

    /*
     * Private iterator class.
     */
    private static class GeometryIterator<T extends Geometry> implements Iterator<T> {

        int i = 0; 
        GeometryCollection gc;

        GeometryIterator(GeometryCollection gc) {
            this.gc = gc;
        }

        @Override
        public boolean hasNext() {
            return i < gc.getNumGeometries();
        }

        @SuppressWarnings("unchecked")
        @Override
        public T next() {
            return (T) gc.getGeometryN(i++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Creates a prepared geometry.
     * <p>
     * Prepared geometries make operations like intersection must faster.
     * </p>
     */
    public static PreparedGeometry prepare(Geometry g) {
        return PreparedGeometryFactory.prepare(g);
    }

    /**
     * Converts a geometry object into the associated geometry collection. For
     * example Polygon to MultiPolygon.
     * <p>
     *  If the input is already a collection it is returned as is.
     * </p>
     */
    public static GeometryCollection multi(Geometry g) {
        switch(Geom.Type.from(g)) {
            case POINT:
                return factory.createMultiPoint(new Point[]{(Point)g});
            case LINESTRING:
                return factory.createMultiLineString(new LineString[]{(LineString)g});
            case POLYGON:
                return factory.createMultiPolygon(new Polygon[]{(Polygon)g});
            default:
                return (GeometryCollection) g;
        }
    }
}
