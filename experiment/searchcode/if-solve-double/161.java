/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package scRisk.optModels.fortification.shortestPath.worstCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import objects.Edge;
import objects.SupplyDemandNode;
import scRisk.networks.FlowNetwork;
import scRisk.optModels.interdiction.ArcInterdictor;
import scRisk.optModels.interdiction.shortestPath.ShortestPathInterdictionMIP;

/**
 *
 * @author hmedal
 */
public class ShortestPathInterdictionFortificationTreeSearch {

    //protected Map<Collection<Edge>,Double> solutions;
    protected double minObjValue;
    protected Collection<Edge> optimalSolution;
    protected int Q;
    protected ShortestPathInterdictionMIP sp;

    public ShortestPathInterdictionFortificationTreeSearch(int Q,ShortestPathInterdictionMIP sp){
        this.sp=sp;
        this.Q=Q;
        this.minObjValue=Double.POSITIVE_INFINITY;
        //solutions=new HashMap<Collection<Edge>,Double>();
    }

    public static Collection union(Collection a,Collection b){
        Collection c=new ArrayList();
        for(Object o:a)
            c.add(o);
        for(Object o:b){
            if(!c.contains(o))
                c.add(o);
        }
        return c;
    }

    public static Collection copy(Collection a){
        Collection c=new ArrayList();
        for(Object o:a)
            c.add(o);
        return c;
    }

    public void solveNodeAndBranch(int level,Collection<Edge> fixed){
        //System.out.println("level= "+level);
        sp.clearFixedEdgesConstraint();
        sp.fixEdgesToBeFortified(fixed);
        //System.out.println("fixed "+fixed);
        sp.solve();
        //System.out.println("interdicted "+sp.getInterdictedEdges());
        if(level==Q){
          double objValue=sp.getObjValue();
          if(objValue<minObjValue){
            minObjValue=objValue;
            optimalSolution=copy(fixed);
          }
          //System.out.println(fixed+"\t"+objValue);
          //solutions.put(fixed,objValue);
        }
        else{
            for(Edge e:sp.getInterdictedEdges()){
                ArrayList<Edge> toFix= new ArrayList<Edge>();
                toFix.add(e);
                Collection<Edge> newFixed=union(fixed,toFix);
                //System.out.println("newFixed"+newFixed);
                solveNodeAndBranch(level+1,newFixed);
            }
        }
    }

    public double getMinObjValue() {
        return minObjValue;
    }

    public Collection<Edge> getOptimalSolution() {
        return optimalSolution;
    }
    
    public static void main(String[] args){
        int r=2;
        int Q=2;
        FlowNetwork fn = new FlowNetwork();
        fn.readFromToListFromFile("//ineg-file/shares/ntscoe_sc_risk/data/fromTo_shortestPathFlow_2010-2-25.txt",1,10,7);
        ArcInterdictor interdictor = new ArcInterdictor();
        interdictor.setCardinalityNumber(r);
        Map<Integer,Map<Integer,Integer>> delays=new HashMap<Integer,Map<Integer,Integer>>();
        for(int i=1;i<=6;i++)
            delays.put(i,new HashMap<Integer,Integer>());
        delays.get(1).put(2, 15);
        delays.get(1).put(3, 45);
        delays.get(1).put(4, 25);
        delays.get(2).put(4, 2);
        delays.get(2).put(5, 60);
        delays.get(3).put(6, 50);
        delays.get(4).put(3, 2);
        delays.get(4).put(7, 100);
        delays.get(5).put(7, 2);
        delays.get(6).put(7, 1);
        for(int i:delays.keySet()){
            for(int j:delays.get(i).keySet()){
                SupplyDemandNode fromNode=fn.getNodesMap().get(i);
                SupplyDemandNode toNode=fn.getNodesMap().get(j);
                interdictor.addDelay(fn.getEdgesMap().get(fromNode).get(toNode),delays.get(i).get(j));
            }
        }
        ShortestPathInterdictionMIP sp = new ShortestPathInterdictionMIP(fn, interdictor,fn.getNodesArray()[0],fn.getNodesArray()[6]);
        sp.createModel();
        sp.setOut(null);
        sp.setWarning(null);
        ShortestPathInterdictionFortificationTreeSearch treeSearch = new ShortestPathInterdictionFortificationTreeSearch(Q, sp);
        treeSearch.solveNodeAndBranch(0, new ArrayList<Edge>());
        System.out.println(treeSearch.getMinObjValue());
        System.out.println(treeSearch.getOptimalSolution());
    }
}

