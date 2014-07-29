package com.spatial4j.core.shape.jts;

import static java.lang.Math.floor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.GeometryFilter;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;

/**
 * Utilities for date-line logic.
 * 
 * {@link DateLineOps#unwrapDateline(Geometry, double, double)} detects
 * date-line crosses, and "unwraps" the geometry.
 * 
 * {@link DateLineOps#pageGeom(Geometry, double, double)} cuts a geometry, such
 * that each is between xmin and xmax.
 * 
 * Note that much of this code is copied from
 * https://github.com/spatial4j/spatial4j
 * /blob/185132fae6897f57faacdac91c0a98a92e380bfc
 * /src/main/java/com/spatial4j/core/shape/jts/JtsGeometry.java.
 * 
 * A few modifications have been made. Rather than unioning paged geometries,
 * they are merely "collected" into a single geometry (for efficiency's sake).
 * 
 * Moreover, the boundaries of the date-line have been generalized (in order to
 * work with generic projections).
 * 
 * These methods assume that geometries are valid (since they will be performing intersections).
 * Accordingly, the tests for holes-in-shells have been removed.
 * 
 * Fixed a bug in {@link DateLineOps#unwrapDateline(Geometry, double, double)}
 * that invokes {@link Geometry#geometryChanged()} for instances of
 * {@link GeometryCollection}.
 * 
 * Extended to handle concrete {@link GeometryCollection}s.
 * 
 * Extended {@link DateLineOps#unwrapDateline(Geometry, double, double)} to
 * handle coordinates outside of the bounds.
 */
public class DateLineOps {

    /**
     * NOTE: This geometry factory servers as a work-around for a JTS bug. See
     * @see TestJTSIntersectionAndCoordinateFilterBug
     * 
     * http://sourceforge.net/p/jts-topo-suite/mailman/message/32619267/
     */
    private static final GeometryFactory FACTORY = new GeometryFactory(
            new PackedCoordinateSequenceFactory());

    /* If true, keep track of the shift history, and undo it when an unclosed 
     * LinearRing is detected, which can occur when the heuristic for determining
     * a date-line cross fails.
     */
    private static final boolean DETECT_AND_UNDO_BAD_SHIFTS = true;

    //
    // -------------------------------------------------------------- Spatial4j
    //

    /**
     * If <code>geom</code> spans the dateline, then this modifies it to be a
     * valid JTS geometry that extends to the right of the standard -180 to +180
     * width such that some points are greater than +180 but some remain less.
     * Takes care to invoke
     * {@link com.vividsolutions.jts.geom.Geometry#geometryChanged()} if needed.
     * 
     * @return The number of times the geometry spans the dateline. >= 0
     * 
     * @see com.spatial4j.core.shape.jts
     */
    public static int unwrapDateline(
            Geometry geom,
            final double xmin,
            final double xmax) {

        final double w2 = (xmax - xmin) / 2;// half width

        if (geom.getEnvelopeInternal().getWidth() < w2)
            return 0;// can't possibly cross the dateline
        final int[] crossings = {0};// an array so that an inner class can modify it.
        geom.apply(new GeometryFilter() {

            @Override
            public void filter(Geometry geom) {
                int cross = 0;
                if (geom instanceof LineString) {// note: LinearRing extends
                    // LineString
                    if (geom.getEnvelopeInternal().getWidth() < w2)
                        return;// can't possibly cross the dateline
                    cross = unwrapDateline((LineString) geom, xmin, xmax);
                } else if (geom instanceof Polygon) {
                    if (geom.getEnvelopeInternal().getWidth() < w2)
                        return;// can't possibly cross the dateline
                    cross = unwrapDateline((Polygon) geom, xmin, xmax);
                } else
                    return;
                crossings[0] = Math.max(crossings[0], cross);
            }
        });// geom.apply()
        if (crossings[0] > 0 && geom instanceof GeometryCollection) {
            geom.geometryChanged(); // mmww
        }

        return crossings[0];
    }

