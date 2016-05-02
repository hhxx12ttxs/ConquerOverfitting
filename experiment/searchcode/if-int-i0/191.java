package net.minecraft.block;

import net.canarymod.api.CanaryDamageSource;
import net.canarymod.hook.entity.DamageHook;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.Random;

public class BlockCactus extends Block {

    protected BlockCactus() {
        super(Material.A);
        this.a(true);
        this.a(CreativeTabs.c);
    }

    public void a(World world, int i0, int i1, int i2, Random random) {
        if (world.c(i0, i1 + 1, i2)) {
            int i3;

            for (i3 = 1; world.a(i0, i1 - i3, i2) == this; ++i3) {
                ;
            }

            if (i3 < 3) {
                int i4 = world.e(i0, i1, i2);

                if (i4 == 15) {
                    world.b(i0, i1 + 1, i2, (Block) this);
                    world.a(i0, i1, i2, 0, 4);
                    this.a(world, i0, i1 + 1, i2, (Block) this);
                }
                else {
                    world.a(i0, i1, i2, i4 + 1, 4);
                }
            }
        }
    }

    public AxisAlignedBB a(World world, int i0, int i1, int i2) {
        float f0 = 0.0625F;

        return AxisAlignedBB.a().a((double) ((float) i0 + f0), (double) i1, (double) ((float) i2 + f0), (double) ((float) (i0 + 1) - f0), (double) ((float) (i1 + 1) - f0), (double) ((float) (i2 + 1) - f0));
    }

    public boolean d() {
        return false;
    }

    public boolean c() {
        return false;
    }

    public int b() {
        return 13;
    }

    public boolean c(World world, int i0, int i1, int i2) {
        return !super.c(world, i0, i1, i2) ? false : this.j(world, i0, i1, i2);
    }

    public void a(World world, int i0, int i1, int i2, Block block) {
        if (!this.j(world, i0, i1, i2)) {
            world.a(i0, i1, i2, true);
        }
    }

    public boolean j(World world, int i0, int i1, int i2) {
        if (world.a(i0 - 1, i1, i2).o().a()) {
            return false;
        }
        else if (world.a(i0 + 1, i1, i2).o().a()) {
            return false;
        }
        else if (world.a(i0, i1, i2 - 1).o().a()) {
            return false;
        }
        else if (world.a(i0, i1, i2 + 1).o().a()) {
            return false;
        }
        else {
            Block block = world.a(i0, i1 - 1, i2);

            return block == Blocks.aF || block == Blocks.m;
        }
    }

    public void a(World world, int i0, int i1, int i2, Entity entity) {
        // CanaryMod: Damage (Craptus)
        DamageHook hook = (DamageHook) new DamageHook(null, entity.getCanaryEntity(), new CanaryDamageSource(DamageSource.g), 1.0F).call();
        if (!hook.isCanceled()) {
            entity.a((((CanaryDamageSource) hook.getDamageSource()).getHandle()), hook.getDamageDealt());
        }
        //
    }
}

