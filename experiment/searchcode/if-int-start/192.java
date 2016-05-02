/*
 * Copyright (c) 2005-2007 Henri Sivonen
 * Copyright (c) 2007-2010 Mozilla Foundation
 * Portions of comments Copyright 2004-2010 Apple Computer, Inc., Mozilla 
 * Foundation, and Opera Software ASA.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

/*
 * The comments following this one that use the same comment syntax as this 
 * comment are quotes from the WHATWG HTML 5 spec as of 2 June 2007 
 * amended as of June 18 2008 and May 31 2010.
 * That document came with this statement:
 * "Š Copyright 2004-2010 Apple Computer, Inc., Mozilla Foundation, and 
 * Opera Software ASA. You are granted a license to use, reproduce and 
 * create derivative works of this document."
 */

package nu.validator.htmlparser.impl;

import nu.validator.htmlparser.annotation.Auto;
import nu.validator.htmlparser.annotation.CharacterName;
import nu.validator.htmlparser.annotation.Const;
import nu.validator.htmlparser.annotation.Inline;
import nu.validator.htmlparser.annotation.Local;
import nu.validator.htmlparser.annotation.NoLength;
import nu.validator.htmlparser.common.EncodingDeclarationHandler;
import nu.validator.htmlparser.common.Interner;
import nu.validator.htmlparser.common.TokenHandler;
import nu.validator.htmlparser.common.XmlViolationPolicy;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * An implementation of
 * http://www.whatwg.org/specs/web-apps/current-work/multipage/tokenization.html
 * 
 * This class implements the <code>Locator</code> interface. This is not an
 * incidental implementation detail: Users of this class are encouraged to make
 * use of the <code>Locator</code> nature.
 * 
 * By default, the tokenizer may report data that XML 1.0 bans. The tokenizer
 * can be configured to treat these conditions as fatal or to coerce the infoset
 * to something that XML 1.0 allows.
 * 
 * @version $Id$
 * @author hsivonen
 */
public class Tokenizer implements Locator {

    private static final int DATA_AND_RCDATA_MASK = ~1;

    public static final int DATA = 0;

    public static final int RCDATA = 1;

    public static final int SCRIPT_DATA = 2;

    public static final int RAWTEXT = 3;

    public static final int SCRIPT_DATA_ESCAPED = 4;

    public static final int ATTRIBUTE_VALUE_DOUBLE_QUOTED = 5;

    public static final int ATTRIBUTE_VALUE_SINGLE_QUOTED = 6;

    public static final int ATTRIBUTE_VALUE_UNQUOTED = 7;

    public static final int PLAINTEXT = 8;

    public static final int TAG_OPEN = 9;

    public static final int CLOSE_TAG_OPEN = 10;

    public static final int TAG_NAME = 11;

    public static final int BEFORE_ATTRIBUTE_NAME = 12;

    public static final int ATTRIBUTE_NAME = 13;

    public static final int AFTER_ATTRIBUTE_NAME = 14;

    public static final int BEFORE_ATTRIBUTE_VALUE = 15;

    public static final int AFTER_ATTRIBUTE_VALUE_QUOTED = 16;

    public static final int BOGUS_COMMENT = 17;

    public static final int MARKUP_DECLARATION_OPEN = 18;

    public static final int DOCTYPE = 19;

    public static final int BEFORE_DOCTYPE_NAME = 20;

    public static final int DOCTYPE_NAME = 21;

    public static final int AFTER_DOCTYPE_NAME = 22;

    public static final int BEFORE_DOCTYPE_PUBLIC_IDENTIFIER = 23;

    public static final int DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED = 24;

    public static final int DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED = 25;

    public static final int AFTER_DOCTYPE_PUBLIC_IDENTIFIER = 26;

    public static final int BEFORE_DOCTYPE_SYSTEM_IDENTIFIER = 27;

    public static final int DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED = 28;

    public static final int DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED = 29;

    public static final int AFTER_DOCTYPE_SYSTEM_IDENTIFIER = 30;

    public static final int BOGUS_DOCTYPE = 31;

    public static final int COMMENT_START = 32;

    public static final int COMMENT_START_DASH = 33;

    public static final int COMMENT = 34;

    public static final int COMMENT_END_DASH = 35;

    public static final int COMMENT_END = 36;

    public static final int COMMENT_END_BANG = 37;

    public static final int NON_DATA_END_TAG_NAME = 38;

    public static final int MARKUP_DECLARATION_HYPHEN = 39;

    public static final int MARKUP_DECLARATION_OCTYPE = 40;

    public static final int DOCTYPE_UBLIC = 41;

    public static final int DOCTYPE_YSTEM = 42;

    public static final int AFTER_DOCTYPE_PUBLIC_KEYWORD = 43;

    public static final int BETWEEN_DOCTYPE_PUBLIC_AND_SYSTEM_IDENTIFIERS = 44;

    public static final int AFTER_DOCTYPE_SYSTEM_KEYWORD = 45;

    public static final int CONSUME_CHARACTER_REFERENCE = 46;

    public static final int CONSUME_NCR = 47;

    public static final int CHARACTER_REFERENCE_TAIL = 48;

    public static final int HEX_NCR_LOOP = 49;

    public static final int DECIMAL_NRC_LOOP = 50;

    public static final int HANDLE_NCR_VALUE = 51;

    public static final int HANDLE_NCR_VALUE_RECONSUME = 52;

    public static final int CHARACTER_REFERENCE_HILO_LOOKUP = 53;

    public static final int SELF_CLOSING_START_TAG = 54;

    public static final int CDATA_START = 55;

    public static final int CDATA_SECTION = 56;

    public static final int CDATA_RSQB = 57;

    public static final int CDATA_RSQB_RSQB = 58;

    public static final int SCRIPT_DATA_LESS_THAN_SIGN = 59;

    public static final int SCRIPT_DATA_ESCAPE_START = 60;

    public static final int SCRIPT_DATA_ESCAPE_START_DASH = 61;

    public static final int SCRIPT_DATA_ESCAPED_DASH = 62;

    public static final int SCRIPT_DATA_ESCAPED_DASH_DASH = 63;

    public static final int BOGUS_COMMENT_HYPHEN = 64;

    public static final int RAWTEXT_RCDATA_LESS_THAN_SIGN = 65;

    public static final int SCRIPT_DATA_ESCAPED_LESS_THAN_SIGN = 66;

    public static final int SCRIPT_DATA_DOUBLE_ESCAPE_START = 67;

    public static final int SCRIPT_DATA_DOUBLE_ESCAPED = 68;

