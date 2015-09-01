# Spatial4j Supported Formats

Spatial4j supports reading and writing Shapes to and from strings

 * WKT
 * GeoJSON
 * Polyshape (custom format based on https://developers.google.com/maps/documentation/utilities/polylinealgorithm)


## Polyshape

The standard 'Encoded Polyline Algorithm Format' offers a compact way to encode a list of lat/lon pairs into a simple ASCII string.  In Spatial4j, we have extended this format to support a wider diversity of shapes.  In particular an encoded string has:


### Prefix Key
a prefix key defining the Shape type (point, line, polygon, etc)
```
  KEY_POINT      = '0'
  KEY_LINE       = '1'
  KEY_POLYGON    = '2'
  KEY_MULTIPOINT = '3'
  KEY_CIRCLE     = '4'
  KEY_BOX        = '5'
```

### Arguments
The format supports an optional arguments to support things like Circle (with radius) and BufferedLineString (buffer size)  If the character immediatly after the prefix is a '(', everythign up to the next ')' is considered an argument to the Shape

### Polygons with Holes
Polygons are encoded as:
'2'+encode(exterior ring)[')'encode(interior ring)]*

### Spaces
Each Shape in a geometry collection is seperated by a space


## Polyline Examples & tests
In general, it looks like this format is ~25% as big as GeoJSON and ~50% as big as WKT.  This is with data with low precision numbers (100.1 vs 100.123456) so that may be a low estimate

For this data, it looks like the polyshape format is ~1/3-~1/2 the size as the binary codec.  

Format | bytes | %poly | encoded
------ | ----- | ----- | -------
 binary | 17 | 53% | ...
GeoJSON | 42 | 21% | {"type":"Point","coordinates":[100.1,0.1]}
WKT | 16 | 56% | POINT(100.1 0.1)
POLY | 9 | 100% | 0_x}aR_pR


Format | bytes | %poly | encoded
------ | ----- | ----- | -------
 binary | 41 | 41% | ...
GeoJSON | 61 | 28% | {"type":"LineString","coordinates":[[100.1,0.1],[101.1,1.1]]}
WKT | 33 | 52% | LINESTRING (100.1 0.1, 101.1 1.1)
POLY | 17 | 100% | 1_x}aR_pR_ibE_ibE


Format | bytes | %poly | encoded
------ | ----- | ----- | -------
 binary | 33 | 52% | ...
GeoJSON | 96 | 18% | {"type":"Polygon","coordinates":[[[100.1,0.1],[100.1,1.1],[101.1,1.1],[101.1,0.1],[100.1,0.1]]]}
WKT | 32 | 53% | ENVELOPE(100.1, 101.1, 1.1, 0.1)
POLY | 17 | 100% | 5_x}aR_pR_ibE_ibE


Format | bytes | %poly | encoded
------ | ----- | ----- | -------
 binary | 177 | 33% | ...
GeoJSON | 158 | 37% | {"type":"Polygon","coordinates":[[[100.1,0.1],[101.1,0.1],[101.1,1.1],[100.1,1.1],[100.1,0.1]],[[100.2,0.2],[100.8,0.2],[100.8,0.8],[100.2,0.8],[100.2,0.2]]]}
WKT | 122 | 48% | POLYGON ((100.1 0.1, 101.1 0.1, 101.1 1.1, 100.1 1.1, 100.1 0.1), (100.2 0.2, 100.8 0.2, 100.8 0.8, 100.2 0.8, 100.2 0.2))
POLY | 59 | 100% | 2_x}aR_pR_ibE??_ibE~hbE??~hbE(_iqbR_af@_etB??_etB~dtB??~dtB


Format | bytes | %poly | encoded
------ | ----- | ----- | -------
 binary | 88 | 41% | ...
GeoJSON | 169 | 21% | {"type":"GeometryCollection","geometries": [{"type":"LineString","coordinates":[[100.1,0.1],[101.1,1.1]]},{"type":"LineString","coordinates":[[102.1,2.1],[103.1,3.1]]}]}
WKT | 87 | 41% | GEOMETRYCOLLECTION(LINESTRING (100.1 0.1, 101.1 1.1),LINESTRING (102.1 2.1, 103.1 3.1))
POLY | 36 | 100% | 1_x}aR_pR_ibE_ibE 1_ldnR_dyK_ibE_ibE


Format | bytes | %poly | encoded
------ | ----- | ----- | -------
 binary | 40 | 50% | ...
GeoJSON | 131 | 15% | {"type":"GeometryCollection","geometries": [{"type":"Point","coordinates":[100.1,0.1]},{"type":"Point","coordinates":[101.1,1.1]}]}
WKT | 53 | 38% | GEOMETRYCOLLECTION(POINT(100.1 0.1),POINT(101.1 1.1))
POLY | 20 | 100% | 0_x}aR_pR 0_bahR_zuE


Format | bytes | %poly | encoded
------ | ----- | ----- | -------
 binary | 33 | 52% | ...
GeoJSON | 96 | 18% | {"type":"Polygon","coordinates":[[[100.1,0.1],[100.1,1.1],[101.1,1.1],[101.1,0.1],[100.1,0.1]]]}
WKT | 32 | 53% | ENVELOPE(100.1, 101.1, 1.1, 0.1)
POLY | 17 | 100% | 5_x}aR_pR_ibE_ibE




## Known limitations
- the format is optimized to store lat/lon points, very big or very small values may get lost in the rounding
- all values are rounded to: Math.round(value * 1e5);
- In the JTS version, a homogenous ShapeCollection will be read as MultPoint/MultiLine/MultiPoly etc

