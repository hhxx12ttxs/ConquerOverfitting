package com.bmc.arsys.demo.javadriver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.bmc.arsys.api.ARException;
import com.bmc.arsys.api.ARServerUser;
import com.bmc.arsys.api.Constants;
import com.bmc.arsys.api.Entry;
import com.bmc.arsys.api.LoggingInfo;
import com.bmc.arsys.api.ProxyManager;
import com.bmc.arsys.api.QualifierInfo;
import com.bmc.arsys.api.StatusInfo;
import com.bmc.arsys.api.Value;
import com.bmc.arsys.api.WfdBreakpoint;
import com.bmc.arsys.api.WfdDebugLocation;
import com.bmc.arsys.api.WfdUserContext;

public class WFD extends JavaDriver {
    boolean executeCommand = true;

    static int quietMode = 0;
    static int randomNumberSeed;
    static String resultDirectory;
    boolean primaryThread = false;
    static String[] commandLineArgs = null;
    static int maxConnectionPerServer = 250;


    static WfdOutputWriter outputWriter = new WfdOutputWriter();

    static int	currentRpcQueue = 0;

    static boolean javaDriverOnly = true;

    static int SUPPRESS_RESULTS = 0x0001;
    static int SUPPRESS_HEADERS = 0x0002;
    static int SUPPRESS_PROMPTS = 0x0004;
    static int SUPPRESS_MENU = 0x0008;
    static int SUPPRESS_ERRORS = 0x0010;
    static int SUPPRESS_WARNINGS = 0x0020;
    static int SUPPRESS_TIME = 0x0040;
    static int SUPPRESS_DATE = 0x0080;
    static int SUPPRESS_FOR_WFD = 0x0100;
    
    static int MAX_NESTED_LOOP_DEPTH = 10;
    static int RAND_MAX = 0x7fff;

    static List<WfdBreakpoint> Breakpoints = new ArrayList<WfdBreakpoint>(10);

    void printAuxillaryStatus() {
        //do nothing
    }

    @Override
    public ARServerUser getControlStructObject() {
        ThreadControlBlock threadControlBlockPtr = getThreadControlBlockPtr();
        return threadControlBlockPtr.getContext();
    }

    public void setPrimaryThread(boolean primary) {
        primaryThread = primary;
    }

    public boolean isPrimaryThread() {
        return primaryThread;
    }

    public void setCommandLineArgs(String[] args) {
        commandLineArgs = args;
    }

    public String[] getCommandLineArgs() {
        return commandLineArgs;
    }

    public static void setResultDirectory(String directory) {
        resultDirectory = directory;
    }

    public static String getResultDirectory() {
        return resultDirectory;
    }

    public static void setQuietMode(int value) {
        quietMode = value;
    }

    public static int getQuietMode() {
        return quietMode;
    }

    public static void setRandomNumberSeed(int value) {
        randomNumberSeed = value;
    }

    public static int getRandomNumberSeed() {
        return randomNumberSeed;
    }

    public static boolean notWfdQuiet() {
    	if( (quietMode & SUPPRESS_FOR_WFD) == 0 )
    		return false;
    	else
    		return true;
    }

    @Override
    int getNextCommand(StringBuilder args) throws IOException {
        ThreadControlBlock threadControlBlockPtr = getThreadControlBlockPtr();
        threadControlBlockPtr.setArgs(null);
        outputWriter.driverPrintPrompt("\nCommand: ");

        int commandCode = -99;
        while (commandCode == -99) { /* read and validate the command */
            InputReader.getInputLine();

            // Search for blank and if present copy the arguments
            String buffer = threadControlBlockPtr.getBuffer();
            String command = buffer;
            for (int i = 0; i < buffer.length(); i++) {
                if (buffer.charAt(i) == ' ') {
                    char[] tempArgs = new char[buffer.length() - i - 1];
                    buffer.getChars(i + 1, buffer.length(), tempArgs, 0);
                    String tmp = new String(tempArgs);
                    threadControlBlockPtr.setArgs(tmp);

                    char[] tempCmd = new char[i];
                    buffer.getChars(0, i, tempCmd, 0);
                    command = new String(tempCmd);
                    break;
                }
            }

            if ((command.length() == 0) || (command.charAt(0) == '#')) {
                // blank line or comment
                outputWriter.driverPrintPrompt("\nCommand: ");
            } else if (command.length() > 6) {
                outputWriter.driverPrintError(" ***Command too long, unrecognized ****\n");
                outputWriter.driverPrintPrompt("\nCommand: ");
            } else if (command.equals("h") || command.equals("?")) {
                outputWriter.driverPrintHelp();
                outputWriter.driverPrintPrompt("\nCommand: ");
            } else if (command.equals("e") || command.equals("q") || command.equals("x")) {
                commandCode = WfdCommands.COMMAND_EXIT;
            } else {
                commandCode = WfdCommands.getCommandCode(command);
                if (commandCode != WfdCommands.UNKNOWN_COMMAND) {
                    threadControlBlockPtr.setCurrentCommand(command);
                } else {
                    outputWriter.driverPrintError(" *** Command not recognized ***\n");
                    outputWriter.driverPrintPrompt("\nCommand: ");
                }
            }
        }
        return commandCode;
    }

