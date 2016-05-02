package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockPortal extends BlockBreakable {

    public static final int[][] a = new int[][]{ new int[0], { 3, 1 }, { 2, 0 } };

    public BlockPortal() {
        super("portal", Material.E, false);
        this.a(true);
    }

    public void a(World world, int i0, int i1, int i2, Random random) {
        super.a(world, i0, i1, i2, random);
        if (world.t.d() && world.N().b("doMobSpawning") && random.nextInt(2000) < world.r.a()) {
            int i3;

            for (i3 = i1; !World.a((IBlockAccess) world, i0, i3, i2) && i3 > 0; --i3) {
                ;
            }

            if (i3 > 0 && !world.a(i0, i3 + 1, i2).r()) {
                Entity entity = ItemMonsterPlacer.a(world, 57, (double) i0 + 0.5D, (double) i3 + 1.1D, (double) i2 + 0.5D);

                if (entity != null) {
                    entity.an = entity.ai();
                }
            }
        }
    }

    public AxisAlignedBB a(World world, int i0, int i1, int i2) {
        return null;
    }

    public void a(IBlockAccess iblockaccess, int i0, int i1, int i2) {
        int i3 = b(iblockaccess.e(i0, i1, i2));

        if (i3 == 0) {
            if (iblockaccess.a(i0 - 1, i1, i2) != this && iblockaccess.a(i0 + 1, i1, i2) != this) {
                i3 = 2;
            }
            else {
                i3 = 1;
            }

            if (iblockaccess instanceof World && !((World) iblockaccess).E) {
                ((World) iblockaccess).a(i0, i1, i2, i3, 2);
            }
        }

        float f0 = 0.125F;
        float f1 = 0.125F;

        if (i3 == 1) {
            f0 = 0.5F;
        }

        if (i3 == 2) {
            f1 = 0.5F;
        }

        this.a(0.5F - f0, 0.0F, 0.5F - f1, 0.5F + f0, 1.0F, 0.5F + f1);
    }

    public boolean d() {
        return false;
    }

    public boolean e(World world, int i0, int i1, int i2) {
        Size blockportal_size = new Size(world, i0, i1, i2, 1);
        Size blockportal_size1 = new Size(world, i0, i1, i2, 2);

        if (blockportal_size.b() && blockportal_size.e == 0) {
            blockportal_size.c();
            return true;
        }
        else if (blockportal_size1.b() && blockportal_size1.e == 0) {
            blockportal_size1.c();
            return true;
        }
        else {
            return false;
        }
    }

    public void a(World world, int i0, int i1, int i2, Block block) {
        int i3 = b(world.e(i0, i1, i2));
        Size blockportal_size = new Size(world, i0, i1, i2, 1);
        Size blockportal_size1 = new Size(world, i0, i1, i2, 2);

        if (i3 == 1 && (!blockportal_size.b() || blockportal_size.e < blockportal_size.h * blockportal_size.g)) {
            world.b(i0, i1, i2, Blocks.a);
        }
        else if (i3 == 2 && (!blockportal_size1.b() || blockportal_size1.e < blockportal_size1.h * blockportal_size1.g)) {
            world.b(i0, i1, i2, Blocks.a);
        }
        else if (i3 == 0 && !blockportal_size.b() && !blockportal_size1.b()) {
            world.b(i0, i1, i2, Blocks.a);
        }

    }

    public int a(Random random) {
        return 0;
    }

    public void a(World world, int i0, int i1, int i2, Entity entity) {
        if (entity.n == null && entity.m == null) {
            entity.ah();
        }
    }

    public static int b(int i0) {
        return i0 & 3;
    }

    public static class Size {
        //TODO: Restore Portal Reconstruct Job/onPortalCreate

        private final World a;
        private final int b;
        private final int c;
        private final int d;
        private int e = 0;
        private ChunkCoordinates f;
        private int g;
        private int h;

        public Size(World i17, int i18, int i19, int i20, int i21) {
            this.a = i17;
            this.b = i21;
            this.d = BlockPortal.a[i21][0];
            this.c = BlockPortal.a[i21][1];

            for (int i10 = i19; i19 > i10 - 21 && i19 > 0 && this.a(i17.a(i18, i19 - 1, i20)); --i19) {
                ;
            }

            int i11 = this.a(i18, i19, i20, this.d) - 1;

            if (i11 >= 0) {
                this.f = new ChunkCoordinates(i18 + i11 * Direction.a[this.d], i19, i20 + i11 * Direction.b[this.d]);
                this.h = this.a(this.f.a, this.f.b, this.f.c, this.c);
                if (this.h < 2 || this.h > 21) {
                    this.f = null;
                    this.h = 0;
                }
            }

            if (this.f != null) {
                this.g = this.a();
            }

        }

        protected int a(int i17, int i18, int i19, int i20) {
            int i10 = Direction.a[i20];
            int i11 = Direction.b[i20];

            int i21;
            Block block;

            for (i21 = 0; i21 < 22; ++i21) {
                block = this.a.a(i17 + i10 * i21, i18, i19 + i11 * i21);
                if (!this.a(block)) {
                    break;
                }

                Block block1 = this.a.a(i17 + i10 * i21, i18 - 1, i19 + i11 * i21);

                if (block1 != Blocks.Z) {
                    break;
                }
            }

            block = this.a.a(i17 + i10 * i21, i18, i19 + i11 * i21);
            return block == Blocks.Z ? i21 : 0;
        }

        protected int a() {
            int i17;
            int i18;
            int i19;
            int i20;

            label56:
            for (this.g = 0; this.g < 21; ++this.g) {
                i17 = this.f.b + this.g;

                for (i18 = 0; i18 < this.h; ++i18) {
                    i19 = this.f.a + i18 * Direction.a[BlockPortal.a[this.b][1]];
                    i20 = this.f.c + i18 * Direction.b[BlockPortal.a[this.b][1]];
                    Block i21 = this.a.a(i19, i17, i20);

                    if (!this.a(i21)) {
                        break label56;
                    }

                    if (i21 == Blocks.aO) {
                        ++this.e;
                    }

                    if (i18 == 0) {
                        i21 = this.a.a(i19 + Direction.a[BlockPortal.a[this.b][0]], i17, i20 + Direction.b[BlockPortal.a[this.b][0]]);
                        if (i21 != Blocks.Z) {
                            break label56;
                        }
                    }
                    else if (i18 == this.h - 1) {
                        i21 = this.a.a(i19 + Direction.a[BlockPortal.a[this.b][1]], i17, i20 + Direction.b[BlockPortal.a[this.b][1]]);
                        if (i21 != Blocks.Z) {
                            break label56;
                        }
                    }
                }
            }

            for (i17 = 0; i17 < this.h; ++i17) {
                i18 = this.f.a + i17 * Direction.a[BlockPortal.a[this.b][1]];
                i19 = this.f.b + this.g;
                i20 = this.f.c + i17 * Direction.b[BlockPortal.a[this.b][1]];
                if (this.a.a(i18, i19, i20) != Blocks.Z) {
                    this.g = 0;
                    break;
                }
            }

            if (this.g <= 21 && this.g >= 3) {
                return this.g;
            }
            else {
                this.f = null;
                this.h = 0;
                this.g = 0;
                return 0;
            }
        }

        protected boolean a(Block i17) {
            return i17.J == Material.a || i17 == Blocks.ab || i17 == Blocks.aO;
        }

        public boolean b() {
            return this.f != null && this.h >= 2 && this.h <= 21 && this.g >= 3 && this.g <= 21;
        }

        public void c() {
            for (int i17 = 0; i17 < this.h; ++i17) {
                int i18 = this.f.a + Direction.a[this.c] * i17;
                int i19 = this.f.c + Direction.b[this.c] * i17;

                for (int i20 = 0; i20 < this.g; ++i20) {
                    int i21 = this.f.b + i20;

                    this.a.d(i18, i21, i19, Blocks.aO, this.b, 2);
                }
            }

        }
    }
}

