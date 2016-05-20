import java.util.*;
import java.awt.Point;

class Terrain extends Noise {

  Terrain(int w, int h) {
    super(w,h);
  }

  // TODO: could probably avoid the copy by doing passes in
  // alternating directions

  void erode(double threshold, double limit) {
    final int[] neighbordx = {   0, W-1, 0, 1,   W-1,   1, W-1, 1 };
    final int[] neighbordy = { H-1,   0, 1, 0,   H-1, H-1,   1, 1 }; 
    int nneighbors = 8;  // 4 for Von Neumann, 8 for Moore neighborhood

    Noise o = new Noise(this);

    for (int x = 0; x < W; ++x) {
      for (int y = 0; y < H; ++y) {
        double h = o.noise(x,y);
        double dmax = 0;
        int xmax=0, ymax=0;
        for (int i = 0; i < nneighbors; ++i) {
          int xi = (x + neighbordx[i]) % W;
          int yi = (y + neighbordy[i]) % H;
          double hi = o.noise(xi,yi);
          double di = h - hi;
          if (dmax < di) {
            dmax = di;
            xmax = xi;
            ymax = yi;
          }
        }

        if (threshold < dmax && dmax <= limit) {
          double dh = dmax / 2.0;
          z[x+y*W] -= dh;
          z[xmax+ymax*W] += dh;
        }
      }
    }
  }

  double[] water = null;
  double[] sediment = null;

  double wmean = 0;
  double wdev = 0;

  private double altitude(int x, int y) {
    if (x < 0) x += W; else if (x >= W) x -= W;
    if (y < 0) y += H; else if (y >= H) y -= H;
    return z[x+y*W] + water[x+y*W];
  }


  /** Implementation of reference hydraulic erosion described in
      "Realtime Procedural Terrain Generation" by Jacob Olsen without
      the optimizations described there, except the use of Von Neuman
      rather than Moore neighborhood and with immediate effect. */
  void hydraulicErosion(double rain, double solubility, 
                        double evaporation, double capacity) {
    if (water == null || sediment == null) {
      water = new double[W*H];
      sediment = new double[W*H];
      for (int x = 0; x < W; ++x) 
        for (int y = 0; y < H; ++y) 
          water[x+y*W] = sediment[x+y*W] = 0.0;
    }

    final int[] neighbordx = {   0, W-1, 0, 1,   W-1,   1, W-1, 1 };
    final int[] neighbordy = { H-1,   0, 1, 0,   H-1, H-1,   1, 1 }; 
    int nneighbors = 8;  // 4 for Von Neumann, 8 for Moore neighborhood

    // 1. Appearance of new water
    for (int x = 0; x < W; ++x) 
      for (int y = 0; y < H; ++y) 
        water[x+y*W] += rain;
    // 2. Dissolution
    for (int x = 0; x < W; ++x) {
      for (int y = 0; y < H; ++y) { 
        double dissolved = solubility * water[x+y*W];
        z[x+y*W] -= dissolved;
        sediment[x+y*W] += dissolved;
      }
    }
    // 3. Transport
    double[] d = new double[nneighbors];
    for (int x = 0; x < W; ++x) {
      for (int y = 0; y < H; ++y) { 
        double a = altitude(x,y);
        double w = water[x+y*W];
        double dtotal = 0;
        double atotal = 0;
        int n = 0;
        for (int i = 0; i < nneighbors; ++i) {
          int xi = (x + neighbordx[i]) % W;
          int yi = (y + neighbordy[i]) % H;
          double ai = z[xi+yi*W] + water[xi+yi*W];
          d[i] = a - ai;
          if (d[i] > 0) {
            dtotal += d[i];
            atotal += ai;
            ++n;
          }
        }
        if (n == 0) continue;
        double amean = atotal / n;
        for (int i = 0; i < nneighbors; ++i) {
          if (d[i] <= 0) continue;
          int xi = (x + neighbordx[i]) % W;
          int yi = (y + neighbordy[i]) % H;
          double Da = a - amean;
          double Dwi = Math.min(w, Da) * (d[i] / dtotal);
          water[x+y*W] -= Dwi;
          water[xi+yi*W] += Dwi;
          double Dmi = sediment[x+y*W] * Dwi / w;
          sediment[x+y*W] -= Dmi;
          sediment[xi+yi*W] += Dmi;
        }
      }
    }
    // 4. Evaporation & deposition
    double wtotal = 0;
    double wwtotal = 0;
    int wcount = 0;
    for (int x = 0; x < W; ++x) {
      for (int y = 0; y < H; ++y) { 
        water[x+y*W] *= (1.0 - evaporation);
        double mmax = capacity * water[x+y*W];
        double Dm = sediment[x+y*W] - mmax;
        if (Dm > 0) {
          sediment[x+y*W] -= Dm;
          z[x+y*W] += Dm;
        }

        if (z[x+y*W] > 0) {
          wcount++;
          wtotal += water[x+y*W];
          wwtotal += water[x+y*W] * water[x+y*W];
        }
      }
    }
    wmean = wtotal / wcount;
    wdev = Math.sqrt(wwtotal / wcount - wmean * wmean);
  }

