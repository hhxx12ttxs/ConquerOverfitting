package net.minecraft.entity.projectile;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

public abstract class EntityThrowable extends Entity implements IProjectile {

    private int c = -1;
    private int d = -1;
    private int e = -1;
    private Block f;
    protected boolean a;
    public int b;
    private EntityLivingBase g;
    private String h;
    private int i;
    private int j;
    public float gravity = 0.03F; // CanaryMod

    public EntityThrowable(World world) {
        super(world);
        this.a(0.25F, 0.25F);
    }

    protected void c() {
    }

    public EntityThrowable(World world, EntityLivingBase entitylivingbase) {
        super(world);
        this.g = entitylivingbase;
        this.a(0.25F, 0.25F);
        this.b(entitylivingbase.t, entitylivingbase.u + (double) entitylivingbase.g(), entitylivingbase.v, entitylivingbase.z, entitylivingbase.A);
        this.t -= (double) (MathHelper.b(this.z / 180.0F * 3.1415927F) * 0.16F);
        this.u -= 0.10000000149011612D;
        this.v -= (double) (MathHelper.a(this.z / 180.0F * 3.1415927F) * 0.16F);
        this.b(this.t, this.u, this.v);
        this.M = 0.0F;
        float f0 = 0.4F;

        this.w = (double) (-MathHelper.a(this.z / 180.0F * 3.1415927F) * MathHelper.b(this.A / 180.0F * 3.1415927F) * f0);
        this.y = (double) (MathHelper.b(this.z / 180.0F * 3.1415927F) * MathHelper.b(this.A / 180.0F * 3.1415927F) * f0);
        this.x = (double) (-MathHelper.a((this.A + this.f()) / 180.0F * 3.1415927F) * f0);
        this.c(this.w, this.x, this.y, this.e(), 1.0F);
    }

    public EntityThrowable(World world, double d0, double d1, double d2) {
        super(world);
        this.i = 0;
        this.a(0.25F, 0.25F);
        this.b(d0, d1, d2);
        this.M = 0.0F;
    }

    protected float e() {
        return 1.5F;
    }

    protected float f() {
        return 0.0F;
    }

    public void c(double d0, double d1, double d2, float f0, float f1) {
        float f2 = MathHelper.a(d0 * d0 + d1 * d1 + d2 * d2);

        d0 /= (double) f2;
        d1 /= (double) f2;
        d2 /= (double) f2;
        d0 += this.aa.nextGaussian() * 0.007499999832361937D * (double) f1;
        d1 += this.aa.nextGaussian() * 0.007499999832361937D * (double) f1;
        d2 += this.aa.nextGaussian() * 0.007499999832361937D * (double) f1;
        d0 *= (double) f0;
        d1 *= (double) f0;
        d2 *= (double) f0;
        this.w = d0;
        this.x = d1;
        this.y = d2;
        float f3 = MathHelper.a(d0 * d0 + d2 * d2);

        this.B = this.z = (float) (Math.atan2(d0, d2) * 180.0D / 3.1415927410125732D);
        this.C = this.A = (float) (Math.atan2(d1, (double) f3) * 180.0D / 3.1415927410125732D);
        this.i = 0;
    }

