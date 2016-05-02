package net.minecraft.server;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import net.canarymod.Canary;
import net.canarymod.api.CanaryConfigurationManager;
import net.canarymod.api.CanaryServer;
import net.canarymod.api.world.CanarySaveConverter;
import net.canarymod.api.world.CanaryWorld;
import net.canarymod.api.world.CanaryWorldManager;
import net.canarymod.api.world.DimensionType;
import net.canarymod.config.Configuration;
import net.canarymod.config.WorldConfiguration;
import net.canarymod.hook.system.LoadWorldHook;
import net.canarymod.hook.system.ServerTickHook;
import net.canarymod.tasks.ServerTaskManager;
import net.canarymod.util.ShutdownLogger;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Bootstrap;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.profiler.IPlayerUsage;
import net.minecraft.profiler.PlayerUsageSnooper;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.visualillusionsent.utils.PropertiesFile;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;

public abstract class MinecraftServer implements ICommandSender, Runnable, IPlayerUsage {

    private static boolean notHeadless = !GraphicsEnvironment.isHeadless(); // CanaryMod
    private static final Logger h = LogManager.getLogger();
    private static MinecraftServer i;
    private final ISaveFormat j;
    private final PlayerUsageSnooper k = new PlayerUsageSnooper("server", this, ap());
    private final File l;
    private final List m = new ArrayList();
    private final ICommandManager n;
    public final Profiler a = new Profiler();
    private final NetworkSystem o;
    private final ServerStatusResponse p = new ServerStatusResponse();
    private final Random q = new Random();
    private String r;
    private int s = -1;
    // public WorldServer[] b; // XXX
    public ServerConfigurationManager t; // CanaryMod private -> public
    private boolean u = true;
    private boolean v;
    private int w;
    protected final Proxy c;
    public String d;
    public int e;
    private boolean x;
    private boolean y;
    private boolean z;
    private boolean A;
    private boolean B;
    private String C;
    private int D;
    private int E = 0;
    public final long[] f = new long[100];
    public long[][] g;
    private KeyPair F;
    private String G;
    private String H;
    private boolean J;
    private boolean K;
    private boolean L;
    private String M = "";
    private boolean N;
    private long O;
    private String P;
    private boolean Q;
    private boolean R;
    private final MinecraftSessionService S;
    private long T = 0L;

    // CanaryMod start: Multiworld \o/
    public CanaryWorldManager worldManager = new CanaryWorldManager();
    private CanaryServer server;

    // CanaryMod start: Stop Message
    private String stopMsg;

    //

    public MinecraftServer(File file1, Proxy proxy) {
        i = this;
        this.c = proxy;
        this.l = file1;
        this.o = new NetworkSystem(this);
        this.n = new ServerCommandManager();
        this.j = new AnvilSaveConverter(file1, DimensionType.NORMAL);
        this.S = (new YggdrasilAuthenticationService(proxy, UUID.randomUUID().toString())).createMinecraftSessionService();
        // CanaryMod
        this.server = new CanaryServer(this);
        Canary.setServer(server);
        //
    }

    protected abstract boolean e() throws IOException;

    protected void a(String s2) {
        if (this.Q().b(s2)) {
            h.info("Converting map!");
            this.b("menu.convertingLevel");
            this.Q().a(s2, new IProgressUpdate() {

                private long b = System.currentTimeMillis();

                public void a(String s2) {
                }

                public void a(int s2) {
                    if (System.currentTimeMillis() - this.b >= 1000L) {
                        this.b = System.currentTimeMillis();
                        MinecraftServer.h.info("Converting... " + s2 + "%");
                    }

                }

                public void c(String s2) {
                }
            });
        }

    }

    protected synchronized void b(String s0) {
        this.P = s0;
    }

