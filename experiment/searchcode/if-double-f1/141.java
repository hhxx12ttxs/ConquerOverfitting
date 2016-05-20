// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst 

package net.minecraft.src;

import java.util.*;

// Referenced classes of package net.minecraft.src:
//            EntityLiving, InventoryPlayer, FoodStats, PlayerCapabilities, 
//            ContainerPlayer, World, ChunkCoordinates, DataWatcher, 
//            ItemStack, Item, EnumAction, Container, 
//            StatList, Vec3D, Potion, PotionEffect, 
//            MathHelper, AxisAlignedBB, Entity, EnchantmentHelper, 
//            EntityItem, Material, NBTTagCompound, NBTTagList, 
//            DamageSource, EntityMob, EntityArrow, EntityCreeper, 
//            EntityGhast, EntityWolf, AchievementList, EnumStatus, 
//            WorldProvider, BlockBed, Block, IChunkProvider, 
//            EntityMinecart, EntityBoat, EntityPig, EntityFishHook, 
//            IInventory, TileEntityFurnace, TileEntityDispenser, TileEntitySign, 
//            TileEntityBrewingStand, StatBase

public abstract class EntityPlayer extends EntityLiving
{

    public InventoryPlayer field_416_aj;
    public Container field_20053_ao;
    public Container field_20052_ap;
    protected FoodStats field_35217_m;
    protected int field_35216_n;
    public byte field_9152_am;
    public int field_9151_an;
    public float field_9150_ao;
    public float field_9149_ap;
    public boolean field_9148_aq;
    public int field_9147_ar;
    public String field_409_aq;
    public int field_4110_as;
    public int field_35218_w;
    public double field_20047_ay;
    public double field_20046_az;
    public double field_20051_aA;
    public double field_20050_aB;
    public double field_20049_aC;
    public double field_20048_aD;
    protected boolean field_21900_a;
    public ChunkCoordinates field_21901_b;
    private int field_21902_c;
    public float field_22066_z;
    public float field_22067_A;
    private ChunkCoordinates field_24900_d;
    private ChunkCoordinates field_27995_d;
    public int field_28016_C;
    protected boolean field_28015_D;
    public float field_28014_E;
    public PlayerCapabilities field_35214_K;
    public int field_35211_M;
    public int field_35212_N;
    public float field_35210_L;
    private ItemStack field_34908_d;
    private int field_34909_e;
    protected float field_35213_O;
    protected float field_35215_P;
    public EntityFishHook field_6124_at;

    public EntityPlayer(World p_i173_1_)
    {
        super(p_i173_1_);
        field_416_aj = new InventoryPlayer(this);
        field_35217_m = new FoodStats();
        field_35216_n = 0;
        field_9152_am = 0;
        field_9151_an = 0;
        field_9148_aq = false;
        field_9147_ar = 0;
        field_35218_w = 0;
        field_28016_C = 20;
        field_28015_D = false;
        field_35214_K = new PlayerCapabilities();
        field_35213_O = 0.1F;
        field_35215_P = 0.02F;
        field_6124_at = null;
        field_20053_ao = new ContainerPlayer(field_416_aj, !p_i173_1_.field_792_x);
        field_20052_ap = field_20053_ao;
        field_9076_H = 1.62F;
        ChunkCoordinates chunkcoordinates = p_i173_1_.func_22078_l();
        func_107_c((double)chunkcoordinates.field_22216_a + 0.5D, chunkcoordinates.field_529_a + 1, (double)chunkcoordinates.field_528_b + 0.5D, 0.0F, 0.0F);
        field_9116_aJ = "humanoid";
        field_9117_aI = 180F;
        field_9062_Y = 20;
        field_9119_aG = "/mob/char.png";
    }

    public int func_40095_c()
    {
        return 20;
    }

    protected void func_21044_a()
    {
        super.func_21044_a();
        field_21045_af.func_21153_a(16, Byte.valueOf((byte)0));
        field_21045_af.func_21153_a(17, Byte.valueOf((byte)0));
    }

    public boolean func_35209_o_()
    {
        return field_34908_d != null;
    }

    public void func_35196_E()
    {
        if(field_34908_d != null)
        {
            field_34908_d.func_35613_a(field_9093_l, this, field_34909_e);
        }
        func_35205_F();
    }

    public void func_35205_F()
    {
        field_34908_d = null;
        field_34909_e = 0;
        if(!field_9093_l.field_792_x)
        {
            func_35148_h(false);
        }
    }

    public boolean func_35180_G()
    {
        return func_35209_o_() && Item.field_176_c[field_34908_d.field_855_c].func_35406_b(field_34908_d) == EnumAction.block;
    }

