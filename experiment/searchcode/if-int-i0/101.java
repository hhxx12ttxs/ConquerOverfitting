package net.minecraft.world;

import net.canarymod.api.entity.living.humanoid.CanaryHuman;
import net.canarymod.api.entity.vehicle.CanaryVehicle;
import net.canarymod.api.world.CanaryWorld;
import net.canarymod.api.world.blocks.CanaryBlock;
import net.canarymod.config.Configuration;
import net.canarymod.config.WorldConfiguration;
import net.canarymod.hook.entity.EntitySpawnHook;
import net.canarymod.hook.entity.VehicleCollisionHook;
import net.canarymod.hook.world.BlockUpdateHook;
import net.canarymod.hook.world.TimeChangeHook;
import net.canarymod.hook.world.WeatherChangeHook;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.command.IEntitySelector;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.INpc;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Direction;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3Pool;
import net.minecraft.village.VillageCollection;
import net.minecraft.village.VillageSiege;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

public abstract class World implements IBlockAccess {

    public boolean d;
    public List e = new ArrayList();
    protected List f = new ArrayList();
    public List g = new ArrayList();
    private List a = new ArrayList();
    private List b = new ArrayList();
    public List h = new ArrayList();
    public List i = new ArrayList();
    private long c = 16777215L;
    public int j;
    protected int k = (new Random()).nextInt();
    protected final int l = 1013904223;
    protected float m;
    protected float n;
    protected float o;
    protected float p;
    public int q;
    public EnumDifficulty r;
    public Random s = new Random();
    public final WorldProvider t;
    protected List u = new ArrayList();
    protected IChunkProvider v;
    protected final ISaveHandler w;
    public WorldInfo x; // CanaryMod: protected => public
    public boolean y;
    public MapStorage z;
    public final VillageCollection A;
    protected final VillageSiege B = new VillageSiege(this);
    public final Profiler C;
    private final Vec3Pool J = new Vec3Pool(300, 2000);
    private final Calendar K = Calendar.getInstance();
    public Scoreboard D = new Scoreboard(); // Protected => public
    public boolean E;
    protected Set F = new HashSet();
    private int L;
    protected boolean G;
    protected boolean H;
    private ArrayList M;
    private boolean N;
    int[] I;

    // CanaryMod: multiworld
    protected CanaryWorld canaryDimension;

