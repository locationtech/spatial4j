# Spatial4j

Spatial4j is a general purpose spatial / geospatial [ASL](http://www.apache.org/licenses/LICENSE-2.0.html) licensed open-source Java library. It's core capabilities are 3-fold: to provide common shapes that can work in Euclidean and geodesic (surface of sphere) world models, to provide distance calculations and other math, and to read shapes from [WKT](http://en.wikipedia.org/wiki/Well-known_text) formatted strings.  Spatial4j is a [pending](http://www.locationtech.org/proposals/spatial4j) member of the [LocationTech](http://www.locationtech.org) Industry Working Group family of projects.

If you are working with spatial grid-square indexing schemes, be it [Geohash](http://en.wikipedia.org/wiki/Geohash) or something custom, then you are likely to find especially high utility from Spatial4j -- doubly-so for Apache Software Foundation projects due to restrictions on use of LGPL software there.

## Shapes and Other Features

The main part of Spatial4j is its collection of shapes.  Shapes in Spatial4j have these features:

* Compute its lat-lon bounding box.
* Compute an area.  For some shapes its more of an estimate.
* Compute if it contains a provided point.
* Compute the relationship to a lat-lon rectangle. Relationships are: CONTAINS, WITHIN, DISJOINT, INTERSECTS.  Note that Spatial4j doesn't have a notion of "touching".

Spatial4j has a variety of shapes that operate in Euclidean-space -- i.e. a flat 2D plane.  Most shapes are augmented to support a wrap-around at `X` -180/+180 for compatibility with latitude & longitudes, which is effectively a cylindrical model.  But the real bonus is its circle (i.e. point-radius shape that can operate on a surface-of-a-sphere model.  See below for further info.  I often use the term "geodetic" or "geodesic" or "geo" as synonymous with that model but technically those words have a more broad meaning.

| Shape      | Euclidean | Cylindrical | Spherical|
| -----------|:---------:|:-----------:|:--------:|
| **Point**      | Y     | Y           | Y        |
| **Rectangle**  | Y     | Y           | Y        |
| **Circle**     | Y     | N           | Y        |
| **LineString** | Y     | N           | N        |
| **Buffered L/S** | Y   | N           | N        |
| **Polygon**    | Y     | Y           | N        |
| **ShapeCollection** | Y | Y          | Y        |

* The Rectangle shape exists in the spherical model as a lat-lon rectangle, which basically means it's math is no different than cylindrical.
* Polygons don't support pole-wrap (sorry, no Antarctica polygon); just dateline-cross.  Polygons are supported by wrapping JTS's `Geometry`, which is to say that most of the fundamental logic for that shape is implemented by JTS.

### Other Features

* Parse shapes from strings in [WKT](http://en.wikipedia.org/wiki/Well-known_text) to include the ENVELOPE extension from CQL, plus a Spatial4j custom BUFFER operation. Buffering a point gets you a Circle.
* 3 great-circle distance calculators: Law of Cosines, Haversine, Vincenty
* The code is well tested and it's monitored via [Travis-CI](http://travis-ci.org/#!/spatial4j/spatial4j) continuous integration.
* Spatial4j has no dependencies on other libraries except for JTS, which is only triggered if you use Polygons, or obviously if you use any of the classes prefixed with "Jts".

## Why not use JTS? Why should you use Spatial4j?

Spatial4j was born out of an unmet need from other open-source Java software.

[JTS](https://sourceforge.net/projects/jts-topo-suite/) is the most popular spatial library in Java. JTS is powerful but it only supports Euclidean geometry (no geodesics), it has no Circle shape, and it is LGPL licensed (which is planned to change soon).  Spatial4j has a geodesic circle implementation, and it wraps JTS geometries to add dateline-wrap support (no pole wrap yet).

A geodesic circle implementation (i.e. point-radius on surface of a sphere), has been non-trivial; see for yourself and look at the extensive testing.  Presumably many applications will use a polygon substitute for a circle, however note that not only is it an approximation, but common algorithms *inscribe* instead of *circumscribe* the circle. The result is a polygon that doesn't quite completely cover the intended shape, potentially resulting in not finding desired data when applied to the information-retrieval domain (i.e. indexing/search in Apache Lucene) where it is usually better to find a false match versus not find a positive match when making approximations.  Also, Spatial4j's implementation goes to some lengths to be efficient by only calculating the great-circle-distance a minimum number of times in order to find the intersection relationship with a rectangle.  Even computing the bounding-box of this shape was non-obvious, as the initial algorithm lifted from the web at a popular site turned out to be false.

## Getting Started

**[Javadoc API](https://spatial4j.github.io/spatial4j/apidocs/)**

The facade to all of Spatial4j is the [`SpatialContext`](https://spatial4j.github.io/spatial4j/apidocs/com/spatial4j/core/context/SpatialContext.html). It acts as a factory for shapes and it holds references to most other classes you might use and/or it has convenience methods for them.  For example you can get a [`DistanceCalculator`](https://spatial4j.github.io/spatial4j/apidocs/com/spatial4j/core/distance/DistanceCalculator.html) but if you just want to calculate the distance then the context has a method for that.

To get a SpatialContext (or just "context" for short), you could use a global singleton `SpatialContext.GEO` or `JtsSpatialContext.GEO` with both use geodesic surface-of-sphere calculations (when available); the JTS one principally adds Polygon support. If you want a non-geodesic implementation or you want to customize one of many options, then instantiate a [`SpatialContextFactory`](https://spatial4j.github.io/spatial4j/apidocs/com/spatial4j/core/context/SpatialContextFactory.html) (or `JtsSpatialContextFactory`), set the options, then invoke `newSpatialContext()`. If you have a set of name-value string pairs, perhaps from a java properties file, then instead use the static `makeSpatialContext(map, classLoader)` method which adds a lot of flexibility to the configuration initialization versus hard-coding it.

*You should generally avoid calling constructors for anything in Spatial4j except for the `SpatialContextFactory`.* Constructors aren't strictly forbidden but the factories are there to provide an extension point / abstraction, so don't side-step them unless there's a deliberate reason.

## Miscellaneous

Discuss Spatial4j on our [mailing list](http://spatial4j.16575.n6.nabble.com/).

View metadata about the project as generated by Maven: [maven site](https://spatial4j.github.io/spatial4j/).

Spatial4j has been ported to .NET (C#) where it is appropriately named [Spatial4n](https://github.com/synhershko/Spatial4n).

### Future Road Map Ideas

* Support for projections by incorporating Proj4j
* More surface-of-sphere implemented shapes (LineString, Polygon)
* Polygon pole wrap
* Multi-dimensional?

### History

Before Spatial4j, there was [Lucene Spatial Playground](http://code.google.com/p/lucene-spatial-playground/) (LSP) and from this work a generic core spatial library emerged, independent of Lucene: Spatial4j. The other parts of LSP were either merged into Lucene / Solr itself or were migrated to
[Spatial Solr Sandbox](https://github.com/ryantxu/spatial-solr-sandbox).