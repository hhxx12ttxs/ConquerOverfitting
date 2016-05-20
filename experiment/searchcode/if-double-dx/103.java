package slimevoid.infection.core;

/*
 * TODO:
 * V	- Ameliorer le fond du lobby
 * V	- Finir le block spawn
 * 		- Interdir la pose de block
 * 		- Debuger le point de spawn joueur
 * 		- Finir le mode Spec
 * 		- Agrandir la limite de construction dans l'infection
 */

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_DST_COLOR;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor4d;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glGetBoolean;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScaled;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTexCoord2d;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2d;
import static org.lwjgl.opengl.GL11.glVertex3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.src.ModLoader;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.lwjgl.util.vector.Vector3f;

import slimevoid.infection.InfectionGameSession;
import slimevoid.infection.InfectionGamemode;
import slimevoid.infection.blocks.BlockGroundDiamond;
import slimevoid.infection.blocks.BlockInfected;
import slimevoid.infection.blocks.BlockInfectedLeaves;
import slimevoid.infection.blocks.BlockInfectedSky;
import slimevoid.infection.blocks.BlockInfectedWater;
import slimevoid.infection.blocks.BlockInfectedWood;
import slimevoid.infection.blocks.BlockSpawn;
import slimevoid.infection.blocks.RenderDiamond;
import slimevoid.infection.blocks.RenderSpawnBlock;
import slimevoid.infection.blocks.TileEntityDiamond;
import slimevoid.infection.blocks.TileEntitySpawnBlock;
import slimevoid.infection.core.cutscene.CutsceneSpec;
import slimevoid.infection.core.cutscene.CutsceneStart;
import slimevoid.infection.core.cutscene.GuiCutscene;
import slimevoid.infection.entities.EntityInfectedArrow;
import slimevoid.infection.entities.RenderInfectedArrow;
import slimevoid.infection.fx.TextureInfectedWaterFX;
import slimevoid.infection.gui.GuiLobby;
import slimevoid.infection.gui.GuiOverlayInfection;
import slimevoid.infection.mobs.EntityInfectedSkeleton;
import slimevoid.infection.mobs.EntityInfectedSpider;
import slimevoid.infection.mobs.EntityInfectedWorm;
import slimevoid.infection.mobs.EntityInfectedWormRenderer;
import slimevoid.infection.mobs.EntityInfectedZombie;
import slimevoid.lib.ICommonProxy;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(
		modid = "InfectionMod",
		name = "Infection",
		version = "1.0.0.0"
)
public class InfectionMod {
	@Instance("InfectionMod")
	public static InfectionMod instance;
	
	private long sessionStartTime;
	private long sessionEndTime;
	
	private GuiOverlayInfection overlay;
	private float animOverlay;
	private float prevAlphaOverlay;
	private float alphaOverlay;
	private boolean inInfectedWater = false;
	private boolean inInfection = false;
 	private List<AmbientDot> ambientInfection;
 	private Random rand = new Random();
 	private boolean nearAreaBorder = false;
 	private double distAreaBorder = 0;
 	
 	private Map<String, Boolean> playersReadyness;
 	
	private int sessionStatus = -1;
 	
 	public static ChunkCoordinates infectionPos;
	
	public InfectionGamemode gamemode;
	public static InfectionGameSession gameSession;
	
	@SidedProxy(
			clientSide="slimevoid.infection.client.proxy.IN_ClientProxy",
			serverSide="slimevoid.infection.proxy.IN_CommonProxy")
	public static ICommonProxy proxy;
	
