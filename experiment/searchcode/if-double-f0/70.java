package net.minecraft.entity.item;

import net.canarymod.api.entity.CanaryTNTPrimed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityTNTPrimed extends Entity {

    public int a;
    private EntityLivingBase b;

    public EntityTNTPrimed(World world) {
        super(world);
        this.l = true;
        this.a(0.98F, 0.98F);
        this.M = this.O / 2.0F;
        this.entity = new CanaryTNTPrimed(this); // CanaryMod: Wrap Entity
    }

    public EntityTNTPrimed(World world, double d0, double d1, double d2, EntityLivingBase entitylivingbase) {
        this(world);
        this.b(d0, d1, d2);
        float f0 = (float) (Math.random() * 3.1415927410125732D * 2.0D);

        this.w = (double) (-((float) Math.sin((double) f0)) * 0.02F);
        this.x = 0.20000000298023224D;
        this.y = (double) (-((float) Math.cos((double) f0)) * 0.02F);
        this.a = 80;
        this.q = d0;
        this.r = d1;
        this.s = d2;
        this.b = entitylivingbase;
    }

    protected void c() {
    }

    protected boolean g_() {
        return false;
    }

    public boolean R() {
        return !this.L;
    }

    public void h() {
        this.q = this.t;
        this.r = this.u;
        this.s = this.v;
        this.x -= 0.03999999910593033D;
        this.d(this.w, this.x, this.y);
        this.w *= 0.9800000190734863D;
        this.x *= 0.9800000190734863D;
        this.y *= 0.9800000190734863D;
        if (this.E) {
            this.w *= 0.699999988079071D;
            this.y *= 0.699999988079071D;
            this.x *= -0.5D;
        }

        if (this.a-- <= 0) {
            this.B();
            if (!this.p.E) {
                this.f();
            }
        }
        else {
            this.p.a("smoke", this.t, this.u + 0.5D, this.v, 0.0D, 0.0D, 0.0D);
        }
    }

    private void f() {
        // float f0 = 4.0F;

        this.p.a(this, this.t, this.u, this.v, ((CanaryTNTPrimed) entity).getPower(), true); // CanaryMod: get power level
    }

    protected void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.a("Fuse", (byte) this.a);
    }

    protected void a(NBTTagCompound nbttagcompound) {
        this.a = nbttagcompound.d("Fuse");
    }

    public EntityLivingBase e() {
        return this.b;
    }
}

