package com.ike.rwdccalc.Utilities;

import java.util.ArrayList;

import com.ike.rwdccalc.Objects.Coordinates;
import com.ike.rwdccalc.Objects.DistancePath;
import com.ike.rwdccalc.Objects.DoubleObject;
// This will be used to calculation each individual set of two planes
public class CalculatingTools {
	
	///////////////
	// Calculate //
	///////////////
	public static DistancePath calculate(boolean radius1InFeet, boolean radius2InFeet, double radiusOne, double radiusTwo, double footprintWidth) {
		
		double radius1 = 0;	// Extra variable to replace RadiusOne because of the possibility of the radius is in miles
		double radius2 = 0;	// Extra variable to replace RadiusTwo because of the possibility of the radius is in miles
		
		// Check to make sure that radius one is in feet
		if (!radius1InFeet) {
			// It is not, so convert it from miles to feet
			radius1 = radiusOne * 5280;
			// Some logging, warn the user about this
			System.out.println("W:RWDCCalc:CalculatingTools.calculate: - Radius one is " + radiusOne + " miles, converting to " + radius1 +  " feet");
		} else {
			// It is in feet, so leave it how it is
			radius1 = radiusOne;
		}
		
		// Check to make sure that radius two is in feet
		if (!radius2InFeet) {
			// It is not, so convert it from miles to feet
			radius2 = radiusTwo * 5280;
			// Some logging, warn the user about this
			System.out.println("W:RWDCCalc:CalculatingTools.calculate: - Radius two is " + radiusTwo + " miles, converting to " + radius2 +  " feet");
		} else {
			// It is in feet, so leave it how it is
			radius2 = radiusTwo;
		}
		
		// Check to make sure radius two is larger than radius one
		// If it isn't then the distance cannot be calculated correctly
		if (radius1 > radius2) {
			// Radius one is larger than radius two, so warn the user
			System.out.println("W:RWDCCalc:CalculatingTools.calculate: - radius one is larger than radius two");
			// Flip radius one and radius two, in case the user got them backwards
			return pathDistance(radius2, radius1, footprintWidth);
		} else {
			// Everything is fine, run the calculation
			return pathDistance(radius1, radius2, footprintWidth);
		}
		
	}
	
