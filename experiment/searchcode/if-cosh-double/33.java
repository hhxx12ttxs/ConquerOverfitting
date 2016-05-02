package de.georgwiese.calculationFunktions;

import android.util.Log;

/*
 * Copyright by Georg Wiese
 * 
 * CalcFkts v3.0 (2.5.2011)
 */

public class CalcFkts {

	public static String formatFktString(String s){
		String r="";
		for (int i=0;i<s.length();i++){		//deletes the spaces
			if (s.charAt(i)!=' '){
				r+=s.charAt(i);
			}
		}
		for(int i=0;i<r.length()-1;i++){		//intended to change "5x" to "5*x", for example
			boolean abort=false;
			//Log.d("Developer", "i: "+Integer.toString(i));
			//Log.d("Developer", "r: "+r);
			if(r.length()>(2+i) && r.substring(i, i+3).equalsIgnoreCase("abs"))
				i+=2;
			else if(r.length()>3+i && r.substring(i, i+4).equalsIgnoreCase("asin"))
				i+=3;
			else if(r.length()>3+i && r.substring(i, i+4).equalsIgnoreCase("acos"))
				i+=3;
			else if(r.length()>3+i && r.substring(i, i+4).equalsIgnoreCase("atan"))
				i+=3;
			else if(r.length()>4+i && r.substring(i, i+5).equalsIgnoreCase("asinh"))
				i+=4;
			else if(r.length()>4+i && r.substring(i, i+5).equalsIgnoreCase("acosh"))
				i+=4;
			else if(r.length()>4+i && r.substring(i, i+5).equalsIgnoreCase("atanh"))
				i+=4;

			else if(isNumber(r.charAt(i))&&isPossibleCharacter(r.charAt(i+1))){
				r=r.substring(0, i+1)+"*"+r.substring(i+1);
			}

		}
		return r;
	}
	
	private static boolean isNumber (char c){
		if (c=='0'|c=='1'|c=='2'|c=='3'|c=='4'|c=='5'|c=='6'|c=='7'|c=='8'|c=='9'|c=='x'|c=='y'|c=='b'|c=='c'|c=='a'
			|c=='i'|c=='I'|c=='e'|c=='E'|c==')')
			return true;
		return false;
	}
		
	private static boolean isPossibleCharacter (char c){
		String s=String.valueOf(c);
		if (s.equalsIgnoreCase("p")|s.equalsIgnoreCase("s")|s.equalsIgnoreCase("c")|s.equalsIgnoreCase("t")
				|s.equalsIgnoreCase("l")|s.equalsIgnoreCase("x")|s.equalsIgnoreCase("y")|s.equalsIgnoreCase("a")
				|s.equalsIgnoreCase("e") | c=='('|s.equalsIgnoreCase("b")|s.equalsIgnoreCase("c"))
			return true;
		return false;
	}
	
