package net.minecraft.entity.passive;

import net.canarymod.api.entity.living.animal.CanaryHorse;
import net.canarymod.api.entity.vehicle.Vehicle;
import net.canarymod.hook.CancelableHook;
import net.canarymod.hook.entity.VehicleEnterHook;
import net.canarymod.hook.entity.VehicleExitHook;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIRunAroundLikeCrazy;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;

public class EntityHorse extends EntityAnimal implements IInvBasic {

    private static final IEntitySelector bu = new IEntitySelector() {

        public boolean a(Entity var1) {
            return var1 instanceof EntityHorse && ((EntityHorse) var1).cm();
        }
    };
    private static final Attribute bv = (new RangedAttribute("horse.jumpStrength", 0.7D, 0.0D, 2.0D)).a("Jump Strength").a(true);
    private static final String[] bw = new String[]{ null, "textures/entity/horse/armor/horse_armor_iron.png", "textures/entity/horse/armor/horse_armor_gold.png", "textures/entity/horse/armor/horse_armor_diamond.png" };
    private static final String[] bx = new String[]{ "", "meo", "goo", "dio" };
    private static final int[] by = new int[]{ 0, 5, 7, 11 };
    private static final String[] bz = new String[]{ "textures/entity/horse/horse_white.png", "textures/entity/horse/horse_creamy.png", "textures/entity/horse/horse_chestnut.png", "textures/entity/horse/horse_brown.png", "textures/entity/horse/horse_black.png", "textures/entity/horse/horse_gray.png", "textures/entity/horse/horse_darkbrown.png" };
    private static final String[] bA = new String[]{ "hwh", "hcr", "hch", "hbr", "hbl", "hgr", "hdb" };
    private static final String[] bB = new String[]{ null, "textures/entity/horse/horse_markings_white.png", "textures/entity/horse/horse_markings_whitefield.png", "textures/entity/horse/horse_markings_whitedots.png", "textures/entity/horse/horse_markings_blackdots.png" };
    private static final String[] bC = new String[]{ "", "wo_", "wmo", "wdo", "bdo" };
    private int bD;
    private int bE;
    private int bF;
    public int bp;
    public int bq;
    protected boolean br;
    public AnimalChest bG; // CanaryMod: private => public
    private boolean bH;
    protected int bs;
    protected float bt;
    private boolean bI;
    private float bJ;
    private float bK;
    private float bL;
    private float bM;
    private float bN;
    private float bO;
    private int bP;
    private String bQ;
    private String[] bR = new String[3];

