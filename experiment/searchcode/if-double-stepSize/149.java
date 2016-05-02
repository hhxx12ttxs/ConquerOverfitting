/**
 * 
 * Copyright 2010 Paul Scherrer Institute. All rights reserved.
 * 
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This code is distributed in the hope that it will be useful,
 * but without any warranty; without even the implied warranty of
 * merchantability or fitness for a particular purpose. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package ch.psi.fda.core.loops.cr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import jcifs.smb.SmbFile;
import gov.aps.jca.CAException;
import ch.psi.fda.core.Action;
import ch.psi.fda.core.ActionLoop;
import ch.psi.fda.core.Sensor;
import ch.psi.fda.core.actors.OTFActuator;
import ch.psi.fda.core.messages.ComponentMetadata;
import ch.psi.fda.core.messages.DataMessage;
import ch.psi.fda.core.messages.DataMessageMetadata;
import ch.psi.fda.core.messages.DataQueue;
import ch.psi.fda.core.messages.EndOfStreamMessage;
import ch.psi.fda.core.messages.Message;
import ch.psi.fda.core.sensors.MillisecondTimestampSensor;
import ch.psi.fda.core.sensors.OTFNamedChannelSensor;
import ch.psi.fda.core.sensors.OTFScalerChannelSensor;
import ch.psi.jcae.ChannelBeanFactory;

/**
 * @author ebner
 * 
 * While using Crlogic the IOC system clock rate should/must be set to 1000 (default 60)
 * 
 * sysClkRateSet 1000
 *
 */
public class CrlogicLoop implements ActionLoop {
	
	// Get Logger
	private static final Logger logger = Logger.getLogger(CrlogicLoop.class.getName());
	
	/**
	 * Flag to indicate whether the data of this loop will be grouped
	 * According to this flag the dataGroup flag in EndOfStream will be set.
	 */
	private boolean dataGroup = false;
	
	boolean keepTmpFiles = true;
	
	private BlockingQueue<String> readQueue = new LinkedBlockingQueue<String>();
	private volatile boolean stopReadoutThread = false;
	private Thread readoutThread;
	
	// Constants
	
	/**
	 * Default timeout (in milliseconds) for wait operations
	 */
	private long startStopTimeout = 8000;
	
	/**
	 * Name of the NFS server to place the data of the OTF logic
	 */
	private final String server;
	/**
	 * Share on the NFS server to put the OTF data on to
	 */
	private final String share;
	/**
	 * SMB share  to access the data written by the OTF C logic
	 */
	private final String smbShare;
	
	/**
	 * Flag whether the actor of this loop should move in zig zag mode
	 */
	private final boolean zigZag;
	
	boolean useReadback;
	boolean useEncoder;
	
	/**
	 * List of actions that are executed at the beginning of the loop.
	 */
	private List<Action> preActions;
	/**
	 * List of actions that are executed at the end of the loop.
	 */
	private List<Action> postActions;
	
	/**
	 * Prefix for the CRLOGIC channels
	 */
	private String prefix;
	
	private CrlogicChannelsTemplate template;
	private MotorChannelsTemplate motortemplate;
	
	/**
	 * Semaphore to ensure that data is read in correct sequence
	 */
	private Semaphore semaphore = new Semaphore(1);
	
	/**
	 * Special OTF Actuator
	 */
	private OTFActuator actuator = null;
	
	/**
	 * List of sensors of this loop
	 */
	private List<Sensor> sensors;
	
	private List<String> readoutResources;
	private Map<Integer, CrlogicDeltaDataFilter> scalerIndices;
	private CrlogicRangeDataFilter crlogicDataFilter;
	
	/**
	 * Data queue sensor data is posted to. A message consists of a list of data objects
	 * that are read out of the sensors of this loop.
	 */
	private BlockingQueue<Message> dataQueue;
	
