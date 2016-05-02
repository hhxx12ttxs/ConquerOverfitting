/**
 * A library to interact with Virtual Worlds such as OpenSim
 * Copyright (C) 2012  Jitendra Chauhan, Email: jitendra.chauhan@gmail.com
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package jj2000.j2k.wavelet.analysis;

import jj2000.j2k.image.*;

/**
 * This is a generic abstract class to store a code-block of wavelet data,
 * quantized or not. This class does not have the notion of
 * components. Therefore, it should be used for data from a single
 * component. Subclasses should implement the different types of storage
 * (<tt>int</tt>, <tt>float</tt>, etc.).
 *
 * <p>The data is always stored in one array, of the type matching the data
 * type (i.e. for 'int' it's an 'int[]'). The data should be stored in the
 * array in standard scan-line order. That is the samples go from the top-left
 * corner of the code-block to the lower-right corner by line and then
 * column.</p>
 *
 * <p>The member variable 'offset' gives the index in the array of the first
 * data element (i.e. the top-left coefficient). The member variable 'scanw'
 * gives the width of the scan that is used to store the data, that can be
 * different from the width of the block. Element '(x,y)' of the code-block
 * (i.e. '(0,0)' is the top-left coefficient), will appear at position
 * 'offset+y*scanw+x' in the array of data.</p>
 *
 * <p>The classes <tt>CBlkWTDataInt</tt> and <tt>CBlkWTDataFloat</tt> provide
 * implementations for <tt>int</tt> and <tt>float</tt> types respectively.</p>
 *
 * <p>The types of data are the same as those defined by the 'DataBlk'
 * class.</p>
 *
 * @see CBlkWTDataSrc
 * @see jj2000.j2k.quantization.quantizer.CBlkQuantDataSrcEnc
 * @see DataBlk
 * @see CBlkWTDataInt
 * @see CBlkWTDataFloat
 * */
public abstract class CBlkWTData {

    /** The horizontal coordinate of the upper-left corner of the code-block */
    public int ulx;

    /** The vertical coordinate of the upper left corner of the code-block */
    public int uly;

    /** The horizontal index of the code-block, within the subband */
    public int n;

    /** The vertical index of the code-block, within the subband */
    public int m;

    /** The subband in which this code-block is found */
    public SubbandAn sb;

    /** The width of the code-block */
    public int w;

    /** The height of the code-block */
    public int h;

    /** The offset in the array of the top-left coefficient */
    public int offset;

    /** The width of the scanlines used to store the data in the array */
    public int scanw;

    /** The number of magnitude bits in the integer representation. This is
     * only used for quantized wavelet data. */
    public int magbits;

    /** The WMSE scaling factor (multiplicative) to apply to the distortion
     * measures of the data of this code-block. By default it is 1.*/
    public float wmseScaling = 1f;

    /** The value by which the absolute value of the data has to be divided in
     * order to get the real absolute value. This value is useful to obtain
     * the complement of 2 representation of a coefficient that is currently
     * using the sign-magnitude representation. */
    public double convertFactor = 1.0;

    /** The quantization step size of the code-block. The value is updated by
     * the quantizer module */
    public double stepSize = 1.0;

    /** Number of ROI coefficients in the code-block */
    public int nROIcoeff = 0;

    /** Number of ROI magnitude bit-planes */
    public int nROIbp = 0;

    /**
     * Returns the data type of the <tt>CBlkWTData</tt> object, as defined in
     * the DataBlk class.
     *
     * @return The data type of the object, as defined in the DataBlk class.
     *
     * @see DataBlk
     * */
    public abstract int getDataType();

    /**
     * Returns the array containing the data, or null if there is no data. The
     * returned array is of the type returned by <tt>getDataType()</tt> (e.g.,
     * for <tt>TYPE_INT</tt>, it is a <tt>int[]</tt>).
     *
     * <p>Each implementing class should provide a type specific equivalent
     * method (e.g., <tt>getDataInt()</tt> in <tt>DataBlkInt</tt>) which
     * returns an array of the correct type explicitely and not through an
     * <tt>Object</tt>.</p>
     *
     * @return The array containing the data, or <tt>null</tt> if there is no
     * data.
     *
     * @see #getDataType
     * */
    public abstract Object getData();

    /**
     * Sets the data array to the specified one. The type of the specified
     * data array must match the one returned by <tt>getDataType()</tt> (e.g.,
     * for <tt>TYPE_INT</tt>, it should be a <tt>int[]</tt>). If the wrong
     * type of array is given a <tt>ClassCastException</tt> will be thrown.
     *
     * <p>The size of the array is not necessarily checked for consistency
     * with <tt>w</tt> and <tt>h</tt> or any other fields.</p>
     *
     * <p>Each implementing class should provide a type specific equivalent
     * method (e.g., <tt>setDataInt()</tt> in <tt>DataBlkInt</tt>) which takes
     * an array of the correct type explicetely and not through an
     * <tt>Object</tt>.</p>
     *
     * @param arr The new data array to use
     *
     * @see #getDataType
     * */
    public abstract void setData(Object arr);

    /**
     * Returns a string of informations about the DataBlk
     *
     * @return Block dimensions and progressiveness in a string
     * */
    public String toString(){
	String typeString = "";
	switch(getDataType()){
	case  DataBlk.TYPE_BYTE:
	    typeString = "Unsigned Byte";
	    break;
	case  DataBlk.TYPE_SHORT:
	    typeString = "Short";
	    break;
	case  DataBlk.TYPE_INT:
	    typeString = "Integer";
	    break;
	case  DataBlk.TYPE_FLOAT:
	    typeString = "Float";
	    break;
	}

	return
	    "ulx="+ulx+", uly="+uly+", idx=("+m+","+n+"), w="+w+
	    ", h="+h+", off="+offset+", scanw="+scanw+", wmseScaling="+
            wmseScaling+", convertFactor="+convertFactor+", stepSize="+
            stepSize+
	    ", type="+typeString +", magbits="+magbits+", nROIcoeff="+
            nROIcoeff+", nROIbp="+nROIbp;
    }
}

