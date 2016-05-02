/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package scRisk.metrics;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import objects.Customer;
import objects.Edge;
import objects.IntermediateNode;
import objects.NetworkEntity;
import objects.SupplyNetworkArc;
import scRisk.networks.SupplyNetworkBipartiteDataset;
import scRisk.networks.SupplyNetworkDataset;
import scRisk.optModels.classic.CFLP;
import scRisk.optModels.classic.TransportationIP;

/**
 *
 * @author hmedal
 */
public class DemandUnmetForCustomer {

    public double getMetricValue(SupplyNetworkDataset dataset,Customer c,NetworkEntity entityToChange, double capacityDegradationProportion,double penalty) {
        //System.out.println("getMetricValue");
        SupplyNetworkDataset newDataset = dataset.newInstance();
        //System.out.println("\nbefore removed\n"+newDataset.getBipartite());
        //System.out.println("remove "+entityToChange);
        if(entityToChange!=null){
            if(capacityDegradationProportion==1){
                newDataset.removeEntity(entityToChange);
                //System.out.println("remove "+entityToChange);
            }
            else{
                double currentCapacity = entityToChange.getCapacity();
                entityToChange.setCapacity(currentCapacity*(1-capacityDegradationProportion));
            }
        }
        //System.out.println("GET BIP");
        SupplyNetworkBipartiteDataset bipDataset=newDataset.getBipartite();
        //System.out.println("after removed\n"+bipDataset);
        TransportationIP trans = new TransportationIP(bipDataset,penalty);
        trans.createModel();
        //System.out.println(trans);
        trans.solve();
        double unmetDemand = trans.getDemandUnmet(c);
        //System.out.println("\n"+c.getName()+" unmet= "+unmetDemand);
        return unmetDemand;
    }

    public static void removingNodesExperiment(int weightType){
        DecimalFormat fmt = new DecimalFormat("#.###");
        SupplyNetworkDataset gd = new SupplyNetworkDataset();
        gd.readFacInfoFromFile("//130.184.181.4/ntscoe_sc_risk/data/facInfo.txt");
        gd.readCustomerInfoFromFile("//130.184.181.4/ntscoe_sc_risk/data/custInfo.txt");
        gd.readIntermediateNodeNamesFromFile("//130.184.181.4/ntscoe_sc_risk/data/nodeNames.txt");
        gd.readFromToListFromFile("//130.184.181.4/ntscoe_sc_risk/data/fromTo.txt");
        gd.createMinimumEdgeMap(weightType);
        gd.changeCapacities(1.2);
        SupplyNetworkDataset facilitiesSelectedDataset = CFLP.getFacilitiesSelectedDataset(gd);
        SupplyNetworkBipartiteDataset bDataset = facilitiesSelectedDataset.getBipartite();
        TransportationIP trans = new TransportationIP(bDataset,5000);
        trans.createModel();
        trans.solve();
        trans.setFlowOnNodes();
        //System.out.println(trans.printFlowSolution());
        trans.end();
        List<IntermediateNode> usedNodes=new ArrayList<IntermediateNode>();
        usedNodes = trans.getUsedNodes(gd.getMinimumEdgeMap());
        //usedNodes.add((IntermediateNode)gd.getNode(12));
        //for(IntermediateNode node:usedNodes)
        //    System.out.println(node.getName());
        Customer[] custs = facilitiesSelectedDataset.getCustomersArray();
        DemandUnmetForCustomer unmetDemand = new DemandUnmetForCustomer();
        DemandProportionUnmetForCustomer unmetDemandProportion = new DemandProportionUnmetForCustomer();
        ScRiskMetricIfc metric1 = new TransportationIpOptimalCost();
        ScRiskMetricIfc metric2 = new TransportationCost();
        double penalty=5000;
        /*for(Node n:gd.getEdgesMap().keySet()){
            System.out.println(n);
            System.out.println(gd.getEdgesMap().get(n).keySet());
        }*/
        System.out.print("#;name;cost inc;trans cost incr;");
        for(Customer c:custs){
            System.out.print(c.getName()+";");
        }
        System.out.println();
        for(IntermediateNode n:usedNodes){
            double capacityDegradationProportion = 1;
            NetworkEntity entity = null;
            entity = (NetworkEntity)n;
            if(n.getClass()!=Customer.class){
                //System.out.println("remove in loop "+n.getName());
                double indexValue = ImportanceIndex.getMetricValue(facilitiesSelectedDataset, metric1,entity,capacityDegradationProportion);
                double indexValue2 = ImportanceIndex.getMetricValue(facilitiesSelectedDataset, metric2,entity,capacityDegradationProportion);
                System.out.print(n.getNumber()+";"+n.getName()+";"+fmt.format(indexValue)+";"+fmt.format(indexValue2)+";");
                for(Customer c:custs){
                    //IntermediateNode n = (IntermediateNode)gd.getNodesMap().get(36);
                    double value = unmetDemand.getMetricValue(facilitiesSelectedDataset,c,entity,capacityDegradationProportion,penalty);
                    double valueProp = unmetDemandProportion.getMetricValue(facilitiesSelectedDataset, c,entity,capacityDegradationProportion,penalty);
                    System.out.print(fmt.format(value)+"("+fmt.format(valueProp)+");");
                }
                System.out.println("");
            }
        }
    }

