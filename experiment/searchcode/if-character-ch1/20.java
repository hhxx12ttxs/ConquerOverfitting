/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)CocoLanguage.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package com.sun.encoder.coco.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Encapsulates information about the Cobol language, as it pertains to
 * Copybook contents.
 *
 * @author  Noel Ang
 *
 * @version $Revision: 1.1 $
 */
public class CocoLanguage {

/**
 * Cobol SPACE character
 */
public static final String SPACE = " ";

/**
 * Cobol plus character
 */
public static final String PLUS = "+";

/**
 * Cobol hyphen character
 */
public static final String HYPHEN = "-";

/**
 * Cobol asterisk character
 */
public static final String ASTERISK = "*";

/**
 * Cobol slant character
 */
public static final String SLANT = "/";

/**
 * Cobol equal character
 */
public static final String EQUAL = "=";

/**
 * Cobol currency character
 */
public static final String CURRENCY = "$";

/**
 * Cobol comma character
 */
public static final String COMMA = ",";

/**
 * Cobol comma separator character
 */
public static final String COMMA_S = ", ";

/**
 * Cobol semicolon character
 */
public static final String SEMICOLON = ";";

/**
 * Cobol semicolon separator character
 */
public static final String SEMICOLON_S = "; ";

/**
 * Cobol colon character
 */
public static final String COLON = ":";

/**
 * Cobol period character
 */
public static final String PERIOD = ".";

/**
 * Cobol period separator character
 */
public static final String PERIOD_S = ". ";

/**
 * Cobol quotation character
 */
public static final String QUOTATION = "\"";

/**
 * Cobol quotation separator character
 */
public static final String QUOTATION_S = "\" ";

/**
 * Cobol left parenthesis character
 */
public static final String LPARENS = "(";

/**
 * Cobol right parenthesis character
 */
public static final String RPARENS = ")";

/**
 * Cobol greater-than character
 */
public static final String GREATER = ">";

/**
 * Cobol less-than character
 */
public static final String LESS = "<";

/**
 * Cobol apostrophe character
 */
public static final String APOSTROPHE = "\'";

/**
 * Cobol apostrophe separator character
 */
public static final String APOSTROPHE_S = "\' ";

/**
 * Cobol non-numeric literal opening
 */
public static final String NONNUMERIC_LIT_OPEN = "X\"";

/**
 * Cobol non-numeric literal opening
 */
public static final String NONNUMERIC_LIT_OPEN_APOS = "X'";

/**
 * Cobol non-numeric literal opening
 */
public static final String NONNUMERIC_LIT_DBCSG_OPEN = "G\"";

/**
 * Cobol non-numeric literal opening
 */
public static final String NONNUMERIC_LIT_DBCSG_OPEN_APOS = "G'";

/**
 * Cobol non-numeric literal opening
 */
public static final String NONNUMERIC_LIT_DBCSN_OPEN = "N\"";

/**
 * Cobol non-numeric literal opening
 */
public static final String NONNUMERIC_LIT_DBCSN_OPEN_APOS = "N'";

/**
 * Cobol null non-numeric literal opening
 */
public static final String NONNUMERIC_NULL_LIT_OPEN = "Z\"";

/**
 * Cobol null non-numeric literal opening
 */
public static final String NONNUMERIC_NULL_LIT_OPEN_APOS = "Z'";

/**
 * Cobol pseudo-text delimiter
 */
public static final String PSEUDOTEXT_DELIM = "==";

/**
 * Debugging line indicator
 */
public static final String DEBUGGING_INDICATOR = "D";

/**
 * Cobol figurative constant ZERO
 */
public static final String ZERO_FIGCONST = "ZERO";

/**
 * Cobol figurative constant ZERO
 */
public static final String ZEROS_FIGCONST = "ZEROS";

/**
 * Cobol figurative constant ZERO
 */
public static final String ZEROES_FIGCONST = "ZEROES";

/**
 * Cobol figurative constant SPACE
 */
public static final String SPACE_FIGCONST = "SPACE";

/**
 * Cobol figurative constant SPACES
 */
public static final String SPACES_FIGCONST = "SPACES";

/**
 * Cobol figurative constant HIGH-VALUE
 */
public static final String HIGHVALUE_FIGCONST = "HIGH-VALUE";

/**
 * Cobol figurative constant HIGH-VALUES
 */
public static final String HIGHVALUES_FIGCONST = "HIGH-VALUES";

/**
 * Cobol figurative constant LOW-VALUES
 */
public static final String LOWVALUE_FIGCONST = "LOW-VALUE";

/**
 * Cobol figurative constant LOW-VALUES
 */
public static final String LOWVALUES_FIGCONST = "LOW-VALUES";

/**
 * Cobol figurative constant QUOTE
 */
public static final String QUOTE_FIGCONST = "QUOTE";

/**
 * Cobol figurative constant QUOTES
 */
public static final String QUOTES_FIGCONST = "QUOTES";

/**
 * Cobol figurative constant ALL
 */
public static final String ALL_FIGCONST = "ALL";

/**
 * Cobol figurative constant NULL
 */
public static final String NULL_FIGCONST = "NULL";

/**
 * Cobol figurative constant NULLS
 */
public static final String NULLS_FIGCONST = "NULLS";

/**
 * Maximum valid length for a numeric literal
 */
public static final int NUMLITERAL_MAX_LENGTH = 30;

/**
 * Maximum valid length for a non-numeric literal
 *
 */
public static final int NONNUMLITERAL_MAX_LENGTH = 160;

/**
 * Maximum valid length for a Cobol word
 */
public static final int WORD_MAX_LENGTH = 30;

/**
 * Maximum valid length for a separator
 */
public static final int SEPARATOR_MAX_LENGTH = 2;

/**
 * Minimum legal level number (excluding special level numbers)
 */
public static final int MIN_LEVEL_NUMBER = 1;

/**
 * Maximum legal level number (excluding special level numbers)
 */
public static final int MAX_LEVEL_NUMBER = 49;

protected static Set mSpecialCharacterSet;
protected static Set mSeparators;
protected static Set mReservedWords;
protected static Set mFigurativeConstants;
protected static Set mClauseWords;

static {
    mSpecialCharacterSet = buildSpecialCharacterSet();
    mSeparators          = buildSeparatorSet();
    mReservedWords       = buildReservedWordSet();
    mFigurativeConstants = buildFigurativeConstantSet();
    mClauseWords         = buildClauseWordSet();
}

private static final Pattern cNumericLiteralPattern =
        Pattern.compile("[+-]?(?:(?:\\d+\\.\\d+[Ee][+-]\\d\\d)|(?:(?:\\d+\\.?\\d+)|(?:\\d+)))" ,
                        Pattern.CASE_INSENSITIVE);

private static final Pattern cNonumericLiteralStartPattern =
        Pattern.compile("[GNXZ]?[\"']", Pattern.CASE_INSENSITIVE);

/**
 * Indicates whether a character is part of the Cobol character set.
 * @param  ch the character in consideration
 * @return true if ch is part of the set, or else false
 */
public static boolean isInCobolCharSet(char ch) {
    boolean isInSet = false;

    isInSet |= Character.isLetterOrDigit(ch);
    isInSet |= mSpecialCharacterSet.contains(String.valueOf(ch));
    return isInSet;
}

/**
 * Indicates whether a string renders a numeric literal.
 *
 * @param  str the string in consideration
 * @return true if str is formatted as a numeric literal, or else false
 */
public static boolean isNumericLiteral(String str) {
    return cNumericLiteralPattern.matcher(str).matches();
}

/**
 * Indicates whether a string renders the starting delimiter of a
 * nonnumeric literal.
 *
 * @param  str the string in consideration
 * @return true if str is formatted as a numeric literal, or else false
 */
public static boolean isNonnumericLiteralStart(String str) {
    return cNonumericLiteralStartPattern.matcher(str).matches();
}

/**
 * Indicates whether a string is a Cobol separator character-string.
 * @param  str the string in consideration
 * @return true if str is a separator, or else false
 */
public static boolean isSeparator(String str) {
    if (!isAllCaps(str)) {
        str = str.toUpperCase();
    }
    return mSeparators.contains(str);
}

/**
 * Indicates whether a character is a Cobol separator character-string.
 * @param  ch the character in consideration
 * @return true if str is a separator, or else false
 */
public static boolean isSeparator(char ch) {
    ch = Character.toUpperCase(ch);
    return mSeparators.contains(String.valueOf(ch))
           ||
           (ch == 0xe || ch == 0xf); // shift-out and shift-in DBCS characters
}

/**
 * Indicates whether a string is a Cobol separator character-string.
 * Convenience method.
 *
 * @param  ch1 the first character of the string in consideration
 * @param  ch2 the second character of the string in consideration
 * @return true if str is a separator, or else false
 * @see    #isSeparator(java.lang.String)
 */
public static boolean isSeparator(char ch1, char ch2) {
    ch1 = Character.toUpperCase(ch1);
    ch2 = Character.toUpperCase(ch2);
    char[] chr = new char[] { ch1, ch2 };
    String str = String.valueOf(chr);
    return mSeparators.contains(str);

}

/**
 * Indicates whether the byte is a Cobol separator.
 * Convenience method.
 *
 * @param  byt Candidate separator
 * @see    #isSeparator(java.lang.String)
 */
public static boolean isSeparator(byte byt) {
    byt &= 0x7F;
    return (byt == 0xE || byt == 0xF || mSeparators.contains(String.valueOf(byt)));

}

/**
 * Indicates whether a string is a reserved word in Cobol.
 * @param  str the string in consideration
 * @return true if str is a reserved word, or else false
 */
public static boolean isReservedWord(String str) {
    if (!isAllCaps(str)) {
        str = str.toUpperCase();
    }
    return mReservedWords.contains(str);
}

/**
 * Indicates whether a string is a Cobol word.  A Cobol word is any
 * word that is reserved, user-defined, or is a system name. It has length
 * and composition requirements as well. Consult the Cobol Language Reference
 * for details.
 *
 * <p>This implementation does not care about system names nor user-defined
 * names, so no checks are made against these types.</p>
 *
 * @param  str the string in consideration
 *
 * @return true if str is a Cobol word, or else false
 */
public static boolean isCobolWord(String str) {
    if (!isAllCaps(str)) {
        str = str.toUpperCase();
    }
    boolean isWord = false;
    if (str != null) {
        isWord = mReservedWords.contains(str);
        isWord &= (str.length() > 0) && (str.length() <= 30);
        isWord &= Character.isLetterOrDigit(str.charAt(0));
        isWord &= Character.isLetterOrDigit(str.charAt(str.length() - 1));
        if (isWord) {
            for (int i = 1; isWord && (i < str.length() - 1); i++) {
                isWord &= Character.isLetterOrDigit(str.charAt(i))
                    || (str.charAt(i) == HYPHEN.charAt(0));
            }
        }
    }
    return isWord;
}
    

/**
 * Indicates whether the word is a starting word for an item clause.
 * 
 * @param word The word in consideration
 * 
 * @return <code>true</code> if the word marks the beginning of a clause
 */
public static boolean isClauseWord(String word) {
    if (!isAllCaps(word)) {
        word = word.toUpperCase();
    }
    return mClauseWords.contains(word);
}

/**
 * Indicates whether a string is a figurative constant.
 *
 * @param  str the string in consideration
 * @return true if str is a figurative constant, or else false
 */
public static boolean isFigurativeConstant(String str) {
    if (!isAllCaps(str)) {
        str = str.toUpperCase();
    }
    return mFigurativeConstants.contains(str);
}

/**
 * Indicates whether the string symbol represents a currency symbol.
 *
 * @param  str the string in consideration
 * @return true if str is a currency symbol, or else false
 */
public static boolean isCurrencySymbol(String str) {
    return str.equals("$");
}

/**
 * Creates a set consisting of all Cobol figurative constants.
 * The set created is unmodifiable.
 *
 * @return the set of all Cobol figurative constants.
 */
protected static Set buildFigurativeConstantSet() {
    Set set = new HashSet();
    set.add(ZERO_FIGCONST);
    set.add(ZEROS_FIGCONST);
    set.add(ZEROES_FIGCONST);
    set.add(HIGHVALUE_FIGCONST);
    set.add(HIGHVALUES_FIGCONST);
    set.add(SPACE_FIGCONST);
    set.add(SPACES_FIGCONST);
    set.add(LOWVALUE_FIGCONST);
    set.add(LOWVALUES_FIGCONST);
    set.add(QUOTE_FIGCONST);
    set.add(QUOTES_FIGCONST);
    set.add(ALL_FIGCONST);
    set.add(NULL_FIGCONST);
    set.add(NULLS_FIGCONST);
    return Collections.unmodifiableSet(set);
}

/**
 * Creates a set consisting of all Cobol separator character-strings.
 * The set created is unmodifiable.
 *
 * @return the set of all Cobol separator character-strings.
 */
protected static Set buildSeparatorSet() {
    Set set = new HashSet();
    set.add(SPACE);
    set.add(PERIOD);
    set.add(PERIOD_S);
    set.add(COMMA);
    set.add(COMMA_S);
    set.add(COLON);
    set.add(SEMICOLON);
    set.add(SEMICOLON_S);
    set.add(LPARENS);
    set.add(RPARENS);
    set.add(QUOTATION);
    set.add(QUOTATION_S);
    set.add(APOSTROPHE);
    set.add(APOSTROPHE_S);
    set.add(NONNUMERIC_LIT_OPEN);
    set.add(NONNUMERIC_LIT_OPEN_APOS);
    set.add(NONNUMERIC_LIT_DBCSG_OPEN);
    set.add(NONNUMERIC_LIT_DBCSG_OPEN_APOS);
    set.add(NONNUMERIC_LIT_DBCSN_OPEN);
    set.add(NONNUMERIC_LIT_DBCSN_OPEN_APOS);
    set.add(NONNUMERIC_NULL_LIT_OPEN);
    set.add(NONNUMERIC_NULL_LIT_OPEN_APOS);
    set.add(PSEUDOTEXT_DELIM);
    return Collections.unmodifiableSet(set);
}

/**
 * Creates a set consisting of strings that represent all Cobol special
 * characters. The set created is unmodifiable.
 *
 * @return the set of all Cobol special characters, as strings
 */
protected static Set buildSpecialCharacterSet() {
    Set set = new HashSet();
    set.add(SPACE);
    set.add(PLUS);
    set.add(HYPHEN);
    set.add(ASTERISK);
    set.add(SLANT);
    set.add(EQUAL);
    set.add(CURRENCY);
    set.add(COMMA);
    set.add(SEMICOLON);
    set.add(COLON);
    set.add(PERIOD);
    set.add(QUOTATION);
    set.add(LPARENS);
    set.add(RPARENS);
    set.add(GREATER);
    set.add(LESS);
    set.add(APOSTROPHE);
    return Collections.unmodifiableSet(set);
}

/**
 * Creates a set consisting of Cobol reserved words.
 * The set created is unmodifiable.
 *
 * @return the set of all Cobol reserved words
 */
protected static Set buildReservedWordSet() {
    Set set = new HashSet();
    set.add("ACCEPT");
    set.add("ACCESS");
    set.add("ADD");
    set.add("ADDRESS");
    set.add("ADVANCING");
    set.add("AFTER");
    set.add("ALL");
    set.add("ALPHABET");
    set.add("ALPHABETIC");
    set.add("ALPHABETIC-LOWER");
    set.add("ALPHABETIC-UPPER");
    set.add("ALPHANUMERIC");
    set.add("ALPHANUMERIC-EDITED");
    set.add("ALSO");
    set.add("ALTER");
    set.add("ALTERNATE");
    set.add("AND");
    set.add("ANY");
    set.add("APPLY");
    set.add("ARE");
    set.add("AREA");
    set.add("AREAS");
    set.add("ASCENDING");
    set.add("ASSIGN");
    set.add("AT");
    set.add("AUTHOR");
    set.add("AUTOMATIC");
    set.add("BASIS");
    set.add("BEFORE");
    set.add("BEGINNING");
    set.add("BINARY");
    //set.add("BLANK");
    set.add("BLOCK");
    set.add("BOTTOM");
    set.add("BY");
    set.add("CALL");
    set.add("CANCEL");
    set.add("CBL");
    set.add("CD");
    set.add("CF");
    set.add("CH");
    set.add("CHARACTER");
    set.add("CHARACTERS");
    set.add("CLASS");
    set.add("CLASS-ID");
    set.add("CLOCK-UNITS");
    set.add("CLOSE");
    set.add("COBOL");
    set.add("CODE");
    set.add("CODE-SET");
    set.add("COLLATING");
    set.add("COLUMN");
    set.add("COM-REG");
    set.add("COMMA");
    set.add("COMMON");
    set.add("COMMUNICATION");
    set.add("COMP");
    set.add("COMP-1");
    set.add("COMP-2");
    set.add("COMP-3");
    set.add("COMP-4");
    set.add("COMP-5");
    set.add("COMPUTATIONAL");
    set.add("COMPUTATIONAL-1");
    set.add("COMPUTATIONAL-2");
    set.add("COMPUTATIONAL-3");
    set.add("COMPUTATIONAL-4");
    set.add("COMPUTATIONAL-5");
    set.add("COMPUTE");
    set.add("CONFIGURATION");
    set.add("CONTAINS");
    set.add("CONTENT");
    set.add("CONTINUE");
    set.add("CONTROL");
    set.add("CONTROLS");
    set.add("CONVERTING");
    set.add("COPY");
    set.add("CORR");
    set.add("CORRESPONDING");
    set.add("COUNT");
    set.add("CURRENCY");
    set.add("DATA");
    set.add("DATE");
    set.add("DATE-COMPILED");
    set.add("DATE-WRITTEN");
    set.add("DAY");
    set.add("DAY-OF-WEEK");
    set.add("DBCS");
    set.add("DE");
    set.add("DEBUG-CONTENTS");
    set.add("DEBUG-ITEM");
    set.add("DEBUG-LINE");
    set.add("DEBUG-NAME");
    set.add("DEBUG-SUB-1");
    set.add("DEBUG-SUB-2");
    set.add("DEBUG-SUB-3");
    set.add("DEBUGGING");
    set.add("DECIMAL-POINT");
    set.add("DECLARATIVES");
    set.add("DELETE");
    set.add("DELIMITED");
    set.add("DELIMITER");
    set.add("DEPENDING");
    set.add("DESCENDING");
    set.add("DESTINATION");
    set.add("DETAIL");
    set.add("DISPLAY");
    set.add("DISPLAY-1");
    set.add("DIVIDE");
    set.add("DIVISION");
    set.add("DOWN");
    set.add("DUPLICATES");
    set.add("DYNAMIC");
    set.add("EGCS");
    set.add("EGI");
    set.add("EJECT");
    set.add("ELSE");
    set.add("EMI");
    set.add("ENABLE");
    set.add("END");
    set.add("END-ADD");
    set.add("END-CALL");
    set.add("END-COMPUTE");
    set.add("END-DELETE");
    set.add("END-DIVIDE");
    set.add("END-EVALUATE");
    set.add("END-IF");
    set.add("END-INVOKE");
    set.add("END-MULTIPLY");
    set.add("END-OF-PAGE");
    set.add("END-PERFORM");
    set.add("END-READ");
    set.add("END-RECEIVE");
    set.add("END-RETURN");
    set.add("END-REWRITE");
    set.add("END-SEARCH");
    set.add("END-START");
    set.add("END-STRING");
    set.add("END-SUBTRACT");
    set.add("END-UNSTRING");
    set.add("END-WRITE");
    set.add("ENDING");
    set.add("ENTER");
    set.add("ENTRY");
    set.add("ENVIRONMENT");
    set.add("EOP");
    set.add("EQUAL");
    set.add("ERROR");
    set.add("ESI");
    set.add("EVALUATE");
    set.add("EVERY");
    set.add("EXCEPTION");
    set.add("EXIT");
    set.add("EXTEND");
    set.add("EXTERNAL");
    set.add("FALSE");
    set.add("FD");
    set.add("FILE");
    set.add("FILE-CONTROL");
    //set.add("FILLER");
    set.add("FINAL");
    set.add("FIRST");
    set.add("FOOTING");
    set.add("FOR");
    set.add("FORMAT");
    set.add("FROM");
    set.add("FUNCTION");
    set.add("GENERATE");
    set.add("GIVING");
    set.add("GLOBAL");
    set.add("GO");
    set.add("GOBACK");
    set.add("GREATER");
    set.add("GROUP");
    set.add("HEADING");
    set.add("HIGH-VALUE");
    set.add("HIGH-VALUES");
    set.add("I-O");
    set.add("I-O-CONTROL");
    set.add("ID");
    set.add("IDENTIFICATION");
    set.add("IF");
    set.add("IN");
    set.add("INDEX");
    set.add("INDEXED");
    set.add("INDICATE");
    set.add("INHERITS");
    set.add("INITIAL");
    set.add("INITIALIZE");
    set.add("INITIATE");
    set.add("INPUT");
    set.add("INPUT-OUTPUT");
    set.add("INSERT");
    set.add("INSPECT");
    set.add("INSTALLATION");
    set.add("INTO");
    set.add("INVALID");
    set.add("INVOKE");
    set.add("IS");
    set.add("JUST");
    set.add("JUSTIFIED");
    set.add("KANJI");
    set.add("KEY");
    set.add("LABEL");
    set.add("LAST");
    set.add("LEADING");
    set.add("LEFT");
    set.add("LENGTH");
    set.add("LESS");
    set.add("LIMIT");
    set.add("LIMITS");
    set.add("LINAGE");
    set.add("LINAGE-COUNTER");
    set.add("LINE");
    set.add("LINE-COUNTER");
    set.add("LINES");
    set.add("LINKAGE");
    set.add("LOCAL-STORAGE");
    set.add("LOCK");
    set.add("LOW-VALUE");
    set.add("LOW-VALUES");
    set.add("MEMORY");
    set.add("MERGE");
    set.add("MESSAGE");
    set.add("METACLASS");
    set.add("METHOD");
    set.add("METHOD-ID");
    set.add("MODE");
    set.add("MODULES");
    set.add("MORE-LABELS");
    set.add("MOVE");
    set.add("MULTIPLE");
    set.add("NATIVE");
    set.add("NEGATIVE");
    set.add("NEXT");
    set.add("NO");
    set.add("NOT");
    set.add("NULL");
    set.add("NULLS");
    set.add("NUMBER");
    set.add("NUMERIC");
    set.add("NUMERIC-EDITED");
    set.add("OBJECT");
    set.add("OBJECT-COMPUTER");
    set.add("OCCURS");
    set.add("OF");
    set.add("OFF");
    set.add("OMITTED");
    set.add("ON");
    set.add("OPEN");
    set.add("OPTIONAL");
    set.add("OR");
    set.add("ORDER");
    set.add("ORGANIZATION");
    set.add("OTHER");
    set.add("OUTPUT");
    set.add("OVERFLOW");
    set.add("OVERRIDE");
    set.add("PACKED-DECIMAL");
    set.add("PADDING");
    set.add("PAGE");
    set.add("PAGE-COUNTER");
    set.add("PASSWORD");
    set.add("PERFORM");
    set.add("PF");
    set.add("PH");
    set.add("PIC");
    set.add("PICTURE");
    set.add("PLUS");
    set.add("POINTER");
    set.add("POSITION");
    set.add("POSITIVE");
    set.add("PREVIOUS");
    set.add("PRINTING");
    set.add("PROCEDURE");
    set.add("PROCEDURE-POINTER");
    set.add("PROCEDURES");
    set.add("PROCEED");
    set.add("PROCESSING");
    set.add("PROGRAM");
    set.add("PROGRAM-ID");
    set.add("PURGE");
    set.add("QUEUE");
    set.add("QUOTE");
    set.add("QUOTES");
    set.add("RANDOM");
    set.add("RD");
    set.add("READ");
    set.add("READY");
    set.add("RECEIVE");
    set.add("RECORD");
    set.add("RECORDING");
    set.add("RECORDS");
    set.add("RECURSIVE");
    set.add("REDEFINES");
    set.add("REEL");
    set.add("REFERENCE");
    set.add("REFERENCES");
    set.add("RELATIVE");
    set.add("RELEASE");
    set.add("RELOAD");
    set.add("REMAINDER");
    set.add("REMOVAL");
    set.add("RENAMES");
    set.add("REPLACE");
    set.add("REPLACING");
    set.add("REPORT");
    set.add("REPORTING");
    set.add("REPORTS");
    set.add("REPOSITORY");
    set.add("RERUN");
    set.add("RESERVE");
    set.add("RESET");
    set.add("RETURN");
    set.add("RETURN-CODE");
    set.add("RETURNING");
    set.add("REVERSED");
    set.add("REWIND");
    set.add("REWRITE");
    set.add("RF");
    set.add("RH");
    set.add("RIGHT");
    set.add("ROUNDED");
    set.add("RUN");
    set.add("SAME");
    set.add("SD");
    set.add("SEARCH");
    set.add("SECTION");
    set.add("SECURITY");
    set.add("SEGMENT");
    set.add("SEGMENT-LIMIT");
    set.add("SELECT");
    set.add("SELF");
    set.add("SEND");
    set.add("SENTENCE");
    set.add("SEPARATE");
    set.add("SEQUENCE");
    set.add("SEQUENTIAL");
    set.add("SERVICE");
    set.add("SET");
    set.add("SHIFT-IN");
    set.add("SHIFT-OUT");
    set.add("SIGN");
    set.add("SIZE");
    set.add("SKIP1");
    set.add("SKIP2");
    set.add("SKIP3");
    set.add("SORT");
    set.add("SORT-CONTROL");
    set.add("SORT-CORE-SIZE");
    set.add("SORT-FILE-SIZE");
    set.add("SORT-MERGE");
    set.add("SORT-MESSAGE");
    set.add("SORT-MODE-SIZE");
    set.add("SORT-RETURN");
    set.add("SOURCE");
    set.add("SOURCE-COMPUTER");
    set.add("SPACE");
    set.add("SPACES");
    set.add("SPECIAL-NAMES");
    set.add("STANDARD");
    set.add("STANDARD-1");
    set.add("STANDARD-2");
    set.add("START");
    set.add("STATUS");
    set.add("STOP");
    set.add("STRING");
    set.add("SUB-QUEUE-1");
    set.add("SUB-QUEUE-2");
    set.add("SUB-QUEUE-3");
    set.add("SUBTRACT");
    set.add("SUM");
    set.add("SUPER");
    set.add("SUPPRESS");
    set.add("SYMBOLIC");
    set.add("SYNC");
    set.add("SYNCHRONIZED");
    set.add("TABLE");
    set.add("TALLY");
    set.add("TALLYING");
    set.add("TAPE");
    set.add("TERMINAL");
    set.add("TERMINATE");
    set.add("TEST");
    set.add("TEXT");
    set.add("THAN");
    set.add("THEN");
    set.add("THROUGH");
    set.add("THRU");
    set.add("TIME");
    set.add("TIMES");
    set.add("TITLE");
    set.add("TO");
    set.add("TOP");
    set.add("TRACE");
    set.add("TRAILING");
    set.add("TRUE");
    set.add("TYPE");
    set.add("UNIT");
    set.add("UNSTRING");
    set.add("UNTIL");
    set.add("UP");
    set.add("UPON");
    set.add("USAGE");
    set.add("USE");
    set.add("USING");
    set.add("VALUE");
    set.add("VALUES");
    set.add("VARYING");
    set.add("WHEN");
    set.add("WHEN-COMPILED");
    set.add("WITH");
    set.add("WORDS");
    set.add("WORKING-STORAGE");
    set.add("WRITE");
    set.add("WRITE-ONLY");
    set.add("ZERO");
    set.add("ZEROES");
    set.add("ZEROS");
    return Collections.unmodifiableSet(set);
}

/**
 * Creates a set consisting of Cobol clause starting words.
 * The set created is unmodifiable.
 * 
 * @return the set of recognized Cobol clause words
 */ 
protected static Set buildClauseWordSet() {
    Set set = new HashSet();
    set.add("BLANK");
    set.add("DATE");
    set.add("EXTERNAL");
    set.add("GLOBAL");
    set.add("JUSTIFIED");
    set.add("JUST");
    set.add("OCCURS");
    set.add("PIC");
    set.add("PICTURE");
    set.add("REDEFINES");
    set.add("RENAMES");
    set.add("SIGN");
    set.add("LEADING");
    set.add("TRAILING");
    set.add("SYNC");
    set.add("SYNCHRONIZED");
    set.add("USAGE");
    set.add("BINARY");
    set.add("COMP");
    set.add("COMP-1");
    set.add("COMP-2");
    set.add("COMP-3");
    set.add("COMP-4");
    set.add("COMP-5");
    set.add("COMPUTATIONAL");
    set.add("COMPUTATIONAL-1");
    set.add("COMPUTATIONAL-2");
    set.add("COMPUTATIONAL-3");
    set.add("COMPUTATIONAL-4");
    set.add("COMPUTATIONAL-5");
    set.add("DISPLAY");
    set.add("DISPLAY-1");
    set.add("INDEX");
    set.add("PACKED-DECIMAL");
    set.add("POINTER");
    set.add("PROCEDURE-POINTER");
    set.add("OBJECT");
    set.add("NATIVE");
    set.add("VALUE");
    return set;
}
    
/**
 * Indicate whether all alphabets in a string are in their upper case.
 * A string without any alphabets will evaluate to true
 *
 * @param  str the string in consideration
 *
 * @return true if all alphabets in str are in their upper case, or if there
 *         are no alphabets in str, or str is null; otherwise returns false
 */
private static boolean isAllCaps(String str) {
    boolean isCaps = true;
    if (str != null) {
        for (int i = 0; isCaps && (i < str.length()); i++) {
            char ch = str.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                isCaps &= Character.isUpperCase(ch);
            }
        }
    }
    return isCaps;
}

}
/* EOF $RCSfile: CocoLanguage.java,v $ */

