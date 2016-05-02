package prog03;
import prog03.UserInterface;
import prog03.GUI;

/**
 *
 * @author vjm
 */
public class Main {
  /** Use this variable to store the result of each call to fib. */
  public static double fibn;
  /**This variable is used to store the global fibConst to be used to estimate the next fibConst **/
  public static double fibConst;
  
  
  /** Determine the time in milliseconds it takes to calculate the
      n'th Fibonacci number.
      @param fib an object that implements the Fib interface
      @param n the index of the Fibonacci number to calculate
      @return the average time per call
  */
  public static double callTime (Fib fib, int n) {
    // Get the current time in milliseconds.  This is a static
    // method in the System class.  Actually, it is the time in
    // milliseconds since midnight, January 1, 1970.  What type
    // should you use to store the current time?  Why?
    long start = System.currentTimeMillis();

    // Calculate the n'th Fibonacci number.  Store the
    // result in fibn.
    fibn = fib.fib(n);

    // Get the current time in milliseconds.
    long stop = System.currentTimeMillis();

    // Return the difference between the end time and the
    // start time.
    return(stop-start);
  }

  /** Determine the average time in milliseconds it takes to calculate
      the n'th Fibonacci number.
      @param fib an object that implements the Fib interface
      @param n the index of the Fibonacci number to calculate
      @param ncalls the number of calls to average over
      @return the average time per call
  */
  public static double averageTime (Fib fib, int n, long ncalls) {
    double total = 0;

    // Add up the total call time for ncalls calls.  Use long for the
    // counter.
    for (long counter = 0; counter < ncalls; counter++) {
    	total = total + callTime(fib,n);
    }
      // Add the time for the next call to the total.

    // Return the average time.
    return (total/ncalls);	
  }

  /** Determine the time in milliseconds it takes to to calculate
      the n'th Fibonacci number ACCURATE TO THREE SIGNIFICANT FIGURES.
      @param fib an object that implements the Fib interface
      @param n the index of the Fibonacci number to calculate
      @return the time it it takes to compute the n'th Fibonacci number
  */
  public static double accurateTime (Fib fib, int n) {
    // Since the clock is only accurate to the millisecond, we
    // need to use a value of ncalls such that the total time is a
    // second.  First we need to figure that value of ncalls.

    // Starting with ncalls=1, calculate the total time, which is
    // ncalls times the average time.  Use the method
    // averageTime(fib,n,ncalls) method to get the average time.  Keep
    // multiplying ncalls times 2 and recalculating the average time
    // until the total time is more than a second.
    long ncalls = 1;
    double time = 0;
    // Put your loop here. Average times ncalls equals total run time
    while((time*ncalls) < 1000) {
    	time = averageTime(fib, n, ncalls);
    	ncalls = ncalls*2;
    }
    // Return the average time for that value of ncalls.  As a
    // test, print out ncalls times this average time to make sure
    // it is more than a second.
    //System.out.println("Debug Ncalls: "+ncalls+" Run Time: "+time +" Ncalls*times: "+ (ncalls * time));
    return time;
  }
  /** Controls the GUI and calls DoExperiments1(Fib fib) with each different fib type
   * Also resets the global fibConst variable
  @param n/a
  @return n/a
*/
  static void doExperiments() {
	  //Initialize GUI
	  UserInterface ui = new GUI();
	  //Set buttons
		String[] commands = {
				"Constant Fib",
				"Exponential Fib",
				"Linear Fib",
				"Log Fib",
				"Power Fib",
				"Exit"};

		Fib fib; //initialize a fib 
		boolean exit = true; //exit variable
		while (exit) {
			/** Reset the fibConst so its set to zero when you change fib type **/
			fibConst = 0; 
			int c = ui.getCommand(commands); //waits for user command
			switch (c) {
			case 0:
				fib = new ConstantFib();
				doExperiments1(fib);
				break;
			case 1:
				fib = new ExponentialFib();
				doExperiments1(fib);
				break;
			case 2:
				fib = new LinearFib();
				doExperiments1(fib);
				break;
			case 3: 
				fib = new LogFib();
				doExperiments1(fib);
				break;
			case 4:
				fib = new PowerFib();
				doExperiments1(fib);
				break;
			case 5: 
				exit = false;
				break;
			default:
				exit = false;
				break;
			}
		}
  }
  /** Continuously prompts user for number <n> of Fibbonnacci numbers to find until user enters a white space
   * Calculates: Accurate time, 
   * Local constant (Constant for this interation), 
   * Global Constant estimates the Fib constant using previous fib constant info.
  @param fib an object that implements the Fib interface
  @return n/a
*/
  static void doExperiments1 (Fib fib) {
	  UserInterface ui = new GUI(); //initialize GUI

	  String n = null;//string to hold user input
	  int num = 0; //int to hold (int)n
	  double time = 0; //calculated run time

	  boolean endLoop = true; //boolean to end loop on white space entered or null
	  while(endLoop) {
		  //use a GUI to get user input
		n = ui.getInfo("Enter number <n> of Fibbonnacci numbers you wish to find: ");
		//check if a white space or cancel is pressed
	  	if(Character.isWhitespace(n.charAt(0))||(n == null)||(n==" ")) {
	  		endLoop = false;
	  		break;
	  	}
	  	//string to int
	  	num = Integer.parseInt(n);
	  	//calculate accurate time 
	  	time = accurateTime(fib, num);
	    //System.out.println("Accurate Time n1 "+ num+ " Accurate time: "+time);
	    // Calculate constant:  time = constant times O(n).
	    double c = time / fib.o(num);
	   // System.out.println("c " + c);
	    double estimatedTime = 0.0;
//If fibConstant is zero set the initial value of c to the first iteration.
	    if(fibConst == 0) {
	    	fibConst = c; //accurate time/
	    	estimatedTime = c*fib.o(num);
	    } else {
// Else run an estimate of the run time using the fib constant from previous run
	    	estimatedTime = fibConst * fib.o(num);
	    }
	    ui.sendMessage(num+" fib: "+fib.fib(num)+
	    		"\nAccurate Time for find "+ num+ " fibbonnacci numbers: "+time+
	    		"\nEstimate time for +"+num+" fib numbers: "+estimatedTime+
	    		"\nConstant for this iteration: "+c);
	    //set global fibConst to c to use in the next iteration.
	    fibConst = c;
	  }
  }

