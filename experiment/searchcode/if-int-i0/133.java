package net.minecraft.block;

import net.canarymod.hook.world.TreeGrowHook;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenCanopyTree;
import net.minecraft.world.gen.feature.WorldGenForest;
import net.minecraft.world.gen.feature.WorldGenMegaJungle;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;


public class BlockSapling extends BlockBush implements IGrowable {

    public static final String[] a = new String[]{ "oak", "spruce", "birch", "jungle", "acacia", "roofed_oak" };
    private static final Icon[] b = new Icon[a.length];

    protected BlockSapling() {
        float f0 = 0.4F;

        this.a(0.5F - f0, 0.0F, 0.5F - f0, 0.5F + f0, f0 * 2.0F, 0.5F + f0);
        this.a(CreativeTabs.c);
    }

    public void a(World world, int i0, int i1, int i2, Random random) {
        if (!world.E) {
            super.a(world, i0, i1, i2, random);
            if (world.k(i0, i1 + 1, i2) >= 9 && random.nextInt(7) == 0) {
                this.c(world, i0, i1, i2, random);
            }
        }
    }

    public void c(World world, int i0, int i1, int i2, Random random) {
        int i3 = world.e(i0, i1, i2);

        if ((i3 & 8) == 0) {
            world.a(i0, i1, i2, i3 | 8, 4);
        }
        else {
            // CanaryMod: TreeGrow; If someone figures out how to get more information into this, let me know - darkdiplomat;
            TreeGrowHook hook = (TreeGrowHook) new TreeGrowHook(world.getCanaryWorld().getBlockAt(i0, i1, i2)).call();
            if (!hook.isCanceled()) {
                this.d(world, i0, i1, i2, random);
            }
            //
        }
    }

    public void d(World world, int i0, int i1, int i2, Random random) {
        int i3 = world.e(i0, i1, i2) & 7;
        Object object = random.nextInt(10) == 0 ? new WorldGenBigTree(true) : new WorldGenTrees(true);
        int i4 = 0;
        int i5 = 0;
        boolean flag0 = false;

        switch (i3) {
            case 0:
            default:
                break;

            case 1:
                label78:
                for (i4 = 0; i4 >= -1; --i4) {
                    for (i5 = 0; i5 >= -1; --i5) {
                        if (this.a(world, i0 + i4, i1, i2 + i5, 1) && this.a(world, i0 + i4 + 1, i1, i2 + i5, 1) && this.a(world, i0 + i4, i1, i2 + i5 + 1, 1) && this.a(world, i0 + i4 + 1, i1, i2 + i5 + 1, 1)) {
                            object = new WorldGenMegaPineTree(false, random.nextBoolean());
                            flag0 = true;
                            break label78;
                        }
                    }
                }

                if (!flag0) {
                    i5 = 0;
                    i4 = 0;
                    object = new WorldGenTaiga2(true);
                }
                break;

            case 2:
                object = new WorldGenForest(true, false);
                break;

            case 3:
                label93:
                for (i4 = 0; i4 >= -1; --i4) {
                    for (i5 = 0; i5 >= -1; --i5) {
                        if (this.a(world, i0 + i4, i1, i2 + i5, 3) && this.a(world, i0 + i4 + 1, i1, i2 + i5, 3) && this.a(world, i0 + i4, i1, i2 + i5 + 1, 3) && this.a(world, i0 + i4 + 1, i1, i2 + i5 + 1, 3)) {
                            object = new WorldGenMegaJungle(true, 10, 20, 3, 3);
                            flag0 = true;
                            break label93;
                        }
                    }
                }

                if (!flag0) {
                    i5 = 0;
                    i4 = 0;
                    object = new WorldGenTrees(true, 4 + random.nextInt(7), 3, 3, false);
                }
                break;

            case 4:
                object = new WorldGenSavannaTree(true);
                break;

            case 5:
                label108:
                for (i4 = 0; i4 >= -1; --i4) {
                    for (i5 = 0; i5 >= -1; --i5) {
                        if (this.a(world, i0 + i4, i1, i2 + i5, 5) && this.a(world, i0 + i4 + 1, i1, i2 + i5, 5) && this.a(world, i0 + i4, i1, i2 + i5 + 1, 5) && this.a(world, i0 + i4 + 1, i1, i2 + i5 + 1, 5)) {
                            object = new WorldGenCanopyTree(true);
                            flag0 = true;
                            break label108;
                        }
                    }
                }

                if (!flag0) {
                    return;
                }
        }

        Block block = Blocks.a;

        if (flag0) {
            world.d(i0 + i4, i1, i2 + i5, block, 0, 4);
            world.d(i0 + i4 + 1, i1, i2 + i5, block, 0, 4);
            world.d(i0 + i4, i1, i2 + i5 + 1, block, 0, 4);
            world.d(i0 + i4 + 1, i1, i2 + i5 + 1, block, 0, 4);
        }
        else {
            world.d(i0, i1, i2, block, 0, 4);
        }

        if (!((WorldGenerator) object).a(world, random, i0 + i4, i1, i2 + i5)) {
            if (flag0) {
                world.d(i0 + i4, i1, i2 + i5, this, i3, 4);
                world.d(i0 + i4 + 1, i1, i2 + i5, this, i3, 4);
                world.d(i0 + i4, i1, i2 + i5 + 1, this, i3, 4);
                world.d(i0 + i4 + 1, i1, i2 + i5 + 1, this, i3, 4);
            }
            else {
                world.d(i0, i1, i2, this, i3, 4);
            }
        }
    }

    public boolean a(World world, int i0, int i1, int i2, int i3) {
        return world.a(i0, i1, i2) == this && (world.e(i0, i1, i2) & 7) == i3;
    }

    public int a(int i0) {
        return MathHelper.a(i0 & 7, 0, 5);
    }

    public boolean a(World world, int i0, int i1, int i2, boolean flag0) {
        return true;
    }

    public boolean a(World world, Random random, int i0, int i1, int i2) {
        return (double) world.s.nextFloat() < 0.45D;
    }

    public void b(World world, Random random, int i0, int i1, int i2) {
        this.c(world, i0, i1, i2, random);
    }

}