  /** Make a valid point out of an x,y that might be a bit off the map */
  private Point wrap(int x, int y) {
    return new Point((x + W) % W,
                     (y + H) % H);
  }

  private Point[] vnn(int x, int y) {
    return new Point[] { wrap(x,y-1), wrap(x-1,y), wrap(x+1,y), wrap(x,y+1) };
  }

  /** Fill in local minima so that the entire above-sea-level portion of
      the map flows downhill to the sea. Not sure if this is worth anything. */
  void fill() {
    double hull[] = new double[W*H];
    Set<Point> todo = new HashSet<Point>();
    for (int x = 0; x < W; ++x) {
      for (int y = 0; y < H; ++y) {
        if (z[x+y*W] <= 0) {
          hull[x+y*W] = z[x+y*W];
          for (Point n : vnn(x,y)) 
            if (z[n.x+n.y*W] > 0)
              todo.add(n);
        } else { 
          hull[x+y*W] = Double.MAX_VALUE;
        }
      }
    }
    while (!todo.isEmpty()) {
      Point p = todo.iterator().next();
      todo.remove(p);
      double b = Double.MAX_VALUE;
      for (Point n : vnn(p.x, p.y)) 
        if (b > hull[n.x+n.y*W])
          b = hull[n.x+n.y*W];
      b = Math.max(z[p.x+p.y*W], b);
      if (hull[p.x+p.y*W] > b) {
        hull[p.x+p.y*W] = b;
        for (Point n : vnn(p.x,p.y)) 
          if (Math.max(b, z[n.x+n.y*W]) < hull[n.x+n.y*W])
            todo.add(n);
      }
    }
    z = hull;
  }

  void flow() {
    double[] water = new double[W*H];
    double[] flow = new double[W*H];
    for (int x = 0; x < W; ++x) {
      for (int y = 0; y < H; ++y) {
        flow[x+y*W] = 0.0;
        water[x+y*W] = 1.0;
      }
    }
    int count = 1;
    while (count > 0) {
      count = 0;
      for (int x = 0; x < W; ++x) {
        for (int y = 0; y < H; ++y) {
          if (z[x+y*W] > 0 && water[x+y*W] > 0) {
            ++count;
            Point lowest = null;
            double low = z[x+y*W];
            for (Point n : vnn(x,y)) {
              if (low > z[n.x+n.y*W]) {
                lowest = n;
                low = z[n.x+n.y*W];
              }
            }
            if (lowest != null) {
              flow[ lowest.x+lowest.y*W] += water[x+y*W];
              water[lowest.x+lowest.y*W] += water[x+y*W];
            }
            water[x+y*W] = 0;
          }
        }
      }
    }
    double wtotal = 0;
    double wwtotal = 0;
    int wcount = 0;
    for (int x = 0; x < W; ++x) {
      for (int y = 0; y < H; ++y) { 
        if (z[x+y*W] > 0) {
          wcount++;
          wtotal += flow[x+y*W];
          wwtotal += flow[x+y*W] * flow[x+y*W];
        }
      }
    }
    wmean = wtotal / wcount;
    wdev = Math.sqrt(wwtotal / wcount - wmean * wmean);
    for (int x = 0; x < W; ++x) 
      for (int y = 0; y < H; ++y) 
        if (flow[x+y*W] > wmean) 
          z[x+y*W] -= 0.001 * (flow[x+y*W] - wmean) / wdev;
    this.water = flow;
  }


