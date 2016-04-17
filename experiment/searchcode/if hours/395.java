/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tnkmatic.onlinebooking.ejb.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Eiichi
 */
@Embeddable
public class MstBusinessHoursPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 4)
    @Column(name = "BUSINESS_HOURS_FROM")
    private String businessHoursFrom;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 4)
    @Column(name = "BUSINESS_HOURS_TO")
    private String businessHoursTo;

    public MstBusinessHoursPK() {
    }

    public MstBusinessHoursPK(String businessHoursFrom, String businessHoursTo) {
        this.businessHoursFrom = businessHoursFrom;
        this.businessHoursTo = businessHoursTo;
    }

    public String getBusinessHoursFrom() {
        return businessHoursFrom;
    }

    public void setBusinessHoursFrom(String businessHoursFrom) {
        this.businessHoursFrom = businessHoursFrom;
    }

    public String getBusinessHoursTo() {
        return businessHoursTo;
    }

    public void setBusinessHoursTo(String businessHoursTo) {
        this.businessHoursTo = businessHoursTo;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (businessHoursFrom != null ? businessHoursFrom.hashCode() : 0);
        hash += (businessHoursTo != null ? businessHoursTo.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MstBusinessHoursPK)) {
            return false;
        }
        MstBusinessHoursPK other = (MstBusinessHoursPK) object;
        if ((this.businessHoursFrom == null && other.businessHoursFrom != null) || (this.businessHoursFrom != null && !this.businessHoursFrom.equals(other.businessHoursFrom))) {
            return false;
        }
        if ((this.businessHoursTo == null && other.businessHoursTo != null) || (this.businessHoursTo != null && !this.businessHoursTo.equals(other.businessHoursTo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.tnkmatic.onlinebooking.ejb.entity.MstBusinessHoursPK[ businessHoursFrom=" + businessHoursFrom + ", businessHoursTo=" + businessHoursTo + " ]";
    }
    
}

