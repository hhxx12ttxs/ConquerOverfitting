package net.minecraft.src;

import java.util.ArrayList;
import java.util.HashMap;

public class PhysManager {
	
	public static final double EARTH_GRAVITY = 9.8;
	public static double g = PhysManager.EARTH_GRAVITY;
	
	public static PhysVector windCurrent = new PhysVector(0, 0, 0);
	public static PhysVector waterCurrent = new PhysVector(0, 0, 0);
	
	public static long currentLoopTime = System.currentTimeMillis();
	public static long prevLoopTime = System.currentTimeMillis();
	
	public static int timeStepMilli = 15;
	public static double timeStepSeconds;
	public static double timeStepMilliInv;
	public static double timeStepSecondsInv;
	
	public static int particleTimeStepMilli = 45;
	public static double particleTimeStepSeconds;
	public static int particleTimeStepMilliInv;
	public static double particleTimeStepSecondsInv;
	
	//public static PhysFluidCell cell = new PhysFluidCell(new PhysVector(-5, 55, -5), new PhysVector(10, 90, 10));
	
	static
	{
		timeStepSeconds = ((double)timeStepMilli)/1000.0;
		timeStepMilliInv = 1/timeStepMilli;
		timeStepSecondsInv = 1/timeStepSeconds;
		
		particleTimeStepSeconds = ((double)particleTimeStepMilli)/1000.0;
		particleTimeStepMilliInv = 1/particleTimeStepMilli;
		particleTimeStepSecondsInv = 1/particleTimeStepSeconds;
	}
	