  static void labExperiments () {
    // Create (Exponential time) Fib object and test it.
    //Fib efib = new LogFib();
    //Fib efib = new LinearFib();
    //Fib efib = new ExponentialFib();
	Fib efib = new ConstantFib();  
    System.out.println(efib);
    for (int i = 0; i < 11; i++)
      System.out.println(i + " " + efib.fib(i));
    
    // Determine running time for n1 = 20 and print it out.
    int n1 = 20;
    //double time1 = averageTime(efib, n1, 1000);
    //about 13000 times should be enough to get the one second run time
    double time1 = averageTime(efib, n1, 13000); 
    System.out.println("Average Time n1 (13000 iterations) " + n1 + " time1 " + time1);
    
    double accTime1 = accurateTime(efib, n1);
    System.out.println("Accurate Time n1 "+ n1+ " AccTime1 "+accTime1);
    
    // Calculate constant:  time = constant times O(n).
    double c = time1 / efib.o(n1);
    System.out.println("c " + c);
    double c4 = accTime1 / efib.o(n1);
    System.out.println("Acc Time: " + c4);
    
    // Estimate running time for n2=30.
    int n2 = 30;
    double time2est = c * efib.o(n2);
    System.out.println("n2 " + n2 + " estimated time " + time2est);
    
    // Calculate actual running time for n2=30.
    double time2 = averageTime(efib, n1, 13000);
    System.out.println("Average time (100 iterations) n2 " + n2 + " average time " + time2);
    
    double acTime2 = accurateTime(efib, n2);
    System.out.println("Accurate time n2 " + n2 + " actual time " + acTime2);
    
    // Calculate constant:  time = constant times O(n).
    double c2 = acTime2 / efib.o(n2); //accurate time/
    System.out.println("c2 " + c2);
    
    // Estimate running time for n2=30.
    int n3 = 100;
    double timeHundred = c2 * efib.o(n3);
    System.out.println("n3 Hundred Iterations " + n2 + " estimated time " + timeHundred);
  }

  /**
   * @param args the command line arguments
   */
  public static void main (String[] args) {
	//Fib efib = new ConstantFib();
    //doExperiments1(efib);
    //labExperiments();
	doExperiments();
  }
}
