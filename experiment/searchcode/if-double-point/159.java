package net.minecraft.src;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

public class PhysMatrix {
	
	public static FloatBuffer getGLMatrix(double[] matrix)
	{
		return getGLMatrix(convertToFloat(matrix));
	}
	
	public static FloatBuffer getGLMatrix(float[] matrix)
	{
		float[] transpose = transpose(matrix);
		return getDirectBuffer(transpose);
	}
	
	public static float[] convertToFloat(double[] array)
	{
		float[] floatArray = new float[array.length];
		for(int i = 0; i < array.length; i++)
		{
			floatArray[i] = (float)array[i];
		}
		return floatArray;
	}
	
	public static FloatBuffer getDirectBuffer(float[] buffer)
	{
		ByteBuffer buff = ByteBuffer.allocateDirect(16*4);
		FloatBuffer floatBuff = buff.asFloatBuffer();
		floatBuff.put(buffer);
		floatBuff.flip();
		return floatBuff;
	}
	
	public static double[] transpose(double[] matrix)
	{
		double[] transpose = new double[16];
		transpose [0] = matrix[0];
		transpose [1] = matrix[4];
		transpose [2] = matrix[8];
		transpose [3] = matrix[12];
		transpose [4] = matrix[1];
		transpose [5] = matrix[5];
		transpose [6] = matrix[9];
		transpose [7] = matrix[13];
		transpose [8] = matrix[2];
		transpose [9] = matrix[6];
		transpose [10] = matrix[10];
		transpose [11] = matrix[14];
		transpose [12] = matrix[3];
		transpose [13] = matrix[7];
		transpose [14] = matrix[11];
		transpose [15] = matrix[15];
		return transpose;
	}
	
	public static float[] transpose(float[] matrix)
	{
		float[] transpose = new float[16];
		transpose [0] = matrix[0];
		transpose [1] = matrix[4];
		transpose [2] = matrix[8];
		transpose [3] = matrix[12];
		transpose [4] = matrix[1];
		transpose [5] = matrix[5];
		transpose [6] = matrix[9];
		transpose [7] = matrix[13];
		transpose [8] = matrix[2];
		transpose [9] = matrix[6];
		transpose [10] = matrix[10];
		transpose [11] = matrix[14];
		transpose [12] = matrix[3];
		transpose [13] = matrix[7];
		transpose [14] = matrix[11];
		transpose [15] = matrix[15];
		return transpose;
	}
	
	public static double[] getTranslationMatrix(double x, double y, double z)
	{
		double[] matrix = getDoubleIdentity();
		matrix[3] = x;
		matrix[7] = y;
		matrix[11] = z;
		return matrix;
	}
	
	public static double[] getRotationMatrix(double ux, double uy, double uz, double angle)
	{
		if(ux == 0 && uy == 0 && uz == 0)
		{
			return getDoubleIdentity();
		}
		double C = Math.cos(angle);
		double S = Math.sin(angle);
		double t = 1-C;

		double axismag = Math.sqrt(ux*ux + uy*uy + uz*uz);
		ux /= axismag;
		uy /= axismag;
		uz /= axismag;
		
		double[] matrix = getDoubleIdentity();
		
		matrix[0] = t*ux*ux + C;
		matrix[1] = t*ux*uy - S*uz;
		matrix[2] = t*ux*uz + S*uy;
		matrix[4] = t*ux*uy + S*uz;
		matrix[5] = t*uy*uy + C;
		matrix[6] = t*uy*uz - S*ux;
		matrix[8] = t*ux*uz - S*uy;
		matrix[9] = t*uy*uz + S*ux;
		matrix[10] = t*uz*uz + C;

		return matrix;
	}
	public static double[] getRotationMatrixAboutAxisAndPoint(double ux, double uy, double uz, double px, double py, double pz, double angle)
	{
		double[] matrix1 = getTranslationMatrix(-px, -py, -pz);
		double[] matrix2 = getRotationMatrix(ux, uy, uz, angle);
		double[] matrix3 = getTranslationMatrix(px, py, pz);
		double[] matrix4 = getMatrixProduct(matrix2, matrix1);
		return getMatrixProduct(matrix3, matrix4);
	}
	
