package org.loon.framework.javase.game.action.sprite;

import java.awt.Image;
import java.awt.Polygon;
import java.awt.geom.Point2D;

import org.loon.framework.javase.game.core.LObject;
import org.loon.framework.javase.game.core.LSystem;
import org.loon.framework.javase.game.core.geom.RectBox;
import org.loon.framework.javase.game.core.geom.Vector2D;
import org.loon.framework.javase.game.core.graphics.LImage;
import org.loon.framework.javase.game.core.graphics.device.LGraphics;
import org.loon.framework.javase.game.core.graphics.device.LTrans;
import org.loon.framework.javase.game.core.graphics.filter.ImageFilterFactory;
import org.loon.framework.javase.game.core.graphics.filter.ImageFilterType;
import org.loon.framework.javase.game.utils.CollisionUtils;
import org.loon.framework.javase.game.utils.GraphicsUtils;

/**
 * Copyright 2008 - 2010
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
 * @version 0.1.2
 */
public class Sprite extends LObject implements ISprite, Collidable, LTrans {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1982110847888726016L;

	// ????????
	final static private long defaultTimer = 150;

	// ??????
	private int filterType;

	// ?????
	private ImageFilterFactory factory;

	// ????
	private boolean visible;

	// ????
	private String spriteName;

	// ????
	private SpriteImage image;

	// ??
	private Animation animation = new Animation();

	private int transform;

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
	public Sprite(double x, double y) {
		this("Sprite" + System.currentTimeMillis(), x, y);
	}

