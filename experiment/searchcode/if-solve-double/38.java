/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package scRisk.optModels.interdiction.facility;

import scRisk.optModels.util.NetworkFlowCplexModel;
import scRisk.optModels.util.SortPair;
import scRisk.optModels.util.SortPairMax;
import scRisk.optModels.*;
import scRisk.optModels.classic.TransportationIP;
import scRisk.optModels.classic.WLP;
import scRisk.optModels.classic.pMedian;
//import cyclic.GeneralizedPMedian;
import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import scRisk.networks.PointsDataset;
import scRisk.networks.SupplyNetworkBipartiteDataset;
import scRisk.networks.SupplyNetworkDataset;

/**
 *
 * @author hmedal
 * from Church, Scaparra, and Middleton (2004)
 */
public class rInterdictionMedianProblem extends NetworkFlowCplexModel{

    private SupplyNetworkBipartiteDataset dataset;
    //private List<Integer> locationsChosen;
    private HashMap<Facility, IloNumVar> interdict;
    private HashMap<Customer,HashMap<Facility,List<Facility>>> T;
    private HashMap<Customer,Map<Integer,Facility>> closest;
    private Facility[] facs;
    private Customer[] custs;
    private int r;

    public rInterdictionMedianProblem(SupplyNetworkBipartiteDataset dataset,int r){
        this.dataset=dataset;
        this.r=r;
        facs = dataset.getFacilitiesArray();
        custs = dataset.getCustomersArray();
        this.closest = getClosestMatrix();
        //this.locationsChosen = new ArrayList<Integer>();
        this.T=getTmap();
    }

    private HashMap<Customer,HashMap<Facility,List<Facility>>> getTmap() {
        HashMap<Customer,HashMap<Facility,List<Facility>>> tMap = new HashMap<Customer,HashMap<Facility,List<Facility>>>();
        for(Customer i:custs){
            tMap.put(i, new HashMap<Facility,List<Facility>>());
            for(Facility j:facs){
                tMap.get(i).put(j, new ArrayList<Facility>());
                for(Facility k:facs){
                    double d1=dataset.getDistance(k, i);
                    double d2=dataset.getDistance(j, i);
                    if(k!=j&&(d1>d2)){
                        tMap.get(i).get(j).add(k);
                    }
                }
                /*System.out.print(i.getName()+"\t"+j.getName()+":\t");
                for(Facility f:tMap.get(i).get(j))
                    System.out.print(f.getName()+"\t");
                System.out.println();*/
            }
        }
        return tMap;
    }

    public IloCplex createModel(){
        try {
            interdict = new HashMap<Facility,IloNumVar>();
            for(Facility f:facs)
                interdict.put(f, cplex.boolVar("s_"+f.getName()));

            for(Facility f:facs){
                if(!flow.containsKey(f))
                    flow.put(f, new HashMap<Customer,IloNumVar>());
                //supply[i]=new IloNumVar[this.numCustomers];
                for(Customer c:custs)
                    flow.get(f).put(c,cplex.numVar(0.0, 1.0,"x_{"+c.getName()+","+f.getName()+"}"));
            }
            //Demand Constraint
            for(Customer c:custs) {
                IloLinearNumExpr demandConstraint = cplex.linearNumExpr();
                for(Facility f:facs) {
                    demandConstraint.addTerm(1.0, flow.get(f).get(c));
                }
                cplex.addEq(1.0, demandConstraint,"Demand satisfied for customer "+c.getName());
            }
            IloLinearNumExpr numInterdicted = cplex.linearNumExpr();
            for(Facility f:facs) {
                numInterdicted.addTerm(1.0, interdict.get(f));
            }
            IloRange expr1=cplex.addEq(r, numInterdicted,"Total number interdicted");
            //System.out.println(expr1);
            //Constraint (10)
            for(Facility j:facs) {//i
                for(Customer i:custs) {
                    IloLinearNumExpr capacityConstraint = cplex.linearNumExpr();
                    for(Facility k:T.get(i).get(j)){
                        capacityConstraint.addTerm(flow.get(k).get(i),1.0);
                    }
                    IloConstraint expr2=cplex.addLe(capacityConstraint, interdict.get(j),i.getName()+"\t"+j.getName());
                    //System.out.println(expr2);
                }   
            }
            //Objective
            IloLinearNumExpr objective = cplex.linearNumExpr();
            for(Facility f:facs) {
                for(Customer c:custs) {
                    objective.addTerm(c.getDemand()*dataset.getDistance(f, c),flow.get(f).get(c));
                }
            }
            //Specify objective direction
            cplex.addMaximize(objective);
        } catch(IloException e) {
                System.err.println("Ilog Error e: ");
                e.printStackTrace();
        }
        return cplex;
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
        return f.getFixedCost()+sum;
    }