    public static final int SCRIPT_DATA_DOUBLE_ESCAPED_LESS_THAN_SIGN = 69;

    public static final int SCRIPT_DATA_DOUBLE_ESCAPED_DASH = 70;

    public static final int SCRIPT_DATA_DOUBLE_ESCAPED_DASH_DASH = 71;

    public static final int SCRIPT_DATA_DOUBLE_ESCAPE_END = 72;

    public static final int PROCESSING_INSTRUCTION = 73;

    public static final int PROCESSING_INSTRUCTION_QUESTION_MARK = 74;

    /**
     * Magic value for UTF-16 operations.
     */
    private static final int LEAD_OFFSET = (0xD800 - (0x10000 >> 10));

    /**
     * UTF-16 code unit array containing less than and greater than for emitting
     * those characters on certain parse errors.
     */
    private static final @NoLength char[] LT_GT = { '<', '>' };

    /**
     * UTF-16 code unit array containing less than and solidus for emitting
     * those characters on certain parse errors.
     */
    private static final @NoLength char[] LT_SOLIDUS = { '<', '/' };

    /**
     * UTF-16 code unit array containing ]] for emitting those characters on
     * state transitions.
     */
    private static final @NoLength char[] RSQB_RSQB = { ']', ']' };

    /**
     * Array version of U+FFFD.
     */
    private static final @NoLength char[] REPLACEMENT_CHARACTER = { '\uFFFD' };

    // [NOCPP[

    /**
     * Array version of space.
     */
    private static final @NoLength char[] SPACE = { ' ' };

    // ]NOCPP]

    /**
     * Array version of line feed.
     */
    private static final @NoLength char[] LF = { '\n' };

    /**
     * Buffer growth parameter.
     */
    private static final int BUFFER_GROW_BY = 1024;

    /**
     * "CDATA[" as <code>char[]</code>
     */
    private static final @NoLength char[] CDATA_LSQB = "CDATA[".toCharArray();

    /**
     * "octype" as <code>char[]</code>
     */
    private static final @NoLength char[] OCTYPE = "octype".toCharArray();

    /**
     * "ublic" as <code>char[]</code>
     */
    private static final @NoLength char[] UBLIC = "ublic".toCharArray();

    /**
     * "ystem" as <code>char[]</code>
     */
    private static final @NoLength char[] YSTEM = "ystem".toCharArray();

    private static final char[] TITLE_ARR = { 't', 'i', 't', 'l', 'e' };

    private static final char[] SCRIPT_ARR = { 's', 'c', 'r', 'i', 'p', 't' };

    private static final char[] STYLE_ARR = { 's', 't', 'y', 'l', 'e' };

    private static final char[] PLAINTEXT_ARR = { 'p', 'l', 'a', 'i', 'n', 't',
            'e', 'x', 't' };

    private static final char[] XMP_ARR = { 'x', 'm', 'p' };

    private static final char[] TEXTAREA_ARR = { 't', 'e', 'x', 't', 'a', 'r',
            'e', 'a' };

    private static final char[] IFRAME_ARR = { 'i', 'f', 'r', 'a', 'm', 'e' };

    private static final char[] NOEMBED_ARR = { 'n', 'o', 'e', 'm', 'b', 'e',
            'd' };

    private static final char[] NOSCRIPT_ARR = { 'n', 'o', 's', 'c', 'r', 'i',
            'p', 't' };

    private static final char[] NOFRAMES_ARR = { 'n', 'o', 'f', 'r', 'a', 'm',
            'e', 's' };

    /**
     * The token handler.
     */
    protected final TokenHandler tokenHandler;

    protected EncodingDeclarationHandler encodingDeclarationHandler;

    // [NOCPP[

    /**
     * The error handler.
     */
    protected ErrorHandler errorHandler;

    // ]NOCPP]

    /**
     * Whether the previous char read was CR.
     */
    protected boolean lastCR;

    protected int stateSave;

    private int returnStateSave;

    protected int index;

    private boolean forceQuirks;

    private char additional;

    private int entCol;

    private int firstCharKey;

    private int lo;

    private int hi;

    private int candidate;

    private int strBufMark;

    private int prevValue;

    protected int value;

    private boolean seenDigits;

    protected int cstart;

    /**
     * The SAX public id for the resource being tokenized. (Only passed to back
     * as part of locator data.)
     */
    private String publicId;

    /**
     * The SAX system id for the resource being tokenized. (Only passed to back
     * as part of locator data.)
     */
    private String systemId;

    /**
     * Buffer for short identifiers.
     */
    private @Auto char[] strBuf;

    /**
     * Number of significant <code>char</code>s in <code>strBuf</code>.
     */
    private int strBufLen;

    /**
     * <code>-1</code> to indicate that <code>strBuf</code> is used or otherwise
     * an offset to the main buffer.
     */
    // private int strBufOffset = -1;
    /**
     * Buffer for long strings.
     */
    private @Auto char[] longStrBuf;

    /**
     * Number of significant <code>char</code>s in <code>longStrBuf</code>.
     */
    private int longStrBufLen;

    /**
     * <code>-1</code> to indicate that <code>longStrBuf</code> is used or
     * otherwise an offset to the main buffer.
     */
    // private int longStrBufOffset = -1;

    /**
     * Buffer for expanding NCRs falling into the Basic Multilingual Plane.
     */
    private final @Auto char[] bmpChar;

    /**
     * Buffer for expanding astral NCRs.
     */
    private final @Auto char[] astralChar;

    /**
     * The element whose end tag closes the current CDATA or RCDATA element.
     */
    protected ElementName endTagExpectation = null;

    private char[] endTagExpectationAsArray; // not @Auto!

    /**
     * <code>true</code> if tokenizing an end tag
     */
    protected boolean endTag;

    /**
     * The current tag token name.
     */
    private ElementName tagName = null;

    /**
     * The current attribute name.
     */
    protected AttributeName attributeName = null;

    // [NOCPP[

    /**
     * Whether comment tokens are emitted.
     */
    private boolean wantsComments = false;

    /**
     * <code>true</code> when HTML4-specific additional errors are requested.
     */
    protected boolean html4;

    /**
     * Whether the stream is past the first 512 bytes.
     */
    private boolean metaBoundaryPassed;

    // ]NOCPP]

    /**
     * The name of the current doctype token.
     */
    private @Local String doctypeName;

    /**
     * The public id of the current doctype token.
     */
    private String publicIdentifier;

    /**
     * The system id of the current doctype token.
     */
    private String systemIdentifier;

    /**
     * The attribute holder.
     */
    private HtmlAttributes attributes;

    // [NOCPP[

