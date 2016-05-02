// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst 

package net.minecraft.src;

import java.util.*;

// Referenced classes of package net.minecraft.src:
//            Entity, EntityAITasks, ChunkCoordinates, EntityLookHelper, 
//            EntityMoveHelper, EntityJumpHelper, EntityBodyHelper, PathNavigate, 
//            EntitySenses, EntityCreeper, EntityGhast, MathHelper, 
//            DataWatcher, Vec3D, World, Profiler, 
//            DamageSource, Material, Potion, EntityXPOrb, 
//            EntityPlayer, EntityWolf, PotionEffect, EnchantmentHelper, 
//            Block, StepSound, AxisAlignedBB, NBTTagCompound, 
//            NBTTagList, PotionHelper, EnumCreatureAttribute, ItemStack, 
//            Item

public abstract class EntityLiving extends Entity
{

    public int field_9099_av;
    public float field_9098_aw;
    public float field_9096_ay;
    public float field_9095_az;
    public float field_9125_aA;
    public float field_46027_X;
    public float field_46026_Y;
    protected float field_9124_aB;
    protected float field_9123_aC;
    protected float field_9122_aD;
    protected float field_9121_aE;
    protected boolean field_9120_aF;
    protected String field_9119_aG;
    protected boolean field_9118_aH;
    protected float field_9117_aI;
    protected String field_9116_aJ;
    protected float field_9115_aK;
    protected int field_9114_aL;
    protected float field_9113_aM;
    public float field_35194_aj;
    public float field_35193_ak;
    public float field_9111_aO;
    public float field_9110_aP;
    protected int field_9109_aQ;
    public int field_9108_aR;
    protected int field_40105_ap;
    private int field_4099_a;
    public int field_9107_aS;
    public int field_9106_aT;
    public float field_9105_aU;
    public int field_9104_aV;
    public int field_9103_aW;
    public float field_9102_aX;
    public float field_9101_aY;
    protected boolean field_9100_aZ;
    protected int field_35192_ax;
    public int field_9144_ba;
    public float field_9143_bb;
    public float field_9142_bc;
    public float field_9141_bd;
    public float field_386_ba;
    protected EntityPlayer field_34903_b;
    protected int field_34904_c;
    private EntityLiving field_46024_aI;
    private int field_48339_c;
    private EntityLiving field_48338_d;
    public int field_35189_aD;
    public int field_35190_aE;
    protected HashMap field_35191_aF;
    private boolean field_39002_b;
    private int field_39003_c;
    private EntityLookHelper field_46030_d;
    private EntityMoveHelper field_46031_e;
    private EntityJumpHelper field_46028_f;
    private EntityBodyHelper field_48344_j;
    private PathNavigate field_48345_k;
    protected EntityAITasks field_46025_aM;
    protected EntityAITasks field_48337_aM;
    private EntityLiving field_48342_l;
    private EntitySenses field_48343_m;
    private float field_48340_n;
    private ChunkCoordinates field_48341_o;
    private float field_48346_p;
    protected int field_9140_bf;
    protected double field_9139_bg;
    protected double field_9138_bh;
    protected double field_9137_bi;
    protected double field_9136_bj;
    protected double field_9135_bk;
    float field_9134_bl;
    protected int field_9133_bm;
    protected int field_9132_bn;
    protected float field_9131_bo;
    protected float field_9130_bp;
    protected float field_9129_bq;
    protected boolean field_9128_br;
    protected float field_9127_bs;
    protected float field_9126_bt;
    private int field_39004_d;
    private Entity field_4098_b;
    protected int field_4104_c;

    public EntityLiving(World p_i280_1_)
    {
        super(p_i280_1_);
        field_9099_av = 20;
        field_9095_az = 0.0F;
        field_9125_aA = 0.0F;
        field_46027_X = 0.0F;
        field_46026_Y = 0.0F;
        field_9120_aF = true;
        field_9119_aG = "/mob/char.png";
        field_9118_aH = true;
        field_9117_aI = 0.0F;
        field_9116_aJ = null;
        field_9115_aK = 1.0F;
        field_9114_aL = 0;
        field_9113_aM = 0.0F;
        field_35194_aj = 0.1F;
        field_35193_ak = 0.02F;
        field_9105_aU = 0.0F;
        field_9104_aV = 0;
        field_9103_aW = 0;
        field_9100_aZ = false;
        field_9144_ba = -1;
        field_9143_bb = (float)(Math.random() * 0.89999997615814209D + 0.10000000149011612D);
        field_34903_b = null;
        field_34904_c = 0;
        field_46024_aI = null;
        field_48339_c = 0;
        field_48338_d = null;
        field_35189_aD = 0;
        field_35190_aE = 0;
        field_35191_aF = new HashMap();
        field_39002_b = true;
        field_46025_aM = new EntityAITasks();
        field_48337_aM = new EntityAITasks();
        field_48341_o = new ChunkCoordinates(0, 0, 0);
        field_48346_p = -1F;
        field_9134_bl = 0.0F;
        field_9133_bm = 0;
        field_9132_bn = 0;
        field_9128_br = false;
        field_9127_bs = 0.0F;
        field_9126_bt = 0.7F;
        field_39004_d = 0;
        field_4104_c = 0;
        field_9109_aQ = func_40095_c();
        field_329_e = true;
        field_46030_d = new EntityLookHelper(this);
        field_46031_e = new EntityMoveHelper(this);
        field_46028_f = new EntityJumpHelper(this);
        field_48344_j = new EntityBodyHelper(this);
        field_48345_k = new PathNavigate(this, p_i280_1_, 16F);
        field_48343_m = new EntitySenses(this);
        field_9096_ay = (float)(Math.random() + 1.0D) * 0.01F;
        func_86_a(field_322_l, field_321_m, field_320_n);
        field_9098_aw = (float)Math.random() * 12398F;
        field_316_r = (float)(Math.random() * 3.1415927410125732D * 2D);
        field_46027_X = field_316_r;
        field_9067_S = 0.5F;
    }

    public EntityLookHelper func_46021_ae()
    {
        return field_46030_d;
    }

    public EntityMoveHelper func_46012_af()
    {
        return field_46031_e;
    }

    public EntityJumpHelper func_46013_ag()
    {
        return field_46028_f;
    }

    public PathNavigate func_48333_ak()
    {
        return field_48345_k;
    }

    public EntitySenses func_48318_al()
    {
        return field_48343_m;
    }

