
1. checkout:
https://lucene-spatial-playground.googlecode.com/svn/branches/geohash/

2. put geonames US.txt data in data/geonames

3. Compile everything:
mvn clean install

4. run demo:
cd spatial-demo
mvn jetty run

5. load test data (for now just geonames)
from: http://localhost:8080
click "load..." at the bottom

this will take a while to index.  Don't worry about speed for this just yet.  
It is currenty indexing the input data 6 different ways w/o optimization

6. Try a polygon query.
my sample tests have been super fast!



-------------------------

To run the demo from eclipse:
1. run mvn eclipse:eclipse

2. From: spatial-demo project run:
org.apache.solr.spatial.demo.StartDemo