    public void func_106_b_()
    {
        if(field_34908_d != null)
        {
            ItemStack itemstack = field_416_aj.func_213_b();
            if(itemstack != field_34908_d)
            {
                func_35205_F();
            } else
            {
                if(field_34909_e <= 25 && field_34909_e % 4 == 0)
                {
                    func_35208_b(itemstack, 5);
                }
                if(--field_34909_e == 0 && !field_9093_l.field_792_x)
                {
                    func_35199_C();
                }
            }
        }
        if(field_35218_w > 0)
        {
            field_35218_w--;
        }
        if(func_22057_E())
        {
            field_21902_c++;
            if(field_21902_c > 100)
            {
                field_21902_c = 100;
            }
            if(!field_9093_l.field_792_x)
            {
                if(!func_22063_l())
                {
                    func_22062_a(true, true, false);
                } else
                if(field_9093_l.func_453_a())
                {
                    func_22062_a(false, true, true);
                }
            }
        } else
        if(field_21902_c > 0)
        {
            field_21902_c++;
            if(field_21902_c >= 110)
            {
                field_21902_c = 0;
            }
        }
        super.func_106_b_();
        if(!field_9093_l.field_792_x && field_20052_ap != null && !field_20052_ap.func_20126_b(this))
        {
            func_20043_I();
            field_20052_ap = field_20053_ao;
        }
        if(field_35214_K.field_35658_b)
        {
            for(int i = 0; i < 8; i++) { }
        }
        if(func_40035_z() && field_35214_K.field_35660_a)
        {
            func_40036_aw();
        }
        field_20047_ay = field_20050_aB;
        field_20046_az = field_20049_aC;
        field_20051_aA = field_20048_aD;
        double d = field_322_l - field_20050_aB;
        double d1 = field_321_m - field_20049_aC;
        double d2 = field_320_n - field_20048_aD;
        double d3 = 10D;
        if(d > d3)
        {
            field_20047_ay = field_20050_aB = field_322_l;
        }
        if(d2 > d3)
        {
            field_20051_aA = field_20048_aD = field_320_n;
        }
        if(d1 > d3)
        {
            field_20046_az = field_20049_aC = field_321_m;
        }
        if(d < -d3)
        {
            field_20047_ay = field_20050_aB = field_322_l;
        }
        if(d2 < -d3)
        {
            field_20051_aA = field_20048_aD = field_320_n;
        }
        if(d1 < -d3)
        {
            field_20046_az = field_20049_aC = field_321_m;
        }
        field_20050_aB += d * 0.25D;
        field_20048_aD += d2 * 0.25D;
        field_20049_aC += d1 * 0.25D;
        func_25046_a(StatList.field_25114_j, 1);
        if(field_327_g == null)
        {
            field_27995_d = null;
        }
        if(!field_9093_l.field_792_x)
        {
            field_35217_m.func_35584_a(this);
        }
    }

    protected void func_35208_b(ItemStack p_35208_1_, int p_35208_2_)
    {
        if(p_35208_1_.func_35615_m() == EnumAction.drink)
        {
            field_9093_l.func_506_a(this, "random.drink", 0.5F, field_9093_l.field_803_m.nextFloat() * 0.1F + 0.9F);
        }
        if(p_35208_1_.func_35615_m() == EnumAction.eat)
        {
            for(int i = 0; i < p_35208_2_; i++)
            {
                Vec3D vec3d = Vec3D.func_768_b(((double)field_9064_W.nextFloat() - 0.5D) * 0.10000000000000001D, Math.random() * 0.10000000000000001D + 0.10000000000000001D, 0.0D);
                vec3d.func_35571_a((-field_315_s * 3.141593F) / 180F);
                vec3d.func_35572_b((-field_316_r * 3.141593F) / 180F);
                Vec3D vec3d1 = Vec3D.func_768_b(((double)field_9064_W.nextFloat() - 0.5D) * 0.29999999999999999D, (double)(-field_9064_W.nextFloat()) * 0.59999999999999998D - 0.29999999999999999D, 0.59999999999999998D);
                vec3d1.func_35571_a((-field_315_s * 3.141593F) / 180F);
                vec3d1.func_35572_b((-field_316_r * 3.141593F) / 180F);
                vec3d1 = vec3d1.func_757_c(field_322_l, field_321_m + (double)func_104_p(), field_320_n);
                field_9093_l.func_514_a((new StringBuilder()).append("iconcrack_").append(p_35208_1_.func_569_a().field_234_aS).toString(), vec3d1.field_1055_a, vec3d1.field_1054_b, vec3d1.field_1058_c, vec3d.field_1055_a, vec3d.field_1054_b + 0.050000000000000003D, vec3d.field_1058_c);
            }

            field_9093_l.func_506_a(this, "random.eat", 0.5F + 0.5F * (float)field_9064_W.nextInt(2), (field_9064_W.nextFloat() - field_9064_W.nextFloat()) * 0.2F + 1.0F);
        }
    }

    protected void func_35199_C()
    {
        if(field_34908_d != null)
        {
            func_35208_b(field_34908_d, 16);
            int i = field_34908_d.field_853_a;
            ItemStack itemstack = field_34908_d.func_35617_b(field_9093_l, this);
            if(itemstack != field_34908_d || itemstack != null && itemstack.field_853_a != i)
            {
                field_416_aj.field_496_a[field_416_aj.field_499_d] = itemstack;
                if(itemstack.field_853_a == 0)
                {
                    field_416_aj.field_496_a[field_416_aj.field_499_d] = null;
                }
            }
            func_35205_F();
        }
    }

    protected boolean func_22058_w()
    {
        return func_40097_ai() <= 0 || func_22057_E();
    }

    protected void func_20043_I()
    {
        field_20052_ap = field_20053_ao;
    }

    public void func_115_v()
    {
        double d = field_322_l;
        double d1 = field_321_m;
        double d2 = field_320_n;
        super.func_115_v();
        field_9150_ao = field_9149_ap;
        field_9149_ap = 0.0F;
        func_27015_h(field_322_l - d, field_321_m - d1, field_320_n - d2);
    }

    private int func_35204_o()
    {
        if(func_35184_a(Potion.field_35452_e))
        {
            return 6 - (1 + func_35187_b(Potion.field_35452_e).func_35652_c()) * 1;
        }
        if(func_35184_a(Potion.field_35449_f))
        {
            return 6 + (1 + func_35187_b(Potion.field_35449_f).func_35652_c()) * 2;
        } else
        {
            return 6;
        }
    }

    protected void func_152_d_()
    {
        int i = func_35204_o();
        if(field_9148_aq)
        {
            field_9147_ar++;
            if(field_9147_ar >= i)
            {
                field_9147_ar = 0;
                field_9148_aq = false;
            }
        } else
        {
            field_9147_ar = 0;
        }
        field_9110_aP = (float)field_9147_ar / (float)i;
    }

