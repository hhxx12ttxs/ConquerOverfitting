package net.minecraft.entity.item;

import net.canarymod.Canary;
import net.canarymod.api.CanaryDamageSource;
import net.canarymod.api.entity.living.EntityLiving;
import net.canarymod.api.entity.vehicle.Minecart;
import net.canarymod.api.entity.vehicle.Vehicle;
import net.canarymod.api.world.position.Vector3D;
import net.canarymod.config.Configuration;
import net.canarymod.hook.entity.MinecartActivateHook;
import net.canarymod.hook.entity.VehicleCollisionHook;
import net.canarymod.hook.entity.VehicleDamageHook;
import net.canarymod.hook.entity.VehicleDestroyHook;
import net.canarymod.hook.entity.VehicleEnterHook;
import net.canarymod.hook.entity.VehicleMoveHook;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.ai.EntityMinecartMobSpawner;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

public abstract class EntityMinecart extends Entity {

    public boolean a; // CanaryMod: private -> public
    private String b;
    private static final int[][][] c = new int[][][]{ { { 0, 0, -1 }, { 0, 0, 1 } }, { { -1, 0, 0 }, { 1, 0, 0 } }, { { -1, -1, 0 }, { 1, 0, 0 } }, { { -1, 0, 0 }, { 1, -1, 0 } }, { { 0, 0, -1 }, { 0, -1, 1 } }, { { 0, -1, -1 }, { 0, 0, 1 } }, { { 0, 0, 1 }, { 1, 0, 0 } }, { { 0, 0, 1 }, { -1, 0, 0 } }, { { 0, 0, -1 }, { -1, 0, 0 } }, { { 0, 0, -1 }, { 1, 0, 0 } } };
    private int d;
    private double e;
    private double f;
    private double g;
    private double h;
    private double i;

    public EntityMinecart(World world) {
        super(world);
        this.l = true;
        this.a(0.98F, 0.7F);
        this.M = this.O / 2.0F;
    }

    public static EntityMinecart a(World world, double d0, double d1, double d2, int i0) {
        switch (i0) {
            case 1:
                return new EntityMinecartChest(world, d0, d1, d2);

            case 2:
                return new EntityMinecartFurnace(world, d0, d1, d2);

            case 3:
                return new EntityMinecartTNT(world, d0, d1, d2);

            case 4:
                return new EntityMinecartMobSpawner(world, d0, d1, d2);

            case 5:
                return new EntityMinecartHopper(world, d0, d1, d2);
            case 6:
                return new EntityMinecartCommandBlock(world, d0, d1, d2);

            default:
                return new EntityMinecartEmpty(world, d0, d1, d2);
        }
    }

    protected boolean g_() {
        return false;
    }

    protected void c() {
        this.ag.a(17, new Integer(0));
        this.ag.a(18, new Integer(1));
        this.ag.a(19, new Float(0.0F));
        this.ag.a(20, new Integer(0));
        this.ag.a(21, new Integer(6));
        this.ag.a(22, Byte.valueOf((byte) 0));
    }

    public AxisAlignedBB g(Entity entity) {
        return entity.S() ? entity.D : null;
    }

    public AxisAlignedBB J() {
        return null;
    }

    public boolean S() {
        return true;
    }

    public EntityMinecart(World world, double d0, double d1, double d2) {
        this(world);
        this.b(d0, d1, d2);
        this.w = 0.0D;
        this.x = 0.0D;
        this.y = 0.0D;
        this.q = d0;
        this.r = d1;
        this.s = d2;
    }

    public double ae() {
        return (double) this.O * 0.0D - 0.30000001192092896D;
    }

    public boolean a(DamageSource damagesource, float f0) {
        if (!this.p.E && !this.L) {
            if (this.aw()) {
                return false;
            }
            else {
                // CanaryMod: VehicleDamage
                net.canarymod.api.entity.Entity attk = null;

                if (damagesource.j() != null) {
                    attk = damagesource.j().getCanaryEntity();
                }
                VehicleDamageHook hook = (VehicleDamageHook) new VehicleDamageHook((Vehicle) this.entity, attk, new CanaryDamageSource(damagesource), (int) f0).call();
                if (hook.isCanceled()) {
                    return false;
                }
                f0 = hook.getDamageDealt();
                //

                this.j(-this.l());
                this.c(10);
                this.Q();
                this.a(this.j() + f0 * 10.0F);
                boolean flag0 = damagesource.j() instanceof EntityPlayer && ((EntityPlayer) damagesource.j()).bF.d;

                if (flag0 || this.j() > 40.0F) {
                    if (this.m != null) {
                        this.m.a((Entity) this);
                    }

                    if (flag0 && !this.k_()) {
                        this.B();
                    }
                    else {
                        this.a(damagesource);
                    }
                }

                return true;
            }
        }
        else {
            return true;
        }
    }

