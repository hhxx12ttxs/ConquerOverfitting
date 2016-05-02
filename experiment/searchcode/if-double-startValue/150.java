/**
 * Copyright (c) 2011, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.force.sdk.jpa.entities;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;

import javax.persistence.*;

import com.force.sdk.jpa.annotation.CustomField;
import com.force.sdk.jpa.annotation.PicklistValue;
import com.force.sdk.jpa.entities.TestEntity.PickValues;
import com.sforce.soap.metadata.FieldType;

/**
 * This class is a clone of TestEntity.java with the JPA annotations at the method level instead of the field level.
 * If you're trying to add additional fields to this class please add them to AnnotatedEntity.java to keep both entities in sync.
 * 
 * @author Jill Wetzler
 *
 */
@Entity
public class TestEntityMethodAnnotations implements AnnotatedEntity {

    String id;
    
    String name;
    
    boolean boolType; //checkbox
    byte byteType; //text
    short shortType; //number
    int intType; //number
    long longType; //number
    double doubleType; //number
    float floatType; //number
    char charType; //text
    
    Boolean booleanObject;
    Byte byteObject;
    Short shortObject;
    Integer integerObject;
    Long longObject;
    Double doubleObject;
    Float floatObject;
    Character characterObject;

    BigDecimal bigDecimalObject; //currency
    BigInteger bigIntegerObject; //number
    String stringObject; //text

    URL url; //url

    String phone; //phone
    String email; //email
    int percent; //percent

    Date date; //date
    Calendar dateTimeCal; //date/time
    GregorianCalendar dateTimeGCal; //date/time
    
    // Test other date formats
    Date dateTemporal; //date
    Date dateTimeTemporal; //date/time
        
    PickValues pickValueDef; //picklist
    
    PickValues pickValue; //picklist
    
    PickValues pickValueOrdinal; //MSP

    String liberalPickValueDef; //non-strict picklist value
    
    PickValues[] pickValueMultiDef; //MSP
    
    PickValues[] pickValueMulti; //MSP
    
    PickValues[] pickValueMultiOrdinal; //picklist

    String[] liberalPickValueMultiDef; //non-strict picklist value
    
    public ParentTestEntity parent; //custom lookup
    
    public User userLookUp; //standard lookup
    
    public ParentTestEntity parentMasterDetail; //standard master detail

    Calendar lastModifiedDate;
    
    String unused;
    
    EmbeddedTestEntity embedded;
    
    long autoNum;
    
    String textArea;
    
    String longTextArea;
    
    String richTextArea;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getStringObject() {
        return stringObject;
    }

    @Override
    public void setStringObject(String stringObject) {
        this.stringObject = stringObject;
    }

    @Override
    public Boolean getBooleanObject() {
        return booleanObject;
    }

    @Override
    public void setBooleanObject(Boolean booleanObject) {
        this.booleanObject = booleanObject;
    }
    
    @Override
    public boolean getBoolType() {
        return boolType;
    }

    @Override
    public void setBoolType(boolean boolType) {
        this.boolType = boolType;
    }
    
    @Override
    public Byte getByteObject() {
        return byteObject;
    }

    @Override
    public void setByteObject(Byte byteObject) {
        this.byteObject = byteObject;
    }
    
    @Override
    public byte getByteType() {
        return byteType;
    }
    
    @Override
    public void setByteType(byte byteType) {
        this.byteType = byteType;
    }
    
    @CustomField(precision = 11, scale = 0) //CustomField annotation trumps Column annotation
    @Column(precision = 17, scale = 0)
    @Override
    public Short getShortObject() {
        return shortObject;
    }
    
    @Override
    public void setShortObject(Short shortObject) {
        this.shortObject = shortObject;
    }
    
    @Override
    public short getShortType() {
        return shortType;
    }
    
    @Override
    public void setShortType(short shortType) {
        this.shortType = shortType;
    }

    @Override
    public Integer getIntegerObject() {
        return integerObject;
    }
    
    @Override
    public void setIntegerObject(Integer integerObject) {
        this.integerObject = integerObject;
    }
    
    @Override
    public int getIntType() {
        return intType;
    }
    
    @Override
    public void setIntType(int intType) {
        this.intType = intType;
    }
    
    @Override
    public Long getLongObject() {
        return longObject;
    }

    @Override
    public void setLongObject(Long longObject) {
        this.longObject = longObject;
    }
    
    @Override
    public long getLongType() {
        return longType;
    }

    @Override
    public void setLongType(long longType) {
        this.longType = longType;
    }
    
