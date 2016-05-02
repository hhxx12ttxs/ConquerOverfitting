package slimevoid.infection.core.cutscene;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScaled;
import static org.lwjgl.opengl.GL11.glTranslated;
import slimevoid.infection.core.InfectionMod;
import slimevoid.infection.core.cutscene.effect.Effect2D;
import slimevoid.infection.core.cutscene.effect.Effect2DColor;
import slimevoid.infection.core.cutscene.effect.Effect2DFade;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ChunkCoordinates;

public class CutsceneStart extends Cutscene {
	
	public CutsceneStart(double pX, double pY, double pZ) {
		this.sX = pX;
		this.sY = pY;
		this.sZ = pZ;
		
		this.eX = pX;
		this.eY = pY + 20;
		this.eZ = pZ + 10;
	}
	
	@Override
	protected void initScenes() {
		addScene(new Scene(7000L) {
			@Override
			protected void initEffects() {
				
				addEffect2D(new Effect2DColor	(7050L, 0x000000)			, 0L);
				addEffect2D(new Effect2D(6000L) {
					@Override
					public void render(float progress, float frameDelta) {
						glPushMatrix();
						glEnable(GL_BLEND);
						glTranslated(width / 2, height / 2, 0);
						double scale = .5 + progress * .75;
						double a1 = progress < .5 ? progress * 2 : 1 - (progress - .5) * 2;
						double a2 = progress < .75 ? (progress < .25 ? 0 : (progress - .25) * 2) : 1 - (progress - .75) * 4;
						glScaled(scale, scale, scale);
						glTranslated(-width / 2, -height / 2, 0);
						String str = "You are joining a new world";
						if(a1 > 6 / 255F) fontRenderer.drawStringWithShadow(str, width / 2 - fontRenderer.getStringWidth(str) / 2, height / 2 - 15, (((int)(a1 * 0xFF)) << 24) + 0xFFFFFF);
						str = "while something dark is raising";
						if(a2 > 6 / 255F) fontRenderer.drawStringWithShadow(str, width / 2 - fontRenderer.getStringWidth(str) / 2, height / 2, (((int)(a2 * 0xFF)) << 24) + 0xFFFFFF);
						glDisable(GL_BLEND);
						glPopMatrix();
					}
				}, 1000L);
			}
			
			@Override
			public void onTick(long time) {
			}
		});
		ChunkCoordinates cc = InfectionMod.infectionPos;
		addScene(new SceneMeteor(cc.posX, cc.posY, cc.posZ)) ;
		addScene(new Scene3D() {
			
			@Override
			protected void init() {
				super.init();
			}
			
			@Override 
			protected void initWaypoints() {
				addWaypoint(new Waypoint(eX, eY, eZ - 15, 0, 90), 0L);
				addWaypoint(new Waypoint(sX, sY + 5, sZ - 10, 0, 30), 8000L);
				addWaypoint(new Waypoint(sX, sY, sZ, 0, 0), 2000L);
				
			}

			@Override
			protected void initEffects() {
				addEffect2D(new Effect2DFade	(2000L, 0x000000)			, 0L);
			}
			
			
			@Override
			public void onTick(long time) {
				super.onTick(time);
			}
			
			@Override
			public void render3D(float frameDelta, long time) { 
				RenderManager.instance.renderEntityWithPosYaw(mc.thePlayer, sX, sY, sZ, 0, 0);
			}
		});
		
	}
	
	private double sX, sY, sZ;
	private double eX, eY, eZ;
}

