/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)CocoDescriptionEntry.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package com.sun.encoder.coco.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.AnnotationDocument.Annotation;

import com.sun.encoder.coco.appinfo.CocoEncoding;
import com.sun.encoder.coco.appinfo.CocoEncoding.Sign;
import com.sun.encoder.coco.appinfo.CocoEncoding.Usage;
import com.sun.encoder.coco.runtime.messages.ErrorManager;
import com.sun.encoder.coco.runtime.messages.Message;
import com.sun.encoder.coco.runtime.messages.MessageCatalog;

/**
 * Represents a Cobol Copybook item description entry.
 *
 * @author  Noel Ang
 *
 */
public class CocoDescriptionEntry {

    /**
     * Enumeration of usage types.
     */
    public interface UsageType {
        public static final int BINARY = 1;
        public static final int COMP = 2; // synonymous with BINARY but requires unique value
        public static final int COMP1 = 21;
        public static final int COMP2 = 22;
        public static final int COMP3 = 23;
        public static final int COMP4 = 24;
        public static final int COMP5 = 25;
        public static final int DISPLAY = 3;
        public static final int DISPLAY1 = 31;
        public static final int PACDEC = 4;
        public static final int INDEX = 5;
    }

    public interface UsageTypeName {
        public static final String BINARY = "BINARY";
        public static final String COMP = "COMP"; 
        public static final String COMP1 = "COMP-1";
        public static final String COMP2 = "COMP-2";
        public static final String COMP3 = "COMP-3";
        public static final String COMP4 = "COMP-4";
        public static final String COMP5 = "COMP-5";
        public static final String DISPLAY = "DISPLAY";
        public static final String DISPLAY1 = "DISPLAY1";
        public static final String PACDEC = "PACKED-DECIMAL";
        public static final String INDEX = "INDEX";
    }

    public interface BarkKey {
        public static final String IS_ELEMENTARY = "Elementary?";
        public static final String IS_REDEFINITION = "Redefinition?";
        public static final String IS_REDEFINED = "Redefined?";
        public static final String IS_BLANK_WHEN_ZERO = "BlankWhenZero?";
        public static final String IS_JUSTIFIED = "Justified?";
        public static final String IS_SIGNED = "Signed?";
        public static final String IS_SIGN_SEPARATE = "SignIsSeparate?";
        public static final String IS_SIGN_LEADING = "SignIsLeading?";
        public static final String IS_SIGN_TRAILING = "SignIsTrailing?";
        public static final String LEVEL = "Level";
        public static final String OCCURS_DEPENDS = "OccursDependsOn";
        public static final String REDEFINITION_ID = "RedefinitionObject";
        public static final String LENGTH = "Length";
        public static final String MAX_OCCURS = "MaxOccurs";
        public static final String MIN_OCCURS = "MinOccurs";
        public static final String PICTURE = "Picture";
        public static final String PICTURE_CATEGORY = "PictureCategory";
        public static final String PICTURE_DECIMAL_POS = "PictureDecimalPos";
        public static final String PICTURE_DECIMAL_SCALING_SPAN =
            "PictureDecimalScalePositions";
        public static final String USAGE = "Usage";
        public static final String JAVATYPE = "JavaType";
        public static final String FQN = "FQN";
    }

    public enum JavaType {
        INT, LONG, STRING, BIGDEC, BYTEARRAY, FLOAT, DOUBLE
    }

    protected ArrayList mChildren;
    protected CocoDescriptionEntry mParent;
    protected String mName;
    protected String mOriginalName;
    protected String mFQN;
    protected int mLevel;
    protected String mIndicatorValue;
    protected boolean mIsContinuation;
    protected boolean mIsComment;
    protected boolean mIsBlankWhenZero;
    protected boolean mIsJustified;
    protected boolean mIsSigned;
    protected CocoDescriptionEntry mOccursDepends;
    protected int mOccursMax;
    protected int mOccursMin;

    // added begin (88778,88779) - for late resolution of DEPENDING ON data-name (might with qualifiers)
    protected String mOccursDependsName;
    protected List mOccursDependsQualifiers;
    // added end
    
    protected CocoPicture mPicture;
    protected CocoDescriptionEntry mRedefinedTarget;
    protected List mRedefinitions;
    protected int mUsage;
    protected CocoSign mSign;
    protected boolean mIsSetSign;
    protected boolean mIsSeparateSign;
    
    private boolean mIsUsageExplicit;
    private boolean mIsNameFiller;
    private boolean mIsNameBlank;

    // remember an reserved word encountered exception
    // so that it can be thrown later during parsing of an 
    // data item entry
    private CocoParseException mReservedWordAfterLevel;
    
    private final ErrorManager mErrorMgr =
            ErrorManager.getManager("OpenESB.encoder.COBOLCopybook."
                                    + getClass().getName());

