/*
Part of the proMIDI lib - http://texone.org/promidi

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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

/**
 * Note represents a MIDI note. It has a MIDI port, a MIDI channel, a pitch and a velocity. You can receive Notes from
 * MIDI inputs and send them to MIDI outputs.
 */
public class CCNote extends CCMidiEvent {
	private int _myCommand = ShortMessage.NOTE_ON;

	/**
	 * the length of the note in milliSeconds
	 */
	private int _myLength;

	/**
	 * Initializes a new Note object. You can build a Note to send it to a MIDI output.
	 * 
	 * @param thePitch pitch of a note
	 * @param theVelocity velocity of a note
	 * @param theLength length of the note in milliseconds
	 */
	public CCNote(final int thePitch, final int theVelocity, final int theLength) {
		super(NOTE_ON, thePitch, theVelocity);
		_myLength = theLength;
	}

	public CCNote(final int thePitch, final int theVelocity) {
		this(thePitch, theVelocity, 0);
	}

	/**
	 * Initializes a new Note from a java ShortMessage
	 * 
	 * @param theShortMessage
	 * @invisible
	 */
	CCNote(final ShortMessage theShortMessage) {
		super(theShortMessage);
		_myLength = 0;
	}

	/**
	 * Use this method to get the pitch of a note.
	 * 
	 * @return the pitch of a note
	 */
	public int pitch() {
		return data1();
	}

	/**
	 * Use this method to set the pitch of a note
	 * 
	 * @param pitch new pitch for the note
	 */
	public void pitch(final int thePitch) {
		data1(thePitch);
	}

	/**
	 * Use this method to get the velocity of a note.
	 * 
	 * @return the velocity of a note
	 */
	public int velocity() {
		return data2();
	}

	/**
	 * Use this method to set the velocity of a note.
	 * 
	 * @param velocity new velocity for the note
	 */
	public void velocity(final int theVelocity) {
		data2(theVelocity);
	}

	/**
	 * Returns the length of the note in milliseconds
	 * 
	 * @return the length of the note
	 * @related Note
	 */
	public int length() {
		return _myLength;
	}

	/**
	 * Sets the length of the note
	 * 
	 * @param theLength new length of the note
	 */
	public void length(final int theLength) {
		_myLength = theLength;
	}

	/**
	 * Internal Method to set this note to send the note off command
	 */
	void setToNoteOff() {
		try {
			_myCommand = ShortMessage.NOTE_OFF;
			setMessage(_myCommand, getChannel(), getData1(), getData2());
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	void setToNoteOn() {
		try {
			_myCommand = ShortMessage.NOTE_ON;
			setMessage(_myCommand, getChannel(), getData1(), getData2());
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
}

