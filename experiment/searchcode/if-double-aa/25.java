package net.minecraft.entity;

import net.canarymod.api.CanaryDamageSource;
import net.canarymod.api.entity.living.CanaryLivingBase;
import net.canarymod.api.entity.living.humanoid.EntityNonPlayableCharacter;
import net.canarymod.api.potion.CanaryPotionEffect;
import net.canarymod.hook.entity.DamageHook;
import net.canarymod.hook.entity.EntityDeathHook;
import net.canarymod.hook.entity.PotionEffectAppliedHook;
import net.canarymod.hook.entity.PotionEffectFinishHook;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.ServersideAttributeMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public abstract class EntityLivingBase extends Entity {

    private static final UUID b = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    private static final AttributeModifier c = (new AttributeModifier(b, "Sprinting speed boost", 0.30000001192092896D, 2)).a(false);
    private BaseAttributeMap d;
    private final CombatTracker e = new CombatTracker(this);
    private final HashMap f = new HashMap();
    private final ItemStack[] g = new ItemStack[5];
    public boolean au;
    public int av;
    public int aw;
    public float ax;
    public int ay;
    public int az;
    public float aA;
    public int aB;
    public int aC;
    public float aD;
    public float aE;
    public float aF;
    public float aG;
    public float aH;
    public int aI = 20;
    public float aJ;
    public float aK;
    public float aL;
    public float aM;
    public float aN;
    public float aO;
    public float aP;
    public float aQ;
    public float aR = 0.02F;
    protected EntityPlayer aS;
    protected int aT;
    protected boolean aU;
    protected int aV;
    protected float aW;
    protected float aX;
    protected float aY;
    protected float aZ;
    protected float ba;
    protected int bb;
    protected float bc;
    protected boolean bd;
    public float be;
    public float bf;
    protected float bg;
    protected int bh;
    protected double bi;
    protected double bj;
    protected double bk;
    protected double bl;
    protected double bm;
    private boolean h = true;
    private EntityLivingBase i;
    private int j;
    private EntityLivingBase bn;
    private int bo;
    private float bp;
    private int bq;
    private float br;

    public EntityLivingBase(World world) {
        super(world);
        this.aD();
        this.g(this.aY());
        this.l = true;
        this.aM = (float) (Math.random() + 1.0D) * 0.01F;
        this.b(this.t, this.u, this.v);
        this.aL = (float) Math.random() * 12398.0F;
        this.z = (float) (Math.random() * 3.1415927410125732D * 2.0D);
        this.aP = this.z;
        this.X = 0.5F;
    }

    protected void c() {
        this.ag.a(7, Integer.valueOf(0));
        this.ag.a(8, Byte.valueOf((byte) 0));
        this.ag.a(9, Byte.valueOf((byte) 0));
        this.ag.a(6, Float.valueOf(1.0F));
    }

    protected void aD() {
        this.bc().b(SharedMonsterAttributes.a);
        this.bc().b(SharedMonsterAttributes.c);
        this.bc().b(SharedMonsterAttributes.d);
        if (!this.bk()) {
            this.a(SharedMonsterAttributes.d).a(0.10000000149011612D);
        }
    }

    protected void a(double d0, boolean flag0) {
        if (!this.M()) {
            this.N();
        }

        if (flag0 && this.S > 0.0F) {
            int i0 = MathHelper.c(this.t);
            int i1 = MathHelper.c(this.u - 0.20000000298023224D - (double) this.M);
            int i2 = MathHelper.c(this.v);
            Block block = this.p.a(i0, i1, i2);

            if (block.o() == Material.a) {
                int i3 = this.p.a(i0, i1 - 1, i2).b();

                if (i3 == 11 || i3 == 32 || i3 == 21) {
                    block = this.p.a(i0, i1 - 1, i2);
                }
            }
            else if (!this.p.E && this.S > 3.0F) {
                this.p.c(2006, i0, i1, i2, MathHelper.f(this.S - 3.0F));
            }

            block.a(this.p, i0, i1, i2, this, this.S);
        }

        super.a(d0, flag0);
    }

    public boolean aE() {
        return false;
    }

    public void C() {
        this.aD = this.aE;
        super.C();
        this.p.C.a("livingEntityBaseTick");
        if (this.Z() && this.aa()) {
            // CanaryMod: call DamageHook (Suffocation)
            DamageHook hook = (DamageHook) new DamageHook(null, entity, new CanaryDamageSource(DamageSource.d), 1.0F).call();
            if (!hook.isCanceled()) {
                this.a((((CanaryDamageSource) hook.getDamageSource()).getHandle()), hook.getDamageDealt());
            }
            //
        }

        if (this.K() || this.p.E) {
            this.F();
        }

        boolean flag0 = this instanceof EntityPlayer && ((EntityPlayer) this).bF.a;

        if (this.Z() && this.a(Material.h)) {
            if (!this.aE() && !this.k(Potion.o.H) && !flag0) {
                this.h(this.j(this.ar()));
                if (this.ar() == -20) {
                    this.h(0);

                    // CanaryMod - drowning damage.
                    DamageHook hook = (DamageHook) new DamageHook(null, entity, new CanaryDamageSource(DamageSource.e), 2.0F).call();
                    if (!hook.isCanceled()) {
                        for (int i0 = 0; i0 < 8; ++i0) {
                            float f0 = this.aa.nextFloat() - this.aa.nextFloat();
                            float f1 = this.aa.nextFloat() - this.aa.nextFloat();
                            float f2 = this.aa.nextFloat() - this.aa.nextFloat();

                            this.p.a("bubble", this.t + (double) f0, this.u + (double) f1, this.v + (double) f2, this.w, this.x, this.y);
                        }

                        this.a((((CanaryDamageSource) hook.getDamageSource()).getHandle()), hook.getDamageDealt());
                    }
                    //
                }
            }

            if (!this.p.E && this.am() && this.n instanceof EntityLivingBase) {
                this.a((Entity) null);
            }
        }
        else {
            this.h(300);
        }

        if (this.Z() && this.L()) {
            this.F();
        }

        this.aJ = this.aK;
        if (this.aC > 0) {
            --this.aC;
        }

        if (this.ay > 0) {
            --this.ay;
        }

        if (this.ae > 0 && !(this instanceof EntityPlayerMP)) {
            --this.ae;
        }

        if (this.aS() <= 0.0F) {
            this.aF();
        }

        if (this.aT > 0) {
            --this.aT;
        }
        else {
            this.aS = null;
        }

        if (this.bn != null && !this.bn.Z()) {
            this.bn = null;
        }

        if (this.i != null) {
            if (!this.i.Z()) {
                this.b((EntityLivingBase) null);
            }
            else if (this.ab - this.j > 100) {
                this.b((EntityLivingBase) null);
            }
        }

        this.aO();
        this.aZ = this.aY;
        this.aO = this.aN;
        this.aQ = this.aP;
        this.B = this.z;
        this.C = this.A;
        this.p.C.b();
    }

    public boolean f() {
        return false;
    }

    protected void aF() {
        ++this.aB;
        if (this.aB == 20) {
            int i0;

            if (!this.p.E && (this.aT > 0 || this.aH()) && this.aG() && this.p.N().b("doMobLoot")) {
                i0 = this.e(this.aS);

                while (i0 > 0) {
                    int i1 = EntityXPOrb.a(i0);

                    i0 -= i1;
                    this.p.d((Entity) (new EntityXPOrb(this.p, this.t, this.u, this.v, i1)));
                }
            }

            this.B();

            for (i0 = 0; i0 < 20; ++i0) {
                double d0 = this.aa.nextGaussian() * 0.02D;
                double d1 = this.aa.nextGaussian() * 0.02D;
                double d2 = this.aa.nextGaussian() * 0.02D;

                this.p.a("explode", this.t + (double) (this.aa.nextFloat() * this.N * 2.0F) - (double) this.N, this.u + (double) (this.aa.nextFloat() * this.O), this.v + (double) (this.aa.nextFloat() * this.N * 2.0F) - (double) this.N, d0, d1, d2);
            }
        }
    }

    protected boolean aG() {
        return !this.f();
    }

    protected int j(int i0) {
        int i1 = EnchantmentHelper.b(this);

        return i1 > 0 && this.aa.nextInt(i1 + 1) > 0 ? i0 : i0 - 1;
    }

    protected int e(EntityPlayer entityplayer) {
        return 0;
    }

    protected boolean aH() {
        return false;
    }

    public Random aI() {
        return this.aa;
    }

    public EntityLivingBase aJ() {
        return this.i;
    }

    public int aK() {
        return this.j;
    }

    public void b(EntityLivingBase entitylivingbase) {
        this.i = entitylivingbase;
        this.j = this.ab;
    }

    public EntityLivingBase aL() {
        return this.bn;
    }

    public int aM() {
        return this.bo;
    }

    public void k(Entity entity) {
        if (entity instanceof EntityLivingBase) {
            this.bn = (EntityLivingBase) entity;
        }
        else {
            this.bn = null;
        }

        this.bo = this.ab;
    }

    public int aN() {
        return this.aV;
    }

    public void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.a("HealF", this.aS());
        nbttagcompound.a("Health", (short) ((int) Math.ceil((double) this.aS())));
        nbttagcompound.a("HurtTime", (short) this.ay);
        nbttagcompound.a("DeathTime", (short) this.aB);
        nbttagcompound.a("AttackTime", (short) this.aC);
        nbttagcompound.a("AbsorptionAmount", this.bs());
        ItemStack[] aitemstack = this.ak();
        int i0 = aitemstack.length;

        int i1;
        ItemStack itemstack;

        for (i1 = 0; i1 < i0; ++i1) {
            itemstack = aitemstack[i1];
            if (itemstack != null) {
                this.d.a(itemstack.D());
            }
        }

        nbttagcompound.a("Attributes", (NBTBase) SharedMonsterAttributes.a(this.bc()));
        aitemstack = this.ak();
        i0 = aitemstack.length;

        for (i1 = 0; i1 < i0; ++i1) {
            itemstack = aitemstack[i1];
            if (itemstack != null) {
                this.d.b(itemstack.D());
            }
        }

        if (!this.f.isEmpty()) {
            NBTTagList nbttaglist = new NBTTagList();
            Iterator iterator = this.f.values().iterator();

            while (iterator.hasNext()) {
                PotionEffect potioneffect = (PotionEffect) iterator.next();

                nbttaglist.a((NBTBase) potioneffect.a(new NBTTagCompound()));
            }

            nbttagcompound.a("ActiveEffects", (NBTBase) nbttaglist);
        }
    }

    public void a(NBTTagCompound nbttagcompound) {
        this.m(nbttagcompound.h("AbsorptionAmount"));
        if (nbttagcompound.b("Attributes", 9) && this.p != null && !this.p.E) {
            SharedMonsterAttributes.a(this.bc(), nbttagcompound.c("Attributes", 10));
        }

        if (nbttagcompound.b("ActiveEffects", 9)) {
            NBTTagList nbttaglist = nbttagcompound.c("ActiveEffects", 10);

            for (int i0 = 0; i0 < nbttaglist.c(); ++i0) {
                NBTTagCompound nbttagcompound1 = nbttaglist.b(i0);
                PotionEffect potioneffect = PotionEffect.b(nbttagcompound1);

                if (potioneffect != null) {
                    this.f.put(Integer.valueOf(potioneffect.a()), potioneffect);
                }
            }
        }

        if (nbttagcompound.b("HealF", 99)) {
            this.g(nbttagcompound.h("HealF"));
        }
        else {
            NBTBase nbtbase = nbttagcompound.a("Health");

            if (nbtbase == null) {
                this.g(this.aY());
            }
            else if (nbtbase.a() == 5) {
                this.g(((NBTTagFloat) nbtbase).h());
            }
            else if (nbtbase.a() == 2) {
                this.g((float) ((NBTTagShort) nbtbase).e());
            }
        }

        this.ay = nbttagcompound.e("HurtTime");
        this.aB = nbttagcompound.e("DeathTime");
        this.aC = nbttagcompound.e("AttackTime");
    }

    protected void aO() {
        Iterator iterator = this.f.keySet().iterator();

        while (iterator.hasNext()) {
            Integer integer = (Integer) iterator.next();
            PotionEffect potioneffect = (PotionEffect) this.f.get(integer);

            if (!potioneffect.a(this)) {
                if (!this.p.E) {
                    iterator.remove();
                    this.b(potioneffect);
                }
            }
            else if (potioneffect.b() % 600 == 0) {
                this.a(potioneffect, false);
            }
        }

        int i0;

        if (this.h) {
            if (!this.p.E) {
                if (this.f.isEmpty()) {
                    this.ag.b(8, Byte.valueOf((byte) 0));
                    this.ag.b(7, Integer.valueOf(0));
                    this.d(false);
                }
                else {
                    i0 = PotionHelper.a(this.f.values());
                    this.ag.b(8, Byte.valueOf((byte) (PotionHelper.b(this.f.values()) ? 1 : 0)));
                    this.ag.b(7, Integer.valueOf(i0));
                    this.d(this.k(Potion.p.H));
                }
            }

            this.h = false;
        }

        i0 = this.ag.c(7);
        boolean flag0 = this.ag.a(8) > 0;

        if (i0 > 0) {
            boolean flag1 = false;

            if (!this.ap()) {
                flag1 = this.aa.nextBoolean();
            }
            else {
                flag1 = this.aa.nextInt(15) == 0;
            }

            if (flag0) {
                flag1 &= this.aa.nextInt(5) == 0;
            }

            if (flag1 && i0 > 0) {
                double d0 = (double) (i0 >> 16 & 255) / 255.0D;
                double d1 = (double) (i0 >> 8 & 255) / 255.0D;
                double d2 = (double) (i0 >> 0 & 255) / 255.0D;

                this.p.a(flag0 ? "mobSpellAmbient" : "mobSpell", this.t + (this.aa.nextDouble() - 0.5D) * (double) this.N, this.u + this.aa.nextDouble() * (double) this.O - (double) this.M, this.v + (this.aa.nextDouble() - 0.5D) * (double) this.N, d0, d1, d2);
            }
        }
    }

    public void aP() {
        Iterator iterator = this.f.keySet().iterator();

        while (iterator.hasNext()) {
            Integer integer = (Integer) iterator.next();
            PotionEffect potioneffect = (PotionEffect) this.f.get(integer);

            if (!this.p.E) {
                iterator.remove();
                this.b(potioneffect);
            }
        }
    }

    public Collection aQ() {
        return this.f.values();
    }

    public boolean k(int i0) {
        return this.f.containsKey(Integer.valueOf(i0));
    }

    public boolean a(Potion potion) {
        return this.f.containsKey(Integer.valueOf(potion.H));
    }

    public PotionEffect b(Potion potion) {
        return (PotionEffect) this.f.get(Integer.valueOf(potion.H));
    }

    public void c(PotionEffect potioneffect) {
        if (this.d(potioneffect)) {
            // CanaryMod: PotionEffectApplied
            PotionEffectAppliedHook hook = (PotionEffectAppliedHook) new PotionEffectAppliedHook((net.canarymod.api.entity.living.LivingBase) getCanaryEntity(), new CanaryPotionEffect(potioneffect)).call();
            if (hook.getPotionEffect() == null) {
                return;
            }
            potioneffect = ((CanaryPotionEffect) hook.getPotionEffect()).getHandle();
            //
            if (this.d(potioneffect)) {
                if (this.f.containsKey(Integer.valueOf(potioneffect.a()))) {
                    ((PotionEffect) this.f.get(Integer.valueOf(potioneffect.a()))).a(potioneffect);
                    this.a((PotionEffect) this.f.get(Integer.valueOf(potioneffect.a())), true);
                }
                else {
                    this.f.put(Integer.valueOf(potioneffect.a()), potioneffect);
                    this.a(potioneffect);
                }
            }
        }
    }

    public boolean d(PotionEffect potioneffect) {
        if (this.bd() == EnumCreatureAttribute.UNDEAD) {
            int i0 = potioneffect.a();

            if (i0 == Potion.l.H || i0 == Potion.u.H) {
                return false;
            }
        }

        return true;
    }

    public boolean aR() {
        return this.bd() == EnumCreatureAttribute.UNDEAD;
    }

    public void m(int i0) {
        PotionEffect potioneffect = (PotionEffect) this.f.remove(Integer.valueOf(i0));

        if (potioneffect != null) {
            this.b(potioneffect);
        }
    }

    protected void a(PotionEffect potioneffect) {
        this.h = true;
        if (!this.p.E) {
            Potion.a[potioneffect.a()].b(this, this.bc(), potioneffect.c());
        }
    }

    protected void a(PotionEffect potioneffect, boolean flag0) {
        this.h = true;
        if (flag0 && !this.p.E) {
            Potion.a[potioneffect.a()].a(this, this.bc(), potioneffect.c());
            Potion.a[potioneffect.a()].b(this, this.bc(), potioneffect.c());
        }
    }

    protected void b(PotionEffect potioneffect) {
        this.h = true;
        if (!this.p.E) {
            // CanaryMod: PotionEffectFinish
            new PotionEffectFinishHook((net.canarymod.api.entity.living.LivingBase) getCanaryEntity(), new CanaryPotionEffect(potioneffect)).call();
            //
            Potion.a[potioneffect.a()].a(this, this.bc(), potioneffect.c());
        }
    }

    public void f(float f0) {
        float f1 = this.aS();

        if (f1 > 0.0F) {
            this.g(f1 + f0);
        }
    }

    public final float aS() {
        return this.ag.d(6);
    }

    public void g(float f0) {
        this.ag.b(6, Float.valueOf(MathHelper.a(f0, 0.0F, this.aY())));
    }

    public boolean a(DamageSource damagesource, float f0) {
        if (this.aw()) {
            return false;
        }
        else if (this.p.E) {
            return false;
        }
        else {
            this.aV = 0;
            if (this.aS() <= 0.0F) {
                return false;
            }
            else if (damagesource.o() && this.a(Potion.n)) {
                return false;
            }
            else {
                if ((damagesource == DamageSource.m || damagesource == DamageSource.n) && this.q(4) != null) {
                    this.q(4).a((int) (f0 * 4.0F + this.aa.nextFloat() * f0 * 2.0F), this);
                    f0 *= 0.75F;
                }

                this.aG = 1.5F;
                boolean flag0 = true;

                // CanaryMod: call DamageHook (Entity)
                CanaryLivingBase attacker = null;

                if (damagesource instanceof EntityDamageSource && (damagesource).j() instanceof EntityLivingBase) {
                    attacker = (CanaryLivingBase) (damagesource).j().getCanaryEntity();
                }
                DamageHook hook = new DamageHook(attacker, entity, new CanaryDamageSource(damagesource), f0);

                if ((float) this.ae > (float) this.aI / 2.0F) {
                    if (f0 <= this.bc) {
                        return false;
                    }

                    hook.setDamageDealt((f0 - this.bc));
                    if (attacker != null) {
                        hook.call();
                    }
                    if (hook.isCanceled()) {
                        return false;
                    }

                    this.d((((CanaryDamageSource) hook.getDamageSource()).getHandle()), hook.getDamageDealt());
                    this.bc = f0;
                    flag0 = false;
                }
                else {
                    if (attacker != null) {
                        hook.call();
                    }
                    if (hook.isCanceled()) {
                        return false;
                    }
                    this.bc = f0;
                    this.ax = this.aS();
                    this.ae = this.aI;
                    this.d((((CanaryDamageSource) hook.getDamageSource()).getHandle()), hook.getDamageDealt());
                    this.ay = this.az = 10;
                }
                //

                this.aA = 0.0F;
                Entity entity = damagesource.j();

                if (entity != null) {
                    if (entity instanceof EntityLivingBase) {
                        this.b((EntityLivingBase) entity);
                    }

                    if (entity instanceof EntityPlayer) {
                        this.aT = 100;
                        this.aS = (EntityPlayer) entity;
                    }
                    else if (entity instanceof EntityWolf) {
                        EntityWolf entitywolf = (EntityWolf) entity;

                        if (entitywolf.bX()) {
                            this.aT = 100;
                            this.aS = null;
                        }
                    }
                }

                if (flag0) {
                    this.p.a(this, (byte) 2);
                    if (damagesource != DamageSource.e) {
                        this.Q();
                    }

                    if (entity != null) {
                        double d0 = entity.t - this.t;

                        double d1;

                        for (d1 = entity.v - this.v; d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
                            d0 = (Math.random() - Math.random()) * 0.01D;
                        }

                        this.aA = (float) (Math.atan2(d1, d0) * 180.0D / 3.1415927410125732D) - this.z;
                        this.a(entity, f0, d0, d1);
                    }
                    else {
                        this.aA = (float) ((int) (Math.random() * 2.0D) * 180);
                    }
                }

                String s0;

                if (this.aS() <= 0.0F) {
                    s0 = this.aU();
                    if (flag0 && s0 != null) {
                        this.a(s0, this.bf(), this.bg());
                    }

                    this.a(damagesource);
                }
                else {
                    s0 = this.aT();
                    if (flag0 && s0 != null) {
                        this.a(s0, this.bf(), this.bg());
                    }
                }

                return true;
            }
        }
    }

    public void a(ItemStack itemstack) {
        this.a("random.break", 0.8F, 0.8F + this.p.s.nextFloat() * 0.4F);

        for (int i0 = 0; i0 < 5; ++i0) {
            Vec3 vec3 = this.p.U().a(((double) this.aa.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);

            vec3.a(-this.A * 3.1415927F / 180.0F);
            vec3.b(-this.z * 3.1415927F / 180.0F);
            Vec3 vec31 = this.p.U().a(((double) this.aa.nextFloat() - 0.5D) * 0.3D, (double) (-this.aa.nextFloat()) * 0.6D - 0.3D, 0.6D);

            vec31.a(-this.A * 3.1415927F / 180.0F);
            vec31.b(-this.z * 3.1415927F / 180.0F);
            vec31 = vec31.c(this.t, this.u + (double) this.g(), this.v);
            this.p.a("iconcrack_" + Item.b(itemstack.b()), vec31.c, vec31.d, vec31.e, vec3.c, vec3.d + 0.05D, vec3.e);
        }
    }

    public void a(DamageSource damagesource) {
        // CanaryMod: EntityDeath
        new EntityDeathHook(this.getCanaryEntity(), damagesource.getCanaryDamageSource()).call();
        //
        Entity entity = damagesource.j();
        EntityLivingBase entitylivingbase = this.aX();

        if (this.bb >= 0 && entitylivingbase != null) {
            entitylivingbase.b(this, this.bb);
        }

        if (entity != null) {
            entity.a(this);
        }

        this.aU = true;
        if (!this.p.E) {
            int i0 = 0;

            if (entity instanceof EntityPlayer) {
                i0 = EnchantmentHelper.i((EntityLivingBase) entity);
            }

            if (this.aG() && this.p.N().b("doMobLoot")) {
                this.b(this.aT > 0, i0);
                this.a(this.aT > 0, i0);
                if (this.aT > 0) {
                    int i1 = this.aa.nextInt(200) - i0;

                    if (i1 < 5) {
                        this.n(i1 <= 0 ? 1 : 0);
                    }
                }
            }
        }

        this.p.a(this, (byte) 3);
    }

    protected void a(boolean flag0, int i0) {
    }

    public void a(Entity entity, float f0, double d0, double d1) {
        if (this.aa.nextDouble() >= this.a(SharedMonsterAttributes.c).e()) {
            this.am = true;
            float f1 = MathHelper.a(d0 * d0 + d1 * d1);
            float f2 = 0.4F;

            this.w /= 2.0D;
            this.x /= 2.0D;
            this.y /= 2.0D;
            this.w -= d0 / (double) f1 * (double) f2;
            this.x += (double) f2;
            this.y -= d1 / (double) f1 * (double) f2;
            if (this.x > 0.4000000059604645D) {
                this.x = 0.4000000059604645D;
            }
        }
    }

    protected String aT() {
        return "game.neutral.hurt";
    }

    protected String aU() {
        return "game.neutral.die";
    }

    protected void n(int i0) {
    }

    protected void b(boolean flag0, int i0) {
    }

    public boolean h_() {
        int i0 = MathHelper.c(this.t);
        int i1 = MathHelper.c(this.D.b);
        int i2 = MathHelper.c(this.v);
        Block block = this.p.a(i0, i1, i2);

        return block == Blocks.ap || block == Blocks.bd;
    }

    public boolean Z() {
        return !this.L && this.aS() > 0.0F;
    }

    protected void b(float f0) {
        super.b(f0);
        PotionEffect potioneffect = this.b(Potion.j);
        float f1 = potioneffect != null ? (float) (potioneffect.c() + 1) : 0.0F;
        int i0 = MathHelper.f(f0 - 3.0F - f1);

        if (i0 > 0) {
            this.a(this.o(i0), 1.0F, 1.0F);
            // CanaryMod: call DamageHook (Fall)
            DamageHook hook = (DamageHook) new DamageHook(null, entity, new CanaryDamageSource(DamageSource.h), i0).call();

            if (!hook.isCanceled()) {
                this.a((((CanaryDamageSource) hook.getDamageSource()).getHandle()), hook.getDamageDealt());
            }
            //
            int i1 = MathHelper.c(this.t);
            int i2 = MathHelper.c(this.u - 0.20000000298023224D - (double) this.M);
            int i3 = MathHelper.c(this.v);
            Block block = this.p.a(i1, i2, i3);
            if (block.o() != Material.a) {
                Block.SoundType block_soundtype = block.H;
                this.a(block_soundtype.e(), block_soundtype.c() * 0.5F, block_soundtype.d() * 0.75F);
            }
        }
    }

    protected String o(int i0) {
        return i0 > 4 ? "game.neutral.hurt.fall.big" : "game.neutral.hurt.fall.small";
    }

    public int aV() {
        int i0 = 0;
        ItemStack[] aitemstack = this.ak();
        int i1 = aitemstack.length;

        for (int i2 = 0; i2 < i1; ++i2) {
            ItemStack itemstack = aitemstack[i2];

            if (itemstack != null && itemstack.b() instanceof ItemArmor) {
                int i3 = ((ItemArmor) itemstack.b()).c;

                i0 += i3;
            }
        }

        return i0;
    }

    protected void h(float f0) {
    }

    protected float b(DamageSource damagesource, float f0) {
        if (!damagesource.e()) {
            int i0 = 25 - this.aV();
            float f1 = f0 * (float) i0;

            this.h(f0);
            f0 = f1 / 25.0F;
        }

        return f0;
    }

    protected float c(DamageSource damagesource, float f0) {
        if (damagesource.h()) {
            return f0;
        }
        else {
            if (this instanceof EntityZombie) {
                f0 = f0;
            }

            int i0;
            int i1;
            float f1;

            if (this.a(Potion.m) && damagesource != DamageSource.i) {
                i0 = (this.b(Potion.m).c() + 1) * 5;
                i1 = 25 - i0;
                f1 = f0 * (float) i1;
                f0 = f1 / 25.0F;
            }

            if (f0 <= 0.0F) {
                return 0.0F;
            }
            else {
                i0 = EnchantmentHelper.a(this.ak(), damagesource);
                if (i0 > 20) {
                    i0 = 20;
                }

                if (i0 > 0 && i0 <= 20) {
                    i1 = 25 - i0;
                    f1 = f0 * (float) i1;
                    f0 = f1 / 25.0F;
                }

                return f0;
            }
        }
    }

    protected void d(DamageSource damagesource, float f0) {
        if (!this.aw()) {
            f0 = this.b(damagesource, f0);
            f0 = this.c(damagesource, f0);
            float f1 = f0;

            f0 = Math.max(f0 - this.bs(), 0.0F);
            this.m(this.bs() - (f1 - f0));
            if (f0 != 0.0F) {
                float f2 = this.aS();

                this.g(f2 - f0);
                this.aW().a(damagesource, f2, f0);
                this.m(this.bs() - f0);
            }
        }
    }

    public CombatTracker aW() {
        return this.e;
    }

    public EntityLivingBase aX() {
        return (EntityLivingBase) (this.e.c() != null ? this.e.c() : (this.aS != null ? this.aS : (this.i != null ? this.i : null)));
    }

    public final float aY() {
        return (float) this.a(SharedMonsterAttributes.a).e();
    }

    public final int aZ() {
        return this.ag.a(9);
    }

    public final void p(int i0) {
        this.ag.b(9, Byte.valueOf((byte) i0));
    }

    private int j() {
        return this.a(Potion.e) ? 6 - (1 + this.b(Potion.e).c()) * 1 : (this.a(Potion.f) ? 6 + (1 + this.b(Potion.f).c()) * 2 : 6);
    }

    public void ba() {
        if (!this.au || this.av >= this.j() / 2 || this.av < 0) {
            this.av = -1;
            this.au = true;
            if (this.p instanceof WorldServer) {
                ((WorldServer) this.p).q().a((Entity) this, (Packet) (new S0BPacketAnimation(this, 0)));
            }
        }
    }

    protected void G() {
        this.a(DamageSource.i, 4.0F);
    }

    protected void bb() {
        int i0 = this.j();

        if (this.au) {
            ++this.av;
            if (this.av >= i0) {
                this.av = 0;
                this.au = false;
            }
        }
        else {
            this.av = 0;
        }

        this.aE = (float) this.av / (float) i0;
    }

    public AttributeInstance a(Attribute attribute) {
        return this.bc().a(attribute);
    }

    public BaseAttributeMap bc() {
        if (this.d == null) {
            this.d = new ServersideAttributeMap();
        }

        return this.d;
    }

    public EnumCreatureAttribute bd() {
        return EnumCreatureAttribute.UNDEFINED;
    }

    public abstract ItemStack be();

    public abstract ItemStack q(int i0);

    public abstract void c(int i0, ItemStack itemstack);

    public void c(boolean flag0) {
        super.c(flag0);
        AttributeInstance attributeinstance = this.a(SharedMonsterAttributes.d);

        if (attributeinstance.a(b) != null) {
            attributeinstance.b(c);
        }

        if (flag0) {
            attributeinstance.a(c);
        }
    }

    public abstract ItemStack[] ak();

    protected float bf() {
        return 1.0F;
    }

    protected float bg() {
        return this.f() ? (this.aa.nextFloat() - this.aa.nextFloat()) * 0.2F + 1.5F : (this.aa.nextFloat() - this.aa.nextFloat()) * 0.2F + 1.0F;
    }

    protected boolean bh() {
        return this.aS() <= 0.0F;
    }

    public void a(double d0, double d1, double d2) {
        this.b(d0, d1, d2, this.z, this.A);
    }

    public void l(Entity entity) {
        double d0 = entity.t;
        double d1 = entity.D.b + (double) entity.O;
        double d2 = entity.v;
        byte b0 = 3;

        for (int i0 = -b0; i0 <= b0; ++i0) {
            for (int i1 = -b0; i1 < b0; ++i1) {
                if (i0 != 0 || i1 != 0) {
                    int i2 = (int) (this.t + (double) i0);
                    int i3 = (int) (this.v + (double) i1);
                    AxisAlignedBB axisalignedbb = this.D.c((double) i0, 1.0D, (double) i1);

                    if (this.p.a(axisalignedbb).isEmpty()) {
                        if (World.a((IBlockAccess) this.p, i2, (int) this.u, i3)) {
                            this.a(this.t + (double) i0, this.u + 1.0D, this.v + (double) i1);
                            return;
                        }

                        if (World.a((IBlockAccess) this.p, i2, (int) this.u - 1, i3) || this.p.a(i2, (int) this.u - 1, i3).o() == Material.h) {
                            d0 = this.t + (double) i0;
                            d1 = this.u + 1.0D;
                            d2 = this.v + (double) i1;
                        }
                    }
                }
            }
        }

        this.a(d0, d1, d2);
    }

    protected void bj() {
        this.x = 0.41999998688697815D;
        if (this.a(Potion.j)) {
            this.x += (double) ((float) (this.b(Potion.j).c() + 1) * 0.1F);
        }

        if (this.ao()) {
            float f0 = this.z * 0.017453292F;

            this.w -= (double) (MathHelper.a(f0) * 0.2F);
            this.y += (double) (MathHelper.b(f0) * 0.2F);
        }

        this.am = true;
    }

    public void e(float f0, float f1) {
        double d0;

        if (this.M() && (!(this instanceof EntityPlayer) || !((EntityPlayer) this).bF.b)) {
            d0 = this.u;
            this.a(f0, f1, this.bk() ? 0.04F : 0.02F);
            this.d(this.w, this.x, this.y);
            this.w *= 0.800000011920929D;
            this.x *= 0.800000011920929D;
            this.y *= 0.800000011920929D;
            this.x -= 0.02D;
            if (this.F && this.c(this.w, this.x + 0.6000000238418579D - this.u + d0, this.y)) {
                this.x = 0.30000001192092896D;
            }
        }
        else if (this.P() && (!(this instanceof EntityPlayer) || !((EntityPlayer) this).bF.b)) {
            d0 = this.u;
            this.a(f0, f1, 0.02F);
            this.d(this.w, this.x, this.y);
            this.w *= 0.5D;
            this.x *= 0.5D;
            this.y *= 0.5D;
            this.x -= 0.02D;
            if (this.F && this.c(this.w, this.x + 0.6000000238418579D - this.u + d0, this.y)) {
                this.x = 0.30000001192092896D;
            }
        }
        else {
            float f2 = 0.91F;

            if (this.E) {
                f2 = this.p.a(MathHelper.c(this.t), MathHelper.c(this.D.b) - 1, MathHelper.c(this.v)).K * 0.91F;
            }

            float f3 = 0.16277136F / (f2 * f2 * f2);
            float f4;

            if (this.E) {
                f4 = this.bl() * f3;
            }
            else {
                f4 = this.aR;
            }

            this.a(f0, f1, f4);
            f2 = 0.91F;
            if (this.E) {
                f2 = this.p.a(MathHelper.c(this.t), MathHelper.c(this.D.b) - 1, MathHelper.c(this.v)).K * 0.91F;
            }

            if (this.h_()) {
                float f5 = 0.15F;

                if (this.w < (double) (-f5)) {
                    this.w = (double) (-f5);
                }

                if (this.w > (double) f5) {
                    this.w = (double) f5;
                }

                if (this.y < (double) (-f5)) {
                    this.y = (double) (-f5);
                }

                if (this.y > (double) f5) {
                    this.y = (double) f5;
                }

                this.S = 0.0F;
                if (this.x < -0.15D) {
                    this.x = -0.15D;
                }

                boolean flag0 = this.an() && this instanceof EntityPlayer;

                if (flag0 && this.x < 0.0D) {
                    this.x = 0.0D;
                }
            }

            this.d(this.w, this.x, this.y);
            if (this.F && this.h_()) {
                this.x = 0.2D;
            }

            if (this.p.E && (!this.p.d((int) this.t, 0, (int) this.v) || !this.p.d((int) this.t, (int) this.v).d)) {
                if (this.u > 0.0D) {
                    this.x = -0.1D;
                }
                else {
                    this.x = 0.0D;
                }
            }
            else {
                this.x -= 0.08D;
            }

            this.x *= 0.9800000190734863D;
            this.w *= (double) f2;
            this.y *= (double) f2;
        }

        this.aF = this.aG;
        d0 = this.t - this.q;
        double d1 = this.v - this.s;
        float f6 = MathHelper.a(d0 * d0 + d1 * d1) * 4.0F;

        if (f6 > 1.0F) {
            f6 = 1.0F;
        }

        this.aG += (f6 - this.aG) * 0.4F;
        this.aH += this.aG;
    }

    protected boolean bk() {
        return false;
    }

    public float bl() {
        return this.bk() ? this.bp : 0.1F;
    }

    public void i(float f0) {
        this.bp = f0;
    }

    public boolean m(Entity entity) {
        this.k(entity);
        return false;
    }

    public boolean bm() {
        return false;
    }

    public void h() {
        super.h();
        if (!this.p.E) {
            int i0 = this.aZ();

            if (i0 > 0) {
                if (this.aw <= 0) {
                    this.aw = 20 * (30 - i0);
                }

                --this.aw;
                if (this.aw <= 0) {
                    this.p(i0 - 1);
                }
            }

            for (int i1 = 0; i1 < 5; ++i1) {
                ItemStack itemstack = this.g[i1];
                ItemStack itemstack1 = this.q(i1);

                if (!ItemStack.b(itemstack1, itemstack)) {
                    ((WorldServer) this.p).q().a((Entity) this, (Packet) (new S04PacketEntityEquipment(this.y(), i1, itemstack1)));
                    if (itemstack != null) {
                        this.d.a(itemstack.D());
                    }

                    if (itemstack1 != null) {
                        this.d.b(itemstack1.D());
                    }

                    this.g[i1] = itemstack1 == null ? null : itemstack1.m();
                }
            }
        }

        this.e();
        double d0 = this.t - this.q;
        double d1 = this.v - this.s;
        float f0 = (float) (d0 * d0 + d1 * d1);
        float f1 = this.aN;
        float f2 = 0.0F;

        this.aW = this.aX;
        float f3 = 0.0F;

        if (f0 > 0.0025000002F) {
            f3 = 1.0F;
            f2 = (float) Math.sqrt((double) f0) * 3.0F;
            f1 = (float) Math.atan2(d1, d0) * 180.0F / 3.1415927F - 90.0F;
        }

        if (this.aE > 0.0F) {
            f1 = this.z;
        }

        if (!this.E) {
            f3 = 0.0F;
        }

        this.aX += (f3 - this.aX) * 0.3F;
        this.p.C.a("headTurn");
        f2 = this.f(f1, f2);
        this.p.C.b();
        this.p.C.a("rangeChecks");

        while (this.z - this.B < -180.0F) {
            this.B -= 360.0F;
        }

        while (this.z - this.B >= 180.0F) {
            this.B += 360.0F;
        }

        while (this.aN - this.aO < -180.0F) {
            this.aO -= 360.0F;
        }

        while (this.aN - this.aO >= 180.0F) {
            this.aO += 360.0F;
        }

        while (this.A - this.C < -180.0F) {
            this.C -= 360.0F;
        }

        while (this.A - this.C >= 180.0F) {
            this.C += 360.0F;
        }

        while (this.aP - this.aQ < -180.0F) {
            this.aQ -= 360.0F;
        }

        while (this.aP - this.aQ >= 180.0F) {
            this.aQ += 360.0F;
        }

        this.p.C.b();
        this.aY += f2;
    }

    protected float f(float f0, float f1) {
        float f2 = MathHelper.g(f0 - this.aN);

        this.aN += f2 * 0.3F;
        float f3 = MathHelper.g(this.z - this.aN);
        boolean flag0 = f3 < -90.0F || f3 >= 90.0F;

        if (f3 < -75.0F) {
            f3 = -75.0F;
        }

        if (f3 >= 75.0F) {
            f3 = 75.0F;
        }

        this.aN = this.z - f3;
        if (f3 * f3 > 2500.0F) {
            this.aN += f3 * 0.2F;
        }

        if (flag0) {
            f1 *= -1.0F;
        }

        return f1;
    }

    public void e() {
        if (this.bq > 0) {
            --this.bq;
        }

        if (this.bh > 0) {
            double d0 = this.t + (this.bi - this.t) / (double) this.bh;
            double d1 = this.u + (this.bj - this.u) / (double) this.bh;
            double d2 = this.v + (this.bk - this.v) / (double) this.bh;
            double d3 = MathHelper.g(this.bl - (double) this.z);

            this.z = (float) ((double) this.z + d3 / (double) this.bh);
            this.A = (float) ((double) this.A + (this.bm - (double) this.A) / (double) this.bh);
            --this.bh;
            this.b(d0, d1, d2);
            this.b(this.z, this.A);
        }
        else if (!this.br()) {
            this.w *= 0.98D;
            this.x *= 0.98D;
            this.y *= 0.98D;
        }

        if (Math.abs(this.w) < 0.005D) {
            this.w = 0.0D;
        }

        if (Math.abs(this.x) < 0.005D) {
            this.x = 0.0D;
        }

        if (Math.abs(this.y) < 0.005D) {
            this.y = 0.0D;
        }

        this.p.C.a("ai");
        if (this.bh()) {
            this.bd = false;
            this.be = 0.0F;
            this.bf = 0.0F;
            this.bg = 0.0F;
        }
        else if (this.br()) {
            if (this.bk()) {
                this.p.C.a("newAi");
                this.bn();
                this.p.C.b();
            }
            else {
                this.p.C.a("oldAi");
                this.bq();
                this.p.C.b();
                this.aP = this.z;
            }
            // CanaryMod: NPC Addition, this is vital for pathfinding
            // Just call the "newAi" method up there ^^
            if (this instanceof EntityNonPlayableCharacter) {
                this.bn();
            }// END
        }
        this.p.C.b();
        this.p.C.a("jump");
        if (this.bd) {
            if (!this.M() && !this.P()) {
                if (this.E && this.bq == 0) {
                    this.bj();
                    this.bq = 10;
                }
            }
            else {
                this.x += 0.03999999910593033D;
            }
        }
        else {
            this.bq = 0;
        }

        this.p.C.b();
        this.p.C.a("travel");
        this.be *= 0.98F;
        this.bf *= 0.98F;
        this.bg *= 0.9F;
        this.e(this.be, this.bf);
        this.p.C.b();
        this.p.C.a("push");
        if (!this.p.E) {
            this.bo();
        }

        this.p.C.b();
    }

    protected void bn() {
    }

    protected void bo() {
        List list = this.p.b((Entity) this, this.D.b(0.20000000298023224D, 0.0D, 0.20000000298023224D));

        if (list != null && !list.isEmpty()) {
            for (int i0 = 0; i0 < list.size(); ++i0) {
                Entity entity = (Entity) list.get(i0);

                if (entity.S()) {
                    this.n(entity);
                }
            }
        }
    }

    protected void n(Entity entity) {
        entity.f((Entity) this);
    }

    public void ab() {
        super.ab();
        this.aW = this.aX;
        this.aX = 0.0F;
        this.S = 0.0F;
    }

    protected void bp() {
    }

    protected void bq() {
        ++this.aV;
    }

    public void f(boolean flag0) {
        this.bd = flag0;
    }

    public void a(Entity entity, int i0) {
        if (!entity.L && !this.p.E) {
            EntityTracker entitytracker = ((WorldServer) this.p).q();

            if (entity instanceof EntityItem) {
                entitytracker.a(entity, (Packet) (new S0DPacketCollectItem(entity.y(), this.y())));
            }

            if (entity instanceof EntityArrow) {
                entitytracker.a(entity, (Packet) (new S0DPacketCollectItem(entity.y(), this.y())));
            }

            if (entity instanceof EntityXPOrb) {
                entitytracker.a(entity, (Packet) (new S0DPacketCollectItem(entity.y(), this.y())));
            }
        }
    }

    public boolean o(Entity entity) {
        return this.p.a(this.p.U().a(this.t, this.u + (double) this.g(), this.v), this.p.U().a(entity.t, entity.u + (double) entity.g(), entity.v)) == null;
    }

    public Vec3 ag() {
        return this.j(1.0F);
    }

    public Vec3 j(float f0) {
        float f1;
        float f2;
        float f3;
        float f4;

        if (f0 == 1.0F) {
            f1 = MathHelper.b(-this.z * 0.017453292F - 3.1415927F);
            f2 = MathHelper.a(-this.z * 0.017453292F - 3.1415927F);
            f3 = -MathHelper.b(-this.A * 0.017453292F);
            f4 = MathHelper.a(-this.A * 0.017453292F);
            return this.p.U().a((double) (f2 * f3), (double) f4, (double) (f1 * f3));
        }
        else {
            f1 = this.C + (this.A - this.C) * f0;
            f2 = this.B + (this.z - this.B) * f0;
            f3 = MathHelper.b(-f2 * 0.017453292F - 3.1415927F);
            f4 = MathHelper.a(-f2 * 0.017453292F - 3.1415927F);
            float f5 = -MathHelper.b(-f1 * 0.017453292F);
            float f6 = MathHelper.a(-f1 * 0.017453292F);

            return this.p.U().a((double) (f4 * f5), (double) f6, (double) (f3 * f5));
        }
    }

    public boolean br() {
        return !this.p.E;
    }

    public boolean R() {
        return !this.L;
    }

    public boolean S() {
        return !this.L;
    }

    public float g() {
        return this.O * 0.85F;
    }

    protected void Q() {
        this.I = this.aa.nextDouble() >= this.a(SharedMonsterAttributes.c).e();
    }

    public float au() {
        return this.aP;
    }

    public float bs() {
        return this.br;
    }

    public void m(float f0) {
        if (f0 < 0.0F) {
            f0 = 0.0F;
        }

        this.br = f0;
    }

    public Team bt() {
        return null;
    }

    public boolean c(EntityLivingBase entitylivingbase) {
        return this.a(entitylivingbase.bt());
    }

    public boolean a(Team team) {
        return this.bt() != null ? this.bt().a(team) : false;
    }

    // CanaryMod
    public void setAge(int age) {
        this.aV = age;
    }

    //CanaryMod
    public void removeAllPotionEffects() {
        Iterator iterator = this.f.values().iterator();
        while (iterator.hasNext()) {
            PotionEffect potioneffect = (PotionEffect) iterator.next();
            this.b(potioneffect);
        }
    }
}

