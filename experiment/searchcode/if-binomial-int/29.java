public class e1_1_27 {
	public static int rec_count = 0;

	public static double binomial(int N, int k, double p) {
		rec_count++;
		if((N == 0) || (k < 0)) return 1.0;
		double d =(1.0-p) * binomial(N-1, k, p) + p * binomial(N-1, k-1, p);
		System.out.println(rec_count + ": ("+ N +", "+ k +") = " + d);
		return d;
	}

	public static void main(String[] args) {
		binomial(100, 50, 1);
		System.out.println(rec_count);
	}
}

