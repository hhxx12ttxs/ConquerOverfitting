package exnihilo.blocks.tileentities;

import exnihilo.Blocks;
import exnihilo.blocks.tileentities.TileEntityBarrel.BarrelMode;
import exnihilo.blocks.tileentities.TileEntitySieve.SieveMode;
import exnihilo.data.BlockData;
import exnihilo.registries.CompostRegistry;
import exnihilo.registries.CrucibleRegistry;
import exnihilo.registries.HeatRegistry;
import exnihilo.registries.helpers.Meltable;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityCrucible extends TileEntity implements IFluidHandler, ISidedInventory{
	private static final float MIN_RENDER_CAPACITY = 0.20f;
	private static final float MAX_RENDER_CAPACITY = 0.95f;
	private static final int MAX_FLUID = 10000;
	private static final int UPDATE_INTERVAL = 10;

	public enum CrucibleMode
	{EMPTY(0), USED(1);
	private CrucibleMode(int v){this.value = v;}
	public int value;
	}

	private boolean needsUpdate = false;
	private int updateTimer = 0;

	public FluidStack fluid;
	private int contentID = 0;
	private int contentMeta = 0;
	private float solidVolume = 0;
	private float airVolume = 0;
	private float fluidVolume = 0;
	public CrucibleMode mode;

	public TileEntityCrucible()
	{
		mode = mode.EMPTY;
		fluid = new FluidStack(FluidRegistry.WATER, 0);
	}

	public float getAdjustedVolume()
	{
		float volume = (solidVolume + fluidVolume + airVolume) / this.MAX_FLUID;
		float capacity = MAX_RENDER_CAPACITY - MIN_RENDER_CAPACITY;
		float adjusted = volume * capacity;		
		adjusted += MIN_RENDER_CAPACITY;
		return adjusted;
	}

	public Icon getContentIcon()
	{
		if (worldObj.isRemote)
		{
			Meltable meltable = CrucibleRegistry.getItem(this.contentID, this.contentMeta);

			if (meltable != null && meltable.getIcon() != null)
			{
				return meltable.getIcon();
			}
			else
			{
				return Block.stone.getIcon(0, 0);
			}
		}
		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);

		switch (compound.getInteger("mode"))
		{
		case 0:
			mode = CrucibleMode.EMPTY;
			break;

		case 1:
			mode = CrucibleMode.USED;
			break;
		}

		solidVolume = compound.getFloat("solidVolume");
		fluidVolume = compound.getFloat("fluidVolume");
		airVolume = compound.getFloat("airVolume");
		contentID = compound.getInteger("contentID");
		contentMeta = compound.getInteger("contentMeta");
		fluid = new FluidStack(FluidRegistry.getFluid(compound.getShort("fluid")), Math.round(fluidVolume));
	}

	@Override
	public void writeToNBT(NBTTagCompound compound)
	{
		super.writeToNBT(compound);
		compound.setInteger("mode", mode.value);
		compound.setFloat("solidVolume", solidVolume);
		compound.setFloat("fluidVolume", fluidVolume);
		compound.setFloat("airVolume", airVolume);
		compound.setInteger("contentID", contentID);
		compound.setInteger("contentMeta", contentMeta);
		compound.setShort("fluid", (short)fluid.fluidID);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound tag = new NBTTagCompound();
		this.writeToNBT(tag);

		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, BlockData.SIEVE_ID, tag);
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt)
	{
		NBTTagCompound tag = pkt.data;
		this.readFromNBT(tag);
	}

	public boolean addItem(ItemStack item)
	{
		if (!CrucibleRegistry.containsItem(item.itemID, item.getItemDamage()))
		{
			return false;
		}

		Meltable meltable = CrucibleRegistry.getItem(item.itemID, item.getItemDamage());

		if (!worldObj.isRemote && getCapacity() >= meltable.solidVolume && isFluidValid(meltable.fluid))
		{
			this.contentID = item.itemID; 
			this.contentMeta = item.getItemDamage();

			this.solidVolume += meltable.fluidVolume;
			this.airVolume += meltable.solidVolume - meltable.fluidVolume;

			this.mode = CrucibleMode.USED;
			this.fluid = new FluidStack(meltable.fluid.getID(), (int)fluidVolume);
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
		else
		{
			return false;
		}

		return true;
	}

	@Override
	public void updateEntity()
	{	
		float speed = this.getMeltSpeed();

		if (this.airVolume > 0)
		{
			this.airVolume -= this.airVolume * (speed / this.solidVolume);

			if (this.airVolume < 0)
			{
				this.airVolume = 0;
			}
		}

		if (this.solidVolume > 0)
		{
			if (this.solidVolume - speed >= 0)
			{
				this.fluidVolume += speed;
				this.solidVolume -= speed;
			}else
			{
				this.fluidVolume += this.solidVolume;
				this.solidVolume = 0;
			}

			
			fluid.amount = Math.round(fluidVolume);
			//System.out.println("fluid: " + fluid.amount + ", fluidVolume: " + fluidVolume + ", air: " + airVolume);
			needsUpdate = true;
		}
		else if (Math.round(this.getCapacity()) >= this.MAX_FLUID)
		{
			this.mode = CrucibleMode.EMPTY;
			needsUpdate = true;
		}

		if (updateTimer >= UPDATE_INTERVAL)
		{
			updateTimer = 0;
			if (needsUpdate)
			{
				needsUpdate = false;
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}
		else
		{
			updateTimer++;
		}
	}

	public float getCapacity()
	{
		return this.MAX_FLUID - (solidVolume + fluidVolume + airVolume);
	}

	public float getMeltSpeed()
	{
		int targetID = worldObj.getBlockId(xCoord, yCoord - 1, zCoord);
		int targetMeta = worldObj.getBlockMetadata(xCoord, yCoord - 1, zCoord);
		
		if (HeatRegistry.containsItem(targetID, targetMeta))
		{
			return HeatRegistry.getItem(targetID, targetMeta).value;
		}

		return 0.0f;
	}

	public boolean hasSolids()
	{
		return solidVolume > 0;
	}

	public boolean renderFluid()
	{
		if (solidVolume < fluidVolume && fluid.getFluid().getID() != FluidRegistry.WATER.getID())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean isFluidValid(Fluid fluid)
	{
		if (this.mode == CrucibleMode.EMPTY)
		{
			return true;
		}

		if (this.mode == CrucibleMode.USED && fluid.getID() == this.fluid.fluidID)
		{
			return true;
		}

		return false;
	}


	//IFluidHandler!	
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		//Simulate the fill to see if there is room for incoming liquids.
		int capacity = (int)this.getCapacity();

		if (!doFill)
		{
			if (mode == CrucibleMode.EMPTY)
			{
				return resource.amount;
			}

			if (mode == CrucibleMode.USED && resource.fluidID == fluid.fluidID)
			{
				if (capacity >= resource.amount)
				{
					return resource.amount;
				}else
				{
					return capacity;
				}
			}
		}else
			//Really fill the barrel.
		{
			if (mode == CrucibleMode.EMPTY)
			{
				if (resource.fluidID != fluid.fluidID)
				{
					fluid =  new FluidStack(FluidRegistry.getFluid(resource.fluidID),resource.amount);
				}else
				{
					fluid.amount = resource.amount;
				}
				mode = CrucibleMode.USED;
				this.fluidVolume += fluid.amount;

				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				return resource.amount;
			}

			if (mode == CrucibleMode.USED && resource.fluidID == fluid.fluidID)
			{
				if (capacity >= resource.amount)
				{
					fluidVolume += resource.amount;
					fluid.amount = (int)fluidVolume;
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
					return resource.amount;
				}else
				{
					fluidVolume += capacity;
					fluid.amount = (int)fluidVolume;
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
					//needsUpdate = true;
					return capacity;
				}
			}
		}

		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		if (resource == null || mode != CrucibleMode.USED || !resource.isFluidEqual(fluid))
			return null;

		if (!doDrain)
		{
			if (fluid.amount >= resource.amount)
			{
				FluidStack simulated = new FluidStack(FluidRegistry.getFluid(resource.fluidID),resource.amount);
				return simulated;
			}else
			{
				FluidStack simulated = new FluidStack(FluidRegistry.getFluid(resource.fluidID),fluid.amount);
				return simulated;
			}
		}else
		{
			if (fluid.amount > resource.amount)
			{
				FluidStack drained = new FluidStack(FluidRegistry.getFluid(resource.fluidID),resource.amount);
				fluidVolume -= resource.amount;
				fluid.amount =  (int)fluidVolume;
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				return drained;
			}else
			{
				FluidStack drained = new FluidStack(FluidRegistry.getFluid(resource.fluidID),fluid.amount);
				fluidVolume -= fluid.amount;
				fluid.amount = 0;
				//mode = CrucibleMode.EMPTY;
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				return drained;
			}
		}
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		if (mode != CrucibleMode.USED)
			return null;

		if (!doDrain)
		{
			if (fluid.amount >= maxDrain)
			{
				FluidStack simulated = new FluidStack(FluidRegistry.getFluid(fluid.fluidID),maxDrain);
				return simulated;
			}else
			{
				FluidStack simulated = new FluidStack(FluidRegistry.getFluid(fluid.fluidID),fluid.amount);
				return simulated;
			}
		}else
		{
			if (fluid.amount > maxDrain)
			{
				FluidStack drained = new FluidStack(FluidRegistry.getFluid(fluid.fluidID),maxDrain);
				fluidVolume -= maxDrain;
				fluid.amount = (int)fluidVolume;
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				return drained;
			}else
			{
				FluidStack drained = new FluidStack(FluidRegistry.getFluid(fluid.fluidID),fluid.amount);
				fluidVolume -= fluid.amount;
				fluid.amount = 0;
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				return drained;
			}
		}
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		FluidTankInfo info = new FluidTankInfo(fluid, MAX_FLUID);
		FluidTankInfo[] array =  new FluidTankInfo[1];
		array[0] = info;
		return array;
	}

	public int getLightLevel()
	{
		if (mode == CrucibleMode.USED)
		{
			float lumens = fluid.getFluid().getLuminosity() * (this.fluidVolume / this.MAX_FLUID);

			return Math.round(lumens);
		}
		return 0;
	}
	
	
	
	
	

	//ISidedInventory!
	@Override
	public int getSizeInventory() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		//This should never get called
		return null;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		//This should never get called.
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack item) {
		if (slot == 1)
		{			
			if (CrucibleRegistry.containsItem(item.itemID, item.getItemDamage()))
			{
				Meltable meltable = CrucibleRegistry.getItem(item.itemID, item.getItemDamage());
				
				if(this.getCapacity() >= meltable.solidVolume && isFluidValid(meltable.fluid))
				{
					this.addItem(item);
				}
			}
		}
	}

	@Override
	public String getInvName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInvNameLocalized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack item) {
		// TODO Auto-generated method stub		
		if (slot == 1)
		{
			if (CrucibleRegistry.containsItem(item.itemID, item.getItemDamage()))
			{
				Meltable meltable = CrucibleRegistry.getItem(item.itemID, item.getItemDamage());
				
				if(this.getCapacity() >= meltable.solidVolume && isFluidValid(meltable.fluid))
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		if (side == 1)
		{
			return new int[]{1};
		}

		return new int[0];
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack item, int side) {
		if (side == 1 && slot == 1)
		{
			if (CrucibleRegistry.containsItem(item.itemID, item.getItemDamage()))
			{
				Meltable meltable = CrucibleRegistry.getItem(item.itemID, item.getItemDamage());
				
				if(this.getCapacity() >= meltable.solidVolume && isFluidValid(meltable.fluid))
				{
					return true;
				}
			}

		}

		return false;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack item, int side) {
		//Never allow items to be extracted.
		return false;
	}
}

