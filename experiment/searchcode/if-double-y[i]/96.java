package cloud;

//<pre>
import java.util.*;

public class vpuff extends VolumeApplet {
	Random R = new Random(0);
	int blur = 20, mode = 0;
	double P[][];
	double freq = 6.;

	boolean isAddingNoise = false;

	public void setAddingNoise(boolean tf) {
		isAddingNoise = tf;
		freshStart();
	}

	// THINGS TO INITIALIZE WHEN STARTING THE APPLET

	public void initialize() {

		setSpecular(.5, .7, .7, .01);
		setAmbient(.5, 0, 0);

		// CREATE CLOUD OF PARTICLES

		P = new double[200][3];
		for (int i = 0; i < P.length; i++) {
			double rr = 10;
			do {
				double z = (1 - 2. * i / P.length);
				P[i][2] = z;
				double r = Math.sqrt(1 - z * z);
				P[i][0] = r * (2 * (R.nextDouble() % 1) - 1);
				P[i][1] = r * (2 * (R.nextDouble() % 1) - 1) / 1.414;
				rr = P[i][0] * P[i][0] + 2 * P[i][1] * P[i][1] + P[i][2]
						* P[i][2];
			} while (rr > 1.01);
		}

		C = 1;
		S = 0;
	}

	double r = .3, rxr = r * r;
	int i0 = 0;

	public void restart() {
		i0 = 0;
	}

	public double density(double x, double y, double z) {

		x /= .7;
		y /= .7;
		z /= .7;

		for (; i0 < P.length; i0++)
			if (P[i0][2] < z + r)
				break;

		double d = 0;
		for (int i = i0; i < P.length && P[i][2] > z - r; i++) {
			double pu = P[i][0], py = P[i][1], pv = P[i][2];
			double px = C * pu - S * pv;
			double pz = S * pu + C * pv;
			px -= x;
			py -= y;
			pz -= z;
			double rr = (px * px + py * py + pz * pz) / rxr;
			if (rr < 1)
				d += .5 + .5 * Math.cos(Math.PI * Math.sqrt(rr));
		}
		if (d > 0) {
			if (isAddingNoise)
				d += 4 * addNoise(x, y, z);
			d = Math.max(.01, Math.min(.99, d));
			d = .1 * filter(d);
		}
		return d;
	}

	double rot = Math.PI / 3, c = Math.cos(rot), s = Math.sin(rot);

	double addNoise(double x, double y, double z) {
		double u, v;
		x += 100;
		double d = 0;
		d += abn(1, x, y, z);
		u = x;
		v = y;
		x = c * u - s * v;
		y = s * u + c * v;
		d += abn(2, x, y, z);
		u = x;
		v = y;
		x = c * u - s * v;
		y = s * u + c * v;
		d += abn(4, x, y, z);
		return 1.2 * d / freq;
	}

	double abn(double f, double x, double y, double z) {
		double freq = this.freq * f;
		return (Math.abs(ImprovedNoise.noise(freq * x, freq * y, freq * z)) - .5) / f;
	}

	double filter(double t) {
		t = bias(t, .67);
		if (t < .5)
			t = gain(t, .86);
		t = bias(t, .35);
		return t;
	}
}

