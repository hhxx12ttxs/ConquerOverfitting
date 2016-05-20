package projecteuler;

public class P33DigitCancellingFractions
{
	
	public static void main(String[] args)
	{
		StopWatch watch = new StopWatch().start();
		int prodnum = 1;
		int proddem = 1;
		for(int a=1;a<10;a++)
			for(int b=1;b<10;b++)
				for(int c=1;c<10;c++)
				{
					// ab/bc
					int num = 10*a+b;
					int dem = 10*b+c;
					if(num>=dem) continue;
					if((double)num/dem==(double)a/c)
					{
						prodnum*=num;
						proddem*=dem;
						System.out.println(10*a+b+" "+(10*b+c));
					}
				}
		System.out.println(prodnum+"/"+proddem); // 1/100
		System.out.println(watch.getElapsedMicroseconds()+" micros");
	}
	
}
