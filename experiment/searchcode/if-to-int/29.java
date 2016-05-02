package org.loon.framework.android.game.srpg.actor;

import org.loon.framework.android.game.action.sprite.AnimationHelper;
import org.loon.framework.android.game.core.LRelease;
import org.loon.framework.android.game.core.graphics.Screen;
import org.loon.framework.android.game.core.graphics.opengl.GLColor;
import org.loon.framework.android.game.core.graphics.opengl.GLEx;
import org.loon.framework.android.game.core.graphics.opengl.LTexture;
import org.loon.framework.android.game.srpg.SRPGType;

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
 * @version 0.1
 */
public class SRPGActor implements LRelease {

	private int posX, posY, tileWidth, tileHeight;

	private int move_direction;

	private int max_frame, frame;

	private int direction, animation, changeble, actionCount;

	private boolean isEnd, isAttack, isAction, isAnimation, isAutoEnd;

	private boolean isVisible, isExist;

	private SRPGStatus status;

	private LTexture[] actionImage;

	private AnimationHelper move, attack;

	public SRPGActor(String name, String fileName, int index, int level,
			int team, int tileWidth, int tileHeight) {
		this(SRPGActorFactory.makeActorStatus(name, index, level, team),
				AnimationHelper.makeRMVXObject(fileName), null, tileWidth,
				tileHeight);
	}

	public SRPGActor(String name, int index, int level, int team,
			AnimationHelper animation, int tileWidth, int tileHeight) {
		this(SRPGActorFactory.makeActorStatus(name, index, level, team),
				animation, null, tileWidth, tileHeight);
	}

	public SRPGActor(SRPGStatus status, AnimationHelper animation,
			int tileWidth, int tileHeight) {
		this(status, animation, null, tileWidth, tileHeight);
	}

	public SRPGActor(SRPGStatus status, AnimationHelper mv,
			AnimationHelper atk, int tileWidth, int tileHeight) {
		this.max_frame = 0;
		this.frame = 0;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.move = mv;
		if (atk == null) {
			attack = mv;
		} else {
			attack = atk;
		}
		this.direction = SRPGType.MOVE_DOWN;
		this.animation = 25;
		this.changeble = animation;
		this.actionCount = 0;
		this.isAnimation = true;
		this.isVisible = true;
		this.isExist = true;
		if (status != null) {
			this.status = status;
		} else {
			this.status = new SRPGStatus();
			this.isVisible = false;
			this.isExist = false;
		}
	}

	SRPGActor() {
	}

	/**
	 * ??????
	 * 
	 * @param direction
	 * @return
	 */
	public LTexture[] getAnimationImages(final AnimationHelper o, final int d) {
		LTexture[] result = null;
		switch (d) {
		// ????
		case SRPGType.MOVE_DOWN:
			result = o.downImages;
			break;
		// ????
		case SRPGType.MOVE_UP:
			result = o.upImages;
			break;
		// ????
		case SRPGType.MOVE_LEFT:
			result = o.leftImages;
			break;
		// ????
		case SRPGType.MOVE_RIGHT:
			result = o.rightImages;
			break;
		default:
			result = o.downImages;
		}
		return result;
	}

	/**
	 * ????????
	 * 
	 * @return
	 */
	public LTexture getActionImage() {
		if (actionImage != null) {
			return actionImage[actionCount];
		}
		return null;
	}

	/**
	 * ???????????
	 * 
	 * @return
	 */
	public LTexture getImage() {
		LTexture[] img = getAnimationImages(move, SRPGType.MOVE_DOWN);
		if (img != null) {
			return img[actionImage.length / 2];
		}
		return null;
	}

	/**
	 * ??????????????
	 * 
	 * @return
	 */
	public boolean getStatus() {
		return frame < max_frame && status != null && status.action != 0;
	}

	/**
	 * ?????????
	 * 
	 */
	public void waitMove() {
		for (; getStatus();) {
		}
	}