    public EntityHorse(World world) {
        super(world);
        this.a(1.4F, 1.6F);
        this.af = false;
        this.l(false);
        this.m().a(true);
        this.c.a(0, new EntityAISwimming(this));
        this.c.a(1, new EntityAIPanic(this, 1.2D));
        this.c.a(1, new EntityAIRunAroundLikeCrazy(this, 1.2D));
        this.c.a(2, new EntityAIMate(this, 1.0D));
        this.c.a(4, new EntityAIFollowParent(this, 1.0D));
        this.c.a(6, new EntityAIWander(this, 0.7D));
        this.c.a(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.c.a(8, new EntityAILookIdle(this));
        this.cL();
        this.entity = new CanaryHorse(this); // CanaryMod: wrap entity
    }

    protected void c() {
        super.c();
        this.ag.a(16, Integer.valueOf(0));
        this.ag.a(19, Byte.valueOf((byte) 0));
        this.ag.a(20, Integer.valueOf(0));
        this.ag.a(21, String.valueOf(""));
        this.ag.a(22, Integer.valueOf(0));
    }

    public void s(int i0) {
        this.ag.b(19, Byte.valueOf((byte) i0));
        this.cN();
    }

    public int bX() {
        return this.ag.a(19);
    }

    public void t(int i0) {
        this.ag.b(20, Integer.valueOf(i0));
        this.cN();
    }

    public int bY() {
        return this.ag.c(20);
    }

    public String b_() {
        if (this.bF()) {
            return this.bE();
        }
        else {
            int i0 = this.bX();

            switch (i0) {
                case 0:
                default:
                    return StatCollector.a("entity.horse.name");

                case 1:
                    return StatCollector.a("entity.donkey.name");

                case 2:
                    return StatCollector.a("entity.mule.name");

                case 3:
                    return StatCollector.a("entity.zombiehorse.name");

                case 4:
                    return StatCollector.a("entity.skeletonhorse.name");
            }
        }
    }

    private boolean x(int i0) {
        return (this.ag.c(16) & i0) != 0;
    }

    private void b(int i0, boolean flag0) {
        int i1 = this.ag.c(16);

        if (flag0) {
            this.ag.b(16, Integer.valueOf(i1 | i0));
        }
        else {
            this.ag.b(16, Integer.valueOf(i1 & ~i0));
        }
    }

    public boolean bZ() {
        return !this.f();
    }

    public boolean ca() {
        return this.x(2);
    }

    public boolean ce() {
        return this.bZ();
    }

    public String cf() {
        return this.ag.e(21);
    }

    public void b(String s0) {
        this.ag.b(21, s0);
    }

    public float cg() {
        int i0 = this.d();

        return i0 >= 0 ? 1.0F : 0.5F + (float) (-24000 - i0) / -24000.0F * 0.5F;
    }

    public void a(boolean flag0) {
        if (flag0) {
            this.a(this.cg());
        }
        else {
            this.a(1.0F);
        }
    }

    public boolean ch() {
        return this.br;
    }

    public void i(boolean flag0) {
        this.b(2, flag0);
    }

    public void j(boolean flag0) {
        this.br = flag0;
    }

    public boolean bK() {
        return !this.cC() && super.bK();
    }

    protected void o(float f0) {
        if (f0 > 6.0F && this.ck()) {
            this.o(false);
        }
    }

    public boolean ci() {
        return this.x(8);
    }

    public int cj() {
        return this.ag.c(22);
    }

    private int e(ItemStack itemstack) {
        if (itemstack == null) {
            return 0;
        }
        else {
            Item item = itemstack.b();

            return item == Items.bX ? 1 : (item == Items.bY ? 2 : (item == Items.bZ ? 3 : 0));
        }
    }

    public boolean ck() {
        return this.x(32);
    }

    public boolean cl() {
        return this.x(64);
    }

    public boolean cm() {
        return this.x(16);
    }

    public boolean cn() {
        return this.bH;
    }

    public void d(ItemStack itemstack) {
        this.ag.b(22, Integer.valueOf(this.e(itemstack)));
        this.cN();
    }

    public void k(boolean flag0) {
        this.b(16, flag0);
    }

    public void l(boolean flag0) {
        this.b(8, flag0);
    }

    public void m(boolean flag0) {
        this.bH = flag0;
    }

    public void n(boolean flag0) {
        this.b(4, flag0);
    }

    public int co() {
        return this.bs;
    }

    public void u(int i0) {
        this.bs = i0;
    }

    public int v(int i0) {
        int i1 = MathHelper.a(this.co() + i0, 0, this.cu());

        this.u(i1);
        return i1;
    }

    public boolean a(DamageSource damagesource, float f0) {
        Entity entity = damagesource.j();

        return this.m != null && this.m.equals(entity) ? false : super.a(damagesource, f0);
    }

    public int aV() {
        return by[this.cj()];
    }

    public boolean S() {
        return this.m == null;
    }

    public boolean cp() {
        int i0 = MathHelper.c(this.t);
        int i1 = MathHelper.c(this.v);

        this.p.a(i0, i1);
        return true;
    }

    public void cq() {
        if (!this.p.E && this.ci()) {
            this.a(Item.a((Block) Blocks.ae), 1);
            this.l(false);
        }
    }

    private void cJ() {
        this.cQ();
        this.p.a((Entity) this, "eating", 1.0F, 1.0F + (this.aa.nextFloat() - this.aa.nextFloat()) * 0.2F);
    }

    protected void b(float f0) {
        if (f0 > 1.0F) {
            this.a("mob.horse.land", 0.4F, 1.0F);
        }

        int i0 = MathHelper.f(f0 * 0.5F - 3.0F);

        if (i0 > 0) {
            this.a(DamageSource.h, (float) i0);
            if (this.m != null) {
                this.m.a(DamageSource.h, (float) i0);
            }

            Block block = this.p.a(MathHelper.c(this.t), MathHelper.c(this.u - 0.2D - (double) this.B), MathHelper.c(this.v));

            if (block.o() != Material.a) {
                Block.SoundType block_soundtype = block.H;

                this.p.a((Entity) this, block_soundtype.e(), block_soundtype.c() * 0.5F, block_soundtype.d() * 0.75F);
            }
        }
    }

    private int cK() {
        int i0 = this.bX();

        return this.ci() && (i0 == 1 || i0 == 2) ? 17 : 2;
    }

    private void cL() {
        AnimalChest animalchest = this.bG;

        this.bG = new AnimalChest("HorseChest", this.cK());
        this.bG.a(this.b_());
        if (animalchest != null) {
            animalchest.b(this);
            int i0 = Math.min(animalchest.a(), this.bG.a());

            for (int i1 = 0; i1 < i0; ++i1) {
                ItemStack itemstack = animalchest.a(i1);

                if (itemstack != null) {
                    this.bG.a(i1, itemstack.m());
                }
            }

            animalchest = null;
        }

        this.bG.a(this);
        this.cM();
    }

    private void cM() {
        if (!this.p.E) {
            this.n(this.bG.a(0) != null);
            if (this.cz()) {
                this.d(this.bG.a(1));
            }
        }
    }

    public void a(InventoryBasic inventorybasic) {
        int i0 = this.cj();
        boolean flag0 = this.cs();

        this.cM();
        if (this.ab > 20) {
            if (i0 == 0 && i0 != this.cj()) {
                this.a("mob.horse.armor", 0.5F, 1.0F);
            }
            else if (i0 != this.cj()) {
                this.a("mob.horse.armor", 0.5F, 1.0F);
            }

            if (!flag0 && this.cs()) {
                this.a("mob.horse.leather", 0.5F, 1.0F);
            }
        }
    }

    public boolean bw() {
        this.cp();
        return super.bw();
    }

    protected EntityHorse a(Entity entity, double d0) {
        double d1 = Double.MAX_VALUE;
        Entity entity1 = null;
        List list = this.p.a(entity, entity.D.a(d0, d0, d0), bu);
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            Entity entity2 = (Entity) iterator.next();
            double d2 = entity2.e(entity.t, entity.u, entity.v);

            if (d2 < d1) {
                entity1 = entity2;
                d1 = d2;
            }
        }

        return (EntityHorse) entity1;
    }

