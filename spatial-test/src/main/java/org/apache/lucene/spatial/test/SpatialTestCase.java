package org.apache.lucene.spatial.test;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class SpatialTestCase extends LuceneTestCase {

  private IndexReader indexReader;
  private IndexWriter indexWriter;
  private Directory directory;
  private IndexSearcher indexSearcher;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    directory = newDirectory();

    IndexWriterConfig writerConfig = newIndexWriterConfig(random, TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT));
    indexWriter = new IndexWriter(directory, writerConfig);
  }

  @Override
  @After
  public void tearDown() throws Exception {
    if (indexWriter != null) {
      indexWriter.close();
    }
    if (indexSearcher != null) {
      indexSearcher.close();
    }
    if (indexReader != null) {
      indexReader.close();
    }
    if (directory != null) {
      directory.close();
    }
    super.tearDown();
  }

  // ================================================= Helper Methods ================================================

  protected void addDocument(Document doc) throws IOException {
    indexWriter.addDocument(doc);
  }

  protected void addDocumentsAndCommit(List<Document> documents) throws IOException {
    for (Document document : documents) {
      indexWriter.addDocument(document);
    }
    commit();
  }

  protected void deleteAllAndCommit() throws IOException {
    indexWriter.deleteAll();
    commit();
  }

  protected void commit() throws IOException {
    indexWriter.commit();
    if (indexReader == null) {
      indexReader = IndexReader.open(directory);
    } else {
      indexReader = indexReader.reopen();
    }
    indexSearcher = newSearcher(indexReader);
  }

  protected void verifyDocumentsIndexed(int numDocs) {
    assertEquals(numDocs, indexReader.numDocs());
  }

  protected SearchResults executeQuery(Query query, int numDocs) {
    try {
      TopDocs topDocs = indexSearcher.search(query, numDocs);

      List<SearchResult> results = new ArrayList<SearchResult>();
      for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
        results.add(new SearchResult(scoreDoc.score, indexSearcher.doc(scoreDoc.doc)));
      }
      return new SearchResults(topDocs.totalHits, results);
    } catch (IOException ioe) {
      throw new RuntimeException("IOException thrown while executing query", ioe);
    }
  }

  // ================================================= Inner Classes =================================================

  protected static class SearchResults {

    public int numFound;
    public List<SearchResult> results;

    public SearchResults(int numFound, List<SearchResult> results) {
      this.numFound = numFound;
      this.results = results;
    }
  }

  protected static class SearchResult {

    public float score;
    public Document document;

    public SearchResult(float score, Document document) {
      this.score = score;
      this.document = document;
    }
  }
}

