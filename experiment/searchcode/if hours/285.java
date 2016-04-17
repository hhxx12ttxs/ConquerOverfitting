package com.wm.offshore.report.dto.utilization;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.wm.offshore.report.dto.WorkingWeekend;
import com.wm.offshore.util.TimeUtil;

/**
 * @author mmeng
 */
public abstract class WorkingHour implements Cloneable {

	public static final double STANDARD_WORKING_HOURS = 8.0;

	public static final double UTILIZATION_SCORE_FINE = 0.9;

	public static final double UTILIZATION_SCORE_PASS = 0.5;

	// This field is only used for plan working hour calculation.
	/**
	 * 
	 */
	protected int resCount = 1;

	/**
	 * 
	 */
	protected Map<Integer, Integer> vendorResCount = new HashMap<Integer, Integer>();

	protected Date startDate;

	protected Date endDate;

	protected Date logEndDate;

	/**
	 * 
	 */
	protected double projectHours = 0.0;

	/**
	 * 
	 */
	protected double supportHours = 0.0;

	private double trainingHours = 0.0;

	private double offtimeHours = 0.0;

	private double adminHours = 0.0;

	private double vendorHours = 0.0;

	// Non working day hours
	/**
	 * 
	 */
	protected double nonProjectHours = 0.0;

	/**
	 * 
	 */
	protected double nonWorkingHours = 0.0;

	/**
	 * 
	 */
	protected double planWorkingHours = 0.0;

	/**
	 * 
	 */
	protected double totalWorkingHours = 0.0;

	/**
	 * 
	 */
	protected double totalHours = 0.0;

	/**
	 * 
	 */
	protected double utilization = 0.0;

	/**
	 * 
	 */
	protected double projectUtilization = 0.0;

	/**
	 * 
	 */
	protected double supportUtilization = 0.0;

