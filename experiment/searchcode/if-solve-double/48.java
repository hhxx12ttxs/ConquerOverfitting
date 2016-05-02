package plane;


import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;
import java.util.Arrays;
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
public class MultiFacilityRectilinearMinimax {

    private double[][] weightBetweenFacilities;
    private double[][] w;
    private double[][] points;
    private double[][] tchebychevCoordinates;
    private IloCplex cplex;
    private IloNumVar[] z;
    private IloNumVar[][] tchebyshevVars;
    private boolean hasSolved;
    //private int xy;

    public MultiFacilityRectilinearMinimax(int numNewFacilities,double[][] v,double[][] w,double[][] points){
        try {
            cplex = new IloCplex();
        } catch (IloException ex) {
            Logger.getLogger(MultiFacilityRectilinearMinisumCoordinate.class.getName()).log(Level.SEVERE, null, ex);
        }
        tchebyshevVars = new IloNumVar[numNewFacilities][2];
        //p = new IloNumVar[x.length-1][x.length];
        //q = new IloNumVar[x.length-1][x.length];
        //r = new IloNumVar[x.length][points.length];
        //s = new IloNumVar[x.length][points.length];
        this.weightBetweenFacilities = v;
        this.w = w;
        this.points = points;
        this.tchebychevCoordinates = getTchebychevCoordinates(points);
        //this.xy = xy;
    }

    public double[] getTchebychevCoordinates(double[] rectilinearCoordinates){
        double[] newCoors = new double[2];
        newCoors[0]= rectilinearCoordinates[0]+rectilinearCoordinates[1];
        newCoors[1]= -rectilinearCoordinates[0]+rectilinearCoordinates[1];
        return newCoors;
    }

    public double[] getRectilinearCoordinates(double[] tchebychevCoordinates){
        double[] newCoors = new double[2];
        newCoors[0]= .5*(tchebychevCoordinates[0]-tchebychevCoordinates[1]);
        newCoors[1]= .5*(tchebychevCoordinates[0]+tchebychevCoordinates[1]);
        return newCoors;
    }

    public double[][] getTchebychevCoordinates(double[][] rectilinearCoordinates){
        double[][] newCoors = new double[rectilinearCoordinates.length][2];
        for(int i=0;i<rectilinearCoordinates.length;i++)
            newCoors[i] = getTchebychevCoordinates(rectilinearCoordinates[i]);
        return newCoors;
    }

    public double[][] getRectilinearCoordinates(double[][] tchebychevCoordinates){
        double[][] newCoors = new double[tchebychevCoordinates.length][2];
        for(int i=0;i<tchebychevCoordinates.length;i++)
            newCoors[i] = getRectilinearCoordinates(tchebychevCoordinates[i]);
        return newCoors;
    }

