package modalclust;

import modalclust.seq.Snippet;

public class DistanceMetrics {
  public static int numAllCalculations = 0;
  public static int numHeuristics = 0;


  public static float distanceMothur(final Snippet a, final Snippet b, float maxRadius) {
    // To take care of the right side, we must find the minimum of the right side for both sequences.
    numAllCalculations++;

    final int l = Math.min(a.l, b.l);
    final int maxDist = ((int) (l*maxRadius)) + 1;

    boolean ga = true, gb = true; // Are you in a gap? (this default to true to take care of the left side of snippet)

    final int xa = Math.max(a.xl, b.xl), xb = Math.min(a.xr, b.xr);
    final int overlap = xb - xa + 1;
    final int union = Math.max(a.xr, b.xr) - Math.min(a.xl, b.xl) + 1;
    int dist = 0;

    // Heuristic measure to throw out very large distances:
    int heuristic = 0;
    heuristic += Math.abs(a.na - b.na);
    heuristic += Math.abs(a.nc - b.nc);
    heuristic += Math.abs(a.ng - b.ng);
    heuristic += Math.abs(a.nt - b.nt);
    heuristic /= 2;
    heuristic -= (union - overlap);
    heuristic += Math.abs(a.ngr - b.ngr);
    final boolean heuristicResult = (heuristic >= 5*maxDist);

    if (heuristicResult) {
      numHeuristics++;
      return 1f;
    }

    for (int i = xa; i <= xb; i++) {
      final byte na = a.data[i];
      final byte nb = b.data[i];

      // Optimization that assumes that most bases are the same in a comparison:
      if (na == nb) {
        if (na == '-') {
          ga = true; gb = true;
          continue;
        } else {
          ga = false; gb = false;
          continue;
        }
      }

      if (na == '-') {
        // na is a gap, hence nb definitely isn't

        // If it's a new gap for a, then count it, otherwise ignore.
        if (!ga){
          dist++;
          if (dist >= maxDist) return 1f;
        }

        gb = false;
        ga = true;
      } else {
        if (nb == '-') {
          // nb is gap but na isn't

          // If it's a new gap for b, then count it, otherwise ignore.
          if (!gb){
            dist++;
            if (dist >= maxDist) return 1f;
          }

          gb = true;
        } else {
          // Both are non-gap
          dist++;
          if (dist >= maxDist) return 1f;
          gb = false;
        }
        ga = false;
      }
    }
    return (float) (((double) dist) / ((double) l));
  }
}

