package net.minecraft.block;

import net.canarymod.api.world.blocks.CanaryBlock;
import net.canarymod.hook.world.LeafDecayHook;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

import java.util.Random;

public abstract class BlockLeaves extends BlockLeavesBase {


    int[] a;
    protected Icon[][] M = new Icon[2][];

    public BlockLeaves() {
        super(Material.j, false);
        this.a(true);
        this.a(CreativeTabs.c);
        this.c(0.2F);
        this.g(1);
        this.a(h);
    }

    public void a(World world, int i0, int i1, int i2, Block block, int i3) {
        byte b0 = 1;
        int i4 = b0 + 1;

        if (world.b(i0 - i4, i1 - i4, i2 - i4, i0 + i4, i1 + i4, i2 + i4)) {
            for (int i5 = -b0; i5 <= b0; ++i5) {
                for (int i6 = -b0; i6 <= b0; ++i6) {
                    for (int i7 = -b0; i7 <= b0; ++i7) {
                        if (world.a(i0 + i5, i1 + i6, i2 + i7).o() == Material.j) {
                            int i8 = world.e(i0 + i5, i1 + i6, i2 + i7);

                            world.a(i0 + i5, i1 + i6, i2 + i7, i8 | 8, 4);
                        }
                    }
                }
            }
        }
    }

    public void a(World world, int i0, int i1, int i2, Random random) {
        if (!world.E) {
            int i3 = world.e(i0, i1, i2);

            if ((i3 & 8) != 0 && (i3 & 4) == 0) {
                byte b0 = 4;
                int i4 = b0 + 1;
                byte b1 = 32;
                int i5 = b1 * b1;
                int i6 = b1 / 2;

                if (this.a == null) {
                    this.a = new int[b1 * b1 * b1];
                }

                int i7;

                if (world.b(i0 - i4, i1 - i4, i2 - i4, i0 + i4, i1 + i4, i2 + i4)) {
                    int i8;
                    int i9;
                    int i10;

                    for (i7 = -b0; i7 <= b0; ++i7) {
                        for (i8 = -b0; i8 <= b0; ++i8) {
                            for (i9 = -b0; i9 <= b0; ++i9) {
                                Block block = world.a(i0 + i7, i1 + i8, i2 + i9);

                                if (block != Blocks.r && block != Blocks.s) {
                                    if (block.o() == Material.j) {
                                        this.a[(i7 + i6) * i5 + (i8 + i6) * b1 + i9 + i6] = -2;
                                    }
                                    else {
                                        this.a[(i7 + i6) * i5 + (i8 + i6) * b1 + i9 + i6] = -1;
                                    }
                                }
                                else {
                                    this.a[(i7 + i6) * i5 + (i8 + i6) * b1 + i9 + i6] = 0;
                                }
                            }
                        }
                    }

                    for (i7 = 1; i7 <= 4; ++i7) {
                        for (i8 = -b0; i8 <= b0; ++i8) {
                            for (i9 = -b0; i9 <= b0; ++i9) {
                                for (i10 = -b0; i10 <= b0; ++i10) {
                                    if (this.a[(i8 + i6) * i5 + (i9 + i6) * b1 + i10 + i6] == i7 - 1) {
                                        if (this.a[(i8 + i6 - 1) * i5 + (i9 + i6) * b1 + i10 + i6] == -2) {
                                            this.a[(i8 + i6 - 1) * i5 + (i9 + i6) * b1 + i10 + i6] = i7;
                                        }

                                        if (this.a[(i8 + i6 + 1) * i5 + (i9 + i6) * b1 + i10 + i6] == -2) {
                                            this.a[(i8 + i6 + 1) * i5 + (i9 + i6) * b1 + i10 + i6] = i7;
                                        }

                                        if (this.a[(i8 + i6) * i5 + (i9 + i6 - 1) * b1 + i10 + i6] == -2) {
                                            this.a[(i8 + i6) * i5 + (i9 + i6 - 1) * b1 + i10 + i6] = i7;
                                        }

                                        if (this.a[(i8 + i6) * i5 + (i9 + i6 + 1) * b1 + i10 + i6] == -2) {
                                            this.a[(i8 + i6) * i5 + (i9 + i6 + 1) * b1 + i10 + i6] = i7;
                                        }

                                        if (this.a[(i8 + i6) * i5 + (i9 + i6) * b1 + (i10 + i6 - 1)] == -2) {
                                            this.a[(i8 + i6) * i5 + (i9 + i6) * b1 + (i10 + i6 - 1)] = i7;
                                        }

                                        if (this.a[(i8 + i6) * i5 + (i9 + i6) * b1 + i10 + i6 + 1] == -2) {
                                            this.a[(i8 + i6) * i5 + (i9 + i6) * b1 + i10 + i6 + 1] = i7;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                i7 = this.a[i6 * i5 + i6 * b1 + i6];
                if (i7 >= 0) {
                    world.a(i0, i1, i2, i3 & -9, 4);
                }
                else {
                    this.e(world, i0, i1, i2);
                }
            }
        }
    }

    private void e(World world, int i0, int i1, int i2) {
        // CanaryMod: LeafDecay
        CanaryBlock leaves = (CanaryBlock) world.getCanaryWorld().getBlockAt(i0, i1, i2);
        LeafDecayHook hook = (LeafDecayHook) new LeafDecayHook(leaves).call();
        if (!hook.isCanceled()) {
            this.b(world, i0, i1, i2, world.e(i0, i1, i2), 0);
            world.f(i0, i1, i2);
        }
        //
    }

    public int a(Random random) {
        return random.nextInt(20) == 0 ? 1 : 0;
    }

    public Item a(int i0, Random random, int i1) {
        return Item.a(Blocks.g);
    }

    public void a(World world, int i0, int i1, int i2, int i3, float f0, int i4) {
        if (!world.E) {
            int i5 = this.b(i3);

            if (i4 > 0) {
                i5 -= 2 << i4;
                if (i5 < 10) {
                    i5 = 10;
                }
            }

            if (world.s.nextInt(i5) == 0) {
                Item item = this.a(i3, world.s, i4);

                this.a(world, i0, i1, i2, new ItemStack(item, 1, this.a(i3)));
            }

            i5 = 200;
            if (i4 > 0) {
                i5 -= 10 << i4;
                if (i5 < 40) {
                    i5 = 40;
                }
            }

            this.c(world, i0, i1, i2, i3, i5);
        }

    }

    protected void c(World world, int i0, int i1, int i2, int i3, int i4) {
    }

    protected int b(int i0) {
        return 20;
    }

    public void a(World world, EntityPlayer entityplayer, int i0, int i1, int i2, int i3) {
        if (!world.E && entityplayer.bD() != null && entityplayer.bD().b() == Items.aZ) {
            entityplayer.a(StatList.C[Block.b((Block) this)], 1);
            this.a(world, i0, i1, i2, new ItemStack(Item.a((Block) this), 1, i3 & 3));
        }
        else {
            super.a(world, entityplayer, i0, i1, i2, i3);
        }
    }

    public int a(int i0) {
        return i0 & 3;
    }

    public boolean c() {
        return !this.P;
    }

    protected ItemStack j(int i0) {
        return new ItemStack(Item.a((Block) this), 1, i0 & 3);
    }

    public abstract String[] e();
}

