/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package facLoc.discrete;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import datasets.DatasetFromFileIfc;
import parameters.Responses;
import java.util.ArrayList;
import java.util.List;
import dataStructures.ListUtil;
import dataStructures.MatrixUtil;
import models.facLoc.FacLocCplexResponses;
import datasets.facLoc.FailableDistanceMatrixDataset;
import datasets.facLoc.PointsDatasetFixedCostRandomFailuresDifferentProbsDataset;
import org.apache.commons.lang.StringUtils;
import parameters.OptModelExperimentable;
import parameters.Parameters;

/**
 *
 * @author hmedal
 */
public class MinimaxLocationSetCoverAlgorithm implements OptModelExperimentable {

    protected double objectiveValue;
    protected ArrayList<Integer> centers;
    protected int numFacs;
    protected LocationSetCover lsc;
    protected double coverDistLB;
    protected double coverDistUB;
    protected double runTime;
    protected List<Double> valuesToTry;
    protected PrintStream out;

    public MinimaxLocationSetCoverAlgorithm(){
        lsc = new LocationSetCover();
        lsc.setOut(null);
        try {
            out = new PrintStream("defaultPrintStream.txt");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MinimaxLocationSetCoverAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public MinimaxLocationSetCoverAlgorithm(int numFacs,LocationSetCover lsc){
        this.numFacs=numFacs;
        this.lsc=lsc;
        initialize();
    }

    public void initialize(){
        coverDistLB=lsc.getMaxValueOfKthSmallestWtdDist();
        //System.out.println("initialLB: "+coverDistLB);
        //System.out.println(MatrixUtil.matrixToStringByRow(lsc.getWtdDistMatrix()," "));
        coverDistUB=lsc.getMaxWtdDist();
        //System.out.println("initialUB: "+coverDistUB);
        lsc.initialize();
        valuesToTry=lsc.getListOfCoverThresholds();
    }
    
    public void solve(){
        out.println("start");
        long startTime=System.currentTimeMillis();
        runIterations(coverDistLB, coverDistUB);
        runTime = (.001)*(System.currentTimeMillis()-startTime);
        out.println("end");
    }

    public void runIterations(double coverDistanceLB,double coverDistanceUB){
        double coverDistance = Math.floor((coverDistanceLB+coverDistanceUB)/2);
        lsc.resetCplex();
        lsc.setOut(null);
        Parameters p = lsc.getParameters();
        p.setDouble("coverDist", coverDistance);
        lsc.setParameters(p);
        //lsc.resetCoverConstraints();
        lsc.createModel();
        //System.out.println("cover: "+coverDistance);
        //System.out.println(MatrixUtil.matrixToStringByRow(lsc.getAmatrix(), " "));
        out.println(lsc);
        lsc.solve();
        //centers = lsc.getAssignments();
        double setCoverObjectiveValue = lsc.getResponses().getDoubleResponse("objValue");
        if(lsc.getHasSolved())
            centers = lsc.getAssignments();
        else{
            centers=null;
            //System.out.println(coverDistance);
            //System.out.println(lsc.getMaxValueOfKthSmallestWtdDist());
            //System.out.println(MatrixUtil.matrixToStringByRow(lsc.getAmatrix(), " "));
            //System.out.println(lsc);
            //throw new RuntimeException("infeasible");
        }
        out.println(coverDistanceLB+";"+coverDistanceUB+";"+coverDistance+";"+setCoverObjectiveValue+";"+centers);
        if(lsc.getHasSolved()){
            if(setCoverObjectiveValue<=numFacs){
                coverDistanceUB = coverDistance;
            }
            else{
                coverDistanceLB = coverDistance+1;
            }
        }
        else
            coverDistanceLB = coverDistance+1;
        if(coverDistanceLB!=coverDistanceUB){
            runIterations(coverDistanceLB,coverDistanceUB);
        }
        else{
            //objectiveValue = coverDistanceLB;
            lsc.resetCplex();
            lsc.setOut(null);
            Parameters p2 = lsc.getParameters();
            p2.setDouble("coverDist", coverDistance);
            lsc.setParameters(p2);
            //lsc.resetCoverConstraints();
            lsc.createModel();
            lsc.solve();
            centers = lsc.getAssignments();
            objectiveValue=coverDistanceUB;
            //System.out.println(coverDistance);
            if(centers.size()==0||centers.size()>numFacs){
                lsc.resetCplex();
                lsc.setOut(null);
                Parameters p3 = lsc.getParameters();
                double lastCoverDist = valuesToTry.get(ListUtil.getIndexOfLeastUpperBound(coverDistance, valuesToTry));
                p3.setDouble("coverDist", lastCoverDist);
                lsc.setParameters(p3);
                //System.out.println(coverDistance+"\t"+lastCoverDist);
                lsc.createModel();
                lsc.solve();
                centers = lsc.getAssignments();
                objectiveValue=lastCoverDist;
            }
                
        }
    }
    
    public double getObjectiveValue(){
        return objectiveValue;
    }

    public ArrayList<Integer> getCenters(){
        return centers;
    }

    public void setDataset(DatasetFromFileIfc dataset) {
        lsc.setDataset(dataset);
    }

    @Override
    public Parameters getNewParametersFromString(Parameters paramsObj,String paramsString) {
        String[] params = StringUtils.split(paramsString,"\t");
        paramsObj.setInt("numFacs",Integer.parseInt(params[0]));
        paramsObj.setInt("numCovers",Integer.parseInt(params[1]));
        return paramsObj;
    }

    @Override
    public String getResponsesString() {
        StringBuffer sb = new StringBuffer();
        Responses r = this.getResponses();
        sb.append(r.getDoubleResponse("runTime")+"\t");
        sb.append(r.getDoubleResponse("objValue")+"\t");
        sb.append(r.getIntResponse("numLocatedFacs")+"\t");
        sb.append(r.getStringResponse("locatedFacs")+"\t");
        return sb.toString();
    }

    @Override
    public Parameters getParameters() {
        return new LSCP_Params(this);
    }

    public void setParameters(Parameters parameters) {
        numFacs = parameters.getInt("numFacs");
        lsc.setNumCoversRequired(parameters.getInt("numCovers"));
    }

    public String getName() {
        return "MinimaxLocationSetCoverAlgorithm";
    }

    public long getId() {
        return 0;
    }

    @Override
    public Responses getResponses() {
        Responses r = null;
        r = new FacLocCplexResponses(this);
        r.setStringResponse("locatedFacs", centers + "");
        r.setStringResponse("numLocatedFacs", centers.size() + "");
        r.setDoubleResponse("runTime", runTime);
        r.setDoubleResponse("objValue", objectiveValue);
        return r;
    }

    public static void SwainDataset(){
        PointsDatasetFixedCostRandomFailuresDifferentProbsDataset pd =
                new PointsDatasetFixedCostRandomFailuresDifferentProbsDataset(
                "//ineg-file/shares/NTSCOE_SC_Risk/data/datasetsInPublishedWorks/testing/Swain05.txt");
        int numFacs = 5;
        int numInterdictions = 2;
        LocationSetCover locSetCvr=new LocationSetCover();
        locSetCvr.setOut(null);
        locSetCvr.setDataset(pd);
        Parameters params = locSetCvr.getParameters();
        params.setInt("numCovers",numInterdictions+1);
        locSetCvr.setParameters(params);
        locSetCvr.initialize();
        locSetCvr.createModel();
        MinimaxLocationSetCoverAlgorithm alg = new MinimaxLocationSetCoverAlgorithm(numFacs,locSetCvr);
        alg.solve();
        System.out.println(alg.getObjectiveValue());
        System.out.println(alg.getCenters());
    }

    public void setOut(OutputStream out) {
        this.out=null;
        this.out = new PrintStream(out);
    }

    public void end() {
        
    }

    public void createModel() {
        
    }

    public class LSCP_Params extends Parameters{
        public LSCP_Params(MinimaxLocationSetCoverAlgorithm p){
            super(p);
            this.myParameters.put("numFacs",Integer.toString(MinimaxLocationSetCoverAlgorithm.this.numFacs));
            this.myParameters.put("numCovers",
                    Integer.toString(lsc.getNumCoversRequired()));
        }
    }

    public static void test(){
        FailableDistanceMatrixDataset pd =
                new FailableDistanceMatrixDataset();
        //PointsDatasetFixedCostRandomFailuresDifferentProbsDataset pd =
        //        new PointsDatasetFixedCostRandomFailuresDifferentProbsDataset();
        pd.readFromFile("//ineg-file/shares/NTSCOE_SC_Risk/data/datasetsInPublishedWorks/Alberta05.txt");
        //int numFacs = 5;
        //System.out.println(Arrays.deepToString(pd.getDistanceMatrix()));
        //System.out.println(Arrays.toString(pd.getDemands()));
        int numInterdictions = 5;
        MinimaxLocationSetCoverAlgorithm alg = new MinimaxLocationSetCoverAlgorithm();
        alg.setDataset(pd);
        Parameters params = alg.getParameters();
        params.setInt("numFacs",30);
        params.setInt("numCovers",numInterdictions+1);
        alg.setParameters(params);
        alg.initialize();
        alg.solve();
        System.out.println(alg.getResponsesString());
    }

    public static void main(String[] args){
        ///SwainDataset();
        test();
        //comparisonWithChurchScaparra2007();
    }
}

