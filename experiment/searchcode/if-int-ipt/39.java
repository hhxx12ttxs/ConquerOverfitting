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
package com.stc.sbme.stand.address;

import java.util.ArrayList;

import com.stc.sbme.api.SbmeMatchEngineException;
import com.stc.sbme.api.SbmeStandardizationException;
import com.stc.sbme.stand.util.BooleanWrapper;
import com.stc.sbme.stand.util.ReadStandConstantsValues;
import com.stc.sbme.util.EmeUtil;
import com.stc.sbme.util.SbmeLogUtil;
import com.stc.sbme.util.SbmeLogger;
/**
 * 
 * 
 * @author Sofiane Ouaguenouni
 * @version $Revision: 1.1.2.1 $
 */
class AddressPatternsFinder {


    private final int NUMBER_OF_OUTPUT_TOKEN_TYPES;
    private final int MAX_INPUT_TOKENS;

    private final double H_STAR_WEIGHT;
    private final double T_STAR_WEIGHT;
    private final double P_STAR_WEIGHT;
    private final double R_STAR_WEIGHT;
    private final double W_STAR_WEIGHT;
    private final double B_STAR_WEIGHT;
    private final double U_STAR_WEIGHT;
    private final double H_PLUS_WEIGHT;
    private final double B_PLUS_WEIGHT;
    private final double P_PLUS_WEIGHT;
    private final double R_PLUS_WEIGHT;
    private final double W_PLUS_WEIGHT;

    // Read from ReadStandConstantsValues class the size of the string arrays
    private final int PATSIZE;
    private final int MAX_PATT_SIZE;
    private final int MAX_OUTPUT_PATTERNS;

    private final String PAT_TYPES;
    private final String TOKEN_TYPES;
    private final SbmeLogger mLogger = SbmeLogUtil.getLogger(this);  
    
    private String[] patTypes;
    private String[] validOutputTokenTypes;

    private OutputPattern[] pattsFound;

    private int iTT;
    private int iPT;

    private boolean weightScores = true;
    
    
    private final double unmatchPts;
    
    private final int[] curClpos;
    private final int[] maxIntoks;
    private int enp;
    private boolean morePatts;
    private int fr;
    private int to;
    
    private final SearchComboPatterns searchComboPatterns;
    private final SearchPatternTable searchPatternTable;

    AddressPatternsFinder(String domain) {
        NUMBER_OF_OUTPUT_TOKEN_TYPES = ReadStandConstantsValues.getInstance(domain).getNumberOfOutputTokenTypes();
        MAX_INPUT_TOKENS = ReadStandConstantsValues.getInstance(domain).getMaxInputTokens();
        H_STAR_WEIGHT = ReadStandConstantsValues.getInstance(domain).getHStarWeight();
        T_STAR_WEIGHT = ReadStandConstantsValues.getInstance(domain).getTStarWeight();
        P_STAR_WEIGHT = ReadStandConstantsValues.getInstance(domain).getPStarWeight();
        R_STAR_WEIGHT = ReadStandConstantsValues.getInstance(domain).getRStarWeight();
        W_STAR_WEIGHT = ReadStandConstantsValues.getInstance(domain).getWStarWeight();
        B_STAR_WEIGHT = ReadStandConstantsValues.getInstance(domain).getBStarWeight();
        U_STAR_WEIGHT = ReadStandConstantsValues.getInstance(domain).getUStarWeight();
        H_PLUS_WEIGHT = ReadStandConstantsValues.getInstance(domain).getHPlusWeight();
        B_PLUS_WEIGHT = ReadStandConstantsValues.getInstance(domain).getBPlusWeight();
        P_PLUS_WEIGHT = ReadStandConstantsValues.getInstance(domain).getPPlusWeight();
        R_PLUS_WEIGHT = ReadStandConstantsValues.getInstance(domain).getRPlusWeight();
        W_PLUS_WEIGHT = ReadStandConstantsValues.getInstance(domain).getWPlusWeight();

        // Read from ReadStandConstantsValues class the size of the string arrays
        PATSIZE = ReadStandConstantsValues.getInstance(domain).getPatSize();
        MAX_PATT_SIZE = ReadStandConstantsValues.getInstance(domain).getMaxPattSize();
        MAX_OUTPUT_PATTERNS = ReadStandConstantsValues.getInstance(domain).getMaxOutputPatterns();

        PAT_TYPES = ReadStandConstantsValues.getInstance(domain).getPatTypes();
        TOKEN_TYPES = ReadStandConstantsValues.getInstance(domain).getTokenTypes();
        
        pattsFound = new OutputPattern[MAX_OUTPUT_PATTERNS];

        int i;

        // Call method to read from the above strings, assign them to sting
        // array elements, and return their number // Read tokens of each of the
        // above strings 
        validOutputTokenTypes = EmeUtil.stringArrayTokens(TOKEN_TYPES, " ");
        patTypes = EmeUtil.stringArrayTokens(PAT_TYPES, " ");

        for (i = 0; i < MAX_OUTPUT_PATTERNS; i++) {
            pattsFound[i] = new OutputPattern(domain);
        }
        
        unmatchPts = Double.parseDouble(ReadStandConstantsValues.getInstance(domain).getUnmatchPts());

        curClpos = new int[MAX_INPUT_TOKENS + 1];
        maxIntoks = new int[MAX_INPUT_TOKENS + 1];
        searchComboPatterns = new SearchComboPatterns(domain);
        searchPatternTable = new SearchPatternTable(domain);
    }

