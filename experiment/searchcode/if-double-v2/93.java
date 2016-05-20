public class Vector2D
{
  public double x, y;
  
  public Vector2D( ) {
    x = y = 0;
  }
  public Vector2D( double x, double y ) {
    this.x = x;
    this.y = y;
  }
  public Vector2D( final Vector2D vector ) {
    x = vector.x;
    y = vector.y;
  }
  
  public void set( final Vector2D v1 ) {
    x = v1.x;
    y = v1.y;
  }
  
  public void add( final Vector2D v1) {
    x += v1.x;
    y += v1.y;
  }
  public static Vector2D add( final Vector2D v1, final Vector2D v2 ) {
    return new Vector2D( v1.x + v2.x, v1.y + v2.y );
  }
  public void sub( final Vector2D v1 ) {
    x -= v1.x;
    y -= v1.y;
  }
  public static Vector2D sub( final Vector2D v1, final Vector2D v2 ) {
    return new Vector2D( v1.x - v2.x, v1.y - v2.y );
  }
  public void scale( double s ) {
    x *= s;
    y *= s;
  }
  public static Vector2D scale( final Vector2D v1, double s ) {
    return new Vector2D( v1.x * s, v1.y * s);
  }
  public double dot( final Vector2D v1 ) {
    return ( x * v1.x ) + ( y * v1.y );
  }
  public static double dot( final Vector2D v1, final Vector2D v2 ) {
    return ( v1.x * v2.x ) + ( v1.y * v2.y );
  }
  public double magnitudeSquared( ) {
    return x*x + y*y;
  }
  public double magnitude( ) {
    return Math.sqrt( x*x + y*y );
  }
  public void norm( ) {
    double m = magnitude();
    if (m != 0) {
      x /= m;
      y /= m;
    }
  }
  public static Vector2D norm( final Vector2D v1 ) {
    double m = v1.magnitude();
    if (m != 0) return new Vector2D( v1.x / m, v1.y / m );
    else return new Vector2D( 0, 0 );
  }
  public Vector2D perp( ) {
    return new Vector2D( -y, x );
  }
  public void negate( ) {
    x = -x;
    y = -y;
  }
  
  public String toString(  ) {
    return "(" + x + "," + y + ")";
  }
}

