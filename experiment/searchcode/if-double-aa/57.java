package net.minecraft.entity.monster;

import net.canarymod.api.entity.living.monster.CanarySlime;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

public class EntitySlime extends EntityLiving implements IMob {

    public float h;
    public float i;
    public float j;
    private int bn;

    public EntitySlime(World world) {
        super(world);
        int i0 = 1 << this.aa.nextInt(3);

        this.M = 0.0F;
        this.bn = this.aa.nextInt(20) + 10;
        this.a(i0);
        this.entity = new CanarySlime(this); // CanaryMod: Wrap Entity
    }

    protected void c() {
        super.c();
        this.ag.a(16, new Byte((byte) 1));
    }

    public void a(int i0) { // CanaryMod: protected => public
        this.ag.b(16, new Byte((byte) i0));
        this.a(0.6F * (float) i0, 0.6F * (float) i0);
        this.b(this.t, this.u, this.v);
        this.a(SharedMonsterAttributes.a).a((double) (i0 * i0));
        this.g(this.aY());
        this.b = i0;
    }

    public int bV() {
        return this.ag.a(16);
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.a("Size", this.bV() - 1);
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.a(nbttagcompound.f("Size") + 1);
    }

    protected String bN() {
        return "slime";
    }

    protected String bT() {
        return "mob.slime." + (this.bV() > 1 ? "big" : "small");
    }

    public void h() {
        if (!this.p.E && this.p.r == EnumDifficulty.PEACEFUL && this.bV() > 0) {
            this.L = true;
        }

        this.i += (this.h - this.i) * 0.5F;
        this.j = this.i;
        boolean flag0 = this.E;

        super.h();
        int i0;

        if (this.E && !flag0) {
            i0 = this.bV();

            for (int i1 = 0; i1 < i0 * 8; ++i1) {
                float f0 = this.aa.nextFloat() * 3.1415927F * 2.0F;
                float f1 = this.aa.nextFloat() * 0.5F + 0.5F;
                float f2 = MathHelper.a(f0) * (float) i0 * 0.5F * f1;
                float f3 = MathHelper.b(f0) * (float) i0 * 0.5F * f1;

                this.p.a(this.bN(), this.t + (double) f2, this.D.b, this.v + (double) f3, 0.0D, 0.0D, 0.0D);
            }

            if (this.bU()) {
                this.a(this.bT(), this.bf(), ((this.aa.nextFloat() - this.aa.nextFloat()) * 0.2F + 1.0F) / 0.8F);
            }

            this.h = -0.5F;
        }
        else if (!this.E && flag0) {
            this.h = 1.0F;
        }

        this.bQ();
        if (this.p.E) {
            i0 = this.bV();
            this.a(0.6F * (float) i0, 0.6F * (float) i0);
        }
    }

    protected void bq() {
        this.w();
        EntityPlayer entityplayer = this.p.b(this, 16.0D);

        if (entityplayer != null) {
            this.a(entityplayer, 10.0F, 20.0F);
        }

        if (this.E && this.bn-- <= 0) {
            this.bn = this.bP();
            if (entityplayer != null) {
                this.bn /= 3;
            }

            this.bd = true;
            if (this.bW()) {
                this.a(this.bT(), this.bf(), ((this.aa.nextFloat() - this.aa.nextFloat()) * 0.2F + 1.0F) * 0.8F);
            }

            this.be = 1.0F - this.aa.nextFloat() * 2.0F;
            this.bf = (float) (1 * this.bV());
        }
        else {
            this.bd = false;
            if (this.E) {
                this.be = this.bf = 0.0F;
            }
        }
    }

    protected void bQ() {
        this.h *= 0.6F;
    }

    protected int bP() {
        return this.aa.nextInt(20) + 10;
    }

    protected EntitySlime bO() {
        return new EntitySlime(this.p);
    }

    public void B() {
        int i0 = this.bV();

        if (!this.p.E && i0 > 1 && this.aS() <= 0.0F) {
            int i1 = 2 + this.aa.nextInt(3);

            for (int i2 = 0; i2 < i1; ++i2) {
                float f0 = ((float) (i2 % 2) - 0.5F) * (float) i0 / 4.0F;
                float f1 = ((float) (i2 / 2) - 0.5F) * (float) i0 / 4.0F;
                EntitySlime entityslime = this.bO();

                entityslime.a(i0 / 2);
                entityslime.b(this.t + (double) f0, this.u + 0.5D, this.v + (double) f1, this.aa.nextFloat() * 360.0F, 0.0F);
                this.p.d((Entity) entityslime);
            }
        }

        super.B();
    }

    public void b_(EntityPlayer entityplayer) {
        if (this.bR()) {
            int i0 = this.bV();

            if (this.o(entityplayer) && this.e(entityplayer) < 0.6D * (double) i0 * 0.6D * (double) i0 && entityplayer.a(DamageSource.a((EntityLivingBase) this), (float) this.bS())) {
                this.a("mob.attack", 1.0F, (this.aa.nextFloat() - this.aa.nextFloat()) * 0.2F + 1.0F);
            }
        }
    }

    protected boolean bR() {
        return this.bV() > 1;
    }

    protected int bS() {
        return this.bV();
    }

    protected String aT() {
        return "mob.slime." + (this.bV() > 1 ? "big" : "small");
    }

    protected String aU() {
        return "mob.slime." + (this.bV() > 1 ? "big" : "small");
    }

    protected Item u() {
        return this.bV() == 1 ? Items.aH : Item.d(0);
    }

    public boolean bw() {
        Chunk chunk = this.p.d(MathHelper.c(this.t), MathHelper.c(this.v));

        if (this.p.M().u() == WorldType.c && this.aa.nextInt(4) != 1) {
            return false;
        }
        else {
            if (this.bV() == 1 || this.p.r != EnumDifficulty.PEACEFUL) {
                BiomeGenBase biomegenbase = this.p.a(MathHelper.c(this.t), MathHelper.c(this.v));

                if (biomegenbase == BiomeGenBase.u && this.u > 50.0D && this.u < 70.0D && this.aa.nextFloat() < 0.5F && this.aa.nextFloat() < this.p.x() && this.p.k(MathHelper.c(this.t), MathHelper.c(this.u), MathHelper.c(this.v)) <= this.aa.nextInt(8)) {
                    return super.bw();
                }

                if (this.aa.nextInt(10) == 0 && chunk.a(987234911L).nextInt(10) == 0 && this.u < 40.0D) {
                    return super.bw();
                }
            }

            return false;
        }
    }

    protected float bf() {
        return 0.4F * (float) this.bV();
    }

    public int x() {
        return 0;
    }

    protected boolean bW() {
        return this.bV() > 0;
    }

    protected boolean bU() {
        return this.bV() > 2;
    }
}