    /**
     * Default constructor
     */
    public CocoDescriptionEntry() {
        mName            = "";
        mOriginalName    = "";
        mLevel           = -1;
        mIndicatorValue  = "";
        mPicture         = null;
        mRedefinedTarget = null;
        mOccursDepends   = null;
        mRedefinitions   = Collections.synchronizedList(new ArrayList());
        mIsContinuation  = false;
        mIsComment       = false;
        mIsBlankWhenZero = false;
        mIsJustified     = false;
        mUsage           = UsageType.DISPLAY;
        mOccursMin       = 1;
        mOccursMax       = 1;
        mParent          = null;
        mChildren        = new ArrayList();
        mIsSetSign       = false;
        mSign            = com.sun.encoder.coco.model.CocoSign.TrailingSign;
        mIsSeparateSign  = false;

        mIsUsageExplicit = false;
        mIsNameFiller    = false;
        mIsNameBlank     = false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // sb.append("CocoDescriptionEntry@").append(Integer.toHexString(hashCode()));
        sb.append(" name='").append(mName).append("'");
        if (mOriginalName != null && mOriginalName.length() > 0) {
            sb.append(" originalName='").append(mOriginalName).append("'");
        }
        if (mLevel > -1) {
            sb.append(" level=").append(mLevel);
        }
        sb.append(" usage=").append(getUsageName(mUsage));
        sb.append(" occursMin=").append(mOccursMin);
        sb.append(" occursMax=").append(mOccursMax);
        if (mIndicatorValue != null && mIndicatorValue.length() > 0) {
            sb.append(" indicator=").append(mIndicatorValue);
        }
        if (mPicture != null) {
            sb.append(" picture=[").append(mPicture).append("]");
        }
        if (mRedefinedTarget != null) {
            sb.append(" redefinedTarget=[").append(mRedefinedTarget).append("]");
        }
        if (mOccursDepends != null) {
            sb.append(" occursDepends=[").append(mOccursDepends).append("]");
        }
        if (mRedefinitions.size() > 0) {
            sb.append(" redefinitions=").append(mRedefinitions);
        }
        if (mIsContinuation) {
            sb.append(" IsContinuation");
        }
        if (mIsComment) {
            sb.append(" IsComment");
        }
        if (mIsBlankWhenZero) {
            sb.append(" IsBlankWhenZero");
        }
        if (mIsJustified) {
            sb.append(" IsJustified");
        }
        if (mParent != null) {
            sb.append(" parent=[").append(mParent).append("]");
        }
        if (mChildren != null && mChildren.size() > 0) {
            sb.append(" children.size=").append(mChildren.size());
        }
        if (mIsSetSign) {
            sb.append(" IsSetSign");
        }
        sb.append(" sign=").append(mSign);
        if (mIsSeparateSign) {
            sb.append(" IsSignSeparate");
        }
        if (mIsUsageExplicit) {
            sb.append(" IsUsageExplicit");
        }
        if (mIsNameFiller) {
            sb.append(" IsNameFiller");
        }
        if (mIsNameBlank) {
            sb.append(" IsNameBlank");
        }
        return sb.toString();
    }

    /**
     * Create an entry.
     *
     * @param  name  Name for entry
     *
     * @param  level Level number for entry
     */
    public CocoDescriptionEntry(String name, int level)
        throws IllegalArgumentException {

        if ((level < com.sun.encoder.coco.model.CocoLanguage.MIN_LEVEL_NUMBER
             || level > com.sun.encoder.coco.model.CocoLanguage.MAX_LEVEL_NUMBER)
            && (level != 66 && level != 77 && level != 88 )) {
            Message msg = MessageCatalog.getMessage("CCCB4108");
            String text = msg.formatText(new Object[] {
                String.valueOf(name),
                String.valueOf(level)
            });
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalArgumentException(text);
        }
        setName(name);

        mLevel           = level;
        mIndicatorValue  = "";
        mPicture         = null;
        mRedefinedTarget = null;
        mOccursDepends   = null;
        mRedefinitions   = Collections.synchronizedList(new ArrayList());
        mIsContinuation  = false;
        mIsComment       = false;
        mIsBlankWhenZero = false;
        mIsJustified     = false;
        mUsage           = UsageType.DISPLAY;
        mOccursMin       = 1;
        mOccursMax       = 1;
        mParent          = null;
        mChildren        = new ArrayList();
        mIsSigned        = false;
        mIsSetSign       = false;
        mSign            = com.sun.encoder.coco.model.CocoSign.TrailingSign;
        mIsSeparateSign  = false;

        mIsUsageExplicit   = false;
        mIsNameFiller = false;
        mIsNameBlank = false;
    }

    /**
     * Obtain name of entry.
     *
     * @return name specified for entry; blank string if no name assigned
     */
    public String getName() {
        return mName;
    }

     /**
     * Specify name for entry.
     *
     * @param  name Name for entry. If null, it is ignored and a blank name
     *         assigned instead.
     */
    public void setName(String name) {
        if (name != null) {
            name = name.trim();
            if (name.length() > 0) {
                mName = name;
            } else {
                mName = "";
            }
        } else {
            mName = "";
        }
    }

    /**
     * Gets the original name of the entry (whatever name read from the copybook
     * without any modification).
     *
     * @return the name of the entry (whatever name read from the copybook
     *          without any modification)
     */
    public String getOriginalName() {
        return mOriginalName;
    }

    /**
     * Sets the original name of the entry (whatever name read from the copybook
     * without any modification).
     *
     * @param originalName the original name of the entry
     */
    public void setOriginalName(String originalName) {
        if (originalName != null) {
            originalName = originalName.trim();
            if (originalName.length() > 0) {
                mOriginalName = originalName;
            } else {
                mOriginalName = "";
            }
        } else {
            mOriginalName = "";
        }
    }

    /**
     * Checks if the entry's name is FILLER.
     *
     * @return true if the entry's name is FILLER
     */
    public boolean isNameFiller() {
        return mIsNameFiller;
    }

    /**
     * Sets the isNameFiller flag.
     *
     * @param isFiller
     */
    public void setNameFiller(boolean isFiller) {
        mIsNameFiller = isFiller;
    }

    /**
     * Checks if the entry has a blank name.
     *
     * @return true is the name is blank
     */
    public boolean isNameBlank() {
        return mIsNameBlank;
    }

    /**
     * Sets the isNameBlank flag.
     *
     * @param isBlank
     */
    public void setNameBlank(boolean isBlank) {
        mIsNameBlank = isBlank;
    }

    /**
     * Obtain the FQN of the entry
     * @return the fully qualified name for the entry
     */
    public String getFQN() {
        return this.mFQN;
    }

    /**
     * set the FQN for the entry
     * @param name
     */
    public void setFQN(String name) {
        this.mFQN = name;
    }

    /**
     * Obtain level number of entry.
     *
     * @return level assigned to entry, or -1
     */
    public int getLevel() {
        return mLevel;
    }

    /**
     * Specify level for entry.
     *
     * @param  level Level number for entry
     *
     * @throws java.lang.IllegalArgumentException if the level number is illegal
     *
     * @see    com.sun.encoder.coco.model.CocoLanguage#MIN_LEVEL_NUMBER
     *
     * @see    com.sun.encoder.coco.model.CocoLanguage#MAX_LEVEL_NUMBER
     */
    public void setLevel(int level) throws IllegalArgumentException {
        if ((level < com.sun.encoder.coco.model.CocoLanguage.MIN_LEVEL_NUMBER
             || level > com.sun.encoder.coco.model.CocoLanguage.MAX_LEVEL_NUMBER)
            && (level != 66 && level != 77 && level != 88 )) {
            Message msg = MessageCatalog.getMessage("CCCB4108");
            String text = msg.formatText(new Object[] {
                String.valueOf(mName),
                String.valueOf(level)
            });
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalArgumentException(text);
        }
        mLevel = level;
    }

