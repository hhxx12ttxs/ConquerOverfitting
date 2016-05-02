package plane;


import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;
import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hmedal
 */
public class MultiFacilityRectilinearMinisumCoordinate {

    private double[][] v;
    private double[][] w;
    double[][] points;
    private IloCplex cplex;
    private IloNumVar[] x;
    private IloNumVar[][] p;
    private IloNumVar[][] q;
    private IloNumVar[][] r;
    private IloNumVar[][] s;
    private boolean hasSolved;
    private int xy;

    public MultiFacilityRectilinearMinisumCoordinate(int numNewFacilities,double[][] v,double[][] w,double[][] points,int xy){
        try {
            cplex = new IloCplex();
        } catch (IloException ex) {
            Logger.getLogger(MultiFacilityRectilinearMinisumCoordinate.class.getName()).log(Level.SEVERE, null, ex);
        }
        x = new IloNumVar[numNewFacilities];
        p = new IloNumVar[x.length-1][x.length];
        q = new IloNumVar[x.length-1][x.length];
        r = new IloNumVar[x.length][points.length];
        s = new IloNumVar[x.length][points.length];
        this.v = v;
        this.w = w;
        this.points = points;
        this.xy = xy;
    }

    public void createModel(){
        for(int i=0;i<x.length;i++){
            try {
                x[i] = cplex.numVar(Double.MIN_VALUE, Double.MAX_VALUE,"x_"+(i+1));
            } catch (IloException ex) {
                Logger.getLogger(MultiFacilityRectilinearMinisumCoordinate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        for(int j=0;j < x.length-1; j++){
            p[j]=new IloNumVar[x.length];
            q[j]=new IloNumVar[x.length];
            for(int k=j+1;k<x.length;k++){
                try {
                    p[j][k] = cplex.numVar(0, Double.MAX_VALUE,"p_"+(j+1)+","+(k+1));
                    q[j][k]=cplex.numVar(0, Double.MAX_VALUE,"q_"+(j+1)+","+(k+1));
                } catch (IloException ex) {
                    Logger.getLogger(MultiFacilityRectilinearMinisumCoordinate.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        for(int j=0;j < x.length; j++){
                r[j]=new IloNumVar[points.length];
                s[j]=new IloNumVar[points.length];
                for(int i=0;i < points.length;i++){
                try {
                    r[j][i] = cplex.numVar(0, Double.MAX_VALUE,"r_"+(j+1)+","+(i+1));
                    s[j][i]=cplex.numVar(0, Double.MAX_VALUE,"s_"+(j+1)+","+(i+1));
                } catch (IloException ex) {
                    Logger.getLogger(MultiFacilityRectilinearMinisumCoordinate.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        try {
            for(int k=0;k < x.length; k++){
                for(int j=0;j<k;j++){
                    IloLinearNumExpr constraint1 = cplex.linearNumExpr();
                    constraint1.addTerm(1.0,x[j]);
                    constraint1.addTerm(-1.0,x[k]);
                    constraint1.addTerm(1.0,p[j][k]);
                    constraint1.addTerm(-1.0,q[j][k]);
                    cplex.addEq(0.0, constraint1,"constraint 1: "+(j+1)+"\t"+(k+1));
                }
            }
            for(int j=0;j < x.length; j++){
                for(int i=0;i < points.length;i++){
                    IloLinearNumExpr constraint2 = cplex.linearNumExpr();
                    constraint2.addTerm(1.0,x[j]);
                    constraint2.addTerm(-1, r[j][i]);
                    constraint2.addTerm(1, s[j][i]);
                    cplex.addEq(points[i][xy], constraint2,"constraint 2: "+(j+1)+"\t"+(i+1));
                }
            }

            //Objective
            IloLinearNumExpr objective = cplex.linearNumExpr();
            for(int k=0;k < x.length; k++){
                for(int j=0;j<k;j++){
                    objective.addTerm(v[j][k],p[j][k]);
                    objective.addTerm(v[j][k],q[j][k]);
                }
            }
            for(int j=0;j < x.length; j++){
                for(int i=0;i < points.length;i++){
                    objective.addTerm(w[j][i],r[j][i]);
                    objective.addTerm(w[j][i],s[j][i]);
                }
            }
            cplex.addMinimize(objective);
        } catch(IloException e) {
            System.err.println("Ilog Error e: ");
            e.printStackTrace();
        }
    }

    public void solve(){
        System.out.println(cplex);
        try {
            hasSolved = cplex.solve();
        } catch (IloException ex) {
            Logger.getLogger(MultiFacilityRectilinearMinisumCoordinate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public double getObjValue(){
        double objValue = -1;
        try {
            if(hasSolved){
                    objValue=cplex.getObjValue();
                }
        } catch (IloException ex) {
            Logger.getLogger(MultiFacilityRectilinearMinisumCoordinate.class.getName()).log(Level.SEVERE, null, ex);
        }
        return objValue;
    }

    public double getSolution(double[] coors,int xy){
        double sum=0;
        for(int k=0;k<x.length;k++){
            for(int j=0;j<k;j++){
                sum+=v[j][k]*Math.abs(coors[j]-coors[k]);
            }
        }
        //System.out.println(sum);
        for(int j=0;j<x.length;j++){
            for(int i=0;i<points.length;i++){
                //System.out.println((j+1)+"\t"+(i+1)+"\t"+w[j][i]+"\t"+Math.abs(coors[j]-points[i][xy])+"\t"+w[j][i]*Math.abs(coors[j]-points[i][xy]));
                sum+=w[j][i]*Math.abs(coors[j]-points[i][xy]);
            }
        }
        return sum;
    }

    public double[] getLocations(){
        double[] locations = new double[x.length];
        int i=0;
        for(IloNumVar var:x){
            try {
                if(hasSolved){
                    locations[i] = cplex.getValue(x[i]);
                }
            } catch (UnknownObjectException ex) {
                Logger.getLogger(MultiFacilityRectilinearMinisumCoordinate.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IloException ex) {
                Logger.getLogger(MultiFacilityRectilinearMinisumCoordinate.class.getName()).log(Level.SEVERE, null, ex);
            }
            i++;
        }
        return locations;
    }

    public static void HS1_problem10b(){
        double v[][] = {{0,1,3},{1,0,2},{3,2,0}};
        double w[][] = {{4,2,0,4,0,0},{2,0,4,0,0,7},{0,0,4,2,5,0}};
        double[][] points = {{0,10},{15,0},{10,25},{5,15},{20,20},{25,5}};
        int xy=1;
        MultiFacilityRectilinearMinisumCoordinate prob = new MultiFacilityRectilinearMinisumCoordinate(3, v, w,points,xy);
        prob.createModel();
        prob.solve();
        double[] locations =prob.getLocations();
        System.out.println(Arrays.toString(locations));
        System.out.println(prob.getObjValue());
        //System.out.println(prob.getSolution(locations,xy));
    }

    public static void HS1_problem9a(){
        double v[][] = {{0,4},{4,0}};
        double w[][] = {{10,6,5,4,3},{2,3,4,6,12}};
        double[][] points = {{10,25},{10,15},{15,30},{20,10},{25,25}};
        int xy=0;
        MultiFacilityRectilinearMinisumCoordinate prob = new MultiFacilityRectilinearMinisumCoordinate(2, v, w,points,xy);
        prob.createModel();
        prob.solve();
        double[] locations =prob.getLocations();
        System.out.println(Arrays.toString(locations));
        System.out.println(prob.getObjValue());
        int xy2=1;
        MultiFacilityRectilinearMinisumCoordinate prob2 = new MultiFacilityRectilinearMinisumCoordinate(2, v, w,points,xy2);
        prob2.createModel();
        prob2.solve();
        double[] locations2 =prob2.getLocations();
        System.out.println(Arrays.toString(locations2));
        System.out.println(prob2.getObjValue());
    }

    public static void HS2_problem6a(){
        double v[][] = {{0,8},{8,0}};
        double w[][] = {{6,3,5},{0,7,2}};
        double[][] points = {{8,15},{10,20},{30,10}};
        int xy=0;
        MultiFacilityRectilinearMinisumCoordinate prob = new MultiFacilityRectilinearMinisumCoordinate(2, v, w,points,xy);
        prob.createModel();
        prob.solve();
        double[] locations =prob.getLocations();
        System.out.println(Arrays.toString(locations));
        System.out.println(prob.getObjValue());
        int xy2=1;
        MultiFacilityRectilinearMinisumCoordinate prob2 = new MultiFacilityRectilinearMinisumCoordinate(2, v, w,points,xy2);
        prob2.createModel();
        prob2.solve();
        double[] locations2 =prob2.getLocations();
        System.out.println(Arrays.toString(locations2));
        System.out.println(prob2.getObjValue());
    }

    public static void takehome1_problem10(){
        double v[][] = {{0,1},{1,0}};
        double w[][] = {{1,2,1,0,0},{0,0,2,1,2}};
        double[][] points = {{0,10},{5,15},{15,5},{15,10},{10,0}};
        int xy=0;
        MultiFacilityRectilinearMinisumCoordinate prob = new MultiFacilityRectilinearMinisumCoordinate(2, v, w,points,xy);
        prob.createModel();
        prob.solve();
        double[] locations =prob.getLocations();
        System.out.println(Arrays.toString(locations));
        System.out.println(prob.getObjValue());
        int xy2=1;
        MultiFacilityRectilinearMinisumCoordinate prob2 = new MultiFacilityRectilinearMinisumCoordinate(2, v, w,points,xy2);
        prob2.createModel();
        prob2.solve();
        double[] locations2 =prob2.getLocations();
        System.out.println(Arrays.toString(locations2));
        System.out.println(prob2.getObjValue());
    }

    public static void main(String[] args){
        takehome1_problem10();
    }
}

