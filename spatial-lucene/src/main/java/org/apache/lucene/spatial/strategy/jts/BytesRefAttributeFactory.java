package org.apache.lucene.spatial.strategy.jts;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.AttributeSource.AttributeFactory;


class BytesRefAttributeFactory extends AttributeFactory {

  private final AttributeFactory delegate;
  private final BytesRef bytes;

  public BytesRefAttributeFactory(AttributeFactory delegate, BytesRef b) {
    this.delegate = delegate;
    this.bytes = b;
  }

  @Override
  public AttributeImpl createAttributeInstance(Class<? extends Attribute> attClass) {
    if (attClass == TermToBytesRefAttribute.class) {
      return new BytesRefTermAttributeImpl(bytes);
    }
    if (CharTermAttribute.class.isAssignableFrom(attClass)) {
      throw new IllegalArgumentException("no");
    }
    return delegate.createAttributeInstance(attClass);
  }
}