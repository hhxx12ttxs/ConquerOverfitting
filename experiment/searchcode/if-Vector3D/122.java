package com.voyagegames.core.android.opengles.modules;

import android.opengl.Matrix;

import com.voyagegames.core.android.opengles.interfaces.IEntity;

public class Entity implements IEntity {
	
	protected Vector3D mPosition;
	protected Vector3D mRotationAxis;
	protected float    mRotationAngle;
	
	public Entity() {
		mPosition = new Vector3D(0, 0, 0);
		mRotationAxis = new Vector3D(0, 0, 0);
	}

	@Override
	public Vector3D position() {
		return mPosition;
	}

	@Override
	public void setPosition(final Vector3D position) {
		mPosition = position;
	}

	@Override
	public float rotationAngle() {
		return mRotationAngle;
	}

	@Override
	public void setRotationAngle(final float angle) {
		mRotationAngle = angle;
	}

	@Override
	public Vector3D rotationAxis() {
		return mRotationAxis;
	}

	@Override
	public void setRotationAxis(final Vector3D axis) {
		mRotationAxis = axis;
	}

	@Override
	public float[] modelMatrix() {
		final float[] modelMatrix = new float[16];
		
		Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, mPosition.x, mPosition.y, mPosition.z);
        
        if (mRotationAngle != 0 && (mRotationAxis.x != 0 || mRotationAxis.y != 0 || mRotationAxis.z != 0)) {
        	Matrix.rotateM(modelMatrix, 0, mRotationAngle, mRotationAxis.x, mRotationAxis.y, mRotationAxis.z);
        }
        
		return modelMatrix;
	}

}

