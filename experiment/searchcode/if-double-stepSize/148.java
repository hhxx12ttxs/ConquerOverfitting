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

import ch.psi.fda.core.Actor;

/**
 * Special actuator for the OTFLoop. This type of actor must not be used in any other
 * type of loop than OTFLoop. If it is used it will, depending on the loop, immediately stop the loop
 * as it no single step.
 * @author ebner
 *
 */
public class OTFActuator implements Actor {

	/**
	 * Name of the motor channel
	 */
	private final String name;
	
	/**
	 * Channel name of the encoder;
	 */
	private final String readback;
	
	private  double start;
	private  double end;
	private final double stepSize;
	private final double integrationTime;
	
	private final String id;
	
	/**
	 * Additional backlash for the motor
	 */
	private final double additionalBacklash;
	
	
	public OTFActuator(String id, String name, String readback, double start, double end, double stepSize, double integrationTime, double additionalBacklash){
		this.id = id;
		this.name = name;
		this.readback = readback;
		this.start = start;
		this.end = end;
		this.stepSize = stepSize;
		this.integrationTime = integrationTime;
		this.additionalBacklash = additionalBacklash;
	}
	
	/* (non-Javadoc)
	 * @see ch.psi.fda.core.Actor#set()
	 */
	@Override
	public void set() {
		// Do nothing
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.Actor#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return false;
	}

	
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the readback
	 */
	public String getReadback() {
		return readback;
	}

	/**
	 * @return the start
	 */
	public double getStart() {
		return start;
	}

	/**
	 * @return the end
	 */
	public double getEnd() {
		return end;
	}

	/**
	 * @return the stepSize
	 */
	public double getStepSize() {
		return stepSize;
	}

	/**
	 * @return the integrationTime
	 */
	public double getIntegrationTime() {
		return integrationTime;
	}

	/**
	 * @return the additionalBacklash
	 */
	public double getAdditionalBacklash() {
		return additionalBacklash;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.Actor#init()
	 */
	@Override
	public void init() {
		// Not implemented
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.Actor#reverse()
	 */
	@Override
	public void reverse() {
		// Not implemented
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.Actor#reset()
	 */
	@Override
	public void reset() {
		// Not implemented
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.core.Actor#destroy()
	 */
	@Override
	public void destroy() {
		// Nothing to be done
		
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(double start) {
		this.start = start;
	}

	/**
	 * @param end the end to set
	 */
	public void setEnd(double end) {
		this.end = end;
	}
	
}

