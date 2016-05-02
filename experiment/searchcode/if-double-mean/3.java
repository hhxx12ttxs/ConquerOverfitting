package homework5;

//The ceiling function in Excel 2007 is very buggy, so I use java to implement 
//a ceiling function by myself



public class Simulation_Problem_4 {
	public static void main (String []args){
		simulation();
	}
	
	
	public static void simulation(){
		//p is defined here, it can be changed 
		final double p = 0.3;
		
		double [] X = new double [2000];
		
		double sumMean = 0.0; 
		double sumVariance = 0.0; 
		double std = 0.0; 
		double mean = 0.0; 
		for(int simulation = 0; simulation < 2000; simulation++){
			 double rand = Math.random(); 
			 if(rand <1/(1+p)){
				 X[simulation] = Ceiling(Math.log(rand+rand*p)/(-Math.log(p)));
			 }else{
				 X[simulation] = Ceiling(Math.log(1+p-rand-rand*p)/Math.log(p)-1);
			 }
			 sumMean += X[simulation];
		}
		
		mean = sumMean/2000.0; 
		
		for (int i =0; i < 2000; i++){
			sumVariance += (X[i]-mean)*(X[i]-mean);	
		}
		std = Math.sqrt(sumVariance/2000.0);
		System.out.print("mean value in theory:");
		System.out.println(0.0);
		System.out.print("mean value in simulation:");
		System.out.println(mean);
		System.out.print("standard deviation value in theory:");
		System.out.println(Math.sqrt(2*p)/(1-p));
		System.out.print("standard value in simulation:");
		System.out.println(std);
		
	}
	
	
	public static int Ceiling(double number){
		if(number<=0){
			return (int)(number);
		}else{
			return ((int)(number) +1);	
		}
	}

}


