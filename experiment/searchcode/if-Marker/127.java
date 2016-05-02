/*
 * Copyright 2010-2012 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 * 
 *  http://aws.amazon.com/apache2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.services.rds.model;
import com.amazonaws.AmazonWebServiceRequest;

/**
 * Container for the parameters to the {@link com.amazonaws.services.rds.AmazonRDS#describeReservedDBInstancesOfferings(DescribeReservedDBInstancesOfferingsRequest) DescribeReservedDBInstancesOfferings operation}.
 * <p>
 * Lists available reserved DB Instance offerings.
 * </p>
 *
 * @see com.amazonaws.services.rds.AmazonRDS#describeReservedDBInstancesOfferings(DescribeReservedDBInstancesOfferingsRequest)
 */
public class DescribeReservedDBInstancesOfferingsRequest extends AmazonWebServiceRequest {

    /**
     * The offering identifier filter value. Specify this parameter to show
     * only the available offering that matches the specified reservation
     * identifier. <p>Example:
     * <code>438012d3-4052-4cc7-b2e3-8d3372e0e706</code>
     */
    private String reservedDBInstancesOfferingId;

    /**
     * The DB Instance class filter value. Specify this parameter to show
     * only the available offerings matching the specified DB Instance class.
     */
    private String dBInstanceClass;

    /**
     * Duration filter value, specified in years or seconds. Specify this
     * parameter to show only reservations for this duration. <p>Valid
     * Values: <code>1 | 3 | 31536000 | 94608000</code>
     */
    private String duration;

    /**
     * Product description filter value. Specify this parameter to show only
     * the available offerings matching the specified product description.
     */
    private String productDescription;

    /**
     * The Multi-AZ filter value. Specify this parameter to show only the
     * available offerings matching the specified Multi-AZ parameter.
     */
    private Boolean multiAZ;

    /**
     * The maximum number of records to include in the response. If more than
     * the <code>MaxRecords</code> value is available, a marker is included
     * in the response so that the following results can be retrieved.
     * <p>Default: 100 <p>Constraints: minimum 20, maximum 100
     */
    private Integer maxRecords;

    /**
     * The marker provided in the previous request. If this parameter is
     * specified, the response includes records beyond the marker only, up to
     * <code>MaxRecords</code>.
     */
    private String marker;

    /**
     * Default constructor for a new DescribeReservedDBInstancesOfferingsRequest object.  Callers should use the
     * setter or fluent setter (with...) methods to initialize this object after creating it.
     */
    public DescribeReservedDBInstancesOfferingsRequest() {}
    
    /**
     * The offering identifier filter value. Specify this parameter to show
     * only the available offering that matches the specified reservation
     * identifier. <p>Example:
     * <code>438012d3-4052-4cc7-b2e3-8d3372e0e706</code>
     *
     * @return The offering identifier filter value. Specify this parameter to show
     *         only the available offering that matches the specified reservation
     *         identifier. <p>Example:
     *         <code>438012d3-4052-4cc7-b2e3-8d3372e0e706</code>
     */
    public String getReservedDBInstancesOfferingId() {
        return reservedDBInstancesOfferingId;
    }
    
    /**
     * The offering identifier filter value. Specify this parameter to show
     * only the available offering that matches the specified reservation
     * identifier. <p>Example:
     * <code>438012d3-4052-4cc7-b2e3-8d3372e0e706</code>
     *
     * @param reservedDBInstancesOfferingId The offering identifier filter value. Specify this parameter to show
     *         only the available offering that matches the specified reservation
     *         identifier. <p>Example:
     *         <code>438012d3-4052-4cc7-b2e3-8d3372e0e706</code>
     */
    public void setReservedDBInstancesOfferingId(String reservedDBInstancesOfferingId) {
        this.reservedDBInstancesOfferingId = reservedDBInstancesOfferingId;
    }
    
