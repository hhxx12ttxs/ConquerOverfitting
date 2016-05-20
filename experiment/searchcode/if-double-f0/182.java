package net.minecraft.network;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.canarymod.Canary;
import net.canarymod.LineTracer;
import net.canarymod.ToolBox;
import net.canarymod.api.CanaryNetServerHandler;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.inventory.slot.ButtonPress;
import net.canarymod.api.inventory.slot.GrabMode;
import net.canarymod.api.inventory.slot.SecondarySlotType;
import net.canarymod.api.inventory.slot.SlotHelper;
import net.canarymod.api.inventory.slot.SlotType;
import net.canarymod.api.world.blocks.BlockFace;
import net.canarymod.api.world.blocks.CanaryBlock;
import net.canarymod.api.world.position.Location;
import net.canarymod.config.Configuration;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.hook.player.DisconnectionHook;
import net.canarymod.hook.player.KickHook;
import net.canarymod.hook.player.PlayerArmSwingHook;
import net.canarymod.hook.player.PlayerMoveHook;
import net.canarymod.hook.player.SignChangeHook;
import net.canarymod.hook.player.SlotClickHook;
import net.canarymod.hook.player.TeleportHook;
import net.minecraft.block.material.Material;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.network.play.server.S3APacketTabComplete;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.visualillusionsent.utils.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

public class NetHandlerPlayServer implements INetHandlerPlayServer {

    private static final Logger c = LogManager.getLogger();
    public final INetworkManager a;
    public final MinecraftServer d;// private to public
    public EntityPlayerMP b;
    private int e;
    private int f;
    private boolean g;
    private int h;
    private long i;
    private static Random j = new Random();
    private long k;
    private int l;
    private int m;
    private IntHashMap n = new IntHashMap();
    private double o;
    private double p;
    private double q;
    private boolean r = true;

    // CanaryMod
    protected CanaryNetServerHandler serverHandler;
    private final String lastJoin = DateUtils.longToDateTime(System.currentTimeMillis());
    //

    public NetHandlerPlayServer(MinecraftServer minecraftserver, INetworkManager inetworkmanager, EntityPlayerMP entityplayermp) {
        this.d = minecraftserver;
        this.a = inetworkmanager;
        inetworkmanager.a((INetHandler) this);
        this.b = entityplayermp;
        entityplayermp.a = this;
        serverHandler = new CanaryNetServerHandler(this);
    }

    public void a() {
        this.g = false;
        ++this.e;
        this.d.a.a("keepAlive");
        if ((long) this.e - this.k > 40L) {
            this.k = (long) this.e;
            this.i = this.d();
            this.h = (int) this.i;
            this.a((Packet) (new S00PacketKeepAlive(this.h)));
        }

        if (this.l > 0) {
            --this.l;
        }

        if (this.m > 0) {
            --this.m;
        }

        this.d.a.c("playerTick");
        this.d.a.b();
    }

    public INetworkManager b() {
        return this.a;
    }

    public void c(String s0) {
        // CanaryMod: KickHook
        new KickHook(serverHandler.getUser(), Canary.getServer(), s0).call();
        // CanaryMod: Forward to kickNoHook
        this.kickNoHook(s0);
    }

    public void kickNoHook(String s0) {
        final ChatComponentText chatcomponenttext = new ChatComponentText(s0);
        this.a.a(new S40PacketDisconnect(chatcomponenttext), new GenericFutureListener[]{ new GenericFutureListener() {
            public void operationComplete(Future future) {
                NetHandlerPlayServer.this.a.a((IChatComponent) chatcomponenttext);
            }
        }
        });
        this.a.g();
    }

    public void a(C0CPacketInput c0cpacketinput) {
        this.b.a(c0cpacketinput.c(), c0cpacketinput.d(), c0cpacketinput.e(), c0cpacketinput.f());
    }

