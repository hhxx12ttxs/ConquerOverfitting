package com.bmc.arsys.demo.javadriver;

import com.bmc.arsys.api.*;

import java.io.*;
import java.util.*;

public class ThreadControlBlock {

    ARServerUser context = null;

    String currentInputFileName = null;

    Stack<InputFile> inFile = new Stack<InputFile>();

    PrintWriter outFile;

    boolean isStdOut = false;

    PrintWriter recordFile;

    PrintWriter loggingFile;

    String buffer;

    String args;

    boolean primaryThread;

    String outFileName;

    String firstListId;

    String secondListId;

    String lastListId;

    String currCommand;

    int numFailedBeginLoop;

    int currentLoopDepth;

    int[] numIterations;

    long[] loopBeginFilePos;

    Stack<Object> threadHandles = null;

    SyncObject waitObject;

    SyncObject releaseObject;

    public ARServerUser getContext() {
        return context;
    }

    public String getFirstListId() {
        return firstListId;
    }

    public String getLastListId() {
        return lastListId;
    }

    public String getSecondListId() {
        return secondListId;
    }

    public String getCurrentCommand() {
        return currCommand;
    }

    public void setBuffer(String content) {
        buffer = content;
    }

    public void resetListIds() {
        firstListId = null;
        secondListId = null;
        lastListId = null;
    }

    public void setFirstListId(String id) {
        firstListId = id;
    }

    public void setLastListId(String id) {
        lastListId = id;
    }

    public void setSecondListId(String id) {
        secondListId = id;
    }

    public void setCurrentInputFileReadingPostion(long filePosition) throws IOException, FileNotFoundException {
        InputFile fp = (InputFile) inFile.pop();
        if (fp.isFileStdIn()) {
            inFile.push(fp);
        } else {
            // Get the current input file name 
            String name = fp.getFileName();

            // Close the current input file
            fp.close();

            // Create the new buffered reader with the same name
            inFile.push(new InputFile(name));

            // Set the current input files reading position
            long currentPos = 0;
            BufferedReader reader = getCurrentInputFile();
            String inputLine = null;

            do {
                inputLine = reader.readLine();
                if (inputLine != null)
                    currentPos += inputLine.length();
            } while (currentPos < filePosition);
            setCurrentInputFilePosition(currentPos);
        }
    }

    public void setCurrentInputFilePosition(long pos) {
        InputFile fp = (InputFile) inFile.peek();
        fp.setCurrentPosition(pos);
    }

    public void addToCurrentInputFilePosition(long increment) {
        InputFile fp = (InputFile) inFile.peek();
        fp.addToCurrentPosition(increment);
    }

    public long getCurrentInputFilePos() {
        InputFile fp = (InputFile) inFile.peek();
        return fp.getCurrentPosition();
    }

    public void setLoopBeginFilePos(int index, long filePosition) {
        loopBeginFilePos[index] = filePosition;
    }

    public long getLoopBeginFilePos(int index) {
        return loopBeginFilePos[index];
    }

    public void setNumberOfIterations(int index, int iterations) {
        numIterations[index] = iterations;
    }

    public int getNumberOfIterations(int index) {
        return numIterations[index];
    }

    public void reduceNumberOfIterations(int index) {
        numIterations[index] = numIterations[index] - 1;
    }

    public void incrementNumFailedBeginLoop() {
        numFailedBeginLoop++;
    }

    public void reduceNumFailedBeginLoop() {
        numFailedBeginLoop--;
    }

    public int getNumFailedBeginLoop() {
        return numFailedBeginLoop;
    }

    public int getCurrentLoopDepth() {
        return currentLoopDepth;
    }

    public void incrementCurrentLoopDepth() {
        currentLoopDepth++;
    }

    public void reduceCurrentLoopDepth() {
        currentLoopDepth--;
    }

    public void addThreadHandle(Object handle) {
        if (threadHandles == null) {
            threadHandles = new Stack<Object>();
        }
        threadHandles.push(handle);
    }

    public Object getThreadHandle() {
        if ((threadHandles != null) && (!threadHandles.isEmpty())) {
            return threadHandles.peek();
        }
        return null;
    }

    public void deleteThreadHandle() {
        if (threadHandles != null) {
            threadHandles.pop();
        }
    }

    public ThreadControlBlock() {
        waitObject = new SyncObject(false);
        releaseObject = new SyncObject(false);
        numIterations = new int[10];
        loopBeginFilePos = new long[10];
        context = new ARServerUser();
        resetListIds();
    }

    public SyncObject getWaitObject() {
        return waitObject;
    }

    public SyncObject getReleaseObject() {
        return releaseObject;
    }

    public void setWaitObjectToNull() {
        waitObject = null;
    }

    public void setReleaseObjectToNull() {
        releaseObject = null;
    }

    public boolean isCurrentInputSourceStdInput() {
        InputFile fp = (InputFile) inFile.peek();
        return fp.isFileStdIn();
    }

    public void setAuthentication(String authString) {
        context.setAuthentication(authString);
    }

