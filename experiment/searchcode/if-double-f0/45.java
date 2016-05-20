package net.minecraft.entity.item;

import net.canarymod.api.entity.CanaryEntityItem;
import net.canarymod.hook.entity.EntityDespawnHook;
import net.canarymod.hook.entity.ItemTouchGroundHook;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;

public class EntityItem extends Entity {

    private static final Logger d = LogManager.getLogger();
    public int a;
    public int b;
    public int e; // CanaryMod: private => public; health
    private String f;
    private String g;
    public float c;

    public EntityItem(World world, double d0, double d1, double d2) {
        super(world);
        this.e = 5;
        this.c = (float) (Math.random() * 3.141592653589793D * 2.0D);
        this.a(0.25F, 0.25F);
        this.M = this.O / 2.0F;
        this.b(d0, d1, d2);
        this.z = (float) (Math.random() * 360.0D);
        this.w = (double) ((float) (Math.random() * 0.20000000298023224D - 0.10000000149011612D));
        this.x = 0.20000000298023224D;
        this.y = (double) ((float) (Math.random() * 0.20000000298023224D - 0.10000000149011612D));
        this.entity = new CanaryEntityItem(this); // CanaryMod: Wrap Entity
    }

    public EntityItem(World world, double d0, double d1, double d2, ItemStack itemstack) {
        this(world, d0, d1, d2);
        this.a(itemstack);
    }

    protected boolean g_() {
        return false;
    }

    public EntityItem(World world) {
        super(world);
        this.e = 5;
        this.c = (float) (Math.random() * 3.141592653589793D * 2.0D);
        this.a(0.25F, 0.25F);
        this.M = this.O / 2.0F;
        this.entity = new CanaryEntityItem(this); // CanaryMod: Wrap Entity
    }

    protected void c() {
        this.z().a(10, 5);
    }

    public void h() {
        if (this.f() == null) {
            this.B();
        }
        else {
            super.h();
            if (this.b > 0) {
                --this.b;
            }

            boolean tmpTouch = this.I; // CanaryMod

            this.q = this.t;
            this.r = this.u;
            this.s = this.v;
            this.x -= 0.03999999910593033D;
            this.Y = this.j(this.t, (this.D.b + this.D.e) / 2.0D, this.v);
            this.d(this.w, this.x, this.y);
            boolean flag0 = (int) this.q != (int) this.t || (int) this.r != (int) this.u || (int) this.s != (int) this.v;

            if (flag0 || this.ab % 25 == 0) {
                if (this.p.a(MathHelper.c(this.t), MathHelper.c(this.u), MathHelper.c(this.v)).o() == Material.i) {
                    this.x = 0.20000000298023224D;
                    this.w = (double) ((this.aa.nextFloat() - this.aa.nextFloat()) * 0.2F);
                    this.y = (double) ((this.aa.nextFloat() - this.aa.nextFloat()) * 0.2F);
                    this.a("random.fizz", 0.4F, 2.0F + this.aa.nextFloat() * 0.4F);
                }

                if (!this.p.E) {
                    this.k();
                }
            }

            float f0 = 0.98F;

            if (this.E) {
                f0 = this.p.a(MathHelper.c(this.t), MathHelper.c(this.D.b) - 1, MathHelper.c(this.v)).K * 0.98F;
            }

            // CanaryMod: ItemTouchGround
            // It does touch the ground now, but didn't in last tick
            if (!tmpTouch) {
                ItemTouchGroundHook hook = (ItemTouchGroundHook) new ItemTouchGroundHook((net.canarymod.api.entity.EntityItem) getCanaryEntity()).call();
                if (hook.isCanceled()) {
                    this.B(); // kill the item
                }
            }
            //

            this.w *= (double) f0;
            this.x *= 0.9800000190734863D;
            this.y *= (double) f0;
            if (this.E) {
                this.x *= -0.5D;
            }

            ++this.a;
            if (!this.p.E && this.a >= 6000) {
                // CanaryMod: EntityDespawn
                EntityDespawnHook hook = (EntityDespawnHook) new EntityDespawnHook(getCanaryEntity()).call();
                if (!hook.isCanceled()) {
                    this.B();
                }
                else {
                    this.a = 0; // Reset Age
                }
                //
            }
        }
    }

    private void k() {
        Iterator iterator = this.p.a(EntityItem.class, this.D.b(0.5D, 0.0D, 0.5D)).iterator();

        while (iterator.hasNext()) {
            EntityItem entityitem = (EntityItem) iterator.next();

            this.a(entityitem);
        }
    }

