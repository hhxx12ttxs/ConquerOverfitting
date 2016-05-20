package net.minecraft.entity.monster;

import net.canarymod.api.entity.living.monster.CanaryWitch;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class EntityWitch extends EntityMob implements IRangedAttackMob {

    private static final UUID bp = UUID.fromString("5CD17E52-A79A-43D3-A529-90FDE04B181E");
    private static final AttributeModifier bq = (new AttributeModifier(bp, "Drinking speed penalty", -0.25D, 0)).a(false);
    private static final Item[] br = new Item[]{ Items.aO, Items.aT, Items.ax, Items.bp, Items.bo, Items.H, Items.y, Items.y };
    private int bs;

    public EntityWitch(World world) {
        super(world);
        this.c.a(1, new EntityAISwimming(this));
        this.c.a(2, new EntityAIArrowAttack(this, 1.0D, 60, 10.0F));
        this.c.a(2, new EntityAIWander(this, 1.0D));
        this.c.a(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.c.a(3, new EntityAILookIdle(this));
        this.d.a(1, new EntityAIHurtByTarget(this, false));
        this.d.a(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
        this.entity = new CanaryWitch(this); // CanaryMod: Wrap Entity
    }

    protected void c() {
        super.c();
        this.z().a(21, Byte.valueOf((byte) 0));
    }

    protected String t() {
        return "mob.witch.idle";
    }

    protected String aT() {
        return "mob.witch.hurt";
    }

    protected String aU() {
        return "mob.witch.death";
    }

    public void a(boolean flag0) {
        this.z().b(21, Byte.valueOf((byte) (flag0 ? 1 : 0)));
    }

    public boolean bX() {
        return this.z().a(21) == 1;
    }

    protected void aD() {
        super.aD();
        this.a(SharedMonsterAttributes.a).a(26.0D);
        this.a(SharedMonsterAttributes.d).a(0.25D);
    }

    public boolean bk() {
        return true;
    }

    public void e() {
        if (!this.p.E) {
            if (this.bX()) {
                if (this.bs-- <= 0) {
                    this.a(false);
                    ItemStack itemstack = this.be();

                    this.c(0, (ItemStack) null);
                    if (itemstack != null && itemstack.b() == Items.bn) {
                        List list = Items.bn.g(itemstack);

                        if (list != null) {
                            Iterator iterator = list.iterator();

                            while (iterator.hasNext()) {
                                PotionEffect potioneffect = (PotionEffect) iterator.next();

                                this.c(new PotionEffect(potioneffect));
                            }
                        }
                    }

                    this.a(SharedMonsterAttributes.d).b(bq);
                }
            }
            else {
                short short1 = -1;

                if (this.aa.nextFloat() < 0.15F && this.a(Material.h) && !this.a(Potion.o)) {
                    short1 = 8237;
                }
                else if (this.aa.nextFloat() < 0.15F && this.al() && !this.a(Potion.n)) {
                    short1 = 16307;
                }
                else if (this.aa.nextFloat() < 0.05F && this.aS() < this.aY()) {
                    short1 = 16341;
                }
                else if (this.aa.nextFloat() < 0.25F && this.o() != null && !this.a(Potion.c) && this.o().e(this) > 121.0D) {
                    short1 = 16274;
                }
                else if (this.aa.nextFloat() < 0.25F && this.o() != null && !this.a(Potion.c) && this.o().e(this) > 121.0D) {
                    short1 = 16274;
                }

                if (short1 > -1) {
                    this.c(0, new ItemStack(Items.bn, 1, short1));
                    this.bs = this.be().n();
                    this.a(true);
                    AttributeInstance attributeinstance = this.a(SharedMonsterAttributes.d);

                    attributeinstance.b(bq);
                    attributeinstance.a(bq);
                }
            }

            if (this.aa.nextFloat() < 7.5E-4F) {
                this.p.a(this, (byte) 15);
            }
        }

        super.e();
    }

    protected float c(DamageSource damagesource, float f0) {
        f0 = super.c(damagesource, f0);
        if (damagesource.j() == this) {
            f0 = 0.0F;
        }

        if (damagesource.s()) {
            f0 = (float) ((double) f0 * 0.15D);
        }

        return f0;
    }

    protected void b(boolean flag0, int i0) {
        int i1 = this.aa.nextInt(3) + 1;

        for (int i2 = 0; i2 < i1; ++i2) {
            int i3 = this.aa.nextInt(3);
            Item item = br[this.aa.nextInt(br.length)];

            if (i0 > 0) {
                i3 += this.aa.nextInt(i0 + 1);
            }

            for (int i4 = 0; i4 < i3; ++i4) {
                this.a(item, 1);
            }
        }
    }

    public void a(EntityLivingBase entitylivingbase, float f0) {
        if (!this.bX()) {
            EntityPotion entitypotion = new EntityPotion(this.p, this, 32732);

            entitypotion.A -= -20.0F;
            double d0 = entitylivingbase.t + entitylivingbase.w - this.t;
            double d1 = entitylivingbase.u + (double) entitylivingbase.g() - 1.100000023841858D - this.u;
            double d2 = entitylivingbase.v + entitylivingbase.y - this.v;
            float f1 = MathHelper.a(d0 * d0 + d2 * d2);

            if (f1 >= 8.0F && !entitylivingbase.a(Potion.d)) {
                entitypotion.a(32698);
            }
            else if (entitylivingbase.aS() >= 8.0F && !entitylivingbase.a(Potion.u)) {
                entitypotion.a(32660);
            }
            else if (f1 <= 3.0F && !entitylivingbase.a(Potion.t) && this.aa.nextFloat() < 0.25F) {
                entitypotion.a(32696);
            }

            entitypotion.c(d0, d1 + (double) (f1 * 0.2F), d2, 0.75F, 8.0F);
            this.p.d((Entity) entitypotion);
        }
    }
}

