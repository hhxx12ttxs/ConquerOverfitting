/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package scRisk.riskMetrics;

import java.text.DecimalFormat;
import java.util.List;
import objects.Customer;
import objects.Edge;
import objects.IntermediateNode;
import objects.NetworkEntity;
import objects.SupplyNetworkArc;
import network.SupplyNetworkBipartiteDataset;
import network.SupplyNetworkDataset;
import models.facLoc.CFLP;
import models.networkOpt.TransportationIP;

/**
 *
 * @author hmedal
 */
public class DemandProportionUnmetForCustomer {
    private static DecimalFormat fmt;

    public static double getMetricValue(SupplyNetworkDataset dataset,Customer c,NetworkEntity entityToChange, double capacityDegradationProportion,double penalty) {
        SupplyNetworkDataset newDataset = dataset.newInstance();
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
        TransportationIP trans = new TransportationIP(newDataset.getBipartite(),penalty);
        trans.createModel();
        trans.solve();
        return trans.getProportionUnmet(c);
    }

    public static void removingNodesExperiment(){
        DecimalFormat fmt = new DecimalFormat("#.###");
        SupplyNetworkDataset gd = new SupplyNetworkDataset();
        gd.readFacInfoFromFile("//130.184.181.4/ntscoe_sc_risk/data/facInfo_9-8.txt");
        gd.readCustomerInfoFromFile("//130.184.181.4/ntscoe_sc_risk/data/custInfo_9-8.txt");
        gd.readIntermediateNodeNamesFromFile("//130.184.181.4/ntscoe_sc_risk/data/nodeNames_9-8.txt");
        gd.readFromToListFromFile("//130.184.181.4/ntscoe_sc_risk/data/fromTo_9-8.txt");
        SupplyNetworkDataset facilitiesSelectedDataset = CFLP.getFacilitiesSelectedDataset(gd);
        SupplyNetworkBipartiteDataset bDataset = facilitiesSelectedDataset.getBipartite();
        TransportationIP trans = new TransportationIP(bDataset,1000);
        trans.createModel();
        trans.solve();
        trans.setFlowOnNodes();
        //System.out.println(trans.printFlowSolution());
        trans.end();
        List<IntermediateNode> usedNodes = trans.getUsedNodes(gd.getMinimumEdgeMap());
        //for(IntermediateNode node:usedNodes)
        //    System.out.println(node.getName());
        Customer[] custs = facilitiesSelectedDataset.getCustomersArray();
        DemandProportionUnmetForCustomer unmetDemand = new DemandProportionUnmetForCustomer();
        ScRiskMetricIfc metric1 = new TransportationIpOptimalCost();
        ScRiskMetricIfc metric2 = new TransportationCost();
        double penalty=1000;
        System.out.print(";;;;");
        for(Customer c:custs){
            System.out.print(c.getName()+";");
        }
        System.out.println();
        for(IntermediateNode n:usedNodes){
            double capacityDegradationProportion = 1;
            NetworkEntity entity = null;
            entity = (NetworkEntity)n;
            if(n.getClass()!=Customer.class){
                double indexValue = ImportanceIndex.getMetricValue(facilitiesSelectedDataset, metric1,entity,capacityDegradationProportion);
                double indexValue2 = ImportanceIndex.getMetricValue(facilitiesSelectedDataset, metric2,entity,capacityDegradationProportion);
                System.out.print(n.getNumber()+";"+n.getName()+";"+fmt.format(indexValue)+";"+fmt.format(indexValue2)+";");
                for(Customer c:custs){
                    //IntermediateNode n = (IntermediateNode)gd.getNodesMap().get(36);

                    double value = unmetDemand.getMetricValue(facilitiesSelectedDataset,c,entity,capacityDegradationProportion,penalty);
                    System.out.print(fmt.format(value)+";");
                }
                System.out.println("");
            }
        }
    }

    public static void removingArcsExperiment(){
        DecimalFormat fmt = new DecimalFormat("#.###");
        SupplyNetworkDataset gd = new SupplyNetworkDataset();
        gd.readFacInfoFromFile("//130.184.181.4/ntscoe_sc_risk/data/facInfo_9-8.txt");
        gd.readCustomerInfoFromFile("//130.184.181.4/ntscoe_sc_risk/data/custInfo_9-8.txt");
        gd.readIntermediateNodeNamesFromFile("//130.184.181.4/ntscoe_sc_risk/data/nodeNames_9-8.txt");
        gd.readFromToListFromFile("//130.184.181.4/ntscoe_sc_risk/data/fromTo_9-8.txt");
        SupplyNetworkDataset facilitiesSelectedDataset = CFLP.getFacilitiesSelectedDataset(gd);
        TransportationIP trans = new TransportationIP(facilitiesSelectedDataset.getBipartite(),1000);
        trans.createModel();
        trans.solve();
        trans.setFlowOnNodes();
        trans.setFlowOnArcs();
        //System.out.println(trans.printFlowSolution());
        trans.end();
        List<SupplyNetworkArc> usedEdges = trans.getUsedEdges(gd.getMinimumEdgeMap());
        //System.out.println(usedEdges);
        //for(Edge edge:usedEdges)
        //    System.out.println("edge "+edge);
        Customer[] custs = facilitiesSelectedDataset.getCustomersArray();
        DemandProportionUnmetForCustomer unmetDemand = new DemandProportionUnmetForCustomer();
        ScRiskMetricIfc metric1 = new TransportationIpOptimalCost();
        ScRiskMetricIfc metric2 = new TransportationCost();
        double penalty=1000;
        System.out.print(";;;;;;");
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
                System.out.print(fromIndex+";"+fromName+";"+toIndex+";"+toName+";"+fmt.format(indexValue1)+";"+fmt.format(indexValue2)+";");
                for(Customer c:custs){
                    double value = unmetDemand.getMetricValue(facilitiesSelectedDataset, c,entity,capacityDegradationProportion,penalty);
                    System.out.print(fmt.format(value)+";");
                }
                System.out.println("");
            //}
        }

    }

    public static void main(String[] args){
        removingNodesExperiment();
        removingArcsExperiment();
    }
}