	public static HashMap<Integer, Double> blockMasses = new HashMap<Integer, Double>();
	static
	{
		blockMasses.put(0, 1.14);
		blockMasses.put(Block.stone.blockID, 2400.0);
		blockMasses.put(Block.grass.blockID, 50.0);
		blockMasses.put(Block.dirt.blockID, 1000.0);
		blockMasses.put(Block.cobblestone.blockID, 2400.0);
		blockMasses.put(Block.planks.blockID, 200.0);
		blockMasses.put(Block.sapling.blockID, 100.0);
		blockMasses.put(Block.bedrock.blockID, 2400.0);
		blockMasses.put(Block.waterMoving.blockID, 1000.0);
		blockMasses.put(Block.waterStill.blockID, 1000.0);
		blockMasses.put(Block.lavaMoving.blockID, 2400.0);
		blockMasses.put(Block.lavaStill.blockID, 2400.0);
		blockMasses.put(Block.sand.blockID, 1200.0);
		blockMasses.put(Block.gravel.blockID, 1200.0);
		blockMasses.put(Block.oreGold.blockID, 4000.0);
		blockMasses.put(Block.oreIron.blockID, 3000.0);
		blockMasses.put(Block.oreCoal.blockID, 1500.0);
		blockMasses.put(Block.wood.blockID, 800.0);
		blockMasses.put(Block.leaves.blockID, 50.0);
		blockMasses.put(Block.sponge.blockID, 20.0);
		blockMasses.put(Block.glass.blockID, 300.0);
		blockMasses.put(Block.oreLapis.blockID, 2500.0);
		blockMasses.put(Block.blockLapis.blockID, 2700.0);
		blockMasses.put(Block.dispenser.blockID, 1200.0);
		blockMasses.put(Block.sandStone.blockID, 1200.0);
		blockMasses.put(Block.music.blockID, 1000.0);
		blockMasses.put(Block.bed.blockID, 500.0);
		blockMasses.put(Block.railPowered.blockID, 1000.0);
		blockMasses.put(Block.railDetector.blockID, 500.0);
		blockMasses.put(Block.pistonStickyBase.blockID, 1000.0);
		blockMasses.put(Block.web.blockID, 10.0);
		blockMasses.put(Block.tallGrass.blockID, 20.0);
		blockMasses.put(Block.deadBush.blockID, 20.0);
		blockMasses.put(Block.pistonBase.blockID, 2000.0);
		blockMasses.put(Block.pistonExtension.blockID, 2000.0);
		blockMasses.put(Block.cloth.blockID, 1300.0);
		blockMasses.put(Block.pistonMoving.blockID, 1000.0);
		blockMasses.put(Block.plantYellow.blockID, 5.0);
		blockMasses.put(Block.plantRed.blockID, 5.0);
		blockMasses.put(Block.mushroomBrown.blockID, 5.0);
		blockMasses.put(Block.mushroomRed.blockID, 5.0);
		blockMasses.put(Block.blockGold.blockID, 19300.0);
		blockMasses.put(Block.blockSteel.blockID, 6980.0);
		blockMasses.put(Block.stairDouble.blockID, 600.0);
		blockMasses.put(Block.stairSingle.blockID, 600.0);
		blockMasses.put(Block.brick.blockID, 2000.0);
		blockMasses.put(Block.tnt.blockID, 1000.0);
		blockMasses.put(Block.bookShelf.blockID, 500.0);
		blockMasses.put(Block.cobblestoneMossy.blockID, 2400.0);
		blockMasses.put(Block.obsidian.blockID, 2700.0);
		blockMasses.put(Block.torchWood.blockID, 7.5);
		blockMasses.put(Block.fire.blockID, 0.0);
		blockMasses.put(Block.mobSpawner.blockID, 2000.0);
		blockMasses.put(Block.stairCompactPlanks.blockID, 100.0);
		blockMasses.put(Block.chest.blockID, 1000.0);
		blockMasses.put(Block.redstoneWire.blockID, 10.0);
		blockMasses.put(Block.oreDiamond.blockID, 2500.0);
		blockMasses.put(Block.blockDiamond.blockID, 3500.0);
		blockMasses.put(Block.workbench.blockID, 500.0);
		blockMasses.put(Block.crops.blockID, 50.0);
		blockMasses.put(Block.tilledField.blockID, 1000.0);
		blockMasses.put(Block.stoneOvenIdle.blockID, 1000.0);
		blockMasses.put(Block.stoneOvenActive.blockID, 1000.0);
		blockMasses.put(Block.signPost.blockID, 20.0);
		blockMasses.put(Block.doorWood.blockID, 50.0);
		blockMasses.put(Block.ladder.blockID, 20.0);
		blockMasses.put(Block.rail.blockID, 250.0);
		blockMasses.put(Block.stairCompactCobblestone.blockID, 600.0);
		blockMasses.put(Block.signWall.blockID, 15.0);
		blockMasses.put(Block.lever.blockID, 7.5);
		blockMasses.put(Block.pressurePlateStone.blockID, 200.0);
		blockMasses.put(Block.doorSteel.blockID, 300.0);
		blockMasses.put(Block.pressurePlatePlanks.blockID, 50.0);
		blockMasses.put(Block.oreRedstone.blockID, 1000.0);
		blockMasses.put(Block.oreRedstoneGlowing.blockID, 1000.0);
		blockMasses.put(Block.torchRedstoneIdle.blockID, 7.5);
		blockMasses.put(Block.torchRedstoneActive.blockID, 7.5);
		blockMasses.put(Block.button.blockID, 7.5);
		blockMasses.put(Block.snow.blockID, 11.0);
		blockMasses.put(Block.ice.blockID, 910.0);
		blockMasses.put(Block.blockSnow.blockID, 100.0);
		blockMasses.put(Block.cactus.blockID, 40.0);
		blockMasses.put(Block.blockClay.blockID, 1000.0);
		blockMasses.put(Block.reed.blockID, 20.0);
		blockMasses.put(Block.jukebox.blockID, 100.0);
		blockMasses.put(Block.fence.blockID, 50.0);
		blockMasses.put(Block.pumpkin.blockID, 10.0);
		blockMasses.put(Block.netherrack.blockID, 1000.0);
		blockMasses.put(Block.slowSand.blockID, 1000.0);
		blockMasses.put(Block.glowStone.blockID, 1000.0);
		blockMasses.put(Block.portal.blockID, 0.0);
		blockMasses.put(Block.pumpkinLantern.blockID, 25.0);
		blockMasses.put(Block.cake.blockID, 15.0);
		blockMasses.put(Block.redstoneRepeaterIdle.blockID, 50.0);
		blockMasses.put(Block.redstoneRepeaterActive.blockID, 50.0);
		blockMasses.put(Block.lockedChest.blockID, 1000.0);
		blockMasses.put(Block.trapdoor.blockID, 25.0);
		blockMasses.put(Block.silverfish.blockID, 5.0);
		blockMasses.put(Block.stoneBrick.blockID, 2400.0);
		blockMasses.put(Block.mushroomCapBrown.blockID, 5.0);
		blockMasses.put(Block.mushroomCapRed.blockID, 5.0);
		blockMasses.put(Block.fenceIron.blockID, 50.0);
		blockMasses.put(Block.thinGlass.blockID, 25.0);
		blockMasses.put(Block.melon.blockID, 10.0);
		blockMasses.put(Block.pumpkinStem.blockID, 5.0);
		blockMasses.put(Block.melonStem.blockID, 10.0);
		blockMasses.put(Block.vine.blockID, 10.0);
		blockMasses.put(Block.fenceGate.blockID, 30.0);
		blockMasses.put(Block.stairsBrick.blockID, 600.0);
		blockMasses.put(Block.stairsStoneBrickSmooth.blockID, 600.0);
		blockMasses.put(Block.mycelium.blockID, 1000.0);
		blockMasses.put(Block.waterlily.blockID, 10.0);
		blockMasses.put(Block.netherBrick.blockID, 2000.0);
		blockMasses.put(Block.netherFence.blockID, 100.0);
		blockMasses.put(Block.stairsNetherBrick.blockID, 400.0);
		blockMasses.put(Block.netherStalk.blockID, 50.0);
		blockMasses.put(Block.enchantmentTable.blockID, 70.0);
		blockMasses.put(Block.brewingStand.blockID, 70.0);
		blockMasses.put(Block.cauldron.blockID, 100.0);
		blockMasses.put(Block.endPortal.blockID, 0.0);
		blockMasses.put(Block.endPortalFrame.blockID, 9000.0);
		blockMasses.put(Block.whiteStone.blockID, 1000.0);
		blockMasses.put(Block.dragonEgg.blockID, 30.0);
		blockMasses.put(Block.redstoneLampIdle.blockID, 20.0);
		blockMasses.put(Block.redstoneLampActive.blockID, 20.0);
	}

