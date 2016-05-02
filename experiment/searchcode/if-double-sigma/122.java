import java.util.Random;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.awt.geom.*;
import java.awt.Point;

class Noise {

  protected double[] z;
  int W, H;
  Random rand = new Random();

  Noise(int width, int height) {
    W = width;
    H = height;
    z = new double[W*H];
  }

  Noise(Noise clone) {
    W = clone.W;
    H = clone.H;
    // Deep clone
    z = (double[]) clone.z.clone();
  }


  double noise(int x, int y) {
    if (x < 0) x += W; else if (x >= W) x -= W;
    if (y < 0) y += H; else if (y >= H) y -= H;
    return z[x+y*W];
  }

  double noise(Point p) {
    return noise(p.x, p.y);
  }


  /** Generate white noise */
  void generateWhiteNoise() {
    for (int x = 0; x < W; ++x)
      for (int y = 0; y < H; ++y)
        z[x+y*W] = rand.nextGaussian();
  }


  /** Generate pink noise using Perlin noise */
  void generatePerlinNoise(double plane,
                           int maxScale, int minScale,
                           double alpha) {
    for (int x = 0; x < W; ++x)
      for (int y = 0; y < H; ++y)
        z[x+y*W] = PerlinNoise.pinkNoise(x,y,plane, maxScale,minScale, alpha);
  }


  /** Generate 1/f, aka pink, noise using midpoint displacement via the
      diamond-square algorithm */
  void generateMidpointDisplacementNoise(int n, double persistence) {
    if (persistence == 0.0) persistence = 0.5;  // default value, essentially

    if (n == 0) {
      // Find largest power of 2 <= to smallest dimension - that will
      // be our starting fractal scale
      for (n = 1; n <= Math.min(W, H) / 2; n += n) { }
    }

    final double SQRT2 = Math.sqrt(2.0);
    double sigma = 0.5;

    // Fill in initial values
    for (int y = 0; y < H; y += n)
      for (int x = 0; x < W; x += n)
        z[x+y*W] = rand.nextGaussian() * sigma;

    // At each scale level, apply diamond-square algorithm to
    // midpoints between existing values
    while (n > 1) {
      n /= 2;
      sigma *= persistence;
      for (int y = n; y < H; y += 2*n) {
        for (int x = n; x < W; x += 2*n) {
          z[x+y*W] = rand.nextGaussian() * sigma * SQRT2 + bishopavg(x,y,n);
        }
      }

      for (int y = 0; y < H; y += 2*n) {
        for (int x = n; x < W; x += 2*n) {
          z[x+y*W] = rand.nextGaussian() * sigma + rookavg(x,y,n);
        }
      }

      for (int y = n; y < H; y += 2*n) {
        for (int x = 0; x < W; x += 2*n) {
          z[x+y*W] = rand.nextGaussian() * sigma + rookavg(x,y,n);
        }
      }
    }
  }        

  private double bishopavg(int x, int y, int d) {
    return (noise(x-d,y-d) + noise(x+d,y-d) +
            noise(x-d,y+d) + noise(x+d,y+d)) / 4.0;
  }

  private double rookavg(int x, int y, int d) {
    return (        noise(x,y-d) +
            noise(x-d,y) + noise(x+d,y) +
                    noise(x,y+d)) / 4.0;
  }

  /** numFaults=200 works okay */
  void generateFaultNoise(int numFaults) {

    class Fault {
      boolean line;
      double x0, y0, radius;
      double m, b;
      double amplitude;
    }

    // Pick circle radius to cover half the area
    double radius = Math.sqrt(W * H / (2 * Math.PI));  

    List<Fault> formulae = new LinkedList<Fault>();

    // Generate set of faults
    for (int x = 0; x < numFaults; ++x) {
      Fault f = new Fault();
      f.line = rand.nextBoolean();
      if (f.line) {
        // displace x,y where y < mx + b
        double angle = rand.nextDouble() * Math.PI;  // angle of line
        f.m = Math.tan(angle);  // slope
        f.x0 = rand.nextDouble() * W;
        f.y0 = rand.nextDouble() * H;
        f.b = f.y0 - f.m * f.x0;
      } else {
        // Displace points within fixed radius of x0,y0
        f.x0 = -radius/2 + rand.nextDouble() * (W + radius);
        f.y0 = -radius/2 + rand.nextDouble() * (H + radius);
      }
      f.amplitude = rand.nextBoolean() ? 1 : -1;
      formulae.add(f);
    }
     
    for (int y = 0; y < H; ++y) {
      for (int x = 0; x < W; ++x) {
        z[x+y*W] = 0;
        for (Fault f : formulae) {
          if (f.line) {
            if (y > f.m * x + f.b)
              z[x+y*W] += f.amplitude;
          } else {
            double d = distance2(x, y, f.x0, f.y0);
            if (d < radius * radius)
              z[x+y*W] += f.amplitude;
          }
        }
      }
    }
  }