    public BiomeGenBase a(final int i0, final int i1) {
        if (this.d(i0, 0, i1)) {
            Chunk chunk = this.d(i0, i1);

            try {
                return chunk.a(i0 & 15, i1 & 15, this.t.e);
            }
            catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Getting biome");
                CrashReportCategory crashreportcategory = crashreport.a("Coordinates of biome request");

                crashreportcategory.a("Location", new Callable() {

                    public String call() {
                        return CrashReportCategory.a(i0, 0, i1);
                    }
                });
                throw new ReportedException(crashreport);
            }

        }
        else {
            return this.t.e.a(i0, i1);
        }
    }

    public WorldChunkManager u() {
        return this.t.e;
    }

    public World(ISaveHandler isavehandler, String s0, WorldSettings worldsettings, WorldProvider worldprovider, Profiler profiler, net.canarymod.api.world.DimensionType type) {
        this.L = this.s.nextInt(12000);
        this.G = true;
        this.H = true;
        this.M = new ArrayList();
        this.I = new int['\u8000'];
        this.w = isavehandler;
        this.C = profiler;
        this.z = new MapStorage(isavehandler);
        this.x = isavehandler.d();
        if (worldprovider != null) {
            this.t = worldprovider;
        }
        else if (this.x != null && this.x.j() != 0) {
            this.t = WorldProvider.a(this.x.j());
        }
        else {
            this.t = WorldProvider.a(0);
        }
        // CanaryMod set dimension type in world provider
        canaryDimension = new CanaryWorld(s0, (WorldServer) this, type);
        this.t.setCanaryDimensionType(type);
        if (this.x == null) {
            this.x = new WorldInfo(worldsettings, s0);
        }
        else {
            this.x.a(s0);
        }

        this.t.a(this);
        this.v = this.j();
        if (!this.x.w()) {
            try {
                this.a(worldsettings);
            }
            catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Exception initializing level");

                try {
                    this.a(crashreport);
                }
                catch (Throwable throwable1) {
                    ;
                }

                throw new ReportedException(crashreport);
            }

            this.x.d(true);
        }

        VillageCollection villagecollection = (VillageCollection) this.z.a(VillageCollection.class, "villages");

        if (villagecollection == null) {
            this.A = new VillageCollection(this);
            this.z.a("villages", (WorldSavedData) this.A);
        }
        else {
            this.A = villagecollection;
            this.A.a(this);
        }

        this.A();
        this.a();
    }

    protected abstract IChunkProvider j();

    protected void a(WorldSettings worldsettings) {
        this.x.d(true);
    }

    public Block b(int i0, int i1) {
        int i2;

        for (i2 = 63; !this.c(i0, i2 + 1, i1); ++i2) {
            ;
        }

        return this.a(i0, i2, i1);
    }

    public Block a(int i0, int i1, int i2) {
        if (i0 >= -30000000 && i2 >= -30000000 && i0 < 30000000 && i2 < 30000000 && i1 >= 0 && i1 < 256) {
            Chunk chunk = null;

            try {
                chunk = this.e(i0 >> 4, i2 >> 4);
                return chunk.a(i0 & 15, i1, i2 & 15);
            }
            catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Exception getting block type in world");
                CrashReportCategory crashreportcategory = crashreport.a("Requested block coordinates");

                crashreportcategory.a("Found chunk", (Object) Boolean.valueOf(chunk == null));
                crashreportcategory.a("Location", (Object) CrashReportCategory.a(i0, i1, i2));
                throw new ReportedException(crashreport);
            }
        }
        else {
            return Blocks.a;
        }
    }

    public boolean c(int i0, int i1, int i2) {
        return this.a(i0, i1, i2).o() == Material.a;
    }

    public boolean d(int i0, int i1, int i2) {
        return i1 >= 0 && i1 < 256 ? this.c(i0 >> 4, i2 >> 4) : false;
    }

    public boolean a(int i0, int i1, int i2, int i3) {
        return this.b(i0 - i3, i1 - i3, i2 - i3, i0 + i3, i1 + i3, i2 + i3);
    }

    public boolean b(int i0, int i1, int i2, int i3, int i4, int i5) {
        if (i4 >= 0 && i1 < 256) {
            i0 >>= 4;
            i2 >>= 4;
            i3 >>= 4;
            i5 >>= 4;

            for (int i6 = i0; i6 <= i3; ++i6) {
                for (int i7 = i2; i7 <= i5; ++i7) {
                    if (!this.c(i6, i7)) {
                        return false;
                    }
                }
            }

            return true;
        }
        else {
            return false;
        }
    }

    protected boolean c(int i0, int i1) {
        return this.v.a(i0, i1);
    }

    public Chunk d(int i0, int i1) {
        return this.e(i0 >> 4, i1 >> 4);
    }

    public Chunk e(int i0, int i1) {
        return this.v.d(i0, i1);
    }

    public boolean d(int i0, int i1, int i2, Block block, int i3, int i4) {
        if (i0 >= -30000000 && i2 >= -30000000 && i0 < 30000000 && i2 < 30000000) {
            if (i1 < 0) {
                return false;
            }
            else if (i1 >= 256) {
                return false;
            }
            else {
                Chunk chunk = this.e(i0 >> 4, i2 >> 4);
                Block block1 = null;

                if ((i4 & 1) != 0) {
                    block1 = chunk.a(i0 & 15, i1, i2 & 15);
                }

                // CanaryMod: BlockUpdate
                boolean flag0 = false;
                CanaryBlock cblock;
                if (canaryDimension != null) {
                    cblock = (CanaryBlock) this.canaryDimension.getBlockAt(i0, i1, i2);
                    BlockUpdateHook hook = (BlockUpdateHook) new BlockUpdateHook(cblock, i3).call();
                    if (!hook.isCanceled()) {
                        flag0 = chunk.a(i0 & 15, i1, i2 & 15, block, i3);
                    }
                    //
                }
                else {
                    // Do not forward things when the world wrapper isn't init yet
                    flag0 = chunk.a(i0 & 15, i1, i2 & 15, block, i3);
                }

                this.C.a("checkLight");
                this.t(i0, i1, i2);
                this.C.b();
                if (flag0) {
                    if ((i4 & 2) != 0 && (!this.E || (i4 & 4) == 0) && chunk.k()) {
                        this.g(i0, i1, i2);
                    }

                    if (!this.E && (i4 & 1) != 0) {
                        this.c(i0, i1, i2, block1);
                        if (block.M()) {
                            this.f(i0, i1, i2, block);
                        }
                    }
                }

                return flag0;
            }
        }
        else {
            return false;
        }
    }

    public int e(int i0, int i1, int i2) {
        if (i0 >= -30000000 && i2 >= -30000000 && i0 < 30000000 && i2 < 30000000) {
            if (i1 < 0) {
                return 0;
            }
            else if (i1 >= 256) {
                return 0;
            }
            else {
                Chunk chunk = this.e(i0 >> 4, i2 >> 4);

                i0 &= 15;
                i2 &= 15;
                return chunk.c(i0, i1, i2);
            }
        }
        else {
            return 0;
        }
    }

    public boolean a(int i0, int i1, int i2, int i3, int i4) {
        if (i0 >= -30000000 && i2 >= -30000000 && i0 < 30000000 && i2 < 30000000) {
            if (i1 < 0) {
                return false;
            }
            else if (i1 >= 256) {
                return false;
            }
            else {
                Chunk chunk = this.e(i0 >> 4, i2 >> 4);
                int i5 = i0 & 15;
                int i6 = i2 & 15;
                boolean flag0 = chunk.a(i5, i1, i6, i3);

                if (flag0) {
                    Block block = chunk.a(i5, i1, i6);

                    if ((i4 & 2) != 0 && (!this.E || (i4 & 4) == 0) && chunk.k()) {
                        this.g(i0, i1, i2);
                    }

                    if (!this.E && (i4 & 1) != 0) {
                        this.c(i0, i1, i2, block);
                        if (block.M()) {
                            this.f(i0, i1, i2, block);
                        }
                    }
                }

                return flag0;
            }
        }
        else {
            return false;
        }
    }

    public boolean f(int i0, int i1, int i2) {
        return this.d(i0, i1, i2, Blocks.a, 0, 3);
    }

    public boolean a(int i0, int i1, int i2, boolean flag0) {
        Block block = this.a(i0, i1, i2);

        if (block.o() == Material.a) {
            return false;
        }
        else {
            int i3 = this.e(i0, i1, i2);

            this.c(2001, i0, i1, i2, Block.b(block) + (i3 << 12));
            if (flag0) {
                block.b(this, i0, i1, i2, i3, 0);
            }

            return this.d(i0, i1, i2, Blocks.a, 0, 3);
        }
    }

    public boolean b(int i0, int i1, int i2, Block block) {
        return this.d(i0, i1, i2, block, 0, 3);
    }

    public void g(int i0, int i1, int i2) {
        for (int i3 = 0; i3 < this.u.size(); ++i3) {
            ((IWorldAccess) this.u.get(i3)).a(i0, i1, i2);
        }
    }

    public void c(int i0, int i1, int i2, Block block) {
        this.d(i0, i1, i2, block);
    }

    public void b(int i0, int i1, int i2, int i3) {
        int i4;

        if (i2 > i3) {
            i4 = i3;
            i3 = i2;
            i2 = i4;
        }

        if (!this.t.g) {
            for (i4 = i2; i4 <= i3; ++i4) {
                this.c(EnumSkyBlock.Sky, i0, i4, i1);
            }
        }

        this.c(i0, i2, i1, i0, i3, i1);
    }

    public void c(int i0, int i1, int i2, int i3, int i4, int i5) {
        for (int i6 = 0; i6 < this.u.size(); ++i6) {
            ((IWorldAccess) this.u.get(i6)).a(i0, i1, i2, i3, i4, i5);
        }
    }

    public void d(int i0, int i1, int i2, Block block) {
        this.e(i0 - 1, i1, i2, block);
        this.e(i0 + 1, i1, i2, block);
        this.e(i0, i1 - 1, i2, block);
        this.e(i0, i1 + 1, i2, block);
        this.e(i0, i1, i2 - 1, block);
        this.e(i0, i1, i2 + 1, block);
    }

    public void b(int i0, int i1, int i2, Block block, int i3) {
        if (i3 != 4) {
            this.e(i0 - 1, i1, i2, block);
        }

        if (i3 != 5) {
            this.e(i0 + 1, i1, i2, block);
        }

        if (i3 != 0) {
            this.e(i0, i1 - 1, i2, block);
        }

        if (i3 != 1) {
            this.e(i0, i1 + 1, i2, block);
        }

        if (i3 != 2) {
            this.e(i0, i1, i2 - 1, block);
        }

        if (i3 != 3) {
            this.e(i0, i1, i2 + 1, block);
        }
    }

    public void e(int i0, int throwable2, int i2, final Block block) {
        if (!this.E) {
            Block block1 = this.a(i0, throwable2, i2);

            try {
                block1.a(this, i0, throwable2, i2, block);
            }
            catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Exception while updating neighbours");
                CrashReportCategory crashreportcategory = crashreport.a("Block being updated");

                int i3;

                try {
                    i3 = this.e(i0, throwable2, i2);
                }
                catch (Throwable throwable1) {
                    i3 = -1;
                }

                crashreportcategory.a("Source block type", new Callable() {

                    public String call() {
                        try {
                            return String.format("ID #%d (%s // %s)", new Object[]{ Integer.valueOf(Block.b(block)), block.a(), block.getClass().getCanonicalName() });
                        }
                        catch (Throwable throwable2) {
                            return "ID #" + Block.b(block);
                        }
                    }
                });
                CrashReportCategory.a(crashreportcategory, i0, throwable2, i2, block1, i3);
                throw new ReportedException(crashreport);
            }
        }
    }

    public boolean a(int i0, int i1, int i2, Block block) {
        return false;
    }

    public boolean i(int i0, int i1, int i2) {
        return this.e(i0 >> 4, i2 >> 4).d(i0 & 15, i1, i2 & 15);
    }

    public int j(int i0, int i1, int i2) {
        if (i1 < 0) {
            return 0;
        }
        else {
            if (i1 >= 256) {
                i1 = 255;
            }

            return this.e(i0 >> 4, i2 >> 4).b(i0 & 15, i1, i2 & 15, 0);
        }
    }

    public int k(int i0, int i1, int i2) {
        return this.b(i0, i1, i2, true);
    }

    public int b(int i0, int i1, int i2, boolean flag0) {
        if (i0 >= -30000000 && i2 >= -30000000 && i0 < 30000000 && i2 < 30000000) {
            if (flag0 && this.a(i0, i1, i2).n()) {
                int i3 = this.b(i0, i1 + 1, i2, false);
                int i4 = this.b(i0 + 1, i1, i2, false);
                int i5 = this.b(i0 - 1, i1, i2, false);
                int i6 = this.b(i0, i1, i2 + 1, false);
                int i7 = this.b(i0, i1, i2 - 1, false);

                if (i4 > i3) {
                    i3 = i4;
                }

                if (i5 > i3) {
                    i3 = i5;
                }

                if (i6 > i3) {
                    i3 = i6;
                }

                if (i7 > i3) {
                    i3 = i7;
                }

                return i3;
            }
            else if (i1 < 0) {
                return 0;
            }
            else {
                if (i1 >= 256) {
                    i1 = 255;
                }

                Chunk chunk = this.e(i0 >> 4, i2 >> 4);

                i0 &= 15;
                i2 &= 15;
                return chunk.b(i0, i1, i2, this.j);
            }
        }
        else {
            return 15;
        }
    }

    public int f(int i0, int i1) {
        if (i0 >= -30000000 && i1 >= -30000000 && i0 < 30000000 && i1 < 30000000) {
            if (!this.c(i0 >> 4, i1 >> 4)) {
                return 0;
            }
            else {
                Chunk chunk = this.e(i0 >> 4, i1 >> 4);

                return chunk.b(i0 & 15, i1 & 15);
            }
        }
        else {
            return 64;
        }
    }

    public int g(int i0, int i1) {
        if (i0 >= -30000000 && i1 >= -30000000 && i0 < 30000000 && i1 < 30000000) {
            if (!this.c(i0 >> 4, i1 >> 4)) {
                return 0;
            }
            else {
                Chunk chunk = this.e(i0 >> 4, i1 >> 4);

                return chunk.r;
            }
        }
        else {
            return 64;
        }
    }

    public int b(EnumSkyBlock enumskyblock, int i0, int i1, int i2) {
        if (i1 < 0) {
            i1 = 0;
        }

        if (i1 >= 256) {
            i1 = 255;
        }

        if (i0 >= -30000000 && i2 >= -30000000 && i0 < 30000000 && i2 < 30000000) {
            int i3 = i0 >> 4;
            int i4 = i2 >> 4;

            if (!this.c(i3, i4)) {
                return enumskyblock.c;
            }
            else {
                Chunk chunk = this.e(i3, i4);

                return chunk.a(enumskyblock, i0 & 15, i1, i2 & 15);
            }
        }
        else {
            return enumskyblock.c;
        }
    }

    public void b(EnumSkyBlock enumskyblock, int i0, int i1, int i2, int i3) {
        if (i0 >= -30000000 && i2 >= -30000000 && i0 < 30000000 && i2 < 30000000) {
            if (i1 >= 0) {
                if (i1 < 256) {
                    if (this.c(i0 >> 4, i2 >> 4)) {
                        Chunk chunk = this.e(i0 >> 4, i2 >> 4);

                        chunk.a(enumskyblock, i0 & 15, i1, i2 & 15, i3);

                        for (int i4 = 0; i4 < this.u.size(); ++i4) {
                            ((IWorldAccess) this.u.get(i4)).b(i0, i1, i2);
                        }
                    }
                }
            }
        }
    }

    public void m(int i0, int i1, int i2) {
        for (int i3 = 0; i3 < this.u.size(); ++i3) {
            ((IWorldAccess) this.u.get(i3)).b(i0, i1, i2);
        }
    }

    public float n(int i0, int i1, int i2) {
        return this.t.h[this.k(i0, i1, i2)];
    }

    public boolean v() {
        return this.j < 4;
    }

    public MovingObjectPosition a(Vec3 vec3, Vec3 vec31) {
        return this.a(vec3, vec31, false, false, false);
    }

    public MovingObjectPosition a(Vec3 vec3, Vec3 vec31, boolean flag0) {
        return this.a(vec3, vec31, flag0, false, false);
    }

    public MovingObjectPosition a(Vec3 vec3, Vec3 vec31, boolean flag0, boolean flag1, boolean flag2) {
        if (!Double.isNaN(vec3.c) && !Double.isNaN(vec3.d) && !Double.isNaN(vec3.e)) {
            if (!Double.isNaN(vec31.c) && !Double.isNaN(vec31.d) && !Double.isNaN(vec31.e)) {
                int i0 = MathHelper.c(vec31.c);
                int i1 = MathHelper.c(vec31.d);
                int i2 = MathHelper.c(vec31.e);
                int i3 = MathHelper.c(vec3.c);
                int i4 = MathHelper.c(vec3.d);
                int i5 = MathHelper.c(vec3.e);
                Block block = this.a(i3, i4, i5);
                int i6 = this.e(i3, i4, i5);

                if ((!flag1 || block.a(this, i3, i4, i5) != null) && block.a(i6, flag0)) {
                    MovingObjectPosition movingobjectposition = block.a(this, i3, i4, i5, vec3, vec31);

                    if (movingobjectposition != null) {
                        return movingobjectposition;
                    }
                }
                MovingObjectPosition movingobjectposition1 = null;

                i6 = 200;

                while (i6-- >= 0) {
                    if (Double.isNaN(vec3.c) || Double.isNaN(vec3.d) || Double.isNaN(vec3.e)) {
                        return null;
                    }

                    if (i3 == i0 && i4 == i1 && i5 == i2) {
                        return flag2 ? movingobjectposition1 : null;
                    }

                    boolean flag3 = true;
                    boolean flag4 = true;
                    boolean flag5 = true;
                    double d0 = 999.0D;
                    double d1 = 999.0D;
                    double d2 = 999.0D;

                    if (i0 > i3) {
                        d0 = (double) i3 + 1.0D;
                    }
                    else if (i0 < i3) {
                        d0 = (double) i3 + 0.0D;
                    }
                    else {
                        flag3 = false;
                    }

                    if (i1 > i4) {
                        d1 = (double) i4 + 1.0D;
                    }
                    else if (i1 < i4) {
                        d1 = (double) i4 + 0.0D;
                    }
                    else {
                        flag4 = false;
                    }

                    if (i2 > i5) {
                        d2 = (double) i5 + 1.0D;
                    }
                    else if (i2 < i5) {
                        d2 = (double) i5 + 0.0D;
                    }
                    else {
                        flag5 = false;
                    }

                    double d3 = 999.0D;
                    double d4 = 999.0D;
                    double d5 = 999.0D;
                    double d6 = vec31.c - vec3.c;
                    double d7 = vec31.d - vec3.d;
                    double d8 = vec31.e - vec3.e;

                    if (flag3) {
                        d3 = (d0 - vec3.c) / d6;
                    }

                    if (flag4) {
                        d4 = (d1 - vec3.d) / d7;
                    }

                    if (flag5) {
                        d5 = (d2 - vec3.e) / d8;
                    }

                    boolean flag6 = false;
                    byte b0;

                    if (d3 < d4 && d3 < d5) {
                        if (i0 > i3) {
                            b0 = 4;
                        }
                        else {
                            b0 = 5;
                        }

                        vec3.c = d0;
                        vec3.d += d7 * d3;
                        vec3.e += d8 * d3;
                    }
                    else if (d4 < d5) {
                        if (i1 > i4) {
                            b0 = 0;
                        }
                        else {
                            b0 = 1;
                        }

                        vec3.c += d6 * d4;
                        vec3.d = d1;
                        vec3.e += d8 * d4;
                    }
                    else {
                        if (i2 > i5) {
                            b0 = 2;
                        }
                        else {
                            b0 = 3;
                        }

                        vec3.c += d6 * d5;
                        vec3.d += d7 * d5;
                        vec3.e = d2;
                    }

                    Vec3 vec32 = this.U().a(vec3.c, vec3.d, vec3.e);

                    i3 = (int) (vec32.c = (double) MathHelper.c(vec3.c));
                    if (b0 == 5) {
                        --i3;
                        ++vec32.c;
                    }

                    i4 = (int) (vec32.d = (double) MathHelper.c(vec3.d));
                    if (b0 == 1) {
                        --i4;
                        ++vec32.d;
                    }

                    i5 = (int) (vec32.e = (double) MathHelper.c(vec3.e));
                    if (b0 == 3) {
                        --i5;
                        ++vec32.e;
                    }

                    Block block1 = this.a(i3, i4, i5);
                    int i7 = this.e(i3, i4, i5);

                    if (!flag1 || block1.a(this, i3, i4, i5) != null) {
                        if (block1.a(i7, flag0)) {
                            MovingObjectPosition movingobjectposition2 = block1.a(this, i3, i4, i5, vec3, vec31);

                            if (movingobjectposition2 != null) {
                                return movingobjectposition2;
                            }
                        }
                        else {
                            movingobjectposition1 = new MovingObjectPosition(i3, i4, i5, b0, vec3, false);
                        }
                    }
                }

                return flag2 ? movingobjectposition1 : null;
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    public void a(Entity entity, String s0, float f0, float f1) {
        for (int i0 = 0; i0 < this.u.size(); ++i0) {
            ((IWorldAccess) this.u.get(i0)).a(s0, entity.t, entity.u - (double) entity.M, entity.v, f0, f1);
        }
    }

    public void a(EntityPlayer entityplayer, String s0, float f0, float f1) {
        for (int i0 = 0; i0 < this.u.size(); ++i0) {
            ((IWorldAccess) this.u.get(i0)).a(entityplayer, s0, entityplayer.t, entityplayer.u - (double) entityplayer.M, entityplayer.v, f0, f1);
        }
    }

    public void a(double d0, double d1, double d2, String s0, float f0, float f1) {
        for (int i0 = 0; i0 < this.u.size(); ++i0) {
            ((IWorldAccess) this.u.get(i0)).a(s0, d0, d1, d2, f0, f1);
        }
    }

    public void a(double d0, double d1, double d2, String s0, float f0, float f1, boolean flag0) {
    }

    public void a(String s0, int i0, int i1, int i2) {
        for (int i3 = 0; i3 < this.u.size(); ++i3) {
            ((IWorldAccess) this.u.get(i3)).a(s0, i0, i1, i2);
        }
    }

    public void a(String s0, double d0, double d1, double d2, double d3, double d4, double d5) {
        for (int i0 = 0; i0 < this.u.size(); ++i0) {
            ((IWorldAccess) this.u.get(i0)).a(s0, d0, d1, d2, d3, d4, d5);
        }
    }

    public boolean c(Entity entity) {
        this.i.add(entity);
        return true;
    }

    public boolean d(Entity entity) {

        // Check Entity for spawning
        if (!canSpawn(entity)) {
            return false;
        }
        // CanaryMod: EntitySpawn
        if (!(entity.getCanaryEntity() instanceof CanaryHuman)) {
            EntitySpawnHook hook = (EntitySpawnHook) new EntitySpawnHook(entity.getCanaryEntity()).call();
            if (hook.isCanceled()) {
                return false;
            }
        }
        //
        int i0 = MathHelper.c(entity.t / 16.0D);
        int i1 = MathHelper.c(entity.v / 16.0D);
        boolean flag0 = entity.o;

        if (entity instanceof EntityPlayer) {
            flag0 = true;
        }

        if (!flag0 && !this.c(i0, i1)) {
            return false;
        }
        else {
            if (entity instanceof EntityPlayerMP) { // CanaryMod: dont handle NPC's this way
                EntityPlayer entityplayer = (EntityPlayer) entity;

                this.h.add(entityplayer);
                this.c();
            }

            this.e(i0, i1).a(entity);
            this.e.add(entity);
            this.a(entity);
            return true;
        }
    }

    protected void a(Entity entity) {
        for (int i0 = 0; i0 < this.u.size(); ++i0) {
            ((IWorldAccess) this.u.get(i0)).a(entity);
        }
    }

    protected void b(Entity entity) {
        for (int i0 = 0; i0 < this.u.size(); ++i0) {
            ((IWorldAccess) this.u.get(i0)).b(entity);
        }
    }

    public void e(Entity entity) {
        if (entity.m != null) {
            entity.m.a((Entity) null);
        }

        if (entity.n != null) {
            entity.a((Entity) null);
        }

        entity.B();
        if (entity instanceof EntityPlayer) {
            this.h.remove(entity);
            this.c();
        }
    }

    public void f(Entity entity) {
        entity.B();
        if (entity instanceof EntityPlayer) {
            this.h.remove(entity);
            this.c();
        }

        int i0 = entity.ai;
        int i1 = entity.ak;

        if (entity.ah && this.c(i0, i1)) {
            this.e(i0, i1).b(entity);
        }

        this.e.remove(entity);
        this.b(entity);
    }

    public void a(IWorldAccess iworldaccess) {
        this.u.add(iworldaccess);
    }

    public List a(Entity entity, AxisAlignedBB axisalignedbb) {
        this.M.clear();
        int i0 = MathHelper.c(axisalignedbb.a);
        int i1 = MathHelper.c(axisalignedbb.d + 1.0D);
        int i2 = MathHelper.c(axisalignedbb.b);
        int i3 = MathHelper.c(axisalignedbb.e + 1.0D);
        int i4 = MathHelper.c(axisalignedbb.c);
        int i5 = MathHelper.c(axisalignedbb.f + 1.0D);

        for (int i6 = i0; i6 < i1; ++i6) {
            for (int i7 = i4; i7 < i5; ++i7) {
                if (this.d(i6, 64, i7)) {
                    for (int i8 = i2 - 1; i8 < i3; ++i8) {
                        Block block;

                        if (i6 >= -30000000 && i6 < 30000000 && i7 >= -30000000 && i7 < 30000000) {
                            block = this.a(i6, i8, i7);
                        }
                        else {
                            block = Blocks.b;
                        }
                        block.a(this, i6, i8, i7, axisalignedbb, this.M, entity);
                    }
                }
            }
        }

        // CanaryMod: Implemented fix via M4411K4 VEHICLE_COLLISION hook
        CanaryVehicle vehicle = null;

        if (entity instanceof EntityMinecart || entity instanceof EntityBoat) {
            vehicle = (CanaryVehicle) entity.getCanaryEntity();
        }

        double d0 = 0.25D;
        List list = this.b(entity, axisalignedbb.b(d0, d0, d0));

        for (int i9 = 0; i9 < list.size(); ++i9) {
            Entity entity1 = (Entity) list.get(i9); // CanaryMod: split these two lines
            AxisAlignedBB axisalignedbb1 = entity1.J();

            if (axisalignedbb1 != null && axisalignedbb1.b(axisalignedbb)) {
                // CanaryMod: this collided with a boat
                if (vehicle != null) {
                    VehicleCollisionHook vch = (VehicleCollisionHook) new VehicleCollisionHook(vehicle, entity.getCanaryEntity()).call();
                    if (vch.isCanceled()) {
                        continue;
                    }
                }
                //
                this.M.add(axisalignedbb1);
            }

            axisalignedbb1 = entity.g((Entity) list.get(i9));
            if (axisalignedbb1 != null && axisalignedbb1.b(axisalignedbb)) {
                // CanaryMod: this collided with entity
                if (vehicle != null) {
                    VehicleCollisionHook vch = (VehicleCollisionHook) new VehicleCollisionHook(vehicle, entity.getCanaryEntity()).call();
                    if (vch.isCanceled()) {
                        continue;
                    }
                }
                //
                this.M.add(axisalignedbb1);
            }
        }

        return this.M;
    }

    public List a(AxisAlignedBB axisalignedbb) {
        this.M.clear();
        int i0 = MathHelper.c(axisalignedbb.a);
        int i1 = MathHelper.c(axisalignedbb.d + 1.0D);
        int i2 = MathHelper.c(axisalignedbb.b);
        int i3 = MathHelper.c(axisalignedbb.e + 1.0D);
        int i4 = MathHelper.c(axisalignedbb.c);
        int i5 = MathHelper.c(axisalignedbb.f + 1.0D);

        for (int i6 = i0; i6 < i1; ++i6) {
            for (int i7 = i4; i7 < i5; ++i7) {
                if (this.d(i6, 64, i7)) {
                    for (int i8 = i2 - 1; i8 < i3; ++i8) {
                        Block block;

                        if (i6 >= -30000000 && i6 < 30000000 && i7 >= -30000000 && i7 < 30000000) {
                            block = this.a(i6, i8, i7);
                        }
                        else {
                            block = Blocks.h;
                        }
                        block.a(this, i6, i8, i7, axisalignedbb, this.M, (Entity) null);
                    }
                }
            }
        }

        return this.M;
    }

    public int a(float f0) {
        float f1 = this.c(f0);
        float f2 = 1.0F - (MathHelper.b(f1 * 3.1415927F * 2.0F) * 2.0F + 0.5F);

        if (f2 < 0.0F) {
            f2 = 0.0F;
        }

        if (f2 > 1.0F) {
            f2 = 1.0F;
        }

        f2 = 1.0F - f2;
        f2 = (float) ((double) f2 * (1.0D - (double) (this.j(f0) * 5.0F) / 16.0D));
        f2 = (float) ((double) f2 * (1.0D - (double) (this.h(f0) * 5.0F) / 16.0D));
        f2 = 1.0F - f2;
        return (int) (f2 * 11.0F);
    }

    public float c(float f0) {
        return this.t.a(this.x.g(), f0);
    }

    public float x() {
        return WorldProvider.a[this.t.a(this.x.g())];
    }

    public float d(float f0) {
        float f1 = this.c(f0);

        return f1 * 3.1415927F * 2.0F;
    }

    public int h(int i0, int i1) {
        return this.d(i0, i1).d(i0 & 15, i1 & 15);
    }

    public int i(int i0, int i1) {
        Chunk chunk = this.d(i0, i1);
        int i2 = chunk.h() + 15;

        i0 &= 15;

        for (i1 &= 15; i2 > 0; --i2) {
            Block block = chunk.a(i0, i2, i1);

            if (block.o().c() && block.o() != Material.j) {
                return i2 + 1;
            }
        }

        return -1;
    }

    public void a(int i0, int i1, int i2, Block block, int i3) {
    }

    public void a(int i0, int i1, int i2, Block block, int i3, int i4) {
    }

    public void b(int i0, int i1, int i2, Block block, int i3, int i4) {
    }

    public void h() {
        this.C.a("entities");
        this.C.a("global");

        int i0;
        Entity entity;
        CrashReport crashreport;
        CrashReportCategory crashreportcategory;

        for (i0 = 0; i0 < this.i.size(); ++i0) {
            entity = (Entity) this.i.get(i0);

            try {
                ++entity.ab;
                entity.h();
            }
            catch (Throwable throwable) {
                crashreport = CrashReport.a(throwable, "Ticking entity");
                crashreportcategory = crashreport.a("Entity being ticked");
                if (entity == null) {
                    crashreportcategory.a("Entity", (Object) "~~NULL~~");
                }
                else {
                    entity.a(crashreportcategory);
                }

                throw new ReportedException(crashreport);
            }

            if (entity.L) {
                this.i.remove(i0--);
            }
        }

        this.C.c("remove");
        this.e.removeAll(this.f);

        int i1;
        int i2;

        for (i0 = 0; i0 < this.f.size(); ++i0) {
            entity = (Entity) this.f.get(i0);
            i1 = entity.ai;
            i2 = entity.ak;
            if (entity.ah && this.c(i1, i2)) {
                this.e(i1, i2).b(entity);
            }
        }

        for (i0 = 0; i0 < this.f.size(); ++i0) {
            this.b((Entity) this.f.get(i0));
        }

        this.f.clear();
        this.C.c("regular");

        for (i0 = 0; i0 < this.e.size(); ++i0) {
            entity = (Entity) this.e.get(i0);
            if (entity.n != null) {
                if (!entity.n.L && entity.n.m == entity) {
                    continue;
                }

                entity.n.m = null;
                entity.n = null;
            }

            this.C.a("tick");
            if (!entity.L) {
                try {
                    this.g(entity);
                }
                catch (Throwable throwable1) {
                    crashreport = CrashReport.a(throwable1, "Ticking entity");
                    crashreportcategory = crashreport.a("Entity being ticked");
                    entity.a(crashreportcategory);
                    throw new ReportedException(crashreport);
                }
            }

            this.C.b();
            this.C.a("remove");
            if (entity.L) {
                i1 = entity.ai;
                i2 = entity.ak;
                if (entity.ah && this.c(i1, i2)) {
                    this.e(i1, i2).b(entity);
                }

                this.e.remove(i0--);
                this.b(entity);
            }

            this.C.b();
        }

        this.C.c("blockEntities");
        this.N = true;
        Iterator iterator = this.g.iterator();

        while (iterator.hasNext()) {
            TileEntity tileentity = (TileEntity) iterator.next();

            if (!tileentity.r() && tileentity.o() && this.d(tileentity.c, tileentity.d, tileentity.e)) {
                try {
                    tileentity.h();
                }
                catch (Throwable throwable2) {
                    crashreport = CrashReport.a(throwable2, "Ticking block entity");
                    crashreportcategory = crashreport.a("Block entity being ticked");
                    tileentity.a(crashreportcategory);
                    throw new ReportedException(crashreport);
                }
            }

            if (tileentity.r()) {
                iterator.remove();
                if (this.c(tileentity.c >> 4, tileentity.e >> 4)) {
                    Chunk chunk = this.e(tileentity.c >> 4, tileentity.e >> 4);

                    if (chunk != null) {
                        chunk.f(tileentity.c & 15, tileentity.d, tileentity.e & 15);
                    }
                }
            }
        }

        this.N = false;
        if (!this.b.isEmpty()) {
            this.g.removeAll(this.b);
            this.b.clear();
        }

        this.C.c("pendingBlockEntities");
        if (!this.a.isEmpty()) {
            for (int i3 = 0; i3 < this.a.size(); ++i3) {
                TileEntity tileentity1 = (TileEntity) this.a.get(i3);

                if (!tileentity1.r()) {
                    if (!this.g.contains(tileentity1)) {
                        this.g.add(tileentity1);
                    }

                    if (this.c(tileentity1.c >> 4, tileentity1.e >> 4)) {
                        Chunk chunk1 = this.e(tileentity1.c >> 4, tileentity1.e >> 4);

                        if (chunk1 != null) {
                            chunk1.a(tileentity1.c & 15, tileentity1.d, tileentity1.e & 15, tileentity1);
                        }
                    }

                    this.g(tileentity1.c, tileentity1.d, tileentity1.e);
                }
            }

            this.a.clear();
        }

        this.C.b();
        this.C.b();
    }

    public void a(Collection collection) {
        if (this.N) {
            this.a.addAll(collection);
        }
        else {
            this.g.addAll(collection);
        }
    }

    public void g(Entity entity) {
        this.a(entity, true);
    }

    public void a(Entity entity, boolean flag0) {
        int i0 = MathHelper.c(entity.t);
        int i1 = MathHelper.c(entity.v);
        byte b0 = 32;

        if (!flag0 || this.b(i0 - b0, 0, i1 - b0, i0 + b0, 0, i1 + b0)) {
            entity.T = entity.t;
            entity.U = entity.u;
            entity.V = entity.v;
            entity.B = entity.z;
            entity.C = entity.A;
            if (flag0 && entity.ah) {
                ++entity.ab;
                if (entity.n != null) {
                    entity.ab();
                }
                else {
                    entity.h();
                }
            }

            this.C.a("chunkCheck");
            if (Double.isNaN(entity.t) || Double.isInfinite(entity.t)) {
                entity.t = entity.T;
            }
            if (Double.isNaN(entity.u) || Double.isInfinite(entity.u)) {
                entity.u = entity.U;
            }

            if (Double.isNaN(entity.v) || Double.isInfinite(entity.v)) {
                entity.v = entity.V;
            }

            if (Double.isNaN((double) entity.A) || Double.isInfinite((double) entity.A)) {
                entity.A = entity.C;
            }

            if (Double.isNaN((double) entity.z) || Double.isInfinite((double) entity.z)) {
                entity.z = entity.B;
            }

            int i2 = MathHelper.c(entity.t / 16.0D);
            int i3 = MathHelper.c(entity.u / 16.0D);
            int i4 = MathHelper.c(entity.v / 16.0D);

            if (!entity.ah || entity.ai != i2 || entity.aj != i3 || entity.ak != i4) {
                if (entity.ah && this.c(entity.ai, entity.ak)) {
                    this.e(entity.ai, entity.ak).a(entity, entity.aj);
                }

                if (this.c(i2, i4)) {
                    entity.ah = true;
                    this.e(i2, i4).a(entity);
                }
                else {
                    entity.ah = false;
                }
            }

            this.C.b();
            if (flag0 && entity.ah && entity.m != null) {
                if (!entity.m.L && entity.m.n == entity) {
                    this.g(entity.m);
                }
                else {
                    entity.m.n = null;
                    entity.m = null;
                }
            }
        }
    }

    public boolean b(AxisAlignedBB axisalignedbb) {
        return this.a(axisalignedbb, (Entity) null);
    }

    public boolean a(AxisAlignedBB axisalignedbb, Entity entity) {
        List list = this.b((Entity) null, axisalignedbb);

        for (int i0 = 0; i0 < list.size(); ++i0) {
            Entity entity1 = (Entity) list.get(i0);

            if (!entity1.L && entity1.l && entity1 != entity) {
                return false;
            }
        }

        return true;
    }

    public boolean c(AxisAlignedBB axisalignedbb) {
        int i0 = MathHelper.c(axisalignedbb.a);
        int i1 = MathHelper.c(axisalignedbb.d + 1.0D);
        int i2 = MathHelper.c(axisalignedbb.b);
        int i3 = MathHelper.c(axisalignedbb.e + 1.0D);
        int i4 = MathHelper.c(axisalignedbb.c);
        int i5 = MathHelper.c(axisalignedbb.f + 1.0D);

        if (axisalignedbb.a < 0.0D) {
            --i0;
        }

        if (axisalignedbb.b < 0.0D) {
            --i2;
        }

        if (axisalignedbb.c < 0.0D) {
            --i4;
        }

        for (int i6 = i0; i6 < i1; ++i6) {
            for (int i7 = i2; i7 < i3; ++i7) {
                for (int i8 = i4; i8 < i5; ++i8) {
                    Block block = this.a(i6, i7, i8);

                    if (block.o() != Material.a) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean d(AxisAlignedBB axisalignedbb) {
        int i0 = MathHelper.c(axisalignedbb.a);
        int i1 = MathHelper.c(axisalignedbb.d + 1.0D);
        int i2 = MathHelper.c(axisalignedbb.b);
        int i3 = MathHelper.c(axisalignedbb.e + 1.0D);
        int i4 = MathHelper.c(axisalignedbb.c);
        int i5 = MathHelper.c(axisalignedbb.f + 1.0D);

        if (axisalignedbb.a < 0.0D) {
            --i0;
        }

        if (axisalignedbb.b < 0.0D) {
            --i2;
        }

        if (axisalignedbb.c < 0.0D) {
            --i4;
        }

        for (int i6 = i0; i6 < i1; ++i6) {
            for (int i7 = i2; i7 < i3; ++i7) {
                for (int i8 = i4; i8 < i5; ++i8) {
                    Block block = this.a(i6, i7, i8);

                    if (block.o().d()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean e(AxisAlignedBB axisalignedbb) {
        int i0 = MathHelper.c(axisalignedbb.a);
        int i1 = MathHelper.c(axisalignedbb.d + 1.0D);
        int i2 = MathHelper.c(axisalignedbb.b);
        int i3 = MathHelper.c(axisalignedbb.e + 1.0D);
        int i4 = MathHelper.c(axisalignedbb.c);
        int i5 = MathHelper.c(axisalignedbb.f + 1.0D);

        if (this.b(i0, i2, i4, i1, i3, i5)) {
            for (int i6 = i0; i6 < i1; ++i6) {
                for (int i7 = i2; i7 < i3; ++i7) {
                    for (int i8 = i4; i8 < i5; ++i8) {
                        Block block = this.a(i6, i7, i8);

                        if (block == Blocks.ab || block == Blocks.k || block == Blocks.l) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean a(AxisAlignedBB axisalignedbb, Material material, Entity entity) {
        int i0 = MathHelper.c(axisalignedbb.a);
        int i1 = MathHelper.c(axisalignedbb.d + 1.0D);
        int i2 = MathHelper.c(axisalignedbb.b);
        int i3 = MathHelper.c(axisalignedbb.e + 1.0D);
        int i4 = MathHelper.c(axisalignedbb.c);
        int i5 = MathHelper.c(axisalignedbb.f + 1.0D);

        if (!this.b(i0, i2, i4, i1, i3, i5)) {
            return false;
        }
        else {
            boolean flag0 = false;
            Vec3 vec3 = this.U().a(0.0D, 0.0D, 0.0D);

            for (int i6 = i0; i6 < i1; ++i6) {
                for (int i7 = i2; i7 < i3; ++i7) {
                    for (int i8 = i4; i8 < i5; ++i8) {
                        Block block = this.a(i6, i7, i8);

                        if (block.o() == material) {
                            double d0 = (double) ((float) (i7 + 1) - BlockLiquid.b(this.e(i6, i7, i8)));

                            if ((double) i3 >= d0) {
                                flag0 = true;
                                block.a(this, i6, i7, i8, entity, vec3);
                            }
                        }
                    }
                }
            }

            if (vec3.b() > 0.0D && entity.aC()) {
                vec3 = vec3.a();
                double d1 = 0.014D;

                entity.w += vec3.c * d1;
                entity.x += vec3.d * d1;
                entity.y += vec3.e * d1;
            }

            return flag0;
        }
    }

    public boolean a(AxisAlignedBB axisalignedbb, Material material) {
        int i0 = MathHelper.c(axisalignedbb.a);
        int i1 = MathHelper.c(axisalignedbb.d + 1.0D);
        int i2 = MathHelper.c(axisalignedbb.b);
        int i3 = MathHelper.c(axisalignedbb.e + 1.0D);
        int i4 = MathHelper.c(axisalignedbb.c);
        int i5 = MathHelper.c(axisalignedbb.f + 1.0D);

        for (int i6 = i0; i6 < i1; ++i6) {
            for (int i7 = i2; i7 < i3; ++i7) {
                for (int i8 = i4; i8 < i5; ++i8) {
                    if (this.a(i6, i7, i8).o() == material) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean b(AxisAlignedBB axisalignedbb, Material material) {
        int i0 = MathHelper.c(axisalignedbb.a);
        int i1 = MathHelper.c(axisalignedbb.d + 1.0D);
        int i2 = MathHelper.c(axisalignedbb.b);
        int i3 = MathHelper.c(axisalignedbb.e + 1.0D);
        int i4 = MathHelper.c(axisalignedbb.c);
        int i5 = MathHelper.c(axisalignedbb.f + 1.0D);

        for (int i6 = i0; i6 < i1; ++i6) {
            for (int i7 = i2; i7 < i3; ++i7) {
                for (int i8 = i4; i8 < i5; ++i8) {
                    Block block = this.a(i6, i7, i8);

                    if (block.o() == material) {
                        int i9 = this.e(i6, i7, i8);
                        double d0 = (double) (i7 + 1);

                        if (i9 < 8) {
                            d0 = (double) (i7 + 1) - (double) i9 / 8.0D;
                        }

                        if (d0 >= axisalignedbb.b) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public Explosion a(Entity entity, double d0, double d1, double d2, float f0, boolean flag0) {
        return this.a(entity, d0, d1, d2, f0, false, flag0);
    }

    public Explosion a(Entity entity, double d0, double d1, double d2, float f0, boolean flag0, boolean flag1) {
        Explosion explosion = new Explosion(this, entity, d0, d1, d2, f0);

        explosion.a = flag0;
        explosion.b = flag1;
        explosion.a();
        explosion.a(true);
        return explosion;
    }

    public float a(Vec3 vec3, AxisAlignedBB axisalignedbb) {
        double d0 = 1.0D / ((axisalignedbb.d - axisalignedbb.a) * 2.0D + 1.0D);
        double d1 = 1.0D / ((axisalignedbb.e - axisalignedbb.b) * 2.0D + 1.0D);
        double d2 = 1.0D / ((axisalignedbb.f - axisalignedbb.c) * 2.0D + 1.0D);
        int i0 = 0;
        int i1 = 0;

        for (float f0 = 0.0F; f0 <= 1.0F; f0 = (float) ((double) f0 + d0)) {
            for (float f1 = 0.0F; f1 <= 1.0F; f1 = (float) ((double) f1 + d1)) {
                for (float f2 = 0.0F; f2 <= 1.0F; f2 = (float) ((double) f2 + d2)) {
                    double d3 = axisalignedbb.a + (axisalignedbb.d - axisalignedbb.a) * (double) f0;
                    double d4 = axisalignedbb.b + (axisalignedbb.e - axisalignedbb.b) * (double) f1;
                    double d5 = axisalignedbb.c + (axisalignedbb.f - axisalignedbb.c) * (double) f2;

                    if (this.a(this.U().a(d3, d4, d5), vec3) == null) {
                        ++i0;
                    }

                    ++i1;
                }
            }
        }

        return (float) i0 / (float) i1;
    }

    public boolean a(EntityPlayer entityplayer, int i0, int i1, int i2, int i3) {
        if (i3 == 0) {
            --i1;
        }

        if (i3 == 1) {
            ++i1;
        }

        if (i3 == 2) {
            --i2;
        }

        if (i3 == 3) {
            ++i2;
        }

        if (i3 == 4) {
            --i0;
        }

        if (i3 == 5) {
            ++i0;
        }

        if (this.a(i0, i1, i2) == Blocks.ab) {
            this.a(entityplayer, 1004, i0, i1, i2, 0);
            this.f(i0, i1, i2);
            return true;
        }
        else {
            return false;
        }
    }

    public TileEntity o(int i0, int i1, int i2) {
        if (i1 >= 0 && i1 < 256) {
            TileEntity tileentity = null;
            int i3;
            TileEntity tileentity1;

            if (this.N) {
                for (i3 = 0; i3 < this.a.size(); ++i3) {
                    tileentity1 = (TileEntity) this.a.get(i3);
                    if (!tileentity1.r() && tileentity1.c == i0 && tileentity1.d == i1 && tileentity1.e == i2) {
                        tileentity = tileentity1;
                        break;
                    }
                }
            }

            if (tileentity == null) {
                Chunk chunk = this.e(i0 >> 4, i2 >> 4);

                if (chunk != null) {
                    tileentity = chunk.e(i0 & 15, i1, i2 & 15);
                }
            }

            if (tileentity == null) {
                for (i3 = 0; i3 < this.a.size(); ++i3) {
                    tileentity1 = (TileEntity) this.a.get(i3);
                    if (!tileentity1.r() && tileentity1.c == i0 && tileentity1.d == i1 && tileentity1.e == i2) {
                        tileentity = tileentity1;
                        break;
                    }
                }
            }

            return tileentity;
        }
        else {
            return null;
        }
    }

    public void a(int i0, int i1, int i2, TileEntity tileentity) {
        if (tileentity != null && !tileentity.r()) {
            if (this.N) {
                tileentity.c = i0;
                tileentity.d = i1;
                tileentity.e = i2;
                Iterator iterator = this.a.iterator();

                while (iterator.hasNext()) {
                    TileEntity tileentity1 = (TileEntity) iterator.next();

                    if (tileentity1.c == i0 && tileentity1.d == i1 && tileentity1.e == i2) {
                        tileentity1.s();
                        iterator.remove();
                    }
                }

                this.a.add(tileentity);
            }
            else {
                this.g.add(tileentity);
                Chunk chunk = this.e(i0 >> 4, i2 >> 4);

                if (chunk != null) {
                    chunk.a(i0 & 15, i1, i2 & 15, tileentity);
                }
            }
        }
    }

    public void p(int i0, int i1, int i2) {
        TileEntity tileentity = this.o(i0, i1, i2);

        if (tileentity != null && this.N) {
            tileentity.s();
            this.a.remove(tileentity);
        }
        else {
            if (tileentity != null) {
                this.a.remove(tileentity);
                this.g.remove(tileentity);
            }

            Chunk chunk = this.e(i0 >> 4, i2 >> 4);

            if (chunk != null) {
                chunk.f(i0 & 15, i1, i2 & 15);
            }
        }
    }

    public void a(TileEntity tileentity) {
        this.b.add(tileentity);
    }

    public boolean q(int i0, int i1, int i2) {
        AxisAlignedBB axisalignedbb = this.a(i0, i1, i2).a(this, i0, i1, i2);

        return axisalignedbb != null && axisalignedbb.b() >= 1.0D;
    }

    public static boolean a(IBlockAccess iblockaccess, int i0, int i1, int i2) {
        Block block = iblockaccess.a(i0, i1, i2);
        int i3 = iblockaccess.e(i0, i1, i2);

        return block.o().k() && block.d() ? true : (block instanceof BlockStairs ? (i3 & 4) == 4 : (block instanceof BlockSlab ? (i3 & 8) == 8 : (block instanceof BlockHopper ? true : (block instanceof BlockSnow ? (i3 & 7) == 7 : false))));
    }

    public boolean c(int i0, int i1, int i2, boolean flag0) {
        if (i0 >= -30000000 && i2 >= -30000000 && i0 < 30000000 && i2 < 30000000) {
            Chunk chunk = this.v.d(i0 >> 4, i2 >> 4);

            if (chunk != null && !chunk.g()) {
                Block block = this.a(i0, i1, i2);

                return block.o().k() && block.d();
            }
            else {
                return flag0;
            }
        }
        else {
            return flag0;
        }
    }

    public void A() {
        int i0 = this.a(1.0F);

        if (i0 != this.j) {
            this.j = i0;
        }
    }

    public void a(boolean flag0, boolean flag1) {
        this.G = flag0;
        this.H = flag1;
    }

    public void b() {
        this.o();
    }

    private void a() {
        if (this.x.p()) {
            this.n = 1.0F;
            if (this.x.n()) {
                this.p = 1.0F;
            }
        }
    }

    protected void o() {
        if (!this.t.g) {
            if (!this.E) {
                int i0 = this.x.o();

                if (i0 <= 0) {
                    if (this.x.n()) {
                        this.x.f(this.s.nextInt(12000) + 3600);
                    }
                    else {
                        this.x.f(this.s.nextInt(168000) + 12000);
                    }
                }
                else {
                    --i0;
                    this.x.f(i0);
                    if (i0 <= 0) {
                        // CanaryMod: WeatherChange (Thunder)
                        WeatherChangeHook hook = (WeatherChangeHook) new WeatherChangeHook(canaryDimension, !this.x.n(), true).call();
                        if (!hook.isCanceled()) {
                            this.x.a(!this.x.n());
                        }
                        //
                    }
                    this.o = this.p;
                    if (this.x.n()) {
                        this.p = (float) ((double) this.p + 0.01D);
                    }
                    else {
                        this.p = (float) ((double) this.p - 0.01D);
                    }

                    this.p = MathHelper.a(this.p, 0.0F, 1.0F);
                    int i1 = this.x.q();

                    if (i1 <= 0) {
                        if (this.x.p()) {
                            this.x.g(this.s.nextInt(12000) + 12000);
                        }
                        else {
                            this.x.g(this.s.nextInt(168000) + 12000);
                        }
                    }
                    else {
                        --i1;
                        this.x.g(i1);
                        if (i1 <= 0) {
                            // CanaryMod: WeatherChange (Rain)
                            WeatherChangeHook hook = (WeatherChangeHook) new WeatherChangeHook(canaryDimension, !this.x.p(), false).call();
                            if (!hook.isCanceled()) {
                                this.x.b(!this.x.p());
                            }
                            //
                        }
                    }

                    this.m = this.n;
                    if (this.x.p()) {
                        this.n = (float) ((double) this.n + 0.01D);
                    }
                    else {
                        this.n = (float) ((double) this.n - 0.01D);
                    }

                    this.n = MathHelper.a(this.n, 0.0F, 1.0F);
                }
            }
        }
    }

    protected void B() {
        this.F.clear();
        this.C.a("buildList");

        int i0;
        EntityPlayer entityplayer;
        int i1;
        int i2;

        for (i0 = 0; i0 < this.h.size(); ++i0) {
            entityplayer = (EntityPlayer) this.h.get(i0);
            i1 = MathHelper.c(entityplayer.t / 16.0D);
            i2 = MathHelper.c(entityplayer.v / 16.0D);
            byte b0 = 7;

            for (int i3 = -b0; i3 <= b0; ++i3) {
                for (int i4 = -b0; i4 <= b0; ++i4) {
                    this.F.add(new ChunkCoordIntPair(i3 + i1, i4 + i2));
                }
            }
        }

        this.C.b();
        if (this.L > 0) {
            --this.L;
        }

        this.C.a("playerCheckLight");
        if (!this.h.isEmpty()) {
            i0 = this.s.nextInt(this.h.size());
            entityplayer = (EntityPlayer) this.h.get(i0);
            i1 = MathHelper.c(entityplayer.t) + this.s.nextInt(11) - 5;
            i2 = MathHelper.c(entityplayer.u) + this.s.nextInt(11) - 5;
            int i5 = MathHelper.c(entityplayer.v) + this.s.nextInt(11) - 5;

            this.t(i1, i2, i5);
        }

        this.C.b();
    }

    protected void a(int i0, int i1, Chunk chunk) {
        this.C.c("moodSound");
        if (this.L == 0 && !this.E) {
            this.k = this.k * 3 + 1013904223;
            int i2 = this.k >> 2;
            int i3 = i2 & 15;
            int i4 = i2 >> 8 & 15;
            int i5 = i2 >> 16 & 255;
            Block block = chunk.a(i3, i5, i4);

            i3 += i0;
            i4 += i1;
            if (block.o() == Material.a && this.j(i3, i5, i4) <= this.s.nextInt(8) && this.b(EnumSkyBlock.Sky, i3, i5, i4) <= 0) {
                EntityPlayer entityplayer = this.a((double) i3 + 0.5D, (double) i5 + 0.5D, (double) i4 + 0.5D, 8.0D);

                if (entityplayer != null && entityplayer.e((double) i3 + 0.5D, (double) i5 + 0.5D, (double) i4 + 0.5D) > 4.0D) {
                    this.a((double) i3 + 0.5D, (double) i5 + 0.5D, (double) i4 + 0.5D, "ambient.cave.cave", 0.7F, 0.8F + this.s.nextFloat() * 0.2F);
                    this.L = this.s.nextInt(12000) + 6000;
                }
            }
        }

        this.C.c("checkLight");
        chunk.o();
    }

    protected void g() {
        this.B();
    }

    public boolean r(int i0, int i1, int i2) {
        return this.d(i0, i1, i2, false);
    }

    public boolean s(int i0, int i1, int i2) {
        return this.d(i0, i1, i2, true);
    }

    public boolean d(int i0, int i1, int i2, boolean flag0) {
        BiomeGenBase biomegenbase = this.a(i0, i2);
        float f0 = biomegenbase.a(i0, i1, i2);

        if (f0 > 0.15F) {
            return false;
        }
        else {
            if (i1 >= 0 && i1 < 256 && this.b(EnumSkyBlock.Block, i0, i1, i2) < 10) {
                Block block = this.a(i0, i1, i2);

                if ((block == Blocks.j || block == Blocks.i) && this.e(i0, i1, i2) == 0) {
                    if (!flag0) {
                        return true;
                    }

                    boolean flag1 = true;

                    if (flag1 && this.a(i0 - 1, i1, i2).o() != Material.h) {
                        flag1 = false;
                    }

                    if (flag1 && this.a(i0 + 1, i1, i2).o() != Material.h) {
                        flag1 = false;
                    }

                    if (flag1 && this.a(i0, i1, i2 - 1).o() != Material.h) {
                        flag1 = false;
                    }

                    if (flag1 && this.a(i0, i1, i2 + 1).o() != Material.h) {
                        flag1 = false;
                    }

                    if (!flag1) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public boolean e(int i0, int i1, int i2, boolean flag0) {
        BiomeGenBase biomegenbase = this.a(i0, i2);
        float f0 = biomegenbase.a(i0, i1, i2);

        if (f0 > 0.15F) {
            return false;
        }
        else if (!flag0) {
            return true;
        }
        else {
            if (i1 >= 0 && i1 < 256 && this.b(EnumSkyBlock.Block, i0, i1, i2) < 10) {
                Block block = this.a(i0, i1, i2);

                if (block.o() == Material.a && Blocks.aC.c(this, i0, i1, i2)) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean t(int i0, int i1, int i2) {
        boolean flag0 = false;
        if (!this.t.g) {
            flag0 |= this.c(EnumSkyBlock.Sky, i0, i1, i2);
        }

        flag0 |= this.c(EnumSkyBlock.Block, i0, i1, i2);
        return flag0;
    }

    private int a(int i0, int i1, int i2, EnumSkyBlock enumskyblock) {
        if (enumskyblock == EnumSkyBlock.Sky && this.i(i0, i1, i2)) {
            return 15;
        }
        else {
            Block block = this.a(i0, i1, i2);
            int i3 = enumskyblock == EnumSkyBlock.Sky ? 0 : block.m();
            int i4 = block.k();

            if (i4 >= 15 && block.m() > 0) {
                i4 = 1;
            }

            if (i4 < 1) {
                i4 = 1;
            }

            if (i4 >= 15) {
                return 0;
            }
            else if (i3 >= 14) {
                return i3;
            }
            else {
                for (int i5 = 0; i5 < 6; ++i5) {
                    int i6 = i0 + Facing.b[i5];
                    int i7 = i1 + Facing.c[i5];
                    int i8 = i2 + Facing.d[i5];
                    int i9 = this.b(enumskyblock, i6, i7, i8) - i4;

                    if (i9 > i3) {
                        i3 = i9;
                    }

                    if (i3 >= 14) {
                        return i3;
                    }
                }

                return i3;
            }
        }
    }

    public boolean c(EnumSkyBlock enumskyblock, int i0, int i1, int i2) {
        if (!this.a(i0, i1, i2, 17)) {
            return false;
        }
        else {
            int i3 = 0;
            int i4 = 0;

            this.C.a("getBrightness");
            int i5 = this.b(enumskyblock, i0, i1, i2);
            int i6 = this.a(i0, i1, i2, enumskyblock);
            int i7;
            int i8;
            int i9;
            int i10;
            int i11;
            int i12;
            int i13;
            int i14;
            int i15;

            if (i6 > i5) {
                this.I[i4++] = 133152;
            }
            else if (i6 < i5) {
                this.I[i4++] = 133152 | i5 << 18;

                while (i3 < i4) {
                    i7 = this.I[i3++];
                    i8 = (i7 & 63) - 32 + i0;
                    i9 = (i7 >> 6 & 63) - 32 + i1;
                    i10 = (i7 >> 12 & 63) - 32 + i2;
                    i11 = i7 >> 18 & 15;
                    i12 = this.b(enumskyblock, i8, i9, i10);
                    if (i12 == i11) {
                        this.b(enumskyblock, i8, i9, i10, 0);
                        if (i11 > 0) {
                            i13 = MathHelper.a(i8 - i0);
                            i15 = MathHelper.a(i9 - i1);
                            i14 = MathHelper.a(i10 - i2);
                            if (i13 + i15 + i14 < 17) {
                                for (int i16 = 0; i16 < 6; ++i16) {
                                    int i17 = i8 + Facing.b[i16];
                                    int i18 = i9 + Facing.c[i16];
                                    int i19 = i10 + Facing.d[i16];
                                    int i20 = Math.max(1, this.a(i17, i18, i19).k());

                                    i12 = this.b(enumskyblock, i17, i18, i19);
                                    if (i12 == i11 - i20 && i4 < this.I.length) {
                                        this.I[i4++] = i17 - i0 + 32 | i18 - i1 + 32 << 6 | i19 - i2 + 32 << 12 | i11 - i20 << 18;
                                    }
                                }
                            }
                        }
                    }
                }

                i3 = 0;
            }

            this.C.b();
            this.C.a("checkedPosition < toCheckCount");

            while (i3 < i4) {
                i7 = this.I[i3++];
                i8 = (i7 & 63) - 32 + i0;
                i9 = (i7 >> 6 & 63) - 32 + i1;
                i10 = (i7 >> 12 & 63) - 32 + i2;
                i11 = this.b(enumskyblock, i8, i9, i10);
                i12 = this.a(i8, i9, i10, enumskyblock);
                if (i12 != i11) {
                    this.b(enumskyblock, i8, i9, i10, i12);
                    if (i12 > i11) {
                        i13 = Math.abs(i8 - i0);
                        i15 = Math.abs(i9 - i1);
                        i14 = Math.abs(i10 - i2);
                        boolean flag0 = i4 < this.I.length - 6;

                        if (i13 + i15 + i14 < 17 && flag0) {
                            if (this.b(enumskyblock, i8 - 1, i9, i10) < i12) {
                                this.I[i4++] = i8 - 1 - i0 + 32 + (i9 - i1 + 32 << 6) + (i10 - i2 + 32 << 12);
                            }

                            if (this.b(enumskyblock, i8 + 1, i9, i10) < i12) {
                                this.I[i4++] = i8 + 1 - i0 + 32 + (i9 - i1 + 32 << 6) + (i10 - i2 + 32 << 12);
                            }

                            if (this.b(enumskyblock, i8, i9 - 1, i10) < i12) {
                                this.I[i4++] = i8 - i0 + 32 + (i9 - 1 - i1 + 32 << 6) + (i10 - i2 + 32 << 12);
                            }

                            if (this.b(enumskyblock, i8, i9 + 1, i10) < i12) {
                                this.I[i4++] = i8 - i0 + 32 + (i9 + 1 - i1 + 32 << 6) + (i10 - i2 + 32 << 12);
                            }

                            if (this.b(enumskyblock, i8, i9, i10 - 1) < i12) {
                                this.I[i4++] = i8 - i0 + 32 + (i9 - i1 + 32 << 6) + (i10 - 1 - i2 + 32 << 12);
                            }

                            if (this.b(enumskyblock, i8, i9, i10 + 1) < i12) {
                                this.I[i4++] = i8 - i0 + 32 + (i9 - i1 + 32 << 6) + (i10 + 1 - i2 + 32 << 12);
                            }
                        }
                    }
                }
            }

            this.C.b();
            return true;
        }
    }

    public boolean a(boolean flag0) {
        return false;
    }

    public List a(Chunk chunk, boolean flag0) {
        return null;
    }

    public List b(Entity entity, AxisAlignedBB axisalignedbb) {
        return this.a(entity, axisalignedbb, (IEntitySelector) null);
    }

    public List a(Entity entity, AxisAlignedBB axisalignedbb, IEntitySelector ientityselector) {
        ArrayList arraylist = new ArrayList();
        int i0 = MathHelper.c((axisalignedbb.a - 2.0D) / 16.0D);
        int i1 = MathHelper.c((axisalignedbb.d + 2.0D) / 16.0D);
        int i2 = MathHelper.c((axisalignedbb.c - 2.0D) / 16.0D);
        int i3 = MathHelper.c((axisalignedbb.f + 2.0D) / 16.0D);

        for (int i4 = i0; i4 <= i1; ++i4) {
            for (int i5 = i2; i5 <= i3; ++i5) {
                if (this.c(i4, i5)) {
                    this.e(i4, i5).a(entity, axisalignedbb, arraylist, ientityselector);
                }
            }
        }

        return arraylist;
    }

    public List a(Class oclass0, AxisAlignedBB axisalignedbb) {
        return this.a(oclass0, axisalignedbb, (IEntitySelector) null);
    }

    public List a(Class oclass0, AxisAlignedBB axisalignedbb, IEntitySelector ientityselector) {
        int i0 = MathHelper.c((axisalignedbb.a - 2.0D) / 16.0D);
        int i1 = MathHelper.c((axisalignedbb.d + 2.0D) / 16.0D);
        int i2 = MathHelper.c((axisalignedbb.c - 2.0D) / 16.0D);
        int i3 = MathHelper.c((axisalignedbb.f + 2.0D) / 16.0D);
        ArrayList arraylist = new ArrayList();

        for (int i4 = i0; i4 <= i1; ++i4) {
            for (int i5 = i2; i5 <= i3; ++i5) {
                if (this.c(i4, i5)) {
                    this.e(i4, i5).a(oclass0, axisalignedbb, arraylist, ientityselector);
                }
            }
        }

        return arraylist;
    }

    public Entity a(Class oclass0, AxisAlignedBB axisalignedbb, Entity entity) {
        List list = this.a(oclass0, axisalignedbb);
        Entity entity1 = null;
        double d0 = Double.MAX_VALUE;

        for (int i0 = 0; i0 < list.size(); ++i0) {
            Entity entity2 = (Entity) list.get(i0);

            if (entity2 != entity) {
                double d1 = entity.e(entity2);

                if (d1 <= d0) {
                    entity1 = entity2;
                    d0 = d1;
                }
            }
        }

        return entity1;
    }

    public abstract Entity a(int i0);

    public void b(int i0, int i1, int i2, TileEntity tileentity) {
        if (this.d(i0, i1, i2)) {
            this.d(i0, i2).e();
        }
    }

    public int a(Class oclass0) {
        int i0 = 0;

        for (int i1 = 0; i1 < this.e.size(); ++i1) {
            Entity entity = (Entity) this.e.get(i1);

            if ((!(entity instanceof EntityLiving) || !((EntityLiving) entity).bI()) && oclass0.isAssignableFrom(entity.getClass())) {
                ++i0;
            }
        }

        return i0;
    }

    public void a(List list) {
        this.e.addAll(list);

        for (int i0 = 0; i0 < list.size(); ++i0) {
            this.a((Entity) list.get(i0));
        }
    }

    public void b(List list) {
        this.f.addAll(list);
    }

    public boolean a(Block block, int i0, int i1, int i2, boolean flag0, int i3, Entity entity, ItemStack itemstack) {
        Block block1 = this.a(i0, i1, i2);
        AxisAlignedBB axisalignedbb = flag0 ? null : block.a(this, i0, i1, i2);

        return axisalignedbb != null && !this.a(axisalignedbb, entity) ? false : (block1.o() == Material.q && block == Blocks.bQ ? true : block1.o().j() && block.a(this, i0, i1, i2, i3, itemstack));
    }

    public PathEntity a(Entity entity, Entity entity1, float f0, boolean flag0, boolean flag1, boolean flag2, boolean flag3) {
        this.C.a("pathfind");
        int i0 = MathHelper.c(entity.t);
        int i1 = MathHelper.c(entity.u + 1.0D);
        int i2 = MathHelper.c(entity.v);
        int i3 = (int) (f0 + 16.0F);
        int i4 = i0 - i3;
        int i5 = i1 - i3;
        int i6 = i2 - i3;
        int i7 = i0 + i3;
        int i8 = i1 + i3;
        int i9 = i2 + i3;
        ChunkCache chunkcache = new ChunkCache(this, i4, i5, i6, i7, i8, i9, 0);
        PathEntity pathentity = (new PathFinder(chunkcache, flag0, flag1, flag2, flag3)).a(entity, entity1, f0);

        this.C.b();
        return pathentity;
    }

    public PathEntity a(Entity entity, int i0, int i1, int i2, float f0, boolean flag0, boolean flag1, boolean flag2, boolean flag3) {
        this.C.a("pathfind");
        int i3 = MathHelper.c(entity.t);
        int i4 = MathHelper.c(entity.u);
        int i5 = MathHelper.c(entity.v);
        int i6 = (int) (f0 + 8.0F);
        int i7 = i3 - i6;
        int i8 = i4 - i6;
        int i9 = i5 - i6;
        int i10 = i3 + i6;
        int i11 = i4 + i6;
        int i12 = i5 + i6;
        ChunkCache chunkcache = new ChunkCache(this, i7, i8, i9, i10, i11, i12, 0);
        PathEntity pathentity = (new PathFinder(chunkcache, flag0, flag1, flag2, flag3)).a(entity, i0, i1, i2, f0);

        this.C.b();
        return pathentity;
    }

    public int e(int i0, int i1, int i2, int i3) {
        return this.a(i0, i1, i2).c((IBlockAccess) this, i0, i1, i2, i3);
    }

    public int u(int i0, int i1, int i2) {
        byte b0 = 0;
        int i3 = Math.max(b0, this.e(i0, i1 - 1, i2, 0));

        if (i3 >= 15) {
            return i3;
        }
        else {
            i3 = Math.max(i3, this.e(i0, i1 + 1, i2, 1));
            if (i3 >= 15) {
                return i3;
            }
            else {
                i3 = Math.max(i3, this.e(i0, i1, i2 - 1, 2));
                if (i3 >= 15) {
                    return i3;
                }
                else {
                    i3 = Math.max(i3, this.e(i0, i1, i2 + 1, 3));
                    if (i3 >= 15) {
                        return i3;
                    }
                    else {
                        i3 = Math.max(i3, this.e(i0 - 1, i1, i2, 4));
                        if (i3 >= 15) {
                            return i3;
                        }
                        else {
                            i3 = Math.max(i3, this.e(i0 + 1, i1, i2, 5));
                            return i3 >= 15 ? i3 : i3;
                        }
                    }
                }
            }
        }
    }

    public boolean f(int i0, int i1, int i2, int i3) {
        return this.g(i0, i1, i2, i3) > 0;
    }

    public int g(int i0, int i1, int i2, int i3) {
        return this.a(i0, i1, i2).r() ? this.u(i0, i1, i2) : this.a(i0, i1, i2).b((IBlockAccess) this, i0, i1, i2, i3);
    }

    public boolean v(int i0, int i1, int i2) {
        return this.g(i0, i1 - 1, i2, 0) > 0 ? true : (this.g(i0, i1 + 1, i2, 1) > 0 ? true : (this.g(i0, i1, i2 - 1, 2) > 0 ? true : (this.g(i0, i1, i2 + 1, 3) > 0 ? true : (this.g(i0 - 1, i1, i2, 4) > 0 ? true : this.g(i0 + 1, i1, i2, 5) > 0))));
    }

    public int w(int i0, int i1, int i2) {
        int i3 = 0;

        for (int i4 = 0; i4 < 6; ++i4) {
            int i5 = this.g(i0 + Facing.b[i4], i1 + Facing.c[i4], i2 + Facing.d[i4], i4);

            if (i5 >= 15) {
                return 15;
            }

            if (i5 > i3) {
                i3 = i5;
            }
        }

        return i3;
    }

    public EntityPlayer a(Entity entity, double d0) {
        return this.a(entity.t, entity.u, entity.v, d0);
    }

    public EntityPlayer a(double d0, double d1, double d2, double d3) {
        double d4 = -1.0D;
        EntityPlayer entityplayer = null;

        for (int i0 = 0; i0 < this.h.size(); ++i0) {
            EntityPlayer entityplayer1 = (EntityPlayer) this.h.get(i0);
            double d5 = entityplayer1.e(d0, d1, d2);

            if ((d3 < 0.0D || d5 < d3 * d3) && (d4 == -1.0D || d5 < d4)) {
                d4 = d5;
                entityplayer = entityplayer1;
            }
        }

        return entityplayer;
    }

    public EntityPlayer b(Entity entity, double d0) {
        return this.b(entity.t, entity.u, entity.v, d0);
    }

    public EntityPlayer b(double d0, double d1, double d2, double d3) {
        double d4 = -1.0D;
        EntityPlayer entityplayer = null;

        for (int i0 = 0; i0 < this.h.size(); ++i0) {
            EntityPlayer entityplayer1 = (EntityPlayer) this.h.get(i0);

            if (!entityplayer1.bF.a && entityplayer1.Z()) {
                double d5 = entityplayer1.e(d0, d1, d2);
                double d6 = d3;

                if (entityplayer1.an()) {
                    d6 = d3 * 0.800000011920929D;
                }

                if (entityplayer1.ap()) {
                    float f0 = entityplayer1.bC();

                    if (f0 < 0.1F) {
                        f0 = 0.1F;
                    }

                    d6 *= (double) (0.7F * f0);
                }

                if ((d3 < 0.0D || d5 < d6 * d6) && (d4 == -1.0D || d5 < d4)) {
                    d4 = d5;
                    entityplayer = entityplayer1;
                }
            }
        }

        return entityplayer;
    }

    public EntityPlayer a(String s0) {
        for (int i0 = 0; i0 < this.h.size(); ++i0) {
            if (s0.equals(((EntityPlayer) this.h.get(i0)).b_())) {
                return (EntityPlayer) this.h.get(i0);
            }
        }

        return null;
    }

    public void F() throws MinecraftException {
        this.w.c();
    }

    public long G() {
        return this.x.b();
    }

    public long H() {
        return this.x.f();
    }

    public long I() {
        return this.x.g();
    }

    public void b(long i0) {
        // CanaryMod: TimeChange
        TimeChangeHook hook = (TimeChangeHook) new TimeChangeHook(canaryDimension, i0).call();
        if (!hook.isCanceled()) {
            this.x.c(i0);
        }
        //
    }

    public ChunkCoordinates J() {
        return new ChunkCoordinates(this.x.c(), this.x.d(), this.x.e());
    }

    public void x(int i0, int i1, int i2) {
        this.x.a(i0, i1, i2);
    }

    public boolean a(EntityPlayer entityplayer, int i0, int i1, int i2) {
        return true;
    }

    public void a(Entity entity, byte b0) {
    }

    public IChunkProvider K() {
        return this.v;
    }

    public void c(int i0, int i1, int i2, Block block, int i3, int i4) {
        block.a(this, i0, i1, i2, i3, i4);
    }

    public ISaveHandler L() {
        return this.w;
    }

    public WorldInfo M() {
        return this.x;
    }

    public GameRules N() {
        return this.x.x();
    }

    public void c() {
    }

    public float h(float f0) {
        return (this.o + (this.p - this.o) * f0) * this.j(f0);
    }

    public float j(float f0) {
        return this.m + (this.n - this.m) * f0;
    }

    public boolean O() {
        return (double) this.h(1.0F) > 0.9D;
    }

    public boolean P() {
        return (double) this.j(1.0F) > 0.2D;
    }

    public boolean y(int i0, int i1, int i2) {
        if (!this.P()) {
            return false;
        }
        else if (!this.i(i0, i1, i2)) {
            return false;
        }
        else if (this.h(i0, i2) > i1) {
            return false;
        }
        else {
            BiomeGenBase biomegenbase = this.a(i0, i2);

            return biomegenbase.d() ? false : (this.e(i0, i1, i2, false) ? false : biomegenbase.e());
        }
    }

    public boolean z(int i0, int i1, int i2) {
        BiomeGenBase biomegenbase = this.a(i0, i2);

        return biomegenbase.f();
    }

    public void a(String s0, WorldSavedData worldsaveddata) {
        this.z.a(s0, worldsaveddata);
    }

    public WorldSavedData a(Class oclass0, String s0) {
        return this.z.a(oclass0, s0);
    }

    public int b(String s0) {
        return this.z.a(s0);
    }

    public void b(int i0, int i1, int i2, int i3, int i4) {
        for (int i5 = 0; i5 < this.u.size(); ++i5) {
            ((IWorldAccess) this.u.get(i5)).a(i0, i1, i2, i3, i4);
        }
    }

    public void c(int i0, int i1, int i2, int i3, int i4) {
        this.a((EntityPlayer) null, i0, i1, i2, i3, i4);
    }

    public void a(EntityPlayer entityplayer, int i0, int i1, int i2, int i3, int i4) {
        try {
            for (int i5 = 0; i5 < this.u.size(); ++i5) {
                ((IWorldAccess) this.u.get(i5)).a(entityplayer, i0, i1, i2, i3, i4);
            }
        }
        catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Playing level event");
            CrashReportCategory crashreportcategory = crashreport.a("Level event being played");

            crashreportcategory.a("Block coordinates", (Object) CrashReportCategory.a(i1, i2, i3));
            crashreportcategory.a("Event source", (Object) entityplayer);
            crashreportcategory.a("Event type", (Object) Integer.valueOf(i0));
            crashreportcategory.a("Event data", (Object) Integer.valueOf(i4));
            throw new ReportedException(crashreport);
        }
    }

    public int Q() {
        return 256;
    }

    public int R() {
        return this.t.g ? 128 : 256;
    }

    public Random A(int i0, int i1, int i2) {
        long i3 = (long) i0 * 341873128712L + (long) i1 * 132897987541L + this.M().b() + (long) i2;

        this.s.setSeed(i3);
        return this.s;
    }

    public ChunkPosition b(String s0, int i0, int i1, int i2) {
        return this.K().a(this, s0, i0, i1, i2);
    }

    public CrashReportCategory a(CrashReport crashreport) {
        CrashReportCategory crashreportcategory = crashreport.a("Affected level", 1);

        crashreportcategory.a("Level name", (Object) (this.x == null ? "????" : this.x.k()));
        crashreportcategory.a("All players", new Callable() {

            public String call() {
                return World.this.h.size() + " total; " + World.this.h.toString();
            }
        });
        crashreportcategory.a("Chunk stats", new Callable() {

            public String call() {
                return World.this.v.e();
            }
        });

        try {
            this.x.a(crashreportcategory);
        }
        catch (Throwable throwable) {
            crashreportcategory.a("Level Data Unobtainable", throwable);
        }

        return crashreportcategory;
    }

    public void d(int i0, int i1, int i2, int i3, int i4) {
        for (int i5 = 0; i5 < this.u.size(); ++i5) {
            IWorldAccess iworldaccess = (IWorldAccess) this.u.get(i5);

            iworldaccess.b(i0, i1, i2, i3, i4);
        }
    }

    public Vec3Pool U() {
        return this.J;
    }

    public Calendar V() {
        if (this.H() % 600L == 0L) {
            this.K.setTimeInMillis(MinecraftServer.ap());
        }

        return this.K;
    }

    public Scoreboard W() {
        return this.D;
    }

    public void f(int i0, int i1, int i2, Block block) {
        for (int i3 = 0; i3 < 4; ++i3) {
            int i4 = i0 + Direction.a[i3];
            int i5 = i2 + Direction.b[i3];
            Block block1 = this.a(i4, i1, i5);

            if (Blocks.bU.e(block1)) {
                block1.a(this, i4, i1, i5, block);
            }
            else if (block1.r()) {
                i4 += Direction.a[i3];
                i5 += Direction.b[i3];
                Block block2 = this.a(i4, i1, i5);

                if (Blocks.bU.e(block2)) {
                    block2.a(this, i4, i1, i5, block);
                }
            }
        }
    }

    public float b(double d0, double d1, double d2) {
        return this.B(MathHelper.c(d0), MathHelper.c(d1), MathHelper.c(d2));
    }

    public float B(int i0, int i1, int i2) {
        float f0 = 0.0F;
        boolean flag0 = this.r == EnumDifficulty.HARD;

        if (this.d(i0, i1, i2)) {
            float f1 = this.x();

            f0 += MathHelper.a((float) this.d(i0, i2).s / 3600000.0F, 0.0F, 1.0F) * (flag0 ? 1.0F : 0.75F);
            f0 += f1 * 0.25F;
        }

        if (this.r == EnumDifficulty.EASY || this.r == EnumDifficulty.PEACEFUL) {
            f0 *= (float) this.r.a() / 2.0F;
        }

        return MathHelper.a(f0, 0.0F, flag0 ? 1.5F : 1.0F);
    }

    public void X() {
        Iterator iterator = this.u.iterator();

        while (iterator.hasNext()) {
            IWorldAccess iworldaccess = (IWorldAccess) iterator.next();

            iworldaccess.b();
        }

    }

    /**
     * Get the canary dimension wrapper
     *
     * @return
     */
    public CanaryWorld getCanaryWorld() {
        return canaryDimension;
    }

    /**
     * Set the canary dimension wrapper
     *
     * @param dim
     */
    public void setCanaryWorld(CanaryWorld dim) {
        this.canaryDimension = dim;
    }

    /**
     * Checks if the Entity is allowed to spawn based on the world configuration
     *
     * @param entity
     *         the entity to check
     *
     * @return true if can spawn; false if not
     */
    protected final boolean canSpawn(Entity entity) {
        WorldConfiguration world_cfg = Configuration.getWorldConfig(this.canaryDimension.getFqName());
        String fqEntityName = entity.getCanaryEntity().getFqName();
        if (entity instanceof EntityAnimal || entity instanceof EntityWaterMob || entity instanceof EntityAmbientCreature) {
            if (!world_cfg.canSpawnAnimals()) {
                return false;
            }
            else if (entity instanceof EntityWaterMob) {
                if (!world_cfg.getSpawnableWaterAnimals().contains(fqEntityName)) {
                    return false;
                }
            } else if (!world_cfg.getSpawnableAnimals().contains(fqEntityName)) {
                return false;
            }
        }
        else if (entity instanceof IMob) {
            if (!world_cfg.canSpawnMonsters()) {
                return false;
            }
            else if (!world_cfg.getSpawnableMobs().contains(fqEntityName)) {
                return false;
            }
        }
        else if (entity instanceof INpc && !world_cfg.canSpawnVillagers()) {
            return false;
        }
        else if (entity instanceof EntityGolem) {
            if (!world_cfg.canSpawnGolems()) {
                return false;
            }
            else if (!world_cfg.getSpawnableGolems().contains(fqEntityName)) {
                return false;
            }
        }
        return true;
    }
}

