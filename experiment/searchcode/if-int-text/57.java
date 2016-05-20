/* XML Im-/Exporter: Copyright 2002-2004, Oliver Zeigermann (oliver@zeigermann.de)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this list of
 *   conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list
 *   of conditions and the following disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 * - Neither the name of the Oliver Zeigermann nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without specific
 *   prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, ORTORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */ 
package wsl.fw.xml;

/**
 * Collection of XML encoding/decoding helpers. <br>
 * This is all about the special characters &amp; and &lt;, and for attributes
 * &quot; and &apos;. These must be encoded/decoded from/to XML.
 *
 * @author <a href="mailto:oliver@zeigermann.de">Olli Z.</a>
 */
public final class XMLEncode {

    private final static int CDATA_BLOCK_THRESHOLD_LENGTH = 12;
    private final static char DEFAULT_QUOTE_CHAR = '"';

    /** Checks if this text purely consists of the white space characters
     * ' ',  TAB, NEWLINE.
     */
    public final static boolean isWhiteSpace(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    /** Makes any text fit into XML attributes. */
    public final static String xmlEncodeTextForAttribute(String text, char quoteChar) {
        if (text == null)
            return null;
        return xmlEncodeTextAsPCDATA(text, true, quoteChar);
    }

    /** Encodes text as XML in the most suitable way, either CDATA block or PCDATA. */
    public final static String xmlEncodeText(String text) {
        if (text == null)
            return null;
        if (!needsEncoding(text)) {
            return text;
        } else {
            // only encode as cdata if is is longer than CDATA block overhead:
            if (text.length() > CDATA_BLOCK_THRESHOLD_LENGTH) {
                String cdata = xmlEncodeTextAsCDATABlock(text);
                if (cdata != null) {
                    return cdata;
                }
            }
        }
        // if every thing else fails, do it the save way...
        return xmlEncodeTextAsPCDATA(text);
    }

    /** Encodes any text as PCDATA. */
    public final static String xmlEncodeTextAsPCDATA(String text) {
        if (text == null)
            return null;
        return xmlEncodeTextAsPCDATA(text, false);
    }

    /** Encodes any text as PCDATA. 
     * @param forAttribute if you want
     * quotes and apostrophes specially treated for attributes
     */
    public final static String xmlEncodeTextAsPCDATA(String text, boolean forAttribute) {
        return xmlEncodeTextAsPCDATA(text, forAttribute, DEFAULT_QUOTE_CHAR);
    }

    /** Encodes any text as PCDATA. 
     * @param forAttribute if you want
     * quotes and apostrophes specially treated for attributes
     * @param quoteChar if this is for attributes this <code>char</code> is used to quote the attribute value
     */
    public final static String xmlEncodeTextAsPCDATA(String text, boolean forAttribute, char quoteChar) {
        if (text == null)
            return null;
        char c;
        StringBuffer n = new StringBuffer(text.length() * 2);
        for (int i = 0; i < text.length(); i++) {
            c = text.charAt(i);
            switch (c) {
                case '&' :
                    n.append("&amp;");
                    break;
                case '<' :
                    n.append("&lt;");
                    break;
                case '>' : // FIX for sourceforge bug #802520 ("]]>" needs encoding)
                    n.append("&gt;");
                    break;
                case '"' :
                    if (forAttribute)
                        n.append("&quot;");
                    else
                        n.append(c);
                    break;
                case '\'' :
                    if (forAttribute)
                        n.append("&apos;");
                    else
                        n.append(c);
                    break;
                default :
                    {
                        n.append(c);
                        break;
                    }
            }
        }

        if (forAttribute) {
            n.append(quoteChar);
            n.insert(0, quoteChar);
        }

        return n.toString();
    }

    /** Returns string as CDATA block if possible, otherwise null. */
    public final static String xmlEncodeTextAsCDATABlock(String text) {
        if (text == null)
            return null;
        if (isCompatibleWithCDATABlock(text)) {
            return "<![CDATA[" + text + "]]>";
        } else {
            return null;
        }
    }

    /** Checks if this text needs encoding in order to be represented in XML. */
    public final static boolean needsEncoding(String text) {
        return needsEncoding(text, false);
    }

    /** Checks if this text needs encoding in order to be represented in XML.
     * 
     * Set <code>checkForAttr</code> if you want to check for storability in 
     * an attribute. 
     */
    public final static boolean needsEncoding(String data, boolean checkForAttr) {
        if (data == null)
            return false;
        char c;
        for (int i = 0; i < data.length(); i++) {
            c = data.charAt(i);
            if (c == '&' || c == '<' || (checkForAttr && (c == '"' || c == '\'')))
                return true;
        }
        return false;
    }

    /** Can this text be stored into a CDATA block? */
    public final static boolean isCompatibleWithCDATABlock(String text) {
        if (text == null)
            return false;
        return (text.indexOf("]]>") == -1);
    }

    /** Make CDATA out of possibly encoded PCDATA. <br>
     * E.g. make '&amp;' out of '&amp;amp;'
     */
    public final static String xmlDecodeTextToCDATA(String pcdata) {
        if (pcdata == null)
            return null;
        char c, c1, c2, c3, c4, c5;
        StringBuffer n = new StringBuffer(pcdata.length());
        for (int i = 0; i < pcdata.length(); i++) {
            c = pcdata.charAt(i);
            if (c == '&') {
                c1 = lookAhead(1, i, pcdata);
                c2 = lookAhead(2, i, pcdata);
                c3 = lookAhead(3, i, pcdata);
                c4 = lookAhead(4, i, pcdata);
                c5 = lookAhead(5, i, pcdata);

                if (c1 == 'a' && c2 == 'm' && c3 == 'p' && c4 == ';') {
                    n.append("&");
                    i += 4;
                } else if (c1 == 'l' && c2 == 't' && c3 == ';') {
                    n.append("<");
                    i += 3;
                } else if (c1 == 'g' && c2 == 't' && c3 == ';') {
                    n.append(">");
                    i += 3;
                } else if (c1 == 'q' && c2 == 'u' && c3 == 'o' && c4 == 't' && c5 == ';') {
                    n.append("\"");
                    i += 5;
                } else if (c1 == 'a' && c2 == 'p' && c3 == 'o' && c4 == 's' && c5 == ';') {
                    n.append("'");
                    i += 5;
                } else
                    n.append("&");
            } else
                n.append(c);
        }
        return n.toString();
    }

    private final static char lookAhead(int la, int offset, String data) {
        try {
            return data.charAt(offset + la);
        } catch (StringIndexOutOfBoundsException e) {
            return 0x0;
        }
    }

    // combine multiple checks in one methods for speed
    private final static boolean contains(String text, char[] chars) {
        if (text == null || chars == null || chars.length == 0) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            for (int j = 0; j < chars.length; j++) {
                if (chars[j] == c) {
                    return true;
                }
            }
        }
        return false;
    }

}

