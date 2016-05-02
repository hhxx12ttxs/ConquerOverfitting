// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst 

package net.minecraft.src;


// Referenced classes of package net.minecraft.src:
//            ItemMapBase, ItemStack, MapData, World, 
//            WorldInfo, WorldProvider, Entity, MathHelper, 
//            Block, Chunk, Material, MapColor, 
//            EntityPlayer, Packet131MapData, Item, Packet

public class ItemMap extends ItemMapBase
{

    protected ItemMap(int p_i564_1_)
    {
        super(p_i564_1_);
        func_21086_c(1);
    }

    public MapData func_28023_a(ItemStack p_28023_1_, World p_28023_2_)
    {
        String s = (new StringBuilder()).append("map_").append(p_28023_1_.func_21125_h()).toString();
        MapData mapdata = (MapData)p_28023_2_.func_28103_a(net.minecraft.src.MapData.class, (new StringBuilder()).append("map_").append(p_28023_1_.func_21125_h()).toString());
        if(mapdata == null)
        {
            p_28023_1_.func_28145_b(p_28023_2_.func_28104_b("map"));
            String s1 = (new StringBuilder()).append("map_").append(p_28023_1_.func_21125_h()).toString();
            mapdata = new MapData(s1);
            mapdata.field_28164_b = p_28023_2_.func_22081_n().func_22184_c();
            mapdata.field_28163_c = p_28023_2_.func_22081_n().func_22189_e();
            mapdata.field_28161_e = 3;
            mapdata.field_28162_d = (byte)p_28023_2_.field_4272_q.field_6165_g;
            mapdata.func_28146_a();
            p_28023_2_.func_28102_a(s1, mapdata);
        }
        return mapdata;
    }

    public void func_28024_a(World p_28024_1_, Entity p_28024_2_, MapData p_28024_3_)
    {
        if(p_28024_1_.field_4272_q.field_6165_g != p_28024_3_.field_28162_d)
        {
            return;
        }
        char c = '\200';
        char c1 = '\200';
        int i = 1 << p_28024_3_.field_28161_e;
        int j = p_28024_3_.field_28164_b;
        int k = p_28024_3_.field_28163_c;
        int l = MathHelper.func_584_b(p_28024_2_.field_322_l - (double)j) / i + c / 2;
        int i1 = MathHelper.func_584_b(p_28024_2_.field_320_n - (double)k) / i + c1 / 2;
        int j1 = 128 / i;
        if(p_28024_1_.field_4272_q.field_4306_c)
        {
            j1 /= 2;
        }
        p_28024_3_.field_28159_g++;
        for(int k1 = (l - j1) + 1; k1 < l + j1; k1++)
        {
            if((k1 & 0xf) != (p_28024_3_.field_28159_g & 0xf))
            {
                continue;
            }
            int l1 = 255;
            int i2 = 0;
            double d = 0.0D;
            for(int j2 = i1 - j1 - 1; j2 < i1 + j1; j2++)
            {
                if(k1 < 0 || j2 < -1 || k1 >= c || j2 >= c1)
                {
                    continue;
                }
                int k2 = k1 - l;
                int l2 = j2 - i1;
                boolean flag = k2 * k2 + l2 * l2 > (j1 - 2) * (j1 - 2);
                int i3 = ((j / i + k1) - c / 2) * i;
                int j3 = ((k / i + j2) - c1 / 2) * i;
                int k3 = 0;
                int l3 = 0;
                int i4 = 0;
                int ai[] = new int[256];
                Chunk chunk = p_28024_1_.func_492_a(i3, j3);
                int j4 = i3 & 0xf;
                int k4 = j3 & 0xf;
                int l4 = 0;
                double d1 = 0.0D;
                if(p_28024_1_.field_4272_q.field_4306_c)
                {
                    int i5 = i3 + j3 * 0x389bf;
                    i5 = i5 * i5 * 0x1dd6751 + i5 * 11;
                    if((i5 >> 20 & 1) == 0)
                    {
                        ai[Block.field_533_w.field_573_bc] += 10;
                    } else
                    {
                        ai[Block.field_535_u.field_573_bc] += 10;
                    }
                    d1 = 100D;
                } else
                {
                    for(int j5 = 0; j5 < i; j5++)
                    {
                        for(int l5 = 0; l5 < i; l5++)
                        {
                            int j6 = chunk.func_337_b(j5 + j4, l5 + k4) + 1;
                            int l6 = 0;
                            if(j6 > 1)
                            {
                                boolean flag1 = false;
                                do
                                {
                                    flag1 = true;
                                    l6 = chunk.func_344_a(j5 + j4, j6 - 1, l5 + k4);
                                    if(l6 == 0)
                                    {
                                        flag1 = false;
                                    } else
                                    if(j6 > 0 && l6 > 0 && Block.field_542_n[l6].field_553_bn.field_28131_A == MapColor.field_28199_b)
                                    {
                                        flag1 = false;
                                    }
                                    if(!flag1)
                                    {
                                        j6--;
                                        l6 = chunk.func_344_a(j5 + j4, j6 - 1, l5 + k4);
                                    }
                                } while(j6 > 0 && !flag1);
                                if(j6 > 0 && l6 != 0 && Block.field_542_n[l6].field_553_bn.func_217_d())
                                {
                                    int i7 = j6 - 1;
                                    int k7 = 0;
                                    do
                                    {
                                        k7 = chunk.func_344_a(j5 + j4, i7--, l5 + k4);
                                        l4++;
                                    } while(i7 > 0 && k7 != 0 && Block.field_542_n[k7].field_553_bn.func_217_d());
                                }
                            }
                            d1 += (double)j6 / (double)(i * i);
                            ai[l6]++;
                        }

                    }

                }
                l4 /= i * i;
                k3 /= i * i;
                l3 /= i * i;
                i4 /= i * i;
                int k5 = 0;
                int i6 = 0;
                for(int k6 = 0; k6 < 256; k6++)
                {
                    if(ai[k6] > k5)
                    {
                        i6 = k6;
                        k5 = ai[k6];
                    }
                }

                double d2 = ((d1 - d) * 4D) / (double)(i + 4) + ((double)(k1 + j2 & 1) - 0.5D) * 0.40000000000000002D;
                byte byte0 = 1;
                if(d2 > 0.59999999999999998D)
                {
                    byte0 = 2;
                }
                if(d2 < -0.59999999999999998D)
                {
                    byte0 = 0;
                }
                int j7 = 0;
                if(i6 > 0)
                {
                    MapColor mapcolor = Block.field_542_n[i6].field_553_bn.field_28131_A;
                    if(mapcolor == MapColor.field_28187_n)
                    {
                        double d3 = (double)l4 * 0.10000000000000001D + (double)(k1 + j2 & 1) * 0.20000000000000001D;
                        byte0 = 1;
                        if(d3 < 0.5D)
                        {
                            byte0 = 2;
                        }
                        if(d3 > 0.90000000000000002D)
                        {
                            byte0 = 0;
                        }
                    }
                    j7 = mapcolor.field_28184_q;
                }
                d = d1;
                if(j2 < 0 || k2 * k2 + l2 * l2 >= j1 * j1 || flag && (k1 + j2 & 1) == 0)
                {
                    continue;
                }
                byte byte1 = p_28024_3_.field_28160_f[k1 + j2 * c];
                byte byte2 = (byte)(j7 * 4 + byte0);
                if(byte1 == byte2)
                {
                    continue;
                }
                if(l1 > j2)
                {
                    l1 = j2;
                }
                if(i2 < j2)
                {
                    i2 = j2;
                }
                p_28024_3_.field_28160_f[k1 + j2 * c] = byte2;
            }

            if(l1 <= i2)
            {
                p_28024_3_.func_28153_a(k1, l1, i2);
            }
        }

    }

