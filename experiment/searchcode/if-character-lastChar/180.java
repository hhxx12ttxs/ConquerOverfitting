/*
 ===============================================================================
 This class (Pairtree) was originally written by Justin Littman (jlit@loc.gov)
 It has been modified and renamed by Kevin S. Clarke (ksclarke@gmail.com)
 ===============================================================================

 This software is a work of the United States Government and is not subject
 to copyright protection in the United States.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR THE UNITED STATES BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.


 Foreign copyrights may apply. To the extent that foreign copyrights in the
 software exist outside the United States, the following terms apply:

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.

 */

package info.freelibrary.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for working with Pairtrees.
 *
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public class PairtreeUtils {

    public static final String HEX_INDICATOR = "^";

    private static Character mySeparator = File.separatorChar;

    private static int myShortyLength = 2;

    private PairtreeUtils() {
    }

    /**
     * Returns the Pairtree shorty length.
     *
     * @return The Pairtree shorty length
     */
    public static int getShortyLength() {
        return myShortyLength;
    }

    /**
     * Sets the Pairtree shorty length
     *
     * @param aLength A length to set as the shorty length
     */
    public static void setShortyLength(final int aLength) {
        myShortyLength = aLength;
    }

    /**
     * Returns the separator character.
     *
     * @return The separator character
     */
    public static Character getSeparator() {
        return mySeparator;
    }

    /**
     * Sets the separator character.
     *
     * @param aSeparator The separator character
     */
    public static void setSeparator(final Character aSeparator) {
        mySeparator = aSeparator;
    }

    /**
     * Maps the supplied ID to a Pairtree path.
     *
     * @param aID An ID to map to a Pairtree path
     * @return The Pairtree path for the supplied ID
     */
    public static String mapToPtPath(final String aID) {
        assert aID != null;

        final String encodedID = encodeID(aID);
        final List<String> shorties = new ArrayList<String>();
        int start = 0;

        while (start < encodedID.length()) {
            int end = start + myShortyLength;

            if (end > encodedID.length()) {
                end = encodedID.length();
            }

            shorties.add(encodedID.substring(start, end));
            start = end;
        }

        return concat(shorties.toArray(new String[shorties.size()]));
    }

    /**
     * Maps the supplied ID to a Pairtree path using the supplied base path.
     *
     * @param aID An ID to map to a Pairtree path
     * @param aBasePath The base path to use in the mapping
     * @return The Pairtree path for the supplied ID
     */
    public static String mapToPtPath(final String aBasePath, final String aID) {
        return concat(aBasePath, mapToPtPath(aID), null);
    }

    /**
     * Maps the supplied ID to a Pairtree path using the supplied base path.
     *
     * @param aID An ID to map to a Pairtree path
     * @param aBasePath The base path to use in the mapping
     * @param aEncapsulatingDirName The name of the encapsulating directory
     * @return The Pairtree path for the supplied ID
     */
    public static String mapToPtPath(final String aBasePath, final String aID, final String aEncapsulatingDirName) {
        return concat(aBasePath, mapToPtPath(aID), aEncapsulatingDirName);
    }

    /**
     * Maps the supplied base path to an ID using the supplied Pairtree path.
     *
     * @param aBasePath A base path to use for the mapping
     * @param aPtPath A Pairtree path to map to an ID
     * @return The ID that is a result of the mapping
     * @throws InvalidPtPathException If there is trouble mapping the path
     */
    public static String mapToID(final String aBasePath, final String aPtPath) throws InvalidPtPathException {
        final String newPath = removeBasePath(aBasePath, aPtPath);
        return mapToID(newPath);
    }

    /**
     * Maps the supplied base path to an ID.
     *
     * @param aPtPath A Pairtree path to map to an ID
     * @return The ID that is a result of the mapping
     * @throws InvalidPtPathException If there is trouble mapping the path
     */
    public static String mapToID(final String aPtPath) throws InvalidPtPathException {
        String id = aPtPath;

        if (id.endsWith(Character.toString(mySeparator))) {
            id = id.substring(0, id.length() - 1);
        }

        final String encapsulatingDir = getEncapsulatingDir(aPtPath);

        if (encapsulatingDir != null) {
            id = id.substring(0, id.length() - encapsulatingDir.length());
        }

        id = id.replace(Character.toString(mySeparator), "");
        id = decodeID(id);

        return id;
    }

    /**
     * Extracts the encapsulating directory from the supplied Pairtree path, using the supplied base path.
     *
     * @param aBasePath A base path for the Pairtree path
     * @param aPtPath The Pairtree path
     * @return The name of the encapsulating directory
     * @throws InvalidPtPathException If there is a problem extracting the encapsulating directory
     */
    public static String getEncapsulatingDir(final String aBasePath, final String aPtPath)
            throws InvalidPtPathException {
        final String newPath = removeBasePath(aBasePath, aPtPath);
        return getEncapsulatingDir(newPath);
    }

    /**
     * Extracts the encapsulating directory from the supplied Pairtree path.
     *
     * @param aPtPath The Pairtree path from which to extract the encapsulating directory
     * @return The name of the encapsulating directory
     * @throws InvalidPtPathException If there is a problem extracting the encapsulating directory
     */
    public static String getEncapsulatingDir(final String aPtPath) throws InvalidPtPathException {
        assert aPtPath != null;

        // Walk the Pairtree path looking for first non-shorty
        final String[] pPathParts = aPtPath.split("\\" + mySeparator);

        // If there is only 1 part
        if (pPathParts.length == 1) {
            // If part <= shorty length then no encapsulating directory
            if (pPathParts[0].length() <= myShortyLength) {
                return null;
            } else { // Else no Pairtree path
                throw new InvalidPtPathException(StringUtils.format("PtPath {} contains no shorties", aPtPath));
            }
        }

        // All parts up to next to last and last should have shorty length
        for (int i = 0; i < pPathParts.length - 2; i++) {
            if (pPathParts[i].length() != myShortyLength) {
                throw new InvalidPtPathException(StringUtils.format("PtPath {} parts count should be {} but is {}",
                        aPtPath, myShortyLength, pPathParts[i].length()));
            }
        }

        final String nextToLastPart = pPathParts[pPathParts.length - 2];
        final String lastPart = pPathParts[pPathParts.length - 1];

        // Next to last should have shorty length or less
        if (nextToLastPart.length() > myShortyLength) {
            throw new InvalidPtPathException(StringUtils.format("Pairtree Path {} has parts of incorrect length",
                    aPtPath));
        }

        // If next to last has shorty length
        if (nextToLastPart.length() == myShortyLength) {
            // If last has length > shorty length then encapsulating directory
            if (lastPart.length() > myShortyLength) {
                return lastPart;
            } else { // Else no encapsulating directory
                return null;
            }
        }

        // Else last is encapsulating directory
        return lastPart;
    }

    /**
     * Concatenates the Pairtree paths varargs.
     *
     * @param aPathsVarargs The Pairtree paths varargs
     * @return The concatenated Pairtree paths
     */
    private static String concat(final String... aPathsVarargs) {
        if (aPathsVarargs == null || aPathsVarargs.length == 0) {
            return null;
        }

        final StringBuffer pathBuf = new StringBuffer();
        Character lastChar = null;

        for (int i = 0; i < aPathsVarargs.length; i++) {
            if (aPathsVarargs[i] != null) {
                int length;

                if (lastChar != null && !mySeparator.equals(lastChar)) {
                    pathBuf.append(mySeparator);
                }

                pathBuf.append(aPathsVarargs[i]);
                length = aPathsVarargs[i].length();
                lastChar = aPathsVarargs[i].charAt(length - 1);
            }
        }

        return pathBuf.toString();
    }

    /**
     * Removes the base path from the supplied Pairtree path.
     *
     * @param aBasePath A base path for a Pairtree path
     * @param aPtPath A Pairtree path
     * @return The Pairtree path without the base path
     */
    public static String removeBasePath(final String aBasePath, final String aPtPath) {
        assert aBasePath != null;
        assert aPtPath != null;
        String newPath = aPtPath;

        if (aPtPath.startsWith(aBasePath)) {
            newPath = newPath.substring(aBasePath.length());

            if (newPath.startsWith(Character.toString(mySeparator))) {
                newPath = newPath.substring(1);
            }
        }

        return newPath;
    }

    /**
     * Cleans an ID for use in a Pairtree path.
     *
     * @param aID An idea to be cleaned
     * @return The cleaned ID for use in a Pairtree path
     */
    public static String encodeID(final String aID) {
        assert aID != null;
        byte[] bytes; // First pass

        try {
            bytes = aID.getBytes("utf-8");
        } catch (final UnsupportedEncodingException details) {
            throw new RuntimeException("Error getting UTF-8 for path", details);
        }

        final StringBuffer idBuf = new StringBuffer();

        for (int c = 0; c < bytes.length; c++) {
            final byte b = bytes[c];
            final int i = b & 0xff;

            if (i < 0x21 || i > 0x7e || i == 0x22 || i == 0x2a || i == 0x2b || i == 0x2c || i == 0x3c || i == 0x3d ||
                    i == 0x3e || i == 0x3f || i == 0x5c || i == 0x5e || i == 0x7c) {
                // Encode
                idBuf.append(HEX_INDICATOR);
                idBuf.append(Integer.toHexString(i));
            } else {
                // Don't encode
                final char[] chars = Character.toChars(i);

                assert chars.length == 1;
                idBuf.append(chars[0]);
            }
        }

        for (int c = 0; c < idBuf.length(); c++) {
            final char ch = idBuf.charAt(c);

            if (ch == '/') {
                idBuf.setCharAt(c, '=');
            } else if (ch == ':') {
                idBuf.setCharAt(c, '+');
            } else if (ch == '.') {
                idBuf.setCharAt(c, ',');
            }
        }

        return idBuf.toString();
    }

    /**
     * Unclean the ID from the Pairtree path.
     *
     * @param aID A cleaned ID to unclean
     * @return The unclean ID
     */
    public static String decodeID(final String aID) {
        final StringBuffer idBuf = new StringBuffer();

        for (int c = 0; c < aID.length(); c++) {
            final char ch = aID.charAt(c);

            if (ch == '=') {
                idBuf.append('/');
            } else if (ch == '+') {
                idBuf.append(':');
            } else if (ch == ',') {
                idBuf.append('.');
            } else if (ch == '^') {
                // Get the next 2 chars
                final String hex = aID.substring(c + 1, c + 3);
                final char[] chars = Character.toChars(Integer.parseInt(hex, 16));

                assert chars.length == 1;
                idBuf.append(chars[0]);
                c = c + 2;
            } else {
                idBuf.append(ch);
            }
        }

        return idBuf.toString();
    }
}

