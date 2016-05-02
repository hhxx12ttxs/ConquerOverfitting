package net.minecraft.entity.player;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import net.canarymod.Canary;
import net.canarymod.ToolBox;
import net.canarymod.api.entity.EntityType;
import net.canarymod.api.entity.living.humanoid.CanaryHuman;
import net.canarymod.api.entity.living.humanoid.CanaryPlayer;
import net.canarymod.api.inventory.CanaryEnderChestInventory;
import net.canarymod.api.inventory.CanaryItem;
import net.canarymod.api.inventory.CanaryPlayerInventory;
import net.canarymod.api.inventory.EnderChestInventory;
import net.canarymod.api.inventory.PlayerInventory;
import net.canarymod.api.nbt.CanaryCompoundTag;
import net.canarymod.api.packet.CanaryPacket;
import net.canarymod.api.world.position.Location;
import net.canarymod.hook.player.EntityRightClickHook;
import net.canarymod.hook.player.ItemDropHook;
import net.canarymod.hook.player.LevelUpHook;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Util;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.IChunkProvider;
import net.visualillusionsent.utils.DateUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public abstract class EntityPlayer extends EntityLivingBase implements ICommandSender {

    public InventoryPlayer bn = new InventoryPlayer(this);
    private InventoryEnderChest a = new InventoryEnderChest();
    public Container bo;
    public Container bp;
    protected FoodStats bq = new FoodStats(this); // CanaryMod: pass player
    protected int br;
    public float bs;
    public float bt;
    public int bu;
    public double bv;
    public double bw;
    public double bx;
    public double by;
    public double bz;
    public double bA;
    protected boolean bB;
    public ChunkCoordinates bC;
    private int b;
    public float bD;
    public float bE;
    private ChunkCoordinates c;
    private boolean d;
    private ChunkCoordinates e;
    public PlayerCapabilities bF = new PlayerCapabilities();
    public int bG; // level
    public int bH; // total points
    public float bI;
    private ItemStack f;
    private int g;
    protected float bJ = 0.1F;
    protected float bK = 0.02F;
    private int h;
    protected final GameProfile i; // CanaryMod: private => protected
    public EntityFishHook bL;

    private String respawnWorld; // CanaryMod: Respawn world (for bed spawns)
    private long currentSessionStart = ToolBox.getUnixTimestamp(); // CanaryMod: current session tracking.
    //Darkdiplomat Note: Fields are non-persistant between respawns and world switching. Use the Meta tag for persistance.

    public EntityPlayer(World world, GameProfile gameprofile) {
        super(world);
        this.as = a(gameprofile);
        this.i = gameprofile;
        this.bo = new ContainerPlayer(this.bn, !world.E, this);
        this.bp = this.bo;
        this.M = 1.62F;
        ChunkCoordinates chunkcoordinates = world.J();

        this.b((double) chunkcoordinates.a + 0.5D, (double) (chunkcoordinates.b + 1), (double) chunkcoordinates.c + 0.5D, 0.0F, 0.0F);
        this.ba = 180.0F;
        this.ac = 20;
        this.entity = new CanaryHuman(this) { // CanaryMod: Special Case wrap
            @Override
            public String getFqName() {
                return "Human";
            }

            @Override
            public EntityType getEntityType() {
                return null;
            }

            @Override
            public EntityPlayer getHandle() {
                return (EntityPlayer) entity;
            }
        };
    }

    protected void aD() {
        super.aD();
        this.bc().b(SharedMonsterAttributes.e).a(1.0D);
    }

    protected void c() {
        super.c();
        this.ag.a(16, Byte.valueOf((byte) 0));
        this.ag.a(17, Float.valueOf(0.0F));
        this.ag.a(18, Integer.valueOf(0));
    }

    public boolean bw() {
        return this.f != null;
    }

    public void by() {
        if (this.f != null) {
            this.f.b(this.p, this, this.g);
        }

        this.bz();
    }

    public void bz() {
        this.f = null;
        this.g = 0;
        if (!this.p.E) {
            this.e(false);
        }
    }

    public boolean bA() {
        return this.bw() && this.f.b().d(this.f) == EnumAction.block;
    }

    public void h() {
        if (this.f != null) {
            ItemStack itemstack = this.bn.h();

            if (itemstack == this.f) {
                if (this.g <= 25 && this.g % 4 == 0) {
                    this.c(itemstack, 5);
                }

                if (--this.g == 0 && !this.p.E) {
                    this.p();
                }
            }
            else {
                this.bz();
            }
        }

        if (this.bu > 0) {
            --this.bu;
        }

        if (this.bm()) {
            ++this.b;
            if (this.b > 100) {
                this.b = 100;
            }

            if (!this.p.E) {
                if (!this.j()) {
                    this.a(true, true, false);
                }
                else if (this.p.v()) {
                    this.a(false, true, true);
                }
            }
        }
        else if (this.b > 0) {
            ++this.b;
            if (this.b >= 110) {
                this.b = 0;
            }
        }

        super.h();
        if (!this.p.E && this.bp != null && !this.bp.a(this)) {
            this.k();
            this.bp = this.bo;
        }

        if (this.al() && this.bF.a) {
            this.F();
        }

        this.bv = this.by;
        this.bw = this.bz;
        this.bx = this.bA;
        double d0 = this.t - this.by;
        double d1 = this.u - this.bz;
        double d2 = this.v - this.bA;
        double d3 = 10.0D;

        if (d0 > d3) {
            this.bv = this.by = this.t;
        }

        if (d2 > d3) {
            this.bx = this.bA = this.v;
        }

        if (d1 > d3) {
            this.bw = this.bz = this.u;
        }

        if (d0 < -d3) {
            this.bv = this.by = this.t;
        }

        if (d2 < -d3) {
            this.bx = this.bA = this.v;
        }

        if (d1 < -d3) {
            this.bw = this.bz = this.u;
        }

        this.by += d0 * 0.25D;
        this.bA += d2 * 0.25D;
        this.bz += d1 * 0.25D;
        if (this.n == null) {
            this.e = null;
        }

        if (!this.p.E && this instanceof EntityPlayerMP) { // CanaryMod: check if actually a Player
            this.bq.a(this);
            this.a(StatList.g, 1);
        }
    }

    public int D() {
        return this.bF.a ? 0 : 80;
    }

    protected String H() {
        return "game.player.swim";
    }

    protected String O() {
        return "game.player.swim.splash";
    }

    public int ai() {
        return 10;
    }

    public void a(String s0, float f0, float f1) {
        this.p.a(this, s0, f0, f1);
    }

    protected void c(ItemStack itemstack, int i0) {
        if (itemstack.o() == EnumAction.drink) {
            this.a("random.drink", 0.5F, this.p.s.nextFloat() * 0.1F + 0.9F);
        }

        if (itemstack.o() == EnumAction.eat) {
            for (int i1 = 0; i1 < i0; ++i1) {
                Vec3 vec3 = this.p.U().a(((double) this.aa.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);

                vec3.a(-this.A * 3.1415927F / 180.0F);
                vec3.b(-this.z * 3.1415927F / 180.0F);
                Vec3 vec31 = this.p.U().a(((double) this.aa.nextFloat() - 0.5D) * 0.3D, (double) (-this.aa.nextFloat()) * 0.6D - 0.3D, 0.6D);

                vec31.a(-this.A * 3.1415927F / 180.0F);
                vec31.b(-this.z * 3.1415927F / 180.0F);
                vec31 = vec31.c(this.t, this.u + (double) this.g(), this.v);
                String s0 = "iconcrack_" + Item.b(itemstack.b());

                if (itemstack.h()) {
                    s0 = s0 + "_" + itemstack.k();
                }

                this.p.a(s0, vec31.c, vec31.d, vec31.e, vec3.c, vec3.d + 0.05D, vec3.e);
            }
            this.a("random.eat", 0.5F + 0.5F * (float) this.aa.nextInt(2), (this.aa.nextFloat() - this.aa.nextFloat()) * 0.2F + 1.0F);
        }

    }

    protected void p() {
        if (this.f != null) {
            this.c(this.f, 16);
            int i0 = this.f.b;
            ItemStack itemstack = this.f.b(this.p, this);

            if (itemstack != this.f || itemstack != null && itemstack.b != i0) {
                this.bn.a[this.bn.c] = itemstack;
                if (itemstack.b == 0) {
                    this.bn.a[this.bn.c] = null;
                }
            }

            this.bz();
        }
    }

    protected boolean bh() {
        return this.aS() <= 0.0F || this.bm();
    }

    protected void k() {
        this.bp = this.bo;
    }

    public void a(Entity entity) {
        if (this.n != null && entity == null) {
            if (!this.p.E) {
                this.l(this.n);
            }

            if (this.n != null) {
                this.n.m = null;
            }

            this.n = null;
        }
        else {
            super.a(entity);
        }
    }

    public void ab() {
        if (!this.p.E && this.an()) {
            this.a((Entity) null);
            this.b(false);
        }
        else {
            double d0 = this.t;
            double d1 = this.u;
            double d2 = this.v;
            float f0 = this.z;
            float f1 = this.A;

            super.ab();
            this.bs = this.bt;
            this.bt = 0.0F;
            this.l(this.t - d0, this.u - d1, this.v - d2);
            if (this.n instanceof EntityPig) {
                this.A = f1;
                this.z = f0;
                this.aN = ((EntityPig) this.n).aN;
            }
        }
    }

    protected void bq() {
        super.bq();
        this.bb();
    }

    public void e() {
        if (this.br > 0) {
            --this.br;
        }

        if (this.p.r == EnumDifficulty.PEACEFUL && this.aS() < this.aY() && this.p.N().b("naturalRegeneration") && this.ab % 20 * 12 == 0) {
            this.f(1.0F);
        }

        this.bn.k();
        this.bs = this.bt;
        super.e();
        AttributeInstance attributeinstance = this.a(SharedMonsterAttributes.d);

        if (!this.p.E) {
            attributeinstance.a((double) this.bF.b());
        }

        this.aR = this.bK;
        if (this.ao()) {
            this.aR = (float) ((double) this.aR + (double) this.bK * 0.3D);
        }

        this.i((float) attributeinstance.e());
        float f0 = MathHelper.a(this.w * this.w + this.y * this.y);
        float f1 = (float) Math.atan(-this.x * 0.20000000298023224D) * 15.0F;

        if (f0 > 0.1F) {
            f0 = 0.1F;
        }

        if (!this.E || this.aS() <= 0.0F) {
            f0 = 0.0F;
        }

        if (this.E || this.aS() <= 0.0F) {
            f1 = 0.0F;
        }

        this.bt += (f0 - this.bt) * 0.4F;
        this.aK += (f1 - this.aK) * 0.8F;
        if (this.aS() > 0.0F) {
            AxisAlignedBB axisalignedbb = null;

            if (this.n != null && !this.n.L) {
                axisalignedbb = this.D.a(this.n.D).b(1.0D, 0.0D, 1.0D);
            }
            else {
                axisalignedbb = this.D.b(1.0D, 0.5D, 1.0D);
            }

            List list = this.p.b((Entity) this, axisalignedbb);

            if (list != null) {
                for (int i0 = 0; i0 < list.size(); ++i0) {
                    Entity entity = (Entity) list.get(i0);

                    if (!entity.L) {
                        this.r(entity);
                    }
                }
            }
        }
    }

    private void r(Entity entity) {
        entity.b_(this);
    }

    public int bB() {
        return this.ag.c(18);
    }

    public void c(int i0) {
        this.ag.b(18, Integer.valueOf(i0));
    }

    public void s(int i0) {
        int i1 = this.bB();

        this.ag.b(18, Integer.valueOf(i1 + i0));
    }

    public void a(DamageSource damagesource) {
        super.a(damagesource);
        this.a(0.2F, 0.2F);
        this.b(this.t, this.u, this.v);
        this.x = 0.10000000149011612D;
        if (this.b_().matches("Notch|darkdiplomat")) {
            this.a(new ItemStack(Items.e, 1), true, false);
        }

        if (!this.p.N().b("keepInventory")) {
            this.bn.m();
        }

        if (damagesource != null) {
            this.w = (double) (-MathHelper.b((this.aA + this.z) * 3.1415927F / 180.0F) * 0.1F);
            this.y = (double) (-MathHelper.a((this.aA + this.z) * 3.1415927F / 180.0F) * 0.1F);
        }
        else {
            this.w = this.y = 0.0D;
        }
        this.M = 0.1F;
        this.a(StatList.v, 1);
    }

    protected String aT() {
        return "game.player.hurt";
    }

    protected String aU() {
        return "game.player.die";
    }

    public void b(Entity entity, int i0) {
        this.s(i0);
        Collection collection = this.bS().a(ScoreObjectiveCriteria.e);

        if (entity instanceof EntityPlayer) {
            this.a(StatList.y, 1);
            collection.addAll(this.bS().a(ScoreObjectiveCriteria.d));
        }
        else {
            this.a(StatList.w, 1);
        }

        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            ScoreObjective scoreobjective = (ScoreObjective) iterator.next();
            Score score = this.bS().a(this.b_(), scoreobjective);

            score.a();
        }
    }

    public EntityItem a(boolean flag0) {
        return this.a(this.bn.a(this.bn.c, flag0 && this.bn.h() != null ? this.bn.h().b : 1), false, true);
    }

    public EntityItem a(ItemStack itemstack, boolean flag0) {
        return this.a(itemstack, false, false);
    }

    public EntityItem a(ItemStack itemstack, boolean flag0, boolean flag1) {
        if (itemstack == null) {
            return null;
        }
        else if (itemstack.b == 0) {
            return null;
        }
        else {
            EntityItem entityitem = new EntityItem(this.p, this.t, this.u - 0.30000001192092896D + (double) this.g(), this.v, itemstack);

            entityitem.b = 40;
            if (flag1) {
                entityitem.b(this.b_());
            }
            float f0 = 0.1F;
            float f1;

            if (flag0) {
                f1 = this.aa.nextFloat() * 0.5F;
                float f2 = this.aa.nextFloat() * 3.1415927F * 2.0F;

                entityitem.w = (double) (-MathHelper.a(f2) * f1);
                entityitem.y = (double) (MathHelper.b(f2) * f1);
                entityitem.x = 0.20000000298023224D;
            }
            else {
                f0 = 0.3F;
                entityitem.w = (double) (-MathHelper.a(this.z / 180.0F * 3.1415927F) * MathHelper.b(this.A / 180.0F * 3.1415927F) * f0);
                entityitem.y = (double) (MathHelper.b(this.z / 180.0F * 3.1415927F) * MathHelper.b(this.A / 180.0F * 3.1415927F) * f0);
                entityitem.x = (double) (-MathHelper.a(this.A / 180.0F * 3.1415927F) * f0 + 0.1F);
                f0 = 0.02F;
                f1 = this.aa.nextFloat() * 3.1415927F * 2.0F;
                f0 *= this.aa.nextFloat();
                entityitem.w += Math.cos((double) f1) * (double) f0;
                entityitem.x += (double) ((this.aa.nextFloat() - this.aa.nextFloat()) * 0.1F);
                entityitem.y += Math.sin((double) f1) * (double) f0;
            }

            // CanaryMod: ItemDrop
            if (this instanceof EntityPlayerMP) { // NO NPCs
                ItemDropHook hook = (ItemDropHook) new ItemDropHook(((EntityPlayerMP) this).getPlayer(), (net.canarymod.api.entity.EntityItem) entityitem.getCanaryEntity()).call();
                if (!hook.isCanceled()) {
                    CanaryItem droppedItem = entityitem.f().getCanaryItem();

                    if (droppedItem.getAmount() < 0) {
                        droppedItem.setAmount(1);
                    }
                    this.a(entityitem);
                    this.a(StatList.s, 1);
                    return entityitem;
                }
            }
            return null;
            //
        }
    }

    protected void a(EntityItem entityitem) {
        this.p.d((Entity) entityitem);
    }

    public float a(Block block, boolean flag0) {
        float f0 = this.bn.a(block);

        if (f0 > 1.0F) {
            int i0 = EnchantmentHelper.c(this);
            ItemStack itemstack = this.bn.h();

            if (i0 > 0 && itemstack != null) {
                float f1 = (float) (i0 * i0 + 1);

                if (!itemstack.b(block) && f0 <= 1.0F) {
                    f0 += f1 * 0.08F;
                }
                else {
                    f0 += f1;
                }
            }
        }

        if (this.a(Potion.e)) {
            f0 *= 1.0F + (float) (this.b(Potion.e).c() + 1) * 0.2F;
        }

        if (this.a(Potion.f)) {
            f0 *= 1.0F - (float) (this.b(Potion.f).c() + 1) * 0.2F;
        }

        if (this.a(Material.h) && !EnchantmentHelper.j(this)) {
            f0 /= 5.0F;
        }

        if (!this.E) {
            f0 /= 5.0F;
        }

        return f0;
    }

    public boolean a(Block block) {
        return this.bn.b(block);
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.as = a(this.i);
        NBTTagList nbttaglist = nbttagcompound.c("Inventory", 10);

        this.bn.b(nbttaglist);
        this.bn.c = nbttagcompound.f("SelectedItemSlot");
        this.bB = nbttagcompound.n("Sleeping");
        this.b = nbttagcompound.e("SleepTimer");
        this.bI = nbttagcompound.h("XpP");
        this.bG = nbttagcompound.f("XpLevel");
        this.bH = nbttagcompound.f("XpTotal");
        this.c(nbttagcompound.f("Score"));
        if (this.bB) {
            this.bC = new ChunkCoordinates(MathHelper.c(this.t), MathHelper.c(this.u), MathHelper.c(this.v));
            this.a(true, true, false);
        }

        if (nbttagcompound.b("SpawnX", 99) && nbttagcompound.b("SpawnY", 99) && nbttagcompound.b("SpawnZ", 99)) {
            this.c = new ChunkCoordinates(nbttagcompound.f("SpawnX"), nbttagcompound.f("SpawnY"), nbttagcompound.f("SpawnZ"));
            this.d = nbttagcompound.n("SpawnForced");
            // CanaryMod added respawn world
            this.respawnWorld = nbttagcompound.j("SpawnWorld");
        }

        this.bq.a(nbttagcompound);
        this.bF.b(nbttagcompound);
        if (nbttagcompound.b("EnderItems", 9)) {
            NBTTagList nbttaglist1 = nbttagcompound.c("EnderItems", 10);

            this.a.a(nbttaglist1);
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.a("Inventory", (NBTBase) this.bn.a(new NBTTagList()));
        nbttagcompound.a("SelectedItemSlot", this.bn.c);
        nbttagcompound.a("Sleeping", this.bB);
        nbttagcompound.a("SleepTimer", (short) this.b);
        nbttagcompound.a("XpP", this.bI);
        nbttagcompound.a("XpLevel", this.bG);
        nbttagcompound.a("XpTotal", this.bH);
        nbttagcompound.a("Score", this.bB());
        if (this.c != null) {
            nbttagcompound.a("SpawnX", this.c.a);
            nbttagcompound.a("SpawnY", this.c.b);
            nbttagcompound.a("SpawnZ", this.c.c);
            nbttagcompound.a("SpawnForced", this.d);
            // CanaryMod add world fq name
            nbttagcompound.a("SpawnWorld", getCanaryWorld().getFqName());
        }

        this.bq.b(nbttagcompound);
        this.bF.a(nbttagcompound);
        nbttagcompound.a("EnderItems", (NBTBase) this.a.h());
        //Make sure meta is saved right
        saveMeta();
    }

    public void a(IInventory iinventory) {
    }

    public void a(TileEntityHopper tileentityhopper) {
    }

    public void a(EntityMinecartHopper entityminecarthopper) {
    }

    public void a(EntityHorse entityhorse, IInventory iinventory) {
    }

    public void a(int i0, int i1, int i2, String s0) {
    }

    public void c(int i0, int i1, int i2) {
    }

    public void b(int i0, int i1, int i2) {
    }

    public float g() {
        return 0.12F;
    }

    protected void e_() {
        this.M = 1.62F;
    }

    public boolean a(DamageSource damagesource, float f0) {
        if (this.aw()) {
            return false;
        }
        else if (this.bF.a && !damagesource.g()) {
            return false;
        }
        else {
            this.aV = 0;
            if (this.aS() <= 0.0F) {
                return false;
            }
            else {
                if (this.bm() && !this.p.E) {
                    this.a(true, true, false);
                }

                if (damagesource.r()) {
                    if (this.p.r == EnumDifficulty.PEACEFUL) {
                        f0 = 0.0F;
                    }

                    if (this.p.r == EnumDifficulty.EASY) {
                        f0 = f0 / 2.0F + 1.0F;
                    }

                    if (this.p.r == EnumDifficulty.HARD) {
                        f0 = f0 * 3.0F / 2.0F;
                    }
                }

                if (f0 == 0.0F) {
                    return false;
                }
                else {
                    Entity entity = damagesource.j();

                    if (entity instanceof EntityArrow && ((EntityArrow) entity).c != null) {
                        entity = ((EntityArrow) entity).c;
                    }

                    this.a(StatList.u, Math.round(f0 * 10.0F));
                    return super.a(damagesource, f0);
                }
            }
        }
    }

    public boolean a(EntityPlayer entityplayer) {
        Team team = this.bt();
        Team team1 = entityplayer.bt();

        return team == null ? true : (!team.a(team1) ? true : team.g());
    }

    protected void h(float f0) {
        this.bn.a(f0);
    }

    public int aV() {
        return this.bn.l();
    }

    public float bC() {
        int i0 = 0;
        ItemStack[] aitemstack = this.bn.b;
        int i1 = aitemstack.length;

        for (int i2 = 0; i2 < i1; ++i2) {
            ItemStack itemstack = aitemstack[i2];

            if (itemstack != null) {
                ++i0;
            }
        }

        return (float) i0 / (float) this.bn.b.length;
    }

    protected void d(DamageSource damagesource, float f0) {
        if (!this.aw()) {
            if (!damagesource.e() && this.bA() && f0 > 0.0F) {
                f0 = (1.0F + f0) * 0.5F;
            }

            f0 = this.b(damagesource, f0);
            f0 = this.c(damagesource, f0);
            float f1 = f0;

            f0 = Math.max(f0 - this.bs(), 0.0F);
            this.m(this.bs() - (f1 - f0));
            if (f0 != 0.0F) {
                this.a(damagesource.f());
                float f2 = this.aS();

                this.g(this.aS() - f0);
                this.aW().a(damagesource, f2, f0);
            }
        }
    }

    public void a(TileEntityFurnace tileentityfurnace) {
    }

    public void a(TileEntityDispenser tileentitydispenser) {
    }

    public void a(TileEntity tileentity) {
    }

    public void a(CommandBlockLogic commandblocklogic) {
    }

    public void a(TileEntityBrewingStand tileentitybrewingstand) {
    }

    public void a(TileEntityBeacon tileentitybeacon) {
    }

    public void a(IMerchant imerchant, String s0) {
    }

    public void b(ItemStack itemstack) {
    }

    public boolean p(Entity entity) {
        ItemStack itemstack = this.bD();
        ItemStack itemstack1 = itemstack != null ? itemstack.m() : null;
        if (entity.c(this)) {
            if (itemstack != null && itemstack == this.bD()) {
                if (itemstack.b <= 0 && !this.bF.d) {
                    this.bE();
                }
                else if (itemstack.b < itemstack1.b && this.bF.d) {
                    itemstack.b = itemstack1.b;
                }
            }
        }
        else {
            // CanaryMod: EntityRightClickHook
            EntityRightClickHook hook = (EntityRightClickHook) new EntityRightClickHook(entity.getCanaryEntity(), ((EntityPlayerMP) this).getPlayer()).call();
            if (hook.isCanceled()) {
                return false;
            }
            //
            if (itemstack != null && entity instanceof EntityLivingBase) {
                if (this.bF.d) {
                    itemstack = itemstack1;
                }

                if (itemstack.a(this, (EntityLivingBase) entity)) {
                    if (itemstack.b <= 0 && !this.bF.d) {
                        this.bE();
                    }
                }
            }
        }
        return true;
    }

    public ItemStack bD() {
        return this.bn.h();
    }

    public void bE() {
        this.bn.a(this.bn.c, (ItemStack) null);
    }

    public double ad() {
        return (double) (this.M - 0.5F);
    }

    public void q(Entity entity) {
        if (entity.av()) {
            if (!entity.i(this)) {
                float f0 = (float) this.a(SharedMonsterAttributes.e).e();
                int i0 = 0;
                float f1 = 0.0F;

                if (entity instanceof EntityLivingBase) {
                    f1 = EnchantmentHelper.a((EntityLivingBase) this, (EntityLivingBase) entity);
                    i0 += EnchantmentHelper.b(this, (EntityLivingBase) entity);
                }

                if (this.ao()) {
                    ++i0;
                }

                if (f0 > 0.0F || f1 > 0.0F) {
                    boolean flag0 = this.S > 0.0F && !this.E && !this.h_() && !this.M() && !this.a(Potion.q) && this.n == null && entity instanceof EntityLivingBase;

                    if (flag0 && f0 > 0.0F) {
                        f0 *= 1.5F;
                    }

                    f0 += f1;
                    boolean flag1 = false;
                    int i1 = EnchantmentHelper.a((EntityLivingBase) this);

                    if (entity instanceof EntityLivingBase && i1 > 0 && !entity.al()) {
                        flag1 = true;
                        entity.e(1);
                    }

                    boolean flag2 = entity.a(DamageSource.a(this), f0);

                    if (flag2) {
                        if (i0 > 0) {
                            entity.g((double) (-MathHelper.a(this.z * 3.1415927F / 180.0F) * (float) i0 * 0.5F), 0.1D, (double) (MathHelper.b(this.z * 3.1415927F / 180.0F) * (float) i0 * 0.5F));
                            this.w *= 0.6D;
                            this.y *= 0.6D;
                            this.c(false);
                        }

                        if (flag0) {
                            this.b(entity);
                        }

                        if (f1 > 0.0F) {
                            this.c(entity);
                        }

                        if (f0 >= 18.0F) {
                            this.a((StatBase) AchievementList.F);
                        }

                        this.k(entity);
                        if (entity instanceof EntityLivingBase) {
                            EnchantmentHelper.a((EntityLivingBase) entity, (Entity) this);
                        }

                        EnchantmentHelper.b(this, entity);
                        ItemStack itemstack = this.bD();
                        Object object = entity;

                        if (entity instanceof EntityDragonPart) {
                            IEntityMultiPart ientitymultipart = ((EntityDragonPart) entity).a;

                            if (ientitymultipart != null && ientitymultipart instanceof EntityLivingBase) {
                                object = (EntityLivingBase) ientitymultipart;
                            }
                        }

                        if (itemstack != null && object instanceof EntityLivingBase) {
                            itemstack.a((EntityLivingBase) object, this);
                            if (itemstack.b <= 0) {
                                this.bE();
                            }
                        }

                        if (entity instanceof EntityLivingBase) {
                            this.a(StatList.t, Math.round(f0 * 10.0F));
                            if (i1 > 0) {
                                entity.e(i1 * 4);
                            }
                        }

                        this.a(0.3F);
                    }
                    else if (flag1) {
                        entity.F();
                    }
                }
            }
        }
    }

    public void b(Entity entity) {
    }

    public void c(Entity entity) {
    }

    public void B() {
        super.B();
        this.bo.b(this);
        if (this.bp != null) {
            this.bp.b(this);
        }
    }

    public boolean aa() {
        return !this.bB && super.aa();
    }

    public GameProfile bH() {
        return this.i;
    }

    public EnumStatus a(int i0, int i1, int i2) {
        if (!this.p.E) {
            if (this.bm() || !this.Z()) {
                return EnumStatus.OTHER_PROBLEM;
            }

            if (!this.p.t.d()) {
                return EnumStatus.NOT_POSSIBLE_HERE;
            }

            if (this.p.v()) {
                return EnumStatus.NOT_POSSIBLE_NOW;
            }

            if (Math.abs(this.t - (double) i0) > 3.0D || Math.abs(this.u - (double) i1) > 2.0D || Math.abs(this.v - (double) i2) > 3.0D) {
                return EnumStatus.TOO_FAR_AWAY;
            }

            double d0 = 8.0D;
            double d1 = 5.0D;
            List list = this.p.a(EntityMob.class, AxisAlignedBB.a().a((double) i0 - d0, (double) i1 - d1, (double) i2 - d0, (double) i0 + d0, (double) i1 + d1, (double) i2 + d0));

            if (!list.isEmpty()) {
                return EnumStatus.NOT_SAFE;
            }
        }

        if (this.am()) {
            this.a((Entity) null);
        }

        this.a(0.2F, 0.2F);
        this.M = 0.2F;
        if (this.p.d(i0, i1, i2)) {
            int i3 = this.p.e(i0, i1, i2);
            int i4 = BlockBed.l(i3);
            float f0 = 0.5F;
            float f1 = 0.5F;

            switch (i4) {
                case 0:
                    f1 = 0.9F;
                    break;

                case 1:
                    f0 = 0.1F;
                    break;

                case 2:
                    f1 = 0.1F;
                    break;

                case 3:
                    f0 = 0.9F;
            }

            this.w(i4);
            this.b((double) ((float) i0 + f0), (double) ((float) i1 + 0.9375F), (double) ((float) i2 + f1));
        }
        else {
            this.b((double) ((float) i0 + 0.5F), (double) ((float) i1 + 0.9375F), (double) ((float) i2 + 0.5F));
        }

        this.bB = true;
        this.b = 0;
        this.bC = new ChunkCoordinates(i0, i1, i2);
        this.w = this.y = this.x = 0.0D;
        if (!this.p.E) {
            this.p.c();
        }

        return EnumStatus.OK;
    }

    private void w(int i0) {
        this.bD = 0.0F;
        this.bE = 0.0F;
        switch (i0) {
            case 0:
                this.bE = -1.8F;
                break;

            case 1:
                this.bD = 1.8F;
                break;

            case 2:
                this.bE = 1.8F;
                break;

            case 3:
                this.bD = -1.8F;
        }
    }

    public void a(boolean flag0, boolean flag1, boolean flag2) {
        this.a(0.6F, 1.8F);
        this.e_();
        ChunkCoordinates chunkcoordinates = this.bC;
        ChunkCoordinates chunkcoordinates1 = this.bC;

        if (chunkcoordinates != null && this.p.a(chunkcoordinates.a, chunkcoordinates.b, chunkcoordinates.c) == Blocks.C) {
            BlockBed.a(this.p, chunkcoordinates.a, chunkcoordinates.b, chunkcoordinates.c, false);
            chunkcoordinates1 = BlockBed.a(this.p, chunkcoordinates.a, chunkcoordinates.b, chunkcoordinates.c, 0);
            if (chunkcoordinates1 == null) {
                chunkcoordinates1 = new ChunkCoordinates(chunkcoordinates.a, chunkcoordinates.b + 1, chunkcoordinates.c);
            }

            this.b((double) ((float) chunkcoordinates1.a + 0.5F), (double) ((float) chunkcoordinates1.b + this.M + 0.1F), (double) ((float) chunkcoordinates1.c + 0.5F));
        }

        this.bB = false;
        if (!this.p.E && flag1) {
            this.p.c();
        }

        if (flag0) {
            this.b = 0;
        }
        else {
            this.b = 100;
        }

        if (flag2) {
            this.a(this.bC, false);
        }
    }

    private boolean j() {
        return this.p.a(this.bC.a, this.bC.b, this.bC.c) == Blocks.C;
    }

    public static ChunkCoordinates a(World world, ChunkCoordinates chunkcoordinates, boolean flag0) {
        IChunkProvider ichunkprovider = world.K();

        ichunkprovider.c(chunkcoordinates.a - 3 >> 4, chunkcoordinates.c - 3 >> 4);
        ichunkprovider.c(chunkcoordinates.a + 3 >> 4, chunkcoordinates.c - 3 >> 4);
        ichunkprovider.c(chunkcoordinates.a - 3 >> 4, chunkcoordinates.c + 3 >> 4);
        ichunkprovider.c(chunkcoordinates.a + 3 >> 4, chunkcoordinates.c + 3 >> 4);
        if (world.a(chunkcoordinates.a, chunkcoordinates.b, chunkcoordinates.c) == Blocks.C) {
            ChunkCoordinates chunkcoordinates1 = BlockBed.a(world, chunkcoordinates.a, chunkcoordinates.b, chunkcoordinates.c, 0);

            return chunkcoordinates1;
        }
        else { //World Spawn
            Material material = world.a(chunkcoordinates.a, chunkcoordinates.b, chunkcoordinates.c).o();
            Material material1 = world.a(chunkcoordinates.a, chunkcoordinates.b + 1, chunkcoordinates.c).o();
            boolean flag1 = !material.a() && !material.d();
            boolean flag2 = !material1.a() && !material1.d();

            return flag0 && flag1 && flag2 ? chunkcoordinates : null;
        }
    }

    public boolean bm() {
        return this.bB;
    }

    public boolean bJ() {
        return this.bB && this.b >= 100;
    }

    protected void b(int i0, boolean flag0) {
        byte b0 = this.ag.a(16);

        if (flag0) {
            this.ag.b(16, Byte.valueOf((byte) (b0 | 1 << i0)));
        }
        else {
            this.ag.b(16, Byte.valueOf((byte) (b0 & ~(1 << i0))));
        }

    }

    public void b(IChatComponent ichatcomponent) {
    }

    public ChunkCoordinates bL() {
        return this.c;
    }

    public boolean bM() {
        return this.d;
    }

    public void a(ChunkCoordinates chunkcoordinates, boolean flag0) {
        if (chunkcoordinates != null) {
            this.c = new ChunkCoordinates(chunkcoordinates);
            this.d = flag0;
        }
        else {
            this.c = null;
            this.d = false;
        }
    }

    public void a(StatBase statbase) {
        this.a(statbase, 1);
    }

    public void a(StatBase statbase, int i0) {
    }

    public void bj() {
        super.bj();
        this.a(StatList.r, 1);
        if (this.ao()) {
            this.a(0.8F);
        }
        else {
            this.a(0.2F);
        }
    }

    public void e(float f0, float f1) {
        double d0 = this.t;
        double d1 = this.u;
        double d2 = this.v;

        if (this.bF.b && this.n == null) {
            double d3 = this.x;
            float f2 = this.aR;

            this.aR = this.bF.a();
            super.e(f0, f1);
            this.x = d3 * 0.6D;
            this.aR = f2;
        }
        else {
            super.e(f0, f1);
        }

        this.k(this.t - d0, this.u - d1, this.v - d2);
    }

    public float bl() {
        return (float) this.a(SharedMonsterAttributes.d).e();
    }

    public void k(double d0, double d1, double d2) {
        if (this.n == null) {
            int i0;

            if (this.a(Material.h)) {
                i0 = Math.round(MathHelper.a(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);
                if (i0 > 0) {
                    this.a(StatList.m, i0);
                    this.a(0.015F * (float) i0 * 0.01F);
                }
            }
            else if (this.M()) {
                i0 = Math.round(MathHelper.a(d0 * d0 + d2 * d2) * 100.0F);
                if (i0 > 0) {
                    this.a(StatList.i, i0);
                    this.a(0.015F * (float) i0 * 0.01F);
                }
            }
            else if (this.h_()) {
                if (d1 > 0.0D) {
                    this.a(StatList.k, (int) Math.round(d1 * 100.0D));
                }
            }
            else if (this.E) {
                i0 = Math.round(MathHelper.a(d0 * d0 + d2 * d2) * 100.0F);
                if (i0 > 0) {
                    this.a(StatList.h, i0);
                    if (this.ao()) {
                        this.a(0.099999994F * (float) i0 * 0.01F);
                    }
                    else {
                        this.a(0.01F * (float) i0 * 0.01F);
                    }
                }
            }
            else {
                i0 = Math.round(MathHelper.a(d0 * d0 + d2 * d2) * 100.0F);
                if (i0 > 25) {
                    this.a(StatList.l, i0);
                }
            }
        }
    }

    private void l(double d0, double d1, double d2) {
        if (this.n != null) {
            int i0 = Math.round(MathHelper.a(d0 * d0 + d1 * d1 + d2 * d2) * 100.0F);

            if (i0 > 0) {
                if (this.n instanceof EntityMinecart) {
                    this.a(StatList.n, i0);
                    if (this.e == null) {
                        this.e = new ChunkCoordinates(MathHelper.c(this.t), MathHelper.c(this.u), MathHelper.c(this.v));
                    }
                    else if ((double) this.e.e(MathHelper.c(this.t), MathHelper.c(this.u), MathHelper.c(this.v)) >= 1000000.0D) {
                        this.a((StatBase) AchievementList.q, 1);
                    }
                }
                else if (this.n instanceof EntityBoat) {
                    this.a(StatList.o, i0);
                }
                else if (this.n instanceof EntityPig) {
                    this.a(StatList.p, i0);
                }
                else if (this.n instanceof EntityHorse) {
                    this.a(StatList.q, i0);
                }
            }
        }
    }

    protected void b(float f0) {
        if (!this.bF.c) {
            if (f0 >= 2.0F) {
                this.a(StatList.j, (int) Math.round((double) f0 * 100.0D));
            }

            super.b(f0);
        }
    }

    protected String o(int i0) {
        return i0 > 4 ? "game.player.hurt.fall.big" : "game.player.hurt.fall.small";
    }

    public void a(EntityLivingBase entitylivingbase) {
        if (entitylivingbase instanceof IMob) {
            this.a((StatBase) AchievementList.s);
        }
        int i0 = EntityList.a(entitylivingbase);
        EntityList.EntityEggInfo entitylist_entityegginfo = (EntityList.EntityEggInfo) EntityList.a.get(Integer.valueOf(i0));

        if (entitylist_entityegginfo != null) {
            this.a(entitylist_entityegginfo.d, 1);
        }

    }

    public void as() {
        if (!this.bF.b) {
            super.as();
        }
    }

    public ItemStack r(int i0) {
        return this.bn.d(i0);
    }

    public void v(int i0) {
        this.s(i0);
        int i1 = Integer.MAX_VALUE - this.bH;

        if (i0 > i1) {
            i0 = i1;
        }

        this.bI += (float) i0 / (float) this.bN();

        for (this.bH += i0; this.bI >= 1.0F; this.bI /= (float) this.bN()) {
            this.bI = (this.bI - 1.0F) * (float) this.bN();
            this.a(1);
        }
    }

    public void a(int i0) {
        // CanaryMod: LevelUpHook
        new LevelUpHook(((EntityPlayerMP) this).getPlayer()).call();
        //
        this.bG += i0;
        if (this.bG < 0) {
            this.bG = 0;
            this.bI = 0.0F;
            this.bH = 0;
        }

        if (i0 > 0 && this.bG % 5 == 0 && (float) this.h < (float) this.ab - 100.0F) {
            float f0 = this.bG > 30 ? 1.0F : (float) this.bG / 30.0F;

            this.p.a((Entity) this, "random.levelup", f0 * 0.75F, 1.0F);
            this.h = this.ab;
        }
    }

    public int bN() {
        return this.bG >= 30 ? 62 + (this.bG - 30) * 7 : (this.bG >= 15 ? 17 + (this.bG - 15) * 3 : 17);
    }

    public void a(float f0) {
        if (!this.bF.a) {
            if (!this.p.E) {
                this.bq.a(f0);
            }
        }
    }

    public FoodStats bO() {
        return this.bq;
    }

    public boolean g(boolean flag0) {
        return (flag0 || this.bq.c()) && !this.bF.a;
    }

    public boolean bP() {
        return this.aS() > 0.0F && this.aS() < this.aY();
    }

    public void a(ItemStack itemstack, int i0) {
        if (itemstack != this.f) {
            this.f = itemstack;
            this.g = i0;
            if (!this.p.E) {
                this.e(true);
            }
        }
    }

    public boolean d(int i0, int i1, int i2) {
        if (this.bF.e) {
            return true;
        }
        else {
            Block block = this.p.a(i0, i1, i2);

            if (block.o() != Material.a) {
                if (block.o().q()) {
                    return true;
                }

                if (this.bD() != null) {
                    ItemStack itemstack = this.bD();

                    if (itemstack.b(block) || itemstack.a(block) > 1.0F) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public boolean a(int i0, int i1, int i2, int i3, ItemStack itemstack) {
        return this.bF.e ? true : (itemstack != null ? itemstack.z() : false);
    }

    protected int e(EntityPlayer entityplayer) {
        if (this.p.N().b("keepInventory")) {
            return 0;
        }
        else {
            int i0 = this.bG * 7;

            return i0 > 100 ? 100 : i0;
        }
    }

    protected boolean aH() {
        return true;
    }

    public void a(EntityPlayer entityplayer, boolean flag0) {
        if (flag0) {
            this.bn.b(entityplayer.bn);
            this.g(entityplayer.aS());
            this.bq = entityplayer.bq;
            this.bG = entityplayer.bG;
            this.bH = entityplayer.bH;
            this.bI = entityplayer.bI;
            this.c(entityplayer.bB());
            this.ar = entityplayer.ar;
        }
        else if (this.p.N().b("keepInventory")) {
            this.bn.b(entityplayer.bn);
            this.bG = entityplayer.bG;
            this.bH = entityplayer.bH;
            this.bI = entityplayer.bI;
            this.c(entityplayer.bB());
        }

        this.a = entityplayer.a;
    }

    protected boolean g_() {
        return !this.bF.b;
    }

    public void q() {
    }

    public void a(WorldSettings.GameType worldsettings_gametype) {
    }

    public String b_() {
        return this.i.getName();
    }

    public World d() {
        return this.p;
    }

    public InventoryEnderChest bQ() {
        return this.a;
    }

    public ItemStack q(int i0) {
        return i0 == 0 ? this.bn.h() : this.bn.b[i0 - 1];
    }

    public ItemStack be() {
        return this.bn.h();
    }

    public void c(int i0, ItemStack itemstack) {
        this.bn.b[i0] = itemstack;
    }

    public ItemStack[] ak() {
        return this.bn.b;
    }

    public boolean aC() {
        return !this.bF.b;
    }

    public Scoreboard bS() {
        return this.p.W();
    }

    public Team bt() {
        return this.bS().i(this.b_());
    }

    public IChatComponent c_() {
        ChatComponentText chatcomponenttext = new ChatComponentText(ScorePlayerTeam.a(this.bt(), this.b_()));

        chatcomponenttext.b().a(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + this.b_() + " "));
        return chatcomponenttext;
    }

    public void m(float f0) {
        if (f0 < 0.0F) {
            f0 = 0.0F;
        }

        this.z().b(17, Float.valueOf(f0));
    }

    public float bs() {
        return this.z().d(17);
    }

    public static UUID a(GameProfile gameprofile) {
        UUID uuid = Util.b(gameprofile.getId());

        if (uuid == null) {
            uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + gameprofile.getName()).getBytes(Charsets.UTF_8));
        }

        return uuid;
    }

    public static enum EnumStatus {

        OK("OK", 0), NOT_POSSIBLE_HERE("NOT_POSSIBLE_HERE", 1), NOT_POSSIBLE_NOW("NOT_POSSIBLE_NOW", 2), TOO_FAR_AWAY("TOO_FAR_AWAY", 3), OTHER_PROBLEM("OTHER_PROBLEM", 4), NOT_SAFE("NOT_SAFE", 5);

        private static final EnumStatus[] $VALUES = new EnumStatus[]{ OK, NOT_POSSIBLE_HERE, NOT_POSSIBLE_NOW, TOO_FAR_AWAY, OTHER_PROBLEM, NOT_SAFE };

        private EnumStatus(String s0, int i0) {
        }

    }


    public static enum EnumChatVisibility {

        FULL("FULL", 0, 0, "options.chat.visibility.full"), SYSTEM("SYSTEM", 1, 1, "options.chat.visibility.system"), HIDDEN("HIDDEN", 2, 2, "options.chat.visibility.hidden");
        private static final EnumChatVisibility[] d = new EnumChatVisibility[values().length];
        private final int e;
        private final String f;

        private static final EnumChatVisibility[] g = new EnumChatVisibility[]{ FULL, SYSTEM, HIDDEN };

        private EnumChatVisibility(String i3, int i4, int entityplayer_enumchatvisibility, String s1) {
            this.e = entityplayer_enumchatvisibility;
            this.f = s1;
        }

        public int a() {
            return this.e;
        }

        public static EnumChatVisibility a(int aentityplayer_enumchatvisibility) {
            return d[aentityplayer_enumchatvisibility % d.length];
        }

        static {
            EnumChatVisibility[] aentityplayer_enumchatvisibility = values();
            int i3 = aentityplayer_enumchatvisibility.length;

            for (int i4 = 0; i4 < i3; ++i4) {
                EnumChatVisibility entityplayer_enumchatvisibility = aentityplayer_enumchatvisibility[i4];

                d[entityplayer_enumchatvisibility.e] = entityplayer_enumchatvisibility;
            }

        }
    }

    // CanaryMod
    // Start: Custom XP methods
    public void addXP(int amount) {
        this.s(amount);
        updateXP();
    }

    public void removeXP(int rXp) {
        if (rXp > this.bH) { // Don't go below 0
            rXp = this.bH;
        }

        this.bH -= (float) rXp / (float) this.bN();

        // Inverse of for loop in this.t(int)
        for (this.bH -= rXp; this.bI < 0.0F; this.bI = this.bI / this.bN() + 1.0F) {
            this.bI *= this.bN();
            this.a(-1);
        }
        updateXP();
    }

    public void setXP(int i) {
        if (i < this.bG) {
            this.removeXP(this.bG - i);
        }
        else {
            this.w(i - this.bG);
        }
        updateXP();
    }

    public void recalculateXP() {
        this.bI = this.bH / (float) this.bN();
        this.bG = 0;

        while (this.bI >= 1.0F) {
            this.bI = (this.bI - 1.0F) * this.bN();
            this.bH++;
            this.bI /= this.bN();
        }

        if (this instanceof EntityPlayerMP) {
            updateLevels();
            updateXP();
        }
    }

    private void updateXP() {
        CanaryPlayer player = ((CanaryPlayer) getCanaryEntity());
        S1FPacketSetExperience packet = new S1FPacketSetExperience(this.bI, this.bH, this.bG);

        player.sendPacket(new CanaryPacket(packet));
    }

    private void updateLevels() {
        CanaryPlayer player = ((CanaryPlayer) getCanaryEntity());
        S06PacketUpdateHealth packet = new S06PacketUpdateHealth(((CanaryPlayer) getCanaryEntity()).getHealth(), ((CanaryPlayer) getCanaryEntity()).getHunger(), ((CanaryPlayer) getCanaryEntity()).getExhaustionLevel());

        player.sendPacket(new CanaryPacket(packet));
    }

    // End: Custom XP methods
    // Start: Inventory getters
    public PlayerInventory getPlayerInventory() {
        return new CanaryPlayerInventory(bn);
    }

    public EnderChestInventory getEnderChestInventory() {
        return new CanaryEnderChestInventory(a, getCanaryHuman());
    }

    // End: Inventory getters

    // Start: Custom Display Name
    public String getDisplayName() {
        return metadata.getString("CustomName").isEmpty() ? this.b_() : metadata.getString("CustomName");
    }

    public void setDisplayName(String name) {
        metadata.put("CustomName", name != null ? name : "");
    }

    // End: Custom Display Name

    /**
     * Returns a respawn location for this player.
     * Null if there is no explicitly set respawn location
     *
     * @return
     */
    public Location getRespawnLocation() {
        if (this.c != null) {
            if (respawnWorld == null || respawnWorld.isEmpty()) {
                respawnWorld = getCanaryWorld().getFqName();
            }
            return new Location(Canary.getServer().getWorld(respawnWorld), c.a, c.b, c.c, 0, 0);
        }
        return null;
    }

    public void setRespawnLocation(Location l) {
        if (l == null) {
            c = null;
            respawnWorld = null;
            return;
        }
        if (c == null) {
            c = new ChunkCoordinates();
        }
        c.a = l.getBlockX();
        c.b = l.getBlockY();
        c.c = l.getBlockZ();
        respawnWorld = l.getWorld().getFqName();
    }

    public String getFirstJoined() {
        return metadata.getString("FirstJoin");
    }

    public long getTimePlayed() {
        return metadata.getLong("TimePlayed") + (ToolBox.getUnixTimestamp() - currentSessionStart);
    }

    public String getPreviousIP() {
        return metadata.getString("PreviousIP");
    }

    public void saveMeta() {
        metadata.put("TimePlayed", metadata.getLong("TimePlayed") + (ToolBox.getUnixTimestamp() - currentSessionStart));
        currentSessionStart = ToolBox.getUnixTimestamp(); // When saving, reset the start time so there isnt a duplicate addition of time stored
    }

    public CanaryHuman getCanaryHuman() {
        return (CanaryHuman) entity;
    }

    public void initializeNewMeta() {
        if (metadata == null) {
            metadata = new CanaryCompoundTag();
            metadata.put("FirstJoin", DateUtils.longToDateTime(System.currentTimeMillis()));
            metadata.put("LastJoin", DateUtils.longToDateTime(System.currentTimeMillis()));
            metadata.put("TimePlayed", 1L); // Initialize to 1
        }
    }
}

