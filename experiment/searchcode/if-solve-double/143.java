/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package datasets.network;

import algorithms.stochasticProgramming.scenarios.CompleteFailureScenarioSetGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import models.networkOpt.DeterministicMaxCostNetFlow;

/**
 *
 * @author hmedal
 */
public class CapacitatedNetworkFlowGraphEdgesList {
    private double[] supply;
    private double[] capacity;
    private double[] probFailure;
    private HashMap<Integer,HashMap<Integer,Double>> edgesList;
    private int numArcs;

    public CapacitatedNetworkFlowGraphEdgesList(double[] supply, double[] capacity, double[] probFailure,HashMap<Integer,HashMap<Integer,Double>> edgesList){
        this.supply=supply;
        this.capacity=capacity;
        this.edgesList=edgesList;
        this.probFailure=probFailure;
        if(edgesList!=null)
            this.numArcs=getNumArcs();
    }

    /**
     * functioning: value = 0
     * failed: value = 1
     * @param nodes
     * @param n
     * @return
     */
    public double getProbabilityAtLeastNofInSetFunctioning(List<Integer> nodes,int n){
        double[] probsOfFailure=new double[nodes.size()];
        for(int i=0;i<nodes.size();i++)
            probsOfFailure[i]=probFailure[nodes.get(i)];
        double totalProb=0;
        //System.out.println("CapNetworkFlowGraph: probs of Failure"+Arrays.toString(probsOfFailure));
        CompleteFailureScenarioSetGenerator gen = new CompleteFailureScenarioSetGenerator(probsOfFailure);
        double[] scenario=null;
        double sum=0;
        while(gen.hasNext()){
            scenario=gen.nextScenario().clone();
            for(double d:scenario)
                sum+=d;
            if((nodes.size()-sum)>=n){
                totalProb+=CompleteFailureScenarioSetGenerator.computeProbability(scenario,probsOfFailure);
            }
            sum=0;
        }
        return totalProb;
    }
    
    protected ArrayList<int[]> getDummyArcs(){
        int source=0;
        int sink=supply.length-1;
        ArrayList<int[]> list = new ArrayList<int[]>();
        list.add(new int[]{source,sink});
        return list;
    }

    public int getNumNodes(){
        return this.supply.length;
    }
    
    public void setLengthOfDummyArcs(double[] lengths){
        ArrayList<int[]> list = getDummyArcs();
        for(int i=0;i<lengths.length;i++){
            int source=list.get(i)[0];
            int sink=list.get(i)[1];
            edgesList.get(source).put(sink, lengths[i]);
        }
    }

    public void setLengthOfDummyArcsAsPenaltyTimesMaxPath(double penaltyMult){
        DeterministicMaxCostNetFlow maxModel =
                new DeterministicMaxCostNetFlow(getSupply(),getCapacity(),getEdgesList());
        maxModel.createModel();
        maxModel.setOut(null);
        maxModel.solve();
        double maxLength=maxModel.getObjValue();
        //System.out.println("max length= "+maxLength);
        ArrayList<int[]> list = getDummyArcs();
        for(int i=0;i<list.size();i++){
            int source=list.get(i)[0];
            int sink=list.get(i)[1];
            edgesList.get(source).put(sink,penaltyMult*maxLength);
        }
    }
    
    public int getNumArcs(){
        int numArcs=0;
        for(int i:edgesList.keySet()){
            for(int j:edgesList.get(i).keySet())
                numArcs++;
        }
        return numArcs;
    }
    
    public double[] getCapacity() {
        return capacity;
    }

    public double[] getMeanCapacity(){
        double[] meanCapacity=new double[capacity.length];
        for(int i=0;i<capacity.length;i++)
            meanCapacity[i]=capacity[i]*(1-probFailure[i]);
        return meanCapacity;
    }
    public HashMap<Integer, HashMap<Integer, Double>> getEdgesList() {
        return edgesList;
    }

    public double[] getProbFailure() {
        return probFailure;
    }

    public double[] getSupply() {
        return supply;
    }


    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("Nodes\n");
        for(int i=0;i<capacity.length;i++){
            sb.append(i+"\t"+supply[i]+"\t"+capacity[i]+"\t"+probFailure[i]);
            sb.append("\n");
        }
        sb.append("Arcs\n");
        for(int i:edgesList.keySet()){
            for(int j:edgesList.get(i).keySet()){
                sb.append(i+"\t"+j+"\t"+edgesList.get(i).get(j)+"\n");
            }
        }
        return sb.toString();
    }

    public static void main(String[] args){
        CapacitatedNetworkFlowGraphEdgesList g = new CapacitatedNetworkFlowGraphEdgesList(null, null, new double[]{0,.1,.2,.3,.4,0}, null);
        ArrayList<Integer> list = new ArrayList<Integer>();
        for(int i=1;i<=4;i++)
            list.add(i);
        int numInSet=list.size();
        int Q=1;
        System.out.println(g.getProbabilityAtLeastNofInSetFunctioning(list,numInSet-Q));
    }
}

