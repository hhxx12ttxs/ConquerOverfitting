package net.minecraft.block;

import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.CanaryBlock;
import net.canarymod.hook.world.FlowHook;
import net.canarymod.hook.world.LiquidDestroyHook;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.Random;

public class BlockDynamicLiquid extends BlockLiquid {

    int a;
    boolean[] b = new boolean[4];
    int[] M = new int[4];

    protected BlockDynamicLiquid(Material material) {
        super(material);
    }

    private void n(World world, int i0, int i1, int i2) {
        int i3 = world.e(i0, i1, i2);

        world.d(i0, i1, i2, Block.e(Block.b((Block) this) + 1), i3, 2);
    }

    public void a(World world, int i0, int i1, int i2, Random random) {

        // CanaryMod: Flow from
        CanaryBlock from = (CanaryBlock) world.getCanaryWorld().getBlockAt(i0, i1, i2);
        //

        int i3 = this.e(world, i0, i1, i2);
        byte b0 = 1;

        if (this.J == Material.i && !world.t.f) {
            b0 = 2;
        }

        boolean flag0 = true;
        int i4 = this.a(world);
        int i5;

        if (i3 > 0) {
            byte b1 = -100;

            this.a = 0;
            int i6 = this.a(world, i0 - 1, i1, i2, b1);

            i6 = this.a(world, i0 + 1, i1, i2, i6);
            i6 = this.a(world, i0, i1, i2 - 1, i6);
            i6 = this.a(world, i0, i1, i2 + 1, i6);
            i5 = i6 + b0;
            if (i5 >= 8 || i6 < 0) {
                i5 = -1;
            }

            if (this.e(world, i0, i1 + 1, i2) >= 0) {
                int i7 = this.e(world, i0, i1 + 1, i2);

                if (i7 >= 8) {
                    i5 = i7;
                }
                else {
                    i5 = i7 + 8;
                }
            }

            if (this.a >= 2 && this.J == Material.h) {
                if (world.a(i0, i1 - 1, i2).o().a()) {
                    i5 = 0;
                }
                else if (world.a(i0, i1 - 1, i2).o() == this.J && world.e(i0, i1 - 1, i2) == 0) {
                    i5 = 0;
                }
            }

            if (this.J == Material.i && i3 < 8 && i5 < 8 && i5 > i3 && random.nextInt(4) != 0) {
                i4 *= 4;
            }

            if (i5 == i3) {
                if (flag0) {
                    this.n(world, i0, i1, i2);
                }
            }
            else {
                i3 = i5;
                if (i5 < 0) {
                    world.f(i0, i1, i2);
                }
                else {
                    world.a(i0, i1, i2, i5, 2);
                    world.a(i0, i1, i2, this, i4);
                    world.d(i0, i1, i2, this);
                }
            }
        }
        else {
            this.n(world, i0, i1, i2);
        }

        if (this.q(world, i0, i1 - 1, i2)) {
            if (this.J == Material.i && world.a(i0, i1 - 1, i2).o() == Material.h) {
                world.b(i0, i1 - 1, i2, Blocks.b);
                this.m(world, i0, i1 - 1, i2);
                return;
            }

            // CanaryMod: Flow (down)
            CanaryBlock to = (CanaryBlock) world.getCanaryWorld().getBlockAt(i0, i1 - 1, i2);
            FlowHook hook = (FlowHook) new FlowHook(from, to).call();
            if (!hook.isCanceled()) {
                if (i3 >= 8) {
                    this.h(world, i0, i1 - 1, i2, i3);
                }
                else {
                    this.h(world, i0, i1 - 1, i2, i3 + 8);
                }
            }
            //
        }
        else if (i3 >= 0 && (i3 == 0 || this.p(world, i0, i1 - 1, i2))) {
            boolean[] aboolean = this.o(world, i0, i1, i2);

            i5 = i3 + b0;
            if (i3 >= 8) {
                i5 = 1;
            }

            if (i5 >= 8) {
                return;
            }

            if (aboolean[0]) {
                // CanaryMod: Flow
                CanaryBlock to = (CanaryBlock) world.getCanaryWorld().getBlockAt(i0 - 1, i1, i2);
                FlowHook hook = (FlowHook) new FlowHook(from, to).call();
                if (!hook.isCanceled()) {
                    this.h(world, i0 - 1, i1, i2, i5);
                }
                //
            }

            if (aboolean[1]) {
                // CanaryMod: Flow
                CanaryBlock to = (CanaryBlock) world.getCanaryWorld().getBlockAt(i0 + 1, i1, i2);
                FlowHook hook = (FlowHook) new FlowHook(from, to).call();
                if (!hook.isCanceled()) {
                    this.h(world, i0 + 1, i1, i2, i5);
                }
                //
            }

            if (aboolean[2]) {
                // CanaryMod: Flow
                CanaryBlock to = (CanaryBlock) world.getCanaryWorld().getBlockAt(i0, i1, i2 - 1);
                FlowHook hook = (FlowHook) new FlowHook(from, to).call();
                if (!hook.isCanceled()) {
                    this.h(world, i0, i1, i2 - 1, i5);
                }
                //
            }

            if (aboolean[3]) {
                // CanaryMod: Flow
                CanaryBlock to = (CanaryBlock) world.getCanaryWorld().getBlockAt(i0, i1, i2 + 1);
                FlowHook hook = (FlowHook) new FlowHook(from, to).call();
                if (!hook.isCanceled()) {
                    this.h(world, i0, i1, i2 + 1, i5);
                }
                //
            }
        }
    }

