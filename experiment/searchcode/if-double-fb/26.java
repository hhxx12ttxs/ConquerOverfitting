/*
 *  Copyright (C) 2010-2011 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jpexs.asdec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.jpexs.asdec.abc.NotSameException;
import com.jpexs.asdec.helpers.Helper;
import com.jpexs.asdec.tags.Tag;
import com.jpexs.asdec.types.BUTTONCONDACTION;
import com.jpexs.asdec.types.BUTTONRECORD;
import com.jpexs.asdec.types.CLIPACTIONRECORD;
import com.jpexs.asdec.types.CLIPACTIONS;
import com.jpexs.asdec.types.CLIPEVENTFLAGS;
import com.jpexs.asdec.types.CXFORMWITHALPHA;
import com.jpexs.asdec.types.MATRIX;
import com.jpexs.asdec.types.RECT;
import com.jpexs.asdec.types.RGBA;
import com.jpexs.asdec.types.filters.BEVELFILTER;
import com.jpexs.asdec.types.filters.BLURFILTER;
import com.jpexs.asdec.types.filters.COLORMATRIXFILTER;
import com.jpexs.asdec.types.filters.CONVOLUTIONFILTER;
import com.jpexs.asdec.types.filters.DROPSHADOWFILTER;
import com.jpexs.asdec.types.filters.FILTER;
import com.jpexs.asdec.types.filters.GLOWFILTER;
import com.jpexs.asdec.types.filters.GRADIENTBEVELFILTER;
import com.jpexs.asdec.types.filters.GRADIENTGLOWFILTER;

/**
 * Class for writing data into SWF file
 *
 * @author JPEXS
 */
public class SWFOutputStream extends OutputStream {
    private OutputStream os;
    private int version;
    private long pos = 0;

    /**
     * Constructor
     *
     * @param os      OutputStream for writing data
     * @param version Version of SWF
     */
    public SWFOutputStream(OutputStream os, int version) {
        this.version = version;
        this.os = os;
    }

    /**
     * Writes byte to the stream
     *
     * @param b byte to write
     * @throws IOException
     */
    @Override
    public void write(int b) throws IOException {
        alignByte();
        os.write(b);
        pos++;
    }

    private void alignByte() throws IOException {
        if (bitPos > 0) {
            bitPos = 0;
            write(tempByte);
            tempByte = 0;
        }
    }

    /**
     * Writes UI8 (Unsigned 8bit integer) value to the stream
     *
     * @param val UI8 value to write
     * @throws IOException
     */
    public void writeUI8(int val) throws IOException {
        write(val);
    }

    /**
     * Writes String value to the stream
     *
     * @param value String value
     * @throws IOException
     */
    public void writeString(String value) throws IOException {
        write(value.getBytes("utf8"));
        write(0);
    }

    /**
     * Writes UI32 (Unsigned 32bit integer) value to the stream
     *
     * @param value UI32 value
     * @throws IOException
     */
    public void writeUI32(long value) throws IOException {
        write((int) (value & 0xff));
        write((int) ((value >> 8) & 0xff));
        write((int) ((value >> 16) & 0xff));
        write((int) ((value >> 24) & 0xff));
    }

    /**
     * Writes UI16 (Unsigned 16bit integer) value to the stream
     *
     * @param value UI16 value
     * @throws IOException
     */
    public void writeUI16(int value) throws IOException {
        write((int) (value & 0xff));
        write((int) ((value >> 8) & 0xff));
    }

    /**
     * Writes SI32 (Signed 32bit integer) value to the stream
     *
     * @param value SI32 value
     * @throws IOException
     */
    public void writeSI32(long value) throws IOException {
        writeUI32(value);
    }

    /**
     * Writes SI16 (Signed 16bit integer) value to the stream
     *
     * @param value SI16 value
     * @throws IOException
     */
    public void writeSI16(int value) throws IOException {
        writeUI16(value);
    }

    /**
     * Writes SI8 (Signed 8bit integer) value to the stream
     *
     * @param value SI8 value
     * @throws IOException
     */
    public void writeSI8(int value) throws IOException {
        writeUI8(value);
    }