    /**
     * Define a factory method that return an instance of the class 
     * @return a AddressPatternFinder instance
     */
    static AddressPatternsFinder getInstance(String domain) {
        return new AddressPatternsFinder(domain);
    }


    /**
     * Search for the best pattern (or patterns if more than one person is in a
     * string) for a one-string person name. Note that such pattern is domain-
     * independent because the structure of names is quasi-universal
     *
     * @param orderedKeys aaa
     * @param fieldList aaa
     * @param sVar aaa
     * @return a 2D array of pattern strings
     */
    String[][] findPatterns(String[][] orderedKeys, ArrayList fieldList,
                                   AddressStandVariables sVar) {

        return null;
    }


    /**
     * Search for the best patterns (we can have more than one pattern per string)
     * for an array of strings representing person names.
     *
     * @param orderedKeys aaa
     * @param fieldList aaa
     * @param sVar aaa
     * @return a 3D array of pattern strings
     */
    String[][][] findPatterns(String[][][] orderedKeys, ArrayList[]
                                       fieldList, AddressStandVariables sVar) {

        /* Number of records */
        int numberOfRecords = fieldList.length;

        return null;
    }


    /**
     *
     *
     * @param returnPatt aaa
     * @param fieldList aaa
     * @param sVar aaa
     * @param state the state code
     * @param zip the zip code
     * @throws SbmeStandardizationExceptionan an exception
     * @throws SbmeMatchEngineException an exception
     * @return the number of patterns
     */
    int getBestPatterns(OutputPattern[] returnPatt, ArrayList fieldList, AddressStandVariables sVar,
                        StringBuffer state, StringBuffer zip, int[] lineNums, String domain)
        throws SbmeStandardizationException, SbmeMatchEngineException {

        /* The indexes of the found patterns in the table */
        int[] ind = new int[3];

        /* Number of matched patterns */
        short numberMatch = 0;
        
        /* Number of returned patterns */
        int numberOutPat;

        /* Number of records */
        int numberOfFields = fieldList.size();

        
        /* Define the ArrayList of AddressOutputPattern objetcs to be returned */
        ArrayList retPat = new ArrayList();

 
        iTT = validOutputTokenTypes.length;
        iPT = patTypes.length;

        int i;
        int ito;
        int score = 0;
        BooleanWrapper found9999 = new BooleanWrapper();
        int pass;
        int returnNumpatts = 0;
        int numpatts = 0;
        
        int highestScore = -10000;
        
        StringBuffer nextpatt = new StringBuffer(200);


        
        patTypes = new String[iPT];
        
        for (ito = 0; ito < iPT; ito++) {
            patTypes[ito] = patTypes[ito];
        }


        // initially, we have NUMBER_OF_OUTPUT_TOKEN_TYPES instead of iTT
        //D.C Comment this out
/*
        validOutputTokenTypes = new String[iTT];

        for (ito = 0; ito < iPT; ito++) {
        
            validOutputTokenTypes[ito] = validOutputTokenTypes[ito];
        }
*/
        StringBuffer[] tokArray = new StringBuffer[MAX_INPUT_TOKENS];


        // loadPositionArray loads the array maxIntoks, which contains
        // the number of input tokens for each clue in the pattern.
        // EX:  123 main ave rt 4,  where "rt" has 2 tokens, HR and RR.
        loadPositionArray(sVar.tokensSt, numberOfFields);

        enp = numberOfFields - 1;
        morePatts = true;
 
 
        // nextPatternPermutation returns every possible pattern combination from
        // tokensSt. EX:  123 main st has 2 possible patterns:  NU AU TY and NU AU AU
        while (nextPatternPermutation(nextpatt, sVar.tokensSt, numberOfFields, found9999)) {
 
 
            // If any clue word in the current pattern string has an id=9999
            // (meaning this clue type is invalid for this usage), then continue
            if ((found9999.getBoolean()).booleanValue()) {
                continue;
            }

            // Loads tokArray, which is a simple array of input tokens for the
            // current pattern
            buildTokenArray(tokArray, nextpatt, numberOfFields);

            // process each pattern string
            if ((numpatts = processPattern(tokArray, numberOfFields, pattsFound, domain,
                sVar.usf, state, zip, lineNums, fieldList)) == 0) {
                
                mLogger.fatal("ERROR:  the address pattern " + nextpatt + " is not found");
                throw new SbmeStandardizationException("ERROR:  Nothing could be done with the "
                     + "following pattern: " + nextpatt);
            }

            // Check for a blowout
            if (numpatts > MAX_OUTPUT_PATTERNS) {
                
                mLogger.fatal("ERROR:  too many subpatterns were needed for\n"
                + "        the pattern: " + nextpatt + "        Max allowed: " + MAX_OUTPUT_PATTERNS);

                throw new SbmeStandardizationException("ERROR:  too many subpatterns were needed for\n"
                + "        the pattern: " + nextpatt + "        Max allowed: " + MAX_OUTPUT_PATTERNS);
            }


            // for each pattern string, call a function (searchComboPatterns)
            // TWICE (hence giving it 2 chances to find combinations of
            // combination patterns) that will search for sub-pattern combinations,
            // and a function (calculateScore) which calculates a score for the
            // current pattern string.
            pass = 1;
            searchComboPatterns.searchComboPatterns(pattsFound, numpatts, pass,
                                                      sVar.usf, state, zip, lineNums, fieldList);

            pass = 2;
            searchComboPatterns.searchComboPatterns(pattsFound, numpatts, pass,
                                                      sVar.usf, state, zip, lineNums, fieldList);
                                                      

            // Resolve NL tokens, if any
            checkForNL(pattsFound, numpatts, domain);

            score = calculateScore(pattsFound, numpatts);

            // Save the highest scoring pattern
            if (score > highestScore) {
            
                highestScore = score;
                returnNumpatts = numpatts;
                
                loadReturnPatternStruct(pattsFound, numpatts, returnPatt);
            }

            // free token array
            for (i = 0; i < MAX_INPUT_TOKENS; i++) {
                tokArray[i] = null;
            }

            if (score >= 90) {
                break;
            }
        }

        /* Return the number of patterns */
        return returnNumpatts;
    }
    
    
    /*
     * Calculates the next possible pattern string from the array of 
     * token structs (sVar.tokensSt).
     */
    private boolean nextPatternPermutation(StringBuffer pattstring, TokenStruct[] tst,
                                                  int numtok, BooleanWrapper found9999) 
        throws SbmeStandardizationException, SbmeMatchEngineException {
        int i;
        int back = 1;

        // build the pattern string
        if (!morePatts) {
            return false;
        }
        
        found9999.setBoolean(Boolean.valueOf(buildString(pattstring, tst, numtok)));
        
        // check for a blowout
        if (pattstring.length() > MAX_PATT_SIZE) {

            mLogger.fatal("ERROR:  pattern string is longer than the maximum: " + MAX_PATT_SIZE);
            throw new SbmeStandardizationException("ERROR:  pattern string is too long: " + pattstring
                + "        Max length is " + MAX_PATT_SIZE);
        }

        if (lastPermutation(numtok)) {
            morePatts = false;
            return true;
        }

        // Calculate the next permutation.  The algorithm here is
        // basically the same as nested do loops.
        curClpos[enp]++;


        if (curClpos[enp] > maxIntoks[enp]) {
        
            curClpos[enp] = 1;

            if ((enp - back) < 0) {
                
                mLogger.error("negative index: " + enp + " - " + back);
                throw new SbmeStandardizationException("negative index: " + enp + " - " + back);
            }

            curClpos[enp - back]++;

            while (curClpos[enp - back] > maxIntoks[enp - back]) {
            
                curClpos[enp - back] = 1;
                back++;
                
                if ((enp - back) < 0) {
                
                    morePatts = false;
                    break;
                }
                curClpos[enp - back]++;
            }
            back = 1;
        }
        return true;
    }
 
 
    /*
     * Loops through the array of token structs and tst, and loads the integer token
     * array maxIntoks.
     */
    private void loadPositionArray(TokenStruct[] tst, int numTokens) {
    
        int i;

        for (i = 0; i < numTokens; i++) {
        
            if (EmeUtil.strcmp(tst[i].getClueType1(), "  ") == 0) {
                maxIntoks[i] = 0;
                
            } else if (EmeUtil.strcmp(tst[i].getClueType2(), "  ") == 0) {
                maxIntoks[i] = 1;
                
            } else if (EmeUtil.strcmp(tst[i].getClueType3(), "  ") == 0) {
                maxIntoks[i] = 2;
                
            } else if (EmeUtil.strcmp(tst[i].getClueType4(), "  ") == 0) {
                maxIntoks[i] = 3;
                
            } else if (EmeUtil.strcmp(tst[i].getClueType5(), "  ") == 0) {
                maxIntoks[i] = 4;
                
            } else {
                maxIntoks[i] = 5;
            }
        }

        for (i = 0; i < MAX_INPUT_TOKENS; i++) {
            curClpos[i] = 1;
        }
    }
    

