package net.minecraft.entity.passive;

import net.canarymod.api.entity.EntityType;
import net.canarymod.api.entity.living.animal.CanaryAnimal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public abstract class EntityAnimal extends EntityAgeable implements IAnimals {

    private int bp;
    private int bq;
    private EntityPlayer br;

    public EntityAnimal(World world) {
        super(world);

        //CanaryMod: Genericly wrapped animal
        this.entity = new CanaryAnimal(this) {

            @Override
            public String getFqName() {
                return "Generic Animal";
            }

            @Override
            public EntityType getEntityType() {
                return EntityType.GENERIC_ANIMAL;
            }

            @Override
            public EntityAnimal getHandle() {
                return (EntityAnimal) entity;
            }
        };
    }

    protected void bp() {
        if (this.d() != 0) {
            this.bp = 0;
        }

        super.bp();
    }

    public void e() {
        super.e();
        if (this.d() != 0) {
            this.bp = 0;
        }

        if (this.bp > 0) {
            --this.bp;
            String s0 = "heart";

            if (this.bp % 10 == 0) {
                double d0 = this.aa.nextGaussian() * 0.02D;
                double d1 = this.aa.nextGaussian() * 0.02D;
                double d2 = this.aa.nextGaussian() * 0.02D;

                this.p.a(s0, this.t + (double) (this.aa.nextFloat() * this.N * 2.0F) - (double) this.N, this.u + 0.5D + (double) (this.aa.nextFloat() * this.O), this.v + (double) (this.aa.nextFloat() * this.N * 2.0F) - (double) this.N, d0, d1, d2);
            }
        }
        else {
            this.bq = 0;
        }

    }

    protected void a(Entity entity, float f0) {
        if (entity instanceof EntityPlayer) {
            if (f0 < 3.0F) {
                double d0 = entity.t - this.t;
                double d1 = entity.v - this.v;

                this.z = (float) (Math.atan2(d1, d0) * 180.0D / 3.1415927410125732D) - 90.0F;
                this.bn = true;
            }

            EntityPlayer entityplayer = (EntityPlayer) entity;

            if (entityplayer.bD() == null || !this.c(entityplayer.bD())) {
                this.j = null;
            }
        }
        else if (entity instanceof EntityAnimal) {
            EntityAnimal entityanimal = (EntityAnimal) entity;

            if (this.d() > 0 && entityanimal.d() < 0) {
                if ((double) f0 < 2.5D) {
                    this.bn = true;
                }
            }
            else if (this.bp > 0 && entityanimal.bp > 0) {
                if (entityanimal.j == null) {
                    entityanimal.j = this;
                }

                if (entityanimal.j == this && (double) f0 < 3.5D) {
                    ++entityanimal.bp;
                    ++this.bp;
                    ++this.bq;
                    if (this.bq % 4 == 0) {
                        this.p.a("heart", this.t + (double) (this.aa.nextFloat() * this.N * 2.0F) - (double) this.N, this.u + 0.5D + (double) (this.aa.nextFloat() * this.O), this.v + (double) (this.aa.nextFloat() * this.N * 2.0F) - (double) this.N, 0.0D, 0.0D, 0.0D);
                    }

                    if (this.bq == 60) {
                        this.b((EntityAnimal) entity);
                    }
                }
                else {
                    this.bq = 0;
                }
            }
            else {
                this.bq = 0;
                this.j = null;
            }
        }

    }

    private void b(EntityAnimal entityanimal) {
        EntityAgeable entityageable = this.a((EntityAgeable) entityanimal);

        if (entityageable != null) {
            if (this.br == null && entityanimal.cb() != null) {
                this.br = entityanimal.cb();
            }

            if (this.br != null) {
                this.br.a(StatList.x);
                if (this instanceof EntityCow) {
                    this.br.a((StatBase) AchievementList.H);
                }
            }

            this.c(6000);
            entityanimal.c(6000);
            this.bp = 0;
            this.bq = 0;
            this.j = null;
            entityanimal.j = null;
            entityanimal.bq = 0;
            entityanimal.bp = 0;
            entityageable.c(-24000);
            entityageable.b(this.t, this.u, this.v, this.z, this.A);

            for (int i0 = 0; i0 < 7; ++i0) {
                double d0 = this.aa.nextGaussian() * 0.02D;
                double d1 = this.aa.nextGaussian() * 0.02D;
                double d2 = this.aa.nextGaussian() * 0.02D;

                this.p.a("heart", this.t + (double) (this.aa.nextFloat() * this.N * 2.0F) - (double) this.N, this.u + 0.5D + (double) (this.aa.nextFloat() * this.O), this.v + (double) (this.aa.nextFloat() * this.N * 2.0F) - (double) this.N, d0, d1, d2);
            }

            this.p.d((Entity) entityageable);
        }

    }

    public boolean a(DamageSource damagesource, float f0) {
        if (this.aw()) {
            return false;
        }
        else {
            this.bo = 60;
            if (!this.bk()) {
                AttributeInstance attributeinstance = this.a(SharedMonsterAttributes.d);

                if (attributeinstance.a(h) == null) {
                    attributeinstance.a(i);
                }
            }

            this.j = null;
            this.bp = 0;
            return super.a(damagesource, f0);
        }
    }

    public float a(int i0, int i1, int i2) {
        return this.p.a(i0, i1 - 1, i2) == Blocks.c ? 10.0F : this.p.n(i0, i1, i2) - 0.5F;
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.a("InLove", this.bp);
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.bp = nbttagcompound.f("InLove");
    }

    protected Entity bP() {
        if (this.bo > 0) {
            return null;
        }
        else {
            float f0 = 8.0F;
            List list;
            int i0;
            EntityAnimal entityanimal;

            if (this.bp > 0) {
                list = this.p.a(this.getClass(), this.D.b((double) f0, (double) f0, (double) f0));

                for (i0 = 0; i0 < list.size(); ++i0) {
                    entityanimal = (EntityAnimal) list.get(i0);
                    if (entityanimal != this && entityanimal.bp > 0) {
                        return entityanimal;
                    }
                }
            }
            else if (this.d() == 0) {
                list = this.p.a(EntityPlayer.class, this.D.b((double) f0, (double) f0, (double) f0));

                for (i0 = 0; i0 < list.size(); ++i0) {
                    EntityPlayer entityplayer = (EntityPlayer) list.get(i0);

                    if (entityplayer.bD() != null && this.c(entityplayer.bD())) {
                        return entityplayer;
                    }
                }
            }
            else if (this.d() > 0) {
                list = this.p.a(this.getClass(), this.D.b((double) f0, (double) f0, (double) f0));

                for (i0 = 0; i0 < list.size(); ++i0) {
                    entityanimal = (EntityAnimal) list.get(i0);
                    if (entityanimal != this && entityanimal.d() < 0) {
                        return entityanimal;
                    }
                }
            }

            return null;
        }
    }

    public boolean bw() {
        int i0 = MathHelper.c(this.t);
        int i1 = MathHelper.c(this.D.b);
        int i2 = MathHelper.c(this.v);

        return this.p.a(i0, i1 - 1, i2) == Blocks.c && this.p.j(i0, i1, i2) > 8 && super.bw();
    }

    public int q() {
        return 120;
    }

    protected boolean v() {
        return false;
    }

    protected int e(EntityPlayer entityplayer) {
        return 1 + this.p.s.nextInt(3);
    }

    public boolean c(ItemStack itemstack) {
        return itemstack.b() == Items.O;
    }

    public boolean a(EntityPlayer entityplayer) {
        ItemStack itemstack = entityplayer.bn.h();

        if (itemstack != null && this.c(itemstack) && this.d() == 0 && this.bp <= 0) {
            if (!entityplayer.bF.d) {
                --itemstack.b;
                if (itemstack.b <= 0) {
                    entityplayer.bn.a(entityplayer.bn.c, (ItemStack) null);
                }
            }

            this.f(entityplayer);
            return true;
        }
        else {
            return super.a(entityplayer);
        }
    }

    public void f(EntityPlayer entityplayer) {
        this.bp = 600;
        this.br = entityplayer;
        this.j = null;
        this.p.a(this, (byte) 18);
    }

    public EntityPlayer cb() {
        return this.br;
    }

    public boolean cc() {
        return this.bp > 0;
    }

    public void cd() {
        this.bp = 0;
    }

    public boolean a(EntityAnimal entityanimal) {
        return entityanimal == this ? false : (entityanimal.getClass() != this.getClass() ? false : this.cc() && entityanimal.cc());
    }
}

