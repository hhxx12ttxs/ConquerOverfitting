/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package scRisk.optModels.fortification.facility.expectedValue;

import scRisk.optModels.util.SortPair;
import scRisk.optModels.util.*;
import scRisk.optModels.util.SortPairMax;
import scRisk.optModels.*;
import scRisk.optModels.classic.pMedian;
//import cyclic.GeneralizedPMedian;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
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
import scRisk.networks.PointsDataset;
import scRisk.networks.SupplyNetworkBipartiteDataset;
import scRisk.networks.SupplyNetworkDataset;

/**
 *
 * @author hmedal
 */
public class pMedianFortificationProblem extends CplexModel{
    //from Snyder et al. (2006) section 4.2.1
    private double q;
    private Map<Facility,Double> qMap;
    private HashMap<Customer,Map<Integer,Facility>> closest;
    //private double[][] d;
    private int P;
    private int Q;
    //private double[] h;
    //private int numCustomers;
    private HashMap<Facility,IloNumVar> Z;
    private HashMap<Customer,Map<Integer,IloNumVar>> W;
    private Facility[] facs;
    private Customer[] custs;
    private SupplyNetworkBipartiteDataset dataset;

    public pMedianFortificationProblem(SupplyNetworkBipartiteDataset dataset,double q,int Q){
        this.q=q;
        this.dataset=dataset;
        facs=dataset.getFacilitiesArray();
        custs=dataset.getCustomersArray();
        this.P=facs.length;
        this.closest = getClosestMatrix();
        this.Q=Q;
        this.qMap=new HashMap<Facility,Double>();
    }

    public double getExpectedTransportationCost(Customer i,int k){
        double sum =0;
        for(int j=1;j<=k-1;j++){
            Facility f1 = this.closest.get(i).get(j);
            sum += Math.pow(q, j-1)*(1-q)*dataset.getDistance(f1, i);
        }
        Facility f2=this.closest.get(i).get(k);
        return sum + Math.pow(q, k-1)*dataset.getDistance(f2, i);
    }

