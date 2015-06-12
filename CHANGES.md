## VERSION 0.4

DATE: 20 January 2014

### User/API changes & Notes:

 * It used to be the case that rectangular polygons provided in WKT had to have its vertexes given
   in counter-clockwise order to indicate which way around the earth it went. The default is now
   the shorter width (less than 180 degrees). This setting can be changed via the “datelineRule”
   setting. To avoid ambiguity, just use the ENVELOPE syntax.

 * Unless you refer to the SpatialContext.GEO or JtsSpatialContext.GEO instances, the only true way
   to create a context is to use the SpatialContextFactory (and including the JTS based subclass),
   which have a host of settings that make Spatial4j very customizable.

 * SpatialContext.readShape(String) and toString(Shape) are still deprecated but will be
   removed in the next release. You should instead read WKT via the new method
   readShapeFromWkt(String). This also means the worldBounds initialization via SpatialContextFactory
   should be specified as an ENVELOPE WKT. Spatial4j will no longer provide a way to generate WKT
   from a shape although it’s pretty easy to use JTS for that.

 * In Spatial4j’s older JTS based implementation, it used to be the case that when reading WKT,
   latitudes would be normalized into the -90 to 90 range; it’s now an error. Longitudes outside of
   -180 to 180 were also normalized and this is now an error too. Longitude normalization can be
   enabled with the “normWrapLongitude” option.

 * Newly deprecated: ParseUtils, some methods and constants in DistanceUtils (which seemed only used
   by Solr), and SpatialContext constructors other than that which takes a SpatialContextFactory.
   In the "io" package, these classes were **deleted**:  LineReader, SampleData..., Geoname...
   Copies of those were moved to the _Spatial Solr Sandbox_ project.

### Features:

 * New built-in WKT parser separate from JTS’s. JTS is no longer required for WKT but still is for
   certain shapes like polygons. It supports the ENVELOPE rectangle syntax seen in OGC’s CQL spec,
   and it has a custom BUFFER(shape, distance) that can be used to buffer a shape. A buffered point
   is a circle. Parse via SpatialContext.readShapeFromWkt(String). JTS’s WKT parser can be used as
   an alternative via the JtsWKTReaderShapeParser class.

 * New ShapeCollection shape. It’s similar to a JTS/OGC GeometryCollection and is used to hold
   multiple shapes of the same or different types.
   
 * New BufferedLine & BufferedLineString shapes. A 0 buffer results in effectively Line &
   LineString shapes. A non-0 buffer is buffered in a rectangular corner fashion (i.e. a rectangle
   on an angle). This new shape does not yet support geodesics (e.g. the dateline), and so JTS’s
   equivalents are used by default for now.
   
 * JtsGeometry can now be “indexed”.  It builds an internal index to speed up subsequent
   calculations. It can be done automatically when read from WKT via the new “autoIndex” option.
   
 * JtsGeometry shapes when read via WKT have configurable validation and automatic repair via a
   couple algorithms. See the new “validationRule” setting.
   
 * Shapes can now be “empty”; see Shape.isEmpty().
 
 * Configurable dateline crossing algorithm: none, width180, ccwRect
 
 * JTS’s PrecisionModel is configurable from the JtsSpatialContextFactory.
 
 * A new “BinaryCodec” is added which is a binary format for shapes; see
   SpatialContext.getBinaryCodec(). In this release it’s a pretty straight-forward implementation,
   but might get optimized for more compactness in the future. When using JTS and the
   “floating_single” PrecisionModel, it will use 4-byte floats instead of 8-byte doubles for shapes
   other than JtsGeometry (i.e. non-polygons). JtsGeometry is written in WKB format which is always
   8-byte doubles. In the future, WKB will not likely be used.
   
 * New SpatialContext.calcDistance convenience methods to avoid referencing DistanceCalculator
 
 * New DistanceCalculator.withinDistance method used by Euclidean circle avoids a Math.sqrt call.
 
 * DistanceUtils.DEG_TO_KM & KM_TO_DEG convenience constants

### Bugs:

 * Geodetic circles sometimes computed the wrong relationship with a rectangle.
 
 * JtsGeometry now calculates the minimum geodetic bounding box (also used by ShapeCollection).
   This fixed an issue where Lucene-spatial would give Fiji a much courser grid granularity than it
   deserved.
   
 * JtsGeometry shapes that had > 180 width sometimes resulted in an exception. (#41 & SOLR-4879)
 
 * Converting a Euclidean circle to a polygonal geometry was wrong. (#44)
 
 * DistanceUtil.vectorDistance for Manhattan style was wrong. (LUCENE-3814)
 
 * More consistently wrap shape parsing errors with InvalidShapeException or ParseException as
   applicable.