    /*
     *  Returns true if all permutations of the input tokens have been processed.
     */
    private boolean lastPermutation(int numTok) {
    
        int i;

        for (i = 0; i < numTok; i++) {
        
            if (curClpos[i] < maxIntoks[i]) {
                return false;
            }
        }
        return true;
    }


    /**
     *  Constructs a character string of input tokens from tst.
     *  Returns true if any tokens have id = 9999
     * 
     * @param string an input string
     * @param tst aaa
     * @param numtok aaa
     * @return a boolean 
     * @throws SbmeMatchEngineException an exception
     */
    private  boolean buildString(StringBuffer string, TokenStruct[] tst, int numtok)
        throws SbmeMatchEngineException {
    
        int i;
        boolean temp = false;

        for (i = 0; i < numtok; i++) {

            if (curClpos[i] == 1) {

                EmeUtil.strncpy(string, i * 3, tst[i].getClueType1(), 2);

                if (tst[i].getClueWordId1() == 9999) {
                    temp = true;
                }
                
            } else if (curClpos[i] == 2) {
            
                EmeUtil.strncpy(string, i * 3, tst[i].getClueType2(), 2);

                if (tst[i].getClueWordId2() == 9999) {
                    temp = true;
                }
                
            } else if (curClpos[i] == 3) {
            
                EmeUtil.strncpy(string, i * 3, tst[i].getClueType3(), 2);

                if (tst[i].getClueWordId3() == 9999) {
                    temp = true;
                }
                
            } else if (curClpos[i] == 4) {
            
                EmeUtil.strncpy(string, i * 3, tst[i].getClueType4(), 2);

                if (tst[i].getClueWordId4() == 9999) {
                    temp = true;
                }
                
            } else if (curClpos[i] == 5) {
            
                EmeUtil.strncpy(string, i * 3, tst[i].getClueType5(), 2);

                if (tst[i].getClueWordId5() == 9999) {
                    temp = true;
                }
            }

            EmeUtil.strncpy(string, i * 3 + 2, " ", 1);
            
        }
        string.setLength((i-1) * 3 + 2);
        return temp;
    }


