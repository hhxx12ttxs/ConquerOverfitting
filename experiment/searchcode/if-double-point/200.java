<<<<<<< HEAD
package org.gpsanonymity.merge;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.gpsanonymity.data.MergedWayPoint;
import org.gpsanonymity.data.comparator.ReferenceWayPointComparator;
import org.gpsanonymity.io.IOFunctions;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.ImmutableGpxTrack;
import org.openstreetmap.josm.data.gpx.ImmutableGpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;


public class MergeGPS {
	static public List<WayPoint> mergeWaypoints(List<WayPoint> givenWaypoints, double accuracy, int grade){
		return eliminateLowerGrades(mergeWaypoints(givenWaypoints, accuracy),grade);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<WayPoint> eliminateLowerGrades(
			List<MergedWayPoint> mergeWaypoints
			,int grade){
		return (List<WayPoint>)(List)eliminateLowerGradesMerged(mergeWaypoints, grade);
	}
	public static List<MergedWayPoint> eliminateLowerGradesMerged(
			List<MergedWayPoint> mergeWaypoints
			,int grade) {
		LinkedList<MergedWayPoint> resultlist = new LinkedList<MergedWayPoint>();
		for (MergedWayPoint mergedWayPoint : mergeWaypoints) {
			if (mergedWayPoint.getTrackGrade() >= grade){
				resultlist.add(mergedWayPoint);
			}
		}
		return resultlist;
	}
	/**
	 * Simple approach
	 * @param waypoints
	 * @param accuracy
	 * @return
	 */
	static public List<MergedWayPoint> mergeWaypoints(Collection<WayPoint> waypoints, double accuracy) {
		LinkedList<WayPoint> tempWaypoints = new LinkedList<WayPoint>(waypoints);
		LinkedList<MergedWayPoint> mergedWaypoints = new LinkedList<MergedWayPoint>();
		double procentBlocks = tempWaypoints.size()/100;
		double procents=100;
		while(!tempWaypoints.isEmpty()){
			if(Math.round(tempWaypoints.size()/procentBlocks)==procents){
				System.out.println("WayPoints Merged: "+ procents + "%");
				procents--;
			}
			MergedWayPoint mwp = new MergedWayPoint(tempWaypoints.getFirst());
			ReferenceWayPointComparator wpc = new ReferenceWayPointComparator();
			wpc.setReferencePoint(tempWaypoints.getFirst());
			Collections.sort(tempWaypoints, wpc);
			for (int i = 1; i < tempWaypoints.size() 
					&& (tempWaypoints.get(i).getCoor()
							.greatCircleDistance(
									tempWaypoints.getFirst().getCoor()
					)<=accuracy) ; i++) {
				mwp.addWayPoint(tempWaypoints.get(i));
				tempWaypoints.remove(i);
				i--;// correct index cause we removed the point
			}
			//mwp.calculateNewCoordinates();
			//mwp.calculateNewDate();
			tempWaypoints.removeFirst();
			mergedWaypoints.add(mwp);
		}

		return mergedWaypoints;

	}

	public static LatLon calculateCentroid(Collection<WayPoint> cluster) {
		if (cluster.isEmpty()){
			return null;
		}
		double lat =0;
		double lon = 0;
		for (Iterator<WayPoint> iterator = cluster.iterator(); iterator.hasNext();) {
			WayPoint wp = (WayPoint) iterator.next();
			lat+=wp.getCoor().getY();//Lat
			lon+=wp.getCoor().getX();//Lon
		}
		return new LatLon(lat/cluster.size(),lon/cluster.size());
	}

	public static String simpleGeneralizeDate(Collection<WayPoint> sourceWaypoints) {
		//FIXME:something goes wrong
		//sort by Date
//		if (!sourceWaypoints.isEmpty()){
//			Collections.sort(sourceWaypoints);
//			Date first = sourceWaypoints.getFirst().getTime();
//			Date last = sourceWaypoints.getLast().getTime();
//			SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
//			SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MM");
//			SimpleDateFormat dateFormatDay = new SimpleDateFormat("dd");
//			SimpleDateFormat dateFormatHour = new SimpleDateFormat("HH");
//			SimpleDateFormat dateFormatMinute = new SimpleDateFormat("mm");
//			SimpleDateFormat dateFormatSecond = new SimpleDateFormat("ss");
//			SimpleDateFormat dateFormatMilli = new SimpleDateFormat("SSS");
//			String result = new String();
//			//Year
//			if (	dateFormatYear.format(first)
//					.compareTo(
//							dateFormatYear.format(last))
//							==0
//					){
//				result+=dateFormatYear.format(last)+":";
//			}else{
//				result+="XXXX:";
//			}
//			//Month
//			if (	dateFormatMonth.format(first)
//					.compareTo(
//							dateFormatMonth.format(last))
//							==0
//					){
//				result+=dateFormatMonth.format(last)+":";
//			}else{
//				result+="XX:";
//			}
//			//Day
//			if (	dateFormatDay.format(first)
//					.compareTo(
//							dateFormatDay.format(last))
//							==0
//					){
//				result+=dateFormatDay.format(last)+":";
//			}else{
//				result+="XX:";
//			}
//			//Hour
//			if (	dateFormatHour.format(first)
//					.compareTo(
//							dateFormatHour.format(last))
//							==0
//					){
//				result+=dateFormatHour.format(last)+":";
//			}else{
//				result+="XX:";
//			}				
//			//Minute
//			if (	dateFormatMinute.format(first)
//					.compareTo(
//							dateFormatMinute.format(last))
//							==0
//					){
//				result+=dateFormatMinute.format(last)+":";
//			}else{
//				result+="XX:";
//			}
//			//Second
//			if (	dateFormatSecond.format(first)
//					.compareTo(
//							dateFormatSecond.format(last))
//							==0
//					){
//				result+=dateFormatSecond.format(last)+":";
//			}else{
//				result+="XX:";
//			}
//			//Milli
//			if (	dateFormatMilli.format(first)
//					.compareTo(
//							dateFormatMilli.format(last))
//							==0
//					){
//				result+=dateFormatMilli.format(last)+":";
//			}else{
//				result+="XXX";
//			}
//			return result;
//		}
		return "";
	}
	public static List<GpxTrack> createMoreWaypointsOnTracks(Collection<GpxTrack> tracks, double maxdistance){
		List<GpxTrack> newTracks= new LinkedList<GpxTrack>();
		for (GpxTrack gpxTrack : tracks) {
			newTracks.add(MergeGPS.createMoreWaypointsOnTrack(gpxTrack, maxdistance));
		}
		return newTracks;
	}
	public static GpxTrack createMoreWaypointsOnTrack(GpxTrack gpxTrack, double maxdistance) {
		LinkedList<WayPoint> mergedSegments=new LinkedList<WayPoint>();
		for (GpxTrackSegment gpxTrackSegment : gpxTrack.getSegments()) {
			mergedSegments.addAll(gpxTrackSegment.getWayPoints());
		}
		LinkedList<WayPoint> tempWaypoints = mergedSegments;
		for (int i = 1; i < tempWaypoints.size(); i++) {
			WayPoint firstWP = tempWaypoints.get(i-1);
			WayPoint secondWP = tempWaypoints.get(i);
			LatLon firstLL = tempWaypoints.get(i-1).getCoor();
			LatLon secondLL = tempWaypoints.get(i).getCoor();
			if (firstLL
					.greatCircleDistance(
							secondLL)
							>maxdistance){
				WayPoint tempWayPoint = new WayPoint(
						new LatLon(
								secondLL
								.getCenter(
										firstLL)
										)
						);
				tempWayPoint.time=(firstWP.time+secondWP.time)/2;
				tempWaypoints.add(i, tempWayPoint);
				i--;
			}
		}
		LinkedList<WayPoint> resultseq = tempWaypoints;
		LinkedList<Collection<WayPoint>> tracklist= new LinkedList<Collection<WayPoint>>();
		tracklist.add(resultseq);
		return new ImmutableGpxTrack(tracklist, new HashMap<String, Object>());
	}
	public static List<GpxTrackSegment> mergeSegmentsWithKMeans(List<GpxTrackSegment> list, int k, boolean ignoreDirection, double angleWeight, double distanceWeight){
		@SuppressWarnings("unchecked")
		List<GpxTrackSegment> oldClusterSegs,clusterSegs=getRandomEntrys(list,k);
		List<GpxTrackSegment> cluster;
		do{
			cluster= makeSegmentCluster(clusterSegs, list,angleWeight, distanceWeight);
			oldClusterSegs=clusterSegs;
			clusterSegs=cluster;
			IOFunctions.exportTrackSegments(cluster, "output/a_cluster.gpx");
			//IOFunctions.exportTrackSegments(oldClusterSegs, "output/a_ocluster.gpx");
		}while(!areSameSegments(oldClusterSegs,clusterSegs));
		return makeMergeSegmentCluster(clusterSegs,list, ignoreDirection, angleWeight, distanceWeight);
				
	}
	private static boolean areSameSegments(
			List<GpxTrackSegment> oldClusterSegs,
			List<GpxTrackSegment> clusterSegs) {
		//needed if more the one thread
		//SegmentComparator comp = new SegmentComparator();
		//Collections.sort(oldClusterSegs,comp);
		//Collections.sort(clusterSegs,comp);
		int count=-1;
		Iterator<GpxTrackSegment> newOneIter =clusterSegs.iterator();
		if(clusterSegs.size()==oldClusterSegs.size()){
			while (newOneIter.hasNext()) {
				count++;
				GpxTrackSegment newOne = newOneIter.next();
				if(!oldClusterSegs.contains(newOne)){
					System.out.println("KMeans cluster: "+count);
					return false;
				}
			}
		}else{
			return false;
		}
		return true;
	}
	public static List<MergedWayPoint> mergeWithKMeans(List<WayPoint> list, int k){
		return makeMergeCluster(findKMeansCluster(list,k),list);
				
	}
	public static List<WayPoint> findKMeansCluster(List<WayPoint> list, int k) {
		@SuppressWarnings("unchecked")
		List<WayPoint> oldClusterPoints,clusterPoints=getRandomEntrys(list,k);
		List<WayPoint> cluster;
		do{
			cluster= makeCluster(clusterPoints, list);
			oldClusterPoints=clusterPoints;
			clusterPoints=cluster;
		}while(!haveSameCoord(oldClusterPoints,clusterPoints));
		return clusterPoints;
	}
	/**
	 * Gives better clusters of the waypoints of list back
	 * starting by clusterPoints (if clusterPoints is not the best)
	 * NOTE: for faster calculations {@link MergeGPS.makeCluster}
	 * @param clusterPoints starting clusterPoints
	 * @param list list of all points
	 * @param distanceMartix distancematrix for all distances
	 * @return new clusterPoints with sourcePoints in it 
	 */
	private static List<MergedWayPoint> makeMergeCluster(
			List<WayPoint> clusterPoints, List<WayPoint> list) {
		List<MergedWayPoint> result= new LinkedList<MergedWayPoint>();
		//initialize result
		for (int i = 0; i < clusterPoints.size(); i++) {
			result.add(new MergedWayPoint(clusterPoints.get(i)));
		}
		for (WayPoint wayPoint : list) {
			//for each waypoint find nearest cluster point
			int index =findNearestPointIndex(wayPoint,clusterPoints);
			MergedWayPoint resultPoint = result.get(index);
			resultPoint.addWayPoint(wayPoint);
		}
		for (int i = 0; i < clusterPoints.size(); i++) {
			result.get(i).removeIfExist(clusterPoints.get(i));
		}
		return result;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static List getRandomEntrys(List list,
			int clusterNumber) {
		if (clusterNumber<list.size()){
			List result = new LinkedList();
			Random generator= new Random(1);
			while(result.size()< clusterNumber) {
				int index =generator.nextInt(list.size());
				Object randomWayPoint = list.get(index);
				if(!result.contains(randomWayPoint)){
					result.add(list.get(index));
				}

			}
			return result;
		}else{
			return new LinkedList(list);
		}
	}
	private static List<GpxTrackSegment> makeMergeSegmentCluster(
			List<GpxTrackSegment> clusterSegs, List<GpxTrackSegment> list, boolean ignoreDirection, double angleWeight, double distanceWeight) {
		List<List<GpxTrackSegment>> clusterGroups= new LinkedList<List<GpxTrackSegment>>();
		//initialize result
		for (int i = 0; i < clusterSegs.size(); i++) {
			clusterGroups.add(new LinkedList<GpxTrackSegment>());
		}
		for (GpxTrackSegment seg : list) {
			//for each waypoint find nearest cluster point
			if(seg.length()!=0){
				MergeGPS.findClusterAndAdd(seg,clusterSegs,angleWeight, distanceWeight,clusterGroups);
			}
		}
		LinkedList<GpxTrackSegment> result = new LinkedList<GpxTrackSegment>();
		//for each clusterGroup find new centroid
		for (List<GpxTrackSegment> cluster : clusterGroups) {
			if(!cluster.isEmpty()){
				result.add(MergeGPS.calculateMergeSegmentCentroid(cluster));
			}
		}
		return result;
	}
	public static GpxTrackSegment calculateMergeSegmentCentroid(
			List<GpxTrackSegment> cluster) {
		List<WayPoint> all1Wps = new LinkedList<WayPoint>();
		List<WayPoint> all2Wps = new LinkedList<WayPoint>();
		LatLon reference1=null;
		LatLon reference2=null;
		for (GpxTrackSegment gpxTrackSegment : cluster) {
			List<WayPoint> wps = new LinkedList<WayPoint>(gpxTrackSegment.getWayPoints());
			if(reference1==null || reference2==null){
				reference1=wps.get(0).getCoor();
				reference2=wps.get(1).getCoor();
				all1Wps.add(wps.get(0));
				all2Wps.add(wps.get(1));
			}else if(reference1.distance(wps.get(0).getCoor())//if 0 is closer to reference1
					<reference1.distance(wps.get(1).getCoor())){
				all1Wps.add(wps.get(0));
				all2Wps.add(wps.get(1));
			}else{
				all1Wps.add(wps.get(1));
				all2Wps.add(wps.get(0));
			}
		}
		WayPoint wp1= new MergedWayPoint(all1Wps);
		WayPoint wp2= new MergedWayPoint(all2Wps);
		LinkedList<WayPoint> newWps = new LinkedList<WayPoint>();
		newWps.add(wp1);
		newWps.add(wp2);
		return new ImmutableGpxTrackSegment(newWps);
	}
	private static List<GpxTrackSegment> makeSegmentCluster(
			List<GpxTrackSegment> clusterSegs, List<GpxTrackSegment> list, double angleWeight, double distanceWeight) {
		List<GpxTrackSegment> synList = list;
		List<List<GpxTrackSegment>> clusterGroups= new LinkedList<List<GpxTrackSegment>>();
		//initialize result
		for (int i = 0; i < clusterSegs.size(); i++) {
			clusterGroups.add(Collections.synchronizedList(new LinkedList<GpxTrackSegment>()));
		}
		for (GpxTrackSegment seg : synList) {
			if(seg.length()!=0){
				MergeGPS.findClusterAndAdd(seg,clusterSegs,angleWeight, distanceWeight,clusterGroups);
			}
		}
		LinkedList<GpxTrackSegment> result = new LinkedList<GpxTrackSegment>();
		//for each clusterGroup find new centroid
		for (List<GpxTrackSegment> cluster : clusterGroups) {
			if(!cluster.isEmpty()){
				//IOFunctions.exportTrackSegments(cluster, "output/cluster.gpx");
				GpxTrackSegment clusterSegment = MergeGPS.calculateSegmentCentroid(cluster);
				result.add(clusterSegment);
				LinkedList<GpxTrackSegment> clusterSegmentList = new LinkedList<GpxTrackSegment>();
				clusterSegmentList.add(clusterSegment);
				//IOFunctions.exportTrackSegments(clusterSegmentList, "output/resultingCluster.gpx");
			}
		}
		return result;
	}
	public static GpxTrackSegment calculateSegmentCentroid(
			List<GpxTrackSegment> cluster) {
		List<WayPoint> all1Wps = new LinkedList<WayPoint>();
		List<WayPoint> all2Wps = new LinkedList<WayPoint>();
		LatLon reference1=null;
		LatLon reference2=null;
		for (GpxTrackSegment gpxTrackSegment : cluster) {
			List<WayPoint> wps = new LinkedList<WayPoint>(gpxTrackSegment.getWayPoints());
			if(reference1==null || reference2==null){
				reference1=wps.get(0).getCoor();
				reference2=wps.get(1).getCoor();
				all1Wps.add(wps.get(0));
				all2Wps.add(wps.get(1));
			}else if(reference1.distance(wps.get(0).getCoor())//if 0 is closer to reference1
					<reference1.distance(wps.get(1).getCoor())){
				all1Wps.add(wps.get(0));
				all2Wps.add(wps.get(1));
			}else{
				all1Wps.add(wps.get(1));
				all2Wps.add(wps.get(0));
			}
		}
		WayPoint wp1= new WayPoint(calculateCentroid(all1Wps));
		WayPoint wp2= new WayPoint(calculateCentroid(all2Wps));
		LinkedList<WayPoint> newWps = new LinkedList<WayPoint>();
		newWps.add(wp1);
		newWps.add(wp2);
		return new ImmutableGpxTrackSegment(newWps);
	}
	public static void findClusterAndAdd(GpxTrackSegment seg,
			List<GpxTrackSegment> list, double angleWeight, double distanceWeight, List<List<GpxTrackSegment>> resultCluster) {
		double distance=Double.MAX_VALUE;
		int result=-1;
		for (int i = 0; i < list.size(); i++) {
			double currentDistance;
			currentDistance = segmentDistance(list.get(i),seg,angleWeight,distanceWeight);
			if (currentDistance<distance){
				distance=currentDistance;
				result=i;
				
			}
		}
		if(result!=-1){
			List<GpxTrackSegment> cluster = resultCluster.get(result);
			cluster.add(seg);
		}
	}
	@SuppressWarnings("unused")
	private static Double segmentAnglePercentDistance(GpxTrackSegment seg1,
			GpxTrackSegment seg2) {
		double distance = hausDorffDistance(seg1.getWayPoints(), seg2.getWayPoints());
		double angle = calculateAngle(seg1, seg2);
		double angleFactor=(Math.PI/2)/Math.abs(Math.PI/2-angle);
		//angle/(pi/2) * distance
		double result=distance*angleFactor;
		return result;
		
	}
	private static Double segmentDistance(GpxTrackSegment seg1,
			GpxTrackSegment seg2, double angleWeight, double distanceWeight) {
		assert(seg1.getWayPoints().size()==2 && seg2.getWayPoints().size()==2);
		double distance = additiveMinDistance(seg1.getWayPoints(), seg2.getWayPoints());
		double lonDiff1=Double.NaN;
		for (WayPoint wp1 : seg1.getWayPoints()) {
			if(Double.isNaN(lonDiff1)){
				lonDiff1=wp1.getCoor().getY();
			}else{
				lonDiff1-=wp1.getCoor().getY();
			}
		}
		double lonDiff2=Double.NaN;
		for (WayPoint wp2 : seg2.getWayPoints()) {
			if(Double.isNaN(lonDiff2)){
				lonDiff2=wp2.getCoor().getY();
			}else{
				lonDiff2-=wp2.getCoor().getY();
			}
		}
		double lonDistance=Math.abs(Math.abs(lonDiff1)-Math.abs(lonDiff2));
		double result=distance*(distanceWeight)+lonDistance*(angleWeight);
		return result;
		
	}
	/**
	 * Gives better clusters of the waypoints of list back
	 * starting by clusterPoints (if clusterPoints is not the best)
	 * @param clusterPoints starting clusterPoints
	 * @param list list of all points
	 * @param distanceMartix distancematrix for all distances
	 * @return new clusterPoints 
	 */
	private static List<WayPoint> makeCluster(
			List<WayPoint> clusterPoints, List<WayPoint> list) {
		List<List<WayPoint>> clusterGroups= new LinkedList<List<WayPoint>>();
		//initialize result
		for (int i = 0; i < clusterPoints.size(); i++) {
			clusterGroups.add(new LinkedList<WayPoint>());
		}
		for (WayPoint wayPoint : list) {
			//for each waypoint find nearest cluster point
			int index =findNearestPointIndex(wayPoint,clusterPoints);
			List<WayPoint> cluster = clusterGroups.get(index);
			cluster.add(wayPoint);
		}
		LinkedList<WayPoint> result = new LinkedList<WayPoint>();
		//for each clusterGroup find new centroid
		for (List<WayPoint> cluster : clusterGroups) {
			if(!cluster.isEmpty()){
				result.add(new WayPoint(MergeGPS.calculateCentroid(cluster)));
			}
		}
		return result;
	}
	private static int findNearestPointIndex(WayPoint wayPoint, List<WayPoint> list) {
		double distance=Double.MAX_VALUE;
		int result=-1;
		for (int i = 0; i < list.size(); i++) {
			Double currentDistance;
			/*
			if (distanceMartix!=null){
				currentDistance = distanceMartix.getValue(list.get(i), wayPoint);
				if(currentDistance==null){
					currentDistance = list.get(i).getCoor().greatCircleDistance(wayPoint.getCoor());
				}
			}else{*/
				currentDistance = list.get(i).getCoor().distance(wayPoint.getCoor());
			/*}*/
			if (currentDistance<distance){
				distance=currentDistance;
				result=i;
			}
		}
		return result;
	}
	private static boolean haveSameCoord(List<WayPoint> oldClusterPoints,
			List<WayPoint> clusterPoints) {
		if(clusterPoints.size()==oldClusterPoints.size()){
			for (int i = 0; i < clusterPoints.size(); i++) {
				LatLon point=clusterPoints.get(i).getCoor();
				LatLon point2=oldClusterPoints.get(i).getCoor();
				if (!point.equals(point2)){
					return false;
				}
			}
		}
			
		return true;
	}
	static public Double hausDorffDistance(Collection<? extends WayPoint> trackSeqs1, Collection<? extends WayPoint> trackSeqs2) {
		
		LinkedList<WayPoint> tempList1= new LinkedList<WayPoint>(trackSeqs1);
		LinkedList<WayPoint> tempList2= new LinkedList<WayPoint>(trackSeqs2);
		//get min
		LinkedList<Double> allDiffs1 = new LinkedList<Double>();
		LinkedList<Double> allDiffs2 = new LinkedList<Double>();
		for (WayPoint wayPoint : tempList1) {
			allDiffs1.add(getMinDifference(wayPoint,tempList2));
		}
		for (WayPoint wayPoint : tempList2) {
			allDiffs2.add(getMinDifference(wayPoint,tempList1));
		}
		double max1=getMax(allDiffs1);
		double max2=getMax(allDiffs2);
		return Math.max(max1,max2);
	}
static public Double additiveMinDistance(Collection<? extends WayPoint> trackSeqs1, Collection<? extends WayPoint> trackSeqs2) {
		
		LinkedList<WayPoint> tempList1= new LinkedList<WayPoint>(trackSeqs1);
		LinkedList<WayPoint> tempList2= new LinkedList<WayPoint>(trackSeqs2);
		//get min
		double result=0;
		for (WayPoint wayPoint : tempList1) {
			result+=getMinDifference(wayPoint,tempList2);
		}
		return result;
	}
	
	private static double getMax(LinkedList<Double> allDiffs1) {
		double result=0;
		for (Double double1 : allDiffs1) {
			if(double1>result){
				result=double1;
			}
		}
		return result;
	}
	private static Double getMinDifference(WayPoint wayPoint,
			LinkedList<WayPoint> tempList2) {
		double result=Double.MAX_VALUE;
		for (WayPoint wayPoint2 : tempList2) {
			double distance = wayPoint.getCoor().greatCircleDistance(wayPoint2.getCoor());
			if(distance<result){
				result=distance;
			}
		}
		return result;
	}
	@SuppressWarnings("unused")
	static private void sort(WayPoint wayPoint, LinkedList<WayPoint> tempList2) {
		ReferenceWayPointComparator wpc = new ReferenceWayPointComparator();
		wpc.setReferencePoint(wayPoint);
		Collections.sort(tempList2, wpc);
		
	}

	public static List<List<MergedWayPoint>> createSegments(List<MergedWayPoint> mergedWayPoints, int k) {
		if(mergedWayPoints.size()==0){
			return null;
		}
		List<List<MergedWayPoint>> segs= new LinkedList<List<MergedWayPoint>>();
		LinkedList<MergedWayPoint> mergedWayPointsList = new LinkedList<MergedWayPoint>(mergedWayPoints);
		List<MergedWayPoint> list = new LinkedList<MergedWayPoint>();
		MergedWayPoint temp = mergedWayPointsList.getFirst();
		MergedWayPoint neighbor;
		while(!mergedWayPointsList.isEmpty()){
			list.add(temp);
			neighbor = temp.getHighestNotMarkedNeighbor(k);
			if(neighbor==null){
				mergedWayPointsList.remove(temp);
				if(list.size()>1){//no one point seqs
					segs.add(list);
				}
				if(mergedWayPointsList.isEmpty()){
					break;
				}else{
					temp=mergedWayPointsList.getFirst();
					list = new LinkedList<MergedWayPoint>();
				}
			}else{
				temp.markConnection(neighbor);
				temp=neighbor;
			}
		}
		return segs;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<GpxTrack> buildTracks(List<MergedWayPoint> mergedWayPointsList, int k) {
		List<List<MergedWayPoint>> segs;
		LinkedList<GpxTrack> tracks = new LinkedList<GpxTrack>();
		Collection<Collection<WayPoint>> virtualSeq;
		segs=createSegments(mergedWayPointsList, k);
		if(segs==null){
			return tracks;
		}
		for (List<MergedWayPoint> seg : segs) {
			virtualSeq = new LinkedList<Collection<WayPoint>>();
			virtualSeq.add((List<WayPoint>)(List)seg);
			tracks.add(new ImmutableGpxTrack(virtualSeq,new HashMap<String, Object>()));
		}
		
		return tracks;
		
	}
	public static boolean isHausDorffDistanceShorter(
			Collection<? extends WayPoint> trackSeqs1, Collection<? extends WayPoint> trackSeqs2,
			double trackDistance) {
		LinkedList<WayPoint> tempList1= new LinkedList<WayPoint>(trackSeqs1);
		LinkedList<WayPoint> tempList2= new LinkedList<WayPoint>(trackSeqs2);
		//get min
		LinkedList<Double> allDiffs1 = new LinkedList<Double>();
		LinkedList<Double> allDiffs2 = new LinkedList<Double>();
		for (WayPoint wayPoint : tempList1) {
			double wpMin =getMinDifference(wayPoint,tempList2);
			if(wpMin>trackDistance){
				return false;
			}
			allDiffs1.add(wpMin);
		}
		for (WayPoint wayPoint : tempList2) {
			double wpMin = getMinDifference(wayPoint,tempList2);
			if (wpMin>trackDistance){
				return false;
			}
			allDiffs2.add(wpMin);
		}
		double max1=getMax(allDiffs1);
		double max2=getMax(allDiffs2);
		return Math.max(max1,max2)<trackDistance;
	}
	public static Bounds getBoundsWithSpace(Bounds bounds, double distance) {
		return new Bounds(addDistance(bounds.getMin(), -distance, -distance),
				addDistance(bounds.getMax(), distance, distance));
	}
	public static LatLon addDistance(LatLon p, double northernDistance, double westernDistance){
		double y = p.getY()+(180/Math.PI)*(northernDistance/6378135);
		double x = p.getX()+(180/Math.PI)*(westernDistance/6378135)/Math.cos(Math.toRadians(p.getY()));
		return new LatLon(y,x);
	}
	public static boolean haveNotTheSamePoints(GpxTrackSegment seg,
			GpxTrackSegment seg2) {
		for(WayPoint wp1 : seg.getWayPoints()){
			for(WayPoint wp2 : seg2.getWayPoints()){
				if (wp1.equals(wp2)){
					return false;
				}
			}
		}
		return true;
	}
	public static double calculateAngle(GpxTrackSegment seg,
			GpxTrackSegment seg2) {
		LinkedList<WayPoint> wps1 = new LinkedList<WayPoint>(seg.getWayPoints());
		LinkedList<WayPoint> wps2 = new LinkedList<WayPoint>(seg2.getWayPoints());
		double latVector1=wps1.get(0).getCoor().getY()
					-wps1.get(wps1.size()-1).getCoor().getY();
		double lonVector1=wps1.get(0).getCoor().getX()
					-wps1.get(wps1.size()-1).getCoor().getX();
		double latVector2=wps2.get(0).getCoor().getY()
					-wps2.get(wps2.size()-1).getCoor().getY();
		double lonVector2=wps2.get(0).getCoor().getX()
					-wps2.get(wps2.size()-1).getCoor().getX();
		double cosinusAlpha=(latVector1*latVector2+lonVector1*lonVector2)
					/(Math.sqrt(latVector1*latVector1+lonVector1*lonVector1)
							*(Math.sqrt(latVector2*latVector2+lonVector2*lonVector2)));
		if(cosinusAlpha>1){
			cosinusAlpha=1;
		}
		double alpha = Math.acos(cosinusAlpha);
		return alpha;
	}
	public static boolean differenceInAngleIsLowerThan(GpxTrackSegment seg,
			GpxTrackSegment seg2, double maxAngle) {
		return calculateAngle(seg,seg2)<maxAngle;
	}
	public static boolean haveSameTracks(Collection<WayPoint> wayPoints,
			Collection<WayPoint> wayPoints2) {
		for (WayPoint wp1 : wayPoints) {
			if(MergedWayPoint.class.isInstance(wp1)){
				for (WayPoint wp2 : wayPoints2) {
					if(MergedWayPoint.class.isInstance(wp2)){
						MergedWayPoint mwp1 = (MergedWayPoint)wp1;
						MergedWayPoint mwp2 = (MergedWayPoint)wp2;
						if (mwp1.hasSameTracks(mwp2)){
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	public static int getWayPointNumber(List<GpxTrack> tracks) {
		int result=0;
		for (GpxTrack gpxTrack : tracks) {
			for (GpxTrackSegment seg : gpxTrack.getSegments()) {
				result+=seg.getWayPoints().size();
			}
		}
		return result;
	}
	
	

=======
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.examples;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * A map/reduce program that estimates the value of Pi
 * using a quasi-Monte Carlo (qMC) method.
 * Arbitrary integrals can be approximated numerically by qMC methods.
 * In this example,
 * we use a qMC method to approximate the integral $I = \int_S f(x) dx$,
 * where $S=[0,1)^2$ is a unit square,
 * $x=(x_1,x_2)$ is a 2-dimensional point,
 * and $f$ is a function describing the inscribed circle of the square $S$,
 * $f(x)=1$ if $(2x_1-1)^2+(2x_2-1)^2 <= 1$ and $f(x)=0$, otherwise.
 * It is easy to see that Pi is equal to $4I$.
 * So an approximation of Pi is obtained once $I$ is evaluated numerically.
 * 
 * There are better methods for computing Pi.
 * We emphasize numerical approximation of arbitrary integrals in this example.
 * For computing many digits of Pi, consider using bbp.
 *
 * The implementation is discussed below.
 *
 * Mapper:
 *   Generate points in a unit square
 *   and then count points inside/outside of the inscribed circle of the square.
 *
 * Reducer:
 *   Accumulate points inside/outside results from the mappers.
 *
 * Let numTotal = numInside + numOutside.
 * The fraction numInside/numTotal is a rational approximation of
 * the value (Area of the circle)/(Area of the square) = $I$,
 * where the area of the inscribed circle is Pi/4
 * and the area of unit square is 1.
 * Finally, the estimated value of Pi is 4(numInside/numTotal).  
 */
public class QuasiMonteCarlo extends Configured implements Tool {
  static final String DESCRIPTION
      = "A map/reduce program that estimates Pi using a quasi-Monte Carlo method.";
  /** tmp directory for input/output */
  static private final Path TMP_DIR = new Path(
      QuasiMonteCarlo.class.getSimpleName() + "_TMP_3_141592654");
  
  /** 2-dimensional Halton sequence {H(i)},
   * where H(i) is a 2-dimensional point and i >= 1 is the index.
   * Halton sequence is used to generate sample points for Pi estimation. 
   */
  private static class HaltonSequence {
    /** Bases */
    static final int[] P = {2, 3}; 
    /** Maximum number of digits allowed */
    static final int[] K = {63, 40}; 

    private long index;
    private double[] x;
    private double[][] q;
    private int[][] d;

    /** Initialize to H(startindex),
     * so the sequence begins with H(startindex+1).
     */
    HaltonSequence(long startindex) {
      index = startindex;
      x = new double[K.length];
      q = new double[K.length][];
      d = new int[K.length][];
      for(int i = 0; i < K.length; i++) {
        q[i] = new double[K[i]];
        d[i] = new int[K[i]];
      }

      for(int i = 0; i < K.length; i++) {
        long k = index;
        x[i] = 0;
        
        for(int j = 0; j < K[i]; j++) {
          q[i][j] = (j == 0? 1.0: q[i][j-1])/P[i];
          d[i][j] = (int)(k % P[i]);
          k = (k - d[i][j])/P[i];
          x[i] += d[i][j] * q[i][j];
        }
      }
    }

    /** Compute next point.
     * Assume the current point is H(index).
     * Compute H(index+1).
     * 
     * @return a 2-dimensional point with coordinates in [0,1)^2
     */
    double[] nextPoint() {
      index++;
      for(int i = 0; i < K.length; i++) {
        for(int j = 0; j < K[i]; j++) {
          d[i][j]++;
          x[i] += q[i][j];
          if (d[i][j] < P[i]) {
            break;
          }
          d[i][j] = 0;
          x[i] -= (j == 0? 1.0: q[i][j-1]);
        }
      }
      return x;
    }
  }

  /**
   * Mapper class for Pi estimation.
   * Generate points in a unit square
   * and then count points inside/outside of the inscribed circle of the square.
   */
  public static class QmcMapper extends 
      Mapper<LongWritable, LongWritable, BooleanWritable, LongWritable> {

    /** Map method.
     * @param offset samples starting from the (offset+1)th sample.
     * @param size the number of samples for this map
     * @param context output {ture->numInside, false->numOutside}
     */
    public void map(LongWritable offset,
                    LongWritable size,
                    Context context) 
        throws IOException, InterruptedException {

      final HaltonSequence haltonsequence = new HaltonSequence(offset.get());
      long numInside = 0L;
      long numOutside = 0L;

      for(long i = 0; i < size.get(); ) {
        //generate points in a unit square
        final double[] point = haltonsequence.nextPoint();

        //count points inside/outside of the inscribed circle of the square
        final double x = point[0] - 0.5;
        final double y = point[1] - 0.5;
        if (x*x + y*y > 0.25) {
          numOutside++;
        } else {
          numInside++;
        }

        //report status
        i++;
        if (i % 1000 == 0) {
          context.setStatus("Generated " + i + " samples.");
        }
      }

      //output map results
      context.write(new BooleanWritable(true), new LongWritable(numInside));
      context.write(new BooleanWritable(false), new LongWritable(numOutside));
    }
  }

  /**
   * Reducer class for Pi estimation.
   * Accumulate points inside/outside results from the mappers.
   */
  public static class QmcReducer extends 
      Reducer<BooleanWritable, LongWritable, WritableComparable<?>, Writable> {
    
    private long numInside = 0;
    private long numOutside = 0;
      
    /**
     * Accumulate number of points inside/outside results from the mappers.
     * @param isInside Is the points inside? 
     * @param values An iterator to a list of point counts
     * @param context dummy, not used here.
     */
    public void reduce(BooleanWritable isInside,
        Iterable<LongWritable> values, Context context)
        throws IOException, InterruptedException {
      if (isInside.get()) {
        for (LongWritable val : values) {
          numInside += val.get();
        }
      } else {
        for (LongWritable val : values) {
          numOutside += val.get();
        }
      }
    }

    /**
     * Reduce task done, write output to a file.
     */
    @Override
    public void cleanup(Context context) throws IOException {
      //write output to a file
      Path outDir = new Path(TMP_DIR, "out");
      Path outFile = new Path(outDir, "reduce-out");
      Configuration conf = context.getConfiguration();
      FileSystem fileSys = FileSystem.get(conf);
      SequenceFile.Writer writer = SequenceFile.createWriter(fileSys, conf,
          outFile, LongWritable.class, LongWritable.class, 
          CompressionType.NONE);
      writer.append(new LongWritable(numInside), new LongWritable(numOutside));
      writer.close();
    }
  }

  /**
   * Run a map/reduce job for estimating Pi.
   *
   * @return the estimated value of Pi
   */
  public static BigDecimal estimatePi(int numMaps, long numPoints,
      Configuration conf
      ) throws IOException, ClassNotFoundException, InterruptedException {
    Job job = new Job(conf);
    //setup job conf
    job.setJobName(QuasiMonteCarlo.class.getSimpleName());
    job.setJarByClass(QuasiMonteCarlo.class);

    job.setInputFormatClass(SequenceFileInputFormat.class);

    job.setOutputKeyClass(BooleanWritable.class);
    job.setOutputValueClass(LongWritable.class);
    job.setOutputFormatClass(SequenceFileOutputFormat.class);

    job.setMapperClass(QmcMapper.class);

    job.setReducerClass(QmcReducer.class);
    job.setNumReduceTasks(1);

    // turn off speculative execution, because DFS doesn't handle
    // multiple writers to the same file.
    job.setSpeculativeExecution(false);

    //setup input/output directories
    final Path inDir = new Path(TMP_DIR, "in");
    final Path outDir = new Path(TMP_DIR, "out");
    FileInputFormat.setInputPaths(job, inDir);
    FileOutputFormat.setOutputPath(job, outDir);

    final FileSystem fs = FileSystem.get(conf);
    if (fs.exists(TMP_DIR)) {
      throw new IOException("Tmp directory " + fs.makeQualified(TMP_DIR)
          + " already exists.  Please remove it first.");
    }
    if (!fs.mkdirs(inDir)) {
      throw new IOException("Cannot create input directory " + inDir);
    }

    try {
      //generate an input file for each map task
      for(int i=0; i < numMaps; ++i) {
        final Path file = new Path(inDir, "part"+i);
        final LongWritable offset = new LongWritable(i * numPoints);
        final LongWritable size = new LongWritable(numPoints);
        final SequenceFile.Writer writer = SequenceFile.createWriter(
            fs, conf, file,
            LongWritable.class, LongWritable.class, CompressionType.NONE);
        try {
          writer.append(offset, size);
        } finally {
          writer.close();
        }
        System.out.println("Wrote input for Map #"+i);
      }
  
      //start a map/reduce job
      System.out.println("Starting Job");
      final long startTime = System.currentTimeMillis();
      job.waitForCompletion(true);
      final double duration = (System.currentTimeMillis() - startTime)/1000.0;
      System.out.println("Job Finished in " + duration + " seconds");

      //read outputs
      Path inFile = new Path(outDir, "reduce-out");
      LongWritable numInside = new LongWritable();
      LongWritable numOutside = new LongWritable();
      SequenceFile.Reader reader = new SequenceFile.Reader(fs, inFile, conf);
      try {
        reader.next(numInside, numOutside);
      } finally {
        reader.close();
      }

      //compute estimated value
      final BigDecimal numTotal
          = BigDecimal.valueOf(numMaps).multiply(BigDecimal.valueOf(numPoints));
      return BigDecimal.valueOf(4).setScale(20)
          .multiply(BigDecimal.valueOf(numInside.get()))
          .divide(numTotal, RoundingMode.HALF_UP);
    } finally {
      fs.delete(TMP_DIR, true);
    }
  }

  /**
   * Parse arguments and then runs a map/reduce job.
   * Print output in standard out.
   * 
   * @return a non-zero if there is an error.  Otherwise, return 0.  
   */
  public int run(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: "+getClass().getName()+" <nMaps> <nSamples>");
      ToolRunner.printGenericCommandUsage(System.err);
      return 2;
    }
    
    final int nMaps = Integer.parseInt(args[0]);
    final long nSamples = Long.parseLong(args[1]);
        
    System.out.println("Number of Maps  = " + nMaps);
    System.out.println("Samples per Map = " + nSamples);
        
    System.out.println("Estimated value of Pi is "
        + estimatePi(nMaps, nSamples, getConf()));
    return 0;
  }

  /**
   * main method for running it as a stand alone command. 
   */
  public static void main(String[] argv) throws Exception {
    System.exit(ToolRunner.run(null, new QuasiMonteCarlo(), argv));
  }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

