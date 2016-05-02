/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package facLoc.discrete;

import cyclic.*;
import util.FloydWarshall;
import util.Node;
import util.Edge;
import cyclic.DispersionLocationIP;
import cyclic.SetCoverCyclic;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hmedal
 */
public class OLD_MinimaxLocationSetCoverAlgorithm {

    private double objectiveValue;
    private ArrayList<Integer> centers;
    private int n;
    private double[][] distance;
    private int m;

    public OLD_MinimaxLocationSetCoverAlgorithm(double[][] distance,int n){
        this.n=n;
        this.m=distance.length;
        this.distance=distance;
    }

    public void solve(){
        double coverDistanceUB = (m-1)*DispersionLocationIP.getMax(distance);
        //double coverDistanceUB = 85;
        double coverDistanceLB = 0;
        runIterations(coverDistanceLB, coverDistanceUB);
    }

    public void runIterations(double coverDistanceLB,double coverDistanceUB){
        double coverDistance = Math.floor((coverDistanceLB+coverDistanceUB)/2);
        LocationSetCover sc = new LocationSetCover(distance,coverDistance);
        sc.createModel();
        sc.solve();
        centers = sc.getAssignments();
        double setCoverObjectiveValue = sc.getResponses().getDoubleResponse("objValue");
        System.out.println(coverDistanceLB+";"+coverDistanceUB+";"+coverDistance+";"+setCoverObjectiveValue+";"+centers);
        if(setCoverObjectiveValue<=n){
            coverDistanceUB = coverDistance;
        }
        else{
            coverDistanceLB = coverDistance+1;
        }
        if(coverDistanceLB!=coverDistanceUB){
            runIterations(coverDistanceLB,coverDistanceUB);
        }
        else{
            objectiveValue = coverDistanceLB;
            SetCoverCyclic scFinal = new SetCoverCyclic(distance,objectiveValue);
            scFinal.createModel();
            scFinal.solve();
            centers = scFinal.getAssignments();
        }
    }

    public static int[][] createAmatrix(double[][] distance,double value){
        int[][] aMatrix = new int[distance.length][];
        for(int i=0;i<distance.length;i++){
            aMatrix[i]=new int[distance[i].length];
            for(int j=0;j<distance[i].length;j++){
                if(distance[i][j]<=value)
                    aMatrix[i][j]=1;
            }
        }
        return aMatrix;
    }

    public double solveSetCover(double coverDistance){
        int[][] a = createAmatrix(distance, coverDistance);
        IloNumVar[] x;
        try {
            IloCplex cplex = new IloCplex();
            x=new IloNumVar[m];
            for (int j = 0; j < m; j++) {
                    x[j]=cplex.numVar(0,Double.POSITIVE_INFINITY,"x_"+(j+1));
            }
            //constraint 1
            for (int i = 0; i < m; i++) {
                IloLinearNumExpr constraint1 = cplex.linearNumExpr();
                   for (int j = 0; j < m; j++) {
                       constraint1.addTerm(a[i][j],x[j]);
                   }
                   cplex.addGe(constraint1,1);
            }


            IloLinearNumExpr z = cplex.linearNumExpr();
            for (int j = 0; j < m; j++) {
                z.addTerm(1.0,x[j]);
            }
            cplex.addMinimize(z);
            //System.out.println(cplex);
        } catch (IloException ex) {

        }
        return -1;
    }

    public static double[][] getShortestDistances(int[][] edgesArray){
       Map<Integer,Node> nodes = new HashMap<Integer,Node>();
       Edge[] edges = new Edge[edgesArray.length*2];
       int i=0;
       for(int[] edge:edgesArray){
           int from = edge[0];
           int to = edge[1];
           int distance = edge[2];
           if(!nodes.containsKey(from))
               nodes.put(from, new Node(from));
           if(!nodes.containsKey(to))
               nodes.put(to, new Node(to));
           edges[i]=new Edge(nodes.get(from),nodes.get(to),distance);
           edges[i+edges.length/2]=new Edge(nodes.get(to),nodes.get(from),distance);
           i++;
       }
       //System.out.println(Arrays.deepToString(nodes));
       //System.out.println(Arrays.deepToString(edges));
       FloydWarshall fw = new FloydWarshall();
       //System.out.println(nodes);
       fw.calcShortestPaths(nodes.values().toArray(new Node[nodes.size()]), edges);
       double[][] shortestDistances = new double[nodes.size()][];
       int s=0;
       int t=0;
       for(int m:nodes.keySet()){
           shortestDistances[s]= new double[nodes.size()];
           Node a = nodes.get(m);
           t=0;
           for(int n:nodes.keySet()){
               Node b= nodes.get(n);
               //System.out.println(k+"\t"+l);
               if(a.getNumber()==b.getNumber())
                   shortestDistances[s][t]=0;
               else
                shortestDistances[s][t]=fw.getShortestDistance(a,b);
               t++;
           }
           s++;
       }
       //System.out.println(Arrays.deepToString(shortestDistances));
       return shortestDistances;
   }

    public double getObjectiveValue(){
        return objectiveValue;
    }

    public ArrayList<Integer> getCenters(){
        return centers;
    }

    public static void exampleInNotes(){
        int[][] edgesArray = {
           {	1	,	3	,	10	}	,
            {	1	,	4	,	9	}	,
            {	1	,	2	,	12	}	,
            {	2	,	3	,	8	}	,
            {	2	,	4	,	13	}	,
            {	2	,	6	,	7	}	,
            {	3	,	5	,	7	}	,
            {	3	,	6	,	13	}	,
            {	4	,	6	,	17	},
            {	5	,	6	,	9	}
       };
       double[][] distances = getShortestDistances(edgesArray);
       OLD_MinimaxLocationSetCoverAlgorithm p = new OLD_MinimaxLocationSetCoverAlgorithm(distances, 2);
       p.solve();
       ArrayList<Integer> centers = p.getCenters();
       System.out.println(centers);
       double obj = p.getObjectiveValue();
       System.out.println(obj);
    }

    public static void problem8EandF(){
        int[][] edgesArray = {
           {	1	,	2	,	5	}	,
            {	1	,	3	,	8	}	,
            {	2	,	4	,	6	}	,
            {	2	,	5	,	9	}	,
            {	3	,	6	,	7	}	,
            {	4	,	5	,	2	}	,
            {	4	,	6	,	3	}	,
            {	5	,	7	,	4	}	,
            {	6	,	7	,	1	}
       };
       double[][] distances = getShortestDistances(edgesArray);
       for(int n=2;n<=3;n++){
           OLD_MinimaxLocationSetCoverAlgorithm p = new OLD_MinimaxLocationSetCoverAlgorithm(distances, n);
           p.solve();
           ArrayList<Integer> centers = p.getCenters();
           System.out.println(centers);
           double obj = p.getObjectiveValue();
           System.out.println(obj);
       }
    }

    public static void main(String[] args){
        problem8EandF();
    }
}

