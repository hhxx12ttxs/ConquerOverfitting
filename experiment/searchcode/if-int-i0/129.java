package net.minecraft.block;

import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.CanaryBlock;
import net.canarymod.hook.world.RedstoneChangeHook;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityComparator;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockRedstoneComparator extends BlockRedstoneDiode implements ITileEntityProvider {

    public BlockRedstoneComparator(boolean flag0) {
        super(flag0);
        this.A = true;
    }

    public Item a(int i0, Random random, int i1) {
        return Items.bS;
    }

    protected int b(int i0) {
        return 2;
    }

    protected BlockRedstoneDiode e() {
        return Blocks.bV;
    }

    protected BlockRedstoneDiode i() {
        return Blocks.bU;
    }

    public int b() {
        return 37;
    }

    protected boolean c(int i0) {
        return this.a || (i0 & 8) != 0;
    }

    protected int f(IBlockAccess iblockaccess, int i0, int i1, int i2, int i3) {
        return this.e(iblockaccess, i0, i1, i2).a();
    }

    private int j(World world, int i0, int i1, int i2, int i3) {
        return !this.d(i3) ? this.h(world, i0, i1, i2, i3) : Math.max(this.h(world, i0, i1, i2, i3) - this.h(world, i0, i1, i2, i3), 0);
    }

    public boolean d(int i0) {
        return (i0 & 4) == 4;
    }

    protected boolean a(World world, int i0, int i1, int i2, int i3) {
        int i4 = this.h(world, i0, i1, i2, i3);

        if (i4 >= 15) {
            return true;
        }
        else if (i4 == 0) {
            return false;
        }
        else {
            int i5 = this.h((IBlockAccess) world, i0, i1, i2, i3); // CanaryMod: Cast World to IBlockAccess for method f

            return i5 == 0 ? true : i4 >= i5;
        }
    }

    protected int h(World world, int i0, int i1, int i2, int i3) {
        int i4 = super.h(world, i0, i1, i2, i3);
        int i5 = l(i3);
        int i6 = i0 + Direction.a[i5];
        int i7 = i2 + Direction.b[i5];
        Block block = world.a(i6, i1, i7);

        if (block.M()) {
            i4 = block.g(world, i6, i1, i7, Direction.f[i5]);
        }
        else if (i4 < 15 && block.r()) {
            i6 += Direction.a[i5];
            i7 += Direction.b[i5];
            block = world.a(i6, i1, i7);
            if (block.M()) {
                i4 = block.g(world, i6, i1, i7, Direction.f[i5]);
            }
        }

        return i4;
    }

    public TileEntityComparator e(IBlockAccess iblockaccess, int i0, int i1, int i2) {
        return (TileEntityComparator) iblockaccess.o(i0, i1, i2);
    }

    public boolean a(World world, int i0, int i1, int i2, EntityPlayer entityplayer, int i3, float f0, float f1, float f2) {
        int i4 = world.e(i0, i1, i2);
        boolean flag0 = this.a | (i4 & 8) != 0;
        boolean flag1 = !this.d(i4);
        int i5 = flag1 ? 4 : 0;

        i5 |= flag0 ? 8 : 0;
        world.a((double) i0 + 0.5D, (double) i1 + 0.5D, (double) i2 + 0.5D, "random.click", 0.3F, flag1 ? 0.55F : 0.5F);
        world.a(i0, i1, i2, i5 | i4 & 3, 2);
        this.c(world, i0, i1, i2, world.s);
        return true;
    }

    protected void b(World world, int i0, int i1, int i2, Block block) {
        if (!world.a(i0, i1, i2, (Block) this)) {
            int i3 = world.e(i0, i1, i2);
            int i4 = this.j(world, i0, i1, i2, i3);
            int i5 = this.e((IBlockAccess) world, i0, i1, i2).a();

            if (i4 != i5 || this.c(i3) != this.a(world, i0, i1, i2, i3)) {
                if (this.i(world, i0, i1, i2, i3)) {
                    world.a(i0, i1, i2, this, this.b(0), -1);
                }
                else {
                    world.a(i0, i1, i2, this, this.b(0), 0);
                }
            }
        }
    }

    private void c(World world, int i0, int i1, int i2, Random random) {
        int i3 = world.e(i0, i1, i2);
        int i4 = this.j(world, i0, i1, i2, i3);
        int i5 = this.e((IBlockAccess) world, i0, i1, i2).a();

        this.e((IBlockAccess) world, i0, i1, i2).a(i4);
        if (i5 != i4 || !this.d(i3)) {
            // CanaryMod: RedstoneChange; Comparator change
            RedstoneChangeHook hook = (RedstoneChangeHook) new RedstoneChangeHook(world.getCanaryWorld().getBlockAt(i0, i1, i2), i5, i4).call();
            if (hook.isCanceled()) {
                return;
            }
            //
            boolean flag0 = this.a(world, i0, i1, i2, i3);
            boolean flag1 = this.a || (i3 & 8) != 0;

            if (flag1 && !flag0) {
                world.a(i0, i1, i2, i3 & -9, 2);
            }
            else if (!flag1 && flag0) {
                world.a(i0, i1, i2, i3 | 8, 2);
            }

            this.e(world, i0, i1, i2);
        }
    }

    public void a(World world, int i0, int i1, int i2, Random random) {
        if (this.a) {
            int i3 = world.e(i0, i1, i2);

            world.d(i0, i1, i2, this.i(), i3 | 8, 4);
        }

        this.c(world, i0, i1, i2, random);
    }

    public void b(World world, int i0, int i1, int i2) {
        super.b(world, i0, i1, i2);
        world.a(i0, i1, i2, this.a(world, 0));
    }

    public void a(World world, int i0, int i1, int i2, Block block, int i3) {
        // CanaryMod: Comparator break
        int oldLvl = this.e((IBlockAccess) world, i0, i1, i2).a();
        if (oldLvl != 0) {
            new RedstoneChangeHook(new CanaryBlock(BlockType.RedstoneComparator.getId(), (short) 2, i0, i1, i2, world.getCanaryWorld()), oldLvl, 0).call();
        }
        //
        super.a(world, i0, i1, i2, block, i3);
        world.p(i0, i1, i2);
        this.e(world, i0, i1, i2);
    }

    public boolean a(World world, int i0, int i1, int i2, int i3, int i4) {
        super.a(world, i0, i1, i2, i3, i4);
        TileEntity tileentity = world.o(i0, i1, i2);

        return tileentity != null ? tileentity.c(i3, i4) : false;
    }

    public TileEntity a(World world, int i0) {
        return new TileEntityComparator();
    }
}

