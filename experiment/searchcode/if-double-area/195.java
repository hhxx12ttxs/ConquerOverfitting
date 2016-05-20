package a4.s100502508;

public class Triangle2D 
{
	private MyPoint p1 = new MyPoint(0.0, 0.0); // points of original triangle triangle
	private MyPoint p2 = new MyPoint(17.0, 6.0);
	private MyPoint p3 = new MyPoint(10.0, 15.0);
	private MyPoint p1_new, p2_new, p3_new; // points of new triangle

	public MyPoint getP1_new()// get the p1 of new triangle
	{ 
		return p1_new;
	}

	public MyPoint getP2_new()// get the p2 of new triangle
	{ 
		return p2_new;
	}

	public MyPoint getP3_new()// get the p3 of new triangle
	{ 
		return p3_new;
	}

	public Triangle2D(double x1, double y1, double x2, double y2, double x3, double y3)// constructor with arguments to initial the points of the new triangle
	{
		p1_new=new MyPoint(x1,y1);
		p2_new=new MyPoint(x2,y2);
		p3_new=new MyPoint(x3,y3);
		
	}

	public double getArea()// calculate the area of the new triangle
	{ 
		double s=getPerimeter()/2;
		return Math.sqrt(s*(s-p1_new.distance(p2_new))*(s-p2_new.distance(p3_new))*(s-p3_new.distance(p1_new)));
	}

	public double getPerimeter()// calculate the perimeter of the new triangle
	{ 
		return p1_new.distance(p2_new)+p2_new.distance(p3_new)+p3_new.distance(p1_new);
	}
	
	public double area(MyPoint P1, MyPoint P2, MyPoint P3)//calculate three(MyPoint object) parameters area of a triangle 
	{
		double side1=P1.distance(P2);
		double side2=P2.distance(P3);
		double side3=P3.distance(P1);
		double s=(side1+side2+side3)/2;
		return Math.sqrt(s*(s-side1)*(s-side2)*(s-side3));
	}
	
	public boolean contains(MyPoint point)// check the input point is in the original triangle of the object or not
	{ 
		if(Math.abs(area(p1,p2,p3)-area(point,p1,p2)-area(point,p2,p3)-area(point,p3,p1))<=0.5)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean contains(Triangle2D input)// check the input triangle(Triangle2D object) is in the original triangle of this object or not
	{
		if(Math.abs(area(p1,p2,p3)-area(input.getP1_new(),p1,p2)-area(input.getP1_new(),p2,p3)-area(input.getP1_new(),p3,p1))<=0.5)
		{
			if(Math.abs(area(p1,p2,p3)-area(input.getP2_new(),p1,p2)-area(input.getP2_new(),p2,p3)-area(input.getP2_new(),p3,p1))<=0.5)
			{
				if(Math.abs(area(p1,p2,p3)-area(input.getP3_new(),p1,p2)-area(input.getP3_new(),p2,p3)-area(input.getP3_new(),p3,p1))<=0.5)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
}
