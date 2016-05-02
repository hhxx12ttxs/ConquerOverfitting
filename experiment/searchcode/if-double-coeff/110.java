/*
 * This file is part of seadams Utils.
 *
 * Copyright (c) 2008-2011 Sam Adams <seadams@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.seadams.util;

public class BitStringFingerprint {

    protected int length;
    protected boolean[] bits;

    /**
     * Creates new bit string.
     * @param length
     */
    public BitStringFingerprint(int length) {
        this.length = length;
        bits = new boolean[length];
    }

    /**
     * Sets specified bit.
     * @param n     Index of bit to set.
     * @throws IndexOutOfBoundsException if n is invalid.
     */
    public void set(int n) {
        if (n < 0 || n >= length) {
            throw new IndexOutOfBoundsException();
        }
        bits[n] = true;
    }

    /**
     * Unsets specified bit.
     * @param n     Index of bit to set.
     * @throws IndexOutOfBoundsException if n is invalid.
     */
    public void unset(int n) {
        if (n < 0 || n >= length) {
            throw new IndexOutOfBoundsException();
        }
        bits[n] = false;
    }

    /**
     * Flips specified bit.
     * @param n     Index of bit to set.
     * @throws IndexOutOfBoundsException if n is invalid.
     */
    public void flip(int n) {
        if (n < 0 || n >= length) {
            throw new IndexOutOfBoundsException();
        }
        bits[n] = !bits[n];
    }

    /**
     * Returns length of bit string.
     * @return
     */
    public int getLength() {
        return(length);
    }

    /**
     * Counts number of bits that are set.
     * @return
     */
    public int getNumberOfBitsSet() {
        int n = 0;
        for (boolean b : bits) {
            if (b) {
                n ++;
            }
        }

        return(n);
    }

    /**
     * Counts number of bits that are unset.
     * @return
     */
    public int getNumberOfBitsUnset() {
        int n = 0;
        for (boolean b : bits) {
            if (!b) {
                n ++;
            }
        }

        return(n);
    }

    public int getHashedPosition(String s) {
        int n = (new java.util.Random(s.hashCode())).nextInt(bits.length);
        return(n);
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (boolean b : bits) {
            sb.append(b ? "1" : "0");
        }

        return(sb.toString());
    }


    /**
     * Calculates Tanimoto coefficient between this and another
     * fingerprint.
     *
     * @param fp
     * @return
     */
    public double getTanimotoCoefficient(BitStringFingerprint fp) {
        if (fp.getLength() != bits.length) {
            // TODO: error
        }

        int na = 0;
        int nb = 0;
        int nab = 0;
        for (int i = 0; i < bits.length; i ++) {
            if (this.bits[i]) {
                na ++;
            }
            if (fp.bits[i]) {
                nb ++;
            }
            if (this.bits[i] && fp.bits[i]) {
                nab ++;
            }
        }

        double coeff = (nab) / ((double) na + nb - nab);
        return(coeff);
    }
}