    public Random func_46019_ai()
    {
        return field_9064_W;
    }

    public EntityLiving func_46020_aj()
    {
        return field_46024_aI;
    }

    public EntityLiving func_48324_ao()
    {
        return field_48338_d;
    }

    public void func_48335_g(Entity p_48335_1_)
    {
        if(p_48335_1_ instanceof EntityLiving)
        {
            field_48338_d = (EntityLiving)p_48335_1_;
        }
    }

    public int func_46018_ak()
    {
        return field_9132_bn;
    }

    public float func_48314_aq()
    {
        return field_46027_X;
    }

    public float func_48332_ar()
    {
        return field_48340_n;
    }

    public void func_48320_d(float p_48320_1_)
    {
        field_48340_n = p_48320_1_;
        func_46017_d(p_48320_1_);
    }

    public boolean func_35224_c(Entity p_35224_1_)
    {
        func_48335_g(p_35224_1_);
        return false;
    }

    public EntityLiving func_48331_as()
    {
        return field_48342_l;
    }

    public void func_48327_b(EntityLiving p_48327_1_)
    {
        field_48342_l = p_48327_1_;
    }

    public boolean func_48336_a(Class p_48336_1_)
    {
        return (net.minecraft.src.EntityCreeper.class) != p_48336_1_ && (net.minecraft.src.EntityGhast.class) != p_48336_1_;
    }

    public void func_48319_z()
    {
    }

    public boolean func_48325_at()
    {
        return func_48328_e(MathHelper.func_584_b(field_322_l), MathHelper.func_584_b(field_321_m), MathHelper.func_584_b(field_320_n));
    }

    public boolean func_48328_e(int p_48328_1_, int p_48328_2_, int p_48328_3_)
    {
        if(field_48346_p == -1F)
        {
            return true;
        } else
        {
            return field_48341_o.func_48473_c(p_48328_1_, p_48328_2_, p_48328_3_) < field_48346_p * field_48346_p;
        }
    }

    public void func_48317_b(int p_48317_1_, int p_48317_2_, int p_48317_3_, int p_48317_4_)
    {
        field_48341_o.func_48474_a(p_48317_1_, p_48317_2_, p_48317_3_);
        field_48346_p = p_48317_4_;
    }

    public ChunkCoordinates func_48323_au()
    {
        return field_48341_o;
    }

    public float func_48330_av()
    {
        return field_48346_p;
    }

    public void func_48322_aw()
    {
        field_48346_p = -1F;
    }

    public boolean func_48329_ax()
    {
        return field_48346_p != -1F;
    }

    public void func_48334_a(EntityLiving p_48334_1_)
    {
        field_46024_aI = p_48334_1_;
        field_48339_c = field_46024_aI == null ? 0 : 60;
    }

    protected void func_21044_a()
    {
        field_21045_af.func_21153_a(8, Integer.valueOf(field_39003_c));
    }

    public boolean func_145_g(Entity p_145_1_)
    {
        return field_9093_l.func_486_a(Vec3D.func_768_b(field_322_l, field_321_m + (double)func_104_p(), field_320_n), Vec3D.func_768_b(p_145_1_.field_322_l, p_145_1_.field_321_m + (double)p_145_1_.func_104_p(), p_145_1_.field_320_n)) == null;
    }

    public boolean func_129_c_()
    {
        return !field_304_B;
    }

    public boolean func_124_r()
    {
        return !field_304_B;
    }

    public float func_104_p()
    {
        return field_298_E * 0.85F;
    }

    public int func_146_b()
    {
        return 80;
    }

    public void func_22056_G()
    {
        String s = func_6097_d();
        if(s != null)
        {
            field_9093_l.func_506_a(this, s, func_6102_h(), func_40090_w());
        }
    }

    public void func_84_k()
    {
        field_9111_aO = field_9110_aP;
        super.func_84_k();
        Profiler.func_40518_a("mobBaseTick");
        if(func_120_t() && field_9064_W.nextInt(1000) < field_4099_a++)
        {
            field_4099_a = -func_146_b();
            func_22056_G();
        }
        if(func_120_t() && func_91_u())
        {
            if(!func_121_a(DamageSource.field_35087_d, 1));
        }
        if(func_40033_ax() || field_9093_l.field_792_x)
        {
            func_40036_aw();
        }
        if(func_120_t() && func_110_a(Material.field_521_f) && !func_21046_d_() && !field_35191_aF.containsKey(Integer.valueOf(Potion.field_35457_o.field_35447_H)))
        {
            func_41008_j(func_40094_f(func_41009_al()));
            if(func_41009_al() == -20)
            {
                func_41008_j(0);
                for(int i = 0; i < 8; i++)
                {
                    float f = field_9064_W.nextFloat() - field_9064_W.nextFloat();
                    float f1 = field_9064_W.nextFloat() - field_9064_W.nextFloat();
                    float f2 = field_9064_W.nextFloat() - field_9064_W.nextFloat();
                    field_9093_l.func_514_a("bubble", field_322_l + (double)f, field_321_m + (double)f1, field_320_n + (double)f2, field_319_o, field_318_p, field_317_q);
                }

                func_121_a(DamageSource.field_35088_e, 2);
            }
            func_40036_aw();
        } else
        {
            func_41008_j(300);
        }
        field_9102_aX = field_9101_aY;
        if(field_9103_aW > 0)
        {
            field_9103_aW--;
        }
        if(field_9107_aS > 0)
        {
            field_9107_aS--;
        }
        if(field_9083_ac > 0)
        {
            field_9083_ac--;
        }
        if(field_9109_aQ <= 0)
        {
            func_40102_ag();
        }
        if(field_34904_c > 0)
        {
            field_34904_c--;
        } else
        {
            field_34903_b = null;
        }
        if(field_48338_d != null && !field_48338_d.func_120_t())
        {
            field_48338_d = null;
        }
        if(field_46024_aI != null)
        {
            if(!field_46024_aI.func_120_t())
            {
                func_48334_a(null);
            } else
            if(field_48339_c > 0)
            {
                field_48339_c--;
            } else
            {
                func_48334_a(null);
            }
        }
        func_35186_aj();
        field_9121_aE = field_9122_aD;
        field_9125_aA = field_9095_az;
        field_46026_Y = field_46027_X;
        field_9089_x = field_316_r;
        field_9088_y = field_315_s;
        Profiler.func_40517_a();
    }