    public double cr() {
        return this.a(bv).e();
    }

    protected String aU() {
        this.cQ();
        int i0 = this.bX();

        return i0 == 3 ? "mob.horse.zombie.death" : (i0 == 4 ? "mob.horse.skeleton.death" : (i0 != 1 && i0 != 2 ? "mob.horse.death" : "mob.horse.donkey.death"));
    }

    protected Item u() {
        boolean flag0 = this.aa.nextInt(4) == 0;
        int i0 = this.bX();

        return i0 == 4 ? Items.aS : (i0 == 3 ? (flag0 ? Item.d(0) : Items.bh) : Items.aA);
    }

    protected String aT() {
        this.cQ();
        if (this.aa.nextInt(3) == 0) {
            this.cS();
        }

        int i0 = this.bX();

        return i0 == 3 ? "mob.horse.zombie.hit" : (i0 == 4 ? "mob.horse.skeleton.hit" : (i0 != 1 && i0 != 2 ? "mob.horse.hit" : "mob.horse.donkey.hit"));
    }

    public boolean cs() {
        return this.x(4);
    }

    protected String t() {
        this.cQ();
        if (this.aa.nextInt(10) == 0 && !this.bh()) {
            this.cS();
        }

        int i0 = this.bX();

        return i0 == 3 ? "mob.horse.zombie.idle" : (i0 == 4 ? "mob.horse.skeleton.idle" : (i0 != 1 && i0 != 2 ? "mob.horse.idle" : "mob.horse.donkey.idle"));
    }

    protected String ct() {
        this.cQ();
        this.cS();
        int i0 = this.bX();

        return i0 != 3 && i0 != 4 ? (i0 != 1 && i0 != 2 ? "mob.horse.angry" : "mob.horse.donkey.angry") : null;
    }

    protected void a(int i0, int i1, int i2, Block block) {
        Block.SoundType block_soundtype = block.H;

        if (this.p.a(i0, i1 + 1, i2) == Blocks.aC) {
            block_soundtype = Blocks.aC.H;
        }

        if (!block.o().d()) {
            int i3 = this.bX();

            if (this.m != null && i3 != 1 && i3 != 2) {
                ++this.bP;
                if (this.bP > 5 && this.bP % 3 == 0) {
                    this.a("mob.horse.gallop", block_soundtype.c() * 0.15F, block_soundtype.d());
                    if (i3 == 0 && this.aa.nextInt(10) == 0) {
                        this.a("mob.horse.breathe", block_soundtype.c() * 0.6F, block_soundtype.d());
                    }
                }
                else if (this.bP <= 5) {
                    this.a("mob.horse.wood", block_soundtype.c() * 0.15F, block_soundtype.d());
                }
            }
            else if (block_soundtype == Block.f) {
                this.a("mob.horse.wood", block_soundtype.c() * 0.15F, block_soundtype.d());
            }
            else {
                this.a("mob.horse.soft", block_soundtype.c() * 0.15F, block_soundtype.d());
            }
        }
    }

