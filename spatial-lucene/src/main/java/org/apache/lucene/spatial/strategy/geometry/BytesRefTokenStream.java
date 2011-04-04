package org.apache.lucene.spatial.strategy.geometry;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.util.BytesRef;


class BytesRefTokenStream extends TokenStream {

  private boolean exhausted = false;

  public BytesRefTokenStream(BytesRef b) {
    super(new BytesRefAttributeFactory(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY, b));
    addAttribute(TermToBytesRefAttribute.class);
  }

  @Override
  public final boolean incrementToken() {
    if (exhausted) {
      return false;
    } else {
      clearAttributes();
      //singleToken.copyTo(tokenAtt);
      exhausted = true;
      return true;
    }
  }

  @Override
  public void reset() {
    exhausted = false;
  }
}