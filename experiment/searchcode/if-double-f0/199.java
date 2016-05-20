package net.minecraft.server.management;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.canarymod.Canary;
import net.canarymod.ToolBox;
import net.canarymod.Translator;
import net.canarymod.api.CanaryConfigurationManager;
import net.canarymod.api.PlayerListEntry;
import net.canarymod.api.entity.living.humanoid.CanaryPlayer;
import net.canarymod.api.nbt.CanaryCompoundTag;
import net.canarymod.api.packet.CanaryPacket;
import net.canarymod.api.world.CanaryWorld;
import net.canarymod.api.world.DimensionType;
import net.canarymod.api.world.position.Location;
import net.canarymod.bansystem.Ban;
import net.canarymod.config.Configuration;
import net.canarymod.config.ServerConfiguration;
import net.canarymod.hook.player.ConnectionHook;
import net.canarymod.hook.player.PlayerListEntryHook;
import net.canarymod.hook.player.PlayerRespawnedHook;
import net.canarymod.hook.player.PlayerRespawningHook;
import net.canarymod.hook.player.PreConnectionHook;
import net.canarymod.hook.player.TeleportHook;
import net.canarymod.hook.system.ServerShutdownHook;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsFile;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.demo.DemoWorldManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class ServerConfigurationManager {

    private static final Logger d = LogManager.getLogger();
    private static final SimpleDateFormat e = new SimpleDateFormat("yyyy-MM-dd \'at\' HH:mm:ss z");
    private final MinecraftServer f;
    public final List a = new ArrayList();
    // private final BanList g = new BanList(new File("banned-players.txt"));
    // private final BanList h = new BanList(new File("banned-ips.txt"));
    private final Set i = new HashSet();
    private final Set j = new HashSet();
    private final Map k = Maps.newHashMap();
    private IPlayerFileData l;
    private boolean m;
    protected int b;
    protected int c;
    private WorldSettings.GameType n;
    private boolean o;
    private int p;

    // CanaryMod
    protected CanaryConfigurationManager configurationmanager;
    private HashMap<String, IPlayerFileData> playerFileData = new HashMap<String, IPlayerFileData>();

    //
    public ServerConfigurationManager(MinecraftServer minecraftserver) {
        this.f = minecraftserver;
        // this.g.a(false);
        // this.h.a(false);
        this.b = Configuration.getServerConfig().getMaxPlayers();
        configurationmanager = new CanaryConfigurationManager(this);
    }

    // XXX LOGIN
    public void a(INetworkManager inetworkmanager, EntityPlayerMP entityplayermp) {
        NBTTagCompound nbttagcompound = this.a(entityplayermp);
        CanaryWorld w;
        boolean firstTime = true;
        if (nbttagcompound != null) {
            w = (CanaryWorld) Canary.getServer().getWorldManager().getWorld(nbttagcompound.j("LevelName"), net.canarymod.api.world.DimensionType.fromId(nbttagcompound.f("Dimension")), true);
            firstTime = false;
        }
        else {
            w = (CanaryWorld) Canary.getServer().getDefaultWorld();
        }
        entityplayermp.a(w.getHandle());
        entityplayermp.c.a((WorldServer) entityplayermp.p);
        String s0 = "local";

        if (inetworkmanager.b() != null) {
            s0 = inetworkmanager.b().toString();
        }

        d.info(entityplayermp.b_() + "[" + s0 + "] logged in with entity id " + entityplayermp.y() + " at (" + entityplayermp.t + ", " + entityplayermp.u + ", " + entityplayermp.v + ")");
        // CanaryMod: Use world we got from players NBT data
        WorldServer worldserver = (WorldServer) w.getHandle();
        ChunkCoordinates chunkcoordinates = worldserver.J();

        this.a(entityplayermp, (EntityPlayerMP) null, worldserver);
        NetHandlerPlayServer nethandlerplayserver = new NetHandlerPlayServer(this.f, inetworkmanager, entityplayermp);

        nethandlerplayserver.a((Packet) (new S01PacketJoinGame(entityplayermp.y(), entityplayermp.c.b(), worldserver.M().t(), worldserver.t.i, worldserver.r, this.l(), worldserver.M().u())));
        nethandlerplayserver.a((Packet) (new S3FPacketCustomPayload("MC|Brand", this.p().getServerModName().getBytes(Charsets.UTF_8))));
        nethandlerplayserver.a((Packet) (new S05PacketSpawnPosition(chunkcoordinates.a, chunkcoordinates.b, chunkcoordinates.c)));
        nethandlerplayserver.a((Packet) (new S39PacketPlayerAbilities(entityplayermp.bF)));
        nethandlerplayserver.a((Packet) (new S09PacketHeldItemChange(entityplayermp.bn.c)));
        entityplayermp.x().d();
        entityplayermp.x().b(entityplayermp);
        this.a((ServerScoreboard) worldserver.W(), entityplayermp);
        this.f.au();
        ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("multiplayer.player.joined", new Object[]{ entityplayermp.c_() });
        chatcomponenttranslation.b().a(EnumChatFormatting.YELLOW);
        // CanaryMod Connection hook
        ConnectionHook hook = (ConnectionHook) new ConnectionHook(entityplayermp.getPlayer(), chatcomponenttranslation.e(), firstTime).call();
        if (!hook.isHidden()) {
            this.a((IChatComponent) chatcomponenttranslation);
        }
        // CanaryMod end
        this.c(entityplayermp);
        nethandlerplayserver.a(entityplayermp.t, entityplayermp.u, entityplayermp.v, entityplayermp.z, entityplayermp.A, entityplayermp.aq, w.getName(), TeleportHook.TeleportCause.RESPAWN);
        this.b(entityplayermp, worldserver);
        if (this.f.T().length() > 0) {
            entityplayermp.a(this.f.T());
        }


        Iterator iterator = entityplayermp.aQ().iterator();

        while (iterator.hasNext()) {
            PotionEffect potioneffect = (PotionEffect) iterator.next();

            nethandlerplayserver.a((Packet) (new S1DPacketEntityEffect(entityplayermp.y(), potioneffect)));
        }

        entityplayermp.d_();
        if (nbttagcompound != null && nbttagcompound.b("Riding", 10)) {
            Entity entity = EntityList.a(nbttagcompound.m("Riding"), worldserver);

            if (entity != null) {
                entity.o = true;
                worldserver.d(entity);
                entityplayermp.a(entity);
                entity.o = false;
            }
        }

        // Making sure some stuff is there before attempting to read it (if the player has no nbt, then the usual route is skipped)
        if (nbttagcompound != null && nbttagcompound.c("Canary")) {
            entityplayermp.setMetaData(new CanaryCompoundTag((NBTTagCompound) nbttagcompound.m("Canary")));
        }
        else {
            entityplayermp.initializeNewMeta();
        }
        // CanaryMod: Send Message of the Day
        Canary.motd().sendMOTD(entityplayermp.getPlayer());
        //
    }

    protected void a(ServerScoreboard serverscoreboard, EntityPlayerMP entityplayermp) {
        HashSet hashset = new HashSet();
        Iterator iterator = serverscoreboard.g().iterator();

        while (iterator.hasNext()) {
            ScorePlayerTeam scoreplayerteam = (ScorePlayerTeam) iterator.next();

            entityplayermp.a.a((Packet) (new S3EPacketTeams(scoreplayerteam, 0)));
        }

        for (int i0 = 0; i0 < 3; ++i0) {
            ScoreObjective scoreobjective = serverscoreboard.a(i0);

            if (scoreobjective != null && !hashset.contains(scoreobjective)) {
                List list = serverscoreboard.d(scoreobjective);
                Iterator iterator1 = list.iterator();

                while (iterator1.hasNext()) {
                    Packet packet = (Packet) iterator1.next();

                    entityplayermp.a.a(packet);
                }

                hashset.add(scoreobjective);
            }
        }
    }

    public void a(WorldServer[] server) {
        // CanaryMod Multiworld
        playerFileData.put(server[0].getCanaryWorld().getName(), server[0].L().e()); // XXX May need to review this
        //
    }

    public void a(EntityPlayerMP entityplayermp, WorldServer worldserver) {
        WorldServer worldserver1 = entityplayermp.r();

        if (worldserver != null) {
            worldserver.s().c(entityplayermp);
        }

        worldserver1.s().a(entityplayermp);
        worldserver1.b.c((int) entityplayermp.t >> 4, (int) entityplayermp.v >> 4);
    }

    public int a() {
        return PlayerManager.a(this.o());
    }

    public NBTTagCompound a(EntityPlayerMP entityplayermp) {
        NBTTagCompound nbttagcompound = entityplayermp.getCanaryWorld().getHandle().M().i();
        NBTTagCompound nbttagcompound1;

        if (entityplayermp.b_().equals(this.f.K()) && nbttagcompound != null) {
            entityplayermp.f(nbttagcompound);
            nbttagcompound1 = nbttagcompound;
            d.debug("loading single player");
        }
        else {
            // CanaryMod Multiworld
            nbttagcompound1 = playerFileData.get(entityplayermp.getCanaryWorld().getName()).b(entityplayermp);
            //
        }

        return nbttagcompound1;
    }

    // CanaryMod: get player data for name
    public static NBTTagCompound getPlayerDatByName(String name) {
        ISaveHandler handler = ((CanaryWorld) Canary.getServer().getDefaultWorld()).getHandle().L();

        if (handler instanceof SaveHandler) {
            SaveHandler saves = (SaveHandler) handler;

            return saves.a(name);
        }
        else {
            throw new RuntimeException("ISaveHandler is not of type SaveHandler! Failing to load playerdata");
        }
    }

    protected void b(EntityPlayerMP entityplayermp) {
        // CanaryMod Multiworld
        playerFileData.get(entityplayermp.getCanaryWorld().getName()).a(entityplayermp);
        //
        //CanaryMod: TODO: May need to do StatisticsFile relocation
        StatisticsFile statisticsfile = (StatisticsFile) this.k.get(entityplayermp.b_());

        if (statisticsfile != null) {
            statisticsfile.b();
        }
    }

    public void c(EntityPlayerMP entityplayermp) {
        // CanaryMod: PlayerListEntry  (online players receiving connecting player's info for first time)
        if (Configuration.getServerConfig().isPlayerListEnabled()) {
            PlayerListEntry plentry = entityplayermp.getPlayer().getPlayerListEntry(true);
            plentry.setPing(1000); // Set the ping for the initial connection
            // Get PlayerListEntry
            for (int i0 = 0; i0 < this.a.size(); ++i0) {
                EntityPlayerMP entityplayermp1 = (EntityPlayerMP) this.a.get(i0);
                // Clone the entry so that each receiver will start with the given data
                PlayerListEntry clone = plentry.clone();
                // Call PlayerListEntryHook
                new PlayerListEntryHook(clone, entityplayermp1.getPlayer()).call();
                // Send Packet
                entityplayermp1.a.a(new S38PacketPlayerListItem(clone.getName(), clone.isShown(), 1000)); //Ping Ignored
            }
        }
        //
        this.a.add(entityplayermp);

        // CanaryMod: Directly use playerworld instead
        WorldServer worldserver = (WorldServer) entityplayermp.getCanaryWorld().getHandle(); // this.e.a(entityplayermp.ar);
        worldserver.d(entityplayermp);
        this.a(entityplayermp, (WorldServer) null);

        // CanaryMod: PlayerListEntry (Connecting player receiving online players)
        if (Configuration.getServerConfig().isPlayerListEnabled()) {
            // Get PlayerListEntry
            for (int i0 = 0; i0 < this.a.size(); ++i0) {
                EntityPlayerMP entityplayermp1 = (EntityPlayerMP) this.a.get(i0);
                PlayerListEntry plentry = entityplayermp1.getPlayer().getPlayerListEntry(true);
                // Call PlayerListEntryHook
                new PlayerListEntryHook(plentry, entityplayermp.getPlayer()).call();
                // Send Packet
                entityplayermp.a.a(new S38PacketPlayerListItem(plentry.getName(), plentry.isShown(), plentry.getPing()));
            }
        }
        //
    }

    public void d(EntityPlayerMP entityplayermp) {
        entityplayermp.r().s().d(entityplayermp);
    }

    public void e(EntityPlayerMP entityplayermp) {
        entityplayermp.a(StatList.f);
        this.b(entityplayermp);
        WorldServer worldserver = entityplayermp.r();

        if (entityplayermp.n != null) {
            worldserver.f(entityplayermp.n);
            d.debug("removing player mount");
        }

        worldserver.e(entityplayermp);
        worldserver.s().c(entityplayermp);
        this.a.remove(entityplayermp);
        this.k.remove(entityplayermp.b_());

        // CanaryMod: PlayerListEntry
        if (Configuration.getServerConfig().isPlayerListEnabled()) {
            PlayerListEntry plentry = entityplayermp.getPlayer().getPlayerListEntry(false);
            plentry.setPing(9999); // Set the ping for the initial connection
            for (int i0 = 0; i0 < this.a.size(); ++i0) {
                EntityPlayerMP entityplayermp1 = (EntityPlayerMP) this.a.get(i0);
                // Get the PlayerListEntry
                PlayerListEntry clone = plentry.clone();
                // Call PlayerListEntryHook
                new PlayerListEntryHook(clone, entityplayermp1.getPlayer()).call();
                // Send Packet
                entityplayermp1.a.a(new S38PacketPlayerListItem(clone.getName(), clone.isShown(), clone.getPing()));
            }
        }
        //
    }

    public String a(SocketAddress socketaddress, GameProfile gameprofile) {
/* DISABLED
        if (this.g.a(gameprofile.getName())) {
            BanEntry banentry = (BanEntry) this.g.c().get(gameprofile.getName());
            String s0 = "You are banned from this server!\nReason: " + banentry.f();

            if (banentry.d() != null) {
                s0 = s0 + "\nYour ban will be removed on " + e.format(banentry.d());
            }

            return s0;
        } else if (!this.c(gameprofile.getName())) {
            return "You are not white-listed on this server!";
        } else {
            String s1 = socketaddress.toString();

            s1 = s1.substring(s1.indexOf("/") + 1);
            s1 = s1.substring(0, s1.indexOf(":"));
            if (this.h.a(s1)) {
                BanEntry banentry1 = (BanEntry) this.h.c().get(s1);
                String s2 = "Your IP address is banned from this server!\nReason: " + banentry1.f();

                if (banentry1.d() != null) {
                    s2 = s2 + "\nYour ban will be removed on " + e.format(banentry1.d());
                }

                return s2;
            } else {
                return this.a.size() >= this.b ? "The server is full!" : null;
            }
        }
*/
//      CanaryMod, redo the whole thing
        String s0 = gameprofile.getName();
        String s2 = ((InetSocketAddress) socketaddress).getAddress().getHostAddress(); // Proper IPv6 handling

        PreConnectionHook hook = (PreConnectionHook) new PreConnectionHook(s2, s0, net.canarymod.api.world.DimensionType.NORMAL, Canary.getServer().getDefaultWorldName()).call();

        if (hook.getKickReason() != null) {
            return hook.getKickReason();
        }
        ServerConfiguration srv = Configuration.getServerConfig();

        if (Canary.bans().isBanned(s0)) {
            Ban ban = Canary.bans().getBan(s0);

            if (ban.getTimestamp() != -1) {
                return ban.getReason() + ", " +
                        srv.getBanExpireDateMessage() + ToolBox.formatTimestamp(ban.getTimestamp());
            }
            return ban.getReason();
        }

        if (Canary.bans().isIpBanned(s2)) {
            return Translator.translate(srv.getDefaultBannedMessage());
        }
        if (!Canary.whitelist().isWhitelisted(s0) && Configuration.getServerConfig().isWhitelistEnabled()) {
            return srv.getWhitelistMessage();
        }

        if (this.a.size() >= this.b) {
            if (Canary.reservelist().isSlotReserved(s0) && Configuration.getServerConfig().isReservelistEnabled()) {
                return null;
            }
            return srv.getServerFullMessage();
        }
        return null;
    }

    public EntityPlayerMP a(GameProfile gameprofile) {
        ArrayList arraylist = new ArrayList();

        EntityPlayerMP entityplayermp;

        for (int i0 = 0; i0 < this.a.size(); ++i0) {
            entityplayermp = (EntityPlayerMP) this.a.get(i0);
            if (entityplayermp.b_().equalsIgnoreCase(gameprofile.getName())) {
                arraylist.add(entityplayermp);
            }
        }

        Iterator iterator = arraylist.iterator();

        while (iterator.hasNext()) {
            entityplayermp = (EntityPlayerMP) iterator.next();
            entityplayermp.a.c("You logged in from another location");
        }

        // CanaryMod read the players dat file to find out the world it was last in
        String worldName = Canary.getServer().getDefaultWorldName();
        net.canarymod.api.world.DimensionType worldtype = DimensionType.NORMAL;
        NBTTagCompound playertag = getPlayerDatByName(gameprofile.getName());

        if (playertag != null) {
            CanaryCompoundTag canarycompound = new CanaryCompoundTag(playertag);

            worldName = canarycompound.getString("LevelName");
            if (worldName == null || worldName.isEmpty()) {
                worldName = Canary.getServer().getDefaultWorldName();
            }
            worldtype = net.canarymod.api.world.DimensionType.fromId(canarycompound.getInt("Dimension"));
        }

        WorldServer world = (WorldServer) ((CanaryWorld) Canary.getServer().getWorldManager().getWorld(worldName, worldtype, true)).getHandle();
        Object object;

        if (this.f.P()) {
            object = new DemoWorldManager(world);
        }
        else {
            object = new ItemInWorldManager(world);
        }

        return new EntityPlayerMP(this.f, this.f.getWorld(worldName, 0), gameprofile, (ItemInWorldManager) object);
    }

    public EntityPlayerMP a(EntityPlayerMP entityplayermp, int i0, boolean flag) {
        return this.a(entityplayermp, i0, flag, null);
    }

    // XXX IMPORTANT, HERE IS WORLD SWITCHING GOING ON!
    public EntityPlayerMP a(EntityPlayerMP entityplayermp, int i0, boolean flag0, Location loc) {
        entityplayermp.r().q().a(entityplayermp);
        entityplayermp.r().q().b(entityplayermp);
        entityplayermp.r().s().c(entityplayermp);
        this.a.remove(entityplayermp);
//        this.f.getWorld(entityplayermp.getCanaryWorld().getName(), entityplayermp.aq).f(entityplayermp); // CanaryMod: added multiworld support
        entityplayermp.getCanaryWorld().getHandle().f(entityplayermp);
        // ChunkCoordinates chunkcoordinates = entityplayermp.bL(); //CanaryMod removed in favor of a real location
        Location respawnLocation = entityplayermp.getRespawnLocation();
        boolean flag1 = entityplayermp.bM();
        boolean isBedSpawn = respawnLocation != null;
        entityplayermp.aq = i0;
        String name = entityplayermp.getCanaryWorld().getName();
        net.canarymod.api.world.DimensionType type = net.canarymod.api.world.DimensionType.fromId(i0);
        // CanaryMod: PlayerRespawn
        PlayerRespawningHook hook = (PlayerRespawningHook) new PlayerRespawningHook(entityplayermp.getPlayer(), loc, isBedSpawn).call();
        loc = hook.getRespawnLocation();
        WorldServer worldserver = (WorldServer) (loc == null ? (WorldServer) ((CanaryWorld) Canary.getServer().getWorldManager().getWorld(name, type, true)).getHandle() : ((CanaryWorld) loc.getWorld()).getHandle());

        // CanaryMod changes to accommodate multiworld bed spawns
        ChunkCoordinates chunkcoordinates = null;
        if (respawnLocation != null) {
            chunkcoordinates = new ChunkCoordinates(respawnLocation.getBlockX(), respawnLocation.getBlockY(), respawnLocation.getBlockZ());
            // Check if the spawn world differs from the expected one and adjust
            if (!worldserver.equals(((CanaryWorld) respawnLocation.getWorld()).getHandle())) {
                worldserver = (WorldServer) ((CanaryWorld) respawnLocation.getWorld()).getHandle();
            }
        }
        if (loc != null) {
            chunkcoordinates = new ChunkCoordinates(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            // Check if the spawn world differs from the expected one and adjust
            if (!worldserver.equals(((CanaryWorld) loc.getWorld()).getHandle())) {
                worldserver = (WorldServer) ((CanaryWorld) loc.getWorld()).getHandle();
            }
        }
        //
        ItemInWorldManager itemInWorldManager;
        if (this.f.P()) {
            itemInWorldManager = new DemoWorldManager(worldserver);
        }
        else {
            itemInWorldManager = new ItemInWorldManager(worldserver);
        }
        // Use the wrapper keeper constructor
        EntityPlayerMP entityplayermp1 = new EntityPlayerMP(this.f, worldserver, entityplayermp.bH(), itemInWorldManager, entityplayermp.getPlayer());
        // Copy nethandlerplayserver as the connection is still the same
        entityplayermp1.a = entityplayermp.a;
        entityplayermp1.a.b = entityplayermp1;
        entityplayermp1.a((EntityPlayer) entityplayermp, flag0);
        entityplayermp1.d(entityplayermp.y());

        // CanaryMod: metadata persistance
        entityplayermp.saveMeta();
        entityplayermp1.setMetaData(entityplayermp.getMetaData());
        //

        this.a(entityplayermp1, entityplayermp, worldserver); // GameMode changing
        ChunkCoordinates chunkcoordinates1;
        if (chunkcoordinates != null) {
            chunkcoordinates1 = loc != null ? chunkcoordinates : EntityPlayer.a(worldserver, chunkcoordinates, flag1); // Is it overridden?
            if (chunkcoordinates1 != null) {
                float rot = 0.0F, yaw = 0.0F;
                if (loc != null) {
                    rot = loc.getRotation();
                    yaw = loc.getPitch();
                }
                entityplayermp1.b(chunkcoordinates1.a + 0.5D, chunkcoordinates1.b + 0.1D, chunkcoordinates1.c + 0.5D, rot, yaw);
                entityplayermp1.a(chunkcoordinates, flag1);
            }
            else if (isBedSpawn) {
                entityplayermp1.a.a(new S2BPacketChangeGameState(0, 0.0F));
            }
        }
        while (worldserver.getCanaryWorld().getBlockAt(ToolBox.floorToBlock(entityplayermp1.t), ToolBox.floorToBlock(entityplayermp1.u + 1), ToolBox.floorToBlock(entityplayermp1.v)).getTypeId() != 0) {
            entityplayermp1.u += 1D;
        }

        //sending 2 respawn packets seems to force the client to clear its chunk cache. Removes ghosting blocks
        entityplayermp1.a.a((new S07PacketRespawn(entityplayermp1.aq, entityplayermp1.p.r, entityplayermp1.p.M().u(), entityplayermp1.c.b())));
        entityplayermp1.a.a((new S07PacketRespawn(entityplayermp1.aq, entityplayermp1.p.r, entityplayermp1.p.M().u(), entityplayermp1.c.b())));

        entityplayermp1.a.a(entityplayermp1.t, entityplayermp1.u, entityplayermp1.v, entityplayermp1.z, entityplayermp1.A, entityplayermp1.aq, worldserver.getCanaryWorld().getName(), TeleportHook.TeleportCause.RESPAWN);
        // ChanaryMod: Changed to use Player Coordinates instead of the give chunk coords \/
        entityplayermp1.a.a((new S05PacketSpawnPosition((int) entityplayermp1.t, (int) entityplayermp1.u, (int) entityplayermp1.v)));
        entityplayermp1.a.a((new S1FPacketSetExperience(entityplayermp1.bI, entityplayermp1.bH, entityplayermp1.bG)));
        this.b(entityplayermp1, worldserver);
        worldserver.s().a(entityplayermp1);
        worldserver.d(entityplayermp1); //Tracks new entity
        this.a.add(entityplayermp1);
        entityplayermp1.d_();
        entityplayermp1.g(entityplayermp1.aS());
        new PlayerRespawnedHook(entityplayermp1.getPlayer(), new Location(
                entityplayermp1.getPlayer().getWorld(),
                entityplayermp1.t,
                entityplayermp1.u,
                entityplayermp1.v,
                entityplayermp1.A,
                entityplayermp1.z)).call();
        //
        return entityplayermp1;
    }

    @Deprecated
    public void a(EntityPlayerMP entityplayermp, int i0) {
        throw new UnsupportedOperationException("a(EntityPlayerMP, int) is deprecated. please use a(EntityPlayerMP, String, int))");
    }

    // XXX IMPORTANT, HERE IS DIMENSION SWITCHING GOING ON!
    public void a(EntityPlayerMP entityplayermp, String worldName, int i0) {
        int i1 = entityplayermp.aq;
        WorldServer worldserver = (WorldServer) entityplayermp.getCanaryWorld().getHandle();

        entityplayermp.aq = i0;
        net.canarymod.api.world.DimensionType type = net.canarymod.api.world.DimensionType.fromId(i0);
        WorldServer worldserver1 = (WorldServer) ((CanaryWorld) Canary.getServer().getWorldManager().getWorld(worldName, type, true)).getHandle();

        // Pre-load a chunk in the new world, makes spawning there a little faster
        worldserver1.b.c((int) entityplayermp.t >> 4, (int) entityplayermp.v >> 4);

        /* Force a Dimension's Default GameMode */
        if (Configuration.getWorldConfig(worldserver1.getCanaryWorld().getFqName()).forceDefaultGamemodeDimensional()) {
            entityplayermp.c.a(worldserver1.M().r());
        }
        entityplayermp.a.a((Packet) (new S07PacketRespawn(entityplayermp.aq, entityplayermp.p.r, entityplayermp.p.M().u(), entityplayermp.c.b())));
        worldserver.f(entityplayermp);
        entityplayermp.L = false;
        this.a(entityplayermp, i1, worldserver, worldserver1);
        this.a(entityplayermp, worldserver);
        // TP player to position
        entityplayermp.a.a(entityplayermp.t, entityplayermp.u, entityplayermp.v, entityplayermp.z, entityplayermp.A, entityplayermp.getCanaryWorld().getType().getId(), entityplayermp.getCanaryWorld().getName(), TeleportHook.TeleportCause.PORTAL);
        entityplayermp.c.a(worldserver1);
        this.b(entityplayermp, worldserver1);
        this.f(entityplayermp);

        for (Object o1 : entityplayermp.aQ()) {
            PotionEffect potioneffect = (PotionEffect) o1;

            entityplayermp.a.a((Packet) (new S1DPacketEntityEffect(entityplayermp.y(), potioneffect)));
        }
    }

    public void a(Entity entity, int i0, WorldServer worldserver, WorldServer worldserver1) {
        double d0 = entity.t;
        double d1 = entity.v;
        double d2 = 8.0D;
        double d3 = entity.t;
        double d4 = entity.u;
        double d5 = entity.v;
        float f0 = entity.z;

        worldserver.C.a("moving");
        if (entity.aq == -1) {
            d0 /= d2;
            d1 /= d2;
            entity.b(d0, entity.u, d1, entity.z, entity.A);
            if (entity.Z()) {
                worldserver.a(entity, false);
            }
        }
        else if (entity.aq == 0) {
            d0 *= d2;
            d1 *= d2;
            entity.b(d0, entity.u, d1, entity.z, entity.A);
            if (entity.Z()) {
                worldserver.a(entity, false);
            }
        }
        else {
            ChunkCoordinates chunkcoordinates;

            if (i0 == 1) {
                chunkcoordinates = worldserver1.J();
            }
            else {
                chunkcoordinates = worldserver1.l();
            }

            // CanaryMod: fix for an Entity trying to go to an unloaded world via a portal
            if (chunkcoordinates != null) {
                d0 = (double) chunkcoordinates.a;
                entity.u = (double) chunkcoordinates.b;
                d1 = (double) chunkcoordinates.c;
                entity.b(d0, entity.u, d1, 90.0F, 0.0F);
                if (entity.Z()) {
                    worldserver.a(entity, false);
                }
            }
        }

        worldserver.C.b();
        if (i0 != 1) {
            worldserver.C.a("placing");
            d0 = (double) MathHelper.a((int) d0, -29999872, 29999872);
            d1 = (double) MathHelper.a((int) d1, -29999872, 29999872);
            if (entity.Z()) {
                entity.b(d0, entity.u, d1, entity.z, entity.A);
                worldserver1.t().a(entity, d3, d4, d5, f0);
                worldserver1.d(entity);
                worldserver1.a(entity, false);
            }

            worldserver.C.b();
        }

        entity.a((World) worldserver1);
    }

    public void b() {
        if (Configuration.getServerConfig().getPlayerlistAutoUpdate()) {
            if (++this.p > Configuration.getServerConfig().getPlayerlistTicks()) {
                this.p = 0;
            }

            // CanaryMod: PlayerListEntry
            if (Configuration.getServerConfig().isPlayerListEnabled() && this.p < this.a.size()) {
                EntityPlayerMP entityplayermp = (EntityPlayerMP) this.a.get(this.p);

                // Get PlayerListEntry
                PlayerListEntry plentry = entityplayermp.getPlayer().getPlayerListEntry(true);
                for (int i0 = 0; i0 < this.a.size(); ++i0) {
                    EntityPlayerMP entityplayermp1 = (EntityPlayerMP) this.a.get(i0);
                    // Clone the entry so that each receiver will start with the given data
                    PlayerListEntry clone = plentry.clone();
                    // Call PlayerListEntryHook
                    new PlayerListEntryHook(clone, entityplayermp1.getPlayer()).call();
                    // Send Packet
                    entityplayermp1.a.a(new S38PacketPlayerListItem(clone.getName(), clone.isShown(), clone.getPing()));
                }
            }
            //
        }
    }

    public void a(Packet packet) {
        for (int i0 = 0; i0 < this.a.size(); ++i0) {
            ((EntityPlayerMP) this.a.get(i0)).a.a(packet);
        }
    }

    // CanaryMod re-route packets properly
    public void sendPacketToDimension(Packet packet, String world, int i) {
        for (int j = 0; j < this.a.size(); ++j) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP) this.a.get(j);

            if (world.equals(entityplayermp.getCanaryWorld().getName()) && entityplayermp.aq == i) {
                // TODO check: CanaryMod re-route time updates to world-specific entity trackers
                entityplayermp.a.a(packet);
            }
        }
    }

    // CanaryMod end

    @Deprecated
    public void a(Packet packet, int i0) {
        throw new UnsupportedOperationException("a(packet, int) has been deprecated. use sendPacketToDimension instead!");
    }

    public String c() {
        String s0 = "";

        for (int i0 = 0; i0 < this.a.size(); ++i0) {
            if (i0 > 0) {
                s0 = s0 + ", ";
            }

            s0 = s0 + ((EntityPlayerMP) this.a.get(i0)).b_();
        }

        return s0;
    }

    public String[] d() {
        String[] astring = new String[this.a.size()];

        for (int i0 = 0; i0 < this.a.size(); ++i0) {
            astring[i0] = ((EntityPlayerMP) this.a.get(i0)).b_();
        }

        return astring;
    }

    public BanList e() {
        // CanaryMod this is unused
        throw new UnsupportedOperationException("SCM.e() is deprecated");
    }

    public BanList f() {
        // CanaryMod this is unused
        throw new UnsupportedOperationException("SCM.f() is deprecated");
    }

    public void a(String s0) {
        Canary.ops().addPlayer(s0); // CanaryMod: Re-route to our Ops listing
    }

    public void b(String s0) {
        Canary.ops().removePlayer(s0); // CanaryMod: Re-route to our Ops listing
    }

    public boolean c(String s0) {
        return !this.m || Canary.ops().isOpped(s0); // CanaryMod: Re-route to our Ops listing
    }

    public boolean d(String s0) {
        WorldServer srv = (WorldServer) ((CanaryWorld) Canary.getServer().getDefaultWorld()).getHandle();

        // CanaryMod: Added Re-route to our Ops listing
        return Canary.ops().isOpped(s0) || this.f.L() && srv.M().v() && this.f.K().equalsIgnoreCase(s0) || this.o;
    }

    public EntityPlayerMP e(String s0) {
        Iterator iterator = this.a.iterator();

        EntityPlayerMP entityplayermp;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            entityplayermp = (EntityPlayerMP) iterator.next();
        } while (!entityplayermp.b_().equalsIgnoreCase(s0));

        return entityplayermp;
    }

    public List a(ChunkCoordinates chunkcoordinates, int i0, int i1, int i2, int i3, int i4, int i5, Map map, String s0, String s1, World world) {
        if (this.a.isEmpty()) {
            return null;
        }
        else {
            Object object = new ArrayList();
            boolean flag0 = i2 < 0;
            boolean flag1 = s0 != null && s0.startsWith("!");
            boolean flag2 = s1 != null && s1.startsWith("!");
            int i6 = i0 * i0;
            int i7 = i1 * i1;

            i2 = MathHelper.a(i2);
            if (flag1) {
                s0 = s0.substring(1);
            }

            if (flag2) {
                s1 = s1.substring(1);
            }

            for (int i8 = 0; i8 < this.a.size(); ++i8) {
                EntityPlayerMP entityplayermp = (EntityPlayerMP) this.a.get(i8);

                if ((world == null || entityplayermp.p == world) && (s0 == null || flag1 != s0.equalsIgnoreCase(entityplayermp.b_()))) {
                    if (s1 != null) {
                        Team team = entityplayermp.bt();
                        String s2 = team == null ? "" : team.b();

                        if (flag2 == s1.equalsIgnoreCase(s2)) {
                            continue;
                        }
                    }

                    if (chunkcoordinates != null && (i0 > 0 || i1 > 0)) {
                        float f0 = chunkcoordinates.e(entityplayermp.f_());

                        if (i0 > 0 && f0 < (float) i6 || i1 > 0 && f0 > (float) i7) {
                            continue;
                        }
                    }

                    if (this.a((EntityPlayer) entityplayermp, map) && (i3 == WorldSettings.GameType.NOT_SET.a() || i3 == entityplayermp.c.b().a()) && (i4 <= 0 || entityplayermp.bG >= i4) && entityplayermp.bG <= i5) {
                        ((List) object).add(entityplayermp);
                    }
                }
            }

            if (chunkcoordinates != null) {
                Collections.sort((List) object, new PlayerPositionComparator(chunkcoordinates));
            }

            if (flag0) {
                Collections.reverse((List) object);
            }

            if (i2 > 0) {
                object = ((List) object).subList(0, Math.min(i2, ((List) object).size()));
            }

            return (List) object;
        }
    }

    private boolean a(EntityPlayer entityplayer, Map map) {
        if (map != null && map.size() != 0) {
            Iterator iterator = map.entrySet().iterator();

            Entry entry;
            boolean flag0;
            int i0;

            do {
                if (!iterator.hasNext()) {
                    return true;
                }

                entry = (Entry) iterator.next();
                String s0 = (String) entry.getKey();

                flag0 = false;
                if (s0.endsWith("_min") && s0.length() > 4) {
                    flag0 = true;
                    s0 = s0.substring(0, s0.length() - 4);
                }

                Scoreboard scoreboard = entityplayer.bS();
                ScoreObjective scoreobjective = scoreboard.b(s0);

                if (scoreobjective == null) {
                    return false;
                }

                Score score = entityplayer.bS().a(entityplayer.b_(), scoreobjective);

                i0 = score.c();
                if (i0 < ((Integer) entry.getValue()).intValue() && flag0) {
                    return false;
                }
            } while (i0 <= ((Integer) entry.getValue()).intValue() || flag0);

            return false;
        }
        else {
            return true;
        }
    }

    public void a(double d0, double d1, double d2, double d3, int i0, Packet packet) {
        this.a((EntityPlayer) null, d0, d1, d2, d3, i0, packet);
    }

    public void a(EntityPlayer entityplayer, double d0, double d1, double d2, double d3, int i0, Packet packet) {
        for (int i1 = 0; i1 < this.a.size(); ++i1) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP) this.a.get(i1);

            if (entityplayermp != entityplayer && entityplayermp.aq == i0) {
                double d4 = d0 - entityplayermp.t;
                double d5 = d1 - entityplayermp.u;
                double d6 = d2 - entityplayermp.v;

                if (d4 * d4 + d5 * d5 + d6 * d6 < d3 * d3) {
                    entityplayermp.a.a(packet);
                }
            }
        }
    }

    public void g() {
        for (int i0 = 0; i0 < this.a.size(); ++i0) {
            this.b((EntityPlayerMP) this.a.get(i0));
        }
    }

    public void f(String s0) {
        this.j.add(s0);
    }

    public void g(String s0) {
        this.j.remove(s0);
    }

    public Set h() {
        return this.j;
    }

    public Set i() {
        return this.i;
    }

    public void j() {
    }

    public void b(EntityPlayerMP entityplayermp, WorldServer worldserver) {
        entityplayermp.a.a((Packet) (new S03PacketTimeUpdate(worldserver.H(), worldserver.I(), worldserver.N().b("doDaylightCycle"))));
        if (worldserver.P()) {
            entityplayermp.a.a((Packet) (new S2BPacketChangeGameState(1, 0.0F)));
            entityplayermp.a.a((Packet) (new S2BPacketChangeGameState(7, worldserver.j(1.0F))));
            entityplayermp.a.a((Packet) (new S2BPacketChangeGameState(8, worldserver.h(1.0F))));
        }
    }

    public void f(EntityPlayerMP entityplayermp) {
        entityplayermp.a(entityplayermp.bo);
        entityplayermp.o();
        entityplayermp.a.a((Packet) (new S09PacketHeldItemChange(entityplayermp.bn.c)));
    }

    public int k() {
        return this.a.size();
    }

    public int l() {
        return this.b;
    }

    public String[] m() {
        WorldServer srv = (WorldServer) ((CanaryWorld) Canary.getServer().getDefaultWorld()).getHandle();

        return srv.L().e().f();
    }

    public boolean n() {
        return this.m;
    }

    public void a(boolean flag0) {
        this.m = flag0;
    }

    public List h(String s0) {
        ArrayList arraylist = new ArrayList();
        Iterator iterator = this.a.iterator();

        while (iterator.hasNext()) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP) iterator.next();

            if (entityplayermp.s().equals(s0)) {
                arraylist.add(entityplayermp);
            }
        }

        return arraylist;
    }

    public int o() {
        return this.c;
    }

    public MinecraftServer p() {
        return this.f;
    }

    public NBTTagCompound q() {
        return null;
    }

    private void a(EntityPlayerMP entityplayermp, EntityPlayerMP entityplayermp1, World world) {
        // CanaryMod always use world!

        // if (entityplayermp1 != null) {
        // entityplayermp.c.a(entityplayermp1.c.b());
        // } else if (this.l != null) {
        // entityplayermp.c.a(this.l);
        // }

        //Check GameMode persistance
        if (entityplayermp1 != null) {
            boolean dimensional = entityplayermp.getCanaryWorld().getName().equals(entityplayermp1.getCanaryWorld().getName());
            if (!dimensional && Configuration.getWorldConfig(world.getCanaryWorld().getFqName()).forceDefaultGamemode()) {
                entityplayermp.c.a(world.M().r()); // Force Default GameMode
            }
            else {
                entityplayermp.c.a(entityplayermp1.c.b()); // Persist
            }
        }
        else {
            entityplayermp.c.b(world.M().r()); // Set if not set
        }
    }

    public void r(String msg) {
        // CanaryMod shutdown hook
        ServerShutdownHook hook = (ServerShutdownHook) new ServerShutdownHook(msg != null ? msg : "Server closed").call();
        //
        for (int i0 = 0; i0 < this.a.size(); ++i0) {
            ((EntityPlayerMP) this.a.get(i0)).a.c(hook.getReason());
        }
    }

    public void a(IChatComponent ichatcomponent, boolean flag0) {
        this.f.a(ichatcomponent);
        this.a((Packet) (new S02PacketChat(ichatcomponent, flag0)));
    }

    public void a(IChatComponent ichatcomponent) {
        this.a(ichatcomponent, true);
    }

    public StatisticsFile i(String s0) {
        StatisticsFile statisticsfile = (StatisticsFile) this.k.get(s0);

        if (statisticsfile == null) {
            statisticsfile = new StatisticsFile(this.f, new File(/*this.f.a(0).L().b()*/new File("worlds"), "stats/" + s0 + ".json"));
            statisticsfile.a();
            this.k.put(s0, statisticsfile);
        }

        return statisticsfile;
    }


    /**
     * Get the configuration management wrapper
     *
     * @return the canary configuration manager
     */
    public CanaryConfigurationManager getConfigurationManager() {
        return configurationmanager;
    }

    // This is a CanaryMod method

    /**
     * Send a packet to a specified player.
     *
     * @param player
     * @param packet
     */
    public void sendPacketToPlayer(CanaryPlayer player, CanaryPacket packet) {
        if (a.contains(player.getHandle())) {
            player.getHandle().a.a(packet.getPacket());
        }
    }
}

