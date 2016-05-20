package net.minecraft.entity.monster;

import net.canarymod.api.entity.living.monster.CanarySkeleton;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLivingData;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFleeSun;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIRestrictSun;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderHell;

import java.util.Calendar;

public class EntitySkeleton extends EntityMob implements IRangedAttackMob {

    private EntityAIArrowAttack bp = new EntityAIArrowAttack(this, 1.0D, 20, 60, 15.0F);
    private EntityAIAttackOnCollide bq = new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.2D, false);

    public EntitySkeleton(World world) {
        super(world);
        this.c.a(1, new EntityAISwimming(this));
        this.c.a(2, new EntityAIRestrictSun(this));
        this.c.a(3, new EntityAIFleeSun(this, 1.0D));
        this.c.a(5, new EntityAIWander(this, 1.0D));
        this.c.a(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.c.a(6, new EntityAILookIdle(this));
        this.d.a(1, new EntityAIHurtByTarget(this, false));
        this.d.a(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
        if (world != null && !world.E) {
            this.bX();
        }
        this.entity = new CanarySkeleton(this); // CanaryMod: Wrap Entity
    }

    protected void aD() {
        super.aD();
        this.a(SharedMonsterAttributes.d).a(0.25D);
    }

    protected void c() {
        super.c();
        this.ag.a(13, new Byte((byte) 0));
    }

    public boolean bk() {
        return true;
    }

    protected String t() {
        return "mob.skeleton.say";
    }

    protected String aT() {
        return "mob.skeleton.hurt";
    }

    protected String aU() {
        return "mob.skeleton.death";
    }

    protected void a(int i0, int i1, int i2, Block block) {
        this.a("mob.skeleton.step", 0.15F, 1.0F);
    }

    public boolean m(Entity entity) {
        if (super.m(entity)) {
            if (this.bZ() == 1 && entity instanceof EntityLivingBase) {
                ((EntityLivingBase) entity).c(new PotionEffect(Potion.v.H, 200));
            }

            return true;
        }
        else {
            return false;
        }
    }

    public EnumCreatureAttribute bd() {
        return EnumCreatureAttribute.UNDEAD;
    }

    public void e() {
        if (this.p.v() && !this.p.E) {
            float f0 = this.d(1.0F);

            if (f0 > 0.5F && this.aa.nextFloat() * 30.0F < (f0 - 0.4F) * 2.0F && this.p.i(MathHelper.c(this.t), MathHelper.c(this.u), MathHelper.c(this.v))) {
                boolean flag0 = true;
                ItemStack itemstack = this.q(4);

                if (itemstack != null) {
                    if (itemstack.g()) {
                        itemstack.b(itemstack.j() + this.aa.nextInt(2));
                        if (itemstack.j() >= itemstack.l()) {
                            this.a(itemstack);
                            this.c(4, (ItemStack) null);
                        }
                    }

                    flag0 = false;
                }

                if (flag0) {
                    this.e(8);
                }
            }
        }

        if (this.p.E && this.bZ() == 1) {
            this.a(0.72F, 2.34F);
        }

        super.e();
    }

    public void ab() {
        super.ab();
        if (this.n instanceof EntityCreature) {
            EntityCreature entitycreature = (EntityCreature) this.n;

            this.aN = entitycreature.aN;
        }
    }

    public void a(DamageSource damagesource) {
        super.a(damagesource);
        if (damagesource.i() instanceof EntityArrow && damagesource.j() instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) damagesource.j();
            double d0 = entityplayer.t - this.t;
            double d1 = entityplayer.v - this.v;

            if (d0 * d0 + d1 * d1 >= 2500.0D) {
                entityplayer.a((StatBase) AchievementList.v);
            }
        }
    }

    protected Item u() {
        return Items.g;
    }

    protected void b(boolean flag0, int i0) {
        int i1;
        int i2;

        if (this.bZ() == 1) {
            i1 = this.aa.nextInt(3 + i0) - 1;

            for (i2 = 0; i2 < i1; ++i2) {
                this.a(Items.h, 1);
            }
        }
        else {
            i1 = this.aa.nextInt(3 + i0);

            for (i2 = 0; i2 < i1; ++i2) {
                this.a(Items.g, 1);
            }
        }

        i1 = this.aa.nextInt(3 + i0);

        for (i2 = 0; i2 < i1; ++i2) {
            this.a(Items.aS, 1);
        }
    }

    protected void n(int i0) {
        if (this.bZ() == 1) {
            this.a(new ItemStack(Items.bL, 1, 1), 0.0F);
        }
    }

    protected void bA() {
        super.bA();
        this.c(0, new ItemStack(Items.f));
    }

    public EntityLivingData a(EntityLivingData entitylivingdata) {
        entitylivingdata = super.a(entitylivingdata);
        if (this.p.t instanceof WorldProviderHell && this.aI().nextInt(5) > 0) {
            this.c.a(4, this.bq);
            this.a(1);
            this.c(0, new ItemStack(Items.q));
            this.a(SharedMonsterAttributes.e).a(4.0D);
        }
        else {
            this.c.a(4, this.bp);
            this.bA();
            this.bB();
        }

        this.h(this.aa.nextFloat() < 0.55F * this.p.b(this.t, this.u, this.v));
        if (this.q(4) == null) {
            Calendar calendar = this.p.V();

            if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.aa.nextFloat() < 0.25F) {
                this.c(4, new ItemStack(this.aa.nextFloat() < 0.1F ? Blocks.aP : Blocks.aK));
                this.e[4] = 0.0F;
            }
        }

        return entitylivingdata;
    }

    public void bX() {
        this.c.a((EntityAIBase) this.bq);
        this.c.a((EntityAIBase) this.bp);
        ItemStack itemstack = this.be();

        if (itemstack != null && itemstack.b() == Items.f) {
            this.c.a(4, this.bp);
        }
        else {
            this.c.a(4, this.bq);
        }
    }

    public void a(EntityLivingBase entitylivingbase, float f0) {
        EntityArrow entityarrow = new EntityArrow(this.p, this, entitylivingbase, 1.6F, (float) (14 - this.p.r.a() * 4));
        int i0 = EnchantmentHelper.a(Enchantment.v.B, this.be());
        int i1 = EnchantmentHelper.a(Enchantment.w.B, this.be());

        entityarrow.b((double) (f0 * 2.0F) + this.aa.nextGaussian() * 0.25D + (double) ((float) this.p.r.a() * 0.11F));
        if (i0 > 0) {
            entityarrow.b(entityarrow.e() + (double) i0 * 0.5D + 0.5D);
        }

        if (i1 > 0) {
            entityarrow.a(i1);
        }

        if (EnchantmentHelper.a(Enchantment.x.B, this.be()) > 0 || this.bZ() == 1) {
            entityarrow.e(100);
        }

        this.a("random.bow", 1.0F, 1.0F / (this.aI().nextFloat() * 0.4F + 0.8F));
        this.p.d((Entity) entityarrow);
    }

    public int bZ() {
        return this.ag.a(13);
    }

    public void a(int i0) {
        this.ag.b(13, Byte.valueOf((byte) i0));
        this.af = i0 == 1;
        if (i0 == 1) {
            this.a(0.72F, 2.34F);
        }
        else {
            this.a(0.6F, 1.8F);
        }
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (nbttagcompound.b("SkeletonType", 99)) {
            byte b0 = nbttagcompound.d("SkeletonType");

            this.a(b0);
        }

        this.bX();
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.a("SkeletonType", (byte) this.bZ());
    }

    public void c(int i0, ItemStack itemstack) {
        super.c(i0, itemstack);
        if (!this.p.E && i0 == 0) {
            this.bX();
        }
    }

    public double ad() {
        return super.ad() - 0.5D;
    }
}

