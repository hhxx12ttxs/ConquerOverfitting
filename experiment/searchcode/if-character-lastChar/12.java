/* *************************************************************************
 *
 *          Copyright (c) 2002, SeeBeyond Technology Corporation,
 *          All Rights Reserved
 *
 *          This program, and all the routines referenced herein,
 *          are the proprietary properties and trade secrets of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 *          Except as provided for by license agreement, this
 *          program shall not be duplicated, used, or disclosed
 *          without  written consent signed by an officer of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 ***************************************************************************/
package com.stc.sbme.stand.businessname;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.stc.sbme.api.SbmeStandardizationException;


/**
 * This class parses the business name string into its individual components
 *
 * @author Sofiane Ouaguenouni
 * @version $Revision: 1.1.2.1 $
 */
public class BusinessNameParser {
    
    /**
     * A factory method that return an instance of the class
     * @return a BusinessNameParser object
     */
    public static BusinessNameParser getInstance() {
        return new BusinessNameParser();
    }
    
    
    /**
     * Breaks down the record into its basic components including 
     * any special characters
     *
     * @param rec the string to be parsed
     * @param sVar the basic instance that provide access to any other
     *             used instances
     * @return an ArrayList
     * @throws SbmeStandardizationException a generic stand exception
     */
    public ArrayList performParsing(String rec, BusinessStandVariables sVar)
        throws SbmeStandardizationException {
        
        StringBuffer record;
        ArrayList fields;
        String str;
        int numberFields;
        
        
        /*
         * Remove any existing white space from the beginning
         * and the end of name string
         */
        rec.trim();
        
        /* Transform to upper case */
        rec = rec.toUpperCase();
        
        /* Wrap the string record into a StringBuffer  */
        record = new StringBuffer(rec);
        
        /* Remove unprintable characters if any */
        removeUnprintableChars(record);
        
        /* Remove certain special characters with no significance */
        removeSpecialChars(record, sVar);

        /* Perform a preliminary parsing */
        fields = performPrelimParsing(record, sVar);
 
        /* Count the number of fields */
        numberFields = fields.size();
        
        /* Initialize all the flags and pre-flags */
        initializeFields(sVar, numberFields);

       /* 
        * Handle certain special characters that help pre-identify
        * the types of certain keywords
        */
        handleSpecialChars(fields, sVar);
        
        
        /* Perform preliminary parsing and data typing */
        performFinalParsing(fields, sVar);
        
        return fields;
    }
    
    /**
     * Search for unprintable characters and remove them. Each time we find such
     * a character, we remove it and concatenate the bordering characters if
     * any, or just remove the space it occupies.
     *
     * @param str the string where we search for unprintable characters
     */
    private void removeUnprintableChars(StringBuffer str) {
        
    }


    /**
     * Search for characters with no significance like {[, ], {, }, <, >, /, ?,
     * ^, #, !, ~, \, |, (, ), "}. Remove them by concatenating the bordering
     * characters if any.
     *
     * @param str the string where we search for special characters
     * @param specChars the list of special characters to be removed
     */
    private void removeSpecialChars(StringBuffer str, BusinessStandVariables sVar) {
        
        int i;
        int numberOfChars = sVar.specChars.length;
        
       /*
        * Remove all the string's characters that are in the list of special
        * characters.
        */
        for (i = 0; i < numberOfChars; i++) {
            
            /* Search for this character in the string */
            removeCharFromString(str, sVar);
        }
    }
    
 
 
 
    /**
     * Preliminary parsing that helps in handling the search for characters
     * that help idenfiy the parsing
     *
     * @param str the string to be pre-parsed
     * @param sVar
     * @return an ArrayList list of components from the string
     */
    private ArrayList performPrelimParsing(StringBuffer str, BusinessStandVariables sVar) {
        
        int i;
        int numberOfTokens;
        int numberOfChars = str.length();
        
        ArrayList fields = new ArrayList();
        StringTokenizer stok;
        
        String token;
        
        /* Retrim the string */
        str.toString().trim();
        
        /* 
         * Before tokenizing the string separate commas at the beginning or end of a token
         * frm the token itself 
         */
        isolateCommas(str);
        
       /* Parse the string and search for special characters that are considered as delimiters */
        stok = new StringTokenizer(str.toString(), sVar.delimiters);
        
        /* Count the number of fields inside the string */
        numberOfTokens = stok.countTokens();
        
        for (i = 0; i < numberOfTokens; i++) {
            
            // Add the different tokens to the list
            fields.add(i, stok.nextToken());
        }
        
        return fields;
    }