    public void a(C03PacketPlayer c03packetplayer) {
        WorldServer worldserver = (WorldServer) this.b.getCanaryWorld().getHandle(); // this.d.a(this.b.aq);

        this.g = true;
        if (!this.b.j) {
            //CanaryMod: Don't idle-kick when only moving (this was missing here)
            this.b.w();
            double d0;

            if (!this.r) {
                d0 = c03packetplayer.d() - this.p;
                if (c03packetplayer.c() == this.o && d0 * d0 < 0.01D && c03packetplayer.e() == this.q) {
                    this.r = true;
                }
            }

            // CanaryMod: PlayerMoveHook
            Player player = this.b.getPlayer();
            // Need to use floorToBlock or the flooring doesnt come out right...
            if (ToolBox.floorToBlock(o) != ToolBox.floorToBlock(player.getX()) || ToolBox.floorToBlock(p) != ToolBox.floorToBlock(player.getY()) || ToolBox.floorToBlock(q) != ToolBox.floorToBlock(player.getZ())) {
                Location from = new Location(player.getWorld(), o, p, q, player.getPitch(), player.getRotation());// Remember rotation and pitch are swapped in Location constructor...
                PlayerMoveHook hook = (PlayerMoveHook) new PlayerMoveHook(player, from, player.getLocation()).call();
                if (hook.isCanceled()) {
                    // Return the player to their previous position gracefully, hopefully bypassing the TeleportHook and not going derp.
                    this.b.a.a(new S08PacketPlayerPosLook(from.getX(), from.getY() + 1.6200000047683716D, from.getZ(), from.getRotation(), from.getPitch(), this.b.E));
                    this.b.b(from.getX(), from.getY(), from.getZ()); // correct position server side to, or get BoUnCy
                    return;
                }
            }
            //

            if (this.r) {
                double d1;
                double d2;
                double d3;

                if (this.b.n != null) {
                    float f0 = this.b.z;
                    float f1 = this.b.A;

                    this.b.n.ac();
                    d1 = this.b.t;
                    d2 = this.b.u;
                    d3 = this.b.v;
                    if (c03packetplayer.k()) {
                        f0 = c03packetplayer.g();
                        f1 = c03packetplayer.h();
                    }

                    this.b.E = c03packetplayer.i();
                    this.b.i();
                    this.b.W = 0.0F;
                    this.b.a(d1, d2, d3, f0, f1);
                    if (this.b.n != null) {
                        this.b.n.ac();
                    }

                    this.d.af().d(this.b);
                    if (this.r) {
                        this.o = this.b.t;
                        this.p = this.b.u;
                        this.q = this.b.v;
                    }

                    worldserver.g(this.b);
                    return;
                }

                if (this.b.bm()) {
                    this.b.i();
                    this.b.a(this.o, this.p, this.q, this.b.z, this.b.A);
                    worldserver.g(this.b);
                    return;
                }

                d0 = this.b.u;
                this.o = this.b.t;
                this.p = this.b.u;
                this.q = this.b.v;
                d1 = this.b.t;
                d2 = this.b.u;
                d3 = this.b.v;
                float f2 = this.b.z;
                float f3 = this.b.A;

                if (c03packetplayer.j() && c03packetplayer.d() == -999.0D && c03packetplayer.f() == -999.0D) {
                    c03packetplayer.a(false);
                }

                double d4;

                if (c03packetplayer.j()) {
                    d1 = c03packetplayer.c();
                    d2 = c03packetplayer.d();
                    d3 = c03packetplayer.e();
                    d4 = c03packetplayer.f() - c03packetplayer.d();
                    if (!this.b.bm() && (d4 > 1.65D || d4 < 0.1D)) {
                        this.c("Illegal stance");
                        c.warn(this.b.b_() + " had an illegal stance: " + d4);
                        return;
                    }

                    if (Math.abs(c03packetplayer.c()) > 3.2E7D || Math.abs(c03packetplayer.e()) > 3.2E7D) {
                        this.c("Illegal position");
                        return;
                    }
                }

                if (c03packetplayer.k()) {
                    f2 = c03packetplayer.g();
                    f3 = c03packetplayer.h();
                }

                this.b.i();
                this.b.W = 0.0F;
                this.b.a(this.o, this.p, this.q, f2, f3);
                if (!this.r) {
                    return;
                }

                d4 = d1 - this.b.t;
                double d5 = d2 - this.b.u;
                double d6 = d3 - this.b.v;
                double d7 = Math.min(Math.abs(d4), Math.abs(this.b.w));
                double d8 = Math.min(Math.abs(d5), Math.abs(this.b.x));
                double d9 = Math.min(Math.abs(d6), Math.abs(this.b.y));
                double d10 = d7 * d7 + d8 * d8 + d9 * d9;

                if (d10 > 100.0D && (!this.d.L() || !this.d.K().equals(this.b.b_()))) {
                    c.warn(this.b.b_() + " moved too quickly! " + d4 + "," + d5 + "," + d6 + " (" + d7 + ", " + d8 + ", " + d9 + ")");
                    this.a(this.o, this.p, this.q, this.b.z, this.b.A, this.b.p.getCanaryWorld().getType().getId(), this.b.p.getCanaryWorld().getName(), TeleportHook.TeleportCause.MOVEMENT);
                    return;
                }

                float f4 = 0.0625F;
                boolean flag0 = worldserver.a(this.b, this.b.D.c().e((double) f4, (double) f4, (double) f4)).isEmpty();

                if (this.b.E && !c03packetplayer.i() && d5 > 0.0D) {
                    this.b.bj();
                }

                this.b.d(d4, d5, d6);
                this.b.E = c03packetplayer.i();
                this.b.k(d4, d5, d6);
                double d11 = d5;

                d4 = d1 - this.b.t;
                d5 = d2 - this.b.u;
                if (d5 > -0.5D || d5 < 0.5D) {
                    d5 = 0.0D;
                }

                d6 = d3 - this.b.v;
                d10 = d4 * d4 + d5 * d5 + d6 * d6;
                boolean flag1 = false;

                if (d10 > 0.0625D && !this.b.bm() && !this.b.c.d()) {
                    flag1 = true;
                    c.warn(this.b.b_() + " moved wrongly!");
                }

                this.b.a(d1, d2, d3, f2, f3);
                boolean flag2 = worldserver.a(this.b, this.b.D.c().e((double) f4, (double) f4, (double) f4)).isEmpty();

                if (flag0 && (flag1 || !flag2) && !this.b.bm()) {
                    this.a(this.o, this.p, this.q, f2, f3, b.getCanaryWorld().getType().getId(), b.getCanaryWorld().getName(), TeleportHook.TeleportCause.MOVEMENT);
                    return;
                }

                AxisAlignedBB axisalignedbb = this.b.D.c().b((double) f4, (double) f4, (double) f4).a(0.0D, -0.55D, 0.0D);

                // CanaryMod: check on flying capability instead of mode
                // moved allow-flight to per-world config
                if (!Configuration.getWorldConfig(worldserver.getCanaryWorld().getFqName()).isFlightAllowed()) { // CanaryMod: Check if flight is allowed
                    if (!worldserver.c(axisalignedbb) && !this.b.c.d() && !(player.canIgnoreRestrictions() || player.isAdmin())) { // on ground | mode | admin
                        if (d11 >= -0.03125D) {
                            ++this.f;
                            if (this.f > 80) {
                                c.warn(this.b.b_() + " was kicked for floating too long!");
                                this.c("Flying is not enabled on this server");
                                return;
                            }
                        }
                    }
                    else {
                        this.f = 0;
                    }
                }

                this.b.E = c03packetplayer.i();
                this.d.af().d(this.b);
                this.b.b(this.b.u - d0, c03packetplayer.i());
            }
            else if (this.e % 20 == 0) {
                this.a(this.o, this.p, this.q, this.b.z, this.b.A, this.b.p.getCanaryWorld().getType().getId(), this.b.p.getCanaryWorld().getName(), TeleportHook.TeleportCause.MOVEMENT);
            }
        }
    }

