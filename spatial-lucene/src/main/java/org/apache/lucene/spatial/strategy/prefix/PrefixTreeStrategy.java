package org.apache.lucene.spatial.strategy.prefix;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.distance.EuclidianDistanceCalculator;
import org.apache.lucene.spatial.base.prefix.Node;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixTree;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.SpatialStrategy;
import org.apache.lucene.spatial.strategy.util.CachedDistanceValueSource;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class PrefixTreeStrategy extends SpatialStrategy<SimpleSpatialFieldInfo> {
  protected final SpatialPrefixTree grid;
  private final Map<String, PointPrefixTreeFieldCacheProvider> provider = new ConcurrentHashMap<String, PointPrefixTreeFieldCacheProvider>();
  protected int defaultFieldValuesArrayLen = 2;
  protected double distErrPct = SpatialArgs.DEFAULT_DIST_PRECISION;

  public PrefixTreeStrategy(SpatialPrefixTree grid) {
    this.grid = grid;
  }

  /** Used in the in-memory ValueSource as a default ArrayList length for this field's array of values, per doc. */
  public void setDefaultFieldValuesArrayLen(int defaultFieldValuesArrayLen) {
    this.defaultFieldValuesArrayLen = defaultFieldValuesArrayLen;
  }

  /** See {@link SpatialPrefixTree#getMaxLevelForPrecision(org.apache.lucene.spatial.base.shape.Shape, double)}. */
  public void setDistErrPct(double distErrPct) {
    this.distErrPct = distErrPct;
  }

  @Override
  public Fieldable createField(SimpleSpatialFieldInfo fieldInfo, Shape shape, boolean index, boolean store) {
    int detailLevel = grid.getMaxLevelForPrecision(shape,distErrPct);
    List<Node> cells = grid.getNodes(shape, detailLevel, true);//true=intermediates cells
    //If shape isn't a point, add a full-resolution center-point so that
    // PrefixFieldCacheProvider has the center-points.
    // TODO index each center of a multi-point? Yes/no?
    if (!(shape instanceof Point)) {
      Point ctr = shape.getCenter();
      //TODO should be smarter; don't index 2 tokens for this in CellTokenizer. Harmless though.
      cells.add(grid.getNodes(ctr,grid.getMaxLevels(),false).get(0));
    }
    BasicPrefixTreeFieldable fieldable = new BasicPrefixTreeFieldable(fieldInfo.getFieldName(), store);
    fieldable.tokens = new CellTokenizer(cells.iterator());

    if (store) {//TODO figure out how to re-use original string instead of reconstituting it.
      fieldable.value = grid.getSpatialContext().toString(shape);
    }

    return fieldable;
  }

  /** Outputs the tokenString of a cell, and if its a leaf, outputs it again with the leaf byte. */
  static class CellTokenizer extends Tokenizer {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    private Iterator<Node> iter = null;

    public CellTokenizer(Iterator<Node> tokens) {
      this.iter = tokens;
    }

    CharSequence nextTokenStringNeedingLeaf = null;

    @Override
    public final boolean incrementToken() throws IOException {
      clearAttributes();
      if (nextTokenStringNeedingLeaf != null) {
        termAtt.setLength(0);
        termAtt.append(nextTokenStringNeedingLeaf);
        termAtt.append((char) Node.LEAF_BYTE);
        nextTokenStringNeedingLeaf = null;
        return true;
      }
      if (iter.hasNext()) {
        Node cell = iter.next();
        termAtt.setLength(0);
        CharSequence token = cell.getTokenString();
        termAtt.append(token);
        if (cell.isLeaf())
          nextTokenStringNeedingLeaf = token;
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

  @Override
  public ValueSource makeValueSource(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo) {
    DistanceCalculator calc = new EuclidianDistanceCalculator();
    return makeValueSource(args, fieldInfo,calc);
  }
  
  public ValueSource makeValueSource(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo, DistanceCalculator calc) {
    PointPrefixTreeFieldCacheProvider p = provider.get( fieldInfo.getFieldName() );
    if( p == null ) {
      synchronized (this) {//double checked locking idiom is okay since provider is threadsafe
        p = provider.get( fieldInfo.getFieldName() );
        if (p == null) {
          p = new PointPrefixTreeFieldCacheProvider(grid, fieldInfo.getFieldName(), defaultFieldValuesArrayLen);
          provider.put(fieldInfo.getFieldName(),p);
        }
      }
    }
    Point point = args.getShape().getCenter();
    return new CachedDistanceValueSource(point, calc, p);
  }

  public SpatialPrefixTree getGrid() {
    return grid;
  }
}
