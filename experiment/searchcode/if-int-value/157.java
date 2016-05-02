/*

 Copyright (c) 2005 Christian Riekoff

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General
 Public License along with this library; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 Boston, MA  02111-1307  USA
 */

package cc.creativecomputing.protocol.midi;

import javax.sound.midi.ShortMessage;

/**
 * Controller represents a MIDI controller. It has a number and a value. You can
 * receive Controller values from MIDI ins and send them to MIDI outs.
 */
public class CCController extends CCMidiEvent{

	/**
	 * Initializes a new Controller object.
	 * @param theNumber number of a controller
	 * @param theValue value of a controller
	 */
	public CCController(final int theNumber, final int theValue){
		super(CONTROL_CHANGE, theNumber, theValue);
	}
	
	/**
	 * Initializes a new Note from a java ShortMessage
	 * @param theShortMessage
	 * @invisible
	 */
	CCController(ShortMessage theShortMessage){
		super(theShortMessage);
	}

	/**
	 * Use this method to get the number of a controller.
	 * @return the number of a controller
	 */
	public int number(){
		return data1();
	}

	/**
	 * Use this method to set the number of a controller.
	 * @return the number of a note
	 */
	public void number(final int theNumber){
		data1(theNumber);
	}

	/**
	 * Use this method to get the value of a controller.
	 * @return the value of a note
	 */
	public int value(){
		return data2();
	}

	/**
	 * Use this method to set the value of a controller.
	 * @return the value of a note
	 */
	public void value(final int theValue){
		data2(theValue);
	}
}

