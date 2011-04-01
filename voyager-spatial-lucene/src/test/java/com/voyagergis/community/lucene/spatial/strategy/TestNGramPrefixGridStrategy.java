package com.voyagergis.community.lucene.spatial.strategy;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.base.prefix.LinearPrefixGrid;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialArgsParser;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.simple.Point2D;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.prefix.NGramPrefixGridStrategy;
import org.apache.lucene.spatial.test.SpatialTestCase;
import org.junit.Test;

import com.voyagergis.community.lucene.spatial.JtsSpatialContext;

/**
 * @author Chris Male
 */
public class TestNGramPrefixGridStrategy extends SpatialTestCase {

  @Test
  public void testNGramPrefixGridLosAngeles() throws IOException {
    SimpleSpatialFieldInfo fieldInfo = new SimpleSpatialFieldInfo("geo");
    NGramPrefixGridStrategy prefixGridStrategy = new NGramPrefixGridStrategy(new LinearPrefixGrid(), 0);

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
