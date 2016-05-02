import java.util.*;

class NK {
	private int n;
	private int k;

	public NK(int n, int k) {
		this.n = n;
		this.k = k;
	}

	public boolean equals(Object o) {
		return o instanceof NK && ((NK)o).n == n && ((NK)o).k == k;
	}

	public int hashCode() {
		return 37*n+k;
	}
	
	public String toString() {
		return "("+n+", " + k+ ")";
	}
}

public class e1_1_27_2 {
	public static Map<NK, Double> map = new HashMap<NK, Double>();
	public static int rec_count = 0;

	public static double binomial(int N, int k, double p) {
		rec_count++;
		if((N == 0) || (k < 0)) {
			map.put(new NK(N, k), 1.0);
			return 1.0;
		}
		NK nk = new NK(N, k);
		Double d = map.get(nk);
		if(d != null)
			return d;
		d = (1.0-p) * binomial(N-1, k, p) + p * binomial(N-1, k-1, p);
		map.put(nk, d);
		return d;
	}

	public static void main(String[] args) {
		double d = binomial(100, 50, 1);
		System.out.println(d + ": " +rec_count);
		System.out.println(map);
	}
}

