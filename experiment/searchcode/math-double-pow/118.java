package com.emathias.projecteuler;

/**
 * @author u0159471
 * NOT FINISHED
 */
public class Problem410 implements EulerProblem {
	
	@Override
	public String run() {
		
		int R = 10;
		int X = 100;
		int tangents = 0;
		
		for (int r = 1; r <= R; r++) { // radius of circle
			tangents += X * 2; // horizontal tangents
			for (int a = 1; a <= X; a++) { // x distance from circle
			
				for (int b = r - 1/* , c = 0 */; b > -r * 100; b--) { // y dist
				
					double tx = getTangentX(r, a, b);
					double ty = Math.sqrt(Math.pow(r, 2) - Math.pow(tx, 2));
					
					double m = EulerUtils.slope(tx, ty, a, b);
					double i = ty - (tx * m);
					
					double c = m * -a + i;
					
					if (Math.abs(c - Math.rint(c)) < (1 / Math.pow(10,
							Math.log10(R * X)))) {
						System.out.println(r + "," + a + "," + c + "," + b);
						tangents += 4;
					}
					
					// while (isTangent(r, a, b, -a, c) < 0) {
					// c++;
					// }
					//
					// // check end-points traveling up
					// // for (; !passed1; c1++) {
					// while (isTangent(r, a, b, -a, c) > 0) {
					// c--;
					// }
					// if (isTangent(r, a, b, -a, c) == 0) {
					// tangents += 4; // found 4 tangents using symmetry
					// System.out.println(r + "," + a + "," + c + "," + b);
					// }
					
				}
			}
		}
		
		return Integer.toString(tangents);
	}
	
	private double getTangentX(double r, double px, double py) {
		return ((Math.sqrt(Math.pow(px, 2) * Math.pow(py, 2) * Math.pow(r, 2)
				+ Math.pow(py, 4) * Math.pow(r, 2) - Math.pow(py, 2)
				* Math.pow(r, 4))) + px * Math.pow(r, 2))
				/ (Math.pow(px, 2) + Math.pow(py, 2));
	}
	
	/**
	 * @param r
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 * 	[line distance from radius] - radius
	 * 	(0 if tangent, <0 if secant, >0 if none)
	 */
	private double isTangent(double r, double x1, double y1, double x2,
			double y2) {
		double slope1 = EulerUtils.slope(x1, y1, x2, y2);
		double slope2 = -1 / slope1;
		double b1 = y1 - (x1 * slope1);
		double px = b1 / (slope2 - slope1);
		double py = slope2 * px;
		return EulerUtils.distance(px, py, 0, 0) - r;
		/*
		 * if (EulerUtils.distance(px, py, 0, 0) < r) { continue; } else if
		 * (EulerUtils.distance(px, py, 0, 0) == r) { tangents += 4; // found 4
		 * tangents using symmetry } else { passed = true; }
		 */
	}
	
	@Override
	public String name() {
		return "Problem 410";
	}
}

