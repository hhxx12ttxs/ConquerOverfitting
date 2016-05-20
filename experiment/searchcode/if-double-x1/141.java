package a4.s995002201;

public class Triangle2D 
{
	private double x1=0,x2=0,x3=10,y1=15,y2=17,y3=6;
	private double p1,p2;
	public double getArea(double a1,double b1,double a2,double b2,double a3,double b3)
	{
		x1 = a1;
		y1 = b1;
		x2 = a2;
		y2 = b2;
		x3 = a3;
		y3 = b3;
		double lengtha=0,lengthb=0,lengthc=0,s=0,area=0;
		lengtha = Math.sqrt(Math.abs(x1-x2)*Math.abs(x1-x2) + Math.abs(y1-y2)*Math.abs(y1-y2));
		lengthb = Math.sqrt(Math.abs(x3-x2)*Math.abs(x3-x2) + Math.abs(y3-y2)*Math.abs(y3-y2));
		lengthc = Math.sqrt(Math.abs(x3-x1)*Math.abs(x3-x1) + Math.abs(y3-y1)*Math.abs(y3-y1));
		s = 0.5*(lengtha+lengthb+lengthc);
		area = Math.sqrt(s*(s-lengtha)*(s-lengthb)*(s-lengthc));
		return area;
	}
	public double getPerimeter(double a1,double b1,double a2,double b2,double a3,double b3)
	{
		x1 = a1;
		y1 = b1;
		x2 = a2;
		y2 = b2;
		x3 = a3;
		y3 = b3;
		double lengtha=0,lengthb=0,lengthc=0,finallength=0;
		lengtha = Math.sqrt(Math.abs(x1-x2)*Math.abs(x1-x2) + Math.abs(y1-y2)*Math.abs(y1-y2));
		lengthb = Math.sqrt(Math.abs(x3-x2)*Math.abs(x3-x2) + Math.abs(y3-y2)*Math.abs(y3-y2));
		lengthc = Math.sqrt(Math.abs(x3-x1)*Math.abs(x3-x1) + Math.abs(y3-y1)*Math.abs(y3-y1));
		finallength = lengtha+lengthb+lengthc;
		return finallength;
	}
	public boolean contains(Triangle2D input)
	{
		MyPoint mypoint = new MyPoint();
		mypoint.MyPoint(x1, y1, x2, y2, x3, y3);
		
		if((contains(x1,y1)==true)&&(contains(x2,y2)==true)&&(contains(x3,y3)==true))
			return true;
		else
			return false;	
	}
	public boolean contains(double x,double y)
	{
		p1 = x;
		p2 = y;
		double area1=0,area2=0,area3=0,ans=0,ans1=0;
		MyPoint mypoint = new MyPoint();
		mypoint.MyPoint(x1, y1, x2, y2, x3, y3);
		area1 = getArea(p1, p2, 10, 15, 17, 6);
		area2 = getArea(0, 0, p1, p2, 17, 6);
		area3 = getArea(0, 0, 10, 15, p1, p2);
		ans = area1+area2+area3;
		ans1 = getArea(0, 0, 10, 15, 17, 6);
		if(ans1-ans<=0.5)
			return true;
		else
			return false;
	}
}

