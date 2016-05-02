package org.loon.framework.javase.game.core.graphics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.loon.framework.javase.game.action.sprite.ISprite;
import org.loon.framework.javase.game.action.sprite.Sprites;
import org.loon.framework.javase.game.action.sprite.Sprites.SpriteListener;
import org.loon.framework.javase.game.core.EmulatorButtons;
import org.loon.framework.javase.game.core.EmulatorListener;
import org.loon.framework.javase.game.core.LHandler;
import org.loon.framework.javase.game.core.LInput;
import org.loon.framework.javase.game.core.LObject;
import org.loon.framework.javase.game.core.LRelease;
import org.loon.framework.javase.game.core.LSystem;
import org.loon.framework.javase.game.core.LTransition;
import org.loon.framework.javase.game.core.geom.RectBox;
import org.loon.framework.javase.game.core.graphics.component.LLayer;
import org.loon.framework.javase.game.core.graphics.component.awt.AWTInputDialog;
import org.loon.framework.javase.game.core.graphics.component.awt.AWTMessageDialog;
import org.loon.framework.javase.game.core.graphics.component.awt.AWTOpenDialog;
import org.loon.framework.javase.game.core.graphics.component.awt.AWTYesNoCancelDialog;
import org.loon.framework.javase.game.core.graphics.device.LGraphics;
import org.loon.framework.javase.game.core.timer.LTimerContext;
import org.loon.framework.javase.game.media.SoundBox;
import org.loon.framework.javase.game.utils.FileUtils;
import org.loon.framework.javase.game.utils.GraphicsUtils;
import org.loon.framework.javase.game.utils.log.Level;
import org.loon.framework.javase.game.utils.log.Log;
import org.loon.framework.javase.game.utils.log.LogFactory;

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
 * @email??ceponline ceponline@yahoo.com.cn
 * @version 0.1.2
 */
