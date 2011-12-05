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

import org.apache.solr.SolrTestCaseJ4;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TestSort extends SolrTestCaseJ4
{

  public static final String SFIELD = "recursive";

  @BeforeClass
  public static void beforeClass() throws Exception {
    initCore("solrconfig.xml", "schema.xml");
  }

  /** Test that that queries against a spatial field return the distance as the score. */
  @Test
  public void directQuery() throws Exception {
    final String sfield = "recursive";
    assertU(adoc("id", "100", sfield, "1,2"));
    assertU(adoc("id", "101", sfield, "4,-1"));
    assertU(commit());

    assertJQ(req(
        "q",sfield+":\"Intersects(Circle(3,4 d=1000))\"",
        "fl","id,score",
        "sort","score asc")//want ascending due to increasing distance
        , 1e-4
        , "/response/docs/[0]/id=='100'"
        , "/response/docs/[0]/score==314.4034"
        , "/response/docs/[1]/id=='101'"
        , "/response/docs/[1]/score==565.9612"
    );
    //query again with the query point closer to #101, and check the new ordering
    assertJQ(req(
        "q",sfield+":\"Intersects(Circle(4,0 d=1000))\"",
        "fl","id,score",
        "sort","score asc")//want ascending due to increasing distance
        , 1e-4
        , "/response/docs/[0]/id=='101'"
        , "/response/docs/[1]/id=='100'"
    );

    //use sort=query(...)
    assertJQ(req(
        "q","*:*",
        "sort","query($sortQuery) asc", //want ascending due to increasing distance
        "sortQuery",sfield+":\"Intersects(Circle(3,4 d=1000))\"",
        "fl","id,score")
        , 1e-4
        , "/response/docs/[0]/id=='100'"
        , "/response/docs/[1]/id=='101'"  );
    //check reversed direction with query point closer to #101
    assertJQ(req(
        "q","*:*",
        "sort","query($sortQuery) asc", //want ascending due to increasing distance
        "sortQuery",sfield+":\"Intersects(Circle(4,0 d=1000))\"",
        "fl","id,score")
        , 1e-4
        , "/response/docs/[0]/id=='101'"
        , "/response/docs/[1]/id=='100'"  );
  }

  @Test
  public void multiVal() throws Exception {
    assertU(adoc("id", "100", SFIELD, "1,2"));//1 point
    assertU(adoc("id", "101", SFIELD, "4,-1", SFIELD, "3,5"));//2 points, 2nd is pretty close to query point
    assertU(commit());

    assertJQ(req(
        "q", SFIELD +":\"Intersects(Circle(3,4 d=1000))\"",
        "fl","id,score",
        "sort","score asc")//want ascending due to increasing distance
        , 1e-4
        , "/response/docs/[0]/id=='101'"
        , "/response/docs/[0]/score==111.04236"//dist to 3,5
    );
  }

  /** Ported from DistanceFunctionTest */
  @Test @Ignore
  public void ported_DistanceFunctionTest_testLatLon() throws Exception {
    final String sfield = "recursive";
    assertU(adoc("id", "100", sfield, "1,2"));
    assertU(commit());

    // default to reading pt
    assertJQ(req("defType","func",
        "q","geodist(1,2)",
        "pt","3,4",
        "fq","id:100",
        "fl","id,score")
        , 1e-5
        , "/response/docs/[0]/score==314.40338"
    );

    // default to reading pt first
    assertJQ(req("defType","func",
        "q","geodist(1,2)",
        "pt","3,4",
        "sfield", sfield,
        "fq","id:100",
        "fl","id,score")
        , 1e-5
        , "/response/docs/[0]/score==314.40338"
    );

    // if pt missing, use sfield
    assertJQ(req("defType","func",
        "q","geodist(3,4)",
        "sfield", sfield,
        "fq","id:100",
        "fl","id,score")
        , 1e-5
        ,"/response/docs/[0]/score==314.40338"
    );

    // read both pt and sfield
    assertJQ(req("defType","func",
        "q","geodist()","pt","3,4",
        "sfield", sfield,
        "fq","id:100",
        "fl","id,score")
        , 1e-5
        ,"/response/docs/[0]/score==314.40338"
    );

    // param substitution
    assertJQ(req("defType","func",
        "q","geodist($a,$b)",
        "a","3,4",
        "b", sfield,
        "fq","id:100",
        "fl","id,score")
        , 1e-5
        ,"/response/docs/[0]/score==314.40338"
    );

  }
}
