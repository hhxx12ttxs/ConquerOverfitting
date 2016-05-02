package net.minecraft.entity.player;

import net.canarymod.hook.player.ItemPickupHook;
import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ReportedException;

import java.util.concurrent.Callable;

public class InventoryPlayer implements IInventory {

    public ItemStack[] a = new ItemStack[36];
    public ItemStack[] b = new ItemStack[4];
    public int c;
    public EntityPlayer d;
    private ItemStack g;
    public boolean e;
    private String name = "container.inventory"; // CanaryMod: custom inventory name

    public InventoryPlayer(EntityPlayer entityplayer) {
        this.d = entityplayer;
    }

    public ItemStack h() {
        return this.c < 9 && this.c >= 0 ? this.a[this.c] : null;
    }

    public static int i() {
        return 9;
    }

    private int c(Item item) {
        for (int i0 = 0; i0 < this.a.length; ++i0) {
            if (this.a[i0] != null && this.a[i0].b() == item) {
                return i0;
            }
        }

        return -1;
    }

    private int d(ItemStack itemstack) {
        for (int i0 = 0; i0 < this.a.length; ++i0) {
            if (this.a[i0] != null && this.a[i0].b() == itemstack.b() && this.a[i0].f() && this.a[i0].b < this.a[i0].e() && this.a[i0].b < this.d() && (!this.a[i0].h() || this.a[i0].k() == itemstack.k()) && ItemStack.a(this.a[i0], itemstack)) {
                return i0;
            }
        }

        return -1;
    }

    public int j() {
        for (int i0 = 0; i0 < this.a.length; ++i0) {
            if (this.a[i0] == null) {
                return i0;
            }
        }

        return -1;
    }

    public int a(Item item, int i0) {
        int i1 = 0;

        int i2;
        ItemStack itemstack;

        for (i2 = 0; i2 < this.a.length; ++i2) {
            itemstack = this.a[i2];
            if (itemstack != null && (item == null || itemstack.b() == item) && (i0 <= -1 || itemstack.k() == i0)) {
                i1 += itemstack.b;
                this.a[i2] = null;
            }
        }

        for (i2 = 0; i2 < this.b.length; ++i2) {
            itemstack = this.b[i2];
            if (itemstack != null && (item == null || itemstack.b() == item) && (i0 <= -1 || itemstack.k() == i0)) {
                i1 += itemstack.b;
                this.b[i2] = null;
            }
        }

        if (this.g != null) {
            if (item != null && this.g.b() != item) {
                return i1;
            }

            if (i0 > -1 && this.g.k() != i0) {
                return i1;
            }

            i1 += this.g.b;
            this.b((ItemStack) null);
        }

        return i1;
    }

    private int e(ItemStack itemstack) {
        Item item = itemstack.b();
        int i0 = itemstack.b;
        int i1;

        if (itemstack.e() == 1) {
            i1 = this.j();
            if (i1 < 0) {
                return i0;
            }
            else {
                if (this.a[i1] == null) {
                    this.a[i1] = ItemStack.b(itemstack);
                }

                return 0;
            }
        }
        else {
            i1 = this.d(itemstack);
            if (i1 < 0) {
                i1 = this.j();
            }

            if (i1 < 0) {
                return i0;
            }
            else {
                if (this.a[i1] == null) {
                    this.a[i1] = new ItemStack(item, 0, itemstack.k());
                    if (itemstack.p()) {
                        this.a[i1].d((NBTTagCompound) itemstack.q().b());
                    }
                }

                int i2 = i0;

                if (i0 > this.a[i1].e() - this.a[i1].b) {
                    i2 = this.a[i1].e() - this.a[i1].b;
                }

                if (i2 > this.d() - this.a[i1].b) {
                    i2 = this.d() - this.a[i1].b;
                }

                if (i2 == 0) {
                    return i0;
                }
                else {
                    i0 -= i2;
                    this.a[i1].b += i2;
                    this.a[i1].c = 5;
                    return i0;
                }
            }
        }
    }

    public void k() {
        for (int i0 = 0; i0 < this.a.length; ++i0) {
            if (this.a[i0] != null) {
                this.a[i0].a(this.d.p, this.d, i0, this.c == i0);
            }
        }
    }

    public boolean a(Item item) {
        int i0 = this.c(item);

        if (i0 < 0) {
            return false;
        }
        else {
            if (--this.a[i0].b <= 0) {
                this.a[i0] = null;
            }

            return true;
        }
    }

    public boolean b(Item item) {
        int i0 = this.c(item);

        return i0 >= 0;
    }

