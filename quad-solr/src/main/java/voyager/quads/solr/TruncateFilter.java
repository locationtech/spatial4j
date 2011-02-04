package voyager.quads.solr;


import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public final class TruncateFilter extends TokenFilter {

  private final int maxTokenLength;

  private final CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);

  public TruncateFilter(TokenStream in, int maxTokenLength) {
    super( in );
    this.maxTokenLength = maxTokenLength;
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (!input.incrementToken()) return false;

    if( termAttr.length() > maxTokenLength ) {
      termAttr.setLength( maxTokenLength );
    }
    return true;
  }
}
