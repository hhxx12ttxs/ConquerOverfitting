package net.minecraft.src;

import java.awt.Point;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class CJB_Minimap implements Runnable
{
    public static final boolean SUPPORT_HEIGHT_MOD = true;
    public static final boolean SUPPORT_NEW_LIGHTING = true;
    public static final boolean SUPPORT_SWAMPLAND_BIOME_COLOR = true;
    public static final boolean CHANGE_SUNRISE_DIRECTION = true;
    private static final int[] updateFrequencys = new int[] {2, 5, 10, 20, 40};
    public static final CJB_Minimap instance = new CJB_Minimap();
    Minecraft theMinecraft;
    private Tessellator tessellator;
    private World theWorld;
    private EntityPlayer thePlayer;
    private GuiIngame ingameGUI;
    private ScaledResolution scaledResolution;
    private int currentDimension;
    private int scWidth;
    private int scHeight;
    private CJB_GLTextureBufferedImage texture;
    private CJB_ChunkCache chunkCache;
    final Thread mcThread;
    private Thread workerThread;
    private Lock lock;
    private Condition condition;
    private CJB_StripCounter stripCounter;
    private int stripCountMax1;
    private int stripCountMax2;
    private int posX;
    private double posYd;
    private int posZ;
    private int chunkCoordX;
    private int chunkCoordZ;
    private int lastX;
    private int lastZ;
    private boolean isUpdateImage;
    private boolean isCompleteImage;
    private boolean showMenuKey;
    private boolean filtering;
    private int mapPosition;
    private float mapOpacity;
    private int lightmap;
    private int lightType;
    private boolean undulate;
    private boolean omitHeightCalc;
    private int updateFrequencySetting;
    private int threadPriority;
    private boolean heightmap;
    private int fontScale;
    private int mapScale;
    private boolean notchDirection;
    private boolean roundmap;
    private boolean forceUpdate;
    private boolean marker;
    private boolean markerLabel;
    private boolean markerIcon;
    private boolean markerDistance;
    private long currentTimeMillis;
    private int renderType;
    private double targetZoom;
    private double currentZoom;
    private long delay;
    private boolean delayFlag;
    private int worldHeight;
    private boolean nosky;
    private HashMap dimensionName;
    private HashMap dimensionScale;
    private boolean allowCavemap;
    private boolean playerdead;
    long ntime;
    int count;
    static float[] temp;
    private float[] lightmapRed;
    private float[] lightmapGreen;
    private float[] lightmapBlue;
    
    private int waypointcolor;

    boolean getAllowCavemap()
    {
        return this.allowCavemap;
    }

    private CJB_Minimap()
    {
        this.tessellator = Tessellator.instance;
        this.texture = CJB_GLTextureBufferedImage.create(256, 256);
        this.chunkCache = new CJB_ChunkCache(6);
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
        this.stripCounter = new CJB_StripCounter(289);
        this.stripCountMax1 = 0;
        this.stripCountMax2 = 0;
        this.showMenuKey = false;
        this.filtering = false;
        this.mapPosition = 2;
        this.mapOpacity = 1.0F;
        this.lightmap = 0;
        this.lightType = 0;
        this.undulate = true;
        this.omitHeightCalc = false;
        this.updateFrequencySetting = 2;
        this.threadPriority = 1;
        this.heightmap = true;
        this.fontScale = 1;
        this.mapScale = 1;
        this.notchDirection = true;
        this.roundmap = true;
        this.marker = true;
        this.markerLabel = true;
        this.markerIcon = true;
        this.markerDistance = true;
        this.renderType = 0;
        this.targetZoom = 1.0D;
        this.currentZoom = 1.0D;
        this.worldHeight = 255;
        this.dimensionName = new HashMap();
        this.dimensionScale = new HashMap();
        this.dimensionName.put(Integer.valueOf(0), "Overworld");
        this.dimensionScale.put(Integer.valueOf(0), Double.valueOf(1.0D));
        this.dimensionName.put(Integer.valueOf(-1), "Nether");
        this.dimensionScale.put(Integer.valueOf(-1), Double.valueOf(8.0D));
        this.dimensionName.put(Integer.valueOf(1), "The Ender");
        this.dimensionScale.put(Integer.valueOf(1), Double.valueOf(1.0D));

        this.ntime = 0L;
        this.count = 0;
        this.lightmapRed = new float[256];
        this.lightmapGreen = new float[256];
        this.lightmapBlue = new float[256];

        this.mcThread = Thread.currentThread();
    }

    public void onTickInGame(Minecraft mc)
    {
    	double zooma[] = {0.5d, 1.0d, 2.0d, 4.0d, 8.0d};
    	
    	currentZoom = zooma[CJB.mmzoom];//0.5d * CJB.zoom;
    	targetZoom = zooma[CJB.mmzoom];//0.5d * CJB.zoom;
        this.lightmap = CJB.mmskylight ? 0 : 1;
        this.lightType = 0;
        
        this.filtering = false;
        this.undulate = CJB.mmshadow;
        this.roundmap = !CJB.mmsquare;
        this.mapScale = 0;
        this.heightmap = false;
        this.mapPosition = CJB.mmside ? 2 : 0;
        this.fontScale = 0;
        
        this.updateFrequencySetting = CJB.mmfrequency;
        
        this.notchDirection = true;
        
        this.waypointcolor = 0xffffff00;
        
        float fa[] = {1f, 0.75f, 0.5f, 0.25f};
        this.mapOpacity = fa[CJB.mmtrans];
    	
        this.currentTimeMillis = System.currentTimeMillis();
        GL11.glPushAttrib(1048575);
        GL11.glPushClientAttrib(-1);
        GL11.glPushMatrix();
        label2841:
        {
            try
            {
                if (mc != null)
                {
                    int var3;

                    if (this.theMinecraft == null)
                    {
                        this.theMinecraft = mc;
                        this.ingameGUI = this.theMinecraft.ingameGUI;
                    }

                    if (this.thePlayer != this.theMinecraft.thePlayer) {
                    	this.thePlayer = this.theMinecraft.thePlayer;
                    	playerdead = false;
                    }

                    if (this.theWorld != this.theMinecraft.theWorld)
                    {
                    	CJB_BlockColors.calcBlockColorD();
                        this.delay = this.currentTimeMillis + 500L;
                        this.isUpdateImage = false;
                        this.texture.unregister();
                        this.theWorld = this.theMinecraft.theWorld;
                        CJB_Settings.loadData(CJB.mmwaypoints, "waypoints", true);
                        
                        if (this.theWorld != null)
                        {
                        	CJB_Environment.setWorld(theWorld);
                            this.worldHeight = nosky ? 127 : this.theWorld.getHeight() - 1;
                            this.currentDimension = this.thePlayer.dimension;
                            this.nosky = theWorld.worldProvider.hasNoSky;
                        }

                        this.stripCounter.reset();
                    }
                    
                    if (this.thePlayer != null && this.thePlayer.isDead && !playerdead)
            		{
                    	playerdead = true;
            			CJB_Data wptemp = null;
            			CJB_Settings.loadData(CJB.mmwaypoints, "waypoints", true);
            			
            			for( CJB_Data wp : CJB.mmwaypoints )
                		{
                			if (wp.Name.equalsIgnoreCase("Death Point")) {
                				wptemp = wp;
                				break;
                			}
                		}
                		
                		if (wptemp != null) CJB.mmwaypoints.remove(wptemp);
                		
                		CJB_Data loc = new CJB_Data();
                		loc.Name = "Death Point";
                		loc.posx = this.thePlayer.posX;
                		loc.posy = this.thePlayer.posY;
                		loc.posz = this.thePlayer.posZ;
                		loc.data = this.thePlayer.dimension;
                		loc.color = 0xff000001;
                		CJB.mmwaypoints.add(loc);
                		CJB_Settings.saveData(CJB.mmwaypoints, "waypoints", true);
                		CJB_GuiMinimap.waypoint = loc;
            		}

                    this.delayFlag = this.currentTimeMillis < this.delay;
                    CJB_Environment.calcEnvironment();

                    int var41 = this.theMinecraft.displayWidth;
                    var3 = this.theMinecraft.displayHeight;
                    this.scaledResolution = new ScaledResolution(this.theMinecraft.gameSettings, var41, var3);
                    GL11.glScaled(1.0D / this.scaledResolution.scaleFactor, 1.0D / this.scaledResolution.scaleFactor, 1.0D);
                    this.scWidth = mc.displayWidth;
                    this.scHeight = mc.displayHeight;

                    this.renderType = 0;

                    if (CJB.mmenabled && checkGuiScreen())
                    {
                        if (CJB.mmthreading)
                        {
                            if (this.workerThread == null || !this.workerThread.isAlive() || this.threadPriority != CJB.mmpriority)
                            {
                            	this.threadPriority = CJB.mmpriority;
                                this.workerThread = new Thread(this);
                                this.workerThread.setPriority(3 + this.threadPriority);
                                this.workerThread.setDaemon(true);
                                this.workerThread.start();
                            }
                        }
                        else
                        {
                            this.mapCalc(true);
                        }

                        if (this.lock.tryLock())
                        {
                            try
                            {
                                if (this.isUpdateImage)
                                {
                                    this.isUpdateImage = false;
                                    this.texture.setMinFilter(this.filtering);
                                    this.texture.setMagFilter(this.filtering);
                                    this.texture.setClampTexture(true);
                                    this.texture.register();
                                }

                                this.condition.signal();
                            }
                            finally
                            {
                                this.lock.unlock();
                            }
                        }

                        if (this.texture.getId() != 0)
                        {
                            if (this.roundmap)
                            {
                                this.renderRoundMap();
                            }
                            else
                            {
                                this.renderSquareMap();
                            }
                        }

                        break label2841;
                    }

                    return;
                }
            }
            catch (RuntimeException var35)
            {
                var35.printStackTrace();
                break label2841;
            }
            finally
            {
                GL11.glPopMatrix();
                GL11.glPopClientAttrib();
                GL11.glPopAttrib();
            }

            return;
        }

        if (this.count != 0)
        {
            this.theMinecraft.fontRenderer.drawStringWithShadow(String.format("%12d", new Object[] {Long.valueOf(this.ntime / this.count)}), 2, 12, -1);
        }

        Thread.yield();
    }

    public void run()
    {
        if (this.theMinecraft != null)
        {
            Thread var1 = Thread.currentThread();

            while (true)
            {
                while (!CJB.mmenabled || var1 != this.workerThread || !CJB.mmthreading)
                {
                    try
                    {
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException var20)
                    {
                        return;
                    }

                    this.lock.lock();
                    label213:
                    {
                        try
                        {
                            this.condition.await();
                            break label213;
                        }
                        catch (InterruptedException var24)
                        {
                            ;
                        }
                        finally
                        {
                            this.lock.unlock();
                        }

                        return;
                    }

                    if (var1 != this.workerThread)
                    {
                        return;
                    }
                }

                try
                {
                    if (this.renderType == 0)
                    {
                        Thread.sleep((updateFrequencys[updateFrequencys.length - this.updateFrequencySetting - 1] * 2));
                    }
                    else
                    {
                        Thread.sleep((updateFrequencys[updateFrequencys.length - this.updateFrequencySetting - 1] * 6));
                    }
                }
                catch (InterruptedException var19)
                {
                    return;
                }

                this.lock.lock();

                try
                {
                    this.mapCalc(false);

                    if (this.isCompleteImage || this.isUpdateImage)
                    {
                        this.condition.await();
                    }

                    continue;
                }
                catch (InterruptedException var21)
                {
                    ;
                }
                catch (Exception var22)
                {
                    var22.printStackTrace();
                    continue;
                }
                finally
                {
                    this.lock.unlock();
                }

                return;
            }
        }
    }

    private void startDrawingQuads()
    {
        this.tessellator.startDrawingQuads();
    }

    private void draw()
    {
        this.tessellator.draw();
    }

    private void addVertexWithUV(double var1, double var3, double var5, double var7, double var9)
    {
        this.tessellator.addVertexWithUV(var1, var3, var5, var7, var9);
    }

    private void mapCalc(boolean var1)
    {
        if (!this.delayFlag)
        {
            if (this.theWorld != null && this.thePlayer != null)
            {
                Thread thread = Thread.currentThread();
                double var3;

                if (this.stripCounter.count() == 0)
                {
                    this.posX = MathHelper.floor_double(this.thePlayer.posX);
                    //this.posY = MathHelper.floor_double(this.thePlayer.posY);
                    this.posYd = this.thePlayer.posY;
                    this.posZ = MathHelper.floor_double(this.thePlayer.posZ);
                    this.chunkCoordX = this.thePlayer.chunkCoordX;
                    this.chunkCoordZ = this.thePlayer.chunkCoordZ;
                    //this.skylightSubtracted = this.calculateSkylightSubtracted(this.theWorld.getWorldTime(), 0.0F);

                    if (this.lightType == 0)
                    {
                        switch (this.lightmap)
                        {
                            case 0:
                                this.updateLightmap(this.theWorld.getWorldTime(), 0.0F);
                                break;

                            case 1:
                                this.updateLightmap(6000L, 0.0F);
                                break;

                            case 2:
                                this.updateLightmap(18000L, 0.0F);
                                break;

                            case 3:
                                this.updateLightmap(6000L, 0.0F);
                        }
                    }

                    var3 = Math.toRadians(this.roundmap ? (double)(45.0F - this.thePlayer.rotationYaw) : (double)(this.notchDirection ? 225 : -45));
                }
                
                var3 = Math.ceil(4.0D / this.currentZoom) * 2.0D + 1.0D;
                this.stripCountMax1 = (int)(var3 * var3);
                var3 = Math.ceil(4.0D / this.targetZoom) * 2.0D + 1.0D;
                this.stripCountMax2 = (int)(var3 * var3);
                
                if (!this.forceUpdate && var1)
                {
                    this.surfaceCalcStrip(thread);
                }
                else
                {
                    this.surfaceCalc(thread);
                }

                if (this.isCompleteImage)
                {
                    this.forceUpdate = false;
                    this.isCompleteImage = false;
                    this.stripCounter.reset();
                    this.lastX = this.posX;
                    //this.lastY = this.posY;
                    this.lastZ = this.posZ;
                }
            }
        }
    }

    private void surfaceCalc(Thread thread)
    {
        int var2 = Math.max(this.stripCountMax1, this.stripCountMax2);

        while (this.stripCounter.count() < var2)
        {
            Point var3 = this.stripCounter.next();
            Chunk var4 = this.chunkCache.get(this.theWorld, this.chunkCoordX + var3.x, this.chunkCoordZ + var3.y);
            this.surfaceCalc(var4, thread);
        }

        this.isUpdateImage = this.stripCounter.count() >= this.stripCountMax1;
        this.isCompleteImage = this.isUpdateImage && this.stripCounter.count() >= this.stripCountMax2;
    }

    private void surfaceCalcStrip(Thread thread)
    {
        int var2 = Math.max(this.stripCountMax1, this.stripCountMax2);
        int var3 = updateFrequencys[this.updateFrequencySetting];

        for (int var4 = 0; var4 < var3 && this.stripCounter.count() < var2; ++var4)
        {
            Point var5 = this.stripCounter.next();
            Chunk var6 = this.chunkCache.get(this.theWorld, this.chunkCoordX + var5.x, this.chunkCoordZ + var5.y);
            this.surfaceCalc(var6, thread);
        }

        this.isUpdateImage = this.stripCounter.count() >= this.stripCountMax1;
        this.isCompleteImage = this.isUpdateImage && this.stripCounter.count() >= this.stripCountMax2;
    }

    private void surfaceCalc(Chunk chunk, Thread thread)
    {
        if (!this.delayFlag)
        {
            if (chunk != null && !(chunk instanceof EmptyChunk))
            {
                int var3 = 128 + chunk.xPosition * 16 - this.posX;
                int var4 = 128 + chunk.zPosition * 16 - this.posZ;
                boolean slimechunk = CJB.mmslimechunks && this.currentDimension == 0 && this.chunkCache.isSlimeSpawn(chunk.xPosition, chunk.zPosition);
                CJB_PixelColor pix = new CJB_PixelColor(false);
                Chunk var7 = null;
                Chunk var8 = null;
                Chunk var9 = null;
                Chunk var10 = null;
                Chunk var11 = null;
                Chunk var12 = null;
                Chunk var13 = null;
                Chunk var14 = null;
                
                boolean undulate = this.undulate && !nosky;

                if (undulate)
                {
                    var9 = this.getChunk(chunk.worldObj, chunk.xPosition, chunk.zPosition - 1);
                    var10 = this.getChunk(chunk.worldObj, chunk.xPosition, chunk.zPosition + 1);
                    var7 = this.getChunk(chunk.worldObj, chunk.xPosition - 1, chunk.zPosition);
                    var8 = this.getChunk(chunk.worldObj, chunk.xPosition + 1, chunk.zPosition);
                }

                for (int z = 0; z < 16; ++z)
                {
                    int mmz = var4 + z;

                    if (mmz >= 0)
                    {
                        if (mmz >= 256)
                        {
                            break;
                        }

                        if (undulate)
                        {
                            var13 = z == 0 ? var9 : chunk;
                            var14 = z == 15 ? var10 : chunk;
                        }

                        for (int x = 0; x < 16; ++x)
                        {
                            int mmx = var3 + x;

                            if (mmx >= 0)
                            {
                                if (mmx >= 256)
                                {
                                    break;
                                }

                                pix.clear();
                                int var19 = !this.omitHeightCalc && !this.heightmap && !this.undulate ? this.worldHeight : Math.min(this.worldHeight, chunk.getHeightValue(x, z));
                                int y = this.omitHeightCalc ? Math.min(this.worldHeight, var19 + 1) : this.worldHeight;
                                
                                if (nosky) {
                                	for (int i = 0 ; i < 127 ; i++) {
                                		if (chunk.getBlockID(x, i, z) == 0) {
                                			y = i;
                                			break;
                                		}
                                	}
                                }

                                if (y < 0)
                                {
                                    this.texture.setRGB(mmx, mmz, -16777216);
                                }
                                else
                                {
                                    this.surfaceCalc(chunk, x, y, z, pix, thread);
                                    float colorstrength;

                                    if (this.heightmap)
                                    {
                                    	colorstrength = undulate ? 0.15F : 0.6F;
                                        double var22 = var19 - this.posYd;
                                        float var24 = (float)Math.log10(Math.abs(var22) * 0.125D + 1.0D) * colorstrength;

                                        if (var22 >= 0.0D)
                                        {
                                        	pix.red += var24 * (1.0F - pix.red);
                                        	pix.green += var24 * (1.0F - pix.green);
                                        	pix.blue += var24 * (1.0F - pix.blue);
                                        }
                                        else
                                        {
                                            var24 = Math.abs(var24);
                                            pix.red -= var24 * pix.red;
                                            pix.green -= var24 * pix.green;
                                            pix.blue -= var24 * pix.blue;
                                        }
                                    }

                                    colorstrength = 1.0F;

                                    if (undulate)
                                    {
                                        var11 = x == 0 ? var7 : chunk;
                                        var12 = x == 15 ? var8 : chunk;
                                        int var26 = var11.getHeightValue(x - 1 & 15, z);
                                        int var23 = var12.getHeightValue(x + 1 & 15, z);
                                        int var30 = var13.getHeightValue(x, z - 1 & 15);
                                        int var25 = var14.getHeightValue(x, z + 1 & 15);
                                        colorstrength += Math.max(-4.0F, Math.min(3.0F, (float)(var26 - var23) * -1 + (float)(var30 - var25) * -1)) * 0.14142136F * 0.8F;
                                    }

                                    if (slimechunk)
                                    {
                                    	pix.red = (float)(pix.red * 0.0D);
                                    	pix.green = (float)(pix.green * 1.5D);
                                    	pix.blue = (float)(pix.blue * 0.0D);
                                    }

                                    byte br = ftob(pix.red * colorstrength);
                                    byte bg = ftob(pix.green * colorstrength);
                                    byte bb = ftob(pix.blue * colorstrength);

                                    this.texture.setRGB(mmx, mmz, br, bg, bb);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private static final byte ftob(float var0)
    {
        return (byte)Math.max(0, Math.min(255, (int)(var0 * 255.0F)));
    }

    private void surfaceCalc(Chunk chunk, int x, int y, int z, CJB_PixelColor pix, Thread thread)
    {
        int bid = chunk.getBlockID(x, y, z);
        
        if (bid != 0 && (CJB.mmshowsnow || bid != 78))
        {
            int bmd = CJB_BlockColors.useMetadata(bid, 0) ? chunk.getBlockMetadata(x, y, z) : 0;
            
            if (bid == Block.leaves.blockID)
            	bmd = bmd & 3;
            
            CJB_BlockColors bcol = CJB_BlockColors.getBlockColor(bid, bmd);
            pix.composite(bcol.argb);
            
            if (bcol.alpha == 0.0F && y > 0)
            {
                this.surfaceCalc(chunk, x, y - 1, z, pix, thread);
                return;
            }
            
            int var23;

            if (this.lightType == 0)
            {
                switch (this.lightmap)
                {
                    case 3:
                        var23 = 15;
                        break;

                    default:
                        this.lightmap = 0;

                    case 0:
                    case 1:
                    case 2:
                        var23 = y < this.worldHeight ? chunk.getSavedLightValue(EnumSkyBlock.Sky, x, y + 1, z) : 15;
                }

                int var12 = Math.max(Block.lightValue[bid], chunk.getSavedLightValue(EnumSkyBlock.Block, x, Math.min(this.worldHeight, y + 1), z));
                int var13 = var23 << 4 | var12;
                float var14 = this.lightmapRed[var13];
                float var15 = this.lightmapGreen[var13];
                float var16 = this.lightmapBlue[var13];
                
                if (bcol.tintType == CJB_TintType.GRASS) {

                	CJB_Environment env = CJB_Environment.getEnvironment(chunk, x, z, thread);
                	int grass = env.getGrassColor();
                	BiomeGenBase bio = env.getBiome();
                	
                	pix.composite(bcol.alpha, CJB_Environment.calcGrassColor(bio, grass), bcol.red * var14, bcol.green * var15, bcol.blue * var16);
                	return;
                }
                /*if (bcol.tintType == CJB_TintType.FOLIAGE) {

                	CJB_Environment env = CJB_Environment.getEnvironment(chunk, x, z, thread);
                	int foliage = env.getFoliageColor();
                	BiomeGenBase bio = env.getBiome();
                	
                	pix.composite(bcol.alpha, CJB_Environment.calcFoliageColor(bio, foliage), bcol.red * var14, bcol.green * var15, bcol.blue * var16);
                	return;
                }
                if (bcol.tintType == CJB_TintType.BIRCH) {

                	CJB_Environment env = CJB_Environment.getEnvironment(chunk, x, z, thread);
                	int foliage = env.getFoliageColorBirch();
                	BiomeGenBase bio = env.getBiome();
                	
                	pix.composite(bcol.alpha, CJB_Environment.calcFoliageColor(bio, foliage), bcol.red * var14, bcol.green * var15, bcol.blue * var16);
                	return;
                }
                if (bcol.tintType == CJB_TintType.PINE) {

                	CJB_Environment env = CJB_Environment.getEnvironment(chunk, x, z, thread);
                	int foliage = env.getFoliageColorPine();
                	BiomeGenBase bio = env.getBiome();
                	
                	pix.composite(bcol.alpha, CJB_Environment.calcFoliageColor(bio, foliage), bcol.red * var14, bcol.green * var15, bcol.blue * var16);
                	return;
                }*/
                pix.composite(bcol.alpha, bcol.red * var14, bcol.green * var15, bcol.blue * var16);
            }
            
            
        }
        else
        {
            if (y > 0)
            {
                this.surfaceCalc(chunk, x, y - 1, z, pix, thread);
            }
        }
    }

    private void renderRoundMap()
    {
        float var1 = 1;

        if (this.mapScale == 0)
        {
            var1 = this.scaledResolution.scaleFactor * (1.2f - 0.1f * CJB.mmsize);
        }
        else if (this.mapScale == 1)
        {
            while (this.scWidth >= (var1 + 1) * 320 && this.scHeight >= (var1 + 1) * 240)
            {
                ++var1;
            }
        }
        else
        {
            var1 = this.mapScale - 1;
        }

        int var2 = this.fontScale - 1;
        var2 = this.scaledResolution.scaleFactor + 1 >> 1;


        int var3 = (int) ((this.mapPosition & 2) == 0 ? 33 * var1 : this.scWidth - 33 * var1);
        int var4 = (int) ((this.mapPosition & 1) == 0 ? 33 * var1 : this.scHeight - 33 * var1);

        if ((this.mapPosition & 1) == 1)
        {
            var4 -= ((this.showMenuKey | CJB.mmcoords ? 2 : 0) + (this.showMenuKey ? 9 : 0) + (CJB.mmcoords ? 18 : 0)) * var2;
        }

        GL11.glTranslated(var3, var4, 0.0D);
        float f = 0;//0.6f - (0.5f * CJB.mmsize);
        GL11.glScalef(var1 + f, var1 + f, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColorMask(false, false, false, false);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
        GL11.glDepthMask(true);
        
        GL11.glPushMatrix();
        GL11.glRotatef(90.0F - this.thePlayer.rotationYaw, 0.0F, 0.0F, 1.0F);
        CJB_GLTexture.ROUND_MAP_MASK.bind();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawCenteringRectangle(0.0D, 0.0D, 1.01D, 64.0D, 64.0D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColorMask(true, true, true, true);
        double var5 = 0.25D / this.currentZoom;
        double var7 = (this.thePlayer.posX - this.lastX) * 0.00390625D;
        double var9 = (this.thePlayer.posZ - this.lastZ) * 0.00390625D;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, this.mapOpacity);
        this.texture.bind();
        this.startDrawingQuads();
        this.addVertexWithUV(-32.0D, 32.0D, 1.0D, 0.5D + var5 + var7, 0.5D + var5 + var9);
        this.addVertexWithUV(32.0D, 32.0D, 1.0D, 0.5D + var5 + var7, 0.5D - var5 + var9);
        this.addVertexWithUV(32.0D, -32.0D, 1.0D, 0.5D - var5 + var7, 0.5D - var5 + var9);
        this.addVertexWithUV(-32.0D, -32.0D, 1.0D, 0.5D - var5 + var7, 0.5D + var5 + var9);
        this.draw();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
        double var11;
        double var17;
        int var16;
        float var21;
        float var20;

        {
            var11 = 29.0D;
            ArrayList var13 = new ArrayList();
            var13.addAll(this.theWorld.loadedEntityList);
            Iterator var14 = var13.iterator();
            Entity var15;

            while (var14.hasNext())
            {
                var15 = (Entity)var14.next();

                if (var15 != null)
                {
                    var16 = this.getEntityColor(var15);

                    if (var16 != 0)
                    {
                        var17 = this.thePlayer.posX - var15.posX;
                        double var19 = this.thePlayer.posZ - var15.posZ;
                        var21 = (float)Math.toDegrees(Math.atan2(var17, var19));
                        double var22 = Math.sqrt(var17 * var17 + var19 * var19) * this.currentZoom * 0.5D;

                        try
                        {
                            GL11.glPushMatrix();

                            if (var22 < var11)
                            {
                                float var24 = (var16 >> 16 & 255) * 0.003921569F;
                                float var25 = (var16 >> 8 & 255) * 0.003921569F;
                                float var26 = (var16 & 255) * 0.003921569F;
                                float var27 = var15 instanceof EntityPlayer ? 1f : (float)Math.max(0.0, 1.0D - Math.abs(this.thePlayer.posY - var15.posY) * 0.06D);
                                GL11.glColor4f(var24, var25, var26, var27);
                                GL11.glRotatef(-var21 - this.thePlayer.rotationYaw + 180.0F, 0.0F, 0.0F, 1.0F);
                                GL11.glTranslated(0.0D, -var22, 0.0D);
                                GL11.glRotatef(-(-var21 - this.thePlayer.rotationYaw + 180.0F), 0.0F, 0.0F, 1.0F);
                                CJB_GLTexture.ENTITY.bind();
                                this.drawCenteringRectangle(0.0D, 0.0D, 1.0D, 8.0D, 8.0D);
                            }
                        }
                        finally
                        {
                            GL11.glPopMatrix();
                        }
                    }
                }
            }
        }
        
        {
            var11 = 29.0D;
            ArrayList var13 = new ArrayList();
            var13.addAll(CJB.mmwaypoints);
            Iterator var14 = var13.iterator();
            CJB_Data var15;

            while (var14.hasNext())
            {
                var15 = (CJB_Data)var14.next();

                if (var15 != null)
                {
                    var16 = var15.color;

                    if (var16 != 0)
                    {
                        var17 = this.thePlayer.posX - var15.posx - 0.5d;
                        double var19 = this.thePlayer.posZ - var15.posz - 0.5d;
                        var21 = (float)Math.toDegrees(Math.atan2(var17, var19));
                        double var22 = Math.sqrt(var17 * var17 + var19 * var19) * this.currentZoom * 0.5D;

                        try
                        {
                            GL11.glPushMatrix();

                            if (var22 < var11)
                            {
                                float var24 = (var16 >> 16 & 255) * 0.003921569F;
                                float var25 = (var16 >> 8 & 255) * 0.003921569F;
                                float var26 = (var16 & 255) * 0.003921569F;
                                float var28 = 1f;
                                var24 *= var28;
                                var25 *= var28;
                                var26 *= var28;
                                GL11.glColor4f(var24, var25, var26, 1f);
                                GL11.glRotatef(-var21 - this.thePlayer.rotationYaw + 180.0F, 0.0F, 0.0F, 1.0F);
                                GL11.glTranslated(0.0D, -var22, 0.0D);
                                GL11.glRotatef(-(-var21 - this.thePlayer.rotationYaw + 180.0F), 0.0F, 0.0F, 1.0F);
                                CJB_GLTexture.WAYPOINT.bind();
                                this.drawCenteringRectangle(0.0D, 0.0D, 1.0D, 10.0D, 10.0D);
                            }
                        }
                        finally
                        {
                            GL11.glPopMatrix();
                        }
                    }
                }
            }
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, this.mapOpacity);
        
        if (!CJB.mmnoborder) {
        	CJB_GLTexture.ROUND_MAP.bind();
        	this.drawCenteringRectangle(0.0D, 0.0D, 1.0D, 64.0D, 64.0D);
        }
        
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        var11 = Math.sin(Math.toRadians(this.thePlayer.rotationYaw-90)) * 28.0D;
        double var44 = Math.cos(Math.toRadians(this.thePlayer.rotationYaw-90)) * 28.0D;

        CJB_GLTexture.N.bind();
        this.drawCenteringRectangle(var44, -var11, 1.0D, 8.0D, 8.0D);
        
        if (CJB.mmshowallwp) {
	        for (CJB_Data way : CJB.mmwaypoints)
	        {
	            double d1 = thePlayer.posX - way.posx-0.5d;
	            double d2 = thePlayer.posZ - way.posz-0.5d;
	            int disint = (int)Math.sqrt((d1*d1)+(d2*d2));
	            double d = -Math.toDegrees(Math.atan2(d1,d2))+90d;
	            
	            var11 = Math.sin(Math.toRadians(this.thePlayer.rotationYaw-d)) * 30.5D;
	            var44 = Math.cos(Math.toRadians(this.thePlayer.rotationYaw-d)) * 30.5D;
	            
	            CJB_GLTexture.WAYPOINTMARKER.bind();
	            GLColor(0xff000000 + way.color);
	            if (disint > 55 / this.currentZoom)
	            	this.drawCenteringRectangle(var44, -var11, 1.0D, 10.0D, 10.0D);
	        }
        } else {
        	if (CJB_GuiMinimap.waypoint != null) {
        		CJB_Data way = CJB_GuiMinimap.waypoint.getDataFromList(CJB.mmwaypoints);
	            
	            double d1 = thePlayer.posX - way.posx-0.5d;
	            double d2 = thePlayer.posZ - way.posz-0.5d;
	            int disint = (int)Math.sqrt((d1*d1)+(d2*d2));
	            double d = -Math.toDegrees(Math.atan2(d1,d2))+90d;
	            
	            var11 = Math.sin(Math.toRadians(this.thePlayer.rotationYaw-d)) * 30.5D;
	            var44 = Math.cos(Math.toRadians(this.thePlayer.rotationYaw-d)) * 30.5D;
	            
	            CJB_GLTexture.WAYPOINTMARKER.bind();
	            GLColor(0xff000000 + way.color);
	            if (disint > 55 / this.currentZoom)
	            	this.drawCenteringRectangle(var44, -var11, 1.0D, 10.0D, 10.0D);
        	}
        }
        
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glScaled(1.0D / var1, 1.0D / var1, 1.0D);
        FontRenderer var48 = this.theMinecraft.fontRenderer;
        int var56;
        int var63;
        int var54 = (int) (32 * var1);
        String var52;

        if (CJB.mmcoords) {
            String var61;

            var56 = MathHelper.floor_double(this.thePlayer.posX);
            var63 = MathHelper.floor_double(this.thePlayer.boundingBox.minY);
            int var59 = MathHelper.floor_double(this.thePlayer.posZ);
            var52 = String.format("%d, %d", new Object[] {Integer.valueOf(var56), Integer.valueOf(var59)});
            var61 = Integer.toString(var63);

            var20 = var48.getStringWidth(var52) * 0.5F * var2;
            var21 = var48.getStringWidth(var61) * 0.5F * var2;
            float var60 = (37 * var1) < var20 ? (37 * var1) - var20 : 0.0F;

            if ((this.mapPosition & 2) == 0)
            {
                var60 = -var60;
            }

            GL11.glTranslatef(var60 - var20, var54, 0.0F);
            GL11.glScalef(var2, var2, 1.0F);
            var48.drawStringWithShadow(var52, 0, 2, 16777215);
            GL11.glScaled(1.0D / var2, 1.0D / var2, 1.0D);
            GL11.glTranslatef(var20 - var21, 0.0F, 0.0F);
            GL11.glScalef(var2, var2, 1.0F);
            var48.drawStringWithShadow(var61, 0, 11, 16777215);
            GL11.glScaled(1.0D / var2, 1.0D / var2, 1.0D);
            GL11.glTranslatef(var21 - var60, (-var54), 0.0F);
            var54 += 36 * var2;
        }
        
        if (CJB_GuiMinimap.waypoint != null)
        {
        	CJB_Data way = CJB_GuiMinimap.waypoint.getDataFromList(CJB.mmwaypoints);
            
            double d1 = thePlayer.posX + -way.posx;
            double d2 = thePlayer.posZ + -way.posz;
            int disint = (int)Math.sqrt((d1*d1)+(d2*d2));
            String dis = Integer.toString(disint) + "m";
            String name = way.Name;
            
            var21 = var48.getStringWidth(dis) * 0.5F * var2;
            var20 = var48.getStringWidth(name) * 0.5F * var2;
            
            float var60 = (37 * var1) < var20 ? (37 * var1) - var20 : 0.0F;

            if ((this.mapPosition & 2) == 0)
            {
                var60 = -var60;
            }

            GL11.glTranslatef(var60 - var20, var54, 0.0F);
            GL11.glScalef(var2, var2, 1.0F);
            var48.drawStringWithShadow(name, 0, 2, 16777215);
            GL11.glScaled(1.0D / var2, 1.0D / var2, 1.0D);
            GL11.glTranslatef(var20 - var21, 0.0F, 0.0F);
            GL11.glScalef(var2, var2, 1.0F);
            var48.drawStringWithShadow(dis, 0, 11, 16777215);
            GL11.glScaled(1.0D / var2, 1.0D / var2, 1.0D);
            GL11.glTranslatef(var21 - var60, (-var54), 0.0F);
            var54 += 18 * var2;
        }

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private void renderSquareMap()
    {
        float var1 = 1;

        if (this.mapScale == 0)
        {
            var1 = this.scaledResolution.scaleFactor * (1.2f - 0.1f * CJB.mmsize);
        }
        else if (this.mapScale == 1)
        {
            while (this.scWidth >= (var1 + 1) * 320 && this.scHeight >= (var1 + 1) * 240)
            {
                ++var1;
            }
        }
        else
        {
            var1 = this.mapScale - 1;
        }

        int var2 = this.scaledResolution.scaleFactor + 1 >> 1;

        int var3 = (int) ((this.mapPosition & 2) == 0 ? 33 * var1 : this.scWidth - 33 * var1);
        int var4 = (int) ((this.mapPosition & 1) == 0 ? 33 * var1 : this.scHeight - 33 * var1);

        if ((this.mapPosition & 1) == 1)
        {
            var4 -= ((this.showMenuKey | CJB.mmcoords ? 2 : 0) + (this.showMenuKey ? 9 : 0) + (CJB.mmcoords ? 18 : 0)) * var2;
        }

        GL11.glTranslated(var3, var4, 0.0D);
        GL11.glScalef(var1, var1, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColorMask(false, false, false, false);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
        GL11.glDepthMask(true);

        CJB_GLTexture.SQUARE_MAP_MASK.bind();
        this.drawCenteringRectangle(0.0D, 0.0D, 1.001D, 64.0D, 64.0D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        double var5 = 0.25D / this.currentZoom;
        double var7 = (this.thePlayer.posX - this.lastX) * 0.00390625D;
        double var9 = (this.thePlayer.posZ - this.lastZ) * 0.00390625D;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, this.mapOpacity);
        this.texture.bind();
        this.startDrawingQuads();

        if (this.notchDirection)
        {
            this.addVertexWithUV(32.0D, 32.0D, 1.0D, 0.5D + var5 + var7, 0.5D + var5 + var9);
            this.addVertexWithUV(32.0D, -32.0D, 1.0D, 0.5D + var5 + var7, 0.5D - var5 + var9);
            this.addVertexWithUV(-32.0D, -32.0D, 1.0D, 0.5D - var5 + var7, 0.5D - var5 + var9);
            this.addVertexWithUV(-32.0D, 32.0D, 1.0D, 0.5D - var5 + var7, 0.5D + var5 + var9);
        }
        else
        {
            this.addVertexWithUV(-32.0D, 32.0D, 1.0D, 0.5D + var5 + var7, 0.5D + var5 + var9);
            this.addVertexWithUV(32.0D, 32.0D, 1.0D, 0.5D + var5 + var7, 0.5D - var5 + var9);
            this.addVertexWithUV(32.0D, -32.0D, 1.0D, 0.5D - var5 + var7, 0.5D - var5 + var9);
            this.addVertexWithUV(-32.0D, -32.0D, 1.0D, 0.5D - var5 + var7, 0.5D + var5 + var9);
        }

        this.draw();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Iterator var13;
        int var15;
        double var20;

        {
            float var11 = 29.0F;
            ArrayList var12 = new ArrayList();
            var12.addAll(this.theWorld.loadedEntityList);
            var13 = var12.iterator();
            Entity var14;

            while (var13.hasNext())
            {
                var14 = (Entity)var13.next();

                if (var14 != null)
                {
                    var15 = this.getEntityColor(var14);

                    if (var15 != 0)
                    {
                        double var16 = this.thePlayer.posX - var14.posX;
                        double var18 = this.thePlayer.posZ - var14.posZ;
                        var16 = var16 * this.currentZoom * 0.5D;
                        var18 = var18 * this.currentZoom * 0.5D;
                        var20 = Math.max(Math.abs(var16), Math.abs(var18));

                        try
                        {
                            GL11.glPushMatrix();

                            if (var20 < var11)
                            {
                                float var22 = (var15 >> 16 & 255) * 0.003921569F;
                                float var23 = (var15 >> 8 & 255) * 0.003921569F;
                                float var24 = (var15 & 255) * 0.003921569F;
                                float var25 = var14 instanceof EntityPlayer ? 1f : (float)Math.max(0.0D, 1.0D - Math.abs(this.thePlayer.posY - var14.posY) * 0.06D);
                                GL11.glColor4f(var22, var23, var24, var25);
                                double var27;
                                double var29;

                                if (this.notchDirection)
                                {
                                    var27 = -var16;
                                    var29 = -var18;
                                }
                                else
                                {
                                    var27 = var18;
                                    var29 = -var16;
                                }
                                CJB_GLTexture.ENTITY.bind();
                                this.drawCenteringRectangle(var27, var29, 1.0D, 8.0D, 8.0D);
                            }
                        }
                        finally
                        {
                            GL11.glPopMatrix();
                        }
                    }
                }
            }
        }
        
        {
            float var11 = 29.0F;
            ArrayList var12 = new ArrayList();
            var12.addAll(CJB.mmwaypoints);
            var13 = var12.iterator();
            CJB_Data var14;

            while (var13.hasNext())
            {
                var14 = (CJB_Data)var13.next();

                if (var14 != null)
                {
                	double var16 = this.thePlayer.posX - var14.posx-0.5d;
                	double var18 = this.thePlayer.posZ - var14.posz-0.5d;
                	var16 = var16 * this.currentZoom * 0.5D;
                	var18 = var18 * this.currentZoom * 0.5D;
                	var20 = Math.max(Math.abs(var16), Math.abs(var18));

                	try
                	{
                		GL11.glPushMatrix();

                		if (var20 < var11)
                		{
                			GLColor(waypointcolor);
                			double var27;
                			double var29;

                			if (this.notchDirection)
                			{
                				var27 = -var16;
                				var29 = -var18;
                			}
                			else
                			{
                				var27 = var18;
                				var29 = -var16;
                			}
                			CJB_GLTexture.WAYPOINT.bind();
                			this.drawCenteringRectangle(var27, var29, 1.0D, 10.0D, 10.0D);
                		}
                	}
                	finally
                	{
                		GL11.glPopMatrix();
                	}
                }
            }
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, this.mapOpacity);
        
        if (!CJB.mmnoborder) {
        	CJB_GLTexture.SQUARE_MAP.bind();
        	this.drawCenteringRectangle(0.0D, 0.0D, 1.0D, 64.0D, 64.0D);
        }
        
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        try
        {
            GL11.glColor3f(1.0F, 1.0F, 1.0F);
            GL11.glPushMatrix();
            CJB_GLTexture.MMARROW.bind();
            GL11.glRotatef(this.thePlayer.rotationYaw - (this.notchDirection ? 180.0F : 90.0F), 0.0F, 0.0F, 1.0F);
            this.drawCenteringRectangle(0.0D, 0.0D, 1.0D, 3.0D, 4.0D);
        }
        catch (Exception var56){}
        finally
        {
            GL11.glPopMatrix();
        }
        
        if (CJB.mmshowallwp) {
        	for (CJB_Data way : CJB.mmwaypoints) {
        		double d1 = thePlayer.posX - way.posx-0.5d;
	            double d2 = thePlayer.posZ - way.posz-0.5d;
	            int disint = (int)Math.sqrt((d1*d1)+(d2*d2));
	            double d = -Math.toDegrees(Math.atan2(d1,d2))-90d;
	            
	            double var11 = Math.sin(Math.toRadians(-d)) * 30.5D;
	            double var44 = Math.cos(Math.toRadians(-d)) * 30.5D;
	            
	            CJB_GLTexture.WAYPOINTMARKER.bind();
	            GLColor(0xff000000 + way.color);
	            if (disint > 55 / this.currentZoom)
	            	this.drawCenteringRectangle(var44, -var11, 1.0D, 10.0D, 10.0D);
        	}
        } else {
	        if (CJB_GuiMinimap.waypoint != null)
	        {
	        	CJB_Data way = CJB_GuiMinimap.waypoint.getDataFromList(CJB.mmwaypoints);
	            
	            double d1 = thePlayer.posX - way.posx-0.5d;
	            double d2 = thePlayer.posZ - way.posz-0.5d;
	            int disint = (int)Math.sqrt((d1*d1)+(d2*d2));
	            double d = -Math.toDegrees(Math.atan2(d1,d2))-90d;
	            
	            double var11 = Math.sin(Math.toRadians(-d)) * 30.5D;
	            double var44 = Math.cos(Math.toRadians(-d)) * 30.5D;
	            
	            CJB_GLTexture.WAYPOINTMARKER.bind();
	            GLColor(0xff000000 + way.color);
	            if (disint > 55 / this.currentZoom)
	            	this.drawCenteringRectangle(var44, -var11, 1.0D, 10.0D, 10.0D);
	            
	        }
        }
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glScaled(1.0D / var1, 1.0D / var1, 1.0D);
        FontRenderer var63 = this.theMinecraft.fontRenderer;
        int var70;
        int var77;

        int var68 = (int) (32 * var1);
        String var71;
        float var73;

        if (CJB.mmcoords)
        {
            String var79;

            var70 = MathHelper.floor_double(this.thePlayer.posX);
            var77 = MathHelper.floor_double(this.thePlayer.boundingBox.minY);
            int var78 = MathHelper.floor_double(this.thePlayer.posZ);
            var71 = String.format("%d, %d", new Object[] {Integer.valueOf(var70), Integer.valueOf(var78)});
            var79 = Integer.toString(var77);

            var73 = var63.getStringWidth(var71) * 0.5F * var2;
            float var75 = var63.getStringWidth(var79) * 0.5F * var2;
            float var76 = (37 * var1) < var73 ? (37 * var1) - var73 : 0.0F;

            if ((this.mapPosition & 2) == 0)
            {
                var76 = -var76;
            }

            GL11.glTranslatef(var76 - var73, var68, 0.0F);
            GL11.glScalef(var2, var2, 1.0F);
            var63.drawStringWithShadow(var71, 0, 2, 16777215);
            GL11.glScaled(1.0D / var2, 1.0D / var2, 1.0D);
            GL11.glTranslatef(var73 - var75, 0.0F, 0.0F);
            GL11.glScalef(var2, var2, 1.0F);
            var63.drawStringWithShadow(var79, 0, 11, 16777215);
            GL11.glScaled(1.0D / var2, 1.0D / var2, 1.0D);
            GL11.glTranslatef(var75 - var76, (-var68), 0.0F);
            var68 += 36 * var2;
        }
        
        if (CJB_GuiMinimap.waypoint != null)
        {
        	CJB_Data way = CJB_GuiMinimap.waypoint.getDataFromList(CJB.mmwaypoints);
            
            double d1 = thePlayer.posX + -way.posx;
            double d2 = thePlayer.posZ + -way.posz;
            int disint = (int)Math.sqrt((d1*d1)+(d2*d2));
            String dis = Integer.toString(disint) + "m";
            String name = way.Name;
            
            var73 = var63.getStringWidth(name) * 0.5F * var2;
            float var75 = var63.getStringWidth(dis) * 0.5F * var2;
            
            float var76 = (37 * var1) < var73 ? (37 * var1) - var73 : 0.0F;

            if ((this.mapPosition & 2) == 0)
            {
            	var76 = -var76;
            }

            GL11.glTranslatef(var76 - var73, var68, 0.0F);
            GL11.glScalef(var2, var2, 1.0F);
            var63.drawStringWithShadow(name, 0, 2, 16777215);
            GL11.glScaled(1.0D / var2, 1.0D / var2, 1.0D);
            GL11.glTranslatef(var73 - var75, 0.0F, 0.0F);
            GL11.glScalef(var2, var2, 1.0F);
            var63.drawStringWithShadow(dis, 0, 11, 16777215);
            GL11.glScaled(1.0D / var2, 1.0D / var2, 1.0D);
            GL11.glTranslatef(var75 - var76, (-var68), 0.0F);
            var68 += 18 * var2;
        }
        
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private void updateLightmap(long var1, float var3)
    {
        float var4 = this.func_35464_b(var1, var3);

        for (int var5 = 0; var5 < 256; ++var5)
        {
            float var6 = var4 * 0.95F + 0.05F;
            float var7 = this.theWorld.worldProvider.lightBrightnessTable[var5 / 16] * var6;
            float var8 = this.theWorld.worldProvider.lightBrightnessTable[var5 % 16] * 1.55F;

            if (this.theWorld.lightningFlash > 0)
            {
                var7 = this.theWorld.worldProvider.lightBrightnessTable[var5 / 16];
            }

            float var9 = var7 * (var4 * 0.65F + 0.35F);
            float var10 = var7 * (var4 * 0.65F + 0.35F);
            float var13 = var8 * ((var8 * 0.6F + 0.4F) * 0.6F + 0.4F);
            float var14 = var8 * (var8 * var8 * 0.6F + 0.4F);
            float var15 = var9 + var8;
            float var16 = var10 + var13;
            float var17 = var7 + var14;
            var15 = Math.min(1.0F, var15 * 0.96F + 0.03F);
            var16 = Math.min(1.0F, var16 * 0.96F + 0.03F);
            var17 = Math.min(1.0F, var17 * 0.96F + 0.03F);
            float var18 = this.theMinecraft.gameSettings.gammaSetting;
            float var19 = 1.0F - var15;
            float var20 = 1.0F - var16;
            float var21 = 1.0F - var17;
            var19 = 1.0F - var19 * var19 * var19 * var19;
            var20 = 1.0F - var20 * var20 * var20 * var20;
            var21 = 1.0F - var21 * var21 * var21 * var21;
            var15 = var15 * (1.0F - var18) + var19 * var18;
            var16 = var16 * (1.0F - var18) + var20 * var18;
            var17 = var17 * (1.0F - var18) + var21 * var18;
            this.lightmapRed[var5] = Math.max(0.0F, Math.min(1.0F, var15 * 0.96F + 0.03F));
            this.lightmapGreen[var5] = Math.max(0.0F, Math.min(1.0F, var16 * 0.96F + 0.03F));
            this.lightmapBlue[var5] = Math.max(0.0F, Math.min(1.0F, var17 * 0.96F + 0.03F));
        }
    }

    private float func_35464_b(long var1, float var3)
    {
        float var4 = this.calculateCelestialAngle(var1) + var3;
        float var5 = Math.max(0.0F, Math.min(1.0F, 1.0F - (MathHelper.cos(var4 * (float)Math.PI * 2.0F) * 2.0F + 0.2F)));
        var5 = 1.0F - var5;
        var5 *= 1.0F - this.theWorld.getRainStrength(1.0F) * 5.0F * 0.0625F;
        var5 *= 1.0F - this.theWorld.getWeightedThunderStrength(1.0F) * 5.0F * 0.0625F;
        return var5 * 0.8F + 0.2F;
    }

    private float calculateCelestialAngle(long var1)
    {
        int var3 = (int)(var1 % 24000L);
        float var4 = (var3 + 1) * 4.1666666E-5F - 0.25F;

        if (var4 < 0.0F)
        {
            ++var4;
        }
        else if (var4 > 1.0F)
        {
            --var4;
        }

        float var5 = var4;
        var4 = 1.0F - (float)((Math.cos(var4 * Math.PI) + 1.0D) * 0.5D);
        var4 = var5 + (var4 - var5) * 0.33333334F;
        return var4;
    }

    private Chunk getChunk(World var1, int var2, int var3)
    {
        boolean var4 = Math.abs(this.chunkCoordX - var2) <= 8 && Math.abs(this.chunkCoordZ - var3) <= 8;
        return (var4 ? this.chunkCache.get(var1, var2, var3) : new EmptyChunk(var1, var2, var3));
    }

    private void drawCenteringRectangle(double var1, double var3, double var5, double var7, double var9)
    {
        var7 *= 0.5D;
        var9 *= 0.5D;
        this.startDrawingQuads();
        this.addVertexWithUV(var1 - var7, var3 + var9, var5, 0.0D, 1.0D);
        this.addVertexWithUV(var1 + var7, var3 + var9, var5, 1.0D, 1.0D);
        this.addVertexWithUV(var1 + var7, var3 - var9, var5, 1.0D, 0.0D);
        this.addVertexWithUV(var1 - var7, var3 - var9, var5, 0.0D, 0.0D);
        this.draw();
    }

    public static String toUpperCase(String var0)
    {
        return var0 == null ? null : var0.replace(' ', '_').toUpperCase(Locale.ENGLISH);
    }

    private static boolean checkGuiScreen()
    {
        return true;//var0 == null || var0 instanceof GuiChat || var0 instanceof GuiGameOver;
    }

    String getDimensionName(int var1)
    {
        String var2 = (String)this.dimensionName.get(Integer.valueOf(var1));
        return var2 == null ? "DIM:" + var1 : var2;
    }

    int getCurrentDimension()
    {
        return this.currentDimension;
    }

    boolean isMinecraftThread()
    {
        return Thread.currentThread() == this.mcThread;
    }

    static final int version(int var0, int var1, int var2, int var3)
    {
        return (var0 & 255) << 24 | (var1 & 255) << 16 | (var2 & 255) << 8 | (var3 & 255) << 0;
    }

    int getWorldHeight()
    {
        return this.worldHeight;
    }
    
    private void GLColor(int color) {
    	float f  = (color >> 24 & 0xff) * 0.003921569F;
    	float f1 = (color >> 16 & 0xff) * 0.003921569F;
        float f2 = (color >> 8 & 0xff) * 0.003921569F;
        float f3 = (color & 0xff) * 0.003921569F;
        
        GL11.glColor4f(f1, f2, f3, f);
    }

    private int getEntityColor(Entity ent)
    {
    	if (ent == thePlayer)
			return 0;
    	
    	if (CJB.mmplayers && CJB.pmentity && ent instanceof EntityPlayer) 
    		return 0x0000FF;
    	
    	if (CJB.mmmobs && ent instanceof EntityLiving) {
	    	if (ent instanceof EntityMob) {
	    		return 0xFF0000;
	    	}
	    	
	    	if (ent instanceof EntityAnimal) {
	    		
	    		if (ent instanceof EntitySquid)
	    			return 0x5555FF;
	    		
	    		if (ent instanceof EntityTameable && ((EntityTameable) ent).isTamed())
	    			return 0xbbbbbb;
	    		
	    		return 0x00FF00;
	    	}
	    	
	    	if (ent instanceof EntityVillager)
	    		return 0xFF4080;
	    	
	    	return 0x00FF00;
    	}
    	
    	if (CJB.mmitems && ent instanceof EntityItem)
    		return 0xffffff;
    	
    	return 0;
    }

    boolean getMarker()
    {
        return this.marker & (this.markerIcon | this.markerLabel | this.markerDistance);
    }

    boolean getMarkerIcon()
    {
        return this.markerIcon;
    }

    boolean getMarkerLabel()
    {
        return this.markerLabel;
    }

    boolean getMarkerDistance()
    {
        return this.markerDistance;
    }

    static GuiIngame access$000(CJB_Minimap var0)
    {
        return var0.ingameGUI;
    }

    static
    {
        LinkedList var0 = new LinkedList();
        BiomeGenBase[] var1 = BiomeGenBase.biomeList;
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3)
        {
            BiomeGenBase var4 = var1[var3];

            if (var4 != null)
            {
                var0.add(var4);
            }
        }

        InputStream var7 = GuiIngame.class.getResourceAsStream(GuiIngame.class.getSimpleName() + ".class");

        if (var7 != null)
        {
            try
            {
                ByteArrayOutputStream var9 = new ByteArrayOutputStream();
                byte[] var10 = new byte[4096];

                while (true)
                {
                    int var11 = var7.read(var10);

                    if (var11 == -1)
                    {
                        var7.close();
                        String var12 = (new String(var9.toByteArray(), "UTF-8")).toLowerCase(Locale.ENGLISH);

                        if (var12.indexOf("\u00a70\u00a70") != -1 && var12.indexOf("\u00a7e\u00a7f") != -1)
                        {
                            instance.texture.unregister();
                            instance.texture = null;
                            instance.chunkCache.clear();
                            instance.chunkCache = null;
                        }

                        break;
                    }

                    var9.write(var10, 0, var11);
                }
            }
            catch (Exception var5)
            {
                ;
            }
        }

        temp = new float[10];
        float var6 = 0.0F;
        int var8;

        for (var8 = 0; var8 < temp.length; ++var8)
        {
            temp[var8] = (float)(1.0D / Math.sqrt((var8 + 1)));
            var6 += temp[var8];
        }

        var6 = 0.3F / var6;

        for (var8 = 0; var8 < temp.length; ++var8)
        {
            temp[var8] *= var6;
        }

        var6 = 0.0F;

        for (var8 = 0; var8 < 10; ++var8)
        {
            var6 += temp[var8];
        }
    }
}
