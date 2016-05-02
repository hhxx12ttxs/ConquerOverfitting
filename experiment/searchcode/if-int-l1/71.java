package net.minecraft.ssMineShipMOD;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Vec3Pool;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;

public class mineshipWorld extends WorldServer
{
	WorldServer データ用ワールド;

	public mineshipWorld(WorldServer データ用ワールド) {
		super(
				MinecraftServer.getServer(), データ用ワールド.
				getSaveHandler(),
				データ用ワールド.getWorldInfo().getWorldName(),
				データ用ワールド.provider.dimensionId,
				new WorldSettings(
						データ用ワールド.getWorldInfo()
						),
						データ用ワールド.theProfiler,
						データ用ワールド.getWorldLogAgent()
				);
		this.データ用ワールド = データ用ワールド;
	}

	public void func_96440_m(int par1, int par2, int par3, int par4)
	{
		for (int i1 = 0; i1 < 4; ++i1)
		{
			int j1 = par1 + Direction.offsetX[i1];
			int k1 = par3 + Direction.offsetZ[i1];
			int l1 = this.getBlockId(j1, par2, k1);

			if (l1 != 0)
			{
				Block block = Block.blocksList[l1];

				if (Block.redstoneComparatorIdle.func_94487_f(l1))
				{
					if(!ssMineShipMOD.インスタンス.ブロックを更新しない)
						block.onNeighborBlockChange(this, j1, par2, k1, par4);
				}
				else if (Block.isNormalCube(l1))
				{
					j1 += Direction.offsetX[i1];
					k1 += Direction.offsetZ[i1];
					l1 = this.getBlockId(j1, par2, k1);
					block = Block.blocksList[l1];

					if (Block.redstoneComparatorIdle.func_94487_f(l1))
					{
						if(!ssMineShipMOD.インスタンス.ブロックを更新しない)
							block.onNeighborBlockChange(this, j1, par2, k1, par4);
					}
				}
			}
		}
	}

	public TileEntity getBlockTileEntity(int par1, int par2, int par3)
	{
		if(ssMineShipMOD.インスタンス.ヌルを返す)
		{
			return null;
		}
		else return データ用ワールド.getBlockTileEntity(par1, par2, par3);
	}

	public void notifyBlockOfNeighborChange(int par1, int par2, int par3, int par4)
	{
		if (!this.isRemote)
		{
			int i1 = this.getBlockId(par1, par2, par3);
			Block block = Block.blocksList[i1];

			if (block != null)
			{
				try
				{
					ssMineShipMOD.インスタンス.カレントcolエンティティ = ssMineShipMOD.インスタンス.座標toブロックデータ.get(new posXYZ(par1,par2,par3));
					block.onNeighborBlockChange(this, par1, par2, par3, par4);
				}
				catch (Throwable throwable)
				{
					throwable.printStackTrace();
				}
			}
		}
	}

	public void scheduleBlockUpdate(int par1, int par2, int par3, int par4, int par5)
	{
		ssMineShipMOD.インスタンス.カレントcolエンティティ = ssMineShipMOD.インスタンス.座標toブロックデータ.get(new posXYZ(par1,par2,par3));
		this.func_82740_a(par1, par2, par3, par4, par5, 0);
	}

	public boolean blockExists(int par1, int par2, int par3)
	{
		ssMineShipMOD.インスタンス.カレントcolエンティティ = ssMineShipMOD.インスタンス.座標toブロックデータ.get(new posXYZ(par1,par2,par3));
		return par2 >= 0 && par2 < 256 ? this.chunkExists(par1 >> 4, par3 >> 4) : false;
	}

	@Override
	public boolean spawnEntityInWorld(Entity par1Entity)
	{
		if(ssMineShipMOD.インスタンス.カレントcolエンティティ != null)
		{
			serverDataBlock c = ssMineShipMOD.インスタンス.カレントcolエンティティ;

			double cosx = Math.cos((double)(c.メイン.rotationYaw + c.mainとの角度) * Math.PI / 180.0D)*c.mainとの距離;
			double var3 = Math.sin((double)(c.メイン.rotationYaw + c.mainとの角度) * Math.PI / 180.0D)*c.mainとの距離;
			par1Entity.setPosition(c.メイン.posX + cosx, c.メイン.posY + c.mainとの相対座標Y, c.メイン.posZ + var3);

			par1Entity.worldObj = MinecraftServer.getServer().worldServerForDimension(ssMineShipMOD.インスタンス.カレントエンティティ.worldObj.provider.dimensionId);
			return MinecraftServer.getServer().worldServerForDimension(ssMineShipMOD.インスタンス.カレントcolエンティティ.メイン.worldObj.provider.dimensionId).spawnEntityInWorld(par1Entity);
		}
		return false;
	}

	public boolean setBlock(int par1, int par2, int par3, int par4, int par5, int par6)
	{
		return データ用ワールド.setBlock(par1, par2, par3, par4,par5,par6);
	}

	public boolean setBlockMetadataWithNotify(int par1, int par2, int par3, int par4, int par5)
	{
		return データ用ワールド.setBlockMetadataWithNotify(par1, par2, par3, par4, par5);
	}

	public void setBlockTileEntity(int par1, int par2, int par3, TileEntity par4TileEntity)
	{
		データ用ワールド.setBlockTileEntity(par1, par2, par3, par4TileEntity);
	}

	public void markBlockForUpdate(int par1, int par2, int par3)
	{
		データ用ワールド.markBlockForUpdate(par1, par2, par3);
	}

	@Override
	public int getBlockId(int i, int j, int k) {
		if(データ用ワールド != null)
			return データ用ワールド.getBlockId(i,j,k);
		return 0;
	}

	@Override
	public int getBlockMetadata(int i, int j, int k) {
		return データ用ワールド.getBlockMetadata(i,j,k);
	}

	@Override
	public Material getBlockMaterial(int i, int j, int k) {
		return データ用ワールド.getBlockMaterial(i,j,k);
	}

	@Override
	public boolean isBlockNormalCube(int i, int j, int k) {
		return データ用ワールド.isBlockNormalCube(i,j,k);
	}

	@Override
	public Vec3Pool getWorldVec3Pool() {
		return データ用ワールド.getWorldVec3Pool();
	}

	@Override
	public int isBlockProvidingPowerTo(int i, int j, int k, int l) {
		return データ用ワールド.isBlockProvidingPowerTo(i,j,k,l);
	}
}