    /**
     * Obtain indicator value for entry.
     *
     * @return symbol specified for this entry via {@link #setIndicatorValue(java.lang.String)}
     *         or a blank string if no symbol was specified
     *
     * @see #setIndicatorValue(java.lang.String)
     */
    public String getIndicatorValue() {
        return mIndicatorValue;
    }

    /**
     * Specify the indicator symbol, if any, for this entry.  The indicator symbol
     * is the Indicator Area datum in a Cobol Copybook item description entry.
     *
     * @param value Indicator area symbol of this entry. If null is specified,
     *        it is ignored and a blank string is used instead.
     */
    public void setIndicatorValue(String value) {
        if (value == null) {
            mIndicatorValue = "";
        } else {
            mIndicatorValue = value;
        }
    }

    /**
     * Indicates whether this entry is a continuation of the/a previous entry, or not.
     *
     * @return true if the entry is a continuation, false if it is not
     */
    public boolean isContinuation() {
        return mIsContinuation;
    }

    /**
     * Specify whether this entry is a continuation (of the/a previous entry), or not.
     *
     * @param  is True to indicate this is a continuation entry, else false
     */
    public void setContinuation(boolean is) {
        mIsContinuation = is;
    }

    /**
     * Indicate whether this entry is a comment entry.
     *
     * @return true if the entry is a comment, false if it is not
     */
    public boolean isComment() {
        return mIsComment;
    }

    /**
     * Specify whether this entry is a comment.
     *
     * @param  is True to indicate this is a comment, else false
     */
    public void setComment(boolean is) {
        mIsComment = is;
    }

    /**
     * Indicate if this entry has specifies a BLANK WHEN ZERO clause.
     *
     * @return true if this entry specifies a BLANK WHEN ZERO clause, else false
     */
    public boolean isBlankWhenZero() {
        return mIsBlankWhenZero;
    }

    /**
     * Specify whether or not this entry specifies a BLANK WHEN ZERO clause.
     *
     * @param  is True to indicate BLANK WHEN ZERO, else false
     */
    public void setBlankWhenZero(boolean is) {
        mIsBlankWhenZero = is;
    }

    /**
     * Indicate if this entry has specifies a JUSTIFIED clause.
     *
     * @return true if this entry specifies a JUSTIFIED clause, else false
     */
    public boolean isJustified() {
        return mIsJustified;
    }

    /**
     * Specify whether or not this entry specifies a JUSTIFIED clause.
     *
     * @param  is True to indicate JUSTIFIED, else false
     */
    public void setJustified(boolean is) {
        mIsJustified = is;
    }

    /**
     * Get the USAGE type for entry.
     *
     * @return usage type as number
     *
     * @see UsageType
     */
    public int getUsage() {
        return mUsage;
    }

    /**
     * Specify the USAGE type for the entry.
     *
     * @param type type value
     *
     * @throws java.lang.IllegalArgumentException if the specified type is not valid
     *
     * @see UsageType
     */
    public void setUsage(int type) throws IllegalArgumentException {
        switch (type) {
            case UsageType.DISPLAY:
                mUsage = type;
                mIsUsageExplicit = true;
                break;
            case UsageType.DISPLAY1:
                mUsage = type;
                mIsUsageExplicit = true;
                break;
            case UsageType.BINARY:
            case UsageType.COMP:
                mUsage = type;
                mIsUsageExplicit = true;
                break;
            case UsageType.COMP1:
                if (mPicture != null) {
                    Message msg = MessageCatalog.getMessage("CCCB4109");
                    String text = msg.formatText(new Object[] {
                        getName(),
                    });
                    mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
                    throw new IllegalArgumentException(text);
                }
                mUsage = type;
                mIsUsageExplicit = true;
                break;
            case UsageType.COMP2:
                if (mPicture != null) {
                    Message msg = MessageCatalog.getMessage("CCCB4110");
                    String text = msg.formatText(new Object[] {
                        getName(),
                    });
                    mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
                    throw new IllegalArgumentException(text);
                }
                mUsage = type;
                mIsUsageExplicit = true;
                break;
            case UsageType.COMP3:
                mUsage = type;
                mIsUsageExplicit = true;
                break;
            case UsageType.COMP4:
                mUsage = type;
                mIsUsageExplicit = true;
                break;
            case UsageType.COMP5:
                mUsage = type;
                mIsUsageExplicit = true;
                break;
            case UsageType.PACDEC:
                mUsage = type;
                mIsUsageExplicit = true;
                break;
            case UsageType.INDEX:
                mUsage = type;
                mIsUsageExplicit = true;
                break;
            default:
                Message msg = MessageCatalog.getMessage("CCCB4112");
                String text = msg.formatText(new Object[] {
                    getName(),
                    String.valueOf(type)
                });
                mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
                throw new IllegalArgumentException(text);
        }
    }

    /**
     * Retrieve PICTURE clause character-string associated with this entry.
     *
     * @return PICTURE character-string, or a blank string if no entry has no PIC
     */
    public String getPicture() {
        String pic = "";
        if (mPicture != null) {
            pic = mPicture.getPicture();
        }
        return pic;
    }

    /**
     * Specify the PIC string to associate with this entry.
     *
     * @param  picture PIC string for entry
     *
     * @throws java.lang.IllegalArgumentException if the entry disallows a PIC string
     *         (e.g., COMP-1 and COMP-2 items)
     */
    public void setPicture(com.sun.encoder.coco.model.CocoPicture picture) throws IllegalArgumentException {
        if (mUsage == UsageType.COMP1 || mUsage == UsageType.COMP2) {
            if (picture != null) {
                Message msg = MessageCatalog.getMessage("CCCB4111");
                String text = msg.toString();
                mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
                throw new IllegalArgumentException(text);
            }
        }

        if (picture.getCategory() == CocoPicture.Category.DBCS) {
            mUsage = UsageType.DISPLAY1;
        }
        mPicture = picture;
        evaluateForSign();
    }