    public boolean a(EntityItem entityitem) {
        if (entityitem == this) {
            return false;
        }
        else if (entityitem.Z() && this.Z()) {
            ItemStack itemstack = this.f();
            ItemStack itemstack1 = entityitem.f();

            if (itemstack1.b() != itemstack.b()) {
                return false;
            }
            else if (itemstack1.p() ^ itemstack.p()) {
                return false;
            }
            else if (itemstack1.p() && !itemstack1.q().equals(itemstack.q())) {
                return false;
            }
            else if (itemstack1.b() == null) {
                return false;
            }
            else if (itemstack1.b().n() && itemstack1.k() != itemstack.k()) {
                return false;
            }
            else if (itemstack1.b < itemstack.b) {
                return entityitem.a(this);
            }
            else if (itemstack1.b + itemstack.b > itemstack1.e()) {
                return false;
            }
            else {
                itemstack1.b += itemstack.b;
                entityitem.b = Math.max(entityitem.b, this.b);
                entityitem.a = Math.min(entityitem.a, this.a);
                entityitem.a(itemstack1);
                this.B();
                return true;
            }
        }
        else {
            return false;
        }
    }

    public void e() {
        this.a = 4800;
    }

    public boolean N() {
        return this.p.a(this.D, Material.h, (Entity) this);
    }

    protected void f(int i0) {
        this.a(DamageSource.a, (float) i0);
    }

    public boolean a(DamageSource damagesource, float f0) {
        if (this.aw()) {
            return false;
        }
        else if (this.f() != null && this.f().b() == Items.bN && damagesource.c()) {
            return false;
        }
        else {
            this.Q();
            this.e = (int) ((float) this.e - f0);
            if (this.e <= 0) {
                this.B();
            }

            return false;
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.a("Health", (short) ((byte) this.e));
        nbttagcompound.a("Age", (short) this.a);
        if (this.j() != null) {
            nbttagcompound.a("Thrower", this.f);
        }
        if (this.i() != null) {
            nbttagcompound.a("Owner", this.g);
        }

        if (this.f() != null) {
            nbttagcompound.a("Item", (NBTBase) this.f().b(new NBTTagCompound()));
        }
    }

    public void a(NBTTagCompound nbttagcompound) {
        this.e = nbttagcompound.e("Health") & 255;
        this.a = nbttagcompound.e("Age");
        if (nbttagcompound.c("Owner")) {
            this.g = nbttagcompound.j("Owner");
        }

        if (nbttagcompound.c("Thrower")) {
            this.f = nbttagcompound.j("Thrower");
        }

        NBTTagCompound nbttagcompound1 = nbttagcompound.m("Item");

        this.a(ItemStack.a(nbttagcompound1));
        if (this.f() == null) {
            this.B();
        }
    }

    public void b_(EntityPlayer entityplayer) {
        if (!this.p.E) {
            ItemStack itemstack = this.f();
            int i0 = itemstack.b;

            if (this.b == 0 &&
                    (this.g == null || 6000 - this.a <= 200 || this.g.equals(entityplayer.b_())) && entityplayer.bn.canPickup(this)) { // CanaryMod: simulate pickup first
                if (entityplayer.bn.a(itemstack)) {
                    if (itemstack.b() == Item.a(Blocks.r)) {
                        entityplayer.a((StatBase) AchievementList.g);
                    }

                    if (itemstack.b() == Item.a(Blocks.s)) {
                        entityplayer.a((StatBase) AchievementList.g);
                    }

                    if (itemstack.b() == Items.aA) {
                        entityplayer.a((StatBase) AchievementList.t);
                    }

                    if (itemstack.b() == Items.i) {
                        entityplayer.a((StatBase) AchievementList.w);
                    }

                    if (itemstack.b() == Items.bj) {
                        entityplayer.a((StatBase) AchievementList.A);
                    }

                    if (itemstack.b() == Items.i && this.j() != null) {
                        EntityPlayer entityplayer1 = this.p.a(this.j());

                        if (entityplayer1 != null && entityplayer1 != entityplayer) {
                            entityplayer1.a((StatBase) AchievementList.x);
                        }
                    }

                    this.p.a((Entity) entityplayer, "random.pop", 0.2F, ((this.aa.nextFloat() - this.aa.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    entityplayer.a((Entity) this, i0);
                    if (itemstack.b <= 0) {
                        this.B();
                    }
                }
            }
        }
    }

    public String b_() {
        return StatCollector.a("item." + this.f().a());
    }

    public boolean av() {
        return false;
    }

    public void b(int i0) {
        super.b(i0);
        if (!this.p.E) {
            this.k();
        }
    }

    public ItemStack f() {
        ItemStack itemstack = this.z().f(10);

        if (itemstack == null) {
            if (this.p != null) {
                d.error("Item entity " + this.y() + " has no item?!");
            }

            return new ItemStack(Blocks.b);
        }
        else {
            return itemstack;
        }
    }

    public void a(ItemStack itemstack) {
        this.z().b(10, itemstack);
        this.z().h(10);
    }

    public String i() {
        return this.g;
    }

    public void a(String s0) {
        this.g = s0;
    }

    public String j() {
        return this.f;
    }

    public void b(String s0) {
        this.f = s0;
    }

    public CanaryEntityItem getEntityItem() {
        return (CanaryEntityItem) entity;
    }
}