	/**
	 * ??????? ???,??x,??y
	 * 
	 * @param spriteName
	 * @param x
	 * @param y
	 */
	private Sprite(String spriteName, double x, double y) {
		this.setLocation(x, y);
		this.spriteName = spriteName;
		this.visible = true;
		this.filterType = ImageFilterFactory.NoneFilter;
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
	public Sprite(String fileName, double x, double y, int row, int col) {
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
	private Sprite(String fileName, double x, double y, int row, int col,
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
	public Sprite(String fileName, int maxFrame, double x, double y, int row,
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
	public Sprite(String fileName, int maxFrame, double x, double y, int row,
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
	public Sprite(String spriteName, String fileName, int maxFrame, double x,
			double y, int row, int col, long timer) {
		this(spriteName, GraphicsUtils
				.getSplitImages(fileName, row, col, false), maxFrame, x, y,
				timer);
	}

	/**
	 * ??????
	 * 
	 * @param fileName
	 */
	public Sprite(String fileName) {
		this(GraphicsUtils.loadImage(fileName));
	}

	/**
	 * ??????
	 * 
	 * @param images
	 */
	public Sprite(final LImage img) {
		this(new Image[] { img.getBufferedImage() }, 0, 0);
	}

	/**
	 * ??????
	 * 
	 * @param images
	 */
	public Sprite(final Image img) {
		this(new Image[] { img }, 0, 0);
	}

	/**
	 * ??????? ????
	 * 
	 * @param images
	 */
	public Sprite(Image[] images) {
		this(images, 0, 0);
	}

	/**
	 * ??????? ????,??x,??y
	 * 
	 * @param images
	 * @param x
	 * @param y
	 */
	public Sprite(Image[] images, double x, double y) {
		this(images, x, y, defaultTimer);
	}

	/**
	 * ??????? ????,????????
	 * 
	 * @param images
	 * @param timer
	 */
	public Sprite(Image[] images, long timer) {
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
	public Sprite(Image[] images, double x, double y, long timer) {
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
	public Sprite(Image[] images, int maxFrame, double x, double y, long timer) {
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
	public Sprite(String spriteName, Image[] images, int maxFrame, double x,
			double y, long timer) {
		this.setLocation(x, y);
		this.spriteName = spriteName;
		this.setAnimation(animation, images, maxFrame, timer);
		this.visible = true;
		this.filterType = ImageFilterFactory.NoneFilter;
		this.transform = LTrans.TRANS_NONE;
		this.factory = ImageFilterFactory.getInstance();
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
		if (newX + sprite.getWidth() >= LSystem.screenRect
				.getWidth()) {
			return (LSystem.screenRect.getWidth()
					- sprite.getWidth() - 1);
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
		if (newY + sprite.getHeight() >= LSystem.screenRect
				.getHeight()) {
			return (LSystem.screenRect.getHeight()
					- sprite.getHeight() - 1);
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
	private void setAnimation(Animation myAnimation, Image[] images,
			int maxFrame, long timer) {
		if (maxFrame != -1) {
			for (int i = 0; i < maxFrame; i++) {
				myAnimation.addFrame(new SpriteImage(images[i]), timer);
			}
		} else {
			for (int i = 0; i < images.length; i++) {
				myAnimation.addFrame(new SpriteImage(images[i]), timer);
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
		setAnimation(new Animation(), GraphicsUtils.getSplitImages(fileName,
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
	public void setAnimation(Image[] images, int maxFrame, long timer) {
		setAnimation(new Animation(), images, maxFrame, timer);
	}

	/**
	 * ??????
	 * 
	 * @param images
	 * @param timer
	 */
	public void setAnimation(Image[] images, long timer) {
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
	public void updateLocation(Vector2D vector) {
		this.setX(Math.round(vector.getX()));
		this.setY(Math.round(vector.getY()));
	}

	public LImage getBitmap() {
		return new LImage(animation.getSpriteImage().getImage());
	}

	public SpriteImage getImage() {
		return animation.getSpriteImage();
	}

	public SpriteImage getImage(int index) {
		return animation.getSpriteImage(index);
	}

	/**
	 * ?????????Polygon
	 * 
	 * @return
	 */
	public Polygon getPolygon() {
		SpriteImage si = animation.getSpriteImage();
		if (si == null) {
			return new Polygon();
		}
		return si.getPolygon(x(), y(), transform);
	}

	/**
	 * ????Polygon???????
	 * 
	 * @param i
	 */
	public void setPolygonInterval(int i) {
		SpriteImage si = animation.getSpriteImage();
		if (si == null) {
			return;
		}
		si.setMakePolygonInterval(i);
	}

	public int getWidth() {
		SpriteImage si = animation.getSpriteImage();
		if (si == null) {
			return -1;
		}
		return si.getWidth();
	}

	public int getHeight() {
		SpriteImage si = animation.getSpriteImage();
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
	public Point2D.Float getMiddlePoint() {
		return new Point2D.Float(getLocation().x() + getWidth() / 2,
				getLocation().y() + getHeight() / 2);
	}

	/**
	 * ???????????
	 * 
	 * @param second
	 * @return
	 */
	public double getDistance(Sprite second) {
		return this.getMiddlePoint().distance(second.getMiddlePoint());
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
		return CollisionUtils.isRectToRect(this.getCollisionBox(), sprite
				.getCollisionBox());
	}

	/**
	 * ??????????????????
	 * 
	 * @param sprite
	 * @return
	 */
	public boolean isCircToCirc(Sprite sprite) {
		return CollisionUtils.isCircToCirc(this.getCollisionBox(), sprite
				.getCollisionBox());
	}

	/**
	 * ?????????????????????
	 * 
	 * @param sprite
	 * @return
	 */
	public boolean isRectToCirc(Sprite sprite) {
		return CollisionUtils.isRectToCirc(this.getCollisionBox(), sprite
				.getCollisionBox());
	}

	/**
	 * ?????????????????
	 * 
	 * @param sprite
	 * @return
	 */
	public boolean isPixelCollision(Sprite sprite) {
		return CollisionUtils.isPixelHit(this, sprite);
	}

	/**
	 * ???????????????
	 */
	public CollisionMask getMask() {
		SpriteImage si = animation.getSpriteImage();
		if (si == null) {
			return null;
		}
		return si.getMask(transform, x(), y());
	}

	public void createUI(LGraphics g) {
		if (!visible) {
			return;
		}
		image = animation.getSpriteImage();
		if (image == null) {
			return;
		}

		switch (filterType) {
		case ImageFilterType.NoneFilter:
			if (LTrans.TRANS_NONE == transform) {
				g.drawImage(image.serializablelImage.getImage(), x(), y());
			} else {
				g.drawRegion(image.serializablelImage.getImage(), 0, 0,
						getWidth(), getHeight(), transform, x(), y(),
						LGraphics.TOP | LGraphics.LEFT);
			}
			return;
		default:
			Image tmp = factory.doFilter(image.serializablelImage.getImage(),
					filterType);
			if (LTrans.TRANS_NONE == transform) {
				g.drawImage(tmp, x(), y());
			} else {
				g.drawRegion(tmp, 0, 0, getWidth(), getHeight(), transform,
						x(), y(), LGraphics.TOP | LGraphics.LEFT);
			}
			tmp = null;
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

	public int getFilterType() {
		return filterType;
	}

	public void setFilterType(int filterType) {
		this.filterType = filterType;
	}

	public float getAlpha() {
		return this.animation.getAlpha();
	}

	public void setAlpha(float alpha) {
		this.animation.setAlpha(alpha);
	}

	public int getTransform() {
		return transform;
	}

	public void setTransform(int transform) {
		this.transform = transform;
	}

	public void dispose() {
		if (image != null) {
			image.dispose();
			image = null;
		}
		if (animation != null) {
			animation.dispose();
			animation = null;
		}
	}

}

