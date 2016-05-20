package com.breaktrycatch.needmorehumans.tracing.algorithms;

import processing.core.PVector;

import com.breaktrycatch.needmorehumans.tracing.EdgeVO;
import com.breaktrycatch.needmorehumans.utils.LogRepository;

public class BetterRelevancy {
	public static double calculate(EdgeVO s1, EdgeVO s2) {
		double k;
		
		//k = ((angle between s1 and s2)*(length of s1)*(length of s2))/(length of s1) + (length of s2)
		
		//First convert the edges to Vectors
		//Vec AB needs to be derived as BA
		//Vec BC can stay as BC. This gives intersection of B to find angle ABC.
		PVector v1 = new PVector(s1.p1.x - s1.p2.x, s1.p1.y - s1.p2.y);
		PVector v2 = new PVector(s2.p2.x - s2.p1.x, s2.p2.y - s2.p1.y);
		
		double scalar = (v1.x * v2.x) + (v1.y * v2.y);
		double l1 = v1.mag();
		double l2 = v2.mag();
		
		double angle = Math.acos(scalar/(l1*l2))*(180/Math.PI);
		
		if (Double.isNaN(angle)) {
			//LogRepository.getInstance().getJonsLogger().info("ANGLE " + angle + " scalar " + scalar + " v1x " + v1.x + " v1y " + v1.y + " v2x " + v2.x + " v2y " + v2.y);
			k = 0;
		}
		else {
			k = (Math.abs(angle - 180) * l1 * l2) / (l1 + l2);
		}
		
		//LogRepository.getInstance().getJonsLogger().info("BETTER RELEVANCY " + k);
		
		return k;
	}
}

