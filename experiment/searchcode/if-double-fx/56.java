package net.minecraft.src;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;

public class PhysController {
	
	public static PhysRopeJoint grabbedRope;
	
    public static int ControlledSubIndex = 0;

    public static boolean easyControls = true;
    public static KeyBinding keyBindBlockForward = new KeyBinding("key.structforward", 23);
    public static KeyBinding keyBindBlockBack = new KeyBinding("key.structforward", 37);
    public static KeyBinding keyBindBlockRight = new KeyBinding("key.structforward", 36);
    public static KeyBinding keyBindBlockLeft = new KeyBinding("key.structforward", 38);
    public static KeyBinding keyBindBlockUp = new KeyBinding("key.structforward", 22);
    public static KeyBinding keyBindBlockDown = new KeyBinding("key.structforward", 24);
    public static KeyBinding keyBindBlockStop = new KeyBinding("key.structforward", 25);
    public static KeyBinding keyBindBlockRotXP = new KeyBinding("key.structforward", 33);
    public static KeyBinding keyBindBlockRotXN = new KeyBinding("key.structforward", 35);
    public static KeyBinding keyBindBlockRotYP = new KeyBinding("key.structforward", 19);
    public static KeyBinding keyBindBlockRotYN = new KeyBinding("key.structforward", 21);
    public static KeyBinding keyBindBlockRotZP = new KeyBinding("key.structforward", 47);
    public static KeyBinding keyBindBlockRotZN = new KeyBinding("key.structforward", 34);
    public static KeyBinding keyBindLocal = new KeyBinding("key.local", 56);
    public static KeyBinding keyBindGravity = new KeyBinding("key.gravity", 45);
    public static KeyBinding keyBindEasyControls = new KeyBinding("key.simpleC", 46);
    public static KeyBinding keyBindScaling = new KeyBinding("key.scale", 44);
	
