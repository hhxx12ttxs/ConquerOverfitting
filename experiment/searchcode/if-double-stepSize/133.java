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

package ch.psi.fda.core.loops;

import gov.aps.jca.CAException;

import java.util.List;

import ch.psi.jcae.annotation.CaChannel;
import ch.psi.jcae.ChannelBean;

/**
 * Bean holding all OTF channels and functionality
 * @author ebner
 *
 */
public class OTFBean {

	public enum Status { SETUP, INACTIVE, INITIALIZE, ACTIVE, STOP, FAULT, ERROR };
	public enum Command { NONE, START, STOP };
	
	private long timeoutMotorOk = 8000;
	private long commandTimeout = 20000; // Maximum time until a command should take effect

	@CaChannel(type=String.class, name =":UMOT")
	private ChannelBean<String> motor;
	
	@CaChannel(type=String.class, name=":MENC")
	private ChannelBean<String> encoder;
	
	@CaChannel(type=Double.class, name=":UBEG")
	private ChannelBean<Double> begin;
	
	@CaChannel(type=Double.class, name=":UBEG.DRVL")
	private ChannelBean<Double> beginMin;
	
	@CaChannel(type=Double.class, name=":UBEG.DRVH")
	private ChannelBean<Double> beginMax;
	
	@CaChannel(type=Double.class, name=":UEND")
	private ChannelBean<Double> end;
	
	@CaChannel(type=Double.class, name=":UEND.DRVL")
	private ChannelBean<Double> endMin;
	
	@CaChannel(type=Double.class, name=":UEND.DRVH")
	private ChannelBean<Double> endMax;
	
	@CaChannel(type=Double.class, name=":USSIZ")
	private ChannelBean<Double> stepSize;
	
	@CaChannel(type=Double.class, name=":USSIZ.DRVL")
	private ChannelBean<Double> stepSizeMin;
	
	@CaChannel(type=Double.class, name=":UITIM")
	private ChannelBean<Double> integrationTime;
	
	@CaChannel(type=Double.class, name=":UITIM.DRVL")
	private ChannelBean<Double> integrationTimeMin;
	
	@CaChannel(type=Double.class, name=":UITIM.DRVH")
	private ChannelBean<Double> integrationTimeMax;
	
	@CaChannel(type=Double.class, name=":UBCL")
	private ChannelBean<Double> userBacklash;
	
	@CaChannel(type=String.class, name=":NFSSE")
	private ChannelBean<String> nfsServer;
	
	@CaChannel(type=String.class, name=":NFSSH")
	private ChannelBean<String> nfsShare;
	
	@CaChannel(type=String.class, name=":DFNAM")
	private ChannelBean<String> fileName;
	
	@CaChannel(type=String.class, name=":FFORM")
	private ChannelBean<String> fileNameFormat;
	
	@CaChannel(type=Integer.class, name=":FCNT")
	private ChannelBean<Integer> fileCount;
	
	@CaChannel(type=Integer.class, name=":FCNT.B")
	private ChannelBean<Integer> resetFileCounter;
	
	@CaChannel(type=Boolean.class, name=":FAPPE")
	private ChannelBean<Boolean> appendFile;

	@CaChannel(type=Boolean.class, name=":FUSE")
	private ChannelBean<Boolean> fileNameGeneration;
	
	@CaChannel(type=Boolean.class, name=":UZIGZ")
	private ChannelBean<Boolean> zigZag;
	
	@CaChannel(type=Integer.class, name=":UCOM")
	private ChannelBean<Integer> command;
	
	@CaChannel(type=Boolean.class, name=":SCRU", monitor=true)
	private ChannelBean<Boolean> scanRunning;
	
	@CaChannel(type=Boolean.class, name=":MUENC")
	private ChannelBean<Boolean> useEncoder;
	
	@CaChannel(type=String.class, name={":CTM0",":CTM1",":CTM2",":CTM3",":CTM4",":CTM5",":CTM6",":CTM7"})
	private List<ChannelBean<String>> monitoredChannels;
	
	@CaChannel(type=Boolean.class, name=":OTF", monitor=true)
	private ChannelBean<Boolean> running;
	
	@CaChannel(type=Integer.class, name=":USTAT", monitor=true)
	private ChannelBean<Integer> status;
	
	@CaChannel(type=Boolean.class, name=":MOK", monitor=true)
	private ChannelBean<Boolean> motorOk;
	
	@CaChannel(type=Boolean.class, name=":EOK", monitor=true)
	private ChannelBean<Boolean> encoderOk;
	