    public void a(double d0, double d1, double d2, float f0, float f1, int dimension, String world, TeleportHook.TeleportCause cause) {
        // CanaryMod: TeleportHook
        net.canarymod.api.world.World dim = Canary.getServer().getWorldManager().getWorld(world, net.canarymod.api.world.DimensionType.fromId(dimension), true);
        Location location = new Location(dim, d0, d1, d2, f1, f0); // Remember rotation and pitch are swapped in Location constructor...
        TeleportHook hook = (TeleportHook) new TeleportHook(b.getPlayer(), location, cause).call();
        if (hook.isCanceled()) {
            return;
        }
        //
        this.r = false;
        this.o = d0;
        this.p = d1;
        this.q = d2;
        this.b.a(d0, d1, d2, f0, f1);
        this.b.a.a((Packet) (new S08PacketPlayerPosLook(d0, d1 + 1.6200000047683716D, d2, f0, f1, false)));
    }

    @Override
    public void a(C07PacketPlayerDigging c07packetplayerdigging) {
        WorldServer worldserver = (WorldServer) this.b.getCanaryWorld().getHandle(); // this.d.a(this.b.aq);

        this.b.w();
        if (c07packetplayerdigging.g() == 4) {
            this.b.a(false);
        }
        else if (c07packetplayerdigging.g() == 3) {
            this.b.a(true);
        }
        else if (c07packetplayerdigging.g() == 5) {
            this.b.by();
        }
        else {
            boolean flag0 = false;

            if (c07packetplayerdigging.g() == 0) {
                flag0 = true;
            }

            if (c07packetplayerdigging.g() == 1) {
                flag0 = true;
            }

            if (c07packetplayerdigging.g() == 2) {
                flag0 = true;
            }

            int i0 = c07packetplayerdigging.c();
            int i1 = c07packetplayerdigging.d();
            int i2 = c07packetplayerdigging.e();

            if (flag0) {
                double d0 = this.b.t - ((double) i0 + 0.5D);
                double d1 = this.b.u - ((double) i1 + 0.5D) + 1.5D;
                double d2 = this.b.v - ((double) i2 + 0.5D);
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (d3 > 36.0D) {
                    return;
                }

                if (i1 >= this.d.ad()) {
                    return;
                }
            }

            // CanaryMod: NOTE - Hook calls moved into the ItemInWorldManager
            if (c07packetplayerdigging.g() == 0) {
                if ((!this.d.a(worldserver, i0, i1, i2, this.b) || this.b.getPlayer().hasPermission("canary.world.spawnbuild")) && this.b.getPlayer().canBuild()) {
                    this.b.c.a(i0, i1, i2, c07packetplayerdigging.f());
                }
                else {
                    this.b.a.a((Packet) (new S23PacketBlockChange(i0, i1, i2, worldserver)));
                }
            }
            else if (c07packetplayerdigging.g() == 2) {
                this.b.c.a(i0, i1, i2);
                if (worldserver.a(i0, i1, i2).o() != Material.a) {
                    this.b.a.a((Packet) (new S23PacketBlockChange(i0, i1, i2, worldserver)));
                }
            }
            else if (c07packetplayerdigging.g() == 1) {
                this.b.c.c(i0, i1, i2);
                if (worldserver.a(i0, i1, i2).o() != Material.a) {
                    this.b.a.a((Packet) (new S23PacketBlockChange(i0, i1, i2, worldserver)));
                }
            }
            //
        }
    }

    private CanaryBlock lastRightClicked;