    /**
     * See {@link #unwrapDateline(Geometry)}.
     * 
     * @see com.spatial4j.core.shape.jts
     */
    private static int unwrapDateline(Polygon poly, double xmin, double xmax) {
        LineString exteriorRing = poly.getExteriorRing();
        int cross = unwrapDateline(exteriorRing, xmin, xmax);
        if (cross > 0) {
            // TODO TEST THIS! Maybe bug if doesn't cross but is in another
            // page? -- MMWW (assume holes are in shell)
            for (int i = 0; i < poly.getNumInteriorRing(); i++) {
                LineString innerLineString = poly.getInteriorRingN(i);
                unwrapDateline(innerLineString, xmin, xmax);

                /* MMWW -- expensive check for validity can be ignored if we assume validity
                 * 
                for (int shiftCount = 0; !exteriorRing.contains(innerLineString); shiftCount++) {
                    if (shiftCount > cross)
                        throw new IllegalArgumentException(
                            "The inner ring doesn't appear to be within the exterior: " +
                                exteriorRing +
                                " inner: " +
                                innerLineString);
                    DateLineOps.shiftGeomByX(innerLineString, xmax - xmin);
                }*/
            }
            poly.geometryChanged();
        }
        return cross;
    }

    /** See {@link #unwrapDateline(Geometry)}. */
    private static int unwrapDateline(
            LineString lineString,
            double xmin,
            double xmax) {

        double w = xmax - xmin;
        double w2 = w / 2;

        CoordinateSequence cseq = lineString.getCoordinateSequence();
        int size = cseq.size();
        if (size <= 1)
            return 0;

        int shiftX = 0;// invariant: == shiftXPage*360
        int shiftXPage = 0;
        int shiftXPageMin = 0/* <= 0 */, shiftXPageMax = 0; /* >= 0 */
        double prevX = cseq.getX(0);

        // track shifts -- if we come across an error, we can undo changes
        // to the geometry
        ShiftHistory shiftHistory = new ShiftHistory(lineString);

        // let the first coordinate choose the default page -- MMWW
        if (prevX < xmin) {
            double pageDelta = w * (floor((xmax - prevX) / w));
            xmin -= pageDelta;
            xmax -= pageDelta;
        } else if (prevX > xmax) {
            double pageDelta = w * (floor((prevX - xmin) / w));
            xmin += pageDelta;
            xmax += pageDelta;
        }

        for (int i = 1; i < size; i++) {
            double thisX_orig = cseq.getX(i);

            // MMWW -- Make sure all coordinates are in bounds so that dateline
            // crossings can be detected.
            {
                double shift = Double.NaN;
                if (thisX_orig < xmin) {
                    shift = w * floor((xmax - thisX_orig) / w);
                } else if (thisX_orig > xmax) {
                    shift = -w * floor((thisX_orig - xmin) / w);
                }

                if (!Double.isNaN(shift)) {
                    thisX_orig += shift;
                    cseq.setOrdinate(i, CoordinateSequence.X, thisX_orig);
                    shiftHistory.record(i, shift);
                }
            }

            //assert thisX_orig >= xmin && thisX_orig <= xmax : "X not in geo bounds"; -- MMWW: too strict now            

            double thisX = thisX_orig + shiftX;
            if (prevX - thisX > w2) {// cross dateline from left to right
                thisX += w;
                shiftX += w;
                shiftXPage += 1;
                shiftHistory.record(i, w);
                shiftXPageMax = Math.max(shiftXPageMax, shiftXPage);
            } else if (thisX - prevX > w2) {// cross dateline from right to left
                thisX -= w;
                shiftX -= w;
                shiftXPage -= 1;
                shiftHistory.record(i, -w);
                shiftXPageMin = Math.min(shiftXPageMin, shiftXPage);
            }
            if (shiftXPage != 0)
                cseq.setOrdinate(i, CoordinateSequence.X, thisX);
            prevX = thisX;
        }
        if (lineString instanceof LinearRing) {
            boolean isClosed = cseq.getCoordinate(0)
                    .equals(cseq.getCoordinate(size - 1));

            if (DETECT_AND_UNDO_BAD_SHIFTS && !isClosed) {
                shiftHistory.undo();
                shiftXPageMax = shiftXPageMin = shiftXPage = 0;
								isClosed = cseq.getCoordinate(0)
                    .equals(cseq.getCoordinate(size - 1));
            }

            assert isClosed;
            assert shiftXPage == 0;// starts and ends at 0
        }
        assert shiftXPageMax >= 0 && shiftXPageMin <= 0;
        // Unfortunately we are shifting again; it'd be nice to be smarter and
        // shift once
        //DateLineOps.shiftGeomByX(lineString, shiftXPageMin * -w);
        int crossings = shiftXPageMax - shiftXPageMin;
        if (crossings > 0)
            lineString.geometryChanged();
        return crossings;
    }