    public void func_28018_a(ItemStack p_28018_1_, World p_28018_2_, Entity p_28018_3_, int p_28018_4_, boolean p_28018_5_)
    {
        if(p_28018_2_.field_792_x)
        {
            return;
        }
        MapData mapdata = func_28023_a(p_28018_1_, p_28018_2_);
        if(p_28018_3_ instanceof EntityPlayer)
        {
            EntityPlayer entityplayer = (EntityPlayer)p_28018_3_;
            mapdata.func_28155_a(entityplayer, p_28018_1_);
        }
        if(p_28018_5_)
        {
            func_28024_a(p_28018_2_, p_28018_3_, mapdata);
        }
    }

    public void func_28020_c(ItemStack p_28020_1_, World p_28020_2_, EntityPlayer p_28020_3_)
    {
        p_28020_1_.func_28145_b(p_28020_2_.func_28104_b("map"));
        String s = (new StringBuilder()).append("map_").append(p_28020_1_.func_21125_h()).toString();
        MapData mapdata = new MapData(s);
        p_28020_2_.func_28102_a(s, mapdata);
        mapdata.field_28164_b = MathHelper.func_584_b(p_28020_3_.field_322_l);
        mapdata.field_28163_c = MathHelper.func_584_b(p_28020_3_.field_320_n);
        mapdata.field_28161_e = 3;
        mapdata.field_28162_d = (byte)p_28020_2_.field_4272_q.field_6165_g;
        mapdata.func_28146_a();
    }

    public Packet func_28022_b(ItemStack p_28022_1_, World p_28022_2_, EntityPlayer p_28022_3_)
    {
        byte abyte0[] = func_28023_a(p_28022_1_, p_28022_2_).func_28154_a(p_28022_1_, p_28022_2_, p_28022_3_);
        if(abyte0 == null)
        {
            return null;
        } else
        {
            return new Packet131MapData((short)Item.field_28021_bb.field_234_aS, (short)p_28022_1_.func_21125_h(), abyte0);
        }
    }
}

