package net.minecraft.entity;

import net.canarymod.hook.entity.MobTargetHook;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.UUID;

public abstract class EntityCreature extends EntityLiving {

    public static final UUID h = UUID.fromString("E199AD21-BA8A-4C53-8D13-6182D5C69D3A");
    public static final AttributeModifier i = (new AttributeModifier(h, "Fleeing speed bonus", 2.0D, 2)).a(false);
    private PathEntity bp;
    protected Entity j;
    protected boolean bn;
    protected int bo;
    private ChunkCoordinates bq = new ChunkCoordinates(0, 0, 0);
    private float br = -1.0F;
    private EntityAIBase bs = new EntityAIMoveTowardsRestriction(this, 1.0D);
    private boolean bt;

    public EntityCreature(World world) {
        super(world);
    }

    protected boolean bN() {
        return false;
    }

    protected void bq() {
        this.p.C.a("ai");
        if (this.bo > 0 && --this.bo == 0) {
            AttributeInstance attributeinstance = this.a(SharedMonsterAttributes.d);

            attributeinstance.b(i);
        }

        this.bn = this.bN();
        float f11 = 16.0F;

        if (this.j == null) {
            // CanaryMod: MobTarget
            Entity entity = this.bP();
            if (entity != null && entity instanceof EntityLivingBase) {
                MobTargetHook hook = (MobTargetHook) new MobTargetHook((net.canarymod.api.entity.living.LivingBase) this.getCanaryEntity(), (net.canarymod.api.entity.living.LivingBase) entity.getCanaryEntity()).call();
                if (!hook.isCanceled()) {
                    this.j = entity;
                }
            }
            else {
                this.j = entity;
            }
            //

            if (this.j != null) {
                this.bp = this.p.a(this, this.j, f11, true, false, false, true);
            }
        }
        else if (this.j.Z()) {
            float f1 = this.j.d((Entity) this);

            if (this.o(this.j)) {
                this.a(this.j, f1);
            }
        }
        else {
            this.j = null;
        }

        if (this.j instanceof EntityPlayerMP && ((EntityPlayerMP) this.j).c.d()) {
            this.j = null;
        }

        this.p.C.b();
        if (!this.bn && this.j != null && (this.bp == null || this.aa.nextInt(20) == 0)) {
            this.bp = this.p.a(this, this.j, f11, true, false, false, true);
        }
        else if (!this.bn && (this.bp == null && this.aa.nextInt(180) == 0 || this.aa.nextInt(120) == 0 || this.bo > 0) && this.aV < 100) {
            this.bO();
        }

        int i0 = MathHelper.c(this.D.b + 0.5D);
        boolean flag0 = this.M();
        boolean flag1 = this.P();

        this.A = 0.0F;
        if (this.bp != null && this.aa.nextInt(100) != 0) {
            this.p.C.a("followpath");
            Vec3 vec3 = this.bp.a((Entity) this);
            double d0 = (double) (this.N * 2.0F);

            while (vec3 != null && vec3.d(this.t, vec3.d, this.v) < d0 * d0) {
                this.bp.a();
                if (this.bp.b()) {
                    vec3 = null;
                    this.bp = null;
                }
                else {
                    vec3 = this.bp.a((Entity) this);
                }
            }

            this.bd = false;
            if (vec3 != null) {
                double d1 = vec3.c - this.t;
                double d2 = vec3.e - this.v;
                double d3 = vec3.d - (double) i0;
                float f2 = (float) (Math.atan2(d2, d1) * 180.0D / 3.1415927410125732D) - 90.0F;
                float f3 = MathHelper.g(f2 - this.z);

                this.bf = (float) this.a(SharedMonsterAttributes.d).e();
                if (f3 > 30.0F) {
                    f3 = 30.0F;
                }

                if (f3 < -30.0F) {
                    f3 = -30.0F;
                }

                this.z += f3;
                if (this.bn && this.j != null) {
                    double d4 = this.j.t - this.t;
                    double d5 = this.j.v - this.v;
                    float f4 = this.z;

                    this.z = (float) (Math.atan2(d5, d4) * 180.0D / 3.1415927410125732D) - 90.0F;
                    f3 = (f4 - this.z + 90.0F) * 3.1415927F / 180.0F;
                    this.be = -MathHelper.a(f3) * this.bf * 1.0F;
                    this.bf = MathHelper.b(f3) * this.bf * 1.0F;
                }

                if (d3 > 0.0D) {
                    this.bd = true;
                }
            }

            if (this.j != null) {
                this.a(this.j, 30.0F, 30.0F);
            }

            if (this.F && !this.bQ()) {
                this.bd = true;
            }

            if (this.aa.nextFloat() < 0.8F && (flag0 || flag1)) {
                this.bd = true;
            }

            this.p.C.b();
        }
        else {
            super.bq();
            this.bp = null;
        }
    }

