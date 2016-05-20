package net.minecraft.world.biome;


import com.google.common.collect.Sets;
import net.canarymod.api.world.CanaryBiome;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.init.Blocks;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenDoublePlant;
import net.minecraft.world.gen.feature.WorldGenSwamp;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public abstract class BiomeGenBase {

    private static final Logger aC = LogManager.getLogger();
    protected static final Height a = new Height(0.1F, 0.2F);
    protected static final Height b = new Height(-0.5F, 0.0F);
    protected static final Height c = new Height(-1.0F, 0.1F);
    protected static final Height d = new Height(-1.8F, 0.1F);
    protected static final Height e = new Height(0.125F, 0.05F);
    protected static final Height f = new Height(0.2F, 0.2F);
    protected static final Height g = new Height(0.45F, 0.3F);
    protected static final Height h = new Height(1.5F, 0.025F);
    protected static final Height i = new Height(1.0F, 0.5F);
    protected static final Height j = new Height(0.0F, 0.025F);
    protected static final Height k = new Height(0.1F, 0.8F);
    protected static final Height l = new Height(0.2F, 0.3F);
    protected static final Height m = new Height(-0.2F, 0.1F);
    private static final BiomeGenBase[] aD = new BiomeGenBase[256];
    public static final Set n = Sets.newHashSet();
    public static final BiomeGenBase o = (new BiomeGenOcean(0)).b(112).a("Ocean").a(c);
    public static final BiomeGenBase p = (new BiomeGenPlains(1)).b(9286496).a("Plains");
    public static final BiomeGenBase q = (new BiomeGenDesert(2)).b(16421912).a("Desert").b().a(2.0F, 0.0F).a(e);
    public static final BiomeGenBase r = (new BiomeGenHills(3, false)).b(6316128).a("Extreme Hills").a(i).a(0.2F, 0.3F);
    public static final BiomeGenBase s = (new BiomeGenForest(4, 0)).b(353825).a("Forest");
    public static final BiomeGenBase t = (new BiomeGenTaiga(5, 0)).b(747097).a("Taiga").a(5159473).a(0.25F, 0.8F).a(f);
    public static final BiomeGenBase u = (new BiomeGenSwamp(6)).b(522674).a("Swampland").a(9154376).a(m).a(0.8F, 0.9F);
    public static final BiomeGenBase v = (new BiomeGenRiver(7)).b(255).a("River").a(b);
    public static final BiomeGenBase w = (new BiomeGenHell(8)).b(16711680).a("Hell").b().a(2.0F, 0.0F);
    public static final BiomeGenBase x = (new BiomeGenEnd(9)).b(8421631).a("Sky").b();
    public static final BiomeGenBase y = (new BiomeGenOcean(10)).b(9474208).a("FrozenOcean").c().a(c).a(0.0F, 0.5F);
    public static final BiomeGenBase z = (new BiomeGenRiver(11)).b(10526975).a("FrozenRiver").c().a(b).a(0.0F, 0.5F);
    public static final BiomeGenBase A = (new BiomeGenSnow(12, false)).b(16777215).a("Ice Plains").c().a(0.0F, 0.5F).a(e);
    public static final BiomeGenBase B = (new BiomeGenSnow(13, false)).b(10526880).a("Ice Mountains").c().a(g).a(0.0F, 0.5F);
    public static final BiomeGenBase C = (new BiomeGenMushroomIsland(14)).b(16711935).a("MushroomIsland").a(0.9F, 1.0F).a(l);
    public static final BiomeGenBase D = (new BiomeGenMushroomIsland(15)).b(10486015).a("MushroomIslandShore").a(0.9F, 1.0F).a(j);
    public static final BiomeGenBase E = (new BiomeGenBeach(16)).b(16440917).a("Beach").a(0.8F, 0.4F).a(j);
    public static final BiomeGenBase F = (new BiomeGenDesert(17)).b(13786898).a("DesertHills").b().a(2.0F, 0.0F).a(g);
    public static final BiomeGenBase G = (new BiomeGenForest(18, 0)).b(2250012).a("ForestHills").a(g);
    public static final BiomeGenBase H = (new BiomeGenTaiga(19, 0)).b(1456435).a("TaigaHills").a(5159473).a(0.25F, 0.8F).a(g);
    public static final BiomeGenBase I = (new BiomeGenHills(20, true)).b(7501978).a("Extreme Hills Edge").a(i.a()).a(0.2F, 0.3F);
    public static final BiomeGenBase J = (new BiomeGenJungle(21, false)).b(5470985).a("Jungle").a(5470985).a(0.95F, 0.9F);
    public static final BiomeGenBase K = (new BiomeGenJungle(22, false)).b(2900485).a("JungleHills").a(5470985).a(0.95F, 0.9F).a(g);
    public static final BiomeGenBase L = (new BiomeGenJungle(23, true)).b(6458135).a("JungleEdge").a(5470985).a(0.95F, 0.8F);
    public static final BiomeGenBase M = (new BiomeGenOcean(24)).b(48).a("Deep Ocean").a(d);
    public static final BiomeGenBase N = (new BiomeGenStoneBeach(25)).b(10658436).a("Stone Beach").a(0.2F, 0.3F).a(k);
    public static final BiomeGenBase O = (new BiomeGenBeach(26)).b(16445632).a("Cold Beach").a(0.05F, 0.3F).a(j).c();
    public static final BiomeGenBase P = (new BiomeGenForest(27, 2)).a("Birch Forest").b(3175492);
    public static final BiomeGenBase Q = (new BiomeGenForest(28, 2)).a("Birch Forest Hills").b(2055986).a(g);
    public static final BiomeGenBase R = (new BiomeGenForest(29, 3)).b(4215066).a("Roofed Forest");
    public static final BiomeGenBase S = (new BiomeGenTaiga(30, 0)).b(3233098).a("Cold Taiga").a(5159473).c().a(-0.5F, 0.4F).a(f).c(16777215);
    public static final BiomeGenBase T = (new BiomeGenTaiga(31, 0)).b(2375478).a("Cold Taiga Hills").a(5159473).c().a(-0.5F, 0.4F).a(g).c(16777215);
    public static final BiomeGenBase U = (new BiomeGenTaiga(32, 1)).b(5858897).a("Mega Taiga").a(5159473).a(0.3F, 0.8F).a(f);
    public static final BiomeGenBase V = (new BiomeGenTaiga(33, 1)).b(4542270).a("Mega Taiga Hills").a(5159473).a(0.3F, 0.8F).a(g);
    public static final BiomeGenBase W = (new BiomeGenHills(34, true)).b(5271632).a("Extreme Hills+").a(i).a(0.2F, 0.3F);
    public static final BiomeGenBase X = (new BiomeGenSavanna(35)).b(12431967).a("Savanna").a(1.2F, 0.0F).b().a(e);
    public static final BiomeGenBase Y = (new BiomeGenSavanna(36)).b(10984804).a("Savanna Plateau").a(1.0F, 0.0F).b().a(h);
    public static final BiomeGenBase Z = (new BiomeGenMesa(37, false, false)).b(14238997).a("Mesa");
    public static final BiomeGenBase aa = (new BiomeGenMesa(38, false, true)).b(11573093).a("Mesa Plateau F").a(h);
    public static final BiomeGenBase ab = (new BiomeGenMesa(39, false, false)).b(13274213).a("Mesa Plateau").a(h);
    protected static final NoiseGeneratorPerlin ac;
    protected static final NoiseGeneratorPerlin ad;
    protected static final WorldGenDoublePlant ae;
    public String af;
    public int ag;
    public int ah;
    public Block ai;
    public int aj;
    public Block ak;
    public int al;
    public float am;
    public float an;
    public float ao;
    public float ap;
    public int aq;
    public BiomeDecorator ar;
    protected List as;
    protected List at;
    protected List au;
    protected List av;
    protected boolean aw;
    protected boolean ax;
    public final int ay;
    protected WorldGenTrees az;
    protected WorldGenBigTree aA;
    protected WorldGenSwamp aB;
    private CanaryBiome biome = new CanaryBiome(this); // CanaryMod

    protected BiomeGenBase(int i0) {
        this.ai = Blocks.c;
        this.aj = 0;
        this.ak = Blocks.d;
        this.al = 5169201;
        this.am = a.a;
        this.an = a.b;
        this.ao = 0.5F;
        this.ap = 0.5F;
        this.aq = 16777215;
        this.as = new ArrayList();
        this.at = new ArrayList();
        this.au = new ArrayList();
        this.av = new ArrayList();
        this.ax = true;
        this.az = new WorldGenTrees(false);
        this.aA = new WorldGenBigTree(false);
        this.aB = new WorldGenSwamp();
        this.ay = i0;
        aD[i0] = this;
        this.ar = this.a();
        this.at.add(new SpawnListEntry(EntitySheep.class, 12, 4, 4));
        this.at.add(new SpawnListEntry(EntityPig.class, 10, 4, 4));
        this.at.add(new SpawnListEntry(EntityChicken.class, 10, 4, 4));
        this.at.add(new SpawnListEntry(EntityCow.class, 8, 4, 4));
        this.as.add(new SpawnListEntry(EntitySpider.class, 100, 4, 4));
        this.as.add(new SpawnListEntry(EntityZombie.class, 100, 4, 4));
        this.as.add(new SpawnListEntry(EntitySkeleton.class, 100, 4, 4));
        this.as.add(new SpawnListEntry(EntityCreeper.class, 100, 4, 4));
        this.as.add(new SpawnListEntry(EntitySlime.class, 100, 4, 4));
        this.as.add(new SpawnListEntry(EntityEnderman.class, 10, 1, 4));
        this.as.add(new SpawnListEntry(EntityWitch.class, 5, 1, 1));
        this.au.add(new SpawnListEntry(EntitySquid.class, 10, 4, 4));
        this.av.add(new SpawnListEntry(EntityBat.class, 10, 8, 8));
    }

    protected BiomeDecorator a() {
        return new BiomeDecorator();
    }

    protected BiomeGenBase a(float f0, float f1) {
        if (f0 > 0.1F && f0 < 0.2F) {
            throw new IllegalArgumentException("Please avoid temperatures in the range 0.1 - 0.2 because of snow");
        }
        else {
            this.ao = f0;
            this.ap = f1;
            return this;
        }
    }

    protected final BiomeGenBase a(Height biomegenbase_height) {
        this.am = biomegenbase_height.a;
        this.an = biomegenbase_height.b;
        return this;
    }

    protected BiomeGenBase b() {
        this.ax = false;
        return this;
    }

    public WorldGenAbstractTree a(Random random) {
        return (WorldGenAbstractTree) (random.nextInt(10) == 0 ? this.aA : this.az);
    }

    public WorldGenerator b(Random random) {
        return new WorldGenTallGrass(Blocks.H, 1);
    }

    public String a(Random random, int i0, int i1, int i2) {
        return random.nextInt(3) > 0 ? BlockFlower.b[0] : BlockFlower.a[0];
    }

    protected BiomeGenBase c() {
        this.aw = true;
        return this;
    }

    protected BiomeGenBase a(String s0) {
        this.af = s0;
        return this;
    }

    public BiomeGenBase a(int i0) { // CanaryMod: protected to public
        this.al = i0;
        return this;
    }

    protected BiomeGenBase b(int i0) {
        this.a(i0, false);
        return this;
    }

    protected BiomeGenBase c(int i0) {
        this.ah = i0;
        return this;
    }

    protected BiomeGenBase a(int i0, boolean flag0) {
        this.ag = i0;
        if (flag0) {
            this.ah = (i0 & 16711422) >> 1;
        }
        else {
            this.ah = i0;
        }

        return this;
    }

    public List a(EnumCreatureType enumcreaturetype) {
        return enumcreaturetype == EnumCreatureType.monster ? this.as : (enumcreaturetype == EnumCreatureType.creature ? this.at : (enumcreaturetype == EnumCreatureType.waterCreature ? this.au : (enumcreaturetype == EnumCreatureType.ambient ? this.av : null)));
    }

    public boolean d() {
        return this.j();
    }

    public boolean e() {
        return this.j() ? false : this.ax;
    }

    public boolean f() {
        return this.ap > 0.85F;
    }

    public float g() {
        return 0.1F;
    }

    public final int h() {
        return (int) (this.ap * 65536.0F);
    }

    public final float a(int i0, int i1, int i2) {
        if (i1 > 64) {
            float f0 = (float) ac.a((double) i0 * 1.0D / 8.0D, (double) i2 * 1.0D / 8.0D) * 4.0F;

            return this.ao - (f0 + (float) i1 - 64.0F) * 0.05F / 30.0F;
        }
        else {
            return this.ao;
        }
    }

    public void a(World world, Random random, int i0, int i1) {
        this.ar.a(world, random, this, i0, i1);
    }

    public boolean j() {
        return this.aw;
    }

    public void a(World world, Random random, Block[] ablock, byte[] abyte, int i0, int i1, double d0) {
        this.b(world, random, ablock, abyte, i0, i1, d0);
    }

    public final void b(World world, Random random, Block[] ablock, byte[] abyte, int i0, int i1, double d0) {
        boolean flag0 = true;
        Block block = this.ai;
        byte b0 = (byte) (this.aj & 255);
        Block block1 = this.ak;
        int i2 = -1;
        int i3 = (int) (d0 / 3.0D + 3.0D + random.nextDouble() * 0.25D);
        int i4 = i0 & 15;
        int i5 = i1 & 15;
        int i6 = ablock.length / 256;

        for (int i7 = 255; i7 >= 0; --i7) {
            int i8 = (i5 * 16 + i4) * i6 + i7;

            if (i7 <= 0 + random.nextInt(5)) {
                ablock[i8] = Blocks.h;
            }
            else {
                Block block2 = ablock[i8];

                if (block2 != null && block2.o() != Material.a) {
                    if (block2 == Blocks.b) {
                        if (i2 == -1) {
                            if (i3 <= 0) {
                                block = null;
                                b0 = 0;
                                block1 = Blocks.b;
                            }
                            else if (i7 >= 59 && i7 <= 64) {
                                block = this.ai;
                                b0 = (byte) (this.aj & 255);
                                block1 = this.ak;
                            }

                            if (i7 < 63 && (block == null || block.o() == Material.a)) {
                                if (this.a(i0, i7, i1) < 0.15F) {
                                    block = Blocks.aD;
                                    b0 = 0;
                                }
                                else {
                                    block = Blocks.j;
                                    b0 = 0;
                                }
                            }

                            i2 = i3;
                            if (i7 >= 62) {
                                ablock[i8] = block;
                                abyte[i8] = b0;
                            }
                            else if (i7 < 56 - i3) {
                                block = null;
                                block1 = Blocks.b;
                                ablock[i8] = Blocks.n;
                            }
                            else {
                                ablock[i8] = block1;
                            }
                        }
                        else if (i2 > 0) {
                            --i2;
                            ablock[i8] = block1;
                            if (i2 == 0 && block1 == Blocks.m) {
                                i2 = random.nextInt(4) + Math.max(0, i7 - 63);
                                block1 = Blocks.A;
                            }
                        }
                    }
                }
                else {
                    i2 = -1;
                }
            }
        }

    }

    protected BiomeGenBase k() {
        return new BiomeGenMutated(this.ay + 128, this);
    }

    public Class l() {
        return this.getClass();
    }

    public boolean a(BiomeGenBase biomegenbase) {
        return biomegenbase == this ? true : (biomegenbase == null ? false : this.l() == biomegenbase.l());
    }

    public TempCategory m() {
        return (double) this.ao < 0.2D ? TempCategory.COLD : ((double) this.ao < 1.0D ? TempCategory.MEDIUM : TempCategory.WARM);
    }

    public static BiomeGenBase[] n() {
        return aD;
    }

    public static BiomeGenBase d(int i0) {
        if (i0 >= 0 && i0 <= aD.length) {
            return aD[i0];
        }
        else {
            aC.warn("Biome ID is out of bounds: " + i0 + ", defaulting to 0 (Ocean)");
            return o;
        }
    }

    static {
        p.k();
        q.k();
        s.k();
        t.k();
        u.k();
        A.k();
        J.k();
        L.k();
        S.k();
        X.k();
        Y.k();
        Z.k();
        aa.k();
        ab.k();
        P.k();
        Q.k();
        R.k();
        U.k();
        r.k();
        W.k();
        aD[V.ay + 128] = aD[U.ay + 128];
        BiomeGenBase[] abiomegenbase = aD;
        int i0 = abiomegenbase.length;

        for (int i1 = 0; i1 < i0; ++i1) {
            BiomeGenBase biomegenbase = abiomegenbase[i1];

            if (biomegenbase != null && biomegenbase.ay < 128) {
                n.add(biomegenbase);
            }
        }

        n.remove(w);
        n.remove(x);
        ac = new NoiseGeneratorPerlin(new Random(1234L), 1);
        ad = new NoiseGeneratorPerlin(new Random(2345L), 1);
        ae = new WorldGenDoublePlant();
    }

    public static class SpawnListEntry extends WeightedRandom.Item {

        public Class b;
        public int c;
        public int d;

        public SpawnListEntry(Class oclass0, int i0, int i1, int i2) {
            super(i0);
            this.b = oclass0;
            this.c = i1;
            this.d = i2;
        }

        public String toString() {
            return this.b.getSimpleName() + "*(" + this.c + "-" + this.d + "):" + this.a;
        }
    }


    public static class Height {

        public float a;
        public float b;

        public Height(float f0, float f1) {
            this.a = f0;
            this.b = f1;
        }

        public Height a() {
            return new Height(this.a * 0.8F, this.b * 0.6F);
        }
    }


    public static enum TempCategory {

        OCEAN("OCEAN", 0), COLD("COLD", 1), MEDIUM("MEDIUM", 2), WARM("WARM", 3);

        private static final TempCategory[] e = new TempCategory[]{ OCEAN, COLD, MEDIUM, WARM };

        private TempCategory(String s0, int i0) {
        }

    }

    /*
     * Convenience Methods
     */
    public void setCanSnow(boolean bool) {
        this.aw = bool;
    }

    public void setCanRain(boolean bool) {
        this.ax = bool;
    }

    public boolean canRain() {
        return this.ax;
    }

    public void setTemperatureAndPrecipitation(float temp, float precipitation) {
        this.a(temp, precipitation);
    }

    public CanaryBiome getCanaryBiome() {
        return this.biome;
    }
}

