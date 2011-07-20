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

package org.apache.lucene.spatial.strategy.prefix;


import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.AbstractField;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;


public class BasicGridFieldable extends AbstractField {
  public String value;
  public TokenStream tokens;

  public BasicGridFieldable(String name, boolean stored) {
    super(name, stored ? Store.YES : Store.NO, Index.ANALYZED_NO_NORMS, TermVector.NO);
    setOmitNorms(true);
  }

  @Override
  public Reader readerValue() {
    return new StringReader(value);
  }

  @Override
  public String stringValue() {
    return value;
  }

  @Override
  public TokenStream tokenStreamValue() {
    return tokens;
  }
}

