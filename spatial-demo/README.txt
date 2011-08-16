
To try the demonstration application:

(start at the lucene-spatial-playground main directory, which is the parent of where this file is)

1. Put geonames US.txt data in data/geonames

2. Compile everything:
%> mvn clean install

3. Run demo:
%> cd spatial-extras-demo
%> mvn jetty:run

4. Load test data
from: http://localhost:8080
click "load..." at the bottom

this will take a while to index.  Don't worry about speed for this just yet.  
It is currenty indexing the input data 6 different ways w/o optimization

5. Try query.
In the "Field", try "geohash", for "Operation" try "intersects", then draw a query shape and click "search".
Notice the "KML" links.  They should open in Google Earth, showing the quad grid.

-------------------------

To run the demo from eclipse:
1. run mvn eclipse:eclipse

2. From: spatial-demo project run:
org.apache.solr.spatial.demo.StartDemo

