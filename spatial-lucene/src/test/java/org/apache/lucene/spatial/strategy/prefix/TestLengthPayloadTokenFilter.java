package org.apache.lucene.spatial.strategy.prefix;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.spatial.strategy.prefix.LengthPayloadTokenFilter;
import org.apache.lucene.spatial.strategy.prefix.PayloadAwareEdgeNGramTokenFilter;
import org.apache.lucene.spatial.strategy.prefix.StringListTokenizer;
import org.apache.lucene.spatial.strategy.prefix.TruncateFilter;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * @author Chris Male
 */
public class TestLengthPayloadTokenFilter {

  @Test
  public void testGridLengthPayloadTokenFilter() throws IOException {
    List<CharSequence> grids = new ArrayList<CharSequence>();
    grids.add("ABCDADAA");
    grids.add("ADD");

    Map<String, List<Integer>> expectedPayloadsPerToken = new HashMap<String, List<Integer>>();
    expectedPayloadsPerToken.put("A", Arrays.asList(8, 3));
    expectedPayloadsPerToken.put("AB", Arrays.asList(8));
    expectedPayloadsPerToken.put("ABC", Arrays.asList(8));
    expectedPayloadsPerToken.put("ABCD", Arrays.asList(8));
    expectedPayloadsPerToken.put("ABCDA", Arrays.asList(8));
    expectedPayloadsPerToken.put("ABCDAD", Arrays.asList(8));
    expectedPayloadsPerToken.put("ABCDADA", Arrays.asList(8));
    expectedPayloadsPerToken.put("ABCDADAA", Arrays.asList(8));
    expectedPayloadsPerToken.put("AD", Arrays.asList(3));
    expectedPayloadsPerToken.put("ADD", Arrays.asList(3));

    TokenStream tokenStream = new PayloadAwareEdgeNGramTokenFilter(
        new RemoveDuplicatesTokenFilter(
            new LengthPayloadTokenFilter(
                new TruncateFilter(
                    new StringListTokenizer(grids), 10))),
        PayloadAwareEdgeNGramTokenFilter.Side.FRONT, 1, 20);

    CharTermAttribute termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
    PayloadAttribute payloadAttribute = tokenStream.getAttribute(PayloadAttribute.class);
    while (tokenStream.incrementToken()) {
      List<Integer> payloadsForToken = expectedPayloadsPerToken.get(termAttribute.toString());
      assertTrue(payloadsForToken.contains(PayloadHelper.decodeInt(payloadAttribute.getPayload().getData(), 0)));
    }
  }
}
