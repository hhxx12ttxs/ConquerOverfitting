package SYSC4005Gui;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JFrame;


public class Topology1 {
public final static int SERVER_IDLE = -1;
	
	private int N = 5;
	private int serverStates[];
	private double probability[];
	private double lambdas[];
	private int[][] queueLength;
	private int timeSlotCount;
	private int currentTimeSlot = 0;
	private int Policy=0;
	private boolean[][] queueIsconnected;
	private int iterations;
	private kFrame kfrm;
	private double num1, num2, runRan=-1;
	private boolean testUni=false;

	
	
	/**
	 * Construct the Simulation System.
	 * Default Repo on bitbucket  : https://bitbucket.org/nbhasin/sysc-4005-project-simulator
	 */
	public Topology1(int timeSlotCount, double probability[], double lambdas[], int policy, int iterations, kFrame kf){
		assert(N == probability.length);
		
		this.timeSlotCount = timeSlotCount;		
		this.probability = probability;
		this.lambdas = lambdas;
		//this.policy = policy;
		//this.policy.setSimulationSystem(this);
		Policy = policy;
		this.iterations = iterations;
		kfrm = kf;
		serverStates = new int[timeSlotCount];
		queueLength = new int[N][timeSlotCount];		
		queueIsconnected = new boolean[N][timeSlotCount];
	}
	
	public Topology1(boolean tesUNIF, double one, double two, int timeSlotCount, double probability[], double lambdas[], int policy, int iterations, kFrame kf) {//AbstractPolicy1 policy, int iterations) {
		assert(N == probability.length);
		testUni = tesUNIF;
		this.timeSlotCount = timeSlotCount;		
		this.probability = probability;
		this.lambdas = lambdas;
		
		Policy = policy;
		this.iterations = iterations;
		nextNext(one,two);
		kfrm = kf;
		serverStates = new int[timeSlotCount];
		queueLength = new int[N][timeSlotCount];		
		queueIsconnected = new boolean[N][timeSlotCount];
	}
	
	
	

	public int getN() {
		return N;
	}


	private double getProbability(int n) {
		return probability[n];
	}
	

	public boolean isConnected(int n, int t) {
		return queueIsconnected[n][t];
	}
	

	public void setServerState(int t, int state) {
		this.serverStates[t] = state;		
		//System.out.println("Server state at " + t + " is " + state);
	}



	public boolean isEmpty(int n, int t) {
		if (t < 0) return true;
		return getQueueLength(n, t) == 0;
	}

	private void simulateSystem() {
		for (int t = 0; t < timeSlotCount; t++) {
			advanceTimeSlot();
		}
	}
	

	private void advanceTimeSlot() {
		assert(currentTimeSlot < timeSlotCount);
		
		// calculate Cn
		for (int n = 0; n < N; n++) {
			queueIsconnected[n][currentTimeSlot] = BernoulliGeneratorNext(getProbability(n))==1;  //new BernoulliGenerator(stream, getProbability(n)).next() == 1;			
		}		
		
		if(Policy == 1)
		{
			allocateServerRR(currentTimeSlot);
		}
		if(Policy == 2)
		{
			allocateServerLCQ(currentTimeSlot, testUni);
		}
		if(Policy == 3)
		{
			allocateServerRAN(currentTimeSlot, testUni);
		}
		
		
		for (int n = 0; n < N; n++) {
			int Xn = 0;		
			int Hn = 0;
			if (currentTimeSlot > 0) {
				Xn = queueLength[n][currentTimeSlot - 1];
				
				if (serverStates[currentTimeSlot] == n && getQueueLength(n, currentTimeSlot - 1) > 0) {
					Hn = 1;
				}
			}	
			
			int An = BernoulliGeneratorNext(lambdas[n]); //new BernoulliGenerator(stream, lambdas[n]).next();
			//debugTotalAn += An;
			
			// equation found in section 2.1 
			queueLength[n][currentTimeSlot] = Xn - Hn + An;
		}			
		
			currentTimeSlot++;
	}



	public int getQueueLength(int n, int t) {
		return queueLength[n][t];
	}
	

	private double getAverageQueueOccupancy() {
		double total = 0;
		for (int n = 0; n < N; n++) {
			for (int t = 0; t < timeSlotCount; t++)
			total += queueLength[n][t];
		}		
		return total / (N * timeSlotCount);
	}