    /**
     * Compute the number of bytes an elementary entry occupies.
     *
     * @param picture an instance of CocoPicture
     * @param usage usage information as refering to UsageType
     * @param separateSign a boolean value indicating whether the sign is separate character
     *
     * @return size of the entry in bytes, which may be zero (0) if the entry
     *         does not have a picture, and its usage type requires one.
     */
    public static int computeElementarySize(CocoPicture picture, int usage, boolean separateSign) {
        int length = 0;
        if (picture == null) {
            switch (usage) {
            case UsageType.COMP1:
                length = 4;
                break;
            case UsageType.COMP2:
                length = 8;
                break;
            }
        } else if (picture.getCategory() == CocoPicture.Category.EX_FLOAT) {
            String pic   = picture.getPicture();
            int stopIdx  = pic.indexOf('E');
            int numBytes = 0;
            for (int i = 0; i < stopIdx; i++) {
                if ("9.".indexOf(pic.charAt(i)) != -1) {
                    numBytes++;
                }
            }
            length = numBytes + 5; // mantissa sign + 'E' + exponent sign + exponent 9(2)
        } else {
            String pic = picture.getPicture();
            boolean stop = false;

            for (int i = 0; i < pic.length() && !stop; i++) {

                char ch = pic.charAt(i);
                switch (ch) {
                case 'A':
                case 'E':
                case 'X':
                    length += 1;
                    break;
                case 'G':
                case 'N':
                    length += 2;
                    break;
                case 'B':
                    length += (picture.getCategory() == CocoPicture.Category.DBCS ? 2 : 1);
                    break;
                case 'S':
                    length += (separateSign ? 1 : 0);
                    break;
                case 'C': // CR
                case 'D': // DB
                    i += 1;
                    length += 2;
                    break;
                case 'V':
                case 'P':
                    length += 0;
                    break;
                default:
                    switch (usage) {
                    case UsageType.BINARY:
                    case UsageType.COMP:
                    case UsageType.COMP4:
                    case UsageType.COMP5: {
                        int digitCount = 0;
                        for (int j = 0; j < pic.length(); j++) {
                            char numch = pic.charAt(j);
                            if ("90Z".indexOf(numch) != -1 ) {
                                digitCount++;
                            }
                        }
                        if (digitCount > 0) {
                            if (digitCount < 5) {
                                length = 2;
                            } else if (digitCount < 10) {
                                length = 4;
                            } else {
                                length = 8;
                            }
                        }
                        stop = true;
                        break;
                    }

                    case UsageType.PACDEC:
                    case UsageType.COMP3: {
                        int digitCount = 0;
                        for (int j = 0; j < pic.length(); j++) {
                            if ("90Z".indexOf(pic.charAt(j)) != -1 ) {
                                digitCount++;
                            }
                        }
                        boolean oddNumbered = ((digitCount % 2) != 0);
                        length = (digitCount / 2) + (digitCount % 2);
                        length += (oddNumbered ? 0 : 1); // for sign nibble
                        stop = true;
                        break;
                    }

                    case UsageType.INDEX:
                        length = 4;
                        stop = true;
                        break;
                    case UsageType.DISPLAY:
                        length += 1;
                        break;
                    case UsageType.DISPLAY1:
                        length += 2;
                        break;
                    }
                    break;
                }
            }
        }
        return length;
    }

    /**
     * Retrieve number of bytes the entry occupies.
     * If the entry does not represent an elementary item, then the value it
     * yields is the sum of the sizes of its subordinate items.
     *
     * @return size of the entry in bytes, which may be zero (0) if the entry
     *         does not have a picture, and its usage type requires one.
     *
     * @see    #isElementary()
     */
    public int getSize() {

        /*
         * If item is elementary and repeating, return length of 1 occurence.
         * If item is elementary and non-repeating, return length.
         * If item is non-elementary, return sum length of its subordinates.
         */

        int length = 0;

        if (!isElementary()) {
            ListIterator it = mChildren.listIterator();
            while (it.hasNext()) {
                //Same logic in RuleNode's readRule() method
                CocoDescriptionEntry entry = (CocoDescriptionEntry) it.next();
                int multiplier = (entry.getOccursOn() == null
                                  ? entry.getMaximumOccurs() : 1);
                length += (entry.getSize() * multiplier);
            }

        } else {
            length = computeElementarySize(mPicture, mUsage, isSeparateSign());
        }
        return length;
    }

    /**
     * Determines a suitable Java type to represent the data of this entry. The
     * picture of the entry, as well as its usage type, affect the determination.
     *
     * @return Java type ordinal value mapped to {@link JavaType}
     *
     * @see JavaType
     */
    public JavaType getJavaType() {
        JavaType type;
        if (mPicture != null) {
            CocoPicture.Category category = mPicture.getCategory();
            switch (category) {
                case ALPHABETIC:
                    type = JavaType.STRING;
                    break;
                case ALPHANUMERIC:
                    type = JavaType.STRING;
                    break;
                case ALPHANUMERIC_EDITED:
                    type = JavaType.STRING;
                    break;
                case DBCS:
                    type = JavaType.BYTEARRAY;
                    break;
                case EX_FLOAT:
                    type = JavaType.BIGDEC;
                    break;
                case NUMERIC_EDITED:
                    type = JavaType.STRING;
                    break;
                case NUMERIC:
                    int decPos = mPicture.getDecimalPosition();
                    if (decPos > 0) {
                        type = JavaType.BIGDEC;
                    } else {
                        int numDigits = mPicture.countDigits();
                        if (decPos < 0) {
                            numDigits += Math.abs(decPos);
                        }
                        if (numDigits <= 9) {
                            type = JavaType.INT;
                        } else if (numDigits <= 18) {
                            type = JavaType.LONG;
                        } else {
                            type = JavaType.BIGDEC;
                        }
                    }
                    break;
                default: // Should not end up here!!
                    Message msg = MessageCatalog.getMessage("CCCB4113");
                    String text = msg.formatText(new Object[] {
                        getName(),
                        mPicture.getPicture()
                    });
                    mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
                    throw new RuntimeException(text);
            }
        } else {
            switch (mUsage) {
                case UsageType.COMP1:
                    type = JavaType.FLOAT;
                    break;
                case UsageType.COMP2:
                    type = JavaType.DOUBLE;
                    break;
                default:
                    type = JavaType.STRING;
            }
        }

        return type;
    }

