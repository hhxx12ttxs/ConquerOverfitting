/**
 * IterationData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package org.xplanner.soap;

public class IterationData  extends com.technoetic.xplanner.soap.domain.DomainData  implements java.io.Serializable {
    private double actualHours;
    private double addedHours;
    private double adjustedEstimatedHours;
    private double daysWorked;
    private java.lang.String description;
    private java.util.Calendar endDate;
    private double estimatedHours;
    private java.lang.String name;
    private double overestimatedHours;
    private double postponedHours;
    private int projectId;
    private double remainingHours;
    private java.util.Calendar startDate;
    private java.lang.String statusKey;
    private double underestimatedHours;

    public IterationData() {
    }

    public IterationData(
           double actualHours,
           double addedHours,
           double adjustedEstimatedHours,
           double daysWorked,
           java.lang.String description,
           java.util.Calendar endDate,
           double estimatedHours,
           java.lang.String name,
           double overestimatedHours,
           double postponedHours,
           int projectId,
           double remainingHours,
           java.util.Calendar startDate,
           java.lang.String statusKey,
           double underestimatedHours) {
           this.actualHours = actualHours;
           this.addedHours = addedHours;
           this.adjustedEstimatedHours = adjustedEstimatedHours;
           this.daysWorked = daysWorked;
           this.description = description;
           this.endDate = endDate;
           this.estimatedHours = estimatedHours;
           this.name = name;
           this.overestimatedHours = overestimatedHours;
           this.postponedHours = postponedHours;
           this.projectId = projectId;
           this.remainingHours = remainingHours;
           this.startDate = startDate;
           this.statusKey = statusKey;
           this.underestimatedHours = underestimatedHours;
    }


    /**
     * Gets the actualHours value for this IterationData.
     * 
     * @return actualHours
     */
    public double getActualHours() {
        return actualHours;
    }


    /**
     * Sets the actualHours value for this IterationData.
     * 
     * @param actualHours
     */
    public void setActualHours(double actualHours) {
        this.actualHours = actualHours;
    }


    /**
     * Gets the addedHours value for this IterationData.
     * 
     * @return addedHours
     */
    public double getAddedHours() {
        return addedHours;
    }


    /**
     * Sets the addedHours value for this IterationData.
     * 
     * @param addedHours
     */
    public void setAddedHours(double addedHours) {
        this.addedHours = addedHours;
    }


    /**
     * Gets the adjustedEstimatedHours value for this IterationData.
     * 
     * @return adjustedEstimatedHours
     */
    public double getAdjustedEstimatedHours() {
        return adjustedEstimatedHours;
    }


    /**
     * Sets the adjustedEstimatedHours value for this IterationData.
     * 
     * @param adjustedEstimatedHours
     */
    public void setAdjustedEstimatedHours(double adjustedEstimatedHours) {
        this.adjustedEstimatedHours = adjustedEstimatedHours;
    }


    /**
     * Gets the daysWorked value for this IterationData.
     * 
     * @return daysWorked
     */
    public double getDaysWorked() {
        return daysWorked;
    }


    /**
     * Sets the daysWorked value for this IterationData.
     * 
     * @param daysWorked
     */
    public void setDaysWorked(double daysWorked) {
        this.daysWorked = daysWorked;
    }


    /**
     * Gets the description value for this IterationData.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this IterationData.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the endDate value for this IterationData.
     * 
     * @return endDate
     */
    public java.util.Calendar getEndDate() {
        return endDate;
    }


    /**
     * Sets the endDate value for this IterationData.
     * 
     * @param endDate
     */
    public void setEndDate(java.util.Calendar endDate) {
        this.endDate = endDate;
    }


    /**
     * Gets the estimatedHours value for this IterationData.
     * 
     * @return estimatedHours
     */
    public double getEstimatedHours() {
        return estimatedHours;
    }


    /**
     * Sets the estimatedHours value for this IterationData.
     * 
     * @param estimatedHours
     */
    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }


    /**
     * Gets the name value for this IterationData.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this IterationData.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the overestimatedHours value for this IterationData.
     * 
     * @return overestimatedHours
     */
    public double getOverestimatedHours() {
        return overestimatedHours;
    }


    /**
     * Sets the overestimatedHours value for this IterationData.
     * 
     * @param overestimatedHours
     */
    public void setOverestimatedHours(double overestimatedHours) {
        this.overestimatedHours = overestimatedHours;
    }


    /**
     * Gets the postponedHours value for this IterationData.
     * 
     * @return postponedHours
     */
    public double getPostponedHours() {
        return postponedHours;
    }


    /**
     * Sets the postponedHours value for this IterationData.
     * 
     * @param postponedHours
     */
    public void setPostponedHours(double postponedHours) {
        this.postponedHours = postponedHours;
    }


    /**
     * Gets the projectId value for this IterationData.
     * 
     * @return projectId
     */
    public int getProjectId() {
        return projectId;
    }


    /**
     * Sets the projectId value for this IterationData.
     * 
     * @param projectId
     */
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }


    /**
     * Gets the remainingHours value for this IterationData.
     * 
     * @return remainingHours
     */
    public double getRemainingHours() {
        return remainingHours;
    }


    /**
     * Sets the remainingHours value for this IterationData.
     * 
     * @param remainingHours
     */
    public void setRemainingHours(double remainingHours) {
        this.remainingHours = remainingHours;
    }


    /**
     * Gets the startDate value for this IterationData.
     * 
     * @return startDate
     */
    public java.util.Calendar getStartDate() {
        return startDate;
    }


    /**
     * Sets the startDate value for this IterationData.
     * 
     * @param startDate
     */
    public void setStartDate(java.util.Calendar startDate) {
        this.startDate = startDate;
    }


    /**
     * Gets the statusKey value for this IterationData.
     * 
     * @return statusKey
     */
    public java.lang.String getStatusKey() {
        return statusKey;
    }


    /**
     * Sets the statusKey value for this IterationData.
     * 
     * @param statusKey
     */
    public void setStatusKey(java.lang.String statusKey) {
        this.statusKey = statusKey;
    }


    /**
     * Gets the underestimatedHours value for this IterationData.
     * 
     * @return underestimatedHours
     */
    public double getUnderestimatedHours() {
        return underestimatedHours;
    }


    /**
     * Sets the underestimatedHours value for this IterationData.
     * 
     * @param underestimatedHours
     */
    public void setUnderestimatedHours(double underestimatedHours) {
        this.underestimatedHours = underestimatedHours;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof IterationData)) return false;
        IterationData other = (IterationData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            this.actualHours == other.getActualHours() &&
            this.addedHours == other.getAddedHours() &&
            this.adjustedEstimatedHours == other.getAdjustedEstimatedHours() &&
            this.daysWorked == other.getDaysWorked() &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            ((this.endDate==null && other.getEndDate()==null) || 
             (this.endDate!=null &&
              this.endDate.equals(other.getEndDate()))) &&
            this.estimatedHours == other.getEstimatedHours() &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            this.overestimatedHours == other.getOverestimatedHours() &&
            this.postponedHours == other.getPostponedHours() &&
            this.projectId == other.getProjectId() &&
            this.remainingHours == other.getRemainingHours() &&
            ((this.startDate==null && other.getStartDate()==null) || 
             (this.startDate!=null &&
              this.startDate.equals(other.getStartDate()))) &&
            ((this.statusKey==null && other.getStatusKey()==null) || 
             (this.statusKey!=null &&
              this.statusKey.equals(other.getStatusKey()))) &&
            this.underestimatedHours == other.getUnderestimatedHours();
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        _hashCode += new Double(getActualHours()).hashCode();
        _hashCode += new Double(getAddedHours()).hashCode();
        _hashCode += new Double(getAdjustedEstimatedHours()).hashCode();
        _hashCode += new Double(getDaysWorked()).hashCode();
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        if (getEndDate() != null) {
            _hashCode += getEndDate().hashCode();
        }
        _hashCode += new Double(getEstimatedHours()).hashCode();
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        _hashCode += new Double(getOverestimatedHours()).hashCode();
        _hashCode += new Double(getPostponedHours()).hashCode();
        _hashCode += getProjectId();
        _hashCode += new Double(getRemainingHours()).hashCode();
        if (getStartDate() != null) {
            _hashCode += getStartDate().hashCode();
        }
        if (getStatusKey() != null) {
            _hashCode += getStatusKey().hashCode();
        }
        _hashCode += new Double(getUnderestimatedHours()).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

}

