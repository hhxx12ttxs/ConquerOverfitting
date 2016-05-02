package gradDesc;

import java.util.Date;
import java.util.LinkedList;

public class GradientDescent {
	
	private static LinkedList<Double> givenTimes;
	private static LinkedList<Double> givenValues;
	
	//Enable searchPrint to print the constants and the function f as they progress through the algorithm
	private static boolean searchPrint = false;
	
	//=================================================================================================//
	/*Functions:
	 * Nelson-seigel: g(a,b,c,d,t) = a + (b + c)(d/t)(1-e^(-t/d)) - (c*e^(-t/d))
	 * 
	 * Objective function: f(a,b,c,d) = summation as i goes from 0 to n of (g(a,b,c,d,Vi)-Ui)^2, 
	 * 											where Vi is an array of given times at index i,
	 * 										 and Ui is an array of given values at index i.
	 */
	//=================================================================================================//
	
//	public static void main(String [ ] args){
//		LinkedList<Double> times = new LinkedList<Double>();
//		times.add(1.0);
//		times.add(2.0);
//		times.add(3.0);
//		times.add(5.0);
//		times.add(7.0);
//		times.add(10.0);
//		LinkedList<Double> values = new LinkedList<Double>();
//		values.add(2.05);
//		values.add(1.98);
//		values.add(2.23);
//		values.add(2.80);
//		values.add(3.24);
//		values.add(3.77);
//		
//		double parameters[] = {1.0,1.0,1.0,1.0};
//		double constants[] = new double[4];
//		constants = minimizeF(values, times, parameters, 0.002);
//	}

