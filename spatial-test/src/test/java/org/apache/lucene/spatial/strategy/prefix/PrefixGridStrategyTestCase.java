package org.apache.lucene.spatial.strategy.prefix;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.base.prefix.QuadPrefixGrid;
import org.apache.lucene.spatial.base.query.SpatialArgsParser;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.simple.Point2D;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.test.StrategyTestCase;
import org.junit.Test;

public class PrefixGridStrategyTestCase extends StrategyTestCase<SimpleSpatialFieldInfo>{

  @Test
  public void testPrefixGridLosAngeles() throws IOException {
    SimpleSpatialFieldInfo fieldInfo = new SimpleSpatialFieldInfo("geo");
    PrefixGridStrategy prefixGridStrategy = new PrefixGridStrategy(new QuadPrefixGrid(), 0);

    Shape point = new Point2D(-118.243680, 34.052230);

    Document losAngeles = new Document();
    losAngeles.add(new Field("name", "Los Angeles", Field.Store.YES, Field.Index.NOT_ANALYZED));
    losAngeles.add(prefixGridStrategy.createField(fieldInfo, point, true, true));

    addDocuments(Arrays.asList(losAngeles));

    // Polygon won't work with SimpleSpatialContext
    SpatialContext ctx = new SimpleSpatialContext();
    SpatialArgsParser spatialArgsParser = new SpatialArgsParser();

    // TODO -- use a non polygon query...
//    SpatialArgs spatialArgs = spatialArgsParser.parse(
//        "IsWithin(POLYGON((-127.00390625 39.8125,-112.765625 39.98828125,-111.53515625 31.375,-125.94921875 30.14453125,-127.00390625 39.8125)))",
//        ctx );

//    Query query = prefixGridStrategy.makeQuery(spatialArgs, fieldInfo);
//    SearchResults searchResults = executeQuery(query, 1);
//    assertEquals(1, searchResults.numFound);
  }
}
