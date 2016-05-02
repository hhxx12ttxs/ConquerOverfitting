package net.minecraft.tileentity;

import net.canarymod.api.world.blocks.CanaryBrewingStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionHelper;

import java.util.List;

public class TileEntityBrewingStand extends TileEntity implements ISidedInventory {

    private static final int[] a = new int[]{ 3 };
    private static final int[] i = new int[]{ 0, 1, 2 };
    public ItemStack[] j = new ItemStack[4]; // CanaryMod: private => public
    private int k;
    private int l;
    private Item m;
    private String n;

    public TileEntityBrewingStand() {
        this.complexBlock = new CanaryBrewingStand(this); // CanaryMod: create once, use forever
    }

    public String b() {
        return this.k_() ? this.n : "container.brewing";
    }

    public boolean k_() {
        return this.n != null && this.n.length() > 0;
    }

    public void a(String s0) {
        this.n = s0;
    }

    public int a() {
        return this.j.length;
    }

    public void h() {
        if (this.k > 0) {
            --this.k;
            if (this.k == 0) {
                this.l();
                this.e();
            }
            else if (!this.k()) {
                this.k = 0;
                this.e();
            }
            else if (this.m != this.j[3].b()) {
                this.k = 0;
                this.e();
            }
        }
        else if (this.k()) {
            this.k = 400;
            this.m = this.j[3].b();
        }

        int i0 = this.j();

        if (i0 != this.l) {
            this.l = i0;
            this.b.a(this.c, this.d, this.e, i0, 2);
        }

        super.h();
    }

    public int i() {
        return this.k;
    }

    private boolean k() {
        if (this.j[3] != null && this.j[3].b > 0) {
            ItemStack itemstack = this.j[3];

            if (!itemstack.b().m(itemstack)) {
                return false;
            }
            else {
                boolean flag0 = false;

                for (int i0 = 0; i0 < 3; ++i0) {
                    if (this.j[i0] != null && this.j[i0].b() == Items.bn) {
                        int i1 = this.j[i0].k();
                        int i2 = this.c(i1, itemstack);

                        if (!ItemPotion.g(i1) && ItemPotion.g(i2)) {
                            flag0 = true;
                            break;
                        }

                        List list = Items.bn.c(i1);
                        List list1 = Items.bn.c(i2);

                        if ((i1 <= 0 || list != list1) && (list == null || !list.equals(list1) && list1 != null) && i1 != i2) {
                            flag0 = true;
                            break;
                        }
                    }
                }

                return flag0;
            }
        }
        else {
            return false;
        }
    }

    private void l() {
        if (this.k()) {
            ItemStack itemstack = this.j[3];

            for (int i0 = 0; i0 < 3; ++i0) {
                if (this.j[i0] != null && this.j[i0].b() == Items.bn) {
                    int i1 = this.j[i0].k();
                    int i2 = this.c(i1, itemstack);
                    List list = Items.bn.c(i1);
                    List list1 = Items.bn.c(i2);

                    if ((i1 <= 0 || list != list1) && (list == null || !list.equals(list1) && list1 != null)) {
                        if (i1 != i2) {
                            this.j[i0].b(i2);
                        }
                    }
                    else if (!ItemPotion.g(i1) && ItemPotion.g(i2)) {
                        this.j[i0].b(i2);
                    }
                }
            }

            if (itemstack.b().u()) {
                this.j[3] = new ItemStack(itemstack.b().t());
            }
            else {
                --this.j[3].b;
                if (this.j[3].b <= 0) {
                    this.j[3] = null;
                }
            }
        }
    }

    private int c(int i0, ItemStack itemstack) {
        return itemstack == null ? i0 : (itemstack.b().m(itemstack) ? PotionHelper.a(i0, itemstack.b().i(itemstack)) : i0);
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        NBTTagList nbttaglist = nbttagcompound.c("Items", 10);

        this.j = new ItemStack[this.a()];

        for (int i0 = 0; i0 < nbttaglist.c(); ++i0) {
            NBTTagCompound nbttagcompound1 = nbttaglist.b(i0);
            byte b0 = nbttagcompound1.d("Slot");

            if (b0 >= 0 && b0 < this.j.length) {
                this.j[b0] = ItemStack.a(nbttagcompound1);
            }
        }

        this.k = nbttagcompound.e("BrewTime");
        if (nbttagcompound.b("CustomName", 8)) {
            this.n = nbttagcompound.j("CustomName");
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.a("BrewTime", (short) this.k);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i0 = 0; i0 < this.j.length; ++i0) {
            if (this.j[i0] != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();

                nbttagcompound1.a("Slot", (byte) i0);
                this.j[i0].b(nbttagcompound1);
                nbttaglist.a((NBTBase) nbttagcompound1);
            }
        }

        nbttagcompound.a("Items", (NBTBase) nbttaglist);
        if (this.k_()) {
            nbttagcompound.a("CustomName", this.n);
        }
    }

    public ItemStack a(int i0) {
        return i0 >= 0 && i0 < this.j.length ? this.j[i0] : null;
    }

    public ItemStack a(int i0, int i1) {
        if (i0 >= 0 && i0 < this.j.length) {
            ItemStack itemstack = this.j[i0];

            this.j[i0] = null;
            return itemstack;
        }
        else {
            return null;
        }
    }

    public ItemStack a_(int i0) {
        if (i0 >= 0 && i0 < this.j.length) {
            ItemStack itemstack = this.j[i0];

            this.j[i0] = null;
            return itemstack;
        }
        else {
            return null;
        }
    }

    public void a(int i0, ItemStack itemstack) {
        if (i0 >= 0 && i0 < this.j.length) {
            this.j[i0] = itemstack;
        }
    }

    public int d() {
        return 64;
    }

    public boolean a(EntityPlayer entityplayer) {
        return this.b.o(this.c, this.d, this.e) != this ? false : entityplayer.e((double) this.c + 0.5D, (double) this.d + 0.5D, (double) this.e + 0.5D) <= 64.0D;
    }

    public void f() {
    }

    public void l_() {
    }

    public boolean b(int i0, ItemStack itemstack) {
        return i0 == 3 ? itemstack.b().m(itemstack) : itemstack.b() == Items.bn || itemstack.b() == Items.bo;
    }

    public int j() {
        int i0 = 0;

        for (int i1 = 0; i1 < 3; ++i1) {
            if (this.j[i1] != null) {
                i0 |= 1 << i1;
            }
        }

        return i0;
    }

    public int[] c(int i0) {
        return i0 == 1 ? a : i;
    }

    public boolean a(int i0, ItemStack itemstack, int i1) {
        return this.b(i0, itemstack);
    }

    public boolean b(int i0, ItemStack itemstack, int i1) {
        return true;
    }

    // CanaryMod
    public CanaryBrewingStand getCanaryBrewingStand() {
        return (CanaryBrewingStand) complexBlock;
    }
}