	public static double[] getRotationMatrixAboutAxisAndPoint(PhysVector axis, PhysVector point, double angle)
	{
		double[] matrix1 = getTranslationMatrix(-point.X, -point.Y, -point.Z);
		double[] matrix2 = getRotationMatrix(axis.X, axis.Y, axis.Z, angle);
		double[] matrix3 = getTranslationMatrix(point.X, point.Y, point.Z);
		double[] matrix4 = getMatrixProduct(matrix2, matrix1);
		return getMatrixProduct(matrix3, matrix4);
	}
	
	public static void rotateAboutAxisAndPoint(PhysVector vec, double ux, double uy, double uz, double px, double py, double pz, float angle)
	{
		applyTransform(getRotationMatrixAboutAxisAndPoint((float)ux, (float)uy, (float)uz, px, py, pz, angle), vec);
	}
	
	public static double[] getDoubleIdentity()
	{
		double[] identity = new double[16];
		
		identity[0] = 1;
		identity[1] = 0;
		identity[2] = 0;
		identity[3] = 0;
		identity[4] = 0;
		identity[5] = 1;
		identity[6] = 0;
		identity[7] = 0;
		identity[8] = 0;
		identity[9] = 0;
		identity[10] = 1;
		identity[11] = 0;
		identity[12] = 0;
		identity[13] = 0;
		identity[14] = 0;
		identity[15] = 1;
		
		return identity;
	}
	
	public static double[] getScaleMatrix(PhysVector scale)
	{
		double[] identity = new double[16];
		
		identity[0] = scale.X;
		identity[1] = 0;
		identity[2] = 0;
		identity[3] = 0;
		identity[4] = 0;
		identity[5] = scale.Y;
		identity[6] = 0;
		identity[7] = 0;
		identity[8] = 0;
		identity[9] = 0;
		identity[10] = scale.Z;
		identity[11] = 0;
		identity[12] = 0;
		identity[13] = 0;
		identity[14] = 0;
		identity[15] = 1;
		
		return identity;
	}
	
	public static double[] getDoubleIdentity(int size)
	{
		double[] identity = new double[size*size];
		
		for(int i = 0; i < identity.length; i += size + 1)
		{
			identity[i] = 1;
			for(int j = i + 1; j < i + size + 1 && j < identity.length; j++)
			{
				identity[j] = 0;
			}
		}
		
		return identity;
	}
	
	public static double[] getZeroMatrix(int size)
	{
		double[] zero = new double[size*size];
		
		for(int i = 0; i < zero.length; i++)
		{
			zero[i] = 0;
		}
		
		return zero;
	}
	
	public static float[] getFloatIdentity()
	{
		float[] identity = new float[16];
		
		identity[0] = 1;
		identity[1] = 0;
		identity[2] = 0;
		identity[3] = 0;
		identity[4] = 0;
		identity[5] = 1;
		identity[6] = 0;
		identity[7] = 0;
		identity[8] = 0;
		identity[9] = 0;
		identity[10] = 1;
		identity[11] = 0;
		identity[12] = 0;
		identity[13] = 0;
		identity[14] = 0;
		identity[15] = 1;
		
		return identity;
	}
	
