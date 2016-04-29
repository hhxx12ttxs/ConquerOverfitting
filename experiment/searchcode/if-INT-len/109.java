/*
 **** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2001 Alan Moore <alan_moore@gmx.net>
 * Copyright (C) 2001-2002 Benoit Cerrina <b.cerrina@wanadoo.fr>
 * Copyright (C) 2001-2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2002-2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2002-2006 Thomas E Enebo <enebo@acm.org>
 * Copyright (C) 2004 Stefan Matthias Aust <sma@3plus4.de>
 * Copyright (C) 2004 David Corbin <dcorbin@users.sourceforge.net>
 * Copyright (C) 2005 Tim Azzopardi <tim@tigerfive.com>
 * Copyright (C) 2006 Miguel Covarrubias <mlcovarrubias@gmail.com>
 * Copyright (C) 2006 Ola Bini <ola@ologix.com>
 * Copyright (C) 2007 Nick Sieger <nicksieger@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.jruby;

import static org.jruby.RubyEnumerator.enumeratorize;
import static org.jruby.anno.FrameField.BACKREF;
import static org.jruby.util.StringSupport.CR_7BIT;
import static org.jruby.util.StringSupport.CR_BROKEN;
import static org.jruby.util.StringSupport.CR_MASK;
import static org.jruby.util.StringSupport.CR_UNKNOWN;
import static org.jruby.util.StringSupport.CR_VALID;
import static org.jruby.util.StringSupport.codeLength;
import static org.jruby.util.StringSupport.codePoint;
import static org.jruby.util.StringSupport.codeRangeScan;
import static org.jruby.util.StringSupport.searchNonAscii;
import static org.jruby.util.StringSupport.strLengthWithCodeRange;
import static org.jruby.util.StringSupport.toLower;
import static org.jruby.util.StringSupport.toUpper;
import static org.jruby.util.StringSupport.unpackArg;
import static org.jruby.util.StringSupport.unpackResult;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Locale;

import org.jcodings.Encoding;
import org.jcodings.ascii.AsciiTables;
import org.jcodings.constants.CharacterType;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.jcodings.util.IntHash;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Region;
import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.javasupport.util.RuntimeHelpers;
import org.jruby.runtime.Block;
import org.jruby.runtime.ClassIndex;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import static org.jruby.runtime.Visibility.*;
import static org.jruby.CompatVersion.*;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.encoding.EncodingCapable;
import org.jruby.runtime.marshal.UnmarshalStream;
import org.jruby.util.ByteList;
import org.jruby.util.ConvertBytes;
import org.jruby.util.MurmurHash;
import org.jruby.util.Numeric;
import org.jruby.util.Pack;
import org.jruby.util.RegexpOptions;
import org.jruby.util.Sprintf;
import org.jruby.util.StringSupport;
import org.jruby.util.TypeConverter;
import org.jruby.util.log.Logger;
import org.jruby.util.log.LoggerFactory;
import org.jruby.util.string.JavaCrypt;

import static org.jruby.javasupport.util.RuntimeHelpers.invokedynamic;
import static org.jruby.runtime.MethodIndex.OP_EQUAL;
import static org.jruby.runtime.MethodIndex.OP_CMP;

/**
 * Implementation of Ruby String class
 * 
 * Concurrency: no synchronization is required among readers, but
 * all users must synchronize externally with writers.
 *
 */
@JRubyClass(name="String", include={"Enumerable", "Comparable"})
public class RubyString extends RubyObject implements EncodingCapable {

    private static final Logger LOG = LoggerFactory.getLogger("RubyString");

    private static final ASCIIEncoding ASCII = ASCIIEncoding.INSTANCE;
    private static final UTF8Encoding UTF8 = UTF8Encoding.INSTANCE;
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    // string doesn't share any resources
    private static final int SHARE_LEVEL_NONE = 0;
    // string has it's own ByteList, but it's pointing to a shared buffer (byte[])
    private static final int SHARE_LEVEL_BUFFER = 1;
    // string doesn't have it's own ByteList (values)
    private static final int SHARE_LEVEL_BYTELIST = 2;

    private volatile int shareLevel = SHARE_LEVEL_NONE;

    private ByteList value;

    public static RubyClass createStringClass(Ruby runtime) {
        RubyClass stringClass = runtime.defineClass("String", runtime.getObject(), STRING_ALLOCATOR);
        runtime.setString(stringClass);
        stringClass.index = ClassIndex.STRING;
        stringClass.setReifiedClass(RubyString.class);
        stringClass.kindOf = new RubyModule.KindOf() {
            @Override
                public boolean isKindOf(IRubyObject obj, RubyModule type) {
                    return obj instanceof RubyString;
                }
            };

        stringClass.includeModule(runtime.getComparable());
        if (!runtime.is1_9()) stringClass.includeModule(runtime.getEnumerable());
        stringClass.defineAnnotatedMethods(RubyString.class);

        return stringClass;
    }

    private static ObjectAllocator STRING_ALLOCATOR = new ObjectAllocator() {
        public IRubyObject allocate(Ruby runtime, RubyClass klass) {
            return RubyString.newEmptyString(runtime, klass);
        }
    };

    public Encoding getEncoding() {
        return value.getEncoding();
    }

    public void setEncoding(Encoding encoding) {
        value.setEncoding(encoding);
    }

    public void associateEncoding(Encoding enc) {
        if (value.getEncoding() != enc) {
            if (!isCodeRangeAsciiOnly() || !enc.isAsciiCompatible()) clearCodeRange();
            value.setEncoding(enc);
        }
    }

    public final void setEncodingAndCodeRange(Encoding enc, int cr) {
        value.setEncoding(enc);
        setCodeRange(cr);
    }

    public final Encoding toEncoding(Ruby runtime) {
        return runtime.getEncodingService().findEncoding(this);
    }

    public final int getCodeRange() {
        return flags & CR_MASK;
    }

    public final void setCodeRange(int codeRange) {
        clearCodeRange();
        flags |= codeRange & CR_MASK;
    }

    public final void clearCodeRange() {
        flags &= ~CR_MASK;
    }

    private void keepCodeRange() {
        if (getCodeRange() == CR_BROKEN) clearCodeRange();
    }

    // ENC_CODERANGE_ASCIIONLY
    public final boolean isCodeRangeAsciiOnly() {
        return getCodeRange() == CR_7BIT;
    }

    // rb_enc_str_asciionly_p
    public final boolean isAsciiOnly() {
        return value.getEncoding().isAsciiCompatible() && scanForCodeRange() == CR_7BIT;
    }

    public final boolean isCodeRangeValid() {
        return (flags & CR_VALID) != 0;
    }

    public final boolean isCodeRangeBroken() {
        return (flags & CR_BROKEN) != 0;
    }

    static int codeRangeAnd(int cr1, int cr2) {
        if (cr1 == CR_7BIT) return cr2;
        if (cr1 == CR_VALID) return cr2 == CR_7BIT ? CR_VALID : cr2;
        return CR_UNKNOWN;
    }

    private void copyCodeRangeForSubstr(RubyString from, Encoding enc) {
        int fromCr = from.getCodeRange();
        if (fromCr == CR_7BIT) {
            setCodeRange(fromCr);
        } else if (fromCr == CR_VALID) {
            if (!enc.isAsciiCompatible() || searchNonAscii(value) != -1) {
                setCodeRange(CR_VALID);
            } else {
                setCodeRange(CR_7BIT);
            }
        } else{ 
            if (value.getRealSize() == 0) {
                setCodeRange(!enc.isAsciiCompatible() ? CR_VALID : CR_7BIT);
            }
        }
    }

    private void copyCodeRange(RubyString from) {
        value.setEncoding(from.value.getEncoding());
        setCodeRange(from.getCodeRange());
    }

    // rb_enc_str_coderange
    final int scanForCodeRange() {
        int cr = getCodeRange();
        if (cr == CR_UNKNOWN) {
            cr = codeRangeScan(value.getEncoding(), value);
            setCodeRange(cr);
        }
        return cr;
    }

