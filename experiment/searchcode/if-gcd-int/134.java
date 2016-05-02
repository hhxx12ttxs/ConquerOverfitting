package a1;

public class RelativePrime {

	public static boolean[][] generateMatrix(int n) {//generates an n by n matrix
		boolean [][] temp = new boolean[n][n];
		for (int row = 0; row < n; row++) {
			for (int column = 0; column < n; column++) {
				if (gcd(row,column) == 1) temp[row][column] = true;
				else temp[row][column] = false;
			}
		}
		return temp;
	}
	public static int gcd(int p, int q) {
		if (q==0) return p;
		int r = p % q;
		return gcd(q,r);
	}

}

