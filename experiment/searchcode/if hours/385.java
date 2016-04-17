
package com.tomtom.work.workbus.route.respons;

import javax.annotation.Generated;

import com.google.common.base.Objects;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class SectionTime {

    @SerializedName("hours")
    @Expose
    private Integer hours;
    @SerializedName("minutes")
    @Expose
    private Integer minutes;

    /**
     * 
     * @return
     *     The hours
     */
    public Integer getHours() {
        return hours;
    }

    /**
     * 
     * @param hours
     *     The hours
     */
    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public SectionTime withHours(Integer hours) {
        this.hours = hours;
        return this;
    }

    /**
     * 
     * @return
     *     The minutes
     */
    public Integer getMinutes() {
        return minutes;
    }

    /**
     * 
     * @param minutes
     *     The minutes
     */
    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public SectionTime withMinutes(Integer minutes) {
        this.minutes = minutes;
        return this;
    }

    @Override
    public String toString() {
        if (hours>0){
            return hours + ":" + minutes;
        }
        return minutes + "min";


    }
}