    final boolean singleByteOptimizable() {
        return getCodeRange() == CR_7BIT || value.getEncoding().isSingleByte();
    }

    final boolean singleByteOptimizable(Encoding enc) {
        return getCodeRange() == CR_7BIT || enc.isSingleByte();
    }

    private Encoding isCompatibleWith(RubyString other) { 
        Encoding enc1 = value.getEncoding();
        Encoding enc2 = other.value.getEncoding();

        if (enc1 == enc2) return enc1;

        if (other.value.getRealSize() == 0) return enc1;
        if (value.getRealSize() == 0) return enc2;

        if (!enc1.isAsciiCompatible() || !enc2.isAsciiCompatible()) return null;

        return RubyEncoding.areCompatible(enc1, scanForCodeRange(), enc2, other.scanForCodeRange());
    }

    final Encoding isCompatibleWith(EncodingCapable other) {
        if (other instanceof RubyString) return checkEncoding((RubyString)other);
        Encoding enc1 = value.getEncoding();
        Encoding enc2 = other.getEncoding();

        if (enc1 == enc2) return enc1;
        if (value.getRealSize() == 0) return enc2;
        if (!enc1.isAsciiCompatible() || !enc2.isAsciiCompatible()) return null;
        if (enc2 instanceof USASCIIEncoding) return enc1;
        if (scanForCodeRange() == CR_7BIT) return enc2;
        return null;
    }

    final Encoding checkEncoding(RubyString other) {
        Encoding enc = isCompatibleWith(other);
        if (enc == null) throw getRuntime().newEncodingCompatibilityError("incompatible character encodings: " + 
                                value.getEncoding() + " and " + other.value.getEncoding());
        return enc;
    }

    final Encoding checkEncoding(EncodingCapable other) {
        Encoding enc = isCompatibleWith(other);
        if (enc == null) throw getRuntime().newEncodingCompatibilityError("incompatible character encodings: " + 
                                value.getEncoding() + " and " + other.getEncoding());
        return enc;
    }

    private Encoding checkDummyEncoding() {
        Encoding enc = value.getEncoding();
        if (enc.isDummy()) throw getRuntime().newEncodingCompatibilityError(
                "incompatible encoding with this operation: " + enc);
        return enc;
    }

    private boolean isComparableWith(RubyString other) {
        ByteList otherValue = other.value;
        if (value.getEncoding() == otherValue.getEncoding() ||
            value.getRealSize() == 0 || otherValue.getRealSize() == 0) return true;
        return isComparableViaCodeRangeWith(other);
    }

    private boolean isComparableViaCodeRangeWith(RubyString other) {
        int cr1 = scanForCodeRange();
        int cr2 = other.scanForCodeRange();

        if (cr1 == CR_7BIT && (cr2 == CR_7BIT || other.value.getEncoding().isAsciiCompatible())) return true;
        if (cr2 == CR_7BIT && value.getEncoding().isAsciiCompatible()) return true;
        return false;
    }

    private int strLength(Encoding enc) {
        if (singleByteOptimizable(enc)) return value.getRealSize();
        return strLength(value, enc);
    }

    final int strLength() {
        if (singleByteOptimizable()) return value.getRealSize();
        return strLength(value);
    }

    private int strLength(ByteList bytes) {
        return strLength(bytes, bytes.getEncoding());
    }

    private int strLength(ByteList bytes, Encoding enc) {
        if (isCodeRangeValid() && enc instanceof UTF8Encoding) return StringSupport.utf8Length(value);

        long lencr = strLengthWithCodeRange(bytes, enc);
        int cr = unpackArg(lencr);
        if (cr != 0) setCodeRange(cr);
        return unpackResult(lencr);
    }

    final int subLength(int pos) {
        if (singleByteOptimizable() || pos < 0) return pos;
        return StringSupport.strLength(value.getEncoding(), value.getUnsafeBytes(), value.getBegin(), value.getBegin() + pos);
    }

    /** short circuit for String key comparison
     * 
     */
    @Override
    public final boolean eql(IRubyObject other) {
        Ruby runtime = getRuntime();
        if (getMetaClass() != runtime.getString() || getMetaClass() != other.getMetaClass()) return super.eql(other);
        return runtime.is1_9() ? eql19(runtime, other) : eql18(runtime, other);
    }

    private boolean eql18(Ruby runtime, IRubyObject other) {
        return value.equal(((RubyString)other).value);
    }

    // rb_str_hash_cmp
    private boolean eql19(Ruby runtime, IRubyObject other) {
        RubyString otherString = (RubyString)other;
        return isComparableWith(otherString) && value.equal(((RubyString)other).value);
    }

    public RubyString(Ruby runtime, RubyClass rubyClass) {
        this(runtime, rubyClass, EMPTY_BYTE_ARRAY);
    }

    public RubyString(Ruby runtime, RubyClass rubyClass, CharSequence value) {
        super(runtime, rubyClass);
        assert value != null;
        byte[] bytes = RubyEncoding.encodeUTF8(value);
        this.value = new ByteList(bytes, false);
        this.value.setEncoding(UTF8);
    }

    public RubyString(Ruby runtime, RubyClass rubyClass, byte[] value) {
        super(runtime, rubyClass);
        assert value != null;
        this.value = new ByteList(value);
    }

    public RubyString(Ruby runtime, RubyClass rubyClass, ByteList value) {
        super(runtime, rubyClass);
        assert value != null;
        this.value = value;
    }

    public RubyString(Ruby runtime, RubyClass rubyClass, ByteList value, boolean objectSpace) {
        super(runtime, rubyClass, objectSpace);
        assert value != null;
        this.value = value;
    }

    public RubyString(Ruby runtime, RubyClass rubyClass, ByteList value, Encoding encoding, boolean objectSpace) {
        this(runtime, rubyClass, value, objectSpace);
        value.setEncoding(encoding);
    }

    protected RubyString(Ruby runtime, RubyClass rubyClass, ByteList value, Encoding enc, int cr) {
        this(runtime, rubyClass, value);
        value.setEncoding(enc);
        flags |= cr;
    }

    protected RubyString(Ruby runtime, RubyClass rubyClass, ByteList value, Encoding enc) {
        this(runtime, rubyClass, value);
        value.setEncoding(enc);
    }

    protected RubyString(Ruby runtime, RubyClass rubyClass, ByteList value, int cr) {
        this(runtime, rubyClass, value);
        flags |= cr;
    }

    // Deprecated String construction routines
    /** Create a new String which uses the same Ruby runtime and the same
     *  class like this String.
     *
     *  This method should be used to satisfy RCR #38.
     *  @deprecated  
     */
    @Deprecated
    public RubyString newString(CharSequence s) {
        return new RubyString(getRuntime(), getType(), s);
    }

    /** Create a new String which uses the same Ruby runtime and the same
     *  class like this String.
     *
     *  This method should be used to satisfy RCR #38.
     *  @deprecated
     */
    @Deprecated
    public RubyString newString(ByteList s) {
        return new RubyString(getRuntime(), getMetaClass(), s);
    }

    @Deprecated
    public static RubyString newString(Ruby runtime, RubyClass clazz, CharSequence str) {
        return new RubyString(runtime, clazz, str);
    }

    public static RubyString newStringLight(Ruby runtime, ByteList bytes) {
        return new RubyString(runtime, runtime.getString(), bytes, false);
    }

    public static RubyString newStringLight(Ruby runtime, int size) {
        return new RubyString(runtime, runtime.getString(), new ByteList(size), false);
    }

    public static RubyString newStringLight(Ruby runtime, int size, Encoding encoding) {
        return new RubyString(runtime, runtime.getString(), new ByteList(size), encoding, false);
    }
  
    public static RubyString newString(Ruby runtime, CharSequence str) {
        return new RubyString(runtime, runtime.getString(), str);
    }

    public static RubyString newString(Ruby runtime, String str) {
        return new RubyString(runtime, runtime.getString(), str);
    }
    