    public void func_153_y()
    {
        if(field_35216_n > 0)
        {
            field_35216_n--;
        }
        if(field_9093_l.field_804_l == 0 && func_40097_ai() < func_40095_c() && (field_9063_X % 20) * 12 == 0)
        {
            func_137_a(1);
        }
        field_416_aj.func_210_c();
        field_9150_ao = field_9149_ap;
        super.func_153_y();
        field_35194_aj = field_35213_O;
        field_35193_ak = field_35215_P;
        if(func_35149_at())
        {
            field_35194_aj += (double)field_35213_O * 0.29999999999999999D;
            field_35193_ak += (double)field_35215_P * 0.29999999999999999D;
        }
        float f = MathHelper.func_583_a(field_319_o * field_319_o + field_317_q * field_317_q);
        float f1 = (float)Math.atan(-field_318_p * 0.20000000298023224D) * 15F;
        if(f > 0.1F)
        {
            f = 0.1F;
        }
        if(!field_9086_A || func_40097_ai() <= 0)
        {
            f = 0.0F;
        }
        if(field_9086_A || func_40097_ai() <= 0)
        {
            f1 = 0.0F;
        }
        field_9149_ap += (f - field_9149_ap) * 0.4F;
        field_9101_aY += (f1 - field_9101_aY) * 0.8F;
        if(func_40097_ai() > 0)
        {
            List list = field_9093_l.func_450_b(this, field_312_v.func_708_b(1.0D, 0.0D, 1.0D));
            if(list != null)
            {
                for(int i = 0; i < list.size(); i++)
                {
                    Entity entity = (Entity)list.get(i);
                    if(!entity.field_304_B)
                    {
                        func_171_h(entity);
                    }
                }

            }
        }
    }

    private void func_171_h(Entity p_171_1_)
    {
        p_171_1_.func_6093_b(this);
    }

    public void func_142_f(DamageSource p_142_1_)
    {
        super.func_142_f(p_142_1_);
        func_113_a(0.2F, 0.2F);
        func_86_a(field_322_l, field_321_m, field_320_n);
        field_318_p = 0.10000000149011612D;
        if(field_409_aq.equals("Notch"))
        {
            func_48349_a(new ItemStack(Item.field_171_h, 1), true);
        }
        field_416_aj.func_199_f();
        if(p_142_1_ != null)
        {
            field_319_o = -MathHelper.func_582_b(((field_9105_aU + field_316_r) * 3.141593F) / 180F) * 0.1F;
            field_317_q = -MathHelper.func_585_a(((field_9105_aU + field_316_r) * 3.141593F) / 180F) * 0.1F;
        } else
        {
            field_319_o = field_317_q = 0.0D;
        }
        field_9076_H = 0.1F;
        func_25046_a(StatList.field_25098_u, 1);
    }

    public void func_96_b(Entity p_96_1_, int p_96_2_)
    {
        field_9151_an += p_96_2_;
        if(p_96_1_ instanceof EntityPlayer)
        {
            func_25046_a(StatList.field_25096_w, 1);
        } else
        {
            func_25046_a(StatList.field_25097_v, 1);
        }
    }

    protected int func_40094_f(int p_40094_1_)
    {
        int i = EnchantmentHelper.func_40628_a(field_416_aj);
        if(i > 0 && field_9064_W.nextInt(i + 1) > 0)
        {
            return p_40094_1_;
        } else
        {
            return super.func_40094_f(p_40094_1_);
        }
    }

    public EntityItem func_48347_R()
    {
        return func_48349_a(field_416_aj.func_20069_a(field_416_aj.field_499_d, 1), false);
    }

    public EntityItem func_48348_b(ItemStack p_48348_1_)
    {
        return func_48349_a(p_48348_1_, false);
    }

    public EntityItem func_48349_a(ItemStack p_48349_1_, boolean p_48349_2_)
    {
        if(p_48349_1_ == null)
        {
            return null;
        }
        EntityItem entityitem = new EntityItem(field_9093_l, field_322_l, (field_321_m - 0.30000001192092896D) + (double)func_104_p(), field_320_n, p_48349_1_);
        entityitem.field_433_ad = 40;
        float f = 0.1F;
        if(p_48349_2_)
        {
            float f2 = field_9064_W.nextFloat() * 0.5F;
            float f4 = field_9064_W.nextFloat() * 3.141593F * 2.0F;
            entityitem.field_319_o = -MathHelper.func_585_a(f4) * f2;
            entityitem.field_317_q = MathHelper.func_582_b(f4) * f2;
            entityitem.field_318_p = 0.20000000298023224D;
        } else
        {
            float f1 = 0.3F;
            entityitem.field_319_o = -MathHelper.func_585_a((field_316_r / 180F) * 3.141593F) * MathHelper.func_582_b((field_315_s / 180F) * 3.141593F) * f1;
            entityitem.field_317_q = MathHelper.func_582_b((field_316_r / 180F) * 3.141593F) * MathHelper.func_582_b((field_315_s / 180F) * 3.141593F) * f1;
            entityitem.field_318_p = -MathHelper.func_585_a((field_315_s / 180F) * 3.141593F) * f1 + 0.1F;
            f1 = 0.02F;
            float f3 = field_9064_W.nextFloat() * 3.141593F * 2.0F;
            f1 *= field_9064_W.nextFloat();
            entityitem.field_319_o += Math.cos(f3) * (double)f1;
            entityitem.field_318_p += (field_9064_W.nextFloat() - field_9064_W.nextFloat()) * 0.1F;
            entityitem.field_317_q += Math.sin(f3) * (double)f1;
        }
        func_162_a(entityitem);
        func_25046_a(StatList.field_25103_r, 1);
        return entityitem;
    }

