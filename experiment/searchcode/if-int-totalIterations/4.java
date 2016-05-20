/**
 *  Copyright (C) 2011  Kyle Thayer <kyle.thayer AT gmail.com>
 *
 *  This file is part of the IFCSoft project (http://ifcsoft.com)
 *
 *  IFCSoft is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ifcSoft.model.som;

import ifcSoft.model.dataSet.dataSetScalar.DataSetScalar;
import ifcSoft.model.som.jobs.ComputeBatchSOMJob;
import ifcSoft.model.som.jobs.FindMembershipsJob;
import ifcSoft.model.som.jobs.YieldJob;
import ifcSoft.model.thread.ThreadJob;
import java.awt.Point;
import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.puremvc.java.patterns.facade.Facade;

/**
 *
 * @author kthayer
 */
public class SOMCalcFns {



  static public void calculateSOM(int iterations, SOM som, int maxNeighborSize, int minNeighborSize,
      DataSetScalar SOMScaledData, AtomicBoolean iscanceled,
      BlockingQueue<ThreadJob> jobqueue, Facade facade){




    int numTests = iterations;
    //if(numTests < 2*som.getWidth()*som.getHeight()){
    //  numTests = 2*som.getWidth()*som.getHeight();
    //}
    System.out.println("doing "+ numTests+ "number of iterations");
    double neighborsize = maxNeighborSize;
    double neighborConst = Math.log(neighborsize / minNeighborSize) / Math.log(2);
    Random r = new Random();
    for (int i = 0; i < numTests; i++) {

      neighborsize = (maxNeighborSize * Math.pow(2, -i * neighborConst / numTests));
      //neighborsize = (maxNeighborSize - minNeighborSize)*  ((double)numTests - i )/  (numTests) + minNeighborSize;
      if (neighborsize < 1) { //I'm not sure if this really makes a difference
        neighborsize = 1; //If I let it get too small I get NaN for SOM Nodes
      }


      float[] currentDataWeights = SOMScaledData.getPoint(r.nextInt(SOMScaledData.length()));

      Point bmu = SOMHelperFns.findBMU(currentDataWeights, som);


      //update bmu of it's neighbors
      for (int j = 1; j <= neighborsize; j++) {
        LinkedList<Point> neighbors = SOMHelperFns.neighborsAtDistance(bmu, j, som);
        float alpha = getAlpha(i, numTests, j, neighborsize);
        if (alpha > 1 || alpha < 0) {
          System.out.println("a out of range" + alpha);
        }
        while (true) {
          Point pt;
          try {
            pt = neighbors.removeFirst();
          } catch (NoSuchElementException obj) {
            break;
          }

          //update vector
          som.SOMnodes[pt.x][pt.y].shiftWeights(currentDataWeights, alpha);
        }
      }

      //update Winner seperately
      //updateBMUWeights(som, bmu, i, numTests, neighborsize, currentDataWeights);
      myUpdateBMUWeights(som, bmu, i, numTests, neighborsize, currentDataWeights);

      if (iscanceled.get()) {
        som.SOMnodes = som.OldSOMnodes;
        //reset to old SOM, set the canceled variable to false since we canceled
        //som.setCanceled(false);
        System.out.println("som canceled");
        return;
      }
      som.setProgress(((float) i) / numTests);
    }
    //when I'm really done, set it to 100 rather than a fraction of 1 just to be clear
    som.setProgress(100);
    som.clearUMaps(); //so that we don't try to load old, outdated Umaps

    //add jobs to find membership

    som.initDenseMap();  //first initialize everything
    som.initMemberArray(SOMScaledData.length());
    som.initSetDenseMaps();

    //find umap error
    /*float [][] umap = SOMRetrieveFns.getRawUMap(som);
    double umaperror = 0;
    for(int i = 0; i < umap.length; i++){
      for(int j = 0; j < umap[0].length; j++){
        umaperror += umap[i][j];
      }
    }
    //do nearest neighbor test
    int nearestNeighborFails = doNearestNeighborTest(som);
    facade.sendNotification(ifcSoft.ApplicationFacade.STRINGALERT,
        "umap error: "+umaperror + " NN Fails: "+ nearestNeighborFails,
        null);*/


    //add a few Yield Jobs first so GUI can update
    for(int i = 0; i < 4; i++){
      YieldJob newjob = new YieldJob();
      try {
        jobqueue.put(newjob);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    //add the jobs to find all the memberships
    int firstpt = 0;
    while(firstpt < SOMScaledData.length()){
      int lastpt = firstpt + 1000 - 1;
      if(lastpt >= SOMScaledData.length()){
        lastpt = SOMScaledData.length() - 1;
      }
      FindMembershipsJob newjob = new FindMembershipsJob(-1, som, firstpt, lastpt, iscanceled, som.ispaused);
      try {
        jobqueue.put(newjob);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      firstpt+= 1000;
    }

  }



  static public void calculateBatchSOM(ComputeBatchSOMJob job){
    SOM som = job.som;
    DataSetScalar SOMScaledData = job.SOMdata;


    for(int iter = 0; iter < job.iterations; iter++){
      //making a 2d array of linked lists is a pain because of generics
      LinkedList<float[]>[][] nodeWeightPlaced =  (LinkedList<float[]>[][]) Array.newInstance(LinkedList[].class, job.som.SOMnodes.length);
      for(int i = 0; i < nodeWeightPlaced.length; i++){
        nodeWeightPlaced[i] = (LinkedList<float[]>[]) Array.newInstance(LinkedList.class, job.som.SOMnodes[0].length);
      }

      //System.out.println("nodeWeightPlaced.length = "+ nodeWeightPlaced.length + " nodeWeightPlaced[0].length = "+ nodeWeightPlaced[0].length);
      
      for(int i = 0; i < nodeWeightPlaced.length; i++){
        for(int j = 0; j < nodeWeightPlaced[0].length; j++){
          nodeWeightPlaced[i][j] = new LinkedList<float[]>();
        }
      }
      
      //now place the right number of data points
      int neighborsize = (int)(((job.iterations -iter) / (float) job.iterations) *
              (job.maxNeighborSize-job.minNeighborSize)  + job.minNeighborSize);

      int numInNeighbor = (int) (1 + 6*(neighborsize*(neighborsize + 1) /2.0));

      int dataptstoplace = job.ptsperNode *nodeWeightPlaced.length * nodeWeightPlaced[0].length / numInNeighbor;

      System.out.println(" NSize: "+neighborsize + " NumDataPts: "+ dataptstoplace);

      //place the data points
      if(job.SOMdata.length() <= dataptstoplace){ //if I have less than this number of points, place all
        for(int i = 0; i < job.SOMdata.length(); i++){
          float[] vector = job.SOMdata.getPoint(i);
          Point bmu = SOMHelperFns.findBMU(vector, job.som);
          nodeWeightPlaced[bmu.x][bmu.y].add(vector);
        }
      }else{
        for(int i = 0; i < dataptstoplace; i++){
          int randindex =(int) (Math.random() * (job.SOMdata.length()-1));
          float[] vector = job.SOMdata.getPoint(randindex);
          Point bmu = SOMHelperFns.findBMU(vector, job.som);
          nodeWeightPlaced[bmu.x][bmu.y].add(vector);
        }
      }

      //update the SOM Nodes to be avg of weights placed in neighborsize
      for(int i = 0; i < nodeWeightPlaced.length; i++){
        for(int j = 0; j < nodeWeightPlaced[0].length; j++){
          int[] numPtsPerDim = new int[job.SOMdata.getDimensions()];
          for(int k = 0; k < numPtsPerDim.length; k++){
            numPtsPerDim[k] = 0;
          }
          double[] avgWeights = new double[job.SOMdata.getDimensions()];
          double vectorsplaced[] = new double[job.SOMdata.getDimensions()];
          for(int k = 0; k < numPtsPerDim.length; k++){
            vectorsplaced[k] = 0;
          }
          Point currentpt = new Point(i, j);
          //go through all in neighborhoodsize putting in all the weights
          for(int nsize = 0; nsize <= neighborsize; nsize++){
            double factor = ((double)neighborsize +1 - nsize)/(neighborsize+1); //1 for nsize 0, 0 for nsize =  neighborsize +1
            //double factor = Math.pow(Math.pow(.2, 1.0 / neighborsize), nsize); //starts at 0, goes to .2
            LinkedList<Point> neighbors = SOMHelperFns.neighborsAtDistance(currentpt, nsize, job.som);
            while(neighbors.size() > 0){
              Point nbr = neighbors.removeFirst();
              LinkedList<float[]> vectors = nodeWeightPlaced[nbr.x][nbr.y];
              for(int m = 0; m < vectors.size(); m++){
                float[] vector = vectors.get(m);
                for(int k = 0; k < vector.length; k++){
                  if(!Float.isNaN(vector[k])){
                    avgWeights[k] += vector[k] * factor;
                    numPtsPerDim[k]++;
                    vectorsplaced[k] += factor;
                  }
                }
                
              }
            }
          }
          float newWeights[] = new float[avgWeights.length];
          for(int k=0; k < vectorsplaced.length; k++){
            if(vectorsplaced[k] > 0){
              //update the node with the new weight
              newWeights[k] = (float) (avgWeights[k] / vectorsplaced[k]);
            }else{ //if no new info, keep old weight
              newWeights[k] = job.som.SOMnodes[i][j].getWeights()[k];
            }
          }
          job.som.SOMnodes[i][j].setWeights(newWeights);
        }
      }

      if (job.iscanceled.get()) {
        job.som.SOMnodes = job.som.OldSOMnodes;
        //reset to old SOM, set the canceled variable to false since we canceled
        //som.setCanceled(false);
        System.out.println("som canceled");
        return;
      }
      job.som.setProgress(((float) iter) / job.iterations);

    }

    //when I'm really done, set it to 100 rather than a fraction of 1 just to be clear
    som.setProgress(100);
    som.clearUMaps(); //so that we don't try to load old, outdated Umaps

    //add jobs to find membership

    som.initDenseMap();  //first initialize everything
    som.initMemberArray(SOMScaledData.length());
    som.initSetDenseMaps();

    //find umap error
    /*float [][] umap = SOMRetrieveFns.getRawUMap(som);
    double umaperror = 0;
    for(int i = 0; i < umap.length; i++){
      for(int j = 0; j < umap[0].length; j++){
        umaperror += umap[i][j];
      }
    }
    //do nearest neighbor test
    int nearestNeighborFails = doNearestNeighborTest(som);
    som.facade.sendNotification(ifcSoft.ApplicationFacade.STRINGALERT,
        "umap error: "+umaperror + " NN Fails: "+ nearestNeighborFails,
        null);*/


    //add a few Yield Jobs first so GUI can update
    for(int i = 0; i < 4; i++){
      YieldJob newjob = new YieldJob();
      try {
        som.jobqueue.put(newjob);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    //add the jobs to find all the memberships
    int firstpt = 0;
    while(firstpt < SOMScaledData.length()){
      int lastpt = firstpt + 1000 - 1;
      if(lastpt >= SOMScaledData.length()){
        lastpt = SOMScaledData.length() - 1;
      }
      FindMembershipsJob newjob = new FindMembershipsJob(-1, som, firstpt, lastpt, som.iscanceled, som.ispaused);
      try {
        som.jobqueue.put(newjob);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      firstpt+= 1000;
    }


  }

  static private int doNearestNeighborTest(SOM som){
    int numFails = 0;
    for(int i = 0; i < som.getWidth(); i++){
      for(int j = 0; j < som.getHeight(); j++){

        float nodeWeights[] = som.SOMnodes[i][j].getWeights();
        Point[] nodeBMU = SOMHelperFns.findSecondBMU(new Point(i, j), som);
        LinkedList<Point> neighbors = SOMHelperFns.neighborsAtDistance(new Point(i, j), 1, som); //get the neighbors
        boolean foundBMUinNeighbors = false;
        for(int k = 0; k < neighbors.size(); k++){
          if(neighbors.get(k).x == nodeBMU[0].x
              && neighbors.get(k).y == nodeBMU[0].y ){
            foundBMUinNeighbors = true;
          }
        }
        boolean foundSecondBMUinNeighbors = false;
        for(int k = 0; k < neighbors.size(); k++){
          if(neighbors.get(k).x == nodeBMU[1].x
              && neighbors.get(k).y == nodeBMU[1].y ){
            foundSecondBMUinNeighbors = true;
          }
        }
        if(!foundBMUinNeighbors || !foundSecondBMUinNeighbors){
          numFails++;
        }
      }
    }
    return numFails;
  }

  /**
   * The true winner gets updated differently than the neighbors (it is made to somewhat average them).
   * @param som
   * @param bmu
   * @param iteration
   * @param numTests
   * @param neighborsize
   * @param testWeight
   */
  static private void updateBMUWeights(SOM som, Point bmu, int iteration, int numTests,
    double neighborsize, float[] testWeight) {
    //mc(t+1) = mc(t) + a0(t)*(x(t)-mc(t)) - a2/2* Sum of Neighbors [x(t) - mk(t)]
        //formula from SOM book 3.48
    float alpha0 = getAlpha(iteration, numTests, 0, neighborsize);

    float alpha1 = getAlpha(iteration, numTests, 1, neighborsize);
    SOMNode winner = som.SOMnodes[bmu.x][bmu.y];
    float[] winnerWeights = winner.getWeights();
    float[] newWeights = new float[winnerWeights.length];
    
    System.arraycopy(winnerWeights, 0, newWeights, 0, winnerWeights.length);

    //+ a0(t) * (x(t) - mc(t))
    for(int k = 0; k < winnerWeights.length; k++){
      newWeights[k] += alpha0 * (testWeight[k] - winnerWeights[k]);
    }

    // - a2/2* Sum of Neighbors [x(t) - mk(t)]
    LinkedList<Point> neighbors = SOMHelperFns.neighborsAtDistance(bmu , 1, som);
    while(true){
      Point pt;
      try{
        pt = neighbors.removeFirst();
      }catch(NoSuchElementException obj){
        break;
      }
      SOMNode neighbor = som.SOMnodes[pt.x][pt.y];//.getWeight(k)
      //update vector
      for(int k = 0; k < winnerWeights.length; k++){
        newWeights[k] -= alpha1 /2 * (testWeight[k] - neighbor.getWeight(k));
      }

    }
  }

  /**
   * The true winner gets updated differently than the neighbors (it is made to somewhat average them).
   * This uses what I think will be a better way of doing it.
   * @param som
   * @param bmu
   * @param iteration
   * @param numTests
   * @param neighborsize
   * @param testWeight
   */
  static private void myUpdateBMUWeights(SOM som, Point bmu, int iteration, int numTests,
      //Rather let's try alpha adding new point, then alpha shifting to neighbor avg.

    double neighborsize, float[] testWeight) {
    //my formula
    //mc(t+1) = (1 - alpha0) * mc(t) + alpha0* (1/2 avg of neighbors + 1/2 x(t))
    float alpha = getAlpha(iteration, numTests, 0, neighborsize);

    SOMNode winner = som.SOMnodes[bmu.x][bmu.y];
    float[] winnerWeights = winner.getWeights();
    float[] newWeights = new float[winnerWeights.length];
    //mc(t+1) = (1 - alpha0) * mc(t)
    for(int k = 0; k < winnerWeights.length; k++){
      newWeights[k] = (1 - alpha) * winnerWeights[k];
    }
    //TODO: using neighborsAtDistance is unnecesarily slow, and depends on it being a hex map
    if(SOMHelperFns.neighborsAtDistance(bmu,1, som).size() < 6){ //if it is a border point, don't average it with others
      for(int k = 0; k < winnerWeights.length; k++){
        newWeights[k] += alpha * testWeight[k];
      }
    }else{
      //+ alpha* 1/2 x(t)
      for(int k = 0; k < winnerWeights.length; k++){
        newWeights[k] += alpha * .5* testWeight[k];
      }

      // + alpha* (1/2 avg of neighbors)
      LinkedList<Point> neighbors = SOMHelperFns.neighborsAtDistance(bmu , 1, som);
      int numNeighbors = neighbors.size();
      while(true){
        Point pt;
        try{
          pt = neighbors.removeFirst();
        }catch(NoSuchElementException obj){
          break;
        }
        SOMNode neighbor = som.SOMnodes[pt.x][pt.y];//.getWeight(k)
        //update vector
        for(int k = 0; k < winnerWeights.length; k++){
          newWeights[k] += alpha *.5 * neighbor.getWeight(k) / numNeighbors ;
        }

      }
    }
  }


  /**
   * Gets the alpha value (amount the SOM node will be pulled to the new value)
   * @param iteration
   * @param totalIterations
   * @param dist
   * @param neighborhood
   * @return
   */
  public static float getAlpha(int iteration, int totalIterations, int dist, double neighborhood){
    //As an attempt to optimize tha map, for iteractions, make 1st 10 % be 100, then do 2^(-iter*5/total)
    int firstTenthiter = totalIterations / 10;
    iteration -= firstTenthiter;
    totalIterations -= firstTenthiter;
    if(iteration < 0){
      iteration = 0;
    }
    //float alpha = (float) (Math.pow(2, - iteration *5.0 / totalIterations) * Math.sqrt(1 -  dist*dist / n / n));
    float alpha = (float)(.5*Math.pow(2, - iteration *6.0 / totalIterations) * Math.pow(2, - dist*dist / neighborhood / neighborhood));
    return alpha;

    /*original*/
    //float alpha = (float) (Math.pow(2, - iteration *3.0 / totalIterations) * Math.pow(2, - 4.0*dist*dist / neighborhood / neighborhood));
    //return alpha;
  }
}