    public static RubyString newString(Ruby runtime, byte[] bytes) {
        return new RubyString(runtime, runtime.getString(), bytes);
    }

    public static RubyString newString(Ruby runtime, byte[] bytes, int start, int length) {
        byte[] copy = new byte[length];
        System.arraycopy(bytes, start, copy, 0, length);
        return new RubyString(runtime, runtime.getString(), new ByteList(copy, false));
    }

    public static RubyString newString(Ruby runtime, ByteList bytes) {
        return new RubyString(runtime, runtime.getString(), bytes);
    }

    public static RubyString newString(Ruby runtime, ByteList bytes, Encoding encoding) {
        return new RubyString(runtime, runtime.getString(), bytes, encoding);
    }
    
    public static RubyString newUnicodeString(Ruby runtime, String str) {
        ByteList byteList = new ByteList(RubyEncoding.encodeUTF8(str), UTF8Encoding.INSTANCE, false);
        return new RubyString(runtime, runtime.getString(), byteList);
    }
    
    public static RubyString newUnicodeString(Ruby runtime, CharSequence str) {
        ByteList byteList = new ByteList(RubyEncoding.encodeUTF8(str), UTF8Encoding.INSTANCE, false);
        return new RubyString(runtime, runtime.getString(), byteList);
    }

    /**
     * Return a new Ruby String encoded as the default internal encoding given a Java String that
     * has come from an external source. If there is no default internal encoding set, the Ruby
     * String will be encoded using Java's default external encoding. If an internal encoding is
     * set, that encoding will be used for the Ruby String.
     *
     * @param runtime
     * @param str
     * @return
     */
    public static RubyString newInternalFromJavaExternal(Ruby runtime, String str) {
        // Ruby internal
        Encoding internal = runtime.getDefaultInternalEncoding();
        Charset rubyInt = null;
        if (internal != null && internal.getCharset() != null) rubyInt = internal.getCharset();

        // Java external, used if no internal
        Charset javaExt = Charset.defaultCharset();
        Encoding javaExtEncoding = runtime.getEncodingService().getJavaDefault();

        if (rubyInt == null) {
            return RubyString.newString(
                    runtime,
                    new ByteList(str.getBytes(), javaExtEncoding));
        } else {
            return RubyString.newString(
                    runtime,
                    new ByteList(RubyEncoding.encode(str, rubyInt), internal));
        }
    }

    // String construction routines by NOT byte[] buffer and making the target String shared 
    public static RubyString newStringShared(Ruby runtime, RubyString orig) {
        orig.shareLevel = SHARE_LEVEL_BYTELIST;
        RubyString str = new RubyString(runtime, runtime.getString(), orig.value);
        str.shareLevel = SHARE_LEVEL_BYTELIST;
        return str;
    }       

    public static RubyString newStringShared(Ruby runtime, ByteList bytes) {
        return newStringShared(runtime, runtime.getString(), bytes);
    }

    public static RubyString newStringShared(Ruby runtime, ByteList bytes, Encoding encoding) {
        return newStringShared(runtime, runtime.getString(), bytes, encoding);
    }


    public static RubyString newStringShared(Ruby runtime, ByteList bytes, int codeRange) {
        RubyString str = new RubyString(runtime, runtime.getString(), bytes, codeRange);
        str.shareLevel = SHARE_LEVEL_BYTELIST;
        return str;
    }

    public static RubyString newStringShared(Ruby runtime, RubyClass clazz, ByteList bytes) {
        RubyString str = new RubyString(runtime, clazz, bytes);
        str.shareLevel = SHARE_LEVEL_BYTELIST;
        return str;
    }

    public static RubyString newStringShared(Ruby runtime, RubyClass clazz, ByteList bytes, Encoding encoding) {
        RubyString str = new RubyString(runtime, clazz, bytes, encoding);
        str.shareLevel = SHARE_LEVEL_BYTELIST;
        return str;
    }

    public static RubyString newStringShared(Ruby runtime, byte[] bytes) {
        return newStringShared(runtime, new ByteList(bytes, false));
    }

    public static RubyString newStringShared(Ruby runtime, byte[] bytes, int start, int length) {
        return newStringShared(runtime, new ByteList(bytes, start, length, false));
    }

    public static RubyString newEmptyString(Ruby runtime) {
        return newEmptyString(runtime, runtime.getString());
    }

    public static RubyString newEmptyString(Ruby runtime, RubyClass metaClass) {
        RubyString empty = new RubyString(runtime, metaClass, ByteList.EMPTY_BYTELIST);
        empty.shareLevel = SHARE_LEVEL_BYTELIST;
        return empty;
    }

    // String construction routines by NOT byte[] buffer and NOT making the target String shared 
    public static RubyString newStringNoCopy(Ruby runtime, ByteList bytes) {
        return newStringNoCopy(runtime, runtime.getString(), bytes);
    }    

    public static RubyString newStringNoCopy(Ruby runtime, RubyClass clazz, ByteList bytes) {
        return new RubyString(runtime, clazz, bytes);
    }    

    public static RubyString newStringNoCopy(Ruby runtime, byte[] bytes, int start, int length) {
        return newStringNoCopy(runtime, new ByteList(bytes, start, length, false));
    }

    public static RubyString newStringNoCopy(Ruby runtime, byte[] bytes) {
        return newStringNoCopy(runtime, new ByteList(bytes, false));
    }

    /** Encoding aware String construction routines for 1.9
     * 
     */
    private static final class EmptyByteListHolder {
        final ByteList bytes;
        final int cr;
        EmptyByteListHolder(Encoding enc) {
            this.bytes = new ByteList(ByteList.NULL_ARRAY, enc);
            this.cr = bytes.getEncoding().isAsciiCompatible() ? CR_7BIT : CR_VALID;
        }
    }

    private static EmptyByteListHolder EMPTY_BYTELISTS[] = new EmptyByteListHolder[4];

    static EmptyByteListHolder getEmptyByteList(Encoding enc) {
        int index = enc.getIndex();
        EmptyByteListHolder bytes;
        if (index < EMPTY_BYTELISTS.length && (bytes = EMPTY_BYTELISTS[index]) != null) {
            return bytes;
        }
        return prepareEmptyByteList(enc);
    }

    private static EmptyByteListHolder prepareEmptyByteList(Encoding enc) {
        int index = enc.getIndex();
        if (index >= EMPTY_BYTELISTS.length) {
            EmptyByteListHolder tmp[] = new EmptyByteListHolder[index + 4];
            System.arraycopy(EMPTY_BYTELISTS,0, tmp, 0, EMPTY_BYTELISTS.length);
            EMPTY_BYTELISTS = tmp;
        }
        return EMPTY_BYTELISTS[index] = new EmptyByteListHolder(enc);
    }

    public static RubyString newEmptyString(Ruby runtime, RubyClass metaClass, Encoding enc) {
        EmptyByteListHolder holder = getEmptyByteList(enc);
        RubyString empty = new RubyString(runtime, metaClass, holder.bytes, holder.cr);
        empty.shareLevel = SHARE_LEVEL_BYTELIST;
        return empty;
    }

    public static RubyString newEmptyString(Ruby runtime, Encoding enc) {
        return newEmptyString(runtime, runtime.getString(), enc);
    }

    public static RubyString newStringNoCopy(Ruby runtime, RubyClass clazz, ByteList bytes, Encoding enc, int cr) {
        return new RubyString(runtime, clazz, bytes, enc, cr);
    }

    public static RubyString newStringNoCopy(Ruby runtime, ByteList bytes, Encoding enc, int cr) {
        return newStringNoCopy(runtime, runtime.getString(), bytes, enc, cr);
    }

    public static RubyString newUsAsciiStringNoCopy(Ruby runtime, ByteList bytes) {
        return newStringNoCopy(runtime, bytes, USASCIIEncoding.INSTANCE, CR_7BIT);
    }