  /** Square of distance betweem <x0,y0> and <x1,y1> */
  static private double distance2(double x0, double y0, double x1, double y1) {
    return (x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1);
  }


  // Not sure what my original source was for this, but this is
  // related, though different:
  // http://www.gamedev.net/reference/articles/article900.asp
  /** Generate terrain as a sum of random sin waves */
  void generateSpectralNoise( int numwaves, double dimension, double maxtau) {
    double maxtaulog2 = Math.log(maxtau)/Math.log(2);
    double mintaulog2 = 1.0;

    class Wave {
      double amplitude, offset, rx, ry, nu;
    }

    List<Wave> waves = new LinkedList<Wave>();
    for (int i = 0; i < numwaves; ++i) {
      Wave w = new Wave();
      double taulog2 = mintaulog2 + i * (maxtaulog2-mintaulog2) / numwaves;
      double tau = Math.pow(2.0, taulog2);  // period
      w.nu = 1.0 / tau;  // What's nu? Frequency.
      double alpha = rand.nextDouble() * Math.PI;
      w.rx = Math.sin(alpha);
      w.ry = Math.cos(alpha);
      w.amplitude = Math.pow(tau, dimension);
      w.offset = rand.nextDouble() * 2 * Math.PI;
      waves.add(w);
    }

    for (int x = 0; x < W; ++x) {
      for (int y = 0; y < H; ++y) {
        z[x+y*W] = 0;
        for (Wave w : waves) {
          double t = w.rx * x + w.ry * y;  // Y coord of rotated x,y
          z[x+y*W] += w.amplitude * Math.sin(t * w.nu + w.offset);
        }
      }
    }

  }


  private double dotProduct(double[] a, double[] b) {
    double result = 0;
    for (int i = 0; i < a.length && i < b.length; ++i)
      result += a[i] * b[i];
    return result;
  }

  
  void generateVoronoi(int npoints, double[] coefficients) {

    double[] pointsx = new double[npoints];
    double[] pointsy = new double[npoints];
    for (int n = 0; n < npoints; ++n) {
      pointsx[n] = rand.nextDouble();
      pointsy[n] = rand.nextDouble();
    }

    for (int y = 0; y < H; ++y) {
      double fy = 1.0 * y / H;
      for (int x = 0; x < W; ++x) {
        double fx = 1.0 * x / W;
        double[] ranges = new double[npoints];
        for (int n = 0; n < npoints; ++n) {
          double dx = Math.abs(pointsx[n] - fx);
          double dy = Math.abs(pointsy[n] - fy);
          if (dx > 0.5) dx = 1.0 - dx;
          if (dy > 0.5) dy = 1.0 - dy;
          ranges[n] = Math.sqrt(dx*dx + dy*dy);
        }
        Arrays.sort(ranges);
        z[x+y*W] = dotProduct(coefficients, ranges);
      }
    }
  }

  private int leastbit(int n) {
    // Return position of the least significant 1 bit (or 1)
    if (0 == n) return 1;
    int m = 1;
    int v = 0;
    while (0 == (m & n)) {
      m += m;
      ++v;
    }
    return v;
  }

  /** Grid pattern for testing filters */
  void generateGrid() {
    for (int x = 0; x < W; ++x) 
      for (int y = 0; y < H; ++y) 
        z[x+y*W] = Math.max(leastbit(x), leastbit(y));
  }


  /** Normalize range to [-1,1] */
  void normalize() {
    normalize(-1,1);
  }

  /** Normalize range to [from,to] */
  void normalize(double from, double to) {
    double min, max;
    min = max = z[0];
    for (int x = 0; x < W; ++x) {
      for (int y = 0; y < H; ++y) {
        if (min > z[x+y*W])  min = z[x+y*W];
        if (max < z[x+y*W])  max = z[x+y*W];
      }
    }
    if (max != min) {
      for (int x = 0; x < W; ++x) 
        for (int y = 0; y < H; ++y) 
          z[x+y*W] = (z[x+y*W] - min) * ((to-from)/(max-min))  + from;
    } else {
      // Special case avoids div by 0
      for (int x = 0; x < W; ++x)  
        for (int y = 0; y < H; ++y) 
          z[x+y*W] = (from + to)/2;
    }

  }


  /** Normalize range to [from,0) and [0,to] */
  void binormalize(double from, double to) {
    double min, max;
    min = max = z[0];
    for (int x = 0; x < W; ++x) {
      for (int y = 0; y < H; ++y) {
        if (min > z[x+y*W])  min = z[x+y*W];
        if (max < z[x+y*W])  max = z[x+y*W];
      }
    }
    if (max != min) {
      for (int x = 0; x < W; ++x) 
        for (int y = 0; y < H; ++y) 
          if (z[x+y*W] < 0)
            z[x+y*W] = (z[x+y*W] - min) * (from/min) + from;
          else
            z[x+y*W] = z[x+y*W] * (to/max);
    } else {
      // Special case avoids div by 0
      for (int x = 0; x < W; ++x)  
        for (int y = 0; y < H; ++y) 
          z[x+y*W] = (from + to)/2;
    }

  }
    

