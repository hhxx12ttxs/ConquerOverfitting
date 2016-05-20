package net.minecraft.entity;

import net.canarymod.api.entity.hanging.HangingEntity;
import net.canarymod.hook.entity.HangingEntityDestroyHook;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;

public abstract class EntityHanging extends Entity {

    private int e;
    public int a;
    public int b;
    public int c;
    public int d;

    public EntityHanging(World world) {
        super(world);
        this.M = 0.0F;
        this.a(0.5F, 0.5F);
    }

    public EntityHanging(World world, int i0, int i1, int i2, int i3) {
        this(world);
        this.b = i0;
        this.c = i1;
        this.d = i2;
    }

    protected void c() {
    }

    public void a(int i0) {
        this.a = i0;
        this.B = this.z = (float) (i0 * 90);
        float f0 = (float) this.f();
        float f1 = (float) this.i();
        float f2 = (float) this.f();

        if (i0 != 2 && i0 != 0) {
            f0 = 0.5F;
        }
        else {
            f2 = 0.5F;
            this.z = this.B = (float) (Direction.f[i0] * 90);
        }

        f0 /= 32.0F;
        f1 /= 32.0F;
        f2 /= 32.0F;
        float f3 = (float) this.b + 0.5F;
        float f4 = (float) this.c + 0.5F;
        float f5 = (float) this.d + 0.5F;
        float f6 = 0.5625F;

        if (i0 == 2) {
            f5 -= f6;
        }

        if (i0 == 1) {
            f3 -= f6;
        }

        if (i0 == 0) {
            f5 += f6;
        }

        if (i0 == 3) {
            f3 += f6;
        }

        if (i0 == 2) {
            f3 -= this.c(this.f());
        }

        if (i0 == 1) {
            f5 += this.c(this.f());
        }

        if (i0 == 0) {
            f3 += this.c(this.f());
        }

        if (i0 == 3) {
            f5 -= this.c(this.f());
        }

        f4 += this.c(this.i());
        this.b((double) f3, (double) f4, (double) f5);
        float f7 = -0.03125F;

        this.D.b((double) (f3 - f0 - f7), (double) (f4 - f1 - f7), (double) (f5 - f2 - f7), (double) (f3 + f0 + f7), (double) (f4 + f1 + f7), (double) (f5 + f2 + f7));
    }

    private float c(int i0) {
        return i0 == 32 ? 0.5F : (i0 == 64 ? 0.5F : 0.0F);
    }

    public void h() {
        this.q = this.t;
        this.r = this.u;
        this.s = this.v;
        if (this.e++ == 100 && !this.p.E) {
            this.e = 0;
            if (!this.L && !this.e()) {
                this.B();
                this.b((Entity) null);
            }
        }
    }

    public boolean e() {
        if (!this.p.a((Entity) this, this.D).isEmpty()) {
            return false;
        }
        else {
            int i0 = Math.max(1, this.f() / 16);
            int i1 = Math.max(1, this.i() / 16);
            int i2 = this.b;
            int i3 = this.c;
            int i4 = this.d;

            if (this.a == 2) {
                i2 = MathHelper.c(this.t - (double) ((float) this.f() / 32.0F));
            }

            if (this.a == 1) {
                i4 = MathHelper.c(this.v - (double) ((float) this.f() / 32.0F));
            }

            if (this.a == 0) {
                i2 = MathHelper.c(this.t - (double) ((float) this.f() / 32.0F));
            }

            if (this.a == 3) {
                i4 = MathHelper.c(this.v - (double) ((float) this.f() / 32.0F));
            }

            i3 = MathHelper.c(this.u - (double) ((float) this.i() / 32.0F));

            for (int i5 = 0; i5 < i0; ++i5) {
                for (int i6 = 0; i6 < i1; ++i6) {
                    Material material;

                    if (this.a != 2 && this.a != 0) {
                        material = this.p.a(this.b, i3 + i6, i4 + i5).o();
                    }
                    else {
                        material = this.p.a(i2 + i5, i3 + i6, this.d).o();
                    }

                    if (!material.a()) {
                        return false;
                    }
                }
            }

            List list = this.p.b((Entity) this, this.D);
            Iterator iterator = list.iterator();

            Entity entity;

            do {
                if (!iterator.hasNext()) {
                    return true;
                }

                entity = (Entity) iterator.next();
            } while (!(entity instanceof EntityHanging));

            return false;
        }
    }

    public boolean R() {
        return true;
    }

    public boolean i(Entity entity) {
        return entity instanceof EntityPlayer ? this.a(DamageSource.a((EntityPlayer) entity), 0.0F) : false;
    }

    public void i(int i0) {
        this.p.X();
    }

    public boolean a(DamageSource damagesource, float f0) {
        if (this.aw()) {
            return false;
        }
        else {
            if (!this.L && !this.p.E) {
                EntityPlayer entityplayer = null;

                if (damagesource.i() instanceof EntityPlayer) {
                    entityplayer = (EntityPlayer) damagesource.i();
                }

                if (entityplayer != null) {
                    // CanaryMod: HangingEntityDestory
                    HangingEntityDestroyHook hook = (HangingEntityDestroyHook) new HangingEntityDestroyHook((HangingEntity) this.entity, ((EntityPlayerMP) entityplayer).getPlayer(), damagesource.getCanaryDamageSource()).call();
                    if (hook.isCanceled()) {
                        return false;
                    }
                    //
                }

                this.B();
                this.Q();
                this.b(damagesource.j());
            }

            return true;
        }
    }

    public void d(double d0, double d1, double d2) {
        if (!this.p.E && !this.L && d0 * d0 + d1 * d1 + d2 * d2 > 0.0D) {
            this.B();
            this.b((Entity) null);
        }
    }

    public void g(double d0, double d1, double d2) {
        if (!this.p.E && !this.L && d0 * d0 + d1 * d1 + d2 * d2 > 0.0D) {
            this.B();
            this.b((Entity) null);
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.a("Direction", (byte) this.a);
        nbttagcompound.a("TileX", this.b);
        nbttagcompound.a("TileY", this.c);
        nbttagcompound.a("TileZ", this.d);
        switch (this.a) {
            case 0:
                nbttagcompound.a("Dir", (byte) 2);
                break;

            case 1:
                nbttagcompound.a("Dir", (byte) 1);
                break;

            case 2:
                nbttagcompound.a("Dir", (byte) 0);
                break;

            case 3:
                nbttagcompound.a("Dir", (byte) 3);
        }
    }

    public void a(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.b("Direction", 99)) {
            this.a = nbttagcompound.d("Direction");
        }
        else {
            switch (nbttagcompound.d("Dir")) {
                case 0:
                    this.a = 2;
                    break;

                case 1:
                    this.a = 1;
                    break;

                case 2:
                    this.a = 0;
                    break;

                case 3:
                    this.a = 3;
            }
        }

        this.b = nbttagcompound.f("TileX");
        this.c = nbttagcompound.f("TileY");
        this.d = nbttagcompound.f("TileZ");
        this.a(this.a);
    }

    public abstract int f();

    public abstract int i();

    public abstract void b(Entity entity);

    protected boolean V() {
        return false;
    }

    // CanaryMod
    public int getTickCounter() {
        return this.e;
    }

    public void setTicks(int ticks) {
        this.e = ticks;
    }
    //
}