    protected void func_40102_ag()
    {
        field_9104_aV++;
        if(field_9104_aV == 20)
        {
            if(!field_9093_l.field_792_x && (field_34904_c > 0 || func_35188_X()) && !func_40104_l())
            {
                for(int i = func_36000_a(field_34903_b); i > 0;)
                {
                    int k = EntityXPOrb.func_35152_b(i);
                    i -= k;
                    field_9093_l.func_526_a(new EntityXPOrb(field_9093_l, field_322_l, field_321_m, field_320_n, k));
                }

            }
            func_6101_K();
            func_118_j();
            for(int j = 0; j < 20; j++)
            {
                double d = field_9064_W.nextGaussian() * 0.02D;
                double d1 = field_9064_W.nextGaussian() * 0.02D;
                double d2 = field_9064_W.nextGaussian() * 0.02D;
                field_9093_l.func_514_a("explode", (field_322_l + (double)(field_9064_W.nextFloat() * field_300_D * 2.0F)) - (double)field_300_D, field_321_m + (double)(field_9064_W.nextFloat() * field_298_E), (field_320_n + (double)(field_9064_W.nextFloat() * field_300_D * 2.0F)) - (double)field_300_D, d, d1, d2);
            }

        }
    }

    protected int func_40094_f(int p_40094_1_)
    {
        return p_40094_1_ - 1;
    }

    protected int func_36000_a(EntityPlayer p_36000_1_)
    {
        return field_35192_ax;
    }

    protected boolean func_35188_X()
    {
        return false;
    }

    public void func_156_D()
    {
        for(int i = 0; i < 20; i++)
        {
            double d = field_9064_W.nextGaussian() * 0.02D;
            double d1 = field_9064_W.nextGaussian() * 0.02D;
            double d2 = field_9064_W.nextGaussian() * 0.02D;
            double d3 = 10D;
            field_9093_l.func_514_a("explode", (field_322_l + (double)(field_9064_W.nextFloat() * field_300_D * 2.0F)) - (double)field_300_D - d * d3, (field_321_m + (double)(field_9064_W.nextFloat() * field_298_E)) - d1 * d3, (field_320_n + (double)(field_9064_W.nextFloat() * field_300_D * 2.0F)) - (double)field_300_D - d2 * d3, d, d1, d2);
        }

    }

    public void func_115_v()
    {
        super.func_115_v();
        field_9124_aB = field_9123_aC;
        field_9123_aC = 0.0F;
        field_9072_N = 0.0F;
    }

    public void func_106_b_()
    {
        super.func_106_b_();
        if(field_35189_aD > 0)
        {
            if(field_35190_aE <= 0)
            {
                field_35190_aE = 60;
            }
            field_35190_aE--;
            if(field_35190_aE <= 0)
            {
                field_35189_aD--;
            }
        }
        func_153_y();
        double d = field_322_l - field_9092_m;
        double d1 = field_320_n - field_9090_o;
        float f = MathHelper.func_583_a(d * d + d1 * d1);
        float f1 = field_9095_az;
        float f2 = 0.0F;
        field_9124_aB = field_9123_aC;
        float f3 = 0.0F;
        if(f > 0.05F)
        {
            f3 = 1.0F;
            f2 = f * 3F;
            f1 = ((float)Math.atan2(d1, d) * 180F) / 3.141593F - 90F;
        }
        if(field_9110_aP > 0.0F)
        {
            f1 = field_316_r;
        }
        if(!field_9086_A)
        {
            f3 = 0.0F;
        }
        field_9123_aC = field_9123_aC + (f3 - field_9123_aC) * 0.3F;
        if(func_46022_as())
        {
            field_48344_j.func_48431_a();
        } else
        {
            float f4;
            for(f4 = f1 - field_9095_az; f4 < -180F; f4 += 360F) { }
            for(; f4 >= 180F; f4 -= 360F) { }
            field_9095_az += f4 * 0.3F;
            float f5;
            for(f5 = field_316_r - field_9095_az; f5 < -180F; f5 += 360F) { }
            for(; f5 >= 180F; f5 -= 360F) { }
            boolean flag = f5 < -90F || f5 >= 90F;
            if(f5 < -75F)
            {
                f5 = -75F;
            }
            if(f5 >= 75F)
            {
                f5 = 75F;
            }
            field_9095_az = field_316_r - f5;
            if(f5 * f5 > 2500F)
            {
                field_9095_az += f5 * 0.2F;
            }
            if(flag)
            {
                f2 *= -1F;
            }
        }
        for(; field_316_r - field_9089_x < -180F; field_9089_x -= 360F) { }
        for(; field_316_r - field_9089_x >= 180F; field_9089_x += 360F) { }
        for(; field_9095_az - field_9125_aA < -180F; field_9125_aA -= 360F) { }
        for(; field_9095_az - field_9125_aA >= 180F; field_9125_aA += 360F) { }
        for(; field_315_s - field_9088_y < -180F; field_9088_y -= 360F) { }
        for(; field_315_s - field_9088_y >= 180F; field_9088_y += 360F) { }
        for(; field_46027_X - field_46026_Y < -180F; field_46026_Y -= 360F) { }
        for(; field_46027_X - field_46026_Y >= 180F; field_46026_Y += 360F) { }
        field_9122_aD += f2;
    }

    protected void func_113_a(float p_113_1_, float p_113_2_)
    {
        super.func_113_a(p_113_1_, p_113_2_);
    }

    public void func_137_a(int p_137_1_)
    {
        if(field_9109_aQ <= 0)
        {
            return;
        }
        field_9109_aQ += p_137_1_;
        if(field_9109_aQ > func_40095_c())
        {
            field_9109_aQ = func_40095_c();
        }
        field_9083_ac = field_9099_av / 2;
    }

    public abstract int func_40095_c();

    public int func_40097_ai()
    {
        return field_9109_aQ;
    }

    public void func_40103_i(int p_40103_1_)
    {
        field_9109_aQ = p_40103_1_;
        if(p_40103_1_ > func_40095_c())
        {
            p_40103_1_ = func_40095_c();
        }
    }

