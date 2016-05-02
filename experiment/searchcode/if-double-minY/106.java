package net.minecraft.src;

import java.util.*;
import java.io.*;

import net.minecraft.client.Minecraft;

public class PhysWorld extends World{
	
	public static HashMap<Integer, Double> blockMasses = PhysManager.blockMasses;
	public static int timeStepMilli = PhysManager.timeStepMilli;
	
	public boolean scaling = false;
	
	public PhysRenderGlobal renderGlobal;
    public PhysEffectRenderer effectRenderer;
    public EntityRenderer entityRenderer;
    public Minecraft mc;
    public ArrayList<PhysTie> ties = new ArrayList<PhysTie>();
    
	public int index;
    public MovingObjectPosition objectMouseOver;
	
	public boolean localControls = false;
	
	public boolean pushOnGoing = false;
	public Long lastPush = 0L;
	public Long currentLoopTime = 0L;
	public Long prevLoopTime = 0L;
	public int lastTimeInc;
	public int timeLeft = 0;
	
	public int numMinXBlocks = 0;
	public int numMaxXBlocks = 0;
	public int numMinYBlocks = 0;
	public int numMaxYBlocks = 0;
	public int numMinZBlocks = 0;
	public int numMaxZBlocks = 0;
	
	public boolean pushed = false;
	
	int waterLevel = -1;
	//private byte[] cornersSealedVals = new byte[0xffffff];
	//private byte[] cornersSealChecked = new byte[0xffffff];
	//private byte[] blocksSealedVals = new byte[0xffffff];
	//private byte[] blocksSealChecked = new byte[0xffffff];
	//private byte[] blockLocsA = new byte[0xffffff];
	ArrayList<Integer> corners = new ArrayList<Integer>();
	ArrayList<Integer> faces = new ArrayList<Integer>();
	
	public double dragCoefficient = 0.05;
	public double dynamicPressureCoefficient = 1;
	public double airPressureMultiplier = 1;
	public double waterPressureMultiplier = 1;
	
	//Parent World
	World worldObj;
	ArrayList<PhysEntityCollision> entityCollisions = new ArrayList<PhysEntityCollision>();
	ArrayList<int[]> blockLocs;
	int minX = 0;
	int minY = 0;
	int minZ = 0;
	int maxX = 0;
	int maxY = 0;
	int maxZ = 0;
	
	public ArrayList<PhysCollision> currentTickCollisions = new ArrayList<PhysCollision>();
	
	public PhysVector scale;
	public double[] lToWTransform = PhysMatrix.getDoubleIdentity();
	public double[] unScaledTransform = PhysMatrix.getDoubleIdentity();
	public double[] wToLTransform = PhysMatrix.getDoubleIdentity();
	public double[] transformInc = PhysMatrix.getDoubleIdentity();
	
	public PhysVector wRightInLocalC = PhysVector.unitVector(0);
	public PhysVector wUpInLocalC = PhysVector.unitVector(1);
	public PhysVector wForwardInLocalC = PhysVector.unitVector(2);
	public PhysVector lRightInWorldC = PhysVector.unitVector(0);
	public PhysVector lUpInWorldC = PhysVector.unitVector(1);
	public PhysVector lForwardInWorldC = PhysVector.unitVector(2);
	
	/*public Vector rFaceAreas = Vector.unitVector(0);
	public Vector uFaceAreas = Vector.unitVector(1);
	public Vector fFaceAreas = Vector.unitVector(2);*/
	
	public double mass = 0.0;
	public double[] MoITensor = PhysMatrix.getZeroMatrix(3);
	public double[] invMoITensor = PhysMatrix.getZeroMatrix(3);
	
	public PhysVector cm = new PhysVector(0, 0, 0);
	public PhysVector push = new PhysVector(0, 0, 0);
	public PhysVector twist = new PhysVector(0, 0, 0);
	public PhysVector force = new PhysVector(0, 0, 0);
	public PhysVector momentum = new PhysVector(0, 0, 0);
	
	public PhysVector torque = new PhysVector(0, 0, 0);
	public PhysVector moment = new PhysVector(0, 0, 0);
	public PhysVector aVelocity = new PhysVector(0, 0, 0);
	
	public double angleInc = 0.0F;
	public double xInc = 0.0;
	public double yInc = 0.0;
	public double zInc = 0.0;

	public PhysWorld(World world, Minecraft minecraft, int ndx) {
		super(world, new PhysWorldProvider(ndx));
        mapStorage = null;
        worldInfo = world.worldInfo;
		
		index = ndx;
		worldObj = world;
		PhysManagerShips.subWorlds.add(this);
		mc = minecraft;
		renderGlobal = new PhysRenderGlobal(mc, mc.renderEngine);
		renderGlobal.changeWorld(this);
		effectRenderer = new PhysEffectRenderer(this, mc.renderEngine);
		entityRenderer = new EntityRenderer(mc);
		
		if(!(worldObj instanceof WorldClient))
		{
			blockLocs = PhysManagerSaves.loadSubBlockLocs((SaveHandler)saveHandler,index);
			
			if(blockLocs.size() > 0)
			{
		        for(int i = 0; i < blockLocs.size(); i++)
		        {
		        	int[] coordinates = blockLocs.get(i);
		        	int x = coordinates[0];
		        	int y = coordinates[1];
		        	int z = coordinates[2];
		        	
		        	calculateNewLimits(x, y, z);
		        	updateBlocksCorners(x, y, z);
		        	updateBlocksFaces(x, y, z);
		        	addOrRemoveBlockMass(x, y, z, getBlockId(x, y, z), true);
		        }
		        renderGlobal.loadRenderers();
			}
		}
		else
		{
			blockLocs = new ArrayList<int[]>();
		}
		
		unScaledTransform = PhysManagerSaves.loadTransform((SaveHandler)saveHandler, index);
		scale = PhysManagerSaves.loadScale((SaveHandler)saveHandler, index);
		lToWTransform = PhysMatrix.getScaledMatrix(unScaledTransform, scale);
		wToLTransform = PhysMatrix.inverse(lToWTransform, scale);
	}
	
	public void moveMass(double x0, double y0, double z0, double xN, double yN, double zN, double mass)
	{
		addMass(x0, y0, z0, -mass);
		addMass(xN, yN, zN, mass);
	}
	
	public void addMass(double x, double y, double z, double addedMass)
	{
		if(mass + addedMass == 0)
		{
			mass = 0;
			MoITensor = PhysMatrix.getZeroMatrix(3);
			return;
		}
		PhysVector prevCM = new PhysVector(cm);
		
		cm.multiply(mass);
		cm.add(new PhysVector(x, y, z).getProduct(addedMass));
		cm.multiply(1/(mass + addedMass));
		
		PhysVector cmShift = prevCM.getSubtraction(cm);
		double cmShiftX = cmShift.X;
		double cmShiftY = cmShift.Y;
		double cmShiftZ = cmShift.Z;
		double rx = x-cm.X;
		double ry = y-cm.Y;
		double rz = z-cm.Z;
		
		MoITensor[0] = MoITensor[0] + (cmShiftY*cmShiftY + cmShiftZ*cmShiftZ)*mass + (ry*ry + rz*rz)*addedMass;
		MoITensor[1] = MoITensor[1] - cmShiftX*cmShiftY*mass - rx*ry*addedMass;
		MoITensor[2] = MoITensor[2] - cmShiftX*cmShiftZ*mass - rx*rz*addedMass;
		MoITensor[3] = MoITensor[1];
		MoITensor[4] = MoITensor[4] + (cmShiftX*cmShiftX + cmShiftZ*cmShiftZ)*mass + (rx*rx + rz*rz)*addedMass;
		MoITensor[5] = MoITensor[5] - cmShiftY*cmShiftZ*mass - ry*rz*addedMass;
		MoITensor[6] = MoITensor[2];
		MoITensor[7] = MoITensor[5];
		MoITensor[8] = MoITensor[8]  + (cmShiftX*cmShiftX + cmShiftY*cmShiftY)*mass + (rx*rx + ry*ry)*addedMass;
		
		invMoITensor = PhysMatrix.inverse3by3(MoITensor);
		
		mass += addedMass;
	}
	
	public void subTick()
	{
		worldProvider.worldChunkMgr.cleanupCache();
		int i = calculateSkylightSubtracted(1.0F);

		if (i != skylightSubtracted)
		{
			skylightSubtracted = i;
		}

		long l1 = worldInfo.getWorldTime() + 1L;

		if (l1 % (long)autosavePeriod == 0L)
		{
			saveWorld(false, null);
		}
	}
	
	protected IChunkProvider createChunkProvider()
    {
        IChunkLoader ichunkloader = PhysManagerSaves.getChunkLoader((SaveHandler)saveHandler, worldProvider);
        
        return new PhysChunkProvider(this, ichunkloader, worldProvider.getChunkProvider());
    }
	
	public void calculateMoIMassAndCMFromScratch()
	{
		mass = 0;
		for(int i = 0; i < blockLocs.size(); i++)
		{
			int[] blockLoc = blockLocs.get(i);
			int x = blockLoc[0];
			int y = blockLoc[1];
			int z = blockLoc[2];
			addOrRemoveBlockMass(x, y, z, getBlockId(x, y, z), true);
		}
	}
    
