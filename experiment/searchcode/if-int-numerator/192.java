<<<<<<< HEAD
/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package org.jikesrvm;

import org.jikesrvm.mm.mminterface.Barriers;
import org.jikesrvm.runtime.Entrypoints;
import org.jikesrvm.runtime.Magic;
import org.jikesrvm.scheduler.Synchronization;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Interruptible;
import org.vmmagic.pragma.NoInline;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptibleNoWarn;
import org.vmmagic.unboxed.Offset;

/**
 *  Various service utilities.  This is a common place for some shared utility routines
 */
@Uninterruptible
public class Services implements SizeConstants {
  /**
   * Biggest buffer you would possibly need for {@link org.jikesrvm.scheduler.RVMThread#dump(char[], int)}
   * Modify this if you modify that method.
   */
  public static final int MAX_DUMP_LEN =
    10 /* for thread ID  */ + 7 + 5 + 5 + 11 + 5 + 10 + 13 + 17 + 10;

  /** Pre-allocate the dump buffer, since dump() might get called inside GC. */
  private static final char[] dumpBuffer = new char[MAX_DUMP_LEN];

  @SuppressWarnings({"unused", "CanBeFinal", "UnusedDeclaration"})// accessed via EntryPoints
  private static int dumpBufferLock = 0;

  /** Reset at boot time. */
  private static Offset dumpBufferLockOffset = Offset.max();

  /**
   * A map of hexadecimal digit values to their character representations.
   * <P>
   * XXX We currently only use '0' through '9'.  The rest are here pending
   * possibly merging this code with the similar code in Log.java, or breaking
   * this code out into a separate utility class.
   */
  private static final char [] hexDigitCharacter =
  { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
    'f' };

  /**
   * How many characters we need to have in a buffer for building string
   * representations of <code>long</code>s, such as {@link #intBuffer}. A
   * <code>long</code> is a signed 64-bit integer in the range -2^63 to
   * 2^63+1. The number of digits in the decimal representation of 2^63 is
   * ceiling(log10(2^63)) == ceiling(63 * log10(2)) == 19. An extra character
   * may be required for a minus sign (-). So the maximum number of characters
   * is 20.
   */
  private static final int INT_BUFFER_SIZE = 20;

  /** A buffer for building string representations of <code>long</code>s */
  private static final char [] intBuffer = new char[INT_BUFFER_SIZE];

  /** A lock for {@link #intBuffer} */
  @SuppressWarnings({"unused", "CanBeFinal", "UnusedDeclaration"})// accessed via EntryPoints
  private static int intBufferLock = 0;

  /** The offset of {@link #intBufferLock} in this class's TIB.
   *  This is set properly at boot time, even though it's a
   *  <code>private</code> variable. . */
  private static Offset intBufferLockOffset = Offset.max();

  /**
   * Called during the boot sequence, any time before we go multi-threaded. We
   * do this so that we can leave the lockOffsets set to -1 until the VM
   * actually needs the locking (and is running multi-threaded).
   */
  public static void boot() {
    dumpBufferLockOffset = Entrypoints.dumpBufferLockField.getOffset();
    intBufferLockOffset = Entrypoints.intBufferLockField.getOffset();
  }

  public static char[] grabDumpBuffer() {
    if (!dumpBufferLockOffset.isMax()) {
      while (!Synchronization.testAndSet(Magic.getJTOC(), dumpBufferLockOffset, 1)) {
        ;
      }
    }
    return dumpBuffer;
  }

  public static void releaseDumpBuffer() {
    if (!dumpBufferLockOffset.isMax()) {
      Synchronization.fetchAndStore(Magic.getJTOC(), dumpBufferLockOffset, 0);
    }
  }