    @Override
    public void a(C08PacketPlayerBlockPlacement c08packetplayerblockplacement) {
        WorldServer worldserver = (WorldServer) this.b.getCanaryWorld().getHandle(); // this.d.a(this.c.ar);
        ItemStack itemstack = this.b.bn.h();
        boolean flag0 = false;
        int i0 = c08packetplayerblockplacement.c();
        int i1 = c08packetplayerblockplacement.d();
        int i2 = c08packetplayerblockplacement.e();
        int i3 = c08packetplayerblockplacement.f();

        this.b.w();

        // CanaryMod: BlockRightClick/ItemUse
        CanaryBlock blockClicked = (CanaryBlock) worldserver.getCanaryWorld().getBlockAt(i0, i1, i2);
        blockClicked.setFaceClicked(BlockFace.fromByte((byte) i3));

        if (i3 == 255) {
            // item use: use last right clicked block
            blockClicked = lastRightClicked;
            lastRightClicked = null;
        } else {
            lastRightClicked = blockClicked;
        }

        if (c08packetplayerblockplacement.f() == 255) {
            if (itemstack == null) {
                return;
            }

            blockClicked = blockClicked != null ? blockClicked : new CanaryBlock((short) 0, (short) 0, ToolBox.floorToBlock(this.o), ToolBox.floorToBlock(this.p), ToolBox.floorToBlock(this.q), this.b.getCanaryWorld());
            //
            this.b.c.itemUsed(this.b.getPlayer(), worldserver, itemstack, blockClicked); // CanaryMod: Redirect through ItemInWorldManager.itemUsed
        }
        else if (c08packetplayerblockplacement.d() >= this.d.ad() - 1 && (c08packetplayerblockplacement.f() == 1 || c08packetplayerblockplacement.d() >= this.d.ad())) {
            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("build.tooHigh", new Object[]{ Integer.valueOf(this.d.ad()) });

            chatcomponenttranslation.b().a(EnumChatFormatting.RED);
            this.b.a.a((Packet) (new S02PacketChat(chatcomponenttranslation)));
            flag0 = true;
        }
        else {
            if (this.r && this.b.e((double) i0 + 0.5D, (double) i1 + 0.5D, (double) i2 + 0.5D) < 64.0D && (!this.d.a(worldserver, i0, i1, i2, this.b) || b.getPlayer().hasPermission("canary.world.spawnbuild")) && b.getPlayer().canBuild()) {
                // CanaryMod: BlockRightClicked
                BlockRightClickHook hook = (BlockRightClickHook) new BlockRightClickHook(b.getPlayer(), blockClicked).call();
                if (!hook.isCanceled()) {
                    this.b.c.a(this.b, worldserver, itemstack, i0, i1, i2, i3, c08packetplayerblockplacement.h(), c08packetplayerblockplacement.i(), c08packetplayerblockplacement.j());
                }
                // NOTE: calling the BlockChange packet here and returning was causing ghosting (fake visible blocks)
            }
            //

            flag0 = true;
        }

        if (flag0) {
            this.b.a.a((Packet) (new S23PacketBlockChange(i0, i1, i2, worldserver)));
            if (i3 == 0) {
                --i1;
            }

            if (i3 == 1) {
                ++i1;
            }

            if (i3 == 2) {
                --i2;
            }

            if (i3 == 3) {
                ++i2;
            }

            if (i3 == 4) {
                --i0;
            }

            if (i3 == 5) {
                ++i0;
            }

            this.b.a.a((Packet) (new S23PacketBlockChange(i0, i1, i2, worldserver)));
        }

        itemstack = this.b.bn.h();
        if (itemstack != null && itemstack.b == 0) {
            this.b.bn.a[this.b.bn.c] = null;
            itemstack = null;
        }

        if (itemstack == null || itemstack.n() == 0) {
            this.b.h = true;
            this.b.bn.a[this.b.bn.c] = ItemStack.b(this.b.bn.a[this.b.bn.c]);
            Slot slot = this.b.bp.a((IInventory) this.b.bn, this.b.bn.c);

            this.b.bp.b();
            this.b.h = false;
            if (!ItemStack.b(this.b.bn.h(), c08packetplayerblockplacement.g())) {
                this.a((Packet) (new S2FPacketSetSlot(this.b.bp.d, slot.g, this.b.bn.h())));
            }
        }
    }

    @Override
    public void a(IChatComponent ichatcomponent) {
        c.info(this.b.b_() + " lost connection: " + ichatcomponent.e());
        this.b.storeLastJoin(lastJoin); // Respawning could push this around, so save at a true disconnect
        this.d.au();
        ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("multiplayer.player.left", new Object[]{ this.b.c_() });
        chatcomponenttranslation.b().a(EnumChatFormatting.YELLOW);

        // CanaryMod: DisconnectionHook
        DisconnectionHook hook = (DisconnectionHook) new DisconnectionHook(this.b.getPlayer(), ichatcomponent.e(), chatcomponenttranslation.e()).call();
        if (!hook.isHidden()) {
            this.d.af().a((IChatComponent) chatcomponenttranslation);
        }
        //
        this.b.n();
        this.d.af().e(this.b);
        // CanaryMod unregester Custom Payload registrations
        Canary.channels().unregisterClientAll(serverHandler);
        // End
        if (this.d.L() && this.b.b_().equals(this.d.K())) {
            c.info("Stopping singleplayer server as player logged out");
            this.d.q();
        }
    }