    // CanaryMod: Simulate Pickup (Its the same as a(ItemStack) but without altering the inventory
    public boolean canPickup(EntityItem entityitem) {
        ItemStack itemstack = entityitem.f();
        int i;

        if (itemstack.i()) {
            i = InventoryPlayer.i();
            if (i >= 0) {
                // CanaryMod: ItemPickUp
                ItemPickupHook hook = (ItemPickupHook) new ItemPickupHook(((EntityPlayerMP) this.d).getPlayer(), (net.canarymod.api.entity.EntityItem) entityitem.getCanaryEntity()).call();
                return !hook.isCanceled();
                //
            }
            else if (this.d.bF.d) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            int slot = 0;
            int left = itemstack.b;

            do {
                ItemStack itemstack1 = this.a[slot];
                int delta = 0;

                if (itemstack1 == null) {
                    delta = Math.min(64, left);
                }
                else if (itemstack1.b < 64 && itemstack.c == itemstack1.c && itemstack.e() == itemstack1.e()) {
                    delta = Math.min(64 - itemstack.b, left);
                }
                left -= delta;
                slot++;
            }
            while (left > 0 && slot < 36);
            if (itemstack.b - left > 0) {
                // CanaryMod: ItemPickUp
                if (this.d instanceof EntityPlayerMP) { // Cause NPC may be picking something up...
                    return !((ItemPickupHook) new ItemPickupHook(((EntityPlayerMP) this.d).getPlayer(), (net.canarymod.api.entity.EntityItem) entityitem.getCanaryEntity()).call()).isCanceled();
                }
                return true; // TODO: NPC Behavior?
                //
            }
            else {
                return false;
            }
        }
    }