  /** Copy a String into a character array.
   *
   *  This function may be called during GC and may be used in conjunction
   *  with the MMTk {@link org.mmtk.utility.Log} class.   It avoids write barriers and allocation.
   *  <p>
   *  XXX This function should probably be moved to a sensible location where
   *   we can use it as a utility.   Suggestions welcome.
   *  <P>
   *
   * @param dest char array to copy into.
   * @param destOffset Offset into <code>dest</code> where we start copying
   *
   * @return 1 plus the index of the last character written.  If we were to
   *         write zero characters (which we won't) then we would return
   *         <code>offset</code>.  This is intended to represent the first
   *         unused position in the array <code>dest</code>.  However, it also
   *         serves as a pseudo-overflow check:  It may have the value
   *         <code>dest.length</code>, if the array <code>dest</code> was
   *         completely filled by the call, or it may have a value greater
   *         than <code>dest.length</code>, if the info needs more than
   *         <code>dest.length - offset</code> characters of space.
   *
   * @return  -1 if <code>offset</code> is negative.
   *
   * the MMTk {@link org.mmtk.utility.Log} class).
   */
  public static int sprintf(char[] dest, int destOffset, String s) {
    final char[] sArray = java.lang.JikesRVMSupport.getBackingCharArray(s);
    return sprintf(dest, destOffset, sArray);
  }

  public static int sprintf(char[] dest, int destOffset, char[] src) {
    return sprintf(dest, destOffset, src, 0, src.length);
  }

  /** Copies characters from <code>src</code> into the destination character
   * array <code>dest</code>.
   *
   *  The first character to be copied is at index <code>srcBegin</code>; the
   *  last character to be copied is at index <code>srcEnd-1</code>.  (This is
   *  the same convention as followed by java.lang.String#getChars).
   *
   * @param dest char array to copy into.
   * @param destOffset Offset into <code>dest</code> where we start copying
   * @param src Char array to copy from
   * @param srcStart index of the first character of <code>src</code> to copy.
   * @param srcEnd index after the last character of <code>src</code> to copy.
   */
  public static int sprintf(char[] dest, int destOffset, char[] src, int srcStart, int srcEnd) {
    for (int i = srcStart; i < srcEnd; ++i) {
      char nextChar = getArrayNoBarrier(src, i);
      destOffset = sprintf(dest, destOffset, nextChar);
    }
    return destOffset;
  }

  public static int sprintf(char[] dest, int destOffset, char c) {
    if (destOffset < 0) {
      // bounds check
      return -1;
    }

    if (destOffset < dest.length) {
      setArrayNoBarrier(dest, destOffset, c);
    }
    return destOffset + 1;
  }

  /** Copy the printed decimal representation of a long into
   * a character array.  The value is not padded and no
   * thousands seperator is copied.  If the value is negative a
   * leading minus sign (-) is copied.
   *
   *  This function may be called during GC and may be used in conjunction
   *  with the Log class.   It avoids write barriers and allocation.
   *  <p>
   *  XXX This function should probably be moved to a sensible location where
   *   we can use it as a utility.   Suggestions welcome.
   * <p>
   *  XXX This method's implementation is stolen from the {@link org.mmtk.utility.Log} class.
   *
   * @param dest char array to copy into.
   * @param offset Offset into <code>dest</code> where we start copying
   *
   * @return 1 plus the index of the last character written.  If we were to
   *         write zero characters (which we won't) then we would return
   *         <code>offset</code>.  This is intended to represent the first
   *         unused position in the array <code>dest</code>.  However, it also
   *         serves as a pseudo-overflow check:  It may have the value
   *         <code>dest.length</code>, if the array <code>dest</code> was
   *         completely filled by the call, or it may have a value greater
   *         than <code>dest.length</code>, if the info needs more than
   *         <code>dest.length - offset</code> characters of space.
   *
   * @return  -1 if <code>offset</code> is negative.
   */
  public static int sprintf(char[] dest, int offset, long l) {
    boolean negative = l < 0;
    int nextDigit;
    char nextChar;
    int index = INT_BUFFER_SIZE - 1;
    char[] intBuffer = grabIntBuffer();

    nextDigit = (int) (l % 10);
    nextChar = getArrayNoBarrier(hexDigitCharacter, negative ? -nextDigit : nextDigit);
    setArrayNoBarrier(intBuffer, index--, nextChar);
    l = l / 10;

    while (l != 0) {
      nextDigit = (int) (l % 10);
      nextChar = getArrayNoBarrier(hexDigitCharacter, negative ? -nextDigit : nextDigit);
      setArrayNoBarrier(intBuffer, index--, nextChar);
      l = l / 10;
    }

    if (negative) {
     setArrayNoBarrier(intBuffer, index--, '-');
    }

    int newOffset = sprintf(dest, offset, intBuffer, index + 1, INT_BUFFER_SIZE);
    releaseIntBuffer();
    return newOffset;
  }

