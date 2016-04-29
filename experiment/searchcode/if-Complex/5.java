import java.io.*;
class ExComplex
{
	public static void main(String args[])
	{
		Complex a=new Complex(2,3);
		Complex b=new Complex(2,3);
		Complex c=a.add(b);
		c.display();
		c.sub(a,b);
		c.display();
		c=Complex.multiply(a,b);
		c.display();
		c.div(a,b);
		c.display();
	}
}
class Complex
{
	private int i,r;
	public Complex()
	{}
	public Complex(int r,int i)
	{
		this.i=i;
		this.r=r;
	}
	public static Complex multiply(Complex a,Complex b)
	{
		Complex d=new Complex();
		d.i=a.i*b.r-a.i*b.i;
		d.r=a.r*b.i-b.r*a.i;
		return d;	
 	}
	public void div(Complex a,Complex b)
	{
		i=(a.i*b.r-a.i*b.r)/(b.r*b.r+b.i*b.i);
		r=(a.r*b.r+a.i*b.i)/(b.r*b.r+b.i*b.i);
	}
	public void display()
	{
		System.out.print(r+"");
		if(i<0)
			System.out.println(i+"i");
		else
			System.out.println("+"+i+"i");
	}
	public Complex add(Complex a)
	{
		Complex d=new Complex();	
		d.i=a.i+i;	
		d.r=a.r+r;
		return d;
	}
	public void sub(Complex a,Complex b)
	{
		i=a.i-b.i;
		r=a.r-b.r;
	}
	

			
}
	
		
