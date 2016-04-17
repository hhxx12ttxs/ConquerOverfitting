package AlgoImplementation;


import java.util.Scanner;

public class TimeInWords {

	public static void main(String ar[])
	{
		Scanner scan=new Scanner(System.in);
		int hour=scan.nextInt();
		int min=scan.nextInt();
		String[] hours={"null","one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve"};
		if(min==30)
		{
			System.out.println("half past "+hours[hour]);
			return;
		}
		else if(min<30)
		{
			if(min==15)
			{
				System.out.println("quarter past "+hours[hour]);
				return;
			}
			if(min==00)
			{
				System.out.println(hours[hour]+" o' clock");
				return;
			}
			if(min<13)
			{
				System.out.println(hours[min]+" minutes past "+hours[hour]);
				return;
			}
            if(min==13)
			{
				System.out.println("thirteen minutes past "+hours[hour+1]);
				return;
			}
			if(min>12&&min<20)
			{
				int temp=min-10;
				System.out.println(hours[temp]+"teen past "+hours[hour]);
				return;
			}
			if(min>19)
			{
				int temp=min-20;
				if(min==20)
					{
					System.out.println("twenty minutes past "+hours[hour]);
					return;
					}
				else
					{
					System.out.println("twenty "+hours[temp]+" minutes past "+hours[hour]);
					return;
					}
		}
		}
		else
		{
			min=60-min;
			if(min==15)
			{
				System.out.println("quarter to "+hours[hour+1]);
				return;
			}
			if(min<13)
			{
				System.out.println(hours[min]+" minutes to "+hours[hour+1]);
				return;
			}
            if(min==13)
			{
				System.out.println("thirteen minutes to "+hours[hour+1]);
				return;
			}
			if(min>12&&min<20)
			{
				int temp=min-10;
				System.out.println(hours[temp]+"teen minutes to "+hours[hour+1]);
				return;
			}
			if(min>19)
			{
				int temp=min-20;
				if(min==20)
					{
					System.out.println("twenty minutes to "+hours[hour+1]);
					return;
					}
				else
					{
					System.out.println("twenty "+hours[temp]+" minutes to "+hours[hour+1]);
					return;
					}
			}
		}
	}
}

