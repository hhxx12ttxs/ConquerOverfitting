/**
 * 
 */
package javabuch5;

import java.util.Calendar;
import java.util.GregorianCalendar;
import static java.util.Calendar.*;

/**
 * @author sigma
 * 
 */
public class Geburtstag implements Comparable<Geburtstag> {

	private Calendar geburtsDatum;

	/**
	 * @param datum
	 */
	public Geburtstag(Calendar datum) {
		super();
		this.geburtsDatum = datum;
	}

	public int alter() {
		Calendar today = new GregorianCalendar();
		if (today.get(MONTH) <= geburtsDatum.get(MONTH)
				&& today.get(DAY_OF_MONTH) < geburtsDatum.get(DAY_OF_MONTH))
			return today.get(YEAR) - geburtsDatum.get(YEAR) - 1;

		return today.get(YEAR) - geburtsDatum.get(YEAR);
	}

	public Calendar naechsteGeburtstagsfeier() {
		// declaration of birthday fields
		Calendar today = new GregorianCalendar();
		Calendar thisYear = new GregorianCalendar(today.get(YEAR),
				geburtsDatum.get(MONTH), geburtsDatum.get(DAY_OF_MONTH));
		Calendar nextYear = new GregorianCalendar(today.get(YEAR) + 1,
				geburtsDatum.get(MONTH), geburtsDatum.get(DAY_OF_MONTH));

		if (geburtsDatum.get(MONTH) < today.get(MONTH))
			return nextYear;
		if (geburtsDatum.get(MONTH) == today.get(MONTH)
				&& today.get(DAY_OF_MONTH) > geburtsDatum.get(DAY_OF_MONTH))
			return nextYear;
		return thisYear;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Geburtstag o) {
		return this.naechsteGeburtstagsfeier().compareTo(
				o.naechsteGeburtstagsfeier());
	}

}

