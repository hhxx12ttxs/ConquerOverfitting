<<<<<<< HEAD
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mahout.clustering.meanshift;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import org.apache.hadoop.conf.Configuration;
import org.apache.mahout.common.ClassUtils;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.kernel.IKernelProfile;
import org.apache.mahout.math.Vector;

public class MeanShiftCanopyClusterer {

  private final double convergenceDelta;

  // the T1 distance threshold
  private final double t1;

  // the T2 distance threshold
  private final double t2;

  // the distance measure
  private final DistanceMeasure measure;

  private final IKernelProfile kernelProfile;

  // if true accumulate clusters during merge so clusters can be produced later
  private final boolean runClustering;

  public MeanShiftCanopyClusterer(Configuration configuration) {
    measure = ClassUtils.instantiateAs(configuration.get(MeanShiftCanopyConfigKeys.DISTANCE_MEASURE_KEY), 
                                       DistanceMeasure.class);
    measure.configure(configuration);
    runClustering = configuration.getBoolean(MeanShiftCanopyConfigKeys.CLUSTER_POINTS_KEY, true);
    kernelProfile = ClassUtils.instantiateAs(configuration.get(MeanShiftCanopyConfigKeys.KERNEL_PROFILE_KEY),
                                             IKernelProfile.class);
    // nextCanopyId = 0; // never read?
    t1 = Double
        .parseDouble(configuration.get(MeanShiftCanopyConfigKeys.T1_KEY));
    t2 = Double
        .parseDouble(configuration.get(MeanShiftCanopyConfigKeys.T2_KEY));
    convergenceDelta = Double.parseDouble(configuration
        .get(MeanShiftCanopyConfigKeys.CLUSTER_CONVERGENCE_KEY));
  }

  public MeanShiftCanopyClusterer(DistanceMeasure aMeasure,
      IKernelProfile aKernelProfileDerivative, double aT1, double aT2,
      double aDelta, boolean runClustering) {
    // nextCanopyId = 100; // so canopyIds will sort properly // never read?
    measure = aMeasure;
    t1 = aT1;
    t2 = aT2;
    convergenceDelta = aDelta;
    kernelProfile = aKernelProfileDerivative;
    this.runClustering = runClustering;
  }

  public double getT1() {
    return t1;
  }

  public double getT2() {
    return t2;
  }

  /**
   * Merge the given canopy into the canopies list. If it touches any existing
   * canopy (norm<T1) then add the center of each to the other. If it covers any
   * other canopies (norm<T2), then merge the given canopy with the closest
   * covering canopy. If the given canopy does not cover any other canopies, add
   * it to the canopies list.
   * 
   * @param aCanopy
   *          a MeanShiftCanopy to be merged
   * @param canopies
   *          the List<Canopy> to be appended
   */
  public void mergeCanopy(MeanShiftCanopy aCanopy,
      Collection<MeanShiftCanopy> canopies) {
    MeanShiftCanopy closestCoveringCanopy = null;
    double closestNorm = Double.MAX_VALUE;
    for (MeanShiftCanopy canopy : canopies) {
      double norm = measure.distance(canopy.getCenter(), aCanopy.getCenter());
      double weight = kernelProfile.calculateDerivativeValue(norm, t1);
      if (weight > 0.0) {
        aCanopy.touch(canopy, weight);
      }
      if (norm < t2 && (closestCoveringCanopy == null || norm < closestNorm)) {
        closestNorm = norm;
        closestCoveringCanopy = canopy;
      }
    }
    if (closestCoveringCanopy == null) {
      canopies.add(aCanopy);
    } else {
      closestCoveringCanopy.merge(aCanopy, runClustering);
    }
  }

  /**
   * Shift the center to the new centroid of the cluster
   * 
   * @param canopy
   *          the canopy to shift.
   * @return if the cluster is converged
   */
  public boolean shiftToMean(MeanShiftCanopy canopy) {
    canopy.observe(canopy.getCenter(), canopy.getMass());
    canopy.computeConvergence(measure, convergenceDelta);
    canopy.computeParameters();
    return canopy.isConverged();
  }

