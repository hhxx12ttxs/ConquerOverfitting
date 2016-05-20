package net.minecraft.entity.projectile;

import net.canarymod.api.entity.CanaryArrow;
import net.canarymod.hook.entity.ProjectileHitHook;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class EntityArrow extends Entity implements IProjectile {

    private int d = -1;
    private int e = -1;
    private int f = -1;
    private Block g;
    private int h;
    public boolean i; // CanaryMod: private => public; inGround
    public int a;
    public int b;
    public Entity c;
    public int j; // CanaryMod: private => public; ticksInGround
    public int au; // CanaryMod: private => public; ticksInAir
    private double av = 2.0D;
    private int aw;

    public EntityArrow(World world) {
        super(world);
        this.k = 10.0D;
        this.a(0.5F, 0.5F);
        this.entity = new CanaryArrow(this); // CanaryMod: Wrap Entity
    }

    public EntityArrow(World world, double d0, double d1, double d2) {
        super(world);
        this.k = 10.0D;
        this.a(0.5F, 0.5F);
        this.b(d0, d1, d2);
        this.M = 0.0F;
        this.entity = new CanaryArrow(this); // CanaryMod: Wrap Entity
    }

    public EntityArrow(World world, EntityLivingBase entitylivingbase, EntityLivingBase entitylivingbase1, float f0, float f1) {
        super(world);
        this.k = 10.0D;
        this.c = entitylivingbase;
        if (entitylivingbase instanceof EntityPlayer) {
            this.a = 1;
        }
        this.entity = new CanaryArrow(this); // CanaryMod: Wrap Entity

        this.u = entitylivingbase.u + (double) entitylivingbase.g() - 0.10000000149011612D;
        double d0 = entitylivingbase1.t - entitylivingbase.t;
        double d1 = entitylivingbase1.D.b + (double) (entitylivingbase1.O / 3.0F) - this.u;
        double d2 = entitylivingbase1.v - entitylivingbase.v;
        double d3 = (double) MathHelper.a(d0 * d0 + d2 * d2);

        if (d3 >= 1.0E-7D) {
            float f2 = (float) (Math.atan2(d2, d0) * 180.0D / 3.1415927410125732D) - 90.0F;
            float f3 = (float) (-(Math.atan2(d1, d3) * 180.0D / 3.1415927410125732D));
            double d4 = d0 / d3;
            double d5 = d2 / d3;

            this.b(entitylivingbase.t + d4, this.u, entitylivingbase.v + d5, f2, f3);
            this.M = 0.0F;
            float f4 = (float) d3 * 0.2F;

            this.c(d0, d1 + (double) f4, d2, f0, f1);
        }
    }

    public EntityArrow(World world, EntityLivingBase entitylivingbase, float f0) {
        super(world);
        this.k = 10.0D;
        this.c = entitylivingbase;
        if (entitylivingbase instanceof EntityPlayer) {
            this.a = 1;
        }
        this.entity = new CanaryArrow(this); // CanaryMod: Wrap Entity

        this.a(0.5F, 0.5F);
        this.b(entitylivingbase.t, entitylivingbase.u + (double) entitylivingbase.g(), entitylivingbase.v, entitylivingbase.z, entitylivingbase.A);
        this.t -= (double) (MathHelper.b(this.z / 180.0F * 3.1415927F) * 0.16F);
        this.u -= 0.10000000149011612D;
        this.v -= (double) (MathHelper.a(this.z / 180.0F * 3.1415927F) * 0.16F);
        this.b(this.t, this.u, this.v);
        this.M = 0.0F;
        this.w = (double) (-MathHelper.a(this.z / 180.0F * 3.1415927F) * MathHelper.b(this.A / 180.0F * 3.1415927F));
        this.y = (double) (MathHelper.b(this.z / 180.0F * 3.1415927F) * MathHelper.b(this.A / 180.0F * 3.1415927F));
        this.x = (double) (-MathHelper.a(this.A / 180.0F * 3.1415927F));
        this.c(this.w, this.x, this.y, f0 * 1.5F, 1.0F);
    }

    protected void c() {
        this.ag.a(16, Byte.valueOf((byte) 0));
    }

    public void c(double d0, double d1, double d2, float f0, float f1) {
        float f2 = MathHelper.a(d0 * d0 + d1 * d1 + d2 * d2);

        d0 /= (double) f2;
        d1 /= (double) f2;
        d2 /= (double) f2;
        d0 += this.aa.nextGaussian() * (double) (this.aa.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) f1;
        d1 += this.aa.nextGaussian() * (double) (this.aa.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) f1;
        d2 += this.aa.nextGaussian() * (double) (this.aa.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) f1;
        d0 *= (double) f0;
        d1 *= (double) f0;
        d2 *= (double) f0;
        this.w = d0;
        this.x = d1;
        this.y = d2;
        float f3 = MathHelper.a(d0 * d0 + d2 * d2);

        this.B = this.z = (float) (Math.atan2(d0, d2) * 180.0D / 3.1415927410125732D);
        this.C = this.A = (float) (Math.atan2(d1, (double) f3) * 180.0D / 3.1415927410125732D);
        this.j = 0;
    }

    public void h() {
        super.h();
        if (this.C == 0.0F && this.B == 0.0F) {
            float f0 = MathHelper.a(this.w * this.w + this.y * this.y);

            this.B = this.z = (float) (Math.atan2(this.w, this.y) * 180.0D / 3.1415927410125732D);
            this.C = this.A = (float) (Math.atan2(this.x, (double) f0) * 180.0D / 3.1415927410125732D);
        }

        Block block = this.p.a(this.d, this.e, this.f);

        if (block.o() != Material.a) {
            block.a((IBlockAccess) this.p, this.d, this.e, this.f);
            AxisAlignedBB axisalignedbb = block.a(this.p, this.d, this.e, this.f);

            if (axisalignedbb != null && axisalignedbb.a(this.p.U().a(this.t, this.u, this.v))) {
                this.i = true;
            }
        }

        if (this.b > 0) {
            --this.b;
        }

        if (this.i) {
            int i0 = this.p.e(this.d, this.e, this.f);

            if (block == this.g && i0 == this.h) {
                ++this.j;
                if (this.j == 1200) {
                    this.B();
                }
            }
            else {
                this.i = false;
                this.w *= (double) (this.aa.nextFloat() * 0.2F);
                this.x *= (double) (this.aa.nextFloat() * 0.2F);
                this.y *= (double) (this.aa.nextFloat() * 0.2F);
                this.j = 0;
                this.au = 0;
            }
        }
        else {
            ++this.au;
            Vec3 vec3 = this.p.U().a(this.t, this.u, this.v);
            Vec3 vec31 = this.p.U().a(this.t + this.w, this.u + this.x, this.v + this.y);
            MovingObjectPosition movingobjectposition = this.p.a(vec3, vec31, false, true, false);

            vec3 = this.p.U().a(this.t, this.u, this.v);
            vec31 = this.p.U().a(this.t + this.w, this.u + this.x, this.v + this.y);
            if (movingobjectposition != null) {
                vec31 = this.p.U().a(movingobjectposition.f.c, movingobjectposition.f.d, movingobjectposition.f.e);
            }

            Entity entity = null;
            List list = this.p.b((Entity) this, this.D.a(this.w, this.x, this.y).b(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;

            int i1;
            float f1;

            for (i1 = 0; i1 < list.size(); ++i1) {
                Entity entity1 = (Entity) list.get(i1);

                if (entity1.R() && (entity1 != this.c || this.au >= 5)) {
                    f1 = 0.3F;
                    AxisAlignedBB axisalignedbb1 = entity1.D.b((double) f1, (double) f1, (double) f1);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb1.a(vec3, vec31);

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

            if (movingobjectposition != null && movingobjectposition.g != null && movingobjectposition.g instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) movingobjectposition.g;

                if (entityplayer.bF.a || this.c instanceof EntityPlayer && !((EntityPlayer) this.c).a(entityplayer)) {
                    movingobjectposition = null;
                }
            }

            float f2;
            float f3;

            if (movingobjectposition != null) {
                // CanaryMod: ProjectileHit
                ProjectileHitHook hook = (ProjectileHitHook) new ProjectileHitHook(this.getCanaryEntity(), movingobjectposition.g == null ? null : movingobjectposition.g.getCanaryEntity()).call();
                if (!hook.isCanceled() && movingobjectposition.g != null) { //
                    f2 = MathHelper.a(this.w * this.w + this.x * this.x + this.y * this.y);
                    int i2 = MathHelper.f((double) f2 * this.av);

                    if (this.f()) {
                        i2 += this.aa.nextInt(i2 / 2 + 2);
                    }

                    DamageSource damagesource = null;

                    if (this.c == null) {
                        damagesource = DamageSource.a(this, this);
                    }
                    else {
                        damagesource = DamageSource.a(this, this.c);
                    }

                    if (this.al() && !(movingobjectposition.g instanceof EntityEnderman)) {
                        movingobjectposition.g.e(5);
                    }

                    if (movingobjectposition.g.a(damagesource, (float) i2)) {
                        if (movingobjectposition.g instanceof EntityLivingBase) {
                            EntityLivingBase entitylivingbase = (EntityLivingBase) movingobjectposition.g;

                            if (!this.p.E) {
                                entitylivingbase.p(entitylivingbase.aZ() + 1);
                            }

                            if (this.aw > 0) {
                                f3 = MathHelper.a(this.w * this.w + this.y * this.y);
                                if (f3 > 0.0F) {
                                    movingobjectposition.g.g(this.w * (double) this.aw * 0.6000000238418579D / (double) f3, 0.1D, this.y * (double) this.aw * 0.6000000238418579D / (double) f3);
                                }
                            }

                            if (this.c != null && this.c instanceof EntityLivingBase) {
                                EnchantmentHelper.a(entitylivingbase, this.c);
                                EnchantmentHelper.b((EntityLivingBase) this.c, (Entity) entitylivingbase);
                            }

                            if (this.c != null && movingobjectposition.g != this.c && movingobjectposition.g instanceof EntityPlayer && this.c instanceof EntityPlayerMP) {
                                ((EntityPlayerMP) this.c).a.a((Packet) (new S2BPacketChangeGameState(6, 0.0F)));
                            }
                        }

                        this.a("random.bowhit", 1.0F, 1.2F / (this.aa.nextFloat() * 0.2F + 0.9F));
                        if (!(movingobjectposition.g instanceof EntityEnderman)) {
                            this.B();
                        }
                    }
                    else {
                        this.w *= -0.10000000149011612D;
                        this.x *= -0.10000000149011612D;
                        this.y *= -0.10000000149011612D;
                        this.z += 180.0F;
                        this.B += 180.0F;
                        this.au = 0;
                    }
                }
                else {
                    this.d = movingobjectposition.b;
                    this.e = movingobjectposition.c;
                    this.f = movingobjectposition.d;
                    this.g = block;
                    this.h = this.p.e(this.d, this.e, this.f);
                    this.w = (double) ((float) (movingobjectposition.f.c - this.t));
                    this.x = (double) ((float) (movingobjectposition.f.d - this.u));
                    this.y = (double) ((float) (movingobjectposition.f.e - this.v));
                    f2 = MathHelper.a(this.w * this.w + this.x * this.x + this.y * this.y);
                    this.t -= this.w / (double) f2 * 0.05000000074505806D;
                    this.u -= this.x / (double) f2 * 0.05000000074505806D;
                    this.v -= this.y / (double) f2 * 0.05000000074505806D;
                    this.a("random.bowhit", 1.0F, 1.2F / (this.aa.nextFloat() * 0.2F + 0.9F));
                    this.i = true;
                    this.b = 7;
                    this.a(false);
                    if (this.g.o() != Material.a) {
                        this.g.a(this.p, this.d, this.e, this.f, (Entity) this);
                    }
                }
            }

            if (this.f()) {
                for (i1 = 0; i1 < 4; ++i1) {
                    this.p.a("crit", this.t + this.w * (double) i1 / 4.0D, this.u + this.x * (double) i1 / 4.0D, this.v + this.y * (double) i1 / 4.0D, -this.w, -this.x + 0.2D, -this.y);
                }
            }

            this.t += this.w;
            this.u += this.x;
            this.v += this.y;
            f2 = MathHelper.a(this.w * this.w + this.y * this.y);
            this.z = (float) (Math.atan2(this.w, this.y) * 180.0D / 3.1415927410125732D);

            for (this.A = (float) (Math.atan2(this.x, (double) f2) * 180.0D / 3.1415927410125732D); this.A - this.C < -180.0F; this.C -= 360.0F) {
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
            float f4 = 0.99F;

            f1 = 0.05F;
            if (this.M()) {
                for (int i3 = 0; i3 < 4; ++i3) {
                    f3 = 0.25F;
                    this.p.a("bubble", this.t - this.w * (double) f3, this.u - this.x * (double) f3, this.v - this.y * (double) f3, this.w, this.x, this.y);
                }

                f4 = 0.8F;
            }

            if (this.L()) {
                this.F();
            }

            this.w *= (double) f4;
            this.x *= (double) f4;
            this.y *= (double) f4;
            this.x -= (double) f1;
            this.b(this.t, this.u, this.v);
            this.I();
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.a("xTile", (short) this.d);
        nbttagcompound.a("yTile", (short) this.e);
        nbttagcompound.a("zTile", (short) this.f);
        nbttagcompound.a("life", (short) this.j);
        nbttagcompound.a("inTile", (byte) Block.b(this.g));
        nbttagcompound.a("inData", (byte) this.h);
        nbttagcompound.a("shake", (byte) this.b);
        nbttagcompound.a("inGround", (byte) (this.i ? 1 : 0));
        nbttagcompound.a("pickup", (byte) this.a);
        nbttagcompound.a("damage", this.av);
    }

    public void a(NBTTagCompound nbttagcompound) {
        this.d = nbttagcompound.e("xTile");
        this.e = nbttagcompound.e("yTile");
        this.f = nbttagcompound.e("zTile");
        this.j = nbttagcompound.e("life");
        this.g = Block.e(nbttagcompound.d("inTile") & 255);
        this.h = nbttagcompound.d("inData") & 255;
        this.b = nbttagcompound.d("shake") & 255;
        this.i = nbttagcompound.d("inGround") == 1;
        if (nbttagcompound.b("damage", 99)) {
            this.av = nbttagcompound.i("damage");
        }

        if (nbttagcompound.b("pickup", 99)) {
            this.a = nbttagcompound.d("pickup");
        }
        else if (nbttagcompound.b("player", 99)) {
            this.a = nbttagcompound.n("player") ? 1 : 0;
        }
    }

    public void b_(EntityPlayer entityplayer) {
        if (!this.p.E && this.i && this.b <= 0) {
            boolean flag0 = this.a == 1 || this.a == 2 && entityplayer.bF.d;

            if (this.a == 1 && !entityplayer.bn.a(new ItemStack(Items.g, 1))) {
                flag0 = false;
            }

            if (flag0) {
                this.a("random.pop", 0.2F, ((this.aa.nextFloat() - this.aa.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                entityplayer.a((Entity) this, 1);
                this.B();
            }
        }
    }

    protected boolean g_() {
        return false;
    }

    public void b(double d0) {
        this.av = d0;
    }

    public double e() {
        return this.av;
    }

    public void a(int i0) {
        this.aw = i0;
    }

    public boolean av() {
        return false;
    }

    public void a(boolean flag0) {
        byte b0 = this.ag.a(16);

        if (flag0) {
            this.ag.b(16, Byte.valueOf((byte) (b0 | 1)));
        }
        else {
            this.ag.b(16, Byte.valueOf((byte) (b0 & -2)));
        }
    }

    public boolean f() {
        byte b0 = this.ag.a(16);

        return (b0 & 1) != 0;
    }

    public int cmeakb() {
        return this.aw;
    }
}

