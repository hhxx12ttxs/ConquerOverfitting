package a4.s100502511;

public class Triangle2D {
	private MyPoint p1 = new MyPoint(0.0, 0.0); // p1,p2,p3????????
	private MyPoint p2 = new MyPoint(17.0, 6.0);
	private MyPoint p3 = new MyPoint(10.0, 15.0);
	private MyPoint p1_new, p2_new, p3_new; // ???????

	public MyPoint getP1_new() { // p1_new???????p1
		return p1_new;
	}

	public MyPoint getP2_new() { // p2_new???????p2
		return p2_new;
	}

	public MyPoint getP3_new() { // p3_new???????p3
		return p3_new;
	}

	public Triangle2D(double x1, double y1, double x2, double y2, double x3,
			double y3) {
		p1_new = new MyPoint(x1, y1);
		p2_new = new MyPoint(x2, y2);
		p3_new = new MyPoint(x3, y3);
	}

	public double getArea() { // ???? ???"????"
		double A = p1_new.distance(p2_new);
		double B = p1_new.distance(p3_new);
		double C = p2_new.distance(p3_new);
		double s = getPerimeter() / 2;
		double d = s * (s - A) * (s - B) * (s - C);
		double area = Math.pow(d, 0.5);
		return area;
	}

	public double getPerimeter() { // ????
		double Perimeter = p1_new.distance(p2_new) + p1_new.distance(p3_new)
				+ p2_new.distance(p3_new);
		return Perimeter;
	}

	public boolean contains(Triangle2D input) { // ?????????????????????
		if (contains(input.getP1_new()) && input.contains(input.getP2_new()) // ?????????true
				&& contains(input.getP3_new())) {
			return true;
		} else {
			return false;
		}
	}

	public boolean contains(MyPoint p) { // ????????????????
		double side1, side2, side3, side4, side5, side6, side7, side8, side9, side10, side11, side12, s, s2, s3, s4, areaPAB, areaPBC, areaPCA, areaABC = 0;
		// ???????"????"

		side1 = (p.distance(p1));
		side2 = (p.distance(p2));
		side3 = (p1.distance(p2));

		s = (side1 + side2 + side3) / 2;
		areaPAB = Math // ??PAB???
				.pow((s * (s - side1) * (s - side2) * (s - side3)), 0.5);

		side4 = (p.distance(p2));
		side5 = (p.distance(p3));
		side6 = (p2.distance(p3));

		s2 = (side4 + side5 + side6) / 2;
		areaPBC = Math.pow((s2 * (s2 - side4) * (s2 - side5) * (s2 - side6)),// ??PBC???
				0.5);

		side7 = (p.distance(p1));
		side8 = (p.distance(p3));
		side9 = (p1.distance(p3));

		s3 = (side7 + side8 + side9) / 2;
		areaPCA = Math.pow((s3 * (s3 - side7) * (s3 - side8) * (s3 - side9)),// ??PCA???
				0.5);

		side10 = (p1.distance(p2));
		side11 = (p2.distance(p3));
		side12 = (p1.distance(p3));

		s4 = (side10 + side11 + side12) / 2;
		areaABC = Math.pow(// ??ABC???
				(s4 * (s4 - side10) * (s4 - side11) * (s4 - side12)), 0.5);

		double judge = Math.abs((areaPAB + areaPCA + areaPBC) - areaABC);
		if (judge < 0.5 && judge > -0.5) { // ?????????true
			return true;
		} else {
			return false;
		}
	}
}