    protected void bO() {
        this.p.C.a("stroll");
        boolean flag0 = false;
        int i0 = -1;
        int i1 = -1;
        int i2 = -1;
        float f0 = -99999.0F;

        for (int i3 = 0; i3 < 10; ++i3) {
            int i4 = MathHelper.c(this.t + (double) this.aa.nextInt(13) - 6.0D);
            int i5 = MathHelper.c(this.u + (double) this.aa.nextInt(7) - 3.0D);
            int i6 = MathHelper.c(this.v + (double) this.aa.nextInt(13) - 6.0D);
            float f1 = this.a(i4, i5, i6);

            if (f1 > f0) {
                f0 = f1;
                i0 = i4;
                i1 = i5;
                i2 = i6;
                flag0 = true;
            }
        }

        if (flag0) {
            this.bp = this.p.a(this, i0, i1, i2, 10.0F, true, false, false, true);
        }

        this.p.C.b();
    }

    protected void a(Entity entity, float f0) {
    }

    public float a(int i0, int i1, int i2) {
        return 0.0F;
    }

    protected Entity bP() {
        return null;
    }

    public boolean bw() {
        int i0 = MathHelper.c(this.t);
        int i1 = MathHelper.c(this.D.b);
        int i2 = MathHelper.c(this.v);

        return super.bw() && this.a(i0, i1, i2) >= 0.0F;
    }

    public boolean bQ() {
        return this.bp != null;
    }

    public void a(PathEntity pathentity) {
        this.bp = pathentity;
    }

    public Entity bR() {
        return this.j;
    }

    public void b(Entity entity) {
        this.j = entity;
    }

    public boolean bS() {
        return this.b(MathHelper.c(this.t), MathHelper.c(this.u), MathHelper.c(this.v));
    }

    public boolean b(int i0, int i1, int i2) {
        return this.br == -1.0F ? true : this.bq.e(i0, i1, i2) < this.br * this.br;
    }

    public void a(int i0, int i1, int i2, int i3) {
        this.bq.b(i0, i1, i2);
        this.br = (float) i3;
    }

    public ChunkCoordinates bT() {
        return this.bq;
    }

    public float bU() {
        return this.br;
    }

    public void bV() {
        this.br = -1.0F;
    }

    public boolean bW() {
        return this.br != -1.0F;
    }

    protected void bJ() {
        super.bJ();
        if (this.bL() && this.bM() != null && this.bM().p == this.p) {
            Entity entity = this.bM();

            this.a((int) entity.t, (int) entity.u, (int) entity.v, 5);
            float f0 = this.d(entity);

            if (this instanceof EntityTameable && ((EntityTameable) this).bY()) {
                if (f0 > 10.0F) {
                    this.a(true, true);
                }

                return;
            }

            if (!this.bt) {
                this.c.a(2, this.bs);
                this.m().a(false);
                this.bt = true;
            }

            this.o(f0);
            if (f0 > 4.0F) {
                this.m().a(entity, 1.0D);
            }

            if (f0 > 6.0F) {
                double d0 = (entity.t - this.t) / (double) f0;
                double d1 = (entity.u - this.u) / (double) f0;
                double d2 = (entity.v - this.v) / (double) f0;

                this.w += d0 * Math.abs(d0) * 0.4D;
                this.x += d1 * Math.abs(d1) * 0.4D;
                this.y += d2 * Math.abs(d2) * 0.4D;
            }

            if (f0 > 10.0F) {
                this.a(true, true);
            }
        }
        else if (!this.bL() && this.bt) {
            this.bt = false;
            this.c.a(this.bs);
            this.m().a(true);
            this.bV();
        }
    }

    protected void o(float f0) {
    }
}