    public void setUser(String name) {
        context.setUser(name);
    }

    public void setLocale(String locale) {
        context.setLocale(locale);
    }

    public void setTimeZone(String timeZone) {
        context.setTimeZone(timeZone);        
    }
    
    public void setPassword(String password) {
        context.setPassword(password);
    }

    public void setServer(String server) {
        context.setServer(server);
    }

    public void setPort(int portNumber) {
        context.setPort(portNumber);
    }

    public void setPrivateRpcQueue(int progNumber) {
        try {
            context.usePrivateRpcQueue(progNumber);
        } catch (ARException e) {
            System.out.println("ARException :" + e);
        }
    }

    public String getBuffer() {
        return buffer;
    }

    public void setCurrentCommand(String command) {
        currCommand = command;
    }

    public void setArgs(String argsString) {
        args = argsString;
    }

    public String getArgs() {
        return args;
    }

    public boolean getIsStdOut() {
        return isStdOut;
    }

    public void setOutputFile(String fileName) throws IOException {
        // Close the current output file if it is not stdout
        if ((isStdOut == false) && (outFile != null)) {
            outFile.close();
        }

        // open the new output file
        outFile = new PrintWriter(new FileOutputStream(new File(fileName)));
        outFileName = fileName;
        isStdOut = false;
    }

    public void setOutputFile(PrintWriter fp, String name, boolean stdOutFlag) {
        // Close the current output file if it is not stdout
        if ((isStdOut == false) && (outFile != null)) {
            outFile.close();
        }

        outFile = fp;
        outFileName = name;
        isStdOut = stdOutFlag;
    }

    public void closeInputFiles() throws IOException {
        while (!inFile.empty()) {
            InputFile file = (InputFile) inFile.pop();
            file.close();
        }
    }

    public void closeOutputFile() {
        if ((outFile != null) && (isStdOut == false)) {
            System.out.println("isStdout is :" + isStdOut);
            System.out.println("Closing the output file");
            outFile.flush();
            outFile.close();
        }
        outFile = null;
        outFileName = null;
        isStdOut = false;
    }

    public void closeRecordFile() {
        if (recordFile != null) {
            recordFile.close();
        }
        recordFile = null;
    }

    public void setOutputFileName(String name) {
        outFileName = name;
    }

    public String getOutputFileName() {
        return outFileName;
    }

    public void setOutputToStdOut() {
        if (isStdOut == false) {
            if (outFile != null) {
                outFile.close();
                outFile = null;
            }
        }
        outFileName = null;

        isStdOut = true;
        //System.out.println(" In setoutputToStdout isStdOut is :" + isStdOut);
        outFile = new PrintWriter(System.out);
    }

    public void setRecordFile(PrintWriter recFile) {
        if (recordFile != null) {
            recordFile.close();
            recordFile = null;
        }
        recordFile = recFile;
    }

    public PrintWriter getRecordFile() {
        return recordFile;
    }

    public BufferedReader getCurrentInputFile() {
        InputFile fp = (InputFile) inFile.peek();
        setCurrentInputFileName(fp.name);
        return fp.getFileReader();
    }

    public void setCurrentInputFile(InputFile ipFile) {
        setCurrentInputFileName(ipFile.getFileName());
        inFile.push(ipFile);
    }

    public void setCurrentInputFile(String name) throws FileNotFoundException {
        setCurrentInputFileName(name);
        inFile.push(new InputFile(name));
    }

    public void setCurrentInputToStdIn() {
        setCurrentInputFileName(null);
        inFile.push(new InputFile(System.in, true));
    }

    public void closeCurrentInputFile() throws IOException {
        InputFile temp = (InputFile) inFile.pop();
        temp.close();
        temp = null;
    }

    public boolean getPrimaryThread() {
        return primaryThread;
    }

    public void setPrimaryThread(boolean primary) {
        primaryThread = primary;
    }

    public PrintWriter getOutFile() {
        return outFile;
    }

    /**
     * @return the inFileName
     */
    public String getCurrentInputFileName() {
        return currentInputFileName;
    }

    /**
     * @param inFileName the inFileName to set
     */
    public void setCurrentInputFileName(String inFileName) {
        this.currentInputFileName = inFileName;
    }

    public boolean processCommentLine(String commentLine) throws IOException {
        // determine if this comment is a special transaction begin/end
        // indicator in which case we need to echo it to the performance
        // log file

        if (commentLine.startsWith("## transaction")) {
            logTransaction(commentLine);
            //  indicate that we do not want this comment echoed in the
            //  result output file if there is one
            return false;
        }

        return true;
    }

    protected void threadSleep(long sleepTimeInMilliSeconds) {
        if (sleepTimeInMilliSeconds > 0) {
            try {
                Thread.sleep(sleepTimeInMilliSeconds);
            } catch (InterruptedException e) {
            }
        }
    }

    protected boolean isResultFileOpened() {
        return true;
    }

    protected void writeInResultFile(String content) {
        //do nothing
    }

    protected void logTransaction(String commentLine) throws IOException {
        //do nothing
    }

}

