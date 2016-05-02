/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bio.tit.narise.RSASCore.util.filter;

import bio.tit.narise.RSASCore.controller.api.Args;
import bio.tit.narise.RSASCore.model.RSASResult;
import bio.tit.narise.RSASCore.model.RSASResults;
import java.util.List;

/**
 *
 * @author tn
 */
public class SetFilter {

    // this is an utility class
    private SetFilter(){ throw new UnsupportedOperationException(); }
    
    static RSASResults filtWithFdr(RSASResults rsasResults, Args args){
        List<RSASResult> resultsPos = rsasResults.getResultsPos();
        List<RSASResult> resultsNeg = rsasResults.getResultsNeg();
        int setNumPos = rsasResults.getSetNumPos();
        int setNumNeg = rsasResults.getSetNumNeg();
        double setNumAll = setNumPos + setNumNeg;
        double pValAtThisRank;
        double pValRevAtThisRank;
        
        double fdr = args.getFdr()/100;
        
        for(int i = setNumPos - 1; i > -1; i--) {
            pValAtThisRank = resultsPos.get(i).getpVal();
            
            if( pValAtThisRank > ((double) (i+1)/setNumAll)*fdr ){
                resultsPos.remove(i);
            }
            else {
                break;
            }
        }
    
        for(int i = setNumNeg - 1; i > -1; i--) {
            pValRevAtThisRank = resultsNeg.get(i).getpVal();

            if( pValRevAtThisRank > ((double)(i+1)/setNumAll)*fdr ){
                resultsNeg.remove(i);
            }
            else {
                break;
            }
        }
        
        RSASResults filteredResults = new RSASResults(resultsPos, resultsNeg);
        return filteredResults;
    }
    
    static RSASResults filtWithPVal(RSASResults rsasResults, Args args){
        List<RSASResult> resultsPos = rsasResults.getResultsPos();
        List<RSASResult> resultsNeg = rsasResults.getResultsNeg();
        int setNumPos = rsasResults.getSetNumPos();
        int setNumNeg = rsasResults.getSetNumNeg();
        double setNumAll = setNumPos + setNumNeg;
        double pValAtThisRank;
        double pValRevAtThisRank;
        
        double pVal = args.getSetPVal()/100;
        
        for(int i = setNumPos - 1; i > -1; i--) {
            pValAtThisRank = resultsPos.get(i).getpVal();
            
            if( pValAtThisRank > pVal ){
                resultsPos.remove(i);
            }
            else {
                break;
            }
        }
    
        for(int i = setNumNeg - 1; i > -1; i--) {
            pValRevAtThisRank = resultsNeg.get(i).getpVal();

            if( pValRevAtThisRank > pVal ){
                resultsNeg.remove(i);
            }
            else {
                break;
            }
        }
        
        RSASResults filteredResults = new RSASResults(resultsPos, resultsNeg);
        return filteredResults;
    }
}