    public static JavaType getJavaType(String typeName) {
        int trial = 1;

        if (typeName == null) {
            return null; // (-1);
        }

        while (trial != 0) {
            if (typeName.equals("int")) {
                return JavaType.INT;
            }
            if (typeName.equals("long")) {
                return JavaType.LONG;
            }
            if (typeName.equals("float")) {
                return JavaType.FLOAT;
            }
            if (typeName.equals("double")) {
                return JavaType.DOUBLE;
            }
            if (typeName.equals("BigDecimal") || typeName.equals("java.math.BigDecimal")) {
                return JavaType.BIGDEC;
            }
            if (typeName.equals("byte[]") || typeName.equals("byte []")) {
                return JavaType.BYTEARRAY;
            }
            if (typeName.equals("java.lang.String") || typeName.equals("String")) {
                return JavaType.STRING;
            }
            if (trial == 1) {
                typeName = typeName.trim();
                trial--;
            }
        }

        return null; // -1;
    }

    public String getJavaTypeName() {
        String javaType;

        switch (getJavaType()) {
            case BIGDEC:
                javaType = "java.math.BigDecimal";
                break;
            case BYTEARRAY:
                javaType = "byte[]";
                break;
            case INT:
                javaType = "int";
                break;
            case LONG:
                javaType = "long";
                break;
            case STRING:
                javaType = "java.lang.String";
                break;
            case DOUBLE:
                javaType = "double";
                break;
            case FLOAT:
                javaType = "float";
                break;
            default:
                javaType = "java.lang.String";
        }

        return javaType;
    }

    public static String getJavaTypeName(JavaType type) {
        String javaType;

        switch (type) {
            case INT:
                javaType = "int";
                break;
            case LONG:
                javaType = "long";
                break;
            case FLOAT:
                javaType = "float";
                break;
            case DOUBLE:
                javaType = "double";
                break;
            case BYTEARRAY:
                javaType = "byte[]";
                break;
            case STRING:
                javaType = "java.lang.String";
                break;
            case BIGDEC:
                javaType = "java.math.BigDecimal";
                break;
            default:
                javaType = null;
        }

        return javaType;
    }

    public boolean isPrimitiveJavaType() {
        switch (getJavaType()) {
            case BIGDEC:
                return false;
            case BYTEARRAY:
                return false;
            case INT:
                return true;
            case LONG:
                return true;
            case DOUBLE:
                return true;
            case FLOAT:
                return true;
            case STRING:
                return false;
            default:
                return false;
        }
    }

    public static boolean isPrimitiveJavaType(JavaType type) {
        switch (type) {
            case BIGDEC:
                return false;
            case BYTEARRAY:
                return false;
            case INT:
                return true;
            case LONG:
                return true;
            case DOUBLE:
                return true;
            case FLOAT:
                return true;
            case STRING:
                return false;
            default:
                return false;
        }
    }

    /**
     * Indicate if the entry is redefined by another entry.
     *
     * @return true if the entry is redefined by another (one or more) entry, else
     *         false
     */
    public boolean isRedefined() {
        return (mRedefinitions.size() > 0);
    }

    /**
     * Indicate if the entry is a redefinition of another entry.
     *
     * @return true if the entry redefines another entry, else false
     */
    public boolean isRedefinition() {
        return (mRedefinedTarget != null);
    }

    /**
     * Get the entry that this entry redefines.
     *
     * @return entry that this entry redefines, or null if this entry is not
     *         a redefinition
     */
    public CocoDescriptionEntry getRedefinedTarget() {
        return mRedefinedTarget;
    }

    /**
     * Determine number of redefinitions of this entry.
     *
     * @return number of redefinitions
     */
    public int countRedefinitions() {
        return mRedefinitions.size();
    }

    /**
     * Get redefinition of entry.
     *
     * @param  idx Ordinal of the desired redefinition, counting from 0
     *
     * @return the indicated redefining entry
     *
     * @throws java.lang.IndexOutOfBoundsException if idx < 0 or idx >= number of
     *         redefinitions associated with entry
     */
    public CocoDescriptionEntry getRedefinition(int idx)
        throws IndexOutOfBoundsException {
        return (CocoDescriptionEntry) mRedefinitions.get(idx);
    }

    /**
     * Specify the entry that this entry redefines.
     *
     * @param  entry The entry that this entry redefines
     *
     * @throws java.lang.IllegalArgumentException if the specified entry is null, or is
     *         the redefining entry itself, or is a redefinition itself, or has
     *         a level number that is not equal to this entry's level
     *
     * @throws java.lang.IllegalStateException if this entry already redefines another
     *         entry, and the specified entry is not it
     */
    public void setRedefinedTarget(CocoDescriptionEntry entry)
        throws IllegalArgumentException, IllegalStateException {

        if (entry == null) {
            throw new NullPointerException();
        }

        /* can't redefine myself */
        if (entry == this) {
            Message msg = MessageCatalog.getMessage("CCCB4114");
            String text = msg.formatText(new Object[] {
                getName(),
            });
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalArgumentException(text);
        }
        /* can't re-use redefine */
        if ((mRedefinedTarget != null) && (mRedefinedTarget != entry)) {
            Message msg = MessageCatalog.getMessage("CCCB4115");
            String text = msg.formatText(new Object[] {
                getName(),
                entry.getName(),
                mRedefinedTarget.getName()
            });
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalStateException(text);
        }

        /* can't redefine an entry of a different level */
        if (mLevel != entry.getLevel()) {
            Message msg = MessageCatalog.getMessage("CCCB4116");
            String text = msg.formatText(new Object[] {
                getName(),
                entry.getName(),
                String.valueOf(mLevel),
                String.valueOf(entry.getLevel())
            });
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalArgumentException(text);
        }

        /* If the item I am redefining, itself redefines something-else,
           I redefine that something-else directly */
        if (entry.isRedefinition()) {
            entry = entry.getRedefinedTarget();
        }
        mRedefinedTarget = entry;
        if (!entry.mRedefinitions.contains(this)) {
            entry.mRedefinitions.add(this);

            // Unless explicitly specified, a redefinition's usage
            // is equivalent to the usage of the non-elementary item
            // in whose scope its redefined item exists.
            //
            // 05 A COMP3
            //    10 B PIC S(5)
            //    10 C REDEFINES B PIC S(5)
            //    10 D REDEFINES B PIC 9(3) DISPLAY
            //
            // B is usage COMP3 because of A (covered elsewhere)
            // C is usage COMP3 also
            // D is usage DISPLAY because it is explicitly specified
            if (!mIsUsageExplicit) {
                CocoDescriptionEntry parentEntry = entry.getParent();
                if (null != parentEntry) {
                    setUsage(parentEntry.getUsage());
                    mIsUsageExplicit = false;
                }
            }
        }
    }

