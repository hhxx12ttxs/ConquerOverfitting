public class BuildingRoutes {
public int build(String[] dist, int T) {
int len = dist.length;
int[][] d = new int[len][len];
int[][] flow = new int[len][len];
int res = 0;