	/**
	 * Abort status
	 */
	private boolean abort = false;
	private boolean abortForce = false;
	private Thread executionThread = null;
	
	
	public CrlogicLoop(String prefix, String server, String share, String smbShare, boolean zigZag){
		this.prefix = prefix;
		this.server = server;
		this.share = share;
		this.smbShare = smbShare;
		this.zigZag = zigZag;
		
		// Initialize lists used by the loop
		this.preActions = new ArrayList<Action>();
		this.postActions = new ArrayList<Action>();
		this.sensors = new ArrayList<Sensor>();
		this.readoutResources = new ArrayList<String>();
		this.scalerIndices = new HashMap<Integer, CrlogicDeltaDataFilter>();
		
		this.crlogicDataFilter = new CrlogicRangeDataFilter();
		this.dataQueue = new LinkedBlockingQueue<Message>(2000);
	}
	
		
	/**
	 * Collect data from share
	 * @param tmpFileName	Name of the temporary file
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	private void collectData(String tmpDir, String tmpFileName) throws InterruptedException, IOException {
		semaphore.acquire();
		if (tmpDir.startsWith("smb:")) {

			SmbFile tmpFile = new SmbFile(tmpDir, tmpFileName);
			logger.info("Collect data from " + tmpFile.getCanonicalPath());

			SmbFile lockfile = new SmbFile(tmpFile.getCanonicalPath() + ".lock");

			logger.info("Wait until file is written [lock file: " + lockfile.getCanonicalPath() + "]");
			// Wait until file is created
			while ((!tmpFile.exists()) || lockfile.exists()) {
				try{
					Thread.sleep(100);
				}
				catch(InterruptedException e){
					abort=true;
				}
				 if(abort){
					 // If abort is issued while waiting for data immediately return without
					 // trying to read the data
					 return;
				 }
			}

			InputStreamReader inreader = new InputStreamReader(tmpFile.getInputStream());
			BufferedReader in = new BufferedReader(inreader);
			String line;
			boolean firstline = true;
			int linecount=0;
			int mcounter=0;
			
//			boolean wasInRangeBefore = false;
			boolean discardAnyway = false;
			
			while (true) {
				line = in.readLine();
				linecount++;
				if (line == null) {
					break;
				} else {
					// if(line.matches("^\\[.*")){
					if (line.matches("^ *#.*")) {
						// Skip header/comment lines
//						logger.info("HEADER: " + line);
					} else {
						if (firstline) {
							firstline = false;
							continue;
						}

//						logger.info(line);

						// Write into queue
						DataMessage message = new DataMessage();
						String[] tokens = line.split("\t");
						boolean use = true;
						
						for(int i=0;i<tokens.length;i++){
							String t = tokens[i];
							Double val;
							
							if(i==0){
								Double raw = new Double(t);
								
								if(useEncoder){
									val = crlogicDataFilter.calculatePositionMotorUseEncoder(raw);
								}
								else if(useReadback){
									val = crlogicDataFilter.calculatePositionMotorUseReadback(raw);
								}
								else{
									val = crlogicDataFilter.calculatePositionMotor(raw);
								}
								
								// Check whether data is within the configured range - otherwise drop data
								use = crlogicDataFilter.filter(val);
//								if(!use){
//									break;
//								}
							}
							else if(scalerIndices.containsKey(i)){
								CrlogicDeltaDataFilter f = scalerIndices.get(i); 
								val = f.delta(new Double(t));
							}
							else{
								val = new Double(t);
							}
							
							
							message.getData().add(val);
						}
						
						// Does not work if zigzag, ... 
//						// Use this to filter out motor retry movements at the end of the scan
//						wasInRangeBefore = wasInRangeBefore | use;
//						if(!use && wasInRangeBefore){
//							discardAnyway=true;
//							// Optimization - terminate read loop once range is left
//							logger.info("Terminate read loop because point is outside range");
//							break;
//						}
						
						// Filter data
						if(use && !discardAnyway){
							dataQueue.put(message);
							mcounter++;
						}
						
						
					}
				}
			}
			in.close();
			inreader.close();
			
			logger.info("Lines read: "+linecount+" Messages generated (after filtering): "+mcounter);
			
			// Remove temporary file
			if(!keepTmpFiles){
				tmpFile.delete();
			}
		}
		else{
			// TODO - File in local file system
		}
		
		// Issue end of loop control message
		dataQueue.put(new EndOfStreamMessage(dataGroup));
		semaphore.release();
	}
	
	

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.Action#execute()
	 */
	@Override
	public void execute() throws InterruptedException {
		try{
		
			double stepSize = actuator.getStepSize();
			double start = actuator.getStart();
			double end = actuator.getEnd();
			double integrationTime = actuator.getIntegrationTime();
			double ubacklash = actuator.getAdditionalBacklash();
			
			// Set values for the datafilter
			crlogicDataFilter.setStart(actuator.getStart());
			crlogicDataFilter.setEnd(actuator.getEnd());
			
			// Reset data filter
			for(Integer k: scalerIndices.keySet()){
				scalerIndices.get(k).reset();
			}
			
			synchronized(this){
				// Set abort state to false
				abort = false;
				abortForce = false;
			
			// Set execution thread to current thread
				executionThread = Thread.currentThread();
			}
			
			
			// TODO each actuator will result in an additional sensor (at the beginning)
			// Dependent on actuator settings (readback use encoder, ...)
			
			
			// TODO filename generation?
			final String tmpFileName = "tmp-"+System.currentTimeMillis()+".txt";
			
			Long timeout = 600000l; // 10 minutes move timeout
			
			// Check if logic is inactive, otherwise return early
			if(!template.getStatus().getValue().equals(CrlogicChannelsTemplate.Status.INACTIVE.toString())){
				logger.info("CRLOGIC is not inactive!");
				// TODO Decide what to do in this situation
				
				if(template.getStatus().getValue().equals(CrlogicChannelsTemplate.Status.FAULT.toString())){
					// If in fault show message and recover
					logger.info("CRLOGIC in FAULT state");
					logger.info("Error message: "+template.getMessage().getValue());
					logger.info("Recover logic and set it to INACTIVE");
					template.getStatus().setValue(CrlogicChannelsTemplate.Status.INACTIVE.toString());
				}
				else if(template.getStatus().getValue().equals(CrlogicChannelsTemplate.Status.ACTIVE.toString())){
					template.getStatus().setValue(CrlogicChannelsTemplate.Status.STOP.toString());
					template.getStatus().waitForValue(CrlogicChannelsTemplate.Status.INACTIVE.toString(), startStopTimeout);
				}
				else{
					throw new RuntimeException("CRLOGIC is not inactive");
				}
			}
			
			
			logger.info("Set parameters");
			template.getNfsServer().setValue(server);
			template.getNfsShare().setValue(share);
			template.getDataFile().setValue(tmpFileName);
			
			int tps = template.getTicksPerSecond().getValue();
			logger.info("Ticks per second: "+tps);
			
			logger.info("Set readout resources");
			
			template.getReadoutResources().setValue(readoutResources.toArray(new String[readoutResources.size()]));
			
			
			
			// Set ticks between interrupt to integration time
			int ticks = (int)(tps*integrationTime);
			template.getTicksBetweenInterrupts().setValue(ticks);
			
			// Prepare motor
			double totalTimeSeconds = Math.abs((end-start)/stepSize*integrationTime);
			int hours = (int) Math.floor(totalTimeSeconds/60/60);
			int minutes = (int) Math.floor(totalTimeSeconds/60-hours*60);
			int seconds = (int) Math.floor(totalTimeSeconds-hours*60*60-minutes*60);
			
			logger.info("Estimated time: "+hours+":"+minutes+":"+seconds);
			
			int direction = 1;
			if(end-start<0){
				direction = -1;
			}
			
			double motorBaseSpeed = motortemplate.getBaseSpeed().getValue();
			double motorHighLimit = motortemplate.getHighLimit().getValue();
			double motorLowLimit = motortemplate.getLowLimit().getValue();
			double motorBacklash = motortemplate.getBacklashDistance().getValue();
			
			boolean respectMotorMinSpeed = false; // if false set min speed to 0
			double motorMinSpeed = 0;
			if(respectMotorMinSpeed){
				motorMinSpeed = motorBaseSpeed;
			}
			
			// Check user parameters
			// TODO start and end values must be between the motor high and low value - otherwise fail
			if(start>motorHighLimit || start<motorLowLimit){
				// Start value is outside motor high and/or low value
				logger.info("Start value is outside motor high and/or low value");
				throw new IllegalArgumentException("Start value is outside motor high and/or low value");
			}
			if(end>motorHighLimit || end<motorLowLimit){
				// End value is outside motor high and/or low value
				logger.info("End value is outside motor high and/or low value");
				throw new IllegalArgumentException("End value is outside motor high and/or low value");
			}
			// TODO Check minimum step size
			int minimumTicks = 10;
			double minStepSize = motorMinSpeed*(minimumTicks/tps);
			if(stepSize<minStepSize){
				// Step size is too small
				logger.info("Step size is too small");
				throw new IllegalArgumentException("Step size is too small");
			}
			// TODO Check integration time
			if(motorMinSpeed>0){
				double maxIntegrationTime = stepSize/motorMinSpeed;
				if(integrationTime>maxIntegrationTime){
					logger.info("Integration time is too big");
					// Integration time is too big
					throw new IllegalArgumentException("Integration time is too big");
				}
			}
			double motorMaxSpeed = motortemplate.getVelocity().getValue();
			double minIntegrationTime = Math.min( (stepSize/motorMaxSpeed), ((double)minimumTicks/(double)tps) );
			if(integrationTime<minIntegrationTime){
				// Integration time is too small
				logger.info("Integration time is too small [min integration time: "+minIntegrationTime+"]");
				throw new IllegalArgumentException("Integration time is too small [min integration time: "+minIntegrationTime+"]");
			}
			
			
			// TODO Calculate and set motor speed, backlash, etc.
			double motorSpeed = stepSize/integrationTime;
			double backlash = (0.5*motorSpeed*motortemplate.getAccelerationTime().getValue())+motorBacklash+ubacklash;
			double realEnd = end+(backlash*direction);
			double realStart = start-(backlash*direction);
	
			
			// Move to start
			logger.info("Move motor to start ["+realStart+"]");
			motortemplate.getSetValue().setValue(realStart, timeout); // Will block until move is done
			
			
			// Set motor paramters
			// Backup settings
			logger.info("Backup motor settings");
			double backupSpeed = motortemplate.getVelocity().getValue();
			double backupBacklash = motorBacklash;
			double backupMinSpeed = motorBaseSpeed;
			
			try{
				// Set motor settings
				logger.info("Update motor settings");
//				if(!respectMotorMinSpeed){
//					motortemplate.getBaseSpeed().setValue(0d);
//				}
				// Set base speed as fast as possible but not faster than the original base speed.
				double base = motorBaseSpeed;
				if(motorSpeed<base){
					base = motorSpeed;
				}
				motortemplate.getBaseSpeed().setValue(base);
				motortemplate.getVelocity().setValue(motorSpeed);
				motortemplate.getBacklashDistance().setValue(0d);
				
				
				// Execute pre actions
				for(Action action: preActions){
					action.execute();
				}
				
				
				// Start crlogic logic
				logger.info("Start CRLOGIC");
				template.getStatus().setValue(CrlogicChannelsTemplate.Status.INITIALIZE.toString());
				try{
					template.getStatus().waitForValue(CrlogicChannelsTemplate.Status.ACTIVE.toString(), startStopTimeout);
				}
				catch(CAException e){
					logger.info( "Failed to start CRLOGIC. Logic in status: "+template.getStatus().getValue() );
					if(template.getStatus().getValue().equals(CrlogicChannelsTemplate.Status.FAULT.toString())){
						logger.info("Error message: "+template.getMessage().getValue());
					}
					// Recover to inactive
					template.getStatus().setValue(CrlogicChannelsTemplate.Status.INACTIVE.toString());
					// TODO Improve error handling
					throw new RuntimeException("Failed to start CRLOGIC. Logic in status: "+template.getStatus().getValue()+ " Error message: "+template.getMessage().getValue(), e);
					
				}

				// Move motor(s) to end / wait until motor is stopped
				logger.info("Move motor to end ["+realEnd+"]");
				try{
					motortemplate.getSetValue().setValue(realEnd, timeout); // Will block until move is done
				}
				catch (InterruptedException e) {
					if(abort & (!abortForce)){
						// Abort motor move 
						motortemplate.getCommand().setValue(MotorChannelsTemplate.Commands.Stop.ordinal());
						motortemplate.getCommand().setValue(MotorChannelsTemplate.Commands.Go.ordinal());
					}
					else{
						throw e;
					}
				}
				logger.info("Motor reached end position");
				
				// Stop crlogic logic
				logger.info("Stop CRLOGIC");
				template.getStatus().setValue(CrlogicChannelsTemplate.Status.STOP.toString());
				// Wait until stopped
				logger.info("Wait until stopped");
				try{
					template.getStatus().waitForValue(CrlogicChannelsTemplate.Status.INACTIVE.toString(), startStopTimeout);
				}
				catch(CAException e){
					logger.info( "Failed to stop CRLOGIC. Logic in status: "+template.getStatus().getValue() );
					// TODO Improve error handling
					throw new RuntimeException("Failed to stop CRLOGIC.  Logic in status: "+template.getStatus().getValue(), e);
				}
				logger.info("CRLOGIC is now stopped");
				
				
				// Execute post actions
				for(Action action: postActions){
					action.execute();
				}
				
				
			}
			finally{
				logger.info("Restore motor settings");
				motortemplate.getBaseSpeed().setValue(backupMinSpeed);
				motortemplate.getVelocity().setValue(backupSpeed);
				motortemplate.getBacklashDistance().setValue(backupBacklash);
			}
			
			// Request read of data file
			readQueue.put(tmpFileName);
			
//			// Read data
//			Thread t = new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
//					try {
//						collectData(smbShare, tmpFileName);
//					} catch (InterruptedException e) {
//						throw new RuntimeException("Unable to read CRLOGIC raw data file",e);
//					} catch (IOException e) {
//						throw new RuntimeException("Unable to read CRLOGIC raw data file",e);
//					}
//				}
//			});
//			t.start();
			
			if(zigZag){
				// Swap start and end
				double aend = actuator.getEnd();
				actuator.setEnd(actuator.getStart());
				actuator.setStart(aend);
			}
			
			synchronized(this){
				executionThread = null;
			}
		}
		catch(CAException e){
			throw new RuntimeException("Unable to execute crloop", e);
		}
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.Action#abort()
	 */
	@Override
	public void abort() {
		abort(false);
	}
	
	/**
	 * Abort logic
	 * @param force
	 */
	public synchronized void abort(boolean force){
		abort = true;
		abortForce = force;
		
		// executionThread variable guarded by "this"
		if(executionThread != null){
			executionThread.interrupt();
		}
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.Action#destroy()
	 */
	@Override
	public void destroy() {
		stopReadoutThread = true;
		readoutThread.interrupt();
		// TODO eventually interrupt readout thread
		
		try {
			
			ChannelBeanFactory.getFactory().destroyChannelBeans(template);
			ChannelBeanFactory.getFactory().destroyChannelBeans(motortemplate);
			template = null;
			motortemplate = null;
			
		} catch (Exception e) {
			throw new RuntimeException("Unable to destroy CrlogicLoop", e);
		}
		
		
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.ActionLoop#prepare()
	 */
	@Override
	public void prepare() {
		
		stopReadoutThread = false;
		// Start readout Thread
		readoutThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(!stopReadoutThread){
					String file;
					try {
						file = readQueue.take();
					} catch (InterruptedException e) {
						break;
					}
					// TODO Read file and 
					try {
						collectData(smbShare, file);
					} catch (InterruptedException e) {
						throw new RuntimeException("Unable to read CRLOGIC raw data file",e);
					} catch (IOException e) {
						throw new RuntimeException("Unable to read CRLOGIC raw data file",e);
					}
					
				}
				
			}
		});
		readoutThread.start();
		
		try{
			// Connect crlogic channels
			template = new CrlogicChannelsTemplate();
			logger.info("Connect channels");
			ChannelBeanFactory.getFactory().createChannelBeans(template, prefix);
			
			// Connect motor channels
			motortemplate = new MotorChannelsTemplate();
			ChannelBeanFactory.getFactory().createChannelBeans(motortemplate, actuator.getName());
			
			useReadback = motortemplate.getUseReadback().getValue();
			useEncoder = motortemplate.getUseEncoder().getValue();
			
			logger.info("Motor type: "+ MotorChannelsTemplate.Type.values()[motortemplate.getType().getValue()]);
			logger.info("Motor use readback: "+useReadback);
			logger.info("Motor use encoder: "+useEncoder);
			
			// TODO build up list of readout resources (based on sensors)
			readoutResources.clear();
			// first sensor is the actuator
			
			// Determine mode of motor
			if((!useReadback) && (!useEncoder)){
				// Open loop
				if(actuator.getReadback()!=null){
					throw new IllegalArgumentException("Readback not supported if motor is configured in open loop");
				}
				else{
					readoutResources.add(actuator.getName());
				}
				
			}
			else if(useReadback && (!useEncoder)){
				String readback;
				// use readback link
				if(actuator.getReadback()!=null){
					// Use specified readback
					readback = (actuator.getReadback());
				}
				else{
					// Set resouce to readback link
					readback = (motortemplate.getReadbackLink().getValue());
					readback = readback.replaceAll(" +.*", ""); // remove NPP etc at the end
				}
				
				readoutResources.add(readback);
				
				// Fill readback encoder settings
				// Connect to encoder
				EncoderChannelsTemplate encodertemplate = new EncoderChannelsTemplate();
				ChannelBeanFactory.getFactory().createChannelBeans(encodertemplate, readback);
				
				// Read encoder settings
				if(encodertemplate.getDirection().getValue()==EncoderChannelsTemplate.Direction.Positive.ordinal()){
					crlogicDataFilter.setEncoderDirection(1);
				}
				else{
					crlogicDataFilter.setEncoderDirection(-1);
				}
				crlogicDataFilter.setEncoderOffset(encodertemplate.getOffset().getValue());
				crlogicDataFilter.setEncoderResolution(encodertemplate.getResolution().getValue());
				
				// Disconnect from encoder
				ChannelBeanFactory.getFactory().destroyChannelBeans(encodertemplate);
				
			}
			else if (useEncoder && (!useReadback)){
				// use readback link
				if(actuator.getReadback()!=null){
					throw new IllegalArgumentException("Readback not supported if motor is configured to use encoder");
				}
				else{
					// Set resouce to readback link
					readoutResources.add(actuator.getName()+"_ENC");
				}
			}
			else{
				throw new IllegalArgumentException("Motor configuration not supportet: use readback - "+useReadback+" use encoder - "+useEncoder);
			}
			
			// Fill Motor specific settings
			if(motortemplate.getDirection().getValue()==MotorChannelsTemplate.Direction.Positive.ordinal()){
				crlogicDataFilter.setMotorDirection(1);
			}
			else{
				crlogicDataFilter.setMotorDirection(-1);
			}
			crlogicDataFilter.setMotorEncoderResolution(motortemplate.getEncoderResolution().getValue());
			crlogicDataFilter.setMotorOffset(motortemplate.getOffset().getValue());
			crlogicDataFilter.setMotorReadbackResolution(motortemplate.getReadbackResolution().getValue());
			crlogicDataFilter.setMotorResolution(motortemplate.getMotorResolution().getValue());
			
			// Clear all indices
			scalerIndices.clear();
			
			int c = 1; // We start at 1 because the actuator right now is an implicit sensor 
			for(Sensor s: sensors){
					if(s instanceof OTFNamedChannelSensor){
						// Monitored channel (MUST be configured MODULE ID'S)
						OTFNamedChannelSensor so = (OTFNamedChannelSensor) s;
						readoutResources.add(so.getName());
					}
					else if (s instanceof OTFScalerChannelSensor){
						OTFScalerChannelSensor so = (OTFScalerChannelSensor) s;
						readoutResources.add("SCALER"+so.getIndex());
						scalerIndices.put(c, new CrlogicDeltaDataFilter());
					}
					else if (s instanceof MillisecondTimestampSensor){
						readoutResources.add("TIMESTAMP");
					}
					else{
						throw new IllegalArgumentException("Sensor type "+s.getClass()+" is not supported by this loop");
					}
					c++;
			}
			
			// Workaround - somehow one has to add an empty thing to the value otherwise the c logic 
			// does not pick up the end
			readoutResources.add("");
		}
		catch(Exception e){
			throw new RuntimeException("Unable to prepare crloop: ",e);
		}
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.ActionLoop#cleanup()
	 */
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.ActionLoop#getPreActions()
	 */
	@Override
	public List<Action> getPreActions() {
		return preActions;
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.ActionLoop#getPostActions()
	 */
	@Override
	public List<Action> getPostActions() {
		return postActions;
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.ActionLoop#isDataGroup()
	 */
	@Override
	public boolean isDataGroup() {
		// TODO Auto-generated method stub
		return dataGroup;
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.ActionLoop#setDataGroup(boolean)
	 */
	@Override
	public void setDataGroup(boolean dataGroup) {
		this.dataGroup = dataGroup;
	}
	
	/**
	 * @return the sensors
	 */
	public List<Sensor> getSensors() {
		return sensors;
	}
	
	/**
	 * @return the actor
	 */
	public OTFActuator getActor() {
		return actuator;
	}

	/**
	 * @param actor the actor to set
	 */
	public void setActor(OTFActuator actor) {
		this.actuator = actor;
	}

	/**
	 * The structure of the data message depends on the sensors registered at this loop 
	 * at the time this method is called.
	 * @return the data queue and the metadata of the data messages
	 */
	public DataQueue getDataQueue() {
		DataMessageMetadata m = new DataMessageMetadata();
		
		// Build up data message metadata based on the sensors currently registered.
		m.getComponents().add(new ComponentMetadata(actuator.getId()));
		for(Sensor s: sensors){
			m.getComponents().add(new ComponentMetadata(s.getId()));
		}
		return new DataQueue(dataQueue, m);
	}
	
}

