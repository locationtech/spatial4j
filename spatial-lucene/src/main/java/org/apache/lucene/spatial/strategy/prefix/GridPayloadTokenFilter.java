package org.apache.lucene.spatial.strategy.prefix;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.index.Payload;

import java.io.IOException;

/**
 * @author Chris Male
 */
public final class GridPayloadTokenFilter extends TokenFilter {

  private final PayloadAttribute payloadAttribute = addAttribute(PayloadAttribute.class);
  private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);

  public GridPayloadTokenFilter(TokenStream input) {
    super(input);
  }

  public final boolean incrementToken() throws IOException {
    if (!input.incrementToken()) {
      return false;
    }


    payloadAttribute.setPayload(new Payload(termAttribute.toString().getBytes()));
    return true;
  }


}
