/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.citrus.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;

import com.alibaba.citrus.util.i18n.LocaleUtil;
import com.alibaba.citrus.util.internal.Entities;

/**
 * ??????????????????? Java?Java Script?HTML?XML??SQL??????
 *
 * @author Michael Zhou
 */
public class StringEscapeUtil {
    // ==========================================================================
    // Java?JavaScript?
    // ==========================================================================

    /**
     * ?Java????????????
     * <p>
     * ????????????<code>'\\'</code>????????tab????????<code>\t</code>?
     * </p>
     * <p>
     * Java?JavaScript??????????JavaScript????????????Java????
     * </p>
     * <p>
     * ???????<code>He didn't say, "Stop!"</code>????
     * <code>He didn't say, \"Stop!\"</code>
     * </p>
     *
     * @param str ???????
     * @return ???????????????<code>null</code>????<code>null</code>
     */
    public static String escapeJava(String str) {
        return escapeJavaStyleString(str, false, false);
    }

    /**
     * ?Java????????????
     * <p>
     * ????????????<code>'\\'</code>????????tab????????<code>\t</code>?
     * </p>
     * <p>
     * Java?JavaScript??????????JavaScript????????????Java????
     * </p>
     * <p>
     * ???????<code>He didn't say, "Stop!"</code>????
     * <code>He didn't say, \"Stop!\"</code>
     * </p>
     *
     * @param str    ???????
     * @param strict ?????????????
     * @return ???????????????<code>null</code>????<code>null</code>
     */
    public static String escapeJava(String str, boolean strict) {
        return escapeJavaStyleString(str, false, strict);
    }

    /**
     * ?Java????????????
     * <p>
     * ????????????<code>'\\'</code>????????tab????????<code>\t</code>?
     * </p>
     * <p>
     * Java?JavaScript??????????JavaScript????????????Java????
     * </p>
     * <p>
     * ???????<code>He didn't say, "Stop!"</code>????
     * <code>He didn't say, \"Stop!\"</code>
     * </p>
     *
     * @param str ???????
     * @param out ???
     * @throws IllegalArgumentException ??????<code>null</code>
     * @throws IOException              ??????
     */
    public static void escapeJava(String str, Appendable out) throws IOException {
        escapeJavaStyleString(str, false, out, false);
    }

    /**
     * ?Java????????????
     * <p>
     * ????????????<code>'\\'</code>????????tab????????<code>\t</code>?
     * </p>
     * <p>
     * Java?JavaScript??????????JavaScript????????????Java????
     * </p>
     * <p>
     * ???????<code>He didn't say, "Stop!"</code>????
     * <code>He didn't say, \"Stop!\"</code>
     * </p>
     *
     * @param str    ???????
     * @param out    ???
     * @param strict ?????????????
     * @throws IllegalArgumentException ??????<code>null</code>
     * @throws IOException              ??????
     */
    public static void escapeJava(String str, Appendable out, boolean strict) throws IOException {
        escapeJavaStyleString(str, false, out, strict);
    }

    /**
     * ?JavaScript????????????
     * <p>
     * ????????????????<code>'\\'</code>????????tab????????<code>\t</code>?
     * </p>
     * <p>
     * Java?JavaScript??????????JavaScript????????????Java????
     * </p>
     * <p>
     * ???????<code>He didn't say, "Stop!"</code>????
     * <code>He didn\'t say, \"Stop!\"</code>
     * </p>
     *
     * @param str ???????
     * @return ???????????????<code>null</code>????<code>null</code>
     */
    public static String escapeJavaScript(String str) {
        return escapeJavaStyleString(str, true, false);
    }

    /**
     * ?JavaScript????????????
     * <p>
     * ????????????????<code>'\\'</code>????????tab????????<code>\t</code>?
     * </p>
     * <p>
     * Java?JavaScript??????????JavaScript????????????Java????
     * </p>
     * <p>
     * ???????<code>He didn't say, "Stop!"</code>????
     * <code>He didn\'t say, \"Stop!\"</code>
     * </p>
     *
     * @param str    ???????
     * @param strict ?????????????
     * @return ???????????????<code>null</code>????<code>null</code>
     */
    public static String escapeJavaScript(String str, boolean strict) {
        return escapeJavaStyleString(str, true, strict);
    }

    /**
     * ?JavaScript????????????
     * <p>
     * ????????????????<code>'\\'</code>????????tab????????<code>\t</code>?
     * </p>
     * <p>
     * Java?JavaScript??????????JavaScript????????????Java????
     * </p>
     * <p>
     * ???????<code>He didn't say, "Stop!"</code>????
     * <code>He didn\'t say, \"Stop!\"</code>
     * </p>
     *
     * @param str ???????
     * @param out ???
     * @throws IllegalArgumentException ??????<code>null</code>
     * @throws IOException              ??????
     */
    public static void escapeJavaScript(String str, Appendable out) throws IOException {
        escapeJavaStyleString(str, true, out, false);
    }

    /**
     * ?JavaScript????????????
     * <p>
     * ????????????????<code>'\\'</code>????????tab????????<code>\t</code>?
     * </p>
     * <p>
     * Java?JavaScript??????????JavaScript????????????Java????
     * </p>
     * <p>
     * ???????<code>He didn't say, "Stop!"</code>????
     * <code>He didn\'t say, \"Stop!\"</code>
     * </p>
     *
     * @param str    ???????
     * @param out    ???
     * @param strict ?????????????
     * @throws IllegalArgumentException ??????<code>null</code>
     * @throws IOException              ??????
     */
    public static void escapeJavaScript(String str, Appendable out, boolean strict) throws IOException {
        escapeJavaStyleString(str, true, out, strict);
    }

