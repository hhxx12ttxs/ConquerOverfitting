package net.minecraft.entity.item;


import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public abstract class EntityMinecartContainer extends EntityMinecart implements IInventory {

    public ItemStack[] a = new ItemStack[36]; // CanaryMod: private -> public
    private boolean b = true;

    public EntityMinecartContainer(World world) {
        super(world);
    }

    public EntityMinecartContainer(World world, double d0, double d1, double d2) {
        super(world, d0, d1, d2);
    }

    public void a(DamageSource damagesource) {
        super.a(damagesource);

        for (int i0 = 0; i0 < this.a(); ++i0) {
            ItemStack itemstack = this.a(i0);

            if (itemstack != null) {
                float f0 = this.aa.nextFloat() * 0.8F + 0.1F;
                float f1 = this.aa.nextFloat() * 0.8F + 0.1F;
                float f2 = this.aa.nextFloat() * 0.8F + 0.1F;

                while (itemstack.b > 0) {
                    int i1 = this.aa.nextInt(21) + 10;

                    if (i1 > itemstack.b) {
                        i1 = itemstack.b;
                    }

                    itemstack.b -= i1;
                    EntityItem entityitem = new EntityItem(this.p, this.t + (double) f0, this.u + (double) f1, this.v + (double) f2, new ItemStack(itemstack.b(), i1, itemstack.k()));
                    float f3 = 0.05F;

                    entityitem.w = (double) ((float) this.aa.nextGaussian() * f3);
                    entityitem.x = (double) ((float) this.aa.nextGaussian() * f3 + 0.2F);
                    entityitem.y = (double) ((float) this.aa.nextGaussian() * f3);
                    this.p.d((Entity) entityitem);
                }
            }
        }
    }

    public ItemStack a(int i0) {
        return this.a[i0];
    }

    public ItemStack a(int i0, int i1) {
        if (this.a[i0] != null) {
            ItemStack itemstack;

            if (this.a[i0].b <= i1) {
                itemstack = this.a[i0];
                this.a[i0] = null;
                return itemstack;
            }
            else {
                itemstack = this.a[i0].a(i1);
                if (this.a[i0].b == 0) {
                    this.a[i0] = null;
                }

                return itemstack;
            }
        }
        else {
            return null;
        }
    }

    public ItemStack a_(int i0) {
        if (this.a[i0] != null) {
            ItemStack itemstack = this.a[i0];

            this.a[i0] = null;
            return itemstack;
        }
        else {
            return null;
        }
    }

    public void a(int i0, ItemStack itemstack) {
        this.a[i0] = itemstack;
        if (itemstack != null && itemstack.b > this.d()) {
            itemstack.b = this.d();
        }
    }

    public void e() {
    }

    public boolean a(EntityPlayer entityplayer) {
        return this.L ? false : entityplayer.e(this) <= 64.0D;
    }

    public void f() {
    }

    public void l_() {
    }

    public boolean b(int i0, ItemStack itemstack) {
        return true;
    }

    public String b() {
        return this.k_() ? this.u() : "container.minecart";
    }

    public int d() {
        return 64;
    }

    public void b(int i0) {
        this.b = false;
        super.b(i0);
    }

    public void B() {
        if (this.b) {
            for (int i0 = 0; i0 < this.a(); ++i0) {
                ItemStack itemstack = this.a(i0);

                if (itemstack != null) {
                    float f0 = this.aa.nextFloat() * 0.8F + 0.1F;
                    float f1 = this.aa.nextFloat() * 0.8F + 0.1F;
                    float f2 = this.aa.nextFloat() * 0.8F + 0.1F;

                    while (itemstack.b > 0) {
                        int i1 = this.aa.nextInt(21) + 10;

                        if (i1 > itemstack.b) {
                            i1 = itemstack.b;
                        }

                        itemstack.b -= i1;
                        EntityItem entityitem = new EntityItem(this.p, this.t + (double) f0, this.u + (double) f1, this.v + (double) f2, new ItemStack(itemstack.b(), i1, itemstack.k()));

                        if (itemstack.p()) {
                            entityitem.f().d((NBTTagCompound) itemstack.q().b());
                        }

                        float f3 = 0.05F;

                        entityitem.w = (double) ((float) this.aa.nextGaussian() * f3);
                        entityitem.x = (double) ((float) this.aa.nextGaussian() * f3 + 0.2F);
                        entityitem.y = (double) ((float) this.aa.nextGaussian() * f3);
                        this.p.d((Entity) entityitem);
                    }
                }
            }
        }

        super.B();
    }

    protected void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i0 = 0; i0 < this.a.length; ++i0) {
            if (this.a[i0] != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();

                nbttagcompound1.a("Slot", (byte) i0);
                this.a[i0].b(nbttagcompound1);
                nbttaglist.a((NBTBase) nbttagcompound1);
            }
        }

        nbttagcompound.a("Items", (NBTBase) nbttaglist);
    }

    protected void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        NBTTagList nbttaglist = nbttagcompound.c("Items", 10);

        this.a = new ItemStack[this.a()];

        for (int i0 = 0; i0 < nbttaglist.c(); ++i0) {
            NBTTagCompound nbttagcompound1 = nbttaglist.b(i0);
            int i1 = nbttagcompound1.d("Slot") & 255;

            if (i1 >= 0 && i1 < this.a.length) {
                this.a[i1] = ItemStack.a(nbttagcompound1);
            }
        }
    }

    public boolean c(EntityPlayer entityplayer) {
        if (!this.p.E) {
            entityplayer.a((IInventory) this);
        }

        return true;
    }

    protected void i() {
        int i0 = 15 - Container.b((IInventory) this);
        float f0 = 0.98F + (float) i0 * 0.001F;

        this.w *= (double) f0;
        this.x *= 0.0D;
        this.y *= (double) f0;
    }
}