	@CaChannel(type=String.class, name=":MSG")
	private ChannelBean<String> message;

	/**
	 * Get the trigger name that can be used by the sscan record to trigger an OTFScan
	 * @return	Name of the trigger that can be used by sscan record
	 */
	public String getSScanTriggerName(){
		return(running.getName());
	}

	/**
	 * Start OTF scan
	 */
	public void start() {
		try{
		if(getStatus().equals(Status.INACTIVE)){
			
			// Send START command
			this.command.setValue(Command.START.ordinal());
			
			// Wait until OtF logic is active
			this.scanRunning.waitForValue(true, commandTimeout);
		}
		else{
			throw new RuntimeException("Cannot start scan, status is not INACTIVE.\nThe current status is: "+getStatus()+" . The OTF logic need to be recovered manually [Message: "+getMessage()+"]");
		}
		}
		catch(Exception e){
			throw new RuntimeException("Unable to start OTF scan.",e);
		}
	}
	
	/**
	 * Abort scan
	 */
	public void abort() {
		try{
			// Send STOP command
			this.command.setValue(Command.STOP.ordinal());
	
			// Do not wait for put to return
			this.running.setValueNoWait(false);
		}
		catch(Exception e){
			throw new RuntimeException("Unable to abort OTF logic" ,e);
		}
	}
	
	/**
	 * Stop OTF scan
	 * @throws Exception
	 */
	public void stop() throws Exception{
		if(!getStatus().equals(Status.INACTIVE) || !getStatus().equals(Status.FAULT)){
			
			// Send STOP command
			this.command.setValue(Command.STOP.ordinal());

			// Wait until logic is stopped
			status.waitForValue(Status.INACTIVE.ordinal(), commandTimeout);
		}
		
	}

	/**
	 * Wait until scan has stopped
	 * @throws InterruptedException 
	 */
	public void waitUntilStopped() throws InterruptedException {
		try {
			scanRunning.waitForValue(false); // Use of default wait timeout
			
			// Check whether the status is INACTIVE otherwise get messge from OTF and throw an exception
			if(status.getValue() != Status.INACTIVE.ordinal()){
				throw new RuntimeException("OTFSCAN failed with message: "+message.getValue());
			}
			
		} catch (CAException e) {
			throw new RuntimeException("An error occurred while waiting for the OTF logic to finish.", e);
		}
	}
	
	/**
	 * Wait until scan has stopped
	 * @param waitTimeout
	 * @throws InterruptedException 
	 */
	public void waitUntilStopped(Long waitTimeout) throws InterruptedException {
		try {
			scanRunning.waitForValue(false, waitTimeout);
			
			// Check whether the status is INACTIVE otherwise get messge from OTF and throw an exception
			if(status.getValue() != Status.INACTIVE.ordinal()){
				throw new RuntimeException("OTFSCAN failed with message: "+message.getValue());
			}
			
		} catch (CAException e) {
			throw new RuntimeException("An error occurred while waiting for the OTF logic to finish.", e);
		}
		
		
		
	}
	
	/**
	 * Reset OTFScan records to defaults
	 * @throws CAException
	 * @throws InterruptedException 
	 */
	public void resetToDefaults() throws CAException, InterruptedException{
		setMonitoredChannels(new String[]{});
		setMotor("");
		begin.setValue(0d);
		end.setValue(0d);
		stepSize.setValue(0d);
		integrationTime.setValue(0d);
		zigZag.setValue(false);
		setAppendFile(false);
		setFileNameGeneration(true);
		setFileName("");
		setFileNameFormat("%06d.txt");
		resetFileCounter();
		
		setUserBacklash(0d);
		
//		setNfsServer("");
//		setNfsShare("");
		
		waitUntilMotorNotOk(timeoutMotorOk);
	}

	/**
	 * Get motor of the OTFScan axis
	 * @return	Name of the OTF motor
	 * @throws CAException
	 */
	public String getMotor() throws CAException, InterruptedException {
		return(this.motor.getValue());
	}
	
	/**
	 * Set motor of the OTFScan axis
	 * @param motor
	 * @throws CAException
	 */
	public void setMotor(String motor) throws CAException, InterruptedException {
		this.motor.setValue(motor);
	}
	
	/**
	 * Get encoder of the OTFScan axis
	 * @return	Name of the used encoder
	 * @throws CAException
	 */
	public String getEncoder() throws CAException, InterruptedException {
		return(this.encoder.getValue());
	}
	
