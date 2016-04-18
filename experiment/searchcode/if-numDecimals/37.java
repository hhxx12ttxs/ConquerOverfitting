package com.endoplasm.engine;

public class MathUtil {
	
	public static float reduce(float f, int numDecimals){
		return (float) (f - (f * Math.pow(10, numDecimals) % 1 / Math.pow(10, numDecimals)));
	}

	public static float getMin(float[] values) {
		float lowest = values[0];
		for (float f : values) {
			if (f < lowest) lowest = f;
		}
		return lowest;
	}

	public static float getMax(float[] values) {
		float highest = values[0];
		for (float f : values) {
			if (f > highest) highest = f;
		}
		return highest;
	}

	public static float distance(float x1, float y1, float x2, float y2) {
		float x = x1 - x2;
		float y = y1 - y2;
		return (float) Math.sqrt((x * x) + (y * y));
	}

	public static float direction(float x1, float y1, float x2, float y2) {
		double dx = x1 - x2;
		double dy = y2 - y1;

		double inRads = Math.atan2(dy, dx);

		if (inRads < 0)
			inRads = Math.abs(inRads);
		else
			inRads = 2 * Math.PI - inRads;
		return (float) Math.toDegrees(inRads);
	}

	public static float getXSpeed(float rotation, float dist) {
		return (float) Math.cos(Math.toRadians(rotation)) * dist;
	}

	public static float getYSpeed(float rotation, float dist) {
		return (float) Math.sin(Math.toRadians(rotation)) * dist;
	}

	public static float getAngleBetween(float targetA, float sourceA) {
		float a = targetA - sourceA;
		a = signedMod(a + 180, 360) - 180;
		return a;
	}
	
	public static float signedMod(float a, float n){
		return (float) (a - Math.floor(a/n) * n);
	}
}