	protected WorkingHour(Date startDate, Date endDate, Date logEndDate) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.logEndDate = logEndDate;
	}

	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * @return
	 * 
	 */
	public abstract String getKey();

	/**
	 * @param key
	 * 
	 */
	public abstract void setKey(String key);

	/**
	 * @return
	 * 
	 */
	public abstract String getName();

	/**
	 * @param name
	 * 
	 */
	public abstract void setName(String name);

	/**
	 * @return
	 * 
	 */
	public double getProjectHours() {
		return projectHours;
	}

	/**
	 * @param projectHours
	 * 
	 */
	public void setProjectHours(double projectHours) {
		this.projectHours = projectHours;
	}

	/**
	 * @return
	 * 
	 */
	public double getNonWorkingHours() {
		return nonWorkingHours;
	}

	/**
	 * @param nonWorkingHours
	 * 
	 */
	public void setNonWorkingHours(double nonWorkingHours) {
		this.nonWorkingHours = nonWorkingHours;
	}

	/**
	 * @param totalWorkingHours
	 * 
	 */
	public void setTotalWorkingHours(double totalWorkingHours) {
		this.totalWorkingHours = totalWorkingHours;
	}

	/**
	 * @return
	 * 
	 */
	public double getTotalHours() {
		return totalHours;
	}

	/**
	 * @param totalHours
	 * 
	 */
	public void setTotalHours(double totalHours) {
		this.totalHours = totalHours;
	}

	public void addWorkHour(double hour) {
		totalHours += hour;
	}

	/**
	 * @return
	 * 
	 */
	public double getTotalWorkingHours() {
		if (this.totalWorkingHours == 0.0) {
			this.totalWorkingHours = this.totalHours - this.nonWorkingHours;
		}
		return totalWorkingHours;
	}

	/**
	 * @return
	 * 
	 */
	public double getUtilization() {
		if (this.utilization == 0.0) {
			if (getPlanWorkingHours() == 0) {
				return 0.0;
			}
			this.utilization = (this.projectHours + this.supportHours)
					/ getPlanWorkingHours() * 100;
		}
		return this.utilization;
	}

	/**
	 * @return
	 * 
	 */
	public double getNonProjectHours() {
		if (this.nonProjectHours == 0.0) {
			this.nonProjectHours = this.getTotalWorkingHours()
					- this.getProjectHours();
		}
		return nonProjectHours;
	}

	/**
	 * @param nonProjectHours
	 * 
	 */
	public void setNonProjectHours(double nonProjectHours) {
		this.nonProjectHours = nonProjectHours;
	}

	/**
	 * @return
	 * 
	 */
	public double getPlanWorkingHours() {
		if (this.planWorkingHours == 0.0) {
			Date eDate = null;
			if (this.endDate.before(logEndDate)) {
				eDate = this.endDate;
			} else {
				eDate = logEndDate;
			}
			this.planWorkingHours = TimeUtil.getWorkingDay(this.startDate,
					eDate)
					* STANDARD_WORKING_HOURS * resCount - this.nonWorkingHours;
			// while calculating the plan working hour, we need add those
			// weekend which is actually working days.
			int daysNumber = 0;
			for (int vendor : this.vendorResCount.keySet()) {
				int days = WorkingWeekend.getWorkingWeekendNumber(vendor,
						this.startDate, this.endDate);
				daysNumber += days * this.vendorResCount.get(vendor);
			}
			this.planWorkingHours += daysNumber * STANDARD_WORKING_HOURS;
		}
		return this.planWorkingHours;
	}

	/**
	 * @return
	 * 
	 */
	public double getSupportHours() {
		return supportHours;
	}

	/**
	 * @param supportHours
	 * 
	 */
	public void setSupportHours(double supportHours) {
		this.supportHours = supportHours;
	}

	/**
	 * @return
	 * 
	 */
	public double getProjectUtilization() {
		if (getPlanWorkingHours() == 0) {
			return 0.0;
		}
		projectUtilization = this.projectHours / getPlanWorkingHours() * 100;
		return projectUtilization;
	}

	/**
	 * @param projectUtilization
	 * 
	 */
	public void setProjectUtilization(double projectUtilization) {
		this.projectUtilization = projectUtilization;
	}

	/**
	 * @return
	 * 
	 */
	public double getSupportUtilization() {
		if (getPlanWorkingHours() == 0) {
			return 0.0;
		}
		supportUtilization = this.supportHours / getPlanWorkingHours() * 100;
		return supportUtilization;
	}

	/**
	 * @param supportUtilization
	 * 
	 */
	public void setSupportUtilization(double supportUtilization) {
		this.supportUtilization = supportUtilization;
	}

	/**
	 * @return
	 * 
	 */
	public int getResCount() {
		return resCount;
	}

	/**
	 * @param resCount
	 * 
	 */
	public void setResCount(int resCount) {
		this.resCount = resCount;
	}

	/**
	 * @return
	 * 
	 */
	public Map<Integer, Integer> getVendorResCount() {
		return vendorResCount;
	}

	/**
	 * @param vendorResCount
	 * 
	 */
	public void setVendorResCount(Map<Integer, Integer> vendorResCount) {
		this.vendorResCount = vendorResCount;
	}

	public double getTrainingHours() {
		return trainingHours;
	}

	public void setTrainingHours(double trainingHours) {
		this.trainingHours = trainingHours;
	}

	public double getOfftimeHours() {
		return offtimeHours;
	}

	public void setOfftimeHours(double offtimeHours) {
		this.offtimeHours = offtimeHours;
	}

	public double getAdminHours() {
		return adminHours;
	}

	public void setAdminHours(double adminHours) {
		this.adminHours = adminHours;
	}

	public double getVendorHours() {
		return vendorHours;
	}

	public void setVendorHours(double vendorHours) {
		this.vendorHours = vendorHours;
	}

	private int headCount;

	public int getHeadCount() {
		if (headCount == 0 && null != vendorResCount) {
			for (int vendor : vendorResCount.keySet()) {
				headCount += vendorResCount.get(vendor);
			}
		}
		return headCount;
	}

	public void setHeadCount(int count) {
		headCount = count;
	}

	public double getTotalReportHour() {
		return this.projectHours + this.supportHours + this.totalHours
				+ this.adminHours + this.offtimeHours + this.vendorHours;
	}
	
	public double getProjectAndSupportHour(){
		return this.projectHours + this.supportHours;
	}

}

