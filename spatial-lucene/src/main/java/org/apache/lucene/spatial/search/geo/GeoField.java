package org.apache.lucene.spatial.search.geo;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.AbstractField;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.StringHelper;


public final class GeoField extends AbstractField implements Fieldable
{
  private final BytesRef bytes;

  public GeoField(String name, byte[] value, boolean store, boolean index)
  {
    if (name == null)
      throw new IllegalArgumentException("name cannot be null");
    if (value == null)
      throw new IllegalArgumentException("value cannot be null");

    this.name = StringHelper.intern(name);        // field names are interned
    fieldsData = value;

    bytes = new BytesRef();
    bytes.bytes = value;
    bytes.length = value.length;

    isStored  = false; //store;
    isIndexed = index;
    isTokenized = true; //false;
    omitTermFreqAndPositions = false;
    omitNorms = true;

    isBinary    = false; //true;
    binaryLength = value.length;
    binaryOffset = 0;

    setStoreTermVector(TermVector.NO);
  }

  @Override
  public Reader readerValue() {
    return null;
  }

  @Override
  public String stringValue() {
    return null;
  }

  @Override
  public TokenStream tokenStreamValue() {
    return new BytesRefTokenStream( bytes );
  }
}