    public void processCommands() throws IOException {
        int commandCode = WfdCommands.COMMAND_EXIT; /* code for the command requested */
        StringBuilder argsBuf = new StringBuilder(); /* pointer to arguments */

        InputReader.setNullPromptOption(true);
        if( getControlStructObject().getServer() != null )
        {
        	try {
        		getControlStructObject().wfdSetDebugMode(65535);
        	} catch (ARException e) {
        		e.printStackTrace();
        	}
        }
        
        // process commands until exit specified
        while ((commandCode = getNextCommand(argsBuf)) != WfdCommands.COMMAND_EXIT) {
            argsBuf = new StringBuilder();
            switch (commandCode) {
            case WfdCommands.COMMAND_LOGIN:
                getARServerUser();
                break;
            case WfdCommands.COMMAND_INITIALIZATION:
                wfdInitialization();
                break;
            case WfdCommands.COMMAND_TERMINATION:
                termination();
                break;
            case WfdCommands.COMMAND_SET_SERVER_PORT:
                setServerPort();
                break;
            case WfdCommands.COMMAND_WFD_STEP:
                wfdExec( Constants.WFD_EXECUTE_STEP );
                wfdGetCurrentLocation();
                break;
            case WfdCommands.COMMAND_WFD_RUN_TO_RBP:
            	releaseRunThread();
                break;
            case WfdCommands.COMMAND_WFD_GO_TO_BP:
                wfdGoToBp();
                wfdGetCurrentLocation();
                break;
            case WfdCommands.COMMAND_WFD_GET_CURRENT_LOCATION:
                wfdGetCurrentLocation();
                break;
            case WfdCommands.COMMAND_WFD_GET_FIELDVALUES:
                wfdGetFieldValues();
                break;
            case WfdCommands.COMMAND_WFD_SET_FIELDVALUES:
                wfdSetFieldValues();
                break;
            case WfdCommands.COMMAND_WFD_GET_DEBUG_MODE:
                wfdGetDebugMode();
                break;
            case WfdCommands.COMMAND_WFD_SET_DEBUG_MODE:
                wfdSetDebugMode();
                break;
            case WfdCommands.COMMAND_WFD_SETALL_MODE:
                wfdDoSetDebugMode( 0xffff );
                break;
            case WfdCommands.COMMAND_WFD_CLEAR_MODE:
                wfdDoSetDebugMode( 0 );
                break;
            case WfdCommands.COMMAND_WFD_GET_FILTER_QUAL:
                wfdGetFilterQual();
                break;
            case WfdCommands.COMMAND_WFD_SET_QUAL_RESULT:
                wfdSetQualifierResult();
                break;
            case WfdCommands.COMMAND_WFD_SET_BREAK_PT:
                wfdSetBreakPt();
                break;
            case WfdCommands.COMMAND_WFD_CLR_BREAK_PT:
                wfdClrBreakPt();
                break;
            case WfdCommands.COMMAND_WFD_LIST_BREAKPTS:
                wfdListBreakPt();
                break;
            case WfdCommands.COMMAND_WFD_CLR_BREAK_LST:
                wfdClearBpList();
                break;
            case WfdCommands.COMMAND_WFD_TERMINATE_API:
                wfdTerminateAPI();
                break;
            case WfdCommands.COMMAND_WFD_DISPLAY_STK_FRM:
                wfdGetStackLocation();
                break;
            case WfdCommands.COMMAND_WFD_LIST_STACK:
                wfdDumpStack();
                break;
            case WfdCommands.COMMAND_WFD_FINISH_API:
                wfdFinishAPI();
                break;
            case WfdCommands.COMMAND_WFD_GET_KEYWORD:
            	wfdGetKeyword();
            	break;
            case WfdCommands.COMMAND_WFD_GET_USER_CONTEXT:
            	wfdGetUserContext();
            	break;
            case WfdCommands.COMMAND_WFD_RMT_SET_BKPT:
            	wfdRmtSetBreakPt();
            	break;
            case WfdCommands.COMMAND_WFD_RMT_CLR_BKPT:
            	wfdRmtClrBreakPt();
            	break;
            case WfdCommands.COMMAND_WFD_RMT_LIST_BKPT:
            	wfdRmtListBreakPt();
            	break;
            case WfdCommands.COMMAND_WFD_RMT_CLR_BP_LIST:
            	wfdRmtClearBpList();
            	break;
            case WfdCommands.COMMAND_WFD_INFO:
            	outputWriter.printExtraHelp();
            	break;
            case WfdCommands.COMMAND_WFD_RESTART:
            	wfdRestart();
            	break;

            default:
                outputWriter.driverPrintNotSupportCommand(commandCode);
            break;
            }
        }
        if (getControlStructObject().getServer() != null) {
			try {
				getControlStructObject().wfdSetDebugMode(0);
			} catch (ARException e) {
				e.printStackTrace();
			}
		}
    }

    void setClientType() {
        getControlStructObject().setClientType(3);
    }

