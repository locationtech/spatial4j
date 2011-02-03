package voyager.quads.solr;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Put a list of strings directly into the token stream
 */
class StringListTokenizer extends Tokenizer
{
  final Iterable<String> tokens;
  Iterator<String> iter = null;

  public StringListTokenizer(Iterable<String> tokens) {
    this.tokens = tokens;
  }

  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);


  @Override
  public final boolean incrementToken() throws IOException {
    clearAttributes();
    if( iter == null ) {
      iter = tokens.iterator();
    }
    if( iter.hasNext() ) {
      String t = iter.next();
      termAtt.setLength(0);
      termAtt.append( t );
      return true;
    }
    return false;
  }

  @Override
  public final void end() {
  }

  @Override
  public void reset(Reader input) throws IOException {
    super.reset(input);
  }
}