  /**
   * Return if the point is covered by this canopy
   * 
   * @param canopy
   *          a canopy.
   * @param point
   *          a Vector point
   * @return if the point is covered
   */
  boolean covers(MeanShiftCanopy canopy, Vector point) {
    return measure.distance(canopy.getCenter(), point) < t1;
  }

  /**
   * Return if the point is closely covered by the canopy
   * 
   * @param canopy
   *          a canopy.
   * @param point
   *          a Vector point
   * @return if the point is covered
   */
  public boolean closelyBound(MeanShiftCanopy canopy, Vector point) {
    return measure.distance(canopy.getCenter(), point) < t2;
  }

  /**
   * This is the reference mean-shift implementation. Given its inputs it
   * iterates over the points and clusters until their centers converge or until
   * the maximum number of iterations is exceeded.
   * 
   * @param points
   *          the input List<Vector> of points
   * @param measure
   *          the DistanceMeasure to use
   * @param numIter
   *          the maximum number of iterations
   */
  public static List<MeanShiftCanopy> clusterPoints(Iterable<Vector> points,
      DistanceMeasure measure, IKernelProfile aKernelProfileDerivative,
      double convergenceThreshold, double t1, double t2, int numIter) {
    MeanShiftCanopyClusterer clusterer = new MeanShiftCanopyClusterer(measure,
        aKernelProfileDerivative, t1, t2, convergenceThreshold, true);
    int nextCanopyId = 0;

    List<MeanShiftCanopy> canopies = Lists.newArrayList();
    for (Vector point : points) {
      clusterer.mergeCanopy(
          new MeanShiftCanopy(point, nextCanopyId++, measure), canopies);
    }
    List<MeanShiftCanopy> newCanopies = canopies;
    boolean[] converged = { false };
    for (int iter = 0; !converged[0] && iter < numIter; iter++) {
      newCanopies = clusterer.iterate(newCanopies, converged);
    }
    return newCanopies;
  }

  protected List<MeanShiftCanopy> iterate(Iterable<MeanShiftCanopy> canopies,
      boolean[] converged) {
    converged[0] = true;
    List<MeanShiftCanopy> migratedCanopies = Lists.newArrayList();
    for (MeanShiftCanopy canopy : canopies) {
      converged[0] = shiftToMean(canopy) && converged[0];
      mergeCanopy(canopy, migratedCanopies);
    }
    return migratedCanopies;
  }

  protected static MeanShiftCanopy findCoveringCanopy(MeanShiftCanopy canopy,
      Iterable<MeanShiftCanopy> clusters) {
    // canopies use canopyIds assigned when input vectors are processed as
    // vectorIds too
    int vectorId = canopy.getId();
    for (MeanShiftCanopy msc : clusters) {
      for (int containedId : msc.getBoundPoints().toList()) {
        if (vectorId == containedId) {
          return msc;
        }
      }
    }
    return null;
  }
=======
package src;

import java.util.ArrayList;
import java.util.Random;

public class kmeans {
	static String filename;
	static int k;

	// 1 = random
	// 2 = SelectCentroids
	static int initCentMethod = 2;

	static int centRecalcMethod;

	// 1 = # of reassigned pointeres
	// 2 = total distance change of all centroids
	// 3 = decrease of SSE
	static int stopCriteria = 3;

	static int reassignMinimum = 4;
	static double clustoidDistMinimum = 1.0;
	static double SSEDecreaseMinimum = 5;

