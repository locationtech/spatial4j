package org.apache.lucene.spatial.search.prefix;

import java.util.List;

import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.search.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.search.SpatialIndexer;

/**
 * @author Chris Male
 */
public class PrefixGridSpatialIndexer extends SpatialIndexer<SimpleSpatialFieldInfo> {

  private final SpatialPrefixGrid prefixGrid;
  private final int maxLength;

  public PrefixGridSpatialIndexer(SpatialPrefixGrid prefixGrid, int maxLength) {
    this.prefixGrid = prefixGrid;
    this.maxLength = maxLength;
  }

  @Override
  public Fieldable createField(SimpleSpatialFieldInfo indexInfo, Shape shape, boolean index, boolean store) {
    List<CharSequence> match = prefixGrid.readCells(shape);
    BasicGridFieldable f = new BasicGridFieldable(indexInfo.getFieldName(), store);
    if (maxLength > 0) {
      f.tokens = new RemoveDuplicatesTokenFilter(
          new TruncateFilter(new StringListTokenizer(match), maxLength));
    } else {
      f.tokens = new StringListTokenizer(match);
    }

    if (store) {
      f.value = match.toString(); //reader.toString( shape );
    }
    return f;
  }
}
