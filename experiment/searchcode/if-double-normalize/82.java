package cloud;

//<pre>
import java.awt.*;

public class VolumeApplet extends VPixApplet {
	
	public double light[] = { 1, 1, 1 }, hilite[] = { .5, .5, 1 };
	
	double T[][], M[][], D[][];
	double X, Y, Z, R, C = 1, S = 0;
	double xx, yy, zz;
	double normal[] = new double[3];
	double rgb[] = new double[3];
	double zRate = 1;
	int skip = 1;
	int animationFrame = 0;

	public void setAmbient(double r, double g, double b) {
		ambient[0] = r;
		ambient[1] = g;
		ambient[2] = b;
	}

	public void setSpecular(double r, double g, double b, double p) {
		specular[0] = r;
		specular[1] = g;
		specular[2] = b;
		power = p;
	}

	public void initialize() {
	}

	public void restart() {
	}

	public void animate(int f) {
	}

	public double density(double x, double y, double z) {
		return 0;
	}

	void lighting(double normal[], double rgb[]) {
		double t = Math.max(0, .5 + .3 * (normal[0] + normal[1] + normal[2]));
		double s = Math.pow(t, power);
		rgb[0] = t * diffuse[0] + s * specular[0];
		rgb[1] = t * diffuse[1] + s * specular[1];
		rgb[2] = t * diffuse[2] + s * specular[2];
	}

	public void init() {
		super.init();
		D = new double[W][H];
		M = new double[W][H];
		T = new double[W][H];
		R = W / 2;

		initialize();
		setToBackground();
	}

	void setToBackground() {
		int rgb = pack(f2i(bg[0]), f2i(bg[1]), f2i(bg[2]));
		int i = 0;
		for (int y = 0; y < H; y++)
			for (int x = 0; x < W; x++)
				pix[i++] = rgb;
	}

	void freshStart() {
		frame0 = frame;
		initialize();
		setToBackground();
		restart();
		clearRender = true;
	}

	int frame, frame0 = 0;

	public void setPix(int frame) {
		this.frame = frame;
		if (skip == 3 && frame - frame0 == W / skip) {
			skip = 1;
			specular[0] *= .5;
			specular[1] *= .5;
			specular[2] *= .5;
			frame0 = frame;
			restart();
			clearRender = true;
		}
		if (frame - frame0 == W / skip) {
			animate(animationFrame++);
			freshStart();
		}

		if (clearRender) {
			for (int x = 0; x < W; x++)
				for (int y = 0; y < H; y++) {
					D[x][y] = 0;
					M[x][y] = 1;
					T[x][y] = 1;
				}
			clearRender = false;
		}

		damage = true;
		for (int x = skip; x < W; x += skip)
			for (int y = skip; y < H; y += skip) {

				M[x - skip][y - skip] = M[x][y];

				if (isSlice) {
					D[x][y] = 0;
					M[x][y] = 1; // SHADOW MASKING
					T[x][y] = 1; // REMAINING FRONT-TO-BACK TRANSPARENCY
					Z = sliceZ;
				} else {
					if (T[x][y] < .01)
						continue;
					Z = (R - skip * zRate * (frame - frame0)) / R;
				}

				X = (x - R) / R;
				Y = (y - R) / R;

				double p = 1 + .2 * Z;
				X /= p;
				Y /= p;

				xx = X;
				// yy = Y*C+Z*S;
				// zz = -Y*S+Z*C;
				yy = Y;
				zz = Z;

				double d = Math.max(0, Math.min(.99, skip * zRate
						* density(xx, yy, zz)));

				if (d == .99)
					D[x][y] = 1;
				else if (d > .01 && d < .99) {
					if (isSlice) {
						int c = (int) (255 * d);
						pix[xy2i(x, H - y)] = pack(c, c, c);
						continue;
					}

					double dx = D[x - skip][y];
					double dy = D[x][y - skip];
					double dz = D[x][y];
					if (isSlice)
						dz = 1;

					if (dx > 0 && dx < 1 && dy > 0 && dy < 1 && dz > 0
							&& dz < 1) {

						double transparency = Math.pow(2.71828, -d);
						double contrib = T[x][y] * (1 - transparency);

						double red = ambient[0];
						double grn = ambient[1];
						double blu = ambient[2];

						if (M[x][y] >= .01) {
							normal[0] = dx - d;
							normal[1] = dy - d;
							normal[2] = d - dz;
							if (normal[0] != 0 && normal[1] != 0
									&& normal[2] != 0)
								normalize(normal);
							lighting(normal, rgb);
							red += M[x][y] * rgb[0];
							grn += M[x][y] * rgb[1];
							blu += M[x][y] * rgb[2];
						}

						int i = xy2i(x, H - y);
						int rgb = pack(f2i(lerp(contrib,
								i2f(unpack(pix[i], 0)), red)), f2i(lerp(
								contrib, i2f(unpack(pix[i], 1)), grn)),
								f2i(lerp(contrib, i2f(unpack(pix[i], 2)), blu)));
						pix[i] = rgb;
						if (skip > 1)
							for (int I = 0; I < skip; I++)
								for (int J = 0; J < skip; J++)
									pix[i + W * I + J] = rgb;

						T[x][y] *= transparency;
						M[x - skip][y - skip] *= transparency;
					}
					D[x][y] = d;
				}
			}
		blur(M);
	}