	/**
	 * Set encoder to use of the OTFScan axis
	 * @param encoder
	 * @throws CAException
	 */
	public void setEncoder(String encoder) throws CAException, InterruptedException {
		this.encoder.setValue(encoder);
	}
	
	/**
	 * Get begin position of the scan
	 * @return Begin position scan
	 * @throws CAException
	 */
	public Double getBegin() throws CAException, InterruptedException {
		return(this.begin.getValue());
	}
	
	/**
	 * Set begin position of scan
	 * @param begin
	 * @throws Exception
	 */
	public void setBegin(Double begin) throws CAException, InterruptedException {
		
		if(begin==null){
			throw new IllegalArgumentException("Begin position must not be null");
		}
		
		if(begin < beginMin.getValue() || begin > beginMax.getValue()){
			throw new IllegalArgumentException("Cannot set begin value to "+begin+ ". Value is outside range [min: "+beginMin.getValue()+" max: "+beginMax.getValue()+"]");
		}
		
		this.begin.setValue(begin);
	}
	
	/**
	 * Get minimum value of the begin position
	 * @return	Min value for begin
	 * @throws CAException
	 */
	public Double getMinBegin() throws CAException, InterruptedException {
		return(this.beginMin.getValue());
	}
	
	/**
	 * Get maximum value of the begin position
	 * @return	Max value for begin
	 * @throws CAException
	 */
	public Double getMaxBegin() throws CAException, InterruptedException {
		return(this.beginMax.getValue());
	}
	
	/**
	 * Get end position of the scan
	 * @return End position scan
	 * @throws CAException
	 */
	public Double getEnd() throws CAException, InterruptedException {
		return(this.end.getValue());
	}
	
	/**
	 * Set end positon of scan
	 * @param end
	 * @throws CAException
	 */
	public void setEnd(Double end) throws CAException, InterruptedException {
		
		if(end==null){
			throw new IllegalArgumentException("End position must not be null");
		}
		
		if(end < endMin.getValue() || end > endMax.getValue()){
			throw new IllegalArgumentException("Cannot set end value to "+end+ ". Value is outside range [min: "+endMin.getValue()+" max: "+endMax.getValue()+"]");
		}
		
		this.end.setValue(end);
	}
	
	/**
	 * Get minimum value of end position
	 * @return	Min value for end
	 * @throws CAException
	 */
	public Double getMinEnd() throws CAException, InterruptedException {
		return(this.endMin.getValue());
	}
	/**
	 * Get maximum value of end position
	 * @return 	Max value for end
	 * @throws CAException
	 */
	public Double getMaxEnd() throws CAException, InterruptedException {
		return(this.endMax.getValue());
	}
	
	/**
	 * Get scan step size
	 * @return Step size
	 * @throws CAException
	 */
	public Double getStepSize() throws CAException, InterruptedException {
		return(this.stepSize.getValue());
	}
	
	/**
	 * Set step size of scan
	 * @param stepSize
	 * @throws CAException
	 */
	public void setStepSize(Double stepSize) throws CAException, InterruptedException {

		if(integrationTime==null){
			throw new IllegalArgumentException("Step size must not be null");
		}
		
		// Check if step size is greater than min step size
		if(stepSizeMin.getValue() != 0 && stepSize < stepSizeMin.getValue()){
			throw new IllegalArgumentException("Step size value ["+stepSize+"] is less than minimum step size ["+stepSizeMin.getValue()+"]!");
		}
		
		this.stepSize.setValue(stepSize);

		// TODO WORKAROUND - Wait to "ensure" that step size related fields are updated (i.e. min/max integration time)
		Thread.sleep(1);
	}
	
	/**
	 * Get minimum integration time
	 * @return	Min value for step size
	 * @throws CAException
	 */
	public double getMinStepSize() throws CAException, InterruptedException {
		return(this.stepSizeMin.getValue());
	}
	
	/**
	 * Get scan integration time (time that is spend in one step)
	 * @return	Integration time
	 * @throws CAException
	 */
	public Double getIntegrationTime() throws CAException, InterruptedException {
		return(this.integrationTime.getValue());
	}