    /**
     * The policy for vertical tab and form feed.
     */
    private XmlViolationPolicy contentSpacePolicy = XmlViolationPolicy.ALTER_INFOSET;

    /**
     * The policy for comments.
     */
    private XmlViolationPolicy commentPolicy = XmlViolationPolicy.ALTER_INFOSET;

    private XmlViolationPolicy xmlnsPolicy = XmlViolationPolicy.ALTER_INFOSET;

    private XmlViolationPolicy namePolicy = XmlViolationPolicy.ALTER_INFOSET;

    private boolean html4ModeCompatibleWithXhtml1Schemata;

    private final boolean newAttributesEachTime;

    // ]NOCPP]

    private int mappingLangToXmlLang;

    private boolean shouldSuspend;

    protected boolean confident;

    private int line;

    private Interner interner;

    // CPPONLY: private boolean viewingXmlSource;

    // [NOCPP[

    protected LocatorImpl ampersandLocation;

    public Tokenizer(TokenHandler tokenHandler, boolean newAttributesEachTime) {
        this.tokenHandler = tokenHandler;
        this.encodingDeclarationHandler = null;
        this.newAttributesEachTime = newAttributesEachTime;
        this.bmpChar = new char[1];
        this.astralChar = new char[2];
        this.tagName = null;
        this.attributeName = null;
        this.doctypeName = null;
        this.publicIdentifier = null;
        this.systemIdentifier = null;
        this.attributes = null;
    }

    // ]NOCPP]

    /**
     * The constructor.
     * 
     * @param tokenHandler
     *            the handler for receiving tokens
     */
    public Tokenizer(TokenHandler tokenHandler
    // CPPONLY: , boolean viewingXmlSource        
    ) {
        this.tokenHandler = tokenHandler;
        this.encodingDeclarationHandler = null;
        // [NOCPP[
        this.newAttributesEachTime = false;
        // ]NOCPP]
        this.bmpChar = new char[1];
        this.astralChar = new char[2];
        this.tagName = null;
        this.attributeName = null;
        this.doctypeName = null;
        this.publicIdentifier = null;
        this.systemIdentifier = null;
        this.attributes = null;
    // CPPONLY: this.viewingXmlSource = viewingXmlSource;
    }

    public void setInterner(Interner interner) {
        this.interner = interner;
    }

    public void initLocation(String newPublicId, String newSystemId) {
        this.systemId = newSystemId;
        this.publicId = newPublicId;

    }

    // CPPONLY: boolean isViewingXmlSource() {
    // CPPONLY: return viewingXmlSource;
    // CPPONLY: }

    // [NOCPP[

    /**
     * Returns the mappingLangToXmlLang.
     * 
     * @return the mappingLangToXmlLang
     */
    public boolean isMappingLangToXmlLang() {
        return mappingLangToXmlLang == AttributeName.HTML_LANG;
    }

    /**
     * Sets the mappingLangToXmlLang.
     * 
     * @param mappingLangToXmlLang
     *            the mappingLangToXmlLang to set
     */
    public void setMappingLangToXmlLang(boolean mappingLangToXmlLang) {
        this.mappingLangToXmlLang = mappingLangToXmlLang ? AttributeName.HTML_LANG
                : AttributeName.HTML;
    }

    /**
     * Sets the error handler.
     * 
     * @see org.xml.sax.XMLReader#setErrorHandler(org.xml.sax.ErrorHandler)
     */
    public void setErrorHandler(ErrorHandler eh) {
        this.errorHandler = eh;
    }

    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    /**
     * Sets the commentPolicy.
     * 
     * @param commentPolicy
     *            the commentPolicy to set
     */
    public void setCommentPolicy(XmlViolationPolicy commentPolicy) {
        this.commentPolicy = commentPolicy;
    }

    /**
     * Sets the contentNonXmlCharPolicy.
     * 
     * @param contentNonXmlCharPolicy
     *            the contentNonXmlCharPolicy to set
     */
    public void setContentNonXmlCharPolicy(
            XmlViolationPolicy contentNonXmlCharPolicy) {
        if (contentNonXmlCharPolicy != XmlViolationPolicy.ALLOW) {
            throw new IllegalArgumentException(
                    "Must use ErrorReportingTokenizer to set contentNonXmlCharPolicy to non-ALLOW.");
        }
    }

    /**
     * Sets the contentSpacePolicy.
     * 
     * @param contentSpacePolicy
     *            the contentSpacePolicy to set
     */
    public void setContentSpacePolicy(XmlViolationPolicy contentSpacePolicy) {
        this.contentSpacePolicy = contentSpacePolicy;
    }

    /**
     * Sets the xmlnsPolicy.
     * 
     * @param xmlnsPolicy
     *            the xmlnsPolicy to set
     */
    public void setXmlnsPolicy(XmlViolationPolicy xmlnsPolicy) {
        if (xmlnsPolicy == XmlViolationPolicy.FATAL) {
            throw new IllegalArgumentException("Can't use FATAL here.");
        }
        this.xmlnsPolicy = xmlnsPolicy;
    }

    public void setNamePolicy(XmlViolationPolicy namePolicy) {
        this.namePolicy = namePolicy;
    }

    /**
     * Sets the html4ModeCompatibleWithXhtml1Schemata.
     * 
     * @param html4ModeCompatibleWithXhtml1Schemata
     *            the html4ModeCompatibleWithXhtml1Schemata to set
     */
    public void setHtml4ModeCompatibleWithXhtml1Schemata(
            boolean html4ModeCompatibleWithXhtml1Schemata) {
        this.html4ModeCompatibleWithXhtml1Schemata = html4ModeCompatibleWithXhtml1Schemata;
    }

    // ]NOCPP]

    // For the token handler to call
    /**
     * Sets the tokenizer state and the associated element name. This should 
     * only ever used to put the tokenizer into one of the states that have
     * a special end tag expectation.
     * 
     * @param specialTokenizerState
     *            the tokenizer state to set
     * @param endTagExpectation
     *            the expected end tag for transitioning back to normal
     */
    public void setStateAndEndTagExpectation(int specialTokenizerState,
            @Local String endTagExpectation) {
        this.stateSave = specialTokenizerState;
        if (specialTokenizerState == Tokenizer.DATA) {
            return;
        }
        @Auto char[] asArray = Portability.newCharArrayFromLocal(endTagExpectation);
        this.endTagExpectation = ElementName.elementNameByBuffer(asArray, 0,
                asArray.length, interner);
        endTagExpectationToArray();
    }

