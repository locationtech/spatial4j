# Spatial4j Supported Formats

Spatial4j supports reading and writing Shapes to and from strings in the following formats:

 * Well Known Text (WKT)
 * GeoJSON
 * Polyshape

## Reader/Writer API

The classes `ShapeReader` and `ShapeWriter` are the interfaces for encoding 
and decoding shapes respectively. They can be obtained from a `SpatialContext` instance.

    SpatialContext ctx = ...;
    ShapeReader shpReader = ctx.getFormats().getReader(ShapeIO.WKT);
    ShapeWriter shpWriter = ctx.getFormats().getWriter(ShapeIO.WKT);

Reading and writing polygons requires a reader/writer obtained from `JtsSpatialContext`.

## Well Known Text

Well-Known-Text (WKT) is a simple to read text based format for representing spatial objects. It is
defined by the [OGC Simple Feature Specification](http://www.opengeospatial.org/standards/sfa).  [Wikipedia's page](https://en.wikipedia.org/wiki/Well-known_text) on it is pretty decent.

The following table shows the various shape types encoded as WKT:

| Shape           | WKT                                                   |
| ----------------|-------------------------------------------------------|
| Point           | `POINT(1 2)`                                          |
| Rectangle       | `ENVELOPE(1, 2, 4, 3)`      _(minX, maxX, maxY, minY)_|
| Circle          | `BUFFER(POINT(-10 30), 5.2)`                          |
| LineString      | `LINESTRING(1 2, 3 4)`                                |
| Buffered L/S    | `BUFFER(LINESTRING(1 2, 3 4), 0.5)`                   |
| Polygon         | `POLYGON ((1 1, 2 1, 2 2, 1 2, 1 1))`                 |
| ShapeCollection | `GEOMETRYCOLLECTION(POINT(1 2),LINESTRING(1 2, 3 4))` |

The `ENVELOPE` keyword/syntax was borrowed from OGC's [Common Query Language (CQL)](http://docs.geoserver.org/stable/en/user/tutorials/cql/cql_tutorial.html) standard, a superset of WKT.  Note the odd argument order.  It's not widely used.  Alternatively a rectangular polygon can be used, which will be recognized as-such when
it is read and turned into a Rectangle instance.

The `BUFFER` keyword is a Spatial4j extension to the WKT spec.  It's used to produce a circle by buffering a point, and to buffer shapes generally.  The 2nd argument is the buffer distance in degrees.

## GeoJSON

GeoJSON is a format for representing geographic objects using JavaScript Object Notation. It is 
defined by the open standard available at http://geojson.org.

The following list shows the various shape types encoded as GeoJSON:

* Point

        {
          "type": "Point",
          "coordinates": [1, 2]
        }

* Rectangle

        {
          "type": "Polygon",
          "coordinates": [
            [[1,3], [1,4], [2,4], [2,3], [1,3]]
          ]
        }

* Circle

        {
          "type": "Circle", 
          "coordinates": [1, 2], 
          "radius": 111.19508, 
          "properties": {
            "radius_units": "km"
          }
        }

* LineString

        {
          "type": "LineString",
          "coordinates": [[1, 2], [3, 4]]
        }

* Buffered LineString

        {
          "type": "LineString",
          "coordinates": [[1, 2], [3, 4]],
          "buffer": 10
        }

* Polygon

        {
          "type": "Polygon", 
          "coordinates": [
            [[1, 1], [2, 1], [2, 2], [1, 2], [1, 1]]
          ]
        }

* ShapeCollection

        {
          "type": "GeometryCollection", 
          "geometries": [
            {
              "type": "Point", 
              "coordinates": [1, 2]
            }, 
            {
              "type": "LineString", 
              "coordinates": [
                [1, 2],
                [3, 4]
              ]
            }
          ]
        }

## Polyshape

The Polyshape format is an extension to the [Polyline](https://developers.google.com/maps/documentation/utilities/polylinealgorithm) format from Google to include a wider diversity of 
shapes. The standard "Encoded Polyline Algorithm Format" algorithm offers a compact way to encode a
list of lat/lon pairs into a simple ASCII string.

The following table shows the various shape types encoded as Polyshape:

| Shape           | Polyshape                                             |
| ----------------|-------------------------------------------------------|
| Point           | `0_ibE_seK`                                           |
| Rectangle       | `5_ibE_}hQ_ibE_ibE`                                   |
| Circle          | `4(_ibE)_ibE_seK`                                     |
| LineString      | `1_ibE_seK_seK_seK`                                   |
| Buffered L/S    | ``1(_c`|@)_ibE_seK_seK_seK``                          |
| Polygon         | `2_ibE_ibE_ibE??_ibE~hbE??~hbE`                       |
| ShapeCollection | `0_ibE_seK 1_ibE_seK_seK_seK`                         |


### Prefix Key

The Polyshape format uses a prefix key to denote the shape type:

| Shape      | Key |
|------------|:---:|
| Point      | 0   |
| LineString | 1   |
| Polygon    | 2   |
| MultiPoint | 3   |
| Circle     | 4   |
| Rectangle  | 5   |


### Collections

Shape collections are represented as each of the individual shape encodings concatenated with 
a space.


### Arguments

The Polyshape format supports optional arguments to handle things like a Circle (with a radius) and 
a BufferedLineString (with a buffer size). If the character immediately after the prefix is a `'('`, 
everything up to the next `')'` is considered an argument to the Shape.


### Polygons with Holes

A Polygon with interior rings is represented by appending the encoding of each ring prefixed with 
a `')'`:

    '2' + encode(exteriorRing) + ['(' + encode(interiorRing)]*

### Known Limitations

- The format is optimized to store lat/lon points, very big or very small values may get lost in the rounding
- All values are rounded to: Math.round(value * 1e5)
- In the JTS version, a homogeneous ShapeCollection will be read as a MultPoint, MultiLineString, or MultiPolygon

## Benchmarks

The following table shows a comparison among the encoded formats in terms of number of bytes in the
final encoding. Note that this comparison may be somewhat misleading since the other encoding 
formats do not implicitly limit precision as Polyshape does. Percentages are calculated relative to
the largest encoding.

| GeoJSON    | WKT      | Binary    | Polyshape  | Shape |
|:----------:|:--------:|:---------:|:----------:|-------|
| 100% (42)  | 38% (16) |  40% (17) |   20% (9)  | `POINT(100.1 0.1)` |
| 100% (61)  | 52% (33) |  67% (41) |   28% (17) | `LINESTRING (100.1 0.1, 101.1 1.1)` |
| 100% (96)  | 33% (32) |  34% (33) |   18% (17) | `ENVELOPE(100.1, 101.1, 1.1, 0.1)` |
| 90% (158)  | 69% (122)| %100 (177)|   33% (59) | `POLYGON ((100.1 0.1, 101.1 0.1, 101.1 1.1, 100.1 1.1, 100.1 0.1), (100.2 0.2, 100.8 0.2, 100.8 0.8, 100.2 0.8, 100.2 0.2))` |
| 100% (169) | 51% (87) |  %52 (88) |   21% (36) | `GEOMETRYCOLLECTION(LINESTRING (100.1 0.1, 101.1 1.1),LINESTRING (102.1 2.1, 103.1 3.1))` |
| 100% (131) | 40% (53) |  %31 (40) |   15% (20) | `GEOMETRYCOLLECTION(POINT(100.1 0.1),POINT(101.1 1.1))` |

With this limited dataset it looks like the Polyshape format is on average ~25% as big as GeoJSON, 
~50% as big as WKT, and ~40% as big as the binary encoding. The data used for this benchmark 
contains data with low precision numbers (100.1 vs 100.123456) so that may be a low estimate.
