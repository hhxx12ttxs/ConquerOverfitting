import java.io.*;
import java.util.*;
class FactorialIterator<Factprial> implements Iterator<Object> {

	private ArrayList<?> factorial;
	private int position;

	FactorialIterator(Factorial1 obj) 
	{
		this.factorial = obj.getFactorial();
	}

	@Override
	public boolean hasNext() 
	{
		if (position < factorial.size())
			return true;
		else
			return false;
	}

	@Override
	public Object next() 
	{
		Object factObj = factorial.get(position);
		position++;
		return factObj;
	}

	@Override
	public void remove() 
	{
		factorial.remove(position);
	}

}

class Factorial 
{
	private int lowerLimit;
	private int upperLimit;
	Factorial()
	{}
	Factorial(int lowerLimit,int upperLimit)
	{
		this.lowerLimit=lowerLimit;
		this.upperLimit=upperLimit;
	}
	public  String toString()
	{
		int fact=1,i=1;
		while(i<=lowerLimit)
		{
			fact*=i;
			i++;
		}
		String result=fact+"";
		while(i<=upperLimit)
		{
			fact*=i;
			result+=" "+fact;
			i++;
		}
		return result;
	}			
}
class Factorial1 implements Iterable<Factorial>
{
	private ArrayList<Factorial> factorial = new ArrayList<Factorial>();
	Factorial1(){}
	public Factorial1(ArrayList al)
	{
		this.factorial=al;
	}
	public ArrayList getFactorial() 
	{
		return this.factorial;
	}
	@Override
	public Iterator<Factorial> iterator()
	{
		return new FactorialIterator(this);
	}
}
class Program3
{
	public static void main(String args[])
	{
		Factorial obj=new Factorial(2,4);
		System.out.println(obj);
		ArrayList<Factorial> factorialList = new ArrayList<Factorial>();
		factorialList.add(new Factorial(1,5));
		factorialList.add(new Factorial(2,6));
		factorialList.add(new Factorial(6,10));
	
		Factorial1 factorial= new Factorial1(factorialList);
		Iterator it=factorial.iterator();
		while(it.hasNext())
		{
			Factorial obj1=(Factorial)it.next();
			System.out.println(obj1);
		}			
	}
}

