// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst 

package net.minecraft.src;

import java.util.Random;

// Referenced classes of package net.minecraft.src:
//            WorldGenerator, MathHelper, World

public class WorldGenBigTree extends WorldGenerator
{

    static final byte field_760_a[] = {
        2, 0, 0, 1, 2, 1
    };
    Random field_759_b;
    World field_758_c;
    int field_757_d[] = {
        0, 0, 0
    };
    int field_756_e;
    int field_755_f;
    double field_754_g;
    double field_753_h;
    double field_752_i;
    double field_751_j;
    double field_750_k;
    int field_749_l;
    int field_748_m;
    int field_747_n;
    int field_746_o[][];

    public WorldGenBigTree(boolean p_i205_1_)
    {
        super(p_i205_1_);
        field_759_b = new Random();
        field_756_e = 0;
        field_754_g = 0.61799999999999999D;
        field_753_h = 1.0D;
        field_752_i = 0.38100000000000001D;
        field_751_j = 1.0D;
        field_750_k = 1.0D;
        field_749_l = 1;
        field_748_m = 12;
        field_747_n = 4;
    }

    void func_424_a()
    {
        field_755_f = (int)((double)field_756_e * field_754_g);
        if(field_755_f >= field_756_e)
        {
            field_755_f = field_756_e - 1;
        }
        int i = (int)(1.3819999999999999D + Math.pow((field_750_k * (double)field_756_e) / 13D, 2D));
        if(i < 1)
        {
            i = 1;
        }
        int ai[][] = new int[i * field_756_e][4];
        int j = (field_757_d[1] + field_756_e) - field_747_n;
        int k = 1;
        int l = field_757_d[1] + field_755_f;
        int i1 = j - field_757_d[1];
        ai[0][0] = field_757_d[0];
        ai[0][1] = j;
        ai[0][2] = field_757_d[2];
        ai[0][3] = l;
        j--;
        while(i1 >= 0) 
        {
            int j1 = 0;
            float f = func_431_a(i1);
            if(f < 0.0F)
            {
                j--;
                i1--;
            } else
            {
                double d = 0.5D;
                for(; j1 < i; j1++)
                {
                    double d1 = field_751_j * ((double)f * ((double)field_759_b.nextFloat() + 0.32800000000000001D));
                    double d2 = (double)field_759_b.nextFloat() * 2D * 3.1415899999999999D;
                    int k1 = MathHelper.func_584_b(d1 * Math.sin(d2) + (double)field_757_d[0] + d);
                    int l1 = MathHelper.func_584_b(d1 * Math.cos(d2) + (double)field_757_d[2] + d);
                    int ai1[] = {
                        k1, j, l1
                    };
                    int ai2[] = {
                        k1, j + field_747_n, l1
                    };
                    if(func_427_a(ai1, ai2) != -1)
                    {
                        continue;
                    }
                    int ai3[] = {
                        field_757_d[0], field_757_d[1], field_757_d[2]
                    };
                    double d3 = Math.sqrt(Math.pow(Math.abs(field_757_d[0] - ai1[0]), 2D) + Math.pow(Math.abs(field_757_d[2] - ai1[2]), 2D));
                    double d4 = d3 * field_752_i;
                    if((double)ai1[1] - d4 > (double)l)
                    {
                        ai3[1] = l;
                    } else
                    {
                        ai3[1] = (int)((double)ai1[1] - d4);
                    }
                    if(func_427_a(ai3, ai1) == -1)
                    {
                        ai[k][0] = k1;
                        ai[k][1] = j;
                        ai[k][2] = l1;
                        ai[k][3] = ai3[1];
                        k++;
                    }
                }

                j--;
                i1--;
            }
        }
        field_746_o = new int[k][4];
        System.arraycopy(ai, 0, field_746_o, 0, k);
    }

