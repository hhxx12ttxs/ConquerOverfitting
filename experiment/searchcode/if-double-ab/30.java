/**
 * CE1002-100502514 FlashTeens Chiang
 * The core of a single triangle
 * This class has been created because of the irrational requirement of
   class Triangle 2D with 2 triangles inside.
 * Methods:
	+InnerTriangle2D(MyPoint p1, MyPoint p2, MyPoint p3):
		Constructor that creates a triangle.
	+getArea():
		returns the area of the new triangle.
	+getPerimeter():
		returns the perimeter of the new triangle.
	+contains(double x, double y):
	+contains(MyPoint p):
		returns true if the specified point p(x,y) is inside the original triangle.
	+contains(Triangle2D t):
		returns true if the specified triangle is inside the original triangle.
 */
package a4.s100502514;

public class InnerTriangle2D {
	/** Private Array of Points to save the triangle points. */
	private MyPoint[] points = new MyPoint[3];
	
	/** Constructor with 3 points */
	public InnerTriangle2D(MyPoint p1, MyPoint p2, MyPoint p3){
		points[0]=p1;
		points[1]=p2;
		points[2]=p3;
	}
	
	/** Get the point by index */
	public MyPoint getMyPoint(int index){
		return points[index];
	}
	
	/** Get the triangle area by cross product
		(My own solution: See header comments in A42.java) */
	public double getArea(){
		MyPoint AB = points[1].minus(points[0]);
		MyPoint AC = points[2].minus(points[0]);
		//Call class MyPoint's method crossProductZ(MyPoint) and divide by 2 for result.
		return Math.abs(AB.crossProductZ(AC)/2);
	}
	
	/** Get the perimeter of the triangle */
	public double getPerimeter(){
		double result=0;
		for(int i=0;i<3;i++){
			result+=points[i].getDistance(points[(i+1)%3]);
		}
		return result;
	}
	
	/** Returns true if the specified point p(x,y) is inside the original triangle.
	 	Otherwise returns false.
	 	There are 3 syntaxes for method contains():
	 	Two are for point; one is for triangle, as parameter(s).
	 	(My own solution: See header comments in A42.java)
	 */
	/** The following two for point: */
	public boolean contains(double x, double y){
		return contains(new MyPoint(x, y));
	}
	public boolean contains(MyPoint p){
		/**
		 * Set AP(x3,y3) = n*AB(x1,y1)+m*AC(x2,y2)
		 * Also: x1*n+x2*m=x3, y1*n+y2*m=y3, find the answer using Kramer's Formula.
		 */
		MyPoint AB = points[1].minus(points[0]);
		MyPoint AC = points[2].minus(points[0]);
		MyPoint AP = p.minus(points[0]);
		/** Using Kramer's Formula with class LinearEquation, borrowed from Assignment 3-2 */
		LinearEquation solution = new LinearEquation(
				new double[]{AB.getX(),AC.getX(),AP.getX(),AB.getY(),AC.getY(),AP.getY()}
				);
		if(!solution.isSolvable()) return false;
		else{
			double n = solution.getX(), m = solution.getY();
			if(n>=0 && m>=0 && n+m<=1) return true;
			else return false;
		}
	}
	/** The following one for triangle: */
	public boolean contains(InnerTriangle2D t){
		/** Returns true if the specified triangle is inside the original triangle.
		 	If any part of t is out of the range of this, just returns false.
		 */
		for(int i=0;i<3;i++){
			if(!contains(t.getMyPoint(i)))return false;
		}
		return true;
	}
}

