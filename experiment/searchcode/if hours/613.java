/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oltranz.USSD4Transport.beans;


import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author aimable
 */

@XmlRootElement(name="HOURS")
@XmlAccessorType(XmlAccessType.FIELD)
public class HoursList {   
    
    @XmlElement(name="HOUR")
    private List<Hour> hours;

    public HoursList() {
        hours=new ArrayList<>();
    }

    /**
     * @return the hours
     */
    public List<Hour> getHours() {
        if(hours==null)
            hours=new ArrayList<>();
        return hours;
    }

    /**
     * @param hours the hours to set
     */
    public void setHours(List<Hour> hours) {
        this.hours = hours;
    }

   
    
}

