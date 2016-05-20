package kretst;

/**
 * 
 * The driver for the <code>ProcessManagementSimulator</code>. See the usage instructions on the command line for more information.
 * 
 * @author Timothy "XBigTK13X" Kretschmer
 *
 */

public class Main {
	
	//Default values of the simulator
	private static ProcessManagementSimulator _simulation;
	private static int _processesCount = 20;
	private static int _contextSwitchCost = 7;
	private static int _timeSlice = 0;
	private static String _scheduleAlgorithm = "FirstComeFirstServe";
	/**
	 * Handles different runtime scenarios: default (as per the assignment), default+preemptive, custom (as per command line args).
	 * 
	 * @param args Command line arguments passed to the application
	 */
	public static void main(String[] args) 
	{
		//We default to the simulation defined in the assignment if no arguments are passed
		if(0 == args.length)
		{
			simulate("FirstComeFirstServe", _timeSlice, _processesCount, _contextSwitchCost);
			simulate("ShortestJobFirst",_timeSlice,_processesCount,_contextSwitchCost);
			simulate("RoundRobin",500,_processesCount,_contextSwitchCost);
			simulate("PreemptivePriority",_timeSlice,_processesCount,_contextSwitchCost);
		}
		else
		{
			if(containsArg("-PART2",args))
			{
				ProcessManagementSimulator.toggleRandomEntryDelay();
			}
			if(containsArg("-tS",args))
			{
				_timeSlice=Integer.parseInt(getArgValue("-tS",args));
			}
			if(containsArg("-N",args))
			{
				_processesCount=Integer.parseInt(getArgValue("-N",args));
			}
			if(containsArg("-tCS",args))
			{
				_contextSwitchCost=Integer.parseInt(getArgValue("-tCS",args));
			}
			if(containsArg("-s",args))
			{
				_scheduleAlgorithm=getArgValue("-tCS",args);
			}
			simulate(_scheduleAlgorithm,_timeSlice,_processesCount,_contextSwitchCost);
		}
	}
	/**
	 * Runs a new simulation based on the input provided.
	 * 
	 * @param scheduleAlgorithm Scheduling method to be used in ordering the process queue
	 * @param timeSlice Amount of time allotted per runtime used for the Round Robin scheme
	 * @param processCount  Number of processes to run in the simulation
	 * @param contextSwitchCost Amount of time required to remove a process and pass a new one to the CPU
	 */
	private static void simulate(String scheduleAlgorithm,int timeSlice,int processCount,int contextSwitchCost)
	{
		System.out.println("\n==========================================================" +
						   "\nSimulating the "+scheduleAlgorithm+" algorithm."+
						   "\n==========================================================");
		_simulation = new ProcessManagementSimulator(scheduleAlgorithm,timeSlice,processCount,contextSwitchCost);
		_simulation.run();
	}
	/**
	 * Determines if a specific command line argument was passed
	 * 
	 * @param s Argument for which to check
	 * @param args All available arguments from the command line
	 * @return Whether or not the command line argument was passed by the user
	 */
	private static boolean containsArg(String s, String[] args)
	{
		for(String arg:args)
		{
			if(s.equalsIgnoreCase(arg))
			{
				return true;
			}
		}
		return false;
	}
	/**
	 * Determines the value associated with numerical command line arugments that were passed
	 * 
	 * @param s Argument for which to check
	 * @param args All available arguments from the command line
	 * @return The value following the argument passed
	 */
	private static String getArgValue(String s,String[] args)
	{
		boolean nextArgIsTheValue = false;
		for(String arg:args)
		{
			if(nextArgIsTheValue)
			{
				if(arg.startsWith("-"))
				{
					argumentFailure();
				}
				return arg;
			}
			if(s.equalsIgnoreCase(arg))
			{
				nextArgIsTheValue = true;
			}
		}
		argumentFailure();
		return null;
	}
	/**
	 * Generates the intended usage of the command line arguments when they are used improperly.
	 */
	private static void argumentFailure()
	{
		System.out.println("Improper usage of command line arguments detected.\nExiting.");
		System.out.println("Usage: java -jar CPUSimulator [-PART2] [-tS _int_] [-N _int_] [-tCS _int_] [-s _string_]");
		System.out.println("\t[-PART2]: Runs the simulation with 25% of processes intially scheduled with 75% entered later during execution.");
		System.out.println("\t[-tS _int_]: Time slice used by the Round Robin scheme. (Default _int_ is 0)");
		System.out.println("\t[-N _int_]: Number of processes to run in the simulation. (Default _int_ is 20)");
		System.out.println("\t[-tCS _int_]: Time required for a context switch. (Default _int_ is 7)");
		System.out.println("\t[-s_int_]: The time slice used by the Round Robin scheme. (Default _int_ is 0)");
		System.exit(-1);
	}
}

