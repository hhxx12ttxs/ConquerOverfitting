package com.voyagegames.core.android.opengles.modules;

public class Vector3D extends Vector2D {
	
	public final float z;
	
	public Vector3D(final float x, final float y, final float z) {
		super(x, y);
		this.z = z;
	}
	
	public Vector3D(final float[] values) {
		super(values);
		
		if (values.length < 3) {
			throw new IllegalArgumentException("Invalid values");
		}
		
		this.z = values[2];
	}
	
	public float[] toArray() {
		final float[] result = new float[3];
		
		result[0] = x;
		result[1] = y;
		result[2] = z;
		return result;
	}

}

