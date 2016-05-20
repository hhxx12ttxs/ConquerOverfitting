package net.minecraft.src;

import java.util.*;

public class PhysAxisAlignedBB extends AxisAlignedBB
{
	public PhysVector[] corners = new PhysVector[8];
	public double[] transform;
	public double[] invTransform;
	public double[] transformInc;
	public boolean check;
	public double relief = 0;
	public double velocity = 0;
	public int worldIndex;
	
	public static int[][] edges = new int[8][3];
	static
	{
		edges[0][0] = 1;
		edges[0][1] = 2;
		edges[0][2] = 4;
		edges[1][0] = 3;
		edges[1][1] = 5;
		edges[1][2] = 0;
		edges[2][0] = 0;
		edges[2][1] = 3;
		edges[2][2] = 6;
		edges[3][0] = 1;
		edges[3][1] = 2;
		edges[3][2] = 7;
		edges[4][0] = 0;
		edges[4][1] = 6;
		edges[4][2] = 5;
		edges[5][0] = 1;
		edges[5][1] = 4;
		edges[5][2] = 7;
		edges[6][0] = 2;
		edges[6][1] = 4;
		edges[6][2] = 7;
		edges[7][0] = 3;
		edges[7][1] = 5;
		edges[7][2] = 6;
	}
	