    /**
     * The offering identifier filter value. Specify this parameter to show
     * only the available offering that matches the specified reservation
     * identifier. <p>Example:
     * <code>438012d3-4052-4cc7-b2e3-8d3372e0e706</code>
     * <p>
     * Returns a reference to this object so that method calls can be chained together.
     *
     * @param reservedDBInstancesOfferingId The offering identifier filter value. Specify this parameter to show
     *         only the available offering that matches the specified reservation
     *         identifier. <p>Example:
     *         <code>438012d3-4052-4cc7-b2e3-8d3372e0e706</code>
     *
     * @return A reference to this updated object so that method calls can be chained 
     *         together. 
     */
    public DescribeReservedDBInstancesOfferingsRequest withReservedDBInstancesOfferingId(String reservedDBInstancesOfferingId) {
        this.reservedDBInstancesOfferingId = reservedDBInstancesOfferingId;
        return this;
    }
    
    
    /**
     * The DB Instance class filter value. Specify this parameter to show
     * only the available offerings matching the specified DB Instance class.
     *
     * @return The DB Instance class filter value. Specify this parameter to show
     *         only the available offerings matching the specified DB Instance class.
     */
    public String getDBInstanceClass() {
        return dBInstanceClass;
    }
    
    /**
     * The DB Instance class filter value. Specify this parameter to show
     * only the available offerings matching the specified DB Instance class.
     *
     * @param dBInstanceClass The DB Instance class filter value. Specify this parameter to show
     *         only the available offerings matching the specified DB Instance class.
     */
    public void setDBInstanceClass(String dBInstanceClass) {
        this.dBInstanceClass = dBInstanceClass;
    }
    
    /**
     * The DB Instance class filter value. Specify this parameter to show
     * only the available offerings matching the specified DB Instance class.
     * <p>
     * Returns a reference to this object so that method calls can be chained together.
     *
     * @param dBInstanceClass The DB Instance class filter value. Specify this parameter to show
     *         only the available offerings matching the specified DB Instance class.
     *
     * @return A reference to this updated object so that method calls can be chained 
     *         together. 
     */
    public DescribeReservedDBInstancesOfferingsRequest withDBInstanceClass(String dBInstanceClass) {
        this.dBInstanceClass = dBInstanceClass;
        return this;
    }
    
    
    /**
     * Duration filter value, specified in years or seconds. Specify this
     * parameter to show only reservations for this duration. <p>Valid
     * Values: <code>1 | 3 | 31536000 | 94608000</code>
     *
     * @return Duration filter value, specified in years or seconds. Specify this
     *         parameter to show only reservations for this duration. <p>Valid
     *         Values: <code>1 | 3 | 31536000 | 94608000</code>
     */
    public String getDuration() {
        return duration;
    }
    
    /**
     * Duration filter value, specified in years or seconds. Specify this
     * parameter to show only reservations for this duration. <p>Valid
     * Values: <code>1 | 3 | 31536000 | 94608000</code>
     *
     * @param duration Duration filter value, specified in years or seconds. Specify this
     *         parameter to show only reservations for this duration. <p>Valid
     *         Values: <code>1 | 3 | 31536000 | 94608000</code>
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    /**
     * Duration filter value, specified in years or seconds. Specify this
     * parameter to show only reservations for this duration. <p>Valid
     * Values: <code>1 | 3 | 31536000 | 94608000</code>
     * <p>
     * Returns a reference to this object so that method calls can be chained together.
     *
     * @param duration Duration filter value, specified in years or seconds. Specify this
     *         parameter to show only reservations for this duration. <p>Valid
     *         Values: <code>1 | 3 | 31536000 | 94608000</code>
     *
     * @return A reference to this updated object so that method calls can be chained 
     *         together. 
     */
    public DescribeReservedDBInstancesOfferingsRequest withDuration(String duration) {
        this.duration = duration;
        return this;
    }
    
    
    /**
     * Product description filter value. Specify this parameter to show only
     * the available offerings matching the specified product description.
     *
     * @return Product description filter value. Specify this parameter to show only
     *         the available offerings matching the specified product description.
     */
    public String getProductDescription() {
        return productDescription;
    }
    