    public double getExpectedTransportationCostDifferentValuesOfQ(Customer i,int k){
        for(Facility f:facs)
            qMap.put(f, q);
        double sum =0;
        for(int j=1;j<=k-1;j++){
            Facility f1 = this.closest.get(i).get(j);
            double product1 = 1;
            for(int m=0;m<=j-1;m++){
                if(m==0)
                    product1*=1;
                else
                    product1*=qMap.get(closest.get(i).get(m));
            }
            sum += product1*(1-qMap.get(this.closest.get(i).get(j)))*dataset.getDistance(f1, i);
        }
        Facility f2=this.closest.get(i).get(k);
        double product2 = 1;
        for(int m=0;m<=k-1;m++){
            if(m==0)
                product2*=1;
            else
                product2*=qMap.get(closest.get(i).get(m));
        }
        return sum + product2*dataset.getDistance(f2, i);
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

    public List<SortPairMax> getSortedByConsequence(Facility[] facs){
        List<SortPairMax> pairs = new ArrayList<SortPairMax>();
        for(Facility f:facs)
            pairs.add(new SortPairMax(this.getConsequenceForFacilityFailure(f),f));
        Collections.sort(pairs);
        //System.out.println(pairs);
        return pairs;
    }

    public HashMap<Customer,Map<Integer,Facility>> getClosestMatrix(){
        HashMap<Customer,Map<Integer,Facility>> kmatrix = new HashMap<Customer,Map<Integer,Facility>>();
        for(Customer c:custs){
            kmatrix.put(c,getOrder(c));
        }
        //System.out.println(kmatrix);
        return kmatrix;
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

    public void createVariables(){
        W = new HashMap<Customer,Map<Integer,IloNumVar>>();
        for(Customer i:custs){
            W.put(i, new HashMap<Integer,IloNumVar>());
            for(int k=1;k<=P-Q+1;k++){
                try {
                    W.get(i).put(k, cplex.boolVar("W_{"+i.getNumber()+","+k+"}"));
                } catch (IloException ex) {
                    Logger.getLogger(pMedianFortificationProblem.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        Z = new HashMap<Facility,IloNumVar>();
        for(Facility f:facs){
            try {
                Z.put(f, cplex.boolVar("Z_"+f.getNumber()));
            } catch (IloException ex) {
                Logger.getLogger(pMedianFortificationProblem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void createConstraints(){
        try {
            for(Customer i:custs){
                IloLinearNumExpr constr1 = cplex.linearNumExpr();
                for(int k=1;k<=P-Q+1;k++){
                    constr1.addTerm(W.get(i).get(k),1.0);//constraint (28)
                    //System.out.println(k+"\t"+W.get(i).get(k));
                }
                //System.out.println(cplex==null);
                //System.out.println(constr1);
                cplex.addEq(constr1,1.0);
            }
            for(Customer i:custs){
                for(int k=1;k<=P-Q+1;k++){
                    //System.out.println("k="+k);
                    cplex.addLe(W.get(i).get(k),Z.get(this.closest.get(i).get(k)));//constraint (29)
                }
            }
            for(Customer i:custs){
                for(int k=2;k<=P-Q+1;k++){
                    cplex.addLe(cplex.sum(W.get(i).get(k),Z.get(this.closest.get(i).get(k-1))),1);//constraint (30)
                }
            }
            IloLinearNumExpr constr31 = cplex.linearNumExpr();
            for(Facility j:facs){
                constr31.addTerm(Z.get(j),1.0);
            }
            cplex.addEq(constr31, Q);//constraint (31)
        } catch (IloException ex) {
            Logger.getLogger(pMedianFortificationProblem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public IloNumExpr getObjectiveExpr(){
        IloLinearNumExpr expr = null;
        try {
            expr = cplex.linearNumExpr();
            for(Customer i:custs){
                for(int k=1;k<=P-Q+1;k++){
                    double expectedTransCost = getExpectedTransportationCost(i, k);
                    expr.addTerm(i.getDemand()*expectedTransCost,W.get(i).get(k));
                    //System.out.println(i.getName()+"\t"+k+"\t"+expectedTransCost);
                }
            }
        } catch (IloException ex) {
            Logger.getLogger(pMedianFortificationProblem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return expr;
    }

    public void createModel(boolean minimize){
        createVariables();
        createConstraints();
        try {
            if(minimize)
                cplex.addMinimize(getObjectiveExpr());
            else{
                cplex.addMaximize(getObjectiveExpr());
            }
        } catch (IloException ex) {
            Logger.getLogger(pMedianFortificationProblem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Facility> getFortifiedFacilities(){
        List<Facility> fortified = new ArrayList<Facility>();
        for(Facility f:facs){
            try {
                if (Math.round(cplex.getValue(Z.get(f))) == 1) {
                    fortified.add(f);
                }
            } catch (UnknownObjectException ex) {
                Logger.getLogger(pMedianFortificationProblem.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IloException ex) {
                Logger.getLogger(pMedianFortificationProblem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return fortified;
    }

    public Map<String,List<Integer>> getWValues(){
        Map<String,List<Integer>> wMap = new HashMap<String,List<Integer>>();
        for(Customer i:custs){
            wMap.put(i.getName(), new ArrayList<Integer>());
            for(int k=1;k<=P-Q+1;k++){
                try {
                    //System.out.println(i.getName()+"\t"+k+"\t"+cplex.getValue(W.get(i).get(k)));
                    if (Math.round(cplex.getValue(W.get(i).get(k))) == 1) {
                        wMap.get(i.getName()).add(k);
                    }
                } catch (UnknownObjectException ex) {
                    Logger.getLogger(pMedianFortificationProblem.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IloException ex) {
                    Logger.getLogger(pMedianFortificationProblem.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return wMap;
    }

    public List<String> getFortifiedFacilitiesNames(){
        List<String> fortified = new ArrayList<String>();
        for(Facility f:facs){
            try {
                if (Math.round(cplex.getValue(Z.get(f))) == 1) {
                    fortified.add(f.getName());
                }
            } catch (UnknownObjectException ex) {
                Logger.getLogger(pMedianFortificationProblem.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IloException ex) {
                Logger.getLogger(pMedianFortificationProblem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return fortified;
    }

    public List<Integer> getFortifiedFacilitiesNumbers(){
        List<Integer> fortified = new ArrayList<Integer>();
        for(Facility f:facs){
            try {
                if (Math.round(cplex.getValue(Z.get(f))) == 1) {
                    fortified.add(f.getNumber());
                }
            } catch (UnknownObjectException ex) {
                Logger.getLogger(pMedianFortificationProblem.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IloException ex) {
                Logger.getLogger(pMedianFortificationProblem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return fortified;
    }

    public static boolean isSubset(List list1,List list2){
        for(Object o:list1){
            if(!list2.contains(o))
                return false;
        }
        return true;
    }

    public static void Swain1971_dataset(){
        PointsDataset pd = new PointsDataset("//ineg-file/shares/NTSCOE_SC_Risk/data/Swain1971.txt");
        //System.out.println(Arrays.deepToString(pd.getDistances()));
        //System.out.println(Arrays.toString(pd.getDemands()));
        double[] fixedCosts = new double[pd.getDistances().length];
        /*for(double q=0.01;q<=.01;q+=.2){
            for(int p=3;p<=7;p++){
                GeneralizedPMedian pMed = new GeneralizedPMedian(pd.getDistances(),pd.getDemands(), fixedCosts, p);
                pMed.createModel();
                pMed.solve();
                List<Integer> medians= pMed.getMedians();
                double objValue = pMed.getObjectiveValue();
                pMed.end();
                SupplyNetworkBipartiteDataset snb = pd.toSupplyNetworkBipartiteDataset(medians);
                pMedianFortificationProblem fort1 = new pMedianFortificationProblem(snb,q,1);
                for(SortPairMax pair:fort1.getSortedByConsequence(snb.getFacilitiesArray())){
                    Facility f=(Facility)pair.getOriginalIndex();
                    //System.out.println(f.getName()+";"+pair.getValue());
                }
                List<String> fortifiedFacilitiesNames = new ArrayList<String>();
                for(int Q=1;Q<=(p-2);Q++){
                    pMedianFortificationProblem fort = new pMedianFortificationProblem(snb,q,Q);
                    fort.createModel(true);
                    fort.solve();
                    double rObj= fort.getObjValue();
                    List<String> previousList=new ArrayList<String>(fortifiedFacilitiesNames);
                    fortifiedFacilitiesNames=fort.getFortifiedFacilitiesNames();
                    boolean isTrue=isSubset(previousList, fortifiedFacilitiesNames);
                    //System.out.println(previousList+"\t"+fortifiedFacilitiesNames);
                    System.out.println(q+";"+p+";"+Q+";"+objValue+";"+rObj+";"+medians+";"+fortifiedFacilitiesNames+";"+isTrue);
                    fort.end();
                }
            }
        }*/
    }

    public static void southeasterUS_dataset(){
        /*SupplyNetworkDataset gd = new SupplyNetworkDataset();
        gd.readFacInfoFromFile("//ineg-file/shares/ntscoe_sc_risk/data/facInfoPmedian.txt");
        gd.readCustomerInfoFromFile("//ineg-file/shares/ntscoe_sc_risk/data/custInfoPmedian.txt");
        gd.readIntermediateNodeNamesFromFile("//ineg-file/shares/ntscoe_sc_risk/data/nodeNames.txt");
        gd.readFromToListFromFile("//ineg-file/shares/ntscoe_sc_risk/data/fromTo.txt");*/
        for(double q=0.2;q<=.9;q+=.2){
            for(int p=12;p<=12;p++){
                double objValue = 0.0;
                SupplyNetworkDataset gd = new SupplyNetworkDataset();
                gd.readFacInfoFromFile("//ineg-file/shares/ntscoe_sc_risk/data/facInfoPmedian.txt");
                gd.readCustomerInfoFromFile("//ineg-file/shares/ntscoe_sc_risk/data/custInfoPmedian.txt");
                gd.readIntermediateNodeNamesFromFile("//ineg-file/shares/ntscoe_sc_risk/data/nodeNames.txt");
                gd.readFromToListFromFile("//ineg-file/shares/ntscoe_sc_risk/data/fromTo.txt");
                SupplyNetworkBipartiteDataset snb1 = gd.getBipartite();
                //System.out.println(snb);
                //gd.removeNode(1);
                //SupplyNetworkBipartiteDataset snbRemoved = gd.getBipartite();
                //System.out.println(snbRemoved);
                pMedian pMed = new pMedian(snb1,p);
                pMed.createModel();
                pMed.solve();
                //System.out.println(pMed.printFlowSolution());
                List<Integer> medians = pMed.getOpenedFacilitiesNumbers();
                //System.out.println(facs);
                
                SupplyNetworkBipartiteDataset snb = pMedian.getFacilitiesSelectedDatasetFromList(gd, pMed.getOpenedFacilities());
                pMed.end();
                //pMedianFortificationProblem fort1 = new pMedianFortificationProblem(snb,q,1);
                //for(SortPairMax pair:fort1.getSortedByConsequence(snb.getFacilitiesArray())){
                    //Facility f=(Facility)pair.getOriginalIndex();
                    //System.out.println(f.getName()+";"+pair.getValue());
                //}
                List<Integer> fortifiedFacilitiesNumbers = new ArrayList<Integer>();
                for(int Q=1;Q<=p-2;Q++){
                    pMedianFortificationProblem fort = new pMedianFortificationProblem(snb,q,Q);
                    fort.createModel(true);
                    fort.solve();
                    double rObj= fort.getObjValue();
                    List<Integer> previousList=new ArrayList<Integer>(fortifiedFacilitiesNumbers);
                    fortifiedFacilitiesNumbers=fort.getFortifiedFacilitiesNumbers();
                    boolean isTrue=isSubset(previousList, fortifiedFacilitiesNumbers);
                    //System.out.println(previousList+"\t"+fortifiedFacilitiesNames);
                    System.out.println(q+";"+p+";"+Q+";"+objValue+";"+rObj+";"+medians+";"+fortifiedFacilitiesNumbers+";"+isTrue);
                    fort.end();
                }
            }
        }
    }

    public static void main(String[] args){
        Swain1971_dataset();
        //southeasterUS_dataset();
    }
}

