// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst 

package net.minecraft.src;

import java.io.PrintStream;
import java.util.*;

// Referenced classes of package net.minecraft.src:
//            IBlockAccess, WorldProvider, Chunk, WorldChunkManager, 
//            VillageCollection, VillageSiege, MapStorage, ISaveHandler, 
//            WorldInfo, ChunkProvider, ChunkPosition, IChunkProvider, 
//            IProgressUpdate, Block, Profiler, Material, 
//            IWorldAccess, EnumSkyBlock, Vec3D, MathHelper, 
//            Entity, EntityPlayer, AxisAlignedBB, NextTickListEntry, 
//            TileEntity, BlockFire, BlockFluid, Explosion, 
//            SpawnerAnimals, ChunkCoordIntPair, EntityLightningBolt, ExtendedBlockStorage, 
//            BiomeGenBase, ChunkCache, PathFinder, PlayerCapabilities, 
//            ChunkCoordinates, WeightedRandom, SpawnListEntry, WorldSettings, 
//            MovingObjectPosition, PathEntity, WorldSavedData, EnumCreatureType

public class World
    implements IBlockAccess
{

    public boolean field_4280_a;
    public List field_815_a;
    private List field_790_z;
    private TreeSet field_789_A;
    private Set field_788_B;
    public List field_814_b;
    private List field_34901_P;
    private List field_34900_Q;
    public List field_805_k;
    public List field_27081_e;
    private long field_6159_E;
    public int field_811_e;
    protected int field_4279_g;
    protected final int field_4278_h = 0x3c6ef35f;
    protected float field_27079_B;
    protected float field_27078_C;
    protected float field_27077_D;
    protected float field_27076_E;
    protected int field_27075_F;
    public int field_27080_i;
    public boolean field_808_h;
    private long field_784_F;
    protected int field_4277_j;
    public int field_804_l;
    public Random field_803_m;
    public boolean field_9212_p;
    public final WorldProvider field_4272_q;
    protected List field_798_r;
    protected IChunkProvider field_783_G;
    protected final ISaveHandler field_9211_s;
    protected WorldInfo field_797_s;
    public boolean field_9209_x;
    private boolean field_22087_A;
    public MapStorage field_28105_z;
    public final VillageCollection field_48096_A = new VillageCollection(this);
    private final VillageSiege field_48097_O = new VillageSiege(this);
    private ArrayList field_9207_I;
    private boolean field_31048_L;
    protected boolean field_21121_K;
    protected boolean field_21120_L;
    protected Set field_4264_K;
    private int field_4263_L;
    int field_35245_H[];
    private List field_778_L;
    public boolean field_792_x;

    public BiomeGenBase func_48091_a(int p_48091_1_, int p_48091_2_)
    {
        if(func_530_e(p_48091_1_, 0, p_48091_2_))
        {
            Chunk chunk = func_492_a(p_48091_1_, p_48091_2_);
            if(chunk != null)
            {
                return chunk.func_48560_a(p_48091_1_ & 0xf, p_48091_2_ & 0xf, field_4272_q.field_4301_b);
            }
        }
        return field_4272_q.field_4301_b.func_4067_a(p_48091_1_, p_48091_2_);
    }

    public WorldChunkManager func_4077_a()
    {
        return field_4272_q.field_4301_b;
    }

    public World(ISaveHandler p_i131_1_, String p_i131_2_, WorldSettings p_i131_3_, WorldProvider p_i131_4_)
    {
        field_4280_a = false;
        field_815_a = new ArrayList();
        field_790_z = new ArrayList();
        field_789_A = new TreeSet();
        field_788_B = new HashSet();
        field_814_b = new ArrayList();
        field_34901_P = new ArrayList();
        field_34900_Q = new ArrayList();
        field_805_k = new ArrayList();
        field_27081_e = new ArrayList();
        field_6159_E = 0xffffffL;
        field_811_e = 0;
        field_4279_g = (new Random()).nextInt();
        field_27075_F = 0;
        field_27080_i = 0;
        field_808_h = false;
        field_784_F = System.currentTimeMillis();
        field_4277_j = 40;
        field_803_m = new Random();
        field_9212_p = false;
        field_798_r = new ArrayList();
        field_9207_I = new ArrayList();
        field_21121_K = true;
        field_21120_L = true;
        field_4264_K = new HashSet();
        field_4263_L = field_803_m.nextInt(12000);
        field_35245_H = new int[32768];
        field_778_L = new ArrayList();
        field_792_x = false;
        field_9211_s = p_i131_1_;
        field_28105_z = new MapStorage(p_i131_1_);
        field_797_s = p_i131_1_.func_22096_c();
        field_9212_p = field_797_s == null;
        if(p_i131_4_ != null)
        {
            field_4272_q = p_i131_4_;
        } else
        if(field_797_s != null && field_797_s.func_22178_h() != 0)
        {
            field_4272_q = WorldProvider.func_4091_a(field_797_s.func_22178_h());
        } else
        {
            field_4272_q = WorldProvider.func_4091_a(0);
        }
        boolean flag = false;
        if(field_797_s == null)
        {
            field_797_s = new WorldInfo(p_i131_3_, p_i131_2_);
            flag = true;
        } else
        {
            field_797_s.func_22190_a(p_i131_2_);
        }
        field_4272_q.func_4093_a(this);
        field_783_G = func_22086_b();
        if(flag)
        {
            func_25072_c();
        }
        func_465_d();
        func_27070_x();
    }

    protected IChunkProvider func_22086_b()
    {
        IChunkLoader ichunkloader = field_9211_s.func_22092_a(field_4272_q);
        return new ChunkProvider(this, ichunkloader, field_4272_q.func_4087_c());
    }

    protected void func_25072_c()
    {
        if(!field_4272_q.func_28108_d())
        {
            field_797_s.func_22181_a(0, field_4272_q.func_46119_e(), 0);
            return;
        }
        field_9209_x = true;
        WorldChunkManager worldchunkmanager = field_4272_q.field_4301_b;
        List list = worldchunkmanager.func_35137_a();
        Random random = new Random(func_22079_j());
        ChunkPosition chunkposition = worldchunkmanager.func_35139_a(0, 0, 256, list, random);
        int i = 0;
        int j = field_4272_q.func_46119_e();
        int k = 0;
        if(chunkposition != null)
        {
            i = chunkposition.field_846_a;
            k = chunkposition.field_847_c;
        } else
        {
            System.out.println("Unable to find spawn biome");
        }
        int l = 0;
        do
        {
            if(field_4272_q.func_4092_a(i, k))
            {
                break;
            }
            i += random.nextInt(64) - random.nextInt(64);
            k += random.nextInt(64) - random.nextInt(64);
        } while(++l != 1000);
        field_797_s.func_22181_a(i, j, k);
        field_9209_x = false;
    }

    public ChunkCoordinates func_40212_d()
    {
        return field_4272_q.func_40545_d();
    }

    public int func_528_f(int p_528_1_, int p_528_2_)
    {
        int i;
        for(i = 63; !func_20111_e(p_528_1_, i + 1, p_528_2_); i++) { }
        return func_444_a(p_528_1_, i, p_528_2_);
    }

    public void func_485_a(boolean p_485_1_, IProgressUpdate p_485_2_)
    {
        if(!field_783_G.func_364_b())
        {
            return;
        }
        if(p_485_2_ != null)
        {
            p_485_2_.func_438_a("Saving level");
        }
        func_478_h();
        if(p_485_2_ != null)
        {
            p_485_2_.func_439_b("Saving chunks");
        }
        field_783_G.func_360_a(p_485_1_, p_485_2_);
    }

    private void func_478_h()
    {
        func_476_g();
        field_9211_s.func_22095_a(field_797_s, field_805_k);
        field_28105_z.func_28176_a();
    }

    public int func_444_a(int p_444_1_, int p_444_2_, int p_444_3_)
    {
        if(p_444_1_ < 0xfe363c80 || p_444_3_ < 0xfe363c80 || p_444_1_ >= 0x1c9c380 || p_444_3_ >= 0x1c9c380)
        {
            return 0;
        }
        if(p_444_2_ < 0)
        {
            return 0;
        }
        if(p_444_2_ >= 256)
        {
            return 0;
        } else
        {
            return func_525_b(p_444_1_ >> 4, p_444_3_ >> 4).func_344_a(p_444_1_ & 0xf, p_444_2_, p_444_3_ & 0xf);
        }
    }

    public int func_48092_f(int p_48092_1_, int p_48092_2_, int p_48092_3_)
    {
        if(p_48092_1_ < 0xfe363c80 || p_48092_3_ < 0xfe363c80 || p_48092_1_ >= 0x1c9c380 || p_48092_3_ >= 0x1c9c380)
        {
            return 0;
        }
        if(p_48092_2_ < 0)
        {
            return 0;
        }
        if(p_48092_2_ >= 256)
        {
            return 0;
        } else
        {
            return func_525_b(p_48092_1_ >> 4, p_48092_3_ >> 4).func_48555_b(p_48092_1_ & 0xf, p_48092_2_, p_48092_3_ & 0xf);
        }
    }

    public boolean func_20111_e(int p_20111_1_, int p_20111_2_, int p_20111_3_)
    {
        return func_444_a(p_20111_1_, p_20111_2_, p_20111_3_) == 0;
    }

    public boolean func_48084_h(int p_48084_1_, int p_48084_2_, int p_48084_3_)
    {
        int i = func_444_a(p_48084_1_, p_48084_2_, p_48084_3_);
        return Block.field_542_n[i] != null && Block.field_542_n[i].func_48124_n();
    }

    public boolean func_530_e(int p_530_1_, int p_530_2_, int p_530_3_)
    {
        if(p_530_2_ < 0 || p_530_2_ >= 256)
        {
            return false;
        } else
        {
            return func_447_g(p_530_1_ >> 4, p_530_3_ >> 4);
        }
    }

    public boolean func_21118_a(int p_21118_1_, int p_21118_2_, int p_21118_3_, int p_21118_4_)
    {
        return func_466_a(p_21118_1_ - p_21118_4_, p_21118_2_ - p_21118_4_, p_21118_3_ - p_21118_4_, p_21118_1_ + p_21118_4_, p_21118_2_ + p_21118_4_, p_21118_3_ + p_21118_4_);
    }

    public boolean func_466_a(int p_466_1_, int p_466_2_, int p_466_3_, int p_466_4_, int p_466_5_, int p_466_6_)
    {
        if(p_466_5_ < 0 || p_466_2_ >= 256)
        {
            return false;
        }
        p_466_1_ >>= 4;
        p_466_3_ >>= 4;
        p_466_4_ >>= 4;
        p_466_6_ >>= 4;
        for(int i = p_466_1_; i <= p_466_4_; i++)
        {
            for(int j = p_466_3_; j <= p_466_6_; j++)
            {
                if(!func_447_g(i, j))
                {
                    return false;
                }
            }

        }

        return true;
    }

    private boolean func_447_g(int p_447_1_, int p_447_2_)
    {
        return field_783_G.func_365_a(p_447_1_, p_447_2_);
    }

    public Chunk func_492_a(int p_492_1_, int p_492_2_)
    {
        return func_525_b(p_492_1_ >> 4, p_492_2_ >> 4);
    }

    public Chunk func_525_b(int p_525_1_, int p_525_2_)
    {
        return field_783_G.func_363_b(p_525_1_, p_525_2_);
    }

    public boolean func_470_a(int p_470_1_, int p_470_2_, int p_470_3_, int p_470_4_, int p_470_5_)
    {
        if(p_470_1_ < 0xfe363c80 || p_470_3_ < 0xfe363c80 || p_470_1_ >= 0x1c9c380 || p_470_3_ >= 0x1c9c380)
        {
            return false;
        }
        if(p_470_2_ < 0)
        {
            return false;
        }
        if(p_470_2_ >= 256)
        {
            return false;
        } else
        {
            Chunk chunk = func_525_b(p_470_1_ >> 4, p_470_3_ >> 4);
            boolean flag = chunk.func_346_a(p_470_1_ & 0xf, p_470_2_, p_470_3_ & 0xf, p_470_4_, p_470_5_);
            Profiler.func_40518_a("checkLight");
            func_35242_p(p_470_1_, p_470_2_, p_470_3_);
            Profiler.func_40517_a();
            return flag;
        }
    }

    public boolean func_462_a(int p_462_1_, int p_462_2_, int p_462_3_, int p_462_4_)
    {
        if(p_462_1_ < 0xfe363c80 || p_462_3_ < 0xfe363c80 || p_462_1_ >= 0x1c9c380 || p_462_3_ >= 0x1c9c380)
        {
            return false;
        }
        if(p_462_2_ < 0)
        {
            return false;
        }
        if(p_462_2_ >= 256)
        {
            return false;
        } else
        {
            Chunk chunk = func_525_b(p_462_1_ >> 4, p_462_3_ >> 4);
            boolean flag = chunk.func_357_a(p_462_1_ & 0xf, p_462_2_, p_462_3_ & 0xf, p_462_4_);
            Profiler.func_40518_a("checkLight");
            func_35242_p(p_462_1_, p_462_2_, p_462_3_);
            Profiler.func_40517_a();
            return flag;
        }
    }

    public Material func_443_c(int p_443_1_, int p_443_2_, int p_443_3_)
    {
        int i = func_444_a(p_443_1_, p_443_2_, p_443_3_);
        if(i == 0)
        {
            return Material.field_526_a;
        } else
        {
            return Block.field_542_n[i].field_553_bn;
        }
    }

    public int func_446_b(int p_446_1_, int p_446_2_, int p_446_3_)
    {
        if(p_446_1_ < 0xfe363c80 || p_446_3_ < 0xfe363c80 || p_446_1_ >= 0x1c9c380 || p_446_3_ >= 0x1c9c380)
        {
            return 0;
        }
        if(p_446_2_ < 0)
        {
            return 0;
        }
        if(p_446_2_ >= 256)
        {
            return 0;
        } else
        {
            Chunk chunk = func_525_b(p_446_1_ >> 4, p_446_3_ >> 4);
            p_446_1_ &= 0xf;
            p_446_3_ &= 0xf;
            return chunk.func_356_b(p_446_1_, p_446_2_, p_446_3_);
        }
    }

    public void func_511_b(int p_511_1_, int p_511_2_, int p_511_3_, int p_511_4_)
    {
        if(func_463_c(p_511_1_, p_511_2_, p_511_3_, p_511_4_))
        {
            int i = func_444_a(p_511_1_, p_511_2_, p_511_3_);
            if(Block.field_28029_t[i & 0xfff])
            {
                func_454_e(p_511_1_, p_511_2_, p_511_3_, i);
            } else
            {
                func_449_g(p_511_1_, p_511_2_, p_511_3_, i);
            }
        }
    }

    public boolean func_463_c(int p_463_1_, int p_463_2_, int p_463_3_, int p_463_4_)
    {
        if(p_463_1_ < 0xfe363c80 || p_463_3_ < 0xfe363c80 || p_463_1_ >= 0x1c9c380 || p_463_3_ >= 0x1c9c380)
        {
            return false;
        }
        if(p_463_2_ < 0)
        {
            return false;
        }
        if(p_463_2_ >= 256)
        {
            return false;
        } else
        {
            Chunk chunk = func_525_b(p_463_1_ >> 4, p_463_3_ >> 4);
            p_463_1_ &= 0xf;
            p_463_3_ &= 0xf;
            return chunk.func_345_b(p_463_1_, p_463_2_, p_463_3_, p_463_4_);
        }
    }

    public boolean func_508_d(int p_508_1_, int p_508_2_, int p_508_3_, int p_508_4_)
    {
        if(func_462_a(p_508_1_, p_508_2_, p_508_3_, p_508_4_))
        {
            func_454_e(p_508_1_, p_508_2_, p_508_3_, p_508_4_);
            return true;
        } else
        {
            return false;
        }
    }

    public boolean func_507_b(int p_507_1_, int p_507_2_, int p_507_3_, int p_507_4_, int p_507_5_)
    {
        if(func_470_a(p_507_1_, p_507_2_, p_507_3_, p_507_4_, p_507_5_))
        {
            func_454_e(p_507_1_, p_507_2_, p_507_3_, p_507_4_);
            return true;
        } else
        {
            return false;
        }
    }

    public void func_521_f(int p_521_1_, int p_521_2_, int p_521_3_)
    {
        for(int i = 0; i < field_798_r.size(); i++)
        {
            ((IWorldAccess)field_798_r.get(i)).func_683_a(p_521_1_, p_521_2_, p_521_3_);
        }

    }

    public void func_454_e(int p_454_1_, int p_454_2_, int p_454_3_, int p_454_4_)
    {
        func_521_f(p_454_1_, p_454_2_, p_454_3_);
        func_449_g(p_454_1_, p_454_2_, p_454_3_, p_454_4_);
    }

    public void func_498_f(int p_498_1_, int p_498_2_, int p_498_3_, int p_498_4_)
    {
        if(p_498_3_ > p_498_4_)
        {
            int i = p_498_4_;
            p_498_4_ = p_498_3_;
            p_498_3_ = i;
        }
        if(!field_4272_q.field_4306_c)
        {
            for(int j = p_498_3_; j <= p_498_4_; j++)
            {
                func_35243_b(EnumSkyBlock.Sky, p_498_1_, j, p_498_2_);
            }

        }
        func_519_b(p_498_1_, p_498_3_, p_498_2_, p_498_1_, p_498_4_, p_498_2_);
    }

    public void func_21119_h(int p_21119_1_, int p_21119_2_, int p_21119_3_)
    {
        for(int i = 0; i < field_798_r.size(); i++)
        {
            ((IWorldAccess)field_798_r.get(i)).func_685_a(p_21119_1_, p_21119_2_, p_21119_3_, p_21119_1_, p_21119_2_, p_21119_3_);
        }

    }

    public void func_519_b(int p_519_1_, int p_519_2_, int p_519_3_, int p_519_4_, int p_519_5_, int p_519_6_)
    {
        for(int i = 0; i < field_798_r.size(); i++)
        {
            ((IWorldAccess)field_798_r.get(i)).func_685_a(p_519_1_, p_519_2_, p_519_3_, p_519_4_, p_519_5_, p_519_6_);
        }

    }

    public void func_449_g(int p_449_1_, int p_449_2_, int p_449_3_, int p_449_4_)
    {
        func_527_k(p_449_1_ - 1, p_449_2_, p_449_3_, p_449_4_);
        func_527_k(p_449_1_ + 1, p_449_2_, p_449_3_, p_449_4_);
        func_527_k(p_449_1_, p_449_2_ - 1, p_449_3_, p_449_4_);
        func_527_k(p_449_1_, p_449_2_ + 1, p_449_3_, p_449_4_);
        func_527_k(p_449_1_, p_449_2_, p_449_3_ - 1, p_449_4_);
        func_527_k(p_449_1_, p_449_2_, p_449_3_ + 1, p_449_4_);
    }

    private void func_527_k(int p_527_1_, int p_527_2_, int p_527_3_, int p_527_4_)
    {
        if(field_808_h || field_792_x)
        {
            return;
        }
        Block block = Block.field_542_n[func_444_a(p_527_1_, p_527_2_, p_527_3_)];
        if(block != null)
        {
            block.func_234_b(this, p_527_1_, p_527_2_, p_527_3_, p_527_4_);
        }
    }

    public boolean func_497_g(int p_497_1_, int p_497_2_, int p_497_3_)
    {
        return func_525_b(p_497_1_ >> 4, p_497_3_ >> 4).func_343_c(p_497_1_ & 0xf, p_497_2_, p_497_3_ & 0xf);
    }

    public int func_28098_j(int p_28098_1_, int p_28098_2_, int p_28098_3_)
    {
        if(p_28098_2_ < 0)
        {
            return 0;
        }
        if(p_28098_2_ >= 256)
        {
            p_28098_2_ = 255;
        }
        return func_525_b(p_28098_1_ >> 4, p_28098_3_ >> 4).func_354_c(p_28098_1_ & 0xf, p_28098_2_, p_28098_3_ & 0xf, 0);
    }

    public int func_495_h(int p_495_1_, int p_495_2_, int p_495_3_)
    {
        return func_517_a(p_495_1_, p_495_2_, p_495_3_, true);
    }

    public int func_517_a(int p_517_1_, int p_517_2_, int p_517_3_, boolean p_517_4_)
    {
        if(p_517_1_ < 0xfe363c80 || p_517_3_ < 0xfe363c80 || p_517_1_ >= 0x1c9c380 || p_517_3_ >= 0x1c9c380)
        {
            return 15;
        }
        if(p_517_4_)
        {
            int i = func_444_a(p_517_1_, p_517_2_, p_517_3_);
            if(i == Block.field_607_al.field_573_bc || i == Block.field_643_aB.field_573_bc || i == Block.field_9055_aH.field_573_bc || i == Block.field_9045_at.field_573_bc)
            {
                int j = func_517_a(p_517_1_, p_517_2_ + 1, p_517_3_, false);
                int k = func_517_a(p_517_1_ + 1, p_517_2_, p_517_3_, false);
                int l = func_517_a(p_517_1_ - 1, p_517_2_, p_517_3_, false);
                int i1 = func_517_a(p_517_1_, p_517_2_, p_517_3_ + 1, false);
                int j1 = func_517_a(p_517_1_, p_517_2_, p_517_3_ - 1, false);
                if(k > j)
                {
                    j = k;
                }
                if(l > j)
                {
                    j = l;
                }
                if(i1 > j)
                {
                    j = i1;
                }
                if(j1 > j)
                {
                    j = j1;
                }
                return j;
            }
        }
        if(p_517_2_ < 0)
        {
            return 0;
        }
        if(p_517_2_ >= 256)
        {
            p_517_2_ = 255;
        }
        Chunk chunk = func_525_b(p_517_1_ >> 4, p_517_3_ >> 4);
        p_517_1_ &= 0xf;
        p_517_3_ &= 0xf;
        return chunk.func_354_c(p_517_1_, p_517_2_, p_517_3_, field_811_e);
    }

    public int func_488_c(int p_488_1_, int p_488_2_)
    {
        if(p_488_1_ < 0xfe363c80 || p_488_2_ < 0xfe363c80 || p_488_1_ >= 0x1c9c380 || p_488_2_ >= 0x1c9c380)
        {
            return 0;
        }
        if(!func_447_g(p_488_1_ >> 4, p_488_2_ >> 4))
        {
            return 0;
        } else
        {
            Chunk chunk = func_525_b(p_488_1_ >> 4, p_488_2_ >> 4);
            return chunk.func_337_b(p_488_1_ & 0xf, p_488_2_ & 0xf);
        }
    }

    public int func_512_a(EnumSkyBlock p_512_1_, int p_512_2_, int p_512_3_, int p_512_4_)
    {
        if(p_512_3_ < 0)
        {
            p_512_3_ = 0;
        }
        if(p_512_3_ >= 256)
        {
            p_512_3_ = 255;
        }
        if(p_512_2_ < 0xfe363c80 || p_512_4_ < 0xfe363c80 || p_512_2_ >= 0x1c9c380 || p_512_4_ >= 0x1c9c380)
        {
            return p_512_1_.field_984_c;
        }
        int i = p_512_2_ >> 4;
        int j = p_512_4_ >> 4;
        if(!func_447_g(i, j))
        {
            return p_512_1_.field_984_c;
        } else
        {
            Chunk chunk = func_525_b(i, j);
            return chunk.func_341_a(p_512_1_, p_512_2_ & 0xf, p_512_3_, p_512_4_ & 0xf);
        }
    }

    public void func_529_b(EnumSkyBlock p_529_1_, int p_529_2_, int p_529_3_, int p_529_4_, int p_529_5_)
    {
        if(p_529_2_ < 0xfe363c80 || p_529_4_ < 0xfe363c80 || p_529_2_ >= 0x1c9c380 || p_529_4_ >= 0x1c9c380)
        {
            return;
        }
        if(p_529_3_ < 0)
        {
            return;
        }
        if(p_529_3_ >= 256)
        {
            return;
        }
        if(!func_447_g(p_529_2_ >> 4, p_529_4_ >> 4))
        {
            return;
        }
        Chunk chunk = func_525_b(p_529_2_ >> 4, p_529_4_ >> 4);
        chunk.func_330_a(p_529_1_, p_529_2_ & 0xf, p_529_3_, p_529_4_ & 0xf, p_529_5_);
        for(int i = 0; i < field_798_r.size(); i++)
        {
            ((IWorldAccess)field_798_r.get(i)).func_48414_b(p_529_2_, p_529_3_, p_529_4_);
        }

    }

    public void func_48086_o(int p_48086_1_, int p_48086_2_, int p_48086_3_)
    {
        for(int i = 0; i < field_798_r.size(); i++)
        {
            ((IWorldAccess)field_798_r.get(i)).func_48414_b(p_48086_1_, p_48086_2_, p_48086_3_);
        }

    }

    public float func_455_j(int p_455_1_, int p_455_2_, int p_455_3_)
    {
        return field_4272_q.field_4305_d[func_495_h(p_455_1_, p_455_2_, p_455_3_)];
    }

    public boolean func_453_a()
    {
        return field_811_e < 4;
    }

    public MovingObjectPosition func_486_a(Vec3D p_486_1_, Vec3D p_486_2_)
    {
        return func_28099_a(p_486_1_, p_486_2_, false, false);
    }

    public MovingObjectPosition func_505_a(Vec3D p_505_1_, Vec3D p_505_2_, boolean p_505_3_)
    {
        return func_28099_a(p_505_1_, p_505_2_, p_505_3_, false);
    }

    public MovingObjectPosition func_28099_a(Vec3D p_28099_1_, Vec3D p_28099_2_, boolean p_28099_3_, boolean p_28099_4_)
    {
        if(Double.isNaN(p_28099_1_.field_1055_a) || Double.isNaN(p_28099_1_.field_1054_b) || Double.isNaN(p_28099_1_.field_1058_c))
        {
            return null;
        }
        if(Double.isNaN(p_28099_2_.field_1055_a) || Double.isNaN(p_28099_2_.field_1054_b) || Double.isNaN(p_28099_2_.field_1058_c))
        {
            return null;
        }
        int i = MathHelper.func_584_b(p_28099_2_.field_1055_a);
        int j = MathHelper.func_584_b(p_28099_2_.field_1054_b);
        int k = MathHelper.func_584_b(p_28099_2_.field_1058_c);
        int l = MathHelper.func_584_b(p_28099_1_.field_1055_a);
        int i1 = MathHelper.func_584_b(p_28099_1_.field_1054_b);
        int j1 = MathHelper.func_584_b(p_28099_1_.field_1058_c);
        int k1 = func_444_a(l, i1, j1);
        int i2 = func_446_b(l, i1, j1);
        Block block = Block.field_542_n[k1];
        if((!p_28099_4_ || block == null || block.func_248_d(this, l, i1, j1) != null) && k1 > 0 && block.func_243_a(i2, p_28099_3_))
        {
            MovingObjectPosition movingobjectposition = block.func_262_a(this, l, i1, j1, p_28099_1_, p_28099_2_);
            if(movingobjectposition != null)
            {
                return movingobjectposition;
            }
        }
        for(int l1 = 200; l1-- >= 0;)
        {
            if(Double.isNaN(p_28099_1_.field_1055_a) || Double.isNaN(p_28099_1_.field_1054_b) || Double.isNaN(p_28099_1_.field_1058_c))
            {
                return null;
            }
            if(l == i && i1 == j && j1 == k)
            {
                return null;
            }
            boolean flag = true;
            boolean flag1 = true;
            boolean flag2 = true;
            double d = 999D;
            double d1 = 999D;
            double d2 = 999D;
            if(i > l)
            {
                d = (double)l + 1.0D;
            } else
            if(i < l)
            {
                d = (double)l + 0.0D;
            } else
            {
                flag = false;
            }
            if(j > i1)
            {
                d1 = (double)i1 + 1.0D;
            } else
            if(j < i1)
            {
                d1 = (double)i1 + 0.0D;
            } else
            {
                flag1 = false;
            }
            if(k > j1)
            {
                d2 = (double)j1 + 1.0D;
            } else
            if(k < j1)
            {
                d2 = (double)j1 + 0.0D;
            } else
            {
                flag2 = false;
            }
            double d3 = 999D;
            double d4 = 999D;
            double d5 = 999D;
            double d6 = p_28099_2_.field_1055_a - p_28099_1_.field_1055_a;
            double d7 = p_28099_2_.field_1054_b - p_28099_1_.field_1054_b;
            double d8 = p_28099_2_.field_1058_c - p_28099_1_.field_1058_c;
            if(flag)
            {
                d3 = (d - p_28099_1_.field_1055_a) / d6;
            }
            if(flag1)
            {
                d4 = (d1 - p_28099_1_.field_1054_b) / d7;
            }
            if(flag2)
            {
                d5 = (d2 - p_28099_1_.field_1058_c) / d8;
            }
            byte byte0 = 0;
            if(d3 < d4 && d3 < d5)
            {
                if(i > l)
                {
                    byte0 = 4;
                } else
                {
                    byte0 = 5;
                }
                p_28099_1_.field_1055_a = d;
                p_28099_1_.field_1054_b += d7 * d3;
                p_28099_1_.field_1058_c += d8 * d3;
            } else
            if(d4 < d5)
            {
                if(j > i1)
                {
                    byte0 = 0;
                } else
                {
                    byte0 = 1;
                }
                p_28099_1_.field_1055_a += d6 * d4;
                p_28099_1_.field_1054_b = d1;
                p_28099_1_.field_1058_c += d8 * d4;
            } else
            {
                if(k > j1)
                {
                    byte0 = 2;
                } else
                {
                    byte0 = 3;
                }
                p_28099_1_.field_1055_a += d6 * d5;
                p_28099_1_.field_1054_b += d7 * d5;
                p_28099_1_.field_1058_c = d2;
            }
            Vec3D vec3d = Vec3D.func_768_b(p_28099_1_.field_1055_a, p_28099_1_.field_1054_b, p_28099_1_.field_1058_c);
            l = (int)(vec3d.field_1055_a = MathHelper.func_584_b(p_28099_1_.field_1055_a));
            if(byte0 == 5)
            {
                l--;
                vec3d.field_1055_a++;
            }
            i1 = (int)(vec3d.field_1054_b = MathHelper.func_584_b(p_28099_1_.field_1054_b));
            if(byte0 == 1)
            {
                i1--;
                vec3d.field_1054_b++;
            }
            j1 = (int)(vec3d.field_1058_c = MathHelper.func_584_b(p_28099_1_.field_1058_c));
            if(byte0 == 3)
            {
                j1--;
                vec3d.field_1058_c++;
            }
            int j2 = func_444_a(l, i1, j1);
            int k2 = func_446_b(l, i1, j1);
            Block block1 = Block.field_542_n[j2];
            if((!p_28099_4_ || block1 == null || block1.func_248_d(this, l, i1, j1) != null) && j2 > 0 && block1.func_243_a(k2, p_28099_3_))
            {
                MovingObjectPosition movingobjectposition1 = block1.func_262_a(this, l, i1, j1, p_28099_1_, p_28099_2_);
                if(movingobjectposition1 != null)
                {
                    return movingobjectposition1;
                }
            }
        }

        return null;
    }

    public void func_506_a(Entity p_506_1_, String p_506_2_, float p_506_3_, float p_506_4_)
    {
        for(int i = 0; i < field_798_r.size(); i++)
        {
            ((IWorldAccess)field_798_r.get(i)).func_689_a(p_506_2_, p_506_1_.field_322_l, p_506_1_.field_321_m - (double)p_506_1_.field_9076_H, p_506_1_.field_320_n, p_506_3_, p_506_4_);
        }

    }

    public void func_502_a(double p_502_1_, double p_502_3_, double p_502_5_, String p_502_7_, 
            float p_502_8_, float p_502_9_)
    {
        for(int i = 0; i < field_798_r.size(); i++)
        {
            ((IWorldAccess)field_798_r.get(i)).func_689_a(p_502_7_, p_502_1_, p_502_3_, p_502_5_, p_502_8_, p_502_9_);
        }

    }

    public void func_491_a(String p_491_1_, int p_491_2_, int p_491_3_, int p_491_4_)
    {
        for(int i = 0; i < field_798_r.size(); i++)
        {
            ((IWorldAccess)field_798_r.get(i)).func_682_a(p_491_1_, p_491_2_, p_491_3_, p_491_4_);
        }

    }

    public void func_514_a(String p_514_1_, double p_514_2_, double p_514_4_, double p_514_6_, 
            double p_514_8_, double p_514_10_, double p_514_12_)
    {
        for(int i = 0; i < field_798_r.size(); i++)
        {
            ((IWorldAccess)field_798_r.get(i)).func_687_a(p_514_1_, p_514_2_, p_514_4_, p_514_6_, p_514_8_, p_514_10_, p_514_12_);
        }

    }

    public boolean func_27073_a(Entity p_27073_1_)
    {
        field_27081_e.add(p_27073_1_);
        return true;
    }

    public boolean func_526_a(Entity p_526_1_)
    {
        int i = MathHelper.func_584_b(p_526_1_.field_322_l / 16D);
        int j = MathHelper.func_584_b(p_526_1_.field_320_n / 16D);
        boolean flag = false;
        if(p_526_1_ instanceof EntityPlayer)
        {
            flag = true;
        }
        if(flag || func_447_g(i, j))
        {
            if(p_526_1_ instanceof EntityPlayer)
            {
                EntityPlayer entityplayer = (EntityPlayer)p_526_1_;
                field_805_k.add(entityplayer);
                func_22082_o();
            }
            func_525_b(i, j).func_335_a(p_526_1_);
            field_815_a.add(p_526_1_);
            func_479_b(p_526_1_);
            return true;
        } else
        {
            return false;
        }
    }

    protected void func_479_b(Entity p_479_1_)
    {
        for(int i = 0; i < field_798_r.size(); i++)
        {
            ((IWorldAccess)field_798_r.get(i)).func_681_a(p_479_1_);
        }

    }

    protected void func_531_c(Entity p_531_1_)
    {
        for(int i = 0; i < field_798_r.size(); i++)
        {
            ((IWorldAccess)field_798_r.get(i)).func_688_b(p_531_1_);
        }

    }

    public void func_22085_d(Entity p_22085_1_)
    {
        if(p_22085_1_.field_328_f != null)
        {
            p_22085_1_.field_328_f.func_6094_e(null);
        }
        if(p_22085_1_.field_327_g != null)
        {
            p_22085_1_.func_6094_e(null);
        }
        p_22085_1_.func_118_j();
        if(p_22085_1_ instanceof EntityPlayer)
        {
            field_805_k.remove((EntityPlayer)p_22085_1_);
            func_22082_o();
        }
    }

    public void func_22073_e(Entity p_22073_1_)
    {
        p_22073_1_.func_118_j();
        if(p_22073_1_ instanceof EntityPlayer)
        {
            field_805_k.remove((EntityPlayer)p_22073_1_);
            func_22082_o();
        }
        int i = p_22073_1_.field_307_aa;
        int j = p_22073_1_.field_303_ac;
        if(p_22073_1_.field_276_Z && func_447_g(i, j))
        {
            func_525_b(i, j).func_350_b(p_22073_1_);
        }
        field_815_a.remove(p_22073_1_);
        func_531_c(p_22073_1_);
    }

    public void func_4072_a(IWorldAccess p_4072_1_)
    {
        field_798_r.add(p_4072_1_);
    }

    public List func_481_a(Entity p_481_1_, AxisAlignedBB p_481_2_)
    {
        field_9207_I.clear();
        int i = MathHelper.func_584_b(p_481_2_.field_964_a);
        int j = MathHelper.func_584_b(p_481_2_.field_969_d + 1.0D);
        int k = MathHelper.func_584_b(p_481_2_.field_963_b);
        int l = MathHelper.func_584_b(p_481_2_.field_968_e + 1.0D);
        int i1 = MathHelper.func_584_b(p_481_2_.field_970_c);
        int j1 = MathHelper.func_584_b(p_481_2_.field_967_f + 1.0D);
        for(int k1 = i; k1 < j; k1++)
        {
            for(int l1 = i1; l1 < j1; l1++)
            {
                if(!func_530_e(k1, 64, l1))
                {
                    continue;
                }
                for(int i2 = k - 1; i2 < l; i2++)
                {
                    Block block = Block.field_542_n[func_444_a(k1, i2, l1)];
                    if(block != null)
                    {
                        block.func_264_a(this, k1, i2, l1, p_481_2_, field_9207_I);
                    }
                }

            }

        }

        double d = 0.25D;
        List list = func_450_b(p_481_1_, p_481_2_.func_708_b(d, d, d));
        for(int j2 = 0; j2 < list.size(); j2++)
        {
            AxisAlignedBB axisalignedbb = ((Entity)list.get(j2)).func_93_n();
            if(axisalignedbb != null && axisalignedbb.func_704_a(p_481_2_))
            {
                field_9207_I.add(axisalignedbb);
            }
            axisalignedbb = p_481_1_.func_89_d((Entity)list.get(j2));
            if(axisalignedbb != null && axisalignedbb.func_704_a(p_481_2_))
            {
                field_9207_I.add(axisalignedbb);
            }
        }

        return field_9207_I;
    }

    public int func_493_a(float p_493_1_)
    {
        float f = func_477_b(p_493_1_);
        float f1 = 1.0F - (MathHelper.func_582_b(f * 3.141593F * 2.0F) * 2.0F + 0.5F);
        if(f1 < 0.0F)
        {
            f1 = 0.0F;
        }
        if(f1 > 1.0F)
        {
            f1 = 1.0F;
        }
        f1 = 1.0F - f1;
        f1 = (float)((double)f1 * (1.0D - (double)(func_27074_d(p_493_1_) * 5F) / 16D));
        f1 = (float)((double)f1 * (1.0D - (double)(func_27065_c(p_493_1_) * 5F) / 16D));
        f1 = 1.0F - f1;
        return (int)(f1 * 11F);
    }

    public float func_477_b(float p_477_1_)
    {
        return field_4272_q.func_4089_a(field_797_s.func_22186_f(), p_477_1_);
    }

    public int func_28100_e(int p_28100_1_, int p_28100_2_)
    {
        return func_492_a(p_28100_1_, p_28100_2_).func_35631_c(p_28100_1_ & 0xf, p_28100_2_ & 0xf);
    }

    public int func_4075_e(int p_4075_1_, int p_4075_2_)
    {
        Chunk chunk = func_492_a(p_4075_1_, p_4075_2_);
        int i = chunk.func_48561_g() + 16;
        p_4075_1_ &= 0xf;
        p_4075_2_ &= 0xf;
        while(i > 0) 
        {
            int j = chunk.func_344_a(p_4075_1_, i, p_4075_2_);
            if(j == 0 || !Block.field_542_n[j].field_553_bn.func_218_c() || Block.field_542_n[j].field_553_bn == Material.field_4218_h)
            {
                i--;
            } else
            {
                return i + 1;
            }
        }
        return -1;
    }

    public void func_22074_c(int p_22074_1_, int p_22074_2_, int p_22074_3_, int p_22074_4_, int p_22074_5_)
    {
        NextTickListEntry nextticklistentry = new NextTickListEntry(p_22074_1_, p_22074_2_, p_22074_3_, p_22074_4_);
        byte byte0 = 8;
        if(field_4280_a)
        {
            if(func_466_a(nextticklistentry.field_1018_a - byte0, nextticklistentry.field_1017_b - byte0, nextticklistentry.field_1023_c - byte0, nextticklistentry.field_1018_a + byte0, nextticklistentry.field_1017_b + byte0, nextticklistentry.field_1023_c + byte0))
            {
                int i = func_444_a(nextticklistentry.field_1018_a, nextticklistentry.field_1017_b, nextticklistentry.field_1023_c);
                if(i == nextticklistentry.field_1022_d && i > 0)
                {
                    Block.field_542_n[i].func_221_a(this, nextticklistentry.field_1018_a, nextticklistentry.field_1017_b, nextticklistentry.field_1023_c, field_803_m);
                }
            }
            return;
        }
        if(func_466_a(p_22074_1_ - byte0, p_22074_2_ - byte0, p_22074_3_ - byte0, p_22074_1_ + byte0, p_22074_2_ + byte0, p_22074_3_ + byte0))
        {
            if(p_22074_4_ > 0)
            {
                nextticklistentry.func_733_a((long)p_22074_5_ + field_797_s.func_22186_f());
            }
            if(!field_788_B.contains(nextticklistentry))
            {
                field_788_B.add(nextticklistentry);
                field_789_A.add(nextticklistentry);
            }
        }
    }

    public void func_41045_d(int p_41045_1_, int p_41045_2_, int p_41045_3_, int p_41045_4_, int p_41045_5_)
    {
        NextTickListEntry nextticklistentry = new NextTickListEntry(p_41045_1_, p_41045_2_, p_41045_3_, p_41045_4_);
        if(p_41045_4_ > 0)
        {
            nextticklistentry.func_733_a((long)p_41045_5_ + field_797_s.func_22186_f());
        }
        if(!field_788_B.contains(nextticklistentry))
        {
            field_788_B.add(nextticklistentry);
            field_789_A.add(nextticklistentry);
        }
    }

    public void func_459_b()
    {
        Profiler.func_40518_a("entities");
        Profiler.func_40518_a("global");
        for(int i = 0; i < field_27081_e.size(); i++)
        {
            Entity entity = (Entity)field_27081_e.get(i);
            entity.func_106_b_();
            if(entity.field_304_B)
            {
                field_27081_e.remove(i--);
            }
        }

        Profiler.func_40519_b("remove");
        field_815_a.removeAll(field_790_z);
        for(int j = 0; j < field_790_z.size(); j++)
        {
            Entity entity1 = (Entity)field_790_z.get(j);
            int i1 = entity1.field_307_aa;
            int k1 = entity1.field_303_ac;
            if(entity1.field_276_Z && func_447_g(i1, k1))
            {
                func_525_b(i1, k1).func_350_b(entity1);
            }
        }

        for(int k = 0; k < field_790_z.size(); k++)
        {
            func_531_c((Entity)field_790_z.get(k));
        }

        field_790_z.clear();
        Profiler.func_40519_b("regular");
        for(int l = 0; l < field_815_a.size(); l++)
        {
            Entity entity2 = (Entity)field_815_a.get(l);
            if(entity2.field_327_g != null)
            {
                if(!entity2.field_327_g.field_304_B && entity2.field_327_g.field_328_f == entity2)
                {
                    continue;
                }
                entity2.field_327_g.field_328_f = null;
                entity2.field_327_g = null;
            }
            if(!entity2.field_304_B)
            {
                func_520_e(entity2);
            }
            Profiler.func_40518_a("remove");
            if(entity2.field_304_B)
            {
                int j1 = entity2.field_307_aa;
                int l1 = entity2.field_303_ac;
                if(entity2.field_276_Z && func_447_g(j1, l1))
                {
                    func_525_b(j1, l1).func_350_b(entity2);
                }
                field_815_a.remove(l--);
                func_531_c(entity2);
            }
            Profiler.func_40517_a();
        }

        Profiler.func_40519_b("tileEntities");
        field_31048_L = true;
        Iterator iterator = field_814_b.iterator();
        do
        {
            if(!iterator.hasNext())
            {
                break;
            }
            TileEntity tileentity = (TileEntity)iterator.next();
            if(!tileentity.func_31004_g() && tileentity.field_479_a != null && func_530_e(tileentity.field_478_b, tileentity.field_483_c, tileentity.field_482_d))
            {
                tileentity.func_184_b();
            }
            if(tileentity.func_31004_g())
            {
                iterator.remove();
                if(func_447_g(tileentity.field_478_b >> 4, tileentity.field_482_d >> 4))
                {
                    Chunk chunk = func_525_b(tileentity.field_478_b >> 4, tileentity.field_482_d >> 4);
                    if(chunk != null)
                    {
                        chunk.func_359_e(tileentity.field_478_b & 0xf, tileentity.field_483_c, tileentity.field_482_d & 0xf);
                    }
                }
            }
        } while(true);
        field_31048_L = false;
        if(!field_34900_Q.isEmpty())
        {
            field_814_b.removeAll(field_34900_Q);
            field_34900_Q.clear();
        }
        Profiler.func_40519_b("pendingTileEntities");
        if(!field_34901_P.isEmpty())
        {
            Iterator iterator1 = field_34901_P.iterator();
            do
            {
                if(!iterator1.hasNext())
                {
                    break;
                }
                TileEntity tileentity1 = (TileEntity)iterator1.next();
                if(!tileentity1.func_31004_g())
                {
                    if(!field_814_b.contains(tileentity1))
                    {
                        field_814_b.add(tileentity1);
                    }
                    if(func_447_g(tileentity1.field_478_b >> 4, tileentity1.field_482_d >> 4))
                    {
                        Chunk chunk1 = func_525_b(tileentity1.field_478_b >> 4, tileentity1.field_482_d >> 4);
                        if(chunk1 != null)
                        {
                            chunk1.func_352_a(tileentity1.field_478_b & 0xf, tileentity1.field_483_c, tileentity1.field_482_d & 0xf, tileentity1);
                        }
                    }
                    func_521_f(tileentity1.field_478_b, tileentity1.field_483_c, tileentity1.field_482_d);
                }
            } while(true);
            field_34901_P.clear();
        }
        Profiler.func_40517_a();
        Profiler.func_40517_a();
    }

    public void func_31047_a(Collection p_31047_1_)
    {
        if(field_31048_L)
        {
            field_34901_P.addAll(p_31047_1_);
        } else
        {
            field_814_b.addAll(p_31047_1_);
        }
    }

    public void func_520_e(Entity p_520_1_)
    {
        func_4074_a(p_520_1_, true);
    }

    public void func_4074_a(Entity p_4074_1_, boolean p_4074_2_)
    {
        int i = MathHelper.func_584_b(p_4074_1_.field_322_l);
        int j = MathHelper.func_584_b(p_4074_1_.field_320_n);
        byte byte0 = 32;
        if(p_4074_2_ && !func_466_a(i - byte0, 0, j - byte0, i + byte0, 0, j + byte0))
        {
            return;
        }
        p_4074_1_.field_9071_O = p_4074_1_.field_322_l;
        p_4074_1_.field_9070_P = p_4074_1_.field_321_m;
        p_4074_1_.field_9069_Q = p_4074_1_.field_320_n;
        p_4074_1_.field_9089_x = p_4074_1_.field_316_r;
        p_4074_1_.field_9088_y = p_4074_1_.field_315_s;
        if(p_4074_2_ && p_4074_1_.field_276_Z)
        {
            if(p_4074_1_.field_327_g != null)
            {
                p_4074_1_.func_115_v();
            } else
            {
                p_4074_1_.func_106_b_();
            }
        }
        Profiler.func_40518_a("chunkCheck");
        if(Double.isNaN(p_4074_1_.field_322_l) || Double.isInfinite(p_4074_1_.field_322_l))
        {
            p_4074_1_.field_322_l = p_4074_1_.field_9071_O;
        }
        if(Double.isNaN(p_4074_1_.field_321_m) || Double.isInfinite(p_4074_1_.field_321_m))
        {
            p_4074_1_.field_321_m = p_4074_1_.field_9070_P;
        }
        if(Double.isNaN(p_4074_1_.field_320_n) || Double.isInfinite(p_4074_1_.field_320_n))
        {
            p_4074_1_.field_320_n = p_4074_1_.field_9069_Q;
        }
        if(Double.isNaN(p_4074_1_.field_315_s) || Double.isInfinite(p_4074_1_.field_315_s))
        {
            p_4074_1_.field_315_s = p_4074_1_.field_9088_y;
        }
        if(Double.isNaN(p_4074_1_.field_316_r) || Double.isInfinite(p_4074_1_.field_316_r))
        {
            p_4074_1_.field_316_r = p_4074_1_.field_9089_x;
        }
        int k = MathHelper.func_584_b(p_4074_1_.field_322_l / 16D);
        int l = MathHelper.func_584_b(p_4074_1_.field_321_m / 16D);
        int i1 = MathHelper.func_584_b(p_4074_1_.field_320_n / 16D);
        if(!p_4074_1_.field_276_Z || p_4074_1_.field_307_aa != k || p_4074_1_.field_305_ab != l || p_4074_1_.field_303_ac != i1)
        {
            if(p_4074_1_.field_276_Z && func_447_g(p_4074_1_.field_307_aa, p_4074_1_.field_303_ac))
            {
                func_525_b(p_4074_1_.field_307_aa, p_4074_1_.field_303_ac).func_332_a(p_4074_1_, p_4074_1_.field_305_ab);
            }
            if(func_447_g(k, i1))
            {
                p_4074_1_.field_276_Z = true;
                func_525_b(k, i1).func_335_a(p_4074_1_);
            } else
            {
                p_4074_1_.field_276_Z = false;
            }
        }
        Profiler.func_40517_a();
        if(p_4074_2_ && p_4074_1_.field_276_Z && p_4074_1_.field_328_f != null)
        {
            if(p_4074_1_.field_328_f.field_304_B || p_4074_1_.field_328_f.field_327_g != p_4074_1_)
            {
                p_4074_1_.field_328_f.field_327_g = null;
                p_4074_1_.field_328_f = null;
            } else
            {
                func_520_e(p_4074_1_.field_328_f);
            }
        }
    }

    public boolean func_522_a(AxisAlignedBB p_522_1_)
    {
        List list = func_450_b(null, p_522_1_);
        for(int i = 0; i < list.size(); i++)
        {
            Entity entity = (Entity)list.get(i);
            if(!entity.field_304_B && entity.field_329_e)
            {
                return false;
            }
        }

        return true;
    }

    public boolean func_27069_b(AxisAlignedBB p_27069_1_)
    {
        int i = MathHelper.func_584_b(p_27069_1_.field_964_a);
        int j = MathHelper.func_584_b(p_27069_1_.field_969_d + 1.0D);
        int k = MathHelper.func_584_b(p_27069_1_.field_963_b);
        int l = MathHelper.func_584_b(p_27069_1_.field_968_e + 1.0D);
        int i1 = MathHelper.func_584_b(p_27069_1_.field_970_c);
        int j1 = MathHelper.func_584_b(p_27069_1_.field_967_f + 1.0D);
        if(p_27069_1_.field_964_a < 0.0D)
        {
            i--;
        }
        if(p_27069_1_.field_963_b < 0.0D)
        {
            k--;
        }
        if(p_27069_1_.field_970_c < 0.0D)
        {
            i1--;
        }
        for(int k1 = i; k1 < j; k1++)
        {
            for(int l1 = k; l1 < l; l1++)
            {
                for(int i2 = i1; i2 < j1; i2++)
                {
                    Block block = Block.field_542_n[func_444_a(k1, l1, i2)];
                    if(block != null)
                    {
                        return true;
                    }
                }

            }

        }

        return false;
    }

    public boolean func_469_b(AxisAlignedBB p_469_1_)
    {
        int i = MathHelper.func_584_b(p_469_1_.field_964_a);
        int j = MathHelper.func_584_b(p_469_1_.field_969_d + 1.0D);
        int k = MathHelper.func_584_b(p_469_1_.field_963_b);
        int l = MathHelper.func_584_b(p_469_1_.field_968_e + 1.0D);
        int i1 = MathHelper.func_584_b(p_469_1_.field_970_c);
        int j1 = MathHelper.func_584_b(p_469_1_.field_967_f + 1.0D);
        if(p_469_1_.field_964_a < 0.0D)
        {
            i--;
        }
        if(p_469_1_.field_963_b < 0.0D)
        {
            k--;
        }
        if(p_469_1_.field_970_c < 0.0D)
        {
            i1--;
        }
        for(int k1 = i; k1 < j; k1++)
        {
            for(int l1 = k; l1 < l; l1++)
            {
                for(int i2 = i1; i2 < j1; i2++)
                {
                    Block block = Block.field_542_n[func_444_a(k1, l1, i2)];
                    if(block != null && block.field_553_bn.func_217_d())
                    {
                        return true;
                    }
                }

            }

        }

        return false;
    }

    public boolean func_523_c(AxisAlignedBB p_523_1_)
    {
        int i = MathHelper.func_584_b(p_523_1_.field_964_a);
        int j = MathHelper.func_584_b(p_523_1_.field_969_d + 1.0D);
        int k = MathHelper.func_584_b(p_523_1_.field_963_b);
        int l = MathHelper.func_584_b(p_523_1_.field_968_e + 1.0D);
        int i1 = MathHelper.func_584_b(p_523_1_.field_970_c);
        int j1 = MathHelper.func_584_b(p_523_1_.field_967_f + 1.0D);
        if(func_466_a(i, k, i1, j, l, j1))
        {
            for(int k1 = i; k1 < j; k1++)
            {
                for(int l1 = k; l1 < l; l1++)
                {
                    for(int i2 = i1; i2 < j1; i2++)
                    {
                        int j2 = func_444_a(k1, l1, i2);
                        if(j2 == Block.field_599_as.field_573_bc || j2 == Block.field_594_D.field_573_bc || j2 == Block.field_592_E.field_573_bc)
                        {
                            return true;
                        }
                    }

                }

            }

        }
        return false;
    }

    public boolean func_490_a(AxisAlignedBB p_490_1_, Material p_490_2_, Entity p_490_3_)
    {
        int i = MathHelper.func_584_b(p_490_1_.field_964_a);
        int j = MathHelper.func_584_b(p_490_1_.field_969_d + 1.0D);
        int k = MathHelper.func_584_b(p_490_1_.field_963_b);
        int l = MathHelper.func_584_b(p_490_1_.field_968_e + 1.0D);
        int i1 = MathHelper.func_584_b(p_490_1_.field_970_c);
        int j1 = MathHelper.func_584_b(p_490_1_.field_967_f + 1.0D);
        if(!func_466_a(i, k, i1, j, l, j1))
        {
            return false;
        }
        boolean flag = false;
        Vec3D vec3d = Vec3D.func_768_b(0.0D, 0.0D, 0.0D);
        for(int k1 = i; k1 < j; k1++)
        {
            for(int l1 = k; l1 < l; l1++)
            {
                for(int i2 = i1; i2 < j1; i2++)
                {
                    Block block = Block.field_542_n[func_444_a(k1, l1, i2)];
                    if(block == null || block.field_553_bn != p_490_2_)
                    {
                        continue;
                    }
                    double d1 = (float)(l1 + 1) - BlockFluid.func_299_b(func_446_b(k1, l1, i2));
                    if((double)l >= d1)
                    {
                        flag = true;
                        block.func_230_a(this, k1, l1, i2, p_490_3_, vec3d);
                    }
                }

            }

        }

        if(vec3d.func_765_c() > 0.0D)
        {
            vec3d = vec3d.func_758_b();
            double d = 0.014D;
            p_490_3_.field_319_o += vec3d.field_1055_a * d;
            p_490_3_.field_318_p += vec3d.field_1054_b * d;
            p_490_3_.field_317_q += vec3d.field_1058_c * d;
        }
        return flag;
    }

    public boolean func_500_a(AxisAlignedBB p_500_1_, Material p_500_2_)
    {
        int i = MathHelper.func_584_b(p_500_1_.field_964_a);
        int j = MathHelper.func_584_b(p_500_1_.field_969_d + 1.0D);
        int k = MathHelper.func_584_b(p_500_1_.field_963_b);
        int l = MathHelper.func_584_b(p_500_1_.field_968_e + 1.0D);
        int i1 = MathHelper.func_584_b(p_500_1_.field_970_c);
        int j1 = MathHelper.func_584_b(p_500_1_.field_967_f + 1.0D);
        for(int k1 = i; k1 < j; k1++)
        {
            for(int l1 = k; l1 < l; l1++)
            {
                for(int i2 = i1; i2 < j1; i2++)
                {
                    Block block = Block.field_542_n[func_444_a(k1, l1, i2)];
                    if(block != null && block.field_553_bn == p_500_2_)
                    {
                        return true;
                    }
                }

            }

        }

        return false;
    }

    public boolean func_524_b(AxisAlignedBB p_524_1_, Material p_524_2_)
    {
        int i = MathHelper.func_584_b(p_524_1_.field_964_a);
        int j = MathHelper.func_584_b(p_524_1_.field_969_d + 1.0D);
        int k = MathHelper.func_584_b(p_524_1_.field_963_b);
        int l = MathHelper.func_584_b(p_524_1_.field_968_e + 1.0D);
        int i1 = MathHelper.func_584_b(p_524_1_.field_970_c);
        int j1 = MathHelper.func_584_b(p_524_1_.field_967_f + 1.0D);
        for(int k1 = i; k1 < j; k1++)
        {
            for(int l1 = k; l1 < l; l1++)
            {
                for(int i2 = i1; i2 < j1; i2++)
                {
                    Block block = Block.field_542_n[func_444_a(k1, l1, i2)];
                    if(block == null || block.field_553_bn != p_524_2_)
                    {
                        continue;
                    }
                    int j2 = func_446_b(k1, l1, i2);
                    double d = l1 + 1;
                    if(j2 < 8)
                    {
                        d = (double)(l1 + 1) - (double)j2 / 8D;
                    }
                    if(d >= p_524_1_.field_963_b)
                    {
                        return true;
                    }
                }

            }

        }

        return false;
    }

    public Explosion func_12013_a(Entity p_12013_1_, double p_12013_2_, double p_12013_4_, double p_12013_6_, 
            float p_12013_8_)
    {
        return func_12015_a(p_12013_1_, p_12013_2_, p_12013_4_, p_12013_6_, p_12013_8_, false);
    }

    public Explosion func_12015_a(Entity p_12015_1_, double p_12015_2_, double p_12015_4_, double p_12015_6_, 
            float p_12015_8_, boolean p_12015_9_)
    {
        Explosion explosion = new Explosion(this, p_12015_1_, p_12015_2_, p_12015_4_, p_12015_6_, p_12015_8_);
        explosion.field_12031_a = p_12015_9_;
        explosion.func_12023_a();
        explosion.func_732_a(true);
        return explosion;
    }

    public float func_494_a(Vec3D p_494_1_, AxisAlignedBB p_494_2_)
    {
        double d = 1.0D / ((p_494_2_.field_969_d - p_494_2_.field_964_a) * 2D + 1.0D);
        double d1 = 1.0D / ((p_494_2_.field_968_e - p_494_2_.field_963_b) * 2D + 1.0D);
        double d2 = 1.0D / ((p_494_2_.field_967_f - p_494_2_.field_970_c) * 2D + 1.0D);
        int i = 0;
        int j = 0;
        for(float f = 0.0F; f <= 1.0F; f = (float)((double)f + d))
        {
            for(float f1 = 0.0F; f1 <= 1.0F; f1 = (float)((double)f1 + d1))
            {
                for(float f2 = 0.0F; f2 <= 1.0F; f2 = (float)((double)f2 + d2))
                {
                    double d3 = p_494_2_.field_964_a + (p_494_2_.field_969_d - p_494_2_.field_964_a) * (double)f;
                    double d4 = p_494_2_.field_963_b + (p_494_2_.field_968_e - p_494_2_.field_963_b) * (double)f1;
                    double d5 = p_494_2_.field_970_c + (p_494_2_.field_967_f - p_494_2_.field_970_c) * (double)f2;
                    if(func_486_a(Vec3D.func_768_b(d3, d4, d5), p_494_1_) == null)
                    {
                        i++;
                    }
                    j++;
                }

            }

        }

        return (float)i / (float)j;
    }

    public boolean func_48093_a(EntityPlayer p_48093_1_, int p_48093_2_, int p_48093_3_, int p_48093_4_, int p_48093_5_)
    {
        if(p_48093_5_ == 0)
        {
            p_48093_3_--;
        }
        if(p_48093_5_ == 1)
        {
            p_48093_3_++;
        }
        if(p_48093_5_ == 2)
        {
            p_48093_4_--;
        }
        if(p_48093_5_ == 3)
        {
            p_48093_4_++;
        }
        if(p_48093_5_ == 4)
        {
            p_48093_2_--;
        }
        if(p_48093_5_ == 5)
        {
            p_48093_2_++;
        }
        if(func_444_a(p_48093_2_, p_48093_3_, p_48093_4_) == Block.field_599_as.field_573_bc)
        {
            func_28101_a(p_48093_1_, 1004, p_48093_2_, p_48093_3_, p_48093_4_, 0);
            func_508_d(p_48093_2_, p_48093_3_, p_48093_4_, 0);
            return true;
        } else
        {
            return false;
        }
    }

    public TileEntity func_451_k(int p_451_1_, int p_451_2_, int p_451_3_)
    {
label0:
        {
            TileEntity tileentity;
label1:
            {
                if(p_451_2_ >= 256)
                {
                    return null;
                }
                Chunk chunk = func_525_b(p_451_1_ >> 4, p_451_3_ >> 4);
                if(chunk == null)
                {
                    break label0;
                }
                tileentity = chunk.func_338_d(p_451_1_ & 0xf, p_451_2_, p_451_3_ & 0xf);
                if(tileentity != null)
                {
                    break label1;
                }
                Iterator iterator = field_34901_P.iterator();
                TileEntity tileentity1;
                do
                {
                    if(!iterator.hasNext())
                    {
                        break label1;
                    }
                    tileentity1 = (TileEntity)iterator.next();
                } while(tileentity1.func_31004_g() || tileentity1.field_478_b != p_451_1_ || tileentity1.field_483_c != p_451_2_ || tileentity1.field_482_d != p_451_3_);
                tileentity = tileentity1;
            }
            return tileentity;
        }
        return null;
    }

    public void func_473_a(int p_473_1_, int p_473_2_, int p_473_3_, TileEntity p_473_4_)
    {
        if(p_473_4_ != null && !p_473_4_.func_31004_g())
        {
            if(field_31048_L)
            {
                p_473_4_.field_478_b = p_473_1_;
                p_473_4_.field_483_c = p_473_2_;
                p_473_4_.field_482_d = p_473_3_;
                field_34901_P.add(p_473_4_);
            } else
            {
                field_814_b.add(p_473_4_);
                Chunk chunk = func_525_b(p_473_1_ >> 4, p_473_3_ >> 4);
                if(chunk != null)
                {
                    chunk.func_352_a(p_473_1_ & 0xf, p_473_2_, p_473_3_ & 0xf, p_473_4_);
                }
            }
        }
    }

    public void func_513_l(int p_513_1_, int p_513_2_, int p_513_3_)
    {
        TileEntity tileentity = func_451_k(p_513_1_, p_513_2_, p_513_3_);
        if(tileentity != null && field_31048_L)
        {
            tileentity.func_31003_h();
            field_34901_P.remove(tileentity);
        } else
        {
            if(tileentity != null)
            {
                field_34901_P.remove(tileentity);
                field_814_b.remove(tileentity);
            }
            Chunk chunk = func_525_b(p_513_1_ >> 4, p_513_3_ >> 4);
            if(chunk != null)
            {
                chunk.func_359_e(p_513_1_ & 0xf, p_513_2_, p_513_3_ & 0xf);
            }
        }
    }

    public void func_35239_a(TileEntity p_35239_1_)
    {
        field_34900_Q.add(p_35239_1_);
    }

    public boolean func_28095_p(int p_28095_1_, int p_28095_2_, int p_28095_3_)
    {
        Block block = Block.field_542_n[func_444_a(p_28095_1_, p_28095_2_, p_28095_3_)];
        if(block == null)
        {
            return false;
        } else
        {
            return block.func_240_b();
        }
    }

    public boolean func_445_d(int p_445_1_, int p_445_2_, int p_445_3_)
    {
        return Block.func_48126_g(func_444_a(p_445_1_, p_445_2_, p_445_3_));
    }

    public boolean func_41047_b(int p_41047_1_, int p_41047_2_, int p_41047_3_, boolean p_41047_4_)
    {
        if(p_41047_1_ < 0xfe363c80 || p_41047_3_ < 0xfe363c80 || p_41047_1_ >= 0x1c9c380 || p_41047_3_ >= 0x1c9c380)
        {
            return p_41047_4_;
        }
        Chunk chunk = field_783_G.func_363_b(p_41047_1_ >> 4, p_41047_3_ >> 4);
        if(chunk == null || chunk.func_41049_g())
        {
            return p_41047_4_;
        }
        Block block = Block.field_542_n[func_444_a(p_41047_1_, p_41047_2_, p_41047_3_)];
        if(block == null)
        {
            return false;
        } else
        {
            return block.field_553_bn.func_28128_h() && block.func_28025_b();
        }
    }

    public void func_465_d()
    {
        int i = func_493_a(1.0F);
        if(i != field_811_e)
        {
            field_811_e = i;
        }
    }

    public void func_21116_a(boolean p_21116_1_, boolean p_21116_2_)
    {
        field_21121_K = p_21116_1_;
        field_21120_L = p_21116_2_;
    }

    public void func_22077_g()
    {
        if(func_22081_n().func_40470_p() && field_804_l < 3)
        {
            field_804_l = 3;
        }
        field_4272_q.field_4301_b.func_35138_b();
        func_27066_i();
        if(func_22084_q())
        {
            boolean flag = false;
            if(field_21121_K)
            {
                if(field_804_l < 1);
            }
            if(!flag)
            {
                long l = field_797_s.func_22186_f() + 24000L;
                field_797_s.func_22180_a(l - l % 24000L);
                func_22083_p();
            }
        }
        Profiler.func_40518_a("mobSpawner");
        SpawnerAnimals.func_4111_a(this, field_21121_K, field_21120_L && field_797_s.func_22186_f() % 400L == 0L);
        Profiler.func_40519_b("chunkSource");
        field_783_G.func_361_a();
        int i = func_493_a(1.0F);
        if(i != field_811_e)
        {
            field_811_e = i;
        }
        long l1 = field_797_s.func_22186_f() + 1L;
        if(l1 % (long)field_4277_j == 0L)
        {
            Profiler.func_40519_b("save");
            func_485_a(false, null);
        }
        field_797_s.func_22180_a(l1);
        Profiler.func_40519_b("tickPending");
        func_518_a(false);
        Profiler.func_40519_b("tickTiles");
        func_4073_g();
        Profiler.func_40519_b("village");
        field_48096_A.func_48633_a();
        field_48097_O.func_48500_a();
        Profiler.func_40517_a();
    }

    private void func_27070_x()
    {
        if(field_797_s.func_27115_l())
        {
            field_27078_C = 1.0F;
            if(field_797_s.func_27122_j())
            {
                field_27076_E = 1.0F;
            }
        }
    }

    protected void func_27066_i()
    {
        if(field_4272_q.field_4306_c)
        {
            return;
        }
        if(field_27075_F > 0)
        {
            field_27075_F--;
        }
        int i = field_797_s.func_27117_k();
        if(i <= 0)
        {
            if(field_797_s.func_27122_j())
            {
                field_797_s.func_27121_b(field_803_m.nextInt(12000) + 3600);
            } else
            {
                field_797_s.func_27121_b(field_803_m.nextInt(0x29040) + 12000);
            }
        } else
        {
            i--;
            field_797_s.func_27121_b(i);
            if(i <= 0)
            {
                field_797_s.func_27119_a(!field_797_s.func_27122_j());
            }
        }
        int j = field_797_s.func_27120_m();
        if(j <= 0)
        {
            if(field_797_s.func_27115_l())
            {
                field_797_s.func_27118_c(field_803_m.nextInt(12000) + 12000);
            } else
            {
                field_797_s.func_27118_c(field_803_m.nextInt(0x29040) + 12000);
            }
        } else
        {
            j--;
            field_797_s.func_27118_c(j);
            if(j <= 0)
            {
                field_797_s.func_27116_b(!field_797_s.func_27115_l());
            }
        }
        field_27079_B = field_27078_C;
        if(field_797_s.func_27115_l())
        {
            field_27078_C += 0.01D;
        } else
        {
            field_27078_C -= 0.01D;
        }
        if(field_27078_C < 0.0F)
        {
            field_27078_C = 0.0F;
        }
        if(field_27078_C > 1.0F)
        {
            field_27078_C = 1.0F;
        }
        field_27077_D = field_27076_E;
        if(field_797_s.func_27122_j())
        {
            field_27076_E += 0.01D;
        } else
        {
            field_27076_E -= 0.01D;
        }
        if(field_27076_E < 0.0F)
        {
            field_27076_E = 0.0F;
        }
        if(field_27076_E > 1.0F)
        {
            field_27076_E = 1.0F;
        }
    }

    private void func_27071_y()
    {
        field_797_s.func_27118_c(0);
        field_797_s.func_27116_b(false);
        field_797_s.func_27121_b(0);
        field_797_s.func_27119_a(false);
    }

    public void func_40218_j()
    {
        field_797_s.func_27118_c(1);
    }

    protected void func_48090_k()
    {
        field_4264_K.clear();
        Profiler.func_40518_a("buildList");
        for(int i = 0; i < field_805_k.size(); i++)
        {
            EntityPlayer entityplayer = (EntityPlayer)field_805_k.get(i);
            int k = MathHelper.func_584_b(entityplayer.field_322_l / 16D);
            int i1 = MathHelper.func_584_b(entityplayer.field_320_n / 16D);
            byte byte0 = 7;
            for(int l1 = -byte0; l1 <= byte0; l1++)
            {
                for(int i2 = -byte0; i2 <= byte0; i2++)
                {
                    field_4264_K.add(new ChunkCoordIntPair(l1 + k, i2 + i1));
                }

            }

        }

        Profiler.func_40517_a();
        if(field_4263_L > 0)
        {
            field_4263_L--;
        }
        Profiler.func_40518_a("playerCheckLight");
        if(!field_805_k.isEmpty())
        {
            int j = field_803_m.nextInt(field_805_k.size());
            EntityPlayer entityplayer1 = (EntityPlayer)field_805_k.get(j);
            int l = (MathHelper.func_584_b(entityplayer1.field_322_l) + field_803_m.nextInt(11)) - 5;
            int j1 = (MathHelper.func_584_b(entityplayer1.field_321_m) + field_803_m.nextInt(11)) - 5;
            int k1 = (MathHelper.func_584_b(entityplayer1.field_320_n) + field_803_m.nextInt(11)) - 5;
            func_35242_p(l, j1, k1);
        }
        Profiler.func_40517_a();
    }

    protected void func_48094_a(int p_48094_1_, int p_48094_2_, Chunk p_48094_3_)
    {
        Profiler.func_40519_b("tickChunk");
        p_48094_3_.func_35635_h();
        Profiler.func_40519_b("moodSound");
        if(field_4263_L == 0)
        {
            field_4279_g = field_4279_g * 3 + 0x3c6ef35f;
            int i = field_4279_g >> 2;
            int j = i & 0xf;
            int k = i >> 8 & 0xf;
            int l = i >> 16 & 0x7f;
            int i1 = p_48094_3_.func_344_a(j, l, k);
            j += p_48094_1_;
            k += p_48094_2_;
            if(i1 == 0 && func_28098_j(j, l, k) <= field_803_m.nextInt(8) && func_512_a(EnumSkyBlock.Sky, j, l, k) <= 0)
            {
                EntityPlayer entityplayer = func_456_a((double)j + 0.5D, (double)l + 0.5D, (double)k + 0.5D, 8D);
                if(entityplayer != null && entityplayer.func_101_d((double)j + 0.5D, (double)l + 0.5D, (double)k + 0.5D) > 4D)
                {
                    func_502_a((double)j + 0.5D, (double)l + 0.5D, (double)k + 0.5D, "ambient.cave.cave", 0.7F, 0.8F + field_803_m.nextFloat() * 0.2F);
                    field_4263_L = field_803_m.nextInt(12000) + 6000;
                }
            }
        }
        Profiler.func_40519_b("checkLight");
        p_48094_3_.func_48557_n();
    }

    protected void func_4073_g()
    {
        func_48090_k();
        int i = 0;
        int j = 0;
        for(Iterator iterator = field_4264_K.iterator(); iterator.hasNext(); Profiler.func_40517_a())
        {
            ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair)iterator.next();
            int k = chunkcoordintpair.field_152_a * 16;
            int l = chunkcoordintpair.field_151_b * 16;
            Profiler.func_40518_a("getChunk");
            Chunk chunk = func_525_b(chunkcoordintpair.field_152_a, chunkcoordintpair.field_151_b);
            func_48094_a(k, l, chunk);
            Profiler.func_40519_b("thunder");
            if(field_803_m.nextInt(0x186a0) == 0 && func_27068_v() && func_27067_u())
            {
                field_4279_g = field_4279_g * 3 + 0x3c6ef35f;
                int i1 = field_4279_g >> 2;
                int k1 = k + (i1 & 0xf);
                int j2 = l + (i1 >> 8 & 0xf);
                int i3 = func_28100_e(k1, j2);
                if(func_27072_q(k1, i3, j2))
                {
                    func_27073_a(new EntityLightningBolt(this, k1, i3, j2));
                    field_27075_F = 2;
                }
            }
            Profiler.func_40519_b("iceandsnow");
            if(field_803_m.nextInt(16) == 0)
            {
                field_4279_g = field_4279_g * 3 + 0x3c6ef35f;
                int j1 = field_4279_g >> 2;
                int l1 = j1 & 0xf;
                int k2 = j1 >> 8 & 0xf;
                int j3 = func_28100_e(l1 + k, k2 + l);
                if(func_40217_q(l1 + k, j3 - 1, k2 + l))
                {
                    func_508_d(l1 + k, j3 - 1, k2 + l, Block.field_4077_aU.field_573_bc);
                }
                if(func_27068_v() && func_40215_r(l1 + k, j3, k2 + l))
                {
                    func_508_d(l1 + k, j3, k2 + l, Block.field_625_aT.field_573_bc);
                }
            }
            Profiler.func_40519_b("tickTiles");
            ExtendedBlockStorage aextendedblockstorage[] = chunk.func_48553_h();
            int i2 = aextendedblockstorage.length;
            for(int l2 = 0; l2 < i2; l2++)
            {
                ExtendedBlockStorage extendedblockstorage = aextendedblockstorage[l2];
                if(extendedblockstorage == null || !extendedblockstorage.func_48607_b())
                {
                    continue;
                }
                for(int k3 = 0; k3 < 3; k3++)
                {
                    field_4279_g = field_4279_g * 3 + 0x3c6ef35f;
                    int l3 = field_4279_g >> 2;
                    int i4 = l3 & 0xf;
                    int j4 = l3 >> 8 & 0xf;
                    int k4 = l3 >> 16 & 0xf;
                    int l4 = extendedblockstorage.func_48591_a(i4, k4, j4);
                    j++;
                    Block block = Block.field_542_n[l4];
                    if(block != null && block.func_48125_m())
                    {
                        i++;
                        block.func_221_a(this, i4 + k, k4 + extendedblockstorage.func_48597_c(), j4 + l, field_803_m);
                    }
                }

            }

        }

    }

    public boolean func_40210_p(int p_40210_1_, int p_40210_2_, int p_40210_3_)
    {
        return func_40213_b(p_40210_1_, p_40210_2_, p_40210_3_, false);
    }

    public boolean func_40217_q(int p_40217_1_, int p_40217_2_, int p_40217_3_)
    {
        return func_40213_b(p_40217_1_, p_40217_2_, p_40217_3_, true);
    }

    public boolean func_40213_b(int p_40213_1_, int p_40213_2_, int p_40213_3_, boolean p_40213_4_)
    {
        BiomeGenBase biomegenbase = func_48091_a(p_40213_1_, p_40213_3_);
        float f = biomegenbase.func_48442_h();
        if(f > 0.15F)
        {
            return false;
        }
        if(p_40213_2_ >= 0 && p_40213_2_ < 256 && func_512_a(EnumSkyBlock.Block, p_40213_1_, p_40213_2_, p_40213_3_) < 10)
        {
            int i = func_444_a(p_40213_1_, p_40213_2_, p_40213_3_);
            if((i == Block.field_596_C.field_573_bc || i == Block.field_598_B.field_573_bc) && func_446_b(p_40213_1_, p_40213_2_, p_40213_3_) == 0)
            {
                if(!p_40213_4_)
                {
                    return true;
                }
                boolean flag = true;
                if(flag && func_443_c(p_40213_1_ - 1, p_40213_2_, p_40213_3_) != Material.field_521_f)
                {
                    flag = false;
                }
                if(flag && func_443_c(p_40213_1_ + 1, p_40213_2_, p_40213_3_) != Material.field_521_f)
                {
                    flag = false;
                }
                if(flag && func_443_c(p_40213_1_, p_40213_2_, p_40213_3_ - 1) != Material.field_521_f)
                {
                    flag = false;
                }
                if(flag && func_443_c(p_40213_1_, p_40213_2_, p_40213_3_ + 1) != Material.field_521_f)
                {
                    flag = false;
                }
                if(!flag)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean func_40215_r(int p_40215_1_, int p_40215_2_, int p_40215_3_)
    {
        BiomeGenBase biomegenbase = func_48091_a(p_40215_1_, p_40215_3_);
        float f = biomegenbase.func_48442_h();
        if(f > 0.15F)
        {
            return false;
        }
        if(p_40215_2_ >= 0 && p_40215_2_ < 256 && func_512_a(EnumSkyBlock.Block, p_40215_1_, p_40215_2_, p_40215_3_) < 10)
        {
            int i = func_444_a(p_40215_1_, p_40215_2_ - 1, p_40215_3_);
            int j = func_444_a(p_40215_1_, p_40215_2_, p_40215_3_);
            if(j == 0 && Block.field_625_aT.func_259_a(this, p_40215_1_, p_40215_2_, p_40215_3_) && i != 0 && i != Block.field_4077_aU.field_573_bc && Block.field_542_n[i].field_553_bn.func_218_c())
            {
                return true;
            }
        }
        return false;
    }

    public void func_35242_p(int p_35242_1_, int p_35242_2_, int p_35242_3_)
    {
        if(!field_4272_q.field_4306_c)
        {
            func_35243_b(EnumSkyBlock.Sky, p_35242_1_, p_35242_2_, p_35242_3_);
        }
        func_35243_b(EnumSkyBlock.Block, p_35242_1_, p_35242_2_, p_35242_3_);
    }

    private int func_35240_d(int p_35240_1_, int p_35240_2_, int p_35240_3_, int p_35240_4_, int p_35240_5_, int p_35240_6_)
    {
        int i = 0;
        if(func_497_g(p_35240_2_, p_35240_3_, p_35240_4_))
        {
            i = 15;
        } else
        {
            if(p_35240_6_ == 0)
            {
                p_35240_6_ = 1;
            }
            int j = func_512_a(EnumSkyBlock.Sky, p_35240_2_ - 1, p_35240_3_, p_35240_4_) - p_35240_6_;
            int k = func_512_a(EnumSkyBlock.Sky, p_35240_2_ + 1, p_35240_3_, p_35240_4_) - p_35240_6_;
            int l = func_512_a(EnumSkyBlock.Sky, p_35240_2_, p_35240_3_ - 1, p_35240_4_) - p_35240_6_;
            int i1 = func_512_a(EnumSkyBlock.Sky, p_35240_2_, p_35240_3_ + 1, p_35240_4_) - p_35240_6_;
            int j1 = func_512_a(EnumSkyBlock.Sky, p_35240_2_, p_35240_3_, p_35240_4_ - 1) - p_35240_6_;
            int k1 = func_512_a(EnumSkyBlock.Sky, p_35240_2_, p_35240_3_, p_35240_4_ + 1) - p_35240_6_;
            if(j > i)
            {
                i = j;
            }
            if(k > i)
            {
                i = k;
            }
            if(l > i)
            {
                i = l;
            }
            if(i1 > i)
            {
                i = i1;
            }
            if(j1 > i)
            {
                i = j1;
            }
            if(k1 > i)
            {
                i = k1;
            }
        }
        return i;
    }

    private int func_35241_e(int p_35241_1_, int p_35241_2_, int p_35241_3_, int p_35241_4_, int p_35241_5_, int p_35241_6_)
    {
        int i = Block.field_536_t[p_35241_5_];
        int j = func_512_a(EnumSkyBlock.Block, p_35241_2_ - 1, p_35241_3_, p_35241_4_) - p_35241_6_;
        int k = func_512_a(EnumSkyBlock.Block, p_35241_2_ + 1, p_35241_3_, p_35241_4_) - p_35241_6_;
        int l = func_512_a(EnumSkyBlock.Block, p_35241_2_, p_35241_3_ - 1, p_35241_4_) - p_35241_6_;
        int i1 = func_512_a(EnumSkyBlock.Block, p_35241_2_, p_35241_3_ + 1, p_35241_4_) - p_35241_6_;
        int j1 = func_512_a(EnumSkyBlock.Block, p_35241_2_, p_35241_3_, p_35241_4_ - 1) - p_35241_6_;
        int k1 = func_512_a(EnumSkyBlock.Block, p_35241_2_, p_35241_3_, p_35241_4_ + 1) - p_35241_6_;
        if(j > i)
        {
            i = j;
        }
        if(k > i)
        {
            i = k;
        }
        if(l > i)
        {
            i = l;
        }
        if(i1 > i)
        {
            i = i1;
        }
        if(j1 > i)
        {
            i = j1;
        }
        if(k1 > i)
        {
            i = k1;
        }
        return i;
    }

    public void func_35243_b(EnumSkyBlock p_35243_1_, int p_35243_2_, int p_35243_3_, int p_35243_4_)
    {
        if(!func_21118_a(p_35243_2_, p_35243_3_, p_35243_4_, 17))
        {
            return;
        }
        int i = 0;
        int j = 0;
        Profiler.func_40518_a("getBrightness");
        int k = func_512_a(p_35243_1_, p_35243_2_, p_35243_3_, p_35243_4_);
        int i1 = 0;
        int k1 = k;
        int j2 = func_444_a(p_35243_2_, p_35243_3_, p_35243_4_);
        int i3 = func_48092_f(p_35243_2_, p_35243_3_, p_35243_4_);
        if(i3 == 0)
        {
            i3 = 1;
        }
        int l3 = 0;
        if(p_35243_1_ == EnumSkyBlock.Sky)
        {
            l3 = func_35240_d(k1, p_35243_2_, p_35243_3_, p_35243_4_, j2, i3);
        } else
        {
            l3 = func_35241_e(k1, p_35243_2_, p_35243_3_, p_35243_4_, j2, i3);
        }
        i1 = l3;
        if(i1 > k)
        {
            field_35245_H[j++] = 0x20820;
        } else
        if(i1 < k)
        {
            if(p_35243_1_ == EnumSkyBlock.Block);
            field_35245_H[j++] = 0x20820 + (k << 18);
            do
            {
                if(i >= j)
                {
                    break;
                }
                int l1 = field_35245_H[i++];
                int k2 = ((l1 & 0x3f) - 32) + p_35243_2_;
                int j3 = ((l1 >> 6 & 0x3f) - 32) + p_35243_3_;
                int i4 = ((l1 >> 12 & 0x3f) - 32) + p_35243_4_;
                int k4 = l1 >> 18 & 0xf;
                int i5 = func_512_a(p_35243_1_, k2, j3, i4);
                if(i5 == k4)
                {
                    func_529_b(p_35243_1_, k2, j3, i4, 0);
                    if(k4 > 0)
                    {
                        int l5 = k2 - p_35243_2_;
                        int j6 = j3 - p_35243_3_;
                        int l6 = i4 - p_35243_4_;
                        if(l5 < 0)
                        {
                            l5 = -l5;
                        }
                        if(j6 < 0)
                        {
                            j6 = -j6;
                        }
                        if(l6 < 0)
                        {
                            l6 = -l6;
                        }
                        if(l5 + j6 + l6 < 17)
                        {
                            int j7 = 0;
                            while(j7 < 6) 
                            {
                                int k7 = (j7 % 2) * 2 - 1;
                                int l7 = k2 + (((j7 / 2) % 3) / 2) * k7;
                                int i8 = j3 + (((j7 / 2 + 1) % 3) / 2) * k7;
                                int j8 = i4 + (((j7 / 2 + 2) % 3) / 2) * k7;
                                int j5 = func_512_a(p_35243_1_, l7, i8, j8);
                                int k8 = Block.field_538_r[func_444_a(l7, i8, j8)];
                                if(k8 == 0)
                                {
                                    k8 = 1;
                                }
                                if(j5 == k4 - k8 && j < field_35245_H.length)
                                {
                                    field_35245_H[j++] = (l7 - p_35243_2_) + 32 + ((i8 - p_35243_3_) + 32 << 6) + ((j8 - p_35243_4_) + 32 << 12) + (k4 - k8 << 18);
                                }
                                j7++;
                            }
                        }
                    }
                }
            } while(true);
            i = 0;
        }
        Profiler.func_40517_a();
        Profiler.func_40518_a("tcp < tcc");
        do
        {
            if(i >= j)
            {
                break;
            }
            int l = field_35245_H[i++];
            int j1 = ((l & 0x3f) - 32) + p_35243_2_;
            int i2 = ((l >> 6 & 0x3f) - 32) + p_35243_3_;
            int l2 = ((l >> 12 & 0x3f) - 32) + p_35243_4_;
            int k3 = func_512_a(p_35243_1_, j1, i2, l2);
            int j4 = func_444_a(j1, i2, l2);
            int l4 = Block.field_538_r[j4];
            if(l4 == 0)
            {
                l4 = 1;
            }
            int k5 = 0;
            if(p_35243_1_ == EnumSkyBlock.Sky)
            {
                k5 = func_35240_d(k3, j1, i2, l2, j4, l4);
            } else
            {
                k5 = func_35241_e(k3, j1, i2, l2, j4, l4);
            }
            if(k5 != k3)
            {
                func_529_b(p_35243_1_, j1, i2, l2, k5);
                if(k5 > k3)
                {
                    int i6 = j1 - p_35243_2_;
                    int k6 = i2 - p_35243_3_;
                    int i7 = l2 - p_35243_4_;
                    if(i6 < 0)
                    {
                        i6 = -i6;
                    }
                    if(k6 < 0)
                    {
                        k6 = -k6;
                    }
                    if(i7 < 0)
                    {
                        i7 = -i7;
                    }
                    if(i6 + k6 + i7 < 17 && j < field_35245_H.length - 6)
                    {
                        if(func_512_a(p_35243_1_, j1 - 1, i2, l2) < k5)
                        {
                            field_35245_H[j++] = (j1 - 1 - p_35243_2_) + 32 + ((i2 - p_35243_3_) + 32 << 6) + ((l2 - p_35243_4_) + 32 << 12);
                        }
                        if(func_512_a(p_35243_1_, j1 + 1, i2, l2) < k5)
                        {
                            field_35245_H[j++] = ((j1 + 1) - p_35243_2_) + 32 + ((i2 - p_35243_3_) + 32 << 6) + ((l2 - p_35243_4_) + 32 << 12);
                        }
                        if(func_512_a(p_35243_1_, j1, i2 - 1, l2) < k5)
                        {
                            field_35245_H[j++] = (j1 - p_35243_2_) + 32 + ((i2 - 1 - p_35243_3_) + 32 << 6) + ((l2 - p_35243_4_) + 32 << 12);
                        }
                        if(func_512_a(p_35243_1_, j1, i2 + 1, l2) < k5)
                        {
                            field_35245_H[j++] = (j1 - p_35243_2_) + 32 + (((i2 + 1) - p_35243_3_) + 32 << 6) + ((l2 - p_35243_4_) + 32 << 12);
                        }
                        if(func_512_a(p_35243_1_, j1, i2, l2 - 1) < k5)
                        {
                            field_35245_H[j++] = (j1 - p_35243_2_) + 32 + ((i2 - p_35243_3_) + 32 << 6) + ((l2 - 1 - p_35243_4_) + 32 << 12);
                        }
                        if(func_512_a(p_35243_1_, j1, i2, l2 + 1) < k5)
                        {
                            field_35245_H[j++] = (j1 - p_35243_2_) + 32 + ((i2 - p_35243_3_) + 32 << 6) + (((l2 + 1) - p_35243_4_) + 32 << 12);
                        }
                    }
                }
            }
        } while(true);
        Profiler.func_40517_a();
    }

    public boolean func_518_a(boolean p_518_1_)
    {
        int i = field_789_A.size();
        if(i != field_788_B.size())
        {
            throw new IllegalStateException("TickNextTick list out of synch");
        }
        if(i > 1000)
        {
            i = 1000;
        }
        for(int j = 0; j < i; j++)
        {
            NextTickListEntry nextticklistentry = (NextTickListEntry)field_789_A.first();
            if(!p_518_1_ && nextticklistentry.field_1021_e > field_797_s.func_22186_f())
            {
                break;
            }
            field_789_A.remove(nextticklistentry);
            field_788_B.remove(nextticklistentry);
            byte byte0 = 8;
            if(!func_466_a(nextticklistentry.field_1018_a - byte0, nextticklistentry.field_1017_b - byte0, nextticklistentry.field_1023_c - byte0, nextticklistentry.field_1018_a + byte0, nextticklistentry.field_1017_b + byte0, nextticklistentry.field_1023_c + byte0))
            {
                continue;
            }
            int k = func_444_a(nextticklistentry.field_1018_a, nextticklistentry.field_1017_b, nextticklistentry.field_1023_c);
            if(k == nextticklistentry.field_1022_d && k > 0)
            {
                Block.field_542_n[k].func_221_a(this, nextticklistentry.field_1018_a, nextticklistentry.field_1017_b, nextticklistentry.field_1023_c, field_803_m);
            }
        }

        return field_789_A.size() != 0;
    }

    public List func_41046_a(Chunk p_41046_1_, boolean p_41046_2_)
    {
        ArrayList arraylist = null;
        ChunkCoordIntPair chunkcoordintpair = p_41046_1_.func_40543_i();
        int i = chunkcoordintpair.field_152_a << 4;
        int j = i + 16;
        int k = chunkcoordintpair.field_151_b << 4;
        int l = k + 16;
        Iterator iterator = field_788_B.iterator();
        do
        {
            if(!iterator.hasNext())
            {
                break;
            }
            NextTickListEntry nextticklistentry = (NextTickListEntry)iterator.next();
            if(nextticklistentry.field_1018_a >= i && nextticklistentry.field_1018_a < j && nextticklistentry.field_1023_c >= k && nextticklistentry.field_1023_c < l)
            {
                if(p_41046_2_)
                {
                    field_789_A.remove(nextticklistentry);
                    iterator.remove();
                }
                if(arraylist == null)
                {
                    arraylist = new ArrayList();
                }
                arraylist.add(nextticklistentry);
            }
        } while(true);
        return arraylist;
    }

    public List func_450_b(Entity p_450_1_, AxisAlignedBB p_450_2_)
    {
        field_778_L.clear();
        int i = MathHelper.func_584_b((p_450_2_.field_964_a - 2D) / 16D);
        int j = MathHelper.func_584_b((p_450_2_.field_969_d + 2D) / 16D);
        int k = MathHelper.func_584_b((p_450_2_.field_970_c - 2D) / 16D);
        int l = MathHelper.func_584_b((p_450_2_.field_967_f + 2D) / 16D);
        for(int i1 = i; i1 <= j; i1++)
        {
            for(int j1 = k; j1 <= l; j1++)
            {
                if(func_447_g(i1, j1))
                {
                    func_525_b(i1, j1).func_329_a(p_450_1_, p_450_2_, field_778_L);
                }
            }

        }

        return field_778_L;
    }

    public List func_457_a(Class p_457_1_, AxisAlignedBB p_457_2_)
    {
        int i = MathHelper.func_584_b((p_457_2_.field_964_a - 2D) / 16D);
        int j = MathHelper.func_584_b((p_457_2_.field_969_d + 2D) / 16D);
        int k = MathHelper.func_584_b((p_457_2_.field_970_c - 2D) / 16D);
        int l = MathHelper.func_584_b((p_457_2_.field_967_f + 2D) / 16D);
        ArrayList arraylist = new ArrayList();
        for(int i1 = i; i1 <= j; i1++)
        {
            for(int j1 = k; j1 <= l; j1++)
            {
                if(func_447_g(i1, j1))
                {
                    func_525_b(i1, j1).func_342_a(p_457_1_, p_457_2_, arraylist);
                }
            }

        }

        return arraylist;
    }

    public Entity func_48085_a(Class p_48085_1_, AxisAlignedBB p_48085_2_, Entity p_48085_3_)
    {
        List list = func_457_a(p_48085_1_, p_48085_2_);
        Entity entity = null;
        double d = 1.7976931348623157E+308D;
        Iterator iterator = list.iterator();
        do
        {
            if(!iterator.hasNext())
            {
                break;
            }
            Entity entity1 = (Entity)iterator.next();
            if(entity1 != p_48085_3_)
            {
                double d1 = p_48085_3_.func_102_b(entity1);
                if(d1 <= d)
                {
                    entity = entity1;
                    d = d1;
                }
            }
        } while(true);
        return entity;
    }

    public void func_515_b(int p_515_1_, int p_515_2_, int p_515_3_, TileEntity p_515_4_)
    {
        if(func_530_e(p_515_1_, p_515_2_, p_515_3_))
        {
            func_492_a(p_515_1_, p_515_3_).func_336_e();
        }
        for(int i = 0; i < field_798_r.size(); i++)
        {
            ((IWorldAccess)field_798_r.get(i)).func_686_a(p_515_1_, p_515_2_, p_515_3_, p_515_4_);
        }

    }

    public int func_487_a(Class p_487_1_)
    {
        int i = 0;
        for(int j = 0; j < field_815_a.size(); j++)
        {
            Entity entity = (Entity)field_815_a.get(j);
            if(p_487_1_.isAssignableFrom(entity.getClass()))
            {
                i++;
            }
        }

        return i;
    }

    public void func_464_a(List p_464_1_)
    {
        field_815_a.addAll(p_464_1_);
        for(int i = 0; i < p_464_1_.size(); i++)
        {
            func_479_b((Entity)p_464_1_.get(i));
        }

    }

    public void func_461_b(List p_461_1_)
    {
        field_790_z.addAll(p_461_1_);
    }

    public boolean func_516_a(int p_516_1_, int p_516_2_, int p_516_3_, int p_516_4_, boolean p_516_5_, int p_516_6_)
    {
        int i = func_444_a(p_516_2_, p_516_3_, p_516_4_);
        Block block = Block.field_542_n[i];
        Block block1 = Block.field_542_n[p_516_1_];
        AxisAlignedBB axisalignedbb = block1.func_248_d(this, p_516_2_, p_516_3_, p_516_4_);
        if(p_516_5_)
        {
            axisalignedbb = null;
        }
        if(axisalignedbb != null && !func_522_a(axisalignedbb))
        {
            return false;
        }
        if(block != null && (block == Block.field_598_B || block == Block.field_596_C || block == Block.field_594_D || block == Block.field_592_E || block == Block.field_599_as || block.field_553_bn.func_27090_g()))
        {
            block = null;
        }
        return p_516_1_ > 0 && block == null && block1.func_28026_e(this, p_516_2_, p_516_3_, p_516_4_, p_516_6_);
    }

    public PathEntity func_48083_a(Entity p_48083_1_, Entity p_48083_2_, float p_48083_3_, boolean p_48083_4_, boolean p_48083_5_, boolean p_48083_6_, boolean p_48083_7_)
    {
        Profiler.func_40518_a("pathfind");
        int i = MathHelper.func_584_b(p_48083_1_.field_322_l);
        int j = MathHelper.func_584_b(p_48083_1_.field_321_m + 1.0D);
        int k = MathHelper.func_584_b(p_48083_1_.field_320_n);
        int l = (int)(p_48083_3_ + 16F);
        int i1 = i - l;
        int j1 = j - l;
        int k1 = k - l;
        int l1 = i + l;
        int i2 = j + l;
        int j2 = k + l;
        ChunkCache chunkcache = new ChunkCache(this, i1, j1, k1, l1, i2, j2);
        PathEntity pathentity = (new PathFinder(chunkcache, p_48083_4_, p_48083_5_, p_48083_6_, p_48083_7_)).func_657_a(p_48083_1_, p_48083_2_, p_48083_3_);
        Profiler.func_40517_a();
        return pathentity;
    }

    public PathEntity func_48088_a(Entity p_48088_1_, int p_48088_2_, int p_48088_3_, int p_48088_4_, float p_48088_5_, boolean p_48088_6_, boolean p_48088_7_, 
            boolean p_48088_8_, boolean p_48088_9_)
    {
        Profiler.func_40518_a("pathfind");
        int i = MathHelper.func_584_b(p_48088_1_.field_322_l);
        int j = MathHelper.func_584_b(p_48088_1_.field_321_m);
        int k = MathHelper.func_584_b(p_48088_1_.field_320_n);
        int l = (int)(p_48088_5_ + 8F);
        int i1 = i - l;
        int j1 = j - l;
        int k1 = k - l;
        int l1 = i + l;
        int i2 = j + l;
        int j2 = k + l;
        ChunkCache chunkcache = new ChunkCache(this, i1, j1, k1, l1, i2, j2);
        PathEntity pathentity = (new PathFinder(chunkcache, p_48088_6_, p_48088_7_, p_48088_8_, p_48088_9_)).func_651_a(p_48088_1_, p_48088_2_, p_48088_3_, p_48088_4_, p_48088_5_);
        Profiler.func_40517_a();
        return pathentity;
    }

    public boolean func_448_i(int p_448_1_, int p_448_2_, int p_448_3_, int p_448_4_)
    {
        int i = func_444_a(p_448_1_, p_448_2_, p_448_3_);
        if(i == 0)
        {
            return false;
        } else
        {
            return Block.field_542_n[i].func_238_d(this, p_448_1_, p_448_2_, p_448_3_, p_448_4_);
        }
    }

    public boolean func_496_m(int p_496_1_, int p_496_2_, int p_496_3_)
    {
        if(func_448_i(p_496_1_, p_496_2_ - 1, p_496_3_, 0))
        {
            return true;
        }
        if(func_448_i(p_496_1_, p_496_2_ + 1, p_496_3_, 1))
        {
            return true;
        }
        if(func_448_i(p_496_1_, p_496_2_, p_496_3_ - 1, 2))
        {
            return true;
        }
        if(func_448_i(p_496_1_, p_496_2_, p_496_3_ + 1, 3))
        {
            return true;
        }
        if(func_448_i(p_496_1_ - 1, p_496_2_, p_496_3_, 4))
        {
            return true;
        }
        return func_448_i(p_496_1_ + 1, p_496_2_, p_496_3_, 5);
    }

    public boolean func_489_j(int p_489_1_, int p_489_2_, int p_489_3_, int p_489_4_)
    {
        if(func_445_d(p_489_1_, p_489_2_, p_489_3_))
        {
            return func_496_m(p_489_1_, p_489_2_, p_489_3_);
        }
        int i = func_444_a(p_489_1_, p_489_2_, p_489_3_);
        if(i == 0)
        {
            return false;
        } else
        {
            return Block.field_542_n[i].func_239_b(this, p_489_1_, p_489_2_, p_489_3_, p_489_4_);
        }
    }

    public boolean func_474_n(int p_474_1_, int p_474_2_, int p_474_3_)
    {
        if(func_489_j(p_474_1_, p_474_2_ - 1, p_474_3_, 0))
        {
            return true;
        }
        if(func_489_j(p_474_1_, p_474_2_ + 1, p_474_3_, 1))
        {
            return true;
        }
        if(func_489_j(p_474_1_, p_474_2_, p_474_3_ - 1, 2))
        {
            return true;
        }
        if(func_489_j(p_474_1_, p_474_2_, p_474_3_ + 1, 3))
        {
            return true;
        }
        if(func_489_j(p_474_1_ - 1, p_474_2_, p_474_3_, 4))
        {
            return true;
        }
        return func_489_j(p_474_1_ + 1, p_474_2_, p_474_3_, 5);
    }

    public EntityPlayer func_472_a(Entity p_472_1_, double p_472_2_)
    {
        return func_456_a(p_472_1_.field_322_l, p_472_1_.field_321_m, p_472_1_.field_320_n, p_472_2_);
    }

    public EntityPlayer func_456_a(double p_456_1_, double p_456_3_, double p_456_5_, double p_456_7_)
    {
        double d = -1D;
        EntityPlayer entityplayer = null;
        for(int i = 0; i < field_805_k.size(); i++)
        {
            EntityPlayer entityplayer1 = (EntityPlayer)field_805_k.get(i);
            double d1 = entityplayer1.func_101_d(p_456_1_, p_456_3_, p_456_5_);
            if((p_456_7_ < 0.0D || d1 < p_456_7_ * p_456_7_) && (d == -1D || d1 < d))
            {
                d = d1;
                entityplayer = entityplayer1;
            }
        }

        return entityplayer;
    }

    public EntityPlayer func_48087_a(double p_48087_1_, double p_48087_3_, double p_48087_5_)
    {
        double d = -1D;
        EntityPlayer entityplayer = null;
        for(int i = 0; i < field_805_k.size(); i++)
        {
            EntityPlayer entityplayer1 = (EntityPlayer)field_805_k.get(i);
            double d1 = entityplayer1.func_101_d(p_48087_1_, entityplayer1.field_321_m, p_48087_3_);
            if((p_48087_5_ < 0.0D || d1 < p_48087_5_ * p_48087_5_) && (d == -1D || d1 < d))
            {
                d = d1;
                entityplayer = entityplayer1;
            }
        }

        return entityplayer;
    }

    public EntityPlayer func_40211_b(Entity p_40211_1_, double p_40211_2_)
    {
        return func_40219_b(p_40211_1_.field_322_l, p_40211_1_.field_321_m, p_40211_1_.field_320_n, p_40211_2_);
    }

    public EntityPlayer func_40219_b(double p_40219_1_, double p_40219_3_, double p_40219_5_, double p_40219_7_)
    {
        double d = -1D;
        EntityPlayer entityplayer = null;
        for(int i = 0; i < field_805_k.size(); i++)
        {
            EntityPlayer entityplayer1 = (EntityPlayer)field_805_k.get(i);
            if(entityplayer1.field_35214_K.field_35660_a)
            {
                continue;
            }
            double d1 = entityplayer1.func_101_d(p_40219_1_, p_40219_3_, p_40219_5_);
            if((p_40219_7_ < 0.0D || d1 < p_40219_7_ * p_40219_7_) && (d == -1D || d1 < d))
            {
                d = d1;
                entityplayer = entityplayer1;
            }
        }

        return entityplayer;
    }

    public EntityPlayer func_25071_a(String p_25071_1_)
    {
        for(int i = 0; i < field_805_k.size(); i++)
        {
            if(p_25071_1_.equals(((EntityPlayer)field_805_k.get(i)).field_409_aq))
            {
                return (EntityPlayer)field_805_k.get(i);
            }
        }

        return null;
    }

    public void func_476_g()
    {
        field_9211_s.func_22091_b();
    }

    public void func_22076_a(long p_22076_1_)
    {
        field_797_s.func_22180_a(p_22076_1_);
    }

    public void func_32005_b(long p_32005_1_)
    {
        long l = p_32005_1_ - field_797_s.func_22186_f();
        for(Iterator iterator = field_788_B.iterator(); iterator.hasNext();)
        {
            NextTickListEntry nextticklistentry = (NextTickListEntry)iterator.next();
            nextticklistentry.field_1021_e += l;
        }

        func_22076_a(p_32005_1_);
    }

    public long func_22079_j()
    {
        return field_797_s.func_22187_b();
    }

    public long func_22080_k()
    {
        return field_797_s.func_22186_f();
    }

    public ChunkCoordinates func_22078_l()
    {
        return new ChunkCoordinates(field_797_s.func_22184_c(), field_797_s.func_22179_d(), field_797_s.func_22189_e());
    }

    public boolean func_6157_a(EntityPlayer p_6157_1_, int p_6157_2_, int p_6157_3_, int i)
    {
        return true;
    }

    public void func_9206_a(Entity entity, byte byte0)
    {
    }

    public IChunkProvider func_25073_n()
    {
        return field_783_G;
    }

    public void func_21117_c(int p_21117_1_, int p_21117_2_, int p_21117_3_, int p_21117_4_, int p_21117_5_)
    {
        int i = func_444_a(p_21117_1_, p_21117_2_, p_21117_3_);
        if(i > 0)
        {
            Block.field_542_n[i].func_21024_a(this, p_21117_1_, p_21117_2_, p_21117_3_, p_21117_4_, p_21117_5_);
        }
    }

    public ISaveHandler func_22075_m()
    {
        return field_9211_s;
    }

    public WorldInfo func_22081_n()
    {
        return field_797_s;
    }

    public void func_22082_o()
    {
        field_22087_A = !field_805_k.isEmpty();
        Iterator iterator = field_805_k.iterator();
        do
        {
            if(!iterator.hasNext())
            {
                break;
            }
            EntityPlayer entityplayer = (EntityPlayer)iterator.next();
            if(entityplayer.func_22057_E())
            {
                continue;
            }
            field_22087_A = false;
            break;
        } while(true);
    }

    protected void func_22083_p()
    {
        field_22087_A = false;
        Iterator iterator = field_805_k.iterator();
        do
        {
            if(!iterator.hasNext())
            {
                break;
            }
            EntityPlayer entityplayer = (EntityPlayer)iterator.next();
            if(entityplayer.func_22057_E())
            {
                entityplayer.func_22062_a(false, false, true);
            }
        } while(true);
        func_27071_y();
    }

    public boolean func_22084_q()
    {
        if(field_22087_A && !field_792_x)
        {
            for(Iterator iterator = field_805_k.iterator(); iterator.hasNext();)
            {
                EntityPlayer entityplayer = (EntityPlayer)iterator.next();
                if(!entityplayer.func_22065_F())
                {
                    return false;
                }
            }

            return true;
        } else
        {
            return false;
        }
    }

    public float func_27065_c(float p_27065_1_)
    {
        return (field_27077_D + (field_27076_E - field_27077_D) * p_27065_1_) * func_27074_d(p_27065_1_);
    }

    public float func_27074_d(float p_27074_1_)
    {
        return field_27079_B + (field_27078_C - field_27079_B) * p_27074_1_;
    }

    public boolean func_27067_u()
    {
        return (double)func_27065_c(1.0F) > 0.90000000000000002D;
    }

    public boolean func_27068_v()
    {
        return (double)func_27074_d(1.0F) > 0.20000000000000001D;
    }

    public boolean func_27072_q(int p_27072_1_, int p_27072_2_, int p_27072_3_)
    {
        if(!func_27068_v())
        {
            return false;
        }
        if(!func_497_g(p_27072_1_, p_27072_2_, p_27072_3_))
        {
            return false;
        }
        if(func_28100_e(p_27072_1_, p_27072_3_) > p_27072_2_)
        {
            return false;
        }
        BiomeGenBase biomegenbase = func_48091_a(p_27072_1_, p_27072_3_);
        if(biomegenbase.func_27049_c())
        {
            return false;
        } else
        {
            return biomegenbase.func_27048_d();
        }
    }

    public boolean func_48089_z(int p_48089_1_, int p_48089_2_, int p_48089_3_)
    {
        BiomeGenBase biomegenbase = func_48091_a(p_48089_1_, p_48089_3_);
        return biomegenbase.func_48441_d();
    }

    public void func_28102_a(String p_28102_1_, WorldSavedData p_28102_2_)
    {
        field_28105_z.func_28177_a(p_28102_1_, p_28102_2_);
    }

    public WorldSavedData func_28103_a(Class p_28103_1_, String p_28103_2_)
    {
        return field_28105_z.func_28178_a(p_28103_1_, p_28103_2_);
    }

    public int func_28104_b(String p_28104_1_)
    {
        return field_28105_z.func_28173_a(p_28104_1_);
    }

    public void func_28097_e(int p_28097_1_, int p_28097_2_, int p_28097_3_, int p_28097_4_, int p_28097_5_)
    {
        func_28101_a(null, p_28097_1_, p_28097_2_, p_28097_3_, p_28097_4_, p_28097_5_);
    }

    public void func_28101_a(EntityPlayer p_28101_1_, int p_28101_2_, int p_28101_3_, int p_28101_4_, int p_28101_5_, int p_28101_6_)
    {
        for(int i = 0; i < field_798_r.size(); i++)
        {
            ((IWorldAccess)field_798_r.get(i)).func_28133_a(p_28101_1_, p_28101_2_, p_28101_3_, p_28101_4_, p_28101_5_, p_28101_6_);
        }

    }

    public int func_48095_y()
    {
        return 256;
    }

    public Random func_35238_t(int p_35238_1_, int p_35238_2_, int p_35238_3_)
    {
        long l = (long)p_35238_1_ * 0x4f9939f508L + (long)p_35238_2_ * 0x1ef1565bd5L + func_22081_n().func_22187_b() + (long)p_35238_3_;
        field_803_m.setSeed(l);
        return field_803_m;
    }

    public boolean func_6156_d()
    {
        return false;
    }

    public SpawnListEntry func_40216_a(EnumCreatureType p_40216_1_, int p_40216_2_, int p_40216_3_, int p_40216_4_)
    {
        List list = func_25073_n().func_40181_a(p_40216_1_, p_40216_2_, p_40216_3_, p_40216_4_);
        if(list == null || list.isEmpty())
        {
            return null;
        } else
        {
            return (SpawnListEntry)WeightedRandom.func_35689_a(field_803_m, list);
        }
    }

    public ChunkPosition func_40214_b(String p_40214_1_, int p_40214_2_, int p_40214_3_, int p_40214_4_)
    {
        return func_25073_n().func_40182_a(this, p_40214_1_, p_40214_2_, p_40214_3_, p_40214_4_);
    }
}