	/**
	 * Set integration time of scan
	 * @param integrationTime
	 * @throws CAException
	 */
	public void setIntegrationTime(Double integrationTime) throws CAException, InterruptedException {
		
		if(integrationTime==null){
			throw new IllegalArgumentException("Integration time must not be null");
		}
		
		// Check range (if limit is set to 0 then limit is not set)
		double min = integrationTimeMin.getValue();
		double max = integrationTimeMax.getValue();
		if(min!= 0 && max!= 0){
			if(integrationTime < min || integrationTime > max){
				throw new IllegalArgumentException("Integration time ["+integrationTime+"] is outside range [min: "+min+" max: "+max+"]");
			}
		}
		else {
			if(min!= 0 && integrationTime<min){
				throw new IllegalArgumentException("Integration time ["+integrationTime+"] is outside range [min: "+min+" max: - ]");
			}
			else if(max!= 0 && integrationTime>max){
				throw new IllegalArgumentException("Integration time ["+integrationTime+"] is outside range [min: -  max: "+max+"]");
			}
		}
		
		this.integrationTime.setValue(integrationTime);
	}
	
	/**
	 * Get minimum integration time
	 * @return	Min value for integration time
	 * @throws CAException
	 */
	public Double getMinIntegrationTime() throws CAException, InterruptedException {
		return(this.integrationTimeMin.getValue());
	}
	/**
	 * Get maximum integration time
	 * @return	Max value for integration time
	 * @throws CAException
	 */
	public Double getMaxIntegrationTime() throws CAException, InterruptedException {
		return(this.integrationTimeMax.getValue());
	}
	
	/**
	 * Get additional user defined backlash
	 * @return	User backlash
	 * @throws CAException 
	 */
	public Double getUserBacklash() throws CAException, InterruptedException {
		return(this.userBacklash.getValue());
	}

	/**
	 * Set additional user defined backlash
	 * @param userBacklash
	 * @throws CAException
	 */
	public void setUserBacklash(Double userBacklash) throws CAException, InterruptedException {
		if(userBacklash==null){
			throw new IllegalArgumentException("User backlash must not be null");
		}
		
		this.userBacklash.setValue(userBacklash);
	}
	
	/**
	 * Get the current NFS server the data is written to
	 * @return	Name of NFS server
	 * @throws CAException
	 */
	public String getNfsServer() throws CAException, InterruptedException {
		return(this.nfsServer.getValue());
	}
	
	/**
	 * Set name of the NFS server the data is written to
	 * @param nfsServer
	 * @throws CAException
	 */
	public void setNfsServer(String nfsServer) throws CAException, InterruptedException {
		this.nfsServer.setValue(nfsServer);
	}
	
	/**
	 * Get the NFS share the data is written to
	 * @return	Name of NFS share
	 * @throws CAException
	 */
	public String getNfsShare() throws CAException, InterruptedException {
		return(this.nfsShare.getValue());
	}
	
	/**
	 * Set name of the NFS share the data is written to
	 * @param nfsShare
	 * @throws CAException
	 */
	public void setNfsShare(String nfsShare) throws CAException, InterruptedException {
		this.nfsShare.setValue(nfsShare);
	}
	
	/**
	 * Get the name of the data file
	 * @return	Name of data file name
	 * @throws CAException
	 */
	public String getFileName() throws CAException, InterruptedException {
		return(this.fileName.getValue());
	}

	/**
	 * Set name of the data file
	 * @param filename
	 * @throws CAException
	 */
	public void setFileName(String filename) throws CAException, InterruptedException {
		this.fileName.setValue(filename);
	}
	
	/**
	 * Get File name formate
	 * @return	Get format for file name
	 * @throws CAException
	 */
	public String getFileNameFormat() throws CAException, InterruptedException {
		return(this.fileNameFormat.getValue());
	}
	
	/**
	 * Set file name formate of the data file
	 * @param fileNameFormat
	 * @throws Exception
	 */
	public void setFileNameFormat(String fileNameFormat) throws CAException, InterruptedException {
		this.fileNameFormat.setValue(fileNameFormat);
	}
	
	/**
	 * Get value of the IOC based file name counter
	 * @return	File counter
	 * @throws CAException
	 */
	public int getFileCounter() throws CAException, InterruptedException {
		return(this.fileCount.getValue());
	}
	
	/**
	 * Reset the IOC based file counter
	 * @throws CAException
	 */
	public void resetFileCounter() throws CAException, InterruptedException {
		this.resetFileCounter.setValue(1);
	}
	
	/**
	 * Get if append file option is activated
	 * @return	Append file flag
	 * @throws CAException
	 */
	public boolean isAppendFile() throws CAException, InterruptedException {
		return(this.appendFile.getValue());
	}
	
	/**
	 * Set whether to append the specified file if the file exists
	 * @param append
	 * @throws CAException
	 */
	public void setAppendFile(boolean append) throws CAException, InterruptedException {
		this.appendFile.setValue(append);
	}
	
