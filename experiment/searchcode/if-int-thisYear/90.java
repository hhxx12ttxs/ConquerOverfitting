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
package com.stc.sbme.match.weightcomp;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import com.stc.sbme.api.SbmeMatchingEngine;
import com.stc.sbme.api.SbmeMatchingException;
import com.stc.sbme.match.util.ReadMatchConstantsValues;
import com.stc.sbme.util.EmeUtil;

/**
 * Compares the A-field and the B-field, and based on the type of
 * comparison chosen, returns the associated matching weight
 *
 * @author Sofiane Ouaguenouni
 * @version $Revision: 1.1.2.1 $
 */
public class WeightComputation {

    // Indices of the characters of the string comparator. Value:{1, 2, 3}
    private static final int PRIMARY = ReadMatchConstantsValues.getPrimary();
    private static final int SECONDARY = ReadMatchConstantsValues.getSecondary();
    private static final int TERTIARY = ReadMatchConstantsValues.getTertiary();
    
    // Available types of matching comparisons
    // A comparison type used with frequency analysis, when dealing with
    // fields like first names, last names, and numeric values.
    private static final int FREQUENCY = ReadMatchConstantsValues.getFrequency();
    // Generic Comparison types
    private static final char EXACT = ReadMatchConstantsValues.getExact();
    private static final char BIGRAM = ReadMatchConstantsValues.getBigram();
    private static final char DATE = ReadMatchConstantsValues.getDate();
    // A comparison type used simultaneously for fields like first names, last names,
    // and numeric values.
    private static final char UNCERTAINTY = ReadMatchConstantsValues.getUncertainty();
    // data-oriented matching Comparators
    // A comparison type used with fields like 'age'.
    private static final char PRORATED = ReadMatchConstantsValues.getProrated();
    // A comparison type used with fields like 'year'.
    private static final char NUMBER = ReadMatchConstantsValues.getNumber();
        
    private static final String NULL60 = ReadMatchConstantsValues.NULL60;
    
