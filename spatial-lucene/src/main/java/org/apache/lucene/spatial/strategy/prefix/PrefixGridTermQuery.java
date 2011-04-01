package org.apache.lucene.spatial.strategy.prefix;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;

/**
 * @author Chris Male
 */
public class PrefixGridTermQuery extends Query {

  private TermQuery termQuery;
  private int bestResolution;
  private PrefixGridSimilarity gridSimilarity;

  public PrefixGridTermQuery(TermQuery termQuery, int bestResolution, PrefixGridSimilarity gridSimilarity) {
    this.termQuery = termQuery;
    this.bestResolution = bestResolution;
    this.gridSimilarity = gridSimilarity;
  }

  @Override
  public int hashCode() {
    return termQuery.hashCode() ^ bestResolution;
  }

  @Override
  public boolean equals(Object obj) {
    if (!PrefixGridTermQuery.class.isInstance(obj)) {
      return false;
    }
    PrefixGridTermQuery query = (PrefixGridTermQuery) obj;
    return query.termQuery.equals(termQuery) && query.bestResolution == bestResolution;
  }

  @Override
  public String toString(String field) {
    return termQuery.toString(field);
  }

  @Override
  public Weight createWeight(IndexSearcher searcher) throws IOException {
    return new PrefixGridTermWeight(termQuery.createWeight(searcher));
  }

  private class PrefixGridTermWeight extends Weight {

    private Weight weight;

    private PrefixGridTermWeight(Weight weight) {
      this.weight = weight;
    }

    @Override
    public Explanation explain(IndexReader.AtomicReaderContext atomicReaderContext, int doc) throws IOException {
      String matchedTerm = termQuery.getTerm().text();
      ComplexExplanation explanation = new ComplexExplanation();
      explanation.setMatch(true);
      explanation.setValue(gridSimilarity.scoreGridSearch(bestResolution, matchedTerm.length()));
      explanation.addDetail(new Explanation(bestResolution, "Best Search Resolution"));
      explanation.addDetail(new Explanation(matchedTerm.length(), "Matched Term Length"));
      return explanation;
    }

    @Override
    public Query getQuery() {
      return PrefixGridTermQuery.this;
    }

    @Override
    public float getValue() {
      return weight.getValue();
    }

    @Override
    public void normalize(float v) {
      weight.normalize(v);
    }

    @Override
    public Scorer scorer(IndexReader.AtomicReaderContext atomicReaderContext, ScorerContext scorerContext) throws IOException {
      Scorer wrappedScorer = weight.scorer(atomicReaderContext, scorerContext);
      return (wrappedScorer != null) ? new PrefixGridTermScorer(this, wrappedScorer) : null;
    }

    @Override
    public float sumOfSquaredWeights() throws IOException {
      return weight.sumOfSquaredWeights();
    }
  }

  private class PrefixGridTermScorer extends Scorer {

    private Scorer scorer;

    private PrefixGridTermScorer(Weight weight, Scorer scorer) {
      super(weight);
      this.scorer = scorer;
    }

    @Override
    public float score() throws IOException {
      String matchedTerm = termQuery.getTerm().text();
      return gridSimilarity.scoreGridSearch(bestResolution, matchedTerm.length());
    }

    @Override
    public int docID() {
      return scorer.docID();
    }

    @Override
    public int nextDoc() throws IOException {
      return scorer.nextDoc();
    }

    @Override
    public int advance(int docId) throws IOException {
      return scorer.advance(docId);
    }
  }
}
