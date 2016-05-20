// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst 

package net.minecraft.src;

import java.util.List;
import java.util.Random;

// Referenced classes of package net.minecraft.src:
//            AxisAlignedBB, DataWatcher, Profiler, MathHelper, 
//            World, DamageSource, EntityPlayer, Block, 
//            StepSound, Material, EntityLiving, BlockFluid, 
//            NBTTagCompound, NBTTagList, NBTTagDouble, NBTTagFloat, 
//            EntityList, ItemStack, EntityItem, StatCollector, 
//            Vec3D, EntityLightningBolt

public abstract class Entity
{

    private static int field_384_a = 0;
    public int field_331_c;
    public double field_9094_h;
    public boolean field_329_e;
    public Entity field_328_f;
    public Entity field_327_g;
    public World field_9093_l;
    public double field_9092_m;
    public double field_9091_n;
    public double field_9090_o;
    public double field_322_l;
    public double field_321_m;
    public double field_320_n;
    public double field_319_o;
    public double field_318_p;
    public double field_317_q;
    public float field_316_r;
    public float field_315_s;
    public float field_9089_x;
    public float field_9088_y;
    public final AxisAlignedBB field_312_v = AxisAlignedBB.func_698_a(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    public boolean field_9086_A;
    public boolean field_9084_B;
    public boolean field_9082_C;
    public boolean field_9080_D;
    public boolean field_9078_E;
    protected boolean field_27012_bb;
    public boolean field_9077_F;
    public boolean field_304_B;
    public float field_9076_H;
    public float field_300_D;
    public float field_298_E;
    public float field_9075_K;
    public float field_9074_L;
    public float field_9072_N;
    private int field_6151_b;
    public double field_9071_O;
    public double field_9070_P;
    public double field_9069_Q;
    public float field_9068_R;
    public float field_9067_S;
    public boolean field_9066_T;
    public float field_286_P;
    protected Random field_9064_W;
    public int field_9063_X;
    public int field_9062_Y;
    private int field_9061_Z;
    protected boolean field_9085_ab;
    public int field_9083_ac;
    private boolean field_4131_c;
    protected boolean field_9079_ae;
    protected DataWatcher field_21045_af;
    private double field_4130_d;
    private double field_4128_e;
    public boolean field_276_Z;
    public int field_307_aa;
    public int field_305_ab;
    public int field_303_ac;
    public boolean field_28008_bI;
    public boolean field_35151_ca;

    public Entity(World p_i444_1_)
    {
        field_331_c = field_384_a++;
        field_9094_h = 1.0D;
        field_329_e = false;
        field_9086_A = false;
        field_9080_D = false;
        field_9078_E = false;
        field_9077_F = true;
        field_304_B = false;
        field_9076_H = 0.0F;
        field_300_D = 0.6F;
        field_298_E = 1.8F;
        field_9075_K = 0.0F;
        field_9074_L = 0.0F;
        field_9072_N = 0.0F;
        field_6151_b = 1;
        field_9068_R = 0.0F;
        field_9067_S = 0.0F;
        field_9066_T = false;
        field_286_P = 0.0F;
        field_9064_W = new Random();
        field_9063_X = 0;
        field_9062_Y = 1;
        field_9061_Z = 0;
        field_9085_ab = false;
        field_9083_ac = 0;
        field_4131_c = true;
        field_9079_ae = false;
        field_21045_af = new DataWatcher();
        field_276_Z = false;
        field_9093_l = p_i444_1_;
        func_86_a(0.0D, 0.0D, 0.0D);
        field_21045_af.func_21153_a(0, Byte.valueOf((byte)0));
        field_21045_af.func_21153_a(1, Short.valueOf((short)300));
        func_21044_a();
    }

    protected abstract void func_21044_a();

    public DataWatcher func_21039_p()
    {
        return field_21045_af;
    }

    public boolean equals(Object p_equals_1_)
    {
        if(p_equals_1_ instanceof Entity)
        {
            return ((Entity)p_equals_1_).field_331_c == field_331_c;
        } else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return field_331_c;
    }

    public void func_118_j()
    {
        field_304_B = true;
    }

    protected void func_113_a(float p_113_1_, float p_113_2_)
    {
        field_300_D = p_113_1_;
        field_298_E = p_113_2_;
    }

    protected void func_6096_b(float p_6096_1_, float p_6096_2_)
    {
        field_316_r = p_6096_1_ % 360F;
        field_315_s = p_6096_2_ % 360F;
    }

    public void func_86_a(double p_86_1_, double p_86_3_, double p_86_5_)
    {
        field_322_l = p_86_1_;
        field_321_m = p_86_3_;
        field_320_n = p_86_5_;
        float f = field_300_D / 2.0F;
        float f1 = field_298_E;
        field_312_v.func_695_c(p_86_1_ - (double)f, (p_86_3_ - (double)field_9076_H) + (double)field_9068_R, p_86_5_ - (double)f, p_86_1_ + (double)f, (p_86_3_ - (double)field_9076_H) + (double)field_9068_R + (double)f1, p_86_5_ + (double)f);
    }

    public void func_106_b_()
    {
        func_84_k();
    }

    public void func_84_k()
    {
        Profiler.func_40518_a("entityBaseTick");
        if(field_327_g != null && field_327_g.field_304_B)
        {
            field_327_g = null;
        }
        field_9063_X++;
        field_9075_K = field_9074_L;
        field_9092_m = field_322_l;
        field_9091_n = field_321_m;
        field_9090_o = field_320_n;
        field_9088_y = field_315_s;
        field_9089_x = field_316_r;
        if(func_35149_at() && !func_27011_Z())
        {
            int i = MathHelper.func_584_b(field_322_l);
            int j = MathHelper.func_584_b(field_321_m - 0.20000000298023224D - (double)field_9076_H);
            int k = MathHelper.func_584_b(field_320_n);
            int j1 = field_9093_l.func_444_a(i, j, k);
            if(j1 > 0)
            {
                field_9093_l.func_514_a((new StringBuilder()).append("tilecrack_").append(j1).toString(), field_322_l + ((double)field_9064_W.nextFloat() - 0.5D) * (double)field_300_D, field_312_v.field_963_b + 0.10000000000000001D, field_320_n + ((double)field_9064_W.nextFloat() - 0.5D) * (double)field_300_D, -field_319_o * 4D, 1.5D, -field_317_q * 4D);
            }
        }
        if(func_119_o())
        {
            if(!field_9085_ab && !field_4131_c)
            {
                float f = MathHelper.func_583_a(field_319_o * field_319_o * 0.20000000298023224D + field_318_p * field_318_p + field_317_q * field_317_q * 0.20000000298023224D) * 0.2F;
                if(f > 1.0F)
                {
                    f = 1.0F;
                }
                field_9093_l.func_506_a(this, "random.splash", f, 1.0F + (field_9064_W.nextFloat() - field_9064_W.nextFloat()) * 0.4F);
                float f1 = MathHelper.func_584_b(field_312_v.field_963_b);
                for(int l = 0; (float)l < 1.0F + field_300_D * 20F; l++)
                {
                    float f2 = (field_9064_W.nextFloat() * 2.0F - 1.0F) * field_300_D;
                    float f4 = (field_9064_W.nextFloat() * 2.0F - 1.0F) * field_300_D;
                    field_9093_l.func_514_a("bubble", field_322_l + (double)f2, f1 + 1.0F, field_320_n + (double)f4, field_319_o, field_318_p - (double)(field_9064_W.nextFloat() * 0.2F), field_317_q);
                }

                for(int i1 = 0; (float)i1 < 1.0F + field_300_D * 20F; i1++)
                {
                    float f3 = (field_9064_W.nextFloat() * 2.0F - 1.0F) * field_300_D;
                    float f5 = (field_9064_W.nextFloat() * 2.0F - 1.0F) * field_300_D;
                    field_9093_l.func_514_a("splash", field_322_l + (double)f3, f1 + 1.0F, field_320_n + (double)f5, field_319_o, field_318_p, field_317_q);
                }

            }
            field_9072_N = 0.0F;
            field_9085_ab = true;
            field_9061_Z = 0;
        } else
        {
            field_9085_ab = false;
        }
        if(field_9093_l.field_792_x)
        {
            field_9061_Z = 0;
        } else
        if(field_9061_Z > 0)
        {
            if(field_9079_ae)
            {
                field_9061_Z -= 4;
                if(field_9061_Z < 0)
                {
                    field_9061_Z = 0;
                }
            } else
            {
                if(field_9061_Z % 20 == 0)
                {
                    func_121_a(DamageSource.field_35089_b, 1);
                }
                field_9061_Z--;
            }
        }
        if(func_112_q())
        {
            func_4040_n();
            field_9072_N *= 0.5F;
        }
        if(field_321_m < -64D)
        {
            func_4043_o();
        }
        if(!field_9093_l.field_792_x)
        {
            func_21041_a(0, field_9061_Z > 0);
            func_21041_a(2, field_327_g != null);
        }
        field_4131_c = false;
        Profiler.func_40517_a();
    }

    protected void func_4040_n()
    {
        if(!field_9079_ae)
        {
            func_121_a(DamageSource.field_35090_c, 4);
            func_40034_j(15);
        }
    }

    public void func_40034_j(int p_40034_1_)
    {
        int i = p_40034_1_ * 20;
        if(field_9061_Z < i)
        {
            field_9061_Z = i;
        }
    }

    public void func_40036_aw()
    {
        field_9061_Z = 0;
    }

    protected void func_4043_o()
    {
        func_118_j();
    }

    public boolean func_133_b(double p_133_1_, double p_133_3_, double p_133_5_)
    {
        AxisAlignedBB axisalignedbb = field_312_v.func_696_c(p_133_1_, p_133_3_, p_133_5_);
        List list = field_9093_l.func_481_a(this, axisalignedbb);
        if(list.size() > 0)
        {
            return false;
        }
        return !field_9093_l.func_469_b(axisalignedbb);
    }

    public void func_88_c(double p_88_1_, double p_88_3_, double p_88_5_)
    {
        if(field_9066_T)
        {
            field_312_v.func_702_d(p_88_1_, p_88_3_, p_88_5_);
            field_322_l = (field_312_v.field_964_a + field_312_v.field_969_d) / 2D;
            field_321_m = (field_312_v.field_963_b + (double)field_9076_H) - (double)field_9068_R;
            field_320_n = (field_312_v.field_970_c + field_312_v.field_967_f) / 2D;
            return;
        }
        Profiler.func_40518_a("move");
        field_9068_R *= 0.4F;
        double d = field_322_l;
        double d1 = field_320_n;
        if(field_27012_bb)
        {
            field_27012_bb = false;
            p_88_1_ *= 0.25D;
            p_88_3_ *= 0.05000000074505806D;
            p_88_5_ *= 0.25D;
            field_319_o = 0.0D;
            field_318_p = 0.0D;
            field_317_q = 0.0D;
        }
        double d2 = p_88_1_;
        double d3 = p_88_3_;
        double d4 = p_88_5_;
        AxisAlignedBB axisalignedbb = field_312_v.func_711_b();
        boolean flag = field_9086_A && func_9059_p() && (this instanceof EntityPlayer);
        if(flag)
        {
            double d5 = 0.050000000000000003D;
            for(; p_88_1_ != 0.0D && field_9093_l.func_481_a(this, field_312_v.func_696_c(p_88_1_, -1D, 0.0D)).size() == 0; d2 = p_88_1_)
            {
                if(p_88_1_ < d5 && p_88_1_ >= -d5)
                {
                    p_88_1_ = 0.0D;
                    continue;
                }
                if(p_88_1_ > 0.0D)
                {
                    p_88_1_ -= d5;
                } else
                {
                    p_88_1_ += d5;
                }
            }

            for(; p_88_5_ != 0.0D && field_9093_l.func_481_a(this, field_312_v.func_696_c(0.0D, -1D, p_88_5_)).size() == 0; d4 = p_88_5_)
            {
                if(p_88_5_ < d5 && p_88_5_ >= -d5)
                {
                    p_88_5_ = 0.0D;
                    continue;
                }
                if(p_88_5_ > 0.0D)
                {
                    p_88_5_ -= d5;
                } else
                {
                    p_88_5_ += d5;
                }
            }

            while(p_88_1_ != 0.0D && p_88_5_ != 0.0D && field_9093_l.func_481_a(this, field_312_v.func_696_c(p_88_1_, -1D, p_88_5_)).size() == 0) 
            {
                if(p_88_1_ < d5 && p_88_1_ >= -d5)
                {
                    p_88_1_ = 0.0D;
                } else
                if(p_88_1_ > 0.0D)
                {
                    p_88_1_ -= d5;
                } else
                {
                    p_88_1_ += d5;
                }
                if(p_88_5_ < d5 && p_88_5_ >= -d5)
                {
                    p_88_5_ = 0.0D;
                } else
                if(p_88_5_ > 0.0D)
                {
                    p_88_5_ -= d5;
                } else
                {
                    p_88_5_ += d5;
                }
                d2 = p_88_1_;
                d4 = p_88_5_;
            }
        }
        List list = field_9093_l.func_481_a(this, field_312_v.func_700_a(p_88_1_, p_88_3_, p_88_5_));
        for(int i = 0; i < list.size(); i++)
        {
            p_88_3_ = ((AxisAlignedBB)list.get(i)).func_701_b(field_312_v, p_88_3_);
        }

        field_312_v.func_702_d(0.0D, p_88_3_, 0.0D);
        if(!field_9077_F && d3 != p_88_3_)
        {
            p_88_1_ = p_88_3_ = p_88_5_ = 0.0D;
        }
        boolean flag1 = field_9086_A || d3 != p_88_3_ && d3 < 0.0D;
        for(int j = 0; j < list.size(); j++)
        {
            p_88_1_ = ((AxisAlignedBB)list.get(j)).func_710_a(field_312_v, p_88_1_);
        }

        field_312_v.func_702_d(p_88_1_, 0.0D, 0.0D);
        if(!field_9077_F && d2 != p_88_1_)
        {
            p_88_1_ = p_88_3_ = p_88_5_ = 0.0D;
        }
        for(int k = 0; k < list.size(); k++)
        {
            p_88_5_ = ((AxisAlignedBB)list.get(k)).func_709_c(field_312_v, p_88_5_);
        }

        field_312_v.func_702_d(0.0D, 0.0D, p_88_5_);
        if(!field_9077_F && d4 != p_88_5_)
        {
            p_88_1_ = p_88_3_ = p_88_5_ = 0.0D;
        }
        if(field_9067_S > 0.0F && flag1 && (flag || field_9068_R < 0.05F) && (d2 != p_88_1_ || d4 != p_88_5_))
        {
            double d6 = p_88_1_;
            double d8 = p_88_3_;
            double d10 = p_88_5_;
            p_88_1_ = d2;
            p_88_3_ = field_9067_S;
            p_88_5_ = d4;
            AxisAlignedBB axisalignedbb1 = field_312_v.func_711_b();
            field_312_v.func_699_b(axisalignedbb);
            List list1 = field_9093_l.func_481_a(this, field_312_v.func_700_a(p_88_1_, p_88_3_, p_88_5_));
            for(int j2 = 0; j2 < list1.size(); j2++)
            {
                p_88_3_ = ((AxisAlignedBB)list1.get(j2)).func_701_b(field_312_v, p_88_3_);
            }

            field_312_v.func_702_d(0.0D, p_88_3_, 0.0D);
            if(!field_9077_F && d3 != p_88_3_)
            {
                p_88_1_ = p_88_3_ = p_88_5_ = 0.0D;
            }
            for(int k2 = 0; k2 < list1.size(); k2++)
            {
                p_88_1_ = ((AxisAlignedBB)list1.get(k2)).func_710_a(field_312_v, p_88_1_);
            }

            field_312_v.func_702_d(p_88_1_, 0.0D, 0.0D);
            if(!field_9077_F && d2 != p_88_1_)
            {
                p_88_1_ = p_88_3_ = p_88_5_ = 0.0D;
            }
            for(int l2 = 0; l2 < list1.size(); l2++)
            {
                p_88_5_ = ((AxisAlignedBB)list1.get(l2)).func_709_c(field_312_v, p_88_5_);
            }

            field_312_v.func_702_d(0.0D, 0.0D, p_88_5_);
            if(!field_9077_F && d4 != p_88_5_)
            {
                p_88_1_ = p_88_3_ = p_88_5_ = 0.0D;
            }
            if(!field_9077_F && d3 != p_88_3_)
            {
                p_88_1_ = p_88_3_ = p_88_5_ = 0.0D;
            } else
            {
                p_88_3_ = -field_9067_S;
                for(int i3 = 0; i3 < list1.size(); i3++)
                {
                    p_88_3_ = ((AxisAlignedBB)list1.get(i3)).func_701_b(field_312_v, p_88_3_);
                }

                field_312_v.func_702_d(0.0D, p_88_3_, 0.0D);
            }
            if(d6 * d6 + d10 * d10 >= p_88_1_ * p_88_1_ + p_88_5_ * p_88_5_)
            {
                p_88_1_ = d6;
                p_88_3_ = d8;
                p_88_5_ = d10;
                field_312_v.func_699_b(axisalignedbb1);
            } else
            {
                double d11 = field_312_v.field_963_b - (double)(int)field_312_v.field_963_b;
                if(d11 > 0.0D)
                {
                    field_9068_R += d11 + 0.01D;
                }
            }
        }
        Profiler.func_40517_a();
        Profiler.func_40518_a("rest");
        field_322_l = (field_312_v.field_964_a + field_312_v.field_969_d) / 2D;
        field_321_m = (field_312_v.field_963_b + (double)field_9076_H) - (double)field_9068_R;
        field_320_n = (field_312_v.field_970_c + field_312_v.field_967_f) / 2D;
        field_9084_B = d2 != p_88_1_ || d4 != p_88_5_;
        field_9082_C = d3 != p_88_3_;
        field_9086_A = d3 != p_88_3_ && d3 < 0.0D;
        field_9080_D = field_9084_B || field_9082_C;
        func_122_m(p_88_3_, field_9086_A);
        if(d2 != p_88_1_)
        {
            field_319_o = 0.0D;
        }
        if(d3 != p_88_3_)
        {
            field_318_p = 0.0D;
        }
        if(d4 != p_88_5_)
        {
            field_317_q = 0.0D;
        }
        double d7 = field_322_l - d;
        double d9 = field_320_n - d1;
        if(func_25017_l() && !flag && field_327_g == null)
        {
            field_9074_L += (double)MathHelper.func_583_a(d7 * d7 + d9 * d9) * 0.59999999999999998D;
            int l = MathHelper.func_584_b(field_322_l);
            int j1 = MathHelper.func_584_b(field_321_m - 0.20000000298023224D - (double)field_9076_H);
            int l1 = MathHelper.func_584_b(field_320_n);
            int j3 = field_9093_l.func_444_a(l, j1, l1);
            if(j3 == 0 && field_9093_l.func_444_a(l, j1 - 1, l1) == Block.field_9050_aZ.field_573_bc)
            {
                j3 = field_9093_l.func_444_a(l, j1 - 1, l1);
            }
            if(field_9074_L > (float)field_6151_b && j3 > 0)
            {
                field_6151_b = (int)field_9074_L + 1;
                func_41011_a(l, j1, l1, j3);
                Block.field_542_n[j3].func_249_b(field_9093_l, l, j1, l1, this);
            }
        }
        int i1 = MathHelper.func_584_b(field_312_v.field_964_a + 0.001D);
        int k1 = MathHelper.func_584_b(field_312_v.field_963_b + 0.001D);
        int i2 = MathHelper.func_584_b(field_312_v.field_970_c + 0.001D);
        int k3 = MathHelper.func_584_b(field_312_v.field_969_d - 0.001D);
        int l3 = MathHelper.func_584_b(field_312_v.field_968_e - 0.001D);
        int i4 = MathHelper.func_584_b(field_312_v.field_967_f - 0.001D);
        if(field_9093_l.func_466_a(i1, k1, i2, k3, l3, i4))
        {
            for(int j4 = i1; j4 <= k3; j4++)
            {
                for(int k4 = k1; k4 <= l3; k4++)
                {
                    for(int l4 = i2; l4 <= i4; l4++)
                    {
                        int i5 = field_9093_l.func_444_a(j4, k4, l4);
                        if(i5 > 0)
                        {
                            Block.field_542_n[i5].func_263_a(field_9093_l, j4, k4, l4, this);
                        }
                    }

                }

            }

        }
        boolean flag2 = func_27008_Y();
        if(field_9093_l.func_523_c(field_312_v.func_694_e(0.001D, 0.001D, 0.001D)))
        {
            func_125_b(1);
            if(!flag2)
            {
                field_9061_Z++;
                if(field_9061_Z == 0)
                {
                    func_40034_j(8);
                }
            }
        } else
        if(field_9061_Z <= 0)
        {
            field_9061_Z = -field_9062_Y;
        }
        if(flag2 && field_9061_Z > 0)
        {
            field_9093_l.func_506_a(this, "random.fizz", 0.7F, 1.6F + (field_9064_W.nextFloat() - field_9064_W.nextFloat()) * 0.4F);
            field_9061_Z = -field_9062_Y;
        }
        Profiler.func_40517_a();
    }

    protected void func_41011_a(int p_41011_1_, int p_41011_2_, int p_41011_3_, int p_41011_4_)
    {
        StepSound stepsound = Block.field_542_n[p_41011_4_].field_555_bl;
        if(field_9093_l.func_444_a(p_41011_1_, p_41011_2_ + 1, p_41011_3_) == Block.field_625_aT.field_573_bc)
        {
            stepsound = Block.field_625_aT.field_555_bl;
            field_9093_l.func_506_a(this, stepsound.func_737_c(), stepsound.func_738_a() * 0.15F, stepsound.func_739_b());
        } else
        if(!Block.field_542_n[p_41011_4_].field_553_bn.func_217_d())
        {
            field_9093_l.func_506_a(this, stepsound.func_737_c(), stepsound.func_738_a() * 0.15F, stepsound.func_739_b());
        }
    }

    protected boolean func_25017_l()
    {
        return true;
    }

    protected void func_122_m(double p_122_1_, boolean p_122_3_)
    {
        if(p_122_3_)
        {
            if(field_9072_N > 0.0F)
            {
                if(this instanceof EntityLiving)
                {
                    int i = MathHelper.func_584_b(field_322_l);
                    int j = MathHelper.func_584_b(field_321_m - 0.20000000298023224D - (double)field_9076_H);
                    int k = MathHelper.func_584_b(field_320_n);
                    int l = field_9093_l.func_444_a(i, j, k);
                    if(l == 0 && field_9093_l.func_444_a(i, j - 1, k) == Block.field_9050_aZ.field_573_bc)
                    {
                        l = field_9093_l.func_444_a(i, j - 1, k);
                    }
                    if(l > 0)
                    {
                        Block.field_542_n[l].func_43001_a(field_9093_l, i, j, k, this, field_9072_N);
                    }
                }
                func_114_a(field_9072_N);
                field_9072_N = 0.0F;
            }
        } else
        if(p_122_1_ < 0.0D)
        {
            field_9072_N -= p_122_1_;
        }
    }

    public AxisAlignedBB func_93_n()
    {
        return null;
    }

    protected void func_125_b(int p_125_1_)
    {
        if(!field_9079_ae)
        {
            func_121_a(DamageSource.field_35091_a, p_125_1_);
        }
    }

    public final boolean func_40033_ax()
    {
        return field_9079_ae;
    }

    protected void func_114_a(float p_114_1_)
    {
        if(field_328_f != null)
        {
            field_328_f.func_114_a(p_114_1_);
        }
    }

    public boolean func_27008_Y()
    {
        return field_9085_ab || field_9093_l.func_27072_q(MathHelper.func_584_b(field_322_l), MathHelper.func_584_b(field_321_m), MathHelper.func_584_b(field_320_n));
    }

    public boolean func_27011_Z()
    {
        return field_9085_ab;
    }

    public boolean func_119_o()
    {
        return field_9093_l.func_490_a(field_312_v.func_708_b(0.0D, -0.40000000596046448D, 0.0D).func_694_e(0.001D, 0.001D, 0.001D), Material.field_521_f, this);
    }

    public boolean func_110_a(Material p_110_1_)
    {
        double d = field_321_m + (double)func_104_p();
        int i = MathHelper.func_584_b(field_322_l);
        int j = MathHelper.func_588_d(MathHelper.func_584_b(d));
        int k = MathHelper.func_584_b(field_320_n);
        int l = field_9093_l.func_444_a(i, j, k);
        if(l != 0 && Block.field_542_n[l].field_553_bn == p_110_1_)
        {
            float f = BlockFluid.func_299_b(field_9093_l.func_446_b(i, j, k)) - 0.1111111F;
            float f1 = (float)(j + 1) - f;
            return d < (double)f1;
        } else
        {
            return false;
        }
    }

    public float func_104_p()
    {
        return 0.0F;
    }

    public boolean func_112_q()
    {
        return field_9093_l.func_500_a(field_312_v.func_708_b(-0.10000000149011612D, -0.40000000596046448D, -0.10000000149011612D), Material.field_520_g);
    }

    public void func_90_a(float p_90_1_, float p_90_2_, float p_90_3_)
    {
        float f = MathHelper.func_586_c(p_90_1_ * p_90_1_ + p_90_2_ * p_90_2_);
        if(f < 0.01F)
        {
            return;
        }
        if(f < 1.0F)
        {
            f = 1.0F;
        }
        f = p_90_3_ / f;
        p_90_1_ *= f;
        p_90_2_ *= f;
        float f1 = MathHelper.func_585_a((field_316_r * 3.141593F) / 180F);
        float f2 = MathHelper.func_582_b((field_316_r * 3.141593F) / 180F);
        field_319_o += p_90_1_ * f2 - p_90_2_ * f1;
        field_317_q += p_90_2_ * f2 + p_90_1_ * f1;
    }

    public float func_108_b(float p_108_1_)
    {
        int i = MathHelper.func_584_b(field_322_l);
        int j = MathHelper.func_584_b(field_320_n);
        if(field_9093_l.func_530_e(i, 0, j))
        {
            double d = (field_312_v.field_968_e - field_312_v.field_963_b) * 0.66000000000000003D;
            int k = MathHelper.func_584_b((field_321_m - (double)field_9076_H) + d);
            return field_9093_l.func_455_j(i, k, j);
        } else
        {
            return 0.0F;
        }
    }

    public void func_28007_a(World p_28007_1_)
    {
        field_9093_l = p_28007_1_;
    }

    public void func_6095_b(double p_6095_1_, double p_6095_3_, double p_6095_5_, float p_6095_7_, 
            float p_6095_8_)
    {
        field_9092_m = field_322_l = p_6095_1_;
        field_9091_n = field_321_m = p_6095_3_;
        field_9090_o = field_320_n = p_6095_5_;
        field_9089_x = field_316_r = p_6095_7_;
        field_9088_y = field_315_s = p_6095_8_;
        field_9068_R = 0.0F;
        double d = field_9089_x - p_6095_7_;
        if(d < -180D)
        {
            field_9089_x += 360F;
        }
        if(d >= 180D)
        {
            field_9089_x -= 360F;
        }
        func_86_a(field_322_l, field_321_m, field_320_n);
        func_6096_b(p_6095_7_, p_6095_8_);
    }

    public void func_107_c(double p_107_1_, double p_107_3_, double p_107_5_, float p_107_7_, 
            float p_107_8_)
    {
        field_9071_O = field_9092_m = field_322_l = p_107_1_;
        field_9070_P = field_9091_n = field_321_m = p_107_3_ + (double)field_9076_H;
        field_9069_Q = field_9090_o = field_320_n = p_107_5_;
        field_316_r = p_107_7_;
        field_315_s = p_107_8_;
        func_86_a(field_322_l, field_321_m, field_320_n);
    }

    public float func_94_a(Entity p_94_1_)
    {
        float f = (float)(field_322_l - p_94_1_.field_322_l);
        float f1 = (float)(field_321_m - p_94_1_.field_321_m);
        float f2 = (float)(field_320_n - p_94_1_.field_320_n);
        return MathHelper.func_586_c(f * f + f1 * f1 + f2 * f2);
    }

    public double func_101_d(double p_101_1_, double p_101_3_, double p_101_5_)
    {
        double d = field_322_l - p_101_1_;
        double d1 = field_321_m - p_101_3_;
        double d2 = field_320_n - p_101_5_;
        return d * d + d1 * d1 + d2 * d2;
    }

    public double func_103_e(double p_103_1_, double p_103_3_, double p_103_5_)
    {
        double d = field_322_l - p_103_1_;
        double d1 = field_321_m - p_103_3_;
        double d2 = field_320_n - p_103_5_;
        return (double)MathHelper.func_583_a(d * d + d1 * d1 + d2 * d2);
    }

    public double func_102_b(Entity p_102_1_)
    {
        double d = field_322_l - p_102_1_.field_322_l;
        double d1 = field_321_m - p_102_1_.field_321_m;
        double d2 = field_320_n - p_102_1_.field_320_n;
        return d * d + d1 * d1 + d2 * d2;
    }

    public void func_6093_b(EntityPlayer entityplayer)
    {
    }

    public void func_92_c(Entity p_92_1_)
    {
        if(p_92_1_.field_328_f == this || p_92_1_.field_327_g == this)
        {
            return;
        }
        double d = p_92_1_.field_322_l - field_322_l;
        double d1 = p_92_1_.field_320_n - field_320_n;
        double d2 = MathHelper.func_581_a(d, d1);
        if(d2 >= 0.0099999997764825821D)
        {
            d2 = MathHelper.func_583_a(d2);
            d /= d2;
            d1 /= d2;
            double d3 = 1.0D / d2;
            if(d3 > 1.0D)
            {
                d3 = 1.0D;
            }
            d *= d3;
            d1 *= d3;
            d *= 0.05000000074505806D;
            d1 *= 0.05000000074505806D;
            d *= 1.0F - field_286_P;
            d1 *= 1.0F - field_286_P;
            func_87_f(-d, 0.0D, -d1);
            p_92_1_.func_87_f(d, 0.0D, d1);
        }
    }

    public void func_87_f(double p_87_1_, double p_87_3_, double p_87_5_)
    {
        field_319_o += p_87_1_;
        field_318_p += p_87_3_;
        field_317_q += p_87_5_;
        field_35151_ca = true;
    }

    protected void func_9060_u()
    {
        field_9078_E = true;
    }

    public boolean func_121_a(DamageSource p_121_1_, int p_121_2_)
    {
        func_9060_u();
        return false;
    }

    public boolean func_129_c_()
    {
        return false;
    }

    public boolean func_124_r()
    {
        return false;
    }

    public void func_96_b(Entity entity, int i)
    {
    }

    public boolean func_95_c(NBTTagCompound p_95_1_)
    {
        String s = func_109_s();
        if(field_304_B || s == null)
        {
            return false;
        } else
        {
            p_95_1_.func_403_a("id", s);
            func_98_d(p_95_1_);
            return true;
        }
    }

    public void func_98_d(NBTTagCompound p_98_1_)
    {
        p_98_1_.func_399_a("Pos", func_132_a(new double[] {
            field_322_l, field_321_m + (double)field_9068_R, field_320_n
        }));
        p_98_1_.func_399_a("Motion", func_132_a(new double[] {
            field_319_o, field_318_p, field_317_q
        }));
        p_98_1_.func_399_a("Rotation", func_85_a(new float[] {
            field_316_r, field_315_s
        }));
        p_98_1_.func_390_a("FallDistance", field_9072_N);
        p_98_1_.func_394_a("Fire", (short)field_9061_Z);
        p_98_1_.func_394_a("Air", (short)func_41009_al());
        p_98_1_.func_393_a("OnGround", field_9086_A);
        func_97_a(p_98_1_);
    }

    public void func_100_e(NBTTagCompound p_100_1_)
    {
        NBTTagList nbttaglist = p_100_1_.func_407_k("Pos");
        NBTTagList nbttaglist1 = p_100_1_.func_407_k("Motion");
        NBTTagList nbttaglist2 = p_100_1_.func_407_k("Rotation");
        field_319_o = ((NBTTagDouble)nbttaglist1.func_388_a(0)).field_735_a;
        field_318_p = ((NBTTagDouble)nbttaglist1.func_388_a(1)).field_735_a;
        field_317_q = ((NBTTagDouble)nbttaglist1.func_388_a(2)).field_735_a;
        if(Math.abs(field_319_o) > 10D)
        {
            field_319_o = 0.0D;
        }
        if(Math.abs(field_318_p) > 10D)
        {
            field_318_p = 0.0D;
        }
        if(Math.abs(field_317_q) > 10D)
        {
            field_317_q = 0.0D;
        }
        field_9092_m = field_9071_O = field_322_l = ((NBTTagDouble)nbttaglist.func_388_a(0)).field_735_a;
        field_9091_n = field_9070_P = field_321_m = ((NBTTagDouble)nbttaglist.func_388_a(1)).field_735_a;
        field_9090_o = field_9069_Q = field_320_n = ((NBTTagDouble)nbttaglist.func_388_a(2)).field_735_a;
        field_9089_x = field_316_r = ((NBTTagFloat)nbttaglist2.func_388_a(0)).field_739_a;
        field_9088_y = field_315_s = ((NBTTagFloat)nbttaglist2.func_388_a(1)).field_739_a;
        field_9072_N = p_100_1_.func_389_f("FallDistance");
        field_9061_Z = p_100_1_.func_406_c("Fire");
        func_41008_j(p_100_1_.func_406_c("Air"));
        field_9086_A = p_100_1_.func_402_l("OnGround");
        func_86_a(field_322_l, field_321_m, field_320_n);
        func_6096_b(field_316_r, field_315_s);
        func_99_b(p_100_1_);
    }

    protected final String func_109_s()
    {
        return EntityList.func_564_b(this);
    }

    protected abstract void func_99_b(NBTTagCompound nbttagcompound);

    protected abstract void func_97_a(NBTTagCompound nbttagcompound);

    protected NBTTagList func_132_a(double p_132_1_[])
    {
        NBTTagList nbttaglist = new NBTTagList();
        double ad[] = p_132_1_;
        int i = ad.length;
        for(int j = 0; j < i; j++)
        {
            double d = ad[j];
            nbttaglist.func_386_a(new NBTTagDouble(null, d));
        }

        return nbttaglist;
    }

    protected NBTTagList func_85_a(float p_85_1_[])
    {
        NBTTagList nbttaglist = new NBTTagList();
        float af[] = p_85_1_;
        int i = af.length;
        for(int j = 0; j < i; j++)
        {
            float f = af[j];
            nbttaglist.func_386_a(new NBTTagFloat(null, f));
        }

        return nbttaglist;
    }

    public EntityItem func_128_a(int p_128_1_, int p_128_2_)
    {
        return func_123_a(p_128_1_, p_128_2_, 0.0F);
    }

    public EntityItem func_123_a(int p_123_1_, int p_123_2_, float p_123_3_)
    {
        return func_21040_a(new ItemStack(p_123_1_, p_123_2_, 0), p_123_3_);
    }

    public EntityItem func_21040_a(ItemStack p_21040_1_, float p_21040_2_)
    {
        EntityItem entityitem = new EntityItem(field_9093_l, field_322_l, field_321_m + (double)p_21040_2_, field_320_n, p_21040_1_);
        entityitem.field_433_ad = 10;
        field_9093_l.func_526_a(entityitem);
        return entityitem;
    }

    public boolean func_120_t()
    {
        return !field_304_B;
    }

    public boolean func_91_u()
    {
        for(int i = 0; i < 8; i++)
        {
            float f = ((float)((i >> 0) % 2) - 0.5F) * field_300_D * 0.8F;
            float f1 = ((float)((i >> 1) % 2) - 0.5F) * 0.1F;
            float f2 = ((float)((i >> 2) % 2) - 0.5F) * field_300_D * 0.8F;
            int j = MathHelper.func_584_b(field_322_l + (double)f);
            int k = MathHelper.func_584_b(field_321_m + (double)func_104_p() + (double)f1);
            int l = MathHelper.func_584_b(field_320_n + (double)f2);
            if(field_9093_l.func_445_d(j, k, l))
            {
                return true;
            }
        }

        return false;
    }

    public boolean func_6092_a(EntityPlayer p_6092_1_)
    {
        return false;
    }

    public AxisAlignedBB func_89_d(Entity p_89_1_)
    {
        return null;
    }

    public void func_115_v()
    {
        if(field_327_g.field_304_B)
        {
            field_327_g = null;
            return;
        }
        field_319_o = 0.0D;
        field_318_p = 0.0D;
        field_317_q = 0.0D;
        func_106_b_();
        if(field_327_g == null)
        {
            return;
        }
        field_327_g.func_127_w();
        field_4128_e += field_327_g.field_316_r - field_327_g.field_9089_x;
        field_4130_d += field_327_g.field_315_s - field_327_g.field_9088_y;
        for(; field_4128_e >= 180D; field_4128_e -= 360D) { }
        for(; field_4128_e < -180D; field_4128_e += 360D) { }
        for(; field_4130_d >= 180D; field_4130_d -= 360D) { }
        for(; field_4130_d < -180D; field_4130_d += 360D) { }
        double d = field_4128_e * 0.5D;
        double d1 = field_4130_d * 0.5D;
        float f = 10F;
        if(d > (double)f)
        {
            d = f;
        }
        if(d < (double)(-f))
        {
            d = -f;
        }
        if(d1 > (double)f)
        {
            d1 = f;
        }
        if(d1 < (double)(-f))
        {
            d1 = -f;
        }
        field_4128_e -= d;
        field_4130_d -= d1;
        field_316_r += d;
        field_315_s += d1;
    }

    public void func_127_w()
    {
        field_328_f.func_86_a(field_322_l, field_321_m + func_130_h() + field_328_f.func_117_x(), field_320_n);
    }

    public double func_117_x()
    {
        return (double)field_9076_H;
    }

    public double func_130_h()
    {
        return (double)field_298_E * 0.75D;
    }

    public void func_6094_e(Entity p_6094_1_)
    {
        field_4130_d = 0.0D;
        field_4128_e = 0.0D;
        if(p_6094_1_ == null)
        {
            if(field_327_g != null)
            {
                func_107_c(field_327_g.field_322_l, field_327_g.field_312_v.field_963_b + (double)field_327_g.field_298_E, field_327_g.field_320_n, field_316_r, field_315_s);
                field_327_g.field_328_f = null;
            }
            field_327_g = null;
            return;
        }
        if(field_327_g == p_6094_1_)
        {
            field_327_g.field_328_f = null;
            field_327_g = null;
            func_107_c(p_6094_1_.field_322_l, p_6094_1_.field_312_v.field_963_b + (double)p_6094_1_.field_298_E, p_6094_1_.field_320_n, field_316_r, field_315_s);
            return;
        }
        if(field_327_g != null)
        {
            field_327_g.field_328_f = null;
        }
        if(p_6094_1_.field_328_f != null)
        {
            p_6094_1_.field_328_f.field_327_g = null;
        }
        field_327_g = p_6094_1_;
        p_6094_1_.field_328_f = this;
    }

    public float func_41010_j_()
    {
        return 0.1F;
    }

    public Vec3D func_4039_B()
    {
        return null;
    }

    public void func_4042_C()
    {
    }

    public ItemStack[] func_20042_E()
    {
        return null;
    }

    public boolean func_40035_z()
    {
        return field_9061_Z > 0 || func_21042_c(0);
    }

    public boolean func_9059_p()
    {
        return func_21042_c(1);
    }

    public void func_21043_b(boolean p_21043_1_)
    {
        func_21041_a(1, p_21043_1_);
    }

    public boolean func_35149_at()
    {
        return func_21042_c(3);
    }

    public void func_35146_g(boolean p_35146_1_)
    {
        func_21041_a(3, p_35146_1_);
    }

    public void func_35148_h(boolean p_35148_1_)
    {
        func_21041_a(4, p_35148_1_);
    }

    protected boolean func_21042_c(int p_21042_1_)
    {
        return (field_21045_af.func_21156_a(0) & 1 << p_21042_1_) != 0;
    }

    protected void func_21041_a(int p_21041_1_, boolean p_21041_2_)
    {
        byte byte0 = field_21045_af.func_21156_a(0);
        if(p_21041_2_)
        {
            field_21045_af.func_21155_b(0, Byte.valueOf((byte)(byte0 | 1 << p_21041_1_)));
        } else
        {
            field_21045_af.func_21155_b(0, Byte.valueOf((byte)(byte0 & ~(1 << p_21041_1_))));
        }
    }

    public int func_41009_al()
    {
        return field_21045_af.func_41048_b(1);
    }

    public void func_41008_j(int p_41008_1_)
    {
        field_21045_af.func_21155_b(1, Short.valueOf((short)p_41008_1_));
    }

    public void func_27009_a(EntityLightningBolt p_27009_1_)
    {
        func_125_b(5);
        field_9061_Z++;
        if(field_9061_Z == 0)
        {
            func_40034_j(8);
        }
    }

    public void func_27010_a(EntityLiving entityliving)
    {
    }

    protected boolean func_28005_g(double p_28005_1_, double p_28005_3_, double p_28005_5_)
    {
        int i = MathHelper.func_584_b(p_28005_1_);
        int j = MathHelper.func_584_b(p_28005_3_);
        int k = MathHelper.func_584_b(p_28005_5_);
        double d = p_28005_1_ - (double)i;
        double d1 = p_28005_3_ - (double)j;
        double d2 = p_28005_5_ - (double)k;
        if(field_9093_l.func_445_d(i, j, k))
        {
            boolean flag = !field_9093_l.func_445_d(i - 1, j, k);
            boolean flag1 = !field_9093_l.func_445_d(i + 1, j, k);
            boolean flag2 = !field_9093_l.func_445_d(i, j - 1, k);
            boolean flag3 = !field_9093_l.func_445_d(i, j + 1, k);
            boolean flag4 = !field_9093_l.func_445_d(i, j, k - 1);
            boolean flag5 = !field_9093_l.func_445_d(i, j, k + 1);
            byte byte0 = -1;
            double d3 = 9999D;
            if(flag && d < d3)
            {
                d3 = d;
                byte0 = 0;
            }
            if(flag1 && 1.0D - d < d3)
            {
                d3 = 1.0D - d;
                byte0 = 1;
            }
            if(flag2 && d1 < d3)
            {
                d3 = d1;
                byte0 = 2;
            }
            if(flag3 && 1.0D - d1 < d3)
            {
                d3 = 1.0D - d1;
                byte0 = 3;
            }
            if(flag4 && d2 < d3)
            {
                d3 = d2;
                byte0 = 4;
            }
            if(flag5 && 1.0D - d2 < d3)
            {
                double d4 = 1.0D - d2;
                byte0 = 5;
            }
            float f = field_9064_W.nextFloat() * 0.2F + 0.1F;
            if(byte0 == 0)
            {
                field_319_o = -f;
            }
            if(byte0 == 1)
            {
                field_319_o = f;
            }
            if(byte0 == 2)
            {
                field_318_p = -f;
            }
            if(byte0 == 3)
            {
                field_318_p = f;
            }
            if(byte0 == 4)
            {
                field_317_q = -f;
            }
            if(byte0 == 5)
            {
                field_317_q = f;
            }
            return true;
        } else
        {
            return false;
        }
    }

    public void func_35147_q()
    {
        field_27012_bb = true;
        field_9072_N = 0.0F;
    }

    public String func_35150_Y()
    {
        String s = EntityList.func_564_b(this);
        if(s == null)
        {
            s = "generic";
        }
        return StatCollector.func_25136_a((new StringBuilder()).append("entity.").append(s).append(".name").toString());
    }

    public Entity[] func_40037_aF()
    {
        return null;
    }

    public boolean func_41012_c_(Entity p_41012_1_)
    {
        return this == p_41012_1_;
    }

    public float func_48314_aq()
    {
        return 0.0F;
    }

    public boolean func_48313_k_()
    {
        return true;
    }

}

