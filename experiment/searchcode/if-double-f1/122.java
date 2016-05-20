package com.csci3130.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.csci3130.factory.Factory;
import com.csci3130.helper.Reference;
import com.csci3130.init.FactoryBlocks;
import com.csci3130.tileentity.TileEntityMagicalWelder;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * @author Tadhg Creedon, Kyle Ramey, Jason Tait, Matthew Tetford.
 * @version 0.1
 * @see net.minecraft.block.BlockFurnace
 *
 * Class for the magical welder block.
 */
public class BlockMagicalWelder extends BlockContainer {

	@SideOnly(Side.CLIENT)
	private IIcon top;
	@SideOnly(Side.CLIENT)
	private IIcon front;

	private static boolean isBurning;
	private final boolean isBurning2;
	private final Random random = new Random();

	/**
	 * Default constructor for magical furnace.
	 * @param isActive
	 */
	public BlockMagicalWelder(boolean isActive) {
		super(Material.rock);
		isBurning2 = isActive;
		setHardness(5f);
		setResistance(7.0f);
	}

	/**
	 * Registers textures for the multiple sides of the furnace and sets the front
	 * depending on whether the block is active.
	 * @param iconregister - IIconRegister
	 */
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconregister) {
		this.blockIcon = iconregister.registerIcon(Reference.MODID + ":BlockWelderSide");
		this.front = iconregister.registerIcon(this.isBurning2 ? Reference.MODID + ":BlockWelderActive" : Reference.MODID + ":BlockWelderInactive");
		this.top = iconregister.registerIcon(Reference.MODID + ":BlockWelderTop");
	}

	/**
	 * Gets registered textures for magical furnace as an IIcon.
	 * @param side_ (int) - side of the IIcon we want.
	 * @param meta (int) - used for animations, appropriate IIcon used depending on value.
	 * @return IIcon
	 */
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side_, int meta)
	{
		if(meta == 0){
			return side_ == 1 ? this.top : (side_ == 0 ? this.top : (side_ != 3 ? this.blockIcon : this.front));

		}
		else{
			return side_ == 1 ? this.top : (side_ == 0 ? this.top : (side_ != meta ? this.blockIcon : this.front));

		}
	}

	/**
	 * What the block does when activated (right-clicked by the player).
	 * @param world (World) - current world
	 * @param x (int) - x location
	 * @param y (int) - y location
	 * @param z (int) - z location
	 * @param player (Player) - Player who activated block
	 * @param par6 (int) - unknown
	 * @param par7 (float) - unknown
	 * @param par8 (float) - unknown
	 * @param par9 (float) - unknown
	 */
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		player.openGui(Factory.modInstance, 2, world, x, y, z);
		return true;
	}

	/**
	 * Gets the specified item to be dropped when the block is broken.
	 * @param par1 (int) - unknown
	 * @param par2Random (int) - new random
	 * @param par3 (int) - unknown
	 * @return item - item to be dropped
	 */
	public Item getItemDropped(int par1, Random random, int par3) {
		return Item.getItemFromBlock(FactoryBlocks.factoryMagicalWelder);
	}

	/**
	 * Gets the Item version of the Block.
	 * @param world (World) - current world
	 * @param par1 (int) - unknown
	 * @param par2 (int) - unknown
	 * @param par3 (int) - unknown
	 */
	public Item getItem(World world, int par2, int par3, int par4) {
		return Item.getItemFromBlock(FactoryBlocks.factoryMagicalWelder);
	}

	/**
	 * Returns a new instance of a block's tile entity class. Called on placing the block.
	 * @param world (World) - current world
	 * @param par1 (int) - unknown
	 * @return TileEntity - TileEntityMagicalWelder
	 */
	public TileEntity createNewTileEntity(World world, int par2)
	{
		return new TileEntityMagicalWelder();
	}

	/**
	 * Calls methods when the block is added to the world.
	 * @param world (World) - current world
	 * @param x (int) - x location
	 * @param y (int) - y location
	 * @param z (int) - z location
	 */
	@SideOnly(Side.CLIENT)
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		this.direction(world, x, y, z);
	}

	/**
	 * Sets direction of the block in the world.  Called by onBlockAdded().
	 * @param world (World) - current world
	 * @param x (int) - x location
	 * @param y (int) - y location
	 * @param z (int) - z location
	 */
	private void direction(World world, int x, int y, int z)
	{
		if (!world.isRemote)
		{
			Block block = world.getBlock(x, y, z - 1);
			Block block1 = world.getBlock(x, y, z + 1);
			Block block2 = world.getBlock(x - 1, y, z);
			Block block3 = world.getBlock(x + 1, y, z);
			byte b0 = 3;

			if (block.func_149730_j() && !block1.func_149730_j())
			{
				b0 = 3;
			}

			if (block1.func_149730_j() && !block.func_149730_j())
			{
				b0 = 2;
			}

			if (block2.func_149730_j() && !block3.func_149730_j())
			{
				b0 = 5;
			}

			if (block3.func_149730_j() && !block2.func_149730_j())
			{
				b0 = 4;
			}

			world.setBlockMetadataWithNotify(x, y, z, b0, 2);
		}
	}

	/**
	 * Sets the direction of the block based on the direction of the entity that places it.
	 * @param world (World) - current world
	 * @param x (int) - x location
	 * @param y (int) - y location
	 * @param z (int) - z location
	 * @param entity (EntityLivingBase) - Entity that placed the block.
	 * @param itemstack (ItemStack) - ...
	 */
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemstack) {
		int direction = MathHelper.floor_double((double) (entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

		if (direction == 0) {
			world.setBlockMetadataWithNotify(x, y, z, 2, 2);
		}

		if (direction == 1) {
			world.setBlockMetadataWithNotify(x, y, z, 5, 2);
		}

		if (direction == 2) {
			world.setBlockMetadataWithNotify(x, y, z, 3, 2);
		}

		if (direction == 3) {
			world.setBlockMetadataWithNotify(x, y, z, 4, 2);
		}

		if (itemstack.hasDisplayName()) {
			((TileEntityMagicalWelder) world.getTileEntity(x, y, z)).welderName(itemstack.getDisplayName());
		}
	}

	/**
	 * Updates block when a calling event makes a change.
	 * @param burning (boolean) - whether the new block's state is active.
	 * @param world (World) - current world
	 * @param x (int) - x location
	 * @param y (int) - y location
	 * @param z (int) - z location
	 */
	public static void updateBlockState(boolean burning, World world, int x, int y, int z) {
		int direction = world.getBlockMetadata(x, y, z);
		TileEntity tileentity = world.getTileEntity(x, y, z);
		isBurning = true;

		if (burning) {
			world.setBlock(x, y, z, FactoryBlocks.factoryMagicalWelderActive);
		} else {
			world.setBlock(x, y, z, FactoryBlocks.factoryMagicalWelder);
		}

		isBurning = false;
		world.setBlockMetadataWithNotify(x, y, z, direction, 2);

		if (tileentity != null) {
			tileentity.validate();
			world.setTileEntity(x, y, z, tileentity);
		}
	}

	/**
	 * Handles throwing out items contained in a block when broken.
	 * @param world (World) - current world
	 * @param x (int) - x location
	 * @param y (int) - y location
	 * @param z (int) - z location
	 * @param block (Block) - the block that is broken
	 * @param meta (int) - used on last line... not sure what it does :\
	 */
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		if (!isBurning) {
			TileEntityMagicalWelder tileentityMagicalWelder = (TileEntityMagicalWelder) world.getTileEntity(x, y, z);

			if (tileentityMagicalWelder != null) {
				for (int i = 0; i < tileentityMagicalWelder.getSizeInventory(); ++i) {
					ItemStack itemstack = tileentityMagicalWelder.getStackInSlot(i);

					if (itemstack != null) {
						float f = this.random.nextFloat() * 0.6F + 0.1F;
						float f1 = this.random.nextFloat() * 0.6F + 0.1F;
						float f2 = this.random.nextFloat() * 0.6F + 0.1F;

						while (itemstack.stackSize > 0) {
							int j = this.random.nextInt(21) + 10;

							if (j > itemstack.stackSize) {
								j = itemstack.stackSize;
							}

							itemstack.stackSize -= j;
							EntityItem entityitem = new EntityItem(world, (double) ((float) x + f), (double) ((float) y + f1), (double) ((float) z + f2), new ItemStack(itemstack.getItem(), j, itemstack.getItemDamage()));

							if (itemstack.hasTagCompound()) {
								entityitem.getEntityItem().setTagCompound(((NBTTagCompound) itemstack.getTagCompound().copy()));
							}

							float f3 = 0.025F;
							entityitem.motionX = (double) ((float) this.random.nextGaussian() * f3);
							entityitem.motionY = (double) ((float) this.random.nextGaussian() * f3 + 0.1F);
							entityitem.motionZ = (double) ((float) this.random.nextGaussian() * f3);
							world.spawnEntityInWorld(entityitem);
						}
					}
				}
				world.func_147453_f(x, y, z, block);
			}
		}
		super.breakBlock(world, x, y, z, block, meta);
	}

	/**
	 * Creates flame particles on the block randomly based on world ticks.
	 * @param world (World) - current world
	 * @param x (int) - x location
	 * @param y (int) - y location
	 * @param z (int) - z location
	 * @param rand (Random) - a random int
	 */
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand)
	{
		if (this.isBurning2)
		{
			int l = world.getBlockMetadata(x, y, z);
			float f = (float)x + 0.5F;
			float f1 = (float)y + 0.25F + rand.nextFloat() * 6.0F / 16.0F;
			float f2 = (float)z + 0.5F;
			float f3 = 0.52F;
			float f4 = rand.nextFloat() * 0.6F - 0.3F;

			if (l == 4)
			{
				world.spawnParticle("smoke", (double)(f - f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", (double)(f - f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
			}
			else if (l == 5)
			{
				world.spawnParticle("smoke", (double)(f + f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", (double)(f + f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
			}
			else if (l == 2)
			{
				world.spawnParticle("smoke", (double)(f + f4), (double)f1, (double)(f2 - f3), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", (double)(f + f4), (double)f1, (double)(f2 - f3), 0.0D, 0.0D, 0.0D);
			}
			else if (l == 3)
			{
				world.spawnParticle("smoke", (double)(f + f4), (double)f1, (double)(f2 + f3), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", (double)(f + f4), (double)f1, (double)(f2 + f3), 0.0D, 0.0D, 0.0D);
			}
		}
	}
}