    protected void func_162_a(EntityItem p_162_1_)
    {
        field_9093_l.func_526_a(p_162_1_);
    }

    public float func_165_a(Block p_165_1_)
    {
        float f = field_416_aj.func_208_a(p_165_1_);
        float f1 = f;
        int i = EnchantmentHelper.func_40630_b(field_416_aj);
        if(i > 0 && field_416_aj.func_207_b(p_165_1_))
        {
            f1 += i * i + 1;
        }
        if(func_35184_a(Potion.field_35452_e))
        {
            f1 *= 1.0F + (float)(func_35187_b(Potion.field_35452_e).func_35652_c() + 1) * 0.2F;
        }
        if(func_35184_a(Potion.field_35449_f))
        {
            f1 *= 1.0F - (float)(func_35187_b(Potion.field_35449_f).func_35652_c() + 1) * 0.2F;
        }
        if(func_110_a(Material.field_521_f) && !EnchantmentHelper.func_40632_g(field_416_aj))
        {
            f1 /= 5F;
        }
        if(!field_9086_A)
        {
            f1 /= 5F;
        }
        return f1;
    }

    public boolean func_167_b(Block p_167_1_)
    {
        return field_416_aj.func_207_b(p_167_1_);
    }

    public void func_99_b(NBTTagCompound p_99_1_)
    {
        super.func_99_b(p_99_1_);
        NBTTagList nbttaglist = p_99_1_.func_407_k("Inventory");
        field_416_aj.func_203_b(nbttaglist);
        field_4110_as = p_99_1_.func_395_d("Dimension");
        field_21900_a = p_99_1_.func_402_l("Sleeping");
        field_21902_c = p_99_1_.func_406_c("SleepTimer");
        field_35210_L = p_99_1_.func_389_f("XpP");
        field_35211_M = p_99_1_.func_395_d("XpLevel");
        field_35212_N = p_99_1_.func_395_d("XpTotal");
        if(field_21900_a)
        {
            field_21901_b = new ChunkCoordinates(MathHelper.func_584_b(field_322_l), MathHelper.func_584_b(field_321_m), MathHelper.func_584_b(field_320_n));
            func_22062_a(true, true, false);
        }
        if(p_99_1_.func_410_a("SpawnX") && p_99_1_.func_410_a("SpawnY") && p_99_1_.func_410_a("SpawnZ"))
        {
            field_24900_d = new ChunkCoordinates(p_99_1_.func_395_d("SpawnX"), p_99_1_.func_395_d("SpawnY"), p_99_1_.func_395_d("SpawnZ"));
        }
        field_35217_m.func_35589_a(p_99_1_);
        field_35214_K.func_40620_b(p_99_1_);
    }

    public void func_97_a(NBTTagCompound p_97_1_)
    {
        super.func_97_a(p_97_1_);
        p_97_1_.func_399_a("Inventory", field_416_aj.func_200_a(new NBTTagList()));
        p_97_1_.func_405_a("Dimension", field_4110_as);
        p_97_1_.func_393_a("Sleeping", field_21900_a);
        p_97_1_.func_394_a("SleepTimer", (short)field_21902_c);
        p_97_1_.func_390_a("XpP", field_35210_L);
        p_97_1_.func_405_a("XpLevel", field_35211_M);
        p_97_1_.func_405_a("XpTotal", field_35212_N);
        if(field_24900_d != null)
        {
            p_97_1_.func_405_a("SpawnX", field_24900_d.field_22216_a);
            p_97_1_.func_405_a("SpawnY", field_24900_d.field_529_a);
            p_97_1_.func_405_a("SpawnZ", field_24900_d.field_528_b);
        }
        field_35217_m.func_35588_b(p_97_1_);
        field_35214_K.func_40621_a(p_97_1_);
    }

    public void func_166_a(IInventory iinventory)
    {
    }

    public void func_40106_c(int i, int j, int k)
    {
    }

    public void func_174_A(int i, int j, int k)
    {
    }

    public void func_163_c(Entity entity, int i)
    {
    }

    public float func_104_p()
    {
        return 0.12F;
    }

    protected void func_22064_l_()
    {
        field_9076_H = 1.62F;
    }

    public boolean func_121_a(DamageSource p_121_1_, int p_121_2_)
    {
        if(field_35214_K.field_35660_a && !p_121_1_.func_35077_d())
        {
            return false;
        }
        field_9132_bn = 0;
        if(func_40097_ai() <= 0)
        {
            return false;
        }
        if(func_22057_E() && !field_9093_l.field_792_x)
        {
            func_22062_a(true, true, false);
        }
        Entity entity = p_121_1_.func_35080_a();
        if((entity instanceof EntityMob) || (entity instanceof EntityArrow))
        {
            if(field_9093_l.field_804_l == 0)
            {
                p_121_2_ = 0;
            }
            if(field_9093_l.field_804_l == 1)
            {
                p_121_2_ = p_121_2_ / 2 + 1;
            }
            if(field_9093_l.field_804_l == 3)
            {
                p_121_2_ = (p_121_2_ * 3) / 2;
            }
        }
        if(p_121_2_ == 0)
        {
            return false;
        }
        Entity entity1 = entity;
        if((entity1 instanceof EntityArrow) && ((EntityArrow)entity1).field_439_ah != null)
        {
            entity1 = ((EntityArrow)entity1).field_439_ah;
        }
        if(entity1 instanceof EntityLiving)
        {
            func_25047_a((EntityLiving)entity1, false);
        }
        func_25046_a(StatList.field_25100_t, p_121_2_);
        return super.func_121_a(p_121_1_, p_121_2_);
    }