    /*
     *  Loads the array of input tokens and tokArray from the pattern string patt.
     */
    private void buildTokenArray(StringBuffer[] tokArray, StringBuffer patt,
                                        int numTokens)
        throws SbmeStandardizationException, SbmeMatchEngineException {
        
        int i;
        int pos;

        for (i = 0, pos = 0; i < numTokens; i++, pos += 3) {
        
            if (patt.length() <= pos) {
                break;
            }

            tokArray[i] = new StringBuffer(3);
            
            EmeUtil.sprintf(tokArray[i], "%.2s", (i < numTokens) ? patt.substring(pos) : " ");   
        }
    }


    /*
     * Main function that processes each pattern string, searching for
     * combinations or whole pattern strings in the pattern table.
     */
    private int processPattern(StringBuffer[] tokArray, int numtoks, OutputPattern[] pattsFound, String domain,
                               char usf, StringBuffer state, StringBuffer zip, int[] lineNums, ArrayList fieldList)
        throws SbmeStandardizationException, SbmeMatchEngineException {
        
        int priorityVals[] = {80, 40, 0};
        int found = 0;
        int highpri = 0;
        int minpri;
        int i;
        int[] outToksUsed = new int[NUMBER_OF_OUTPUT_TOKEN_TYPES];
        
        PatternTable ptrec;
        StringBuffer ip = new StringBuffer(MAX_PATT_SIZE);
        StringBuffer op = new StringBuffer(MAX_PATT_SIZE);


        // initialize the "output tokens used" array
        for (i = 0; i < NUMBER_OF_OUTPUT_TOKEN_TYPES; i++) {
            outToksUsed[i] = 0;
        }

        // First, search for the entire token array, calling findPatt
        // up to 3 times; each time lowering the priority if nothing was found.
        i = 0;
        fr = 1;
        to = numtoks;

        while ((ptrec = findPatt(tokArray, ip, op, outToksUsed, priorityVals[i],
                                 usf, state, zip, lineNums, fieldList, domain)) == null) {
                                 
            if (priorityVals[i] == 0) {
                break;
            }

            fr = 1;
            to = numtoks;
            i++;
        }
        

        // set the highest priority, increment the number of patterns found
        //counter, and load the output pattern struct
        if (ptrec != null) {

            highpri = Integer.parseInt(ptrec.getPriority().toString());

            loadOutputPatternStruct(pattsFound, found, ip, op, ptrec, tokArray);

            found++;
        }


        // Loop through the unfound token array, processing everything
        // that wasnt found before.
        while (nextUnfoundPatternString(tokArray, numtoks)) {

            minpri = (highpri >= 60) ? 40 : 0;

            // Search for the next sub-pattern.  ONLY if findPatt returns
            // true (ie. not NULL) you should check to re-set the highest
            // priority found.
            ptrec = findPatt(tokArray, ip, op, outToksUsed, minpri, usf, state, zip, lineNums, fieldList, domain);

            if (ptrec != null) {
  
                loadOutputPatternStruct(pattsFound, found, ip, op, ptrec, tokArray);

                int ite = Integer.parseInt(ptrec.getPriority().toString());
                
                highpri = ((ite > highpri) ? ite : highpri);
                
            } else {
            
                // Set to = fr because I only want to add single EI strings to
                // the pattsFound structure
                to = fr;
                loadOutputPatternStruct(pattsFound, found, ip, op, ptrec, tokArray);

                dirtyTokenArray(tokArray, fr, to);          
            }
            found++;
        
            //return found;
        }
        
        return found;
    }


