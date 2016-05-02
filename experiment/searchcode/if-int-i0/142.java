package net.minecraft.world.chunk;

import net.canarymod.PortalReconstructJob;
import net.canarymod.api.world.CanaryChunk;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.CanaryBlock;
import net.canarymod.hook.world.PortalDestroyHook;
import net.canarymod.tasks.ServerTaskManager;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.command.IEntitySelector;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

public class Chunk {

    private static final Logger t = LogManager.getLogger();
    public static boolean a;
    private ExtendedBlockStorage[] u;
    public byte[] v; //CanaryMod private => public
    public int[] b;
    public boolean[] c;
    public boolean d;
    public World e;
    public int[] f;
    public final int g;
    public final int h;
    private boolean w;
    public Map i;
    public List[] j;
    public boolean k;
    public boolean l;
    public boolean m;
    public boolean n;
    public boolean o;
    public long p;
    public boolean q;
    public int r;
    public long s;
    private int x;
    private CanaryChunk canaryChunk; // CanaryMod: Chunk wrapper

    public Chunk(World world, int i0, int i1) {
        canaryChunk = new CanaryChunk(this); // CanaryMod: wrap chunk
        this.u = new ExtendedBlockStorage[16];
        this.v = new byte[256];
        this.b = new int[256];
        this.c = new boolean[256];
        this.i = new HashMap();
        this.x = 4096;
        this.j = new List[16];
        this.e = world;
        this.g = i0;
        this.h = i1;
        this.f = new int[256];

        for (int i2 = 0; i2 < this.j.length; ++i2) {
            this.j[i2] = new ArrayList();
        }

        Arrays.fill(this.b, -999);
        Arrays.fill(this.v, (byte) -1);
    }

    public Chunk(World world, Block[] ablock, int i0, int i1) {
        this(world, i0, i1);
        int i2 = ablock.length / 256;
        boolean flag0 = !world.t.g;

        for (int i3 = 0; i3 < 16; ++i3) {
            for (int i4 = 0; i4 < 16; ++i4) {
                for (int i5 = 0; i5 < i2; ++i5) {
                    Block block = ablock[i3 << 11 | i4 << 7 | i5];

                    if (block != null && block.o() != Material.a) {
                        int i6 = i5 >> 4;

                        if (this.u[i6] == null) {
                            this.u[i6] = new ExtendedBlockStorage(i6 << 4, flag0);
                        }

                        this.u[i6].a(i3, i5 & 15, i4, block);
                    }
                }
            }
        }

    }

    public Chunk(World world, Block[] ablock, byte[] abyte, int i0, int i1) {
        this(world, i0, i1);
        int i2 = ablock.length / 256;
        boolean flag0 = !world.t.g;

        for (int i3 = 0; i3 < 16; ++i3) {
            for (int i4 = 0; i4 < 16; ++i4) {
                for (int i5 = 0; i5 < i2; ++i5) {
                    int i6 = i3 * i2 * 16 | i4 * i2 | i5;
                    Block block = ablock[i6];

                    if (block != null && block != Blocks.a) {
                        int i7 = i5 >> 4;

                        if (this.u[i7] == null) {
                            this.u[i7] = new ExtendedBlockStorage(i7 << 4, flag0);
                        }

                        this.u[i7].a(i3, i5 & 15, i4, block);
                        this.u[i7].a(i3, i5 & 15, i4, abyte[i6]);
                    }
                }
            }
        }

    }

    public boolean a(int i0, int i1) {
        return i0 == this.g && i1 == this.h;
    }

    public int b(int i0, int i1) {
        return this.f[i1 << 4 | i0];
    }

    public int h() {
        for (int i0 = this.u.length - 1; i0 >= 0; --i0) {
            if (this.u[i0] != null) {
                return this.u[i0].d();
            }
        }

        return 0;
    }

    public ExtendedBlockStorage[] i() {
        return this.u;
    }

