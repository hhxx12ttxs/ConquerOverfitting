class factorial
{
	public int calculate(int n)
	{
		if(n==1)
			return 1;
		else
		{
			return n*calculate(n-1);
		}
	}
}
class fac
{
	public static void main(String args[])
	{
		factorial f=new factorial();
		System.out.println(f.calculate(5));
	}
}