	public void addOrRemoveBlockMass(int x, int y, int z, int blockID, boolean adding)
	{
		double partialWeight = blockMasses.get(blockID)/8.0;
		double shift = 2.0/3.0;
    	if(adding)
    	{
    		addMass(x + 0.5 - shift, y + 0.5 - shift, z + 0.5 - shift, partialWeight);
    		addMass(x + 0.5 - shift, y + 0.5 + shift, z + 0.5 - shift, partialWeight);
    		addMass(x + 0.5 - shift, y + 0.5 - shift, z + 0.5 + shift, partialWeight);
    		addMass(x + 0.5 - shift, y + 0.5 + shift, z + 0.5 + shift, partialWeight);
    		addMass(x + 0.5 + shift, y + 0.5 - shift, z + 0.5 - shift, partialWeight);
    		addMass(x + 0.5 + shift, y + 0.5 + shift, z + 0.5 - shift, partialWeight);
    		addMass(x + 0.5 + shift, y + 0.5 - shift, z + 0.5 + shift, partialWeight);
    		addMass(x + 0.5 + shift, y + 0.5 + shift, z + 0.5 + shift, partialWeight);
    	}
    	else
    	{
    		addMass(x + 0.5 - shift, y + 0.5 - shift, z + 0.5 - shift, -partialWeight);
    		addMass(x + 0.5 - shift, y + 0.5 + shift, z + 0.5 - shift, -partialWeight);
    		addMass(x + 0.5 - shift, y + 0.5 - shift, z + 0.5 + shift, -partialWeight);
    		addMass(x + 0.5 - shift, y + 0.5 + shift, z + 0.5 + shift, -partialWeight);
    		addMass(x + 0.5 + shift, y + 0.5 - shift, z + 0.5 - shift, -partialWeight);
    		addMass(x + 0.5 + shift, y + 0.5 + shift, z + 0.5 - shift, -partialWeight);
    		addMass(x + 0.5 + shift, y + 0.5 - shift, z + 0.5 + shift, -partialWeight);
    		addMass(x + 0.5 + shift, y + 0.5 + shift, z + 0.5 + shift, -partialWeight);
    	}
	}
	
    public void saveWorld(boolean par1, IProgressUpdate par2IProgressUpdate)
    {
        chunkProvider.saveChunks(par1, par2IProgressUpdate);
        PhysManagerSaves.saveSubBlockLocs((SaveHandler)saveHandler, blockLocs, index);
        PhysManagerSaves.saveTransform((SaveHandler)saveHandler, unScaledTransform, index);
        PhysManagerSaves.saveScale((SaveHandler)saveHandler, scale, index);
    }

    protected void generateSpawnPoint()
    {
    }

    public BiomeGenBase getBiomeGenForCoords(int par1, int par2)
    {
        return worldObj.worldProvider.worldChunkMgr.getBiomeGenAt(0, 0);
    }

    public boolean setBlockAndMetadata(int par1, int par2, int par3, int par4, int par5)
    {
        if (par1 < 0xfe363c80 || par3 < 0xfe363c80 || par1 >= 0x1c9c380 || par3 >= 0x1c9c380)
        {
            return false;
        }

        if (par2 < 0)
        {
            return false;
        }

        if (par2 >= 256)
        {
            return false;
        }
        
        int lastId = getBlockId(par1, par2, par3);

        Chunk chunk = getChunkFromChunkCoords(par1 >> 4, par3 >> 4);
        boolean flag = chunk.setBlockIDWithMetadata(par1 & 0xf, par2, par3 & 0xf, par4, par5);

        if(flag)
        {
            updateBlockLocs(par1, par2, par3, par4, lastId);
        	addOrRemoveBlockMass(par1, par2, par3, lastId, false);
        	addOrRemoveBlockMass(par1, par2, par3, par4, true);
        }
    	updateBlocksCorners(par1, par2, par3);
    	updateBlocksFaces(par1, par2, par3);
        
        Profiler.startSection("checkLight");
        updateAllLightTypes(par1, par2, par3);
        Profiler.endSection();
        return flag;
    }
    
    public boolean setBlock(int par1, int par2, int par3, int par4)
    {
        if (par1 < 0xfe363c80 || par3 < 0xfe363c80 || par1 >= 0x1c9c380 || par3 >= 0x1c9c380)
        {
            return false;
        }

        if (par2 < 0)
        {
            return false;
        }

        if (par2 >= 256)
        {
            return false;
        }

        int lastId = getBlockId(par1, par2, par3);
        
        Chunk chunk = getChunkFromChunkCoords(par1 >> 4, par3 >> 4);
        boolean flag = chunk.setBlockID(par1 & 0xf, par2, par3 & 0xf, par4);
        
        if(flag)
        {
            updateBlockLocs(par1, par2, par3, par4, lastId);
        	addOrRemoveBlockMass(par1, par2, par3, lastId, false);
        	addOrRemoveBlockMass(par1, par2, par3, par4, true);
        }
    	updateBlocksCorners(par1, par2, par3);
    	updateBlocksFaces(par1, par2, par3);
		
        Profiler.startSection("checkLight");
        updateAllLightTypes(par1, par2, par3);
        Profiler.endSection();
        return flag;
    }
    
    public void updateBlockLocs(int par1, int par2, int par3, int par4, int lastId)
    {
    	if(par4 != 0 && lastId == 0)
    	{
    		int[] blockLoc = new int[3];
    		blockLoc[0] = par1;
    		blockLoc[1] = par2;
    		blockLoc[2] = par3;
    		blockLocs.add(blockLoc);
    		calculateNewLimits(par1, par2, par3);
    	}
    	else if(par4 == 0 && lastId != 0)
    	{
    		for(int i = 0; i < blockLocs.size(); i++)
    		{
    			if(par1 == blockLocs.get(i)[0])
    			{
	    			if(par2 == blockLocs.get(i)[1])
	    			{
	    				if(par3 == blockLocs.get(i)[2])
	    				{
	    					removeBlocksMomentum(par1, par2, par3, blockMasses.get(lastId));
	    					blockLocs.remove(i);
	    				}
	    			}
    			}
    		}
    	}
    	/*if(blockLocs.size() == 0)
    	{
    		worldObj.emptySubs.add(index);
    	}*/
    }
    
    
    
