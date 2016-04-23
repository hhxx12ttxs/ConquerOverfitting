
public class MathFunctions {

	public static double area(double radius) {
		
		return Math.PI * radius * radius;
	}
	
	public static double absoluteValue(double x) {
		
		if (x < 0)
			return -x;
		else
			return x;
	}
	
	public static double distance
		(double x1, double y1, double x2, double y2) {
		
		double dx = x2 - x1;
		double dy = y2 - y1;
		double dsquared = (dx*dx) + (dy*dy);
		double result = Math.sqrt(dsquared);
		return result;
	}
	
	
	public static double area
		(double x1, double y1, double x2, double y2) {
		
		return area(distance(x1, y1, x2, y2));
	}
	
	public static int factorial(int n) {
		
		if (n == 0)
			return 1;
		else
			return n * factorial(n-1);
	}
	
	public static int fibonacci(int n) {
		
		if (n == 0 || n == 1)
			return 1;
		else
			return fibonacci(n-1) + fibonacci(n-2);
	}
	
	public static void main(String[] args) {
		
		double dist = distance(1.0, 2.0, 4.0, 6.0);
		System.out.println(dist);
	}
}

