package net.minecraft.block;

import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.CanaryBlock;
import net.canarymod.hook.world.RedstoneChangeHook;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BlockRedstoneTorch extends BlockTorch {

    private boolean a;
    private static Map b = new HashMap();

    private boolean a(World world, int i0, int i1, int i2, boolean flag0) {
        if (!b.containsKey(world)) {
            b.put(world, new ArrayList());
        }

        List list = (List) b.get(world);

        if (flag0) {
            list.add(new Toggle(i0, i1, i2, world.H()));
        }

        int i3 = 0;

        for (int i4 = 0; i4 < list.size(); ++i4) {
            Toggle blockredstonetorch_toggle = (Toggle) list.get(i4);

            if (blockredstonetorch_toggle.a == i0 && blockredstonetorch_toggle.b == i1 && blockredstonetorch_toggle.c == i2) {
                ++i3;
                if (i3 >= 8) {
                    return true;
                }
            }
        }

        return false;
    }

    protected BlockRedstoneTorch(boolean flag0) {
        this.a = flag0;
        this.a(true);
        this.a((CreativeTabs) null);
    }

    public int a(World world) {
        return 2;
    }

    public void b(World world, int i0, int i1, int i2) {
        if (world.e(i0, i1, i2) == 0) {
            super.b(world, i0, i1, i2);
        }

        if (this.a) {
            // CanaryMod: RedstoneChange; Torch put in
            RedstoneChangeHook hook = (RedstoneChangeHook) new RedstoneChangeHook(world.getCanaryWorld().getBlockAt(i0, i1, i2), 0, 15).call();
            if (hook.isCanceled()) {
                this.a = false;
                return;
            }
            //
            world.d(i0, i1 - 1, i2, this);
            world.d(i0, i1 + 1, i2, this);
            world.d(i0 - 1, i1, i2, this);
            world.d(i0 + 1, i1, i2, this);
            world.d(i0, i1, i2 - 1, this);
            world.d(i0, i1, i2 + 1, this);
        }
    }

    public void a(World world, int i0, int i1, int i2, Block block, int i3) {
        if (this.a) {
            // CanaryMod: RedstoneChange; Torch broke
            new RedstoneChangeHook(new CanaryBlock(BlockType.RedstoneLampOn.getId(), (short) 2, i0, i1, i2, world.getCanaryWorld()), 15, 0).call();
            //
            world.d(i0, i1 - 1, i2, this);
            world.d(i0, i1 + 1, i2, this);
            world.d(i0 - 1, i1, i2, this);
            world.d(i0 + 1, i1, i2, this);
            world.d(i0, i1, i2 - 1, this);
            world.d(i0, i1, i2 + 1, this);
        }
    }

    public int b(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        if (!this.a) {
            return 0;
        }
        else {
            int i4 = iblockaccess.e(i0, i1, i2);

            return i4 == 5 && i3 == 1 ? 0 : (i4 == 3 && i3 == 3 ? 0 : (i4 == 4 && i3 == 2 ? 0 : (i4 == 1 && i3 == 5 ? 0 : (i4 == 2 && i3 == 4 ? 0 : 15))));
        }
    }

    private boolean m(World world, int i0, int i1, int i2) {
        int i3 = world.e(i0, i1, i2);

        return i3 == 5 && world.f(i0, i1 - 1, i2, 0) ? true : (i3 == 3 && world.f(i0, i1, i2 - 1, 2) ? true : (i3 == 4 && world.f(i0, i1, i2 + 1, 3) ? true : (i3 == 1 && world.f(i0 - 1, i1, i2, 4) ? true : i3 == 2 && world.f(i0 + 1, i1, i2, 5))));
    }

    public void a(World world, int i0, int i1, int i2, Random random) {
        boolean flag0 = this.m(world, i0, i1, i2);
        List list = (List) b.get(world);

        while (list != null && !list.isEmpty() && world.H() - ((Toggle) list.get(0)).d > 60L) {
            list.remove(0);
        }

        if (this.a) {
            if (flag0) {
                // CanaryMod: RedstoneChange; Torch off
                RedstoneChangeHook hook = (RedstoneChangeHook) new RedstoneChangeHook(world.getCanaryWorld().getBlockAt(i0, i1, i2), 15, 0).call();
                if (hook.isCanceled()) {
                    return;
                }
                //
                world.d(i0, i1, i2, Blocks.az, world.e(i0, i1, i2), 3);
                if (this.a(world, i0, i1, i2, true)) {
                    world.a((double) ((float) i0 + 0.5F), (double) ((float) i1 + 0.5F), (double) ((float) i2 + 0.5F), "random.fizz", 0.5F, 2.6F + (world.s.nextFloat() - world.s.nextFloat()) * 0.8F);

                    for (int i3 = 0; i3 < 5; ++i3) {
                        double d0 = (double) i0 + random.nextDouble() * 0.6D + 0.2D;
                        double d1 = (double) i1 + random.nextDouble() * 0.6D + 0.2D;
                        double d2 = (double) i2 + random.nextDouble() * 0.6D + 0.2D;

                        world.a("smoke", d0, d1, d2, 0.0D, 0.0D, 0.0D);
                    }
                }
            }
        }
        else if (!flag0 && !this.a(world, i0, i1, i2, false)) {
            // CanaryMod: RedstoneChange; Torch on
            RedstoneChangeHook hook = (RedstoneChangeHook) new RedstoneChangeHook(world.getCanaryWorld().getBlockAt(i0, i1, i2), 15, 0).call();
            if (hook.isCanceled()) {
                return;
            }
            //
            world.d(i0, i1, i2, Blocks.aA, world.e(i0, i1, i2), 3);
        }
    }

    public void a(World world, int i0, int i1, int i2, Block block) {
        if (!this.b(world, i0, i1, i2, block)) {
            boolean flag0 = this.m(world, i0, i1, i2);

            if (this.a && flag0 || !this.a && !flag0) {
                world.a(i0, i1, i2, this, this.a(world));
            }
        }
    }

    public int c(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        return i3 == 0 ? this.b(iblockaccess, i0, i1, i2, i3) : 0;
    }

    public Item a(int i0, Random random, int i1) {
        return Item.a(Blocks.aA);
    }

    public boolean f() {
        return true;
    }

    public boolean c(Block block) {
        return block == Blocks.az || block == Blocks.aA;
    }

    static class Toggle {

        int a;
        int b;
        int c;
        long d;

        public Toggle(int i0, int i1, int i2, long i3) {
            this.a = i0;
            this.b = i1;
            this.c = i2;
            this.d = i3;
        }
    }
}

