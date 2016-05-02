//-----------------------------------------------------------------------------//
//                                                                             //
//  Deterministic Arts - Utilities                                             //
//  Copyright (c) 2009 Dirk EĂer                                               //
//                                                                             //
//  Licensed under the Apache License, Version 2.0 (the "License");            //
//  you may not use this file except in compliance with the License.           //
//  You may obtain a copy of the License at                                    //
//                                                                             //
//    http://www.apache.org/licenses/LICENSE-2.0                               //
//                                                                             //
//  Unless required by applicable law or agreed to in writing, software        //
//  distributed under the License is distributed on an "AS IS" BASIS,          //
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   //
//  See the License for the specific language governing permissions and        //
//  limitations under the License.                                             //
//                                                                             //
//-----------------------------------------------------------------------------//

package de.deterministicarts.lib.util;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * Sequence of bytes.
 * 
 * <p>A byte string is an immutable sequence of arbitrary bytes. Byte strings
 * support serialization, comparison (for equality as well as for order), and
 * hashing. The text representation of a byte string is the hexadecimal
 * representation of its constituent bytes.</p>
 * 
 * <p>Byte strings offer most of the methods standard Java {@linkplain String strings}
 * do.</p>
 * 
 * <p><strong>Thread-Safety</strong> Instances of this class are immutable,
 * and thus safe for use from multiple concurrently running threads.</p>
 * 
 * @author Dirk EĂer
 */

