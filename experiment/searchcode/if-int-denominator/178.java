<<<<<<< HEAD
/**
 * JHOVE2 - Next-generation architecture for format-aware characterization
 * <p>
 * Copyright (c) 2010 by The Regents of the University of California. All rights reserved.
 * </p>
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * </p>
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.</li>
 * <li>Neither the name of the University of California/California Digital
 * Library, Ithaka Harbors/Portico, or Stanford University, nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.</li>
 * </ul>
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * </p>
 */

package org.jhove2.module.format.icc.type;

import com.sleepycat.persist.model.Persistent;

/** ICC u16Fixed16Number, a fixed unsigned 4 byte/32 bit quantity with 16
 * fractional bits.  The number is organized as:
 * iiiiiiiiiiiiiiiiffffffffffffffff, with the value:
 * iiiiiiiiiiiiiiii + ffffffffffffffff/65536. See ICC.1:2004-10, \u00a7 5.1.4.
 * 
 * @author slabrams
 */
@Persistent
public class U16Fixed16Number
{
    /** Fractional denominator. */
    public static final int DENOMINATOR = 65536;
    
    /** Fractional part: 0x0000ffff. */
    protected int fractional;
    
    /** Unsigned integral part: 0xffff0000. */
    protected int integral;
    
    /** Floating point value. */
    protected double value;
    
    private U16Fixed16Number(){
    	super();
    }
    /** Instantiate a new <code>U16Fixed16Number</code>.
     */
    public U16Fixed16Number(long in) {
    	this();
        this.integral   = (int)((in & 0x00000000ffff0000) >> 16);
        this.fractional = (int)  in & 0x0000ffff;
        this.value      = this.integral +
                 ((double)this.fractional/(double)DENOMINATOR);
        /* Only keep 5 significant fractional digits. */
        long ln     = (long)(this.value*100000);
        this.value  = ln;
        this.value /= 100000;
    }
    
    /** Get fractional part.
     * @return Fractional part
     */
    public int getFractional() {
        return this.fractional;
    }
    
    /** Get integral part.
     * @return Integral part 
     */
    public int getIntegral() {
        return this.integral;
    }
    
    /** Get floating point value.
     * @return Floating point value
     */
    public double getValue() {
        return this.value;
    }
    
    /** String representation of the arithmetic representation of the
     * number: integral + fractional/65536.
     * @return String representation of the arithmetic representation
     */
    public String toArithmeticString() {
        return Integer.toString(this.integral)   + " + " +
               Integer.toString(this.fractional) +  "/"  +
               Integer.toString(DENOMINATOR);
    }
    
    /** String representation of the floating point value of the number.
     * @return String representation of the floating point value of number
     */
    @Override
    public String toString() {
        return Double.toString(this.value);
    }
}
=======
/*
 * Copyright (C) 2010 David Hoyt <dhoyt@hoytsoft.org>
 * Copyright (C) 2009 Levente Farkas
 * Copyright (C) 2009 Tamas Korodi <kotyo@zamba.fm>
 * Copyright (C) 2007 Wayne Meissner
 * Copyright (C) 2003 David A. Schleef <ds@schleef.org>
 *
 * This file contains code from the gstreamer-java project.
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package ossbuild.media.gstreamer;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import java.io.UnsupportedEncodingException;
import ossbuild.StringUtil;
import ossbuild.media.gstreamer.api.GType;
import ossbuild.media.gstreamer.api.GValue;
import static ossbuild.media.gstreamer.api.GObject.*;
import static ossbuild.media.gstreamer.api.GStreamer.*;

/**
 *
 * @author David Hoyt <dhoyt@hoytsoft.org>
 */
public class Structure extends BaseNativeObject {
	//<editor-fold defaultstate="collapsed" desc="Variables">
	boolean ownedByParent = true;
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="Initialization">
	public Structure(String name) {
		super();
		
		if (StringUtil.isNullOrEmpty(name))
			throw new IllegalArgumentException("name cannot be empty, it must start with a letter, and can then follow with any letter, number, or the following symbols: /-_.:");
		
		this.ptr = gst_structure_empty_new(name);
		this.managed = true;
		this.ownedByParent = false;
	}

