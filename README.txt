Overview
  The code is distributed into several modules:

=== MODULES OVERVIEW ===

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
An extension of Spatial-lucene that uses the 3rd party JTS library to implement the shapes.
This primarily ads polygon support and WKT processing.


* Spatial-demo
Contains a demo web application using Spatial-solr.  Consumes sample data and geonames.
See the provided README.txt in there for instructions to try it out.


=== SOLR INSTRUCTIONS ===

The following is a simple set of instructions to use LSP in Solr's trunk example app to get point multi-value support.

# Build
%> mvn clean install

# Copy libs to solr
copy (LSP)/spatial-lucene/target/spatial-lucene-VERSION.jar to (solr)/example/solr/lib/
copy (LSP)/spatial-solr/target/spatial-solr-VERSION.jar to (solr)/example/solr/lib/

# Modify schema.xml
## Comment out existing geohash field type (class=solr.GeoHashField) so that you don't use it.
## Define a field type
  <!-- LSP -->
  <fieldType name="geohash" class="org.apache.solr.spatial.prefix.RecursiveGeohashPrefixTreeFieldType" />
## Make the "store" field use it, and make it multiValued since presumably you want it to be
  <field name="store" type="geohash" indexed="true" stored="true" multiValued="true"/>

# Remove the previous index (solr)/example/solr/data

# Start Solr

# Index sample data

# Query
  http://localhost:8983/solr/select?q=store%3A%22IsWithin(-98+34+-96+36)%22
The q param in un-escaped form is store:"IsWithin(-98 34 -96 36)"
Notes:
  You supply points to index in either "x y" or "y,x" format. You may get out "x y" in results even if you gave "y,x"
which is probably a bug.
  The contents of the IsWithin operation is in this case 4 numbers, which means its a rectangle: minX minY maxX maxY
For a circle (point-radius) use: store:"IsWithin(Circle(-98 34 d=200))"  The first two numbers are x and y (lon lat),
and the d is distance in kilometers.
