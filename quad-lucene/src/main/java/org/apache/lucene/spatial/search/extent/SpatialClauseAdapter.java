/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.spatial.search.extent;
//
//import com.esri.gpt.catalog.discovery.Discoverable;
//import com.esri.gpt.catalog.discovery.DiscoveryException;
//import com.esri.gpt.catalog.discovery.LogicalClause;
//import com.esri.gpt.catalog.discovery.SpatialClause;
//import com.esri.gpt.framework.collection.StringAttributeMap;
//import com.esri.gpt.framework.context.RequestContext;
//import com.esri.gpt.framework.geometry.Envelope;
//import com.esri.gpt.framework.util.Val;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.function.ValueSourceQuery;
import org.apache.lucene.spatial.core.Extent;
import org.apache.lucene.spatial.core.SpatialOperation;


/**
 * Adapts a catalog discovery SpatialClause to the Lucene model.
 */
public class SpatialClauseAdapter
{
  private ExtentFieldNameInfo fields;
  private Extent queryExtent;



  protected void adaptSpatialClause(SpatialOperation spatialClause)
  {
    // determine spatialRelevance parameters
    // (original defaults were queryPower=2.0, targetPower=0.5)
    double queryPower = 1.0; //Val.chkDbl(params.getValue("spatialRelevance.queryPower"),1.0);
    double targetPower = 1.0f; //Val.chkDbl(params.getValue("spatialRelevance.targetPower"),1.0);
    boolean bUseSpatialRanking = false;

    // No spatial scoring for:
    // SpatialClause.GeometryIsDisjointTo
    // SpatialClause.GeometryIsEqualTo


    if (bUseSpatialRanking) {
      SpatialRankingValueSource srvs = new SpatialRankingValueSource(queryExtent,
          queryPower,targetPower, "fieldname" );
      Query spatialRankingQuery = new ValueSourceQuery(srvs);
//        BooleanQuery bq = new BooleanQuery();
//        bq.add(spatialQuery,BooleanClause.Occur.MUST);
//        bq.add(spatialRankingQuery,BooleanClause.Occur.MUST);
//        appendQuery(activeBooleanQuery,activeLogicalClause,bq);
//        this.getQueryAdapter().setHasScoredExpression(true);
    }

    SpatialOperation op = null;
  }

  public Query make( SpatialOperation op )
  {
    switch( op )
    {
    case BBOXIntersects: return makeIntersects();
    case Contains: return makeContains();
    case Intersects: return makeIntersects();
    case IsEqualTo: return makeEquals();
    case IsDisjointTo: return makeDisjoint();
    case IsWithin: return makeWithin();
    case Overlaps: return makeIntersects();
    }

    throw new RuntimeException("UnspporetedOperation: "+op);
  }


