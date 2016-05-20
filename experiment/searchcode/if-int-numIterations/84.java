package com.bmc.arsys.demo.javadriver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.bmc.arsys.api.*;

public class JavaDriver extends Thread {
    boolean executeCommand = true;

    static int quietMode = 0;
    static int randomNumberSeed;
    static String resultDirectory;
    boolean primaryThread;
    static String[] commandLineArgs = null;
    static int maxConnectionPerServer = 250;
    static int idleConnectionsPerServer = 5;
    static long connectionTimeoutDefault = 0;  
    static long connectionLifespanDefault = 0;
    static String connectionTimeUnit = "MINUTES";
    static ThreadControlLocalStorage localStorage = null;
    static RandomNumberThread randomNumberGenerator = null;
    static CommandProcessor timerCommandProcessor = new CommandProcessor();

    static OutputWriter outputWriter = new OutputWriter();
    ThreadStartInfo threadStartInfoObject = null;
    static boolean javaDriverOnly = true;

    static int SUPPRESS_RESULTS = 0x0001;
    static int SUPPRESS_HEADERS = 0x0002;
    static int SUPPRESS_PROMPTS = 0x0004;
    static int SUPPRESS_MENU = 0x0008;
    static int SUPPRESS_ERRORS = 0x0010;
    static int SUPPRESS_WARNINGS = 0x0020;
    static int MAX_NESTED_LOOP_DEPTH = 10;
    static int RAND_MAX = 0x7fff;

    void printAuxillaryStatus() {
        //do nothing
    }

    public void initCommandProcessing() {
        launchRandomNumberThread();
    }

    public void termCommandProcessing() {
        // process if we have special output requirements
    }

    public void beginAPICall() {
        //do nothing
    }

    public void endAPICall(List<StatusInfo> statusList) {
        //do nothing
    }

    protected void logResult(List<StatusInfo> statusList, long startTime) throws IOException {
        //do nothing
    }

    public static int getRandomNumber() {
        if (randomNumberGenerator != null) {
            return randomNumberGenerator.getRandomNumber();
        }

        return 0;
    }

    public void cleanupThreadEnvironment() {
        if (randomNumberGenerator != null) {
            randomNumberGenerator.interrupt();
        }

        randomNumberGenerator.setWaitObjToNull();

        randomNumberGenerator = null;
    }

    public void setThreadStartInfo(ThreadStartInfo infoObject) {
        threadStartInfoObject = infoObject;
    }

    public ARServerUser getControlStructObject() {
        ThreadControlBlock threadControlBlockPtr = getThreadControlBlockPtr();
        return threadControlBlockPtr.getContext();
    }

    public void launchRandomNumberThread() {
        randomNumberGenerator = new RandomNumberThread(randomNumberSeed);
        randomNumberGenerator.start();
    }