	public static int count = 0;
    public static boolean first = true;
	public static void tick()
	{
		currentLoopTime = System.currentTimeMillis();
		double seconds = (double)(currentLoopTime - prevLoopTime)/1000.0;
		prevLoopTime = currentLoopTime;
		if(seconds > 0.015)
		{
			seconds = 0.015;
		}
        //PhysManagerFluids.tick(seconds);
		PhysManagerObjects.tickPhysicsEntities();
    	if(first)
    	{
    		//EntityCloth firstCloth = new EntityCloth(this, 0, 110, 0);
    		//spawnEntityInWorld(firstCloth);
    		first = false;
    	}
    	/*for(int i = 0; i < emptySubs.size(); i++)
    	{
    		int sIndex = emptySubs.get(i);
    		subWorlds.remove(getSubArrayIndex(sIndex));
    		saveHandler.deleteSubWorld(sIndex);
    	}
    	emptySubs.clear();*/
    	/*count++;
    	if(count < 2)
    	{
        	World world = PhysManagerMC.mc().theWorld;
        	EffectRenderer effectRenderer = PhysManagerMC.mc().effectRenderer;
        	//effectRenderer.addEffect(new PhysFluidParticle(world, 4.0*Math.random() - 2.0, 70 + 4.0*Math.random() - 2, 4.0*Math.random() - 2));
    		int l = 8;
    		for(int i = 1; i < l - 1; i++)
    		{
    			for(int j = 1; j < l - 1; j++)
        		{
    				for(int k = 1; k < l - 1; k++)
    	    		{
    					cell.addParticle(new PhysFluidParticle(world, 4.0*(double)i/(double)l, 70 + 16.0*(double)j/(double)l, 4.0*(double)k/(double)l));
    	    		}
        		}
    		}
    	}*/
        ArrayList<PhysWorld> sWorlds = PhysManagerShips.subWorlds;
        for(int i = 0; i < sWorlds.size(); i++)
        {
        	PhysWorld sWorld = sWorlds.get(i);
        	sWorld.subTick();
     		sWorld.updateEntities();
        	sWorld.effectRenderer.updateEffects();
        	sWorld.entityRenderer.updateRenderer();
        }
	}
	
