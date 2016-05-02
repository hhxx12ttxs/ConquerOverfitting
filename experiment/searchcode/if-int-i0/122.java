package net.minecraft.block;


import net.canarymod.hook.world.RedstoneChangeHook;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.Random;

public class BlockChest extends BlockContainer {

    private final Random b = new Random();
    public final int a;
    private int oldLvl; // CanaryMod: store old

    protected BlockChest(int i0) {
        super(Material.d);
        this.a = i0;
        this.a(CreativeTabs.c);
        this.a(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
    }

    public boolean c() {
        return false;
    }

    public boolean d() {
        return false;
    }

    public int b() {
        return 22;
    }

    public void a(IBlockAccess iblockaccess, int i0, int i1, int i2) {
        if (iblockaccess.a(i0, i1, i2 - 1) == this) {
            this.a(0.0625F, 0.0F, 0.0F, 0.9375F, 0.875F, 0.9375F);
        }
        else if (iblockaccess.a(i0, i1, i2 + 1) == this) {
            this.a(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 1.0F);
        }
        else if (iblockaccess.a(i0 - 1, i1, i2) == this) {
            this.a(0.0F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
        }
        else if (iblockaccess.a(i0 + 1, i1, i2) == this) {
            this.a(0.0625F, 0.0F, 0.0625F, 1.0F, 0.875F, 0.9375F);
        }
        else {
            this.a(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
        }
    }

    public void b(World world, int i0, int i1, int i2) {
        super.b(world, i0, i1, i2);
        this.e(world, i0, i1, i2);
        Block block = world.a(i0, i1, i2 - 1);
        Block block1 = world.a(i0, i1, i2 + 1);
        Block block2 = world.a(i0 - 1, i1, i2);
        Block block3 = world.a(i0 + 1, i1, i2);

        if (block == this) {
            this.e(world, i0, i1, i2 - 1);
        }

        if (block1 == this) {
            this.e(world, i0, i1, i2 + 1);
        }

        if (block2 == this) {
            this.e(world, i0 - 1, i1, i2);
        }

        if (block3 == this) {
            this.e(world, i0 + 1, i1, i2);
        }
    }

    public void a(World world, int i0, int i1, int i2, EntityLivingBase entitylivingbase, ItemStack itemstack) {
        Block block = world.a(i0, i1, i2 - 1);
        Block block1 = world.a(i0, i1, i2 + 1);
        Block block2 = world.a(i0 - 1, i1, i2);
        Block block3 = world.a(i0 + 1, i1, i2);
        byte b0 = 0;
        int i3 = MathHelper.c((double) (entitylivingbase.z * 4.0F / 360.0F) + 0.5D) & 3;

        if (i3 == 0) {
            b0 = 2;
        }

        if (i3 == 1) {
            b0 = 5;
        }

        if (i3 == 2) {
            b0 = 3;
        }

        if (i3 == 3) {
            b0 = 4;
        }

        if (block != this && block1 != this && block2 != this && block3 != this) {
            world.a(i0, i1, i2, b0, 3);
        }
        else {
            if ((block == this || block1 == this) && (b0 == 4 || b0 == 5)) {
                if (block == this) {
                    world.a(i0, i1, i2 - 1, b0, 3);
                }
                else {
                    world.a(i0, i1, i2 + 1, b0, 3);
                }

                world.a(i0, i1, i2, b0, 3);
            }

            if ((block2 == this || block3 == this) && (b0 == 2 || b0 == 3)) {
                if (block2 == this) {
                    world.a(i0 - 1, i1, i2, b0, 3);
                }
                else {
                    world.a(i0 + 1, i1, i2, b0, 3);
                }

                world.a(i0, i1, i2, b0, 3);
            }
        }

        if (itemstack.u()) {
            ((TileEntityChest) world.o(i0, i1, i2)).a(itemstack.s());
        }
    }

    public void e(World world, int i0, int i1, int i2) {
        if (!world.E) {
            Block block = world.a(i0, i1, i2 - 1);
            Block block1 = world.a(i0, i1, i2 + 1);
            Block block2 = world.a(i0 - 1, i1, i2);
            Block block3 = world.a(i0 + 1, i1, i2);
            boolean flag0 = true;
            int i3;
            Block block4;
            int i4;
            Block block5;
            boolean flag1;
            byte b0;
            int i5;

            if (block != this && block1 != this) {
                if (block2 != this && block3 != this) {
                    b0 = 3;
                    if (block.j() && !block1.j()) {
                        b0 = 3;
                    }

                    if (block1.j() && !block.j()) {
                        b0 = 2;
                    }

                    if (block2.j() && !block3.j()) {
                        b0 = 5;
                    }

                    if (block3.j() && !block2.j()) {
                        b0 = 4;
                    }
                }
                else {
                    i3 = block2 == this ? i0 - 1 : i0 + 1;
                    block4 = world.a(i3, i1, i2 - 1);
                    i4 = block2 == this ? i0 - 1 : i0 + 1;
                    block5 = world.a(i4, i1, i2 + 1);
                    b0 = 3;
                    flag1 = true;
                    if (block2 == this) {
                        i5 = world.e(i0 - 1, i1, i2);
                    }
                    else {
                        i5 = world.e(i0 + 1, i1, i2);
                    }

                    if (i5 == 2) {
                        b0 = 2;
                    }

                    if ((block.j() || block4.j()) && !block1.j() && !block5.j()) {
                        b0 = 3;
                    }

                    if ((block1.j() || block5.j()) && !block.j() && !block4.j()) {
                        b0 = 2;
                    }
                }
            }
            else {
                i3 = block == this ? i2 - 1 : i2 + 1;
                block4 = world.a(i0 - 1, i1, i3);
                i4 = block == this ? i2 - 1 : i2 + 1;
                block5 = world.a(i0 + 1, i1, i4);
                b0 = 5;
                flag1 = true;
                if (block == this) {
                    i5 = world.e(i0, i1, i2 - 1);
                }
                else {
                    i5 = world.e(i0, i1, i2 + 1);
                }

                if (i5 == 4) {
                    b0 = 4;
                }

                if ((block2.j() || block4.j()) && !block3.j() && !block5.j()) {
                    b0 = 5;
                }

                if ((block3.j() || block5.j()) && !block2.j() && !block4.j()) {
                    b0 = 4;
                }
            }

            world.a(i0, i1, i2, b0, 3);
        }
    }

    public boolean c(World world, int i0, int i1, int i2) {
        int i3 = 0;

        if (world.a(i0 - 1, i1, i2) == this) {
            ++i3;
        }

        if (world.a(i0 + 1, i1, i2) == this) {
            ++i3;
        }

        if (world.a(i0, i1, i2 - 1) == this) {
            ++i3;
        }

        if (world.a(i0, i1, i2 + 1) == this) {
            ++i3;
        }

        return i3 > 1 ? false : (this.n(world, i0 - 1, i1, i2) ? false : (this.n(world, i0 + 1, i1, i2) ? false : (this.n(world, i0, i1, i2 - 1) ? false : !this.n(world, i0, i1, i2 + 1))));
    }

    private boolean n(World world, int i0, int i1, int i2) {
        return world.a(i0, i1, i2) != this ? false : (world.a(i0 - 1, i1, i2) == this ? true : (world.a(i0 + 1, i1, i2) == this ? true : (world.a(i0, i1, i2 - 1) == this ? true : world.a(i0, i1, i2 + 1) == this)));
    }

    public void a(World world, int i0, int i1, int i2, Block block) {
        super.a(world, i0, i1, i2, block);
        TileEntityChest tileentitychest = (TileEntityChest) world.o(i0, i1, i2);

        if (tileentitychest != null) {
            tileentitychest.u();
        }
    }

    public void a(World world, int i0, int i1, int i2, Block block, int i3) {
        TileEntityChest tileentitychest = (TileEntityChest) world.o(i0, i1, i2);

        if (tileentitychest != null) {
            for (int i4 = 0; i4 < tileentitychest.a(); ++i4) {
                ItemStack itemstack = tileentitychest.a(i4);

                if (itemstack != null) {
                    float f0 = this.b.nextFloat() * 0.8F + 0.1F;
                    float f1 = this.b.nextFloat() * 0.8F + 0.1F;

                    EntityItem entityitem;

                    for (float f2 = this.b.nextFloat() * 0.8F + 0.1F; itemstack.b > 0; world.d((Entity) entityitem)) {
                        int i5 = this.b.nextInt(21) + 10;

                        if (i5 > itemstack.b) {
                            i5 = itemstack.b;
                        }

                        itemstack.b -= i5;
                        entityitem = new EntityItem(world, (double) ((float) i0 + f0), (double) ((float) i1 + f1), (double) ((float) i2 + f2), new ItemStack(itemstack.b(), i5, itemstack.k()));
                        float f3 = 0.05F;

                        entityitem.w = (double) ((float) this.b.nextGaussian() * f3);
                        entityitem.x = (double) ((float) this.b.nextGaussian() * f3 + 0.2F);
                        entityitem.y = (double) ((float) this.b.nextGaussian() * f3);
                        if (itemstack.p()) {
                            entityitem.f().d((NBTTagCompound) itemstack.q().b());
                        }
                    }
                }
            }

            world.f(i0, i1, i2, block);
        }

        super.a(world, i0, i1, i2, block, i3);
    }

    public boolean a(World world, int i0, int i1, int i2, EntityPlayer entityplayer, int i3, float f0, float f1, float f2) {
        if (world.E) {
            return true;
        }
        else {
            IInventory iinventory = this.m(world, i0, i1, i2);

            if (iinventory != null) {
                entityplayer.a(iinventory);
            }

            return true;
        }
    }

    public IInventory m(World world, int i0, int i1, int i2) {
        Object object = (TileEntityChest) world.o(i0, i1, i2);

        if (object == null) {
            return null;
        }
        else if (world.a(i0, i1 + 1, i2).r()) {
            return null;
        }
        else if (o(world, i0, i1, i2)) {
            return null;
        }
        else if (world.a(i0 - 1, i1, i2) == this && (world.a(i0 - 1, i1 + 1, i2).r() || o(world, i0 - 1, i1, i2))) {
            return null;
        }
        else if (world.a(i0 + 1, i1, i2) == this && (world.a(i0 + 1, i1 + 1, i2).r() || o(world, i0 + 1, i1, i2))) {
            return null;
        }
        else if (world.a(i0, i1, i2 - 1) == this && (world.a(i0, i1 + 1, i2 - 1).r() || o(world, i0, i1, i2 - 1))) {
            return null;
        }
        else if (world.a(i0, i1, i2 + 1) == this && (world.a(i0, i1 + 1, i2 + 1).r() || o(world, i0, i1, i2 + 1))) {
            return null;
        }
        else {
            if (world.a(i0 - 1, i1, i2) == this) {
                object = new InventoryLargeChest("container.chestDouble", (TileEntityChest) world.o(i0 - 1, i1, i2), (IInventory) object);
            }

            if (world.a(i0 + 1, i1, i2) == this) {
                object = new InventoryLargeChest("container.chestDouble", (IInventory) object, (TileEntityChest) world.o(i0 + 1, i1, i2));
            }

            if (world.a(i0, i1, i2 - 1) == this) {
                object = new InventoryLargeChest("container.chestDouble", (TileEntityChest) world.o(i0, i1, i2 - 1), (IInventory) object);
            }

            if (world.a(i0, i1, i2 + 1) == this) {
                object = new InventoryLargeChest("container.chestDouble", (IInventory) object, (TileEntityChest) world.o(i0, i1, i2 + 1));
            }

            return (IInventory) object;
        }
    }

    public TileEntity a(World world, int i0) {
        TileEntityChest tileentitychest = new TileEntityChest();

        return tileentitychest;
    }

    public boolean f() {
        return this.a == 1;
    }

    public int b(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        if (!this.f()) {
            return 0;
        }
        else {
            int i4 = ((TileEntityChest) iblockaccess.o(i0, i1, i2)).o;
            // CanaryMod: RedstoneChange
            int newLvl = MathHelper.a(i4, 0, 15);
            if (newLvl != oldLvl) {
                RedstoneChangeHook hook = (RedstoneChangeHook) new RedstoneChangeHook(((World) iblockaccess).getCanaryWorld().getBlockAt(i0, i1, i2), oldLvl, newLvl).call();
                if (hook.isCanceled()) {
                    return oldLvl;
                }
            }
            return oldLvl = newLvl;
            //
        }
    }

    public int c(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        return i3 == 1 ? this.b(iblockaccess, i0, i1, i2, i3) : 0;
    }

    private static boolean o(World world, int i0, int i1, int i2) {
        Iterator iterator = world.a(EntityOcelot.class, AxisAlignedBB.a().a((double) i0, (double) (i1 + 1), (double) i2, (double) (i0 + 1), (double) (i1 + 2), (double) (i2 + 1))).iterator();

        EntityOcelot entityocelot;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            EntityOcelot entityocelot1 = (EntityOcelot) iterator.next();

            entityocelot = (EntityOcelot) entityocelot1;
        } while (!entityocelot.bY());

        return true;
    }

    public boolean M() {
        return true;
    }

    public int g(World world, int i0, int i1, int i2, int i3) {
        return Container.b(this.m(world, i0, i1, i2));
    }
}

