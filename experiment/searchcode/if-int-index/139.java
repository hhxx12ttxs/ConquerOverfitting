package org.loon.framework.android.game.srpg;

import org.loon.framework.android.game.action.avg.command.Command;
import org.loon.framework.android.game.action.sprite.AnimationHelper;
import org.loon.framework.android.game.action.sprite.WaitAnimation;
import org.loon.framework.android.game.core.EmulatorButton;
import org.loon.framework.android.game.core.EmulatorButtons;
import org.loon.framework.android.game.core.EmulatorListener;
import org.loon.framework.android.game.core.LSystem;
import org.loon.framework.android.game.core.graphics.LFont;
import org.loon.framework.android.game.core.graphics.LImage;
import org.loon.framework.android.game.core.graphics.Screen;
import org.loon.framework.android.game.core.graphics.component.LMessage;
import org.loon.framework.android.game.core.graphics.component.LSelect;
import org.loon.framework.android.game.core.graphics.device.LGraphics;
import org.loon.framework.android.game.core.graphics.opengl.GLColor;
import org.loon.framework.android.game.core.graphics.opengl.GLEx;
import org.loon.framework.android.game.core.graphics.opengl.GLGradation;
import org.loon.framework.android.game.core.graphics.opengl.GLLoader;
import org.loon.framework.android.game.core.graphics.opengl.LTexture;
import org.loon.framework.android.game.core.input.LTouch;
import org.loon.framework.android.game.core.input.LInputFactory.Touch;
import org.loon.framework.android.game.core.timer.LTimer;
import org.loon.framework.android.game.srpg.ability.SRPGAbilityFactory;
import org.loon.framework.android.game.srpg.ability.SRPGAbilityOption;
import org.loon.framework.android.game.srpg.ability.SRPGDamageAverage;
import org.loon.framework.android.game.srpg.ability.SRPGDamageData;
import org.loon.framework.android.game.srpg.actor.SRPGActor;
import org.loon.framework.android.game.srpg.actor.SRPGActorFactory;
import org.loon.framework.android.game.srpg.actor.SRPGActors;
import org.loon.framework.android.game.srpg.actor.SRPGPosition;
import org.loon.framework.android.game.srpg.actor.SRPGStatus;
import org.loon.framework.android.game.srpg.effect.SRPGEffect;
import org.loon.framework.android.game.srpg.effect.SRPGEffectFactory;
import org.loon.framework.android.game.srpg.effect.SRPGNumberEffect;
import org.loon.framework.android.game.srpg.effect.SRPGPhaseEffect;
import org.loon.framework.android.game.srpg.effect.SRPGUpperEffect;
import org.loon.framework.android.game.srpg.field.SRPGField;
import org.loon.framework.android.game.srpg.field.SRPGFieldElement;
import org.loon.framework.android.game.srpg.field.SRPGFieldElements;
import org.loon.framework.android.game.srpg.field.SRPGFieldMove;
import org.loon.framework.android.game.srpg.field.SRPGTeams;
import org.loon.framework.android.game.srpg.view.SRPGAbilityNameView;
import org.loon.framework.android.game.srpg.view.SRPGActorStatusView;
import org.loon.framework.android.game.srpg.view.SRPGAvgView;
import org.loon.framework.android.game.srpg.view.SRPGChoiceView;
import org.loon.framework.android.game.srpg.view.SRPGDamageExpectView;
import org.loon.framework.android.game.srpg.view.SRPGDrawView;
import org.loon.framework.android.game.srpg.view.SRPGFieldChoiceView;
import org.loon.framework.android.game.srpg.view.SRPGMessageListener;
import org.loon.framework.android.game.srpg.view.SRPGMessageView;
import org.loon.framework.android.game.srpg.view.SRPGMiniStatusView;
import org.loon.framework.android.game.utils.RecordStoreUtils;
import org.loon.framework.android.game.utils.collection.ArrayMap;

/**
 * Copyright 2008 - 2011
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loonframework
 * @author chenpeng
 * @email?ceponline@yahoo.com.cn
 * @version 0.1.1
 */
public abstract class SRPGScreen extends Screen implements SRPGType, Runnable {

	public static int TILE_WIDTH, TILE_HEIGHT;

	private LTexture cursor, messageImage;

	private boolean isCursor;

	private boolean isBattleMode;

	private Thread srpgThread;

	private SRPGAI srpgAI;

	private SRPGAvgView srpgAvgView;

	private final static GLColor[] colors = { new GLColor(255, 0, 32, 125),
			new GLColor(0, 192, 0, 125), new GLColor(192, 192, 0, 125) };

	private GLColor moving_our = new GLColor(0, 128, 255, 125),
			moving_other = new GLColor(255, 100, 20, 125),
			attack_target = new GLColor(255, 192, 0, 125),
			attack_range = new GLColor(255, 128, 64, 125),
			moving_change = new GLColor(0, 64, 255, 125),
			hero_flag = GLColor.blue, enemy_flag = GLColor.red;

	private SRPGMessageView srpgHelper;

	private int cam_x, cam_y;

	private int move;

	private int currentActor;

	private int mouse_x, mouse_y;

	private int halfTileWidth;

	private int halfTileHeight;

	private int halfWidth;

	private int halfHeight;

	private SRPGChoiceView srpgChoiceView;

	private SRPGFieldChoiceView srpgChoiceField;

	private SRPGDrawView srpgDrawView;

	private SRPGActors srpgActors;

	private SRPGField srpgField;

	private SRPGFieldElements srpgElements;

	private SRPGFieldMove srpgMove;

	private SRPGEvent srpgEvent, temp_event_1, temp_event_2;

	private SRPGPosition srpgPosition, tempPosition;

	private SRPGEffect srpgEffect;

	private SRPGTeams srpgTeams;

	private int choiceX, choiceY;

	private int tileWidth, tileHeight, procFlag;

	private boolean isGridAlpha, isSrpgTouchLock, isSrpgNoMove;

	private String fileName;

	private LTimer defTimer;

	private WaitAnimation waitDraw;

	private LFont choiceFont = LFont.getFont("Monospaced", 0, 22);

	private LFont simpleFont = LFont.getFont("Dialog", 0, 12);

	// ?????
	private final String[][] menuItems = new String[4][2];

	// ???????????????????????????????????
	private boolean isEventLoop, isCameraLock, isBattleStart, isAnimationEvent;

	// ????????,???????????,?????????,????????,????????????????????,????????
	private static boolean isGrid, isTeamColor, isEndView = true,
			isEnemyView = true, isPhase = true, isBattle = true, isSound;

	// ??????
	private static int moveSpeed = 20;

	private int sleepTime = 300;

	// ???SRPG????????????
	private int maxLevel, maxExp;

	private int language_index;

	public SRPGScreen(String fileName) {
		this(null, fileName, null, 32, 32);
	}

	public SRPGScreen(String fileName, LTexture img) {
		this(null, fileName, img, 32, 32);
	}

	public SRPGScreen(String fileName, int row, int col) {
		this(null, fileName, null, row, col);
	}

	public SRPGScreen(String fileName, LTexture img, int row, int col) {
		this(null, fileName, img, row, col);
	}

	public SRPGScreen(SRPGFieldElements elements, String fileName,
			LTexture img, int row, int col) {
		LTexture.AUTO_LINEAR();
		this.srpgElements = elements;
		this.tileWidth = row;
		this.tileHeight = col;
		this.halfTileWidth = tileWidth / 2;
		this.halfTileHeight = tileHeight / 2;
		this.halfWidth = getWidth() / 2;
		this.halfHeight = getHeight() / 2;
		this.fileName = fileName;
		this.messageImage = img;
		this.move = -1;
		this.currentActor = -1;
		this.mouse_x = -1;
		this.mouse_y = -1;
		this.choiceX = 50;
		this.choiceY = 50;
		this.procFlag = PROC_NORMAL;
		SRPGScreen.TILE_WIDTH = tileWidth;
		SRPGScreen.TILE_HEIGHT = tileHeight;
	}

	/**
	 * Screen????????????(?????????)
	 * 
	 * @param g
	 */
	protected void initLoading(GLEx g) {
		g.drawClear();
		if (LSystem.isLogo) {
			return;
		}
		if (waitDraw == null) {
			waitDraw = new WaitAnimation(1, getWidth(), getHeight());
			waitDraw.setRunning(true);
		}
		if (defTimer == null) {
			defTimer = new LTimer(120);
		}
		if (defTimer.action(elapsedTime)) {
			waitDraw.next();
		}
		waitDraw.draw(g, 0, 0);
	}

	/**
	 * ?????????,?????????
	 * 
	 */
	protected final void initActorsData() {
		SRPGData.getInstnace().initActors(srpgActors);
	}

	/**
	 * ??????????????
	 * 
	 * @param name
	 * @throws Exception
	 */
	protected final void savePositionData(String name) throws Exception {
		RecordStoreUtils.setBytes(name, SRPGData.getInstnace().savePosition());
	}

	/**
	 * ?????????????
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	protected SRPGPosition[] loadPositionData(String name) throws Exception {
		byte[] result = RecordStoreUtils.getBytes(name);
		if (result != null) {
			return SRPGData.getInstnace().loadPosition(result);
		}
		return null;
	}

	/**
	 * ??????????????
	 * 
	 * @param name
	 * @throws Exception
	 */
	protected final void saveStatusData(String name) throws Exception {
		RecordStoreUtils.setBytes(name, SRPGData.getInstnace().saveStatus());
	}