    protected void aD() {
        super.aD();
        this.bc().b(bv);
        this.a(SharedMonsterAttributes.a).a(53.0D);
        this.a(SharedMonsterAttributes.d).a(0.22499999403953552D);
    }

    public int bz() {
        return 6;
    }

    public int cu() {
        return 100;
    }

    protected float bf() {
        return 0.8F;
    }

    public int q() {
        return 400;
    }

    private void cN() {
        this.bQ = null;
    }

    public void g(EntityPlayer entityplayer) {
        if (!this.p.E && (this.m == null || this.m == entityplayer) && this.ca()) {
            this.bG.a(this.b_());
            entityplayer.a(this, (IInventory) this.bG);
        }
    }

    public boolean a(EntityPlayer entityplayer) {
        ItemStack itemstack = entityplayer.bn.h();

        if (itemstack != null && itemstack.b() == Items.bx) {
            return super.a(entityplayer);
        }
        else if (!this.ca() && this.cC()) {
            return false;
        }
        else if (this.ca() && this.bZ() && entityplayer.an()) {
            this.g(entityplayer);
            return true;
        }
        else if (this.ce() && this.m != null) {
            return super.a(entityplayer);
        }
        else {
            if (itemstack != null) {
                boolean flag0 = false;

                if (this.cz()) {
                    byte b0 = -1;

                    if (itemstack.b() == Items.bX) {
                        b0 = 1;
                    }
                    else if (itemstack.b() == Items.bY) {
                        b0 = 2;
                    }
                    else if (itemstack.b() == Items.bZ) {
                        b0 = 3;
                    }

                    if (b0 >= 0) {
                        if (!this.ca()) {
                            this.cH();
                            return true;
                        }

                        this.g(entityplayer);
                        return true;
                    }
                }

                if (!flag0 && !this.cC()) {
                    float f0 = 0.0F;
                    short short1 = 0;
                    byte b1 = 0;

                    if (itemstack.b() == Items.O) {
                        f0 = 2.0F;
                        short1 = 60;
                        b1 = 3;
                    }
                    else if (itemstack.b() == Items.aT) {
                        f0 = 1.0F;
                        short1 = 30;
                        b1 = 3;
                    }
                    else if (itemstack.b() == Items.P) {
                        f0 = 7.0F;
                        short1 = 180;
                        b1 = 3;
                    }
                    else if (Block.a(itemstack.b()) == Blocks.cf) {
                        f0 = 20.0F;
                        short1 = 180;
                    }
                    else if (itemstack.b() == Items.e) {
                        f0 = 3.0F;
                        short1 = 60;
                        b1 = 3;
                    }
                    else if (itemstack.b() == Items.bK) {
                        f0 = 4.0F;
                        short1 = 60;
                        b1 = 5;
                        if (this.ca() && this.d() == 0) {
                            flag0 = true;
                            this.f(entityplayer);
                        }
                    }
                    else if (itemstack.b() == Items.ao) {
                        f0 = 10.0F;
                        short1 = 240;
                        b1 = 10;
                        if (this.ca() && this.d() == 0) {
                            flag0 = true;
                            this.f(entityplayer);
                        }
                    }

                    if (this.aS() < this.aY() && f0 > 0.0F) {
                        this.f(f0);
                        flag0 = true;
                    }

                    if (!this.bZ() && short1 > 0) {
                        this.a(short1);
                        flag0 = true;
                    }

                    if (b1 > 0 && (flag0 || !this.ca()) && b1 < this.cu()) {
                        flag0 = true;
                        this.v(b1);
                    }

                    if (flag0) {
                        this.cJ();
                    }
                }

                if (!this.ca() && !flag0) {
                    if (itemstack != null && itemstack.a(entityplayer, (EntityLivingBase) this)) {
                        return true;
                    }

                    this.cH();
                    return true;
                }

                if (!flag0 && this.cA() && !this.ci() && itemstack.b() == Item.a((Block) Blocks.ae)) {
                    this.l(true);
                    this.a("mob.chickenplop", 1.0F, (this.aa.nextFloat() - this.aa.nextFloat()) * 0.2F + 1.0F);
                    flag0 = true;
                    this.cL();
                }

                if (!flag0 && this.ce() && !this.cs() && itemstack.b() == Items.av) {
                    this.g(entityplayer);
                    return true;
                }

                if (flag0) {
                    if (!entityplayer.bF.d && --itemstack.b == 0) {
                        entityplayer.bn.a(entityplayer.bn.c, (ItemStack) null);
                    }

                    return true;
                }
            }

            if (this.ce() && this.m == null) {
                if (itemstack != null && itemstack.a(entityplayer, (EntityLivingBase) this)) {
                    return true;
                }
                else {
                    this.i(entityplayer);
                    return true;
                }
            }
            else {
                return super.a(entityplayer);
            }
        }
    }