    public void b() {
        int i0 = this.h();

        this.r = Integer.MAX_VALUE;

        for (int i1 = 0; i1 < 16; ++i1) {
            int i2 = 0;

            while (i2 < 16) {
                this.b[i1 + (i2 << 4)] = -999;
                int i3 = i0 + 16 - 1;

                while (true) {
                    if (i3 > 0) {
                        if (this.b(i1, i3 - 1, i2) == 0) {
                            --i3;
                            continue;
                        }

                        this.f[i2 << 4 | i1] = i3;
                        if (i3 < this.r) {
                            this.r = i3;
                        }
                    }

                    if (!this.e.t.g) {
                        i3 = 15;
                        int i4 = i0 + 16 - 1;

                        do {
                            int i5 = this.b(i1, i4, i2);

                            if (i5 == 0 && i3 != 15) {
                                i5 = 1;
                            }

                            i3 -= i5;
                            if (i3 > 0) {
                                ExtendedBlockStorage extendedblockstorage = this.u[i4 >> 4];

                                if (extendedblockstorage != null) {
                                    extendedblockstorage.b(i1, i4 & 15, i2, i3);
                                    this.e.m((this.g << 4) + i1, i4, (this.h << 4) + i2);
                                }
                            }

                            --i4;
                        } while (i4 > 0 && i3 > 0);
                    }

                    ++i2;
                    break;
                }
            }
        }

        this.n = true;
    }

    private void e(int i0, int i1) {
        this.c[i0 + i1 * 16] = true;
        this.w = true;
    }

    //CanaryMod private->public
    public void c(boolean flag0) {
        this.e.C.a("recheckGaps");
        if (this.e.a(this.g * 16 + 8, 0, this.h * 16 + 8, 16)) {
            for (int i0 = 0; i0 < 16; ++i0) {
                for (int i1 = 0; i1 < 16; ++i1) {
                    if (this.c[i0 + i1 * 16]) {
                        this.c[i0 + i1 * 16] = false;
                        int i2 = this.b(i0, i1);
                        int i3 = this.g * 16 + i0;
                        int i4 = this.h * 16 + i1;
                        int i5 = this.e.g(i3 - 1, i4);
                        int i6 = this.e.g(i3 + 1, i4);
                        int i7 = this.e.g(i3, i4 - 1);
                        int i8 = this.e.g(i3, i4 + 1);

                        if (i6 < i5) {
                            i5 = i6;
                        }

                        if (i7 < i5) {
                            i5 = i7;
                        }

                        if (i8 < i5) {
                            i5 = i8;
                        }

                        this.g(i3, i4, i5);
                        this.g(i3 - 1, i4, i2);
                        this.g(i3 + 1, i4, i2);
                        this.g(i3, i4 - 1, i2);
                        this.g(i3, i4 + 1, i2);
                        if (flag0) {
                            this.e.C.b();
                            return;
                        }
                    }
                }
            }

            this.w = false;
        }

        this.e.C.b();
    }

    private void g(int i0, int i1, int i2) {
        int i3 = this.e.f(i0, i1);

        if (i3 > i2) {
            this.c(i0, i1, i2, i3 + 1);
        }
        else if (i3 < i2) {
            this.c(i0, i1, i3, i2 + 1);
        }
    }

    private void c(int i0, int i1, int i2, int i3) {
        if (i3 > i2 && this.e.a(i0, 0, i1, 16)) {
            for (int i4 = i2; i4 < i3; ++i4) {
                this.e.c(EnumSkyBlock.Sky, i0, i4, i1);
            }

            this.n = true;
        }
    }