    public static RubyString newUsAsciiStringShared(Ruby runtime, ByteList bytes) {
        RubyString str = newStringNoCopy(runtime, bytes, USASCIIEncoding.INSTANCE, CR_7BIT);
        str.shareLevel = SHARE_LEVEL_BYTELIST;
        return str;
    }
    
    public static RubyString newUsAsciiStringShared(Ruby runtime, byte[] bytes, int start, int length) {
        byte[] copy = new byte[length];
        System.arraycopy(bytes, start, copy, 0, length);
        return newUsAsciiStringShared(runtime, new ByteList(copy, false));
    }

    @Override
    public int getNativeTypeIndex() {
        return ClassIndex.STRING;
    }

    @Override
    public Class getJavaClass() {
        return String.class;
    }

    @Override
    public RubyString convertToString() {
        return this;
    }

    @Override
    public String toString() {
        return decodeString();
    }

    /**
     * Convert this Ruby string to a Java String. This version is encoding-aware.
     *
     * @return A decoded Java String, based on this Ruby string's encoding.
     */
    public String decodeString() {
        Ruby runtime = getRuntime();
        // Note: we always choose UTF-8 for outbound strings in 1.8 mode.  This is clearly undesirable
        // but we do not mark any incoming Strings from JI with their real encoding so we just pick utf-8.
        
        if (runtime.is1_9()) {
            Encoding encoding = getEncoding();
            
            if (encoding == UTF8) {
                // faster UTF8 decoding
                return RubyEncoding.decodeUTF8(value.getUnsafeBytes(), value.begin(), value.length());
            }
            
            Charset charset = runtime.getEncodingService().charsetForEncoding(encoding);

            encoding.getCharset();

            // charset is not defined for this encoding in jcodings db.  Try letting Java resolve this.
            if (charset == null) {
                try {
                    return new String(value.getUnsafeBytes(), value.begin(), value.length(), encoding.toString());
                } catch (UnsupportedEncodingException uee) {
                    return value.toString();
                }
            }
            
            return RubyEncoding.decode(value.getUnsafeBytes(), value.begin(), value.length(), charset);
        } else {
            // fast UTF8 decoding
            return RubyEncoding.decodeUTF8(value.getUnsafeBytes(), value.begin(), value.length());
        }
    }

    /**
     * Overridden dup for fast-path logic.
     *
     * @return A new RubyString sharing the original backing store.
     */
    @Override
    public IRubyObject dup() {
        RubyClass mc = metaClass.getRealClass();
        if (mc.index != ClassIndex.STRING) return super.dup();

        return strDup(mc.getClassRuntime(), mc.getRealClass());
    }

    /** rb_str_dup
     * 
     */
    @Deprecated
    public final RubyString strDup() {
        return strDup(getRuntime(), getMetaClass());
    }
    
    public final RubyString strDup(Ruby runtime) {
        return strDup(runtime, getMetaClass());
    }
    
    @Deprecated
    final RubyString strDup(RubyClass clazz) {
        return strDup(getRuntime(), getMetaClass());
    }

    final RubyString strDup(Ruby runtime, RubyClass clazz) {
        shareLevel = SHARE_LEVEL_BYTELIST;
        RubyString dup = new RubyString(runtime, clazz, value);
        dup.shareLevel = SHARE_LEVEL_BYTELIST;
        dup.flags |= flags & (CR_MASK | TAINTED_F | UNTRUSTED_F);
        
        return dup;
    }
    
    /* rb_str_subseq */
    public final RubyString makeSharedString(Ruby runtime, int index, int len) {
        return makeShared(runtime, runtime.getString(), index, len);
    }
    
    public RubyString makeSharedString19(Ruby runtime, int index, int len) {
        return makeShared19(runtime, runtime.getString(), value, index, len);
    }

    public final RubyString makeShared(Ruby runtime, int index, int len) {
        return makeShared(runtime, getType(), index, len);
    }

    public final RubyString makeShared(Ruby runtime, RubyClass meta, int index, int len) {
        final RubyString shared;
        if (len == 0) {
            shared = newEmptyString(runtime, meta);
        } else if (len == 1) {
            shared = newStringShared(runtime, meta, 
                    RubyInteger.SINGLE_CHAR_BYTELISTS[value.getUnsafeBytes()[value.getBegin() + index] & 0xff]);
        } else {
            if (shareLevel == SHARE_LEVEL_NONE) shareLevel = SHARE_LEVEL_BUFFER;
            shared = new RubyString(runtime, meta, value.makeShared(index, len));
            shared.shareLevel = SHARE_LEVEL_BUFFER;
        }

        shared.infectBy(this);
        return shared;
    }

    public final RubyString makeShared19(Ruby runtime, int index, int len) {
        return makeShared19(runtime, value, index, len);
    }

    private RubyString makeShared19(Ruby runtime, ByteList value, int index, int len) {
        return makeShared19(runtime, getType(), value, index, len);
    }
    
    private RubyString makeShared19(Ruby runtime, RubyClass meta, ByteList value, int index, int len) {
        final RubyString shared;
        Encoding enc = value.getEncoding();

        if (len == 0) {
            shared = newEmptyString(runtime, meta, enc);
        } else {
            if (shareLevel == SHARE_LEVEL_NONE) shareLevel = SHARE_LEVEL_BUFFER;
            shared = new RubyString(runtime, meta, value.makeShared(index, len));
            shared.shareLevel = SHARE_LEVEL_BUFFER;
            shared.copyCodeRangeForSubstr(this, enc); // no need to assign encoding, same bytelist shared
        }
        shared.infectBy(this);
        return shared;
    }

    public final void modifyCheck() {
        frozenCheck();

        if (!isTaint() && getRuntime().getSafeLevel() >= 4) {
            throw getRuntime().newSecurityError("Insecure: can't modify string");
        }
    }

    private void modifyCheck(byte[] b, int len) {
        if (value.getUnsafeBytes() != b || value.getRealSize() != len) throw getRuntime().newRuntimeError("string modified");
    }

    private void modifyCheck(byte[] b, int len, Encoding enc) {
        if (value.getUnsafeBytes() != b || value.getRealSize() != len || value.getEncoding() != enc) throw getRuntime().newRuntimeError("string modified");
    }

    private void frozenCheck() {
        frozenCheck(false);
    }

    private void frozenCheck(boolean runtimeError) {
        if (isFrozen()) throw getRuntime().newFrozenError("string", runtimeError);
    }

    /** rb_str_modify
     * 
     */
    public final void modify() {
        modifyCheck();

        if (shareLevel != SHARE_LEVEL_NONE) {
            if (shareLevel == SHARE_LEVEL_BYTELIST) {
                value = value.dup();
            } else {
                value.unshare();
            }
            shareLevel = SHARE_LEVEL_NONE;
        }

        value.invalidate();
    }

    public final void modify19() {
        modify();
        clearCodeRange();
    }

    private void modifyAndKeepCodeRange() {
        modify();
        keepCodeRange();
    }
    
    /** rb_str_modify (with length bytes ensured)
     * 
     */    
    public final void modify(int length) {
        modifyCheck();

        if (shareLevel != SHARE_LEVEL_NONE) {
            if (shareLevel == SHARE_LEVEL_BYTELIST) {
                value = value.dup(length);
            } else {
                value.unshare(length);
            }
            shareLevel = SHARE_LEVEL_NONE;
        } else {
            value.ensure(length);
        }

        value.invalidate();
    }
    
    public final void modify19(int length) {
        modify(length);
        clearCodeRange();
    }
    
    /** rb_str_resize
     */
    public final void resize(int length) {
        modify();
        if (value.getRealSize() > length) {
            value.setRealSize(length);
        } else if (value.length() < length) {
            value.length(length);
        }
    }

    public final void view(ByteList bytes) {
        modifyCheck();

        value = bytes;
        shareLevel = SHARE_LEVEL_NONE;
    }

    private void view(byte[]bytes) {
        modifyCheck();        

        value.replace(bytes);
        shareLevel = SHARE_LEVEL_NONE;

        value.invalidate();        
    }

