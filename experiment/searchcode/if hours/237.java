/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tnkmatic.onlinebooking.ejb.entity;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Eiichi
 */
@Entity
@Table(name = "mst_business_hours")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MstBusinessHours.findAll", query = "SELECT m FROM MstBusinessHours m"),
    @NamedQuery(name = "MstBusinessHours.findByBusinessHoursFrom", query = "SELECT m FROM MstBusinessHours m WHERE m.mstBusinessHoursPK.businessHoursFrom = :businessHoursFrom"),
    @NamedQuery(name = "MstBusinessHours.findByBusinessHoursTo", query = "SELECT m FROM MstBusinessHours m WHERE m.mstBusinessHoursPK.businessHoursTo = :businessHoursTo")})
public class MstBusinessHours implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected MstBusinessHoursPK mstBusinessHoursPK;

    public MstBusinessHours() {
    }

    public MstBusinessHours(MstBusinessHoursPK mstBusinessHoursPK) {
        this.mstBusinessHoursPK = mstBusinessHoursPK;
    }

    public MstBusinessHours(String businessHoursFrom, String businessHoursTo) {
        this.mstBusinessHoursPK = new MstBusinessHoursPK(businessHoursFrom, businessHoursTo);
    }

    public MstBusinessHoursPK getMstBusinessHoursPK() {
        return mstBusinessHoursPK;
    }

    public void setMstBusinessHoursPK(MstBusinessHoursPK mstBusinessHoursPK) {
        this.mstBusinessHoursPK = mstBusinessHoursPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (mstBusinessHoursPK != null ? mstBusinessHoursPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MstBusinessHours)) {
            return false;
        }
        MstBusinessHours other = (MstBusinessHours) object;
        if ((this.mstBusinessHoursPK == null && other.mstBusinessHoursPK != null) || (this.mstBusinessHoursPK != null && !this.mstBusinessHoursPK.equals(other.mstBusinessHoursPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.tnkmatic.onlinebooking.ejb.entity.MstBusinessHours[ mstBusinessHoursPK=" + mstBusinessHoursPK + " ]";
    }
    
}

