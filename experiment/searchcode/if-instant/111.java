package org.dftproject.lineagelinkage.adapter;

import java.io.Serializable;

import edu.byu.cs428.twenty_gen.datamodel.pedigree.view.interfaces.IDate;
import org.dftproject.genesis.data.genealogy.IInstant;
import org.dftproject.genesis.data.genealogy.impl.InstantImpl;

public class GenDate implements Serializable, IDate {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4863092853258495975L;

	private int day;
	private int month;
	private int year;

	public GenDate(IInstant instant) {
		if (instant == null) {
			day = 0;
			month = 0;
			year = 0;
		} else if (instant instanceof InstantImpl) {
			InstantImpl impl = (InstantImpl) instant;
			day = (impl.getDay() == null) ? 0 : impl.getDay();
			month = (impl.getMonth() == null) ? 0 : impl.getMonth();
			year = (impl.getYear() == null) ? 0 : impl.getYear();
		} else {
			throw new UnsupportedOperationException(
					"Temporarily must be InstantImpl");
		}
	}

	public int getDay() {
		return day;
	}

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}

	public String toString() {
		String s = "";
		s += (day != 0) ? day + " " : "";
		s += (month != 0) ? month + " " : "";
		s += (year != 0) ? year + "\n" : "";
		return s;
	}

	/***************************************************************************
	 * Unsupported Operations:
	 */

	public void setDay(int day) {
		throw new UnsupportedOperationException();
	}

	public void setMonth(int month) {
		throw new UnsupportedOperationException();
	}

	public void setYear(int year) {
		throw new UnsupportedOperationException();
	}
}

