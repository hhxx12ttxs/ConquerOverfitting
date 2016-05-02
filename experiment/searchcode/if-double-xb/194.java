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
package icc.tags;

import java.io.IOException;
import java.util.Vector;
import java.io.RandomAccessFile;
import icc .ICCProfile;
import icc .types.XYZNumber;

/**
 * A tag containing a triplet.
 * 
 * @see		jj2000.j2k.icc.tags.ICCXYZTypeReverse 
 * @see	    jj2000.j2k.icc.types.XYZNumber
 * @version	1.0
 * @author	Bruce A. Kern
 */
public class ICCXYZType extends ICCTag {

    /** x component */ public final long x;
    /** y component */ public final long y;
    /** z component */ public final long z;

    /** Normalization utility */
    public static long DoubleToXYZ ( double x ) {
        return (long) Math.floor(x * 65536.0 + 0.5); }

    /** Normalization utility */
    public static double XYZToDouble (long x) { 
        return x / 65536.0; }

    /**
     * Construct this tag from its constituant parts
     *   @param signature tag id
     *   @param data array of bytes
     *   @param offset to data in the data array
     *   @param length of data in the data array
     */
    protected ICCXYZType (int signature, byte [] data, int offset, int length) {
        super (signature, data, offset, length);
        x=ICCProfile.getInt (data, offset+2*ICCProfile.int_size);
        y=ICCProfile.getInt (data, offset+3*ICCProfile.int_size);
        z=ICCProfile.getInt (data, offset+4*ICCProfile.int_size); }


    /** Return the string rep of this tag. */
    public String toString () {
        return "[" + super.toString() + "(" + x + ", " + y + ", " + z + ")]"; }

    
    /** Write to a file. */
    public void write (RandomAccessFile raf) throws IOException {
        byte [] xb = ICCProfile.setLong (x);
        byte [] yb = ICCProfile.setLong (y);
        byte [] zb = ICCProfile.setLong (z);

        raf.write (xb, ICCProfile.int_size, 0);
        raf.write (yb, ICCProfile.int_size, 0);
        raf.write (zb, ICCProfile.int_size, 0); }


    /* end class ICCXYZType */ }












