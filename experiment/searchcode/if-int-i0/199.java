package net.minecraft.server.management;

import net.canarymod.api.CanaryPlayerManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;

public class PlayerManager {

    private final WorldServer a;
    private final List b = new ArrayList();
    private final LongHashMap c = new LongHashMap();
    private final List d = new ArrayList();
    private final List e = new ArrayList();
    private final int f;
    private long g;
    private final int[][] h = new int[][]{ { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 } };

    // CanaryMod
    private CanaryPlayerManager playerManager;

    //
    public PlayerManager(WorldServer worldserver, int i0) {
        if (i0 > 15) {
            throw new IllegalArgumentException("Too big view radius!");
        }
        else if (i0 < 3) {
            throw new IllegalArgumentException("Too small view radius!");
        }
        else {
            this.f = i0;
            this.a = worldserver;
        }
        playerManager = new CanaryPlayerManager(this, worldserver.getCanaryWorld());
    }

    public WorldServer a() {
        return this.a;
    }

    public void b() {
        long i0 = this.a.H();
        int i1;
        PlayerInstance playermanager_playerinstance;

        if (i0 - this.g > 8000L) {
            this.g = i0;

            for (i1 = 0; i1 < this.e.size(); ++i1) {
                playermanager_playerinstance = (PlayerInstance) this.e.get(i1);
                playermanager_playerinstance.b();
                playermanager_playerinstance.a();
            }
        }
        else {
            for (i1 = 0; i1 < this.d.size(); ++i1) {
                playermanager_playerinstance = (PlayerInstance) this.d.get(i1);
                playermanager_playerinstance.b();
            }
        }

        this.d.clear();
        if (this.b.isEmpty()) {
            WorldProvider worldprovider = this.a.t;

            if (!worldprovider.e()) {
                this.a.b.a();
            }
        }
    }

    private PlayerInstance a(int i0, int i1, boolean flag0) {
        long i2 = (long) i0 + 2147483647L | (long) i1 + 2147483647L << 32;
        PlayerInstance playermanager_playerinstance = (PlayerInstance) this.c.a(i2);

        if (playermanager_playerinstance == null && flag0) {
            playermanager_playerinstance = new PlayerInstance(i0, i1);
            this.c.a(i2, playermanager_playerinstance);
            this.e.add(playermanager_playerinstance);
        }

        return playermanager_playerinstance;
    }

    public void a(int i0, int i1, int i2) {
        int i3 = i0 >> 4;
        int i4 = i2 >> 4;
        PlayerInstance playermanager_playerinstance = this.a(i3, i4, false);

        if (playermanager_playerinstance != null) {
            playermanager_playerinstance.a(i0 & 15, i1, i2 & 15);
        }
    }

    public void a(EntityPlayerMP entityplayermp) {
        int i0 = (int) entityplayermp.t >> 4;
        int i1 = (int) entityplayermp.v >> 4;

        entityplayermp.d = entityplayermp.t;
        entityplayermp.e = entityplayermp.v;

        for (int i2 = i0 - this.f; i2 <= i0 + this.f; ++i2) {
            for (int i3 = i1 - this.f; i3 <= i1 + this.f; ++i3) {
                this.a(i2, i3, true).a(entityplayermp);
            }
        }

        this.b.add(entityplayermp);
        this.b(entityplayermp);
    }

    public void b(EntityPlayerMP entityplayermp) {
        ArrayList arraylist = new ArrayList(entityplayermp.f);
        int i0 = 0;
        int i1 = this.f;
        int i2 = (int) entityplayermp.t >> 4;
        int i3 = (int) entityplayermp.v >> 4;
        int i4 = 0;
        int i5 = 0;
        ChunkCoordIntPair chunkcoordintpair = this.a(i2, i3, true).c;

        entityplayermp.f.clear();
        if (arraylist.contains(chunkcoordintpair)) {
            entityplayermp.f.add(chunkcoordintpair);
        }

        int i6;

        for (i6 = 1; i6 <= i1 * 2; ++i6) {
            for (int i7 = 0; i7 < 2; ++i7) {
                int[] aint = this.h[i0++ % 4];

                for (int i8 = 0; i8 < i6; ++i8) {
                    i4 += aint[0];
                    i5 += aint[1];
                    chunkcoordintpair = this.a(i2 + i4, i3 + i5, true).c;
                    if (arraylist.contains(chunkcoordintpair)) {
                        entityplayermp.f.add(chunkcoordintpair);
                    }
                }
            }
        }

        i0 %= 4;

        for (i6 = 0; i6 < i1 * 2; ++i6) {
            i4 += this.h[i0][0];
            i5 += this.h[i0][1];
            chunkcoordintpair = this.a(i2 + i4, i3 + i5, true).c;
            if (arraylist.contains(chunkcoordintpair)) {
                entityplayermp.f.add(chunkcoordintpair);
            }
        }
    }