public final class ByteString 
implements Serializable, Comparable<ByteString> {

    private static final long serialVersionUID = 5218512872016551081L;

    private final byte[] data;
    private int hash;

    private ByteString(byte[] dt, int h) {
        data = dt;
        hash = h;
    }

    public ByteString(byte[] b) {
        if( b == null )
            throw new NullPointerException("missing byte array");
        else {
            data = copy(b);
            hash = 0;
        }
    }

    public boolean equals(Object r) {
        if( r == this )
            return true;
        else {
            if( r instanceof ByteString ) {
                final ByteString s = (ByteString) r;
                final byte[] bs = s.data;
                final int length = bs.length;
                if( length == data.length ) {
                    for( int k = 0; k < length; ++k )
                        if( data[k] != bs[k] ) return false;
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Obtain the length of this string.
     * 
     * <p>
     * This method answers the number of bytes in this string.
     * </p>
     * 
     * @return the number of bytes
     */

    public int length() {
        return data.length;
    }

    /**
     * Obtain the byte at a specified index.
     * 
     * <p>
     * This method returns the value of the byte at index {@code k} in this byte
     * string. If {@code k} is not a valid index for this byte string, then an
     * exception is raised.
     * </p>
     * 
     * @param k
     *            the index to inspect
     * @return the value of the byte at index {@code k}
     * @throws IndexOutOfBoundsException
     *             if {@code k} is invalid
     */

    public byte byteAt(int k) {
        return data[k];
    }

    /**
     * Obtain a copy of this byte string as raw array of bytes.
     * 
     * @return a new array of bytes, initialized from the contents of this byte
     *         string
     */

    public byte[] toByteArray() {
        return copy(data);
    }

    public int hashCode() {
    	int h = hash;
    	if (h == 0)  {
            for( byte b : data ) h = h * 31 + b;
            h = (h == 0)? 1 : h;
            hash = h;
    	}
        return h;
    }

    public ByteString substring(int start) {
        return substring(start, data.length);
    }

    public ByteString substring(int start, int end) {
        if( start < 0 || start >= data.length )
            throw new IndexOutOfBoundsException();
        if( end < start || end > data.length )
            throw new IndexOutOfBoundsException();
        if( start == 0 && end == data.length )
            return this;
        else {
            final int length = end - start;
            final byte[] buffer = new byte[length];
            System.arraycopy(data, start, buffer, 0, length);
            return new ByteString(buffer, 0);
        }
    }

    public int lastIndexOf(byte b) {
        return lastIndexOf(b, data.length - 1);
    }

    public int lastIndexOf(byte b, int start) {
        if( start < 0 ) return -1;
        for( int k = Math.min(data.length - 1, start); k >= 0; --k )
            if( data[k] == b ) return k;
        return -1;
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified byte value. If a byte with value <code>b</code> occurs in the
     * byte sequence represented by this <code>ByteString</code> object, then
     * the index of the first such occurrence is returned. This is the smallest
     * value <i>k</i> such that: <blockquote>
     * 
     * <pre>
     * this.byteAt(&lt;i&gt;k&lt;/i&gt;) == b
     * </pre>
     * 
     * </blockquote> is true. If no such byte occurs in this string, then
     * <code>-1</code> is returned.
     * 
     * @param b
     *            a byte value
     * @return the index of the first occurrence of the value in the byte
     *         sequence represented by this object, or <code>-1</code> if the
     *         byte does not occur.
     */

    public int indexOf(byte b) {
        return indexOf(b, 0);
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified byte value, starting the search at index {@code start}. If a
     * byte with value <code>b</code> occurs in the byte sequence represented by
     * this <code>ByteString</code> object, then the index of the first such
     * occurrence is returned. This is the smallest value <i>k</i> such that:
     * <blockquote>
     * <pre>
     * this.byteAt(<i>k</i>) == b &amp;&amp; <i>k</i> >= start</pre>
     * </blockquote> is true. If no such byte occurs in this string, then
     * <code>-1</code> is returned.
     * 
     * @param b		a byte value
     * @param start	index of the first byte to look at
     * 
     * @return the index of the first occurrence of the value in the byte
     *         sequence represented by this object, or <code>-1</code> if the
     *         byte does not occur.
     */

    public int indexOf(byte b, int start) {
        if( start >= data.length ) return -1;
        final int length = data.length;
        for( int k = Math.max(0, start); k < length; ++k )
            if( data[k] == b ) return k;
        return -1;
    }

    private static byte[] copy(byte[] b) {
        if( b == null )
            throw new NullPointerException("missing byte array");
        else {
            final byte[] data = new byte[b.length];
            System.arraycopy(b, 0, data, 0, b.length);
            return data;
        }
    }

    private static final class Proxy implements Serializable {

        private static final long serialVersionUID = -1604254503272489941L;
        private final byte[] data;

        private Proxy(ByteString s) {
            data = s.data;
        }

        public Object readResolve() {
            return new ByteString(copy(data), 0);
        }
    }

    private Object writeReplace() {
        return new Proxy(this);
    }

    private void readObject(ObjectInputStream stream) throws IOException {
        throw new InvalidObjectException("deserialization requires proxy: "
                        + getClass().getName());
    }

    /**
     * Compare for order.
     * 
     * <p>
     * Compares this byte string and {@code o} for order, returning an integer
     * value, which is <code>&lt; 0</code>, <code>= 0</code> or <code>>
     * 0</code>, depending on whether this string should by regarded as less
     * than, equal to or greater than {@code o}.
     * </p>
     * 
     * <p>Note, that the comparison is done with the unsigned byte value, that
     * is, you get <code>new ByteString(new byte[] { (byte)-1 }).compareTo(new ByteString(new byte[] { 1 })) > 0</code>!
     * This is a little bit counter-intertuitive, I have to admit...</p>
     * 
     * <p>
     * The ordering defined by this method is consistent with
     * {@link #equals(Object)}.
     * </p>
     * 
     * @return see description
     */

    public int compareTo(ByteString o) {

        final byte[] rdata = o.data;
        final int rlength = rdata.length;
        final int count = Math.min(data.length, rlength);

        for( int k = 0; k < count; ++k ) {
            final int diff = (0xff & data[k]) - (0xff & rdata[k]);
            if( diff != 0 ) return diff;
        }

        if( data.length < rlength ) return -1;
        if( data.length > rlength ) return 1;
        return 0;
    }

    private static final char[] hexDigits = { '0', '1', '2', '3', '4', '5',
                    '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public String toString() {
        final char[] buffer = new char[2 * data.length];
        final int length = data.length;
        int wp = 0, rp = 0;

        while( rp < length ) {
            final int b = 0xff & data[rp++];
            buffer[wp++] = hexDigits[b >>> 4];
            buffer[wp++] = hexDigits[b & 0xf];
        }

        return new String(buffer);
    }

    /**
     * Create a byte string from its hexadecimal representation.
     * 
     * <p>
     * This method answers a byte string, whose contents is computed from
     * {@code data}, which is assumed to be a string of hexadecimal digits.
     * </p>
     * 
     * @param data
     *            text to decode
     * @return a byte string or <code>null</code>, if {@code data} is
     *         <code>null</code>
     * 
     * @throws ByteStringDataException
     * 
     *             if {@code data} is not a valid byte string representation
     */

    public static ByteString fromString(String data) throws DataException {

        if( data == null )
            return null;
        else {

            final int length = data.length();

            if( (length & 1) != 0 )
                throw new DataException("invalid length of byte data string");
            else {

                final int size = length >>> 1;
                final byte[] buffer = new byte[size];
                int rp = 0, wp = 0;
                int h = 0;

                while( rp < length ) {

                    final char ch1 = data.charAt(rp++);
                    final int value1 = Character.digit(ch1, 16);

                    if( value1 < 0 )
                        throw new DataException(
                                        "invalid hex character at offset "
                                                        + (rp - 1));
                    else {

                        final char ch2 = data.charAt(rp++);
                        final int value2 = Character.digit(ch2, 16);

                        if( value2 < 0 )
                            throw new DataException(
                                            "invalid hex character at offset "
                                                            + (rp - 1));
                        else {
                            final byte b = buffer[wp++] = (byte) ((value1 << 4) | value2);
                            h = h * 31 + b;
                        }
                    }
                }

                return new ByteString(buffer, h == 0 ? 1 : h);
            }
        }
    }

    /**
     * Create a byte string from text.
     * 
     * <p>
     * This method answers with a byte string, whose contents is the result of
     * encoding the given string {@code b} using the encoding scheme named in
     * {@code encoding}. Raises an exception, if {@code encoding} is not
     * supported by the Java VM.
     * </p>
     * 
     * @param b
     *            text to encode
     * @param encoding
     *            name of the desired character encoding
     * @return a new byte string or <code>null</code>, if {@code b} is
     *         <code>null</code>
     * @throws UnsupportedEncodingException
     *             if {@code encoding} is not supported by the VM
     */

    public static ByteString fromText(String b, String encoding)
        throws UnsupportedEncodingException {
        if( encoding == null )
            throw new NullPointerException("missing encoding name");
        else {
            if( b == null ) return null;
            return new ByteString(b.getBytes(encoding), 0);
        }
    }

    /**
     * Create a byte string from text.
     * 
     * <p>
     * This method answers with a byte string, whose contents is the result of
     * encoding the given string {@code b} using the Java VM's standard
     * character encoding.
     * </p>
     * 
     * @param b
     *            text to encode
     * @return a new byte string or <code>null</code>, if {@code b} is
     *         <code>null</code>
     */

    public static ByteString fromText(String b) {
        if( b == null ) return null;
        return new ByteString(b.getBytes(), 0);
    }

    /**
     * Obtain a text representation using a particular encoding.
     * 
     * <p>
     * This method answers a representation of the contents of this byte string
     * as text. The representation is obtained by interpreting the bytes in this
     * buffer as being a representation for some text, encoded using the {@code
     * encoding} specified.
     * </p>
     * 
     * <p>
     * If {@code encoding} is not the name of a character encoding supported by
     * the Java VM, then this method raises a
     * {@link UnsupportedEncodingException}.
     * </p>
     * 
     * @param encoding
     *            encoding name
     * @return a text representation of this byte string
     * @throws UnsupportedEncodingException
     *             if the given {@code encoding} is not supported by the Java VM
     */

    public String toText(String encoding) throws UnsupportedEncodingException {
        if( encoding == null )
            throw new NullPointerException("missing encoding");
        return new String(data, encoding);
    }

    /**
     * Obtain a text representation.
     * 
     * <p>
     * This method answers a representation of the contents of this byte string
     * as text. The representation is obtained by interpreting the bytes as
     * characters in the Java VM's default encoding. If the byte string contains
     * byte sequences, which cannot be interpreted in the VM's default encoding,
     * the result of this method is undefined.
     * </p>
     * 
     * @return a text representation of this byte string
     */

    public String toText() {
        return new String(data);
    }

    public static class DataException extends Exception {

        private static final long serialVersionUID = 1L;

        public DataException() {
            super();
        }

        public DataException(String message, Throwable cause) {
            super(message, cause);
        }

        public DataException(String message) {
            super(message);
        }

        public DataException(Throwable cause) {
            super(cause);
        }
    }
}

