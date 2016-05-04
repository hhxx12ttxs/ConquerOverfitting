package org.loon.framework.android.game.action.sprite;

import org.loon.framework.android.game.action.collision.CollisionHelper;
import org.loon.framework.android.game.core.LObject;
import org.loon.framework.android.game.core.LSystem;
import org.loon.framework.android.game.core.geom.Point;
import org.loon.framework.android.game.core.geom.RectBox;
import org.loon.framework.android.game.core.geom.Vector2f;
import org.loon.framework.android.game.core.graphics.device.LGraphics;
import org.loon.framework.android.game.core.graphics.device.LTrans;
import org.loon.framework.android.game.core.graphics.opengl.GLColor;
import org.loon.framework.android.game.core.graphics.opengl.GLEx;
import org.loon.framework.android.game.core.graphics.opengl.LTexture;
import org.loon.framework.android.game.utils.TextureUtils;

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
public class Sprite extends LObject implements ISprite, LTrans {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1982110847888726016L;

	// ????????
	final static private long defaultTimer = 150;

	// ????
	private boolean visible = true;

	// ????
	private String spriteName;

	// ????
	private LTexture image;

	// ??
	private Animation animation = new Animation();

	private int transform;

	private float rotation, scaleX = 1, scaleY = 1, alpha = 1;

	/**
	 * ??????
	 * 
	 */
	public Sprite() {
		this(0, 0);
	}

	/**
	 * ??????? ??x,??y
	 * 
	 * @param x
	 * @param y
	 */
	public Sprite(float x, float y) {
		this("Sprite" + System.currentTimeMillis(), x, y);
	}

	/**
	 * ??????? ???,??x,??y
	 * 
	 * @param spriteName
	 * @param x
	 * @param y
	 */
	private Sprite(String spriteName, float x, float y) {
		this.setLocation(x, y);
		this.spriteName = spriteName;
		this.visible = true;
		this.transform = LTrans.TRANS_NONE;
	}

	/**
	 * ??????? ????,??????,??????
	 * 
	 * @param fileName
	 * @param row
	 * @param col
	 */
	public Sprite(String fileName, int row, int col) {
		this(fileName, -1, 0, 0, row, col, defaultTimer);
	}

	/**
	 * ??????? ????,??????,??????,????????
	 * 
	 * @param fileName
	 * @param row
	 * @param col
	 * @param timer
	 */
	public Sprite(String fileName, int row, int col, long timer) {
		this(fileName, -1, 0, 0, row, col, timer);
	}

	/**
	 * ??????? ????,??x,??y,??????,??????
	 * 
	 * @param fileName
	 * @param x
	 * @param y
	 * @param row
	 * @param col
	 */
	public Sprite(String fileName, float x, float y, int row, int col) {
		this(fileName, x, y, row, col, defaultTimer);
	}

	/**
	 * ??????? ????,??x,??y,??????,??????,????????
	 * 
	 * @param fileName
	 * @param x
	 * @param y
	 * @param row
	 * @param col
	 * @param timer
	 */
	private Sprite(String fileName, float x, float y, int row, int col,
			long timer) {
		this(fileName, -1, x, y, row, col, timer);
	}

	/**
	 * ??????? ????,??????,??x,??y,??????,??????
	 * 
	 * @param fileName
	 * @param maxFrame
	 * @param x
	 * @param y
	 * @param row
	 * @param col
	 */
	public Sprite(String fileName, int maxFrame, float x, float y, int row,
			int col) {
		this(fileName, maxFrame, x, y, row, col, defaultTimer);
	}

	/**
	 * ??????? ????,??????,??x,??y,??????,??????,????????
	 * 
	 * @param fileName
	 * @param maxFrame
	 * @param x
	 * @param y
	 * @param row
	 * @param col
	 * @param timer
	 */
	public Sprite(String fileName, int maxFrame, float x, float y, int row,
			int col, long timer) {
		this("Sprite" + System.currentTimeMillis(), fileName, maxFrame, x, y,
				row, col, timer);
	}

	/**
	 * ??????? ???????????????,??x,??y,??????,??????,????????
	 * 
	 * @param spriteName
	 * @param fileName
	 * @param maxFrame
	 * @param x
	 * @param y
	 * @param row
	 * @param col
	 * @param timer
	 */
	public Sprite(String spriteName, String fileName, int maxFrame, float x,
			float y, int row, int col, long timer) {
		this(spriteName, TextureUtils.getSplitTextures(fileName, row, col),
				maxFrame, x, y, timer);
	}

	/**
	 * ??????
	 * 
	 * @param fileName
	 */
	public Sprite(String fileName) {
		this(new LTexture(fileName));
	}

	/**
	 * ??????
	 * 
	 * @param images
	 */
	public Sprite(final LTexture img) {
		this(new LTexture[] { img }, 0, 0);
	}

	/**
	 * ??????? ????
	 * 
	 * @param images
	 */
	public Sprite(LTexture[] images) {
		this(images, 0, 0);
	}

	/**
	 * ??????? ????,??x,??y
	 * 
	 * @param images
	 * @param x
	 * @param y
	 */
	public Sprite(LTexture[] images, float x, float y) {
		this(images, x, y, defaultTimer);
	}

