
To try the demonstration application:

(start at the lucene-spatial-playground main directory, which is the parent of where this file is)

1. Compile everything:
%> mvn clean install

2. Run demo:
%> cd spatial-demo
%> mvn jetty:run

3. Load test data
from: http://localhost:8080
click "load..." at the bottom

this will take a while to index.  Don't worry about speed for this just yet.  
It is currenty indexing the input data 6 different ways w/o optimization

4. Try query.
In the "Field", try "geohash", for "Operation" try "intersects", then draw a query shape and click "search".
Notice the "KML" links.  They should open in Google Earth, showing the quad grid.

-------------------------

To run the demo from eclipse:
1. run mvn eclipse:eclipse

2. From: spatial-demo project run:
org.apache.solr.spatial.demo.StartDemo