    /*
     *  findPatt is the main searching function that tries to find
     *  any sub-pattern in the pattern table.
     */
    private PatternTable findPatt(StringBuffer[] tokArray, StringBuffer ip, StringBuffer op,
            int[] outToksUsed, int minpri, char usf, StringBuffer state, StringBuffer zip, 
            int[] lineNums, ArrayList fieldList, String domain)
        throws SbmeStandardizationException, SbmeMatchEngineException {
        
        int i;
        int gap;
        int st;
        int end;
        int origGap;
        int numIts;
        
        StringBuffer string = new StringBuffer(MAX_PATT_SIZE);
        PatternTable temp;

        /*
         *  The first pattern search (ie. when you are initially searching for
         *  the entire pattern) is different then subsequent searches.
         *
         *  Anytime a pattern is found, you must make sure that the following
         *  conditions are met:
         *      (1).  The priority is >= minpri
         *      (2).  The pattern type hasn't already been found.
         *
         *
         *  1ST CHECK:  Bias at the beginning of the pattern
         *  EXAMPLE:  Assume    123 main ave apt 2 a, then the first searches are:
         *                       1   2    3   4  5 6
         *                       1   2    3   4  5
         *                       1   2    3   4
         *                       1   2    3
         *                       1   2
         *                       1
         */             
        for (st = fr, end = to; end - st >= 0; end--) {

 
            buildStringFromTokenArray(string, tokArray, st, end);


            if (((temp = searchPatternTable.searchPatternTable(string, ip, op, usf, state, zip)) != null)
                && (Integer.parseInt(temp.getPriority().toString()) >= minpri)
                && (!outputTokAlreadyUsed(op, outToksUsed))) {


                // If it is found, then set the dirty bit in outToksUsed, and return
                outToksUsed = setOutputTokenAsUsed(op, outToksUsed);

                dirtyTokenArray(tokArray, st, end);

                fr = st;
                to = end;
         
                return temp;
            }
        }


        /*
         *  2ND CHECK:  Bias at the end of the pattern
         *  EXAMPLE (from above):     2  3  4  5  6
         *                               3  4  5  6
         *                                  4  5  6
         *                                     5  6
         *                                        6
         */
        for (st = fr + 1, end = to; (end - st) >= 0; st++) {
 
            buildStringFromTokenArray(string, tokArray, st, end);

            if (((temp = searchPatternTable.searchPatternTable(string, ip, op, usf, state, zip)) != null)
                && (Integer.parseInt(temp.getPriority().toString()) >= minpri)
                && (!outputTokAlreadyUsed(op, outToksUsed))) {           

                // If its found, then set the dirty bit in outToksUsed, and return
                outToksUsed = setOutputTokenAsUsed(op, outToksUsed);
                
                dirtyTokenArray(tokArray, st, end);

                fr = st;
                to = end;
                return temp;
            }
        }

        /*
         *  FINAL CHECK:  eliminate the beginning and ending tokens, and
         *                search for the longest pattern in between.
         *
         *  EXAMPLE (from above):    2  3  4  5   search all "4"s
         *
         *                           2  3  4      search all "3"s
         *                              3  4  5
         *
         *                           2  3         search all "2"s
         *                              3  4
         *                                 4  5
         *
         *                           2            search all "1"s
         *                              3
         *                                 4
         *                                    5
         */
        end = to - 1;
        st = fr + 1;
        
        gap = end - st;
        origGap = gap;

        while (gap >= 0) {
        
            numIts = (origGap - gap) + 1;
          
            for (i = 0; i < numIts; i++) {
            
                buildStringFromTokenArray(string, tokArray, st + i, st + i + gap);

                if (((temp = searchPatternTable.searchPatternTable(string, ip, op, usf, state, zip)) != null)
                    && (Integer.parseInt(temp.getPriority().toString()) >= minpri)
                    && (!outputTokAlreadyUsed(op, outToksUsed))) {

                    // If it is found, then set the dirty bit in outToksUsed, and
                    // return
                    outToksUsed = setOutputTokenAsUsed(op, outToksUsed);
                    
                    dirtyTokenArray(tokArray, st + i, st + i + gap);

                    fr = st + i;
                    to = st + i + gap;

                    return temp;
                }   
            }   
            gap--;
        }   

        // if nothing was found, then dirty the Token between the original
        // starting and ending tokens and return NULL
        return null;
    }
    
    
    /**
     *  Uses the token array "tokArray" to build a character string of input tokens
     *  from "frr" to "too".
     *
     * @param string an input string
     * @param tokArray aaa
     * @param frr the start index
     * @return too the end index
     * @throws SbmeStandardizationException an exception
     * @throws SbmeMatchEngineException an exception
     */
    void buildStringFromTokenArray(StringBuffer string, StringBuffer[] tokArray,
                                          int frr, int too)
        throws SbmeStandardizationException, SbmeMatchEngineException {
        
        int st;
        int i;

        for (i = 0, st = frr; st <= too; st++, i += 3) {

            EmeUtil.sprintf(string, i, "%.2s", tokArray[st - 1]);

            string.setLength(i + 3);
            string.setCharAt(i + 2, ' ');
        }

        string.setLength(i - 1);
    }