    protected int func_40099_b(DamageSource p_40099_1_, int p_40099_2_)
    {
        int i = super.func_40099_b(p_40099_1_, p_40099_2_);
        if(i <= 0)
        {
            return 0;
        }
        int j = EnchantmentHelper.func_40634_a(field_416_aj, p_40099_1_);
        if(j > 20)
        {
            j = 20;
        }
        if(j > 0 && j <= 20)
        {
            int k = 25 - j;
            int l = i * k + field_40105_ap;
            i = l / 25;
            field_40105_ap = l % 25;
        }
        return i;
    }

    protected boolean func_27016_t()
    {
        return false;
    }

    protected void func_25047_a(EntityLiving p_25047_1_, boolean p_25047_2_)
    {
        if((p_25047_1_ instanceof EntityCreeper) || (p_25047_1_ instanceof EntityGhast))
        {
            return;
        }
        if(p_25047_1_ instanceof EntityWolf)
        {
            EntityWolf entitywolf = (EntityWolf)p_25047_1_;
            if(entitywolf.func_48373_u_() && field_409_aq.equals(entitywolf.func_48367_A()))
            {
                return;
            }
        }
        if((p_25047_1_ instanceof EntityPlayer) && !func_27016_t())
        {
            return;
        }
        List list = field_9093_l.func_457_a(net.minecraft.src.EntityWolf.class, AxisAlignedBB.func_693_b(field_322_l, field_321_m, field_320_n, field_322_l + 1.0D, field_321_m + 1.0D, field_320_n + 1.0D).func_708_b(16D, 4D, 16D));
        Iterator iterator = list.iterator();
        do
        {
            if(!iterator.hasNext())
            {
                break;
            }
            Entity entity = (Entity)iterator.next();
            EntityWolf entitywolf1 = (EntityWolf)entity;
            if(entitywolf1.func_48373_u_() && entitywolf1.func_25024_A() == null && field_409_aq.equals(entitywolf1.func_48367_A()) && (!p_25047_2_ || !entitywolf1.func_48371_v_()))
            {
                entitywolf1.func_48369_c(false);
                entitywolf1.func_25025_c(p_25047_1_);
            }
        } while(true);
    }

    protected void func_40101_g(int p_40101_1_)
    {
        field_416_aj.func_211_b(p_40101_1_);
    }

    public int func_40092_O()
    {
        return field_416_aj.func_212_e();
    }

    protected void func_6099_c(DamageSource p_6099_1_, int p_6099_2_)
    {
        if(!p_6099_1_.func_35083_b() && func_35180_G())
        {
            p_6099_2_ = 1 + p_6099_2_ >> 1;
        }
        p_6099_2_ = func_40091_d(p_6099_1_, p_6099_2_);
        p_6099_2_ = func_40099_b(p_6099_1_, p_6099_2_);
        func_35198_b(p_6099_1_.func_35074_c());
        field_9109_aQ -= p_6099_2_;
    }

    public void func_170_a(TileEntityFurnace tileentityfurnace)
    {
    }

    public void func_21072_a(TileEntityDispenser tileentitydispenser)
    {
    }

    public void func_4048_a(TileEntitySign tileentitysign)
    {
    }

    public void func_40110_a(TileEntityBrewingStand tileentitybrewingstand)
    {
    }

    public void func_9145_g(Entity p_9145_1_)
    {
        if(p_9145_1_.func_6092_a(this))
        {
            return;
        }
        ItemStack itemstack = func_172_B();
        if(itemstack != null && (p_9145_1_ instanceof EntityLiving))
        {
            itemstack.func_21129_b((EntityLiving)p_9145_1_);
            if(itemstack.field_853_a <= 0)
            {
                itemstack.func_577_a(this);
                func_164_C();
            }
        }
    }

    public ItemStack func_172_B()
    {
        return field_416_aj.func_213_b();
    }

    public void func_164_C()
    {
        field_416_aj.func_206_a(field_416_aj.field_499_d, null);
    }

    public double func_117_x()
    {
        return (double)(field_9076_H - 0.5F);
    }

    public void func_168_z()
    {
        if(!field_9148_aq || field_9147_ar >= func_35204_o() / 2 || field_9147_ar < 0)
        {
            field_9147_ar = -1;
            field_9148_aq = true;
        }
    }

    public void func_9146_h(Entity p_9146_1_)
    {
        if(!p_9146_1_.func_48313_k_())
        {
            return;
        }
        int i = field_416_aj.func_9157_a(p_9146_1_);
        if(func_35184_a(Potion.field_35450_g))
        {
            i += 3 << func_35187_b(Potion.field_35450_g).func_35652_c();
        }
        if(func_35184_a(Potion.field_35467_t))
        {
            i -= 2 << func_35187_b(Potion.field_35467_t).func_35652_c();
        }
        int j = 0;
        int k = 0;
        if(p_9146_1_ instanceof EntityLiving)
        {
            k = EnchantmentHelper.func_40641_a(field_416_aj, (EntityLiving)p_9146_1_);
            j += EnchantmentHelper.func_40637_b(field_416_aj, (EntityLiving)p_9146_1_);
        }
        if(func_35149_at())
        {
            j++;
        }
        if(i > 0 || k > 0)
        {
            boolean flag = field_9072_N > 0.0F && !field_9086_A && !func_144_E() && !func_27011_Z() && !func_35184_a(Potion.field_35470_q) && field_327_g == null && (p_9146_1_ instanceof EntityLiving);
            if(flag)
            {
                i += field_9064_W.nextInt(i / 2 + 2);
            }
            i += k;
            boolean flag1 = p_9146_1_.func_121_a(DamageSource.func_35076_b(this), i);
            if(flag1)
            {
                if(j > 0)
                {
                    p_9146_1_.func_87_f(-MathHelper.func_585_a((field_316_r * 3.141593F) / 180F) * (float)j * 0.5F, 0.10000000000000001D, MathHelper.func_582_b((field_316_r * 3.141593F) / 180F) * (float)j * 0.5F);
                    field_319_o *= 0.59999999999999998D;
                    field_317_q *= 0.59999999999999998D;
                    func_35146_g(false);
                }
                if(flag)
                {
                    func_35202_e(p_9146_1_);
                }
                if(k > 0)
                {
                    func_40109_c(p_9146_1_);
                }
                if(i >= 18)
                {
                    func_27017_a(AchievementList.field_40473_E);
                }
                func_48335_g(p_9146_1_);
            }
            ItemStack itemstack = func_172_B();
            if(itemstack != null && (p_9146_1_ instanceof EntityLiving))
            {
                itemstack.func_9217_a((EntityLiving)p_9146_1_, this);
                if(itemstack.field_853_a <= 0)
                {
                    itemstack.func_577_a(this);
                    func_164_C();
                }
            }
            if(p_9146_1_ instanceof EntityLiving)
            {
                if(p_9146_1_.func_120_t())
                {
                    func_25047_a((EntityLiving)p_9146_1_, true);
                }
                func_25046_a(StatList.field_25102_s, i);
                int l = EnchantmentHelper.func_40636_c(field_416_aj, (EntityLiving)p_9146_1_);
                if(l > 0)
                {
                    p_9146_1_.func_40034_j(l * 4);
                }
            }
            func_35198_b(0.3F);
        }
    }