    public void a(DamageSource damagesource) {
        // CanaryMod: VehicleDestroy
        new VehicleDestroyHook((Vehicle) this.entity).call();
        //
        this.B();
        ItemStack itemstack = new ItemStack(Items.au, 1);

        if (this.b != null) {
            itemstack.c(this.b);
        }

        this.a(itemstack, 0.0F);
    }

    public boolean R() {
        return !this.L;
    }

    public void B() {
        super.B();
    }

    public void h() {
        if (this.k() > 0) {
            this.c(this.k() - 1);
        }

        if (this.j() > 0.0F) {
            this.a(this.j() - 1.0F);
        }

        if (this.u < -64.0D) {
            this.G();
        }

        int i0;

        if (!this.p.E && this.p instanceof WorldServer) {
            this.p.C.a("portal");
            //MinecraftServer minecraftserver = ((WorldServer) this.p).p();

            i0 = this.D();
            if (this.ao) {
                // CanaryMod moved allow-nether to per-world config
                if (Configuration.getWorldConfig(getCanaryWorld().getFqName()).isNetherAllowed()) {
                    if (this.n == null && this.ap++ >= i0) {
                        this.ap = i0;
                        this.an = this.ai();
                        byte b0;

                        if (this.p.t.i == -1) {
                            b0 = 0;
                        }
                        else {
                            b0 = -1;
                        }

                        this.b(b0);
                    }

                    this.ao = false;
                }
            }
            else {
                if (this.ap > 0) {
                    this.ap -= 4;
                }

                if (this.ap < 0) {
                    this.ap = 0;
                }
            }

            if (this.an > 0) {
                --this.an;
            }

            this.p.C.b();
        }

        if (this.p.E) {
            if (this.d > 0) {
                double d0 = this.t + (this.e - this.t) / (double) this.d;
                double d1 = this.u + (this.f - this.u) / (double) this.d;
                double d2 = this.v + (this.g - this.v) / (double) this.d;
                double d3 = MathHelper.g(this.h - (double) this.z);

                this.z = (float) ((double) this.z + d3 / (double) this.d);
                this.A = (float) ((double) this.A + (this.i - (double) this.A) / (double) this.d);
                --this.d;
                this.b(d0, d1, d2);
                this.b(this.z, this.A);
            }
            else {
                this.b(this.t, this.u, this.v);
                this.b(this.z, this.A);
            }
        }
        else {
            float prevRot = this.B, prevPit = this.C;
            double ppX = this.q, ppY = this.r, ppZ = this.s;
            this.q = this.t;
            this.r = this.u;
            this.s = this.v;
            this.x -= 0.03999999910593033D;
            int i1 = MathHelper.c(this.t);

            i0 = MathHelper.c(this.u);
            int i2 = MathHelper.c(this.v);

            if (BlockRailBase.b_(this.p, i1, i0 - 1, i2)) {
                --i0;
            }

            double d4 = 0.4D;
            double d5 = 0.0078125D;
            Block block = this.p.a(i1, i0, i2);

            if (BlockRailBase.a(block)) {
                int i3 = this.p.e(i1, i0, i2);

                this.a(i1, i0, i2, d4, d5, block, i3);
                if (block == Blocks.cc) {
                    // CanaryMod: MinecartActivate
                    MinecartActivateHook mah = new MinecartActivateHook((Minecart) this.getCanaryEntity(), (i1 & 8) != 0);

                    Canary.hooks().callHook(mah);
                    if (!mah.isCanceled()) {
                        this.a(i1, i0, i2, (i3 & 8) != 0);
                    }
                    //
                }
            }
            else {
                this.b(d4);
            }

            this.I();
            this.A = 0.0F;
            double d6 = this.q - this.t;
            double d7 = this.s - this.v;

            if (d6 * d6 + d7 * d7 > 0.001D) {
                this.z = (float) (Math.atan2(d7, d6) * 180.0D / 3.141592653589793D);
                if (this.a) {
                    this.z += 180.0F;
                }
            }

            double d8 = (double) MathHelper.g(this.z - this.B);

            if (d8 < -170.0D || d8 >= 170.0D) {
                this.z += 180.0F;
                this.a = !this.a;
            }

            this.b(this.z, this.A);
            // CanaryMod: VehicleMove
            Vector3D from = new Vector3D(this.q, this.r, this.s);
            Vector3D to = new Vector3D(this.t, this.u, this.v);
            if (hasMovedOneBlockOrMore()) {
                VehicleMoveHook vmh = (VehicleMoveHook) new VehicleMoveHook((Vehicle) this.entity, from, to).call();
                if (vmh.isCanceled()) {
                    this.w = 0.0D;
                    this.x = 0.0D;
                    this.y = 0.0D;
                    this.b(this.q, this.r, this.s, this.B, this.C);
                    this.q = ppX;
                    this.r = ppY;
                    this.s = ppZ;
                    this.B = prevRot;
                    this.C = prevRot;
                    this.ac(); // Update rider
                }
            }
            //
            List list = this.p.b((Entity) this, this.D.b(0.20000000298023224D, 0.0D, 0.20000000298023224D));

            if (list != null && !list.isEmpty()) {
                for (int i4 = 0; i4 < list.size(); ++i4) {
                    Entity entity = (Entity) list.get(i4);

                    if (entity != this.m && entity.S() && entity instanceof EntityMinecart) {
                        entity.f((Entity) this);
                    }
                }
            }

            if (this.m != null && this.m.L) {
                if (this.m.n == this) {
                    this.m.n = null;
                }

                this.m = null;
            }

        }
    }