    private void h(World world, int i0, int i1, int i2, int i3) {
        if (this.q(world, i0, i1, i2)) {
            Block block = world.a(i0, i1, i2);

            if (this.J == Material.i) {
                this.m(world, i0, i1, i2);
            }
            else {
                block.b(world, i0, i1, i2, world.e(i0, i1, i2), 0);
            }

            world.d(i0, i1, i2, this, i3, 3);
        }
    }

    private int c(World world, int i0, int i1, int i2, int i3, int i4) {
        int i5 = 1000;

        for (int i6 = 0; i6 < 4; ++i6) {
            if ((i6 != 0 || i4 != 1) && (i6 != 1 || i4 != 0) && (i6 != 2 || i4 != 3) && (i6 != 3 || i4 != 2)) {
                int i7 = i0;
                int i8 = i2;

                if (i6 == 0) {
                    i7 = i0 - 1;
                }

                if (i6 == 1) {
                    ++i7;
                }

                if (i6 == 2) {
                    i8 = i2 - 1;
                }

                if (i6 == 3) {
                    ++i8;
                }

                if (!this.p(world, i7, i1, i8) && (world.a(i7, i1, i8).o() != this.J || world.e(i7, i1, i8) != 0)) {
                    if (!this.p(world, i7, i1 - 1, i8)) {
                        return i3;
                    }

                    if (i3 < 4) {
                        int i9 = this.c(world, i7, i1, i8, i3 + 1, i6);

                        if (i9 < i5) {
                            i5 = i9;
                        }
                    }
                }
            }
        }

        return i5;
    }

    private boolean[] o(World world, int i0, int i1, int i2) {
        int i3;
        int i4;

        for (i3 = 0; i3 < 4; ++i3) {
            this.M[i3] = 1000;
            i4 = i0;
            int i5 = i2;

            if (i3 == 0) {
                i4 = i0 - 1;
            }

            if (i3 == 1) {
                ++i4;
            }

            if (i3 == 2) {
                i5 = i2 - 1;
            }

            if (i3 == 3) {
                ++i5;
            }

            if (!this.p(world, i4, i1, i5) && (world.a(i4, i1, i5).o() != this.J || world.e(i4, i1, i5) != 0)) {
                if (this.p(world, i4, i1 - 1, i5)) {
                    this.M[i3] = this.c(world, i4, i1, i5, 1, i3);
                }
                else {
                    this.M[i3] = 0;
                }
            }
        }

        i3 = this.M[0];

        for (i4 = 1; i4 < 4; ++i4) {
            if (this.M[i4] < i3) {
                i3 = this.M[i4];
            }
        }

        for (i4 = 0; i4 < 4; ++i4) {
            this.b[i4] = this.M[i4] == i3;
        }

        return this.b;
    }

    private boolean p(World world, int i0, int i1, int i2) {
        Block block = world.a(i0, i1, i2);

        return block != Blocks.ao && block != Blocks.av && block != Blocks.an && block != Blocks.ap && block != Blocks.aH ? (block.J == Material.E ? true : block.J.c()) : true;
    }

    protected int a(World world, int i0, int i1, int i2, int i3) {
        int i4 = this.e(world, i0, i1, i2);

        if (i4 < 0) {
            return i3;
        }
        else {
            if (i4 == 0) {
                ++this.a;
            }

            if (i4 >= 8) {
                i4 = 0;
            }

            return i3 >= 0 && i4 >= i3 ? i3 : i4;
        }
    }

    private boolean q(World world, int i0, int i1, int i2) {
        // CanaryMod: LiquidDestroy
        CanaryBlock dest = (CanaryBlock) world.getCanaryWorld().getBlockAt(i0, i1, i2);
        BlockType liquid = this.J == Material.i ? BlockType.LavaFlowing : BlockType.WaterFlowing;
        LiquidDestroyHook hook = (LiquidDestroyHook) new LiquidDestroyHook(liquid, dest).call();
        if (hook.isForceDestroy()) {
            return true;
        }
        else if (hook.isCanceled()) {
            return false;
        }
        //

        Material material = world.a(i0, i1, i2).o();

        return material == this.J ? false : (material == Material.i ? false : !this.p(world, i0, i1, i2));
    }

    public void b(World world, int i0, int i1, int i2) {
        super.b(world, i0, i1, i2);
        if (world.a(i0, i1, i2) == this) {
            world.a(i0, i1, i2, this, this.a(world));
        }
    }

    public boolean L() {
        return true;
    }
}