    /**
     * Writes FIXED (Fixed point 16.16) value to the stream
     *
     * @param value FIXED value
     * @throws IOException
     */
    public void writeFIXED(double value) throws IOException {
        long valueLong = (long) (value * (1 << 16));
        int beforePoint = (int) valueLong >> 16;

        int afterPoint = (int) valueLong % (1 << 16);
        writeUI16(afterPoint);
        writeUI16(beforePoint);
    }

    /**
     * Writes FIXED8 (Fixed point 8.8) value to the stream
     *
     * @param value FIXED8 value
     * @throws IOException
     */
    public void writeFIXED8(float value) throws IOException {
        int beforePoint = (int) getIntPart(value);
        int afterPoint = (int) getIntPart((value + (value < 0 ? beforePoint : -beforePoint)) * 256);
        writeUI8(afterPoint);
        writeUI8(beforePoint);
    }

    private void writeLong(long value) throws IOException {
        byte writeBuffer[] = new byte[8];
        writeBuffer[3] = (byte) (value >>> 56);
        writeBuffer[2] = (byte) (value >>> 48);
        writeBuffer[1] = (byte) (value >>> 40);
        writeBuffer[0] = (byte) (value >>> 32);
        writeBuffer[7] = (byte) (value >>> 24);
        writeBuffer[6] = (byte) (value >>> 16);
        writeBuffer[5] = (byte) (value >>> 8);
        writeBuffer[4] = (byte) (value >>> 0);
        write(writeBuffer);
    }

    /**
     * Writes DOUBLE (double precision floating point value) value to the stream
     *
     * @param value DOUBLE value
     * @throws IOException
     */
    public void writeDOUBLE(double value) throws IOException {
        writeLong(Double.doubleToLongBits(value));
    }

    /**
     * Writes FLOAT (single precision floating point value) value to the stream
     *
     * @param value FLOAT value
     * @throws IOException
     */
    public void writeFLOAT(float value) throws IOException {
        writeUI32(Float.floatToIntBits(value));
    }

    /**
     * Writes FLOAT16 (16bit floating point value) value to the stream
     *
     * @param value FLOAT16 value
     * @throws IOException
     */
    public void writeFLOAT16(float value) throws IOException {
        int bits = Float.floatToRawIntBits(value);
        int sign = bits >> 31;
        int exponent = (bits >> 22) & 0xff;
        int mantisa = bits & 0x3FFFFF;
        mantisa = mantisa >> 13;
        writeUI16((sign << 15) + (exponent << 10) + mantisa);
    }

    /**
     * Writes EncodedU32 (Encoded unsigned 32bit value) value to the stream
     *
     * @param value U32 value
     * @throws IOException
     */
    public void writeEncodedU32(long value) throws IOException {
        boolean loop = true;
        value = value & 0xFFFFFFFF;
        do {
            int ret = (int) (value & 0x7F);
            if (value < 0x80) {
                loop = false;
            }
            if (value > 0x7F) {
                ret += 0x80;
            }
            write(ret);
            value = value >> 7;
        } while (loop);
    }

    private int bitPos = 0;
    private int tempByte = 0;

    /**
     * Flushes data to underlying stream
     *
     * @throws IOException
     */
    @Override
    public void flush() throws IOException {
        if (bitPos > 0) {
            bitPos = 0;
            write(tempByte);
            tempByte = 0;
        }
        os.flush();
    }

    /**
     * Closes the stream
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        flush();
        os.close();
    }

    /**
     * Writes UB[nBits] (Unsigned-bit value) value to the stream
     *
     * @param nBits Number of bits which represent value
     * @param value Unsigned value to write
     * @throws IOException
     */
    public void writeUB(int nBits, long value) throws IOException {
        for (int bit = 0; bit < nBits; bit++) {
            int nb = (int) ((value >> (nBits - 1 - bit)) & 1);
            tempByte += nb * (1 << (7 - bitPos));
            bitPos++;
            if (bitPos == 8) {
                bitPos = 0;
                write(tempByte);
                tempByte = 0;
            }
        }
    }

    /**
     * Writes SB[nBits] (Signed-bit value) value to the stream
     *
     * @param nBits Number of bits which represent value
     * @param value Signed value to write
     * @throws IOException
     */
    public void writeSB(int nBits, long value) throws IOException {
        writeUB(nBits, value);
    }

