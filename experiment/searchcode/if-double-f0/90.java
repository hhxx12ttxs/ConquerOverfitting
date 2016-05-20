package net.minecraft.entity.item;

import net.canarymod.api.entity.CanaryFireworkRocket;
import net.canarymod.api.entity.FireworkRocket;
import net.canarymod.hook.world.FireworkExplodeHook;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;


public class EntityFireworkRocket extends Entity {

    public int a; // CanaryMod: private => public; life
    public int b; // CanaryMod: private => public; life max

    public EntityFireworkRocket(World world) {
        super(world);
        this.a(0.25F, 0.25F);
        this.entity = new CanaryFireworkRocket(this); // CanaryMod: Wrap Entity
    }

    protected void c() {
        this.ag.a(8, 5);
    }

    public EntityFireworkRocket(World world, double d0, double d1, double d2, ItemStack itemstack) {
        super(world);
        this.a = 0;
        this.a(0.25F, 0.25F);
        this.b(d0, d1, d2);
        this.M = 0.0F;
        int i0 = 1;

        if (itemstack != null && itemstack.p()) {
            this.ag.b(8, itemstack);
            NBTTagCompound nbttagcompound = itemstack.q();
            NBTTagCompound nbttagcompound1 = nbttagcompound.m("Fireworks");

            if (nbttagcompound1 != null) {
                i0 += nbttagcompound1.d("Flight");
            }
        }

        this.w = this.aa.nextGaussian() * 0.001D;
        this.y = this.aa.nextGaussian() * 0.001D;
        this.x = 0.05D;
        this.b = 10 * i0 + this.aa.nextInt(6) + this.aa.nextInt(7);
        this.entity = new CanaryFireworkRocket(this); // CanaryMod: Wrap Entity
    }

    public void h() {
        this.T = this.t;
        this.U = this.u;
        this.V = this.v;
        super.h();
        this.w *= 1.15D;
        this.y *= 1.15D;
        this.x += 0.04D;
        this.d(this.w, this.x, this.y);
        float f0 = MathHelper.a(this.w * this.w + this.y * this.y);

        this.z = (float) (Math.atan2(this.w, this.y) * 180.0D / 3.1415927410125732D);

        for (this.A = (float) (Math.atan2(this.x, (double) f0) * 180.0D / 3.1415927410125732D); this.A - this.C < -180.0F; this.C -= 360.0F) {
            ;
        }

        while (this.A - this.C >= 180.0F) {
            this.C += 360.0F;
        }

        while (this.z - this.B < -180.0F) {
            this.B -= 360.0F;
        }

        while (this.z - this.B >= 180.0F) {
            this.B += 360.0F;
        }

        this.A = this.C + (this.A - this.C) * 0.2F;
        this.z = this.B + (this.z - this.B) * 0.2F;
        if (this.a == 0) {
            this.p.a((Entity) this, "fireworks.launch", 3.0F, 1.0F);
        }

        ++this.a;
        if (this.p.E && this.a % 2 < 2) {
            this.p.a("fireworksSpark", this.t, this.u - 0.3D, this.v, this.aa.nextGaussian() * 0.05D, -this.x * 0.5D, this.aa.nextGaussian() * 0.05D);
        }

        if (!this.p.E && this.a > this.b) {
            // CanaryMod: FireworkExplode
            FireworkExplodeHook hook = (FireworkExplodeHook) new FireworkExplodeHook((FireworkRocket) this.getCanaryEntity()).call();
            if (!hook.isCanceled()) {
                this.p.a(this, (byte) 17);
                this.B();
            }
            //
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.a("Life", this.a);
        nbttagcompound.a("LifeTime", this.b);
        ItemStack itemstack = this.ag.f(8);

        if (itemstack != null) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();

            itemstack.b(nbttagcompound1);
            nbttagcompound.a("FireworksItem", (NBTBase) nbttagcompound1);
        }
    }

    public void a(NBTTagCompound nbttagcompound) {
        this.a = nbttagcompound.f("Life");
        this.b = nbttagcompound.f("LifeTime");
        NBTTagCompound nbttagcompound1 = nbttagcompound.m("FireworksItem");

        if (nbttagcompound1 != null) {
            ItemStack itemstack = ItemStack.a(nbttagcompound1);

            if (itemstack != null) {
                this.ag.b(8, itemstack);
            }
        }
    }

    public float d(float f0) {
        return super.d(f0);
    }

    public boolean av() {
        return false;
    }

    public ItemStack getItemStack() {
        return this.ag.f(8);
    }

    public void setItemStack(ItemStack stack) {
        this.ag.b(8, stack);
        // Update Flight information
        NBTTagCompound nbttagcompound = stack.q();
        NBTTagCompound nbttagcompound1 = nbttagcompound.m("Fireworks");
        if (nbttagcompound1 != null) {
            this.b = 10 * nbttagcompound1.d("Flight") + this.aa.nextInt(6) + this.aa.nextInt(7);
        }
    }
}

