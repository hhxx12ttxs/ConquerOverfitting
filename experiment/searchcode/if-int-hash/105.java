/*
 * Copyright (c) 2008-2011 Mozilla Foundation
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

package nu.validator.htmlparser.impl;

import java.util.Arrays;

import nu.validator.htmlparser.annotation.Local;
import nu.validator.htmlparser.annotation.NoLength;
import nu.validator.htmlparser.annotation.NsUri;
import nu.validator.htmlparser.annotation.Prefix;
import nu.validator.htmlparser.annotation.QName;
import nu.validator.htmlparser.annotation.Virtual;
import nu.validator.htmlparser.common.Interner;

public final class AttributeName
// Uncomment to regenerate
// implements Comparable<AttributeName>
{
    // [NOCPP[

    public static final int NCNAME_HTML = 1;

    public static final int NCNAME_FOREIGN = (1 << 1) | (1 << 2);

    public static final int NCNAME_LANG = (1 << 3);

    public static final int IS_XMLNS = (1 << 4);

    public static final int CASE_FOLDED = (1 << 5);

    public static final int BOOLEAN = (1 << 6);

    // ]NOCPP]
    
    /**
     * An array representing no namespace regardless of namespace mode (HTML,
     * SVG, MathML, lang-mapping HTML) used.
     */
    static final @NoLength @NsUri String[] ALL_NO_NS = { "", "", "",
    // [NOCPP[
            ""
    // ]NOCPP]
    };

    /**
     * An array that has no namespace for the HTML mode but the XMLNS namespace
     * for the SVG and MathML modes.
     */
    private static final @NoLength @NsUri String[] XMLNS_NS = { "",
            "http://www.w3.org/2000/xmlns/", "http://www.w3.org/2000/xmlns/",
            // [NOCPP[
            ""
    // ]NOCPP]
    };

    /**
     * An array that has no namespace for the HTML mode but the XML namespace
     * for the SVG and MathML modes.
     */
    private static final @NoLength @NsUri String[] XML_NS = { "",
            "http://www.w3.org/XML/1998/namespace",
            "http://www.w3.org/XML/1998/namespace",
            // [NOCPP[
            ""
    // ]NOCPP]
    };

    /**
     * An array that has no namespace for the HTML mode but the XLink namespace
     * for the SVG and MathML modes.
     */
    private static final @NoLength @NsUri String[] XLINK_NS = { "",
            "http://www.w3.org/1999/xlink", "http://www.w3.org/1999/xlink",
            // [NOCPP[
            ""
    // ]NOCPP]
    };

    // [NOCPP[
    /**
     * An array that has no namespace for the HTML, SVG and MathML modes but has
     * the XML namespace for the lang-mapping HTML mode.
     */
    private static final @NoLength @NsUri String[] LANG_NS = { "", "", "",
            "http://www.w3.org/XML/1998/namespace" };

    // ]NOCPP]

    /**
     * An array for no prefixes in any mode.
     */
    static final @NoLength @Prefix String[] ALL_NO_PREFIX = { null, null, null,
    // [NOCPP[
            null
    // ]NOCPP]
    };

    /**
     * An array for no prefixe in the HTML mode and the <code>xmlns</code>
     * prefix in the SVG and MathML modes.
     */
    private static final @NoLength @Prefix String[] XMLNS_PREFIX = { null,
            "xmlns", "xmlns",
            // [NOCPP[
            null
    // ]NOCPP]
    };

    /**
     * An array for no prefixe in the HTML mode and the <code>xlink</code>
     * prefix in the SVG and MathML modes.
     */
    private static final @NoLength @Prefix String[] XLINK_PREFIX = { null,
            "xlink", "xlink",
            // [NOCPP[
            null
    // ]NOCPP]
    };

    /**
     * An array for no prefixe in the HTML mode and the <code>xml</code> prefix
     * in the SVG and MathML modes.
     */
    private static final @NoLength @Prefix String[] XML_PREFIX = { null, "xml",
            "xml",
            // [NOCPP[
            null
    // ]NOCPP]
    };

    // [NOCPP[

    private static final @NoLength @Prefix String[] LANG_PREFIX = { null, null,
            null, "xml" };

    private static @QName String[] COMPUTE_QNAME(String[] local, String[] prefix) {
        @QName String[] arr = new String[4];
        for (int i = 0; i < arr.length; i++) {
            if (prefix[i] == null) {
                arr[i] = local[i];
            } else {
                arr[i] = (prefix[i] + ':' + local[i]).intern();
            }
        }
        return arr;
    }

    // ]NOCPP]

    /**
     * An initialization helper for having a one name in the SVG mode and
     * another name in the other modes.
     * 
     * @param name
     *            the name for the non-SVG modes
     * @param camel
     *            the name for the SVG mode
     * @return the initialized name array
     */
    private static @NoLength @Local String[] SVG_DIFFERENT(@Local String name,
            @Local String camel) {
        @NoLength @Local String[] arr = new String[4];
        arr[0] = name;
        arr[1] = name;
        arr[2] = camel;
        // [NOCPP[
        arr[3] = name;
        // ]NOCPP]
        return arr;
    }

    /**
     * An initialization helper for having a one name in the MathML mode and
     * another name in the other modes.
     * 
     * @param name
     *            the name for the non-MathML modes
     * @param camel
     *            the name for the MathML mode
     * @return the initialized name array
     */
    private static @NoLength @Local String[] MATH_DIFFERENT(@Local String name,
            @Local String camel) {
        @NoLength @Local String[] arr = new String[4];
        arr[0] = name;
        arr[1] = camel;
        arr[2] = name;
        // [NOCPP[
        arr[3] = name;
        // ]NOCPP]
        return arr;
    }

    /**
     * An initialization helper for having a different local name in the HTML
     * mode and the SVG and MathML modes.
     * 
     * @param name
     *            the name for the HTML mode
     * @param suffix
     *            the name for the SVG and MathML modes
     * @return the initialized name array
     */
    private static @NoLength @Local String[] COLONIFIED_LOCAL(
            @Local String name, @Local String suffix) {
        @NoLength @Local String[] arr = new String[4];
        arr[0] = name;
        arr[1] = suffix;
        arr[2] = suffix;
        // [NOCPP[
        arr[3] = name;
        // ]NOCPP]
        return arr;
    }

    /**
     * An initialization helper for having the same local name in all modes.
     * 
     * @param name
     *            the name
     * @return the initialized name array
     */
    static @NoLength @Local String[] SAME_LOCAL(@Local String name) {
        @NoLength @Local String[] arr = new String[4];
        arr[0] = name;
        arr[1] = name;
        arr[2] = name;
        // [NOCPP[
        arr[3] = name;
        // ]NOCPP]
        return arr;
    }

    /**
     * Returns an attribute name by buffer.
     * 
     * <p>
     * C++ ownership: The return value is either released by the caller if the
     * attribute is a duplicate or the ownership is transferred to
     * HtmlAttributes and released upon clearing or destroying that object.
     * 
     * @param buf
     *            the buffer
     * @param offset
     *            ignored
     * @param length
     *            length of data
     * @param checkNcName
     *            whether to check ncnameness
     * @return an <code>AttributeName</code> corresponding to the argument data
     */
    static AttributeName nameByBuffer(@NoLength char[] buf, int offset,
            int length
            // [NOCPP[
            , boolean checkNcName
            // ]NOCPP]
            , Interner interner) {
        // XXX deal with offset
        int hash = AttributeName.bufToHash(buf, length);
        int index = Arrays.binarySearch(AttributeName.ATTRIBUTE_HASHES, hash);
        if (index < 0) {
            return AttributeName.createAttributeName(
                    Portability.newLocalNameFromBuffer(buf, offset, length,
                            interner)
                    // [NOCPP[
                    , checkNcName
            // ]NOCPP]
            );
        } else {
            AttributeName attributeName = AttributeName.ATTRIBUTE_NAMES[index];
            @Local String name = attributeName.getLocal(AttributeName.HTML);
            if (!Portability.localEqualsBuffer(name, buf, offset, length)) {
                return AttributeName.createAttributeName(
                        Portability.newLocalNameFromBuffer(buf, offset, length,
                                interner)
                        // [NOCPP[
                        , checkNcName
                // ]NOCPP]
                );
            }
            return attributeName;
        }
    }

    /**
     * This method has to return a unique integer for each well-known
     * lower-cased attribute name.
     * 
     * @param buf
     * @param len
     * @return
     */
    private static int bufToHash(@NoLength char[] buf, int len) {
        int hash2 = 0;
        int hash = len;
        hash <<= 5;
        hash += buf[0] - 0x60;
        int j = len;
        for (int i = 0; i < 4 && j > 0; i++) {
            j--;
            hash <<= 5;
            hash += buf[j] - 0x60;
            hash2 <<= 6;
            hash2 += buf[i] - 0x5F;
        }
        return hash ^ hash2;
    }

    /**
     * The mode value for HTML.
     */
    public static final int HTML = 0;

    /**
     * The mode value for MathML.
     */
    public static final int MATHML = 1;

    /**
     * The mode value for SVG.
     */
    public static final int SVG = 2;

    // [NOCPP[

    /**
     * The mode value for lang-mapping HTML.
     */
    public static final int HTML_LANG = 3;

    // ]NOCPP]

    /**
     * The namespaces indexable by mode.
     */
    private final @NsUri @NoLength String[] uri;

    /**
     * The local names indexable by mode.
     */
    private final @Local @NoLength String[] local;

    /**
     * The prefixes indexably by mode.
     */
    private final @Prefix @NoLength String[] prefix;

    // [NOCPP[

    private final int flags;

    /**
     * The qnames indexable by mode.
     */
    private final @QName @NoLength String[] qName;

    // ]NOCPP]

    /**
     * The startup-time constructor.
     * 
     * @param uri
     *            the namespace
     * @param local
     *            the local name
     * @param prefix
     *            the prefix
     * @param ncname
     *            the ncnameness
     * @param xmlns
     *            whether this is an xmlns attribute
     */
    protected AttributeName(@NsUri @NoLength String[] uri,
            @Local @NoLength String[] local, @Prefix @NoLength String[] prefix
            // [NOCPP[
            , int flags
    // ]NOCPP]        
    ) {
        this.uri = uri;
        this.local = local;
        this.prefix = prefix;
        // [NOCPP[
        this.qName = COMPUTE_QNAME(local, prefix);
        this.flags = flags;
        // ]NOCPP]
    }

    /**
     * Creates an <code>AttributeName</code> for a local name.
     * 
     * @param name
     *            the name
     * @param checkNcName
     *            whether to check ncnameness
     * @return an <code>AttributeName</code>
     */
    private static AttributeName createAttributeName(@Local String name
    // [NOCPP[
            , boolean checkNcName
    // ]NOCPP]
    ) {
        // [NOCPP[
        int flags = NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG;
        if (name.startsWith("xmlns:")) {
            flags = IS_XMLNS;
        } else if (checkNcName && !NCName.isNCName(name)) {
            flags = 0;
        }
        // ]NOCPP]
        return new AttributeName(AttributeName.ALL_NO_NS,
                AttributeName.SAME_LOCAL(name), ALL_NO_PREFIX, flags);
    }

    /**
     * Deletes runtime-allocated instances in C++.
     */
    @Virtual void release() {
        // No-op in Java.
        // Implement as |delete this;| in subclass.
    }

    /**
     * The C++ destructor.
     */
    @SuppressWarnings("unused") private void destructor() {
        Portability.deleteArray(local);
    }

    /**
     * Clones the attribute using an interner. Returns <code>this</code> in Java
     * and for non-dynamic instances in C++.
     * 
     * @param interner
     *            an interner
     * @return a clone
     */
    @Virtual public AttributeName cloneAttributeName(Interner interner) {
        return this;
    }

    // [NOCPP[
    /**
     * Creator for use when the XML violation policy requires an attribute name
     * to be changed.
     * 
     * @param name
     *            the name of the attribute to create
     */
    static AttributeName create(@Local String name) {
        return new AttributeName(AttributeName.ALL_NO_NS,
                AttributeName.SAME_LOCAL(name), ALL_NO_PREFIX,
                NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    }

    /**
     * Queries whether this name is an XML 1.0 4th ed. NCName.
     * 
     * @param mode
     *            the SVG/MathML/HTML mode
     * @return <code>true</code> if this is an NCName in the given mode
     */
    public boolean isNcName(int mode) {
        return (flags & (1 << mode)) != 0;
    }

    /**
     * Queries whether this is an <code>xmlns</code> attribute.
     * 
     * @return <code>true</code> if this is an <code>xmlns</code> attribute
     */
    public boolean isXmlns() {
        return (flags & IS_XMLNS) != 0;
    }
        
    /**
     * Queries whether this attribute has a case-folded value in the HTML4 mode
     * of the parser.
     * 
     * @return <code>true</code> if the value is case-folded
     */
    boolean isCaseFolded() {
        return (flags & CASE_FOLDED) != 0;
    }

    boolean isBoolean() {
        return (flags & BOOLEAN) != 0;
    }

    public @QName String getQName(int mode) {
        return qName[mode];
    }

    // ]NOCPP]

    public @NsUri String getUri(int mode) {
        return uri[mode];
    }

    public @Local String getLocal(int mode) {
        return local[mode];
    }

    public @Prefix String getPrefix(int mode) {
        return prefix[mode];
    }

    boolean equalsAnother(AttributeName another) {
        return this.getLocal(AttributeName.HTML) == another.getLocal(AttributeName.HTML);
    }

    // START CODE ONLY USED FOR GENERATING CODE uncomment to regenerate

//    /**
//     * @see java.lang.Object#toString()
//     */
//    @Override public String toString() {
//        return "(" + formatNs() + ", " + formatLocal() + ", " + formatPrefix()
//                + ", " + formatFlags() + ")";
//    }
//
//    private String formatFlags() {
//        StringBuilder builder = new StringBuilder();
//        if ((flags & NCNAME_HTML) != 0) {
//            if (builder.length() != 0) {
//                builder.append(" | ");
//            }
//            builder.append("NCNAME_HTML");
//        }            
//        if ((flags & NCNAME_FOREIGN) != 0) {
//            if (builder.length() != 0) {
//                builder.append(" | ");
//            }
//            builder.append("NCNAME_FOREIGN");
//        }
//        if ((flags & NCNAME_LANG) != 0) {
//            if (builder.length() != 0) {
//                builder.append(" | ");
//            }
//            builder.append("NCNAME_LANG");
//        }
//        if (isXmlns()) {
//            if (builder.length() != 0) {
//                builder.append(" | ");
//            }
//            builder.append("IS_XMLNS");
//        }
//        if (isCaseFolded()) {
//            if (builder.length() != 0) {
//                builder.append(" | ");
//            }
//            builder.append("CASE_FOLDED");
//        }
//        if (isBoolean()) {
//            if (builder.length() != 0) {
//                builder.append(" | ");
//            }
//            builder.append("BOOLEAN");
//        }
//        if (builder.length() == 0) {
//            return "0";
//        }
//        return builder.toString();
//    }
//
//    public int compareTo(AttributeName other) {
//        int thisHash = this.hash();
//        int otherHash = other.hash();
//        if (thisHash < otherHash) {
//            return -1;
//        } else if (thisHash == otherHash) {
//            return 0;
//        } else {
//            return 1;
//        }
//    }
//
//    private String formatPrefix() {
//        if (prefix[0] == null && prefix[1] == null && prefix[2] == null
//                && prefix[3] == null) {
//            return "ALL_NO_PREFIX";
//        } else if (prefix[0] == null && prefix[1] == prefix[2]
//                && prefix[3] == null) {
//            if ("xmlns".equals(prefix[1])) {
//                return "XMLNS_PREFIX";
//            } else if ("xml".equals(prefix[1])) {
//                return "XML_PREFIX";
//            } else if ("xlink".equals(prefix[1])) {
//                return "XLINK_PREFIX";
//            } else {
//                throw new IllegalStateException();
//            }
//        } else if (prefix[0] == null && prefix[1] == null && prefix[2] == null
//                && prefix[3] == "xml") {
//            return "LANG_PREFIX";
//        } else {
//            throw new IllegalStateException();
//        }
//    }
//
//    private String formatLocal() {
//        if (local[0] == local[1] && local[0] == local[3]
//                && local[0] != local[2]) {
//            return "SVG_DIFFERENT(\"" + local[0] + "\", \"" + local[2] + "\")";
//        }
//        if (local[0] == local[2] && local[0] == local[3]
//                && local[0] != local[1]) {
//            return "MATH_DIFFERENT(\"" + local[0] + "\", \"" + local[1] + "\")";
//        }
//        if (local[0] == local[3] && local[1] == local[2]
//                && local[0] != local[1]) {
//            return "COLONIFIED_LOCAL(\"" + local[0] + "\", \"" + local[1]
//                    + "\")";
//        }
//        for (int i = 1; i < local.length; i++) {
//            if (local[0] != local[i]) {
//                throw new IllegalStateException();
//            }
//        }
//        return "SAME_LOCAL(\"" + local[0] + "\")";
//    }
//
//    private String formatNs() {
//        if (uri[0] == "" && uri[1] == "" && uri[2] == "" && uri[3] == "") {
//            return "ALL_NO_NS";
//        } else if (uri[0] == "" && uri[1] == uri[2] && uri[3] == "") {
//            if ("http://www.w3.org/2000/xmlns/".equals(uri[1])) {
//                return "XMLNS_NS";
//            } else if ("http://www.w3.org/XML/1998/namespace".equals(uri[1])) {
//                return "XML_NS";
//            } else if ("http://www.w3.org/1999/xlink".equals(uri[1])) {
//                return "XLINK_NS";
//            } else {
//                throw new IllegalStateException();
//            }
//        } else if (uri[0] == "" && uri[1] == "" && uri[2] == ""
//                && uri[3] == "http://www.w3.org/XML/1998/namespace") {
//            return "LANG_NS";
//        } else {
//            throw new IllegalStateException();
//        }
//    }
//
//    private String constName() {
//        String name = getLocal(HTML);
//        char[] buf = new char[name.length()];
//        for (int i = 0; i < name.length(); i++) {
//            char c = name.charAt(i);
//            if (c == '-' || c == ':') {
//                buf[i] = '_';
//            } else if (c >= 'a' && c <= 'z') {
//                buf[i] = (char) (c - 0x20);
//            } else {
//                buf[i] = c;
//            }
//        }
//        return new String(buf);
//    }
//
//    private int hash() {
//        String name = getLocal(HTML);
//        return bufToHash(name.toCharArray(), name.length());
//    }
//
//    /**
//     * Regenerate self
//     * 
//     * @param args
//     */
//    public static void main(String[] args) {
//        Arrays.sort(ATTRIBUTE_NAMES);
//        for (int i = 1; i < ATTRIBUTE_NAMES.length; i++) {
//            if (ATTRIBUTE_NAMES[i].hash() == ATTRIBUTE_NAMES[i - 1].hash()) {
//                System.err.println("Hash collision: "
//                        + ATTRIBUTE_NAMES[i].getLocal(HTML) + ", "
//                        + ATTRIBUTE_NAMES[i - 1].getLocal(HTML));
//                return;
//            }
//        }
//        for (int i = 0; i < ATTRIBUTE_NAMES.length; i++) {
//            AttributeName att = ATTRIBUTE_NAMES[i];
//            System.out.println("public static final AttributeName "
//                    + att.constName() + " = new AttributeName" + att.toString()
//                    + ";");
//        }
//        System.out.println("private final static @NoLength AttributeName[] ATTRIBUTE_NAMES = {");
//        for (int i = 0; i < ATTRIBUTE_NAMES.length; i++) {
//            AttributeName att = ATTRIBUTE_NAMES[i];
//            System.out.println(att.constName() + ",");
//        }
//        System.out.println("};");
//        System.out.println("private final static int[] ATTRIBUTE_HASHES = {");
//        for (int i = 0; i < ATTRIBUTE_NAMES.length; i++) {
//            AttributeName att = ATTRIBUTE_NAMES[i];
//            System.out.println(Integer.toString(att.hash()) + ",");
//        }
//        System.out.println("};");
//    }

    // START GENERATED CODE
    public static final AttributeName D = new AttributeName(ALL_NO_NS, SAME_LOCAL("d"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName K = new AttributeName(ALL_NO_NS, SAME_LOCAL("k"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName R = new AttributeName(ALL_NO_NS, SAME_LOCAL("r"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName X = new AttributeName(ALL_NO_NS, SAME_LOCAL("x"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName Y = new AttributeName(ALL_NO_NS, SAME_LOCAL("y"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName Z = new AttributeName(ALL_NO_NS, SAME_LOCAL("z"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName BY = new AttributeName(ALL_NO_NS, SAME_LOCAL("by"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName CX = new AttributeName(ALL_NO_NS, SAME_LOCAL("cx"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName CY = new AttributeName(ALL_NO_NS, SAME_LOCAL("cy"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName DX = new AttributeName(ALL_NO_NS, SAME_LOCAL("dx"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName DY = new AttributeName(ALL_NO_NS, SAME_LOCAL("dy"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName G2 = new AttributeName(ALL_NO_NS, SAME_LOCAL("g2"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName G1 = new AttributeName(ALL_NO_NS, SAME_LOCAL("g1"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName FX = new AttributeName(ALL_NO_NS, SAME_LOCAL("fx"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName FY = new AttributeName(ALL_NO_NS, SAME_LOCAL("fy"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName K4 = new AttributeName(ALL_NO_NS, SAME_LOCAL("k4"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName K2 = new AttributeName(ALL_NO_NS, SAME_LOCAL("k2"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName K3 = new AttributeName(ALL_NO_NS, SAME_LOCAL("k3"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName K1 = new AttributeName(ALL_NO_NS, SAME_LOCAL("k1"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ID = new AttributeName(ALL_NO_NS, SAME_LOCAL("id"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName IN = new AttributeName(ALL_NO_NS, SAME_LOCAL("in"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName U2 = new AttributeName(ALL_NO_NS, SAME_LOCAL("u2"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName U1 = new AttributeName(ALL_NO_NS, SAME_LOCAL("u1"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName RT = new AttributeName(ALL_NO_NS, SAME_LOCAL("rt"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName RX = new AttributeName(ALL_NO_NS, SAME_LOCAL("rx"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName RY = new AttributeName(ALL_NO_NS, SAME_LOCAL("ry"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName TO = new AttributeName(ALL_NO_NS, SAME_LOCAL("to"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName Y2 = new AttributeName(ALL_NO_NS, SAME_LOCAL("y2"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName Y1 = new AttributeName(ALL_NO_NS, SAME_LOCAL("y1"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName X1 = new AttributeName(ALL_NO_NS, SAME_LOCAL("x1"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName X2 = new AttributeName(ALL_NO_NS, SAME_LOCAL("x2"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ALT = new AttributeName(ALL_NO_NS, SAME_LOCAL("alt"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName DIR = new AttributeName(ALL_NO_NS, SAME_LOCAL("dir"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED);
    public static final AttributeName DUR = new AttributeName(ALL_NO_NS, SAME_LOCAL("dur"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName END = new AttributeName(ALL_NO_NS, SAME_LOCAL("end"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName FOR = new AttributeName(ALL_NO_NS, SAME_LOCAL("for"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName IN2 = new AttributeName(ALL_NO_NS, SAME_LOCAL("in2"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName MAX = new AttributeName(ALL_NO_NS, SAME_LOCAL("max"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName MIN = new AttributeName(ALL_NO_NS, SAME_LOCAL("min"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName LOW = new AttributeName(ALL_NO_NS, SAME_LOCAL("low"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName REL = new AttributeName(ALL_NO_NS, SAME_LOCAL("rel"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName REV = new AttributeName(ALL_NO_NS, SAME_LOCAL("rev"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName SRC = new AttributeName(ALL_NO_NS, SAME_LOCAL("src"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName AXIS = new AttributeName(ALL_NO_NS, SAME_LOCAL("axis"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ABBR = new AttributeName(ALL_NO_NS, SAME_LOCAL("abbr"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName BBOX = new AttributeName(ALL_NO_NS, SAME_LOCAL("bbox"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName CITE = new AttributeName(ALL_NO_NS, SAME_LOCAL("cite"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName CODE = new AttributeName(ALL_NO_NS, SAME_LOCAL("code"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName BIAS = new AttributeName(ALL_NO_NS, SAME_LOCAL("bias"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName COLS = new AttributeName(ALL_NO_NS, SAME_LOCAL("cols"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName CLIP = new AttributeName(ALL_NO_NS, SAME_LOCAL("clip"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName CHAR = new AttributeName(ALL_NO_NS, SAME_LOCAL("char"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName BASE = new AttributeName(ALL_NO_NS, SAME_LOCAL("base"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName EDGE = new AttributeName(ALL_NO_NS, SAME_LOCAL("edge"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName DATA = new AttributeName(ALL_NO_NS, SAME_LOCAL("data"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName FILL = new AttributeName(ALL_NO_NS, SAME_LOCAL("fill"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName FROM = new AttributeName(ALL_NO_NS, SAME_LOCAL("from"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName FORM = new AttributeName(ALL_NO_NS, SAME_LOCAL("form"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName FACE = new AttributeName(ALL_NO_NS, SAME_LOCAL("face"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName HIGH = new AttributeName(ALL_NO_NS, SAME_LOCAL("high"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName HREF = new AttributeName(ALL_NO_NS, SAME_LOCAL("href"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName OPEN = new AttributeName(ALL_NO_NS, SAME_LOCAL("open"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ICON = new AttributeName(ALL_NO_NS, SAME_LOCAL("icon"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName NAME = new AttributeName(ALL_NO_NS, SAME_LOCAL("name"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName MODE = new AttributeName(ALL_NO_NS, SAME_LOCAL("mode"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName MASK = new AttributeName(ALL_NO_NS, SAME_LOCAL("mask"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName LINK = new AttributeName(ALL_NO_NS, SAME_LOCAL("link"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName LANG = new AttributeName(LANG_NS, SAME_LOCAL("lang"), LANG_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName LOOP = new AttributeName(ALL_NO_NS, SAME_LOCAL("loop"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName LIST = new AttributeName(ALL_NO_NS, SAME_LOCAL("list"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName TYPE = new AttributeName(ALL_NO_NS, SAME_LOCAL("type"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED);
    public static final AttributeName WHEN = new AttributeName(ALL_NO_NS, SAME_LOCAL("when"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName WRAP = new AttributeName(ALL_NO_NS, SAME_LOCAL("wrap"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName TEXT = new AttributeName(ALL_NO_NS, SAME_LOCAL("text"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName PATH = new AttributeName(ALL_NO_NS, SAME_LOCAL("path"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName PING = new AttributeName(ALL_NO_NS, SAME_LOCAL("ping"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName REFX = new AttributeName(ALL_NO_NS, SVG_DIFFERENT("refx", "refX"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName REFY = new AttributeName(ALL_NO_NS, SVG_DIFFERENT("refy", "refY"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName SIZE = new AttributeName(ALL_NO_NS, SAME_LOCAL("size"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName SEED = new AttributeName(ALL_NO_NS, SAME_LOCAL("seed"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ROWS = new AttributeName(ALL_NO_NS, SAME_LOCAL("rows"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName SPAN = new AttributeName(ALL_NO_NS, SAME_LOCAL("span"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName STEP = new AttributeName(ALL_NO_NS, SAME_LOCAL("step"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED);
    public static final AttributeName ROLE = new AttributeName(ALL_NO_NS, SAME_LOCAL("role"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName XREF = new AttributeName(ALL_NO_NS, SAME_LOCAL("xref"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ASYNC = new AttributeName(ALL_NO_NS, SAME_LOCAL("async"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED | BOOLEAN);
    public static final AttributeName ALINK = new AttributeName(ALL_NO_NS, SAME_LOCAL("alink"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ALIGN = new AttributeName(ALL_NO_NS, SAME_LOCAL("align"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED);
    public static final AttributeName CLOSE = new AttributeName(ALL_NO_NS, SAME_LOCAL("close"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName COLOR = new AttributeName(ALL_NO_NS, SAME_LOCAL("color"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName CLASS = new AttributeName(ALL_NO_NS, SAME_LOCAL("class"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName CLEAR = new AttributeName(ALL_NO_NS, SAME_LOCAL("clear"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED);
    public static final AttributeName BEGIN = new AttributeName(ALL_NO_NS, SAME_LOCAL("begin"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName DEPTH = new AttributeName(ALL_NO_NS, SAME_LOCAL("depth"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName DEFER = new AttributeName(ALL_NO_NS, SAME_LOCAL("defer"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED | BOOLEAN);
    public static final AttributeName FENCE = new AttributeName(ALL_NO_NS, SAME_LOCAL("fence"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName FRAME = new AttributeName(ALL_NO_NS, SAME_LOCAL("frame"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED);
    public static final AttributeName ISMAP = new AttributeName(ALL_NO_NS, SAME_LOCAL("ismap"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED | BOOLEAN);
    public static final AttributeName ONEND = new AttributeName(ALL_NO_NS, SAME_LOCAL("onend"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName INDEX = new AttributeName(ALL_NO_NS, SAME_LOCAL("index"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ORDER = new AttributeName(ALL_NO_NS, SAME_LOCAL("order"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName OTHER = new AttributeName(ALL_NO_NS, SAME_LOCAL("other"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONCUT = new AttributeName(ALL_NO_NS, SAME_LOCAL("oncut"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName NARGS = new AttributeName(ALL_NO_NS, SAME_LOCAL("nargs"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName MEDIA = new AttributeName(ALL_NO_NS, SAME_LOCAL("media"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName LABEL = new AttributeName(ALL_NO_NS, SAME_LOCAL("label"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName LOCAL = new AttributeName(ALL_NO_NS, SAME_LOCAL("local"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName WIDTH = new AttributeName(ALL_NO_NS, SAME_LOCAL("width"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName TITLE = new AttributeName(ALL_NO_NS, SAME_LOCAL("title"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName VLINK = new AttributeName(ALL_NO_NS, SAME_LOCAL("vlink"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName VALUE = new AttributeName(ALL_NO_NS, SAME_LOCAL("value"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName SLOPE = new AttributeName(ALL_NO_NS, SAME_LOCAL("slope"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName SHAPE = new AttributeName(ALL_NO_NS, SAME_LOCAL("shape"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED);
    public static final AttributeName SCOPE = new AttributeName(ALL_NO_NS, SAME_LOCAL("scope"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED);
    public static final AttributeName SCALE = new AttributeName(ALL_NO_NS, SAME_LOCAL("scale"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName SPEED = new AttributeName(ALL_NO_NS, SAME_LOCAL("speed"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName STYLE = new AttributeName(ALL_NO_NS, SAME_LOCAL("style"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName RULES = new AttributeName(ALL_NO_NS, SAME_LOCAL("rules"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED);
    public static final AttributeName STEMH = new AttributeName(ALL_NO_NS, SAME_LOCAL("stemh"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName STEMV = new AttributeName(ALL_NO_NS, SAME_LOCAL("stemv"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName START = new AttributeName(ALL_NO_NS, SAME_LOCAL("start"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName XMLNS = new AttributeName(XMLNS_NS, SAME_LOCAL("xmlns"), ALL_NO_PREFIX, IS_XMLNS);
    public static final AttributeName ACCEPT = new AttributeName(ALL_NO_NS, SAME_LOCAL("accept"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ACCENT = new AttributeName(ALL_NO_NS, SAME_LOCAL("accent"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ASCENT = new AttributeName(ALL_NO_NS, SAME_LOCAL("ascent"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ACTIVE = new AttributeName(ALL_NO_NS, SAME_LOCAL("active"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED | BOOLEAN);
    public static final AttributeName ALTIMG = new AttributeName(ALL_NO_NS, SAME_LOCAL("altimg"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ACTION = new AttributeName(ALL_NO_NS, SAME_LOCAL("action"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName BORDER = new AttributeName(ALL_NO_NS, SAME_LOCAL("border"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName CURSOR = new AttributeName(ALL_NO_NS, SAME_LOCAL("cursor"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName COORDS = new AttributeName(ALL_NO_NS, SAME_LOCAL("coords"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName FILTER = new AttributeName(ALL_NO_NS, SAME_LOCAL("filter"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName FORMAT = new AttributeName(ALL_NO_NS, SAME_LOCAL("format"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName HIDDEN = new AttributeName(ALL_NO_NS, SAME_LOCAL("hidden"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName HSPACE = new AttributeName(ALL_NO_NS, SAME_LOCAL("hspace"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName HEIGHT = new AttributeName(ALL_NO_NS, SAME_LOCAL("height"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONMOVE = new AttributeName(ALL_NO_NS, SAME_LOCAL("onmove"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONLOAD = new AttributeName(ALL_NO_NS, SAME_LOCAL("onload"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONDRAG = new AttributeName(ALL_NO_NS, SAME_LOCAL("ondrag"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ORIGIN = new AttributeName(ALL_NO_NS, SAME_LOCAL("origin"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONZOOM = new AttributeName(ALL_NO_NS, SAME_LOCAL("onzoom"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONHELP = new AttributeName(ALL_NO_NS, SAME_LOCAL("onhelp"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONSTOP = new AttributeName(ALL_NO_NS, SAME_LOCAL("onstop"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONDROP = new AttributeName(ALL_NO_NS, SAME_LOCAL("ondrop"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONBLUR = new AttributeName(ALL_NO_NS, SAME_LOCAL("onblur"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName OBJECT = new AttributeName(ALL_NO_NS, SAME_LOCAL("object"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName OFFSET = new AttributeName(ALL_NO_NS, SAME_LOCAL("offset"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ORIENT = new AttributeName(ALL_NO_NS, SAME_LOCAL("orient"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONCOPY = new AttributeName(ALL_NO_NS, SAME_LOCAL("oncopy"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName NOWRAP = new AttributeName(ALL_NO_NS, SAME_LOCAL("nowrap"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED | BOOLEAN);
    public static final AttributeName NOHREF = new AttributeName(ALL_NO_NS, SAME_LOCAL("nohref"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED | BOOLEAN);
    public static final AttributeName MACROS = new AttributeName(ALL_NO_NS, SAME_LOCAL("macros"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName METHOD = new AttributeName(ALL_NO_NS, SAME_LOCAL("method"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED);
    public static final AttributeName LOWSRC = new AttributeName(ALL_NO_NS, SAME_LOCAL("lowsrc"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName LSPACE = new AttributeName(ALL_NO_NS, SAME_LOCAL("lspace"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName LQUOTE = new AttributeName(ALL_NO_NS, SAME_LOCAL("lquote"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName USEMAP = new AttributeName(ALL_NO_NS, SAME_LOCAL("usemap"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName WIDTHS = new AttributeName(ALL_NO_NS, SAME_LOCAL("widths"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName TARGET = new AttributeName(ALL_NO_NS, SAME_LOCAL("target"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName VALUES = new AttributeName(ALL_NO_NS, SAME_LOCAL("values"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName VALIGN = new AttributeName(ALL_NO_NS, SAME_LOCAL("valign"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED);
    public static final AttributeName VSPACE = new AttributeName(ALL_NO_NS, SAME_LOCAL("vspace"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName POSTER = new AttributeName(ALL_NO_NS, SAME_LOCAL("poster"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName POINTS = new AttributeName(ALL_NO_NS, SAME_LOCAL("points"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName PROMPT = new AttributeName(ALL_NO_NS, SAME_LOCAL("prompt"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName SCOPED = new AttributeName(ALL_NO_NS, SAME_LOCAL("scoped"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName STRING = new AttributeName(ALL_NO_NS, SAME_LOCAL("string"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName SCHEME = new AttributeName(ALL_NO_NS, SAME_LOCAL("scheme"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName STROKE = new AttributeName(ALL_NO_NS, SAME_LOCAL("stroke"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName RADIUS = new AttributeName(ALL_NO_NS, SAME_LOCAL("radius"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName RESULT = new AttributeName(ALL_NO_NS, SAME_LOCAL("result"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName REPEAT = new AttributeName(ALL_NO_NS, SAME_LOCAL("repeat"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName RSPACE = new AttributeName(ALL_NO_NS, SAME_LOCAL("rspace"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ROTATE = new AttributeName(ALL_NO_NS, SAME_LOCAL("rotate"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName RQUOTE = new AttributeName(ALL_NO_NS, SAME_LOCAL("rquote"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ALTTEXT = new AttributeName(ALL_NO_NS, SAME_LOCAL("alttext"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ARCHIVE = new AttributeName(ALL_NO_NS, SAME_LOCAL("archive"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName AZIMUTH = new AttributeName(ALL_NO_NS, SAME_LOCAL("azimuth"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName CLOSURE = new AttributeName(ALL_NO_NS, SAME_LOCAL("closure"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName CHECKED = new AttributeName(ALL_NO_NS, SAME_LOCAL("checked"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED | BOOLEAN);
    public static final AttributeName CLASSID = new AttributeName(ALL_NO_NS, SAME_LOCAL("classid"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName CHAROFF = new AttributeName(ALL_NO_NS, SAME_LOCAL("charoff"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName BGCOLOR = new AttributeName(ALL_NO_NS, SAME_LOCAL("bgcolor"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName COLSPAN = new AttributeName(ALL_NO_NS, SAME_LOCAL("colspan"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName CHARSET = new AttributeName(ALL_NO_NS, SAME_LOCAL("charset"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName COMPACT = new AttributeName(ALL_NO_NS, SAME_LOCAL("compact"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED | BOOLEAN);
    public static final AttributeName CONTENT = new AttributeName(ALL_NO_NS, SAME_LOCAL("content"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ENCTYPE = new AttributeName(ALL_NO_NS, SAME_LOCAL("enctype"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED);
    public static final AttributeName DATASRC = new AttributeName(ALL_NO_NS, SAME_LOCAL("datasrc"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName DATAFLD = new AttributeName(ALL_NO_NS, SAME_LOCAL("datafld"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName DECLARE = new AttributeName(ALL_NO_NS, SAME_LOCAL("declare"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED | BOOLEAN);
    public static final AttributeName DISPLAY = new AttributeName(ALL_NO_NS, SAME_LOCAL("display"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName DIVISOR = new AttributeName(ALL_NO_NS, SAME_LOCAL("divisor"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName DEFAULT = new AttributeName(ALL_NO_NS, SAME_LOCAL("default"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED | BOOLEAN);
    public static final AttributeName DESCENT = new AttributeName(ALL_NO_NS, SAME_LOCAL("descent"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName KERNING = new AttributeName(ALL_NO_NS, SAME_LOCAL("kerning"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName HANGING = new AttributeName(ALL_NO_NS, SAME_LOCAL("hanging"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName HEADERS = new AttributeName(ALL_NO_NS, SAME_LOCAL("headers"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONPASTE = new AttributeName(ALL_NO_NS, SAME_LOCAL("onpaste"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONCLICK = new AttributeName(ALL_NO_NS, SAME_LOCAL("onclick"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName OPTIMUM = new AttributeName(ALL_NO_NS, SAME_LOCAL("optimum"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONBEGIN = new AttributeName(ALL_NO_NS, SAME_LOCAL("onbegin"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONKEYUP = new AttributeName(ALL_NO_NS, SAME_LOCAL("onkeyup"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONFOCUS = new AttributeName(ALL_NO_NS, SAME_LOCAL("onfocus"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONERROR = new AttributeName(ALL_NO_NS, SAME_LOCAL("onerror"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONINPUT = new AttributeName(ALL_NO_NS, SAME_LOCAL("oninput"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONABORT = new AttributeName(ALL_NO_NS, SAME_LOCAL("onabort"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONSTART = new AttributeName(ALL_NO_NS, SAME_LOCAL("onstart"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONRESET = new AttributeName(ALL_NO_NS, SAME_LOCAL("onreset"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName OPACITY = new AttributeName(ALL_NO_NS, SAME_LOCAL("opacity"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName NOSHADE = new AttributeName(ALL_NO_NS, SAME_LOCAL("noshade"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED | BOOLEAN);
    public static final AttributeName MINSIZE = new AttributeName(ALL_NO_NS, SAME_LOCAL("minsize"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName MAXSIZE = new AttributeName(ALL_NO_NS, SAME_LOCAL("maxsize"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName LARGEOP = new AttributeName(ALL_NO_NS, SAME_LOCAL("largeop"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName UNICODE = new AttributeName(ALL_NO_NS, SAME_LOCAL("unicode"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName TARGETX = new AttributeName(ALL_NO_NS, SVG_DIFFERENT("targetx", "targetX"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName TARGETY = new AttributeName(ALL_NO_NS, SVG_DIFFERENT("targety", "targetY"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName VIEWBOX = new AttributeName(ALL_NO_NS, SVG_DIFFERENT("viewbox", "viewBox"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName VERSION = new AttributeName(ALL_NO_NS, SAME_LOCAL("version"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName PATTERN = new AttributeName(ALL_NO_NS, SAME_LOCAL("pattern"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName PROFILE = new AttributeName(ALL_NO_NS, SAME_LOCAL("profile"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName SPACING = new AttributeName(ALL_NO_NS, SAME_LOCAL("spacing"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName RESTART = new AttributeName(ALL_NO_NS, SAME_LOCAL("restart"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ROWSPAN = new AttributeName(ALL_NO_NS, SAME_LOCAL("rowspan"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName SANDBOX = new AttributeName(ALL_NO_NS, SAME_LOCAL("sandbox"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName SUMMARY = new AttributeName(ALL_NO_NS, SAME_LOCAL("summary"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName STANDBY = new AttributeName(ALL_NO_NS, SAME_LOCAL("standby"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName REPLACE = new AttributeName(ALL_NO_NS, SAME_LOCAL("replace"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED);
    public static final AttributeName AUTOPLAY = new AttributeName(ALL_NO_NS, SAME_LOCAL("autoplay"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ADDITIVE = new AttributeName(ALL_NO_NS, SAME_LOCAL("additive"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName CALCMODE = new AttributeName(ALL_NO_NS, SVG_DIFFERENT("calcmode", "calcMode"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName CODETYPE = new AttributeName(ALL_NO_NS, SAME_LOCAL("codetype"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName CODEBASE = new AttributeName(ALL_NO_NS, SAME_LOCAL("codebase"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName CONTROLS = new AttributeName(ALL_NO_NS, SAME_LOCAL("controls"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName BEVELLED = new AttributeName(ALL_NO_NS, SAME_LOCAL("bevelled"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName BASELINE = new AttributeName(ALL_NO_NS, SAME_LOCAL("baseline"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName EXPONENT = new AttributeName(ALL_NO_NS, SAME_LOCAL("exponent"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName EDGEMODE = new AttributeName(ALL_NO_NS, SVG_DIFFERENT("edgemode", "edgeMode"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ENCODING = new AttributeName(ALL_NO_NS, SAME_LOCAL("encoding"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName GLYPHREF = new AttributeName(ALL_NO_NS, SVG_DIFFERENT("glyphref", "glyphRef"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName DATETIME = new AttributeName(ALL_NO_NS, SAME_LOCAL("datetime"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName DISABLED = new AttributeName(ALL_NO_NS, SAME_LOCAL("disabled"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED | BOOLEAN);
    public static final AttributeName FONTSIZE = new AttributeName(ALL_NO_NS, SAME_LOCAL("fontsize"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName KEYTIMES = new AttributeName(ALL_NO_NS, SVG_DIFFERENT("keytimes", "keyTimes"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName PANOSE_1 = new AttributeName(ALL_NO_NS, SAME_LOCAL("panose-1"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName HREFLANG = new AttributeName(ALL_NO_NS, SAME_LOCAL("hreflang"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONRESIZE = new AttributeName(ALL_NO_NS, SAME_LOCAL("onresize"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONCHANGE = new AttributeName(ALL_NO_NS, SAME_LOCAL("onchange"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONBOUNCE = new AttributeName(ALL_NO_NS, SAME_LOCAL("onbounce"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONUNLOAD = new AttributeName(ALL_NO_NS, SAME_LOCAL("onunload"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONFINISH = new AttributeName(ALL_NO_NS, SAME_LOCAL("onfinish"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONSCROLL = new AttributeName(ALL_NO_NS, SAME_LOCAL("onscroll"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName OPERATOR = new AttributeName(ALL_NO_NS, SAME_LOCAL("operator"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName OVERFLOW = new AttributeName(ALL_NO_NS, SAME_LOCAL("overflow"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONSUBMIT = new AttributeName(ALL_NO_NS, SAME_LOCAL("onsubmit"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONREPEAT = new AttributeName(ALL_NO_NS, SAME_LOCAL("onrepeat"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ONSELECT = new AttributeName(ALL_NO_NS, SAME_LOCAL("onselect"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName NOTATION = new AttributeName(ALL_NO_NS, SAME_LOCAL("notation"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName NORESIZE = new AttributeName(ALL_NO_NS, SAME_LOCAL("noresize"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED | BOOLEAN);
    public static final AttributeName MANIFEST = new AttributeName(ALL_NO_NS, SAME_LOCAL("manifest"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName MATHSIZE = new AttributeName(ALL_NO_NS, SAME_LOCAL("mathsize"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName MULTIPLE = new AttributeName(ALL_NO_NS, SAME_LOCAL("multiple"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED | BOOLEAN);
    public static final AttributeName LONGDESC = new AttributeName(ALL_NO_NS, SAME_LOCAL("longdesc"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName LANGUAGE = new AttributeName(ALL_NO_NS, SAME_LOCAL("language"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName TEMPLATE = new AttributeName(ALL_NO_NS, SAME_LOCAL("template"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName TABINDEX = new AttributeName(ALL_NO_NS, SAME_LOCAL("tabindex"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName READONLY = new AttributeName(ALL_NO_NS, SAME_LOCAL("readonly"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED | BOOLEAN);
    public static final AttributeName SELECTED = new AttributeName(ALL_NO_NS, SAME_LOCAL("selected"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG | CASE_FOLDED | BOOLEAN);
    public static final AttributeName ROWLINES = new AttributeName(ALL_NO_NS, SAME_LOCAL("rowlines"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName SEAMLESS = new AttributeName(ALL_NO_NS, SAME_LOCAL("seamless"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName ROWALIGN = new AttributeName(ALL_NO_NS, SAME_LOCAL("rowalign"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName STRETCHY = new AttributeName(ALL_NO_NS, SAME_LOCAL("stretchy"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAME_LANG);
    public static final AttributeName REQUIRED = new AttributeName(ALL_NO_NS, SAME_LOCAL("required"), ALL_NO_PREFIX, NCNAME_HTML | NCNAME_FOREIGN | NCNAM