    /**
     * Retrieve the minimum number of times the data represented by this entry is
     * to occur.
     *
     * @return the minimum number of occurences of the entry's data
     */
    public int getMinimumOccurs() {
        return mOccursMin;
    }

    /**
     * Retrieve the maximum number of times the data represented by this entry is to
     * occur.
     *
     * @return the maximum number of occurences of the entry's data
     */
    public int getMaximumOccurs() {
        return mOccursMax;
    }

    /**
     * Retrieve the entry whose value determines how many times this entry is to
     * occur.
     *
     * @return the entry whose value determines the occurence count of this entry,
     *         or null if this entry has no such dependence
     */
    public CocoDescriptionEntry getOccursOn() {
        return mOccursDepends;
    }

    /**
     * get the data name after DEPENDING ON
     * @return the name (it is the base name part - qualifiers are in another attribute)
     */
    public String getDependOnName() {
        return this.mOccursDependsName;
    }

    /**
     * set the base name for the data name after DEPENDING ON
     * @param name - the base name
     */
    public void setDependOnName(String name) {
        this.mOccursDependsName = name;
    }

    /**
     * get the qualifiers for the data name after DEPENDING ON
     * @return the qualifiers;
     */
    public List getDependOnNameQualifiers() {
        return this.mOccursDependsQualifiers;
    }

    /**
     * set the qualifiers for the data name after DEPENDING ON
     * @param name - the qualifiers.
     */
    public void setDependOnNameQualifiers(List qualifiers) {
        this.mOccursDependsQualifiers = qualifiers;
    }

    /**
     * Specify the minimum and maximum occurence count of this entry.
     *
     * @param  count Occurence count
     *
     * @throws java.lang.IllegalArgumentException if count is not positive value
     */
    public void setOccurs(int count) throws IllegalArgumentException {
        if (count < 1) {
            Message msg = MessageCatalog.getMessage("CCCB4117");
            String text = msg.formatText(new Object[] {
                String.valueOf(count)
            });
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalArgumentException(text);
        }
        mOccursMin = count;
        mOccursMax = count;
        mOccursDepends = null;
    }

    /**
     * set the entry that this OCCURS entry depends on because of DEPENDING ON clause
     *
     */
    public void setDependsOnTarget(CocoDescriptionEntry entry) {
        this.mOccursDepends = entry;
    }

    /**
     * Assign sign information to the entry.
     *
     * @param  sign {@link com.sun.encoder.coco.model.CocoSign#LeadingSign} or {@link com.sun.encoder.coco.model.CocoSign#TrailingSign}.
     */
    public void setSign(com.sun.encoder.coco.model.CocoSign sign) {
        if (sign != null) {
            mIsSetSign = true;
            mSign = sign;
        }
        evaluateForSign();
    }

    /**
     * Specify whether or not this entry's sign is represented by a separate
     * symbol.
     *
     * @param  hasSeparateSign use true if the entry's sign is separate, false if it
     *                         is not.
     */
    public void setSeparateSign(boolean hasSeparateSign) {
        mIsSeparateSign = hasSeparateSign;
        evaluateForSign();
    }

    /**
     * Indicate whether or not this entry's sign is represented by a separate
     * symbol.
     *
     * @return  true if the entry's sign is separate, false if it is not
     */
    public boolean isSeparateSign() {
        return mIsSeparateSign;
    }

    public boolean isSigned() {
        return mIsSigned;
    }

    private void evaluateForSign() {
        mIsSigned = mPicture != null && (mPicture.getPicture().indexOf("S") != -1);
        if (!mIsSigned) {
            mIsSeparateSign = false;
        }
    }

    /**
     * Specify the minimum and maximum occurence count of this entry, and the
     * the entry whose value determines the actual occurence count for this entry.
     *
     * @param  low Minimum occurence count
     *
     * @param  high Maximum occurence count
     *
     * @param  depends Entry whose value determines this entry's actual occurence
     *         count
     *
     * @throws java.lang.IllegalArgumentException if low less than 1 when
     *         high == low, or if high < low, or when low or high is a negative
     *         value, or  when depends is null.
     */
    public void setOccurs(int low, int high, CocoDescriptionEntry depends)
        throws IllegalArgumentException {

        if (low < 1 && low == high) {
            Message msg = MessageCatalog.getMessage("CCCB4118");
            String text = msg.formatText(new Object[] {
                String.valueOf(low),
                String.valueOf(high)
            });
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalArgumentException(text);
        }

        if (low < 0) {
            Message msg = MessageCatalog.getMessage("CCCB4118");
            String text = msg.formatText(new Object[] {
                String.valueOf(low),
                String.valueOf(high)
            });
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalArgumentException(text);
        }

        if (high < 0) {
            Message msg = MessageCatalog.getMessage("CCCB4118");
            String text = msg.formatText(new Object[] {
                String.valueOf(low),
                String.valueOf(high)
            });
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalArgumentException(text);
        }

        if (high < low) {
            Message msg = MessageCatalog.getMessage("CCCB4118");
            String text = msg.formatText(new Object[] {
                String.valueOf(low),
                String.valueOf(high)
            });
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalArgumentException(text);
        }
        if (null == depends) {
            Message msg = MessageCatalog.getMessage("CCCB4119");
            String text = msg.toString();
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalArgumentException(text);
        }
        this.mOccursDependsName = null;
        this.mOccursDependsQualifiers = null;
        mOccursMin = low;
        mOccursMax = high;
        mOccursDepends = depends;
    }

