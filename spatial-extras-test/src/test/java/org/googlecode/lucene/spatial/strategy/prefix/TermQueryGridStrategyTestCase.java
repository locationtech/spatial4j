package org.googlecode.lucene.spatial.strategy.prefix;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.base.prefix.QuadPrefixGrid;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialArgsParser;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.simple.PointImpl;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.prefix.TermQueryGridStrategy;
import org.apache.lucene.spatial.test.SpatialMatchConcern;
import org.apache.lucene.spatial.test.StrategyTestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class TermQueryGridStrategyTestCase extends StrategyTestCase<SimpleSpatialFieldInfo>{

  @Override
  public void setUp() throws Exception {
    super.setUp();

    this.ctx = new JtsSpatialContext();
    this.strategy = new TermQueryGridStrategy(
      new QuadPrefixGrid(ctx, 12));
    this.fieldInfo = new SimpleSpatialFieldInfo("geo");
  }

  //
//  @Test
//  public void testPrefixGridPolyWithJts() throws IOException {
//    executeQueries( new JtsSpatialContext(),
//        SpatialMatchConcern.SUPERSET,
//        DATA_STATES_POLY,
//        QTEST_States_IsWithin_BBox,
//        QTEST_States_Intersects_BBox );
//  }

  @Test
  public void testPrefixGridPointsJts() throws IOException {
    getAddAndVerifyIndexedDocuments(DATA_WORLD_CITIES_POINTS);
    executeQueries(SpatialMatchConcern.SUPERSET, QTEST_Cities_IsWithin_BBox);
  }


  @Test
  public void testPrefixGridLosAngeles() throws IOException {

    Shape point = new PointImpl(-118.243680, 34.052230);

    Document losAngeles = new Document();
    losAngeles.add(new Field("name", "Los Angeles", Field.Store.YES, Field.Index.NOT_ANALYZED));
    losAngeles.add(strategy.createField(fieldInfo, point, true, true));

    addDocuments(Arrays.asList(losAngeles));

    // Polygon won't work with SimpleSpatialContext
    SpatialArgsParser spatialArgsParser = new SpatialArgsParser();
    SpatialArgs spatialArgs = spatialArgsParser.parse(
        "IsWithin(POLYGON((-127.00390625 39.8125,-112.765625 39.98828125,-111.53515625 31.375,-125.94921875 30.14453125,-127.00390625 39.8125)))",
        ctx );

    Query query = strategy.makeQuery(spatialArgs, fieldInfo);
    SearchResults searchResults = executeQuery(query, 1);
    assertEquals(1, searchResults.numFound);
  }
}
