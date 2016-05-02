package net.gloombone.aeterno.item;

import net.gloombone.aeterno.init.ModBlocks;
import net.gloombone.aeterno.utility.LogHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemPortalKey extends ItemAeterno {

    /**
     * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
     * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
     */
    public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float x2, float y2, float z2) {
		if (!world.isRemote) {
			if (world.getBlock(x, y, z).equals(ModBlocks.blockTownPortal) && side==1) {
				world.setBlock(x, y+1, z, ModBlocks.blockTownPortal);
			}
            LogHelper.info("Side: " + side);
            LogHelper.info("Locked");
		}

		if (!world.isRemote) {
			for (int x3 = -5; x3 < 6; x3++) {
				for (int z3 = -5; z3 < 6; z3++) {
					for (int i = 4; i < 17; i++) {
						world.setBlock(x+x3, y+i, z+z3, Blocks.air);	
					}
				}
			}
		}
        return false;
    }
}