    void func_426_a(int p_426_1_, int p_426_2_, int p_426_3_, float p_426_4_, byte p_426_5_, int p_426_6_)
    {
        int i = (int)((double)p_426_4_ + 0.61799999999999999D);
        byte byte0 = field_760_a[p_426_5_];
        byte byte1 = field_760_a[p_426_5_ + 3];
        int ai[] = {
            p_426_1_, p_426_2_, p_426_3_
        };
        int ai1[] = {
            0, 0, 0
        };
        int j = -i;
        int k = -i;
        ai1[p_426_5_] = ai[p_426_5_];
        for(; j <= i; j++)
        {
            ai1[byte0] = ai[byte0] + j;
            for(int l = -i; l <= i;)
            {
                double d = Math.sqrt(Math.pow((double)Math.abs(j) + 0.5D, 2D) + Math.pow((double)Math.abs(l) + 0.5D, 2D));
                if(d > (double)p_426_4_)
                {
                    l++;
                } else
                {
                    ai1[byte1] = ai[byte1] + l;
                    int i1 = field_758_c.func_444_a(ai1[0], ai1[1], ai1[2]);
                    if(i1 != 0 && i1 != 18)
                    {
                        l++;
                    } else
                    {
                        func_41043_a(field_758_c, ai1[0], ai1[1], ai1[2], p_426_6_, 0);
                        l++;
                    }
                }
            }

        }

    }

    float func_431_a(int p_431_1_)
    {
        if((double)p_431_1_ < (double)(float)field_756_e * 0.29999999999999999D)
        {
            return -1.618F;
        }
        float f = (float)field_756_e / 2.0F;
        float f1 = (float)field_756_e / 2.0F - (float)p_431_1_;
        float f2;
        if(f1 == 0.0F)
        {
            f2 = f;
        } else
        if(Math.abs(f1) >= f)
        {
            f2 = 0.0F;
        } else
        {
            f2 = (float)Math.sqrt(Math.pow(Math.abs(f), 2D) - Math.pow(Math.abs(f1), 2D));
        }
        f2 *= 0.5F;
        return f2;
    }

    float func_429_b(int p_429_1_)
    {
        if(p_429_1_ < 0 || p_429_1_ >= field_747_n)
        {
            return -1F;
        }
        return p_429_1_ != 0 && p_429_1_ != field_747_n - 1 ? 3F : 2.0F;
    }

    void func_423_a(int p_423_1_, int p_423_2_, int p_423_3_)
    {
        int i = p_423_2_;
        for(int j = p_423_2_ + field_747_n; i < j; i++)
        {
            float f = func_429_b(i - p_423_2_);
            func_426_a(p_423_1_, i, p_423_3_, f, (byte)1, 18);
        }

    }

    void func_425_a(int p_425_1_[], int p_425_2_[], int p_425_3_)
    {
        int ai[] = {
            0, 0, 0
        };
        byte byte0 = 0;
        int i = 0;
        for(; byte0 < 3; byte0++)
        {
            ai[byte0] = p_425_2_[byte0] - p_425_1_[byte0];
            if(Math.abs(ai[byte0]) > Math.abs(ai[i]))
            {
                i = byte0;
            }
        }

        if(ai[i] == 0)
        {
            return;
        }
        byte byte1 = field_760_a[i];
        byte byte2 = field_760_a[i + 3];
        byte byte3;
        if(ai[i] > 0)
        {
            byte3 = 1;
        } else
        {
            byte3 = -1;
        }
        double d = (double)ai[byte1] / (double)ai[i];
        double d1 = (double)ai[byte2] / (double)ai[i];
        int ai1[] = {
            0, 0, 0
        };
        int j = 0;
        for(int k = ai[i] + byte3; j != k; j += byte3)
        {
            ai1[i] = MathHelper.func_584_b((double)(p_425_1_[i] + j) + 0.5D);
            ai1[byte1] = MathHelper.func_584_b((double)p_425_1_[byte1] + (double)j * d + 0.5D);
            ai1[byte2] = MathHelper.func_584_b((double)p_425_1_[byte2] + (double)j * d1 + 0.5D);
            func_41043_a(field_758_c, ai1[0], ai1[1], ai1[2], p_425_3_, 0);
        }

    }

    void func_421_b()
    {
        int i = 0;
        for(int j = field_746_o.length; i < j; i++)
        {
            int k = field_746_o[i][0];
            int l = field_746_o[i][1];
            int i1 = field_746_o[i][2];
            func_423_a(k, l, i1);
        }

    }

    boolean func_430_c(int p_430_1_)
    {
        return (double)p_430_1_ >= (double)field_756_e * 0.20000000000000001D;
    }