	public static Object getControllerFieldValue(String string)
	{
		try
		{
			PlayerController controller = PhysManagerMC.mc().playerController;
			Field field = controller.getClass().getDeclaredField(string);
			field.setAccessible(true);
			return field.get(controller);
			
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Field getControllerField(String string)
	{
		try
		{
			PlayerController controller = PhysManagerMC.mc().playerController;
			Field field = controller.getClass().getDeclaredField(string);
			field.setAccessible(true);
			return field;
			
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean onPlayerDestroySubBlock(int par1, int par2, int par3, int par4, PhysWorld sWorld)
    {
        Block block = Block.blocksList[sWorld.getBlockId(par1, par2, par3)];

        if (block == null)
        {
            return false;
        }

        sWorld.playAuxSFX(2001, par1, par2, par3, block.blockID + (sWorld.getBlockMetadata(par1, par2, par3) << 12));
        int i = sWorld.getBlockMetadata(par1, par2, par3);
        boolean flag = sWorld.setBlockWithNotify(par1, par2, par3, 0);

        if (flag)
        {
            block.onBlockDestroyedByPlayer(sWorld, par1, par2, par3, i);
        }

        return flag;
    }
	
	public static void clickSubBlockCreative(Minecraft par0Minecraft, PlayerController par1PlayerController, int par2, int par3, int par4, int par5, PhysWorld sWorld)
    {
        if (sWorld.func_48457_a(par0Minecraft.thePlayer, par2, par3, par4, par5))
        {
            PhysController.onPlayerDestroySubBlock(par2, par3, par4, par5, sWorld);
        }
    }
	
    public static void clickBlock(int par1, int par2, int par3, int par4, PhysWorld sWorld)
    {
    	Minecraft mc = PhysManagerMC.mc();
        if (!mc.thePlayer.canPlayerEdit(par1, par2, par3))
        {
            return;
        }

        sWorld.func_48457_a(mc.thePlayer, par1, par2, par3, par4);
        int i = sWorld.getBlockId(par1, par2, par3);

        if (i > 0 && (Float)getControllerFieldValue("curBlockDamage") == 0.0F)
        {
            Block.blocksList[i].onBlockClicked(sWorld, par1, par2, par3, mc.thePlayer);
        }

        if (i > 0 && Block.blocksList[i].blockStrength(mc.thePlayer) >= 1.0F)
        {
            onPlayerDestroySubBlock(par1, par2, par3, par4, sWorld);
        }
    }
    
    public static boolean onSPlayerDestroySubBlock(int par1, int par2, int par3, int par4, PhysWorld sWorld)
    {
    	EntityPlayer thePlayer = PhysManagerMC.mc().thePlayer;
        int i = sWorld.getBlockId(par1, par2, par3);
        int j = sWorld.getBlockMetadata(par1, par2, par3);
        boolean flag = PhysController.onPlayerDestroySubBlock(par1, par2, par3, par4, sWorld);
        ItemStack itemstack = thePlayer.getCurrentEquippedItem();
        boolean flag1 = thePlayer.canHarvestBlock(Block.blocksList[i]);

        if (itemstack != null)
        {
            itemstack.onDestroyBlock(i, par1, par2, par3,thePlayer);

            if (itemstack.stackSize == 0)
            {
                itemstack.onItemDestroyedByUse(thePlayer);
                thePlayer.destroyCurrentEquippedItem();
            }
        }

        if (flag && flag1)
        {
            Block.blocksList[i].harvestBlock(sWorld, thePlayer, par1, par2, par3, j);
        }

        return flag;
    }
    
    public static void onPlayerDamageBlockSP(int par1, int par2, int par3, int par4, PhysWorld sWorld)
    {
    	Minecraft mc = PhysManagerMC.mc();
    	PlayerController controller = mc.playerController;

    	Field blHitWait = getControllerField("blockHitWait");
    	Field cBlockX = getControllerField("curBlockX");
    	Field cBlockY = getControllerField("curBlockY");
    	Field cBlockZ = getControllerField("curBlockZ");
    	Field cBlockDamage = getControllerField("curBlockDamage");
    	Field pBlockDamage = getControllerField("prevBlockDamage");
    	Field blDestroySoundCounter = getControllerField("blockDestroySoundCounter");

    	try {
    		int blockHitWait = blHitWait.getInt(controller);
    		int curBlockX = cBlockX.getInt(controller);
    		int curBlockY = cBlockY.getInt(controller);
    		int curBlockZ = cBlockZ.getInt(controller);
    		float curBlockDamage = cBlockDamage.getFloat(controller);
    		float prevBlockDamage = pBlockDamage.getFloat(controller);
    		float blockDestroySoundCounter = blDestroySoundCounter.getFloat(controller);

    		if (blockHitWait > 0)
    		{
    			blHitWait.setInt(controller, blockHitWait - 1);
    			return;
    		}

    		if (par1 == curBlockX && par2 == curBlockY && par3 == curBlockZ)
    		{
    			int i = sWorld.getBlockId(par1, par2, par3);

    			if (!mc.thePlayer.canPlayerEdit(par1, par2, par3))
    			{
    				return;
    			}

    			if (i == 0)
    			{
    				return;
    			}

    			Block block = Block.blocksList[i];
    			cBlockDamage.setFloat(controller, curBlockDamage + block.blockStrength(mc.thePlayer));

    			if (blockDestroySoundCounter % 4F == 0.0F && block != null)
    			{
    				mc.sndManager.playSound(block.stepSound.getStepSound(), (float)par1 + 0.5F, (float)par2 + 0.5F, (float)par3 + 0.5F, (block.stepSound.getVolume() + 1.0F) / 8F, block.stepSound.getPitch() * 0.5F);
    			}

    			blDestroySoundCounter.setFloat(controller, blockDestroySoundCounter + 1);

    			if (curBlockDamage >= 1.0F)
    			{
    				PhysController.onPlayerDestroySubBlock(par1, par2, par3, par4, sWorld);
    				cBlockDamage.setFloat(controller, 0.0F);
    				pBlockDamage.setFloat(controller, 0.0F);
    				blDestroySoundCounter.setFloat(controller, 0.0F);
    				blHitWait.setInt(controller, 5);
    			}
    		}
    		else
    		{
    			cBlockDamage.set(controller, 0.0F);
    			pBlockDamage.set(controller, 0.0F);
    			blDestroySoundCounter.set(controller, 0.0F);
    			cBlockX.setInt(controller, par1);
    			cBlockY.setInt(controller, par2);
    			cBlockZ.setInt(controller, par3);
    		}
    	} catch (IllegalArgumentException e) {
    		e.printStackTrace();
    	} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
    }
    
    public static void clickBlockCreative(PhysWorld sWorld, int i, int j, int k, int sideHit)
    {
    	Minecraft mc = PhysManagerMC.mc();
    	PlayerController playerController = mc.playerController;
    	EntityPlayer thePlayer = mc.thePlayer;
    	
    	PlayerControllerCreative playc = (PlayerControllerCreative)playerController;
    	if (!sWorld.func_48457_a(thePlayer, i, j, k, sideHit))
        {
            Block block = Block.blocksList[sWorld.getBlockId(i, j, k)];

            if (block != null)
            {
                sWorld.playAuxSFX(2001, i, j, k, block.blockID + (sWorld.getBlockMetadata(i, j, k) << 12));
                int meta = sWorld.getBlockMetadata(i, j, k);
                boolean newFlag = sWorld.setBlockWithNotify(i, j, k, 0);

                if (newFlag)
                {
                    block.onBlockDestroyedByPlayer(sWorld, i, j, k, meta);
                }
            }
        }
    	Field pField;
		try {
			pField = playc.getClass().getDeclaredField("field_35647_c");
        	pField.setAccessible(true);
			pField.set(playc, 5);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
    }
    
    public static void onPlayerDamageBlockCreative(int i, int j, int k, int sideHit, PhysWorld sWorld)
    {
    	Minecraft mc = PhysManagerMC.mc();
    	PlayerController playerController = mc.playerController;
    	EntityPlayer thePlayer = (EntityPlayer)mc.thePlayer;
    	
    	PlayerControllerCreative playc = (PlayerControllerCreative)playerController;
    	Field pField;
		try {
			pField = playc.getClass().getDeclaredField("field_35647_c");
        	pField.setAccessible(true);
			pField.set(playc, (Integer)pField.get(playc) - 1);
			if ((Integer)pField.get(playc) <= 0)
            {
            	pField.set(playc, 5);
            	if (!sWorld.func_48457_a(thePlayer, i, j, k, sideHit))
                {
                    Block block = Block.blocksList[sWorld.getBlockId(i, j, k)];

                    if (block != null)
                    {
	                    sWorld.playAuxSFX(2001, i, j, k, block.blockID + (sWorld.getBlockMetadata(i, j, k) << 12));
	                    int meta = sWorld.getBlockMetadata(i, j, k);
	                    boolean newFlag = sWorld.setBlockWithNotify(i, j, k, 0);
	
	                    if (newFlag)
	                    {
	                        block.onBlockDestroyedByPlayer(sWorld, i, j, k, meta);
	                    }
                    }
                }
            }
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
    }
    
    public static void grabRopeAt(PhysRopeJoint joint)
    {
    	EntityPlayer player = PhysManagerMC.mc().thePlayer;
    	if(grabbedRope != null)
    	{
    		grabbedRope.tie = null;
    		grabbedRope = null;
    	}
    	
    	PhysVector j = new PhysVector(player.posX, player.posY, player.posZ);
    	
    	double yaw = player.renderYawOffset;
    	double handX = -0.4*Math.cos(yaw*Math.PI*11.0/(12.0*180.0));
    	double handZ = -0.4*Math.sin(yaw*Math.PI*11.0/(12.0*180.0));
    	
    	j.add(handX, -0.6F, handZ);
    	
    	joint.moveTo(j);
    	
    	grabbedRope = joint;
    	joint.tie = new PhysTie(joint, new PhysVector(player.posX, player.posY, player.posZ), -2);
    }
    
    public static void checkShipControls()
    {
    	if(PhysManagerShips.subWorlds == null)
    	{
    		return;
    	}
    	if(ControlledSubIndex > PhysManagerShips.subWorlds.size() - 1)
    	{
    		return;
    	}
    	PhysWorld sWorld = PhysManagerShips.subWorlds.get(ControlledSubIndex);

    	/*if(keyBindScaling.isPressed())
    	{
    		if(sWorld.scaling)
    		{
    			sWorld.scaling = false;
    		}
    		else
    		{
    			sWorld.scaling = true;
    		}
    	}*/
    	
    	if(sWorld.scaling)
    	{
        	if(keyBindBlockForward.pressed)
        	{
        		sWorld.lToWTransform = PhysMatrix.getMatrixProduct(sWorld.lToWTransform, PhysMatrix.getScaleMatrix(new PhysVector(1, 1, 1.01)));
        		sWorld.scale.Z *= 1.01;
        	}
        	if(keyBindBlockBack.pressed)
        	{
        		sWorld.lToWTransform = PhysMatrix.getMatrixProduct(sWorld.lToWTransform, PhysMatrix.getScaleMatrix(new PhysVector(1, 1, .99)));
        		sWorld.scale.Z *= .99;
        	}
        	if(keyBindBlockRight.pressed)
        	{
        		sWorld.lToWTransform = PhysMatrix.getMatrixProduct(sWorld.lToWTransform, PhysMatrix.getScaleMatrix(new PhysVector(1.01, 1, 1)));
        		sWorld.scale.X *= 1.01;
        	}
        	
        	if(keyBindBlockLeft.pressed)
        	{
        		sWorld.lToWTransform = PhysMatrix.getMatrixProduct(sWorld.lToWTransform, PhysMatrix.getScaleMatrix(new PhysVector(.99, 1, 1)));
        		sWorld.scale.X *= .99;
        	}
        	if(keyBindBlockUp.pressed)
        	{
        		sWorld.lToWTransform = PhysMatrix.getMatrixProduct(sWorld.lToWTransform, PhysMatrix.getScaleMatrix(new PhysVector(1, 1.01, 1)));
        		sWorld.scale.Y *= 1.01;
        	}
        	if(keyBindBlockDown.pressed)
        	{
        		sWorld.lToWTransform = PhysMatrix.getMatrixProduct(sWorld.lToWTransform, PhysMatrix.getScaleMatrix(new PhysVector(1, .99, 1)));
        		sWorld.scale.Y *= .99;
        	}
    		sWorld.wToLTransform = PhysMatrix.inverse(sWorld.lToWTransform, sWorld.scale);
        	return;
    	}
    	
		double pushMult = 2;
		double twistMult = 1;
		
    	if(keyBindEasyControls.isPressed())
    	{
    		if(easyControls)
    		{
        		easyControls = false;
    		}
    		else
    		{
        		easyControls = true;
    		}
    	}
    	
    	if(keyBindLocal.pressed)
    	{
    		sWorld.localControls = true;
    	}
    	else
    	{
    		sWorld.localControls = false;
    	}
    		
    	if(easyControls)
    	{
    		sWorld.localControls = true;
        	if(keyBindBlockForward.pressed)
        	{
        		sWorld.push.Z += sWorld.mass * pushMult;
        	}
        	if(keyBindBlockBack.pressed)
        	{
        		sWorld.push.Z -= sWorld.mass * pushMult;
        	}
        	if(keyBindBlockRight.pressed)
        	{
                sWorld.twist.Y += sWorld.MoITensor[4] * twistMult;
        	}
        	if(keyBindBlockLeft.pressed)
        	{
        		sWorld.twist.Y -= sWorld.MoITensor[4] * twistMult;
        	}
        	
        	if(keyBindBlockStop.pressed)
        	{
        		sWorld.resetMotion();
        	}
        	return;
    	}
    	
    	if(keyBindBlockForward.pressed)
    	{
    		sWorld.push.Z += sWorld.mass * pushMult;
    	}
    	if(keyBindBlockBack.pressed)
    	{
    		sWorld.push.Z -= sWorld.mass * pushMult;
    	}
    	if(keyBindBlockRight.pressed)
    	{
    		sWorld.push.X += sWorld.mass * pushMult;
    	}
    	if(keyBindBlockLeft.pressed)
    	{
    		sWorld.push.X -= sWorld.mass * pushMult;
    	}
    	if(keyBindBlockUp.pressed)
    	{
    		sWorld.push.Y += sWorld.mass * pushMult + PhysManager.g*sWorld.mass;
    	}
    	if(keyBindBlockDown.pressed)
    	{
    		sWorld.push.Y -= sWorld.mass * pushMult - PhysManager.g*sWorld.mass;
    	}
    	
        if(keyBindBlockRotXP.pressed)
        {
        	sWorld.twist.X += sWorld.MoITensor[0] * twistMult;
        }
        if(keyBindBlockRotXN.pressed)
        {
        	sWorld.twist.X -= sWorld.MoITensor[0] * twistMult;
        }
        if(keyBindBlockRotYP.pressed)
        {
        	sWorld.twist.Y += sWorld.MoITensor[4] * twistMult;
        }
        if(keyBindBlockRotYN.pressed)
        {
        	sWorld.twist.Y -= sWorld.MoITensor[4] * twistMult;
        }
        if(keyBindBlockRotZP.pressed)
        {
        	sWorld.twist.Z += sWorld.MoITensor[8] * twistMult;
        }
        if(keyBindBlockRotZN.pressed)
        {
        	sWorld.twist.Z -= sWorld.MoITensor[8] * twistMult;
        }
    	if(keyBindBlockStop.pressed)
    	{
    		if(PhysManagerMC.mc().theWorld instanceof WorldClient)
    		{
    			//FIX//((WorldClient)theWorld).sendQueue.addToSendQueue(new Packet252ShipLocation(0, theBoatWorld.subPosX, theBoatWorld.subPosY, theBoatWorld.subPosZ, 0, 0, 0));
    		}
    		sWorld.resetMotion();
    	}
    	if(keyBindGravity.isPressed())
    	{
    		if(PhysManager.g == 0.0)
    		{
    			PhysManager.g = PhysManager.EARTH_GRAVITY;
    		}
    		else
    		{
    			PhysManager.g = 0.0;
    		}
    	}
    	//FIX//
    	double fx = 0.0;
		double fy = 0.0;
		double fz = 0.0;
		
		
    	//FIX//if(fx != 0  || fy != 0 || fz != 0)
    	/*{
    		if(theWorld.isRemote)
    		{
    			if(!theBoatWorld.pushOnGoing)
    			{
    				theBoatWorld.lastPush = System.currentTimeMillis();
    				theBoatWorld.pushOnGoing = true;
    			}
    			((WorldClient)theWorld).sendQueue.addToSendQueue(new Packet251MoveShip(0, fx, fy, fz, theBoatWorld.lastTimeInc));
    		}
    	}
    	else
    	{
    		theBoatWorld.pushOnGoing = false;
    	}*/
    }
}