    public void a(int i0, int i1, int i2, boolean flag0) {
    }

    protected void b(double d0) {
        if (this.w < -d0) {
            this.w = -d0;
        }

        if (this.w > d0) {
            this.w = d0;
        }

        if (this.y < -d0) {
            this.y = -d0;
        }

        if (this.y > d0) {
            this.y = d0;
        }

        if (this.E) {
            this.w *= 0.5D;
            this.x *= 0.5D;
            this.y *= 0.5D;
        }

        this.d(this.w, this.x, this.y);
        if (!this.E) {
            this.w *= 0.949999988079071D;
            this.x *= 0.949999988079071D;
            this.y *= 0.949999988079071D;
        }
    }

    protected void a(int i0, int i1, int i2, double d0, double d1, Block block, int i3) {
        this.S = 0.0F;
        Vec3 vec3 = this.a(this.t, this.u, this.v);

        this.u = (double) i1;
        boolean flag0 = false;
        boolean flag1 = false;

        if (block == Blocks.D) {
            flag0 = (i3 & 8) != 0;
            flag1 = !flag0;
        }

        if (((BlockRailBase) block).e()) {
            i3 &= 7;
        }

        if (i3 >= 2 && i3 <= 5) {
            this.u = (double) (i1 + 1);
        }

        if (i3 == 2) {
            this.w -= d1;
        }

        if (i3 == 3) {
            this.w += d1;
        }

        if (i3 == 4) {
            this.y += d1;
        }

        if (i3 == 5) {
            this.y -= d1;
        }

        int[][] aint = c[i3];
        double d2 = (double) (aint[1][0] - aint[0][0]);
        double d3 = (double) (aint[1][2] - aint[0][2]);
        double d4 = Math.sqrt(d2 * d2 + d3 * d3);
        double d5 = this.w * d2 + this.y * d3;

        if (d5 < 0.0D) {
            d2 = -d2;
            d3 = -d3;
        }

        double d6 = Math.sqrt(this.w * this.w + this.y * this.y);

        if (d6 > 2.0D) {
            d6 = 2.0D;
        }

        this.w = d6 * d2 / d4;
        this.y = d6 * d3 / d4;
        double d7;
        double d8;
        double d9;
        double d10;

        if (this.m != null && this.m instanceof EntityLivingBase) {
            d7 = (double) ((EntityLivingBase) this.m).bf;
            if (d7 > 0.0D) {
                d8 = -Math.sin((double) (this.m.z * 3.1415927F / 180.0F));
                d9 = Math.cos((double) (this.m.z * 3.1415927F / 180.0F));
                d10 = this.w * this.w + this.y * this.y;
                if (d10 < 0.01D) {
                    this.w += d8 * 0.1D;
                    this.y += d9 * 0.1D;
                    flag1 = false;
                }
            }
        }

        if (flag1) {
            d7 = Math.sqrt(this.w * this.w + this.y * this.y);
            if (d7 < 0.03D) {
                this.w *= 0.0D;
                this.x *= 0.0D;
                this.y *= 0.0D;
            }
            else {
                this.w *= 0.5D;
                this.x *= 0.0D;
                this.y *= 0.5D;
            }
        }

        d7 = 0.0D;
        d8 = (double) i0 + 0.5D + (double) aint[0][0] * 0.5D;
        d9 = (double) i2 + 0.5D + (double) aint[0][2] * 0.5D;
        d10 = (double) i0 + 0.5D + (double) aint[1][0] * 0.5D;
        double d11 = (double) i2 + 0.5D + (double) aint[1][2] * 0.5D;

        d2 = d10 - d8;
        d3 = d11 - d9;
        double d12;
        double d13;

        if (d2 == 0.0D) {
            this.t = (double) i0 + 0.5D;
            d7 = this.v - (double) i2;
        }
        else if (d3 == 0.0D) {
            this.v = (double) i2 + 0.5D;
            d7 = this.t - (double) i0;
        }
        else {
            d12 = this.t - d8;
            d13 = this.v - d9;
            d7 = (d12 * d2 + d13 * d3) * 2.0D;
        }

        this.t = d8 + d2 * d7;
        this.v = d9 + d3 * d7;
        this.b(this.t, this.u + (double) this.M, this.v);
        d12 = this.w;
        d13 = this.y;
        if (this.m != null) {
            d12 *= 0.75D;
            d13 *= 0.75D;
        }

        if (d12 < -d0) {
            d12 = -d0;
        }

        if (d12 > d0) {
            d12 = d0;
        }

        if (d13 < -d0) {
            d13 = -d0;
        }

        if (d13 > d0) {
            d13 = d0;
        }

        this.d(d12, 0.0D, d13);
        if (aint[0][1] != 0 && MathHelper.c(this.t) - i0 == aint[0][0] && MathHelper.c(this.v) - i2 == aint[0][2]) {
            this.b(this.t, this.u + (double) aint[0][1], this.v);
        }
        else if (aint[1][1] != 0 && MathHelper.c(this.t) - i0 == aint[1][0] && MathHelper.c(this.v) - i2 == aint[1][2]) {
            this.b(this.t, this.u + (double) aint[1][1], this.v);
        }

        this.i();
        Vec3 vec31 = this.a(this.t, this.u, this.v);

        if (vec31 != null && vec3 != null) {
            double d14 = (vec3.d - vec31.d) * 0.05D;

            d6 = Math.sqrt(this.w * this.w + this.y * this.y);
            if (d6 > 0.0D) {
                this.w = this.w / d6 * (d6 + d14);
                this.y = this.y / d6 * (d6 + d14);
            }

            this.b(this.t, vec31.d, this.v);
        }

        int i4 = MathHelper.c(this.t);
        int i5 = MathHelper.c(this.v);

        if (i4 != i0 || i5 != i2) {
            d6 = Math.sqrt(this.w * this.w + this.y * this.y);
            this.w = d6 * (double) (i4 - i0);
            this.y = d6 * (double) (i5 - i2);
        }

        if (flag0) {
            double d15 = Math.sqrt(this.w * this.w + this.y * this.y);

            if (d15 > 0.01D) {
                double d16 = 0.06D;

                this.w += this.w / d15 * d16;
                this.y += this.y / d15 * d16;
            }
            else if (i3 == 1) {
                if (this.p.a(i0 - 1, i1, i2).r()) {
                    this.w = 0.02D;
                }
                else if (this.p.a(i0 + 1, i1, i2).r()) {
                    this.w = -0.02D;
                }
            }
            else if (i3 == 0) {
                if (this.p.a(i0, i1, i2 - 1).r()) {
                    this.y = 0.02D;
                }
                else if (this.p.a(i0, i1, i2 + 1).r()) {
                    this.y = -0.02D;
                }
            }
        }
    }

