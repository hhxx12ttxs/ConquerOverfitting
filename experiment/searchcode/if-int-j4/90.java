// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst 

package net.minecraft.src;

import java.util.Random;

// Referenced classes of package net.minecraft.src:
//            WorldGenerator, World, Material, Block, 
//            BlockLeaves, BlockGrass

public class WorldGenSwamp extends WorldGenerator
{

    public WorldGenSwamp()
    {
    }

    public boolean func_419_a(World p_419_1_, Random p_419_2_, int p_419_3_, int p_419_4_, int p_419_5_)
    {
        int i = p_419_2_.nextInt(4) + 5;
        for(; p_419_1_.func_443_c(p_419_3_, p_419_4_ - 1, p_419_5_) == Material.field_521_f; p_419_4_--) { }
        boolean flag = true;
        if(p_419_4_ < 1 || p_419_4_ + i + 1 > 128)
        {
            return false;
        }
        for(int j = p_419_4_; j <= p_419_4_ + 1 + i; j++)
        {
            byte byte0 = 1;
            if(j == p_419_4_)
            {
                byte0 = 0;
            }
            if(j >= (p_419_4_ + 1 + i) - 2)
            {
                byte0 = 3;
            }
            for(int k1 = p_419_3_ - byte0; k1 <= p_419_3_ + byte0 && flag; k1++)
            {
                for(int k2 = p_419_5_ - byte0; k2 <= p_419_5_ + byte0 && flag; k2++)
                {
                    if(j >= 0 && j < 128)
                    {
                        int j3 = p_419_1_.func_444_a(k1, j, k2);
                        if(j3 == 0 || j3 == Block.field_581_L.field_573_bc)
                        {
                            continue;
                        }
                        if(j3 == Block.field_596_C.field_573_bc || j3 == Block.field_598_B.field_573_bc)
                        {
                            if(j > p_419_4_)
                            {
                                flag = false;
                            }
                        } else
                        {
                            flag = false;
                        }
                    } else
                    {
                        flag = false;
                    }
                }

            }

        }

        if(!flag)
        {
            return false;
        }
        int k = p_419_1_.func_444_a(p_419_3_, p_419_4_ - 1, p_419_5_);
        if(k != Block.field_534_v.field_573_bc && k != Block.field_533_w.field_573_bc || p_419_4_ >= 128 - i - 1)
        {
            return false;
        }
        func_50023_a(p_419_1_, p_419_3_, p_419_4_ - 1, p_419_5_, Block.field_533_w.field_573_bc);
        for(int l = (p_419_4_ - 3) + i; l <= p_419_4_ + i; l++)
        {
            int l1 = l - (p_419_4_ + i);
            int l2 = 2 - l1 / 2;
            for(int k3 = p_419_3_ - l2; k3 <= p_419_3_ + l2; k3++)
            {
                int i4 = k3 - p_419_3_;
                for(int k4 = p_419_5_ - l2; k4 <= p_419_5_ + l2; k4++)
                {
                    int l4 = k4 - p_419_5_;
                    if((Math.abs(i4) != l2 || Math.abs(l4) != l2 || p_419_2_.nextInt(2) != 0 && l1 != 0) && !Block.field_540_p[p_419_1_.func_444_a(k3, l, k4)])
                    {
                        func_50023_a(p_419_1_, k3, l, k4, Block.field_581_L.field_573_bc);
                    }
                }

            }

        }

        for(int i1 = 0; i1 < i; i1++)
        {
            int i2 = p_419_1_.func_444_a(p_419_3_, p_419_4_ + i1, p_419_5_);
            if(i2 == 0 || i2 == Block.field_581_L.field_573_bc || i2 == Block.field_598_B.field_573_bc || i2 == Block.field_596_C.field_573_bc)
            {
                func_50023_a(p_419_1_, p_419_3_, p_419_4_ + i1, p_419_5_, Block.field_582_K.field_573_bc);
            }
        }

        for(int j1 = (p_419_4_ - 3) + i; j1 <= p_419_4_ + i; j1++)
        {
            int j2 = j1 - (p_419_4_ + i);
            int i3 = 2 - j2 / 2;
            for(int l3 = p_419_3_ - i3; l3 <= p_419_3_ + i3; l3++)
            {
                for(int j4 = p_419_5_ - i3; j4 <= p_419_5_ + i3; j4++)
                {
                    if(p_419_1_.func_444_a(l3, j1, j4) != Block.field_581_L.field_573_bc)
                    {
                        continue;
                    }
                    if(p_419_2_.nextInt(4) == 0 && p_419_1_.func_444_a(l3 - 1, j1, j4) == 0)
                    {
                        func_35292_a(p_419_1_, l3 - 1, j1, j4, 8);
                    }
                    if(p_419_2_.nextInt(4) == 0 && p_419_1_.func_444_a(l3 + 1, j1, j4) == 0)
                    {
                        func_35292_a(p_419_1_, l3 + 1, j1, j4, 2);
                    }
                    if(p_419_2_.nextInt(4) == 0 && p_419_1_.func_444_a(l3, j1, j4 - 1) == 0)
                    {
                        func_35292_a(p_419_1_, l3, j1, j4 - 1, 1);
                    }
                    if(p_419_2_.nextInt(4) == 0 && p_419_1_.func_444_a(l3, j1, j4 + 1) == 0)
                    {
                        func_35292_a(p_419_1_, l3, j1, j4 + 1, 4);
                    }
                }

            }

        }

        return true;
    }

    private void func_35292_a(World p_35292_1_, int p_35292_2_, int p_35292_3_, int p_35292_4_, int p_35292_5_)
    {
        func_41043_a(p_35292_1_, p_35292_2_, p_35292_3_, p_35292_4_, Block.field_35045_bv.field_573_bc, p_35292_5_);
        for(int i = 4; p_35292_1_.func_444_a(p_35292_2_, --p_35292_3_, p_35292_4_) == 0 && i > 0; i--)
        {
            func_41043_a(p_35292_1_, p_35292_2_, p_35292_3_, p_35292_4_, Block.field_35045_bv.field_573_bc, p_35292_5_);
        }

    }
}

