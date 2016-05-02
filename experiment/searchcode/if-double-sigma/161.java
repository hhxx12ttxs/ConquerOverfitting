package pontbrownien;

public class BrownienGeo {

	public static double[] simu(double T, double n) {

		double delta = T / n;
		double sigma = 0.2;
		double m = 0.13;

		double u = Math.exp(sigma * Math.sqrt(delta));
		double d = Math.exp(-sigma * Math.sqrt(delta));

		double p = (1 + m / sigma * Math.sqrt(delta)) / 2;
		int sumY = 0;
		double[] S = new double[2];

		for (int i = 0; i < n; i++) {
			if (Math.random() >= p) {
				sumY += 1;
			}
		}

		S[0] = 49 * Math.pow(u, sumY) * Math.pow(d, n - sumY);

		S[1] = -T * sigma / Math.sqrt(delta) + 2 * sigma * Math.sqrt(delta) * sumY;

		return S;

	}

	public static double[] BrownienGeo(double T, double n) {

		/*
		 * int n=1000; double[] x = new double[2]; double[][] retour= new
		 * double[2][n]; double T=1;
		 * 
		 * for(int i=1;i<n;i++){ x=simu(T,i); retour[0][i]=x[0];
		 * retour[1][i]=x[1]; }
		 * 
		 * return retour;
		 */
		double delta = T / n;
		double sigma = 0.2;
		double m = 0.13;
		double[] S = new double[(int) n];
		double u = Math.exp(sigma * Math.sqrt(delta));
		double d = Math.exp(-sigma * Math.sqrt(delta));
		double p = (1 + m / sigma * Math.sqrt(delta)) / 2;
		double rand;

		S[0] = 49;
		for (int i = 1; i < n; i++) {

			rand = Math.random();

			if (p > rand) {
				S[i] = S[i - 1] * d;
			} else {
				S[i] = S[i - 1] * u;
			}
		}

		return S;
	}

	public static void EspVar() {

		double T = 1;
		double n = 1000;
		int Nmc = 10000;
		double[] X;
		double esperance = 0;
		double esperanceCarre = 0;
		double variance = 0;

		for (int i = 0; i < Nmc; i++) {
			X = BrownienGeo(T, n);
			esperance += X[X.length - 1] / Nmc;
			esperanceCarre += X[X.length - 1] * X[X.length - 1] / Nmc;
		}

		variance = esperanceCarre - esperance * esperance;

		System.out.println("Esperance = " + esperance);
		System.out.println("Variance = " + variance);
	}
}