    //CanaryMod private->public
    public void h(int i0, int i1, int i2) {
        int i3 = this.f[i2 << 4 | i0] & 255;
        int i4 = i3;

        if (i1 > i3) {
            i4 = i1;
        }

        while (i4 > 0 && this.b(i0, i4 - 1, i2) == 0) {
            --i4;
        }

        if (i4 != i3) {
            this.e.b(i0 + this.g * 16, i2 + this.h * 16, i4, i3);
            this.f[i2 << 4 | i0] = i4;
            int i5 = this.g * 16 + i0;
            int i6 = this.h * 16 + i2;
            int i7;
            int i8;

            if (!this.e.t.g) {
                ExtendedBlockStorage extendedblockstorage;

                if (i4 < i3) {
                    for (i7 = i4; i7 < i3; ++i7) {
                        extendedblockstorage = this.u[i7 >> 4];
                        if (extendedblockstorage != null) {
                            extendedblockstorage.b(i0, i7 & 15, i2, 15);
                            this.e.m((this.g << 4) + i0, i7, (this.h << 4) + i2);
                        }
                    }
                }
                else {
                    for (i7 = i3; i7 < i4; ++i7) {
                        // CanaryMod start: Catch corrupt index info
                        if (i7 >> 4 < 0 || i7 >> 4 >= 16) {
                            t.warn("Invalid chunk info array index: " + (i7 >> 4));
                            t.warn("x: " + i3 + ", z: " + i4);
                            t.warn("Chunk location: " + i5 + ", " + i6);
                            i7 = 0;
                        }
                        // CanaryMod end
                        extendedblockstorage = this.u[i7 >> 4];
                        if (extendedblockstorage != null) {
                            extendedblockstorage.b(i0, i7 & 15, i2, 0);
                            this.e.m((this.g << 4) + i0, i7, (this.h << 4) + i2);
                        }
                    }
                }

                i7 = 15;

                while (i4 > 0 && i7 > 0) {
                    --i4;
                    i8 = this.b(i0, i4, i2);
                    if (i8 == 0) {
                        i8 = 1;
                    }

                    i7 -= i8;
                    if (i7 < 0) {
                        i7 = 0;
                    }

                    ExtendedBlockStorage extendedblockstorage1 = this.u[i4 >> 4];

                    if (extendedblockstorage1 != null) {
                        extendedblockstorage1.b(i0, i4 & 15, i2, i7);
                    }
                }
            }

            i7 = this.f[i2 << 4 | i0];
            i8 = i3;
            int i9 = i7;

            if (i7 < i3) {
                i8 = i7;
                i9 = i3;
            }

            if (i7 < this.r) {
                this.r = i7;
            }

            if (!this.e.t.g) {
                this.c(i5 - 1, i6, i8, i9);
                this.c(i5 + 1, i6, i8, i9);
                this.c(i5, i6 - 1, i8, i9);
                this.c(i5, i6 + 1, i8, i9);
                this.c(i5, i6, i8, i9);
            }

            this.n = true;
        }
    }

    public int b(int i0, int i1, int i2) {
        return this.a(i0, i1, i2).k();
    }