    public double getContribution(NetworkEntity entity){
        if(Facility.class.isAssignableFrom(entity.getClass())){
            return getFacilityContribution((Facility)entity);
        }
        else if(IntermediateNode.class.isAssignableFrom(entity.getClass())){
            return ((IntermediateNode)entity).getFlow();
        }
        else if(SupplyNetworkArc.class.isAssignableFrom(entity.getClass())){
            return ((SupplyNetworkArc)entity).getFlow();
        }
        return -1;
    }

    public List<Facility> getInterdictedFacilities(){
        List<Facility> interdicted = new ArrayList<Facility>();
        for(Facility f:interdict.keySet()){
                int openIndicator;
            try {
                openIndicator = (int)Math.round(cplex.getValue(interdict.get(f)));
                if(openIndicator==1)
                    interdicted.add(f);
            } catch (UnknownObjectException ex) {
                Logger.getLogger(WLP.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IloException ex) {
                Logger.getLogger(WLP.class.getName()).log(Level.SEVERE, null, ex);
            }

            }
        return interdicted;
    }

    public List<Integer> getInterdictedFacilityNumbers(){
        List<Integer> interdicted = new ArrayList<Integer>();
        for(Facility f:interdict.keySet()){
                int openIndicator;
            try {
                openIndicator = (int)Math.round(cplex.getValue(interdict.get(f)));
                if(openIndicator==1)
                    interdicted.add(f.getNumber());
            } catch (UnknownObjectException ex) {
                Logger.getLogger(WLP.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IloException ex) {
                Logger.getLogger(WLP.class.getName()).log(Level.SEVERE, null, ex);
            }

            }
        return interdicted;
    }

    public void setFlowOnArcs(){
        super.setFlowOnArcs(dataset.getPaths());
    }

    public void setFlowOnNodes(){
        super.setFlowOnNodes(dataset.getPaths());
    }

    public static SupplyNetworkDataset getFacilitiesSelectedDataset(SupplyNetworkDataset snd){
        WLP cflp = new WLP(snd.getBipartite());
        cflp.createModel();
        cflp.solve();
        for(Facility f:cflp.getUnopenedFacilities())
            snd.removeFacility(f.getNumber());
        cflp.end();
        return snd;
    }

    public double getConsequenceForFacilityFailure(Facility f){
        double sum=0;
        for(Customer c:custs){
            Facility closestFacility = closest.get(c).get(1);
            if(closestFacility==f){
                Facility nextClosestFacility = closest.get(c).get(2);
                double closestDist =dataset.getDistance(closestFacility, c);
                double nextClosestDist =dataset.getDistance(nextClosestFacility, c);
                sum+= c.getDemand()*(nextClosestDist-closestDist);
            }
        }
        return sum;
    }

    public HashMap<Customer,Map<Integer,Facility>> getClosestMatrix(){
        HashMap<Customer,Map<Integer,Facility>> kmatrix = new HashMap<Customer,Map<Integer,Facility>>();
        for(Customer c:custs){
            kmatrix.put(c,getOrder(c));
        }
        //System.out.println(kmatrix);
        return kmatrix;
    }

    public List<SortPairMax> getSortedByConsequence(Facility[] facs){
        List<SortPairMax> pairs = new ArrayList<SortPairMax>();
        for(Facility f:facs)
            pairs.add(new SortPairMax(this.getConsequenceForFacilityFailure(f),f));
        Collections.sort(pairs);
        //System.out.println(pairs);
        return pairs;
    }

    public Map<Integer,Facility> getOrder(Customer c){
        Map<Integer,Facility> order = new HashMap<Integer,Facility>();
        SortPair[] pairs = new SortPair[facs.length];
        int i=0;
        for(Facility f:facs){
            pairs[i++] = new SortPair(dataset.getDistance(f, c),f);
        }
        Arrays.sort(pairs);
        //System.out.println(c.getName()+"\t"+Arrays.toString(pairs));
        for(int k=0;k<pairs.length;k++){
            order.put(k+1,(Facility)pairs[k].getOriginalIndex());
        }
        //System.out.println(order);
        return order;
    }

    public static void Swain1971_dataset(){
        PointsDataset pd = new PointsDataset("//ineg-file/shares/NTSCOE_SC_Risk/data/Swain1971.txt");
        //System.out.println(Arrays.deepToString(pd.getDistances()));
        //System.out.println(Arrays.toString(pd.getDemands()));
        double[] fixedCosts = new double[pd.getDistances().length];
        
        /*for(int p=11;p<=11;p+=2){
            GeneralizedPMedian pMed = new GeneralizedPMedian(pd.getDistances(),pd.getDemands(), fixedCosts, p);
            pMed.createModel();
            pMed.solve();
            List<Integer> medians= pMed.getMedians();
            double objValue = pMed.getObjectiveValue();
            pMed.end();
            SupplyNetworkBipartiteDataset snb = pd.toSupplyNetworkBipartiteDataset(medians);
            rInterdictionMedianProblem rInterdict1 = new rInterdictionMedianProblem(snb,1);
            for(SortPairMax pair:rInterdict1.getSortedByConsequence(snb.getFacilitiesArray())){
                Facility f=(Facility)pair.getOriginalIndex();
                System.out.println(f.getNumber()+";"+f.getName()+";"+pair.getValue());
            }
            for(int r=1;r<=(p-2);r++){
                rInterdictionMedianProblem rInterdict = new rInterdictionMedianProblem(snb,r);
                rInterdict.createModel();
                rInterdict.solve();
                double rObj= rInterdict.getObjValue();
                System.out.println(p+";"+r+";"+objValue+";"+rObj+";"+medians+";"+rInterdict.getInterdictedFacilityNumbers());
                rInterdict.end();
            }
        }*/

    }

    public static void southernUS_dataset(){
        SupplyNetworkDataset gd = new SupplyNetworkDataset();
        gd.readFacInfoFromFile("//ineg-file/shares/ntscoe_sc_risk/data/facInfoPmedian.txt");
        gd.readCustomerInfoFromFile("//ineg-file/shares/ntscoe_sc_risk/data/custInfoPmedian.txt");
        gd.readIntermediateNodeNamesFromFile("//ineg-file/shares/ntscoe_sc_risk/data/nodeNames.txt");
        gd.readFromToListFromFile("//ineg-file/shares/ntscoe_sc_risk/data/fromTo.txt");
        SupplyNetworkBipartiteDataset snb1 = gd.getBipartite();
        int p=7;
        pMedian pMed = new pMedian(snb1,p);
        pMed.createModel();
        pMed.solve();
        //System.out.println(pMed.printFlowSolution());
        List<Integer> medians = pMed.getOpenedFacilitiesNumbers();
        //System.out.println(facs);
        double objValue = pMed.getObjValue();
        SupplyNetworkBipartiteDataset snb = pMedian.getFacilitiesSelectedDatasetFromList(gd, pMed.getOpenedFacilities());
        pMed.end();
        
        rInterdictionMedianProblem r1 = new rInterdictionMedianProblem(snb,1);
        //for(Facility f:snb.getFacilitiesArray())
        //        System.out.println(f.getNumber()+";"+f.getName()+";"+r1.getConsequenceForFacilityFailure(f));
        for(SortPairMax pair:r1.getSortedByConsequence(snb.getFacilitiesArray())){
            Facility f=(Facility)pair.getOriginalIndex();
            System.out.println(f.getNumber()+";"+f.getName()+";"+pair.getValue());
        }
        for(int R=1;R<=(p-2);R++){
            rInterdictionMedianProblem r2 = new rInterdictionMedianProblem(snb,R);
            r2.createModel();
            r2.solve();
            double rObj= r2.getObjValue();
            System.out.println(p+";"+R+";"+objValue+";"+rObj+";"+medians+";"+r2.getInterdictedFacilityNumbers());
            //System.out.println(r.printFlowSolution());
            r2.end();
        }
    }

    public static void main(String[] args){
        //Swain1971_dataset();
        southernUS_dataset();
    }
}