    @Override
    boolean processCommandLine() {
        String[] argv = getCommandLineArgs();
        int maxArgumentLen = 0;
        char option;
        String tempPtr = null;
        int rpcQueue = 0;

        ThreadControlBlock threadControlBlockPtr = (ThreadControlBlock) getThreadControlBlockPtr();

        int argc = 0;
        if (argv != null) {
            argc = argv.length;
        }
        for (int i = 0; i < argc; i++) { /* process each entry on the command line */
            if ((argv[i].charAt(0) == '-')
                    && (argv[i].charAt(1) == 'u' || argv[i].charAt(1) == 'p' || argv[i].charAt(1) == 'l'
                        || argv[i].charAt(1) == 's' || argv[i].charAt(1) == 'x' || argv[i].charAt(1) == 'o'
                            || argv[i].charAt(1) == 'd' || argv[i].charAt(1) == 'q' || argv[i].charAt(1) == 'g'
                                || argv[i].charAt(1) == 'c' || argv[i].charAt(1) == 'a' || argv[i].charAt(1) == 'S')) {

                option = argv[i].charAt(1);
            } else { /* unrecognized option */
                outputWriter.driverPrintError("Unrecognized option " + argv[i] + "\n");
                return false;
            }
            /* have option, load the value */
            if (argv[i].length() > 2) {
                int cmdLength = argv[i].length() - 2;
                char[] tempCmd = new char[cmdLength];
                argv[i].getChars(2, argv[i].length(), tempCmd, 0);
                tempPtr = new String(tempCmd);
            } else { /* check next argument for name */
                i++;
                if (i < argc) {
                    tempPtr = argv[i];
                } else { /* no next argument so error */
                    outputWriter.driverPrintError("Missing value for -" + option + "  option\n");
                    return false;
                }
            }
            /* get max argument length */
            switch (option) {
            case 'a':
            case 'u':
            case 'p':
            case 'l':
            case 's':
                maxArgumentLen = 254;
                break;
            case 'x':
            case 'd':
                maxArgumentLen = 255;
                break;
            case 'o':
            case 'q':
                maxArgumentLen = 3;
                break;
            case 'g':
            case 'S':
                maxArgumentLen = 10;
                break;
            case 'c':
                maxArgumentLen = 5;
                break;
            }

            if (tempPtr.length() > maxArgumentLen) { /* argument too long so error */
                outputWriter.driverPrintError("Value for -" + option + "  option is too long: " + tempPtr + "\n");
                return false;
            }

            /* take appropriate action */
            char[] tempCmd = null;
            switch (option) {
            case 'u':
                boolean authStringFound = false;

                int j;
                for (j = 0 ; j < tempPtr.length(); j++) {
                    if (tempPtr.charAt(j) == '\\') {
                        tempCmd = new char[j];
                        tempPtr.getChars(0, j, tempCmd, 0);
                        threadControlBlockPtr.setAuthentication(new String(tempCmd));
                        authStringFound = true;
                        break;
                    }
                }
                if (authStringFound) {
                    int cmdLength = tempPtr.length() - j - 1;
                    tempCmd = new char[cmdLength];
                    tempPtr.getChars(j + 1, tempPtr.length(), tempCmd, 0);
                    threadControlBlockPtr.setUser(new String(tempCmd));
                } else {
                    threadControlBlockPtr.setUser(tempPtr);
                }
                outputWriter.printString("Setting user to " + tempPtr + "\n");
                break;

            case 'a':
                threadControlBlockPtr.setAuthentication(tempPtr);
                break;

            case 'p':
                threadControlBlockPtr.setPassword(tempPtr);
                break;

            case 'l':
                threadControlBlockPtr.setLocale(tempPtr);
                break;

            case 's':
                threadControlBlockPtr.setServer(tempPtr);
                outputWriter.printString("Setting server to " + tempPtr + "\n");
                break;

            case 'S':
                rpcQueue = new Integer(tempPtr).intValue();
                break;

            case 'x':
                openInputFile(tempPtr);
                break;

            case 'o':
                setOutputSetting(tempPtr);
                break;

            case 'd':
                resultDirectory = tempPtr;
                break;

            case 'q':
                quietMode = new Integer(tempPtr).intValue();
                break;

            case 'g':
                randomNumberSeed = new Integer(tempPtr).intValue();
                break;

            case 'c':
                setOutputCount(tempPtr);
                break;
            }
        }
        if (rpcQueue != 0) {
			try {
				getControlStructObject().usePrivateRpcQueue(rpcQueue);
				currentRpcQueue = rpcQueue;
			} catch (ARException e) {
				outputWriter.printARException(e);
			}
		}
		outputWriter.printString("Setting RPC Queue to " + rpcQueue + "\n");
        return true;
    }

    protected void setOutputSetting(String tempPtr) {
        ProxyManager.setConnectionLimits(maxConnectionPerServer);
    }

    protected void setOutputCount(String tempPtr) {
        //do nothing
    }

