package org.apache.lucene.spatial.strategy.prefix;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.index.Payload;

import java.io.IOException;

/**
 * @author Chris Male
 */
class PayloadAwareEdgeNGramTokenFilter extends TokenFilter {

  public static final Side DEFAULT_SIDE = Side.FRONT;
  public static final int DEFAULT_MAX_GRAM_SIZE = 1;
  public static final int DEFAULT_MIN_GRAM_SIZE = 1;

  /** Specifies which side of the input the n-gram should be generated from */
  public static enum Side {

    /** Get the n-gram from the front of the input */
    FRONT {
      @Override
      public String getLabel() { return "front"; }
    },

    /** Get the n-gram from the end of the input */
    BACK  {
      @Override
      public String getLabel() { return "back"; }
    };

    public abstract String getLabel();

    // Get the appropriate Side from a string
    public static Side getSide(String sideName) {
      if (FRONT.getLabel().equals(sideName)) {
        return FRONT;
      }
      if (BACK.getLabel().equals(sideName)) {
        return BACK;
      }
      return null;
    }
  }

  private final int minGram;
  private final int maxGram;
  private Side side;
  private char[] curTermBuffer;
  private int curTermLength;
  private int curGramSize;
  private int tokStart;

  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
  private final PayloadAttribute payloadAttribute = addAttribute(PayloadAttribute.class);

  /**
   * Creates EdgeNGramTokenFilter that can generate n-grams in the sizes of the given range
   *
   * @param input {@link org.apache.lucene.analysis.TokenStream} holding the input to be tokenized
   * @param side the {@link Side} from which to chop off an n-gram
   * @param minGram the smallest n-gram to generate
   * @param maxGram the largest n-gram to generate
   */
  public PayloadAwareEdgeNGramTokenFilter(TokenStream input, Side side, int minGram, int maxGram) {
    super(input);

    if (side == null) {
      throw new IllegalArgumentException("sideLabel must be either front or back");
    }

    if (minGram < 1) {
      throw new IllegalArgumentException("minGram must be greater than zero");
    }

    if (minGram > maxGram) {
      throw new IllegalArgumentException("minGram must not be greater than maxGram");
    }

    this.minGram = minGram;
    this.maxGram = maxGram;
    this.side = side;
  }

  /**
   * Creates EdgeNGramTokenFilter that can generate n-grams in the sizes of the given range
   *
   * @param input {@link TokenStream} holding the input to be tokenized
   * @param sideLabel the name of the {@link Side} from which to chop off an n-gram
   * @param minGram the smallest n-gram to generate
   * @param maxGram the largest n-gram to generate
   */
  public PayloadAwareEdgeNGramTokenFilter(TokenStream input, String sideLabel, int minGram, int maxGram) {
    this(input, Side.getSide(sideLabel), minGram, maxGram);
  }

  @Override
  public final boolean incrementToken() throws IOException {
    while (true) {
      if (curTermBuffer == null) {
        if (!input.incrementToken()) {
          return false;
        } else {
          curTermBuffer = termAtt.buffer().clone();
          curTermLength = termAtt.length();
          curGramSize = minGram;
          tokStart = offsetAtt.startOffset();
        }
      }
      if (curGramSize <= maxGram) {
        if (! (curGramSize > curTermLength         // if the remaining input is too short, we can't generate any n-grams
            || curGramSize > maxGram)) {       // if we have hit the end of our n-gram size range, quit
          // grab gramSize chars from front or back
          int start = side == Side.FRONT ? 0 : curTermLength - curGramSize;
          int end = start + curGramSize;

          Payload payload = payloadAttribute.getPayload();
          clearAttributes();
          payloadAttribute.setPayload(payload);
          offsetAtt.setOffset(tokStart + start, tokStart + end);
          termAtt.copyBuffer(curTermBuffer, start, curGramSize);
          curGramSize++;
          return true;
        }
      }
      curTermBuffer = null;
    }
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    curTermBuffer = null;
  }
}