    public void h() {
        this.T = this.t;
        this.U = this.u;
        this.V = this.v;
        super.h();
        if (this.b > 0) {
            --this.b;
        }

        if (this.a) {
            if (this.p.a(this.c, this.d, this.e) == this.f) {
                ++this.i;
                if (this.i == 1200) {
                    this.B();
                }

                return;
            }

            this.a = false;
            this.w *= (double) (this.aa.nextFloat() * 0.2F);
            this.x *= (double) (this.aa.nextFloat() * 0.2F);
            this.y *= (double) (this.aa.nextFloat() * 0.2F);
            this.i = 0;
            this.j = 0;
        }
        else {
            ++this.j;
        }

        Vec3 vec3 = this.p.U().a(this.t, this.u, this.v);
        Vec3 vec31 = this.p.U().a(this.t + this.w, this.u + this.x, this.v + this.y);
        MovingObjectPosition movingobjectposition = this.p.a(vec3, vec31);

        vec3 = this.p.U().a(this.t, this.u, this.v);
        vec31 = this.p.U().a(this.t + this.w, this.u + this.x, this.v + this.y);
        if (movingobjectposition != null) {
            vec31 = this.p.U().a(movingobjectposition.f.c, movingobjectposition.f.d, movingobjectposition.f.e);
        }

        if (!this.p.E) {
            Entity entity = null;
            List list = this.p.b((Entity) this, this.D.a(this.w, this.x, this.y).b(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;
            EntityLivingBase entitylivingbase = this.j();

            for (int i0 = 0; i0 < list.size(); ++i0) {
                Entity entity1 = (Entity) list.get(i0);

                if (entity1.R() && (entity1 != entitylivingbase || this.j >= 5)) {
                    float f0 = 0.3F;
                    AxisAlignedBB axisalignedbb = entity1.D.b((double) f0, (double) f0, (double) f0);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb.a(vec3, vec31);

                    if (movingobjectposition1 != null) {
                        double d1 = vec3.d(movingobjectposition1.f);

                        if (d1 < d0 || d0 == 0.0D) {
                            entity = entity1;
                            d0 = d1;
                        }
                    }
                }
            }

            if (entity != null) {
                movingobjectposition = new MovingObjectPosition(entity);
            }
        }

        if (movingobjectposition != null) {
            if (movingobjectposition.a == MovingObjectPosition.MovingObjectType.BLOCK && this.p.a(movingobjectposition.b, movingobjectposition.c, movingobjectposition.d) == Blocks.aO) {
                this.ah();
            }
            else {
                this.a(movingobjectposition);
            }
        }

        this.t += this.w;
        this.u += this.x;
        this.v += this.y;
        float f1 = MathHelper.a(this.w * this.w + this.y * this.y);

        this.z = (float) (Math.atan2(this.w, this.y) * 180.0D / 3.1415927410125732D);

        for (this.A = (float) (Math.atan2(this.x, (double) f1) * 180.0D / 3.1415927410125732D); this.A - this.C < -180.0F; this.C -= 360.0F) {
            ;
        }

        while (this.A - this.C >= 180.0F) {
            this.C += 360.0F;
        }

        while (this.z - this.B < -180.0F) {
            this.B -= 360.0F;
        }

        while (this.z - this.B >= 180.0F) {
            this.B += 360.0F;
        }

        this.A = this.C + (this.A - this.C) * 0.2F;
        this.z = this.B + (this.z - this.B) * 0.2F;
        float f2 = 0.99F;
        float f3 = this.i();

        if (this.M()) {
            for (int i1 = 0; i1 < 4; ++i1) {
                float f4 = 0.25F;

                this.p.a("bubble", this.t - this.w * (double) f4, this.u - this.x * (double) f4, this.v - this.y * (double) f4, this.w, this.x, this.y);
            }

            f2 = 0.8F;
        }

        this.w *= (double) f2;
        this.x *= (double) f2;
        this.y *= (double) f2;
        this.x -= (double) f3;
        this.b(this.t, this.u, this.v);
    }

    protected float i() {
        return gravity; // CanaryMod: return gravity
    }

    protected abstract void a(MovingObjectPosition movingobjectposition);

    public void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.a("xTile", (short) this.c);
        nbttagcompound.a("yTile", (short) this.d);
        nbttagcompound.a("zTile", (short) this.e);
        nbttagcompound.a("inTile", (byte) Block.b(this.f));
        nbttagcompound.a("shake", (byte) this.b);
        nbttagcompound.a("inGround", (byte) (this.a ? 1 : 0));
        if ((this.h == null || this.h.length() == 0) && this.g != null && this.g instanceof EntityPlayer) {
            this.h = this.g.b_();
        }

        nbttagcompound.a("ownerName", this.h == null ? "" : this.h);
    }

    public void a(NBTTagCompound nbttagcompound) {
        this.c = nbttagcompound.e("xTile");
        this.d = nbttagcompound.e("yTile");
        this.e = nbttagcompound.e("zTile");
        this.f = Block.e(nbttagcompound.d("inTile") & 255);
        this.b = nbttagcompound.d("shake") & 255;
        this.a = nbttagcompound.d("inGround") == 1;
        this.h = nbttagcompound.j("ownerName");
        if (this.h != null && this.h.length() == 0) {
            this.h = null;
        }
    }

    public EntityLivingBase j() {
        if (this.g == null && this.h != null && this.h.length() > 0) {
            this.g = this.p.a(this.h);
        }

        return this.g;
    }
}