    public boolean a(final ItemStack itemstack) {
        if (itemstack != null && itemstack.b != 0 && itemstack.b() != null) {
            try {
                int i0;

                if (itemstack.i()) {
                    i0 = this.j();
                    if (i0 >= 0) {
                        this.a[i0] = ItemStack.b(itemstack);
                        this.a[i0].c = 5;
                        itemstack.b = 0;
                        return true;
                    }
                    else if (this.d.bF.d) {
                        itemstack.b = 0;
                        return true;
                    }
                    else {
                        return false;
                    }
                }
                else {
                    do {
                        i0 = itemstack.b;
                        itemstack.b = this.e(itemstack);
                    } while (itemstack.b > 0 && itemstack.b < i0);

                    if (itemstack.b == i0 && this.d.bF.d) {
                        itemstack.b = 0;
                        return true;
                    }
                    else {
                        return itemstack.b < i0;
                    }
                }
            }
            catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Adding item to inventory");
                CrashReportCategory crashreportcategory = crashreport.a("Item being added");

                crashreportcategory.a("Item ID", (Object) Integer.valueOf(Item.b(itemstack.b())));
                crashreportcategory.a("Item data", (Object) Integer.valueOf(itemstack.k()));
                crashreportcategory.a("Item name", new Callable() {

                    public String call() {
                        return itemstack.s();
                    }
                });
                throw new ReportedException(crashreport);
            }
        }
        else {
            return false;
        }
    }

    public ItemStack a(int i0, int i1) {
        ItemStack[] aitemstack = this.a;

        if (i0 >= this.a.length) {
            aitemstack = this.b;
            i0 -= this.a.length;
        }

        if (aitemstack[i0] != null) {
            ItemStack itemstack;

            if (aitemstack[i0].b <= i1) {
                itemstack = aitemstack[i0];
                aitemstack[i0] = null;
                return itemstack;
            }
            else {
                itemstack = aitemstack[i0].a(i1);
                if (aitemstack[i0].b == 0) {
                    aitemstack[i0] = null;
                }

                return itemstack;
            }
        }
        else {
            return null;
        }
    }

    public ItemStack a_(int i0) {
        ItemStack[] aitemstack = this.a;

        if (i0 >= this.a.length) {
            aitemstack = this.b;
            i0 -= this.a.length;
        }

        if (aitemstack[i0] != null) {
            ItemStack itemstack = aitemstack[i0];

            aitemstack[i0] = null;
            return itemstack;
        }
        else {
            return null;
        }
    }

    public void a(int i0, ItemStack itemstack) {
        ItemStack[] aitemstack = this.a;

        if (i0 >= aitemstack.length) {
            i0 -= aitemstack.length;
            aitemstack = this.b;
        }

        aitemstack[i0] = itemstack;
    }

    public float a(Block block) {
        float f0 = 1.0F;

        if (this.a[this.c] != null) {
            f0 *= this.a[this.c].a(block);
        }

        return f0;
    }

    public NBTTagList a(NBTTagList nbttaglist) {
        int i0;
        NBTTagCompound nbttagcompound;

        for (i0 = 0; i0 < this.a.length; ++i0) {
            if (this.a[i0] != null) {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.a("Slot", (byte) i0);
                this.a[i0].b(nbttagcompound);
                nbttaglist.a((NBTBase) nbttagcompound);
            }
        }

        for (i0 = 0; i0 < this.b.length; ++i0) {
            if (this.b[i0] != null) {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.a("Slot", (byte) (i0 + 100));
                this.b[i0].b(nbttagcompound);
                nbttaglist.a((NBTBase) nbttagcompound);
            }
        }

        return nbttaglist;
    }

    public void b(NBTTagList nbttaglist) {
        this.a = new ItemStack[36];
        this.b = new ItemStack[4];

        for (int i0 = 0; i0 < nbttaglist.c(); ++i0) {
            NBTTagCompound nbttagcompound = nbttaglist.b(i0);
            int i1 = nbttagcompound.d("Slot") & 255;
            ItemStack itemstack = ItemStack.a(nbttagcompound);

            if (itemstack != null) {
                if (i1 >= 0 && i1 < this.a.length) {
                    this.a[i1] = itemstack;
                }

                if (i1 >= 100 && i1 < this.b.length + 100) {
                    this.b[i1 - 100] = itemstack;
                }
            }
        }
    }

    public int a() {
        return this.a.length + 4;
    }

    public ItemStack a(int i0) {
        ItemStack[] aitemstack = this.a;

        if (i0 >= aitemstack.length) {
            i0 -= aitemstack.length;
            aitemstack = this.b;
        }

        return aitemstack[i0];
    }

    public String b() {
        return name; // CanaryMod: return name
    }

    public boolean k_() {
        return false;
    }

    public int d() {
        return 64;
    }

    public boolean b(Block block) {
        if (block.o().l()) {
            return true;
        }
        else {
            ItemStack itemstack = this.a(this.c);

            return itemstack != null ? itemstack.b(block) : false;
        }
    }

    public ItemStack d(int i0) {
        return this.b[i0];
    }

    public int l() {
        int i0 = 0;

        for (int i1 = 0; i1 < this.b.length; ++i1) {
            if (this.b[i1] != null && this.b[i1].b() instanceof ItemArmor) {
                int i2 = ((ItemArmor) this.b[i1].b()).c;

                i0 += i2;
            }
        }

        return i0;
    }

    public void a(float f0) {
        f0 /= 4.0F;
        if (f0 < 1.0F) {
            f0 = 1.0F;
        }

        for (int i0 = 0; i0 < this.b.length; ++i0) {
            if (this.b[i0] != null && this.b[i0].b() instanceof ItemArmor) {
                this.b[i0].a((int) f0, (EntityLivingBase) this.d);
                if (this.b[i0].b == 0) {
                    this.b[i0] = null;
                }
            }
        }
    }

    public void m() {
        int i0;

        for (i0 = 0; i0 < this.a.length; ++i0) {
            if (this.a[i0] != null) {
                this.d.a(this.a[i0], true, false);
                this.a[i0] = null;
            }
        }

        for (i0 = 0; i0 < this.b.length; ++i0) {
            if (this.b[i0] != null) {
                this.d.a(this.b[i0], true, false);
                this.b[i0] = null;
            }
        }
    }

    public void e() {
        this.e = true;
    }

    public void b(ItemStack itemstack) {
        this.g = itemstack;
    }

    public ItemStack o() {
        return this.g;
    }

    public boolean a(EntityPlayer entityplayer) {
        return this.d.L ? false : entityplayer.e(this.d) <= 64.0D;
    }

    public boolean c(ItemStack itemstack) {
        int i0;

        for (i0 = 0; i0 < this.b.length; ++i0) {
            if (this.b[i0] != null && this.b[i0].a(itemstack)) {
                return true;
            }
        }

        for (i0 = 0; i0 < this.a.length; ++i0) {
            if (this.a[i0] != null && this.a[i0].a(itemstack)) {
                return true;
            }
        }

        return false;
    }

    public void f() {
    }

    public void l_() {
    }

    public boolean b(int i0, ItemStack itemstack) {
        return true;
    }

    public void b(InventoryPlayer inventoryplayer) {
        int i0;

        for (i0 = 0; i0 < this.a.length; ++i0) {
            this.a[i0] = ItemStack.b(inventoryplayer.a[i0]);
        }

        for (i0 = 0; i0 < this.b.length; ++i0) {
            this.b[i0] = ItemStack.b(inventoryplayer.b[i0]);
        }

        this.c = inventoryplayer.c;
    }

    // CanaryMod
    public void setName(String value) {
        this.name = value;
    }
}

