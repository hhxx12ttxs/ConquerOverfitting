package kretst;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
/**
 * Simulates CPU scheduling
 * 
 * @author Timothy "XBigTK13X" Kretschmer
 */
public class ProcessManagementSimulator {	
	
	private static boolean __randomEntryDelay = false; 
	private ProcessDispatcher _scheduler;
	private Processor _cpu;
	private int _nextPID = 1;
	private Random _rand = new Random();
	private ArrayList<Process> _processedQueue = new ArrayList<Process>(); 
	private String _scheduleAlgorithm;
	private int _processAmount;
	private int _contextSwitchCost;
	
	public ProcessManagementSimulator(String scheduleAlgorithm,int timeSlice,int processAmount,int contextSwitchCost) {
		_scheduleAlgorithm = scheduleAlgorithm;
		_cpu = new Processor(timeSlice);
		_scheduler = new ProcessDispatcher(_scheduleAlgorithm);
		if(Validator.isGreaterThan(processAmount, 0)==true)
		{
			_processAmount=processAmount;
		}
		if(Validator.isGreaterThan(contextSwitchCost, -1))
		{
			_contextSwitchCost = contextSwitchCost;
		}
	}
	
	/**
	 * Allows processes to be added into the queue after clock time zero.
	 * 
	 * @return The new value of whether or not random process delay entry is enabled.
	 */
	public static boolean toggleRandomEntryDelay()
	{
		return __randomEntryDelay=!__randomEntryDelay;
	}
	
	/**
	 * A driver method that unites the actions of the <code>ProcessDispatcher</code> with those of the <code>Processor</code>
	 */
	public void run()
	{
		addProcessesToQueue(_processAmount);
		Process currentProcess;
		currentProcess = _scheduler.popCurrentProcess();
		_cpu.setRunningProcess(currentProcess);
		while(0 < _scheduler.getScheduledQueueSize() || null !=_cpu.getRunningProcess())
		{
			
			currentProcess = _cpu.tick();
			if(currentProcess != null)
			{
				if(-1 == currentProcess.getStartTime())
				{
					currentProcess.setStartTime(_cpu.getClockTime());
				}
				//TODO Remove this debugging statement
				//System.out.println(_scheduler);
				
				//If the process hasn't finished running, then schedule it for another run
				if(0 < currentProcess.getRunTime())
				{
					_scheduler.addProcess(currentProcess,true);
				}
				else
				{
					currentProcess.setFinishTime(_cpu.getClockTime());
					_processedQueue.add(currentProcess);
					System.out.println(_cpu+"Process "+currentProcess.getPID()+" terminated (turnaround time "+currentProcess.getTurnaroundTime()+"ms, wait time "+currentProcess.getWaitTime()+"ms)");
				}
				try
				{
					//Context switching
					_cpu.setRunningProcess(_scheduler.popCurrentProcess());
					_cpu.tick(_contextSwitchCost);
				}
				//If an empty queue is popped, then this exception is thrown
				catch(Exception e)
				{
					//We only want to exit if all the processes in both the queue and the unscheduled queue are empty
					break;
				}
				System.out.println(_cpu+"Context switch (swapped out process "+currentProcess.getPID()+" for process "+_cpu.getRunningProcess().getPID()+")");
				if(-1==currentProcess.getStartTime())
				{
					currentProcess.setStartTime(_cpu.getClockTime());
					System.out.println(_cpu+"Process "+currentProcess.getPID()+" accessed the CPU for the first time (wait time "+currentProcess.getWaitTime()+")");
				}
			}
		}
		printStatistics(_processedQueue);
	}
	
	/**
	 * Passes a set number of new <code>Process</code> instances into the <code>ProcessDispatcher</code>
	 * 
	 * @param amountToAdd Number of processes to add into the scheduling queues.
	 */
	private void addProcessesToQueue(int amountToAdd)
	{
		while(amountToAdd>0)
		{
			int priority = 0;
			if(_scheduleAlgorithm.equalsIgnoreCase("PreemptivePriority"))
			{
				priority = _rand.nextInt(5);
			}
			Process nextProcess = new Process(500+_rand.nextInt(9500),priority,_nextPID++,_cpu.getClockTime());
			_scheduler.addProcess(nextProcess,true);
			System.out.println(_cpu +"Process "+(nextProcess.getPID())+" created (requiring "+nextProcess.getRunTime()+"ms CPU time, priority "+nextProcess.getPriority()+")");
			amountToAdd--;
		}
	}
	
	/**
	 * Prints various statistics about <code>Process</code> run times to System.out.
	 * 
	 * @param finishedProcesses The <code>Process</code> instances that have been run through the <code>Processor</code>.
	 */
	private void printStatistics(ArrayList<Process> finishedProcesses)
	{
		ArrayList<Double> waitTimes = new ArrayList<Double>();
		ArrayList<Double> turnaroundTimes = new ArrayList<Double>();
		for(Process process:finishedProcesses)
		{
			waitTimes.add((double)process.getWaitTime());
		}
		for(Process process:finishedProcesses)
		{
			turnaroundTimes.add((double)process.getTurnaroundTime());
		}
		
		DecimalFormat threeDecimalPlaces = new DecimalFormat("0.000");
		
		System.out.println("Turnaround Time Statistics:\n\tMIN:\t"+threeDecimalPlaces.format(calculateMinimum(turnaroundTimes))+"ms"+
													  "\n\tMAX:\t"+threeDecimalPlaces.format(calculateMaximum(turnaroundTimes))+"ms"+
													  "\n\tAVG:\t"+threeDecimalPlaces.format(calculateAverage(turnaroundTimes))+"ms");
		
		System.out.println("Wait Time Statistics:\n\tMIN:\t"+threeDecimalPlaces.format(calculateMinimum(waitTimes))+"ms"+
				  "\n\tMAX:\t"+threeDecimalPlaces.format(calculateMaximum(waitTimes))+"ms"+
				  "\n\tAVG:\t"+threeDecimalPlaces.format(calculateAverage(waitTimes))+"ms");
	}
	/**
	 * Finds the smallest value within a collection of Doubles.
	 * 
	 * @param numbers A collection containing doubles to check.
	 * @return Smallest value within the collection.
	 */
	private double calculateMinimum(ArrayList<Double> numbers)
	{
		double minimum = Integer.MAX_VALUE;
		for(Double number:numbers)
		{
			if(number < minimum)
				minimum = number;
		}
		return minimum;
	}
	/**
	 * Finds the largest value within a collection of Doubles.
	 * 
	 * @param numbers A collection containing doubles to check.
	 * @return Largest value within the collection.
	 */
	private double calculateMaximum(ArrayList<Double> numbers)
	{
		double maximum = Integer.MIN_VALUE;
		for(Double number:numbers)
		{
			if(number > maximum)
				maximum = number;
		}
		return maximum;
	}
	/**
	 * Calculates the average value of a collection containing Doubles
	 * 
	 * @param numbers A collection containing doubles to process.
	 * @return Calculated average value of the collection.
	 */
	private double calculateAverage(ArrayList<Double> numbers)
	{
		double average = 0;
		for(Double number:numbers)
		{
			average+=number;
		}
		average/=numbers.size();
		return average;
	}
	
}

