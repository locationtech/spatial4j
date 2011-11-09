package org.apache.solr.spatial;

import org.apache.solr.SolrTestCaseJ4;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


/**
 */
public class TestSolrSpatialField extends SolrTestCaseJ4
{
  @BeforeClass
  public static void beforeClass() throws Exception {
    initCore("solrconfig.xml", "schema.xml");
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