	double tmpX[], tmpY[];

	void blur(double M[][]) {

		if (tmpY == null)
			tmpY = new double[H];
		for (int x = 0; x < W; x++) {
			for (int y = 1; y < H - 1; y++)
				tmpY[y] = M[x][y - 1] / 6 + 2 * M[x][y] / 3 + M[x][y + 1] / 6;
			for (int y = 1; y < H - 1; y++)
				M[x][y] = tmpY[y];
		}

		if (tmpX == null)
			tmpX = new double[W];
		for (int y = 0; y < H; y++) {
			for (int x = 1; x < W - 1; x++)
				tmpX[x] = M[x - 1][y] / 6 + 2 * M[x][y] / 3 + M[x + 1][y] / 6;
			for (int x = 1; x < W - 1; x++)
				M[x][y] = tmpX[x];
		}
	}

	void normalize(double v[]) {
		double t = Math.sqrt(dot(v, v));
		v[0] /= t;
		v[1] /= t;
		v[2] /= t;
	}

	// ////////////// MATH PRIMITIVES //////////////////////////

	double dot(double a[], double b[]) {
		return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
	}

	double scurve(double t) {
		return t < 0 ? 0 : t > 1 ? 1 : t * t * (3 - t - t);
	}

	double lerp(double t, double a, double b) {
		return a + t * (b - a);
	}

	double i2f(int i) {
		return i / 255.;
	}

	int f2i(double f) {
		return Math.max(0, Math.min(255, (int) (255 * f)));
	}

	double boundary(double t) {
		return 10 * scurve(t);
	}

	double clip(double lo, double hi, double t) {
		return Math.max(lo, Math.min(hi, t));
	}

	static final double LOG_HALF = Math.log(0.5);

	static double gain(double a, double b) {
		double p;

		if (a < .001)
			return 0.;
		else if (a > .999)
			return 1;

		b = (b < .001) ? .0001 : (b > .999) ? .999 : b;
		p = Math.log(1. - b) / LOG_HALF;
		if (a < 0.5)
			return Math.pow(2 * a, p) / 2;
		else
			return 1. - Math.pow(2 * (1. - a), p) / 2;
	}

	static double bias(double a, double b) {
		if (a < .001)
			return 0.;
		else if (a > .999)
			return 1.;
		else if (b < .001)
			return 0.;
		else if (b > .999)
			return 1.;
		else
			return Math.pow(a, Math.log(b) / LOG_HALF);
	}

	// ////////////// COLORS //////////////////////////

	private double ambient[] = { .2, .2, .2 };
	private double diffuse[] = { .8, .8, .8 };
	private double specular[] = { 0, 0, 0 }, power = 2;
	private double bg[] = { .5, .5, .8 };
	public double fg[] = { 1, 1, 1 };

	// /////////////// USER INTERACTION FUNCTIONS //////////////////////

	boolean clearRender = true;
	boolean isSlice = false;
	double sliceZ = 0.0;

	public boolean keyUp(Event e, int key) {
		switch (key) {
		case 's':
			isSlice = !isSlice;
			clearRender = true;
			break;
		default:
			break;
		}
		return true;
	}

	double theta = 0;
	int mx = 0;

	public boolean mouseDown(Event e, int x, int y) {
		mx = x;
		return true;
	}

	public boolean mouseDrag(Event e, int x, int y) {
		if (isSlice) {
			sliceZ = 2. * y / H - 1;
			clearRender = true;
		}
		theta += (double) (x - mx) / W;
		mx = x;
		return true;
	}

	public boolean mouseUp(Event e, int x, int y) {
		if (isSlice) {
			sliceZ = 2. * y / H - 1;
			clearRender = true;
		} else {
			System.out.println(theta);
			C = Math.cos(theta);
			S = Math.sin(theta);
			freshStart();
		}
		return true;
	}
}

