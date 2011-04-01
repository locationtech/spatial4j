package org.apache.lucene.spatial.strategy.prefix;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialOperation;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.junit.Test;

/**
 * @author Chris Male
 */
public class TestPrefixGridStrategy {

  @Test
  public void testMakeQuery_isWithin() {
    final List<String> spatialGrids = Arrays.asList("aaaDBCDA+", "aaaBDDA*");
    PrefixGridStrategy gridStrategy = new PrefixGridStrategy(new MockSpatialPrefixGrid() {
      @Override
      public List<String> readCells(Shape geo) {
        return spatialGrids;
      }
    }, -1);

    BooleanQuery booleanQuery = (BooleanQuery) gridStrategy.makeQuery(
        new SpatialArgs(SpatialOperation.IsWithin),
        new SimpleSpatialFieldInfo("field"));

    BooleanClause[] booleanClauses = booleanQuery.getClauses();
    assertEquals(2, booleanClauses.length);

    assertSpatialPrefixGridQueryTerms(booleanClauses, 0, 1, "aaadbcda*", "aaabdda*");
  }

  @Test
  public void testMakeQuery_insersects() {
    final List<String> spatialGrids = Arrays.asList("aaaDBCDA+", "aaaBDDA*");
    PrefixGridStrategy gridStrategy = new PrefixGridStrategy(new MockSpatialPrefixGrid() {
      @Override
      public List<String> readCells(Shape geo) {
        return spatialGrids;
      }
    }, -1);

    BooleanQuery booleanQuery = (BooleanQuery) gridStrategy.makeQuery(
        new SpatialArgs(SpatialOperation.Intersects),
        new SimpleSpatialFieldInfo("field"));

    BooleanClause[] booleanClauses = booleanQuery.getClauses();
    assertEquals(12, booleanClauses.length);

    assertSpatialPrefixGridQueryTerms(booleanClauses, 0, 1, "aaadbcda*", "aaabdda*");
    assertPrefixGridTermQueryTerms(booleanClauses, 2, 11, "a", "aa", "aaa", "aaab", "aaabd", "aaabdd", "aaad", "aaadb", "aaadbc", "aaadbcd");
  }

  private void assertSpatialPrefixGridQueryTerms(BooleanClause[] queries, int start, int end, String... terms) {
    for (int i = start; i <= end; i++) {
      assertEquals(terms[i - start], ((SpatialPrefixGridQuery) queries[i].getQuery()).getTerm().text());
    }
  }

  private void assertPrefixGridTermQueryTerms(BooleanClause[] queries, int start, int end, String... terms) {
    for (int i = start; i <= end; i++) {
      assertEquals(terms[i - start], ((PrefixGridTermQuery) queries[i].getQuery()).getTerm().text());
    }
  }
}