	public static double g(double a, double b, double c, double d, double t){
		return a + (b+c)*(d/t)*(1-Math.exp(-t/d)) - (c*Math.exp(-t/d));
	}
	public static double partialOf_Ga(double a, double b, double c, double d, double t){
		return 1;
	}
	public static double partialOf_Gb(double a, double b, double c, double d, double t){
		return (d/t)*(1-Math.exp(-t/d));
	}
	public static double partialOf_Gc(double a, double b, double c, double d, double t){
		return (d/t)*(1-Math.exp(-t/d)) - Math.exp(-t/d);
	}
	public static double partialOf_Gd(double a, double b, double c, double d, double t){
		return (Math.exp(-t/d)*( (Math.pow(d, 2)*(b+c)*(Math.exp(t/d)-1))-(t*((b*d) + c*(d+t))) ))/(Math.pow(d, 2)*t);
	}
	public static double partialOfGStep(double initialConstants[], double t, double step){
		double a = initialConstants[0];
		double b = initialConstants[1];
		double c = initialConstants[2];
		double d = initialConstants[3];
		LinkedList<Double> fGradient = gradientOfF(initialConstants);
		double q = fGradient.get(0);
		double w = fGradient.get(1);
		double r = fGradient.get(2);
		double u = fGradient.get(3);
		double part1 = q;
		double part2 = (u*(1-Math.exp(-t/(d+step*u)))*(b + c + (step*(r + w)))/t) - (u*Math.exp(-t/(d+step*u))*(b + c + (step*(r + w)))/(d+(step*u))) + ((r + w)*(d+(step*u))*(1-Math.exp(-t/(d+step*u)))/t);
		double part3 = (c*t*u*Math.exp(-t/(d+step*u)))/Math.pow((d + (step*u)),2);
		
		return part1 + part2 + part3;
	}
	public static LinkedList<Double> gradientOfG(double a, double b, double c, double d, double t){
		LinkedList<Double> gradient = new LinkedList<Double>();
		gradient.add(0, 1.0);
		gradient.add(1, (d/t)*(1-Math.exp(-t/d)));
		gradient.add(2, (d/t)*(1-Math.exp(-t/d)) - Math.exp(-t/d));
		gradient.add(3, (Math.exp(-t/d)*( (Math.pow(d, 2)*(b+c)*(Math.exp(t/d)-1))-(t*((b*d) + c*(d+t))) ))/(Math.pow(d, 2)*t));
		return gradient;
	}
	public static double f(double initialConstants[]){
		double a = initialConstants[0];
		double b = initialConstants[1];
		double c = initialConstants[2];
		double d = initialConstants[3];
		double value = 0;
		for(int i=0;i<getGivenTimes().size();i++){
			value = value + Math.pow(g(a,b,c,d,getGivenTimes().get(i))-getGivenValues().get(i),2);
		}
		return value;
	}
	public static LinkedList<Double> gradientOfF(double initialConstants[]){
		double a = initialConstants[0];
		double b = initialConstants[1];
		double c = initialConstants[2];
		double d = initialConstants[3];
		LinkedList<Double> gradient = new LinkedList<Double>();
		gradient.add(0, 0.0);
		gradient.add(1, 0.0);
		gradient.add(2, 0.0);
		gradient.add(3, 0.0);
		double scalarComponent = 0;
		LinkedList<Double> vectorComponent = new LinkedList<Double>();
		for(int i=0;i<getGivenTimes().size();i++){
			scalarComponent = 2*(g(a,b,c,d,getGivenTimes().get(i))-getGivenValues().get(i));
			vectorComponent = gradientOfG(a,b,c,d,getGivenTimes().get(i));
			gradient.set(0,gradient.get(0)+scalarComponent*vectorComponent.get(0));
			gradient.set(1,gradient.get(1)+scalarComponent*vectorComponent.get(1));
			gradient.set(2,gradient.get(2)+scalarComponent*vectorComponent.get(2));
			gradient.set(3,gradient.get(3)+scalarComponent*vectorComponent.get(3));
		}
		return gradient;
	}
	public static double[] minimizeF(LinkedList givenVals, LinkedList times, double initialConstants[], double epsilon){
		givenValues = givenVals;
		givenTimes = times;
		
		double newConstants[] = {0, 0, 0, 0};
		for(int i=0; i < initialConstants.length; i++){
			newConstants[i] = initialConstants[i];
		}
		double stepSize = .001;
		double j;
		LinkedList<Double> vectorComponent = new LinkedList<Double>();
		while((j = Math.abs(f(newConstants))) > epsilon){
			if(searchPrint){
				System.out.println("[" + newConstants[0] + ", " + newConstants[1] + ", " + newConstants[2] + ", " + newConstants[3] + "]");
				System.out.println("f = " + j + "\n");
			}
			vectorComponent = gradientOfF(newConstants);
			//stepSize = getStepSize(initialConstants, vectorComponent, stepSize);
			for(int i=0; i < newConstants.length; i++){
				newConstants[i] -= stepSize*vectorComponent.get(i);
			}
		}
		
		return newConstants;
	}
	public static double partialOfFStep(double initialConstants[], double step){
		double a = initialConstants[0];
		double b = initialConstants[1];
		double c = initialConstants[2];
		double d = initialConstants[3];
		double t = 0;
		double sum = 0;
		for(int i=0;i<getGivenTimes().size();i++){
			t = getGivenTimes().get(i)-getGivenValues().get(i);
			sum += 2*(g(a,b,c,d,t))*partialOfGStep(initialConstants,t,step);
		}
		return sum;
	}
	public static double getStepSize(double initialConstants[], LinkedList<Double> fGradient, double initialStep){
		double step = .001;
		double initialF = f(initialConstants);
		double stepConstants[] = {0,0,0,0};
		LinkedList<Double> p = productOfDoubleAndVector(-1,getUnitVector(fGradient));
		for(int i=0; i<stepConstants.length; i++){
			stepConstants[i] = initialConstants[i] + (step*p.get(i));
		}
		double c1 = .001;
		double c2 = .9;
		double fStep = 0;
		int counter = 0;
		//direction, p, squared
		LinkedList<Double> product = productOfVectorAndVector(p, fGradient);
		boolean done = false;
		while(!done){
			fStep = f(stepConstants);
			LinkedList<Double> gradientOfFStep = gradientOfF(stepConstants);
			for(int i=0; i<stepConstants.length; i++){
				if((fStep <= initialF+(c1*step*product.get(i))) && ((p.get(i)*gradientOfFStep.get(i)) >= c2*product.get(i))){
					done = true;
					break;
				}
			}
			if(!done)
				step -= .001*partialOfFStep(initialConstants, step);
			for(int i=0; i<stepConstants.length; i++){
				stepConstants[i] = initialConstants[i] + (step*p.get(i));
			}
		}

		return step;
	}
	public static LinkedList<Double> productOfDoubleAndVector(double val, LinkedList<Double> vector){
		LinkedList<Double> product = new LinkedList<Double>();
		for(double element : vector){
			product.add(element*val);
		}
		return product;
	}
	public static LinkedList<Double> sumOfDoubleAndVector(double val, LinkedList<Double> vector){
		LinkedList<Double> sum = new LinkedList<Double>();
		for(double element : vector){
			sum.add(element+val);
		}
		return sum;
	}
	public static LinkedList<Double> productOfVectorAndVector(LinkedList<Double> vector1, LinkedList<Double> vector2){
		LinkedList<Double> product = new LinkedList<Double>();
		for(int i=0; i<vector1.size(); i++){
			product.add(vector1.get(i)*vector2.get(i));
		}
		return product;
	}
	public static LinkedList<Double> sumOfVectorAndVector(LinkedList<Double> vector1, LinkedList<Double> vector2){
		LinkedList<Double> sum = new LinkedList<Double>();
		for(int i=0; i<vector1.size(); i++){
			sum.add(vector1.get(i)+vector2.get(i));
		}
		return sum;
	}
	public static LinkedList<Double> getUnitVector(LinkedList<Double> vector){
		LinkedList<Double> result = new LinkedList<Double>();
		double divisor = 0;
		double sum = 0;
		for(double element : vector){
			sum += Math.pow(element, 2);
		}
		divisor = Math.sqrt(sum);
		for(int i=0; i<vector.size(); i++){
			result.add(vector.get(i)/divisor);
		}
		return result;
	}
	
	public static void toggleSearchPrint(boolean val){
		searchPrint = val;
	}
	public static LinkedList<Double> getGivenTimes() {
		return givenTimes;
	}
	public static void setGivenTimes(LinkedList<Double> times) {
		givenTimes = times;
	}
	public static LinkedList<Double> getGivenValues() {
		return givenValues;
	}
	public static void setGivenValues(LinkedList<Double> values) {
		givenValues = values;
	}
}

