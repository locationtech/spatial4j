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

package org.apache.solr.spatial;

import org.apache.lucene.spatial.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.prefix.PrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTreeFactory;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.spatial.util.MapListener;

import java.util.Map;

public abstract class PrefixTreeFieldType<T extends PrefixTreeStrategy> extends SpatialFieldType<SimpleSpatialFieldInfo> {

  protected SpatialPrefixTree grid;

  @Override
  protected void init(IndexSchema schema, Map<String, String> args) {
    super.init(schema, args);

    //Solr expects us to remove the parameters we've used.
    MapListener<String, String> argsWrap = new MapListener<String, String>(args);
    grid = SpatialPrefixTreeFactory.makeSPT(argsWrap, schema.getResourceLoader().getClassLoader(), ctx);
    args.keySet().removeAll(argsWrap.getSeenKeys());

    PrefixTreeStrategy strat = initStrategy(schema, args);

    strat.setIgnoreIncompatibleGeometry( ignoreIncompatibleGeometry );

    String v = args.remove("distErrPct");
    if (v != null)
      strat.setDistErrPct(Double.parseDouble(v));
    
    v = args.remove("defaultFieldValuesArrayLen");
    if (v != null)
      strat.setDefaultFieldValuesArrayLen(Integer.parseInt(v));

    spatialStrategy = strat;

    log.info(this.toString()+" strat: "+strat+" maxLevels: "+ grid.getMaxLevels());//TODO output maxDetailKm
  }

  protected abstract T initStrategy(IndexSchema schema, Map<String, String> args);

  @Override
  protected SimpleSpatialFieldInfo getFieldInfo(SchemaField field) {
    return new SimpleSpatialFieldInfo(field.getName());
  }
}