    // Used to initialize the "master" worlds
    protected void initWorld(String name, long seed, WorldType nmsWt, net.canarymod.api.world.DimensionType dimType, String generatorSettings) {
        this.a(name); // Anvil Converter
        this.b("menu.loadingLevel");
        File worldFolder = new File("worlds/" + name);
        CanarySaveConverter converter = new CanarySaveConverter(worldFolder);

        if (converter.isVanillaFormat()) {
            Canary.log.info("World " + name + " is Vanilla. Will now attempt to convert.");
            converter.convert();
        }
        AnvilSaveHandler isavehandler = new AnvilSaveHandler(new File("worlds/"), name, true, dimType);
        WorldInfo worldinfo = isavehandler.d();
        WorldConfiguration config = Configuration.getWorldConfig(name + "_" + dimType.getName());

        WorldSettings worldsettings;
        WorldServer world;

        if (worldinfo == null) {
            worldsettings = new WorldSettings(seed, WorldSettings.GameType.a(config.getGameMode().getId()), config.generatesStructures(), false, nmsWt);
            PropertiesFile worldRaw = config.getFile();
            worldRaw.setString("world-seed", String.valueOf(seed));
            worldRaw.setInt("gamemode", 0);
            worldRaw.save();
            // CanaryMod those are flatworld settings, and they are likely unset
            if (generatorSettings != null) {
                worldsettings.a(generatorSettings);
            }
            else {
                worldsettings.a("");
            }
            //
        }
        else {
            // CanaryMod: Force game type from config
            worldinfo.a(WorldSettings.GameType.a(config.getGameMode().getId()));
            worldsettings = new WorldSettings(worldinfo);
        }

        if (this.K) {
            worldsettings.a();
        }

        if (dimType.getId() == 0) {
            if (this.P()) {
                world = new DemoWorldServer(this, isavehandler, name, dimType.getId(), this.a);
            }
            else {
                world = new WorldServer(this, isavehandler, name, dimType.getId(), worldsettings, this.a);
            }
        }
        else {
            world = new WorldServerMulti(this, isavehandler, name, dimType.getId(), worldsettings, (WorldServer) ((CanaryWorld) worldManager.getWorld(name, net.canarymod.api.world.DimensionType.NORMAL, true)).getHandle(), this.a);
        }

        world.a((IWorldAccess) (new WorldManager(this, world)));
        if (!this.L()) {
            world.M().a(WorldSettings.GameType.a(config.getGameMode().getId()));
        }

        this.t.a(new WorldServer[]{ world }); // Init player data files

        // this.a(this.j()); // If we call this then worlds can't do custom difficulty, plus it doesn't work
        world.r = EnumDifficulty.a(config.getDifficulty().getId()); // Set difficulty directly based on WorldConfiguration setting
        this.g(world); // Generate terrain
        worldManager.addWorld(world.getCanaryWorld());
        new LoadWorldHook(world.getCanaryWorld()).call();
    }

    protected void g(WorldServer worldserver) { // CanaryMod: signature changed
        boolean flag0 = true;
        boolean flag1 = true;
        boolean flag2 = true;
        boolean flag3 = true;
        int i0 = 0;

        this.b("menu.generatingTerrain");
        byte b0 = 0;

        h.info("Preparing start region for level " + worldserver.getCanaryWorld().getName());
        ChunkCoordinates chunkcoordinates = worldserver.J();
        long i1 = ap();

        for (int i2 = -192; i2 <= 192 && this.p(); i2 += 16) {
            for (int i3 = -192; i3 <= 192 && this.p(); i3 += 16) {
                long i4 = ap();

                if (i4 - i1 > 1000L) {
                    this.a_("Preparing spawn area", i0 * 100 / 625);
                    i1 = i4;
                }

                ++i0;
                worldserver.b.c(chunkcoordinates.a + i2 >> 4, chunkcoordinates.c + i3 >> 4);
            }
        }

        this.m();
    }

    public abstract boolean h();

    public abstract WorldSettings.GameType i();

    public abstract EnumDifficulty j();

    public abstract boolean k();

    public abstract int l();

    protected void a_(String s0, int i0) {
        this.d = s0;
        this.e = i0;
        h.info(s0 + ": " + i0 + "%");
    }

    protected void m() {
        this.d = null;
        this.e = 0;
    }

    protected void a(boolean flag0) {
        a(flag0, h);
    }

