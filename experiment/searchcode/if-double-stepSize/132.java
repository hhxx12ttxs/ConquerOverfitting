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

package ch.psi.fda.core.actors;

import gov.aps.jca.CAException;

import java.util.logging.Logger;

import ch.psi.fda.core.Actor;
import ch.psi.fda.core.EngineConfiguration;
import ch.psi.jcae.ChannelBean;
import ch.psi.jcae.ChannelBeanFactory;

/**
 * This actuator sets an Channel Access channel from a start to an end value by doing discrete steps.
 * @author ebner
 *
 */
public class ChannelAccessLinearActuator<T> implements Actor {
	
	// Get Logger
	private static Logger logger = Logger.getLogger(ChannelAccessLinearActuator.class.getName());

	private boolean asynchronous = false;
	
	/**
	 * Start value of the actuator
	 */
	private double start;
	
	/**
	 * End value of the actuator
	 */
	private double end;
	
	/**
	 * Step size of the move
	 */
	private double stepSize;
	
	/**
	 * Move direction (start&lt;end = 1, start&gt;end = -1)
	 */
	private int direction;
	
	/**
	 * Execution count of actuator. This variable is used to minimize the floating point
	 * rounding errors for calculating the next step. 
	 */
	private int count;
	
	/**
	 * Flag that indicates whether there is a next set value for the Actor
	 */
	private boolean next;
	
	/**
	 * Value to set at next @see ch.psi.fda.engine.Actor#set() call
	 */
	private double value;
	
	/**
	 * Level of accuracy the positioner need to have (e.g. if a positioner is set to 1 the readback set value
	 * of the positioner need to have at lease 1+/-accuracy) 
	 * Default is stepSize/2 
	 */
	private double accuracy;
	
	/**
	 * Channel Access channel of the actuator
	 */
	private ChannelBean<Double> channel;
	
	/**
	 * Channel Access channel of the actuator
	 */
	private ChannelBean<T> doneChannel = null;
	
	private final T doneValue;
	private final long doneDelay;
	
