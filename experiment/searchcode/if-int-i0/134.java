package net.minecraft.block;

import net.canarymod.hook.world.RedstoneChangeHook;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public abstract class BlockRedstoneDiode extends BlockDirectional {

    protected final boolean a;

    protected BlockRedstoneDiode(boolean flag0) {
        super(Material.q);
        this.a = flag0;
        this.a(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
    }

    public boolean d() {
        return false;
    }

    public boolean c(World world, int i0, int i1, int i2) {
        return !World.a((IBlockAccess) world, i0, i1 - 1, i2) ? false : super.c(world, i0, i1, i2);
    }

    public boolean j(World world, int i0, int i1, int i2) {
        return !World.a((IBlockAccess) world, i0, i1 - 1, i2) ? false : super.j(world, i0, i1, i2);
    }

    public void a(World world, int i0, int i1, int i2, Random random) {
        int i3 = world.e(i0, i1, i2);

        if (!this.g((IBlockAccess) world, i0, i1, i2, i3)) {
            boolean flag0 = this.a(world, i0, i1, i2, i3);

            if (this.a && !flag0) {
                // CanaryMod: RedstoneChange; turning off
                RedstoneChangeHook hook = (RedstoneChangeHook) new RedstoneChangeHook(world.getCanaryWorld().getBlockAt(i0, i1, i2), 15, 0).call();
                if (hook.isCanceled()) {
                    return;
                }
                //
                world.d(i0, i1, i2, this.i(), i3, 2);
            }
            else if (!this.a) {
                // CanaryMod: RedstoneChange; turning on
                RedstoneChangeHook hook = (RedstoneChangeHook) new RedstoneChangeHook(world.getCanaryWorld().getBlockAt(i0, i1, i2), 0, 15).call();
                if (hook.isCanceled()) {
                    return;
                }
                //
                world.d(i0, i1, i2, this.e(), i3, 2);
                if (!flag0) {
                    world.a(i0, i1, i2, this.e(), this.k(i3), -1);
                }
            }
        }
    }

    public int b() {
        return 36;
    }

    protected boolean c(int i0) {
        return this.a;
    }

    public int c(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        return this.b(iblockaccess, i0, i1, i2, i3);
    }

    public int b(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        int i4 = iblockaccess.e(i0, i1, i2);

        if (!this.c(i4)) {
            return 0;
        }
        else {
            int i5 = l(i4);

            return i5 == 0 && i3 == 3 ? this.f(iblockaccess, i0, i1, i2, i4) : (i5 == 1 && i3 == 4 ? this.f(iblockaccess, i0, i1, i2, i4) : (i5 == 2 && i3 == 2 ? this.f(iblockaccess, i0, i1, i2, i4) : (i5 == 3 && i3 == 5 ? this.f(iblockaccess, i0, i1, i2, i4) : 0)));
        }
    }

    public void a(World world, int i0, int i1, int i2, Block block) {
        if (!this.j(world, i0, i1, i2)) {
            this.b(world, i0, i1, i2, world.e(i0, i1, i2), 0);
            world.f(i0, i1, i2);
            world.d(i0 + 1, i1, i2, this);
            world.d(i0 - 1, i1, i2, this);
            world.d(i0, i1, i2 + 1, this);
            world.d(i0, i1, i2 - 1, this);
            world.d(i0, i1 - 1, i2, this);
            world.d(i0, i1 + 1, i2, this);
        }
        else {
            this.b(world, i0, i1, i2, block);
        }
    }

    protected void b(World world, int i0, int i1, int i2, Block block) {
        int i3 = world.e(i0, i1, i2);

        if (!this.g((IBlockAccess) world, i0, i1, i2, i3)) {
            boolean flag0 = this.a(world, i0, i1, i2, i3);

            if ((this.a && !flag0 || !this.a && flag0) && !world.a(i0, i1, i2, (Block) this)) {
                byte b0 = -1;

                if (this.i(world, i0, i1, i2, i3)) {
                    b0 = -3;
                }
                else if (this.a) {
                    b0 = -2;
                }

                world.a(i0, i1, i2, this, this.b(i3), b0);
            }
        }
    }

    public boolean g(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        return false;
    }

    protected boolean a(World world, int i0, int i1, int i2, int i3) {
        return this.h(world, i0, i1, i2, i3) > 0;
    }

    protected int h(World world, int i0, int i1, int i2, int i3) {
        int i4 = l(i3);
        int i5 = i0 + Direction.a[i4];
        int i6 = i2 + Direction.b[i4];
        int i7 = world.g(i5, i1, i6, Direction.d[i4]);

        return i7 >= 15 ? i7 : Math.max(i7, world.a(i5, i1, i6) == Blocks.af ? world.e(i5, i1, i6) : 0);
    }

    protected int h(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        int i4 = l(i3);

        switch (i4) {
            case 0:
            case 2:
                return Math.max(this.i(iblockaccess, i0 - 1, i1, i2, 4), this.i(iblockaccess, i0 + 1, i1, i2, 5));

            case 1:
            case 3:
                return Math.max(this.i(iblockaccess, i0, i1, i2 + 1, 3), this.i(iblockaccess, i0, i1, i2 - 1, 2));

            default:
                return 0;
        }
    }

    protected int i(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        Block block = iblockaccess.a(i0, i1, i2);

        return this.a(block) ? (block == Blocks.af ? iblockaccess.e(i0, i1, i2) : iblockaccess.e(i0, i1, i2, i3)) : 0;
    }

    public boolean f() {
        return true;
    }

    public void a(World world, int i0, int i1, int i2, EntityLivingBase entitylivingbase, ItemStack itemstack) {
        int i3 = ((MathHelper.c((double) (entitylivingbase.z * 4.0F / 360.0F) + 0.5D) & 3) + 2) % 4;

        world.a(i0, i1, i2, i3, 3);
        boolean flag0 = this.a(world, i0, i1, i2, i3);

        if (flag0) {
            world.a(i0, i1, i2, this, 1);
        }
    }

    public void b(World world, int i0, int i1, int i2) {
        this.e(world, i0, i1, i2);
    }

    protected void e(World world, int i0, int i1, int i2) {
        int i3 = l(world.e(i0, i1, i2));

        if (i3 == 1) {
            world.e(i0 + 1, i1, i2, this);
            world.b(i0 + 1, i1, i2, this, 4);
        }

        if (i3 == 3) {
            world.e(i0 - 1, i1, i2, this);
            world.b(i0 - 1, i1, i2, this, 5);
        }

        if (i3 == 2) {
            world.e(i0, i1, i2 + 1, this);
            world.b(i0, i1, i2 + 1, this, 2);
        }

        if (i3 == 0) {
            world.e(i0, i1, i2 - 1, this);
            world.b(i0, i1, i2 - 1, this, 3);
        }
    }

    public void b(World world, int i0, int i1, int i2, int i3) {
        if (this.a) {
            world.d(i0 + 1, i1, i2, this);
            world.d(i0 - 1, i1, i2, this);
            world.d(i0, i1, i2 + 1, this);
            world.d(i0, i1, i2 - 1, this);
            world.d(i0, i1 - 1, i2, this);
            world.d(i0, i1 + 1, i2, this);
        }

        super.b(world, i0, i1, i2, i3);
    }

    public boolean c() {
        return false;
    }

    protected boolean a(Block block) {
        return block.f();
    }

    protected int f(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        return 15;
    }

    public static boolean d(Block block) {
        return Blocks.aR.e(block) || Blocks.bU.e(block);
    }

    public boolean e(Block block) {
        return block == this.e() || block == this.i();
    }

    public boolean i(World world, int i0, int i1, int i2, int i3) {
        int i4 = l(i3);

        if (d(world.a(i0 - Direction.a[i4], i1, i2 - Direction.b[i4]))) {
            int i5 = world.e(i0 - Direction.a[i4], i1, i2 - Direction.b[i4]);
            int i6 = l(i5);

            return i6 != i4;
        }
        else {
            return false;
        }
    }

    protected int k(int i0) {
        return this.b(i0);
    }

    protected abstract int b(int i0);

    protected abstract BlockRedstoneDiode e();

    protected abstract BlockRedstoneDiode i();

    public boolean c(Block block) {
        return this.e(block);
    }
}

