//A program for print the given series i.e: 1!+2!+3!+...+n!
package com.vl.sf.core;
import java.util.Scanner;;

public class FactorialSeries {
	public long factorialSeries(int number){
		long factorialSeriesSum=0;//local variable for total sum of the series
		long factorial=1;//local variable for find factorial value of each digit
		long preNumberFactorial=1;
		if(number>=0){
			for (int i = 1; i <=number; i++) {
                factorial=preNumberFactorial*i;
			    preNumberFactorial=factorial;
                factorialSeriesSum+=factorial;
                factorial=1;
			}//outer for loop
            return factorialSeriesSum;
		}else {
            return 0;
		}//else block
	}//end of factorialSeries()

	public static void main(String[] args) {
		FactorialSeries fs=new FactorialSeries();
		Scanner scanner=new Scanner(System.in);
		long factorialSum=0;//local variable to assign the returned value of the series
		System.out.print("Enter the Value of n for 1!+2!+3!+.....+n!):");
		factorialSum=fs.factorialSeries(scanner.nextInt());
		
		if(factorialSum!=0){
			System.out.println("The total sum of the given series is "+factorialSum);
		}else {
			System.out.println("U entered a negetive number");
		}
		scanner.close();
	}//end of main()

}//end of class

