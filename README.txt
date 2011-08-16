Overview
  The code is distributed into several modules:


* Spatial-lucene
The is the heart of the codebase. The code is broadly divided into 'base' and 'strategies'. It also contains benchmark
code and a fair amount of tests.

Major pieces in 'base':
** SpatialContext interface and simple implementation.
** Distance math code.
** Shapes interface and simple implementations.
** Prefix/Grid/Tree interface and implementations.

The "strategies" portion of this module contains spatial indexing/search implementations using Lucene.
Major interfaces (just one):
** SpatialStrategy (including abstract PrefixGridStrategy)
Major implementations:
** RecursiveGridStrategy
** TermQueryGridStrategy
** TwoDoubleStrategy


* Spatial-solr
Ads Solr support on top of the Spatial-lucene module.


* Spatial-extras
An extension of Spatial-base that uses the 3rd party JTS library to implement the shapes.
This primarily ads polygon support and WKT processing.


* Spatial-demo
Contains a demo web application using Spatial-solr.  Consumes sample data and geonames.
See the provided README.txt in there for instructions to try it out.
