/* 
 * Copyright (C) 2014 Alex Pavlunenko <alexp at xpresstek.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.xpresstek.zroster.ejb;

/**
 *
 * @author apavlune
 */
public class EmployeeHours {

    private Employee employee;

    private double dayScheduledHours = 0;
    private double dayWorkedHours = 0;
    private double dayRemainingHours = 0;
    private double weekScheduledHours = 0;
    private double weekWorkedHours = 0;
    private double weekRemainingHours = 0;

    public EmployeeHours(Employee employee) {
        this.employee = employee;
    }

    /**
     *
     * @return Scheduled hours for today.
     */
    public double getDayScheduledHours() {
        return dayScheduledHours;

    }

    /**
     * Setter for today's scheduled hours
     *
     * @param todayScheduledHours
     */
    public void setDayScheduledHours(double todayScheduledHours) {
        this.dayScheduledHours = todayScheduledHours;
        this.dayRemainingHours
                = this.dayScheduledHours - this.dayWorkedHours;
    }

    /**
     *
     * @return Worked hours for today
     */
    public double getDayWorkedHours() {
        return dayWorkedHours;
    }

    /**
     * Setter for today's worked hours
     *
     * @param todayWorkedHours
     */
    public void setDayWorkedHours(double todayWorkedHours) {
        this.dayWorkedHours = todayWorkedHours;
        this.dayRemainingHours
                = this.dayScheduledHours - this.dayWorkedHours;
    }

    /**
     *
     * @return todays Worked hours
     */
    public double getDayRemainingHours() {
        return dayRemainingHours;
    }

    /**
     * Setter for today's worked hours
     *
     * @param todayRemainingHours
     */
    public void setDayRemainingHours(double todayRemainingHours) {
        this.dayRemainingHours = todayRemainingHours;

    }

    /**
     *
     * @return Scheduled hours for the week
     */
    public double getWeekScheduledHours() {
        return weekScheduledHours;
    }

    /**
     * Setter for scheduled hours for the week
     *
     * @param weekScheduledHours
     */
    public void setWeekScheduledHours(double weekScheduledHours) {
        this.weekScheduledHours = weekScheduledHours;
        this.weekRemainingHours
                = this.weekScheduledHours - this.weekWorkedHours;
    }

    /**
     *
     * @return Worked hours for the week
     */
    public double getWeekWorkedHours() {
        return weekWorkedHours;
    }

    /**
     * Setter for worked hours for the week
     *
     * @param weekWorkedHours
     */
    public void setWeekWorkedHours(double weekWorkedHours) {
        this.weekWorkedHours = weekWorkedHours;
        this.weekRemainingHours
                = this.weekScheduledHours - this.weekWorkedHours;
    }

    /**
     *
     * @return Remaining hours for the week
     */
    public double getWeekRemainingHours() {
        return weekRemainingHours;
    }

    /**
     * Setter for the remaining hours for the week
     *
     * @param weekRemainingHours
     */
    public void setWeekRemainingHours(double weekRemainingHours) {
        this.weekRemainingHours = weekRemainingHours;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    
    
}

