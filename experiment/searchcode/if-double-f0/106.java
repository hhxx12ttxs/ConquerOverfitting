package net.minecraft.block;

import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.CanaryBlock;
import net.canarymod.hook.world.PistonExtendHook;
import net.canarymod.hook.world.PistonRetractHook;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class BlockPistonBase extends Block {

    private final boolean a;
    private boolean attemptRetract; // CanaryMod: Used to signal wether to retract the block attached to the stick piston.

    public BlockPistonBase(boolean flag0) {
        super(Material.H);
        this.a = flag0;
        this.a(i);
        this.c(0.5F);
        this.a(CreativeTabs.d);
    }

    public int b() {
        return 16;
    }

    public boolean c() {
        return false;
    }

    public boolean a(World world, int i0, int i1, int i2, EntityPlayer entityplayer, int i3, float f0, float f1, float f2) {
        return false;
    }

    public void a(World world, int i0, int i1, int i2, EntityLivingBase entitylivingbase, ItemStack itemstack) {
        int i3 = a(world, i0, i1, i2, entitylivingbase);

        world.a(i0, i1, i2, i3, 2);
        if (!world.E) {
            this.e(world, i0, i1, i2);
        }
    }

    public void a(World world, int i0, int i1, int i2, Block block) {
        if (!world.E) {
            this.e(world, i0, i1, i2);
        }
    }

    public void b(World world, int i0, int i1, int i2) {
        if (!world.E && world.o(i0, i1, i2) == null) {
            this.e(world, i0, i1, i2);
        }
    }

    private void e(World world, int i0, int i1, int i2) {
        int i3 = world.e(i0, i1, i2);
        int i4 = b(i3);

        if (i4 != 7) {
            boolean flag0 = this.a(world, i0, i1, i2, i4);

            // CanaryMod: Get Blocks
            CanaryBlock piston = new CanaryBlock((this.a ? BlockType.Piston.getId() : BlockType.StickyPiston.getId()), (byte) 0, i0, i1, i2, world.getCanaryWorld());
            CanaryBlock moving = new CanaryBlock((short) Block.b(world.a(i0 + Facing.b[i4], i1 + Facing.c[i4], i2 + Facing.d[i4])), (byte) 0, (i0 + Facing.b[i4]), (i1 + Facing.c[i4]), (i2 + Facing.d[i4]), world.getCanaryWorld());

            //

            if (flag0 && !c(i3)) {
                if (h(world, i0, i1, i2, i4)) {
                    // CanaryMod: PistonExtend
                    PistonExtendHook hook = (PistonExtendHook) new PistonExtendHook(piston, moving).call();
                    if (!hook.isCanceled()) {
                        world.c(i0, i1, i2, this, 0, i4);
                    }
                    //
                }
            }
            else if (!flag0 && c(i3)) {
                // CanaryMod: PistonRetract
                moving = new CanaryBlock((short) Block.b(world.a(i0 + Facing.b[i4] * 2, i1 + Facing.c[i4] * 2, i2 + Facing.d[i4] * 2)), (byte) 0, (i0 + Facing.b[i4]), (i1 + Facing.c[i4]), (i2 + Facing.d[i4]), world.getCanaryWorld());
                PistonRetractHook hook = (PistonRetractHook) new PistonRetractHook(piston, moving).call();
                attemptRetract = !hook.isCanceled();
                //
                world.a(i0, i1, i2, i4, 2);
                world.c(i0, i1, i2, this, 1, i4);
            }
        }
    }

    private boolean a(World world, int i0, int i1, int i2, int i3) {
        return i3 != 0 && world.f(i0, i1 - 1, i2, 0) ? true : (i3 != 1 && world.f(i0, i1 + 1, i2, 1) ? true : (i3 != 2 && world.f(i0, i1, i2 - 1, 2) ? true : (i3 != 3 && world.f(i0, i1, i2 + 1, 3) ? true : (i3 != 5 && world.f(i0 + 1, i1, i2, 5) ? true : (i3 != 4 && world.f(i0 - 1, i1, i2, 4) ? true : (world.f(i0, i1, i2, 0) ? true : (world.f(i0, i1 + 2, i2, 1) ? true : (world.f(i0, i1 + 1, i2 - 1, 2) ? true : (world.f(i0, i1 + 1, i2 + 1, 3) ? true : (world.f(i0 - 1, i1 + 1, i2, 4) ? true : world.f(i0 + 1, i1 + 1, i2, 5)))))))))));
    }

    public boolean a(World world, int i0, int i1, int i2, int i3, int i4) {
        if (!world.E) {
            boolean flag0 = this.a(world, i0, i1, i2, i4);

            if (flag0 && i3 == 1) {
                world.a(i0, i1, i2, i4 | 8, 2);
                return false;
            }

            if (!flag0 && i3 == 0) {
                return false;
            }
        }

        if (i3 == 0) {
            if (!this.i(world, i0, i1, i2, i4)) {
                return false;
            }

            world.a(i0, i1, i2, i4 | 8, 2);
            world.a((double) i0 + 0.5D, (double) i1 + 0.5D, (double) i2 + 0.5D, "tile.piston.out", 0.5F, world.s.nextFloat() * 0.25F + 0.6F);
        }
        else if (i3 == 1) {
            TileEntity tileentity = world.o(i0 + Facing.b[i4], i1 + Facing.c[i4], i2 + Facing.d[i4]);

            if (tileentity instanceof TileEntityPiston) {
                ((TileEntityPiston) tileentity).f();
            }

            world.d(i0, i1, i2, Blocks.M, i4, 3);
            world.a(i0, i1, i2, BlockPistonMoving.a(this, i4, i4, false, true));
            if (this.a) {
                int i5 = i0 + Facing.b[i4] * 2;
                int i6 = i1 + Facing.c[i4] * 2;
                int i7 = i2 + Facing.d[i4] * 2;
                Block block = world.a(i5, i6, i7);
                int i8 = world.e(i5, i6, i7);
                boolean flag1 = false;

                if (block == Blocks.M) {
                    TileEntity tileentity1 = world.o(i5, i6, i7);

                    if (tileentity1 instanceof TileEntityPiston) {
                        TileEntityPiston tileentitypiston = (TileEntityPiston) tileentity1;

                        if (tileentitypiston.c() == i4 && tileentitypiston.b()) {
                            tileentitypiston.f();
                            block = tileentitypiston.a();
                            i8 = tileentitypiston.p();
                            flag1 = true;
                        }
                    }
                }

                // CanaryMod: check attemptRetract
                if (attemptRetract && !flag1 && block.o() != Material.a && a(block, world, i5, i6, i7, false) && (block.h() == 0 || block == Blocks.J || block == Blocks.F)) {
                    i0 += Facing.b[i4];
                    i1 += Facing.c[i4];
                    i2 += Facing.d[i4];
                    world.d(i0, i1, i2, Blocks.M, i8, 3);
                    world.a(i0, i1, i2, BlockPistonMoving.a(block, i8, i4, false, false));
                    world.f(i5, i6, i7);
                }
                else if (!flag1) {
                    world.f(i0 + Facing.b[i4], i1 + Facing.c[i4], i2 + Facing.d[i4]);
                }
            }
            else {
                world.f(i0 + Facing.b[i4], i1 + Facing.c[i4], i2 + Facing.d[i4]);
            }

            world.a((double) i0 + 0.5D, (double) i1 + 0.5D, (double) i2 + 0.5D, "tile.piston.in", 0.5F, world.s.nextFloat() * 0.15F + 0.6F);
        }

        return true;
    }

    public void a(IBlockAccess iblockaccess, int i0, int i1, int i2) {
        int i3 = iblockaccess.e(i0, i1, i2);

        if (c(i3)) {
            float f0 = 0.25F;

            switch (b(i3)) {
                case 0:
                    this.a(0.0F, 0.25F, 0.0F, 1.0F, 1.0F, 1.0F);
                    break;

                case 1:
                    this.a(0.0F, 0.0F, 0.0F, 1.0F, 0.75F, 1.0F);
                    break;

                case 2:
                    this.a(0.0F, 0.0F, 0.25F, 1.0F, 1.0F, 1.0F);
                    break;

                case 3:
                    this.a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.75F);
                    break;

                case 4:
                    this.a(0.25F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
                    break;

                case 5:
                    this.a(0.0F, 0.0F, 0.0F, 0.75F, 1.0F, 1.0F);
            }
        }
        else {
            this.a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public void g() {
        this.a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    public void a(World world, int i0, int i1, int i2, AxisAlignedBB axisalignedbb, List list, Entity entity) {
        this.a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        super.a(world, i0, i1, i2, axisalignedbb, list, entity);
    }

    public AxisAlignedBB a(World world, int i0, int i1, int i2) {
        this.a((IBlockAccess) world, i0, i1, i2);
        return super.a(world, i0, i1, i2);
    }

    public boolean d() {
        return false;
    }

    public static int b(int i0) {
        return i0 & 7;
    }

    public static boolean c(int i0) {
        return (i0 & 8) != 0;
    }

    public static int a(World world, int i0, int i1, int i2, EntityLivingBase entitylivingbase) {
        if (MathHelper.e((float) entitylivingbase.t - (float) i0) < 2.0F && MathHelper.e((float) entitylivingbase.v - (float) i2) < 2.0F) {
            double d0 = entitylivingbase.u + 1.82D - (double) entitylivingbase.M;

            if (d0 - (double) i1 > 2.0D) {
                return 1;
            }

            if ((double) i1 - d0 > 0.0D) {
                return 0;
            }
        }

        int i3 = MathHelper.c((double) (entitylivingbase.z * 4.0F / 360.0F) + 0.5D) & 3;

        return i3 == 0 ? 2 : (i3 == 1 ? 5 : (i3 == 2 ? 3 : (i3 == 3 ? 4 : 0)));
    }

    private static boolean a(Block block, World world, int i0, int i1, int i2, boolean flag0) {
        if (block == Blocks.Z) {
            return false;
        }
        else {
            if (block != Blocks.J && block != Blocks.F) {
                if (block.f(world, i0, i1, i2) == -1.0F) {
                    return false;
                }

                if (block.h() == 2) {
                    return false;
                }

                if (block.h() == 1) {
                    if (!flag0) {
                        return false;
                    }

                    return true;
                }
            }
            else if (c(world.e(i0, i1, i2))) {
                return false;
            }

            return !(block instanceof ITileEntityProvider);
        }
    }

    private static boolean h(World world, int i0, int i1, int i2, int i3) {
        int i4 = i0 + Facing.b[i3];
        int i5 = i1 + Facing.c[i3];
        int i6 = i2 + Facing.d[i3];
        int i7 = 0;

        while (true) {
            if (i7 < 13) {
                if (i5 <= 0 || i5 >= 255) {
                    return false;
                }

                Block block = world.a(i4, i5, i6);

                if (block.o() != Material.a) {
                    if (!a(block, world, i4, i5, i6, true)) {
                        return false;
                    }

                    if (block.h() != 1) {
                        if (i7 == 12) {
                            return false;
                        }

                        i4 += Facing.b[i3];
                        i5 += Facing.c[i3];
                        i6 += Facing.d[i3];
                        ++i7;
                        continue;
                    }
                }
            }

            return true;
        }
    }

    private boolean i(World world, int i0, int i1, int i2, int i3) {
        int i4 = i0 + Facing.b[i3];
        int i5 = i1 + Facing.c[i3];
        int i6 = i2 + Facing.d[i3];
        int i7 = 0;

        while (true) {
            if (i7 < 13) {
                if (i5 <= 0 || i5 >= 255) {
                    return false;
                }

                Block block = world.a(i4, i5, i6);

                if (block.o() != Material.a) {
                    if (!a(block, world, i4, i5, i6, true)) {
                        return false;
                    }

                    if (block.h() != 1) {
                        if (i7 == 12) {
                            return false;
                        }

                        i4 += Facing.b[i3];
                        i5 += Facing.c[i3];
                        i6 += Facing.d[i3];
                        ++i7;
                        continue;
                    }

                    block.b(world, i4, i5, i6, world.e(i4, i5, i6), 0);
                    world.f(i4, i5, i6);
                }
            }

            i7 = i4;
            int i8 = i5;
            int i9 = i6;
            int i10 = 0;

            Block[] ablock;
            int i11;
            int i12;
            int i13 = 0;

            for (ablock = new Block[13]; i4 != i0 || i5 != i1 || i6 != i2; i6 = i13) {
                i11 = i4 - Facing.b[i3];
                i12 = i5 - Facing.c[i3];
                i13 = i6 - Facing.d[i3];
                Block block1 = world.a(i11, i12, i13);
                int i14 = world.e(i11, i12, i13);

                if (block1 == this && i11 == i0 && i12 == i1 && i13 == i2) {
                    world.d(i4, i5, i6, Blocks.M, i3 | (this.a ? 8 : 0), 4);
                    world.a(i4, i5, i6, BlockPistonMoving.a(Blocks.K, i3 | (this.a ? 8 : 0), i3, true, false));
                }
                else {
                    world.d(i4, i5, i6, Blocks.M, i14, 4);
                    world.a(i4, i5, i6, BlockPistonMoving.a(block1, i14, i3, true, false));
                }

                ablock[i10++] = block1;
                i4 = i11;
                i5 = i12;
            }

            i4 = i7;
            i5 = i8;
            i6 = i9;

            for (i10 = 0; i4 != i0 || i5 != i1 || i6 != i2; i6 = i13) {
                i11 = i4 - Facing.b[i3];
                i12 = i5 - Facing.c[i3];
                i13 = i6 - Facing.d[i3];
                world.d(i11, i12, i13, ablock[i10++]);
                i4 = i11;
                i5 = i12;
            }

            return true;
        }
    }
}