  /**
   * Constructs a query to retrieve documents that fully contain the input envelope.
   * @return the spatial query
   */
  private Query makeContains() {

    /*
    // the original contains query does not work for envelopes that cross the date line
    // docMinX <= queryExtent.getMinX(), docMinY <= queryExtent.getMinY(), docMaxX >= queryExtent.getMaxX(), docMaxY >= queryExtent.getMaxY()
    Query qMinX = NumericRangeQuery.newDoubleRange(docMinX,null,queryExtent.getMinX(),false,true);
    Query qMinY = NumericRangeQuery.newDoubleRange(docMinY,null,queryExtent.getMinY(),false,true);
    Query qMaxX = NumericRangeQuery.newDoubleRange(docMaxX,queryExtent.getMaxX(),null,true,false);
    Query qMaxY = NumericRangeQuery.newDoubleRange(docMaxY,queryExtent.getMaxY(),null,true,false);
    BooleanQuery bq = new BooleanQuery();
    bq.add(qMinX,BooleanClause.Occur.MUST);
    bq.add(qMinY,BooleanClause.Occur.MUST);
    bq.add(qMaxX,BooleanClause.Occur.MUST);
    bq.add(qMaxY,BooleanClause.Occur.MUST);
    return bq;
    */

    // general case
    // docMinX <= queryExtent.getMinX() AND docMinY <= queryExtent.getMinY() AND docMaxX >= queryExtent.getMaxX() AND docMaxY >= queryExtent.getMaxY()

    // Y conditions
    // docMinY <= queryExtent.getMinY() AND docMaxY >= queryExtent.getMaxY()
    Query qMinY = NumericRangeQuery.newDoubleRange(fields.docMinY,null,queryExtent.getMinY(),false,true);
    Query qMaxY = NumericRangeQuery.newDoubleRange(fields.docMaxY,queryExtent.getMaxY(),null,true,false);
    Query yConditions = this.makeQuery(new Query[]{qMinY,qMaxY},BooleanClause.Occur.MUST);

    // X conditions
    Query xConditions = null;

    // queries that do not cross the date line
    if (!queryExtent.getCrossesDateLine()) {

      // X Conditions for documents that do not cross the date line,
      // documents that contain the min X and max X of the query envelope,
      // docMinX <= queryExtent.getMinX() AND docMaxX >= queryExtent.getMaxX()
      Query qMinX = NumericRangeQuery.newDoubleRange(fields.docMinX,null,queryExtent.getMinX(),false,true);
      Query qMaxX = NumericRangeQuery.newDoubleRange(fields.docMaxX,queryExtent.getMaxX(),null,true,false);
      Query qMinMax = this.makeQuery(new Query[]{qMinX,qMaxX},BooleanClause.Occur.MUST);
      Query qNonXDL = this.makeXDL(false,qMinMax);

      // X Conditions for documents that cross the date line,
      // the left portion of the document contains the min X of the query
      // OR the right portion of the document contains the max X of the query,
      // docMinXLeft <= queryExtent.getMinX() OR docMaxXRight >= queryExtent.getMaxX()
      Query qXDLLeft = NumericRangeQuery.newDoubleRange(fields.docMinXLeft,null,queryExtent.getMinX(),false,true);
      Query qXDLRight = NumericRangeQuery.newDoubleRange(fields.docMaxXRight,queryExtent.getMaxX(),null,true,false);
      Query qXDLLeftRight = this.makeQuery(new Query[]{qXDLLeft,qXDLRight},BooleanClause.Occur.SHOULD);
      Query qXDL = this.makeXDL(true,qXDLLeftRight);

      // apply the non-XDL and XDL conditions
      xConditions = this.makeQuery(new Query[]{qNonXDL,qXDL},BooleanClause.Occur.SHOULD);

    // queries that cross the date line
    } else {

      // No need to search for documents that do not cross the date line

      // X Conditions for documents that cross the date line,
      // the left portion of the document contains the min X of the query
      // AND the right portion of the document contains the max X of the query,
      // docMinXLeft <= queryExtent.getMinX() AND docMaxXRight >= queryExtent.getMaxX()
      Query qXDLLeft = NumericRangeQuery.newDoubleRange(fields.docMinXLeft,null,queryExtent.getMinX(),false,true);
      Query qXDLRight = NumericRangeQuery.newDoubleRange(fields.docMaxXRight,queryExtent.getMaxX(),null,true,false);
      Query qXDLLeftRight = this.makeQuery(new Query[]{qXDLLeft,qXDLRight},BooleanClause.Occur.MUST);
      Query qXDL = this.makeXDL(true,qXDLLeftRight);

      xConditions = qXDL;
    }

    // both X and Y conditions must occur
    Query xyConditions = this.makeQuery(new Query[]{xConditions,yConditions},BooleanClause.Occur.MUST);
    return xyConditions;
  }

