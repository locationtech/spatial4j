package voyager.quads.hilbert;

public class Battenburg
{
  double a;
  double b;
  double c;
  double d;

  public Battenburg( double a, double b, double c, double d ) {
    this.a = a;
    this.b = b;
    this.c = c;
    this.d = d;
  }

  public Battenburg mirror_x() {
    return new Battenburg(this.c, this.d, this.a, this.b);
  }

  /**
   * Returns a new battenburg rotated 90 degress clockwise.
   */
  public Battenburg rotate_c(){
    return new Battenburg(this.c, this.a, this.d, this.b);
  }

  /**
   * Returns a new battenburg rotated 90 degress counter-clockwise.
   */
  public Battenburg rotate_cc(){
    return new Battenburg(this.b, this.d, this.a, this.c);
  }

  public Battenburg divide_by_scalar( double div ) {
    return new Battenburg(this.a / div, this.b / div, this.c / div, this.d /div);
  }

  public Battenburg add( Battenburg bb ) {
    return new Battenburg(this.a + bb.a, this.b + bb.b, this.c + bb.c, this.d + bb.d);
  }


  public static double hilbert_2d_to_1d(double x, double y, int recursion, Battenburg curve)
  {
    //console.log(x, y, recursion, curve);
    if (curve == null) {
      curve = new Battenburg(0.0, 0.25, 0.75, 0.5);
    }
    if(recursion < -10 ) {
      recursion = 28; // a good number for doubles
    }
    if (recursion <= 0) {
      return 0;
    }

    double retv = -1;
    if (x >= 0.0 && x < 0.5 && y >= 0.0 && y < 0.5) {
      retv = curve.a + hilbert_2d_to_1d( x*2, y*2, recursion-1, curve.mirror_x().rotate_c() ) / 4;
    }
    if (x >= 0.5 && x < 1.0 && y >= 0.0 && y < 0.5) {
      retv = curve.b + hilbert_2d_to_1d( (x - 0.5)*2, y*2, recursion-1, curve ) / 4;
    }
    if (x >= 0.0 && x < 0.5 && y >= 0.5 && y < 1.0) {
      retv = curve.c + hilbert_2d_to_1d( x*2, (y - 0.5)*2, recursion-1, curve.mirror_x().rotate_cc() ) / 4;
    }
    if (x >= 0.5 && x < 1.0 && y >= 0.5 && y < 1.0) {
      retv = curve.d + hilbert_2d_to_1d( (x - 0.5)*2, (y - 0.5)*2, recursion-1, curve ) / 4;
    }
    return retv;
  }



  public static double[] hilbert_1d_to_2d(double d, int recursion, Battenburg curve) {
    if (curve == null) {
      curve = new Battenburg(0.0, 0.25, 0.75, 0.5);
    }
    if(recursion < -10 ) {
      recursion = 28; // a good number for doubles
    }
    if (recursion <= 0) {
      return new double[] { 0, 0 };
    }

    if (d >= curve.a && d < curve.a + 0.25) {
      double d_ = (d - curve.a) * 4;
      double[] adj = hilbert_1d_to_2d(d_, recursion - 1, curve.rotate_cc().mirror_x());
      return new double[] { 0 + adj[0]/2, 0 + adj[1]/2 };
    }
    if (d >= curve.b && d < curve.b + 0.25) {
      double d_ = (d - curve.b) * 4;
      double[] adj = hilbert_1d_to_2d(d_, recursion - 1, curve);
      return new double[] { 0.5 + adj[0]/2, 0 + adj[1]/2 };
    }
    if (d >= curve.c && d < curve.c + 0.25) {
      double d_ = (d - curve.c) * 4;
      double[] adj = hilbert_1d_to_2d(d_, recursion - 1, curve.rotate_c().mirror_x());
      return new double[] {0 + adj[0]/2, 0.5 + adj[1]/2 };
    }
    if (d >= curve.d && d < curve.d + 0.25) {
      double d_ = (d - curve.d) * 4;
      double[] adj = hilbert_1d_to_2d(d_, recursion - 1, curve);
      return new double[] { 0.5 + adj[0]/2, 0.5 + adj[1]/2 };
    }
    return null; // ??
  }

}
