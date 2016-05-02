/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package scRisk.optModels.classic;

import scRisk.optModels.classic.CFLP;
import scRisk.optModels.util.NetworkFlowCplexModel;
import scRisk.optModels.*;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex.UnknownObjectException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import objects.Customer;
import objects.Facility;
import objects.IntermediateNode;
import objects.NetworkEntity;
import objects.SupplyNetworkArc;
import scRisk.networks.SupplyNetworkBipartiteDataset;
import scRisk.networks.SupplyNetworkDataset;
import scRisk.optModels.util.*;

/**
 *
 * @author hmedal
 */
public class TransportationIP extends NetworkFlowCplexModel{

    private double penalty;
    private SupplyNetworkBipartiteDataset dataset;
    private Facility dummy;
    //private List<Integer> locationsChosen;
    //private HashMap<Facility, IloNumVar> open;
    //private HashMap<Facility, Map<Customer, IloNumVar>> supply;

    public TransportationIP(SupplyNetworkBipartiteDataset dataset,double penalty){
        super();
        this.dataset=dataset;
        this.penalty=penalty;
    }

    public void createModel(){
        try {
            Facility[] facs = dataset.getFacilitiesArray();
            Customer[] custs = dataset.getCustomersArray();
            //open = new HashMap<Facility,IloNumVar>();
            //for(Facility f:facs)
            //    open.put(f, cplex.boolVar("x_"+f.getName()));
            //supply = new HashMap<Facility,Map<Customer,IloNumVar>>();
            
            for(Facility f:facs){
                if(!flow.containsKey(f))
                    flow.put(f, new HashMap<Customer,IloNumVar>());
                //supply[i]=new IloNumVar[this.numCustomers];
                for(Customer c:custs)
                    flow.get(f).put(c,cplex.numVar(0.0, 1.0,"y_{"+f.getName()+","+c.getName()+"}"));
            }
            dummy = new Facility(-1, "Dummy",Integer.MAX_VALUE,0.0);
            flow.put(dummy, new HashMap<Customer,IloNumVar>());
            for(Customer c:custs){
                //System.out.println("put "+c);
                flow.get(dummy).put(c,cplex.numVar(0.0, 1.0,"y_{"+dummy.getName()+","+c.getName()+"}"));
            }
            //Demand Constraint
            for(Customer c:custs) {
                IloLinearNumExpr demandConstraint = cplex.linearNumExpr();
                for(Facility f:flow.keySet()) {
                    demandConstraint.addTerm(1.0, flow.get(f).get(c));
                }
                cplex.addEq(1.0, demandConstraint,"Demand satisfied for customer "+c.getName());
            }
            //Capacity Constraint
            for(Facility f:flow.keySet()) {//i
                IloLinearNumExpr capacityConstraint = cplex.linearNumExpr();
                for(Customer c:custs) {//j
                    capacityConstraint.addTerm((double)c.getDemand(), flow.get(f).get(c));
                }
                cplex.addLe(capacityConstraint,f.getProductionCapacity(),"Capacity not exceeded at "+f.getName());

            }
            //Objective
            IloLinearNumExpr objective = cplex.linearNumExpr();
            for(Facility f:facs) {
                for(Customer c:custs) {
                    objective.addTerm(dataset.getDistance(f, c)*c.getDemand(),flow.get(f).get(c));
                }
            }
            for(Customer c:custs){
                double totalPenalty = penalty*c.getDemand();
                objective.addTerm(totalPenalty,flow.get(dummy).get(c));
            }
            //Specify objective direction
            cplex.addMinimize(objective);
        } catch(IloException e) {
                System.err.println("Ilog Error e: ");
                e.printStackTrace();
        }
        //return cplex;
    }

    public String toString(){
        return cplex.toString();
    }