    public void createModel(){
        for(int xy=0;xy<2;xy++){
            for(int i=0;i<tchebyshevVars.length;i++){
                try {
                    String name ="";
                    if(xy==0)
                        name ="u_";
                    if(xy==1)
                        name = "v_";
                    tchebyshevVars[i][xy] = cplex.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,name+(i+1));
                } catch (IloException ex) {
                    Logger.getLogger(MultiFacilityRectilinearMinisumCoordinate.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        try {
            z = new IloNumVar[2];
            z[0] = cplex.numVar(Double.MIN_VALUE, Double.MAX_VALUE,"z_u");
            z[1] = cplex.numVar(Double.MIN_VALUE, Double.MAX_VALUE,"z_v");
            for(int xy=0;xy<2;xy++){
                for(int j=0;j < tchebyshevVars.length; j++){
                    for(int i=0;i < points.length;i++){
                        if(w[j][i]!=0){
                            IloLinearNumExpr constraint1 = cplex.linearNumExpr();
                            constraint1.addTerm(-1.0,tchebyshevVars[j][xy]);
                            constraint1.addTerm(1.0/w[j][i],z[xy]);
                            cplex.addGe(constraint1,-tchebychevCoordinates[i][xy],"constraint 1: "+(j+1)+"\t"+(i+1));
                            IloLinearNumExpr constraint2 = cplex.linearNumExpr();
                            constraint2.addTerm(1.0,tchebyshevVars[j][xy]);
                            constraint2.addTerm(1.0/w[j][i],z[xy]);
                            cplex.addGe(constraint2,tchebychevCoordinates[i][xy],"constraint 2: "+(j+1)+"\t"+(i+1));
                        }
                    }
                }
            }
            for(int xy=0;xy<2;xy++){
                for(int k=0;k < tchebyshevVars.length; k++){
                    for(int j=0;j<k;j++){
                        if(weightBetweenFacilities[j][k]!=0){
                            IloLinearNumExpr constraint3 = cplex.linearNumExpr();
                            constraint3.addTerm(1.0,tchebyshevVars[j][xy]);
                            constraint3.addTerm(-1.0,tchebyshevVars[k][xy]);
                            constraint3.addTerm(1.0/weightBetweenFacilities[j][k],z[xy]);
                            cplex.addGe(constraint3,0.0,"constraint 3: "+(j+1)+"\t"+(k+1));
                            IloLinearNumExpr constraint4 = cplex.linearNumExpr();
                            constraint4.addTerm(-1.0,tchebyshevVars[j][xy]);
                            constraint4.addTerm(1.0,tchebyshevVars[k][xy]);
                            constraint4.addTerm(1.0/weightBetweenFacilities[j][k],z[xy]);
                            cplex.addGe(constraint4,0.0,"constraint 4: "+(j+1)+"\t"+(k+1));
                        }
                    }
                }
            }
            //Objective
            cplex.addMinimize(cplex.sum(z[0],z[1]));
        } catch(IloException e) {
            System.err.println("Ilog Error e: ");
            e.printStackTrace();
        }
        //return cplex;
    }

    public void provideInitialSolution(){
        double[] values = {2.25,0,8.5,-1.71,7.3,2.571,13.5,17.2};
        double[] otherParams=new double[values.length];
        Arrays.fill(otherParams,0);
        IloNumVar[] vars=new IloNumVar[tchebyshevVars.length*2+2];
        int counter=0;
        for(int j=0;j<tchebyshevVars.length;j++){
            for(int xy=0;xy<2;xy++){
                vars[counter++]=tchebyshevVars[j][xy];
            }
        }
        vars[tchebyshevVars.length*2]=z[0];
        vars[tchebyshevVars.length*2+1]=z[1];
        System.out.println(cplex.getName()+"\t"+values+"\t"+otherParams+"\t"+Arrays.toString(vars));
        try {
            cplex.setVectors(values, null, vars, null, null, null);
            //cplex.getValue(z[0]);
            //cplex.getValue(z[1]);
        } catch (IloException ex) {
            Logger.getLogger(MultiFacilityRectilinearMinimax.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void solve(){
        //System.out.println(Arrays.deepToString(tchebychevCoordinates));
        //System.out.println(cplex);
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

    public double[] getZValues(){
        if(hasSolved){
            try {
                return new double[]{cplex.getValue(z[0]), cplex.getValue(z[1])};
            } catch (UnknownObjectException ex) {
                Logger.getLogger(MultiFacilityRectilinearMinimax.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IloException ex) {
                Logger.getLogger(MultiFacilityRectilinearMinimax.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public double[][] getLocations(){
        double[][] locations = new double[tchebyshevVars.length][2];
        for(int j=0;j<tchebyshevVars.length;j++){
            locations[j]=new double[2];
            for(int xy=0;xy<2;xy++ ){
                try {
                    if(hasSolved){
                        locations[j][xy] = cplex.getValue(tchebyshevVars[j][xy]);
                    }
                } catch (UnknownObjectException ex) {
                    Logger.getLogger(MultiFacilityRectilinearMinisumCoordinate.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IloException ex) {
                    Logger.getLogger(MultiFacilityRectilinearMinisumCoordinate.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return locations;
    }

    public static void main(String[] args){
        double v[][] = {{0,1},{1,0}};
        double w[][] = {{1,2,1,0,0},{0,0,2,1,2}};
        double[][] points = {{0,10},{5,15},{15,5},{15,10},{10,0}};
        //int xy=0;
        MultiFacilityRectilinearMinimax prob = new MultiFacilityRectilinearMinimax(v.length, v, w,points);
        prob.createModel();
        prob.provideInitialSolution();
        prob.solve();
        double[][] locations =prob.getLocations();
        System.out.println(Arrays.deepToString(locations));
        double obj1=prob.getObjValue();
        //System.out.println(obj1);
        double[] zVals=prob.getZValues();
        System.out.println(Arrays.toString(zVals));
        double[][] rectilinear = prob.getRectilinearCoordinates(locations);
        System.out.println(Arrays.deepToString(rectilinear));
        /*int xy2=1;
        MultiFacilityRectilinearMinimaxP1 prob2 = new MultiFacilityRectilinearMinimaxP1(3, v, w,points,xy2);
        prob2.createModel();
        prob2.solve();
        double[] locations2 =prob2.getLocations();
        System.out.println(Arrays.toString(locations2));
        double obj2=prob2.getObjValue();
        System.out.println(obj2);
        //double[][] tchebychev = prob.getRectilinearCoordinates(new double[][]{locations,locations2});
        //System.out.println(Arrays.deepToString(tchebychev));
        //System.out.println(Math.max(obj1, obj2));*/
    }
}

