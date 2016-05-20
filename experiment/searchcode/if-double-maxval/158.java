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

import ifcSoft.model.DataSetProxy;
import ifcSoft.model.dataSet.DataSet;
import ifcSoft.model.som.jobs.FindMembershipsJob;
import java.awt.Point;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 *
 * @author kthayer
 */
public class SOMRetrieveFns {

  /**
   * Return the SOM map of the given dimension.
   * @param dimension
   * @return
   */
  static public float[][] getMap(int dimension, SOM som){
    SOMNode[][] SOMnodes = som.SOMnodes;
    
    double maxVal = Double.MIN_VALUE;
    double minVal = Double.MAX_VALUE;
    float[][] newMap = new float[SOMnodes.length][SOMnodes[0].length];
    for(int i=0; i < SOMnodes.length; i++){
      for(int j=0; j < SOMnodes[0].length; j++){
        newMap[i][j] = SOMnodes[i][j].getWeight(dimension);
        if(maxVal < newMap[i][j]){
          maxVal = newMap[i][j];
        }
        if(minVal > newMap[i][j]){
          minVal = newMap[i][j];
        }
      }
    }
    if(maxVal == minVal){
      System.out.println("Note: maxVal = minVal (in SOM.getMap)");
      for(int i=0; i < SOMnodes.length; i++){
        for(int j=0; j < SOMnodes[0].length; j++){
          newMap[i][j] = 0;
        }
      }
      return newMap;
    }
    for(int i=0; i < SOMnodes.length; i++){
      for(int j=0; j < SOMnodes[0].length; j++){
        newMap[i][j] = (float) (( newMap[i][j] - minVal ) / (maxVal - minVal));
      }
    }

    return newMap;
  }


  /**
   * Return the SOM UMatrix. (it is normalized with 1 as the max)
   * @return
   */
  static public float[][] getUMap(SOM som){
    //if it hasn't been calculated yet, calculate it
    float [][] normUMap = getRawUMap(som);

    float maxDist = 0;

    for(int i = 0; i < normUMap.length; i++){
      for(int j = 0; j < normUMap[0].length; j++){
        if(normUMap[i][j] > maxDist){
          maxDist= normUMap[i][j];
        }
      }
    }

    //now I need to normalize the UMap so maxDist is 1
    for(int i = 0; i < normUMap.length; i++){
      for(int j = 0; j < normUMap[0].length; j++){
        normUMap[i][j] = normUMap[i][j] / maxDist;
      }
    }
    return normUMap;
  }

  static public float[][] getRawUMap(SOM som){
    //if it hasn't been calculated yet, calculate it
    if(som.UMap != null){
      return som.UMap;
    }

    float maxDist = 0;
    SOMNode[][] SOMnodes = som.SOMnodes;

    som.UMap = new float[SOMnodes.length][SOMnodes[0].length];
    for(int i = 0; i < SOMnodes.length; i++){
      for(int j = 0; j < SOMnodes[0].length; j++){
        //for each node compute the avg distance to neighbors
        LinkedList<Point> neighbors = SOMHelperFns.neighborsAtDistance(new Point(i,j), 1, som);
        float d = 0;
        int numNeighbors = 0;
        while(true){ //while there are neighbors to check
          Point pt;
          try{
            pt = neighbors.removeFirst();
          }catch(NoSuchElementException obj){
            break;
          }
          //update vector
          d +=  som.getDistance(new Point(i,j), pt);
          numNeighbors++;
        }
        //average the distances
        som.UMap[i][j] = d / numNeighbors;
        if(som.UMap[i][j] > maxDist){
          maxDist = som.UMap[i][j];
        }
      }
    }
    return som.UMap;
  }

