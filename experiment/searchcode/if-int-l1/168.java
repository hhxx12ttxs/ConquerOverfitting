package bcut.logic;

import java.util.Random;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneLogic;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockLogicCircut extends BlockRedstoneLogic {

	private int blockId;
	private boolean isOn;
	
	protected BlockLogicCircut(int par1, boolean par2) {
		super(par1, par2);

		setUnlocalizedName("Inverter");

		this.blockId = par1;
		this.isOn = par2;
		
		if(!isOn)
			setCreativeTab(UTLogicMain.UsefulThingsLogicTab);
		
	}

	
	@Override
	protected int func_94481_j_(int i) {
		// TODO Auto-generated method stub
		return 1;
	}
	
	
	
	
	@Override
	 public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLiving par5EntityLiving, ItemStack par6ItemStack)
	 {
		 
		 
		 int l = ((MathHelper.floor_double((double)(par5EntityLiving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3) + 2) % 4;
	     par1World.setBlockMetadataWithNotify(par2, par3, par4, l, 3);
	     boolean flag = this.func_94478_d(par1World, par2, par3, par4, l);

	 
		 
	        if(isOn) 
	        { 
	        	par1World.scheduleBlockUpdate(par2, par3, par4, blockId, 1); 
	        	par1World.scheduleBlockUpdate(par2, par3, par4, blockId - 1, 1);
	        }
	        else
	        {
	        	par1World.scheduleBlockUpdate(par2, par3, par4, blockId, 1); 
	        	par1World.scheduleBlockUpdate(par2, par3, par4, blockId + 1, 1);
	        }
	        	
	
	 }

	
	@Override
	protected BlockRedstoneLogic func_94485_e() {
		// TODO Auto-generated method stub
		return UTLogicMain.circutoff;
	}

	
	@Override
	protected BlockRedstoneLogic func_94484_i() {
		// TODO Auto-generated method stub
		return UTLogicMain.circut;
	}


	@Override
    public int idDropped(int par1, Random par2Random, int par3)
    {
		if(isOn)
			return blockId -1;
		else
			return blockId;
    }


    @SideOnly(Side.CLIENT)

    /**
     * only called by clickMiddleMouseButton , and passed to inventory.setCurrentItem (along with isCreative)
     */
    @Override
    public int idPicked(World par1World, int par2, int par3, int par4)
    {
    	if(isOn)
			return blockId -1;
		else
			return blockId;
    }

    
    /**
     * Returns the signal strength at one input of the block. Args: world, X, Y, Z, side
     */
    protected int getInputStrength(World par1World, int par2, int par3, int par4, int par5)
    {
        int i1 = super.getInputStrength(par1World, par2, par3, par4, par5);
        int j1 = getDirection(par5);
        int k1 = par2 + Direction.offsetX[j1];
        int l1 = par4 + Direction.offsetZ[j1];
        int i2 = par1World.getBlockId(k1, par3, l1);

        if (i2 > 0)
        {
            if (Block.blocksList[i2].hasComparatorInputOverride())
            {
                i1 = Block.blocksList[i2].getComparatorInputOverride(par1World, k1, par3, l1, Direction.rotateOpposite[j1]);
            }
            else if (i1 < 15 && Block.isNormalCube(i2))
            {
                k1 += Direction.offsetX[j1];
                l1 += Direction.offsetZ[j1];
                i2 = par1World.getBlockId(k1, par3, l1);

                if (i2 > 0 && Block.blocksList[i2].hasComparatorInputOverride())
                {
                    i1 = Block.blocksList[i2].getComparatorInputOverride(par1World, k1, par3, l1, Direction.rotateOpposite[j1]);
                }
            }
        }

        return i1;
    }
   
	
	@Override
	public int isProvidingWeakPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
	{
		if(!isRepeaterPowered)
		{			
			return 15;
		}
		else
		{
			return 0;
		}
		
	}
	
	
	@Override
	 public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random)
	 {
		
		 
		 System.out.println("UpdateTick");
		 
		 int l = par1World.getBlockMetadata(par2, par3, par4);
		 
		 if (!this.func_94476_e(par1World, par2, par3, par4, l))
	        {
		 
	            boolean flag = this.func_94478_d(par1World, par2, par3, par4, l);
	            par1World.func_82740_a(par2, par3, par4, this.func_94485_e().blockID, this.func_94486_g(l), -1);

	            if (flag)
	            {
	                par1World.setBlock(par2, par3, par4, this.func_94484_i().blockID, l, 2); //:TODO SET SIGNAL OFF
	                System.out.println("if (this.isRepeaterPowered && !flag)");
	               
	                
	            }
	            else 
	            {
	                par1World.setBlock(par2, par3, par4, this.func_94485_e().blockID, l, 2); //:TODO SET SIGNAL - YAY
	                System.out.println(" else if (!this.isRepeaterPowered)");
	               
	            }
	        
	        }
		 
	 }

}

