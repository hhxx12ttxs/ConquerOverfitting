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
package com.stc.sbme.stand.personname;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.stc.sbme.api.SbmeStandardizationException;

/**
 * This class parses the person name string into its individual components
 *
 * @author Sofiane Ouaguenouni
 * @version $Revision: 1.1.2.1 $
 */
public class PersonNameParser {
    
    /**
     * Define a factory method that return an instance of the class
     * @return an instance of PersonNameParser class
     */
    public static PersonNameParser getInstance() {
        return new PersonNameParser();
    }
    
    
    /**
     * Performs the parsing of the input string
     *
     * @param rec the string to be parsed
     * @param sVar the basic instance that provide access to any other
     *             used instances
     * @return an ArrayList object
     * @throws SbmeStandardizationException a generic stand exception
     */
    public ArrayList performParsing(String rec, PersonNameStandVariables sVar)
    throws SbmeStandardizationException {
        
        int i;
        int j;
        int endchar;
        int numberFields;
        
        StringBuffer record;
        ArrayList fields;
        String str;

        /*
         * Remove any existing white space from the beginning
         * and the end of name string
         */
        rec.trim();
        
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
        
        
       /* Handle certain special characters that help pre-identify
        * the types of certain fields
        */
        handleSpecialChars(fields, sVar);
 
        
        /* Perform the final parsing and return the ArrayList */
        performFinalParsing(fields);

        return fields;
    }
    
    
    /**
     *
     *
     * @param  sVar
     * @param numberFields
     */
    private void initializeFields(PersonNameStandVariables sVar, int numberFields) {
        
        int i;

            /* Initialize all the potential fields */
        for (i = 0; i < numberFields; i++) {
            
            sVar.emailFlag[i] = false;
            sVar.midInitialFlag[i] = false;
            sVar.genSuffixPreFlag[i] = false;
            sVar.titlePreFlag[i] = false;
            sVar.lastNamePreFlag[i] = false;
            sVar.firstNameFirstPreFlag[i] = false;
            sVar.firstNameSecondPreFlag[i] = false;
            sVar.firstNamePreFlag[i] = false;
            sVar.lastNameWithPrefixPreFlag[i] = false;
        }
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
    private void removeSpecialChars(StringBuffer str, PersonNameStandVariables sVar) {
        
        int i;
        int numberOfChars = sVar.specChars.length;
        
       /*
        * Remove all the string's characters that are in the list of special
        * characters.
        */
        for (i = 0; i < sVar.nbChars; i++) {
            /* Search for this character in the string */
            removeCharFromString(str, sVar.specChars[i].charAt(0));
        }
    }
    
    /**
     * Preliminary parsing that helps in handling the search for characters
     * that help idenfiy the parsing
     *
     * @param str the string to be pre-parsed
     * @param sVar
     * @return an ArrayList object
     */
    private ArrayList performPrelimParsing(StringBuffer str, PersonNameStandVariables sVar) {
        
        int i;
        int numberOfTokens;
        int numberOfChars = str.length();
        
        ArrayList fields = new ArrayList();
        StringTokenizer stok;
        
        String token;
        
        /* Retrim the string */
        str.toString().trim();
        
       /*
        * Search for special characters that are considered as delimiters
        * Any character besides the alphanumerics and the specChars3 is a
        * delimiter.
        */
        stok = new StringTokenizer(str.toString(), sVar.delimiters);
        
        /* Count the number of fields inside the string */
        numberOfTokens = stok.countTokens();
        
        for (i = 0; i < numberOfTokens; i++) {
            
            token = stok.nextToken();
            
           /*
            * Make sure that no special character forms a token
            * If so, remove the token
            */
            if ((token.length() == 1) && !Character.isLetterOrDigit(token.charAt(0))) {
                continue;
            } else {
                
                /* Insert the tokens into the ArrayList */
                fields.add(i, token);
            }
        }
        return fields;
    }
    
    
    /**
     * Special characters that help in the identification of the types
     * of the different tokens based on some flag variables
     *
     * @param aList
     * @param sVar
     */
    private void handleSpecialChars(ArrayList aList, PersonNameStandVariables sVar) {
        
        int i;
        int k;
        
        int numberOfChars;
        int numberOfTokens = aList.size();
        
        boolean dotCharExist = false;
        boolean commaCharExist = false;
        boolean hypenCharExist = false;
        boolean apposCharExist = false;
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
                    
                } else if (modList.charAt(k) == '\'') {
                    
                                    /* Update the status */
                    apposCharExist = true;
                    
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
                replaceCharOfTypeOne(modList, '.', i, sVar);
            }
            
            if (commaCharExist) {
                    /* Search for this character in the string */
                replaceCharOfTypeTwo(modList, ',', i, sVar);
            }
            
            if (hypenCharExist) {
                
                /* Search for this character in the string */
                replaceCharOfTypeThree(modList, '-', numberOfTokens, i, sVar);
            }
            
            if (apposCharExist) {
                
                /* Search for this character in the string */
                replaceCharOfTypeFour(modList, '\'', numberOfTokens, i, sVar);
            }
            
            if (apposCharExist) {
                
                    /* Default handling of other characters */
                replaceWithWhite(modList);
            }
        }
    }
    
    
    /**
     * Perform final parsing of the string. Start by changing the characters to
     * uppercases, then parse the tokens.
     *
     * @param aList the array of strings to be parsed.
     */
    private void performFinalParsing(ArrayList aList) {
        
        int i;
        int k;
        int numberOfTokens = aList.size();
        String field;
        
        for (i = 0; i < numberOfTokens; i++) {
            
           /*
            * First trim the trailing spaces. Then, change all lowercase
            * characters to uppercase characters
            */
            field = ((String) aList.get(i)).trim();

            field = field.toUpperCase();

            aList.set(i, field);
        }
    }
    
    
    /**
     * Method of type 1 replace special characters with a white space if it
     * is at the border or isolated and remove it if it is in the middle of a
     * field
     *
     * @param str string where we search for special characters of type 1
     * @param specChar the special character
     * @param index
     * @param sVar
     */
    private static void replaceCharOfTypeOne(StringBuffer str, char specChar, int
    index,
    PersonNameStandVariables sVar) {
        
        int i;
        int k;
        
        /* Default index of the character holding a '@' character */
        int emailCharIndex = 1000;
        
        /* The number of characters */
        int numberOfChars = str.length();
        
            /* to simplify the coding */
        int lastChar = numberOfChars - 1;
        
        for (i = 0; i < numberOfChars; i++) {
            
            if (str.charAt(i) == specChar) {
                
                        /* If the dot is at the beginning of the field */
                if ((i == 0) && (Character.isLetterOrDigit(str.charAt(1)))) {
                    
                   /*
                    * Replace with white space. We suppose that the character
                    * has no meaning regarding the type
                    */
                    str.setCharAt(i, ' ');
                    
                } else if ((i == lastChar) && (Character.isLetter(str.charAt(lastChar)))) {
               /*
                * If the dot is at the end of the field and the previous one
                * is a letter
                */
                    
                    /* Check if the token is a middle initial */
                    if (numberOfChars == 2) {
                        sVar.midInitialFlag[index] = true;
                        
                    } else if (numberOfChars > 2) {
                    /* Check if the token is a title or a generational suffix */
                        
                        sVar.titlePreFlag[index] = true;
                        sVar.genSuffixPreFlag[index] = true;
                    }
                    
                    /* Finally, replace the dot with a white space. */
                    str.setCharAt(i, ' ');
                    
                } else if ((i == lastChar) 
                    && (Character.isDigit(str.charAt(lastChar)))) {
                           /*
                            * If the dot is at the end of the field and the previous
                            * character is a digit
                            */
                    
                    /* Preflag the token as a generational suffix */
                    sVar.genSuffixPreFlag[index] = true;
                    
                   /* Finally, replace the dot with a white space. */
                    str.setCharAt(i, ' ');
                    
                } else if ((i > 0) && (i < lastChar - 1)
                && Character.isLetterOrDigit(str.charAt(i + 1))
                && Character.isLetterOrDigit(str.charAt(i - 1))) {
                                   /*
                                    * If the dot is in the middle of the field and the surrounding
                                    * characters are alphanumerics
                                    */
                    
                   /*
                    * Before deleting the dot and concatenating the token, check
                    * if it is not an email address or a url. Search for a '@'
                    * and eventually for a '//:' characters.
                    */
                    if (emailCharIndex < i - 1) {
                        
                        for (k = i - 2; k < emailCharIndex; k++) {
                            
                            // Need review. We need to include more characters
                            if (str.charAt(k) == ' ') {
                                break;
                                
                            } else if (k == (emailCharIndex - 1)) {
                                
                                sVar.emailFlag[index] = true;
                            }
                        }
                    }
                    
                   /*
                    * Delete the character and concatenate the bordering
                    * fields
                    */
                    str.deleteCharAt(i);
                    
                } else {
                    
                    /* If the dot is isolated from the fields */
                    str.setCharAt(i, ' ');
                }
            }
            
               /* Check if the character is a '@' */
            if (str.charAt(i) == '@') {
                emailCharIndex = i;
            }
        }
    }
    
    
    /**
     * Method of type 2 handle the special characters ','. It search for
     * possible last name
     *
     * @param str string where we search for special characters of type 1
     * @param specChar the special character
     * @param index
     * @param sVar
     */
    private static void replaceCharOfTypeTwo(StringBuffer str, char specChar, int
    index,
    PersonNameStandVariables sVar) {
        
        int i;
        
        /* The number of characters */
        int numberOfChars = str.length();
        
        for (i = 0; i < numberOfChars; i++) {
            
            if (str.charAt(i) == specChar) {
                
                /* If the comma is at the end of the field */
                if (i == (numberOfChars - 1)) {
                    
                    sVar.lastNamePreFlag[index] = true;
                }
                
               /* Replace with a white space. */
                str.setCharAt(i, ' ');
            }
        }
    }
    
    
    /**
     * Method of type 3 handle the special characters '-'. It search for
     * different patterns related to first name, and others.
     *
     * @param str string where we search for special characters of type 1
     * @param specChar the special character
     * @param numberOfTokens
     * @param index
     * @param sVar
     */
    private static void replaceCharOfTypeThree(StringBuffer str, char specChar, int
    numberOfTokens,
    int index, PersonNameStandVariables sVar) {
        
        int i;
        
        /* The number of characters */
        int numberOfChars = str.length();
        
            /* to simplify the coding */
        int lastChar = numberOfChars - 1;
        
        for (i = 0; i < numberOfChars; i++) {
            
            if (str.charAt(i) == specChar) {
                
                /* If the dash is at the end of the field */
                if (i == (numberOfChars - 1)) {
                    
                    /* Check if there is another token ahead of this one */
                    if (i < numberOfTokens - 1) {
                        
                                    /* First part of first name */
                        sVar.firstNameFirstPreFlag[index] = true;
                        
                        /* Second part of first name */
                        sVar.firstNameSecondPreFlag[index] = true;
                    }
                    
                        /* Replace with a white space. */
                    str.setCharAt(i, ' ');
                    
                } else if ((i > 0) && (i < lastChar - 1)
                && Character.isLetterOrDigit(str.charAt(i + 1))
                && Character.isLetterOrDigit(str.charAt(i - 1))) {
                                            /* If the dash is in the middle of the field */
                    
                            /* This signal a full first name. We should keep the dash */
                    sVar.firstNameDashPreFlag[index] = true;
                    
                   /*
                    * This could signal also a last name with prefix.
                    * keep the dash
                    */
                    sVar.lastNameDashPreFlag[index] = true;
                    
                } else {
                        /* If the dash is at the start of the field */
                    
                   /* Replace with a white space. */
                    str.setCharAt(i, ' ');
                }
            }
        }
    }
    
    
    /**
     * Method of type 4 handle the special characters '\''. It search for
     * different patterns related to certain last names.
     *
     * @param str string where we search for special characters of type 1
     * @param specChar the special character
     * @param numberOfTokens
     * @param index
     * @param sVar
     */
    private static void replaceCharOfTypeFour(StringBuffer str, char specChar, int
    numberOfTokens,
    int index, PersonNameStandVariables sVar) {
        
        int i;
        
        /* The number of characters */
        int numberOfChars = str.length();
        
            /* to simplify the coding */
        int lastChar = numberOfChars - 1;
        
        for (i = 0; i < numberOfChars; i++) {
            
            if (str.charAt(i) == specChar) {
                
                    /* If the apostrophe is in the middle of the field */
                if ((i > 0) && (i < lastChar - 1)
                && Character.isLetterOrDigit(str.charAt(i + 1))
                && Character.isLetterOrDigit(str.charAt(i - 1))) {
                    
                    sVar.lastNamePreFlag[index] = true;
                    
                                    /* remove the apostrophe */
                    str.deleteCharAt(i);
                    
                } else {
                    /* Replace with a white space. */
                    str.setCharAt(i, ' ');
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
    private static void removeCharFromString(StringBuffer str, char specChar) {
        
        int i;
        
        /* The number of characters */
        int numberOfChars = str.length();
        
        for (i = 0; i < numberOfChars; i++) {
            
            if (str.charAt(i) == specChar) {
                
               /*
                * Delete the character and concatenate the bordering
                * fields
                */
                str.deleteCharAt(i);
            }
        }
    }
    
    /**
     * Method that removes some special characters if found anywhere
     * inside the string. These characters are provided inside a list table
     *
     * @param str string where we search for special characters of type 1
     */
    private static void replaceWithWhite(StringBuffer str) {
        
        int i;
        
        /* The number of characters */
        int numberOfChars = str.length();
        
        for (i = 0; i < numberOfChars; i++) {
            
            if ((str.charAt(i) != '-' ||  str.charAt(i) != '&#x2013;')
            && !Character.isLetterOrDigit(str.charAt(i))) {
                
               /*
                * Delete the character and concatenate the bordering
                * fields
                */
                str.deleteCharAt(i);
            }
        }
    }
}

