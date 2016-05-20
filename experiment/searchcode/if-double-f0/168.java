package net.minecraft.tileentity;

import net.canarymod.api.world.blocks.CanaryChest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;

import java.util.Iterator;
import java.util.List;

public class TileEntityChest extends TileEntity implements IInventory {

    public ItemStack[] p = new ItemStack[36]; // CanaryMod: private => public
    public boolean a;
    public TileEntityChest i;
    public TileEntityChest j;
    public TileEntityChest k;
    public TileEntityChest l;
    public float m;
    public float n;
    public int o;
    private int q;
    private int r = -1;
    private String s;

    public TileEntityChest() {
        this.complexBlock = new CanaryChest(this); // CanaryMod: create once, use forever
    }

    public int a() {
        return 27;
    }

    public ItemStack a(int i0) {
        return this.p[i0];
    }

    public ItemStack a(int i0, int i1) {
        if (this.p[i0] != null) {
            ItemStack itemstack;

            if (this.p[i0].b <= i1) {
                itemstack = this.p[i0];
                this.p[i0] = null;
                this.e();
                return itemstack;
            }
            else {
                itemstack = this.p[i0].a(i1);
                if (this.p[i0].b == 0) {
                    this.p[i0] = null;
                }

                this.e();
                return itemstack;
            }
        }
        else {
            return null;
        }
    }

    public ItemStack a_(int i0) {
        if (this.p[i0] != null) {
            ItemStack itemstack = this.p[i0];

            this.p[i0] = null;
            return itemstack;
        }
        else {
            return null;
        }
    }

    public void a(int i0, ItemStack itemstack) {
        this.p[i0] = itemstack;
        if (itemstack != null && itemstack.b > this.d()) {
            itemstack.b = this.d();
        }

        this.e();
    }

    public String b() {
        return this.k_() ? this.s : "container.chest";
    }

    public boolean k_() {
        return this.s != null && this.s.length() > 0;
    }

    public void a(String s0) {
        this.s = s0;
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        NBTTagList nbttaglist = nbttagcompound.c("Items", 10);

        this.p = new ItemStack[this.a()];
        if (nbttagcompound.b("CustomName", 8)) {
            this.s = nbttagcompound.j("CustomName");
        }

        for (int i0 = 0; i0 < nbttaglist.c(); ++i0) {
            NBTTagCompound nbttagcompound1 = nbttaglist.b(i0);
            int i1 = nbttagcompound1.d("Slot") & 255;

            if (i1 >= 0 && i1 < this.p.length) {
                this.p[i1] = ItemStack.a(nbttagcompound1);
            }
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i0 = 0; i0 < this.p.length; ++i0) {
            if (this.p[i0] != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();

                nbttagcompound1.a("Slot", (byte) i0);
                this.p[i0].b(nbttagcompound1);
                nbttaglist.a((NBTBase) nbttagcompound1);
            }
        }

        nbttagcompound.a("Items", (NBTBase) nbttaglist);
        if (this.k_()) {
            nbttagcompound.a("CustomName", this.s);
        }
    }

    public int d() {
        return 64;
    }

    public boolean a(EntityPlayer entityplayer) {
        return this.b.o(this.c, this.d, this.e) != this ? false : entityplayer.e((double) this.c + 0.5D, (double) this.d + 0.5D, (double) this.e + 0.5D) <= 64.0D;
    }

    public void u() {
        super.u();
        this.a = false;
    }

    private void a(TileEntityChest tileentitychest, int i0) {
        if (tileentitychest.r()) {
            this.a = false;
        }
        else if (this.a) {
            switch (i0) {
                case 0:
                    if (this.l != tileentitychest) {
                        this.a = false;
                    }
                    break;

                case 1:
                    if (this.k != tileentitychest) {
                        this.a = false;
                    }
                    break;

                case 2:
                    if (this.i != tileentitychest) {
                        this.a = false;
                    }
                    break;

                case 3:
                    if (this.j != tileentitychest) {
                        this.a = false;
                    }
            }
        }
    }

