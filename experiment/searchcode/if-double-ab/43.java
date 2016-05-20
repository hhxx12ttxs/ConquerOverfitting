package a4.s100502505;

public class Triangle2D {
	private MyPoint p1 = new MyPoint(0.0,0.0);//??p1???
	private MyPoint p2 = new MyPoint(17.0,6.0);//??p2???
	private MyPoint p3 = new MyPoint(10.0,15.0);//??p3???
	private MyPoint p1_new,p2_new,p3_new;
	
	public MyPoint getP1_new()//p1_new???
	{
		return p1_new;
	}
	
	public MyPoint getP2_new()//p2_new???
	{
		return p2_new;
	}
	
	public MyPoint getP3_new()//p3_new???
	{
		return p3_new;
	}
	
	public Triangle2D(double x1,double y1,double x2,double y2,double x3,double y3)//????? ?????
	{
		p1_new = new MyPoint(x1,y1);
		p2_new = new MyPoint(x2,y2);
		p3_new = new MyPoint(x3,y3);
	}
	
	public double getArea()//?????? ????????
	{
		double dis1 = Math.sqrt(Math.pow(p1_new.getX()-p2_new.getX(),2) + Math.pow(p1_new.getY()-p2_new.getY(),2));
		double dis2 = Math.sqrt(Math.pow(p2_new.getX()-p3_new.getX(),2) + Math.pow(p2_new.getY()-p3_new.getY(),2));
		double dis3 = Math.sqrt(Math.pow(p1_new.getX()-p3_new.getX(),2) + Math.pow(p1_new.getY()-p3_new.getY(),2));
		double Area = Math.sqrt((getPerimeter()/2)*(getPerimeter()/2-dis1)*(getPerimeter()/2-dis2)*(getPerimeter()/2-dis3));
		return Area;
	}
	
	public double getPerimeter()//??????
	{
		double dis1 = Math.sqrt(Math.pow(p1_new.getX()-p2_new.getX(),2) + Math.pow(p1_new.getY()-p2_new.getY(),2));
		double dis2 = Math.sqrt(Math.pow(p2_new.getX()-p3_new.getX(),2) + Math.pow(p2_new.getY()-p3_new.getY(),2));
		double dis3 = Math.sqrt(Math.pow(p1_new.getX()-p3_new.getX(),2) + Math.pow(p1_new.getY()-p3_new.getY(),2));
		return dis1 + dis2 + dis3;
	}
	
	public boolean contains(MyPoint p)//????????? ?????
	{
		double PA = p1.distance(p);
		double PB = p2.distance(p);
		double PC = p3.distance(p);
		double AB = p1.distance(p2);
		double BC = p2.distance(p3);
		double AC = p1.distance(p3);
		double areaPAB = Math.sqrt((PA+PB+AB)/2.0*((PA+PB+AB)/2.0-PA)*((PA+PB+AB)/2.0-PB)*((PA+PB+AB)/2.0-AB));
		double areaPAC = Math.sqrt((PA+PC+AC)/2.0*((PA+PC+AC)/2.0-PA)*((PA+PC+AC)/2.0-PC)*((PA+PC+AC)/2.0-AC));
		double areaPBC = Math.sqrt((PB+PC+BC)/2.0*((PB+PC+BC)/2.0-PC)*((PB+PC+BC)/2.0-PB)*((PB+PC+BC)/2.0-BC));
		double areaABC = Math.sqrt((AB+AC+BC)/2.0*((AB+AC+BC)/2.0-AB)*((AB+AC+BC)/2.0-AC)*((AB+AC+BC)/2.0-BC));
		if(Math.abs(areaABC-(areaPAB + areaPAC + areaPBC)) <=0.5)
		{
			return true;
		}else{
			return false;
		}
	}
	
	public boolean contains(Triangle2D input)//?????????????
	{
		if(contains(input.getP1_new()) && contains(input.getP2_new()) && contains(input.getP3_new()))
		{
			return true;
		}else{
			return false;
		}
	}
}//??!