    public void func_35202_e(Entity entity)
    {
    }

    public void func_40109_c(Entity entity)
    {
    }

    public void func_20045_a(ItemStack itemstack)
    {
    }

    public void func_118_j()
    {
        super.func_118_j();
        field_20053_ao.func_20130_a(this);
        if(field_20052_ap != null)
        {
            field_20052_ap.func_20130_a(this);
        }
    }

    public boolean func_91_u()
    {
        return !field_21900_a && super.func_91_u();
    }

    public EnumStatus func_22060_a(int p_22060_1_, int p_22060_2_, int p_22060_3_)
    {
        if(!field_9093_l.field_792_x)
        {
            if(func_22057_E() || !func_120_t())
            {
                return EnumStatus.OTHER_PROBLEM;
            }
            if(!field_9093_l.field_4272_q.func_48567_d())
            {
                return EnumStatus.NOT_POSSIBLE_HERE;
            }
            if(field_9093_l.func_453_a())
            {
                return EnumStatus.NOT_POSSIBLE_NOW;
            }
            if(Math.abs(field_322_l - (double)p_22060_1_) > 3D || Math.abs(field_321_m - (double)p_22060_2_) > 2D || Math.abs(field_320_n - (double)p_22060_3_) > 3D)
            {
                return EnumStatus.TOO_FAR_AWAY;
            }
            double d = 8D;
            double d1 = 5D;
            List list = field_9093_l.func_457_a(net.minecraft.src.EntityMob.class, AxisAlignedBB.func_693_b((double)p_22060_1_ - d, (double)p_22060_2_ - d1, (double)p_22060_3_ - d, (double)p_22060_1_ + d, (double)p_22060_2_ + d1, (double)p_22060_3_ + d));
            if(!list.isEmpty())
            {
                return EnumStatus.NOT_SAFE;
            }
        }
        func_113_a(0.2F, 0.2F);
        field_9076_H = 0.2F;
        if(field_9093_l.func_530_e(p_22060_1_, p_22060_2_, p_22060_3_))
        {
            int i = field_9093_l.func_446_b(p_22060_1_, p_22060_2_, p_22060_3_);
            int j = BlockBed.func_48132_b(i);
            float f = 0.5F;
            float f1 = 0.5F;
            switch(j)
            {
            case 0: // '\0'
                f1 = 0.9F;
                break;

            case 2: // '\002'
                f1 = 0.1F;
                break;

            case 1: // '\001'
                f = 0.1F;
                break;

            case 3: // '\003'
                f = 0.9F;
                break;
            }
            func_22059_e(j);
            func_86_a((float)p_22060_1_ + f, (float)p_22060_2_ + 0.9375F, (float)p_22060_3_ + f1);
        } else
        {
            func_86_a((float)p_22060_1_ + 0.5F, (float)p_22060_2_ + 0.9375F, (float)p_22060_3_ + 0.5F);
        }
        field_21900_a = true;
        field_21902_c = 0;
        field_21901_b = new ChunkCoordinates(p_22060_1_, p_22060_2_, p_22060_3_);
        field_319_o = field_317_q = field_318_p = 0.0D;
        if(!field_9093_l.field_792_x)
        {
            field_9093_l.func_22082_o();
        }
        return EnumStatus.OK;
    }

    private void func_22059_e(int p_22059_1_)
    {
        field_22066_z = 0.0F;
        field_22067_A = 0.0F;
        switch(p_22059_1_)
        {
        case 0: // '\0'
            field_22067_A = -1.8F;
            break;

        case 2: // '\002'
            field_22067_A = 1.8F;
            break;

        case 1: // '\001'
            field_22066_z = 1.8F;
            break;

        case 3: // '\003'
            field_22066_z = -1.8F;
            break;
        }
    }

