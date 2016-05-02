package net.minecraft.block;

import net.canarymod.api.world.blocks.CanaryBlock;
import net.canarymod.hook.world.IgnitionHook;
import net.canarymod.hook.world.IgnitionHook.IgnitionCause;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;

import java.util.Random;

public class BlockFire extends Block {

    private int[] a = new int[256];
    private int[] b = new int[256];

    protected BlockFire() {
        super(Material.o);
        this.a(true);
    }

    public static void e() {
        Blocks.ab.a(b(Blocks.f), 5, 20);
        Blocks.ab.a(b(Blocks.bw), 5, 20);
        Blocks.ab.a(b(Blocks.bx), 5, 20);
        Blocks.ab.a(b(Blocks.aJ), 5, 20);
        Blocks.ab.a(b(Blocks.ad), 5, 20);
        Blocks.ab.a(b(Blocks.bG), 5, 20);
        Blocks.ab.a(b(Blocks.bF), 5, 20);
        Blocks.ab.a(b(Blocks.bH), 5, 20);
        Blocks.ab.a(b(Blocks.r), 5, 5);
        Blocks.ab.a(b(Blocks.s), 5, 5);
        Blocks.ab.a(b(Blocks.t), 30, 60);
        Blocks.ab.a(b(Blocks.u), 30, 60);
        Blocks.ab.a(b(Blocks.X), 30, 20);
        Blocks.ab.a(b(Blocks.W), 15, 100);
        Blocks.ab.a(b(Blocks.H), 60, 100);
        Blocks.ab.a(b(Blocks.cm), 60, 100);
        Blocks.ab.a(b(Blocks.N), 60, 100);
        Blocks.ab.a(b(Blocks.O), 60, 100);
        Blocks.ab.a(b(Blocks.L), 30, 60);
        Blocks.ab.a(b(Blocks.bd), 15, 100);
        Blocks.ab.a(b(Blocks.ci), 5, 5);
        Blocks.ab.a(b(Blocks.cf), 60, 20);
        Blocks.ab.a(b(Blocks.cg), 60, 20);
    }