	//////////////////////////////
	// Path Distance Calculator //
	//////////////////////////////
	public static DistancePath pathDistance(double innerRadius, double outerRadius, double footprintWidth) {
		
		final double tau = 2 * Math.PI;	// Pi times two
		
		// Show some logging of the numbers that were input
		System.out.println("I:RWDCCalc:CalculatingTools.pathDistance: - innerRadius: " + innerRadius);
		System.out.println("I:RWDCCalc:CalculatingTools.pathDistance: - outerRadius: " + outerRadius);
		System.out.println("I:RWDCCalc:CalculatingTools.pathDistance: - footprintWidth: " + footprintWidth);
		
		// Find half of the footprint width, this will be used for the first
		// iteration where the plane is on the edge
		double halfWidth = footprintWidth / 2;
		// Show some logging of the halfWidth value
		System.out.println("I:RWDCCalc:CalculatingTools.pathDistance: - halfWidth is " + halfWidth);
		// Find the baseRadius, this is what we will base the calculations off of
		final double baseRadius = outerRadius - halfWidth;
		// Show some logging of the baseRadius value
		System.out.println("I:RWDCCalc:CalculatingTools.pathDistance: - baseRadius is " + baseRadius);
		
		// Initiate the totalDistance double ArrayList
		ArrayList<DoubleObject> totalDistance = new ArrayList<DoubleObject>();
		
		// This is the difference between the inner radius and the base radius
		// this tells us how many iterations we are going to need to do
		double radiusDif = baseRadius - innerRadius;
		
		// Calculate the number of iterations we will do based on the footprint width
		double iterationsDouble = radiusDif / footprintWidth;		
		// Round iterationsDouble to an integer, we can only do a fixed amount of iterations
		//double iterations = Math.round(iterationsDouble);
		// Show some logging of the iterations value
		if (iterationsDouble == 1) {
			System.out.println("I:RWDCCalc:CalculatingTools.pathDistance: - Doing " + iterationsDouble + " iterations");
		} else {
			System.out.println("I:RWDCCalc:CalculatingTools.pathDistance: - Doing " + iterationsDouble + " iterations");
		}
		
		double distance = 0;	// This is holds the distance between two points
		double theta = 0;		// This is the angle the points are from the center
		double radius = 0;		// This is the length of the radius
		double angle = 360;		// This is used to find the angle from center on each iteration
		
		// We will use the rate of change to find out how much to shorten the radius each time
		double rateOfChange = footprintWidth / 360;
		// Show some logging of the value of rateOfChange
		System.out.println("I:RWDCCalc:CalculatingTools.pathDistance: - rateOfChange is " + rateOfChange);
		
		// A and B will be used for the calculations of the distances
		Coordinates A = new Coordinates();
		Coordinates B = new Coordinates();
		
		// These ArrayLists will update every iteration with information provided
		ArrayList<String> distanceFromCenter = new ArrayList<String>();
		ArrayList<String> angleFromCenter = new ArrayList<String>();
		ArrayList<String> segmentType = new ArrayList<String>();
		
		// Only run this if there is a greater than 0 amount of iterations
		if (iterationsDouble != 0)
		for (double x = 0; x < iterationsDouble; x+=0.0041666666666666666667) {
			
			// Add the distance from center, angle from center, and segment type here
			distanceFromCenter.add(Double.toString(baseRadius - (footprintWidth * x)));
			angleFromCenter.add(Double.toString(angle * x));
			segmentType.add("P");
		
			// We will go around the circle and calculate the distance every degree around the circle
			for (double y = 0; y <= 1.5; y++) {
				
				// Set theta to whatever y is, which will be where
				// we are around the circle
				theta = y;
				
				// Let's update the radius of the flight path as the plane flys
				// around the circle in a spiral
				if (y == 0 && x == 0) {
					A.setX(baseRadius * Math.cos(Math.toRadians(theta)));
					A.setY(baseRadius * Math.sin(Math.toRadians(theta)));
					radius = baseRadius;
				} else {
					radius -= rateOfChange;
					// Let's figure out the coordinates of B
					B.setX(radius * Math.cos(Math.toRadians(theta)));
					B.setY(radius * Math.sin(Math.toRadians(theta)));
					
					// Let's find the distance between coordinate A and coordinate B
					distance = pointDistance(A.getX(), B.getX(), A.getY(), B.getY());
					
					// Let's add the distance to the totalDistance array
					totalDistance.add(new DoubleObject(distance));
					
					// Let's set B to A, and clear B, so B can be set on the next iteration
					A.clearAll();
					A.set(B);
					B.clearAll();
				}
			}
		}
		
		// Add in the distance of the outside circle that the plane will have to travel as well
		totalDistance.add(new DoubleObject(tau * baseRadius));
		
		// Return the answer as a DistancePath object
		DistancePath dp = new DistancePath(addArray(totalDistance), distanceFromCenter, angleFromCenter, segmentType);

		return dp;
	}
	
	/////////////////////////////////
	// Distance Between Two Points //
	/////////////////////////////////
	
	public static double pointDistance(double xPointA, double xPointB, double yPointA, double yPointB) {
		
		// Use the distance equation to find the distance between two arbitrary points
		double answer = Math.sqrt(Math.pow(xPointB - xPointA, 2) + Math.pow(yPointA - yPointB, 2));
		
		return answer;
	}
	
	////////////////////////////////////////////
	// Add Up All of the Elements In An Array //
	////////////////////////////////////////////
	
	public static double addArray(ArrayList<DoubleObject> array) {
		// Show some logging of the size of the array
		System.out.println("I:RWDCCalc:CalculatingTools.addArray: - Array size is " + array.size());
		double answer = 0; // This double will hold the incremental summations of the array
		for (int x = 0; x < array.size(); x++) {
			// Add up the array by adding each value on top of each other
			answer = answer + array.get(x).getValue();
		}
		return answer;
	}
}