  /**
   * Get exclusive access to {@link #intBuffer}, the buffer for building
   * string representations of integers.
   */
  private static char[] grabIntBuffer() {
    if (!intBufferLockOffset.isMax()) {
      while (!Synchronization.testAndSet(Magic.getJTOC(), intBufferLockOffset, 1)) {
        ;
      }
    }
    return intBuffer;
  }

  /**
   * Release {@link #intBuffer}, the buffer for building string
   * representations of integers.
   */
  private static void releaseIntBuffer() {
    if (!intBufferLockOffset.isMax()) {
      Synchronization.fetchAndStore(Magic.getJTOC(), intBufferLockOffset, 0);
    }
  }

  /**
   * Utility printing function.
   * @param i
   * @param blank
   */
  @Interruptible
  public static String getHexString(int i, boolean blank) {
    StringBuilder buf = new StringBuilder(8);
    for (int j = 0; j < 8; j++, i <<= 4) {
      int n = i >>> 28;
      if (blank && (n == 0) && (j != 7)) {
        buf.append(' ');
      } else {
        buf.append(Character.forDigit(n, 16));
        blank = false;
      }
    }
    return buf.toString();
  }

  @NoInline
  public static void breakStub() {
  }

  static void println() { VM.sysWrite("\n"); }

  static void print(String s) { VM.sysWrite(s); }

  static void println(String s) {
    print(s);
    println();
  }

  static void print(int i) { VM.sysWrite(i); }

  static void println(int i) {
    print(i);
    println();
  }

  static void print(String s, int i) {
    print(s);
    print(i);
  }

  static void println(String s, int i) {
    print(s, i);
    println();
  }

  public static void percentage(int numerator, int denominator, String quantity) {
    print("\t");
    if (denominator > 0) {
      print((int) ((((double) numerator) * 100.0) / ((double) denominator)));
    } else {
      print("0");
    }
    print("% of ");
    println(quantity);
  }

  static void percentage(long numerator, long denominator, String quantity) {
    print("\t");
    if (denominator > 0L) {
      print((int) ((((double) numerator) * 100.0) / ((double) denominator)));
    } else {
      print("0");
    }
    print("% of ");
    println(quantity);
  }

  /**
   * Sets an element of a object array without possibly losing control.
   * NB doesn't perform checkstore or array index checking.
   *
   * @param dst the destination array
   * @param index the index of the element to set
   * @param value the new value for the element
   */
  @UninterruptibleNoWarn("Interruptible code not reachable at runtime")
  @Inline
  public static void setArrayUninterruptible(Object[] dst, int index, Object value) {
    if (VM.runningVM) {
      if (Barriers.NEEDS_OBJECT_ASTORE_BARRIER) {
        Barriers.objectArrayWrite(dst, index, value);
      } else {
        Magic.setObjectAtOffset(dst, Offset.fromIntZeroExtend(index << LOG_BYTES_IN_ADDRESS), value);
      }
    } else {
      dst[index] = value;
    }
  }

  /**
   * Sets an element of a char array without invoking any write
   * barrier.  This method is called by the Log method, as it will be
   * used during garbage collection and needs to manipulate character
   * arrays without causing a write barrier operation.
   *
   * @param dst the destination array
   * @param index the index of the element to set
   * @param value the new value for the element
   */
  public static void setArrayNoBarrier(char[] dst, int index, char value) {
    if (VM.runningVM)
      Magic.setCharAtOffset(dst, Offset.fromIntZeroExtend(index << LOG_BYTES_IN_CHAR), value);
    else
      dst[index] = value;
  }

  /**
   * Gets an element of an Object array without invoking any read
   * barrier or performing bounds checks.
   *
   * @param src the source array
   * @param index the natural array index of the element to get
   * @return the new value of element
   */
  public static Object getArrayNoBarrier(Object[] src, int index) {
    if (VM.runningVM)
      return Magic.getObjectAtOffset(src, Offset.fromIntZeroExtend(index << LOG_BYTES_IN_ADDRESS));
    else
      return src[index];
  }

