package org.dftproject.lineagelinkage.adapter;

import java.io.Serializable;

import org.dftproject.genesis.data.genealogy.IInstant;
import org.dftproject.genesis.data.genealogy.IPlace;

import edu.byu.cs428.twenty_gen.datamodel.pedigree.event.IEventRole;
import edu.byu.cs428.twenty_gen.datamodel.pedigree.event.IEventType;
import edu.byu.cs428.twenty_gen.datamodel.pedigree.event.Participant;
import edu.byu.cs428.twenty_gen.datamodel.pedigree.view.interfaces.IDate;
import edu.byu.cs428.twenty_gen.datamodel.pedigree.view.interfaces.IEvent;
import edu.byu.cs428.twenty_gen.datamodel.pedigree.view.interfaces.ILocation;
import edu.byu.cs428.twenty_gen.utils.MultiMap;

public class GenEvent implements Serializable, IEvent {

	private static final long serialVersionUID = 1390538226524547093L;

	private IDate date;
	private ILocation location;

	public GenEvent(IInstant instant, IPlace place) {
		if (instant != null) {
			date = new GenDate(instant);
		}
		if (place != null && place.toString() != null
				&& place.toString().length() > 0) {
			location = new GenLocation(place);
		}
	}

	public IDate getDate() {
		return date;
	}

	public ILocation getLocation() {
		return location;
	}

	/***************************************************************************
	 * UNSUPPORTED
	 */

	public IEventType getEventType() {
		throw new UnsupportedOperationException();
	}

	public MultiMap<IEventRole, Participant> getParticipants() {
		throw new UnsupportedOperationException();
	}

	public void setDate(IDate date) {
		throw new UnsupportedOperationException();

	}

	public void setEventType(IEventType eventType) {
		throw new UnsupportedOperationException();

	}

	public void setLocation(ILocation location) {
		throw new UnsupportedOperationException();

	}

	public void setParticipant(Participant participant) {
		throw new UnsupportedOperationException();
	}
}