    /**
     * Writes FB[nBits] (Signed fixed-point bit value) value to the stream
     *
     * @param nBits Number of bits which represent value
     * @param value Double value to write
     * @throws IOException
     */
    public void writeFB(int nBits, double value) throws IOException {
        long longVal = (long) (value * (1 << 16));
        writeSB(nBits, longVal);
    }

    /**
     * Writes RECT value to the stream
     *
     * @param value RECT value
     * @throws IOException
     */
    public void writeRECT(RECT value) throws IOException {
        int nBits = 0;
        nBits = enlargeBitCountS(nBits, value.Xmin);
        nBits = enlargeBitCountS(nBits, value.Xmax);
        nBits = enlargeBitCountS(nBits, value.Ymin);
        nBits = enlargeBitCountS(nBits, value.Ymax);

        writeUB(5, nBits);
        writeSB(nBits, value.Xmin);
        writeSB(nBits, value.Xmax);
        writeSB(nBits, value.Ymin);
        writeSB(nBits, value.Ymax);
    }

    /**
     * Writes list of Tag values to the stream
     *
     * @param tags List of tag values
     * @throws IOException
     */
    public void writeTags(List<Tag> tags) throws IOException {
        for (Tag tag : tags) {
        	//try {
        		writeTag(tag);
        	/*} catch (NotSameException nse) {
        		throw new RuntimeException("error in tag "+tag+" at pos "+Helper.formatHex((int)tag.getPos(), 8), nse);
        	}*/
         //NotSameException must be processed in order to catch it elsewhere
        }
    }

    /**
     * Writes Tag value to the stream
     *
     * @param tag Tag value
     * @throws IOException
     */
    public void writeTag(Tag tag) throws IOException {
        byte data[] = tag.getData(version);
        int tagLength = data.length;
        int tagID = tag.getId();
        int tagIDLength = (tagID << 6);
        if ((tagLength < 0x3f) && (!tag.forceWriteAsLong)) {
            tagIDLength += tagLength;
            writeUI16(tagIDLength);
        } else {
            tagIDLength += 0x3f;
            writeUI16(tagIDLength);
            writeSI32(tagLength);
        }
        write(data);
    }


    /**
     * Get needed bits
     * @param number
     * @param bits 1 for signed,0 if unsigned
     * @return
     */
    public static int getNeededBits(int number, int bits)
	{
        number=Math.abs(number);
        int val = 1;
        for (int x = 1; val <= number && !(bits > 32); x <<= 1)
        {
            val = val | x;
            bits++;
        }

        if (bits > 32)
		{
			assert false : ("minBits " + bits + " must not exceed 32");
		}
        return bits;
    }


    /**
     * Calculates number of bits needed for representing unsigned value
     *
     * @param v Unsigned value
     * @return Number of bits
     */
    public static int getNeededBitsU(int v) {

       return getNeededBits(v,0);
    }

    /**
     * Calculates number of bits needed for representing signed value
     *
     * @param v Signed value
     * @return Number of bits
     */
    public static int getNeededBitsS(int v) {
        return getNeededBits(v,1);
    }


    private static long getIntPart(double value) {
        if (value < 0) return (long) Math.ceil(value);
        return (long) Math.floor(value);
    }

    private static double getFloatPart(double value) {
        if (value < 0) return value - getIntPart(value);
        return value + getIntPart(value);
    }

    /**
     * Calculates number of bits needed for representing fixed-point value
     *
     * @param value Fixed-point value
     * @return Number of bits
     */
    public static int getNeededBitsF(double value) {
        if (value == -1) return 18;
        int val = (int) (value * (1 << 16));
        return getNeededBitsS(val);
    }

    private int enlargeBitCountU(int currentBitCount, int value) {
        int neededNew = getNeededBitsU(value);
        if (neededNew > currentBitCount) return neededNew;
        return currentBitCount;
    }

    private int enlargeBitCountS(int currentBitCount, int value) {
        int neededNew = getNeededBitsS(value);
        if (neededNew > currentBitCount) return neededNew;
        return currentBitCount;
    }

    private int enlargeBitCountF(int currentBitCount, double value) {
        int neededNew = getNeededBitsF(value);
        if (neededNew > currentBitCount) return neededNew;
        return currentBitCount;
    }

