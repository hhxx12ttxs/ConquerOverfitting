package ce1002.s101502023;

import java.util.Scanner;

public class Q1 
{
    public static void main(String[] args)
    {
        int s=1;
        
    	while(s!=0)
    	{	
    	    int X;
	    	System.out.println("Please choose the method you want to use:");
	    	System.out.println("1.toFahrenheit");
	    	System.out.println("2.toCelcius");
	    	System.out.println("3.Exit");
	    	Scanner input=new Scanner(System.in);
	    	X=input.nextInt();
	    	if(X==1)//choose the method of three ways.
	    	{
	    		System.out.println("Please input the temperature:");
	    		double x=input.nextDouble();
	    		double a=(x*9/5+32);//change Celcius into Fahrenheit.
	    		System.out.println(x+" in Celcius is equal to "+a+" in Fahrenheit.");
	    	}
	    	else if(X==2)//choose the method of three ways.
	    	{
	    		System.out.println("Please input the temperature:");
	    		double y=input.nextDouble();
	    		double b=((y-32)*5/9);//change Fahrenheit into Celcius.
	    		System.out.println(y+" in Fahrenheit is equal to "+b+" in Celcius.");
	        }
	    	else if(X==3)//choose the method of three ways.
	    	{
	    		System.out.println("Good Bye");
	    		break;//if choose the last method,it would be closed.
	    	}
	    	System.out.println(" ");
    	}	
    }
}

