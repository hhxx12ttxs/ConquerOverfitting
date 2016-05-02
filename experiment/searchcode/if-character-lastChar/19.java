/*
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
*
* Copyright 1997-2007 Sun Microsystems, Inc. All Rights Reserved.
*
* The contents of this file are subject to the terms of the Common
* Development and Distribution License ("CDDL")(the "License"). You
* may not use this file except in compliance with the License.
*
* You can obtain a copy of the License at
* https://open-esb.dev.java.net/public/CDDLv1.0.html
* or mural/license.txt. See the License for the specific language
* governing permissions and limitations under the License. *
* When distributing Covered Code, include this CDDL Header Notice
* in each file and include the License file at mural/license.txt.
* If applicable, add the following below the CDDL Header, with the
* fields enclosed by brackets [] replaced by your own identifying
* information: "Portions Copyrighted [year] [name of copyright owner]"
*/ 

package com.sun.mdm.sbme.datatype.businessname.parser.postprocessor;

import static com.sun.mdm.sbme.datatype.businessname.Flag.Url;
import static com.sun.mdm.sbme.datatype.businessname.InputTokenType.URL;

import com.sun.mdm.sbme.datatype.businessname.Token;

/**
 * <code>TokenPostprocessor</code> which handles tokens containing a dot character.
 *
 * @author Shant Gharibi (shant.gharibi@sun.com)
 */
public class DotTokenPostprocessor implements TokenPostprocessor {

	/**
	 * Set appropriate flags and possibly do some string transformations if special
	 * characters are encountered.
	 * 
	 * @param token the token containing the dot
	 */
    public void postprocess(final Token token) {
        final String image = token.getImage();
        final StringBuilder str = new StringBuilder(image);
        int numberOfChars = str.length();

        /* to simplify the coding */
        int lastChar = numberOfChars - 1;

        /* Case where we have a one-character token */
        if (image.length() == 1) {
            /* Replace with a blank character */
            str.setCharAt(0, ' ');
            token.setImage(str.toString());
            return;
        }

        /* to simplify the coding */
        lastChar = numberOfChars - 1;

        /*
         * Otherwise, loop over each character and search for the special
         * character
         */
        for (int i = 0; i < numberOfChars; i++) {
            if (str.charAt(i) == '.') {
                /* If the specChar (i.e. dot) is at the borders of the field */
                if (i > 0 && i < lastChar && Character.isLetterOrDigit(str.charAt(i + 1)) && Character.isLetterOrDigit(str.charAt(i - 1))) {
                    /*
                     * If the dot is in the middle of the field and the
                     * surrounding characters are alphanumerics
                     */

                    /*
                     * Before deleting the dot and concatenating the token,
                     * check if it is not a url. Search for a "www" before it or
                     * "com" after it.
                     */
                    if (i > 2 && str.substring(0, 3).equals("WWW")) {
                        // Check for "COM"
                        if (i < lastChar - 2 && str.substring(lastChar - 2, lastChar + 1).equals("COM")) {

                            // Don't remove the special character
                        	token.setFlag(Url, true);
                            token.setInputTokenType(URL);
                        }
                    } else if (i > 9 && (str.substring(0, 3).equals("HTTP://WWW") || str.substring(0, 3).equals("HTTPS://WWW"))) {

                        // Don't remove the special character

                    	token.setFlag(Url, true);
                        token.setInputTokenType(URL);

                    } else if (i < lastChar - 2 && str.substring(lastChar - 2, lastChar + 1).equals("COM")) {
                        /* Search for "COM" at the end of the token */

                        // Don't remove the special character
                    	token.setFlag(Url, true);
                        token.setInputTokenType(URL);

                    } else {
                        /* Replace with a blank character */
                        str.deleteCharAt(i);
                        // Update the length of the string
                        numberOfChars--;
                        // Update the index number of the character
                        i--;
                        // Update also the last character index
                        lastChar = numberOfChars - 1;
                    }
                }
            }
        }
        token.setImage(str.toString());
    }
}

