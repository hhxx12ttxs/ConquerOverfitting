package net.minecraft.entity.monster;

import net.canarymod.api.entity.living.monster.CanaryZombie;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLivingData;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Calendar;
import java.util.UUID;

public class EntityZombie extends EntityMob {

    protected static final Attribute bp = (new RangedAttribute("zombie.spawnReinforcements", 0.0D, 0.0D, 1.0D)).a("Spawn Reinforcements Chance");
    private static final UUID bq = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final AttributeModifier br = new AttributeModifier(bq, "Baby speed boost", 0.5D, 1);
    private final EntityAIBreakDoor bs = new EntityAIBreakDoor(this);
    private int bt;
    private boolean bu = false;
    private float bv = -1.0F;
    private float bw;

    public EntityZombie(World world) {
        super(world);
        this.m().b(true);
        this.c.a(0, new EntityAISwimming(this));
        this.c.a(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
        this.c.a(4, new EntityAIAttackOnCollide(this, EntityVillager.class, 1.0D, true));
        this.c.a(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.c.a(6, new EntityAIMoveThroughVillage(this, 1.0D, false));
        this.c.a(7, new EntityAIWander(this, 1.0D));
        this.c.a(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.c.a(8, new EntityAILookIdle(this));
        this.d.a(1, new EntityAIHurtByTarget(this, true));
        this.d.a(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
        this.d.a(2, new EntityAINearestAttackableTarget(this, EntityVillager.class, 0, false));
        this.a(0.6F, 1.8F);
        this.entity = new CanaryZombie(this); // CanaryMod: Wrap Entity
    }

    protected void aD() {
        super.aD();
        this.a(SharedMonsterAttributes.b).a(40.0D);
        this.a(SharedMonsterAttributes.d).a(0.23000000417232513D);
        this.a(SharedMonsterAttributes.e).a(3.0D);
        this.bc().b(bp).a(this.aa.nextDouble() * 0.10000000149011612D);
    }

    protected void c() {
        super.c();
        this.z().a(12, Byte.valueOf((byte) 0));
        this.z().a(13, Byte.valueOf((byte) 0));
        this.z().a(14, Byte.valueOf((byte) 0));
    }

    public int aV() {
        int i0 = super.aV() + 2;

        if (i0 > 20) {
            i0 = 20;
        }

        return i0;
    }

    protected boolean bk() {
        return true;
    }

    public boolean bX() {
        return this.bu;
    }

    public void a(boolean flag0) {
        if (this.bu != flag0) {
            this.bu = flag0;
            if (flag0) {
                this.c.a(1, this.bs);
            }
            else {
                this.c.a((EntityAIBase) this.bs);
            }
        }

    }

    public boolean f() {
        return this.z().a(12) == 1;
    }

    protected int e(EntityPlayer entityplayer) {
        if (this.f()) {
            this.b = (int) ((float) this.b * 2.5F);
        }

        return super.e(entityplayer);
    }

    public void i(boolean flag0) {
        this.z().b(12, Byte.valueOf((byte) (flag0 ? 1 : 0)));
        if (this.p != null && !this.p.E) {
            AttributeInstance attributeinstance = this.a(SharedMonsterAttributes.d);

            attributeinstance.b(br);
            if (flag0) {
                attributeinstance.a(br);
            }
        }
        this.k(flag0);
    }

    public boolean bZ() {
        return this.z().a(13) == 1;
    }

    public void j(boolean flag0) {
        this.z().b(13, Byte.valueOf((byte) (flag0 ? 1 : 0)));
    }

    public void e() {
        if (this.p.v() && !this.p.E && !this.f()) {
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

        super.e();
    }

    public boolean a(DamageSource damagesource, float f0) {
        if (!super.a(damagesource, f0)) {
            return false;
        }
        else {
            EntityLivingBase entitylivingbase = this.o();

            if (entitylivingbase == null && this.bR() instanceof EntityLivingBase) {
                entitylivingbase = (EntityLivingBase) this.bR();
            }

            if (entitylivingbase == null && damagesource.j() instanceof EntityLivingBase) {
                entitylivingbase = (EntityLivingBase) damagesource.j();
            }

            if (entitylivingbase != null && this.p.r == EnumDifficulty.HARD && (double) this.aa.nextFloat() < this.a(bp).e()) {
                int i0 = MathHelper.c(this.t);
                int i1 = MathHelper.c(this.u);
                int i2 = MathHelper.c(this.v);
                EntityZombie entityzombie = new EntityZombie(this.p);

                for (int i3 = 0; i3 < 50; ++i3) {
                    int i4 = i0 + MathHelper.a(this.aa, 7, 40) * MathHelper.a(this.aa, -1, 1);
                    int i5 = i1 + MathHelper.a(this.aa, 7, 40) * MathHelper.a(this.aa, -1, 1);
                    int i6 = i2 + MathHelper.a(this.aa, 7, 40) * MathHelper.a(this.aa, -1, 1);

                    if (World.a((IBlockAccess) this.p, i4, i5 - 1, i6) && this.p.k(i4, i5, i6) < 10) {
                        entityzombie.b((double) i4, (double) i5, (double) i6);
                        if (this.p.b(entityzombie.D) && this.p.a((Entity) entityzombie, entityzombie.D).isEmpty() && !this.p.d(entityzombie.D)) {
                            this.p.d((Entity) entityzombie);
                            entityzombie.d(entitylivingbase);
                            entityzombie.a((EntityLivingData) null);
                            this.a(bp).a(new AttributeModifier("Zombie reinforcement caller charge", -0.05000000074505806D, 0));
                            entityzombie.a(bp).a(new AttributeModifier("Zombie reinforcement callee charge", -0.05000000074505806D, 0));
                            break;
                        }
                    }
                }
            }

            return true;
        }
    }

    public void h() {
        if (!this.p.E && this.ca()) {
            int i0 = this.cc();

            this.bt -= i0;
            if (this.bt <= 0) {
                this.cb();
            }
        }

        super.h();
    }

    public boolean m(Entity entity) {
        boolean flag0 = super.m(entity);

        if (flag0) {
            int i0 = this.p.r.a();

            if (this.be() == null && this.al() && this.aa.nextFloat() < (float) i0 * 0.3F) {
                entity.e(2 * i0);
            }
        }

        return flag0;
    }

    protected String t() {
        return "mob.zombie.say";
    }

    protected String aT() {
        return "mob.zombie.hurt";
    }

    protected String aU() {
        return "mob.zombie.death";
    }

    protected void a(int i0, int i1, int i2, Block block) {
        this.a("mob.zombie.step", 0.15F, 1.0F);
    }

    protected Item u() {
        return Items.bh;
    }

    public EnumCreatureAttribute bd() {
        return EnumCreatureAttribute.UNDEAD;
    }

    protected void n(int i0) {
        switch (this.aa.nextInt(3)) {
            case 0:
                this.a(Items.j, 1);
                break;

            case 1:
                this.a(Items.bF, 1);
                break;

            case 2:
                this.a(Items.bG, 1);
        }
    }

    protected void bA() {
        super.bA();
        if (this.aa.nextFloat() < (this.p.r == EnumDifficulty.HARD ? 0.05F : 0.01F)) {
            int i0 = this.aa.nextInt(3);

            if (i0 == 0) {
                this.c(0, new ItemStack(Items.l));
            }
            else {
                this.c(0, new ItemStack(Items.a));
            }
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        if (this.f()) {
            nbttagcompound.a("IsBaby", true);
        }

        if (this.bZ()) {
            nbttagcompound.a("IsVillager", true);
        }

        nbttagcompound.a("ConversionTime", this.ca() ? this.bt : -1);
        nbttagcompound.a("CanBreakDoors", this.bX());
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (nbttagcompound.n("IsBaby")) {
            this.i(true);
        }

        if (nbttagcompound.n("IsVillager")) {
            this.j(true);
        }

        if (nbttagcompound.b("ConversionTime", 99) && nbttagcompound.f("ConversionTime") > -1) {
            this.a(nbttagcompound.f("ConversionTime"));
        }
        this.a(nbttagcompound.n("CanBreakDoors"));
    }

    public void a(EntityLivingBase entitylivingbase) {
        super.a(entitylivingbase);
        if ((this.p.r == EnumDifficulty.NORMAL || this.p.r == EnumDifficulty.HARD) && entitylivingbase instanceof EntityVillager) {
            if (this.aa.nextBoolean()) {
                return;
            }

            EntityZombie entityzombie = new EntityZombie(this.p);

            entityzombie.j(entitylivingbase);
            this.p.e((Entity) entitylivingbase);
            entityzombie.a((EntityLivingData) null);
            entityzombie.j(true);
            if (entitylivingbase.f()) {
                entityzombie.i(true);
            }

            this.p.d((Entity) entityzombie);
            this.p.a((EntityPlayer) null, 1016, (int) this.t, (int) this.u, (int) this.v, 0);
        }
    }

    public EntityLivingData a(EntityLivingData entitylivingdata) {
        Object object = super.a(entitylivingdata);
        float f0 = this.p.b(this.t, this.u, this.v);

        this.h(this.aa.nextFloat() < 0.55F * f0);
        if (object == null) {
            object = new GroupData(this.p.s.nextFloat() < 0.05F, this.p.s.nextFloat() < 0.05F, null);
        }

        if (object instanceof GroupData) {
            GroupData entityzombie_groupdata = (GroupData) object;

            if (entityzombie_groupdata.b) {
                this.j(true);
            }

            if (entityzombie_groupdata.a) {
                this.i(true);
            }
        }

        this.a(this.aa.nextFloat() < f0 * 0.1F);
        this.bA();
        this.bB();
        if (this.q(4) == null) {
            Calendar calendar = this.p.V();

            if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.aa.nextFloat() < 0.25F) {
                this.c(4, new ItemStack(this.aa.nextFloat() < 0.1F ? Blocks.aP : Blocks.aK));
                this.e[4] = 0.0F;
            }
        }

        this.a(SharedMonsterAttributes.c).a(new AttributeModifier("Random spawn bonus", this.aa.nextDouble() * 0.05000000074505806D, 0));
        double d0 = this.aa.nextDouble() * 1.5D * (double) this.p.b(this.t, this.u, this.v);

        if (d0 > 1.0D) {
            this.a(SharedMonsterAttributes.b).a(new AttributeModifier("Random zombie-spawn bonus", d0, 2));
        }

        if (this.aa.nextFloat() < f0 * 0.05F) {
            this.a(bp).a(new AttributeModifier("Leader zombie bonus", this.aa.nextDouble() * 0.25D + 0.5D, 0));
            this.a(SharedMonsterAttributes.a).a(new AttributeModifier("Leader zombie bonus", this.aa.nextDouble() * 3.0D + 1.0D, 2));
            this.a(true);
        }

        return (EntityLivingData) object;
    }

    public boolean a(EntityPlayer entityplayer) {
        ItemStack itemstack = entityplayer.bD();

        if (itemstack != null && itemstack.b() == Items.ao && itemstack.k() == 0 && this.bZ() && this.a(Potion.t)) {
            if (!entityplayer.bF.d) {
                --itemstack.b;
            }

            if (itemstack.b <= 0) {
                entityplayer.bn.a(entityplayer.bn.c, (ItemStack) null);
            }

            if (!this.p.E) {
                this.a(this.aa.nextInt(2401) + 3600);
            }

            return true;
        }
        else {
            return false;
        }
    }

    protected void a(int i0) {
        this.bt = i0;
        this.z().b(14, Byte.valueOf((byte) 1));
        this.m(Potion.t.H);
        this.c(new PotionEffect(Potion.g.H, i0, Math.min(this.p.r.a() - 1, 0)));
        this.p.a(this, (byte) 16);
    }

    protected boolean v() {
        return !this.ca();
    }

    public boolean ca() {
        return this.z().a(14) == 1;
    }

    public void cb() { // CanaryMod: protected => public
        EntityVillager entityvillager = new EntityVillager(this.p);

        entityvillager.j(this);
        entityvillager.a((EntityLivingData) null);
        entityvillager.cb();
        if (this.f()) {
            entityvillager.c(-24000);
        }

        this.p.e((Entity) this);
        this.p.d((Entity) entityvillager);
        entityvillager.c(new PotionEffect(Potion.k.H, 200, 0));
        this.p.a((EntityPlayer) null, 1017, (int) this.t, (int) this.u, (int) this.v, 0);
    }

    protected int cc() {
        int i0 = 1;

        if (this.aa.nextFloat() < 0.01F) {
            int i1 = 0;

            for (int i2 = (int) this.t - 4; i2 < (int) this.t + 4 && i1 < 14; ++i2) {
                for (int i3 = (int) this.u - 4; i3 < (int) this.u + 4 && i1 < 14; ++i3) {
                    for (int i4 = (int) this.v - 4; i4 < (int) this.v + 4 && i1 < 14; ++i4) {
                        Block block = this.p.a(i2, i3, i4);

                        if (block == Blocks.aY || block == Blocks.C) {
                            if (this.aa.nextFloat() < 0.3F) {
                                ++i0;
                            }

                            ++i1;
                        }
                    }
                }
            }
        }

        return i0;
    }

    public void k(boolean flag0) {
        this.a(flag0 ? 0.5F : 1.0F);
    }

    protected final void a(float f0, float f1) {
        boolean flag0 = this.bv > 0.0F && this.bw > 0.0F;

        this.bv = f0;
        this.bw = f1;
        if (!flag0) {
            this.a(1.0F);
        }

    }

    protected final void a(float f0) {
        super.a(this.bv * f0, this.bw * f0);
    }

    class GroupData implements EntityLivingData {

        public boolean a;
        public boolean b;

        private GroupData(boolean flag2, boolean flag3) {
            this.a = false;
            this.b = false;
            this.a = flag2;
            this.b = flag3;
        }

        GroupData(boolean flag2, boolean flag3, Object object) {
            this(flag2, flag3);
        }
    }

    // CanaryMod
    public int getConvertTicks() {
        return this.bt;
    }

    public void stopConversion() {
        this.z().b(12, Byte.valueOf((byte) 0));
        this.bt = -1;
    }
    //
}

