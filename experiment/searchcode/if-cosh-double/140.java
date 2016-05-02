package de.georgwiese.calculationFunktions;

/*
 * Copyright by Georg Wiese
 * 
 * Function v0.9.2 (30.1.11)
 */

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;

import android.util.Log;

public class Function {
	ArrayList<Addent> addents = new ArrayList<Addent>();
	Boolean isValue;
	double value;
	double x,y,a,b,c;
	String string;
	
	public Function (String function){
		//Log.d("Developer", "Function: "+function);
		string=function;
		x=0; y=0; this.a=0; this.b=0; this.c=0;
		isValue=isValue(function);
		if(isValue)
			this.value=CalcFkts.calculate(function);
		int brackets=0;
		if (function.charAt(0)!='+' & function.charAt(0)!='-')
			function="+"+function;
		for (int i=1; i<function.length();i++){
			if (function.charAt(i)=='(') brackets++;
			if (function.charAt(i)==')') brackets--;
			if ((function.charAt(i)=='+' | function.charAt(i)=='-') & brackets==0){
				addents.add(new Addent(function.substring(0, i)));
				function=function.substring(i);
				i=0;
				brackets=0;
			}
		}
		addents.add(new Addent(function));
	}
	public Function (String function, double a, double b, double c){
		this(function);
		this.a=a; this.b=b; this.c=c;
	}
	public Function clone(){
		Function f = new Function(getString());
		f.setParams(new double[]{a, b, c});
		return f;
	}
	public String getString(){
		return string;
	}
	public void setA(double a){
		this.a=a;
	}
	public double getA(){
		return this.a;}
	public void setB(double b){
		this.b=b;}
	public double getB(){
		return this.b;}
	public void setC(double c){
		this.c=c;}
	public double getC(){
		return this.c;}
	public void setParams(double[] params){
		a = params[0];
		b = params[1];
		c = params[2];
	}
	
	public double slope(double x){
		return (calculate(x)-calculate(x-0.000001))/(0.000001);
	}
	public double calculate(double x){
		return calculate(x,0);
	}
	public double calculate(double x, double y, double a, double b, double c){
		this.a=a; this.b=b; this.c=c;
		return calculate(x,y);
	}
	public double calculate(double x, double y){
		if (this.isValue)
			return this.value;
		this.x=x;
		this.y=y;
		double result=0.0;
		for (Addent add:addents)
			result+=add.calculate();
		return result;
	}
	
	public ArrayList<Double> getDiscontinuities(double startX, double endX, double precision){
		ArrayList<Double> result = new ArrayList<Double>();
		for (Addent a:addents){
			ArrayList<Double> inAd = a.getDiscontinuities(startX, endX, precision);
			for (double d:inAd)
				result.add(d);
		}
		Collections.sort(result);
		for(int i=1; i<result.size(); i++){
			if (result.get(i-1).equals(result.get(i))){
				result.remove(i);
				i--;
			}
		}
		return result;
	}
	
	private Boolean isValue(String s){
		Boolean result=true;
		
		for (int i=0; i<s.length();i++){
			char c = s.charAt(i);
			if (c=='x' | c=='y' | c=='a' | c=='b' | c=='c' | c=='X' | c=='Y' | c=='A' | c=='B' | c=='C')
				result=false;
		}
		
		return result;
	}
	
	private class Addent{
		ArrayList<Factor> factors = new ArrayList<Factor>();
		boolean isValue;
		double value;
		boolean positive;
		
		public Addent (String addent){
			this.isValue=isValue(addent);
			if(this.isValue)
				this.value=CalcFkts.calculate(addent);
			int brackets=0;
			positive=addent.charAt(0)=='+';
			addent=addent.substring(1);
			if (addent.charAt(0)!='*' & addent.charAt(0)!='/')
				addent="*"+addent;
			for (int i=1; i<addent.length();i++){
				if (addent.charAt(i)=='(') brackets++;
				if (addent.charAt(i)==')') brackets--;
				if ((addent.charAt(i)=='*' | addent.charAt(i)=='/') & brackets==0){
					factors.add(new Factor(addent.substring(0, i)));
					addent=addent.substring(i);
					i=0;
					brackets=0;
				}
			}
			factors.add(new Factor(addent));
		}
		
		public double calculate(){
			if (this.isValue)
				return this.value;
			double result=1.0;
			for (Factor f:factors)
				result*=f.calculate();
			if (!positive)
				result*=-1;
			return result;
		}
		