    public boolean func_121_a(DamageSource p_121_1_, int p_121_2_)
    {
        if(field_9093_l.field_792_x)
        {
            return false;
        }
        field_9132_bn = 0;
        if(field_9109_aQ <= 0)
        {
            return false;
        }
        if(p_121_1_.func_40272_k() && func_35184_a(Potion.field_35456_n))
        {
            return false;
        }
        field_9141_bd = 1.5F;
        boolean flag = true;
        if((float)field_9083_ac > (float)field_9099_av / 2.0F)
        {
            if(p_121_2_ <= field_9133_bm)
            {
                return false;
            }
            func_6099_c(p_121_1_, p_121_2_ - field_9133_bm);
            field_9133_bm = p_121_2_;
            flag = false;
        } else
        {
            field_9133_bm = p_121_2_;
            field_9108_aR = field_9109_aQ;
            field_9083_ac = field_9099_av;
            func_6099_c(p_121_1_, p_121_2_);
            field_9107_aS = field_9106_aT = 10;
        }
        field_9105_aU = 0.0F;
        Entity entity = p_121_1_.func_35080_a();
        if(entity != null)
        {
            if(entity instanceof EntityLiving)
            {
                func_48334_a((EntityLiving)entity);
            }
            if(entity instanceof EntityPlayer)
            {
                field_34904_c = 60;
                field_34903_b = (EntityPlayer)entity;
            } else
            if(entity instanceof EntityWolf)
            {
                EntityWolf entitywolf = (EntityWolf)entity;
                if(entitywolf.func_48373_u_())
                {
                    field_34904_c = 60;
                    field_34903_b = null;
                }
            }
        }
        if(flag)
        {
            field_9093_l.func_9206_a(this, (byte)2);
            func_9060_u();
            if(entity != null)
            {
                double d = entity.field_322_l - field_322_l;
                double d1;
                for(d1 = entity.field_320_n - field_320_n; d * d + d1 * d1 < 0.0001D; d1 = (Math.random() - Math.random()) * 0.01D)
                {
                    d = (Math.random() - Math.random()) * 0.01D;
                }

                field_9105_aU = (float)((Math.atan2(d1, d) * 180D) / 3.1415927410125732D) - field_316_r;
                func_143_a(entity, p_121_2_, d, d1);
            } else
            {
                field_9105_aU = (int)(Math.random() * 2D) * 180;
            }
        }
        if(field_9109_aQ <= 0)
        {
            if(flag)
            {
                field_9093_l.func_506_a(this, func_6098_f(), func_6102_h(), func_40090_w());
            }
            func_142_f(p_121_1_);
        } else
        if(flag)
        {
            field_9093_l.func_506_a(this, func_6100_e(), func_6102_h(), func_40090_w());
        }
        return true;
    }

    private float func_40090_w()
    {
        if(func_40104_l())
        {
            return (field_9064_W.nextFloat() - field_9064_W.nextFloat()) * 0.2F + 1.5F;
        } else
        {
            return (field_9064_W.nextFloat() - field_9064_W.nextFloat()) * 0.2F + 1.0F;
        }
    }

    public int func_40092_O()
    {
        return 0;
    }

    protected void func_40101_g(int i)
    {
    }

    protected int func_40091_d(DamageSource p_40091_1_, int p_40091_2_)
    {
        if(!p_40091_1_.func_35083_b())
        {
            int i = 25 - func_40092_O();
            int j = p_40091_2_ * i + field_40105_ap;
            func_40101_g(p_40091_2_);
            p_40091_2_ = j / 25;
            field_40105_ap = j % 25;
        }
        return p_40091_2_;
    }

    protected int func_40099_b(DamageSource p_40099_1_, int p_40099_2_)
    {
        if(func_35184_a(Potion.field_35459_m))
        {
            int i = (func_35187_b(Potion.field_35459_m).func_35652_c() + 1) * 5;
            int j = 25 - i;
            int k = p_40099_2_ * j + field_40105_ap;
            p_40099_2_ = k / 25;
            field_40105_ap = k % 25;
        }
        return p_40099_2_;
    }

    protected void func_6099_c(DamageSource p_6099_1_, int p_6099_2_)
    {
        p_6099_2_ = func_40091_d(p_6099_1_, p_6099_2_);
        p_6099_2_ = func_40099_b(p_6099_1_, p_6099_2_);
        field_9109_aQ -= p_6099_2_;
    }

    protected float func_6102_h()
    {
        return 1.0F;
    }

    protected String func_6097_d()
    {
        return null;
    }

    protected String func_6100_e()
    {
        return "damage.hurtflesh";
    }

    protected String func_6098_f()
    {
        return "damage.hurtflesh";
    }

    public void func_143_a(Entity p_143_1_, int p_143_2_, double p_143_3_, double p_143_5_)
    {
        field_35151_ca = true;
        float f = MathHelper.func_583_a(p_143_3_ * p_143_3_ + p_143_5_ * p_143_5_);
        float f1 = 0.4F;
        field_319_o /= 2D;
        field_318_p /= 2D;
        field_317_q /= 2D;
        field_319_o -= (p_143_3_ / (double)f) * (double)f1;
        field_318_p += f1;
        field_317_q -= (p_143_5_ / (double)f) * (double)f1;
        if(field_318_p > 0.40000000596046448D)
        {
            field_318_p = 0.40000000596046448D;
        }
    }

    public void func_142_f(DamageSource p_142_1_)
    {
        Entity entity = p_142_1_.func_35080_a();
        if(field_9114_aL >= 0 && entity != null)
        {
            entity.func_96_b(this, field_9114_aL);
        }
        if(entity != null)
        {
            entity.func_27010_a(this);
        }
        field_9100_aZ = true;
        if(!field_9093_l.field_792_x)
        {
            int i = 0;
            if(entity instanceof EntityPlayer)
            {
                i = EnchantmentHelper.func_40633_f(((EntityPlayer)entity).field_416_aj);
            }
            if(!func_40104_l())
            {
                func_21047_g_(field_34904_c > 0, i);
                if(field_34904_c > 0)
                {
                    int j = field_9064_W.nextInt(200) - i;
                    if(j < 5)
                    {
                        func_48321_b(j > 0 ? 0 : 1);
                    }
                }
            }
        }
        field_9093_l.func_9206_a(this, (byte)3);
    }

    protected void func_48321_b(int i)
    {
    }

    protected void func_21047_g_(boolean p_21047_1_, int p_21047_2_)
    {
        int i = func_149_g();
        if(i > 0)
        {
            int j = field_9064_W.nextInt(3);
            if(p_21047_2_ > 0)
            {
                j += field_9064_W.nextInt(p_21047_2_ + 1);
            }
            for(int k = 0; k < j; k++)
            {
                func_128_a(i, 1);
            }

        }
    }

    protected int func_149_g()
    {
        return 0;
    }

