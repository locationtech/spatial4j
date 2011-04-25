package org.apache.lucene.spatial.strategy.prefix;

import java.io.IOException;
import java.util.Arrays;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.prefix.LinearPrefixGrid;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialArgsParser;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.simple.Point2D;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.StrategyTestCase;
import org.junit.Test;

public class PrefixGridStrategyTestCase extends StrategyTestCase<SimpleSpatialFieldInfo>{

  public void executeQueries( SpatialContext io, String data, String ... tests ) throws IOException {

    SimpleSpatialFieldInfo finfo = new SimpleSpatialFieldInfo("geo");
    PrefixGridStrategy s
      = new PrefixGridStrategy(
          new LinearPrefixGrid(-180, 180, -90, 90, 12, io), 0);

    executeQueries( s, io, finfo, data, tests );
  }

  @Test
  public void testPrefixGridPolyWithJts() throws IOException {
//    executeQueries( new JtsSpatialContext(),
//        DATA_STATES_POLY,
//        QTEST_States_IsWithin_BBox,
//        QTEST_States_Intersects_BBox );
  }

  @Test
  public void testPrefixGridPointsJts() throws IOException {
//    executeQueries( new JtsSpatialContext(),
//        DATA_WORLD_CITIES_POINTS,
//        QTEST_Cities_IsWithin_BBox );
  }


  @Test
  public void testPrefixGridLosAngeles() throws IOException {
    SimpleSpatialFieldInfo fieldInfo = new SimpleSpatialFieldInfo("geo");
    PrefixGridStrategy prefixGridStrategy = new PrefixGridStrategy(new LinearPrefixGrid(), 0);

    Shape point = new Point2D(-118.243680, 34.052230);

    Document losAngeles = new Document();
    losAngeles.add(new Field("name", "Los Angeles", Field.Store.YES, Field.Index.NOT_ANALYZED));
    losAngeles.add(prefixGridStrategy.createField(fieldInfo, point, true, true));

    addDocuments(Arrays.asList(losAngeles));

    SpatialArgsParser spatialArgsParser = new SpatialArgsParser();
    SpatialArgs spatialArgs = spatialArgsParser.parse(
        "IsWithin(POLYGON((-127.00390625 39.8125,-112.765625 39.98828125,-111.53515625 31.375,-125.94921875 30.14453125,-127.00390625 39.8125)))",
        new JtsSpatialContext());

    Query query = prefixGridStrategy.makeQuery(spatialArgs, fieldInfo);
    SearchResults searchResults = executeQuery(query, 1);
    assertEquals(1, searchResults.numFound);
  }
}