	public static void main(String[] args) {
		// System.exit(0);
		// java kmeans <Filename> <k>
		if (args.length == 2) {
			filename = args[0];
			k = Integer.parseInt(args[1]);
		} else {
			System.err.println("Use: java kmeans <Filename> <k>");
			System.exit(1);
		}
		// read in the file and extract the needed information
		Csv csvInfo = new Csv(filename);
		ArrayList<ArrayList<Double>> dataPoints = new ArrayList<ArrayList<Double>>(
				csvInfo.datas);
		ArrayList<String> stringLists = csvInfo.strings;
		ArrayList<Integer> restrictions = csvInfo.restrictions;

		// remove the restricted columns
		for (int i = restrictions.size() - 1; i >= 0; i--) {
			// for each dataPoints
			for (int j = 0; j < dataPoints.size(); j++) {
				if (restrictions.get(i) != 1) {
					dataPoints.get(j).remove(i);
					csvInfo.datas.get(j).remove(i);
				}
			}
			if (restrictions.get(i) != 1) {
				restrictions.remove(i);
				csvInfo.restrictions.remove(i);
			}
		}

		System.out.println("Restrictions:");
		System.out.println(restrictions);
		System.out.println("strings:");
		for (int i = 0; i < stringLists.size(); i++) {
			System.out.println(stringLists.get(i));
		}
		System.out.println("Datas:(" + dataPoints.size() + ")");
		for (int i = 0; i < dataPoints.size(); i++) {
			System.out.println(dataPoints.get(i));
		}

		ArrayList<Centroid> cent;
		// select initial centroid
		if (initCentMethod == 1) {
			cent = SIC_Random(dataPoints, k);
		} else {
			cent = SIC_SelectCentroids(dataPoints, k);
		}

		// this becomes false when the main loop should be over
		boolean keepOnGoing = true;
		// keeps track of SSE to see if main loops should be over
		double SSEVal = 0;
		double SSEBefore = 0;
		
		// while we should keep on going
		while (keepOnGoing) {
			// reset the centroids: clear all datapoints, and changed to false
			for (int i = 0; i < cent.size(); i++) {
				cent.get(i).reset();
			}

			// for each data points
			for (int idx = 0; idx < dataPoints.size(); idx++) {
				// find the nearest centroid
				int indexOfCent = nearestCentroid(dataPoints.get(idx), cent);
				// put the data point into the centroid
				cent.get(indexOfCent).add(dataPoints.get(idx));
			}

			// recalcuate the centroid positions for each centroids
			for (int i = 0; i < cent.size(); i++) {
				cent.get(i).calcCenter_Mean();
				System.out.println(i + ":" + cent.get(i));
			}
			
			// check if we should stop:-------------------------

			// reassignement of data pointers
			int reassign = 0;
			for (int i = 0; i < cent.size(); i++) {
				reassign += Math.abs(cent.get(i).getLR() - cent.get(i).getRE());
			}
			System.out.println("reassign=" + reassign);
			if ((stopCriteria == 1) && reassign >= -1
					&& reassign <= reassignMinimum) {
				keepOnGoing = false;
			}
			// change in clustoids position
			double distanceChanged = 0.0;
			for (int i = 0; i < cent.size(); i++) {
				distanceChanged += cent.get(i).distChange();
			}
			System.out.println("distanceChanged=" + distanceChanged);
			if ((stopCriteria == 2) && distanceChanged <= clustoidDistMinimum) {
				keepOnGoing = false;
			}
			// calc current SSE
			SSEBefore = SSEVal;
			SSEVal = 0;
			//for each cent
			for(int centIdx = 0; centIdx < cent.size(); centIdx++){
				cent.get(centIdx).calcSSE();
				SSEVal += cent.get(centIdx).getSSE();
			}
			
			// change in clustoids position
			double decreseSSE = Math.abs(SSEBefore - SSEVal);
			System.out.println("decreseSSE=" + decreseSSE);
			if ((stopCriteria == 3) && decreseSSE <= SSEDecreaseMinimum) {
				keepOnGoing = false;
			}
		}

		System.out.println("===============================");
		// print out and evaluate the centroids
		for (int i = 0; i < cent.size(); i++) {
			System.out.println("Cluster " + i + ": ");
			System.out.println("Center: " + cent.get(i).getPos());
			// calc min and max distance from the centroid
			double maxDistValue = pointDistance(cent.get(i).getDP().get(0),
					cent.get(i).getPos());
			double minDistValue = pointDistance(cent.get(i).getDP().get(0),
					cent.get(i).getPos());
			double averageDistValue = 0;
			int maxDistIndex = 0;
			int minDistIndex = 0;
			for (int tempDistIndex = 0; 
					tempDistIndex < cent.get(i).getDP().size(); 
					tempDistIndex++) {
				double tempDistValue = pointDistance(
						cent.get(i).getDP().get(tempDistIndex), cent.get(i)
								.getPos());
				if (maxDistValue < tempDistValue) {
					maxDistValue = tempDistValue;
					maxDistIndex = tempDistIndex;
				}
				if (minDistValue > tempDistValue) {
					minDistValue = tempDistValue;
					minDistIndex = tempDistIndex;
				}
				averageDistValue += tempDistValue;
			}
			averageDistValue /= cent.get(i).getDP().size();
			System.out.println("Max Dist. to Center: " + maxDistValue);
			System.out.println("Min Dist. to Center: " + minDistValue);
			System.out.println("Avg Dist. to Center: " + averageDistValue);

			System.out.println(cent.get(i).getDP().size() + " Points:");
			for (int DPIndex = 0; DPIndex < cent.get(i).getDP().size(); DPIndex++) {
				System.out.println(cent.get(i).getDP().get(DPIndex));
			}
			System.out.println("");

		}
		
		double totalSSE = 0;
		for (int centIdx = 0; centIdx < cent.size(); centIdx++) {
			totalSSE += cent.get(centIdx).getSSE();
		}
		System.out.println("TotalSSE Value="+totalSSE);
		
		// if it's 2d data, let's plot it.
		if (restrictions.size() == 2) {
			System.out.println("2D data detected, plotting...");
			ArrayList<ArrayList<ArrayList<Double>>> ALLL = new ArrayList<ArrayList<ArrayList<Double>>>();
			for (int i = 0; i < cent.size(); i++) {
				ALLL.add(cent.get(i).getDP());
			}
			new ScatterPlot(ALLL);
		}
	}