	/**
	 * ??????? ????,????????
	 * 
	 * @param images
	 * @param timer
	 */
	public Sprite(LTexture[] images, long timer) {
		this(images, -1, 0, 0, defaultTimer);
	}

	/**
	 * ??????? ????,??x,??y,????????
	 * 
	 * @param images
	 * @param x
	 * @param y
	 * @param timer
	 */
	public Sprite(LTexture[] images, float x, float y, long timer) {
		this(images, -1, x, y, timer);
	}

	/**
	 * ??????? ????,??????,??x,??y,????????
	 * 
	 * @param spriteName
	 * @param images
	 * @param maxFrame
	 * @param x
	 * @param y
	 * @param timer
	 */
	public Sprite(LTexture[] images, int maxFrame, float x, float y, long timer) {
		this("Sprite" + System.currentTimeMillis(), images, maxFrame, x, y,
				timer);
	}

	/**
	 * ??????? ???????????????,??x,??y,????????
	 * 
	 * @param spriteName
	 * @param images
	 * @param maxFrame
	 * @param x
	 * @param y
	 * @param timer
	 */
	public Sprite(String spriteName, LTexture[] images, int maxFrame, float x,
			float y, long timer) {
		this.setLocation(x, y);
		this.spriteName = spriteName;
		this.setAnimation(animation, images, maxFrame, timer);
		this.visible = true;
		this.transform = LTrans.TRANS_NONE;
	}

	/**
	 * ???????
	 * 
	 * @param running
	 */
	public void setRunning(boolean running) {
		animation.setRunning(running);
	}

	/**
	 * ???????
	 * 
	 * @return
	 */
	public int getTotalFrames() {
		return animation.getTotalFrames();
	}

	/**
	 * ?????
	 * 
	 * @param index
	 */
	public void setCurrentFrameIndex(int index) {
		animation.setCurrentFrameIndex(index);
	}

	/**
	 * ???????
	 * 
	 * @return
	 */
	public int getCurrentFrameIndex() {
		return animation.getCurrentFrameIndex();
	}

	/**
	 * ??????????????
	 * 
	 * @param x
	 * @return
	 */
	public int centerX(int x) {
		return centerX(this, x);
	}

	/**
	 * ??????????????
	 * 
	 * @param sprite
	 * @param x
	 * @return
	 */
	public static int centerX(Sprite sprite, int x) {
		int newX = x - (sprite.getWidth() / 2);
		if (newX + sprite.getWidth() >= LSystem.screenRect.width) {
			return (LSystem.screenRect.width - sprite.getWidth() - 1);
		}
		if (newX < 0) {
			return x;
		} else {
			return newX;
		}
	}

	/**
	 * ??????????????
	 * 
	 * @param y
	 * @return
	 */
	public int centerY(int y) {
		return centerY(this, y);
	}

	/**
	 * ??????????????
	 * 
	 * @param sprite
	 * @param y
	 * @return
	 */
	public static int centerY(Sprite sprite, int y) {
		int newY = y - (sprite.getHeight() / 2);
		if (newY + sprite.getHeight() >= LSystem.screenRect.height) {
			return (LSystem.screenRect.height - sprite.getHeight() - 1);
		}
		if (newY < 0) {
			return y;
		} else {
			return newY;
		}
	}

	/**
	 * ??????
	 * 
	 * @param myAnimation
	 * @param images
	 * @param maxFrame
	 * @param timer
	 */
	private void setAnimation(Animation myAnimation, LTexture[] images,
			int maxFrame, long timer) {
		if (maxFrame != -1) {
			for (int i = 0; i < maxFrame; i++) {
				myAnimation.addFrame(images[i], timer);
			}
		} else {
			for (int i = 0; i < images.length; i++) {
				myAnimation.addFrame(images[i], timer);
			}
		}
	}

	/**
	 * ??????
	 * 
	 * @param fileName
	 * @param maxFrame
	 * @param row
	 * @param col
	 * @param timer
	 */
	public void setAnimation(String fileName, int maxFrame, int row, int col,
			long timer) {
		setAnimation(new Animation(), TextureUtils.getSplitTextures(fileName,
				row, col), maxFrame, timer);
	}

	/**
	 * ??????
	 * 
	 * @param fileName
	 * @param row
	 * @param col
	 * @param timer
	 */
	public void setAnimation(String fileName, int row, int col, long timer) {
		setAnimation(fileName, -1, row, col, timer);
	}

	/**
	 * ??????
	 * 
	 * @param images
	 * @param maxFrame
	 * @param timer
	 */
	public void setAnimation(LTexture[] images, int maxFrame, long timer) {
		setAnimation(new Animation(), images, maxFrame, timer);
	}

	/**
	 * ??????
	 * 
	 * @param images
	 * @param timer
	 */
	public void setAnimation(LTexture[] images, long timer) {
		setAnimation(new Animation(), images, -1, timer);
	}

	/**
	 * ??????
	 * 
	 * @param animation
	 */
	public void setAnimation(Animation animation) {
		this.animation = animation;
	}