  /**
   * Constructs a query to retrieve documents that are disjoint to the input envelope.
   * @return the spatial query
   */
  private Query makeDisjoint() {

    /*
    // the original disjoint query does not work for envelopes that cross the date line
    // docMinX > queryExtent.getMaxX() OR docMaxX < queryExtent.getMinX() OR docMinY > queryExtent.getMaxY() OR docMaxY < queryExtent.getMinY()
    Query qMinX = NumericRangeQuery.newDoubleRange(docMinX,queryExtent.getMaxX(),null,false,false);
    Query qMaxX = NumericRangeQuery.newDoubleRange(docMaxX,null,queryExtent.getMinX(),false,false);
    Query qMinY = NumericRangeQuery.newDoubleRange(docMinY,queryExtent.getMaxY(),null,false,false);
    Query qMaxY = NumericRangeQuery.newDoubleRange(docMaxY,null,queryExtent.getMinY(),false,false);
    BooleanQuery bq = new BooleanQuery();
    bq.add(qMinX,BooleanClause.Occur.SHOULD);
    bq.add(qMinY,BooleanClause.Occur.SHOULD);
    bq.add(qMaxX,BooleanClause.Occur.SHOULD);
    bq.add(qMaxY,BooleanClause.Occur.SHOULD);
    */

    // general case
    // docMinX > queryExtent.getMaxX() OR docMaxX < queryExtent.getMinX() OR docMinY > queryExtent.getMaxY() OR docMaxY < queryExtent.getMinY()

    // Y conditions
    // docMinY > queryExtent.getMaxY() OR docMaxY < queryExtent.getMinY()
    Query qMinY = NumericRangeQuery.newDoubleRange(fields.docMinY,queryExtent.getMaxY(),null,false,false);
    Query qMaxY = NumericRangeQuery.newDoubleRange(fields.docMaxY,null,queryExtent.getMinY(),false,false);
    Query yConditions = this.makeQuery(new Query[]{qMinY,qMaxY},BooleanClause.Occur.SHOULD);

    // X conditions
    Query xConditions = null;

    // queries that do not cross the date line
    if (!queryExtent.getCrossesDateLine()) {

      // X Conditions for documents that do not cross the date line,
      // docMinX > queryExtent.getMaxX() OR docMaxX < queryExtent.getMinX()
      Query qMinX = NumericRangeQuery.newDoubleRange(fields.docMinX,queryExtent.getMaxX(),null,false,false);
      Query qMaxX = NumericRangeQuery.newDoubleRange(fields.docMaxX,null,queryExtent.getMinX(),false,false);
      Query qMinMax = this.makeQuery(new Query[]{qMinX,qMaxX},BooleanClause.Occur.SHOULD);
      Query qNonXDL = this.makeXDL(false,qMinMax);

      // X Conditions for documents that cross the date line,
      // both the left and right portions of the document must be disjoint to the query
      // (docMinXLeft > queryExtent.getMaxX() OR docMaxXLeft < queryExtent.getMinX()) AND
      // (docMinXRight > queryExtent.getMaxX() OR docMaxXRight < queryExtent.getMinX())
      // where: docMaxXLeft = 180.0, docMinXRight = -180.0
      // (docMaxXLeft  < queryExtent.getMinX()) equates to (180.0  < queryExtent.getMinX()) and is ignored
      // (docMinXRight > queryExtent.getMaxX()) equates to (-180.0 > queryExtent.getMaxX()) and is ignored
      Query qMinXLeft = NumericRangeQuery.newDoubleRange(fields.docMinXLeft,queryExtent.getMaxX(),null,false,false);
      Query qMaxXRight = NumericRangeQuery.newDoubleRange(fields.docMaxXRight,null,queryExtent.getMinX(),false,false);
      Query qLeftRight = this.makeQuery(new Query[]{qMinXLeft,qMaxXRight},BooleanClause.Occur.MUST);
      Query qXDL = this.makeXDL(true,qLeftRight);

      // apply the non-XDL and XDL conditions
      xConditions = this.makeQuery(new Query[]{qNonXDL,qXDL},BooleanClause.Occur.SHOULD);

    // queries that cross the date line
    } else {

      // X Conditions for documents that do not cross the date line,
      // the document must be disjoint to both the left and right query portions
      // (docMinX > queryExtent.getMaxX()Left OR docMaxX < queryExtent.getMinX()) AND (docMinX > queryExtent.getMaxX() OR docMaxX < queryExtent.getMinX()Left)
      // where: queryExtent.getMaxX()Left = 180.0, queryExtent.getMinX()Left = -180.0
      Query qMinXLeft = NumericRangeQuery.newDoubleRange(fields.docMinX,180.0,null,false,false);
      Query qMaxXLeft = NumericRangeQuery.newDoubleRange(fields.docMaxX,null,queryExtent.getMinX(),false,false);
      Query qMinXRight = NumericRangeQuery.newDoubleRange(fields.docMinX,queryExtent.getMaxX(),null,false,false);
      Query qMaxXRight = NumericRangeQuery.newDoubleRange(fields.docMaxX,null,-180.0,false,false);
      Query qLeft = this.makeQuery(new Query[]{qMinXLeft,qMaxXLeft},BooleanClause.Occur.SHOULD);
      Query qRight = this.makeQuery(new Query[]{qMinXRight,qMaxXRight},BooleanClause.Occur.SHOULD);
      Query qLeftRight = this.makeQuery(new Query[]{qLeft,qRight},BooleanClause.Occur.MUST);
      Query qNonXDL = this.makeXDL(false,qLeftRight);

      // No need to search for documents that do not cross the date line

      xConditions = qNonXDL;
    }

    // either X or Y conditions should occur
    Query xyConditions = this.makeQuery(new Query[]{xConditions,yConditions},BooleanClause.Occur.SHOULD);
    return xyConditions;
  }

