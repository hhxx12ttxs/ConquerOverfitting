/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.TileEntities.Production;

import java.util.List;

import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import Reika.DragonAPI.Instantiable.HybridTank;
import Reika.DragonAPI.Instantiable.ParallelTicker;
import Reika.DragonAPI.Instantiable.StepTimer;
import Reika.DragonAPI.Instantiable.Data.BlockArray;
import Reika.DragonAPI.Libraries.ReikaInventoryHelper;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaEngLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaPhysicsHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaTimeHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaParticleHelper;
import Reika.DragonAPI.Libraries.World.ReikaRedstoneHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.RotaryCraft.RotaryConfig;
import Reika.RotaryCraft.RotaryCraft;
import Reika.RotaryCraft.API.PowerGenerator;
import Reika.RotaryCraft.API.ShaftMerger;
import Reika.RotaryCraft.API.Event.JetEngineEnterFailureEvent;
import Reika.RotaryCraft.API.Event.JetEngineExplosionEvent;
import Reika.RotaryCraft.Auxiliary.ItemStacks;
import Reika.RotaryCraft.Auxiliary.PowerSourceList;
import Reika.RotaryCraft.Auxiliary.Interfaces.PartialInventory;
import Reika.RotaryCraft.Auxiliary.Interfaces.PipeConnector;
import Reika.RotaryCraft.Auxiliary.Interfaces.SimpleProvider;
import Reika.RotaryCraft.Auxiliary.Interfaces.TemperatureTE;
import Reika.RotaryCraft.Auxiliary.Interfaces.UpgradeableMachine;
import Reika.RotaryCraft.Base.EntityTurretShot;
import Reika.RotaryCraft.Base.TileEntity.TileEntityIOMachine;
import Reika.RotaryCraft.Base.TileEntity.TileEntityInventoryIOMachine;
import Reika.RotaryCraft.Base.TileEntity.TileEntityPiping.Flow;
import Reika.RotaryCraft.Items.Tools.ItemFuelLubeBucket;
import Reika.RotaryCraft.Registry.ConfigRegistry;
import Reika.RotaryCraft.Registry.DifficultyEffects;
import Reika.RotaryCraft.Registry.EngineType;
import Reika.RotaryCraft.Registry.ItemRegistry;
import Reika.RotaryCraft.Registry.MachineRegistry;
import Reika.RotaryCraft.Registry.PacketRegistry;
import Reika.RotaryCraft.Registry.RotaryAchievements;
import Reika.RotaryCraft.Registry.SoundRegistry;
import Reika.RotaryCraft.TileEntities.Auxiliary.TileEntityEngineController;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class TileEntityEngine extends TileEntityInventoryIOMachine implements TemperatureTE, SimpleProvider,
PipeConnector, PowerGenerator, IFluidHandler, PartialInventory, UpgradeableMachine {

	/** Water capacity */
	public static final int CAPACITY = 60*RotaryConfig.MILLIBUCKET;
	public int MAXTEMP = 1000;

	/** Fuel capacity */
	public static final int FUELCAP = 240*RotaryConfig.MILLIBUCKET;

	public static final int LUBECAP = 24*RotaryConfig.MILLIBUCKET;

	private HybridTank water = new HybridTank("enginewater", CAPACITY);
	private HybridTank fuel = new HybridTank("enginefuel", FUELCAP);

	public int temperature;

	private HybridTank lubricant = new HybridTank("enginelube", LUBECAP);

	/** For timing control */
	public int soundtick = 2000;

	/** Used in acPower */
	private boolean[] lastPower = new boolean[3];

	/** Used in combustion power */
	public int additives;
	private boolean starvedengine;

	public boolean isJetFailing = false;

	private EngineType type;

	/** Used in jet engines */
	public int FOD = 0;
	private int dumpx;
	private int dumpz;
	private int dumpvx;
	private int dumpvz;

	public int backx;
	public int backz;

	private int chickenCount = 0;

	private boolean isChoking = false;

	private boolean isOn;

	private StepTimer jetstarttimer = new StepTimer(479);
	private long lastpower = 0;

	private ParallelTicker timer = new ParallelTicker().addTicker("fuel").addTicker("sound").addTicker("temperature", ReikaTimeHelper.SECOND.getDuration());

	public TileEntityEngine(EngineType eng) {
		type = eng;
	}

	public TileEntityEngine() {
		this(EngineType.DC);
	}

	public EngineType getEngineType() {
		return type;
	}

	public int getInventoryStackLimit() {
		return type.allowInventoryStacking() ? 64 : 1;
	}

	public int getSizeInventory() {
		return 2;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer ep) {
		if (!type.hasGui())
			return false;
		return super.isUseableByPlayer(ep);
	}

	public int getLiquidScaled(int par1) {
		return (water.getLevel()*par1)/CAPACITY;
	}

	public int getTempScaled(int par1) {
		return (temperature*par1)/MAXTEMP;
	}

	public int getEthanolScaled(int par1) {
		return (fuel.getLevel() * par1) / FUELCAP;
	}

	public int getAdditivesScaled(int par1) {
		return (additives * par1*1000) / FUELCAP;
	}

	public int getJetFuelScaled(int par1) {
		return (fuel.getLevel() * par1) / FUELCAP;
	}

	private void consumeFuel() {
		this.internalizeFuel();
		if (timer.checkCap("fuel")) {
			switch(type) {
			case STEAM:
				if (water.getLevel() > 0 && temperature >= 100)
					water.removeLiquid(this.getConsumedFuel());
				break;
			case GAS:
				if (fuel.getLevel() > 0)
					fuel.removeLiquid(this.getConsumedFuel());
				break;
			case SPORT:
				if (fuel.getLevel() > 0) {
					fuel.removeLiquid(this.getConsumedFuel());
					if (rand.nextInt(30) == 0)
						if (additives > 0)
							additives--;
				}
				break;
			case MICRO:
				if (fuel.getLevel() > 0)
					fuel.removeLiquid(this.getConsumedFuel());
				break;
			case JET:
				if (fuel.getLevel() > 0)
					fuel.removeLiquid(this.getConsumedFuel());
				break;
			default:
				break;
			}

		}
	}

	private int getConsumedFuel() {
		return type == EngineType.JET ? 20 : 10;
	}

	private void internalizeFuel() {
		switch(type) {
		case STEAM:
			if (inv[0] != null) {
				if (inv[0].itemID == Item.bucketWater.itemID && water.getLevel() <= CAPACITY) {
					water.addLiquid(FluidContainerRegistry.BUCKET_VOLUME*inv[0].stackSize, FluidRegistry.WATER);
					inv[0] = new ItemStack(Item.bucketEmpty.itemID, inv[0].stackSize, 0);
				}
			}
			break;
		case GAS:
			if (inv[0] != null && fuel.getLevel()+FluidContainerRegistry.BUCKET_VOLUME <= FUELCAP) {
				if (inv[0].itemID == ItemRegistry.ETHANOL.getShiftedID()) {
					ReikaInventoryHelper.decrStack(0, inv);
					fuel.addLiquid(1000, RotaryCraft.ethanolFluid);
				}
			}
			break;
		case SPORT:
			if (inv[0] != null && fuel.getLevel()+FluidContainerRegistry.BUCKET_VOLUME < FUELCAP) {
				if (inv[0].itemID == ItemRegistry.ETHANOL.getShiftedID()) {
					ReikaInventoryHelper.decrStack(0, inv);
					fuel.addLiquid(1000, RotaryCraft.ethanolFluid);
				}
			}
			if (inv [1] != null && additives < FUELCAP/FluidContainerRegistry.BUCKET_VOLUME) { //additives
				int id = inv[1].itemID;
				if (id == Item.blazePowder.itemID || id == Item.redstone.itemID || id == Item.gunpowder.itemID) {
					ReikaInventoryHelper.decrStack(1, inv);
					if (id == Item.redstone.itemID)
						additives += 1;
					if (id == Item.gunpowder.itemID)
						additives += 2;
					if (id == Item.blazePowder.itemID)
						additives += 4;
				}
			}
			break;
		case MICRO:
		case JET:
			break;
		default:
			break;
		}
	}

	private boolean getRequirements(World world, int x, int y, int z, int meta) {
		if (type.isAirBreathing() && !this.hasAir(world, x, y, z))
			return false;
		switch (type) {
		case DC:
			return world.isBlockIndirectlyGettingPowered(x, y, z);
		case WIND:
			return this.windCheck(world, x, y, z, meta);
		case STEAM:
			return this.steamCheck(world, x, y, z, meta);
		case GAS:
			return this.combustionCheck(world, x, y, z, meta);
		case AC:
			return this.acPower(world, x, y, z);
		case SPORT:
			return this.combustionCheck(world, x, y, z, meta);
		case HYDRO:
			return this.hydroCheck(world, x, y, z, meta);
		case MICRO:
		case JET:
			return this.jetCheck(world, x, y, z);
		}
		return false;
	}

	private boolean hasAir(World world, int x, int y, int z) {
		if (this.isDrowned(world, x, y, z))
			return false;
		if (world.provider instanceof IGalacticraftWorldProvider) {
			IGalacticraftWorldProvider ig = (IGalacticraftWorldProvider)world.provider;
			if (ig.getSoundVolReductionAmount() > 1)
				return false;
		}
		return true;
	}

	private boolean hydroCheck(World world, int x, int y, int z, int meta) {
		if (this.doesBlockObstructBlades(world, x, y+1, z))
			return false;
		if (this.doesBlockObstructBlades(world, x, y-1, z))
			return false;

		int[] pos = this.getWaterColumnPos();
		int id = world.getBlockId(pos[0], y, pos[1]);
		if (id == Block.lavaMoving.blockID || id == Block.lavaStill.blockID) {
			if (ReikaRandomHelper.doWithChance(2)) {
				world.createExplosion(null, x+0.5, y+0.5, z+0.5, 2, true);
				world.setBlock(x, y, z, 0);
			}
		}
		if (id != Block.waterStill.blockID && id != Block.waterMoving.blockID)
			return false;
		if (!ReikaWorldHelper.isLiquidAColumn(world, pos[0], y, pos[1]))
			return false;

		for (int i = -1; i <= 1; i++)
			if (this.doesBlockObstructBlades(world, 2*x-pos[0], y+i, 2*z-pos[1]))
				return false;

		return true;
	}

	private boolean doesBlockObstructBlades(World world, int x, int y, int z) {
		return !ReikaWorldHelper.softBlocks(world, x, y, z);
	}

	private int[] getWaterColumnPos() {
		int[] pos = {xCoord, zCoord};
		switch(this.getBlockMetadata()) {
		case 0:
			pos[1] += -1;
			break;
		case 1:
			pos[1] += 1;
			break;
		case 2:
			pos[0] += 1;
			break;
		case 3:
			pos[0] += -1;
			break;
		}
		return pos;
	}

	private float getHydroFactor(World world, int x, int y, int z, boolean isTorque) {
		double grav = ReikaPhysicsHelper.g;
		if (world.provider instanceof IGalacticraftWorldProvider) {
			IGalacticraftWorldProvider ig = (IGalacticraftWorldProvider)world.provider;
			grav += ig.getGravity()*10;
		}
		int[] pos = this.getWaterColumnPos();
		double dy = (ReikaWorldHelper.findWaterSurface(world, pos[0], y, pos[1])-y)-0.5;
		double v = Math.sqrt(2*grav*dy);
		double mdot = ReikaEngLibrary.rhowater*v;
		double P = 0.25*mdot*dy;
		if (P >= type.getPower())
			return 1;
		return (float)(P/type.getPower());
	}

	private void dealPanelDamage(World world, int x, int y, int z, int meta) {
		int a = 0; int b = 0;
		if (meta < 2)
			b = 1;
		else
			a = 1;
		AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x, y, z, x+1, y+1, z+1).expand(a, 1, b);
		List in = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
		for (int i = 0; i < in.size(); i++) {
			EntityLivingBase ent = (EntityLivingBase)in.get(i);
			ent.attackEntityFrom(RotaryCraft.hydrokinetic, 1);
		}
	}

	private void dealBladeDamage(World world, int x, int y, int z, int meta) {
		int c = 0; int d = 0;
		int a = 0; int b = 0;
		if (meta < 2)
			b = 1;
		else
			a = 1;
		switch (meta) {
		case 0:
			c = 1;
			break;
		case 1:
			c = -1;
			break;
		case 2:
			d = 1;
			break;
		case 3:
			d = -1;
			break;
		}
		AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x+c, y, z+d, x+1+c, y+1, z+1+d).expand(a, 1, b);
		List in = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
		for (int i = 0; i < in.size(); i++) {
			EntityLivingBase ent = (EntityLivingBase)in.get(i);
			ent.attackEntityFrom(DamageSource.generic, 1);
		}
	}

	private boolean windCheck(World world, int x, int y, int z, int meta) {
		int c = 0; int d = 0;
		int a = 0; int b = 0;
		if (meta < 2)
			b = 1;
		else
			a = 1;
		switch (meta) {
		case 0:
			c = 1;
			break;
		case 1:
			c = -1;
			break;
		case 2:
			d = 1;
			break;
		case 3:
			d = -1;
			break;
		}
		for (int i = 1; i < 16; i++) {
			int id = world.getBlockId(x+c*i, y, z+d*i);
			if (id != 0) {
				if (Block.blocksList[id].getCollisionBoundingBoxFromPool(world, x+c*i, y, z+d*i) != null)
					return false;
			}
		}
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				int id = world.getBlockId(x+a*i+c, y+j, z+b*i+d);
				if (!ReikaWorldHelper.softBlocks(id))
					return false;
			}
		}
		return true;
	}

	private float getWindFactor(World world, int x, int y, int z, int meta) {
		if (world.provider.terrainType == WorldType.FLAT) {
			if (y < 4)
				return 0;
			float f = (y-4)/16F;
			if (f > 1)
				f = 1;
			return f;
		}
		else {
			if (y < 62)
				return 0;
			float f = (y-62)/62F;
			if (f > 1)
				f = 1;
			return f;
		}
	}

	public boolean isDrowned(World world, int x, int y, int z) {
		boolean flag = false;
		for (int i = 0; i < 6; i++) {
			ForgeDirection dir = dirs[i];
			int dx = x+dir.offsetX;
			int dy = y+dir.offsetY;
			int dz = z+dir.offsetZ;
			int id = world.getBlockId(dx, dy, dz);
			boolean fluid = Block.blocksList[id] instanceof BlockFluid;
			flag = flag || fluid;
			if (id == 0)
				return false;
			if (!fluid)
				if (ReikaWorldHelper.softBlocks(world, dx, dy, dz))
					return false;
		}
		return flag && true;
	}

	private boolean combustionCheck(World world, int x, int y, int z, int meta) {
		if (timer.checkCap("temperature")) {
			this.updateTemperature(world, x, y, z, meta);
		}
		if (fuel.isEmpty())
			return false;
		if (type.usesAdditives()) {
			if (additives <= 0)
				starvedengine = true;
			else
				starvedengine = false;
		}
		return true;
	}

	private boolean steamCheck(World world, int x, int y, int z, int meta) {
		if (timer.checkCap("temperature")) {
			this.updateTemperature(world, x, y, z, meta);
		}

		if (temperature < 100) //water boiling point
			return false;
		if (water.isEmpty())
			return false;

		RotaryAchievements.STEAMENGINE.triggerAchievement(this.getPlacer());
		return true;
	}

	public void updateTemperature(World world, int x, int y, int z, int meta) {
		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
		int Tamb = ReikaWorldHelper.getAmbientTemperatureAt(world, x, y, z);
		//ReikaChatHelper.writeInt(temperature);
		if (temperature > Tamb && omega == 0 && torque == 0 && type == EngineType.SPORT) { //If off and hot
			if (temperature > Tamb+300)
				temperature -= (temperature-Tamb)/100;
			else if (temperature > Tamb+100)
				temperature -= (temperature-Tamb)/50;
			else if (temperature > Tamb+40)
				temperature -= (temperature-Tamb)/10;
			else if (temperature > Tamb+4)
				temperature -= (temperature-Tamb)/2;
			else
				temperature = Tamb;
		}

		switch(type) {
		case STEAM:
			if (biome == BiomeGenBase.hell)
				Tamb = 101;	//boils water, so 101C
			if (world.getBlockId(x, y-1, z) == Block.fire.blockID)
				temperature++;
			if (world.getBlockId(x, y-1, z) == Block.fire.blockID && biome == BiomeGenBase.hell)
				temperature++; //Nether has 50% hotter fire
			if (world.getBlockMaterial(x, y-1, z) == Material.lava)
				temperature += 2;
			if (Tamb < 0 && world.getBlockId(x, y-1, z) == Block.fire.blockID)
				Tamb += 30;
			if (temperature < Tamb)
				temperature += ReikaMathLibrary.extrema((Tamb-temperature)/40, 1, "max");
			if (world.getBlockId(x, y-1, z) != Block.fire.blockID && world.getBlockMaterial(x, y-1, z) != Material.lava && temperature > Tamb)
				temperature--;
			if (temperature > Tamb) {
				temperature -= (temperature-Tamb)/96;
			}
			if (temperature > MAXTEMP)
				this.overheat(world, x, y, z);
			break;
		case SPORT:
			if (temperature < Tamb)
				temperature += ReikaMathLibrary.extrema((Tamb-temperature)/40, 1, "max");
			if (omega > 0 && torque > 0) { //If engine is on
				temperature += 1;
				if (water.getLevel() > 0 && temperature > Tamb) {
					water.removeLiquid(20);
					temperature--;
				}
				if (temperature > MAXTEMP/2) {
					if (rand.nextInt(10) == 0) {
						world.spawnParticle("smoke", x+0.5, y+0.5, z+0.5, 0, 0, 0);
						world.playSoundEffect(x+0.5, y+0.5, z+0.5, "random.fizz", 1F, 1F);
					}
				}
				if (temperature > MAXTEMP/1.25) {
					if (rand.nextInt(3) == 0) {
						world.playSoundEffect(x+0.5, y+0.5, z+0.5, "random.fizz", 1F, 1F);
					}
					world.spawnParticle("smoke", x+0.0, y+1.0625, z+0.5, 0, 0, 0);
					world.spawnParticle("smoke", x+0.5, y+1.0625, z+0.5, 0, 0, 0);
					world.spawnParticle("smoke", x+1, y+1.0625, z+0.5, 0, 0, 0);
					world.spawnParticle("smoke", x+0.0, y+1.0625, z+0, 0, 0, 0);
					world.spawnParticle("smoke", x+0.0, y+1.0625, z+1, 0, 0, 0);
					world.spawnParticle("smoke", x+1, y+1.0625, z+0, 0, 0, 0);
					world.spawnParticle("smoke", x+1, y+1.0625, z+1, 0, 0, 0);
				}
			}
			if (temperature > MAXTEMP) {
				this.overheat(world, x, y, z);
			}
			break;
		default:
			break;
		}
	}

	public void overheat(World world, int x, int y, int z) {
		temperature = MAXTEMP;
		if (type == EngineType.SPORT) {
			ReikaWorldHelper.overheat(world, x, y, z, ItemStacks.scrap.itemID, ItemStacks.scrap.getItemDamage(), 0, 27, true, 1.5F, true, ConfigRegistry.BLOCKDAMAGE.getState(), 6F);
		}
		else if (type == EngineType.STEAM) {
			ReikaWorldHelper.overheat(world, x, y, z, ItemStacks.scrap.itemID, ItemStacks.scrap.getItemDamage(), 0, 17, false, 1F, false, true, 2F);
			RotaryAchievements.OVERPRESSURE.triggerAchievement(this.getPlacer());
		}
		world.setBlockToAir(x, y, z);
	}

	private boolean acPower (World world, int x, int y, int z) {
		ItemStack is = inv[0];
		if (is == null)
			return false;
		if (is.itemID != ItemStacks.shaftcore.itemID || is.getItemDamage() != ItemStacks.shaftcore.getItemDamage())
			return false;
		if (is.stackTagCompound == null)
			return false;
		if (!is.stackTagCompound.hasKey("magnet"))
			return false;
		if (is.stackTagCompound.getInteger("magnet") <= 0)
			return false;

		boolean ac = ReikaRedstoneHelper.isGettingACRedstone(world, x, y, z, lastPower);

		if (!world.isRemote && ac && timer.checkCap("fuel")) {
			int m = is.stackTagCompound.getInteger("magnet");
			m--;
			is.stackTagCompound.setInteger("magnet", m);
		}

		return (ac);
	}

	private boolean jetCheck(World world, int x, int y, int z) {
		if (type == EngineType.JET) {
			if (FOD >= 8) {
				jetstarttimer.reset();
				return false;
			}
		}
		if (fuel.getLevel() <= 0) {
			jetstarttimer.reset();
			return false;
		}
		if (type == EngineType.JET)
			RotaryAchievements.JETENGINE.triggerAchievement(this.getPlacer());
		return true;
	}

	public float getChokedFraction(World world, int x, int y, int z, int meta) {
		int[] pos = {x, z};
		switch(meta) {
		case 0:
			pos[0] += 1;
			break;
		case 1:
			pos[0] += -1;
			break;
		case 2:
			pos[1] += 1;
			break;
		case 3:
			pos[1] += -1;
			break;
		}
		int id = world.getBlockId(pos[0], y, pos[1]);
		int dmg = world.getBlockMetadata(pos[0], y, pos[1]);
		if (id == 0)
			return 1;
		if (Block.blocksList[id].getCollisionBoundingBoxFromPool(world, pos[0], y, pos[1]) == null)
			return 1;
		if (id == Block.fence.blockID || id == Block.netherFence.blockID)
			return 0.75F;
		if (id == Block.fenceIron.blockID)
			return 1F;
		if (id == Block.cobblestoneWall.blockID)
			return 0.25F;
		if (id == Block.thinGlass.blockID)
			return 0.5F;
		Block b = Block.blocksList[id];
		if (b.getBlockBoundsMaxX() > 0.875 && b.getBlockBoundsMaxY() > 0.875 && b.getBlockBoundsMaxZ() > 0.875)
			if (b.getBlockBoundsMinX() < 0.125 && b.getBlockBoundsMinY() < 0.125 && b.getBlockBoundsMinZ() < 0.125)
				return 0;
		double frac;
		double dx = b.getBlockBoundsMaxX()-b.getBlockBoundsMinX();
		double dy = b.getBlockBoundsMaxY()-b.getBlockBoundsMinY();
		double dz = b.getBlockBoundsMaxZ()-b.getBlockBoundsMinZ();
		if (b.getBlockBoundsMaxX() <= 0.125 || b.getBlockBoundsMinX() >= 0.875)
			dx = 0;
		if (b.getBlockBoundsMaxY() <= 0.125 || b.getBlockBoundsMinY() >= 0.875)
			dy = 0;
		if (b.getBlockBoundsMaxZ() <= 0.125 || b.getBlockBoundsMinZ() >= 0.875)
			dz = 0;
		if (b.getBlockBoundsMaxY() >= 0.75)
			dy += 0.125;
		//ReikaJavaLibrary.pConsole(dx+"  "+dy+"  "+dz);
		frac = 1-(dx*dy*dz);
		return (float)frac;
	}

	private void getEngineType(World world, int x, int y, int z, int meta) {
		switch (type) {
		case STEAM:
			MAXTEMP = 150;
			break;
		case SPORT:
			MAXTEMP = 240;
			break;
		default:
			break;
		}

		timer.setCap("fuel", type.getFuelUnitDuration());
		timer.setCap("sound", type.getSoundLength(FOD, 1));

		boolean on = true;
		if (type.isECUControllable()) {
			MachineRegistry m = MachineRegistry.getMachine(world, x, y-1, z);
			if (m == MachineRegistry.ECU) {
				TileEntityEngineController ecu = (TileEntityEngineController)world.getBlockTileEntity(x, y-1, z);
				on = ecu.canProducePower();
			}
		}
		if (on && this.getRequirements(world, x, y, z, meta)) {
			isOn = true;
			int speed;
			switch (type) {
			case DC:
				//omega = EnumEngineType.DC.getSpeed();
				this.updateSpeed(EngineType.DC.getSpeed(), true);
				torque = EngineType.DC.getTorque();
				break;
			case WIND:
				speed = (int)(EngineType.WIND.getSpeed()*this.getWindFactor(world, x, y, z, meta));
				this.updateSpeed(speed, true);
				torque = EngineType.WIND.getTorque();
				if (omega == 0) {
					isOn = false;
					torque = 0;
				}
				if (omega > 0)
					this.dealBladeDamage(world, x, y, z, meta);
				break;
			case STEAM:
				this.updateSpeed(EngineType.STEAM.getSpeed(), true);
				torque = EngineType.STEAM.getTorque();
				break;
			case GAS:
				this.updateSpeed(EngineType.GAS.getSpeed(), true);
				torque = EngineType.GAS.getTorque();
				break;
			case AC:
				this.updateSpeed(EngineType.AC.getSpeed(), true);
				torque = EngineType.AC.getTorque();
				break;
			case SPORT:
				if (!starvedengine) {
					this.updateSpeed(EngineType.SPORT.getSpeed(), true);
					torque = EngineType.SPORT.getTorque();
				}
				else {
					this.updateSpeed(EngineType.GAS.getSpeed(), true);
					torque = EngineType.GAS.getTorque();
				}
				break;
			case HYDRO:
				speed = (int)(EngineType.HYDRO.getSpeed()*this.getHydroFactor(world, x, y, z, true));
				if (speed == 0)
					speed = 1;
				boolean hasLube = !lubricant.isEmpty() && lubricant.getActualFluid().equals(FluidRegistry.getFluid("lubricant"));
				//ReikaJavaLibrary.pConsole(lubricant, Side.SERVER);
				this.updateSpeed(speed, hasLube);
				torque = (int)(EngineType.HYDRO.getTorque()*this.getHydroFactor(world, x, y, z, false)*this.getArrayTorqueMultiplier());
				if (omega == 0) {
					isOn = false;
					torque = 0;
				}
				if (omega > 0)
					this.dealPanelDamage(world, x, y, z, meta);
				break;
			case MICRO:
				this.updateSpeed(EngineType.MICRO.getSpeed(), true);
				torque = EngineType.MICRO.getTorque();
				break;
			case JET:
				this.checkJetFailure(world, x, y, z, meta);
				speed = (int)(EngineType.JET.getSpeed()*this.getChokedFraction(world, x, y, z, meta));
				this.updateSpeed(speed, true);
				torque = EngineType.JET.getTorque()/(int)ReikaMathLibrary.intpow(2, FOD);
				if (omega == 0) {
					isOn = false;
					torque = 0;
				}
				if (MachineRegistry.getMachine(world, x, y-1, z) == MachineRegistry.ECU) {
					TileEntityEngineController te = (TileEntityEngineController)world.getBlockTileEntity(x, y-1, z);
					if (te != null) {
						if (!te.canProducePower()) {
							return;
						}
					}
				}
				if (omega > 0) {
					this.ingest(world, x, y, z, meta);
					//ReikaJavaLibrary.pConsole(lastpower+":"+power, Side.SERVER);
					if (lastpower == 0) {
						SoundRegistry.JETSTART.playSoundAtBlock(world, x, y, z);
					}
				}
				break;
			}
		}
		else {
			isOn = false;
			this.updateSpeed(0, false);
			//omega = 0;
			if (omega == 0)
				torque = 0;
			if (soundtick == 0 && omega == 0)
				soundtick = 2000;
			//timer.resetTicker("fuel");
		}
	}

	private void updateSpeed(int maxspeed, boolean revup) {
		if (MachineRegistry.getMachine(worldObj, xCoord, yCoord-1, zCoord) == MachineRegistry.ECU) {
			TileEntityEngineController te = (TileEntityEngineController)worldObj.getBlockTileEntity(xCoord, yCoord-1, zCoord);
			if (te != null) {
				maxspeed *= te.getSpeedMultiplier();
			}
			if (omega > maxspeed)
				revup = false;
		}
		if (revup) {
			if (omega < maxspeed) {
				//ReikaJavaLibrary.pConsole(omega+"->"+(omega+2*(int)(ReikaMathLibrary.logbase(maxspeed, 2))), Side.SERVER);
				omega += 4*(int)ReikaMathLibrary.logbase(maxspeed+1, 2);
				timer.setCap("fuel", Math.max(type.getFuelUnitDuration()/4, 1)); //4x fuel burn while spinning up
				if (omega > maxspeed)
					omega = maxspeed;
			}
		}
		else {
			if (omega > 0) {
				//ReikaJavaLibrary.pConsole(omega+"->"+(omega-omega/128-1), Side.SERVER);
				omega -= omega/256+1;
				//soundtick = 2000;
			}
		}
	}

	private void checkJetFailure(World world, int x, int y, int z, int meta) {
		if (isJetFailing)
			this.jetEngineDetonation(world, x, y, z, meta);
		else if (FOD > 0 && rand.nextInt(DifficultyEffects.JETFAILURE.getInt()*(9-FOD)) == 0) {
			RotaryCraft.logger.warn("WARNING: "+this+" just entered failure mode!");
			isJetFailing = true;
			RotaryAchievements.JETFAIL.triggerAchievement(this.getPlacer());
			MinecraftForge.EVENT_BUS.post(new JetEngineEnterFailureEvent(this));
		}
	}

	/** Like BC obsidian pipe - suck in entities in a "funnel" in front of the engine, and deal damage (50 hearts).
	 * Items (including players' inventories and mob drops) will be spat out the back.
	 * Large mobs (Player, creeper, spider, ghast, etc) will cause foreign object damage, necessitating repair.
	 */
	private void ingest(World world, int x, int y, int z, int meta) {
		if (FOD >= 8)
			return;
		for (int step = 0; step < 8; step++) {
			AxisAlignedBB zone = this.getSuctionZone(world, x, y, z, meta, step);
			List inzone = world.getEntitiesWithinAABB(Entity.class, zone);
			for (int i = 0; i < inzone.size(); i++) {
				boolean immune = false;
				float mult = 1;
				if (inzone.get(i) instanceof EntityPlayer) {
					EntityPlayer caughtpl = (EntityPlayer)inzone.get(i);
					if (caughtpl.capabilities.isCreativeMode)
						immune = true;
					ItemStack is = caughtpl.getCurrentArmor(0);
					if (is != null) {
						if (is.itemID == ItemRegistry.BEDBOOTS.getShiftedID())
							mult = 0.1F;
						if (is.itemID == ItemRegistry.BEDJUMP.getShiftedID())
							mult = 0.1F;
					}
				}
				if (inzone.get(i) instanceof EntityTurretShot)
					immune = true;
				Entity caught = (Entity)inzone.get(i);
				if (!immune) {
					caught.motionX += (x+0.5D - caught.posX)/20*mult;
					caught.motionY += (y+0.5D - caught.posY)/20*mult;
					caught.motionZ += (z+0.5D - caught.posZ)/20*mult;
					if (!world.isRemote)
						caught.velocityChanged = true;
				}
				if (ReikaMathLibrary.py3d(caught.posX-(x+0.5), caught.posY-(y+0.5), caught.posZ-(z+0.5)) < 1.2) { // Kill the adjacent entities, except items, which are teleported
					if (caught instanceof EntityItem) {
						if (!caught.isDead) {
							ItemStack is = ((EntityItem) caught).getEntityItem();
							caught.setDead();
							int trycount = 0;
							while (trycount < 1 && !ReikaWorldHelper.nonSolidBlocks(world.getBlockId(dumpx, y, dumpz))) {
								if (dumpvx == 1)
									dumpx++;
								if (dumpvx == -1)
									dumpx--;
								if (dumpvz == 1)
									dumpz++;
								if (dumpvz == -1)
									dumpz--;
								trycount++;
							}
							EntityItem item = new EntityItem(world, dumpx+0.5D, y+0.375D, dumpz+0.5D, is);
							if (!world.isRemote)
								world.spawnEntityInWorld(item);
							item.motionX = dumpvx*1.5D;
							item.motionY = 0.15;
							item.motionZ = dumpvz*1.5D;
							if (!world.isRemote)
								caught.velocityChanged = true;
							if (is.itemID == ItemRegistry.SCREWDRIVER.getShiftedID()) {
								caught.setDead();
								FOD = 2;
								isJetFailing = true;
							}
						}
					}
					else if (caught instanceof EntityXPOrb) {
						if (!caught.isDead) {
							int xp = ((EntityXPOrb)caught).getXpValue();
							caught.setDead();
							int trycount = 0;
							while (trycount < 1 && !ReikaWorldHelper.nonSolidBlocks(world.getBlockId(dumpx, y, dumpz))) {
								if (dumpvx == 1)
									dumpx++;
								if (dumpvx == -1)
									dumpx--;
								if (dumpvz == 1)
									dumpz++;
								if (dumpvz == -1)
									dumpz--;
								trycount++;
							}
							EntityXPOrb item = new EntityXPOrb(world, dumpx+0.5D, y+0.375D, dumpz+0.5D, xp);
							if (!world.isRemote)
								world.spawnEntityInWorld(item);
							item.motionX = dumpvx*1.5D;
							item.motionY = 0.15;
							item.motionZ = dumpvz*1.5D;
							if (!world.isRemote)
								caught.velocityChanged = true;
						}
					}
					else if (caught instanceof EntityLivingBase && !(caught instanceof EntityPlayer && immune)) {
						caught.setFire(2);
						if (!world.isRemote && ((EntityLivingBase)caught).getHealth() > 0 && this.canDamageEngine(caught))
							this.damageEngine();
						if (FOD > 8)
							FOD = 8;
						if (caught instanceof EntityChicken && !caught.isDead && ((EntityChicken)caught).getHealth() > 0) {
							chickenCount++;
							if (chickenCount >= 50) {
								RotaryAchievements.JETCHICKEN.triggerAchievement(this.getPlacer());
							}
						}
						if (!caught.isDead && !(caught instanceof EntityLivingBase && ((EntityLivingBase)caught).getHealth() < 0))
							SoundRegistry.INGESTION.playSoundAtBlock(world, x, y, z, 1, 1.4F);
						caught.attackEntityFrom(RotaryCraft.jetingest, 10000);
						if (caught instanceof EntityPlayer) {
							RotaryAchievements.SUCKEDINTOJET.triggerAchievement((EntityPlayer)caught);
						}
					}
				}
			}
		}
	}

	private void damageEngine() {
		FOD++;
		//SoundRegistry.JETDAMAGE.playSoundAtBlock(worldObj, xCoord, yCoord, zCoord);
	}

	private boolean canDamageEngine(Entity caught) {
		if (caught instanceof EntityChicken)
			return false;
		if (caught instanceof EntityBat)
			return false;
		if (caught instanceof EntitySilverfish)
			return false;
		if (caught instanceof EntityItem)
			return false;
		if (caught instanceof EntityXPOrb)
			return false;
		return caught instanceof EntityLivingBase;
	}

	private AxisAlignedBB getSuctionZone(World world, int x, int y, int z, int meta, int step) {
		int minx = 0;
		int miny = 0;
		int minz = 0;
		int maxx = 0;
		int maxy = 0;
		int maxz = 0;

		switch (meta) {
		case 0:
			minx = x+1+step;
			maxx = x+1+step+1;
			miny = y-step;
			maxy = y+step+1;
			minz = z-step;
			maxz = z+step+1;
			dumpx = x-1;
			dumpz = z;
			dumpvx = -1;
			dumpvz = 0;
			break;
		case 1:
			minx = x-1-step;
			maxx = x-1-step+1;
			miny = y-step;
			maxy = y+step+1;
			minz = z-step;
			maxz = z+step+1;
			dumpx = x+1;
			dumpz = z;
			dumpvx = 1;
			dumpvz = 0;
			break;
		case 2:
			minz = z+1+step;
			maxz = z+1+step+1;
			miny = y-step;
			maxy = y+step+1;
			minx = x-step;
			maxx = x+step+1;
			dumpx = x;
			dumpz = z-1;
			dumpvx = 0;
			dumpvz = -1;
			break;
		case 3:
			minz = z-1-step;
			maxz = z-1-step+1;
			miny = y-step;
			maxy = y+step+1;
			minx = x-step;
			maxx = x+step+1;
			dumpx = x;
			dumpz = z+1;
			dumpvx = 0;
			dumpvz = 1;
			break;
		}

		return AxisAlignedBB.getBoundingBox(minx, miny, minz, maxx, maxy, maxz).expand(0.25, 0.25, 0.25);
	}

	private void playSounds(World world, int x, int y, int z, float pitchMultiplier) {
		soundtick++;
		if (type.jetNoise() && FOD > 0 && rand.nextInt(2*(9-FOD)) == 0) {
			world.playSoundEffect(x+0.5, y+0.5, z+0.5, "mob.blaze.hit", 1F+rand.nextFloat(), 1F);
			world.spawnParticle("crit", x+rand.nextFloat(), y+rand.nextFloat(), z+rand.nextFloat(), -0.5+rand.nextFloat(), rand.nextFloat(), -0.5+rand.nextFloat());
		}
		if (!ConfigRegistry.ENGINESOUNDS.getState())
			return;
		float volume = 1;
		if (this.isMuffled(world, x, y, z)) {
			volume = 0.3125F;
		}

		if (soundtick < type.getSoundLength(FOD, 1F/pitchMultiplier) && soundtick < 2000)
			return;
		soundtick = 0;

		if (type.electricNoise())
			SoundRegistry.ELECTRIC.playSoundAtBlock(world, x, y, z, 0.125F*volume, 1F*pitchMultiplier);
		if (type.turbineNoise()) {
			float pitch = 1F;
			if (type.jetNoise()) {
				pitch = 1F/(0.125F*FOD+1);
			}
			else {
				volume *= 0.125F;
			}
			if (type.jetNoise()) {
				if (jetstarttimer.getTick() >= jetstarttimer.getCap())
					SoundRegistry.JET.playSoundAtBlock(world, x, y, z, volume, pitch*pitchMultiplier);
				else
					soundtick = 2000;
			}
			else
				SoundRegistry.MICRO.playSoundAtBlock(world, x, y, z, volume, pitch*pitchMultiplier);
		}
		if (type.steamNoise())
			SoundRegistry.STEAM.playSoundAtBlock(world, x, y, z, 0.7F*volume, 1F*pitchMultiplier);
		if (type.carNoise())
			SoundRegistry.CAR.playSoundAtBlock(world, x, y, z, 0.33F*volume, 0.9F*pitchMultiplier);
		if (type.waterNoise() && (this.isFrontOfArray() || !this.isPartOfArray()))
			SoundRegistry.HYDRO.playSoundAtBlock(world, x, y, z, 1F*volume, 0.9F*pitchMultiplier);
		if (type.windNoise()) {
			SoundRegistry.WIND.playSoundAtBlock(world, x, y, z, 1.1F*volume, 1F*pitchMultiplier);
		}
	}

	private boolean isMuffled(World world, int x, int y, int z) {
		for (int i = 0; i < 6; i++) {
			ForgeDirection dir = dirs[i];
			if (dir != ForgeDirection.DOWN) {
				int dx = x+dir.offsetX;
				int dy = y+dir.offsetY;
				int dz = z+dir.offsetZ;
				if ((dir != write.getOpposite() && dir != write) || dir == ForgeDirection.UP) {
					int id = world.getBlockId(dx, dy, dz);
					if (id != Block.cloth.blockID)
						return false;
				}
			}
		}
		return true;
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta)
	{
		super.updateTileEntity();
		this.getIOSides(world, x, y, z, meta);

		timer.updateTicker("temperature");
		if (this.isShutdown()) {
			omega = torque = 0;
			power = 0;
		}
		else {
			this.getEngineType(world, x, y, z, meta);
			power = (long)torque*(long)omega;
		}

		if (type == EngineType.JET && power > 0)
			jetstarttimer.update();

		float pitch = 1F;
		float soundfactor = 1F;
		if (type.isECUControllable()) {
			if (MachineRegistry.getMachine(world, x, y-1, z) == MachineRegistry.ECU) {
				TileEntityEngineController te = (TileEntityEngineController)world.getBlockTileEntity(x, y-1, z);
				if (te != null) {
					if (te.canProducePower()) {
						if (omega >= type.getSpeed()*te.getSpeedMultiplier()) {
							//omega = (int)(omega*te.getSpeedMultiplier());
							int max = (int)(type.getSpeed()*te.getSpeedMultiplier());
							//this.updateSpeed(max, omega < max);
						}
						int fuelcap = timer.getCapOf("fuel");
						fuelcap = fuelcap*te.getFuelMultiplier();
						timer.setCap("fuel", fuelcap);
						pitch = te.getSoundStretch();
						soundfactor = 1F/te.getSoundStretch();
						int soundcap = timer.getCapOf("sound");
						soundcap = (int)(soundcap*soundfactor);
						timer.setCap("sound", soundcap);
						int tempcap = timer.getCapOf("temperature");
						tempcap *= soundfactor;
						timer.setCap("temperature", tempcap);
					}
					else {
						//this.updateSpeed(0, false);
						if (omega == 0)
							torque = 0;
						power = (long)omega*(long)torque;
						if (type.hasTemperature()) {
							if (timer.checkCap("temperature")) {
								this.updateTemperature(world, x, y, z, meta);
							}
						}
						soundtick = 2000;
						jetstarttimer.reset();
						lastpower = power;
						return;
					}
				}
			}
		}

		this.basicPowerReceiver();

		if (type.isJetFueled() && inv[0] != null && fuel.getLevel()+ItemFuelLubeBucket.JET_VALUE <= FUELCAP) {
			if (inv[0].itemID == ItemStacks.fuelbucket.itemID && inv[0].getItemDamage() == ItemStacks.fuelbucket.getItemDamage()) {
				inv[0] = new ItemStack(Item.bucketEmpty.itemID, 1, 0);
				fuel.addLiquid(ItemFuelLubeBucket.JET_VALUE, RotaryCraft.jetFuelFluid);
			}
		}
		timer.updateTicker("fuel");
		if (type.burnsFuel())
			this.consumeFuel();

		if (type.requiresLubricant())
			this.distributeLubricant(world, x, y, z);

		if (power > 0) {
			this.playSounds(world, x, y, z, pitch);
			if (type == EngineType.JET) {
				double dx = x-backx;
				double dz = z-backz;
				dx /= 2;
				dz /= 2;
				double vx = -(x-backx)/2D;
				double vz = -(z-backz)/2D;
				world.spawnParticle("smoke", dx+x+0.25+0.5*rand.nextDouble(), y+0.5*rand.nextDouble(), dz+z+0.25+0.5*rand.nextDouble(), -vx-0.1+0.2*rand.nextDouble(), -0.1+0.2*rand.nextDouble(), -vz-0.1+0.2*rand.nextDouble());
			}
			else if (type == EngineType.HYDRO) {
				int[] xz = this.getWaterColumnPos();
				ReikaParticleHelper.RAIN.spawnAroundBlock(world, x, y, z, 16);
				ReikaParticleHelper.RAIN.spawnAroundBlock(world, xz[0], y, xz[1], 16);
			}
		}
		else if (soundtick < type.getSoundLength(FOD, soundfactor))
			soundtick = 2000;

		lastpower = power;
	}

	private void distributeLubricant(World world, int x, int y, int z) {
		for (int i = 2; i < 6; i++) {
			ForgeDirection dir = dirs[i];
			int dx = x+dir.offsetX;
			int dy = y+dir.offsetY;
			int dz = z+dir.offsetZ;
			MachineRegistry m = MachineRegistry.getMachine(world, dx, dy, dz);
			if (m == MachineRegistry.ENGINE) {
				TileEntityEngine eng = (TileEntityEngine)world.getBlockTileEntity(dx, dy, dz);
				if (eng.type.requiresLubricant()) {
					int it = eng.lubricant.getLevel();
					int dL = lubricant.getLevel()-it;
					if (dL > 3) {
						eng.lubricant.addLiquid(dL/4, FluidRegistry.getFluid("lubricant"));
						lubricant.removeLiquid(dL/4);
					}
				}
			}
		}
		if (!lubricant.isEmpty() && omega > 0) {
			if (world.getWorldTime()%40 == 0)
				lubricant.removeLiquid(1);
		}
	}

	public void getIOSides(World world, int x, int y, int z, int metadata) {
		switch(metadata) {
		case 0:
			write = ForgeDirection.WEST;
			backx = x+1;
			backz = z;
			break;
		case 1:
			write = ForgeDirection.EAST;
			backx = x-1;
			backz = z;
			break;
		case 2:
			write = ForgeDirection.NORTH;
			backx = x;
			backz = z+1;
			break;
		case 3:	//works
			write = ForgeDirection.SOUTH;
			backx = x;
			backz = z-1;
			break;
		}
	}

	@Override
	protected void writeSyncTag(NBTTagCompound NBT)
	{
		super.writeSyncTag(NBT);

		if (type.hasTemperature())
			NBT.setInteger("temperature", temperature);

		if (type.needsWater())
			water.writeToNBT(NBT);
		if (type.isEthanolFueled() || type.isJetFueled())
			fuel.writeToNBT(NBT);

		if (type.usesAdditives())
			NBT.setInteger("additive", additives);
		if (type == EngineType.JET) {
			NBT.setBoolean("choke", isChoking);
			NBT.setBoolean("jetfail", isJetFailing);
		}

		if (type.requiresLubricant())
			lubricant.writeToNBT(NBT);
	}

	@Override
	protected void readSyncTag(NBTTagCompound NBT)
	{
		super.readSyncTag(NBT);

		if (type.hasTemperature())
			temperature = NBT.getInteger("temperature");

		if (type.requiresLubricant())
			lubricant.readFromNBT(NBT);

		if (type.needsWater())
			water.readFromNBT(NBT);
		if (type.isEthanolFueled() || type.isJetFueled())
			fuel.readFromNBT(NBT);

		if (type.usesAdditives())
			additives = NBT.getInteger("additive");
		if (type == EngineType.JET) {
			isChoking = NBT.getBoolean("choke");
			isJetFailing = NBT.getBoolean("jetfail");
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound NBT) {
		NBT.setInteger("type", type.ordinal());
		super.writeToNBT(NBT);
		NBT.setInteger("FOD", FOD);
		NBT.setInteger("chickens", chickenCount);
		timer.writeToNBT(NBT, "engine");
	}

	@Override
	public void readFromNBT(NBTTagCompound NBT) {
		type = EngineType.setType(NBT.getInteger("type"));
		super.readFromNBT(NBT);
		FOD = NBT.getInteger("FOD");
		chickenCount = NBT.getInteger("chickens");
		timer.readFromNBT(NBT, "engine");

		if (omega > type.getSpeed())
			omega = type.getSpeed();
		if (torque > type.getTorque())
			torque = type.getTorque();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack is) {
		if (!type.isValidFuel(is))
			return false;
		if (i >= type.getSizeInventory())
			return false;
		switch(type) {
		case GAS:
		case AC:
			return true;
		case SPORT:
			return (i == 0 && is.itemID == ItemRegistry.ETHANOL.getShiftedID()) || (i == 1 && type.isAdditive(is));
		default:
			return false;
		}
	}

	@Override
	public boolean hasModelTransparency() {
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		if (type == EngineType.AC) {
			if (ReikaItemHelper.matchStacks(itemstack, ItemStacks.shaftcore)) {
				if (itemstack.stackTagCompound == null)
					return true;
				if (itemstack.stackTagCompound.getInteger("magnet") == 0)
					return true;
			}
			return false;
		}
		if (type == EngineType.STEAM) {
			return itemstack.itemID == Item.bucketEmpty.itemID;
		}
		return false;
	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {
		if (!this.isInWorld()) {
			phi = 0;
			return;
		}
		double pow = 1.05;
		double mult = 1;
		if (type == EngineType.JET)
			pow = 1.1;
		if (type == EngineType.HYDRO) {
			mult = 256F/type.getSpeed();
		}
		phi += ReikaMathLibrary.doubpow(ReikaMathLibrary.logbase(mult*omega+1, 2), pow);
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public MachineRegistry getMachine() {
		return MachineRegistry.ENGINE;
	}

	@Override
	public int getThermalDamage() {
		if (type.canHurtPlayer() && type.hasTemperature())
			return (temperature)/100;
		return 0;
	}

	@Override
	public int getRedstoneOverride() {
		if (type.burnsFuel()) {
			if (type.isEthanolFueled())
				return 15*fuel.getLevel()/FUELCAP;
			if (type.isJetFueled())
				return 15*fuel.getLevel()/FUELCAP;
			else
				return 15*water.getLevel()/FUELCAP;
		}
		return 0;
	}

	public void jetEngineDetonation(World world, int x, int y, int z, int meta) {
		AxisAlignedBB zone = this.getFlameZone(world, x, y, z, meta);
		List in = world.getEntitiesWithinAABB(EntityLivingBase.class, zone);
		for (int i = 0; i < in.size(); i++) {
			EntityLivingBase e = (EntityLivingBase)in.get(i);
			e.setFire(2);
		}
		double vx = (x-backx)/2D;
		double vz = (z-backz)/2D;
		for (int i = 0; i < 16; i++) {
			String part;
			if (i%2 == 0)
				part = "flame";
			else
				part = "smoke";
			world.spawnParticle(part, x+0.25+0.5*rand.nextDouble(), y+0.25+0.5*rand.nextDouble(), z+0.25+0.5*rand.nextDouble(), vx-0.1+0.2*rand.nextDouble(), -0.1+0.2*rand.nextDouble(), vz-0.1+0.2*rand.nextDouble());
		}
		int dx = x-backx;
		int dz = z-backz;
		for (int i = 0; i < 16; i++) {
			ReikaWorldHelper.temperatureEnvironment(world, x+dx*i, y, z+dz*i, 800);
		}
		world.playSoundEffect(x+0.5, y+0.5, z+0.5, "mob.blaze.hit", 1F, 1F);
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			if (fuel.getLevel() < FUELCAP/12 && rand.nextInt(10) == 0) {
				ReikaPacketHelper.sendUpdatePacket(RotaryCraft.packetChannel, PacketRegistry.ENGINEBACKFIRE.getMinValue(), this);
				this.backFire(world, x, y, z);
			}
			if (fuel.getLevel() < FUELCAP/4 && rand.nextInt(20) == 0) {
				ReikaPacketHelper.sendUpdatePacket(RotaryCraft.packetChannel, PacketRegistry.ENGINEBACKFIRE.getMinValue(), this);
				this.backFire(world, x, y, z);
			}
			else if (rand.nextInt(40) == 0) {
				ReikaPacketHelper.sendUpdatePacket(RotaryCraft.packetChannel, PacketRegistry.ENGINEBACKFIRE.getMinValue(), this);
				this.backFire(world, x, y, z);
			}
		}
		if (rand.nextInt(2) == 0)
			temperature++;

		if (temperature >= 800) {
			RotaryCraft.logger.warn("WARNING: "+this+" is near explosion!");
		}

		if (temperature > 1000) {
			MinecraftForge.EVENT_BUS.post(new JetEngineExplosionEvent(this));
			int r = 6;
			for (int i = -r; i <= r; i++) {
				for (int j = -r; j <= r; j++) {
					for (int k = -r; k <= r; k++) {
						if (ConfigRegistry.BLOCKDAMAGE.getState())
							ReikaWorldHelper.temperatureEnvironment(world, x+i, y+j, z+k, 1000);
						world.spawnParticle("lava", x+i, y+j, z+k, 0, 0, 0);
						world.spawnParticle("lava", x+i, y+j, z+k, rand.nextDouble()-0.5, rand.nextDouble()-0.5, rand.nextDouble()-0.5);
					}
				}
			}
			if (!world.isRemote) {
				world.newExplosion(null, x+0.5, y+0.5, z+0.5, 12F, true, true);
				for (int m = 0; m < 6; m++)
					world.newExplosion(null, x-4+rand.nextInt(11), y-4+rand.nextInt(11), z-4+rand.nextInt(11), 4F+rand.nextFloat()*2, true, true);
			}
		}
	}

	public void backFire(World world, int x, int y, int z) {
		double vx = (x-backx)/2D;
		double vz = (z-backz)/2D;
		world.createExplosion(null, x+0.5, y+0.5, z+0.5, 2*rand.nextFloat(), false);
		ReikaSoundHelper.playSoundAtBlock(world, x, y, z, "random.explode", 1, 0.5F);
		for (int i = 0; i < 32; i++) {
			String part;
			if (i%2 == 0)
				part = "flame";
			else
				part = "smoke";
			world.spawnParticle(part, x+0.25+0.5*rand.nextDouble(), y+0.25+0.5*rand.nextDouble(), z+0.25+0.5*rand.nextDouble(), -vx-0.1+0.2*rand.nextDouble(), -0.1+0.2*rand.nextDouble(), -vz-0.1+0.2*rand.nextDouble());
		}
	}

	private AxisAlignedBB getFlameZone(World world, int x, int y, int z, int meta) {
		switch(meta) {
		case 0: //-x
			return AxisAlignedBB.getAABBPool().getAABB(x-6, y, z, x+1, y+1, z+1);
		case 1: //+x
			return AxisAlignedBB.getAABBPool().getAABB(x, y, z, x+7, y+1, z+1);
		case 2: //-z
			return AxisAlignedBB.getAABBPool().getAABB(x, y, z-6, x+1, y+1, z+1);
		case 3: //+z
			return AxisAlignedBB.getAABBPool().getAABB(x, y, z, x+1, y+1, z+7);
		default:
			return null;
		}
	}

	private void launchEntities(World world, int x, int y, int z) {
		AxisAlignedBB box = AxisAlignedBB.getAABBPool().getAABB(x, y, z, x+1, y+1, z+1).expand(8, 8, 8);
		List<Entity> inbox = world.getEntitiesWithinAABB(Entity.class, box);
		for (int i = 0; i < inbox.size(); i++) {
			Entity e = inbox.get(i);
			double dx = e.posX-x-0.5;
			double dy = e.posY-y-0.5;
			double dz = e.posZ-z-0.5;
			double dd = ReikaMathLibrary.py3d(dx, dy, dz);
			e.motionX += 2*dx/dd;
			e.motionY += 2*dy/dd;
			e.motionZ += 2*dz/dd;
			if (!world.isRemote)
				e.velocityChanged = true;
			if (e instanceof EntityPainting || e instanceof EntityItemFrame)
				e.attackEntityFrom(DamageSource.generic, 10);
		}
	}

	public int getFuelLevel() {
		if (!type.burnsFuel())
			return -1;
		if (type.isEthanolFueled())
			return fuel.getLevel();
		if (type.isJetFueled())
			return fuel.getLevel();
		return water.getLevel();
	}

	public int getFuelCapacity() {
		if (type.isEthanolFueled())
			return FUELCAP;
		if (type.isJetFueled())
			return FUELCAP;
		if (type == EngineType.STEAM)
			return CAPACITY;
		return 0;
	}

	/** In seconds */
	public int getFuelDuration() {
		if (!type.burnsFuel())
			return -1;
		int fuel = this.getFuelLevel();
		float burnprogress = 0;
		if (fuel > 0)
			burnprogress = 1F-timer.getPortionOfCap("fuel")/fuel;
		float factor = type.getFuelUnitDuration()/timer.getCapOf("fuel"); //to compensate for 4x burn during spinup
		if (factor <= 0)
			return 0;
		return (int)((fuel*type.getFuelUnitDuration()*(burnprogress))*5/factor/RotaryConfig.MILLIBUCKET);
	}

	/** In seconds */
	public int getFullTankDuration() {
		if (!type.burnsFuel())
			return -1;
		return this.getFuelCapacity()*type.getFuelUnitDuration()*5;
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m == MachineRegistry.PIPE || m == MachineRegistry.FUELLINE || m == MachineRegistry.HOSE;
	}

	@Override
	public boolean canConnectToPipeOnSide(MachineRegistry p, ForgeDirection side) {
		if (type == null)
			return false;
		if (type.isJetFueled())
			if (p == MachineRegistry.FUELLINE && side == ForgeDirection.DOWN)
				return true;
		if (type.isEthanolFueled())
			if (p == MachineRegistry.FUELLINE && side == ForgeDirection.DOWN)
				return true;
		if (type.isWaterPiped() && p == MachineRegistry.PIPE) {
			switch(side) {
			case EAST:
				return this.getBlockMetadata() == 0;
			case SOUTH:
				return this.getBlockMetadata() == 2;
			case WEST:
				return this.getBlockMetadata() == 1;
			case NORTH:
				return this.getBlockMetadata() == 3;
			default:
				return false;
			}
		}
		if (type.requiresLubricant() && p == MachineRegistry.HOSE) {
			//ReikaJavaLibrary.pConsole(this.getBlockMetadata()+":"+side.name());
			switch(side) {
			case EAST:
				return this.getBlockMetadata() == 0;
			case SOUTH:
				return this.getBlockMetadata() == 2;
			case WEST:
				return this.getBlockMetadata() == 1;
			case NORTH:
				return this.getBlockMetadata() == 3;
			default:
				return false;
			}
		}
		return false;
	}

	public void repairJet() {
		FOD = 0;
		isJetFailing = false;
		temperature = ReikaWorldHelper.getAmbientTemperatureAt(worldObj, xCoord, yCoord, zCoord);
	}

	public void repairJetPartial() {
		if (FOD > 0)
			FOD--;
	}

	@Override
	public void addTemperature(int temp) {
		temperature += temp;
	}

	@Override
	public int getTemperature() {
		return temperature;
	}

	private boolean isPartOfArray() {
		return this.isBackEndOfArray() || this.isFrontOfArray();
	}

	public boolean isBackEndOfArray() {
		MachineRegistry to = this.getMachine(write);
		if (to == MachineRegistry.ENGINE) {
			TileEntityEngine te = (TileEntityEngine)this.getAdjacentTileEntity(write);
			return te.type == EngineType.HYDRO;
		}
		return false;
	}

	public boolean isFrontOfArray() {
		MachineRegistry from = MachineRegistry.getMachine(worldObj, backx, yCoord, backz);
		MachineRegistry to = this.getMachine(write);
		if (from == MachineRegistry.ENGINE && to != MachineRegistry.ENGINE) {
			TileEntityEngine te = (TileEntityEngine)worldObj.getBlockTileEntity(backx, yCoord, backz);
			return te.type == EngineType.HYDRO;
		}
		return false;
	}

	public double getArrayTorqueMultiplier() {
		boolean front = this.isFrontOfArray();
		boolean back = this.isBackEndOfArray();
		if (!front && !back)
			return 1;
		if (back)
			return 0;
		if (front) {
			BlockArray b = new BlockArray();
			b.recursiveAdd(worldObj, xCoord, yCoord, zCoord, this.getTileEntityBlockID());
			double size = 0;
			for (int i = 0; i < b.getSize(); i++) {
				int[] xyz = b.getNthBlock(i);
				TileEntity te = worldObj.getBlockTileEntity(xyz[0], xyz[1], xyz[2]);
				if (te instanceof TileEntityEngine) {
					TileEntityEngine eng = (TileEntityEngine)te;
					if (eng.type == EngineType.HYDRO) {
						if (eng.hydroCheck(worldObj, xyz[0], xyz[1], xyz[2], eng.getBlockMetadata())) {
							if (eng.omega == omega) {
								//float fac = eng.getHydroFactor(worldObj, xyz[0], xyz[1], xyz[2], true);
								size += 1;//*fac;
							}
							else {
								ReikaParticleHelper.CRITICAL.spawnAroundBlock(worldObj, xyz[0], xyz[1], xyz[2], 5);
								if (rand.nextInt(3) == 0)
									ReikaSoundHelper.playSoundAtBlock(worldObj, xyz[0], xyz[1], xyz[2], "mob.blaze.hit");
							}
						}
					}
				}
			}
			return size;
		}
		else //never happens
			return 1;
	}

	@Override
	public void onEMP() {
		if (type.isEMPImmune())
			return;
		else
			super.onEMP();
	}

	@Override
	public PowerSourceList getPowerSources(TileEntityIOMachine io, ShaftMerger caller) {
		return new PowerSourceList().addSource(this);
	}

	public long getMaxPower() {
		if (type == null)
			return 0;
		return type.getPower();
	}

	public long getCurrentPower() {
		return power;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		Fluid f = resource.getFluid();
		if (!this.canFill(from, f))
			return 0;
		if (f.equals(FluidRegistry.WATER)) {
			return water.fill(resource, doFill);
		}
		else if (f.equals(FluidRegistry.getFluid("lubricant"))) {
			return lubricant.fill(resource, doFill);
		}
		else {
			return fuel.fill(resource, doFill);
		}
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		if (!type.canReceiveFluid(fluid))
			return false;
		if (fluid.equals(FluidRegistry.WATER)) {
			int dx = xCoord+from.offsetX;
			int dy = yCoord+from.offsetY;
			int dz = zCoord+from.offsetZ;
			return dx == backx && dy == yCoord && dz == backz;
		}
		else if (fluid.equals(FluidRegistry.getFluid("lubricant"))) {
			int dx = xCoord+from.offsetX;
			int dy = yCoord+from.offsetY;
			int dz = zCoord+from.offsetZ;
			return dx == backx && dy == yCoord && dz == backz;
		}
		else if (fluid.equals(FluidRegistry.getFluid("jet fuel"))) {
			return from == ForgeDirection.DOWN;
		}
		else if (fluid.equals(FluidRegistry.getFluid("rc ethanol"))) {
			return from == ForgeDirection.DOWN;
		}
		else if (fluid.equals(FluidRegistry.getFluid("bioethanol"))) {
			return from == ForgeDirection.DOWN;
		}
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[]{water.getInfo(), fuel.getInfo(), lubricant.getInfo()};
	}

	public void addFuel(int amt) {
		fuel.addLiquid(amt, type.getFuelType());
	}

	public void addLubricant(int amt) {
		lubricant.addLiquid(amt, FluidRegistry.getFluid("lubricant"));
	}

	public void subtractFuel(int amt) {
		fuel.removeLiquid(amt);
	}

	public void addWater(int amt) {
		water.addLiquid(amt, FluidRegistry.WATER);
	}

	public void setLube(int amt) {
		lubricant.setContents(amt, FluidRegistry.getFluid("lubricant"));
	}

	public int getWater() {
		return water.getLevel();
	}

	public int getLube() {
		return lubricant.getLevel();
	}

	@Override
	public Flow getFlowForSide(ForgeDirection side) {
		return Flow.INPUT;
	}

	@Override
	public boolean hasInventory() {
		return type.hasInventory();
	}

	@Override
	public int getEmittingX() {
		return xCoord+write.offsetX;
	}

	@Override
	public int getEmittingY() {
		return yCoord+write.offsetY;
	}

	@Override
	public int getEmittingZ() {
		return zCoord+write.offsetZ;
	}

	public void magneticInterference(int mag, double dd) {
		torque = (int)(0.125*ReikaMathLibrary.logbase(mag, 2)*this.getEngineType().getTorque()/dd);
		omega = (int)(0.125*ReikaMathLibrary.logbase(mag, 2)*this.getEngineType().getSpeed()/dd/4D);
		power = (long)omega*(long)torque;
	}

	@Override
	public void upgrade() {
		if (type == EngineType.GAS) {
			type = EngineType.SPORT;
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, type.ordinal(), 3);
			this.syncAllData();
		}
	}

	public boolean canUpgradeWith(ItemStack item) {
		return item.getItemDamage() == 0;
	}
}

