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
 * @(#)CobolDataConverter.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */
package com.sun.encoder.coco.runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.sun.encoder.coco.runtime.messages.ErrorManager;
import com.sun.encoder.coco.runtime.messages.Message;
import com.sun.encoder.coco.runtime.messages.MessageCatalog;

/**
 * Principal class for converting between Copybook data and Java data
 * reprseentations.
 *
 * @author Noel Ang
 * @version $Revision: 1.4 $
 */
public class CobolDataConverter {

    private static final int MAX_EXFLOAT_EXPONENT = 99;
    private static final byte LIT_BYTE = (byte) 0xFF;
    private static final byte ZONE_NYBBLE_SIGN_BYTEMASK = (byte) 0xF0;
    private static final byte ZONE_NYBBLE_VALUE_BYTEMASK = (byte) 0x0F;
    private static final byte PACKED_NYBBLE_SIGN_BYTEMASK = (byte) 0x0F;
    private static final byte POSITIVE_ZONE_SIGN = (byte) 0xC0;
    private static final byte NEGATIVE_ZONE_SIGN = (byte) 0xD0;
    private static final byte UNSIGNED_ZONE_SIGN = (byte) 0xF0;
    private static final byte POSITIVE_PACK_SIGN = 0x0C;
    private static final byte NEGATIVE_PACK_SIGN = 0x0D;
    private static final byte UNSIGNED_PACK_SIGN = 0x0F;
    private static final byte[] DBCSSPACE = {0x40, 0x40};
    private static final char[] SPACE = {' '};
    private static final char[] PLUS = {'+'};
    private static final char[] MINUS = {'-'};
    private static final Map<String, byte[]> mSpaceEncodings =
            Collections.synchronizedMap(new HashMap<String, byte[]>());
    private static final Map<String, byte[]> mPlusEncodings =
            Collections.synchronizedMap(new HashMap<String, byte[]>());
    private static final Map<String, byte[]> mMinusEncodings =
            Collections.synchronizedMap(new HashMap<String, byte[]>());
    private static final ErrorManager cErrorMgr =
            ErrorManager.getManager("OpenESB.encoder.COBOLCopybook." + CobolDataConverter.class.getName());

    private CobolDataConverter() {
    }

    /**
     * Write a value as a Cobol display usage item. Use for alphabetic,
     * alphanumeric, alphanumeric-edited, and numeric-edited items. If the
     * value's size is less than the item size, it is padded with trailing space
     * characters.
     *
     * @param outStream Outlet for data
     * @param data   The value to write
     * @param spec   Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     * @param enc    Encoding to use for the output
     *
     * @throws IOException if an I/O error occurs in writing the value,
     *                             or if the supplied writer does not use the
     *                             required character encoding
     */
    public static void encodeToDisplay(OutputStream outStream,
            String data,
            CobolCharacteristics specs,
            String enc)
            throws IOException {

        final int size;

        OutputStreamWriter writer = new OutputStreamWriter(outStream, enc);
        size = specs.getSize();

        if (data.length() > size) {
            Message msg = MessageCatalog.getMessage("CCCR3002");
            cErrorMgr.log(ErrorManager.Severity.WARN,
                    null,
                    msg.formatText(new Object[]{data, String.valueOf(size)}));
            data = data.substring(0, size);
        }
        writer.write(data);
        writer.flush();
        for (int i = 0, pad = size - data.length(); i < pad; i++) {
            writer.write(' ');
        }
    }

    /**
     * Write a value as an external floating point (display usage) item.
     *
     * @param outStream  Outlet for data
     * @param data    Value to write
     * @param picture Cobol picture string associated with the item
     * @param spec    Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     * @param enc     Encoding to use for the output
     *
     * @throws IOException if an I/O error occurs in writing the value,
     *                             or if the supplied writer does not use the
     *                             required character encoding
     * @throws ArithmeticException if the data exponent is too large
     */
    public static void encodeToExternalFloat(OutputStream outStream,
            BigDecimal data,
            String picture,
            CobolCharacteristics specs,
            String enc)
            throws IOException {

        final StringBuffer value = new StringBuffer(data.unscaledValue().toString());
        final int scalePic = specs.getDecimalPosition();
        int scale = data.scale();
        int exponent = 0 - scale;
        int digitsPic = 0;
        int digits = value.length();

        OutputStreamWriter writer = new OutputStreamWriter(outStream, enc);
        /* calc picture's digit requirements */
        digitsPic = 0;
        for (int i = 0;
                i < picture.length() && picture.charAt(i) != 'E'; i++) {
            if (picture.charAt(i) == '9') {
                digitsPic++;
            }
        }

        /* remove sign from value */
        if (data.signum() == -1) {
            value.delete(0, 1);
            digits--;
        }

        // truncate if needed
        if (digitsPic < digits) {
            int snip = digits - digitsPic;
            value.delete(digits - snip, digits);
            scale = Math.max(0, scale - snip);
            exponent += snip;
            digits = value.length();
        }

        // pad if needed
        if (digits < digitsPic) {
            int snip = digitsPic - digits;
            for (int i = 0; i < snip; i++) {
                value.append('0');
            }
            scale += (scale > 0 ? snip : 0);
            exponent -= snip;
            digits = value.length();
        }

        // reconcile data and picture scales;
        // compensate for their decimal positions' misalignment
        exponent = scalePic + exponent;

        // adjust exponent now that all padding, truncation, and
        // decimal point insertion are completed
        int dotLoc = picture.indexOf('.');
        boolean haveExplicitDecimal = (dotLoc != -1);

        // picture offset - 1 == value offset
        if (haveExplicitDecimal) {
            value.insert(dotLoc - 1, '.');
        }

        // exponent range check
        if (Math.abs(exponent) > MAX_EXFLOAT_EXPONENT) {
            Message msg = MessageCatalog.getMessage("CCCR4009");
            String err = msg.formatText(new Object[]{
                        String.valueOf(exponent)
                    });
            cErrorMgr.log(ErrorManager.Severity.ERROR, null, err);
            throw new ArithmeticException(err);
        }

        // write sign
        boolean negValue = (data.signum() == -1);
        boolean needSignificandSign = !Character.isDigit(picture.charAt(0));
        boolean explicitSignificandSign = needSignificandSign && picture.charAt(0) == '+';
        if (needSignificandSign) {
            if (negValue && new BigDecimal(
                    value.toString()).unscaledValue().intValue() != 0) {
                writer.write('-');
            } else if (explicitSignificandSign) {
                writer.write('+');
            }
        }

        // write significand and exponent
        boolean explicitExponentSign = picture.charAt(picture.indexOf('E') + 1) == '+';
        writer.write(value.toString() + 'E');
        if (exponent < 0) {
            writer.write('-');
        } else if (explicitExponentSign) {
            writer.write('+');
        }
        exponent = Math.abs(exponent);
        if (exponent < 10) {
            writer.write('0');
        }
        writer.write(Integer.toString(exponent));
        writer.flush();
    }

    /**
     * Write a value as a Cobol COMP-1 usage, floating point item.
     *
     * @param stream Outlet for data
     * @param val    The value to write
     *
     * @throws IOException if an I/O error occurs in writing the value
     */
    public static void encodeToFloat(OutputStream stream,
            float val)
            throws IOException {

        int value = Float.floatToIntBits(val);
        byte[] bytes = new byte[]{
            (byte) ((value & 0xFF000000) >> 24),
            (byte) ((value & 0x00FF0000) >> 16),
            (byte) ((value & 0x0000FF00) >> 8),
            (byte) (value & 0x000000FF)
        };

        stream.write(bytes, 0, bytes.length);
        stream.flush();
    }

    /**
     * Write a value as a Cobol COMP-2 usage, floating point item.
     *
     * @param stream Outlet for data
     * @param val    The value to write
     *
     * @throws IOException if an I/O error occurs in writing the value
     */
    public static void encodeToDouble(OutputStream stream,
            double val)
            throws IOException {

        long value = Double.doubleToLongBits(val);
        byte[] bytes = new byte[]{
            (byte) ((value & 0xFF00000000000000L) >> 56),
            (byte) ((value & 0x00FF000000000000L) >> 48),
            (byte) ((value & 0x0000FF0000000000L) >> 40),
            (byte) ((value & 0x000000FF00000000L) >> 32),
            (byte) ((value & 0x00000000FF000000L) >> 24),
            (byte) ((value & 0x0000000000FF0000L) >> 16),
            (byte) ((value & 0x000000000000FF00L) >> 8),
            (byte) (value & 0x00000000000000FFL)
        };

        stream.write(bytes, 0, bytes.length);
        stream.flush();
    }

    /**
     * Write a value as a Cobol display usage, zoned item. Use for external
     * decimal (zoned) items.
     *
     * @param stream  Outlet for data
     * @param data    The value to write
     * @param picture Cobol item picture for the value
     * @param spec    Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     * @param enc     Encoding to use for the output
     *
     * @throws IOException if an I/O error occurs in writing the value
     */
    public static void encodeToZoned(OutputStream stream,
            BigDecimal data,
            String picture,
            CobolCharacteristics specs,
            String enc)
            throws IOException {

        final StringBuffer value = new StringBuffer(data.unscaledValue().toString());
        final boolean needSign = specs.isSigned();
        final boolean separateSign = specs.isSignSeparate();
        final boolean leadingSign = specs.isSignLeading();
        final int scalePic = specs.getDecimalPosition();
        final int scalingPositions = specs.getDecimalScalingPositions();
        int digitsPic;
        byte signByte = 0;
        int scale;

        // Count number of digits in the picture
        digitsPic = 0;
        for (int i = 0, len = picture.length(); i < len; i++) {
            if (picture.charAt(i) == '9') {
                digitsPic += 1;
            }
        }

        // Scale value to its picture
        scale = data.scale();
        scale =
                fitToNumericPicture(value,
                scale,
                digitsPic,
                scalePic,
                scalingPositions);

        // Remove negative sign for convenience
        if (data.signum() == -1) {
            value.delete(0, 1);
        }

        // Add sign
        if (needSign) {

            // Two ways to add it:
            // Prepend/append if separate sign requested,
            if (separateSign) {
                if (leadingSign) {
                    switch (data.signum()) {
                        case -1:
                            value.insert(0, '-');
                            break;
                        case 0:
                            value.insert(0, '+');
                            break;
                        case 1:
                            value.insert(0, '+');
                            break;
                        default:
                    }
                } else {
                    switch (data.signum()) {
                        case -1:
                            value.append('-');
                            break;
                        case 0:
                            value.append('+');
                            break;
                        case 1:
                            value.append('+');
                            break;
                        default:
                    }
                }
            } // Or add it to first/last digit's fist nybble if non-separate sign
            else {

                // for a non-separate, leading sign, embed it into the
                // first nybble of the first digit
                if (leadingSign) {
                    signByte = (byte) Character.digit(value.charAt(0), 10);
                    value.delete(0, 1);
                } // for a non-separate, trailing sign, embed it into the
                // first nybble of the last digit
                else {
                    int last = value.length() - 1;
                    signByte =
                            (byte) Character.digit(value.charAt(last), 10);
                    value.delete(last, last + 1);
                }

                switch (data.signum()) {
                    case -1:
                        signByte |= NEGATIVE_ZONE_SIGN;
                        break;
                    case 0:
                        signByte |= POSITIVE_ZONE_SIGN;
                        break;
                    case 1:
                        signByte |= POSITIVE_ZONE_SIGN;
                        break;
                    default:
                }
            }
        }

        // Write the data out
        if (needSign && !separateSign && leadingSign) {
            stream.write((int) signByte);
            stream.flush();
        }
        OutputStreamWriter writer = new OutputStreamWriter(stream, enc);
        writer.write(value.toString());
        writer.flush();
        if (needSign && !separateSign && !leadingSign) {
            stream.write((int) signByte);
            stream.flush();
        }
    }

    /**
     * Write a value as a Cobol display usage, zoned item. Use for internal
     * decimal (zoned) items. If the value's size is less than the item's size,
     * the item is padded with leading zeroes.
     *
     * @param stream  Outlet for data
     * @param data    The value to write
     * @param picture Cobol item picture
     * @param spec    Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     * @param enc     Encoding to use for the output
     *
     * @throws IOException if an I/O error occurs in writing the value
     */
    public static void encodeToZoned(OutputStream stream,
            long data,
            String picture,
            CobolCharacteristics specs,
            String enc)
            throws IOException {

        // Legacy behaviour: when setting zoned values
        // (which may have scale > 0) using long or int (which cannot store
        // information about scale > 0), treat the input not as an integral,
        // but as floating point value.  This means applying scale info
        // of the item to the input after it is converted to a BigDecimal.
        //
        // A proposal to clean up this mess by removing int- and long-based
        // getters and setters for zoned items has been rejected on grounds of
        // backward compatibility.
        BigDecimal decimal;
        decimal = new BigDecimal(Long.toString(data));
        decimal.movePointLeft(specs.getDecimalPosition());

        encodeToZoned(stream, decimal, picture, specs, enc);
    }

    /**
     * Write a value as a Cobol display usage, zoned item. Use for internal
     * decimal (zoned) items. If the value's size is less than the item's size,
     * the item is padded with leading zeroes.
     *
     * @param stream  Outlet for data
     * @param data    The value to write
     * @param picture Cobol item picture
     * @param spec    Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     * @param enc     Encoding to use for the output
     *
     * @throws IOException if an I/O error occurs in writing the value
     */
    public static void encodeToZoned(OutputStream stream,
            int data,
            String picture,
            CobolCharacteristics specs,
            String enc)
            throws IOException {

        // Legacy behaviour: when setting zoned values
        // (which may have scale > 0) using long or int (which cannot store
        // information about scale > 0), treat the input not as an integral,
        // but as floating point value.  This means applying scale info
        // of the item to the input after it is converted to a BigDecimal.
        //
        // A proposal to clean up this mess by removing int- and long-based
        // getters and setters for zoned items has been rejected on grounds of
        // backward compatibility.
        BigDecimal decimal;
        decimal = new BigDecimal(Integer.toString(data));
        decimal.movePointLeft(specs.getDecimalPosition());

        encodeToZoned(stream, decimal, picture, specs, enc);
    }

    /**
     * Write a value as a Cobol display-1 usage item. Use for DBCS items. If the
     * value's size is less than the item size, the item is padded with trailing
     * spaces.
     *
     * @param stream Outlet for data
     * @param data   The value to write
     * @param size   Item size
     *
     * @throws IllegalArgumentException if the data size exceeds the item size,
     *                                  or the number of bytes in the data is
     *                                  not a multiple of 2.
     * @throws IOException      if an I/O error occurs in writing the
     *                                  value
     */
    public static void encodeToDbcs(OutputStream stream,
            byte[] data,
            int size)
            throws IOException {

        int length = data.length;

        if ((length % 2) != 0) {
            Message msg = MessageCatalog.getMessage("CCCR4010");
            String err = msg.formatText(new Object[]{
                        String.valueOf(length)
                    });
            cErrorMgr.log(ErrorManager.Severity.ERROR,
                    null,
                    err);
            throw new IllegalArgumentException(err);
        }
        if (length > size) {
            Message msg = MessageCatalog.getMessage("CCCR3003");
            cErrorMgr.log(ErrorManager.Severity.WARN,
                    null,
                    msg.formatText(new Object[]{
                        String.valueOf(length),
                        String.valueOf(size)
                    }));

            byte[] copy = new byte[size];
            System.arraycopy(data, 0, copy, 0, size);
            data = copy;
        }
        stream.write(data);
        for (int i = 0, pad = (size - length) / DBCSSPACE.length;
                i < pad; i++) {
            stream.write(DBCSSPACE, 0, DBCSSPACE.length);
        }
        stream.flush();
    }

    /**
     * Write a value as a Cobol 2-, 4- or 8-byte binary (usage BINARY/COMP)
     * item. The binary item width used depends on the number of digits in the
     * picture, as specified in IBM Cobol Reference:
     * <p/>
     * <pre>
     * 1-4 digits:   2 bytes
     * 5-9 digits:   4 bytes
     * 10-18 digits: 8 bytes
     * </pre>
     * <p/>
     * However, note that the width computation <em>is not derived from the
     * supplied picture</em>, it is taken from the pre-computed value encoded in
     * the characteristics bit vector <code>parms</code>.
     *
     * @param stream  Outlet for data
     * @param data    The value to write
     * @param picture Cobol item picture
     * @param spec    Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     *
     * @throws IOException if an I/O error occurs in writing the value
     */
    public static void encodeToBinary(OutputStream stream,
            BigDecimal data,
            String picture,
            CobolCharacteristics specs)
            throws IOException {

        final StringBuffer svalue = new StringBuffer(data.unscaledValue().toString());
        final int size = specs.getSize();
        final boolean signed = specs.isSigned();
        final int scalePic = specs.getDecimalPosition();
        final int scalingPositions = specs.getDecimalScalingPositions();
        int digitsPic;
        BigInteger value;
        byte[] valueBytes;
        int valueLen;

        // Remove sign info from value if item is unsigned
        if (!signed && data.signum() == -1) {
            svalue.delete(0, 1);
        }

        // Count number of digits in the picture
        digitsPic = 0;
        for (int i = 0, len = picture.length(); i < len; i++) {
            if (picture.charAt(i) == '9') {
                digitsPic += 1;
            }
        }

        fitToNumericPicture(svalue,
                data.scale(),
                digitsPic,
                scalePic,
                scalingPositions);

        value = new BigInteger(svalue.toString());
        valueBytes = value.toByteArray();
        valueLen = valueBytes.length;

        // Add byte padding
        if (size - valueLen > 0) {
            int padding = size - valueLen;
            byte[] newbytes = new byte[size];
            byte pad;
            int pos = 0;
            if (signed && data.signum() == -1) {
                pad = LIT_BYTE;
            } else {
                pad = 0;
            }
            while (padding-- > 0) {
                newbytes[pos] = pad;
                pos += 1;
            }
            for (int i = 0; i < valueLen; i++) {
                newbytes[pos] = valueBytes[i];
                pos += 1;
            }
            valueBytes = newbytes;
        }
        stream.write(valueBytes);
    }

    /**
     * Write a value as a Cobol 2-, 4- or 8-byte native binary (usage COMP5 or
     * INDEX) item. The binary item width used depends on the number of digits
     * in the picture, as specified in IBM Cobol Reference:
     * <p/>
     * <pre>
     * 1-4 digits:   2 bytes
     * 5-9 digits:   4 bytes
     * 10-18 digits: 8 bytes
     * </pre>
     * <p/>
     * However, note that the width computation <em>is not derived from the
     * supplied picture</em>, it is taken from the pre-computed value encoded in
     * the characteristics bit vector <code>parms</code>.
     *
     * @param stream  Outlet for data
     * @param data    The value to write
     * @param picture Cobol item picture
     * @param spec    Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     *
     * @throws IOException if an I/O error occurs in writing the value
     */
    public static void encodeToNativeBinary(OutputStream stream,
            BigDecimal data,
            String picture,
            CobolCharacteristics specs)
            throws IOException {

        final StringBuffer svalue = new StringBuffer(data.unscaledValue().toString());
        final int size = specs.getSize();
        final boolean signed = specs.isSigned();
        final int scalePic = specs.getDecimalPosition();
        final int scalingPositions = specs.getDecimalScalingPositions();
        int digitsPic;
        BigInteger value;
        byte[] valueBytes;
        int valueLen;

        // Remove sign info from value if item is unsigned
        if (!signed && data.signum() == -1) {
            svalue.delete(0, 1);
        }

        // Count number of digits in the picture
        digitsPic = 0;
        for (int i = 0, len = picture.length(); i < len; i++) {
            if (picture.charAt(i) == '9') {
                digitsPic += 1;
            }
        }

        fitToNumericNativePicture(svalue,
                data.scale(),
                digitsPic,
                scalePic,
                scalingPositions,
                size);

        value = new BigInteger(svalue.toString());
        valueBytes = value.toByteArray();
        valueLen = valueBytes.length;

        // Add byte padding
        if (size - valueLen > 0) {
            int padding = size - valueLen;
            byte[] newbytes = new byte[size];
            byte pad;
            int pos = 0;
            if (signed && value.signum() == -1) {
                pad = LIT_BYTE;
            } else {
                pad = 0;
            }
            while (padding-- > 0) {
                newbytes[pos] = pad;
                pos += 1;
            }
            for (int i = 0; i < valueLen; i++) {
                newbytes[pos] = valueBytes[i];
                pos += 1;
            }
            valueBytes = newbytes;
        }
        stream.write(valueBytes);
    }

    /**
     * Write a value as a Cobol 2-, 4- or 8-byte binary item (usage
     * BINARY/COMP). Delegates to
     * {@link #convertToBinary(OutputStream, BigDecimal, String, String)}
     * so see that method for additional details.
     *
     * @param stream  Outlet for data
     * @param data    The value to write
     * @param picture Cobol item picture
     * @param spec    Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     *
     * @throws IOException if an I/O error occurs in writing the value
     */
    public static void encodeToBinary(OutputStream stream,
            long data,
            String picture,
            CobolCharacteristics specs)
            throws IOException {

        // Legacy behaviour: when setting binary values
        // (which may have scale > 0) using long or int (which cannot store
        // information about scale > 0), treat the input not as an integral,
        // but as floating point value.  This means applying scale info
        // of the item to the input after it is converted to a BigDecimal.
        //
        // A proposal to clean up this mess by removing int- and long-based
        // getters and setters for binary items has been rejected on grounds of
        // backward compatibility.
        BigDecimal decimal;
        decimal = new BigDecimal(Long.toString(data));
        decimal.movePointLeft(specs.getDecimalPosition());

        encodeToBinary(stream, decimal, picture, specs);
    }

    /**
     * Write a value as a Cobol 2-, 4- or 8-byte binary item (usage
     * BINARY/COMP). Delegates to
     * {@link #convertToBinary(OutputStream, BigDecimal, String, String)}
     * so see that method for additional details.
     *
     * @param stream  Outlet for data
     * @param data    The value to write
     * @param picture Cobol item picture
     * @param spec    Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     *
     * @throws IOException if an I/O error occurs in writing the value
     */
    public static void encodeToBinary(OutputStream stream,
            int data,
            String picture,
            CobolCharacteristics specs)
            throws IOException {

        // Legacy behaviour: when setting binary values
        // (which may have scale > 0) using long or int (which cannot store
        // information about scale > 0), treat the input not as an integral,
        // but as floating point value.  This means applying scale info
        // of the item to the input after it is converted to a BigDecimal.
        //
        // A proposal to clean up this mess by removing int- and long-based
        // getters and setters for binary items has been rejected on grounds of
        // backward compatibility.
        BigDecimal decimal;
        decimal = new BigDecimal(Integer.toString(data));
        decimal.movePointLeft(specs.getDecimalPosition());

        encodeToBinary(stream, decimal, picture, specs);
    }

    /**
     * Write a value as a Cobol packed-decimal item. Use for internal decimal
     * items. Unlike the other variants of this method, this one can handle
     * values with non-zero decimal scaling.
     *
     * @param stream  Outlet for data
     * @param data    The value to write
     * @param picture Item picture
     * @param spec    Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     *
     * @throws IOException if an I/O error occurs in writing the value
     */
    public static void encodeToPacked(OutputStream stream,
            BigDecimal data,
            String picture,
            CobolCharacteristics specs)
            throws IOException {

        final StringBuffer value = new StringBuffer(data.unscaledValue().toString());
        final boolean signed = specs.isSigned();
        final int size = specs.getSize();
        final int scalePic = specs.getDecimalPosition();
        final int scalingPositions = specs.getDecimalScalingPositions();
        int digitsPic;
        int scale;

        // calc picture's digit requirements
        digitsPic = 0;
        for (int i = 0; i < picture.length(); i++) {
            if (picture.charAt(i) == '9') {
                digitsPic++;
            }
        }

        // Remove '-' for convenience
        if (data.signum() == -1) {
            value.delete(0, 1);
        }

        // Scale value to its picture
        scale = data.scale();
        scale = fitToNumericPicture(value,
                scale,
                digitsPic,
                scalePic,
                scalingPositions);

        // Packed decimal-specific truncation check
        int maxFitDigits = (size << 1) - 1; // reserve one for the
        //"sign" sign (neg, pos, unsigned)
        if (maxFitDigits < value.length()) {
            Message msg = MessageCatalog.getMessage("CCCR3004");
            String err = msg.formatText(new Object[]{
                        data.toString(),
                        String.valueOf((value.length() >> 1) + 1),
                        String.valueOf(maxFitDigits)
                    });
            cErrorMgr.log(ErrorManager.Severity.WARN, null, err);

            // truncate fraction
            if (scale > 0) {
                int truncsize = Math.max(0, scale - scalePic);
                value.delete(value.length() - truncsize, value.length());
                scale -= truncsize;
            }

            // truncate non-fraction
            if (maxFitDigits < value.length()) {
                int truncsize = Math.max(0, (value.length() - scale) - (digitsPic - scalePic));
                value.delete(0, truncsize);
            }
        }

        /*
         * if even number digits, need to add extra byte for sign:
         *
         * decimal = 12345
         * packed  = 12|34|5s  where s == sign nybble; extra byte not needed
         *
         * decimal = 1234
         * packed  = 01|23|4s  where s == sign nybble; extra byte needed
         */
        final boolean needPad = ((value.length() % 2) == 0);

        int current = 0;
        if (needPad) {
            byte pad = 0;
            byte digit =
                    (byte) Character.digit(value.charAt(current++), 10);
            byte b = (byte) (pad | digit);
            stream.write(b);
        }
        for (int i = current; i < value.length(); i += 2) {
            byte digit1 = (byte) Character.digit(value.charAt(i), 10);
            byte digit2 = 0;

            if (i < (value.length() - 1)) { // if not last digit
                digit2 = (byte) Character.digit(value.charAt(i + 1), 10);
            } else if (!signed) {
                digit2 = UNSIGNED_PACK_SIGN;
            } else if ((new BigInteger(value.toString())).equals(
                    BigInteger.ZERO)) {
                digit2 = POSITIVE_PACK_SIGN;
            } //            else if ( Integer.valueOf( value.toString() ).intValue() == 0 ) {
            //                digit2 = POSITIVE_PACK_SIGN;
            //            }
            else {
                switch (data.signum()) {
                    case -1:
                        digit2 = NEGATIVE_PACK_SIGN;
                        break;
                    case 0:
                        digit2 = POSITIVE_PACK_SIGN;
                        break;
                    case 1:
                        digit2 = POSITIVE_PACK_SIGN;
                        break;
                    default:
                }
            }

            byte b = (byte) ((digit1 << 4) | digit2);
            stream.write(b);
        }
    }

    /**
     * Write a value as a Cobol packed-decimal item. Use for internal decimal
     * items.
     *
     * @param stream  Outlet for data
     * @param data    The value to write
     * @param picture Item picture
     * @param spec    Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     *
     * @throws IOException if an I/O error occurs in writing the value
     */
    public static void encodeToPacked(OutputStream stream,
            long data,
            String picture,
            CobolCharacteristics specs)
            throws IOException {

        // Legacy behaviour: when setting packed values
        // (which may have scale > 0) using long or int (which cannot store
        // information about scale > 0), treat the input not as an integral,
        // but as floating point value.  This means applying scale info
        // of the item to the input after it is converted to a BigDecimal.
        //
        // A proposal to clean up this mess by removing int- and long-based
        // getters and setters for packed items has been rejected on grounds of
        // backward compatibility.
        BigDecimal decimal;
        decimal = new BigDecimal(Long.toString(data));
        decimal.movePointLeft(specs.getDecimalPosition());

        encodeToPacked(stream, decimal, picture, specs);
    }

    /**
     * Write a value as a Cobol packed-decimal item. Use for internal decimal
     * items.
     *
     * @param stream  Outlet for data
     * @param data    The value to write
     * @param picture Item picture
     * @param spec    Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     *
     * @throws IOException if an I/O error occurs in writing the value
     */
    public static void encodeToPacked(OutputStream stream,
            int data,
            String picture,
            CobolCharacteristics specs)
            throws IOException {

        // Legacy behaviour: when setting packed values
        // (which may have scale > 0) using long or int (which cannot store
        // information about scale > 0), treat the input not as an integral,
        // but as floating point value.  This means applying scale info
        // of the item to the input after it is converted to a BigDecimal.
        //
        // A proposal to clean up this mess by removing int- and long-based
        // getters and setters for packed items has been rejected on grounds of
        // backward compatibility.
        BigDecimal decimal;
        decimal = new BigDecimal(Integer.toString(data));
        decimal.movePointLeft(specs.getDecimalPosition());

        encodeToPacked(stream, decimal, picture, specs);
    }

    /**
     * Write a value as a Cobol/EBCDIC index (4-byte) item.
     *
     * @param stream Outlet for data
     * @param data   The value to write
     * @param spec   Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     *
     * @throws IOException if an I/O error occurs in writing the value
     */
    public static void encodeToIndex(OutputStream stream,
            int data,
            CobolCharacteristics specs)
            throws IOException {
        BigDecimal value = new BigDecimal(data);
        encodeToNativeBinary(stream, value, "999999999", specs);
    }

    /**
     * Convert the data from the input stream into to a Java integer (int)
     * value.  Use for numeric items that can fit 4-byte signed storage.
     *
     * @param stream  Data inlet as a byte stream
     * @param picture Cobol item picture
     * @param spec    Characteristics of the item;
     *                      see {@link CobolCharacteristics#toString()}
     * @param enc     Encoding to use to read character data
     *
     * @return Data as an int
     *
     * @throws IllegalArgumentException if an int value cannot be produced given
     *                                  the data item information
     * @throws IOException      if an I/O error occured in reading
     *                                  input
     */
    public static int decodeToInt(InputStream stream,
            String picture,
            CobolCharacteristics specs,
            String enc)
            throws IOException {

        int result;
        int category = specs.getPicCategory();
        int usage = specs.getUsage();

        if (CobolCharacteristics.PIC_NUM == category) {
            switch (usage) {
                case CobolCharacteristics.USAGE_DISPLAY: {
                    String value = readNumberDisplay(stream, specs, enc);
                    result = Integer.parseInt(value);
                    break;
                }
                case CobolCharacteristics.USAGE_BINARY:
                case CobolCharacteristics.USAGE_COMP:
                case CobolCharacteristics.USAGE_COMP4: {
                    BigDecimal value = readNumberBinary(stream, specs);
                    result = value.unscaledValue().intValue();
                    break;
                }
                case CobolCharacteristics.USAGE_INDEX: {
                    BigDecimal value = readNumberBinary(stream, specs);
                    result = value.unscaledValue().intValue();
                    break;
                }
                case CobolCharacteristics.USAGE_PACKED:
                case CobolCharacteristics.USAGE_COMP3: {
                    BigDecimal value = readNumberPacked(stream, specs);
                    result = value.unscaledValue().intValue();
                    break;
                }
                case CobolCharacteristics.USAGE_COMP5: {
                    BigDecimal value = readNumberBinary(stream, specs);
                    result = value.unscaledValue().intValue();
                    break;
                }
                default:
                    Message msg = MessageCatalog.getMessage("CCCR4011");
                    String err = msg.toString();
                    cErrorMgr.log(ErrorManager.Severity.ERROR,
                            null,
                            err);
                    throw new IllegalArgumentException(err);
            }
        } else {
            Message msg = MessageCatalog.getMessage("CCCR4013");
            String err = msg.formatText(new Object[]{compressedPic(picture)});
            cErrorMgr.log(ErrorManager.Severity.ERROR, null, err);
            throw new IllegalArgumentException(err);
        }

        return result;
    }

    /**
     * Convert the data from the input stream into a Java long integer (long)
     * value.  Use for numeric items exceeding 4-byte signed storage
     * capability.
     *
     * @param stream  Data inlet as a byte stream
     * @param picture Cobol item picture
     * @param spec    Characteristics of the item;
     *                      see {@link CobolCharacteristics#toString()}
     * @param enc     Encoding to use to read character data
     *
     * @return Data as an long
     *
     * @throws IllegalArgumentException if a long value cannot be produced given
     *                                  the data item information
     * @throws IOException      an I/O error occured in reading input
     */
    public static long decodeTolong(InputStream stream,
            String picture,
            CobolCharacteristics specs,
            String enc)
            throws IOException {

        long result;
        int category = specs.getPicCategory();
        int usage = specs.getUsage();

        if (CobolCharacteristics.PIC_NUM == category) {
            switch (usage) {
                case CobolCharacteristics.USAGE_DISPLAY: {
                    String value = readNumberDisplay(stream, specs, enc);
                    result = Long.parseLong(value);
                    break;
                }
                case CobolCharacteristics.USAGE_BINARY:
                case CobolCharacteristics.USAGE_COMP:
                case CobolCharacteristics.USAGE_COMP4: {
                    BigDecimal value = readNumberBinary(stream, specs);
                    result = value.unscaledValue().longValue();
                    break;
                }
                case CobolCharacteristics.USAGE_PACKED:
                case CobolCharacteristics.USAGE_COMP3: {
                    BigDecimal value = readNumberPacked(stream, specs);
                    result = value.unscaledValue().longValue();
                    break;
                }
                case CobolCharacteristics.USAGE_COMP5: {
                    BigDecimal value = readNumberBinary(stream, specs);
                    result = value.unscaledValue().longValue();
                    break;
                }
                case CobolCharacteristics.USAGE_INDEX: {
                    BigDecimal value = readNumberBinary(stream, specs);
                    result = value.unscaledValue().longValue();
                    break;
                }
                default:
                    Message msg = MessageCatalog.getMessage("CCCR4012");
                    String err = msg.toString();
                    cErrorMgr.log(ErrorManager.Severity.ERROR,
                            null,
                            err);
                    throw new IllegalArgumentException(err);
            }
        } else {
            Message msg = MessageCatalog.getMessage("CCCR4013");
            String err = msg.formatText(new Object[]{compressedPic(picture)});
            cErrorMgr.log(ErrorManager.Severity.ERROR, null, err);
            throw new IllegalArgumentException(err);
        }

        return result;
    }

    /**
     * Convert the data from the input stream into a Java float. Use for COMP-1
     * items.
     *
     * @param stream  Byte Data inlet
     *
     * @return Data as float
     *
     * @throws IOException if an I/O error occurs while reading the input,
     *                     including having insufficient data
     */
    public static float decodeToFloat(InputStream stream)
            throws IOException {

        final int NEED = 4;

        final byte[] bytebuf = new byte[NEED];
        int got = stream.read(bytebuf);
        if (got != NEED) {
            Message msg = MessageCatalog.getMessage("CCCR4014");
            String err = msg.formatText(new Object[]{
                        "COMP-1",
                        String.valueOf(NEED),
                        String.valueOf(Math.max(0, got))
                    });
            cErrorMgr.log(ErrorManager.Severity.ERROR, null, err);
            throw new IOException(err);
        }

        int intBits;
        intBits = (((int) bytebuf[0]) << 24) & 0xFF000000;
        intBits |= (((int) bytebuf[1]) << 16) & 0x00FF0000;
        intBits |= (((int) bytebuf[2]) << 8) & 0x0000FF00;
        intBits |= bytebuf[3] & 0x000000FF;

        return Float.intBitsToFloat(intBits);
    }

    /**
     * Convert the data from the input stream into a Java double. Use for COMP-2
     * items.
     * 
     * @param stream  Byte Data inlet
     *
     * @return Data as double
     * 
     * @throws IOException if an I/O error occurs while reading the input,
     *                     including having insufficient data
     */
    public static double decodeToDouble(InputStream stream)
            throws IOException {

        final int NEED = 8;

        final byte[] bytebuf = new byte[NEED];
        int got = stream.read(bytebuf);
        if (got != NEED) {
            Message msg = MessageCatalog.getMessage("CCCR4014");
            String err = msg.formatText(new Object[]{
                        "COMP-2",
                        String.valueOf(NEED),
                        String.valueOf(Math.max(0, got))
                    });
            cErrorMgr.log(ErrorManager.Severity.ERROR, null, err);
            throw new IOException(err);
        }

        long longBits;
        longBits = (((long) bytebuf[0]) << 56) & 0xFF00000000000000L;
        longBits |= (((long) bytebuf[1]) << 48) & 0x00FF000000000000L;
        longBits |= (((long) bytebuf[2]) << 40) & 0x0000FF0000000000L;
        longBits |= (((long) bytebuf[3]) << 32) & 0x000000FF00000000L;
        longBits |= (((long) bytebuf[4]) << 24) & 0x00000000FF000000L;
        longBits |= (((long) bytebuf[5]) << 16) & 0x0000000000FF0000L;
        longBits |= (((long) bytebuf[6]) << 8) & 0x000000000000FF00L;
        longBits |= bytebuf[7] & 0x00000000000000FFL;

        return Double.longBitsToDouble(longBits);
    }

    /**
     * Convert the data from the input stream into a Java byte array. Use for
     * DBCS items.
     *
     * @param stream  Data inlet as a byte stream
     * @param picture Cobol item picture
     * @param spec    Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     *
     * @return Data as an array of bytes, with each element containing a half (a
     *         byte) of a double-byte character value
     *
     * @throws IOException      if an I/O error occured in reading
     *                                  input
     * @throws IllegalArgumentException if the Cobol item category specified is
     *                                  not DBCS, or a DBCS value cannot be read
     *                                  given the data item information
     */
    public static byte[] decodeTobytes(InputStream stream,
            String picture,
            CobolCharacteristics specs)
            throws IOException {

        byte[] result = null;
        int category = specs.getPicCategory();

        if (CobolCharacteristics.PIC_DBCS == category) {
            result = readDBCS(stream, specs);
        } else {
            Message msg = MessageCatalog.getMessage("CCCR4015");
            String err = msg.formatText(new Object[]{compressedPic(picture)});
            cErrorMgr.log(ErrorManager.Severity.ERROR, null, err);
            throw new IllegalArgumentException(err);
        }

        return result;
    }

    /**
     * Convert the data from the input stream into a Java string value. Use for
     * alphabetic, alphanumeric, alphanumeric-edited, and numeric-edited items.
     *
     * @param stream  Data inlet as a byte stream
     * @param reader  Data inlet as a character reader
     * @param picture Cobol item picture
     * @param spec    Characteristics of the item;
     *                      see {@link CobolCharacteristics#toString()}
     * @param enc     Encoding to use to read character data
     *
     * @return Data as a string
     *
     * @throws IllegalArgumentException if a string value cannot be produced
     *                                  given the data item information
     * @throws IOException      an I/O error occured in reading input
     */
    public static String decodeToString(InputStream stream,
            String picture,
            CobolCharacteristics specs,
            String enc)
            throws IOException {

        String result = null;
        int category = specs.getPicCategory();

        // Switch back to old implementation. Will improve it later
        // if necessary.  
        //
        // ESR 104580
        // Profiling shows that creating a new InputStreamReader
        // from InputStream is very expensive, in particular on 
        // HP/UX 11iV2.  According to CocoCodeGen.java, the InputStreamReader
        // argument, reader, is created from the InputStream argument, 
        // stream, using the encoding represented by the String argument,
        // enc.  Therefore there is really no need to create a new
        // InputStreamReader from InputStream.
        // 
        // Relevant code in CocoCodeGen.java is
        //
        //    emitter.emit("mEncoding = \"cp037\"; // initial default");
        //    ...  
        //    emitter.emit("mWriter =new OutputStreamWriter(mOutputStream, mEncoding);");
        //    emitter.emit("mReader = new InputStreamReader(mInputStream, mEncoding);");
        // and
        //    emitter.emit( "mEncoding = enc.name();" );
        //    emitter.emit( "mWriter = new OutputStreamWriter(mOutputStream, mEncoding);" );
        //    emitter.emit( "mReader = new InputStreamReader(mInputStream, mEncoding);" );
        // and
        //    emitter.emit("return CobolDataConverter2.convertTo"+javaName+"(mRoot.mInputStream, mRoot.mReader, m"+wood.getJavaName()+
        //           "Picture, m"+wood.getJavaName()+"Mask, mRoot.mEncoding);");

        switch (category) {
            case CobolCharacteristics.PIC_ALPHA:
                //// ESR 104580
                result = readAlpha(stream, specs, enc);
                //result = readAlpha( reader, specs, enc );
                break;
            case CobolCharacteristics.PIC_ALPHANUM:
                //// ESR 104580
                result = readAlphanum(stream, specs, enc);
                //result = readAlphanum( reader, specs, enc );
                break;
            case CobolCharacteristics.PIC_ALPHANUME:
                //// ESR 104580
                result = readAlphanumEdited(stream, specs, enc);
                //result = readAlphanumEdited( reader, specs, enc );
                break;
            case CobolCharacteristics.PIC_NUME:
                //// ESR 104580
                result = readNumEdited(stream, specs, enc);
                //result = readNumEdited( reader, picture, specs, enc );
                break;
            case CobolCharacteristics.PIC_DBCS:
                result = readDBCS(stream, specs, enc);
                break;
            default:
                Message msg = MessageCatalog.getMessage("CCCR4016");
                String err = msg.formatText(new Object[]{compressedPic(picture)});
                cErrorMgr.log(ErrorManager.Severity.ERROR, null, err);
                throw new IllegalArgumentException(err);
        }

        return result;
    }

    /**
     * Convert the data from the input stream into a Java BigDecimal. Use for
     * internal and external floating point items, and internal decimal
     * exceeding capabilities of 8-byte signed storage.
     *
     * @param stream  Data inlet as a byte stream
     * @param picture Cobol item picture
     * @param spec    Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     * @param enc     Encoding to use to read character data
     *
     * @return Data as a BigDecimal
     *
     * @throws IllegalArgumentException if a BigDecimal value cannot be produced
     *                                  given the data item information
     * @throws IOException      an I/O error occured in reading input
     */
    public static BigDecimal decodeToBigDecimal(InputStream stream,
            String picture,
            CobolCharacteristics specs,
            String enc)
            throws IOException {

        BigDecimal result = null;
        int category = specs.getPicCategory();
        int usage = specs.getUsage();

        if (CobolCharacteristics.PIC_EXFLOAT == category) {
            result = readExFloat(stream, specs, enc);
        } else if (CobolCharacteristics.PIC_NUM == category) {
            switch (usage) {
                case CobolCharacteristics.USAGE_DISPLAY:
                    result =
                            new BigDecimal(readNumberDisplay(stream, specs, enc));
                    break;
                case CobolCharacteristics.USAGE_BINARY:
                case CobolCharacteristics.USAGE_COMP:
                case CobolCharacteristics.USAGE_COMP4:
                    result = readNumberBinary(stream, specs);
                    break;
                case CobolCharacteristics.USAGE_PACKED:
                case CobolCharacteristics.USAGE_COMP3:
                    result = readNumberPacked(stream, specs);
                    break;
                case CobolCharacteristics.USAGE_COMP5:
                    result = readNumberBinary(stream, specs);
                    break;
                case CobolCharacteristics.USAGE_INDEX:
                    result = readNumberBinary(stream, specs);
                    break;
                default:
                    Message msg = MessageCatalog.getMessage("CCCR4017");
                    String err = msg.formatText(new Object[]{compressedPic(picture)});
                    cErrorMgr.log(ErrorManager.Severity.ERROR, null, err);
                    throw new IllegalArgumentException(err);
            }
        } else {
            Message msg = MessageCatalog.getMessage("CCCR4018");
            String err = msg.formatText(new Object[]{compressedPic(picture)});
            cErrorMgr.log(ErrorManager.Severity.ERROR, null, err);
            throw new IllegalArgumentException(err);
        }

        return result;
    }

    public static boolean isSameEncoding(String enc1, String enc2) {
        boolean same = false;

        if (enc1 != null && enc2 != null) {
            if (enc1.equalsIgnoreCase(enc2)) {
                same = true;
            } else {
                try {
                    Charset Lcs1 = Charset.forName(enc1);
                    Charset Lcs2 = Charset.forName(enc2);
                    same = Lcs1.equals(Lcs2);
                } catch (Exception e) {
                    same = false;
                }
            }
        }
        return same;
    }

    /**
     * Get the byte sequence for a "space" character in the indicated encoding
     *
     * @param enc Charset encoding
     *
     * @throws CharacterCodingException
     *          if the specified encoding cannot encode the space character
     * @throws UnsupportedEncodingException
     *          if the specified encoding is not supported
     */
    public static byte[] getSpace(String enc)
            throws CharacterCodingException,
            UnsupportedEncodingException {
        byte[] LspaceBytes;

        synchronized (mSpaceEncodings) {
            LspaceBytes = (byte[]) mSpaceEncodings.get(enc);
        }

        if (LspaceBytes == null) {
            LspaceBytes = charsToBytes(SPACE, enc);
            synchronized (mSpaceEncodings) {
                mSpaceEncodings.put(enc, LspaceBytes);
            }
        }
        return LspaceBytes;
    }

    /**
     * Get the byte sequence for a "plus" character in the indicated encoding
     *
     * @param enc Charset encoding
     *
     * @throws CharacterCodingException
     *          if the specified encoding cannot encode the plus character
     * @throws UnsupportedEncodingException
     *          if the specified encoding is not supported
     */
    public static byte[] getPlus(String enc)
            throws CharacterCodingException,
            UnsupportedEncodingException {
        byte[] LplusBytes;

        synchronized (mPlusEncodings) {
            LplusBytes = (byte[]) mPlusEncodings.get(enc);
        }

        if (LplusBytes == null) {
            LplusBytes = charsToBytes(PLUS, enc);
            synchronized (mPlusEncodings) {
                mPlusEncodings.put(enc, LplusBytes);
            }
        }
        return LplusBytes;
    }

    /**
     * Get the byte sequence for a "minus" character in the indicated encoding
     *
     * @param enc Charset encoding
     *
     * @throws CharacterCodingException
     *          if the specified encoding cannot encode the minus character
     * @throws UnsupportedEncodingException
     *          if the specified encoding is not supported
     */
    public static byte[] getMinus(String enc)
            throws CharacterCodingException,
            UnsupportedEncodingException {
        byte[] LminusBytes;

        synchronized (mMinusEncodings) {
            LminusBytes = (byte[]) mMinusEncodings.get(enc);
        }

        if (LminusBytes == null) {
            LminusBytes = charsToBytes(MINUS, enc);
            synchronized (mMinusEncodings) {
                mMinusEncodings.put(enc, LminusBytes);
            }
        }
        return LminusBytes;
    }

    private static String readAlpha(InputStream stream,
            CobolCharacteristics spec,
            String enc)
            throws IOException {
        byte[] buf = new byte[spec.getSize()];
        int got = stream.read(buf, 0, buf.length);

        if ((got == -1) || (got != buf.length)) {
            return null;
        }

        return new String(buf, enc);
    }

    /**
     * Read (Cobol alphanumeric) data from the input stream into a string.
     *
     * @param stream Data inlet
     * @param spec   Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     * @param enc    Encoding to use when reading data
     *
     * @return string representation of the consumed data, or null if there is
     *         no or insufficient data to process
     *
     * @throws IOException if an I/O error occurs attempting to read
     *                             from the input stream, or if the specified
     *                             encoding is not supported
     */
    private static String readAlphanum(InputStream stream,
            CobolCharacteristics spec,
            String enc)
            throws IOException {

        byte[] buf = new byte[spec.getSize()];
        int want = buf.length;
        int got = stream.read(buf, 0, want);

        if ((got == -1) || (got != want)) {
            return null;
        }

        return new String(buf, enc);
    }

    /**
     * Read (Cobol alphanumeric-edited) data from the input stream.
     *
     * @param stream Data inlet
     * @param spec   Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     * @param enc    Encoding to use when reading data
     *
     * @return string representation of the consumed data, or null if there is
     *         no or insufficient data to process
     *
     * @throws IOException if an I/O error occurs attempting to read
     *                             from the input stream, or if the specified
     *                             encoding is not supported
     */
    private static String readAlphanumEdited(InputStream stream,
            CobolCharacteristics spec,
            String enc)
            throws IOException {

        /*
         * Edited items are supported as follows:
         * Consume ANY characters, up to the picture's length.
         * If there's not enough data, pad with spaces.
         * The data I get is somebody else's problem.
         */

        byte[] buf = new byte[spec.getSize()];
        int want = buf.length;
        int got = stream.read(buf, 0, want);

        if ((got == -1) || (got != want)) {
            return null;
        }

        return new String(buf, enc);
    }

    /**
     * Read (Cobol numeric-edited) data from the input stream.
     *
     * @param stream  Data inlet
     * @param picture Cobol (numeric-edited) picture
     * @param spec    Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     * @param enc     Encoding to use when reading data
     *
     * @return string representation of the consumed data, or null if there is
     *         no or insufficient data to process
     *
     * @throws IOException if an I/O error occurs attempting to read
     *                             from the input stream, or if the specified
     *                             encoding is not supported
     */
    private static String readNumEdited(InputStream stream,
            CobolCharacteristics spec,
            String enc)
            throws IOException {

        /*
         * Edited items are supported as follows:
         * Consume ANY characters, up to the picture's length.
         * If there's not enough data, pad (leading) with zeroes.
         * The data I get is somebody else's problem
         */

        byte[] buf = new byte[spec.getSize()];
        int want = buf.length;
        int got = stream.read(buf, 0, want);

        if ((got == -1) || (got != want)) {
            return null;
        }

        return new String(buf, enc);
    }

    /**
     * Read (Cobol external floating-point) data from the input stream.
     *
     * @param stream Data inlet
     * @param spec   Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     * @param enc    Encoding to use when reading data
     *
     * @return BigDecimal representation of the consumed data, or null if there
     *         is no or insufficient data to process
     *
     * @throws IOException if an I/O error occurs attempting to read
     *                             from the input stream, or if the specified
     *                             encoding is not supported
     */
    private static BigDecimal readExFloat(InputStream stream,
            CobolCharacteristics spec,
            String enc)
            throws IOException {

        InputStreamReader reader = createReader(stream, enc);

        /* get long data (usage display) */
        int count = spec.getSize();
        char[] data = new char[count];
        int got = reader.read(data);
        if (got != count) {
            return null;
        }

        /* convert to BigDecimal */
        BigDecimal value = null;
        if (validateExFloat(data)) {
            value = new BigDecimal(new String(data));
        }
        return value;
    }

    /**
     * Compares character data with an external floating point picture. The
     * picture must be a valid external floating point picture (e.g.,
     * "+9V99E+99".
     *
     * @param data Character data (i.e., usage display) to evaluate against the
     *             picture.
     *
     * @return true if the data is valid for the given picture, otherwise false
     *         is returned
     */
    private static boolean validateExFloat(char[] data) {
        StringBuffer buf = new StringBuffer(String.valueOf(data));
        int len = buf.length();
        boolean haveExponentSign;
        boolean haveSignificandSign;

        if (len < 3) {
            return false;
        }

        int charindex = buf.indexOf("E");
        if (charindex == -1 || len == charindex + 1) {
            return false;
        }

        char signchar = buf.charAt(charindex + 1);
        haveExponentSign = (signchar == '-' || signchar == '+');
        if (!haveExponentSign && !Character.isDigit(signchar)) {
            return false;
        }

        char startchar = buf.charAt(0);
        haveSignificandSign = (startchar == '+' || startchar == '-');
        if (!haveSignificandSign && !Character.isDigit(startchar)) {
            return false;
        }

        if (haveExponentSign && charindex + 2 == len) {
            return false;
        }

        if (haveSignificandSign && charindex == 1) {
            return false;
        }

        for (int i = 1; i < charindex; i++) {
            if (!Character.isDigit(buf.charAt(i))) {
                return false;
            }
        }

        for (int i = charindex + 2; i < len; i++) {
            if (!Character.isDigit(buf.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Read DBCS data from the input stream.
     *
     * @param stream  Data inlet
     * @param picture Cobol (DBCS) picture
     * @param spec    Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     *
     * @return byte array representation of the consumed data, or null if there
     *         is no or insufficient data to process
     *
     * @throws IOException if an I/O error occurs attempting to read
     *                             from the input stream
     */
    private static byte[] readDBCS(InputStream stream,
            CobolCharacteristics spec)
            throws IOException {

        int need = spec.getSize();
        byte[] bytebuf = new byte[need];
        int got = stream.read(bytebuf);
        if (got != need) {
            return null;
        }
        return bytebuf;
    }

    private static String readDBCS(InputStream stream,
            CobolCharacteristics spec, String enc) throws IOException {
        byte[] buf = new byte[spec.getSize()];
        int got = stream.read(buf, 0, buf.length);

        if ((got == -1) || (got != buf.length)) {
            return null;
        }

        return new String(buf, enc);
    }

    /**
     * Read (Cobol numeric) data from the input stream stored with DISPLAY usage
     * (one character position per byte).
     *
     * @param stream Data inlet
     * @param spec   Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     * @param enc    Encoding to use when reading data
     *
     * @return string representation of the consumed data, or null if there is
     *         no or insufficient data to process. The representation returned
     *         DOES NOT HAVE DECIMAL POINT INFORMATION.
     *
     * @throws IOException if an I/O error occurs attempting to read
     *                             from the input stream
     */
    private static String readNumberDisplay(InputStream stream,
            CobolCharacteristics spec,
            String enc)
            throws IOException {

        /* count number of digits (== bytes) to process as value */
        int countDigits = spec.getSize();

        /* signed data? */
        boolean isSigned = spec.isSigned();

        /* get value */
        long[] value = new long[1];
        String[] number = new String[1];
        int read = readZonedNumber(stream,
                spec,
                value,
                isSigned,
                countDigits,
                number,
                enc);
        if (read != countDigits) {
            return null;
        }
        int dec_pos = spec.getDecimalPosition();
        if (dec_pos > 0) {
            BigDecimal bd =
                    new BigDecimal(BigInteger.valueOf(value[0]), dec_pos);
//        	double scaled_value = value[0];
//        	for ( int i = 0; i < dec_pos; i++ )
//        		scaled_value = scaled_value / 10;
//        	return Double.toString(scaled_value);
            return bd.toString();
        } else {
            return Long.toString(value[ 0]);
        }
    }

    /**
     * Read (Cobol numeric) data from the input stream stored with BINARY
     * usage.
     *
     * @param stream Data inlet
     * @param spec   Characteristics of the item;
     *                  see {@link CobolCharacteristics#toString()}
     *
     * @return BigDecimal representation of the consumed data, or null if there
     *         is no or insufficient data to process.
     *
     * @throws IOException if an I/O error occurs attempting to read
     *                             from the input stream
     */
    private static BigDecimal readNumberBinary(InputStream stream,
            CobolCharacteristics spec)
            throws IOException {

        // decimal position and scaling
        int decPos = spec.getDecimalPosition();
        int scalingDigits = spec.getDecimalScalingPositions();

        /* compute bytes to get */
        int numBytes = spec.getSize();

        /* get the data */
        BigDecimal decimal;
        long[] value = new long[1];
        int got = 0;
        switch (numBytes) {
            case 2:
                got = readBinaryNumber(stream, value, 2);
                break;
            case 4:
                got = readBinaryNumber(stream, value, 4);
                break;
            case 8:
                got = readBinaryNumber(stream, value, 8);
                break;
            default:
                got = -1;
        }
        if (got != numBytes) {
            return null;
        }
        decimal = new BigDecimal(Long.toString(value[ 0]));
        decimal = decimal.movePointLeft(decPos);
        decimal = decimal.movePointRight(decPos > 0 ? 0 : scalingDigits);
        return decimal;
    }

    /**
     * Read (Cobol numeric) packed-decimal data from the input stream.
     *
     * @param stream Data inlet
     * @param spec   Characteristics of the item;
     *              see {@link CobolCharacteristics#toString()}
     *
     * @return BigDecimal representation of the consumed data, or null if there
     *         is no or insufficient data to process.
     *
     * @throws IOException if an I/O error occurs attempting to read
     *                             from the input stream
     */
    private static BigDecimal readNumberPacked(InputStream stream,
            CobolCharacteristics spec)
            throws IOException {

        // decimal position and scaling
        int decPos = spec.getDecimalPosition();
        int scalingDigits = spec.getDecimalScalingPositions();

        /* compute bytes to get */
        int numBytes = spec.getSize();

        /* get the data */
        BigDecimal decimal;
        long[] value = new long[1];
        int got = readPackedNumber(stream, value, numBytes);

        if (got != numBytes) {
            return null;
        }
        decimal = new BigDecimal(Long.toString(value[ 0]));
        decimal = decimal.movePointLeft(decPos);
        decimal = decimal.movePointRight(decPos > 0 ? 0 : scalingDigits);
        return decimal;
    }

    /**
     * Read (Cobol numeric) data from the input stream and interpret it as a
     * zoned decimal.  The representation returned DOES NOT HAVE DECIMAL POINT
     * INFORMATION.
     *
     * @param stream Data inlet
     * @param spec   Characteristics of the item; see
     * {@link CobolCharacteristics#toString()}
     * @param data   Array to hold the read value; only the first element of the
     *               array is used.
     * @param signed True to indicate sign indicator is part of the value (not
     *               necessarily a separate character); false to indicate the
     *               value is unsigned
     * @param count  The number of bytes to process from the stream as a zoned
     *               decimal value
     * @param enc    Encoding to use when reading data
     *
     * @return The number of bytes consumed to process the data, or -1 if there
     *         was no or insufficient data to process
     *
     * @throws IOException if an I/O error occurs attempting to read
     *                             from the input stream
     */
    private static int readZonedNumber(InputStream stream,
            CobolCharacteristics spec,
            long[] data,
            boolean signed,
            int count,
            String[] number,
            String enc)
            throws IOException {

        boolean separateSign = spec.isSignSeparate();
        boolean leadingSign = spec.isSignLeading();
        byte[] plus = getPlus(enc);
        byte[] minus = getMinus(enc);

        byte[] buf = new byte[count];
        int got = 0;
        int sign = 0;

        /* get value (number) */
        got = stream.read(buf);
        if (got != count) {
            return (-1);
        }

        /* process separate sign */
        if (separateSign) {
            byte[] signbytes = getSeparateSign(leadingSign, buf, plus, minus);
            if (signbytes == null) {
                return got;
            } else if (Arrays.equals(plus, signbytes)) {
                sign = 1;
                buf = stripSign(leadingSign, buf, plus);
            } else if (Arrays.equals(minus, signbytes)) {
                sign = -1;
                buf = stripSign(leadingSign, buf, minus);
            }
        } else if (signed) { /* process sign nybble */
            byte signByte = (leadingSign ? buf[0] : buf[got - 1]);
            byte signBits = (byte) (signByte & ZONE_NYBBLE_SIGN_BYTEMASK);
            byte valBits = (byte) (signByte & ZONE_NYBBLE_VALUE_BYTEMASK);
            int value = (int) valBits;
            if (value > 9) {
                return 0;
            }
            if (leadingSign) {
                buf[0] =
                        (byte) (valBits | (byte) ZONE_NYBBLE_SIGN_BYTEMASK);
            } else {
                buf[got - 1] =
                        (byte) (valBits | (byte) ZONE_NYBBLE_SIGN_BYTEMASK);
            }
            switch (signBits) {
                case POSITIVE_ZONE_SIGN:
                    sign = 1;
                    break;
                case UNSIGNED_ZONE_SIGN:
                    sign = 1;
                    break;
                case NEGATIVE_ZONE_SIGN:
                    sign = -1;
                    break;
                default:
                    return 0;
            }
        } else { /* no sign */
            sign = 1;
        }
        String svalue = new String(buf, enc);
        number[0] = svalue;
        data[0] = Long.parseLong(svalue) * sign;
        return got;
    }

    /**
     * Read (Cobol numeric) data from the input stream and interpret it as a
     * binary value.  The represenation returned DOES NOT HAVE DECIMAL POINT
     * INFORMATION.
     *
     * @param stream Data inlet
     * @param data   Array to hold the read value; only the first element of the
     *               array is used.
     * @param count  The number of bytes to process from the stream as a binary
     *               value
     *
     * @return The number of bytes consumed to process the data, or -1 if there
     *         was no or insufficient data to process
     *
     * @throws IOException if an I/O error occurs attempting to read
     *                             from the input stream
     */
    private static int readBinaryNumber(InputStream stream,
            long[] data,
            int count)
            throws IOException {

        BigInteger value;
        byte[] bytes = new byte[count];
        int got = 0;

        /* get value (number) */
        got = stream.read(bytes);
        if (got != count) {
            return (-1);
        }
        value = new BigInteger(bytes);

        data[ 0] = value.longValue();
        return got;
    }

    /**
     * Read (Cobol numeric) data from the input stream and interpret it as a
     * packed-decimal value.
     *
     * @param stream Data inlet
     * @param data   Array to hold the read value; only the first element of the
     *               array is used.
     * @param count  The number of bytes to process from the stream as a
     *               packed-decimal value
     *
     * @return The number of bytes consumed to process the data, or -1 if there
     *         was no or insufficient data to process
     *
     * @throws IOException if an I/O error occurs attempting to read
     *                             from the input stream
     */
    private static int readPackedNumber(InputStream stream,
            long[] data,
            int count)
            throws IOException {

        byte[] bytes = new byte[count];
        int got = 0;

        /* get value (number) */
        got = stream.read(bytes);
        if (got != count) {
            Message msg = MessageCatalog.getMessage("CCCR4014");
            cErrorMgr.log(ErrorManager.Severity.ERROR,
                    null,
                    msg.formatText(new Object[]{
                        "COMP-3",
                        String.valueOf(count),
                        String.valueOf(Math.max(0, got))
                    }));
            return (-1);
        }

        /* get sign */
        byte signByte =
                (byte) (bytes[count - 1] & PACKED_NYBBLE_SIGN_BYTEMASK);

        /* decode value */
        count -= 1;
        long value = 0L;
        for (int i = 0; i < count; i++) {
            int digit = (((int) bytes[i]) & 0x000000F0) >> 4;
            if (digit < 0 || digit > 9) {
                Message msg = MessageCatalog.getMessage("CCCR4019");
                cErrorMgr.log(ErrorManager.Severity.ERROR,
                        null,
                        msg.formatText(new Object[]{
                            "COMP-3",
                            String.valueOf((char) digit),
                            String.valueOf(digit),
                            String.valueOf(i),
                            Integer.toHexString(bytes[i])
                        }));
                return (-1);
            }
            value += digit;
            value *= 10;

            digit = (int) bytes[i] & 0x0000000F;
            if (digit < 0 || digit > 9) {
                Message msg = MessageCatalog.getMessage("CCCR4020");
                cErrorMgr.log(ErrorManager.Severity.ERROR,
                        null,
                        msg.formatText(new Object[]{
                            "COMP-3",
                            String.valueOf((char) digit),
                            String.valueOf(digit),
                            String.valueOf(i),
                            Integer.toHexString(bytes[i])
                        }));
                return (-1);
            }
            value += digit;
            value *= 10;
        }
        int digit = (((int) bytes[count]) & 0x000000F0) >> 4;
        if (digit < 0 || digit > 9) {
            Message msg = MessageCatalog.getMessage("CCCR4019");
            cErrorMgr.log(ErrorManager.Severity.ERROR,
                    null,
                    msg.formatText(new Object[]{
                        "COMP-3",
                        String.valueOf((char) digit),
                        String.valueOf(digit),
                        String.valueOf(count),
                        Integer.toHexString(bytes[count])
                    }));
            return (-1);
        }
        value += digit;
        if (signByte == NEGATIVE_PACK_SIGN) {
            value *= (-1);
        }
        data[ 0] = value;

        return got;
    }

    // collapse all contiguous picture symbols into X(n) notation 
    private static String compressedPic(String pic) {
        StringBuffer buf = new StringBuffer(pic);
        char lastSymbol = buf.charAt(0);
        int occurs = 1;

        for (int i = 1; i < buf.length(); i++) {
            char symbol = buf.charAt(i);

            // External floating point, e.g., 9V9E+99
            // On detecting it, stop trying to compress the pic
            if (symbol == 'E') {
                break;
            }

            int lastpos = buf.length() - 1;
            if (lastSymbol == symbol) {
                occurs++;
            }
            if (lastSymbol != symbol || i == lastpos) {
                if (occurs > 1) {
                    int cutpos =
                            i - occurs + 1 + (lastSymbol == symbol ? 1 : 0);
                    buf.delete(cutpos, (lastSymbol == symbol ? i + 1 : i));
                    buf.insert(cutpos, ')');
                    buf.insert(cutpos, occurs);
                    buf.insert(cutpos, '(');
                    occurs = 1;
                    i = buf.indexOf(")", cutpos) + 1;
                }
                lastSymbol = symbol;
            }
        }

        return buf.toString();
    }

    // copies the value in buf to a new array, sans sign
    private static byte[] stripSign(boolean leadingSign,
            byte[] buf,
            byte[] sign) {

        byte[] newbuf = new byte[buf.length - sign.length];
        System.arraycopy(buf,
                (leadingSign ? sign.length : 0),
                newbuf,
                0,
                newbuf.length);
        return newbuf;
    }

    // Evaluates what sign the value in buf contains; if it's positive signed,
    // returns the plus array, otherwise returns the minus array
    private static byte[] getSeparateSign(boolean leadingSign,
            byte[] buf,
            byte[] plus,
            byte[] minus) {

        int maxsignlen = Math.max(plus.length, minus.length);

        if (buf.length <= maxsignlen) {
            return null;
        }

        byte[] bbuf = new byte[maxsignlen];

        if (leadingSign) {
            System.arraycopy(buf,
                    0,
                    bbuf,
                    0,
                    maxsignlen);
        } else {
            int startOffset = Math.max(0, buf.length - maxsignlen - 1);
            int endOffset = Math.min(buf.length - startOffset, maxsignlen);
            System.arraycopy(buf, startOffset, bbuf, 0, endOffset);
        }

        if (Arrays.equals(plus, bbuf)) {
            return plus;
        } else if (Arrays.equals(minus, bbuf)) {
            return minus;
        } else {
            return null;
        }
    }

    // Returns the bytes representation of a string of characters in the
    // requested encoding
    private static byte[] charsToBytes(char[] chars, String enc)
            throws CharacterCodingException,
            UnsupportedEncodingException {
        Charset Lcs;
        byte[] Ldecoded;

        try {
            Lcs = Charset.forName(enc);
        } catch (Exception e) {
            Lcs = null;
        }

        if (Lcs != null) {
            ByteArrayOutputStream Lbs;
            ByteBuffer Lbb;
            Lbb = Lcs.newEncoder().encode(CharBuffer.wrap(chars));
            Lbs = new ByteArrayOutputStream(Lbb.limit() - Lbb.position());
            while (Lbb.hasRemaining()) {
                Lbs.write(Lbb.get());
            }
            Ldecoded = Lbs.toByteArray();
        } else {
            String Lchars = new String(chars);
            Ldecoded = Lchars.getBytes(enc);
        }

        return Ldecoded;
    }

    // Create InputStreamReader wrapped around the stream, using the
    // specified encoding
    private static InputStreamReader createReader(InputStream stream,
            String enc)
            throws UnsupportedEncodingException {
        InputStreamReader reader = new InputStreamReader(stream, enc);
        return reader;
    }

    /**
     * Truncate or pad a numeric value according to rules for internal decimal,
     * zoned decimal, and binary numeric items. The results of any truncation or
     * padding are reflected in the value, which is supplied to this method in a
     * buffer.
     *
     * @param value            Text buffer containing the unscaled value
     * @param scale            Scale (number of digits to the right of the
     *                         decimal point) of the numeric value contained in
     *                         <code>value</code>
     * @param digitsPic        Number of digits for this item
     * @param scalePic         Position of the decimal point for this item
     * @param scalingPositions Scaling positions for this item
     *
     * @return The scale of the value in the buffer, after it is fitted in the
     *         picture
     *
     * @see #fitToNumericPicture(StringBuffer, int, int, int, int)
     */
    private static int fitToNumericPicture(StringBuffer value,
            int scale,
            int digitsPic,
            int scalePic,
            int scalingPositions) {

        final boolean isNegativeValue;

        // remove negative sign if present, it complicates offset calcs
        if (isNegativeValue = (value.charAt(0) == '-')) {
            value.delete(0, 1);
        }

        // Scaling decimal positions (PIC symbol P):
        if (scalingPositions > 0) {

            // Remove side of value not used
            // if scaling to the right of the point, remove digits on left of it
            if (scalePic > 0) {
                int truncsize = Math.max(0, value.length() - scale);
                value.delete(0, truncsize);
            } else {
                value.delete(value.length() - scale, value.length());
                scale = 0;
            }
            if (value.length() == 0) {
                value.append('0');
            }

            // Pad the value to match the width of the picture
            // including the implied scaling positions
            int addsize = Math.max(0,
                    (digitsPic + scalingPositions) - value.length());
            if (scalePic > 0) {
                for (int i = 0; i < addsize; i++) {
                    value.append('0');
                }
                scale += addsize;
            } else {
                for (int i = 0; i < addsize; i++) {
                    value.insert(0, '0');
                }
            }

            // Truncate the value
            int truncsize = Math.max(
                    0, value.length() - (digitsPic + scalingPositions));

            if (scalePic > 0) {
                value.delete(value.length() - truncsize, value.length());
                scale -= truncsize;
            } else {
                value.delete(0, truncsize);
            }


            // Value's digits are all lined up to the picture's
            // Chop off all the digits corresponding to the scaling positions
            if (scalePic > 0) {
                value.delete(0, scalingPositions);
                scale -= scalingPositions;
            } else {
                value.delete(value.length() - scalingPositions,
                        value.length());
            }
        }

        // No scaling decimal (may or may not have PIC symbol V)
        if (scalingPositions == 0) {

            // Truncate or pad the value
            int diff;

            // non-fraction
            diff = (digitsPic - scalePic) - (value.length() - scale);
            if (diff < 1) {
                value.delete(0, Math.abs(diff));
            } else {
                for (int i = 0; i < diff; i++) {
                    value.insert(0, '0');
                }
            }

            // fraction
            diff = scalePic - scale;
            if (diff < 0) {
                value.delete(value.length() + diff, value.length());
                scale += diff;
            } else {
                scale += diff;
                for (int i = 0; i < diff; i++) {
                    value.append('0');
                }
            }
        }

        // restore negative sign if it was removed
        if (isNegativeValue) {
            value.insert(0, '-');
        }

        return scale;
    }

    /**
     * Truncate or pad a numeric value according to rules for native binary
     * (COMP-5) items. The results of any truncation or padding are reflected in
     * the value, which is supplied to this method in a buffer.
     *
     * @param value            Text buffer containing the unscaled value
     * @param scale            Scale (number of digits to the right of the
     *                         decimal point) of the numeric value contained in
     *                         <code>value</code>
     * @param digitsPic        Number of digits for this item
     * @param scalePic         Position of the decimal point for this item
     * @param scalingPositions Scaling positions for this item
     * @param size             Storage capacity (in bytes) for this item
     *
     * @return The scale of the value in the buffer, after it is fitted in the
     *         picture
     *
     * @see #fitToNumericPicture(StringBuffer, int, int, int, int)
     */
    private static int fitToNumericNativePicture(StringBuffer value,
            int scale,
            int digitsPic,
            int scalePic,
            int scalingPositions,
            int size) {

        final boolean isNegativeValue;

        // remove negative sign if present, it complicates offset calcs
        if (isNegativeValue = (value.charAt(0) == '-')) {
            value.delete(0, 1);
        }

        // Scaling decimal positions (PIC symbol P):
        if (scalingPositions > 0) {

            // Remove side of value not used
            // if scaling to the right of the point, remove digits on left of it
            if (scalePic > 0) {
                int truncsize = Math.max(0, value.length() - scale);
                value.delete(0, truncsize);
            } else {
                value.delete(value.length() - scale, value.length());
                scale = 0;
            }
            if (value.length() == 0) {
                value.append('0');
            }

            // Pad the value to match the width of the picture
            // including the implied scaling positions
            int addsize = Math.max(
                    0, (digitsPic + scalingPositions) - value.length());

            if (scalePic > 0) {
                for (int i = 0; i < addsize; i++) {
                    value.append('0');
                }
                scale += addsize;
            } else {
                for (int i = 0; i < addsize; i++) {
                    value.insert(0, '0');
                }
            }

            // Value's digits are all lined up to the picture's
            // Chop off all the digits corresponding to the scaling positions
            if (scalePic > 0) {
                value.delete(0, scalingPositions);
                scale -= scalingPositions;
            } else {
                value.delete(value.length() - scalingPositions,
                        value.length());
            }
        }

        // No scaling decimal (may or may not have PIC symbol V)
        if (scalingPositions == 0) {

            // Truncate or pad the value
            int diff;

            // non-fraction
            diff = (digitsPic - scalePic) - (value.length() - scale);
            if (diff < 1) {
                value.delete(0, Math.abs(diff));
            } else {
                for (int i = 0; i < diff; i++) {
                    value.insert(0, '0');
                }
            }

            // fraction
            diff = scalePic - scale;
            if (diff < 0) {
                value.delete(value.length() + diff, value.length());
                scale += diff;
            } else {
                scale += diff;
                for (int i = 0; i < diff; i++) {
                    value.append('0');
                }
            }
        }

        // restore negative sign if it was removed
        if (isNegativeValue) {
            value.insert(0, '-');
        }

        // Truncate the unscaled value, if necessary, starting from the most
        // significant byte, to fit it into the storage capacity
        BigInteger unscaledValue = new BigInteger(value.toString());
        byte[] unscaledBytes = unscaledValue.toByteArray();
        if (unscaledBytes.length > size) {
            int diff = unscaledBytes.length - size;
            byte[] truncBytes = new byte[size];
            System.arraycopy(unscaledBytes, diff, truncBytes, 0, size);
            unscaledValue = new BigInteger(truncBytes);
            value.delete(0, value.length());
            value.append(unscaledValue.toString());
        }

        return scale;
    }
}
// EOF $RCSfile: CobolDataConverter.java,v $