    /**
     * set OCCURS information for the entry but leave the DEPENDING ON target resolved later
     *
     * @param low
     * @param high
     * @param qualifiers
     * @param depend_on_name
     * @throws IllegalArgumentException
     */
    public void setOccursResolveLater(int low, int high, List qualifiers,
            String depend_on_name)
        throws IllegalArgumentException {

        if (low < 1 && low == high) {
            Message msg = MessageCatalog.getMessage("CCCB4118");
            String text = msg.formatText(new Object[]{
                        String.valueOf(low),
                        String.valueOf(high)
                    });
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalArgumentException(text);
        }

        if (low < 0) {
            Message msg = MessageCatalog.getMessage("CCCB4118");
            String text = msg.formatText(new Object[]{
                        String.valueOf(low),
                        String.valueOf(high)
                    });
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalArgumentException(text);
        }

        if (high < 0) {
            Message msg = MessageCatalog.getMessage("CCCB4118");
            String text = msg.formatText(new Object[]{
                        String.valueOf(low),
                        String.valueOf(high)
                    });
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalArgumentException(text);
        }

        if (high < low) {
            Message msg = MessageCatalog.getMessage("CCCB4118");
            String text = msg.formatText(new Object[]{
                        String.valueOf(low),
                        String.valueOf(high)
                    });
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalArgumentException(text);
        }

        //if (null == depends) {
        //    Message msg = MessageCatalog.getMessage("CCCB4119");
        //    String text = msg.toString();
        //    mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
        //    throw new IllegalArgumentException(text);
        //}

        String normalName = "";
        if (depend_on_name != null) {
            normalName = depend_on_name.toUpperCase();
        }
        mOccursDependsName = normalName;
        mOccursDependsQualifiers = normalizeQualifiers(qualifiers);
        mOccursMin = low;
        mOccursMax = high;
        mOccursDepends = null;
    }


    /**
     * helper to upper case all the qualifier names;
     * @return
     */
    private List normalizeQualifiers(List qualifiers) {
        if ( qualifiers != null && qualifiers.size() > 0 ) {
            for ( int i = 0; i < qualifiers.size(); i++ ) {
                String qn = (String)qualifiers.get(i);
                if ( qn != null )
                    qualifiers.set(i, qn.toUpperCase());
            }
        }
        return qualifiers;
    }

    /**
     * Get the parent of this entry, if any.
     *
     * @return parent of this entry, or null, if none is designated
     */
    public CocoDescriptionEntry getParent() {
        return mParent;
    }

    /**
     * Specify the parent of this entry.
     *
     * @param  entry Parent for this entry
     *
     * @throws java.lang.IllegalArgumentException if the parent specified is this entry, or if
     *         the parent is already a child of this entry
     */
    public void setParent(CocoDescriptionEntry entry) throws IllegalArgumentException {
        if (entry == this) {
            Message msg = MessageCatalog.getMessage("CCCB4120");
            String text = msg.formatText(new Object[] {
                getName()
            });
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalArgumentException(text);
        }
        if (mChildren.contains(entry)) {
            Message msg = MessageCatalog.getMessage("CCCB4121");
            String text = msg.formatText(new Object[] {
                entry.getName(),
                getName()
            });
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalArgumentException(text);
        }
        mParent = entry;
    }

    /**
     * Indicate whether or not the entry is an Cobol elementary item. An elementary
     * item is one without subordinates.
     *
     * @return true if the entry is an elementary item
     */
    public boolean isElementary() {
        return (mChildren.size() == 0);
    }

    /**
     * Determine the number of immediate children of this entry.
     *
     * @return the number of children for this entry
     */
    public int countChildren() {
        return mChildren.size();
    }

    /**
     * Retrieve an immediate child entry of this entry.
     *
     * @param  idx Ordinal of the wanted child, counting from 0
     *
     * @return the indicated child entry
     *
     * @throws java.lang.IndexOutOfBoundsException if idx < 0 or idx >= number of children
     */
    public CocoDescriptionEntry getChild(int idx) throws IndexOutOfBoundsException {
        return (CocoDescriptionEntry) mChildren.get(idx);
    }

    /**
     * Add an entry as a child of this entry. The specified entry may or may not
     * become an immediate child.  If this entry already possesses an immediate
     * child A whose level number is numerically lower than the specified entry B,
     * then B is added a child of A, with the above rules applied (recursively).
     *
     * @param  entry the entry to make as child of this entry
     *
     * @throws java.lang.IllegalArgumentException if the specified entry is this entry,
     *         or null, or is already an immediate child of this entry, or has
     *         a level number numerically equal or lower than this entry
     */
    public void addChild(CocoDescriptionEntry entry) throws IllegalArgumentException {

        if (entry == this || entry == null) {
            throw new NullPointerException();
        }
        if (mChildren.contains(entry)) {
            return;
        }
        if (entry == mParent || entry.getLevel() <= mLevel) {
            Message msg = MessageCatalog.getMessage("CCCB4122");
            String text = msg.formatText(new Object[] {
                entry.getName(),
                getName()
            });
            mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
            throw new IllegalArgumentException(text);
        }
        int idx = mChildren.size() - 1;
        if (idx < 0) {
            if (mIsUsageExplicit) {
                entry.setUsage(mUsage);
            }
            mChildren.add(entry);
            entry.setParent(this);
        } else {
            CocoDescriptionEntry lastChild = (CocoDescriptionEntry) mChildren.get(idx);
            if (lastChild.getLevel() < entry.getLevel()) {
                lastChild.addChild(entry);
            } else {
                if (mIsUsageExplicit) {
                    entry.setUsage(mUsage);
                }
                mChildren.add(entry);
                entry.setParent(this);
            }
        }
    }

    /**
     * error reporting helper
     * @return
     */
    public String getInfo() {
        return this.getLevel() + " " + this.getName() + " FQN = " + this.getFQN();
    }

