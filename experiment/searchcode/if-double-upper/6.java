package safrain.nodelet.util;

import java.util.Random;

public class MathUtil {

	private static final Random random = new Random();

	public static boolean inBound(double value, double upper, double lower) {
		return (value <= upper) && (value >= lower);
	}

	public static int bound(int value, int upper, int lower) {
		if (value > upper) {
			value = upper;
		}
		if (value < lower) {
			value = lower;
		}
		return value;
	}

	public static double bound(double value, double upper, double lower) {
		if (value > upper) {
			value = upper;
		}
		if (value < lower) {
			value = lower;
		}
		return value;
	}

	public static double boundUpper(double value, double upper) {
		if (value > upper) {
			value = upper;
		}
		return value;
	}

	public static double boundLower(double value, double lower) {
		if (value < lower) {
			value = lower;
		}
		return value;
	}

	private static double PI180 = Math.PI / 180;

	public static double sin(double angle) {
		return Math.sin(angle * PI180);
	}

	public static double cos(double angle) {
		return Math.cos(angle * PI180);
	}

	public static double tan(double angle) {
		return Math.tan(angle * PI180);
	}

	public static double arcTan(double x, double y) {
		return (Math.atan2(y, x) * 180 / Math.PI);
	}

	public static double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}

	public static double angle(double x1, double y1, double x2, double y2) {
		return arcTan(x2 - x1, y1 - y2);

	}

	public static double min(double f, double g) {
		if (f < g) {
			return f;
		}
		return g;
	}

	public static int nextInt(int delta) {
		return Math.abs(random.nextInt() % delta);
	}

	public static double nextDouble() {
		return random.nextDouble();
	}
}

