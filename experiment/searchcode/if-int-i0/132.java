package net.minecraft.block;

import net.canarymod.api.world.blocks.CanaryBlock;
import net.canarymod.hook.world.RedstoneChangeHook;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public abstract class BlockBasePressurePlate extends Block {

    private String a;

    protected BlockBasePressurePlate(String s0, Material material) {
        super(material);
        this.a = s0;
        this.a(CreativeTabs.d);
        this.a(true);
        this.b(this.d(15));
    }

    public void a(IBlockAccess iblockaccess, int i0, int i1, int i2) {
        this.b(iblockaccess.e(i0, i1, i2));
    }

    protected void b(int i0) {
        boolean flag0 = this.c(i0) > 0;
        float f0 = 0.0625F;

        if (flag0) {
            this.a(f0, 0.0F, f0, 1.0F - f0, 0.03125F, 1.0F - f0);
        }
        else {
            this.a(f0, 0.0F, f0, 1.0F - f0, 0.0625F, 1.0F - f0);
        }
    }

    public int a(World world) {
        return 20;
    }

    public AxisAlignedBB a(World world, int i0, int i1, int i2) {
        return null;
    }

    public boolean c() {
        return false;
    }

    public boolean d() {
        return false;
    }

    public boolean b(IBlockAccess iblockaccess, int i0, int i1, int i2) {
        return true;
    }

    public boolean c(World world, int i0, int i1, int i2) {
        return World.a((IBlockAccess) world, i0, i1 - 1, i2) || BlockFence.a(world.a(i0, i1 - 1, i2));
    }

    public void a(World world, int i0, int i1, int i2, Block block) {
        boolean flag0 = false;

        if (!World.a((IBlockAccess) world, i0, i1 - 1, i2) && !BlockFence.a(world.a(i0, i1 - 1, i2))) {
            flag0 = true;
        }

        if (flag0) {
            this.b(world, i0, i1, i2, world.e(i0, i1, i2), 0);
            world.f(i0, i1, i2);
        }
    }

    public void a(World world, int i0, int i1, int i2, Random random) {
        if (!world.E) {
            int i3 = this.c(world.e(i0, i1, i2));

            if (i3 > 0) {
                this.a(world, i0, i1, i2, i3);
            }
        }
    }

    public void a(World world, int i0, int i1, int i2, Entity entity) {
        if (!world.E) {
            int i3 = this.c(world.e(i0, i1, i2));

            if (i3 == 0) {
                this.a(world, i0, i1, i2, i3);
            }
        }
    }

    protected void a(World world, int i0, int i1, int i2, int i3) {
        int i4 = this.e(world, i0, i1, i2);

        // CanaryMod: RedstoneChange
        if (i3 != i4) {
            RedstoneChangeHook hook = (RedstoneChangeHook) new RedstoneChangeHook(world.getCanaryWorld().getBlockAt(i0, i1, i2), i3, i4).call();
            if (hook.isCanceled()) {
                i4 = hook.getOldLevel();
            }
        }
        //

        boolean flag0 = i3 > 0;
        boolean flag1 = i4 > 0;

        if (i3 != i4) {
            world.a(i0, i1, i2, this.d(i4), 2);
            this.a_(world, i0, i1, i2);
            world.c(i0, i1, i2, i0, i1, i2);
        }

        if (!flag1 && flag0) {
            world.a((double) i0 + 0.5D, (double) i1 + 0.1D, (double) i2 + 0.5D, "random.click", 0.3F, 0.5F);
        }
        else if (flag1 && !flag0) {
            world.a((double) i0 + 0.5D, (double) i1 + 0.1D, (double) i2 + 0.5D, "random.click", 0.3F, 0.6F);
        }

        if (flag1) {
            world.a(i0, i1, i2, this, this.a(world));
        }
    }

    protected AxisAlignedBB a(int i0, int i1, int i2) {
        float f0 = 0.125F;

        return AxisAlignedBB.a().a((double) ((float) i0 + f0), (double) i1, (double) ((float) i2 + f0), (double) ((float) (i0 + 1) - f0), (double) i1 + 0.25D, (double) ((float) (i2 + 1) - f0));
    }

    public void a(World world, int i0, int i1, int i2, Block block, int i3) {
        int oldLvl = this.c(i3);
        if (oldLvl > 0) {
            new RedstoneChangeHook(new CanaryBlock((short) Block.b(this), (short) i3, i0, i1, i2, world.getCanaryWorld()), oldLvl, 0).call();
            this.a_(world, i0, i1, i2);
        }

        super.a(world, i0, i1, i2, block, i3);
    }

    protected void a_(World world, int i0, int i1, int i2) {
        world.d(i0, i1, i2, this);
        world.d(i0, i1 - 1, i2, this);
    }

    public int b(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        return this.c(iblockaccess.e(i0, i1, i2));
    }

    public int c(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        return i3 == 1 ? this.c(iblockaccess.e(i0, i1, i2)) : 0;
    }

    public boolean f() {
        return true;
    }

    public void g() {
        float f0 = 0.5F;
        float f1 = 0.125F;
        float f2 = 0.5F;

        this.a(0.5F - f0, 0.5F - f1, 0.5F - f2, 0.5F + f0, 0.5F + f1, 0.5F + f2);
    }

    public int h() {
        return 1;
    }

    protected abstract int e(World world, int i0, int i1, int i2);

    protected abstract int c(int i0);

    protected abstract int d(int i0);
}

