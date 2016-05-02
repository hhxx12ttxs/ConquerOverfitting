/*
<<<<<<< HEAD
 * <p> <b>jFin, open source derivatives trade processing</b> </p>
 *
 * <p> Copyright (C) 2005, 2006, 2007 Morgan Brown Consultancy Ltd. </p>
 *
 * <p> This file is part of jFin. </p>
 *
 * <p> jFin is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. </p>
 *
 * <p> jFin is distributed in the hope that it will be useful, but <b>WITHOUT
 * ANY WARRANTY</b>; without even the implied warranty of <b>MERCHANTABILITY</b>
 * or <b>FITNESS FOR A PARTICULAR PURPOSE</b>. See the GNU General Public
 * License for more details. </p>
 *
 * <p> You should have received a copy of the GNU General Public License along
 * with jFin; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA. </p>
 */

package org.jfin.date.daycount.defaultimpl;

import java.util.Calendar;
import java.util.logging.Logger;

import org.jfin.date.daycount.DaycountCalculator;
import org.jfin.date.daycount.DaycountException;

/**
 * If startCalendar and endCalendar are equal dates, returns zero.
 */
public class EU30360 extends DaycountCalculator {

    private static Logger logger = Logger
            .getLogger("org.jfin.date.daycount.defaultimpl.EU30360");

    public double calculateDaycountFraction(Calendar startCalendar,
                                            Calendar endCalendar) throws DaycountException {

        if (startCalendar.after(endCalendar)) {
            logger.fine("Dates are wrong way round so swap over");
            Calendar holdCalendar = startCalendar;
            startCalendar = endCalendar;
            endCalendar = holdCalendar;
        }

        if (startCalendar.equals(endCalendar)) {
            logger.fine("Dates are the same so return zero");
            return 0.0d;
        }
        int dayOfMonth1 = startCalendar.get(Calendar.DAY_OF_MONTH);
        int dayOfMonth2 = endCalendar.get(Calendar.DAY_OF_MONTH);
        int month1 = startCalendar.get(Calendar.MONTH);
        int month2 = endCalendar.get(Calendar.MONTH);
        int year1 = startCalendar.get(Calendar.YEAR);
        int year2 = endCalendar.get(Calendar.YEAR);

        if (dayOfMonth1 == 31) {
            dayOfMonth1 = 30;
        }
        if (dayOfMonth2 == 31) {
            dayOfMonth2 = 30;
        }

        int numerator = 360 * (year2 - year1);
        numerator += 30 * (month2 - month1);
        numerator += dayOfMonth2 - dayOfMonth1;

        return numerator / 360d;
    }

    @Override
    public double calculateDaycountFraction(Calendar startCalendar,
                                            Calendar endCalendar, Calendar periodStartCalendar,
                                            Calendar periodEndCalendar) throws DaycountException {
        return calculateDaycountFraction(startCalendar, endCalendar);
    }
=======
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
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