	// select initial centroid randomly
	private static ArrayList<Centroid> SIC_Random(
			ArrayList<ArrayList<Double>> dataPoints, int k) {
		ArrayList<Centroid> answer = new ArrayList<Centroid>();
		Random rand = new Random();
		for (int i = 0; i < k; i++) {
			int randNum = Math.abs(rand.nextInt());
			int index = randNum % dataPoints.size();
			//System.out.println("index=" + index);
			Centroid tempCent = new Centroid();
			tempCent.add(new ArrayList<Double>(dataPoints.get(index)));
			tempCent.calcCenter_Mean();
			answer.add(new Centroid(tempCent));
			answer.get(i).calcCenter_Mean();
		}
		System.out.println("dataPoints.size()=" + dataPoints.size());
		return answer;
	}

	// select initial centroid using the select centroids algorithm
	private static ArrayList<Centroid> SIC_SelectCentroids(
			ArrayList<ArrayList<Double>> dataPoints, int k) {
		ArrayList<Centroid> answer = new ArrayList<Centroid>();
		
		//calculate the center of the whole dataPoints
		ArrayList<Double> center = centerOfPoints(dataPoints);
		//System.out.println("center="+center);
		
		//find the furtest one, that's the first centroid
		int furthestIndex = furthestPoint(center,dataPoints);
		
		//keep track of used ones
		int [] usedCentroids = new int[k];
		usedCentroids[0] = furthestIndex;
		
		//adding the first centroid
		Centroid tempCent = new Centroid();
		tempCent.add(dataPoints.get(furthestIndex));
		tempCent.calcCenter_Mean();
		tempCent.setDP(new ArrayList<ArrayList<Double>>());
		System.out.println("0:Pos "+tempCent.getPos());		
		answer.add(new Centroid(tempCent));
		
		//keep track of all the points that was used for centroid
		
		//for number of k-1
		for(int centCount = 1; centCount < k; centCount++){
			double maxDistValue = pointDistance(dataPoints.get(0), answer.get(0).getPos());
			int maxDistIndex = 0;
			//System.out.println("answer.size()= "+answer.size());
			//for each datapoints
			for(int currDP = 0; currDP < dataPoints.size(); currDP++){
				//check if this value was already used by looking up the array of used dataPoint indexes
				boolean alreadyUsed = false;
				for(int i = 0; i < usedCentroids.length; i++){
					if(currDP == usedCentroids[i]){
						alreadyUsed = true;
					}
				}
				if(alreadyUsed){
					//System.out.println(dataPoints.get(currDP)+" is already used");
					continue;
				}
				
				double tempDistValue = 0;
				//for each centroid already there
				for(int currCent = 0; currCent < answer.size(); currCent++){
					//add the distance from curr datapoint to curr centroid
					double pointDist = pointDistance(dataPoints.get(currDP), answer.get(currCent).getPos());
					//System.out.println("    pointDist (vs "+currCent+")= "+pointDist+"| DP"+dataPoints.get(currDP)+"| cent"+answer.get(currCent).getPos());
					tempDistValue += pointDist;
				}
				//System.out.println("  tempDistValue= "+tempDistValue);
				
				if(tempDistValue > maxDistValue){
					maxDistValue = tempDistValue;
					maxDistIndex = currDP;
				}
			}
			//System.out.println(centCount+":MaxVal "+maxDistValue);
			
			//set this index of datapoint as used to create centroid
			usedCentroids[centCount] = maxDistIndex;
				
			//create the centroid and add it to the list
			tempCent = new Centroid();
			tempCent.add(dataPoints.get(maxDistIndex));
			tempCent.calcCenter_Mean();
			tempCent.setDP(new ArrayList<ArrayList<Double>>());
			System.out.println(centCount+":Pos "+tempCent.getPos());
			answer.add(new Centroid(tempCent));
		}
		  

		return answer;
	}