    private void view(int index, int len) {
        modifyCheck();

        if (shareLevel != SHARE_LEVEL_NONE) {
            if (shareLevel == SHARE_LEVEL_BYTELIST) {
                // if len == 0 then shared empty
                value = value.makeShared(index, len);
                shareLevel = SHARE_LEVEL_BUFFER;
            } else {
                value.view(index, len);
            }
        } else {        
            value.view(index, len);
            // FIXME this below is temporary, but its much safer for COW (it prevents not shared Strings with begin != 0)
            // this allows now e.g.: ByteList#set not to be begin aware
            shareLevel = SHARE_LEVEL_BUFFER;
        }

        value.invalidate();
    }

    public static String bytesToString(byte[] bytes, int beg, int len) {
        return new String(ByteList.plain(bytes, beg, len));
    }

    public static String byteListToString(ByteList bytes) {
        return bytesToString(bytes.getUnsafeBytes(), bytes.begin(), bytes.length());
    }

    public static String bytesToString(byte[] bytes) {
        return bytesToString(bytes, 0, bytes.length);
    }

    public static byte[] stringToBytes(String string) {
        return ByteList.plain(string);
    }

    @Override
    public RubyString asString() {
        return this;
    }

    @Override
    public IRubyObject checkStringType() {
        return this;
    }

    @Override
    public IRubyObject checkStringType19() {
        return this;
    }

    @JRubyMethod(name = "try_convert", meta = true, compat = RUBY1_9)
    public static IRubyObject try_convert(ThreadContext context, IRubyObject recv, IRubyObject str) {
        return str.checkStringType();
    }

    @JRubyMethod(name = {"to_s", "to_str"})
    @Override
    public IRubyObject to_s() {
        Ruby runtime = getRuntime();
        if (getMetaClass().getRealClass() != runtime.getString()) {
            return strDup(runtime, runtime.getString());
        }
        return this;
    }

    @Override
    public final int compareTo(IRubyObject other) {
        Ruby runtime = getRuntime();
        if (other instanceof RubyString) {
            RubyString otherString = (RubyString)other;
            return runtime.is1_9() ? op_cmp19(otherString) : op_cmp(otherString);
        }
        return (int)op_cmpCommon(runtime.getCurrentContext(), other).convertToInteger().getLongValue();
    }

    /* rb_str_cmp_m */
    @JRubyMethod(name = "<=>", compat = RUBY1_8)
    @Override
    public IRubyObject op_cmp(ThreadContext context, IRubyObject other) {
        if (other instanceof RubyString) {
            return context.getRuntime().newFixnum(op_cmp((RubyString)other));
        }
        return op_cmpCommon(context, other);
    }

    @JRubyMethod(name = "<=>", compat = RUBY1_9)
    public IRubyObject op_cmp19(ThreadContext context, IRubyObject other) {
        if (other instanceof RubyString) {
            return context.getRuntime().newFixnum(op_cmp19((RubyString)other));
        }
        return op_cmpCommon(context, other);
    }

    private IRubyObject op_cmpCommon(ThreadContext context, IRubyObject other) {
        Ruby runtime = context.getRuntime();
        // deal with case when "other" is not a string
        if (other.respondsTo("to_str") && other.respondsTo("<=>")) {
            IRubyObject result = invokedynamic(context, other, OP_CMP, this);
            if (result.isNil()) return result;
            if (result instanceof RubyFixnum) {
                return RubyFixnum.newFixnum(runtime, -((RubyFixnum)result).getLongValue());
            } else {
                return RubyFixnum.zero(runtime).callMethod(context, "-", result);
            }
        }
        return runtime.getNil();        
    }
        
    /** rb_str_equal
     * 
     */
    @JRubyMethod(name = "==", compat = RUBY1_8)
    @Override
    public IRubyObject op_equal(ThreadContext context, IRubyObject other) {
        Ruby runtime = context.getRuntime();
        if (this == other) return runtime.getTrue();
        if (other instanceof RubyString) {
            return value.equal(((RubyString)other).value) ? runtime.getTrue() : runtime.getFalse();    
        }
        return op_equalCommon(context, other);
    }

    @JRubyMethod(name = {"==", "==="}, compat = RUBY1_9)
    public IRubyObject op_equal19(ThreadContext context, IRubyObject other) {
        Ruby runtime = context.getRuntime();
        if (this == other) return runtime.getTrue();
        if (other instanceof RubyString) {
            RubyString otherString = (RubyString)other;
            return isComparableWith(otherString) && value.equal(otherString.value) ? runtime.getTrue() : runtime.getFalse();    
        }
        return op_equalCommon(context, other);
    }

    private IRubyObject op_equalCommon(ThreadContext context, IRubyObject other) {
        Ruby runtime = context.getRuntime();
        if (!other.respondsTo("to_str")) return runtime.getFalse();
        return invokedynamic(context, other, OP_EQUAL, this).isTrue() ? runtime.getTrue() : runtime.getFalse();
    }

    @JRubyMethod(name = "+", required = 1, compat = RUBY1_8)
    public IRubyObject op_plus(ThreadContext context, IRubyObject _str) {
        RubyString str = _str.convertToString();
        RubyString resultStr = newString(context.getRuntime(), addByteLists(value, str.value));
        resultStr.infectBy(flags | str.flags);
        return resultStr;
    }

    @JRubyMethod(name = "+", required = 1, compat = RUBY1_9)
    public IRubyObject op_plus19(ThreadContext context, IRubyObject _str) {
        RubyString str = _str.convertToString();
        Encoding enc = checkEncoding(str);
        RubyString resultStr = newStringNoCopy(context.getRuntime(), addByteLists(value, str.value),
                                    enc, codeRangeAnd(getCodeRange(), str.getCodeRange()));
        resultStr.infectBy(flags | str.flags);
        return resultStr;
    }

    private ByteList addByteLists(ByteList value1, ByteList value2) {
        ByteList result = new ByteList(value1.getRealSize() + value2.getRealSize());
        result.setRealSize(value1.getRealSize() + value2.getRealSize());
        System.arraycopy(value1.getUnsafeBytes(), value1.getBegin(), result.getUnsafeBytes(), 0, value1.getRealSize());
        System.arraycopy(value2.getUnsafeBytes(), value2.getBegin(), result.getUnsafeBytes(), value1.getRealSize(), value2.getRealSize());
        return result;
    }

    @JRubyMethod(name = "*", required = 1, compat = RUBY1_8)
    public IRubyObject op_mul(ThreadContext context, IRubyObject other) {
        return multiplyByteList(context, other);
    }

    @JRubyMethod(name = "*", required = 1, compat = RUBY1_9)
    public IRubyObject op_mul19(ThreadContext context, IRubyObject other) {
        RubyString result = multiplyByteList(context, other);
        result.value.setEncoding(value.getEncoding());
        result.copyCodeRange(this);
        return result;
    }

    private RubyString multiplyByteList(ThreadContext context, IRubyObject arg) {
        int len = RubyNumeric.num2int(arg);
        if (len < 0) throw context.getRuntime().newArgumentError("negative argument");

        // we limit to int because ByteBuffer can only allocate int sizes
        if (len > 0 && Integer.MAX_VALUE / len < value.getRealSize()) {
            throw context.getRuntime().newArgumentError("argument too big");
        }

        ByteList bytes = new ByteList(len *= value.getRealSize());
        if (len > 0) {
            bytes.setRealSize(len);
            int n = value.getRealSize();
            System.arraycopy(value.getUnsafeBytes(), value.getBegin(), bytes.getUnsafeBytes(), 0, n);
            while (n <= len >> 1) {
                System.arraycopy(bytes.getUnsafeBytes(), 0, bytes.getUnsafeBytes(), n, n);
                n <<= 1;
            }
            System.arraycopy(bytes.getUnsafeBytes(), 0, bytes.getUnsafeBytes(), n, len - n);
        }
        RubyString result = new RubyString(context.getRuntime(), getMetaClass(), bytes);
        result.infectBy(this);
        return result;
    }

