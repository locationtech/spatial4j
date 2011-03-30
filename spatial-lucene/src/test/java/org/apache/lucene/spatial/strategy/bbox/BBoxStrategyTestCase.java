package org.apache.lucene.spatial.strategy.bbox;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import junit.framework.Assert;

import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.spatial.base.io.sample.SampleData;
import org.apache.lucene.spatial.base.io.sample.SampleDataReader;
import org.apache.lucene.spatial.base.shape.jts.JtsShapeIO;
import org.apache.lucene.spatial.strategy.SpatialTestQuery;
import org.apache.lucene.spatial.strategy.StrategyTestCase;
import org.apache.lucene.spatial.strategy.util.TrieFieldHelper;
import org.junit.Test;

import com.vividsolutions.jts.geom.GeometryFactory;

public class BBoxStrategyTestCase extends StrategyTestCase<BBoxFieldInfo> {

  @Override
  protected void initStrategy() {
    shapeIO = new JtsShapeIO( new GeometryFactory() );

    BBoxStrategy s = new BBoxStrategy();
    s.trieInfo = new TrieFieldHelper.FieldInfo();
    s.parser = FieldCache.NUMERIC_UTILS_DOUBLE_PARSER;
    strategy = s;

    fieldInfo = new BBoxFieldInfo( "bbox" );
  }

  @Override
  protected Iterator<SampleData> getTestData() throws IOException {
//    File file = new File(getClass().getClassLoader()
//        .getResource("us-states.txt").getFile());
    File file = new File("../spatial-data/src/main/resources/us-states.txt");
    return new SampleDataReader(file);
  }

  @Test
  public void testSpatialSearch() throws IOException {
    System.out.println( "running simple query..." );

    SearchResults got = executeQuery(new MatchAllDocsQuery(), 5 );
    Assert.assertEquals( 51, got.numFound );

    // Test Contains
    File file = new File("src/test/resources/test-us-IsWithin-BBox.txt");
    System.out.println( file.getAbsolutePath() );
    runTestQueries( SpatialTestQuery.getTestQueries(argsParser, shapeIO, file) );

  }
}
