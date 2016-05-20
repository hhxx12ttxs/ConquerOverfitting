/**
 * Copyright 2009 - Morten Udn??s
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.javabatch.fileio;

import static fitnesse.util.ListUtility.list;

import java.util.Arrays;
import java.util.List;

import com.javabatch.petstore.records.CustomerRecord;
/**
 * Contains field defintion used by record classes.
 * @author eco
 *
 */
public class Field {

    /** Starting posistion of field. */
    private final int       startPos;
    /** Length of field. */
    private final int       length;
    /** Field type (TEXT, NUMBER). */
    private final FieldType fieldType;

    /* TODO
    private final boolean   mandatory;
    private final boolean   validate;
    */

    /** String to be used as field separator (usually tab or , ). */
    private final String    fieldSeparator;
    /** Field number in record. */
    private final int       fieldNumber;
    /** Indicates fixed record length. */
    private final boolean   fixedRecord;

    /** Padding with space. */
    private static final byte SPACE = (byte) 32;
    /** Paddingn with leading zeros. */
    private static final byte ZERO =  (byte) 48;

    public Field(int fieldNumber, int startPos, int length,
            FieldType fieldType) {
        this.startPos = startPos;
        this.length = length;
        this.fieldType = fieldType;
        this.fieldSeparator = null;
        this.fieldNumber = fieldNumber;
        this.fixedRecord = true;
    }

    public Field(int fieldNumber, String fieldSeparator, FieldType fieldType) {
        this.startPos = -1;
        this.length = -1;
        this.fieldType = fieldType;
        this.fieldSeparator = fieldSeparator;
        this.fieldNumber = fieldNumber;
        this.fixedRecord = false;
    }



    public int getStartPos() {
        return startPos;
    }

    public int getLength() {
        return length;
    }

    public boolean isFixedRecord() {
        return fixedRecord;
    }

    private byte[] parseUnsupported(FileBuffer buffer, String fieldName,
            List<FieldError> validationErrors) {
        byte[] recordValue = buffer.getBytes(this.startPos, this.length);
        validationErrors.add(new FieldError(this, recordValue,
                FieldError.FIELD_LENGTH_ERROR, fieldName,
                "Length was: " + recordValue.length
                        + "  expected length was: " + this.length));

        return buffer.getBytes();
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%1$#" + n + "s", s);
    }

    private byte[] parseText(FileBuffer buffer, String fieldName,
            List<FieldError> validationErrors) {
        byte[] recordValue = null;
        String validationString = null;

        try {
            if (this.fixedRecord) {
                recordValue = buffer.getBytes(this.startPos, this.length);
                validationString = new String(recordValue);

                if (recordValue.length < this.length) {
                    validationErrors.add(new FieldError(this, recordValue,
                            FieldError.FIELD_LENGTH_ERROR, fieldName,
                            "Length was: " + recordValue.length
                                    + "  expected length was: " + this.length));
                    return null;
                }

            } else {
                // Using easy way of doing this. It can be done faster with raw
                // byte[] operations
                validationString = buffer.toString()
                        .split(this.fieldSeparator)[this.fieldNumber];
                validationString.trim();
                recordValue = validationString.getBytes();
            }
        } catch (Exception e) {
            validationErrors.add(new FieldError(this, recordValue,
                    FieldError.FIELD_FORMAT_ERROR, fieldName,
                    "Invalid value read from file"));
            return null;
        }
        return recordValue;
    }

    private byte[] parseNumber(FileBuffer buffer, String fieldName,
            List<FieldError> validationErrors) {
        byte[] recordValue = null;

        try {
            if (this.fixedRecord) {
                recordValue = buffer.getBytes(this.startPos, this.length);

                // See if its possible to create a long from the read number
                new Long(new String(recordValue));

                if (recordValue.length < this.length) {
                    validationErrors.add(new FieldError(this, recordValue,
                            FieldError.FIELD_LENGTH_ERROR, fieldName,
                            "Length was: " + recordValue.length));
                    return null;
                }

            } else {
                // Using easy way of doing this. It can be done faster with raw
                // byte[] operations
                String stringValue = buffer.toString()
                        .split(this.fieldSeparator)[this.fieldNumber];

                if (stringValue.length() < this.length) {
                    padRight(stringValue, this.length - stringValue.length());
                }

                // See if its possible to create a long from the read number
                new Long(stringValue);
                stringValue = stringValue.replaceAll("^0*", "");
                recordValue = stringValue.getBytes();
            }
            return recordValue;
        } catch (Exception e) {
            validationErrors.add(new FieldError(this, recordValue,
                    FieldError.FIELD_FORMAT_ERROR, fieldName,
                    "Invalid value read from file"));
            return null;
        }

    }

    public byte[] parseField(FileBuffer inputRecord, String fieldName,
            List<FieldError> validationErrors) {

        switch (this.fieldType) {
        case TEXT:
            return parseText(inputRecord, fieldName, validationErrors);
        case NUMBER:
            return parseNumber(inputRecord, fieldName, validationErrors);
        default:
            return parseUnsupported(inputRecord, fieldName, validationErrors);
        }

    }

    public static void addFieldToRecord(String fieldValue, Field field,
            FileBuffer outRecord) {
        addFieldToRecord(fieldValue.getBytes(), field, outRecord);
    }

    public static void addFieldToRecord(byte[] fieldValue, Field field,
            FileBuffer outRecord) {

        if (field.isFixedRecord()) {
            byte[] paddedEmptyField = new byte[field.length];

            if (fieldValue.length > field.length) {
                throw new IllegalArgumentException("Fieldvalue is to long. Max lenght is: " + field.length + " Found length was: "
                        + fieldValue.length + "  Field content was: " + new String(fieldValue));
            } else if (fieldValue.length == field.length) {
                paddedEmptyField = fieldValue;
            } else if (fieldValue.length < field.length) {
                if (field.fieldType == FieldType.TEXT) {
                    Arrays.fill(paddedEmptyField, SPACE);
                    System.arraycopy(fieldValue, 0, paddedEmptyField, 0, fieldValue.length);
                } else {
                    Arrays.fill(paddedEmptyField, ZERO);
                    System.arraycopy(fieldValue, 0, paddedEmptyField, paddedEmptyField.length - fieldValue.length,
                            fieldValue.length);
                }
            }

            outRecord.addBytes(paddedEmptyField);
        } else {
            /* No need for fieldseparator before the first field in record*/
            if (field.fieldNumber == 0) {
                outRecord.addBytes(fieldValue);
            } else {
               outRecord.addBytes(fieldValue, field.fieldSeparator);
            }
        }
    }
}