	/**
	 * ?????????????(??SRPGData??)
	 * 
	 * @param name
	 * @throws Exception
	 */
	protected boolean loadStatusData(String name) throws Exception {
		byte[] result = RecordStoreUtils.getBytes(name);
		if (result != null) {
			return SRPGData.getInstnace().loadStatus(result);
		}
		return false;
	}

	/**
	 * ??????
	 * 
	 * @param index
	 * @return
	 */
	public SRPGActor findActor(int index) {
		if (srpgActors != null) {
			return srpgActors.find(index);
		} else {
			return null;
		}
	}

	/**
	 * ?????????
	 * 
	 * @param index
	 * @return
	 */
	public SRPGStatus findActorStatus(int index) {
		return srpgActors.find(index).getActorStatus();
	}

	/**
	 * ????SRPG??
	 * 
	 * @param status
	 * @param mv
	 * @param atk
	 * @return
	 */
	protected SRPGActor makeActor(SRPGStatus status, AnimationHelper mv,
			AnimationHelper atk) {
		return new SRPGActor(status, mv, atk, tileWidth, tileHeight);
	}

	/**
	 * ????SRPG??
	 * 
	 * @param status
	 * @param mv
	 * @return
	 */
	protected SRPGActor makeActor(SRPGStatus status, AnimationHelper mv) {
		return new SRPGActor(status, mv, tileWidth, tileHeight);
	}

	/**
	 * ???????
	 * 
	 */
	protected synchronized void mainProcess() {
		if (isClose()) {
			return;
		}
		// ????????
		isBattleStart = true;
		// ???????0
		srpgTeams.setPhase(0);
		boolean battleStop = false;
		for (; isEventLoop && !isClose();) {
			if (!isEventLoop) {
				break;
			}
			// ?????????
			if (srpgEvent.queueExist() && battleStop) {
				callSRPGBattleProcEvent(srpgEvent);
			}
			if (!isEventLoop) {
				break;
			}
			// ??????????????
			processInitialize();
			if (!srpgTeams.checkMoving(srpgActors) || !battleStop) {
				if (battleStop) {
					srpgTeams.changePhase(srpgActors);
				} else {
					battleStop = true;
				}
				this.isAnimationEvent = true;
				this.processChangePhaseBefore();
				this.isAnimationEvent = false;
				srpgTeams.startTurn(srpgActors);
				afterCheck();
				// ????
				winnerCheck();
				if (!isEventLoop) {
					break;
				}
				if (!srpgTeams.checkPhase(srpgActors)) {
					continue;
				}
				int roleIndex = -1;
				for (int i = 0; i < srpgActors.size(); i++) {
					SRPGActor actor = srpgActors.find(i);
					if (!actor.isVisible()
							|| srpgTeams.getTeamPhase() != actor
									.getActorStatus().team) {
						continue;
					}
					if (actor.getActorStatus().leader == LEADER_NORMAL
							|| actor.getActorStatus().leader == LEADER_MAIN) {
						roleIndex = i;
						break;
					}
					if (roleIndex == -1) {
						roleIndex = i;
					}
				}
				// ???????????
				if (roleIndex != -1) {
					// ???????????
					centerCamera(roleIndex);
				}
				// ????????
				if (isPhase) {
					// ????
					setLock(true);
					// ??????
					setEffect(makePhaseEffect(srpgTeams.getName()));
					setLock(false);
				}
				// ?????
				processChangePhaseAfter();
				// ?????
				setCameraLock(true);
				if (srpgActors == null) {
					return;
				}
				for (int i = 0; i < srpgActors.size(); i++) {
					SRPGActor actor = srpgActors.find(i);
					if (!actor.isVisible()) {
						continue;
					}
					SRPGStatus status = actor.getActorStatus();

					if (isEndView()) {
						actor.setActionEnd(false);
					}

					boolean isMoving = status.team == srpgTeams.getPhase()
							&& status.moveCheck() && status.action > 0;

					// ????????????
					if (isMoving && status.status[SRPGStatus.STATUS_LOVER] != 0) {
						int group = status.group;
						int[] computer = status.computer;
						status.group = status.substatus[SRPGStatus.STATUS_LOVER];
						status.computer = SRPGType.WIZARD_NORMAL;
						moveCPU(i, false);
						status.group = group;
						status.computer = computer;
						winnerCheck();

						// ??????CPU???
					} else if (isMoving && status.isComputer) {
						int[] computer = status.computer;

						if (computer == null) {
							status.computer = SRPGType.NORMAL_PRIEST_WIZARD;
						}
						moveCPU(i, false);

						status.computer = computer;
						winnerCheck();
					}
					if (status.action == 0) {
						status.action = 1;
					}
				}
				setCameraLock(false);

			}
			if (srpgTeams.getTeamPhase() != 0) {
				setCameraLock(true);
				for (; isEventLoop;) {
					// ????
					if (!srpgTeams.checkMoving(srpgActors)) {
						break;
					}
					for (int i = 0; i < srpgActors.size(); i++) {
						SRPGActor actor = srpgActors.find(i);
						SRPGStatus status1 = actor.getActorStatus();
						if (!actor.isVisible() || status1.action <= 0
								|| !status1.actionCheck()
								|| status1.team != srpgTeams.getTeamPhase()) {
							continue;
						}
						moveCPU(i);
						waitTime(sleepTime);
						if (!isEventLoop) {
							break;
						}
					}

				}
				setCameraLock(false);
				if (!isEventLoop) {
					break;
				}
				srpgTeams.endTurn(srpgActors);
			}
			try {
				super.wait();
			} catch (Exception ex) {
			}
		}
		// ??????
		isBattleStart = false;
	}

	/**
	 * ???????
	 * 
	 * @param elements
	 */
	protected abstract void initFieldElementConfig(SRPGFieldElements elements);

	/**
	 * ???????
	 * 
	 * @param field
	 */
	protected abstract void initMapConfig(SRPGField field);

	/**
	 * ???????
	 * 
	 * @param actors
	 */
	protected abstract void initActorConfig(SRPGActors actors);

	/**
	 * ???????
	 * 
	 * @param team
	 */
	protected abstract void initTeamConfig(SRPGTeams team);