		public ArrayList<Double> getDiscontinuities(double startX, double endX, double precision){
			ArrayList<Double> result = new ArrayList<Double>();
			for (Factor f:factors){
				ArrayList<Double> inFa = f.getDiscontinuities(startX, endX, precision);
				for (double d:inFa)
					result.add(d);
			}
			return result;
		}
	}
	private class Factor{
		Function[] function= new Function[2];
		String factor;
		double value;
		boolean divisor;
		int kind;
		final int VALUE = 0;
		final int X = 1;
		final int SQRT = 2;
		final int SIN = 3;
		final int COS = 4;
		final int TAN = 5;
		final int LN = 6;
		final int EXP = 7;
		final int ASIN = 13;
		final int ACOS = 14;
		final int ATAN = 15;
		final int SINH = 16;
		final int COSH = 17;
		final int TANH = 18;
		final int ASINH = 19;
		final int ACOSH = 20;
		final int ATANH = 21;
		final int ABS = 22;
		final int LOG = 23;
		final int BRACKETS = 8;
		final int Y = 9;
		final int A = 10;
		final int B = 11;
		final int C = 12;
		
		public Factor (String factor){
			divisor=factor.charAt(0)=='/';
			factor=factor.substring(1);
			this.factor=factor;
			
			kind=-1;
			
			//check for exponents....
			int brackets=0;
			for (int i=0;i<factor.length();i++){
				if (factor.charAt(i)=='(')		//ignores anything inside brackets
					brackets++;
				if (factor.charAt(i)==')')
					brackets--;
				if (brackets==0 & factor.charAt(i)=='^' & kind==-1){
					kind=EXP;
					function[0]= new Function(factor.substring(0, i),a,b,c);
					function[1]= new Function(factor.substring(i+1),a,b,c);
					break;
				}
			}
			
			//check for brackets
			if(factor.charAt(0)=='(' & kind==-1){
				kind=BRACKETS;
				function[0]= new Function(factor.substring(1,factor.length()-1),a,b,c);
			}
			
			//check for functions
			if(factor.length()>4){
				if (factor.substring(0,4).equalsIgnoreCase("sin(") & kind==-1){
					kind=SIN;
					function[0]=new Function(factor.substring(4,factor.length()-1),a,b,c);
				}
				if (factor.substring(0,4).equalsIgnoreCase("cos(") & kind==-1){
					kind=COS;
					function[0]=new Function(factor.substring(4,factor.length()-1),a,b,c);
				}
				if (factor.substring(0,4).equalsIgnoreCase("tan(") & kind==-1){
					kind=TAN;
					function[0]=new Function(factor.substring(4,factor.length()-1),a,b,c);
				}
				if (factor.substring(0,3).equalsIgnoreCase("ln(") & kind==-1){
					kind=LN;
					function[0]=new Function(factor.substring(3,factor.length()-1),a,b,c);
				}
				if (factor.length()>6 && factor.substring(0,5).equalsIgnoreCase("sqrt(") & kind==-1){
					kind=SQRT;
					function[0]=new Function(factor.substring(5,factor.length()-1),a,b,c);
				}
				if (factor.length()>6 && factor.substring(0,5).equalsIgnoreCase("asin(") & kind==-1){
					kind=ASIN;
					function[0]=new Function(factor.substring(5,factor.length()-1),a,b,c);
				}
				if (factor.length()>6 && factor.substring(0,5).equalsIgnoreCase("acos(") & kind==-1){
					kind=ACOS;
					function[0]=new Function(factor.substring(5,factor.length()-1),a,b,c);
				}
				if (factor.length()>6 && factor.substring(0,5).equalsIgnoreCase("atan(") & kind==-1){
					kind=ATAN;
					function[0]=new Function(factor.substring(5,factor.length()-1),a,b,c);
				}
				if (factor.length()>6 && factor.substring(0,5).equalsIgnoreCase("sinh(") & kind==-1){
					kind=SINH;
					function[0]=new Function(factor.substring(5,factor.length()-1),a,b,c);
				}
				if (factor.length()>6 && factor.substring(0,5).equalsIgnoreCase("cosh(") & kind==-1){
					kind=COSH;
					function[0]=new Function(factor.substring(5,factor.length()-1),a,b,c);
				}
				if (factor.length()>6 && factor.substring(0,5).equalsIgnoreCase("tanh(") & kind==-1){
					kind=TANH;
					function[0]=new Function(factor.substring(5,factor.length()-1),a,b,c);
				}
				if (factor.length()>7 && factor.substring(0,6).equalsIgnoreCase("asinh(") & kind==-1){
					kind=ASINH;
					function[0]=new Function(factor.substring(6,factor.length()-1),a,b,c);
				}
				if (factor.length()>7 && factor.substring(0,6).equalsIgnoreCase("acosh(") & kind==-1){
					kind=ACOSH;
					function[0]=new Function(factor.substring(6,factor.length()-1),a,b,c);
				}
				if (factor.length()>7 && factor.substring(0,6).equalsIgnoreCase("atanh(") & kind==-1){
					kind=ATANH;
					function[0]=new Function(factor.substring(6,factor.length()-1),a,b,c);
				}
				if (factor.length()>5 && factor.substring(0,4).equalsIgnoreCase("abs(") & kind==-1){
					kind=ABS;
					function[0]=new Function(factor.substring(4,factor.length()-1),a,b,c);
				}
				if (factor.length()>5 && factor.substring(0,4).equalsIgnoreCase("log(") & kind==-1){
					kind=LOG;
					function[0]=new Function(factor.substring(4,factor.length()-1),a,b,c);
				}
			}
			
			//set variables and constants
			if (factor.equalsIgnoreCase("x") & kind==-1)
				kind=X;
			if (factor.equalsIgnoreCase("y") & kind==-1)
				kind=Y;
			if (factor.equalsIgnoreCase("a") & kind==-1){
				kind=A;
			}
			if (factor.equalsIgnoreCase("b") && kind==-1)
				kind=B;
			if (factor.equalsIgnoreCase("c") && kind==-1)
				kind=C;
			if ((factor.equalsIgnoreCase("PI")|factor.equalsIgnoreCase("\u03C0")) && kind==-1){
				kind=VALUE;
				value = 3.14159265;
			}
			if (factor.equalsIgnoreCase("e") && kind==-1){
				kind=VALUE;
				value = 2.71828183;
			}
			
			//set numbers
			  //doubles
			for (int i=0;i<factor.length();i++)
				if ((factor.charAt(i)==','|factor.charAt(i)=='.') & kind==-1){
					kind=VALUE;
					value = CalcFkts.calculate(factor.substring(0,i))+CalcFkts.calculate(factor.substring(i+1))/(Math.pow(10, factor.length()-1-i));
				}
			  //integers
			try {  
				value = 1.0*Integer.parseInt(factor);
				kind=VALUE;  
			} catch(NumberFormatException e) { }
		}
		