    protected void func_114_a(float p_114_1_)
    {
        super.func_114_a(p_114_1_);
        int i = (int)Math.ceil(p_114_1_ - 3F);
        if(i > 0)
        {
            if(i > 4)
            {
                field_9093_l.func_506_a(this, "damage.fallbig", 1.0F, 1.0F);
            } else
            {
                field_9093_l.func_506_a(this, "damage.fallsmall", 1.0F, 1.0F);
            }
            func_121_a(DamageSource.field_35098_h, i);
            int j = field_9093_l.func_444_a(MathHelper.func_584_b(field_322_l), MathHelper.func_584_b(field_321_m - 0.20000000298023224D - (double)field_9076_H), MathHelper.func_584_b(field_320_n));
            if(j > 0)
            {
                StepSound stepsound = Block.field_542_n[j].field_555_bl;
                field_9093_l.func_506_a(this, stepsound.func_737_c(), stepsound.func_738_a() * 0.5F, stepsound.func_739_b() * 0.75F);
            }
        }
    }

    public void func_148_c(float p_148_1_, float p_148_2_)
    {
        if(func_27011_Z())
        {
            double d = field_321_m;
            func_90_a(p_148_1_, p_148_2_, func_46022_as() ? 0.04F : 0.02F);
            func_88_c(field_319_o, field_318_p, field_317_q);
            field_319_o *= 0.80000001192092896D;
            field_318_p *= 0.80000001192092896D;
            field_317_q *= 0.80000001192092896D;
            field_318_p -= 0.02D;
            if(field_9084_B && func_133_b(field_319_o, ((field_318_p + 0.60000002384185791D) - field_321_m) + d, field_317_q))
            {
                field_318_p = 0.30000001192092896D;
            }
        } else
        if(func_112_q())
        {
            double d1 = field_321_m;
            func_90_a(p_148_1_, p_148_2_, 0.02F);
            func_88_c(field_319_o, field_318_p, field_317_q);
            field_319_o *= 0.5D;
            field_318_p *= 0.5D;
            field_317_q *= 0.5D;
            field_318_p -= 0.02D;
            if(field_9084_B && func_133_b(field_319_o, ((field_318_p + 0.60000002384185791D) - field_321_m) + d1, field_317_q))
            {
                field_318_p = 0.30000001192092896D;
            }
        } else
        {
            float f = 0.91F;
            if(field_9086_A)
            {
                f = 0.5460001F;
                int i = field_9093_l.func_444_a(MathHelper.func_584_b(field_322_l), MathHelper.func_584_b(field_312_v.field_963_b) - 1, MathHelper.func_584_b(field_320_n));
                if(i > 0)
                {
                    f = Block.field_542_n[i].field_552_bo * 0.91F;
                }
            }
            float f1 = 0.1627714F / (f * f * f);
            float f2;
            if(field_9086_A)
            {
                if(func_46022_as())
                {
                    f2 = func_48332_ar();
                } else
                {
                    f2 = field_35194_aj;
                }
                f2 *= f1;
            } else
            {
                f2 = field_35193_ak;
            }
            func_90_a(p_148_1_, p_148_2_, f2);
            f = 0.91F;
            if(field_9086_A)
            {
                f = 0.5460001F;
                int j = field_9093_l.func_444_a(MathHelper.func_584_b(field_322_l), MathHelper.func_584_b(field_312_v.field_963_b) - 1, MathHelper.func_584_b(field_320_n));
                if(j > 0)
                {
                    f = Block.field_542_n[j].field_552_bo * 0.91F;
                }
            }
            if(func_144_E())
            {
                float f3 = 0.15F;
                if(field_319_o < (double)(-f3))
                {
                    field_319_o = -f3;
                }
                if(field_319_o > (double)f3)
                {
                    field_319_o = f3;
                }
                if(field_317_q < (double)(-f3))
                {
                    field_317_q = -f3;
                }
                if(field_317_q > (double)f3)
                {
                    field_317_q = f3;
                }
                field_9072_N = 0.0F;
                if(field_318_p < -0.14999999999999999D)
                {
                    field_318_p = -0.14999999999999999D;
                }
                boolean flag = func_9059_p() && (this instanceof EntityPlayer);
                if(flag && field_318_p < 0.0D)
                {
                    field_318_p = 0.0D;
                }
            }
            func_88_c(field_319_o, field_318_p, field_317_q);
            if(field_9084_B && func_144_E())
            {
                field_318_p = 0.20000000000000001D;
            }
            field_318_p -= 0.080000000000000002D;
            field_318_p *= 0.98000001907348633D;
            field_319_o *= f;
            field_317_q *= f;
        }
        field_9142_bc = field_9141_bd;
        double d2 = field_322_l - field_9092_m;
        double d3 = field_320_n - field_9090_o;
        float f4 = MathHelper.func_583_a(d2 * d2 + d3 * d3) * 4F;
        if(f4 > 1.0F)
        {
            f4 = 1.0F;
        }
        field_9141_bd += (f4 - field_9141_bd) * 0.4F;
        field_386_ba += field_9141_bd;
    }

    public boolean func_144_E()
    {
        int i = MathHelper.func_584_b(field_322_l);
        int j = MathHelper.func_584_b(field_312_v.field_963_b);
        int k = MathHelper.func_584_b(field_320_n);
        int l = field_9093_l.func_444_a(i, j, k);
        return l == Block.field_638_aG.field_573_bc || l == Block.field_35045_bv.field_573_bc;
    }

    public void func_97_a(NBTTagCompound p_97_1_)
    {
        p_97_1_.func_394_a("Health", (short)field_9109_aQ);
        p_97_1_.func_394_a("HurtTime", (short)field_9107_aS);
        p_97_1_.func_394_a("DeathTime", (short)field_9104_aV);
        p_97_1_.func_394_a("AttackTime", (short)field_9103_aW);
        if(!field_35191_aF.isEmpty())
        {
            NBTTagList nbttaglist = new NBTTagList();
            NBTTagCompound nbttagcompound;
            for(Iterator iterator = field_35191_aF.values().iterator(); iterator.hasNext(); nbttaglist.func_386_a(nbttagcompound))
            {
                PotionEffect potioneffect = (PotionEffect)iterator.next();
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.func_409_a("Id", (byte)potioneffect.func_35649_a());
                nbttagcompound.func_409_a("Amplifier", (byte)potioneffect.func_35652_c());
                nbttagcompound.func_405_a("Duration", potioneffect.func_35653_b());
            }

            p_97_1_.func_399_a("ActiveEffects", nbttaglist);
        }
    }

