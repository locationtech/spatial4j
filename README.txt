Overview
  The code is distributed into several modules:

* Spatial-base
The is a core module depended on by other modules; it has almost no dependencies.
Major pieces:
** SpatialContext interface and simple implementation.
** Distance math code.
** Shapes interface and simple implementations.
** Prefix/Grid/Tree interface and implementations.

* Spatial-lucene
Spatial indexing/search implementations using Lucene.
Major interfaces (just one):
** SpatialStrategy (including abstract PrefixGridStrategy)
Major implementations:
** RecursiveGridStrategy
** TermQueryGridStrategy
** PointStrategy

* Spatial-solr
Ads Solr support on top of the Spatial-lucene module.

* Spatatial-test
Test & benchmark code.

* Spatial-extras-base
An extension of Spatial-base that uses the 3rd party JTS library to implement the shapes.
This primarily ads polygon support.

* Spatial-extras-test
More tests on top of Spatial-test that relies on JTS.

* Spatial-extras-demo
Contains a demo web application using Spatial-solr.  Consumes sample data and geonames.
See the provided README.txt in there for instructions to try it out.
