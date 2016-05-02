/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package scRisk.optModels.fort.flowCost.expValue;

import algorithms.stochasticProgramming.scenarios.CompleteDisruptiveEventSet;
import algorithms.stochasticProgramming.scenarios.CompleteFailureScenarioSetGenerator;
import models.networkOpt.DeterministicMinCostNetFlow;
import algorithms.stochasticProgramming.scenarios.MultRVFailureScenarioGenerator;
import datasets.network.CapacitatedNetworkFlowGraphEdgesList;
import datasets.network.RandomNetworkGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import jsl.utilities.random.distributions.DUniform;
import jsl.utilities.random.distributions.Uniform;
import scRisk.optModels.fort.flowCost.expValue.FortMinCostNetFlowDEGeneratorDisruptiveEvent;

/**
 *
 * @author hmedal
 */
public class MinCostNetFlowFortificationAfterDisruption extends DeterministicMinCostNetFlowWithFortification{

    private ArrayList<Integer> optimalSolution;
    private double optimalObjective;
    private double probabilityOfOptimalSolution;
    private double[] probsFailure;
    
    public MinCostNetFlowFortificationAfterDisruption(int Q,CapacitatedNetworkFlowGraphEdgesList g) {
        super(Q,g.getSupply(),g.getCapacity(),g.getCapacity(),g.getEdgesList(),
                NetFlowFortificationUtil.getVulnerable(g.getProbFailure()));
        this.probsFailure=g.getProbFailure();
        solveDeterministicProblem(g);
    }

    private void solveDeterministicProblem(CapacitatedNetworkFlowGraphEdgesList g) {
        DeterministicMinCostNetFlow model = new DeterministicMinCostNetFlow(g.getSupply(),g.getCapacity(),g.getEdgesList());
        model.createModel();
        model.setOut(null);
        model.solve();
        //model.printXSolutionTable();
        optimalObjective=model.getObjValue();
        ArrayList<Integer> unReliableNodesUsed=new ArrayList<Integer>();
        for(int i:model.getNodesUsed()){
            if(vulnerable[i]!=0)
                unReliableNodesUsed.add(i);
        }
        optimalSolution=unReliableNodesUsed;
        probabilityOfOptimalSolution=g.getProbabilityAtLeastNofInSetFunctioning(optimalSolution,optimalSolution.size()-Q);
        //System.out.println("deterministic results: "+optimalSolution+"\t"+optimalObjective+"\t"+probabilityOfOptimalSolution);
        model.end();
    }

    public void setRandomParameterRealization(double[] scenario) {
        averageCapacity=NetFlowFortificationUtil.getNewCapacityVector(capacity, scenario);
        
        //failureScenario = scenario;
        //System.out.println("setRandomParam: "+Arrays.toString(scenario));
        //System.out.println(Arrays.toString(averageCapacity));
        replaceCapacityConstraints(capacity);
        //System.out.println(cplex);
    }

    public double getLowerBound(){
        //MinCostNetFlowFortificationAfterDisruption model=
        //        new MinCostNetFlowFortificationAfterDisruption(Q, g);
        CompleteDisruptiveEventSet gen =
                new CompleteDisruptiveEventSet(probsFailure,optimalSolution,optimalSolution.size()-Q);
        double wtdSum=0;
        this.createModel();
        this.setOut(null);
        long startTime=System.currentTimeMillis();
        while(gen.hasNext()){
            double[] scenario = gen.nextScenario();
            //System.out.println(Arrays.toString(scenario));
            this.setRandomParameterRealization(scenario);
            this.solve();
            double prob=CompleteFailureScenarioSetGenerator.computeProbability(scenario,probsFailure);
            wtdSum+=this.getObjValue()*prob;
            //System.out.println(model.toString());
            //System.out.println(Arrays.toString(scenario)+";"+prob+";"+this.getObjValue());
            //System.out.println("reverse model obj: "+this.getObjValue());
        }
        this.end();
        runTime=(.001)*(System.currentTimeMillis()-startTime);
        return wtdSum+probabilityOfOptimalSolution*optimalObjective;
    }
    
    public static void verification(){
        double supply=10;
        RandomNetworkGenerator gen1 = new RandomNetworkGenerator(5,5,supply,new DUniform(1,10),new DUniform(10,13),new Uniform(0.01,0.1),10);
        CapacitatedNetworkFlowGraphEdgesList g = gen1.getGraph();
        int numNodes=g.getSupply().length;
        DeterministicMinCostNetFlow detModel = new DeterministicMinCostNetFlow(g.getSupply(),g.getCapacity(),g.getEdgesList());
        detModel.createModel();
        detModel.setOut(null);
        detModel.solve();
        System.out.println(detModel.getObjValue());
        System.out.println(Arrays.toString(detModel.getSolution()));
        int[] vulnerable=new int[g.getCapacity().length];
        Arrays.fill(vulnerable,1);
        vulnerable[0]=0;
        vulnerable[g.getCapacity().length-1]=0;
        int N=500;
        MultRVFailureScenarioGenerator gen =
                new MultRVFailureScenarioGenerator(g.getProbFailure(),N);
        FortMinCostNetFlowRecourse model = new FortMinCostNetFlowRecourse(g.getSupply(),g.getCapacity(),g.getEdgesList());
        model.createModel();
        model.setOut(null);
        int Q=1;
        MinCostNetFlowFortificationAfterDisruption model2=
                new MinCostNetFlowFortificationAfterDisruption(Q,g);
        model2.createModel();
        model2.setOut(null);
        
        for(int i=0;i<5;i++){
            double[] scenario = gen.nextScenario();
            //System.out.println(Arrays.toString(scenario));
            model.setRandomParameterRealization(scenario);
            model.solve();
            model2.setRandomParameterRealization(scenario);
            model2.solve();
            //System.out.println(model.toString());
            //System.out.println(model2.toString());
            System.out.println("recourse obj: "+model.getObjValue());
            //System.out.println("reverse model obj: "+model2.getObjValue());
        }
        model.end();
        model2.end();
    }

    public static void qualityOfBounds(int Q,int m,int n){
        RandomNetworkGenerator gen1 = new RandomNetworkGenerator(m,n,10,new DUniform(1,10),new DUniform(10,13),new Uniform(0.01,0.1),2);
        CapacitatedNetworkFlowGraphEdgesList g = gen1.getGraph();
        MinCostNetFlowFortificationAfterDisruption after = new MinCostNetFlowFortificationAfterDisruption(Q, g);
        FortMinCostNetFlowDEGeneratorDisruptiveEvent deModel =
                new FortMinCostNetFlowDEGeneratorDisruptiveEvent(Q,g);
        deModel.createModel();
        deModel.setOut(null);
        deModel.solve();
        System.out.println("fort after bound: "+after.getLowerBound());
        System.out.println("de model solution: "+deModel.getObjValue());
    }

    public static void main(String[] args){
        qualityOfBounds(1,3,3);
    }
}