    public void func_99_b(NBTTagCompound p_99_1_)
    {
        if(field_9109_aQ < -32768)
        {
            field_9109_aQ = -32768;
        }
        field_9109_aQ = p_99_1_.func_406_c("Health");
        if(!p_99_1_.func_410_a("Health"))
        {
            field_9109_aQ = func_40095_c();
        }
        field_9107_aS = p_99_1_.func_406_c("HurtTime");
        field_9104_aV = p_99_1_.func_406_c("DeathTime");
        field_9103_aW = p_99_1_.func_406_c("AttackTime");
        if(p_99_1_.func_410_a("ActiveEffects"))
        {
            NBTTagList nbttaglist = p_99_1_.func_407_k("ActiveEffects");
            for(int i = 0; i < nbttaglist.func_387_b(); i++)
            {
                NBTTagCompound nbttagcompound = (NBTTagCompound)nbttaglist.func_388_a(i);
                byte byte0 = nbttagcompound.func_408_b("Id");
                byte byte1 = nbttagcompound.func_408_b("Amplifier");
                int j = nbttagcompound.func_395_d("Duration");
                field_35191_aF.put(Integer.valueOf(byte0), new PotionEffect(byte0, j, byte1));
            }

        }
    }

    public boolean func_120_t()
    {
        return !field_304_B && field_9109_aQ > 0;
    }

    public boolean func_21046_d_()
    {
        return false;
    }

    public void func_46017_d(float p_46017_1_)
    {
        field_9130_bp = p_46017_1_;
    }

    public void func_46014_e(boolean p_46014_1_)
    {
        field_9128_br = p_46014_1_;
    }

    public void func_153_y()
    {
        if(field_39004_d > 0)
        {
            field_39004_d--;
        }
        if(field_9140_bf > 0)
        {
            double d = field_322_l + (field_9139_bg - field_322_l) / (double)field_9140_bf;
            double d1 = field_321_m + (field_9138_bh - field_321_m) / (double)field_9140_bf;
            double d2 = field_320_n + (field_9137_bi - field_320_n) / (double)field_9140_bf;
            double d3;
            for(d3 = field_9136_bj - (double)field_316_r; d3 < -180D; d3 += 360D) { }
            for(; d3 >= 180D; d3 -= 360D) { }
            field_316_r += d3 / (double)field_9140_bf;
            field_315_s += (field_9135_bk - (double)field_315_s) / (double)field_9140_bf;
            field_9140_bf--;
            func_86_a(d, d1, d2);
            func_6096_b(field_316_r, field_315_s);
            List list1 = field_9093_l.func_481_a(this, field_312_v.func_694_e(0.03125D, 0.0D, 0.03125D));
            if(list1.size() > 0)
            {
                double d4 = 0.0D;
                for(int j = 0; j < list1.size(); j++)
                {
                    AxisAlignedBB axisalignedbb = (AxisAlignedBB)list1.get(j);
                    if(axisalignedbb.field_968_e > d4)
                    {
                        d4 = axisalignedbb.field_968_e;
                    }
                }

                d1 += d4 - field_312_v.field_963_b;
                func_86_a(d, d1, d2);
            }
        }
        Profiler.func_40518_a("ai");
        if(func_22058_w())
        {
            field_9128_br = false;
            field_9131_bo = 0.0F;
            field_9130_bp = 0.0F;
            field_9129_bq = 0.0F;
        } else
        if(func_44006_ak())
        {
            if(func_46022_as())
            {
                Profiler.func_40518_a("newAi");
                func_46015_av();
                Profiler.func_40517_a();
            } else
            {
                Profiler.func_40518_a("oldAi");
                func_152_d_();
                Profiler.func_40517_a();
                field_46027_X = field_316_r;
            }
        }
        Profiler.func_40517_a();
        boolean flag = func_27011_Z();
        boolean flag1 = func_112_q();
        if(field_9128_br)
        {
            if(flag)
            {
                field_318_p += 0.039999999105930328D;
            } else
            if(flag1)
            {
                field_318_p += 0.039999999105930328D;
            } else
            if(field_9086_A && field_39004_d == 0)
            {
                func_154_F();
                field_39004_d = 10;
            }
        } else
        {
            field_39004_d = 0;
        }
        field_9131_bo *= 0.98F;
        field_9130_bp *= 0.98F;
        field_9129_bq *= 0.9F;
        float f = field_35194_aj;
        field_35194_aj *= func_35178_D();
        func_148_c(field_9131_bo, field_9130_bp);
        field_35194_aj = f;
        Profiler.func_40518_a("push");
        List list = field_9093_l.func_450_b(this, field_312_v.func_708_b(0.20000000298023224D, 0.0D, 0.20000000298023224D));
        if(list != null && list.size() > 0)
        {
            for(int i = 0; i < list.size(); i++)
            {
                Entity entity = (Entity)list.get(i);
                if(entity.func_124_r())
                {
                    entity.func_92_c(this);
                }
            }

        }
        Profiler.func_40517_a();
    }

    protected boolean func_46022_as()
    {
        return false;
    }

    protected boolean func_44006_ak()
    {
        return !field_9093_l.field_792_x;
    }

    protected boolean func_22058_w()
    {
        return field_9109_aQ <= 0;
    }

    public boolean func_35180_G()
    {
        return false;
    }

    protected void func_154_F()
    {
        field_318_p = 0.41999998688697815D;
        if(func_35184_a(Potion.field_35460_j))
        {
            field_318_p += (float)(func_35187_b(Potion.field_35460_j).func_35652_c() + 1) * 0.1F;
        }
        if(func_35149_at())
        {
            float f = field_316_r * 0.01745329F;
            field_319_o -= MathHelper.func_585_a(f) * 0.2F;
            field_317_q += MathHelper.func_582_b(f) * 0.2F;
        }
        field_35151_ca = true;
    }

    protected boolean func_25020_s()
    {
        return true;
    }

