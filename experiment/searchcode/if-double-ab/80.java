package net.minecraft.entity.boss;

import net.canarymod.api.entity.living.monster.CanaryWither;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;

public class EntityWither extends EntityMob implements IRangedAttackMob {

    private float[] bp = new float[2];
    private float[] bq = new float[2];
    private float[] br = new float[2];
    private float[] bs = new float[2];
    private int[] bt = new int[2];
    private int[] bu = new int[2];
    private int bv;
    private static final IEntitySelector bw = new IEntitySelector() {

        public boolean a(Entity var1) {
            return var1 instanceof EntityLivingBase && ((EntityLivingBase) var1).bd() != EnumCreatureAttribute.UNDEAD;
        }
    };

    public EntityWither(World world) {
        super(world);
        this.g(this.aY());
        this.a(0.9F, 4.0F);
        this.af = true;
        this.m().e(true);
        this.c.a(0, new EntityAISwimming(this));
        this.c.a(2, new EntityAIArrowAttack(this, 1.0D, 40, 20.0F));
        this.c.a(5, new EntityAIWander(this, 1.0D));
        this.c.a(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.c.a(7, new EntityAILookIdle(this));
        this.d.a(1, new EntityAIHurtByTarget(this, false));
        this.d.a(2, new EntityAINearestAttackableTarget(this, EntityLiving.class, 0, false, false, bw));
        this.b = 50;
        this.entity = new CanaryWither(this); // CanaryMod: Wrap Entity
    }

    protected void c() {
        super.c();
        this.ag.a(17, new Integer(0));
        this.ag.a(18, new Integer(0));
        this.ag.a(19, new Integer(0));
        this.ag.a(20, new Integer(0));
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.a("Invul", this.bY());
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.s(nbttagcompound.f("Invul"));
    }

    protected String t() {
        return "mob.wither.idle";
    }

    protected String aT() {
        return "mob.wither.hurt";
    }

    protected String aU() {
        return "mob.wither.death";
    }

    public void e() {
        this.x *= 0.6000000238418579D;
        double d0;
        double d1;
        double d2;

        if (!this.p.E && this.t(0) > 0) {
            Entity entity = this.p.a(this.t(0));

            if (entity != null) {
                if (this.u < entity.u || !this.bZ() && this.u < entity.u + 5.0D) {
                    if (this.x < 0.0D) {
                        this.x = 0.0D;
                    }

                    this.x += (0.5D - this.x) * 0.6000000238418579D;
                }

                double d3 = entity.t - this.t;

                d0 = entity.v - this.v;
                d1 = d3 * d3 + d0 * d0;
                if (d1 > 9.0D) {
                    d2 = (double) MathHelper.a(d1);
                    this.w += (d3 / d2 * 0.5D - this.w) * 0.6000000238418579D;
                    this.y += (d0 / d2 * 0.5D - this.y) * 0.6000000238418579D;
                }
            }
        }

        if (this.w * this.w + this.y * this.y > 0.05000000074505806D) {
            this.z = (float) Math.atan2(this.y, this.w) * 57.295776F - 90.0F;
        }

        super.e();

        int i0;

        for (i0 = 0; i0 < 2; ++i0) {
            this.bs[i0] = this.bq[i0];
            this.br[i0] = this.bp[i0];
        }

        int i1;

        for (i0 = 0; i0 < 2; ++i0) {
            i1 = this.t(i0 + 1);
            Entity entity1 = null;

            if (i1 > 0) {
                entity1 = this.p.a(i1);
            }

            if (entity1 != null) {
                d0 = this.u(i0 + 1);
                d1 = this.v(i0 + 1);
                d2 = this.w(i0 + 1);
                double d4 = entity1.t - d0;
                double d5 = entity1.u + (double) entity1.g() - d1;
                double d6 = entity1.v - d2;
                double d7 = (double) MathHelper.a(d4 * d4 + d6 * d6);
                float f0 = (float) (Math.atan2(d6, d4) * 180.0D / 3.1415927410125732D) - 90.0F;
                float f1 = (float) (-(Math.atan2(d5, d7) * 180.0D / 3.1415927410125732D));

                this.bp[i0] = this.b(this.bp[i0], f1, 40.0F);
                this.bq[i0] = this.b(this.bq[i0], f0, 10.0F);
            }
            else {
                this.bq[i0] = this.b(this.bq[i0], this.aN, 10.0F);
            }
        }

        boolean flag0 = this.bZ();

        for (i1 = 0; i1 < 3; ++i1) {
            double d8 = this.u(i1);
            double d9 = this.v(i1);
            double d10 = this.w(i1);

            this.p.a("smoke", d8 + this.aa.nextGaussian() * 0.30000001192092896D, d9 + this.aa.nextGaussian() * 0.30000001192092896D, d10 + this.aa.nextGaussian() * 0.30000001192092896D, 0.0D, 0.0D, 0.0D);
            if (flag0 && this.p.s.nextInt(4) == 0) {
                this.p.a("mobSpell", d8 + this.aa.nextGaussian() * 0.30000001192092896D, d9 + this.aa.nextGaussian() * 0.30000001192092896D, d10 + this.aa.nextGaussian() * 0.30000001192092896D, 0.699999988079071D, 0.699999988079071D, 0.5D);
            }
        }

        if (this.bY() > 0) {
            for (i1 = 0; i1 < 3; ++i1) {
                this.p.a("mobSpell", this.t + this.aa.nextGaussian() * 1.0D, this.u + (double) (this.aa.nextFloat() * 3.3F), this.v + this.aa.nextGaussian() * 1.0D, 0.699999988079071D, 0.699999988079071D, 0.8999999761581421D);
            }
        }
    }

    protected void bn() {
        int i0;

        if (this.bY() > 0) {
            i0 = this.bY() - 1;
            if (i0 <= 0) {
                this.p.a(this, this.t, this.u + (double) this.g(), this.v, 7.0F, false, this.p.N().b("mobGriefing"));
                this.p.b(1013, (int) this.t, (int) this.u, (int) this.v, 0);
            }

            this.s(i0);
            if (this.ab % 10 == 0) {
                this.f(10.0F);
            }
        }
        else {
            super.bn();

            int i1;

            for (i0 = 1; i0 < 3; ++i0) {
                if (this.ab >= this.bt[i0 - 1]) {
                    this.bt[i0 - 1] = this.ab + 10 + this.aa.nextInt(10);
                    if (this.p.r == EnumDifficulty.NORMAL || this.p.r == EnumDifficulty.HARD) {
                        int i11001 = i0 - 1;
                        int i11003 = this.bu[i0 - 1];

                        this.bu[i11001] = this.bu[i0 - 1] + 1;
                        if (i11003 > 15) {
                            float f0 = 10.0F;
                            float f1 = 5.0F;
                            double d0 = MathHelper.a(this.aa, this.t - (double) f0, this.t + (double) f0);
                            double d1 = MathHelper.a(this.aa, this.u - (double) f1, this.u + (double) f1);
                            double d2 = MathHelper.a(this.aa, this.v - (double) f0, this.v + (double) f0);

                            this.a(i0 + 1, d0, d1, d2, true);
                            this.bu[i0 - 1] = 0;
                        }
                    }

                    i1 = this.t(i0);
                    if (i1 > 0) {
                        Entity entity = this.p.a(i1);

                        if (entity != null && entity.Z() && this.e(entity) <= 900.0D && this.o(entity)) {
                            this.a(i0 + 1, (EntityLivingBase) entity);
                            this.bt[i0 - 1] = this.ab + 40 + this.aa.nextInt(20);
                            this.bu[i0 - 1] = 0;
                        }
                        else {
                            this.b(i0, 0);
                        }
                    }
                    else {
                        List list = this.p.a(EntityLivingBase.class, this.D.b(20.0D, 8.0D, 20.0D), bw);

                        for (int i4 = 0; i4 < 10 && !list.isEmpty(); ++i4) {
                            EntityLivingBase entitylivingbase = (EntityLivingBase) list.get(this.aa.nextInt(list.size()));

                            if (entitylivingbase != this && entitylivingbase.Z() && this.o(entitylivingbase)) {
                                if (entitylivingbase instanceof EntityPlayer) {
                                    if (!((EntityPlayer) entitylivingbase).bF.a) {
                                        this.b(i0, entitylivingbase.y());
                                    }
                                }
                                else {
                                    this.b(i0, entitylivingbase.y());
                                }
                                break;
                            }

                            list.remove(entitylivingbase);
                        }
                    }
                }
            }

            if (this.o() != null) {
                this.b(0, this.o().y());
            }
            else {
                this.b(0, 0);
            }

            if (this.bv > 0) {
                --this.bv;
                if (this.bv == 0 && this.p.N().b("mobGriefing")) {
                    i0 = MathHelper.c(this.u);
                    i1 = MathHelper.c(this.t);
                    int i5 = MathHelper.c(this.v);
                    boolean flag0 = false;

                    for (int i6 = -1; i6 <= 1; ++i6) {
                        for (int i7 = -1; i7 <= 1; ++i7) {
                            for (int i8 = 0; i8 <= 3; ++i8) {
                                int i9 = i1 + i6;
                                int i10 = i0 + i8;
                                int i11 = i5 + i7;
                                Block block = this.p.a(i9, i10, i11);

                                if (block.o() != Material.a && block != Blocks.h && block != Blocks.bq && block != Blocks.br && block != Blocks.bI) {
                                    flag0 = this.p.a(i9, i10, i11, true) || flag0;
                                }
                            }
                        }
                    }

                    if (flag0) {
                        this.p.a((EntityPlayer) null, 1012, (int) this.t, (int) this.u, (int) this.v, 0);
                    }
                }
            }

            if (this.ab % 20 == 0) {
                this.f(1.0F);
            }
        }
    }

    public void bX() {
        this.s(220);
        this.g(this.aY() / 3.0F);
    }

    public void as() {
    }

    public int aV() {
        return 4;
    }

    private double u(int i0) {
        if (i0 <= 0) {
            return this.t;
        }
        else {
            float f0 = (this.aN + (float) (180 * (i0 - 1))) / 180.0F * 3.1415927F;
            float f1 = MathHelper.b(f0);

            return this.t + (double) f1 * 1.3D;
        }
    }

    private double v(int i0) {
        return i0 <= 0 ? this.u + 3.0D : this.u + 2.2D;
    }

    private double w(int i0) {
        if (i0 <= 0) {
            return this.v;
        }
        else {
            float f0 = (this.aN + (float) (180 * (i0 - 1))) / 180.0F * 3.1415927F;
            float f1 = MathHelper.a(f0);

            return this.v + (double) f1 * 1.3D;
        }
    }

    private float b(float f0, float f1, float f2) {
        float f3 = MathHelper.g(f1 - f0);

        if (f3 > f2) {
            f3 = f2;
        }

        if (f3 < -f2) {
            f3 = -f2;
        }

        return f0 + f3;
    }

    private void a(int i0, EntityLivingBase entitylivingbase) {
        this.a(i0, entitylivingbase.t, entitylivingbase.u + (double) entitylivingbase.g() * 0.5D, entitylivingbase.v, i0 == 0 && this.aa.nextFloat() < 0.001F);
    }

    private void a(int i0, double d0, double d1, double d2, boolean flag0) {
        this.p.a((EntityPlayer) null, 1014, (int) this.t, (int) this.u, (int) this.v, 0);
        double d3 = this.u(i0);
        double d4 = this.v(i0);
        double d5 = this.w(i0);
        double d6 = d0 - d3;
        double d7 = d1 - d4;
        double d8 = d2 - d5;
        EntityWitherSkull entitywitherskull = new EntityWitherSkull(this.p, this, d6, d7, d8);

        if (flag0) {
            entitywitherskull.a(true);
        }

        entitywitherskull.u = d4;
        entitywitherskull.t = d3;
        entitywitherskull.v = d5;
        this.p.d((Entity) entitywitherskull);
    }

    public void a(EntityLivingBase entitylivingbase, float f0) {
        this.a(0, entitylivingbase);
    }

    public boolean a(DamageSource damagesource, float f0) {
        if (this.aw()) {
            return false;
        }
        else if (damagesource == DamageSource.e) {
            return false;
        }
        else if (this.bY() > 0) {
            return false;
        }
        else {
            Entity entity;

            if (this.bZ()) {
                entity = damagesource.i();
                if (entity instanceof EntityArrow) {
                    return false;
                }
            }

            entity = damagesource.j();
            if (entity != null && !(entity instanceof EntityPlayer) && entity instanceof EntityLivingBase && ((EntityLivingBase) entity).bd() == this.bd()) {
                return false;
            }
            else {
                if (this.bv <= 0) {
                    this.bv = 20;
                }

                for (int i0 = 0; i0 < this.bu.length; ++i0) {
                    this.bu[i0] += 3;
                }

                return super.a(damagesource, f0);
            }
        }
    }

    protected void b(boolean flag0, int i0) {
        this.a(Items.bN, 1);
        if (!this.p.E) {
            Iterator iterator = this.p.a(EntityPlayer.class, this.D.b(50.0D, 100.0D, 50.0D)).iterator();

            while (iterator.hasNext()) {
                EntityPlayer entityplayer = (EntityPlayer) iterator.next();

                entityplayer.a((StatBase) AchievementList.J);
            }
        }
    }

    protected void w() {
        this.aV = 0;
    }

    protected void b(float f0) {
    }

    public void c(PotionEffect potioneffect) {
    }

    protected boolean bk() {
        return true;
    }

    protected void aD() {
        super.aD();
        this.a(SharedMonsterAttributes.a).a(300.0D);
        this.a(SharedMonsterAttributes.d).a(0.6000000238418579D);
        this.a(SharedMonsterAttributes.b).a(40.0D);
    }

    public int bY() {
        return this.ag.c(20);
    }

    public void s(int i0) {
        this.ag.b(20, Integer.valueOf(i0));
    }

    public int t(int i0) {
        return this.ag.c(17 + i0);
    }

    public void b(int i0, int i1) {
        this.ag.b(17 + i0, Integer.valueOf(i1));
    }

    public boolean bZ() {
        return this.aS() <= this.aY() / 2.0F;
    }

    public EnumCreatureAttribute bd() {
        return EnumCreatureAttribute.UNDEAD;
    }

    public void a(Entity entity) {
        this.n = null;
    }
}

