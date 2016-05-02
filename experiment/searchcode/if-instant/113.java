
package com.chuanonly.train;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;
import loon.action.sprite.SpriteBatch;
import loon.action.sprite.painting.DrawableScreen;
import loon.core.LSystem;
import loon.core.RefObject;
import loon.core.graphics.opengl.GLEx;
import loon.core.input.LInputFactory.Touch;
import loon.core.input.LKey;
import loon.core.input.LTouch;
import loon.core.input.LTransition;
import loon.core.resource.Resources;
import loon.core.timer.GameTime;


public class GameMain extends DrawableScreen implements GameCore {

	public GameState activeState;

	private boolean buyDialogActive = false;
	private boolean changingState;
	private GameState[] gameStates = new GameState[10];
	private int gameTick;

	private int height;

	private boolean menuMusicQuieter;
	private float menuMusicVolume;
	private float menuMusicVolumeTarget;
	private boolean mouseAlreadyDown;
	private boolean mouseDown;
	private int mouseDownTick;
	private boolean mouseUp;
	private int mouseX;
	private int mouseY;

	private EStates nextState;

	private Settings settings;

	private int stateTick;

	private int[] values = new int[6];
	private int width;

	Painter painter;

	@Override
	public void loadContent() {
		painter = new Painter(getSpriteBatch());
		this.width = LSystem.screenRect.width;
		this.height = LSystem.screenRect.height;
		this.menuMusicQuieter = false;
		this.menuMusicVolumeTarget = 0f;
		this.menuMusicVolume = this.menuMusicVolumeTarget;
		for (int i = 0; i < 6; i++) {
			this.values[i] = -1;
		}
		this.mouseAlreadyDown = false;
		this.settings = new Settings();

		if (this.getSettings().m_sounds) {

		} else {

		}

		this.gameStates[0] = this.initState(EStates.EGameStateSplash);
		this.changeState(EStates.EGameStateSplash);

	}

	public final void changeState(EStates id) {
		this.stateTick = 0;
		this.mouseDownTick = -1;
		this.clearMouseStatus();
		if (this.activeState == null) {
			this.activeState = this.gameStates[(id.getValue())];
			this.activeState.activateState();
			this.changingState = false;
		} else {
			this.changingState = true;
			this.nextState = id;
		}
	}

	public final void clearMouseStatus() {
		this.mouseX = -1;
		this.mouseY = -1;
		this.mouseDown = false;
		this.mouseUp = false;
	}

	public final void doButtonPressSound() {
		MainActivity.playSound(MainActivity.SOUND_BUTTON);
	}

	public void draw(GLEx glex) {
		if(!isOnLoadComplete()){
			return;
		}
		if (this.buyDialogActive) {
			this.clearMouseStatus();
		}
		this.painter.begin();
		this.activeState.paint(this.painter);
		this.painter.end();
	}

	public final void exit() {
		this.stopMenuMusic(true);
		System.exit(0);
	}

	public final GameState getGameState(EStates id) {
		return this.gameStates[id.getValue()];
	}

	public final int getH() {
		return this.height;
	}

	public final String getLevelDir(int index) {
		return "";
	}

	public final int getMouseDownTick() {
		return this.mouseDownTick;
	}

	public final int getMouseX() {
		return this.mouseX;
	}

	public final int getMouseY() {
		return this.mouseY;
	}

	public final Settings getSettings() {
		return this.settings;
	}

	public final int getStateTick() {
		return this.stateTick;
	}

	public final int getTick() {
		return this.gameTick;
	}

	public final int getValue(EValues valueId) {
		return this.values[valueId.getValue()];
	}

	public final int getW() {
		return this.width;
	}

	private GameState initState(EStates id) {
		switch (id) {
		case EGameStateSplash:
			return new StateSplash(this);

		case EGameStateMainMenu:
			return new StateMainMenu(this);

		case EGameStateGame:
			return new StateGame(this);

		case EGameStateLevelFailed:
			return new StateDummy(this);

		case EGameStateLevelSuccess:
			return new StateDummy(this);

		case EGameStateLoadGame:
			return new StateDummy(this);

		case EGameStateLevelSelect:
			return new StateLevelSelect(this);

		case EGameStateMainLevelSelect:
			return new StateMainLevelSelect(this);

		case EGameStateGameEnd:
			return new StateGameEnd(this);

		case EGameStateTrial:
			return new StateTrial(this);
		default:
			break;
		}
		return null;
	}

	public final boolean isMouseDown() {
		return this.mouseDown;
	}

	public final boolean isMouseUp() {
		return this.mouseUp;
	}

	public final boolean isTrial() {
		return false;
	}

	public final void loadAllStates() {
		for (int i = 1; i < 10; i++) {
			this.gameStates[i] = this.initState(EStates.forValue(i));
		}
	}