    @JRubyMethod(name = "%", required = 1)
    public IRubyObject op_format(ThreadContext context, IRubyObject arg) {
        return opFormatCommon(context, arg, context.getRuntime().getInstanceConfig().getCompatVersion());
    }

    private IRubyObject opFormatCommon(ThreadContext context, IRubyObject arg, CompatVersion compat) {
        IRubyObject tmp = arg.checkArrayType();
        if (tmp.isNil()) tmp = arg;

        ByteList out = new ByteList(value.getRealSize());
        out.setEncoding(value.getEncoding());
        
        boolean tainted;
        
        // FIXME: Should we make this work with platform's locale,
        // or continue hardcoding US?
        switch (compat) {
        case RUBY1_8:
            tainted = Sprintf.sprintf(out, Locale.US, value, tmp);
            break;
        case RUBY1_9:
            tainted = Sprintf.sprintf1_9(out, Locale.US, value, tmp);
            break;
        default:
            throw new RuntimeException("invalid compat version for sprintf: " + compat);
        }
        RubyString str = newString(context.getRuntime(), out);

        str.setTaint(tainted || isTaint());
        return str;
    }

    @JRubyMethod(name = "hash")
    @Override
    public RubyFixnum hash() {
        Ruby runtime = getRuntime();
        return RubyFixnum.newFixnum(runtime, strHashCode(runtime));
    }

    @Override
    public int hashCode() {
        return strHashCode(getRuntime());
    }

    /**
     * Generate a murmurhash for the String, using its associated Ruby instance's hash seed.
     *
     * @param runtime
     * @return
     */
    public int strHashCode(Ruby runtime) {
        int hash = MurmurHash.hash32(value.getUnsafeBytes(), value.getBegin(), value.getRealSize(), runtime.getHashSeed());
        if (runtime.is1_9()) {
            hash ^= (value.getEncoding().isAsciiCompatible() && scanForCodeRange() == CR_7BIT ? 0 : value.getEncoding().getIndex());
        }
        return hash;
    }

    /**
     * Generate a murmurhash for the String, without a seed.
     *
     * @param runtime
     * @return
     */
    public int unseededStrHashCode(Ruby runtime) {
        int hash = MurmurHash.hash32(value.getUnsafeBytes(), value.getBegin(), value.getRealSize(), 0);
        if (runtime.is1_9()) {
            hash ^= (value.getEncoding().isAsciiCompatible() && scanForCodeRange() == CR_7BIT ? 0 : value.getEncoding().getIndex());
        }
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (other instanceof RubyString) {
            if (((RubyString) other).value.equal(value)) return true;
        }

        return false;
    }

    /** rb_obj_as_string
     *
     */
    public static RubyString objAsString(ThreadContext context, IRubyObject obj) {
        if (obj instanceof RubyString) return (RubyString) obj;
        IRubyObject str = obj.callMethod(context, "to_s");
        if (!(str instanceof RubyString)) return (RubyString) obj.anyToString();
        if (obj.isTaint()) str.setTaint(true);
        return (RubyString) str;
    }

    /** rb_str_cmp
     *
     */
    public final int op_cmp(RubyString other) {
        return value.cmp(other.value);
    }

    public final int op_cmp19(RubyString other) {
        int ret = value.cmp(other.value);
        if (ret == 0 && !isComparableWith(other)) {
            return value.getEncoding().getIndex() > other.value.getEncoding().getIndex() ? 1 : -1;
        }
        return ret;
    }

    /** rb_to_id
     *
     */
    @Override
    public String asJavaString() {
        return toString();
    }

    public IRubyObject doClone(){
        return newString(getRuntime(), value.dup());
    }

    public final RubyString cat(byte[] str) {
        modify(value.getRealSize() + str.length);
        System.arraycopy(str, 0, value.getUnsafeBytes(), value.getBegin() + value.getRealSize(), str.length);
        value.setRealSize(value.getRealSize() + str.length);
        return this;
    }

    public final RubyString cat(byte[] str, int beg, int len) {
        modify(value.getRealSize() + len);
        System.arraycopy(str, beg, value.getUnsafeBytes(), value.getBegin() + value.getRealSize(), len);
        value.setRealSize(value.getRealSize() + len);
        return this;
    }

    // // rb_str_buf_append
    public final RubyString cat19(RubyString str) {
        ByteList other = str.value;
        int otherCr = cat(other.getUnsafeBytes(), other.getBegin(), other.getRealSize(),
                other.getEncoding(), str.getCodeRange());
        infectBy(str);
        str.setCodeRange(otherCr);
        return this;
    }

    public final RubyString cat(RubyString str) {
        return cat(str.getByteList());
    }

    public final RubyString cat(ByteList str) {
        modify(value.getRealSize() + str.getRealSize());
        System.arraycopy(str.getUnsafeBytes(), str.getBegin(), value.getUnsafeBytes(), value.getBegin() + value.getRealSize(), str.getRealSize());
        value.setRealSize(value.getRealSize() + str.getRealSize());
        return this;
    }

    public final RubyString cat(byte ch) {
        modify(value.getRealSize() + 1);
        value.getUnsafeBytes()[value.getBegin() + value.getRealSize()] = ch;
        value.setRealSize(value.getRealSize() + 1);
        return this;
    }

    public final RubyString cat(int ch) {
        return cat((byte)ch);
    }

    public final RubyString cat(int code, Encoding enc) {
        int n = codeLength(getRuntime(), enc, code);
        modify(value.getRealSize() + n);
        enc.codeToMbc(code, value.getUnsafeBytes(), value.getBegin() + value.getRealSize());
        value.setRealSize(value.getRealSize() + n);
        return this;
    }

    public final int cat(byte[]bytes, int p, int len, Encoding enc, int cr) {
        modify(value.getRealSize() + len);
        int toCr = getCodeRange();
        Encoding toEnc = value.getEncoding();
        int cr2 = cr;

        if (toEnc == enc) {
            if (toCr == CR_UNKNOWN || (toEnc == ASCIIEncoding.INSTANCE && toCr != CR_7BIT)) { 
                cr = CR_UNKNOWN;
            } else if (cr == CR_UNKNOWN) {
                cr = codeRangeScan(enc, bytes, p, len);
            }
        } else {
            if (!toEnc.isAsciiCompatible() || !enc.isAsciiCompatible()) {
                if (len == 0) return toCr;
                if (value.getRealSize() == 0) {
                    System.arraycopy(bytes, p, value.getUnsafeBytes(), value.getBegin() + value.getRealSize(), len);
                    value.setRealSize(value.getRealSize() + len);
                    setEncodingAndCodeRange(enc, cr);
                    return cr;
                }
                throw getRuntime().newEncodingCompatibilityError("incompatible character encodings: " + toEnc + " and " + enc);
            }
            if (cr == CR_UNKNOWN) cr = codeRangeScan(enc, bytes, p, len);
            if (toCr == CR_UNKNOWN) {
                if (toEnc == ASCIIEncoding.INSTANCE || cr != CR_7BIT) toCr = scanForCodeRange(); 
            }
        }
        if (cr2 != 0) cr2 = cr;

        if (toEnc != enc && toCr != CR_7BIT && cr != CR_7BIT) {        
            throw getRuntime().newEncodingCompatibilityError("incompatible character encodings: " + toEnc + " and " + enc);
        }

        final int resCr;
        final Encoding resEnc;
        if (toCr == CR_UNKNOWN) {
            resEnc = toEnc;
            resCr = CR_UNKNOWN;
        } else if (toCr == CR_7BIT) {
            if (cr == CR_7BIT) {
                resEnc = toEnc != ASCIIEncoding.INSTANCE ? toEnc : enc;
                resCr = CR_7BIT;
            } else {
                resEnc = enc;
                resCr = cr;
            }
        } else if (toCr == CR_VALID) {
            resEnc = toEnc;
            if (cr == CR_7BIT || cr == CR_VALID) {
                resCr = toCr;
            } else {
                resCr = cr;
            }
        } else {
            resEnc = toEnc;
            resCr = len > 0 ? CR_UNKNOWN : toCr;
        }

        if (len < 0) throw getRuntime().newArgumentError("negative string size (or size too big)");            

        System.arraycopy(bytes, p, value.getUnsafeBytes(), value.getBegin() + value.getRealSize(), len);
        value.setRealSize(value.getRealSize() + len);
        setEncodingAndCodeRange(resEnc, resCr);

        return cr2;
    }