    public void a(final Packet packet) {
        if (packet instanceof S02PacketChat) {
            S02PacketChat s02packetchat = (S02PacketChat) packet;
            EntityPlayer.EnumChatVisibility entityplayer_enumchatvisibility = this.b.v();

            if (entityplayer_enumchatvisibility == EntityPlayer.EnumChatVisibility.HIDDEN) {
                return;
            }

            if (entityplayer_enumchatvisibility == EntityPlayer.EnumChatVisibility.SYSTEM && !s02packetchat.d()) {
                return;
            }
        }

        try {
            this.a.a(packet, new GenericFutureListener[0]);
        }
        catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Sending packet");
            CrashReportCategory crashreportcategory = crashreport.a("Packet being sent");

            crashreportcategory.a("Packet class", new Callable() {

                public String call() {
                    return packet.getClass().getCanonicalName();
                }
            });
            throw new ReportedException(crashreport);
        }
    }

    public void a(C09PacketHeldItemChange c09packethelditemchange) {
        if (c09packethelditemchange.c() >= 0 && c09packethelditemchange.c() < InventoryPlayer.i()) {
            this.b.bn.c = c09packethelditemchange.c();
            this.b.w();
        }
        else {
            c.warn(this.b.b_() + " tried to set an invalid carried item");
        }
    }

    @Override
    public void a(C01PacketChatMessage c01packetchatmessage) {
        /* Diff visibility funkyness
        if (this.b.v() == EntityPlayer.EnumChatVisibility.HIDDEN) {
            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("chat.cannotSend", new Object[0]);

            chatcomponenttranslation.b().a(EnumChatFormatting.RED);
            this.a((Packet) (new S02PacketChat(chatcomponenttranslation)));
        } else {
            this.b.w();
            String s0 = c01packetchatmessage.c();

            s0 = StringUtils.normalizeSpace(s0);

            for (int i0 = 0; i0 < s0.length(); ++i0) {
                if (!ChatAllowedCharacters.a(s0.charAt(i0))) {
                    this.c("Illegal characters in chat");
                    return;
                }
            }

            if (s0.startsWith("/")) {
                this.d(s0);
            } else {
                ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation("chat.type.text", new Object[] { this.b.c_(), s0});

                this.d.af().a(chatcomponenttranslation1, false);
            }

            this.l += 20;
            if (this.l > 200 && !this.d.af().d(this.b.b_())) {
                this.c("disconnect.spam");
            }

        }
        }*/
        // CanaryMod: Re-route to Player chat
        if (this.b.v() == EntityPlayer.EnumChatVisibility.HIDDEN) {
            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("chat.cannotSend", new Object[0]);

            chatcomponenttranslation.b().a(EnumChatFormatting.RED);
            this.a((Packet) (new S02PacketChat(chatcomponenttranslation)));
            return;
        }
        // Reuse Anti-Spam but implement our config into the system
        this.l += 20;
        boolean op = this.d.af().d(this.b.b_()), ignore = this.b.getPlayer().canIgnoreRestrictions(); // OP or Ignores restrictions
        String spamProLvl = Configuration.getServerConfig().getSpamProtectionLevel();
        if (spamProLvl.toLowerCase().equals("all") || (spamProLvl.toLowerCase().equals("default") && !(op || ignore))) {
            if (this.l > 200) {
                this.c("disconnect.spam");
                return;
            }
        }
        this.b.w(); //Not Idle
        this.b.getPlayer().chat(c01packetchatmessage.c());
    }

    private void d(String s0) {
        this.d.H().a(this.b, s0);
    }

    @Override
    public void a(C0APacketAnimation c0apacketanimation) {
        this.b.w();
        if (c0apacketanimation.d() == 1) {
            // CanaryMod: Arm Swinging
            new PlayerArmSwingHook(this.b.getPlayer()).call();
            this.b.ba();
        }
    }

    public void a(C0BPacketEntityAction c0bpacketentityaction) {
        this.b.w();
        if (c0bpacketentityaction.d() == 1) {
            this.b.b(true);
        }
        else if (c0bpacketentityaction.d() == 2) {
            this.b.b(false);
        }
        else if (c0bpacketentityaction.d() == 4) {
            this.b.c(true);
        }
        else if (c0bpacketentityaction.d() == 5) {
            this.b.c(false);
        }
        else if (c0bpacketentityaction.d() == 3) {
            this.b.a(false, true, true);
            this.r = false;
        }
        else if (c0bpacketentityaction.d() == 6) {
            if (this.b.n != null && this.b.n instanceof EntityHorse) {
                ((EntityHorse) this.b.n).w(c0bpacketentityaction.e());
            }
        }
        else if (c0bpacketentityaction.d() == 7 && this.b.n != null && this.b.n instanceof EntityHorse) {
            ((EntityHorse) this.b.n).g(this.b);
        }

    }

    public void a(C02PacketUseEntity c02packetuseentity) {
        WorldServer worldserver = (WorldServer) this.b.getCanaryWorld().getHandle(); // this.d.a(this.b.aq);
        Entity entity = c02packetuseentity.a((World) worldserver);

        this.b.w();
        if (entity != null) {
            boolean flag0 = this.b.o(entity);
            double d0 = 36.0D;

            if (!flag0) {
                d0 = 9.0D;
            }

            if (this.b.e(entity) < d0) {
                if (c02packetuseentity.c() == C02PacketUseEntity.Action.INTERACT) {
                    this.b.p(entity);
                }
                else if (c02packetuseentity.c() == C02PacketUseEntity.Action.ATTACK) {
                    if (entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityArrow || entity == this.b) {
                        this.c("Attempting to attack an invalid entity");
                        this.d.f("Player " + this.b.b_() + " tried to attack an invalid entity");
                        return;
                    }

                    this.b.q(entity);
                }
            }
        }
    }

    public void a(C16PacketClientStatus c16packetclientstatus) {
        this.b.w();
        C16PacketClientStatus.EnumState c16packetclientstatus_enumstate = c16packetclientstatus.c();

        switch (NetHandlerPlayServer.SwitchEnumState.a[c16packetclientstatus_enumstate.ordinal()]) {
            case 1:
                if (this.b.j) {
                    this.b = this.d.af().a(this.b, 0, true);
                }
                else if (this.b.r().M().t()) {
                    if (this.d.L() && this.b.b_().equals(this.d.K())) {
                        this.b.a.c("You have died. Game over, man, it\'s game over!");
                        this.d.S();
                    }
                    else {
                        // CanaryMod use our Ban System instead
                        Canary.bans().issueBan(this.b.getPlayer(), "Death in Hardcore");
                        this.b.a.c("You have died. Game over, man, it\'s game over!");
                    }
                }
                else {
                    if (this.b.aS() > 0.0F) {
                        return;
                    }

                    this.b = this.d.af().a(this.b, 0, false);
                }
                break;

            case 2:
                this.b.x().a(this.b);
                break;

            case 3:
                this.b.a((StatBase) AchievementList.f);
        }

    }

    public void a(C0DPacketCloseWindow c0dpacketclosewindow) {
        this.b.m();
    }

    public void a(C0EPacketClickWindow c0epacketclickwindow) {
        this.b.w();
        if (this.b.bp.d == c0epacketclickwindow.c() && this.b.bp.c(this.b)) {
            // CanaryMod: SlotClick
            ItemStack itemstack = c0epacketclickwindow.d() > -1 ? this.b.bp.a(c0epacketclickwindow.d()).d() : null;
            SlotType slot_type = SlotHelper.getSlotType(this.b.bp, c0epacketclickwindow.d());
            SecondarySlotType finer_slot = SlotHelper.getSpecificSlotType(this.b.bp, c0epacketclickwindow.d());
            GrabMode grab_mode = GrabMode.fromInt(c0epacketclickwindow.h());
            ButtonPress mouse_click = ButtonPress.matchButton(grab_mode, c0epacketclickwindow.e(), c0epacketclickwindow.d());
            SlotClickHook sch = (SlotClickHook) new SlotClickHook(this.b.getPlayer(), this.b.bp.getInventory(), itemstack != null ? itemstack.getCanaryItem() : null, slot_type, finer_slot, grab_mode, mouse_click, (short) c0epacketclickwindow.d(), c0epacketclickwindow.f()).call();
            if (sch.isCanceled()) {
                if (sch.doUpdate()) {
                    if (c0epacketclickwindow.h() == 0) {
                        this.b.bp.updateSlot(c0epacketclickwindow.d());
                        this.b.updateSlot(-1, -1, this.b.bn.o());
                    }
                    else {
                        ArrayList arraylist = new ArrayList();

                        for (int i = 0; i < this.b.bp.c.size(); ++i) {
                            arraylist.add(((Slot) this.b.bp.c.get(i)).d());
                        }

                        this.b.a(this.b.bp, arraylist);
                    }
                }
                return;
            }
            //
            itemstack = this.b.bp.a(c0epacketclickwindow.d(), c0epacketclickwindow.e(), c0epacketclickwindow.h(), this.b);

            if (ItemStack.b(c0epacketclickwindow.g(), itemstack)) {
                this.b.a.a((Packet) (new S32PacketConfirmTransaction(c0epacketclickwindow.c(), c0epacketclickwindow.f(), true)));
                this.b.h = true;
                this.b.bp.b();
                this.b.l();
                this.b.h = false;
            }
            else {
                this.n.a(this.b.bp.d, Short.valueOf(c0epacketclickwindow.f()));
                this.b.a.a((Packet) (new S32PacketConfirmTransaction(c0epacketclickwindow.c(), c0epacketclickwindow.f(), false)));
                this.b.bp.a(this.b, false);
                ArrayList arraylist = new ArrayList();

                for (int i0 = 0; i0 < this.b.bp.c.size(); ++i0) {
                    arraylist.add(((Slot) this.b.bp.c.get(i0)).d());
                }

                this.b.a(this.b.bp, (List) arraylist);
            }
        }
    }

    public void a(C11PacketEnchantItem c11packetenchantitem) {
        this.b.w();
        if (this.b.bp.d == c11packetenchantitem.c() && this.b.bp.c(this.b)) {
            this.b.bp.a((EntityPlayer) this.b, c11packetenchantitem.d());
            this.b.bp.b();
        }

    }

    public void a(C10PacketCreativeInventoryAction c10packetcreativeinventoryaction) {
        if (this.b.c.d()) {
            boolean flag0 = c10packetcreativeinventoryaction.c() < 0;
            ItemStack itemstack = c10packetcreativeinventoryaction.d();
            boolean flag1 = c10packetcreativeinventoryaction.c() >= 1 && c10packetcreativeinventoryaction.c() < 36 + InventoryPlayer.i();
            boolean flag2 = itemstack == null || itemstack.b() != null;
            boolean flag3 = itemstack == null || itemstack.k() >= 0 && itemstack.b <= 64 && itemstack.b > 0;

            if (flag1 && flag2 && flag3) {
                if (itemstack == null) {
                    this.b.bo.a(c10packetcreativeinventoryaction.c(), (ItemStack) null);
                }
                else {
                    this.b.bo.a(c10packetcreativeinventoryaction.c(), itemstack);
                }

                this.b.bo.a(this.b, true);
            }
            else if (flag0 && flag2 && flag3 && this.m < 200) {
                this.m += 20;
                EntityItem entityitem = this.b.a(itemstack, true);

                if (entityitem != null) {
                    entityitem.e();
                }
            }
        }
    }

    public void a(C0FPacketConfirmTransaction c0fpacketconfirmtransaction) {
        Short oshort = (Short) this.n.a(this.b.bp.d);

        if (oshort != null && c0fpacketconfirmtransaction.d() == oshort.shortValue() && this.b.bp.d == c0fpacketconfirmtransaction.c() && !this.b.bp.c(this.b)) {
            this.b.bp.a(this.b, true);
        }
    }

    public void a(C12PacketUpdateSign c12packetupdatesign) {
        this.b.w();
        WorldServer worldserver = (WorldServer) this.b.getCanaryWorld().getHandle(); // this.d.a(this.b.aq);

        if (worldserver.d(c12packetupdatesign.c(), c12packetupdatesign.d(), c12packetupdatesign.e())) {
            TileEntity tileentity = worldserver.o(c12packetupdatesign.c(), c12packetupdatesign.d(), c12packetupdatesign.e());

            if (tileentity instanceof TileEntitySign) {
                TileEntitySign tileentitysign = (TileEntitySign) tileentity;

                if (!tileentitysign.a() || tileentitysign.b() != this.b) {
                    this.d.f("Player " + this.b.b_() + " just tried to change non-editable sign");
                    return;
                }
            }

            int i0;
            int i1;

            for (i1 = 0; i1 < 4; ++i1) {
                boolean flag0 = true;

                if (c12packetupdatesign.f()[i1].length() > 15) {
                    flag0 = false;
                }
                else if (Configuration.getServerConfig().getStrictSignCharacterChecks()) { // CanaryMod: Check if we use strict characters on signs
                    for (i0 = 0; i0 < c12packetupdatesign.f()[i1].length(); ++i0) {
                        if (!ChatAllowedCharacters.a(c12packetupdatesign.f()[i1].charAt(i0))) {
                            if (c12packetupdatesign.f()[i1].charAt(i0) != '\u00A7') { // CanaryMod: Ignore Color Char
                                flag0 = false;
                            }
                        }
                    }
                }

                if (!flag0) {
                    c12packetupdatesign.f()[i1] = "!?";
                }
            }

            if (tileentity instanceof TileEntitySign) {
                i1 = c12packetupdatesign.c();
                int i2 = c12packetupdatesign.d();

                i0 = c12packetupdatesign.e();
                TileEntitySign tileentitysign1 = (TileEntitySign) tileentity;

                // CanaryMod: Copy the old line text
                String[] old = Arrays.copyOf(tileentitysign1.a, tileentitysign1.a.length);
                //
                System.arraycopy(c12packetupdatesign.f(), 0, tileentitysign1.a, 0, 4);
                // CanaryMod: SignChange Hook
                SignChangeHook hook = (SignChangeHook) new SignChangeHook(b.getPlayer(), tileentitysign1.getCanarySign()).call();
                if (hook.isCanceled()) {
                    System.arraycopy(old, 0, tileentitysign1.a, 0, 4); // Restore old text
                }
                //

                tileentitysign1.e();
                worldserver.g(i1, i2, i0);
            }
        }
    }

    public void a(C00PacketKeepAlive c00packetkeepalive) {
        if (c00packetkeepalive.c() == this.h) {
            int i0 = (int) (this.d() - this.i);

            this.b.i = (this.b.i * 3 + i0) / 4;
        }
    }

    private long d() {
        return System.nanoTime() / 1000000L;
    }

    public void a(C13PacketPlayerAbilities c13packetplayerabilities) {
        this.b.bF.b = c13packetplayerabilities.d() && this.b.bF.c;
    }

    public void a(C14PacketTabComplete c14packettabcomplete) {
        ArrayList arraylist = Lists.newArrayList();
        Iterator iterator = this.d.a(this.b, c14packettabcomplete.c()).iterator();

        while (iterator.hasNext()) {
            String s0 = (String) iterator.next();

            arraylist.add(s0);
        }

        this.b.a.a((Packet) (new S3APacketTabComplete((String[]) arraylist.toArray(new String[arraylist.size()]))));
    }

    public void a(C15PacketClientSettings c15packetclientsettings) {
        this.b.a(c15packetclientsettings);
    }

    public void a(C17PacketCustomPayload c17packetcustompayload) {
        ItemStack itemstack;
        ItemStack itemstack1;

        if ("MC|BEdit".equals(c17packetcustompayload.c())) {
            try {
                itemstack = (new PacketBuffer(Unpooled.wrappedBuffer(c17packetcustompayload.e()))).c();
                if (!ItemWritableBook.a(itemstack.q())) {
                    throw new IOException("Invalid book tag!");
                }

                itemstack1 = this.b.bn.h();
                if (itemstack.b() == Items.bA && itemstack.b() == itemstack1.b()) {
                    itemstack1.a("pages", (NBTBase) itemstack.q().c("pages", 8));
                }
            }
            catch (Exception exception) {
                c.error("Couldn\'t handle book info", exception);
            }
        }
        else if ("MC|BSign".equals(c17packetcustompayload.c())) {
            try {
                itemstack = (new PacketBuffer(Unpooled.wrappedBuffer(c17packetcustompayload.e()))).c();
                if (!ItemEditableBook.a(itemstack.q())) {
                    throw new IOException("Invalid book tag!");
                }

                itemstack1 = this.b.bn.h();
                if (itemstack.b() == Items.bB && itemstack1.b() == Items.bA) {
                    itemstack1.a("author", (NBTBase) (new NBTTagString(this.b.b_())));
                    itemstack1.a("title", (NBTBase) (new NBTTagString(itemstack.q().j("title"))));
                    itemstack1.a("pages", (NBTBase) itemstack.q().c("pages", 8));
                    itemstack1.a(Items.bB);
                }
            }
            catch (Exception exception1) {
                c.error("Couldn\'t sign book", exception1);
            }
        }
        else {
            DataInputStream datainputstream;
            int i0;

            if ("MC|TrSel".equals(c17packetcustompayload.c())) {
                try {
                    datainputstream = new DataInputStream(new ByteArrayInputStream(c17packetcustompayload.e()));
                    i0 = datainputstream.readInt();
                    Container container = this.b.bp;

                    if (container instanceof ContainerMerchant) {
                        ((ContainerMerchant) container).e(i0);
                    }
                }
                catch (Exception exception2) {
                    c.error("Couldn\'t select trade", exception2);
                }
            }
            else if ("MC|AdvCdm".equals(c17packetcustompayload.c())) {
                if (!this.d.ab()) {
                    this.b.a((IChatComponent) (new ChatComponentTranslation("advMode.notEnabled", new Object[0])));
                }
                else if (this.b.a(2, "") && this.b.bF.d) {
                    try {
                        PacketBuffer packetbuffer = new PacketBuffer(Unpooled.wrappedBuffer(c17packetcustompayload.e()));
                        byte b0 = packetbuffer.readByte();
                        CommandBlockLogic commandblocklogic = null;

                        if (b0 == 0) {
                            TileEntity tileentity = this.b.p.o(packetbuffer.readInt(), packetbuffer.readInt(), packetbuffer.readInt());

                            if (tileentity instanceof TileEntityCommandBlock) {
                                commandblocklogic = ((TileEntityCommandBlock) tileentity).a();
                            }
                        }
                        else if (b0 == 1) {
                            Entity entity = this.b.p.a(packetbuffer.readInt());

                            if (entity instanceof EntityMinecartCommandBlock) {
                                commandblocklogic = ((EntityMinecartCommandBlock) entity).e();
                            }
                        }

                        String s0 = packetbuffer.c(packetbuffer.readableBytes());

                        if (commandblocklogic != null) {
                            commandblocklogic.a(s0);
                            commandblocklogic.e();
                            this.b.a((IChatComponent) (new ChatComponentTranslation("advMode.setCommand.success", new Object[]{ s0 })));
                        }
                    }
                    catch (Exception exception3) {
                        c.error("Couldn\'t set command block", exception3);
                    }
                }
                else {
                    this.b.a((IChatComponent) (new ChatComponentTranslation("advMode.notAllowed", new Object[0])));
                }
            }
            else if ("MC|Beacon".equals(c17packetcustompayload.c())) {
                if (this.b.bp instanceof ContainerBeacon) {
                    try {
                        datainputstream = new DataInputStream(new ByteArrayInputStream(c17packetcustompayload.e()));
                        i0 = datainputstream.readInt();
                        int i1 = datainputstream.readInt();
                        ContainerBeacon containerbeacon = (ContainerBeacon) this.b.bp;
                        Slot slot = containerbeacon.a(0);

                        if (slot.e()) {
                            slot.a(1);
                            TileEntityBeacon tileentitybeacon = containerbeacon.e();

                            tileentitybeacon.d(i0);
                            tileentitybeacon.e(i1);
                            tileentitybeacon.e();
                        }
                    }
                    catch (Exception exception4) {
                        c.error("Couldn\'t set beacon", exception4);
                    }
                }
            }
            else if ("MC|ItemName".equals(c17packetcustompayload.c()) && this.b.bp instanceof ContainerRepair) {
                ContainerRepair containerrepair = (ContainerRepair) this.b.bp;

                if (c17packetcustompayload.e() != null && c17packetcustompayload.e().length >= 1) {
                    String s1 = ChatAllowedCharacters.a(new String(c17packetcustompayload.e(), Charsets.UTF_8));

                    if (s1.length() <= 30) {
                        containerrepair.a(s1);
                    }
                }
                else {
                    containerrepair.a("");
                }
            }
        }

        // CanaryMod: Custom Payload implementation!
        if ("REGISTER".equals(c17packetcustompayload.c())) {
            try {
                String channel = new String(c17packetcustompayload.e(), Charsets.UTF_8);
                for (String chan : channel.split("\0")) {
                    Canary.channels().registerClient(chan, this.serverHandler);
                }
                Canary.log.info(String.format("Player '%s' registered Custom Payload on channel(s) '%s'", this.b.getPlayer().getName(), Arrays.toString(channel.split("\0"))));
            }
            catch (Exception ex) {
                c.error("Error receiving 'C17PacketCustomPayload': " + ex.getMessage(), ex);
            }
        }
        else if ("UNREGISTER".equals(c17packetcustompayload.c())) {
            try {
                String channel = new String(c17packetcustompayload.e(), Charsets.UTF_8);
                Canary.channels().unregisterClient(channel, this.serverHandler);
                Canary.log.info(String.format("Player '%s' unregistered Custom Payload on channel '%s'", this.b.getPlayer().getName(), channel));
            }
            catch (Exception ex) {
                c.error("Error receiving 'C17PacketCustomPayload': " + ex.getMessage(), ex);
            }
        }
        else {
            try {
                Canary.channels().sendCustomPayloadToListeners(c17packetcustompayload.c(), c17packetcustompayload.e(), this.b.getPlayer());
            }
            catch (Exception ex) {
                c.error("Error receiving 'C17PacketCustomPayload': " + ex.getMessage(), ex);
            }
        }
        // CanaryMod: End
    }

    public void a(EnumConnectionState enumconnectionstate, EnumConnectionState enumconnectionstate1) {
        if (enumconnectionstate1 != EnumConnectionState.PLAY) {
            throw new IllegalStateException("Unexpected change in protocol!");
        }
    }

    static final class SwitchEnumState {
        static final int[] a = new int[C16PacketClientStatus.EnumState.values().length];

        static {
            try {
                a[C16PacketClientStatus.EnumState.PERFORM_RESPAWN.ordinal()] = 1;
            }
            catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                a[C16PacketClientStatus.EnumState.REQUEST_STATS.ordinal()] = 2;
            }
            catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                a[C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT.ordinal()] = 3;
            }
            catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

        }
    }

    /**
     * gets the CanaryNetServerHandler wrapper
     *
     * @return
     */
    public CanaryNetServerHandler getCanaryServerHandler() {
        return serverHandler;
    }
}