  /**
   * Constructs a query to retrieve documents that equal the input envelope.
   * @return the spatial query
   */
  private Query makeEquals() {

    // docMinX = queryExtent.getMinX() AND docMinY = queryExtent.getMinY() AND docMaxX = queryExtent.getMaxX() AND docMaxY = queryExtent.getMaxY()
    Query qMinX = NumericRangeQuery.newDoubleRange(fields.docMinX,queryExtent.getMinX(),queryExtent.getMinX(),true,true);
    Query qMinY = NumericRangeQuery.newDoubleRange(fields.docMinY,queryExtent.getMinY(),queryExtent.getMinY(),true,true);
    Query qMaxX = NumericRangeQuery.newDoubleRange(fields.docMaxX,queryExtent.getMaxX(),queryExtent.getMaxX(),true,true);
    Query qMaxY = NumericRangeQuery.newDoubleRange(fields.docMaxY,queryExtent.getMaxY(),queryExtent.getMaxY(),true,true);
    BooleanQuery bq = new BooleanQuery();
    bq.add(qMinX,BooleanClause.Occur.MUST);
    bq.add(qMinY,BooleanClause.Occur.MUST);
    bq.add(qMaxX,BooleanClause.Occur.MUST);
    bq.add(qMaxY,BooleanClause.Occur.MUST);
    return bq;
  }

