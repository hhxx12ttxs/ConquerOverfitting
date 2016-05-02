import java.util.*;

public class e1_1_30 {
	public static int gcd(int p, int q) {
		if(q == 0) return p;
		int r = p % q;
		return gcd(q, r);
	}
	
	public static void main(String[] args) {
		int N = 5;
		if(args.length > 0)
			N = Integer.parseInt(args[0]);
		boolean[][] a = new boolean[N][N];
		for(int i = 0; i < N; i++)
			for(int j = 0; j < N; j++)
				a[i][j] = (gcd(i, j) == 1);
		System.out.println(Arrays.deepToString(a));
	}
}

