package org.apache.lucene.spatial.search.sketch;


/**
 * following:
 * http://blog.notdot.net/2009/11/Damn-Cool-Algorithms-Spatial-indexing-with-Quadtrees-and-Hilbert-Curves
 *
 * Not sure if there is an advantage to using hilbert numbering...
 */
public class HilbertSketch
{
  static class HilbertLink {
    HilbertState down;
    byte num;

    public HilbertLink( byte n, HilbertState down ) {
      this.down = down;
      this.num = n;
    }
  }

  static class HilbertState {
    HilbertLink c00;
    HilbertLink c01;
    HilbertLink c10;
    HilbertLink c11;

    HilbertState[] subs;
  }

  static final HilbertState U = new HilbertState();
  static final HilbertState C = new HilbertState();
  static final HilbertState A = new HilbertState();
  static final HilbertState D = new HilbertState();
  static {
    U.c00 = new HilbertLink( (byte)0, D );
    U.c01 = new HilbertLink( (byte)1, U );
    U.c10 = new HilbertLink( (byte)3, C );
    U.c11 = new HilbertLink( (byte)2, U );
    U.subs = new HilbertState[] { D, U, U, C };

    C.c00 = new HilbertLink( (byte)2, C );
    C.c01 = new HilbertLink( (byte)1, C );
    C.c10 = new HilbertLink( (byte)3, U );
    C.c11 = new HilbertLink( (byte)0, A );
    C.subs = new HilbertState[] { A, C, C, U };

    A.c00 = new HilbertLink( (byte)2, A );
    A.c01 = new HilbertLink( (byte)3, D );
    A.c10 = new HilbertLink( (byte)1, A );
    A.c11 = new HilbertLink( (byte)0, C );
    A.subs = new HilbertState[] { C, A, A, D };

    D.c00 = new HilbertLink( (byte)0, U );
    D.c01 = new HilbertLink( (byte)3, A );
    D.c10 = new HilbertLink( (byte)1, D );
    D.c11 = new HilbertLink( (byte)2, D );
    D.subs = new HilbertState[] { U,D,D,A };
  }
}
