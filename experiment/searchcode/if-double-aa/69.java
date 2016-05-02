package net.minecraft.entity.passive;

import net.canarymod.api.entity.living.animal.CanaryBat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Calendar;

public class EntityBat extends EntityAmbientCreature {

    private ChunkCoordinates h;

    public EntityBat(World world) {
        super(world);
        this.a(0.5F, 0.9F);
        this.a(true);
        this.entity = new CanaryBat(this); // CanaryMod: Wrap Entity
    }

    protected void c() {
        super.c();
        this.ag.a(16, new Byte((byte) 0));
    }

    protected float bf() {
        return 0.1F;
    }

    protected float bg() {
        return super.bg() * 0.95F;
    }

    protected String t() {
        return this.bN() && this.aa.nextInt(4) != 0 ? null : "mob.bat.idle";
    }

    protected String aT() {
        return "mob.bat.hurt";
    }

    protected String aU() {
        return "mob.bat.death";
    }

    public boolean S() {
        return false;
    }

    protected void n(Entity entity) {
    }

    protected void bo() {
    }

    protected void aD() {
        super.aD();
        this.a(SharedMonsterAttributes.a).a(6.0D);
    }

    public boolean bN() {
        return (this.ag.a(16) & 1) != 0;
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

    protected boolean bk() {
        return true;
    }

    public void h() {
        super.h();
        if (this.bN()) {
            this.w = this.x = this.y = 0.0D;
            this.u = (double) MathHelper.c(this.u) + 1.0D - (double) this.O;
        }
        else {
            this.x *= 0.6000000238418579D;
        }
    }

    protected void bn() {
        super.bn();
        if (this.bN()) {
            if (!this.p.a(MathHelper.c(this.t), (int) this.u + 1, MathHelper.c(this.v)).r()) {
                this.a(false);
                this.p.a((EntityPlayer) null, 1015, (int) this.t, (int) this.u, (int) this.v, 0);
            }
            else {
                if (this.aa.nextInt(200) == 0) {
                    this.aP = (float) this.aa.nextInt(360);
                }

                if (this.p.a(this, 4.0D) != null) {
                    this.a(false);
                    this.p.a((EntityPlayer) null, 1015, (int) this.t, (int) this.u, (int) this.v, 0);
                }
            }
        }
        else {
            if (this.h != null && (!this.p.c(this.h.a, this.h.b, this.h.c) || this.h.b < 1)) {
                this.h = null;
            }

            if (this.h == null || this.aa.nextInt(30) == 0 || this.h.e((int) this.t, (int) this.u, (int) this.v) < 4.0F) {
                this.h = new ChunkCoordinates((int) this.t + this.aa.nextInt(7) - this.aa.nextInt(7), (int) this.u + this.aa.nextInt(6) - 2, (int) this.v + this.aa.nextInt(7) - this.aa.nextInt(7));
            }

            double d0 = (double) this.h.a + 0.5D - this.t;
            double d1 = (double) this.h.b + 0.1D - this.u;
            double d2 = (double) this.h.c + 0.5D - this.v;

            this.w += (Math.signum(d0) * 0.5D - this.w) * 0.10000000149011612D;
            this.x += (Math.signum(d1) * 0.699999988079071D - this.x) * 0.10000000149011612D;
            this.y += (Math.signum(d2) * 0.5D - this.y) * 0.10000000149011612D;
            float f0 = (float) (Math.atan2(this.y, this.w) * 180.0D / 3.1415927410125732D) - 90.0F;
            float f1 = MathHelper.g(f0 - this.z);

            this.bf = 0.5F;
            this.z += f1;
            if (this.aa.nextInt(100) == 0 && this.p.a(MathHelper.c(this.t), (int) this.u + 1, MathHelper.c(this.v)).r()) {
                this.a(true);
            }
        }
    }

    protected boolean g_() {
        return false;
    }

    protected void b(float f0) {
    }

    protected void a(double d0, boolean flag0) {
    }

    public boolean az() {
        return true;
    }

    public boolean a(DamageSource damagesource, float f0) {
        if (this.aw()) {
            return false;
        }
        else {
            if (!this.p.E && this.bN()) {
                this.a(false);
            }

            return super.a(damagesource, f0);
        }
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.ag.b(16, Byte.valueOf(nbttagcompound.d("BatFlags")));
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.a("BatFlags", this.ag.a(16));
    }

    public boolean bw() {
        int i0 = MathHelper.c(this.D.b);

        if (i0 >= 63) {
            return false;
        }
        else {
            int i1 = MathHelper.c(this.t);
            int i2 = MathHelper.c(this.v);
            int i3 = this.p.k(i1, i0, i2);
            byte b0 = 4;
            Calendar calendar = this.p.V();

            if ((calendar.get(2) + 1 != 10 || calendar.get(5) < 20) && (calendar.get(2) + 1 != 11 || calendar.get(5) > 3)) {
                if (this.aa.nextBoolean()) {
                    return false;
                }
            }
            else {
                b0 = 7;
            }

            return i3 > this.aa.nextInt(b0) ? false : super.bw();
        }
    }
}