    /**
     * Final parsing handles the identification of tokens after certain
     * special characters transformation
     *
     * @param str the string to be pre-parsed
     * @param sVar
     * @return an ArrayList list of components from the string
     */
    private void performFinalParsing(ArrayList list, BusinessStandVariables sVar) {
        
        int i;
        int tokenLength1;
        int tokenLength2;
        int numberOfTokens = list.size();
        
        ArrayList fields = new ArrayList();
        String str;

        for (i = 0; i < numberOfTokens; i++) {
            
            // Reads in the token
            str = (String) list.get(i);
            
            // The length of the token
            tokenLength1 = str.length();
            
            str = str.trim();
            
            // The length of the token
            tokenLength2 = str.length();
            
            // Check if any token is empty and remove it
            if (tokenLength2 == 0) {
                list.remove(i);
            } else if (tokenLength1 != tokenLength2) {
                list.remove(i);
                list.add(i, str);
            }
        }
    }
    

    
    /**
     *
     *
     * @param  sVar
     * @param numberFields
     */
    private void initializeFields(BusinessStandVariables sVar, int numberFields) {
        
        int i;

        /* Initialize all the fields' types */
        for (i = 0; i < numberFields; i++) {
            
            // The basic name of the company
            sVar.primaryNameFlag = false;
            // The organization type key. For example: Corporation
            sVar.orgTypeKeyFlag[i] = false;
            // The association type key. For example: Group
            sVar.assocTypeKeyFlag[i] = false;
            // A key that hold a city, country or state name. For example: Japan
            sVar.locationTypeKeyFlag[i] = false;
            // The industry type key. For example: Electronics, Technologies
            sVar.industryTypeKeyFlag[i] = false;
            // A field that does not belong to any known keywords
            sVar.unfoundTypeKeyFlag[i] = false;
            // General use adjectives keywords. For example: Advanced, United
            sVar.adjTypeKeyFlag[i] = false;
            // Location-related adjectives. For example: American, Canadian
            sVar.nationalityTypeKeyFlag[i] = false;
            // A key found in the look up table of aliases
            sVar.aliasTypeKeyFlag[i] = false;
            // A url flag 
            sVar.urlTypeFlag[i] = false;
            // A url flag 
            sVar.ampersandTypeFlag[i] = false;
            
            // A url flag 
            sVar.commaFlag[i] = false;
            // A separator flag like a comma
            sVar.separatorCharKeyFlag[i] = false;
            // A glue type flag: For example: & 
            sVar.glueCharKeyFlag[i] = false;
            // An index of a group of merged businesses
            sVar.mergerIndexFlag[i] = false;
            
            /* Initialize the number of types for each token to zero */
            sVar.numberOfTypes[i] = 0;
        }
    }


    /**
     * Special characters that help in the identification of the types
     * of the different tokens based on some flag variables
     *
     * @param aList
     * @param sVar
     */
    private void handleSpecialChars(ArrayList aList, BusinessStandVariables sVar) {
        
        int i;
        int k;
        
        int numberOfChars;
        int numberOfTokens = aList.size();
        
        boolean dotCharExist = false;
        boolean commaCharExist = false;
        boolean hypenCharExist = false;
        boolean slashCharExist = false;
        boolean ampersandCharExist = false;
        boolean otherSpecCharExist = false;
        
        StringBuffer modList;
        
       /*
        * For each token, search for special characters and define some
        * flags that provide some information about the type of the token.
        */
        for (i = 0; i < numberOfTokens; i++) {
            
            /* Wrap the token inside a mutable object */
            modList = new StringBuffer((String) aList.get(i));
            
            /* Calculate the number of characters in the token */
            numberOfChars = modList.length();
            
            
            /*
             * Search if any of the special characters exist inside the string
             */
            for (k = 0; k < numberOfChars; k++) {
                
                
                if (modList.charAt(k) == '.') {
                    
                    /* Update the status */
                    dotCharExist = true;
                    
                } else if (modList.charAt(k) == ',') {
                    
                    /* Update the status */
                    commaCharExist = true;
                    
                } else if (modList.charAt(k) == '-' || modList.charAt(k) == '&#x2013;') {
                    
                    /* Update the status */
                    hypenCharExist = true;
                    
                } else if (modList.charAt(k) == '/') {
                    
                    /* Update the status */
                    slashCharExist = true;
                    
                } else if (modList.charAt(k) == '&') {
                    
                    /* Update the status */
                    ampersandCharExist = true;
                    
                } else if (!Character.isLetterOrDigit(modList.charAt(k))) {
                    
                    /* Update the status */
                    otherSpecCharExist = true;
                }
            }
            
            
            /* Handle the dot character first. If it is located at the borber
             * of the string or if it is alone, replace it with a space,
             * else if it is in the middle of the token, remove it and
             * concatenate the token. Depending on the case, assign flags.
             */
            if (dotCharExist) {

                /* Search for this character in the string */
                handleCharOfTypeOne(modList, '.', i, sVar);

                // Update the values inside the ArrayList modList
                aList.remove(i);
                aList.add(i, modList.toString());
                
                dotCharExist = false;
            }
            
            if (commaCharExist) {
            
                /* Search for this character in the string */
                handleCharOfTypeTwo(modList, ',', i, sVar);
                
                commaCharExist = false;
            }
            
            if (hypenCharExist) {
            
                /* Search for this character in the string */
                handleCharOfTypeThree(modList, '-', i, sVar);
                
                hypenCharExist = false;
            }
            
            if (slashCharExist) {
            
                /* Search for this character in the string */
                handleCharOfTypeFour(modList, '/', i, sVar);
                
                slashCharExist = false;
            }
            
            if (ampersandCharExist) {

                /* Search for this character in the string */
                handleCharOfTypeFive(modList, '&', i, sVar);
                
                ampersandCharExist = false;
            }
        }
    }