	public PhysAxisAlignedBB(AxisAlignedBB bb, double[] transformation, double[] inverseTransformation, boolean skipTransformation, int index)
	{
		super(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
		invTransform = inverseTransformation;
		updateCorners(skipTransformation, transformation);
		worldIndex = index;
	}
	
	public void updateCorners(boolean skipRotation, double[] transformation)
	{
		corners[0] = new PhysVector(minX, minY, minZ);
    	corners[1] = new PhysVector(minX, maxY, minZ);
    	corners[2] = new PhysVector(minX, minY, maxZ);
    	corners[3] = new PhysVector(minX, maxY, maxZ);
    	corners[4] = new PhysVector(maxX, minY, minZ);
    	corners[5] = new PhysVector(maxX, maxY, minZ);
    	corners[6] = new PhysVector(maxX, minY, maxZ);
    	corners[7] = new PhysVector(maxX, maxY, maxZ);
    	setTransformation(transformation, skipRotation);
	}
	
	public void setTransformation(double[] transformation, boolean skipRotation)
	{
		transform = transformation;
		if(skipRotation)
		{
			return;
		}
		
		for(int i = 0; i < 8; i++)
    	{
    		PhysMatrix.applyTransform(transform, corners[i]);
    	}
	}

    public AxisAlignedBB offset(double par1, double par3, double par5)
    {
        super.offset(par1, par3, par5);
        updateCorners(false, transform);
        
        return this;
    }
    
    public void offsetCorners(double x, double y, double z)
    {
    	for(int i = 0; i < 8; i++)
    	{
    		corners[i].X += x;
    		corners[i].Y += y;
    		corners[i].Z += z;
    	}
    }
    
    public static boolean connected(AxisAlignedBB bb1, AxisAlignedBB bb2)
    {
    	return connectedInX(bb1, bb2) || connectedInY(bb1, bb2) || connectedInZ(bb1, bb2);
    }
    
    public static boolean connectedInX(AxisAlignedBB bb1, AxisAlignedBB bb2)
    {
    	return intersectInX(bb1, bb2) && areXAligned(bb1, bb2);
    }
    public static boolean connectedInY(AxisAlignedBB bb1, AxisAlignedBB bb2)
    {
    	return intersectInY(bb1, bb2) && areYAligned(bb1, bb2);
    }
    public static boolean connectedInZ(AxisAlignedBB bb1, AxisAlignedBB bb2)
    {
    	return intersectInZ(bb1, bb2) && areZAligned(bb1, bb2);
    }
    
    public static boolean intersectInX(AxisAlignedBB bb1, AxisAlignedBB bb2)
    {
    	return (bb1.maxX >= bb2.minX && bb1.maxX < bb2.maxX) || (bb1.minX > bb2.minX && bb1.minX <= bb2.maxX);
    }
    public static boolean intersectInY(AxisAlignedBB bb1, AxisAlignedBB bb2)
    {
    	return (bb1.maxY >= bb2.minY && bb1.maxY < bb2.maxY) || (bb1.minY > bb2.minY && bb1.minY <= bb2.maxY);
    }
    public static boolean intersectInZ(AxisAlignedBB bb1, AxisAlignedBB bb2)
    {
    	return (bb1.maxZ >= bb2.minZ && bb1.maxZ < bb2.maxZ) || (bb1.minZ > bb2.minZ && bb1.minZ <= bb2.maxZ);
    }
    
    public static boolean areXAligned(AxisAlignedBB bb1, AxisAlignedBB bb2)
    {
    	return bb1.minY == bb2.minY && bb1.minZ == bb2.minZ && bb1.maxY == bb2.maxY && bb1.maxZ == bb2.maxZ;
    }
    public static boolean areYAligned(AxisAlignedBB bb1, AxisAlignedBB bb2)
    {
    	return bb1.minX == bb2.minX && bb1.minZ == bb2.minZ && bb1.maxX == bb2.maxX && bb1.maxZ == bb2.maxZ;
    }
    public static boolean areZAligned(AxisAlignedBB bb1, AxisAlignedBB bb2)
    {
    	return bb1.minX == bb2.minX && bb1.minY == bb2.minY && bb1.maxX == bb2.maxX && bb1.maxY == bb2.maxY;
    }
    
    public static AxisAlignedBB getFusedBoundingBox(AxisAlignedBB bb1, AxisAlignedBB bb2)
    {
    	AxisAlignedBB fusedBB = new AxisAlignedBB(bb1.minX, bb1.minY, bb1.minZ, bb1.maxX, bb1.maxY, bb1.maxZ);
    	if(bb2.minX < fusedBB.minX)
    	{
    		fusedBB.minX = bb2.minX;
    	}
    	if(bb2.minY < fusedBB.minY)
    	{
    		fusedBB.minY = bb2.minY;
    	}
    	if(bb2.minZ < fusedBB.minZ)
    	{
    		fusedBB.minZ = bb2.minZ;
    	}
    	if(bb2.maxX > fusedBB.maxX)
    	{
    		fusedBB.maxX = bb2.maxX;
    	}
    	if(bb2.maxY > fusedBB.maxY)
    	{
    		fusedBB.maxY = bb2.maxY;
    	}
    	if(bb2.maxZ > fusedBB.maxZ)
    	{
    		fusedBB.maxZ = bb2.maxZ;
    	}
    	return fusedBB;
    }
    
    public boolean intersectsWith(AxisAlignedBB par1AxisAlignedBB)
    {
    	double miX = par1AxisAlignedBB.minX;
    	double miY = par1AxisAlignedBB.minY;
    	double miZ = par1AxisAlignedBB.minZ;
    	double maX = par1AxisAlignedBB.maxX;
    	double maY = par1AxisAlignedBB.maxY;
    	double maZ = par1AxisAlignedBB.maxZ;
    	for(int i = 0; i < 8; i++)
    	{
    		PhysVector c = corners[i];
    		double x = c.X;
    		double y = c.Y;
    		double z = c.Z;
    		if(x > miX && x < maX)
    		{
    			return true;
    		}
    		if(y > miY && y < maY)
    		{
    			return true;
    		}
    		if(x > miZ && z < maZ)
    		{
    			return true;
    		}
    	}
    	return false;
    }
    
    public boolean isVecInside(Vec3D par1Vector)
    {
    	PhysVector vec = PhysVector.convertToVector(par1Vector);
    	PhysMatrix.applyTransform(transform, vec);
		
		return super.isVecInside(vec.convertToVec3D());
    }
    
    public void redirectMovementOneWay(AxisAlignedBB par1AxisAlignedBB, PhysVector move)
    {
    	PhysVector origin = new PhysVector(0, 0, 0);
    	PhysMatrix.applyTransform(invTransform, origin);
    	PhysMatrix.applyTransform(invTransform, move);
    	PhysVector relMove = origin.getSubtraction(move);
    	
    	PhysAxisAlignedBB par1Rotated = new PhysAxisAlignedBB(par1AxisAlignedBB, invTransform, transform, false, worldIndex);
    	
    	par1Rotated.offsetCorners(relMove.X, relMove.Y, relMove.Z);
    	surface(par1Rotated, relMove);
    	
    	origin = new PhysVector(0, 0, 0);
    	PhysMatrix.applyTransform(transform, origin);
    	PhysMatrix.applyTransform(transform, relMove);
    	PhysVector newMove = origin.getSubtraction(relMove);

    	move.X = newMove.X;
    	move.Y = newMove.Y;
    	move.Z = newMove.Z;
    }
    
    public void redirectMovementTwoWay(AxisAlignedBB par1AxisAlignedBB, PhysVector move)
    {
    	redirectMovementOneWay(par1AxisAlignedBB, move);
    	
    	PhysAxisAlignedBB par1Rotated = new PhysAxisAlignedBB(par1AxisAlignedBB, PhysMatrix.getDoubleIdentity(), PhysMatrix.getDoubleIdentity(), false, worldIndex);

    	par1Rotated.redirectMovementOneWay(this, move);
    }
    
    public void surface(PhysAxisAlignedBB bb, PhysVector move)
    {
    	double moveXPos = 0;
    	double moveYPos = 0;
    	double moveZPos = 0;
    	double moveXNeg = 0;
    	double moveYNeg = 0;
    	double moveZNeg = 0;
    	for(int i = 0; i < 8; i++)
    	{
    		PhysVector vec = bb.corners[i];
	    	if(vec.X > minX && vec.X < maxX)
	    	{
	    		double pos = maxX - vec.X;
	    		double neg = minX - vec.X;
	    		if(pos > moveXPos)
	    		{
	    			moveXPos = pos;
	    		}
	    		if(-neg > -moveXNeg)
	    		{
	    			moveXNeg = neg;
	    		}
	    	}
	    	if(vec.Y > minY && vec.Y < maxY)
	    	{
	    		double pos = maxY - vec.Y;
	    		double neg = minY - vec.Y;
	    		if(pos > moveYPos)
	    		{
	    			moveYPos = pos;
	    		}
	    		if(-neg > -moveYNeg)
	    		{
	    			moveYNeg = neg;
	    		}
	    	}
	    	if(vec.Z > minZ && vec.Z < maxZ)
	    	{
	    		double pos = maxZ - vec.Z;
	    		double neg = minZ - vec.Z;
	    		if(pos > moveZPos)
	    		{
	    			moveZPos = pos;
	    		}
	    		if(-neg > -moveZNeg)
	    		{
	    			moveZNeg = neg;
	    		}
	    	}
    	}
	    	
    	double moveX = 0;
    	double moveY = 0;
    	double moveZ = 0;
    	
    	if(-moveXNeg < moveXPos)
    	{
    		moveX = moveXNeg;
    	}
    	else
    	{
    		moveX = moveXPos;
    	}
    	if(-moveYNeg < moveYPos)
    	{
    		moveY = moveYNeg;
    	}
    	else
    	{
    		moveY = moveYPos;
    	}
    	if(-moveZNeg < moveZPos)
    	{
    		moveZ = moveZNeg;
    	}
    	else
    	{
    		moveZ = moveZPos;
    	}
    	

		if(check)
		{
			//System.out.println(moveY);
		}
    	
    	if(Math.abs(moveX) < Math.abs(moveY))
    	{
    		if(Math.abs(moveX) < Math.abs(moveZ))
    		{
    			move.X += moveX;
    		}
    		else
    		{
    			if(move.Z > 0 && moveY < 0)
    			{
    				move.Z += moveZ;
    			}
    			bb.offsetCorners(0, 0, moveZ);
    		}
    	}
    	else if(Math.abs(moveY) < Math.abs(moveZ))
    	{
			move.Y += moveY;
    	}
    	else
    	{
			move.Z += moveZ;
    	}
    }
    
    public double calculateXOffset(PhysAxisAlignedBB bb, double move)
    {
    	for(int i = 0; i < 8; i++)
    	{
    		move = calculateXOffset(bb, i, move);
    	}
    	return move;
    }
    public double calculateYOffset(PhysAxisAlignedBB bb, double move)
    {
    	for(int i = 0; i < 8; i++)
    	{
    		move = calculateYOffset(bb, i , move);
    	}
    	return move;
    }
    public double calculateZOffset(PhysAxisAlignedBB bb, double move)
    {
    	for(int i = 0; i < 8; i++)
    	{
    		move = calculateZOffset(bb, i, move);
    	}
    	return move;
    }

    public double edgeXOffset(PhysVector corner1, PhysVector corner2, double move)
    {
    	if(true)
    		return move;
    	double xShift = corner2.X - corner1.X;
    	double yShift = corner2.Y - corner1.Y;
    	double zShift = corner2.Z - corner1.Z;
    	if(corner1.Y > maxY && corner2.Y < maxY)
    	{
    		double interpolater = (maxY - corner1.Y)/yShift;
    		PhysVector contact = new PhysVector(corner1.X + xShift*interpolater, corner1.Y + yShift*interpolater, corner1.Z + zShift*interpolater);
    		move = calculateXOffset(contact, move);
    	}
    	if(corner1.Y < minY && corner2.Y > minY)
    	{
    		double interpolater = (minY - corner1.Y)/yShift;
    		PhysVector contact = new PhysVector(corner1.X + xShift*interpolater, corner1.Y + yShift*interpolater, corner1.Z + zShift*interpolater);
    		move = calculateXOffset(contact, move);
    	}
    	if(corner1.Z > maxZ && corner2.Z < maxZ)
    	{
    		double interpolater = (maxZ - corner1.Z)/zShift;
    		PhysVector contact = new PhysVector(corner1.X + xShift*interpolater, corner1.Y + yShift*interpolater, corner1.Z + zShift*interpolater);
    		move = calculateXOffset(contact, move);
    	}
    	if(corner1.Z < minZ && corner2.Z > minZ)
    	{
    		double interpolater = (minZ - corner1.Z)/zShift;
    		PhysVector contact = new PhysVector(corner1.X + xShift*interpolater, corner1.Y + yShift*interpolater, corner1.Z + zShift*interpolater);
    		move = calculateXOffset(contact, move);
    	}
    	return move;
    }
    public double edgeYOffset(PhysVector corner1, PhysVector corner2, double move)
    {
    	if(true)
    		return move;
    	double xShift = corner2.X - corner1.X;
    	double yShift = corner2.Y - corner1.Y;
    	double zShift = corner2.Z - corner1.Z;
    	if(corner1.X > maxX && corner2.X < maxX)
    	{
    		double interpolater = (maxX - corner1.X)/xShift;
    		PhysVector contact = new PhysVector(corner1.X + xShift*interpolater, corner1.Y + yShift*interpolater, corner1.Z + zShift*interpolater);
    		move = calculateYOffset(contact, move);
    	}
    	if(corner1.X < minX && corner2.X > minX)
    	{
    		double interpolater = (minX - corner1.X)/xShift;
    		PhysVector contact = new PhysVector(corner1.X + xShift*interpolater, corner1.Y + yShift*interpolater, corner1.Z + zShift*interpolater);
    		move = calculateYOffset(contact, move);
    	}
    	if(corner1.Z > maxZ && corner2.Z < maxZ)
    	{
    		double interpolater = (maxZ - corner1.Z)/zShift;
    		PhysVector contact = new PhysVector(corner1.X + xShift*interpolater, corner1.Y + yShift*interpolater, corner1.Z + zShift*interpolater);
    		move = calculateYOffset(contact, move);
    	}
    	if(corner1.Z < minZ && corner2.Z > minZ)
    	{
    		double interpolater = (minZ - corner1.Z)/zShift;
    		PhysVector contact = new PhysVector(corner1.X + xShift*interpolater, corner1.Y + yShift*interpolater, corner1.Z + zShift*interpolater);
    		move = calculateYOffset(contact, move);
    	}
    	return move;
    }
    public double edgeZOffset(PhysVector corner1, PhysVector corner2, double move)
    {
    	if(true)
    		return move;
    	double xShift = corner2.X - corner1.X;
    	double yShift = corner2.Y - corner1.Y;
    	double zShift = corner2.Z - corner1.Z;
		
    	if(corner1.X > maxX && corner2.X < maxX)
    	{
    		double interpolater = (maxX - corner1.X)/xShift;
    		PhysVector contact = new PhysVector(corner1.X + xShift*interpolater, corner1.Y + yShift*interpolater, corner1.Z + zShift*interpolater);
    		move = calculateZOffset(contact, move);
    	}
    	if(corner1.X < minX && corner2.X > minX)
    	{
    		double interpolater = (minX - corner1.X)/xShift;
    		PhysVector contact = new PhysVector(corner1.X + xShift*interpolater, corner1.Y + yShift*interpolater, corner1.Z + zShift*interpolater);
    		move = calculateZOffset(contact, move);
    	}
    	if(corner1.Y > maxY && corner2.Y < maxY)
    	{
    		double interpolater = (maxY - corner1.Y)/yShift;
    		PhysVector contact = new PhysVector(corner1.X + xShift*interpolater, corner1.Y + yShift*interpolater, corner1.Z + zShift*interpolater);
    		move = calculateZOffset(contact, move);
    	}
    	if(corner1.Y < minY && corner2.Y > minY)
    	{
    		double interpolater = (minY - corner1.Y)/yShift;
    		PhysVector contact = new PhysVector(corner1.X + xShift*interpolater, corner1.Y + yShift*interpolater, corner1.Z + zShift*interpolater);
    		move = calculateZOffset(contact, move);
    	}
    	return move;
    }
	
    public double calculateXOffset(PhysAxisAlignedBB bb, int cornerIndex, double move)
    {
    	PhysVector corner = bb.corners[cornerIndex];
    	PhysVector vec = PhysMatrix.copy(corner);
    	vec.X += move;
    	if (vec.Y <= minY + relief || vec.Y >= maxY - relief || vec.Z <= minZ + relief || vec.Z >= maxZ - relief)
        {
    		move = edgeXOffset(vec, bb.corners[edges[cornerIndex][0]], move);
    		move = edgeXOffset(vec, bb.corners[edges[cornerIndex][1]], move);
    		move = edgeXOffset(vec, bb.corners[edges[cornerIndex][2]], move);
            return move;
        }
        
        if (move > 0.0D)
        {
            double d = minX - vec.X;

            if (d < move)
            {
                move = d;
            }
        }

        if (move < 0.0D)
        {
            double d1 = maxX - vec.X;

            if (d1 > move)
            {
                move = d1;
            }
        }
    	
        return move;
    }
    public double calculateYOffset(PhysAxisAlignedBB bb, int cornerIndex, double move)
    {
    	PhysVector vec = bb.corners[cornerIndex];
        if (vec.X <= minX + relief || vec.X >= maxX - relief || vec.Z <= minZ + relief || vec.Z >= maxZ - relief)
        {
    		move = edgeYOffset(vec, bb.corners[edges[cornerIndex][0]], move);
    		move = edgeYOffset(vec, bb.corners[edges[cornerIndex][1]], move);
    		move = edgeYOffset(vec, bb.corners[edges[cornerIndex][2]], move);
            return move;
        }
        
        if (move > 0.0D)
        {
            double d = minY - vec.Y;

            if (d < move)
            {
            	if(d < -.5)
            	{
            		d += 1;
            		if(d > move)
            		{
            			move = d;
            		}
            	}
            	else
            	{
            		move = d;
            	}
            }
        }

        if (move < 0.0D)
        {
            double d1 = maxY - vec.Y;

            if (d1 > move)
            {
            	if(d1 > .5)
            	{
            		d1 -= d1;
            		if(d1 < move)
            		{
            			move = d1;
            		}
            	}
            	else
            	{
                    move = d1;
            	}
            }
        }

        return move;
    }
    public double calculateZOffset(PhysAxisAlignedBB bb, int cornerIndex, double move)
    {
    	PhysVector vec = bb.corners[cornerIndex];
    	
        if (vec.X <= minX + relief || vec.X >= maxX - relief || vec.Y <= minY + relief || vec.Y >= maxY - relief)
        {
    		move = edgeZOffset(vec, bb.corners[edges[cornerIndex][0]], move);
    		move = edgeZOffset(vec, bb.corners[edges[cornerIndex][1]], move);
    		move = edgeZOffset(vec, bb.corners[edges[cornerIndex][2]], move);
            return move;
        }

        if (move > 0.0D)
        {
            double d = minZ - vec.Z;

            if (d < move)
            {
                move = d;
            }
        }

        if (move < 0.0D)
        {
            double d1 = maxZ - vec.Z;

            if (d1 > move)
            {
                move = d1;
            }
        }
        
        return move;
    }
    
    public double calculateXOffset(PhysVector vec, double move)
    {
    	if ((vec.Y == minY || vec.Y == maxY) && (vec.Z < minZ + relief || vec.Z > maxZ - relief))
        {
            return move;
        }
    	if ((vec.Y < minY + relief || vec.Y > maxY - relief) && (vec.Z == minZ || vec.Z == maxZ))
        {
            return move;
        }
        
        double mid = (minX + maxX)/2;
        
        if (move > 0.0D && vec.X < mid)
        {
            double d = minX - vec.X;

            if (d < move)
            {
                move = d;
            }
        }

        if (move < 0.0D && vec.X > mid)
        {
            double d1 = maxX - vec.X;

            if (d1 > move)
            {
                move = d1;
            }
        }
    	
        return move;
    }
    public double calculateYOffset(PhysVector vec, double move)
    {
    	if ((vec.X == minX || vec.X == maxX) && (vec.Z < minZ + relief || vec.Z > maxZ - relief))
        {
            return move;
        }
        if ((vec.X < minX + relief || vec.X > maxX - relief) && (vec.Z == minZ || vec.Z == maxZ))
        {
            return move;
        }
        
        double mid = (minY + maxY)/2;

        if (move > 0.0D && vec.Y < mid)
        {
            double d = minY - vec.Y;

            if (d < move)
            {
                move = d;
            }
        }

        if (move < 0.0D && vec.Y > mid)
        {
            double d1 = maxY - vec.Y;

            if (d1 > move)
            {
                move = d1;
            }
        }

        return move;
    }
    public double calculateZOffset(PhysVector vec, double move)
    {
    	if ((vec.X == minX || vec.X == maxX) && (vec.Y < minY + relief || vec.Y > maxY - relief))
        {
            return move;
        }
        if ((vec.X < minX + relief || vec.X > maxX - relief) && (vec.Y == minY || vec.Y == maxY))
        {
            return move;
        }

        double mid = (minZ + maxZ)/2;
        
        if (move > 0.0D && vec.Z < mid)
        {
            double d = minZ - vec.Z;

            if (d < move)
            {
                move = d;
            }
        }

        if (move < 0.0D && vec.Z > mid)
        {
            double d1 = maxZ - vec.Z;

            if (d1 > move)
            {
                move = d1;
            }
        }

        return move;
    }

    public double minorRound(double number)
    {
    	if(number - Math.floor(number) < .05)
    	{
    		return Math.floor(number);
    	}
    	if(Math.ceil(number) - number < .05)
    	{
    		return Math.ceil(number);
    	}
    	return number;
    }
    
    public AxisAlignedBB getEnclosingUnrotatedAABB()
    {
    	PhysVector c = corners[0];
    	double x = c.X;
    	double y = c.Y;
    	double z = c.Z;
    	AxisAlignedBB enclosingBox = new AxisAlignedBB(x, y, z, x, y, z);
    	
    	for(int i = 1; i < 8; i++)
    	{
    		c = corners[i];
        	x = c.X;
        	y = c.Y;
        	z = c.Z;
        	if(enclosingBox.minX > x)
        	{
        		enclosingBox.minX = x;
        	}
        	if(enclosingBox.minY > y)
        	{
        		enclosingBox.minY = y;
        	}
        	if(enclosingBox.minZ > z)
        	{
        		enclosingBox.minZ = z;
        	}
        	if(enclosingBox.maxX < x)
        	{
        		enclosingBox.maxX = x;
        	}
        	if(enclosingBox.maxY < y)
        	{
        		enclosingBox.maxY = y;
        	}
        	if(enclosingBox.maxZ < z)
        	{
        		enclosingBox.maxZ = z;
        	}
    	}
    	return enclosingBox;
    }
	//edges:
	//0-1
	//0-2
	//0-4
	//1-3
	//1-5
	//2-3
	//2-6
	//3-7
	//4-6
	//4-5
	//5-7
	//6-7
	
	///////////
	
	//0-1
	//0-2
	//0-4
	//1-3
	//1-5
	//1-0
	//2-0
	//2-3
	//2-6
	//3-1
	//3-2
	//3-7
	//4-0
	//4-6
	//4-5
	//5-1
	//5-4
	//5-7
	//6-2
	//6-4
	//6-7
	//7-3
	//7-5
	//7-6
}


