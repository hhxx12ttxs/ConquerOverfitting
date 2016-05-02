<<<<<<< HEAD
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

=======
/*
 *  Copyright 2007 - 2009 Martin Roth (mhroth@gmail.com)
 *                        Matthew Yee-King
 * 
 *  This file is part of JVstHost.
 *
 *  JVstHost is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JVstHost is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JVstHost.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.synthbot.audioplugin.vst.vst2;

import com.synthbot.audioplugin.vst.VstVersion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

public class JVstHost20 extends JVstHost2 {
  
  protected int numInputs; // not final because can change (ioChange)
  protected int numOutputs; // locally cached for error checking
  protected int numParameters;
  protected final int numPrograms;
  protected float sampleRate; // the last sampleRate to which the plugin was set
  protected int blockSize; // the last maximum blockSize to which the plugin was set
  protected final boolean canProcessReplacing;
  protected final boolean hasNativeEditor;
  protected volatile Thread editorThread; // volatile because the variable can be get/set by either the vst thread or the editor thread
  protected boolean isTurnedOff;
  
  protected final List<MidiMessage> queuedMidiMessages;
  
  protected final List<JVstHostListener> hostListeners;
  
  protected JVstHost20(File pluginFile, long pluginPtr) {
    super(pluginFile, pluginPtr);
    setThis(vstPluginPtr);
    
    numInputs = numInputs();
    numOutputs = numOutputs();
    numParameters = numParameters();
    numPrograms = numPrograms(); // this value seems never to be changed
    canProcessReplacing = (canReplacing(pluginPtr) != 0);
    hasNativeEditor = (hasEditor(vstPluginPtr) != 0);
    isTurnedOff = true;
    
    queuedMidiMessages = new ArrayList<MidiMessage>();
    
    hostListeners = new ArrayList<JVstHostListener>();
  }
  
  /**
   * Creates a weak global reference to the corresponding java object for the native plugin.
   * This reference allows callbacks methods to by easily called to the correct object.
   */
  private native void setThis(long pluginPtr);

  protected void assertIsTurnedOff() {
    if (!isTurnedOff) {
      throw new IllegalStateException("The plugin must be turned off in order to perform this operation.");
    }
  }
  
  protected void assertIsTurnedOn() {
    if (isTurnedOff) {
      throw new IllegalStateException("The plugin must be turned on in order to perform this operation.");
    }
  }
  
  @Override
  public VstVersion getVstVersion() {
    return VstVersion.VST20;
  }
  
  @Override
  public synchronized void turnOffAndUnloadPlugin() {
    if (isEditorOpen()) {
      closeEditor();
    }
    turnOff();
    unloadPlugin(vstPluginPtr);
    isNativeComponentLoaded = false;
  }
  
  @Override
  public synchronized void queueMidiMessage(SysexMessage message) {
    if (message == null) {
      throw new NullPointerException("Queued midi message may not be null.");
    }
    queuedMidiMessages.add(message);
  }
  
  @Override
  public synchronized void queueMidiMessage(ShortMessage message) {
    if (message == null) {
      throw new NullPointerException("Queued midi message may not be null.");
    }
    queuedMidiMessages.add(message);
  }
  
  @Override
  public synchronized void processReplacing(float[][] inputs, float[][] outputs, int blockSize) {
    assertNativeComponentIsLoaded();
    assertIsTurnedOn();
    if (!canProcessReplacing) {
      throw new IllegalStateException("This plugin does not implement processReplacing().");
    }
    if (inputs == null) {
      throw new NullPointerException("The inputs array is null.");
    } else if (inputs.length < numInputs) {
      throw new IllegalArgumentException("Input array length must equal the number of inputs: " + inputs.length + " < " + numInputs);
    } else {
      for (float[] input : inputs) {
        if (input.length < blockSize) {
          throw new IllegalArgumentException("Input array length must be at least as large as the blockSize: " + input.length + " < " + blockSize);
        }
      }
    }
    if (outputs == null) {
      throw new NullPointerException("The outputs array is null.");
    } else if (outputs.length < numOutputs) {
      throw new IllegalArgumentException("Output array length must equal the number of outputs: " + outputs.length + " < " + numOutputs);
    } else {
      for (float[] output : outputs) {
        if (output.length < blockSize) {
          throw new IllegalArgumentException("Output array length must be at least as large as the blockSize: " + output.length + " < " + blockSize);
        }
      }
    }
    if (blockSize < 0) {
      throw new IllegalArgumentException("Block size must be non-negative: " + blockSize + " < 0");
    }

    MidiMessage[] messages = queuedMidiMessages.toArray(new MidiMessage[0]);
    queuedMidiMessages.clear();
    
    processReplacing(messages, inputs, outputs, blockSize, vstPluginPtr);
  }
  protected static native void processReplacing(MidiMessage[] messages, float[][] inputs, float[][] outputs, int blockSize, long pluginPtr);
  
  @Override
  public synchronized boolean canReplacing() {
    return canProcessReplacing;
  }
  protected static native int canReplacing(long pluginPtr);
  
  @Override
  public synchronized void process(float[][] inputs, float[][] outputs, int blockSize) {
    assertNativeComponentIsLoaded();
    assertIsTurnedOn();
    if (inputs == null) {
      throw new NullPointerException("The inputs array is null.");
    } else if (inputs.length < numInputs) {
      throw new IllegalArgumentException("Input array length must equal the number of inputs: " + inputs.length + " < " + numInputs);
    } else {
      for (float[] input : inputs) {
        if (input.length < blockSize) {
          throw new IllegalArgumentException("Input array length must be at least as large as the blockSize: " + input.length + " < " + blockSize);
        }
      }
    }
    if (outputs == null) {
      throw new NullPointerException("The outputs array is null.");
    } else if (outputs.length < numOutputs) {
      throw new IllegalArgumentException("Output array length must equal the number of outputs: " + outputs.length + " < " + numOutputs);
    } else {
      for (float[] output : outputs) {
        if (output.length < blockSize) {
          throw new IllegalArgumentException("Output array length must be at least as large as the blockSize: " + output.length + " < " + blockSize);
        }
      }
    }
    if (blockSize < 0) {
      throw new IllegalArgumentException("Block size must be non-negative: " + blockSize + " < 0");
    }
    
    MidiMessage[] messages = queuedMidiMessages.toArray(new MidiMessage[0]);
    queuedMidiMessages.clear();
    
    process(messages, inputs, outputs, blockSize, vstPluginPtr);
  }
  protected static native void process(MidiMessage[] messages, float[][] inputs, float[][] outputs, int blockSize, long pluginPtr);
  
  @Override
  public synchronized boolean canDo(VstPluginCanDo canDo) {
    assertNativeComponentIsLoaded();
    return (canDo(canDo.canDoString(), vstPluginPtr) != 0);
  }
  protected static native int canDo(String canDo, long pluginPtr);
  
  @Override
  public synchronized void setParameter(int index, float value) {
    assertNativeComponentIsLoaded();
    if (index < 0 || index >= numParameters) {
      throw new IndexOutOfBoundsException("Parameter index, " + index + ", must be between 0 and " + numParameters);
    }
    if (value < 0f || value > 1f) {
      System.err.println("Parameter values should be constrained to within [0,1]: " + Float.toString(value));
    }
    setParameter(index, value, vstPluginPtr);
  }
  protected static native void setParameter(int index, float value, long pluginPtr);

  @Override
  public synchronized float getParameter(int index) {
    assertNativeComponentIsLoaded();
    if (index < 0 || index >= numParameters) {
      throw new IndexOutOfBoundsException("Parameter index, " + index + ", must be between 0 and " + numParameters);
    }
    return getParameter(index, vstPluginPtr);
  }
  protected static native float getParameter(int index, long pluginPtr);
  
  @Override
  public synchronized boolean isParameterAutomatable(int index) {
    assertNativeComponentIsLoaded();
    if (index < 0 || index >= numParameters) {
      throw new IndexOutOfBoundsException("Parameter index, " + index + ", must be between 0 and " + numParameters);
    }
    return (isParameterAutomatable(index, vstPluginPtr) != 0);
  }
  protected static native int isParameterAutomatable(int index, long vstPluginPtr);
  
  @Override
  public synchronized String getParameterName(int index) {
    assertNativeComponentIsLoaded();
    if (index < 0 || index >= numParameters) {
      throw new IndexOutOfBoundsException("Parameter index, " + index + ", must be between 0 and " + numParameters);
    }
    return getParameterName(index, vstPluginPtr);
  }
  protected static native String getParameterName(int index, long pluginPtr);
  
  @Override
  public synchronized String getParameterDisplay(int index) {
    assertNativeComponentIsLoaded();
    if (index < 0 || index >= numParameters) {
      throw new IndexOutOfBoundsException("Parameter index, " + index + ", must be between 0 and " + numParameters);
    }
    return getParameterDisplay(index, vstPluginPtr);
  }
  protected static native String getParameterDisplay(int index, long pluginPtr);
  
  @Override
  public synchronized String getParameterLabel(int index) throws IndexOutOfBoundsException {
    assertNativeComponentIsLoaded();
    if (index < 0 || index >= numParameters) {
      throw new IndexOutOfBoundsException("Parameter index, " + index + ", must be between 0 and " + numParameters);
    }
    return getParameterLabel(index, vstPluginPtr);
  }
  protected static native String getParameterLabel(int index, long pluginPtr);
  
  @Override
  public synchronized String getEffectName() {
    assertNativeComponentIsLoaded();
    return getEffectName(vstPluginPtr);
  }
  protected static native String getEffectName(long pluginPtr);
  
  @Override
  public synchronized String getVendorName() {
    assertNativeComponentIsLoaded();
    return getVendorName(vstPluginPtr);
  }
  protected static native String getVendorName(long pluginPtr);
  
  @Override
  public synchronized String getProductString() {
    assertNativeComponentIsLoaded();
    return getProductString(vstPluginPtr);
  }
  protected static native String getProductString(long pluginPtr);
  
  @Override
  public synchronized int numParameters() {
    assertNativeComponentIsLoaded();
    return numParameters(vstPluginPtr);
  }
  protected static native int numParameters(long pluginPtr);
  
  @Override
  public synchronized int numInputs() {
    assertNativeComponentIsLoaded();
    return numInputs(vstPluginPtr);
  }
  protected static native int numInputs(long pluginPtr);
  
  @Override
  public synchronized int numOutputs() {
    assertNativeComponentIsLoaded();
    return numOutputs(vstPluginPtr);
  }
  protected static native int numOutputs(long pluginPtr);
  
  @Override
  public synchronized int numPrograms() {
    assertNativeComponentIsLoaded();
    return numPrograms(vstPluginPtr);
  }
  protected static native int numPrograms(long pluginPtr);
  
  @Override
  public synchronized void setSampleRate(float sampleRate) {
    assertIsTurnedOff();
    assertNativeComponentIsLoaded();
    if (sampleRate <= 0f) {
      throw new IllegalArgumentException("Sample rate must be positive: " + sampleRate);
    }
    this.sampleRate = sampleRate;
    setSampleRate(sampleRate, vstPluginPtr);
  }
  protected static native void setSampleRate(float sampleRate, long pluginPtr);
  
  @Override
  public synchronized float getSampleRate() {
    return sampleRate;
  }
  
  @Override
  public synchronized void setTempo(double tempo) {
    setTempo(tempo, vstPluginPtr);
  }
  protected static native void setTempo(double tempo, long pluginPtr);
  
  
  @Override
  public synchronized void setBlockSize(int blockSize) throws IllegalArgumentException {
    assertIsTurnedOff();
    assertNativeComponentIsLoaded();
    if (blockSize <= 0) {
      throw new IllegalArgumentException("Blocks size must be positive: " + blockSize);
    }
    this.blockSize = blockSize;
    setBlockSize(blockSize, vstPluginPtr);
  }
  protected static native void setBlockSize(int blockSize, long pluginPtr);
  
  @Override
  public synchronized int getBlockSize() {
    return blockSize;
  }
  
  @Override
  public synchronized String getUniqueId() {
    int uniqueId = getUniqueIdAsInt();
    byte[] uidArray = {
        (byte) (0x000000FF & (uniqueId >> 24)), 
        (byte) (0x000000FF & (uniqueId >> 16)), 
        (byte) (0x000000FF & (uniqueId >> 8)), 
        (byte) uniqueId};
    return new String(uidArray);
  }
  
  @Override
  public synchronized int getUniqueIdAsInt() {
    assertNativeComponentIsLoaded();
    return getUniqueId(vstPluginPtr);
  }
  protected static native int getUniqueId(long pluginPtr);
  
  @Override
  public synchronized boolean isSynth() {
    assertNativeComponentIsLoaded();
    return (isSynth(vstPluginPtr) != 0);
  }
  protected static native int isSynth(long pluginPtr);
  
  @Override
  public synchronized boolean acceptsProgramsAsChunks() {
    assertNativeComponentIsLoaded();
    return (acceptsProgramsAsChunks(vstPluginPtr) != 0);
  }
  protected static native int acceptsProgramsAsChunks(long pluginPtr);
  
  @Override
  public synchronized void openEditor(final String frameTitle) {
    if (frameTitle == null) {
      throw new NullPointerException("frameTitle may not be null.");
    }
    assertNativeComponentIsLoaded();
    assertHasNativeEditor();
        
    if (!isEditorOpen()) {
      final JVstHost2 thisJVstHost = this;
      editorThread = new Thread(new Runnable() {
        public void run() {
          openEditor(frameTitle, vstPluginPtr); // this method blocks while the native window is open
          editorThread = null;
          synchronized(thisJVstHost) {
            thisJVstHost.notify(); // notify all waiting threads (such as one waiting in closeEditor), that the native editor is now closed
          }
        }
      });
      editorThread.setPriority(Thread.MIN_PRIORITY);
      editorThread.setName(toString() + " native editor thread");
      editorThread.start();      
    }
  }
  protected static native void openEditor(String frameTitle, long pluginPtr);
  
  @Override
  public synchronized boolean isEditorOpen() {
    return (editorThread != null);
  }
  
  @Override
  public synchronized void topEditor() {
    if (isEditorOpen()) {
      topEditor(vstPluginPtr);
      System.out.println("done topping editor");
    }
  }
  protected static native void topEditor(long pluginPtr);
  
  
  @Override
  public synchronized void closeEditor() {
    assertNativeComponentIsLoaded();
    if (isEditorOpen()) {
      closeEditor(vstPluginPtr);
      while (isEditorOpen()) {
        try {
          wait(); // wait for the editorThread to notify this JVstHost2 object that it has completed
        } catch (InterruptedException ie) {
          // do nothing, just wait again
        }
      }
    }
  }
  protected static native void closeEditor(long pluginPtr);
  
  @Override
  public synchronized boolean hasEditor() {
    return hasNativeEditor;
  }
  protected static native int hasEditor(long pluginPtr);
  
  protected void assertHasNativeEditor() {
    if (!hasNativeEditor) {
      throw new IllegalStateException("This plugin has no native editor. Do not try to manipulate it.");
    }
  }
  
  @Override
  public synchronized int getProgram() {
    assertNativeComponentIsLoaded();
    return getProgram(vstPluginPtr);
  }
  protected static native int getProgram(long pluginPtr);
  
  @Override
  public synchronized void setProgram(int index) {
    assertNativeComponentIsLoaded();
    if (index < 0 || index >= numPrograms) {
      throw new IndexOutOfBoundsException("The program index must be in [0, " + numPrograms + "): " + Integer.toString(index));
    }
    setProgram(index, vstPluginPtr);
  }
  protected static native void setProgram(int index, long pluginPtr);
  
  @Override
  public synchronized String getProgramName() {
    assertNativeComponentIsLoaded();
    return getProgramName(vstPluginPtr);
  }
  protected static native String getProgramName(long pluginPtr);
  
  @Override
  public synchronized String getProgramName(int index) {
    assertNativeComponentIsLoaded();
    if (index < 0 || index >= numPrograms) {
      throw new IndexOutOfBoundsException("The program index must be in [0, " + numPrograms + "): " + Integer.toString(index)); 
    }
    return getProgramName(index, vstPluginPtr);
  }
  protected static native String getProgramName(int index, long pluginPtr);
  

  @Override
  public synchronized void setProgramName(String name) {
    assertNativeComponentIsLoaded();
    setProgramName(name, vstPluginPtr);
  }
  protected static native void setProgramName(String name, long pluginPtr);
  
  @Override
  public synchronized int getPluginVersion() {
    assertNativeComponentIsLoaded();
    return getPluginVersion(vstPluginPtr);
  }
  protected static native int getPluginVersion(long pluginPtr);
  
  @Override
  public synchronized int getInitialDelay() {
    assertNativeComponentIsLoaded();
    return getInitialDelay(vstPluginPtr);
  }
  protected static native int getInitialDelay(long pluginPtr);
  
  @Override
  public synchronized void turnOn() {
    if (isTurnedOff) {
      resume(vstPluginPtr);
      isTurnedOff = false;
    }
  }
  protected static native void resume(long pluginPtr);
  
  @Override
  public synchronized void turnOff() {
    if (!isTurnedOff) {
      suspend(vstPluginPtr);
      isTurnedOff = true;
    }
  }
  protected static native void suspend(long pluginPtr);
  
  @Override
  public synchronized void setBankChunk(byte[] chunkData) {
    assertNativeComponentIsLoaded();
    if (chunkData == null) {
      throw new NullPointerException("Chunk data cannot be null.");
    }
    setChunk(0, chunkData, vstPluginPtr);
  }
  
  @Override
  public synchronized void setProgramChunk(byte[] chunkData) {
    assertNativeComponentIsLoaded();
    if (chunkData == null) {
      throw new NullPointerException("Chunk data cannot be null.");
    }
    setChunk(1, chunkData, vstPluginPtr);
  }
  protected static native void setChunk(int bankOrProgram, byte[] chunkData, long pluginPtr);
  
  @Override
  public synchronized byte[] getBankChunk() {
    assertNativeComponentIsLoaded();
    return getChunk(0, vstPluginPtr);
  }
  
  @Override
  public synchronized byte[] getProgramChunk() {
    assertNativeComponentIsLoaded();
    return getChunk(1, vstPluginPtr);
  }
  protected static native byte[] getChunk(int bankOrProgram, long pluginPtr);
  
  @Override
  public synchronized void setBypass(boolean bypass) {
    assertNativeComponentIsLoaded();
    setBypass(bypass, vstPluginPtr);
  }
  protected static native void setBypass(boolean bypass, long pluginPtr);
  
  @Override
  public synchronized VstPinProperties getInputProperties(int index) {
    if (index < 0 || index >= numInputs) {
      throw new IndexOutOfBoundsException("The input index must be in [0, " + numInputs + "): " + Integer.toString(index));
    }
    return getPinProperties(index, true, vstPluginPtr);
  }
  
  @Override
  public synchronized VstPinProperties getOutputProperties(int index) {
    if (index < 0 || index >= numOutputs) {
      throw new IndexOutOfBoundsException("The output index must be in [0, " + numOutputs + "): " + Integer.toString(index));
    }
    return getPinProperties(index, false, vstPluginPtr);
  }
  
  /**
   * @param isInput  <code>true</code> if the index refers to an input. <code>false</code> if it 
   * refers to an output.
   */
  protected static native VstPinProperties getPinProperties(int index, boolean isInput, long pluginPtr);
  
  public void setTimeSignature(int numerator, int denominator) {
    if (numerator <= 0) {
      throw new IllegalArgumentException("The number of note values per measure must be positive: " + Integer.toString(numerator));
    }
    if (denominator <= 0) {
      throw new IllegalArgumentException("The note value per beat must be positive: " + Integer.toString(denominator));
    }
    setTimeSignature(numerator, denominator, vstPluginPtr);
  }
  protected static native void setTimeSignature(int numerator, int denominator, long pluginPtr);
  
  /*
   * Native plugin callbacks.
   */
  protected synchronized void audioMasterProcessMidiEvents(int command, int channel, int data1, int data2) {
    try {
      ShortMessage message = new ShortMessage();
      message.setMessage(command, channel, data1, data2);
      for (JVstHostListener listener : hostListeners) {
        listener.onAudioMasterProcessMidiEvents(this, message);
      }
    } catch (InvalidMidiDataException imde) {
      /*
       * If there is a problem in constructing the ShortMessage, just print out an error message
       * and allow the program to continue. It isn't the end of the world.
       */
      imde.printStackTrace(System.err);
    }
  }
  
  protected synchronized void audioMasterAutomate(int index, float value) {
    for (JVstHostListener listener : hostListeners) {
      listener.onAudioMasterAutomate(this, index, value);
    }
  }
  
  protected synchronized void audioMasterIoChanged(int numInputs, int numOutputs, int initialDelay, int numParameters) {
    this.numInputs = numInputs; // update cached vars
    this.numOutputs = numOutputs;
    this.numParameters = numParameters;
    for (JVstHostListener listener : hostListeners) {
      listener.onAudioMasterIoChanged(this, numInputs, numOutputs, initialDelay, numParameters);
    }
  }
  
  protected synchronized void audioMasterBeginEdit(int index) {
    for (JVstHostListener listener : hostListeners) {
      listener.onAudioMasterBeginEdit(this, index);
    }
  }
  
  protected synchronized void audioMasterEndEdit(int index) {
    for (JVstHostListener listener : hostListeners) {
      listener.onAudioMasterEndEdit(this, index);
    }
  }
  
  /*
   * Listener manager methods.
   */
  @Override
  public synchronized void addJVstHostListener(JVstHostListener listener) {
    if (!hostListeners.contains(listener)) {
      hostListeners.add(listener);
    }
  }
  
  @Override
  public synchronized void removeJVstHostListener(JVstHostListener listener) {
    hostListeners.remove(listener);
  }
  
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

