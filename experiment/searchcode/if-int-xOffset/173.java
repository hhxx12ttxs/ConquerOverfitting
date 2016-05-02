package de.ggj14.wap;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import com.esotericsoftware.minlog.Log;

import de.ggj14.wap.settings.ClientSettings;
import de.ggj14.wap.settings.SettingsPersistenceHelper;
import de.ggj14.wap.sound.SoundManager;
import de.ggj14.wap.states.Menu;
import de.ggj14.wap.states.Multiplayer;
import de.ggj14.wap.states.Observer;
import de.ggj14.wap.states.Singleplayer;

public class WapGame extends StateBasedGame {

	/** used to draw all things in the center of screen **/
	private static int xOffset = 0;
	private static int yOffset = 0;
	private final SettingsPersistenceHelper persistentSettings;

	public WapGame(SettingsPersistenceHelper persistentSettings, String title) throws SlickException {
		super(title);
		this.persistentSettings = persistentSettings;
		sm = new SoundManager();
	}

	/**
	 * @return the xoffset for drawing thing central
	 */
	public static int getXOffset() {
		return xOffset;
	}

	public static void setxOffset(int xOffset) {
		WapGame.xOffset = xOffset;
	}

	/**
	 * @return the y offset to draw thing central
	 */
	public static int getYOffset() {
		return yOffset;
	}

	public static void setyOffset(int yOffset) {
		WapGame.yOffset = yOffset;
	}

	@Override
	public void initStatesList(GameContainer wapGame) throws SlickException {
		addState(new Menu(this.persistentSettings, sm));
		addState(new Singleplayer(sm));
		addState(new Multiplayer(sm));
		addState(new Observer(sm));
		
		ClientSettings settings = this.persistentSettings.loadSettings();
		wapGame.setSoundOn(settings.isSoundOn());
		wapGame.setMusicOn(settings.isMusicOn());
		sm.init();
	}

	public static String DEFAULT_SERVER_ENDPOINT = "http://daraja.de:8090/prophet";
	public static final String GAME_NAME = "We Are Prophet";
	private final SoundManager sm;

	public static void main(String[] args) throws Exception {
		Log.DEBUG();
		if (args.length == 1) {
			DEFAULT_SERVER_ENDPOINT = args[0];
		}
		try (final SettingsPersistenceHelper persistentSettings = new SettingsPersistenceHelper()) {
			WapGame game = new WapGame(persistentSettings, GAME_NAME);
			AppGameContainer app = new AppGameContainer(game);
			WapGame.setxOffset((app.getScreenWidth() - 1080) / 2);
			WapGame.setyOffset((app.getScreenHeight() - 720) / 2);
			app.setDisplayMode(app.getScreenWidth(), app.getScreenHeight(), false);
			app.setMultiSample(4);
			app.setAlwaysRender(true);			
			app.setUpdateOnlyWhenVisible(false);
			app.setForceExit(false);
			app.setShowFPS(false);
			app.start();
		}
		// GAME IS OVER - GUI HAS BEEN CLOSED
		Log.debug("Spiel beendet");
	}
}