	public final boolean LoadLevel(int level, RefObject<Integer> speed,
			RefObject<java.util.ArrayList<Tile>> tiles,
			RefObject<java.util.ArrayList<Tile>> caves,
			RefObject<java.util.ArrayList<ScheduleItem>> schedule) {
		try {
			InputStream stream = Resources.openResource("assets/levels/level_"
					+ level + ".lev");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					stream, LSystem.encoding));
			String record;
			for (; (record = reader.readLine()) != null;) {

				String tempVar = record.substring(0, 1);
				if (tempVar.equals("t")) {
					Tile tile = new Tile();
					tile.InitWithString(record.substring(2));
					tiles.argvalue.add(tile);
				} else if (tempVar.equals("c")) {
					Tile tile2 = new Tile();
					tile2.InitWithString(record.substring(2));
					caves.argvalue.add(tile2);
				}

				else if (tempVar.equals("s")) {
					speed.argvalue = Integer.parseInt(record.substring(2));

				}

				else if (tempVar.equals("x")) {
					String[] strArray = record.substring(2).split("[,]", -1);
					int aCaveId = Integer.parseInt(strArray[0]);
					int aTicks = Integer.parseInt(strArray[1]);
					ScheduleItem item = new ScheduleItem(aCaveId, aTicks);
					schedule.argvalue.add(item);
				}
			}
			stream.close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public void onPause() {
		if (this.activeState != null) {
			this.activeState.gameHidden();
		}
	}

	public void onResume() {

	}

	public final void setMenuMusicQuieter(boolean quiet) {
		this.menuMusicQuieter = quiet;
		this.menuMusicVolumeTarget = this.menuMusicQuieter ? 0.6f : 1f;
	}

	public final void setValue(EValues valueId, int value) {
		this.values[valueId.getValue()] = value;
	}

	public final boolean shouldDoMusic() {
		return false;
	}

	public final void startMenuMusic(boolean instant) {
		if (this.shouldDoMusic() && this.getSettings().m_sounds) {
			this.menuMusicVolumeTarget = this.menuMusicQuieter ? 0.6f : 1f;

			if (instant) {
				this.menuMusicVolume = this.menuMusicVolumeTarget;

			}
		}
	}

	public final void stopMenuMusic(boolean instant) {
		if (this.shouldDoMusic()) {
			this.menuMusicVolumeTarget = 0f;
			if (instant) {
				this.menuMusicVolume = this.menuMusicVolumeTarget;

			}
		}
	}

	public final void testLoad() {
	}

	public final void tickMenuMusic() {
		this.shouldDoMusic();
		if ((!this.shouldDoMusic() && (this.menuMusicVolume != 0f))) {
			this.menuMusicVolumeTarget = 0f;
			this.menuMusicVolume = 0f;

		} else if (this.menuMusicVolume != this.menuMusicVolumeTarget) {
			float num = ((this.menuMusicQuieter && (this.menuMusicVolumeTarget == 0.6f)) && (this.menuMusicVolume < 0.6f)) ? 0.01f
					: 0.08f;
			if (this.menuMusicVolume < this.menuMusicVolumeTarget) {
				this.menuMusicVolume += num;
				if (this.menuMusicVolume > this.menuMusicVolumeTarget) {
					this.menuMusicVolume = this.menuMusicVolumeTarget;
				}
			} else if (this.menuMusicVolume > this.menuMusicVolumeTarget) {
				this.menuMusicVolume -= num;
				if (this.menuMusicVolume < this.menuMusicVolumeTarget) {
					this.menuMusicVolume = this.menuMusicVolumeTarget;
				}
			}

		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unloadContent() {
		this.painter.dispose();

	}

	@Override
	public void pressed(LTouch e) {

	}

	@Override
	public void released(LTouch e) {

	}

	@Override
	public void move(LTouch e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drag(LTouch e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pressed(LKey e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void released(LKey e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(GameTime gameTime) {
		if(!isOnLoadComplete()){
			return;
		}
		if (this.changingState) {
			if (this.activeState != null) {
				this.activeState.deactivateState();
			}
			this.activeState = null;
			this.changeState(this.nextState);
		}
		if ((this.gameTick % 5) == 0) {
			this.tickMenuMusic();
		}

		this.gameTick++;

		if (Touch.isDown()|| Touch.isMove() || Touch.isDrag() ) {
			if (!this.mouseAlreadyDown) {
				this.mouseDown = true;
				this.mouseUp = false;
				this.mouseDownTick = this.gameTick;
				this.mouseAlreadyDown = true;
			}
			this.mouseX = Touch.x();
			this.mouseY = Touch.y();
		} else {
			this.mouseAlreadyDown = false;
			this.mouseX = Touch.x();
			this.mouseY = Touch.y();
			if (this.mouseDown) {
				this.mouseUp = true;
				this.mouseDown = false;
			}
		}
		if (!this.buyDialogActive) {
			this.activeState.tick();
		}

	}

	@Override
	public void showPurchaseDialog() {
		// TODO Auto-generated method stub

	}

	public LTransition onTransition() {
		return LTransition.newFadeIn();
	}


}

