package voyager.quads.hilbert;

import java.text.NumberFormat;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class HilbertSpatialGrid
{
  enum DIRECTION {
    UP,
    LEFT,
    DOWN,
    RIGHT,
  };

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
    DIRECTION[] dir;
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
    U.dir = new DIRECTION[] { DIRECTION.DOWN, DIRECTION.RIGHT, DIRECTION.UP };

    C.c00 = new HilbertLink( (byte)2, C );
    C.c01 = new HilbertLink( (byte)1, C );
    C.c10 = new HilbertLink( (byte)3, U );
    C.c11 = new HilbertLink( (byte)0, A );
    C.subs = new HilbertState[] { A, C, C, U };
    C.dir = new DIRECTION[] { DIRECTION.LEFT, DIRECTION.UP, DIRECTION.RIGHT };

    A.c00 = new HilbertLink( (byte)2, A );
    A.c01 = new HilbertLink( (byte)3, D );
    A.c10 = new HilbertLink( (byte)1, A );
    A.c11 = new HilbertLink( (byte)0, C );
    A.subs = new HilbertState[] { C, A, A, D };
    A.dir = new DIRECTION[] { DIRECTION.UP, DIRECTION.LEFT, DIRECTION.DOWN };

    D.c00 = new HilbertLink( (byte)0, U );
    D.c01 = new HilbertLink( (byte)3, A );
    D.c10 = new HilbertLink( (byte)1, D );
    D.c11 = new HilbertLink( (byte)2, D );
    D.subs = new HilbertState[] { U,D,D,A };
    D.dir = new DIRECTION[] { DIRECTION.RIGHT, DIRECTION.DOWN, DIRECTION.LEFT };
  }


  final double xmin;
  final double xmax;
  final double ymin;
  final double ymax;
  final int maxLevels;

  final double gridW;
  final double gridH;

  final double[] levelW;
  final double[] levelH;
  final int[]    levelS;
  final int[]    levelN;
  final HilbertState[] startState;

  int resolution = 3; // how far down past the 'bbox level'


  public HilbertSpatialGrid( double xmin, double xmax, double ymin, double ymax, int maxLevels )
  {
    this.xmin = xmin;
    this.xmax = xmax;
    this.ymin = ymin;
    this.ymax = ymax;
    this.maxLevels = maxLevels;

    levelW = new double[maxLevels];
    levelH = new double[maxLevels];
    levelS = new int[maxLevels];
    levelN = new int[maxLevels];
    startState = new HilbertState[maxLevels];

    gridW = xmax - xmin;
    gridH = ymax - ymin;
    levelW[0] = gridW/2.0;
    levelH[0] = gridH/2.0;
    levelS[0] = 2;
    levelN[0] = 4;
    startState[0] = U;

    for( int i=1; i<maxLevels; i++ ) {
      levelW[i] = levelW[i-1]/2.0;
      levelH[i] = levelH[i-1]/2.0;
      levelS[i] = levelS[i-1]*2;
      levelN[i] = levelN[i-1]*4;
      startState[i] = startState[i-1].c00.down;
    }
  }

  public HilbertSpatialGrid()
  {
    this( -180, 180, -90, 90, 12 );
  }

  public void printInfo()
  {
    NumberFormat nf = NumberFormat.getNumberInstance();
    nf.setMaximumFractionDigits( 5 );
    nf.setMinimumFractionDigits( 5 );
    nf.setMinimumIntegerDigits( 3 );

    for( int i=0; i<maxLevels; i++ ) {
      System.out.println( i + "]\t"+nf.format(levelW[i])+"\t"+nf.format(levelH[i])+"\t"+levelS[i]+"\t"+(levelS[i]*levelS[i]) );
    }
  }

  public int getBBoxLevel( Geometry geo )
  {
    if( geo instanceof Point ) {
      return maxLevels;
    }
    Envelope env = geo.getEnvelopeInternal();
    if( env == null ) {
      return -1;
    }
    double w = env.getWidth();
    double h = env.getHeight();

    for( int i=0; i<maxLevels; i++ ) {
      if( w > levelW[i] ) return i;
      if( h > levelH[i] ) return i;
    }
    return maxLevels;
  }
}
