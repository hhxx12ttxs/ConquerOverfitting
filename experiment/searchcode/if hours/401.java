import java.util.Scanner;


public class Main
{

	static Scanner scan = new Scanner(System.in);

	public static void main(String[] args) throws InterruptedException
	{

		System.out.println(project1());
		//System.out.println(project2());
		//System.out.println(project3());

	}

	public static double project1()
	{
		double p = 0;
		double hours;
		double rate;

		System.out.println("Please input Hours:");
		hours = scan.nextDouble();

		System.out.println("Please input Pay rate:");
		rate = scan.nextDouble();

		if(hours < 20)
		{

			p = hours * rate;

		}
		else if(hours < 40)
		{

			p = hours * ( 2 * rate );

		}
		else if(hours < 60)
		{

			p = hours * ( 3 * rate );

		}
		else
		{

			p = 60 * ( 3 * rate );

		}


		return p;
	}

	public static int project2()
	{
		int d = 0;
		String plan;
		int hours;

		System.out.println("Which plan do you have? (a,b,c)");
		plan = scan.next();

		System.out.println("How many hours did you use?");
		hours = scan.nextInt();

		if(plan.equals("a"))
		{
			if(hours <= 11)
			{

				d =30;

			}
			else if(hours <=  22)
			{

				d = 30 + (3*(hours - 11));

			}
			else
			{

				d = 63 + (6*(hours - 22));

			}

		}
		else if(plan.equals("b"))
		{
			if(hours <= 22)
			{

				d =35;

			}
			else if(hours <=  44)
			{

				d = 35 + (2*(hours - 22));

			}
			else
			{

				d = 79 + (4*(hours - 44));

			}

		}
		else if(plan.equals("c"))
		{
			if(hours <= 33)
			{

				d =40;

			}
			else if(hours <=  66)
			{

				d = 40 + (1*(hours - 33));

			}
			else
			{

				d = 106 + (2*(hours - 66));

			}

		}

		return d;
	}

	public static int project3(int n) throws InterruptedException
	{
		//		int o = 0;
		//		int a = 1;
		//		int b = 1;
		//
		//		for(int i = 1; i > 0; i++)
		//		{
		//
		//			int c;
		//
		//			c = a + b;
		//			a = b;
		//			b = c;
		//
		//			System.out.println(c);
		//
		//			Thread.sleep(500);
		//
		//		}
		//		
		//		return o;


		if (n == 1 || n == 2)
		{
			return 1;
		} 
		
		return project3(n - 1) + project3(n - 2);

	}

}

