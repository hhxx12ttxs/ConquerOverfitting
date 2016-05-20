package jemm;

import java.util.Vector;

/**
 * this class determines if an incoming event is rare or not
 * @author charlie
 * @version 0.1 11/24/2008
 */
public class EMMRare {
    
    /**
     * theState       contains the new incoming event     
     * Cluster        local EMMCluster object
     * thresholdOF    threshold used for ocurrence frequency
     * thresholdNTP   threshold normalized transition probability
     */
    private EMMNode theState;   
    private EMMCluster cluster;
    private double thresholdOF;
    private double thresholdNTP;
    
    /**
     * parametrized constructor
     * @param simMeasure        defines similarity use
     * @param threshold         EMM threshold
     * @param thOF              threshold used for ocurrence frequency          
     * @param thNTP             threshold normalized transition probability
     */
    EMMRare(String simMeasure, double threshold, double thOF, double thNTP){
        thresholdOF = thOF;
        thresholdNTP = thNTP;
        cluster = new EMMCluster(simMeasure, threshold);
    }
    
    /**
     * function that searches the EMM model and determines if incoming events are
     * rare based on a set of rules
     * @param currentGraph          EMM model at this time
     * @param newEvent              new incoming event
     * @throws java.lang.Exception  
     */
    public void detectEvent(EMM currentGraph, Vector<Double> newEvent)throws Exception {
        theState = null;
        boolean isRare = true;
        currentGraph.updateOverallTimesInState();
        theState = cluster.determineStateSimilarity(currentGraph, newEvent);
        
        /**
         * if the new event did not match to one of the existing EMM cluster 
         * create a new cluster and add it to EMM model. isRare is a boolean 
         * variable used for indicating if the rare event happened by creating a 
         * new state or by simply applying the set of rules
         */
        if(theState == null) {
            theState = createNewState(currentGraph, newEvent);
            currentGraph.setStatePool(theState);
            isRare = false;
        }
        
         /**
         * checkLink()      is a function that searches for a link in  the EMM model and
         * if the transition from current state to the new state is not found
         * creates a new link and returns the number of times visited in the link.
         * setCurrentState()    is a function that sets the new state to be the 
         * current state
         */
        double CN = currentGraph.checkLink(theState);
        currentGraph.setCurrentState(theState);
        
        /**
         * we access the if statement if "isRare" evaluates to true, which means 
         * that we found a match with one of the EMM cluster and that we need to
         * apply the set of rule in order to determine if the new event is rare
         * or not
         */
        if(isRare){
            double getNum = theState.getNumOfState();
            double getTot = currentGraph.getOverallTimesInState();
            double OF = getNum/getTot;
            double NTP = CN/getTot;
            
            if(OF > thresholdOF || NTP > thresholdNTP){
                theState.setRareEvent(true);
            }else{
                theState.setRareEvent(false);
            }
        }
    }
    
    /**
     * This function is used for a new state and sets the cluster to be rare
     * @param currentGraph the EMM graph at this time
     * @param newEvent the new incoming event
     * @return the new state
     */
    public EMMNode createNewState(EMM currentGraph, Vector<Double> newEvent){
        currentGraph.updateNumOfState();
        EMMNode newState = new EMMNode(currentGraph.getNumOfState(), newEvent);
        newState.updateNumOfState();
        newState.setRareEvent(true);
        return newState;
    }
}