    /**
     * Cuts the geometry into pieces, each of which is in between xmin and xmax.
     * This implies that the geometries "wrap".
     * Generally invoked after {@link DateLineOps#unwrapDateline(Geometry, double, double)}
     */
    public static Geometry pageGeom(Geometry geom, double xmin, double xmax) {

        if (geom.getClass() == GeometryCollection.class) {
            GeometryCollection gc = (GeometryCollection) geom;
            int n = gc.getNumGeometries();
            List<Geometry> geometries = new ArrayList<Geometry>(n);
            for (int i = 0; i < n; i++) {
                geometries.add(pageGeom(gc.getGeometryN(i), xmin, xmax));
            }
            return collect(geometries, geom.getFactory());
        }

        double w = xmax - xmin;

        Envelope geomEnv = geom.getEnvelopeInternal();
        if (geomEnv.getMinX() >= xmin && geomEnv.getMaxX() <= xmax) {
            return geom;
        }

        int page = (int) Math.floor((geomEnv.getMinX() - xmin) / (xmax - xmin));

        List<Geometry> geomList = new ArrayList<Geometry>();
        // page 0 is the standard xmin to xmax range
        for (; true; page++) {
            double minX = xmin + page * w;
            if (geomEnv.getMaxX() <= minX) {
                break;
            }
            Geometry rect =
                    FACTORY.toGeometry(new Envelope(
                            minX,
                            minX + w,
                            geomEnv.getMinY(),
                            geomEnv.getMaxY()));
            Geometry pageGeom = rect.intersection(geom);// JTS is doing some
            // hard work

            DateLineOps.shiftGeomByX(pageGeom, page * -w);
            geomList.add(pageGeom);
        }

        return collect(geomList, FACTORY);
    }

