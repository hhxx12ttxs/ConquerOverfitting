package a4.s100502034;

import a4.s100502034.MyPoint;
public class Triangle2D {
	private MyPoint p1 = new MyPoint(0.0, 0.0); // points of original triangle triangle
	private MyPoint p2 = new MyPoint(17.0, 6.0);
	private MyPoint p3 = new MyPoint(10.0, 15.0);
	private MyPoint p1_new, p2_new, p3_new; // points of new triangle

	public MyPoint getP1_new(){ // get the p1 of new triangle
		return p1_new;//p1_new???constructor???
	}

	public MyPoint getP2_new(){ // get the p2 of new triangle
		return p2_new;//p2_new???constructor???
	}

	public MyPoint getP3_new(){ // get the p3 of new triangle
		return p3_new;//p3_new???constructor???
	}

		// constructor with arguments to initial the points of the new triangle
	public Triangle2D(double x1, double y1, double x2, double y2, double x3, double y3){
		p1_new = new MyPoint(x1, y1);//??MyPoint?????
		p2_new = new MyPoint(x2, y2);
		p3_new = new MyPoint(x3, y3);
	}

	public double getArea(){ // calculate the area of the new triangle
		double s = getPerimeter() / 2 ;
		return Math.pow(s*(s-p1_new.distance(p2_new))* (s - p2_new.distance(p3_new)) * (s- p3_new.distance(p1_new)), 0.5);//???? ??distance????????????
	}

	public double getPerimeter(){ // calculate the perimeter of the new triangle
		return p1_new.distance(p2_new) + p2_new.distance(p3_new) + p3_new.distance(p1_new);//??distance????????????
	}

		// check the input point is in the original triangle of the object or not
	public boolean contains(MyPoint p){
		// if input point p is in the triangle, area of PAB + area of PBC + area of PCA = area of ABC
	double side1, side2, side3, s, s1, s2 ,s3 , areaPAB, areaPBC, areaPCA, areaABC;
	side1 = p.distance(p1);
	side2 = p.distance(p2);
	side3 = p.distance(p3);
	s1 = (side1+side2+p1.distance(p2))/2;
	s2 = (side2+side3+p2.distance(p3))/2;
	s3 = (side3+side1+p3.distance(p1))/2;
	s = (p1.distance(p2) + p2.distance(p3) +p3.distance(p1))/2;
	areaABC = Math.pow(s*(s-p1.distance(p2))* (s - p2.distance(p3)) * (s- p3.distance(p1)) , 0.5);// calculate area of ABC
	areaPAB = Math.pow(s1*(s1-side1)* (s1 - side2) * (s1- p1.distance(p2)), 0.5);// calculate area of PAB
	areaPBC = Math.pow(s2*(s2-side2)* (s2 - side3) * (s2- p2.distance(p3)), 0.5);// calculate area of PBC
	areaPCA = Math.pow(s3*(s3-side3)* (s3 - side1) * (s3- p3.distance(p1)), 0.5);// calculate area of PCA
	if (Math.abs(areaABC - (areaPAB + areaPBC + areaPCA)) <= 1){
		return true;
	}
		else{
			return false;
			
	}

		// if areaPAB + areaPBC + areaPCA = areaABC, then p is in the ABC
		                // However, there is some error(??) in this calculation
		                // So if the absolute value of (areaABC - (areaPAB + areaPBC + areaPCA)) is less than 0.5
		                // we consider the point is inside the original triangle
		                // you can use Math.abs(double) to calculate the absolute value

	}

		// check the input triangle(Triangle2D object) is in the original triangle of this object or not
	public boolean contains(Triangle2D input){
		// if the input triangle is in the original triangle of this object
		               // then all points of the input triangle is in the original triangle of this object
		boolean firstP = input.contains(p1);//??????????????
		boolean secondP = input.contains(p2);
		boolean thirdP = input.contains(p3);
		if (firstP == true && secondP == true && thirdP == true){
			return true;
		}
		else{
			return false;
		}
	}

}