public abstract class Screen extends SoundBox implements MouseListener,
		MouseMotionListener, KeyListener, FocusListener, LInput, LRelease {

	public static final int ACTION_DOWN = 0;

	public static final int ACTION_UP = 1;

	public static final int ACTION_MOVE = 2;

	public class LKey {

		int type;

		int keyCode;

		char keyChar;

		LKey() {

		}

		LKey(LKey key) {
			this.type = key.type;
			this.keyCode = key.keyCode;
			this.keyChar = key.keyChar;
		}

		public boolean equals(LKey e) {
			if (e == null) {
				return false;
			}
			if (e == this) {
				return true;
			}
			if (e.type == type && e.keyCode == keyCode && e.keyChar == keyChar) {
				return true;
			}
			return false;
		}

		public char getKeyChar() {
			return keyChar;
		}

		public int getKeyCode() {
			return keyCode;
		}

		public int getType() {
			return type;
		}

	}

	public static class LTouch {

		int type;

		float x, y;

		int action;

		int pointer;

		LTouch() {

		}

		LTouch(LTouch touch) {
			this.type = touch.type;
			this.x = touch.x;
			this.y = touch.y;
			this.action = touch.action;
			this.pointer = touch.pointer;
		}

		public boolean equals(LTouch e) {
			if (e == null) {
				return false;
			}
			if (e == this) {
				return true;
			}
			if (e.type == type && e.x == x && e.y == y && e.action == action
					&& e.pointer == pointer) {
				return true;
			}
			return false;
		}

		public int getAction() {
			return action;
		}

		public int getPointer() {
			return pointer;
		}

		public int getType() {
			return type;
		}

		public int x() {
			return (int) x;
		}

		public int y() {
			return (int) y;
		}

		public float getX() {
			return x;
		}

		public float getY() {
			return y;
		}

	}

	public final static int SCREEN_NOT_REPAINT = 0;

	public final static int SCREEN_BITMAP_REPAINT = -1;

	public final static int SCREEN_CANVAS_REPAINT = -2;

	private int mode, frame;

	private boolean mouseExists, isNext, isDraging, isComponents;

	public long fps, elapsedTime;

	private Point mouse = new Point(0, 0);

	private int touchX, touchY, lastTouchX, lastTouchY, touchDX, touchDY;

	private final static boolean[] touchType, keyType;

	private int touchButtonPressed = LInput.NO_BUTTON,
			touchButtonReleased = LInput.NO_BUTTON;

	private int keyButtonPressed = LInput.NO_KEY,
			keyButtonReleased = LInput.NO_KEY;

	private LInput baseInput;

	private LHandler handler;

	// ????
	private Sprites sprites;

	// ????
	private Desktop desktop;

	// ????
	private BufferedImage currentScreen;

	// ??????
	private final ArrayList<Runnable> runnables;

	private final Log log;

	private int id;

	private int width, height, halfWidth, halfHeight;

	private boolean isLoad, isLock, isClose;

	private static class ThreadID {

		private static int nextThreadID = 1;

		private static ThreadLocal threadID = new ThreadLocal() {
			protected synchronized Object initialValue() {
				return new Integer(nextThreadID++);
			}
		};

		public static int get() {
			return ((Integer) (threadID.get())).intValue();
		}

	}

	static {
		keyType = new boolean[15];
		touchType = new boolean[15];
	}

	/**
	 * ????????????
	 * 
	 */
	public Screen() {
		LSystem.AUTO_REPAINT = true;
		this.handler = LSystem.getSystemHandler();
		this.log = LogFactory.getInstance(this.getClass());
		this.runnables = new ArrayList<Runnable>(1);
		this.width = LSystem.screenRect.width;
		this.height = LSystem.screenRect.height;
		this.halfWidth = width / 2;
		this.halfHeight = height / 2;
		this.setFPS(getMaxFPS());
	}

	/**
	 * ?Screen???(?????)???????
	 * 
	 * @param width
	 * @param height
	 */
	public void onCreate(int width, int height) {
		this.mode = SCREEN_CANVAS_REPAINT;
		this.width = width;
		this.height = height;
		this.halfWidth = width / 2;
		this.halfHeight = height / 2;
		this.baseInput = this;
		if (sprites != null) {
			sprites.dispose();
			sprites = null;
		}
		this.sprites = new Sprites(width, height);
		if (desktop != null) {
			desktop.dispose();
			desktop = null;
		}
		this.desktop = new Desktop(baseInput, width, height);
		this.mouseExists = true;
		this.touchX = touchY = lastTouchX = lastTouchY = touchDX = touchDY = 0;
		this.isDraging = isComponents = isLoad = isLock = isClose = false;
		this.isNext = true;
	}

	/**
	 * ???Screen?????????(?????LTransition??null??????????)
	 * 
	 * @return
	 */
	public LTransition onTransition() {
		return null;
	}

	/**
	 * ??????
	 * 
	 * @param lock
	 */
	public void setLock(boolean lock) {
		this.isLock = lock;
	}

	/**
	 * ???????????
	 * 
	 * @return
	 */
	public boolean isLock() {
		return isLock;
	}

	public void setClose(boolean close) {
		this.isClose = close;
	}

	public boolean isClose() {
		return isClose;
	}

	/**
	 * ?????
	 * 
	 * @param frame
	 */
	public synchronized void setFrame(int frame) {
		this.frame = frame;
	}

	/**
	 * ?????
	 * 
	 * @return
	 */
	public synchronized int getFrame() {
		return frame;
	}

	/**
	 * ?????
	 * 
	 * @return
	 */
	public synchronized boolean next() {
		this.frame++;
		return isNext;
	}

	/**
	 * ????Screen??????
	 * 
	 * @param i
	 */
	public synchronized void waitFrame(int i) {
		for (int wait = frame + i; frame < wait;) {
			try {
				super.wait(0L, 1);
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * ????Screen????
	 * 
	 * @param i
	 */
	public synchronized void waitTime(long i) {
		for (long time = System.currentTimeMillis() + i; System
				.currentTimeMillis() < time;)
			try {
				super.wait(time - System.currentTimeMillis());
			} catch (Exception ex) {
			}
	}

	/**
	 * ?????????
	 */
	public void setEmulatorListener(EmulatorListener emulator) {
		if (handler.getDeploy() != null) {
			handler.getDeploy().setEmulatorListener(emulator);
		}
	}

	/**
	 * ????????
	 * 
	 * @return
	 */
	public EmulatorButtons getEmulatorButtons() {
		if (handler.getDeploy() != null) {
			return handler.getDeploy().getEmulatorButtons();
		}
		return null;
	}

	/**
	 * ???????????
	 * 
	 * @param visible
	 */
	public void emulatorButtonsVisible(boolean visible) {
		if (handler.getDeploy() != null) {
			try {
				EmulatorButtons es = handler.getDeploy().getEmulatorButtons();
				es.setVisible(visible);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * ????????
	 * 
	 * @param runnable
	 */
	public final void callEvent(Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
		}
	}

	/**
	 * ?????????
	 * 
	 * @param runnable
	 */
	public final void callEventWait(Runnable runnable) {
		synchronized (runnable) {
			synchronized (runnables) {
				runnables.add(runnable);
			}
			try {
				runnable.wait();
			} catch (InterruptedException ex) {
			}
		}
	}

	/**
	 * ????????
	 * 
	 */
	public final void callEventInterrupt() {
		synchronized (runnables) {
			for (Iterator it = runnables.iterator(); it.hasNext();) {
				Object running = it.next();
				synchronized (running) {
					if (running instanceof Thread) {
						((Thread) running).setPriority(Thread.MIN_PRIORITY);
						((Thread) running).interrupt();
					}
				}
			}
		}
	}

	/**
	 * ??????
	 * 
	 */
	public final void callEvents() {
		callEvents(true);
	}

	/**
	 * ????????????
	 * 
	 * @param execute
	 */
	private final void callEvents(boolean execute) {
		if (!execute) {
			synchronized (runnables) {
				runnables.clear();
			}
			return;
		}
		if (runnables.size() == 0) {
			return;
		}
		ArrayList runnableList;
		synchronized (runnables) {
			runnableList = new ArrayList<Runnable>(runnables);
			runnables.clear();
		}
		for (Iterator it = runnableList.iterator(); it.hasNext();) {
			Object running = it.next();
			synchronized (running) {
				try {
					if (running instanceof Thread) {
						Thread thread = (Thread) running;
						if (!thread.isAlive()) {
							thread.start();
						}

					} else {
						((Runnable) running).run();
					}
				} catch (Exception ex) {
				}
				running.notifyAll();
			}
		}
		runnableList = null;
	}

	/**
	 * ?????????
	 */
	public void onLoad() {

	}

	/**
	 * ???????
	 * 
	 */
	public void onLoaded() {

	}

	/**
	 * ????????
	 */
	public void setOnLoadState(boolean flag) {
		this.isLoad = flag;
	}

	/**
	 * ????????????
	 */
	public boolean isOnLoadComplete() {
		return isLoad;
	}

	/**
	 * ????Screen??
	 */
	public String getName() {
		return FileUtils.getExtension(getClass().getName());
	}

	/**
	 * ?????Screen???
	 * 
	 */
	public void runFirstScreen() {
		if (handler != null) {
			handler.runFirstScreen();
		}
	}

	/**
	 * ??????Screen???
	 */
	public void runLastScreen() {
		if (handler != null) {
			handler.runLastScreen();
		}
	}

	/**
	 * ???????Screen
	 * 
	 * @param index
	 */
	public void runIndexScreen(int index) {
		if (handler != null) {
			handler.runIndexScreen(index);
		}
	}

	public void runPreviousScreen() {
		if (handler != null) {
			handler.runPreviousScreen();
		}
	}

	public void runNextScreen() {
		if (handler != null) {
			handler.runNextScreen();
		}
	}

	/**
	 * ??????Screen??????????
	 * 
	 * @param screen
	 */
	public void addScreen(Screen screen) {
		if (handler != null) {
			handler.addScreen(screen);
		}
	}

	/**
	 * ?????Screen??
	 * 
	 * @return
	 */
	public LinkedList<Screen> getScreens() {
		if (handler != null) {
			return handler.getScreens();
		}
		return null;
	}

	/**
	 * ?????Screen??
	 */
	public int getScreenCount() {
		if (handler != null) {
			return handler.getScreenCount();
		}
		return 0;
	}

	/**
	 * ????????ID
	 * 
	 * @return
	 */
	public int getID() {
		return id;
	}

	/**
	 * ?????????????
	 * 
	 * @param w
	 * @param h
	 */
	public void resize() {
		this.id = ThreadID.get();
		if (handler != null) {
			int w = handler.getWidth(), h = handler.getHeight();
			if (w < 1 || h < 1) {
				w = h = 1;
			}
			if (w != width || h != height) {
				width = w;
				height = h;
			} else {
				Thread.yield();
				return;
			}
		}
		this.setBackground(GraphicsUtils.createIntdexedImage(width, height));
		if (sprites != null) {
			sprites.dispose();
			sprites = null;
		}
		this.sprites = new Sprites(width, height);
		if (desktop != null) {
			desktop.dispose();
			desktop = null;
		}
		this.desktop = new Desktop(baseInput, width, height);
	}

	/**
	 * ??????
	 * 
	 * @return
	 */
	public SpriteListener getSprListerner() {
		if (sprites == null) {
			return null;
		}
		return sprites.getSprListerner();
	}

	/**
	 * ??Screen???
	 * 
	 * @param sprListerner
	 */
	public void setSprListerner(SpriteListener sprListerner) {
		if (sprites == null) {
			return;
		}
		sprites.setSprListerner(sprListerner);
	}

	/**
	 * ???????
	 * 
	 */
	public void dispose() {

	}

	/**
	 * ??info??
	 * 
	 * @param message
	 */
	public void info(String message) {
		log.info(message);
	}

	/**
	 * ??info??
	 * 
	 * @param message
	 * @param tw
	 */
	public void info(String message, Throwable tw) {
		log.info(message, tw);
	}

	/**
	 * ??debug??
	 * 
	 * @param message
	 */
	public void debug(String message) {
		log.debug(message);
	}

	/**
	 * ??debug??
	 * 
	 * @param message
	 * @param tw
	 */
	public void debug(String message, Throwable tw) {
		log.debug(message, tw);
	}

	/**
	 * ??warn??
	 * 
	 * @param message
	 */
	public void warn(String message) {
		log.warn(message);
	}

	/**
	 * ??warn??
	 * 
	 * @param message
	 * @param tw
	 */
	public void warn(String message, Throwable tw) {
		log.warn(message, tw);
	}

	/**
	 * ??error??
	 * 
	 * @param message
	 */
	public void error(String message) {
		log.error(message);
	}

	/**
	 * ??error??
	 * 
	 * @param message
	 * @param tw
	 */
	public void error(String message, Throwable tw) {
		log.error(message, tw);
	}

	/**
	 * ????
	 * 
	 * @param message
	 */
	public void log(String message) {
		log.log(message);
	}

	/**
	 * ????
	 * 
	 * @param message
	 * @param tw
	 */
	public void log(String message, Throwable tw) {
		log.log(message, tw);
	}

	/**
	 * ????????????
	 * 
	 * @param show
	 */
	public void logShow(boolean show) {
		log.setVisible(show);
	}

	/**
	 * ???????????
	 * 
	 * @param save
	 */
	public void logSave(boolean save) {
		log.setSave(save);
	}

	/**
	 * ??????
	 * 
	 * @param fileName
	 */
	public void logFileName(String fileName) {
		log.setFileName(fileName);
	}

	/**
	 * ??????
	 * 
	 * @param level
	 */
	public void logLevel(int level) {
		log.setLevel(level);
	}

	/**
	 * ??????
	 * 
	 * @param level
	 */
	public void logLevel(Level level) {
		log.setLevel(level);
	}

	/**
	 * ??????????
	 * 
	 * @param sprite
	 * @return
	 */
	public boolean onClick(ISprite sprite) {
		if (sprite == null) {
			return false;
		}
		if (sprite.isVisible()) {
			RectBox rect = sprite.getCollisionBox();
			if (rect.contains(touchX, touchY)
					|| rect.intersects(touchX, touchY)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ??????????
	 * 
	 * @param component
	 * @return
	 */
	public boolean onClick(LComponent component) {
		if (component == null) {
			return false;
		}
		if (component.isVisible()) {
			RectBox rect = component.getCollisionBox();
			if (rect.contains(touchX, touchY)
					|| rect.intersects(touchX, touchY)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ?????
	 * 
	 */
	public void mouseCenter() {
		try {
			GraphicsDevice device = GraphicsEnvironment
					.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			DisplayMode mode = device.getDisplayMode();
			this.touchX = this.lastTouchX = (mode.getWidth() / 2) - 10;
			this.touchY = this.lastTouchY = (mode.getHeight() / 2) - 10;
			LSystem.RO_BOT.mouseMove(this.touchX, this.touchY);
		} catch (Exception e) {
		}
	}

	/**
	 * ????AWT???
	 * 
	 * @param title
	 * @param message
	 */
	public AWTInputDialog showAWTInputDialog(final String title,
			final String message) {
		final AWTInputDialog dialog = new AWTInputDialog(title, message);
		callEvent(new Thread() {
			public void run() {
				dialog.setVisible(true);
			}
		});
		return dialog;
	}

	/**
	 * ????AWT???
	 * 
	 * @param title
	 * @param message
	 */
	public AWTMessageDialog showAWTMessageDialog(final String title,
			final String message) {
		final AWTMessageDialog dialog = new AWTMessageDialog(title, message);
		callEvent(new Thread() {
			public void run() {
				dialog.setVisible(true);
			}
		});
		return dialog;
	}

	/**
	 * ????AWT???
	 * 
	 * @param title
	 * @param message
	 */
	public AWTYesNoCancelDialog showAWTYesNoCancelDialog(String title,
			String message) {
		final AWTYesNoCancelDialog dialog = new AWTYesNoCancelDialog(title,
				message);
		callEvent(new Thread() {
			public void run() {
				dialog.setVisible(true);
			}
		});
		return dialog;
	}

	/**
	 * ????AWT?????
	 * 
	 * @param title
	 * @param message
	 * @return
	 */
	public AWTOpenDialog showAWTOpenDialog(String title, String path) {
		final AWTOpenDialog dialog = new AWTOpenDialog(title, path);
		callEvent(new Thread() {
			public void run() {
				dialog.setVisible(true);
			}
		});
		return dialog;
	}

	/**
	 * ??????
	 */
	public synchronized void setupHandler(LHandler handler) {
		this.handler = handler;
	}

	/**
	 * ????????
	 * 
	 * @return
	 */
	public synchronized LHandler getHandler() {
		return handler;
	}

	/**
	 * ????????
	 * 
	 * @param image
	 */
	public void setFrameIcon(Image icon) {
		if (handler != null) {
			handler.getScene().setIconImage(icon);
		}
	}

	/**
	 * ????????
	 */
	public void setFrameIcon(String fileName) {
		if (handler != null) {
			handler.getScene().setIconImage(fileName);
		}
	}

	/**
	 * ???????
	 * 
	 * @param title
	 */
	public void setFrameTitle(String title) {
		if (handler != null) {
			handler.getScene().setTitle(title);
		}
	}

	/**
	 * ??????
	 * 
	 * @param screen
	 */
	public synchronized void setScreen(Screen screen) {
		if (handler != null) {
			screen.setupHandler(handler);
			this.handler.setScreen(screen);
		}
	}

	/**
	 * ?????
	 * 
	 * @param fps
	 */
	public void setFPS(long fps) {
		if (handler != null) {
			handler.getDeploy().getView().setFPS(fps);
		}
	}

	/**
	 * ?????
	 */
	public long getFPS() {
		if (handler != null) {
			return handler.getDeploy().getView().getCurrentFPS();
		}
		return 0;
	}

	/**
	 * ???????
	 */
	public long getMaxFPS() {
		if (handler != null) {
			return handler.getDeploy().getView().getMaxFPS();
		}
		return 0;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getHalfWidth() {
		return halfWidth;
	}

	public int getHalfHeight() {
		return halfHeight;
	}

	public LInput getInput() {
		return baseInput;
	}

	public void setInput(LInput input) {
		this.baseInput = input;
	}

	public Point getTouch() {
		mouse.setLocation(touchX, touchY);
		return mouse;
	}

	public Desktop getDesktop() {
		return desktop;
	}

	public Sprites getSprites() {
		return sprites;
	}

	/**
	 * ???????????
	 */
	public ArrayList getComponents(Class<? extends LComponent> clazz) {
		if (desktop != null) {
			return desktop.getComponents(clazz);
		}
		return null;
	}

	/**
	 * ???????????
	 * 
	 * @return
	 */
	public LComponent getTopComponent() {
		if (desktop != null) {
			return desktop.getTopComponent();
		}
		return null;
	}

	/**
	 * ???????????
	 * 
	 * @return
	 */
	public LComponent getBottomComponent() {
		if (desktop != null) {
			return desktop.getBottomComponent();
		}
		return null;
	}

	/**
	 * ???????????
	 */
	public LLayer getTopLayer() {
		if (desktop != null) {
			return desktop.getTopLayer();
		}
		return null;
	}

	/**
	 * ???????????
	 */
	public LLayer getBottomLayer() {
		if (desktop != null) {
			return desktop.getBottomLayer();
		}
		return null;
	}

	/**
	 * ????????????
	 * 
	 */
	public ArrayList<ISprite> getSprites(Class<? extends ISprite> clazz) {
		if (sprites != null) {
			return sprites.getSprites(clazz);
		}
		return null;
	}

	/**
	 * ???????????
	 * 
	 */
	public ISprite getTopSprite() {
		if (sprites != null) {
			return sprites.getTopSprite();
		}
		return null;
	}

	/**
	 * ???????????
	 * 
	 */
	public ISprite getBottomSprite() {
		if (sprites != null) {
			return sprites.getBottomSprite();
		}
		return null;
	}

	/**
	 * ??????
	 * 
	 * @param comp
	 */
	public void add(LComponent comp) {
		if (desktop != null) {
			desktop.add(comp);
		}
	}

	/**
	 * ??????
	 * 
	 * @param sprite
	 */
	public void add(ISprite sprite) {
		if (sprites != null) {
			sprites.add(sprite);
		}
	}

	public synchronized void remove(LComponent comp) {
		if (desktop != null) {
			desktop.remove(comp);
		}
	}

	public synchronized void remove(Class<? extends LComponent> comp) {
		if (desktop != null) {
			desktop.remove(comp);
		}
	}

	public synchronized void removeComponent(Class<? extends LComponent> clazz) {
		if (desktop != null) {
			desktop.remove(clazz);
		}
	}

	public synchronized void remove(ISprite sprite) {
		if (sprites != null) {
			sprites.remove(sprite);
		}
	}

	public synchronized void removeSprite(Class<? extends ISprite> clazz) {
		if (sprites != null) {
			sprites.remove(clazz);
		}
	}

	public synchronized void removeAll() {
		if (sprites != null) {
			sprites.removeAll();
		}
		if (desktop != null) {
			desktop.getContentPane().clear();
		}
	}

	public void centerOn(final LObject object) {
		LObject.centerOn(object, getWidth(), getHeight());
	}

	public void topOn(final LObject object) {
		LObject.topOn(object, getWidth(), getHeight());
	}

	public void leftOn(final LObject object) {
		LObject.leftOn(object, getWidth(), getHeight());
	}

	public void rightOn(final LObject object) {
		LObject.rightOn(object, getWidth(), getHeight());
	}

	public void bottomOn(final LObject object) {
		LObject.bottomOn(object, getWidth(), getHeight());
	}

	public boolean openBrowser(String url) {
		return LSystem.openBrowser(url);
	}

	public int getRepaintMode() {
		return mode;
	}

	public void setRepaintMode(int mode) {
		this.mode = mode;
	}

	/**
	 * ?????????
	 */
	public void update(long timer) {
		this.touchDX = touchX - lastTouchX;
		this.touchDY = touchY - lastTouchY;
		this.lastTouchX = touchX;
		this.lastTouchY = touchY;
		this.keyButtonReleased = NO_KEY;
		this.touchButtonReleased = NO_BUTTON;
	}

	/**
	 * ??????
	 */
	public void refresh() {
		for (int i = 0; i < touchType.length; i++) {
			touchType[i] = false;
		}
		touchDX = touchDY = 0;
		for (int i = 0; i < keyType.length; i++) {
			keyType[i] = false;
		}
	}

	/**
	 * ????
	 */
	public synchronized void mouseMove(int x, int y) {
		LSystem.RO_BOT.mouseMove(x, y);
	}

	public boolean isMouseExists() {
		return this.mouseExists;
	}

	public boolean isTouchClick() {
		return touchButtonPressed == MouseEvent.BUTTON1;
	}

	public boolean isTouchClickUp() {
		return touchButtonReleased == MouseEvent.BUTTON3;
	}

	public int getTouchPressed() {
		return touchButtonPressed > LInput.NO_BUTTON ? touchButtonPressed
				: LInput.NO_BUTTON;
	}

	public int getTouchReleased() {
		return touchButtonReleased > LInput.NO_BUTTON ? touchButtonReleased
				: LInput.NO_BUTTON;
	}

	public boolean isTouchPressed(int button) {
		return touchButtonPressed == button;
	}

	public boolean isTouchReleased(int button) {
		return touchButtonReleased == button;
	}

	public boolean isMoving() {
		return isDraging;
	}

	public int getTouchX() {
		return touchX;
	}

	public int getTouchY() {
		return touchY;
	}

	public int getTouchDX() {
		return touchDX;
	}

	public int getTouchDY() {
		return touchDY;
	}

	public boolean isTouchType(int type) {
		return touchType[type];
	}

	public int getKeyPressed() {
		return keyButtonPressed > LInput.NO_KEY ? keyButtonPressed
				: LInput.NO_KEY;
	}

	public boolean isKeyPressed(int keyCode) {
		return keyButtonPressed == keyCode;
	}

	public int getKeyReleased() {
		return keyButtonReleased > LInput.NO_KEY ? keyButtonReleased
				: LInput.NO_KEY;
	}

	public boolean isKeyReleased(int keyCode) {
		return keyButtonReleased == keyCode;
	}

	public boolean isKeyType(int type) {
		return keyType[type];
	}

	/**
	 * ??????
	 * 
	 * @param color
	 */
	public void setBackground(Color color) {
		int w = getWidth(), h = getHeight();
		BufferedImage image = GraphicsUtils.createIntdexedImage(w, h);
		Graphics2D g = image.createGraphics();
		g.setColor(color);
		g.fillRect(0, 0, w, h);
		g.dispose();
		this.setBackground(image);
	}

	public void setBackground(LImage screen) {
		if (screen != null) {
			setBackground(screen.getBufferedImage());
		}
	}

	/**
	 * ??????
	 * 
	 * @param screen
	 */
	public void setBackground(BufferedImage screen) {
		if (screen != null) {
			if (screen.getWidth() != getWidth()
					|| screen.getHeight() != getHeight()) {
				screen = GraphicsUtils.getResize(screen, getWidth(),
						getHeight());
			}
			this.currentScreen = screen;
			this.setRepaintMode(SCREEN_BITMAP_REPAINT);
		} else {
			this.setRepaintMode(SCREEN_CANVAS_REPAINT);
		}
	}

	/**
	 * ??????
	 * 
	 * @param screen
	 */
	public void setBackground(Image screen) {
		this.setBackground(GraphicsUtils.getBufferImage(screen));
	}

	/**
	 * ??????
	 * 
	 * @param fileName
	 */
	public void setBackground(String fileName) {
		this.setBackground(GraphicsUtils.loadBufferedImage(fileName));
	}

	/**
	 * ????????????
	 */
	public void runTimer(LTimerContext timer) {
		if (isClose) {
			return;
		}
		this.elapsedTime = timer.getTimeSinceLastUpdate();
		if (sprites != null && this.sprites.size() > 0) {
			this.sprites.update(elapsedTime);
		}
		if (desktop != null
				&& this.desktop.getContentPane().getComponentCount() > 0) {
			this.desktop.update(elapsedTime);
		}
		this.baseInput.update(elapsedTime);
		this.alter(timer);
	}

	/**
	 * ???????
	 * 
	 * @param g
	 */
	public abstract void draw(LGraphics g);

	/**
	 * ????UI
	 */
	public synchronized void createUI(final LGraphics g) {
		if (isClose) {
			return;
		}
		draw(g);
		if (sprites != null) {
			sprites.createUI(g);
		}
		if (desktop != null) {
			desktop.createUI(g);
		}
	}

	/**
	 * ???????????????
	 * 
	 * @param fileName
	 */
	public void saveScreenImage(String fileName) {
		GraphicsUtils.saveImage(GraphicsUtils.getBufferImage(getScreenImage()),
				fileName);
	}

	/**
	 * ??????????
	 * 
	 */
	public void saveScreenImage() {
		GraphicsUtils.saveImage(GraphicsUtils.getBufferImage(getScreenImage()),
				LSystem.getLScreenFile());
	}

	/**
	 * ?????????
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	public Image getScreenImage() {
		if (handler == null) {
			return null;
		}
		return handler.getDeploy().getView().getAwtImage();
	}

	/**
	 * ??????????????
	 * 
	 * @param w
	 * @param h
	 * @return
	 */
	public Image getScreenImage(int w, int h) {
		return GraphicsUtils.getResize(getScreenImage(), w, h);
	}

	/**
	 * ??Screen?????
	 * 
	 * @return
	 */
	public Rectangle getBounds() {
		if (handler == null) {
			return null;
		}
		Window window = handler.getScene().getWindow();
		Rectangle bounds = window.getBounds();
		Insets insets = window.getInsets();
		return new Rectangle(bounds.x + insets.left, bounds.y + insets.top,
				bounds.width - (insets.left + insets.top), bounds.height
						- (insets.top + insets.bottom));
	}

	/**
	 * ????????????,????????
	 * 
	 */
	private void checkFullScreen() {
		if (isComponents) {
			throw new RuntimeException(
					"Using the AWT/Swing components can not be changed to full screen !");
		}
	}

	/**
	 * ?????????
	 * 
	 * @param d
	 */
	public void updateFullScreen(Dimension d) {
		updateFullScreen((int) d.getWidth(), (int) d.getHeight());
	}

	/**
	 * ?????????
	 * 
	 */
	public void updateFullScreen(int w, int h) {
		checkFullScreen();
		if (handler != null) {
			handler.getScene().updateFullScreen(w, h);
		}
	}

	/**
	 * ????
	 */
	public void updateFullScreen() {
		checkFullScreen();
		if (handler != null) {
			handler.getScene().updateFullScreen();
		}
	}

	/**
	 * ????
	 * 
	 */
	public void updateNormalScreen() {
		checkFullScreen();
		if (handler != null) {
			handler.getScene().updateNormalScreen();
		}
	}

	/**
	 * ?????????????????
	 * 
	 */
	public boolean contains(ISprite sprite) {
		return sprites.contains(sprite);
	}

	/**
	 * ?????????????????
	 * 
	 * @param comp
	 * @return
	 */
	public boolean contains(LComponent comp) {
		return desktop.getContentPane().contains(comp);
	}

	/**
	 * ???????????
	 * 
	 * @param sprite
	 */
	public void sendSpriteToFront(ISprite sprite) {
		sprites.sendToFront(sprite);
	}

	/**
	 * ???????????
	 * 
	 * @param sprite
	 */
	public void sendSpriteToBack(ISprite sprite) {
		sprites.sendToBack(sprite);
	}

	/**
	 * ???????????
	 * 
	 */
	public void setNext(boolean next) {
		this.isNext = next;
	}

	/**
	 * ??????
	 */
	public Image getBackground() {
		return currentScreen;
	}

	/**
	 * ????????
	 * 
	 * @param timeMillis
	 */
	public void pause(long timeMillis) {
		try {
			Thread.sleep(timeMillis);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * ??????
	 * 
	 * @param timer
	 */
	public abstract void alter(LTimerContext timer);

	/**
	 * ??????
	 * 
	 * @return
	 */
	public boolean leftClick() {
		return this.baseInput.isTouchPressed(MouseEvent.BUTTON1);
	}

	/**
	 * ???????(??)
	 * 
	 * @return
	 */
	public boolean middleClick() {
		return this.baseInput.isTouchPressed(MouseEvent.BUTTON2);
	}

	/**
	 * ??????
	 * 
	 * @return
	 */
	public boolean rightClick() {
		return this.baseInput.isTouchPressed(MouseEvent.BUTTON3);
	}

	public void keyTyped(KeyEvent e) {
		e.consume();
	}

	final LKey key = new LKey();

	/**
	 * ????
	 */
	public void keyPressed(KeyEvent e) {
		if (isLock || isClose || !isLoad) {
			return;
		}
		int code = e.getKeyCode();
		if (e.getModifiers() == InputEvent.ALT_MASK
				&& e.getKeyCode() == KeyEvent.VK_F4) {
			if (handler != null) {
				handler.getScene().close();
			}
		}
		int type = ACTION_DOWN;
		key.keyChar = e.getKeyChar();
		key.keyCode = e.getKeyCode();
		key.type = type;
		try {
			this.onKeyDown(key);
			keyType[type] = true;
			keyButtonPressed = code;
			keyButtonReleased = LInput.NO_KEY;
		} catch (Exception ex) {
			keyButtonPressed = LInput.NO_KEY;
			keyButtonReleased = LInput.NO_KEY;
			ex.printStackTrace();
		}

	}

	public void onKeyDown(LKey e) {

	}

	/**
	 * ????????
	 * 
	 * @param code
	 */
	public void setKeyDown(int code) {
		try {
			keyButtonPressed = code;
			keyButtonReleased = LInput.NO_KEY;
		} catch (Exception e) {
		}
	}

	/**
	 * ????
	 */
	public void keyReleased(KeyEvent e) {
		if (isLock || isClose || !isLoad) {
			return;
		}
		int type = ACTION_UP;
		int code = e.getKeyCode();
		key.keyChar = e.getKeyChar();
		key.keyCode = e.getKeyCode();
		key.type = type;
		try {
			this.onKeyUp(key);
			keyType[type] = false;
			keyButtonReleased = code;
			keyButtonPressed = LInput.NO_KEY;
		} catch (Exception ex) {
			keyButtonPressed = LInput.NO_KEY;
			keyButtonReleased = LInput.NO_KEY;
			ex.printStackTrace();
		}
	}

	public void onKeyUp(LKey e) {

	}

	/**
	 * ????????
	 * 
	 * @param code
	 */
	public void setKeyUp(int code) {
		try {
			keyButtonReleased = code;
			keyButtonPressed = LInput.NO_KEY;
		} catch (Exception e) {
		}
	}

	/**
	 * ??????????
	 * 
	 * @param e
	 */
	public void leftClick(MouseEvent e) {

	}

	/**
	 * ???????????
	 * 
	 * @param e
	 */
	public void middleClick(MouseEvent e) {

	}

	/**
	 * ??????????
	 * 
	 * @param e
	 */
	public void rightClick(MouseEvent e) {

	}

	final LTouch touch = new LTouch();

	/**
	 * ????
	 * 
	 * @param e
	 */
	public abstract void onTouchDown(LTouch e);

	/**
	 * ????
	 */
	public void mousePressed(MouseEvent e) {
		if (isLock || isClose || !isLoad) {
			return;
		}
		try {
			if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
				leftClick(e);
			}
			if ((e.getModifiers() & InputEvent.BUTTON2_MASK) != 0) {
				middleClick(e);
			}
			if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
				rightClick(e);
			}
			int type = ACTION_DOWN;
			int button = e.getButton();
			touch.action = type;
			touch.type = button;
			touch.pointer = 1;
			touch.x = e.getX();
			touch.y = e.getY();
			this.touchX = e.getX();
			this.touchY = e.getY();
			this.isDraging = false;
			try {
				touchType[type] = true;
				touchButtonPressed = button;
				touchButtonReleased = LInput.NO_BUTTON;
				onTouchDown(touch);
			} catch (Exception ex) {
				touchButtonPressed = LInput.NO_BUTTON;
				touchButtonReleased = LInput.NO_BUTTON;
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * ????
	 * 
	 * @param e
	 */
	public abstract void onTouchUp(LTouch e);

	/**
	 * ????
	 */
	public void mouseReleased(MouseEvent e) {
		if (isLock || isClose || !isLoad) {
			return;
		}
		int type = ACTION_UP;
		int button = e.getButton();
		touch.action = type;
		touch.type = button;
		touch.pointer = 1;
		touch.x = e.getX();
		touch.y = e.getY();
		this.touchX = e.getX();
		this.touchY = e.getY();
		this.isDraging = false;
		try {
			touchType[type] = false;
			touchButtonReleased = button;
			touchButtonPressed = LInput.NO_BUTTON;
			onTouchUp(touch);
		} catch (Exception ex) {
			touchButtonPressed = LInput.NO_BUTTON;
			touchButtonReleased = LInput.NO_BUTTON;
			ex.printStackTrace();
		}
	}

	public abstract void onTouchMove(LTouch e);

	public synchronized void mouseDragged(MouseEvent e) {
		if (isLock || isClose || !isLoad) {
			return;
		}
		int type = ACTION_MOVE;
		int button = e.getButton();
		touch.action = type;
		touch.type = button;
		touch.pointer = 1;
		touch.x = e.getX();
		touch.y = e.getY();
		this.touchX = e.getX();
		this.touchY = e.getY();
		onTouchMove(touch);
		this.isDraging = true;
	}

	public synchronized void mouseMoved(MouseEvent e) {
		if (isLock || isClose || !isLoad) {
			return;
		}
		if (!isDraging) {
			int type = ACTION_MOVE;
			int button = e.getButton();
			touch.action = type;
			touch.type = button;
			touch.pointer = 1;
			touch.x = e.getX();
			touch.y = e.getY();
			this.touchX = e.getX();
			this.touchY = e.getY();
			onTouchMove(touch);
		}
	}

	public synchronized void mouseClicked(MouseEvent e) {
	}

	public synchronized void mouseEntered(MouseEvent e) {
		if (isLock || isClose || !isLoad) {
			return;
		}
		this.mouseExists = true;
	}

	public synchronized void mouseExited(MouseEvent e) {
		if (isLock || isClose || !isLoad) {
			return;
		}
		this.mouseExists = false;
	}

	public void move(double x, double y) {
		if (isLock || isClose || !isLoad) {
			return;
		}
		this.touchX = (int) x;
		this.touchY = (int) y;
	}

	public void focusGained(FocusEvent e) {
		this.isNext = true;
	}

	public void focusLost(FocusEvent e) {
		this.isNext = false;
		this.refresh();
	}

	public void addComponent(final Component component, final int x,
			final int y, final int w, final int h) {
		if (handler != null) {
			if (handler.getDeploy().addComponent(x, y, w, h, component)) {
				isComponents = true;
			}
		} else {
			Thread componentThread = new Thread(new Runnable() {
				public void run() {
					while (handler == null) {
						Thread.yield();
					}
					if (handler != null) {
						if (handler.getDeploy().addComponent(x, y, w, h,
								component)) {
							isComponents = true;
						}
					}
				}
			});
			componentThread.start();
		}
	}

	public void addComponent(Component component, int x, int y) {
		addComponent(component, x, y, component.getWidth(), component
				.getHeight());
	}

	public void addComponent(Component component) {
		addComponent(component, 0, 0);
	}

	public void removeComponent(Component component) {
		if (handler != null) {
			handler.getDeploy().removeComponent(component);
		}
	}

	public void removeComponent(int index) {
		if (handler != null) {
			this.handler.getDeploy().removeComponent(index);
		}
	}

	/**
	 * ???????
	 * 
	 */
	public void destroy() {
		synchronized (this) {
			isClose = true;
			callEvents(false);
			isNext = false;
			isDraging = false;
			isLock = true;
			isComponents = false;
			if (sprites != null) {
				sprites.dispose();
				sprites = null;
			}
			if (desktop != null) {
				desktop.dispose();
				desktop = null;
			}
			if (currentScreen != null) {
				currentScreen.flush();
				currentScreen = null;
			}
			dispose();
		}
	}
}

