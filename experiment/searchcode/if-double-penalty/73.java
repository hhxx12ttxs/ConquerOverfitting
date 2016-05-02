/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package scRisk.riskMetrics;

import java.util.List;
import objects.Customer;
import objects.Edge;
import objects.IntermediateNode;
import objects.NetworkEntity;
import objects.SupplyNetworkArc;
import network.SupplyNetworkDataset;
import models.facLoc.CFLP;
import models.networkOpt.TransportationIP;

/**
 *
 * @author hmedal
 */
public class TotalUnmetDemand{

    public double getMetricValue(SupplyNetworkDataset dataset,NetworkEntity entityToChange, double capacityDegradationProportion,double penalty) {
        SupplyNetworkDataset newDataset = dataset.newInstance();
        if(entityToChange!=null){
            if(capacityDegradationProportion==1){
                newDataset.removeEntity(entityToChange);
                //System.out.println("remove");
            }
            else{
                double currentCapacity = entityToChange.getCapacity();
                entityToChange.setCapacity(currentCapacity*(1-capacityDegradationProportion));
            }
        }
        TransportationIP trans = new TransportationIP(newDataset.getBipartite(),penalty);
        trans.createModel();
        trans.solve();
        return trans.getTotalUnMetDemand();
    }

    public static void removingNodesExperiment(){
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
        //System.out.println(trans.printFlowSolution());
        trans.end();
        List<IntermediateNode> usedNodes = trans.getUsedNodes(gd.getMinimumEdgeMap());
        //for(IntermediateNode node:usedNodes)
        //    System.out.println(node.getName());
        TotalUnmetDemand metric = new TotalUnmetDemand();
        double penalty = 1000;
        for(IntermediateNode n:usedNodes){
            //IntermediateNode n = (IntermediateNode)gd.getNodesMap().get(36);
            double capacityDegradationProportion = 1;
            NetworkEntity entity = null;
            if(n.getClass()!=Customer.class){
                entity = (NetworkEntity)n;
                double value = metric.getMetricValue(facilitiesSelectedDataset,entity,capacityDegradationProportion,penalty);
                System.out.println(n.getNumber()+"\t\t\t"+n.getName()+"\t\t"+value);
            }
        }

    }

    public static void removingArcsExperiment(){
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
        TotalUnmetDemand metric = new TotalUnmetDemand();
        double penalty=1000;
        for(Edge e:usedEdges){
            double capacityDegradationProportion = 1;
            NetworkEntity entity = null;
            //if(n.getClass()!=Customer.class){
                entity = (NetworkEntity)e;
                double value = metric.getMetricValue(facilitiesSelectedDataset, entity,capacityDegradationProportion,penalty);
                String fromName = ((IntermediateNode)e.getFromNode()).getName();
                String toName = ((IntermediateNode)e.getToNode()).getName();
                int fromIndex = ((IntermediateNode)e.getFromNode()).getNumber();
                int toIndex = ((IntermediateNode)e.getToNode()).getNumber();
                System.out.println(fromIndex+"\t\t\t"+fromName+"\t\t"+toIndex+"\t\t"+toName+"\t\t"+value);
            //}
        }

    }

    public static void main(String[] args){
        removingArcsExperiment();
    }
}

