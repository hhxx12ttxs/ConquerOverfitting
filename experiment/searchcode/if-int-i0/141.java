package net.minecraft.world;

import net.canarymod.api.CanaryEntityTracker;
import net.canarymod.api.CanaryPlayerManager;
import net.canarymod.config.Configuration;
import net.canarymod.hook.world.TimeChangeHook;
import net.canarymod.hook.world.WeatherChangeHook;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.material.Material;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S24PacketBlockAction;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.ScoreboardSaveData;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.feature.WorldGeneratorBonusChest;
import net.minecraft.world.storage.ISaveHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class WorldServer extends World {

    private static final Logger a = LogManager.getLogger();
    private final MinecraftServer J;
    private final EntityTracker K;
    private final PlayerManager L;
    private Set M;
    private TreeSet N;
    public ChunkProviderServer b;
    public boolean c;
    private boolean O;
    private int P;
    private final Teleporter Q;
    private final SpawnerAnimals R = new SpawnerAnimals();
    private ServerBlockEventList[] S = new ServerBlockEventList[]{ new ServerBlockEventList(null), new ServerBlockEventList(null) };
    private int T;
    private static final WeightedRandomChestContent[] U = new WeightedRandomChestContent[]{ new WeightedRandomChestContent(Items.y, 0, 1, 3, 10), new WeightedRandomChestContent(Item.a(Blocks.f), 0, 1, 3, 10), new WeightedRandomChestContent(Item.a(Blocks.r), 0, 1, 3, 10), new WeightedRandomChestContent(Items.t, 0, 1, 1, 3), new WeightedRandomChestContent(Items.p, 0, 1, 1, 5), new WeightedRandomChestContent(Items.s, 0, 1, 1, 3), new WeightedRandomChestContent(Items.o, 0, 1, 1, 5), new WeightedRandomChestContent(Items.e, 0, 2, 3, 5), new WeightedRandomChestContent(Items.P, 0, 2, 3, 3), new WeightedRandomChestContent(Item.a(Blocks.s), 0, 1, 3, 10) };
    private List V = new ArrayList();
    private IntHashMap W;

    public WorldServer(MinecraftServer minecraftserver, ISaveHandler isavehandler, String s0, int i0, WorldSettings worldsettings, Profiler profiler) {
        // TODO: WorldProvider: Needs changing so it would get any WorldProvider. Might need to make a mapping/register
        super(isavehandler, s0, worldsettings, WorldProvider.a(i0), profiler, net.canarymod.api.world.DimensionType.fromId(i0));
        this.J = minecraftserver;
        this.K = new EntityTracker(this);
        // CanaryMod: Use our view-distance handling
        this.L = new PlayerManager(this, Configuration.getServerConfig().getViewDistance());
        if (this.W == null) {
            this.W = new IntHashMap();
        }

        if (this.M == null) {
            this.M = new HashSet();
        }

        if (this.N == null) {
            this.N = new TreeSet();
        }

        this.Q = new Teleporter(this);
        this.D = new ServerScoreboard(minecraftserver);
        ScoreboardSaveData scoreboardsavedata = (ScoreboardSaveData) this.z.a(ScoreboardSaveData.class, "scoreboard");

        if (scoreboardsavedata == null) {
            scoreboardsavedata = new ScoreboardSaveData();
            this.z.a("scoreboard", (WorldSavedData) scoreboardsavedata);
        }

        scoreboardsavedata.a(this.D);
        ((ServerScoreboard) this.D).a(scoreboardsavedata);
    }

    @Override
    public void b() {
        super.b();
        if (this.M().t() && this.r != EnumDifficulty.HARD) {
            this.r = EnumDifficulty.HARD;
        }

        this.t.e.b();
        if (this.e()) {
            if (this.N().b("doDaylightCycle")) {
                long i0 = this.x.g() + 24000L;

                // CanaryMod: TimeChangeHook
                TimeChangeHook hook = (TimeChangeHook) new TimeChangeHook(getCanaryWorld(), i0 - i0 % 24000L).call();
                if (!hook.isCanceled()) {
                    this.x.c(i0 - i0 % 24000L);
                }
                //
                this.d();
            }
        }

        this.C.a("mobSpawner");
        if (this.N().b("doMobSpawning")) {
            this.R.a(this, this.G, this.H, this.x.f() % 400L == 0L);
        }

        this.C.c("chunkSource");
        this.v.c();
        int i1 = this.a(1.0F);

        if (i1 != this.j) {
            this.j = i1;
        }

        this.x.b(this.x.f() + 1L);
        if (this.N().b("doDaylightCycle")) {
            // CanaryMod: TimeChangeHook
            TimeChangeHook hook = (TimeChangeHook) new TimeChangeHook(getCanaryWorld(), this.x.g() + 1L).call();
            if (!hook.isCanceled()) {
                this.x.c(this.x.g() + 1L);
            }
            //
        }
        this.C.c("tickPending");
        this.a(false);
        this.C.c("tickBlocks");
        this.g();
        this.C.c("chunkMap");
        this.L.b();
        this.C.c("village");
        this.A.a();
        this.B.a();
        this.C.c("portalForcer");
        this.Q.a(this.H());
        this.C.b();
        this.Z();
    }

    public BiomeGenBase.SpawnListEntry a(EnumCreatureType enumcreaturetype, int i0, int i1, int i2) {
        List list = this.K().a(enumcreaturetype, i0, i1, i2);

        return list != null && !list.isEmpty() ? (BiomeGenBase.SpawnListEntry) WeightedRandom.a(this.s, (Collection) list) : null;
    }

    public void c() {
        this.O = !this.h.isEmpty();
        Iterator iterator = this.h.iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            if (!entityplayer.bm()) {
                this.O = false;
                break;
            }
        }
    }

    protected void d() {
        this.O = false;
        Iterator iterator = this.h.iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            if (entityplayer.bm()) {
                entityplayer.a(false, false, true);
            }
        }

        this.Y();
    }

    private void Y() {
        // CanaryMod: WeatherChange
        WeatherChangeHook hook = (WeatherChangeHook) new WeatherChangeHook(getCanaryWorld(), false, false).call();
        if (!hook.isCanceled()) {
            this.x.g(0);
            this.x.b(false);
        }
        hook = (WeatherChangeHook) new WeatherChangeHook(getCanaryWorld(), false, true).call();
        if (!hook.isCanceled()) {
            this.x.f(0);
            this.x.a(false);
        }
        //
    }

    public boolean e() {
        if (this.O && !this.E) {
            Iterator iterator = this.h.iterator();

            EntityPlayer entityplayer;

            do {
                if (!iterator.hasNext()) {
                    return true;
                }

                entityplayer = (EntityPlayer) iterator.next();
            } while (entityplayer.bJ());

            return false;
        }
        else {
            return false;
        }
    }

    protected void g() {
        super.g();
        int i0 = 0;
        int i1 = 0;
        Iterator iterator = this.F.iterator();

        while (iterator.hasNext()) {
            ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair) iterator.next();
            int i2 = chunkcoordintpair.a * 16;
            int i3 = chunkcoordintpair.b * 16;

            this.C.a("getChunk");
            Chunk chunk = this.e(chunkcoordintpair.a, chunkcoordintpair.b);

            this.a(i2, i3, chunk);
            this.C.c("tickChunk");
            chunk.b(false);
            this.C.c("thunder");
            int i4;
            int i5;
            int i6;
            int i7;

            if (this.s.nextInt(100000) == 0 && this.P() && this.O()) {
                this.k = this.k * 3 + 1013904223;
                i4 = this.k >> 2;
                i5 = i2 + (i4 & 15);
                i6 = i3 + (i4 >> 8 & 15);
                i7 = this.h(i5, i6);
                if (this.y(i5, i7, i6)) {
                    this.c(new EntityLightningBolt(this, (double) i5, (double) i7, (double) i6));
                }
            }

            this.C.c("iceandsnow");
            if (this.s.nextInt(16) == 0) {
                this.k = this.k * 3 + 1013904223;
                i4 = this.k >> 2;
                i5 = i4 & 15;
                i6 = i4 >> 8 & 15;
                i7 = this.h(i5 + i2, i6 + i3);
                if (this.s(i5 + i2, i7 - 1, i6 + i3)) {
                    this.b(i5 + i2, i7 - 1, i6 + i3, Blocks.aD);
                }

                if (this.P() && this.e(i5 + i2, i7, i6 + i3, true)) {
                    this.b(i5 + i2, i7, i6 + i3, Blocks.aC);
                }

                if (this.P()) {
                    BiomeGenBase biomegenbase = this.a(i5 + i2, i6 + i3);

                    if (biomegenbase.e()) {
                        this.a(i5 + i2, i7 - 1, i6 + i3).l(this, i5 + i2, i7 - 1, i6 + i3);
                    }
                }
            }

            this.C.c("tickBlocks");
            ExtendedBlockStorage[] aextendedblockstorage = chunk.i();

            i5 = aextendedblockstorage.length;

            for (i6 = 0; i6 < i5; ++i6) {
                ExtendedBlockStorage extendedblockstorage = aextendedblockstorage[i6];

                if (extendedblockstorage != null && extendedblockstorage.b()) {
                    for (int i8 = 0; i8 < 3; ++i8) {
                        this.k = this.k * 3 + 1013904223;
                        int i9 = this.k >> 2;
                        int i10 = i9 & 15;
                        int i11 = i9 >> 8 & 15;
                        int i12 = i9 >> 16 & 15;

                        ++i1;
                        Block block = extendedblockstorage.a(i10, i12, i11);

                        if (block.t()) {
                            ++i0;
                            block.a(this, i10 + i2, i12 + extendedblockstorage.d(), i11 + i3, this.s);
                        }
                    }
                }
            }

            this.C.b();
        }
    }

    public boolean a(int i0, int i1, int i2, Block block) {
        NextTickListEntry nextticklistentry = new NextTickListEntry(i0, i1, i2, block);

        return this.V.contains(nextticklistentry);
    }

    public void a(int i0, int i1, int i2, Block block, int i3) {
        this.a(i0, i1, i2, block, i3, 0);
    }

    public void a(int i0, int i1, int i2, Block block, int i3, int i4) {
        NextTickListEntry nextticklistentry = new NextTickListEntry(i0, i1, i2, block);
        byte b0 = 0;

        if (this.d && block.o() != Material.a) {
            if (block.L()) {
                b0 = 8;
                if (this.b(nextticklistentry.a - b0, nextticklistentry.b - b0, nextticklistentry.c - b0, nextticklistentry.a + b0, nextticklistentry.b + b0, nextticklistentry.c + b0)) {
                    Block block1 = this.a(nextticklistentry.a, nextticklistentry.b, nextticklistentry.c);

                    if (block1.o() != Material.a && block1 == nextticklistentry.a()) {
                        block1.a(this, nextticklistentry.a, nextticklistentry.b, nextticklistentry.c, this.s);
                    }
                }

                return;
            }

            i3 = 1;
        }

        if (this.b(i0 - b0, i1 - b0, i2 - b0, i0 + b0, i1 + b0, i2 + b0)) {
            if (block.o() != Material.a) {
                nextticklistentry.a((long) i3 + this.x.f());
                nextticklistentry.a(i4);
            }

            if (!this.M.contains(nextticklistentry)) {
                this.M.add(nextticklistentry);
                this.N.add(nextticklistentry);
            }
        }
    }

    public void b(int i0, int i1, int i2, Block block, int i3, int i4) {
        NextTickListEntry nextticklistentry = new NextTickListEntry(i0, i1, i2, block);

        nextticklistentry.a(i4);
        if (block.o() != Material.a) {
            nextticklistentry.a((long) i3 + this.x.f());
        }

        if (!this.M.contains(nextticklistentry)) {
            this.M.add(nextticklistentry);
            this.N.add(nextticklistentry);
        }
    }

    public void h() {
        if (this.h.isEmpty()) {
            if (this.P++ >= 1200) {
                return;
            }
        }
        else {
            this.i();
        }

        super.h();
    }

    public void i() {
        this.P = 0;
    }

    public boolean a(boolean flag0) {
        int i0 = this.N.size();

        if (i0 != this.M.size()) {
            throw new IllegalStateException("TickNextTick list out of synch");
        }
        else {
            if (i0 > 1000) {
                i0 = 1000;
            }

            this.C.a("cleaning");

            NextTickListEntry nextticklistentry;

            for (int i1 = 0; i1 < i0; ++i1) {
                nextticklistentry = (NextTickListEntry) this.N.first();
                if (!flag0 && nextticklistentry.d > this.x.f()) {
                    break;
                }

                this.N.remove(nextticklistentry);
                this.M.remove(nextticklistentry);
                this.V.add(nextticklistentry);
            }

            this.C.b();
            this.C.a("ticking");
            Iterator iterator = this.V.iterator();

            while (iterator.hasNext()) {
                nextticklistentry = (NextTickListEntry) iterator.next();
                iterator.remove();
                byte b0 = 0;

                if (this.b(nextticklistentry.a - b0, nextticklistentry.b - b0, nextticklistentry.c - b0, nextticklistentry.a + b0, nextticklistentry.b + b0, nextticklistentry.c + b0)) {
                    Block block = this.a(nextticklistentry.a, nextticklistentry.b, nextticklistentry.c);

                    if (block.o() != Material.a && Block.a(block, nextticklistentry.a())) {
                        try {
                            block.a(this, nextticklistentry.a, nextticklistentry.b, nextticklistentry.c, this.s);
                        }
                        catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.a(throwable, "Exception while ticking a block");
                            CrashReportCategory crashreportcategory = crashreport.a("Block being ticked");

                            int i2;

                            try {
                                i2 = this.e(nextticklistentry.a, nextticklistentry.b, nextticklistentry.c);
                            }
                            catch (Throwable throwable1) {
                                i2 = -1;
                            }

                            CrashReportCategory.a(crashreportcategory, nextticklistentry.a, nextticklistentry.b, nextticklistentry.c, block, i2);
                            throw new ReportedException(crashreport);
                        }
                    }
                }
                else {
                    this.a(nextticklistentry.a, nextticklistentry.b, nextticklistentry.c, nextticklistentry.a(), 0);
                }
            }

            this.C.b();
            this.V.clear();
            return !this.N.isEmpty();
        }
    }

    public List a(Chunk chunk, boolean flag0) {
        ArrayList arraylist = null;
        ChunkCoordIntPair chunkcoordintpair = chunk.l();
        int i0 = (chunkcoordintpair.a << 4) - 2;
        int i1 = i0 + 16 + 2;
        int i2 = (chunkcoordintpair.b << 4) - 2;
        int i3 = i2 + 16 + 2;

        for (int i4 = 0; i4 < 2; ++i4) {
            Iterator iterator;

            if (i4 == 0) {
                iterator = this.N.iterator();
            }
            else {
                iterator = this.V.iterator();
                if (!this.V.isEmpty()) {
                    a.debug("toBeTicked = " + this.V.size());
                }
            }

            while (iterator.hasNext()) {
                NextTickListEntry nextticklistentry = (NextTickListEntry) iterator.next();

                if (nextticklistentry.a >= i0 && nextticklistentry.a < i1 && nextticklistentry.c >= i2 && nextticklistentry.c < i3) {
                    if (flag0) {
                        this.M.remove(nextticklistentry);
                        iterator.remove();
                    }

                    if (arraylist == null) {
                        arraylist = new ArrayList();
                    }

                    arraylist.add(nextticklistentry);
                }
            }
        }

        return arraylist;
    }

    public void a(Entity entity, boolean flag0) {
        /* CanaryMod: Spawning checks per world, see World#canSpawn */
        if (!canSpawn(entity)) {
            entity.B();
        }

/* REMOVED
        if (!this.J.X() && (entity instanceof EntityAnimal || entity instanceof EntityWaterMob)) {
            entity.B();
        }

        if (!this.J.Y() && entity instanceof INpc) {
            entity.B();
        }
*/
        super.a(entity, flag0);
    }

    protected IChunkProvider j() {
        IChunkLoader ichunkloader = this.w.a(this.t);

        this.b = new ChunkProviderServer(this, ichunkloader, this.t.c());
        return this.b;
    }

    public List a(int i0, int i1, int i2, int i3, int i4, int i5) {
        ArrayList arraylist = new ArrayList();

        for (int i6 = 0; i6 < this.g.size(); ++i6) {
            TileEntity tileentity = (TileEntity) this.g.get(i6);

            if (tileentity.c >= i0 && tileentity.d >= i1 && tileentity.e >= i2 && tileentity.c < i3 && tileentity.d < i4 && tileentity.e < i5) {
                arraylist.add(tileentity);
            }
        }

        return arraylist;
    }

    public boolean a(EntityPlayer entityplayer, int i0, int i1, int i2) {
        return !this.J.a(this, i0, i1, i2, entityplayer);
    }

    protected void a(WorldSettings worldsettings) {
        if (this.W == null) {
            this.W = new IntHashMap();
        }

        if (this.M == null) {
            this.M = new HashSet();
        }

        if (this.N == null) {
            this.N = new TreeSet();
        }

        this.b(worldsettings);
        super.a(worldsettings);
    }

    protected void b(WorldSettings worldsettings) {
        if (!this.t.e()) {
            this.x.a(0, this.t.i(), 0);
        }
        else {
            this.y = true;
            WorldChunkManager worldchunkmanager = this.t.e;
            List list = worldchunkmanager.a();
            Random random = new Random(this.G());
            ChunkPosition chunkposition = worldchunkmanager.a(0, 0, 256, list, random);
            int i0 = 0;
            int i1 = this.t.i();
            int i2 = 0;

            if (chunkposition != null) {
                i0 = chunkposition.a;
                i2 = chunkposition.c;
            }
            else {
                a.warn("Unable to find spawn biome");
            }

            int i3 = 0;

            while (!this.t.a(i0, i2)) {
                i0 += random.nextInt(64) - random.nextInt(64);
                i2 += random.nextInt(64) - random.nextInt(64);
                ++i3;
                if (i3 == 1000) {
                    break;
                }
            }

            this.x.a(i0, i1, i2);
            this.y = false;
            if (worldsettings.c()) {
                this.k();
            }
        }
    }

    protected void k() {
        WorldGeneratorBonusChest worldgeneratorbonuschest = new WorldGeneratorBonusChest(U, 10);

        for (int i0 = 0; i0 < 10; ++i0) {
            int i1 = this.x.c() + this.s.nextInt(6) - this.s.nextInt(6);
            int i2 = this.x.e() + this.s.nextInt(6) - this.s.nextInt(6);
            int i3 = this.i(i1, i2) + 1;

            if (worldgeneratorbonuschest.a(this, this.s, i1, i3, i2)) {
                break;
            }
        }
    }

    public ChunkCoordinates l() {
        return this.t.h();
    }

    public void a(boolean flag0, IProgressUpdate iprogressupdate) throws MinecraftException {
        // CanaryMod assume every world is able to save
        if (this.v.d()) {
            if (iprogressupdate != null) {
                iprogressupdate.a("Saving level");
            }

            this.a();
            if (iprogressupdate != null) {
                iprogressupdate.c("Saving chunks");
            }

            this.v.a(flag0, iprogressupdate);
        }
    }

    public void m() {
        if (this.v.d()) {
            this.v.b();
        }
    }

    protected void a() throws MinecraftException {
        this.F();
        this.w.a(this.x, this.J.af().q());
        this.z.a();
    }

    protected void a(Entity entity) {
        super.a(entity);
        this.W.a(entity.y(), entity);
        Entity[] aentity = entity.at();

        if (aentity != null) {
            for (int i0 = 0; i0 < aentity.length; ++i0) {
                this.W.a(aentity[i0].y(), aentity[i0]);
            }
        }
    }

    protected void b(Entity entity) {
        super.b(entity);
        this.W.d(entity.y());
        Entity[] aentity = entity.at();

        if (aentity != null) {
            for (int i0 = 0; i0 < aentity.length; ++i0) {
                this.W.d(aentity[i0].y());
            }
        }
    }

    public Entity a(int i0) {
        return (Entity) this.W.a(i0);
    }

    public boolean c(Entity entity) {
        if (super.c(entity)) {
            this.J.af().a(entity.t, entity.u, entity.v, 512.0D, this.t.i, new S2CPacketSpawnGlobalEntity(entity));
            return true;
        }
        else {
            return false;
        }
    }

    public void a(Entity entity, byte b0) {
        this.q().b(entity, new S19PacketEntityStatus(entity, b0));
    }

    public Explosion a(Entity entity, double d0, double d1, double d2, float f0, boolean flag0, boolean flag1) {
        Explosion explosion = new Explosion(this, entity, d0, d1, d2, f0);

        explosion.a = flag0;
        explosion.b = flag1;
        explosion.a();
        explosion.a(false);
        if (!flag1) {
            explosion.h.clear();
        }

        Iterator iterator = this.h.iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            if (entityplayer.e(d0, d1, d2) < 4096.0D) {
                ((EntityPlayerMP) entityplayer).a.a((Packet) (new S27PacketExplosion(d0, d1, d2, f0, explosion.h, (Vec3) explosion.b().get(entityplayer))));
            }
        }

        return explosion;
    }

    public void c(int i0, int i1, int i2, Block block, int i3, int i4) {
        BlockEventData blockeventdata = new BlockEventData(i0, i1, i2, block, i3, i4);
        Iterator iterator = this.S[this.T].iterator();

        BlockEventData blockeventdata1;

        do {
            if (!iterator.hasNext()) {
                this.S[this.T].add(blockeventdata);
                return;
            }

            blockeventdata1 = (BlockEventData) iterator.next();
        } while (!blockeventdata1.equals(blockeventdata));

    }

    private void Z() {
        while (!this.S[this.T].isEmpty()) {
            int i0 = this.T;

            this.T ^= 1;
            Iterator iterator = this.S[i0].iterator();

            while (iterator.hasNext()) {
                BlockEventData blockeventdata = (BlockEventData) iterator.next();

                if (this.a(blockeventdata)) {
                    this.J.af().a((double) blockeventdata.a(), (double) blockeventdata.b(), (double) blockeventdata.c(), 64.0D, this.t.i, new S24PacketBlockAction(blockeventdata.a(), blockeventdata.b(), blockeventdata.c(), blockeventdata.f(), blockeventdata.d(), blockeventdata.e()));
                }
            }

            this.S[i0].clear();
        }
    }

    private boolean a(BlockEventData blockeventdata) {
        Block block = this.a(blockeventdata.a(), blockeventdata.b(), blockeventdata.c());

        return block == blockeventdata.f() ? block.a(this, blockeventdata.a(), blockeventdata.b(), blockeventdata.c(), blockeventdata.d(), blockeventdata.e()) : false;
    }

    public void n() {
        this.w.a();
    }

    protected void o() {
        boolean flag0 = this.P();

        super.o();
        if (this.m != this.n) {
            // CanaryMod: method change
            this.J.af().sendPacketToDimension((Packet) (new S2BPacketChangeGameState(7, this.n)), getCanaryWorld().getName(), this.t.i);
        }
        if (this.o != this.p) {
            // CanaryMod: method change
            this.J.af().sendPacketToDimension((Packet) (new S2BPacketChangeGameState(8, this.p)), getCanaryWorld().getName(), this.t.i);
        }
        if (flag0 != this.P()) {
            if (flag0) {
                this.J.af().a((Packet) (new S2BPacketChangeGameState(2, 0.0F)));
            }
            else {
                this.J.af().a((Packet) (new S2BPacketChangeGameState(1, 0.0F)));
            }
            this.J.af().a((Packet) (new S2BPacketChangeGameState(7, this.n)));
            this.J.af().a((Packet) (new S2BPacketChangeGameState(8, this.p)));
        }
    }

    public MinecraftServer p() {
        return this.J;
    }

    public EntityTracker q() {
        return this.K;
    }

    public PlayerManager s() {
        return this.L;
    }

    public Teleporter t() {
        return this.Q;
    }

    public void a(String s0, double d0, double d1, double d2, int i0, double d3, double d4, double d5, double d6) {
        S2APacketParticles s2apacketparticles = new S2APacketParticles(s0, (float) d0, (float) d1, (float) d2, (float) d3, (float) d4, (float) d5, (float) d6, i0);

        for (int i1 = 0; i1 < this.h.size(); ++i1) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP) this.h.get(i1);
            ChunkCoordinates chunkcoordinates = entityplayermp.f_();
            double d7 = d0 - (double) chunkcoordinates.a;
            double d8 = d1 - (double) chunkcoordinates.b;
            double d9 = d2 - (double) chunkcoordinates.c;
            double d10 = d7 * d7 + d8 * d8 + d9 * d9;

            if (d10 <= 256.0D) {
                entityplayermp.a.a((Packet) s2apacketparticles);
            }
        }

    }

    static class ServerBlockEventList extends ArrayList {

        private ServerBlockEventList() {
        }

        ServerBlockEventList(Object object) {
            this();
        }
    }

    public CanaryEntityTracker getEntityTracker() {
        return K.getCanaryEntityTracker();
    }

    /**
     * Get the canary player manager wrapper for this dimension
     *
     * @return
     */
    public CanaryPlayerManager getPlayerManager() {
        return L.getPlayerManager();
    }
}