    /**
     * Sets the tokenizer state and the associated element name. This should 
     * only ever used to put the tokenizer into one of the states that have
     * a special end tag expectation.
     * 
     * @param specialTokenizerState
     *            the tokenizer state to set
     * @param endTagExpectation
     *            the expected end tag for transitioning back to normal
     */
    public void setStateAndEndTagExpectation(int specialTokenizerState,
            ElementName endTagExpectation) {
        this.stateSave = specialTokenizerState;
        this.endTagExpectation = endTagExpectation;
        endTagExpectationToArray();
    }

    private void endTagExpectationToArray() {
        switch (endTagExpectation.getGroup()) {
            case TreeBuilder.TITLE:
                endTagExpectationAsArray = TITLE_ARR;
                return;
            case TreeBuilder.SCRIPT:
                endTagExpectationAsArray = SCRIPT_ARR;
                return;
            case TreeBuilder.STYLE:
                endTagExpectationAsArray = STYLE_ARR;
                return;
            case TreeBuilder.PLAINTEXT:
                endTagExpectationAsArray = PLAINTEXT_ARR;
                return;
            case TreeBuilder.XMP:
                endTagExpectationAsArray = XMP_ARR;
                return;
            case TreeBuilder.TEXTAREA:
                endTagExpectationAsArray = TEXTAREA_ARR;
                return;
            case TreeBuilder.IFRAME:
                endTagExpectationAsArray = IFRAME_ARR;
                return;
            case TreeBuilder.NOEMBED:
                endTagExpectationAsArray = NOEMBED_ARR;
                return;
            case TreeBuilder.NOSCRIPT:
                endTagExpectationAsArray = NOSCRIPT_ARR;
                return;
            case TreeBuilder.NOFRAMES:
                endTagExpectationAsArray = NOFRAMES_ARR;
                return;
            default:
                assert false: "Bad end tag expectation.";
                return;
        }
    }

    /**
     * For C++ use only.
     */
    public void setLineNumber(int line) {
        this.line = line;
    }

    // start Locator impl

    /**
     * @see org.xml.sax.Locator#getLineNumber()
     */
    @Inline public int getLineNumber() {
        return line;
    }

    // [NOCPP[

    /**
     * @see org.xml.sax.Locator#getColumnNumber()
     */
    @Inline public int getColumnNumber() {
        return -1;
    }

    /**
     * @see org.xml.sax.Locator#getPublicId()
     */
    public String getPublicId() {
        return publicId;
    }

    /**
     * @see org.xml.sax.Locator#getSystemId()
     */
    public String getSystemId() {
        return systemId;
    }

    // end Locator impl

    // end public API

    public void notifyAboutMetaBoundary() {
        metaBoundaryPassed = true;
    }

    void turnOnAdditionalHtml4Errors() {
        html4 = true;
    }

    // ]NOCPP]

    HtmlAttributes emptyAttributes() {
        // [NOCPP[
        if (newAttributesEachTime) {
            return new HtmlAttributes(mappingLangToXmlLang);
        } else {
            // ]NOCPP]
            return HtmlAttributes.EMPTY_ATTRIBUTES;
            // [NOCPP[
        }
        // ]NOCPP]
    }

    @Inline private void clearStrBufAndAppend(char c) {
        strBuf[0] = c;
        strBufLen = 1;
    }

    @Inline private void clearStrBuf() {
        strBufLen = 0;
    }

    /**
     * Appends to the smaller buffer.
     * 
     * @param c
     *            the UTF-16 code unit to append
     */
    private void appendStrBuf(char c) {
        if (strBufLen == strBuf.length) {
            char[] newBuf = new char[strBuf.length + Tokenizer.BUFFER_GROW_BY];
            System.arraycopy(strBuf, 0, newBuf, 0, strBuf.length);
            strBuf = newBuf;
        }
        strBuf[strBufLen++] = c;
    }

    /**
     * The smaller buffer as a String. Currently only used for error reporting.
     * 
     * <p>
     * C++ memory note: The return value must be released.
     * 
     * @return the smaller buffer as a string
     */
    protected String strBufToString() {
        return Portability.newStringFromBuffer(strBuf, 0, strBufLen);
    }

    /**
     * Returns the short buffer as a local name. The return value is released in
     * emitDoctypeToken().
     * 
     * @return the smaller buffer as local name
     */
    private void strBufToDoctypeName() {
        doctypeName = Portability.newLocalNameFromBuffer(strBuf, 0, strBufLen,
                interner);
    }

    /**
     * Emits the smaller buffer as character tokens.
     * 
     * @throws SAXException
     *             if the token handler threw
     */
    private void emitStrBuf() throws SAXException {
        if (strBufLen > 0) {
            tokenHandler.characters(strBuf, 0, strBufLen);
        }
    }

    @Inline private void clearLongStrBuf() {
        longStrBufLen = 0;
    }

    @Inline private void clearLongStrBufAndAppend(char c) {
        longStrBuf[0] = c;
        longStrBufLen = 1;
    }

    /**
     * Appends to the larger buffer.
     * 
     * @param c
     *            the UTF-16 code unit to append
     */
    private void appendLongStrBuf(char c) {
        if (longStrBufLen == longStrBuf.length) {
            char[] newBuf = new char[longStrBufLen + (longStrBufLen >> 1)];
            System.arraycopy(longStrBuf, 0, newBuf, 0, longStrBuf.length);
            longStrBuf = newBuf;
        }
        longStrBuf[longStrBufLen++] = c;
    }

    @Inline private void appendSecondHyphenToBogusComment() throws SAXException {
        // [NOCPP[
        switch (commentPolicy) {
            case ALTER_INFOSET:
                // detachLongStrBuf();
                appendLongStrBuf(' ');
                // FALLTHROUGH
            case ALLOW:
                warn("The document is not mappable to XML 1.0 due to two consecutive hyphens in a comment.");
                // ]NOCPP]
                appendLongStrBuf('-');
                // [NOCPP[
                break;
            case FATAL:
                fatal("The document is not mappable to XML 1.0 due to two consecutive hyphens in a comment.");
                break;
        }
        // ]NOCPP]
    }

    // [NOCPP[
    private void maybeAppendSpaceToBogusComment() throws SAXException {
        switch (commentPolicy) {
            case ALTER_INFOSET:
                // detachLongStrBuf();
                appendLongStrBuf(' ');
                // FALLTHROUGH
            case ALLOW:
                warn("The document is not mappable to XML 1.0 due to a trailing hyphen in a comment.");
                break;
            case FATAL:
                fatal("The document is not mappable to XML 1.0 due to a trailing hyphen in a comment.");
                break;
        }
    }

    // ]NOCPP]