    /**
     * Reads two fields respectively from the candidate and the reference record and
     * compute the associated matching weight
     *
     * @param indx An index used to handle situations where we consider
     *       reversing adjacent fields in matching (when 'i' or 'I' is used)
     * @param candRec the candidate record.
     * @param fieldName    The index of a given field in the reference record.
     * @param j the index of the record
     * @param jIndex the index of the key in the config file
     * @param refRec thew reference record
     * @param mEng  An instance of MatchVariable class.
     * @return A real number that measures the field's matching weight(j, k)
     * @throws SbmeMatchingException an exception
     * @throws ParseException a text parsing exception
     */
    public static double weight(int indx, String[] candRec, String fieldName, int j, 
            int jIndex, String[] refRec, SbmeMatchingEngine mEng)
        throws SbmeMatchingException, ParseException {
        
        int i;
        int fieldLength = 0;

        MatchVariables matchVar = mEng.getMatchVar(mEng);
        String recA = null;
        String recB = null; 
               
//        InputPattern pa = InputPattern.compile("\\S");
//        Matcher matA;
//        Matcher matB;
        
        String sFlag = matchVar.nullFlag[jIndex];
        int nFlag = sFlag.length();
        
        char cFlag = sFlag.charAt(0);
        
        if (indx == 0) {
            
            recA = candRec[j];
            recB = refRec[j];
            fieldLength = matchVar.lengthF[jIndex];
            
        } else if (indx == 1) {
            
            recA = candRec[j + 1];
            recB = refRec[j];
            
            // Get the smaller length of close-by fields
            fieldLength = (matchVar.lengthF[jIndex] > matchVar.lengthF[jIndex + 1])
            ? matchVar.lengthF[jIndex + 1] : matchVar.lengthF[jIndex];
            
        } else if (indx == 2) {
            
            recA = candRec[j];
            recB = refRec[j + 1];
            
            // Get the smaller length of close-by fields
            fieldLength = (matchVar.lengthF[jIndex] > matchVar.lengthF[jIndex + 1])
            ? matchVar.lengthF[jIndex + 1] : matchVar.lengthF[jIndex];
            
        } else {
            throw new SbmeMatchingException("Not the right value for indx: Must be < 3");
        }

        // Check if the records lengths are smaller than the lengthF. If not 
        // reduce it them to lengthF
        if ((recA != null) && recA.length() > fieldLength) {
            recA = recA.substring(0, fieldLength);               
        } 
 
        if ((recB != null) && recB.length() > fieldLength) {
            recB = recB.substring(0, fieldLength);               
        } 

        
        // Instance variables of class MatchVariables
        StringBuffer method = matchVar.comparatorType[jIndex];
        
        double agreement = matchVar.agrWeight[jIndex];
        double disagreement = matchVar.disWeight[jIndex];
        double adj1 = matchVar.adjCurv[jIndex << 2];
        double delAgr = agreement - disagreement;
                
        int thisYear = matchVar.year;
        
        // Local variables
        int temp;
        int temp2;
        
        double primaryWeight = 0.0;
        double secondaryWeight = 0.0;
        double comparison = 0.0;
        double result = 0.0;
        
        // Temporary StringBuffers
//        StringBuffer subRecA = new StringBuffer();
//        StringBuffer subRecB = new StringBuffer();
//        StringBuffer subRecA = null;
//        StringBuffer subRecB = null;        
        
        int table;
        int index;
        double adjustment;
        boolean nullA = false;
        boolean nullB = false;
        int maxLength = 0;
        

        /* Regular expression that catches empty strings */
//        matA = pa.matcher(recA);
//        matB = pa.matcher(recB);
        
        /* test if the candidate record is empty or null */
        if ((recA == null) || (recA.length() == 0) /*|| !(matA.lookingAt())*/) {
            nullA = true;
        }
        
        /* test if the candidate record is empty or null */
        if ((recB == null) || (recB.length() == 0) /*|| !(matB.lookingAt())*/) {
            nullB = true;
        }
        
        //Test if one or both records are empty and 
        //the nullFlag has one character only
        if ((nullA || nullB) && (nFlag == 1)) {
            
            if (cFlag == '0') {
                return 0.0;
                
            } else if (cFlag == '1') {
                
                if (nullA & nullB) { 
                    return agreement;
                } else {
                    return disagreement;
                }
            } else if (cFlag == 'a') {
                return agreement / 2.;
                
            } else if (cFlag == 'd') {
                return disagreement / 2.;
                
            }
            
        } else if (nullA || nullB) {

            nFlag = Integer.parseInt(sFlag.substring(1));

            primaryWeight = disagreement / nFlag;
            secondaryWeight = agreement / nFlag;

            // In case we want to return a neutral weight
            if (cFlag == 'a') {
                return secondaryWeight;
            // In case we want to return a negative weight
            } else if (cFlag == 'd') {
                return primaryWeight;
            } else {
                throw new SbmeMatchingException("The type comparator must start with 'a' or 'd'");
            }
        }

        // If the fields match exactly over a length 'fieldLength'
//        if ((recB.compareTo(recA) == 0) && (method.charAt(1) != 'S')) {        
//        if ((EmeUtil.strncmp(recB, recA, fieldLength) == 0) && (method.charAt(1) != 'S')) {
        if ((EmeUtil.compareNStrings(recB, recA, fieldLength)) && (method.charAt(1) != 'S')) {         

          
            // Check first if we have frequency tables we can use to calculate the
            // comparison value. If not, return total agreement
            if (method.charAt(PRIMARY) == FREQUENCY) {
                
                // 'e' like exist
                method.setCharAt(3, 'e');
                table = Integer.parseInt(String.valueOf(method.charAt(SECONDARY)));
                
                // Returns the weight of the fields from the frequency tables.
                // Need review. Not sure this is the right move.
                return FrequencyWeight.getFrequency(recA, recB, fieldLength, table, matchVar);
                
            } else {
                
                // If no frequency data is used, return total agreement
                return agreement;
            }
            
        } else {
        // Approximate string comparators
        
            // If exact match is required
            if (method.charAt(PRIMARY) == EXACT) {
                
                return disagreement;
                
            } else if (method.charAt(PRIMARY) == BIGRAM) {
                // Choose BIGRAM algorithm to calculate the comparison value. Then use it
                // as a linear coefficient in the weight-calculation formula only if the
                // result is higher than 0.8.               
                comparison = Bigram.bigram(recA, recB, method.charAt(SECONDARY));
                
                // Justified by some extensive studies in computer science.
                if (comparison > 0.8) {
                    return disagreement + comparison * delAgr;
                    
                } else {
                    return disagreement;
                }
                
            } else if (method.charAt(PRIMARY) == DATE) {
                
                // Compare the two dates
                comparison = DateComparator.compareDates(recA, recB, jIndex, matchVar);

                // Evaluate the associated weight
                primaryWeight = disagreement + (delAgr) * comparison;
                
                return primaryWeight;
                
            } else if (method.charAt(PRIMARY) == NUMBER) {
                
                // Test first if the second argument in the comparator exists
                if (method.charAt(SECONDARY) == ' ' || method.charAt(SECONDARY) == 'I'
                    || method.charAt(SECONDARY) == 'R' || method.charAt(SECONDARY) == 'S') {
                                      
                        // We assume that we are comparing two real numbers
                        comparison = Number2NumberComparator.compareNumbers(recA, recB, jIndex, matchVar); 
                                                                                                   
                    if (method.charAt(TERTIARY) == 'V') {
                
                        // We assume that we are comparing two real numbers
//                        comparison = Number2IntervalComparator.compareNumbers(recA, intervalB, jIndex, matchVar);
                    }

                } else if (method.charAt(SECONDARY) == 'A') {
                    
                    if (method.charAt(TERTIARY) == '1') {
                    
                        // We assume that we are comparing two real numbers
//                        comparison = AlphaNumericComparators.compareAlphaNumerics(recA, interB, jIndex, matchVar);
                                                                                 
                    } else if (method.charAt(TERTIARY) == '2') {
                    
                        // We assume that we are comparing two real numbers
//                        comparison = AlphaNumericComparators.compareAlphaNumerics(interA, interB, jIndex, matchVar);
                    
                    } else if (method.charAt(TERTIARY) == '3') {
                    
                    } else {
                        throw new SbmeMatchingException("the third character after nA must be 1, 2 or 3");
                    }
                }

                // Evaluate the associated weight
//                primaryWeight = agreement - (agreement - disagreement) * (1 - comparison);
                primaryWeight = disagreement + delAgr * comparison;
                                
                return primaryWeight;
                

            } else if (method.charAt(PRIMARY) == UNCERTAINTY) {
                // This option use the generic Jaro and/or Winkler string comparators

                // Compute the [0, 1] comparison value using the Winkler algorithm
                if (method.charAt(SECONDARY) == 'a') {
                    comparison = StringComparator.genStrComparator(recA, recB, 2, matchVar);
                    
                } else if (method.charAt(SECONDARY) == 's') {
  
                    if (TERTIARY == 'u') {
                        comparison = UnicodeSofStringComparator.compare(recA, recB, jIndex, false, matchVar);
                    } else {                
                        comparison = SofStringComparator.sofStrComparator(recA, recB, jIndex, false, matchVar);
                    }
                    return disagreement + delAgr * comparison;                
                } else {
//                    comparison = SofStringComparator.sofStrComparator(recA, recB, jIndex, false, matchVar);
                    comparison = GenStringComparator.genStrComparator(recA, recB, jIndex, false, matchVar);
                }
 
                // Compare the returned value with the curve-adjustment parameters.
                // If the comparison value is higher than the max adjustment value,
                // return total agreement.
                if (comparison > adj1) {               
                    return agreement;
                    
                } else if ((comparison <= adj1)
                    && (comparison > matchVar.adjCurv[(jIndex << 2) + 1])) {
                    
                    // If the comparison value is in-between the adjustment parameters,
                    // use it a linear adjustment.                    
                    primaryWeight = agreement - delAgr * (1 - comparison) 
                         * matchVar.adjCurv[(jIndex << 2) + 2];

                    // If the calculated weight is lower than the minimum authorized
                    // limit, replace it with the minimum value.
                    if (primaryWeight < disagreement) {
                        primaryWeight = disagreement;
                    }                   
                    
                    return primaryWeight;
                    
                } else if (comparison <= matchVar.adjCurv[(jIndex << 2) + 1]) {
                
                    // If the comparison value is below the min adjustment parameter,
                    // use it a different linear adjustment.                 
                    primaryWeight = (agreement - delAgr * (1 - comparison) 
                        * matchVar.adjCurv[(jIndex << 2) + 3]);
                                                                     
                    // If the calculated weight is lower than the minimum authorized
                    // limit, replace it with the minimum value.
                    if (primaryWeight < disagreement) {
                        primaryWeight = disagreement;
                    }
                    
                    return primaryWeight;
                }
                
            } else if (method.charAt(PRIMARY) == FREQUENCY) {
            // This option make use of the frequency data analysis
                
                // Get the table's index
                table = Integer.parseInt(String.valueOf(method.charAt(SECONDARY)));
                
                // Decide which method to use to calculate the weights.
                // If the 4th char is 'e', call the method getFrequency() to get the
                // weight from the frequency table
                if (method.charAt(3) == 'e') {                   
                    adjustment = FrequencyWeight.getFrequency(recA, recB, fieldLength, table, matchVar);
                    
                    return adjustment;
                    
                } else if (method.charAt(3) == 'n') {
                    // If the 4th char is 'n', use directly the weight from first
                    // index of the table (NEED MORE REVIEW).              
                    index = matchVar.tableSize[table] + 1;
                    
                    return matchVar.wgtTable[table][index];
                    
                } else {
                    // Use the generic Winkler's string comparator
                    
                    if (method.charAt(SECONDARY) == 'a') {
                        comparison = StringComparator.genStrComparator(recA, recB, 2, matchVar);
                        
                    } else {
                        comparison = StringComparator.genStrComparator(recA, recB, 1, matchVar);
                    }
                }
                
                index = matchVar.tableSize[table] + 1;
                
                
                // If the comparator value is less than 0.6, don't go further.
                if (comparison < 0.6) {
                    return matchVar.wgtTable[table][index];
                    
                } else {
                // Otherwise use it in parallel with the adjustment value from
                // the frequency function and the rpovided curve-adjustment
                // parameters
                    
                    // Calculate the weight from the frequency table
                    adjustment = FrequencyWeight.getFrequency(recA, recB, fieldLength, table,
                    matchVar);
                    
                    // Compare the returned value with the curve-adjustment parameters.
                    // If the comparison value is higher than the max adjustment value,
                    // return total value of adjustment.
                    if (comparison > matchVar.adjCurv[jIndex << 2]) {
                        return adjustment;
                        
                    } else if ((comparison <= matchVar.adjCurv[jIndex << 2])
                        && (comparison > matchVar.adjCurv[(jIndex << 2) + 1])) {
                    // If the comparison value is in-between the curve-adjustment
                    // parameters, use both the adjustment and comparison values in
                    // a linear formula for the weight.
                        
                        primaryWeight = (adjustment - (adjustment - disagreement) 
                                       * (1 - comparison) * matchVar.adjCurv[(jIndex << 2) + 2]);
                        
                        // If the calculated weight is lower than the minimum authorized
                        // limit, replace it with the minimum value.
                        if (primaryWeight < disagreement) {
                            primaryWeight = disagreement;
                        }
                        return primaryWeight;
                        
                    } else if (comparison <= matchVar.adjCurv[(jIndex << 2) + 1]) {
                        // If the comparison value is below the min adjustment parameter,
                        // use it a different linear adjustment formula.
                        
                        primaryWeight = (adjustment - (adjustment - disagreement) 
                                      * (1 - comparison) * matchVar.adjCurv[(jIndex << 2) + 3]);
                        
                        // If the calculated weight is lower than the minimum
                        // authorized limit, replace it with the minimum value.
                        if (primaryWeight < disagreement) {
                            primaryWeight = disagreement;
                        }
                        return primaryWeight;
                    }
                }
                
            } else if (method.charAt(PRIMARY) == PRORATED) {
            // This option is designed to be used with Numerics to handle 
            // percentage differences
            
                Number numA;
                Number numB;
                double relDiff;
                long longRecA = 0;
                long longRecB = 0;
                double doubleRecA = 0;
                double doubleRecB = 0;
                double range = 0;
                String sA;
                String sB;

                // Handle the number format
                NumberFormat nf = NumberFormat.getInstance(Locale.US);
        
                // Before parsing, make sure that both records are numbers
                if (EmeUtil.isNumber(recA) && EmeUtil.isNumber(recB)) {

                    numA = nf.parse(recA);
                    numB = nf.parse(recB);
                    
                    if (numA instanceof Long) {
                        doubleRecA = (numA.longValue());
 
                    } else if (numA instanceof Double) {
                        doubleRecA = numA.doubleValue();
                    }
                    
                    if (numB instanceof Long) {
                        doubleRecB = (numB.longValue());
                        
                    } else if (numB instanceof Double) {
                        doubleRecB = numB.doubleValue();
                    }
                }
              
                // Define the minimum value and the difference between the year-fields.
                if (doubleRecA > doubleRecB) {
                    relDiff = doubleRecA - doubleRecB;                   
                } else {
                    relDiff = doubleRecB - doubleRecA;
                }               
                     

                // NEED REVIEW!
                if (relDiff < matchVar.tolerance1[jIndex]) {
                    return agreement;
                    
                } else if (relDiff >= (matchVar.rangeP[jIndex] - matchVar.tolerance2[jIndex])) {

                    return disagreement;
                    
                } else {
                    
                    range = matchVar.rangeP[jIndex]  - matchVar.tolerance2[jIndex]
                          - matchVar.tolerance1[jIndex];
                    
                    relDiff -= matchVar.tolerance1[jIndex];
                           
                    secondaryWeight = agreement - delAgr * relDiff / range;
                    
                    // If the calculated weight is lower than the minimum
                    // authorized limit, replace it with the minimum value.
                    if (secondaryWeight < disagreement) {
                        secondaryWeight = disagreement;
                    }

                    return secondaryWeight;
                }
                
            } else {
                throw new SbmeMatchingException("The comparator is unknown.\n"
                    + "You are using an invalid match comparator");
            }
        }
        return result;
    }
}