    @Override
    public Double getDoubleObject() {
        return doubleObject;
    }
    
    @Override
    public void setDoubleObject(Double doubleObject) {
        this.doubleObject = doubleObject;
    }
    
    @Override
    public double getDoubleType() {
        return doubleType;
    }
    
    @Override
    public void setDoubleType(double doubleType) {
        this.doubleType = doubleType;
    }
    
    @Override
    public Float getFloatObject() {
        return floatObject;
    }
    
    @Override
    public void setFloatObject(Float floatObject) {
        this.floatObject = floatObject;
    }
    
    @Override
    public float getFloatType() {
        return floatType;
    }
    
    @Override
    public void setFloatType(float floatType) {
        this.floatType = floatType;
    }
    
    @Override
    public Character getCharacterObject() {
        return characterObject;
    }
    
    @Override
    public void setCharacterObject(Character characterObject) {
        this.characterObject = characterObject;
    }
    
    @Override
    public char getCharType() {
        return charType;
    }
    
    @Override
    public void setCharType(char charType) {
        this.charType = charType;
    }
    
    @Override
    @Column(precision = 16, scale = 2)
    public BigDecimal getBigDecimalObject() {
        return bigDecimalObject;
    }

    @Override
    public void setBigDecimalObject(BigDecimal bigDecimalObject) {
        this.bigDecimalObject = bigDecimalObject;
    }
    
    @Override
    public BigInteger getBigIntegerObject() {
       return bigIntegerObject;
    }

    @Override
    public void setBigIntegerObject(BigInteger bigIntegerObject) {
        this.bigIntegerObject = bigIntegerObject;
    }
    
    @Override
    @CustomField(type = FieldType.Email)
    public String getEmail() {
        return email;
    }
    
    @Override
    public void setEmail(String email) {
        this.email = email;
    }
    
    @Override
    @CustomField(type = FieldType.Percent)
    public int getPercent() {
        return percent;
    }
    
    @Override
    public void setPercent(int percent) {
        this.percent = percent;
    }

    @Override
    @CustomField(label = "Date field", description = "This is a test for a data field")
    public Date getDate() {
        return date;
    }
    
    @Override
    public void setDate(Date date) {
        this.date = date;
    }
    
    @Override
    public Calendar getDateTimeCal() {
        return dateTimeCal;
    }
    
    @Override
    public void setDateTimeCal(Calendar dateTimeCal) {
        this.dateTimeCal = dateTimeCal;
    }
    
    @Override
    public GregorianCalendar getDateTimeGCal() {
        return dateTimeGCal;
    }
    
    @Override
    public void setDateTimeGCal(GregorianCalendar dateTimeGCal) {
        this.dateTimeGCal = dateTimeGCal;
    }
    
    /*
    public Time getTime() {
        return time;
    }
    
    public void setTime(Time time) {
        this.time = time;
    }
    */
    
    @Override
    @Temporal(TemporalType.DATE)
    public Date getDateTemporal() {
        return dateTemporal;
    }
    
    @Override
    public void setDateTemporal(Date dateTemporal) {
        this.dateTemporal = dateTemporal;
    }
    
    @Override
    @Temporal(TemporalType.TIMESTAMP)
    public Date getDateTimeTemporal() {
        return dateTimeTemporal;
    }
    
    @Override
    public void setDateTimeTemporal(Date dateTime) {
        this.dateTimeTemporal = dateTime;
    }

    /*
    public Date getTimeTemporal() {
        return timeTemporal;
    }
    
    public void setTimeTemporal(Date time) {
        this.timeTemporal = time;
    }
    */
   
    @Override
    @Column(name = "ParentTestEntity")
    @ManyToOne
    public ParentTestEntity getParent() {
        return parent;
    }

    @Override
    public void setParent(ParentTestEntity parent) {
        this.parent = parent;
    }

    @Override
    @Column(name = "User")
    @ManyToOne
    @CustomField(childRelationshipName = "TestEntitiesMethodAnnotationsForUser")
    public User getUserLookUp() {
        return userLookUp;
    }

    @Override
    public void setUserLookUp(User user) {
        this.userLookUp = user;
    }
    
    @Override
    @Column(name = "ParentTestEntity")
    @ManyToOne
    @CustomField(type = FieldType.MasterDetail, childRelationshipName = "TestEntitiesMethodAnnotationsMD", name = "ParentMD")
    public ParentTestEntity getParentMasterDetail() {
        return parentMasterDetail;
    }
    