    @Inline private void adjustDoubleHyphenAndAppendToLongStrBufAndErr(char c)
            throws SAXException {
        errConsecutiveHyphens();
        // [NOCPP[
        switch (commentPolicy) {
            case ALTER_INFOSET:
                // detachLongStrBuf();
                longStrBufLen--;
                appendLongStrBuf(' ');
                appendLongStrBuf('-');
                // FALLTHROUGH
            case ALLOW:
                warn("The document is not mappable to XML 1.0 due to two consecutive hyphens in a comment.");
                // ]NOCPP]
                appendLongStrBuf(c);
                // [NOCPP[
                break;
            case FATAL:
                fatal("The document is not mappable to XML 1.0 due to two consecutive hyphens in a comment.");
                break;
        }
        // ]NOCPP]
    }

    private void appendLongStrBuf(@NoLength char[] buffer, int offset, int length) {
        int reqLen = longStrBufLen + length;
        if (longStrBuf.length < reqLen) {
            char[] newBuf = new char[reqLen + (reqLen >> 1)];
            System.arraycopy(longStrBuf, 0, newBuf, 0, longStrBuf.length);
            longStrBuf = newBuf;
        }
        System.arraycopy(buffer, offset, longStrBuf, longStrBufLen, length);
        longStrBufLen = reqLen;
    }

    /**
     * Append the contents of the smaller buffer to the larger one.
     */
    @Inline private void appendStrBufToLongStrBuf() {
        appendLongStrBuf(strBuf, 0, strBufLen);
    }

    /**
     * The larger buffer as a string.
     * 
     * <p>
     * C++ memory note: The return value must be released.
     * 
     * @return the larger buffer as a string
     */
    private String longStrBufToString() {
        return Portability.newStringFromBuffer(longStrBuf, 0, longStrBufLen);
    }

    /**
     * Emits the current comment token.
     * 
     * @param pos
     *            TODO
     * 
     * @throws SAXException
     */
    private void emitComment(int provisionalHyphens, int pos)
            throws SAXException {
        // [NOCPP[
        if (wantsComments) {
            // ]NOCPP]
            // if (longStrBufOffset != -1) {
            // tokenHandler.comment(buf, longStrBufOffset, longStrBufLen
            // - provisionalHyphens);
            // } else {
            tokenHandler.comment(longStrBuf, 0, longStrBufLen
                    - provisionalHyphens);
            // }
            // [NOCPP[
        }
        // ]NOCPP]
        cstart = pos + 1;
    }

    /**
     * Flushes coalesced character tokens.
     * 
     * @param buf
     *            TODO
     * @param pos
     *            TODO
     * 
     * @throws SAXException
     */
    protected void flushChars(@NoLength char[] buf, int pos)
            throws SAXException {
        if (pos > cstart) {
            tokenHandler.characters(buf, cstart, pos - cstart);
        }
        cstart = Integer.MAX_VALUE;
    }

    /**
     * Reports an condition that would make the infoset incompatible with XML
     * 1.0 as fatal.
     * 
     * @param message
     *            the message
     * @throws SAXException
     * @throws SAXParseException
     */
    public void fatal(String message) throws SAXException {
        SAXParseException spe = new SAXParseException(message, this);
        if (errorHandler != null) {
            errorHandler.fatalError(spe);
        }
        throw spe;
    }

    /**
     * Reports a Parse Error.
     * 
     * @param message
     *            the message
     * @throws SAXException
     */
    public void err(String message) throws SAXException {
        if (errorHandler == null) {
            return;
        }
        SAXParseException spe = new SAXParseException(message, this);
        errorHandler.error(spe);
    }

    public void errTreeBuilder(String message) throws SAXException {
        ErrorHandler eh = null;
        if (tokenHandler instanceof TreeBuilder<?>) {
            TreeBuilder<?> treeBuilder = (TreeBuilder<?>) tokenHandler;
            eh = treeBuilder.getErrorHandler();
        }
        if (eh == null) {
            eh = errorHandler;
        }
        if (eh == null) {
            return;
        }
        SAXParseException spe = new SAXParseException(message, this);
        eh.error(spe);
    }

    /**
     * Reports a warning
     * 
     * @param message
     *            the message
     * @throws SAXException
     */
    public void warn(String message) throws SAXException {
        if (errorHandler == null) {
            return;
        }
        SAXParseException spe = new SAXParseException(message, this);
        errorHandler.warning(spe);
    }

    /**
     * 
     */
    private void resetAttributes() {
        // [NOCPP[
        if (newAttributesEachTime) {
            // ]NOCPP]
            attributes = null;
            // [NOCPP[
        } else {
            attributes.clear(mappingLangToXmlLang);
        }
        // ]NOCPP]
    }

    private void strBufToElementNameString() {
        // if (strBufOffset != -1) {
        // return ElementName.elementNameByBuffer(buf, strBufOffset, strBufLen);
        // } else {
        tagName = ElementName.elementNameByBuffer(strBuf, 0, strBufLen,
                interner);
        // }
    }

    private int emitCurrentTagToken(boolean selfClosing, int pos)
            throws SAXException {
        cstart = pos + 1;
        maybeErrSlashInEndTag(selfClosing);
        stateSave = Tokenizer.DATA;
        HtmlAttributes attrs = (attributes == null ? HtmlAttributes.EMPTY_ATTRIBUTES
                : attributes);
        if (endTag) {
            /*
             * When an end tag token is emitted, the content model flag must be
             * switched to the PCDATA state.
             */
            maybeErrAttributesOnEndTag(attrs);
            // CPPONLY: if (!viewingXmlSource) {
            tokenHandler.endTag(tagName);
            // CPPONLY: }
            Portability.delete(attributes);
        } else {
            // CPPONLY: if (viewingXmlSource) {
            // CPPONLY: Portability.delete(attributes);
            // CPPONLY: } else {
            tokenHandler.startTag(tagName, attrs, selfClosing);
            // CPPONLY: }
        }
        tagName.release();
        tagName = null;
        resetAttributes();
        /*
         * The token handler may have called setStateAndEndTagExpectation
         * and changed stateSave since the start of this method.
         */
        return stateSave;
    }

