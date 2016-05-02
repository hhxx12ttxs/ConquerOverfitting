package net.minecraft.block;

import net.canarymod.hook.world.DispenseHook;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.IRegistry;
import net.minecraft.dispenser.PositionImpl;
import net.minecraft.dispenser.RegistryDefaulted;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.Random;

public class BlockDispenser extends BlockContainer {

    public static final IRegistry a = new RegistryDefaulted(new BehaviorDefaultDispenseItem());
    protected Random b = new Random();

    protected BlockDispenser() {
        super(Material.e);
        this.a(CreativeTabs.d);
    }

    public int a(World world) {
        return 4;
    }

    public void b(World world, int i0, int i1, int i2) {
        super.b(world, i0, i1, i2);
        this.m(world, i0, i1, i2);
    }

    private void m(World world, int i0, int i1, int i2) {
        if (!world.E) {
            Block block = world.a(i0, i1, i2 - 1);
            Block block1 = world.a(i0, i1, i2 + 1);
            Block block2 = world.a(i0 - 1, i1, i2);
            Block block3 = world.a(i0 + 1, i1, i2);
            byte b0 = 3;

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

            world.a(i0, i1, i2, b0, 2);
        }
    }

    public boolean a(World world, int i0, int i1, int i2, EntityPlayer entityplayer, int i3, float f0, float f1, float f2) {
        if (world.E) {
            return true;
        }
        else {
            TileEntityDispenser tileentitydispenser = (TileEntityDispenser) world.o(i0, i1, i2);

            if (tileentitydispenser != null) {
                entityplayer.a(tileentitydispenser);
            }

            return true;
        }
    }

    protected void e(World world, int i0, int i1, int i2) {
        BlockSourceImpl blocksourceimpl = new BlockSourceImpl(world, i0, i1, i2);
        TileEntityDispenser tileentitydispenser = (TileEntityDispenser) blocksourceimpl.j();

        if (tileentitydispenser != null) {
            int i3 = tileentitydispenser.i();

            if (i3 < 0) {
                // CanaryMod: Dispense Smoke
                DispenseHook hook = (DispenseHook) new DispenseHook(tileentitydispenser.getCanaryDispenser(), null).call();
                if (!hook.isCanceled()) {
                    world.c(1001, i0, i1, i2, 0);
                }
                //
            }
            else {
                ItemStack itemstack = tileentitydispenser.a(i3);
                IBehaviorDispenseItem ibehaviordispenseitem = this.a(itemstack);

                if (ibehaviordispenseitem != IBehaviorDispenseItem.a) {
                    ItemStack itemstack1 = ibehaviordispenseitem.a(blocksourceimpl, itemstack);

                    tileentitydispenser.a(i3, itemstack1.b == 0 ? null : itemstack1);
                }
            }
        }
    }

    public IBehaviorDispenseItem a(ItemStack itemstack) { // CanaryMod: protected => public
        return (IBehaviorDispenseItem) a.a(itemstack.b());
    }

    public void a(World world, int i0, int i1, int i2, Block block) {
        boolean flag0 = world.v(i0, i1, i2) || world.v(i0, i1 + 1, i2);
        int i3 = world.e(i0, i1, i2);
        boolean flag1 = (i3 & 8) != 0;

        if (flag0 && !flag1) {
            world.a(i0, i1, i2, this, this.a(world));
            world.a(i0, i1, i2, i3 | 8, 4);
        }
        else if (!flag0 && flag1) {
            world.a(i0, i1, i2, i3 & -9, 4);
        }
    }

    public void a(World world, int i0, int i1, int i2, Random random) {
        if (!world.E) {
            this.e(world, i0, i1, i2);
        }
    }

    public TileEntity a(World world, int i0) {
        return new TileEntityDispenser();
    }

    public void a(World world, int i0, int i1, int i2, EntityLivingBase entitylivingbase, ItemStack itemstack) {
        int i3 = BlockPistonBase.a(world, i0, i1, i2, entitylivingbase);

        world.a(i0, i1, i2, i3, 2);
        if (itemstack.u()) {
            ((TileEntityDispenser) world.o(i0, i1, i2)).a(itemstack.s());
        }
    }

    public void a(World world, int i0, int i1, int i2, Block block, int i3) {
        TileEntityDispenser tileentitydispenser = (TileEntityDispenser) world.o(i0, i1, i2);

        if (tileentitydispenser != null) {
            for (int i4 = 0; i4 < tileentitydispenser.a(); ++i4) {
                ItemStack itemstack = tileentitydispenser.a(i4);

                if (itemstack != null) {
                    float f0 = this.b.nextFloat() * 0.8F + 0.1F;
                    float f1 = this.b.nextFloat() * 0.8F + 0.1F;
                    float f2 = this.b.nextFloat() * 0.8F + 0.1F;

                    while (itemstack.b > 0) {
                        int i5 = this.b.nextInt(21) + 10;

                        if (i5 > itemstack.b) {
                            i5 = itemstack.b;
                        }

                        itemstack.b -= i5;
                        EntityItem entityitem = new EntityItem(world, (double) ((float) i0 + f0), (double) ((float) i1 + f1), (double) ((float) i2 + f2), new ItemStack(itemstack.b(), i5, itemstack.k()));

                        if (itemstack.p()) {
                            entityitem.f().d((NBTTagCompound) itemstack.q().b());
                        }

                        float f3 = 0.05F;

                        entityitem.w = (double) ((float) this.b.nextGaussian() * f3);
                        entityitem.x = (double) ((float) this.b.nextGaussian() * f3 + 0.2F);
                        entityitem.y = (double) ((float) this.b.nextGaussian() * f3);
                        world.d((Entity) entityitem);
                    }
                }
            }

            world.f(i0, i1, i2, block);
        }

        super.a(world, i0, i1, i2, block, i3);
    }

    public static IPosition a(IBlockSource iblocksource) {
        EnumFacing enumfacing = b(iblocksource.h());
        double d0 = iblocksource.a() + 0.7D * (double) enumfacing.c();
        double d1 = iblocksource.b() + 0.7D * (double) enumfacing.d();
        double d2 = iblocksource.c() + 0.7D * (double) enumfacing.e();

        return new PositionImpl(d0, d1, d2);
    }

    public static EnumFacing b(int i0) {
        return EnumFacing.a(i0 & 7);
    }

    public boolean M() {
        return true;
    }

    public int g(World world, int i0, int i1, int i2, int i3) {
        return Container.b((IInventory) world.o(i0, i1, i2));
    }
}

