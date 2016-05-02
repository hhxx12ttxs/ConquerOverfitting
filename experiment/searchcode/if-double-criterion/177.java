/**
 * CampaignCriterion.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.api.adwords.v201206.cm;


/**
 * Represents a campaign level criterion.
 */
public class CampaignCriterion  implements java.io.Serializable {
    /* The campaign that the criterion is in.
     *                 <span class="constraint Selectable">This field can
     * be selected using the value "CampaignId".</span><span class="constraint
     * Filterable">This field can be filtered on.</span>
     *                 <span class="constraint Required">This field is required
     * and should not be {@code null}.</span> */
    private java.lang.Long campaignId;

    /* <span class="constraint Selectable">This field can be selected
     * using the value "IsNegative".</span><span class="constraint Filterable">This
     * field can be filtered on.</span>
     *                 <span class="constraint ReadOnly">This field is read
     * only and should not be set.  If this field is sent to the API, it
     * will be ignored.</span> */
    private java.lang.Boolean isNegative;

    /* The criterion part of the campaign criterion.
     *                 <span class="constraint Required">This field is required
     * and should not be {@code null}.</span> */
    private com.google.api.adwords.v201206.cm.Criterion criterion;

    /* The modifier for bids when the criterion matches.
     *                 
     *                 This field must be between 0.10 and 10.0, inclusive.
     * Specify -1.0 to clear existing bid modifier.
     *                 <span class="constraint Selectable">This field can
     * be selected using the value "BidModifier".</span><span class="constraint
     * Filterable">This field can be filtered on.</span> */
    private java.lang.Double bidModifier;

    /* Indicates that this instance is a subtype of CampaignCriterion.
     * Although this field is returned in the response, it is ignored on
     * input
     *                 and cannot be selected. Specify xsi:type instead. */
    private java.lang.String campaignCriterionType;

    public CampaignCriterion() {
    }

    public CampaignCriterion(
           java.lang.Long campaignId,
           java.lang.Boolean isNegative,
           com.google.api.adwords.v201206.cm.Criterion criterion,
           java.lang.Double bidModifier,
           java.lang.String campaignCriterionType) {
           this.campaignId = campaignId;
           this.isNegative = isNegative;
           this.criterion = criterion;
           this.bidModifier = bidModifier;
           this.campaignCriterionType = campaignCriterionType;
    }


    /**
     * Gets the campaignId value for this CampaignCriterion.
     * 
     * @return campaignId   * The campaign that the criterion is in.
     *                 <span class="constraint Selectable">This field can
     * be selected using the value "CampaignId".</span><span class="constraint
     * Filterable">This field can be filtered on.</span>
     *                 <span class="constraint Required">This field is required
     * and should not be {@code null}.</span>
     */
    public java.lang.Long getCampaignId() {
        return campaignId;
    }


    /**
     * Sets the campaignId value for this CampaignCriterion.
     * 
     * @param campaignId   * The campaign that the criterion is in.
     *                 <span class="constraint Selectable">This field can
     * be selected using the value "CampaignId".</span><span class="constraint
     * Filterable">This field can be filtered on.</span>
     *                 <span class="constraint Required">This field is required
     * and should not be {@code null}.</span>
     */
    public void setCampaignId(java.lang.Long campaignId) {
        this.campaignId = campaignId;
    }


    /**
     * Gets the isNegative value for this CampaignCriterion.
     * 
     * @return isNegative   * <span class="constraint Selectable">This field can be selected
     * using the value "IsNegative".</span><span class="constraint Filterable">This
     * field can be filtered on.</span>
     *                 <span class="constraint ReadOnly">This field is read
     * only and should not be set.  If this field is sent to the API, it
     * will be ignored.</span>
     */
    public java.lang.Boolean getIsNegative() {
        return isNegative;
    }


    /**
     * Sets the isNegative value for this CampaignCriterion.
     * 
     * @param isNegative   * <span class="constraint Selectable">This field can be selected
     * using the value "IsNegative".</span><span class="constraint Filterable">This
     * field can be filtered on.</span>
     *                 <span class="constraint ReadOnly">This field is read
     * only and should not be set.  If this field is sent to the API, it
     * will be ignored.</span>
     */
    public void setIsNegative(java.lang.Boolean isNegative) {
        this.isNegative = isNegative;
    }


    /**
     * Gets the criterion value for this CampaignCriterion.
     * 
     * @return criterion   * The criterion part of the campaign criterion.
     *                 <span class="constraint Required">This field is required
     * and should not be {@code null}.</span>
     */
    public com.google.api.adwords.v201206.cm.Criterion getCriterion() {
        return criterion;
    }