	// given a data point and list of centroids, return the index of the nearest
	// centroid
	private static int nearestCentroid(ArrayList<Double> dataPoint,
			ArrayList<Centroid> cent) {
		if (cent.size() == 0) {
			System.err.println("No list of centroids found!");
		}
		int index = 0;
		double minDist = pointDistance(dataPoint, cent.get(0).getPos());
		for (int centIdx = 1; centIdx < cent.size(); centIdx++) {
			double currDist = pointDistance(dataPoint, cent.get(centIdx)
					.getPos());
			if (currDist < minDist) {
				index = centIdx;
				minDist = currDist;
			}
		}
		return index;
	}

	public static int nearestPoint(ArrayList<Double> point,
			ArrayList<ArrayList<Double>> dataPoints) {
		if (dataPoints.size() == 0) {
			System.err.println("No list of points found!");
		}
		int index = 0;
		double minDist = pointDistance(point, dataPoints.get(0));
		for (int dpIndex = 1; dpIndex < dataPoints.size(); dpIndex++) {
			double currDist = pointDistance(point, dataPoints.get(dpIndex));
			if (currDist < minDist) {
				index = dpIndex;
				minDist = currDist;
			}
		}
		return index;
	}

	public static int furthestPoint(ArrayList<Double> point,
			ArrayList<ArrayList<Double>> dataPoints) {
		if (dataPoints.size() == 0) {
			System.err.println("No list of points found!");
		}
		int index = 0;
		double maxDist = pointDistance(point, dataPoints.get(0));
		for (int dpIndex = 1; dpIndex < dataPoints.size(); dpIndex++) {
			double currDist = pointDistance(point, dataPoints.get(dpIndex));
			if (currDist > maxDist) {
				index = dpIndex;
				maxDist = currDist;
			}
		}
		return index;
	}

	public static ArrayList<Double> centerOfPoints(
			ArrayList<ArrayList<Double>> dataPoints) {
		if (dataPoints.size() == 0) {
			System.err
					.println("Trying to calculate the center for empty list of points!");
		}
		ArrayList<Double> answer = new ArrayList<Double>();

		// for each entry in a point
		for (int pointIndex = 0; pointIndex < dataPoints.get(0).size(); pointIndex++) {
			double tempSum = 0;
			// for each dataPoints
			for (int DPIndex = 0; DPIndex < dataPoints.size(); DPIndex++) {
				tempSum += dataPoints.get(DPIndex).get(pointIndex);
			}
			tempSum /= (double) dataPoints.size();
			answer.add(tempSum);
		}
		return answer;
	}

	public static double pointDistance(ArrayList<Double> pt1,
			ArrayList<Double> pt2) {
		if (pt1.size() != pt2.size()) {
			System.err.println("Vector size does not match!");
			System.err.println("pt1 size=" + pt1.size() + "|pt2 size="
					+ pt2.size());
			//return 0;
		}

		double result = 0.0;
		for (int i = 0; i < pt1.size(); i++) {
			result += Math.pow(pt1.get(i) - pt2.get(i), 2);
		}
		result = Math.sqrt(result);
		return result;
	}

>>>>>>> 76aa07461566a5976980e6696204781271955163

}