    public void func_22062_a(boolean p_22062_1_, boolean p_22062_2_, boolean p_22062_3_)
    {
        func_113_a(0.6F, 1.8F);
        func_22064_l_();
        ChunkCoordinates chunkcoordinates = field_21901_b;
        ChunkCoordinates chunkcoordinates1 = field_21901_b;
        if(chunkcoordinates != null && field_9093_l.func_444_a(chunkcoordinates.field_22216_a, chunkcoordinates.field_529_a, chunkcoordinates.field_528_b) == Block.field_9037_S.field_573_bc)
        {
            BlockBed.func_22022_a(field_9093_l, chunkcoordinates.field_22216_a, chunkcoordinates.field_529_a, chunkcoordinates.field_528_b, false);
            ChunkCoordinates chunkcoordinates2 = BlockBed.func_22021_g(field_9093_l, chunkcoordinates.field_22216_a, chunkcoordinates.field_529_a, chunkcoordinates.field_528_b, 0);
            if(chunkcoordinates2 == null)
            {
                chunkcoordinates2 = new ChunkCoordinates(chunkcoordinates.field_22216_a, chunkcoordinates.field_529_a + 1, chunkcoordinates.field_528_b);
            }
            func_86_a((float)chunkcoordinates2.field_22216_a + 0.5F, (float)chunkcoordinates2.field_529_a + field_9076_H + 0.1F, (float)chunkcoordinates2.field_528_b + 0.5F);
        }
        field_21900_a = false;
        if(!field_9093_l.field_792_x && p_22062_2_)
        {
            field_9093_l.func_22082_o();
        }
        if(p_22062_1_)
        {
            field_21902_c = 0;
        } else
        {
            field_21902_c = 100;
        }
        if(p_22062_3_)
        {
            func_25050_a(field_21901_b);
        }
    }

    private boolean func_22063_l()
    {
        return field_9093_l.func_444_a(field_21901_b.field_22216_a, field_21901_b.field_529_a, field_21901_b.field_528_b) == Block.field_9037_S.field_573_bc;
    }

    public static ChunkCoordinates func_25051_a(World p_25051_0_, ChunkCoordinates p_25051_1_)
    {
        IChunkProvider ichunkprovider = p_25051_0_.func_25073_n();
        ichunkprovider.func_376_d(p_25051_1_.field_22216_a - 3 >> 4, p_25051_1_.field_528_b - 3 >> 4);
        ichunkprovider.func_376_d(p_25051_1_.field_22216_a + 3 >> 4, p_25051_1_.field_528_b - 3 >> 4);
        ichunkprovider.func_376_d(p_25051_1_.field_22216_a - 3 >> 4, p_25051_1_.field_528_b + 3 >> 4);
        ichunkprovider.func_376_d(p_25051_1_.field_22216_a + 3 >> 4, p_25051_1_.field_528_b + 3 >> 4);
        if(p_25051_0_.func_444_a(p_25051_1_.field_22216_a, p_25051_1_.field_529_a, p_25051_1_.field_528_b) != Block.field_9037_S.field_573_bc)
        {
            return null;
        } else
        {
            ChunkCoordinates chunkcoordinates = BlockBed.func_22021_g(p_25051_0_, p_25051_1_.field_22216_a, p_25051_1_.field_529_a, p_25051_1_.field_528_b, 0);
            return chunkcoordinates;
        }
    }

    public boolean func_22057_E()
    {
        return field_21900_a;
    }

    public boolean func_22065_F()
    {
        return field_21900_a && field_21902_c >= 100;
    }

    public void func_22061_a(String s)
    {
    }

    public ChunkCoordinates func_25049_H()
    {
        return field_24900_d;
    }

    public void func_25050_a(ChunkCoordinates p_25050_1_)
    {
        if(p_25050_1_ != null)
        {
            field_24900_d = new ChunkCoordinates(p_25050_1_);
        } else
        {
            field_24900_d = null;
        }
    }

    public void func_27017_a(StatBase p_27017_1_)
    {
        func_25046_a(p_27017_1_, 1);
    }

    public void func_25046_a(StatBase statbase, int i)
    {
    }

    protected void func_154_F()
    {
        super.func_154_F();
        func_25046_a(StatList.field_25106_q, 1);
        if(func_35149_at())
        {
            func_35198_b(0.8F);
        } else
        {
            func_35198_b(0.2F);
        }
    }

    public void func_148_c(float p_148_1_, float p_148_2_)
    {
        double d = field_322_l;
        double d1 = field_321_m;
        double d2 = field_320_n;
        if(field_35214_K.field_35658_b)
        {
            double d3 = field_318_p;
            float f = field_35193_ak;
            field_35193_ak = 0.05F;
            super.func_148_c(p_148_1_, p_148_2_);
            field_318_p = d3 * 0.59999999999999998D;
            field_35193_ak = f;
        } else
        {
            super.func_148_c(p_148_1_, p_148_2_);
        }
        func_25045_g(field_322_l - d, field_321_m - d1, field_320_n - d2);
    }

    public void func_25045_g(double p_25045_1_, double p_25045_3_, double p_25045_5_)
    {
        if(field_327_g != null)
        {
            return;
        }
        if(func_110_a(Material.field_521_f))
        {
            int i = Math.round(MathHelper.func_583_a(p_25045_1_ * p_25045_1_ + p_25045_3_ * p_25045_3_ + p_25045_5_ * p_25045_5_) * 100F);
            if(i > 0)
            {
                func_25046_a(StatList.field_25108_p, i);
                func_35198_b(0.015F * (float)i * 0.01F);
            }
        } else
        if(func_27011_Z())
        {
            int j = Math.round(MathHelper.func_583_a(p_25045_1_ * p_25045_1_ + p_25045_5_ * p_25045_5_) * 100F);
            if(j > 0)
            {
                func_25046_a(StatList.field_25112_l, j);
                func_35198_b(0.015F * (float)j * 0.01F);
            }
        } else
        if(func_144_E())
        {
            if(p_25045_3_ > 0.0D)
            {
                func_25046_a(StatList.field_25110_n, (int)Math.round(p_25045_3_ * 100D));
            }
        } else
        if(field_9086_A)
        {
            int k = Math.round(MathHelper.func_583_a(p_25045_1_ * p_25045_1_ + p_25045_5_ * p_25045_5_) * 100F);
            if(k > 0)
            {
                func_25046_a(StatList.field_25113_k, k);
                if(func_35149_at())
                {
                    func_35198_b(0.09999999F * (float)k * 0.01F);
                } else
                {
                    func_35198_b(0.01F * (float)k * 0.01F);
                }
            }
        } else
        {
            int l = Math.round(MathHelper.func_583_a(p_25045_1_ * p_25045_1_ + p_25045_5_ * p_25045_5_) * 100F);
            if(l > 25)
            {
                func_25046_a(StatList.field_25109_o, l);
            }
        }
    }