    /**
     * Method of type One replace a special character with a white space if it
     * is at the border or isolated and remove it if it is in the middle of a
     * field, unless it is a special case.
     *
     * @param str string where we search for special characters of type 1
     * @param specChar the special character
     * @param index
     * @param sVar
     */
    private static void handleCharOfTypeOne(StringBuffer str, char specChar, int index,
                                             BusinessStandVariables sVar) {
        
        int i;
        int k;
        
        /* The number of characters */
        int numberOfChars = str.length();
        
        /* to simplify the coding */
        int lastChar = numberOfChars - 1;
        
        /* Case where we have a one-character token */
        if (lastChar == 0) {
        
            /* Replace with a blank character */
            str.setCharAt(0, ' ');
            
            return;
        }
        

        /* Otherwise, loop over each character and search for the special character */
        for (i = 0; i < numberOfChars; i++) {

            if (str.charAt(i) == specChar) {
                
                /* If the specChar (i.e. dot) is at the borders of the field */
                if (((i == 0) && (Character.isLetterOrDigit(str.charAt(1))))
                    || ((i == lastChar) && (Character.isLetter(str.charAt(lastChar - 1))))) {
                    
                   /*
                    * Replace with white space. We suppose that the character
                    * has no meaning regarding the type
                    */
                    str.deleteCharAt(i);
                    // Update the length of the string
                    numberOfChars--;
                    // Update the index number of the character
                    i--;
                    // Update also the last character index
                    lastChar = numberOfChars - 1;

                } else if ((i > 0) && (i < lastChar) && Character.isLetterOrDigit(str.charAt(i + 1))
                    && Character.isLetterOrDigit(str.charAt(i - 1))) {
               /*
                * If the dot is in the middle of the field and the surrounding
                * characters are alphanumerics
                */

                   /*
                    * Before deleting the dot and concatenating the token, check
                    * if it is not a url. Search for a "www" before it 
                    * or "com" after it.
                    */
                    if ((i > 2) && ((str.substring(0, 3)).compareTo("WWW") == 0)) {
                        

                        // Check for "COM"
                        if ((i < lastChar - 2)
                            && ((str.substring(lastChar - 2, lastChar + 1)).compareTo("COM") == 0)) {
                            
                            // Don't remove the special character
                            sVar.urlTypeFlag[index] = true;
                            
                            // Update the number of types 
                            sVar.numberOfTypes[index]++;
                        }
                        
                    } else if ((i > 9) && (((str.substring(0, 3)).compareTo("HTTP://WWW") == 0)
                        || (str.substring(0, 3)).compareTo("HTTPS://WWW") == 0)) {

                        // Don't remove the special character
                        sVar.urlTypeFlag[index] = true;
                        
                        // Update the number of types
                        sVar.numberOfTypes[index]++;
        
                    } else if ((i < lastChar - 2)
                        && ((str.substring(lastChar - 2, lastChar + 1)).compareTo("COM") == 0)) {
                    /* Search for "COM" at the end of the token */
                    
                        // Don't remove the special character
                        sVar.urlTypeFlag[index] = true;
                        
                        // Update the number of types 
                        sVar.numberOfTypes[index]++;
                        
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
    }


    /**
     * Method of type Two handles special characters of type ','. In the case
     * of business entities, this character has multiple meanings and types
     *
     * @param str string where we search for special characters of type two
     * @param specChar the special character
     * @param index
     * @param sVar
     */
    private static void handleCharOfTypeTwo(StringBuffer str, char specChar, int index,
                                             BusinessStandVariables sVar) {
        
        int i;
        
        /* It must be a one-character string (previously, we isolated the commas) */
        if (str.charAt(0) == ',') {
            
            // This is a comma
            sVar.commaFlag[index] = true;

            // A separator flag
            sVar.separatorCharKeyFlag[index] = true;
            // A glue type flag
            sVar.glueCharKeyFlag[index] = true;
            
            sVar.numberOfTypes[index] += 2;
        }
    }
    
    
    /**
     * Method of type Three handle the special characters '-'. 
     *
     * @param str string where we search for special characters of type three
     * @param specChar the special character
     * @param numberOfTokens
     * @param index
     * @param sVar
     */
    private static void handleCharOfTypeThree(StringBuffer str, char specChar, int index, 
                                              BusinessStandVariables sVar) {
        
        int i;
        
        /* The number of characters */
        int numberOfChars = str.length();

        /* to simplify the coding */
        int lastChar = numberOfChars - 1;
     
        /* Case where we have a one-character token */
        if ((lastChar == 0) && (str.charAt(0) == '-' || str.charAt(0) == '&#x2013;')) {
        
            // A separator flag 
            sVar.separatorCharKeyFlag[index] = true;

            // A glue flag
            sVar.glueCharKeyFlag[index] = true;
            
            // Update the number of types 
            sVar.numberOfTypes[index] += 2;
            
            return;
        }

        /* Case where we have a one-character token */
        if ((lastChar == 1) 
            && ((str.charAt(0) == '-') && (str.charAt(1) == '-'))) {
        
            // A separator flag 
            sVar.separatorCharKeyFlag[index] = true;
            
            // Update the number of types 
            sVar.numberOfTypes[index]++;
            
            return;
        }
     
        // case where the token is more than one character
        for (i = 0; i < numberOfChars; i++) {
            
            if (str.charAt(i) == specChar) {
                
                if ((i > 0) && (i < lastChar - 1) && Character.isLetterOrDigit(str.charAt(i + 1))
                    && Character.isLetterOrDigit(str.charAt(i - 1))) {
                /* If the dash is in the middle of the field */
                    
                    // A separator flag 
                    sVar.glueCharKeyFlag[index] = true;
                    
                    // Update the number of types 
                    sVar.numberOfTypes[index]++;
                    
                    return;
                }
            }
        }
    }
    



    /**
     * Method of type Two handles special characters of type '/'. In the case
     * of business entities, this character has multiple meanings and types
     *
     * @param str string where we search for special characters of type two
     * @param specChar the special character
     * @param index
     * @param sVar
     */
    private static void handleCharOfTypeFour(StringBuffer str, char specChar, int index,
                                             BusinessStandVariables sVar) {
        
        int i;
        
        /* The number of characters */
        int numberOfChars = str.length();

        /* to simplify the coding */
        int lastChar = numberOfChars - 1;
     
        /* Case where we have a one-character token */
        if ((lastChar == 0)) {
        
            // A merger flag
            sVar.mergerIndexFlag[index] = true;

            // A glue-type flag
            sVar.glueCharKeyFlag[index] = true;

            // Update the number of types 
            sVar.numberOfTypes[index] += 2;
            
            return;
        }
        
        
        // case where the token is more than one character
        for (i = 0; i < numberOfChars; i++) {
            
            if (str.charAt(i) == specChar) {
                
                if ((i > 0) && (i < lastChar - 1) && Character.isLetterOrDigit(str.charAt(i + 1))
                    && Character.isLetterOrDigit(str.charAt(i - 1))) {
                /* If the slash is in the middle of the field */
                    
                    // A merger flag
                    sVar.glueCharKeyFlag[index] = true;
                    
                    // A merger flag
                    sVar.mergerIndexFlag[index] = true;

                    // Update the number of types 
                    sVar.numberOfTypes[index] += 2;
                    
                    return;
                }
            }
        }
    }
    

    /**
     * Method of type Two handles special characters of type '&'. In the case
     * of business entities, this character acts as a glue
     *
     * @param str string where we search for special characters of type two
     * @param specChar the special character
     * @param index
     * @param sVar
     */
    private static void handleCharOfTypeFive(StringBuffer str, char specChar, int index,
                                             BusinessStandVariables sVar) {
        
        int i;
        
        /* The number of characters */
        int numberOfChars = str.length();

        /* to simplify the coding */
        int lastChar = numberOfChars - 1;
     
        /* Case where we have a one-character token */
        if ((lastChar == 0)) {
        
            // A glue-type flag
            sVar.glueCharKeyFlag[index] = true;
            
            sVar.ampersandTypeFlag[index] = true;

            // Update the number of types 
            sVar.numberOfTypes[index] += 1;
            
            return;
        }
        
        
        // case where the token is more than one character
        for (i = 0; i < numberOfChars; i++) {
            
            if (str.charAt(i) == specChar) {
                
                if ((i > 0) && (i < lastChar - 1) && Character.isLetterOrDigit(str.charAt(i + 1))
                    && Character.isLetterOrDigit(str.charAt(i - 1))) {
                /* If the slash is in the middle of the field */
                    
                    // A merger flag
                    sVar.mergerIndexFlag[index] = true;

                    sVar.ampersandTypeFlag[index] = true;

                    // Update the number of types 
                    sVar.numberOfTypes[index] += 1;
                    
                    return;
                }
            }
        }
    }
    
    
    /**
     * Method that removes some special characters if found anywhere
     * inside the string. These characters are provided inside a list table
     *
     * @param str string where we search for special characters of type 1
     * @param specChar the special character
     */
    private static void removeCharFromString(StringBuffer str, BusinessStandVariables sVar) {
        
        int i;
        int j;
        
        /* The number of characters */
        int numberOfChars = str.length();
        
        for (i = 0; i < numberOfChars; i++) {
        
            for (j = 0; j < sVar.nbSpecChars; j++) {
            
                if (str.charAt(i) == sVar.specChars[j].charAt(0)) {
            
                   /* Delete the character and concatenate the bordering fields */
                    str.deleteCharAt(i);
                    numberOfChars--;
                    
                    // Exit the inner loop
                    break;
                }
            }
        }
    }
 
 
    /**
     * Method that separate a special character if found anywhere at the border 
     * of the string
     *
     * @param str string where we search for the special characters 
     */
    private static void isolateCommas(StringBuffer str) {
        
        int i;
        
        /* The number of characters */
        int numberOfChars = str.length();
        
        /* to simplify the coding */
        int lastChar = numberOfChars - 1;

        for (i = 0; i < numberOfChars; i++) {
            

            /* If the specChar (i.e. dot) is at the borders of the field */
            if ((str.charAt(i) == ',') && (i == 0) && !Character.isWhitespace(str.charAt(i + 1))) {
            
               /* Insert a space between the comma and the token */ 
                str.insert(i + 1, ' ');
                
                // Update the number of characters and the index
                numberOfChars++;
                lastChar++;
                
                // Increment the index. We know that it is a whitespace
                i++;
               
           } else if ((str.charAt(i) == ',') && (i == lastChar) 
               && !Character.isWhitespace(str.charAt(lastChar - 1))) {
           
               /* Insert a space between the comma and the token */ 
                str.insert(i, ' ');
                
                // Update the number of characters and the index
                numberOfChars++;
                lastChar++;
                
                // Increment the index. We know that it is a whitespace
                i++;
                
           } else if ((str.charAt(i) == ',') && !Character.isWhitespace(str.charAt(i - 1))
               && !Character.isWhitespace(str.charAt(i + 1))) {

               /* Insert a space between the comma and the surounding tokens */
                str.insert(i + 1, ' ');
                str.insert(i, ' ');
                
                // Update the number of characters and the index
                numberOfChars += 2;
                lastChar += 2;

                // Increment the index. We know that it is a whitespace
                i += 2;
                
           } else if ((str.charAt(i) == ',') && !Character.isWhitespace(str.charAt(i - 1))
               && Character.isWhitespace(str.charAt(i + 1))) {

               /* Insert a space between the comma and the surounding tokens */
                str.insert(i, ' ');
                
                // Update the number of characters and the index
                numberOfChars++;
                lastChar++;
                
                // Increment the index. We know that it is a whitespace
                i++;
                
           } else if ((str.charAt(i) == ',') && Character.isWhitespace(str.charAt(i - 1))
               && !Character.isWhitespace(str.charAt(i + 1))) {

               /* Insert a space between the comma and the surounding tokens */
                str.insert(i + 1, ' ');
                
                // Update the number of characters and the index
                numberOfChars++;
                lastChar++;
                
                // Increment the index. We know that it is a whitespace
                i++;
           }
        }
    }
}