    /**
     * Sets the criterion value for this CampaignCriterion.
     * 
     * @param criterion   * The criterion part of the campaign criterion.
     *                 <span class="constraint Required">This field is required
     * and should not be {@code null}.</span>
     */
    public void setCriterion(com.google.api.adwords.v201206.cm.Criterion criterion) {
        this.criterion = criterion;
    }


    /**
     * Gets the bidModifier value for this CampaignCriterion.
     * 
     * @return bidModifier   * The modifier for bids when the criterion matches.
     *                 
     *                 This field must be between 0.10 and 10.0, inclusive.
     * Specify -1.0 to clear existing bid modifier.
     *                 <span class="constraint Selectable">This field can
     * be selected using the value "BidModifier".</span><span class="constraint
     * Filterable">This field can be filtered on.</span>
     */
    public java.lang.Double getBidModifier() {
        return bidModifier;
    }


    /**
     * Sets the bidModifier value for this CampaignCriterion.
     * 
     * @param bidModifier   * The modifier for bids when the criterion matches.
     *                 
     *                 This field must be between 0.10 and 10.0, inclusive.
     * Specify -1.0 to clear existing bid modifier.
     *                 <span class="constraint Selectable">This field can
     * be selected using the value "BidModifier".</span><span class="constraint
     * Filterable">This field can be filtered on.</span>
     */
    public void setBidModifier(java.lang.Double bidModifier) {
        this.bidModifier = bidModifier;
    }


    /**
     * Gets the campaignCriterionType value for this CampaignCriterion.
     * 
     * @return campaignCriterionType   * Indicates that this instance is a subtype of CampaignCriterion.
     * Although this field is returned in the response, it is ignored on
     * input
     *                 and cannot be selected. Specify xsi:type instead.
     */
    public java.lang.String getCampaignCriterionType() {
        return campaignCriterionType;
    }


    /**
     * Sets the campaignCriterionType value for this CampaignCriterion.
     * 
     * @param campaignCriterionType   * Indicates that this instance is a subtype of CampaignCriterion.
     * Although this field is returned in the response, it is ignored on
     * input
     *                 and cannot be selected. Specify xsi:type instead.
     */
    public void setCampaignCriterionType(java.lang.String campaignCriterionType) {
        this.campaignCriterionType = campaignCriterionType;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CampaignCriterion)) return false;
        CampaignCriterion other = (CampaignCriterion) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.campaignId==null && other.getCampaignId()==null) || 
             (this.campaignId!=null &&
              this.campaignId.equals(other.getCampaignId()))) &&
            ((this.isNegative==null && other.getIsNegative()==null) || 
             (this.isNegative!=null &&
              this.isNegative.equals(other.getIsNegative()))) &&
            ((this.criterion==null && other.getCriterion()==null) || 
             (this.criterion!=null &&
              this.criterion.equals(other.getCriterion()))) &&
            ((this.bidModifier==null && other.getBidModifier()==null) || 
             (this.bidModifier!=null &&
              this.bidModifier.equals(other.getBidModifier()))) &&
            ((this.campaignCriterionType==null && other.getCampaignCriterionType()==null) || 
             (this.campaignCriterionType!=null &&
              this.campaignCriterionType.equals(other.getCampaignCriterionType())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getCampaignId() != null) {
            _hashCode += getCampaignId().hashCode();
        }
        if (getIsNegative() != null) {
            _hashCode += getIsNegative().hashCode();
        }
        if (getCriterion() != null) {
            _hashCode += getCriterion().hashCode();
        }
        if (getBidModifier() != null) {
            _hashCode += getBidModifier().hashCode();
        }
        if (getCampaignCriterionType() != null) {
            _hashCode += getCampaignCriterionType().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CampaignCriterion.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("https://adwords.google.com/api/adwords/cm/v201206", "CampaignCriterion"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("campaignId");
        elemField.setXmlName(new javax.xml.namespace.QName("https://adwords.google.com/api/adwords/cm/v201206", "campaignId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isNegative");
        elemField.setXmlName(new javax.xml.namespace.QName("https://adwords.google.com/api/adwords/cm/v201206", "isNegative"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("criterion");
        elemField.setXmlName(new javax.xml.namespace.QName("https://adwords.google.com/api/adwords/cm/v201206", "criterion"));
        elemField.setXmlType(new javax.xml.namespace.QName("https://adwords.google.com/api/adwords/cm/v201206", "Criterion"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("bidModifier");
        elemField.setXmlName(new javax.xml.namespace.QName("https://adwords.google.com/api/adwords/cm/v201206", "bidModifier"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("campaignCriterionType");
        elemField.setXmlName(new javax.xml.namespace.QName("https://adwords.google.com/api/adwords/cm/v201206", "CampaignCriterion.Type"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}