	public static void handleCollision(PhysVector pos, PhysVector move)
	{
		PhysVector nP = pos.getFloor();
		
		PhysVector newPos = pos.plus(move);
		PhysVector fNP = newPos.getFloor();
		
		if(fNP.X != nP.X)
		{
			int idNew = PhysManagerMC.mc().theWorld.getBlockId((int)fNP.X, (int)nP.Y, (int)nP.Z);
			if(idNew != 0 && idNew != 8 && idNew!= 9)
			{
				move.X = 0;
			}
		}
		if(fNP.Y != nP.Y)
		{
			int idNew = PhysManagerMC.mc().theWorld.getBlockId((int)nP.X, (int)fNP.Y, (int)nP.Z);
			if(idNew != 0 && idNew != 8 && idNew!= 9)
			{
				move.Y = 0;
			}
		}
		if(fNP.Z != nP.Z)
		{
			int idNew = PhysManagerMC.mc().theWorld.getBlockId((int)nP.X, (int)nP.Y, (int)fNP.Z);
			if(idNew != 0 && idNew != 8 && idNew!= 9)
			{
				move.Z = 0;
			}
		}
	}
	
	public static void handleCollision(PhysVector pos, PhysVector momentum, PhysVector impulse, double mass)
	{
		PhysVector move = momentum.times(timeStepSeconds/mass);
		PhysVector nP = pos.getFloor();
		
		PhysVector newPos = pos.plus(move);
		PhysVector fNP = newPos.getFloor();
		
		if(fNP.X != nP.X)
		{
			int idNew = PhysManagerMC.mc().theWorld.getBlockId((int)fNP.X, (int)fNP.Y, (int)fNP.Z);
			if(idNew != 0 && idNew != 8 && idNew!= 9)
			{
				momentum.X = 0;
				impulse.X = 0;
			}
		}
		if(fNP.Y != nP.Y)
		{
			int idNew = PhysManagerMC.mc().theWorld.getBlockId((int)fNP.X, (int)fNP.Y, (int)fNP.Z);
			if(idNew != 0 && idNew != 8 && idNew!= 9)
			{
				momentum.Y = 0;
				impulse.Y = 0;
			}
		}
		if(fNP.Z != nP.Z)
		{
			int idNew = PhysManagerMC.mc().theWorld.getBlockId((int)fNP.X, (int)fNP.Y, (int)fNP.Z);
			if(idNew != 0 && idNew != 8 && idNew!= 9)
			{
				momentum.Z = 0;
				impulse.Z = 0;
			}
		}
	}
	
	public static void handleCollision(PhysVector pos, PhysVector move, PhysVector velocity)
	{
		PhysVector nP = pos.getFloor();
		
		PhysVector newPos = pos.plus(move);
		PhysVector fNP = newPos.getFloor();
		
		if(fNP.X != nP.X)
		{
			int idNew = PhysManagerMC.mc().theWorld.getBlockId((int)fNP.X, (int)fNP.Y, (int)fNP.Z);
			if(idNew != 0 && idNew != 8 && idNew!= 9)
			{
				velocity.X = 0;
				move.X = 0;
			}
		}
		if(fNP.Y != nP.Y)
		{
			int idNew = PhysManagerMC.mc().theWorld.getBlockId((int)fNP.X, (int)fNP.Y, (int)fNP.Z);
			if(idNew != 0 && idNew != 8 && idNew!= 9)
			{
				velocity.Y = 0;
				move.Y = 0;
			}
		}
		if(fNP.Z != nP.Z)
		{
			int idNew = PhysManagerMC.mc().theWorld.getBlockId((int)fNP.X, (int)fNP.Y, (int)fNP.Z);
			if(idNew != 0 && idNew != 8 && idNew!= 9)
			{
				velocity.Z = 0;
				move.Z = 0;
			}
		}
	}
	