	Structure(Pointer ptr) {
		super(ptr);
		this.managed = false;
		this.ownedByParent = true;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="Dispose">
	@Override
	protected void disposeObject() {
		//TODO: There's no current way to figure out if a GstStructure has
		//      a parent or not. As such, when you create a structure from
		//      scratch that isn't parented, it needs to be freed. 99% of the
		//      time it isn't a problem b/c we're not creating our own
		//      structure - we're using whatever's in the caps (for example).
		//      Any object that's taking ownership must call takeOwnership()
		//      in order to prevent this from being freed and causing a crash.
		synchronized(this) {
			if (!ownedByParent)
				gst_structure_free(ptr);
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="Getters">
	public boolean isOwnedByParent() {
		return ownedByParent;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="ToString">
	@Override
	public String toString() {
		return gst_structure_to_string(ptr);
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="Package Methods">
	void takeOwnership() {
		synchronized(this) {
			//See disposeObject() documentation
			ownedByParent = true;
		}
	}

	void releaseOwnership() {
		synchronized(this) {
			//See disposeObject() documentation
			ownedByParent = false;
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="Public Methods">
	public Structure copy() {
		return from(gst_structure_copy(ptr));
	}

	public String name() {
		return gst_structure_get_name(ptr);
	}

	public void saveName(String name) {
		gst_structure_set_name(ptr, name);
	}

	public boolean nameEquals(String name) {
		return gst_structure_has_name(ptr, name);
	}

	public boolean fieldExists(String fieldName) {
		return gst_structure_has_field(ptr, fieldName);
	}

	public boolean removeField(String fieldName) {
		gst_structure_remove_field(ptr, fieldName);
		return true;
	}

	public boolean removeFields(String...fieldNames) {
		if (fieldNames == null || fieldNames.length <= 0)
			return true;
		for(String fieldName: fieldNames)
			removeField(fieldName);
		return true;
	}

	public void saveFieldAsInt(String fieldName, int value) {
		GValue propValue = new GValue();
		Pointer pPropValue = propValue.getPointer();
		g_value_init(pPropValue, GType.Int.asNativeLong());
		try {
			g_value_set_int(pPropValue, value);
			gst_structure_set_value(ptr, fieldName, pPropValue);
		} finally {
			g_value_unset(pPropValue);
		}
	}

	public void saveFieldAsDouble(String fieldName, double value) {
		GValue propValue = new GValue();
		Pointer pPropValue = propValue.getPointer();
		g_value_init(pPropValue, GType.Double.asNativeLong());
		try {
			g_value_set_double(pPropValue, value);
			gst_structure_set_value(ptr, fieldName, pPropValue);
		} finally {
			g_value_unset(pPropValue);
		}
	}

	public void saveFieldAsIntRange(String fieldName, IntRange range) {
		saveFieldAsIntRange(fieldName, range.getMinimum(), range.getMaximum());
	}

	public void saveFieldAsIntRange(String fieldName, int min, int max) {
		GValue propValue = new GValue();
		Pointer pPropValue = propValue.getPointer();
		g_value_init(pPropValue, gst_int_range_get_type());
		try {
			gst_value_set_int_range(pPropValue, min, max);
			gst_structure_set_value(ptr, fieldName, pPropValue);
		} finally {
			g_value_unset(pPropValue);
		}
	}

	public void saveFieldAsDoubleRange(String fieldName, DoubleRange range) {
		saveFieldAsDoubleRange(fieldName, range.getMinimum(), range.getMaximum());
	}

	public void saveFieldAsDoubleRange(String fieldName, double min, double max) {
		GValue propValue = new GValue();
		Pointer pPropValue = propValue.getPointer();
		g_value_init(pPropValue, gst_double_range_get_type());
		try {
			gst_value_set_double_range(pPropValue, min, max);
			gst_structure_set_value(ptr, fieldName, pPropValue);
		} finally {
			g_value_unset(pPropValue);
		}
	}

	public void saveFieldAsFractionRange(String fieldName, FractionRange range) {
		saveFieldAsFractionRange(fieldName, range.getMinimum(), range.getMaximum());
	}

	public void saveFieldAsFractionRange(String fieldName, Fraction min, Fraction max) {
		saveFieldAsFractionRange(fieldName, min.getNumerator(), min.getDenominator(), max.getNumerator(), max.getDenominator());
	}

	public void saveFieldAsFractionRange(String fieldName, int minNumerator, int minDenominator, int maxNumerator, int maxDenominator) {
		GValue propValue = new GValue();
		Pointer pPropValue = propValue.getPointer();
		g_value_init(pPropValue, gst_fraction_range_get_type());
		try {
			gst_value_set_fraction_range_full(pPropValue, minNumerator, minDenominator, maxNumerator, maxDenominator);
			gst_structure_set_value(ptr, fieldName, pPropValue);
		} finally {
			g_value_unset(pPropValue);
		}
	}

	public void saveFieldAsFraction(String fieldName, Fraction fraction) {
		saveFieldAsFraction(fieldName, fraction.getNumerator(), fraction.getDenominator());
	}

	public void saveFieldAsFraction(String fieldName, int numerator, int denominator) {
		GValue propValue = new GValue();
		Pointer pPropValue = propValue.getPointer();
		g_value_init(pPropValue, gst_fraction_get_type());
		try {
			gst_value_set_fraction(pPropValue, numerator, denominator);
			gst_structure_set_value(ptr, fieldName, pPropValue);
		} finally {
			g_value_unset(pPropValue);
		}
	}

	public void saveFieldAsFourCCInt(String fieldName, int fourcc) {
		GValue propValue = new GValue();
		Pointer pPropValue = propValue.getPointer();
		g_value_init(pPropValue, gst_fourcc_get_type());
		try {
			gst_value_set_fourcc(pPropValue, fourcc);
			gst_structure_set_value(ptr, fieldName, pPropValue);
		} finally {
			g_value_unset(pPropValue);
		}
	}

	public void saveFieldAsFourCCString(String fieldName, String fourcc) {
		if (StringUtil.isNullOrEmpty(fourcc) || fourcc.length() != 4)
			throw new IllegalArgumentException("fourcc cannot be empty and must be exactly 4 characters long");

		byte[] bytes;
		try {
			bytes = fourcc.toUpperCase().getBytes("US-ASCII");
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		if (bytes == null || bytes.length != 4)
			throw new IllegalArgumentException("Unable to convert fourcc string into byte array");
		
		saveFieldAsFourCCInt(
			fieldName,
			(int)(
				  ((bytes[0] & 0xff) << 0)
				| ((bytes[1] & 0xff) << 8)
				| ((bytes[2] & 0xff) << 16)
				| ((bytes[3] & 0xff) << 24)
			)
		);
	}

	public int fieldAsInt(String fieldName) {
		IntByReference ref = new IntByReference();
		if (!gst_structure_get_int(ptr, fieldName, ref))
			throw new InvalidFieldException("Unknown field: " + fieldName);
		return ref.getValue();
	}

	public double fieldAsDouble(String fieldName) {
		DoubleByReference ref = new DoubleByReference();
		if (!gst_structure_get_double(ptr, fieldName, ref))
			throw new InvalidFieldException("Unknown field: " + fieldName);
		return ref.getValue();
	}

	public String fieldAsString(String fieldName) {
		String val;
		if ((val = gst_structure_get_string(ptr, fieldName)) == null)
			throw new InvalidFieldException("Unknown field: " + fieldName);
		return val;
	}

	public boolean fieldAsBoolean(String fieldName) {
		IntByReference ref = new IntByReference();
		if (!gst_structure_get_boolean(ptr, fieldName, ref))
			throw new InvalidFieldException("Unknown field: " + fieldName);
		return (ref.getValue() != 0);
	}

	public Fraction fieldAsFraction(String fieldName) {
		IntByReference numerator = new IntByReference();
		IntByReference denominator = new IntByReference();
		if (!gst_structure_get_fraction(ptr, fieldName, numerator, denominator))
			throw new InvalidFieldException("Unknown field: " + fieldName);
		return new Fraction(numerator.getValue(), denominator.getValue());
	}

	public int fieldAsFourCCInt(String fieldName) {
		IntByReference ref = new IntByReference();
		if (!gst_structure_get_fourcc(ptr, fieldName, ref))
			throw new InvalidFieldException("Unknown field: " + fieldName);
		return ref.getValue();
	}

	public String fieldAsFourCCString(String fieldName) {
		int f = fieldAsFourCCInt(fieldName);
		return new String(new byte[] {
			  (byte)((f >>  0) & 0xff)
			, (byte)((f >>  8) & 0xff)
			, (byte)((f >> 16) & 0xff)
			, (byte)((f >> 24) & 0xff)
		}).toUpperCase();
	}

	public IntRange fieldAsIntRange(String fieldName) {
		Pointer pGValue = gst_structure_get_value(ptr, fieldName);
		if (pGValue == null || pGValue == Pointer.NULL)
			throw new InvalidFieldException("Unknown or invalid type field: " + fieldName);
		return new IntRange(pGValue);
	}

	public DoubleRange fieldAsDoubleRange(String fieldName) {
		Pointer pGValue = gst_structure_get_value(ptr, fieldName);
		if (pGValue == null || pGValue == Pointer.NULL)
			throw new InvalidFieldException("Unknown or invalid type field: " + fieldName);
		return new DoubleRange(pGValue);
	}

	public FractionRange fieldAsFractionRange(String fieldName) {
		Pointer pGValue = gst_structure_get_value(ptr, fieldName);
		if (pGValue == null || pGValue == Pointer.NULL)
			throw new InvalidFieldException("Unknown or invalid type field: " + fieldName);
		return new FractionRange(pGValue);
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="Public Static Methods">
	public static Structure newEmpty(String name) {
		return new Structure(name);
	}

	public static Structure from(String name) {
		return new Structure(name);
	}

	public static Structure from(Pointer ptr) {
		return new Structure(ptr);
	}

	public static Structure fromString(String structure) {
		Structure s = new Structure(gst_structure_from_string(structure, null));
		//This is a managed object that needs to be tracked and freed if necessary.
		s.managed = true;
		return s;
	}
	//</editor-fold>
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