    protected void func_27013_Q()
    {
        EntityPlayer entityplayer = field_9093_l.func_472_a(this, -1D);
        if(entityplayer != null)
        {
            double d = ((Entity) (entityplayer)).field_322_l - field_322_l;
            double d1 = ((Entity) (entityplayer)).field_321_m - field_321_m;
            double d2 = ((Entity) (entityplayer)).field_320_n - field_320_n;
            double d3 = d * d + d1 * d1 + d2 * d2;
            if(func_25020_s() && d3 > 16384D)
            {
                func_118_j();
            }
            if(field_9132_bn > 600 && field_9064_W.nextInt(800) == 0 && d3 > 1024D && func_25020_s())
            {
                func_118_j();
            } else
            if(d3 < 1024D)
            {
                field_9132_bn = 0;
            }
        }
    }

    protected void func_46015_av()
    {
        field_9132_bn++;
        Profiler.func_40518_a("checkDespawn");
        func_27013_Q();
        Profiler.func_40517_a();
        Profiler.func_40518_a("sensing");
        field_48343_m.func_48547_a();
        Profiler.func_40517_a();
        Profiler.func_40518_a("targetSelector");
        field_48337_aM.func_46133_a();
        Profiler.func_40517_a();
        Profiler.func_40518_a("goalSelector");
        field_46025_aM.func_46133_a();
        Profiler.func_40517_a();
        Profiler.func_40518_a("navigation");
        field_48345_k.func_46032_a();
        Profiler.func_40517_a();
        Profiler.func_40518_a("mob tick");
        func_48326_g();
        Profiler.func_40517_a();
        Profiler.func_40518_a("controls");
        field_46031_e.func_46072_a();
        field_46030_d.func_46059_a();
        field_46028_f.func_46116_b();
        Profiler.func_40517_a();
    }

    protected void func_48326_g()
    {
    }

    protected void func_152_d_()
    {
        field_9132_bn++;
        func_27013_Q();
        field_9131_bo = 0.0F;
        field_9130_bp = 0.0F;
        float f = 8F;
        if(field_9064_W.nextFloat() < 0.02F)
        {
            EntityPlayer entityplayer = field_9093_l.func_472_a(this, f);
            if(entityplayer != null)
            {
                field_4098_b = entityplayer;
                field_4104_c = 10 + field_9064_W.nextInt(20);
            } else
            {
                field_9129_bq = (field_9064_W.nextFloat() - 0.5F) * 20F;
            }
        }
        if(field_4098_b != null)
        {
            func_147_b(field_4098_b, 10F, func_25018_n_());
            if(field_4104_c-- <= 0 || field_4098_b.field_304_B || field_4098_b.func_102_b(this) > (double)(f * f))
            {
                field_4098_b = null;
            }
        } else
        {
            if(field_9064_W.nextFloat() < 0.05F)
            {
                field_9129_bq = (field_9064_W.nextFloat() - 0.5F) * 20F;
            }
            field_316_r += field_9129_bq;
            field_315_s = field_9127_bs;
        }
        boolean flag = func_27011_Z();
        boolean flag1 = func_112_q();
        if(flag || flag1)
        {
            field_9128_br = field_9064_W.nextFloat() < 0.8F;
        }
    }

    public int func_25018_n_()
    {
        return 40;
    }

    public void func_147_b(Entity p_147_1_, float p_147_2_, float p_147_3_)
    {
        double d = p_147_1_.field_322_l - field_322_l;
        double d2 = p_147_1_.field_320_n - field_320_n;
        double d1;
        if(p_147_1_ instanceof EntityLiving)
        {
            EntityLiving entityliving = (EntityLiving)p_147_1_;
            d1 = (field_321_m + (double)func_104_p()) - (entityliving.field_321_m + (double)entityliving.func_104_p());
        } else
        {
            d1 = (p_147_1_.field_312_v.field_963_b + p_147_1_.field_312_v.field_968_e) / 2D - (field_321_m + (double)func_104_p());
        }
        double d3 = MathHelper.func_583_a(d * d + d2 * d2);
        float f = (float)((Math.atan2(d2, d) * 180D) / 3.1415927410125732D) - 90F;
        float f1 = (float)(-((Math.atan2(d1, d3) * 180D) / 3.1415927410125732D));
        field_315_s = -func_140_b(field_315_s, f1, p_147_3_);
        field_316_r = func_140_b(field_316_r, f, p_147_2_);
    }

    private float func_140_b(float p_140_1_, float p_140_2_, float p_140_3_)
    {
        float f;
        for(f = p_140_2_ - p_140_1_; f < -180F; f += 360F) { }
        for(; f >= 180F; f -= 360F) { }
        if(f > p_140_3_)
        {
            f = p_140_3_;
        }
        if(f < -p_140_3_)
        {
            f = -p_140_3_;
        }
        return p_140_1_ + f;
    }

    public void func_6101_K()
    {
    }

    public boolean func_155_a()
    {
        return field_9093_l.func_522_a(field_312_v) && field_9093_l.func_481_a(this, field_312_v).size() == 0 && !field_9093_l.func_469_b(field_312_v);
    }

    protected void func_4043_o()
    {
        func_121_a(DamageSource.field_35099_i, 4);
    }

    public Vec3D func_4039_B()
    {
        return func_141_d(1.0F);
    }

    public Vec3D func_141_d(float p_141_1_)
    {
        if(p_141_1_ == 1.0F)
        {
            float f = MathHelper.func_582_b(-field_316_r * 0.01745329F - 3.141593F);
            float f2 = MathHelper.func_585_a(-field_316_r * 0.01745329F - 3.141593F);
            float f4 = -MathHelper.func_582_b(-field_315_s * 0.01745329F);
            float f6 = MathHelper.func_585_a(-field_315_s * 0.01745329F);
            return Vec3D.func_768_b(f2 * f4, f6, f * f4);
        } else
        {
            float f1 = field_9088_y + (field_315_s - field_9088_y) * p_141_1_;
            float f3 = field_9089_x + (field_316_r - field_9089_x) * p_141_1_;
            float f5 = MathHelper.func_582_b(-f3 * 0.01745329F - 3.141593F);
            float f7 = MathHelper.func_585_a(-f3 * 0.01745329F - 3.141593F);
            float f8 = -MathHelper.func_582_b(-f1 * 0.01745329F);
            float f9 = MathHelper.func_585_a(-f1 * 0.01745329F);
            return Vec3D.func_768_b(f7 * f8, f9, f5 * f8);
        }
    }

    public int func_4045_i()
    {
        return 4;
    }

    public boolean func_22057_E()
    {
        return false;
    }