  /** Smooth on the Von Neumann neighborhood; apply this filter:
      0 1 0
      1 2 1
      0 1 0
      This appears to be very close to twice as fast and half as
      smoothing as the Moore neighborhood version, with the same final
      quality */
  void smooth() {
    Noise d = new Noise(this);
    for (int x = 0; x < W; ++x) 
      for (int y = 0; y < H; ++y)
        z[x+y*W] = (        d.noise(x, y-1) +
          d.noise(x-1, y) + 2 * d.z[x+y*W]  + d.noise(x+1, y) +
                           d.noise(x, y+1)) / 6.0;
  }


  /** Smooth on the Moore neighborhood; apply this filter:
      1 2 1
      2 4 2
      1 2 1
      (Can you spot the pun?) */
  void smoothMore() { 
    Noise d = new Noise(this);
    for (int x = 0; x < W; ++x) 
      for (int y = 0; y < H; ++y)
        z[x+y*W] = (1.0/16) * 
          (    d.noise(x-1,y-1) + 2 * d.noise(x,y-1) +     d.noise(x+1,y-1) +
           2 * d.noise(x-1,y  ) + 4 * d.z[x+y*W]      + 2 * d.noise(x+1,y) +
               d.noise(x-1,y+1) + 2 * d.noise(x,y+1) +     d.noise(x+1,y+1));
  }


  public interface Filter {
    double filter(int x, int y, double v);
  }
  public void filter(Filter filter) {  // Say "filter" again, motherfilter!
    for (int x = 0; x < W; ++x) 
      for (int y = 0; y < H; ++y)
        z[x+y*W] = filter.filter(x, y, z[x+y*W]);
  }


  private static double s_curve(double t) {
    return t * t * (3.0 - 2.0 * t);
  }

  private static double mod(double dividend, double divisor) {
    double r = dividend % divisor;
    if (r < 0) r += divisor;
    return r;
  }

  private double interpolate(double x, double y) {
    //System.out.printf("%g %g\n", x, y);
    Point p0 = new Point(((int)x+W) % W, ((int)y+H) % H);
    Point p1 = new Point((p0.x + 1) % W, (p0.y + 1) % H);
    Point2D.Double f = new Point2D.Double(s_curve(mod(x, 1.0)), 
                                          s_curve(mod(y, 1.0)));
    double x0 = noise(p0.x,p0.y) * (1.0-f.x) + noise(p1.x,p0.y) * f.x;
    double x1 = noise(p0.x,p1.y) * (1.0-f.x) + noise(p1.x,p1.y) * f.x;
    return x0 * (1.0-f.y) + x1 * f.y;
  }
  
  /** Linear interpolation from a to b as t goes from 0 to 1 */
  private static Point2D.Double linterp(Point2D.Double a, 
                                        Point2D.Double b, double t) {
    return new Point2D.Double(a.x * (1.0 - t) + b.x * t,
                              a.y * (1.0 - t) + b.y * t);
  }

  /** Laterally distort height field (move the height field around on
      the horizonal plane, that is). Sample parameter values:
      blocksize=16, deviation=4 */
  void distort(int blocksize, double deviation) {
    int GW = W/blocksize;
    int GH = H/blocksize;
    Point2D.Double[] g = new Point2D.Double[GW * GH];
    for (int i = 0; i < GW; ++i) 
      for (int j = 0; j < GH; ++j)
        g[i+j*GW] = new Point2D.Double(rand.nextGaussian() * deviation,
                                       rand.nextGaussian() * deviation);
    
    Noise zr = new Noise(this);
    for (int y = 0; y < H; ++y) {
      int y0 = y/blocksize;
      int y1 = (y0 + 1) % (H/blocksize);
      double fy = s_curve((double)y/blocksize - y0);
      for (int x = 0; x < W; ++x) {
        int x0 = x/blocksize;
        int x1 = (x0 + 1) % (W/blocksize);
        double fx = s_curve((double)x/blocksize - x0);
        Point2D.Double gy0 = linterp(g[x0+y0*GW], g[x1+y0*GW], fx);
        Point2D.Double gy1 = linterp(g[x0+y1*GW], g[x1+y1*GW], fx);
        Point2D.Double gt = linterp(gy0, gy1, fy);
        zr.z[x+y*W] = interpolate(x + gt.x, y + gt.y);
      }
    }
    z = zr.z;
  }

  /** Apply lateral distortion on a range of orders of magnitude.
      Sample parameter values: degree=0.125, maxBlocksize=2^5 */
  void distortFractally(double degree, int maxBlocksize) {
    for (int r = 1; r <= maxBlocksize; r += r) 
      distort(r, r * degree);
  }


  void exp(double exponent) {
    for (int x = 0; x < W; ++x) {
      for (int y = 0; y < H; ++y) {
        if (z[x+y*W] > 0)
          z[x+y*W] = Math.pow(z[x+y*W], exponent);
      }
    }
  }

}
