package slimevoid.infection.core.cutscene;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import java.util.List;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import cpw.mods.fml.server.FMLServerHandler;

public class CutsceneSpec extends Cutscene {

	@Override
	protected void initScenes() {
		addScene(new Scene3D() {
			
			@Override
			protected void initEffects() {
			}
			
			@Override
			protected void initWaypoints() {
			}
			
			@Override
			public long getDuration() {
				return Keyboard.isKeyDown(Keyboard.KEY_END) ? 0L : Long.MAX_VALUE;
			}
			
			@Override
			public void start() {
				super.start();
				mc.mouseHelper.grabMouseCursor();
				nextPlayer();
			}
			
			@Override
			public void render2D(float frameDelta, long time) {
				super.render2D(frameDelta, time);
				ScaledResolution reso = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
				int w = reso.getScaledWidth();
				@SuppressWarnings("unused")
				int h = reso.getScaledHeight();
				String str = followedPlayer != null ? followedPlayer.username : "- Nobody -";
				fontRenderer.drawStringWithShadow(str, w / 2 - fontRenderer.getStringWidth(str) / 2, 2, 0xFFFFFF);
			}
			
			@Override
			public void onTick(long time) {
				super.onTick(time);
				if(mc.theWorld == null) {
					mc.displayGuiScreen(null);
				}
				if(Mouse.isButtonDown(0) && !lastClick) {
					mouseClicked();
				}
				lastClick = Mouse.isButtonDown(0);
				if(followedPlayer == null) {
					return ;
				}
				mYaw *= .75;
				mPitch *= .75;
				if(Display.isActive() && hasFocus()) {
					mc.mouseHelper.mouseXYChange();
					int dX = mc.mouseHelper.deltaX;
					int dY = mc.mouseHelper.deltaY;
					mYaw += dX * .05;
					mPitch += dY * .05;
				}
				prevX = posX;
				prevY = posY;
				prevZ = posZ;
				prevYaw = rotYaw;
				prevPitch = rotPitch;
				rotYaw += mYaw;
				rotPitch += mPitch;
				if(rotPitch > 90) {
					rotPitch = 90;
					mPitch = 0;
				} else if(rotPitch < -90) {
					rotPitch = -90;
					mPitch = 0;
				}
				posX = followedPlayer.posX + sin(toRadians(360 - rotYaw)) * cos(toRadians(rotPitch)) * -2;
				posY = followedPlayer.posY + sin(toRadians(rotPitch)) * 2 + 1.5;
				posZ = followedPlayer.posZ + cos(toRadians(360 - rotYaw)) * cos(toRadians(rotPitch)) * -2;
			}
			
			private void mouseClicked() {
				if(mc.theWorld != null ) {
					nextPlayer();
				}
			}
			
			private void nextPlayer() {
				List<String> playerList = FMLServerHandler.instance().getServer().getConfigurationManager().playerEntityList;
				int i;
				if(followedPlayer == null) {
					i = 0;
				} else {
					i = playerList.indexOf(followedPlayer.username) + 1;
					i = i >= playerList.size() ? 0 : i;
				}
				followedPlayer = mc.theWorld.getPlayerEntityByName(playerList.get(i));
				if(followedPlayer == mc.thePlayer) {
					if(playerList.size() > 1) {
						nextPlayer();
					} else {
						followedPlayer = null;
					}
				}
				if(followedPlayer != null) {
					// TODO :: Moar and Moar and Moar Packet Shizzle
/*					Packet230ModLoader packet = new Packet230ModLoader();
					packet.dataInt = new int[]{5};
					packet.dataString = new String[]{followedPlayer.username};
					ModLoaderMp.sendPacket(mod_Infection.instance, packet);*/
				}
			}

			private double mYaw, mPitch;
		});
	}
	
	@Override
	public void focusLost() {
		super.focusLost();
		mc.mouseHelper.ungrabMouseCursor();
	}
	
	@Override
	public void focusGained() {
		super.focusGained();
		mc.mouseHelper.grabMouseCursor();
	}
	
	private boolean lastClick;
	private EntityPlayer followedPlayer ;
}

