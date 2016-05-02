package net.minecraft.server.management;

import net.canarymod.api.GameMode;
import net.canarymod.api.entity.living.humanoid.CanaryPlayer;
import net.canarymod.api.world.blocks.CanaryBlock;
import net.canarymod.hook.player.BlockDestroyHook;
import net.canarymod.hook.player.BlockLeftClickHook;
import net.canarymod.hook.player.GameModeChangeHook;
import net.canarymod.hook.player.ItemUseHook;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;

public class ItemInWorldManager {

    public World a;
    public EntityPlayerMP b;
    private WorldSettings.GameType c;
    private boolean d;
    private int e;
    private int f;
    private int g;
    private int h;
    private int i;
    private boolean j;
    private int k;
    private int l;
    private int m;
    private int n;
    private int o;

    public ItemInWorldManager(World world) {
        this.c = WorldSettings.GameType.NOT_SET;
        this.o = -1;
        this.a = world;
    }

    public void a(WorldSettings.GameType worldsettings_gametype) {
        // CanaryMod: GameModeChangeHook
        if (this.c != worldsettings_gametype) {
            GameModeChangeHook gmch = (GameModeChangeHook) new GameModeChangeHook(this.b.getPlayer(), GameMode.fromId(worldsettings_gametype.a())).call();
            if (gmch.isCanceled()) {
                return; //Blocked mode change
            }
        }
        //
        this.c = worldsettings_gametype;
        worldsettings_gametype.a(this.b.bF);
        this.b.q();
    }

    public WorldSettings.GameType b() {
        return this.c;
    }

    public boolean d() {
        return this.c.d();
    }

    public void b(WorldSettings.GameType worldsettings_gametype) {
        if (this.c == WorldSettings.GameType.NOT_SET) {
            this.c = worldsettings_gametype;
        }

        this.a(this.c);
    }

    public void a() {
        ++this.i;
        float f0;
        int i0;

        if (this.j) {
            int i1 = this.i - this.n;
            Block block = this.a.a(this.k, this.l, this.m);

            if (block.o() == Material.a) {
                this.j = false;
            }
            else {
                f0 = block.a(this.b, this.b.p, this.k, this.l, this.m) * (float) (i1 + 1);
                i0 = (int) (f0 * 10.0F);
                if (i0 != this.o) {
                    this.a.d(this.b.y(), this.k, this.l, this.m, i0);
                    this.o = i0;
                }

                if (f0 >= 1.0F) {
                    this.j = false;
                    this.b(this.k, this.l, this.m);
                }
            }
        }
        else if (this.d) {
            Block block1 = this.a.a(this.f, this.g, this.h);

            if (block1.o() == Material.a) {
                this.a.d(this.b.y(), this.f, this.g, this.h, -1);
                this.o = -1;
                this.d = false;
            }
            else {
                int i2 = this.i - this.e;

                f0 = block1.a(this.b, this.b.p, this.f, this.g, this.h) * (float) (i2 + 1);
                i0 = (int) (f0 * 10.0F);
                if (i0 != this.o) {
                    this.a.d(this.b.y(), this.f, this.g, this.h, i0);
                    this.o = i0;
                }
            }
        }
    }

    public void a(int i0, int i1, int i2, int i3) {
        // CanaryMod: BlockLeftClick
        net.canarymod.api.world.blocks.Block cblock = this.b.getCanaryWorld().getBlockAt(i0, i1, i2);
        BlockLeftClickHook blcHook = (BlockLeftClickHook) new BlockLeftClickHook(this.b.getPlayer(), cblock).call();
        if (blcHook.isCanceled()) {
            if (this.c.d()) { // Fix ghosting for creative players
                this.b.a.a((Packet) (new S23PacketBlockChange(i0, i1, i2, this.a)));
            }
            return;
        }
        //
        if (!this.c.c() || this.b.d(i0, i1, i2)) {
            if (this.d()) {
                if (!this.a.a((EntityPlayer) null, i0, i1, i2, i3)) {
                    this.b(i0, i1, i2);
                }
            }
            else {
                this.a.a((EntityPlayer) null, i0, i1, i2, i3);
                this.e = this.i;
                float f0 = 1.0F;
                Block block = this.a.a(i0, i1, i2);

                if (block.o() != Material.a) {
                    block.a(this.a, i0, i1, i2, (EntityPlayer) this.b);
                    f0 = block.a(this.b, this.b.p, i0, i1, i2);
                }

                if (block.o() != Material.a && f0 >= 1.0F) {
                    this.b(i0, i1, i2);
                }
                else {
                    this.d = true;
                    this.f = i0;
                    this.g = i1;
                    this.h = i2;
                    int i4 = (int) (f0 * 10.0F);

                    this.a.d(this.b.y(), i0, i1, i2, i4);
                    this.o = i4;
                }
            }
        }
    }

