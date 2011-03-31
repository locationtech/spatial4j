package org.apache.lucene.spatial.strategy.geohash;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.util.BytesRef;

/**
 * An abstraction for encoding details of a hierarchical grid reference system.
 */
public class GridReferenceSystem {

  //TODO incorporate a user specifiable Projection (maps lon-lat to x-y and back)

  //TODO consider alternate more efficient implementation instead of GeoHash.

  final SpatialContext shapeIO;
  final int maxLen;

  public GridReferenceSystem(SpatialContext shapeIO, int maxLen) {
    int MAXP = getMaxPrecision();
    if (maxLen <= 0 || maxLen > MAXP)
      throw new IllegalArgumentException("maxLen must be (0-"+MAXP+"] but got "+maxLen);
    this.maxLen = maxLen;
    this.shapeIO = shapeIO;
  }

  public static int getMaxPrecision() { return GeoHashUtils.PRECISION; }

  public int getGridSize() { return GeoHashUtils.BASE; }

  public List<GridNode> getSubNodes(BBox r) {
    double width = r.getMaxX() - r.getMinX();
    double height = r.getMaxY() - r.getMinY();
    int len = GeoHashUtils.lookupHashLenForWidthHeight(width,height);
    len = Math.min(len,maxLen-1);

    Set<String> cornerGeoHashes = new TreeSet<String>();
    cornerGeoHashes.add(encodeXY(r.getMinX(),r.getMinY(), len));
    cornerGeoHashes.add(encodeXY(r.getMinX(),r.getMaxY(), len));
    cornerGeoHashes.add(encodeXY(r.getMaxX(),r.getMaxY(), len));
    cornerGeoHashes.add(encodeXY(r.getMaxX(),r.getMinY(), len));

    List<GridNode> nodes = new ArrayList<GridNode>(getGridSize()*cornerGeoHashes.size());
    for (String hash : cornerGeoHashes) {//happens in sorted order
      nodes.addAll(getSubNodes(hash));
    }
    return nodes;//should be sorted
  }

  /** Gets an ordered set of nodes directly contained by the given node.*/
  private List<GridNode> getSubNodes(String baseHash) {
    String[] hashes = GeoHashUtils.getSubGeoHashes(baseHash);
    ArrayList<GridNode> nodes = new ArrayList<GridNode>(hashes.length);
    for (String hash : hashes) {
      BytesRef byteRef = new BytesRef(hash);
      BBox rect = GeoHashUtils.decodeBoundary(hash,shapeIO);// min-max lat, min-max lon
      nodes.add(new GridNode(this, byteRef, rect));
    }
    return nodes;
  }

  public List<GridNode> getSubNodes(GridNode node) {
    String baseHash = node == null ? "" : node.thisTerm.utf8ToString();
    return getSubNodes(baseHash);
  }

  public String encodeXY(double x, double y, int len) {
    return GeoHashUtils.encode(y, x, len);
  }

  public String encodeXY(double x, double y) {
    return GeoHashUtils.encode(y, x, maxLen);
  }

  //TODO return Point2D ?
  public double[] decodeXY(Fieldable f) {
    double[] latLon = GeoHashUtils.decode(f.stringValue(),shapeIO);
    //flip to XY
    double y = latLon[0];
    latLon[0] = latLon[1];
    latLon[1] = y;
    return latLon;
  }

  public Point decodeXY(BytesRef term) {
    double[] latLon = GeoHashUtils.decode(term.utf8ToString(),shapeIO);
    return shapeIO.makePoint(latLon[1],latLon[0]);
  }

}