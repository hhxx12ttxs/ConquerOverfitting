package net.minecraft.entity.passive;

import net.canarymod.api.entity.living.animal.CanarySquid;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntitySquid extends EntityWaterMob {

    public float bp;
    public float bq;
    public float br;
    public float bs;
    public float bt;
    public float bu;
    public float bv;
    public float bw;
    private float bx;
    private float by;
    private float bz;
    private float bA;
    private float bB;
    private float bC;

    public EntitySquid(World world) {
        super(world);
        this.a(0.95F, 0.95F);
        this.by = 1.0F / (this.aa.nextFloat() + 1.0F) * 0.2F;
        this.entity = new CanarySquid(this); // CanaryMod: Wrap Entity
    }

    protected void aD() {
        super.aD();
        this.a(SharedMonsterAttributes.a).a(10.0D);
    }

    protected String t() {
        return null;
    }

    protected String aT() {
        return null;
    }

    protected String aU() {
        return null;
    }

    protected float bf() {
        return 0.4F;
    }

    protected Item u() {
        return Item.d(0);
    }

    protected boolean g_() {
        return false;
    }

    protected void b(boolean flag0, int i0) {
        int i1 = this.aa.nextInt(3 + i0) + 1;

        for (int i2 = 0; i2 < i1; ++i2) {
            this.a(new ItemStack(Items.aR, 1, 0), 0.0F);
        }
    }

    public boolean M() {
        return this.p.a(this.D.b(0.0D, -0.6000000238418579D, 0.0D), Material.h, (Entity) this);
    }

    public void e() {
        super.e();
        this.bq = this.bp;
        this.bs = this.br;
        this.bu = this.bt;
        this.bw = this.bv;
        this.bt += this.by;
        if (this.bt > 6.2831855F) {
            this.bt -= 6.2831855F;
            if (this.aa.nextInt(10) == 0) {
                this.by = 1.0F / (this.aa.nextFloat() + 1.0F) * 0.2F;
            }
        }

        if (this.M()) {
            float f0;

            if (this.bt < 3.1415927F) {
                f0 = this.bt / 3.1415927F;
                this.bv = MathHelper.a(f0 * f0 * 3.1415927F) * 3.1415927F * 0.25F;
                if ((double) f0 > 0.75D) {
                    this.bx = 1.0F;
                    this.bz = 1.0F;
                }
                else {
                    this.bz *= 0.8F;
                }
            }
            else {
                this.bv = 0.0F;
                this.bx *= 0.9F;
                this.bz *= 0.99F;
            }

            if (!this.p.E) {
                this.w = (double) (this.bA * this.bx);
                this.x = (double) (this.bB * this.bx);
                this.y = (double) (this.bC * this.bx);
            }

            f0 = MathHelper.a(this.w * this.w + this.y * this.y);
            this.aN += (-((float) Math.atan2(this.w, this.y)) * 180.0F / 3.1415927F - this.aN) * 0.1F;
            this.z = this.aN;
            this.br += 3.1415927F * this.bz * 1.5F;
            this.bp += (-((float) Math.atan2((double) f0, this.x)) * 180.0F / 3.1415927F - this.bp) * 0.1F;
        }
        else {
            this.bv = MathHelper.e(MathHelper.a(this.bt)) * 3.1415927F * 0.25F;
            if (!this.p.E) {
                this.w = 0.0D;
                this.x -= 0.08D;
                this.x *= 0.9800000190734863D;
                this.y = 0.0D;
            }

            this.bp = (float) ((double) this.bp + (double) (-90.0F - this.bp) * 0.02D);
        }
    }

    public void e(float f0, float f1) {
        this.d(this.w, this.x, this.y);
    }

    protected void bq() {
        ++this.aV;
        if (this.aV > 100) {
            this.bA = this.bB = this.bC = 0.0F;
        }
        else if (this.aa.nextInt(50) == 0 || !this.ad || this.bA == 0.0F && this.bB == 0.0F && this.bC == 0.0F) {
            float f0 = this.aa.nextFloat() * 3.1415927F * 2.0F;

            this.bA = MathHelper.b(f0) * 0.2F;
            this.bB = -0.1F + this.aa.nextFloat() * 0.2F;
            this.bC = MathHelper.a(f0) * 0.2F;
        }

        this.w();
    }

    public boolean bw() {
        return this.u > 45.0D && this.u < 63.0D && super.bw();
    }
}