    public void a(int i0, int i1, int i2) {
        this.a[i0] = i1;
        this.b[i0] = i2;
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

    public int b() {
        return 3;
    }

    public int a(Random random) {
        return 0;
    }

    public int a(World world) {
        return 30;
    }

    public void a(World world, int i0, int i1, int i2, Random random) {
        if (world.N().b("doFireTick")) {
            boolean flag0 = world.a(i0, i1 - 1, i2) == Blocks.aL;

            if (world.t instanceof WorldProviderEnd && world.a(i0, i1 - 1, i2) == Blocks.h) {
                flag0 = true;
            }

            if (!this.c(world, i0, i1, i2)) {
                world.f(i0, i1, i2);
            }

            if (!flag0 && world.P() && (world.y(i0, i1, i2) || world.y(i0 - 1, i1, i2) || world.y(i0 + 1, i1, i2) || world.y(i0, i1, i2 - 1) || world.y(i0, i1, i2 + 1))) {
                world.f(i0, i1, i2);
            }
            else {
                int i3 = world.e(i0, i1, i2);

                if (i3 < 15) {
                    world.a(i0, i1, i2, i3 + random.nextInt(3) / 2, 4);
                }

                world.a(i0, i1, i2, this, this.a(world) + random.nextInt(10));
                if (!flag0 && !this.e(world, i0, i1, i2)) {
                    if (!World.a((IBlockAccess) world, i0, i1 - 1, i2) || i3 > 3) {
                        world.f(i0, i1, i2);
                    }
                }
                else if (!flag0 && !this.e((IBlockAccess) world, i0, i1 - 1, i2) && i3 == 15 && random.nextInt(4) == 0) {
                    world.f(i0, i1, i2);
                }
                else {
                    boolean flag1 = world.z(i0, i1, i2);
                    byte b0 = 0;

                    if (flag1) {
                        b0 = -50;
                    }

                    this.a(world, i0 + 1, i1, i2, 300 + b0, random, i3);
                    this.a(world, i0 - 1, i1, i2, 300 + b0, random, i3);
                    this.a(world, i0, i1 - 1, i2, 250 + b0, random, i3);
                    this.a(world, i0, i1 + 1, i2, 250 + b0, random, i3);
                    this.a(world, i0, i1, i2 - 1, 300 + b0, random, i3);
                    this.a(world, i0, i1, i2 + 1, 300 + b0, random, i3);

                    for (int i4 = i0 - 1; i4 <= i0 + 1; ++i4) {
                        for (int i5 = i2 - 1; i5 <= i2 + 1; ++i5) {
                            for (int i6 = i1 - 1; i6 <= i1 + 4; ++i6) {
                                if (i4 != i0 || i6 != i1 || i5 != i2) {
                                    int i7 = 100;

                                    if (i6 > i1 + 1) {
                                        i7 += (i6 - (i1 + 1)) * 100;
                                    }

                                    int i8 = this.m(world, i4, i6, i5);

                                    if (i8 > 0) {
                                        int i9 = (i8 + 40 + world.r.a() * 7) / (i3 + 30);

                                        if (flag1) {
                                            i9 /= 2;
                                        }

                                        if (i9 > 0 && random.nextInt(i7) <= i9 && (!world.P() || !world.y(i4, i6, i5)) && !world.y(i4 - 1, i6, i2) && !world.y(i4 + 1, i6, i5) && !world.y(i4, i6, i5 - 1) && !world.y(i4, i6, i5 + 1)) {
                                            int i10 = i3 + random.nextInt(5) / 4;

                                            if (i10 > 15) {
                                                i10 = 15;
                                            }

                                            // CanaryMod: Ignition
                                            CanaryBlock ignited = (CanaryBlock) world.getCanaryWorld().getBlockAt(i0, i1, i2);

                                            ignited.setStatus((byte) 3); // Spread Status 3
                                            IgnitionHook hook = (IgnitionHook) new IgnitionHook(ignited, null, null, IgnitionCause.FIRE_SPREAD).call();
                                            if (!hook.isCanceled()) {
                                                world.d(i4, i6, i5, this, i10, 3);
                                            }
                                            //
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean L() {
        return false;
    }

    private void a(World world, int i0, int i1, int i2, int i3, Random random, int i4) {
        int i5 = this.b[Block.b(world.a(i0, i1, i2))];

        if (random.nextInt(i3) < i5) {
            boolean flag0 = world.a(i0, i1, i2) == Blocks.W;

            if (random.nextInt(i4 + 10) < 5 && !world.y(i0, i1, i2)) {
                int i6 = i4 + random.nextInt(5) / 4;

                if (i6 > 15) {
                    i6 = 15;
                }
                // CanaryMod: Ignition
                CanaryBlock ignited = (CanaryBlock) world.getCanaryWorld().getBlockAt(i0, i1, i2);

                ignited.setStatus((byte) 3); // Spread Status 3
                IgnitionHook hook = (IgnitionHook) new IgnitionHook(ignited, null, null, IgnitionCause.FIRE_SPREAD).call();
                if (!hook.isCanceled()) {
                    world.d(i0, i1, i2, this, i6, 3);
                }
                //
            }
            else {
                // CanaryMod: Ignition
                CanaryBlock ignited = (CanaryBlock) world.getCanaryWorld().getBlockAt(i0, i1, i2);

                ignited.setStatus((byte) 4); // Burned Up Status 4
                IgnitionHook hook = (IgnitionHook) new IgnitionHook(ignited, null, null, IgnitionCause.BURNT).call();
                if (!hook.isCanceled()) {
                    world.f(i0, i1, i2);
                }
                //
            }

            if (flag0) {
                Blocks.W.b(world, i0, i1, i2, 1);
            }
        }
    }

    private boolean e(World world, int i0, int i1, int i2) {
        return this.e((IBlockAccess) world, i0 + 1, i1, i2) ? true : (this.e((IBlockAccess) world, i0 - 1, i1, i2) ? true : (this.e((IBlockAccess) world, i0, i1 - 1, i2) ? true : (this.e((IBlockAccess) world, i0, i1 + 1, i2) ? true : (this.e((IBlockAccess) world, i0, i1, i2 - 1) ? true : this.e((IBlockAccess) world, i0, i1, i2 + 1)))));
    }

    private int m(World world, int i0, int i1, int i2) {
        byte b0 = 0;

        if (!world.c(i0, i1, i2)) {
            return 0;
        }
        else {
            int i3 = this.a(world, i0 + 1, i1, i2, b0);

            i3 = this.a(world, i0 - 1, i1, i2, i3);
            i3 = this.a(world, i0, i1 - 1, i2, i3);
            i3 = this.a(world, i0, i1 + 1, i2, i3);
            i3 = this.a(world, i0, i1, i2 - 1, i3);
            i3 = this.a(world, i0, i1, i2 + 1, i3);
            return i3;
        }
    }

    public boolean v() {
        return false;
    }

    public boolean e(IBlockAccess iblockaccess, int i0, int i1, int i2) {
        return this.a[Block.b(iblockaccess.a(i0, i1, i2))] > 0;
    }

    public int a(World world, int i0, int i1, int i2, int i3) {
        int i4 = this.a[Block.b(world.a(i0, i1, i2))];

        return i4 > i3 ? i4 : i3;
    }

    public boolean c(World world, int i0, int i1, int i2) {
        return World.a((IBlockAccess) world, i0, i1 - 1, i2) || this.e(world, i0, i1, i2);
    }

    public void a(World world, int i0, int i1, int i2, Block block) {
        if (!World.a((IBlockAccess) world, i0, i1 - 1, i2) && !this.e(world, i0, i1, i2)) {
            world.f(i0, i1, i2);
        }
    }

    public void b(World world, int i0, int i1, int i2) {
        if (world.t.i > 0 || !Blocks.aO.e(world, i0, i1, i2)) {
            if (!World.a((IBlockAccess) world, i0, i1 - 1, i2) && !this.e(world, i0, i1, i2)) {
                world.f(i0, i1, i2);
            }
            else {
                world.a(i0, i1, i2, this, this.a(world) + world.s.nextInt(10));
            }
        }
    }
}

