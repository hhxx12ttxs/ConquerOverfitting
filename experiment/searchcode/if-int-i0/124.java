package net.minecraft.block;

import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.CanaryBlock;
import net.canarymod.hook.world.RedstoneChangeHook;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Direction;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BlockRedstoneWire extends Block {

    private boolean a = true;
    private Set b = new HashSet();

    public BlockRedstoneWire() {
        super(Material.q);
        this.a(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);
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
        return 5;
    }

    public boolean c(World world, int i0, int i1, int i2) {
        return World.a((IBlockAccess) world, i0, i1 - 1, i2) || world.a(i0, i1 - 1, i2) == Blocks.aN;
    }

    private void e(World world, int i0, int i1, int i2) {
        this.a(world, i0, i1, i2, i0, i1, i2);
        ArrayList arraylist = new ArrayList(this.b);

        this.b.clear();

        for (int i3 = 0; i3 < arraylist.size(); ++i3) {
            ChunkPosition chunkposition = (ChunkPosition) arraylist.get(i3);

            world.d(chunkposition.a, chunkposition.b, chunkposition.c, this);
        }
    }

    private void a(World world, int i0, int i1, int i2, int i3, int i4, int i5) {
        int i6 = world.e(i0, i1, i2);
        byte b0 = 0;
        int i7 = this.a(world, i3, i4, i5, b0);

        this.a = false;
        int i8 = world.w(i0, i1, i2);

        this.a = true;
        if (i8 > 0 && i8 > i7 - 1) {
            i7 = i8;
        }

        int i9 = 0;

        for (int i10 = 0; i10 < 4; ++i10) {
            int i11 = i0;
            int i12 = i2;

            if (i10 == 0) {
                i11 = i0 - 1;
            }

            if (i10 == 1) {
                ++i11;
            }

            if (i10 == 2) {
                i12 = i2 - 1;
            }

            if (i10 == 3) {
                ++i12;
            }

            if (i11 != i3 || i12 != i5) {
                i9 = this.a(world, i11, i1, i12, i9);
            }

            if (world.a(i11, i1, i12).r() && !world.a(i0, i1 + 1, i2).r()) {
                if ((i11 != i3 || i12 != i5) && i1 >= i4) {
                    i9 = this.a(world, i11, i1 + 1, i12, i9);
                }
            }
            else if (!world.a(i11, i1, i12).r() && (i11 != i3 || i12 != i5) && i1 <= i4) {
                i9 = this.a(world, i11, i1 - 1, i12, i9);
            }
        }

        if (i9 > i7) {
            i7 = i9 - 1;
        }
        else if (i7 > 0) {
            --i7;
        }
        else {
            i7 = 0;
        }

        if (i8 > i7 - 1) {
            i7 = i8;
        }

        // CanaryMod: RedstoneChange
        if (i6 != i7) {
            RedstoneChangeHook hook = (RedstoneChangeHook) new RedstoneChangeHook(world.getCanaryWorld().getBlockAt(i0, i1, i2), i6, i7).call();
            if (hook.isCanceled()) {
                return;
            }
        }
        //

        if (i6 != i7) {
            world.a(i0, i1, i2, i7, 2);
            this.b.add(new ChunkPosition(i0, i1, i2));
            this.b.add(new ChunkPosition(i0 - 1, i1, i2));
            this.b.add(new ChunkPosition(i0 + 1, i1, i2));
            this.b.add(new ChunkPosition(i0, i1 - 1, i2));
            this.b.add(new ChunkPosition(i0, i1 + 1, i2));
            this.b.add(new ChunkPosition(i0, i1, i2 - 1));
            this.b.add(new ChunkPosition(i0, i1, i2 + 1));
        }
    }

    private void m(World world, int i0, int i1, int i2) {
        if (world.a(i0, i1, i2) == this) {
            world.d(i0, i1, i2, this);
            world.d(i0 - 1, i1, i2, this);
            world.d(i0 + 1, i1, i2, this);
            world.d(i0, i1, i2 - 1, this);
            world.d(i0, i1, i2 + 1, this);
            world.d(i0, i1 - 1, i2, this);
            world.d(i0, i1 + 1, i2, this);
        }
    }

    public void b(World world, int i0, int i1, int i2) {
        super.b(world, i0, i1, i2);
        if (!world.E) {
            this.e(world, i0, i1, i2);
            world.d(i0, i1 + 1, i2, this);
            world.d(i0, i1 - 1, i2, this);
            this.m(world, i0 - 1, i1, i2);
            this.m(world, i0 + 1, i1, i2);
            this.m(world, i0, i1, i2 - 1);
            this.m(world, i0, i1, i2 + 1);
            if (world.a(i0 - 1, i1, i2).r()) {
                this.m(world, i0 - 1, i1 + 1, i2);
            }
            else {
                this.m(world, i0 - 1, i1 - 1, i2);
            }

            if (world.a(i0 + 1, i1, i2).r()) {
                this.m(world, i0 + 1, i1 + 1, i2);
            }
            else {
                this.m(world, i0 + 1, i1 - 1, i2);
            }

            if (world.a(i0, i1, i2 - 1).r()) {
                this.m(world, i0, i1 + 1, i2 - 1);
            }
            else {
                this.m(world, i0, i1 - 1, i2 - 1);
            }

            if (world.a(i0, i1, i2 + 1).r()) {
                this.m(world, i0, i1 + 1, i2 + 1);
            }
            else {
                this.m(world, i0, i1 - 1, i2 + 1);
            }
        }
    }

    public void a(World world, int i0, int i1, int i2, Block block, int i3) {
        super.a(world, i0, i1, i2, block, i3);
        if (!world.E) {
            // CanaryMod: RedstoneChange (Wire Destroy)
            int lvl = world.w(i0, i1, i2) - 1; // Subtract 1 from current in
            if (lvl > 0) {
                new RedstoneChangeHook(new CanaryBlock(BlockType.RedstoneWire.getId(), (short) i3, i0, i1, i2, world.getCanaryWorld()), lvl, 0).call();
            }
            //
            world.d(i0, i1 + 1, i2, this);
            world.d(i0, i1 - 1, i2, this);
            world.d(i0 + 1, i1, i2, this);
            world.d(i0 - 1, i1, i2, this);
            world.d(i0, i1, i2 + 1, this);
            world.d(i0, i1, i2 - 1, this);
            this.e(world, i0, i1, i2);
            this.m(world, i0 - 1, i1, i2);
            this.m(world, i0 + 1, i1, i2);
            this.m(world, i0, i1, i2 - 1);
            this.m(world, i0, i1, i2 + 1);
            if (world.a(i0 - 1, i1, i2).r()) {
                this.m(world, i0 - 1, i1 + 1, i2);
            }
            else {
                this.m(world, i0 - 1, i1 - 1, i2);
            }

            if (world.a(i0 + 1, i1, i2).r()) {
                this.m(world, i0 + 1, i1 + 1, i2);
            }
            else {
                this.m(world, i0 + 1, i1 - 1, i2);
            }

            if (world.a(i0, i1, i2 - 1).r()) {
                this.m(world, i0, i1 + 1, i2 - 1);
            }
            else {
                this.m(world, i0, i1 - 1, i2 - 1);
            }

            if (world.a(i0, i1, i2 + 1).r()) {
                this.m(world, i0, i1 + 1, i2 + 1);
            }
            else {
                this.m(world, i0, i1 - 1, i2 + 1);
            }
        }
    }

    private int a(World world, int i0, int i1, int i2, int i3) {
        if (world.a(i0, i1, i2) != this) {
            return i3;
        }
        else {
            int i4 = world.e(i0, i1, i2);

            return i4 > i3 ? i4 : i3;
        }
    }

    public void a(World world, int i0, int i1, int i2, Block block) {
        if (!world.E) {
            boolean flag0 = this.c(world, i0, i1, i2);

            if (flag0) {
                this.e(world, i0, i1, i2);
            }
            else {
                this.b(world, i0, i1, i2, 0, 0);
                world.f(i0, i1, i2);
            }

            super.a(world, i0, i1, i2, block);
        }
    }

    public Item a(int i0, Random random, int i1) {
        return Items.ax;
    }

    public int c(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        return !this.a ? 0 : this.b(iblockaccess, i0, i1, i2, i3);
    }

    public int b(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        if (!this.a) {
            return 0;
        }
        else {
            int i4 = iblockaccess.e(i0, i1, i2);

            if (i4 == 0) {
                return 0;
            }
            else if (i3 == 1) {
                return i4;
            }
            else {
                boolean flag0 = g(iblockaccess, i0 - 1, i1, i2, 1) || !iblockaccess.a(i0 - 1, i1, i2).r() && g(iblockaccess, i0 - 1, i1 - 1, i2, -1);
                boolean flag1 = g(iblockaccess, i0 + 1, i1, i2, 3) || !iblockaccess.a(i0 + 1, i1, i2).r() && g(iblockaccess, i0 + 1, i1 - 1, i2, -1);
                boolean flag2 = g(iblockaccess, i0, i1, i2 - 1, 2) || !iblockaccess.a(i0, i1, i2 - 1).r() && g(iblockaccess, i0, i1 - 1, i2 - 1, -1);
                boolean flag3 = g(iblockaccess, i0, i1, i2 + 1, 0) || !iblockaccess.a(i0, i1, i2 + 1).r() && g(iblockaccess, i0, i1 - 1, i2 + 1, -1);

                if (!iblockaccess.a(i0, i1 + 1, i2).r()) {
                    if (iblockaccess.a(i0 - 1, i1, i2).r() && g(iblockaccess, i0 - 1, i1 + 1, i2, -1)) {
                        flag0 = true;
                    }

                    if (iblockaccess.a(i0 + 1, i1, i2).r() && g(iblockaccess, i0 + 1, i1 + 1, i2, -1)) {
                        flag1 = true;
                    }

                    if (iblockaccess.a(i0, i1, i2 - 1).r() && g(iblockaccess, i0, i1 + 1, i2 - 1, -1)) {
                        flag2 = true;
                    }

                    if (iblockaccess.a(i0, i1, i2 + 1).r() && g(iblockaccess, i0, i1 + 1, i2 + 1, -1)) {
                        flag3 = true;
                    }
                }

                return !flag2 && !flag1 && !flag0 && !flag3 && i3 >= 2 && i3 <= 5 ? i4 : (i3 == 2 && flag2 && !flag0 && !flag1 ? i4 : (i3 == 3 && flag3 && !flag0 && !flag1 ? i4 : (i3 == 4 && flag0 && !flag2 && !flag3 ? i4 : (i3 == 5 && flag1 && !flag2 && !flag3 ? i4 : 0))));
            }
        }
    }

    public boolean f() {
        return this.a;
    }

    public static boolean f(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        Block block = iblockaccess.a(i0, i1, i2);

        if (block == Blocks.af) {
            return true;
        }
        else if (!Blocks.aR.e(block)) {
            return block.f() && i3 != -1;
        }
        else {
            int i4 = iblockaccess.e(i0, i1, i2);

            return i3 == (i4 & 3) || i3 == Direction.f[i4 & 3];
        }
    }

    public static boolean g(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        if (f(iblockaccess, i0, i1, i2, i3)) {
            return true;
        }
        else if (iblockaccess.a(i0, i1, i2) == Blocks.aS) {
            int i4 = iblockaccess.e(i0, i1, i2);

            return i3 == (i4 & 3);
        }
        else {
            return false;
        }
    }
}