    public void c(EntityPlayerMP entityplayermp) {
        int i0 = (int) entityplayermp.d >> 4;
        int i1 = (int) entityplayermp.e >> 4;

        for (int i2 = i0 - this.f; i2 <= i0 + this.f; ++i2) {
            for (int i3 = i1 - this.f; i3 <= i1 + this.f; ++i3) {
                PlayerInstance playermanager_playerinstance = this.a(i2, i3, false);

                if (playermanager_playerinstance != null) {
                    playermanager_playerinstance.b(entityplayermp);
                }
            }
        }

        this.b.remove(entityplayermp);
    }

    private boolean a(int i0, int i1, int i2, int i3, int i4) {
        int i5 = i0 - i2;
        int i6 = i1 - i3;

        return i5 >= -i4 && i5 <= i4 ? i6 >= -i4 && i6 <= i4 : false;
    }

    public void d(EntityPlayerMP entityplayermp) {
        int i0 = (int) entityplayermp.t >> 4;
        int i1 = (int) entityplayermp.v >> 4;
        double d0 = entityplayermp.d - entityplayermp.t;
        double d1 = entityplayermp.e - entityplayermp.v;
        double d2 = d0 * d0 + d1 * d1;

        if (d2 >= 64.0D) {
            int i2 = (int) entityplayermp.d >> 4;
            int i3 = (int) entityplayermp.e >> 4;
            int i4 = this.f;
            int i5 = i0 - i2;
            int i6 = i1 - i3;

            if (i5 != 0 || i6 != 0) {
                for (int i7 = i0 - i4; i7 <= i0 + i4; ++i7) {
                    for (int i8 = i1 - i4; i8 <= i1 + i4; ++i8) {
                        if (!this.a(i7, i8, i2, i3, i4)) {
                            this.a(i7, i8, true).a(entityplayermp);
                        }

                        if (!this.a(i7 - i5, i8 - i6, i0, i1, i4)) {
                            PlayerInstance playermanager_playerinstance = this.a(i7 - i5, i8 - i6, false);

                            if (playermanager_playerinstance != null) {
                                playermanager_playerinstance.b(entityplayermp);
                            }
                        }
                    }
                }

                this.b(entityplayermp);
                entityplayermp.d = entityplayermp.t;
                entityplayermp.e = entityplayermp.v;
            }
        }
    }

    public boolean a(EntityPlayerMP entityplayermp, int i0, int i1) {
        PlayerInstance playermanager_playerinstance = this.a(i0, i1, false);

        return playermanager_playerinstance == null ? false : playermanager_playerinstance.b.contains(entityplayermp) && !entityplayermp.f.contains(playermanager_playerinstance.c);
    }

    public static int a(int i0) {
        return i0 * 16 - 16;
    }

    class PlayerInstance {

        private final List b = new ArrayList();
        private final ChunkCoordIntPair c;
        private short[] d = new short[64];
        private int e;
        private int f;
        private long g;

        public PlayerInstance(int packet1, int i10) {
            this.c = new ChunkCoordIntPair(packet1, i10);
            PlayerManager.this.a().b.c(packet1, i10);
        }

        public void a(EntityPlayerMP tileentity) {
            if (this.b.contains(tileentity)) {
                throw new IllegalStateException("Failed to add player. " + tileentity + " already is in chunk " + this.c.a + ", " + this.c.b);
            }
            else {
                if (this.b.isEmpty()) {
                    this.g = PlayerManager.this.a.H();
                }

                this.b.add(tileentity);
                tileentity.f.add(this.c);
            }
        }

