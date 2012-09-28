/*
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

package com.spatial4j.core.io.geonames;

import java.io.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class GeonamesReader implements Iterator<Geoname> {

  private BufferedReader reader;
  private String nextLine;

  public GeonamesReader(InputStream inputStream) throws IOException {
    this.reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
    readNextLine();
  }

  public GeonamesReader(Reader reader) throws IOException {
    if (reader instanceof BufferedReader) {
      this.reader = (BufferedReader) reader;
    } else {
      this.reader = new BufferedReader(reader);
    }
    readNextLine();
  }

  @Override
  public Geoname next() {
    Geoname geoname;
    if (nextLine != null) {
      geoname = new Geoname(nextLine);
    } else {
      throw new NoSuchElementException();
    }

    readNextLine();
    return geoname;
  }

  private void readNextLine() {
    if (reader == null) {
      return;
    }

    try {
      while (reader != null) {
        nextLine = reader.readLine();
        if (nextLine == null) {
          reader = null;
          break;
        } else if (!nextLine.startsWith("#")) {
          nextLine = nextLine.trim();
          if (nextLine.length() > 0) {
            break;
          }
        }
      }
    } catch (IOException ioe) {
      throw new RuntimeException("IOException thrown while reading/closing reader", ioe);
    }
  }

  @Override
  public boolean hasNext() {
    return nextLine != null;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Read-only Iterator");
  }
}
