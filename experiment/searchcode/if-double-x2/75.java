import java.util.Iterator;
import java.util.LinkedList;


public class Dijkstra extends Pathfinder{
	
	@Override
	public int[] computeMove(int[][] map, int sx, int sy, int tx, int ty) {
		int xmax = map.length;
		int ymax = map[0].length;
		double[][] value = new double[xmax][ymax];
		for(int x=0; x<xmax; x++) for(int y=0; y<ymax; y++)
			value[x][y] = -1;
		value[tx][ty] = 0;
		LinkedList<Integer> xq = new LinkedList<Integer>();
		LinkedList<Integer> yq = new LinkedList<Integer>();
		xq.add(tx);
		yq.add(ty);
		while(!xq.isEmpty()) {
			int x = xq.removeFirst();
			int y = yq.removeFirst();
			for(int i=0; i<d.length; i++) {
				int x2 = x+d[i][0];
				int y2 = y+d[i][1];
				if(x2<0 || y2<0 || x2>=xmax || y2>=ymax || map[x2][y2]==0) continue;
				if(value[x2][y2] == -1) {
					value[x2][y2] = value[x][y]+
						Math.sqrt(d[i][0]*d[i][0]+d[i][1]*d[i][1]);
					int j=0;
					Iterator<Integer> jx = xq.iterator();
					Iterator<Integer> jy = yq.iterator();
					while(j<xq.size()) {
						if(value[jx.next()][jy.next()]<value[x2][y2]) j++;
						else break;
					}
					xq.add(j, x2);
					yq.add(j, y2);
				}
			}
			if(x==sx && y==sy) break;
		}
		int best = -1;
		double bestValue = value[sx][sy];
		for(int i=0; i<d.length; i++) {
			int x = sx+d[i][0];
			int y = sy+d[i][1];
			if(x<0 || y<0 || x>=xmax || y>=ymax || map[x][y]==0) continue;
			if(bestValue==-1 || value[x][y]+
					Math.sqrt(d[i][0]*d[i][0]+d[i][1]*d[i][1])<=bestValue) {
				best = i;
				bestValue = value[x][y]+
					Math.sqrt(d[i][0]*d[i][0]+d[i][1]*d[i][1]);
			}
		}
		if(best==-1) return new int[] {0, 0};
		else return d[best];
	}

	public double[][] computeDistances(int[][] map, int tx, int ty) {
		int xmax = map.length;
		int ymax = map[0].length;
		double[][] value = new double[xmax][ymax];
		for(int x=0; x<xmax; x++) for(int y=0; y<ymax; y++)
			value[x][y] = -1;
		value[tx][ty] = 0;
		LinkedList<Integer> xq = new LinkedList<Integer>();
		LinkedList<Integer> yq = new LinkedList<Integer>();
		xq.add(tx);
		yq.add(ty);
		while(!xq.isEmpty()) {
			int x = xq.removeFirst();
			int y = yq.removeFirst();
			for(int i=0; i<d.length; i++) {
				int x2 = x+d[i][0];
				int y2 = y+d[i][1];
				if(x2<0 || y2<0 || x2>=xmax || y2>=ymax || map[x2][y2]==0) continue;
				if(value[x2][y2] == -1) {
					value[x2][y2] = value[x][y]+
						Math.sqrt(d[i][0]*d[i][0]+d[i][1]*d[i][1]);
					int j=0;
					Iterator<Integer> jx = xq.iterator();
					Iterator<Integer> jy = yq.iterator();
					while(j<xq.size()) {
						if(value[jx.next()][jy.next()]<value[x2][y2]) j++;
						else break;
					}
					xq.add(j, x2);
					yq.add(j, y2);
				}
			}
		}
		return value;
	}
}

