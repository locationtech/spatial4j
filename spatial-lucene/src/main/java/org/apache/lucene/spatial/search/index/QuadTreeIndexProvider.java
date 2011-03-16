package org.apache.lucene.spatial.search.index;

import java.io.IOException;
import java.util.WeakHashMap;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.spatial.core.BBox;
import org.apache.lucene.spatial.core.Shape;
import org.apache.lucene.spatial.core.ShapeReader;
import org.apache.lucene.spatial.core.jts.JtsEnvelope;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.index.strtree.STRtree;

public class QuadTreeIndexProvider extends CachedIndexProvider
{
  public QuadTreeIndexProvider( String shapeField, ShapeReader reader )
  {
    super( shapeField, reader );
  }
  
  protected SpatialIndex createEmptyIndex()
  {
    return new Quadtree();
  }
}