    /**
     * ?Java?JavaScript????????????
     *
     * @param str        ???????
     * @param javascript ???????slash????
     * @param strict     ?????????????
     * @return ???????
     */
    private static String escapeJavaStyleString(String str, boolean javascript, boolean strict) {
        if (str == null) {
            return null;
        }

        try {
            StringBuilder out = new StringBuilder(str.length() * 2);

            if (escapeJavaStyleString(str, javascript, out, strict)) {
                return out.toString();
            }

            return str;
        } catch (IOException e) {
            return str; // StringBuilder?????????
        }
    }

    /**
     * ?Java?JavaScript????????????
     *
     * @param str        ???????
     * @param javascript ???????slash????
     * @param out        ???
     * @param strict     ?????????????
     * @return ?????????????<code>false</code>
     */
    private static boolean escapeJavaStyleString(String str, boolean javascript, Appendable out, boolean strict)
            throws IOException {
        boolean needToChange = false;

        if (out == null) {
            throw new IllegalArgumentException("The Appendable must not be null");
        }

        if (str == null) {
            return needToChange;
        }

        int length = str.length();

        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);

            if (ch < 32) {
                switch (ch) {
                    case '\b':
                        out.append('\\');
                        out.append('b');
                        break;

                    case '\n':
                        out.append('\\');
                        out.append('n');
                        break;

                    case '\t':
                        out.append('\\');
                        out.append('t');
                        break;

                    case '\f':
                        out.append('\\');
                        out.append('f');
                        break;

                    case '\r':
                        out.append('\\');
                        out.append('r');
                        break;

                    default:

                        if (ch > 0xf) {
                            out.append("\\u00" + Integer.toHexString(ch).toUpperCase());
                        } else {
                            out.append("\\u000" + Integer.toHexString(ch).toUpperCase());
                        }

                        break;
                }

                // ??????
                needToChange = true;
            } else if (strict && ch > 0xff) {
                if (ch > 0xfff) {
                    out.append("\\u").append(Integer.toHexString(ch).toUpperCase());
                } else {
                    out.append("\\u0").append(Integer.toHexString(ch).toUpperCase());
                }

                // ??????
                needToChange = true;
            } else {
                switch (ch) {
                    case '\'':
                    case '/': // ?????javascript??/??escape?????????

                        if (javascript) {
                            out.append('\\');

                            // ??????
                            needToChange = true;
                        }

                        out.append(ch);

                        break;

                    case '"':
                        out.append('\\');
                        out.append('"');

                        // ??????
                        needToChange = true;
                        break;

                    case '\\':
                        out.append('\\');
                        out.append('\\');

                        // ??????
                        needToChange = true;
                        break;

                    default:
                        out.append(ch);
                        break;
                }
            }
        }

        return needToChange;
    }

    /**
     * ?Java??????????????
     * <p>
     * <code>'\\'</code>????????????????<code>\t</code>?????tab???
     * </p>
     * <p>
     * ???????????????????
     * </p>
     *
     * @param str ???????????
     * @return ??????????????????<code>null</code>????<code>null</code>
     */
    public static String unescapeJava(String str) {
        return unescapeJavaStyleString(str);
    }

    /**
     * ?Java??????????????
     * <p>
     * <code>'\\'</code>????????????????<code>\t</code>?????tab???
     * </p>
     * <p>
     * ???????????????????
     * </p>
     *
     * @param str ??????????
     * @param out ???
     * @throws IllegalArgumentException ??????<code>null</code>
     * @throws IOException              ??????
     */
    public static void unescapeJava(String str, Appendable out) throws IOException {
        unescapeJavaStyleString(str, out);
    }

    /**
     * ?JavaScript??????????????
     * <p>
     * <code>'\\'</code>????????????????<code>\t</code>?????tab???
     * </p>
     * <p>
     * ???????????????????
     * </p>
     *
     * @param str ??????????
     * @return ??????????????????<code>null</code>????<code>null</code>
     */
    public static String unescapeJavaScript(String str) {
        return unescapeJavaStyleString(str);
    }

    /**
     * ?Java??????????????
     * <p>
     * <code>'\\'</code>????????????????<code>\t</code>?????tab???
     * </p>
     * <p>
     * ???????????????????
     * </p>
     *
     * @param str ??????????
     * @param out ???
     * @throws IllegalArgumentException ??????<code>null</code>
     * @throws IOException              ??????
     */
    public static void unescapeJavaScript(String str, Appendable out) throws IOException {
        unescapeJavaStyleString(str, out);
    }

    /**
     * ?Java??????????????
     * <p>
     * <code>'\\'</code>????????????????<code>\t</code>?????tab???
     * </p>
     * <p>
     * ???????????????????
     * </p>
     *
     * @param str ??????????
     * @return ???????????
     */
    private static String unescapeJavaStyleString(String str) {
        if (str == null) {
            return null;
        }

        try {
            StringBuilder out = new StringBuilder(str.length());

            if (unescapeJavaStyleString(str, out)) {
                return out.toString();
            }

            return str;
        } catch (IOException e) {
            return str; // StringBuilder?????????
        }
    }

    /**
     * ?Java??????????????
     * <p>
     * <code>'\\'</code>????????????????<code>\t</code>?????tab???
     * </p>
     * <p>
     * ???????????????????
     * </p>
     *
     * @param str ??????????
     * @param out ???
     * @return ?????????????<code>false</code>
     * @throws IllegalArgumentException ??????<code>null</code>
     * @throws IOException              ??????
     */
    private static boolean unescapeJavaStyleString(String str, Appendable out) throws IOException {
        boolean needToChange = false;

        if (out == null) {
            throw new IllegalArgumentException("The Appendable must not be null");
        }

        if (str == null) {
            return needToChange;
        }

        int length = str.length();
        StringBuilder unicode = new StringBuilder(4);
        boolean hadSlash = false;
        boolean inUnicode = false;

        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);

            if (inUnicode) {
                unicode.append(ch);

                if (unicode.length() == 4) {
                    String unicodeStr = unicode.toString();

                    try {
                        int value = Integer.parseInt(unicodeStr, 16);

                        out.append((char) value);
                        unicode.setLength(0);
                        inUnicode = false;
                        hadSlash = false;

                        // ??????
                        needToChange = true;
                    } catch (NumberFormatException e) {
                        out.append("\\u" + unicodeStr);
                    }
                }

                continue;
            }

            if (hadSlash) {
                hadSlash = false;

                switch (ch) {
                    case '\\':
                        out.append('\\');

                        // ??????
                        needToChange = true;
                        break;

                    case '\'':
                        out.append('\'');

                        // ??????
                        needToChange = true;
                        break;

                    case '\"':
                        out.append('"');

                        // ??????
                        needToChange = true;
                        break;

                    case 'r':
                        out.append('\r');

                        // ??????
                        needToChange = true;
                        break;

                    case 'f':
                        out.append('\f');

                        // ??????
                        needToChange = true;
                        break;

                    case 't':
                        out.append('\t');

                        // ??????
                        needToChange = true;
                        break;

                    case 'n':
                        out.append('\n');

                        // ??????
                        needToChange = true;
                        break;

                    case 'b':
                        out.append('\b');

                        // ??????
                        needToChange = true;
                        break;

                    case 'u': {
                        inUnicode = true;
                        break;
                    }

                    default:
                        out.append(ch);
                        break;
                }

                continue;
            } else if (ch == '\\') {
                hadSlash = true;
                continue;
            }

            out.append(ch);
        }

        if (hadSlash) {
            out.append('\\');
        }

        return needToChange;
    }

    // ==========================================================================
    // HTML?XML?
    // ==========================================================================

    /**
     * ??HTML??????????????????????
     * <p>
     * ???<code>"bread" & "butter"</code>?????
     * <tt>&amp;quot;bread&amp;quot; &amp;amp;
     * &amp;quot;butter&amp;quot;</tt>.
     * </p>
     * <p>
     * ????HTML 4.0 entities?
     * </p>
     *
     * @param str ???????
     * @return ??????????????????<code>null</code>????<code>null</code>
     * @see <a
     *      href="http://hotwired.lycos.com/webmonkey/reference/special_characters/">ISO
     *      Entities</a>
     * @see <a href="http://www.w3.org/TR/REC-html32#latin1">HTML 3.2 Character
     *      Entities for ISO Latin-1</a>
     * @see <a href="http://www.w3.org/TR/REC-html40/sgml/entities.html">HTML
     *      4.0 Character entity references</a>
     * @see <a href="http://www.w3.org/TR/html401/charset.html#h-5.3">HTML 4.01
     *      Character References</a>
     * @see <a
     *      href="http://www.w3.org/TR/html401/charset.html#code-position">HTML
     *      4.01 Code positions</a>
     */
    public static String escapeHtml(String str) {
        return escapeEntities(Entities.HTML40_MODIFIED, str);
    }

    /**
     * ??HTML??????????????????????
     * <p>
     * ???<code>"bread" & "butter"</code>?????
     * <tt>&amp;quot;bread&amp;quot; &amp;amp;
     * &amp;quot;butter&amp;quot;</tt>.
     * </p>
     * <p>
     * ????HTML 4.0 entities?
     * </p>
     *
     * @param str ???????
     * @param out ???
     * @throws IllegalArgumentException ??????<code>null</code>
     * @throws IOException              ??????
     * @see <a
     *      href="http://hotwired.lycos.com/webmonkey/reference/special_characters/">ISO
     *      Entities</a>
     * @see <a href="http://www.w3.org/TR/REC-html32#latin1">HTML 3.2 Character
     *      Entities for ISO Latin-1</a>
     * @see <a href="http://www.w3.org/TR/REC-html40/sgml/entities.html">HTML
     *      4.0 Character entity references</a>
     * @see <a href="http://www.w3.org/TR/html401/charset.html#h-5.3">HTML 4.01
     *      Character References</a>
     * @see <a
     *      href="http://www.w3.org/TR/html401/charset.html#code-position">HTML
     *      4.01 Code positions</a>
     */
    public static void escapeHtml(String str, Appendable out) throws IOException {
        escapeEntities(Entities.HTML40_MODIFIED, str, out);
    }

    /**
     * ??XML??????????????????????
     * <p>
     * ???<code>"bread" & "butter"</code>?????
     * <tt>&amp;quot;bread&amp;quot; &amp;amp;
     * &amp;quot;butter&amp;quot;</tt>.
     * </p>
     * <p>
     * ???4????XML???<code>gt</code>?<code>lt</code>?<code>quot</code>?
     * <code>amp</code>? ???DTD??????
     * </p>
     *
     * @param str ???????
     * @return ??????????????????<code>null</code>????<code>null</code>
     */
    public static String escapeXml(String str) {
        return escapeEntities(Entities.XML, str);
    }

    /**
     * ??XML??????????????????????
     * <p>
     * ???<code>"bread" & "butter"</code>?????
     * <tt>&amp;quot;bread&amp;quot; &amp;amp;
     * &amp;quot;butter&amp;quot;</tt>.
     * </p>
     * <p>
     * ???4????XML???<code>gt</code>?<code>lt</code>?<code>quot</code>?
     * <code>amp</code>? ???DTD??????
     * </p>
     *
     * @param str ???????
     * @param out ???
     * @throws IllegalArgumentException ??????<code>null</code>
     * @throws IOException              ??????
     */
    public static void escapeXml(String str, Appendable out) throws IOException {
        escapeEntities(Entities.XML, str, out);
    }

    /**
     * ??????????????????????????
     *
     * @param entities ????
     * @param str      ???????
     * @return ??????????????????<code>null</code>????<code>null</code>
     */
    public static String escapeEntities(Entities entities, String str) {
        if (str == null) {
            return null;
        }

        try {
            StringBuilder out = new StringBuilder(str.length());

            if (escapeEntitiesInternal(entities, str, out)) {
                return out.toString();
            }

            return str;
        } catch (IOException e) {
            return str; // StringBuilder?????????
        }
    }

    /**
     * ??????????????????????????
     *
     * @param entities ????
     * @param str      ???????
     * @param out      ???
     * @throws IllegalArgumentException ??????<code>null</code>
     * @throws IOException              ??????
     */
    public static void escapeEntities(Entities entities, String str, Appendable out) throws IOException {
        escapeEntitiesInternal(entities, str, out);
    }

    /**
     * ?HTML????????????????HTML 4.0?????????unicode???<code>&amp;#12345;</code>
     * ?
     * <p>
     * ???"&amp;lt;Fran&amp;ccedil;ais&amp;gt;"?????"&lt;Fran&ccedil;ais&gt;"
     * </p>
     * <p>
     * ??????????????????
     * </p>
     *
     * @param str ???????????
     * @return ??????????????????<code>null</code>????<code>null</code>
     */
    public static String unescapeHtml(String str) {
        return unescapeEntities(Entities.HTML40, str);
    }

    /**
     * ?HTML????????????????HTML 4.0?????????unicode???<code>&amp;#12345;</code>
     * ?
     * <p>
     * ???"&amp;lt;Fran&amp;ccedil;ais&amp;gt;"?????"&lt;Fran&ccedil;ais&gt;"
     * </p>
     * <p>
     * ??????????????????
     * </p>
     *
     * @param str ??????????
     * @param out ???
     * @throws IllegalArgumentException ??????<code>null</code>
     * @throws IOException              ??????
     */
    public static void unescapeHtml(String str, Appendable out) throws IOException {
        unescapeEntities(Entities.HTML40, str, out);
    }

    /**
     * ?XML????????????????unicode???<code>&amp;#12345;</code>?
     * <p>
     * ???"&amp;lt;Fran&amp;ccedil;ais&amp;gt;"?????"&lt;Fran&ccedil;ais&gt;"
     * </p>
     * <p>
     * ??????????????????
     * </p>
     *
     * @param str ???????????
     * @return ??????????????????<code>null</code>????<code>null</code>
     */
    public static String unescapeXml(String str) {
        return unescapeEntities(Entities.XML, str);
    }

    /**
     * ?XML????????????????unicode???<code>&amp;#12345;</code>?
     * <p>
     * ???"&amp;lt;Fran&amp;ccedil;ais&amp;gt;"?????"&lt;Fran&ccedil;ais&gt;"
     * </p>
     * <p>
     * ??????????????????
     * </p>
     *
     * @param str ???????????
     * @param out ???
     * @throws IllegalArgumentException ??????<code>null</code>
     * @throws IOException              ??????
     */
    public static void unescapeXml(String str, Appendable out) throws IOException {
        unescapeEntities(Entities.XML, str, out);
    }

    /**
     * ?????????????????
     *
     * @param entities ????
     * @param str      ???????????
     * @return ??????????????????<code>null</code>????<code>null</code>
     */
    public static String unescapeEntities(Entities entities, String str) {
        if (str == null) {
            return null;
        }

        try {
            StringBuilder out = new StringBuilder(str.length());

            if (unescapeEntitiesInternal(entities, str, out)) {
                return out.toString();
            }

            return str;
        } catch (IOException e) {
            return str; // StringBuilder?????????
        }
    }

    /**
     * ?????????????????
     * <p>
     * ??????????????????
     * </p>
     *
     * @param entities ????
     * @param str      ???????????
     * @param out      ???
     * @throws IllegalArgumentException ??????<code>null</code>
     * @throws IOException              ??????
     */
    public static void unescapeEntities(Entities entities, String str, Appendable out) throws IOException {
        unescapeEntitiesInternal(entities, str, out);
    }

    /**
     * ??????????????????
     *
     * @param entities ????
     * @param str      ???????
     * @param out      ?????????<code>null</code>
     * @return ?????????????<code>false</code>
     * @throws IllegalArgumentException ??<code>entities</code>?????
     *                                  <code>null</code>
     * @throws IOException              ??????
     */
    private static boolean escapeEntitiesInternal(Entities entities, String str, Appendable out) throws IOException {
        boolean needToChange = false;

        if (entities == null) {
            throw new IllegalArgumentException("The Entities must not be null");
        }

        if (out == null) {
            throw new IllegalArgumentException("The Appendable must not be null");
        }

        if (str == null) {
            return needToChange;
        }

        for (int i = 0; i < str.length(); ++i) {
            char ch = str.charAt(i);
            String entityName = entities.getEntityName(ch);

            if (entityName == null) {
                out.append(ch);
            } else {
                out.append('&');
                out.append(entityName);
                out.append(';');

                // ??????
                needToChange = true;
            }
        }

        return needToChange;
    }

    /**
     * ????????????unicode???<code>&amp;#12345;</code>??????unicode???
     * <p>
     * ????????????
     * </p>
     *
     * @param entities ????????<code>null</code>?????<code>&amp;#number</code>
     *                 ???
     * @param str      ??????????
     * @param out      ?????????<code>null</code>
     * @return ?????????????<code>false</code>
     * @throws IllegalArgumentException ??????<code>null</code>
     * @throws IOException              ??????
     */
    private static boolean unescapeEntitiesInternal(Entities entities, String str, Appendable out) throws IOException {
        boolean needToChange = false;

        if (out == null) {
            throw new IllegalArgumentException("The Appendable must not be null");
        }

        if (str == null) {
            return needToChange;
        }

        for (int i = 0; i < str.length(); ++i) {
            char ch = str.charAt(i);

            if (ch == '&') {
                // ??&xxxx;
                int semi = str.indexOf(';', i + 1);

                if (semi == -1 || i + 1 >= semi - 1) {
                    out.append(ch);
                    continue;
                }

                // ???&#xxxxx;
                if (str.charAt(i + 1) == '#') {
                    int firstCharIndex = i + 2;
                    int radix = 10;

                    if (firstCharIndex >= semi - 1) {
                        out.append(ch);
                        out.append('#');
                        i++;
                        continue;
                    }

                    char firstChar = str.charAt(firstCharIndex);

                    if (firstChar == 'x' || firstChar == 'X') {
                        firstCharIndex++;
                        radix = 16;

                        if (firstCharIndex >= semi - 1) {
                            out.append(ch);
                            out.append('#');
                            i++;
                            continue;
                        }
                    }

                    try {
                        int entityValue = Integer.parseInt(str.substring(firstCharIndex, semi), radix);

                        out.append((char) entityValue);

                        // ??????
                        needToChange = true;
                    } catch (NumberFormatException e) {
                        out.append(ch);
                        out.append('#');
                        i++;
                        continue;
                    }
                } else {
                    String entityName = str.substring(i + 1, semi);
                    int entityValue = -1;

                    if (entities != null) {
                        entityValue = entities.getEntityValue(entityName);
                    }

                    if (entityValue == -1) {
                        out.append('&');
                        out.append(entityName);
                        out.append(';');
                    } else {
                        out.append((char) entityValue);

                        // ??????
                        needToChange = true;
                    }
                }

                i = semi;
            } else {
                out.append(ch);
            }
        }

        return needToChange;
    }

    // ==========================================================================
    // SQL???
    // ==========================================================================

    /**
     * ?SQL??????????????
     * <p>
     * ???
     * <p/>
     * <pre>
     * statement.executeQuery(&quot;SELECT * FROM MOVIES WHERE TITLE='&quot; + StringEscapeUtil.escapeSql(&quot;McHale's Navy&quot;) + &quot;'&quot;);
     * </pre>
     * <p/>
     * </p>
     * <p>
     * ????????????????????<code>"McHale's Navy"</code>???<code>"McHale''s
     * Navy"</code>???????????<code>%</code>?<code>_</code>???
     * </p>
     *
     * @param str ???????
     * @return ???????????????<code>null</code>????<code>null</code>
     * @see <a href="http://www.jguru.com/faq/view.jsp?EID=8881">faq</a>
     */
    public static String escapeSql(String str) {
        return StringUtil.replace(str, "'", "''");
    }

    /**
     * ?SQL??????????????
     * <p>
     * ???
     * <p/>
     * <pre>
     * statement.executeQuery(&quot;SELECT * FROM MOVIES WHERE TITLE='&quot; + StringEscapeUtil.escapeSql(&quot;McHale's Navy&quot;) + &quot;'&quot;);
     * </pre>
     * <p/>
     * </p>
     * <p>
     * ????????????????????<code>"McHale's Navy"</code>???<code>"McHale''s
     * Navy"</code>???????????<code>%</code>?<code>_</code>???
     * </p>
     *
     * @param str ???????
     * @param out ???
     * @throws IllegalArgumentException ??????<code>null</code>
     * @throws IOException              ??????
     * @see <a href="http://www.jguru.com/faq/view.jsp?EID=8881">faq</a>
     */
    public static void escapeSql(String str, Appendable out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("The Appendable must not be null");
        }

        String result = StringUtil.replace(str, "'", "''");

        if (result != null) {
            out.append(result);
        }
    }

    // ==========================================================================
    // URL/URI encoding/decoding?
    // ??RFC2396?http://www.ietf.org/rfc/rfc2396.txt
    // ==========================================================================

    /** "Alpha" characters from RFC 2396. */
    private static final BitSet ALPHA = new BitSet(256);

    static {
        for (int i = 'a'; i <= 'z'; i++) {
            ALPHA.set(i);
        }

        for (int i = 'A'; i <= 'Z'; i++) {
            ALPHA.set(i);
        }
    }

    /** "Alphanum" characters from RFC 2396. */
    private static final BitSet ALPHANUM = new BitSet(256);

    static {
        ALPHANUM.or(ALPHA);

        for (int i = '0'; i <= '9'; i++) {
            ALPHANUM.set(i);
        }
    }

    /** "Mark" characters from RFC 2396. */
    private static final BitSet MARK = new BitSet(256);

    static {
        MARK.set('-');
        MARK.set('_');
        MARK.set('.');
        MARK.set('!');
        MARK.set('~');
        MARK.set('*');
        MARK.set('\'');
        MARK.set('(');
        MARK.set(')');
    }

    /** "Reserved" characters from RFC 2396. */
    private static final BitSet RESERVED = new BitSet(256);

    static {
        RESERVED.set(';');
        RESERVED.set('/');
        RESERVED.set('?');
        RESERVED.set(':');
        RESERVED.set('@');
        RESERVED.set('&');
        RESERVED.set('=');
        RESERVED.set('+');
        RESERVED.set('$');
        RESERVED.set(',');
    }

    /** "Unreserved" characters from RFC 2396. */
    private static final BitSet UNRESERVED = new BitSet(256);

    static {
        UNRESERVED.or(ALPHANUM);
        UNRESERVED.or(MARK);
    }

    /** ????????16??????? */
    private static char[] HEXADECIMAL = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
                                          'F' };

    /**
     * ?????????<code>application/x-www-form-urlencoded</code>???
     * <p>
     * ??RFC2396??<code>unreserved</code>????????????????URL??<code>%xx</code>?
     * ??RFC2396?<code>unreserved</code>??????
     * <p/>
     * <pre>
     * &lt;![CDATA
     *  unreserved  = alphanum | mark
     *  alphanum    = ??????? | ??
     *  mark        = &quot;-&quot; | &quot;_&quot; | &quot;.&quot; | &quot;!&quot; | &quot;&tilde;&quot; | &quot;*&quot; | &quot;'&quot; | &quot;(&quot; | &quot;)&quot;
     * ]]&gt;
     * </pre>
     * <p/>
     * </p>
     * <p>
     * ??????????????????????URL?????????????????????????
     * </p>
     *
     * @param str ???????????<code>null</code>
     * @return URL???????
     */
    public static String escapeURL(String str) {
        try {
            return escapeURLInternal(str, null, true);
        } catch (UnsupportedEncodingException e) {
            return str; // ?????????
        }
    }

    /**
     * ?????????<code>application/x-www-form-urlencoded</code>???
     * <p>
     * ??RFC2396??<code>unreserved</code>????????????????URL??<code>%xx</code>?
     * ??RFC2396?<code>unreserved</code>??????
     * <p/>
     * <pre>
     * &lt;![CDATA
     *  unreserved  = alphanum | mark
     *  alphanum    = ??????? | ??
     *  mark        = &quot;-&quot; | &quot;_&quot; | &quot;.&quot; | &quot;!&quot; | &quot;&tilde;&quot; | &quot;*&quot; | &quot;'&quot; | &quot;(&quot; | &quot;)&quot;
     * ]]&gt;
     * </pre>
     * <p/>
     * </p>
     * <p>
     * ???????????????URL?
     * </p>
     *
     * @param str      ???????????<code>null</code>
     * @param encoding ??????????<code>null</code>??????????
     * @return URL???????
     * @throws UnsupportedEncodingException ?????<code>encoding</code>????
     */
    public static String escapeURL(String str, String encoding) throws UnsupportedEncodingException {
        return escapeURLInternal(str, encoding, true);
    }

    /**
     * ?????????<code>application/x-www-form-urlencoded</code>???
     * <p>
     * ??????<code>strict</code>?<code>true</code>??????????URL? ??RFC2396??
     * <code>unreserved</code>????????????????URL??<code>%xx</code>? ??RFC2396?
     * <code>unreserved</code>??????
     * <p/>
     * <pre>
     * &lt;![CDATA
     *  unreserved  = alphanum | mark
     *  alphanum    = ??????? | ??
     *  mark        = &quot;-&quot; | &quot;_&quot; | &quot;.&quot; | &quot;!&quot; | &quot;&tilde;&quot; | &quot;*&quot; | &quot;'&quot; | &quot;(&quot; | &quot;)&quot;
     * ]]&gt;
     * </pre>
     * <p/>
     * </p>
     * <p>
     * ??????<code>strict</code>?<code>false</code>???????????URL?
     * ?????????????RFC2396??<code>reserved</code>??????????????????
     * ??RFC2396??????????????????<code>reserved</code>??????????
     * <code>%xx</code>???
     * <p/>
     * <pre>
     * &lt;![CDATA
     *  reserved      = &quot;;&quot; | &quot;/&quot; | &quot;?&quot; | &quot;:&quot; | &quot;@&quot; | &quot;&amp;&quot; | &quot;=&quot; | &quot;+&quot; | &quot;$&quot; | &quot;,&quot;
     * ]]&gt;
     * </pre>
     * <p/>
     * </p>
     * <p>
     * ???????????????URL?
     * </p>
     *
     * @param str      ???????????<code>null</code>
     * @param encoding ??????????<code>null</code>?????????????
     * @param strict   ??????????URL
     * @return URL???????
     * @throws UnsupportedEncodingException ?????<code>encoding</code>????
     */
    public static String escapeURL(String str, String encoding, boolean strict) throws UnsupportedEncodingException {
        return escapeURLInternal(str, encoding, strict);
    }

    /**
     * ?????????<code>application/x-www-form-urlencoded</code>???
     * <p>
     * ??RFC2396??<code>unreserved</code>????????????????URL??<code>%xx</code>?
     * ??RFC2396?<code>unreserved</code>??????
     * <p/>
     * <pre>
     * &lt;![CDATA
     *  unreserved  = alphanum | mark
     *  alphanum    = ??????? | ??
     *  mark        = &quot;-&quot; | &quot;_&quot; | &quot;.&quot; | &quot;!&quot; | &quot;&tilde;&quot; | &quot;*&quot; | &quot;'&quot; | &quot;(&quot; | &quot;)&quot;
     * ]]&gt;
     * </pre>
     * <p/>
     * </p>
     * <p>
     * ???????????????URL?
     * </p>
     *
     * @param str      ???????????<code>null</code>
     * @param encoding ??????????<code>null</code>??????????
     * @param out      ????????
     * @throws IOException                  ?????<code>out</code>??
     * @throws UnsupportedEncodingException ?????<code>encoding</code>????
     * @throws IllegalArgumentException     <code>out</code>?<code>null</code>
     */
    public static void escapeURL(String str, String encoding, Appendable out) throws IOException {
        escapeURLInternal(str, encoding, out, true);
    }

    /**
     * ?????????<code>application/x-www-form-urlencoded</code>???
     * <p>
     * ??????<code>strict</code>?<code>true</code>??????????URL? ??RFC2396??
     * <code>unreserved</code>????????????????URL??<code>%xx</code>? ??RFC2396?
     * <code>unreserved</code>??????
     * <p/>
     * <pre>
     * &lt;![CDATA
     *  unreserved  = alphanum | mark
     *  alphanum    = ??????? | ??
     *  mark        = &quot;-&quot; | &quot;_&quot; | &quot;.&quot; | &quot;!&quot; | &quot;&tilde;&quot; | &quot;*&quot; | &quot;'&quot; | &quot;(&quot; | &quot;)&quot;
     * ]]&gt;
     * </pre>
     * <p/>
     * </p>
     * <p>
     * ??????<code>strict</code>?<code>false</code>???????????URL?
     * ?????????????RFC2396??<code>reserved</code>??????????????????
     * ??RFC2396??????????????????<code>reserved</code>??????????
     * <code>%xx</code>???
     * <p/>
     * <pre>
     * &lt;![CDATA
     *  reserved      = &quot;;&quot; | &quot;/&quot; | &quot;?&quot; | &quot;:&quot; | &quot;@&quot; | &quot;&amp;&quot; | &quot;=&quot; | &quot;+&quot; | &quot;$&quot; | &quot;,&quot;
     * ]]&gt;
     * </pre>
     * <p/>
     * </p>
     * <p>
     * ???????????????URL?
     * </p>
     *
     * @param str      ???????????<code>null</code>
     * @param encoding ??????????<code>null</code>??????????
     * @param out      ????????
     * @param strict   ??????????URL
     * @throws IOException                  ?????<code>out</code>??
     * @throws UnsupportedEncodingException ?????<code>encoding</code>????
     * @throws IllegalArgumentException     <code>out</code>?<code>null</code>
     */
    public static void escapeURL(String str, String encoding, Appendable out, boolean strict) throws IOException {
        escapeURLInternal(str, encoding, out, strict);
    }

    /**
     * ?????????<code>application/x-www-form-urlencoded</code>???
     *
     * @param str      ???????????<code>null</code>
     * @param encoding ??????????<code>null</code>??????????
     * @param strict   ??????????URL
     * @return URL???????
     * @throws UnsupportedEncodingException ?????<code>encoding</code>????
     */
    private static String escapeURLInternal(String str, String encoding, boolean strict)
            throws UnsupportedEncodingException {
        if (str == null) {
            return null;
        }

        try {
            StringBuilder out = new StringBuilder(64);

            if (escapeURLInternal(str, encoding, out, strict)) {
                return out.toString();
            }

            return str;
        } catch (UnsupportedEncodingException e) {
            throw e;
        } catch (IOException e) {
            return str; // StringBuilder?????????
        }
    }

    /**
     * ?????????<code>application/x-www-form-urlencoded</code>???
     *
     * @param str      ???????????<code>null</code>
     * @param encoding ??????????<code>null</code>??????????
     * @param strict   ??????????URL
     * @param out      ???
     * @return ????????????<code>true</code>
     * @throws IOException                  ?????<code>out</code>??
     * @throws UnsupportedEncodingException ?????<code>encoding</code>????
     * @throws IllegalArgumentException     <code>out</code>?<code>null</code>
     */
    private static boolean escapeURLInternal(String str, String encoding, Appendable out, boolean strict)
            throws IOException {
        if (encoding == null) {
            encoding = LocaleUtil.getContext().getCharset().name();
        }

        boolean needToChange = false;

        if (out == null) {
            throw new IllegalArgumentException("The Appendable must not be null");
        }

        if (str == null) {
            return needToChange;
        }

        char[] charArray = str.toCharArray();
        int length = charArray.length;

        for (int i = 0; i < length; i++) {
            int ch = charArray[i];

            if (isSafeCharacter(ch, strict)) {
                // ??????????
                out.append((char) ch);
            } else if (ch == ' ') {
                // ????????0x20????'+'
                out.append('+');

                // ??????
                needToChange = true;
            } else {
                // ?ch??URL???
                // ?????encoding??????????
                byte[] bytes = String.valueOf((char) ch).getBytes(encoding);

                for (byte toEscape : bytes) {
                    out.append('%');

                    int low = toEscape & 0x0F;
                    int high = (toEscape & 0xF0) >> 4;

                    out.append(HEXADECIMAL[high]);
                    out.append(HEXADECIMAL[low]);
                }

                // ??????
                needToChange = true;
            }
        }

        return needToChange;
    }

    /**
     * ???????????????????????URL???
     *
     * @param ch     ??????
     * @param strict ??????????
     * @return ??????????<code>true</code>
     */
    private static boolean isSafeCharacter(int ch, boolean strict) {
        if (strict) {
            return UNRESERVED.get(ch);
        } else {
            return ch > ' ' && !RESERVED.get(ch) && !Character.isWhitespace((char) ch);
        }
    }

    /**
     * ??<code>application/x-www-form-urlencoded</code>???????
     * <p>
     * ?????????????????URL????????????????????????
     * </p>
     *
     * @param str ???????????<code>null</code>
     * @return URL???????
     */
    public static String unescapeURL(String str) {
        try {
            return unescapeURLInternal(str, null);
        } catch (UnsupportedEncodingException e) {
            return str; // ?????????
        }
    }

    /**
     * ??<code>application/x-www-form-urlencoded</code>???????
     *
     * @param str      ???????????<code>null</code>
     * @param encoding ??????????<code>null</code>??????????
     * @return URL???????
     * @throws UnsupportedEncodingException ?????<code>encoding</code>????
     */
    public static String unescapeURL(String str, String encoding) throws UnsupportedEncodingException {
        return unescapeURLInternal(str, encoding);
    }

    /**
     * ??<code>application/x-www-form-urlencoded</code>???????
     *
     * @param str      ???????????<code>null</code>
     * @param encoding ??????????<code>null</code>??????????
     * @param out      ???
     * @throws IOException                  ?????<code>out</code>??
     * @throws UnsupportedEncodingException ?????<code>encoding</code>????
     * @throws IllegalArgumentException     <code>out</code>?<code>null</code>
     */
    public static void unescapeURL(String str, String encoding, Appendable out) throws IOException {
        unescapeURLInternal(str, encoding, out);
    }

    /**
     * ??<code>application/x-www-form-urlencoded</code>???????
     *
     * @param str      ???????????<code>null</code>
     * @param encoding ??????????<code>null</code>??????????
     * @return URL???????
     * @throws UnsupportedEncodingException ?????<code>encoding</code>????
     */
    private static String unescapeURLInternal(String str, String encoding) throws UnsupportedEncodingException {
        if (str == null) {
            return null;
        }

        try {
            StringBuilder out = new StringBuilder(str.length());

            if (unescapeURLInternal(str, encoding, out)) {
                return out.toString();
            }

            return str;
        } catch (UnsupportedEncodingException e) {
            throw e;
        } catch (IOException e) {
            return str; // StringBuilder?????????
        }
    }

    /**
     * ??<code>application/x-www-form-urlencoded</code>???????
     *
     * @param str      ???????????<code>null</code>
     * @param encoding ??????????<code>null</code>??????????
     * @param out      ???
     * @return ????????????<code>true</code>
     * @throws IOException                  ?????<code>out</code>??
     * @throws UnsupportedEncodingException ?????<code>encoding</code>????
     * @throws IllegalArgumentException     <code>out</code>?<code>null</code>
     */
    private static boolean unescapeURLInternal(String str, String encoding, Appendable out) throws IOException {
        if (encoding == null) {
            encoding = LocaleUtil.getContext().getCharset().name();
        }

        boolean needToChange = false;

        if (out == null) {
            throw new IllegalArgumentException("The Appendable must not be null");
        }

        byte[] buffer = null;
        int pos = 0;
        int startIndex = 0;

        char[] charArray = str.toCharArray();
        int length = charArray.length;

        for (int i = 0; i < length; i++) {
            int ch = charArray[i];

            if (ch < 256) {
                // ??????????????????????
                if (buffer == null) {
                    buffer = new byte[length - i]; // ?????length - i
                }

                if (pos == 0) {
                    startIndex = i;
                }

                switch (ch) {
                    case '+':

                        // ?'+'???' '
                        buffer[pos++] = ' ';

                        // ??????
                        needToChange = true;
                        break;

                    case '%':

                        if (i + 2 < length) {
                            try {
                                byte b = (byte) Integer.parseInt(str.substring(i + 1, i + 3), 16);

                                buffer[pos++] = b;
                                i += 2;

                                // ??????
                                needToChange = true;
                            } catch (NumberFormatException e) {
                                // ??%xx?????16?????????
                                buffer[pos++] = (byte) ch;
                            }
                        } else {
                            buffer[pos++] = (byte) ch;
                        }

                        break;

                    default:

                        // ??bytes?????????
                        buffer[pos++] = (byte) ch;
                        break;
                }
            } else {
                // ??buffer????????????
                if (pos > 0) {
                    String s = new String(buffer, 0, pos, encoding);

                    out.append(s);

                    if (!needToChange && !s.equals(new String(charArray, startIndex, pos))) {
                        needToChange = true;
                    }

                    pos = 0;
                }

                // ??ch?ISO-8859-1????????????
                out.append((char) ch);
            }
        }

        // ??buffer????????????
        if (pos > 0) {
            String s = new String(buffer, 0, pos, encoding);

            out.append(s);

            if (!needToChange && !s.equals(new String(charArray, startIndex, pos))) {
                needToChange = true;
            }

            pos = 0;
        }

        return needToChange;
    }
}

