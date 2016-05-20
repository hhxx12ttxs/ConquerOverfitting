package net.minecraft.ssTanksMOD;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityHook extends Entity implements IProjectile
{
	private int xTile = -1;
	private int yTile = -1;
	private int zTile = -1;
	private int inTile = 0;
	private int inData = 0;

	private float x = 0;
	private float y = 0;
	private float z = 0;

	public Entity bobber;

	public boolean inEntity;

	private boolean 初回アップデート = true;

	public boolean inGround = false;

	public int arrowShake = 0;

	/** The owner of this arrow. */
	public EntityPlayer shootingEntity;
	private int ticksInGround;
	private int ticksInAir = 0;
	private double damage = 2.0D;

	private int knockbackStrength;

	public EntityHook(World par1World)
	{
		super(par1World);
		this.ignoreFrustumCheck = true;
		this.setSize(0.5F, 0.5F);
	}

	public EntityHook(EntityPlayer shootingEntity,World par1World, double par2, double par4, double par6)
	{
		super(par1World);
		this.shootingEntity = shootingEntity;
		this.setSize(0.5F, 0.5F);
		this.setPosition(par2, par4, par6);
		this.yOffset = 0.0F;
	}

	protected void entityInit()
	{
		this.dataWatcher.addObject(16, Byte.valueOf((byte)0));
		this.dataWatcher.addObject(20, "");//名前
	}

	public EntityHook(World par1World, EntityPlayer par2EntityLiving, float par3)
	{
		super(par1World);
		this.renderDistanceWeight = 10.0D;
		this.shootingEntity = par2EntityLiving;

		this.setSize(0.5F, 0.5F);
		this.setLocationAndAngles(par2EntityLiving.posX, par2EntityLiving.posY + (double)par2EntityLiving.getEyeHeight(), par2EntityLiving.posZ, par2EntityLiving.rotationYaw, par2EntityLiving.rotationPitch);
		this.posX -= (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
		this.posY -= 0.10000000149011612D;
		this.posZ -= (double)(MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.yOffset = 0.0F;
		this.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI));
		this.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI));
		this.motionY = (double)(-MathHelper.sin(this.rotationPitch / 180.0F * (float)Math.PI));
		this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, par3 * 1.5F, 1.0F);
	}

	public EntityHook(World par1World, EntityPlayer par2EntityLiving, float par3,float ry)
	{
		super(par1World);
		this.shootingEntity = par2EntityLiving;

		this.setSize(0.5F, 0.5F);
		this.setLocationAndAngles(par2EntityLiving.posX, par2EntityLiving.posY + (double)par2EntityLiving.getEyeHeight(), par2EntityLiving.posZ, par2EntityLiving.rotationYaw+ry, par2EntityLiving.rotationPitch);
		this.posX -= (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
		this.posY -= 0.10000000149011612D;
		this.posZ -= (double)(MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.yOffset = 0.0F;
		this.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI));
		this.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI));
		this.motionY = (double)(-MathHelper.sin(this.rotationPitch / 180.0F * (float)Math.PI));
		this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, par3 * 1.5F, 1.0F);
	}

	public void setThrowableHeading(double par1, double par3, double par5, float par7, float par8)
	{
		float f2 = MathHelper.sqrt_double(par1 * par1 + par3 * par3 + par5 * par5);
		par1 /= (double)f2;
		par3 /= (double)f2;
		par5 /= (double)f2;
		par1 += this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double)par8;
		par3 += this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double)par8;
		par5 += this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double)par8;
		par1 *= (double)par7;
		par3 *= (double)par7;
		par5 *= (double)par7;
		this.motionX = par1;
		this.motionY = par3;
		this.motionZ = par5;
		float f3 = MathHelper.sqrt_double(par1 * par1 + par5 * par5);
		this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(par1, par5) * 180.0D / Math.PI);
		this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(par3, (double)f3) * 180.0D / Math.PI);
		this.ticksInGround = 0;
	}

	@SideOnly(Side.CLIENT)
	public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9)
	{
		if(clientproxy.mc.theWorld.getPlayerEntityByName(this.get名前()) != null)
			clientproxy.mc.theWorld.getPlayerEntityByName(this.get名前()).sendChatToPlayer("位置");
		this.setPosition(par1, par3, par5);
		this.setRotation(par7, par8);
	}

	@SideOnly(Side.CLIENT)
	public void setVelocity(double par1, double par3, double par5)
	{
		this.motionX = par1;
		this.motionY = par3;
		this.motionZ = par5;

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
		{
			float f = MathHelper.sqrt_double(par1 * par1 + par5 * par5);
			this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(par1, par5) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(par3, (double)f) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch;
			this.prevRotationYaw = this.rotationYaw;
			this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
			this.ticksInGround = 0;
		}
	}


	public void onUpdate()
	{
		super.onUpdate();

		if(初回アップデート&&!this.worldObj.isRemote&&this.shootingEntity != null)
		{
			this.dataWatcher.updateObject(20,this.shootingEntity.username);
			this.初回アップデート = false;
		}

		if(!this.worldObj.isRemote&&(this.shootingEntity == null||this.shootingEntity.isDead))
		{
			this.setDead();
			return;
		}

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
		{
			float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(this.motionY, (double)f) * 180.0D / Math.PI);
		}

		int i = this.worldObj.getBlockId(this.xTile, this.yTile, this.zTile);

		if (i > 0)
		{
			Block.blocksList[i].setBlockBoundsBasedOnState(this.worldObj, this.xTile, this.yTile, this.zTile);
			AxisAlignedBB axisalignedbb = Block.blocksList[i].getCollisionBoundingBoxFromPool(this.worldObj, this.xTile, this.yTile, this.zTile);

			if (axisalignedbb != null && axisalignedbb.isVecInside(this.worldObj.getWorldVec3Pool().getVecFromPool(this.posX, this.posY, this.posZ)))
			{
				this.inGround = true;
			}
		}

		if (this.arrowShake > 0)
		{
			--this.arrowShake;
		}

		if (this.bobber != null)
		{
			if (!this.bobber.isDead)
			{
				this.posX = this.bobber.posX;
				this.posY = this.bobber.boundingBox.minY + (double)this.bobber.height * 0.8D;
				this.posZ = this.bobber.posZ;
				this.setPosition(this.posX, this.posY, this.posZ);
				if(!this.worldObj.isRemote&&this.shootingEntity != null)
				{
					座標設定();
				}
				return;
			}
		}

		if (this.inGround)
		{
			if(!this.worldObj.isRemote&&this.shootingEntity != null)
			{
				座標設定();
			}
		}

		if (this.inGround)
		{
			this.setPosition(this.posX, this.posY, this.posZ);

			int j = this.worldObj.getBlockId(this.xTile, this.yTile, this.zTile);
			int k = this.worldObj.getBlockMetadata(this.xTile, this.yTile, this.zTile);

			if (j == this.inTile && k == this.inData)
			{
				++this.ticksInGround;
			}
			else
			{
				this.inGround = false;
				this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
				this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
				this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
				this.ticksInGround = 0;
				this.ticksInAir = 0;
			}
		}
		else
		{
			++this.ticksInAir;
			Vec3 vec3 = this.worldObj.getWorldVec3Pool().getVecFromPool(this.posX, this.posY, this.posZ);
			Vec3 vec31 = this.worldObj.getWorldVec3Pool().getVecFromPool(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks_do_do(vec3, vec31, false, true);
			vec3 = this.worldObj.getWorldVec3Pool().getVecFromPool(this.posX, this.posY, this.posZ);
			vec31 = this.worldObj.getWorldVec3Pool().getVecFromPool(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

			if (movingobjectposition != null)
			{
				vec31 = this.worldObj.getWorldVec3Pool().getVecFromPool(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
			}

			Entity entity = null;
			List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
			double d0 = 0.0D;
			int l;
			float f1;

			for (l = 0; l < list.size(); ++l)
			{
				Entity entity1 = (Entity)list.get(l);

				if (entity1.canBeCollidedWith() && (entity1 != this.shootingEntity || this.ticksInAir >= 5))
				{
					f1 = 0.3F;
					AxisAlignedBB axisalignedbb1 = entity1.boundingBox.expand((double)f1, (double)f1, (double)f1);
					MovingObjectPosition movingobjectposition1 = axisalignedbb1.calculateIntercept(vec3, vec31);

					if (movingobjectposition1 != null)
					{
						double d1 = vec3.distanceTo(movingobjectposition1.hitVec);

						if (d1 < d0 || d0 == 0.0D)
						{
							entity = entity1;
							d0 = d1;
						}
					}
				}
			}

			if (entity != null)
			{
				movingobjectposition = new MovingObjectPosition(entity);
			}

			if (movingobjectposition != null && movingobjectposition.entityHit != null && movingobjectposition.entityHit instanceof EntityPlayer)
			{
				EntityPlayer entityplayer = (EntityPlayer)movingobjectposition.entityHit;

				if (entityplayer.capabilities.disableDamage || this.shootingEntity instanceof EntityPlayer && !((EntityPlayer)this.shootingEntity).func_96122_a(entityplayer))
				{
					movingobjectposition = null;
				}
			}

			float f2;
			float f3;

			if (movingobjectposition != null)
			{
				if (movingobjectposition.entityHit != null&&!this.worldObj.isRemote&&movingobjectposition.entityHit != this.shootingEntity)
				{
					this.bobber = movingobjectposition.entityHit;
					this.inEntity = true;
				}
				else
				{
					this.xTile = movingobjectposition.blockX;
					this.yTile = movingobjectposition.blockY;
					this.zTile = movingobjectposition.blockZ;
					this.inTile = this.worldObj.getBlockId(this.xTile, this.yTile, this.zTile);
					this.inData = this.worldObj.getBlockMetadata(this.xTile, this.yTile, this.zTile);
					this.motionX = (double)((float)(movingobjectposition.hitVec.xCoord - this.posX));
					this.motionY = (double)((float)(movingobjectposition.hitVec.yCoord - this.posY));
					this.motionZ = (double)((float)(movingobjectposition.hitVec.zCoord - this.posZ));
					f2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
					this.posX -= this.motionX / (double)f2 * 0.08D;
					this.posY -= this.motionY / (double)f2 * 0.08D;
					this.posZ -= this.motionZ / (double)f2 * 0.08D;
					this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
					this.inGround = true;
					this.arrowShake = 7;
					this.setIsCritical(false);

					if (this.inTile != 0)
					{
						Block.blocksList[this.inTile].onEntityCollidedWithBlock(this.worldObj, this.xTile, this.yTile, this.zTile, this);
					}
				}
			}

			if (this.getIsCritical())
			{
				for (l = 0; l < 4; ++l)
				{
					this.worldObj.spawnParticle("crit", this.posX + this.motionX * (double)l / 4.0D, this.posY + this.motionY * (double)l / 4.0D, this.posZ + this.motionZ * (double)l / 4.0D, -this.motionX, -this.motionY + 0.2D, -this.motionZ);
				}
			}

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
			f2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

			for (this.rotationPitch = (float)(Math.atan2(this.motionY, (double)f2) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
			{
				;
			}

			while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
			{
				this.prevRotationPitch += 360.0F;
			}

			while (this.rotationYaw - this.prevRotationYaw < -180.0F)
			{
				this.prevRotationYaw -= 360.0F;
			}

			while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
			{
				this.prevRotationYaw += 360.0F;
			}

			this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
			this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
			float f4 = 0.99F;
			f1 = 0.05F;

			if (this.isInWater())
			{
				for (int j1 = 0; j1 < 4; ++j1)
				{
					f3 = 0.25F;
					this.worldObj.spawnParticle("bubble", this.posX - this.motionX * (double)f3, this.posY - this.motionY * (double)f3, this.posZ - this.motionZ * (double)f3, this.motionX, this.motionY, this.motionZ);
				}
				f4 = 0.8F;
			}

			this.motionX *= (double)f4;
			this.motionZ *= (double)f4;
			this.motionY -= (double)f1;

			this.setPosition(this.posX, this.posY, this.posZ);
			this.doBlockCollisions();
		}
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
	{
		par1NBTTagCompound.setShort("xTile", (short)this.xTile);
		par1NBTTagCompound.setShort("yTile", (short)this.yTile);
		par1NBTTagCompound.setShort("zTile", (short)this.zTile);
		par1NBTTagCompound.setByte("inTile", (byte)this.inTile);
		par1NBTTagCompound.setByte("inData", (byte)this.inData);
		par1NBTTagCompound.setByte("shake", (byte)this.arrowShake);
		par1NBTTagCompound.setByte("inGround", (byte)(this.inGround ? 1 : 0));
		par1NBTTagCompound.setDouble("damage", this.damage);
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		this.xTile = par1NBTTagCompound.getShort("xTile");
		this.yTile = par1NBTTagCompound.getShort("yTile");
		this.zTile = par1NBTTagCompound.getShort("zTile");
		this.inTile = par1NBTTagCompound.getByte("inTile") & 255;
		this.inData = par1NBTTagCompound.getByte("inData") & 255;
		this.arrowShake = par1NBTTagCompound.getByte("shake") & 255;
		this.inGround = par1NBTTagCompound.getByte("inGround") == 1;

		if (par1NBTTagCompound.hasKey("damage"))
		{
			this.damage = par1NBTTagCompound.getDouble("damage");
		}
	}
	
	protected boolean canTriggerWalking()
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	public float getShadowSize()
	{
		return 0.0F;
	}

	public void setDamage(double par1)
	{
		this.damage = par1;
	}

	public double getDamage()
	{
		return this.damage;
	}
	
	public void setKnockbackStrength(int par1)
	{
		this.knockbackStrength = par1;
	}

	public boolean canAttackWithItem()
	{
		return false;
	}

	public void setIsCritical(boolean par1)
	{
		byte b0 = this.dataWatcher.getWatchableObjectByte(16);

		if (par1)
		{
			this.dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 | 1)));
		}
		else
		{
			this.dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 & -2)));
		}
	}

	public boolean getIsCritical()
	{
		byte b0 = this.dataWatcher.getWatchableObjectByte(16);
		return (b0 & 1) != 0;
	}

	public String get名前()
	{
		return this.dataWatcher.getWatchableObjectString(20);
	}

	public void 座標設定()
	{
		x = (int)(this.posX - this.shootingEntity.posX);
		y = (int)(this.posY - this.shootingEntity.posY);
		z = (int)(this.posZ - this.shootingEntity.posZ);

		if(Math.abs(x)>3)
			x = (int) (Math.signum(x));
		else x = 0;
		if(Math.abs(y)>3)
			y = (int) (Math.signum(y));
		else y = 0;
		if(Math.abs(z)>3)
			z = (int) (Math.signum(z));
		else z = 0;
	}

	public boolean isInRangeToRenderDist(double par1)//描画が遠距離でも行われるように
	{
		return true;
	}

	public float[] getxyz()
	{
		float[] xyz = new float[3];
		xyz[0] = x;
		xyz[1] = y;
		xyz[2] = z;
		return xyz;
	}
}

