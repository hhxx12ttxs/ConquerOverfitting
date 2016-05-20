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

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;

import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ????????????
 * <p>
 * ?????????????????<code>null</code>??????<code>NullPointerException</code>?
 * </p>
 *
 * @author Michael Zhou
 */
public class StringUtil {
    // ==========================================================================
    // ?????
    //
    // ??????????isEquals??????ObjectUtil.isEquals?
    // ==========================================================================

    /**
     * ?????????
     *
     * @param str ????????
     * @return ??????<code>null</code>????<code>0</code>????????????
     */
    public static int getLength(String str) {
        return str == null ? 0 : str.length();
    }

    /**
     * ????????????????
     * <p/>
     * <pre>
     * StringUtil.equalsIgnoreCase(null, null)   = true
     * StringUtil.equalsIgnoreCase(null, "abc")  = false
     * StringUtil.equalsIgnoreCase("abc", null)  = false
     * StringUtil.equalsIgnoreCase("abc", "abc") = true
     * StringUtil.equalsIgnoreCase("abc", "ABC") = true
     * </pre>
     *
     * @param str1 ???????1
     * @param str2 ???????2
     * @return ??????????????<code>null</code>????<code>true</code>
     */
    public static boolean isEqualsIgnoreCase(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }

        return str1.equalsIgnoreCase(str2);
    }

    // ==========================================================================
    // ?????
    //
    // ?????????????????
    // 1. null
    // 2. empty - ""
    // 3. blank - "?????" - ???Character.isWhitespace????
    // ==========================================================================

    /**
     * ????????<code>null</code>?????<code>""</code>?
     * <p/>
     * <pre>
     * StringUtil.isEmpty(null)      = true
     * StringUtil.isEmpty("")        = true
     * StringUtil.isEmpty(" ")       = false
     * StringUtil.isEmpty("bob")     = false
     * StringUtil.isEmpty("  bob  ") = false
     * </pre>
     *
     * @param str ???????
     * @return ????, ???<code>true</code>
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * ???????????<code>null</code>?????<code>""</code>????????
     * <p/>
     * <pre>
     * StringUtil.isBlank(null)      = true
     * StringUtil.isBlank("")        = true
     * StringUtil.isBlank(" ")       = true
     * StringUtil.isBlank("bob")     = false
     * StringUtil.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str ???????
     * @return ?????, ???<code>true</code>
     */
    public static boolean isBlank(String str) {
        int length;

        if (str == null || (length = str.length()) == 0) {
            return true;
        }

        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    // ==========================================================================
    // ??????
    //
    // ?????empty?blank??????????????????
    // ????????null????????ObjectUtil.defaultIfNull?
    // ==========================================================================

    /**
     * ??????<code>null</code>?????<code>""</code>??????????????????????
     * <p/>
     * <pre>
     * StringUtil.defaultIfEmpty(null, "default")  = "default"
     * StringUtil.defaultIfEmpty("", "default")    = "default"
     * StringUtil.defaultIfEmpty("  ", "default")  = "  "
     * StringUtil.defaultIfEmpty("bat", "default") = "bat"
     * </pre>
     *
     * @param str        ???????
     * @param defaultStr ?????
     * @return ??????????????
     */
    public static String defaultIfEmpty(String str, String defaultStr) {
        return str == null || str.length() == 0 ? defaultStr : str;
    }

    /**
     * ??????<code>null</code>?????<code>""</code>??????????????????????
     * <p/>
     * <pre>
     * StringUtil.defaultIfBlank(null, "default")  = "default"
     * StringUtil.defaultIfBlank("", "default")    = "default"
     * StringUtil.defaultIfBlank("  ", "default")  = "default"
     * StringUtil.defaultIfBlank("bat", "default") = "bat"
     * </pre>
     *
     * @param str        ???????
     * @param defaultStr ?????
     * @return ??????????????
     */
    public static String defaultIfBlank(String str, String defaultStr) {
        return isBlank(str) ? defaultStr : str;
    }

    // ==========================================================================
    // ???????
    //
    // ??????????????????
    // ==========================================================================

    /**
     * ??????????????????<code>null</code>?????<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.trim(null)          = null
     * StringUtil.trim("")            = ""
     * StringUtil.trim("     ")       = ""
     * StringUtil.trim("abc")         = "abc"
     * StringUtil.trim("    abc    ") = "abc"
     * </pre>
     *
     * @param str ???????
     * @return ???????????????<code>null</code>????<code>null</code>
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * ????????????????????????<code>""</code>????<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.trimToNull(null)          = null
     * StringUtil.trimToNull("")            = null
     * StringUtil.trimToNull("     ")       = null
     * StringUtil.trimToNull("abc")         = "abc"
     * StringUtil.trimToNull("    abc    ") = "abc"
     * </pre>
     *
     * @param str ???????
     * @return ???????????????<code>null</code>???????<code>""</code>????
     *         <code>null</code>
     */
    public static String trimToNull(String str) {
        if (str == null) {
            return null;
        }

        String result = str.trim();

        if (result == null || result.length() == 0) {
            return null;
        }

        return result;
    }

    /**
     * ??????????????????<code>null</code>????????<code>""</code>?
     * <p/>
     * <pre>
     * StringUtil.trimToEmpty(null)          = ""
     * StringUtil.trimToEmpty("")            = ""
     * StringUtil.trimToEmpty("     ")       = ""
     * StringUtil.trimToEmpty("abc")         = "abc"
     * StringUtil.trimToEmpty("    abc    ") = "abc"
     * </pre>
     *
     * @param str ???????
     * @return ???????????????<code>null</code>???????<code>""</code>????
     *         <code>null</code>
     */
    public static String trimToEmpty(String str) {
        if (str == null) {
            return EMPTY_STRING;
        }

        return str.trim();
    }

    /**
     * ????????????????????<code>null</code>?????<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.trim(null, *)          = null
     * StringUtil.trim("", *)            = ""
     * StringUtil.trim("abc", null)      = "abc"
     * StringUtil.trim("  abc", null)    = "abc"
     * StringUtil.trim("abc  ", null)    = "abc"
     * StringUtil.trim(" abc ", null)    = "abc"
     * StringUtil.trim("  abcyx", "xyz") = "  abc"
     * </pre>
     *
     * @param str        ???????
     * @param stripChars ??????????<code>null</code>????????
     * @return ???????????????????<code>null</code>????<code>null</code>
     */
    public static String trim(String str, String stripChars) {
        return trim(str, stripChars, 0);
    }

    /**
     * ?????????????????<code>null</code>????<code>null</code>?
     * <p>
     * ????<code>String.trim</code>????????<code>Character.isWhitespace</code>
     * ?????? ?????????????????????????
     * <p/>
     * <pre>
     * StringUtil.trimStart(null)         = null
     * StringUtil.trimStart("")           = ""
     * StringUtil.trimStart("abc")        = "abc"
     * StringUtil.trimStart("  abc")      = "abc"
     * StringUtil.trimStart("abc  ")      = "abc  "
     * StringUtil.trimStart(" abc ")      = "abc "
     * </pre>
     * <p/>
     * </p>
     *
     * @param str ???????
     * @return ???????????????<code>null</code>???????<code>""</code>????
     *         <code>null</code>
     */
    public static String trimStart(String str) {
        return trim(str, null, -1);
    }

    /**
     * ???????????????????<code>null</code>?????<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.trimStart(null, *)          = null
     * StringUtil.trimStart("", *)            = ""
     * StringUtil.trimStart("abc", "")        = "abc"
     * StringUtil.trimStart("abc", null)      = "abc"
     * StringUtil.trimStart("  abc", null)    = "abc"
     * StringUtil.trimStart("abc  ", null)    = "abc  "
     * StringUtil.trimStart(" abc ", null)    = "abc "
     * StringUtil.trimStart("yxabc  ", "xyz") = "abc  "
     * </pre>
     *
     * @param str        ???????
     * @param stripChars ??????????<code>null</code>????????
     * @return ???????????????????<code>null</code>????<code>null</code>
     */
    public static String trimStart(String str, String stripChars) {
        return trim(str, stripChars, -1);
    }

    /**
     * ?????????????????<code>null</code>????<code>null</code>?
     * <p>
     * ????<code>String.trim</code>????????<code>Character.isWhitespace</code>
     * ?????? ?????????????????????????
     * <p/>
     * <pre>
     * StringUtil.trimEnd(null)       = null
     * StringUtil.trimEnd("")         = ""
     * StringUtil.trimEnd("abc")      = "abc"
     * StringUtil.trimEnd("  abc")    = "  abc"
     * StringUtil.trimEnd("abc  ")    = "abc"
     * StringUtil.trimEnd(" abc ")    = " abc"
     * </pre>
     * <p/>
     * </p>
     *
     * @param str ???????
     * @return ???????????????<code>null</code>???????<code>""</code>????
     *         <code>null</code>
     */
    public static String trimEnd(String str) {
        return trim(str, null, 1);
    }

    /**
     * ???????????????????<code>null</code>?????<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.trimEnd(null, *)          = null
     * StringUtil.trimEnd("", *)            = ""
     * StringUtil.trimEnd("abc", "")        = "abc"
     * StringUtil.trimEnd("abc", null)      = "abc"
     * StringUtil.trimEnd("  abc", null)    = "  abc"
     * StringUtil.trimEnd("abc  ", null)    = "abc"
     * StringUtil.trimEnd(" abc ", null)    = " abc"
     * StringUtil.trimEnd("  abcyx", "xyz") = "  abc"
     * </pre>
     *
     * @param str        ???????
     * @param stripChars ??????????<code>null</code>????????
     * @return ???????????????????<code>null</code>????<code>null</code>
     */
    public static String trimEnd(String str, String stripChars) {
        return trim(str, stripChars, 1);
    }

    /**
     * ????????????????????????<code>""</code>????<code>null</code>?
     * <p>
     * ????<code>String.trim</code>????????<code>Character.isWhitespace</code>
     * ?????? ?????????????????????????
     * <p/>
     * <pre>
     * StringUtil.trim(null, *)          = null
     * StringUtil.trim("", *)            = null
     * StringUtil.trim("abc", null)      = "abc"
     * StringUtil.trim("  abc", null)    = "abc"
     * StringUtil.trim("abc  ", null)    = "abc"
     * StringUtil.trim(" abc ", null)    = "abc"
     * StringUtil.trim("  abcyx", "xyz") = "  abc"
     * </pre>
     * <p/>
     * </p>
     *
     * @param str        ???????
     * @param stripChars ??????????<code>null</code>????????
     * @return ???????????????<code>null</code>???????<code>""</code>????
     *         <code>null</code>
     */
    public static String trimToNull(String str, String stripChars) {
        String result = trim(str, stripChars);

        if (result == null || result.length() == 0) {
            return null;
        }

        return result;
    }

    /**
     * ??????????????????<code>null</code>????????<code>""</code>?
     * <p>
     * ????<code>String.trim</code>????????<code>Character.isWhitespace</code>
     * ?????? ?????????????????????????
     * <p/>
     * <pre>
     * StringUtil.trim(null, *)          = ""
     * StringUtil.trim("", *)            = ""
     * StringUtil.trim("abc", null)      = "abc"
     * StringUtil.trim("  abc", null)    = "abc"
     * StringUtil.trim("abc  ", null)    = "abc"
     * StringUtil.trim(" abc ", null)    = "abc"
     * StringUtil.trim("  abcyx", "xyz") = "  abc"
     * </pre>
     * <p/>
     * </p>
     *
     * @param str ???????
     * @return ???????????????<code>null</code>???????<code>""</code>????
     *         <code>null</code>
     */
    public static String trimToEmpty(String str, String stripChars) {
        String result = trim(str, stripChars);

        if (result == null) {
            return EMPTY_STRING;
        }

        return result;
    }

    /**
     * ????????????????????<code>null</code>?????<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.trim(null, *)          = null
     * StringUtil.trim("", *)            = ""
     * StringUtil.trim("abc", null)      = "abc"
     * StringUtil.trim("  abc", null)    = "abc"
     * StringUtil.trim("abc  ", null)    = "abc"
     * StringUtil.trim(" abc ", null)    = "abc"
     * StringUtil.trim("  abcyx", "xyz") = "  abc"
     * </pre>
     *
     * @param str        ???????
     * @param stripChars ??????????<code>null</code>????????
     * @param mode       <code>-1</code>??trimStart?<code>0</code>??trim???
     *                   <code>1</code>??trimEnd
     * @return ???????????????????<code>null</code>????<code>null</code>
     */
    private static String trim(String str, String stripChars, int mode) {
        if (str == null) {
            return null;
        }

        int length = str.length();
        int start = 0;
        int end = length;

        // ???????
        if (mode <= 0) {
            if (stripChars == null) {
                while (start < end && Character.isWhitespace(str.charAt(start))) {
                    start++;
                }
            } else if (stripChars.length() == 0) {
                return str;
            } else {
                while (start < end && stripChars.indexOf(str.charAt(start)) != -1) {
                    start++;
                }
            }
        }

        // ???????
        if (mode >= 0) {
            if (stripChars == null) {
                while (start < end && Character.isWhitespace(str.charAt(end - 1))) {
                    end--;
                }
            } else if (stripChars.length() == 0) {
                return str;
            } else {
                while (start < end && stripChars.indexOf(str.charAt(end - 1)) != -1) {
                    end--;
                }
            }
        }

        if (start > 0 || end < length) {
            return str.substring(start, end);
        }

        return str;
    }

    // ==========================================================================
    // ??????
    // ==========================================================================

    /**
     * ?????????????<code>Character.toTitleCase</code>?????????
     * <p>
     * ??????<code>null</code>???<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.capitalize(null)  = null
     * StringUtil.capitalize("")    = ""
     * StringUtil.capitalize("cat") = "Cat"
     * StringUtil.capitalize("cAt") = "CAt"
     * </pre>
     * <p/>
     * </p>
     *
     * @param str ???????
     * @return ??????????????????<code>null</code>????<code>null</code>
     */
    public static String capitalize(String str) {
        int strLen;

        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }

        return new StringBuilder(strLen).append(Character.toTitleCase(str.charAt(0))).append(str.substring(1))
                                        .toString();
    }

    /**
     * ????????????????????
     * <p>
     * ??????<code>null</code>???<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.uncapitalize(null)  = null
     * StringUtil.uncapitalize("")    = ""
     * StringUtil.uncapitalize("Cat") = "cat"
     * StringUtil.uncapitalize("CAT") = "CAT"
     * </pre>
     * <p/>
     * </p>
     *
     * @param str ???????
     * @return ??????????????????<code>null</code>????<code>null</code>
     */
    public static String uncapitalize(String str) {
        int strLen;

        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }

        if (strLen > 1 && Character.isUpperCase(str.charAt(1)) && Character.isUpperCase(str.charAt(0))) {
            return str;
        }

        return new StringBuilder(strLen).append(Character.toLowerCase(str.charAt(0))).append(str.substring(1))
                                        .toString();
    }

    /**
     * ??????????
     * <p>
     * ??????<code>null</code>???<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.swapCase(null)                 = null
     * StringUtil.swapCase("")                   = ""
     * StringUtil.swapCase("The dog has a BONE") = "tHE DOG HAS A bone"
     * </pre>
     * <p/>
     * </p>
     *
     * @param str ???????
     * @return ??????????????????<code>null</code>????<code>null</code>
     */
    public static String swapCase(String str) {
        int strLen;

        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }

        StringBuilder buffer = new StringBuilder(strLen);

        char ch = 0;

        for (int i = 0; i < strLen; i++) {
            ch = str.charAt(i);

            if (Character.isUpperCase(ch)) {
                ch = Character.toLowerCase(ch);
            } else if (Character.isTitleCase(ch)) {
                ch = Character.toLowerCase(ch);
            } else if (Character.isLowerCase(ch)) {
                ch = Character.toUpperCase(ch);
            }

            buffer.append(ch);
        }

        return buffer.toString();
    }

    /**
     * ??????????
     * <p>
     * ??????<code>null</code>???<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.toUpperCase(null)  = null
     * StringUtil.toUpperCase("")    = ""
     * StringUtil.toUpperCase("aBc") = "ABC"
     * </pre>
     * <p/>
     * </p>
     *
     * @param str ???????
     * @return ?????????????<code>null</code>????<code>null</code>
     */
    public static String toUpperCase(String str) {
        if (str == null) {
            return null;
        }

        return str.toUpperCase();
    }

    /**
     * ??????????
     * <p>
     * ??????<code>null</code>???<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.toLowerCase(null)  = null
     * StringUtil.toLowerCase("")    = ""
     * StringUtil.toLowerCase("aBc") = "abc"
     * </pre>
     * <p/>
     * </p>
     *
     * @param str ???????
     * @return ?????????????<code>null</code>????<code>null</code>
     */
    public static String toLowerCase(String str) {
        if (str == null) {
            return null;
        }

        return str.toLowerCase();
    }

    /**
     * ???????camel case?
     * <p>
     * ??????<code>null</code>???<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.toCamelCase(null)  = null
     * StringUtil.toCamelCase("")    = ""
     * StringUtil.toCamelCase("aBc") = "aBc"
     * StringUtil.toCamelCase("aBc def") = "aBcDef"
     * StringUtil.toCamelCase("aBc def_ghi") = "aBcDefGhi"
     * StringUtil.toCamelCase("aBc def_ghi 123") = "aBcDefGhi123"
     * </pre>
     * <p/>
     * </p>
     * <p>
     * ???????????????????????
     * </p>
     *
     * @param str ???????
     * @return camel case???????????<code>null</code>????<code>null</code>
     */
    public static String toCamelCase(String str) {
        return new WordTokenizer() {
            @Override
            protected void startSentence(StringBuilder buffer, char ch) {
                buffer.append(Character.toLowerCase(ch));
            }

            @Override
            protected void startWord(StringBuilder buffer, char ch) {
                if (!isDelimiter(buffer.charAt(buffer.length() - 1))) {
                    buffer.append(Character.toUpperCase(ch));
                } else {
                    buffer.append(Character.toLowerCase(ch));
                }
            }

            @Override
            protected void inWord(StringBuilder buffer, char ch) {
                buffer.append(Character.toLowerCase(ch));
            }

            @Override
            protected void startDigitSentence(StringBuilder buffer, char ch) {
                buffer.append(ch);
            }

            @Override
            protected void startDigitWord(StringBuilder buffer, char ch) {
                buffer.append(ch);
            }

            @Override
            protected void inDigitWord(StringBuilder buffer, char ch) {
                buffer.append(ch);
            }

            @Override
            protected void inDelimiter(StringBuilder buffer, char ch) {
                if (ch != UNDERSCORE) {
                    buffer.append(ch);
                }
            }
        }.parse(str);
    }

    /**
     * ???????pascal case?
     * <p>
     * ??????<code>null</code>???<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.toPascalCase(null)  = null
     * StringUtil.toPascalCase("")    = ""
     * StringUtil.toPascalCase("aBc") = "ABc"
     * StringUtil.toPascalCase("aBc def") = "ABcDef"
     * StringUtil.toPascalCase("aBc def_ghi") = "ABcDefGhi"
     * StringUtil.toPascalCase("aBc def_ghi 123") = "aBcDefGhi123"
     * </pre>
     * <p/>
     * </p>
     * <p>
     * ???????????????????????
     * </p>
     *
     * @param str ???????
     * @return pascal case???????????<code>null</code>????<code>null</code>
     */
    public static String toPascalCase(String str) {
        return new WordTokenizer() {
            @Override
            protected void startSentence(StringBuilder buffer, char ch) {
                buffer.append(Character.toUpperCase(ch));
            }

            @Override
            protected void startWord(StringBuilder buffer, char ch) {
                buffer.append(Character.toUpperCase(ch));
            }

            @Override
            protected void inWord(StringBuilder buffer, char ch) {
                buffer.append(Character.toLowerCase(ch));
            }

            @Override
            protected void startDigitSentence(StringBuilder buffer, char ch) {
                buffer.append(ch);
            }

            @Override
            protected void startDigitWord(StringBuilder buffer, char ch) {
                buffer.append(ch);
            }

            @Override
            protected void inDigitWord(StringBuilder buffer, char ch) {
                buffer.append(ch);
            }

            @Override
            protected void inDelimiter(StringBuilder buffer, char ch) {
                if (ch != UNDERSCORE) {
                    buffer.append(ch);
                }
            }
        }.parse(str);
    }

    /**
     * ???????????????????
     * <p>
     * ??????<code>null</code>???<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.toUpperCaseWithUnderscores(null)  = null
     * StringUtil.toUpperCaseWithUnderscores("")    = ""
     * StringUtil.toUpperCaseWithUnderscores("aBc") = "A_BC"
     * StringUtil.toUpperCaseWithUnderscores("aBc def") = "A_BC_DEF"
     * StringUtil.toUpperCaseWithUnderscores("aBc def_ghi") = "A_BC_DEF_GHI"
     * StringUtil.toUpperCaseWithUnderscores("aBc def_ghi 123") = "A_BC_DEF_GHI_123"
     * StringUtil.toUpperCaseWithUnderscores("__a__Bc__") = "__A__BC__"
     * </pre>
     * <p/>
     * </p>
     * <p>
     * ???????????????????
     * </p>
     *
     * @param str ???????
     * @return ???????????????????<code>null</code>????<code>null</code>
     */
    public static String toUpperCaseWithUnderscores(String str) {
        return new WordTokenizer() {
            @Override
            protected void startSentence(StringBuilder buffer, char ch) {
                buffer.append(Character.toUpperCase(ch));
            }

            @Override
            protected void startWord(StringBuilder buffer, char ch) {
                if (!isDelimiter(buffer.charAt(buffer.length() - 1))) {
                    buffer.append(UNDERSCORE);
                }

                buffer.append(Character.toUpperCase(ch));
            }

            @Override
            protected void inWord(StringBuilder buffer, char ch) {
                buffer.append(Character.toUpperCase(ch));
            }

            @Override
            protected void startDigitSentence(StringBuilder buffer, char ch) {
                buffer.append(ch);
            }

            @Override
            protected void startDigitWord(StringBuilder buffer, char ch) {
                if (!isDelimiter(buffer.charAt(buffer.length() - 1))) {
                    buffer.append(UNDERSCORE);
                }

                buffer.append(ch);
            }

            @Override
            protected void inDigitWord(StringBuilder buffer, char ch) {
                buffer.append(ch);
            }

            @Override
            protected void inDelimiter(StringBuilder buffer, char ch) {
                buffer.append(ch);
            }
        }.parse(str);
    }

    /**
     * ???????????????????
     * <p>
     * ??????<code>null</code>???<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.toLowerCaseWithUnderscores(null)  = null
     * StringUtil.toLowerCaseWithUnderscores("")    = ""
     * StringUtil.toLowerCaseWithUnderscores("aBc") = "a_bc"
     * StringUtil.toLowerCaseWithUnderscores("aBc def") = "a_bc_def"
     * StringUtil.toLowerCaseWithUnderscores("aBc def_ghi") = "a_bc_def_ghi"
     * StringUtil.toLowerCaseWithUnderscores("aBc def_ghi 123") = "a_bc_def_ghi_123"
     * StringUtil.toLowerCaseWithUnderscores("__a__Bc__") = "__a__bc__"
     * </pre>
     * <p/>
     * </p>
     * <p>
     * ???????????????????
     * </p>
     *
     * @param str ???????
     * @return ???????????????????<code>null</code>????<code>null</code>
     */
    public static String toLowerCaseWithUnderscores(String str) {
        return new WordTokenizer() {
            @Override
            protected void startSentence(StringBuilder buffer, char ch) {
                buffer.append(Character.toLowerCase(ch));
            }

            @Override
            protected void startWord(StringBuilder buffer, char ch) {
                if (!isDelimiter(buffer.charAt(buffer.length() - 1))) {
                    buffer.append(UNDERSCORE);
                }

                buffer.append(Character.toLowerCase(ch));
            }

            @Override
            protected void inWord(StringBuilder buffer, char ch) {
                buffer.append(Character.toLowerCase(ch));
            }

            @Override
            protected void startDigitSentence(StringBuilder buffer, char ch) {
                buffer.append(ch);
            }

            @Override
            protected void startDigitWord(StringBuilder buffer, char ch) {
                if (!isDelimiter(buffer.charAt(buffer.length() - 1))) {
                    buffer.append(UNDERSCORE);
                }

                buffer.append(ch);
            }

            @Override
            protected void inDigitWord(StringBuilder buffer, char ch) {
                buffer.append(ch);
            }

            @Override
            protected void inDelimiter(StringBuilder buffer, char ch) {
                buffer.append(ch);
            }
        }.parse(str);
    }

    /**
     * ???????????<code>SENTENCE</code>?
     * <p/>
     * <pre>
     *  SENTENCE = WORD (DELIMITER* WORD)*
     *
     *  WORD = UPPER_CASE_WORD | LOWER_CASE_WORD | TITLE_CASE_WORD | DIGIT_WORD
     *
     *  UPPER_CASE_WORD = UPPER_CASE_LETTER+
     *  LOWER_CASE_WORD = LOWER_CASE_LETTER+
     *  TITLE_CASE_WORD = UPPER_CASE_LETTER LOWER_CASE_LETTER+
     *  DIGIT_WORD      = DIGIT+
     *
     *  UPPER_CASE_LETTER = Character.isUpperCase()
     *  LOWER_CASE_LETTER = Character.isLowerCase()
     *  DIGIT             = Character.isDigit()
     *  NON_LETTER_DIGIT  = !Character.isUpperCase() && !Character.isLowerCase() && !Character.isDigit()
     *
     *  DELIMITER = WHITESPACE | NON_LETTER_DIGIT
     * </pre>
     */
    private abstract static class WordTokenizer {
        protected static final char UNDERSCORE = '_';

        /** Parse sentence? */
        public String parse(String str) {
            if (StringUtil.isEmpty(str)) {
                return str;
            }

            int length = str.length();
            StringBuilder buffer = new StringBuilder(length);

            for (int index = 0; index < length; index++) {
                char ch = str.charAt(index);

                // ?????
                if (Character.isWhitespace(ch)) {
                    continue;
                }

                // ???????UpperCaseWord??TitleCaseWord?
                if (Character.isUpperCase(ch)) {
                    int wordIndex = index + 1;

                    while (wordIndex < length) {
                        char wordChar = str.charAt(wordIndex);

                        if (Character.isUpperCase(wordChar)) {
                            wordIndex++;
                        } else if (Character.isLowerCase(wordChar)) {
                            wordIndex--;
                            break;
                        } else {
                            break;
                        }
                    }

                    // 1. wordIndex == length??????????????upperCaseWord????
                    // 2. wordIndex == index???index????titleCaseWord?
                    // 3. wordIndex > index???index?wordIndex - 1????????upperCaseWord???
                    if (wordIndex == length || wordIndex > index) {
                        index = parseUpperCaseWord(buffer, str, index, wordIndex);
                    } else {
                        index = parseTitleCaseWord(buffer, str, index);
                    }

                    continue;
                }

                // ???????LowerCaseWord?
                if (Character.isLowerCase(ch)) {
                    index = parseLowerCaseWord(buffer, str, index);
                    continue;
                }

                // ?????DigitWord?
                if (Character.isDigit(ch)) {
                    index = parseDigitWord(buffer, str, index);
                    continue;
                }

                // ????????Delimiter?
                inDelimiter(buffer, ch);
            }

            return buffer.toString();
        }

        private int parseUpperCaseWord(StringBuilder buffer, String str, int index, int length) {
            char ch = str.charAt(index++);

            // ?????????????
            if (buffer.length() == 0) {
                startSentence(buffer, ch);
            } else {
                startWord(buffer, ch);
            }

            // ??????????
            for (; index < length; index++) {
                ch = str.charAt(index);
                inWord(buffer, ch);
            }

            return index - 1;
        }

        private int parseLowerCaseWord(StringBuilder buffer, String str, int index) {
            char ch = str.charAt(index++);

            // ?????????????
            if (buffer.length() == 0) {
                startSentence(buffer, ch);
            } else {
                startWord(buffer, ch);
            }

            // ??????????
            int length = str.length();

            for (; index < length; index++) {
                ch = str.charAt(index);

                if (Character.isLowerCase(ch)) {
                    inWord(buffer, ch);
                } else {
                    break;
                }
            }

            return index - 1;
        }

        private int parseTitleCaseWord(StringBuilder buffer, String str, int index) {
            char ch = str.charAt(index++);

            // ?????????????
            if (buffer.length() == 0) {
                startSentence(buffer, ch);
            } else {
                startWord(buffer, ch);
            }

            // ??????????
            int length = str.length();

            for (; index < length; index++) {
                ch = str.charAt(index);

                if (Character.isLowerCase(ch)) {
                    inWord(buffer, ch);
                } else {
                    break;
                }
            }

            return index - 1;
        }

        private int parseDigitWord(StringBuilder buffer, String str, int index) {
            char ch = str.charAt(index++);

            // ?????????????
            if (buffer.length() == 0) {
                startDigitSentence(buffer, ch);
            } else {
                startDigitWord(buffer, ch);
            }

            // ??????????
            int length = str.length();

            for (; index < length; index++) {
                ch = str.charAt(index);

                if (Character.isDigit(ch)) {
                    inDigitWord(buffer, ch);
                } else {
                    break;
                }
            }

            return index - 1;
        }

        protected boolean isDelimiter(char ch) {
            return !Character.isUpperCase(ch) && !Character.isLowerCase(ch) && !Character.isDigit(ch);
        }

        protected abstract void startSentence(StringBuilder buffer, char ch);

        protected abstract void startWord(StringBuilder buffer, char ch);

        protected abstract void inWord(StringBuilder buffer, char ch);

        protected abstract void startDigitSentence(StringBuilder buffer, char ch);

        protected abstract void startDigitWord(StringBuilder buffer, char ch);

        protected abstract void inDigitWord(StringBuilder buffer, char ch);

        protected abstract void inDelimiter(StringBuilder buffer, char ch);
    }

    // ==========================================================================
    // ????????
    //
    // ?????????????
    // ==========================================================================

    /**
     * ????????????
     * <p>
     * ?????????????????????????????????<code>null</code>????<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.split(null, *)         = null
     * StringUtil.split("", *)           = []
     * StringUtil.split("a.b.c", '.')    = ["a", "b", "c"]
     * StringUtil.split("a..b.c", '.')   = ["a", "b", "c"]
     * StringUtil.split("a:b:c", '.')    = ["a:b:c"]
     * StringUtil.split("a b c", ' ')    = ["a", "b", "c"]
     * </pre>
     * <p/>
     * </p>
     *
     * @param str           ???????
     * @param separatorChar ???
     * @return ?????????????????<code>null</code>????<code>null</code>
     */
    public static String[] split(String str, char separatorChar) {
        if (str == null) {
            return null;
        }

        int length = str.length();

        if (length == 0) {
            return EMPTY_STRING_ARRAY;
        }

        List<String> list = createLinkedList();
        int i = 0;
        int start = 0;
        boolean match = false;

        while (i < length) {
            if (str.charAt(i) == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                }

                start = ++i;
                continue;
            }

            match = true;
            i++;
        }

        if (match) {
            list.add(str.substring(start, i));
        }

        return list.toArray(new String[list.size()]);
    }

    /**
     * ????????????
     * <p>
     * ?????????????????????????????????<code>null</code>????<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.split(null, *)                = null
     * StringUtil.split("", *)                  = []
     * StringUtil.split("abc def", null)        = ["abc", "def"]
     * StringUtil.split("abc def", " ")         = ["abc", "def"]
     * StringUtil.split("abc  def", " ")        = ["abc", "def"]
     * StringUtil.split(" ab:  cd::ef  ", ":")  = ["ab", "cd", "ef"]
     * StringUtil.split("abc.def", "")          = ["abc.def"]
     * </pre>
     * <p/>
     * </p>
     *
     * @param str            ???????
     * @param separatorChars ???
     * @return ?????????????????<code>null</code>????<code>null</code>
     */
    public static String[] split(String str, String separatorChars) {
        return split(str, separatorChars, -1);
    }

    /**
     * ????????????
     * <p>
     * ?????????????????????????????????<code>null</code>????<code>null</code>?
     * <p/>
     * <pre>
     * StringUtil.split(null, *, *)                 = null
     * StringUtil.split("", *, *)                   = []
     * StringUtil.split("ab cd ef", null, 0)        = ["ab", "cd", "ef"]
     * StringUtil.split("  ab   cd ef  ", null, 0)  = ["ab", "cd", "ef"]
     * StringUtil.split("ab:cd::ef", ":", 0)        = ["ab", "cd", "ef"]
     * StringUtil.split("ab:cd:ef", ":", 2)         = ["ab", "cdef"]
     * StringUtil.split("abc.def", "", 2)           = ["abc.def"]
     * </pre>
     * <p/>
     * </p>
     *
     * @param str            ???????
     * @param separatorChars ???
     * @param max            ?????????????????0???????
     * @return ?????????????????<code>null</code>????<code>null</code>
     */
    public static String[] split(String str, String separatorChars, int max) {
        if (str == null) {
            return null;
        }

        int length = str.length();

        if (length == 0) {
            return EMPTY_STRING_ARRAY;
        }

        List<String> list = createLinkedList();
        int sizePlus1 = 1;
        int i = 0;
        int start = 0;
        boolean match = false;

        if (separatorChars == null) {
            // null???????????
            while (i < length) {
                if (Character.isWhitespace(str.charAt(i))) {
                    if (match) {
                        if (sizePlus1++ == max) {
                            i = length;
                        }

                        list.add(str.substring(start, i));
                        match = false;
                    }

                    start = ++i;
                    continue;
                }

                match = true;
                i++;
            }
        } else if (separatorChars.length() == 1) {
            // ????????1???
            char sep = separatorChars.charAt(0);

            while (i < length) {
                if (str.charAt(i) == sep) {
                    if (match) {
                        if (sizePlus1++ == max) {
                            i = length;
                        }

                        list.add(str.substring(start, i));
                        match = false;
                    }

                    start = ++i;
                    continue;
                }

                match = true;
                i++;
            }
        } else {
            // ????
            while (i < length) {
                if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                    if (match) {
                        if (sizePlus1++ == max) {
                            i = length;
                        }

                        list.add(str.substring(start, i));
                        match = false;
                    }

                    start = ++i;
                    continue;
                }

                match = true;
                i++;
            }
        }

        if (match) {
            list.add(str.substring(start, i));
        }

        return list.toArray(new String[list.size()]);
    }

    // ==========================================================================
    // ????????
    //
    // ??????????????????
    // ==========================================================================

    /**
     * ????????????????
     * <p/>
     * <pre>
     * StringUtil.join(null, *)                = null
     * StringUtil.join([], *)                  = ""
     * StringUtil.join([null], *)              = ""
     * StringUtil.join(["a", "b", "c"], "--")  = "a--b--c"
     * StringUtil.join(["a", "b", "c"], null)  = "abc"
     * StringUtil.join(["a", "b", "c"], "")    = "abc"
     * StringUtil.join([null, "", "a"], ',')   = ",,a"
     * </pre>
     *
     * @param array     ??????
     * @param separator ???
     * @return ??????????????<code>null</code>????<code>null</code>
     */
    public static String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }

        if (separator == null) {
            separator = EMPTY_STRING;
        }

        int arraySize = array.length;
        int bufSize;

        if (arraySize == 0) {
            bufSize = 0;
        } else {
            int firstLength = array[0] == null ? 16 : array[0].toString().length();
            bufSize = arraySize * (firstLength + separator.length());
        }

        StringBuilder buf = new StringBuilder(bufSize);

        for (int i = 0; i < arraySize; i++) {
            if (separator != null && i > 0) {
                buf.append(separator);
            }

            if (array[i] != null) {
                buf.append(array[i]);
            }
        }

        return buf.toString();
    }

    /**
     * ?<code>Iterator</code>?????????????
     * <p/>
     * <pre>
     * StringUtil.join(null, *)                = null
     * StringUtil.join([], *)                  = ""
     * StringUtil.join([null], *)              = ""
     * StringUtil.join(["a", "b", "c"], "--")  = "a--b--c"
     * StringUtil.join(["a", "b", "c"], null)  = "abc"
     * StringUtil.join(["a", "b", "c"], "")    = "abc"
     * StringUtil.join([null, "", "a"], ',')   = ",,a"
     * </pre>
     *
     * @param iterator  ????<code>Iterator</code>
     * @param separator ???
     * @return ??????????????<code>null</code>????<code>null</code>
     */
    public static String join(Iterable<?> list, String separator) {
        if (list == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder(256); // Java????16, ????

        for (Iterator<?> i = list.iterator(); i.hasNext(); ) {
            Object obj = i.next();

            if (obj != null) {
                buf.append(obj);
            }

            if (separator != null && i.hasNext()) {
                buf.append(separator);
            }
        }

        return buf.toString();
    }

    // ==========================================================================
    // ???????  ???????
    // ==========================================================================

    /**
     * ???????????????????????????????<code>null</code>????????<code>-1</code>?
     * <p/>
     * <pre>
     * StringUtil.indexOf(null, *)         = -1
     * StringUtil.indexOf("", *)           = -1
     * StringUtil.indexOf("aabaabaa", 'a') = 0
     * StringUtil.indexOf("aabaabaa", 'b') = 2
     * </pre>
     *
     * @param str        ???????
     * @param searchChar ??????
     * @return ????????????????<code>null</code>????????<code>-1</code>
     */
    public static int indexOf(String str, char searchChar) {
        if (str == null || str.length() == 0) {
            return -1;
        }

        return str.indexOf(searchChar);
    }

    /**
     * ???????????????????????????????<code>null</code>????????<code>-1</code>?
     * <p/>
     * <pre>
     * StringUtil.indexOf(null, *, *)          = -1
     * StringUtil.indexOf("", *, *)            = -1
     * StringUtil.indexOf("aabaabaa", 'b', 0)  = 2
     * StringUtil.indexOf("aabaabaa", 'b', 3)  = 5
     * StringUtil.indexOf("aabaabaa", 'b', 9)  = -1
     * StringUtil.indexOf("aabaabaa", 'b', -1) = 2
     * </pre>
     *
     * @param str        ???????
     * @param searchChar ??????
     * @param startPos   ?????????????0????0
     * @return ????????????????<code>null</code>????????<code>-1</code>
     */
    public static int indexOf(String str, char searchChar, int startPos) {
        if (str == null || str.length() == 0) {
            return -1;
        }

        return str.indexOf(searchChar, startPos);
    }

    /**
     * ????????????????????????????????<code>null</code>????????<code>-1</code>?
     * <p/>
     * <pre>
     * StringUtil.indexOf(null, *)          = -1
     * StringUtil.indexOf(*, null)          = -1
     * StringUtil.indexOf("", "")           = 0
     * StringUtil.indexOf("aabaabaa", "a")  = 0
     * StringUtil.indexOf("aabaabaa", "b")  = 2
     * StringUtil.indexOf("aabaabaa", "ab") = 1
     * StringUtil.indexOf("aabaabaa", "")   = 0
     * </pre>
     *
     * @param str       ???????
     * @param searchStr ???????
     * @return ????????????????<code>null</code>????????<code>-1</code>
     */
    public static int indexOf(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return -1;
        }

        return str.indexOf(searchStr);
    }

    /**
     * ????????????????????????????????<code>null</code>????????<code>-1</code>?
     * <p/>
     * <pre>
     * StringUtil.indexOf(null, *, *)          = -1
     * StringUtil.indexOf(*, null, *)          = -1
     * StringUtil.indexOf("", "", 0)           = 0
     * StringUtil.indexOf("aabaabaa", "a", 0)  = 0
     * StringUtil.indexOf("aabaabaa", "b", 0)  = 2
     * StringUtil.indexOf("aabaabaa", "ab", 0) = 1
     * StringUtil.indexOf("aabaabaa", "b", 3)  = 5
     * StringUtil.indexOf("aabaabaa", "b", 9)  = -1
     * StringUtil.indexOf("aabaabaa", "b", -1) = 2
     * StringUtil.indexOf("aabaabaa", "", 2)   = 2
     * StringUtil.indexOf("abc", "", 9)        = 3
     * </pre>
     *
     * @param str       ???????
     * @param searchStr ???????
     * @param startPos  ?????????????0????0
     * @return ????????????????<code>null</code>????????<code>-1</code>
     */
    public static int indexOf(String str, String searchStr, int startPos) {
        if (str == null || searchStr == null) {
            return -1;
        }

        // JDK1.3??????bug????????????
        if (searchStr.length() == 0 && startPos >= str.length()) {
            return str.length();
        }

        return str.indexOf(searchStr, startPos);
    }

    /**
     * ???????????????????????????????? ??????<code>null</code>????
     * <code>-1</code>? ???????<code>null</code>??????<code>-1</code>?
     * <p/>
     * <pre>
     * StringUtil.indexOfAny(null, *)                = -1
     * StringUtil.indexOfAny("", *)                  = -1
     * StringUtil.indexOfAny(*, null)                = -1
     * StringUtil.indexOfAny(*, [])                  = -1
     * StringUtil.indexOfAny("zzabyycdxx",['z','a']) = 0
     * StringUtil.indexOfAny("zzabyycdxx",['b','y']) = 3
     * StringUtil.indexOfAny("aba", ['z'])           = -1
     * </pre>
     *
     * @param str         ???????
     * @param searchChars ????????
     * @return ????????????????<code>null</code>????????<code>-1</code>
     */
    public static int indexOfAny(String str, char[] searchChars) {
        if (str == null || str.length() == 0 || searchChars == null || searchChars.length == 0) {
            return -1;
        }

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            for (char searchChar : searchChars) {
                if (searchChar == ch) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * ???????????????????????????????? ??????<code>null</code>????
     * <code>-1</code>? ???????<code>null</code>??????<code>-1</code>?
     * <p/>
     * <pre>
     * StringUtil.indexOfAny(null, *)            = -1
     * StringUtil.indexOfAny("", *)              = -1
     * StringUtil.indexOfAny(*, null)            = -1
     * StringUtil.indexOfAny(*, "")              = -1
     * StringUtil.indexOfAny("zzabyycdxx", "za") = 0
     * StringUtil.indexOfAny("zzabyycdxx", "by") = 3
     * StringUtil.indexOfAny("aba","z")          = -1
     * </pre>
     *
     * @param str         ???????
     * @param searchChars ????????
     * @return ????????????????<code>null</code>????????<code>-1</code>
     */
    public static int indexOfAny(String str, String searchChars) {
        if (str == null || str.length() == 0 || searchChars == null || searchChars.length() == 0) {
            return -1;
        }

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            for (int j = 0; j < searchChars.length(); j++) {
                if (searchChars.charAt(j) == ch) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * ?????????????????????????????????? ??????<code>null</code>????
     * <code>-1</code>? ????????<code>null</code>??????<code>-1</code>?
     * ?????????<code>""</code>????????<code>null</code>????
     * <code>str.length()</code>
     * <p/>
     * <pre>
     * StringUtil.indexOfAny(null, *)                     = -1
     * StringUtil.indexOfAny(*, null)                     = -1
     * StringUtil.indexOfAny(*, [])                       = -1
     * StringUtil.indexOfAny("zzabyycdxx", ["ab","cd"])   = 2
     * StringUtil.indexOfAny("zzabyycdxx", ["cd","ab"])   = 2
     * StringUtil.indexOfAny("zzabyycdxx", ["mn","op"])   = -1
     * StringUtil.indexOfAny("zzabyycdxx", ["zab","aby"]) = 1
     * StringUtil.indexOfAny("zzabyycdxx", [""])          = 0
     * StringUtil.indexOfAny("", [""])                    = 0
     * StringUtil.indexOfAny("", ["a"])                   = -1
     * </pre>
     *
     * @param str        ???????
     * @param searchStrs ?????????
     * @return ????????????????<code>null</code>????????<code>-1</code>
     */
    public static int indexOfAny(String str, String[] searchStrs) {
        if (str == null || searchStrs == null) {
            return -1;
        }

        int sz = searchStrs.length;

        // String's can't have a MAX_VALUEth index.
        int ret = Integer.MAX_VALUE;

        int tmp = 0;

        for (int i = 0; i < sz; i++) {
            String search = searchStrs[i];

            if (search == null) {
                continue;
            }

            tmp = str.indexOf(search);

            if (tmp == -1) {
                continue;
            }

            if (tmp < ret) {
                ret = tmp;
            }
        }

        return ret == Integer.MAX_VALUE ? -1 : ret;
    }

    /**
     * ?????????????????????????????????? ??????<code>null</code>????
     * <code>-1</code>? ???????<code>null</code>??????<code>-1</code>?
     * <p/>
     * <pre>
     * StringUtil.indexOfAnyBut(null, *)             = -1
     * StringUtil.indexOfAnyBut("", *)               = -1
     * StringUtil.indexOfAnyBut(*, null)             = -1
     * StringUtil.indexOfAnyBut(*, [])               = -1
     * StringUtil.indexOfAnyBut("zzabyycdxx",'za')   = 3
     * StringUtil.indexOfAnyBut("zzabyycdxx", 'by')  = 0
     * StringUtil.indexOfAnyBut("aba", 'ab')         = -1
     * </pre>
     *
     * @param str         ???????
     * @param searchChars ????????
     * @return ????????????????<code>null</code>????????<code>-1</code>
     */
    public static int indexOfAnyBut(String str, char[] searchChars) {
        if (str == null || str.length() == 0 || searchChars == null || searchChars.length == 0) {
            return -1;
        }

        outer:
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            for (int j = 0; j < searchChars.length; j++) {
                if (searchChars[j] == ch) {
                    continue outer;
                }
            }

            return i;
        }

        return -1;
    }

    /**
     * ?????????????????????????????????? ??????<code>null</code>????
     * <code>-1</code>? ???????<code>null</code>??????<code>-1</code>?
     * <p/>
     * <pre>
     * StringUtil.indexOfAnyBut(null, *)            = -1
     * StringUtil.indexOfAnyBut("", *)              = -1
     * StringUtil.indexOfAnyBut(*, null)            = -1
     * StringUtil.indexOfAnyBut(*, "")              = -1
     * StringUtil.indexOfAnyBut("zzabyycdxx", "za") = 3
     * StringUtil.indexOfAnyBut("zzabyycdxx", "by") = 0
     * StringUtil.indexOfAnyBut("aba","ab")         = -1
     * </pre>
     *
     * @param str         ???????
     * @param searchChars ????????
     * @return ????????????????<code>null</code>????????<code>-1</code>
     */
    public static int indexOfAnyBut(String str, String searchChars) {
        if (str == null || str.length() == 0 || searchChars == null || searchChars.length() == 0) {
            return -1;
        }

        for (int i = 0; i < str.length(); i++) {
            if (searchChars.indexOf(str.charAt(i)) < 0) {
                return i;
            }
        }

        return -1;
    }

    /**
     * ??????????????????????????????????<code>null</code>????????
     * <code>-1</code>?
     * <p/>
     * <pre>
     * StringUtil.lastIndexOf(null, *)         = -1
     * StringUtil.lastIndexOf("", *)           = -1
     * StringUtil.lastIndexOf("aabaabaa", 'a') = 7
     * StringUtil.lastIndexOf("aabaabaa", 'b') = 5
     * </pre>
     *
     * @param str        ???????
     * @param searchChar ??????
     * @return ????????????????<code>null</code>????????<code>-1</code>
     */
    public static int lastIndexOf(String str, char searchChar) {
        if (str == null || str.length() == 0) {
            return -1;
        }

        return str.lastIndexOf(searchChar);
    }

    /**
     * ??????????????????????????????????<code>null</code>????????
     * <code>-1</code>?
     * <p/>
     * <pre>
     * StringUtil.lastIndexOf(null, *, *)          = -1
     * StringUtil.lastIndexOf("", *,  *)           = -1
     * StringUtil.lastIndexOf("aabaabaa", 'b', 8)  = 5
     * StringUtil.lastIndexOf("aabaabaa", 'b', 4)  = 2
     * StringUtil.lastIndexOf("aabaabaa", 'b', 0)  = -1
     * StringUtil.lastIndexOf("aabaabaa", 'b', 9)  = 5
     * StringUtil.lastIndexOf("aabaabaa", 'b', -1) = -1
     * StringUtil.lastIndexOf("aabaabaa", 'a', 0)  = 0
     * </pre>
     *
     * @param str        ???????
     * @param searchChar ??????
     * @param startPos   ???????????
     * @return ????????????????<code>null</code>????????<code>-1</code>
     */
    public static int lastIndexOf(String str, char searchChar, int startPos) {
        if (str == null || str.length() == 0) {
            return -1;
        }

        return str.lastIndexOf(searchChar, startPos);
    }

    /**
     * ???????????????????????????????????<code>null</code>????????
     * <code>-1</code>?
     * <p/>
     * <pre>
     * StringUtil.lastIndexOf(null, *)         = -1
     * StringUtil.lastIndexOf("", *)           = -1
     * StringUtil.lastIndexOf("aabaabaa", 'a') = 7
     * StringUtil.lastIndexOf("aabaabaa", 'b') = 5
     * </pre>
     *
     * @param str       ???????
     * @param searchStr ???????
     * @return ????????????????<code>null</code>????????<code>-1</code>
     */
    public static int lastIndexOf(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return -1;
        }

        return str.lastIndexOf(searchStr);
    }

    /**
     * ???????????????????????????????????<code>null</code>????????
     * <code>-1</code>?
     * <p/>
     * <pre>
     * StringUtil.lastIndexOf(null, *, *)          = -1
     * StringUtil.lastIndexOf(*, null, *)          = -1
     * StringUtil.lastIndexOf("aabaabaa", "a", 8)  = 7
     * StringUtil.lastIndexOf("aabaabaa", "b", 8)  = 5
     * StringUtil.lastIndexOf("aabaabaa", "ab", 8) = 4
     * StringUtil.lastIndexOf("aabaabaa", "b", 9)  = 5
     * StringUtil.lastIndexOf("aabaabaa", "b", -1) = -1
     * StringUtil.lastIndexOf("aabaabaa", "a", 0)  = 0
     * StringUtil.lastIndexOf("aabaabaa", "b", 0)  = -1
     * </pre>
     *
     * @param str       ???????
     * @param searchStr ???????
     * @param startPos  ???????????
     * @return ????????????????<code>null</code>????????<code>-1</code>
     */
    public static int lastIndexOf(String str, String searchStr, int startPos) {
        if (str == null || searchStr == null) {
            return -1;
        }

        return str.lastIndexOf(searchStr, startPos);
    }

    /**
     * ????????????????????????????????????? ??????<code>null</code>????
     * <code>-1</code>? ????????<code>null</code>??????<code>-1</code>?
     * ?????????<code>""</code>????????<code>null</code>????
     * <code>str.length()</code>
     * <p/>
     * <pre>
     * StringUtil.lastIndexOfAny(null, *)                   = -1
     * StringUtil.lastIndexOfAny(*, null)                   = -1
     * StringUtil.lastIndexOfAny(*, [])                     = -1
     * StringUtil.lastIndexOfAny(*, [null])                 = -1
     * StringUtil.lastIndexOfAny("zzabyycdxx", ["ab","cd"]) = 6
     * StringUtil.lastIndexOfAny("zzabyycdxx", ["cd","ab"]) = 6
     * StringUtil.lastIndexOfAny("zzabyycdxx", ["mn","op"]) = -1
     * StringUtil.lastIndexOfAny("zzabyycdxx", ["mn","op"]) = -1
     * StringUtil.lastIndexOfAny("zzabyycdxx", ["mn",""])   = 10
     * </pre>
     *
     * @param str        ???????
     * @param searchStrs ?????????
     * @return ????????????????<code>null</code>????????<code>-1</code>
     */
    public static int lastIndexOfAny(String str, String[] searchStrs) {
        if (str == null || searchStrs == null) {
            return -1;
        }

        int searchStrsLength = searchStrs.length;
        int index = -1;
        int tmp = 0;

        for (int i = 0; i < searchStrsLength; i++) {
            String search = searchStrs[i];

            if (search == null) {
                continue;
            }

            tmp = str.lastIndexOf(search);

            if (tmp > index) {
                index = tmp;
            }
        }

        return index;
    }

    /**
     * ??????????????????????<code>null</code>????<code>false</code>?
     * <p/>
     * <pre>
     * StringUtil.contains(null, *)    = false
     * StringUtil.contains("", *)      = false
     * StringUtil.contains("abc", 'a') = true
     * StringUtil.contains("abc", 'z') = false
     * </pre>
     *
     * @param str        ???????
     * @param searchChar ??????
     * @return ????????<code>true</code>
     */
    public static boolean contains(String str, char searchChar) {
        if (str == null || str.length() == 0) {
            return false;
        }

        return str.indexOf(searchChar) >= 0;
    }

    /**
     * ???????????????????????<code>null</code>????<code>false</code>?
     * <p/>
     * <pre>
     * StringUtil.contains(null, *)     = false
     * StringUtil.contains(*, null)     = false
     * StringUtil.contains("", "")      = true
     * StringUtil.contains("abc", "")   = true
     * StringUtil.contains("abc", "a")  = true
     * StringUtil.contains("abc", "z")  = false
     * </pre>
     *
     * @param str       ???????
     * @param searchStr ???????
     * @return ????????<code>true</code>
     */
    public static boolean contains(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }

        return str.indexOf(searchStr) >= 0;
    }

    /**
     * ??????????????????????
     * <p>
     * ??????<code>null</code>????<code>false</code>? ???????<code>null</code>
     * ???<code>false</code>? ??????????<code>true</code>.
     * </p>
     * <p/>
     * <pre>
     * StringUtil.containsOnly(null, *)       = false
     * StringUtil.containsOnly(*, null)       = false
     * StringUtil.containsOnly("", *)         = true
     * StringUtil.containsOnly("ab", '')      = false
     * StringUtil.containsOnly("abab", 'abc') = true
     * StringUtil.containsOnly("ab1", 'abc')  = false
     * StringUtil.containsOnly("abz", 'abc')  = false
     * </pre>
     *
     * @param str   ???????
     * @param valid ???????
     * @return ????????<code>true</code>
     */
    public static boolean containsOnly(String str, char[] valid) {
        if (valid == null || str == null) {
            return false;
        }

        if (str.length() == 0) {
            return true;
        }

        if (valid.length == 0) {
            return false;
        }

        return indexOfAnyBut(str, valid) == -1;
    }

    /**
     * ??????????????????????
     * <p>
     * ??????<code>null</code>????<code>false</code>? ???????<code>null</code>
     * ???<code>false</code>? ??????????<code>true</code>.
     * </p>
     * <p/>
     * <pre>
     * StringUtil.containsOnly(null, *)       = false
     * StringUtil.containsOnly(*, null)       = false
     * StringUtil.containsOnly("", *)         = true
     * StringUtil.containsOnly("ab", "")      = false
     * StringUtil.containsOnly("abab", "abc") = true
     * StringUtil.containsOnly("ab1", "abc")  = false
     * StringUtil.containsOnly("abz", "abc")  = false
     * </pre>
     *
     * @param str   ???????
     * @param valid ???????
     * @return ????????<code>true</code>
     */
    public static boolean containsOnly(String str, String valid) {
        if (str == null || valid == null) {
            return false;
        }

        return containsOnly(str, valid.toCharArray());
    }

    /**
     * ??????????????????????
     * <p>
     * ??????<code>null</code>????<code>false</code>? ???????<code>null</code>
     * ???<code>true</code>? ??????????<code>true</code>.
     * </p>
     * <p/>
     * <pre>
     * StringUtil.containsNone(null, *)       = true
     * StringUtil.containsNone(*, null)       = true
     * StringUtil.containsNone("", *)         = true
     * StringUtil.containsNone("ab", '')      = true
     * StringUtil.containsNone("abab", 'xyz') = true
     * StringUtil.containsNone("ab1", 'xyz')  = true
     * StringUtil.containsNone("abz", 'xyz')  = false
     * </pre>
     *
     * @param str     ???????
     * @param invalid ???????
     * @return ????????<code>true</code>
     */
    public static boolean containsNone(String str, char[] invalid) {
        if (str == null || invalid == null) {
            return true;
        }

        int strSize = str.length();
        int validSize = invalid.length;

        for (int i = 0; i < strSize; i++) {
            char ch = str.charAt(i);

            for (int j = 0; j < validSize; j++) {
                if (invalid[j] == ch) {
                    return false;
                }
            }
        }