    private void i(EntityPlayer entityplayer) {
        entityplayer.z = this.z;
        entityplayer.A = this.A;
        this.o(false);
        this.p(false);
        if (!this.p.E) {
            // CanaryMod: VehicleEnter/VehicleExit
            CancelableHook hook = null;
            if (this.n == null) {
                hook = new VehicleEnterHook((Vehicle) this.entity, entityplayer.getCanaryHuman());
            }
            else if (this.n == entityplayer) {
                hook = new VehicleExitHook((Vehicle) this.entity, entityplayer.getCanaryHuman());
            }
            if (hook != null) {
                hook.call();
                if (!hook.isCanceled()) {
                    entityplayer.a((Entity) this);
                }
            }
            //
        }
    }

    public boolean cz() {
        return this.bX() == 0;
    }

    public boolean cA() {
        int i0 = this.bX();

        return i0 == 2 || i0 == 1;
    }

    protected boolean bh() {
        return this.m != null && this.cs() ? true : this.ck() || this.cl();
    }

    public boolean cC() {
        int i0 = this.bX();

        return i0 == 3 || i0 == 4;
    }

    public boolean cD() {
        return this.cC() || this.bX() == 2;
    }

    public boolean c(ItemStack itemstack) {
        return false;
    }

    private void cP() {
        this.bp = 1;
    }

    public void a(DamageSource damagesource) {
        super.a(damagesource);
        if (!this.p.E) {
            this.cI();
        }
    }

    public void e() {
        if (this.aa.nextInt(200) == 0) {
            this.cP();
        }

        super.e();
        if (!this.p.E) {
            if (this.aa.nextInt(900) == 0 && this.aB == 0) {
                this.f(1.0F);
            }

            if (!this.ck() && this.m == null && this.aa.nextInt(300) == 0 && this.p.a(MathHelper.c(this.t), MathHelper.c(this.u) - 1, MathHelper.c(this.v)) == Blocks.c) {
                this.o(true);
            }

            if (this.ck() && ++this.bD > 50) {
                this.bD = 0;
                this.o(false);
            }

            if (this.cm() && !this.bZ() && !this.ck()) {
                EntityHorse entityhorse = this.a(this, 16.0D);

                if (entityhorse != null && this.e(entityhorse) > 4.0D) {
                    PathEntity pathentity = this.p.a(this, entityhorse, 16.0F, true, false, false, true);

                    this.a(pathentity);
                }
            }
        }
    }

    public void h() {
        super.h();
        if (this.p.E && this.ag.a()) {
            this.ag.e();
            this.cN();
        }

        if (this.bE > 0 && ++this.bE > 30) {
            this.bE = 0;
            this.b(128, false);
        }

        if (!this.p.E && this.bF > 0 && ++this.bF > 20) {
            this.bF = 0;
            this.p(false);
        }

        if (this.bp > 0 && ++this.bp > 8) {
            this.bp = 0;
        }

        if (this.bq > 0) {
            ++this.bq;
            if (this.bq > 300) {
                this.bq = 0;
            }
        }

        this.bK = this.bJ;
        if (this.ck()) {
            this.bJ += (1.0F - this.bJ) * 0.4F + 0.05F;
            if (this.bJ > 1.0F) {
                this.bJ = 1.0F;
            }
        }
        else {
            this.bJ += (0.0F - this.bJ) * 0.4F - 0.05F;
            if (this.bJ < 0.0F) {
                this.bJ = 0.0F;
            }
        }

        this.bM = this.bL;
        if (this.cl()) {
            this.bK = this.bJ = 0.0F;
            this.bL += (1.0F - this.bL) * 0.4F + 0.05F;
            if (this.bL > 1.0F) {
                this.bL = 1.0F;
            }
        }
        else {
            this.bI = false;
            this.bL += (0.8F * this.bL * this.bL * this.bL - this.bL) * 0.6F - 0.05F;
            if (this.bL < 0.0F) {
                this.bL = 0.0F;
            }
        }

        this.bO = this.bN;
        if (this.x(128)) {
            this.bN += (1.0F - this.bN) * 0.7F + 0.05F;
            if (this.bN > 1.0F) {
                this.bN = 1.0F;
            }
        }
        else {
            this.bN += (0.0F - this.bN) * 0.7F - 0.05F;
            if (this.bN < 0.0F) {
                this.bN = 0.0F;
            }
        }
    }

