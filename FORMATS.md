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

```
{ "test": [
{"wkt" :"POINT(100.1 0.1)",
 "json":{"type":"Point","coordinates":[100.1,0.1]},
 "poly":"0_x}aR_pR",
 "compare_json": "9 vs 42 chars. 21%",
 "compare_wkt" : "9 vs 16 chars. 56%",
 "compare_bin" : "9 vs 17 bytes. 53%"
},
{"wkt" :"LINESTRING (100.1 0.1, 101.1 1.1)",
 "json":{"type":"LineString","coordinates":[[100.1,0.1],[101.1,1.1]]},
 "poly":"1_x}aR_pR_ibE_ibE",
 "compare_json": "17 vs 61 chars. 28%",
 "compare_wkt" : "17 vs 33 chars. 52%",
 "compare_bin" : "17 vs 41 bytes. 41%"
},
{"wkt" :"POLYGON ((100.1 0.1, 101.1 0.1, 101.1 1.1, 100.1 1.1, 100.1 0.1))",
 "json":{"type":"Polygon","coordinates":[[[100.1,0.1],[101.1,0.1],[101.1,1.1],[100.1,1.1],[100.1,0.1]]]},
 "poly":"2_x}aR_pR_ibE??_ibE~hbE??~hbE",
 "compare_json": "29 vs 96 chars. 30%",
 "compare_wkt" : "29 vs 65 chars. 45%",
 "compare_bin" : "29 vs 93 bytes. 31%"
},
{"wkt" :"POLYGON ((100.1 0.1, 101.1 0.1, 101.1 1.1, 100.1 1.1, 100.1 0.1), (100.2 0.2, 100.8 0.2, 100.8 0.8, 100.2 0.8, 100.2 0.2))",
 "json":{"type":"Polygon","coordinates":[[[100.1,0.1],[101.1,0.1],[101.1,1.1],[100.1,1.1],[100.1,0.1]],[[100.2,0.2],[100.8,0.2],[100.8,0.8],[100.2,0.8],[100.2,0.2]]]},
 "poly":"2_x}aR_pR_ibE??_ibE~hbE??~hbE(_iqbR_af@_etB??_etB~dtB??~dtB",
 "compare_json": "59 vs 158 chars. 37%",
 "compare_wkt" : "59 vs 122 chars. 48%",
 "compare_bin" : "59 vs 177 bytes. 33%"
},
{"wkt" :"MULTILINESTRING ((100.1 0.1, 101.1 1.1), (102.1 2.1, 103.1 3.1))",
 "json":{"type":"MultiLineString","coordinates":[[[100.1,0.1],[101.1,1.1]],[[102.1,2.1],[103.1,3.1]]]},
 "poly":"1_x}aR_pR_ibE_ibE 1_ldnR_dyK_ibE_ibE",
 "compare_json": "36 vs 94 chars. 38%",
 "compare_wkt" : "36 vs 64 chars. 56%",
 "compare_bin" : "36 vs 91 bytes. 40%"
},
{"wkt" :"MULTIPOINT ((100.1 0.1), (101.1 1.1))",
 "json":{"type":"MultiPoint","coordinates":[[100.1,0.1],[101.1,1.1]]},
 "poly":"3_x}aR_pR_ibE_ibE",
 "compare_json": "17 vs 61 chars. 28%",
 "compare_wkt" : "17 vs 37 chars. 46%",
 "compare_bin" : "17 vs 51 bytes. 33%"
},
{"wkt" :"ENVELOPE(100.1, 101.1, 1.1, 0.1)",
 "json":{"type":"Polygon","coordinates":[[[100.1,0.1],[100.1,1.1],[101.1,1.1],[101.1,0.1],[100.1,0.1]]]},
 "poly":"5_x}aR_pR_ibE_ibE",
 "compare_json": "17 vs 96 chars. 18%",
 "compare_wkt" : "17 vs 32 chars. 53%",
 "compare_bin" : "17 vs 33 bytes. 52%"
}]}
``` 

## Known limitations
- the format is optimized to store lat/lon points, very big or very small values may get lost in the rounding
- all values are rounded to: Math.round(value * 1e5);
- In the JTS version, a homogenous ShapeCollection will be read as MultPoint/MultiLine/MultiPoly etc

