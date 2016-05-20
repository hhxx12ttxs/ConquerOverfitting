package net.minecraft.entity.monster;

import net.canarymod.api.entity.living.monster.CanaryBlaze;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityBlaze extends EntityMob {

    private float bp = 0.5F;
    private int bq;
    private int br;

    public EntityBlaze(World world) {
        super(world);
        this.af = true;
        this.b = 10;
        this.entity = new CanaryBlaze(this); // CanaryMod: Wrap Entity
    }

    protected void aD() {
        super.aD();
        this.a(SharedMonsterAttributes.e).a(6.0D);
    }

    protected void c() {
        super.c();
        this.ag.a(16, new Byte((byte) 0));
    }

    protected String t() {
        return "mob.blaze.breathe";
    }

    protected String aT() {
        return "mob.blaze.hit";
    }

    protected String aU() {
        return "mob.blaze.death";
    }

    public float d(float f0) {
        return 1.0F;
    }

    public void e() {
        if (!this.p.E) {
            if (this.L()) {
                this.a(DamageSource.e, 1.0F);
            }

            --this.bq;
            if (this.bq <= 0) {
                this.bq = 100;
                this.bp = 0.5F + (float) this.aa.nextGaussian() * 3.0F;
            }

            if (this.bR() != null && this.bR().u + (double) this.bR().g() > this.u + (double) this.g() + (double) this.bp) {
                this.x += (0.30000001192092896D - this.x) * 0.30000001192092896D;
            }
        }

        if (this.aa.nextInt(24) == 0) {
            this.p.a(this.t + 0.5D, this.u + 0.5D, this.v + 0.5D, "fire.fire", 1.0F + this.aa.nextFloat(), this.aa.nextFloat() * 0.7F + 0.3F);
        }

        if (!this.E && this.x < 0.0D) {
            this.x *= 0.6D;
        }

        for (int i0 = 0; i0 < 2; ++i0) {
            this.p.a("largesmoke", this.t + (this.aa.nextDouble() - 0.5D) * (double) this.N, this.u + this.aa.nextDouble() * (double) this.O, this.v + (this.aa.nextDouble() - 0.5D) * (double) this.N, 0.0D, 0.0D, 0.0D);
        }

        super.e();
    }

    protected void a(Entity entity, float f0) {
        if (this.aC <= 0 && f0 < 2.0F && entity.D.e > this.D.b && entity.D.b < this.D.e) {
            this.aC = 20;
            this.m(entity);
        }
        else if (f0 < 30.0F) {
            double d0 = entity.t - this.t;
            double d1 = entity.D.b + (double) (entity.O / 2.0F) - (this.u + (double) (this.O / 2.0F));
            double d2 = entity.v - this.v;

            if (this.aC == 0) {
                ++this.br;
                if (this.br == 1) {
                    this.aC = 60;
                    this.a(true);
                }
                else if (this.br <= 4) {
                    this.aC = 6;
                }
                else {
                    this.aC = 100;
                    this.br = 0;
                    this.a(false);
                }

                if (this.br > 1) {
                    float f1 = MathHelper.c(f0) * 0.5F;

                    this.p.a((EntityPlayer) null, 1009, (int) this.t, (int) this.u, (int) this.v, 0);

                    for (int i0 = 0; i0 < 1; ++i0) {
                        EntitySmallFireball entitysmallfireball = new EntitySmallFireball(this.p, this, d0 + this.aa.nextGaussian() * (double) f1, d1, d2 + this.aa.nextGaussian() * (double) f1);

                        entitysmallfireball.u = this.u + (double) (this.O / 2.0F) + 0.5D;
                        this.p.d((Entity) entitysmallfireball);
                    }
                }
            }

            this.z = (float) (Math.atan2(d2, d0) * 180.0D / 3.1415927410125732D) - 90.0F;
            this.bn = true;
        }
    }

    protected void b(float f0) {
    }

    protected Item u() {
        return Items.bj;
    }

    public boolean al() {
        return this.bX();
    }

    protected void b(boolean flag0, int i0) {
        if (flag0) {
            int i1 = this.aa.nextInt(2 + i0);

            for (int i2 = 0; i2 < i1; ++i2) {
                this.a(Items.bj, 1);
            }
        }
    }

    public boolean bX() {
        return (this.ag.a(16) & 1) != 0;
    }

    public void a(boolean flag0) {
        byte b0 = this.ag.a(16);

        if (flag0) {
            b0 = (byte) (b0 | 1);
        }
        else {
            b0 &= -2;
        }

        this.ag.b(16, Byte.valueOf(b0));
    }

    protected boolean j_() {
        return true;
    }
}

