package edu.khai.applicationtracker.model.application;

import edu.khai.applicationtracker.model.Application;

public class HourlyRate extends Application {

    private static final long serialVersionUID = 8863096574005355968L;

    private Long hours;

    /**
     * @return the hours
     */
    public Long getHours() {
        return hours;
    }

    /**
     * @param hours the hours to set
     */
    public void setHours(Long hours) {
        this.hours = hours;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((hours == null) ? 0 : hours.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        HourlyRate other = (HourlyRate) obj;
        if (hours == null) {
            if (other.hours != null)
                return false;
        } else if (!hours.equals(other.hours))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "HourlyRate [hours=" + hours + "]";
    }

}