    /**
     * Export entry information to an element
     *
     * @param elem element to which to export information
     */
    public CocoEncoding toElement(Element elem) {

        if (isElementary()) {
            if (mIsUsageExplicit && mUsage != UsageType.DISPLAY
                    && mUsage != UsageType.DISPLAY1) {
                switch (mUsage) {
                case UsageType.BINARY:
                case UsageType.PACDEC: //packed decimal (BCD)
                case UsageType.COMP:
                case UsageType.COMP3:  //equivalent to packed decimal
                case UsageType.COMP4:  //equivalent to binary
                case UsageType.COMP5:  //native binary
                    if (mPicture == null) {
                        elem.setType(
                                new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI,
                                        "long"));
                        break;
                    }
                    setBestFitDecimalType(elem, mPicture);
                    break;
                case UsageType.COMP1:  //single precision floating point
                    elem.setType(
                            new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI,
                                    "float"));
                    break;
                case UsageType.COMP2:  //double precision floating point
                    elem.setType(
                            new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI,
                                    "double"));
                    break;
                case UsageType.INDEX:
                    elem.setType(
                            new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "long"));
                    break;
                default:
                    //not possible
                    Message msg = MessageCatalog.getMessage("CCCB4015");
                    String text = msg.formatText(new Object[] {mUsage});
                    mErrorMgr.log(ErrorManager.Severity.ERROR, null, text);
                    throw new IllegalArgumentException(text);
                }
            } else {
                //Assume display
                if (mPicture == null) {
                    elem.setType(
                            new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI,
                                    "string"));
                } else if (mPicture.getCategory() == CocoPicture.Category.NUMERIC){
                    setBestFitDecimalType(elem, mPicture);
                } else {
                    elem.setType(
                            new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI,
                                    "string"));
                }
            }
        }

        CocoEncoding encoding = CocoEncoding.Factory.newInstance();

        if (mIsNameFiller) {
            encoding.setNameFiller("");
        } else if (mIsNameBlank) {
            encoding.setNameBlank("");
        } else {
            encoding.setOriginalName(mOriginalName);
        }

        if (mIsBlankWhenZero) {
            encoding.setBlankWhenZero("");
        }

        if (mIsJustified) {
            encoding.setJustified("");
        }

        if (mIsSetSign) {
            Sign sign = encoding.addNewSign();
            if (mSign == com.sun.encoder.coco.model.CocoSign.LeadingSign) {
                sign.setLeading("");
            } else if (mSign == com.sun.encoder.coco.model.CocoSign.TrailingSign) {
                sign.setTrailing("");
            }
            if (isSeparateSign()) {
                sign.setSeparate("");
            }
        }

        if (mPicture != null) {
            encoding.setPicture(mPicture.getOriginalPicture());
        }

        encoding.setLevel((short) mLevel);

        if (mIsUsageExplicit) {
            Usage usage = encoding.addNewUsage();
            switch (mUsage) {
            case UsageType.BINARY:
                usage.setBinary("");  //the tag must be empty. Only the presence of
                                      //the tag is relevent.
                break;
            case UsageType.COMP:
                usage.setComp("");
                break;
            case UsageType.COMP1:
                usage.setComp1("");
                break;
            case UsageType.COMP2:
                usage.setComp2("");
                break;
            case UsageType.COMP3:
                usage.setComp3("");
                break;
            case UsageType.COMP4:
                usage.setComp4("");
                break;
            case UsageType.COMP5:
                usage.setComp5("");
                break;
            case UsageType.DISPLAY:
                usage.setDisplay("");
                break;
            case UsageType.DISPLAY1:
                usage.setDisplay1("");
                break;
            case UsageType.INDEX:
                usage.setIndex("");
                break;
            case UsageType.PACDEC:
                usage.setPackedDecimal("");
                break;
            }
        }
        encoding.setSource("urn:com.sun:encoder");

        Annotation anno;
        if (elem.isSetAnnotation()) {
            anno = elem.getAnnotation();
        } else {
            anno = elem.addNewAnnotation();
        }
        if (anno.sizeOfAppinfoArray() == 0) {
            anno.addNewAppinfo();
        }
        anno.getAppinfoArray(0).set(encoding);

        return encoding;
    }

    private void setBestFitDecimalType(Element elem, CocoPicture picture) {
        if (picture.getDecimalPosition() <= 0) {
            int digits = picture.countDigits() - picture.getDecimalPosition();
            if (digits > 9) {
                elem.setType(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "long"));
            } else if (digits > 4) {
                elem.setType(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "int"));
            } else {
                elem.setType(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "short"));
            }
        } else {
            elem.setType(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "decimal"));
        }
    }

    public void setReservedWordAfterLevel(CocoParseException e) {
        this.mReservedWordAfterLevel = e;
    }

    public CocoParseException getReservedWordAfterLevel() {
        return this.mReservedWordAfterLevel;
    }

    public boolean usageExplicit() {
        return this.mIsUsageExplicit;
    }

    public String getUsageName(int type) {
        String name = "Unknown";
        switch (type) {
        case UsageType.COMP:
            name = UsageTypeName.COMP;
            break;
        case UsageType.COMP1:
            name = UsageTypeName.COMP1;
            break;
        case UsageType.COMP2:
            name = UsageTypeName.COMP2;
            break;
        case UsageType.COMP3:
            name = UsageTypeName.COMP3;
            break;
        case UsageType.COMP4:
            name = UsageTypeName.COMP4;
            break;
        case UsageType.COMP5:
            name = UsageTypeName.COMP5;
            break;
        case UsageType.BINARY:
            name = UsageTypeName.BINARY;
            break;
        case UsageType.DISPLAY:
            name = UsageTypeName.DISPLAY;
            break;
        case UsageType.DISPLAY1:
            name = UsageTypeName.DISPLAY1;
            break;
        case UsageType.INDEX:
            name = UsageTypeName.INDEX;
            break;
        case UsageType.PACDEC:
            name = UsageTypeName.PACDEC;
            break;
        default:
        }
        return name;
    }
}
/* EOF $RCSfile: CocoDescriptionEntry.java,v $ */