    @Override
    public void setParentMasterDetail(ParentTestEntity parentMasterDetail) {
        this.parentMasterDetail = parentMasterDetail;
    }
    
    @Override
    @Enumerated
    public PickValues getPickValueDef() {
        return pickValueDef;
    }
    
    @Override
    public void setPickValueDef(PickValues pickValueDef) {
        this.pickValueDef = pickValueDef;
    }
    
    @Override
    @Enumerated(EnumType.STRING)
    public PickValues getPickValue() {
        return pickValue;
    }
    
    @Override
    public void setPickValue(PickValues pickValue) {
        this.pickValue = pickValue;
    }
    
    @Override
    @Enumerated(EnumType.ORDINAL)
    public PickValues getPickValueOrdinal() {
        return pickValueOrdinal;
    }
    
    @Override
    public void setPickValueOrdinal(PickValues pickValueOrdinal) {
        this.pickValueOrdinal = pickValueOrdinal;
    }
    
    @Override
    @CustomField(type = FieldType.Picklist)
    @PicklistValue(PickValues.class)
    public String getLiberalPickValueDef() {
        return liberalPickValueDef;
    }
    
    @Override
    public void setLiberalPickValueDef(String liberalPickValueDef) {
        this.liberalPickValueDef = liberalPickValueDef;
    }
    
    @Override
    @Enumerated
    public PickValues[] getPickValueMultiDef() {
        return pickValueMultiDef;
    }
    
    @Override
    public void setPickValueMultiDef(PickValues[] pickValueMultiDef) {
        this.pickValueMultiDef = pickValueMultiDef;
    }
    
    @Override
    @Enumerated(EnumType.STRING)
    public PickValues[] getPickValueMulti() {
        return pickValueMulti;
    }
    
    @Override
    public void setPickValueMulti(PickValues[] pickValueMulti) {
        this.pickValueMulti = pickValueMulti;
    }
    
    @Override
    @Enumerated(EnumType.ORDINAL)
    public PickValues[] getPickValueMultiOrdinal() {
        return pickValueMultiOrdinal;
    }
    
    @Override
    public void setPickValueMultiOrdinal(PickValues[] pickValueMultiOrdinal) {
        this.pickValueMultiOrdinal = pickValueMultiOrdinal;
    }
    
    @Override
    @Enumerated(EnumType.STRING)
    @PicklistValue(PickValues.class)
    public String[] getLiberalPickValueMultiDef() {
        return liberalPickValueMultiDef;
    }
    
    @Override
    public void setLiberalPickValueMultiDef(String[] liberalPickValueMultiDef) {
        this.liberalPickValueMultiDef = liberalPickValueMultiDef;
    }
    
    @Override
    @Basic
    public URL getUrl() {
        return url;
    }
    
    @Override
    public void setUrl(URL url) {
        this.url = url;
    }
    
    @Override
    @CustomField(type = FieldType.Phone)
    public String getPhone() {
        return phone;
    }
    
    @Override
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    @Override
    @Version
    public Calendar getLastModifiedDate() {
        return lastModifiedDate;
    }
    
    @Override
    public void setLastModifiedDate(Calendar lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
    
    @Override
    @Transient
    public String getUnused() {
        return unused;
    }
    
    @Override
    public void setUnused(String unused) {
        this.unused = unused;
    }
    
    @Override
    @CustomField(type = FieldType.AutoNumber, startValue = 100)
    public long getAutoNum() {
        return this.autoNum;
    }
    
    @Override
    public void setAutoNum(long autoNum) {
        this.autoNum = autoNum;
    }
    
    @Override
    @CustomField(type = FieldType.TextArea)
    public String getTextArea() {
        return textArea;
    }
    
    @Override
    public void setTextArea(String textArea) {
        this.textArea = textArea;
    }
    
    @Override
    @CustomField(type = FieldType.LongTextArea)
    public String getLongTextArea() {
        return longTextArea;
    }
    
    @Override
    public void setLongTextArea(String longTextArea) {
        this.longTextArea = longTextArea;
    }
    
    @Override
    @CustomField(type = FieldType.Html)
    public String getRichTextArea() {
        return richTextArea;
    }
    
    @Override
    public void setRichTextArea(String richTextArea) {
        this.richTextArea = richTextArea;
    }

    @Override
    @Embedded
    public EmbeddedTestEntity getEmbedded() {
        return embedded;
    }

    @Override
    public void setEmbedded(EmbeddedTestEntity embedded) {
        this.embedded = embedded;
    }
    
}