        public void b(EntityPlayerMP tileentity) {
            if (this.b.contains(tileentity)) {
                Chunk packet1 = PlayerManager.this.a.e(this.c.a, this.c.b);

                if (packet1.k()) {
                    tileentity.a.a((Packet) (new S21PacketChunkData(packet1, true, 0)));
                }

                this.b.remove(tileentity);
                tileentity.f.remove(this.c);
                if (this.b.isEmpty()) {
                    long i10 = (long) this.c.a + 2147483647L | (long) this.c.b + 2147483647L << 32;

                    this.a(packet1);
                    PlayerManager.this.c.d(i10);
                    PlayerManager.this.e.remove(this);
                    if (this.e > 0) {
                        PlayerManager.this.d.remove(this);
                    }

                    PlayerManager.this.a().b.b(this.c.a, this.c.b);
                }
            }
        }

        public void a() {
            this.a(PlayerManager.this.a.e(this.c.a, this.c.b));
        }

        private void a(Chunk tileentity) {
            tileentity.s += PlayerManager.this.a.H() - this.g;
            this.g = PlayerManager.this.a.H();
        }

        public void a(int tileentity, int packet1, int i10) {
            if (this.e == 0) {
                PlayerManager.this.d.add(this);
            }

            this.f |= 1 << (packet1 >> 4);
            if (this.e < 64) {
                short i11 = (short) (tileentity << 12 | i10 << 8 | packet1);

                for (int list = 0; list < this.e; ++list) {
                    if (this.d[list] == i11) {
                        return;
                    }
                }

                this.d[this.e++] = i11;
            }

        }

        public void a(Packet tileentity) {
            for (int packet1 = 0; packet1 < this.b.size(); ++packet1) {
                EntityPlayerMP i10 = (EntityPlayerMP) this.b.get(packet1);

                if (!i10.f.contains(this.c)) {
                    i10.a.a(tileentity);
                }
            }

        }

        public void b() {
            if (this.e != 0) {
                int tileentity;
                int packet1;
                int i10;

                if (this.e == 1) {
                    tileentity = this.c.a * 16 + (this.d[0] >> 12 & 15);
                    packet1 = this.d[0] & 255;
                    i10 = this.c.b * 16 + (this.d[0] >> 8 & 15);
                    this.a((Packet) (new S23PacketBlockChange(tileentity, packet1, i10, PlayerManager.this.a)));
                    if (PlayerManager.this.a.a(tileentity, packet1, i10).u()) {
                        this.a(PlayerManager.this.a.o(tileentity, packet1, i10));
                    }
                }
                else {
                    int i11;

                    if (this.e == 64) {
                        tileentity = this.c.a * 16;
                        packet1 = this.c.b * 16;
                        this.a((Packet) (new S21PacketChunkData(PlayerManager.this.a.e(this.c.a, this.c.b), false, this.f)));

                        for (i10 = 0; i10 < 16; ++i10) {
                            if ((this.f & 1 << i10) != 0) {
                                i11 = i10 << 4;
                                List list = PlayerManager.this.a.a(tileentity, i11, packet1, tileentity + 16, i11 + 16, packet1 + 16);

                                for (int i12 = 0; i12 < list.size(); ++i12) {
                                    this.a((TileEntity) list.get(i12));
                                }
                            }
                        }
                    }
                    else {
                        this.a((Packet) (new S22PacketMultiBlockChange(this.e, this.d, PlayerManager.this.a.e(this.c.a, this.c.b))));

                        for (tileentity = 0; tileentity < this.e; ++tileentity) {
                            packet1 = this.c.a * 16 + (this.d[tileentity] >> 12 & 15);
                            i10 = this.d[tileentity] & 255;
                            i11 = this.c.b * 16 + (this.d[tileentity] >> 8 & 15);
                            if (PlayerManager.this.a.a(packet1, i10, i11).u()) {
                                this.a(PlayerManager.this.a.o(packet1, i10, i11));
                            }
                        }
                    }
                }

                this.e = 0;
                this.f = 0;
            }
        }

        private void a(TileEntity tileentity) {
            if (tileentity != null) {
                Packet packet1 = tileentity.m();

                if (packet1 != null) {
                    this.a(packet1);
                }
            }

        }
    }

    /**
     * Get the canary player manager
     *
     * @return
     */
    public CanaryPlayerManager getPlayerManager() {
        return playerManager;
    }

    // CanaryMod
    public List<EntityPlayerMP> getManagedPlayers() {
        return b;
    }

    public int getPlayerViewRadius() {
        return f;
    }
    //
}