	public Animation getAnimation() {
		return animation;
	}

	/**
	 * ????
	 */
	public void update(long timer) {
		if (visible) {
			animation.update(timer);
		}
	}

	/**
	 * ???????
	 * 
	 * @param vector
	 */
	public void updateLocation(Vector2f vector) {
		this.setX(Math.round(vector.getX()));
		this.setY(Math.round(vector.getY()));
	}

	public LTexture getImage() {
		return animation.getSpriteImage();
	}

	public int getWidth() {
		LTexture si = animation.getSpriteImage();
		if (si == null) {
			return -1;
		}
		return si.getWidth();
	}

	public int getHeight() {
		LTexture si = animation.getSpriteImage();
		if (si == null) {
			return -1;
		}
		return si.getHeight();
	}

	/**
	 * ?????????
	 * 
	 * @return
	 */
	public Point getMiddlePoint() {
		return new Point(getLocation().x() + getWidth() / 2, getLocation().y()
				+ getHeight() / 2);
	}

	/**
	 * ???????????
	 * 
	 * @param second
	 * @return
	 */
	public float getDistance(Sprite second) {
		return (float) this.getMiddlePoint()
				.distanceTo(second.getMiddlePoint());
	}

	/**
	 * ?????
	 * 
	 * @return
	 */
	public RectBox getCollisionBox() {
		return getRect(getLocation().x(), getLocation().y(),
					getWidth(), getHeight());
	}

	/**
	 * ??????????????????
	 * 
	 * @param sprite
	 * @return
	 */
	public boolean isRectToRect(Sprite sprite) {
		return CollisionHelper.isRectToRect(this.getCollisionBox(), sprite
				.getCollisionBox());
	}

	/**
	 * ??????????????????
	 * 
	 * @param sprite
	 * @return
	 */
	public boolean isCircToCirc(Sprite sprite) {
		return CollisionHelper.isCircToCirc(this.getCollisionBox(), sprite
				.getCollisionBox());
	}

	/**
	 * ?????????????????????
	 * 
	 * @param sprite
	 * @return
	 */
	public boolean isRectToCirc(Sprite sprite) {
		return CollisionHelper.isRectToCirc(this.getCollisionBox(), sprite
				.getCollisionBox());
	}

	final class Filter {

		LTexture tmpFilter;

		int type;
	}

	private Filter filter;

	private GLColor filterColor;

	public void createUI(GLEx g) {
		if (!visible) {
			return;
		}
		image = animation.getSpriteImage();
		if (image == null) {
			return;
		}
		float width = (image.getWidth() * scaleX);
		float height = (image.getHeight() * scaleY);
		if (filterColor == null) {
			if (alpha > 0 && alpha < 1) {
				g.setAlpha(alpha);
			}
			if (LTrans.TRANS_NONE == transform) {
				g.drawTexture(image, x(), y(), width, height, rotation);
			} else {
				g.drawRegion(image, 0, 0, getWidth(), getHeight(), transform,
						x(), y(), LGraphics.TOP | LGraphics.LEFT);
			}
			if (alpha > 0 && alpha < 1) {
				g.setAlpha(1);
			}
			return;
		} else {
			GLColor old = g.getColor();
			if (alpha > 0 && alpha < 1) {
				g.setAlpha(alpha);
			}
			g.setColor(filterColor);
			if (LTrans.TRANS_NONE == transform) {
				g.drawTexture(image, x(), y(), width, height, rotation);
			} else {
				g.drawRegion(image, 0, 0, getWidth(), getHeight(), transform,
						x(), y(), LGraphics.TOP | LGraphics.LEFT);
			}
			if (!old.equals(filterColor)) {
				g.setColor(old);
			}
			if (alpha > 0 && alpha < 1) {
				g.setAlpha(1);
			}
			return;
		}
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public String getSpriteName() {
		return spriteName;
	}

	public void setSpriteName(String spriteName) {
		this.spriteName = spriteName;
	}

	public int getTransform() {
		return transform;
	}

	public void setTransform(int transform) {
		this.transform = transform;
	}

	public GLColor getFilterColor() {
		return filterColor;
	}

	public void setFilterColor(GLColor filterColor) {
		this.filterColor = filterColor;
	}

	public void setAlpha(float a) {
		this.alpha = a;
	}

	public float getAlpha() {
		return alpha;
	}

	public LTexture getBitmap() {
		return this.image;
	}

	public float getScaleX() {
		return scaleX;
	}

	public void setScaleX(float scaleX) {
		this.scaleX = scaleX;
	}

	public float getScaleY() {
		return scaleY;
	}

	public void setScaleY(float scaleY) {
		this.scaleY = scaleY;
	}

	public float getRotation() {
		return this.rotation;
	}

	public void setRotation(float r) {
		this.rotation = r;
	}

	public void dispose() {
		this.visible = false;
		if (image != null) {
			image.dispose();
			image = null;
		}
		if (animation != null) {
			animation.dispose();
			animation = null;
		}
		if (filter != null) {
			if (filter.tmpFilter != null) {
				filter.tmpFilter.dispose();
				filter.tmpFilter = null;
			}
		}
	}
}

