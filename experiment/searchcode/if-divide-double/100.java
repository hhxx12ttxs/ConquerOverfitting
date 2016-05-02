package AaaaaJaProgaju;

import java.io.*;

public class Calculator {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	    String s;
	    String[] temp;
	    double a;
	    double b;
	    int c;
	    double result = 0; 
	    System.out.println("Input arithmetical expression what you want to calculate, ");
	    System.out.println("separate different parts of expression with space.");
	    System.out.println("To exit the program click enter.");
	    
	    
	    while ((s = in.readLine()) != null && s.length() != 0){
	    	c = 0;
	    	temp = s.split(" ");
	    	if (temp.length == 3) {
	    		try {
	    	        a = Integer.parseInt(temp[0]);
	    	        b = Integer.parseInt(temp[2]);
	    	    } catch(NumberFormatException e) {
	    	        System.err.println("Input integers. Try again.");
	    	        break;
	    	    }
	    		if (temp[1].equals("+")) c = 1;
	    		if (temp[1].equals("-")) c = 2;
	    		if (temp[1].equals("*")) c = 3;
	    		if (temp[1].equals("/")) c = 4;
	    		
	    		switch (c){
	    		case 0: 
	    			System.err.println("Input +, -, * or / between integers. Try again.");
	    			continue;
	    		case 1: result = a + b; break; 
	    		case 2: result = a - b; break;
	    		case 3: result = a * b; break;
	    		case 4:
	    			Double tmp = divide (a, b);
	    			if(tmp == null) {
	    				System.out.println("Divide failed");
	    				continue;
	    			}
	    			result = tmp.doubleValue();
	    			break;
	    		}
	    		
	    		System.out.println("The expession result is " + result); 
	    	} else {
	    		System.out.println("The expession should consist three parts. Try again.");
	    	}
	    }
	    		    	
	}
	
	public static Double divide (double a, double b) {
		if (b == 0) {
			return null;
		} else {
			return new Double(a/b); 
		}
		
	}
	    
}