    public Block a(final int i0, final int i1, final int i2) {
        Block block = Blocks.a;

        if (i1 >> 4 < this.u.length) {
            ExtendedBlockStorage extendedblockstorage = this.u[i1 >> 4];

            if (extendedblockstorage != null) {
                try {
                    block = extendedblockstorage.a(i0, i1 & 15, i2);
                }
                catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.a(throwable, "Getting block");
                    CrashReportCategory crashreportcategory = crashreport.a("Block being got");

                    crashreportcategory.a("Location", new Callable() {

                        public String call() {
                            return CrashReportCategory.a(i0, i1, i2);
                        }
                    });
                    throw new ReportedException(crashreport);
                }
            }
        }
        return block;
    }

    public int c(int i0, int i1, int i2) {
        if (i1 >> 4 >= this.u.length) {
            return 0;
        }
        else {
            ExtendedBlockStorage extendedblockstorage = this.u[i1 >> 4];

            return extendedblockstorage != null ? extendedblockstorage.b(i0, i1 & 15, i2) : 0;
        }
    }

    public boolean a(int i0, int i1, int i2, Block block, int i3) {
        return this.a(i0, i1, i2, block, i3, true); // CanaryMod: Redirect
    }

    public boolean a(int i0, int i1, int i2, Block block, int i3, boolean checkPortal) { // CanaryMod: add Portal Check
        int i4 = i2 << 4 | i0;

        if (i1 >= this.b[i4] - 1) {
            this.b[i4] = -999;
        }

        int i5 = this.f[i4];
        Block block1 = this.a(i0, i1, i2);
        int i6 = this.c(i0, i1, i2);

        if (block1 == block && i6 == i3) {
            return false;
        }
        else {
            // CanaryMod: Start - check if removed block is portal block
            int portalPointX = this.g * 16 + i0;
            int portalPointZ = this.h * 16 + i2;

            if (checkPortal == true) {
                int portalPointY = i1;

                int portalId = BlockType.Portal.getId();
                net.canarymod.api.world.World world = canaryChunk.getDimension();
                if (world != null && world.getBlockAt(portalPointX, portalPointY, portalPointZ).getTypeId() == portalId) {
                    // These will be equal 1 if the portal is defined on their axis and 0 if not.
                    int portalXOffset = (canaryChunk.getDimension().getBlockAt(portalPointX - 1, portalPointY, portalPointZ).getTypeId() == portalId || canaryChunk.getDimension().getBlockAt(portalPointX + 1, portalPointY, portalPointZ).getTypeId() == portalId) ? 1 : 0;
                    int portalZOffset = (canaryChunk.getDimension().getBlockAt(portalPointX, portalPointY, portalPointZ - 1).getTypeId() == portalId || canaryChunk.getDimension().getBlockAt(portalPointX, portalPointY, portalPointZ + 1).getTypeId() == portalId) ? 1 : 0;

                    // If the portal is either x aligned or z aligned but not both (has neighbor portal in x or z plane but not both)
                    if (portalXOffset != portalZOffset) {
                        // Get the edge of the portal.
                        int portalX = portalPointX - ((canaryChunk.getDimension().getBlockAt(portalPointX - 1, portalPointY, portalPointZ).getTypeId() == portalId) ? 1 : 0);
                        int portalZ = portalPointZ - ((canaryChunk.getDimension().getBlockAt(portalPointX, portalPointY, portalPointZ - 1).getTypeId() == portalId) ? 1 : 0);
                        int portalY = i1;

                        while (canaryChunk.getDimension().getBlockAt(portalX, ++portalY, portalZ).getTypeId() == portalId) {
                            ;
                        }
                        portalY -= 1;
                        // Scan the portal and see if its still all there (2x3 formation)
                        boolean completePortal = true;
                        CanaryBlock[][] portalBlocks = new CanaryBlock[3][2];

                        for (int i9001 = 0; i9001 < 3 && completePortal; i9001 += 1) {
                            for (int i9002 = 0; i9002 < 2 && completePortal; i9002 += 1) {
                                portalBlocks[i9001][i9002] = (CanaryBlock) canaryChunk.getDimension().getBlockAt(portalX + i9002 * portalXOffset, portalY - i9001, portalZ + i9002 * portalZOffset);
                                if (portalBlocks[i9001][i9002].getTypeId() != portalId) {
                                    completePortal = false;
                                }
                            }
                        }
                        if (completePortal == true) {
                            // CanaryMod: PortalDestroy
                            PortalDestroyHook hook = (PortalDestroyHook) new PortalDestroyHook(portalBlocks).call();
                            if (hook.isCanceled()) {// Hook canceled = don't destroy the portal.
                                // in that case we need to reconstruct the portal's frame to make the portal valid.
                                // Problem is we don't want to reconstruct it right away because more blocks might be deleted (for example on explosion).
                                // In order to avoid spamming the hook for each destroyed block, I'm queuing the reconstruction of the portal instead.
                                ServerTaskManager.addTask(new PortalReconstructJob(this.e.getCanaryWorld(), portalX, portalY, portalZ, (portalXOffset == 1)));
                            }
                        }
                    }
                }
            }
            // CanaryMod: End - check if removed block is portal block0.

            ExtendedBlockStorage extendedblockstorage = this.u[i1 >> 4];
            boolean flag0 = false;

            if (extendedblockstorage == null) {
                if (block == Blocks.a) {
                    return false;
                }

                extendedblockstorage = this.u[i1 >> 4] = new ExtendedBlockStorage(i1 >> 4 << 4, !this.e.t.g);
                flag0 = i1 >= i5;
            }

            int i7 = this.g * 16 + i0;
            int i8 = this.h * 16 + i2;

            if (!this.e.E) {
                block1.f(this.e, i7, i1, i8, i6);
            }

            extendedblockstorage.a(i0, i1 & 15, i2, block);
            if (!this.e.E) {
                block1.a(this.e, i7, i1, i8, block1, i6);
            }
            else if (block1 instanceof ITileEntityProvider && block1 != block) {
                this.e.p(i7, i1, i8);
            }

            if (extendedblockstorage.a(i0, i1 & 15, i2) != block) {
                return false;
            }
            else {
                extendedblockstorage.a(i0, i1 & 15, i2, i3);
                if (flag0) {
                    this.b();
                }
                else {
                    int i9 = block.k();
                    int i10 = block1.k();

                    if (i9 > 0) {
                        if (i1 >= i5) {
                            this.h(i0, i1 + 1, i2);
                        }
                    }
                    else if (i1 == i5 - 1) {
                        this.h(i0, i1, i2);
                    }

                    if (i9 != i10 && (i9 < i10 || this.a(EnumSkyBlock.Sky, i0, i1, i2) > 0 || this.a(EnumSkyBlock.Block, i0, i1, i2) > 0)) {
                        this.e(i0, i2);
                    }
                }

                TileEntity tileentity;

                if (block1 instanceof ITileEntityProvider) {
                    tileentity = this.e(i0, i1, i2);
                    if (tileentity != null) {
                        tileentity.u();
                    }
                }

                if (!this.e.E) {
                    block.b(this.e, i7, i1, i8);
                }

                if (block instanceof ITileEntityProvider) {
                    tileentity = this.e(i0, i1, i2);
                    if (tileentity == null) {
                        tileentity = ((ITileEntityProvider) block).a(this.e, i3);
                        this.e.a(i7, i1, i8, tileentity);
                    }

                    if (tileentity != null) {
                        tileentity.u();
                    }
                }

                this.n = true;
                return true;
            }
        }
    }

    public boolean a(int i0, int i1, int i2, int i3) {
        ExtendedBlockStorage extendedblockstorage = this.u[i1 >> 4];

        if (extendedblockstorage == null) {
//          CanaryMod add new extended block storage
            this.u[i1 >> 4] = new ExtendedBlockStorage(i3, true);
            extendedblockstorage = this.u[i1 >> 4];
//          return false;
        } //else {
        int i4 = extendedblockstorage.b(i0, i1 & 15, i2);

        if (i4 == i3) {
            return false;
        }
        else {
            this.n = true;
            extendedblockstorage.a(i0, i1 & 15, i2, i3);
            if (extendedblockstorage.a(i0, i1 & 15, i2) instanceof ITileEntityProvider) {
                TileEntity tileentity = this.e(i0, i1, i2);

                if (tileentity != null) {
                    tileentity.u();
                    tileentity.g = i3;
                }
            }

            return true;
        }
    }

    public int a(EnumSkyBlock enumskyblock, int i0, int i1, int i2) {
        ExtendedBlockStorage extendedblockstorage = this.u[i1 >> 4];

        return extendedblockstorage == null ? (this.d(i0, i1, i2) ? enumskyblock.c : 0) : (enumskyblock == EnumSkyBlock.Sky ? (this.e.t.g ? 0 : extendedblockstorage.c(i0, i1 & 15, i2)) : (enumskyblock == EnumSkyBlock.Block ? extendedblockstorage.d(i0, i1 & 15, i2) : enumskyblock.c));
    }

    public void a(EnumSkyBlock enumskyblock, int i0, int i1, int i2, int i3) {
        ExtendedBlockStorage extendedblockstorage = this.u[i1 >> 4];

        if (extendedblockstorage == null) {
            extendedblockstorage = this.u[i1 >> 4] = new ExtendedBlockStorage(i1 >> 4 << 4, !this.e.t.g);
            this.b();
        }

        this.n = true;
        if (enumskyblock == EnumSkyBlock.Sky) {
            if (!this.e.t.g) {
                extendedblockstorage.b(i0, i1 & 15, i2, i3);
            }
        }
        else if (enumskyblock == EnumSkyBlock.Block) {
            extendedblockstorage.c(i0, i1 & 15, i2, i3);
        }
    }

    public int b(int i0, int i1, int i2, int i3) {
        ExtendedBlockStorage extendedblockstorage = this.u[i1 >> 4];

        if (extendedblockstorage == null) {
            return !this.e.t.g && i3 < EnumSkyBlock.Sky.c ? EnumSkyBlock.Sky.c - i3 : 0;
        }
        else {
            int i4 = this.e.t.g ? 0 : extendedblockstorage.c(i0, i1 & 15, i2);

            if (i4 > 0) {
                a = true;
            }

            i4 -= i3;
            int i5 = extendedblockstorage.d(i0, i1 & 15, i2);

            if (i5 > i4) {
                i4 = i5;
            }

            return i4;
        }
    }

    public void a(Entity entity) {
        this.o = true;
        int i0 = MathHelper.c(entity.t / 16.0D);
        int i1 = MathHelper.c(entity.v / 16.0D);

        if (i0 != this.g || i1 != this.h) {
            t.error("Wrong location! " + entity);
            Thread.dumpStack();
        }

        int i2 = MathHelper.c(entity.u / 16.0D);

        if (i2 < 0) {
            i2 = 0;
        }

        if (i2 >= this.j.length) {
            i2 = this.j.length - 1;
        }

        entity.ah = true;
        entity.ai = this.g;
        entity.aj = i2;
        entity.ak = this.h;
        this.j[i2].add(entity);
    }

    public void b(Entity entity) {
        this.a(entity, entity.aj);
    }

    public void a(Entity entity, int i0) {
        if (i0 < 0) {
            i0 = 0;
        }

        if (i0 >= this.j.length) {
            i0 = this.j.length - 1;
        }

        this.j[i0].remove(entity);
    }

    public boolean d(int i0, int i1, int i2) {
        return i1 >= this.f[i2 << 4 | i0];
    }

    public TileEntity e(int i0, int i1, int i2) {
        ChunkPosition chunkposition = new ChunkPosition(i0, i1, i2);
        TileEntity tileentity = (TileEntity) this.i.get(chunkposition);

        if (tileentity == null) {
            Block block = this.a(i0, i1, i2);

            if (!block.u()) {
                return null;
            }

            tileentity = ((ITileEntityProvider) block).a(this.e, this.c(i0, i1, i2));
            this.e.a(this.g * 16 + i0, i1, this.h * 16 + i2, tileentity);
        }

        if (tileentity != null && tileentity.r()) {
            this.i.remove(chunkposition);
            return null;
        }
        else {
            return tileentity;
        }
    }

    public void a(TileEntity tileentity) {
        int i0 = tileentity.c - this.g * 16;
        int i1 = tileentity.d;
        int i2 = tileentity.e - this.h * 16;

        this.a(i0, i1, i2, tileentity);
        if (this.d) {
            this.e.g.add(tileentity);
        }
    }

    public void a(int i0, int i1, int i2, TileEntity tileentity) {
        ChunkPosition chunkposition = new ChunkPosition(i0, i1, i2);

        tileentity.a(this.e);
        tileentity.c = this.g * 16 + i0;
        tileentity.d = i1;
        tileentity.e = this.h * 16 + i2;
        if (this.a(i0, i1, i2) instanceof ITileEntityProvider) {
            if (this.i.containsKey(chunkposition)) {
                ((TileEntity) this.i.get(chunkposition)).s();
            }

            tileentity.t();
            this.i.put(chunkposition, tileentity);
        }
    }

    public void f(int i0, int i1, int i2) {
        ChunkPosition chunkposition = new ChunkPosition(i0, i1, i2);

        if (this.d) {
            TileEntity tileentity = (TileEntity) this.i.remove(chunkposition);

            if (tileentity != null) {
                tileentity.s();
            }
        }
    }

    public void c() {
        this.d = true;
        this.e.a(this.i.values());

        for (List aJ : this.j) {
            for (Object anAJ : aJ) {
                Entity entity = (Entity) anAJ;
                entity.X();
            }

            this.e.a(aJ);
        }
    }

    public void d() {
        this.d = false;

        for (Object o1 : this.i.values()) {
            TileEntity tileentity = (TileEntity) o1;

            this.e.a(tileentity);
        }

        for (List aJ : this.j) {
            this.e.b(aJ);
        }
    }

    public void e() {
        this.n = true;
    }

    public void a(Entity entity, AxisAlignedBB axisalignedbb, List list, IEntitySelector ientityselector) {
        int i0 = MathHelper.c((axisalignedbb.b - 2.0D) / 16.0D);
        int i1 = MathHelper.c((axisalignedbb.e + 2.0D) / 16.0D);

        i0 = MathHelper.a(i0, 0, this.j.length - 1);
        i1 = MathHelper.a(i1, 0, this.j.length - 1);

        for (int i2 = i0; i2 <= i1; ++i2) {
            List list1 = this.j[i2];

            for (int i3 = 0; i3 < list1.size(); ++i3) {
                Entity entity1 = (Entity) list1.get(i3);

                if (entity1 != entity && entity1.D.b(axisalignedbb) && (ientityselector == null || ientityselector.a(entity1))) {
                    list.add(entity1);
                    Entity[] aentity = entity1.at();

                    if (aentity != null) {
                        for (int i4 = 0; i4 < aentity.length; ++i4) {
                            entity1 = aentity[i4];
                            if (entity1 != entity && entity1.D.b(axisalignedbb) && (ientityselector == null || ientityselector.a(entity1))) {
                                list.add(entity1);
                            }
                        }
                    }
                }
            }
        }
    }

    public void a(Class oclass0, AxisAlignedBB axisalignedbb, List list, IEntitySelector ientityselector) {
        int i0 = MathHelper.c((axisalignedbb.b - 2.0D) / 16.0D);
        int i1 = MathHelper.c((axisalignedbb.e + 2.0D) / 16.0D);

        i0 = MathHelper.a(i0, 0, this.j.length - 1);
        i1 = MathHelper.a(i1, 0, this.j.length - 1);

        for (int i2 = i0; i2 <= i1; ++i2) {
            List list1 = this.j[i2];

            for (int i3 = 0; i3 < list1.size(); ++i3) {
                Entity entity = (Entity) list1.get(i3);

                if (oclass0.isAssignableFrom(entity.getClass()) && entity.D.b(axisalignedbb) && (ientityselector == null || ientityselector.a(entity))) {
                    list.add(entity);
                }
            }
        }
    }

    public boolean a(boolean flag0) {
        if (flag0) {
            if (this.o && this.e.H() != this.p || this.n) {
                return true;
            }
        }
        else if (this.o && this.e.H() >= this.p + 600L) {
            return true;
        }

        return this.n;
    }

    public Random a(long i0) {
        return new Random(this.e.G() + (long) (this.g * this.g * 4987142) + (long) (this.g * 5947611) + (long) (this.h * this.h) * 4392871L + (long) (this.h * 389711) ^ i0);
    }

    public boolean g() {
        return false;
    }

    public void a(IChunkProvider ichunkprovider, IChunkProvider ichunkprovider1, int i0, int i1) {
        if (!this.k && ichunkprovider.a(i0 + 1, i1 + 1) && ichunkprovider.a(i0, i1 + 1) && ichunkprovider.a(i0 + 1, i1)) {
            ichunkprovider.a(ichunkprovider1, i0, i1);
        }

        if (ichunkprovider.a(i0 - 1, i1) && !ichunkprovider.d(i0 - 1, i1).k && ichunkprovider.a(i0 - 1, i1 + 1) && ichunkprovider.a(i0, i1 + 1) && ichunkprovider.a(i0 - 1, i1 + 1)) {
            ichunkprovider.a(ichunkprovider1, i0 - 1, i1);
        }

        if (ichunkprovider.a(i0, i1 - 1) && !ichunkprovider.d(i0, i1 - 1).k && ichunkprovider.a(i0 + 1, i1 - 1) && ichunkprovider.a(i0 + 1, i1 - 1) && ichunkprovider.a(i0 + 1, i1)) {
            ichunkprovider.a(ichunkprovider1, i0, i1 - 1);
        }

        if (ichunkprovider.a(i0 - 1, i1 - 1) && !ichunkprovider.d(i0 - 1, i1 - 1).k && ichunkprovider.a(i0, i1 - 1) && ichunkprovider.a(i0 - 1, i1)) {
            ichunkprovider.a(ichunkprovider1, i0 - 1, i1 - 1);
        }
    }

    public int d(int i0, int i1) {
        int i2 = i0 | i1 << 4;
        int i3 = this.b[i2];

        if (i3 == -999) {
            int i4 = this.h() + 15;

            i3 = -1;

            while (i4 > 0 && i3 == -1) {
                Block block = this.a(i0, i4, i1);
                Material material = block.o();

                if (!material.c() && !material.d()) {
                    --i4;
                }
                else {
                    i3 = i4 + 1;
                }
            }

            this.b[i2] = i3;
        }

        return i3;
    }

    public void b(boolean flag0) {
        if (this.w && !this.e.t.g && !flag0) {
            this.c(this.e.E);
        }

        this.m = true;
        if (!this.l && this.k) {
            this.p();
        }

    }

    public boolean k() {
        return this.m && this.k && this.l;
    }

    public ChunkCoordIntPair l() {
        return new ChunkCoordIntPair(this.g, this.h);
    }

    public boolean c(int i0, int i1) {
        if (i0 < 0) {
            i0 = 0;
        }

        if (i1 >= 256) {
            i1 = 255;
        }

        for (int i2 = i0; i2 <= i1; i2 += 16) {
            ExtendedBlockStorage extendedblockstorage = this.u[i2 >> 4];

            if (extendedblockstorage != null && !extendedblockstorage.a()) {
                return false;
            }
        }

        return true;
    }

    public void a(ExtendedBlockStorage[] aextendedblockstorage) {
        this.u = aextendedblockstorage;
    }

    public BiomeGenBase a(int i0, int i1, WorldChunkManager worldchunkmanager) {
        int i2 = this.v[i1 << 4 | i0] & 255;

        if (i2 == 255) {
            BiomeGenBase biomegenbase = worldchunkmanager.a((this.g << 4) + i0, (this.h << 4) + i1);

            i2 = biomegenbase.ay;
            this.v[i1 << 4 | i0] = (byte) (i2 & 255);
        }

        return BiomeGenBase.d(i2) == null ? BiomeGenBase.p : BiomeGenBase.d(i2);
    }

    public byte[] m() {
        return this.v;
    }

    public void a(byte[] abyte) {
        this.v = abyte;
    }

    public void n() {
        this.x = 0;
    }

    public void o() {
        for (int i0 = 0; i0 < 8; ++i0) {
            if (this.x >= 4096) {
                return;
            }

            int i1 = this.x % 16;
            int i2 = this.x / 16 % 16;
            int i3 = this.x / 256;

            ++this.x;
            int i4 = (this.g << 4) + i2;
            int i5 = (this.h << 4) + i3;

            for (int i6 = 0; i6 < 16; ++i6) {
                int i7 = (i1 << 4) + i6;

                if (this.u[i1] == null && (i6 == 0 || i6 == 15 || i2 == 0 || i2 == 15 || i3 == 0 || i3 == 15) || this.u[i1] != null && this.u[i1].a(i2, i6, i3).o() == Material.a) {
                    if (this.e.a(i4, i7 - 1, i5).m() > 0) {
                        this.e.t(i4, i7 - 1, i5);
                    }

                    if (this.e.a(i4, i7 + 1, i5).m() > 0) {
                        this.e.t(i4, i7 + 1, i5);
                    }

                    if (this.e.a(i4 - 1, i7, i5).m() > 0) {
                        this.e.t(i4 - 1, i7, i5);
                    }

                    if (this.e.a(i4 + 1, i7, i5).m() > 0) {
                        this.e.t(i4 + 1, i7, i5);
                    }

                    if (this.e.a(i4, i7, i5 - 1).m() > 0) {
                        this.e.t(i4, i7, i5 - 1);
                    }

                    if (this.e.a(i4, i7, i5 + 1).m() > 0) {
                        this.e.t(i4, i7, i5 + 1);
                    }

                    this.e.t(i4, i7, i5);
                }
            }
        }
    }

    public void p() {
        this.k = true;
        this.l = true;
        if (!this.e.t.g) {
            if (this.e.b(this.g * 16 - 1, 0, this.h * 16 - 1, this.g * 16 + 1, 63, this.h * 16 + 1)) {
                for (int i0 = 0; i0 < 16; ++i0) {
                    for (int i1 = 0; i1 < 16; ++i1) {
                        if (!this.f(i0, i1)) {
                            this.l = false;
                            break;
                        }
                    }
                }

                if (this.l) {
                    Chunk chunk = this.e.d(this.g * 16 - 1, this.h * 16);

                    chunk.a(3);
                    chunk = this.e.d(this.g * 16 + 16, this.h * 16);
                    chunk.a(1);
                    chunk = this.e.d(this.g * 16, this.h * 16 - 1);
                    chunk.a(0);
                    chunk = this.e.d(this.g * 16, this.h * 16 + 16);
                    chunk.a(2);
                }
            }
            else {
                this.l = false;
            }
        }

    }

    private void a(int i0) {
        if (this.k) {
            int i1;

            if (i0 == 3) {
                for (i1 = 0; i1 < 16; ++i1) {
                    this.f(15, i1);
                }
            }
            else if (i0 == 1) {
                for (i1 = 0; i1 < 16; ++i1) {
                    this.f(0, i1);
                }
            }
            else if (i0 == 0) {
                for (i1 = 0; i1 < 16; ++i1) {
                    this.f(i1, 15);
                }
            }
            else if (i0 == 2) {
                for (i1 = 0; i1 < 16; ++i1) {
                    this.f(i1, 0);
                }
            }

        }
    }

    private boolean f(int i0, int i1) {
        int i2 = this.h();
        boolean flag0 = false;
        boolean flag1 = false;

        int i3;

        for (i3 = i2 + 16 - 1; i3 > 63 || i3 > 0 && !flag1; --i3) {
            int i4 = this.b(i0, i3, i1);

            if (i4 == 255 && i3 < 63) {
                flag1 = true;
            }

            if (!flag0 && i4 > 0) {
                flag0 = true;
            }
            else if (flag0 && i4 == 0 && !this.e.t(this.g * 16 + i0, i3, this.h * 16 + i1)) {
                return false;
            }
        }

        for (; i3 > 0; --i3) {
            if (this.a(i0, i3, i1).m() > 0) {
                this.e.t(this.g * 16 + i0, i3, this.h * 16 + i1);
            }
        }

        return true;
    }

    // CanaryMod start
    public CanaryChunk getCanaryChunk() {
        return canaryChunk;
    }
    // CanaryMod end
}

