package org.apache.solr.spatial.grid;


import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.AbstractField;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;


class BasicGridField extends AbstractField
{
  public String value;
  public TokenStream tokens;

  public BasicGridField( String name, boolean stored ) {
    super( name, stored?Store.YES:Store.NO, Index.ANALYZED_NO_NORMS, TermVector.NO );
    setOmitTermFreqAndPositions( true );
  }

  @Override
  public Reader readerValue() {
    return new StringReader( value );
  }

  @Override
  public String stringValue() {
    return value;
  }

  @Override
  public TokenStream tokenStreamValue() {
    return tokens;
  }
};