    public void i() {
        if (!this.a) {
            this.a = true;
            this.i = null;
            this.j = null;
            this.k = null;
            this.l = null;
            if (this.a(this.c - 1, this.d, this.e)) {
                this.k = (TileEntityChest) this.b.o(this.c - 1, this.d, this.e);
            }

            if (this.a(this.c + 1, this.d, this.e)) {
                this.j = (TileEntityChest) this.b.o(this.c + 1, this.d, this.e);
            }

            if (this.a(this.c, this.d, this.e - 1)) {
                this.i = (TileEntityChest) this.b.o(this.c, this.d, this.e - 1);
            }

            if (this.a(this.c, this.d, this.e + 1)) {
                this.l = (TileEntityChest) this.b.o(this.c, this.d, this.e + 1);
            }

            if (this.i != null) {
                this.i.a(this, 0);
            }

            if (this.l != null) {
                this.l.a(this, 2);
            }

            if (this.j != null) {
                this.j.a(this, 1);
            }

            if (this.k != null) {
                this.k.a(this, 3);
            }
        }
    }

    private boolean a(int i0, int i1, int i2) {
        Block block = this.b.a(i0, i1, i2);

        return block instanceof BlockChest && ((BlockChest) block).a == this.j();
    }

    public void h() {
        super.h();
        this.i();
        ++this.q;
        float f0;

        if (!this.b.E && this.o != 0 && (this.q + this.c + this.d + this.e) % 200 == 0) {
            this.o = 0;
            f0 = 5.0F;
            List list = this.b.a(EntityPlayer.class, AxisAlignedBB.a().a((double) ((float) this.c - f0), (double) ((float) this.d - f0), (double) ((float) this.e - f0), (double) ((float) (this.c + 1) + f0), (double) ((float) (this.d + 1) + f0), (double) ((float) (this.e + 1) + f0)));
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                EntityPlayer entityplayer = (EntityPlayer) iterator.next();

                if (entityplayer.bp instanceof ContainerChest) {
                    IInventory iinventory = ((ContainerChest) entityplayer.bp).e();

                    if (iinventory == this || iinventory instanceof InventoryLargeChest && ((InventoryLargeChest) iinventory).a((IInventory) this)) {
                        ++this.o;
                    }
                }
            }
        }

        this.n = this.m;
        f0 = 0.1F;
        double d0;

        if (this.o > 0 && this.m == 0.0F && this.i == null && this.k == null) {
            double d1 = (double) this.c + 0.5D;

            d0 = (double) this.e + 0.5D;
            if (this.l != null) {
                d0 += 0.5D;
            }

            if (this.j != null) {
                d1 += 0.5D;
            }

            this.b.a(d1, (double) this.d + 0.5D, d0, "random.chestopen", 0.5F, this.b.s.nextFloat() * 0.1F + 0.9F);
        }

        if (this.o == 0 && this.m > 0.0F || this.o > 0 && this.m < 1.0F) {
            float f1 = this.m;

            if (this.o > 0) {
                this.m += f0;
            }
            else {
                this.m -= f0;
            }

            if (this.m > 1.0F) {
                this.m = 1.0F;
            }

            float f2 = 0.5F;

            if (this.m < f2 && f1 >= f2 && this.i == null && this.k == null) {
                d0 = (double) this.c + 0.5D;
                double d2 = (double) this.e + 0.5D;

                if (this.l != null) {
                    d2 += 0.5D;
                }

                if (this.j != null) {
                    d0 += 0.5D;
                }

                this.b.a(d0, (double) this.d + 0.5D, d2, "random.chestclosed", 0.5F, this.b.s.nextFloat() * 0.1F + 0.9F);
            }

            if (this.m < 0.0F) {
                this.m = 0.0F;
            }
        }
    }

    public boolean c(int i0, int i1) {
        if (i0 == 1) {
            this.o = i1;
            return true;
        }
        else {
            return super.c(i0, i1);
        }
    }

    public void f() {
        if (this.o < 0) {
            this.o = 0;
        }

        ++this.o;
        this.b.c(this.c, this.d, this.e, this.q(), 1, this.o);
        this.b.d(this.c, this.d, this.e, this.q());
        this.b.d(this.c, this.d - 1, this.e, this.q());
    }

    public void l_() {
        if (this.q() instanceof BlockChest) {
            --this.o;
            this.b.c(this.c, this.d, this.e, this.q(), 1, this.o);
            this.b.d(this.c, this.d, this.e, this.q());
            this.b.d(this.c, this.d - 1, this.e, this.q());
        }
    }

    public boolean b(int i0, ItemStack itemstack) {
        return true;
    }

    public void s() {
        super.s();
        this.u();
        this.i();
    }

    public int j() {
        if (this.r == -1) {
            if (this.b == null || !(this.q() instanceof BlockChest)) {
                return 0;
            }

            this.r = ((BlockChest) this.q()).a;
        }

        return this.r;
    }

    // CanaryMod
    public CanaryChest getCanaryChest() {
        return (CanaryChest) complexBlock;
    }
}