    protected void func_35186_aj()
    {
        Iterator iterator = field_35191_aF.keySet().iterator();
        do
        {
            if(!iterator.hasNext())
            {
                break;
            }
            Integer integer = (Integer)iterator.next();
            PotionEffect potioneffect = (PotionEffect)field_35191_aF.get(integer);
            if(!potioneffect.func_35648_a(this) && !field_9093_l.field_792_x)
            {
                iterator.remove();
                func_35185_c(potioneffect);
            }
        } while(true);
        if(field_39002_b)
        {
            if(!field_9093_l.field_792_x)
            {
                if(!field_35191_aF.isEmpty())
                {
                    int i = PotionHelper.func_40553_a(field_35191_aF.values());
                    field_21045_af.func_21155_b(8, Integer.valueOf(i));
                } else
                {
                    field_21045_af.func_21155_b(8, Integer.valueOf(0));
                }
            }
            field_39002_b = false;
        }
        if(field_9064_W.nextBoolean())
        {
            int j = field_21045_af.func_25075_b(8);
            if(j > 0)
            {
                double d = (double)(j >> 16 & 0xff) / 255D;
                double d1 = (double)(j >> 8 & 0xff) / 255D;
                double d2 = (double)(j >> 0 & 0xff) / 255D;
                field_9093_l.func_514_a("mobSpell", field_322_l + (field_9064_W.nextDouble() - 0.5D) * (double)field_300_D, (field_321_m + field_9064_W.nextDouble() * (double)field_298_E) - (double)field_9076_H, field_320_n + (field_9064_W.nextDouble() - 0.5D) * (double)field_300_D, d, d1, d2);
            }
        }
    }

    public void func_40089_ar()
    {
        Iterator iterator = field_35191_aF.keySet().iterator();
        do
        {
            if(!iterator.hasNext())
            {
                break;
            }
            Integer integer = (Integer)iterator.next();
            PotionEffect potioneffect = (PotionEffect)field_35191_aF.get(integer);
            if(!field_9093_l.field_792_x)
            {
                iterator.remove();
                func_35185_c(potioneffect);
            }
        } while(true);
    }

    public Collection func_35183_ak()
    {
        return field_35191_aF.values();
    }

    public boolean func_35184_a(Potion p_35184_1_)
    {
        return field_35191_aF.containsKey(Integer.valueOf(p_35184_1_.field_35447_H));
    }

    public PotionEffect func_35187_b(Potion p_35187_1_)
    {
        return (PotionEffect)field_35191_aF.get(Integer.valueOf(p_35187_1_.field_35447_H));
    }

    public void func_35182_d(PotionEffect p_35182_1_)
    {
        if(!func_40096_a(p_35182_1_))
        {
            return;
        }
        if(field_35191_aF.containsKey(Integer.valueOf(p_35182_1_.func_35649_a())))
        {
            ((PotionEffect)field_35191_aF.get(Integer.valueOf(p_35182_1_.func_35649_a()))).func_35650_a(p_35182_1_);
            func_35179_b((PotionEffect)field_35191_aF.get(Integer.valueOf(p_35182_1_.func_35649_a())));
        } else
        {
            field_35191_aF.put(Integer.valueOf(p_35182_1_.func_35649_a()), p_35182_1_);
            func_35181_a(p_35182_1_);
        }
    }

    public boolean func_40096_a(PotionEffect p_40096_1_)
    {
        if(func_40093_t() == EnumCreatureAttribute.UNDEAD)
        {
            int i = p_40096_1_.func_35649_a();
            if(i == Potion.field_35458_l.field_35447_H || i == Potion.field_35466_u.field_35447_H)
            {
                return false;
            }
        }
        return true;
    }

    public boolean func_40100_at()
    {
        return func_40093_t() == EnumCreatureAttribute.UNDEAD;
    }

    protected void func_35181_a(PotionEffect p_35181_1_)
    {
        field_39002_b = true;
    }

    protected void func_35179_b(PotionEffect p_35179_1_)
    {
        field_39002_b = true;
    }

    protected void func_35185_c(PotionEffect p_35185_1_)
    {
        field_39002_b = true;
    }

    protected float func_35178_D()
    {
        float f = 1.0F;
        if(func_35184_a(Potion.field_35454_c))
        {
            f *= 1.0F + 0.2F * (float)(func_35187_b(Potion.field_35454_c).func_35652_c() + 1);
        }
        if(func_35184_a(Potion.field_35451_d))
        {
            f *= 1.0F - 0.15F * (float)(func_35187_b(Potion.field_35451_d).func_35652_c() + 1);
        }
        return f;
    }

    public void func_40098_a_(double p_40098_1_, double p_40098_3_, double p_40098_5_)
    {
        func_107_c(p_40098_1_, p_40098_3_, p_40098_5_, field_316_r, field_315_s);
    }

    public boolean func_40104_l()
    {
        return false;
    }

    public EnumCreatureAttribute func_40093_t()
    {
        return EnumCreatureAttribute.UNDEFINED;
    }

    public void func_41030_c(ItemStack p_41030_1_)
    {
        field_9093_l.func_506_a(this, "random.break", 0.8F, 0.8F + field_9093_l.field_803_m.nextFloat() * 0.4F);
        for(int i = 0; i < 5; i++)
        {
            Vec3D vec3d = Vec3D.func_768_b(((double)field_9064_W.nextFloat() - 0.5D) * 0.10000000000000001D, Math.random() * 0.10000000000000001D + 0.10000000000000001D, 0.0D);
            vec3d.func_35571_a((-field_315_s * 3.141593F) / 180F);
            vec3d.func_35572_b((-field_316_r * 3.141593F) / 180F);
            Vec3D vec3d1 = Vec3D.func_768_b(((double)field_9064_W.nextFloat() - 0.5D) * 0.29999999999999999D, (double)(-field_9064_W.nextFloat()) * 0.59999999999999998D - 0.29999999999999999D, 0.59999999999999998D);
            vec3d1.func_35571_a((-field_315_s * 3.141593F) / 180F);
            vec3d1.func_35572_b((-field_316_r * 3.141593F) / 180F);
            vec3d1 = vec3d1.func_757_c(field_322_l, field_321_m + (double)func_104_p(), field_320_n);
            field_9093_l.func_514_a((new StringBuilder()).append("iconcrack_").append(p_41030_1_.func_569_a().field_234_aS).toString(), vec3d1.field_1055_a, vec3d1.field_1054_b, vec3d1.field_1058_c, vec3d.field_1055_a, vec3d.field_1054_b + 0.050000000000000003D, vec3d.field_1058_c);
        }

    }
}