	@Init
	public void InfectionModInit(FMLInitializationEvent event) {
		gamemode = new InfectionGamemode("Infection", 4);
		
		INFECTED = new BlockInfected(1100).setHardness(100F).setStepSound(Block.soundGrassFootstep);
		INFECTED_SKY = new BlockInfectedSky(1101).setHardness(100F);
		INFECTED_WOOD = new BlockInfectedWood(1102).setHardness(100F);
		INFECTED_WATER = new BlockInfectedWater(1103).setHardness(100F).setStepSound(Block.soundGrassFootstep);
		INFECTED_LEAVES = new BlockInfectedLeaves(1104).setHardness(.2F);
		SPAWN = new BlockSpawn(1105, 20).setHardness(-1);
		GROUND_DIAMOND = new BlockGroundDiamond(1106).setHardness(-1);
		GameRegistry.registerBlock(INFECTED, "Infected Block");
		// TODO :: Huh? blocksNotifyOnAddAndRm[INFECTED.blockID] = true;
		GameRegistry.registerBlock(INFECTED_SKY, "Infected Sky");
		GameRegistry.registerBlock(INFECTED_WOOD, "Infected Wood");
		GameRegistry.registerBlock(INFECTED_WATER, "Infected Water");
		GameRegistry.registerBlock(INFECTED_LEAVES, "Infected Leaves");
		GameRegistry.registerBlock(SPAWN, "Infected Spawner");
		GameRegistry.registerBlock(GROUND_DIAMOND, "Diamond Ground");
		LanguageRegistry.addName(INFECTED, "Infected Block");
		LanguageRegistry.addName(INFECTED_SKY, "Infected Sky");
		LanguageRegistry.addName(INFECTED_WOOD, "Infected Wood");
		LanguageRegistry.addName(INFECTED_WATER, "Infected Water");
		LanguageRegistry.addName(INFECTED_LEAVES, "Infected Leaves");
		LanguageRegistry.addName(SPAWN, "Infected Spawner");
		LanguageRegistry.addName(GROUND_DIAMOND, "Diamond Ground");
		
		// TODO :: Tick Handler ModLoader.setInGameHook(this, true, true);
		
		EntityRegistry.registerGlobalEntityID(EntityInfectedSpider.class, "Infected Spider", EntityRegistry.findGlobalUniqueEntityId());
		EntityRegistry.registerGlobalEntityID(EntityInfectedZombie.class, "Infected Zombie", EntityRegistry.findGlobalUniqueEntityId());
		EntityRegistry.registerGlobalEntityID(EntityInfectedSkeleton.class, "Infected Skeleton", EntityRegistry.findGlobalUniqueEntityId());
		EntityRegistry.registerGlobalEntityID(EntityInfectedWorm.class, "Infected Worm", EntityRegistry.findGlobalUniqueEntityId());
		EntityRegistry.registerGlobalEntityID(EntityInfectedArrow.class, "Infected Arrow", EntityRegistry.findGlobalUniqueEntityId());
		// TODO :: PacketHandlers ModLoaderMp.registerNetClientHandlerEntity(EntityInfectedArrow.class, 110);
		
		GameRegistry.registerTileEntity(TileEntityDiamond.class, "Diamond");
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDiamond.class, new RenderDiamond());
		GameRegistry.registerTileEntity(TileEntitySpawnBlock.class, "Spawn Block");
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySpawnBlock.class, new RenderSpawnBlock());
		
		ambientInfection = new ArrayList<AmbientDot>();
		overlay = new GuiOverlayInfection();
		playersReadyness = new HashMap<String, Boolean>();
		sessionStartTime = -1;
		sessionEndTime = -1;
		CraftingManager.getInstance().getRecipeList().clear();
		
		gameSession = new InfectionGameSession(gamemode);
	}
	
	public void addRenderer(Map map) {
		map.put(EntityInfectedWorm.class, new EntityInfectedWormRenderer());
		map.put(EntityInfectedArrow.class, new RenderInfectedArrow());
	}
	
	public void registerAnimation(Minecraft minecraft) {
		minecraft.renderEngine.registerTextureFX(new TextureInfectedWaterFX());
	}

	public boolean renderWorldBlock(RenderBlocks renderblocks, IBlockAccess iblockaccess, int i, int j, int k, Block block, int l) {
		return false;//super.renderWorldBlock(renderblocks, iblockaccess, i, j, k, block, l);
	}

	protected void renderWorld(float frameDelta) {
		Minecraft mc = FMLClientHandler.instance().getClient();
		EntityPlayer player = mc.thePlayer;
		World world = mc.theWorld;
		/* Ambience Infection */
		if (!inInfection && ambientInfection.size() > 0) {
			ambientInfection.clear();
		}
				
		if (inInfection) {
			if (ambientInfection.size() < 2000) {
				for (int bi = 0; bi < 10; bi ++) {
					ambientInfection.add(new AmbientDot(player.posX + 64 * rand.nextFloat() - 32, player.posY + 24 * rand.nextFloat() - 12, player.posZ + 64 * rand.nextFloat() - 32));
				}
			}
			
			boolean oldParm = glGetBoolean(GL_TEXTURE_2D);
			boolean oldParm2 = glGetBoolean(GL_BLEND);
			glDisable(GL_TEXTURE_2D);
			glEnable(GL_BLEND);
			
			for (int iDot = 0; iDot < ambientInfection.size(); iDot ++) {
				AmbientDot dot = ambientInfection.get(iDot);
				
				glPushMatrix();
					double x = dot.pos.x;
					double y = dot.pos.y;
					double z = dot.pos.z;
					
					glColor4d(1, 1, 1, dot.light / 2f);
					glPointSize(3.0f);
					glBegin(GL_POINTS);
						glVertex3d(x, y + dot.light, z);
					glEnd();
				
				glPopMatrix();
				
				dot.light += (0.015f) ;
				if (dot.light > 1) {
					ambientInfection.remove(iDot);
					iDot --;
				}
			}
				
			if (oldParm) {
				glEnable(GL_TEXTURE_2D);
			}
			if (!oldParm2) {
				glDisable(GL_BLEND);
			}
		}
		
		/* Spawn block & infection area */
		if (player.getCurrentEquippedItem() != null && world != null) {
			if (player.getCurrentEquippedItem().itemID == SPAWN.blockID) {
				/*OLD
				glDisable(GL_TEXTURE_2D);
		    	glEnable(GL_BLEND);
		    	glDisable(GL_CULL_FACE);	
		    	
//		    	if(mc.gameSettings.fancyGraphics) {
//		    		glDisable(GL_ALPHA_TEST);
//			    	drawAroundSpawnCube(world, player, 0);
//			    	glEnable(GL_ALPHA_TEST);
//		    	}
		    	drawCanPlaceSpawnCube(world);
			    
		    	glEnable(GL_TEXTURE_2D);
		    	glDisable(GL_BLEND);
		    	*/
			}
		}
		
		/* Area Border*/
		if (nearAreaBorder) {
			glPushMatrix();
				glEnable(GL_BLEND);
				glDisable(GL_TEXTURE_2D);
				glTranslated(world.getSpawnPoint().posX, player.posY, world.getSpawnPoint().posZ);
				
				double alpha = ((float)distAreaBorder - (180f * 180f)) / (20f * 20f) / 20f;
				
				mc.renderEngine.bindTexture(mc.renderEngine.getTexture("%blur%/misc/glint.png"));
		        glEnable(GL_BLEND);
		        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
		        glEnable(GL_TEXTURE_2D);
		        
		        float light = 1F;
		        glColor4d(1F * light, 0.16F * light, .16F * light, alpha);
		        
		        glMatrixMode(GL_TEXTURE);
		        glPushMatrix();
			        float scale = .625F;
			        glScalef(0.5f, scale, scale);
			        float f9 = ((System.currentTimeMillis() % 16000L) / 16000F) * 4F;
			        glTranslatef(f9, 0.0F, 0.0F);
			        glRotatef(-90F, 0.0F, 0.0F, 1.0F);
			        glMatrixMode(GL_MODELVIEW);
			        glBegin(GL_QUADS);
			        drawCylinder(64, 200.5f, 64);
			        glEnd();
			        glMatrixMode(GL_TEXTURE);
		        glPopMatrix();
		        
		        glMatrixMode(GL_MODELVIEW);
		        glDepthFunc(GL_LEQUAL);
		        glDisable(GL_TEXTURE_2D);
		        
		        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_DST_COLOR);
		        glPushMatrix();
					glEnable(GL_LINE_SMOOTH);
					glPolygonMode(GL_FRONT, GL_LINE);
					glColor4d(0.4f, 0.2f, 0.2f, alpha);
					glLineWidth(1.2f);
					glBegin(GL_LINES);
						drawCylinder(64, 200.5f, 64);
					glEnd();
					glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
					glEnable(GL_BLEND);
				glPopMatrix();
				glDisable(GL_BLEND);
				glEnable(GL_TEXTURE_2D);
			glPopMatrix();
		}
	}
	
	public static boolean isInGameArea(int x, int y, int z) {
		double dx = infectionPos.posX - x;
		double dy = infectionPos.posY - y;
		double dz = infectionPos.posZ - z;
		
		double dist = sqrt(dx * dx + dy * dy + dz * dz);
//		System.out.println(dist);
		return dist > 50 && dist < 150;
	}
	
	private void drawCanPlaceSpawnCube(World world) {
		Minecraft mc = FMLClientHandler.instance().getClient();
		glPushMatrix();
			if(mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
				int x = mc.objectMouseOver.blockX;
				int y = mc.objectMouseOver.blockY;
				int z = mc.objectMouseOver.blockZ;
				if(!world.canPlaceEntityOnSide(
						SPAWN.blockID,
						x,
						y,
						z,
						true,
						mc.objectMouseOver.sideHit,
						mc.thePlayer) && mc.objectMouseOver.sideHit == 1) {
					y ++;
				}
				glTranslated(x + .5, y + .2501, z + .5);
				glScaled(.5, .5, .5);
				if (world.canPlaceEntityOnSide(
						SPAWN.blockID,
						x,
						y, 
						z,
						false,
						mc.objectMouseOver.sideHit,
						mc.thePlayer) && isInGameArea(x, y, z)) {
	    			glColor4f(0.18f, 1, 0.18f, .4F);
	    		} else if(world.canPlaceEntityOnSide(
	    				SPAWN.blockID,
	    				x,
	    				y,
	    				z,
	    				true,
	    				mc.objectMouseOver.sideHit,
	    				mc.thePlayer) && isInGameArea(x, y, z)) {
	    			glColor4f(1F, 1F, .18F, .4F);
	    		} else {
	    			glColor4f(1, 0.18f, 0.18f, .4F);
	    		}
				glBegin(GL_QUADS);
		    		drawCube(); 
	    		glEnd();
			}
		glPopMatrix();
	}
	
	public void drawCylinder(int slices, double diam, double height) {
		double aStep = (2 * PI) / slices;
		double x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4;
		for (double angle = 0; angle <= 2 * PI - aStep; angle += aStep) {
			x1 = diam * cos(angle + aStep); y1 =  height / 2; z1 = diam * sin(angle + aStep);
			x2 = diam * cos(angle        ); y2 =  height / 2; z2 = diam * sin(angle        );
			x3 = diam * cos(angle        ); y3 = -height / 2; z3 = diam * sin(angle        );
			x4 = diam * cos(angle + aStep); y4 = -height / 2; z4 = diam * sin(angle + aStep);
			glTexCoord2d(1, 0);
			glVertex3d(x1, y1, z1);
			glTexCoord2d(0, 0);
			glVertex3d(x2, y2, z2);
			glTexCoord2d(0, 1);
			glVertex3d(x3, y3, z3);
			glTexCoord2d(1, 1);
			glVertex3d(x4, y4, z4);
		}
	}
	public void drawCube() {
		drawCube(true, true, true, true, true, true);
	}
	
	public void drawCube(boolean bottom, boolean top, boolean front, boolean back, boolean left, boolean right) {
		if(back) {
			glTexCoord2d(0, 0); glVertex3d(-.5, -.5, -.5);
			glTexCoord2d(0, 1); glVertex3d(-.5, +.5, -.5);
			glTexCoord2d(1, 1); glVertex3d(+.5, +.5, -.5);
			glTexCoord2d(1, 0); glVertex3d(+.5, -.5, -.5);
		}
		
		if(front) {
			glTexCoord2d(0, 0); glVertex3d(-.5, -.5, +.5);
			glTexCoord2d(1, 0); glVertex3d(+.5, -.5, +.5);
			glTexCoord2d(1, 1); glVertex3d(+.5, +.5, +.5);
			glTexCoord2d(0, 1); glVertex3d(-.5, +.5, +.5);
		}
		
		if(bottom) {
			glTexCoord2d(0, 0); glVertex3d(-.5, -.5, -.5);
			glTexCoord2d(1, 0); glVertex3d(+.5, -.5, -.5);
			glTexCoord2d(1, 1); glVertex3d(+.5, -.5, +.5);
			glTexCoord2d(0, 1); glVertex3d(-.5, -.5, +.5);
		}
		
		if(top) {
			glTexCoord2d(0, 0); glVertex3d(-.5, +.5, -.5);
			glTexCoord2d(0, 1); glVertex3d(-.5, +.5, +.5);
			glTexCoord2d(1, 1); glVertex3d(+.5, +.5, +.5);
			glTexCoord2d(1, 0); glVertex3d(+.5, +.5, -.5);
		}
		
		if(left) {
			glTexCoord2d(0, 0); glVertex3d(-.5, -.5, -.5);
			glTexCoord2d(1, 0); glVertex3d(-.5, -.5, +.5);
			glTexCoord2d(1, 1); glVertex3d(-.5, +.5, +.5);
			glTexCoord2d(0, 1); glVertex3d(-.5, +.5, -.5);
		}
		
		if(right) {
			glTexCoord2d(0, 0); glVertex3d(+.5, -.5, -.5);
			glTexCoord2d(0, 1); glVertex3d(+.5, +.5, -.5);
			glTexCoord2d(1, 1); glVertex3d(+.5, +.5, +.5);
			glTexCoord2d(1, 0); glVertex3d(+.5, -.5, +.5);
		}
	}
	
	public void drawLineCube(boolean bottom, boolean top, boolean front, boolean back, boolean left, boolean right) {
		if(back) {
			glVertex3d(-.5, +.5, -.5);
			glVertex3d(+.5, +.5, -.5);
			
			glVertex3d(+.5, +.5, -.5);
			glVertex3d(+.5, -.5, -.5);
			
			glVertex3d(+.5, -.5, -.5);
			glVertex3d(-.5, -.5, -.5);
			
			glVertex3d(-.5, -.5, -.5);
			glVertex3d(-.5, +.5, -.5);
		}
		
		if(front) {
			glVertex3d(-.5, +.5, +.5);
			glVertex3d(+.5, +.5, +.5);
			
			glVertex3d(+.5, +.5, +.5);
			glVertex3d(+.5, -.5, +.5);
			
			glVertex3d(+.5, -.5, +.5);
			glVertex3d(-.5, -.5, +.5);
			
			glVertex3d(-.5, -.5, +.5);
			glVertex3d(-.5, +.5, +.5);
		}
		
		if(bottom) {
			glVertex3d(-.5, -.5, +.5);
			glVertex3d(+.5, -.5, +.5);
			
			glVertex3d(+.5, -.5, +.5);
			glVertex3d(+.5, -.5, -.5);
			
			glVertex3d(+.5, -.5, -.5);
			glVertex3d(-.5, -.5, -.5);
			
			glVertex3d(-.5, -.5, -.5);
			glVertex3d(-.5, -.5, +.5);
		}
		
		if(top) {
			glVertex3d(-.5, +.5, +.5);
			glVertex3d(+.5, +.5, +.5);
			
			glVertex3d(+.5, +.5, +.5);
			glVertex3d(+.5, +.5, -.5);
			
			glVertex3d(+.5, +.5, -.5);
			glVertex3d(-.5, +.5, -.5);
			
			glVertex3d(-.5, +.5, -.5);
			glVertex3d(-.5, +.5, +.5);
		}
		
		if(left) {
			glVertex3d(-.5, -.5, +.5);
			glVertex3d(-.5, +.5, +.5);
			
			glVertex3d(-.5, +.5, +.5);
			glVertex3d(-.5, +.5, -.5);
			
			glVertex3d(-.5, +.5, -.5);
			glVertex3d(-.5, -.5, -.5);
			
			glVertex3d(-.5, -.5, -.5);
			glVertex3d(-.5, -.5, +.5);
		}
		
		if(right) {
			glVertex3d(+.5, -.5, +.5);
			glVertex3d(+.5, +.5, +.5);
			
			glVertex3d(+.5, +.5, +.5);
			glVertex3d(+.5, +.5, -.5);
			
			glVertex3d(+.5, +.5, -.5);
			glVertex3d(+.5, -.5, -.5);
			
			glVertex3d(+.5, -.5, -.5);
			glVertex3d(+.5, -.5, +.5);
		}
	}

	public void renderInvBlock(RenderBlocks renderblocks, Block block, int i, int j) {
		//super.renderInvBlock(renderblocks, block, i, j);
	}

	// TODO :: Moar Packet Shizzle
	public void handlePacket(Packet250CustomPayload packet) {
		/*switch(packet.dataInt[0]) {
			case 0: // INFECTION POS
				infectionPos = new ChunkCoordinates(packet.dataInt[1], packet.dataInt[2], packet.dataInt[3]);
				break;
			// 1 : BUY REQUEST
			// 2 : LOBBY READY
			case 3: // PLAYER READYNESS
				sessionStartTime = packet.dataInt[1] < 0 ? -1 : packet.dataInt[1] * 100 + System.currentTimeMillis();
				for(int i = 0; i < packet.dataString.length; i ++) {
					playersReadyness.put(packet.dataString[i], packet.dataInt[i + 2] == 1 ? true : false);
				}
				break;
				
			case 4: // SESSION STATUS
				sessionStatus = packet.dataInt[1];
				switch(packet.dataInt[1]) {
				case 0:
					if(!(mc.currentScreen instanceof GuiLobby)) {
						mc.displayGuiScreen(new GuiLobby());
					}
					break;
					
				case 1:
					if(isPlayerReady(mc.thePlayer.username)) {
						mc.displayGuiScreen(new GuiCutscene(new CutsceneStart(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)));
					}
					break;
				}
			// 5 : PLAYER FOLLOWING
		}*/
	}
	
	public void handleTileEntityPacket(int x, int y, int z, int metadata, int[] ai, float[] af, String[] as) {
		@SuppressWarnings("unused")
		World world = ModLoader.getMinecraftInstance().theWorld;
		switch(ai[0]) {
		}
	}
	
	protected void renderOverlay(float f, boolean flag, int i, int j) {
		//super.renderOverlay(f, flag, i, j);
		Minecraft mc = ModLoader.getMinecraftInstance();
		glPushMatrix();
		ScaledResolution scaledresolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
		glScaled(scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 1);
		
		if (alphaOverlay > 0) {
			
			animOverlay += 0.01F;
			
			float a = prevAlphaOverlay + (alphaOverlay - prevAlphaOverlay) * f;
			
			/* Grain effect */
			mc.renderEngine.bindTexture(mc.renderEngine.getTexture("/infection_overlay.png"));
			glColor4f(0.10f, 0.10f, 0.10f, a * .55F);
			int tileValue =32;
			glBegin(GL_QUADS);
				glTexCoord2d(0.03 * Math.sin(animOverlay), 0.01 * Math.cos(animOverlay + 1.57f));
				glVertex2d(0, 0);
				glTexCoord2d(0.01 * Math.sin(-animOverlay), 0.02 * Math.cos(animOverlay) + tileValue);
				glVertex2d(0, 1);
				glTexCoord2d(0.01 * Math.sin(animOverlay + 1.57f) + tileValue, 0.03 * Math.cos(animOverlay) + tileValue);
				glVertex2d(1, 1);
				glTexCoord2d(0.02 * Math.sin(-animOverlay - 1.57f) + tileValue, 0.02 * Math.cos(-animOverlay));
				glVertex2d(1, 0);
			glEnd();
			
			/* Vignette effect */
			mc.renderEngine.bindTexture(mc.renderEngine.getTexture("/view_overlay.png"));			
			glColor4f(0.1f, 0.1f, 0.1f, a + 0.2F);
			glBegin(GL_QUADS);
				glTexCoord2d(0, 0);
				glVertex2d(0, 0);
				glTexCoord2d(0, 1);
				glVertex2d(0, 1);
				glTexCoord2d(1, 1);
				glVertex2d(1, 1);
				glTexCoord2d(1, 0);
				glVertex2d(1, 0);
			glEnd();
		}
		
		if (inInfectedWater) {
			/* Under Infected Water effect */
			glDisable(GL_TEXTURE_2D);
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			
			glColor4f(0.01f, 0.2f, 0.01f, 0.6f);
			glBegin(GL_QUADS);
				glTexCoord2d(0, 0);
				glVertex2d(0, 0);
				glTexCoord2d(0, 1);
				glVertex2d(0, 1);
				glTexCoord2d(1, 1);
				glVertex2d(1, 1);
				glTexCoord2d(1, 0);
				glVertex2d(1, 0);
			glEnd();
			
		}
		
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_BLEND);
		
		glPopMatrix();
		
		overlay.drawScreen(i, j, f);
	}
	

	public boolean onTickInGame(float f, Minecraft minecraft) {
		EntityPlayer player = minecraft.thePlayer;
		World world = minecraft.theWorld;
		
		inInfection = world.getBlockId((int) player.posX, 128, (int) player.posZ) == INFECTED_SKY.blockID;
		prevAlphaOverlay = alphaOverlay;
		alphaOverlay += inInfection ? .0002 : -.0002;
		if(alphaOverlay < 0) {
			alphaOverlay = 0;
		} else if(alphaOverlay > 1) {
			alphaOverlay = 1;
		}
		if (player.isInWater() && world.getBlockId((int)(player.posX - 1), (int)player.posY, (int)(player.posZ)) == INFECTED_WATER.blockID) {
			inInfectedWater = true;
		} else {
			inInfectedWater = false;
		}
		
		/* Limit map */
		double dx = world.getSpawnPoint().posX - player.posX;
		double dy = world.getSpawnPoint().posY - player.posY;
		double dz = world.getSpawnPoint().posZ - player.posZ;
		distAreaBorder = dx * dx + dy * dy + dz * dz;
		nearAreaBorder = distAreaBorder > 180 * 180;
		
		if (distAreaBorder > 200 * 200 && distAreaBorder < 220 * 220 && !player.isDead) {
			player.knockBack(player, 0, player.motionX * 2, player.motionZ * 2);
			player.motionY = 0;
		}
		if(sessionStatus == 0 && !(minecraft.currentScreen instanceof GuiLobby)) {
			minecraft.displayGuiScreen(new GuiLobby());
		} else if (sessionStatus == 1 && !isPlayerReady(player.username) && !(minecraft.currentScreen instanceof GuiCutscene) && !(minecraft.currentScreen instanceof GuiGameOver)) {
			minecraft.displayGuiScreen(new GuiCutscene(new CutsceneSpec()));
		}
		// TEMP
		if(player.inventory.currentItem == 8) {
			minecraft.displayGuiScreen(new GuiCutscene(new CutsceneStart(player.posX, player.posY, player.posZ)));
			player.inventory.currentItem = 0;
		}
//		MinecraftImpl imp = (MinecraftImpl) mc;
//		Insets i = imp.mcFrame.getInsets();
//		imp.mcFrame.setSize(1280 + i.left + i.right, 720 + i.bottom + i .top);
		// TEMP END
		return true;
	}
	
	public boolean isPlayerReady(String player) {
		if(!playersReadyness.containsKey(player)) {
			return false;
		}
		return playersReadyness.get(player);
	}

	public static Block INFECTED;
	public static Block INFECTED_SKY;
	public static Block INFECTED_WOOD;
	public static Block INFECTED_WATER;
	public static Block INFECTED_LEAVES;
	public static Block SPAWN;
	public static Block GROUND_DIAMOND;
	
	private class AmbientDot {
		
		public AmbientDot(double x, double y, double z) {
			pos.x = (float)x;
			pos.y = (float)y;
			pos.z = (float)z;
		}
		
		private Vector3f pos = new Vector3f();
		private double light = 0;
	}
	
	public long getSessionStartTime() {
		return sessionStartTime;
	}
	
	public long getSessionEndTime() {
		return sessionEndTime;
	}
 	
}