  /**
   * Constructs a query to retrieve documents that intersect the input envelope.
   * @return the spatial query
   */
  private Query makeIntersects() {

    // the original intersects query does not work for envelopes that cross the date line,
    // switch to a NOT Disjoint query

    // MUST_NOT causes a problem when it's the only clause type within a BooleanQuery,
    // to get round it we add all documents as a SHOULD

    // there must be an envelope, it must not be disjoint
    Query qDisjoint = makeDisjoint();
    Query qIsNonXDL = this.makeXDL(false);
    Query qIsXDL = this.makeXDL(true);
    Query qHasEnv = this.makeQuery(new Query[]{qIsNonXDL,qIsXDL},BooleanClause.Occur.SHOULD);
    BooleanQuery qNotDisjoint = new BooleanQuery();
    qNotDisjoint.add(qHasEnv,BooleanClause.Occur.MUST);
    qNotDisjoint.add(qDisjoint,BooleanClause.Occur.MUST_NOT);

    //Query qDisjoint = makeDisjoint();
    //BooleanQuery qNotDisjoint = new BooleanQuery();
    //qNotDisjoint.add(new MatchAllDocsQuery(),BooleanClause.Occur.SHOULD);
    //qNotDisjoint.add(qDisjoint,BooleanClause.Occur.MUST_NOT);
    return qNotDisjoint;
  }

  /**
   * Makes a boolean query based upon a collection of queries and a logical operator.
   * @param queries the query collection
   * @param occur the logical operator
   * @return the query
   */
  private BooleanQuery makeQuery(Query[] queries, BooleanClause.Occur occur) {
    BooleanQuery bq = new BooleanQuery();
    for (Query query: queries) {
      bq.add(query,occur);
    }
    return bq;
  }