  /**
   * Gets an element of an int array without invoking any read barrier
   * or performing bounds checks.
   *
   * @param src the source array
   * @param index the natural array index of the element to get
   * @return the new value of element
   */
  public static int getArrayNoBarrier(int[] src, int index) {
    if (VM.runningVM)
      return Magic.getIntAtOffset(src, Offset.fromIntZeroExtend(index << LOG_BYTES_IN_INT));
    else
      return src[index];
  }

  /**
   * Gets an element of a char array without invoking any read barrier
   * or performing bounds check.
   *
   * @param src the source array
   * @param index the natural array index of the element to get
   * @return the new value of element
   */
  public static char getArrayNoBarrier(char[] src, int index) {
    if (VM.runningVM)
      return Magic.getCharAtOffset(src, Offset.fromIntZeroExtend(index << LOG_BYTES_IN_CHAR));
    else
      return src[index];
  }

  /**
   * Gets an element of a byte array without invoking any read barrier
   * or bounds check.
   *
   * @param src the source array
   * @param index the natural array index of the element to get
   * @return the new value of element
   */
  public static byte getArrayNoBarrier(byte[] src, int index) {
    if (VM.runningVM)
      return Magic.getByteAtOffset(src, Offset.fromIntZeroExtend(index));
    else
      return src[index];
  }

  /**
   * Gets an element of an array of byte arrays without causing the potential
   * thread switch point that array accesses normally cause.
   *
   * @param src the source array
   * @param index the index of the element to get
   * @return the new value of element
   */
  public static byte[] getArrayNoBarrier(byte[][] src, int index) {
    if (VM.runningVM)
      return Magic.addressAsByteArray(Magic.objectAsAddress(Magic.getObjectAtOffset(src, Offset.fromIntZeroExtend(index << LOG_BYTES_IN_ADDRESS))));
    else
      return src[index];
  }
}
=======
package vjo.java.sun.text.normalizer;

/*
 * @(#)UCharacterPropertyReader.java	1.3 05/11/17
 *
 * Portions Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 *******************************************************************************
 * (C) Copyright IBM Corp. 1996-2005 - All Rights Reserved                     *
 *                                                                             *
 * The original version of this source code and documentation is copyrighted   *
 * and owned by IBM, These materials are provided under terms of a License     *
 * Agreement between IBM and Sun. This technology is protected by multiple     *
 * US and International patents. This notice and attribution to IBM may not    *
 * to removed.                                                                 *
 *******************************************************************************
 */

import java.io.IOException;

import vjo.java.lang.* ;

import vjo.java.io.InputStream;
import vjo.java.io.DataInputStream;

