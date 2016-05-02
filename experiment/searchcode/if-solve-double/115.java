/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package scRisk.optModels.hardening.shortestPath.expectedValue;

import models.CplexModel;
import scRisk.optModels.objects.*;
import scRisk.optModels.*;
import ilog.concert.IloConstraint;
import java.util.logging.Level;
import java.util.logging.Logger;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.cplex.IloCplex.UnknownObjectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import objects.Edge;
import objects.Node;
import network.GraphDataset;

/**
 *
 * @author hmedal
 */
public class ShortestPathFortificationSAA extends CplexModel{

    protected Map<Node,Map<Node,Map<EdgeScenario,IloNumVar>>> x;
    protected Map<Edge,IloNumVar> fortification;
    protected int numCustomers;
    protected GraphDataset dataset;
    protected int q;
    protected Collection<EdgeScenario> scenarios;
    IloObjective objective;
    Collection<IloConstraint> flowConstraints;
    Collection<IloConstraint> capacityConstraints;
    protected Edge dummyArc;

    public ShortestPathFortificationSAA(GraphDataset dataset,Collection<EdgeScenario> scenarios,int q,int dummyCostMult){
        this.dataset=dataset;
        this.q=q;
        this.scenarios=scenarios;
        fortification = new HashMap<Edge,IloNumVar>();
        flowConstraints=new ArrayList<IloConstraint>();
        capacityConstraints=new ArrayList<IloConstraint>();
        dummyArc=new Edge(dataset.getOrigin(), dataset.getDestination(),dummyCostMult*getMaxLength(dataset));
    }

    public int getMaxLength(GraphDataset dataset){
        int max=0;
        for(Edge e:dataset.getEdgesArray()){
            if(e.getWeight()>max)
                max=e.getWeight();
        }
        return max;
    }