    void getARServerUser() {
        try {

            InputReader.getARServerUser();
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (IllegalArgumentException e) {
            outputWriter.printString("Bad User or Password...\n");
        }
    }

    void openInputFile(String fileName) {

        try {
            InputReader.openInputFile(fileName);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        }
    }

    void closeOutputFile() {
        outputWriter.closeOutputFile();
    }

    void openOutputFile() {
        try {
            String fileName = InputReader.getString("Filename of output file (): ", "");
            outputWriter.openOutputFile(fileName);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        }
    }

    void startRecording() {
        String fileName = null;
        try {
            ThreadControlBlock threadControlBlockPtr = getThreadControlBlockPtr();

            if (threadControlBlockPtr.getRecordFile() != null) {
                outputWriter.driverPrintWarning(" **** Recording already active; stop previous to start new\n");
            } else {
                // get a filename and open for recording
                fileName = InputReader.getString("Filename of record file (): ", "");
                if ((fileName == null) || (fileName.length() == 0)) {
                    outputWriter.driverPrintError(" **** No filename specified so no recording started\n");
                } else { /* open the new file for write */
                    threadControlBlockPtr.setRecordFile(new PrintWriter(new FileOutputStream(new File(fileName))));
                }
            }
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...or opening the file " + fileName + "  .\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        }
    }

    void stopRecording() {
        try {
            ThreadControlBlock threadControlBlockPtr = getThreadControlBlockPtr();

            if (threadControlBlockPtr.getRecordFile() == null) {
                outputWriter.driverPrintWarning(" **** Recording is not active\n");
            } else {
                threadControlBlockPtr.setRecordFile(null);
            }
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        }
    }

    void beginLoop() {

        try {
            outputWriter.driverPrintHeader("BEGIN LOOP");
            ThreadControlBlock threadControlBlockObject = getThreadControlBlockPtr();
            if (threadControlBlockObject.isCurrentInputSourceStdInput()) {
                // print error and return
                outputWriter.driverPrintError("\n*** Command must be issued from input file\n");
                return;
            }

            int numIterations = InputReader.getInt("Number of iterations (1): ", 1);

            if (numIterations <= 0) {
                numIterations = 1;
            }

            if (threadControlBlockObject.getCurrentLoopDepth() == (MAX_NESTED_LOOP_DEPTH - 1)) {
                // print error and return
                outputWriter.driverPrintError("\n*** Maximum nested loop depth exceeded\n");
                threadControlBlockObject.incrementNumFailedBeginLoop();

                return;
            }

            threadControlBlockObject.incrementCurrentLoopDepth();

            threadControlBlockObject.setNumberOfIterations(threadControlBlockObject.getCurrentLoopDepth(),
                    numIterations);

            threadControlBlockObject.setLoopBeginFilePos(threadControlBlockObject.getCurrentLoopDepth(),
                    threadControlBlockObject.getCurrentInputFilePos());

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        }

    }

    void endLoop() throws IOException, FileNotFoundException {

        try {
            outputWriter.driverPrintHeader("END LOOP");

            ThreadControlBlock threadControlBlockObject = getThreadControlBlockPtr();

            if (threadControlBlockObject.isCurrentInputSourceStdInput()) {
                // print error and return
                outputWriter.driverPrintError("\n*** Command must be issued from input file\n");
                return;
            }

            if (threadControlBlockObject.getCurrentLoopDepth() < 0) {
                // print warning and return
                outputWriter.driverPrintWarning("\n*** Command ignored because no corresponding begin loop issued\n");
                return;
            }

            if (threadControlBlockObject.getNumFailedBeginLoop() > 0) {
                // print warning and return
                outputWriter.driverPrintWarning("\n*** Command ignored because previous begin loop failed\n");
                threadControlBlockObject.reduceNumFailedBeginLoop();
                return;
            }

            long numIterations = threadControlBlockObject.getNumberOfIterations(threadControlBlockObject
                    .getCurrentLoopDepth());
            threadControlBlockObject.reduceNumberOfIterations(threadControlBlockObject.getCurrentLoopDepth());

            if ((numIterations - 1) > 0) {
                int loopDepth = threadControlBlockObject.getCurrentLoopDepth();
                long filePosition = threadControlBlockObject.getLoopBeginFilePos(loopDepth);
                threadControlBlockObject.setCurrentInputFileReadingPostion(filePosition);
            } else {
                threadControlBlockObject.reduceCurrentLoopDepth();
            }

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        }

    }

    void setLogging() {
        try {
            boolean enable = InputReader.getBooleanForChangingInfo("enable logging? (T):", true);
            long logType = 0;
            long whereToLog = 0;
            String fileName = null;

            if (enable == true) {
                logType = InputReader.getLong("Logging Type NONE(0), SQL(1), Filter(2), API(16)? (0): ", 0);
                whereToLog = InputReader.getLong("Return logging records to file(1), status list(2)? (1): ", 1);
                if ((int) (whereToLog & 1) == 1)
                    fileName = InputReader.getString("Name of file to hold returned records: ", "");
            }
            LoggingInfo info = new LoggingInfo(enable, logType, whereToLog, fileName);
            getControlStructObject().setLogging(info);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void wfdExecute() {
        try {
        	if( notWfdQuiet() )
        		outputWriter.driverPrintHeader("WFD EXECUTE");

            // Get the stack depth requested
            int mode = InputReader.getInt("Execute mode ? (0): ", 0);

            // Call ARWfdExecute method
            beginAPICall();
            getControlStructObject().wfdExecute(mode);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Wfd Get Current Location Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void wfdGetCurrentLocation() {
        WfdDebugLocation currentLocation = null;
        try {
        	if( notWfdQuiet() )
        		outputWriter.driverPrintHeader("WFD GET CURRENT LOCATION");

            // Call ARWfdGetDebugLocation method
            currentLocation = getControlStructObject().wfdGetDebugLocation(0);

            outputWriter.printCurrentLocation("Current Location:\n", currentLocation);

            // Success. Print the status
            //List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            //outputWriter.printStatusInfoList("", "Wfd Get Current Location Status", statusList);
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void wfdGetDebugMode() {
        int mode;
        try {
        	if( notWfdQuiet() )
        		outputWriter.driverPrintHeader("WFD GET DEBUG MODE");

            // Call ARWfdGetDebugMode method
            beginAPICall();
            mode = getControlStructObject().wfdGetDebugMode();
            endAPICall(getControlStructObject().getLastStatus());

            System.out.print("Current DebugMode (stop at):\n" + 
                    (((mode & Constants.WFD_STOP_START_API) != 0) ? "   Begin API\n" : "") +
                    (((mode & Constants.WFD_STOP_QUALIFIER) != 0) ? "   Qualfier\n" : "") +
                    (((mode & Constants.WFD_STOP_P1_ACTION) != 0) ? "   Phase 1\n" : "") +
                    (((mode & Constants.WFD_STOP_P2_ACTION) != 0) ? "   Phase 2\n" : "") +
                    (((mode & Constants.WFD_STOP_P3_ACTION) != 0) ? "   Phase 3\n" : "") +
                    (((mode & Constants.WFD_STOP_END_API) != 0) ? "   End API\n" : "") +
                    (((mode & Constants.WFD_STOP_ESC_ACTION) != 0) ? "   Esc Action\n" : "") +
                    (((mode & Constants.WFD_STOP_CMDB) != 0) ? "   CMDB\n" : "") );

            // Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Wfd Get Debug Mode Status", statusList);
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void wfdDoSetDebugMode(int newMode) {
        try {
        	if( notWfdQuiet() )
        		outputWriter.driverPrintHeader("WFD SET DEBUG MODE");

            // Call ARWfdSetDebugMode method
            beginAPICall();
            getControlStructObject().wfdSetDebugMode(newMode);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Wfd Set Debug Mode Status", statusList);
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }
    
    void wfdSetDebugMode() {
    	int		debugMode = 0;
        try {
        	if( notWfdQuiet() )
        		outputWriter.driverPrintHeader("WFD SET DEBUG MODE");

            // Get the stack depth requested
            outputWriter.driverPrintHeader( "Debug Mode to set (stop at)? : " );
            if( InputReader.getBoolean( "   Begin API (T): ", true) )
            	debugMode |= Constants.WFD_STOP_START_API;
            else
            	debugMode &= ~Constants.WFD_STOP_START_API;

            if( InputReader.getBoolean("   Qualifier (T): ", true) )
            	debugMode |= Constants.WFD_STOP_QUALIFIER;
            else
            	debugMode &= ~Constants.WFD_STOP_QUALIFIER;

            if( InputReader.getBoolean("   Phase 1 (T): ", true) )
            	debugMode |= Constants.WFD_STOP_P1_ACTION;
            else
            	debugMode &= ~Constants.WFD_STOP_P1_ACTION;

            if( InputReader.getBoolean("   Phase 2 (T): ", true) )
            	debugMode |= Constants.WFD_STOP_P2_ACTION;
            else
            	debugMode &= ~Constants.WFD_STOP_P2_ACTION;

            if( InputReader.getBoolean("   Phase 3 (T): ", true) )
            	debugMode |= Constants.WFD_STOP_P3_ACTION;
            else
            	debugMode &= ~Constants.WFD_STOP_P3_ACTION;

            if( InputReader.getBoolean("   End API (T): ", true) )
            	debugMode |= Constants.WFD_STOP_END_API;
            else
            	debugMode &= ~Constants.WFD_STOP_END_API;

            if( InputReader.getBoolean("   Esc Action (T): ", true) )
            	debugMode |= Constants.WFD_STOP_ESC_ACTION;
            else
            	debugMode &= ~Constants.WFD_STOP_ESC_ACTION;

            if( InputReader.getBoolean("   CMDB (T): ", true) )
            	debugMode |= Constants.WFD_STOP_CMDB;
            else
            	debugMode &= ~Constants.WFD_STOP_CMDB;

            wfdDoSetDebugMode( debugMode );

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        }
    }

    void wfdGetFieldValues() {
        try {
        	if( notWfdQuiet() )
        		outputWriter.driverPrintHeader("WFD GET FIELD VALUES");

            // Get the stack depth requested
            int depth = InputReader.getInt("Field from stack frame ? (0): ", 0);

            // Call ARWfdGetFieldValues method
            beginAPICall();
            List<Entry> fieldValues = getControlStructObject().wfdGetFieldValues(depth);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printEntry("", "Transaction Fields - ", fieldValues.get(0));
            outputWriter.printEntry("", "Database Fields - ", fieldValues.get(1));

            // Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Wfd Get Field Values Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void wfdSetFieldValues() {
        try {
        	if( notWfdQuiet() )
        		outputWriter.driverPrintHeader("WFD Set Field Values");

            // Get the entry items information
            ArrayList<Entry> fieldValues = new ArrayList<Entry>(2);
            outputWriter.driverPrintPrompt("Field/value pairs to set in transaction list:\n");
            Entry trans = InputReader.getEntry();

            outputWriter.driverPrintPrompt("Field/value pairs to set in database list:\n");
            Entry dbase = InputReader.getEntry();

            fieldValues.add(0, trans);
            fieldValues.add(1, dbase);

            // Call ARWfdSetFieldValues method
            beginAPICall();
            getControlStructObject().wfdSetFieldValues(fieldValues);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Wfd Set Debug Mode Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void wfdGetFilterQual() {
        try {
        	if( notWfdQuiet() )
        		outputWriter.driverPrintHeader("WFD GET FILTER QUALIFIER");

            // Call ARWfdGetFilterQual method
            beginAPICall();
            QualifierInfo qualifier = getControlStructObject().wfdGetFilterQual();
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printQualifierInfo("", "Filter Qualifier:\n", qualifier);

            // Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Wfd Get Filter Qualifier Status", statusList);
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void wfdTerminateAPI() {
        try {
        	if( notWfdQuiet() )
        		outputWriter.driverPrintHeader("WFD Terminate API");

            // Get the errorCode to return
            int errorCode = InputReader.getInt("Error Code to return (AR_ERROR_TERMINATED_BY_DEBUGGER): ", 9871);

            // Call ARWfdTerminateAPI method
            beginAPICall();
            getControlStructObject().wfdTerminateAPI(errorCode);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Wfd Terminate API Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void wfdSetQualifierResult() {
        try {
        	if( notWfdQuiet() )
        		outputWriter.driverPrintHeader("WFD Set Qualifier Result");

            boolean x = InputReader.getNullPromptOption();
            InputReader.setNullPromptOption(true);
            boolean result = InputReader.getBoolean("Set Qualfier Result, T/F? (T): ", true);
            InputReader.setNullPromptOption(x);

            // Call ARWfdSetQualifierResult method
            beginAPICall();
            getControlStructObject().wfdSetQualifierResult(result);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Wfd Set Qualifier Result Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void wfdGetKeyword() {
        try {
        	if( notWfdQuiet() )
        		outputWriter.driverPrintHeader("WFD Get Keyword Value");

            // Get the keyword ID requested
            int kwId = InputReader.getInt("Keyword ID ? (1): ", 1);
            Value val = new Value();

            // Call ARWfdSetQualifierResult method
            beginAPICall();
            val = getControlStructObject().wfdGetKeywordValue(kwId);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printValue( "", "Keyword Value:", val );

            // Print the status
            //List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            //outputWriter.printStatusInfoList("", "Wfd Get Keyword Value Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void wfdGetUserContext() {
        try {
        	if( notWfdQuiet() )
        		outputWriter.driverPrintHeader("WFD Get User Context");

            // Get the keyword ID requested
            int mask = InputReader.getInt("Context Mask? (0): ", 0);
            WfdUserContext userInfo;

            // Call ARWfdSetQualifierResult method
            beginAPICall();
            userInfo = getControlStructObject().wfdGetUserContext(mask);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printWfdUserContext( "User Context:", userInfo );

            // Print the status
            //List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            //outputWriter.printStatusInfoList("", "Wfd Get Keyword Value Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    WfdBreakpoint FindEmptyBP()
    {
        int              i;
        WfdBreakpoint    bp;

        for( i = 0; i < Breakpoints.size(); i++ )
        {
            if( (bp = Breakpoints.get(i)) == null ) {
                Breakpoints.add(i, (bp = new WfdBreakpoint()) );
                bp.id = i;
            }
            else if( bp.filter.length() == 0) {
                return( bp );
            }
        }
        Breakpoints.add(i, (bp = new WfdBreakpoint()) );
        return( bp );
    }

    void wfdSetBreakPt()
    {
        WfdBreakpoint  bp;

        if( (bp = FindEmptyBP()) != null )
        {
            try {
                bp.filter = InputReader.getString( "  BP location -\n  Filter: ", "" );
                bp.schema = InputReader.getString( "  Schema (*): ", "*" );
                bp.stage = WfdDebugLocation.WFD_BEFORE_QUAL + 
                	InputReader.getInt( "  Stage PreQual/Phase1/Phase2/Phase3/Escl/CMDB (0-5) (1): ", 1 );
                if( (bp.stage < WfdDebugLocation.WFD_BEFORE_QUAL) ||
                	(bp.stage > WfdDebugLocation.WFD_BEFORE_CMDB) ) {
                    bp.filter = null;
                    bp.schema = null;
                    outputWriter.printString("Invalid stage " + bp.stage);
                    return;
                 }

                bp.elsePath = InputReader.getBoolean( "  Else path? (0): ", false );
                bp.actionNo = InputReader.getInt( "  Action number (0): ", 0 );
                bp.disable = false; /* TBI */
                bp.passcount = 0; /* TBI */
            } catch (IOException e) {
                outputWriter.printString("Problem in getting the input...Driver problem..\n");
            }
            return;
        }
        else
        {
            outputWriter.printString( "Problem allocating BreakPoint!\n" );    
            return;
        }
    }

    void wfdClearBpList()
    {
        WfdBreakpoint    bp;

        for( int i = 0; i < Breakpoints.size(); i++ )
        {
            if( (bp = Breakpoints.get(i)) != null )
            {
                bp.filter = "";
            }
        }
    }

    
    void wfdClrBreakPt()
    {
        int             bpIndex = 0;
        WfdBreakpoint   bp;
        
        try {
        	bpIndex = InputReader.getInt( "Breakpoint to clear (0): ", 0 );
        } catch (IOException e) {
        	outputWriter.printString("Problem in getting the input...Driver problem..\n");
        }

        try {
        	if ((bp = Breakpoints.get(bpIndex)) != null) {
        		bp.filter = "";
        	}
        } catch (RuntimeException e) {
        	outputWriter.printString("Breakpoint " + bpIndex + " not found..\n");
        }
    }

    void wfdListBreakPt()
    {
       WfdBreakpoint    bp;
       
       for( int i = 0; i < Breakpoints.size(); i++ )
       {
          bp = Breakpoints.get(i);
          if( bp.filter.length() != 0 )
              outputWriter.printBreakpoint( "", bp, true );
       }
    }

    boolean checkForBP( WfdDebugLocation location ) {
        WfdBreakpoint    bp;

        for( int i = 0; i < Breakpoints.size(); i++ )
        {
            bp = Breakpoints.get(i);
            if( (bp.filter.length() != 0) &&
                (bp.filter.equals(location.Filter) &&
                (bp.schema.equals("*") || bp.schema.equals(location.SchemaName)) &&
                (bp.stage == location.Stage) && 
                (bp.elsePath == location.ElsePath) &&
                (bp.actionNo == location.ActionNo)) )
            {
                outputWriter.printString("*** BreakPoint #" + i + " encountered:\n");    
                return( true );
            }
        }

        return false;
    }

    void wfdGoToBp() {
        WfdDebugLocation    location = null;

        /* Get parameters */
        outputWriter.printString("WFD Run To BP");


        /* We want to be out of the idle state at least once before monitoring for BP */
        /* This allows the user to set up a BP while idle, and then say go. The BP will */
        /* be hit after the user starts some workflow. */
        try {
            while( true )
            {
                getControlStructObject().wfdExecute(Constants.WFD_EXECUTE_STEP);
                location = getControlStructObject().wfdGetDebugLocation(0);
                if( location.Stage != WfdDebugLocation.WFD_IDLE )
                    break;
                sleep( 500 );
            }

            /* Now that we're not IDLE, start looking for BP or finished */
            do {
                location = getControlStructObject().wfdGetDebugLocation(0);
                if( (location.Stage == WfdDebugLocation.WFD_IDLE) || 
                     checkForBP(location) )
                    break;
                else if( location.Stage == WfdDebugLocation.WFD_RUNNING )
                    continue;
                getControlStructObject().wfdExecute(Constants.WFD_EXECUTE_STEP);
            } while( true );
        } catch (ARException e) {
            outputWriter.printARException(e);
        } catch (InterruptedException e) {
            outputWriter.printString("Interrupted while waiting for breakpoint.\n");    
            e.printStackTrace();
        }

        return;   
    }
    
    @Override
    public void launchThread(boolean flag) {

    	ThreadStartInfo threadStartInfo = new ThreadStartInfo();

    	try {
    		ThreadControlBlock threadControlBlockPtr = getThreadControlBlockPtr();

    		threadStartInfo.setOutputToStdOut();

    		/* copy the authentication string that the launched thread will login with */
    		threadStartInfo.setAuthentication(this.getControlStructObject().getAuthentication());
    		threadStartInfo.setUser(this.getControlStructObject().getUser());
    		threadStartInfo.setPassword(this.getControlStructObject().getPassword());
    		LocaleCharSet localeCharSet = new LocaleCharSet(this.getControlStructObject().getLocale());
    		threadStartInfo.setLocale(localeCharSet.getLocale());
    		threadStartInfo.setCharset(localeCharSet.getCharSet());
    		threadStartInfo.setServer(this.getControlStructObject().getServer());


    		threadStartInfo.setUpperBound(0);

    		// Set the wait information depending on the wait flag
    		threadStartInfo.setWaitFlag(flag);

    		if (flag) {
    			threadStartInfo.setWaitObject(threadControlBlockPtr.getWaitObject());
    			threadStartInfo.setReleaseObject(threadControlBlockPtr.getReleaseObject());
    		}

    		// Create another WFD thread
    		WFD wfd = instantiateWFD();
    		wfd.setThreadStartInfo(threadStartInfo);
    		wfd.setPrimaryThread(false);

    		// Now start the thread
    		wfd.start();

            // Add this thread to the children list
            threadControlBlockPtr.addThreadHandle(wfd);

            // if wait flag is true wait till the waitobject is notified
            if (flag) {
                SyncObject waitObject = threadControlBlockPtr.getWaitObject();
                synchronized (waitObject) {
                    try {
                        while (waitObject.getFlag() != true) {
                            waitObject.wait();
                        }
                    } catch (InterruptedException e) {
                        waitObject.setFlag(true);
                        System.out.println("Interrupted Exception ...in simplethread run");
                    }
                }
            }

        } catch (NullPointerException e) {
            try {
                threadStartInfo.cleanUp();
                outputWriter.driverPrintException(e);
            } catch (IOException ex) {
                outputWriter.printString("Problem in cleaning up the thread start info..\n");
            }
        }
    }

    protected WFD instantiateWFD() {
        return new WFD();
    }

    public void threadStartFunction() throws IOException {
        ThreadControlBlock threadControlBlockObject = getThreadControlBlockPtr();

        threadControlBlockObject.setCurrentInputToStdIn();

        threadControlBlockObject.setOutputFile(threadStartInfoObject.getOutputFile(), threadStartInfoObject
                .getOutputFileName(), threadStartInfoObject.getIsStdOut());
        threadControlBlockObject.setAuthentication(threadStartInfoObject.getAuthentication());
        threadControlBlockObject.setUser(threadStartInfoObject.getUser());
        threadControlBlockObject.setPassword(threadStartInfoObject.getPassword());
        threadControlBlockObject.setLocale(threadStartInfoObject.getLocale());
        threadControlBlockObject.setServer(threadStartInfoObject.getServer());
        if(currentRpcQueue != 0)
        {
        	try {
        		getControlStructObject().usePrivateRpcQueue(currentRpcQueue);
        	} catch (ARException e) {
        		outputWriter.printARException(e);
        	}
        }
        
        threadControlBlockObject.setPrimaryThread(false);

    	// Release the main thread that started this thread.
        SyncObject waitObject = threadStartInfoObject.getWaitObject();
        synchronized (waitObject) {
            waitObject.setFlag(true);
            waitObject.notify();
        }
        SyncObject releaseObject = threadStartInfoObject.getReleaseObject();
        releaseObject.setFlag(true);
        synchronized (releaseObject) {
            try {
                int rpcUsed = currentRpcQueue;
                while (true) {
                    releaseObject.wait();
                    if (releaseObject.getFlag() == false)
                    	break;
                    if(rpcUsed != currentRpcQueue) {
                        try {
                	        getControlStructObject().usePrivateRpcQueue(currentRpcQueue);
                        } catch (ARException e) {
                            outputWriter.printARException(e);
                        }
                    }
                    wfdExec( Constants.WFD_EXECUTE_RUN );
                    wfdGetCurrentLocation();
                    rpcUsed = getControlStructObject().getServerRpcQueueNumber();
                }
            } catch (InterruptedException e) {
                releaseObject.setFlag(false);
                System.out.println("Interrupted Exception ...in simplethread run");
            }
        }

        // Cleanup the thread start up information
        threadStartInfoObject.cleanUp();
        threadStartInfoObject = null;
    }

    public synchronized void run() {
        boolean unexpectedError = false;
        ThreadControlBlock threadControlBlockPtr = getThreadControlBlockPtr();
        try {
            if (threadControlBlockPtr == null) {
                threadControlBlockPtr = initThreadControlBlockPtr();
            }
            setQuietMode( SUPPRESS_HEADERS | SUPPRESS_FOR_WFD );
            threadControlBlockPtr.setCurrentInputToStdIn();
            threadControlBlockPtr.setOutputToStdOut();
            if (this.isPrimaryThread()) {
                threadControlBlockPtr.setPrimaryThread(true);
                if (!processCommandLine()) {
                    System.exit(1);
                }
                outputWriter.driverPrintHelp();
                initCommandProcessing();
	            launchThread(true);
	            processCommands();
            } else {
                threadControlBlockPtr.setPrimaryThread(false);
                threadStartFunction();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            outputWriter.printString("IOException caught\n");
        } catch (Exception e) {
            e.printStackTrace();
            outputWriter.printString("Exception caught" + e.getLocalizedMessage() + "\n");
            System.out.println(e.getLocalizedMessage());
            unexpectedError = true;
        } finally {
            try {
            	terminateRunThread();
                destroyThreadControlBlockPtr(unexpectedError);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                termCommandProcessing();
                if (this.isPrimaryThread()) {
                    cleanupThreadEnvironment();
                }
                if (this.isPrimaryThread()) {
                    System.exit(0);
                }
            }
        }
    }

    protected void destroyThreadControlBlockPtr(boolean noWait) throws IOException {
        ThreadControlBlock threadControlBlockObject = getThreadControlBlockPtr();
        try {
            threadControlBlockObject.closeOutputFile();
            threadControlBlockObject.closeInputFiles();
            threadControlBlockObject.closeRecordFile();

            // Wait for all the child threads to complete their execution
            Thread childThread = (Thread) threadControlBlockObject.getThreadHandle();
            while (childThread != null) {
                if (!noWait && childThread.isAlive()) {
                    try {
                        Thread.sleep((long) 30);
                    } catch (InterruptedException e) {
                    }
                } else {
                    threadControlBlockObject.deleteThreadHandle();
                    childThread = (Thread) threadControlBlockObject.getThreadHandle();
                }
            }
        } finally {
            // clean up the event objects
            threadControlBlockObject.setWaitObjectToNull();
            threadControlBlockObject.setReleaseObjectToNull();
        }

    }

    public static void main(String[] args) {
        try {
            ProxyManager.setUseConnectionPooling(false);
            WFD wfd = new WFD();
            wfd.setCommandLineArgs(args);
            wfd.setPrimaryThread(true);
            wfd.start();
        } catch (Exception e) {
            outputWriter.printString("Error in executing the command\n");
        }
    }

    void termination() {
        getControlStructObject().logout();
    }

    void setServerPort() {
        try {
        	if( notWfdQuiet() )
        		outputWriter.driverPrintHeader("SET SERVER PORT");

            // Get the port number
            int portNumber = InputReader.getInt("The port number of server (0):", 0);

            // Get the rpc number
            int progNumber = InputReader.getInt("The RPC program number of Server (0):", 0);

            // Call ARSetServerPort method
            ARServerUser server = getControlStructObject();
            server.setPort(portNumber);
            server.usePrivateRpcQueue(progNumber);
            currentRpcQueue = progNumber;

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Set Server Port Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void wfdExec( int mode ) {
    	try {
    		int result[] = getControlStructObject().wfdExecute(mode);
    		switch( result[0] ) 
    		{
    		case Constants.WFD_EXEC_STEPPED:
    			outputWriter.print( "", "", "Workflow Step" );
    			break;
    		case Constants.WFD_EXEC_BRK_POINT:
    			outputWriter.print( "", "", ("Server Breakpoint Encountered: #" + result[1]) );
    			break;
    		case Constants.WFD_EXEC_USER_BRK:
    			outputWriter.print( "", "", "User Break\n" );
    			break;
    		default:
    			List<StatusInfo> statusList = getControlStructObject().getLastStatus();
    			outputWriter.printStatusInfoList("", "WFD Execute Status", statusList);
    			break;
    		}

    	} catch (ARException e) {
    		endAPICall(getControlStructObject().getLastStatus());
    		outputWriter.printARException(e);
    	}
    }

    void wfdInitialization() {
        outputWriter.print("", "", "Initialization.\nOK\n");
    }

    void wfdRestart() {
        outputWriter.print("", "", "Restart.\nOK\n");
    }

    void notImplementedYet() {
        outputWriter.print("", "", "Sorry not implemented yet.\n");
    }

    void wfdFinishAPI() {
        int saveMode;

        try {
            /* get old debug mode */
            saveMode = getControlStructObject().wfdGetDebugMode();

            /* set debug mode to 0 (don't stop */
            getControlStructObject().wfdSetDebugMode(0);

            /* run */
            while (true) {
                getControlStructObject().wfdExecute(Constants.WFD_EXECUTE_STEP);
                WfdDebugLocation location = getControlStructObject().wfdGetDebugLocation(0);
                if (location.Stage == WfdDebugLocation.WFD_IDLE)
                    break;
                else if (location.Stage == WfdDebugLocation.WFD_RUNNING)
                    continue;
            }
            /* put the old debug mode back */
            getControlStructObject().wfdSetDebugMode(saveMode);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
        return;
    }

    void wfdGetStackLocation() {
        WfdDebugLocation currentLocation = null;
        try {
        	if( notWfdQuiet() )
        		outputWriter.driverPrintHeader("WFD GET CURRENT LOCATION");

            // Get the stack depth requested
            int depth = InputReader.getInt("Stack frame ? (0): ", 0);

            // Call ARWfdGetCurrentLocation method
            currentLocation = getControlStructObject().wfdGetDebugLocation(depth);

            outputWriter.printCurrentLocation("Current Location:\n", currentLocation);

            // Success. Print the status
            //List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            //outputWriter.printStatusInfoList("", "Wfd Get Current Location Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

	void releaseRunThread() {

		ThreadControlBlock threadControlBlockObject = getThreadControlBlockPtr();

		SyncObject releaseObject = threadControlBlockObject.getReleaseObject();
		synchronized (releaseObject) {
			releaseObject.setFlag(true);
			releaseObject.notifyAll();
		}
	}

	void terminateRunThread() {

		ThreadControlBlock threadControlBlockObject = getThreadControlBlockPtr();

		SyncObject releaseObject = threadControlBlockObject.getReleaseObject();
		synchronized (releaseObject) {
			releaseObject.setFlag(false);
			releaseObject.notifyAll();
		}
	}

    void wfdDumpStack()

    {
        int                      depth;
        WfdDebugLocation       entry;
        Stack<WfdDebugLocation> myStack = new Stack<WfdDebugLocation>();

        try {
            for( depth = 0; true; depth++ )
            {
                entry = getControlStructObject().wfdGetDebugLocation(depth);
                myStack.push(entry);
                if( entry.Stage == WfdDebugLocation.WFD_IDLE )
                    break;
            }
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }

        /* Print results */
        outputWriter.driverPrintHeader("Current Stack:");
        while( ! myStack.isEmpty() ) {
            entry = myStack.pop();
            outputWriter.printCurrentLocation("Current Location:\n", entry);
        }
        return;   
    }
}

