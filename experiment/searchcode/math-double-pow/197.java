package a4.s100502024;
import java.lang.Math;
public class Triangle2D 
{
	private MyPoint p1 = new MyPoint(0,0); // ??????
	private MyPoint p2 = new MyPoint(17,6);
	private MyPoint p3 = new MyPoint(10,15);
	private MyPoint p1_new,p2_new,p3_new; // ?????Object?????????
	public MyPoint getP1_new()
	{
		return p1_new;
	}
	public MyPoint getP2_new()
	{
		return p2_new;
	}
	public MyPoint getP3_new()
	{
		return p3_new;
	}
	public Triangle2D(double x1,double y1,double x2,double y2,double x3,double y3) // constructor
	{
		p1_new = new MyPoint(x1,y1);
		p2_new = new MyPoint(x2,y2);
		p3_new = new MyPoint(x3,y3);
	}
	public double getArea() // ?????????
	{
		double SIDE1,SIDE2,SIDE3,S,area;
		SIDE1 = p1_new.distance(p2_new);
		SIDE2 = p2_new.distance(p3_new);
		SIDE3 = p3_new.distance(p1_new);
		S = (SIDE1+SIDE2+SIDE3)/2;
		area = Math.pow(S*(S-SIDE1)*(S-SIDE2)*(S-SIDE3),0.5);
		return area;
	}
	public double getPerimeter() // ?????????
	{
		double Side1 = p1_new.distance(p2_new); // ??method?????
		double Side2 = p2_new.distance(p3_new);
		double Side3 = p3_new.distance(p1_new);
		return Side1+Side2+Side3;
	}
	public boolean contains(MyPoint P) // ???????????
	{
		double side1,side2,side3,s,PA,PB,PC,S1,S2,S3,areaPAB,areaPBC,areaPCA,areaABC;
		side1 = p1.distance(p2);
		side2 = p2.distance(p3);
		side3 = p3.distance(p1);
		s = (side1+side2+side3)/2;
		PA = P.distance(p1);
		PB = P.distance(p2);
		PC = P.distance(p3);
		S1 = (PA+PB+side1)/2;
		S2 = (PB+PC+side2)/2;
		S3 = (PC+PA+side3)/2;
		areaPAB = Math.pow(S1*(S1-PA)*(S1-PB)*(S1-side1),0.5);
		areaPBC = Math.pow(S2*(S2-PB)*(S2-PC)*(S2-side2),0.5);
		areaPCA = Math.pow(S3*(S3-PC)*(S3-PA)*(S3-side3),0.5);
		areaABC = Math.pow(s*(s-side1)*(s-side2)*(s-side3),0.5);
		if (Math.abs(areaABC-areaPAB-areaPBC-areaPCA) < 0.5) // ??????0.5???
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	public boolean contains(Triangle2D input) // ??????????????????????
	{
		if(contains(input.getP1_new()) == true && contains(input.getP2_new()) == true && contains(input.getP3_new()) == true)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}	