	/**
	 * Get if file name generation is on or off
	 * @return	File name generation flag
	 * @throws CAException
	 */
	public boolean isFileNameGeneration() throws CAException, InterruptedException {
		return(this.fileNameGeneration.getValue());
	}
	
	/**
	 * Set Whether the file name should be generated out of the file name format and the file counter
	 * @param generation
	 * @throws CAException
	 */
	public void setFileNameGeneration(boolean generation) throws CAException, InterruptedException {
		this.fileNameGeneration.setValue(generation);
	}
	
	/**
	 * Get if ZigZag scan option is on or off
	 * @return	ZigZag flag
	 * @throws CAException
	 */
	public boolean isZigZag() throws CAException, InterruptedException {
		return(this.zigZag.getValue());
	}
	
	/**
	 * Set ZigZag scan mode on/off
	 * @param zigZag	ZigZag mode on = true, ZigZag mode off = false
	 * @throws CAException
	 */
	public void setZigZag(boolean zigZag) throws CAException, InterruptedException {
		this.zigZag.setValue(zigZag);
	}
	
	/**
	 * Get whether encoder is used
	 */
	public boolean isUseEncoder() throws CAException, InterruptedException {
		return(this.useEncoder.getValue());
	}
	
	/**
	 * Set flag to use encoder
	 * @throws CAException
	 */
	public void setUseEncoder(boolean flag) throws CAException, InterruptedException {
		this.useEncoder.setValue(flag);
	}
	
	/**
	 * Get the channels that are currently monitored by the OTFScan logic
	 * @return	Names of the monitored channels
	 * @throws CAException
	 */
	public String[] getMonitoredChannels() throws CAException, InterruptedException {
		String[] values = new String[this.monitoredChannels.size()];
		
		for(int i=0; i<this.monitoredChannels.size();i++){
			values[i] = monitoredChannels.get(i).getValue();
		}
		
		return(values);
	}
	
	/**
	 * Set the channels that need to be monitored.
	 * Note: As OTF only supports 8 channels to be monitored, only the first 8 
	 * values of the passed channelNames are considered.
	 * @param values	Array of channel names to be monitored
	 * @throws CAException
	 */
	public void setMonitoredChannels(String[] values) throws CAException, InterruptedException {
		
		if(values.length>monitoredChannels.size()){
			throw new IllegalArgumentException("Only up to "+monitoredChannels.size()+" monitored channels are supported by OTF");
		}
		
		for(int i=0; i<this.monitoredChannels.size(); i++){
			if(values != null && i<values.length){
				this.monitoredChannels.get(i).setValue(values[i]);
			}
			else{
				this.monitoredChannels.get(i).setValue("");
			}
		}
	}
	
	/**
	 * Returns whether an scan is running
	 * @return	Running flag
	 * @throws CAException 
	 */
	public boolean isRunning() throws CAException, InterruptedException {
		return(running.getValue());
	}
	
	/**
	 * Get status of the scan
	 * @return Status of the scan
	 * @throws CAException 
	 */
	public Status getStatus() throws CAException, InterruptedException {
		return(Status.values()[this.status.getValue()]);
	}
	
	/**
	 * Get the (error) message from the OTF records
	 * @return	Message from OTF C logic
	 * @throws CAException
	 */
	public String getMessage() throws CAException, InterruptedException {
		return(message.getValue());
	}
	
	/**
	 * Check whether the specified motor is recognized as ok (i.e. it is registered as OTFScan motor)
	 * @return	Flag whether motor is ok
	 * @throws CAException 
	 */
	public boolean isMotorOk() throws CAException, InterruptedException {
		return(motorOk.getValue());
	}
	
	/**
	 * Wait until the motor flag goes to ok
	 * @param timeout	Timout in milliseconds
	 * 
	 * @throws CAException	If motor ok flag does not switch to ok within the specified timeout
	 */
	public void waitUntilMotorOk(long timeout) throws CAException, InterruptedException {
		motorOk.waitForValue(true, timeout);
	}
	
	/**
	 * Wait until the motor flag goes to not ok
	 * @param timeout	Timout in milliseconds
	 * 
	 * @throws CAException	If motor ok flag does not switch to ok within the specified timeout
	 */
	public void waitUntilMotorNotOk(long timeout) throws CAException, InterruptedException {
		motorOk.waitForValue(false, timeout);
	}
	
	
	public void waitUntilEncoderOk(long timeout) throws CAException, InterruptedException {
		if(!useEncoder.getValue()){
			return;
		}
		encoderOk.waitForValue(true, timeout);
	}
}

