package net.minecraft.entity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityJumpHelper;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.EntitySenses;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public abstract class EntityLiving extends EntityLivingBase {

    public int a_;
    protected int b;
    private EntityLookHelper h;
    private EntityMoveHelper i;
    private EntityJumpHelper j;
    private EntityBodyHelper bn;
    private PathNavigate bo;
    protected final EntityAITasks c;
    protected final EntityAITasks d;
    private EntityLivingBase bp;
    private EntitySenses bq;
    private ItemStack[] br = new ItemStack[5];
    protected float[] e = new float[5];
    private boolean bs;
    private boolean bt;
    protected float f;
    private Entity bu;
    protected int g;
    private boolean bv;
    private Entity bw;
    private NBTTagCompound bx;

    public EntityLiving(World world) {
        super(world);
        this.c = new EntityAITasks(world != null && world.C != null ? world.C : null);
        this.d = new EntityAITasks(world != null && world.C != null ? world.C : null);
        this.h = new EntityLookHelper(this);
        this.i = new EntityMoveHelper(this);
        this.j = new EntityJumpHelper(this);
        this.bn = new EntityBodyHelper(this);
        this.bo = new PathNavigate(this, world);
        this.bq = new EntitySenses(this);

        for (int i0 = 0; i0 < this.e.length; ++i0) {
            this.e[i0] = 0.085F;
        }
    }

    protected void aD() {
        super.aD();
        this.bc().b(SharedMonsterAttributes.b).a(16.0D);
    }

    public EntityLookHelper j() {
        return this.h;
    }

    public EntityMoveHelper k() {
        return this.i;
    }

    public EntityJumpHelper l() {
        return this.j;
    }

    public PathNavigate m() {
        return this.bo;
    }

    public EntitySenses n() {
        return this.bq;
    }

    public EntityLivingBase o() {
        return this.bp;
    }

    public void d(EntityLivingBase entitylivingbase) {
        this.bp = entitylivingbase;
    }

    public boolean a(Class oclass0) {
        return EntityCreeper.class != oclass0 && EntityGhast.class != oclass0;
    }

    public void p() {
    }

    protected void c() {
        super.c();
        this.ag.a(11, Byte.valueOf((byte) 0));
        this.ag.a(10, "");
    }

    public int q() {
        return 80;
    }

    public void r() {
        String s0 = this.t();

        if (s0 != null) {
            this.a(s0, this.bf(), this.bg());
        }
    }

    public void C() {
        super.C();
        this.p.C.a("mobBaseTick");
        if (this.Z() && this.aa.nextInt(1000) < this.a_++) {
            this.a_ = -this.q();
            this.r();
        }

        this.p.C.b();
    }

    protected int e(EntityPlayer entityplayer) {
        if (this.b > 0) {
            int i0 = this.b;
            ItemStack[] aitemstack = this.ak();

            for (int i1 = 0; i1 < aitemstack.length; ++i1) {
                if (aitemstack[i1] != null && this.e[i1] <= 1.0F) {
                    i0 += 1 + this.aa.nextInt(3);
                }
            }

            return i0;
        }
        else {
            return this.b;
        }
    }

    public void s() {
        for (int i0 = 0; i0 < 20; ++i0) {
            double d0 = this.aa.nextGaussian() * 0.02D;
            double d1 = this.aa.nextGaussian() * 0.02D;
            double d2 = this.aa.nextGaussian() * 0.02D;
            double d3 = 10.0D;

            this.p.a("explode", this.t + (double) (this.aa.nextFloat() * this.N * 2.0F) - (double) this.N - d0 * d3, this.u + (double) (this.aa.nextFloat() * this.O) - d1 * d3, this.v + (double) (this.aa.nextFloat() * this.N * 2.0F) - (double) this.N - d2 * d3, d0, d1, d2);
        }
    }

    public void h() {
        super.h();
        if (!this.p.E) {
            this.bJ();
        }
    }

    protected float f(float f0, float f1) {
        if (this.bk()) {
            this.bn.a();
            return f1;
        }
        else {
            return super.f(f0, f1);
        }
    }

    protected String t() {
        return null;
    }

    protected Item u() {
        return Item.d(0);
    }

    protected void b(boolean flag0, int i0) {
        Item item = this.u();

        if (item != null) {
            int i1 = this.aa.nextInt(3);

            if (i0 > 0) {
                i1 += this.aa.nextInt(i0 + 1);
            }

            for (int i2 = 0; i2 < i1; ++i2) {
                this.a(item, 1);
            }
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.a("CanPickUpLoot", this.bH());
        nbttagcompound.a("PersistenceRequired", this.bt);
        NBTTagList nbttaglist = new NBTTagList();

        NBTTagCompound nbttagcompound1;

        for (int i0 = 0; i0 < this.br.length; ++i0) {
            nbttagcompound1 = new NBTTagCompound();
            if (this.br[i0] != null) {
                this.br[i0].b(nbttagcompound1);
            }

            nbttaglist.a((NBTBase) nbttagcompound1);
        }

        nbttagcompound.a("Equipment", (NBTBase) nbttaglist);
        NBTTagList nbttaglist1 = new NBTTagList();

        for (int i1 = 0; i1 < this.e.length; ++i1) {
            nbttaglist1.a((NBTBase) (new NBTTagFloat(this.e[i1])));
        }

        nbttagcompound.a("DropChances", (NBTBase) nbttaglist1);
        nbttagcompound.a("CustomName", this.bE());
        nbttagcompound.a("CustomNameVisible", this.bG());
        nbttagcompound.a("Leashed", this.bv);
        if (this.bw != null) {
            nbttagcompound1 = new NBTTagCompound();
            if (this.bw instanceof EntityLivingBase) {
                nbttagcompound1.a("UUIDMost", this.bw.aB().getMostSignificantBits());
                nbttagcompound1.a("UUIDLeast", this.bw.aB().getLeastSignificantBits());
            }
            else if (this.bw instanceof EntityHanging) {
                EntityHanging entityhanging = (EntityHanging) this.bw;

                nbttagcompound1.a("X", entityhanging.b);
                nbttagcompound1.a("Y", entityhanging.c);
                nbttagcompound1.a("Z", entityhanging.d);
            }

            nbttagcompound.a("Leash", (NBTBase) nbttagcompound1);
        }
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.h(nbttagcompound.n("CanPickUpLoot"));
        this.bt = nbttagcompound.n("PersistenceRequired");
        if (nbttagcompound.b("CustomName", 8) && nbttagcompound.j("CustomName").length() > 0) {
            this.a(nbttagcompound.j("CustomName"));
        }

        this.g(nbttagcompound.n("CustomNameVisible"));
        NBTTagList nbttaglist;
        int i0;

        if (nbttagcompound.b("Equipment", 9)) {
            nbttaglist = nbttagcompound.c("Equipment", 10);

            for (i0 = 0; i0 < this.br.length; ++i0) {
                this.br[i0] = ItemStack.a(nbttaglist.b(i0));
            }
        }

        if (nbttagcompound.b("DropChances", 9)) {
            nbttaglist = nbttagcompound.c("DropChances", 5);

            for (i0 = 0; i0 < nbttaglist.c(); ++i0) {
                this.e[i0] = nbttaglist.e(i0);
            }
        }

        this.bv = nbttagcompound.n("Leashed");
        if (this.bv && nbttagcompound.b("Leash", 10)) {
            this.bx = nbttagcompound.m("Leash");
        }
    }

    public void n(float f0) {
        this.bf = f0;
    }

    public void i(float f0) {
        super.i(f0);
        this.n(f0);
    }

    public void e() {
        super.e();
        this.p.C.a("looting");
        if (!this.p.E && this.bH() && !this.aU && this.p.N().b("mobGriefing")) {
            List list = this.p.a(EntityItem.class, this.D.b(1.0D, 0.0D, 1.0D));
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                EntityItem entityitem = (EntityItem) iterator.next();

                if (!entityitem.L && entityitem.f() != null) {
                    ItemStack itemstack = entityitem.f();
                    int i0 = b(itemstack);

                    if (i0 > -1) {
                        boolean flag0 = true;
                        ItemStack itemstack1 = this.q(i0);

                        if (itemstack1 != null) {
                            if (i0 == 0) {
                                if (itemstack.b() instanceof ItemSword && !(itemstack1.b() instanceof ItemSword)) {
                                    flag0 = true;
                                }
                                else if (itemstack.b() instanceof ItemSword && itemstack1.b() instanceof ItemSword) {
                                    ItemSword itemsword = (ItemSword) itemstack.b();
                                    ItemSword itemsword1 = (ItemSword) itemstack1.b();

                                    if (itemsword.i() == itemsword1.i()) {
                                        flag0 = itemstack.k() > itemstack1.k() || itemstack.p() && !itemstack1.p();
                                    }
                                    else {
                                        flag0 = itemsword.i() > itemsword1.i();
                                    }
                                }
                                else {
                                    flag0 = false;
                                }
                            }
                            else if (itemstack.b() instanceof ItemArmor && !(itemstack1.b() instanceof ItemArmor)) {
                                flag0 = true;
                            }
                            else if (itemstack.b() instanceof ItemArmor && itemstack1.b() instanceof ItemArmor) {
                                ItemArmor itemarmor = (ItemArmor) itemstack.b();
                                ItemArmor itemarmor1 = (ItemArmor) itemstack1.b();

                                if (itemarmor.c == itemarmor1.c) {
                                    flag0 = itemstack.k() > itemstack1.k() || itemstack.p() && !itemstack1.p();
                                }
                                else {
                                    flag0 = itemarmor.c > itemarmor1.c;
                                }
                            }
                            else {
                                flag0 = false;
                            }
                        }

                        if (flag0) {
                            if (itemstack1 != null && this.aa.nextFloat() - 0.1F < this.e[i0]) {
                                this.a(itemstack1, 0.0F);
                            }

                            if (itemstack.b() == Items.i && entityitem.j() != null) {
                                EntityPlayer entityplayer = this.p.a(entityitem.j());

                                if (entityplayer != null) {
                                    entityplayer.a((StatBase) AchievementList.x);
                                }
                            }

                            this.c(i0, itemstack);
                            this.e[i0] = 2.0F;
                            this.bt = true;
                            this.a(entityitem, 1);
                            entityitem.B();
                        }
                    }
                }
            }
        }

        this.p.C.b();
    }

    protected boolean bk() {
        return false;
    }

    protected boolean v() {
        return true;
    }

    protected void w() {
        if (this.bt) {
            this.aV = 0;
        }
        else {
            EntityPlayer entityplayer = this.p.a(this, -1.0D);

            if (entityplayer != null) {
                double d0 = entityplayer.t - this.t;
                double d1 = entityplayer.u - this.u;
                double d2 = entityplayer.v - this.v;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (this.v() && d3 > 16384.0D) {
                    this.B();
                }

                if (this.aV > 600 && this.aa.nextInt(800) == 0 && d3 > 1024.0D && this.v()) {
                    this.B();
                }
                else if (d3 < 1024.0D) {
                    this.aV = 0;
                }
            }
        }
    }

    protected void bn() {
        ++this.aV;
        this.p.C.a("checkDespawn");
        this.w();
        this.p.C.b();
        this.p.C.a("sensing");
        this.bq.a();
        this.p.C.b();
        this.p.C.a("targetSelector");
        this.d.a();
        this.p.C.b();
        this.p.C.a("goalSelector");
        this.c.a();
        this.p.C.b();
        this.p.C.a("navigation");
        this.bo.f();
        this.p.C.b();
        this.p.C.a("mob tick");
        this.bp();
        this.p.C.b();
        this.p.C.a("controls");
        this.p.C.a("move");
        this.i.c();
        this.p.C.c("look");
        this.h.a();
        this.p.C.c("jump");
        this.j.b();
        this.p.C.b();
        this.p.C.b();
    }

    protected void bq() {
        super.bq();
        this.be = 0.0F;
        this.bf = 0.0F;
        this.w();
        float f0 = 8.0F;

        if (this.aa.nextFloat() < 0.02F) {
            EntityPlayer entityplayer = this.p.a(this, (double) f0);

            if (entityplayer != null) {
                this.bu = entityplayer;
                this.g = 10 + this.aa.nextInt(20);
            }
            else {
                this.bg = (this.aa.nextFloat() - 0.5F) * 20.0F;
            }
        }

        if (this.bu != null) {
            this.a(this.bu, 10.0F, (float) this.x());
            if (this.g-- <= 0 || this.bu.L || this.bu.e((Entity) this) > (double) (f0 * f0)) {
                this.bu = null;
            }
        }
        else {
            if (this.aa.nextFloat() < 0.05F) {
                this.bg = (this.aa.nextFloat() - 0.5F) * 20.0F;
            }

            this.z += this.bg;
            this.A = this.f;
        }

        boolean flag0 = this.M();
        boolean flag1 = this.P();

        if (flag0 || flag1) {
            this.bd = this.aa.nextFloat() < 0.8F;
        }
    }

    public int x() {
        return 40;
    }

    public void a(Entity entity, float f0, float f1) {
        double d0 = entity.t - this.t;
        double d1 = entity.v - this.v;
        double d2;

        if (entity instanceof EntityLivingBase) {
            EntityLivingBase entitylivingbase = (EntityLivingBase) entity;

            d2 = entitylivingbase.u + (double) entitylivingbase.g() - (this.u + (double) this.g());
        }
        else {
            d2 = (entity.D.b + entity.D.e) / 2.0D - (this.u + (double) this.g());
        }

        double d3 = (double) MathHelper.a(d0 * d0 + d1 * d1);
        float f2 = (float) (Math.atan2(d1, d0) * 180.0D / 3.1415927410125732D) - 90.0F;
        float f3 = (float) (-(Math.atan2(d2, d3) * 180.0D / 3.1415927410125732D));

        this.A = this.b(this.A, f3, f1);
        this.z = this.b(this.z, f2, f0);
    }

    private float b(float f0, float f1, float f2) {
        float f3 = MathHelper.g(f1 - f0);

        if (f3 > f2) {
            f3 = f2;
        }

        if (f3 < -f2) {
            f3 = -f2;
        }

        return f0 + f3;
    }

    public boolean bw() {
        return this.p.b(this.D) && this.p.a((Entity) this, this.D).isEmpty() && !this.p.d(this.D);
    }

    public int bz() {
        return 4;
    }

    public int ax() {
        if (this.o() == null) {
            return 3;
        }
        else {
            int i0 = (int) (this.aS() - this.aY() * 0.33F);

            i0 -= (3 - this.p.r.a()) * 4;
            if (i0 < 0) {
                i0 = 0;
            }

            return i0 + 3;
        }
    }

    public ItemStack be() {
        return this.br[0];
    }

    public ItemStack q(int i0) {
        return this.br[i0];
    }

    public ItemStack r(int i0) {
        return this.br[i0 + 1];
    }

    public void c(int i0, ItemStack itemstack) {
        this.br[i0] = itemstack;
    }

    public ItemStack[] ak() {
        return this.br;
    }

    protected void a(boolean flag0, int i0) {
        for (int i1 = 0; i1 < this.ak().length; ++i1) {
            ItemStack itemstack = this.q(i1);
            boolean flag1 = this.e[i1] > 1.0F;

            if (itemstack != null && (flag0 || flag1) && this.aa.nextFloat() - (float) i0 * 0.01F < this.e[i1]) {
                if (!flag1 && itemstack.g()) {
                    int i2 = Math.max(itemstack.l() - 25, 1);
                    int i3 = itemstack.l() - this.aa.nextInt(this.aa.nextInt(i2) + 1);

                    if (i3 > i2) {
                        i3 = i2;
                    }

                    if (i3 < 1) {
                        i3 = 1;
                    }

                    itemstack.b(i3);
                }

                this.a(itemstack, 0.0F);
            }
        }
    }

    protected void bA() {
        if (this.aa.nextFloat() < 0.15F * this.p.b(this.t, this.u, this.v)) {
            int i0 = this.aa.nextInt(2);
            float f0 = this.p.r == EnumDifficulty.HARD ? 0.1F : 0.25F;

            if (this.aa.nextFloat() < 0.095F) {
                ++i0;
            }

            if (this.aa.nextFloat() < 0.095F) {
                ++i0;
            }

            if (this.aa.nextFloat() < 0.095F) {
                ++i0;
            }

            for (int i1 = 3; i1 >= 0; --i1) {
                ItemStack itemstack = this.r(i1);

                if (i1 < 3 && this.aa.nextFloat() < f0) {
                    break;
                }

                if (itemstack == null) {
                    Item item = a(i1 + 1, i0);

                    if (item != null) {
                        this.c(i1 + 1, new ItemStack(item));
                    }
                }
            }
        }
    }

    public static int b(ItemStack itemstack) {
        if (itemstack.b() != Item.a(Blocks.aK) && itemstack.b() != Items.bL) {
            if (itemstack.b() instanceof ItemArmor) {
                switch (((ItemArmor) itemstack.b()).b) {
                    case 0:
                        return 4;

                    case 1:
                        return 3;

                    case 2:
                        return 2;

                    case 3:
                        return 1;
                }
            }

            return 0;
        }
        else {
            return 4;
        }
    }

    public static Item a(int i0, int i1) {
        switch (i0) {
            case 4:
                if (i1 == 0) {
                    return Items.Q;
                }
                else if (i1 == 1) {
                    return Items.ag;
                }
                else if (i1 == 2) {
                    return Items.U;
                }
                else if (i1 == 3) {
                    return Items.Y;
                }
                else if (i1 == 4) {
                    return Items.ac;
                }

            case 3:
                if (i1 == 0) {
                    return Items.R;
                }
                else if (i1 == 1) {
                    return Items.ah;
                }
                else if (i1 == 2) {
                    return Items.V;
                }
                else if (i1 == 3) {
                    return Items.Z;
                }
                else if (i1 == 4) {
                    return Items.ad;
                }

            case 2:
                if (i1 == 0) {
                    return Items.S;
                }
                else if (i1 == 1) {
                    return Items.ai;
                }
                else if (i1 == 2) {
                    return Items.W;
                }
                else if (i1 == 3) {
                    return Items.aa;
                }
                else if (i1 == 4) {
                    return Items.ae;
                }

            case 1:
                if (i1 == 0) {
                    return Items.T;
                }
                else if (i1 == 1) {
                    return Items.aj;
                }
                else if (i1 == 2) {
                    return Items.X;
                }
                else if (i1 == 3) {
                    return Items.ab;
                }
                else if (i1 == 4) {
                    return Items.af;
                }

            default:
                return null;
        }
    }

    protected void bB() {
        float f0 = this.p.b(this.t, this.u, this.v);

        if (this.be() != null && this.aa.nextFloat() < 0.25F * f0) {
            EnchantmentHelper.a(this.aa, this.be(), (int) (5.0F + f0 * (float) this.aa.nextInt(18)));
        }

        for (int i0 = 0; i0 < 4; ++i0) {
            ItemStack itemstack = this.r(i0);

            if (itemstack != null && this.aa.nextFloat() < 0.5F * f0) {
                EnchantmentHelper.a(this.aa, itemstack, (int) (5.0F + f0 * (float) this.aa.nextInt(18)));
            }
        }
    }

    public EntityLivingData a(EntityLivingData entitylivingdata) {
        this.a(SharedMonsterAttributes.b).a(new AttributeModifier("Random spawn bonus", this.aa.nextGaussian() * 0.05D, 1));
        return entitylivingdata;
    }

    public boolean bC() {
        return false;
    }

    public String b_() {
        return this.bF() ? this.bE() : super.b_();
    }

    public void bD() {
        this.bt = true;
    }

    public void a(String s0) {
        this.ag.b(10, s0);
    }

    public String bE() {
        return this.ag.e(10);
    }

    public boolean bF() {
        return this.ag.e(10).length() > 0;
    }

    public void g(boolean flag0) {
        this.ag.b(11, Byte.valueOf((byte) (flag0 ? 1 : 0)));
    }

    public boolean bG() {
        return this.ag.a(11) == 1;
    }

    public void a(int i0, float f0) {
        this.e[i0] = f0;
    }

    public boolean bH() {
        return this.bs;
    }

    public void h(boolean flag0) {
        this.bs = flag0;
    }

    public boolean bI() {
        return this.bt;
    }

    public final boolean c(EntityPlayer entityplayer) {
        if (this.bL() && this.bM() == entityplayer) {
            this.a(true, !entityplayer.bF.d);
            return true;
        }
        else {
            ItemStack itemstack = entityplayer.bn.h();

            if (itemstack != null && itemstack.b() == Items.ca && this.bK()) {
                if (!(this instanceof EntityTameable) || !((EntityTameable) this).bX()) {
                    this.b(entityplayer, true);
                    --itemstack.b;
                    return true;
                }

                if (entityplayer.b_().equalsIgnoreCase(((EntityTameable) this).b())) {
                    this.b(entityplayer, true);
                    --itemstack.b;
                    return true;
                }
            }

            return this.a(entityplayer) ? true : super.c(entityplayer);
        }
    }

    protected boolean a(EntityPlayer entityplayer) {
        return false;
    }

    protected void bJ() {
        if (this.bx != null) {
            this.bN();
        }

        if (this.bv) {
            if (this.bw == null || this.bw.L) {
                this.a(true, true);
            }
        }
    }

    public void a(boolean flag0, boolean flag1) {
        if (this.bv) {
            this.bv = false;
            this.bw = null;
            if (!this.p.E && flag1) {
                this.a(Items.ca, 1);
            }

            if (!this.p.E && flag0 && this.p instanceof WorldServer) {
                ((WorldServer) this.p).q().a((Entity) this, (Packet) (new S1BPacketEntityAttach(1, this, (Entity) null)));
            }
        }
    }

    public boolean bK() {
        return !this.bL() && !(this instanceof IMob);
    }

    public boolean bL() {
        return this.bv;
    }

    public Entity bM() {
        return this.bw;
    }

    public void b(Entity entity, boolean flag0) {
        this.bv = true;
        this.bw = entity;
        if (!this.p.E && flag0 && this.p instanceof WorldServer) {
            ((WorldServer) this.p).q().a((Entity) this, (Packet) (new S1BPacketEntityAttach(1, this, this.bw)));
        }
    }

    private void bN() {
        if (this.bv && this.bx != null) {
            if (this.bx.b("UUIDMost", 4) && this.bx.b("UUIDLeast", 4)) {
                UUID uuid = new UUID(this.bx.g("UUIDMost"), this.bx.g("UUIDLeast"));
                List list = this.p.a(EntityLivingBase.class, this.D.b(10.0D, 10.0D, 10.0D));
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    EntityLivingBase entitylivingbase = (EntityLivingBase) iterator.next();

                    if (entitylivingbase.aB().equals(uuid)) {
                        this.bw = entitylivingbase;
                        break;
                    }
                }
            }
            else if (this.bx.b("X", 99) && this.bx.b("Y", 99) && this.bx.b("Z", 99)) {
                int i0 = this.bx.f("X");
                int i1 = this.bx.f("Y");
                int i2 = this.bx.f("Z");
                EntityLeashKnot entityleashknot = EntityLeashKnot.b(this.p, i0, i1, i2);

                if (entityleashknot == null) {
                    entityleashknot = EntityLeashKnot.a(this.p, i0, i1, i2);
                }

                this.bw = entityleashknot;
            }
            else {
                this.a(false, true);
            }
        }

        this.bx = null;
    }

    // CanaryMod
    public float getDropChanceForSlot(int slot) {
        if (slot < 0 || slot > 5) {
            return -1.0F;
        }
        return this.e[slot];
    }

    public EntityAITasks getTasks() {
        return this.c;
    }
}

