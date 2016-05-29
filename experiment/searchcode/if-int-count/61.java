public class CountAllPossibleWalksFromSsourceToDestinationWithkKEdges {
public static int countWalks(int[][] G, int u, int v, int k) {
if (k == 0 &amp;&amp; u == v)
return 1;
if (k == 1 &amp;&amp; G[u][v] == 1)
return 1;
if (k <= 0)
return 0;
int count = 0;
for (int i = 0; i < G.length; i++) {