	public static void handleSolidPressure(PhysFluidParticle particle)
	{
		/*PhysVector nP = particle.pos.getFloor();
		World world = PhysManagerMC.mc().theWorld;
		
		int x = (int)nP.X;
		int y = (int)nP.Y;
		int z = (int)nP.Z;
		
		int idbot = world.getBlockId(x, y - 1, z);
		int idleft = world.getBlockId(x - 1, y, z);
		int idright = world.getBlockId(x + 1, y, z);
		int idforward = world.getBlockId(x, y, z + 1);
		int idback = world.getBlockId(x, y, z - 1);
		
		PhysVector pos = particle.pos;
		
		if(idbot != 0)
		{
			double dist = pos.Y - y;
			if(dist < .5)
			{
				particle.fluidPressure.Y += (.5 - dist)*particle.density*g;
			}
		}
		if(idleft != 0)
		{
			double dist = pos.X - x;
			if(dist < .5)
			{
				particle.fluidPressure.X += (.5 - dist)*particle.density;
			}
		}
		if(idright != 0)
		{
			double dist = x + 1 - pos.X;
			if(dist < .5)
			{
				particle.fluidPressure.X -= (.5 - dist)*particle.density;
			}
		}
		if(idforward != 0)
		{
			double dist = z + 1 - pos.Z;
			
			if(dist < .5)
			{
				particle.fluidPressure.Z -= (.5 - dist)*particle.density;
			}
		}
		if(idback != 0)
		{
			double dist = pos.Z - z;
			if(dist < .5)
			{
				particle.fluidPressure.Z += (.5 - dist)*particle.density;
			}
		}*/
	}
	
	public static void checkWalls(PhysFluidParticle particle, double seconds)
	{
		PhysVector nP = particle.pos.getFloor();
		World world = PhysManagerMC.mc().theWorld;
		
		int x = (int)nP.X;
		int y = (int)nP.Y;
		int z = (int)nP.Z;
		
		int idbot = world.getBlockId(x, y - 1, z);
		int idleft = world.getBlockId(x - 1, y, z);
		int idright = world.getBlockId(x + 1, y, z);
		int idforward = world.getBlockId(x, y, z + 1);
		int idback = world.getBlockId(x, y, z - 1);
		
		PhysVector pos = particle.pos;
		PhysVector velocity = particle.velocity;
		
		if(idbot != 0)
		{
			double dist = pos.Y - y;
			if(dist < .25)
			{
				pos.Y += .25 - dist;
				velocity.Y += (.25 - dist)/seconds;
			}
		}
		if(idleft != 0)
		{
			double dist = pos.X - x;
			if(dist < .25)
			{
				pos.X += .25 - dist;
				velocity.X += (.25 - dist)/seconds;
			}
		}
		if(idright != 0)
		{
			double dist = x + 1 - pos.X;
			if(dist < .25)
			{
				pos.X -= .25 - dist;
				velocity.X -= (.25 - dist)/seconds;
			}
		}
		if(idforward != 0)
		{
			double dist = z + 1 - pos.Z;
			
			if(dist < .25)
			{
				pos.Z -= .25 - dist;
				velocity.Z -= (.25 - dist)/seconds;
			}
		}
		if(idback != 0)
		{
			double dist = pos.Z - z;
			if(dist < .25)
			{
				pos.Z += .25 - dist;
				velocity.X += (.25 - dist)/seconds;
			}
		}
	}
	
	public static boolean isPhysicsEntity(Entity entity)
	{
		return entity instanceof PhysRopeEntity || entity instanceof PhysClothEntity;
	}
	
	public static void tieRopeAt(World world, PhysRopeJoint ropeJoint, PhysVector loc)
	{
		if(world instanceof PhysWorld)
		{
			((PhysWorld)world).tieRopeAt(ropeJoint, loc);
		}
		else
		{
	        ropeJoint.tie = new PhysTie(ropeJoint, loc, -1);
	        ropeJoint.moveTo(loc);
		}
	}
}