    /**
     * Writes MATRIX value to the stream
     *
     * @param value MATRIX value
     * @throws IOException
     */
    public void writeMatrix(MATRIX value) throws IOException {
        writeUB(1, value.hasScale ? 1 : 0);
        if (value.hasScale) {
            int nBits = 0;
            nBits = enlargeBitCountF(nBits, value.scaleX);
            nBits = enlargeBitCountF(nBits, value.scaleY);
            nBits = value.scaleNBits; //FFFUUU
            writeUB(5, nBits);
            writeFB(nBits, value.scaleX);
            writeFB(nBits, value.scaleY);
        }
        writeUB(1, value.hasRotate ? 1 : 0);
        if (value.hasRotate) {
            int nBits = 0;
            nBits = enlargeBitCountF(nBits, value.rotateSkew0);
            nBits = enlargeBitCountF(nBits, value.rotateSkew1);
            nBits = value.rotateNBits; //FFFUUU
            writeUB(5, nBits);
            writeFB(nBits, value.rotateSkew0);
            writeFB(nBits, value.rotateSkew1);
        }
        int NTranslateBits = 0;
        NTranslateBits = enlargeBitCountS(NTranslateBits, value.translateX);
        NTranslateBits = enlargeBitCountS(NTranslateBits, value.translateY);
        NTranslateBits = value.translateNBits; //FFFUUU
        writeUB(5, NTranslateBits);


        writeSB(NTranslateBits, value.translateX);
        writeSB(NTranslateBits, value.translateY);
        alignByte();

    }

    /**
     * Writes CXFORMWITHALPHA value to the stream
     *
     * @param value CXFORMWITHALPHA value
     * @throws IOException
     */
    public void writeCXFORMWITHALPHA(CXFORMWITHALPHA value) throws IOException {
        writeUB(1, value.hasAddTerms ? 1 : 0);
        writeUB(1, value.hasMultTerms ? 1 : 0);
        int Nbits = 1;
        if (value.hasMultTerms) {
            Nbits = enlargeBitCountS(Nbits, value.redMultTerm);
            Nbits = enlargeBitCountS(Nbits, value.greenMultTerm);
            Nbits = enlargeBitCountS(Nbits, value.blueMultTerm);
            Nbits = enlargeBitCountS(Nbits, value.alphaMultTerm);
        }
        if (value.hasAddTerms) {
            Nbits = enlargeBitCountS(Nbits, value.redAddTerm);
            Nbits = enlargeBitCountS(Nbits, value.greenAddTerm);
            Nbits = enlargeBitCountS(Nbits, value.blueAddTerm);
            Nbits = enlargeBitCountS(Nbits, value.alphaAddTerm);
        }
        writeUB(4, Nbits);
        if (value.hasMultTerms) {
            writeSB(Nbits, value.redMultTerm);
            writeSB(Nbits, value.greenMultTerm);
            writeSB(Nbits, value.blueMultTerm);
            writeSB(Nbits, value.alphaMultTerm);
        }        
        if (value.hasAddTerms) {
            writeSB(Nbits, value.redAddTerm);
            writeSB(Nbits, value.greenAddTerm);
            writeSB(Nbits, value.blueAddTerm);
            writeSB(Nbits, value.alphaAddTerm);
        }
        alignByte();
    }

    /**
     * Writes CLIPEVENTFLAGS value to the stream
     *
     * @param value CLIPEVENTFLAGS value
     * @throws IOException
     */
    public void writeCLIPEVENTFLAGS(CLIPEVENTFLAGS value) throws IOException {
        writeUB(1, value.clipEventKeyUp ? 1 : 0);
        writeUB(1, value.clipEventKeyDown ? 1 : 0);
        writeUB(1, value.clipEventMouseUp ? 1 : 0);
        writeUB(1, value.clipEventMouseDown ? 1 : 0);
        writeUB(1, value.clipEventMouseMove ? 1 : 0);
        writeUB(1, value.clipEventUnload ? 1 : 0);
        writeUB(1, value.clipEventEnterFrame ? 1 : 0);
        writeUB(1, value.clipEventLoad ? 1 : 0);
        writeUB(1, value.clipEventDragOver ? 1 : 0);
        writeUB(1, value.clipEventRollOut ? 1 : 0);
        writeUB(1, value.clipEventRollOver ? 1 : 0);
        writeUB(1, value.clipEventReleaseOutside ? 1 : 0);
        writeUB(1, value.clipEventRelease ? 1 : 0);
        writeUB(1, value.clipEventPress ? 1 : 0);
        writeUB(1, value.clipEventInitialize ? 1 : 0);
        writeUB(1, value.clipEventData ? 1 : 0);
        if (version >= 6) {
            writeUB(5, 0);
            writeUB(1, value.clipEventConstruct ? 1 : 0);
            writeUB(1, value.clipEventKeyPress ? 1 : 0);
            writeUB(1, value.clipEventDragOut ? 1 : 0);
            writeUB(8, 0);
        }
    }

