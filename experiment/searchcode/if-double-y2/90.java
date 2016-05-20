package net.minecraft.src;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class PhysVector implements Serializable{
	
	static final long serialVersionUID = 6798764215885406828L;

	public final static PhysVector ZERO = new PhysVector(0, 0, 0);
	public final static PhysVector right = new PhysVector(1.0, 0.0, 0.0);
	public final static PhysVector up = new PhysVector(0.0, 1.0, 0.0);
	public final static PhysVector forward = new PhysVector(0.0, 0.0, 1.0);
	
    public double X;
    public double Y;
    public double Z;
    
    public PhysVector(double x, double y, double z)
    {
    	X = x;
    	Y = y;
    	Z = z;
    }
    
    public PhysVector(PhysVector v)
    {
    	X = v.X;
    	Y = v.Y;
    	Z = v.Z;
    }

    public PhysVector getSubtraction(PhysVector v)
    {
        return new PhysVector(v.X - X, v.Y - Y, v.Z - Z);
    }
    
    public void subtract(PhysVector v)
    {
        X -= v.X;
        Y -= v.Y;
        Z -= v.Z;
    }
    
    public void subtract(double x, double y, double z)
    {
        X -= x;
        Y -= y;
        Z -= z;
    }
    
    public void add(PhysVector v)
    {
        X += v.X;
        Y += v.Y;
        Z += v.Z;
    }
    
    public void add(double x, double y, double z)
    {
    	X += x;
    	Y += y;
    	Z += z;
    }
    
    public PhysVector plus(PhysVector v)
    {
    	return new PhysVector(v.X + X, v.Y + Y, v.Z + Z);
    }
    
    public PhysVector minus(PhysVector v)
    {
    	return new PhysVector(X - v.X, Y - v.Y, Z - v.Z);
    }
    
    public PhysVector times(double scaler)
    {
    	return new PhysVector(X*scaler, Y*scaler, Z*scaler);
    }
    
    public double dot(PhysVector v)
    {
    	return X*v.X + Y*v.Y + Z*v.Z;
    }
    
    public PhysVector cross(PhysVector v)
    {
    	return new PhysVector(Y*v.Z-v.Y*Z, Z*v.X-X*v.Z, X*v.Y-v.X*Y);
    }
    
    public static PhysVector getSum(PhysVector v1, PhysVector v2)
    {
    	return new PhysVector(v1.X + v2.X, v1.Y + v2.Y, v1.Z + v2.Z);
    }
    
    public static PhysVector getSum(PhysVector v1, PhysVector v2, PhysVector v3)
    {
    	return new PhysVector(v1.X + v2.X + v3.X, v1.Y + v2.Y + v3.Y, v1.Z + v2.Z + v3.Z);
    }
    
    public static PhysVector getSum(PhysVector v1, PhysVector v2, PhysVector v3, PhysVector v4)
    {
    	return new PhysVector(v1.X + v2.X + v3.X + v4.X, v1.Y + v2.Y + v3.Y + v4.Y, v1.Z + v2.Z + v3.Z + v4.Z);
    }
    
    public static PhysVector getDifference(PhysVector vec1, PhysVector vec2)
    {
    	return new PhysVector(vec1.X - vec2.X, vec1.Y - vec2.Y, vec1.Z - vec2.Z);
    }
    
    public void multiply(double scale)
    {
    	X *= scale;
    	Y *= scale;
    	Z *= scale;
    }

    public PhysVector getProduct(double scale)
    {
    	return new PhysVector(X*scale, Y*scale, Z*scale);
    }
    
    public Vec3D convertToVec3D()
    {
    	return Vec3D.createVectorHelper(X, Y, Z);
    }
    
    public static PhysVector convertToVector(Vec3D vec)
    {
    	return new PhysVector(vec.xCoord, vec.yCoord, vec.zCoord);
    }
    
    public void normalize()
    {
        double d = MathHelper.sqrt_double(X * X + Y * Y + Z * Z);

        if (d < 0.0001D)
        {
            X = 0.0D;
            Y = 0.0D;
            Z = 0.0D;
        }
        else
        {
            X /= d;
            Y /= d;
            Z /= d;
        }
    }
    
    public PhysVector getNormalized()
    {
    	PhysVector unitVector = new PhysVector(this);
    	unitVector.normalize();
    	return unitVector;
    }
    
    public double dotProduct(PhysVector vector)
    {
        return X * vector.X + Y * vector.Y + Z * vector.Z;
    }
    
    public double length()
    {
        return Math.sqrt(X * X + Y * Y + Z * Z);
    }
    
    public double lengthSq()
    {
        return X * X + Y * Y + Z * Z;
    }
    
    public void zero()
    {
    	X = 0;
    	Y = 0;
    	Z = 0;
    }
    
    public void orient(double[] transform)
    {
    	PhysVector origin = new PhysVector(0, 0, 0);
    	PhysMatrix.applyTransform(transform, origin);
    	PhysMatrix.applyTransform(transform, this);
    	subtract(origin);
    }
    
    public PhysVector getOriented(double[] transform)
    {
    	PhysVector vec = new PhysVector(this);
    	vec.orient(transform);
    	return vec;
    }
    
    public static PhysVector getCross(PhysVector vec1, PhysVector vec2)
	{
		double x1 = vec1.X;
		double y1 = vec1.Y;
		double z1 = vec1.Z;
		double x2 = vec2.X;
		double y2 = vec2.Y;
		double z2 = vec2.Z;
		
		return new PhysVector(y1*z2-y2*z1, z1*x2-x1*z2, x1*y2-x2*y1);
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
    
    public static PhysVector unitVector(int axis)
    {
    	if(axis == 0)
    	{
    		return new PhysVector(PhysVector.right);
    	}
    	else if(axis == 1)
    	{
    		return new PhysVector(PhysVector.up);
    	}
    	else
    	{
    		return new PhysVector(PhysVector.forward);
    	}
    }
    
    public String toString()
    {
    	String coords = new String("<" + X + ", " + Y + ", " + Z + ">");
    	return coords;
    }
    
    public static double distance(PhysVector v1, PhysVector v2)
    {
    	return PhysVector.getDifference(v1, v2).length();
    }
    
    public PhysVector copy()
    {
    	return new PhysVector(X, Y, Z);
    }
    
    public void moveToBlockSide(int sideHit)
    {
    	if (sideHit == 0)
        {
        	X += 0.5;
        	Z += 0.5;
        }

        if (sideHit == 1)
        {
        	X += 0.5;
        	Y += 1.0;
        	Z += 0.5;
        }

        if (sideHit == 2)
        {
        	X += 0.5;
        	Y += 0.5;
        }

        if (sideHit == 3)
        {
        	X += 0.5;
        	Y += 0.5;
        	Z += 1.0;
        }

        if (sideHit == 4)
        {
        	Y += 0.5;
        	Z += 0.5;
        }

        if (sideHit == 5)
        {
        	X += 1.0;
        	Y += 0.5;
        	Z += 0.5;
        }
    }
    
    public boolean equals(PhysVector v)
    {
    	if(v.X != X)
    	{
    		return false;
    	}
    	if(v.Y != Y)
    	{
    		return false;
    	}
    	if(v.Z != Z)
    	{
    		return false;
    	}
    	return true;
    }
    
    public PhysVector getChunkCoords()
    {
    	PhysVector chunk = new PhysVector(X / 16.0, Y / 16.0, Z / 16.0);
    	return chunk.getFloor();
    }
    
    public PhysVector getFloor()
    {
    	return new PhysVector(MathHelper.floor_double(X), MathHelper.floor_double(Y), MathHelper.floor_double(Z));
    }
    
    public void floor()
    {
    	X = MathHelper.floor_double(X);
    	Y = MathHelper.floor_double(Y);
    	Z = MathHelper.floor_double(Z);
    }
    
    public byte[] toByteArray()
    {
    	ByteBuffer buffer = ByteBuffer.allocate(24);
    	buffer.putLong(Double.doubleToLongBits(X));
    	buffer.putLong(Double.doubleToLongBits(Y));
    	buffer.putLong(Double.doubleToLongBits(Z));

    	return buffer.array();
    }
    
    public void readByteArray(byte[] array, int start)
    {
    	ByteBuffer buffer = ByteBuffer.wrap(array, start, 24);
    	X = buffer.getDouble();
    	Y = buffer.getDouble();
    	Z = buffer.getDouble();
    }
    
    public void addNoise(double range)
    {
    	X += range*(Math.random() - 0.5);
    	Y += range*(Math.random() - 0.5);
    	Z += range*(Math.random() - 0.5);
    }
    
    public PhysVector getNoisy(double range)
    {
    	return new PhysVector(X + range*(Math.random() - 0.5), Y + range*(Math.random() - 0.5), Z + range*(Math.random() - 0.5));
    }
    
    public boolean inBox(AxisAlignedBB box)
    {
    	return (X > box.minX && X < box.maxX) ||(Y > box.minY && Y < box.maxY) || (Z > box.minZ && Z < box.maxZ);
    }
    
    public void addToBox(AxisAlignedBB box)
    {
    	if(X > box.maxX)
    	{
    		box.maxX = X;
    	}
    	else if(X < box.minX)
    	{
    		box.minX = X;
    	}
    	if(Y > box.maxY)
    	{
    		box.maxY = Y;
    	}
    	else if(Y < box.minY)
    	{
    		box.minY = Y;
    	}
    	if(Z > box.maxZ)
    	{
    		box.maxZ = Z;
    	}
    	else if(Z < box.minZ)
    	{
    		box.minZ = Z;
    	}
    }
    
    public boolean lessThan(PhysVector v)
    {
    	return X < v.X && Y < v.Y && Z < v.Z;
    }
   
    public boolean greaterThan(PhysVector v)
    {
    	return X > v.X && Y > v.Y && Z > v.Z;
    }
    
    public boolean componentLessThan(PhysVector v)
    {
    	return X < v.X || Y < v.Y || Z < v.Z;
    }
   
    public boolean componentGreaterThan(PhysVector v)
    {
    	return X > v.X || Y > v.Y || Z > v.Z;
    }
    
    public boolean componentGreaterThanOrEqualTo(PhysVector v)
    {
    	return X >= v.X || Y >= v.Y || Z >= v.Z;
    }
    
    public PhysVector getCeil()
    {
    	return new PhysVector(Math.ceil(X), Math.ceil(Y), Math.ceil(Z));
    }
    
    public PhysIntVector ceilInt()
    {
    	PhysVector ceil = getCeil();
    	return new PhysIntVector((int)ceil.X, (int)ceil.Y, (int)ceil.Z);
    }
    
    public PhysIntVector floorInt()
    {
    	PhysVector floor = getFloor();
    	return new PhysIntVector((int)floor.X, (int)floor.Y, (int)floor.Z);
    }
}

