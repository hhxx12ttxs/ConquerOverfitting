package net.minecraft.entity.item;

import net.canarymod.api.CanaryDamageSource;
import net.canarymod.api.entity.living.LivingBase;
import net.canarymod.api.entity.vehicle.CanaryBoat;
import net.canarymod.api.entity.vehicle.Vehicle;
import net.canarymod.api.world.position.Vector3D;
import net.canarymod.hook.CancelableHook;
import net.canarymod.hook.entity.VehicleCollisionHook;
import net.canarymod.hook.entity.VehicleDamageHook;
import net.canarymod.hook.entity.VehicleDestroyHook;
import net.canarymod.hook.entity.VehicleEnterHook;
import net.canarymod.hook.entity.VehicleExitHook;
import net.canarymod.hook.entity.VehicleMoveHook;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class EntityBoat extends Entity {

    private boolean a;
    private double b;
    private int c;
    private double d;
    private double e;
    private double f;
    private double g;
    private double h;

    public EntityBoat(World world) {
        super(world);
        this.a = true;
        this.b = 0.07D;
        this.l = true;
        this.a(1.5F, 0.6F);
        this.M = this.O / 2.0F;
        this.entity = new CanaryBoat(this); // CanaryMod: Wrap Entity
    }

    protected boolean g_() {
        return false;
    }

    protected void c() {
        this.ag.a(17, new Integer(0));
        this.ag.a(18, new Integer(1));
        this.ag.a(19, new Float(0.0F));
    }

    public AxisAlignedBB g(Entity entity) {
        return entity.D;
    }

    public AxisAlignedBB J() {
        return this.D;
    }

    public boolean S() {
        return true;
    }

    public EntityBoat(World world, double d0, double d1, double d2) {
        this(world);
        this.b(d0, d1 + (double) this.M, d2);
        this.w = 0.0D;
        this.x = 0.0D;
        this.y = 0.0D;
        this.q = d0;
        this.r = d1;
        this.s = d2;

        this.entity = new CanaryBoat(this); // CanaryMod: Wrap Entity
    }

    public double ae() {
        return (double) this.O * 0.0D - 0.30000001192092896D;
    }

    public boolean a(DamageSource damagesource, float f0) {
        if (this.aw()) {
            return false;
        }
        else if (!this.p.E && !this.L) {
            // CanaryMod: VehicleDamage
            net.canarymod.api.entity.Entity attk = null;

            if (damagesource.i() != null) {
                attk = damagesource.i().getCanaryEntity();
            }
            VehicleDamageHook hook = (VehicleDamageHook) new VehicleDamageHook((CanaryBoat) this.entity, attk, new CanaryDamageSource(damagesource), (int) f0).call();
            if (hook.isCanceled()) {
                return false;
            }
            f0 = hook.getDamageDealt();
            //

            this.c(-this.i());
            this.a(10);
            this.a(this.e() + f0 * 10.0F);
            this.Q();
            boolean flag0 = damagesource.j() instanceof EntityPlayer && ((EntityPlayer) damagesource.j()).bF.d;

            if (flag0 || this.e() > 40.0F) {
                if (this.m != null) {
                    this.m.a((Entity) this);
                }

                if (!flag0) {
                    this.a(Items.az, 1, 0.0F);
                }
                // CanaryMod: VehicleDestroy
                new VehicleDestroyHook((Vehicle) this.entity).call();
                //
                this.B();
            }

            return true;
        }
        else {
            return true;
        }
    }

    public boolean R() {
        return !this.L;
    }

    public void h() {
        super.h();
        if (this.f() > 0) {
            this.a(this.f() - 1);
        }

        if (this.e() > 0.0F) {
            this.a(this.e() - 1.0F);
        }

        double ppX = this.q, ppY = this.r, ppZ = this.s; // CanaryMod: previousprevious
        float prevRot = this.B, prevPit = this.C;

        this.q = this.t;
        this.r = this.u;
        this.s = this.v;

        byte b0 = 5;
        double d0 = 0.0D;

        for (int i0 = 0; i0 < b0; ++i0) {
            double d1 = this.D.b + (this.D.e - this.D.b) * (double) (i0 + 0) / (double) b0 - 0.125D;
            double d2 = this.D.b + (this.D.e - this.D.b) * (double) (i0 + 1) / (double) b0 - 0.125D;
            AxisAlignedBB axisalignedbb = AxisAlignedBB.a().a(this.D.a, d1, this.D.c, this.D.d, d2, this.D.f);

            if (this.p.b(axisalignedbb, Material.h)) {
                d0 += 1.0D / (double) b0;
            }
        }

        double d3 = Math.sqrt(this.w * this.w + this.y * this.y);
        double d4;
        double d5;
        int i1;

        if (d3 > 0.26249999999999996D) {
            d4 = Math.cos((double) this.z * 3.141592653589793D / 180.0D);
            d5 = Math.sin((double) this.z * 3.141592653589793D / 180.0D);

            for (i1 = 0; (double) i1 < 1.0D + d3 * 60.0D; ++i1) {
                double d6 = (double) (this.aa.nextFloat() * 2.0F - 1.0F);
                double d7 = (double) (this.aa.nextInt(2) * 2 - 1) * 0.7D;
                double d8;
                double d9;

                if (this.aa.nextBoolean()) {
                    d8 = this.t - d4 * d6 * 0.8D + d5 * d7;
                    d9 = this.v - d5 * d6 * 0.8D - d4 * d7;
                    this.p.a("splash", d8, this.u - 0.125D, d9, this.w, this.x, this.y);
                }
                else {
                    d8 = this.t + d4 + d5 * d6 * 0.7D;
                    d9 = this.v + d5 - d4 * d6 * 0.7D;
                    this.p.a("splash", d8, this.u - 0.125D, d9, this.w, this.x, this.y);
                }
            }
        }

        double d10;
        double d11;

        if (this.p.E && this.a) {
            if (this.c > 0) {
                d4 = this.t + (this.d - this.t) / (double) this.c;
                d5 = this.u + (this.e - this.u) / (double) this.c;
                d10 = this.v + (this.f - this.v) / (double) this.c;
                d11 = MathHelper.g(this.g - (double) this.z);
                this.z = (float) ((double) this.z + d11 / (double) this.c);
                this.A = (float) ((double) this.A + (this.h - (double) this.A) / (double) this.c);
                --this.c;
                this.b(d4, d5, d10);
                this.b(this.z, this.A);
            }
            else {
                d4 = this.t + this.w;
                d5 = this.u + this.x;
                d10 = this.v + this.y;
                this.b(d4, d5, d10);
                if (this.E) {
                    this.w *= 0.5D;
                    this.x *= 0.5D;
                    this.y *= 0.5D;
                }

                this.w *= 0.9900000095367432D;
                this.x *= 0.949999988079071D;
                this.y *= 0.9900000095367432D;
            }
        }
        else {
            if (d0 < 1.0D) {
                d4 = d0 * 2.0D - 1.0D;
                this.x += 0.03999999910593033D * d4;
            }
            else {
                if (this.x < 0.0D) {
                    this.x /= 2.0D;
                }

                this.x += 0.007000000216066837D;
            }

            if (this.m != null && this.m instanceof EntityLivingBase) {
                EntityLivingBase entitylivingbase = (EntityLivingBase) this.m;
                float f0 = this.m.z + -entitylivingbase.be * 90.0F;

                this.w += -Math.sin((double) (f0 * 3.1415927F / 180.0F)) * this.b * (double) entitylivingbase.bf * 0.05000000074505806D;
                this.y += Math.cos((double) (f0 * 3.1415927F / 180.0F)) * this.b * (double) entitylivingbase.bf * 0.05000000074505806D;
            }

            d4 = Math.sqrt(this.w * this.w + this.y * this.y);
            if (d4 > 0.35D) {
                d5 = 0.35D / d4;
                this.w *= d5;
                this.y *= d5;
                d4 = 0.35D;
            }

            if (d4 > d3 && this.b < 0.35D) {
                this.b += (0.35D - this.b) / 35.0D;
                if (this.b > 0.35D) {
                    this.b = 0.35D;
                }
            }
            else {
                this.b -= (this.b - 0.07D) / 35.0D;
                if (this.b < 0.07D) {
                    this.b = 0.07D;
                }
            }

            int i2;

            for (i2 = 0; i2 < 4; ++i2) {
                int i3 = MathHelper.c(this.t + ((double) (i2 % 2) - 0.5D) * 0.8D);

                i1 = MathHelper.c(this.v + ((double) (i2 / 2) - 0.5D) * 0.8D);

                for (int i4 = 0; i4 < 2; ++i4) {
                    int i5 = MathHelper.c(this.u) + i4;
                    Block block = this.p.a(i3, i5, i1);

                    if (block == Blocks.aC) {
                        this.p.f(i3, i5, i1);
                        this.F = false;
                    }
                    else if (block == Blocks.bi) {
                        this.p.a(i3, i5, i1, true);
                        this.F = false;
                    }
                }
            }

            if (this.E) {
                this.w *= 0.5D;
                this.x *= 0.5D;
                this.y *= 0.5D;
            }

            this.d(this.w, this.x, this.y);
            if (this.F && d3 > 0.2D) {
                if (!this.p.E && !this.L) {
                    this.B();

                    for (i2 = 0; i2 < 3; ++i2) {
                        this.a(Item.a(Blocks.f), 1, 0.0F);
                    }

                    for (i2 = 0; i2 < 2; ++i2) {
                        this.a(Items.y, 1, 0.0F);
                    }
                }
            }
            else {
                this.w *= 0.9900000095367432D;
                this.x *= 0.949999988079071D;
                this.y *= 0.9900000095367432D;
            }

            this.A = 0.0F;
            d5 = (double) this.z;
            d10 = this.q - this.t;
            d11 = this.s - this.v;
            if (d10 * d10 + d11 * d11 > 0.001D) {
                d5 = (double) ((float) (Math.atan2(d11, d10) * 180.0D / 3.141592653589793D));
            }

            double d12 = MathHelper.g(d5 - (double) this.z);

            if (d12 > 20.0D) {
                d12 = 20.0D;
            }

            if (d12 < -20.0D) {
                d12 = -20.0D;
            }

            this.z = (float) ((double) this.z + d12);
            this.b(this.z, this.A);
            // CanaryMod: VehicleMove
            Vector3D from = new Vector3D(this.q, this.r, this.s);
            Vector3D to = new Vector3D(this.t, this.u, this.v);
            if (hasMovedOneBlockOrMore()) {
                VehicleMoveHook vmh = (VehicleMoveHook) new VehicleMoveHook((Vehicle) this.entity, from, to).call();
                if (vmh.isCanceled()) {
                    this.w = 0.0D;
                    this.x = 0.0D;
                    this.y = 0.0D;
                    this.b(this.q, this.r, this.s, this.B, this.C);
                    this.q = ppX;
                    this.r = ppY;
                    this.s = ppZ;
                    this.B = prevRot;
                    this.C = prevPit;
                    this.ac(); // Update rider
                }
            }
            //
            if (!this.p.E) {
                List list = this.p.b((Entity) this, this.D.b(0.20000000298023224D, 0.0D, 0.20000000298023224D));

                if (list != null && !list.isEmpty()) {
                    for (int i6 = 0; i6 < list.size(); ++i6) {
                        Entity entity = (Entity) list.get(i6);

                        if (entity != this.m && entity.S() && entity instanceof EntityBoat) {
                            // CanaryMod: VehicleCollision
                            VehicleCollisionHook vch = (VehicleCollisionHook) new VehicleCollisionHook((Vehicle) this.entity, entity.getCanaryEntity()).call();
                            if (!vch.isCanceled()) {
                                entity.f((Entity) this);
                            }
                            //
                        }
                    }
                }

                if (this.m != null && this.m.L) {
                    this.m = null;
                }
            }
        }
    }

    public void ac() {
        if (this.m != null) {
            double d0 = Math.cos((double) this.z * 3.141592653589793D / 180.0D) * 0.4D;
            double d1 = Math.sin((double) this.z * 3.141592653589793D / 180.0D) * 0.4D;

            this.m.b(this.t + d0, this.u + this.ae() + this.m.ad(), this.v + d1);
        }
    }

    protected void b(NBTTagCompound nbttagcompound) {
    }

    protected void a(NBTTagCompound nbttagcompound) {
    }

    public boolean c(EntityPlayer entityplayer) {
        if (this.m != null && this.m instanceof EntityPlayer && this.m != entityplayer) {
            return true;
        }
        else {
            if (!this.p.E) {
                // CanaryMod: VehicleEnter/VehicleExit
                CancelableHook hook = null;

                if (this.n == null) {
                    hook = new VehicleEnterHook((Vehicle) this.entity, (LivingBase) entityplayer.getCanaryEntity());
                }
                else if (this.n == entityplayer) {
                    hook = new VehicleExitHook((Vehicle) this.entity, (LivingBase) entityplayer.getCanaryEntity());
                }
                if (hook != null) {
                    hook.call();
                    if (!hook.isCanceled()) {
                        entityplayer.a((Entity) this);
                    }
                }
                //
            }

            return true;
        }
    }

    protected void a(double d0, boolean flag0) {
        int i0 = MathHelper.c(this.t);
        int i1 = MathHelper.c(this.u);
        int i2 = MathHelper.c(this.v);

        if (flag0) {
            if (this.S > 3.0F) {
                this.b(this.S);
                if (!this.p.E && !this.L) {
                    this.B();

                    int i3;

                    for (i3 = 0; i3 < 3; ++i3) {
                        this.a(Item.a(Blocks.f), 1, 0.0F);
                    }

                    for (i3 = 0; i3 < 2; ++i3) {
                        this.a(Items.y, 1, 0.0F);
                    }
                }

                this.S = 0.0F;
            }
        }
        else if (this.p.a(i0, i1 - 1, i2).o() != Material.h && d0 < 0.0D) {
            this.S = (float) ((double) this.S - d0);
        }

    }

    public void a(float f0) {
        this.ag.b(19, Float.valueOf(f0));
    }

    public float e() {
        return this.ag.d(19);
    }

    public void a(int i0) {
        this.ag.b(17, Integer.valueOf(i0));
    }

    public int f() {
        return this.ag.c(17);
    }

    public void c(int i0) {
        this.ag.b(18, Integer.valueOf(i0));
    }

    public int i() {
        return this.ag.c(18);
    }
}