    private static void shiftGeomByX(Geometry geom, final double xShift) {
        if (xShift == 0) {
            return;
        }
        geom.apply(new CoordinateSequenceFilter() {

            @Override
            public void filter(CoordinateSequence seq, int i) {
                seq.setOrdinate(i, CoordinateSequence.X, seq.getX(i) + xShift);
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public boolean isGeometryChanged() {
                return true;
            }
        });
    }

    /**
     * Like {@link GeometryFactory#buildGeometry(Collection)}, except that
     * Multi* may be flattened.
     * 
     * In general, the tightest possible type is chosen, except that the
     * presence of a concrete GeometryCollection (i.e. not a sub-class) will
     * always result in one, even if all geometries are otherwise of the same type.
     */
    static Geometry collect(
            Collection<Geometry> geometries,
            GeometryFactory factory) {

        if (geometries.isEmpty()) {
            return factory.createGeometryCollection(new Geometry[] {});
        }

        boolean hasPoint = false;
        boolean hasLineString = false;
        boolean hasPolygon = false;
        boolean hasHomogenousCollection = false;
        boolean hasHeterogenousCollection = false;
        int sizeForDump = geometries.size();

        for (Geometry g : geometries) {
            if (g instanceof Point) {
                hasPoint = true;

            } else if (g instanceof MultiPoint) {
                hasPoint = true;
                hasHomogenousCollection = true;
                sizeForDump += ((GeometryCollection) g).getNumGeometries() - 1;

            } else if (g instanceof LineString) {
                hasLineString = true;

            } else if (g instanceof MultiLineString) {
                hasLineString = true;
                hasHomogenousCollection = true;
                sizeForDump += ((GeometryCollection) g).getNumGeometries() - 1;

            } else if (g instanceof Polygon) {
                hasPolygon = true;

            } else if (g instanceof MultiPolygon) {
                hasPolygon = true;
                hasHomogenousCollection = true;
                sizeForDump += ((GeometryCollection) g).getNumGeometries() - 1;

            } else if (g instanceof GeometryCollection) {
                hasHeterogenousCollection = true;

            } else if (g == null) {
                throw new NullPointerException("Null geometry");

            } else {
                throw new UnsupportedOperationException(
                        "Unsupported geometry: " + g.getGeometryType());
            }
        }

        int types =
                (hasPoint ? 1 : 0) + (hasLineString ? 1 : 0) + (hasPolygon ? 1 : 0);

        if (types != 1 || hasHeterogenousCollection) {
            return factory.createGeometryCollection(GeometryFactory.toGeometryArray(geometries));
        }

        if (hasHomogenousCollection) {
            List<Geometry> dumped = new ArrayList<Geometry>(sizeForDump);
            for (Geometry g : geometries) {
                dump(g, dumped);
            }
            geometries = dumped;
        }

        if (sizeForDump == 1) {
            return geometries.iterator().next();

        } else if (hasPoint) {
            return factory.createMultiPoint(GeometryFactory.toPointArray(geometries));

        } else if (hasLineString) {
            return factory.createMultiLineString(GeometryFactory.toLineStringArray(geometries));

        } else if (hasPolygon) {
            return factory.createMultiPolygon(GeometryFactory.toPolygonArray(geometries));

        } else {
            throw new IllegalStateException("Should never happen.");
        }
    }

    /**
     * If the geometry is a {@link GeometryCollection}, recursively invoke dump
     * for each element. Otherwise, add the geometry to the given collection.
     */
    private static void dump(Geometry g, Collection<Geometry> geometries) {
        if (g instanceof GeometryCollection) {
            GeometryCollection collection = (GeometryCollection) g;
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                dump(collection.getGeometryN(i), geometries);
            }

        } else {
            geometries.add(g);
        }
    }

    private static class ShiftHistory {

        private final LineString lineString; 

        /* Marks the beginning of an interval, inclusive, in which coordinates 
         * are shifted. 
         */
        private LinkedHashMap<Integer, Double> startShiftByIndex;

        /* Marks the end of an interval, inclusive, in which coordinates are 
         * shifted. 
         */
        private LinkedHashMap<Integer, Double> endShiftByIndex;

        public ShiftHistory(LineString lineString) {
            this.lineString = lineString;
        }

        /**
         * Record a shift of 'shift' at index 'i'.
         */
        private void record(
                int i, double shift) {

            if (DETECT_AND_UNDO_BAD_SHIFTS) {

                checkInit();
                Double endShift = endShiftByIndex.get(i - 1);
                if (endShift == null || endShift != shift) {
                    // start a new interval
                    startShiftByIndex.put(i, shift);
                    endShiftByIndex.put(i, shift);
                } else {
                    // extend the current interval
                    endShiftByIndex.remove(i - 1); 
                    endShiftByIndex.put(i, shift);
                }
            }
        }

        private void undo() {
            if (checkInit()) {
                throw new IllegalStateException("Nothing to undo!");
            }

            CoordinateSequence cseq = lineString.getCoordinateSequence();
            Iterator<Entry<Integer, Double>> startItr = startShiftByIndex.entrySet().iterator();
            Iterator<Entry<Integer, Double>> endItr = endShiftByIndex.entrySet().iterator();

            while (startItr.hasNext()) {
                Entry<Integer, Double> start = startItr.next();
                Entry<Integer, Double> end = endItr.next();
                if (!start.getValue().equals(end.getValue())) {
                    throw new IllegalStateException("Error in shift-history interval tracking.");
                }

                double shift = start.getValue();
                for (int i = start.getKey(), j = end.getKey(); i <= j; i++) {
                    cseq.setOrdinate(i, CoordinateSequence.X, cseq.getX(i)
                            - shift);
                }
            }
        }

        private boolean checkInit() {
            if (startShiftByIndex == null) {
                // lazy initialization
                startShiftByIndex = new LinkedHashMap<Integer, Double>();
                endShiftByIndex = new LinkedHashMap<Integer, Double>();
                return true;
            }
            return false;
        }
    }
}