	public static double[] getMatrixProduct(float[] M1, float[] M2)
	{
		double[] product = new double[16];
		product[0] = M1[0]*M2[0] + M1[1]*M2[4] + M1[2]*M2[8] + M1[3]*M2[12];
		product[1] = M1[0]*M2[1] + M1[1]*M2[5] + M1[2]*M2[9] + M1[3]*M2[13];
		product[2] = M1[0]*M2[2] + M1[1]*M2[6] + M1[2]*M2[10] + M1[3]*M2[14];
		product[3] = M1[0]*M2[3] + M1[1]*M2[7] + M1[2]*M2[11] + M1[3]*M2[15];
		product[4] = M1[4]*M2[0] + M1[5]*M2[4] + M1[6]*M2[8] + M1[7]*M2[12];
		product[5] = M1[4]*M2[1] + M1[5]*M2[5] + M1[6]*M2[9] + M1[7]*M2[13];
		product[6] = M1[4]*M2[2] + M1[5]*M2[6] + M1[6]*M2[10] + M1[7]*M2[14];
		product[7] = M1[4]*M2[3] + M1[5]*M2[7] + M1[6]*M2[11] + M1[7]*M2[15];
		product[8] = M1[8]*M2[0] + M1[9]*M2[4] + M1[10]*M2[8] + M1[11]*M2[12];
		product[9] = M1[8]*M2[1] + M1[9]*M2[5] + M1[10]*M2[9] + M1[11]*M2[13];
		product[10] = M1[8]*M2[2] + M1[9]*M2[6] + M1[10]*M2[10] + M1[11]*M2[14];
		product[11] = M1[8]*M2[3] + M1[9]*M2[7] + M1[10]*M2[11] + M1[11]*M2[15];
		product[12] = M1[12]*M2[0] + M1[13]*M2[4] + M1[14]*M2[8] + M1[15]*M2[12];
		product[13] = M1[12]*M2[1] + M1[13]*M2[5] + M1[14]*M2[9] + M1[15]*M2[13];
		product[14] = M1[12]*M2[2] + M1[13]*M2[6] + M1[14]*M2[10] + M1[15]*M2[14];
		product[15] = M1[12]*M2[3] + M1[13]*M2[7] + M1[14]*M2[11] + M1[15]*M2[15];
		return product;
	}
	public static double[] getMatrixProduct(double[] M1, float[] M2)
	{
		double[] product = new double[16];
		product[0] = M1[0]*M2[0] + M1[1]*M2[4] + M1[2]*M2[8] + M1[3]*M2[12];
		product[1] = M1[0]*M2[1] + M1[1]*M2[5] + M1[2]*M2[9] + M1[3]*M2[13];
		product[2] = M1[0]*M2[2] + M1[1]*M2[6] + M1[2]*M2[10] + M1[3]*M2[14];
		product[3] = M1[0]*M2[3] + M1[1]*M2[7] + M1[2]*M2[11] + M1[3]*M2[15];
		product[4] = M1[4]*M2[0] + M1[5]*M2[4] + M1[6]*M2[8] + M1[7]*M2[12];
		product[5] = M1[4]*M2[1] + M1[5]*M2[5] + M1[6]*M2[9] + M1[7]*M2[13];
		product[6] = M1[4]*M2[2] + M1[5]*M2[6] + M1[6]*M2[10] + M1[7]*M2[14];
		product[7] = M1[4]*M2[3] + M1[5]*M2[7] + M1[6]*M2[11] + M1[7]*M2[15];
		product[8] = M1[8]*M2[0] + M1[9]*M2[4] + M1[10]*M2[8] + M1[11]*M2[12];
		product[9] = M1[8]*M2[1] + M1[9]*M2[5] + M1[10]*M2[9] + M1[11]*M2[13];
		product[10] = M1[8]*M2[2] + M1[9]*M2[6] + M1[10]*M2[10] + M1[11]*M2[14];
		product[11] = M1[8]*M2[3] + M1[9]*M2[7] + M1[10]*M2[11] + M1[11]*M2[15];
		product[12] = M1[12]*M2[0] + M1[13]*M2[4] + M1[14]*M2[8] + M1[15]*M2[12];
		product[13] = M1[12]*M2[1] + M1[13]*M2[5] + M1[14]*M2[9] + M1[15]*M2[13];
		product[14] = M1[12]*M2[2] + M1[13]*M2[6] + M1[14]*M2[10] + M1[15]*M2[14];
		product[15] = M1[12]*M2[3] + M1[13]*M2[7] + M1[14]*M2[11] + M1[15]*M2[15];
		return product;
	}
	public static double[] getMatrixProduct(float[] M1, double[] M2)
	{
		double[] product = new double[16];
		product[0] = M1[0]*M2[0] + M1[1]*M2[4] + M1[2]*M2[8] + M1[3]*M2[12];
		product[1] = M1[0]*M2[1] + M1[1]*M2[5] + M1[2]*M2[9] + M1[3]*M2[13];
		product[2] = M1[0]*M2[2] + M1[1]*M2[6] + M1[2]*M2[10] + M1[3]*M2[14];
		product[3] = M1[0]*M2[3] + M1[1]*M2[7] + M1[2]*M2[11] + M1[3]*M2[15];
		product[4] = M1[4]*M2[0] + M1[5]*M2[4] + M1[6]*M2[8] + M1[7]*M2[12];
		product[5] = M1[4]*M2[1] + M1[5]*M2[5] + M1[6]*M2[9] + M1[7]*M2[13];
		product[6] = M1[4]*M2[2] + M1[5]*M2[6] + M1[6]*M2[10] + M1[7]*M2[14];
		product[7] = M1[4]*M2[3] + M1[5]*M2[7] + M1[6]*M2[11] + M1[7]*M2[15];
		product[8] = M1[8]*M2[0] + M1[9]*M2[4] + M1[10]*M2[8] + M1[11]*M2[12];
		product[9] = M1[8]*M2[1] + M1[9]*M2[5] + M1[10]*M2[9] + M1[11]*M2[13];
		product[10] = M1[8]*M2[2] + M1[9]*M2[6] + M1[10]*M2[10] + M1[11]*M2[14];
		product[11] = M1[8]*M2[3] + M1[9]*M2[7] + M1[10]*M2[11] + M1[11]*M2[15];
		product[12] = M1[12]*M2[0] + M1[13]*M2[4] + M1[14]*M2[8] + M1[15]*M2[12];
		product[13] = M1[12]*M2[1] + M1[13]*M2[5] + M1[14]*M2[9] + M1[15]*M2[13];
		product[14] = M1[12]*M2[2] + M1[13]*M2[6] + M1[14]*M2[10] + M1[15]*M2[14];
		product[15] = M1[12]*M2[3] + M1[13]*M2[7] + M1[14]*M2[11] + M1[15]*M2[15];
		return product;
	}
	public static double[] getMatrixProduct(double[] M1, double[] M2)
	{
		double[] product = new double[16];
		product[0] = M1[0]*M2[0] + M1[1]*M2[4] + M1[2]*M2[8] + M1[3]*M2[12];
		product[1] = M1[0]*M2[1] + M1[1]*M2[5] + M1[2]*M2[9] + M1[3]*M2[13];
		product[2] = M1[0]*M2[2] + M1[1]*M2[6] + M1[2]*M2[10] + M1[3]*M2[14];
		product[3] = M1[0]*M2[3] + M1[1]*M2[7] + M1[2]*M2[11] + M1[3]*M2[15];
		product[4] = M1[4]*M2[0] + M1[5]*M2[4] + M1[6]*M2[8] + M1[7]*M2[12];
		product[5] = M1[4]*M2[1] + M1[5]*M2[5] + M1[6]*M2[9] + M1[7]*M2[13];
		product[6] = M1[4]*M2[2] + M1[5]*M2[6] + M1[6]*M2[10] + M1[7]*M2[14];
		product[7] = M1[4]*M2[3] + M1[5]*M2[7] + M1[6]*M2[11] + M1[7]*M2[15];
		product[8] = M1[8]*M2[0] + M1[9]*M2[4] + M1[10]*M2[8] + M1[11]*M2[12];
		product[9] = M1[8]*M2[1] + M1[9]*M2[5] + M1[10]*M2[9] + M1[11]*M2[13];
		product[10] = M1[8]*M2[2] + M1[9]*M2[6] + M1[10]*M2[10] + M1[11]*M2[14];
		product[11] = M1[8]*M2[3] + M1[9]*M2[7] + M1[10]*M2[11] + M1[11]*M2[15];
		product[12] = M1[12]*M2[0] + M1[13]*M2[4] + M1[14]*M2[8] + M1[15]*M2[12];
		product[13] = M1[12]*M2[1] + M1[13]*M2[5] + M1[14]*M2[9] + M1[15]*M2[13];
		product[14] = M1[12]*M2[2] + M1[13]*M2[6] + M1[14]*M2[10] + M1[15]*M2[14];
		product[15] = M1[12]*M2[3] + M1[13]*M2[7] + M1[14]*M2[11] + M1[15]*M2[15];
		return product;
	}
	