    /**
     *  Uses the token array "tokArray" to build a character string of input tokens
     *  from "frr" to "too".
     *
     * @param string an input string
     * @param tokArray aaa
     * @param frr the start index
     * @return too the end index
     * @throws SbmeStandardizationException an exception
     * @throws SbmeMatchEngineException an exception
     */
    void buildStringFromTokenArray(StringBuffer string, StringBuffer[] tokArray)
        throws SbmeStandardizationException, SbmeMatchEngineException {
        
        int st;
        int i;

        for (i = 0, st = fr; st <= to; st++, i += 3) {

            EmeUtil.sprintf(string, i, "%.2s", tokArray[st - 1]);

            string.setLength(i + 3);
            string.setCharAt(i + 2, ' ');
        }
        string.setLength(i - 1);
        
    }


    /*
     *  Uses the token array "tokArray" to determine the next consecutive set of
     *  unused input tokens that haven't been used. Returns their position in "fr" 
     *  and "to".
     */
    private boolean nextUnfoundPatternString(StringBuffer[] tokArray, int numtok) {
    
        int i;
        fr = 0;
        to = 0;

        for (i = 0; i < numtok; i++) {


            if ((EmeUtil.strncmp(tokArray[i], "  ", 2) == 0)
                || (EmeUtil.strncmp(tokArray[i], "--", 2) == 0)) {
                
                if (fr != 0) {
                    break;
                    
                } else {
                    continue;
                }
            }

            if (fr == 0) {
                fr = i + 1;
            }
            to = i + 1;
        }

        return ((fr == 0 || to == 0) ? false : true);
    }

    /*
     *  Retrieves the input tokens from "st" to "end" in the token array "tokArray"
     *  and change their values to "--", meaning they've been processed.
     */
    private void dirtyTokenArray(StringBuffer[] tokArray, int st, int end)
        throws SbmeStandardizationException, SbmeMatchEngineException {
        
        while (st <= end) {
        
            EmeUtil.sprintf(tokArray[st - 1], "%.2s", "--");
            st++;
        }
    }