    /**
     * Plays a sound at the entity's position. Args: entity, sound, unknown1, volume (relative to 1.0)
     */
    public void playSoundAtEntity(Entity par1Entity, String par2Str, float par3, float par4)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).playSound(par2Str, par1Entity.posX, par1Entity.posY - (double)par1Entity.yOffset, par1Entity.posZ, par3, par4);
        }
    }

    /**
     * Play a sound effect. Many many parameters for this function. Not sure what they do, but a classic call is :
     * (double)i + 0.5D, (double)j + 0.5D, (double)k + 0.5D, 'random.door_open', 1.0F, world.rand.nextFloat() * 0.1F +
     * 0.9F with i,j,k position of the block.
     */
    public void playSoundEffect(double par1, double par3, double par5, String par7Str, float par8, float par9)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).playSound(par7Str, par1, par3, par5, par8, par9);
        }
    }

    /**
     * Plays a record at the specified coordinates of the specified name. Args: recordName, x, y, z
     */
    public void playRecord(String par1Str, int par2, int par3, int par4)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).playRecord(par1Str, par2, par3, par4);
        }
    }

    /**
     * Spawns a particle.  Args particleName, x, y, z, velX, velY, velZ
     */
    public void spawnParticle(String par1Str, double par2, double par4, double par6, double par8, double par10, double par12)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).spawnParticle(par1Str, par2, par4, par6, par8, par10, par12);
        }
    }

    /**
     * adds a lightning bolt to the list of lightning bolts in this world.
     */
    public boolean addWeatherEffect(Entity par1Entity)
    {
        weatherEffects.add(par1Entity);
        return true;
    }

    /**
     * Start the skin for this entity downloading, if necessary, and increment its reference counter
     */
    protected void obtainEntitySkin(Entity par1Entity)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).obtainEntitySkin(par1Entity);
        }
    }

    /**
     * Decrement the reference counter for this entity's skin image data
     */
    protected void releaseEntitySkin(Entity par1Entity)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).releaseEntitySkin(par1Entity);
        }
    }

    /**
     * Not sure what this does 100%, but from the calling methods this method should be called like this.
     */
    public void setEntityDead(Entity par1Entity)
    {
        if (par1Entity.riddenByEntity != null)
        {
            par1Entity.riddenByEntity.mountEntity(null);
        }

        if (par1Entity.ridingEntity != null)
        {
            par1Entity.mountEntity(null);
        }

        par1Entity.setDead();

        if (par1Entity instanceof EntityPlayer)
        {
            playerEntities.remove((EntityPlayer)par1Entity);
            updateAllPlayersSleepingFlag();
        }
    }

    /**
     * Adds a IWorldAccess to the list of worldAccesses
     */
    public void addWorldAccess(IWorldAccess par1IWorldAccess)
    {
        worldAccesses.add(par1IWorldAccess);
    }

    /**
     * Removes a worldAccess from the worldAccesses object
     */
    public void removeWorldAccess(IWorldAccess par1IWorldAccess)
    {
        worldAccesses.remove(par1IWorldAccess);
    }

    protected boolean pushOutOfBlocks(double par1, double par3, double par5, Entity par7Entity)
    {
    	//FIX//par1 += -subPosX;
    	//FIX//par3 += -subPosY;
    	//FIX//par5 += -subPosZ;
        int i = MathHelper.floor_double(par1);
        int j = MathHelper.floor_double(par3);
        int k = MathHelper.floor_double(par5);
        double d = par1 - (double)i;
        double d1 = par3 - (double)j;
        double d2 = par5 - (double)k;

        if (isBlockNormalCube(i, j, k))
        {
            boolean flag = !isBlockNormalCube(i - 1, j, k);
            boolean flag1 = !isBlockNormalCube(i + 1, j, k);
            boolean flag2 = !isBlockNormalCube(i, j - 1, k);
            boolean flag3 = !isBlockNormalCube(i, j + 1, k);
            boolean flag4 = !isBlockNormalCube(i, j, k - 1);
            boolean flag5 = !isBlockNormalCube(i, j, k + 1);
            byte byte0 = -1;
            double d3 = 9999D;

            if (flag && d < d3)
            {
                d3 = d;
                byte0 = 0;
            }

            if (flag1 && 1.0D - d < d3)
            {
                d3 = 1.0D - d;
                byte0 = 1;
            }

            if (flag2 && d1 < d3)
            {
                d3 = d1;
                byte0 = 2;
            }

            if (flag3 && 1.0D - d1 < d3)
            {
                d3 = 1.0D - d1;
                byte0 = 3;
            }

            if (flag4 && d2 < d3)
            {
                d3 = d2;
                byte0 = 4;
            }

            if (flag5 && 1.0D - d2 < d3)
            {
                double d4 = 1.0D - d2;
                byte0 = 5;
            }

            float f = rand.nextFloat() * 0.2F + 0.1F;

            if (byte0 == 0)
            {
                par7Entity.motionX = -f;
            }

            if (byte0 == 1)
            {
            	par7Entity.motionX = f;
            }

            if (byte0 == 2)
            {
            	par7Entity.motionY = -f;
            }

            if (byte0 == 3)
            {
            	par7Entity.motionY = f;
            }

            if (byte0 == 4)
            {
            	par7Entity.motionZ = -f;
            }

            if (byte0 == 5)
            {
            	par7Entity.motionZ = f;
            }

            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Returns a list of bounding boxes that collide with aabb excluding the passed in entity's collision. Args: entity,
     * aabb
     */
    public List getCollidingBoundingBoxes(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB)
    {
    	ArrayList<AxisAlignedBB> subCollidingBBs = new ArrayList<AxisAlignedBB>();

    	PhysAxisAlignedBB rotatedBB = new PhysAxisAlignedBB(par2AxisAlignedBB, wToLTransform, lToWTransform, false, index);
    	AxisAlignedBB relAxisAlignedBB = rotatedBB.getEnclosingUnrotatedAABB();
    	
    	int i = MathHelper.floor_double(relAxisAlignedBB.minX);
        int j = MathHelper.floor_double(relAxisAlignedBB.maxX + 1.0D);
        int k = MathHelper.floor_double(relAxisAlignedBB.minY);
        int l = MathHelper.floor_double(relAxisAlignedBB.maxY + 1.0D);
        int i1 = MathHelper.floor_double(relAxisAlignedBB.minZ);
        int j1 = MathHelper.floor_double(relAxisAlignedBB.maxZ + 1.0D);
        
        for (int k1 = i; k1 < j; k1++)
        {
            for (int l1 = i1; l1 < j1; l1++)
            {
                if (!blockExists(k1, 64, l1))
                {
                    continue;
                }

                for (int i2 = k - 1; i2 < l; i2++)
                {
                    Block block = Block.blocksList[getBlockId(k1, i2, l1)];

                    if (block != null)
                    {
                        block.getCollidingBoundingBoxes(this, k1, i2, l1, relAxisAlignedBB, subCollidingBBs);
                    }
                }
            }
        }

        ArrayList<AxisAlignedBB> fusedBBs = getFusedBBs(subCollidingBBs);
        ArrayList<PhysAxisAlignedBB> rotatedCollidingBBs = new ArrayList<PhysAxisAlignedBB>();
        
        for(int z = 0; z < fusedBBs.size(); z++)
        {
        	AxisAlignedBB currBox = fusedBBs.get(z);
        	rotatedCollidingBBs.add(new PhysAxisAlignedBB(currBox, lToWTransform, wToLTransform, false, index));
        }
        
        return rotatedCollidingBBs;
    }
    
    public ArrayList<AxisAlignedBB> getFusedBBs(ArrayList<AxisAlignedBB> bbs)
    {
    	ArrayList<AxisAlignedBB> fusedBBs = new ArrayList<AxisAlignedBB>();
    	ArrayList<Integer> fusedIndices = new ArrayList<Integer>();
    	int unchanged = 0;
    	for(int n = 0; n < bbs.size(); n++)
        {
    		int priorSize = fusedBBs.size();
        	for(int m = n + 1; m < bbs.size(); m++)
        	{
        		AxisAlignedBB bb1 = bbs.get(n);
        		AxisAlignedBB bb2 = bbs.get(m);
        		if(PhysAxisAlignedBB.connected(bb1, bb2))
        		{
        			fusedBBs.add(PhysAxisAlignedBB.getFusedBoundingBox(bb1, bb2));
        			fusedIndices.add(m);
        		}
        	}
        	if(priorSize == fusedBBs.size() && !fusedIndices.contains(n))
        	{
        		fusedBBs.add(bbs.get(n));
        		unchanged++;
        	}
        }
    	
    	if(unchanged == fusedBBs.size())
    	{
    		return fusedBBs;
    	}
    	else
    	{
    		return getFusedBBs(fusedBBs);
    	}
    }
  
    ///---------------------

    public float func_35464_b(float par1)
    {
        float f = worldObj.getCelestialAngle(par1);
        float f1 = 1.0F - (MathHelper.cos(f * (float)Math.PI * 2.0F) * 2.0F + 0.2F);

        if (f1 < 0.0F)
        {
            f1 = 0.0F;
        }

        if (f1 > 1.0F)
        {
            f1 = 1.0F;
        }

        f1 = 1.0F - f1;
        f1 = (float)((double)f1 * (1.0D - (double)(getRainStrength(par1) * 5F) / 16D));
        f1 = (float)((double)f1 * (1.0D - (double)(getWeightedThunderStrength(par1) * 5F) / 16D));
        return f1 * 0.8F + 0.2F;
    }

    public Vec3D getSkyColor(Entity par1Entity, float par2)
    {
        float f = worldObj.getCelestialAngle(par2);
        float f1 = MathHelper.cos(f * (float)Math.PI * 2.0F) * 2.0F + 0.5F;

        if (f1 < 0.0F)
        {
            f1 = 0.0F;
        }

        if (f1 > 1.0F)
        {
            f1 = 1.0F;
        }

        int i = MathHelper.floor_double(par1Entity.posX);
        int j = MathHelper.floor_double(par1Entity.posZ);
        BiomeGenBase biomegenbase = getBiomeGenForCoords(i, j);
        float f2 = biomegenbase.getFloatTemperature();
        int k = biomegenbase.getSkyColorByTemp(f2);
        float f3 = (float)(k >> 16 & 0xff) / 255F;
        float f4 = (float)(k >> 8 & 0xff) / 255F;
        float f5 = (float)(k & 0xff) / 255F;
        f3 *= f1;
        f4 *= f1;
        f5 *= f1;
        float f6 = getRainStrength(par2);

        if (f6 > 0.0F)
        {
            float f7 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.6F;
            float f9 = 1.0F - f6 * 0.75F;
            f3 = f3 * f9 + f7 * (1.0F - f9);
            f4 = f4 * f9 + f7 * (1.0F - f9);
            f5 = f5 * f9 + f7 * (1.0F - f9);
        }

        float f8 = getWeightedThunderStrength(par2);

        if (f8 > 0.0F)
        {
            float f10 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.2F;
            float f12 = 1.0F - f8 * 0.75F;
            f3 = f3 * f12 + f10 * (1.0F - f12);
            f4 = f4 * f12 + f10 * (1.0F - f12);
            f5 = f5 * f12 + f10 * (1.0F - f12);
        }

        if (lightningFlash > 0)
        {
            float f11 = (float)lightningFlash - par2;

            if (f11 > 1.0F)
            {
                f11 = 1.0F;
            }

            f11 *= 0.45F;
            f3 = f3 * (1.0F - f11) + 0.8F * f11;
            f4 = f4 * (1.0F - f11) + 0.8F * f11;
            f5 = f5 * (1.0F - f11) + 1.0F * f11;
        }

        return Vec3D.createVector(f3, f4, f5);
    }

    public int getMoonPhase(float par1)
    {
        return worldObj.getMoonPhase(par1);
    }

    public MovingObjectPosition rayTraceBlocks(Vec3D par1Vec3D, Vec3D par2Vec3D)
    {
    	PhysVector vec1 = PhysVector.convertToVector(par1Vec3D);
    	PhysVector vec2 = PhysVector.convertToVector(par2Vec3D);
    	PhysMatrix.applyTransform(wToLTransform, vec1);
    	PhysMatrix.applyTransform(wToLTransform, vec2);
    	
    	return PhysManagerShips.rayTraceBlocks_do_do(this, vec1.convertToVec3D(), vec2.convertToVec3D(), false, false);
    }
    
    public boolean checkIfAABBIsClear(AxisAlignedBB par1AxisAlignedBB)
    {
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, par1AxisAlignedBB);

        for (int i = 0; i < list.size(); i++)
        {
            Entity entity = (Entity)list.get(i);
            if (!entity.isDead && entity.preventEntitySpawning)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if the block at the specified coordinates is an opaque cube. Args: x, y, z
     */
    public boolean isBlockOpaqueCube(int par1, int par2, int par3)
    {
        Block block = Block.blocksList[getBlockId(par1, par2, par3)];

        if (block == null)
        {
            return false;
        }
        else
        {
            return block.isOpaqueCube();
        }
    }

    /**
     * Indicate if a material is a normal solid opaque cube.
     */
    public boolean isBlockNormalCube(int par1, int par2, int par3)
    {
        return Block.isNormalCube(getBlockId(par1, par2, par3));
    }

    public boolean isBlockNormalCubeDefault(int par1, int par2, int par3, boolean par4)
    {
        if (par1 < 0xfe363c80 || par3 < 0xfe363c80 || par1 >= 0x1c9c380 || par3 >= 0x1c9c380)
        {
            return par4;
        }

        Chunk chunk = getChunkFromChunkCoords(par1 >> 4, par3 >> 4);

        if (chunk == null || chunk.isEmpty())
        {
            return par4;
        }

        Block block = Block.blocksList[getBlockId(par1, par2, par3)];

        if (block == null)
        {
            return false;
        }
        else
        {
            return block.blockMaterial.isOpaque() && block.renderAsNormalBlock();
        }
    }

    public void saveWorldIndirectly(IProgressUpdate par1IProgressUpdate)
    {
        saveWorld(true, par1IProgressUpdate);

        try
        {
            ThreadedFileIOBase.threadedIOInstance.waitForFinish();
        }
        catch (InterruptedException interruptedexception)
        {
            interruptedexception.printStackTrace();
        }
    }

    /**
     * Called on construction of the World class to setup the initial skylight values
     */
    public void calculateInitialSkylight()
    {
        int i = calculateSkylightSubtracted(1.0F);

        if (i != skylightSubtracted)
        {
            skylightSubtracted = i;
        }
    }
    
    public float getCelestialAngle(float par1)
    {
    	if(worldObj == null)
    	{
    		return 0;
    	}
    	else
    	{
    		return worldObj.getCelestialAngle(par1);
    	}
    }

    /**
     * Set which types of mobs are allowed to spawn (peaceful vs hostile).
     */
    public void setAllowedSpawnTypes(boolean par1, boolean par2)
    {
        spawnHostileMobs = par1;
        spawnPeacefulMobs = par2;
    }
    
	public void tieRopeAt(PhysRopeJoint joint, PhysVector loc)
	{
		joint.moveTo(PhysMatrix.getTransformedVec(lToWTransform, loc));
		int n = 1;
		if(loc.Y % 1.0 == .5)
		{
			n = 2;
		}
		else if(loc.Z % 1.0 == .5)
		{
			n = 3;
		}
		
		PhysTie tie = new PhysTie(joint, loc, n, index);
		ties.add(tie);
		joint.tie = tie;
	}
	
    public void handleRopeTugs()
    {
    	for(int i = 0; i < ties.size(); i++)
    	{
    		PhysTie tie = ties.get(i);
    		PhysRopeJoint joint = tie.joint;
    		if(joint.tie == null)
    		{
    			ties.remove(i);
    			i--;
    			continue;
    		}

    		PhysVector location = tie.location;
    		momentum.add(joint.lastTug);
    		
    		PhysVector torqueArm = PhysVector.getDifference(location, cm);
    		moment.add(PhysVector.cross(torqueArm, joint.lastTug.getOriented(wToLTransform)));
    		joint.lastTug.zero();
    		
    		PhysVector norm;
    		if(tie.faceNormal == 1)
    		{
        		norm = new PhysVector(1, 0, 0);
    		}
    		else if(tie.faceNormal == 2)
    		{
        		norm = new PhysVector(0, 1, 0);
    		}
    		else
    		{
        		norm = new PhysVector(0, 0, 1);
    		}
    		
    		norm.multiply(joint.lastTorsion);
    		
    		moment.add(norm);
    		joint.lastTorsion = 0;
    	}
    }
    
    public void moveTiedRopes(float seconds)
    {
    	for(int i = 0; i < ties.size(); i++)
    	{
    		PhysTie tie = ties.get(i);
    		
    		PhysRopeJoint joint = tie.joint;
    		if(joint.tie == null)
    		{
    			ties.remove(i);
    			i--;
    			continue;
    		}
    		if(joint.targeting)
    		{
    			joint.target = PhysMatrix.getTransformedVec(lToWTransform, tie.location);
    			return;
    		}
    		double angle;
    		if(tie.faceNormal == 1)
    		{
        		angle = aVelocity.X*seconds;
    		}
    		else if(tie.faceNormal == 2)
    		{
        		angle = aVelocity.Y*seconds;
    		}
    		else
    		{
        		angle = aVelocity.Z*seconds;
    		}
    		
    		PhysVector oldPos = joint.pos.copy();
    		
    		joint.pos = PhysMatrix.getTransformedVec(lToWTransform, tie.location);
    		
    		joint.momentum = joint.pos.minus(oldPos).times(joint.jointMass/seconds);
    		joint.twist += angle;
    	}
    }

    public void motionTick(boolean doPhysics)
    {
    	prevLoopTime = currentLoopTime;
    	currentLoopTime = System.currentTimeMillis();
    	
    	if(!doPhysics)
    	{
    		return;
    	}
    	if(scaling)
    	{
    		return;
    	}
    	if(blockLocs.size() == 0)
    	{
    		removeTies();
    		return;
    	}
    	
    	int timeInc = (int)(currentLoopTime - prevLoopTime);
    	lastTimeInc = timeInc;

    	handleRopeTugs();
    	motionTick(timeInc + timeLeft);
    }
    
    public void motionTick(int timeInc)
    {
    	if(timeInc > 45)
		{
			timeInc = 45;
		}
		
		if(timeInc < 0)
		{
			return;
		}
		
		while(timeInc > timeStepMilli)
		{
			PhysVector currentTwist = new PhysVector(twist);
	    	PhysVector currentPush = new PhysVector(push);
			miniMotionTick(timeStepMilli);
			timeInc -= timeStepMilli; 
			push = currentPush;
			twist = currentTwist;
			waterLevel = -1;
		}

		push.zero();
		twist.zero();
		timeLeft = timeInc;
    }
    
    public void miniMotionTick(int tInc)
    {
    	float timeInc = tInc * 0.001F;

    	orientUnitVectors();
    	calculateFaceProjectionAreas();
    	
		calculateForces(timeInc);
		
		unScaledTransform = PhysMatrix.getMatrixProduct(unScaledTransform, transformInc);
		lToWTransform = PhysMatrix.getScaledMatrix(unScaledTransform, scale);
    	for(int i = 0; i < entityCollisions.size(); i++)
    	{
    		PhysEntityCollision eC = entityCollisions.get(i);
    		PhysMatrix.applyTransformInc(wToLTransform, lToWTransform, eC.entity);
    		eC.time -= tInc;
    		if(eC.time <= 0)
    		{
    			entityCollisions.remove(i);
    			i--;
    		}
    	}
		wToLTransform = PhysMatrix.inverse(lToWTransform, scale);

		moveTiedRopes(timeInc);
    }
    
    public void orientUnitVectors()
    {
    	wRightInLocalC = PhysVector.unitVector(0);
    	wUpInLocalC = PhysVector.unitVector(1);
    	wForwardInLocalC = PhysVector.unitVector(2);
    	wRightInLocalC.orient(wToLTransform);
    	wUpInLocalC.orient(wToLTransform);
    	wForwardInLocalC.orient(wToLTransform);

    	lRightInWorldC = PhysVector.unitVector(0);
    	lUpInWorldC = PhysVector.unitVector(1);
    	lForwardInWorldC = PhysVector.unitVector(2);
    	lRightInWorldC.orient(lToWTransform);
    	lUpInWorldC.orient(lToWTransform);
    	lForwardInWorldC.orient(lToWTransform);
    }
    
    public void calculateFaceProjectionAreas()
    {	
    	/*double rFaceArea1 = Math.abs(lForwardInWorldC.Y*lUpInWorldC.Z - lUpInWorldC.Y*lForwardInWorldC.Z);
    	double uFaceArea1 = Math.abs(lForwardInWorldC.Y*lRightInWorldC.Z - lRightInWorldC.Y*lForwardInWorldC.Z);
    	double fFaceArea1 = Math.abs(lRightInWorldC.Y*lUpInWorldC.Z - lUpInWorldC.Y*lRightInWorldC.Z);
    	double rFaceArea2 = Math.abs(lForwardInWorldC.X*lUpInWorldC.Z - lUpInWorldC.X*lForwardInWorldC.Z);
    	double uFaceArea2 = Math.abs(lForwardInWorldC.X*lRightInWorldC.Z - lRightInWorldC.X*lForwardInWorldC.Z);
    	double fFaceArea2 = Math.abs(lRightInWorldC.X*lUpInWorldC.Z - lUpInWorldC.X*lRightInWorldC.Z);
    	double rFaceArea3 = Math.abs(lForwardInWorldC.X*lUpInWorldC.Y - lUpInWorldC.X*lForwardInWorldC.Y);
    	double uFaceArea3 = Math.abs(lForwardInWorldC.X*lRightInWorldC.Y - lRightInWorldC.X*lForwardInWorldC.Y);
    	double fFaceArea3 = Math.abs(lRightInWorldC.X*lUpInWorldC.Y - lUpInWorldC.X*lRightInWorldC.Y);
    	
    	rFaceAreas = new Vector(rFaceArea1, rFaceArea2, rFaceArea3);
    	uFaceAreas = new Vector(uFaceArea1, uFaceArea2, uFaceArea3);
    	fFaceAreas = new Vector(fFaceArea1, fFaceArea2, fFaceArea3);*/
    }

    boolean newC = false;
    public void calculateForces(float timeInc)
    {
    	for(int i = 0; i < faces.size(); i+=4)
    	{
    		calculatePressureAndDrag(faces.get(i), faces.get(i+1), faces.get(i+2), faces.get(i+3));
    	}
    	force.Y -= mass*PhysManager.g;
    	calculateKinematics(timeInc);
    	handleBlockInteractions(timeInc);
    	
    	int ct = 0;
    	while(newC)
    	{
    		if(currentTickCollisions.size() == ct && ct != 0)
    		{
    			moment.zero();
    			boolean up = false;
    			boolean right = false;
    			boolean forward = false;
    			for(int i = 0; i < currentTickCollisions.size(); i++)
        		{
        			PhysCollision c = currentTickCollisions.get(i);
        			if(c.axis == 0)
        			{
        				if(!right)
        				{
        					momentum.X = 0;
                			right = true;
        				}
        			}
        			else if (c.axis == 1)
        			{
        				if(!up)
        				{
        					momentum.Y = 0;
                			up = true;
        				}
        			}
        			else
        			{
        				if(!forward)
        				{
        					momentum.Z = 0;
                			forward = true;
        				}
        			}
        		}
    			newC = false;
    			break;
    		}
        	PhysVector avgPoint = PhysCollision.getAveragePoint(currentTickCollisions);
        	PhysVector avgAxis = PhysCollision.getAverageAxis(currentTickCollisions);
        	
    		/*for(int i = 0; i < currentTickCollisions.size(); i++)
    		{
    			Collision collision = currentTickCollisions.get(i);
    			handleCollision(Vector.unitVector(collision.axis), collision.point, timeInc);
    		}*/
        	handleCollision(avgAxis, avgPoint, timeInc);
    		ct = currentTickCollisions.size();
    		newC = false;
    		handleBlockInteractions(timeInc);
    	}
    	currentTickCollisions.clear();
    	ct = 0;
    	
		calculateKinematics(timeInc);
		
		//for(int i = 0; i < corners.size(); i += 3)
    	{
			//checkWaterDisplacement(corners.get(i), corners.get(i+1), corners.get(i + 2));
    	}
		waterLevel = -1;
    }
    
    public void calculateKinematics(float timeInc)
    {
    	if(!localControls)
    	{
    		twist.orient(wToLTransform);
    	}
    	else
    	{
    		push.orient(lToWTransform);
    	}
    	
    	force.add(push);
    	torque.add(twist);
    	
		if(mass != 0)
		{
			momentum.add(force.getProduct(timeInc));
			moment.add(torque.getProduct(timeInc));
		}
		
		PhysVector speed = momentum.getProduct(1 / mass);
		aVelocity = PhysMatrix.get3by3TransformedVec(invMoITensor, moment);
		
		speed.orient(wToLTransform);
    	
		xInc = speed.X * timeInc;
		yInc = speed.Y * timeInc;
		zInc = speed.Z * timeInc;
		angleInc = aVelocity.length() * timeInc;//*timeInc;
		
		transformInc = PhysMatrix.getTranslationMatrix(xInc, yInc, zInc);
		double[] rotationInc = PhysMatrix.getRotationMatrixAboutAxisAndPoint(aVelocity.X, aVelocity.Y, aVelocity.Z, cm.X, cm.Y, cm.Z, angleInc);
		transformInc = PhysMatrix.getMatrixProduct(transformInc, rotationInc);

		push.zero();
		twist.zero();
		force.zero();
		torque.zero();
    }
    
    public void handleBlockInteractions(float timeInc)
    {
    	for(int i = 0; i < corners.size(); i += 3)
    	{
    		handleBlockIntercepts(corners.get(i), corners.get(i+1), corners.get(i + 2));
    	}
    }
    
    public void calculatePressureAndDrag(int x, int y, int z, int normal)
    {
    	PhysVector faceCenter = new PhysVector(x, y, z);
    	PhysVector worldNormal = new PhysVector(0, 0, 0);
    	PhysVector localNormal = new PhysVector(0, 0, 0);
    	
    	if(normal == -3)
    	{
    		faceCenter.add(new PhysVector(.5, .5, 0));
    		worldNormal = lForwardInWorldC.getProduct(-1);
    		localNormal = new PhysVector(0, 0, -1);
    	}
    	if(normal == -2)
    	{
    		faceCenter.add(new PhysVector(.5, 0, .5));
    		worldNormal = lUpInWorldC.getProduct(-1);
    		localNormal = new PhysVector(0, -1, 0);
    	}
    	if(normal == -1)
    	{
    		faceCenter.add(new PhysVector(0, .5, .5));
    		worldNormal = lRightInWorldC.getProduct(-1);
    		localNormal = new PhysVector(-1, 0, 0);
    	}
    	if(normal == 1)
    	{
    		faceCenter.add(new PhysVector(0, .5, .5));
    		worldNormal = lRightInWorldC.getProduct(1);
    		localNormal = new PhysVector(1, 0, 0);
    	}
    	if(normal == 2)
    	{
    		faceCenter.add(new PhysVector(.5, 0, .5));
    		worldNormal = lUpInWorldC.getProduct(1);
    		localNormal = new PhysVector(0, 1, 0);
    	}
    	if(normal == 3)
    	{
    		faceCenter.add(new PhysVector(.5, .5, 0));
    		worldNormal = lForwardInWorldC.getProduct(1);
    		localNormal = new PhysVector(0, 0, 1);
    	}
    	PhysVector torqueArm = PhysVector.getDifference(faceCenter, cm);
    	PhysVector localFaceC = new PhysVector(faceCenter);
    	PhysMatrix.applyTransform(lToWTransform, faceCenter);

		int worldX = MathHelper.floor_double(faceCenter.X);
		int worldY = MathHelper.floor_double(faceCenter.Y);
		int worldZ = MathHelper.floor_double(faceCenter.Z);
		
		int id = worldObj.getBlockId(worldX, worldY, worldZ);

		double pressure = 0;
		double dynamicPressure = 0;
		double drag = 0;
		PhysVector relativeCurrent = new PhysVector(0, 0, 0);
		PhysVector alteredCurrent = new PhysVector(0, 0, 0);
		if((id == Block.waterStill.blockID || id == Block.waterMoving.blockID))
		{
			if(waterLevel < 0)
			{
				calculateLiquidLevel(worldX, worldY, worldZ);
			}
			pressure = getWaterPressureAt(faceCenter.Y);
			
			PhysVector worldV = PhysVector.getSum(momentum.getProduct(1.0/mass), PhysMatrix.cross(aVelocity, torqueArm).getOriented(lToWTransform));
			relativeCurrent = PhysVector.getDifference(PhysManager.waterCurrent, worldV);

			double cdotn = relativeCurrent.dotProduct(worldNormal);
			
			alteredCurrent = PhysVector.getDifference(relativeCurrent, worldNormal.getProduct(cdotn));
			
			dynamicPressure = blockMasses.get(8)*Math.abs(cdotn)*cdotn*dynamicPressureCoefficient*waterPressureMultiplier;
		}
		else if(id == 0)
		{
			PhysVector worldV = PhysVector.getSum(momentum.getProduct(1.0/mass), PhysMatrix.cross(aVelocity, torqueArm).getOriented(lToWTransform));
			relativeCurrent = PhysVector.getDifference(PhysManager.windCurrent, worldV);
			
			double cdotn = relativeCurrent.dotProduct(worldNormal);

			alteredCurrent = PhysVector.getDifference(relativeCurrent, worldNormal.getProduct(cdotn));
			
			dynamicPressure = blockMasses.get(0)*Math.abs(cdotn)*cdotn*dynamicPressureCoefficient*airPressureMultiplier;
		}

		drag = -(-pressure + dynamicPressure)*dragCoefficient;

		PhysVector dynamicPressureVLocal = PhysMatrix.cross(torqueArm, localNormal.getProduct(dynamicPressure));
		
		force.add(worldNormal.getProduct(dynamicPressure));
		force.add(alteredCurrent.getProduct(drag));
		torque.add(dynamicPressureVLocal);
		torque.add(PhysVector.getCross(torqueArm, alteredCurrent.getOriented(wToLTransform).getProduct(drag)));
		
		force.Y += (worldNormal.getProduct(-pressure).Y);
		torque.add(PhysMatrix.cross(torqueArm, localNormal.getProduct(-pressure)));
    }
    
    public void checkWaterDisplacement(int cornerX, int cornerY, int cornerZ)
    {
    	PhysVector vec = new PhysVector(cornerX, cornerY, cornerZ);
    	PhysVector vecNew = new PhysVector(cornerX, cornerY, cornerZ);

    	PhysVector localVec = PhysMatrix.copy(vec);
    	
    	PhysMatrix.applyTransform(lToWTransform, vec);
    	
		int worldX = MathHelper.floor_double(vec.X);
		int worldY = MathHelper.floor_double(vec.Y);
		int worldZ = MathHelper.floor_double(vec.Z);

		double[] newTransform = PhysMatrix.getMatrixProduct(lToWTransform, transformInc);
		
		PhysMatrix.applyTransform(newTransform, vecNew);
		
		int worldNewX = MathHelper.floor_double(vecNew.X);
		int worldNewY = MathHelper.floor_double(vecNew.Y);
		int worldNewZ = MathHelper.floor_double(vecNew.Z);
		
		if(worldX != worldNewX || worldY != worldNewY || worldZ != worldNewZ)
		{
			int id = worldObj.getBlockId(worldX, worldY, worldZ);
			int newId = worldObj.getBlockId(worldNewX, worldNewY, worldNewZ);
			//worldObj.setBlockWithNotify(worldNewX, worldNewY, worldNewZ, id);
			/*if(newId == 8 || newId == 9)
			{
				if(waterLevel < 0)
				{
					calculateLiquidLevel(worldX, worldY, worldZ);
				}
				if(isCornerWaterSealed(cornerX, cornerY, cornerZ))
				{
					worldObj.setBlockWithNotify(worldNewX, worldNewY, worldNewZ, 0);
				}
				else
				{
					if(id == 8 || id == 9 && worldY < waterLevel)
					{
						worldObj.setBlockWithNotify(worldNewX, worldNewY, worldNewZ, 9);
					}
				}
			}*/
		}
    }
    
    public void handleBlockIntercepts(int cornerX, int cornerY, int cornerZ)
    {
    	PhysVector vec = new PhysVector(cornerX, cornerY, cornerZ);
    	PhysVector vecNew = new PhysVector(cornerX, cornerY, cornerZ);

    	PhysVector localVec = PhysMatrix.copy(vec);
    	
    	PhysMatrix.applyTransform(lToWTransform, vec);
    	
		int worldX = MathHelper.floor_double(vec.X);
		int worldY = MathHelper.floor_double(vec.Y);
		int worldZ = MathHelper.floor_double(vec.Z);

		double[] newTransform = PhysMatrix.getMatrixProduct(lToWTransform, transformInc);
		
		PhysMatrix.applyTransform(newTransform, vecNew);
		
		int worldNewX = MathHelper.floor_double(vecNew.X);
		int worldNewY = MathHelper.floor_double(vecNew.Y);
		int worldNewZ = MathHelper.floor_double(vecNew.Z);
		
		if(worldX != worldNewX)
		{
			int idNew = worldObj.getBlockId(worldNewX, worldY, worldZ);
			if(idNew != 0 && idNew != 8 && idNew!= 9)
			{
				newC = true;
				PhysCollision.addIfNew(new PhysCollision(new PhysVector(cornerX, cornerY, cornerZ), 0), currentTickCollisions);
			}
		}
		if(worldY != worldNewY)
		{
			int idNew = worldObj.getBlockId(worldX, worldNewY, worldZ);
			if(idNew != 0 && idNew != 8 && idNew!= 9)
			{
				newC = true;
				PhysCollision.addIfNew(new PhysCollision(new PhysVector(cornerX, cornerY, cornerZ), 1), currentTickCollisions);
			}
		}
		if(worldZ != worldNewZ)
		{
			int idNew = worldObj.getBlockId(worldX, worldY, worldNewZ);
			if(idNew != 0 && idNew != 8 && idNew!= 9)
			{
				newC = true;
				PhysCollision.addIfNew(new PhysCollision(new PhysVector(cornerX, cornerY, cornerZ), 2), currentTickCollisions);
			}
		}
    }
    
    public void handleCollision(PhysVector worldAxis, PhysVector localPoint, float timeInc)
    {
    	PhysVector arm = cm.getSubtraction(localPoint);
    	counterForceAndTorque(worldAxis, arm, timeInc);
    }
    
    public void haultMomentAlong(PhysVector worldDirection)
    {
    	worldDirection.orient(wToLTransform);
    	worldDirection.normalize();
    	worldDirection.multiply(moment.dotProduct(worldDirection));
    	moment.subtract(worldDirection);
    }
    
    public void counterForceAndTorque(PhysVector worldAxis, PhysVector torqueArm, float timeInc)
    {
    	PhysVector localAxis = worldAxis.getOriented(wToLTransform);
    	PhysVector localMomentum = momentum.getOriented(wToLTransform);
    	
    	worldAxis.normalize();
    	localAxis.normalize();
    	
    	double nFactor = -(PhysVector.getSum(localMomentum, PhysVector.getCross(aVelocity, torqueArm).getProduct(mass))).dotProduct(localAxis);
    	double dFactor = timeInc*(1 + mass * PhysVector.getCross(PhysMatrix.get3by3TransformedVec(invMoITensor, PhysVector.getCross(torqueArm, localAxis)), torqueArm).dotProduct(localAxis));
    	
		PhysVector counterForce = worldAxis.getProduct(nFactor/dFactor);
		PhysVector counterForceLocal = localAxis.getProduct(nFactor/dFactor);
    	PhysVector counterTorque = PhysMatrix.cross(torqueArm, counterForceLocal);
    	
		force.add(counterForce);
    	torque.add(counterTorque);

    	calculateKinematics(timeInc);
    }
    
    public double getWaterPressureAt(double height)
    {
    	double depth = waterLevel - height;
    	return depth*blockMasses.get(8)*PhysManager.g;
    }
    
    public void calculateLiquidLevel(int x, int y, int z)
    {
    	waterLevel = (int)worldObj.getSeaLevel();
    	/*//FIX//
    	if(worldObj.getBlockId(x, y, z) == Block.waterStill.blockID || worldObj.getBlockId(x, y, z) == Block.waterMoving.blockID || worldObj.getBlockId(x, y, z) == Block.lavaStill.blockID || worldObj.getBlockId(x, y, z) == Block.lavaMoving.blockID)
    	{
    		calculateLiquidLevel(x, y + 1, z);
    	}
    	else
    	{
    		currentLiquidLevel = y;
    		liquidLevCalculated = true;
    	}*/
    }
   
    boolean firstCalculation = true;
    public void calculateNewLimits(int x, int y, int z)
    {
    	if(blockLocs.size() == 1 || firstCalculation)
    	{
    		int[] loc = blockLocs.get(0);
    		minX = loc[0];
    		minY = loc[1];
    		minZ = loc[2];
    		maxX = loc[0] + 1;
    		maxY = loc[1] + 1;
    		maxZ = loc[2] + 1;
    		renderGlobal.loadRenderers();
    		firstCalculation = false;
    		return;
    	}
    	boolean extended = false;
		if(x < minX)
		{
			minX = x;
			extended = true;
		}
		if(y < minY)
		{
			minY = y;
			extended = true;
		}
		if(z < minZ)
		{
			minZ = z;
			extended = true;
		}
		if(x + 1 > maxX)
		{
			maxX = x + 1;
			extended = true;
		}
		if(y + 1 > maxY)
		{
			maxY = y + 1;
			extended = true;
		}
		if(z + 1 > maxZ)
		{
			maxZ = z + 1;
			extended = true;
		}
		if(extended)
		{
			renderGlobal.loadRenderers();
		}
    }
	
    /**
     * Runs a single tick for the world
     */
    public void tick()
    {
    	if (isAllPlayersFullyAsleep())
    	{
    		boolean flag = false;

    		if (spawnHostileMobs)
    		{
    			if (difficultySetting < 1);
    		}

    		if (!flag)
    		{
    			long l = worldInfo.getWorldTime() + 24000L;
    			worldInfo.setWorldTime(l - l % 24000L);
    			wakeUpAllPlayers();
    		}
    	}

    	Profiler.startSection("mobSpawner");
    	//SpawnerAnimals.performSpawning(this, spawnHostileMobs, spawnPeacefulMobs && worldInfo.getWorldTime() % 400L == 0L);
    	//--------------------
    	
    	long l1 = worldObj.worldInfo.getWorldTime() + 1L;
    	if (l1 % (long)autosavePeriod == 0L)
    	{
    		Profiler.endStartSection("save");
    		saveWorld(false, null);
    	}
    	Profiler.endStartSection("tickPending");
    	tickUpdates(false);
    	Profiler.endStartSection("tickTiles");
    	tickBlocksAndAmbiance();
    }

    /**
     * Returns true if the specified block can be placed at the given coordinates, optionally making sure there are no
     * entities in the way.  Args: blockID, x, y, z, ignoreEntities
     */
    public boolean canBlockBePlacedAt(int par1, int par2, int par3, int par4, boolean par5, int par6)
    {
        int i = getBlockId(par2, par3, par4);
        Block block = Block.blocksList[i];
        Block block1 = Block.blocksList[par1];
        AxisAlignedBB axisalignedbb = block1.getCollisionBoundingBoxFromPool(this, par2, par3, par4);
        
        if (par5)
        {
            axisalignedbb = null;
        }

        if (axisalignedbb != null && !checkIfAABBIsClear(axisalignedbb))
        {
            return false;
        }

        if (block != null && (block == Block.waterMoving || block == Block.waterStill || block == Block.lavaMoving || block == Block.lavaStill || block == Block.fire || block.blockMaterial.isGroundCover()))
        {
            block = null;
        }

        return par1 > 0 && block == null && block1.canPlaceBlockOnSide(this, par2, par3, par4, par6);
    }

    /**
     * If on MP, sends a quitting packet.
     */
    public void sendQuittingDisconnectingPacket()
    {
    }
    
    
    public long getWorldTime()
    {
        return worldObj.worldInfo.getWorldTime();
    }

    /**
     * Returns the coordinates of the spawn point
     */
    public ChunkCoordinates getSpawnPoint()
    {
        return new ChunkCoordinates(worldObj.worldInfo.getSpawnX(), worldObj.worldInfo.getSpawnY(), worldObj.worldInfo.getSpawnZ());
    }

    public void setSpawnPoint(ChunkCoordinates par1ChunkCoordinates)
    {
        worldInfo.setSpawnPosition(par1ChunkCoordinates.posX, par1ChunkCoordinates.posY, par1ChunkCoordinates.posZ);
    }

    /**
     * spwans an entity and loads surrounding chunks
     */
    public void joinEntityInSurroundings(Entity par1Entity)
    {
        int i = MathHelper.floor_double(par1Entity.posX / 16D);
        int j = MathHelper.floor_double(par1Entity.posZ / 16D);
        byte byte0 = 2;

        for (int k = i - byte0; k <= i + byte0; k++)
        {
            for (int l = j - byte0; l <= j + byte0; l++)
            {
                getChunkFromChunkCoords(k, l);
            }
        }

        if (!loadedEntityList.contains(par1Entity))
        {
            loadedEntityList.add(par1Entity);
        }
    }

    public void updateEntityList()
    {
        super.updateEntityList();
    }

    /**
     * gets the IChunkProvider this world uses.
     */
    public IChunkProvider getChunkProvider()
    {
        return chunkProvider;
    }

    /**
     * plays a given note at x, y, z. args: x, y, z, instrument, note
     */
    public void playNoteAt(int par1, int par2, int par3, int par4, int par5)
    {
        int i = getBlockId(par1, par2, par3);

        if (i > 0)
        {
            Block.blocksList[i].powerBlock(this, par1, par2, par3, par4, par5);
        }
    }
    
    /**
     * Returns an unique new data id from the MapStorage for the given prefix and saves the idCounts map to the
     * 'idcounts' file.
     */
    public int getUniqueDataId(String par1Str)
    {
        return mapStorage.getUniqueDataId(par1Str);
    }

    /**
     * See description for playAuxSFX.
     */
    public void playAuxSFX(int par1, int par2, int par3, int par4, int par5)
    {
        playAuxSFXAtEntity(null, par1, par2, par3, par4, par5);
    }

    /**
     * See description for playAuxSFX.
     */
    public void playAuxSFXAtEntity(EntityPlayer par1EntityPlayer, int par2, int par3, int par4, int par5, int par6)
    {
        for (int i = 0; i < worldAccesses.size(); i++)
        {
            ((IWorldAccess)worldAccesses.get(i)).playAuxSFX(par1EntityPlayer, par2, par3, par4, par5, par6);
        }
    }
    
    public void removeBlocksMomentum(int x, int y, int z, double blockMass)
    {
    	moment.multiply(1 - (blockMass/mass));
    	momentum.multiply(1 - (blockMass/mass));
    }

	public void updateBlocksCorners(int x, int y, int z)
	{
		updateCorner(x, y, z);
		updateCorner(x + 1, y, z);
		updateCorner(x, y, z + 1);
		updateCorner(x + 1, y, z + 1);
		updateCorner(x, y + 1, z);
		updateCorner(x + 1, y + 1, z);
		updateCorner(x, y + 1, z + 1);
		updateCorner(x + 1, y + 1, z + 1);
	}
	
	public void updateBlocksFaces(int x, int y, int z)
	{
		updateFace(x, y, z, 1);
		updateFace(x, y, z, 2);
		updateFace(x, y, z, 3);
		updateFace(x + 1, y, z, 1);
		updateFace(x, y + 1, z, 2);
		updateFace(x, y, z + 1, 3);
	}
	
	public void updateFace(int x, int y, int z, int axis)
	{
		int faceNormal = faceNormal(x, y, z, axis);
		if(faceNormal == 0)
		{
			removeFace(x, y, z, axis);
		}
		else
		{
			addFace(x, y, z, faceNormal);
		}
	}
	
	public void addFace(int x, int y, int z, int normal)
	{
		for(int i = 0; i < faces.size(); i+=4)
		{
			if(x == faces.get(i))
			{
				if(y == faces.get(i+1))
				{
					if(z == faces.get(i+2))
					{
						if(normal == faces.get(i+3))
						{
							return;
						}
						if(-normal == faces.get(i+3))
						{
							faces.set(i+3, normal);
							return;
						}
					}
				}
			}
		}
		faces.add(x);
		faces.add(y);
		faces.add(z);
		faces.add(normal);
	}
	
	public void removeFace(int x, int y, int z, int axis)
	{
		for(int i = 0; i < faces.size(); i+=4)
		{
			if(x == faces.get(i))
			{
    			if(y == faces.get(i+1))
    			{
    				if(z == faces.get(i+2))
    				{
    					if(axis == Math.abs(faces.get(i+3)))
    					{
        					faces.remove(i+3);
        					faces.remove(i+2);
        					faces.remove(i+1);
        					faces.remove(i);
        					break;
    					}
    				}
    			}
			}
		}
	}
	
	public void updateCorner(int x, int y, int z)
	{
		if(cornerCovered(x, y, z) || !aBlockCorner(x, y, z))
		{
			removeCorner(x, y, z);
		}
		else
		{
			addCorner(x, y, z);
		}
	}
	
	public void removeCorner(int x, int y, int z)
	{
		for(int i = 0; i < corners.size(); i+=3)
		{
			if(x == corners.get(i))
			{
    			if(y == corners.get(i+1))
    			{
    				if(z == corners.get(i+2))
    				{
    					corners.remove(i+2);
    					corners.remove(i+1);
    					corners.remove(i);
    					break;
    				}
    			}
			}
		}
	}
	
	public void addCorner(int x, int y, int z)
	{
		for(int i = 0; i < corners.size(); i+=3)
		{
			if(x == corners.get(i))
			{
				if(y == corners.get(i+1))
				{
					if(z == corners.get(i+2))
					{
						return;
					}
				}
			}
		}
		corners.add(x);
		corners.add(y);
		corners.add(z);
	}
	
	public boolean cornerCovered(int x, int y, int z)
	{
		if(getBlockId(x, y, z) == 0)
		{
			return false;
		}
		if(getBlockId(x - 1, y, z) == 0)
		{
			return false;
		}
		if(getBlockId(x, y, z - 1) == 0)
		{
			return false;
		}
		if(getBlockId(x - 1, y, z - 1) == 0)
		{
			return false;
		}
		if(getBlockId(x, y - 1, z) == 0)
		{
			return false;
		}
		if(getBlockId(x - 1, y - 1, z) == 0)
		{
			return false;
		}
		if(getBlockId(x, y - 1, z - 1) == 0)
		{
			return false;
		}
		if(getBlockId(x - 1, y - 1, z - 1) == 0)
		{
			return false;
		}
		return true;
	}
	
	public int faceNormal(int x, int y, int z, int axis)
	{
		int normal = 0;

		if(axis == 1)
		{
			if(getBlockId(x, y, z) == 0)
			{
				normal += 1;
			}
			if(getBlockId(x - 1, y, z) == 0)
			{
				normal -= 1;
			}
		}
		else if(axis == 2)
		{
			if(getBlockId(x, y, z) == 0)
			{
				normal += 2;
			}
			if(getBlockId(x, y - 1, z) == 0)
			{
				normal -= 2;
			}
		}
		else
		{
			if(getBlockId(x, y, z) == 0)
			{
				normal += 3;
			}
			if(getBlockId(x, y, z - 1) == 0)
			{
				normal -= 3;
			}
		}
		return normal;
	}
	
	public boolean aBlockCorner(int x, int y, int z)
	{
		if(getBlockId(x, y, z) != 0)
		{
			return true;
		}
		if(getBlockId(x - 1, y, z) != 0)
		{
			return true;
		}
		if(getBlockId(x, y, z - 1) != 0)
		{
			return true;
		}
		if(getBlockId(x - 1, y, z - 1) != 0)
		{
			return true;
		}
		if(getBlockId(x, y - 1, z) != 0)
		{
			return true;
		}
		if(getBlockId(x - 1, y - 1, z) != 0)
		{
			return true;
		}
		if(getBlockId(x, y - 1, z - 1) != 0)
		{
			return true;
		}
		if(getBlockId(x - 1, y - 1, z - 1) != 0)
		{
			return true;
		}
		return false;
	}
	
	public void resetMotion()
	{
		momentum.zero();
		moment.zero();
	}
	
	public void removeTies()
	{
		for(int i = 0; i < ties.size(); i++)
		{
			ties.get(i).joint.tie = null;
		}
		ties.clear();
	}
	
	public int getLightBrightnessForSkyBlocks(int par1, int par2, int par3, int par4)
    {
        return worldObj.getLightBrightnessForSkyBlocks(par1, par2, par3, par4);
    }
	
	public boolean blockExists(int par1, int par2, int par3)
    {
		return true;
    }
    
	/*
	public void updateBlockLocsA(int x, int y, int z, int id)
	{
	    y -= 128;
	    int index = 1;
	    int xIndex;
	    int yIndex;
	    int zIndex;
	    if(x < 0)
	    {
	    	xIndex = -x;
	    	index = index << 1;
	    }
	    else
	    {
	    	xIndex = x;
	    }
	    if(y < 0)
	    {
	    	yIndex = -y << 8;
	    	index = index << 2;
	    }
	    else
	    {
	    	yIndex = y << 8;
	    }
	    if(z < 0)
	    {
	    	zIndex = -z << 16;
	    	index = index << 4;
	    }
	    else
	    {
	    	zIndex = z << 16;
	    }

	    if(id != 0)
	    {
	    	blockLocsA[xIndex + yIndex + zIndex] = (byte)(blockLocsA[xIndex + yIndex + zIndex] | index);
	    	blockLocsA[xIndex + yIndex + zIndex] = (byte)(blockLocsA[xIndex + yIndex + zIndex] | index);
	    }
	    else
	    {
	    	index = index ^ 0xf;
	    	blockLocsA[xIndex + yIndex + zIndex] = (byte)(blockLocsA[xIndex + yIndex + zIndex] & index);
	    	blockLocsA[xIndex + yIndex + zIndex] = (byte)(blockLocsA[xIndex + yIndex + zIndex] & index);
	    }
	}
	public boolean blockAt(int x, int y, int z)
    {
    	y -= 128;
    	int index = 0;
    	int xIndex;
    	int yIndex;
    	int zIndex;
    	if(x < 0)
    	{
    		index++;;
    		xIndex = -x;
    	}
    	else
    	{
    		xIndex = x;
    	}
    	if(y < 0)
    	{
    		yIndex = -y << 8;
    		index += 2;
    	}
    	else
    	{
    		yIndex = y << 8;
    	}
    	if(z < 0)
    	{
    		zIndex = -z << 16;
    		index += 4;
    	}
    	else
    	{
    		zIndex = z << 16;
    	}
		int value = (blockLocsA[xIndex + yIndex + zIndex] & (1 << index)) >>> index;
    		
    	if(value == 1)
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }*/
	
	public void setCornerSealValAt(int x, int y, int z, boolean sealed)
	{
		/*y -= 128;
    	int index = 1;
    	int xIndex;
    	int yIndex;
    	int zIndex;
    	if(x < 0)
    	{
    		xIndex = -x;
    		index = index << 1;
    	}
    	else
    	{
    		xIndex = x;
    	}
    	if(y < 0)
    	{
    		yIndex = -y << 8;
    		index = index << 2;
    	}
    	else
    	{
    		yIndex = y << 8;
    	}
    	if(z < 0)
    	{
    		zIndex = -z << 16;
    		index = index << 4;
    	}
    	else
    	{
    		zIndex = z << 16;
    	}
    	
    	if(sealed)
    	{
        	cornersSealedVals[xIndex + yIndex + zIndex] = (byte)(cornersSealedVals[xIndex + yIndex + zIndex] | index);
    	}
    	else
    	{
        	index = index ^ 0xf;
        	cornersSealedVals[xIndex + yIndex + zIndex] = (byte)(cornersSealedVals[xIndex + yIndex + zIndex] & index);
    	}*/
	}
	
    public boolean blockSealedAt(int x, int y, int z)
    {
    	return false;
    	/*y -= 128;
    	int index = 0;
    	int xIndex;
    	int yIndex;
    	int zIndex;
    	if(x < 0)
    	{
    		index++;;
    		xIndex = -x;
    	}
    	else
    	{
    		xIndex = x;
    	}
    	if(y < 0)
    	{
    		yIndex = -y << 8;
    		index += 2;
    	}
    	else
    	{
    		yIndex = y << 8;
    	}
    	if(z < 0)
    	{
    		zIndex = -z << 16;
    		index += 4;
    	}
    	else
    	{
    		zIndex = z << 16;
    	}
		int value = (blocksSealedVals[xIndex + yIndex + zIndex] & (1 << index)) >>> index;
    		
    	if(value == 1)
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}*/
    }
    
    public boolean cornerSealedAt(int x, int y, int z)
    {
    	return false;
    	/*y -= 128;
    	int index = 0;
    	int xIndex;
    	int yIndex;
    	int zIndex;
    	if(x < 0)
    	{
    		index++;;
    		xIndex = -x;
    	}
    	else
    	{
    		xIndex = x;
    	}
    	if(y < 0)
    	{
    		yIndex = -y << 8;
    		index += 2;
    	}
    	else
    	{
    		yIndex = y << 8;
    	}
    	if(z < 0)
    	{
    		zIndex = -z << 16;
    		index += 4;
    	}
    	else
    	{
    		zIndex = z << 16;
    	}
		int value = (cornersSealedVals[xIndex + yIndex + zIndex] & (1 << index)) >>> index;
    		
    	if(value == 1)
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}*/
    }
    
    public void setBlocksCornersSealVals(int x, int y, int z, boolean sealed)
    {
    	setCornerSealValAt(x, y, z, sealed);
    	setCornerSealValAt(x, y + 1, z, sealed);
    	setCornerSealValAt(x, y, z + 1, sealed);
    	setCornerSealValAt(x, y + 1, z + 1, sealed);
    	setCornerSealValAt(x + 1, y, z, sealed);
    	setCornerSealValAt(x + 1, y + 1, z, sealed);
    	setCornerSealValAt(x + 1, y, z + 1, sealed);
    	setCornerSealValAt(x + 1, y + 1, z + 1, sealed);
    }
    
    public void setBlockSealValAt(int x, int y, int z, boolean sealed)
    {
    	/*setBlocksCornersSealVals(x, y, z, sealed);
    	
    	y -= 128;
    	int index = 1;
    	int xIndex;
    	int yIndex;
    	int zIndex;
    	if(x < 0)
    	{
    		xIndex = -x;
    		index = index << 1;
    	}
    	else
    	{
    		xIndex = x;
    	}
    	if(y < 0)
    	{
    		yIndex = -y << 8;
    		index = index << 2;
    	}
    	else
    	{
    		yIndex = y << 8;
    	}
    	if(z < 0)
    	{
    		zIndex = -z << 16;
    		index = index << 4;
    	}
    	else
    	{
    		zIndex = z << 16;
    	}
    	
    	if(sealed)
    	{
        	blocksSealedVals[xIndex + yIndex + zIndex] = (byte)(blocksSealedVals[xIndex + yIndex + zIndex] | index);
    	}
    	else
    	{
        	index = index ^ 0xf;
        	blocksSealedVals[xIndex + yIndex + zIndex] = (byte)(blocksSealedVals[xIndex + yIndex + zIndex] & index);
    	}*/
    }
    
    public boolean isCornerWaterSealed(int x, int y, int z)
    {
    	if(cornerSealedAt(x, y, z))
    	{
    		return true;
    	}
    	if(isWaterSealed(x, y, z) && getBlockId(x, y, z) == 0)
    	{
    		return true;
    	}
    	if(isWaterSealed(x, y - 1, z) && getBlockId(x, y - 1, z) == 0)
    	{
    		return true;
    	}
    	if(isWaterSealed(x, y, z - 1) && getBlockId(x, y, z - 1) == 0)
    	{
    		return true;
    	}
    	if(isWaterSealed(x, y - 1, z - 1) && getBlockId(x, y - 1, z - 1) == 0)
    	{
    		return true;
    	}
    	if(isWaterSealed(x - 1, y, z) && getBlockId(x - 1, y, z) == 0)
    	{
    		return true;
    	}
    	if(isWaterSealed(x - 1, y - 1, z) && getBlockId(x - 1, y - 1, z) == 0)
    	{
    		return true;
    	}
    	if(isWaterSealed(x - 1, y, z - 1) && getBlockId(x - 1, y, z - 1) == 0)
    	{
    		return true;
    	}
    	if(isWaterSealed(x - 1, y - 1, z - 1) && getBlockId(x - 1, y - 1, z - 1) == 0)
    	{
    		return true;
    	}
    	
    	return false;
    }

    public boolean isWaterSealed(int x, int y, int z)
    {
    	if(blockSealedAt(x, y, z))
    	{
    		return true;
    	}
    	if(x < minX || x > maxX || y < minY || y > maxY || z < minZ || z > maxZ)
    	{
    		setBlockSealValAt(x, y, z, false);
    		return false;
    	}

    	PhysVector vec = new PhysVector(x, y, z);

		double[] newTransform = PhysMatrix.getMatrixProduct(lToWTransform, transformInc);
		PhysMatrix.applyTransform(newTransform, vec);
		
    	if(waterLevel <= MathHelper.floor_double(vec.Y))
    	{
    		return true;
    	}
    	
    	if(getBlockId(x, y, z) != 0)
    	{
    		return true;
    	}

    	setBlockSealValAt(x, y, z, true);
    	//if(getBlockId(x + 1, y, z) != 0)
    	if(!isWaterSealed(x + 1, y, z))
    	{
    		setBlockSealValAt(x, y, z, false);
    		return false;
    	}
    	if(!isWaterSealed(x - 1, y, z))
    	{
    		setBlockSealValAt(x, y, z, false);
    		return false;
    	}
    	if(!isWaterSealed(x, y + 1, z))
    	{
    		setBlockSealValAt(x, y, z, false);
    		return false;
    	}
    	if(!isWaterSealed(x, y - 1, z))
    	{
    		setBlockSealValAt(x, y, z, false);
    		return false;
    	}
    	if(!isWaterSealed(x, y, z + 1))
    	{
    		setBlockSealValAt(x, y, z, false);
    		return false;
    	}
    	if(!isWaterSealed(x, y, z - 1))
    	{
    		setBlockSealValAt(x, y, z, false);
    		return false;
    	}

		return true;
    }
	//---------------//
}