	private final double originalStart;
	private final double originalEnd;
	private final int originalDirection;
	
	
	/**
	 * Move timeout
	 */
	private Long timeout;
	
	
	/**
	 * Constructor
	 * @param channelName
	 * @param doneChannelName	If null actor will not wait (for this channel) to continue
	 * @param doneValue
	 * @param doneDelay			Delay in seconds before checking the done channel
	 * @param start
	 * @param end
	 * @param stepSize
	 * @param timeout			Maximum move time (in milliseconds)
	 */
	@SuppressWarnings("unchecked")
	public ChannelAccessLinearActuator(String channelName, String doneChannelName, T doneValue, double doneDelay,  double start, double end, double stepSize, Long timeout){
		
		this.doneValue = doneValue;
		this.doneDelay = (long) Math.floor((doneDelay*1000));
		this.start = start;
		this.end = end;
		
		if(stepSize <= 0){
			throw new IllegalArgumentException("Step size ["+stepSize+"] must be > 0");
		}
		this.stepSize = stepSize;
		
		this.accuracy = stepSize/2;
		
		// Validate and save timeout parameter
		if(timeout!=null && timeout<=0){
			throw new IllegalArgumentException("Timeout must be >0 or null");
		}
		else{
			this.timeout = timeout;
		}
		
		
		init();
		
		// Save original settings
		this.originalStart = start;
		this.originalEnd = end;
		this.originalDirection = direction;
		
		
		// Initialize/create Channel Access channel
		try {
			channel = ChannelBeanFactory.getFactory().createChannelBean(Double.class, channelName, false);
		} catch (CAException e) {
			// Convert Exception into unchecked RuntimeException
			throw new IllegalArgumentException("Unable to initialize actuator channel [name:"+channelName+"]",e);
		} catch (InterruptedException e) {
			throw new RuntimeException("Unable to initialize actuator channel [name:"+channelName+"]",e);
		}
		if(doneChannelName != null){
			try {
				doneChannel = ChannelBeanFactory.getFactory().createChannelBean((Class<T>) doneValue.getClass(), doneChannelName, false);
			} catch (CAException e) {
				// Convert Exception into unchecked RuntimeException
				throw new IllegalArgumentException("Unable to initialize actuator channel [name:"+doneChannelName+"]",e);
			} catch (InterruptedException e) {
				throw new RuntimeException("Unable to initialize actuator channel [name:"+doneChannelName+"]",e);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see ch.psi.fda.engine.Actor#set()
	 */
	@Override
	public void set() throws InterruptedException {
		
		// Throw an IllegalStateException in the case that set is called although there is no next step.
		if(!next){
			throw new IllegalStateException("The actuator does not have any next step.");
		}
		
		// Set actuator channel
		logger.finest("Set actuator channel "+channel.getName()+" to value: "+value);
		try {
			
			if(!asynchronous){
				if(timeout==null){
					channel.setValue(value);
				}
				else{
					channel.setValue(value, timeout); 
				}
			}
			else{
				channel.setValueNoWait(value);
			}
			
			if(doneChannel != null){
				Thread.sleep(doneDelay);
				doneChannel.waitForValue(doneValue); 
			}
			
			// Check whether the set value is really on the value that was set before.
			if(EngineConfiguration.getInstance().isCheckActorSet()){
				double c = channel.getValue(true);
				double a = Math.abs( c - value ); 
				if ( a > accuracy ){
					throw new RuntimeException("Actor could not be set to the value "+value+" The readback of the set value does not match the value that was set [value: "+c+" delta: "+a+" accuracy: "+accuracy+"]");
				}
			}
			
		} catch (CAException e) {
			// Convert Exception into unchecked RuntimeException
			throw new RuntimeException("Unable to move actuator [channel: "+channel.getName()+"] to value "+value,e);
		}
		
		
		count++;
		double nextValue = start+(count*stepSize*direction); // Done like this to keep floating point rounding errors minimal
		
		if((direction==1&&nextValue<=end)||(direction==-1&nextValue>=end)){
			logger.fine("Next actor value: "+nextValue);
			value=nextValue;
			this.next = true;
		}
		else{
			// There is no next set value
			this.next = false;
		}
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.engine.Actor#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return next;
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.Actor#init()
	 */
	@Override
	public void init() {
		this.count = 0;
		
		// Determine move direction
		this.direction = 1;
		if(start>end){
			direction=-1; // Move in negative direction
		}
		
		
		// Set first set value to the start value
		this.value = start;
		this.next = true;
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.Actor#reverse()
	 */
	@Override
	public synchronized void reverse() {
		double oldStart = start;
		this.start = this.end;
		this.end = oldStart;
		
		// Determine move direction
		this.direction = 1;
		if(this.start>this.end){
			direction=-1; // Move in negative direction
		}
		
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.Actor#reset()
	 */
	@Override
	public void reset() {
		this.start = this.originalStart;
		this.end = this.originalEnd;
		this.direction = this.originalDirection;
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.Actor#destroy()
	 */
	@Override
	public void destroy() {
		// Destroy channel
		try {
			logger.finest("Destroy actor channel: "+channel.getName());
			channel.destroy();
		} catch (CAException e) {
			throw new RuntimeException("Unable to destroy channel ["+channel.getName()+"]",e);
		}
		
		// Destroy done channel if exists
		if(doneChannel != null){
			try {
				logger.finest("Destroy actor done channel: "+doneChannel.getName());
				doneChannel.destroy();
			} catch (CAException e) {
				throw new RuntimeException("Unable to destroy channel ["+channel.getName()+"]",e);
			}
		}
	}

	/**
	 * @return the asynchronous
	 */
	public boolean isAsynchronous() {
		return asynchronous;
	}

	/**
	 * @param asynchronous the asynchronous to set
	 */
	public void setAsynchronous(boolean asynchronous) {
		this.asynchronous = asynchronous;
	}
	
	

}