	private void reset() 
	{
		currentTimeSlot = 0;	
	}
	

	public void runAndPrintToFile(String fileName) {
		double totals[] = new double[iterations];	
		double total = 0;
		for (int i = 0; i < iterations; i++ ) {
			reset();
			simulateSystem();
			totals[i] = getAverageQueueOccupancy();			
			System.out.print(totals[i]+",");
			total += totals[i];
		}		
		
		double mean = total / iterations;
		double sampleDeviation = 0;
		for (int i = 0; i < iterations; i++) {
			sampleDeviation += Math.pow(totals[i] - mean, 2);
		}
		
		sampleDeviation /= iterations - 1;
		sampleDeviation = Math.sqrt(sampleDeviation);		
		
		// 95% CI
		double interval = sampleDeviation / Math.sqrt(iterations) * 1.96;
		
		double lower = mean - interval;
		double upper = mean + interval;
		if(kfrm.checkboxTicked())
		{
		kfrm.getTF14().append(""+mean+",");
		kfrm.getTF15().append(""+upper+",");
		kfrm.getTF16().append(""+lower+",");
		}
		else
		{
			kfrm.getTF14().setText(""+mean);
			kfrm.getTF15().setText(""+upper);
			kfrm.getTF16().setText(""+lower);
		}

	}
	
	public int BernoulliGeneratorNext(double d) {

		if(testUni)
		{
			 if (nxtnxt() < d) {
					return 1;
				}
		}
		else if (Math.random() < d) {
			return 1;
		}
		return 0;
	}
	
	
	int currentQueue = 0;
	
	public void allocateServerRR(int t) {
		
		if (!isConnected(currentQueue, t) || isEmpty(currentQueue, t - 1)) {
			setServerState(t, SERVER_IDLE);
			
		} else {
			setServerState(t, currentQueue);
		}
		
		
		currentQueue++;
		if (currentQueue == getN()) {
			currentQueue = 0;
		}		
	}
	
	public void allocateServerRAN(int t, boolean tesUniform) {
		ArrayList<Integer> connectedQueues = new ArrayList<Integer>();
		for (int n = 0; n < getN(); n++) {
			if (isConnected(n, t)  && !isEmpty(n, t - 1)) {
				connectedQueues.add(n);
			}
		}		
		
		int count = connectedQueues.size();
		if (count < 1) {
			setServerState(t, SERVER_IDLE);
			return;
		}
		if(tesUniform)
		{
			int n = (int) Math.floor(nxtnxt()* count); 
			System.out.println("------"+n);
			setServerState(t, connectedQueues.get(n));		
			
		}
		else
		{
			int n = (int) Math.floor(Math.random()* count); //system.getRandomStream().next() * count);
		
			setServerState(t, connectedQueues.get(n));		
		}
	
	}
	
	
	public void allocateServerLCQ(int t, boolean tesUniform) {
		ArrayList<Integer> longestQueues = new ArrayList<Integer>();
		
		int longestLength = 0; 
		for (int n = 0; n < getN(); n++ ) {
			if (isConnected(n, t) && !isEmpty(n, t - 1)) {
				int length = getQueueLength(n, t - 1);
				if (length > longestLength) {
					longestQueues.clear();
					longestQueues.add(n);
					longestLength = length;
				} else if (length == longestLength) {
					longestQueues.add(n);
				}
			}
		}
		
		if (longestLength == 0) {
			setServerState(t, SERVER_IDLE);
		} else {
			if (longestQueues.size() == 1) {
				setServerState(t, longestQueues.get(0));
			} else {
				if(tesUniform)
				{
					int n = (int) Math.floor(nxtnxt() * longestQueues.size());
					setServerState(t, longestQueues.get(n));	
				}
				else
				{
					int n = (int) Math.floor(Math.random() * longestQueues.size());//system.getRandomStream().next() * longestQueues.size());
					setServerState(t, longestQueues.get(n));	
				}
			}
		}		
	}
	
	//uniform 
	public void nextNext(double num1, double num2){
		this.num1=num1;
		this.num2=num2;
		runRan=-1;
		
	}
	

	public double nxtnxt()
	{
		if(runRan==-1)
		{
			runRan = Math.random();
		}
		else
		{
			runRan= (runRan+calculateV())%1;
		}
		return runRan;
	}
	

	private double calculateV(){
		return (num1 + num2) * Math.random() + num1;
	}
}