    /**
     * Writes CLIPACTIONRECORD value to the stream
     *
     * @param value CLIPACTIONRECORD value
     * @throws IOException
     */
    public void writeCLIPACTIONRECORD(CLIPACTIONRECORD value) throws IOException {
        writeCLIPEVENTFLAGS(value.eventFlags);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);

        if (value.eventFlags.clipEventKeyPress) {
            sos.writeUI8(value.keyCode);
        }
        sos.write(value.actionBytes);
        //sos.write(Action.actionsToBytes(value.actions, true, version));
        sos.close();
        byte data[] = baos.toByteArray();
        writeUI32(data.length);     //actionRecordSize
        write(data);
    }


    /**
     * Writes CLIPACTIONS value to the stream
     *
     * @param value CLIPACTIONS value
     * @throws IOException
     */
    public void writeCLIPACTIONS(CLIPACTIONS value) throws IOException {
        writeUI16(0);//reserved
        writeCLIPEVENTFLAGS(value.allEventFlags);
        for (CLIPACTIONRECORD car : value.clipActionRecords) {
            writeCLIPACTIONRECORD(car);
        }
        if (version <= 5) {
            writeUI16(0);
        } else {
            writeUI32(0);
        }
    }

    /**
     * Writes COLORMATRIXFILTER value to the stream
     *
     * @param value COLORMATRIXFILTER value
     * @throws IOException
     */
    public void writeCOLORMATRIXFILTER(COLORMATRIXFILTER value) throws IOException {
        for (int i = 0; i < 20; i++) {
            writeFLOAT(value.matrix[i]);
        }
    }

    /**
     * Writes RGBA value to the stream
     *
     * @param value RGBA value
     * @throws IOException
     */
    public void writeRGBA(RGBA value) throws IOException {
        writeUI8(value.red);
        writeUI8(value.green);
        writeUI8(value.blue);
        writeUI8(value.alpha);
    }

    /**
     * Writes CONVOLUTIONFILTER value to the stream
     *
     * @param value CONVOLUTIONFILTER value
     * @throws IOException
     */
    public void writeCONVOLUTIONFILTER(CONVOLUTIONFILTER value) throws IOException {
        writeUI8(value.matrixX);
        writeUI8(value.matrixY);
        writeFLOAT(value.divisor);
        writeFLOAT(value.bias);
        for (int x = 0; x < value.matrixX; x++) {
            for (int y = 0; y < value.matrixY; y++) {
                writeFLOAT(value.matrix[x][y]);
            }
        }
        writeRGBA(value.defaultColor);
        writeUB(6, 0); //reserved
        writeUB(1, value.clamp ? 1 : 0);
        writeUB(1, value.preserveAlpha ? 1 : 0);
    }

    /**
     * Writes BLURFILTER value to the stream
     *
     * @param value BLURFILTER value
     * @throws IOException
     */
    public void writeBLURFILTER(BLURFILTER value) throws IOException {
        writeFIXED(value.blurX);
        writeFIXED(value.blurY);
        writeUB(5, value.passes);
        writeUB(3, 0);//reserved
    }

    /**
     * Writes DROPSHADOWFILTER value to the stream
     *
     * @param value DROPSHADOWFILTER value
     * @throws IOException
     */
    public void writeDROPSHADOWFILTER(DROPSHADOWFILTER value) throws IOException {
        writeRGBA(value.dropShadowColor);
        writeFIXED(value.blurX);
        writeFIXED(value.blurY);
        writeFIXED(value.angle);
        writeFIXED(value.distance);
        writeFIXED8(value.strength);
        writeUB(1, value.innerShadow ? 1 : 0);
        writeUB(1, value.knockout ? 1 : 0);
        writeUB(1, value.compositeSource ? 1 : 0);
        writeUB(5, value.passes);
    }

    /**
     * Writes GLOWFILTER value to the stream
     *
     * @param value GLOWFILTER value
     * @throws IOException
     */
    public void writeGLOWFILTER(GLOWFILTER value) throws IOException {
        writeRGBA(value.glowColor);
        writeFIXED(value.blurX);
        writeFIXED(value.blurY);
        writeFIXED8(value.strength);
        writeUB(1, value.innerGlow ? 1 : 0);
        writeUB(1, value.knockout ? 1 : 0);
        writeUB(1, value.compositeSource ? 1 : 0);
        writeUB(5, value.passes);
    }

    /**
     * Writes BEVELFILTER value to the stream
     *
     * @param value BEVELFILTER value
     * @throws IOException
     */
    public void writeBEVELFILTER(BEVELFILTER value) throws IOException {
        writeRGBA(value.shadowColor);
        writeRGBA(value.highlightColor);
        writeFIXED(value.blurX);
        writeFIXED(value.blurY);
        writeFIXED(value.angle);
        writeFIXED(value.distance);
        writeFIXED8(value.strength);
        writeUB(1, value.innerShadow ? 1 : 0);
        writeUB(1, value.knockout ? 1 : 0);
        writeUB(1, value.compositeSource ? 1 : 0);
        writeUB(1, value.onTop ? 1 : 0);
        writeUB(4, value.passes);
    }

    /**
     * Writes GRADIENTGLOWFILTER value to the stream
     *
     * @param value GRADIENTGLOWFILTER value
     * @throws IOException
     */
    public void writeGRADIENTGLOWFILTER(GRADIENTGLOWFILTER value) throws IOException {
        writeUI8(value.gradientColors.length);
        for (int i = 0; i < value.gradientColors.length; i++) {
            writeRGBA(value.gradientColors[i]);
        }
        for (int i = 0; i < value.gradientColors.length; i++) {
            writeUI8(value.gradientRatio[i]);
        }
        writeFIXED(value.blurX);
        writeFIXED(value.blurY);
        writeFIXED(value.angle);
        writeFIXED(value.distance);
        writeFIXED8(value.strength);
        writeUB(1, value.innerShadow ? 1 : 0);
        writeUB(1, value.knockout ? 1 : 0);
        writeUB(1, value.compositeSource ? 1 : 0);
        writeUB(1, value.onTop ? 1 : 0);
        writeUB(4, value.passes);
    }

    /**
     * Writes GRADIENTBEVELFILTER value to the stream
     *
     * @param value GRADIENTBEVELFILTER value
     * @throws IOException
     */
    public void writeGRADIENTBEVELFILTER(GRADIENTBEVELFILTER value) throws IOException {
        writeUI8(value.gradientColors.length);
        for (int i = 0; i < value.gradientColors.length; i++) {
            writeRGBA(value.gradientColors[i]);
        }
        for (int i = 0; i < value.gradientColors.length; i++) {
            writeUI8(value.gradientRatio[i]);
        }
        writeFIXED(value.blurX);
        writeFIXED(value.blurY);
        writeFIXED(value.angle);
        writeFIXED(value.distance);
        writeFIXED8(value.strength);
        writeUB(1, value.innerShadow ? 1 : 0);
        writeUB(1, value.knockout ? 1 : 0);
        writeUB(1, value.compositeSource ? 1 : 0);
        writeUB(1, value.onTop ? 1 : 0);
        writeUB(4, value.passes);
    }

    /**
     * Writes list of FILTER values to the stream
     *
     * @param list List of FILTER values
     * @throws IOException
     */
    public void writeFILTERLIST(List<FILTER> list) throws IOException {
        writeUI8(list.size());
        for (int i = 0; i < list.size(); i++) {
            writeFILTER(list.get(i));
        }
    }

    /**
     * Writes FILTER value to the stream
     *
     * @param value FILTER value
     * @throws IOException
     */
    public void writeFILTER(FILTER value) throws IOException {
        writeUI8(value.id);
        switch (value.id) {
            case 0:
                writeDROPSHADOWFILTER((DROPSHADOWFILTER) value);
                break;
            case 1:
                writeBLURFILTER((BLURFILTER) value);
                break;
            case 2:
                writeGLOWFILTER((GLOWFILTER) value);
                break;
            case 3:
                writeBEVELFILTER((BEVELFILTER) value);
                break;
            case 4:
                writeGRADIENTGLOWFILTER((GRADIENTGLOWFILTER) value);
                break;
            case 5:
                writeCONVOLUTIONFILTER((CONVOLUTIONFILTER) value);
                break;
            case 6:
                writeCOLORMATRIXFILTER((COLORMATRIXFILTER) value);
                break;
            case 7:
                writeGRADIENTBEVELFILTER((GRADIENTBEVELFILTER) value);
                break;
        }
    }

    /**
     * Writes list of BUTTONRECORD values to the stream
     *
     * @param list            List of BUTTONRECORD values
     * @param inDefineButton2 Whether write inside of DefineButton2Tag or not
     * @throws IOException
     */
    public void writeBUTTONRECORDList(List<BUTTONRECORD> list, boolean inDefineButton2) throws IOException {
        for (BUTTONRECORD brec : list) {
            writeBUTTONRECORD(brec, inDefineButton2);
        }
        writeUI8(0);
    }

    /**
     * Writes BUTTONRECORD value to the stream
     *
     * @param value           BUTTONRECORD value
     * @param inDefineButton2 Whether write inside of DefineButton2Tag or not
     * @throws IOException
     */
    public void writeBUTTONRECORD(BUTTONRECORD value, boolean inDefineButton2) throws IOException {
        writeUB(2, 0);//reserved
        writeUB(1, value.buttonHasBlendMode ? 1 : 0);
        writeUB(1, value.buttonHasFilterList ? 1 : 0);
        writeUB(1, value.buttonStateHitTest ? 1 : 0);
        writeUB(1, value.buttonStateDown ? 1 : 0);
        writeUB(1, value.buttonStateOver ? 1 : 0);
        writeUB(1, value.buttonStateUp ? 1 : 0);
        writeUI16(value.characterId);
        writeUI16(value.placeDepth);
        writeMatrix(value.placeMatrix);
        if (inDefineButton2) {
            writeCXFORMWITHALPHA(value.colorTransform);
            if (value.buttonHasFilterList) {
                writeFILTERLIST(value.filterList);
            }
            if (value.buttonHasBlendMode) {
                writeUI8(value.blendMode);
            }
        }
    }

    /**
     * Writes list of BUTTONCONDACTION values to the stream
     *
     * @param list List of BUTTONCONDACTION values
     * @throws IOException
     */
    public void writeBUTTONCONDACTIONList(List<BUTTONCONDACTION> list) throws IOException {
        for (int i = 0; i < list.size(); i++) {
            writeBUTTONCONDACTION(list.get(i), i == list.size() - 1);
        }
    }

    /**
     * Writes BUTTONCONDACTION value to the stream
     *
     * @param value  BUTTONCONDACTION value
     * @param isLast True if it is last on the list
     * @throws IOException
     */
    public void writeBUTTONCONDACTION(BUTTONCONDACTION value, boolean isLast) throws IOException {
        BUTTONCONDACTION ret = new BUTTONCONDACTION();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        sos.writeUB(1, value.condIdleToOverDown ? 1 : 0);
        sos.writeUB(1, value.condOutDownToIdle ? 1 : 0);
        sos.writeUB(1, value.condOutDownToOverDown ? 1 : 0);
        sos.writeUB(1, value.condOverDownToOutDown ? 1 : 0);
        sos.writeUB(1, value.condOverDownToOverUp ? 1 : 0);
        sos.writeUB(1, value.condOverUpToOverDown ? 1 : 0);
        sos.writeUB(1, value.condOverUpToIddle ? 1 : 0);
        sos.writeUB(1, value.condIdleToOverUp ? 1 : 0);
        sos.writeUB(7, value.condKeyPress);
        sos.writeUB(1, value.condOverDownToIddle ? 1 : 0);
        sos.write(value.actionBytes);
        //sos.write(Action.actionsToBytes(value.actions, true, version));
        sos.close();
        byte data[] = baos.toByteArray();
        if (isLast) {
            writeUI16(0);
        } else {
            writeUI16(data.length + 2);
        }
        write(data);
    }
}