  /**
   * The U-Edge map is an array [x][y][side] where there are 6 sides for each
   * hexagon (top-right, right, bottom-right, bottom-left, left, top-left)
   * NOTE: I'm thinking of 0,0 as top-left, not sure though if that's what I display
   * @return The U-Edge Map
   */
  static public float[][][] getSOMUEmap(SOM som) {
    if(som.UEmap != null){ //if it already exists, return it
      return som.UEmap;
    }
    if(som.mapType != SOM.HEXSOM){
      //I'm not ready to deal with anything but hex maps
      return null;
    }
    som.UEmap = new float[som.getWidth()][som.getHeight()][6];

    float maxDist = 0; //keep track of the max distance for normalizing

    for(int i = 0; i < som.getWidth(); i++){
      for(int j = 0; j < som.getHeight(); j++){
        Point neighbor = new Point();
        //************ top-right ***********
        neighbor.x = i+1;
        neighbor.y = j-1;
        //if row (y) is even, the row is shifted right logically,
          //so I undo that shift by subtracting 1
        if((neighbor.y / 2) * 2 == neighbor.y)
          neighbor.x -= 1;
        if(SOMHelperFns.isPtValid(neighbor, som)){
          float dist = som.getDistance(new Point(i,j), neighbor);
          som.UEmap[i][j][0] = dist;
          if(dist > maxDist){
            maxDist = dist;
          }
        }else{
          som.UEmap[i][j][0] = -1; //it doesn't have a neighbor on this edge
        }

        //************ right ***********
        neighbor.x = i+1;
        neighbor.y = j;
        if(SOMHelperFns.isPtValid(neighbor, som)){
          float dist = som.getDistance(new Point(i,j), neighbor);
          som.UEmap[i][j][1] = dist;
          if(dist > maxDist){
            maxDist = dist;
          }
        }else{
          som.UEmap[i][j][1] = -1; //it doesn't have a neighbor on this edge
        }

        //************ bottom-right ***********
        neighbor.x = i+1;
        neighbor.y = j+1;
        //if row (y) is even, the row is shifted right logically,
          //so I undo that shift by subtracting 1
        if((neighbor.y / 2) * 2 == neighbor.y)
          neighbor.x -= 1;
        if(SOMHelperFns.isPtValid(neighbor,som)){
          float dist = som.getDistance(new Point(i,j), neighbor);
          som.UEmap[i][j][2] = dist;
          if(dist > maxDist){
            maxDist = dist;
          }
        }else{
          som.UEmap[i][j][2] = -1; //it doesn't have a neighbor on this edge
        }

        //************ bottom-left ***********
        neighbor.x = i;
        neighbor.y = j+1;
        //if row (y) is even, the row is shifted right logically,
          //so I undo that shift by subtracting 1
        if((neighbor.y / 2) * 2 == neighbor.y)
          neighbor.x -= 1;
        if(SOMHelperFns.isPtValid(neighbor, som)){
          float dist = som.getDistance(new Point(i,j), neighbor);
          som.UEmap[i][j][3] = dist;
          if(dist > maxDist){
            maxDist = dist;
          }
        }else{
          som.UEmap[i][j][3] = -1; //it doesn't have a neighbor on this edge
        }

        //************ left ***********
        neighbor.x = i-1;
        neighbor.y = j;
        if(SOMHelperFns.isPtValid(neighbor, som)){
          float dist = som.getDistance(new Point(i,j), neighbor);
          som.UEmap[i][j][4] = dist;
          if(dist > maxDist){
            maxDist = dist;
          }
        }else{
          som.UEmap[i][j][4] = -1; //it doesn't have a neighbor on this edge
        }

        //************ top-left ***********
        neighbor.x = i;
        neighbor.y = j-1;
        //if row (y) is even, the row is shifted right logically,
          //so I undo that shift by subtracting 1
        if((neighbor.y / 2) * 2 == neighbor.y)
          neighbor.x -= 1;
        if(SOMHelperFns.isPtValid(neighbor, som)){
          float dist = som.getDistance(new Point(i,j), neighbor);
          som.UEmap[i][j][5] = dist;
          if(dist > maxDist){
            maxDist = dist;
          }
        }else{
          som.UEmap[i][j][5] = -1; //it doesn't have a neighbor on this edge
        }
      }

    }

    //normalize UEMap
    for(int i = 0; i < som.getWidth(); i++){
      for(int j = 0; j < som.getHeight(); j++){
        for(int k = 0; k < 6; k++){
          if(som.UEmap[i][j][k] != -1){
            som.UEmap[i][j][k] = som.UEmap[i][j][k] / maxDist;
          }
        }
      }
    }

    return som.UEmap;
  }

