## VERSION 0.8

DATE: _unreleased_

* \#177: Improve conversion of a Circle to Shape. JtsShapeFactory allows converting from a Shape
  object to a JTS Geometry object. Geodetic circles now translate to a polygon that has points
  equidistant from the center. Before the change, there was potentially a large inaccuracy.
  (Hrishi Bakshi)
  
* \#163: "Empty" points in JTS are now convertible to a Spatial4j Shape instead of throwing an exception.
  (David Smiley)

* \#162: Fixed WKT & GeoJSON \[de\]serialization of "empty" points and geometrycollections.
  (Jeen Broekstra, David Smiley)

* \#165: Added ShapeFactory.pointLatLon convenience method.
 (MoeweX)

* \#167: WKTWriter now has a means to customize the NumberFromat.
 (MoeweX)
 
* \#175: ShapesAsWKTModule, a Jackson databind module,
 didn't deserialize WKT inside JSON to a Spatial4j Shape at all.
 Now it does.  It continues to serialize correctly.
 (David Smiley)
  
## VERSION 0.7

DATE: 27 December 2017

* \#153: Upgraded to JTS 1.15.  This JTS release has an API breakage in that the package root was changed
  from com.vividsolutions to org.locationtech but should otherwise be compatible.
  JTS is now dual-licensed as EPL 1.0 and EDL 1.0 (a BSD style license).
  This JTS release also included various improvements, including faster LineString intersection.
  (David Smiley)

* \#138: Feature: Added integration for the Jackson-databind ("ObjectMapper") serialization library.
  It's a popular library to serialize/deserialize JSON, XML, and other formats to/from Java objects.
  Jackson is an optional dependency for Spatial4j that is only required for this feature.
  (Ryan McKinley)

* \#151: Moved application of JtsContext.autoIndex from JtsShapeFactory.makeShapeFromGeometry() to
  JtsSpatialContext.makeShape is which is more central.
  (Justin Deoliveira)

* \#155: Ensure that JTS Geometry objects passed to Spatial4j are never modified.  If Spatial4j wants to
  do so in order to dateline wrap it, then it'll now be cloned first (at some new cost, which we try to avoid).
  (David Smiley)

* 408c14a7: Bug: JtsShapeFactory.lineString: if useJtsLineString (true by default) and we have a non-0 buffer
  then a buffer of 0 was applied instead of the intended buffer.
  (Ryan McKinley)

* \#152: Removed test dependency on jeo library; some classes were copied in.
  (Justin Deoliveira)


## VERSION 0.6

DATE: 26 February 2016

### Notes:

* Package change from com.spatial4j.core to org.locationtech.spatial4j. Also, maven coordinates change from groupId
  com.spatial4j to org.locationtech.spatial4j.  (David Smiley)

### Features:

* \#130: New ShapeFactory interface for shape creation. Related methods on SpatialContext are now deprecated; get the
  ShapeFactory from the SpatialContext.  ShapeFactory has builders for Polygon, LineString, MultiShape, MultiPoint,
  MultiLineString, and MultiPolygon.  The ShapeReader formats now use these and thus no longer have hard dependencies
  on JTS just to create a polygon, although should you need to create a polygon, you still need JTS on the classpath at
  this time.  A new JtsSpatialContextFactory.useJtsMulti option (defaults to true) toggles whether JTS's Multi* 
  implementations are to be used in preference to Spatial4j's native ShapeCollection.
  (David Smiley, Justin Deoliveira)

### Bugs:

 * If tried to use an "empty" JTS geometry when geo=false, it would throw an exception. (David Smiley)

 * \#127: JtsGeometry.relate(Circle) was incorrect. (David Smiley)

---------------------------------------

## VERSION 0.5

DATE: 18 September 2015

### User/API changes & Notes:

 * \#96: Java 1.7 is now the minimum version supported by Spatial4j.
 
 * Spatial4j supports more formats now; see "FORMATS.md".  More info below.
 
 * \#107: The DatelineRule and ValidationRule enums were moved to the com.spatial4j.context.jts package, and the
   current setting is exposed on JtsSpatialContext. The autoIndex boolean was moved there too.
   These things used to be defined in JtsWktShapeParser. (Justin Deoliveira & David Smiley)
 
 * \#92: Shape now exposes the SpatialContext
   
 * Given a JTS Geometry instance and the desire to have a Spatial4j equivalent Shape, consider
   using JtsSpatialContext.createShapeFromGeometry(geometry) vs. makeShape(geometry).  See the
   javadocs.
   
 * WktShapeParser was renamed to WKTReader, although WktShapeParser still exists as a deprecated
   subclass of WKTReader.  SpatialContext.getWktShapeParser returns WKTReader.  JtsWktShapeParser
   was renamed to JtsWKTReader.
   
 * \#90: Publish the test-jar. (Nick Knize)    
 
 * The internal class "Range" is deprecated, unused, and will be removed in a future release.

### Features:

 * \#91: SpatialContext.getFormats() returns a new SupportedFormats which lists the supported
   Shape formats for readers & writers.  (Ryan McKinley)

 * \#94: WKT writing support. (Ryan McKinley)

 * \#94: GeoJSON format support; both reading & writing. (Ryan McKinley, Justin Deoliveira)
 
 * \#117: Polyshape format support; both reading & writing. (Ryan McKinley, Justin Deoliveira)
 
 * \#98: SpatialPredicate class -- moved from Lucene Spatial. (Ryan McKinley)
 
### Improvements:
 
 * \#103: JtsGeometry now accepts GeometryCollection if it's component geometry types are
   homogeneous. (Justin Deoliveira)  
   note: for non-homogeneous ones, see JtsSpatialContext.createShapeFromGeometry
 
 * \#97: More consistent use of ParseException vs InvalidShapeException (Ryan McKinley)

### Bugs:

 * \#104: DistanceUtils.distHaversineRAD could return NaN given anti-podal points. (David Smiley)
 
 * \#77: When ShapeCollection & JtsGeometry computed the bounding box for multiple shapes
   longitudinally, it could sometimes produce a world-wrap longitude instead of the minimal
   enclosing span. (David Smiley)

 * \#86: Rectangle.getBuffered() in geo mode was sometimes slightly off in the southern hemisphere.
   (David Smiley)
 
 * \#85: Vertical line Rectangles at the dateline should relate with another such Rectangle
   consistently if one is declared at -180 longitude and the other at +180 longitude.  Before this
   fix, INTERSECTS would sometimes be returned instead of CONTAINS or WITHIN more accurately.
   (David Smiley)


---------------------------------------

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