/**
* <p>Internal reader class for ICU data file uprops.icu containing 
* Unicode codepoint data.</p> 
* <p>This class simply reads uprops.icu, authenticates that it is a valid
* ICU data file and split its contents up into blocks of data for use in
* <a href=UCharacterProperty.html>com.ibm.icu.impl.UCharacterProperty</a>.
* </p> 
* <p>uprops.icu which is in big-endian format is jared together with this 
* package.</p>
* @author Syn Wee Quek
* @since release 2.1, February 1st 2002
* @draft 2.1
*/
/* Unicode character properties file format ------------------------------------

The file format prepared and written here contains several data
structures that store indexes or data.



The following is a description of format version 3 .

Data contents:

The contents is a parsed, binary form of several Unicode character
database files, most prominently UnicodeData.txt.

Any Unicode code point from 0 to 0x10ffff can be looked up to get
the properties, if any, for that code point. This means that the input
to the lookup are 21-bit unsigned integers, with not all of the
21-bit range used.

It is assumed that client code keeps a uint32_t pointer
to the beginning of the data:

    const uint32_t *p32;

Formally, the file contains the following structures:

    const int32_t indexes[16] with values i0..i15:

    i0 propsIndex; -- 32-bit unit index to the table of 32-bit properties words
    i1 exceptionsIndex;  -- 32-bit unit index to the table of 32-bit exception words
    i2 exceptionsTopIndex; -- 32-bit unit index to the array of UChars for special mappings

    i3 additionalTrieIndex; -- 32-bit unit index to the additional trie for more properties
    i4 additionalVectorsIndex; -- 32-bit unit index to the table of properties vectors
    i5 additionalVectorsColumns; -- number of 32-bit words per properties vector

    i6 reservedItemIndex; -- 32-bit unit index to the top of the properties vectors table
    i7..i9 reservedIndexes; -- reserved values; 0 for now

    i10 maxValues; -- maximum code values for vector word 0, see uprops.h (format version 3.1+)
    i11 maxValues2; -- maximum code values for vector word 2, see uprops.h (format version 3.2)
    i12..i15 reservedIndexes; -- reserved values; 0 for now

    PT serialized properties trie, see utrie.h (byte size: 4*(i0-16))

    P  const uint32_t props32[i1-i0];
    E  const uint32_t exceptions[i2-i1];
    U  const UChar uchars[2*(i3-i2)];

    AT serialized trie for additional properties (byte size: 4*(i4-i3))
    PV const uint32_t propsVectors[(i6-i4)/i5][i5]==uint32_t propsVectors[i6-i4];

Trie lookup and properties:

In order to condense the data for the 21-bit code space, several properties of
the Unicode code assignment are exploited:
- The code space is sparse.
- There are several 10k of consecutive codes with the same properties.
- Characters and scripts are allocated in groups of 16 code points.
- Inside blocks for scripts the properties are often repetitive.
- The 21-bit space is not fully used for Unicode.

The lookup of properties for a given code point is done with a trie lookup,
using the UTrie implementation.
The trie lookup result is a 16-bit index in the props32[] table where the
actual 32-bit properties word is stored. This is done to save space.

(There are thousands of 16-bit entries in the trie data table, but
only a few hundred unique 32-bit properties words.
If the trie data table contained 32-bit words directly, then that would be
larger because the length of the table would be the same as now but the
width would be 32 bits instead of 16. This saves more than 10kB.)

With a given Unicode code point

    UChar32 c;

and 0<=c<0x110000, the lookup is done like this:

    uint16_t i;
    UTRIE_GET16(c, i);
    uint32_t props=p32[i];

For some characters, not all of the properties can be efficiently encoded
using 32 bits. For them, the 32-bit word contains an index into the exceptions[]
array:

    if(props&EXCEPTION_BIT)) {
        uint16_t e=(uint16_t)(props>>VALUE_SHIFT);
        ...
    }

The exception values are a variable number of uint32_t starting at

    const uint32_t *pe=p32+exceptionsIndex+e;

The first uint32_t there contains flags about what values actually follow it.
Some of the exception values are UChar32 code points for the case mappings,
others are numeric values etc.

32-bit properties sets:

Each 32-bit properties word contains:

 0.. 4  general category
 5      has exception values
 6..10  BiDi category
11      is mirrored
12..14  numericType:
            0 no numeric value
            1 decimal digit value
            2 digit value
            3 numeric value
            ### TODO: type 4 for Han digits & numbers?!
15..19  reserved
20..31  value according to bits 0..5:
        if(has exception) {
            exception index;
        } else switch(general category) {
        case Ll: delta to uppercase; -- same as titlecase
        case Lu: -delta to lowercase; -- titlecase is same as c
        case Lt: -delta to lowercase; -- uppercase is same as c
        default:
            if(is mirrored) {
                delta to mirror;
            } else if(numericType!=0) {
                numericValue;
            } else {
                0;
            };
        }

Exception values:

In the first uint32_t exception word for a code point,
bits
31..16  reserved
15..0   flags that indicate which values follow:

bit
 0      has uppercase mapping
 1      has lowercase mapping
 2      has titlecase mapping
 3      unused
 4      has numeric value (numerator)
            if numericValue=0x7fffff00+x then numericValue=10^x
 5      has denominator value
 6      has a mirror-image Unicode code point
 7      has SpecialCasing.txt entries
 8      has CaseFolding.txt entries

According to the flags in this word, one or more uint32_t words follow it
in the sequence of the bit flags in the flags word; if a flag is not set,
then the value is missing or 0:

For the case mappings and the mirror-image Unicode code point,
one uint32_t or UChar32 each is the code point.
If the titlecase mapping is missing, then it is the same as the uppercase mapping.

For the digit values, bits 31..16 contain the decimal digit value, and
bits 15..0 contain the digit value. A value of -1 indicates that
this value is missing.

For the numeric/numerator value, an int32_t word contains the value directly,
except for when there is no numerator but a denominator, then the numerator
is implicitly 1. This means:
    numerator denominator result
    none      none        none
    x         none        x
    none      y           1/y
    x         y           x/y

If the numerator value is 0x7fffff00+x then it is replaced with 10^x.

For the denominator value, a uint32_t word contains the value directly.

For special casing mappings, the 32-bit exception word contains:
31      if set, this character has complex, conditional mappings
        that are not stored;
        otherwise, the mappings are stored according to the following bits
30..24  number of UChars used for mappings
23..16  reserved
15.. 0  UChar offset from the beginning of the UChars array where the
        UChars for the special case mappings are stored in the following format:

Format of special casing UChars:
One UChar value with lengths as follows:
14..10  number of UChars for titlecase mapping
 9.. 5  number of UChars for uppercase mapping
 4.. 0  number of UChars for lowercase mapping

Followed by the UChars for lowercase, uppercase, titlecase mappings in this order.

For case folding mappings, the 32-bit exception word contains:
31..24  number of UChars used for the full mapping
23..16  reserved
15.. 0  UChar offset from the beginning of the UChars array where the
        UChars for the special case mappings are stored in the following format:

Format of case folding UChars:
Two UChars contain the simple mapping as follows:
    0,  0   no simple mapping
    BMP,0   a simple mapping to a BMP code point
    s1, s2  a simple mapping to a supplementary code point stored as two surrogates
This is followed by the UChars for the full case folding mappings.

Example:
U+2160, ROMAN NUMERAL ONE, needs an exception because it has a lowercase
mapping and a numeric value.
Its exception values would be stored as 3 uint32_t words:

- flags=0x0a (see above) with combining class 0
- lowercase mapping 0x2170
- numeric value=1

--- Additional properties (new in format version 2.1) ---

The second trie for additional properties (AT) is also a UTrie with 16-bit data.
The data words consist of 32-bit unit indexes (not row indexes!) into the
table of unique properties vectors (PV).
Each vector contains a set of properties.
The width of a vector (number of uint32_t per row) may change
with the formatVersion, it is stored in i5.

Current properties: see icu/source/common/uprops.h

--- Changes in format version 3.1 ---

See i10 maxValues above, contains only UBLOCK_COUNT and USCRIPT_CODE_LIMIT.

--- Changes in format version 3.2 ---

- The tries use linear Latin-1 ranges.
- The additional properties bits store full properties XYZ instead
  of partial Other_XYZ, so that changes in the derivation formulas
  need not be tracked in runtime library code.
- Joining Type and Line Break are also stored completely, so that uprops.c
  needs no runtime formulas for enumerated properties either.
- Store the case-sensitive flag in the main properties word.
- i10 also contains U_LB_COUNT and U_EA_COUNT.
- i11 contains maxValues2 for vector word 2.

----------------------------------------------------------------------------- */

