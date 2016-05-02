package net.minecraft.block;

import net.canarymod.api.world.blocks.CanaryBlock;
import net.canarymod.hook.world.IgnitionHook;
import net.canarymod.hook.world.IgnitionHook.IgnitionCause;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.Random;

public class BlockStaticLiquid extends BlockLiquid {

    protected BlockStaticLiquid(Material material) {
        super(material);
        this.a(false);
        if (material == Material.i) {
            this.a(true);
        }
    }

    public void a(World world, int i0, int i1, int i2, Block block) {
        super.a(world, i0, i1, i2, block);
        if (world.a(i0, i1, i2) == this) {
            this.n(world, i0, i1, i2);
        }
    }

    private void n(World world, int i0, int i1, int i2) {
        int i3 = world.e(i0, i1, i2);

        world.d(i0, i1, i2, Block.e(Block.b((Block) this) - 1), i3, 2);
        world.a(i0, i1, i2, Block.e(Block.b((Block) this) - 1), this.a(world));
    }

    public void a(World world, int i0, int i1, int i2, Random random) {
        if (this.J == Material.i) {
            int i3 = random.nextInt(3);

            // CanaryMod: Ignition
            CanaryBlock ignited = (CanaryBlock) world.getCanaryWorld().getBlockAt(i0, i1, i2);

            ignited.setStatus((byte) 1); // Lava Status 1
            IgnitionHook hook = (IgnitionHook) new IgnitionHook(ignited, null, null, IgnitionCause.LAVA).call();
            if (hook.isCanceled()) {
                return;
            }
            //

            int i4;
            int i5;

            for (i4 = 0; i4 < i3; ++i4) {
                i0 += random.nextInt(3) - 1;
                ++i1;
                i2 += random.nextInt(3) - 1;
                Block block = world.a(i0, i1, i2);

                if (block.J == Material.a) {
                    if (this.o(world, i0 - 1, i1, i2) || this.o(world, i0 + 1, i1, i2) || this.o(world, i0, i1, i2 - 1) || this.o(world, i0, i1, i2 + 1) || this.o(world, i0, i1 - 1, i2) || this.o(world, i0, i1 + 1, i2)) {
                        world.b(i0, i1, i2, (Block) Blocks.ab);
                        return;
                    }
                }
                else if (block.J.c()) {
                    return;
                }
            }

            if (i3 == 0) {
                i4 = i0;
                i5 = i2;

                for (int i6 = 0; i6 < 3; ++i6) {
                    i0 = i4 + random.nextInt(3) - 1;
                    i2 = i5 + random.nextInt(3) - 1;
                    if (world.c(i0, i1 + 1, i2) && this.o(world, i0, i1, i2)) {
                        world.b(i0, i1 + 1, i2, (Block) Blocks.ab);
                    }
                }
            }
        }
    }

    private boolean o(World world, int i0, int i1, int i2) {
        return world.a(i0, i1, i2).o().h();
    }
}