    void func_432_c()
    {
        int i = field_757_d[0];
        int j = field_757_d[1];
        int k = field_757_d[1] + field_755_f;
        int l = field_757_d[2];
        int ai[] = {
            i, j, l
        };
        int ai1[] = {
            i, k, l
        };
        func_425_a(ai, ai1, 17);
        if(field_749_l == 2)
        {
            ai[0]++;
            ai1[0]++;
            func_425_a(ai, ai1, 17);
            ai[2]++;
            ai1[2]++;
            func_425_a(ai, ai1, 17);
            ai[0]--;
            ai1[0]--;
            func_425_a(ai, ai1, 17);
        }
    }

    void func_428_d()
    {
        int i = 0;
        int j = field_746_o.length;
        int ai[] = {
            field_757_d[0], field_757_d[1], field_757_d[2]
        };
        for(; i < j; i++)
        {
            int ai1[] = field_746_o[i];
            int ai2[] = {
                ai1[0], ai1[1], ai1[2]
            };
            ai[1] = ai1[3];
            int k = ai[1] - field_757_d[1];
            if(func_430_c(k))
            {
                func_425_a(ai, ai2, 17);
            }
        }

    }

    int func_427_a(int p_427_1_[], int p_427_2_[])
    {
        int ai[] = {
            0, 0, 0
        };
        byte byte0 = 0;
        int i = 0;
        for(; byte0 < 3; byte0++)
        {
            ai[byte0] = p_427_2_[byte0] - p_427_1_[byte0];
            if(Math.abs(ai[byte0]) > Math.abs(ai[i]))
            {
                i = byte0;
            }
        }

        if(ai[i] == 0)
        {
            return -1;
        }
        byte byte1 = field_760_a[i];
        byte byte2 = field_760_a[i + 3];
        byte byte3;
        if(ai[i] > 0)
        {
            byte3 = 1;
        } else
        {
            byte3 = -1;
        }
        double d = (double)ai[byte1] / (double)ai[i];
        double d1 = (double)ai[byte2] / (double)ai[i];
        int ai1[] = {
            0, 0, 0
        };
        int j = 0;
        int k = ai[i] + byte3;
        do
        {
            if(j == k)
            {
                break;
            }
            ai1[i] = p_427_1_[i] + j;
            ai1[byte1] = MathHelper.func_584_b((double)p_427_1_[byte1] + (double)j * d);
            ai1[byte2] = MathHelper.func_584_b((double)p_427_1_[byte2] + (double)j * d1);
            int l = field_758_c.func_444_a(ai1[0], ai1[1], ai1[2]);
            if(l != 0 && l != 18)
            {
                break;
            }
            j += byte3;
        } while(true);
        if(j == k)
        {
            return -1;
        } else
        {
            return Math.abs(j);
        }
    }

    boolean func_422_e()
    {
        int ai[] = {
            field_757_d[0], field_757_d[1], field_757_d[2]
        };
        int ai1[] = {
            field_757_d[0], (field_757_d[1] + field_756_e) - 1, field_757_d[2]
        };
        int i = field_758_c.func_444_a(field_757_d[0], field_757_d[1] - 1, field_757_d[2]);
        if(i != 2 && i != 3)
        {
            return false;
        }
        int j = func_427_a(ai, ai1);
        if(j == -1)
        {
            return true;
        }
        if(j < 6)
        {
            return false;
        } else
        {
            field_756_e = j;
            return true;
        }
    }

    public void func_420_a(double p_420_1_, double p_420_3_, double p_420_5_)
    {
        field_748_m = (int)(p_420_1_ * 12D);
        if(p_420_1_ > 0.5D)
        {
            field_747_n = 5;
        }
        field_751_j = p_420_3_;
        field_750_k = p_420_5_;
    }

    public boolean func_419_a(World p_419_1_, Random p_419_2_, int p_419_3_, int p_419_4_, int p_419_5_)
    {
        field_758_c = p_419_1_;
        long l = p_419_2_.nextLong();
        field_759_b.setSeed(l);
        field_757_d[0] = p_419_3_;
        field_757_d[1] = p_419_4_;
        field_757_d[2] = p_419_5_;
        if(field_756_e == 0)
        {
            field_756_e = 5 + field_759_b.nextInt(field_748_m);
        }
        if(!func_422_e())
        {
            return false;
        } else
        {
            func_424_a();
            func_421_b();
            func_432_c();
            func_428_d();
            return true;
        }
    }

}