    public static ThreadControlBlock getThreadControlBlockPtr() {
        if (localStorage == null)
            return null;
        return (ThreadControlBlock) localStorage.get();
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

    int getNextCommand(StringBuilder args) throws IOException {
        ThreadControlBlock threadControlBlockPtr = getThreadControlBlockPtr();
        threadControlBlockPtr.args = null;
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
                commandCode = Commands.COMMAND_EXIT;
            } else {
                commandCode = Commands.getCommandCode(command);
                if (commandCode != Commands.UNKNOWN_COMMAND) {
                    threadControlBlockPtr.setCurrentCommand(command);
                } else {
                    outputWriter.driverPrintError(" *** Command not recognized ***\n");
                    outputWriter.driverPrintPrompt("\nCommand: ");
                }
            }
        }
        if ((threadControlBlockPtr.args != null) && (threadControlBlockPtr.args.length() > 0)
                && !threadControlBlockPtr.args.equals("null"))
            args.append(threadControlBlockPtr.args);
        return commandCode;
    }

    public void processCommands() throws IOException {
        int commandCode = Commands.COMMAND_EXIT; /* code for the command requested */
        StringBuilder argsBuf = new StringBuilder(); /* pointer to arguments */

        // process commands until exit specified
        while ((commandCode = getNextCommand(argsBuf)) != Commands.COMMAND_EXIT) {
            String args = (argsBuf.toString().length() > 0) ? argsBuf.toString() : null;
            argsBuf = new StringBuilder();
            switch (commandCode) {
            case Commands.COMMAND_LOGIN:
                getARServerUser();
                break;
            case Commands.COMMAND_GET_ENTRY:
                getEntry();
                break;
            case Commands.COMMAND_SET_ENTRY:
                setEntry();
                break;
            case Commands.COMMAND_CREATE_ENTRY:
                createEntry();
                break;
            case Commands.COMMAND_DELETE_ENTRY:
                deleteEntry();
                break;
            case Commands.COMMAND_GETLIST_ENTRY:
                getListEntry();
                break;
            case Commands.COMMAND_COMPACT_GETLIST_ENTRY:
            case Commands.COMMAND_GETLIST_ENTRY_WITH_FIELDS:
                getListEntryWithFields();
                break;
            case Commands.COMMAND_GETLIST_ENTRY_BLOCKS:
                getListEntryBlocks();
                break;
            case Commands.COMMAND_GET_FILTER:
                getFilter();
                break;
            case Commands.COMMAND_SET_FILTER:
                setFilter();
                break;
            case Commands.COMMAND_CREATE_FILTER:
                createFilter();
                break;
            case Commands.COMMAND_DELETE_FILTER:
                deleteFilter();
                break;
            case Commands.COMMAND_GETLIST_FILTER:
                getListFilter();
                break;
            case Commands.COMMAND_GETMULT_FILTER:
                getMultipleFilters();
                break;
            case Commands.COMMAND_GET_ESCALATION:
                getEscalation();
                break;
            case Commands.COMMAND_SET_ESCALATION:
                setEscalation();
                break;
            case Commands.COMMAND_CREATE_ESCALATION:
                createEscalation();
                break;
            case Commands.COMMAND_DELETE_ESCALATION:
                deleteEscalation();
                break;
            case Commands.COMMAND_GETLIST_ESCALATION:
                getListEscalation();
                break;
            case Commands.COMMAND_GETMULT_ESCALATION:
                getMultipleEscalations();
                break;
            case Commands.COMMAND_GETLIST_GROUP:
                getListGroup();
                break;
            case Commands.COMMAND_GET_SCHEMA:
                getForm();
                break;
            case Commands.COMMAND_SET_SCHEMA:
                setForm();
                break;
            case Commands.COMMAND_CREATE_SCHEMA:
                createForm();
                break;
            case Commands.COMMAND_DELETE_SCHEMA:
                deleteForm();
                break;
            case Commands.COMMAND_GETLIST_SCHEMA:
                getListForm();
                break;
            case Commands.COMMAND_GETMULT_SCHEMA:
                getMultipleForms();
                break;
            case Commands.COMMAND_GET_SCH_FIELD:
                getField();
                break;
            case Commands.COMMAND_SET_SCH_FIELD:
                setField();
                break;
            case Commands.COMMAND_SETMULT_SCH_FIELD:
                setMultipleFields();
                break;
            case Commands.COMMAND_CREATE_SCH_FIELD:
                createField();
                break;
            case Commands.COMMAND_CREATEMULT_SCH_FIELD:
                createMultipleFields();
                break;
            case Commands.COMMAND_DELETE_SCH_FIELD:
                deleteField();
                break;
            case Commands.COMMAND_GETLIST_SCH_FIELD:
                getListField();
                break;
            case Commands.COMMAND_GET_CHAR_MENU:
                getCharMenu();
                break;
            case Commands.COMMAND_SET_CHAR_MENU:
                setCharMenu();
                break;
            case Commands.COMMAND_CREATE_CHAR_MENU:
                createCharMenu();
                break;
            case Commands.COMMAND_DELETE_CHAR_MENU:
                deleteCharMenu();
                break;
            case Commands.COMMAND_GETLIST_CHAR_MENU:
                getListCharMenu();
                break;
            case Commands.COMMAND_GETMULT_CHAR_MENU:
                getMultipleCharMenu();
                break;
            case Commands.COMMAND_GET_VUI:
                getVUI();
                break;
            case Commands.COMMAND_SET_VUI:
                setVUI();
                break;
            case Commands.COMMAND_CREATE_VUI:
                createVUI();
                break;
            case Commands.COMMAND_DELETE_VUI:
                deleteVUI();
                break;
            case Commands.COMMAND_GETLIST_VUI:
                getListVUI();
                break;
            case Commands.COMMAND_EXPORT:
                export();
                break;
            case Commands.COMMAND_IMPORT:
                arImport();
                break;
            case Commands.COMMAND_GET_SERVER_INFO:
                getServerInfo();
                break;
            case Commands.COMMAND_VERIFY_USER:
                verifyUser();
                break;
            case Commands.COMMAND_EXECUTE:
                openInputFile(args);
                break;
            case Commands.COMMAND_OPEN_OUT:
                openOutputFile();
                break;
            case Commands.COMMAND_CLOSE_OUT:
                closeOutputFile();
                break;
            case Commands.COMMAND_RECORD:
                startRecording();
                break;
            case Commands.COMMAND_STOP_RECORD:
                stopRecording();
                break;
            case Commands.COMMAND_LAUNCH_THREAD:
                launchThread(false);
                break;
            case Commands.COMMAND_LAUNCH_WAITING_THREAD:
                launchThread(true);
                break;
            case Commands.COMMAND_RELEASE_WAITING_THREADS:
                timerCommandProcessor.releaseWaitingThreads();
                break;
            case Commands.COMMAND_SLEEP_TIMER:
                timerCommandProcessor.sleepTimer();
                break;
            case Commands.COMMAND_RANDOM_SLEEP_TIMER:
                timerCommandProcessor.randomSleepTimer();
                break;
            case Commands.COMMAND_MILLISECOND_SLEEP_TIMER:
                timerCommandProcessor.millisecondSleepTimer();
                break;
            case Commands.COMMAND_BEGIN_LOOP:
                beginLoop();
                break;
            case Commands.COMMAND_END_LOOP:
                endLoop();
                break;
            case Commands.COMMAND_INITIALIZATION:
                //initialization();
                break;
            case Commands.COMMAND_TERMINATION:
                termination();
                break;
            case Commands.COMMAND_GET_ACTIVE_LINK:
                getActiveLink();
                break;
            case Commands.COMMAND_SET_ACTIVE_LINK:
                setActiveLink();
                break;
            case Commands.COMMAND_CREATE_ACTIVE_LINK:
                createActiveLink();
                break;
            case Commands.COMMAND_DELETE_ACTIVE_LINK:
                deleteActiveLink();
                break;
            case Commands.COMMAND_GETLIST_ACTIVE_LINK:
                getListActiveLink();
                break;
            case Commands.COMMAND_GET_MULTIPLE_ACTIVE_LINKS:
                getMultipleActiveLinks();
                break;
            case Commands.COMMAND_MERGE_ENTRY:
                mergeEntry();
                break;
            case Commands.COMMAND_LOAD_AR_QUAL_STRUCT:
                getQualifier();
                break;
            case Commands.COMMAND_EXPAND_CHAR_MENU:
                expandCharMenu();
                break;
            case Commands.COMMAND_SET_SERVER_INFO:
                setServerInfo();
                break;
            case Commands.COMMAND_GETLIST_USER:
                getListUser();
                break;
            case Commands.COMMAND_ENTRY_STATISTICS:
                getEntryStatistics();
                break;
            case Commands.COMMAND_GET_SERVER_STAT:
                getServerStatistics();
                break;
            case Commands.COMMAND_GETLIST_SQL:
                getListSQL();
                break;
            case Commands.COMMAND_DELETE_MULTI_FIELD:
                deleteMultipleFields();
                break;
            case Commands.COMMAND_EXECUTE_PROCESS:
                executeProcess();
                break;
            case Commands.COMMAND_SET_SERVER_PORT:
                setServerPort();
                break;
            case Commands.COMMAND_GET_MULTIPLE_ENTRY:
                getMultipleEntries();
                break;
            case Commands.COMMAND_GET_SUPPORT_FILE:
                getSupportFile();
                break;
            case Commands.COMMAND_SET_SUPPORT_FILE:
                setSupportFile();
                break;
            case Commands.COMMAND_CREATE_SUPPORT_FILE:
                createSupportFile();
                break;
            case Commands.COMMAND_DELETE_SUPPORT_FILE:
                deleteSupportFile();
                break;
            case Commands.COMMAND_GETLIST_SUPPORT_FILE:
                getListSupportFile();
                break;
            case Commands.COMMAND_GETENTRY_BLOB:
                getEntryBLOB();
                break;
            case Commands.COMMAND_GET_CONTAINER:
                getContainer();
                break;
            case Commands.COMMAND_SET_CONTAINER:
                setContainer();
                break;
            case Commands.COMMAND_CREATE_CONTAINER:
                createContainer();
                break;
            case Commands.COMMAND_DELETE_CONTAINER:
                deleteContainer();
                break;
            case Commands.COMMAND_GETLIST_CONTAINER:
                getListContainer();
                break;
            case Commands.COMMAND_GET_MULTIPLE_CONTAINER:
                getMultipleContainers();
                break;
            case Commands.COMMAND_GET_ERROR_MESSAGE:
                getTextForErrorMessage();
                break;
            case Commands.COMMAND_SET_LOGGING:
                setLogging();
                break;
            case Commands.COMMAND_CLOSE_NET_CONNECTIONS:
                //closeNetworkConnections();
                break;
            case Commands.COMMAND_SIGNAL:
                //signal();
                break;
            case Commands.COMMAND_VALIDATE_FORM_CACHE:
                getvalidateFormCache();
                break;
            case Commands.COMMAND_GET_MULTIPLE_FIELDS:
                getMultipleFields();
                break;
            case Commands.COMMAND_GET_LOCALIZED_VALUE:
                getLocalizedValue();
                break;
            case Commands.COMMAND_GET_MULT_LOCALIZED_VALUES:
                getMultipleLocalizedValues();
                break;
            case Commands.COMMAND_GETLIST_SCHEMA_WITH_ALIAS:
                getListFormWithAlias();
                break;
            case Commands.COMMAND_CREATE_ALERT_EVENT:
                createAlertEvent();
                break;
            case Commands.COMMAND_REGISTER_ALERTS:
                registerForAlerts();
                break;
            case Commands.COMMAND_DEREGISTER_ALERTS:
                deregisterForAlerts();
                break;
            case Commands.COMMAND_GETLIST_ALERT_USER:
                getListAlertUser();
                break;
            case Commands.COMMAND_GET_ALERT_COUNT:
                getAlertCount();
                break;
            case Commands.COMMAND_DECODE_ALERT_MESSAGE:
                decodeAlertMessage();
                break;
            case Commands.COMMAND_ENCODE_QUALIFIER:
                encodeARQualifierStruct();
                break;
            case Commands.COMMAND_DECODE_QUALIFIER:
                decodeARQualifierStruct();
                break;
            case Commands.COMMAND_ENCODE_ASSIGN:
                encodeARAssignStruct();
                break;
            case Commands.COMMAND_DECODE_ASSIGN:
                decodeARAssignStruct();
                break;
            case Commands.COMMAND_ENCODE_HISTORY:
                encodeStatusHistory();
                break;
            case Commands.COMMAND_ENCODE_DIARY:
                encodeDiary();
                break;
            case Commands.COMMAND_GETLIST_EXT_SCHEMA_CANDS:
                getListExtFormCandidates();
                break;
            case Commands.COMMAND_GET_MULT_EXT_FIELD_CANDS:
                getMultipleExtFieldCandidates();
                break;
            case Commands.COMMAND_EXPAND_SS_MENU:
                //expandSSMenu();
                break;
            case Commands.COMMAND_VALIDATE_LICENSE:
                validateLicense();
                break;
            case Commands.COMMAND_VALIDATE_MULTIPLE_LICENSES:
                validateMultipleLicenses();
                break;
            case Commands.COMMAND_GETLIST_LICENSE:
                getListLicense();
                break;
            case Commands.COMMAND_CREATE_LICENSE:
                //createLicense();
                break;
            case Commands.COMMAND_DELETE_LICENSE:
                //deleteLicense();
                break;
            case Commands.COMMAND_GETLIST_SQL_FOR_AL:
                getListSQLForActiveLink();
                break;
            case Commands.COMMAND_EXECUTE_PROCESS_FOR_AL:
                executeProcessForActiveLink();
                break;
            case Commands.COMMAND_DRIVER_VERSION:
                printDriverVersion();
                break;
            case Commands.COMMAND_GET_SESSION_CONFIGURATION:
                getSessionConfiguration();
                break;
            case Commands.COMMAND_SET_SESSION_CONFIGURATION:
                setSessionConfiguration();
                break;
            case Commands.COMMAND_ENCODE_DATE:
                //dateToJulianDate();
                break;
            case Commands.COMMAND_DECODE_DATE:
                //julianDateToDate();
                break;
            case Commands.COMMAND_XML_CREATE_ENTRY:
                xmlCreateEntry();
                break;
            case Commands.COMMAND_XML_GET_ENTRY:
                xmlGetEntry();
                break;
            case Commands.COMMAND_XML_SET_ENTRY:
                xmlSetEntry();
                break;
            case Commands.COMMAND_GET_MULT_CURR_RATIO_SETS:
                getMultipleCurrencyRatioSets();
                break;
            case Commands.COMMAND_GET_CURRENCY_RATIO:
                getCurrencyRatio();
                break;
            case Commands.COMMAND_GET_CLIENT_TYPE:
                //getClientType();
                break;
            case Commands.COMMAND_SET_CLIENT_TYPE:
                setClientType();
                break;
            case Commands.COMMAND_GET_MULTIPLE_ENTRYPOINTS:
                getMultipleEntryPoints();
                break;
            case Commands.COMMAND_GET_LIST_ROLE:
                getListRole();
                break;
            case Commands.COMMAND_BEGIN_BULK_ENTRY_TRANS:
                beginBulkEntryTransaction();
                break;
            case Commands.COMMAND_END_BULK_ENTRY_TRANS:
                endBulkEntryTransaction();
                break;
            case Commands.COMMAND_DUMP_PROXY_INFO:
                dumpProxyInfo();
                break;
            case Commands.COMMAND_USE_CONNECTION_POOLING:
                setUseConnectionPooling();
                break;
            case Commands.COMMAND_SET_CONNECTION_LIMIT_PER_SERVER:
                setConnectionLimitPerServer();
                break;
            case Commands.COMMAND_ADJUST_CONNECTION_PARAMS_PER_SERVER:
                adjustConnectionParametersPerServer();
                break;
            case Commands.COMMAND_SET_IMPERSONATED_USER:
                setImpersonatedUser();
                break;
            case Commands.COMMAND_GET_SERVER_CHARSET:
                getServerCharSet();
                break;
            case Commands.COMMAND_BEGIN_API_RECORDING:
                beginApiRecording();
                break;
            case Commands.COMMAND_STOP_API_RECORDING:
                stopApiRecording();
                break;
            case Commands.COMMAND_EXECUTE_SERVICE:
                executeService();
                break;
            case Commands.COMMAND_GETLIST_APP_STATE:
                getListApplicationState();
                break;
            case Commands.COMMAND_GET_APP_STATE:
                getApplicationState();
                break;
            case Commands.COMMAND_SET_APP_STATE:
                setApplicationState();
                break;
            case Commands.COMMAND_XML_EXECUTE_SERVICE:
                xmlExecuteService();
                break;
            case Commands.COMMAND_GET_OBJ_CHANGE_TIMES:
                getObjectChangeTimes();
                break;
            case Commands.COMMAND_GET_IMAGE:
                getImage();
                break;
            case Commands.COMMAND_GETMULT_IMAGE:
                getMultipleImages();
                break;
            case Commands.COMMAND_SET_IMAGE:
                setImage();
                break;
            case Commands.COMMAND_CREATE_IMAGE:
                createImage();
                break;
            case Commands.COMMAND_DELETE_IMAGE:
                deleteImage();
                break;
            case Commands.COMMAND_GETLIST_IMAGE:
                getListImage();
                break;
            case Commands.COMMAND_DELETE_ALERT:
                deleteAlert();
                break;
            case Commands.COMMAND_WFD_GET_DEBUG_LOCATION:
                wfdGetDebugLocation();
                break;
            case Commands.COMMAND_WFD_EXECUTE:
                wfdExecute();
                break;
            case Commands.COMMAND_WFD_GET_FIELD_VALUES:
                wfdGetFieldValues();
                break;
            case Commands.COMMAND_WFD_SET_FIELD_VALUES:
                wfdSetFieldValues();
                break;
            case Commands.COMMAND_WFD_GET_DEBUG_MODE:
                wfdGetDebugMode();
                break;
            case Commands.COMMAND_WFD_SET_DEBUG_MODE:
                wfdSetDebugMode();
                break;
            case Commands.COMMAND_WFD_GET_FILTER_QUAL:
                wfdGetFilterQual();
                break;
            case Commands.COMMAND_WFD_SET_QUALIFIER_RESULT:
                wfdSetQualifierResult();
                break;
            case Commands.COMMAND_CLIENT_MANAGED_BEGIN_TXN:
                beginClientManagedTransaction();
                break;  
            case Commands.COMMAND_CLIENT_MANAGED_END_TXN:
                endClientManagedTransaction();
                break;   
            case Commands.COMMAND_CLIENT_MANAGED_SET_TXN:
                setClientManagedTransaction();
                break;   
            case Commands.COMMAND_CLIENT_MANAGED_REMOVE_TXN:
                removeClientManagedTransaction();
                break;                  
            case Commands.COMMAND_WFD_TERMINATE_API:
                wfdTerminateAPI();
                break;
            case Commands.COMMAND_RUN_ESCALATION:            
                runEscalation();
                break;
            case Commands.COMMAND_GETONE_ENTRY_WITH_FIELDS:
            	getOneEntryWithFields();
            	break;
            case Commands.COMMAND_GETLIST_ENTRY_WITH_MS_FIELDS:
            	getListEntryWithMultiSchemaFields();
            	break;
            case Commands.COMMAND_GET_CACHE_EVENT:
            	getCacheEvent();
            	break;
            case Commands.COMMAND_SET_GET_ENTRY:
                setGetEntry();
                break;
            case Commands.COMMAND_CREATE_OVERLAY:
            	createOverlay();
            	break;
            case Commands.COMMAND_GET_MULT_VIEWS:
            	getMultipleVUI();
            	break;
            case Commands.COMMAND_SET_OVERLAY_GROUPNAME:
            	setOverlayGroup();
            	break;
            case Commands.COMMAND_CREATE_OVERLAY_FROM_OBJECT:
            	createOverlayFromObject();
            	break;
            case Commands.COMMAND_VERIFY_USER2:
                verifyUser2();
                break;
            default:
                outputWriter.driverPrintNotSupportCommand(commandCode);
                break;
            }
        }
    }

    void setClientType() {
        getControlStructObject().setClientType(3);
    }

    boolean processCommandLine() {
        String[] argv = getCommandLineArgs();
        int maxArgumentLen = 0;
        char option;
        String tempPtr = null;

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
                            || argv[i].charAt(1) == 'c' || argv[i].charAt(1) == 'a' || argv[i].charAt(1) == 't'
                            || argv[i].charAt(1) == 'S' || argv[i].charAt(1) == 'P')) {

                option = argv[i].charAt(1);
            } else if (argv[i].equals("-version")) {
                   /* application is launched only for version info */
                   /* so quit the program after processing          */
                outputWriter.driverPrintHeader("Java Driver Version " + Version.clientVersion);
                return false;
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
                maxArgumentLen = Constants.AR_MAX_AUTH_SIZE;
                break;
            case 'u':
                maxArgumentLen = Constants.AR_MAX_ACCESS_NAME_SIZE;
                break;
            case 'p':
                maxArgumentLen = Constants.AR_MAX_PASSWORD_SIZE;
                break;
            case 'l':
                maxArgumentLen = 64;
                break;
            case 's':
                maxArgumentLen = Constants.AR_MAX_SERVER_SIZE;
                break;
            case 'x':
            case 'd':
                maxArgumentLen = Constants.AR_MAX_FULL_FILENAME;
                break;
            case 't':
                maxArgumentLen = Constants.AR_MAX_LANG_SIZE;
                break;
            case 'o':
            case 'q':
                maxArgumentLen = 3;
                break;
            case 'g':
                maxArgumentLen = 10;
                break;
            case 'c':
            case 'P':
                maxArgumentLen = 5;
                break;
            case 'S':
                maxArgumentLen = 6;
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

                int j = 0;
                for (j = 0; j < tempPtr.length(); j++) {
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

            case 't':
                threadControlBlockPtr.setTimeZone(tempPtr);
                break;

            case 's':
                threadControlBlockPtr.setServer(tempPtr);
                break;

            case 'P':
                threadControlBlockPtr.setPort(new Integer(tempPtr).intValue());
                break;

            case 'S':
                threadControlBlockPtr.setPrivateRpcQueue(new Integer(tempPtr).intValue());
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

    void createForm() {
        try {
            outputWriter.driverPrintHeader("CREATE SCHEMA");

            Form form = InputReader.getForm();
            String formName = InputReader.getString("Form Name: ");

            form.setName(formName);

            List<PermissionInfo> permissions = InputReader.getPermissionInfoList(false);

            form.setPermissions(permissions);

            outputWriter.driverPrintHeader("Ids of sub admin groups allowed to access form:");
            List<Integer> adminGroupList = InputReader.getIntegerList();
            form.setAdminGrpList(adminGroupList);

            List<EntryListFieldInfo> fldInfos = InputReader.getEntryListFieldInfoList();
            form.setEntryListFieldInfo(fldInfos);

            List<SortInfo> sortarr = InputReader.getSortInfoList();
            form.setSortInfo(sortarr);

            List<IndexInfo> index = InputReader.getIndexInfoList();
            form.setIndexInfo(index);

            if (InputReader.getBooleanForChangingInfo("Change archive info? (F): ", false)) {
                ArchiveInfo archive = InputReader.getArchiveInfo();
                form.setArchiveInfo(archive);
            }

            if (InputReader.getBooleanForChangingInfo("Change audit info? (F): ", false)) {
                AuditInfo auditInfo = InputReader.getAuditInfo();
                form.setAuditInfo(auditInfo);
            }

            String vuiName = InputReader.getString("   Default VUI  ");
            form.setDefaultVUI(vuiName);

            String helpText = InputReader.getString("Help Text:", "");
            form.setHelpText(helpText);

            String ownerID = InputReader.getString("Owner:");
            form.setOwner(ownerID);

            String diary = InputReader.getString("Change DiaryList:", "");
            form.appendDiaryText(diary);

            ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
            form.setProperties(propList);
            
            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();

            beginAPICall();
            getControlStructObject().createForm(form, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Create Form Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void deleteForm() {

        try {

            outputWriter.driverPrintHeader("DELETE SCHEMA");

            String formName = InputReader.getString("Form Name: ");
            int deleteOption = InputReader.getInt("Delete option ? ( 0, 1, 2, 4, 8 ) ( 0 ):", 0);
            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();
            
            beginAPICall();
            getControlStructObject().deleteForm(formName, deleteOption, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Delete Form Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getForm() {
        try {

            String formName = InputReader.getString("Form Name:", "");
            			
            FormCriteria criteria = new FormCriteria();
            criteria.setRetrieveAll(true);
            
            beginAPICall();
            Form form = (Form) getControlStructObject().getForm(formName, criteria);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.print("", "Form Information...", form);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get Form Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getMultipleForms() {
        try {
            long changedSince = InputReader.getLong("Get All changed since, as UNIX epoch (0): ", 0);
            FormType formType = InputReader.getFormType();
            boolean hidden = InputReader.getBooleanForChangingInfo("Include hidden forms (T):", true);
            if (hidden && formType != null) {
                formType.setHiddenIncrement();
            }
            outputWriter.driverPrintPrompt("Ids of fields, which must be on the form:\n");
            int[] fieldIds = InputReader.getIntArray();
			
            beginAPICall();
            FormCriteria criteria = new FormCriteria();
            criteria.setRetrieveAll(true);

            List<Form> formList = null;
            if (formType != null) {
                formList = getControlStructObject().getListFormObjects(changedSince, formType.toInt(), null, fieldIds,
                        criteria);
            }
            endAPICall(getControlStructObject().getLastStatus());

            if (formList != null) {
                outputWriter.print("", "Total number of forms: ", formList.size());
                for (int i = 0; i < formList.size(); i++) {
                    Form form = formList.get(i);
                    outputWriter.print("    ", "Form: ", form);
                }
            }
            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "GetListForm Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListForm() {
        try {
            long changedSince = InputReader.getLong("Get All changed since, as UNIX epoch (0): ", 0);
            FormType formType = InputReader.getFormType();
            boolean includeHidden = InputReader.getBooleanForChangingInfo("Include hidden forms (T):", true);
            String formName = null;
            if ((formType != null) && (formType.equals(FormType.UPLINK) || formType.equals(FormType.DOWNLINK))) {
                formName = InputReader.getString("Form name (): ", "");
            } else
                formName = "";
            
            int intFormType = (includeHidden) ? (formType.toInt() | Constants.AR_HIDDEN_INCREMENT) : formType.toInt();
            
            outputWriter.driverPrintPrompt("Ids of fields, which must be on the form:\n");
            int[] fieldIds = InputReader.getIntArray();

            outputWriter.driverPrintPrompt("list of props to search for:\n");
            ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
            
            beginAPICall();
            List<String> formList = getControlStructObject().getListForm(changedSince, intFormType, formName,
                    fieldIds, propList);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.print("", "Schama name List: ", formList);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "GetListForm Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void setForm() {

        try {
            Form form = new RegularForm();
            if (InputReader.getBooleanForChangingInfo("Change compound form info? (F): ", false)) {
                form = InputReader.getForm();
            }
            String name = InputReader.getString("Form Name: ");
            form.setName(name);

            if (InputReader.getBooleanForChangingInfo("Change form name? (F): ", false)) {
                String newName = InputReader.getString("Form name: ");
                form.setNewName(newName);
            }

            if (InputReader.getBooleanForChangingInfo("Change group list? (F): ", false)) {
                List<PermissionInfo> permissions = InputReader.getPermissionInfoList(true);
                form.setPermissions(permissions);
            }

            if (InputReader.getBooleanForChangingInfo("Change sub admin group list? (F): ", false)) {
                outputWriter.driverPrintHeader("Ids of sub admin groups allowed to access form:");
                List<Integer> id = InputReader.getIntegerList();
                form.setAdminGrpList(id);
            }

            if (InputReader.getBooleanForChangingInfo("Change query list fields? (F): ", false)) {
                List<EntryListFieldInfo> fldInfos = InputReader.getEntryListFieldInfoList();
                form.setEntryListFieldInfo(fldInfos);
            }

            if (InputReader.getBooleanForChangingInfo("Change sort list? (F): ", false)) {
                List<SortInfo> sortarr = InputReader.getSortInfoList();
                form.setSortInfo(sortarr);
            }

            if (InputReader.getBooleanForChangingInfo("Change index list? (F): ", false)) {
                List<IndexInfo> index = InputReader.getIndexInfoList();
                form.setIndexInfo(index);
            }

            if (InputReader.getBooleanForChangingInfo("Change archive info? (F): ", false)) {
                ArchiveInfo archive = InputReader.getArchiveInfo();
                form.setArchiveInfo(archive);
            }

            if (InputReader.getBooleanForChangingInfo("Change audit info? (F): ", false)) {
                AuditInfo auditInfo = InputReader.getAuditInfo();
                form.setAuditInfo(auditInfo);
            }

            if (InputReader.getBoolean("Change default VUI? (F): ", false)) {
                String vuiName = InputReader.getString("   Default VUI  ");
                form.setDefaultVUI(vuiName);
            }

            if (InputReader.getBooleanForChangingInfo("Change help text? (F): ", false)) {
                String help = InputReader.getString("Help Text (): ", "");
                form.setHelpText(help);
            }

            if (InputReader.getBooleanForChangingInfo("Change owner? (F): ", false)) {
                String owner = InputReader.getString("New Owner");
                form.setOwner(owner);
            }

            if (InputReader.getBooleanForChangingInfo("Add to change diary? (F): ", false)) {
                String diary = InputReader.getString("Change DiaryList (): ", "");
                form.appendDiaryText(diary);
            }

            if (InputReader.getBooleanForChangingInfo("Change object properties? (F): ", false)) {
                ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
                form.setProperties(propList);
            }
            
            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();

            beginAPICall();
            getControlStructObject().setForm(form, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Set Form Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getEntry() {
        try {
            outputWriter.driverPrintHeader("GET ENTRY");

            // Get the Entry Key
            EntryKey entryKey = getAndConvertEntryKey();

            // Get the fields information
            int[] entryListFields = InputReader.getIntArray();

            beginAPICall();
            Entry entry = getControlStructObject().getEntry(entryKey.getFormName(), entryKey.getEntryID(),
                    entryListFields);
            List<StatusInfo> statusList1 = getControlStructObject().getLastStatus();
            endAPICall(statusList1);

            // If there were diary field values, we call diary.decode()
            // which calls ARDecodeDiary. This wipes out the previous
            // status. So we merge the two status together if needed
            outputWriter.printEntry("", "Entry Information:", entry);

            // Success. Print the status
            List<StatusInfo> statusList2 = getControlStructObject().getLastStatus();
            statusList1.addAll(statusList1.size(), statusList2);
            outputWriter.printStatusInfoList("", "Get Entry Status", statusList1);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    protected EntryKey getAndConvertEntryKey() throws IOException {
        return InputReader.getEntryKey();
    }

    void setEntry() {
        try {

            outputWriter.driverPrintHeader("SET  ENTRY");
            // Get the Entry Key
            EntryKey entryKey = InputReader.getEntryKey();

            // Get the entry items information
            outputWriter.driverPrintPrompt("Field/value pairs to set:\n");
            Entry entry = InputReader.getEntry();

            // Get the time stamp and entry option
            Timestamp getTime = new Timestamp(InputReader.getLong("Time of Get operation (0): ", 0));
            int option = InputReader.getInt("SetEntry option ? (0): ", 0);

            beginAPICall();
            getControlStructObject().setEntry(entryKey.getFormName(), entryKey.getEntryID(), entry, getTime, option);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "SetEntry Status", statusList);
        } catch (IOException x) {
            outputWriter.printString("Problem in getting the input for set entry...\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    public void createEntry() {

        Entry entry = null;

        try {
            outputWriter.driverPrintHeader("CREATE ENTRY");

            // Get the form name
            String formName = InputReader.getString("Form Name: ");

            // create entryItems
            outputWriter.driverPrintHeader("Field/value pairs to create:");
            entry = InputReader.getEntry();

            beginAPICall();
            getControlStructObject().createEntry(formName, entry);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printString("Entry id: " + entry.getEntryId());
            outputWriter.printNewLine();

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Create Entry Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void executeService() {
        try {
            outputWriter.driverPrintHeader("EXECUTE SERVICE");
            String formName = InputReader.getString("Form Name: ");
            String entryId = InputReader.getString("Entry ID:", null);
            outputWriter.driverPrintPrompt("Field/value pairs to set:\n");
            Entry entry = InputReader.getEntry();
            int[] entryListFields = InputReader.getIntArray();
            beginAPICall();
            entry = getControlStructObject().executeService(formName, entryId, entry, entryListFields);
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            endAPICall(statusList);
            outputWriter.printEntry("", "Entry Information:", entry);
            outputWriter.printStatusInfoList("", "executeService Status", statusList);
        } catch (IOException x) {
            outputWriter.printString("Problem in getting the input for executeService input entry...\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void xmlExecuteService() {
        try {
            outputWriter.driverPrintHeader("XML EXECUTE SERVICE");

            String queryMapping = InputReader.getFileContent("Filename containing XML query mapping: ", null, 3,
                    outputWriter);
            if (queryMapping == null)
                return;
            String queryDoc = InputReader.getFileContent("Filename containing XML query document: ", null, 3,
                    outputWriter);
            if (queryDoc == null)
                return;
            String inputMapping = InputReader.getFileContent("Filename containing XML input mapping: ", null, 3,
                    outputWriter);
            if (inputMapping == null)
                return;
            String inputDoc = InputReader.getFileContent("Filename containing XML input document: ", null, 3,
                    outputWriter);
            if (inputDoc == null)
                return;
            String outputMapping = InputReader.getFileContent("Filename containing XML output mapping: ", null, 3,
                    outputWriter);
            if (outputMapping == null)
                return;
            String optionDoc = InputReader.getFileContent("Filename containing XML option document: ", null, 3,
                    outputWriter);
            if (optionDoc == null)
                return;

            String xmlOutputDoc = null;

            beginAPICall();
            xmlOutputDoc = getControlStructObject().xmlExecuteService(queryMapping, queryDoc, inputMapping, inputDoc,
                    outputMapping, optionDoc);
            endAPICall(getControlStructObject().getLastStatus());

            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "XMLExecuteService Results", statusList);

            if (xmlOutputDoc != null) {
                outputWriter.printString(xmlOutputDoc);
            } else {
                outputWriter.printString("xmlOutputDoc is null...\n");
            }
        } catch (NullPointerException e) {
            outputWriter.printString("Null Pointer exception...\n");
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void deleteEntry() {
        try {
            outputWriter.driverPrintHeader("DELETE ENTRY");

            // Get and set the entry key
            EntryKey key = InputReader.getEntryKey();

            int deleteOption = InputReader.getInt("DeleteEntry option ? (0): ", 0);

            // Now remove the entry
            beginAPICall();
            getControlStructObject().deleteEntry(key.getFormName(), key.getEntryID(), deleteOption);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Delete Entry", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getMultipleEntries() {
        try {
            outputWriter.driverPrintHeader("GET MULTIPLE ENTRIES");

            // Get the form Name
            String formName = InputReader.getString("Form Name (): ", "");

            // Get Entry IDs to retrieve
            List<String> entryIdList = InputReader.getStringList("", "Entry Id");

            // Get the fields information
            outputWriter.driverPrintPrompt("Ids of fields to retrieve:\n");
            int[] fieldIdList = InputReader.getIntArray();

            beginAPICall();
            List<Entry> entries = getControlStructObject().getListEntryObjects(formName, entryIdList, fieldIdList);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.print("", "Entry List: ", entries);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "GetMultipleEntries Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListEntry() {
        try {
            outputWriter.driverPrintHeader("GETLIST ENTRY");

            String formName = InputReader.getString("Form Name (): ", "");
            QualifierInfo qualifier = InputReader.getQualifierInfo();
            List<EntryListFieldInfo> entryListFields = InputReader.getEntryListFieldInfoList();
            List<SortInfo> sortOrder = InputReader.getSortInfoList();
            int firstRetrieve = InputReader.getInt("firstRetrieve (0): ", 0);
            int limit = InputReader.getInt("Maximum Number of Entries to retrieve (500):", 500);

            OutputInteger matches = null;
            if (InputReader.getBooleanForChangingInfo("Get number of matches? (F): ", false)) {
                matches = new OutputInteger(0);
            }

            // Get and set the useLocale flag
            boolean useLocaleFlag = InputReader.getBooleanForChangingInfo("Use Locale Sensitive Search(F):", false);

            beginAPICall();
            List<EntryListInfo> entryInfoList = getControlStructObject().getListEntry(formName, qualifier,
                    firstRetrieve, limit, sortOrder, entryListFields, useLocaleFlag, matches);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.print("", "Entry List:", entryInfoList);
            if (matches != null)
                outputWriter.printInteger("", "Number of Matches:", matches);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "GetListEntry Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListEntryWithFields() {
        try {
            outputWriter.driverPrintHeader("GETLIST ENTRY WITH FIELDS");
            String formName = InputReader.getString("Form Name (): ", "");
            QualifierInfo qualifier = InputReader.getQualifierInfo();
            int[] fieldIdList = InputReader.getIntArray();
            List<SortInfo> sortOrder = InputReader.getSortInfoList();
            int firstRetrieve = InputReader.getInt("firstRetrieve (0): ", 0);
            int limit = InputReader.getInt("Maximum Number of Entries to retrieve (500):", 500);

            OutputInteger matches = null;
            if (InputReader.getBooleanForChangingInfo("Get number of matches? (F): ", false)) {
                matches = new OutputInteger(0);
            }

            // Get and set the useLocale flag
            boolean useLocaleFlag = InputReader.getBooleanForChangingInfo("Use Locale Sensitive Search(F):", false);

            beginAPICall();
            List<Entry> entries = getControlStructObject().getListEntryObjects(formName, qualifier, firstRetrieve,
                    limit, sortOrder, fieldIdList, useLocaleFlag, matches);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.print("", "Entry List: ", entries);
            if (matches != null)
                outputWriter.printInteger("", "Number of Matches:", matches);

            // get the control block pointer for the current thread
            ThreadControlBlock threadControlBlockObj = getThreadControlBlockPtr();
            threadControlBlockObj.resetListIds();
            
            // Save off the entry ids
            if (entries.size() > 0) {
                threadControlBlockObj.setFirstListId(entries.get(0).getEntryId());
                if (entries.size() > 1)
                    threadControlBlockObj.setSecondListId(entries.get(1).getEntryId());
                else
                    threadControlBlockObj.setSecondListId(entries.get(0).getEntryId());
                threadControlBlockObj.setLastListId(entries.get(entries.size() - 1).getEntryId());
            }

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "GetListEntryWithFields Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListEntryWithMultiSchemaFields() {
        try {
            outputWriter.driverPrintHeader("GETLIST ENTRY WITH MULTISCHEMA FIELDS");

            RegularQuery query = InputReader.getRegularQuery();
            int firstRetrieve = InputReader.getInt("First Entry to retrieve (0): ", 0);
            int limit = InputReader.getInt("Maximum Number of Entries to retrieve (500):", 500);

            OutputInteger matches = null;
            if (InputReader.getBooleanForChangingInfo("Get Number of Matches? (F): ", false)) {
                matches = new OutputInteger(0);
            }

            // Get and set the useLocale flag
            boolean useLocaleFlag = InputReader.getBooleanForChangingInfo("Use Locale Sensitive Search(F):", false);

            beginAPICall();
            List<QuerySourceValues> entries = getControlStructObject().getListEntryObjects(query, 
            		firstRetrieve, limit, useLocaleFlag, matches);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.print("", "Query Source Values List: ", entries);
            if (matches != null)
                outputWriter.printInteger("", "Number of Matches:", matches);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "GetListEntryWithMultiSchemaFields Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }
    
    void getOneEntryWithFields() {
        try {
            outputWriter.driverPrintHeader("GETONE ENTRY WITH FIELDS");
            String formName = InputReader.getString("Form Name (): ", "");
            QualifierInfo qualifier = InputReader.getQualifierInfo();
            List<EntryListFieldInfo> entryListFields = InputReader.getEntryListFieldInfoList();
            int numItems = (entryListFields == null ? 0 : entryListFields.size());
            int[] fieldIdList = new int[numItems];
            for (int i = 0; i < numItems; i++) {
                fieldIdList[i] = entryListFields.get(i).getFieldId();
            }
            List<SortInfo> sortOrder = InputReader.getSortInfoList();

            OutputInteger matches = null;
            if (InputReader.getBooleanForChangingInfo("Get number of matches? (F): ", false)) {
                matches = new OutputInteger(0);
            }

            // Get and set the useLocale flag
            boolean useLocaleFlag = InputReader.getBooleanForChangingInfo("Use Locale Sensitive Search(F):", false);

            beginAPICall();
            Entry entryObj = getControlStructObject().getOneEntryObject(formName, qualifier,
                    sortOrder, fieldIdList, useLocaleFlag, matches);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.print("", "Entry: ", entryObj);
            if (matches != null)
                outputWriter.printInteger("", "Number of Matches:", matches);

            // get the control block pointer for the current thread
            ThreadControlBlock threadControlBlockObj = getThreadControlBlockPtr();
            threadControlBlockObj.resetListIds();
            
            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "GetOneEntryWithFields Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }
    
    void getCacheEvent() {
    	try {
            outputWriter.driverPrintHeader("GET CACHE EVENT");
                       
            int[] cacheEventIds = InputReader.getIntArray();
            int returnOption = InputReader.getInt("Return Option: Immediate, Next (0-1) (0):",0);
            OutputInteger cacheCount = new OutputInteger(0);
            
            beginAPICall();
            List<Integer> cacheEventIdList = getControlStructObject().getCacheEvent(cacheEventIds, returnOption, cacheCount);
            endAPICall(getControlStructObject().getLastStatus());

            if (cacheEventIdList != null)
            	outputWriter.printInteger("", "Number of Events Occured:", cacheEventIdList.size());

            for (int i = 0; i < cacheEventIdList.size(); i++)
	            outputWriter.printInteger("", "Event ID :", cacheEventIdList.get(i));
            
            outputWriter.printInteger("", "Number of Matches:", cacheCount.intValue());
            
            // get the control block pointer for the current thread
            ThreadControlBlock threadControlBlockObj = getThreadControlBlockPtr();
            threadControlBlockObj.resetListIds();
            
            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "GetCacheEvent Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }
    void getListEntryBlocks() {
        try {
            outputWriter.driverPrintHeader("GETLIST ENTRY BLOCKS");

            String formName = InputReader.getString("Form Name (): ", "");
            QualifierInfo qualifier = InputReader.getQualifierInfo();
            List<SortInfo> sortOrder = InputReader.getSortInfoList();
            int firstRetrieve = InputReader.getInt("firstRetrieve (0): ", 0);
            int limit = InputReader.getInt("Maximum Number of Entries to retrieve (500):", 500);

            //Get the number of matches
            OutputInteger matches = null;
            if (InputReader.getBooleanForChangingInfo("Get number of matches? (F): ", false)) {
                matches = new OutputInteger(0);
            }

            // Get and set the useLocale flag
            boolean useLocaleFlag = InputReader.getBooleanForChangingInfo("Use Locale Sensitive Search(F):", false);

            beginAPICall();

            IARRowIterator ri = new RowIterator();

            getControlStructObject().getListEntryObjects(formName, qualifier, firstRetrieve, limit, sortOrder, null,
                    useLocaleFlag, matches, ri);

            endAPICall(getControlStructObject().getLastStatus());
            if (matches != null)
                outputWriter.printInteger("", "Number of Matches:", matches);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.printString("Null Pointer exception...\n");
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    public void mergeEntry() {
        try {
            outputWriter.driverPrintHeader("MERGE ENTRY");

            //Get the form name
            String form = InputReader.getString("Form Name: ");
            //create entryItems
            outputWriter.driverPrintHeader("Field/value pairs to merge:");
            Entry entry = InputReader.getEntry();

            int mergeType = InputReader.getInt(
                    "Operation on duplicate entry (error, new ID, overwrite, merge) (1-4): ", 4);
            QualifierInfo qualifier = InputReader.getQualifierInfo();
            int multimatchOption = InputReader.getInt("Multi Match Options (do nothing, change first)(0-1):",0);

            beginAPICall();
            getControlStructObject().mergeEntry(form, entry, mergeType, qualifier, multimatchOption);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printString("Entry id: " + entry.getEntryId());
            outputWriter.printNewLine();

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Merge Entry Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void createField() {

        try {
            outputWriter.driverPrintHeader("CREATE FIELD");

            // Set the form name
            String formName = InputReader.getString("Form Name: ");

            // Set the Field ID
            int id = InputReader.getInt("Field id (0): ", 0);

            // Set the reserved id
            boolean reservedID = InputReader.getBoolean("Create even if ID is reserved? (F): ", false);

            // Set the Field Name
            String fieldName = InputReader.getString("Field Name: ");

            // Get the Field datatype and create an empty field
            Field field = InputReader.getField();

			// Set values from other inputs
            field.setForm(formName);
            field.setFieldID(id);
            field.setReservedIDOK(reservedID);
            field.setName(fieldName);

            // Set the field map if necessary.

            // Set the option for entering the value
            int option = InputReader.getInt("Required, Optional, System, or Display-only (1-4) (2): ", 2);
            field.setFieldOption(option);

            // Set create mode
            int createMode = InputReader.getInt("Open or Protected at create (1 or 2) (1): ", 1);
            field.setCreateMode(createMode);

            // Set field option
            int auditOption = InputReader.getInt("Field option for Audit/Copy (0): ", 0);
            field.setAuditOption(auditOption);

            int type = field.getDataType();
            if (InputReader.getBooleanForChangingInfo("Have new default value? (F): ", false)) {
                Value val = InputReader.getValue(type);
                field.setDefaultValue(val);
            }

            // Set permissions
            List<PermissionInfo> permissions = InputReader.getPermissionInfoList(true);
            field.setPermissions(permissions);

            // Set limits
            if (InputReader.getBooleanForChangingInfo("Have field limits? (T): ", true)) {
                FieldLimit limitInfo = InputReader.getFieldLimitInfo(type);
                field.setFieldLimit(limitInfo);
            }

            // Set DisplayInstanceList
            DisplayInstanceMap dispInstanceList = InputReader.getDisplayInstanceMap();
            field.setDisplayInstance(dispInstanceList);

            // Set the help text
            String helpText = InputReader.getString("Help Text: ", "");
            field.setHelpText(helpText);

            // Set the owner
            String owner = InputReader.getString("Owner:");
            field.setOwner(owner);

            // Set Change DiaryList
            String diary = InputReader.getString("DiaryList: ", "");
            field.appendDiaryText(diary);

            // Set the field mapping structure
            FieldMapping mapInfo = InputReader.getFieldMappingInfo();
            field.setFieldMap(mapInfo);

            // Set Object properties           
            ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
            field.setObjectProperty(propList);                                   

            // Now create the field
            beginAPICall();
            getControlStructObject().createField(field, true);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "CreateField Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }
    
    void createMultipleFields()
    {
        try {
            outputWriter.driverPrintHeader("CREATE MULTIPLE FIELDS");
            
            String formName = InputReader.getString("Form name: ");
            
            int noOfFields = InputReader.getInt("Number of Fields (0): ", 0);
            
            List<Field> fields = new ArrayList<Field>(noOfFields);

            //populate each field
            for (int index = 0; index < noOfFields; index++) {
                outputWriter.driverPrintHeader("Enter information for field(" + (index + 1) + ")");
            
                // Set the Field ID
                int id = InputReader.getInt("Field id (0): ", 0);

                // Set the reserved id
                boolean reservedID = InputReader.getBooleanForChangingInfo("Create even if ID is reserved? (F): ", false);

                // Set the Field Name
                String fieldName = InputReader.getString("Field Name: ");

                // Get the Field datatype and create an empty field
                Field field = InputReader.getField();

                // Set values from other inputs
                field.setForm(formName);
                field.setFieldID(id);
                field.setReservedIDOK(reservedID);
                field.setName(fieldName);

                // Set the field map if necessary.

                // Set the option for entering the value
                int option = InputReader.getInt("Required, Optional, System, or Display-only (1-4) (2): ", 2);
                field.setFieldOption(option);

                // Set create mode
                int createMode = InputReader.getInt("Open or Protected at create (1 or 2) (1): ", 1);
                field.setCreateMode(createMode);

                // Set field option
                int auditOption = InputReader.getInt("Field option for Audit/Copy (0): ", 0);
                field.setAuditOption(auditOption);

                int type = field.getDataType();
                if (InputReader.getBooleanForChangingInfo("Have new default value? (F): ", false)) {
                    Value val = InputReader.getValue(type);
                    field.setDefaultValue(val);
                }

                // Set permissions
                List<PermissionInfo> permissions = InputReader.getPermissionInfoList(true);
                field.setPermissions(permissions);

                // Set limits
                if (InputReader.getBooleanForChangingInfo("Have field limits? (T): ", true)) {
                    FieldLimit limitInfo = InputReader.getFieldLimitInfo(type);
                    field.setFieldLimit(limitInfo);
                }

                // Set DisplayInstanceList
                DisplayInstanceMap dispInstanceList = InputReader.getDisplayInstanceMap();
                field.setDisplayInstance(dispInstanceList);

                // Set the help text
                String helpText = InputReader.getString("Help Text: ", "");
                field.setHelpText(helpText);

                // Set the owner
                String owner = InputReader.getString("Owner:");
                field.setOwner(owner);

                // Set Change DiaryList
                String diary = InputReader.getString("DiaryList: ", "");
                field.appendDiaryText(diary);

                // Set the field mapping structure
                FieldMapping mapInfo = InputReader.getFieldMappingInfo();
                field.setFieldMap(mapInfo);

                // Set Object properties           
                ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
                field.setObjectProperty(propList);                                   

                fields.add(field);
                // Now create the field
                
            }
            
            beginAPICall();
            getControlStructObject().createMultipleFields(fields);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Create Multiple Fields Status", statusList);
            
        }catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void deleteField() {
        try {
            outputWriter.driverPrintHeader("DELETE FIELD");

            // Set the form name
            String formName = InputReader.getString("Form Name: ");

            // Set the Field ID
            int fieldId = InputReader.getInt("Field id (0):", 0);

            // set the delete option
            int deleteOption = InputReader.getInt("Delete option - clean, data, cascade (0 - 2) (0): ", 0);

            // Now delete the field
            beginAPICall();
            getControlStructObject().deleteField(formName, fieldId, deleteOption);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "DeleteField Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void deleteMultipleFields() {
        try {
            outputWriter.driverPrintHeader("DELETE MULTIPLE FIELDS");

            // Set the form name
            String formName = InputReader.getString("Form Name: ");

            // Get the Field IDs to be deleted
            outputWriter.driverPrintPrompt("Ids of fields to delete:\n");
            int[] idList = InputReader.getIntArray();

            // Get the delete option
            int deleteOption = InputReader.getInt("Delete Option- clean, data, cascade (0, 1, 2 ) (0): ", 0);

            // Now delete the fields
            beginAPICall();
            getControlStructObject().deleteFields(formName, idList, deleteOption);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Delete Multiple Fields  Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getField() {
        try {
            outputWriter.driverPrintHeader("GET FIELD");

            // Set the form name
            String formName = InputReader.getString("Form Name: ");

            // Set the Field ID
            int id = InputReader.getInt("Field id (1): ", 1);

            FieldKey key = new FieldKey(formName, id);

            // Set the field criteria to retrieve all
            FieldCriteria crit = new FieldCriteria();
            crit.setRetrieveAll(true);

            // Find the field
            beginAPICall();
            Field field = getControlStructObject().getField(key.getFormName(), key.getFieldID(), crit);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the field info
            outputWriter.printField("", "Field Information:", field);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get Field Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListField() {
        try {
            outputWriter.driverPrintHeader("GET LIST FIELD");

            String name = InputReader.getString("Form Name (): ", "");

            long changedSince = InputReader.getLong("Get all changed since ", 0);

            int type = InputReader.getInt(
                    "Field Type - data, trim, control, page, page holder, table, column, attach,\n "
                            + "attach pool  (1, 2, 4, 8, 16, 32, 64, 128, 256) (1): ", 1);

            // Find the fields
            beginAPICall();
            //         List<Integer> fieldList = getControlStructObject().getListField(  name, Constants.AR_FIELD_TYPE_ALL, (int) listCriteria.getModifiedAfter().getValue() );
            List<Field> fieldList = getControlStructObject().getListFieldObjects(name, type, changedSince, null);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the information
            outputWriter.print("", "Field List Information:", fieldList);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get List Field Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void setField() {
        try {
            outputWriter.driverPrintHeader("SET FIELD");

            // Get the Form Name
            String formName = InputReader.getString("Form name: ");
            
            // Get the Field ID
            int id = InputReader.getInt("Field id (1): ", 1);            
            
            // Create an empty field
            Field field = InputReader.getField();

            // Set the Form Name
            field.setForm(formName); 

            // Set the Field ID            
            field.setFieldID(id);
            

            // Set the Field Name
            if (InputReader.getBooleanForChangingInfo("Change fieldName? (F): ", false)) {
                String fieldName = InputReader.getString("field name");
                field.setNewName(fieldName);
            }

            // Set the field mapping structure
            if (InputReader.getBooleanForChangingInfo("Change field mapping? (F): ", false)) {
                FieldMapping mapInfo = InputReader.getFieldMappingInfo();
                field.setFieldMap(mapInfo);
            }

            // Set the option for entering the value
            if (InputReader.getBooleanForChangingInfo("Have new option? (F): ", false)) {
                int option = InputReader.getInt("Required, Optional, System, or Display-only (1-4) (2): ", 2);
                field.setFieldOption(option);
            }

            // Set create mode
            if (InputReader.getBooleanForChangingInfo("Have new create mode? (F): ", false)) {
                int createMode = InputReader.getInt("Open or Protected at create (1 or 2) (1) ", 1);
                field.setCreateMode(createMode);
            }

            // Set audit option
            if (InputReader.getBooleanForChangingInfo("Have new field option? (F): ", false)) {
                int auditOption = InputReader.getInt("Field option for Audit/Copy (0): ", 0);
                field.setAuditOption(auditOption);
            }

            int type = field.getDataType();
            // Set default value
            if (InputReader.getBooleanForChangingInfo("Have new default value? (F): ", false)) {
                Value val = InputReader.getValue(type);
                field.setDefaultValue(val);
            }

            // Set permissions
            if (InputReader.getBooleanForChangingInfo("Have new permissions? (F): ", false)) {
                List<PermissionInfo> permissions = InputReader.getPermissionInfoList(true);
                field.setPermissions(permissions);
            }

            // Set limits
            if (InputReader.getBooleanForChangingInfo("Have new field limits? (F): ", false)) {
                FieldLimit limitInfo = InputReader.getFieldLimitInfo(type);
                field.setFieldLimit(limitInfo);
            }

            // Set DisplayInstanceList
            if (InputReader.getBooleanForChangingInfo("Have new display instance? (F): ", false)) {
                DisplayInstanceMap dispInstanceList = InputReader.getDisplayInstanceMap();
                field.setDisplayInstance(dispInstanceList);
            }

            // Set display instance setfield option
            if (InputReader.getBooleanForChangingInfo("Have a new set field option for display instance? (F): ", false)) {
                int setFieldsOptions = InputReader.getInt(
                        "Replace only new display instances(1) or Replace all display instances(0) ", 1);
                field.setSetFieldOptions(setFieldsOptions);
            }

            // Set the help text
            if (InputReader.getBooleanForChangingInfo("Change help text? (F): ", false)) {
                String helpText = InputReader.getString("Help Text: ", "");
                field.setHelpText(helpText);
            }

            // Set the owner
            if (InputReader.getBooleanForChangingInfo("Change owner? (F): ", false)) {
                String owner = InputReader.getString("Owner: ");
                field.setOwner(owner);
            }

            // Set Change DiaryList
            if (InputReader.getBooleanForChangingInfo("Add to change diary? (F): ", false)) {
                String diary = InputReader.getString("DiaryList: ", "");
                field.appendDiaryText(diary);
            }

            // Set Object properties
            if (InputReader.getBooleanForChangingInfo("Change object properties? (F): ", false)) {
                ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
                field.setObjectProperty(propList);
            }            
            
            // Now store the new field information
            beginAPICall();
            getControlStructObject().setField(field);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Set Field Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }
    
    void setMultipleFields() {
        try {
            outputWriter.driverPrintHeader("SET MULTIPLE FIELDS");

            int noOfFields = InputReader.getInt("Number of Fields (0): ", 0);
            
            List<Field> fields = new ArrayList<Field>(noOfFields);

            String formName = InputReader.getString("Form name: ");

            //populate each field
            for (int index = 0; index < noOfFields; index++) {
                outputWriter.driverPrintHeader("Enter information for field(" + (index + 1) + ")");
                //get Field Id
                int id = InputReader.getInt("Field id (1): ", 1);

                // Create an empty field
                Field field = InputReader.getField();

                // Set the Form Name
                
                field.setForm(formName);

                // Set the Field ID
                field.setFieldID(id);

                // Set the Field Name
                if (InputReader.getBooleanForChangingInfo("Change fieldName? (F): ", false)) {
                    String fieldName = InputReader.getString("field name");
                    field.setNewName(fieldName);
                }

                // Set the field mapping structure
                if (InputReader.getBooleanForChangingInfo("Change field mapping? (F): ", false)) {
                    FieldMapping mapInfo = InputReader.getFieldMappingInfo();
                    field.setFieldMap(mapInfo);
                }

                // Set the option for entering the value
                if (InputReader.getBooleanForChangingInfo("Have new option? (F): ", false)) {
                    int option = InputReader.getInt("Required, Optional, System, or Display-only (1-4) (2): ", 2);
                    field.setFieldOption(option);
                }

                // Set create mode
                if (InputReader.getBooleanForChangingInfo("Have new create mode? (F): ", false)) {
                    int createMode = InputReader.getInt("Open or Protected at create (1 or 2) (1) ", 1);
                    field.setCreateMode(createMode);
                }

                // Set audit option
                if (InputReader.getBooleanForChangingInfo("Have new field option? (F): ", false)) {
                    int auditOption = InputReader.getInt("Field option for Audit/Copy (0): ", 0);
                    field.setAuditOption(auditOption);
                }

                int type = field.getDataType();
                // Set default value
                if (InputReader.getBooleanForChangingInfo("Have new default value? (F): ", false)) {
                    Value val = InputReader.getValue(type);
                    field.setDefaultValue(val);
                }

                // Set permissions
                if (InputReader.getBooleanForChangingInfo("Have new permissions? (F): ", false)) {
                    List<PermissionInfo> permissions = InputReader.getPermissionInfoList(true);
                    field.setPermissions(permissions);
                }

                // Set limits
                if (InputReader.getBooleanForChangingInfo("Have new field limits? (F): ", false)) {
                    FieldLimit limitInfo = InputReader.getFieldLimitInfo(type);
                    field.setFieldLimit(limitInfo);
                }

                // Set DisplayInstanceList
                if (InputReader.getBooleanForChangingInfo("Have new display instance? (F): ", false)) {
                    DisplayInstanceMap dispInstanceList = InputReader.getDisplayInstanceMap();
                    field.setDisplayInstance(dispInstanceList);
                }

                // Set display instance setfield option
                if (InputReader.getBooleanForChangingInfo("Have a new set field option for display instance? (F): ",
                        false)) {
                    int setFieldsOptions = InputReader.getInt(
                            "Replace only new display instances(1) or Replace all display instances(0) ", 1);
                    field.setSetFieldOptions(setFieldsOptions);
                }

                // Set the help text
                if (InputReader.getBooleanForChangingInfo("Change help text? (F): ", false)) {
                    String helpText = InputReader.getString("Help Text: ", "");
                    field.setHelpText(helpText);
                }

                // Set the owner
                if (InputReader.getBooleanForChangingInfo("Change owner? (F): ", false)) {
                    String owner = InputReader.getString("Owner: ");
                    field.setOwner(owner);
                }

                // Set Change DiaryList
                if (InputReader.getBooleanForChangingInfo("Add to change diary? (F): ", false)) {
                    String diary = InputReader.getString("DiaryList: ", "");
                    field.appendDiaryText(diary);
                }
                
                // Set Object properties
                if (InputReader.getBooleanForChangingInfo("Change object properties? (F): ", false)) {
                    ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
                    field.setObjectProperty(propList);
                }      
                fields.add(field);
            }

            // Now store the new field information
            beginAPICall();
            getControlStructObject().setMultipleFields(fields);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<List<StatusInfo>> statusList = getControlStructObject().getMultiLastStatus();
            statusList.add(getControlStructObject().getLastStatus());
            outputWriter.printMultiStatusInfoList("", "Set Multiple Fields Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void createCharMenu() {
        try {
            outputWriter.driverPrintHeader("CREATE CHAR MENU");

            // Create a Menu (empty) instance
            Menu menu = InputReader.getMenu();

            // Set the menu name
            String menuName = InputReader.getString("Character Menu Name");
            menu.setName(menuName);

            // Set refreshing code
            int refreshCode = InputReader.getInt("Refresh on connect, open, interval (1 - 3) (1): ", 1);
            menu.setRefreshCode(refreshCode);

            // Set the Char menu item info
            /* TODO: Getting the menu def part */
            //         outputWriter.driverPrintPrompt("Character menu Info:\n");
            //         CharMenuInfo menuInfo = InputReader.getCharMenuInfo( );
            //         menu.setMenuDefinition( menuInfo );
            // Set help text
            String helpText = InputReader.getString("Help Text():", "");
            menu.setHelpText(helpText);

            // Set Owner
            String owner = InputReader.getString("Owner:");
            menu.setOwner(owner);

            // Set Change diary
            String changeDiary = InputReader.getString("Change DiaryList():", "");
            menu.appendDiaryText(changeDiary);

            // Set the properties
            ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
            menu.setProperties(propList);

            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();

            // Now create the menu
            beginAPICall();
            getControlStructObject().createMenu(menu, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "CreateCharMenu Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void deleteCharMenu() {
        try {
            outputWriter.driverPrintHeader("DELETE CHAR MENU");

            // Set the menu name
            String menuName = InputReader.getString("Character Menu Name");
            int deleteOption = InputReader.getInt("Delete option ? ( 0, 1, 2 ) ( 0 ):", 0);

            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();

            // delete the menu
            beginAPICall();
            getControlStructObject().deleteMenu(menuName, deleteOption, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "DeleteCharMenu", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getCharMenu() {
        try {
            outputWriter.driverPrintHeader("GET CHAR MENU");

            // Get Menu Key
            String key = InputReader.getString("Menu Name:", "");

            // Create MenuCriteria Object
            MenuCriteria crit = new MenuCriteria();
            crit.setRetrieveAll(true);

            // Find the menu
            beginAPICall();
            Menu menu = getControlStructObject().getMenu(key, crit);
            endAPICall(getControlStructObject().getLastStatus());

            // Now print all the information
            outputWriter.printMenu("", "Menu Information: ", menu);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "GetCharMenu Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }

    }

    void getListCharMenu() {
        try {
            outputWriter.driverPrintHeader("GET LIST CHAR MENU");

            long changedSince = InputReader.getLong("Get all changed since (0): ", 0);

            outputWriter.driverPrintHeader("Get menus associated with forms:");
            List<String> formKeys = InputReader.getStringList("", "Form Name");
            outputWriter.driverPrintHeader("Get menus associated with active links:");
            List<String> activeLinkKeys = InputReader.getStringList("", "Active Link Name");

            ObjectPropertyMap propMap = InputReader.getObjectPropertyMap();

            // Find the menu objects
            beginAPICall();
            List<String> menuList = getControlStructObject().getListMenu(changedSince, formKeys, activeLinkKeys, propMap);
            endAPICall(getControlStructObject().getLastStatus());

            // Now print all the information
            outputWriter.print("", "Menu List Information: ", menuList);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get List Char Menu Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }

    }

    void getMultipleCharMenu(){
        try {
            outputWriter.driverPrintHeader("GET MULTIPLE CHAR MENUS");

            long changedSince = InputReader.getLong("Get all changed since (0): ", 0);

            outputWriter.driverPrintHeader("Get char menu by name:");
            List<String> menuKeys = InputReader.getStringList("", "char menu Name");
            // Create MenuCriteria Object
            MenuCriteria crit = new MenuCriteria();
            crit.setRetrieveAll(true);

            // Find the menu objects
            beginAPICall();
            List<Menu> menuList = getControlStructObject().getListMenuObjects(changedSince, menuKeys, crit);
            endAPICall(getControlStructObject().getLastStatus());

            // Now print all the information
            outputWriter.print("", "Multiple Menu Information: ", menuList);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get Multiple Char Menu Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }        
    }
    
    void expandCharMenu() {
        try {
            outputWriter.driverPrintHeader("EXPAND CHAR MENU");
            List<MenuItem> expandedMenuInfo;
            Entry keywordList = null;
            Entry parameterList = null;

            // Set the Char menu info
            Menu menu = InputReader.getMenu();
            
            if (menu.getName() != null && menu.getName().length() != 0) {
            	//that means it is a server side menu, now get rest of the inputs
            	// Get the keyword list information
                outputWriter.driverPrintPrompt("      Keyword List:\n");
                keywordList = InputReader.getEntry();

                // Get the parameter list information
                outputWriter.driverPrintPrompt("      Parameter List:\n");
                parameterList = InputReader.getEntry();
                
                // this is not needed but to be consistent with c driver inputs
                outputWriter.driverPrintPrompt("      Extern List:\n");
                InputReader.getQualifierInfoList();
                
                // Get the server name
                ((SqlMenu)menu).setServer(InputReader.getString("      Server (): ", ""));

                // this are not needed but to be consistent with c driver inputs
                InputReader.getString("      Form Name (): ", "");
                
            }
            int maxRetrieve = InputReader.getInt("Number of entries to retrieve:", 0);
            int nMatches = InputReader.getInt("Returns the number of (accessible) entries that match the qualification criteria :", 0);

            // now expand the menu
			beginAPICall();
			expandedMenuInfo = getControlStructObject().expandMenu(menu, keywordList, parameterList, maxRetrieve, (nMatches == 0) ? null : new OutputInteger(nMatches) );
			endAPICall(getControlStructObject().getLastStatus());

            // Print the expanded menu information
            outputWriter.printCharMenuItemInfoList("", "Expanded char Menu Info:", expandedMenuInfo);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ExpandCharMenu Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void setCharMenu() {
        try {
            outputWriter.driverPrintHeader("SET CHAR MENU");

            // Create a new Menu instance
            Menu menu = InputReader.getMenu();

            String name = InputReader.getString("Character Menu Name");
            menu.setName(name);

            // If needed get the information to change the menu name
            if (InputReader.getBooleanForChangingInfo("Change Character Menu Name? (F): ", false)) {
                String newName = InputReader.getString("New Name");
                menu.setNewName(newName);
            }

            // If needed get and Set new refreshing code
            if (InputReader.getBooleanForChangingInfo("Change Refresh Code? (F): ", false)) {
                int refreshCode = InputReader.getInt("Refresh on connect, open, interval (1 - 3) (1): ", 1);
                menu.setRefreshCode(refreshCode);
            }

            /* TODO: Getting the menu def part */
            //         // If needed get the new menu definition
            //         if( InputReader.getBooleanForChangingInfo( "Change Menu Definition(s)? (F): ", false ) )
            //         {
            //             outputWriter.driverPrintPrompt("Character menu Info\n");
            //             CharMenuInfo menuInfo = InputReader.getCharMenuInfo( );
            //             menu.setMenuDefinition( menuInfo );
            //         }
            // If needed get the new help text
            if (InputReader.getBooleanForChangingInfo("Change help text? (F): ", false)) {
                String helpText = InputReader.getString("    Help Text(): ", "");
                menu.setHelpText(helpText);
            }

            // If needed get the new Owner
            if (InputReader.getBooleanForChangingInfo("Change Owner? (F): ", false)) {
                String owner = InputReader.getString("Owner:");
                menu.setOwner(owner);
            }

            // If needed get the new Owner
            if (InputReader.getBooleanForChangingInfo("Add to Change DiaryList? (F): ", false)) {
                String diary = InputReader.getString("    Change DiaryList (): ", "");
                menu.appendDiaryText(diary);
            }

            // If needed get the new properties
            if (InputReader.getBooleanForChangingInfo("Change object properties? (F): ", false)) {
                ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
                menu.setProperties(propList);
            }


            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();

            // Now store all the menu information
            beginAPICall();
            getControlStructObject().setMenu(menu, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Set Char Menu Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }

    }

    void createContainer() {
        try {
            outputWriter.driverPrintHeader("CREATE CONTAINER");

            // Create a new container object
            Container container = InputReader.getContainer();

            // Get and Set the container name
            String name = InputReader.getString("Container Name");
            container.setName(name);

            // Get and Set the permissions information
            List<PermissionInfo> permissionList = InputReader.getPermissionInfoList(true);
            container.setPermissions(permissionList);

            // Get and Set sub admin groups allowed to access this container
            outputWriter.driverPrintPrompt("Ids of sub admin groups allowed to access container:\n");
            List<Integer> adminGroupList = InputReader.getIntegerList();
            container.setAdminGroupList(adminGroupList);

            // Get and Set container owner
            List<ContainerOwner> containerOwnerList = InputReader.getContainerOwnerList();
            container.setContainerOwner(containerOwnerList);

            // Get and Set the container Label
            String label = InputReader.getString("Label ():", "");
            container.setLabel(label);

            // Get and set the description
            String desc = InputReader.getString("Description ():", "");
            container.setDescription(desc);

            // Get and Set the reference list
            List<Reference> references = InputReader.getReferenceList();
            container.setReferences(references);

            // Get and set the remove flag
            /* TODO: Movee this to the actual delete call */
            boolean removeInvalidReference = InputReader.getBooleanForChangingInfo( "Remove Invalid References( F ):", false );
            //         container.setRemoveFlag( flag );
            // Get and set the help text
            String help = InputReader.getString("Help Text():", "");
            container.setHelpText(help);

            // Get and Set the owner
            String owner = InputReader.getString("Owner:");
            container.setOwner(owner);

            // Get and Set change diary
            String diary = InputReader.getString("Change DiaryList():", "");
            container.appendDiaryText(diary);

            // Get and set properties
            ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
            container.setProperties(propList);
            
            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();

            // Now create the container in the database
            beginAPICall();
            getControlStructObject().createContainer(container, removeInvalidReference, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Create Container Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }

    }

    void setContainer() {
        try {
            outputWriter.driverPrintHeader("SET CONTAINER");

            // Create a container instance
            Container container = InputReader.getContainer();

            // Get and Set container key
            String name = InputReader.getString("Container Name:", "");
            container.setName(name);

            // Get and Set the container new name
            if (InputReader.getBooleanForChangingInfo("Change container name? (F): ", false)) {
                String newName = InputReader.getString("New Name");
                container.setNewName(newName);
            }

            // Get and Set new permission list
            if (InputReader.getBooleanForChangingInfo("Change group( permission ) list? (F): ", false)) {
                List<PermissionInfo> permissionList = InputReader.getPermissionInfoList(true);
                container.setPermissions(permissionList);
            }

            // Get and Set sub admin groups allowed to access this container
            if (InputReader.getBooleanForChangingInfo("Change sub admin group list? (F): ", false)) {
                outputWriter.driverPrintPrompt("Ids of sub admin groups allowed to access container:\n");
                List<Integer> adminGroupList = InputReader.getIntegerList();
                container.setAdminGroupList(adminGroupList);
            }

            // Get and Set new container owner
            if (InputReader.getBooleanForChangingInfo("Change container owner object? (F): ", false)) {
                List<ContainerOwner> containerOwnerList = InputReader.getContainerOwnerList();
                container.setContainerOwner(containerOwnerList);
            }

            // Get and Set the new container Label
            if (InputReader.getBooleanForChangingInfo("Change label? (F): ", false)) {
                String label = InputReader.getString("    Label ():", "");
                container.setLabel(label);
            }

            // Get and set the new description
            if (InputReader.getBooleanForChangingInfo("Change description? (F): ", false)) {
                String desc = InputReader.getString("    Description ():", "");
                container.setDescription(desc);
            }

            boolean removeInvalidReference = false;
            // Get and Set the new reference list
            if (InputReader.getBooleanForChangingInfo("Change reference list? (F): ", false)) {
                List<Reference> references = InputReader.getReferenceList();
                container.setReferences(references);
                /* TODO: this should move to actual API call */
                removeInvalidReference = InputReader.getBooleanForChangingInfo( "Remove invalid references? (F): ", false );
                //            container.setRemoveFlag( flag );
            }

            // Get and set the new help text
            if (InputReader.getBooleanForChangingInfo("Change help text? (F): ", false)) {
                String help = InputReader.getString("    Help Text():", "");
                container.setHelpText(help);
            }

            // Get and set the new owner
            if (InputReader.getBooleanForChangingInfo("Change owner? (F): ", false)) {
                String owner = InputReader.getString("Owner:");
                container.setOwner(owner);
            }

            // Get and Set addition to change diary
            if (InputReader.getBooleanForChangingInfo("Add to change diary? (F): ", false)) {
                String diary = InputReader.getString("    Change DiaryList():", "");
                container.appendDiaryText(diary);
            }

            // Get and set properties
            if (InputReader.getBooleanForChangingInfo("Change object properties? (F): ", false)) {
                ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
                container.setProperties(propList);
            }
            
            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();

            // Now update the container properties in the database
            beginAPICall();
            getControlStructObject().setContainer(container, removeInvalidReference, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Set Container Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }

    }

    void deleteContainer() {
        try {
            outputWriter.driverPrintHeader("DELETE CONTAINER");

            // Get and Set the container name
            String name = InputReader.getString("Container Name");
            int deleteOption = InputReader.getInt("Delete option ? ( 0, 1, 2 ) ( 0 ):", 0);
            
            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();

            // Now remove the container
            beginAPICall();
            getControlStructObject().deleteContainer(name, deleteOption, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Delete Container Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }

    }

    void getContainer() {
        try {
            outputWriter.driverPrintHeader("GET CONTAINER");

            // Get  container key
            String key = InputReader.getString("Container Key:");
            
            // Set the criteria to retrieve all
            ContainerCriteria criteria = new ContainerCriteria();
            criteria.setRetrieveAll(true);
            
            // Now get the container
            beginAPICall();
            Container container = getControlStructObject().getContainer(key, criteria);
            endAPICall(getControlStructObject().getLastStatus());

            // Now print the container information
            outputWriter.printContainer("", "Container Information:", container);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "GetContainer Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListContainer() {
        try {
            outputWriter.driverPrintHeader("GET LIST CONTAINER");
            // Get the time stamp
            long changedSince = InputReader.getLong("Get All changed since, as UNIX epoch (0): ", 0);
           
            // Get and set container types information
            int[] containerTypes = InputReader.getIntArray();

            // Get and set attributes flag
            boolean hiddenFlag = InputReader.getBooleanForChangingInfo(
                    "Retrieve hidden containers (F-T) (T):", true);

            // Get and set the container owner
            List<ContainerOwner> containerOwnerList = InputReader.getContainerOwnerList();
            
            ObjectPropertyMap propsToSearch = InputReader.getObjectPropertyMap();

            // Create Container Criteria object
            ContainerCriteria containerCriteria = new ContainerCriteria();

            // Set the flag to retrieve all the information
            containerCriteria.setRetrieveAll(true);

            // Now find all the container objects
            beginAPICall();
            List<String> containers = getControlStructObject().getListContainer(changedSince, containerTypes,
                    hiddenFlag, containerOwnerList, propsToSearch);
            endAPICall(getControlStructObject().getLastStatus());

            // Now print all the information about the containers
            outputWriter.print("", "Container List:", containers);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "GetListContainer Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getMultipleContainers() {
        try {
            outputWriter.driverPrintHeader("GET MULTIPLE CONTAINERS");
            List<String> containerKeys = InputReader.getStringList("", "Container Name");
            
            // Now find all the container objects
            List<Container> containers = getControlStructObject().getListContainerObjects(containerKeys);

            // Now print all the information about the containers
            outputWriter.print("", "Container List:", containers);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.printString("Null Pointer exception...\n");
        } catch (ARException e) {
            outputWriter.printARException(e);
        }
    }

    void createVUI() {
        try {

            outputWriter.driverPrintHeader("CREATE VIEW");

            // Create a new view instance
            View view = new View();

            // Get and set the form name
            String name = InputReader.getString("Form Name: ");
            view.setFormName(name);

            // Get and Set the VUI id
            int id = InputReader.getInt("VUI id (0): ", 0);
            view.setVUIId(id);

            // Get and set the VUI name
            String vuiName = InputReader.getString("VUI name");
            view.setName(vuiName);

            // Get and set the locale
            String locale = InputReader.getString("Locale (): ", "");
            view.setLocale(locale);

            // Get and set the VUI Type
            int type = InputReader.getInt("VUI-Type (0): ", 0);
            view.setVUIType(type);

            // Get and set the properties
            ViewDisplayPropertyMap properties = InputReader.getViewDisplayPropertyMap();
            view.setDisplayProperties(properties);

            // Get and set help text
            String helpText = InputReader.getString("Help Text:", "");
            view.setHelpText(helpText);

            // Get and Set owner
            String owner = InputReader.getString("Owner:");
            view.setOwner(owner);

            // Get and set the change diary
            String changeDiary = InputReader.getString("Change DiaryList:", "");
            view.appendDiaryText(changeDiary);

            // Now create the view
            beginAPICall();
            getControlStructObject().createView(view);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "CreateView Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void deleteVUI() {
        try {

            outputWriter.driverPrintHeader("DELETE VIEW");

            // Create a new view instance
            View view = new View();

            // Get and set the form name
            String name = InputReader.getString("Form Name: ");
            view.setFormName(name);

            // Get and Set the VUI id
            int id = InputReader.getInt("VUI id (0): ", 0);
            view.setVUIId(id);

            // Now delete the view
            beginAPICall();
            getControlStructObject().deleteView(view.getFormName(), view.getVUIId());
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Delete View Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getVUI() {
        try {

            outputWriter.driverPrintHeader("GET VIEW");

            // Get the Form Name
            String formName = InputReader.getString("Form Name (): ", "");
            // Get the VUI id
            int vuiId = InputReader.getInt("VUI id (1): ", 1);
            // Create the View Criteria and the flag to retrieve all the properties
            ViewCriteria criteria = new ViewCriteria();
            ;
            criteria.setRetrieveAll(true);

            // Now get the view
            beginAPICall();
            View view = getControlStructObject().getView(formName, vuiId, criteria);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the view information
            outputWriter.printView("", "View Information:", view);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get View STatus", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListVUI() {
		try {

			outputWriter.driverPrintHeader("GET LIST VIEW");

			// Create the View Criteria and the flag to retrieve all the
			// properties
			ViewCriteria criteria = new ViewCriteria();
			criteria.setRetrieveAll(true);

			String formName = InputReader.getString("Form Name (): ", "");

			long changedSince = InputReader.getLong(
					"Get All changed since, as UNIX epoch (0): ", 0);
			
			ObjectPropertyMap objProp = InputReader.getObjectPropertyMap();

			// Now get the view
			beginAPICall();
			List<Integer> viewsList = getControlStructObject().getListView(
					formName, changedSince, objProp);
			endAPICall(getControlStructObject().getLastStatus());

			// Print the all views information
			outputWriter.print("", "Views Ids:", viewsList);

			// Success. Print the status
			List<StatusInfo> statusList = getControlStructObject()
					.getLastStatus();
			outputWriter.printStatusInfoList("", "GetListView status",
					statusList);

		} catch (IOException e) {
			outputWriter
					.printString("Problem in getting the input...Driver problem..\n");
		} catch (NullPointerException e) {
			outputWriter.driverPrintException(e);
		} catch (ARException e) {
			endAPICall(getControlStructObject().getLastStatus());
			outputWriter.printARException(e);
		}
	}

    void setVUI() {
        try {

            outputWriter.driverPrintHeader("SET VIEW");

            // Create a new view instance
            View view = new View();

            // Get and set the form name
            String name = InputReader.getString("Form Name: ");
            view.setFormName(name);

            // Get and Set the VUI id
            int id = InputReader.getInt(" VUI id (1): ", 1);
            view.setVUIId(id);

            // Get and set the VUI name
            if (InputReader.getBooleanForChangingInfo("Change VUI name? (F): ", false)) {
                String vuiName = InputReader.getString("VUI name");
                view.setNewName(vuiName);
            }

            // Get and Set the locale
            if (InputReader.getBooleanForChangingInfo("Change Locale? (F): ", false)) {
                view.setLocale(InputReader.getString("Locale(): ", ""));
            }

            // Get and Set the type
            if (InputReader.getBooleanForChangingInfo("Change VUI-Type? (F): ", false)) {
                view.setVUIType(InputReader.getInt("VUI-Type (0): ", 0));
            }

            // Get and set the properties
            if (InputReader.getBooleanForChangingInfo("Have new Prop List Info? (F): ", false)) {
                ViewDisplayPropertyMap properties = InputReader.getViewDisplayPropertyMap();
                view.setDisplayProperties(properties);
            }

            // Get and set help text
            if (InputReader.getBooleanForChangingInfo("Change Help Text? (F): ", false)) {
                String helpText = InputReader.getString("Help Text:", "");
                view.setHelpText(helpText);
            }

            // Get and Set owner
            if (InputReader.getBooleanForChangingInfo("Change Owner? (F): ", false)) {
                String owner = InputReader.getString("Owner:");
                view.setOwner(owner);
            }

            // Get and set the change diary
            if (InputReader.getBooleanForChangingInfo("Add to Change DiaryList? (F): ", false)) {
                String changeDiary = InputReader.getString("Change DiaryList (): ", "");
                view.appendDiaryText(changeDiary);
            }

            // Now store the new view properties
            beginAPICall();
            getControlStructObject().setView(view);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Set View status", statusList);

        }

        catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void createActiveLink() {
        try {
            outputWriter.driverPrintHeader("CREATE ACTIVE LINK");

            // Create a new activelink instance
            ActiveLink activeLink = new ActiveLink();

            // Get and Set the activelink name
            String name = InputReader.getString("Active link Name:");
            activeLink.setName(name);

            // Get and Set the Execution order
            int order = InputReader.getInt("Execution order (1-1000) (1):", 1);
            activeLink.setOrder(order);

            // Get and Set Workflow information
            int wkflowtype = InputReader.getInt("Workflow type (1):", 1);
            if (wkflowtype == 1) {
                List<String> formList = InputReader.getStringList("", "Form");
                activeLink.setFormList(formList);
            }

            // Get and set ids ofgroups allowed to perform the active link
            outputWriter.driverPrintPrompt("Ids of groups allowed to perform active link:\n");
            List<Integer> idList = InputReader.getInternalIDArrayList();
            activeLink.setGroupList(idList);

            // Get and Set the Execution mask
            int executeMask = InputReader.getInt("Execution bit mask (1):", 1);
            activeLink.setExecuteMask(executeMask);

            // Get and Set the Control field
            int controlField = InputReader.getInt("Control Field to tie active link to (0):", 0);
            activeLink.setControlField(controlField);

            // Get and Set the focus field
            int focusField = InputReader.getInt("Focus Field to tie active link to (0):", 0);
            activeLink.setFocusField(focusField);

            // Get and Set the enable flag
            boolean enable = InputReader.getBoolean("Disable/Enable (F-T) (T):", true);
            activeLink.setEnable(enable);

            // Get and Set the QualifierInfo
            QualifierInfo qualifier = InputReader.getQualifierInfo();
            activeLink.setQualifier(qualifier);

            // Get and Set the action information
            List<ActiveLinkAction> actionList = InputReader.getActiveLinkActionInfoList(true);
            activeLink.setActionList(actionList);

            // Get and Set the else action list
            List<ActiveLinkAction> elseActionList = InputReader.getActiveLinkActionInfoList(false);
            activeLink.setElseList(elseActionList);

            // Get and set the help text
            String helpText = InputReader.getString("Help Text():", "");
            activeLink.setHelpText(helpText);

            // Get and Set the owner
            String owner = InputReader.getString("Owner:");
            activeLink.setOwner(owner);

            // Get and Set change diary
            String diary = InputReader.getString("Change DiaryList():", "");
            activeLink.appendDiaryText(diary);

            // Get and set properties
            ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
            activeLink.setProperties(propList);

            int option = InputReader.getInt("Error Handler activelink Options (0-1) (0):", 0);
            activeLink.setErrorActlinkOptions(option);

            name = InputReader.getString("Error Handler Name: ");
            activeLink.setErrorActlinkName(name);
            
            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();
            
            // Now create the activelink in the database
            beginAPICall();
            getControlStructObject().createActiveLink(activeLink, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Create Active Link Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }

    }

    void deleteActiveLink() {
        try {

            outputWriter.driverPrintHeader("DELETE ACTIVELINK");

            // Get and set the activelink name  to be deleted
            String name = InputReader.getString("Active link Name:");
            int deleteOption = InputReader.getInt("Delete option ? ( 0, 1, 2 ) ( 0 ):", 0);

            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();

            // Now delete the active link
            beginAPICall();
            getControlStructObject().deleteActiveLink(name, deleteOption, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Delete Active Link Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getActiveLink() {
        try {

            outputWriter.driverPrintHeader("GET ACTIVELINK");

            // Get the Active link key
            String key = InputReader.getString("Active link Name:");

            // Create an Active link criteria and set the flag to retrieve all the properties
            ActiveLinkCriteria criteria = new ActiveLinkCriteria();
            criteria.setRetrieveAll(true);

            // Now get the active link
            beginAPICall();
            ActiveLink activeLink = getControlStructObject().getActiveLink(key, criteria);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the active link information
            outputWriter.printActiveLink("", "Active Link:", activeLink);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get Active Link Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListActiveLink() {
        try {

            outputWriter.driverPrintHeader("GETLIST ACTIVE LINK");

            String formName = null;
            if (InputReader.getBooleanForChangingInfo("For a specific form? (F):", false)) {
                formName = InputReader.getString("Form Name (): ", "");
            }
            
            long changedSince = InputReader.getLong("Get all changed since (0):", 0);

            ObjectPropertyMap objProp = InputReader.getObjectPropertyMap();

            // Now get the active links list
            beginAPICall();
            List<String> names = getControlStructObject().getListActiveLink(formName, changedSince,
            		objProp);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the active link names
            outputWriter.print("", "Active Link List:", names);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get Active Link List Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }
    void getMultipleActiveLinks() {
        try {

            outputWriter.driverPrintHeader("GET MULTIPLE ACTIVE LINKS");
          
            // Create an Active link criteria and set the flag to retrieve all the properties
            ActiveLinkCriteria criteria = new ActiveLinkCriteria();
            criteria.setRetrieveAll(true);

            long changedSince = InputReader.getLong("Get all changed since (0):", 0);
            List<String> names = InputReader.getStringList("", "Active link name");

            // Now get the active links list
            beginAPICall();
            List<ActiveLink> activeLinkList = getControlStructObject().getListActiveLinkObjects(names, 
            		changedSince, criteria);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the active link information
            outputWriter.print("", "Active Link List:", activeLinkList);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get Active Link List Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void setActiveLink() {
        try {
            outputWriter.driverPrintHeader("SET ACTIVE LINK");

            // Create a new activelink instance
            ActiveLink activeLink = new ActiveLink();

            // Get and Set the activelink name
            String name = InputReader.getString("Active link Name:");
            activeLink.setName(name);

            // Get and Set the activelink new name
            if (InputReader.getBooleanForChangingInfo("Change active link name? (F): ", false)) {
                String newName = InputReader.getString("New Name");
                activeLink.setNewName(newName);
            }

            // Get and Set the Execution order
            if (InputReader.getBooleanForChangingInfo("Change execution order? (F): ", false)) {
                int order = InputReader.getInt("Execution order (1-1000) (1):", 1);
                activeLink.setOrder(order);
            }

            // Get and Set Workflow information
            if (InputReader.getBooleanForChangingInfo("Change Work Flow information? (F): ", false)) {
                List<String> formList = InputReader.getStringList("", "Form");
                activeLink.setFormList(formList);
            }

            // Get and set ids ofgroups allowed to perform the active link
            if (InputReader.getBooleanForChangingInfo("Change group List? (F): ", false)) {
                outputWriter.driverPrintPrompt("Ids of groups allowed to perform active link:\n");
                List<Integer> idList = InputReader.getIntegerList();
                activeLink.setGroupList(idList);
            }

            // Get and Set the Execution mask
            if (InputReader.getBooleanForChangingInfo("Change execute mask? (F): ", false)) {
                int executeMask = InputReader.getInt("Execution bit mask (1):", 1);
                activeLink.setExecuteMask(executeMask);
            }

            // Get and Set the Control field
            if (InputReader.getBooleanForChangingInfo("Change control field? (F): ", false)) {
                int controlField = InputReader.getInt("Control Field to tie active link to (0):", 0);
                activeLink.setControlField(controlField);
            }

            // Get and Set the focus field
            if (InputReader.getBooleanForChangingInfo("Change focus field? (F): ", false)) {
                int focusField = InputReader.getInt("Focus Field to tie active link to (0):", 0);
                activeLink.setFocusField(focusField);
            }

            // Get and Set the enable flag
            if (InputReader.getBooleanForChangingInfo("Change enable/disable? (F): ", false)) {
                boolean enable = InputReader.getBooleanForChangingInfo("Disable/Enable (F-T) (T):", true);
                activeLink.setEnable(enable);
            }

            // Get and Set the QualifierInfo
            if (InputReader.getBooleanForChangingInfo("Change query? (F): ", false)) {
                QualifierInfo qualifier = InputReader.getQualifierInfo();
                activeLink.setQualifier(qualifier);
            }

            // Get and Set the action information
            if (InputReader.getBooleanForChangingInfo("Change actions? (F): ", false)) {
                List<ActiveLinkAction> actionList = InputReader.getActiveLinkActionInfoList(true);
                activeLink.setActionList(actionList);
            }

            // Get and Set the else action list
            if (InputReader.getBooleanForChangingInfo("Change else actions? (F): ", false)) {
                List<ActiveLinkAction> elseActionList = InputReader.getActiveLinkActionInfoList(false);
                activeLink.setElseList(elseActionList);
            }

            // Get and set the help text
            if (InputReader.getBooleanForChangingInfo("Change help text? (F): ", false)) {
                String helpText = InputReader.getString("Help Text():", "");
                activeLink.setHelpText(helpText);
            }

            // Get and Set the owner
            if (InputReader.getBooleanForChangingInfo("Change owner? (F): ", false)) {
                String owner = InputReader.getString("Owner:");
                activeLink.setOwner(owner);
            }

            // Get and Set change diary
            if (InputReader.getBooleanForChangingInfo("Add to change diary? (F): ", false)) {
                String diary = InputReader.getString("Change DiaryList():", "");
                activeLink.appendDiaryText(diary);
            }

            // Get and set properties
            if (InputReader.getBooleanForChangingInfo("Change object properties? (F): ", false)) {
                ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
                activeLink.setProperties(propList);
            }

            if (InputReader.getBooleanForChangingInfo("Change error handler activelink options? (F): ", false)) {
                int value = InputReader.getInt("Error Handler activelink Options (0-1) (0):", 0);
                activeLink.setErrorActlinkOptions(value);
            }
            
            if (InputReader.getBooleanForChangingInfo("Change error handler name? (F): ", false)) {
                String value = InputReader.getString("Error Handler Name: ");
                activeLink.setErrorActlinkName(value);
            }
            
            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();
            
            // Now store the new properties in the database
            beginAPICall();
            getControlStructObject().setActiveLink(activeLink, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Set Active Link Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void createFilter() {
        try {
            outputWriter.driverPrintHeader("CREATE FILTER");

            // Create a new filter instance
            Filter filter = new Filter();

            // Get and Set the filter name
            String name = InputReader.getString("Filter Name: ");
            filter.setName(name);

            // Get and Set the  order
            int order = InputReader.getInt("Filter order (0-1000) (500): ", 500);
            filter.setOrder(order);

            // Get and Set Workflow information
            List<String> formList = InputReader.getStringList("", "Form");
            filter.setFormList(formList);

            // Get and Set the operations bit mask
            int bitMask = InputReader.getInt("Operation bit mask (0):", 0);
            filter.setOpSet(bitMask);

            // Get and Set the enable flag
            boolean enable = InputReader.getBooleanForChangingInfo("Disable/Enable (0-1) (1):", true);
            filter.setEnable(enable);

            // Get and Set the QualifierInfo
            QualifierInfo qualifier = InputReader.getQualifierInfo();
            filter.setQualifier(qualifier);

            // Get and Set the action information
            List<FilterAction> actionList = InputReader.getFilterActionInfoList(true);
            filter.setActionList(actionList);

            // Get and Set the else action list
            List<FilterAction> elseActionList = InputReader.getFilterActionInfoList(false);
            filter.setElseList(elseActionList);

            // Get and set the help text
            String helpText = InputReader.getString("Help Text ():", "");
            filter.setHelpText(helpText);

            // Get and Set the owner
            String owner = InputReader.getString("Owner ():");
            filter.setOwner(owner);

            // Get and Set change diary
            String diary = InputReader.getString("Change DiaryList ():", "");
            filter.appendDiaryText(diary);

            // Get and set properties
            ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
            filter.setProperties(propList);

            int option = InputReader.getInt("Error Handler Filter Options (0-1) (0):", 0);
            filter.setErrorFilterOptions(option);

            name = InputReader.getString("Error Handler Name: ");
            filter.setErrorHandlingFilter(name);
            
            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();

            // Now create the filter in the database
            beginAPICall();
            getControlStructObject().createFilter(filter, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Create Filter Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void deleteFilter() {
        try {

            outputWriter.driverPrintHeader("DELETE FILTER");

            // Get and set the filter name  to be deleted
            String name = InputReader.getString("Filter Name: ");
            
            //Get the delete option
            int deleteOpt = InputReader.getInt("Delete option ? (0): ", 0);

            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();
            
            // Now delete the Filter
            beginAPICall();
            getControlStructObject().deleteFilter(name, deleteOpt, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Delete Filter Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getFilter() {
        try {

            outputWriter.driverPrintHeader("GET FILTER");

            // Get the Filter key
            String key = InputReader.getString("Filter Name: ");

            // Create  Filter criteria object and set the flag to retrieve all the properties
            FilterCriteria criteria = new FilterCriteria();
            criteria.setRetrieveAll(true);

            // Now get the filter
            beginAPICall();
            Filter filter = getControlStructObject().getFilter(key, criteria);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the Filter information
            outputWriter.printFilter("", "Filter:", filter);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get Filter Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListFilter() {
        try {

            outputWriter.driverPrintHeader("GET FILTER LIST");

            String formName = null;
            if (InputReader.getBooleanForChangingInfo("For a specific form? (F):", false)) {
                formName = InputReader.getString("Form Name (): ", "");
            }

            long changedSince = InputReader.getLong("Get all changed since (0):", 0);
            
            ObjectPropertyMap propMap = InputReader.getObjectPropertyMap();
            
            // Now get the Filters list
            beginAPICall();
            List<String> filterList = getControlStructObject().getListFilter(formName, changedSince, propMap);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the Filter information
            outputWriter.print("", "Filter List:", filterList);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get List Filter status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getMultipleFilters() {
        try {

            outputWriter.driverPrintHeader("GET MULTIPLE FILTERS");

            // Create  Filter criteria object and set the flag to retrieve all the properties
            FilterCriteria criteria = new FilterCriteria();

            String formName = null;
            if (InputReader.getBooleanForChangingInfo("For a specific form? (F):", false)) {
                formName = InputReader.getString("Form Name (): ", "");
            }

            long changedSince = InputReader.getLong("Get all changed since (0):", 0);

            criteria.setRetrieveAll(true);
            
            // Now get the Filters list
            beginAPICall();
            List<Filter> filterList = getControlStructObject().getListFilterObjects(formName, changedSince, criteria);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the Filter information
            outputWriter.print("", "Filter List:", filterList);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get Multiple Filters status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void setFilter() {
        try {
            outputWriter.driverPrintHeader("SET FILTER");

            // Create a new filter instance
            Filter filter = new Filter();

            // Get and Set the filter name
            String name = InputReader.getString("Filter Name: ");
            filter.setName(name);

            // Get and Set the filter new name
            if (InputReader.getBooleanForChangingInfo("Change filter name? (F): ", false)) {
                String newName = InputReader.getString("New Name ():");
                filter.setNewName(newName);
            }

            // Get and Set the  order
            if (InputReader.getBooleanForChangingInfo("Change filter order? (F): ", false)) {
                int order = InputReader.getInt("Filter order (1-1000) (500):", 500);
                filter.setOrder(order);
            }

            // Get and Set Workflow information
            if (InputReader.getBooleanForChangingInfo("Change workflow info? (F): ", false)) {
                List<String> formList = InputReader.getStringList("", "Form");
                filter.setFormList(formList);
            }

            // Get and Set the operations bit mask
            if (InputReader.getBooleanForChangingInfo("Change operations mask? (F): ", false)) {
                int bitMask = InputReader.getInt("Operation bit mask (0):", 0);
                filter.setOpSet(bitMask);
            }

            // Get and Set the enable flag
            if (InputReader.getBooleanForChangingInfo("Change enable/disable? (F): ", false)) {
                boolean enable = InputReader.getBooleanForChangingInfo("Disable/Enable (0-1) (1):", true);
                filter.setEnable(enable);
            }

            // Get and Set the QualifierInfo
            if (InputReader.getBooleanForChangingInfo("Change Query? (F): ", false)) {
                QualifierInfo qualifier = InputReader.getQualifierInfo();
                filter.setQualifier(qualifier);
            }

            // Get and Set the action information
            if (InputReader.getBooleanForChangingInfo("Change action(s)? (F): ", false)) {
                List<FilterAction> actionList = InputReader.getFilterActionInfoList(true);
                filter.setActionList(actionList);
            }

            // Get and Set the else action list
            if (InputReader.getBooleanForChangingInfo("Change else(s)? (F): ", false)) {
                List<FilterAction> elseActionList = InputReader.getFilterActionInfoList(false);
                filter.setElseList(elseActionList);
            }

            // Get and set the help text
            if (InputReader.getBooleanForChangingInfo("Change help text? (F): ", false)) {
                String helpText = InputReader.getString("Help Text():", "");
                filter.setHelpText(helpText);
            }

            // Get and Set the owner
            if (InputReader.getBooleanForChangingInfo("Change owner? (F): ", false)) {
                String owner = InputReader.getString("Owner:");
                filter.setOwner(owner);
            }

            // Get and Set change diary
            if (InputReader.getBooleanForChangingInfo("Add to Change DiaryList? (F): ", false)) {
                String diary = InputReader.getString("Change DiaryList(): ", "");
                filter.appendDiaryText(diary);
            }

            // Get and set properties
            if (InputReader.getBooleanForChangingInfo("Change object properties? (F): ", false)) {
                ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
                filter.setProperties(propList);
            }

            if (InputReader.getBooleanForChangingInfo("Change error handler filter options? (F): ", false)) {
                int value = InputReader.getInt("Error Handler Filter Options (0-1) (0):", 0);
                filter.setErrorFilterOptions(value);
            }
            
            if (InputReader.getBooleanForChangingInfo("Change error handler name? (F): ", false)) {
                String value = InputReader.getString("Error Handler Name: ");
                filter.setErrorHandlingFilter(value);
            }
            
            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();

            // Now create the filter in the database
            beginAPICall();
            getControlStructObject().setFilter(filter, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Set Filter Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void createEscalation() {
        try {
            outputWriter.driverPrintHeader("CREATE ESCALATION");

            // Create a new escalation instance
            Escalation escalation = new Escalation();

            // Get and Set the escalation name
            String name = InputReader.getString("Escalation Name: ");
            escalation.setName(name);

            // Get and Set the  Time information
            EscalationTimeCriteria timeInfo = InputReader.getEsclationTmInfo();
            escalation.setEscalationTm(timeInfo);

            // Get and Set Workflow information
            List<String> formList = InputReader.getStringList("", "Form");
            escalation.setFormList(formList);

            // Get and Set the enable flag
            boolean enable = InputReader.getBoolean("Disable/Enable (0-1) (1): ", true);
            escalation.setEnable(enable);

            // Get and Set the QualifierInfo
            QualifierInfo qualifier = InputReader.getQualifierInfo();
            escalation.setQualifier(qualifier);

            // Get and Set the action information
            List<FilterAction> actionList = InputReader.getFilterActionInfoList(true);
            escalation.setActionList(actionList);

            // Get and Set the else action list
            List<FilterAction> elseActionList = InputReader.getFilterActionInfoList(false);
            escalation.setElseList(elseActionList);

            // Get and set the help text
            String helpText = InputReader.getString("Help Text (): ", "");
            escalation.setHelpText(helpText);

            // Get and Set the owner
            String owner = InputReader.getString("Owner: ");
            escalation.setOwner(owner);

            // Get and Set change diary
            String diary = InputReader.getString("Change DiaryList (): ", "");
            escalation.appendDiaryText(diary);

            // Get and set properties
            ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
            escalation.setProperties(propList);

            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();

            // Now create the escalation in the database
            beginAPICall();
            getControlStructObject().createEscalation(escalation, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Create Escalation status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void deleteEscalation() {
        try {
            outputWriter.driverPrintHeader("DELETE ESCALATION");

            // Get and Set the escalation name
            String name = InputReader.getString("Escalation Name: ");
            int deleteOpt = InputReader.getInt("Delete option ? (0): ", 0);

            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();
            
            // Now delete the escalation from the database
            beginAPICall();
            getControlStructObject().deleteEscalation(name, deleteOpt, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Delete Escalation Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getEscalation() {
        try {
            outputWriter.driverPrintHeader("GET ESCALATION");

            // Create a  escalation Key
            String key = InputReader.getString("Escalation Key:");

            // Create a Escalation Criteria object set the flag to retrieve all the properties
            EscalationCriteria criteria = new EscalationCriteria();
            criteria.setRetrieveAll(true);

            // Get the escalation now
            beginAPICall();
            Escalation escalation = getControlStructObject().getEscalation(key, criteria);
            endAPICall(getControlStructObject().getLastStatus());

            // print the esclation data
            outputWriter.printEscalation("", "Esclation Data:", escalation);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get Escalation Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListEscalation() {
		try {
			outputWriter.driverPrintHeader("GET LIST ESCALATION");

			String formName = null;
			if (InputReader.getBooleanForChangingInfo(
					"For a specific form? (F):", false)) {
				formName = InputReader.getString("Form Name (): ", "");
			}

			long changedSince = InputReader.getLong(
					"Get all changed since (0):", 0);
			
			// Get the properties
			ObjectPropertyMap propList = InputReader.getObjectPropertyMap();

			// Get the escalations now
			beginAPICall();
			List<String> escalationList = getControlStructObject()
					.getListEscalation(formName, changedSince, propList);
			endAPICall(getControlStructObject().getLastStatus());

			// print the esclation data
			outputWriter.print("", "Esclation List:", escalationList);

			// Success. Print the status
			List<StatusInfo> statusList = getControlStructObject()
					.getLastStatus();
			outputWriter.printStatusInfoList("", "GetListEscalation status",
					statusList);
		} catch (IOException e) {
			outputWriter
					.printString("Problem in getting the input...Driver problem..\n");
		} catch (NullPointerException e) {
			outputWriter.driverPrintException(e);
		} catch (ARException e) {
			endAPICall(getControlStructObject().getLastStatus());
			outputWriter.printARException(e);
		}
	}
    
    void getMultipleEscalations() {
		try {
			outputWriter.driverPrintHeader("GET MULTIPLE ESCALATIONS");

			// Create a Escalation Criteria object set the flag to retrieve all
			// the properties
			EscalationCriteria criteria = new EscalationCriteria();

			long changedSince = InputReader.getLong(
					"Get all changed since (0):", 0);

			criteria.setRetrieveAll(true);
			
			String formName = null;
			if (InputReader.getBooleanForChangingInfo(
					"For a specific form? (F):", false)) {
				formName = InputReader.getString("Form Name (): ", "");
				// Get the escalations now
				beginAPICall();
				List<Escalation> escalationList = getControlStructObject()
						.getListEscalationObjects(formName, changedSince,
								criteria);
				endAPICall(getControlStructObject().getLastStatus());

				// print the escalation data
				outputWriter.print("", "Esclation List:", escalationList);

				// Success. Print the status
				List<StatusInfo> statusList = getControlStructObject()
						.getLastStatus();
				outputWriter.printStatusInfoList("",
						"GetMultipleEscalations status", statusList);

			} else {
				List<String> names = InputReader.getStringList("",
						"Escalation");
				// Get the escalations now
				beginAPICall();
				List<Escalation> escalationList = getControlStructObject()
						.getListEscalationObjects(names, changedSince, criteria);
				endAPICall(getControlStructObject().getLastStatus());

				// print the escalation data
				outputWriter.print("", "Esclation List:", escalationList);

				// Success. Print the status
				List<StatusInfo> statusList = getControlStructObject()
						.getLastStatus();
				outputWriter.printStatusInfoList("",
						"GetMultipleEscalations status", statusList);

			}

		} catch (IOException e) {
			outputWriter
					.printString("Problem in getting the input...Driver problem..\n");
		} catch (NullPointerException e) {
			outputWriter.driverPrintException(e);
		} catch (ARException e) {
			endAPICall(getControlStructObject().getLastStatus());
			outputWriter.printARException(e);
		}
	}

    void setEscalation() {
        try {
            outputWriter.driverPrintHeader("SET ESCALATION");

            // Create a new escalation instance
            Escalation escalation = new Escalation();

            // Get and Set the escalation name
            String name = InputReader.getString("Escalation Name: ");
            escalation.setName(name);

            // Get and Set the escalation new name
            if (InputReader.getBooleanForChangingInfo("Change escalation name? (F): ", false)) {
                String newName = InputReader.getString("New Name: ");
                escalation.setNewName(newName);
            }

            // Get and Set the  order
            if (InputReader.getBooleanForChangingInfo("Change escalation Time? (F): ", false)) {
                EscalationTimeCriteria timeInfo = InputReader.getEsclationTmInfo();
                escalation.setEscalationTm(timeInfo);
            }

            // Get and Set Workflow information
            if (InputReader.getBooleanForChangingInfo("Change Work Flow information? (F): ", false)) {
                List<String> formList = InputReader.getStringList("", "Form");
                escalation.setFormList(formList);
            }

            // Get and Set the enable flag
            if (InputReader.getBooleanForChangingInfo("Change enable/disable? (F): ", false)) {
                boolean enable = InputReader.getBooleanForChangingInfo("Disable/Enable (0-1) (1): ", true);
                escalation.setEnable(enable);
            }

            // Get and Set the QualifierInfo
            if (InputReader.getBooleanForChangingInfo("Change Query? (F): ", false)) {
                QualifierInfo qualifier = InputReader.getQualifierInfo();
                escalation.setQualifier(qualifier);
            }

            // Get and Set the action information
            if (InputReader.getBooleanForChangingInfo("Change action(s)? (F): ", false)) {
                List<FilterAction> actionList = InputReader.getFilterActionInfoList(true);
                escalation.setActionList(actionList);
            }

            // Get and Set the else action list
            if (InputReader.getBooleanForChangingInfo("Change else(s)? (F): ", false)) {
                List<FilterAction> elseActionList = InputReader.getFilterActionInfoList(false);
                escalation.setElseList(elseActionList);
            }

            // Get and set the help text
            if (InputReader.getBooleanForChangingInfo("Change help text? (F): ", false)) {
                String helpText = InputReader.getString("Help Text (): ", "");
                escalation.setHelpText(helpText);
            }

            // Get and Set the owner
            if (InputReader.getBooleanForChangingInfo("Change owner? (F): ", false)) {
                String owner = InputReader.getString("Owner: ");
                escalation.setOwner(owner);
            }

            // Get and Set change diary
            if (InputReader.getBooleanForChangingInfo("Add to Change DiaryList? (F): ", false)) {
                String diary = InputReader.getString("Change DiaryList (): ", "");
                escalation.appendDiaryText(diary);
            }

            // Get and set properties
            if (InputReader.getBooleanForChangingInfo("Change object properties? (F): ", false)) {
                ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
                escalation.setProperties(propList);
            }
            
            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();

            // Now store the new properties in the database
            beginAPICall();
            getControlStructObject().setEscalation(escalation, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Set Escalation Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void createSupportFile() {
        try {
            outputWriter.driverPrintHeader("CREATE SUPPORT FILE");

            // Create an instance of a Support File
            SupportFile supportFile = new SupportFile();

            // Get and Set Support File key
            SupportFileKey key = InputReader.getSupportFileKey();
            supportFile.setKey(key);

            // Get and set the file name
            String fileName = InputReader.getString("File name ():", "");
            supportFile.setFilePath(fileName);

            // Create the support file
            beginAPICall();
            getControlStructObject().createSupportFile(supportFile);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Create Support File Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void deleteSupportFile() {
        try {
            outputWriter.driverPrintHeader("DELETE SUPPORT FILE");

            // Create an instance of a Support File
            SupportFile supportFile = new SupportFile();

            // Get and Set Support File key
            SupportFileKey key = InputReader.getSupportFileKey();
            supportFile.setKey(key);

            // delete the support file
            beginAPICall();
            getControlStructObject().deleteSupportFile(supportFile.getKey());
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Delete Support File Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getSupportFile() {
        try {
            outputWriter.driverPrintHeader("GET SUPPORT FILE");

            SupportFileKey key = InputReader.getSupportFileKey();

            String filePath = InputReader.getString("Filename to hold returned file: ", "");

            beginAPICall();
            SupportFile supportFile = getControlStructObject().getSupportFile(key.getName(), key.getFileId(),
                    key.getFileType(), key.getFieldId(), filePath);
            endAPICall(getControlStructObject().getLastStatus());

            // print the SupportFile data
            outputWriter.printSupportFile("", "SupportFile Data:", supportFile);

            // Store the support file data in the given location
            if (supportFile != null) {
                String tempPath = supportFile.getFilePath();
                String contents = InputReader.getFileContents(tempPath);
                FileWriter newFp = new FileWriter(tempPath);
                newFp.write(contents, 0, contents.length());
                newFp.close();
            }

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get Support File Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void setSupportFile() {
        try {
            outputWriter.driverPrintHeader("SET SUPPORT FILE");

            // Create an instance of a Support File
            SupportFile supportFile = new SupportFile();

            // Get and Set Support File key
            SupportFileKey key = InputReader.getSupportFileKey();
            supportFile.setKey(key);

            // Get and set the file name
            String fileName = InputReader.getString("File name to load to server():", "");
            supportFile.setFilePath(fileName);

            // Create the support file
            beginAPICall();
            getControlStructObject().setSupportFile(supportFile);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Set SupportFile Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListSupportFile() {
        try {
            outputWriter.driverPrintHeader("GET List SUPPORT FILE");

            int fileType = InputReader.getInt("File type -- external report (1) (1): ", 1);
            String name = InputReader.getString("Nameof associated object ():", "");
            int fieldId = InputReader.getInt("   Supporting ID for object (0): ", 0);
            long changedSince = InputReader.getLong("Get all changed since (0):", 0);

            // Get the list of support files
            beginAPICall();
            List<SupportFile> fileList = getControlStructObject().getListSupportFileObjects(fileType, name, fieldId,
                    changedSince);
            endAPICall(getControlStructObject().getLastStatus());

            // Now print the support files
            outputWriter.print("", "Support Files:", fileList);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get List SupportFile STatus", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListGroup() {
        try {
            outputWriter.driverPrintHeader("GET LIST GROUP");

            String user = null;
            String password = null;
            if (InputReader.getBooleanForChangingInfo("For a specific user? (F): ", false)) {
                user = InputReader.getString("Name of the user: ");
                password = InputReader.getString("Password of the User: ");
            }

            // Get the Group list
            beginAPICall();
            List<GroupInfo> groupList = getControlStructObject().getListGroup(user, password);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the group Information
            outputWriter.print("", "ARGetListGroup Results:", groupList);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARGetListGroup Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListRole() {
        try {
            outputWriter.driverPrintHeader("GET LIST ROLE");
            String application = null;
            String user = null;
            String password = null;

            application = InputReader.getString("Application name:");

            if (InputReader.getBooleanForChangingInfo("For a specific user? (F): ", false)) {
                user = InputReader.getString("Name of the user: ");
                password = InputReader.getString("Password of the User: ");
            }

            // Get the Role list
            beginAPICall();
            List<RoleInfo> roleList = getControlStructObject().getListRole(application, user, password);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the role Information
            outputWriter.print("", "ARGetListRole Results:", roleList);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARGetListRoles Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListUser() {
        try {
            outputWriter.driverPrintHeader("GET LIST USER");

            // Get the type
            int type = InputReader.getInt("User List Type (myself, registered or current (0-2)(1): ", 1);

            // Get the changed since parameter
            long changedSince = InputReader.getLong("Get registered users changed since (0 means all): ", 0);

            // Get the Users information
            beginAPICall();
            List<UserInfo> userList = getControlStructObject().getListUser(type, changedSince);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the group Information
            outputWriter.print("", "ARGetListUser Results:", userList);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARGetListUser Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getServerInfo() {
        try {
            outputWriter.driverPrintHeader("GET SERVER INFO");


            int total = InputReader.getInt("Number of server info operations (1): ", 1);
            if (total < 1)
                total = 1; // this is the default value.
            // Get the request list
            int[] requestList = new int[total];
            for (int j = 0; j < total; j++) {
                requestList[j] = InputReader.getInt("   Operation (1-" + Constants.AR_MAX_SERVER_INFO_USED + ") (1): ",
                        1);
            }

            // Get the Server information
            beginAPICall();
            ServerInfoMap serverInfoMap = getControlStructObject().getServerInfo(requestList);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the server information
            outputWriter.print("", "ARGetServerInfo Results:", serverInfoMap);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARGetServerInfo Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void setServerInfo() {
        try {
            outputWriter.driverPrintHeader("SET SERVER INFO");

            // Get the Server information
            ServerInfoMap serverInfoMap = InputReader.getServerInfoMap();

            // Call ARSetServerInfo method
            beginAPICall();
            getControlStructObject().setServerInfo(serverInfoMap);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Set Server Information Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void setServerPort() {
        try {
            outputWriter.driverPrintHeader("SET SERVER PORT");

            // Get the port number
            int portNumber = InputReader.getInt("The port number of server (0):", 0);

            // Get the rpc number
            int progNumber = InputReader.getInt("The RPC program number of Server (0):", 0);

            // Call ARSetServerPort method
            getControlStructObject().setPort(portNumber);
            getControlStructObject().usePrivateRpcQueue(progNumber);

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

    void export() {
        try {

            outputWriter.driverPrintHeader("ARExport");

            int exportOption = 0;
            // Get the items
            List<StructItemInfo> items = InputReader.getStructItemInfoList();

            // If exporting any VIEWs, prompt for the display tag and vui-type
            String displayTag = null;
            int vuiType = 0;

            WorkflowLockInfo lockInfo = InputReader.getWorkflowLockInfo();
            boolean asXml = false;
            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                	if (items.get(i).getType() >= Constants.AR_STRUCT_XML_OFFSET)
                		asXml = true;
                    if (items.get(i).getType() == StructItemInfo.SCHEMA_VIEW
                            || items.get(i).getType() == StructItemInfo.VUI_2) {
                        displayTag = InputReader.getString("Display Tag");
                        vuiType = InputReader.getInt("VUI- Type (0): ", 0);
                        break;
                    }
                }
                for (int i = 0; i < items.size(); i++) {
                	if (items.get(i).getType() == StructItemInfo.APPLICATION) {
                		exportOption = items.get(i).getExportOption();
                		break;
                	}
                }
            }

            String fileName = InputReader.getString("Filename for exported data: ", "");

            // Call the ARExport method
            beginAPICall();
            getControlStructObject().exportDefToFile(items, asXml, displayTag, vuiType, lockInfo, 
                                                     fileName, true, exportOption);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARExport Status", statusList);

        } catch (IOException e) {
            outputWriter.printString(e.getLocalizedMessage());
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getServerStatistics() {
        try {

            outputWriter.driverPrintHeader("GET SERVER STATISTICS");

            // Get the number of serverInfo operations
            int numberOfOperations = InputReader.getInt("Number Of server info Operations (0):", 0);

            // Get the operations
            int[] requestList = null;
            if (numberOfOperations != 0) {
                requestList = new int[numberOfOperations];

                for (int i = 0; i < numberOfOperations; i++) {
                    requestList[i] = InputReader.getInt("Operation (1-" + Constants.AR_MAX_SERVER_STAT_USED + " ):", 1);
                }
            }

            // Call the ARGetServerStatistics method
            beginAPICall();
            ServerInfoMap serverInfoMap = getControlStructObject().getServerStatistics(requestList);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the information
            outputWriter.printString("ARGetServerStatistics Results\n");
            outputWriter.print("", "Server Information", serverInfoMap);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARGetServerStatistics Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void executeProcess() {
        try {

            outputWriter.driverPrintHeader("EXECUTE PROCESS");

            // Get the command
            String command = InputReader.getString("Command:", "");

            // Get the flag whether to wait for the process or not
            boolean waitFlag = InputReader.getBooleanForChangingInfo("Wait For process to complete (F): ", false);

            // Call the ARExecuteProcess Method
            beginAPICall();
            ProcessResult result = getControlStructObject().executeProcess(command, waitFlag);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the result
            outputWriter.printProcessResult("", "ARExecuteProcess Results:", result);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARExecuteProcess Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListSQL() {
        try {

            outputWriter.driverPrintHeader("GETLIST SQL");

            // Get the SQL Command
            String command = InputReader.getString("SQL Command:", "");

            // Get Maximum number of entries to retrieve
            int numberOfEntries = InputReader.getInt("Maximum number of entries to retrieve (500):", 500);

            // Get the flag to return the number of matches
            boolean flag = InputReader.getBooleanForChangingInfo("Get Number Of Matches? (F): ", false);

            // Call the ARGetListSQL
            beginAPICall();
            SQLResult result = getControlStructObject().getListSQL(command, numberOfEntries, flag);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the result
            outputWriter.printSQLResult("", "ARGetListSQL Results:", result);
            
            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARGetListSQL Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getTextForErrorMessage() {

        try {

            outputWriter.driverPrintHeader("GET TEXT FOR ERROR MESSAGE");

            // Get the
            int msgNumber = InputReader.getInt("The message identifier for the message (0):", 0);

            // Call the ARGetTextForErrorMessage method
            beginAPICall();
            String message = getControlStructObject().getTextForErrorMessage(msgNumber);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the message
            outputWriter.printString("", "ARGetTextForErrorMessage Results:", message, "\n");

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARGetTextForErrorMessage Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getEntryBLOB() {
        try {
            outputWriter.driverPrintHeader("GET ENTRY BLOB");

            // Get the Form Name
            String form = InputReader.getString("Form Name: ");

            // Get Entry ID
            String entryId = InputReader.getString("Entry ID:", "");

            // Get Field ID
            int fieldId = InputReader.getInt("Field ID (1):", 1);

            String filePath = InputReader.getString("File path to retrieve the attachment value :", "");

            byte[] content = null;
            // Call ARGetEntryBlob method
            beginAPICall();
            if ((filePath != null) && (filePath.length() > 0))
                getControlStructObject().getEntryBlob(form, entryId, fieldId, filePath);
            else {
                outputWriter.printString("File path was not supplied, so content will be retrieved into a buffer\n");
                content = getControlStructObject().getEntryBlob(form, entryId, fieldId);
            }
            endAPICall(getControlStructObject().getLastStatus());

            // Check if the content is brought correctly if it is a buffer
            if ((filePath != null) && (filePath.length() > 0) && ((new File(filePath)).exists())) {
                outputWriter.printString("File retrieved to: " + filePath + "\n");
            } else {
                if (content != null) {
                    outputWriter.printString("Buffer size is: " + content.length + "\n");
                } else {
                    outputWriter.printString("Empty buffer...\n");
                }
            }

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get Entry Blob Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void verifyUser() {
        try {

            outputWriter.driverPrintHeader("VERIFY USER");

            // Now call the verifyUser method
            beginAPICall();
            endAPICall(getControlStructObject().getLastStatus());

            // Print the information
            outputWriter.print("", "Verify User Results:", getControlStructObject());
            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Verify User Status", statusList);

        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        }
    }

    void encodeARQualifierStruct() {
        try {
            outputWriter.driverPrintHeader("ENCODE ARQUALIFIER STRUCT TO STRING");
            QualifierInfo qal = InputReader.getQualifierInfo();

            beginAPICall();
            String qaltxt = getControlStructObject().encodeQualification(qal);
            endAPICall(getControlStructObject().getLastStatus());

            if (qaltxt != null) {
                outputWriter.printHeader("", "Qualifier Text: ", "\n");
                outputWriter.printString(qaltxt);
            }

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "EncodeQualifier Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.printString("Null Pointer exception...\n");
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void decodeARQualifierStruct() {
        try {
            outputWriter.driverPrintHeader("DECODE STRING TO ARQUALIFIER STRUCT");
            String qaltxt = InputReader.getString("Qualifier Text: ", "");

            beginAPICall();
            QualifierInfo qal = getControlStructObject().decodeQualification(qaltxt);
            endAPICall(getControlStructObject().getLastStatus());

            if (qal != null) {
                outputWriter.printQualifierInfo("", "ARDecodeARQualifierStruct: ", qal);
            }

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARDecodeARQualifierStruct  Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void encodeARAssignStruct() {
        try {
            outputWriter.driverPrintHeader("ENCODE ARASSIGN STRUCT TO STRING");
            AssignInfo asn = InputReader.getAssignInfo();

            beginAPICall();
            String asntxt = getControlStructObject().encodeAssignment(asn);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printHeader("", "Assign Text: ", "\n");
            if (asntxt != null) {
                outputWriter.printString(asntxt);
                outputWriter.printNewLine();
            }

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "AREncodeARAssignStruct Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void decodeARAssignStruct() {
        try {
            outputWriter.driverPrintHeader("DECODE STRING TO ARASSIGN STRUCT");
            String asntxt = InputReader.getString("Assign Text: ", "");

            beginAPICall();
            AssignInfo asn = getControlStructObject().decodeAssignment(asntxt);
            endAPICall(getControlStructObject().getLastStatus());

            if (asn != null) {
                outputWriter.printAssignInfo("", "ARDecodeARAssignStruct: ", asn);
            }

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARDecodeARAssignStruct Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void encodeStatusHistory() {
        try {
            outputWriter.driverPrintHeader("ENCODE STATUS HISTORY ARRAY TO STRING");
            StatusHistoryValue statHist = InputReader.getStatusHistory();

            beginAPICall();
            String statHistTxt = statHist.encode();
            endAPICall(getControlStructObject().getLastStatus());

            if (statHistTxt != null) {
                outputWriter.printString("Status History String: " + "\n");
                outputWriter.printString(statHistTxt);

                //Decode the status history and print the decoded status history items
                //This is to check whether the encode is properly done or not

                outputWriter.printStatusHistoryString("", "Status History Items:", statHistTxt);

                // Success. Print the status
                List<StatusInfo> statusList = getControlStructObject().getLastStatus();
                outputWriter.printStatusInfoList("", "AREncodeStatusHistory Status", statusList);
            } else {
                outputWriter.printString("Status History String: Null\n");
            }
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        }
    }

    void encodeDiary() {
        try {
            outputWriter.driverPrintHeader("ENCODE DIARY ARRAY TO STRING");
            DiaryListValue diary = InputReader.getDiaryInfoList();

            beginAPICall();
            String diarytxt = ARServerUser.encodeDiary(diary);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printHeader("", "DiaryList Text: ", "\n");
            if (diarytxt != null) {
                outputWriter.printString(diarytxt);

                // Decode the diary string and print the decoded diary items
                // This is to check whether the encode is properly done or not

                outputWriter.printString("", "DiaryList Items:", diarytxt);
                DiaryListValue diaryList = DiaryListValue.decode(diarytxt);
                outputWriter.printDiaryList("", "DiaryList Items:", diaryList);

            } else {
                outputWriter.printString("Encoded diary String: Null\n");
            }

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "AREncodeDiary Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    private void deleteAlert() {
        outputWriter.driverPrintHeader("DELETE ALERT");
        // Get the Entry ID 
        String entryId;
        try {
            entryId = InputReader.getString("Entry ID:", "");
            beginAPICall();
            getControlStructObject().deleteAlert(entryId);
            endAPICall(getControlStructObject().getLastStatus());
            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "deleteAlert Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void createAlertEvent() {
        try {

            outputWriter.driverPrintHeader("CREATE ALERT EVENT");

            // Get the User information
            String user = InputReader.getString("User");

            // Get the alert Text
            String alertText = InputReader.getString("Alert Text(): ", "");

            // Get the alert Priority
            int alertPriority = InputReader.getInt("Alert Priority (0):", 0);

            // Get the alert Source
            String alertSource = InputReader.getString("Alert Source");

            // Get the Server information
            String serverName = InputReader.getString("Server Name: ", "");

            // Get the Form information
            String formName = InputReader.getString("Form Name");

            // Get the Object Id
            String objectId = InputReader.getString("Object Id(): ", "");

            beginAPICall();
            String entryId = getControlStructObject().createAlertEvent(user, alertText, alertPriority, alertSource,
                    serverName, formName, objectId);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the String
            outputWriter.printString("", "String:", entryId);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARCreateAlertEvent Status", statusList);

        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void registerForAlerts() {
        try {

            outputWriter.driverPrintHeader("REGISTER FOR ALERTS");
        	
            int clientPort = InputReader.getInt("clientPort (0):", 0);
            int registrationFlags = InputReader.getInt("registrationFlags (0):", 0);

            // Call ARRegisterForAlerts method
            beginAPICall();
            getControlStructObject().registerForAlerts(clientPort, registrationFlags);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARRegisterForAlerts Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void deregisterForAlerts() {
        try {

            outputWriter.driverPrintHeader("Deregister For Alerts");

            // Get the
            int clientPort = InputReader.getInt("clientPort (0):", 0);

            // Call ARDeregisterForAlerts method
            beginAPICall();
            getControlStructObject().deregisterForAlerts(clientPort);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARDeregisterForAlerts STatus", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListAlertUser() {
        try {

            outputWriter.driverPrintHeader("GET LIST ALERT USER");

            // Call ARGetListAlertUser method
            beginAPICall();
            List<String> users = getControlStructObject().getListAlertUser();
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.print("", "users List: ", users);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARGetListAlertUser Status", statusList);

        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getAlertCount() {
        try {

            outputWriter.driverPrintHeader("GET ALERT COUNT");
            QualifierInfo qal = InputReader.getQualifierInfo();

            beginAPICall();
            int count = getControlStructObject().getAlertCount(qal);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printInt("", "ALert Count = ", count, "\n");

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARGetAlertCount Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getLocalizedValue() {
        try {
            outputWriter.driverPrintHeader("GET LOCALIZED VALUE");

            LocalizedRequestInfo request = InputReader.getLocalizedRequestInfo();

            LocalizedValueCriteria criteria = InputReader.getLocalizedValueCriteria();

            LocalizedValueInfo localizedValue;

            beginAPICall();
            localizedValue = getControlStructObject().getLocalizedValue(criteria, request);
            endAPICall(getControlStructObject().getLastStatus());

            if (localizedValue != null) {
                if (localizedValue.getValue() != null) {
                    outputWriter.printValue("   ", "Localized Value: ", localizedValue.getValue());
                }

                if (localizedValue.getTimestamp() != null) {
                    outputWriter.printString("Got the time stamp");
                }

                outputWriter.printNewLine();
            }

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARGetLocalizedValue Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getMultipleLocalizedValues() {
        try {
            outputWriter.driverPrintHeader("GET LOCALIZED VALUES");

            int numRequests = InputReader.getInt("Number of requests (1): ", 1);
            List<LocalizedRequestInfo> requests = new ArrayList<LocalizedRequestInfo>();
            if (numRequests > 0) {
                for (int i = 0; i < numRequests; i++) {
                    requests.add(InputReader.getLocalizedRequestInfo());
                }
            }

            LocalizedValueCriteria criteria = InputReader.getLocalizedValueCriteria();

            beginAPICall();
            List<LocalizedValueInfo> localizedValues = getControlStructObject().getMultipleLocalizedValues(criteria,
                    requests);
            endAPICall(getControlStructObject().getLastStatus());

            for (int i = 0; i < localizedValues.size(); i++) {
                if (localizedValues.get(i).getValue() != null) {
                    outputWriter.printValue("   ", "Localized Request " + i + ": ", localizedValues.get(i).getValue());
                }
                if (localizedValues.get(i).getTimestamp() != null) {
                    outputWriter
                            .printLong("   ", "Timestamp: ", localizedValues.get(i).getTimestamp().getValue(), "\n");
                }
            }

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARGetMultipleLocalizedValues Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListFormWithAlias() {
        try {
            long changedSince = InputReader.getLong("Get All changed since, as UNIX epoch (0): ", 0);
            FormType formType = InputReader.getFormType();
            String formName = null;
            if ((formType != null) && (formType.equals(FormType.UPLINK) || formType.equals(FormType.DOWNLINK))) {
                formName = InputReader.getString("Form name (): ", "");
            }
            outputWriter.driverPrintPrompt("Ids of fields, which must be on the form:\n");
            int[] fieldIds = InputReader.getIntArray();
            String vuiLabel = InputReader.getString("VUI Label (): ", "");

            beginAPICall();
            List<FormAliasInfo> formAliases = getControlStructObject().getListFormAliases(changedSince,
                    formType.toInt(), formName, fieldIds, vuiLabel);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.print("", "Form Alias Info", formAliases);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "findAliases Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void decodeAlertMessage() {
        try {
            outputWriter.driverPrintHeader("DECODE ALERT MESSAGE");

            AlertMessageCriteria criteria = new AlertMessageCriteria();

            String message = InputReader.getString(" Message (): ", "");

            int len = message.length();

            // This function expects binary data so we are not expecting this to work with character input

            beginAPICall();
            AlertMessageInfo result = getControlStructObject().decodeAlertMessage(criteria, message.getBytes(), len);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the result
            outputWriter.printAlertMessageInfo("", "DecodeAlertMessage Results: ", result);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void executeProcessForActiveLink() {
        try {
            outputWriter.driverPrintHeader("EXECUTE PROCESS FOR ACTIVE LINK");

            // Get the Active Link Name
            String actlinkName = InputReader.getString("Name of Active Link");

            int actionIndex = InputReader.getInt("Action Index (0): ", 0);

            int actionType = InputReader.getInt("actionType (0): ", 0);

            int fieldID = InputReader.getInt("fieldid(0):", 0);

            Timestamp time = InputReader.getTimestamp("timestamp", new Timestamp());

            // Get the Keyword list
            outputWriter.driverPrintPrompt("Keyword List: \n");
            List<Value> keywordsList = InputReader.getValueList();

            // Get Parameter List
            outputWriter.driverPrintPrompt("Parameter List:\n");
            List<Value> parametersList = InputReader.getValueList();

            // Get the flag whether to wait for the process or not
            boolean waitFlag = InputReader.getBooleanForChangingInfo("Wait For process to complete (F): ", false);

            // Not implemented in Java.  Just for the sake of making the C And Java Driver Equal.
            InputReader.getBooleanForChangingInfo("Get return command? (F): ", false);

            // Call the ARExecuteProcess Method
            beginAPICall();
            ProcessResult result = getControlStructObject().executeProcessForActiveLink(actlinkName, actionIndex,
                    actionType, fieldID, time, keywordsList, parametersList, waitFlag);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the result
            outputWriter.printProcessResult("", "ARExecuteProcessForActiveLink Results:", result);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARExecuteProcessForActiveLink Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListSQLForActiveLink() {
        try {
            outputWriter.driverPrintHeader("GETLIST SQL FOR ACTIVE LINK");

            // Get the Active Link Name
            String actlinkName = InputReader.getString("Name of Active Link ");

            int actionIndex = InputReader.getInt("Action Index (0): ", 0);

            int actionType = InputReader.getInt("action type (0): ", 0);

            Timestamp time = InputReader.getTimestamp("timestamp", new Timestamp());

            // Get the Keyword list
            outputWriter.driverPrintPrompt("Keyword List: \n");
            List<Value> keywordsList = InputReader.getValueList();

            // Get Parameter List
            outputWriter.driverPrintPrompt("Parameter List:\n");
            List<Value> parametersList = InputReader.getValueList();

            int maxRetrieve = InputReader.getInt("Maximum number of entries to retrieve (500): ", 500);

            // Get the flag whether to get number of matches or not
            boolean retrieveTotalMatches = InputReader.getBooleanForChangingInfo("Get Number of Matches (F): ", false);

            // Not implemented in Java.  Just for the sake of making the C And Java Driver Equal.
            InputReader.getBooleanForChangingInfo("Get return command? (F): ", false);

            beginAPICall();
            SQLResult result = getControlStructObject().getListSQLForActiveLink(actlinkName, actionIndex, actionType,
                    time, keywordsList, parametersList, maxRetrieve, retrieveTotalMatches);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the result
            outputWriter.printSQLResult("", "ARGetListSQLForActiveLink Results:", result);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARGetListSQLForActiveLink Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListExtFormCandidates() {
        try {
            outputWriter.driverPrintHeader("GET LIST EXTERNAL SCHEMA CANDIDATES");
            int formType = InputReader.getInt("Form Type (3(view) or 5(vendor)): ", 3);
            beginAPICall();
            List<ExtFormCandidatesInfo> result = getControlStructObject().getListExtFormCandidates(formType);
            endAPICall(getControlStructObject().getLastStatus());
            // Print the result
            outputWriter.print("", "ARGetListExtFormCandidates Results: ", result);
            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARGetListExtFormCandidates Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getMultipleExtFieldCandidates() {
        try {
            outputWriter.driverPrintHeader("GET MULTIPLE EXTERNAL FIELD CANDIDATES");
            int type = InputReader.getInt("Form Type (3(view) or 5(vendor)): ", 3);
            String vendorName = "", tableName = "";
            if (type == Constants.AR_SCHEMA_VIEW) {
                tableName = InputReader.getString("Table Name: ");
            } else if (type == Constants.AR_SCHEMA_VENDOR) {
                vendorName = InputReader.getString("Vendor Name: ");
                tableName = InputReader.getString("Table Name: ");
            } else {
                outputWriter.printString("Form type not applicable");
                return;
            }

            List<ExtFieldCandidatesInfo> result = null;
            beginAPICall();
            if (type == Constants.AR_SCHEMA_VIEW)
                result = getControlStructObject().getListViewFormFieldCandidates(tableName);
            else if (type == Constants.AR_SCHEMA_VENDOR)
                result = getControlStructObject().getListVendorFormFieldCandidates(vendorName, tableName);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the result
            outputWriter.print("", "ARGetListExtFormCandidates Results: ", result);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARGetListExtFormCandidates Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getEntryStatistics() {
        try {
            outputWriter.driverPrintHeader("GET ENTRY STATISTICS");

            String formKey = InputReader.getString("Form Key:", "");

            QualifierInfo qualifier = InputReader.getQualifierInfo();

            // Get the target expression
            outputWriter.driverPrintHeader("Target Expression:");
            ArithmeticOrRelationalOperand target = InputReader.getArithmeticOrRelationalOperand(null, "");

            int type = InputReader.getInt("Statistic Operation to Perform (Count,Sum,Avg,Min,Max) (1-5)(1):", 1);

            outputWriter.driverPrintPrompt("Ids of fields to group statistics by:\n ");
            int[] idList = InputReader.getInternalIDList();

            // Call the ARGetEntryStatistics method
            beginAPICall();
            List<StatisticsResultInfo> statResInfoList = getControlStructObject().getEntryStatistics(formKey,
                    qualifier, target, type, idList);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.print("", "GET ENTRY STATISTICS RESULTS:", statResInfoList);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARGetEntryStatistics Status", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void validateLicense() {
        try {
            // print header
            outputWriter.driverPrintHeader("VALIDATE LICENSE");

            // Get the license type
            String licType = InputReader.getString("License Type: ");

            // validate the license
            beginAPICall();
            LicenseValidInfo licValidInfo = getControlStructObject().validateLicense(licType);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the validate info
            outputWriter.printLicenseValidInfo("", "ARValidateLicense Results:", licValidInfo);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getMultipleFields() {
        try {
            outputWriter.driverPrintHeader("GET MULTIPLE FIELDS");

            // Create an empty field

            String formName = InputReader.getString("Form Name (): ", "");

            outputWriter.driverPrintPrompt("Ids of fields to retrieve:\n");
            int[] fieldIdList = InputReader.getIntArray();

            // Set the field criteria to retrieve all
            FieldCriteria criteria = new FieldCriteria();
            criteria.setRetrieveAll(true);

            // Find the fields
            beginAPICall();
            List<Field> fieldList = getControlStructObject()
                    .getListFieldObjects(formName, fieldIdList, criteria);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the information
            outputWriter.print("", "Field List Information:", fieldList);
            outputWriter.printStatusInfoList("", "Status", getControlStructObject().getLastStatus());

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListLicense() {
        try {
            // print header
            outputWriter.driverPrintHeader("GET LIST LICENSE");

            // Get the license type
            String licType = InputReader.getString("License Type: ");

            // validate the license
            beginAPICall();
            List<LicenseInfo> licInfo = getControlStructObject().getListLicense(licType);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the validate info
            outputWriter.print("", "ARGetListLicense Results:", licInfo);
            outputWriter.printStatusInfoList("", "Status", getControlStructObject().getLastStatus());
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void validateMultipleLicenses() {
        try {
            // print header
            outputWriter.driverPrintHeader("VALIDATE MULTIPLE LICENSE");

            // Get the license type
            List<String> licTypeList = InputReader.getStringList("", "license");

            // validate the license
            beginAPICall();
            List<LicenseValidInfo> licValidList = getControlStructObject().validateMultipleLicense(licTypeList);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the license info
            outputWriter.print("", "ARValidateMultipleLicenses Results:", licValidList);
            outputWriter.printStatusInfoList("", "Status", getControlStructObject().getLastStatus());
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListApplicationState() {
        try {
            outputWriter.driverPrintHeader("GET LIST APPLICATION STATE");
            beginAPICall();
            List<String> list = getControlStructObject().getListApplicationState();
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.print("", "getListApplicationState Results:", list);
            outputWriter.printStatusInfoList("", "Status", getControlStructObject().getLastStatus());
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getApplicationState() {
        try {
            outputWriter.driverPrintHeader("GET APPLICATION STATE");
            String applicationName = InputReader.getString("Application Name:");
            beginAPICall();
            String state = getControlStructObject().getApplicationState(applicationName);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.print("", "Application State Data:", state);
            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get Application State Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void setApplicationState() {
        try {
            outputWriter.driverPrintHeader("SET APPLICATION STATE");
            String applicationName = InputReader.getString("Application Name:");
            String state = InputReader.getString("New state:");
            beginAPICall();
            getControlStructObject().setApplicationState(applicationName, state);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.print("", "Application State Data:", state);
            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Set Application State Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }
    
    void getObjectChangeTimes() {
        try {
            outputWriter.driverPrintHeader("GET OBJECT CHANGE TIMES");
            beginAPICall();
            HashMap<Integer, ObjectOperationTimes> objList = getControlStructObject().getObjectChangeTimes();
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printString("Object Change Times:");
            outputWriter.printObjectOperationTimes("", "OP TIMES", objList);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get Object Change Times Status", statusList);
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getImage() {
        try {
            outputWriter.driverPrintHeader("GET IMAGE");

            String imageName = InputReader.getString("Image Name: ", "");
            
            String fileName = InputReader.getString("Filename to hold returned image (): ", ""); 

            ImageCriteria criteria = new ImageCriteria();
            criteria.setRetrieveAll(true);

            beginAPICall();
            Image image = (Image) getControlStructObject().getImage(imageName, criteria);
            endAPICall(getControlStructObject().getLastStatus());
            
            outputWriter.printImage("", "Image Information...", image);
            // Store image to file
            if (image != null && image.getImageData() != null ) {
                image.getImageData().writeToFile(fileName);
                String fileInfo = "Image \"" + image.getName() + "\" saved to ";
                outputWriter.printString("", fileInfo, fileName);
                outputWriter.printNewLine();
            }
            
            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get Image Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }
    
    void getMultipleImages() {
        try {
            outputWriter.driverPrintHeader("GET MULTIPLE IMAGES");

            long changedSince = InputReader.getLong("Get All changed since, as UNIX epoch (0): ", 0);

            outputWriter.driverPrintPrompt("Get images by name\n");
            List<String> imageList = InputReader.getStringList("", "");
            
            outputWriter.driverPrintHeader("Images are returned in files based on image name and type");
            String fileDir = InputReader.getString("Directory to place resulting images (): ", ""); 

            ImageCriteria criteria = new ImageCriteria();
            criteria.setRetrieveAll(true);

            beginAPICall();
            List<Image> objList = (List<Image>) getControlStructObject().getListImageObjects(imageList, changedSince, criteria);
            endAPICall(getControlStructObject().getLastStatus());
            if (objList != null && !objList.isEmpty()) {
                for (Image image : objList) {
                    outputWriter.printImage("", "Image Information...", image);
                    // Store image to file
                    if (image != null && image.getImageData() != null ) {
                        String fileName = null;
                        if (fileDir != null && (fileDir.length() > 0)) {
                            fileName = fileDir + "\\" + image.getName() + "." + image.getType();
                        } else {
                            fileName = ".\\" + image.getName() + "." + image.getType();
                        }
                        image.getImageData().writeToFile(fileName);
                        String fileInfo = "Image \"" + image.getName() + "\" saved to ";
                        outputWriter.printString("", fileInfo, fileName);
                        outputWriter.printNewLine();
                    }
                }
            }

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Get Multiple Images Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }
       
    void setImage() {

        try {
            outputWriter.driverPrintHeader("SET IMAGE");

            Image image = new Image();
            String name = InputReader.getString("Image Name: ");
            image.setName(name);

            if (InputReader.getBooleanForChangingInfo("Change image name? (F): ", false)) {
                String newName = InputReader.getString("Image name: ");
                image.setNewName(newName);
            }

            if (InputReader.getBooleanForChangingInfo("Change image type? (F): ", false)) {
                outputWriter.driverPrintHeader("Enter Image type (i.e., \"jpg\", \"bmp\", etc.)");
                String imageType = InputReader.getString("Image type (): ", "");
                image.setType(imageType);
            }

            if (InputReader.getBooleanForChangingInfo("Change description? (F): ", false)) {
                String description = InputReader.getString("Description (): ", "");
                image.setDescription(description);
            }

            if (InputReader.getBooleanForChangingInfo("Change image? (F): ", false)) {
                // Get and set the file name
                String fileName = InputReader.getString("Image File name (): ", "");
                ImageData data = new ImageData(fileName);
                image.setImageData(data);
             }

            if (InputReader.getBooleanForChangingInfo("Change help text? (F): ", false)) {
                String help = InputReader.getString("Help Text (): ", "");
                image.setHelpText(help);
            }

            if (InputReader.getBooleanForChangingInfo("Change owner? (F): ", false)) {
                String owner = InputReader.getString("New Owner");
                image.setOwner(owner);
            }

            if (InputReader.getBooleanForChangingInfo("Add to change diary? (F): ", false)) {
                String diary = InputReader.getString("Change DiaryList (): ", "");
                image.appendDiaryText(diary);
            }

            if (InputReader.getBooleanForChangingInfo("Change object properties? (F): ", false)) {
                ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
                image.setProperties(propList);
            }
            
            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();

            beginAPICall();
            getControlStructObject().setImage(image, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Set Image Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }
    
    void createImage() {
        try {
            outputWriter.driverPrintHeader("CREATE IMAGE");
            
            // Create a new image instance
            Image image = new Image();

            // Get and Set the filter name
            String name = InputReader.getString("Image Name: ");
            image.setName(name);

            // Get and set the image type
            outputWriter.driverPrintHeader("Enter Image type (i.e., \"jpg\", \"bmp\", etc.)");
            String imageType = InputReader.getString("Image Type (\"jpg\"): ", "jpg");
            image.setType(imageType);

            // Get and set the description
            String description = InputReader.getString("Description (): ", "");
            image.setDescription(description);
            
             // Get and set the Image Data
            ImageData data = InputReader.getImageData();//instead of just putting string name we 
            image.setImageData(data); // need to put option and imagedata  
 
            // Get and set the help text
            String helpText = InputReader.getString("Help Text (): ", "");
            image.setHelpText(helpText);

            // Get and Set the owner
            String owner = InputReader.getString("Owner (): ");
            image.setOwner(owner);

            // Get and Set change diary
            String diary = InputReader.getString("Change DiaryList (): ", "");
            image.appendDiaryText(diary);

            // Get and set properties
            ObjectPropertyMap propList = InputReader.getObjectPropertyMap();
            image.setProperties(propList);
            
            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();

            // Now create the image in the database
            beginAPICall();
            getControlStructObject().createImage(image, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Create Image Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void deleteImage() {
        try {
            outputWriter.driverPrintHeader("DELETE IMAGE");
            String imageName = InputReader.getString("Image to delete: ");
            boolean updateRef = InputReader.getBooleanForChangingInfo("Delete image references in field display props? (F): ", false);
            
            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();

            beginAPICall();
            getControlStructObject().deleteImage(imageName, updateRef, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "DeleteImage Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getListImage() {
        try {
            outputWriter.driverPrintHeader("GET LIST IMAGE");
            boolean searchAll = InputReader.getBoolean("Search all schemas for associated images? (T): ", true);
            List<String> formList = null;
            if (searchAll == false) {
                outputWriter.driverPrintPrompt("Get schemas by name\n");
                formList = InputReader.getStringList("", "");
            }
            long changedSince = InputReader.getLong("Get All changed since, as UNIX epoch (0): ", 0);

            String imageType = InputReader.getString("Get specific image type (\"jpg\", \"bmp\", etc.) (\"\"): ", "");

            beginAPICall();
            List<String> imageList = getControlStructObject().getListImage(formList, changedSince, imageType);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.print("", "Image list: ", imageList);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "GetListImage Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void printDriverVersion() {
        outputWriter.driverPrintHeader(Version.clientVersion);
    }
    
    void getSessionConfiguration() {
        try {
            outputWriter.driverPrintHeader("GET SESSION CONFIGURATION");

            int variableId = InputReader.getInt("Session Variable Id (0): ", 0);

            beginAPICall();
            ARServerUser ctx = getControlStructObject();
            int variableValue = 0;
            switch (variableId) {
            case Constants.AR_SESS_CHUNK_RESPONSE_SIZE:
                variableValue = ctx.getChunkResponseSize();
                break;
            case Constants.AR_SESS_TIMEOUT_NORMAL:
                variableValue = ctx.getTimeoutNormal();
                break;
            case Constants.AR_SESS_TIMEOUT_LONG:
                variableValue = ctx.getTimeoutLong();
                break;
            case Constants.AR_SESS_TIMEOUT_XLONG:
                variableValue = ctx.getTimeoutXLong();
                break;
            case Constants.AR_SESS_LOCK_TO_SOCKET_NUMBER:
                variableValue = ctx.getServerRpcQueueNumber();
                break;
            case Constants.AR_SESS_CLIENT_TYPE:
                variableValue = ctx.getClientType();
                break;
            case Constants.AR_SESS_VUI_TYPE:
                variableValue = ctx.getVUIType();
                break;
            case Constants.AR_SESS_OVERRIDE_PREV_IP:
                variableValue = (ctx.getOverridePrevIP() == true) ? 1 : 0;
                break;
            case com.bmc.arsys.api.Constants.AR_SESS_CONTROL_PROP_API_OVERLAYGROUP:
            case com.bmc.arsys.api.internal.Constants.AR_SESS_CONTROL_PROP_API_OVERLAYGROUP_D:
                variableValue = 0;
                try {
                    variableValue = Integer.parseInt(ctx.getOverlayGroup());
                } catch (Exception e) {
                    //ignore. The value is not set
                }
                break;
            case com.bmc.arsys.api.Constants.AR_SESS_CONTROL_PROP_DESIGN_OVERLAYGROUP:
            case com.bmc.arsys.api.internal.Constants.AR_SESS_CONTROL_PROP_DESIGN_OVERLAYGROUP_D:
                variableValue = 0;
                try {
                    variableValue = Integer.parseInt(ctx.getDesignOverlayGroup());
                } catch (Exception e) {
                    //ignore. The value is not set
                }
                break;
            default :
                break;
            }
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printInteger("   ", "Session Variable: ", variableValue);
            outputWriter.printStatusInfoList("", "Status", getControlStructObject().getLastStatus());
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        }
    }

    void setSessionConfiguration() {
        try {
            outputWriter.driverPrintHeader("SET SESSION CONFIGURATION");

            int variableId = InputReader.getInt("Variable Id (0): ", 0);
            Value variableValue = InputReader.getValue();
            ARServerUser ctx = getControlStructObject();

            beginAPICall();
            switch (variableId) {
            case Constants.AR_SESS_CHUNK_RESPONSE_SIZE:
                ctx.setChunkResponseSize(((Integer) variableValue.getValue()).intValue());
                break;
            case Constants.AR_SESS_TIMEOUT_NORMAL:
                ctx.setTimeoutNormal(((Integer) variableValue.getValue()).intValue());
                break;
            case Constants.AR_SESS_TIMEOUT_LONG:
                ctx.setTimeoutLong(((Integer) variableValue.getValue()).intValue());
                break;
            case Constants.AR_SESS_TIMEOUT_XLONG:
                ctx.setTimeoutXLong(((Integer) variableValue.getValue()).intValue());
                break;
            case Constants.AR_SESS_LOCK_TO_SOCKET_NUMBER:
                ctx.usePrivateRpcQueue(((Integer) variableValue.getValue()).intValue());
                break;
            case Constants.AR_SESS_CLIENT_TYPE:
                ctx.setClientType(((Integer) variableValue.getValue()).intValue());
                break;
            case Constants.AR_SESS_VUI_TYPE:
                ctx.setVUIType(((Integer) variableValue.getValue()).intValue());
                break;
            case Constants.AR_SESS_OVERRIDE_PREV_IP:
                int intVal = ((Integer) variableValue.getValue()).intValue();
                ctx.setOverridePrevIP((intVal == 1) ? true : false);
                break;
            case com.bmc.arsys.api.Constants.AR_SESS_CONTROL_PROP_API_OVERLAYGROUP:
			case com.bmc.arsys.api.internal.Constants.AR_SESS_CONTROL_PROP_API_OVERLAYGROUP_D:
                ctx.setOverlayGroup((String)variableValue.getValue());
                break;
            case com.bmc.arsys.api.Constants.AR_SESS_CONTROL_PROP_DESIGN_OVERLAYGROUP:
			case com.bmc.arsys.api.internal.Constants.AR_SESS_CONTROL_PROP_DESIGN_OVERLAYGROUP_D:
                ctx.setDesignOverlayGroup((String)variableValue.getValue());
                break;
            default:
                outputWriter.printString("Wrong tag for setSessionConfiguration tag = " + variableId);
                break;
            }
            endAPICall(getControlStructObject().getLastStatus());

            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Set Session Results", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    public void xmlCreateEntry() {
        int bytesRead = 0;
        FileReader fileReader = null;

        try {
            outputWriter.driverPrintHeader("XML CREATE ENTRY");

            // Get the file name for xml input mapping
            String fileName = null;
            for (int i = 0; i < 3; i++) {
                fileName = InputReader.getString("Filename containing XML input mapping: ", "");
                if (fileName == null) {
                    outputWriter.printString("Input file not found\n");
                    continue;
                }
                try {
                    fileReader = new FileReader(fileName);
                    break;
                } catch (FileNotFoundException e) {
                    outputWriter.printString("Input file not found\n");
                    fileName = null;
                }
            }

            if (fileName == null) {
                return;
            }

            String xmlInputMapping = "";
            StringBuffer stringInputMappingBuffer = new StringBuffer();
            char readBuffer[] = new char[1024];
            while ((bytesRead = fileReader.read(readBuffer, 0, readBuffer.length - 1)) >= 0)
                stringInputMappingBuffer.append(readBuffer, 0, bytesRead);
            xmlInputMapping = stringInputMappingBuffer.toString();
            fileReader.close();

            // Get the file name for input document
            fileName = null;
            fileReader = null;

            for (int i = 0; i < 3; i++) {
                fileName = InputReader.getString("Filename containing XML input document: ", "");
                if (fileName == null) {
                    outputWriter.printString("Input file not found\n");
                    continue;
                }
                try {
                    fileReader = new FileReader(fileName);
                    break;
                } catch (FileNotFoundException e) {
                    outputWriter.printString("Input file not found\n");
                    fileName = null;
                }
            }

            if (fileName == null) {
                return;
            }

            String xmlInputDoc = "";
            StringBuffer stringInputDocBuffer = new StringBuffer();
            while ((bytesRead = fileReader.read(readBuffer, 0, readBuffer.length - 1)) >= 0)
                stringInputDocBuffer.append(readBuffer, 0, bytesRead);
            xmlInputDoc = stringInputDocBuffer.toString();
            fileReader.close();

            // Get the file name for output mapping
            fileName = null;
            fileReader = null;
            for (int i = 0; i < 3; i++) {
                fileName = InputReader.getString("Filename containing XML output mapping: ", "");
                if (fileName == null) {
                    outputWriter.printString("Input file not found\n");
                    continue;
                }
                try {
                    fileReader = new FileReader(fileName);
                    break;
                } catch (FileNotFoundException e) {
                    outputWriter.printString("Input file not found\n");
                    fileName = null;
                }
            }

            if (fileName == null) {
                return;
            }

            String outputMapping = "";
            StringBuffer stringOutputMappingBuffer = new StringBuffer();

            while ((bytesRead = fileReader.read(readBuffer, 0, readBuffer.length - 1)) >= 0)
                stringOutputMappingBuffer.append(readBuffer, 0, bytesRead);

            outputMapping = stringOutputMappingBuffer.toString();
            fileReader.close();

            // Get the file name for option document

            fileName = null;
            fileReader = null;
            for (int i = 0; i < 3; i++) {
                fileName = InputReader.getString("Filename containing XML option document: ", "");
                if (fileName == null) {
                    outputWriter.printString("Input file not found\n");
                    continue;
                }
                try {
                    fileReader = new FileReader(fileName);
                    break;
                } catch (FileNotFoundException e) {
                    outputWriter.printString("Input file not found\n");
                    fileName = null;
                }
            }

            if (fileName == null) {
                return;
            }

            String optionDoc = "";
            StringBuffer stringOptionDocBuffer = new StringBuffer();

            while ((bytesRead = fileReader.read(readBuffer, 0, readBuffer.length - 1)) >= 0)
                stringOptionDocBuffer.append(readBuffer, 0, bytesRead);

            optionDoc = stringOptionDocBuffer.toString();

            String xmlOutputDoc = null;

            beginAPICall();
            xmlOutputDoc = getControlStructObject().xmlCreateEntry(xmlInputMapping, xmlInputDoc, outputMapping,
                    optionDoc);
            endAPICall(getControlStructObject().getLastStatus());

            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "XMLCreateEntry Results", statusList);

            if (xmlOutputDoc != null) {
                outputWriter.printString(xmlOutputDoc);
            } else {
                outputWriter.printString("xmlOutputDoc is null...\n");
            }
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.printString("Null Pointer exception...\n");
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        } finally {
            try {
                if (fileReader != null)
                    fileReader.close();
            } catch (IOException e) {
            }
        }
    }

    public void xmlGetEntry() {
        int bytesRead = 0;
        FileReader fileReader = null;
        try {
            outputWriter.driverPrintHeader("XML GET ENTRY");

            // Get the file name for xml query mapping
            String fileName = null;
            for (int i = 0; i < 3; i++) {
                fileName = InputReader.getString("Filename containing XML query mapping: ", "");
                if (fileName == null) {
                    outputWriter.printString("Input file not found\n");
                    continue;
                }
                try {
                    fileReader = new FileReader(fileName);
                    break;
                } catch (FileNotFoundException e) {
                    outputWriter.printString("Input file not found\n");
                    fileName = null;
                }
            }

            if (fileName == null) {
                return;
            }

            String xmlQueryMapping = "";
            StringBuffer stringQueryMappingBuffer = new StringBuffer();
            char readBuffer[] = new char[1024];
            while ((bytesRead = fileReader.read(readBuffer, 0, readBuffer.length - 1)) >= 0)
                stringQueryMappingBuffer.append(readBuffer, 0, bytesRead);

            xmlQueryMapping = stringQueryMappingBuffer.toString();
            fileReader.close();

            // Get the file name for query document
            fileName = null;
            fileReader = null;
            for (int i = 0; i < 3; i++) {
                fileName = InputReader.getString("Filename containing XML query document: ", "");
                if (fileName == null) {
                    outputWriter.printString("Input file not found\n");
                    continue;
                }
                try {
                    fileReader = new FileReader(fileName);
                    break;
                } catch (FileNotFoundException e) {
                    outputWriter.printString("Input file not found\n");
                    fileName = null;
                }
            }

            if (fileName == null) {
                return;
            }

            String xmlQueryDoc = "";
            StringBuffer stringQueryDocBuffer = new StringBuffer();
            while ((bytesRead = fileReader.read(readBuffer, 0, readBuffer.length - 1)) >= 0)
                stringQueryDocBuffer.append(readBuffer, 0, bytesRead);

            xmlQueryDoc = stringQueryDocBuffer.toString();
            fileReader.close();

            // Get the file name for output mapping
            fileName = null;
            fileReader = null;
            for (int i = 0; i < 3; i++) {
                fileName = InputReader.getString("Filename containing XML output mapping: ", "");
                if (fileName == null) {
                    outputWriter.printString("Input file not found\n");
                    continue;
                }
                try {
                    fileReader = new FileReader(fileName);
                    break;
                } catch (FileNotFoundException e) {
                    outputWriter.printString("Input file not found\n");
                    fileName = null;
                }
            }

            if (fileName == null) {
                return;
            }

            String outputMapping = "";
            StringBuffer stringOutputMappingBuffer = new StringBuffer();
            while ((bytesRead = fileReader.read(readBuffer, 0, readBuffer.length - 1)) >= 0)
                stringOutputMappingBuffer.append(readBuffer, 0, bytesRead);

            outputMapping = stringOutputMappingBuffer.toString();
            fileReader.close();

            // Get the file name for option document
            fileName = null;
            fileReader = null;
            for (int i = 0; i < 3; i++) {
                fileName = InputReader.getString("Filename containing XML option document: ", "");
                if (fileName == null) {
                    outputWriter.printString("Input file not found\n");
                    continue;
                }
                try {
                    fileReader = new FileReader(fileName);
                    break;
                } catch (FileNotFoundException e) {
                    outputWriter.printString("Input file not found\n");
                    fileName = null;
                }
            }

            if (fileName == null) {
                return;
            }

            String optionDoc = "";
            StringBuffer stringOptionDocBuffer = new StringBuffer();
            while ((bytesRead = fileReader.read(readBuffer, 0, readBuffer.length - 1)) >= 0)
                stringOptionDocBuffer.append(readBuffer, 0, bytesRead);

            optionDoc = stringOptionDocBuffer.toString();

            String xmlOutputDoc = null;

            beginAPICall();
            xmlOutputDoc = getControlStructObject().xmlGetEntry(xmlQueryMapping, xmlQueryDoc, outputMapping, optionDoc);
            endAPICall(getControlStructObject().getLastStatus());

            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "XMLGetEntry Results", statusList);

            if (xmlOutputDoc != null) {
                outputWriter.printString(xmlOutputDoc);
            } else {
                outputWriter.printString("xmlOutputDoc is null...\n");
            }
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.printString("Null Pointer exception...\n");
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        } finally {
            try {
                if (fileReader != null)
                    fileReader.close();
            } catch (IOException e) {
            }
        }
    }

    public void xmlSetEntry() {
        int bytesRead = 0;
        FileReader fileReader = null;
        try {
            outputWriter.driverPrintHeader("XML SET ENTRY");

            // Get the file name for xml query mapping
            String fileName = null;
            for (int i = 0; i < 3; i++) {
                fileName = InputReader.getString("Filename containing XML query mapping: ", "");
                if (fileName == null) {
                    outputWriter.printString("Input file not found\n");
                    continue;
                }
                try {
                    fileReader = new FileReader(fileName);
                    break;
                } catch (FileNotFoundException e) {
                    outputWriter.printString("Input file not found\n");
                    fileName = null;
                }
            }

            if (fileName == null) {
                return;
            }

            String xmlQueryMapping = "";
            StringBuffer stringQueryMappingBuffer = new StringBuffer();
            char readBuffer[] = new char[1024];
            while ((bytesRead = fileReader.read(readBuffer, 0, readBuffer.length - 1)) >= 0)
                stringQueryMappingBuffer.append(readBuffer, 0, bytesRead);

            xmlQueryMapping = stringQueryMappingBuffer.toString();
            fileReader.close();

            // Get the file name for query document
            fileName = null;
            fileReader = null;
            for (int i = 0; i < 3; i++) {
                fileName = InputReader.getString("Filename containing XML query document: ", "");
                if (fileName == null) {
                    outputWriter.printString("Input file not found\n");
                    continue;
                }
                try {
                    fileReader = new FileReader(fileName);
                    break;
                } catch (FileNotFoundException e) {
                    outputWriter.printString("Input file not found\n");
                    fileName = null;
                }
            }

            if (fileName == null) {
                return;
            }

            String xmlQueryDoc = "";
            StringBuffer stringQueryDocBuffer = new StringBuffer();
            while ((bytesRead = fileReader.read(readBuffer, 0, readBuffer.length - 1)) >= 0)
                stringQueryDocBuffer.append(readBuffer, 0, bytesRead);

            xmlQueryDoc = stringQueryDocBuffer.toString();
            fileReader.close();

            // Get the file name for xml input mapping
            fileName = null;
            fileReader = null;
            for (int i = 0; i < 3; i++) {
                fileName = InputReader.getString("Filename containing XML input mapping: ", "");
                if (fileName == null) {
                    outputWriter.printString("Input file not found\n");
                    continue;
                }
                try {
                    fileReader = new FileReader(fileName);
                    break;
                } catch (FileNotFoundException e) {
                    outputWriter.printString("Input file not found\n");
                    fileName = null;
                }
            }

            if (fileName == null) {
                return;
            }

            String xmlInputMapping = "";
            StringBuffer stringInputMappingBuffer = new StringBuffer();
            while ((bytesRead = fileReader.read(readBuffer, 0, readBuffer.length - 1)) >= 0)
                stringInputMappingBuffer.append(readBuffer, 0, bytesRead);

            xmlInputMapping = stringInputMappingBuffer.toString();
            fileReader.close();

            // Get the file name for input document
            fileName = null;
            fileReader = null;
            for (int i = 0; i < 3; i++) {
                fileName = InputReader.getString("Filename containing XML input document: ", "");
                if (fileName == null) {
                    outputWriter.printString("Input file not found\n");
                    continue;
                }
                try {
                    fileReader = new FileReader(fileName);
                    break;
                } catch (FileNotFoundException e) {
                    outputWriter.printString("Input file not found\n");
                    fileName = null;
                }
            }

            if (fileName == null) {
                return;
            }

            String xmlInputDoc = "";
            StringBuffer stringInputDocBuffer = new StringBuffer();
            while ((bytesRead = fileReader.read(readBuffer, 0, readBuffer.length - 1)) >= 0)
                stringInputDocBuffer.append(readBuffer, 0, bytesRead);

            xmlInputDoc = stringInputDocBuffer.toString();
            fileReader.close();

            // Get the file name for output mapping
            fileName = null;
            fileReader = null;
            for (int i = 0; i < 3; i++) {
                fileName = InputReader.getString("Filename containing XML output mapping: ", "");
                if (fileName == null) {
                    outputWriter.printString("Input file not found\n");
                    continue;
                }
                try {
                    fileReader = new FileReader(fileName);
                    break;
                } catch (FileNotFoundException e) {
                    outputWriter.printString("Input file not found\n");
                    fileName = null;
                }
            }

            if (fileName == null) {
                return;
            }

            String outputMapping = "";
            StringBuffer stringOutputMappingBuffer = new StringBuffer();
            while ((bytesRead = fileReader.read(readBuffer, 0, readBuffer.length - 1)) >= 0)
                stringOutputMappingBuffer.append(readBuffer, 0, bytesRead);

            outputMapping = stringOutputMappingBuffer.toString();
            fileReader.close();

            // Get the file name for option document
            fileName = null;
            fileReader = null;
            for (int i = 0; i < 3; i++) {
                fileName = InputReader.getString("Filename containing XML option document: ", "");
                if (fileName == null) {
                    outputWriter.printString("Input file not found\n");
                    continue;
                }
                try {
                    fileReader = new FileReader(fileName);
                    break;
                } catch (FileNotFoundException e) {
                    outputWriter.printString("Input file not found\n");
                    fileName = null;
                }
            }

            if (fileName == null) {
                return;
            }

            String optionDoc = "";
            StringBuffer stringOptionDocBuffer = new StringBuffer();
            while ((bytesRead = fileReader.read(readBuffer, 0, readBuffer.length - 1)) >= 0)
                stringOptionDocBuffer.append(readBuffer, 0, bytesRead);

            optionDoc = stringOptionDocBuffer.toString();

            beginAPICall();
            getControlStructObject().xmlSetEntry(xmlQueryMapping, xmlQueryDoc, xmlInputMapping, xmlInputDoc,
                    outputMapping, optionDoc);
            endAPICall(getControlStructObject().getLastStatus());

            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "XMLSetEntry Results", statusList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.printString("Null Pointer exception...\n");
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        } finally {
            try {
                if (fileReader != null)
                    fileReader.close();
            } catch (IOException e) {
            }
        }
    }

    void getMultipleCurrencyRatioSets() {
        try {
            outputWriter.driverPrintHeader("GET MULTIPLE CURRENCY RATIO SETS");

            List<Timestamp> timestampList = InputReader.getTimestampList();

            beginAPICall();
            List<String> currencyRatioSets = getControlStructObject().getMultipleCurrencyRatioSets(timestampList);
            endAPICall(getControlStructObject().getLastStatus());

            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "GetMultipleCurrencyRatioSets Results", statusList);

            for (int i = 0; i < currencyRatioSets.size(); i++) {
                outputWriter.printString(currencyRatioSets.get(i) + "\n");
            }
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getCurrencyRatio() {
        try {
            outputWriter.driverPrintHeader("GET CURRENCY RATIO");

            String currencyRatioString = InputReader.getString("Currency ratios string:", "");
            String fromCurrencyCode = InputReader.getString("From Currency Code:", "");
            System.out.println(fromCurrencyCode);
            String toCurrencyCode = InputReader.getString("To Currency Code:", "");
            System.out.println(toCurrencyCode);

            beginAPICall();
            BigDecimal currencyRatio = getControlStructObject().getCurrencyRatio(currencyRatioString.trim(),
                    fromCurrencyCode.trim(), toCurrencyCode.trim());
            endAPICall(getControlStructObject().getLastStatus());

            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "GetCurrencyRadio Results", statusList);

            outputWriter.printString("Currency Ratio: " + currencyRatio.toString());
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getQualifier() {
        try {

            String formName = InputReader.getString("Local Form Name: ");
            boolean x = InputReader.getNullPromptOption();
            InputReader.setNullPromptOption(true);
            String remoteformName = InputReader.getString("Remote Form Name: ");
            InputReader.setNullPromptOption(x);

            FieldCriteria crit = new FieldCriteria();
            crit.setRetrieveAll(true);

            List<Field> fieldList = getControlStructObject().getListFieldObjects(formName, FieldType.AR_ALL_FIELD,
                                                                                 0, crit);
            List<Field> remoteFieldList = null;

            if (remoteformName != null) {
                remoteFieldList = getControlStructObject().getListFieldObjects(remoteformName,
                                                                               FieldType.AR_ALL_FIELD, 0, crit);
            }

            int vuiId = InputReader.getInt("Enter ViewID (0):", 0);
            int opt = InputReader.getInt("Customized? (0):", 0);
            String szQual = InputReader.getString("Qualifier String: ", "");

            QualifierInfo qual;

            if (opt == 0) {
                if (remoteFieldList == null) {
                    qual = getControlStructObject().parseQualification(szQual, fieldList, null, 0);
                } else {
                    qual = getControlStructObject().parseQualification(szQual, remoteFieldList, fieldList, 0);
                }
            } else {
                ARQualifierHelper qualHelper = new ARQualifierHelper();
                qualHelper.generateFieldMaps(fieldList, vuiId, null, remoteFieldList);
                qual = qualHelper.parseQualification(getControlStructObject().getLocale(), szQual);
            }

            outputWriter.printQualifierInfo("", "", qual);

            String szQualNew = getControlStructObject().formatQualification(qual, fieldList, remoteFieldList, 0, false);
            System.out.println("\nOriginal Query : " + szQual);
            System.out.println("\nRebuilt Query : " + szQualNew);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            outputWriter.printARException(e);
        }
    }

    void setImpersonatedUser() {
        try {
            outputWriter.driverPrintHeader("SET IMPERSONATED USER");
            String szUser = InputReader.getString("User To Impersonate: ", "");
            beginAPICall();
            getControlStructObject().impersonateUser(szUser);
            endAPICall(getControlStructObject().getLastStatus());
            System.out.println("\nCall Returned OK");
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void dumpProxyInfo() {

        ProxyManager.PoolInfo ppi[] = ProxyManager.getPoolInformation();

        outputWriter.printString("Pooling Info:\n");
        for (int i = 0; i < ppi.length; i++) {
            StringBuffer sb = new StringBuffer();
            sb.append(ppi[i].getPoolServerName()).append(" * ").append(new Integer(ppi[i].getPoolInUse())).append(" * ")
                    .append(new Integer(ppi[i].getPoolFree())).append(" * ").append(new Integer(ppi[i].getPoolNotCreated()));
            outputWriter.printString(sb.toString());
            outputWriter.printString("\n");
        }
    }

    void setUseConnectionPooling() {
        boolean orgFlag = ProxyManager.isUseConnectionPooling();
        boolean poolFlag = orgFlag;
        try {
            outputWriter.driverPrintHeader("SET CONNECTION POOLING");
            poolFlag = InputReader.getBooleanForChangingInfo("Use Connection Pooling (false-true) ("+orgFlag+"): ", orgFlag);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (poolFlag != orgFlag)
            ProxyManager.setUseConnectionPooling(poolFlag);
    }

    void setConnectionLimitPerServer() throws IOException {
        outputWriter.driverPrintHeader("SET CONNECTION LIMITS PER SERVER");
        int currentLimits = InputReader.getInt("Maximum pooled proxy connections per server :", maxConnectionPerServer);
        ProxyManager.setConnectionLimits(currentLimits);
    }

    void adjustConnectionParametersPerServer() throws IOException {
        outputWriter.driverPrintHeader("ADJUST CONNECTION PARAMETERS PER SERVER");
        int maxProxies = InputReader.getInt("Maximum pooled proxy connections per server :", maxConnectionPerServer);
        int minProxies = InputReader.getInt("Idle Connection per server :", idleConnectionsPerServer);
        String unitStr = InputReader.getString("Time unit for connection life span and timeout (DAYS, HOURS, MINUTES, SECONDS) ("+connectionTimeUnit+"):", connectionTimeUnit);
        ARTimeUnit timeUnit = ARTimeUnit.convert(unitStr);
        long connectionTimeout = InputReader.getLong("Connection timeout ("+connectionTimeoutDefault+"):", connectionTimeoutDefault);
        long connectionLifespan = InputReader.getLong("Connection life span ("+connectionLifespanDefault+"):", connectionLifespanDefault);
        ProxyManager.PoolInfo poolSetting = new ProxyManager.PoolInfo(maxProxies, minProxies, connectionTimeout, timeUnit);
        ProxyManager.adjustConnectionPoolVariables(poolSetting);
        ProxyManager.setConnectionLifespan(connectionLifespan, timeUnit);
    }

    void getServerCharSet() {
        try {

            outputWriter.driverPrintHeader("GET SERVER CHARSET");

            beginAPICall();
            String charSet = getControlStructObject().getServerCharSet();
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printString("", "char-set: ", charSet, "\n");

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARGetServerCharSet Status", statusList);

        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void beginApiRecording() {
        try {

            outputWriter.driverPrintHeader("BEGIN API RECORDING");
            int recordingMode = InputReader.getInt("Recording Mode (0,1,2,4,8,16,32,64)(0):", 0);
            String fileName = InputReader.getString("Command and Result Filename prefix:", "");
            List<String> fileList = getControlStructObject().startRecording(recordingMode, fileName);
            if (fileList.size() != 0)
                outputWriter.print("", "Api Recording files created", fileList);
            else
                outputWriter.printString("Problem in creating the Api Recording files\n");

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        }
    }

    void stopApiRecording() {
        outputWriter.driverPrintHeader("STOP API RECORDING");
        getControlStructObject().stopRecording();
    }

    void termination() {
        getControlStructObject().logout();
    }

    void arImport() {
        try {
            outputWriter.driverPrintHeader("IMPORT");

            List<StructItemInfo> itemsList = InputReader.getStructItemInfoList();

            String fileName = InputReader.getString("Filename containing import data:", "");

            int importOption = InputReader.getInt("The import option (0):", 0);

            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();

            beginAPICall();
            getControlStructObject().importDefFromFile(fileName, importOption, itemsList, omLabelAndTaskName[0]); //, omLabelAndTaskName[1]);
            endAPICall(getControlStructObject().getLastStatus());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARImport ", statusList);
        } catch (IOException e) {
            outputWriter.printString(e.getLocalizedMessage());
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    void getMultipleEntryPoints() {
        try {
            outputWriter.driverPrintHeader("GET MULTIPLE ENTRYPOINTS");

            long changedSince = InputReader.getLong("Get All changed since, as UNIX epoch (0): ", 0);
            List<String> applicationKeys = InputReader.getStringList("", "Application name");
            int[] refTypes = InputReader.getIntArray();
            String displayTag = InputReader.getString("Display Tag ():", "");
            int vuiType = InputReader.getInt("VUI Type (1-4) (0): ", 0);
            boolean hiddenFlag = InputReader.getBooleanForChangingInfo("Include hidden (false-true) (true): ", true);

            beginAPICall();
            List<EntryPointInfo> entryPointInfoList = getControlStructObject().getListEntryPoint(changedSince,
                    applicationKeys, refTypes, vuiType, displayTag, hiddenFlag);

            endAPICall(getControlStructObject().getLastStatus());

            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "ARGetMultipleEntryPoints ", statusList);

            // Now print all the information about the containers
            outputWriter.print("", "Entry Points List:", entryPointInfoList);

        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.printString("Null Pointer exception...\n");
        } catch (ARException e) {
            outputWriter.printARException(e);
        }
    }

    void beginBulkEntryTransaction() {
        try {
            outputWriter.driverPrintHeader("BEGIN BULK ENTRY TRANSACTION");
            beginAPICall();
            getControlStructObject().beginBulkEntryTransaction();
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printStatusInfoList("", "BeginBulkEntryTransaction Results", getControlStructObject()
                    .getLastStatus());
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }
 
    

    void endBulkEntryTransaction() {
        List<BulkEntryReturn> bulkEntryReturnList = null;

        try {
            outputWriter.driverPrintHeader("END BULK ENTRY TRANSACTION");

            int actionType = InputReader.getInt("Action Type (1, 2): ", 1);

            beginAPICall();
            bulkEntryReturnList = getControlStructObject().endBulkEntryTransaction(actionType);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printBulkEntryReturnList("   ", "Bulk Entry Return", bulkEntryReturnList);

            outputWriter.printStatusInfoList("", "EndBulkEntryTransaction Results", getControlStructObject()
                    .getLastStatus());
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            if (e.getClass() == ARBulkException.class) {
                bulkEntryReturnList = ((ARBulkException) e).getBulkEntryReturn();
                outputWriter.printBulkEntryReturnList("   ", "Bulk Entry Return", bulkEntryReturnList);
            }
            outputWriter.printARException(e);
        }
    }

    void beginClientManagedTransaction() {
    	String transactionHandle = null;
        try {
            outputWriter.driverPrintHeader("BEGIN CLIENT MANAGED TRANSACTION");
            beginAPICall();
            transactionHandle = getControlStructObject().beginClientManagedTransaction();
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printString(transactionHandle);
            outputWriter.printStatusInfoList("", "BeginClientManagedTransaction results", getControlStructObject()
                    .getLastStatus());
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }
    void endClientManagedTransaction() {
 
        try {
           	int transactionOption  = InputReader.getInt("Transaction Type (1 - Commit, 2 - Rollback): ", 1);        	
            outputWriter.driverPrintHeader("END CLIENT MANAGED TRANSACTION");
            beginAPICall();
            getControlStructObject().endClientManagedTransaction(transactionOption);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printStatusInfoList("", "EndClientManagedTransaction results", getControlStructObject()
                    .getLastStatus());
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        }
        catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
         
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }    
    void setClientManagedTransaction() {
    	String transactionHandle = null;
    	 
        try {
           	transactionHandle  = InputReader.getString("Transaction handle: ", transactionHandle);        	
            outputWriter.driverPrintHeader("SET CLIENT MANAGED TRANSACTION");
            beginAPICall();
            getControlStructObject().setClientManagedTransaction(transactionHandle);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printStatusInfoList("", "SetClientManagedTransaction results", getControlStructObject()
                    .getLastStatus());
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }

    }        
    void removeClientManagedTransaction() {
 
        try {
   
            outputWriter.driverPrintHeader("REMOVE CLIENT MANAGED TRANSACTION");
            beginAPICall();
            getControlStructObject().removeClientManagedTransaction();
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printStatusInfoList("", "RemoveClientManagedTransaction results", getControlStructObject()
                    .getLastStatus());
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }

    }         
    void wfdExecute() {
        try {
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

   void wfdGetDebugLocation() {
        WfdDebugLocation currentLocation = null;
        try {
            outputWriter.driverPrintHeader("WFD GET CURRENT LOCATION");

            // Get the stack depth requested
            int depth = InputReader.getInt("Stack frame ? (0): ", 0);

            // Call ARWfdGetCurrentLocation method
            beginAPICall();
            currentLocation = getControlStructObject().wfdGetDebugLocation(depth);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printCurrentLocation("Current Location:\n", currentLocation);

            // Success. Print the status
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

    void wfdGetDebugMode() {
        int     mode;
        try {
            outputWriter.driverPrintHeader("WFD GET DEBUG MODE");

            // Call ARWfdGetDebugMode method
            beginAPICall();
            mode = getControlStructObject().wfdGetDebugMode();
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printInteger( "", "Debug mode is:\n", mode );

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

    void wfdSetDebugMode() {
        try {
            outputWriter.driverPrintHeader("WFD SET DEBUG MODE");

            // Get the stack depth requested
            int mode = InputReader.getInt("Debug mode ? (0): ", 0);

            // Call ARWfdSetDebugMode method
            beginAPICall();
            getControlStructObject().wfdSetDebugMode(mode);
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
    
    void wfdGetFieldValues() {
        try {
            outputWriter.driverPrintHeader("WFD GET FIELD VALUES");

            // Get the stack depth requested
            int depth = InputReader.getInt("Field from stack frame ? (0): ", 0);

            // Call ARWfdGetFieldValues method
            beginAPICall();
            List<Entry> fieldValues = getControlStructObject().wfdGetFieldValues(depth);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printEntry( "", "Transaction Fields - ", fieldValues.get(0));
            outputWriter.printEntry( "", "Database Fields - ", fieldValues.get(1));
            
            // Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Wfd Get Debug Mode Status", statusList);
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
            outputWriter.driverPrintHeader("WFD GET FILTER QUALIFIER");

            // Call ARWfdGetFilterQual method
            beginAPICall();
            QualifierInfo qualifier = getControlStructObject().wfdGetFilterQual();
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.printQualifierInfo( "", "Filter Qualifier:\n", qualifier);
            
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
            outputWriter.driverPrintHeader("WFD Set Qualifier Result");

            boolean result = InputReader.getBooleanForChangingInfo("Set Qualfier Result, T/F? (T): ", true);
                        
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
    
    void runEscalation() {
        try {
            outputWriter.driverPrintHeader("WFD Run Escalation");
            
            // Get the errorCode to return
            String escalation = InputReader.getString("Escalation to run: ");
            
            // Call ARWfdTerminateAPI method
            beginAPICall();
            getControlStructObject().runEscalation(escalation);
            endAPICall(getControlStructObject().getLastStatus());

            // Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Wfd Run Escalation Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }
    
    void getvalidateFormCache() {
        try {
            outputWriter.driverPrintHeader("VALIDATE FORM CACHE");

            // Create Form Name
            String formNm = InputReader.getString("Form name:");
            Timestamp ts = new Timestamp(0);
            Timestamp tsAL  = InputReader.getTimestamp("Get active links changed since (0): ",ts);
            Timestamp tsMenu  = InputReader.getTimestamp("Get menus changed since (0):",ts);
            Timestamp tsGuide  = InputReader.getTimestamp("Get guides changed since (0):",ts);            
            beginAPICall();
            ValidateFormCacheInfo fci = getControlStructObject().getValidateFormCache(formNm, tsAL, tsMenu, tsGuide);
            endAPICall(getControlStructObject().getLastStatus());
            // print the esclation data
            outputWriter.printValidateFormCache("", " ARValidateFormCache  results", fci);
            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "VALIDATE FORM CACHE Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    protected void wfdRmtSetBreakPt() {
        try {
            outputWriter.driverPrintHeader("WFD Set Remote Breakpoint");
            WfdBreakpoint	bp;

            // Get remainder of the bp info
            bp = InputReader.WFDFillBreakPt( false );

            if( (bp.filter == "") || (bp.schema == "") ||
                (bp.stage < Constants.WFD_BEFORE_API) || (bp.stage > Constants.WFD_BEFORE_CMDB) ) {
            	outputWriter.printString("Invalid Breakpoint definition. Breakpoint not set.\n");
            }
            else {
            	// Call ARWfdSetQualifierResult method
            	beginAPICall();
            	getControlStructObject().wfdSetBreakpoint(bp.id, bp);
            	endAPICall(getControlStructObject().getLastStatus());
            }
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

    protected void wfdRmtClrBreakPt() {
        try {
            outputWriter.driverPrintHeader( "WFD Set Remote Breakpoint" );
            
            // Get bp ID to clear
            int bpId = InputReader.getInt( "Breakpoint to clear (0): ", 0 );


            // Call ARWfdSetQualifierResult method
            beginAPICall();
            getControlStructObject().wfdClearBreakpoint( bpId );
            endAPICall(getControlStructObject().getLastStatus());

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

    protected void wfdRmtListBreakPt() {
        try {
            outputWriter.driverPrintHeader("WFD List Remote Breakpoint");
            List<WfdBreakpoint>	bps;

            // Call ARWfdSetQualifierResult method
            beginAPICall();
            bps = getControlStructObject().wfdListBreakpoints( );
            endAPICall(getControlStructObject().getLastStatus());

            for(WfdBreakpoint bp : bps ) {
            	outputWriter.printBreakpoint( "", bp, false );
            }
            // Print the status
            //List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            //outputWriter.printStatusInfoList("", "Wfd Get Keyword Value Status", statusList);
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    protected void wfdRmtClearBpList() {
        try {
            outputWriter.driverPrintHeader("WFD Clear All Remote Breakpoints");

            // No params or return info
            beginAPICall();
            getControlStructObject().wfdClearAllBreakpoints( );
            endAPICall(getControlStructObject().getLastStatus());

            // Print the status
            //List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            //outputWriter.printStatusInfoList("", "Wfd Get Keyword Value Status", statusList);
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }

    public void launchThread(boolean flag) {

        ThreadStartInfo threadStartInfo = new ThreadStartInfo();

        try {
            ThreadControlBlock threadControlBlockPtr = getThreadControlBlockPtr();
            String msg = flag ? "LAUNCH WAITING THREAD" : "LAUNCH THREAD";
            outputWriter.driverPrintHeader(msg);
            String inputValue = InputReader.getString("Filename of input file (): ", "");
            if ((inputValue == null) || inputValue.length() == 0) {
                outputWriter.driverPrintError(" **** An input file is required to launch a thread\n");
                return;
            }
            threadStartInfo.setInputFile(new InputFile(inputValue));

            /* get the filename of the optional output file */

            String outputValue = InputReader.getString("Filename of output file (): ", "");
            boolean isStdout = false;
            if ((outputValue == null) || ((quietMode & SUPPRESS_RESULTS) > 0)) {
                isStdout = true;
            } else {
                if (outputValue.length() == 0) {
                    isStdout = true;
                }
            }

            if (isStdout) {
                threadStartInfo.setOutputToStdOut();
            } else {
                /* build a file name */
                String fileName = null;
                if ((getResultDirectory() == null) || (getResultDirectory().length() == 0)) {
                    fileName = outputValue;
                } else {
                    fileName = getResultDirectory() + "\\" + outputValue;
                }

                /* open the output file for writing */
                threadStartInfo.setOutputFile(new PrintWriter(new FileOutputStream(new File(fileName))));
                threadStartInfo.setOutputFileName(fileName);
            }

            /* get the optional authentication string that the launched thread will login with */
            String authString = InputReader.getString("Authentication string (): ", "");
            threadStartInfo.setAuthentication(authString);

            /* get the optional user name that the launched thread will login with */
            String userName = InputReader.getString("User name (): ", "");
            threadStartInfo.setUser(userName);

            /* get the optional password that the launched thread will login with */
            String password = InputReader.getString("Password (): ", "");
            threadStartInfo.setPassword(password);

            /* get the optional locale that the launched thread will login with */
            String locale = InputReader.getString("Locale[.charSet] (): ", "");
            LocaleCharSet localeCharSet = new LocaleCharSet(locale);
            threadStartInfo.setLocale(localeCharSet.getLocale());
            threadStartInfo.setCharset(localeCharSet.getCharSet());
            String timeZone = InputReader.getString("TimeZone () : ", "");
            threadStartInfo.setTimezone(timeZone);
            /* get the optional server that the launched thread will login with */
            String server = InputReader.getString("Server (): ", "");
            threadStartInfo.setServer(server);

            /* get the optional upper range value for a delay at the start of the */
            /* launched thread                                                    */
            long upperBound = InputReader.getLong("Thread startup sleep range (0): ", 0);
            threadStartInfo.setUpperBound(upperBound);

            // Set the wait information depending on the wait flag
            threadStartInfo.setWaitFlag(flag);

            if (flag) {
                threadStartInfo.setWaitObject(threadControlBlockPtr.getWaitObject());
                threadStartInfo.setReleaseObject(threadControlBlockPtr.getReleaseObject());
            }

            // Create another JavaDriver thread
            JavaDriver driver = instantiateJavaDriver();
            driver.setThreadStartInfo(threadStartInfo);
            driver.setPrimaryThread(false);

            // Now start the thread
            driver.start();

            // Add this thread to the children list
            threadControlBlockPtr.addThreadHandle(driver);

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

        } catch (IOException e) {
            try {
                threadStartInfo.cleanUp();
                outputWriter.printString("Problem in getting the input...Driver problem..\n");
            } catch (IOException ex) {
                outputWriter.printString("Problem in cleaning up the thread start info..\n");
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

    protected JavaDriver instantiateJavaDriver() {
        return new JavaDriver();
    }

    public void threadStartFunction() throws IOException {
        ThreadControlBlock threadControlBlockObject = getThreadControlBlockPtr();

        threadControlBlockObject.setCurrentInputFile(threadStartInfoObject.getInputFile());

        threadControlBlockObject.setOutputFile(threadStartInfoObject.getOutputFile(), threadStartInfoObject
                .getOutputFileName(), threadStartInfoObject.getIsStdOut());
        threadControlBlockObject.setAuthentication(threadStartInfoObject.getAuthentication());
        threadControlBlockObject.setUser(threadStartInfoObject.getUser());
        threadControlBlockObject.setPassword(threadStartInfoObject.getPassword());
        threadControlBlockObject.setLocale(threadStartInfoObject.getLocale());
        threadControlBlockObject.setTimeZone(threadStartInfoObject.getTimezone());
        threadControlBlockObject.setServer(threadStartInfoObject.getServer());
        threadControlBlockObject.setPrimaryThread(false);

        if (threadStartInfoObject.getWaitFlag()) {
            SyncObject waitObject = threadStartInfoObject.getWaitObject();
            synchronized (waitObject) {
                waitObject.flag = true;
                waitObject.notify();
            }
            SyncObject releaseObject = threadStartInfoObject.getReleaseObject();
            synchronized (releaseObject) {
                try {
                    while (releaseObject.getFlag() == false) {
                        releaseObject.wait();
                    }
                } catch (InterruptedException e) {
                    releaseObject.setFlag(true);
                    System.out.println("Interrupted Exception ...in simplethread run");
                }
            }
        }

        /* if requested we delay the processing of commands by a random length */
        /* sleep that is between zero and the provided number of seconds       */

        if (threadStartInfoObject.getUpperBound() > 0) {
            threadControlBlockObject.setCurrentCommand("rst");
            beginAPICall();
            timerCommandProcessor.randomSleep(0, threadStartInfoObject.getUpperBound());
            endAPICall(getControlStructObject().getLastStatus());
        }

        // Cleanup the threadstartup information
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
            if (this.isPrimaryThread()) {
                threadControlBlockPtr.setPrimaryThread(true);
                threadControlBlockPtr.setCurrentInputToStdIn();
                threadControlBlockPtr.setOutputToStdOut();
                if (!processCommandLine()) {
                    System.exit(1);
                }
                outputWriter.driverPrintHelp();
                initCommandProcessing();
            } else {
                threadStartFunction();
            }

            processCommands();
        } catch (IOException e) {
            e.printStackTrace();
            outputWriter.printString("IOException caught\n");
        } catch (Exception e){
            e.printStackTrace();
            outputWriter.printString("Exception caught"+e.getLocalizedMessage()+"\n");        	
            System.out.println(e.getLocalizedMessage());
            unexpectedError = true;
        } finally {
            try {
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

    protected ThreadControlBlock initThreadControlBlockPtr() {
        localStorage = new ThreadControlLocalStorage();
        return (ThreadControlBlock) localStorage.get();
    }

    public static void main(String[] args) {
        try {
            ProxyManager.setUseConnectionPooling(false);
            JavaDriver driver = new JavaDriver();
            driver.setCommandLineArgs(args);
            driver.setPrimaryThread(true);
            driver.start();
        } catch (Exception e) {
            outputWriter.printString("Error in executing the command\n");
        }
    }

    void setGetEntry() {
        try {

            outputWriter.driverPrintHeader("SET GET ENTRY");
            // Get the Entry Key
            EntryKey entryKey = InputReader.getEntryKey();

            // Get the fields information
            int[] entryListFields = InputReader.getIntArray();
            
            // Get the entry items information
            outputWriter.driverPrintPrompt("Field/value pairs to set:\n");
            Entry entry = InputReader.getEntry();

            // Get the time stamp and entry option
            Timestamp getTime = new Timestamp(InputReader.getLong("Time of Get operation (0): ", 0));
            int option = InputReader.getInt("SetEntry option ? (0): ", 0);

            beginAPICall();
            Entry result = getControlStructObject().setGetEntry(entryKey.getFormName(), entryKey.getEntryID(), entry, getTime, option, entryListFields); 
            endAPICall(getControlStructObject().getLastStatus());

            // If there were diary field values, we call diary.decode()
            // which calls ARDecodeDiary. This wipes out the previous
            // status. So we merge the two status together if needed
            outputWriter.printEntry("", "Entry Information:", result);

            // Success. Print the status
        } catch (IOException x) {
            outputWriter.printString("Problem in getting the input for set entry...\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARSetGetEntryException e){
        	endAPICall(getControlStructObject().getLastStatus());
        	outputWriter.printARSetGetStatusException(e);
        } catch (ARException e){
        	endAPICall(getControlStructObject().getLastStatus());
        }
    }
    
    void createOverlay() {

        try {
            outputWriter.driverPrintHeader("CREATE OVERLAY");

            // Create a new overlaid info instance
            OverlaidInfo object = new OverlaidInfo();

            // Get and Set the type of object which is to be overlaid 
            int objType = InputReader.getInt("Object Type: form(1), filter(5), activelink(6), menu(8), \n"
                    + "escalation(9), container(12), view(14), field(15), image(17): ", 0);
            object.setObjType(objType);

            if (objType == Constants.AR_STRUCT_ITEM_FIELD || objType ==  Constants.AR_STRUCT_ITEM_VUI)
            {
	            // Get and Set schema information (applicable for fields and views)
	            String formName = InputReader.getString("Form Name (for field/view objects): ");
	            object.setFormName(formName);
	            
	            // Get and Set the id of object which is to be overlaid 
	            int inId = InputReader.getInt("Object Id: ", 0);
	            object.setId(inId);
            }

            else
            {            
            	object.setFormName("");
            	object.setId(0);
            	
                // Get and Set the name of object which is to be overlaid
                String name = InputReader.getString("Object Name: ");
                object.setName(name);
            }
            
            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();
            object.setObjectModificationLogLabel(omLabelAndTaskName[0]);

            // Now create the filter in the database
            beginAPICall();
            OverlayPropInfo overlayProps = getControlStructObject().createOverlay(object);
            endAPICall(getControlStructObject().getLastStatus());
            
            // Print the information about the overlay object created
            outputWriter.printOverlayPropInfo("", "Overlay Object Information: ", overlayProps);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Create Overlay Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }
    
    void createOverlayFromObject() {

        try {
        	String name;
        	int objType;
        	
            outputWriter.driverPrintHeader("CREATE OVERLAY FROM OBJECT");
            OverlaidInfo baseObject = new OverlaidInfo();
            OverlaidInfo customObject = new OverlaidInfo();
            
            int operationType = InputReader.getInt("Type of operation - (Convert object to custom - 1, \n" +
            		"				Convert custom object to overlay - 2, \n" +
            		"				Convert custom object to base - 3) (1) :", 1);
            
            switch(operationType)
            {
            case Constants.AR_OVERLAY_CONVERT_TO_CUSTOM:
            	// Get and Set the name of object which is to be overlaid
                name = InputReader.getString("Base Object Name: ");
                customObject.setName(name);

                // Get and Set the type of object which is to be overlaid 
                objType = InputReader.getInt("Base Object Type: form(1), filter(5), activelink(6), menu(8), \n"
                        + "escalation(9), container(12), view(14), field(15), image(17): ", 0);
                customObject.setObjType(objType);

                if (objType == Constants.AR_STRUCT_ITEM_FIELD || objType ==  Constants.AR_STRUCT_ITEM_VUI)
                {
    	            // Get and Set schema information (applicable for fields and views)
    	            String formName = InputReader.getString("Form Name (for field/view objects): ");
    	            customObject.setFormName(formName);
    	            
    	            // Get and Set the id of object which is to be overlaid 
    	            int inId = InputReader.getInt("Base Object Id: ", 0);
    	            customObject.setId(inId);
                }
                else
                {            
                	customObject.setFormName("");
                	customObject.setId(0);
                }
            	break;
            case Constants.AR_OVERLAY_CONVERT_TO_OVERLAY:
            	name = InputReader.getString("Base Object Name: ");
            	baseObject.setName(name);

                // Get and Set the type of object which is to be overlaid 
                objType = InputReader.getInt("Base Object Type: form(1), filter(5), activelink(6), menu(8), \n"
                        + "escalation(9), container(12), view(14), field(15), image(17): ", 0);
                baseObject.setObjType(objType);

                if (objType == Constants.AR_STRUCT_ITEM_FIELD || objType ==  Constants.AR_STRUCT_ITEM_VUI)
                {
    	            // Get and Set schema information (applicable for fields and views)
    	            String formName = InputReader.getString("Form Name (for field/view objects): ");
    	            baseObject.setFormName(formName);
    	            
    	            // Get and Set the id of object which is to be overlaid 
    	            int inId = InputReader.getInt("Base Object Id: ", 0);
    	            baseObject.setId(inId);
                }
                else
                {            
                	baseObject.setFormName("");
                	baseObject.setId(0);
                }
            
            // Get and Set the name of object which is to be overlaid
                name = InputReader.getString("Custom Object Name: ");
            customObject.setName(name);

            // Get and Set the type of object which is to be overlaid 
                objType = InputReader.getInt("Custom Object Type: form(1), filter(5), activelink(6), menu(8), \n"
                        + "escalation(9), container(12), view(14), field(15), image(17): ", 0);
            customObject.setObjType(objType);

            if (objType == Constants.AR_STRUCT_ITEM_FIELD || objType ==  Constants.AR_STRUCT_ITEM_VUI)
            {
	            // Get and Set schema information (applicable for fields and views)
	            String formName = InputReader.getString("Form Name (for field/view objects): ");
	            customObject.setFormName(formName);
	            
	            // Get and Set the id of object which is to be overlaid 
	            int inId = InputReader.getInt("Custom Object Id: ", 0);
	            customObject.setId(inId);
            }
            else
            {            
            	customObject.setFormName("");
            	customObject.setId(0);
            }
            	break;
            case Constants.AR_OVERLAY_CONVERT_TO_BASE:
            	/* As of now, we are using baseObj to take inputs for custom object. This is   
                * purposely to differentiate this case with case 1 where base is NULL as param 
                * API checks if base param is null, then convert custom to overlay and if      
                * custom is NULL, convert custom to base.                                      
                * Get name of base object */
            	name = InputReader.getString("Custom Object Name: ");
            	baseObject.setName(name);

                // Get and Set the type of object which is to be overlaid 
                objType = InputReader.getInt("Custom Object Type: form(1), filter(5), activelink(6), menu(8), \n"
                        + "escalation(9), container(12), view(14), field(15), image(17): ", 0);
                baseObject.setObjType(objType);

                if (objType == Constants.AR_STRUCT_ITEM_FIELD || objType ==  Constants.AR_STRUCT_ITEM_VUI)
                {
    	            // Get and Set schema information (applicable for fields and views)
    	            String formName = InputReader.getString("Form Name (for field/view objects): ");
    	            baseObject.setFormName(formName);
            
    	            // Get and Set the id of object which is to be overlaid 
    	            int inId = InputReader.getInt("Base Object Id: ", 0);
    	            baseObject.setId(inId);
                }
                else
                {            
                	baseObject.setFormName("");
                	baseObject.setId(0);
                }
            	break;
            default:
                outputWriter.printString("Wrong option specifid = " + operationType);
                break;
            }
                       
            if (operationType == Constants.AR_OVERLAY_CONVERT_TO_CUSTOM ||
            		operationType == Constants.AR_OVERLAY_CONVERT_TO_OVERLAY ||
            		operationType == Constants.AR_OVERLAY_CONVERT_TO_BASE)
            {
            String[] omLabelAndTaskName = InputReader.getObjectModificationLogLabelAndTaskName();
            customObject.setObjectModificationLogLabel(omLabelAndTaskName[0]);

            // Now create the filter in the database
            beginAPICall();
            OverlayPropInfo overlayProps = getControlStructObject().createOverlayFromObject(baseObject, customObject);
            endAPICall(getControlStructObject().getLastStatus());
            
            // Print the information about the overlay object created
            outputWriter.printOverlayPropInfo("", "Overlay Object Information: ", overlayProps);

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "Create Overlay Status", statusList);
            }
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }
    
    void getMultipleVUI(){
        try {
            long changedSince = InputReader.getLong("Get All changed since, as UNIX epoch (0): ", 0);
            
            String formName = InputReader.getString("Form Name:", "");
            
            outputWriter.driverPrintPrompt("Ids of Views:\n");
            int[] viewIds = InputReader.getInternalIDList();

            // Create the View Criteria and the flag to retrieve all the properties
            ViewCriteria criteria = new ViewCriteria();
            criteria.setRetrieveAll(true);

            beginAPICall();
            List<View> viewListObjects = getControlStructObject().getListViewObjects(formName, changedSince, viewIds, criteria);
            endAPICall(getControlStructObject().getLastStatus());

            outputWriter.print("", "View List Objects: ", viewListObjects.toString());

            // Success. Print the status
            List<StatusInfo> statusList = getControlStructObject().getLastStatus();
            outputWriter.printStatusInfoList("", "GetListViewObjects Status", statusList);
        } catch (IOException e) {
            outputWriter.printString("Problem in getting the input...Driver problem..\n");
        } catch (NullPointerException e) {
            outputWriter.driverPrintException(e);
        } catch (ARException e) {
            endAPICall(getControlStructObject().getLastStatus());
            outputWriter.printARException(e);
        }
    }
    
    void setOverlayGroup(){
    	 try {
             outputWriter.driverPrintHeader("SET OVERLAY GROUP FOR OBJECTS");
             String overlayGroup = InputReader.getString("Group Name: ", "");
             getControlStructObject().setOverlayGroup(overlayGroup);
             System.out.println("\nCall Returned OK");
         } catch (IOException e) {
             outputWriter.printString("Problem in getting the input...Driver problem..\n");
         } catch (NullPointerException e) {
             outputWriter.driverPrintException(e);
         }
    }

    void verifyUser2() {
        try {
            outputWriter.driverPrintHeader("VERIFY USER2");

            // Now call the verifyUser method
            beginAPICall();
            endAPICall(getControlStructObject().getLastStatus());

            // Print the information
            ARServerUser u = getControlStructObject();
            outputWriter.print("",
                    "Verify User2 Results:", u);
            outputWriter.printBoolean("",
                    "Struct Admin Flag: ", u.isStructAdmin(), "\n");
            outputWriter.printBoolean("",
                    "Struct SubAdmin Flag: ", u.isStructSubadmin(), "\n");
            outputWriter.printBoolean("",
                    "Overlay Group Flag: ", u.isMemberOfOverlayGroup(),
                    "\n");
            outputWriter.printBoolean("",
                    "Base Overlay Group Flag: ", u.isMemberOfBaseOverlayGroup(),
                    "\n");
            // Success. Print the status
            List<StatusInfo> statusList = u.getLastStatus();
            outputWriter.printStatusInfoList("", "Verify User2 Status",
                    statusList);
        } catch (Exception e) {
            outputWriter.driverPrintException(e);
        }
    }
}

class ThreadControlLocalStorage extends ThreadLocal<Object> {

    protected Object initialValue() {
        super.initialValue();
        ThreadControlBlock threadControlBlockPtr = new ThreadControlBlock();
        set(threadControlBlockPtr);
        return get();
    }
}

