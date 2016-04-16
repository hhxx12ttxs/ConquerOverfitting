/**
 * 
 */
package de.fzi.hiwitool.db.hibernateImp.tables.timesheet;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import de.fzi.hiwitool.db.hibernateImp.tables.workingfield.WorkingField;

/**
 * @author dit (03.08.2012)
 * 
 */
@Entity
@Table(name = "work_record")
public class WorkRecord implements Comparable<WorkRecord> {

	@Id
	@GeneratedValue
	private Long			id;

	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private Date			date;

	@Column(nullable = false)
	private Double			hours;

	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinColumn(nullable = false)
	private WorkingField	workingField;

	@Column(nullable = true)
	private String			note;

//	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//	@JoinColumn(nullable = false, name = "work_attest_id")
//	private WorkAttest		workAttest;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @return the hours
	 */
	public Double getHours() {
		return hours;
	}

	/**
	 * @param hours
	 *            the hours to set
	 */
	public void setHours(Double hours) {
		this.hours = hours;
	}

	/**
	 * @return the workingField
	 */
	public WorkingField getWorkingField() {
		return workingField;
	}

	/**
	 * @param workingField
	 *            the workingField to set
	 */
	public void setWorkingField(WorkingField workingField) {
		this.workingField = workingField;
	}

	/**
	 * @return the note
	 */
	public String getNote() {
		return note;
	}

	/**
	 * @param note
	 *            the note to set
	 */
	public void setNote(String note) {
		this.note = note;
	}

//	/**
//	 * @return the workAttest
//	 */
//	public WorkAttest getWorkAttest() {
//		return workAttest;
//	}
//
//	/**
//	 * @param workAttest
//	 *            the workAttest to set
//	 */
//	public void setWorkAttest(WorkAttest workAttest) {
//		this.workAttest = workAttest;
//	}
//
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((date == null) ? 0 : date.hashCode());
//		result = prime * result + ((hours == null) ? 0 : hours.hashCode());
//		result = prime * result + ((id == null) ? 0 : id.hashCode());
//		result = prime * result + ((note == null) ? 0 : note.hashCode());
//		result = prime * result + ((workAttest == null) ? 0 : workAttest.hashCode());
//		result = prime * result + ((workingField == null) ? 0 : workingField.hashCode());
//		return result;
//	}



	@Override
	public int compareTo(WorkRecord o) {
		int r = date.compareTo(o.date);
		if( r == 0){
			r = 1;
		}
		return r;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((hours == null) ? 0 : hours.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((note == null) ? 0 : note.hashCode());
		result = prime * result
				+ ((workingField == null) ? 0 : workingField.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof WorkRecord))
			return false;
		WorkRecord other = (WorkRecord) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (hours == null) {
			if (other.hours != null)
				return false;
		} else if (!hours.equals(other.hours))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (note == null) {
			if (other.note != null)
				return false;
		} else if (!note.equals(other.note))
			return false;
		if (workingField == null) {
			if (other.workingField != null)
				return false;
		} else if (!workingField.equals(other.workingField))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WorkRecord [" + (id != null ? "id=" + id + ", " : "") + (date != null ? "date=" + date + ", " : "")
				+ (hours != null ? "hours=" + hours + ", " : "")
				+ (workingField != null ? "workingField=" + workingField + ", " : "")
				+ (note != null ? "note=" + note : "") + "]";
	}

}