    private void func_27015_h(double p_27015_1_, double p_27015_3_, double p_27015_5_)
    {
        if(field_327_g != null)
        {
            int i = Math.round(MathHelper.func_583_a(p_27015_1_ * p_27015_1_ + p_27015_3_ * p_27015_3_ + p_27015_5_ * p_27015_5_) * 100F);
            if(i > 0)
            {
                if(field_327_g instanceof EntityMinecart)
                {
                    func_25046_a(StatList.field_27095_r, i);
                    if(field_27995_d == null)
                    {
                        field_27995_d = new ChunkCoordinates(MathHelper.func_584_b(field_322_l), MathHelper.func_584_b(field_321_m), MathHelper.func_584_b(field_320_n));
                    } else
                    if(field_27995_d.func_27127_a(MathHelper.func_584_b(field_322_l), MathHelper.func_584_b(field_321_m), MathHelper.func_584_b(field_320_n)) >= 1000D)
                    {
                        func_25046_a(AchievementList.field_27102_q, 1);
                    }
                } else
                if(field_327_g instanceof EntityBoat)
                {
                    func_25046_a(StatList.field_27094_s, i);
                } else
                if(field_327_g instanceof EntityPig)
                {
                    func_25046_a(StatList.field_27093_t, i);
                }
            }
        }
    }

    protected void func_114_a(float p_114_1_)
    {
        if(field_35214_K.field_35659_c)
        {
            return;
        }
        if(p_114_1_ >= 2.0F)
        {
            func_25046_a(StatList.field_25111_m, (int)Math.round((double)p_114_1_ * 100D));
        }
        super.func_114_a(p_114_1_);
    }

    public void func_27010_a(EntityLiving p_27010_1_)
    {
        if(p_27010_1_ instanceof EntityMob)
        {
            func_27017_a(AchievementList.field_27100_s);
        }
    }

    public void func_4042_C()
    {
        if(field_28016_C > 0)
        {
            field_28016_C = 10;
            return;
        } else
        {
            field_28015_D = true;
            return;
        }
    }

    public void func_35195_d(int p_35195_1_)
    {
        field_9151_an += p_35195_1_;
        int i = 0x7fffffff - field_35212_N;
        if(p_35195_1_ > i)
        {
            p_35195_1_ = i;
        }
        field_35210_L += (float)p_35195_1_ / (float)func_35203_U();
        field_35212_N += p_35195_1_;
        for(; field_35210_L >= 1.0F; field_35210_L = field_35210_L / (float)func_35203_U())
        {
            field_35210_L = (field_35210_L - 1.0F) * (float)func_35203_U();
            func_36001_y();
        }

    }

    public void func_40108_b(int p_40108_1_)
    {
        field_35211_M -= p_40108_1_;
        if(field_35211_M < 0)
        {
            field_35211_M = 0;
        }
    }

    public int func_35203_U()
    {
        return 7 + (field_35211_M * 7 >> 1);
    }

    private void func_36001_y()
    {
        field_35211_M++;
    }

    public void func_35198_b(float p_35198_1_)
    {
        if(field_35214_K.field_35660_a)
        {
            return;
        }
        if(!field_9093_l.field_792_x)
        {
            field_35217_m.func_35583_a(p_35198_1_);
        }
    }

    public FoodStats func_35207_V()
    {
        return field_35217_m;
    }

    public boolean func_35197_c(boolean p_35197_1_)
    {
        return (p_35197_1_ || field_35217_m.func_35587_b()) && !field_35214_K.field_35660_a;
    }

    public boolean func_35206_W()
    {
        return func_40097_ai() > 0 && func_40097_ai() < func_40095_c();
    }

    public void func_35201_a(ItemStack p_35201_1_, int p_35201_2_)
    {
        if(p_35201_1_ == field_34908_d)
        {
            return;
        }
        field_34908_d = p_35201_1_;
        field_34909_e = p_35201_2_;
        if(!field_9093_l.field_792_x)
        {
            func_35148_h(true);
        }
    }

    public boolean func_35200_c(int p_35200_1_, int p_35200_2_, int p_35200_3_)
    {
        return true;
    }

    protected int func_36000_a(EntityPlayer p_36000_1_)
    {
        int i = field_35211_M * 7;
        if(i > 100)
        {
            return 100;
        } else
        {
            return i;
        }
    }

    protected boolean func_35188_X()
    {
        return true;
    }

    public String func_35150_Y()
    {
        return field_409_aq;
    }

    public void func_40107_e(int i)
    {
    }

    public void func_41031_d(EntityPlayer p_41031_1_)
    {
        field_416_aj.func_41013_a(p_41031_1_.field_416_aj);
        field_9109_aQ = p_41031_1_.field_9109_aQ;
        field_35217_m = p_41031_1_.field_35217_m;
        field_35211_M = p_41031_1_.field_35211_M;
        field_35212_N = p_41031_1_.field_35212_N;
        field_35210_L = p_41031_1_.field_35210_L;
        field_9151_an = p_41031_1_.field_9151_an;
    }

    protected boolean func_25017_l()
    {
        return !field_35214_K.field_35658_b;
    }

    public void func_50022_L()
    {
    }
}