    protected void i() {
        if (this.m != null) {
            this.w *= 0.996999979019165D;
            this.x *= 0.0D;
            this.y *= 0.996999979019165D;
        }
        else {
            this.w *= 0.9599999785423279D;
            this.x *= 0.0D;
            this.y *= 0.9599999785423279D;
        }
    }

    public Vec3 a(double d0, double d1, double d2) {
        int i0 = MathHelper.c(d0);
        int i1 = MathHelper.c(d1);
        int i2 = MathHelper.c(d2);

        if (BlockRailBase.b_(this.p, i0, i1 - 1, i2)) {
            --i1;
        }

        Block block = this.p.a(i0, i1, i2);

        if (BlockRailBase.a(block)) {
            int i3 = this.p.e(i0, i1, i2);

            d1 = (double) i1;
            if (((BlockRailBase) block).e()) {
                i3 &= 7;
            }

            if (i3 >= 2 && i3 <= 5) {
                d1 = (double) (i1 + 1);
            }

            int[][] aint = c[i3];
            double d3 = 0.0D;
            double d4 = (double) i0 + 0.5D + (double) aint[0][0] * 0.5D;
            double d5 = (double) i1 + 0.5D + (double) aint[0][1] * 0.5D;
            double d6 = (double) i2 + 0.5D + (double) aint[0][2] * 0.5D;
            double d7 = (double) i0 + 0.5D + (double) aint[1][0] * 0.5D;
            double d8 = (double) i1 + 0.5D + (double) aint[1][1] * 0.5D;
            double d9 = (double) i2 + 0.5D + (double) aint[1][2] * 0.5D;
            double d10 = d7 - d4;
            double d11 = (d8 - d5) * 2.0D;
            double d12 = d9 - d6;

            if (d10 == 0.0D) {
                d0 = (double) i0 + 0.5D;
                d3 = d2 - (double) i2;
            }
            else if (d12 == 0.0D) {
                d2 = (double) i2 + 0.5D;
                d3 = d0 - (double) i0;
            }
            else {
                double d13 = d0 - d4;
                double d14 = d2 - d6;

                d3 = (d13 * d10 + d14 * d12) * 2.0D;
            }

            d0 = d4 + d10 * d3;
            d1 = d5 + d11 * d3;
            d2 = d6 + d12 * d3;
            if (d11 < 0.0D) {
                ++d1;
            }

            if (d11 > 0.0D) {
                d1 += 0.5D;
            }

            return this.p.U().a(d0, d1, d2);
        }
        else {
            return null;
        }
    }

