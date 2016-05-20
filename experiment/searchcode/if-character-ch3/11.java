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
 * @(#)CocoLexer.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package com.sun.encoder.coco.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Tokenizer for Cobol Copybook input.
 *
 * @author Noel Ang
 *
 */
public final class CocoLexer {

    private int mRecordRow;
    private int mRecordCol;
    private ArrayList mUndoTokens;
    private final PushbackReader mInputReader;
    private boolean mIsDisable72ColumnLimit;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // sb.append("CocoLexer@").append(Integer.toHexString(hashCode()));
        sb.append("row=").append(mRecordRow);
        sb.append(" col=").append(mRecordCol);
        if (mIsDisable72ColumnLimit)
            sb.append(" Col>72OK");
        else
            sb.append(" Col<=72");
        if (mUndoTokens != null && mUndoTokens.size() > 0) {
            sb.append(" undoTokens=").append(mUndoTokens);
        }
        return sb.toString();
    }

    /**
     * Create a Cobol Copybook tokenizer for an input source.
     *
     * @param file The input source
     *
     * @throws FileNotFoundException if file does not point to an existing,
     *                               readable file
     */
    public CocoLexer(File file) throws FileNotFoundException {
        mInputReader = new PushbackReader(
                new InputStreamReader(new FileInputStream(file)),
                10);
        mRecordRow = 1;
        mRecordCol = 1;
        mUndoTokens = new ArrayList();
    }

    /**
     * Create a Cobol Copybook tokenizer for an input source.
     *
     * @param file     The input source
     * @param encoding Encoding of the input source
     *
     * @throws FileNotFoundException        if file does not point to an
     *                                      existing, readable file
     * @throws UnsupportedEncodingException if the specified encoding is not
     *                                      supported
     */
    public CocoLexer(File file, String encoding)
            throws FileNotFoundException, UnsupportedEncodingException {
        mInputReader = new PushbackReader(
                new InputStreamReader(new FileInputStream(file), encoding),
                10);
        mRecordRow = 1;
        mRecordCol = 1;
        mUndoTokens = new ArrayList();
    }

    /**
     * Provide hint to the lexer that it will no longer be used, and thus it
     * may release resources.  After calling dispose(), undefined behaviour will
     * result if the lexer continues to be used.
     */ 
    public void dispose() {
        mUndoTokens.clear();
        try {
            mInputReader.close();
        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * Disable lexer compliance with IBM Cobol constraint of 72-column copybooks.
     * When disabled, the lexer allows copybook "Area B" content to extend past
     * column 72.
     *
     * @param b <code>true</code> to disable the 72-column constraint,
     *                  <code>false</code> to (re)enable it.
     */
    public void setDisable72ColumnLimit(boolean b) {
        mIsDisable72ColumnLimit = b;
    }

    /**
     * Indicate whether or not the lexer is configured to enforce the IBM Cobol
     * constraint of 72-column copybook content.
     *
     * @return <code>true</code> if the constraint is enforced, <code>false</code>
     *         if it is not.
     *
     * @see #setDisable72ColumnLimit
     */
    public boolean is72ColumnLimitEnforced() {
        return !mIsDisable72ColumnLimit;
    }

    /**
     * Get the next token from the input.
     *
     * @return Next token, or null if no more tokens available
     * @throws java.io.IOException if an I/O error event occurs; note that this is
     *         distinguishable from an EOF/EOD event!
     */
    public CocoToken getNextToken() throws IOException {
        // check if undoTokens has something
        if (mUndoTokens.size() > 0) {
            int idx = mUndoTokens.size() - 1;
            CocoToken undoToken = (CocoToken) mUndoTokens.remove(idx);
            return undoToken;
        }

        /*
         * The Cobol language has characters that serve dual purposes. For
         * example, the letter G at the head of sequence could yield a Cobol word
         * or a separator (DBCS literal opening delimiter: G").  Therefore it is not
         * sufficient to assume exclusive classifications of every scanned
         * character ...
         *
         * Keep this in mind ...
         */

        CocoToken token = null;
        int bytefat4;

        try {
            bytefat4 = peek();

            if (bytefat4 != -1) {
                char ch = (char) bytefat4;

                if (ch == '\n' || ch == '\r') {
                    /* Case: newline (CR or CR LF) */
                    if (ch == '\n') {
                        token = new CocoToken(CocoLanguage.SPACE,
                                              CocoTokenTypes.SEPARATOR_TOKEN,
                                              mRecordRow,
                                              mRecordCol);
                        token.setIsEOL(true);
                        mRecordRow++;
                        mRecordCol = 1;
                        read();
                    } else if (isCharsAvailable(2)) {
                        char[] peeks = new char[2];
                        peek(peeks);
                        char ch2 = peeks[1];
                        if (ch2 == '\n') {
                            token = new CocoToken(CocoLanguage.SPACE,
                                                  CocoTokenTypes.SEPARATOR_TOKEN,
                                                  mRecordRow,
                                                  mRecordCol);
                            token.setIsEOL(true);
                            mRecordRow++;
                            mRecordCol = 1;
                            read();
                            read();
                        }
                    }
                } else if (Character.isDigit(ch)) {
                    /* Case: digit */
                    token = getNumeric();
                } else if (Character.isLetter(ch)) {
                    /*
                     * Case: alpha
                     * ... but some delimiters begin with alphas ...
                     */
                    if (isPrettyDamnAnnoyingDelimiterNext()) {
                        token = getSeparator();
                    } else {
                        token = getAlphaNumeric();
                    }
                } else if (!CocoLanguage.isInCobolCharSet(ch)) {
                    /* Case: character in system's set but not Cobol's set */
                    read(); // discard
                    token = new CocoToken(String.valueOf(ch),
                                          CocoTokenTypes.NONCOBOL_TOKEN,
                                          mRecordRow,
                                          mRecordCol);
                    movePosition(1);
                } else {
                    /* Case: special character or delimiter */
                    if (isSeparatorNext()) {
                        token = getSeparator();
                    } else {
                        token = getCobolCharacter();
                    }
                }
            } else {
                // EOF - empty string is used to indicate this special token
                token = new CocoToken("EOF",
                        CocoTokenTypes.EOF_TOKEN,
                        mRecordRow,
                        mRecordCol);
            }
        } catch (IOException ioe) {
            bytefat4 = -1;
        }

        return token;
    }

    /**
     * Put back a token into the token stream.  The next call to
     * {@link #getNextToken} produces the re-inserted token. The method doesn't
     * actually check that the specified token is the same one it emitted in the
     * last prior call to getNextToken, so you can cheat, but cheating is bad.
     *
     * @param  token Token to re-insert in "front" of the token stream
     * @throws java.lang.IllegalArgumentException if token is null
     */
    public void ungetToken(CocoToken token)
            throws IllegalArgumentException {
        if (token == null) {
            throw new IllegalArgumentException();
        }
        mUndoTokens.add(token);
    }

    /**
     * Scan for an alphanumeric lexeme.
     *
     * @return alphanumeric token, or null if no (or no valid) input left to form one
     * @throws java.io.IOException if an I/O error occurs
     */
    private CocoToken getAlphaNumeric() throws IOException {
        StringBuffer buffer = new StringBuffer();

        while (isAlphaOrDigitNext()) {
            buffer.append((char) read());
        }

        CocoToken token = null;
        if (buffer.length() > 0) {
            token = new CocoToken(buffer.toString(),
                    CocoTokenTypes.ALNUM_TOKEN,
                    mRecordRow,
                    mRecordCol);
            movePosition(token.getLength());
        }
        return token;
    }

    /**
     * Scan for a numeric lexeme. If a letter character is encountered, it is
     * tolerated, and the method ends up returning an alphanumeric token instead.
     *
     * @return numeric or alphanumeric token, or null if no (or no valid) input
     *         left to form one
     * @throws java.io.IOException if an I/O error occurs
     */
    private CocoToken getNumeric() throws IOException {
        StringBuffer buffer = new StringBuffer();

        /* preliminary guess */
        CocoTokenTypes tokenType = CocoTokenTypes.NUM_TOKEN;

        while (isAlphaOrDigitNext()) {
            char ch = (char) read();

            /* Accept alphas, but if I do, return an alphanumeric token instead. */
            if (Character.isLetter(ch)) {
                tokenType = CocoTokenTypes.ALNUM_TOKEN;
            }
            buffer.append(ch);
        }

        CocoToken token = null;
        if (buffer.length() > 0) {
            token = new CocoToken(buffer.toString(),
                    tokenType, mRecordRow, mRecordCol);
            movePosition(token.getLength());
        }
        return token;
    }

    /**
     * Scan for a Cobol character.
     *
     * @return Cobol character token, or null if no (or no valid) input left to
     *         form one
     * @throws java.io.IOException if an I/O error occurs
     */
    private CocoToken getCobolCharacter() throws IOException {
        CocoToken token = null;
        char ch = (char) peek();
        if (CocoLanguage.isInCobolCharSet(ch)) {
            token = new CocoToken(String.valueOf(ch),
                    CocoTokenTypes.SPECIALCHAR_TOKEN,
                    mRecordRow,
                    mRecordCol);
            movePosition(1);
            read();
        }
        return token;
    }


    /**
     * Scan for a Cobol separator.
     *
     * @return Cobol separator token, or null if no (or no valid) input left to
     *         form one
     * @throws java.io.IOException if an I/O error occurs
     */
    private CocoToken getSeparator() throws IOException {

        CocoToken token = null;

        while (token == null && isCharsAvailable(1) ) {

            int chi = read();
            char ch = (char) chi;

            // b== is a separator
            if (ch == ' ') {
                if (isCharsAvailable(2)) {
                    char[] peeks = new char[2];
                    peek(peeks);
                    char ch2 = peeks[0];
                    char ch3 = peeks[1];
                    if (ch2 == '=' && ch3 == '=') {
                        token = new CocoToken("==",
                                CocoTokenTypes.SEPARATOR_TOKEN,
                                mRecordRow,
                                mRecordCol);
                        movePosition(3);
                        read();
                        read();
                    }
                }
                if (token == null) {
                    if (CocoLanguage.isSeparator(ch)) {
                        token = new CocoToken(String.valueOf(ch),
                                CocoTokenTypes.SEPARATOR_TOKEN,
                                mRecordRow,
                                mRecordCol);
                        movePosition(1);
                    } else {
                        unread(chi);
                        break;
                    }
                }
            }

            // Z", X", N", and G" are separators
            if (("GNXZ".indexOf(ch) != -1) && isCharsAvailable(1)) {
                char ch1 = Character.toUpperCase(ch);
                char ch2 = Character.toUpperCase((char) peek());
                if (CocoLanguage.isSeparator(ch1, ch2)) {
                    char[] c = new char[2];
                    c[0] = ch1;
                    c[1] = ch2;
                    token = new CocoToken(new String(c, 0, 2),
                            CocoTokenTypes.SEPARATOR_TOKEN,
                            mRecordRow,
                            mRecordCol);
                    movePosition(2);
                    read();
                }
            }

            // covers every other case
            if (token == null) {
                if (CocoLanguage.isSeparator(ch)) {
                    token = new CocoToken(String.valueOf(ch),
                            CocoTokenTypes.SEPARATOR_TOKEN,
                            mRecordRow,
                            mRecordCol);
                    movePosition(1);
                } else {
                    unread(chi);
                    break;
                }
            }
        }

        return token;
    }

    /**
     * Determine if the next input character is a numeric or alpha character.
     *
     * @return true if the next character is numeric or alpha, else false
     * @throws java.io.IOException if an I/O error occurs
     */
    private boolean isAlphaOrDigitNext() throws IOException {
        boolean isIt = false;
        int value = peek();
        if (value != -1) {
            char ch = (char) value;
            isIt = Character.isLetterOrDigit(ch);
        }
        return isIt;
    }

    /**
     * Determine if the next input character is in the Cobol character set.
     *
     * @return true if the next character is in the set, else false
     * @throws java.io.IOException if an I/O error occurs
     */
    private boolean isCobolCharNext() throws IOException {
        boolean isIt = false;
        int value = peek();
        if (value != -1) {
            char ch = (char) value;
            isIt = com.sun.encoder.coco.model.CocoLanguage.isInCobolCharSet(ch);
        }
        return isIt;
    }

    /**
     * Determine if the next input character is a separator.
     *
     * @return true if the next character is a separator, else false
     * @throws java.io.IOException if an I/O error occurs
     */
    private boolean isSeparatorNext() throws IOException {
        boolean isIt = false;
        int value = peek();
        if (value != -1) {
            char ch = (char) value;
            isIt = com.sun.encoder.coco.model.CocoLanguage.isSeparator(ch);
        }
        return isIt;
    }

    /**
     * Determine if the next few input characters in the token stream comprise
     * a multi-byte delimiters: X", Z", N", G" or the sequence == preceeded by a
     * space
     *
     * @return true if one of these delimiters have been found, else false
     */
    private boolean isPrettyDamnAnnoyingDelimiterNext() throws IOException {
        boolean isIt = false;
        char[] c = new char[3];
        int len  = peek(c);

        if (len >= 2) {
            switch (Character.toUpperCase(c[0])) {
                case 'X':
                case 'Z':
                case 'N':
                case 'G':
                    isIt = CocoLanguage.isSeparator(c[0], c[1]);
                    break;
                case ' ':
                    isIt = (len == 3);
                    isIt = CocoLanguage.isSeparator(c[1], c[2]);
            }
        }
        return isIt;
    }

    /**
     * Determine if the token input has been exhausted.
     *
     * @return true if there is no more input, else false
     * @throws java.io.IOException if an I/O error occurs
     */
    private boolean isEod() throws IOException {
        boolean isIt = false;
        int value = peek();
        isIt = (value == -1);
        return isIt;
    }

    /**
     * Update row and column counters by the specified displacement.
     * The counters are coordinates into a position in a 2-axis view of the lexer's
     * input (i.e., view as a Cobol source file).  This "file" has a width
     * determined by {@link com.sun.encoder.coco.model.CocoParser#SOURCE_LINE_LENGTH}.
     *
     * <p>When amount is a positive value, the counters are updated as if the
     * position they currently represent is displaced "forward" in the file by the
     * indicated amount.  When amount is negative, the counters update to display
     * the position "backward".</p>
     *
     * <p>So, for example, if the file view has a width of 72, and
     * column = 71, and row = 10, and amount = 3, then in moving forward 3 units
     * causes the changes: column = 2, row = 11.</p>
     *
     * @param amount Positive or negative value indicating size of forward or
     *               backward displacement
     */
    private void movePosition(int amount) {
        mRecordCol += amount;
        if (!mIsDisable72ColumnLimit) {
            if (mRecordCol > CocoParser.SOURCE_LINE_LENGTH) {
                mRecordRow += mRecordCol / CocoParser.SOURCE_LINE_LENGTH;
                mRecordCol = mRecordCol % CocoParser.SOURCE_LINE_LENGTH;
            }
        }
    }

    /**
     * Obtain the next character from the lexer input source without consuming
     * the character.
     *
     * @return Next input character or -1 if EOF/EOD occured
     *
     * @throws IOException if an I/O error occurs
     */
    private int peek() throws IOException {
        int value = mInputReader.read();
        if (value != -1) {
            mInputReader.unread(value);
        }
        return value;
    }

    /**
     * Fill an array with characters from the lexer input source without
     * removing the the characters from the input stream.
     *
     * @param c array to fill
     *
     * @return Number of elements in c actually filled with input data; 0 if c
     *         is a zero-size array; -1 if c could not be filled because EOF/EOD
     *         occured before obtaining any characters
     *
     * @throws IOException          if an I/O error occurs
     * @throws NullPointerException if c is null
     */
    private int peek(char[] c)
            throws IOException, NullPointerException {
        int count = 0;
        int value = 0;
        int[] buf = new int[c.length];
        while ((value != -1) && (count < c.length)) {
            value = mInputReader.read();
            buf[count] = value;
            if (value != -1) {
                c[count++] = (char) value;
            }
        }
        if (count > 0) {
            mInputReader.unread(c, 0, count);
        }
        return count;
    }
    
    /**
     * Determines whether the next input stream read request for a given number
     * of characters will succeed.  Success is defined as, not encountering an
     * EOD.  Stream readiness (as reported by {@link InputStreamReader#ready()})
     * is not factored in the decision because blocking is desirable. This call
     * may itself block if the stream is not ready.
     *
     * @param mincount Desired number of ready characters in the stream
     *
     * @return <code>true</code> if the next read request for max(1,
     *         <code>mincount</code>) will neither block or encounter EOD
     *
     * @throws IOException if an I/O error occurs
     */ 
    private boolean isCharsAvailable(int mincount)
            throws IOException {
        int count;
        int got;
        char[] cr;
        
        count = Math.max(0, mincount);
        cr = new char[count];
        got = peek(cr);
        
        return (got == count);
    }
    
    private int read() throws IOException {
        return mInputReader.read();
    }

    private void unread(int chi) throws IOException {
        mInputReader.unread(chi);
    }
}
