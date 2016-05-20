package net.minecraft.entity.passive;

import net.canarymod.api.entity.living.animal.CanaryWolf;
import net.canarymod.hook.entity.EntityTameHook;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBeg;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITargetNonTamed;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityWolf extends EntityTameable {

    private float bq;
    private float br;
    private boolean bs;
    private boolean bt;
    private float bu;
    private float bv;

    public EntityWolf(World world) {
        super(world);
        this.a(0.6F, 0.8F);
        this.m().a(true);
        this.c.a(1, new EntityAISwimming(this));
        this.c.a(2, this.bp);
        this.c.a(3, new EntityAILeapAtTarget(this, 0.4F));
        this.c.a(4, new EntityAIAttackOnCollide(this, 1.0D, true));
        this.c.a(5, new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.c.a(6, new EntityAIMate(this, 1.0D));
        this.c.a(7, new EntityAIWander(this, 1.0D));
        this.c.a(8, new EntityAIBeg(this, 8.0F));
        this.c.a(9, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.c.a(9, new EntityAILookIdle(this));
        this.d.a(1, new EntityAIOwnerHurtByTarget(this));
        this.d.a(2, new EntityAIOwnerHurtTarget(this));
        this.d.a(3, new EntityAIHurtByTarget(this, true));
        this.d.a(4, new EntityAITargetNonTamed(this, EntitySheep.class, 200, false));
        this.j(false);
        this.entity = new CanaryWolf(this); // CanaryMod: Wrap Entity
    }

    protected void aD() {
        super.aD();
        this.a(SharedMonsterAttributes.d).a(0.30000001192092896D);
        if (this.bX()) {
            this.a(SharedMonsterAttributes.a).a(20.0D);
        }
        else {
            this.a(SharedMonsterAttributes.a).a(8.0D);
        }
    }

    public boolean bk() {
        return true;
    }

    public void d(EntityLivingBase entitylivingbase) {
        super.d(entitylivingbase);
        if (entitylivingbase == null) {
            this.l(false);
        }
        else if (!this.bX()) {
            this.l(true);
        }
    }

    protected void bp() {
        this.ag.b(18, Float.valueOf(this.aS()));
    }

    protected void c() {
        super.c();
        this.ag.a(18, new Float(this.aS()));
        this.ag.a(19, new Byte((byte) 0));
        this.ag.a(20, new Byte((byte) BlockColored.b(1)));
    }

    protected void a(int i0, int i1, int i2, Block block) {
        this.a("mob.wolf.step", 0.15F, 1.0F);
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.a("Angry", this.cg());
        nbttagcompound.a("CollarColor", (byte) this.ch());
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.l(nbttagcompound.n("Angry"));
        if (nbttagcompound.b("CollarColor", 99)) {
            this.s(nbttagcompound.d("CollarColor"));
        }
    }

    protected String t() {
        return this.cg() ? "mob.wolf.growl" : (this.aa.nextInt(3) == 0 ? (this.bX() && this.ag.d(18) < 10.0F ? "mob.wolf.whine" : "mob.wolf.panting") : "mob.wolf.bark");
    }

    protected String aT() {
        return "mob.wolf.hurt";
    }

    protected String aU() {
        return "mob.wolf.death";
    }

    protected float bf() {
        return 0.4F;
    }

    protected Item u() {
        return Item.d(-1);
    }

    public void e() {
        super.e();
        if (!this.p.E && this.bs && !this.bt && !this.bQ() && this.E) {
            this.bt = true;
            this.bu = 0.0F;
            this.bv = 0.0F;
            this.p.a(this, (byte) 8);
        }
    }

    public void h() {
        super.h();
        this.br = this.bq;
        if (this.ci()) {
            this.bq += (1.0F - this.bq) * 0.4F;
        }
        else {
            this.bq += (0.0F - this.bq) * 0.4F;
        }

        if (this.ci()) {
            this.g = 10;
        }

        if (this.L()) {
            this.bs = true;
            this.bt = false;
            this.bu = 0.0F;
            this.bv = 0.0F;
        }
        else if ((this.bs || this.bt) && this.bt) {
            if (this.bu == 0.0F) {
                this.a("mob.wolf.shake", this.bf(), (this.aa.nextFloat() - this.aa.nextFloat()) * 0.2F + 1.0F);
            }

            this.bv = this.bu;
            this.bu += 0.05F;
            if (this.bv >= 2.0F) {
                this.bs = false;
                this.bt = false;
                this.bv = 0.0F;
                this.bu = 0.0F;
            }

            if (this.bu > 0.4F) {
                float f0 = (float) this.D.b;
                int i0 = (int) (MathHelper.a((this.bu - 0.4F) * 3.1415927F) * 7.0F);

                for (int i1 = 0; i1 < i0; ++i1) {
                    float f1 = (this.aa.nextFloat() * 2.0F - 1.0F) * this.N * 0.5F;
                    float f2 = (this.aa.nextFloat() * 2.0F - 1.0F) * this.N * 0.5F;

                    this.p.a("splash", this.t + (double) f1, (double) (f0 + 0.8F), this.v + (double) f2, this.w, this.x, this.y);
                }
            }
        }
    }

    public float g() {
        return this.O * 0.8F;
    }

    public int x() {
        return this.bY() ? 20 : super.x();
    }

    public boolean a(DamageSource damagesource, float f0) {
        if (this.aw()) {
            return false;
        }
        else {
            Entity entity = damagesource.j();

            this.bp.a(false);
            if (entity != null && !(entity instanceof EntityPlayer) && !(entity instanceof EntityArrow)) {
                f0 = (f0 + 1.0F) / 2.0F;
            }

            return super.a(damagesource, f0);
        }
    }

    public boolean m(Entity entity) {
        int i0 = this.bX() ? 4 : 2;

        return entity.a(DamageSource.a((EntityLivingBase) this), (float) i0);
    }

    public void j(boolean flag0) {
        super.j(flag0);
        if (flag0) {
            this.a(SharedMonsterAttributes.a).a(20.0D);
        }
        else {
            this.a(SharedMonsterAttributes.a).a(8.0D);
        }
    }

    public boolean a(EntityPlayer entityplayer) {
        ItemStack itemstack = entityplayer.bn.h();

        if (this.bX()) {
            if (itemstack != null) {
                if (itemstack.b() instanceof ItemFood) {
                    ItemFood itemfood = (ItemFood) itemstack.b();

                    if (itemfood.i() && this.ag.d(18) < 20.0F) {
                        if (!entityplayer.bF.d) {
                            --itemstack.b;
                        }

                        this.f((float) itemfood.g(itemstack));
                        if (itemstack.b <= 0) {
                            entityplayer.bn.a(entityplayer.bn.c, (ItemStack) null);
                        }

                        return true;
                    }
                }
                else if (itemstack.b() == Items.aR) {
                    int i0 = BlockColored.b(itemstack.k());

                    if (i0 != this.ch()) {
                        this.s(i0);
                        if (!entityplayer.bF.d && --itemstack.b <= 0) {
                            entityplayer.bn.a(entityplayer.bn.c, (ItemStack) null);
                        }

                        return true;
                    }
                }
            }

            if (entityplayer.b_().equalsIgnoreCase(this.b()) && !this.p.E && !this.c(itemstack)) {
                this.bp.a(!this.bY());
                this.bd = false;
                this.a((PathEntity) null);
                this.b((Entity) null);
                this.d((EntityLivingBase) null);
            }
        }
        else if (itemstack != null && itemstack.b() == Items.aS && !this.cg()) {
            if (!entityplayer.bF.d) {
                --itemstack.b;
            }

            if (itemstack.b <= 0) {
                entityplayer.bn.a(entityplayer.bn.c, (ItemStack) null);
            }

            if (!this.p.E) {
                // CanaryMod: EntityTame
                EntityTameHook hook = new EntityTameHook((net.canarymod.api.entity.living.animal.EntityAnimal) this.getCanaryEntity(), ((EntityPlayerMP) entityplayer).getPlayer(), this.aa.nextInt(3) == 0);

                if (hook.isTamed() && !hook.isCanceled()) {
                    //
                    this.j(true);
                    this.a((PathEntity) null);
                    this.d((EntityLivingBase) null);
                    this.bp.a(true);
                    this.g(20.0F);
                    this.b(entityplayer.b_());
                    this.i(true);
                    this.p.a(this, (byte) 7);
                }
                else {
                    this.i(false);
                    this.p.a(this, (byte) 6);
                }
            }

            return true;
        }

        return super.a(entityplayer);
    }

    public boolean c(ItemStack itemstack) {
        return itemstack == null ? false : (!(itemstack.b() instanceof ItemFood) ? false : ((ItemFood) itemstack.b()).i());
    }

    public int bz() {
        return 8;
    }

    public boolean cg() {
        return (this.ag.a(16) & 2) != 0;
    }

    public void l(boolean flag0) {
        byte b0 = this.ag.a(16);

        if (flag0) {
            this.ag.b(16, Byte.valueOf((byte) (b0 | 2)));
        }
        else {
            this.ag.b(16, Byte.valueOf((byte) (b0 & -3)));
        }
    }

    public int ch() {
        return this.ag.a(20) & 15;
    }

    public void s(int i0) {
        this.ag.b(20, Byte.valueOf((byte) (i0 & 15)));
    }

    public EntityWolf b(EntityAgeable entityageable) {
        EntityWolf entitywolf = new EntityWolf(this.p);
        String s0 = this.b();

        if (s0 != null && s0.trim().length() > 0) {
            entitywolf.b(s0);
            entitywolf.j(true);
        }

        return entitywolf;
    }

    public void m(boolean flag0) {
        if (flag0) {
            this.ag.b(19, Byte.valueOf((byte) 1));
        }
        else {
            this.ag.b(19, Byte.valueOf((byte) 0));
        }
    }

    public boolean a(EntityAnimal entityanimal) {
        if (entityanimal == this) {
            return false;
        }
        else if (!this.bX()) {
            return false;
        }
        else if (!(entityanimal instanceof EntityWolf)) {
            return false;
        }
        else {
            EntityWolf entitywolf = (EntityWolf) entityanimal;

            return !entitywolf.bX() ? false : (entitywolf.bY() ? false : this.cc() && entitywolf.cc());
        }
    }

    public boolean ci() {
        return this.ag.a(19) == 1;
    }

    protected boolean v() {
        return !this.bX() && this.ab > 2400;
    }

    public boolean a(EntityLivingBase entitylivingbase, EntityLivingBase entitylivingbase1) {
        if (!(entitylivingbase instanceof EntityCreeper) && !(entitylivingbase instanceof EntityGhast)) {
            if (entitylivingbase instanceof EntityWolf) {
                EntityWolf entitywolf = (EntityWolf) entitylivingbase;

                if (entitywolf.bX() && entitywolf.bZ() == entitylivingbase1) {
                    return false;
                }
            }

            return entitylivingbase instanceof EntityPlayer && entitylivingbase1 instanceof EntityPlayer && !((EntityPlayer) entitylivingbase1).a((EntityPlayer) entitylivingbase) ? false : !(entitylivingbase instanceof EntityHorse) || !((EntityHorse) entitylivingbase).ca();
        }
        else {
            return false;
        }
    }

    public EntityAgeable a(EntityAgeable entityageable) {
        return this.b(entityageable);
    }
}