    private void attributeNameComplete() throws SAXException {
        // if (strBufOffset != -1) {
        // attributeName = AttributeName.nameByBuffer(buf, strBufOffset,
        // strBufLen, namePolicy != XmlViolationPolicy.ALLOW);
        // } else {
        attributeName = AttributeName.nameByBuffer(strBuf, 0, strBufLen
        // [NOCPP[
                , namePolicy != XmlViolationPolicy.ALLOW
                // ]NOCPP]
                , interner);
        // }

        if (attributes == null) {
            attributes = new HtmlAttributes(mappingLangToXmlLang);
        }

        /*
         * When the user agent leaves the attribute name state (and before
         * emitting the tag token, if appropriate), the complete attribute's
         * name must be compared to the other attributes on the same token; if
         * there is already an attribute on the token with the exact same name,
         * then this is a parse error and the new attribute must be dropped,
         * along with the value that gets associated with it (if any).
         */
        if (attributes.contains(attributeName)) {
            errDuplicateAttribute();
            attributeName.release();
            attributeName = null;
        }
    }

    private void addAttributeWithoutValue() throws SAXException {
        noteAttributeWithoutValue();

        // [NOCPP[
        if (metaBoundaryPassed && AttributeName.CHARSET == attributeName
                && ElementName.META == tagName) {
            err("A \u201Ccharset\u201D attribute on a \u201Cmeta\u201D element found after the first 512 bytes.");
        }
        // ]NOCPP]
        if (attributeName != null) {
            // [NOCPP[
            if (html4) {
                if (attributeName.isBoolean()) {
                    if (html4ModeCompatibleWithXhtml1Schemata) {
                        attributes.addAttribute(attributeName,
                                attributeName.getLocal(AttributeName.HTML),
                                xmlnsPolicy);
                    } else {
                        attributes.addAttribute(attributeName, "", xmlnsPolicy);
                    }
                } else {
                    if (AttributeName.BORDER != attributeName) {
                        err("Attribute value omitted for a non-boolean attribute. (HTML4-only error.)");
                        attributes.addAttribute(attributeName, "", xmlnsPolicy);
                    }
                }
            } else {
                if (AttributeName.SRC == attributeName
                        || AttributeName.HREF == attributeName) {
                    warn("Attribute \u201C"
                            + attributeName.getLocal(AttributeName.HTML)
                            + "\u201D without an explicit value seen. The attribute may be dropped by IE7.");
                }
                // ]NOCPP]
                attributes.addAttribute(attributeName,
                        Portability.newEmptyString()
                        // [NOCPP[
                        , xmlnsPolicy
                // ]NOCPP]
                );
                // [NOCPP[
            }
            // ]NOCPP]
            attributeName = null; // attributeName has been adopted by the
            // |attributes| object
        }
    }

    private void addAttributeWithValue() throws SAXException {
        // [NOCPP[
        if (metaBoundaryPassed && ElementName.META == tagName
                && AttributeName.CHARSET == attributeName) {
            err("A \u201Ccharset\u201D attribute on a \u201Cmeta\u201D element found after the first 512 bytes.");
        }
        // ]NOCPP]
        if (attributeName != null) {
            String val = longStrBufToString(); // Ownership transferred to
            // HtmlAttributes
            // CPPONLY: if (mViewSource) {
            // CPPONLY:   mViewSource.MaybeLinkifyAttributeValue(attributeName, val);
            // CPPONLY: }
            // [NOCPP[
            if (!endTag && html4 && html4ModeCompatibleWithXhtml1Schemata
                    && attributeName.isCaseFolded()) {
                val = newAsciiLowerCaseStringFromString(val);
            }
            // ]NOCPP]
            attributes.addAttribute(attributeName, val
            // [NOCPP[
                    , xmlnsPolicy
            // ]NOCPP]
            );
            attributeName = null; // attributeName has been adopted by the
            // |attributes| object
        }
    }

    // [NOCPP[