    public void createModel(){
        try {
            //System.out.println(Arrays.toString(dataset.getEdgesArray()));
            x = new HashMap<Node,Map<Node,Map<EdgeScenario,IloNumVar>>>();
            for(Edge e:dataset.getEdgesArray()) {
                if(!x.keySet().contains(e.getFromNode())){
                    x.put(e.getFromNode(), new HashMap<Node,Map<EdgeScenario,IloNumVar>>());
                }
                //for (Node j:dataset.getForwardStar(e)) {
                    x.get(e.getFromNode()).put(e.getToNode(), new HashMap<EdgeScenario,IloNumVar>());
                    //for(EdgeScenario s:scenarios){
                    //    if(i!=j)
                    //        x.get(i).get(j).put(s,cplex.numVar(0,Double.POSITIVE_INFINITY,"x_{"+(i.getNumber())+","+(j.getNumber())+","+(s.getNumber())+"}"));
                   // }
                //}
            }
            //x.put(dummyArc.getFromNode(), new HashMap<Node,Map<EdgeScenario,IloNumVar>>());
            x.get(dummyArc.getFromNode()).put(dummyArc.getToNode(), new HashMap<EdgeScenario,IloNumVar>());
            //System.out.println(x);
            for(Edge e:dataset.getEdgesArray()){
                fortification.put(e,cplex.boolVar("z_{"+e.getFromIndex()+","+e.getToIndex()+"}"));
            }
            resetScenarios(scenarios);
            //cardinality constraint
            IloLinearNumExpr cardSum = cplex.linearNumExpr();
            for(Edge e:dataset.getEdgesArray())
                cardSum.addTerm(1.0,fortification.get(e));
            cplex.addEq(cardSum,q,"cardinality");
            
            
        } catch (IloException ex) {
            //Logger.getLogger(WLP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void resetScenarios(Collection<EdgeScenario> scenarios){
        this.scenarios=scenarios;
        try {
            cplex.remove(objective);
            for(IloConstraint constr:capacityConstraints)
                cplex.remove(constr);
            for(IloConstraint constr:flowConstraints)
                cplex.remove(constr);
            for(Edge e:dataset.getEdgesArray()) {
                try{
                    x.get(e.getFromNode()).get(e.getToNode()).clear();
                }
                catch(NullPointerException ex){
                    System.err.println(e.getFromIndex()+"\t"+e.getToIndex()+"\n"+x.get(e.getFromNode()));
                }
                for(EdgeScenario s:scenarios){
                    //System.out.println(x.get(e.getFromNode()));
                    x.get(e.getFromNode()).get(e.getToNode()).put(s,cplex.numVar(0,
                            Double.POSITIVE_INFINITY,"x_{"+e.getFromIndex()+","+e.getToIndex()+","+s.getNumber()+"}"));
                }
            }
            for(EdgeScenario s:scenarios)
                x.get(dummyArc.getFromNode()).get(dummyArc.getToNode()).put(s,cplex.numVar(0,Double.POSITIVE_INFINITY,"x_dummyArc,"+(s.getNumber())+"}"));
            for(EdgeScenario s:scenarios){
                for (Node i:dataset.getNodesArray()) {
                    IloLinearNumExpr flowIn = cplex.linearNumExpr();
                    IloLinearNumExpr flowOut = cplex.linearNumExpr();
                    int diff=0;
                    //System.out.println(dataset.getDestination());
                    if(dataset.getOrigin()==i){
                        //System.out.println(i);
                        for (Edge e:dataset.getForwardStar(i)) {
                            flowOut.addTerm(1.0,x.get(e.getFromNode()).get(e.getToNode()).get(s));
                        }
                        diff=1;
                    }
                    else if(dataset.getDestination()==i){
                        //System.out.println(i);
                        for (Edge e:dataset.getBackwardStar(i)) {
                            flowIn.addTerm(1.0,x.get(e.getFromNode()).get(e.getToNode()).get(s));
                        }
                        diff=-1;
                    }
                    else{
                        //System.out.println(i);
                        for (Edge e:dataset.getForwardStar(i)) {
                            flowOut.addTerm(1.0,x.get(e.getFromNode()).get(e.getToNode()).get(s));
                        }
                        for (Edge e:dataset.getBackwardStar(i)) {
                            flowIn.addTerm(1.0,x.get(e.getFromNode()).get(e.getToNode()).get(s));
                        }
                    }
                    flowConstraints.add(cplex.addEq(flowOut,cplex.sum(flowIn,diff), "flow balance at node "+i+" in scenario "+s.getNumber()));
                }
            }
            //capacity
            for(EdgeScenario s:scenarios){
                for(Edge e:s.getEdges()){
                    capacityConstraints.add(cplex.addLe(x.get(e.getFromNode()).get(e.getToNode()).get(s), cplex.sum(s.getCapacity(e),
                            fortification.get(e)),"scenario "+s.getNumber()));
                }
            }
            
            IloLinearNumExpr sum = cplex.linearNumExpr();
            for(EdgeScenario s:scenarios){
                for(Edge e:dataset.getEdgesArray()){
                    sum.addTerm(e.getWeight(),x.get(e.getFromNode()).get(e.getToNode()).get(s));
                    //if(s.getNumber()==851)
                        //System.out.println(s.getProbability()*e.getWeight()+";"+s.getNumber()+";"+e.getFromIndex()+";"+e.getToIndex());
                }
                sum.addTerm(dummyArc.getWeight(),x.get(dummyArc.getFromNode()).get(dummyArc.getToNode()).get(s));
            }
            objective=cplex.addMinimize(cplex.prod(1.0/scenarios.size(),sum));
        } catch (IloException ex) {
            Logger.getLogger(ShortestPathFortificationSAA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<int[]> getFortified(){
        ArrayList<int[]> fortified=new ArrayList<int[]>();
        for(Edge e:dataset.getEdgesArray()){
            try {
                double value = cplex.getValue(fortification.get(e));
                if (value == 1.0) {
                    fortified.add(new int[]{e.getFromIndex(), e.getToIndex()});
                }
            } catch (UnknownObjectException ex) {
                Logger.getLogger(ShortestPathFortificationSAA.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IloException ex) {
                Logger.getLogger(ShortestPathFortificationSAA.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return fortified;
    }

    public ArrayList<Edge> getFortifiedEdges(){
        ArrayList<Edge> fortified=new ArrayList<Edge>();
        for(Edge e:dataset.getEdgesArray()){
            try {
                double value = cplex.getValue(fortification.get(e));
                if (value == 1.0) {
                    fortified.add(e);
                }
            } catch (UnknownObjectException ex) {
                Logger.getLogger(ShortestPathFortificationSAA.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IloException ex) {
                Logger.getLogger(ShortestPathFortificationSAA.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return fortified;
    }

    public ArrayList<Integer> getPath(EdgeScenario s){
        //System.out.println("scenario "+s.getNumber());
        ArrayList<Integer> path=new ArrayList<Integer>();
        Node fromNode=dataset.getOrigin();
        while(fromNode!=dataset.getDestination()){
            for(Node toNode:dataset.getEdgesMap().get(fromNode).keySet()){
                try {
                    int value = (int)Math.round(cplex.getValue(x.get(fromNode).get(toNode).get(s)));
                    if (value == 1.0) {
                        //System.out.println(fromNode.getNumber()+"\t"+toNode.getNumber());
                        path.add(toNode.getNumber());
                        fromNode=toNode;
                        break;
                    }
                } catch (UnknownObjectException ex) {
                    Logger.getLogger(ShortestPathFortificationSAA.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IloException ex) {
                    Logger.getLogger(ShortestPathFortificationSAA.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return path;
    }

    public Map<Integer,ArrayList<Integer>> getFlowVariables(){
        Map<Integer,ArrayList<Integer>> paths=new HashMap<Integer,ArrayList<Integer>>();
        for(EdgeScenario s:scenarios){
            paths.put((int)s.getNumber(), getPath(s));
        }
        return paths;
    }

    public static void main(String[] args){
        /*GraphDataset gd = new GraphDataset();
        gd.readFromToListFromFile("//ineg-file/shares/ntscoe_sc_risk/data/fromTo_shortestPath_2010-2-18.txt");

        //System.out.println(Arrays.deepToString(gd.getEdges()));
        Node[] nodes =gd.getNodesArray();
        gd.setOrigin(nodes[0]);
        gd.setDestination(nodes[6]);
        //System.out.println(gd.getDestination());
        int Q=4;
        double q=0.1;
        int N=10;
        int Nprime=10;
        int M=10;
        EdgeScenarioGenerator gen = new EdgeScenarioGenerator(gd, 0.1);
        //Collection<EdgeScenario> scenarios = EdgeScenario.getAllFailureScenarios(gd, 0.1);
        ShortestPathFortificationSAA sp= new ShortestPathFortificationSAA(gd,gen.getRandomSet(gd, N),Q,10);
        sp.createModel();
        sp.setOut(null);
        //estimate lower bound
        double bestLB=Double.POSITIVE_INFINITY;
        Statistic lbStat = new Statistic();
        //Statistic ubEstimate = new Statistic();
        //Statistic gapEstimate = new Statistic();
        List<Edge> bestFortification=new ArrayList<Edge>();
        for(int i=1;i<=M;i++){
            sp.resetScenarios(gen.getRandomSet(gd, N));
            
            //System.out.println(sp);
            sp.solve();
            //optimalFortifications=sp.getFortifiedEdges();
            double lb=sp.getObjValue();
            lbStat.collect(lb);
            if(lb<bestLB){
                bestLB=lb;
                bestFortification=sp.getFortifiedEdges();
            }
            //gapEstimate.collect(gap);
            //optimalFortifications.add(sp.get);
        }
        System.out.println(lbStat.getAverage());
        ShortestPathFortificationUBProblem ub= new ShortestPathFortificationUBProblem(gd,gen.getRandomSet(gd, 1),Q,10);
        ub.createModel();
        //ub.setOut(null);
        ub.setFortified(bestFortification);
        ub.resetScenarios(gen.getRandomSet(gd, Nprime));
        System.out.println(ub);
        ub.solve();
        double upperBound=ub.getObjValue();
        System.out.println(upperBound);
        sp.end();
        ub.end();*/
    }
}

