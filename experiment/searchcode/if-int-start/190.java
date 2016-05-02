/*
 * Copyright (c) 2007 Henri Sivonen
 * Copyright (c) 2007-2011 Mozilla Foundation
 * Portions of comments Copyright 2004-2008 Apple Computer, Inc., Mozilla 
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
 * comment are quotes from the WHATWG HTML 5 spec as of 27 June 2007 
 * amended as of June 28 2007.
 * That document came with this statement:
 * "Â Copyright 2004-2007 Apple Computer, Inc., Mozilla Foundation, and 
 * Opera Software ASA. You are granted a license to use, reproduce and 
 * create derivative works of this document."
 */

package nu.validator.htmlparser.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import nu.validator.htmlparser.annotation.Auto;
import nu.validator.htmlparser.annotation.Const;
import nu.validator.htmlparser.annotation.IdType;
import nu.validator.htmlparser.annotation.Inline;
import nu.validator.htmlparser.annotation.Literal;
import nu.validator.htmlparser.annotation.Local;
import nu.validator.htmlparser.annotation.NoLength;
import nu.validator.htmlparser.annotation.NsUri;
import nu.validator.htmlparser.common.DoctypeExpectation;
import nu.validator.htmlparser.common.DocumentMode;
import nu.validator.htmlparser.common.DocumentModeHandler;
import nu.validator.htmlparser.common.Interner;
import nu.validator.htmlparser.common.TokenHandler;
import nu.validator.htmlparser.common.XmlViolationPolicy;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class TreeBuilder<T> implements TokenHandler,
        TreeBuilderState<T> {
    
    /**
     * Array version of U+FFFD.
     */
    private static final @NoLength char[] REPLACEMENT_CHARACTER = { '\uFFFD' };
    
    // Start dispatch groups

    final static int OTHER = 0;

    final static int A = 1;

    final static int BASE = 2;

    final static int BODY = 3;

    final static int BR = 4;

    final static int BUTTON = 5;

    final static int CAPTION = 6;

    final static int COL = 7;

    final static int COLGROUP = 8;

    final static int FORM = 9;

    final static int FRAME = 10;

    final static int FRAMESET = 11;

    final static int IMAGE = 12;

    final static int INPUT = 13;

    final static int ISINDEX = 14;

    final static int LI = 15;

    final static int LINK_OR_BASEFONT_OR_BGSOUND = 16;

    final static int MATH = 17;

    final static int META = 18;

    final static int SVG = 19;

    final static int HEAD = 20;

    final static int HR = 22;

    final static int HTML = 23;

    final static int NOBR = 24;

    final static int NOFRAMES = 25;

    final static int NOSCRIPT = 26;

    final static int OPTGROUP = 27;

    final static int OPTION = 28;

    final static int P = 29;

    final static int PLAINTEXT = 30;

    final static int SCRIPT = 31;

    final static int SELECT = 32;

    final static int STYLE = 33;

    final static int TABLE = 34;

    final static int TEXTAREA = 35;

    final static int TITLE = 36;

    final static int TR = 37;

    final static int XMP = 38;

    final static int TBODY_OR_THEAD_OR_TFOOT = 39;

    final static int TD_OR_TH = 40;

    final static int DD_OR_DT = 41;

    final static int H1_OR_H2_OR_H3_OR_H4_OR_H5_OR_H6 = 42;

    final static int MARQUEE_OR_APPLET = 43;

    final static int PRE_OR_LISTING = 44;

    final static int B_OR_BIG_OR_CODE_OR_EM_OR_I_OR_S_OR_SMALL_OR_STRIKE_OR_STRONG_OR_TT_OR_U = 45;

    final static int UL_OR_OL_OR_DL = 46;

    final static int IFRAME = 47;

    final static int EMBED_OR_IMG = 48;

    final static int AREA_OR_WBR = 49;

    final static int DIV_OR_BLOCKQUOTE_OR_CENTER_OR_MENU = 50;

    final static int ADDRESS_OR_ARTICLE_OR_ASIDE_OR_DETAILS_OR_DIR_OR_FIGCAPTION_OR_FIGURE_OR_FOOTER_OR_HEADER_OR_HGROUP_OR_NAV_OR_SECTION_OR_SUMMARY = 51;

    final static int RUBY_OR_SPAN_OR_SUB_OR_SUP_OR_VAR = 52;

    final static int RT_OR_RP = 53;

    final static int COMMAND = 54;

    final static int PARAM_OR_SOURCE_OR_TRACK = 55;

    final static int MGLYPH_OR_MALIGNMARK = 56;

    final static int MI_MO_MN_MS_MTEXT = 57;

    final static int ANNOTATION_XML = 58;

    final static int FOREIGNOBJECT_OR_DESC = 59;

    final static int NOEMBED = 60;

    final static int FIELDSET = 61;

    final static int OUTPUT_OR_LABEL = 62;

    final static int OBJECT = 63;

    final static int FONT = 64;

    final static int KEYGEN = 65;

    final static int MENUITEM = 66;

    // start insertion modes

    private static final int INITIAL = 0;

    private static final int BEFORE_HTML = 1;

    private static final int BEFORE_HEAD = 2;

    private static final int IN_HEAD = 3;

    private static final int IN_HEAD_NOSCRIPT = 4;

    private static final int AFTER_HEAD = 5;

    private static final int IN_BODY = 6;

    private static final int IN_TABLE = 7;

    private static final int IN_CAPTION = 8;

    private static final int IN_COLUMN_GROUP = 9;

    private static final int IN_TABLE_BODY = 10;

    private static final int IN_ROW = 11;

    private static final int IN_CELL = 12;

    private static final int IN_SELECT = 13;

    private static final int IN_SELECT_IN_TABLE = 14;

    private static final int AFTER_BODY = 15;

    private static final int IN_FRAMESET = 16;

    private static final int AFTER_FRAMESET = 17;

    private static final int AFTER_AFTER_BODY = 18;

    private static final int AFTER_AFTER_FRAMESET = 19;

    private static final int TEXT = 20;

    private static final int FRAMESET_OK = 21;

    // start charset states

    private static final int CHARSET_INITIAL = 0;

    private static final int CHARSET_C = 1;

    private static final int CHARSET_H = 2;

    private static final int CHARSET_A = 3;

    private static final int CHARSET_R = 4;

    private static final int CHARSET_S = 5;

    private static final int CHARSET_E = 6;

    private static final int CHARSET_T = 7;

    private static final int CHARSET_EQUALS = 8;

    private static final int CHARSET_SINGLE_QUOTED = 9;

    private static final int CHARSET_DOUBLE_QUOTED = 10;

    private static final int CHARSET_UNQUOTED = 11;

    // end pseudo enums

    // [NOCPP[

    private final static String[] HTML4_PUBLIC_IDS = {
            "-//W3C//DTD HTML 4.0 Frameset//EN",
            "-//W3C//DTD HTML 4.0 Transitional//EN",
            "-//W3C//DTD HTML 4.0//EN", "-//W3C//DTD HTML 4.01 Frameset//EN",
            "-//W3C//DTD HTML 4.01 Transitional//EN",
            "-//W3C//DTD HTML 4.01//EN" };

    // ]NOCPP]

    @Literal private final static String[] QUIRKY_PUBLIC_IDS = {
            "+//silmaril//dtd html pro v0r11 19970101//",
            "-//advasoft ltd//dtd html 3.0 aswedit + extensions//",
            "-//as//dtd html 3.0 aswedit + extensions//",
            "-//ietf//dtd html 2.0 level 1//",
            "-//ietf//dtd html 2.0 level 2//",
            "-//ietf//dtd html 2.0 strict level 1//",
            "-//ietf//dtd html 2.0 strict level 2//",
            "-//ietf//dtd html 2.0 strict//",
            "-//ietf//dtd html 2.0//",
            "-//ietf//dtd html 2.1e//",
            "-//ietf//dtd html 3.0//",
            "-//ietf//dtd html 3.2 final//",
            "-//ietf//dtd html 3.2//",
            "-//ietf//dtd html 3//",
            "-//ietf//dtd html level 0//",
            "-//ietf//dtd html level 1//",
            "-//ietf//dtd html level 2//",
            "-//ietf//dtd html level 3//",
            "-//ietf//dtd html strict level 0//",
            "-//ietf//dtd html strict level 1//",
            "-//ietf//dtd html strict level 2//",
            "-//ietf//dtd html strict level 3//",
            "-//ietf//dtd html strict//",
            "-//ietf//dtd html//",
            "-//metrius//dtd metrius presentational//",
            "-//microsoft//dtd internet explorer 2.0 html strict//",
            "-//microsoft//dtd internet explorer 2.0 html//",
            "-//microsoft//dtd internet explorer 2.0 tables//",
            "-//microsoft//dtd internet explorer 3.0 html strict//",
            "-//microsoft//dtd internet explorer 3.0 html//",
            "-//microsoft//dtd internet explorer 3.0 tables//",
            "-//netscape comm. corp.//dtd html//",
            "-//netscape comm. corp.//dtd strict html//",
            "-//o'reilly and associates//dtd html 2.0//",
            "-//o'reilly and associates//dtd html extended 1.0//",
            "-//o'reilly and associates//dtd html extended relaxed 1.0//",
            "-//softquad software//dtd hotmetal pro 6.0::19990601::extensions to html 4.0//",
            "-//softquad//dtd hotmetal pro 4.0::19971010::extensions to html 4.0//",
            "-//spyglass//dtd html 2.0 extended//",
            "-//sq//dtd html 2.0 hotmetal + extensions//",
            "-//sun microsystems corp.//dtd hotjava html//",
            "-//sun microsystems corp.//dtd hotjava strict html//",
            "-//w3c//dtd html 3 1995-03-24//", "-//w3c//dtd html 3.2 draft//",
            "-//w3c//dtd html 3.2 final//", "-//w3c//dtd html 3.2//",
            "-//w3c//dtd html 3.2s draft//", "-//w3c//dtd html 4.0 frameset//",
            "-//w3c//dtd html 4.0 transitional//",
            "-//w3c//dtd html experimental 19960712//",
            "-//w3c//dtd html experimental 970421//", "-//w3c//dtd w3 html//",
            "-//w3o//dtd w3 html 3.0//", "-//webtechs//dtd mozilla html 2.0//",
            "-//webtechs//dtd mozilla html//" };

    private static final int NOT_FOUND_ON_STACK = Integer.MAX_VALUE;

    // [NOCPP[

    private static final @Local String HTML_LOCAL = "html";
    
    // ]NOCPP]

    private int mode = INITIAL;

    private int originalMode = INITIAL;
    
    /**
     * Used only when moving back to IN_BODY.
     */
    private boolean framesetOk = true;

    protected Tokenizer tokenizer;

    // [NOCPP[

    protected ErrorHandler errorHandler;

    private DocumentModeHandler documentModeHandler;

    private DoctypeExpectation doctypeExpectation = DoctypeExpectation.HTML;

    private LocatorImpl firstCommentLocation;
    
    // ]NOCPP]

    private boolean scriptingEnabled = false;

    private boolean needToDropLF;

    // [NOCPP[

    private boolean wantingComments;

    // ]NOCPP]

    private boolean fragment;

    private @Local String contextName;

    private @NsUri String contextNamespace;

    private T contextNode;

    private @Auto StackNode<T>[] stack;

    private int currentPtr = -1;

    private @Auto StackNode<T>[] listOfActiveFormattingElements;

    private int listPtr = -1;

    private T formPointer;

    private T headPointer;

    /**
     * Used to work around Gecko limitations. Not used in Java.
     */
    private T deepTreeSurrogateParent;

    protected @Auto char[] charBuffer;

    protected int charBufferLen = 0;

    private boolean quirks = false;

    // [NOCPP[

    private boolean reportingDoctype = true;

    private XmlViolationPolicy namePolicy = XmlViolationPolicy.ALTER_INFOSET;

    private final Map<String, LocatorImpl> idLocations = new HashMap<String, LocatorImpl>();

    private boolean html4;

    // ]NOCPP]

    protected TreeBuilder() {
        fragment = false;
    }

    /**
     * Reports an condition that would make the infoset incompatible with XML
     * 1.0 as fatal.
     * 
     * @throws SAXException
     * @throws SAXParseException
     */
    protected void fatal() throws SAXException {
    }

    // [NOCPP[

    protected final void fatal(Exception e) throws SAXException {
        SAXParseException spe = new SAXParseException(e.getMessage(),
                tokenizer, e);
        if (errorHandler != null) {
            errorHandler.fatalError(spe);
        }
        throw spe;
    }

    final void fatal(String s) throws SAXException {
        SAXParseException spe = new SAXParseException(s, tokenizer);
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
    final void err(String message) throws SAXException {
        if (errorHandler == null) {
            return;
        }
        errNoCheck(message);
    }
    
    /**
     * Reports a Parse Error without checking if an error handler is present.
     * 
     * @param message
     *            the message
     * @throws SAXException
     */
    final void errNoCheck(String message) throws SAXException {
        SAXParseException spe = new SAXParseException(message, tokenizer);
        errorHandler.error(spe);
    }

    private void errListUnclosedStartTags(int eltPos) throws SAXException {
        if (currentPtr != -1) {
            for (int i = currentPtr; i > eltPos; i--) {
                reportUnclosedElementNameAndLocation(i);
            }
        }
    }

    /**
     * Reports the name and location of an unclosed element.
     * 
     * @throws SAXException
     */
    private final void reportUnclosedElementNameAndLocation(int pos) throws SAXException {
        StackNode<T> node = stack[pos];
        if (node.isOptionalEndTag()) {
            return;
        }
        TaintableLocatorImpl locator = node.getLocator();
        if (locator.isTainted()) {
            return;
        }
        locator.markTainted();
        SAXParseException spe = new SAXParseException(
                "Unclosed element \u201C" + node.popName + "\u201D.", locator);
        errorHandler.error(spe);
    }

    /**
     * Reports a warning
     * 
     * @param message
     *            the message
     * @throws SAXException
     */
    final void warn(String message) throws SAXException {
        if (errorHandler == null) {
            return;
        }
        SAXParseException spe = new SAXParseException(message, tokenizer);
        errorHandler.warning(spe);
    }

    /**
     * Reports a warning with an explicit locator
     * 
     * @param message
     *            the message
     * @throws SAXException
     */
    final void warn(String message, Locator locator) throws SAXException {
        if (errorHandler == null) {
            return;
        }
        SAXParseException spe = new SAXParseException(message, locator);
        errorHandler.warning(spe);
    }

    // ]NOCPP]
    
    @SuppressWarnings("unchecked") public final void startTokenization(Tokenizer self) throws SAXException {
        tokenizer = self;
        stack = new StackNode[64];
        listOfActiveFormattingElements = new StackNode[64];
        needToDropLF = false;
        originalMode = INITIAL;
        currentPtr = -1;
        listPtr = -1;
        formPointer = null;
        headPointer = null;
        deepTreeSurrogateParent = null;
        // [NOCPP[
        html4 = false;
        idLocations.clear();
        wantingComments = wantsComments();
        firstCommentLocation = null;
        // ]NOCPP]
        start(fragment);
        charBufferLen = 0;
        charBuffer = new char[1024];
        framesetOk = true;
        if (fragment) {
            T elt;
            if (contextNode != null) {
                elt = contextNode;
            } else {
                elt = createHtmlElementSetAsRoot(tokenizer.emptyAttributes());
            }
            StackNode<T> node = new StackNode<T>(ElementName.HTML, elt
            // [NOCPP[
                    , errorHandler == null ? null : new TaintableLocatorImpl(tokenizer)
            // ]NOCPP]
            );
            currentPtr++;
            stack[currentPtr] = node;
            resetTheInsertionMode();
            if ("title" == contextName || "textarea" == contextName) {
                tokenizer.setStateAndEndTagExpectation(Tokenizer.RCDATA, contextName);
            } else if ("style" == contextName || "xmp" == contextName
                    || "iframe" == contextName || "noembed" == contextName
                    || "noframes" == contextName
                    || (scriptingEnabled && "noscript" == contextName)) {
                tokenizer.setStateAndEndTagExpectation(Tokenizer.RAWTEXT, contextName);
            } else if ("plaintext" == contextName) {
                tokenizer.setStateAndEndTagExpectation(Tokenizer.PLAINTEXT, contextName);
            } else if ("script" == contextName) {
                tokenizer.setStateAndEndTagExpectation(Tokenizer.SCRIPT_DATA,
                        contextName);
            } else {
                tokenizer.setStateAndEndTagExpectation(Tokenizer.DATA, contextName);
            }
            contextName = null;
            contextNode = null;
        } else {
            mode = INITIAL;
            // If we are viewing XML source, put a foreign element permanently
            // on the stack so that cdataSectionAllowed() returns true.
            // CPPONLY: if (tokenizer.isViewingXmlSource()) {
            // CPPONLY: T elt = createElement("http://www.w3.org/2000/svg",
            // CPPONLY: "svg",
            // CPPONLY: tokenizer.emptyAttributes());
            // CPPONLY: StackNode<T> node = new StackNode<T>(ElementName.SVG,
            // CPPONLY: "svg",
            // CPPONLY: elt);
            // CPPONLY: currentPtr++;
            // CPPONLY: stack[currentPtr] = node;
            // CPPONLY: }
        }
    }

    public final void doctype(@Local String name, String publicIdentifier,
            String systemIdentifier, boolean forceQuirks) throws SAXException {
        needToDropLF = false;
        if (!isInForeign()) {
            switch (mode) {
                case INITIAL:
                    // [NOCPP[
                    if (reportingDoctype) {
                        // ]NOCPP]
                        String emptyString = Portability.newEmptyString();
                        appendDoctypeToDocument(name == null ? "" : name,
                                publicIdentifier == null ? emptyString
                                        : publicIdentifier,
                                systemIdentifier == null ? emptyString
                                        : systemIdentifier);
                        Portability.releaseString(emptyString);
                        // [NOCPP[
                    }
                    switch (doctypeExpectation) {
                        case HTML:
                            // ]NOCPP]
                            if (isQuirky(name, publicIdentifier,
                                    systemIdentifier, forceQuirks)) {
                                errQuirkyDoctype();
                                documentModeInternal(DocumentMode.QUIRKS_MODE,
                                        publicIdentifier, systemIdentifier,
                                        false);
                            } else if (isAlmostStandards(publicIdentifier,
                                    systemIdentifier)) {
                                // [NOCPP[
                                if (firstCommentLocation != null) {
                                    warn("Comments seen before doctype. Internet Explorer will go into the quirks mode.", firstCommentLocation);
                                }
                                // ]NOCPP]
                                errAlmostStandardsDoctype();
                                documentModeInternal(
                                        DocumentMode.ALMOST_STANDARDS_MODE,
                                        publicIdentifier, systemIdentifier,
                                        false);
                            } else {
                                // [NOCPP[
                                if (firstCommentLocation != null) {
                                    warn("Comments seen before doctype. Internet Explorer will go into the quirks mode.", firstCommentLocation);
                                }
                                if ((Portability.literalEqualsString(
                                        "-//W3C//DTD HTML 4.0//EN",
                                        publicIdentifier) && (systemIdentifier == null || Portability.literalEqualsString(
                                        "http://www.w3.org/TR/REC-html40/strict.dtd",
                                        systemIdentifier)))
                                        || (Portability.literalEqualsString(
                                                "-//W3C//DTD HTML 4.01//EN",
                                                publicIdentifier) && (systemIdentifier == null || Portability.literalEqualsString(
                                                "http://www.w3.org/TR/html4/strict.dtd",
                                                systemIdentifier)))
                                        || (Portability.literalEqualsString(
                                                "-//W3C//DTD XHTML 1.0 Strict//EN",
                                                publicIdentifier) && Portability.literalEqualsString(
                                                "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd",
                                                systemIdentifier))
                                        || (Portability.literalEqualsString(
                                                "-//W3C//DTD XHTML 1.1//EN",
                                                publicIdentifier) && Portability.literalEqualsString(
                                                "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd",
                                                systemIdentifier))

                                ) {
                                    warn("Obsolete doctype. Expected \u201C<!DOCTYPE html>\u201D.");
                                } else if (!((systemIdentifier == null || Portability.literalEqualsString(
                                        "about:legacy-compat", systemIdentifier)) && publicIdentifier == null)) {
                                    err("Legacy doctype. Expected \u201C<!DOCTYPE html>\u201D.");
                                }
                                // ]NOCPP]
                                documentModeInternal(
                                        DocumentMode.STANDARDS_MODE,
                                        publicIdentifier, systemIdentifier,
                                        false);
                            }
                            // [NOCPP[
                            break;
                        case HTML401_STRICT:
                            html4 = true;
                            tokenizer.turnOnAdditionalHtml4Errors();
                            if (isQuirky(name, publicIdentifier,
                                    systemIdentifier, forceQuirks)) {
                                err("Quirky doctype. Expected \u201C<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\u201D.");
                                documentModeInternal(DocumentMode.QUIRKS_MODE,
                                        publicIdentifier, systemIdentifier,
                                        true);
                            } else if (isAlmostStandards(publicIdentifier,
                                    systemIdentifier)) {
                                if (firstCommentLocation != null) {
                                    warn("Comments seen before doctype. Internet Explorer will go into the quirks mode.", firstCommentLocation);
                                }
                                err("Almost standards mode doctype. Expected \u201C<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\u201D.");
                                documentModeInternal(
                                        DocumentMode.ALMOST_STANDARDS_MODE,
                                        publicIdentifier, systemIdentifier,
                                        true);
                            } else {
                                if (firstCommentLocation != null) {
                                    warn("Comments seen before doctype. Internet Explorer will go into the quirks mode.", firstCommentLocation);
                                }
                                if ("-//W3C//DTD HTML 4.01//EN".equals(publicIdentifier)) {
                                    if (!"http://www.w3.org/TR/html4/strict.dtd".equals(systemIdentifier)) {
                                        warn("The doctype did not contain the system identifier prescribed by the HTML 4.01 specification. Expected \u201C<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\u201D.");
                                    }
                                } else {
                                    err("The doctype was not the HTML 4.01 Strict doctype. Expected \u201C<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\u201D.");
                                }
                                documentModeInternal(
                                        DocumentMode.STANDARDS_MODE,
                                        publicIdentifier, systemIdentifier,
                                        true);
                            }
                            break;
                        case HTML401_TRANSITIONAL:
                            html4 = true;
                            tokenizer.turnOnAdditionalHtml4Errors();
                            if (isQuirky(name, publicIdentifier,
                                    systemIdentifier, forceQuirks)) {
                                err("Quirky doctype. Expected \u201C<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\u201D.");
                                documentModeInternal(DocumentMode.QUIRKS_MODE,
                                        publicIdentifier, systemIdentifier,
                                        true);
                            } else if (isAlmostStandards(publicIdentifier,
                                    systemIdentifier)) {
                                if (firstCommentLocation != null) {
                                    warn("Comments seen before doctype. Internet Explorer will go into the quirks mode.", firstCommentLocation);
                                }
                                if ("-//W3C//DTD HTML 4.01 Transitional//EN".equals(publicIdentifier)
                                        && systemIdentifier != null) {
                                    if (!"http://www.w3.org/TR/html4/loose.dtd".equals(systemIdentifier)) {
                                        warn("The doctype did not contain the system identifier prescribed by the HTML 4.01 specification. Expected \u201C<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\u201D.");
                                    }
                                } else {
                                    err("The doctype was not a non-quirky HTML 4.01 Transitional doctype. Expected \u201C<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\u201D.");
                                }
                                documentModeInternal(
                                        DocumentMode.ALMOST_STANDARDS_MODE,
                                        publicIdentifier, systemIdentifier,
                                        true);
                            } else {
                                if (firstCommentLocation != null) {
                                    warn("Comments seen before doctype. Internet Explorer will go into the quirks mode.", firstCommentLocation);
                                }
                                err("The doctype was not the HTML 4.01 Transitional doctype. Expected \u201C<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\u201D.");
                                documentModeInternal(
                                        DocumentMode.STANDARDS_MODE,
                                        publicIdentifier, systemIdentifier,
                                        true);
                            }
                            break;
                        case AUTO:
                            html4 = isHtml4Doctype(publicIdentifier);
                            if (html4) {
                                tokenizer.turnOnAdditionalHtml4Errors();
                            }
                            if (isQuirky(name, publicIdentifier,
                                    systemIdentifier, forceQuirks)) {
                                err("Quirky doctype. Expected e.g. \u201C<!DOCTYPE html>\u201D.");
                                documentModeInternal(DocumentMode.QUIRKS_MODE,
                                        publicIdentifier, systemIdentifier,
                                        html4);
                            } else if (isAlmostStandards(publicIdentifier,
                                    systemIdentifier)) {
                                if (firstCommentLocation != null) {
                                    warn("Comments seen before doctype. Internet Explorer will go into the quirks mode.", firstCommentLocation);
                                }
                                if ("-//W3C//DTD HTML 4.01 Transitional//EN".equals(publicIdentifier)) {
                                    if (!"http://www.w3.org/TR/html4/loose.dtd".equals(systemIdentifier)) {
                                        warn("The doctype did not contain the system identifier prescribed by the HTML 4.01 specification. Expected \u201C<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\u201D.");
                                    }
                                } else {
                                    err("Almost standards mode doctype. Expected e.g. \u201C<!DOCTYPE html>\u201D.");
                                }
                                documentModeInternal(
                                        DocumentMode.ALMOST_STANDARDS_MODE,
                                        publicIdentifier, systemIdentifier,
                                        html4);
                            } else {
                                if (firstCommentLocation != null) {
                                    warn("Comments seen before doctype. Internet Explorer will go into the quirks mode.", firstCommentLocation);
                                }
                                if ("-//W3C//DTD HTML 4.01//EN".equals(publicIdentifier)) {
                                    if (!"http://www.w3.org/TR/html4/strict.dtd".equals(systemIdentifier)) {
                                        warn("The doctype did not contain the system identifier prescribed by the HTML 4.01 specification. Expected \u201C<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\u201D.");
                                    }
                                } else {
                                    if (!(publicIdentifier == null && systemIdentifier == null)) {
                                        err("Legacy doctype. Expected e.g. \u201C<!DOCTYPE html>\u201D.");
                                    }
                                }
                                documentModeInternal(
                                        DocumentMode.STANDARDS_MODE,
                                        publicIdentifier, systemIdentifier,
                                        html4);
                            }
                            break;
                        case NO_DOCTYPE_ERRORS:
                            if (isQuirky(name, publicIdentifier,
                                    systemIdentifier, forceQuirks)) {
                                documentModeInternal(DocumentMode.QUIRKS_MODE,
                                        publicIdentifier, systemIdentifier,
                                        false);
                            } else if (isAlmostStandards(publicIdentifier,
                                    systemIdentifier)) {
                                documentModeInternal(
                                        DocumentMode.ALMOST_STANDARDS_MODE,
                                        publicIdentifier, systemIdentifier,
                                        false);
                            } else {
                                documentModeInternal(
                                        DocumentMode.STANDARDS_MODE,
                                        publicIdentifier, systemIdentifier,
                                        false);
                            }
                            break;
                    }
                    // ]NOCPP]

                    /*
                     * 
                     * Then, switch to the root element mode of the tree
                     * construction stage.
                     */
                    mode = BEFORE_HTML;
                    return;
                default:
                    break;
            }
        }
        /*
         * A DOCTYPE token Parse error.
         */
        errStrayDoctype();
        /*
         * Ignore the token.
         */
        return;
    }

    // [NOCPP[

    private boolean isHtml4Doctype(String publicIdentifier) {
        if (publicIdentifier != null
                && (Arrays.binarySearch(TreeBuilder.HTML4_PUBLIC_IDS,
                        publicIdentifier) > -1)) {
            return true;
        }
        return false;
    }

    // ]NOCPP]

    public final void comment(@NoLength char[] buf, int start, int length)
            throws SAXException {
        needToDropLF = false;
        // [NOCPP[
        if (firstCommentLocation == null) {
            firstCommentLocation = new LocatorImpl(tokenizer);
        }
        if (!wantingComments) {
            return;
        }
        // ]NOCPP]
        if (!isInForeign()) {
            switch (mode) {
                case INITIAL:
                case BEFORE_HTML:
                case AFTER_AFTER_BODY:
                case AFTER_AFTER_FRAMESET:
                    /*
                     * A comment token Append a Comment node to the Document
                     * object with the data attribute set to the data given in
                     * the comment token.
                     */
                    appendCommentToDocument(buf, start, length);
                    return;
                case AFTER_BODY:
                    /*
                     * A comment token Append a Comment node to the first
                     * element in the stack of open elements (the html element),
                     * with the data attribute set to the data given in the
                     * comment token.
                     */
                    flushCharacters();
                    appendComment(stack[0].node, buf, start, length);
                    return;
                default:
                    break;
            }
        }
        /*
         * A comment token Append a Comment node to the current node with the
         * data attribute set to the data given in the comment token.
         */
        flushCharacters();
        appendComment(stack[currentPtr].node, buf, start, length);
        return;
    }

    /**
     * @see nu.validator.htmlparser.common.TokenHandler#characters(char[], int,
     *      int)
     */
    public final void characters(@Const @NoLength char[] buf, int start, int length)
            throws SAXException {
        // Note: Can't attach error messages to EOF in C++ yet

        // CPPONLY: if (tokenizer.isViewingXmlSource()) {
        // CPPONLY: return;
        // CPPONLY: }
        if (needToDropLF) {
            needToDropLF = false;
            if (buf[start] == '\n') {
                start++;
                length--;
                if (length == 0) {
                    return;
                }
            }
        }

        // optimize the most common case
        switch (mode) {
            case IN_BODY:
            case IN_CELL:
            case IN_CAPTION:
                if (!isInForeignButNotHtmlOrMathTextIntegrationPoint()) {
                    reconstructTheActiveFormattingElements();
                }
                // fall through
            case TEXT:
                accumulateCharacters(buf, start, length);
                return;
            case IN_TABLE:
            case IN_TABLE_BODY:
            case IN_ROW:
                accumulateCharactersForced(buf, start, length);
                return;
            default:
                int end = start + length;
                charactersloop: for (int i = start; i < end; i++) {
                    switch (buf[i]) {
                        case ' ':
                        case '\t':
                        case '\n':
                        case '\r':
                        case '\u000C':
                            /*
                             * A character token that is one of one of U+0009
                             * CHARACTER TABULATION, U+000A LINE FEED (LF),
                             * U+000C FORM FEED (FF), or U+0020 SPACE
                             */
                            switch (mode) {
                                case INITIAL:
                                case BEFORE_HTML:
                                case BEFORE_HEAD:
                                    /*
                                     * Ignore the token.
                                     */
                                    start = i + 1;
                                    continue;
                                case IN_HEAD:
                                case IN_HEAD_NOSCRIPT:
                                case AFTER_HEAD:
                                case IN_COLUMN_GROUP:
                                case IN_FRAMESET:
                                case AFTER_FRAMESET:
                                    /*
                                     * Append the character to the current node.
                                     */
                                    continue;
                                case FRAMESET_OK:
                                case IN_BODY:
                                case IN_CELL:
                                case IN_CAPTION:
                                    if (start < i) {
                                        accumulateCharacters(buf, start, i
                                                - start);
                                        start = i;
                                    }

                                    /*
                                     * Reconstruct the active formatting
                                     * elements, if any.
                                     */
                                    if (!isInForeignButNotHtmlOrMathTextIntegrationPoint()) {
                                        flushCharacters();
                                        reconstructTheActiveFormattingElements();
                                    }
                                    /*
                                     * Append the token's character to the
                                     * current node.
                                     */
                                    break charactersloop;
                                case IN_SELECT:
                                case IN_SELECT_IN_TABLE:
                                    break charactersloop;
                                case IN_TABLE:
                                case IN_TABLE_BODY:
                                case IN_ROW:
                                    accumulateCharactersForced(buf, i, 1);
                                    start = i + 1;
                                    continue;
                                case AFTER_BODY:
                                case AFTER_AFTER_BODY:
                                case AFTER_AFTER_FRAMESET:
                                    if (start < i) {
                                        accumulateCharacters(buf, start, i
                                                - start);
                                        start = i;
                                    }
                                    /*
                                     * Reconstruct the active formatting
                                     * elements, if any.
                                     */
                                    flushCharacters();
                                    reconstructTheActiveFormattingElements();
                                    /*
                                     * Append the token's character to the
                                     * current node.
                                     */
                                    continue;
                            }
                        default:
                            /*
                             * A character token that is not one of one of
                             * U+0009 CHARACTER TABULATION, U+000A LINE FEED
                             * (LF), U+000C FORM FEED (FF), or U+0020 SPACE
                             */
                            switch (mode) {
                                case INITIAL:
                                    /*
                                     * Parse error.
                                     */
                                    // [NOCPP[
                                    switch (doctypeExpectation) {
                                        case AUTO:
                                            err("Non-space characters found without seeing a doctype first. Expected e.g. \u201C<!DOCTYPE html>\u201D.");
                                            break;
                                        case HTML:
                                            // XXX figure out a way to report this in the Gecko View Source case
                                            err("Non-space characters found without seeing a doctype first. Expected \u201C<!DOCTYPE html>\u201D.");
                                            break;
                                        case HTML401_STRICT:
                                            err("Non-space characters found without seeing a doctype first. Expected \u201C<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\u201D.");
                                            break;
                                        case HTML401_TRANSITIONAL:
                                            err("Non-space characters found without seeing a doctype first. Expected \u201C<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\u201D.");
                                            break;
                                        case NO_DOCTYPE_ERRORS:
                                    }
                                    // ]NOCPP]
                                    /*
                                     * 
                                     * Set the document to quirks mode.
                                     */
                                    documentModeInternal(
                                            DocumentMode.QUIRKS_MODE, null,
                                            null, false);
                                    /*
                                     * Then, switch to the root element mode of
                                     * the tree construction stage
                                     */
                                    mode = BEFORE_HTML;
                                    /*
                                     * and reprocess the current token.
                                     */
                                    i--;
                                    continue;
                                case BEFORE_HTML:
                                    /*
                                     * Create an HTMLElement node with the tag
                                     * name html, in the HTML namespace. Append
                                     * it to the Document object.
                                     */
                                    // No need to flush characters here,
                                    // because there's nothing to flush.
                                    appendHtmlElementToDocumentAndPush();
                                    /* Switch to the main mode */
                                    mode = BEFORE_HEAD;
                                    /*
                                     * reprocess the current token.
                                     */
                                    i--;
                                    continue;
                                case BEFORE_HEAD:
                                    if (start < i) {
                                        accumulateCharacters(buf, start, i
                                                - start);
                                        start = i;
                                    }
                                    /*
                                     * /Act as if a start tag token with the tag
                                     * name "head" and no attributes had been
                                     * seen,
                                     */
                                    flushCharacters();
                                    appendToCurrentNodeAndPushHeadElement(HtmlAttributes.EMPTY_ATTRIBUTES);
                                    mode = IN_HEAD;
                                    /*
                                     * then reprocess the current token.
                                     * 
                                     * This will result in an empty head element
                                     * being generated, with the current token
                                     * being reprocessed in the "after head"
                                     * insertion mode.
                                     */
                                    i--;
                                    continue;
                                case IN_HEAD:
                                    if (start < i) {
                                        accumulateCharacters(buf, start, i
                                                - start);
                                        start = i;
                                    }
                                    /*
                                     * Act as if an end tag token with the tag
                                     * name "head" had been seen,
                                     */
                                    flushCharacters();
                                    pop();
                                    mode = AFTER_HEAD;
                                    /*
                                     * and reprocess the current token.
                                     */
                                    i--;
                                    continue;
                                case IN_HEAD_NOSCRIPT:
                                    if (start < i) {
                                        accumulateCharacters(buf, start, i
                                                - start);
                                        start = i;
                                    }
                                    /*
                                     * Parse error. Act as if an end tag with
                                     * the tag name "noscript" had been seen
                                     */
                                    errNonSpaceInNoscriptInHead();
                                    flushCharacters();
                                    pop();
                                    mode = IN_HEAD;
                                    /*
                                     * and reprocess the current token.
                                     */
                                    i--;
                                    continue;
                                case AFTER_HEAD:
                                    if (start < i) {
                                        accumulateCharacters(buf, start, i
                                                - start);
                                        start = i;
                                    }
                                    /*
                                     * Act as if a start tag token with the tag
                                     * name "body" and no attributes had been
                                     * seen,
                                     */
                                    flushCharacters();
                                    appendToCurrentNodeAndPushBodyElement();
                                    mode = FRAMESET_OK;
                                    /*
                                     * and then reprocess the current token.
                                     */
                                    i--;
                                    continue;
                                case FRAMESET_OK:
                                    framesetOk = false;
                                    mode = IN_BODY;
                                    i--;
                                    continue;
                                case IN_BODY:
                                case IN_CELL:
                                case IN_CAPTION:
                                    if (start < i) {
                                        accumulateCharacters(buf, start, i
                                                - start);
                                        start = i;
                                    }
                                    /*
                                     * Reconstruct the active formatting
                                     * elements, if any.
                                     */
                                    if (!isInForeignButNotHtmlOrMathTextIntegrationPoint()) {
                                        flushCharacters();
                                        reconstructTheActiveFormattingElements();
                                    }
                                    /*
                                     * Append the token's character to the
                                     * current node.
                                     */
                                    break charactersloop;
                                case IN_TABLE:
                                case IN_TABLE_BODY:
                                case IN_ROW:
                                    accumulateCharactersForced(buf, i, 1);
                                    start = i + 1;
                                    continue;
                                case IN_COLUMN_GROUP:
                                    if (start < i) {
                                        accumulateCharacters(buf, start, i
                                                - start);
                                        start = i;
                                    }
                                    /*
                                     * Act as if an end tag with the tag name
                                     * "colgroup" had been seen, and then, if
                                     * that token wasn't ignored, reprocess the
                                     * current token.
                                     */
                                    if (currentPtr == 0) {
                                        errNonSpaceInColgroupInFragment();
                                        start = i + 1;
                                        continue;
                                    }
                                    flushCharacters();
                                    pop();
                                    mode = IN_TABLE;
                                    i--;
                                    continue;
                                case IN_SELECT:
                                case IN_SELECT_IN_TABLE:
                                    break charactersloop;
                                case AFTER_BODY:
                                    errNonSpaceAfterBody();
                                    fatal();
                                    mode = framesetOk ? FRAMESET_OK : IN_BODY;
                                    i--;
                                    continue;
                                case IN_FRAMESET:
                                    if (start < i) {
                                        accumulateCharacters(buf, start, i
                                                - start);
                                        start = i;
                                    }
                                    /*
                                     * Parse error.
                                     */
                                    errNonSpaceInFrameset();
                                    /*
                                     * Ignore the token.
                                     */
                                    start = i + 1;
                                    continue;
                                case AFTER_FRAMESET:
                                    if (start < i) {
                                        accumulateCharacters(buf, start, i
                                                - start);
                                        start = i;
                                    }
                                    /*
                                     * Parse error.
                                     */
                                    errNonSpaceAfterFrameset();
                                    /*
                                     * Ignore the token.
                                     */
                                    start = i + 1;
                                    continue;
                                case AFTER_AFTER_BODY:
                                    /*
                                     * Parse error.
                                     */
                                    errNonSpaceInTrailer();
                                    /*
                                     * Switch back to the main mode and
                                     * reprocess the token.
                                     */
                                    mode = framesetOk ? FRAMESET_OK : IN_BODY;
                                    i--;
                                    continue;
                                case AFTER_AFTER_FRAMESET:
                                    errNonSpaceInTrailer();
                                    /*
                                     * Switch back to the main mode and
                                     * reprocess the token.
                                     */
                                    mode = IN_FRAMESET;
                                    i--;
                                    continue;
                            }
                    }
                }
                if (start < end) {
                    accumulateCharacters(buf, start, end - start);
                }
        }
    }

    /**
     * @see nu.validator.htmlparser.common.TokenHandler#zeroOriginatingReplacementCharacter()
     */
    public void zeroOriginatingReplacementCharacter() throws SAXException {
        if (mode == TEXT) {
            accumulateCharacters(REPLACEMENT_CHARACTER, 0, 1);
            return;
        }
        if (currentPtr >= 0) {
            if (isSpecialParentInForeign(stack[currentPtr])) {
                return;
            }
            accumulateCharacters(REPLACEMENT_CHARACTER, 0, 1);
        }
    }

    public final void eof() throws SAXException {
        flushCharacters();
        // Note: Can't attach error messages to EOF in C++ yet
        eofloop: for (;;) {
            if (isInForeign()) {
                // [NOCPP[
                err("End of file in a foreign namespace context.");
                // ]NOCPP]
                break eofloop;
            }
            switch (mode) {
                case INITIAL:
                    /*
                     * Parse error.
                     */
                    // [NOCPP[
                    switch (doctypeExpectation) {
                        case AUTO:
                            err("End of file seen without seeing a doctype first. Expected e.g. \u201C<!DOCTYPE html>\u201D.");
                            break;
                        case HTML:
                            err("End of file seen without seeing a doctype first. Expected \u201C<!DOCTYPE html>\u201D.");
                            break;
                        case HTML401_STRICT:
                            err("End of file seen without seeing a doctype first. Expected \u201C<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\u201D.");
                            break;
                        case HTML401_TRANSITIONAL:
                            err("End of file seen without seeing a doctype first. Expected \u201C<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\u201D.");
                            break;
                        case NO_DOCTYPE_ERRORS:
                    }
                    // ]NOCPP]
                    /*
                     * 
                     * Set the document to quirks mode.
                     */
                    documentModeInternal(DocumentMode.QUIRKS_MODE, null, null,
                            false);
                    /*
                     * Then, switch to the root element mode of the tree
                     * construction stage
                     */
                    mode = BEFORE_HTML;
                    /*
                     * and reprocess the current token.
                     */
                    continue;
                case BEFORE_HTML:
                    /*
                     * Create an HTMLElement node with the tag name html, in the
                     * HTML namespace. Append it to the Document object.
                     */
                    appendHtmlElementToDocumentAndPush();
                    // XXX application cache manifest
                    /* Switch to the main mode */
                    mode = BEFORE_HEAD;
                    /*
                     * reprocess the current token.
                     */
                    continue;
                case BEFORE_HEAD:
                    appendToCurrentNodeAndPushHeadElement(HtmlAttributes.EMPTY_ATTRIBUTES);
                    mode = IN_HEAD;
                    continue;
                case IN_HEAD:
                    // [NOCPP[
                    if (errorHandler != null && currentPtr > 1) {
                        errEofWithUnclosedElements();
                    }
                    // ]NOCPP]
                    while (currentPtr > 0) {
                        popOnEof();
                    }
                    mode = AFTER_HEAD;
                    continue;
                case IN_HEAD_NOSCRIPT:
                    // [NOCPP[
                    errEofWithUnclosedElements();
                    // ]NOCPP]
                    while (currentPtr > 1) {
                        popOnEof();
                    }
                    mode = IN_HEAD;
                    continue;
                case AFTER_HEAD:
                    appendToCurrentNodeAndPushBodyElement();
                    mode = IN_BODY;
                    continue;
                case IN_COLUMN_GROUP:
                    if (currentPtr == 0) {
                        assert fragment;
                        break eofloop;
                    } else {
                        popOnEof();
                        mode = IN_TABLE;
                        continue;
                    }
                case FRAMESET_OK:
                case IN_CAPTION:
                case IN_CELL:
                case IN_BODY:
                    // [NOCPP[
                    openelementloop: for (int i = currentPtr; i >= 0; i--) {
                        int group = stack[i].getGroup();
                        switch (group) {
                            case DD_OR_DT:
                            case LI:
                            case P:
                            case TBODY_OR_THEAD_OR_TFOOT:
                            case TD_OR_TH:
                            case BODY:
                            case HTML:
                                break;
                            default:
                                errEofWithUnclosedElements();
                                break openelementloop;
                        }
                    }
                    // ]NOCPP]
                    break eofloop;
                case TEXT:
                    // [NOCPP[
                    if (errorHandler != null) {
                        errNoCheck("End of file seen when expecting text or an end tag.");
                        errListUnclosedStartTags(0);
                    }
                    // ]NOCPP]
                    // XXX mark script as already executed
                    if (originalMode == AFTER_HEAD) {
                        popOnEof();
                    }
                    popOnEof();
                    mode = originalMode;
                    continue;
                case IN_TABLE_BODY:
                case IN_ROW:
                case IN_TABLE:
                case IN_SELECT:
                case IN_SELECT_IN_TABLE:
                case IN_FRAMESET:
                    // [NOCPP[
                    if (errorHandler != null && currentPtr > 0) {
                        errEofWithUnclosedElements();
                    }
                    // ]NOCPP]
                    break eofloop;
                case AFTER_BODY:
                case AFTER_FRAMESET:
                case AFTER_AFTER_BODY:
                case AFTER_AFTER_FRAMESET:
                default:
                    // [NOCPP[
                    if (currentPtr == 0) { // This silliness is here to poison
                        // buggy compiler optimizations in
                        // GWT
                        System.currentTimeMillis();
                    }
                    // ]NOCPP]
                    break eofloop;
            }
        }
        while (currentPtr > 0) {
            popOnEof();
        }
        if (!fragment) {
            popOnEof();
        }
        /* Stop parsing. */
    }

    /**
     * @see nu.validator.htmlparser.common.TokenHandler#endTokenization()
     */
    public final void endTokenization() throws SAXException {
        formPointer = null;
        headPointer = null;
        deepTreeSurrogateParent = null;
        if (stack != null) {
            while (currentPtr > -1) {
                stack[currentPtr].release();
                currentPtr--;
            }
            stack = null;
        }
        if (listOfActiveFormattingElements != null) {
            while (listPtr > -1) {
                if (listOfActiveFormattingElements[listPtr] != null) {
                    listOfActiveFormattingElements[listPtr].release();
                }
                listPtr--;
            }
            listOfActiveFormattingElements = null;
        }
        // [NOCPP[
        idLocations.clear();
        // ]NOCPP]
        charBuffer = null;
        end();
    }

    public final void startTag(ElementName elementName,
            HtmlAttributes attributes, boolean selfClosing) throws SAXException {
        flushCharacters();

        // [NOCPP[
        if (errorHandler != null) {
            // ID uniqueness
            @IdType String id = attributes.getId();
            if (id != null) {
                LocatorImpl oldLoc = idLocations.get(id);
                if (oldLoc !
