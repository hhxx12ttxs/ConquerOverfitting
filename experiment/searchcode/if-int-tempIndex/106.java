package machine;

import instructions.ATmegaProgram;
import instructions.Function;
import instructions.Instr;
import instructions.InstrLabel;
import instructions.RuntimeError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;

import java.io.*;

import machine.functions.Func;
import machine.functions.FuncButtonDown;
import machine.functions.FuncDisplaySlate;
import machine.functions.FuncMalloc;
import machine.functions.FuncSetPix;
import machine.functions.FuncGetPix;
import machine.functions.FuncDelay;
import machine.functions.FuncTone;
import machine.functions.FuncSetAux;

import org.apache.log4j.Logger;

public class MachineState {
	final private static Logger logger = Logger.getLogger(MachineState.class);
	final private String ledGrid[][] = new String[8][8];
	final private String displaySlate[][] = new String[8][8];
	final private Map<Integer, Integer> stack;
	final private Map<Integer, Integer> heap;
	final private Map<Integer, Integer> registers;
	final private ArrayList<Instr> programSpace;
	final private ArrayList<MachineUpdate> updateObj;
	final private Map<String, Integer> functionMapping;
	final private Map<String, Integer> labelMapping;
	final private Map<String, Integer> labelJumps;
	final private Map<String, Func> predefinedFunctions = new HashMap<String, Func>();
	final private String name;
	private int labelJmps = 10;// default value.
	private boolean batch = true;
	private int pc = 0;// set to the beginning of the programSpace.
	private boolean finished = false;
	private int numLoads = 0;
	private int numStores = 0;
	private boolean countinstrs = false;
	
	public boolean isFinished() {
		return finished;
	}

	private int stackPointer = 0x3e3d; // this value is taken from the SPL and
	// SPH from the main.s file.
	private int returnAddress = -1;
	final private SREG statusReg;

    //--------- Fields for implementing the arg_opts file like Meggy.java uses.
    //private File argoptsfile = null;
    private boolean mUsingArgOpts = false;
    private int mMaxCalls = 0;  // Max number of calls to Meggy 
                                // interface before halting
    private boolean mDelaySim = false; // Are we simulating delays?
    private enum Button { B,
        A,
        Up,
        Down,
        Left,
        Right }
    // A list of button press sets.  Each phase has a set.  Phases are
    // occur between delay calls.
    private LinkedList<HashSet<Button>> mButtonPresses 
        = new LinkedList<HashSet<Button>>();
    private int mCurrPhase = 0;

    //--------- End of fields for implementing the arg_opts file


	//use the singleton pattern for this.

	private static MachineState machine = null;

	public static MachineState createMachine(String name) 
	{
		if(machine == null)
		{
			machine = new MachineState(name);
		}
		return machine;
	}

	public static MachineState createMachine(String name, int jmps,
	    File argoptsfile) 
	{
		if(machine == null)
		{
			machine = new MachineState(name,jmps,argoptsfile);
		}
		return machine;		
	}

	public static MachineState createMachine(String name, boolean batch,
	    File argoptsfile, boolean countinstrs)
	{
		if(machine == null)
		{
			machine = new MachineState(name,batch,argoptsfile, countinstrs);
		}
		return machine;		
	}

	public static MachineState createMachine(String name, boolean batch)
	{
		if(machine == null)
		{
			machine = new MachineState(name,batch,null,false);
		}
		return machine;		
	}


	public static void uninitMachine()
	{
		machine = null;
	}


