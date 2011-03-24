package org.apache.lucene.spatial.search.wkb;

import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.BytesRef;

class BytesRefTermAttributeImpl extends AttributeImpl implements TermToBytesRefAttribute {

  final BytesRef bytes;

  public BytesRefTermAttributeImpl( BytesRef b )
  {
    bytes = b;
  }

  public int fillBytesRef() {
    return bytes.hashCode();
  }

  public BytesRef getBytesRef() {
    return bytes;
  }

  @Override
  public void clear() {
  }

  @Override
  public boolean equals(Object other) {
    return other == this;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public void copyTo(AttributeImpl target) {
  }

  @Override
  public Object clone() {
    throw new UnsupportedOperationException();
  }
}