    public final int cat(byte[]bytes, int p, int len, Encoding enc) {
        return cat(bytes, p, len, enc, CR_UNKNOWN);
    }

    public final RubyString catAscii(byte[]bytes, int p, int len) {
        Encoding enc = value.getEncoding();
        if (enc.isAsciiCompatible()) {
            cat(bytes, p, len, enc, CR_7BIT);
        } else {
            byte buf[] = new byte[enc.maxLength()];
            int end = p + len;
            while (p < end) {
                int c = bytes[p];
                int cl = codeLength(getRuntime(), enc, c);
                enc.codeToMbc(c, buf, 0);
                cat(buf, 0, cl, enc, CR_VALID);
                p++;
            }
        }
        return this;
    }

    /** rb_str_replace_m
     *
     */
    @JRubyMethod(name = {"replace", "initialize_copy"}, required = 1, compat = RUBY1_8)
    public IRubyObject replace(IRubyObject other) {
        if (this == other) return this;
        replaceCommon(other);
        return this;
    }

    @JRubyMethod(name = {"replace", "initialize_copy"}, required = 1, compat = RUBY1_9)
    public RubyString replace19(IRubyObject other) {
        modifyCheck();
        if (this == other) return this;        
        setCodeRange(replaceCommon(other).getCodeRange()); // encoding doesn't have to be copied.
        return this;
    }

    private RubyString replaceCommon(IRubyObject other) {
        modifyCheck();
        RubyString otherStr = other.convertToString();
        otherStr.shareLevel = shareLevel = SHARE_LEVEL_BYTELIST;
        value = otherStr.value;
        infectBy(otherStr);
        return otherStr;
    }

    @JRubyMethod(name = "clear", compat = RUBY1_9)
    public RubyString clear() {
        modifyCheck();
        Encoding enc = value.getEncoding();

        EmptyByteListHolder holder = getEmptyByteList(enc); 
        value = holder.bytes;
        shareLevel = SHARE_LEVEL_BYTELIST;
        setCodeRange(holder.cr);
        return this;
    }

    @JRubyMethod(name = "reverse", compat = RUBY1_8)
    public IRubyObject reverse(ThreadContext context) {
        Ruby runtime = context.getRuntime();
        if (value.getRealSize() <= 1) return strDup(context.getRuntime());

        byte[]bytes = value.getUnsafeBytes();
        int p = value.getBegin();
        int len = value.getRealSize();
        byte[]obytes = new byte[len];

        for (int i = 0; i <= len >> 1; i++) {
            obytes[i] = bytes[p + len - i - 1];
            obytes[len - i - 1] = bytes[p + i];
        }

        return new RubyString(runtime, getMetaClass(), new ByteList(obytes, false)).infectBy(this);
    }

    @JRubyMethod(name = "reverse", compat = RUBY1_9)
    public IRubyObject reverse19(ThreadContext context) {        
        Ruby runtime = context.getRuntime();
        if (value.getRealSize() <= 1) return strDup(context.getRuntime());

        byte[]bytes = value.getUnsafeBytes();
        int p = value.getBegin();
        int len = value.getRealSize();
        byte[]obytes = new byte[len];

        boolean single = true;
        Encoding enc = value.getEncoding();
        // this really needs to be inlined here
        if (singleByteOptimizable(enc)) {
            for (int i = 0; i <= len >> 1; i++) {
                obytes[i] = bytes[p + len - i - 1];
                obytes[len - i - 1] = bytes[p + i];
            }
        } else {
            int end = p + len;
            int op = len;
            while (p < end) {
                int cl = StringSupport.length(enc, bytes, p, end);
                if (cl > 1 || (bytes[p] & 0x80) != 0) {
                    single = false;
                    op -= cl;
                    System.arraycopy(bytes, p, obytes, op, cl);
                    p += cl;
                } else {
                    obytes[--op] = bytes[p++];
                }
            }
        }

        RubyString result = new RubyString(runtime, getMetaClass(), new ByteList(obytes, false));

        if (getCodeRange() == CR_UNKNOWN) setCodeRange(single ? CR_7BIT : CR_VALID);
        Encoding encoding = value.getEncoding();
        result.value.setEncoding(encoding);
        result.copyCodeRangeForSubstr(this, encoding);
        return result.infectBy(this);
    }

    @JRubyMethod(name = "reverse!", compat = RUBY1_8)
    public RubyString reverse_bang(ThreadContext context) {
        if (value.getRealSize() > 1) {
            modify();
            byte[]bytes = value.getUnsafeBytes();
            int p = value.getBegin();
            int len = value.getRealSize();
            for (int i = 0; i < len >> 1; i++) {
                byte b = bytes[p + i];
                bytes[p + i] = bytes[p + len - i - 1];
                bytes[p + len - i - 1] = b;
            }
        }

        return this;
    }

    @JRubyMethod(name = "reverse!", compat = RUBY1_9)
    public RubyString reverse_bang19(ThreadContext context) {
        modifyCheck();
        if (value.getRealSize() > 1) {
            modifyAndKeepCodeRange();
            byte[]bytes = value.getUnsafeBytes();
            int p = value.getBegin();
            int len = value.getRealSize();
            
            Encoding enc = value.getEncoding();
            // this really needs to be inlined here
            if (singleByteOptimizable(enc)) {
                for (int i = 0; i < len >> 1; i++) {
                    byte b = bytes[p + i];
                    bytes[p + i] = bytes[p + len - i - 1];
                    bytes[p + len - i - 1] = b;
                }
            } else {
                int end = p + len;
                int op = len;
                byte[]obytes = new byte[len];
                boolean single = true;
                while (p < end) {
                    int cl = StringSupport.length(enc, bytes, p, end);
                    if (cl > 1 || (bytes[p] & 0x80) != 0) {
                        single = false;
                        op -= cl;
                        System.arraycopy(bytes, p, obytes, op, cl);
                        p += cl;
                    } else {
                        obytes[--op] = bytes[p++];
                    }
                }
                value.setUnsafeBytes(obytes);
                if (getCodeRange() == CR_UNKNOWN) setCodeRange(single ? CR_7BIT : CR_VALID);
            }
        }
        return this;
    }

    /** rb_str_s_new
     *
     */
    public static RubyString newInstance(IRubyObject recv, IRubyObject[] args, Block block) {
        RubyString newString = newStringShared(recv.getRuntime(), ByteList.EMPTY_BYTELIST);
        newString.setMetaClass((RubyClass) recv);
        newString.callInit(args, block);
        return newString;
    }

    @JRubyMethod(visibility = PRIVATE, compat = RUBY1_8)
    @Override
    public IRubyObject initialize(ThreadContext context) {
        return this;
    }

    @JRubyMethod(visibility = PRIVATE, compat = RUBY1_8)
    public IRubyObject initialize(ThreadContext context, IRubyObject arg0) {
        replace(arg0);
        return this;
    }

    @JRubyMethod(name = "initialize", visibility = PRIVATE, compat = RUBY1_9)
    @Override
    public IRubyObject initialize19(ThreadContext context) {
        return this;
    }

    @JRubyMethod(name = "initialize", visibility = PRIVATE, compat = RUBY1_9)
    public IRubyObject initialize19(ThreadContext context, IRubyObject arg0) {
        replace19(arg0);
        return this;
    }

    @JRubyMethod(compat = RUBY1_8)
    public IRubyObject casecmp(ThreadContext context, IRubyObject other) {
        return RubyFixnum.newFixnum(context.getRuntime(), value.caseInsensitiveCmp(other.convertToString().value));
    }