	/**
	 * ????Screen???
	 * 
	 * @param screen
	 */
	public void waitMove(final Screen screen) {
		for (; getStatus();) {
			try {
				screen.wait();
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * ????????????????
	 * 
	 * @param d
	 * @param frame
	 */
	public void moveActorShow(int d, int frame) {
		if (getStatus()) {
			return;
		} else {
			moveActor(d, frame);
			isAnimation = true;
			isAutoEnd = true;
			return;
		}
	}

	/**
	 * ????????????????
	 * 
	 * @param d
	 * @param frame
	 */
	public void moveActor(int d, int frame) {
		if (getStatus()) {
			return;
		} else {
			moveActorOnly(d, frame);
			direction = d;
			return;
		}
	}

	/**
	 * ????????????????
	 * 
	 * @param d
	 * @param f
	 */
	public void moveActorOnly(int d, int f) {
		if (getStatus()) {
			return;
		}
		this.frame = 0;
		this.max_frame = f;
		switch (d) {
		case SRPGType.MOVE_LEFT:
			posX--;
			break;
		case SRPGType.MOVE_RIGHT:
			posX++;
			break;
		case SRPGType.MOVE_UP:
			posY--;
			break;
		case SRPGType.MOVE_DOWN:
			posY++;
			break;
		}
		this.move_direction = d;
	}

	/**
	 * ????X,Y??????????
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int findDirection(int x, int y) {
		int nx = x - posX;
		int ny = y - posY;
		int tx = nx;
		int ty = ny;
		if (tx < 0) {
			tx *= -1;
		}
		if (ty < 0) {
			ty *= -1;
		}
		if (tx > ty) {
			return nx >= 0 ? SRPGType.MOVE_RIGHT : SRPGType.MOVE_LEFT;
		}
		if (tx < ty) {
			return ny >= 0 ? SRPGType.MOVE_DOWN : SRPGType.MOVE_UP;
		}
		if (nx < 0 && ny < 0) {
			if (direction == SRPGType.MOVE_RIGHT) {
				return SRPGType.MOVE_UP;
			}
			if (direction == SRPGType.MOVE_DOWN) {
				return SRPGType.MOVE_LEFT;
			}
		} else if (nx > 0 && ny < 0) {
			if (direction == SRPGType.MOVE_LEFT) {
				return SRPGType.MOVE_UP;
			}
			if (direction == SRPGType.MOVE_DOWN) {
				return SRPGType.MOVE_RIGHT;
			}
		} else if (nx < 0 && ny > 0) {
			if (direction == SRPGType.MOVE_RIGHT) {
				return SRPGType.MOVE_DOWN;
			}
			if (direction == SRPGType.MOVE_UP) {
				return SRPGType.MOVE_LEFT;
			}
		} else if (nx > 0 && ny > 0) {
			if (direction == SRPGType.MOVE_LEFT) {
				return SRPGType.MOVE_DOWN;
			}
			if (direction == SRPGType.MOVE_UP) {
				return SRPGType.MOVE_RIGHT;
			}
		}
		return direction;
	}

	/**
	 * ????SRPG??????????
	 * 
	 * @param actor
	 * @return
	 */
	public int getDirectionStatus(SRPGActor actor) {
		int d = actor.getDirection();
		int newd = actor.findDirection(getPosX(), getPosY());
		if (d == newd) {
			return SRPGType.MOVE_DOWN;
		}
		return (d != SRPGType.MOVE_UP || newd != SRPGType.MOVE_DOWN)
				&& (d != SRPGType.MOVE_DOWN || newd != SRPGType.MOVE_UP)
				&& (d != SRPGType.MOVE_RIGHT || newd != SRPGType.MOVE_LEFT)
				&& (d != SRPGType.MOVE_LEFT || newd != SRPGType.MOVE_RIGHT) ? SRPGType.MOVE_LEFT
				: SRPGType.MOVE_RIGHT;

	}

	/**
	 * ????????????
	 * 
	 * @param d
	 * @return
	 */
	public final static int matchDirection(final int d) {
		int result = 0;
		switch (d) {
		case SRPGType.MOVE_UP:
			result = SRPGType.MOVE_DOWN;
			break;
		case SRPGType.MOVE_DOWN:
			result = SRPGType.MOVE_UP;
			break;
		case SRPGType.MOVE_LEFT:
			result = SRPGType.MOVE_RIGHT;
			break;
		case SRPGType.MOVE_RIGHT:
			result = SRPGType.MOVE_LEFT;
			break;
		}
		return result;
	}

	/**
	 * ?????????
	 * 
	 */
	public void next() {
		if (!isVisible()) {
			return;
		} else {
			frame++;
			return;
		}
	}

	/**
	 * ????
	 * 
	 * @param g
	 * @param x
	 * @param y
	 */
	private void drawActor(GLEx g, int x, int y) {
		boolean isMoving = (this.status.action != 0);
		// ????
		if (isAttack) {
			actionImage = getAnimationImages(attack, direction);
			if (actionCount >= actionImage.length - 1) {
				setAttack(false);
			}
			// ????
		} else if (isAction) {
			actionImage = getAnimationImages(attack, direction);
			// ????
		} else {
			actionImage = getAnimationImages(move, direction);
		}
		// ????
		if (isAnimation && animation > 0 && isMoving) {
			changeble--;
			if (changeble <= 0) {
				changeble = animation;
				actionCount++;
				if (actionCount >= actionImage.length) {
					actionCount = 0;
				}
			}
		}

		if (!getStatus() && isAutoEnd) {
			this.isAutoEnd = false;
			this.max_frame = 0;
			this.frame = 0;
		}
		LTexture img = actionImage[actionCount];

		// ???(?????????????)
		if (isMoving) {
			g.drawTexture(img, x + (tileWidth - img.getWidth()) / 2, y
					+ (tileHeight - img.getHeight()) / 2);
			// ??????
		} else if (isEnd) {
			g.drawTexture(img, x + (tileWidth - img.getWidth()) / 2, y
					+ (tileHeight - img.getHeight()) / 2, GLColor.gray);
			// ????
		} else {
			g.drawTexture(img, x + (tileWidth - img.getWidth()) / 2, y
					+ (tileHeight - img.getHeight()) / 2);
		}
	}

	/**
	 * ???????????
	 * 
	 * @param g
	 * @param x
	 * @param y
	 */
	public void draw(final GLEx g, int x, int y) {
		if (isVisible()) {
			drawActor(g, drawX() - x, drawY() - y);
		}
	}

	/**
	 * ????????X??
	 * 
	 * @return
	 */
	public int drawX() {
		int x = posX * tileWidth;
		if (getStatus()) {
			int j = (frame * tileWidth) / max_frame;
			switch (move_direction) {
			case SRPGType.MOVE_LEFT:
				x += tileWidth - j;
				break;
			case SRPGType.MOVE_RIGHT:
				x -= tileWidth - j;
				break;
			}
		}
		return x;
	}

	/**
	 * ????????Y??
	 * 
	 * @return
	 */
	public int drawY() {
		int y = posY * tileHeight;
		if (getStatus()) {
			int j = (frame * tileHeight) / max_frame;
			switch (move_direction) {
			case SRPGType.MOVE_UP:
				y += tileHeight - j;
				break;
			case SRPGType.MOVE_DOWN:
				y -= tileHeight - j;
				break;
			}
		}
		return y;
	}

	public boolean isAttack() {
		return isAttack;
	}

	public void setAttack(boolean attack) {
		this.isAttack = attack;
	}

	public boolean isAction() {
		return isAction;
	}

	public void setAction(boolean action) {
		this.isAction = action;
	}

	public boolean isActionEnd() {
		return isEnd;
	}

	public void setActionEnd(boolean e) {
		isEnd = e;
	}

	public void setPosX(int x) {
		max_frame = 0;
		posX = x;
	}

	public void setPosY(int y) {
		max_frame = 0;
		posY = y;
	}

	public void setPos(int x, int y) {
		setPosX(x);
		setPosY(y);
	}

	public void setPos(int[] res) {
		setPosX(res[0]);
		setPosY(res[1]);
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}

	public int[] getPos() {
		int[] res = new int[2];
		res[0] = getPosX();
		res[1] = getPosY();
		return res;
	}

	public boolean isVisible() {
		return isExist && isVisible;
	}

	public void setVisible(boolean flag) {
		isVisible = flag;
	}

	public boolean isExist() {
		return isExist;
	}

	public void setExist(boolean flag) {
		isExist = flag;
	}

	public void setDirection(int d) {
		this.direction = d;
	}

	public int getDirection() {
		return direction;
	}

	public void setCount(int i) {
		actionCount = i;
	}

	public int getCount() {
		return actionCount;
	}

	public void setAnimation(boolean flag) {
		isAnimation = flag;
	}

	public boolean isAnimation() {
		return isAnimation;
	}

	public void setAnimationFrame(int i) {
		animation = i;
		changeble = i;
	}

	public int getAnimationFrame() {
		return animation;
	}

	public void setActorStatus(SRPGStatus status) {
		this.status = status;
	}

	public SRPGStatus getActorStatus() {
		return this.status;
	}

	public int getTileHeight() {
		return tileHeight;
	}

	public void setTileHeight(int tileHeight) {
		this.tileHeight = tileHeight;
	}

	public int getTileWidth() {
		return tileWidth;
	}

	public void setTileWidth(int tileWidth) {
		this.tileWidth = tileWidth;
	}

	public void dispose() {

	}

}

