

/* Generalized implementation of A* pathfinding. Returns a list of
   coordinates demonstrating the cheapest path from start to goal. */
class Pathfinding {

  /*
  List<int> aStar(Point start,  // starting coordinate
                  Point goal/*,   // finish coordinates
                  nbhd,   // nbhd(p0) is the set of p1 in its neighborhood
                  cost,   // cost(p0,p1) is actual cost of moving from p0 to p1
                  heuristic * /)  // heuristic(p,goal) ests cost from p to goal  
  {
    Set<int> closed = new Set<int>();
    class Path {
      double fp;
      List<int> xy;
      List<double> gx;
    };
    PriorityQueue<Path> q = new PriorityQueue<Path>();
    q.add((0+heuristic(start,goal), [(start,0)]));
    while (!q.isEmpty()) {
      Pt p = q.remove();
      if (closed.has(p.xy[-1]))
        continue;
      if (p.xy[-1] == goal)
        return p.xy;
      closed.add(x);
      
      for y in nbhd(x):
        if y not in closed:
          gy = gx + cost(x, y)
          hy = heuristic(y, goal)
          heapq.heappush(q, (gy+hy, p + [(y,gy)]))
    return None  // failure
            }  
  */


}