    private void a(boolean flag0, Logger h) {
        if (!this.L) {
            // CanaryMod changed to use worldManager
            for (net.canarymod.api.world.World w : worldManager.getAllWorlds()) {
                WorldServer worldserver = (WorldServer) ((CanaryWorld) w).getHandle();

                if (worldserver != null) {
                    if (!flag0) {
                        h.info("Saving chunks for level \'" + worldserver.M().k() + "\'/" + worldserver.t.l());
                    }

                    try {
                        worldserver.a(true, (IProgressUpdate) null);
                    }
                    catch (MinecraftException minecraftexception) {
                        h.warn(minecraftexception.getMessage());
                    }
                }
                else {
                    h.warn("null world");
                }
            }
        }
    }

    public void n() {
        if (!this.L) {
            // CanaryMod start: If we're in the shutdown hook, we can't rely on log4j for logging.
            Logger log;
            if (Thread.currentThread().getName().equals("Server Shutdown Thread")) {
                log = new ShutdownLogger(MinecraftServer.class);
            } else {
                log = MinecraftServer.h;
            }

            log.info("Stopping server");
            if (this.ag() != null) {
                this.ag().b();
            }

            if (this.t != null) {
                log.info("Saving players");
                this.t.g();
                this.t.r(this.stopMsg);
            }

            log.info("Saving worlds");
            this.a(false, log);

            // CanaryMod Multiworld
            for (net.canarymod.api.world.World w : worldManager.getAllWorlds()) {
                WorldServer worldserver = (WorldServer) ((CanaryWorld) w).getHandle();

                worldserver.n();
            }

            if (this.k.d()) {
                this.k.e();
            }
            // CanaryMod disable plugins:
            Logger canaryLogger = log == h ? Canary.log : new ShutdownLogger("CanaryMod");
            canaryLogger.info("Disabling Plugins ...");
            Canary.loader().disableAllPlugins(canaryLogger);
        }
    }

    public String o() {
        return this.r;
    }

    public void c(String s0) {
        this.r = s0;
    }

    public boolean p() {
        return this.u;
    }

    public void q() {
        this.u = false;
    }

