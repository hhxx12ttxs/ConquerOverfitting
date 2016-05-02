package net.rfactor.livescoring.client.endurance.impl;

import net.rfactor.livescoring.client.endurance.Penalty;

public class PenaltyImpl implements Penalty {
	private final Type m_type;
	private final String m_description;
	private final String m_reason;
	private final double m_time;
	private final double m_timeout;
	private boolean m_isResolved;
	// TODO add time and other info about when it was resolved
	private String m_clearReason; // If cleared by racecontrol, this holds the entered reason (if any)
	private double m_clearTime;	  // If cleared by racecontrol, this holds the time when the penalty has been cleared
	private String m_clearUser;   // This holds the username of the user who cleared the penalty
	private final String m_vehicleName; // used to create a back reference to the car that this penalty belongs to
	private Object m_data; // penalty type specific data, if any
	
	public PenaltyImpl(Type type, String description, String reason, double time, double timeout, String vehicleName) {
		m_type = type;
		m_description = description;
		m_reason = reason;
		m_time = time;
		m_timeout = timeout;
		m_isResolved = false;
		m_vehicleName = vehicleName;
	}

	public boolean isResolved() {
		return m_isResolved;
	}
	
	public void setResolved(boolean resolved) {
		m_isResolved = resolved;
	}

	public Type getType() {
		return m_type;
	}

	public String getDescription() {
		return m_description;
	}

	public String getReason() {
		return m_reason;
	}

	public double getTime() {
		return m_time;
	}

	public double getTimeout() {
		return m_timeout;
	}
	
	public void clearPenalty(String reason, double time, String user){
		m_clearReason = reason;
		m_clearTime = time;
		m_clearUser = user;
		m_isResolved = true;
	}
	
	public String getClearReason() {
		return m_clearReason;
	}

	public double getClearTime() {
		return m_clearTime;
	}
	
	public String getClearUser() {
		return m_clearUser;
	}

	public String getVehicleName() {
		return m_vehicleName;
	}
	
	public Object getData() {
		return m_data;
	}
	
	public void setData(Object data) {
		m_data = data;
	}
	
	@Override
	public String toString() {
		// return some readable string representation of this penalty
		return "" + m_type + " " + m_description + " " + m_reason;
	}
}