	/**
	 * ????????????????(????????????)
	 * 
	 * @return
	 */
	protected boolean gameWinner() {
		setLock(true);
		setHelper(WINNER_LOSER[language_index][0]);
		isEventLoop = false;
		setLock(false);
		return false;
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @return
	 */
	protected boolean gameLoser() {
		setLock(true);
		setHelper(WINNER_LOSER[language_index][1]);
		isEventLoop = false;
		setLock(false);
		return false;
	}

	/**
	 * ????(????,????)
	 * 
	 */
	protected synchronized void changeItem() {
		setHelper(NO_SUPPORT[language_index]);
		defaultCommand();
	}

	/**
	 * ????????
	 * 
	 * @param g
	 */
	public void background(GLEx g) {

	}

	/**
	 * ????????
	 * 
	 * @param g
	 */
	public void foreground(GLEx g) {
	}

	/**
	 * ??????????????????true?????
	 * 
	 * @return
	 */
	protected abstract boolean startProcess();

	/**
	 * ???????????????????????true?????
	 * 
	 * @return
	 */
	protected abstract boolean endProcess();

	/**
	 * ???????????????
	 * 
	 */
	protected void processInitialize() {

	}

	/**
	 * ????????????????
	 * 
	 */
	protected abstract void processChangePhaseBefore();

	/**
	 * ????????????????
	 * 
	 */
	protected abstract void processChangePhaseAfter();

	/**
	 * ???????????????????ID
	 * 
	 * @param i
	 */
	protected abstract void processDeadActorBefore(int index, SRPGActor actor);

	/**
	 * ????????????????
	 * 
	 * @param actor
	 */
	protected abstract void processDeadActorAfter(int index, SRPGActor actor);

	/**
	 * ????????,??????
	 * 
	 * @param actor
	 */
	protected abstract void processAttackBefore(int index, SRPGActor actor);

	/**
	 * ????????,??????
	 * 
	 * @param actor
	 */
	protected abstract void processAttackAfter(int index, SRPGActor actor);

	/**
	 * ???????????????
	 * 
	 * @param actor
	 * @param level
	 */
	public abstract void processLevelUpBefore(int index, SRPGActor actor);

	/**
	 * ???????????????
	 * 
	 * @param actor
	 * @param level
	 */
	public abstract void processLevelUpAfter(int index, SRPGActor actor);

	/**
	 * ???????????????,?????????????????
	 * (???????setBattle(false),????????????????)
	 * 
	 * @param damagedata
	 * @param atk
	 * @param def
	 */
	protected abstract void processDamageInputAfter(SRPGDamageData damagedata,
			int atk, int def);

	/**
	 * ?????????????,?????????????????
	 * 
	 * @param damagedata
	 * @param atk
	 * @param def
	 */
	protected abstract void processDamageInputBefore(SRPGDamageData damagedata,
			int atk, int def);

	/**
	 * ????????????
	 * 
	 * @param i
	 */
	protected void processDeadTeam(int i) {
	}

	/**
	 * ????????ID(???????ID)
	 * 
	 * @param actorIndex
	 */
	protected void processTurnEndActor(int actorIndex) {
	}

	/**
	 * ????(?????,?????)
	 * 
	 * @param i
	 * @return
	 */
	protected boolean processDeadActor(int i) {
		return true;
	}

	/**
	 * ??????????
	 * 
	 * @param actor
	 * @param x
	 * @param y
	 */
	public abstract void onClickActor(final SRPGActor actor, int x, int y);

	/**
	 * ?????????????
	 * 
	 * @param element
	 * @param x
	 * @param y
	 */
	public abstract void onClickField(final SRPGFieldElement element, int x,
			int y);

	/**
	 * ????????(???????????)
	 * 
	 * @param teamName
	 * @return
	 */
	protected SRPGEffect makePhaseEffect(String teamName) {
		return new SRPGPhaseEffect(teamName);
	}

	/**
	 * ???????
	 * 
	 * @param actor
	 * @return
	 */
	protected SRPGEffect makeUpperDeltaEffect(int index, SRPGActor actor) {
		return new SRPGUpperEffect(actor.drawX() + halfTileWidth, actor.drawY()
				+ tileHeight, GLColor.black);
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param factory
	 * @param status
	 * @return
	 */
	protected SRPGDrawView makeAbilityNameView(SRPGAbilityFactory factory,
			SRPGStatus status) {
		return new SRPGAbilityNameView(factory, status);
	}

	/**
	 * ???????????????????????????
	 * 
	 * @param ab
	 * @param srpgField
	 * @param atk
	 * @param def
	 * @return
	 */
	protected SRPGDrawView makeDamageExpectView(final SRPGAbilityFactory ab,
			final SRPGField srpgField, int atk, int def) {
		SRPGDamageExpectView view = new SRPGDamageExpectView(ab, srpgField,
				srpgActors, atk, def);
		view.setLocation((getWidth() - view.getWidth()) / 2, getHeight()
				- view.getHeight() - 10);
		return view;
	}

	/**
	 * ?????????????????????????????
	 * 
	 * @param status
	 * @return
	 */
	protected SRPGDrawView makeActorStatusView(SRPGStatus status) {
		SRPGDrawView view = new SRPGActorStatusView(status);
		view.setLocation(5, 5);
		return view;
	}

	/**
	 * ?????????????????????????????
	 * 
	 * @param status
	 * @return
	 */
	protected SRPGDrawView makeActorMiniStatusView(SRPGStatus status,
			SRPGActor actor) {
		SRPGDrawView view = new SRPGMiniStatusView(status);
		view.setLocation(10, getHeight() - 10 - view.getHeight());
		if (mouse_x >= view.getLeft()
				&& mouse_x <= view.getLeft() + view.getWidth()
				&& mouse_y >= view.getTop()
				&& mouse_y <= view.getTop() + view.getHeight()) {
			view.setLocation(10, 10);
		}
		return view;
	}

	/**
	 * ??????
	 * 
	 * @param ability
	 * @param actor
	 * @param x
	 * @param y
	 * @return
	 */
	protected SRPGEffect makeAbilityEffect(SRPGAbilityFactory ability,
			SRPGActor actor, int x, int y) {
		return ability.getAbilityEffect(actor, x, y);
	}

	/**
	 * ???????????
	 * 
	 * @param w
	 * @param h
	 */
	protected void makeCursor(int w, int h) {
		this.cursor = AnimationHelper.makeCursor(w, h);
		this.isCursor = true;
	}

	/**
	 * ????????????
	 * 
	 * @param f
	 */
	protected void makeEmulatorButton(String f) {
		makeEmulatorButton(f, -1, -1);
	}

	/**
	 * ????????????
	 * 
	 * @param f
	 * @param x
	 * @param y
	 */
	protected void makeEmulatorButton(String f, int x, int y) {
		makeEmulatorButtons(new LTexture(f), null, x, y);
	}

	/**
	 * ????????????
	 * 
	 */
	protected void makeEmulatorButton() {
		makeEmulatorButton(-1, -1);
	}

	/**
	 * ????????????
	 * 
	 * @param x
	 * @param y
	 */
	protected void makeEmulatorButton(int x, int y) {
		makeEmulatorButtons(null, null, x, y);
	}

	/**
	 * ????????????
	 * 
	 * @param on
	 * @param un
	 * @param x
	 * @param y
	 */
	protected void makeEmulatorButtons(LTexture on, LTexture un, int x, int y) {

		EmulatorListener listener = new EmulatorListener() {

			public void onUpClick() {
			}

			public void onDownClick() {
			}

			public void onLeftClick() {
			}

			public void onRightClick() {
			}

			public void onCircleClick() {
			}

			public void onCancelClick() {
				onCancel(-1, -1);
				setTouchLock(true);
			}

			public void onSquareClick() {
			}

			public void onTriangleClick() {
			}

			public void unCircleClick() {
			}

			public void unCancelClick() {
				setTouchLock(false);
			}

			public void unDownClick() {
			}

			public void unLeftClick() {
			}

			public void unRightClick() {
			}

			public void unSquareClick() {
			}

			public void unTriangleClick() {
			}

			public void unUpClick() {
			}

		};

		setEmulatorListener(listener);

		EmulatorButtons buttons = getEmulatorButtons();

		if (buttons != null) {

			buttons.hideLeft();
			EmulatorButton square = buttons.getSquare();
			EmulatorButton triangle = buttons.getTriangle();
			EmulatorButton circle = buttons.getCircle();
			EmulatorButton cancel = buttons.getCancel();
			if (on != null && un != null) {
				cancel.setClickImage(on, un);
			} else if (on != null) {
				cancel.setClickImage(on);
			}
			if (x != -1 || y != -1) {
				cancel.setLocation(x, y);
			} else {
				cancel.setLocation(getWidth() - circle.getWidth() - 40,
						getHeight() - cancel.getHeight() - 30);
			}
			circle.disable(true);
			square.disable(true);
			triangle.disable(true);

		}
	}

	/**
	 * ?????
	 * 
	 * @param message
	 * @param view
	 */
	private void createChoice(String[] message, SRPGChoiceView view) {
		if (view == null) {
			view = new SRPGChoiceView(message, choiceFont, halfWidth - 50,
					halfHeight - 50);
		} else {
			view.set(message, choiceFont, halfWidth - 50, halfHeight - 50);
		}
	}

	/**
	 * ????????????
	 * 
	 * @return
	 */
	private LTexture createTempImage() {
		if (messageImage == null) {
			LImage tmp = LImage.createImage(getWidth() - 40,
					getHeight() / 2 - 20, true);
			LGraphics g = tmp.getLGraphics();
			g.setColor(0, 0, 0, 125);
			g.fillRect(0, 0, tmp.getWidth(), tmp.getHeight());
			g.dispose();
			messageImage = new LTexture(GLLoader.getTextureData(tmp));
			if (tmp != null) {
				tmp.dispose();
				tmp = null;
			}
		}
		return messageImage;
	}

	/**
	 * ??????
	 * 
	 * @param actors
	 * @return
	 */
	public final SRPGTeams createTeams(SRPGActors actors) {
		if (srpgTeams == null) {
			this.srpgTeams = new SRPGTeams(actors);
		} else {
			this.srpgTeams.set(actors);
		}
		return srpgTeams;
	}

	private final void resetWindow() {
		if (srpgDrawView == null) {
			srpgDrawView = new SRPGDrawView();
			return;
		}
		srpgDrawView.reset();
	}

	// ---- ????????? ----//

	/**
	 * ?????????????
	 */
	public void centerCamera() {
		centerCamera(currentActor);
	}

	/**
	 * ?????????????
	 * 
	 * @param index
	 */
	public void centerCamera(int index) {
		SRPGActor actor = srpgActors.find(index);
		centerCamera((actor.drawX() + halfTileWidth) - halfWidth, (actor
				.drawY() + halfTileHeight)
				- halfHeight);
	}

	/**
	 * ?????????SRPG????
	 * 
	 */
	public void centerCameraSetting() {
		if (currentActor != -1) {
			centerCamera();
		}
	}

	/**
	 * ?????????X,Y???
	 * 
	 * @param x
	 * @param y
	 */
	public void centerCamera(int x, int y) {
		this.cam_x = x;
		this.cam_y = y;
	}

	/**
	 * ?????????????????X,Y??
	 * 
	 * @param x
	 * @param y
	 * @param sleep
	 */
	public void moveCameraCenter(int x, int y, int sleep) {
		moveCamera(x - halfWidth, y - halfHeight, sleep);
	}

	/**
	 * ????????????X,Y??
	 * 
	 * @param x
	 * @param y
	 * @param sleep
	 */
	public void moveCamera(int x, int y, int sleep) {
		boolean flag = isLock();
		setLock(true);
		for (float i = 0; i < sleep; i++) {
			float nx = (cam_x * (sleep - i)) / sleep + (x * i) / sleep;
			float ny = (cam_y * (sleep - i)) / sleep + (y * i) / sleep;
			if (nx >= 0.0F) {
				nx += 0.5F;
			} else {
				nx -= 0.5F;
			}
			if (ny >= 0.0F) {
				ny += 0.5F;
			} else {
				ny -= 0.5F;
			}
			centerCamera((int) nx, (int) ny);
			try {
				super.wait();
			} catch (Exception ex) {
			}
		}
		centerCamera((int) x, (int) y);
		setLock(flag);
	}

	/**
	 * ??????????????
	 * 
	 * @param index
	 * @param sleep
	 */
	public void moveCamera(int index, int sleep) {
		SRPGActor actor = srpgActors.find(index);
		moveCamera((actor.drawX() + halfTileWidth) - halfWidth,
				(actor.drawY() + halfTileHeight) - halfHeight, sleep);
	}

	public void setCameraX(int i) {
		cam_x = i;
	}

	public void setCameraY(int i) {
		cam_y = i;
	}

	public int getCameraX() {
		return cam_x;
	}

	public int getCameraY() {
		return cam_y;
	}

	public void setCenterActor(int i) {
		currentActor = i;
	}

	public int getCenterActor() {
		return currentActor;
	}

	// ---- ????????? ----//

	// ---- ???????? ----//

	private void returningDamageValue(int[] ability, int atk, int def) {
		SRPGActor attacker = srpgActors.find(atk);
		SRPGActor defender = srpgActors.find(def);
		// ????????????
		if (!attacker.getActorStatus().moveCheck()) {
			return;
		}
		if (srpgField.getPosMapElement(attacker.getPosX(), attacker.getPosY()).state == 4) {
			return;
		}
		if (defender.getActorStatus().hp <= 0
				&& !defender.getActorStatus().checkSkill(
						SRPGStatus.SKILL_UNDEAD)) {
			return;
		}
		int[] res = SRPGAbilityFactory.filtedRange(SRPGAbilityFactory
				.filtedAbility(ability, attacker.getActorStatus(), false),
				srpgField, attacker.getPosX(), attacker.getPosY(), defender
						.getPosX(), defender.getPosY());
		if (res == null) {
			return;
		}
		SRPGPosition oldPosition = srpgPosition;
		if (tempPosition == null) {
			tempPosition = new SRPGPosition();
		} else {
			tempPosition.reset();
		}
		srpgPosition = tempPosition;
		srpgPosition.counter = true;
		srpgPosition.setTarget(defender.getPosX(), defender.getPosY());

		int optimizeAbility = 0;
		srpgPosition.number = atk;
		srpgPosition.enemy = def;
		procFlag = PROC_COUNTER;
		srpgPosition.ability = res[0];
		setTargetRange(res[0], srpgPosition.target[0], srpgPosition.target[1]);
		if (attacker.getActorStatus().team == 0) {
			setChoiceAbility(res);
			srpgPosition.ability = srpgChoiceView.getJointContent();
			srpgDrawView = makeDamageExpectView(SRPGAbilityFactory
					.getInstance(srpgChoiceView.getJointContent()), srpgField,
					srpgPosition.number, srpgPosition.enemy);

			optimizeAbility = srpgChoiceView.choiceWait(this);
		} else {
			optimizeAbility = SRPGAbilityFactory.getOptimizeAbility(res,
					srpgField, srpgActors, atk, def);
			if (isEnemyView()) {
				boolean flag = getCameraLock();
				setCameraLock(true);
				srpgPosition.ability = optimizeAbility;
				setTargetRange(optimizeAbility, srpgPosition.target[0],
						srpgPosition.target[1]);
				srpgDrawView = makeDamageExpectView(SRPGAbilityFactory
						.getInstance(optimizeAbility), srpgField,
						srpgPosition.number, srpgPosition.enemy);
				waitTime(sleepTime * 2);
				setCameraLock(flag);
			}
		}
		procFlag = PROC_ATTACK;
		resetWindow();
		srpgPosition.ability = optimizeAbility;
		setDamageValueImplement(optimizeAbility, srpgPosition.number,
				SRPGAbilityOption.getInstance(false), false);
		srpgPosition = oldPosition;
	}

	protected synchronized boolean setDamageValue(int number, int index) {
		return setDamageValue(number, index, true);
	}

	protected synchronized boolean setDamageValue(int number, int index,
			boolean flag) {
		return setDamageValue(number, index, SRPGAbilityOption
				.getInstance(flag), true);
	}

	protected synchronized boolean setDamageValue(int number, int index,
			boolean flag, boolean flag1) {
		return beforeDamageValue(number, index, SRPGAbilityOption
				.getInstance(flag), flag1);
	}

	protected synchronized boolean setDamageValue(int number, int index,
			SRPGAbilityOption abilityoption, boolean flag) {
		return beforeDamageValue(number, index, abilityoption, flag);
	}

	protected synchronized boolean beforeDamageValue(int number, int index,
			SRPGAbilityOption abilityoption, boolean flag) {
		SRPGAbilityFactory ability = SRPGAbilityFactory.getInstance(number);
		if (ability.checkAbilitySkill(SRPGStatus.SKILL_CARRY)) {
			int ai[] = srpgPosition.target;
			int l = srpgActors.checkActor(ai[0], ai[1]);
			if (!getTargetTrue(ability, index, l)) {
				setHelper(STRING_CARRY[language_index][0]);
				return false;
			}
			setHelper(STRING_CARRY[language_index][1]);
			SRPGActor actor = srpgActors.find(l);
			int nx = -1;
			int ny = -1;
			do {
				for (;;) {
					if (srpgChoiceField == null) {
						srpgChoiceField = new SRPGFieldChoiceView(srpgField);
					} else {
						srpgChoiceField.set(srpgField);
					}
					int[] res = srpgChoiceField.choiceWait(this, true);
					if (res == null) {
						return false;
					}
					nx = res[0];
					ny = res[1];
					if (srpgActors.checkActor(nx, ny) == -1
							&& srpgField.getMoveCost(
									actor.getActorStatus().movetype, nx, ny) != -1) {
						break;
					}
					setHelper(STRING_CARRY[language_index][2]);
				}
				String[] mes = { STRING_CARRY[language_index][3],
						STRING_CARRY[language_index][4] };
				createChoice(mes, srpgChoiceView);
			} while (srpgChoiceView.choiceWait(this, true) != 0);
			abilityoption.warp = true;
			abilityoption.setWarpPos(nx, ny);
		}
		setDamageValueImplement(number, index, abilityoption, flag);
		return true;
	}

	protected synchronized void setDamageValueImplement(int abilityIndex,
			int roleIndex, SRPGAbilityOption abilityoption, boolean flag) {
		SRPGAbilityFactory ability = SRPGAbilityFactory
				.getInstance(abilityIndex);
		SRPGActor actorObject = srpgActors.find(roleIndex);
		setLock(true);
		processAttackBefore(roleIndex, actorObject);
		if (abilityoption.extinctmp) {
			actorObject.getActorStatus().mp -= ability.getMP(actorObject
					.getActorStatus());
		}
		SRPGDamageAverage damageaverage = new SRPGDamageAverage();
		SRPGDrawView temp_view = srpgDrawView;
		srpgDrawView = makeAbilityNameView(ability, actorObject
				.getActorStatus());
		int[] res = srpgPosition.target;
		int posX = res[0];
		int posY = res[1];
		actorObject.setDirection(actorObject.findDirection(posX, posY));
		if (posX * tileWidth - cam_x < tileWidth
				|| posY * tileHeight - cam_y < tileHeight
				|| (posX * tileWidth - cam_x) + tileWidth > getWidth()
						- tileWidth
				|| (posX * tileHeight - cam_y) + tileHeight > getHeight()
						- tileHeight) {
			moveCameraCenter(posX * tileWidth + halfTileWidth, posY
					* tileHeight + halfTileWidth, 10);
		}
		actorObject.setAttack(true);

		// ????
		setEffect(makeAbilityEffect(ability, actorObject, posX, posY));

		setLock(false);

		int[] actors = new int[srpgActors.size()];
		for (int i = 0; i < actors.length; i++) {
			actors[i] = -1;
		}

		int count = 0;
		int actorExp = 0;

		setTargetRange(abilityIndex, posX, posY);
		int[][] area = srpgPosition.area;
		for (int y = 0; y < area.length; y++) {
			for (int x = 0; x < area[y].length; x++) {
				if (area[y][x] == -1) {
					continue;
				}
				int index = srpgActors.checkActor(x, y);

				if (index == -1
						|| !getTargetTrue(ability, roleIndex, index)
						&& (abilityoption.counter || index != srpgPosition.enemy)) {
					continue;
				}
				boolean lock = isLock();
				setLock(true);
				SRPGActor actor = srpgActors.find(index);
				int chp = actor.getActorStatus().hp;
				srpgDrawView = makeActorMiniStatusView(actor.getActorStatus(),
						actor);
				waitTime(sleepTime);

				SRPGDamageData damageData = null;
				if (isBattle) {
					damageData = ability.getDamageExpect(srpgField, srpgActors,
							roleIndex, index);
					if (damageData.isHit()) {
						if (abilityoption.warp) {
							actor.setVisible(false);
							actor.setPos(abilityoption.warp_pos);
							if (actor.drawX() - cam_x < tileWidth
									|| actor.drawY() - cam_y < tileHeight
									|| (actor.drawX() - cam_x) + tileWidth > getWidth()
											- tileWidth
									|| (actor.drawY() - cam_y) + tileHeight > getHeight()
											- tileHeight) {
								moveCamera((actor.drawX() + halfTileWidth)
										- halfWidth,
										(actor.drawY() + halfTileHeight)
												- halfHeight, 10);
							}
							waitTime(sleepTime);
							actor.setVisible(true);

							setEffect(SRPGEffectFactory.getAbilityEffect(
									SRPGEffectFactory.EFFECT_OUT, actor, actor
											.getPosX(), actor.getPosY()));

							waitTime(sleepTime);
						}
					}
					if (damageData.isHit() && damageData.getMoveStack() != null) {
						damageData.getMoveStack().moveActor(actor, this);
					}
					if (damageData.isHit() && damageData.getPosX() != -1
							&& damageData.getPosY() != -1) {
						actor
								.setPos(damageData.getPosX(), damageData
										.getPosY());
					}
					if (actor.getActorStatus().moveCheckStatus()
							&& damageData.getActorStatus().moveCheckStatus()) {
						actor.setDirection(actor.findDirection(actorObject
								.getPosX(), actorObject.getPosY()));
					}
					if (damageData.isHit() && damageData.getDirection() != -1) {
						actor.setDirection(damageData.getDirection());
					}
					if (damageData.isHit()) {
						damageaverage.addDamage(damageData.getDamage());
						damageaverage.addMP(damageData.getMP());
					}
				} else {
					damageData = new SRPGDamageData();
					processDamageInputAfter(damageData, roleIndex, index);
				}

				int cexp = ((actor.getActorStatus().level - actorObject
						.getActorStatus().level) + 1) * 4;
				if (cexp < 1) {
					cexp = 1;
				}
				actorExp += cexp;

				processDamageInputBefore(damageData, roleIndex, index);
				setDamage(damageData, actor);
				resetWindow();
				if (damageData.isHit()) {
					innerDamageValue(ability, damageData, index);
				}
				if (actor.getActorStatus().hp <= 0
						&& chp > 0
						&& !actor.getActorStatus().checkSkill(
								SRPGStatus.SKILL_UNDEAD)) {
					int exp = ((actor.getActorStatus().level - actorObject
							.getActorStatus().level) + 1) * 60 - 40;
					if (exp < 1) {
						exp = 1;
					}
					actorExp += exp;
				}
				if (ability.getDirect() == 0 && roleIndex != index) {
					actors[count] = index;
					count++;
				}
				setLock(lock);
			}
		}

		// ????????
		SRPGDamageData damagedata = ability.dataInput(damageaverage,
				actorObject.getActorStatus());

		if (damagedata != null) {
			boolean isLock = isLock();
			this.setLock(true);
			this.srpgDrawView = makeActorMiniStatusView(actorObject
					.getActorStatus(), actorObject);
			this.waitTime(sleepTime);
			this.processDamageInputBefore(damagedata, -1, roleIndex);
			this.setDamage(damagedata, srpgActors.find(roleIndex));
			this.resetWindow();
			this.setLock(isLock);
		}

		if (actorExp > maxExp) {
			actorExp = maxExp;
		}
		actorObject.getActorStatus().exp += actorExp;
		if (actorObject.getActorStatus().exp > maxExp) {
			actorObject.getActorStatus().exp = maxExp;
		}
		resetWindow();
		if (temp_view.isExist()) {
			srpgDrawView = temp_view;
		} else {
			srpgDrawView = new SRPGDrawView();
		}
		int type = 1;
		if (actorObject.getActorStatus().status[SRPGStatus.STATUS_DUPLICATE] != 0) {
			type = 2;
		}
		abilityoption.attack_value = abilityoption.attack_value + 1;
		if (type > abilityoption.attack_value
				&& abilitySuccess(ability, actorObject, null, posX, posY)) {
			boolean lock = isLock();
			setLock(true);
			waitTime(sleepTime);
			setEffect(makeUpperDeltaEffect(roleIndex, actorObject));
			waitTime(sleepTime);
			SRPGAbilityOption option = SRPGAbilityOption.getInstance(false);
			option.attack_value = abilityoption.attack_value;
			setDamageValueImplement(abilityIndex, roleIndex, option, false);
			setLock(lock);
		}
		if (!abilityoption.counter) {
			return;
		}
		int typeFlag = 1;
		if (ability.checkAbilitySkill(SRPGStatus.SKILL_STATUSINVALID)) {
			typeFlag = 2;
		}
		if (ability.checkAbilitySkill(SRPGStatus.SKILL_DOUBLEATTACK)) {
			typeFlag = 3;
		}
		for (int i = 1; i < typeFlag; i++) {
			if (abilitySuccess(ability, actorObject, null, posX, posY)) {
				setDamageValueImplement(abilityIndex, roleIndex,
						SRPGAbilityOption.getInstance(false), false);
			}
		}
		for (int i = 0; i < actors.length && actors[i] != -1; i++) {
			returningDamageValue(
					srpgActors.find(actors[i]).getActorStatus().ability,
					actors[i], roleIndex);
		}
		if ((ability.checkAbilitySkill(SRPGStatus.SKILL_DOUBLEACTION) || actorObject
				.getActorStatus().checkSkill(SRPGStatus.SKILL_DOUBLEATTACK))
				&& abilitySuccess(ability, srpgActors.find(roleIndex), null,
						posX, posY)) {
			setDamageValueImplement(abilityIndex, roleIndex, SRPGAbilityOption
					.getInstance(false), false);
		}

		processAttackAfter(roleIndex, actorObject);
		afterCheck();
		if (flag) {
			winnerCheck();
		}
		srpgEvent.reset();
	}

	protected boolean abilitySuccess(SRPGAbilityFactory ability, SRPGActor atk,
			SRPGActor def, int x, int y) {
		if (atk.getActorStatus().actionCheck()
				&& atk.getActorStatus().mp >= ability.getMP(atk
						.getActorStatus())) {
			boolean[][] range = ability.setTrueRange(srpgField, atk.getPosX(),
					atk.getPosY());
			if (def != null && range[def.getPosY()][def.getPosX()]) {
				return true;
			}
			if (def == null && range[y][x]) {
				if (ability.getSelectNeed() == 1) {
					return true;
				}
				int i = srpgActors.checkActor(x, y);
				if (i != -1
						&& ability.getSelectNeed() == 0
						&& ability.getTargetTrue(atk.getActorStatus().group,
								srpgActors.find(i).getActorStatus().group)) {
					return true;
				}
			}
		}
		return false;
	}

	protected void innerDamageValue(SRPGAbilityFactory ability,
			SRPGDamageData damagedata, int index) {
		SRPGActor actor = srpgActors.find(index);
		SRPGStatus status = actor.getActorStatus();
		if (ability.checkAbilitySkill(SRPGStatus.SKILL_UNDEAD)) {
			if (status.hp > 0) {
				status.hp = 0;
			}
			if (processDeadActor(index)) {
				actor.setVisible(false);
			}
		}
	}

	protected void setDamage(SRPGDamageData damagedata, SRPGActor actor) {
		SRPGStatus status = actor.getActorStatus();
		if (actor.drawX() - cam_x < tileWidth
				|| actor.drawY() - cam_y < tileHeight
				|| (actor.drawX() - cam_x) + tileWidth > getWidth() - tileWidth
				|| (actor.drawY() - cam_y) + tileHeight > getHeight()
						- tileHeight) {
			moveCamera((actor.drawX() + halfTileWidth) - halfWidth, (actor
					.drawY() + halfTileHeight)
					- halfHeight, 10);
		}
		if (damagedata.getGenre() != GENRE_MPRECOVERY
				&& damagedata.getGenre() != GENRE_MPDAMAGE) {
			status.hp = damagedata.getActorStatus().hp;
		} else {
			status.mp = damagedata.getActorStatus().mp;
		}
		setEffect(damagedata.getNumberEffect(actor.getPosX() * tileWidth, actor
				.getPosY()
				* tileHeight));
		if ((damagedata.getGenre() == GENRE_ALLDAMAGE || damagedata.getGenre() == GENRE_ALLRECOVERY)
				&& damagedata.isHit()) {
			int genre = 0;
			if (damagedata.getGenre() == GENRE_ALLDAMAGE) {
				genre = GENRE_MPRECOVERY;
			} else if (damagedata.getGenre() == GENRE_ALLRECOVERY) {
				genre = GENRE_MPDAMAGE;
			}
			status.mp = damagedata.getActorStatus().mp;
			setEffect(damagedata.getNumberEffect(genre, String
					.valueOf(damagedata.getMP()), actor.getPosX() * tileWidth,
					actor.getPosY() * tileHeight));
		}
		SRPGEffect[] effects = damagedata.getPopupEffect(actor.getPosX()
				* tileWidth, actor.getPosY() * tileHeight);
		if (effects != null) {
			for (int i = 0; i < effects.length; i++) {
				srpgEffect = effects[i];
				srpgEffect.wait(this);
			}
		}
		status.copy(damagedata.getActorStatus());
	}

	// ---- ???????? ----//

	/**
	 * ????????
	 * 
	 * @param i
	 * @param actor
	 */
	private void setMoveViews(int i, SRPGActor actor) {
		srpgPosition.number = i;
		move = actor.getActorStatus().move;
		if (srpgMove == null) {
			srpgMove = SRPGFieldMove.getInstance(srpgField.getMoveSpaceAll(
					srpgActors, i));
		} else {
			srpgMove.set(srpgField.getMoveSpaceAll(srpgActors, i));
		}
		srpgPosition.area = srpgMove.moveArea(actor.getPosX(), actor.getPosY(),
				move);
		procFlag = PROC_MOVEVIEW;
	}

	/**
	 * ????????
	 * 
	 * @param i
	 * @param actor
	 */
	private synchronized void setMove(int i, SRPGActor actor) {
		setMoveViews(i, actor);
		if (srpgChoiceField == null) {
			srpgChoiceField = new SRPGFieldChoiceView(srpgField);
		} else {
			srpgChoiceField.set(srpgField);
		}
		int[] pos = srpgChoiceField.choiceWait(this, true);
		resetWindow();
		if (pos == null) {
			procFlag = PROC_NORMAL;
		} else {
			if (actor.getActorStatus().team != 0
					|| actor.getActorStatus().action == 0
					|| !actor.getActorStatus().actionCheck()
					|| actor.getActorStatus().isComputer) {
				procFlag = PROC_NORMAL;
				return;
			}
			srpgPosition.route = srpgMove.moveRoute(actor.getPosX(), actor
					.getPosY(), pos[0], pos[1], move);
			if (actor.getPosX() == pos[0] && actor.getPosY() == pos[1]) {
				srpgPosition.setPast(actor.getPosX(), actor.getPosY());
				srpgPosition.vector = actor.getDirection();
				defaultCommand();
				return;
			}
			if (srpgActors.checkActor(pos[0], pos[1]) != -1) {
				setHelper(TOUCH_NO_SUPPORT[language_index][0]);
				setMove(i, actor);
				return;
			}
			if (srpgPosition.route == null) {
				setHelper(TOUCH_NO_SUPPORT[language_index][0]);
				setMove(i, actor);
				return;
			}
			procFlag = PROC_MOVING;
			srpgPosition.setPast(actor.getPosX(), actor.getPosY());
			srpgPosition.vector = actor.getDirection();
			callActorMove(srpgPosition.number);
			srpgEvent.reset();
			defaultCommand();
		}

	}

	/**
	 * ?CPU???????????
	 * 
	 * @param i
	 */
	private void moveCPU(int i) {
		moveCPU(i, true);
	}

	/**
	 * ?CPU????????????????????????
	 * 
	 * @param i
	 * @param flag
	 */
	private void moveCPU(int index, boolean flag) {
		centerCamera(index);
		SRPGActor actor = srpgActors.find(index);
		if (srpgAI == null) {
			srpgAI = new SRPGAI(srpgField, srpgActors, index, actor
					.getActorStatus().computer);
		} else {
			srpgAI.set(srpgField, srpgActors, index,
					actor.getActorStatus().computer);
		}
		srpgAI.runThinking();
		srpgPosition.number = index;
		// ????????
		if (isEnemyView()) {
			setMoveViews(index, actor);
			waitTime(sleepTime);
			procFlag = PROC_MOVING;
		}
		// ????????
		if (srpgAI.getRoute() != null) {
			callActorMove(index, srpgAI.getRoute());
			waitTime(sleepTime);
		}
		if (srpgAI.getAbility() != -1) {
			srpgPosition.setTarget(srpgAI.getTargetX(), srpgAI.getTargetY());
			srpgPosition.ability = srpgAI.getAbility();
			if (isEnemyView()) {
				setAttackRange(srpgAI.getAbility(), actor.getPosX(), actor
						.getPosY());
				procFlag = PROC_ABILITYTARGET;
				moveCameraCenter(srpgAI.getTargetX() * tileWidth
						+ halfTileWidth, srpgAI.getTargetY() * tileHeight
						+ halfTileHeight, 8);
				waitTime(sleepTime / 2);
				int role = srpgActors.checkActor(srpgAI.getTargetX(), srpgAI
						.getTargetY());
				if (!getTargetTrue(SRPGAbilityFactory.getInstance(srpgAI
						.getAbility()), srpgPosition.number, role)) {
					role = -1;
				}
				int[] res = { srpgAI.getTargetX(), srpgAI.getTargetY() };
				abilityTargetSetting(srpgAI.getAbility(), res, index, role);
				procFlag = PROC_TARGETSURE;
				waitTime(sleepTime * 3);
			}
			resetWindow();
			procFlag = PROC_ATTACK;
			setDamageValue(srpgAI.getAbility(), index, true, flag);
		} else if (srpgAI.getDirection() != -1) {
			actor.setDirection(srpgAI.getDirection());
		}
		procFlag = PROC_NORMAL;
		setTurnMinus();
	}

	/**
	 * ????????????????
	 * 
	 * @param i
	 */
	public void callActorMove(int i) {
		callActorMove(i, srpgPosition.route);
	}

	/**
	 * ????????????????????????
	 * 
	 * @param i
	 * @param res
	 */
	public void callActorMove(int i, int[][] res) {
		setCenterActor(i);
		SRPGActor actor = srpgActors.find(i);
		for (int j = 0; j < res.length; j++) {
			int direction = MOVE_DOWN;
			int x = res[j][0] - actor.getPosX();
			int y = res[j][1] - actor.getPosY();
			if (y < 0) {
				direction = MOVE_UP;
			} else if (y > 0) {
				direction = MOVE_DOWN;
			} else if (x < 0) {
				direction = MOVE_LEFT;
			} else if (x > 0) {
				direction = MOVE_RIGHT;
			}
			actor.moveActorShow(direction, getMoveSpeed());
			actor.waitMove(this);
		}
		centerCamera();
		setCenterActor(-1);
	}

	private void afterCheck() {
		boolean[] teamValues = new boolean[SRPGTeams.getTeamsValue(srpgActors)];
		for (int i = 0; i < teamValues.length; i++) {
			teamValues[i] = true;
		}
		for (int j = 0; j < srpgActors.size(); j++) {
			SRPGActor actor = srpgActors.find(j);
			if (!actor.isVisible()) {
				continue;
			}
			SRPGStatus status = actor.getActorStatus();
			if (status.status[SRPGStatus.STATUS_REVIVE] != 0 && status.hp <= 0
					&& !status.checkSkill(SRPGStatus.SKILL_UNDEAD)) {
				int hp = status.max_hp / 2;
				if (actor.drawX() - cam_x < 0 || actor.drawY() - cam_y < 0
						|| actor.drawX() - cam_x > getWidth() - tileWidth
						|| actor.drawY() - cam_y > getHeight() - tileHeight) {
					moveCamera(j, 10);
				}

				// ????
				actor.setVisible(false);
				processDeadActorAfter(j, actor);
				// ??250??
				waitTime(sleepTime);

				setEffect(SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_OUT, actor, actor.getPosX(),
						actor.getPosY()));

				srpgDrawView = makeActorMiniStatusView(actor.getActorStatus(),
						actor);

				waitTime(sleepTime);
				SRPGDamageData damagedata = new SRPGDamageData();
				damagedata.setDamage(hp);
				damagedata.setGenre(GENRE_RECOVERY);
				damagedata.setActorStatus(SRPGAbilityFactory.damageInput(
						damagedata, status));
				processDamageInputBefore(damagedata, -1, j);
				setDamage(damagedata, actor);
				int chp = status.hp;
				status.defaultStatus();
				status.hp = chp;
				resetWindow();
			}
			boolean flag = false;
			SRPGFieldElement element = srpgField.getPosMapElement(actor
					.getPosX(), actor.getPosY());
			// ??????
			if (element != null && element.state == SRPGField.FIELD_KILL) {
				flag = true;
				if (status.hp > 0) {
					status.hp = 0;
				}
			} else if (element != null && element.state == SRPGField.FIELD_PLUS) {

				flag = true;
				if (status.hp > 0) {
					status.hp += status.max_hp / 10;
					status.mp += status.max_mp / 10;
				}
				if (status.hp > status.max_hp) {
					status.hp = status.max_hp;
				}
				if (status.mp > status.max_mp) {
					status.mp = status.max_mp;
				}

			}
			if ((status.hp > 0 || status.checkSkill(SRPGStatus.SKILL_UNDEAD))
					&& !flag) {
				continue;
			}
			processDeadActorBefore(j, actor);
			if (!processDeadActor(j)) {
				continue;
			}
			if (actor.drawX() - cam_x < 0 || actor.drawY() - cam_y < 0
					|| actor.drawX() - cam_x > getWidth() - tileWidth
					|| actor.drawY() - cam_y > getHeight() - tileHeight) {
				moveCamera(j, 10);
			}
			actor.setVisible(false);
			if (teamValues[status.team]) {
				teamValues[status.team] = srpgTeams.leaderCheck(srpgActors, j);
			}
			processDeadActorAfter(j, actor);

		}

		// ???????????
		for (int j = 0; j < srpgActors.size(); j++) {
			SRPGActor actor = srpgActors.find(j);
			SRPGStatus status = actor.getActorStatus();
			if (!actor.isExist() || !actor.isVisible()
					|| (status.exp != -100 && status.exp < maxExp)) {
				continue;
			}
			setLock(true);
			processLevelUpBefore(j, actor);
			if (actor.drawX() - cam_x < 0 || actor.drawY() - cam_y < 0
					|| actor.drawX() - cam_x > getWidth() - tileWidth
					|| actor.drawY() - cam_y > getHeight() - tileHeight) {
				moveCamera(j, 10);
			}
			int d = actor.getDirection();
			int[] moving = { MOVE_DOWN, MOVE_LEFT, MOVE_UP, MOVE_RIGHT,
					MOVE_DOWN };
			for (int i = 0; i < moving.length; i++) {
				actor.setDirection(moving[i]);
				waitFrame(2);
			}
			if (status.exp != -100) {
				setEffect(new SRPGNumberEffect(actor.getPosX() * tileWidth,
						actor.getPosY() * tileHeight, GLColor.red, "Level Up!"));
				int level = status.level;
				if (level < maxLevel) {
					status = SRPGActorFactory.runLevelUp(status, level + 1);
				}

			} else {
				setEffect(new SRPGNumberEffect(actor.getPosX() * tileWidth,
						actor.getPosY() * tileHeight, GLColor.red,
						"Level Down!"));
			}

			status.exp = 0;
			actor.setDirection(d);
			processLevelUpAfter(j, actor);
			setLock(false);
		}

		for (int j = 0; j < teamValues.length; j++) {
			if (teamValues[j]) {
				continue;
			}
			processDeadTeam(j);
			setLock(true);
			for (int i = 0; i < srpgActors.size(); i++) {
				SRPGActor actor = srpgActors.find(i);
				if (actor.isVisible() && actor.getActorStatus().team == j) {
					waitTime(sleepTime);
					actor.setVisible(false);
				}
			}
			setLock(false);
		}

	}

	/**
	 * ????(???????????????)
	 * 
	 */
	protected void winnerCheck() {
		int group = 0;
		int index = 0;
		for (;;) {
			if (index >= srpgActors.size()) {
				break;
			}
			SRPGActor actor = srpgActors.find(index);
			if (actor.isVisible() && actor.getActorStatus().team == 0) {
				group = actor.getActorStatus().group;
				break;
			}
			index++;
		}
		if (!srpgTeams.checkPhase(0, srpgActors)) {
			for (; gameLoser();) {
				try {
					super.wait();
				} catch (Exception ex) {
				}
			}
			return;
		}
		for (int i = 0; i < srpgActors.size(); i++) {
			SRPGActor actor = srpgActors.find(i);
			if (actor.isVisible() && actor.getActorStatus().group != group) {
				return;
			}
		}
		for (; gameWinner();) {
			try {
				super.wait();
			} catch (Exception ex) {
			}
		}
	}

	public boolean getTargetTrue(SRPGAbilityFactory ability, int atk, int def) {
		return def != -1
				&& (ability.getTarget() != 0 || srpgActors.find(atk)
						.getActorStatus().group != srpgActors.find(def)
						.getActorStatus().group)
				&& (ability.getTarget() != 1 || srpgActors.find(atk)
						.getActorStatus().group == srpgActors.find(def)
						.getActorStatus().group);
	}

	/**
	 * ??????
	 * 
	 */
	protected synchronized void battleReset() {
		setLock(true);
		isEventLoop = false;
		waitTime(1000);
		srpgActors.reset();
		if (srpgActors != null) {
			initActorConfig(srpgActors);
		}
		createTeams(srpgActors);
		if (srpgTeams != null) {
			initTeamConfig(srpgTeams);
		}
		isEventLoop = true;
		mainProcess();
		setLock(false);
	}

	/**
	 * ????
	 * 
	 */
	private synchronized void teamList() {
		for (;;) {
			int size = SRPGTeams.getTeamsAlive(srpgActors);
			String[] names = new String[size];
			int[] phase = new int[size];
			int count = 0;
			newFor: for (int i = 0; i < srpgTeams.getLength(); i++) {
				int index = 0;
				for (;;) {
					if (index >= srpgActors.size()) {
						continue newFor;
					}
					if (srpgActors.find(index).isVisible()
							&& srpgActors.find(index).getActorStatus().team == srpgTeams
									.getTeamPhase(i)) {
						names[count] = srpgTeams.getName(i);
						phase[count] = srpgTeams.getTeamPhase(i);
						count++;
						continue newFor;
					}
					index++;
				}
			}
			if (srpgChoiceView == null) {
				srpgChoiceView = new SRPGChoiceView(names, phase, choiceFont,
						choiceX, choiceY);
			} else {
				srpgChoiceView.set(names, phase, choiceFont, choiceX, choiceY);
			}
			int index = srpgChoiceView.choiceWait(this, true);
			if (index != -1) {
				actorList(index);
			} else {
				return;
			}
		}
	}

	/**
	 * ????
	 * 
	 * @param i
	 */
	private synchronized void actorList(int i) {
		for (;;) {
			int j = 0;
			for (int c = 0; c < srpgActors.size(); c++) {
				if (srpgActors.find(c).isVisible()
						&& srpgActors.find(c).getActorStatus().team == i) {
					j++;
				}
			}
			if (j == 0) {
				return;
			}
			String[][] mes = new String[j][3];
			int[] list = new int[j];
			int l = 0;
			for (int c = 0; c < srpgActors.size(); c++) {
				if (!srpgActors.find(c).isVisible()) {
					continue;
				}
				SRPGStatus status = srpgActors.find(c).getActorStatus();
				if (status.team == i) {
					mes[l][0] = status.name;
					mes[l][1] = status.jobname;
					mes[l][2] = String.valueOf(status.hp + " / "
							+ status.max_hp);
					list[l] = c;
					l++;
				}
			}
			srpgChoiceView = new SRPGChoiceView(mes, list, choiceFont, 15, 15);
			srpgChoiceView.setTab(15);
			int index = srpgChoiceView.choiceWait(this, true);
			if (index != -1) {
				moveCamera((srpgActors.find(index).drawX() - halfWidth)
						+ halfTileWidth,
						(srpgActors.find(index).drawY() - halfHeight)
								+ halfTileHeight, 10);
				setCameraLock(true);
				waitTime(sleepTime);
				setCameraLock(false);
				srpgDrawView = makeActorStatusView(srpgActors.find(index)
						.getActorStatus());
			} else {
				return;
			}
		}

	}

	/**
	 * ???????1
	 * 
	 */
	protected synchronized void defaultBackMenu1() {
		boolean flag = true;
		for (;;) {
			if (!flag) {
				break;
			}
			createChoice(BACK_MENU_1[language_index], srpgChoiceView);
			switch (srpgChoiceView.choiceWait(this, true)) {
			case 0:
				createChoice(YES_NO[language_index], srpgChoiceView);
				if (srpgChoiceView.choiceWait(this, true) == 0) {
					srpgTeams.endTurn(srpgActors);
					flag = false;
				}
				break;
			case 1:
				teamList();
				break;
			case 2:
				defaultBackMenu2();
				break;
			case 3:
				createChoice(YES_NO[language_index], srpgChoiceView);
				if (srpgChoiceView.choiceWait(this, true) == 0) {
					flag = false;
					battleReset();
				}
				break;
			default:
				flag = false;
				break;
			}
		}
	}

	/**
	 * ???????2
	 * 
	 */
	protected synchronized void defaultBackMenu2() {
		boolean flag = true;
		int i = 0;
		for (;;) {
			if (!flag) {
				break;
			}
			int menuIndex = 0;

			// ??????,0
			String[] menuItemGridSelect = new String[2];
			menuItemGridSelect[0] = BACK_MENU_2[language_index][0];
			if (isGrid()) {
				menuItemGridSelect[1] = DISPLAY[language_index][0];
			} else {
				menuItemGridSelect[1] = DISPLAY[language_index][1];
			}
			menuItems[menuIndex++] = menuItemGridSelect;

			// ????????,1
			String[] menuItemTeamColorSelect = new String[2];
			menuItemTeamColorSelect[0] = BACK_MENU_2[language_index][1];
			if (isTeamColor()) {
				menuItemTeamColorSelect[1] = DISPLAY[language_index][0];
			} else {
				menuItemTeamColorSelect[1] = DISPLAY[language_index][1];
			}
			menuItems[menuIndex++] = menuItemTeamColorSelect;

			// ??????????,2
			String[] menuItemEndViewSelect = new String[2];
			menuItemEndViewSelect[0] = BACK_MENU_2[language_index][2];
			if (isEndView()) {
				menuItemEndViewSelect[1] = DISPLAY[language_index][0];
			} else {
				menuItemEndViewSelect[1] = DISPLAY[language_index][1];
			}
			menuItems[menuIndex++] = menuItemEndViewSelect;

			// ????????,4
			String[] menuItemEnemyViewSelect = new String[2];
			menuItemEnemyViewSelect[0] = BACK_MENU_2[language_index][3];
			if (isEnemyView()) {
				menuItemEnemyViewSelect[1] = DISPLAY[language_index][0];
			} else {
				menuItemEnemyViewSelect[1] = DISPLAY[language_index][1];
			}
			menuItems[menuIndex++] = menuItemEnemyViewSelect;

			if (srpgChoiceView == null) {
				// ????
				srpgChoiceView = new SRPGChoiceView(menuItems, choiceFont, 15,
						25);
			} else {
				srpgChoiceView.set(menuItems, choiceFont, 15, 25);
			}
			srpgChoiceView.setTab(15);
			srpgChoiceView.setContent(i);
			i = srpgChoiceView.choiceWait(this, true);
			switch (i) {
			// ????
			case 0:
				setGrid(!isGrid());
				break;

			// ????
			case 1:
				setTeamColor(!isTeamColor());
				break;

			// ????
			case 2:
				setEndView(!isEndView());
				break;

			// ????
			case 3:
				setEnemyView(!isEnemyView());
				break;

			case -1:
				flag = false;
				break;
			}
		}
	}

	/**
	 * ????????
	 * 
	 */
	protected synchronized void defaultCommand() {
		this.procFlag = PROC_COMMAND;
		SRPGActor actor = srpgActors.find(srpgPosition.number);
		setAttackRange(actor.getActorStatus().ability, actor.getPosX(), actor
				.getPosY());
		if (srpgChoiceView == null) {
			srpgChoiceView = new SRPGChoiceView(BATTLE[language_index],
					choiceFont, choiceX, choiceY);
		} else {
			srpgChoiceView.set(BATTLE[language_index], choiceFont, choiceX,
					choiceY);
		}
		srpgChoiceView.setTab(15);

		switch (srpgChoiceView.choiceWait(this, true)) {
		default:
			break;

		case 0:
			srpgPosition.ability = -1;
			setAbilityCommand();
			break;

		case 1:
			changeItem();
			break;
		case 2:
			changeDirection();
			break;

		case 3:
			srpgDrawView = makeActorStatusView(srpgActors.find(
					srpgPosition.number).getActorStatus());
			srpgChoiceView.setExist(true);
			defaultCommand();
			break;

		case 4:
			procFlag = PROC_NORMAL;
			setTurnMinus();
			break;

		case -1:
			int[] past = srpgPosition.past;
			SRPGActor actorObject = srpgActors.find(srpgPosition.number);
			actorObject.setPosX(past[0]);
			actorObject.setPosY(past[1]);
			actorObject.setDirection(srpgPosition.vector);
			int x = (actorObject.drawX() + halfTileWidth) - halfWidth;
			int y = (actorObject.drawY() + halfTileHeight) - halfHeight;
			setMoveViews(srpgPosition.number, actorObject);
			if (actorObject.drawX() - cam_x < tileWidth
					|| actorObject.drawY() - cam_y < tileHeight
					|| (actorObject.drawX() - cam_x) + tileWidth > getWidth()
							- tileWidth
					|| (actorObject.drawY() - cam_y) + tileHeight > getHeight()
							- tileHeight) {
				moveCamera(x, y, 10);
			}
			setMove(srpgPosition.number, actorObject);
			break;
		}
	}

	/**
	 * ?????????
	 * 
	 */
	protected synchronized void changeDirection() {
		this.procFlag = PROC_CHANGEVECTOR;
		if (srpgChoiceField == null) {
			srpgChoiceField = new SRPGFieldChoiceView(srpgField);
		} else {
			srpgChoiceField.set(srpgField);
		}
		int[] res = srpgChoiceField.choiceWait(this, true);
		resetWindow();
		if (res == null) {
			defaultCommand();
			return;
		}
		SRPGActor actor = srpgActors.find(srpgPosition.number);
		int x = actor.getPosX() - res[0];
		int y = actor.getPosY() - res[1];
		if (x < 0) {
			x *= -1;
		}
		if (y < 0) {
			y *= -1;
		}
		if (x + y != 1) {
			changeDirection();
			return;
		} else {
			actor.setDirection(actor.findDirection(res[0], res[1]));
			procFlag = PROC_NORMAL;
			setTurnMinus();
			return;
		}
	}

	// ---- ?????? ----//

	/**
	 * ???????????
	 * 
	 */
	private synchronized void setAbilityCommand() {
		SRPGStatus status = srpgActors.find(srpgPosition.number)
				.getActorStatus();
		int[] abilitys = SRPGAbilityFactory.filtedAbility(status.ability,
				status, true);
		if (abilitys == null) {
			setHelper(NO_SUPPORT[language_index][0]);
			defaultCommand();
			return;
		} else {
			setChoiceAbility(abilitys);
			procFlag = PROC_ABILITYSELECT;
			setAbilitySelect();
			return;
		}
	}

	/**
	 * ??????????
	 * 
	 */
	private synchronized void setAbilitySelect() {
		int i = srpgChoiceView.choiceWait(this, true);
		switch (i) {
		case -1:
			defaultCommand();
			break;
		default:
			SRPGAbilityFactory ability = SRPGAbilityFactory.getInstance(i);
			SRPGActor actor = srpgActors.find(srpgPosition.number);
			int mp = ability.getMP(actor.getActorStatus());
			if (ability.getMP(actor.getActorStatus()) > actor.getActorStatus().mp) {
				setHelper("   MP < " + mp + " !   ");
				srpgChoiceView.setExist(true);
				setAbilitySelect();
				break;
			}
			setAttackRange(i, actor.getPosX(), actor.getPosY());
			srpgPosition.ability = i;
			if (ability.getSelectNeed() == 0) {
				boolean flag = false;
				int[][] area = srpgPosition.area;
				int index = 0;
				for (;;) {
					if (index >= area.length) {
						break;
					}
					for (int role = 0; role < area[index].length; role++) {
						if (area[index][role] == 0) {
							continue;
						}
						int res = srpgActors.checkActor(role, index);
						if (!getTargetTrue(ability, srpgPosition.number, res)) {
							continue;
						}
						flag = true;
						break;
					}

					if (flag) {
						break;
					}
					index++;
				}
				if (!flag) {
					setHelper(NO_SUPPORT[language_index][0]);
					srpgChoiceView.setExist(true);
					setAbilitySelect();
					break;
				}
			}
			procFlag = PROC_ABILITYTARGET;
			abilityTarget();
			break;
		}
	}

	private void abilityTargetSetting(int i, int[] res, int atk, int def) {
		srpgPosition.setTarget(res[0], res[1]);
		srpgDrawView = makeDamageExpectView(SRPGAbilityFactory.getInstance(i),
				srpgField, atk, def);
		setTargetRange(i, res[0], res[1]);
	}

	private synchronized void abilityTarget() {
		if (srpgDrawView == null) {
			srpgDrawView = new SRPGDrawView();
		} else {
			srpgDrawView.reset();
		}
		boolean flag = false;
		SRPGAbilityFactory ability = SRPGAbilityFactory
				.getInstance(srpgPosition.ability);
		int[] pos = null;
		if (ability.getMinLength() != 0 || ability.getMaxLength() != 0) {
			if (srpgChoiceField == null) {
				srpgChoiceField = new SRPGFieldChoiceView(srpgField);
			} else {
				srpgChoiceField.set(srpgField);
			}
			pos = srpgChoiceField.choiceWait(this, true);
		} else {
			pos = new int[2];
			pos[0] = srpgActors.find(srpgPosition.number).getPosX();
			pos[1] = srpgActors.find(srpgPosition.number).getPosY();
			flag = true;
		}
		if (srpgDrawView == null) {
			srpgDrawView = new SRPGDrawView();
		} else {
			srpgDrawView.reset();
		}
		if (pos != null) {
			SRPGAbilityFactory ability1 = SRPGAbilityFactory
					.getInstance(srpgPosition.ability);
			int i = srpgActors.checkActor(pos[0], pos[1]);
			if (srpgPosition.area[pos[1]][pos[0]] == 0) {
				setHelper(TOUCH_NO_SUPPORT[language_index][0]);
				abilityTarget();
				return;
			}
			boolean result = true;
			if (!getTargetTrue(ability1, srpgPosition.number, i)) {
				result = false;
			}
			if (ability1.getSelectNeed() == 0) {
				if (i == -1) {
					setHelper(NO_SUPPORT[language_index][0]);
					abilityTarget();
					return;
				}
				if (!result) {
					setHelper(NO_SUPPORT[language_index][0]);
					abilityTarget();
					return;
				}
			}
			if (!result) {
				i = -1;
			}
			abilityTargetSetting(srpgPosition.ability, pos,
					srpgPosition.number, i);
			createChoice(YES_NO[language_index], srpgChoiceView);
			procFlag = PROC_TARGETSURE;
			switch (srpgChoiceView.choiceWait(this, true)) {
			case 0:
				procFlag = PROC_ATTACK;
				resetWindow();
				if (setDamageValue(srpgPosition.ability, srpgPosition.number)) {
					procFlag = 0;
					setTurnMinus();
					return;
				}

			case -1:
			case 1:
				setAttackRange(srpgPosition.ability, srpgActors.find(
						srpgPosition.number).getPosX(), srpgActors.find(
						srpgPosition.number).getPosY());
				procFlag = PROC_ABILITYTARGET;
				srpgChoiceView.setExist(false);
				if (!flag) {
					abilityTarget();
				} else {
					resetWindow();
					srpgPosition.ability = -1;
					setAbilityCommand();
				}
				return;
			}
		} else {
			srpgPosition.ability = -1;
			setAbilityCommand();
		}
	}

	private synchronized void setChoiceAbility(int[] res) {
		String[][] mes = new String[res.length][3];
		int[] abilitys = new int[res.length];
		int i = 0;
		for (int j = 0; j < res.length; j++) {
			SRPGAbilityF