  /**
   * Constructs a query to retrieve documents are fully within the input envelope.
   * @return the spatial query
   */
  private Query makeWithin() {

    /*
    // the original within query does not work for envelopes that cross the date line
    // docMinX >= queryExtent.getMinX() AND docMinY >= queryExtent.getMinY() AND docMaxX <= queryExtent.getMaxX() AND docMaxY <= queryExtent.getMaxY()
    Query qMinX = NumericRangeQuery.newDoubleRange(docMinX,queryExtent.getMinX(),null,true,false);
    Query qMinY = NumericRangeQuery.newDoubleRange(docMinY,queryExtent.getMinY(),null,true,false);
    Query qMaxX = NumericRangeQuery.newDoubleRange(docMaxX,null,queryExtent.getMaxX(),false,true);
    Query qMaxY = NumericRangeQuery.newDoubleRange(docMaxY,null,queryExtent.getMaxY(),false,true);
    BooleanQuery bq = new BooleanQuery();
    bq.add(qMinX,BooleanClause.Occur.MUST);
    bq.add(qMinY,BooleanClause.Occur.MUST);
    bq.add(qMaxX,BooleanClause.Occur.MUST);
    bq.add(qMaxY,BooleanClause.Occur.MUST);
    return bq;
    */

    // general case
    // docMinX >= queryExtent.getMinX() AND docMinY >= queryExtent.getMinY() AND docMaxX <= queryExtent.getMaxX() AND docMaxY <= queryExtent.getMaxY()

    // Y conditions
    // docMinY >= queryExtent.getMinY() AND docMaxY <= queryExtent.getMaxY()
    Query qMinY = NumericRangeQuery.newDoubleRange(fields.docMinY,queryExtent.getMinY(),null,true,false);
    Query qMaxY = NumericRangeQuery.newDoubleRange(fields.docMaxY,null,queryExtent.getMaxY(),false,true);
    Query yConditions = this.makeQuery(new Query[]{qMinY,qMaxY},BooleanClause.Occur.MUST);

    // X conditions
    Query xConditions = null;

    // X Conditions for documents that cross the date line,
    // the left portion of the document must be within the left portion of the query,
    // AND the right portion of the document must be within the right portion of the query
    // docMinXLeft >= queryExtent.getMinX() AND docMaxXLeft <= 180.0
    // AND docMinXRight >= -180.0 AND docMaxXRight <= queryExtent.getMaxX()
    Query qXDLLeft = NumericRangeQuery.newDoubleRange(fields.docMinXLeft,queryExtent.getMinX(),null,true,false);
    Query qXDLRight = NumericRangeQuery.newDoubleRange(fields.docMaxXRight,null,queryExtent.getMaxX(),false,true);
    Query qXDLLeftRight = this.makeQuery(new Query[]{qXDLLeft,qXDLRight},BooleanClause.Occur.MUST);
    Query qXDL  = this.makeXDL(true,qXDLLeftRight);

    // queries that do not cross the date line
    if (!queryExtent.getCrossesDateLine()) {

      // X Conditions for documents that do not cross the date line,
      // docMinX >= queryExtent.getMinX() AND docMaxX <= queryExtent.getMaxX()
      Query qMinX = NumericRangeQuery.newDoubleRange(fields.docMinX,queryExtent.getMinX(),null,true,false);
      Query qMaxX = NumericRangeQuery.newDoubleRange(fields.docMaxX,null,queryExtent.getMaxX(),false,true);
      Query qMinMax = this.makeQuery(new Query[]{qMinX,qMaxX},BooleanClause.Occur.MUST);
      Query qNonXDL = this.makeXDL(false,qMinMax);

      // apply the non-XDL or XDL X conditions
      if ((queryExtent.getMinX() <= -180.0) && queryExtent.getMaxX() >= 180.0) {
        xConditions = this.makeQuery(new Query[]{qNonXDL,qXDL},BooleanClause.Occur.SHOULD);
      } else {
        xConditions = qNonXDL;
      }

    // queries that cross the date line
    } else {

      // X Conditions for documents that do not cross the date line

      // the document should be within the left portion of the query
      // docMinX >= queryExtent.getMinX() AND docMaxX <= 180.0
      Query qMinXLeft = NumericRangeQuery.newDoubleRange(fields.docMinX,queryExtent.getMinX(),null,true,false);
      Query qMaxXLeft = NumericRangeQuery.newDoubleRange(fields.docMaxX,null,180.0,false,true);
      Query qLeft = this.makeQuery(new Query[]{qMinXLeft,qMaxXLeft},BooleanClause.Occur.MUST);

      // the document should be within the right portion of the query
      // docMinX >= -180.0 AND docMaxX <= queryExtent.getMaxX()
      Query qMinXRight = NumericRangeQuery.newDoubleRange(fields.docMinX,-180.0,null,true,false);
      Query qMaxXRight = NumericRangeQuery.newDoubleRange(fields.docMaxX,null,queryExtent.getMaxX(),false,true);
      Query qRight = this.makeQuery(new Query[]{qMinXRight,qMaxXRight},BooleanClause.Occur.MUST);

      // either left or right conditions should occur,
      // apply the left and right conditions to documents that do not cross the date line
      Query qLeftRight = this.makeQuery(new Query[]{qLeft,qRight},BooleanClause.Occur.SHOULD);
      Query qNonXDL = this.makeXDL(false,qLeftRight);

      // apply the non-XDL and XDL conditions
      xConditions = this.makeQuery(new Query[]{qNonXDL,qXDL},BooleanClause.Occur.SHOULD);
    }

    // both X and Y conditions must occur
    Query xyConditions = this.makeQuery(new Query[]{xConditions,yConditions},BooleanClause.Occur.MUST);
    return xyConditions;
  }

  /**
   * Constructs a query to retrieve documents that do or do not cross the date line.
   * @param crossedDateLine <code>true</true> for documents that cross the date line
   * @return the query
   */
  private Query makeXDL(boolean crossedDateLine) {
    return new TermQuery(new Term(fields.docXDL,""+crossedDateLine));
  }

  /**
   * Constructs a query to retrieve documents that do or do not cross the date line
   * and match the supplied spatial query.
   * @param crossedDateLine <code>true</true> for documents that cross the date line
   * @param query the spatial query
   * @return the query
   */
  private Query makeXDL(boolean crossedDateLine, Query query) {
    BooleanQuery bq = new BooleanQuery();
    bq.add(this.makeXDL(crossedDateLine),BooleanClause.Occur.MUST);
    bq.add(query,BooleanClause.Occur.MUST);
    return bq;
  }

}