  static public float[][] getSetDenseMapsDelta(SOM som){
    som.rawSetNames = som.datasetScalar.getRawSetNames();
    float [][] setDenseMapsDelta = new float[som.SOMnodes.length][som.SOMnodes[0].length];

    for(int i = 0; i < setDenseMapsDelta.length; i++){
      for(int j = 0; j < setDenseMapsDelta[0].length; j++){
        setDenseMapsDelta[i][j]=0;
      }
    }

    //I think it starts out clear, but I'll clear it just in case

    for(int i =0; i < som.subsetDenseMaps[0].length; i++){
      for(int j=0; j < som.subsetDenseMaps[0][0].length; j++){
        for(int k = 0; k < som.subsetDenseMaps.length; k++){ //each data set
          //setDenseMapsDelta[i][j]+= Math.pow(1.2, -subsetDenseMaps[k][i][j]);
          //go through neighbors and find the change in density map
          LinkedList<Point> neighbors = SOMHelperFns.neighborsAtDistance(new Point(i, j), 1, som);
          //should be proportional change, but what about 0?
          double delta = 0;
          int numNeighbors = neighbors.size();
          while(!neighbors.isEmpty()){
            Point n = neighbors.removeFirst();
            delta += Math.abs(som.subsetDenseMaps[k][i][j] -som.subsetDenseMaps[k][n.x][n.y]);
          }
          setDenseMapsDelta[i][j] += delta / numNeighbors //change per neighbor
                *Math.pow(1.05, -som.subsetDenseMaps[k][i][j]); //scale it so if this is a lower density node, it is weighted higher

          //neighbors distance 2
          neighbors = SOMHelperFns.neighborsAtDistance(new Point(i, j),2, som);
          //should be proportional change, but what about 0?
          delta = 0;
          numNeighbors = neighbors.size();
          while(!neighbors.isEmpty()){
            Point n = neighbors.removeFirst();
            delta += Math.abs(som.subsetDenseMaps[k][i][j] -som.subsetDenseMaps[k][n.x][n.y]);
          }
          setDenseMapsDelta[i][j] += .5 * delta / numNeighbors //change per neighbor
                *Math.pow(1.05, -som.subsetDenseMaps[k][i][j]); //scale it so if this is a lower density node, it is weighted higher
        }
      }
    }
    return setDenseMapsDelta;
  }


  /**
   * Return the total Hit Histogram (Density Map).
   * @return
   */
  static public synchronized int[][] getDensityMap(SOM som){
    if(som.DenseMap == null){
      return null;
    }
    int[][] newCopy = new int[som.DenseMap.length][som.DenseMap[0].length];
    for(int i = 0; i < som.DenseMap.length; i++){
      System.arraycopy(som.DenseMap[i], 0, newCopy[i], 0, som.DenseMap[0].length);
    }
    //we return a new copy so it isn't being updated as we try to read it
    return newCopy;
  }

  /**
   * Return the density Map of the requested other data set.
   * @param set
   * @return
   */
  static public synchronized int[][] getSubsetDenseMap(int set, SOM som) {
    if(som.subsetDenseMaps == null || som.subsetDenseMaps[set] == null){
      return null;
    }
    //private int[][][] DataSetDenseMaps;
    int[][] newCopy = new int[som.subsetDenseMaps[set].length][som.subsetDenseMaps[set][0].length];
    for(int i = 0; i < som.subsetDenseMaps[set].length; i++){
      System.arraycopy(som.subsetDenseMaps[set][i], 0, newCopy[i], 0, som.subsetDenseMaps[set][0].length);
    }
    //we return a new copy so it isn't being updated as we try to read it
    return newCopy;
  }


  /**
   * Find the density map of the given data set.
   * @param dataSet
   * @param dataSets
   */
  static public synchronized void findDensity(int dataSet, LinkedList<DataSetProxy> dataSets, SOM som){
    som.dataSets = dataSets;

    if(som.dataSetDenseMaps == null){
      som.dataSetDenseMaps = new int[dataSet+1][][];
      som.dataSetDenseMapPlaced = new int[dataSet+1];
    }
    if(dataSet >= som.dataSetDenseMaps.length){
      int [][][] newDataSetDenseMaps = new int[dataSet+1][][];
      System.arraycopy(som.dataSetDenseMaps, 0, newDataSetDenseMaps, 0, som.dataSetDenseMaps.length);
      int newDataSetDenseMapPlaced[] = new int[dataSet+1];
      System.arraycopy(som.dataSetDenseMapPlaced, 0, newDataSetDenseMapPlaced, 0, som.dataSetDenseMapPlaced.length);
      som.dataSetDenseMaps = newDataSetDenseMaps;
      som.dataSetDenseMapPlaced = newDataSetDenseMapPlaced;
    }

    //if it hasn't already been started
    if(som.dataSetDenseMaps[dataSet] == null){
      //initialize denseMap
      som.dataSetDenseMaps[dataSet] = new int[som.SOMnodes.length][som.SOMnodes[0].length];
      for(int i = 0; i < som.dataSetDenseMaps[dataSet].length; i++){
        for(int j = 0; j < som.dataSetDenseMaps[dataSet][0].length; j++){
          som.dataSetDenseMaps[dataSet][i][j] = 0;
        }
      }
      som.dataSetDenseMapPlaced[dataSet] = 0;

      //add job

      DataSet data = som.getDataSet(dataSet);

      int firstpt = 0;
      while(firstpt < data.length()){
        int lastpt = firstpt + 1000 - 1;
        if(lastpt >= data.length()){
          lastpt = data.length() - 1;
        }
        FindMembershipsJob newjob = new FindMembershipsJob(dataSet, som, firstpt, lastpt, som.iscanceled, som.ispaused);
        try {
          som.jobqueue.put(newjob); 
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        firstpt+= 1000;
      }

    }
  }

}

