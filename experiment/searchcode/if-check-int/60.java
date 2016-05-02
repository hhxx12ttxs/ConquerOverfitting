/***** BEGIN LICENSE BLOCK *****
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
 * Copyright (C) 2004-2005 Thomas E Enebo <enebo@acm.org>
 * Copyright (C) 2004 Stefan Matthias Aust <sma@3plus4.de>
 * Copyright (C) 2005 David Corbin <dcorbin@users.sourceforge.net>
 * Copyright (C) 2006 Nick Sieger <nicksieger@gmail.com>
 * Copyright (C) 2006 Miguel Covarrubias <mlcovarrubias@gmail.com>
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

import static org.jruby.anno.FrameField.BACKREF;
import static org.jruby.anno.FrameField.LASTLINE;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.joni.Matcher;
import org.joni.NameEntry;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Region;
import org.joni.Syntax;
import org.joni.exception.JOniException;
import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.common.IRubyWarnings.ID;
import org.jruby.exceptions.RaiseException;
import org.jruby.parser.ReOptions;
import org.jruby.runtime.Arity;
import org.jruby.runtime.Block;
import org.jruby.runtime.ClassIndex;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.Visibility;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.encoding.EncodingCapable;
import org.jruby.runtime.marshal.MarshalStream;
import org.jruby.runtime.marshal.UnmarshalStream;
import org.jruby.util.ByteList;
import org.jruby.util.KCode;
import org.jruby.util.Pack;
import org.jruby.util.RegexpOptions;
import org.jruby.util.Sprintf;
import org.jruby.util.StringSupport;
import org.jruby.util.TypeConverter;

@JRubyClass(name="Regexp")
public class RubyRegexp extends RubyObject implements ReOptions, EncodingCapable {
    private Regex pattern;
    private ByteList str = ByteList.EMPTY_BYTELIST;
    private RegexpOptions options;

    public static final int ARG_ENCODING_FIXED     =   16;

    public void setLiteral() {
        options.setLiteral(true);
    }

    public void clearLiteral() {
        options.setLiteral(false);
    }

    public boolean isLiteral() {
        return options.isLiteral();
    }

    public boolean isKCodeDefault() {
        return options.isKcodeDefault();
    }

    public void setEncodingNone() {
        options.setEncodingNone(true);
    }
    
    public void clearEncodingNone() {
        options.setEncodingNone(false);
    }

    public boolean isEncodingNone() {
        return options.isEncodingNone();
    }

    public KCode getKCode() {
        return options.getKCode();
    }

    public Encoding getEncoding() {
        return pattern.getEncoding();
    }

    public void setEncoding(Encoding encoding) {
        // FIXME: Which encoding should be changed here?  
        // FIXME: transcode?
    }

    private static final class RegexpCache {
        private volatile SoftReference<Map<ByteList, Regex>> cache = new SoftReference<Map<ByteList, Regex>>(null);
        private Map<ByteList, Regex> get() {
            Map<ByteList, Regex> patternCache = cache.get();
            if (patternCache == null) {
                patternCache = new ConcurrentHashMap<ByteList, Regex>(5);
                cache = new SoftReference<Map<ByteList, Regex>>(patternCache);
            }
            return patternCache;
        }
    }

    private static final RegexpCache patternCache = new RegexpCache();
    private static final RegexpCache quotedPatternCache = new RegexpCache();
    private static final RegexpCache preprocessedPatternCache = new RegexpCache();

    private static Regex makeRegexp(Ruby runtime, ByteList bytes, RegexpOptions options, Encoding enc) {
        try {
            int p = bytes.getBegin();
            return new Regex(bytes.getUnsafeBytes(), p, p + bytes.getRealSize(), options.toJoniOptions(), enc, Syntax.DEFAULT, runtime.getWarnings());
        } catch (Exception e) {
            if (runtime.is1_9()) {
                raiseRegexpError19(runtime, bytes, enc, options, e.getMessage());
            } else {
                raiseRegexpError(runtime, bytes, enc, options, e.getMessage());
            }
            return null; // not reached
        }
    }

    static Regex getRegexpFromCache(Ruby runtime, ByteList bytes, Encoding enc, RegexpOptions options) {
        Map<ByteList, Regex> cache = patternCache.get();
        Regex regex = cache.get(bytes);
        if (regex != null && regex.getEncoding() == enc && regex.getOptions() == options.toJoniOptions()) return regex;
        regex = makeRegexp(runtime, bytes, options, enc);
        cache.put(bytes, regex);
        return regex;
    }

    static Regex getQuotedRegexpFromCache(Ruby runtime, ByteList bytes, Encoding enc, RegexpOptions options) {
        Map<ByteList, Regex> cache = quotedPatternCache.get();
        Regex regex = cache.get(bytes);
        if (regex != null && regex.getEncoding() == enc && regex.getOptions() == options.toJoniOptions()) return regex;
        regex = makeRegexp(runtime, quote(bytes, enc), options, enc);
        cache.put(bytes, regex);
        return regex;
    }

    static Regex getQuotedRegexpFromCache19(Ruby runtime, ByteList bytes, RegexpOptions options, boolean asciiOnly) {
        Map<ByteList, Regex> cache = quotedPatternCache.get();
        Regex regex = cache.get(bytes);
        Encoding enc = asciiOnly ? USASCIIEncoding.INSTANCE : bytes.getEncoding();
        if (regex != null && regex.getEncoding() == enc && regex.getOptions() == options.toJoniOptions()) return regex;
        ByteList quoted = quote19(bytes, asciiOnly);
        regex = makeRegexp(runtime, quoted, options, quoted.getEncoding());
        regex.setUserObject(quoted);
        cache.put(bytes, regex);
        return regex;
    }

    private static Regex getPreprocessedRegexpFromCache(Ruby runtime, ByteList bytes, Encoding enc, RegexpOptions options, ErrorMode mode) {
        Map<ByteList, Regex> cache = preprocessedPatternCache.get();
        Regex regex = cache.get(bytes);
        if (regex != null && regex.getEncoding() == enc && regex.getOptions() == options.toJoniOptions()) return regex;
        ByteList preprocessed = preprocess(runtime, bytes, enc, new Encoding[]{null}, ErrorMode.RAISE);
        regex = makeRegexp(runtime, preprocessed, options, enc);
        regex.setUserObject(preprocessed);
        cache.put(bytes, regex);
        return regex;
    }

    public static RubyClass createRegexpClass(Ruby runtime) {
        RubyClass regexpClass = runtime.defineClass("Regexp", runtime.getObject(), REGEXP_ALLOCATOR);
        runtime.setRegexp(regexpClass);

        regexpClass.index = ClassIndex.REGEXP;
        regexpClass.setReifiedClass(RubyRegexp.class);
        
        regexpClass.kindOf = new RubyModule.KindOf() {
            @Override
            public boolean isKindOf(IRubyObject obj, RubyModule type) {
                return obj instanceof RubyRegexp;
            }
        };

        regexpClass.defineConstant("IGNORECASE", runtime.newFixnum(RE_OPTION_IGNORECASE));
        regexpClass.defineConstant("EXTENDED", runtime.newFixnum(RE_OPTION_EXTENDED));
        regexpClass.defineConstant("MULTILINE", runtime.newFixnum(RE_OPTION_MULTILINE));

        if (runtime.is1_9()) regexpClass.defineConstant("FIXEDENCODING", runtime.newFixnum(ARG_ENCODING_FIXED));

        regexpClass.defineAnnotatedMethods(RubyRegexp.class);

        return regexpClass;
    }

    private static ObjectAllocator REGEXP_ALLOCATOR = new ObjectAllocator() {
        public IRubyObject allocate(Ruby runtime, RubyClass klass) {
            return new RubyRegexp(runtime, klass);
        }
    };

    @Override
    public int getNativeTypeIndex() {
        return ClassIndex.REGEXP;
    }

    /** used by allocator
     */
    private RubyRegexp(Ruby runtime, RubyClass klass) {
        super(runtime, klass);
        this.options = new RegexpOptions();
    }

    /** default constructor
     */
    private RubyRegexp(Ruby runtime) {
        super(runtime, runtime.getRegexp());
        this.options = new RegexpOptions();
    }

    private RubyRegexp(Ruby runtime, ByteList str) {
        this(runtime);
        this.str = str;
        this.pattern = getRegexpFromCache(runtime, str, getEncoding(runtime, str), RegexpOptions.NULL_OPTIONS);
    }

    private RubyRegexp(Ruby runtime, ByteList str, RegexpOptions options) {
        this(runtime);

        if (runtime.is1_9()) {
            initializeCommon19(str, str.getEncoding(), options);
        } else {
            this.options = options;            
            this.str = str;
            this.pattern = getRegexpFromCache(runtime, str, getEncoding(runtime, str), options);
        }
    }

    private Encoding getEncoding(Ruby runtime, ByteList str) {
        if (runtime.is1_9()) return str.getEncoding();

        // Whatever $KCODE is we should use
        if (options.isKcodeDefault()) return runtime.getKCode().getEncoding();
        
        return options.getKCode().getEncoding();
    }

    // used only by the compiler/interpreter (will set the literal flag)
    public static RubyRegexp newRegexp(Ruby runtime, String pattern, RegexpOptions options) {
        return newRegexp(runtime, ByteList.create(pattern), options);
    }

    // used only by the compiler/interpreter (will set the literal flag)
    public static RubyRegexp newRegexp(Ruby runtime, ByteList pattern, RegexpOptions options) {
        try {
            return new RubyRegexp(runtime, pattern, options);
        } catch (RaiseException re) {
            throw runtime.newSyntaxError(re.getMessage());
        }
    }

    // used only by the compiler/interpreter (will set the literal flag)
    public static RubyRegexp newDRegexp(Ruby runtime, RubyString pattern, RegexpOptions options) {
        try {
            return new RubyRegexp(runtime, pattern.getByteList(), options);
        } catch (RaiseException re) {
            throw runtime.newRegexpError(re.getMessage());
        }
    }

    // used only by the compiler/interpreter (will set the literal flag)
    public static RubyRegexp newDRegexp(Ruby runtime, RubyString pattern, int joniOptions) {
        try {
            RegexpOptions options = RegexpOptions.fromJoniOptions(joniOptions);
            return new RubyRegexp(runtime, pattern.getByteList(), options);
        } catch (RaiseException re) {
            throw runtime.newRegexpError(re.getMessage());
        }
    }

    // used only by the compiler/interpreter (will set the literal flag)
    public static RubyRegexp newDRegexpEmbedded(Ruby runtime, RubyString pattern, int embeddedOptions) {
        try {
            RegexpOptions options = RegexpOptions.fromEmbeddedOptions(embeddedOptions);
            // FIXME: Massive hack (fix in DRegexpNode too for interpreter)
            if (pattern.getEncoding() == USASCIIEncoding.INSTANCE) {
                pattern.setEncoding(ASCIIEncoding.INSTANCE);
            }
            return new RubyRegexp(runtime, pattern.getByteList(), options);
        } catch (RaiseException re) {
            throw runtime.newRegexpError(re.getMessage());
        }
    }
    
    public static RubyRegexp newDRegexpEmbedded19(Ruby runtime, IRubyObject[] strings, int embeddedOptions) {
        try {
            RegexpOptions options = RegexpOptions.fromEmbeddedOptions(embeddedOptions);
            RubyString pattern = preprocessDRegexp(runtime, strings, options);
            
            return new RubyRegexp(runtime, pattern.getByteList(), options);
        } catch (RaiseException re) {
            throw runtime.newRegexpError(re.getMessage());
        }
        
    }
    
    public static RubyRegexp newRegexp(Ruby runtime, ByteList pattern) {
        return new RubyRegexp(runtime, pattern);
    }

    static RubyRegexp newRegexp(Ruby runtime, ByteList str, Regex pattern) {
        RubyRegexp regexp = new RubyRegexp(runtime);
        regexp.str = str;
        regexp.options = RegexpOptions.fromJoniOptions(pattern.getOptions());
        regexp.pattern = pattern;
        return regexp;
    }
    
    // internal usage (Complex/Rational)
    static RubyRegexp newDummyRegexp(Ruby runtime, Regex regex) {
        RubyRegexp regexp = new RubyRegexp(runtime);
        regexp.pattern = regex;
        regexp.str = ByteList.EMPTY_BYTELIST;
        regexp.options.setFixed(true);
        return regexp;
    }

    /** rb_reg_options
     */
    public RegexpOptions getOptions() {
        check();
        return options;
    }

    public final Regex getPattern() {
        check();
        return pattern;
    }

    private static void encodingMatchError(Ruby runtime, Regex pattern, Encoding strEnc) {
        throw runtime.newEncodingCompatibilityError("incompatible encoding regexp match (" +
                pattern.getEncoding() + " regexp with " + strEnc + " string)");
    }

    private Encoding checkEncoding(RubyString str, boolean warn) {
        if (str.scanForCodeRange() == StringSupport.CR_BROKEN) {
            throw getRuntime().newArgumentError("invalid byte sequence in " + str.getEncoding());
        }
        check();
        Encoding enc = str.getEncoding();
        if (!enc.isAsciiCompatible()) {
            if (enc != pattern.getEncoding()) encodingMatchError(getRuntime(), pattern, enc);
        } else if (options.isFixed()) {
            if (enc != pattern.getEncoding() && 
               (!pattern.getEncoding().isAsciiCompatible() ||
               str.scanForCodeRange() != StringSupport.CR_7BIT)) encodingMatchError(getRuntime(), pattern, enc);
            enc = pattern.getEncoding();
        }
        if (warn && isEncodingNone() && enc != ASCIIEncoding.INSTANCE && str.scanForCodeRange() != StringSupport.CR_7BIT) {
            getRuntime().getWarnings().warn(ID.REGEXP_MATCH_AGAINST_STRING, "regexp match /.../n against to " + enc + " string");
        }
        return enc;
    }

    final Regex preparePattern(RubyString str) {
        check();
        Encoding enc = checkEncoding(str, true);
        if (enc == pattern.getEncoding()) return pattern;
        return getPreprocessedRegexpFromCache(getRuntime(), this.str, enc, options, ErrorMode.PREPROCESS);
    }

    static Regex preparePattern(Ruby runtime, Regex pattern, RubyString str) {
        if (str.scanForCodeRange() == StringSupport.CR_BROKEN) {
            throw runtime.newArgumentError("invalid byte sequence in " + str.getEncoding());
        }
        Encoding enc = str.getEncoding();
        if (!enc.isAsciiCompatible()) {
            if (enc != pattern.getEncoding()) encodingMatchError(runtime, pattern, enc);
        }
        // TODO: check for isKCodeDefault() somehow
//        if (warn && isEncodingNone() && enc != ASCIIEncoding.INSTANCE && str.scanForCodeRange() != StringSupport.CR_7BIT) {
//            getRuntime().getWarnings().warn(ID.REGEXP_MATCH_AGAINST_STRING, "regexp match /.../n against to " + enc + " string");
//        }
        if (enc == pattern.getEncoding()) return pattern;
        return getPreprocessedRegexpFromCache(runtime, (ByteList)pattern.getUserObject(), enc, RegexpOptions.fromJoniOptions(pattern.getOptions()), ErrorMode.PREPROCESS);
    }

    private static enum ErrorMode {RAISE, PREPROCESS, DESC} 

    private static int raisePreprocessError(Ruby runtime, ByteList str, String err, ErrorMode mode) {
        switch (mode) {
        case RAISE:
            raiseRegexpError19(runtime, str, str.getEncoding(), RegexpOptions.NULL_OPTIONS, err);
        case PREPROCESS:
            throw runtime.newArgumentError("regexp preprocess failed: " + err);
        case DESC:
            // silent ?
        }
        return 0;
    }

    private static int readEscapedByte(Ruby runtime, byte[]to, int toP, byte[]bytes, int p, int end, ByteList str, ErrorMode mode) {
        if (p == end || bytes[p++] != (byte)'\\') raisePreprocessError(runtime, str, "too short escaped multibyte character", mode);

        boolean metaPrefix = false, ctrlPrefix = false;
        int code = 0;
        while (true) {
            if (p == end) raisePreprocessError(runtime, str, "too short escape sequence", mode);

            switch (bytes[p++]) {
            case '\\': code = '\\'; break;
            case 'n': code = '\n'; break;
            case 't': code = '\t'; break;
            case 'r': code = '\r'; break;
            case 'f': code = '\f'; break;
            case 'v': code = '\013'; break;
            case 'a': code = '\007'; break;
            case 'e': code = '\033'; break;

            /* \OOO */
            case '0': case '1': case '2': case '3':
            case '4': case '5': case '6': case '7':
                p--;
                int olen = end < p + 3 ? end - p : 3;
                code = StringSupport.scanOct(bytes, p, olen);
                p += StringSupport.octLength(bytes, p, olen);
                break;

            case 'x': /* \xHH */
                int hlen = end < p + 2 ? end - p : 2;
                code = StringSupport.scanHex(bytes, p, hlen);
                int len = StringSupport.hexLength(bytes, p, hlen);
                if (len < 1) raisePreprocessError(runtime, str, "invalid hex escape", mode);
                p += len;
                break;

            case 'M': /* \M-X, \M-\C-X, \M-\cX */
                if (metaPrefix) raisePreprocessError(runtime, str, "duplicate meta escape", mode);
                metaPrefix = true;
                if (p + 1 < end && bytes[p++] == (byte)'-' && (bytes[p] & 0x80) == 0) {
                    if (bytes[p] == (byte)'\\') {
                        p++;
                        continue;
                    } else {
                        code = bytes[p++] & 0xff;
                        break;
                    }
                }
                raisePreprocessError(runtime, str, "too short meta escape", mode);

            case 'C': /* \C-X, \C-\M-X */
                if (p == end || bytes[p++] != (byte)'-') raisePreprocessError(runtime, str, "too short control escape", mode);

            case 'c': /* \cX, \c\M-X */
                if (ctrlPrefix) raisePreprocessError(runtime, str, "duplicate control escape", mode);
                ctrlPrefix = true;
                if (p < end && (bytes[p] & 0x80) == 0) {
                    if (bytes[p] == (byte)'\\') {
                        p++;
                        continue;
                    } else {
                        code = bytes[p++] & 0xff;
                        break;
                    }
                }
                raisePreprocessError(runtime, str, "too short control escape", mode);
            default:
                raisePreprocessError(runtime, str, "unexpected escape sequence", mode);
            } // switch

            if (code < 0 || code > 0xff) raisePreprocessError(runtime, str, "invalid escape code", mode);

            if (ctrlPrefix) code &= 0x1f;
            if (metaPrefix) code |= 0x80;

            to[toP] = (byte)code;
            return p;
        } // while
    }

    // MRI: unescape_escapted_nonascii
    private static int unescapeEscapedNonAscii(Ruby runtime, ByteList to, byte[]bytes, int p, int end, Encoding enc, Encoding[]encp, ByteList str, ErrorMode mode) {
        byte[]chBuf = new byte[enc.maxLength()];
        int chLen = 0;

        p = readEscapedByte(runtime, chBuf, chLen++, bytes, p, end, str, mode);
        while (chLen < enc.maxLength() && StringSupport.preciseLength(enc, chBuf, 0, chLen) < -1) { // MBCLEN_NEEDMORE_P
            p = readEscapedByte(runtime, chBuf, chLen++, bytes, p, end, str, mode);
        }

        int cl = StringSupport.preciseLength(enc, chBuf, 0, chLen);
        if (cl == -1) {
            raisePreprocessError(runtime, str, "invalid multibyte escape", mode); // MBCLEN_INVALID_P
        }

        if (chLen > 1 || (chBuf[0] & 0x80) != 0) {
            to.append(chBuf, 0, chLen);

            if (encp[0] == null) {
                encp[0] = enc;
            } else if (encp[0] != enc) {
                raisePreprocessError(runtime, str, "escaped non ASCII character in UTF-8 regexp", mode);
            }
        } else {
            Sprintf.sprintf(runtime, to, "\\x%02X", chBuf[0] & 0xff);
        }
        return p;
    }

    private static void checkUnicodeRange(Ruby runtime, int code, ByteList str, ErrorMode mode) {
        // Unicode is can be only 21 bits long, int is enough
        if ((0xd800 <= code && code <= 0xdfff) /* Surrogates */ || 0x10ffff < code) {
            raisePreprocessError(runtime, str, "invalid Unicode range", mode);
        }
    }

    private static void appendUtf8(Ruby runtime, ByteList to, int code, Encoding[]enc, ByteList str, ErrorMode mode) {
        checkUnicodeRange(runtime, code, str, mode);

        if (code < 0x80) {
            Sprintf.sprintf(runtime, to, "\\x%02X", code);
        } else {
            to.ensure(to.getRealSize() + 6);
            to.setRealSize(to.getRealSize() + Pack.utf8Decode(runtime, to.getUnsafeBytes(), to.getBegin() + to.getRealSize(), code));
            if (enc[0] == null) {
                enc[0] = UTF8Encoding.INSTANCE;
            } else if (!(enc[0] instanceof UTF8Encoding)) { // do not load the class if not used
                raisePreprocessError(runtime, str, "UTF-8 character in non UTF-8 regexp", mode);
            }
        }
    }
    
    private static int unescapeUnicodeList(Ruby runtime, ByteList to, byte[]bytes, int p, int end, Encoding[]encp, ByteList str, ErrorMode mode) {
        while (p < end && ASCIIEncoding.INSTANCE.isSpace(bytes[p] & 0xff)) p++;

        boolean hasUnicode = false; 
        while (true) {
            int code = StringSupport.scanHex(bytes, p, end - p);
            int len = StringSupport.hexLength(bytes, p, end - p);
            if (len == 0) break;
            if (len > 6) raisePreprocessError(runtime, str, "invalid Unicode range", mode);
            p += len;
            appendUtf8(runtime, to, code, encp, str, mode);
            hasUnicode = true;
            while (p < end && ASCIIEncoding.INSTANCE.isSpace(bytes[p] & 0xff)) p++;
        }

        if (!hasUnicode) raisePreprocessError(runtime, str, "invalid Unicode list", mode); 
        return p;
    }

    private static int unescapeUnicodeBmp(Ruby runtime, ByteList to, byte[]bytes, int p, int end, Encoding[]encp, ByteList str, ErrorMode mode) {
        if (p + 4 > end) raisePreprocessError(runtime, str, "invalid Unicode escape", mode);
        int code = StringSupport.scanHex(bytes, p, 4);
        int len = StringSupport.hexLength(bytes, p, 4);
        if (len != 4) raisePreprocessError(runtime, str, "invalid Unicode escape", mode);
        appendUtf8(runtime, to, code, encp, str, mode);
        return p + 4;
    }

    private static boolean unescapeNonAscii(Ruby runtime, ByteList to, byte[]bytes, int p, int end, Encoding enc, Encoding[]encp, ByteList str, ErrorMode mode) {
        boolean hasProperty = false;

        while (p < end) {
            int cl = StringSupport.preciseLength(enc, bytes, p, end);
            if (cl <= 0) raisePreprocessError(runtime, str, "invalid multibyte character", mode);
            if (cl > 1 || (bytes[p] & 0x80) != 0) {
                to.append(bytes, p, cl);
                p += cl;
                if (encp[0] == null) {
                    encp[0] = enc;
                } else if (encp[0] != enc) {
                    raisePreprocessError(runtime, str, "non ASCII character in UTF-8 regexp", mode);
                }
                continue;
            }
            int c;
            switch (c = bytes[p++] & 0xff) {
            case '\\':
                if (p == end) raisePreprocessError(runtime, str, "too short escape sequence", mode);

                switch (c = bytes[p++] & 0xff) {
                case '1': case '2': case '3':
                case '4': case '5': case '6': case '7': /* \O, \OO, \OOO or backref */
                    if (StringSupport.scanOct(bytes, p - 1, end - (p - 1)) <= 0177) {
                        to.append('\\').append(c);
                        break;
                    }

                case '0': /* \0, \0O, \0OO */
                case 'x': /* \xHH */
                case 'c': /* \cX, \c\M-X */
                case 'C': /* \C-X, \C-\M-X */
                case 'M': /* \M-X, \M-\C-X, \M-\cX */
                    p = unescapeEscapedNonAscii(runtime, to, bytes, p - 2, end, enc, encp, str, mode);
                    break;

                case 'u':
                    if (p == end) raisePreprocessError(runtime, str, "too short escape sequence", mode);
                    if (bytes[p] == (byte)'{') { /* \\u{H HH HHH HHHH HHHHH HHHHHH ...} */
                        p++;
                        p = unescapeUnicodeList(runtime, to, bytes, p, end, encp, str, mode);
                        if (p == end || bytes[p++] != (byte)'}') raisePreprocessError(runtime, str, "invalid Unicode list", mode);
                    } else { /* \\uHHHH */
                        p = unescapeUnicodeBmp(runtime, to, bytes, p, end, encp, str, mode);
                    }
                    break;
                case 'p': /* \p{Hiragana} */
                    if (encp[0] == null) hasProperty = true;
                    to.append('\\').append(c);
                    break;

                default:
                    to.append('\\').append(c);
                    break;
                } // inner switch
                break;

            default:
                to.append(c);
            } // switch
        } // while
        return hasProperty;
    }

    private static ByteList preprocess(Ruby runtime, ByteList str, Encoding enc, Encoding[]fixedEnc, ErrorMode mode) {
        ByteList to = new ByteList(str.getRealSize());

        if (enc.isAsciiCompatible()) {
            fixedEnc[0] = null;
        } else {
            fixedEnc[0] = enc;
            to.setEncoding(enc);
        }

        boolean hasProperty = unescapeNonAscii(runtime, to, str.getUnsafeBytes(), str.getBegin(), str.getBegin() + str.getRealSize(), enc, fixedEnc, str, mode);
        if (hasProperty && fixedEnc[0] == null) fixedEnc[0] = enc;
        if (fixedEnc[0] != null) to.setEncoding(fixedEnc[0]);
        return to;
    }

    public static void preprocessCheck(Ruby runtime, ByteList bytes) {
        preprocess(runtime, bytes, bytes.getEncoding(), new Encoding[]{null}, ErrorMode.RAISE);
    }
    
    // rb_reg_preprocess_dregexp
    public static RubyString preprocessDRegexp(Ruby runtime, IRubyObject[] strings, RegexpOptions options) {
        RubyString string = null;
        Encoding regexpEnc = null;
        
        for (int i = 0; i < strings.length; i++) {
            RubyString str = strings[i].convertToString();
            str.scanForCodeRange(); // FIXME: Move to better location
            Encoding strEnc = str.getEncoding();
            
            if (options.isEncodingNone() && strEnc != ASCIIEncoding.INSTANCE) {
                if (str.getCodeRange() != StringSupport.CR_7BIT) {
                    throw runtime.newRegexpError("/.../n has a non escaped non ASCII character in non ASCII-8BIT script");
                }
                strEnc = ASCIIEncoding.INSTANCE;
            }
            
            Encoding[] fixedEnc = new Encoding[1];
            ByteList buf = RubyRegexp.preprocess(runtime, str.getByteList(), strEnc, fixedEnc, RubyRegexp.ErrorMode.PREPROCESS);
            
            if (fixedEnc[0] != null) {
                if (regexpEnc != null && regexpEnc != fixedEnc[0]) {
                    throw runtime.newRegexpError("encoding mismatch in dynamic regexp: " + new String(regexpEnc.getName()) + " and " + new String(fixedEnc[0].getName()));
                }
                regexpEnc = fixedEnc[0];
            }
            
            if (string == null) {
                string = (RubyString)str.dup();
            } else {
                string.append19(str);
            }
        }
        
        if (regexpEnc != null) {
            string.setEncoding(regexpEnc);
        }

        return string;
    }

    private void check() {
        if (pattern == null) throw getRuntime().newTypeError("uninitialized Regexp");
    }

    @JRubyMethod(name = {"new", "compile"}, rest = true, meta = true)
    public static RubyRegexp newInstance(IRubyObject recv, IRubyObject[] args) {
        RubyClass klass = (RubyClass)recv;
        RubyRegexp re = (RubyRegexp) klass.allocate();

        re.callInit(args, Block.NULL_BLOCK);
        return re;
    }

    @JRubyMethod(name = "try_convert", meta = true, compat = CompatVersion.RUBY1_9)
    public static IRubyObject try_convert(ThreadContext context, IRubyObject recv, IRubyObject args) {
        return TypeConverter.convertToTypeWithCheck(args, context.getRuntime().getRegexp(), "to_regexp");
    }

    /** rb_reg_s_quote
     * 
     */
    @JRubyMethod(name = {"quote", "escape"}, required = 1, optional = 1, meta = true, compat = CompatVersion.RUBY1_8)
    public static RubyString quote(ThreadContext context, IRubyObject recv, IRubyObject[] args) {
        Ruby runtime = context.getRuntime();
        final KCode code;
        if (args.length == 1 || args[1].isNil()) {
            code = runtime.getKCode();
        } else {
            code = KCode.create(runtime, args[1].toString());
        }

        RubyString src = args[0].convertToString();
        RubyString dst = RubyString.newStringShared(runtime, quote(src.getByteList(), code.getEncoding()));
        dst.infectBy(src);
        return dst;
    }

    @JRubyMethod(name = {"quote", "escape"}, meta = true, compat = CompatVersion.RUBY1_9)
    public static IRubyObject quote19(ThreadContext context, IRubyObject recv, IRubyObject arg) {
        Ruby runtime = context.getRuntime();
        RubyString str = operandCheck(runtime, arg);
        return RubyString.newStringShared(runtime, quote19(str.getByteList(), str.isAsciiOnly()));
    }

    /** rb_reg_quote
     *
     */
    private static ByteList quote(ByteList bs, Encoding enc) {
        int p = bs.getBegin();
        int end = p + bs.getRealSize();
        byte[]bytes = bs.getUnsafeBytes();

        metaFound: do {
            for(; p < end; p++) {
                int c = bytes[p] & 0xff;
                int cl = enc.length(bytes, p, end);
                if (cl != 1) {
                    while (cl-- > 0 && p < end) p++;
                    p--;
                    continue;
                }
                switch (c) {
                case '[': case ']': case '{': case '}':
                case '(': case ')': case '|': case '-':
                case '*': case '.': case '\\':
                case '?': case '+': case '^': case '$':
                case ' ': case '#':
                case '\t': case '\f': case '\n': case '\r':
                    break metaFound;
                }
            }
            return bs;
        } while (false);

        ByteList result = new ByteList(end * 2);
        byte[]obytes = result.getUnsafeBytes();
        int op = p - bs.getBegin();
        System.arraycopy(bytes, bs.getBegin(), obytes, 0, op);

        for(; p < end; p++) {
            int c = bytes[p] & 0xff;
            int cl = enc.length(bytes, p, end);
            if (cl != 1) {
                while (cl-- > 0 && p < end) obytes[op++] = bytes[p++];
                p--;
                continue;
            }

            switch (c) {
            case '[': case ']': case '{': case '}':
            case '(': case ')': case '|': case '-':
            case '*': case '.': case '\\':
            case '?': case '+': case '^': case '$':
            case '#': obytes[op++] = '\\'; break;
            case ' ': obytes[op++] = '\\'; obytes[op++] = ' '; continue;
            case '\t':obytes[op++] = '\\'; obytes[op++] = 't'; continue;
            case '\n':obytes[op++] = '\\'; obytes[op++] = 'n'; continue;
            case '\r':obytes[op++] = '\\'; obytes[op++] = 'r'; continue;
            case '\f':obytes[op++] = '\\'; obytes[op++] = 'f'; continue;
            }
            obytes[op++] = (byte)c;
        }

        result.setRealSize(op);
        return result;
    }

    static ByteList quote19(ByteList bs, boolean asciiOnly) {
        int p = bs.getBegin();
        int end = p + bs.getRealSize();
        byte[]bytes = bs.getUnsafeBytes();
        Encoding enc = bs.getEncoding();

        metaFound: do {
            while (p < end) {
                final int c;
                final int cl;
                if (enc.isAsciiCompatible()) {
                    cl = 1;
                    c = bytes[p] & 0xff;
                } else {
                    cl = StringSupport.preciseLength(enc, bytes, p, end);
                    c = enc.mbcToCode(bytes, p, end);
                }

                if (!Encoding.isAscii(c)) {
                    p += StringSupport.length(enc, bytes, p, end);
                    continue;
                }
                
                switch (c) {
                case '[': case ']': case '{': case '}':
                case '(': case ')': case '|': case '-':
                case '*': case '.': case '\\':
                case '?': case '+': case '^': case '$':
                case ' ': case '#':
                case '\t': case '\f': case '\n': case '\r':
                    break metaFound;
                }
                p += cl;
            }
            if (asciiOnly) {
                ByteList tmp = bs.shallowDup();
                tmp.setEncoding(USASCIIEncoding.INSTANCE);
                return tmp;
            }
            return bs;
        } while (false);

        ByteList result = new ByteList(end * 2);
        result.setEncoding(asciiOnly ? USASCIIEncoding.INSTANCE : bs.getEncoding());
        byte[]obytes = result.getUnsafeBytes();
        int op = p - bs.getBegin();
        System.arraycopy(bytes, bs.getBegin(), obytes, 0, op);

        while (p < end) {
            final int c;
            final int cl;
            if (enc.isAsciiCompatible()) {
                cl = 1;
                c = bytes[p] & 0xff;
            } else {
                cl = StringSupport.preciseLength(enc, bytes, p, end);
                c = enc.mbcToCode(bytes, p, end);
            }

            if (!Encoding.isAscii(c)) {
                int n = StringSupport.length(enc, bytes, p, end);
                while (n-- > 0) obytes[op++] = bytes[p++];
                continue;
            }
            p += cl;
            switch (c) {
            case '[': case ']': case '{': case '}':
            case '(': case ')': case '|': case '-':
            case '*': case '.': case '\\':
            case '?': case '+': case '^': case '$':
            case '#': 
                op += enc.codeToMbc('\\', obytes, op);
                break;
            case ' ':
                op += enc.codeToMbc('\\', obytes, op);
                op += enc.codeToMbc(' ', obytes, op);
                continue;
            case '\t':
                op += enc.codeToMbc('\\', obytes, op);
                op += enc.codeToMbc('t', obytes, op);
                continue;
            case '\n':
                op += enc.codeToMbc('\\', obytes, op);
                op += enc.codeToMbc('n', obytes, op);
                continue;
            case '\r':
                op += enc.codeToMbc('\\', obytes, op);
                op += enc.codeToMbc('r', obytes, op);
                continue;
            case '\f':
                op += enc.codeToMbc('\\', obytes, op);
                op += enc.codeToMbc('f', obytes, op);
                continue;
            }
            op += enc.codeToMbc(c, obytes, op);
        }

        result.setRealSize(op);
        return result;
    }
    
    /**
     * Variable arity version for compatibility. Not bound to a Ruby method.
     * @deprecated Use the versions with zero, one, or two args.
     */
    public static IRubyObject last_match_s(ThreadContext context, IRubyObject recv, IRubyObject[] args) {
        switch (args.length) {
        case 0:
            return last_match_s(context, recv);
        case 1:
            return last_match_s(context, recv, args[0]);
        default:
            Arity.raiseArgumentError(context.getRuntime(), args.length, 0, 1);
            return null; // not reached
        }
    }

    /** rb_reg_s_last_match / match_getter
    *
    */
    @JRubyMethod(name = "last_match", meta = true, reads = BACKREF)
    public static IRubyObject last_match_s(ThreadContext context, IRubyObject recv) {
        IRubyObject match = context.getCurrentScope().getBackRef(context.getRuntime());
        if (match instanceof RubyMatchData) ((RubyMatchData)match).use();
        return match;
    }

    /** rb_reg_s_last_match
    *
    */
    @JRubyMethod(name = "last_match", meta = true, reads = BACKREF)
    public static IRubyObject last_match_s(ThreadContext context, IRubyObject recv, IRubyObject nth) {
        IRubyObject match = context.getCurrentScope().getBackRef(context.getRuntime());
        if (match.isNil()) return match;
        return nth_match(((RubyMatchData)match).backrefNumber(nth), match);
    }

    /** rb_reg_s_union
    *
    */
    @JRubyMethod(name = "union", rest = true, meta = true, compat = CompatVersion.RUBY1_8)
    public static IRubyObject union(ThreadContext context, IRubyObject recv, IRubyObject[] args) {
        Ruby runtime = context.getRuntime();
        if (args.length == 0) return newRegexp(runtime, ByteList.create("(?!)"));

        IRubyObject[] realArgs = args;        
        if (args.length == 1) {
            // The power of the union of one!
            IRubyObject v = TypeConverter.convertToTypeWithCheck(args[0], runtime.getRegexp(), "to_regexp");
            if (!v.isNil()) return v;
            
            IRubyObject a = TypeConverter.convertToTypeWithCheck(args[0], runtime.getArray(), "to_ary");
            if (a.isNil()) return newRegexp(runtime, quote(context, recv, args).getByteList());

            RubyArray aa = (RubyArray)a;
            int len = aa.getLength();
            realArgs = new IRubyObject[len];
            for(int i = 0; i<len; i++) {
                realArgs[i] = aa.entry(i);
            }
        }

        KCode kcode = null;
        IRubyObject kcode_re = runtime.getNil();
        RubyString source = runtime.newString();

        for (int i = 0; i < realArgs.length; i++) {
            if (0 < i) source.cat((byte)'|');
            IRubyObject v = TypeConverter.convertToTypeWithCheck(realArgs[i], runtime.getRegexp(), "to_regexp");
            if (!v.isNil()) {
                if (!((RubyRegexp)v).isKCodeDefault()) {
                    if (kcode == null) { // First regexp of union sets kcode.
                        kcode_re = v;
                        kcode = ((RubyRegexp)v).options.getKCode();
                    } else if (((RubyRegexp)v).options.getKCode() != kcode) { // n kcode doesn't match first one
                        IRubyObject str1 = kcode_re.inspect();
                        IRubyObject str2 = v.inspect();
                        throw runtime.newArgumentError("mixed kcode " + str1 + " and " + str2);
                    }
                }
                v = ((RubyRegexp)v).to_s();
            } else {
                v = quote(context, recv, new IRubyObject[]{realArgs[i]});
            }
            source.append(v);
        }

        IRubyObject[] _args = new IRubyObject[3];        
        _args[0] = source;
        _args[1] = runtime.getNil();
        if (kcode == null) { // No elements in the array.
            _args[2] = runtime.getNil();
        } else if (kcode == KCode.NONE) {
            _args[2] = runtime.newString("n");
        } else if (kcode == KCode.EUC) {
            _args[2] = runtime.newString("e");
        } else if (kcode == KCode.SJIS) {
            _args[2] = runtime.newString("s");
        } else if (kcode == KCode.UTF8) {
            _args[2] = runtime.newString("u");
        }
        return recv.callMethod(context, "new", _args);
    }
    
   @JRubyMethod(name = "union", rest = true, meta = true, compat = CompatVersion.RUBY1_9)
   public static IRubyObject union19(ThreadContext context, IRubyObject recv, IRubyObject[] args) {
        Ruby runtime = context.getRuntime();
        if (args.length == 0) return newRegexp(runtime, ByteList.create("(?!)"));

        IRubyObject[] realArgs = args;        
        if (args.length == 1) {
            // The power of the union of one!
            IRubyObject v = TypeConverter.convertToTypeWithCheck(args[0], runtime.getRegexp(), "to_regexp");
            if (!v.isNil()) return v;
            
            IRubyObject a = TypeConverter.convertToTypeWithCheck(args[0], runtime.getArray(), "to_ary");
            if (a.isNil()) return newRegexp(runtime, ((RubyString) quote19(context, recv, args[0])).getByteList());

            RubyArray aa = (RubyArray)a;
            int len = aa.getLength();
            realArgs = new IRubyObject[len];
            for(int i = 0; i<len; i++) {
                realArgs[i] = aa.entry(i);
            }
        }

        boolean hasAsciiOnly = false;
        RubyString source = runtime.newString();
        Encoding hasAsciiCompatFixed = null;
        Encoding hasAsciiIncompat = null;        

        for (int i = 0; i < realArgs.length; i++) {
            if (0 < i) source.cat((byte)'|');
            
            Encoding enc;
            IRubyObject v = TypeConverter.convertToTypeWithCheck(realArgs[i], runtime.getRegexp(), "to_regexp");
            if (!v.isNil()) {
                RubyRegexp regex = (RubyRegexp) v;
                enc = regex.getEncoding();

                if (!enc.isAsciiCompatible()) {
                    if (hasAsciiIncompat == null) { // First regexp of union sets kcode.
                        hasAsciiIncompat = enc;
                    } else if (hasAsciiIncompat != enc) { // n kcode doesn't match first one
                        throw runtime.newArgumentError("incompatible encodings: " + hasAsciiIncompat + " and " + enc);
                    }
                } else if (regex.getOptions().isFixed()) {
                    if (hasAsciiCompatFixed == null) { // First regexp of union sets kcode.
                        hasAsciiCompatFixed = enc;
                    } else if (hasAsciiCompatFixed != enc) { // n kcode doesn't match first one
                        throw runtime.newArgumentError("incompatible encodings: " + hasAsciiCompatFixed + " and " + enc);
                    }
                } else {
                    hasAsciiOnly = true;
                }
                v = regex.to_s();
            } else {
                RubyString str = realArgs[i].convertToString();
                enc = str.getEncoding();
                
                if (!enc.isAsciiCompatible()) {
                    if (hasAsciiIncompat == null) { // First regexp of union sets kcode.
                        hasAsciiIncompat = enc;
                    } else if (hasAsciiIncompat != enc) { // n kcode doesn't match first one
                        throw runtime.newArgumentError("incompatible encodings: " + hasAsciiIncompat + " and " + enc);
                    }
                } else if (str.isAsciiOnly()) {
                    hasAsciiOnly = true;
                } else {
                    if (hasAsciiCompatFixed == null) { // First regexp of union sets kcode.
                        hasAsciiCompatFixed = enc;
                    } else if (hasAsciiCompatFixed != enc) { // n kcode doesn't match first one
                        throw runtime.newArgumentError("incompatible encodings: " + hasAsciiCompatFixed + " and " + enc);
                    }
                }                
                
                v = quote(context, recv, new IRubyObject[]{str});
            }
            
            if (hasAsciiIncompat != null) {
                if (hasAsciiOnly) {
                    throw runtime.newArgumentError("ASCII incompatible encoding: " + hasAsciiIncompat);
                }
                if (hasAsciiCompatFixed != null) {
                    throw runtime.newArgumentError("incompatible encodings: " + hasAsciiIncompat + " and " + hasAsciiCompatFixed);
                }
            }
            
            // Enebo: not sure why this is needed.
            if (i == 0) source.setEncoding(enc);

            source.append(v);
        }
        
        if (hasAsciiIncompat != null) {
            source.setEncoding(hasAsciiIncompat);
        } else if (hasAsciiCompatFixed != null) {
            source.setEncoding(hasAsciiCompatFixed);
        } else {
            source.setEncoding(ASCIIEncoding.INSTANCE);
        }

        return recv.callMethod(context, "new", new IRubyObject[] { source });
    }
    
    // rb_reg_raise
    private static void raiseRegexpError(Ruby runtime, ByteList bytes, Encoding enc, RegexpOptions options, String err) {
        throw runtime.newRegexpError(err + ": " + regexpDescription(runtime, bytes, enc, options));
    }

    // rb_reg_desc
    private static ByteList regexpDescription(Ruby runtime, ByteList bytes, Encoding enc, RegexpOptions options) {
        return regexpDescription(runtime, bytes.getUnsafeBytes(), bytes.getBegin(), bytes.getRealSize(), enc, options);
    }
    private static ByteList regexpDescription(Ruby runtime, byte[] bytes, int start, int len, Encoding enc, RegexpOptions options) {
        ByteList description = new ByteList();
        description.append((byte)'/');
        appendRegexpString(runtime, description, bytes, start, len, enc);
        description.append((byte)'/');
        appendOptions(description, options);
        return description;
    }

    // rb_enc_reg_raise
    private static void raiseRegexpError19(Ruby runtime, ByteList bytes, Encoding enc, RegexpOptions options, String err) {
        // TODO: we loose encoding information here, fix it
        throw runtime.newRegexpError(err + ": " + regexpDescription19(runtime, bytes, options, enc));
    }

    // rb_enc_reg_error_desc
    static ByteList regexpDescription19(Ruby runtime, ByteList bytes, RegexpOptions options, Encoding enc) {
        return regexpDescription19(runtime, bytes.getUnsafeBytes(), bytes.getBegin(), bytes.getRealSize(), options, enc);
    }
    private static ByteList regexpDescription19(Ruby runtime, byte[] s, int start, int len, RegexpOptions options, Encoding enc) {
        ByteList description = new ByteList();
        description.setEncoding(enc);
        description.append((byte)'/');
        appendRegexpString19(runtime, description, s, start, len, enc);
        description.append((byte)'/');
        appendOptions(description, options);
        if (options.isEncodingNone()) description.append((byte) 'n');
        return description; 
    }

    /** rb_reg_init_copy
     */
    @JRubyMethod(name = "initialize_copy", required = 1)
    @Override
    public IRubyObject initialize_copy(IRubyObject re) {
        if (this == re) return this;
        checkFrozen();

        if (getMetaClass().getRealClass() != re.getMetaClass().getRealClass()) {
            throw getRuntime().newTypeError("wrong argument type");
        }

        RubyRegexp regexp = (RubyRegexp)re;
        regexp.check();

        return getRuntime().is1_9() ? 
            initializeCommon19(regexp.str, regexp.str.getEncoding(), regexp.getOptions()) :
            initializeCommon(regexp.str, regexp.getOptions());
    }
    
    private int objectAsJoniOptions(IRubyObject arg) {
        if (arg instanceof RubyFixnum) return RubyNumeric.fix2int(arg);
        if (arg.isTrue()) return RE_OPTION_IGNORECASE;
        
        return 0;
    }

    @JRubyMethod(name = "initialize", visibility = Visibility.PRIVATE, compat = CompatVersion.RUBY1_8)
    public IRubyObject initialize_m(IRubyObject arg) {
        if (arg instanceof RubyRegexp) return initializeByRegexp((RubyRegexp)arg);
        return initializeCommon(arg.convertToString().getByteList(), new RegexpOptions());
    }
    
    @JRubyMethod(name = "initialize", visibility = Visibility.PRIVATE, compat = CompatVersion.RUBY1_8)
    public IRubyObject initialize_m(IRubyObject arg0, IRubyObject arg1) {
        if (arg0 instanceof RubyRegexp) {
            getRuntime().getWarnings().warn(ID.REGEXP_IGNORED_FLAGS, "flags ignored");            
            return initializeByRegexp((RubyRegexp)arg0);
        }
        
        options = RegexpOptions.fromJoniOptions(objectAsJoniOptions(arg1));
        return initializeCommon(arg0.convertToString().getByteList(), options);
    }
    
    @JRubyMethod(name = "initialize", visibility = Visibility.PRIVATE, compat = CompatVersion.RUBY1_8)
    public IRubyObject initialize_m(IRubyObject arg0, IRubyObject arg1, IRubyObject arg2) {
        if (arg0 instanceof RubyRegexp) {
            getRuntime().getWarnings().warn(ID.REGEXP_IGNORED_FLAGS, "flags and encoding ignored");            
            return initializeByRegexp((RubyRegexp)arg0);
        }
        
        int optionsInt = objectAsJoniOptions(arg1);
        options = RegexpOptions.fromJoniOptions(optionsInt);
        
        if (!arg2.isNil()) {
            ByteList kcodeBytes = arg2.convertToString().getByteList();
            char first = kcodeBytes.length() > 0 ? kcodeBytes.charAt(0) : 0;
            switch (first) {
            case 'n': case 'N':
                options.setExplicitKCode(KCode.NONE);
                break;
            case 'e': case 'E':
                options.setExplicitKCode(KCode.EUC);
                break;
            case 's': case 'S':
                options.setExplicitKCode(KCode.SJIS);
                break;
            case 'u': case 'U':
                options.setExplicitKCode(KCode.UTF8);
                break;
            default:
                break;
            }
        }
        return initializeCommon(arg0.convertToString().getByteList(), options);
    }

    private IRubyObject initializeByRegexp(RubyRegexp regexp) {
        regexp.check();

        return initializeCommon(regexp.str, regexp.options);
    }

    private RubyRegexp initializeCommon(ByteList bytes, RegexpOptions newOptions) {
        Ruby runtime = getRuntime();
        // Options needs a little more set up.
        if (!isTaint() && runtime.getSafeLevel() >= 4) throw runtime.newSecurityError("Insecure: can't modify regexp");
        checkFrozen();
        if (isLiteral()) throw runtime.newSecurityError("can't modify literal regexp");
        options = newOptions;
        pattern = getRegexpFromCache(runtime, bytes, options.getKCode().getEncoding(), options);
        str = bytes;
        return this;
    }

    @JRubyMethod(name = "initialize", visibility = Visibility.PRIVATE, compat = CompatVersion.RUBY1_9)
    public IRubyObject initialize_m19(IRubyObject arg) {
        if (arg instanceof RubyRegexp) return initializeByRegexp19((RubyRegexp)arg);
        return initializeCommon19(arg.convertToString(), new RegexpOptions());
    }

    @JRubyMethod(name = "initialize", visibility = Visibility.PRIVATE, compat = CompatVersion.RUBY1_9)
    public IRubyObject initialize_m19(IRubyObject arg0, IRubyObject arg1) {
        if (arg0 instanceof RubyRegexp) {
            getRuntime().getWarnings().warn(ID.REGEXP_IGNORED_FLAGS, "flags ignored");
            return initializeByRegexp19((RubyRegexp)arg0);
        }
        
        options = RegexpOptions.fromJoniOptions(objectAsJoniOptions(arg1));        
        return initializeCommon19(arg0.convertToString(), options);
    }

    @JRubyMethod(name = "initialize", visibility = Visibility.PRIVATE, compat = CompatVersion.RUBY1_9)
    public IRubyObject initialize_m19(IRubyObject arg0, IRubyObject arg1, IRubyObject arg2) {
        if (arg0 instanceof RubyRegexp) {
            getRuntime().getWarnings().warn(ID.REGEXP_IGNORED_FLAGS, "flags ignored");            
            return initializeByRegexp19((RubyRegexp)arg0);
        }
        int optionsInt = objectAsJoniOptions(arg1);
        RegexpOptions newOptions = RegexpOptions.fromJoniOptions(optionsInt);

        if (!arg2.isNil()) {
            ByteList kcodeBytes = arg2.convertToString().getByteList();
            if ((kcodeBytes.getRealSize() > 0 && kcodeBytes.getUnsafeBytes()[kcodeBytes.getBegin()] == 'n') ||
                (kcodeBytes.getRealSize() > 1 && kcodeBytes.getUnsafeBytes()[kcodeBytes.getBegin() + 1] == 'N')) {
                return initializeCommon19(arg0.convertToString().getByteList(), ASCIIEncoding.INSTANCE, newOptions);
            } else {
                getRuntime().getWarnings().warn("encoding option is ignored - " + kcodeBytes);
            }
        }
        return initializeCommon19(arg0.convertToString(), newOptions);
    }

    private IRubyObject initializeByRegexp19(RubyRegexp regexp) {
        regexp.check();
//        System.out.println("str: " + regexp.str + ", ENC: " + regexp.getEncoding() + ", OPT: " + regexp.getOptions());
//        System.out.println("KCODE: " + regexp.kcode);
        // Clone and toggle flags since this is no longer a literal regular expression
        // but it did come from one.
        RegexpOptions newOptions = (RegexpOptions) regexp.getOptions().clone();
        newOptions.setLiteral(false);
        return initializeCommon19(regexp.str, regexp.getEncoding(), newOptions);
    }

    // rb_reg_initialize_str
    private RubyRegexp initializeCommon19(RubyString str, RegexpOptions options) {
        ByteList bytes = str.getByteList();
        Encoding enc = bytes.getEncoding();
        if (options.isEncodingNone()) {
            if (enc != ASCIIEncoding.INSTANCE) {
                if (str.scanForCodeRange() != StringSupport.CR_7BIT) {
                    raiseRegexpError19(getRuntime(), bytes, enc, options, "/.../n has a non escaped non ASCII character in non ASCII-8BIT script");
                }
                enc = ASCIIEncoding.INSTANCE;
            }
        }
        return initializeCommon19(bytes, enc, options);
    }

    // rb_reg_initialize
    private RubyRegexp initializeCommon19(ByteList bytes, Encoding enc, RegexpOptions options) {
        Ruby runtime = getRuntime();
        this.options = options;
        
//        System.out.println("OPTIONS: " + options + ", STR.enc = " + enc);
        
        if (!isTaint() && runtime.getSafeLevel() >= 4) throw runtime.newSecurityError("Insecure: can't modify regexp");
        checkFrozen();
        // FIXME: Something unsets this bit, but we aren't...be more permissive until we figure this out
        //if (isLiteral()) throw runtime.newSecurityError("can't modify literal regexp");
        if (pattern != null) throw runtime.newTypeError("already initialized regexp");
        if (enc.isDummy()) raiseRegexpError19(runtime, bytes, enc, options, "can't make regexp with dummy encoding");
        
        Encoding[]fixedEnc = new Encoding[]{null};
        ByteList unescaped = preprocess(runtime, bytes, enc, fixedEnc, ErrorMode.RAISE);
        if (fixedEnc[0] != null) {
            if ((fixedEnc[0] != enc && options.isFixed()) ||
               (fixedEnc[0] != ASCIIEncoding.INSTANCE && options.isEncodingNone())) {
                   raiseRegexpError19(runtime, bytes, enc, options, "incompatible character encoding");
            }
            if (fixedEnc[0] != ASCIIEncoding.INSTANCE) {
                options.setFixed(true);
                enc = fixedEnc[0];
            }
        } else if (!options.isFixed()) {
            enc = USASCIIEncoding.INSTANCE;
        }

        if (fixedEnc[0] != null) options.setFixed(true);
        if (options.isEncodingNone()) setEncodingNone();

        pattern = getRegexpFromCache(runtime, unescaped, enc, options);
        str = bytes;
        return this;
    }

    @JRubyMethod(name = "kcode")
    public IRubyObject kcode(ThreadContext context) {
        Ruby runtime = context.getRuntime();        
        String kcodeName = options.getKCodeName();
        
        return kcodeName == null ? runtime.getNil() : runtime.newString(kcodeName);
    }

    @JRubyMethod(name = "hash")
    @Override
    public RubyFixnum hash() {
        check();
        int hash = pattern.getOptions();
        int len = str.getRealSize();
        int p = str.getBegin();
        byte[]bytes = str.getUnsafeBytes();
        while (len-- > 0) {
            hash = hash * 33 + bytes[p++];
        }
        return getRuntime().newFixnum(hash + (hash >> 5));
    }

    @JRubyMethod(name = {"==", "eql?"}, required = 1)
    @Override
    public IRubyObject op_equal(ThreadContext context, IRubyObject other) {
        if (this == other) return context.getRuntime().getTrue();
        if (!(other instanceof RubyRegexp)) return context.getRuntime().getFalse();
        RubyRegexp otherRegex = (RubyRegexp)other;
        
        check();
        otherRegex.check();
        
        return context.getRuntime().newBoolean(str.equal(otherRegex.str) && 
                getOptions().equals(otherRegex.options));
    }

    // FIXME: is this actually used in 1.8? It was 1.9-only before, but called the wrong search
    @JRubyMethod(name = "~", reads = {LASTLINE, BACKREF}, writes = BACKREF, compat = CompatVersion.RUBY1_8)
    public IRubyObject op_match2(ThreadContext context) {
        Ruby runtime = context.getRuntime();
        IRubyObject line = context.getCurrentScope().getLastLine(runtime);
        if (line instanceof RubyString) {
            int start = search(context, (RubyString)line, 0, false);
            if (start < 0) return runtime.getNil();
            return runtime.newFixnum(start);
        }
        context.getCurrentScope().setBackRef(runtime.getNil());
        return runtime.getNil();
    }

    @JRubyMethod(name = "~", reads = {LASTLINE, BACKREF}, writes = BACKREF, compat = CompatVersion.RUBY1_9)
    public IRubyObject op_match2_19(ThreadContext context) {
        Ruby runtime = context.getRuntime();
        IRubyObject line = context.getCurrentScope().getLastLine(runtime);
        if (line instanceof RubyString) {
            int start = search19(context, (RubyString)line, 0, false);
            if (start < 0) return runtime.getNil();
            return runtime.newFixnum(start);
        }
        context.getCurrentScope().setBackRef(runtime.getNil());
        return runtime.getNil();
    }

    /** rb_reg_eqq
     * 
     */
    @JRubyMethod(name = "===", required = 1, writes = BACKREF, compat = CompatVersion.RUBY1_8)
    public IRubyObject eqq(ThreadContext context, IRubyObject arg) {
        Ruby runtime = context.getRuntime();
        final RubyString str;
        if (arg instanceof RubyString) {
            str = (RubyString)arg;
        } else {
            IRubyObject tmp = arg.checkStringType();
            if (tmp.isNil()) {
                context.getCurrentScope().setBackRef(tmp);
                return runtime.getFalse();
            }
            str = (RubyString)tmp;
        }

        int start = search(context, str, 0, false);
        return (start < 0) ? runtime.getFalse() : runtime.getTrue();
    }

    @JRubyMethod(name = "===", required = 1, writes = BACKREF, compat = CompatVersion.RUBY1_9)
    public IRubyObject eqq19(ThreadContext context, IRubyObject arg) {
        Ruby runtime = context.getRuntime();
        arg = operandNoCheck(arg);
        if (arg.isNil()) {
            context.getCurrentScope().setBackRef(arg);
            return runtime.getFalse();
        }
        int start = search19(context, (RubyString)arg, 0, false);
        return (start < 0) ? runtime.getFalse() : runtime.getTrue();
    }
    
    /** rb_reg_match
     * 
     */
    @JRubyMethod(name = "=~", required = 1, writes = BACKREF, compat = CompatVersion.RUBY1_8)
    @Override
    public IRubyObject op_match(ThreadContext context, IRubyObject str) {
        Ruby runtime = context.getRuntime();
        if (str.isNil()) {
            context.getCurrentScope().setBackRef(str);
            return str;
        }
        int start = search(context, str.convertToString(), 0, false);
        if (start < 0) return runtime.getNil();
        return RubyFixnum.newFixnum(runtime, start);
    }
    
    @JRubyMethod(name = "=~", required = 1, writes = BACKREF, compat = CompatVersion.RUBY1_9)
    public IRubyObject op_match19(ThreadContext context, IRubyObject arg) {
        Ruby runtime = context.getRuntime();
        if (arg.isNil()) {
            context.getCurrentScope().setBackRef(arg);
            return arg;
        }
        RubyString str = operandCheck(runtime, arg);
        int pos = matchPos(context, str, 0);
        if (pos < 0) return runtime.getNil();
        return RubyFixnum.newFixnum(runtime, str.subLength(pos));
    }

    /** rb_reg_match_m
     * 
     */
    @JRubyMethod(name = "match", required = 1, reads = BACKREF, compat = CompatVersion.RUBY1_8)
    public IRubyObject match_m(ThreadContext context, IRubyObject str) {
        IRubyObject result = op_match(context, str);
        if (result.isNil()) return result;
        result = context.getCurrentScope().getBackRef(context.getRuntime());
        ((RubyMatchData)result).use();
        return result;
    }

    @JRubyMethod(name = "match", reads = BACKREF, compat = CompatVersion.RUBY1_9)
    public IRubyObject match_m19(ThreadContext context, IRubyObject str, Block block) {
        return match19Common(context, str, 0, block);
    }

    @JRubyMethod(name = "match", reads = BACKREF, compat = CompatVersion.RUBY1_9)
    public IRubyObject match_m19(ThreadContext context, IRubyObject str, IRubyObject pos, Block block) {
        return match19Common(context, str, RubyNumeric.num2int(pos), block);
    }

    private IRubyObject match19Common(ThreadContext context, IRubyObject arg, int pos, Block block) {
        DynamicScope scope = context.getCurrentScope();
        if (arg.isNil()) {
            scope.setBackRef(arg);
            return arg;
        }
        Ruby runtime = context.getRuntime();
        RubyString str = operandCheck(runtime, arg);

        if (matchPos(context, str, pos) < 0) {
            scope.setBackRef(runtime.getNil());
            return runtime.getNil();
        }

        IRubyObject backref = scope.getBackRef(runtime);
        ((RubyMatchData)backref).use();
        if (block.isGiven()) return block.yield(context, backref);
        return backref;
    }

    private int matchPos(ThreadContext context, RubyString str, int pos) {
        if (pos != 0) {
            if (pos < 0) {
                pos += str.strLength();
                if (pos < 0) return pos;
            }
            pos = adjustStartPos19(str, pos, false);
        }
        return search19(context, str, pos, false);
    }

    /** rb_reg_search
     */
    public final int search(ThreadContext context, RubyString str, int pos, boolean reverse) {
        check();
        DynamicScope scope = context.getCurrentScope();
        ByteList value = str.getByteList();

        if (pos <= value.getRealSize() && pos >= 0) {
            int realSize = value.getRealSize();
            int begin = value.getBegin();
            Matcher matcher = pattern.matcher(value.getUnsafeBytes(), begin, begin + realSize);

            int result = matcher.search(begin + pos, begin + (reverse ? 0 : realSize), Option.NONE);
            if (result >= 0) {
                updateBackRef(context, str, scope, matcher);
                return result;
            }
        }

        scope.setBackRef(context.getRuntime().getNil());
        return -1;
    }

    private RubyMatchData updateBackRef(ThreadContext context, RubyString str, DynamicScope scope, Matcher matcher) {
        RubyMatchData match = updateBackRef(context, str, scope, matcher, pattern);
        match.regexp = this;
        match.infectBy(this);
        return match;
    }

    static final RubyMatc
