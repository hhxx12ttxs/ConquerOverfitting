package com.br.jty.process.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table
@Entity
public class SLA {
	
	@Id
	@GeneratedValue (strategy=GenerationType.AUTO)
	@Column(name="SLA_ID")
	private Long id;
	@Column
	private Long maxWorkingHoursToDone;
	@Column
	private Date initialDate;
	@Column
	private Date finalDate;
	@Column
	private Long hoursDuetoFinish;
	@Column
	private Long hoursSpentInTask;
	@Column
	private Long hoursOverdue;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getMaxWorkingHoursToDone() {
		return maxWorkingHoursToDone;
	}
	public void setMaxWorkingHoursToDone(Long maxWorkingHoursToDone) {
		this.maxWorkingHoursToDone = maxWorkingHoursToDone;
	}
	public Date getInitialDate() {
		return initialDate;
	}
	public void setInitialDate(Date initialDate) {
		this.initialDate = initialDate;
	}
	public Date getFinalDate() {
		return finalDate;
	}
	public void setFinalDate(Date finalDate) {
		this.finalDate = finalDate;
	}
	public Long getHoursDuetoFinish() {
		return hoursDuetoFinish;
	}
	public void setHoursDuetoFinish(Long hoursDuetoFinish) {
		this.hoursDuetoFinish = hoursDuetoFinish;
	}
	public Long getHoursSpentInTask() {
		return hoursSpentInTask;
	}
	public void setHoursSpentInTask(Long hoursSpentInTask) {
		this.hoursSpentInTask = hoursSpentInTask;
	}
	public Long getHoursOverdue() {
		return hoursOverdue;
	}
	public void setHoursOverdue(Long hoursOverdue) {
		this.hoursOverdue = hoursOverdue;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((finalDate == null) ? 0 : finalDate.hashCode());
		result = prime
				* result
				+ ((hoursDuetoFinish == null) ? 0 : hoursDuetoFinish.hashCode());
		result = prime * result
				+ ((hoursOverdue == null) ? 0 : hoursOverdue.hashCode());
		result = prime
				* result
				+ ((hoursSpentInTask == null) ? 0 : hoursSpentInTask.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((initialDate == null) ? 0 : initialDate.hashCode());
		result = prime
				* result
				+ ((maxWorkingHoursToDone == null) ? 0 : maxWorkingHoursToDone
						.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SLA other = (SLA) obj;
		if (finalDate == null) {
			if (other.finalDate != null)
				return false;
		} else if (!finalDate.equals(other.finalDate))
			return false;
		if (hoursDuetoFinish == null) {
			if (other.hoursDuetoFinish != null)
				return false;
		} else if (!hoursDuetoFinish.equals(other.hoursDuetoFinish))
			return false;
		if (hoursOverdue == null) {
			if (other.hoursOverdue != null)
				return false;
		} else if (!hoursOverdue.equals(other.hoursOverdue))
			return false;
		if (hoursSpentInTask == null) {
			if (other.hoursSpentInTask != null)
				return false;
		} else if (!hoursSpentInTask.equals(other.hoursSpentInTask))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (initialDate == null) {
			if (other.initialDate != null)
				return false;
		} else if (!initialDate.equals(other.initialDate))
			return false;
		if (maxWorkingHoursToDone == null) {
			if (other.maxWorkingHoursToDone != null)
				return false;
		} else if (!maxWorkingHoursToDone.equals(other.maxWorkingHoursToDone))
			return false;
		return true;
	}
	
	
}