    @JRubyMethod(name = "casecmp", compat = RUBY1_9)
    public IRubyObject casecmp19(ThreadContext context, IRubyObject other) {
        Ruby runtime = context.getRuntime();
        RubyString otherStr = other.convertToString();
        Encoding enc = isCompatibleWith(otherStr);
        if (enc == null) return runtime.getNil();
        
        if (singleByteOptimizable() && otherStr.singleByteOptimizable()) {
            return RubyFixnum.newFixnum(runtime, value.caseInsensitiveCmp(otherStr.value));
        } else {
            return multiByteCasecmp(runtime, enc, value, otherStr.value);
        }
    }

    private IRubyObject multiByteCasecmp(Ruby runtime, Encoding enc, ByteList value, ByteList otherValue) {
        byte[]bytes = value.getUnsafeBytes();
        int p = value.getBegin();
        int end = p + value.getRealSize();

        byte[]obytes = otherValue.getUnsafeBytes();
        int op = otherValue.getBegin();
        int oend = op + otherValue.getRealSize();

        while (p < end && op < oend) {
            final int c, oc;
            if (enc.isAsciiCompatible()) {
                c = bytes[p] & 0xff;
                oc = obytes[op] & 0xff;
            } else {
                c = StringSupport.preciseCodePoint(enc, bytes, p, end);
                oc = StringSupport.preciseCodePoint(enc, obytes, op, oend);                
            }

            int cl, ocl;
            if (Encoding.isAscii(c) && Encoding.isAscii(oc)) {
                byte uc = AsciiTables.ToUpperCaseTable[c];
                byte uoc = AsciiTables.ToUpperCaseTable[oc];
                if (uc != uoc) {
                    return uc < uoc ? RubyFixnum.minus_one(runtime) : RubyFixnum.one(runtime);
                }
                cl = ocl = 1;
            } else {
                cl = StringSupport.length(enc, bytes, p, end);
                ocl = StringSupport.length(enc, obytes, op, oend);
                // TODO: opt for 2 and 3 ?
                int ret = StringSupport.caseCmp(bytes, p, obytes, op, cl < ocl ? cl : ocl);
                if (ret != 0) return ret < 0 ? RubyFixnum.minus_one(runtime) : RubyFixnum.one(runtime);
                if (cl != ocl) return cl < ocl ? RubyFixnum.minus_one(runtime) : RubyFixnum.one(runtime);
            }

            p += cl;
            op += ocl;
        }
        if (end - p == oend - op) return RubyFixnum.zero(runtime);
        return end - p > oend - op ? RubyFixnum.one(runtime) : RubyFixnum.minus_one(runtime);
    }

    /** rb_str_match
     *
     */
    @JRubyMethod(name = "=~", compat = RUBY1_8, writes = BACKREF)
    @Override
    public IRubyObject op_match(ThreadContext context, IRubyObject other) {
        if (other instanceof RubyRegexp) return ((RubyRegexp) other).op_match(context, this);
        if (other instanceof RubyString) throw context.getRuntime().newTypeError("type mismatch: String given");
        return other.callMethod(context, "=~", this);
    }

    @JRubyMethod(name = "=~", compat = RUBY1_9, writes = BACKREF)
    @Override
    public IRubyObject op_match19(ThreadContext context, IRubyObject other) {
        if (other instanceof RubyRegexp) return ((RubyRegexp) other).op_match19(context, this);
        if (other instanceof RubyString) throw context.getRuntime().newTypeError("type mismatch: String given");
        return other.callMethod(context, "=~", this);
    }
    /**
     * String#match(pattern)
     *
     * rb_str_match_m
     *
     * @param pattern Regexp or String
     */
    @JRubyMethod(compat = RUBY1_8, reads = BACKREF)
    public IRubyObject match(ThreadContext context, IRubyObject pattern) {
        return getPattern(pattern).callMethod(context, "match", this);
    }

    @JRubyMethod(name = "match", compat = RUBY1_9, reads = BACKREF)
    public IRubyObject match19(ThreadContext context, IRubyObject pattern, Block block) {
        IRubyObject result = getPattern(pattern).callMethod(context, "match", this);
        return block.isGiven() && !result.isNil() ? block.yield(context, result) : result;
    }

    @JRubyMethod(name = "match", required = 2, rest = true, compat = RUBY1_9, reads = BACKREF)
    public IRubyObject match19(ThreadContext context, IRubyObject[]args, Block block) {
        RubyRegexp pattern = getPattern(args[0]);
        args[0] = this;
        IRubyObject result = pattern.callMethod(context, "match", args);
        return block.isGiven() && !result.isNil() ? block.yield(context, result) : result;
    }

    /** rb_str_capitalize / rb_str_capitalize_bang
     *
     */
    @JRubyMethod(name = "capitalize", compat = RUBY1_8)
    public IRubyObject capitalize(ThreadContext context) {
        RubyString str = strDup(context.getRuntime());
        str.capitalize_bang(context);
        return str;
    }

    @JRubyMethod(name = "capitalize!", compat = RUBY1_8)
    public IRubyObject capitalize_bang(ThreadContext context) {
        Ruby runtime = context.getRuntime();
        if (value.getRealSize() == 0) {
            modifyCheck();
            return runtime.getNil();
        }

        modify();

        int s = value.getBegin();
        int end = s + value.getRealSize();
        byte[]bytes = value.getUnsafeBytes();
        boolean modify = false;
        
        int c = bytes[s] & 0xff;
        if (ASCII.isLower(c)) {
            bytes[s] = AsciiTables.ToUpperCaseTable[c];
            modify = true;
        }

        while (++s < end) {
            c = bytes[s] & 0xff;
            if (ASCII.isUpper(c)) {
                bytes[s] = AsciiTables.ToLowerCaseTable[c];
                modify = true;
            }
        }

        return modify ? this : runtime.getNil();
    }

    @JRubyMethod(name = "capitalize", compat = RUBY1_9)
    public IRubyObject capitalize19(ThreadContext context) {
        RubyString str = strDup(context.getRuntime());
        str.capitalize_bang19(context);
        return str;
    }

    @JRubyMethod(name = "capitalize!", compat = RUBY1_9)
    public IRubyObject capitalize_bang19(ThreadContext context) {
        Ruby runtime = context.getRuntime();
        Encoding enc = checkDummyEncoding();

        if (value.getRealSize() == 0) {
            modifyCheck();
            return runtime.getNil();
        }

        modifyAndKeepCodeRange();

        int s = value.getBegin();
        int end = s + value.getRealSize();
        byte[]bytes = value.getUnsafeBytes();
        boolean modify = false;

        int c = codePoint(runtime, enc, bytes, s, end);
        if (enc.isLower(c)) {
            enc.codeToMbc(toUpper(enc, c), bytes, s);
            modify = true;
        }

        s += codeLength(runtime, enc, c);
        while (s < end) {
            c = codePoint(runtime, enc, bytes, s, end);
            if (enc.isUpper(c)) {
                enc.codeToMbc(toLower(enc, c), bytes, s);
                modify = true;
            }
            s += codeLength(runtime, enc, c);
        }

        return modify ? this : runtime.getNil();
    }

    @JRubyMethod(name = ">=", compat = RUBY1_8)
    public IRubyObject op_ge(ThreadContext context, IRubyObject other) {
        if (other instanceof RubyString) return context.getRuntime().newBoolean(op_cmp((RubyString) other) >= 0);
        return RubyComparable.op_ge(context, this, other);
    }

    @JRubyMethod(name = ">=", compat = RUBY1_9)
    public IRubyObject op_ge19(ThreadContext context, IRubyObject other) {
        if (other instanceof RubyString) return context.getRuntime().newBoolean(op_cmp19((RubyString) other) >= 0);
        return RubyComparable.op_ge(context, this, other);
    }

    @JRubyMethod(name = ">", compat = RUBY1_8)
    public IRubyObject op_gt(ThreadContext context, IRubyObject other) {
        if (other instanceof RubyString) return context.getRuntime().newBoolean(op_c