	public static double[] getMatrixProduct3by3(double[] M1, double[] M2)
	{
		double[] M1Exp = getDoubleIdentity();
		double[] M2Exp = getDoubleIdentity();
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				M1Exp[4*i + j]  = M1[i*3 + j];
				M2Exp[4*i + j]  = M2[i*3 + j];
			}
		}
		return getMatrixProduct(M1Exp, M2Exp);
	}
	
	public static void applyTransformInc(double[] M1, double[] M2, Entity entity)
	{
    	PhysVector playerPos = new PhysVector(entity.posX, entity.posY, entity.posZ);
		PhysMatrix.applyTransform(M1, playerPos);
		PhysMatrix.applyTransform(M2, playerPos);
    	entity.setPosition(playerPos.X, playerPos.Y, playerPos.Z);
    	
    	PhysVector playerLastPos = new PhysVector(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ);
    	PhysMatrix.applyTransform(M1, playerLastPos);
    	PhysMatrix.applyTransform(M2, playerLastPos);
    	entity.lastTickPosX = playerLastPos.X;
    	entity.lastTickPosY = playerLastPos.Y;
    	entity.lastTickPosZ = playerLastPos.Z;
    	
    	PhysVector playerPrevPos = new PhysVector(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
    	PhysMatrix.applyTransform(M1, playerPrevPos);
    	PhysMatrix.applyTransform(M2, playerPrevPos);
    	entity.prevPosX = playerPrevPos.X;
    	entity.prevPosY = playerPrevPos.Y;
    	entity.prevPosZ = playerPrevPos.Z;
    	
    	Vec3D v = entity.getLookVec();
    	
    	if(v == null)
    	{
    		return;
    	}
    	PhysVector vecFormer = new PhysVector(v.xCoord, v.yCoord, v.zCoord);
    	PhysVector vecNew = vecFormer.getOriented(M1);
    	vecNew.orient(M2);
    	
    	PhysVector vecFXZ = new PhysVector(v.xCoord, 0, v.zCoord);
    	PhysVector vecNXZ = new PhysVector(vecNew.X, 0, vecNew.Z);
    	
    	vecFXZ.normalize();
    	vecNXZ.normalize();
    	
    	double dot = vecFXZ.dotProduct(vecNXZ);
    	double dir = vecNXZ.X*vecFXZ.Z - vecFXZ.X*vecNXZ.Z;
    	
    	double yawInc = 0;
		if(dot < -1)
		{
			dot = -1;
		}
		else if(dot > 1)
    	{
    		dot = 1;
    	}
    	if(dir < 0)
    	{
        	yawInc = (180/Math.PI)*Math.acos(dot);
    	}
    	else
    	{
        	yawInc = -(180/Math.PI)*Math.acos(dot);
    	}
    	
    	//entity.rotationPitch += pitchInc;
    	//entity.prevRotationPitch += pitchInc;
    	entity.rotationYaw += yawInc;
    	entity.prevRotationYaw += yawInc;
	}
	
	public static void applyTransform(double[] M, Entity entity)
	{
    	PhysVector playerPos = new PhysVector(entity.posX, entity.posY, entity.posZ);
		PhysMatrix.applyTransform(M, playerPos);
    	entity.setPosition(playerPos.X, playerPos.Y, playerPos.Z);
    	
    	PhysVector playerLastPos = new PhysVector(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ);
    	PhysMatrix.applyTransform(M, playerLastPos);
    	entity.lastTickPosX = playerLastPos.X;
    	entity.lastTickPosY = playerLastPos.Y;
    	entity.lastTickPosZ = playerLastPos.Z;
    	
    	PhysVector playerPrevPos = new PhysVector(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
    	PhysMatrix.applyTransform(M, playerPrevPos);
    	entity.prevPosX = playerPrevPos.X;
    	entity.prevPosY = playerPrevPos.Y;
    	entity.prevPosZ = playerPrevPos.Z;
    	
    	Vec3D v = entity.getLookVec();
    	
    	if(v == null)
    	{
    		return;
    	}
    	PhysVector vecFormer = new PhysVector(v.xCoord, v.yCoord, v.zCoord);
    	PhysVector vecNew = vecFormer.getOriented(M);
    	
    	PhysVector vecFXZ = new PhysVector(v.xCoord, 0, v.zCoord);
    	PhysVector vecNXZ = new PhysVector(vecNew.X, 0, vecNew.Z);
    	
    	vecFXZ.normalize();
    	vecNXZ.normalize();
    	
    	double dot = vecFXZ.dotProduct(vecNXZ);
    	
    	double dir = vecNXZ.X*vecFXZ.Z - vecFXZ.X*vecNXZ.Z;
    	
    	double yawInc = 0;
		if(dot < -1)
		{
			dot = -1;
		}
		else if(dot > 1)
    	{
    		dot = 1;
    	}
    	if(dir < 0)
    	{
        	yawInc = (180/Math.PI)*Math.acos(dot);
    	}
    	else
    	{
        	yawInc = -(180/Math.PI)*Math.acos(dot);
    	}
    	
    	//entity.rotationPitch += pitchInc;
    	//entity.prevRotationPitch += pitchInc;
    	entity.rotationYaw += yawInc;
    	entity.prevRotationYaw += yawInc;
	}
	
	public static void applyTransform(float[] M, Entity entity)
	{
    	PhysVector playerPos = new PhysVector(entity.posX, entity.posY, entity.posZ);
		PhysMatrix.applyTransform(M, playerPos);
    	entity.setPosition(playerPos.X, playerPos.Y, playerPos.Z);
    	
    	PhysVector playerLastPos = new PhysVector(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ);
    	PhysMatrix.applyTransform(M, playerLastPos);
    	entity.lastTickPosX = playerLastPos.X;
    	entity.lastTickPosY = playerLastPos.Y;
    	entity.lastTickPosZ = playerLastPos.Z;
    	
    	PhysVector playerPrevPos = new PhysVector(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
    	PhysMatrix.applyTransform(M, playerPrevPos);
    	entity.prevPosX = playerPrevPos.X;
    	entity.prevPosY = playerPrevPos.Y;
    	entity.prevPosZ = playerPrevPos.Z;
	}
	
	public static void applyTransform(float[] M, PhysVector vec)
	{
		double x = vec.X;
		double y = vec.Y;
		double z = vec.Z;
		
		vec.X = x*M[0] + y*M[1] + z*M[2] + 1*M[3];
		vec.Y = x*M[4] + y*M[5] + z*M[6] + 1*M[7];
		vec.Z = x*M[8] + y*M[9] + z*M[10] + 1*M[11];
	}
	public static void applyTransform(double[] M, PhysVector vec)
	{
		double x = vec.X;
		double y = vec.Y;
		double z = vec.Z;
		
		vec.X = x*M[0] + y*M[1] + z*M[2] + 1*M[3];
		vec.Y = x*M[4] + y*M[5] + z*M[6] + 1*M[7];
		vec.Z = x*M[8] + y*M[9] + z*M[10] + 1*M[11];
	}
	
	public static void applyTransform3by3(double[] M, PhysVector vec)
	{
		double x = vec.X;
		double y = vec.Y;
		double z = vec.Z;
		
		vec.X = x*M[0] + y*M[1] + z*M[2];
		vec.Y = x*M[3] + y*M[4] + z*M[5];
		vec.Z = x*M[6] + y*M[7] + z*M[8];
	}
	
	public static PhysVector get3by3TransformedVec(double[] M, PhysVector v)
	{
		PhysVector vec = copy(v);
		applyTransform3by3(M, vec);
		return vec;
	}
	
	public static PhysVector getTransformedVec(double[] M, PhysVector v)
	{
		PhysVector vec = copy(v);
		applyTransform(M, vec);
		return vec;
	}
	public static PhysVector copy(PhysVector vec)
	{
		return new PhysVector(vec.X, vec.Y, vec.Z);
	}
	
	public static void copyTransform(Entity entity1, Entity entity2)
	{
		entity2.setPosition(entity1.posX, entity1.posY, entity1.posZ);
    	entity2.lastTickPosX = entity1.lastTickPosX;
    	entity2.lastTickPosY = entity1.lastTickPosY;
    	entity2.lastTickPosZ = entity1.lastTickPosZ;
    	entity2.prevPosX = entity1.prevPosX;
    	entity2.prevPosY = entity1.prevPosY;
    	entity2.prevPosZ = entity1.prevPosZ;
    	entity2.rotationPitch = entity1.rotationPitch;
    	entity2.rotationYaw = entity1.rotationYaw;
    	entity2.prevRotationPitch = entity1.prevRotationPitch;
    	entity2.prevRotationYaw = entity1.prevRotationYaw;
	}

	public static double[] getTransforms(Entity entity)
	{
		double[] transforms = new double[14];

		transforms[0] = entity.posX;
		transforms[1] = entity.posY;
		transforms[2] = entity.posZ;
		transforms[3] = entity.lastTickPosX;
		transforms[4] = entity.lastTickPosY;
		transforms[5] = entity.lastTickPosZ;
		transforms[6] = entity.prevPosX;
		transforms[7] = entity.prevPosY;
		transforms[8] = entity.prevPosZ;
		transforms[9] = entity.rotationPitch;
		transforms[10] = entity.rotationYaw;
		transforms[11] = entity.prevRotationPitch;
		transforms[12] = entity.prevRotationYaw;
		
		return transforms;
	}
	
	public static void loadTransforms(Entity entity, double[] transforms)
	{
		entity.setPosition(transforms[0], transforms[1], transforms[2]);
		entity.lastTickPosX = transforms[3];
		entity.lastTickPosY = transforms[4];
		entity.lastTickPosZ = transforms[5];
		entity.prevPosX = transforms[6];
		entity.prevPosY = transforms[7];
		entity.prevPosZ = transforms[8];
		entity.rotationPitch = (float)transforms[9];
		entity.rotationYaw = (float)transforms[10];
		entity.prevRotationPitch = (float)transforms[11];
		entity.prevRotationYaw = (float)transforms[12];
	}
	
	public static void printElements(double[] matrix)
	{
		int dimension = (int)Math.sqrt(matrix.length);
		for(int i = 0; i < dimension; i++)
		{
			for(int j = 0; j < dimension; j++)
			{
				System.out.print(matrix[dimension*i + j] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static void printElements(float[] matrix)
	{
		for(int i = 0; i < 4; i++)
		{
			for(int j = 0; j < 4; j++)
			{
				System.out.print(matrix[4*i + j] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static PhysVector addVectors(PhysVector vec1, PhysVector vec2)
	{
		return new PhysVector(vec1.X + vec2.X, vec1.Y + vec2.Y, vec1.Z + vec2.Z);
	}
	
	public static PhysVector cross(PhysVector vec1, PhysVector vec2)
	{
		double x1 = vec1.X;
		double y1 = vec1.Y;
		double z1 = vec1.Z;
		double x2 = vec2.X;
		double y2 = vec2.Y;
		double z2 = vec2.Z;
		
		return new PhysVector(y1*z2-y2*z1, z1*x2-x1*z2, x1*y2-x2*y1);
	}
	
	public static void normalize(PhysVector vec)
	{
		double mag = Math.sqrt(vec.X*vec.X + vec.Y*vec.Y + vec.Z*vec.Z);
		vec.X /= mag;
		vec.Y /= mag;
		vec.Z /= mag;
	}
	
	public static void scaleVec(PhysVector vec, double scale)
	{
		vec.X *= scale;
		vec.Y *= scale;
		vec.Z *= scale;
	}
	
	public static PhysVector getScaledVec(PhysVector vec, double scale)
	{
		return new PhysVector(vec.X * scale, vec.Y * scale, vec.Z * scale);
	}
	
	public static double[] inverse3by3(double[] matrix)
	{
		//System.out.println(matrix);
		double[] inverse = new double[9];
		
		inverse[0] = matrix[4]*matrix[8] - matrix[5]*matrix[7];
		inverse[3] = matrix[5]*matrix[6] - matrix[3]*matrix[8];
		inverse[6] = matrix[3]*matrix[7] - matrix[4]*matrix[6];
		inverse[1] = matrix[2]*matrix[6] - matrix[1]*matrix[8];
		inverse[4] = matrix[0]*matrix[8] - matrix[2]*matrix[6];
		inverse[7] = matrix[6]*matrix[1] - matrix[0]*matrix[7];
		inverse[2] = matrix[1]*matrix[5] - matrix[2]*matrix[4];
		inverse[5] = matrix[2]*matrix[3] - matrix[0]*matrix[5];
		inverse[8] = matrix[0]*matrix[4] - matrix[1]*matrix[3];
		
		double det = matrix[0]*inverse[0] + matrix[1]*inverse[3] + matrix[2]*inverse[6];
		
		//System.out.println(det);
		
		for(int i = 0; i < 9; i++)
		{
			inverse[i] /= det;
		}
		//printElements(inverse);
		
		return inverse;
	}
	
	public static double[] inverse(double[] matrix)
	{
		double[] inverse = new double[16];
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				inverse[i*4 + j] = matrix[i + j*4];
			}
			inverse[i*4 + 3] = -inverse[i*4]*matrix[3] - inverse[i*4 + 1]*matrix[7] - inverse[i*4 + 2]*matrix[11];
		}

		inverse[12] = 0;
		inverse[13] = 0;
		inverse[14] = 0;
		inverse[15] = 1;
		
		return inverse;
	}
	
	public static double[] getScaledMatrix(double[] matrix, PhysVector scaleVec)
	{
		double[] scaledMatrix = new double[16];
		double[] scale = {scaleVec.X, scaleVec.Y, scaleVec.Z};
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				scaledMatrix[i*4 + j] = matrix[i*4 + j]*scale[j];
			}
			scaledMatrix[i*4 + 3] = matrix[i*4 + 3];
		}
		
		scaledMatrix[12] = matrix[12];
		scaledMatrix[13] = matrix[13];
		scaledMatrix[14] = matrix[14];
		scaledMatrix[15] = matrix[15];
		return scaledMatrix;
	}
	
	public static void scaleMatrix(double[] matrix, PhysVector scaleVec)
	{
		double[] scale = {scaleVec.X, scaleVec.Y, scaleVec.Z};
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				matrix[i*4 + j] = matrix[i*4 + j]*scale[j];
			}
		}
	}
	
	public static double[] inverse(double[] matrix, PhysVector scaleVec)
	{
		double[] inverse = new double[16];
		double[] scale = {scaleVec.X, scaleVec.Y, scaleVec.Z};
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				inverse[i*4 + j] = matrix[i + j*4]/(scale[i]*scale[i]);
			}
			inverse[i*4 + 3] = -inverse[i*4]*matrix[3] - inverse[i*4 + 1]*matrix[7] - inverse[i*4 + 2]*matrix[11];
		}
		
		inverse[12] = 0;
		inverse[13] = 0;
		inverse[14] = 0;
		inverse[15] = 1;
		
		return inverse;
		
	}
	
	public static boolean isNaN(double[] M)
	{
		for(int i = 0; i < M.length; i++)
		{
			if(Double.isNaN(M[i]))
			{
				return true;
			}
		}
		return false;
	}
	
	public static double[] copy(double[] M)
	{
		double[] c = new double[M.length];
		for(int i = 0; i < M.length; i++)
		{
			c[i] = M[i];
		}
		return c;
	}
	
	public static void pushSubMatrix(PhysWorld subWorld, double x, double y, double z)
	{
    	GL11.glPushMatrix();
    	GL11.glTranslatef(-(float)x, -(float)y, -(float)z);
        FloatBuffer buff1 = BufferUtils.createFloatBuffer(16);
    	buff1.put(PhysMatrix.transpose(PhysMatrix.convertToFloat(subWorld.lToWTransform)));
    	buff1.flip();
    	GL11.glMultMatrix(buff1);
    	GL11.glTranslatef((float)x, (float)y, (float)z);
	}
	
}

