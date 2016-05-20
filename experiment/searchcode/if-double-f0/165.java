package net.minecraft.block;

import net.canarymod.api.world.blocks.CanaryJukebox;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockJukebox extends BlockContainer {

    protected BlockJukebox() {
        super(Material.d);
        this.a(CreativeTabs.c);
    }

    public boolean a(World world, int i0, int i1, int i2, EntityPlayer entityplayer, int i3, float f0, float f1, float f2) {
        if (world.e(i0, i1, i2) == 0) {
            return false;
        }
        else {
            this.e(world, i0, i1, i2);
            return true;
        }
    }

    public void b(World world, int i0, int i1, int i2, ItemStack itemstack) {
        if (!world.E) {
            TileEntityJukebox blockjukebox_tileentityjukebox = (TileEntityJukebox) world.o(i0, i1, i2);

            if (blockjukebox_tileentityjukebox != null) {
                blockjukebox_tileentityjukebox.a(itemstack.m());
                world.a(i0, i1, i2, 1, 2);
            }
        }
    }

    public void e(World world, int i0, int i1, int i2) {
        if (!world.E) {
            TileEntityJukebox blockjukebox_tileentityjukebox = (TileEntityJukebox) world.o(i0, i1, i2);

            if (blockjukebox_tileentityjukebox != null) {
                ItemStack itemstack = blockjukebox_tileentityjukebox.a();

                if (itemstack != null) {
                    world.c(1005, i0, i1, i2, 0);
                    world.a((String) null, i0, i1, i2);
                    blockjukebox_tileentityjukebox.a((ItemStack) null);
                    world.a(i0, i1, i2, 0, 2);
                    float f0 = 0.7F;
                    double d0 = (double) (world.s.nextFloat() * f0) + (double) (1.0F - f0) * 0.5D;
                    double d1 = (double) (world.s.nextFloat() * f0) + (double) (1.0F - f0) * 0.2D + 0.6D;
                    double d2 = (double) (world.s.nextFloat() * f0) + (double) (1.0F - f0) * 0.5D;
                    ItemStack itemstack1 = itemstack.m();
                    EntityItem entityitem = new EntityItem(world, (double) i0 + d0, (double) i1 + d1, (double) i2 + d2, itemstack1);

                    entityitem.b = 10;
                    world.d((Entity) entityitem);
                }
            }
        }
    }

    public void a(World world, int i0, int i1, int i2, Block block, int i3) {
        this.e(world, i0, i1, i2);
        super.a(world, i0, i1, i2, block, i3);
    }

    public void a(World world, int i0, int i1, int i2, int i3, float f0, int i4) {
        if (!world.E) {
            super.a(world, i0, i1, i2, i3, f0, 0);
        }
    }

    public TileEntity a(World world, int i0) {
        return new TileEntityJukebox();
    }

    public boolean M() {
        return true;
    }

    public int g(World world, int i0, int i1, int i2, int i3) {
        ItemStack itemstack = ((TileEntityJukebox) world.o(i0, i1, i2)).a();

        return itemstack == null ? 0 : Item.b(itemstack.b()) + 1 - Item.b(Items.cd);
    }

    public static class TileEntityJukebox extends TileEntity {

        private ItemStack a;

        public TileEntityJukebox() {
            this.complexBlock = new CanaryJukebox(this); // CanaryMod: wrap tile entity
        }

        public void a(NBTTagCompound itemstack) {
            super.a(itemstack);
            if (itemstack.b("RecordItem", 10)) {
                this.a(ItemStack.a(itemstack.m("RecordItem")));
            }
            else if (itemstack.f("Record") > 0) {
                this.a(new ItemStack(Item.d(itemstack.f("Record")), 1, 0));
            }

        }

        public void b(NBTTagCompound itemstack) {
            super.b(itemstack);
            if (this.a() != null) {
                itemstack.a("RecordItem", (NBTBase) this.a().b(new NBTTagCompound()));
                itemstack.a("Record", Item.b(this.a().b()));
            }

        }

        public ItemStack a() {
            return this.a;
        }

        public void a(ItemStack itemstack) {
            this.a = itemstack;
            this.e();
        }

        // CanaryMod
        public CanaryJukebox getCanaryJukebox() {
            return (CanaryJukebox) complexBlock;
        }
    }
}