	private static int countOperators(String s,char o1,char o2){
		
		int brackets=0;
		int count=0;
		for(int i=0;i<s.length();i++){
			if(s.charAt(i)=='(')		//ignores anything inside brackets
				brackets++;
			if(s.charAt(i)==')')
				brackets--;
			if(brackets==0 & (s.charAt(i)==o1|s.charAt(i)==o2))	//count operators
				count++;
		}
		return count;
	}
	public static Double calculate (String s){
		return calculate(s,1.0,1.0); 
	}
	public static Double calculate (String s, Double x){
		return calculate(s,x,1.0); 
	}
	public static String prepareParam(String s, char param){
		return s.replace(param, 'y');
	}
	public static Double calculate (String s, Double x, Double y){
		//check for addends
		int addends=countOperators(s,'+','-');
		if (addends>0 & !(s.charAt(0)=='+'|s.charAt(0)=='-'))
			addends++;
		
		if (addends==1 & s.charAt(0)=='+')
			return calculate(s.substring(1), x, y);
		if (addends==1 & s.charAt(0)=='-')
			return (-1)*calculate(s.substring(1), x, y);
		
		if (addends>0){
			double result=0.0;
			String[] parts= new String[addends];
			int il=0;	//previous i
			int pos=0;	//position in the parts[] array
			int brackets=0;
			for (int i=0;i<s.length();i++){
				if(s.charAt(i)=='(')		//ignores anything inside brackets
					brackets++;
				if(s.charAt(i)==')')
					brackets--;
				if(brackets==0 & i!=0 & (s.charAt(i)=='+'|s.charAt(i)=='-')){
					parts[pos]=s.substring(il,i);
					il=i;
					pos++;
				}
			}
			parts[addends-1]=s.substring(il);
			
			for (int i=0;i<addends;i++)		//adding all parts
				result+=calculate(parts[i],x,y);
			return result;
		}
		
		//check for * or /
		int factors=countOperators(s,'*','/')+1;
		
		if (s.charAt(0)=='*')
			return calculate(s.substring(1),x,y);
		if (s.charAt(0)=='/')
			return 1/calculate(s.substring(1),x,y);
		
		if (factors>1){
			double result=1.0;
			String[] parts= new String[factors];
			int il=0;	//previous i
			int pos=0;	//position in the parts[] array
			int brackets=0;
			for (int i=0;i<s.length();i++){
				if(s.charAt(i)=='(')		//ignores anything inside brackets
					brackets++;
				if(s.charAt(i)==')')
					brackets--;
				
				if(brackets==0 & (s.charAt(i)=='*'|s.charAt(i)=='/')){
					parts[pos]=s.substring(il,i);
					il=i;
					pos++;
				}
			}
			parts[factors-1]=s.substring(il);
			
			for (int i=0;i<factors;i++)		//multiply all factors
				result*=calculate(parts[i],x,y);
			return result;
		}
		
		//check for exponents....
		int brackets=0;
		for (int i=0;i<s.length();i++){
			if (s.charAt(i)=='(')		//ignores anything inside brackets
				brackets++;
			if (s.charAt(i)==')')
				brackets--;
			if (brackets==0 & s.charAt(i)=='^')
				return Math.pow(calculate(s.substring(0,i),x,y),calculate(s.substring(i+1),x,y));
		}
		
		//check for brackets
		if(s.charAt(0)=='(')
			return calculate(s.substring(1,s.length()-1),x,y);
		
		//check for functions
		if(s.length()>4){
			if (s.substring(0,4).equalsIgnoreCase("sin("))
				return Math.sin(calculate(s.substring(4,s.length()-1),x,y));
			if (s.substring(0,4).equalsIgnoreCase("cos("))
				return Math.cos(calculate(s.substring(4,s.length()-1),x,y));
			if (s.substring(0,4).equalsIgnoreCase("tan("))
				return Math.tan(calculate(s.substring(4,s.length()-1),x,y));
			if (s.substring(0,3).equalsIgnoreCase("ln("))
				return Math.log(calculate(s.substring(3,s.length()-1),x,y));
			if (s.length()>6 && s.substring(0,5).equalsIgnoreCase("sqrt("))
				return Math.pow(calculate(s.substring(5,s.length()-1),x,y),0.5);
			if (s.length()>6 && s.substring(0,5).equalsIgnoreCase("asin("))
				return Math.asin(calculate(s.substring(5,s.length()-1),x,y));
			if (s.length()>6 && s.substring(0,5).equalsIgnoreCase("acos("))
				return Math.acos(calculate(s.substring(5,s.length()-1),x,y));
			if (s.length()>6 && s.substring(0,5).equalsIgnoreCase("atan("))
				return Math.atan(calculate(s.substring(5,s.length()-1),x,y));
			if (s.length()>6 && s.substring(0,5).equalsIgnoreCase("sinh("))
				return Math.sinh(calculate(s.substring(5,s.length()-1),x,y));
			if (s.length()>6 && s.substring(0,5).equalsIgnoreCase("cosh("))
				return Math.cosh(calculate(s.substring(5,s.length()-1),x,y));
			if (s.length()>6 && s.substring(0,5).equalsIgnoreCase("tanh("))
				return Math.tanh(calculate(s.substring(5,s.length()-1),x,y));
			if (s.length()>7 && s.substring(0,6).equalsIgnoreCase("asinh("))
				return asinh(calculate(s.substring(5,s.length()-1),x,y));
			if (s.length()>7 && s.substring(0,6).equalsIgnoreCase("acosh("))
				return acosh(calculate(s.substring(5,s.length()-1),x,y));
			if (s.length()>7 && s.substring(0,6).equalsIgnoreCase("atanh("))
				return atanh(calculate(s.substring(5,s.length()-1),x,y));
			if (s.length()>5 && s.substring(0,4).equalsIgnoreCase("abs("))
				return Math.abs(calculate(s.substring(4,s.length()-1)));
			if (s.length()>5 && s.substring(0,4).equalsIgnoreCase("log("))
				return Math.log10(calculate(s.substring(4,s.length()-1)));
		}
		
		//set variables and constants
		if (s.equalsIgnoreCase("x"))
			return x;
		if (s.equalsIgnoreCase("y"))
			return y;
		if (s.equalsIgnoreCase("PI") |s.equalsIgnoreCase("\u03C0"))
			return 3.14159265;
		if (s.equalsIgnoreCase("e"))
			return 2.71828183;
		
		//set numbers
		  //doubles
		for (int i=0;i<s.length();i++)
			if (s.charAt(i)==','|s.charAt(i)=='.')
				return calculate(s.substring(0,i),x,y)+calculate(s.substring(i+1),x,y)/(Math.pow(10, s.length()-1-i));
		  //integers
		try {
			    return 1.0*Integer.parseInt(s);
			  } catch(NumberFormatException e) { }
			  
		return 0.0;		//should never occur
	}

