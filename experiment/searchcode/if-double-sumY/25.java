
public class ApproximateLinear {

	private double[] xs;
	private double[] ys;
	private int n;
	private double sumx = 0;
	private double sumy = 0;
	private double sumxSquared = 0;
	private double sumxy = 0;
	private double determinant;
	private double a;
	private double b;
// F - x; l - y; a - d - b; b - c - a;
	public ApproximateLinear(double[] xs, double[] ys) {
		this.xs = xs;
		this.ys = ys;
		this.n = xs.length;
		calcSumx();
		calcSumy();
		calcSumxSquared();
		calcSumxy();
		calcDeterminant();
		calcA();
		calcB();
		if (xs.length != ys.length) System.out.println("Invalid input in ApproximateLinear object.");
	}
	
	private void calcSumx() {
		for (int i = 0; i < n; i++)	sumx += xs[i];
	}
	
	private void calcSumy() {
		for (int i = 0; i < n; i++)	sumy += ys[i];
	}
	
	private void calcSumxSquared() {
		for (int i = 0; i < n; i++) sumxSquared += xs[i] * xs[i];
	}
	
	private void calcSumxy() {
		for (int i = 0; i < n; i++) sumxy += xs[i] * ys[i];
	}
	
	private void calcDeterminant() {
		determinant = n * sumxSquared - sumx * sumx;
	}
	
	private void calcA() {
		a = (n * sumxy - sumx * sumy) / determinant;
	}
	
	private void calcB() {
		b = (sumxSquared * sumy - sumx * sumxy) / determinant;
	}
	
	public double getDeterminant() {
		return determinant;
	}
	
	public double getA() {
		return a;
	}
	
	public double getB() {
		return b;
	}
	
}