	protected MachineState(String name) {
		// Using a TreeMap instead of a HashMap because after a large number of
		// addresses have been accessed
		// the speed of the HashMap will greatly decrease. The number of
		// registers however is small enough
		// that a HashMap will give a better speed.
		stack = new TreeMap<Integer, Integer>();
		heap = new TreeMap<Integer, Integer>();
		registers = new HashMap<Integer, Integer>();
		updateObj = new ArrayList<MachineUpdate>();
		// This will be used to determine how many jumps 
		// a function makes before we exit.
		labelJumps = new HashMap<String, Integer>();

		programSpace = new ArrayList<Instr>();

		// This records the function name and maps it to a integer where the
		// function starts in the programSpace.
		functionMapping = new HashMap<String, Integer>();
		// This records the mapping from a label (not a function) to a pc value.
		labelMapping = new HashMap<String, Integer>();

		statusReg = new SREG();
		this.name = name;
		// set up the return address for main
		stack.put(stackPointer, 0xFF);
		stack.put(stackPointer - 1, 0xFF);
		stackPointer -= 2;

		// init the 2 slate datastructures.
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				displaySlate[x][y] = "DARK";
				ledGrid[x][y] = "DARK";
			}
		}

		// add all of the predefined functions
		predefinedFunctions.put("_Z6DrawPxhhh", new FuncSetPix(this));
		predefinedFunctions.put("_Z6ReadPxhh", new FuncGetPix(this));
		predefinedFunctions.put("_Z12DisplaySlatev", new FuncDisplaySlate(this));
		predefinedFunctions.put("_Z16CheckButtonsDownv",new FuncButtonDown(this));
		predefinedFunctions.put("_Z8delay_msj", new FuncDelay(this));
		predefinedFunctions.put("malloc", new FuncMalloc(this));
		predefinedFunctions.put("_Z10Tone_Startjj", new FuncTone(this));
		predefinedFunctions.put("_Z10SetAuxLEDsh", new FuncSetAux(this));

		//add the values for the buttons into the memory space.
		//add these values above the stack pointer.
		labelMapping.put("Button_A", stackPointer+10);
		labelMapping.put("Button_B", stackPointer+11);
		labelMapping.put("Button_Up", stackPointer+12);
		labelMapping.put("Button_Down", stackPointer+13);
		labelMapping.put("Button_Left", stackPointer+14);
		labelMapping.put("Button_Right", stackPointer+15);
		
	}
	
	private void readArgOptsFile(File argoptsfile) {
		// Initializing data structures from arg_opts file if it exists.
		logger.trace(argoptsfile);
		if (argoptsfile!=null && argoptsfile.exists()) {
		    logger.debug("Going to read in meggy/arg_opts file");
		    this.mUsingArgOpts = true;
		    
	        HashSet<Button> currSet;
        
            // zeroth phase
            this.mButtonPresses.add(currSet = new HashSet<Button>());
            try {
                Scanner sc = new Scanner( argoptsfile );

                String line = sc.nextLine();
                this.mMaxCalls = Integer.valueOf( line );
                this.mDelaySim = Boolean.parseBoolean( sc.nextLine() );
                // Parse and run commands
                while(sc.hasNextLine()){
                    String st = sc.nextLine().trim();
                    if (st.equals("delay")) {
                        this.mButtonPresses.add(
                            currSet = new HashSet<Button>());
                    } else {
                        currSet.add(Button.valueOf(st));
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            logger.debug("this.mButtonPresses = "+this.mButtonPresses);
        }

	}
	
	public void noteDelay() {
	    if (this.mUsingArgOpts) {
	        this.mCurrPhase ++;
	    }
	}
	
	public void noteMeggyCall() {
		logger.debug("Checking opt_arg file logic.");
	    if (this.mUsingArgOpts) {
	        this.mMaxCalls--; if (this.mMaxCalls<0) {
	            logger.info("Exiting Systems due to Max Calls being reached.");
	            if (this.countinstrs) {
	                System.out.println("numLoads = "+this.numLoads
			                   +", numStores = "+this.numStores);
			    }
	            System.exit(0);
	            //this.finished = true; slightly off
	        }
	    }
	}
	
	public boolean hasProgram()
	{
		return !programSpace.isEmpty();
		
	}
	
	// Need an int value to represent true or false in simulated machine.
	public int checkButton(String bstr) {
        logger.debug("checkButton, bstr="+bstr+", this.mButtonPresses = "
            +this.mButtonPresses);
        if (this.mUsingArgOpts) {

            // Do we have a phase for the current phase?
            // && Check to see if it's a button press for this phase
            if (this.mButtonPresses.size()>this.mCurrPhase && 
                this.mButtonPresses.get(this.mCurrPhase).contains(
                                                        Button.valueOf(bstr)) ) 
            {
                logger.debug("checkButton returning 1");
                return 1;
            } else {
                return 0;
            }
            
        } else {
            return 0;
        }
    }

	protected MachineState(String name, boolean batch, File argoptsfile,
	                       boolean countinstrs) 
	{
		this(name);		
		this.batch = batch;
		this.readArgOptsFile(argoptsfile);
		this.countinstrs = countinstrs;
	}

	protected MachineState(String name, int jmps, File argoptsfile) {
		this(name);
		this.batch = true;
		this.labelJmps = jmps;
		this.readArgOptsFile(argoptsfile);
	}

	/**
	 * Executes the current instruction at PC.
	 * 
	 * @throws RuntimeError
	 */
	public void executeInstruction() throws RuntimeError {
		logger.trace("Execute Instruction: PC value is (" + pc + ")");

		final Instr currentInstr = programSpace.get(pc);
		// check to see if we are in batch mode, and if so check to see how many
		// times we have hit the label.
		if (batch && (currentInstr instanceof InstrLabel)) {
			logger.debug("We are in batch mode and our current instr is a Label.");
			Integer currentJmps = labelJumps.get(currentInstr.toString());
			if (currentJmps != null) {
				logger.debug("Max jumps: " + labelJmps);
				// If not using arg_opts file and over max jumps then exit.
				if (!this.mUsingArgOpts && (currentJmps > labelJmps)) {
					throw new RuntimeError(
							"Hit max number of jumps for label: "
							+ currentInstr.toString());
				}
				currentJmps++;
				labelJumps.put(currentInstr.toString(), currentJmps);
			} else {
				labelJumps.put(currentInstr.toString(), new Integer(0));
			}

		}
		currentInstr.executeWrapper();
		// after this instruction the pc value should be updated.
		logger.trace("Printing the pc value for determining if it is the end case.\nPC=(" + Integer.toHexString(pc) + ")");
		if ((pc >= programSpace.size()) || (pc == 0xFFFF))// all Fs should be
			// the return
			// address for the
			// main function.
		{
			finished = true;
			logger.info("Hit the finished case.");
			if (this.countinstrs) {
			    System.out.println("numLoads = "+this.numLoads
			                   +", numStores = "+this.numStores);
			}
		}
	}
	
	public void noteLoad() {
	    this.numLoads++;
	}

	public void noteStore() {
	    this.numStores++;
	}

	public ArrayList<Instr> getProgramSpace()
	{
		return programSpace;
	}

	public String getDisplaySlate(int x, int y) {
		return displaySlate[x][y];
	}

	public Integer getFunction(String functionName) {
		return functionMapping.get(functionName);
	}

	public String getGridColor(int x, int y) {
		return ledGrid[x][y];
	}

	/**
	 * Returns a value from within the heap space.
	 * 
	 * @param address
	 * @return Returns the value at the address within the heap space, returns
	 *         null if that address does not exist.
	 */
	public Integer getHeap(int address) {
		final Integer tempReg = heap.get(address);
		return tempReg;
	}

	public Integer getLabel(String labelName) {
		return labelMapping.get(labelName);
	}

	/**
	 * Returns a value from anywhere within the memory space, include the heap
	 * and the stack space.
	 * 
	 * @param address
	 * @return The value located at the provided memory address, null if the
	 *         address has not been assigned to.
	 */
	public Integer getMemory(int address) {
		Integer memValue = null;
		if ((memValue = heap.get(address)) != null) {
			return memValue;
		} else if ((memValue = stack.get(address)) != null) {
			return memValue;
		}
		return new Integer(0); //return the default value of zero.
	}

	/**
	 * Returns the current value of the PC.
	 * 
	 * @return
	 */
	public int getPC() {
		return pc;
	}

	public Func getPreDefinedFunction(String functionName) {
		return predefinedFunctions.get(functionName);
	}

	/**
	 * Returns the value of the register (reg).
	 * 
	 * @param reg
	 *            The register to get the value of.
	 * @return Returns the value of the register.
	 */
	public int getRegister(int reg) {
		final Integer tempReg = registers.get(reg);
		if (tempReg == null)// check to make sure it is not null. if it is
			// return 0.
		{
			return 0;
		}
		return tempReg;
	}

	public int getReturnAddress() {
		return returnAddress;
	}

	/**
	 * Return a copy of the SREG, changing the SREG that is returned here will
	 * not change the SREG that is inside of the machine state. You must use the
	 * UpdateEvent to edit the machine state SREG
	 * 
	 * @return
	 */
	public SREG getSREG() {
		return (SREG) statusReg.clone();
	}

	public int getStack(int address) {
		final Integer tempReg = stack.get(address);
		if (tempReg == null) {
			return 0;
		}
		return tempReg;
	}

	/**
	 * Only use this for debugging and possibly gui purposes.
	 * 
	 * @return A Map with the stack address mapped to the value stored there.
	 */
	public Map<Integer, Integer> getStack()
	{
		return stack;
	}

	/**
	 * Return the current top of the stack.
	 * 
	 * @return The integer representation of the memory address of the top of
	 *         the stack.
	 */
	public int getStackPointer() {
		return stackPointer;
	}

	/**
	 * If there are still instructions to be ran, meaning we have not returned
	 * from main, return true.
	 * 
	 * @return
	 */
	public boolean hasNextInstr() {
		return !finished;
	}

	public boolean isPredefinedFunction(String functionName) {
		return predefinedFunctions.containsKey(functionName);
	}

	/**
	 * This function will init the program space so that we have a mapping from
	 * a pc integer value in the program space to an instruction, it will also
	 * map a function call to a address in the program space.
	 * 
	 * @param program
	 *            The ATmega assembly program to read into the MachineState.
	 */
	public void readAtmegaProgram(ATmegaProgram program) {
		logger.debug("Entered into the readATmegaProgram to read the ATmegaProgram object into the machine program state.");
		final List<Function> functions = program.getFunctions();
		logger.debug("There are (" + functions.size()
				+ ") functions to read into the program space.");
		for (final Function func : functions) {
			// Get the current size of the programSpace arraylist and use that
			// for the mapping.
			final int tempIndex = programSpace.size();
			final List<Instr> instructions = func.getInstructions();
			for (final Instr instr : instructions) {
				logger.trace("Adding new Instruction to ATmegaProgram: <"
						+ instr.getClass().toString() + "> " + instr.toString());
				if (instr instanceof InstrLabel) {
					logger.trace("Adding label: " + instr.toString());
					labelMapping.put(((InstrLabel) instr).getLabel(),
							programSpace.size());
				}
				programSpace.add(instr);
			}
			functionMapping.put(func.getName(), tempIndex);
			logger.info("Adding Function (" + func.getName()
					+ ") to the program space at index (" + tempIndex + ")");
		}
	}

	public void setDisplaySlate(int x, int y, String color) {
		if(color == null)
		{
			logger.fatal("Color is null.");
		}
		displaySlate[x][y] = color;
	}

	public void setGridColor(int x, int y, String color) {
		ledGrid[x][y] = color;
	}

	public void setReturnAddress(int address) {
		this.returnAddress = address;
	}

	/**
	 * Add an object to receive updates from the MachineState.
	 * @param obj
	 */
	public void addUpdate(MachineUpdate obj)
	{
		if(!updateObj.contains(obj))
		{
			updateObj.add(obj);
			//after an add, send the entire machine state to the newly added object.
			MachineUpdateData updateData = new MachineUpdateData();

			for(int reg: registers.keySet())
			{
				updateData.putReg(reg, registers.get(reg));
			}

			for(int addr: stack.keySet())
			{
				updateData.putStack(addr, stack.get(addr));
			}

			for(int addr: heap.keySet())
			{
				updateData.putHeap(addr, heap.get(addr));
			}

			updateData.setStackPointer(stackPointer);

			obj.update(updateData);
		}
	}

	@Override
	public String toString() {
		return "MachineState: " + name;
	}

	private void updateSREG(SREG statusRegisterCopy) {
		if (statusRegisterCopy.isC() != statusReg.isC()) {
			statusReg.setC(statusRegisterCopy.isC());
		}
		if (statusRegisterCopy.isH() != statusReg.isH()) {
			statusReg.setH(statusRegisterCopy.isH());
		}
		if (statusRegisterCopy.isI() != statusReg.isI()) {
			statusReg.setI(statusRegisterCopy.isI());
		}
		if (statusRegisterCopy.isN() != statusReg.isN()) {
			statusReg.setN(statusRegisterCopy.isN());
		}
		if (statusRegisterCopy.isS() != statusReg.isS()) {
			statusReg.setS(statusRegisterCopy.isS());
		}
		if (statusRegisterCopy.isT() != statusReg.isT()) {
			statusReg.setT(statusRegisterCopy.isT());
		}
		if (statusRegisterCopy.isV() != statusReg.isV()) {
			statusReg.setV(statusRegisterCopy.isV());
		}
		if (statusRegisterCopy.isZ() != statusReg.isZ()) {
			statusReg.setZ(statusRegisterCopy.isZ());
		}
	}

	public void updateState(UpdateEvent event) {
		logger.info("Received UpdateEvent...processing...");
		MachineUpdateData updatedData = new MachineUpdateData();

		if (event.getRd() != null) {
			final HashMap<Integer, Integer> rds = event.getRd();
			for(Integer key:rds.keySet())
			{
				logger.info("Updating register r(" + key
					+ ") with the value (" + rds.get(key) + ")"
					+ " and hex ( 0x" + Integer.toHexString(rds.get(key))+")" );	
				registers.put(key, rds.get(key));				
				updatedData.putReg(key, rds.get(key));
			}
		}

		if (event.getMemory() != null) {
			final HashMap<Integer, Integer> updateMemory = event.getMemory();
			for(Integer key:updateMemory.keySet())
			{
				logger.info("Updating memory location (" + key
						+ ") with the value (" + updateMemory.get(key) + ")");
				stack.put(key, updateMemory.get(key));

				if(key < stackPointer) //this should be the heap then?
				{
					logger.trace("Adding data to the heap.");
					updatedData.putHeap(key, updateMemory.get(key));
				}
				else
				{
					updatedData.putStack(key, updateMemory.get(key));
				}
			}
		}
		logger.debug("Getting event StackPointer" + event.getStackPointer());
		if (event.getStackPointer() >= 0) {
			logger.info("Updating stack pointer to (0x"
					+ Integer.toHexString(event.getStackPointer()) + ")");
			this.stackPointer = event.getStackPointer();
			updatedData.setStackPointer(stackPointer);
		}

		if (event.getSREG() != null) {
			updateSREG(event.getSREG());
			updatedData.setSREG(event.getSREG());
		}

		if(event.getPc() >=0)
		{
			logger.trace("Updating PC value: " + event.getPc());
			pc = event.getPc();
		}

		sendUpdates(updatedData);
	}

	private void sendUpdates(MachineUpdateData updatedData)
	{
		for(MachineUpdate obj: updateObj)
		{
			obj.update(updatedData);
		}
	}
}