    private void cQ() {
        if (!this.p.E) {
            this.bE = 1;
            this.b(128, true);
        }
    }

    private boolean cR() {
        return this.m == null && this.n == null && this.ca() && this.bZ() && !this.cD() && this.aS() >= this.aY();
    }

    public void e(boolean flag0) {
        this.b(32, flag0);
    }

    public void o(boolean flag0) {
        this.e(flag0);
    }

    public void p(boolean flag0) {
        if (flag0) {
            this.o(false);
        }

        this.b(64, flag0);
    }

    private void cS() {
        if (!this.p.E) {
            this.bF = 1;
            this.p(true);
        }
    }

    public void cH() {
        this.cS();
        String s0 = this.ct();

        if (s0 != null) {
            this.a(s0, this.bf(), this.bg());
        }
    }

    public void cI() {
        this.a(this, this.bG);
        this.cq();
    }

    private void a(Entity entity, AnimalChest animalchest) {
        if (animalchest != null && !this.p.E) {
            for (int i0 = 0; i0 < animalchest.a(); ++i0) {
                ItemStack itemstack = animalchest.a(i0);

                if (itemstack != null) {
                    this.a(itemstack, 0.0F);
                }
            }
        }
    }

    public boolean h(EntityPlayer entityplayer) {
        this.b(entityplayer.b_());
        this.i(true);
        return true;
    }