    /**
     * Product description filter value. Specify this parameter to show only
     * the available offerings matching the specified product description.
     *
     * @param productDescription Product description filter value. Specify this parameter to show only
     *         the available offerings matching the specified product description.
     */
    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }
    
    /**
     * Product description filter value. Specify this parameter to show only
     * the available offerings matching the specified product description.
     * <p>
     * Returns a reference to this object so that method calls can be chained together.
     *
     * @param productDescription Product description filter value. Specify this parameter to show only
     *         the available offerings matching the specified product description.
     *
     * @return A reference to this updated object so that method calls can be chained 
     *         together. 
     */
    public DescribeReservedDBInstancesOfferingsRequest withProductDescription(String productDescription) {
        this.productDescription = productDescription;
        return this;
    }
    
    
    /**
     * The Multi-AZ filter value. Specify this parameter to show only the
     * available offerings matching the specified Multi-AZ parameter.
     *
     * @return The Multi-AZ filter value. Specify this parameter to show only the
     *         available offerings matching the specified Multi-AZ parameter.
     */
    public Boolean isMultiAZ() {
        return multiAZ;
    }
    
    /**
     * The Multi-AZ filter value. Specify this parameter to show only the
     * available offerings matching the specified Multi-AZ parameter.
     *
     * @param multiAZ The Multi-AZ filter value. Specify this parameter to show only the
     *         available offerings matching the specified Multi-AZ parameter.
     */
    public void setMultiAZ(Boolean multiAZ) {
        this.multiAZ = multiAZ;
    }
    
    /**
     * The Multi-AZ filter value. Specify this parameter to show only the
     * available offerings matching the specified Multi-AZ parameter.
     * <p>
     * Returns a reference to this object so that method calls can be chained together.
     *
     * @param multiAZ The Multi-AZ filter value. Specify this parameter to show only the
     *         available offerings matching the specified Multi-AZ parameter.
     *
     * @return A reference to this updated object so that method calls can be chained 
     *         together. 
     */
    public DescribeReservedDBInstancesOfferingsRequest withMultiAZ(Boolean multiAZ) {
        this.multiAZ = multiAZ;
        return this;
    }
    
    
    /**
     * The Multi-AZ filter value. Specify this parameter to show only the
     * available offerings matching the specified Multi-AZ parameter.
     *
     * @return The Multi-AZ filter value. Specify this parameter to show only the
     *         available offerings matching the specified Multi-AZ parameter.
     */
    public Boolean getMultiAZ() {
        return multiAZ;
    }
    
    /**
     * The maximum number of records to include in the response. If more than
     * the <code>MaxRecords</code> value is available, a marker is included
     * in the response so that the following results can be retrieved.
     * <p>Default: 100 <p>Constraints: minimum 20, maximum 100
     *
     * @return The maximum number of records to include in the response. If more than
     *         the <code>MaxRecords</code> value is available, a marker is included
     *         in the response so that the following results can be retrieved.
     *         <p>Default: 100 <p>Constraints: minimum 20, maximum 100
     */
    public Integer getMaxRecords() {
        return maxRecords;
    }
    
    /**
     * The maximum number of records to include in the response. If more than
     * the <code>MaxRecords</code> value is available, a marker is included
     * in the response so that the following results can be retrieved.
     * <p>Default: 100 <p>Constraints: minimum 20, maximum 100
     *
     * @param maxRecords The maximum number of records to include in the response. If more than
     *         the <code>MaxRecords</code> value is available, a marker is included
     *         in the response so that the following results can be retrieved.
     *         <p>Default: 100 <p>Constraints: minimum 20, maximum 100
     */
    public void setMaxRecords(Integer maxRecords) {
        this.maxRecords = maxRecords;
    }
    
    /**
     * The maximum number of records to include in the response. If more than
     * the <code>MaxRecords</code> value is available, a marker is included
     * in the response so that the following results can be retrieved.
     * <p>Default: 100 <p>Constraints: minimum 20, maximum 100
     * <p>
     * Returns a reference to this object so that method calls can be chained together.
     *
     * @param maxRecords The maximum number of records to include in the response. If more than
     *         the <code>MaxRecords</code> value is available, a marker is included
     *         in the response so that the following results can be retrieved.
     *         <p>Default: 100 <p>Constraints: minimum 20, maximum 100
     *
     * @return A reference to this updated object so that method calls can be chained 
     *         together. 
     */
    public DescribeReservedDBInstancesOfferingsRequest withMaxRecords(Integer maxRecords) {
        this.maxRecords = maxRecords;
        return this;
    }
    
    
    /**
     * The marker provided in the previous request. If this parameter is
     * specified, the response includes records beyond the marker only, up to
     * <code>MaxRecords</code>.
     *
     * @return The marker provided in the previous request. If this parameter is
     *         specified, the response includes records beyond the marker only, up to
     *         <code>MaxRecords</code>.
     */
    public String getMarker() {
        return marker;
    }
    
    /**
     * The marker provided in the previous request. If this parameter is
     * specified, the response includes records beyond the marker only, up to
     * <code>MaxRecords</code>.
     *
     * @param marker The marker provided in the previous request. If this parameter is
     *         specified, the response includes records beyond the marker only, up to
     *         <code>MaxRecords</code>.
     */
    public void setMarker(String marker) {
        this.marker = marker;
    }
    
    /**
     * The marker provided in the previous request. If this parameter is
     * specified, the response includes records beyond the marker only, up to
     * <code>MaxRecords</code>.
     * <p>
     * Returns a reference to this object so that method calls can be chained together.
     *
     * @param marker The marker provided in the previous request. If this parameter is
     *         specified, the response includes records beyond the marker only, up to
     *         <code>MaxRecords</code>.
     *
     * @return A reference to this updated object so that method calls can be chained 
     *         together. 
     */
    public DescribeReservedDBInstancesOfferingsRequest withMarker(String marker) {
        this.marker = marker;
        return this;
    }
    
    
    /**
     * Returns a string representation of this object; useful for testing and
     * debugging.
     *
     * @return A string representation of this object.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (reservedDBInstancesOfferingId != null) sb.append("ReservedDBInstancesOfferingId: " + reservedDBInstancesOfferingId + ", ");
        if (dBInstanceClass != null) sb.append("DBInstanceClass: " + dBInstanceClass + ", ");
        if (duration != null) sb.append("Duration: " + duration + ", ");
        if (productDescription != null) sb.append("ProductDescription: " + productDescription + ", ");
        if (multiAZ != null) sb.append("MultiAZ: " + multiAZ + ", ");
        if (maxRecords != null) sb.append("MaxRecords: " + maxRecords + ", ");
        if (marker != null) sb.append("Marker: " + marker + ", ");
        sb.append("}");
        return sb.toString();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;
        
        hashCode = prime * hashCode + ((getReservedDBInstancesOfferingId() == null) ? 0 : getReservedDBInstancesOfferingId().hashCode()); 
        hashCode = prime * hashCode + ((getDBInstanceClass() == null) ? 0 : getDBInstanceClass().hashCode()); 
        hashCode = prime * hashCode + ((getDuration() == null) ? 0 : getDuration().hashCode()); 
        hashCode = prime * hashCode + ((getProductDescription() == null) ? 0 : getProductDescription().hashCode()); 
        hashCode = prime * hashCode + ((isMultiAZ() == null) ? 0 : isMultiAZ().hashCode()); 
        hashCode = prime * hashCode + ((getMaxRecords() == null) ? 0 : getMaxRecords().hashCode()); 
        hashCode = prime * hashCode + ((getMarker() == null) ? 0 : getMarker().hashCode()); 
        return hashCode;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
    
        if (obj instanceof DescribeReservedDBInstancesOfferingsRequest == false) return false;
        DescribeReservedDBInstancesOfferingsRequest other = (DescribeReservedDBInstancesOfferingsRequest)obj;
        
        if (other.getReservedDBInstancesOfferingId() == null ^ this.getReservedDBInstancesOfferingId() == null) return false;
        if (other.getReservedDBInstancesOfferingId() != null && other.getReservedDBInstancesOfferingId().equals(this.getReservedDBInstancesOfferingId()) == false) return false; 
        if (other.getDBInstanceClass() == null ^ this.getDBInstanceClass() == null) return false;
        if (other.getDBInstanceClass() != null && other.getDBInstanceClass().equals(this.getDBInstanceClass()) == false) return false; 
        if (other.getDuration() == null ^ this.getDuration() == null) return false;
        if (other.getDuration() != null && other.getDuration().equals(this.getDuration()) == false) return false; 
        if (other.getProductDescription() == null ^ this.getProductDescription() == null) return false;
        if (other.getProductDescription() != null && other.getProductDescription().equals(this.getProductDescription()) == false) return false; 
        if (other.isMultiAZ() == null ^ this.isMultiAZ() == null) return false;
        if (other.isMultiAZ() != null && other.isMultiAZ().equals(this.isMultiAZ()) == false) return false; 
        if (other.getMaxRecords() == null ^ this.getMaxRecords() == null) return false;
        if (other.getMaxRecords() != null && other.getMaxRecords().equals(this.getMaxRecords()) == false) return false; 
        if (other.getMarker() == null ^ this.getMarker() == null) return false;
        if (other.getMarker() != null && other.getMarker().equals(this.getMarker()) == false) return false; 
        return true;
    }
    
}
    
