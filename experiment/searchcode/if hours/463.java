package org.pht.user.data;

import java.io.Serializable;
import java.util.Calendar;

// Represents an entry in the user's daily logs
public class DataEntry implements Serializable
{
	private static final long serialVersionUID = 5638248638171687539L;
	private int cardioHours, strengthHours, workHours, sleepHours, 
		systolic, diastolic, restingHeartRate;
	private double bloodSugar;
	private Calendar calendar;
	private String memo;
	
	public static final int CAL = 0, CARDIO_HOURS = 1, STRENGTH_HOURS = 2, WORK_HOURS = 3, SLEEP_HOURS = 4, SYSTOLIC = 5, DIASTOLIC = 6, RESTING_HEART_RATE = 7, BLOOD_SUGAR = 8, MEMO = 9;

	
	public DataEntry() { }
	public DataEntry(Calendar calendar, int cardioHours, int strengthHours, int workHours, 
			int sleepHours, int systolic, int diastolic, int restingHeartRate, 
			double bloodSugar, String memo) {
		this.cardioHours = cardioHours;
		this.strengthHours = strengthHours;
		this.workHours = workHours;
		this.sleepHours = sleepHours;
		this.systolic = systolic;
		this.diastolic = diastolic;
		this.restingHeartRate = restingHeartRate;
		this.bloodSugar = bloodSugar;
		this.calendar = calendar;
		this.memo = memo;
	}
	
	/**
	 * I hope you're getting the right parameters if you use this.
	 * @param name
	 * @return
	 */
	public Object get(int fieldName) {
		switch (fieldName) {
		case CAL:					return this.calendar;
    	case CARDIO_HOURS:			return this.cardioHours;
		case STRENGTH_HOURS: 		return this.strengthHours;
		case WORK_HOURS: 			return this.workHours;
		case SLEEP_HOURS:			return this.sleepHours;
		case SYSTOLIC:				return this.systolic;
		case DIASTOLIC:				return this.diastolic;
		case RESTING_HEART_RATE:	return this.restingHeartRate;
		case BLOOD_SUGAR:			return this.bloodSugar;
		case MEMO:					return this.MEMO;
		default: 					return -0.0;
		}	
	}
	
	public int getSystolic() { return systolic; }
	public int getDiastolic() { return diastolic; }
	public int getRestingHeartRate() { return restingHeartRate; }
	public int getSleepHours() { return sleepHours; }
	public int getStrengthHours() { return strengthHours; }
	public int getCardioHours() { return cardioHours; }
	public int getWorkHours() { return workHours; }
	public double getBloodSugar() { return bloodSugar; }
	public Calendar getCalendar() { return this.calendar; }
	public String getMemo() { return this.memo; }
		
	public void setSystolic(int systolic) { this.systolic = systolic; }
	public void setDiastolic(int diastolic) { this.diastolic = diastolic; }
	public void setRestingHeartRate(int restingHeartRate) { this.restingHeartRate = restingHeartRate; }
	public void setBloodSugar(double bloodSugar) { this.bloodSugar = bloodSugar; }
	public void setCardioHours(int cardioHours) { this.cardioHours = cardioHours; }
	public void setStrengthHours(int strengthHours) { this.strengthHours = strengthHours; }
	public void setSleepHours(int sleepHours) { this.sleepHours = sleepHours; }
	public void setWorkHours(int workHours) { this.workHours = workHours; }
	public void setCalendar(Calendar calendar) { this.calendar = calendar; }
	public void setMemo(String memo) { this.memo = memo; }
	public int compareTo(DataEntry e) { return this.calendar.compareTo(e.getCalendar()); }
	@Override
	public String toString() {
		return "DataEntry [cardioHours=" + cardioHours + ", strengthHours="
				+ strengthHours + ", workHours=" + workHours + ", sleepHours="
				+ sleepHours + ", systolic=" + systolic + ", diastolic="
				+ diastolic + ", restingHeartRate=" + restingHeartRate
				+ ", bloodSugar=" + bloodSugar + ", cal="
				+ calendar + ", getSystolic()=" + getSystolic()
				+ ", getDiastolic()=" + getDiastolic()
				+ ", getRestingHeartRate()=" + getRestingHeartRate()
				+ ", getSleepHours()=" + getSleepHours()
				+ ", getStrengthHours()=" + getStrengthHours()
				+ ", getCardioHours()=" + getCardioHours()
				+ ", getWorkHours()=" + getWorkHours() + ", getBloodSugar()="
				+ getBloodSugar() + ", getCalendar().getTimeInMillis=" + getCalendar().getTimeInMillis() + ", getClass()="
				+ getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}