    /*
     *  Receives an output token string just found and process each output token in
     *  the string, and "dirty" it's respective element in the array, outToksUsed;
     *  hence indicating that output token is not allowed in any pattern searching.
     */
    private int[] setOutputTokenAsUsed(StringBuffer string, int[] outToksUsed) 
    throws SbmeStandardizationException {
    
        int i;
        int j;
        int numpatts;
        boolean found = false;
        numpatts = (AddressClueWordsTable.trimln(string) + 1) / 3;

        for (j = 0; j < numpatts; j++) {
        
            for (i = 0; i < NUMBER_OF_OUTPUT_TOKEN_TYPES; i++) {
            
                if (EmeUtil.strncmp(string, j * 3, validOutputTokenTypes[i], 2) == 0) {
                
                    outToksUsed[i] = 1;
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            throw new SbmeStandardizationException("Invalid token: " + string);
        }
        return outToksUsed;
    }


    /*
     *  Returns true if any output token in the string has been already
     *  used or found in previous searches.
     *
     */
    private boolean outputTokAlreadyUsed(StringBuffer string, int[] outToksUsed) {
    
        int i;
        int j;
        int numTokens;
        
        numTokens = (AddressClueWordsTable.trimln(string) + 1) / 3;

        for (j = 0; j < numTokens; j++) {
        
            for (i = 0; i < NUMBER_OF_OUTPUT_TOKEN_TYPES; i++) {
            
                if (EmeUtil.strncmp(string, j * 3, validOutputTokenTypes[i], 2) == 0) {
                
                    if (outToksUsed[i] == 1) {
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }


    /*
     *  Loads the array of pattern structs "pattsFound" with its required information.
     */
    private void loadOutputPatternStruct(OutputPattern[] pattsFound, int found, StringBuffer ip,
            StringBuffer op, PatternTable ptrec, StringBuffer[] tokArray)
        throws SbmeStandardizationException, SbmeMatchEngineException {
        
        int i;
        StringBuffer str = new StringBuffer(MAX_PATT_SIZE);

        // If the pattern string from "fr" to "to" in the token array, tokArray,
        // couldn't be found then the pattern is loaded as extra information


        // If pattern not found
        if (ptrec == null) {

            pattsFound[found].setInputPatt(EmeUtil.memset(pattsFound[found].getInputPatt(), ' ',
                                            PATSIZE).toString());

            pattsFound[found].setOutputPatt(EmeUtil.memset(pattsFound[found].getOutputPatt(),
                                             ' ', PATSIZE).toString());

            buildStringFromTokenArray(str, tokArray);
            
            pattsFound[found].setInputPatt(EmeUtil.sprintf(pattsFound[found].getInputPatt(),
                    "%." + AddressClueWordsTable.trimln(str) + "s", str).toString());


            for (i = 0; i <= (to - fr); i++) {
            
                pattsFound[found].setOutputPatt(EmeUtil.sprintf(pattsFound[found].getOutputPatt(),
                        3 * i, "%.3s", (i == (to - fr) ? "EI" : "EI ")));
            }
            

            pattsFound[found].setPatternType(EmeUtil.sprintf(pattsFound[found].getPatternType(),
                    "%.2s", "EI").toString());


            pattsFound[found].setPriority(EmeUtil.sprintf(pattsFound[found].getPriority(),
                    "%.2s", "30").toString());
  
                    
        } else {
        
            pattsFound[found].setInputPatt(EmeUtil.memset(pattsFound[found].getInputPatt(),
                    ' ', PATSIZE).toString());

            pattsFound[found].setOutputPatt(EmeUtil.memset(pattsFound[found].getOutputPatt(),
                    ' ', PATSIZE).toString());
            
            pattsFound[found].setInputPatt(EmeUtil.sprintf(pattsFound[found].getInputPatt(),
                    "%." + AddressClueWordsTable.trimln(ip) + "s", ip).toString());

            pattsFound[found].setOutputPatt(EmeUtil.sprintf(pattsFound[found].getOutputPatt(),
                    "%." + AddressClueWordsTable.trimln(op) + "s", op).toString());

            pattsFound[found].setPatternType(EmeUtil.sprintf (pattsFound[found].getPatternType(),
                    "%.2s", ptrec.getPatternType()).toString());

            pattsFound[found].setPriority(EmeUtil.sprintf(pattsFound[found].getPriority(),
                    "%.2s", ptrec.getPriority()).toString());
                    
        }            
        
        pattsFound[found].setBeg(fr);
        pattsFound[found].setEnd(to);
 
    }


    /*
     *  Determines a score for a pattern based on the following calculation:
     *  score = avg. priority of all sub-patterns found (minus) total number
     *  of unmatched tokens x PT, where PT = 0 for EI's embedded in patterns
     *  and PT = 20 for singular EI's.
     */
    private int calculateScore(OutputPattern[] pattsFound, int numpatts) {
    
        int j;
        int len;
        int i;
        int gap;
        int avgpri;
        int unmatchpatt = 0;
        int unmatchtok = 0;
        int longpatt = 0;
        
        boolean plusfound = false; 
        int aufound = 0;

        StringBuffer pos;

        double[] patWeights = {H_STAR_WEIGHT, T_STAR_WEIGHT, P_STAR_WEIGHT,
                                R_STAR_WEIGHT, W_STAR_WEIGHT, B_STAR_WEIGHT,
                                U_STAR_WEIGHT, H_PLUS_WEIGHT, P_PLUS_WEIGHT,
                                R_PLUS_WEIGHT, W_PLUS_WEIGHT, B_PLUS_WEIGHT
                               };

       double np = 0.0;
       double weightSum = 0.0;
       double sweight = 0.0;
       double totpri = 0.0;

       for (i = 0; i < numpatts; i++) {


           // Dont average patterns where pattern type = "EI", but tally
           // the number of unmatched patterns (AUs get tallied only once)
           // D.C. Allow AU type unmatched patterns to be counted three times
           // D.C. This tends to give higher scores to patterns combinations
           // D.C. that account for more tokens.  TODO: Make this configurable?
           if (EmeUtil.strncmp(pattsFound[i].getPatternType(), "EI", 2) == 0) {
           
               if (EmeUtil.strcmp(pattsFound[i].getInputPatt(), "AU") == 0) {
               
                   if (aufound < 3) {
                       unmatchpatt++;
                   }
                   aufound++;
                   
               } else {
                   unmatchpatt++;
               }
               
               continue;
           }

           // Determine the weight for the pattern type
           for (j = 0; j < 8; j++) {

               if (EmeUtil.strcmp(pattsFound[i].getPatternType(), patTypes[j]) == 0) {
                   break;
               }
               sweight = patWeights[j];
           }

           // Dont average in the priority for the current sub-pattern if
           // a "+" pattern type has already been averaged.
           if (pattsFound[i].getPatternType().charAt(1) == '+') {
           
               if (plusfound) {
                   continue;
               }
               plusfound = true;
           }
      
           totpri += Double.parseDouble(pattsFound[i].getPriority().toString())
                   * (weightScores ? sweight : 1.0);

           weightSum += sweight;
           np++;
       }
     
       if (np == 0.0) {
           return 0;
       }

       return (int) (((totpri / (weightScores ? weightSum : np)) + 0.5)  - (unmatchpatt * unmatchPts));
   }
   
   
    /*
     *  Loads the pattern struct "returnPatt" using information from pattsFound,
     *  a structure of the same type.
     */
    private void loadReturnPatternStruct(OutputPattern[] pattsFound, int numpatts,
            OutputPattern[] returnPatt) {
            
        int i;

        for (i = 0; i < MAX_OUTPUT_PATTERNS; i++) {

            // Reset previously loaded info
            returnPatt[i].setInputPatt(EmeUtil.memset(returnPatt[i].getInputPatt(), ' ',
                                       PATSIZE).toString());

            returnPatt[i].setOutputPatt(EmeUtil.memset(returnPatt[i].getOutputPatt(), ' ',
                                        PATSIZE).toString());

            returnPatt[i].setPatternType(EmeUtil.memset(returnPatt[i].getPatternType(), ' ',
                                         3).toString());

            returnPatt[i].setPriority(EmeUtil.memset(returnPatt[i].getPriority(), ' ',
                                      3).toString());

            returnPatt[i].setBeg(0);
            returnPatt[i].setEnd(0);

            // Load new info
            if (i < numpatts) {
            
                returnPatt[i].setInputPatt(pattsFound[i].getInputPatt().toString());
                returnPatt[i].setOutputPatt(pattsFound[i].getOutputPatt().toString());
                returnPatt[i].setPatternType(pattsFound[i].getPatternType().toString());
                returnPatt[i].setPriority(pattsFound[i].getPriority().toString());

                returnPatt[i].setBeg(pattsFound[i].getBeg());
                returnPatt[i].setEnd(pattsFound[i].getEnd());
            }
        }
    }


    /**
     *
     * @param pattsFound aaa 
     * @param numpatts aaa
     */
    void checkForNL(OutputPattern[] pattsFound, int numpatts, String domain) {
    
        int i;
        int j;
        int k;
        int numtoks;

        for (i = 0; i < numpatts; i++) {
        
            if (EmeUtil.strstr(pattsFound[i].getOutputPatt(), "NL") == -1) {
                continue;
            }

            numtoks = (AddressClueWordsTable.trimln(pattsFound[i].getOutputPatt()) + 1) / 3;

            for (j = 0; j < numtoks; j++) {
            
                if (EmeUtil.strncmp(pattsFound[i].getOutputPatt(), j * 3, "NL", 2) == 0) {
                
                    for (k = j + 1; k < numtoks; k++) {
                    
                        if (EmeUtil.strncmp(pattsFound[i].getOutputPatt(), k * 3, "NL", 2) == 0) {
                            continue;
                        }
                        
                        if (EmeUtil.strncmp(pattsFound[i].getOutputPatt(), k * 3, "NA", 2) == 0) {
                        
                            EmeUtil.strncpy(pattsFound[i].getOutputPatt(), j * 3, "NA", 2);

                        } else if (EmeUtil.strncmp(pattsFound[i].getOutputPatt(), k * 3, "BN", 2) == 0) {
                            
                            EmeUtil.strncpy(pattsFound[i].getOutputPatt(), j * 3, "BN", 2);
                        } else if (EmeUtil.strncmp(pattsFound[i].getOutputPatt(), k * 3, "EI", 2) == 0) {
                            
                            if (domain.compareTo("FR") != 0) {                          
                            EmeUtil.strncpy(pattsFound[i].getOutputPatt(), j * 3, "NA", 2);  
                            EmeUtil.strncpy(pattsFound[i].getOutputPatt(), k * 3, "NA", 2);                            
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }
}