final class UCharacterPropertyReader implements ICUBinary.Authenticate
{
    // public methods ----------------------------------------------------
    
    public boolean isDataVersionAcceptable(byte version[])
    {
        return version[0] == DATA_FORMAT_VERSION_[0] 
               && version[2] == DATA_FORMAT_VERSION_[2] 
               && version[3] == DATA_FORMAT_VERSION_[3];
    }
    
    // protected constructor ---------------------------------------------
    
    /**
    * <p>Protected constructor.</p>
    * @param inputStream ICU uprop.dat file input stream
    * @exception IOException throw if data file fails authentication 
    * @draft 2.1
    */
    protected UCharacterPropertyReader(InputStream inputStream) 
                                                        throws IOException
    {
        m_unicodeVersion_ = ICUBinary.readHeader(inputStream, DATA_FORMAT_ID_, 
                                                 this);
        m_dataInputStream_ = new DataInputStream(inputStream);
    }
    
    // protected methods -------------------------------------------------
      
    /**
    * <p>Reads uprops.icu, parse it into blocks of data to be stored in
    * UCharacterProperty.</P
    * @param ucharppty UCharacterProperty instance
    * @exception thrown when data reading fails
    * @draft 2.1
    */
    protected void read(UCharacterProperty ucharppty) throws IOException
    {
        // read the indexes
        int count = INDEX_SIZE_;
        m_propertyOffset_          = m_dataInputStream_.readInt();
        count --;
        m_exceptionOffset_         = m_dataInputStream_.readInt();
        count --;
        m_caseOffset_              = m_dataInputStream_.readInt();
        count --;
        m_additionalOffset_        = m_dataInputStream_.readInt();
        count --;
        m_additionalVectorsOffset_ = m_dataInputStream_.readInt();
        count --;
        m_additionalColumnsCount_  = m_dataInputStream_.readInt();
        count --;
        m_reservedOffset_          = m_dataInputStream_.readInt();
        count --;
        m_dataInputStream_.skipBytes(3 << 2);
        count -= 3;
        ucharppty.m_maxBlockScriptValue_ = m_dataInputStream_.readInt();
        count --; // 10
        ucharppty.m_maxJTGValue_ = m_dataInputStream_.readInt();
        count --; // 11
        m_dataInputStream_.skipBytes(count << 2);
        
        // read the trie index block
        // m_props_index_ in terms of ints
        ucharppty.m_trie_ = new CharTrie(m_dataInputStream_, ucharppty);
        
        // reads the 32 bit properties block
        int size = m_exceptionOffset_ - m_propertyOffset_;
        ucharppty.m_property_ = new int[size];
        for (int i = 0; i < size; i ++) {
            ucharppty.m_property_[i] = m_dataInputStream_.readInt();
        }
        
        // reads the 32 bit exceptions block
        size = m_caseOffset_ - m_exceptionOffset_;
        ucharppty.m_exception_ = new int[size];
        for (int i = 0; i < size; i ++) {
            ucharppty.m_exception_[i] = m_dataInputStream_.readInt();
        }
        
        // reads the 32 bit case block
        size = (m_additionalOffset_ - m_caseOffset_) << 1;
        ucharppty.m_case_ = new char[size];
        for (int i = 0; i < size; i ++) {
            ucharppty.m_case_[i] = m_dataInputStream_.readChar();
        }
        
        // reads the additional property block
        ucharppty.m_additionalTrie_ = new CharTrie(m_dataInputStream_, 
                                                   ucharppty);
                                                           
        // additional properties
        size = m_reservedOffset_ - m_additionalVectorsOffset_;
        ucharppty.m_additionalVectors_ = new int[size];
        for (int i = 0; i < size; i ++) {
            ucharppty.m_additionalVectors_[i] = m_dataInputStream_.readInt();
        }
        
        m_dataInputStream_.close();
        ucharppty.m_additionalColumnsCount_ = m_additionalColumnsCount_;
        ucharppty.m_unicodeVersion_ = VersionInfo.getInstance(
                         (int)m_unicodeVersion_[0], (int)m_unicodeVersion_[1],
                         (int)m_unicodeVersion_[2], (int)m_unicodeVersion_[3]);
    }
    
    // private variables -------------------------------------------------
      
    /**
    * Index size
    */
    private static final int INDEX_SIZE_ = 16;
    
    /**
    * ICU data file input stream
    */
    private DataInputStream m_dataInputStream_;
      
    /**
    * Offset information in the indexes.
    */
    private int m_propertyOffset_;
    private int m_exceptionOffset_;
    private int m_caseOffset_;
    private int m_additionalOffset_;
    private int m_additionalVectorsOffset_;
    private int m_additionalColumnsCount_;
    private int m_reservedOffset_;
    private byte m_unicodeVersion_[];  
                                      
    /**
    * File format version that this class understands.
    * No guarantees are made if a older version is used
    */
    private static final byte DATA_FORMAT_ID_[] = {(byte)0x55, (byte)0x50, 
                                                    (byte)0x72, (byte)0x6F};
    private static final byte DATA_FORMAT_VERSION_[] = {(byte)0x3, (byte)0x1, 
                                             (byte)Trie.INDEX_STAGE_1_SHIFT_, 
                                             (byte)Trie.INDEX_STAGE_2_SHIFT_};
}

>>>>>>> 76aa07461566a5976980e6696204781271955163

