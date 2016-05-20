package net.minecraft.entity.item;

import net.canarymod.api.entity.CanaryEnderEye;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityEnderEye extends Entity {

    public double a; // CanaryMod: private => public; targetX
    public double b; // CanaryMod: private => public; targetY
    public double c; // CanaryMod: private => public; targetZ
    public int d; // CanaryMod: private => public; despawnTimer
    public boolean e; // CanaryMod: private => public; dropOrShatter

    public EntityEnderEye(World world) {
        super(world);
        this.a(0.25F, 0.25F);
        this.entity = new CanaryEnderEye(this); // CanaryMod: Wrap Entity
    }

    protected void c() {
    }

    public EntityEnderEye(World world, double d0, double d1, double d2) {
        super(world);
        this.d = 0;
        this.a(0.25F, 0.25F);
        this.b(d0, d1, d2);
        this.M = 0.0F;
        this.entity = new CanaryEnderEye(this); // CanaryMod: Wrap Entity
    }

    public void a(double d0, int i0, double d1) {
        double d2 = d0 - this.t;
        double d3 = d1 - this.v;
        float f0 = MathHelper.a(d2 * d2 + d3 * d3);

        if (f0 > 12.0F) {
            this.a = this.t + d2 / (double) f0 * 12.0D;
            this.c = this.v + d3 / (double) f0 * 12.0D;
            this.b = this.u + 8.0D;
        }
        else {
            this.a = d0;
            this.b = (double) i0;
            this.c = d1;
        }

        this.d = 0;
        this.e = this.aa.nextInt(5) > 0;
    }

    public void h() {
        this.T = this.t;
        this.U = this.u;
        this.V = this.v;
        super.h();
        this.t += this.w;
        this.u += this.x;
        this.v += this.y;
        float f0 = MathHelper.a(this.w * this.w + this.y * this.y);

        this.z = (float) (Math.atan2(this.w, this.y) * 180.0D / 3.1415927410125732D);

        for (this.A = (float) (Math.atan2(this.x, (double) f0) * 180.0D / 3.1415927410125732D); this.A - this.C < -180.0F; this.C -= 360.0F) {
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
        if (!this.p.E) {
            double d0 = this.a - this.t;
            double d1 = this.c - this.v;
            float f1 = (float) Math.sqrt(d0 * d0 + d1 * d1);
            float f2 = (float) Math.atan2(d1, d0);
            double d2 = (double) f0 + (double) (f1 - f0) * 0.0025D;

            if (f1 < 1.0F) {
                d2 *= 0.8D;
                this.x *= 0.8D;
            }

            this.w = Math.cos((double) f2) * d2;
            this.y = Math.sin((double) f2) * d2;
            if (this.u < this.b) {
                this.x += (1.0D - this.x) * 0.014999999664723873D;
            }
            else {
                this.x += (-1.0D - this.x) * 0.014999999664723873D;
            }
        }

        float f3 = 0.25F;

        if (this.M()) {
            for (int i0 = 0; i0 < 4; ++i0) {
                this.p.a("bubble", this.t - this.w * (double) f3, this.u - this.x * (double) f3, this.v - this.y * (double) f3, this.w, this.x, this.y);
            }
        }
        else {
            this.p.a("portal", this.t - this.w * (double) f3 + this.aa.nextDouble() * 0.6D - 0.3D, this.u - this.x * (double) f3 - 0.5D, this.v - this.y * (double) f3 + this.aa.nextDouble() * 0.6D - 0.3D, this.w, this.x, this.y);
        }

        if (!this.p.E) {
            this.b(this.t, this.u, this.v);
            ++this.d;
            if (this.d > 80 && !this.p.E) {
                this.B();
                if (this.e) {
                    this.p.d((Entity) (new EntityItem(this.p, this.t, this.u, this.v, new ItemStack(Items.bv))));
                }
                else {
                    this.p.c(2003, (int) Math.round(this.t), (int) Math.round(this.u), (int) Math.round(this.v), 0);
                }
            }
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
    }

    public void a(NBTTagCompound nbttagcompound) {
    }

    public float d(float f0) {
        return 1.0F;
    }

    public boolean av() {
        return false;
    }
}