    private static String newAsciiLowerCaseStringFromString(String str) {
        if (str == null) {
            return null;
        }
        char[] buf = new char[str.length()];
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                c += 0x20;
            }
            buf[i] = c;
        }
        return new String(buf);
    }

    protected void startErrorReporting() throws SAXException {

    }

    // ]NOCPP]
    
    public void start() throws SAXException {
        initializeWithoutStarting();
        tokenHandler.startTokenization(this);
        // [NOCPP[
        startErrorReporting();
        // ]NOCPP]
    }

    public boolean tokenizeBuffer(UTF16Buffer buffer) throws SAXException {
        int state = stateSave;
        int returnState = returnStateSave;
        char c = '\u0000';
        shouldSuspend = false;
        lastCR = false;

        int start = buffer.getStart();
        /**
         * The index of the last <code>char</code> read from <code>buf</code>.
         */
        int pos = start - 1;

        /**
         * The index of the first <code>char</code> in <code>buf</code> that is
         * part of a coalesced run of character tokens or
         * <code>Integer.MAX_VALUE</code> if there is not a current run being
         * coalesced.
         */
        switch (state) {
            case DATA:
            case RCDATA:
            case SCRIPT_DATA:
            case PLAINTEXT:
            case RAWTEXT:
            case CDATA_SECTION:
            case SCRIPT_DATA_ESCAPED:
            case SCRIPT_DATA_ESCAPE_START:
            case SCRIPT_DATA_ESCAPE_START_DASH:
            case SCRIPT_DATA_ESCAPED_DASH:
            case SCRIPT_DATA_ESCAPED_DASH_DASH:
            case SCRIPT_DATA_DOUBLE_ESCAPE_START:
            case SCRIPT_DATA_DOUBLE_ESCAPED:
            case SCRIPT_DATA_DOUBLE_ESCAPED_LESS_THAN_SIGN:
            case SCRIPT_DATA_DOUBLE_ESCAPED_DASH:
            case SCRIPT_DATA_DOUBLE_ESCAPED_DASH_DASH:
            case SCRIPT_DATA_DOUBLE_ESCAPE_END:
                cstart = start;
                break;
            default:
                cstart = Integer.MAX_VALUE;
                break;
        }

        /**
         * The number of <code>char</code>s in <code>buf</code> that have
         * meaning. (The rest of the array is garbage and should not be
         * examined.)
         */
        // CPPONLY: if (mViewSource) {
        // CPPONLY:   mViewSource.SetBuffer(buffer);
        // CPPONLY:   pos = stateLoop(state, c, pos, buffer.getBuffer(), false, returnState, buffer.getEnd());
        // CPPONLY:   mViewSource.DropBuffer((pos == buffer.getEnd()) ? pos : pos + 1);
        // CPPONLY: } else {
        // CPPONLY:   pos = stateLoop(state, c, pos, buffer.getBuffer(), false, returnState, buffer.getEnd());
        // CPPONLY: }
        // [NOCPP[
        pos = stateLoop(state, c, pos, buffer.getBuffer(), false, returnState,
                buffer.getEnd());
        // ]NOCPP]
        if (pos == buffer.getEnd()) {
            // exiting due to end of buffer
            buffer.setStart(pos);
        } else {
            buffer.setStart(pos + 1);
        }
        return lastCR;
    }

    @SuppressWarnings("unused") private int stateLoop(int state, char c,
            int pos, @NoLength char[] buf, boolean reconsume, int returnState,
            int endPos) throws SAXException {
        /*
         * Idioms used in this code:
         * 
         * 
         * Consuming the next input character
         * 
         * To consume the next input character, the code does this: if (++pos ==
         * endPos) { break stateloop; } c = checkChar(buf, pos);
         * 
         * 
         * Staying in a state
         * 
         * When there's a state that the tokenizer may stay in over multiple
         * input characters, the state has a wrapper |for(;;)| loop and staying
         * in the state continues the loop.
         * 
         * 
         * Switching to another state
         * 
         * To switch to another state, the code sets the state variable to the
         * magic number of the new state. Then it either continues stateloop or
         * breaks out of the state's own wrapper loop if the target state is
         * right after the current state in source order. (This is a partial
         * workaround for Java's lack of goto.)
         * 
         * 
         * Reconsume support
         * 
         * The spec sometimes says that an input character is reconsumed in
         * another state. If a state can ever be entered so that an input
         * character can be reconsumed in it, the state's code starts with an
         * |if (reconsume)| that sets reconsume to false and skips over the
         * normal code for consuming a new character.
         * 
         * To reconsume the current character in another state, the code sets
         * |reconsume| to true and then switches to the other state.
         * 
         * 
         * Emitting character tokens
         * 
         * This method emits character tokens lazily. Whenever a new range of
         * character tokens starts, the field cstart must be set to the start
         * index of the range. The flushChars() method must be called at the end
         * of a range to flush it.
         * 
         * 
         * U+0000 handling
         * 
         * The various states have to handle the replacement of U+0000 with
         * U+FFFD. However, if U+0000 would be reconsumed in another state, the
         * replacement doesn't need to happen, because it's handled by the
         * reconsuming state.
         * 
         * 
         * LF handling
         * 
         * Every state needs to increment the line number upon LF unless the LF
         * gets reconsumed by another state which increments the line number.
         * 
         * 
         * CR handling
         * 
         * Every state needs to handle CR unless the CR gets reconsumed and is
         * handled by the reconsuming state. The CR needs to be handled as if it
         * were and LF, the lastCR field must be set to true and then this
         * method must return. The IO driver will then swallow the next
         * character if it is an LF to coalesce CRLF.
         */
        stateloop: for (;;) {
            switch (state) {
                case DATA:
                    dataloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        switch (c) {
                            case '&':
                                /*
                                 * U+0026 AMPERSAND (&) Switch to the character
                                 * reference in data state.
                                 */
                                flushChars(buf, pos);
                                clearStrBufAndAppend(c);
                                setAdditionalAndRememberAmpersandLocation('\u0000');
                                returnState = state;
                                state = transition(state, Tokenizer.CONSUME_CHARACTER_REFERENCE, reconsume, pos);
                                continue stateloop;
                            case '<':
                                /*
                                 * U+003C LESS-THAN SIGN (<) Switch to the tag
                                 * open state.
                                 */
                                flushChars(buf, pos);

                                state = transition(state, Tokenizer.TAG_OPEN, reconsume, pos);
                                break dataloop; // FALL THROUGH continue
                            // stateloop;
                            case '\u0000':
                                emitReplacementCharacter(buf, pos);
                                continue;
                            case '\r':
                                emitCarriageReturn(buf, pos);
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                            default:
                                /*
                                 * Anything else Emit the input character as a
                                 * character token.
                                 * 
                                 * Stay in the data state.
                                 */
                                continue;
                        }
                    }
                    // WARNING FALLTHRU CASE TRANSITION: DON'T REORDER
                case TAG_OPEN:
                    tagopenloop: for (;;) {
                        /*
                         * The behavior of this state depends on the content
                         * model flag.
                         */
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * If the content model flag is set to the PCDATA state
                         * Consume the next input character:
                         */
                        if (c >= 'A' && c <= 'Z') {
                            /*
                             * U+0041 LATIN CAPITAL LETTER A through to U+005A
                             * LATIN CAPITAL LETTER Z Create a new start tag
                             * token,
                             */
                            endTag = false;
                            /*
                             * set its tag name to the lowercase version of the
                             * input character (add 0x0020 to the character's
                             * code point),
                             */
                            clearStrBufAndAppend((char) (c + 0x20));
                            /* then switch to the tag name state. */
                            state = transition(state, Tokenizer.TAG_NAME, reconsume, pos);
                            /*
                             * (Don't emit the token yet; further details will
                             * be filled in before it is emitted.)
                             */
                            break tagopenloop;
                            // continue stateloop;
                        } else if (c >= 'a' && c <= 'z') {
                            /*
                             * U+0061 LATIN SMALL LETTER A through to U+007A
                             * LATIN SMALL LETTER Z Create a new start tag
                             * token,
                             */
                            endTag = false;
                            /*
                             * set its tag name to the input character,
                             */
                            clearStrBufAndAppend(c);
                            /* then switch to the tag name state. */
                            state = transition(state, Tokenizer.TAG_NAME, reconsume, pos);
                            /*
                             * (Don't emit the token yet; further details will
                             * be filled in before it is emitted.)
                             */
                            break tagopenloop;
                            // continue stateloop;
                        }
                        switch (c) {
                            case '!':
                                /*
                                 * U+0021 EXCLAMATION MARK (!) Switch to the
                                 * markup declaration open state.
                                 */
                                state = transition(state, Tokenizer.MARKUP_DECLARATION_OPEN, reconsume, pos);
                                continue stateloop;
                            case '/':
                                /*
                                 * U+002F SOLIDUS (/) Switch to the close tag
                                 * open state.
                                 */
                                state = transition(state, Tokenizer.CLOSE_TAG_OPEN, reconsume, pos);
                                continue stateloop;
                            case '?':
                                // CPPONLY: if (viewingXmlSource) {
                                // CPPONLY: state = transition(state,
                                // CPPONLY: Tokenizer.PROCESSING_INSTRUCTION,
                                // CPPONLY: reconsume,
                                // CPPONLY: pos);
                                // CPPONLY: continue stateloop;
                                // CPPONLY: }
                                /*
                                 * U+003F QUESTION MARK (?) Parse error.
                                 */
                                errProcessingInstruction();
                                /*
                                 * Switch to the bogus comment state.
                                 */
                                clearLongStrBufAndAppend(c);
                                state = transition(state, Tokenizer.BOGUS_COMMENT, reconsume, pos);
                                continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Parse error.
                                 */
                                errLtGt();
                                /*
                                 * Emit a U+003C LESS-THAN SIGN character token
                                 * and a U+003E GREATER-THAN SIGN character
                                 * token.
                                 */
                                tokenHandler.characters(Tokenizer.LT_GT, 0, 2);
                                /* Switch to the data state. */
                                cstart = pos + 1;
                                state = transition(state, Tokenizer.DATA, reconsume, pos);
                                continue stateloop;
                            default:
                                /*
                                 * Anything else Parse error.
                                 */
                                errBadCharAfterLt(c);
                                /*
                                 * Emit a U+003C LESS-THAN SIGN character token
                                 */
                                tokenHandler.characters(Tokenizer.LT_GT, 0, 1);
                                /*
                                 * and reconsume the current input character in
                                 * the data state.
                                 */
                                cstart = pos;
                                reconsume = true;
                                state = transition(state, Tokenizer.DATA, reconsume, pos);
                                continue stateloop;
                        }
                    }
                    // FALL THROUGH DON'T REORDER
                case TAG_NAME:
                    tagnameloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                strBufToElementNameString();
                                state = transition(state, Tokenizer.BEFORE_ATTRIBUTE_NAME, reconsume, pos);
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                            case ' ':
                            case '\t':
                            case '\u000C':
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE
                                 * Switch to the before attribute name state.
                                 */
                                strBufToElementNameString();
                                state = transition(state, Tokenizer.BEFORE_ATTRIBUTE_NAME, reconsume, pos);
                                break tagnameloop;
                            // continue stateloop;
                            case '/':
                                /*
                                 * U+002F SOLIDUS (/) Switch to the self-closing
                                 * start tag state.
                                 */
                                strBufToElementNameString();
                                state = transition(state, Tokenizer.SELF_CLOSING_START_TAG, reconsume, pos);
                                continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Emit the current
                                 * tag token.
                                 */
                                strBufToElementNameString();
                                state = transition(state, emitCurrentTagToken(false, pos), reconsume, pos);
                                if (shouldSuspend) {
                                    break stateloop;
                                }
                                /*
                                 * Switch to the data state.
                                 */
                                continue stateloop;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            default:
                                if (c >= 'A' && c <= 'Z') {
                                    /*
                                     * U+0041 LATIN CAPITAL LETTER A through to
                                     * U+005A LATIN CAPITAL LETTER Z Append the
                                     * lowercase version of the current input
                                     * character (add 0x0020 to the character's
                                     * code point) to the current tag token's
                                     * tag name.
                                     */
                                    c += 0x20;
                                }
                                /*
                                 * Anything else Append the current input
                                 * character to the current tag token's tag
                                 * name.
                                 */
                                appendStrBuf(c);
                                /*
                                 * Stay in the tag name state.
                                 */
                                continue;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case BEFORE_ATTRIBUTE_NAME:
                    beforeattributenameloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                            case ' ':
                            case '\t':
                            case '\u000C':
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE Stay
                                 * in the before attribute name state.
                                 */
                                continue;
                            case '/':
                                /*
                                 * U+002F SOLIDUS (/) Switch to the self-closing
                                 * start tag state.
                                 */
                                state = transition(state, Tokenizer.SELF_CLOSING_START_TAG, reconsume, pos);
                                continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Emit the current
                                 * tag token.
                                 */
                                state = transition(state, emitCurrentTagToken(false, pos), reconsume, pos);
                                if (shouldSuspend) {
                                    break stateloop;
                                }
                                /*
                                 * Switch to the data state.
                                 */
                                continue stateloop;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            case '\"':
                            case '\'':
                            case '<':
                            case '=':
                                /*
                                 * U+0022 QUOTATION MARK (") U+0027 APOSTROPHE
                                 * (') U+003C LESS-THAN SIGN (<) U+003D EQUALS
                                 * SIGN (=) Parse error.
                                 */
                                errBadCharBeforeAttributeNameOrNull(c);
                                /*
                                 * Treat it as per the "anything else" entry
                                 * below.
                                 */
                            default:
                                /*
                                 * Anything else Start a new attribute in the
                                 * current tag token.
                                 */
                                if (c >= 'A' && c <= 'Z') {
                                    /*
                                     * U+0041 LATIN CAPITAL LETTER A through to
                                     * U+005A LATIN CAPITAL LETTER Z Set that
                                     * attribute's name to the lowercase version
                                     * of the current input character (add
                                     * 0x0020 to the character's code point)
                                     */
                                    c += 0x20;
                                }
                                /*
                                 * Set that attribute's name to the current
                                 * input character,
                                 */
                                clearStrBufAndAppend(c);
                                /*
                                 * and its value to the empty string.
                                 */
                                // Will do later.
                                /*
                                 * Switch to the attribute name state.
                                 */
                                state = transition(state, Tokenizer.ATTRIBUTE_NAME, reconsume, pos);
                                break beforeattributenameloop;
                            // continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case ATTRIBUTE_NAME:
                    attributenameloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                attributeNameComplete();
                                state = transition(state, Tokenizer.AFTER_ATTRIBUTE_NAME, reconsume, pos);
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                            case ' ':
                            case '\t':
                            case '\u000C':
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE
                                 * Switch to the after attribute name state.
                                 */
                                attributeNameComplete();
                                state = transition(state, Tokenizer.AFTER_ATTRIBUTE_NAME, reconsume, pos);
                                continue stateloop;
                            case '/':
                                /*
                                 * U+002F SOLIDUS (/) Switch to the self-closing
                                 * start tag state.
                                 */
                                attributeNameComplete();
                                addAttributeWithoutValue();
                                state = transition(state, Tokenizer.SELF_CLOSING_START_TAG, reconsume, pos);
                                continue stateloop;
                            case '=':
                                /*
                                 * U+003D EQUALS SIGN (=) Switch to the before
                                 * attribute value state.
                                 */
                                attributeNameComplete();
                                state = transition(state, Tokenizer.BEFORE_ATTRIBUTE_VALUE, reconsume, pos);
                                break attributenameloop;
                            // continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Emit the current
                                 * tag token.
                                 */
                                attributeNameComplete();
                                addAttributeWithoutValue();
                                state = transition(state, emitCurrentTagToken(false, pos), reconsume, pos);
                                if (shouldSuspend) {
                                    break stateloop;
                                }
                                /*
                                 * Switch to the data state.
                                 */
                                continue stateloop;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            case '\"':
                            case '\'':
                            case '<':
      
