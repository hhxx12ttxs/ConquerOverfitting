package net.minecraft.tileentity;

import net.canarymod.api.world.blocks.CanaryHopperBlock;
import net.canarymod.hook.world.HopperTransferHook;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockHopper;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class TileEntityHopper extends TileEntity implements IHopper {

    public ItemStack[] a = new ItemStack[5]; // CanaryMod: private to public
    private String i;
    public int j = -1; // CanaryMod: private to public

    public TileEntityHopper() {
        this.complexBlock = new CanaryHopperBlock(this); // CanaryMod: create once, use forever
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        NBTTagList nbttaglist = nbttagcompound.c("Items", 10);

        this.a = new ItemStack[this.a()];
        if (nbttagcompound.b("CustomName", 8)) {
            this.i = nbttagcompound.j("CustomName");
        }

        this.j = nbttagcompound.f("TransferCooldown");

        for (int i0 = 0; i0 < nbttaglist.c(); ++i0) {
            NBTTagCompound nbttagcompound1 = nbttaglist.b(i0);
            byte b0 = nbttagcompound1.d("Slot");

            if (b0 >= 0 && b0 < this.a.length) {
                this.a[b0] = ItemStack.a(nbttagcompound1);
            }
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
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
        nbttagcompound.a("TransferCooldown", this.j);
        if (this.k_()) {
            nbttagcompound.a("CustomName", this.i);
        }
    }

    public void e() {
        super.e();
    }

    public int a() {
        return this.a.length;
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

    public String b() {
        return this.k_() ? this.i : "container.hopper";
    }

    public boolean k_() {
        return this.i != null && this.i.length() > 0;
    }

    public void a(String s0) {
        this.i = s0;
    }

    public int d() {
        return 64;
    }

    public boolean a(EntityPlayer entityplayer) {
        // CanaryMod: remote inventories
        if (getCanaryHopper().canOpenRemote()) {
            return true;
        }
        //

        return this.b.o(this.c, this.d, this.e) != this ? false : entityplayer.e((double) this.c + 0.5D, (double) this.d + 0.5D, (double) this.e + 0.5D) <= 64.0D;
    }

    public void f() {
    }

    public void l_() {
    }

    public boolean b(int i0, ItemStack itemstack) {
        return true;
    }

    public void h() {
        if (this.b != null && !this.b.E) {
            --this.j;
            if (!this.j()) {
                this.c(0);
                this.i();
            }
        }
    }

    public boolean i() {
        if (this.b != null && !this.b.E) {
            if (!this.j() && BlockHopper.c(this.p())) {
                boolean flag0 = this.k();

                flag0 = a((IHopper) this) || flag0;
                if (flag0) {
                    this.c(8);
                    this.e();
                    return true;
                }
            }

            return false;
        }
        else {
            return false;
        }
    }

    private boolean k() {
        IInventory iinventory = this.l();

        if (iinventory == null) {
            return false;
        }
        else {
            for (int i0 = 0; i0 < this.a(); ++i0) {
                if (this.a(i0) != null) {
                    ItemStack itemstack = this.a(i0).m();
                    // CanaryMod: Hopper Transfer hook
                    HopperTransferHook hook = (HopperTransferHook) new HopperTransferHook(getCanaryHopper(), new net.canarymod.api.inventory.CanaryItem(itemstack), false).call();
                    if (hook.isCanceled()) {
                        return false;
                    }// CanaryMod: End

                    ItemStack itemstack1 = a(iinventory, this.a(i0, 1), Facing.a[BlockHopper.b(this.p())]);

                    if (itemstack1 == null || itemstack1.b == 0) {
                        iinventory.e();
                        return true;
                    }

                    this.a(i0, itemstack);
                }
            }

            return false;
        }
    }

    public static boolean a(IHopper ihopper) {
        IInventory iinventory = b(ihopper);

        if (iinventory != null) {
            byte b0 = 0;

            if (iinventory instanceof ISidedInventory && b0 > -1) {
                ISidedInventory isidedinventory = (ISidedInventory) iinventory;
                int[] aint = isidedinventory.c(b0);

                for (int i0 = 0; i0 < aint.length; ++i0) {
                    if (a(ihopper, iinventory, aint[i0], b0)) {
                        return true;
                    }
                }
            }
            else {
                int i1 = iinventory.a();

                for (int i2 = 0; i2 < i1; ++i2) {
                    if (a(ihopper, iinventory, i2, b0)) {
                        return true;
                    }
                }
            }
        }
        else {
            EntityItem entityitem = a(ihopper.w(), ihopper.x(), ihopper.aD() + 1.0D, ihopper.aE());

            if (entityitem != null) {
                return a((IInventory) ihopper, entityitem);
            }
        }

        return false;
    }

    private static boolean a(IHopper ihopper, IInventory iinventory, int i0, int i1) {
        ItemStack itemstack = iinventory.a(i0);

        if (itemstack != null && b(iinventory, itemstack, i0, i1)) {
            ItemStack itemstack1 = itemstack.m();
            // CanaryMod: Hopper Transfer hook.
            net.canarymod.api.inventory.Hopper hookHopper = null;

            if (ihopper instanceof TileEntityHopper) {
                hookHopper = (net.canarymod.api.inventory.Hopper) ((TileEntityHopper) ihopper).getCanaryHopper();
            }
            else if (ihopper instanceof EntityMinecartHopper) {
                hookHopper = (net.canarymod.api.inventory.Hopper) ((EntityMinecartHopper) ihopper).getCanaryEntity();
            }
            HopperTransferHook hook = (HopperTransferHook) new HopperTransferHook(hookHopper, new net.canarymod.api.inventory.CanaryItem(itemstack), true).call();
            if (hook.isCanceled()) {
                return false;
            }// CanaryMod: End

            ItemStack itemstack2 = a(ihopper, iinventory.a(i0, 1), -1);

            if (itemstack2 == null || itemstack2.b == 0) {
                iinventory.e();
                return true;
            }

            iinventory.a(i0, itemstack1);
        }

        return false;
    }

    public static boolean a(IInventory iinventory, EntityItem entityitem) {
        boolean flag0 = false;

        if (entityitem == null) {
            return false;
        }
        else {
            ItemStack itemstack = entityitem.f().m();
            ItemStack itemstack1 = a(iinventory, itemstack, -1);

            if (itemstack1 != null && itemstack1.b != 0) {
                entityitem.a(itemstack1);
            }
            else {
                flag0 = true;
                entityitem.B();
            }

            return flag0;
        }
    }

    public static ItemStack a(IInventory iinventory, ItemStack itemstack, int i0) {
        if (iinventory instanceof ISidedInventory && i0 > -1) {
            ISidedInventory isidedinventory = (ISidedInventory) iinventory;
            int[] aint = isidedinventory.c(i0);

            for (int i1 = 0; i1 < aint.length && itemstack != null && itemstack.b > 0; ++i1) {
                itemstack = c(iinventory, itemstack, aint[i1], i0);
            }
        }
        else {
            int i2 = iinventory.a();

            for (int i3 = 0; i3 < i2 && itemstack != null && itemstack.b > 0; ++i3) {
                itemstack = c(iinventory, itemstack, i3, i0);
            }
        }

        if (itemstack != null && itemstack.b == 0) {
            itemstack = null;
        }

        return itemstack;
    }

    private static boolean a(IInventory iinventory, ItemStack itemstack, int i0, int i1) {
        return !iinventory.b(i0, itemstack) ? false : !(iinventory instanceof ISidedInventory) || ((ISidedInventory) iinventory).a(i0, itemstack, i1);
    }

    private static boolean b(IInventory iinventory, ItemStack itemstack, int i0, int i1) {
        return !(iinventory instanceof ISidedInventory) || ((ISidedInventory) iinventory).b(i0, itemstack, i1);
    }

    private static ItemStack c(IInventory iinventory, ItemStack itemstack, int i0, int i1) {
        ItemStack itemstack1 = iinventory.a(i0);

        if (a(iinventory, itemstack, i0, i1)) {
            boolean flag0 = false;

            if (itemstack1 == null) {
                iinventory.a(i0, itemstack);
                itemstack = null;
                flag0 = true;
            }
            else if (a(itemstack1, itemstack)) {
                int i2 = itemstack.e() - itemstack1.b;
                int i3 = Math.min(itemstack.b, i2);

                itemstack.b -= i3;
                itemstack1.b += i3;
                flag0 = i3 > 0;
            }

            if (flag0) {
                if (iinventory instanceof TileEntityHopper) {
                    ((TileEntityHopper) iinventory).c(8);
                    iinventory.e();
                }

                iinventory.e();
            }
        }

        return itemstack;
    }

    private IInventory l() {
        int i0 = BlockHopper.b(this.p());

        return b(this.w(), (double) (this.c + Facing.b[i0]), (double) (this.d + Facing.c[i0]), (double) (this.e + Facing.d[i0]));
    }

    public static IInventory b(IHopper ihopper) {
        return b(ihopper.w(), ihopper.x(), ihopper.aD() + 1.0D, ihopper.aE());
    }

    public static EntityItem a(World world, double d0, double d1, double d2) {
        List list = world.a(EntityItem.class, AxisAlignedBB.a().a(d0, d1, d2, d0 + 1.0D, d1 + 1.0D, d2 + 1.0D), IEntitySelector.a);

        return list.size() > 0 ? (EntityItem) list.get(0) : null;
    }

    public static IInventory b(World world, double d0, double d1, double d2) {
        IInventory iinventory = null;
        int i0 = MathHelper.c(d0);
        int i1 = MathHelper.c(d1);
        int i2 = MathHelper.c(d2);
        TileEntity tileentity = world.o(i0, i1, i2);

        if (tileentity != null && tileentity instanceof IInventory) {
            iinventory = (IInventory) tileentity;
            if (iinventory instanceof TileEntityChest) {
                Block block = world.a(i0, i1, i2);

                if (block instanceof BlockChest) {
                    iinventory = ((BlockChest) block).m(world, i0, i1, i2);
                }
            }
        }

        if (iinventory == null) {
            List list = world.a((Entity) null, AxisAlignedBB.a().a(d0, d1, d2, d0 + 1.0D, d1 + 1.0D, d2 + 1.0D), IEntitySelector.b);

            if (list != null && list.size() > 0) {
                iinventory = (IInventory) list.get(world.s.nextInt(list.size()));
            }
        }

        return iinventory;
    }

    private static boolean a(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack.b() != itemstack1.b() ? false : (itemstack.k() != itemstack1.k() ? false : (itemstack.b > itemstack.e() ? false : ItemStack.a(itemstack, itemstack1)));
    }

    public double x() {
        return (double) this.c;
    }

    public double aD() {
        return (double) this.d;
    }

    public double aE() {
        return (double) this.e;
    }

    public void c(int i0) {
        this.j = i0;
    }

    public boolean j() {
        return this.j > 0;
    }

    // CanaryMod
    public CanaryHopperBlock getCanaryHopper() {
        return (CanaryHopperBlock) complexBlock;
    }

    public IInventory getInputInventory() {
        return b(this);
    }

    public IInventory getOutputInventory() {
        return this.l();
    }
}

