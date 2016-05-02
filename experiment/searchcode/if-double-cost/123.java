/*
 * Copyright (C) 2011 Brian Reber
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by Brian Reber.
 * THIS SOFTWARE IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.reber.vehicletracker.client;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.reber.vehicletracker.db.FuelRecord;

/**
 * Computes statistics for the vehicle based on the fuel records
 * 
 * @author breber
 */
public class VehicleStats {
	/**
	 * Comparator used for soriting fuel records by date (oldest to newest)
	 */
	private static final Comparator<FuelRecord> DATE_COMPARATOR = new Comparator<FuelRecord>() {
		@Override
		public int compare(FuelRecord arg0, FuelRecord arg1) {
			return arg0.getDate().compareTo(arg1.getDate());
		}
	};

	/**
	 * The list of fuel records to base our calculations on
	 */
	private List<FuelRecord> fuelRecords;

	/**
	 * A calendar instance so we don't have to keep creating a new one
	 */
	private static Calendar calendarInstance = Calendar.getInstance();

	/**
	 * Creates a new Vehicle Stats with the given vehicle and list of fuel records
	 * 
	 * @param veh
	 * The vehicle to calculate
	 * @param fuelRecords
	 * The list of fuel records to use
	 */
	public VehicleStats(List<FuelRecord> fuelRecords) {
		this.fuelRecords = fuelRecords;
	}

	/**
	 * Gets the total number of miles on the vehicle
	 * 
	 * @return the total number of miles on the car
	 */
	public double getTotalMiles() {
		if (fuelRecords.size() > 0) {
			Collections.sort(fuelRecords, DATE_COMPARATOR);

			return fuelRecords.get(fuelRecords.size() - 1).getOdometer();
		} else {
			return 0;
		}
	}

	/**
	 * Gets the total cost of the vehicle based on the records
	 * 
	 * @return The total cost of all records
	 */
	public double getTotalCost() {
		if (fuelRecords.size() > 0) {
			double totalCost = 0;

			for (FuelRecord rec : fuelRecords) {
				totalCost += rec.getTotalCost();
			}

			return totalCost;
		} else {
			return 0;
		}
	}

	/**
	 * Gets the lifetime average Miles/Gallon
	 * 
	 * @return the lifetime average mpg
	 */
	public double getAverageMpg() {
		if (fuelRecords.size() > 0) {
			double totalGallons = 0;
			double totalMiles = 0;

			Collections.sort(fuelRecords, DATE_COMPARATOR);

			for (FuelRecord rec : fuelRecords) {
				totalGallons += rec.getGallons();
			}

			totalMiles = fuelRecords.get(fuelRecords.size() - 1).getOdometer() - fuelRecords.get(0).getOdometer();

			return totalMiles / totalGallons;
		} else {
			return 0;
		}
	}

	/**
	 * Gets the average cost per year
	 * 
	 * @return the average cost per year
	 */
	public double getCostPerYear() {
		if (fuelRecords.size() > 0) {
			double costPerYear = 0;
			double numYears = 0;
			ArrayList<YearCostWrapper> values = getCosts();

			for (YearCostWrapper val : values) {
				System.out.println(val.year + ": " + val.cost);
				if (val.year != calendarInstance.get(Calendar.YEAR)) {
					costPerYear += val.cost;
					numYears++;
				} else {
					costPerYear += val.cost;
					numYears += (calendarInstance.get(Calendar.MONTH) / 12.0);
				}
			}

			return costPerYear / numYears;
		} else {
			return 0;
		}
	}

	/**
	 * Gets the average monthly cost
	 * 
	 * @return the average monthly cost
	 */
	public double getCostPerMonth() {
		if (fuelRecords.size() > 0) {
			double costPerMonth = 0;
			int numMonths = 0;
			ArrayList<YearCostWrapper> values = getCosts();

			for (YearCostWrapper val : values) {
				if (val.year != calendarInstance.get(Calendar.YEAR)) {
					costPerMonth += val.cost;
					numMonths += 12;
				} else {
					costPerMonth += val.cost;
					numMonths += calendarInstance.get(Calendar.MONTH);
				}
			}

			return costPerMonth / numMonths;
		} else {
			return 0;
		}
	}

	/**
	 * Gets the average weekly cost
	 * 
	 * @return the average weekly cost
	 */
	public double getCostPerWeek() {
		if (fuelRecords.size() > 0) {
			double costPerWeek = 0;
			int numWeeks = 0;
			ArrayList<YearCostWrapper> values = getCosts();

			for (YearCostWrapper val : values) {
				if (val.year != calendarInstance.get(Calendar.YEAR)) {
					costPerWeek += val.cost;
					numWeeks += 52;
				} else {
					costPerWeek += val.cost;
					numWeeks += calendarInstance.get(Calendar.WEEK_OF_YEAR);
				}
			}

			return costPerWeek / numWeeks;
		} else {
			return 0;
		}
	}

	/**
	 * Separates the costs into yearly costs
	 * 
	 * @return a list of costs / year
	 */
	private ArrayList<YearCostWrapper> getCosts() {
		if (fuelRecords.size() > 0) {
			ArrayList<Integer> years = new ArrayList<Integer>();
			ArrayList<YearCostWrapper> values = new ArrayList<YearCostWrapper>();

			for (FuelRecord rec : fuelRecords) {
				Calendar cal = calendarInstance;
				cal.setTime(rec.getDate());
				if (years.contains(cal.get(Calendar.YEAR))) {
					for (YearCostWrapper val : values) {
						if (val.year == cal.get(Calendar.YEAR)) {
							val.cost += rec.getTotalCost();
						}
					}
				} else {
					YearCostWrapper temp = new YearCostWrapper();
					temp.cost = rec.getTotalCost();
					temp.year = cal.get(Calendar.YEAR);

					values.add(temp);
					years.add(temp.year);
				}
			}

			return values;
		} else {
			return new ArrayList<YearCostWrapper>();
		}
	}

	/**
	 * A small wrapper class used to contain a year and a cost
	 * 
	 * @author breber
	 */
	private class YearCostWrapper {
		public int year;
		public double cost;
	}
}