    public void a(int i0, int i1, int i2) {
        if (i0 == this.f && i1 == this.g && i2 == this.h) {
            int i3 = this.i - this.e;
            Block block = this.a.a(i0, i1, i2);

            if (block.o() != Material.a) {
                float f0 = block.a(this.b, this.b.p, i0, i1, i2) * (float) (i3 + 1);

                if (f0 >= 0.7F) {
                    this.d = false;
                    this.a.d(this.b.y(), i0, i1, i2, -1);
                    this.b(i0, i1, i2);
                }
                else if (!this.j) {
                    this.d = false;
                    this.j = true;
                    this.k = i0;
                    this.l = i1;
                    this.m = i2;
                    this.n = this.e;
                }
            }
        }
    }

    public void c(int i0, int i1, int i2) { // Cancel
        this.d = false;
        this.a.d(this.b.y(), this.f, this.g, this.h, -1);
    }

    private boolean d(int i0, int i1, int i2) {
        // CanaryMod: BlockDestroyHook
        net.canarymod.api.world.blocks.Block cblock = this.b.getCanaryWorld().getBlockAt(i0, i1, i2);
        BlockDestroyHook hook = (BlockDestroyHook) new BlockDestroyHook(this.b.getPlayer(), cblock).call();
        if (hook.isCanceled()) {
            this.c(i0, i1, i2);
            return false;
        }
        //
        Block block = this.a.a(i0, i1, i2);
        int i3 = this.a.e(i0, i1, i2);

        block.a(this.a, i0, i1, i2, i3, (EntityPlayer) this.b);
        boolean flag0 = this.a.f(i0, i1, i2);

        if (flag0) {
            block.b(this.a, i0, i1, i2, i3);
        }

        return flag0;
    }

    public boolean b(int i0, int i1, int i2) {
        if (this.c.c() && !this.b.d(i0, i1, i2)) {
            return false;
        }
        else if (this.c.d() && this.b.be() != null && this.b.be().b() instanceof ItemSword) {
            return false;
        }
        else {
            Block block = this.a.a(i0, i1, i2);
            int i3 = this.a.e(i0, i1, i2);
            this.a.a(this.b, 2001, i0, i1, i2, Block.b(block) + (this.a.e(i0, i1, i2) << 12));
            boolean flag0 = this.d(i0, i1, i2);

            if (this.d()) {
                this.b.a.a((Packet) (new S23PacketBlockChange(i0, i1, i2, this.a)));
            }
            else {
                ItemStack itemstack = this.b.bD();
                boolean flag1 = this.b.a(block);

                if (itemstack != null) {
                    itemstack.a(this.a, block, i0, i1, i2, this.b);
                    if (itemstack.b == 0) {
                        this.b.bE();
                    }
                }

                if (flag0 && flag1) {
                    block.a(this.a, this.b, i0, i1, i2, i3);
                }
            }

            return flag0;
        }
    }

    // CanaryMod: ItemUseHook
    public boolean itemUsed(CanaryPlayer player, World world, ItemStack itemstack, CanaryBlock clicked) {
        ItemUseHook hook = (ItemUseHook) new ItemUseHook(player, itemstack.getCanaryItem(), clicked).call();
        if (hook.isCanceled()) {
            return false;
        }
        return this.a(player.getHandle(), world, itemstack);
    }
    //

    public boolean a(EntityPlayer entityplayer, World world, ItemStack itemstack) {
        int i0 = itemstack.b;
        int i1 = itemstack.k();
        ItemStack itemstack1 = itemstack.a(world, entityplayer);

        if (itemstack1 == itemstack && (itemstack1 == null || itemstack1.b == i0 && itemstack1.n() <= 0 && itemstack1.k() == i1)) {
            return false;
        }
        else {
            entityplayer.bn.a[entityplayer.bn.c] = itemstack1;
            if (this.d()) {
                itemstack1.b = i0;
                if (itemstack1.g()) {
                    itemstack1.b(i1);
                }
            }

            if (itemstack1.b == 0) {
                entityplayer.bn.a[entityplayer.bn.c] = null;
            }

            if (!entityplayer.bw()) {
                ((EntityPlayerMP) entityplayer).a(entityplayer.bo);
            }

            return true;
        }
    }

    public boolean a(EntityPlayer entityplayer, World world, ItemStack itemstack, int i0, int i1, int i2, int i3, float f0, float f1, float f2) {
        if ((!entityplayer.an() || entityplayer.be() == null) && world.a(i0, i1, i2).a(world, i0, i1, i2, entityplayer, i3, f0, f1, f2)) {
            return true;
        }
        else if (itemstack == null) {
            return false;
        }
        else if (this.d()) {
            int i4 = itemstack.k();
            int i5 = itemstack.b;
            boolean flag0 = itemstack.a(entityplayer, world, i0, i1, i2, i3, f0, f1, f2);

            itemstack.b(i4);
            itemstack.b = i5;
            return flag0;
        }
        else {
            return itemstack.a(entityplayer, world, i0, i1, i2, i3, f0, f1, f2);
        }
    }

    public void a(WorldServer worldserver) {
        this.a = worldserver;
    }
}

