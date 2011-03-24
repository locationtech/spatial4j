package org.apache.lucene.spatial.search.jts;

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
import org.apache.lucene.spatial.base.exception.InvalidShapeException;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.StringHelper;


/**
 * This has an indexed WKB value.
 *
 * In the future, this should use Column Stride Fields rather then indexing
 * the data.
 */
class WKBField extends AbstractField implements Fieldable
{
  private final String wkt;
  private final BytesRef bytes;

  public WKBField(String name, byte[] wkb, String wkt )
  {
    if (name == null)
      throw new IllegalArgumentException("name cannot be null");
    if (wkb == null && wkt == null)
      throw new IllegalArgumentException("wkt or wkb must be valid");

    this.name = StringHelper.intern(name);        // field names are interned
    fieldsData = wkb;
    this.wkt = wkt;

    if( wkb == null ) {
      bytes = null;
    }
    else {
      if( wkb.length > 32000 ) {
        throw new InvalidShapeException( "WKB must be less then 32K ["+wkb.length+"]" );
      }
      bytes = new BytesRef();
      bytes.bytes = wkb;
      bytes.length = wkb.length;
    }

    isStored  = wkt != null; //store;
    isIndexed = wkb != null;
    isTokenized = isIndexed; //false;
    omitTermFreqAndPositions = false;
    omitNorms = true;

    setStoreTermVector(TermVector.NO);
  }

  @Override
  public Reader readerValue() {
    return null;
  }

  @Override
  public String stringValue() {
    return wkt;
  }

  @Override
  public TokenStream tokenStreamValue() {
    return new BytesRefTokenStream( bytes );
  }
}
