/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import model.user.Staff;
import util.Utilization;

/**
 *
 * @author CUNEYT
 */
public class ConsultedCampaign {

    private Staff staff;
    private Campaign campaign;
    private double inOfficeHours;
    private double outOfOfficeHours;

    public ConsultedCampaign(Staff staff, Campaign campaign) {
        this.staff = staff;
        this.campaign = campaign;
        this.inOfficeHours = 0.0;
        this.outOfOfficeHours = 0.0;
    }

    public void addInOfficeHours(double amount) {
        if (amount > 0) {
            inOfficeHours += amount;
        }
    }

    public void addOutOfOfficeHours(double amount) {
        if (amount > 0) {
            outOfOfficeHours += amount;
        }
    }

    public double getInOfficeHours() {
        return Utilization.formatDouble(inOfficeHours);
    }

    public void setInOfficeHours(double inOfficeHours) {
        this.inOfficeHours = inOfficeHours;
    }

    public double getOutOfOfficeHours() {
        return Utilization.formatDouble(outOfOfficeHours);
    }

    public void setOutOfOfficeHours(double outOfOfficeHours) {
        this.outOfOfficeHours = outOfOfficeHours;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    @Override
    public String toString() {
        return getCampaign().getTitle() + ", in-office hours: " + getInOfficeHours() + ", out-of-office hours: " + getOutOfOfficeHours();
    }
}

