package com.harness.mysql;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.Class;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.Map;

import com.google.gson.Gson;

import com.harness.common.TestHarness;

public class MySQLTestHarness extends TestHarness{

	MySQLConfiguration config = new MySQLConfiguration();

	private MySQLTestHarness() {
		this.config = new MySQLConfiguration();
	}

	public static MySQLTestHarness createMySQLTestHarness(String filename) {
		MySQLTestHarness mth = new MySQLTestHarness();
		mth.init(filename);
		return mth;
	} 

    protected void init(String filename) {
        try {
            FileReader f = new FileReader(filename);
            Gson gson = new Gson();
            config = gson.fromJson(f, MySQLConfiguration.class);
            outputContent = "";
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        int messagesPerIteration = config.getMessagesPerIteration();

        outputContent += "Messages Per Iteration: " + messagesPerIteration + "\n";
        outputContent += "Number of Iterations: " + config.getIterations() + "\n";

    }

	protected Map<String, Long> dispatchEvents() {
	    ConcurrentHashMap<String, Long> callTimes = new ConcurrentHashMap<String, Long>();	

	    int totalIterations = config.getIterations();
	    int messagesPerIteration = config.getMessagesPerIteration();

	    String workloadType = config.getWorkloadType();

		CountDownLatch countDownLatch = new CountDownLatch (totalIterations * messagesPerIteration);

	    try {

	        for (int iteration = 0; iteration < totalIterations; iteration++) {
	        	if (workloadType.equals("read")) {
	        		// Spawn read thread
	        	}
	        	else if (workloadType.equals("write")) {
	        		// Spawn write thread
	        	}
	        	else if (workloadType.equals("mixed")) {
	        		// Spawn a mixed result thread
	        	}
	        }

		} catch (Exception e) {
			e.printStackTrace();
		}
		return callTimes;
	}

    protected void printResults(Map<String, Long> timePerCall) {
        int time = 0;

        for (String i : timePerCall.keySet()) {
            time += timePerCall.get(i);
            outputContent += "Latency for " + i + ", " + timePerCall.get(i) + "\n";
        }

        outputContent += "Total Time, " + time;
        try{
            FileWriter fstream = new FileWriter(config.getOutputFilename());
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(outputContent);
            out.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

}