    protected void a(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.n("CustomDisplayTile")) {
            this.k(nbttagcompound.f("DisplayTile"));
            this.l(nbttagcompound.f("DisplayData"));
            this.m(nbttagcompound.f("DisplayOffset"));
        }

        if (nbttagcompound.b("CustomName", 8) && nbttagcompound.j("CustomName").length() > 0) {
            this.b = nbttagcompound.j("CustomName");
        }
    }

    protected void b(NBTTagCompound nbttagcompound) {
        if (this.t()) {
            nbttagcompound.a("CustomDisplayTile", true);
            nbttagcompound.a("DisplayTile", this.n().o() == Material.a ? 0 : Block.b(this.n()));
            nbttagcompound.a("DisplayData", this.p());
            nbttagcompound.a("DisplayOffset", this.r());
        }

        if (this.b != null && this.b.length() > 0) {
            nbttagcompound.a("CustomName", this.b);
        }
    }

    public void f(Entity entity) {
        if (!this.p.E) {
            if (entity != this.m) {
                // CanaryMod: VehicleCollision
                VehicleCollisionHook vch = new VehicleCollisionHook((Vehicle) this.entity, entity.getCanaryEntity());

                Canary.hooks().callHook(vch);
                if (vch.isCanceled()) {
                    return;
                }
                //
                if (entity instanceof EntityLivingBase && !(entity instanceof EntityPlayer) && !(entity instanceof EntityIronGolem) && this.m() == 0 && this.w * this.w + this.y * this.y > 0.01D && this.m == null && entity.n == null) {
                    // CanaryMod: VehicleEnter (Animal/Mob)
                    VehicleEnterHook veh = new VehicleEnterHook((Vehicle) this.entity, (EntityLiving) entity.getCanaryEntity());

                    Canary.hooks().callHook(veh);
                    if (!veh.isCanceled()) {
                        entity.a((Entity) this);
                    }
                    //
                }

                double d0 = entity.t - this.t;
                double d1 = entity.v - this.v;
                double d2 = d0 * d0 + d1 * d1;

                if (d2 >= 9.999999747378752E-5D) {
                    d2 = (double) MathHelper.a(d2);
                    d0 /= d2;
                    d1 /= d2;
                    double d3 = 1.0D / d2;

                    if (d3 > 1.0D) {
                        d3 = 1.0D;
                    }

                    d0 *= d3;
                    d1 *= d3;
                    d0 *= 0.10000000149011612D;
                    d1 *= 0.10000000149011612D;
                    d0 *= (double) (1.0F - this.Z);
                    d1 *= (double) (1.0F - this.Z);
                    d0 *= 0.5D;
                    d1 *= 0.5D;
                    if (entity instanceof EntityMinecart) {
                        double d4 = entity.t - this.t;
                        double d5 = entity.v - this.v;
                        Vec3 vec3 = this.p.U().a(d4, 0.0D, d5).a();
                        Vec3 vec31 = this.p.U().a((double) MathHelper.b(this.z * 3.1415927F / 180.0F), 0.0D, (double) MathHelper.a(this.z * 3.1415927F / 180.0F)).a();
                        double d6 = Math.abs(vec3.b(vec31));

                        if (d6 < 0.800000011920929D) {
                            return;
                        }

                        double d7 = entity.w + this.w;
                        double d8 = entity.y + this.y;

                        if (((EntityMinecart) entity).m() == 2 && this.m() != 2) {
                            this.w *= 0.20000000298023224D;
                            this.y *= 0.20000000298023224D;
                            this.g(entity.w - d0, 0.0D, entity.y - d1);
                            entity.w *= 0.949999988079071D;
                            entity.y *= 0.949999988079071D;
                        }
                        else if (((EntityMinecart) entity).m() != 2 && this.m() == 2) {
                            entity.w *= 0.20000000298023224D;
                            entity.y *= 0.20000000298023224D;
                            entity.g(this.w + d0, 0.0D, this.y + d1);
                            this.w *= 0.949999988079071D;
                            this.y *= 0.949999988079071D;
                        }
                        else {
                            d7 /= 2.0D;
                            d8 /= 2.0D;
                            this.w *= 0.20000000298023224D;
                            this.y *= 0.20000000298023224D;
                            this.g(d7 - d0, 0.0D, d8 - d1);
                            entity.w *= 0.20000000298023224D;
                            entity.y *= 0.20000000298023224D;
                            entity.g(d7 + d0, 0.0D, d8 + d1);
                        }
                    }
                    else {
                        this.g(-d0, 0.0D, -d1);
                        entity.g(d0 / 4.0D, 0.0D, d1 / 4.0D);
                    }
                }
            }
        }
    }

    public void a(float f0) {
        this.ag.b(19, Float.valueOf(f0));
    }

    public float j() {
        return this.ag.d(19);
    }

    public void c(int i0) {
        this.ag.b(17, Integer.valueOf(i0));
    }

    public int k() {
        return this.ag.c(17);
    }

    public void j(int i0) {
        this.ag.b(18, Integer.valueOf(i0));
    }

    public int l() {
        return this.ag.c(18);
    }

    public abstract int m();

    public Block n() {
        if (!this.t()) {
            return this.o();
        }
        else {
            int i0 = this.z().c(20) & '\uffff';

            return Block.e(i0);
        }
    }

    public Block o() {
        return Blocks.a;
    }

    public int p() {
        return !this.t() ? this.q() : this.z().c(20) >> 16;
    }

    public int q() {
        return 0;
    }

    public int r() {
        return !this.t() ? this.s() : this.z().c(21);
    }

    public int s() {
        return 6;
    }

    public void k(int i0) {
        this.z().b(20, Integer.valueOf(i0 & '\uffff' | this.p() << 16));
        this.a(true);
    }

    public void l(int i0) {
        this.z().b(20, Integer.valueOf(Block.b(this.n()) & '\uffff' | i0 << 16));
        this.a(true);
    }

    public void m(int i0) {
        this.z().b(21, Integer.valueOf(i0));
        this.a(true);
    }

    public boolean t() {
        return this.z().a(22) == 1;
    }

    public void a(boolean flag0) {
        this.z().b(22, Byte.valueOf((byte) (flag0 ? 1 : 0)));
    }

    public void a(String s0) {
        this.b = s0;
    }

    public String b_() {
        return this.b != null ? this.b : super.b_();
    }

    public boolean k_() {
        return this.b != null;
    }

    public String u() {
        return this.b;
    }
}