    public void e(float f0, float f1) {
        if (this.m != null && this.cs()) {
            this.B = this.z = this.m.z;
            this.A = this.m.A * 0.5F;
            this.b(this.z, this.A);
            this.aP = this.aN = this.z;
            f0 = ((EntityLivingBase) this.m).be * 0.5F;
            f1 = ((EntityLivingBase) this.m).bf;
            if (f1 <= 0.0F) {
                f1 *= 0.25F;
                this.bP = 0;
            }

            if (this.E && this.bt == 0.0F && this.cl() && !this.bI) {
                f0 = 0.0F;
                f1 = 0.0F;
            }

            if (this.bt > 0.0F && !this.ch() && this.E) {
                this.x = this.cr() * (double) this.bt;
                if (this.a(Potion.j)) {
                    this.x += (double) ((float) (this.b(Potion.j).c() + 1) * 0.1F);
                }

                this.j(true);
                this.am = true;
                if (f1 > 0.0F) {
                    float f2 = MathHelper.a(this.z * 3.1415927F / 180.0F);
                    float f3 = MathHelper.b(this.z * 3.1415927F / 180.0F);

                    this.w += (double) (-0.4F * f2 * this.bt);
                    this.y += (double) (0.4F * f3 * this.bt);
                    this.a("mob.horse.jump", 0.4F, 1.0F);
                }

                this.bt = 0.0F;
            }

            this.X = 1.0F;
            this.aR = this.bl() * 0.1F;
            if (!this.p.E) {
                this.i((float) this.a(SharedMonsterAttributes.d).e());
                super.e(f0, f1);
            }

            if (this.E) {
                this.bt = 0.0F;
                this.j(false);
            }

            this.aF = this.aG;
            double d0 = this.t - this.q;
            double d1 = this.v - this.s;
            float f4 = MathHelper.a(d0 * d0 + d1 * d1) * 4.0F;

            if (f4 > 1.0F) {
                f4 = 1.0F;
            }

            this.aG += (f4 - this.aG) * 0.4F;
            this.aH += this.aG;
        }
        else {
            this.X = 0.5F;
            this.aR = 0.02F;
            super.e(f0, f1);
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.a("EatingHaystack", this.ck());
        nbttagcompound.a("ChestedHorse", this.ci());
        nbttagcompound.a("HasReproduced", this.cn());
        nbttagcompound.a("Bred", this.cm());
        nbttagcompound.a("Type", this.bX());
        nbttagcompound.a("Variant", this.bY());
        nbttagcompound.a("Temper", this.co());
        nbttagcompound.a("Tame", this.ca());
        nbttagcompound.a("OwnerName", this.cf());
        if (this.ci()) {
            NBTTagList nbttaglist = new NBTTagList();

            for (int i0 = 2; i0 < this.bG.a(); ++i0) {
                ItemStack itemstack = this.bG.a(i0);

                if (itemstack != null) {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();

                    nbttagcompound1.a("Slot", (byte) i0);
                    itemstack.b(nbttagcompound1);
                    nbttaglist.a((NBTBase) nbttagcompound1);
                }
            }

            nbttagcompound.a("Items", (NBTBase) nbttaglist);
        }

        if (this.bG.a(1) != null) {
            nbttagcompound.a("ArmorItem", (NBTBase) this.bG.a(1).b(new NBTTagCompound()));
        }

        if (this.bG.a(0) != null) {
            nbttagcompound.a("SaddleItem", (NBTBase) this.bG.a(0).b(new NBTTagCompound()));
        }
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.o(nbttagcompound.n("EatingHaystack"));
        this.k(nbttagcompound.n("Bred"));
        this.l(nbttagcompound.n("ChestedHorse"));
        this.m(nbttagcompound.n("HasReproduced"));
        this.s(nbttagcompound.f("Type"));
        this.t(nbttagcompound.f("Variant"));
        this.u(nbttagcompound.f("Temper"));
        this.i(nbttagcompound.n("Tame"));
        if (nbttagcompound.b("OwnerName", 8)) {
            this.b(nbttagcompound.j("OwnerName"));
        }

        AttributeInstance attributeinstance = this.bc().a("Speed");

        if (attributeinstance != null) {
            this.a(SharedMonsterAttributes.d).a(attributeinstance.b() * 0.25D);
        }

        if (this.ci()) {
            NBTTagList nbttaglist = nbttagcompound.c("Items", 10);

            this.cL();

            for (int i0 = 0; i0 < nbttaglist.c(); ++i0) {
                NBTTagCompound nbttagcompound1 = nbttaglist.b(i0);
                int i1 = nbttagcompound1.d("Slot") & 255;

                if (i1 >= 2 && i1 < this.bG.a()) {
                    this.bG.a(i1, ItemStack.a(nbttagcompound1));
                }
            }
        }

        ItemStack itemstack;

        if (nbttagcompound.b("ArmorItem", 10)) {
            itemstack = ItemStack.a(nbttagcompound.m("ArmorItem"));
            if (itemstack != null && a(itemstack.b())) {
                this.bG.a(1, itemstack);
            }
        }

        if (nbttagcompound.b("SaddleItem", 10)) {
            itemstack = ItemStack.a(nbttagcompound.m("SaddleItem"));
            if (itemstack != null && itemstack.b() == Items.av) {
                this.bG.a(0, itemstack);
            }
        }
        else if (nbttagcompound.n("Saddle")) {
            this.bG.a(0, new ItemStack(Items.av));
        }

        this.cM();
    }

    public boolean a(EntityAnimal entityanimal) {
        if (entityanimal == this) {
            return false;
        }
        else if (entityanimal.getClass() != this.getClass()) {
            return false;
        }
        else {
            EntityHorse entityhorse = (EntityHorse) entityanimal;

            if (this.cR() && entityhorse.cR()) {
                int i0 = this.bX();
                int i1 = entityhorse.bX();

                return i0 == i1 || i0 == 0 && i1 == 1 || i0 == 1 && i1 == 0;
            }
            else {
                return false;
            }
        }
    }

    public EntityAgeable a(EntityAgeable entityageable) {
        EntityHorse entityhorse = (EntityHorse) entityageable;
        EntityHorse entityhorse1 = new EntityHorse(this.p);
        int i0 = this.bX();
        int i1 = entityhorse.bX();
        int i2 = 0;

        if (i0 == i1) {
            i2 = i0;
        }
        else if (i0 == 0 && i1 == 1 || i0 == 1 && i1 == 0) {
            i2 = 2;
        }

        if (i2 == 0) {
            int i3 = this.aa.nextInt(9);
            int i4;

            if (i3 < 4) {
                i4 = this.bY() & 255;
            }
            else if (i3 < 8) {
                i4 = entityhorse.bY() & 255;
            }
            else {
                i4 = this.aa.nextInt(7);
            }

            int i5 = this.aa.nextInt(5);

            if (i5 < 2) {
                i4 |= this.bY() & '\uff00';
            }
            else if (i5 < 4) {
                i4 |= entityhorse.bY() & '\uff00';
            }
            else {
                i4 |= this.aa.nextInt(5) << 8 & '\uff00';
            }

            entityhorse1.t(i4);
        }

        entityhorse1.s(i2);
        double d0 = this.a(SharedMonsterAttributes.a).b() + entityageable.a(SharedMonsterAttributes.a).b() + (double) this.cT();

        entityhorse1.a(SharedMonsterAttributes.a).a(d0 / 3.0D);
        double d1 = this.a(bv).b() + entityageable.a(bv).b() + this.cU();

        entityhorse1.a(bv).a(d1 / 3.0D);
        double d2 = this.a(SharedMonsterAttributes.d).b() + entityageable.a(SharedMonsterAttributes.d).b() + this.cV();

        entityhorse1.a(SharedMonsterAttributes.d).a(d2 / 3.0D);
        return entityhorse1;
    }

    public EntityLivingData a(EntityLivingData entitylivingdata) {
        Object object = super.a(entitylivingdata);
        boolean flag0 = false;
        int i0 = 0;
        int i1;

        if (object instanceof EntityHorse.GroupData) {
            i1 = ((EntityHorse.GroupData) object).a;
            i0 = ((EntityHorse.GroupData) object).b & 255 | this.aa.nextInt(5) << 8;
        }
        else {
            if (this.aa.nextInt(10) == 0) {
                i1 = 1;
            }
            else {
                int i2 = this.aa.nextInt(7);
                int i3 = this.aa.nextInt(5);

                i1 = 0;
                i0 = i2 | i3 << 8;
            }

            object = new EntityHorse.GroupData(i1, i0);
        }

        this.s(i1);
        this.t(i0);
        if (this.aa.nextInt(5) == 0) {
            this.c(-24000);
        }

        if (i1 != 4 && i1 != 3) {
            this.a(SharedMonsterAttributes.a).a((double) this.cT());
            if (i1 == 0) {
                this.a(SharedMonsterAttributes.d).a(this.cV());
            }
            else {
                this.a(SharedMonsterAttributes.d).a(0.17499999701976776D);
            }
        }
        else {
            this.a(SharedMonsterAttributes.a).a(15.0D);
            this.a(SharedMonsterAttributes.d).a(0.20000000298023224D);
        }

        if (i1 != 2 && i1 != 1) {
            this.a(bv).a(this.cU());
        }
        else {
            this.a(bv).a(0.5D);
        }

        this.g(this.aY());
        return (EntityLivingData) object;
    }

    protected boolean bk() {
        return true;
    }

    public void w(int i0) {
        if (this.cs()) {
            if (i0 < 0) {
                i0 = 0;
            }
            else {
                this.bI = true;
                this.cS();
            }

            if (i0 >= 90) {
                this.bt = 1.0F;
            }
            else {
                this.bt = 0.4F + 0.4F * (float) i0 / 90.0F;
            }
        }
    }

    public void ac() {
        super.ac();
        if (this.bM > 0.0F) {
            float f0 = MathHelper.a(this.aN * 3.1415927F / 180.0F);
            float f1 = MathHelper.b(this.aN * 3.1415927F / 180.0F);
            float f2 = 0.7F * this.bM;
            float f3 = 0.15F * this.bM;

            this.m.b(this.t + (double) (f2 * f0), this.u + this.ae() + this.m.ad() + (double) f3, this.v - (double) (f2 * f1));
            if (this.m instanceof EntityLivingBase) {
                ((EntityLivingBase) this.m).aN = this.aN;
            }
        }
    }

    private float cT() {
        return 15.0F + (float) this.aa.nextInt(8) + (float) this.aa.nextInt(9);
    }

    private double cU() {
        return 0.4000000059604645D + this.aa.nextDouble() * 0.2D + this.aa.nextDouble() * 0.2D + this.aa.nextDouble() * 0.2D;
    }

    private double cV() {
        return (0.44999998807907104D + this.aa.nextDouble() * 0.3D + this.aa.nextDouble() * 0.3D + this.aa.nextDouble() * 0.3D) * 0.25D;
    }

    public static boolean a(Item item) {
        return item == Items.bX || item == Items.bY || item == Items.bZ;
    }

    public boolean h_() {
        return false;
    }

    public static class GroupData implements EntityLivingData {

        public int a;
        public int b;

        public GroupData(int i0, int i1) {
            this.a = i0;
            this.b = i1;
        }
    }
}

