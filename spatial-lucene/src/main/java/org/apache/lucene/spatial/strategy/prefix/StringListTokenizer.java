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

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Put a list of strings directly into the token stream
 */
class StringListTokenizer extends Tokenizer {

  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

  private final Iterable<String> tokens;
  private Iterator<String> iter = null;

  public StringListTokenizer(Iterable<String> tokens) {
    this.tokens = tokens;
  }

  @Override
  public final boolean incrementToken() throws IOException {
    clearAttributes();
    if (iter == null) {
      iter = tokens.iterator();
    }
    if (iter.hasNext()) {
      String t = iter.next();
      termAtt.setLength(0);
      termAtt.append(t);
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