    @Override
    public void run() {
        try {
            if (this.e()) {
                long i0 = ap();
                long i20 = 0L;

                this.p.a((IChatComponent) (new ChatComponentText(this.C)));
                this.p.a(new ServerStatusResponse.MinecraftProtocolVersionIdentifier("1.7.2", 4));
                this.a(this.p);

                while (this.u) {
                    long i2 = ap();
                    long i3 = i2 - i0;

                    if (i3 > 2000L && i0 - this.O >= 15000L) {
                        h.warn("Can\'t keep up! Did the system time change, or is the server overloaded? Running {}ms behind, skipping {} tick(s)", new Object[]{ Long.valueOf(i3), Long.valueOf(i3 / 50L) });
                        i3 = 2000L;
                        this.O = i0;
                    }

                    if (i3 < 0L) {
                        h.warn("Time ran backwards! Did the system time change?");
                        i3 = 0L;
                    }

                    i20 += i3;
                    i0 = i2;
                    // CanaryMod start: multiworld sleeping
                    boolean allSleeping = true;

                    for (net.canarymod.api.world.World w : worldManager.getAllWorlds()) {
                        allSleeping &= ((WorldServer) ((CanaryWorld) w).getHandle()).e();
                    }
                    // CanaryMod end
                    if (allSleeping) {
                        this.t(); // Run tick
                        i20 = 0L;
                    }
                    else {
                        while (i20 > 50L) {
                            i20 -= 50L;
                            this.t();
                        }
                    }

                    Thread.sleep(1L);
                    this.N = true;
                }
            }
            else {
                this.a((CrashReport) null);
            }
        }
        catch (Throwable throwable) {
            h.error("Encountered an unexpected exception", throwable);
            CrashReport crashreport = null;

            if (throwable instanceof ReportedException) {
                crashreport = this.b(((ReportedException) throwable).a());
            }
            else {
                crashreport = this.b(new CrashReport("Exception in server tick loop", throwable));
            }

            File file1 = new File(new File(this.r(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");

            if (crashreport.a(file1)) {
                h.error("This crash report has been saved to: " + file1.getAbsolutePath());
            }
            else {
                h.error("We were unable to save this crash report to disk.");
            }

            this.a(crashreport);
        }
        finally {
            try {
                this.n();
                this.v = true;
            }
            catch (Throwable throwable1) {
                h.error("Exception stopping the server", throwable1);
            }
            finally {
                this.s();
            }
        }

    }

    private void a(ServerStatusResponse serverstatusresponse) {
        File file1 = this.d("server-icon.png");

        if (file1.isFile()) {
            ByteBuf bytebuf = Unpooled.buffer();

            try {
                BufferedImage bufferedimage = ImageIO.read(file1);

                Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
                Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
                ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
                ByteBuf bytebuf1 = Base64.encode(bytebuf);

                serverstatusresponse.a("data:image/png;base64," + bytebuf1.toString(Charsets.UTF_8));
            }
            catch (Exception exception) {
                h.error("Couldn\'t load server icon", exception);
            }
        }
    }

    protected File r() {
        return new File(".");
    }

    protected void a(CrashReport crashreport) {
    }

    protected void s() {
    }

    protected void t() {
        ServerTaskManager.runTasks(); // CanaryMod: Run tasks
        long i0 = System.nanoTime();

        AxisAlignedBB.a().a();
        ++this.w;
        if (this.Q) {
            this.Q = false;
            this.a.a = true;
            this.a.a();
        }

        this.a.a("root");
        this.u();
        if (i0 - this.T >= 5000000000L) {
            this.T = i0;
            this.p.a(new ServerStatusResponse.PlayerCountData(this.C(), this.B()));
            GameProfile[] agameprofile = new GameProfile[Math.min(this.B(), 12)];
            int i1 = MathHelper.a(this.q, 0, this.B() - agameprofile.length);

            for (int i2 = 0; i2 < agameprofile.length; ++i2) {
                agameprofile[i2] = ((EntityPlayerMP) this.t.a.get(i1 + i2)).bH();
            }

            Collections.shuffle(Arrays.asList(agameprofile));
            this.p.b().a(agameprofile);
        }

        if (this.w % 900 == 0) {
            this.a.a("save");
            this.t.g();
            this.a(true);
            this.a.b();
        }

        this.a.a("tallying");
        this.f[this.w % 100] = System.nanoTime() - i0;
        this.a.b();
        this.a.a("snooper");
        if (!this.k.d() && this.w > 100) {
            this.k.a();
        }

        if (this.w % 6000 == 0) {
            this.k.b();
        }

        this.a.b();
        this.a.b();
    }

    // CanaryMod: ticks world
    private long previousTick = -1L; // Tick Time Tracker

    public void u() {
        new ServerTickHook(previousTick).call(); // CanaryMod: ServerTick
        long curTrack = System.nanoTime(); // CanaryMod: Start tick track

        this.a.a("levels");
        int i0;

        // CanaryMod use worldManager instead, and copy into a new list (underlaying list may get modified)
        for (net.canarymod.api.world.World w : new ArrayList<net.canarymod.api.world.World>(worldManager.getAllWorlds())) {
            WorldServer worldserver = (WorldServer) ((CanaryWorld) w).getHandle();
            //
            long i1 = System.nanoTime();

            this.a.a(worldserver.M().k());
            this.a.a("pools");
            worldserver.U().a();
            this.a.b();
            if (this.w % 20 == 0) {
                this.a.a("timeSync");
                // this.t.a((Packet) (new S03PacketTimeUpdate(worldserver.H(), worldserver.I(), worldserver.N().b("doDaylightCycle"))), worldserver.t.i);
                this.t.sendPacketToDimension(new S03PacketTimeUpdate(worldserver.H(), worldserver.I(), worldserver.N().b("doDaylightCycle")), worldserver.getCanaryWorld().getName(), worldserver.t.i);
                this.a.b();
            }

            this.a.a("tick");

            CrashReport crashreport;

            try {
                worldserver.b();
            }
            catch (Throwable throwable) {
                crashreport = CrashReport.a(throwable, "Exception ticking world");
                worldserver.a(crashreport);
                throw new ReportedException(crashreport);
            }

            try {
                worldserver.h();
            }
            catch (Throwable throwable1) {
                crashreport = CrashReport.a(throwable1, "Exception ticking world entities");
                worldserver.a(crashreport);
                throw new ReportedException(crashreport);
            }

            this.a.b();
            this.a.a("tracker");
            worldserver.q().a();
            this.a.b();
            this.a.b();
            w.setNanoTick(this.w % 100, System.nanoTime() - i1);
            // this.k[i0][this.w % 100] = System.nanoTime() - i1;
        }

        this.a.c("connection");
        this.ag().c();
        this.a.c("players");
        this.t.b();
        this.a.c("tickables");

        for (i0 = 0; i0 < this.m.size(); ++i0) {
            ((IUpdatePlayerListBox) this.m.get(i0)).a();
        }

        this.a.b();

        // CanaryMod: set this ticks time
        previousTick = System.nanoTime() - curTrack;
        //
    }

    @Deprecated //CanaryMod: deprecate method
    public boolean v() {
        throw new UnsupportedOperationException("allow-nether has been moved to a per-world configuration!");
    }

    public void a(IUpdatePlayerListBox iupdateplayerlistbox) {
        this.m.add(iupdateplayerlistbox);
    }

    public static void main(String[] astring) {
        Bootstrap.b();

        try {
            // CanaryMod - Removed boolean flag0 = !GraphicsEnvironment.isHeadless();
            String s0 = null;
            String s1 = ".";
            String s2 = null;
            boolean flag1 = false;
            boolean flag2 = false;
            int i0 = -1;

            for (int i1 = 0; i1 < astring.length; ++i1) {
                String s3 = astring[i1];
                String s4 = i1 == astring.length - 1 ? null : astring[i1 + 1];
                boolean flag3 = false;

                // CanaryMod removed Nogui check, now done in Main
                if (s3.equals("--port") && s4 != null) {
                    flag3 = true;

                    try {
                        i0 = Integer.parseInt(s4);
                    }
                    catch (NumberFormatException numberformatexception) {
                        ;
                    }
                }
                else if (s3.equals("--singleplayer") && s4 != null) {
                    flag3 = true;
                    s0 = s4;
                }
                else if (s3.equals("--universe") && s4 != null) {
                    flag3 = true;
                    s1 = s4;
                }
                else if (s3.equals("--world") && s4 != null) {
                    flag3 = true;
                    s2 = s4;
                }
                else if (s3.equals("--demo")) {
                    flag1 = true;
                }
                else if (s3.equals("--bonusChest")) {
                    flag2 = true;
                }
                // CanaryMod removed else to Nogui check

                if (flag3) {
                    ++i1;
                }
            }

            final DedicatedServer dedicatedserver = new DedicatedServer(new File(s1));

            if (s0 != null) {
                dedicatedserver.j(s0);
            }

            if (s2 != null) {
                dedicatedserver.k(s2);
            }

            if (i0 >= 0) {
                dedicatedserver.b(i0);
            }

            if (flag1) {
                dedicatedserver.b(true);
            }

            if (flag2) {
                dedicatedserver.c(true);
            }

            // CanaryMod Removed old call to start GUI

            dedicatedserver.w();
            Runtime.getRuntime().addShutdownHook(new Thread("Server Shutdown Thread") {

                public void run() {
                    if (!dedicatedserver.ae()) {
                        dedicatedserver.n();
                    }
                }
            });
        }
        catch (Exception exception) {
            //h.fatal("Failed to start the minecraft server", exception);
            throw new RuntimeException(exception); //We need to know something happen so we can terminate
        }
    }

    public void w() {
        (new Thread("Server thread") {

            public void run() {
                MinecraftServer.this.run();
            }
        }

        ).start();
    }

    public File d(String s0) {
        return new File(this.r(), s0);
    }

    public void e(String s0) {
        h.info(s0);
    }

    public void f(String s0) {
        h.warn(s0);
    }

    @Deprecated //CanaryMod: deprecate method
    public WorldServer a(int i0) {
        throw new UnsupportedOperationException("MinecraftServer.a(int) has" + " been replaced by MinecraftServer.getWorld(String, int).");
    }

    public WorldServer getWorld(String s, int i) {
        net.canarymod.api.world.World w = worldManager.getWorld(s, net.canarymod.api.world.DimensionType.fromId(i), true);

        if (w != null) {
            return (WorldServer) ((CanaryWorld) w).getHandle();
        }
        return null;
    }

    public String x() {
        return this.r;
    }

    public int y() {
        return this.s;
    }

    public String z() {
        return this.C;
    }

    public String A() {
        return "1.7.2";
    }

    public int B() {
        return this.t.k();
    }

    public int C() {
        return this.t.l();
    }

    public String[] D() {
        return this.t.d();
    }

    public String E() {
        return "";
    }

    public String g(String s0) {
        RConConsoleSource.a.e();
        this.n.a(RConConsoleSource.a, s0);
        return RConConsoleSource.a.f();
    }

    public boolean F() {
        return false;
    }

    public void h(String s0) {
        h.error(s0);
    }

    public void i(String s0) {
        if (this.F()) {
            h.info(s0);
        }
    }

    public String getServerModName() {
        return "CanaryMod";
    }

    public CrashReport b(CrashReport i0) {
        i0.g().a("Profiler Position", new Callable() {

            public String call() {
                return MinecraftServer.this.a.a ? MinecraftServer.this.a.c() : "N/A (disabled)";
            }
        });
        //if (this.b != null && this.b.length > 0 && this.b[0] != null) {
        for (net.canarymod.api.world.World cWorld : this.getWorldManager().getAllWorlds()) {
            if (cWorld.getType() != DimensionType.NORMAL)
                continue; // CanaryMod: Skip non-default dimentional worlds
            final WorldServer worldServer = (WorldServer) ((CanaryWorld) cWorld).getHandle();
            i0.g().a("Vec3 Pool Size", new Callable() {

                public String call() {
                    int i0 = worldServer.U().c();
                    int i1 = 56 * i0;
                    int i2 = i1 / 1024 / 1024;
                    int i3 = worldServer.U().d();
                    int i4 = 56 * i3;
                    int i5 = i4 / 1024 / 1024;

                    return i0 + " (" + i1 + " bytes; " + i2 + " MB) allocated, " + i3 + " (" + i4 + " bytes; " + i5 + " MB) used";
                }
            });
        }

        if (this.t != null) {
            i0.g().a("Player Count", new Callable() {

                public String call() {
                    return MinecraftServer.this.t.k() + " / " + MinecraftServer.this.t.l() + "; " + MinecraftServer.this.t.a;
                }
            });
        }

        return i0;
    }

    public List a(ICommandSender icommandsender, String s0) {
        ArrayList arraylist = new ArrayList();

        if (s0.startsWith("/")) {
            s0 = s0.substring(1);
            boolean flag0 = !s0.contains(" ");

            List list = this.n.b(icommandsender, s0);

            if (list != null) {
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    String s1 = (String) iterator.next();

                    if (flag0) {
                        arraylist.add("/" + s1);
                    }
                    else {
                        arraylist.add(s1);
                    }
                }
            }

            return arraylist;
        }
        else {
            String[] astring = s0.split(" ", -1);
            String s2 = astring[astring.length - 1];
            String[] astring1 = this.t.d();
            int i0 = astring1.length;

            for (int i1 = 0; i1 < i0; ++i1) {
                String s3 = astring1[i1];

                if (CommandBase.a(s2, s3)) {
                    arraylist.add(s3);
                }
            }

            return arraylist;
        }
    }

    public static MinecraftServer G() {
        return i;
    }

    public String b_() {
        return "Server";
    }

    public void a(IChatComponent ichatcomponent) {
        h.info(ichatcomponent.c());
    }

    public boolean a(int i0, String s0) {
        return true;
    }

    public ICommandManager H() {
        return this.n;
    }

    public KeyPair I() {
        return this.F;
    }

    public int J() {
        return this.s;
    }

    public void b(int i0) {
        this.s = i0;
    }

    public String K() {
        return this.G;
    }

    public void j(String s0) {
        this.G = s0;
    }

    public boolean L() {
        return this.G != null;
    }

    public String M() {
        return this.H;
    }

    public void k(String s0) {
        this.H = s0;
    }

    public void a(KeyPair keypair) {
        this.F = keypair;
    }

    public void a(EnumDifficulty enumdifficulty, WorldServer worldserver) { // CanaryMod: Signature change to include world
        // CanaryMod changes for Multiworld, Don't set every world the same.
        //WorldServer worldserver = (WorldServer) ((CanaryWorld) w).getHandle();
        if (worldserver != null) {
            // System.out.println(worldserver.getCanaryWorld().getName() + " Difficulty " + i0);
            boolean monsters = Configuration.getWorldConfig(worldserver.getCanaryWorld().getFqName()).canSpawnMonsters();
            boolean animals = Configuration.getWorldConfig(worldserver.getCanaryWorld().getFqName()).canSpawnAnimals();
            if (worldserver.M().t()) {
                worldserver.r = EnumDifficulty.HARD;
                worldserver.a(monsters, animals);
            }
            //else if (this.L()) { // NOT SINGLE PLAYER ANYWAYS
            //    worldserver.r = enumdifficulty;
            //    worldserver.a(monsters, animals);
            //}
            else {
                worldserver.r = enumdifficulty;
                worldserver.a(Configuration.getWorldConfig(worldserver.getCanaryWorld().getFqName()).canSpawnMonsters(), animals);
            }
        }
    }

    protected boolean O() {
        return true;
    }

    public boolean P() {
        return this.J;
    }

    public void b(boolean flag0) {
        this.J = flag0;
    }

    public void c(boolean flag0) {
        this.K = flag0;
    }

    public ISaveFormat Q() {
        return this.j;
    }

    public void S() {
        this.L = true;
        this.Q().d();

        for (net.canarymod.api.world.World w : worldManager.getAllWorlds()) {
            WorldServer worldserver = (WorldServer) ((CanaryWorld) w).getHandle();

            if (worldserver != null) {
                worldserver.n();
            }

            if (w.getType().getId() == 0) {
                this.Q().e(worldserver.L().g());
            }
        }

        this.q();
    }

    public String T() {
        return this.M;
    }

    public void m(String s0) {
        this.M = s0;
    }

    @Override
    public void a(PlayerUsageSnooper playerusagesnooper) {
        playerusagesnooper.a("whitelist_enabled", Boolean.valueOf(false));
        playerusagesnooper.a("whitelist_count", Integer.valueOf(0));
        playerusagesnooper.a("players_current", Integer.valueOf(this.B()));
        playerusagesnooper.a("players_max", Integer.valueOf(this.C()));
        playerusagesnooper.a("players_seen", Integer.valueOf(this.t.m().length));
        playerusagesnooper.a("uses_auth", Boolean.valueOf(this.x));
        playerusagesnooper.a("gui_state", this.ai() ? "enabled" : "disabled");
        playerusagesnooper.a("run_time", Long.valueOf((ap() - playerusagesnooper.g()) / 60L * 1000L));
        playerusagesnooper.a("avg_tick_ms", Integer.valueOf((int) (MathHelper.a(this.f) * 1.0E-6D)));
        int i0 = 0;

        for (net.canarymod.api.world.World cWorld : this.getWorldManager().getAllWorlds()) {
            WorldServer worldserver = (WorldServer) ((CanaryWorld) cWorld).getHandle();
            //WorldServer worldserver = this.b[i1];
            WorldInfo worldinfo = worldserver.M();

            playerusagesnooper.a("world[" + i0 + "][dimension]", Integer.valueOf(worldserver.t.i));
            playerusagesnooper.a("world[" + i0 + "][mode]", worldinfo.r());
            playerusagesnooper.a("world[" + i0 + "][difficulty]", worldserver.r);
            playerusagesnooper.a("world[" + i0 + "][hardcore]", Boolean.valueOf(worldinfo.t()));
            playerusagesnooper.a("world[" + i0 + "][generator_name]", worldinfo.u().a());
            playerusagesnooper.a("world[" + i0 + "][generator_version]", Integer.valueOf(worldinfo.u().d()));
            playerusagesnooper.a("world[" + i0 + "][height]", Integer.valueOf(this.D));
            playerusagesnooper.a("world[" + i0 + "][chunks_loaded]", Integer.valueOf(worldserver.K().f()));
            ++i0;
            //}
        }

        playerusagesnooper.a("worlds", Integer.valueOf(i0));
    }

    @Override
    public void b(PlayerUsageSnooper playerusagesnooper) {
        playerusagesnooper.a("singleplayer", Boolean.valueOf(this.L()));
        playerusagesnooper.a("server_brand", this.getServerModName());
        playerusagesnooper.a("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
        playerusagesnooper.a("dedicated", Boolean.valueOf(this.V()));
    }

    public boolean U() {
        return true;
    }

    public abstract boolean V();

    public boolean W() {
        return this.x;
    }

    public void d(boolean flag0) {
        this.x = flag0;
    }

    public boolean X() {
        return this.y;
    }

    public void e(boolean flag0) {
        this.y = flag0;
    }

    public boolean Y() {
        return this.z;
    }

    public void f(boolean flag0) {
        this.z = flag0;
    }

    public boolean Z() {
        return this.A;
    }

    public void g(boolean flag0) {
        this.A = flag0;
    }

    public boolean aa() {
        return this.B;
    }

    public void h(boolean flag0) {
        this.B = flag0;
    }

    public abstract boolean ab();

    public String ac() {
        return this.C;
    }

    public void n(String s0) {
        this.C = s0;
    }

    public int ad() {
        return this.D;
    }

    public void c(int i0) {
        this.D = i0;
    }

    public boolean ae() {
        return this.v;
    }

    public ServerConfigurationManager af() {
        return this.t;
    }

    public void a(ServerConfigurationManager serverconfigurationmanager) {
        this.t = serverconfigurationmanager;
    }

    public void a(WorldSettings.GameType worldsettings_gametype) {
        for (net.canarymod.api.world.World w : worldManager.getAllWorlds()) {
            WorldServer worldserver = (WorldServer) ((CanaryWorld) w).getHandle();

            worldserver.M().a(worldsettings_gametype);
        }
    }

    public NetworkSystem ag() {
        return this.o;
    }

    public boolean ai() {
        return false;
    }

    public abstract String a(WorldSettings.GameType worldsettings_gametype, boolean flag0);

    public int aj() {
        return this.w;
    }

    public void ak() {
        this.Q = true;
    }

    public ChunkCoordinates f_() {
        return new ChunkCoordinates(0, 0, 0);
    }

    public World d() {
        return ((CanaryWorld) Canary.getServer().getDefaultWorld()).getHandle(); // CanaryMod
    }

    public int am() {
        return 16;
    }

    public boolean a(World world, int i0, int i1, int i2, EntityPlayer entityplayer) {
        return false;
    }

    public void i(boolean flag0) {
        this.R = flag0;
    }

    public boolean an() {
        return this.R;
    }

    public Proxy ao() {
        return this.c;
    }

    public static long ap() {
        return System.currentTimeMillis();
    }

    public int aq() {
        return this.E;
    }

    public void d(int i0) {
        this.E = i0;
    }

    public IChatComponent c_() {
        return new ChatComponentText(this.b_());
    }

    public boolean ar() {
        return true;
    }

    public MinecraftSessionService as() {
        return this.S;
    }

    public ServerStatusResponse at() {
        return this.p;
    }

    public void au() {
        this.T = 0L;
    }

    /**
     * Returns the canary world manager for this server instance
     *
     * @return
     */
    public CanaryWorldManager getWorldManager() {
        return worldManager;
    }

    /**
     * Reload configurations
     */
    public abstract void reload(); // look in DedicatedServer class

    // CanaryMod

    /**
     * Get the CanaryMod server handler
     *
     * @return
     */
    public CanaryServer getServer() {
        return server;
    }

    public CanaryConfigurationManager getConfigurationManager() {
        return t.getConfigurationManager();
    }

    public void initShutdown(String message) {
        this.stopMsg = message;
        this.q();
    }

    public boolean isRunning() {
        return this.u;
    }

    /**
     * Creates a new world with given name and seed.
     * This will load the default (NORMAL) world
     *
     * @param name
     * @param seed
     */
    public void loadWorld(String name, long seed) {
        loadWorld(name, seed, net.canarymod.api.world.DimensionType.NORMAL);
    }

    public void loadWorld(String name, long seed, net.canarymod.api.world.DimensionType type) {
        this.loadWorld(name, seed, type, net.canarymod.api.world.WorldType.DEFAULT);
    }

    public void loadWorld(String name, long seed, net.canarymod.api.world.DimensionType type, net.canarymod.api.world.WorldType typeGen) {
        this.initWorld(name, seed, WorldType.a(typeGen.toString()), type, null);
    }

    public static boolean isHeadless() {
        return !MinecraftServer.notHeadless;
    }

    public static void setHeadless(boolean state) {
        if (!GraphicsEnvironment.isHeadless()) {
            MinecraftServer.notHeadless = !state;
        }
    }

}

