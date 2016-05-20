package a4.s100502033;
import java.io.*;
import javax.swing.JOptionPane;
import java.util.Scanner;
public class Triangle2D 
{
	private MyPoint p1 = new MyPoint( 0.0, 0.0); //old Triangle
	private MyPoint p2 = new MyPoint(17.0, 6.0);
	private MyPoint p3 = new MyPoint(10.0, 15.0);
	private MyPoint p1_new, p2_new, p3_new;
	public void getP1_new()//newTriangle
	{
		System.out.print(p1_new.getX() + " " + p1_new.getY() + " ");
	}
	public void getP2_new()//newTriangle
	{
		System.out.print(p2_new.getX() + " " + p2_new.getY() + " ");
	}
	public void getP3_new()//newTriangle
	{
		System.out.println(p3_new.getX() + " " + p3_new.getY());
	}
	public Triangle2D(double x1, double y1, double x2, double y2, double x3, double y3)//constructor to initial private data member
	{
		MyPoint p4_new = new MyPoint(x1 , y1);
		MyPoint p5_new = new MyPoint(x2 , y2);
		MyPoint p6_new = new MyPoint(x3 , y3);
		p1_new = p4_new;
		p2_new = p5_new;
		p3_new = p6_new;
		
	}
	public double getPerimeter() //????
	{
		return p1_new.distance(p2_new) + p2_new.distance(p3_new) + p3_new.distance(p1_new);
	}
	public double getArea()//????
	{
		return Math.sqrt(getPerimeter()/2 * (getPerimeter()/2 - p1_new.distance(p2_new)) * (getPerimeter()/2 - p2_new.distance(p3_new)) * (getPerimeter()/2 - p3_new.distance(p1_new)));
		//????
	}
	public boolean contains(MyPoint p)//?????????????
	{
		double PerimeterPAB, PerimeterPBC, PerimeterPCA, PerimeterABC, areaPAB, areaPBC, areaPCA, areaABC;
		double AB = p1.distance(p2);
		double BC = p2.distance(p3);
		double CA = p3.distance(p1);
		double AP = p1.distance(p);
		double BP = p2.distance(p);
		double CP = p3.distance(p);
		PerimeterPAB = AB + AP + BP;//??PAB,PBC,PCA,ABC???
		PerimeterPBC = BC + BP + CP;
		PerimeterPCA = CA + CP + AP;
		PerimeterABC = AB + BC + CA;
		areaPAB = Math.sqrt(PerimeterPAB/2 * (PerimeterPAB/2 - AB) * (PerimeterPAB/2 - AP) * (PerimeterPAB/2 - BP));
		areaPBC = Math.sqrt(PerimeterPBC/2 * (PerimeterPBC/2 - BC) * (PerimeterPBC/2 - BP) * (PerimeterPBC/2 - CP));
		areaPCA = Math.sqrt(PerimeterPCA/2 * (PerimeterPCA/2 - CA) * (PerimeterPCA/2 - CP) * (PerimeterPCA/2 - AP));
		areaABC = Math.sqrt(PerimeterABC/2 * (PerimeterABC/2 - AB) * (PerimeterABC/2 - BC) * (PerimeterABC/2 - CA));
		//??PAB,PBC,PCA,ABC???
		if(areaPAB + areaPBC + areaPCA - areaABC < 0.5 && areaPAB + areaPBC + areaPCA - areaABC > -0.5)//????+-0.5
		{
			return true;
		}
		else
		{
			return false ;
		}
		
	}
	public boolean contains(Triangle2D input)//??????????????
	{	
		if( input.contains(input.p1_new) == true && input.contains(input.p2_new) == true && input.contains(input.p3_new) == true)//??????????????????
		{
			return true;
		}
		else 
		{
			return false;
		}
		
	}
}