  /*
  private double heuristic(Point start, Point end) {
    return Math.sqrt(Math.pow(start.x - end.x, 2) +
                     Math.pow(start.y - end.y, 2)) + 
      Math.abs(noise(start.x, start.y) - noise(end.x, end.y));
  }

  private Set<Point> nbhd(Point p) {
    Set<Point> result = new HashSet<Point>();
    if (p.x > 0)   result.add(new Point(p.x-1, p.y));
    if (p.y > 0)   result.add(new Point(p.x,   p.y-1));
    if (p.x < W-1) result.add(new Point(p.x+1, p.y));
    if (p.y < H-1) result.add(new Point(p.x,   p.y+1));
    return result;
  }

  List<Point> findPath(Point start,  // starting coordinate
                       Point goal)//,   // finish coordinates
                     //                 nbhd,   // nbhd(p0) is the set of p1 in its neighborhood
                     //                  cost,   // cost(p0,p1) is actual cost of moving from p0 to p1
                     //                  heuristic)  // heuristic(p,goal) ests cost from p to goal  
  {
  
    class Path {
      double fp;
      LinkedList<Point> xy;
      LinkedList<Double> gx;
      Path(double fp, Point starter, double gx) { 
        this.fp = fp; 
        this.xy = new LinkedList<Point>(); this.xy.add(starter);
        this.gx = new LinkedList<Double>(); this.gx.add(gx); 
      }
      Path(double fp, Path starter, Point nextp, double nextg) { 
        this.fp = fp; 
        this.xy = (LinkedList<Point>) starter.xy.clone(); 
        this.xy.add(nextp);
        this.gx = (LinkedList<Double>) starter.gx.clone(); 
        this.gx.add(nextg); 
      }
    };
    Set<Point> closed = new HashSet<Point>();
    PriorityQueue<Path> q = new PriorityQueue<Path>();
    q.add(new Path(heuristic(start,goal), start, 0));
    while (!q.isEmpty()) {
      Path p = q.poll();
      Point x = p.xy.getLast();
      if (closed.contains(x))
        continue;
      if (x == goal)
        return p.xy;
      closed.add(x);
      
      for (Point y : nbhd(x)) {
        if (!closed.contains(y)) {
          double gy = p.gx.getLast() + // cost: 
            1 + Math.abs(noise(x.x,x.y) -
                         noise(y.x,y.y));
          double hy = heuristic(y, goal);
          Path n = new Path(gy+hy, p, y, gy);
          q.add(n);
        }
      }
    }
    return null;
    
  } 
*/ 

  /*
  List<Point> wikiFindPath(Point start, Point goal) {
    Set<Point> closed = new HashSet<Point>();    //  nodes already evaluated
    PriorityQueue<Path> open = new PriorityQueue<Point>();  // node 2b evaled
    open.push(start);
    Map<Point,Point> came_from = new HashMap<Point,Point>();  // nav'd nodes
 
    Map<Point, double> g = new HashMap<Point, double>(); // best cost fr start
    //Map<Point, double> h = new HashMap<Point, double>();
    //h_score[start] := heuristic_cost_estimate(start, goal)
    //f_score[start] := g_score[start] + h_score[start]    // Estimated
    // total cost from start to goal through y.
 
    while (!open.isEmpty()) {
      x := the node in openset having the lowest f_score[] value
         if x = goal
             return reconstruct_path(came_from, came_from[goal])
 
         remove x from openset
         add x to closedset
         foreach y in neighbor_nodes(x)
             if y in closedset
                 continue
             tentative_g_score := g_score[x] + dist_between(x,y)
 
             if y not in openset
                 add y to openset
                 tentative_is_better := true
             else if tentative_g_score < g_score[y]
                 tentative_is_better := true
             else
                 tentative_is_better := false
 
             if tentative_is_better = true
                 came_from[y] := x
                 g_score[y] := tentative_g_score
                 h_score[y] := heuristic_cost_estimate(y, goal)
                 f_score[y] := g_score[y] + h_score[y]
 
     return failure
 
 function reconstruct_path(came_from, current_node)
     if came_from[current_node] is set
         p := reconstruct_path(came_from, came_from[current_node])
         return (p + current_node)
     else
         return current_node
*/
}

