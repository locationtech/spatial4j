package org.apache.lucene.spatial.strategy.prefix;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.index.Payload;

/**
 * @author Chris Male
 */
@Deprecated
public final class GridPayloadTokenFilter extends TokenFilter {

  private final PayloadAttribute payloadAttribute = addAttribute(PayloadAttribute.class);
  private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);

  public GridPayloadTokenFilter(TokenStream input) {
    super(input);
  }

  @Override
  public final boolean incrementToken() throws IOException {
    if (!input.incrementToken()) {
      return false;
    }


    payloadAttribute.setPayload(new Payload(termAttribute.toString().getBytes()));
    return true;
  }


}
