import java.util.ArrayList;
import java.util.Random;

public class firstTopology {
	static ArrayList<SimQueue<Double>> queues = new ArrayList<SimQueue<Double>>();
	public final static int CYCLES = 50;
	private static final int SAMPLE = 50; 
	static int roundRobinCounter = 0;
	private static double LAMBDA = 0.02;

	public static void main(String args[]) {
		// In this loop we initialize our queues
		for (int i = 0; i < 5; i++) {
			System.out.println("Queue " + (i+1) + " Created");
			SimQueue<Double> temp = new SimQueue<Double>();
			queues.add(temp);
		}

		System.out.println("------------------------------------");
		System.out.println("Begin Simulation");
		System.out.println("------------------------------------");

		// Core simulation loop
		// Every iteration in this loop is a clock cycle
		for (int i = 0; i < CYCLES; i++) {
			System.out.println("\n--CYCLE "+(i+1)+"--\n");
			// Select a queue to service based on length and connectivity
			int queueSelection = lcqPolicy();

			//Successful Selection - Servicing of a queue
			if (queueSelection >=0 &&(!queues.get(queueSelection).isEmpty()) && queues.get(queueSelection).connected) {
				queues.get(queueSelection).removeLast();
				System.out.println("Queue " + (queueSelection+1) + " Serviced");

			}
			
			//Unsuccessful Selection- Idle Time
			else {
				System.out.println("Idle Time!");
			}

			// Arrivals into each queue based on probability
			
			generateU(0.5,0.5);
			for(int k =0; k< SAMPLE; k+=5){
				if (getBernoulli(k,LAMBDA)) 
					{queues.get(0).add(new Double(1)); System.out.println("Queue 1 Arrival");}
				if (getBernoulli(k+1,LAMBDA)) 
					{queues.get(1).add(new Double(1)); System.out.println("Queue 2 Arrival");}
				if (getBernoulli(k+2,LAMBDA)) 
				{queues.get(2).add(new Double(1)); System.out.println("Queue 3 Arrival");}
				if (getBernoulli(k+3,LAMBDA)) 
					{queues.get(3).add(new Double(1)); System.out.println("Queue 4 Arrival");}
				if (getBernoulli(k+4,LAMBDA)) 
					{queues.get(4).add(new Double(1)); System.out.println("Queue 5 Arrival");}
			}
			
			// Connectivity for each queue based one probability
			generateU(0.5,0.5);
			resetConnections();
			for(int k =0; k< SAMPLE; k+=5){
			if (getBernoulli(k,0.02)) {queues.get(0).connected=true; System.out.println("Queue 1 Connected");}
			if (getBernoulli(k+1,0.02)) {queues.get(1).connected=true; System.out.println("Queue 2 Connected");}
			if (getBernoulli(k+2,0.02)) {queues.get(2).connected=true; System.out.println("Queue 3 Connected");}
			if (getBernoulli(k+3,0.02)) {queues.get(3).connected=true; System.out.println("Queue 4 Connected");}
			if (getBernoulli(k+4,0.02)) {queues.get(4).connected=true; System.out.println("Queue 5 Connected");}
			}

		}//END SIMULATION LOOP
	}

	public static double[] array;
	
	public static void generateU( double a, double b){

        double v = Math.random() *(a+b) -b;
        double u = Math.random();
        array = new double[SAMPLE]; 
        array[0]= u;
        for(int i =1; i< SAMPLE ; i++){
                 v = Math.random() *(a+b) -b;
                
                array[i]= (array[i-1]+v)-Math.floor(array[i-1]+v);  
        }
	}

	public static boolean getBernoulli(int i, double lambda){
                return(array[i]<lambda);
	}
	
	public static void resetConnections(){
		for (SimQueue<Double> q: queues){
			q.connected=false;
		}
	}
	
	public static int randomizedPolicy() {
		ArrayList<Integer> possibleChoices = new ArrayList<Integer>();
		for (int i = 0;i< queues.size();i++){
			if ((!queues.get(i).isEmpty()) && queues.get(i).connected) possibleChoices.add(i); //If queue is not empty, and is connected, add to possible choices
		}
		int randomQueue = 0 + (int) (Math.random() * ((possibleChoices.size()-1 - 0) + 1)); //Select a random element in the possible choices
		
		if (possibleChoices.size()>0) return possibleChoices.get(randomQueue);
		else return -1; //No possible choices
	}

	public static int roundRobinPolicy() {
		if (roundRobinCounter > 4) roundRobinCounter = 0;
		int current = roundRobinCounter;
		roundRobinCounter++;
		return current;
	}

	//Longest non-empty queue
	public static int lcqPolicy() {
		int largestQueue = 0;
		for (int i = 0; i < queues.size(); i++) {
			if (queues.get(i).size() > largestQueue && queues.get(i).connected)
				largestQueue = i;
		}
		return largestQueue;
	}
}
