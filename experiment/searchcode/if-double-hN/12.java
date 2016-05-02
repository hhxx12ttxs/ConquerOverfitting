/*
	Exercise 45
*/

import java.util.ArrayList;

public class Collector {
	
	public static int repeted(int N) {
		ArrayList<Integer> ints = new ArrayList<Integer>();
		int cnt = 0;
		boolean ok = false;
		while(!ok) {
			int rand = StdRandom.uniform(0, N);
			cnt++;
			ints.add(rand);
			ok = true;
			for(int i = 0; i < N; i++)
				if(!ints.contains(i))
					ok = false;
		}
		return cnt;
	}

	public static void main(String[] args) {
		int N = Integer.parseInt(args[0]);
		int n = Integer.parseInt(args[1]);
		int rez = 0;
		for(int i = 0; i < n; i++)
			rez += repeted(N);
		double hN = 0;
		for(int i = 1; i <= N; i++)
			hN += 1.0/ i;
		StdOut.printf("Result: %10.5f ~ %10.5f\n", (double)rez /  n, N * hN);
	}
}