	public static Boolean check (String s){
		s=formatFktString(s);
		//Log.d("Developer", s);
		int brackets2 = 0;
		for (int i=0; i<s.length();i++){
			if (s.charAt(i)=='(')
				brackets2++;
			if (s.charAt(i)==')')
				brackets2--;
		}
		if (brackets2!=0)
			return false;
		try{
		
			if(s.charAt(s.length()-1)=='+'|
					s.charAt(s.length()-1)=='-'|
					s.charAt(s.length()-1)=='/'|
					s.charAt(s.length()-1)=='*'|
					s.charAt(s.length()-1)=='('|
					s.charAt(s.length()-1)=='^')
				return false;
			
		//check for addends
		int addends=countOperators(s,'+','-');
		if (addends>0 & !(s.charAt(0)=='+'|s.charAt(0)=='-'))
			addends++;
		
		if (addends==1 & s.charAt(0)=='+')
			return check(s.substring(1));
		if (addends==1 & s.charAt(0)=='-')
			return check(s.substring(1));
		
		if (addends>0){
			Boolean result=true;
			String[] parts= new String[addends];
			int il=0;	//previous i
			int pos=0;	//position in the parts[] array
			int brackets=0;
			for (int i=0;i<s.length();i++){
				if(s.charAt(i)=='(')		//ignores anything inside brackets
					brackets++;
				if(s.charAt(i)==')')
					brackets--;
				if(brackets==0 & i!=0 & (s.charAt(i)=='+'|s.charAt(i)=='-')){
					parts[pos]=s.substring(il,i);
					il=i;
					pos++;
				}
			}
			parts[addends-1]=s.substring(il);
			
			for (int i=0;i<addends;i++)		//adding all parts
				if(!check(parts[i]))
					result=false;
			return result;
		}
		
		//check for * or /
		int factors=countOperators(s,'*','/')+1;
		
		if (s.charAt(0)=='*')
			return check(s.substring(1));
		if (s.charAt(0)=='/')
			return check(s.substring(1));
		
		if (factors>1){
			Boolean result=true;
			String[] parts= new String[factors];
			int il=0;	//previous i
			int pos=0;	//position in the parts[] array
			int brackets=0;
			for (int i=0;i<s.length();i++){
				if(s.charAt(i)=='(')		//ignores anything inside brackets
					brackets++;
				if(s.charAt(i)==')')
					brackets--;
				
				if(brackets==0 & (s.charAt(i)=='*'|s.charAt(i)=='/')){
					parts[pos]=s.substring(il,i);
					il=i;
					pos++;
				}
			}
			parts[factors-1]=s.substring(il);
			
			for (int i=0;i<factors;i++)		//multiply all factors
				if(!check(parts[i]))
					result=false;
			return result;
		}
		
		//check for exponents....
		int brackets=0;
		for (int i=0;i<s.length();i++){
			if (s.charAt(i)=='(')		//ignores anything inside brackets
				brackets++;
			if (s.charAt(i)==')')
				brackets--;
			if (brackets==0 & s.charAt(i)=='^')
				return check(s.substring(0,i))&check(s.substring(i+1));
		}
		
		//check for brackets
		if(s.charAt(0)=='(')
			return check(s.substring(1,s.length()-1));
		
		//check for functions
		if(s.length()>4){
			if (s.substring(0,4).equalsIgnoreCase("sin("))
				return check(s.substring(4,s.length()-1));
			if (s.substring(0,4).equalsIgnoreCase("cos("))
				return check(s.substring(4,s.length()-1));
			if (s.substring(0,4).equalsIgnoreCase("tan("))
				return check(s.substring(4,s.length()-1));
			if (s.substring(0,3).equalsIgnoreCase("ln("))
				return check(s.substring(3,s.length()-1));
			if (s.length()>6 && s.substring(0,5).equalsIgnoreCase("sqrt("))
				return check(s.substring(5,s.length()-1));
			if (s.length()>6 && s.substring(0,5).equalsIgnoreCase("asin("))
				return check(s.substring(5,s.length()-1));
			if (s.length()>6 && s.substring(0,5).equalsIgnoreCase("acos("))
				return check(s.substring(5,s.length()-1));
			if (s.length()>6 && s.substring(0,5).equalsIgnoreCase("atan("))
				return check(s.substring(5,s.length()-1));
			if (s.length()>6 && s.substring(0,5).equalsIgnoreCase("sinh("))
				return check(s.substring(5,s.length()-1));
			if (s.length()>6 && s.substring(0,5).equalsIgnoreCase("cosh("))
				return check(s.substring(5,s.length()-1));
			if (s.length()>6 && s.substring(0,5).equalsIgnoreCase("tanh("))
				return check(s.substring(5,s.length()-1));
			if (s.length()>7 && s.substring(0,6).equalsIgnoreCase("asinh("))
				return check(s.substring(6,s.length()-1));
			if (s.length()>7 && s.substring(0,6).equalsIgnoreCase("acosh("))
				return check(s.substring(6,s.length()-1));
			if (s.length()>7 && s.substring(0,6).equalsIgnoreCase("atanh("))
				return check(s.substring(6,s.length()-1));
			if (s.length()>5 && s.substring(0,4).equalsIgnoreCase("abs("))
				return check(s.substring(4,s.length()-1));
			if (s.length()>5 && s.substring(0,4).equalsIgnoreCase("log("))
				return check(s.substring(4,s.length()-1));
		}
		
		//set variables and constants
		if (s.equalsIgnoreCase("x"))
			return true;
		if (s.equalsIgnoreCase("y"))
			return true;
		if (s.equalsIgnoreCase("PI") | s.equalsIgnoreCase("\u03C0"))
			return true;
		if (s.equalsIgnoreCase("e"))
			return true;
		if (s.equalsIgnoreCase("a"))
			return true;
		if (s.equalsIgnoreCase("b"))
			return true;
		if (s.equalsIgnoreCase("c"))
			return true;
		
		//set numbers
		  //doubles
		for (int i=0;i<s.length();i++)
			if (s.charAt(i)==','|s.charAt(i)=='.')
				return check(s.substring(0,i))&check(s.substring(i+1));
		  //integers
		try {
			    Integer.parseInt(s);
			  } catch(NumberFormatException e) {
				  return false;
			  }
			  
		return true;		//should never occur
	
		}catch(java.lang.StringIndexOutOfBoundsException e){
			return false;
		}
	}
	
	public static double asinh (double x){
		return Math.log(x+Math.sqrt(1+x*x));
	}
	
	public static double acosh (double x){
		return 2*Math.log(Math.sqrt((x+1)/2)+Math.sqrt((x-1)/2));
	}
	
	public static double atanh (double x){
		return (Math.log(1+x)-Math.log(1-x))/2;
	}
}

