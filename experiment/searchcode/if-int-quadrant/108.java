package com.pointcliki.map;

import java.util.ArrayList;

public class RandomHeightMap {
	
	protected int fWidth;
	protected int fHeight;
	protected Integer[][] fWeights;
	protected int fMaxStep;
	protected int fMinStep;
	protected int fMin;
	protected int fMax;

	public RandomHeightMap(int width, int height, int min, int max, int maxStep, int minStep) {
		fWidth = width;
		fHeight = height;
		
		fMin = min;
		fMax = max;
		
		fMaxStep = maxStep;
		fMinStep = minStep;
		
		fWeights = new Integer[width][height];
	}
	
	public void computeWeights() {
		ArrayList<Quadrant> queue = new ArrayList<Quadrant>();
		queue.add(new Quadrant(0, 0, fWidth, fHeight));
		
		while (!queue.isEmpty()) {
			Quadrant quad = queue.remove(0);
			
			// Check for size
			if (quad.w < 3 && quad.h < 3) continue;
			
			// Get the four corner values
			int nw = fWeights[quad.x][quad.y];
			int ne = fWeights[quad.x + quad.w - 1][quad.y];
			int sw = fWeights[quad.x][quad.y + quad.h - 1];
			int se = fWeights[quad.x + quad.w - 1][quad.y + quad.h - 1];
			
			// Find the middle of the quadrant
			int midx = quad.x + quad.w / 2;
			int midy = quad.y + quad.h / 2;
			
			// Make a new weight for the middle
			fWeights[midx][midy] = (int) Math.min(Math.max((nw + ne + sw + se) / 4 + generateWeight(), fMin), fMax);
			// Make new weights for the sides
			fWeights[midx][quad.y] = (int) Math.min(Math.max((nw + ne) / 2 + generateWeight(), fMin), fMax);
			fWeights[quad.x][midy] = (int) Math.min(Math.max((nw + sw) / 2 + generateWeight(), fMin), fMax);
			fWeights[quad.x + quad.w - 1][midy] = (int) Math.min(Math.max((ne + se) / 2 + generateWeight(), fMin), fMax);
			fWeights[midx][quad.y + quad.h - 1] = (int) Math.min(Math.max((sw + se) / 2 + generateWeight(), fMin), fMax);
			
			// Add sub-quads to queue
			queue.add(new Quadrant(quad.x, quad.y, midx + 1 - quad.x, midy + 1 - quad.y));
			queue.add(new Quadrant(midx, quad.y, quad.x + quad.w - midx, midy + 1 - quad.y));
			queue.add(new Quadrant(quad.x, midy, midx + 1 - quad.x, quad.y + quad.h - midy));
			queue.add(new Quadrant(midx, midy, quad.x + quad.w - midx, quad.y + quad.h - midy));
		}
	}
	
	public int generateWeight() {
		return (int) Math.round(Math.random() * (fMaxStep - fMinStep) + fMinStep);
	}

	public void setWeight(int x, int y, int cost) {
		fWeights[x][y] = cost;
	}

	public int getWeight(int x, int y) {
		return fWeights[x][y];
	}

	public int getHeight() {
		return fHeight;
	}

	public int getWidth() {
		return fWidth;
	}
	
	@Override
	public String toString() {
		String s = MapString.ArrayXYToString(fWeights, 0);
		if (s != null) return s;
		return "[Empty Random Height Map]";
	}
	
	protected class Quadrant {
		public int x;
		public int y;
		public int w;
		public int h;
		public Quadrant(int x, int y, int w, int h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}
		public String toString() {
			return "(" + x + ", " + y + ", " + w + ", " + h + ")";
		}
	}
}