    public double getDistanceObjective(){
        double sum = 0;
        for(Facility f:dataset.getFacilitiesArray()) {
            for(Customer c:dataset.getCustomersArray()) {
                try {
                    sum += dataset.getDistance(f, c) * c.getDemand() * cplex.getValue(flow.get(f).get(c));
                } catch (UnknownObjectException ex) {
                    Logger.getLogger(TransportationIP.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IloException ex) {
                    Logger.getLogger(TransportationIP.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return sum;
    }

    public double getCostObjective(){
        double sum = 0;
        for(Facility f:dataset.getFacilitiesArray()) {
            for(Customer c:dataset.getCustomersArray()) {
                try {
                    sum += dataset.getDistance(f, c) * c.getDemand() * cplex.getValue(flow.get(f).get(c));
                } catch (UnknownObjectException ex) {
                    Logger.getLogger(TransportationIP.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IloException ex) {
                    Logger.getLogger(TransportationIP.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return sum;
    }

    public double getTimeObjective(){
        double sum = 0;
        for(Facility f:dataset.getFacilitiesArray()) {
            for(Customer c:dataset.getCustomersArray()) {
                try {
                    sum += dataset.getDistance(f, c) * c.getDemand() * cplex.getValue(flow.get(f).get(c));
                } catch (UnknownObjectException ex) {
                    Logger.getLogger(TransportationIP.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IloException ex) {
                    Logger.getLogger(TransportationIP.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return sum;
    }

    /*public HashMap<Facility, Map<Customer, Double>> getSolution(){
        HashMap<Facility,Map<Customer,Double>> solution = new HashMap<Facility,Map<Customer,Double>>();
        for(Facility f:supply.keySet()){
            solution.put(f, new HashMap<Customer,Double>());
            for(Customer c:supply.get(f).keySet()){
                try {
                    solution.get(f).put(c, cplex.getValue(supply.get(f).get(c)));
                } catch (UnknownObjectException ex) {
                    Logger.getLogger(TransportationIP.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IloException ex) {
                    Logger.getLogger(TransportationIP.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return solution;
    }*/

    public String printFlowSolutionWithPath(){
        DecimalFormat df = new DecimalFormat("#######.###");
        StringBuilder sb = new StringBuilder();
        sb.append("Flow solution with path\n");
        sb.append("fromNum;fromName;toNum;toName;flow;optimalPath\n");
        HashMap<Facility,Map<Customer,Double>> solution = getFlowSolution();
        for(Facility f:solution.keySet()){
            for(Customer c:solution.get(f).keySet()){
                sb.append(f.getNumber()+";"+f.getName()+";"+c.getNumber()+";"+c.getName()+";"+df.format(solution.get(f).get(c)));
                if(f.getNumber()!=-1)
                    sb.append(";"+dataset.printPath(f, c)+"\n");
                else
                    sb.append("\n");
            }
        }
        return sb.toString();
    }

    public List<SupplyNetworkArc> getArcsUsed(Facility f){
        List<SupplyNetworkArc> arcs = new ArrayList<SupplyNetworkArc>();
        HashMap<Facility,Map<Customer,Double>> solution = getFlowSolution();
        for(Customer c:solution.get(f).keySet()){
            if(solution.get(f).get(c)>0){
                for(SupplyNetworkArc arc:dataset.getPath(f, c))
                    if(!arcs.contains(arc))
                        arcs.add(arc);
            }
        }
        return arcs;
    }

    public double getProportionUnmet(Customer c){
        double value =0;
        try {
            //Customer cust = dataset.getCustomersArray()[customerNumber];
            //System.out.println("proportion unmet "+(cplex==null)+"\t"+(flow==null)+"\t"+(dummy==null));
            //System.out.println("proportion unmet "+(flow.get(dummy)==null));
            value = cplex.getValue(flow.get(dummy).get(c));
            /*if(value>0){
                System.out.println("proportion unmet for "+c.getName()+"\t"+value);
                for(Facility f:flow.keySet())
                    System.out.println(f.getName()+"\t"+cplex.getValue(flow.get(f).get(c)));
            }*/
        } catch (UnknownObjectException ex) {
            Logger.getLogger(TransportationIP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IloException ex) {
            Logger.getLogger(TransportationIP.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println(value);
        return value;
    }

    public double getFacilityContribution(Facility f){
        double sum=0;
        for(Customer c:dataset.getCustomersArray()){
            try {
                sum += this.dataset.getDistance(f, c) * cplex.getValue(this.flow.get(f).get(c)) * c.getDemand();
            } catch (UnknownObjectException ex) {
                Logger.getLogger(TransportationIP.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IloException ex) {
                Logger.getLogger(TransportationIP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return sum;
    }

    public double getContribution(NetworkEntity entity){
        /*if(dataset.getFacilitiesSet().contains(entity)){
            System.out.println("facility");
            return getFacilityContribution((Facility)entity);
        }*/
        if(IntermediateNode.class.isAssignableFrom(entity.getClass())){
            //System.out.println("node");
            return ((IntermediateNode)entity).getFlow();
        }
        else if(SupplyNetworkArc.class.isAssignableFrom(entity.getClass())){
            return ((SupplyNetworkArc)entity).getFlow();
        }
        return -1;
    }

    public double getMaxTransportationCost(){
        double max=Double.MIN_VALUE;
        for(Facility f:dataset.getFacilitiesArray()){
            for(Customer c:dataset.getCustomersArray()){
                if(dataset.getDistance(f, c)>max)
                    max=dataset.getDistance(f, c);
            }
        }
        return max;
    }

    public double getDemandUnmet(Customer c){
        return getProportionUnmet(c)*c.getDemand();
    }
    
    public double getTotalUnMetDemand(){
        double sum=0;
        for(Customer c:dataset.getCustomersArray()){
            sum+=getDemandUnmet(c);
        }
        return sum;
    }

    public void setFlowOnArcs(){
        super.setFlowOnArcs(dataset.getPaths());
    }

    public void setFlowOnNodes(){
        super.setFlowOnNodes(dataset.getPaths());
    }

    public static void main(String[] args){
        SupplyNetworkDataset gd = new SupplyNetworkDataset();
        gd.readFacInfoFromFile("//130.184.181.4/ntscoe_sc_risk/data/facInfo_9-8.txt");
        gd.readCustomerInfoFromFile("//130.184.181.4/ntscoe_sc_risk/data/custInfo_9-8.txt");
        gd.readIntermediateNodeNamesFromFile("//130.184.181.4/ntscoe_sc_risk/data/nodeNames_9-8.txt");
        gd.readFromToListFromFile("//130.184.181.4/ntscoe_sc_risk/data/fromTo_9-8.txt");
        //System.out.println(Arrays.deepToString(gd.getEdgesArray()));
        //Node[] nodes =gd.getNodesArray();
        //System.out.println(Arrays.deepToString(nodes));
        //System.out.println(gd);
        SupplyNetworkBipartiteDataset facilitiesSelectedBipartite = CFLP.getFacilitiesSelectedDataset(gd).getBipartite();
        //SupplyNetworkBipartiteDataset snb = gd.getBipartite();
        //System.out.println(snb);
        //gd.removeNode(1);
        //SupplyNetworkBipartiteDataset snbRemoved = gd.getBipartite();
        //System.out.println(snbRemoved);
        TransportationIP trans = new TransportationIP(facilitiesSelectedBipartite,10);
        trans.createModel();
        trans.solve();
        trans.setFlowOnArcs();
        System.out.println(trans.printFlowSolution());
        System.out.println(facilitiesSelectedBipartite.flowsToString());
        gd.removeNode(2);
        SupplyNetworkBipartiteDataset snbRemoved = gd.getBipartite();
        TransportationIP trans2 = new TransportationIP(snbRemoved,10);
        trans2.createModel();
        trans2.solve();
        trans2.setFlowOnArcs();
        System.out.println(snbRemoved.flowsToString());
        trans.end();
    }

    public double getPenalty() {
        return penalty;
    }
}

