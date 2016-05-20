package com.harness.accumulo;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.Class;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.Map;

import com.google.gson.Gson;

import com.harness.accumulo.ReadThread;
import com.harness.common.TestHarness;
import com.harness.common.Client;

import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.data.Range;
import org.apache.hadoop.io.Text;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.AccumuloException;
import java.util.Iterator;
import java.util.Map.Entry;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;


public class AccumuloTestHarness extends TestHarness {

	AccumuloConfiguration config;

	private AccumuloTestHarness() {
		this.config = new AccumuloConfiguration();
	}

	public static AccumuloTestHarness createAccumuloTestHarness(String filename) {
		AccumuloTestHarness ath = new AccumuloTestHarness();
		ath.init(filename);
		return ath;
	}

    protected void init(String filename) {
        try {
            FileReader f = new FileReader(filename);
            Gson gson = new Gson();
            config = gson.fromJson(f, AccumuloConfiguration.class);
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
			//Instance instance = new ZooKeeperInstance(config.getZooKeeperInstance(), config.getZooServers());
			//Connection connection = instance.getConnector(config.getAccumuloUser(), config.getAccumuloPassword());

        /*Client client = null;
        try {
            client = new Client("instance", "localhost", "s22", "p");
        } catch (AccumuloException ex) {
            ex.printStackTrace();
        }
        catch (AccumuloSecurityException ex) {
            ex.printStackTrace();
        }
        long startTime = 0;
        long endTime = 0;
        try {
            startTime = System.currentTimeMillis();
            Iterator<Entry<Key, Value>> iterator = client.read("mathzor", new Authorizations("s22"), new Range(new Text("1"), new Text("1459")));
System.out.println("Is there something? " + iterator.hasNext());
            while (iterator.hasNext()) {
                Entry<Key, Value> entry = (Entry<Key, Value>) iterator.next(); 
                Text row = entry.getKey().getRow();
                Value value = entry.getValue();
            }

            endTime = System.currentTimeMillis();            
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/

	        for (int iteration = 0; iteration < totalIterations; iteration++) {
	        	if (workloadType.equals("read")) {
                                System.out.println(config.getTable());
	        		// Spawn read thread
	        		new ReadThread(countDownLatch, callTimes, iteration, config.getZooKeeperInstance(), config.getZooServers(), config.getAccumuloUser(), config.getAccumuloPassword(), config.getTable(), config.getAuthorization(), config.getRangeStart(), config.getRangeEnd());
	        	}
	        	else if (workloadType.equals("write")) {
	        		// Spawn write thread
	        	}
	        	else if (workloadType.equals("mixed")) {
	        		// Spawn a mixed result thread
	        	}
	        	else if (workloadType.equals("map_reduce")) {
	        		// Spawn a mapreduce thread
	        	}
	        }
                //Thread.sleep(10000);
                countDownLatch.await();
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