    public static void removingArcsExperiment(int weightType){
        DecimalFormat fmt = new DecimalFormat("#.###");
        SupplyNetworkDataset gd = new SupplyNetworkDataset();
        gd.readFacInfoFromFile("//130.184.181.4/ntscoe_sc_risk/data/facInfo.txt");
        gd.readCustomerInfoFromFile("//130.184.181.4/ntscoe_sc_risk/data/custInfo.txt");
        gd.readIntermediateNodeNamesFromFile("//130.184.181.4/ntscoe_sc_risk/data/nodeNames.txt");
        gd.readFromToListFromFile("//130.184.181.4/ntscoe_sc_risk/data/fromTo.txt");
        gd.createMinimumEdgeMap(weightType);
        gd.changeCapacities(1.2);
        SupplyNetworkDataset facilitiesSelectedDataset = CFLP.getFacilitiesSelectedDataset(gd);
        TransportationIP trans = new TransportationIP(facilitiesSelectedDataset.getBipartite(),5000);
        trans.createModel();
        trans.solve();
        trans.setFlowOnNodes();
        trans.setFlowOnArcs();
        //System.out.println(trans.printFlowSolution());
        trans.end();
        List<SupplyNetworkArc> usedEdges = new ArrayList<SupplyNetworkArc>();
        //usedEdges.add((SupplyNetworkArc)gd.getEdge(28, 2, 3));
        usedEdges = trans.getUsedEdges(gd.getMinimumEdgeMap());
        //System.out.println(usedEdges);
        //for(Edge edge:usedEdges)
        //    System.out.println("edge "+edge);
        Customer[] custs = facilitiesSelectedDataset.getCustomersArray();
        //Customer[] custs = {facilitiesSelectedDataset.getCustomersArray()[0]};
        DemandUnmetForCustomer unmetDemand = new DemandUnmetForCustomer();
        DemandProportionUnmetForCustomer unmetDemandProportion = new DemandProportionUnmetForCustomer();
        ScRiskMetricIfc metric1 = new TransportationIpOptimalCost();
        ScRiskMetricIfc metric2 = new TransportationCost();
        double penalty=100000;
        System.out.print("from #;from name;to #;to name;mode of removed;cost inc;trans cost incr;");
        for(Customer c:custs){
            System.out.print(c.getName()+";");
        }
        System.out.println();
        for(Edge e:usedEdges){
            double capacityDegradationProportion = 1;
            NetworkEntity entity = null;
            //if(n.getClass()!=Customer.class){
                entity = (NetworkEntity)e;
                double indexValue1 = ImportanceIndex.getMetricValue(facilitiesSelectedDataset, metric1,entity,capacityDegradationProportion);
                double indexValue2 = ImportanceIndex.getMetricValue(facilitiesSelectedDataset, metric2,entity,capacityDegradationProportion);
                String fromName = ((IntermediateNode)e.getFromNode()).getName();
                String toName = ((IntermediateNode)e.getToNode()).getName();
                int fromIndex = ((IntermediateNode)e.getFromNode()).getNumber();
                int toIndex = ((IntermediateNode)e.getToNode()).getNumber();
                int mode = ((SupplyNetworkArc)e).getMode();
                System.out.print(fromIndex+";"+fromName+";"+toIndex+";"+toName+";"+mode+";"+fmt.format(indexValue1)+";"+fmt.format(indexValue2)+";");
                for(Customer c:custs){
                    double value = unmetDemand.getMetricValue(facilitiesSelectedDataset, c,entity,capacityDegradationProportion,penalty);
                    double valueProp = unmetDemandProportion.getMetricValue(facilitiesSelectedDataset, c,entity,capacityDegradationProportion,penalty);
                    System.out.print(fmt.format(value)+"("+fmt.format(valueProp)+");");
                }
                System.out.println("");
            //}
        }
    }

    public static void main(String[] args){
        String[] objectives = {"distance","cost","time"};
        /*for(int i=0;i<3;i++){
            System.out.println("removal:;nodes");
            System.out.println("objective:; "+objectives[i]);
            removingNodesExperiment(i+1);
        }*/
        for(int i=2;i<3;i++){
            System.out.println("metric:;importance index");
            System.out.println("removal:;arcs");
            System.out.println("objective:; "+objectives[i]);
            System.out.println("capacity increase: "+1.2);
            removingArcsExperiment(i+1);
        }
    }
}