		public double calculate(){
			double result=9999;
			
			switch (kind){
			case VALUE:
				result = value;
				break;
			case X:
				result = x;
				break;
			case Y:
				result = y;
				break;
			case A:
				result = a;
				break;
			case B:
				result = b;
				break;
			case C:
				result = c;
				break;
			case SQRT:
				result = Math.sqrt(function[0].calculate(x,y,a,b,c));
				break;
			case SIN:
				result = Math.sin(function[0].calculate(x,y,a,b,c));
				break;
			case COS:
				result = Math.cos(function[0].calculate(x,y,a,b,c));
				break;
			case TAN:
				result = Math.tan(function[0].calculate(x,y,a,b,c));
				break;
			case LN:
				result = Math.log(function[0].calculate(x,y,a,b,c));
				break;
			case EXP:
				result = Math.pow(function[0].calculate(x,y,a,b,c),function[1].calculate(x,y,a,b,c));
				break;
			case BRACKETS:
				result = function[0].calculate(x,y,a,b,c);
				break;
			case ASIN:
				result = Math.asin(function[0].calculate(x,y,a,b,c));
				break;
			case ACOS:
				result = Math.acos(function[0].calculate(x,y,a,b,c));
				break;
			case ATAN:
				result = Math.atan(function[0].calculate(x,y,a,b,c));
				break;
			case SINH:
				result = Math.sinh(function[0].calculate(x,y,a,b,c));
				break;
			case COSH:
				result = Math.cosh(function[0].calculate(x,y,a,b,c));
				break;
			case TANH:
				result = Math.tanh(function[0].calculate(x,y,a,b,c));
				break;
			case ASINH:
				result = CalcFkts.asinh(function[0].calculate(x,y,a,b,c));
				break;
			case ACOSH:
				result = CalcFkts.acosh(function[0].calculate(x,y,a,b,c));
				break;
			case ATANH:
				result = CalcFkts.atanh(function[0].calculate(x,y,a,b,c));
				break;
			case ABS:
				result = Math.abs(function[0].calculate(x,y,a,b,c));
				break;
			case LOG:
				result = Math.log10(function[0].calculate(x,y,a,b,c));
				break;
			}
			
			if (divisor)
				return 1/result;
			else
				return result;
		}
		
		public ArrayList<Double> getDiscontinuities(double startX, double endX, double precision){
			ArrayList<Double> result = new ArrayList<Double>();
			ArrayList<Point> points;
			if (divisor){
				Function f = new Function(factor,a,b,c);
				points = PointMaker.getRoots(f, startX, endX, precision);
				for (Point p:points)
					result.add(p.getX());
			}
			switch (kind){
			//case SQRT:
				//points = PointMaker.getRoots(function[0], startX, endX, precision);
				//for (Point p:points)
					//result.add(p.getX());
				//break;
			case LN:
				points = PointMaker.getRoots(function[0], startX, endX, precision);
				for (Point p:points)
					result.add(p.getX());
				break;
			case TAN:
				Function f = new Function("sin("+function[0].getString()+")^2-1",a,b,c);
				points = PointMaker.getRoots(f, startX, endX, precision);
				for (Point p:points)
					result.add(p.getX());
				break;
			case BRACKETS:
				result.addAll(function[0].getDiscontinuities(startX, endX, precision));
				break;
			}
			for (Function f:function)
				if (f!=null)
					result.addAll(f.getDiscontinuities(startX, endX, precision));
			return result;
		}
	}
	@Override
	public String toString() {
		return super.toString() + ": " + string;
	}
}


