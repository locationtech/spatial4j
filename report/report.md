# Report for assignment 3

This is a template for your report. You are free to modify it as needed.
It is not required to use markdown for your report either, but the report
has to be delivered in a standard, cross-platform format.

## Project

Name: Spatial4j

URL: https://github.com/locationtech/spatial4j

Spatial4j is a general purpose spatial / geospatial ASL licensed open-source Java library. It's core capabilities are 3-fold: to provide common shapes that can work in Euclidean and geodesic (surface of sphere) world models, to provide distance calculations and other math, and to read & write shapes from formats like WKT and GeoJSON. 

## Onboarding experience

Did it build as documented?
    
No issues with the onboarding. Just cloning the repo and importing the project into and IDE like intelliJ did the trick
for most of the members of the group.


## Complexity

| Function                                           | CCN (lizard) | CCN Manual |
|----------------------------------------------------|:------------:|-----------:|
| PolyshapeWriter::write                             |      13      |            |
| Geometry::unwrapDateline                           |      11      |            |
| GeoCircle::relateRectangleCircleWrapsPol           |      13      |            |
| GeoJSONReader::readDistance                        |      11      |            |
| JtsGeoJSONWriter::write                            |      14      |            |
| CartesianDistCalc::distanceToLineSegment           |       4      |            |
| DistanceUtils::calcBoxByDistFromPt_latHorizAxisDEG |       7      |            |
| Range::LongitudeRange::expandTo                    |       7      |            |
| ShapeCollection<S::computeMutualDisjoint           |       4      |            | Anropas inte
| GeodesicSphereDistCalc::pointOnBearing             |       3      |            |


### PolyshapeWriter::write 

### Geometry::unwrapDateline  

### GeoCircle::relateRectangleCircleWrapsPol

### GeoJSONReader::readDistance   

### JtsGeoJSONWriter::write  
1. What are your results for the ten most complex functions? (If ranking
is not easily possible: ten complex functions)?
   * Did all tools/methods get the same result?
   * Are the results clear?
2. Are the functions just complex, or also long?
3. What is the purpose of the functions?
4. Are exceptions taken into account in the given measurements?
5. Is the documentation clear w.r.t. all the possible outcomes?

## Coverage

### Tools

Document your experience in using a "new"/different coverage tool.

How well was the tool documented? Was it possible/easy/difficult to
integrate it with your build environment?

### DYI

Show a patch that show the instrumented code in main (or the unit
test setup), and the ten methods where branch coverage is measured.

The patch is probably too long to be copied here, so please add
the git command that is used to obtain the patch instead:

git diff ...

What kinds of constructs does your tool support, and how accurate is
its output?

### Evaluation

Report of old coverage: [link]

Report of new coverage: [link]

Test cases added:

git diff ...

## Refactoring

Plan for refactoring complex code:

Carried out refactoring (optional)

git diff ...

## Effort spent

For each team member, how much time was spent in

1. plenary discussions/meetings;

2. discussions within parts of the group;

3. reading documentation;

4. configuration;

5. analyzing code/output;

6. writing documentation;

7. writing code;

8. running code?

## Overall experience

What are your main take-aways from this project? What did you learn?

Is there something special you want to mention here?
