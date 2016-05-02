package net.minecraft.block;

import net.canarymod.hook.world.RedstoneChangeHook;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class BlockRailDetector extends BlockRailBase {

    public BlockRailDetector() {
        super(true);
        this.a(true);
    }

    public int a(World world) {
        return 20;
    }

    public boolean f() {
        return true;
    }

    public void a(World world, int i0, int i1, int i2, Entity entity) {
        if (!world.E) {
            int i3 = world.e(i0, i1, i2);

            if ((i3 & 8) == 0) {
                this.a(world, i0, i1, i2, i3);
            }
        }
    }

    public void a(World world, int i0, int i1, int i2, Random random) {
        if (!world.E) {
            int i3 = world.e(i0, i1, i2);

            if ((i3 & 8) != 0) {
                this.a(world, i0, i1, i2, i3);
            }
        }
    }

    public int b(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        return (iblockaccess.e(i0, i1, i2) & 8) != 0 ? 15 : 0;
    }

    public int c(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        return (iblockaccess.e(i0, i1, i2) & 8) == 0 ? 0 : (i3 == 1 ? 15 : 0);
    }

    private void a(World world, int i0, int i1, int i2, int i3) {
        boolean flag0 = (i3 & 8) != 0;
        boolean flag1 = false;
        float f0 = 0.125F;
        List list = world.a(EntityMinecart.class, AxisAlignedBB.a().a((double) ((float) i0 + f0), (double) i1, (double) ((float) i2 + f0), (double) ((float) (i0 + 1) - f0), (double) ((float) (i1 + 1) - f0), (double) ((float) (i2 + 1) - f0)));

        if (!list.isEmpty()) {
            flag1 = true;
        }

        if (flag1 && !flag0) {
            // CanaryMod: RedstoneChange; Rails on
            RedstoneChangeHook hook = (RedstoneChangeHook) new RedstoneChangeHook(world.getCanaryWorld().getBlockAt(i0, i1, i2), 0, 15).call();
            if (hook.isCanceled()) {
                return;
            }
            //
            world.a(i0, i1, i2, i3 | 8, 3);
            world.d(i0, i1, i2, this);
            world.d(i0, i1 - 1, i2, this);
            world.c(i0, i1, i2, i0, i1, i2);
        }

        if (!flag1 && flag0) {
            // CanaryMod: RedstoneChange; Rails off
            RedstoneChangeHook hook = (RedstoneChangeHook) new RedstoneChangeHook(world.getCanaryWorld().getBlockAt(i0, i1, i2), 15, 0).call();
            if (hook.isCanceled()) {
                return;
            }
            //
            world.a(i0, i1, i2, i3 & 7, 3);
            world.d(i0, i1, i2, this);
            world.d(i0, i1 - 1, i2, this);
            world.c(i0, i1, i2, i0, i1, i2);
        }

        if (flag1) {
            world.a(i0, i1, i2, this, this.a(world));
        }

        world.f(i0, i1, i2, this);
    }

    public void b(World world, int i0, int i1, int i2) {
        super.b(world, i0, i1, i2);
        this.a(world, i0, i1, i2, world.e(i0, i1, i2));
    }

    public boolean M() {
        return true;
    }

    public int g(World world, int i0, int i1, int i2, int i3) {
        if ((world.e(i0, i1, i2) & 8) > 0) {
            float f0 = 0.125F;
            List list = world.a(EntityMinecartCommandBlock.class, AxisAlignedBB.a().a((double) ((float) i0 + f0), (double) i1, (double) ((float) i2 + f0), (double) ((float) (i0 + 1) - f0), (double) ((float) (i1 + 1) - f0), (double) ((float) (i2 + 1) - f0)));

            if (list.size() > 0) {
                return ((EntityMinecartCommandBlock) list.get(0)).e().g();
            }

            List list1 = world.a(EntityMinecart.class, AxisAlignedBB.a().a((double) ((float) i0 + f0), (double) i1, (double) ((float) i2 + f0), (double) ((float) (i0 + 1) - f0), (double) ((float) (i1 + 1) - f0), (double) ((float) (i2 + 1) - f0)), IEntitySelector.b);

            if (list1.size() > 0) {
                return Container.b((IInventory) list1.get(0));
            }
        }

        return 0;
    }
}

