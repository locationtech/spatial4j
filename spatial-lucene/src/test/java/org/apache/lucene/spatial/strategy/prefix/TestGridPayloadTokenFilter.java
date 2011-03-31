package org.apache.lucene.spatial.strategy.prefix;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.spatial.strategy.prefix.GridPayloadTokenFilter;
import org.apache.lucene.spatial.strategy.prefix.PayloadAwareEdgeNGramTokenFilter;
import org.apache.lucene.spatial.strategy.prefix.StringListTokenizer;
import org.apache.lucene.spatial.strategy.prefix.TruncateFilter;
import org.apache.lucene.spatial.strategy.prefix.PayloadAwareEdgeNGramTokenFilter.Side;
import org.junit.Test;

/**
 * @author Chris Male
 */
public class TestGridPayloadTokenFilter {

  @Test
  public void testGridLengthPayloadTokenFilter() throws IOException {
    List<CharSequence> grids = new ArrayList<CharSequence>();
    grids.add("ABCDADAA");
    grids.add("ADD");

    List<byte[]> grid1Bytes = Arrays.asList("ABCDADAA".getBytes());
    List<byte[]> grid2Bytes = Arrays.asList("ADD".getBytes());
    List<byte[]> grid1And2Bytes = Arrays.asList("ABCDADAA".getBytes(), "ADD".getBytes());

    Map<String, List<byte[]>> expectedPayloadsPerToken = new HashMap<String, List<byte[]>>();
    expectedPayloadsPerToken.put("A", grid1And2Bytes);
    expectedPayloadsPerToken.put("AB", grid1Bytes);
    expectedPayloadsPerToken.put("ABC", grid1Bytes);
    expectedPayloadsPerToken.put("ABCD", grid1Bytes);
    expectedPayloadsPerToken.put("ABCDA", grid1Bytes);
    expectedPayloadsPerToken.put("ABCDAD", grid1Bytes);
    expectedPayloadsPerToken.put("ABCDADA", grid1Bytes);
    expectedPayloadsPerToken.put("ABCDADAA", grid1Bytes);
    expectedPayloadsPerToken.put("AD", grid2Bytes);
    expectedPayloadsPerToken.put("ADD", grid2Bytes);

    TokenStream tokenStream = new PayloadAwareEdgeNGramTokenFilter(
        new RemoveDuplicatesTokenFilter(
            new GridPayloadTokenFilter(
                new TruncateFilter(
                    new StringListTokenizer(grids), 10))),
        PayloadAwareEdgeNGramTokenFilter.Side.FRONT, 1, 20);

    CharTermAttribute termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
    PayloadAttribute payloadAttribute = tokenStream.getAttribute(PayloadAttribute.class);
    while (tokenStream.incrementToken()) {
      List<byte[]> payloadsForToken = expectedPayloadsPerToken.get(termAttribute.toString());
      assertTrue(containsArray(payloadsForToken, payloadAttribute.getPayload().getData()));
    }
  }

  private boolean containsArray(List<byte[]> list, byte[] array) {
    for (byte[] listArray : list) {
      boolean matches = true;

      for (int i = 0; i < listArray.length; i++) {
        if (listArray[i] != array[i]) {
          matches = false;
          break;
        }
      }
      if (matches) {
        return true;
      }
    }
    return false;
  }
}
