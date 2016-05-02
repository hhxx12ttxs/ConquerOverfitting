package org.loon.framework.android.game.core.graphics.opengl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.opengles.GL11Ext;

import org.loon.framework.android.game.core.LSystem;
import org.loon.framework.android.game.core.geom.Polygon;
import org.loon.framework.android.game.core.geom.RectBox;
import org.loon.framework.android.game.core.geom.Shape;
import org.loon.framework.android.game.core.geom.Triangle;
import org.loon.framework.android.game.core.geom.Triangle2f;
import org.loon.framework.android.game.core.geom.Vector2f;
import org.loon.framework.android.game.core.graphics.LFont;
import org.loon.framework.android.game.core.graphics.LImage;
import org.loon.framework.android.game.core.graphics.LPixmap;
import org.loon.framework.android.game.core.graphics.device.LGraphics;
import org.loon.framework.android.game.core.graphics.device.LTrans;
import org.loon.framework.android.game.core.graphics.opengl.LTexture.Format;
import org.loon.framework.android.game.utils.BufferUtils;
import org.loon.framework.android.game.utils.GLUtils;
import org.loon.framework.android.game.utils.MathUtils;
import org.loon.framework.android.game.utils.ScreenUtils;
import org.loon.framework.android.game.utils.StringUtils;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * 
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
public final class GLEx implements LTrans {

	/**
	 * ????????(?J2ME??)
	 * 
	 * @param sx
	 * @param sy
	 * @param width
	 * @param height
	 * @param dx
	 * @param dy
	 * @param anchor
	 */
	public void copyArea(int sx, int sy, int width, int height, int dx, int dy,
			int anchor) {
		if (width <= 0 || height <= 0) {
			return;
		}
		boolean badAnchor = false;
		if ((anchor & 0x7f) != anchor || (anchor & BASELINE) != 0) {
			badAnchor = true;
		}
		if ((anchor & TOP) != 0) {
			if ((anchor & (VCENTER | BOTTOM)) != 0)
				badAnchor = true;
		} else if ((anchor & BOTTOM) != 0) {
			if ((anchor & VCENTER) != 0)
				badAnchor = true;
			else {
				dy -= height - 1;
			}
		} else if ((anchor & VCENTER) != 0) {
			dy -= (height - 1) >>> 1;
		} else {
			badAnchor = true;
		}
		if ((anchor & LEFT) != 0) {
			if ((anchor & (HCENTER | RIGHT)) != 0)
				badAnchor = true;
		} else if ((anchor & RIGHT) != 0) {
			if ((anchor & HCENTER) != 0)
				badAnchor = true;
			else {
				dx -= width;
			}
		} else if ((anchor & HCENTER) != 0) {
			dx -= (width - 1) >>> 1;
		} else {
			badAnchor = true;
		}
		if (badAnchor) {
			throw new IllegalArgumentException("Bad Anchor !");
		}
		copyArea(sx, sy, width, height, dx - sx, dy - sy);
	}

	/**
	 * ????????
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param dx
	 * @param dy
	 */
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		copyArea(null, x, y, width, height, dx, dy);
	}

	/**
	 * ????????
	 * 
	 * @param texture
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param dx
	 * @param dy
	 */
	public void copyArea(LTexture texture, int x, int y, int width, int height,
			int dx, int dy) {
		if (isClose) {
			return;
		}
		if (x < 0) {
			width += x;
			x = 0;
		}
		if (y < 0) {
			height += y;
			y = 0;
		}
		if (texture != null) {
			if (x + width > texture.getWidth()) {
				width = texture.getWidth() - x;
			}
			if (y + height > texture.getHeight()) {
				height = texture.getHeight() - y;
			}
			LTexture tex2d = texture.getSubTexture(x, y, width, height);
			drawTexture(tex2d, x + dx, y + dy);
			if (GLEx.isVbo()) {
				deleteBuffer(tex2d.bufferID);
			}
			tex2d = null;
		} else {
			if (x + width > getWidth()) {
				width = getWidth() - x;
			}
			if (y + height > getHeight()) {
				height = getHeight() - y;
			}
			LTexture tex2d = ScreenUtils.toScreenCaptureTexture(x, y, width,
					height);
			drawTexture(tex2d, x + dx, y + dy);
			if (tex2d != null) {
				tex2d.destroy();
				tex2d = null;
			}
		}
	}

	/**
	 * ??????
	 * 
	 * @param texture
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void draw(LTexture texture, float x, float y, float width,
			float height, boolean use) {
		if (texture == null) {
			return;
		}
		draw(texture, null, x, y, width, height, 0, 0, texture.getWidth(),
				texture.getHeight(), use);
	}

	/**
	 * ??????
	 * 
	 * @param texture
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param srcX
	 * @param srcY
	 * @param srcWidth
	 * @param srcHeight
	 */
	public void draw(LTexture texture, float x, float y, float width,
			float height, float srcX, float srcY, float srcWidth,
			float srcHeight, boolean use) {
		if (texture == null) {
			return;
		}
		draw(texture, null, x, y, width, height, srcX, srcY, srcWidth,
				srcHeight, use);
	}

	/**
	 * ????????????
	 * 
	 * @param texture
	 * @param colors
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void draw(LTexture texture, GLColor[] colors, float x, float y,
			float width, float height, boolean use) {
		if (texture == null) {
			return;
		}
		draw(texture, colors, x, y, width, height, 0, 0, texture.getWidth(),
				texture.getHeight(), use);
	}

	/**
	 * ????????????
	 * 
	 * @param texture
	 * @param colors
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param srcX
	 * @param srcY
	 * @param srcWidth
	 * @param srcHeight
	 */
	public void draw(LTexture texture, GLColor[] colors, float x, float y,
			float width, float height, float srcX, float srcY, float srcWidth,
			float srcHeight, boolean use) {
		if (!texture.isLoaded) {
			texture.loadTexture();
		}
		glTex2DEnable();
		{
			bind(texture.textureID);
			if (use) {
				glBegin(GL.GL_TRIANGLE_STRIP, false);
			}
			float xOff = (((float) srcX / texture.width) * texture.widthRatio)
					+ texture.xOff;
			float yOff = (((float) srcY / texture.height) * texture.heightRatio)
					+ texture.yOff;
			float widthRatio = (((float) srcWidth / texture.width) * texture.widthRatio);
			float heightRatio = (((float) srcHeight / texture.height) * texture.heightRatio);
			if (colors == null) {
				glVertex3f(x, y, 0);
				glTexCoord2f(xOff, yOff);
				glVertex3f(x + width, y, 0);
				glTexCoord2f(widthRatio, yOff);
				glVertex3f(x, y + height, 0);
				glTexCoord2f(xOff, heightRatio);
				glVertex3f(x + width, y + height, 0);
				glTexCoord2f(widthRatio, heightRatio);
			} else {
				glColor4ES(colors[LTexture.TOP_LEFT]);
				glVertex3f(x, y, 0);
				glTexCoord2f(xOff, yOff);
				glColor4ES(colors[LTexture.TOP_RIGHT]);
				glVertex3f(x + width, y, 0);
				glTexCoord2f(widthRatio, yOff);
				glColor4ES(colors[LTexture.BOTTOM_LEFT]);
				glVertex3f(x, y + height, 0);
				glTexCoord2f(xOff, heightRatio);
				glColor4ES(colors[LTexture.BOTTOM_RIGHT]);
				glVertex3f(x + width, y + height, 0);
				glTexCoord2f(widthRatio, heightRatio);
			}
			if (use) {
				glEnd();
			}
		}
	}

	/**
	 * ??????????????????
	 * 
	 * @param texture
	 * @param pix
	 */
	public void copyPixelsToTexture(LTexture texture, LPixmap pix) {
		copyPixelsToTexture(texture, pix, false, true);
	}

	/**
	 * ??????????????????
	 * 
	 * @param texture
	 * @param pix
	 * @param remove
	 * @param check
	 */
	public void copyPixelsToTexture(LTexture texture, LPixmap pix,
			boolean remove, boolean check) {
		copyPixelsToTexture(texture, pix, remove, check);
	}

	/**
	 * ??????????????????
	 * 
	 * @param texture
	 * @param pix
	 * @param remove
	 */
	public void copyPixelsToTexture(LTexture texture, LPixmap pix, Bitmap temp,
			boolean remove, boolean check) {
		int hashCode = 0;
		if (check) {
			synchronized (LTextures.copyToTextures) {
				if (LTextures.copyToTextures.size() > LSystem.DEFAULT_MAX_CACHE_SIZE) {
					LTextures.copyToTextures.clear();
					LSystem.gc();
				}
				int[] pixels = pix.getData();
				hashCode = pixels.hashCode();
				hashCode = LSystem.unite(hashCode, texture.textureID);
				hashCode = LSystem.unite(hashCode, pix.getTexWidth());
				hashCode = LSystem.unite(hashCode, pix.getTexHeight());
				hashCode = LSystem.unite(hashCode, pixels[0]);
				hashCode = LSystem.unite(hashCode, pixels[pixels.length - 1]);
				if (remove) {
					LTextures.copyToTextures.remove(hashCode);
				} else if (LTextures.copyToTextures.contains(hashCode)) {
					return;
				}
			}
		}
		if (!texture.isLoaded) {
			texture.loadTexture();
		}
		glTex2DEnable();
		{
			bind(texture.textureID);
			gl10.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, pix.hasAlpha() ? 4 : 1);
			if (temp != null) {
				temp.setPixels(pix.getData(), 0, pix.getTexWidth(), 0, 0, pix
						.getTexWidth(), pix.getTexHeight());
				android.opengl.GLUtils.texSubImage2D(GL10.GL_TEXTURE_2D, 0, 0,
						0, temp);
			} else {
				gl10.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, pix
						.getTexWidth(), pix.getTexHeight(),
						pix.hasAlpha() ? GL.GL_RGBA : GL.GL_RGB,
						GL.GL_UNSIGNED_BYTE, pix.getPixels());
			}
		}
		if (check) {
			synchronized (LTextures.copyToTextures) {
				LTextures.copyToTextures.add(hashCode);
			}
		}
	}

	/**
	 * ??????????????????
	 * 
	 * @param texture
	 * @param pix
	 */
	public void copyImageToTexture(LTexture texture, LImage pix) {
		copyImageToTexture(texture, pix, false, true);
	}

	/**
	 * ??????????????????
	 * 
	 * @param texture
	 * @param pix
	 * @param x
	 * @param y
	 */
	public void copyImageToTexture(LTexture texture, LImage pix, int x, int y) {
		copyImageToTexture(texture, pix, x, y, false, true);
	}

	/**
	 * ??????????????????
	 * 
	 * @param texture
	 * @param pix
	 * @param remove
	 * @param check
	 */
	public void copyImageToTexture(LTexture texture, LImage pix,
			boolean remove, boolean check) {
		copyImageToTexture(texture, pix, 0, 0, remove, check);
	}

	/**
	 * ??????????????????
	 * 
	 * @param texture
	 * @param pix
	 * @param x
	 * @param y
	 * @param remove
	 * @param check
	 */
	public void copyImageToTexture(LTexture texture, LImage pix, int x, int y,
			boolean remove, boolean check) {
		int hashCode = 0;
		if (check) {
			synchronized (LTextures.copyToTextures) {
				if (LTextures.copyToTextures.size() > LSystem.DEFAULT_MAX_CACHE_SIZE) {
					LTextures.copyToTextures.clear();
					LSystem.gc();
				}
				hashCode = pix.hashCode();
				hashCode = LSystem.unite(hashCode, texture.textureID);
				hashCode = LSystem.unite(hashCode, x);
				hashCode = LSystem.unite(hashCode, y);
				hashCode = LSystem.unite(hashCode, pix.getWidth());
				hashCode = LSystem.unite(hashCode, pix.getHeight());
				if (remove) {
					LTextures.copyToTextures.remove(hashCode);
				} else if (LTextures.copyToTextures.contains(hashCode)) {
					return;
				}
			}
		}
		if (!texture.isLoaded) {
			texture.loadTexture();
		}
		glTex2DEnable();
		{
			bind(texture.textureID);
			gl10.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, pix.hasAlpha() ? 4 : 1);
			android.opengl.GLUtils.texSubImage2D(GL10.GL_TEXTURE_2D, 0, x, y,
					pix.getBitmap());
		}
		if (check) {
			synchronized (LTextures.copyToTextures) {
				LTextures.copyToTextures.add(hashCode);
			}
		}
	}

	// ----- ????????? ------//

	public void beginBatch() {
		this.glTex2DEnable();
		this.glTex2DARRAYEnable();
	}

	public void drawBatch(LImage image, float x, float y) {
		if (image == null) {
			return;
		}
		drawBatch(image.getTexture(), x, y);
	}

	public void drawBatch(LTexture texture, float x, float y) {
		if (texture == null || isClose) {
			return;
		}
		if (!texture.isLoaded) {
			texture.loadTexture();
		}
		bind(texture.textureID);
		{
			if (x != 0 || y != 0) {
				gl10.glTranslatef(x, y, 0);
			}
			if (vboOn) {
				gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, texture.bufferID);
				gl11.glVertexPointer(2, GL11.GL_FLOAT, 0, 0);
				gl11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, texture.texSize);
				gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
				gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
			} else {
				texture.data.position(0);
				GLUtils.vertexPointer(gl10, 2, texture.data);
				texture.data.position(8);
				gl10.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texture.data);
				gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
			}
			if (x != 0 || y != 0) {
				gl10.glTranslatef(-x, -y, 0);
			}
		}
	}

	public void drawBatch(LTexture texture, float x, float y, GLColor c) {
		if (texture == null || isClose) {
			return;
		}
		if (!texture.isLoaded) {
			texture.loadTexture();
		}
		bind(texture.textureID);
		{
			boolean flag = !color.equals(c);
			if (x != 0 || y != 0) {
				gl10.glTranslatef(x, y, 0);
			}
			if (flag) {
				gl10.glColor4f(c.r, c.g, c.b, c.a);
			}
			if (vboOn) {
				gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, texture.bufferID);
				gl11.glVertexPointer(2, GL11.GL_FLOAT, 0, 0);
				gl11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, texture.texSize);
				gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
				gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
			} else {
				texture.data.position(0);
				GLUtils.vertexPointer(gl10, 2, texture.data);
				texture.data.position(8);
				gl10.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texture.data);
				gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
			}
			if (flag) {
				gl10.glColor4f(color.r, color.g, color.b, color.a);
			}
			if (x != 0 || y != 0) {
				gl10.glTranslatef(-x, -y, 0);
			}
		}
	}

	public void drawBatch(LImage image, float x, float y, float width,
			float height, float rotation, GLColor color) {
		if (image == null) {
			return;
		}
		drawBatch(image.getTexture(), x, y, width, height, rotation, color);
	}

	public void drawBatch(LTexture texture, float x, float y, float width,
			float height, float rotation, GLColor c) {
		if (texture == null || isClose) {
			return;
		}
		if (!texture.isLoaded) {
			texture.loadTexture();
		}
		bind(texture.textureID);
		{
			boolean flag = !color.equals(c);
			gl10.glPushMatrix();
			gl10.glTranslatef(x, y, 0);
			if (rotation != 0) {
				float centerX = width / 2;
				float centerY = height / 2;
				gl10.glTranslatef(centerX, centerY, 0.0f);
				gl10.glRotatef(rotation, 0.0f, 0.0f, 1.0f);
				gl10.glTranslatef(-centerX, -centerY, 0.0f);
			}
			if (width != texture.width || height != texture.height) {
				float sx = width / texture.width;
				float sy = height / texture.height;
				try {
					gl10.glScalef(sx, sy, 1);
				} catch (Exception e) {
					gl10.glScalef(sx, sy, 0);
				}
			}
			if (flag) {
				gl10.glColor4f(c.r, c.g, c.b, c.a);
			}
			if (vboOn) {
				gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, texture.bufferID);
				gl11.glVertexPointer(2, GL11.GL_FLOAT, 0, 0);
				gl11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, texture.texSize);
				gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
				gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
			} else {
				texture.data.position(0);
				GLUtils.vertexPointer(gl10, 2, texture.data);
				texture.data.position(8);
				gl10.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texture.data);
				gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
			}
			if (flag) {
				gl10.glColor4f(color.r, color.g, color.b, color.a);
			}
			gl10.glPopMatrix();

		}
	}

	public void drawBatch(LImage image, float x, float y, float width,
			float height, float srcX, float srcY, float srcWidth,
			float srcHeight) {
		if (image == null) {
			return;
		}
		drawBatch(image.getTexture(), x, y, width, height, srcX, srcY,
				srcWidth, srcHeight);
	}

	public void drawBatch(LTexture texture, float x, float y, float width,
			float height, float srcX, float srcY, float srcWidth,
			float srcHeight) {
		if (texture == null || isClose) {
			return;
		}
		if (!texture.isLoaded) {
			texture.loadTexture();
		}

		if (oesOn) {
			bind(texture.textureID);

			if (texture.parent != null) {
				gl11.glTexParameteriv(GL10.GL_TEXTURE_2D,
						GL11Ext.GL_TEXTURE_CROP_RECT_OES, texture.crops, 0);
			}
			float swidth = srcWidth - srcX;
			float sheight = srcHeight - srcY;

			crop[0] = srcX;
			crop[1] = (sheight + srcY);
			crop[2] = swidth;
			crop[3] = -sheight;
			gl11ex.glTexParameterfv(GL10.GL_TEXTURE_2D,
					GL11Ext.GL_TEXTURE_CROP_RECT_OES, crop, 0);
			if (LSystem.scaleWidth == 1 && LSystem.scaleHeight == 1) {
				gl11ex.glDrawTexfOES(x, (viewPort.height - height - y), 0,
						width, height);
			} else {
				gl11ex.glDrawTexfOES(x * LSystem.scaleWidth, (viewPort.height
						- height - y)
						* LSystem.scaleHeight, 0, width * LSystem.scaleWidth,
						height * LSystem.scaleHeight);
			}

		} else {

			boolean save = width != texture.width || height != texture.height;
			bind(texture.textureID);
			{
				if (x != 0 || y != 0) {
					gl10.glTranslatef(x, y, 0);
				}
				if (save) {
					gl10.glPushMatrix();
					float sx = width / texture.width;
					float sy = height / texture.height;
					try {
						gl10.glScalef(sx, sy, 1);
					} catch (Exception e) {
						gl10.glScalef(sx, sy, 0);
					}
				}
				if (GLEx.vboOn) {
					gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, texture.bufferID);
					gl11.glVertexPointer(2, GL10.GL_FLOAT, 0, 0);
					if (srcX != 0 || srcY != 0 || srcWidth != texture.width
							|| srcHeight != texture.height) {
						gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, glDataBufferID);
						putRectangle(texture, srcX, srcY, srcWidth, srcHeight);
						rectData.position(8);
						gl11.glBufferSubData(GL11.GL_ARRAY_BUFFER,
								texture.vertexSize, texture.texSize, rectData);
						gl11.glTexCoordPointer(2, GL11.GL_FLOAT, 0,
								texture.texSize);
					} else {
						gl11.glTexCoordPointer(2, GL11.GL_FLOAT, 0,
								texture.texSize);
					}
					gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
					gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
				} else {
					texture.data.position(0);
					GLUtils.vertexPointer(gl10, 2, texture.data);
					if (srcX != 0 || srcY != 0 || srcWidth != texture.width
							|| srcHeight != texture.height) {
						putRectangle(texture, srcX, srcY, srcWidth, srcHeight);
						rectData.position(8);
						gl10.glTexCoordPointer(2, GL10.GL_FLOAT, 0, rectData);
					} else {
						texture.data.position(8);
						gl10.glTexCoordPointer(2, GL10.GL_FLOAT, 0,
								texture.data);
					}
					gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
				}

			}
			if (save) {
				gl10.glPopMatrix();
			}
			if (x != 0 || y != 0) {
				gl10.glTranslatef(-x, -y, 0);
			}
		}
	}

	public void endBatch() {
		this.glTex2DARRAYDisable();
		this.glTex2DDisable();
	}

	// ----- ????????? ------//

	public static class Clip {

		public int x;

		public int y;

		public int width;

		public int height;

		public Clip(Clip clip) {
			this(clip.x, clip.y, clip.width, clip.height);
		}

		public Clip(int x, int y, int w, int h) {
			this.setBounds(x, y, w, h);
		}

		public void setBounds(int x, int y, int w, int h) {
			this.x = x;
			this.y = y;
			this.width = w;
			this.height = h;
		}

		public int getBottom() {
			return height;
		}

		public int getLeft() {
			return x;
		}

		public int getRight() {
			return width;
		}

		public int getTop() {
			return y;
		}

	}

	public static enum Direction {
		TRANS_NONE, TRANS_MIRROR, TRANS_FILP, TRANS_MF;
	}

	public static GLEx self;

	public static GLBase gl;

	public static GL10 gl10;

	public static GL11 gl11;

	private static GLU glu;

	private GL11Ext gl11ex;

	private final float[] crop = new float[4];

	private int currentBlendMode;

	private int prevBlendMode;

	private float lastAlpha = 1.0F, lineWidth, sx = 1, sy = 1;

	private boolean isClose, isTex2DEnabled, isARRAYEnable, isAntialias,
			isScissorTest, isPushed;

	private boolean isUpdateColor, isUpdateTexture;

	private GLColor boundColor;

	private final Clip clip;

	private int mode, drawingType = 0;

	private float translateX, translateY;

	private final RectBox viewPort;

	private static boolean isAlpha, isMask;

	private boolean preSmoot, preTex2d, preLight, preCull, preTex2dMode;

	private static boolean oesOn, vboOn, vboSupported;

	public static int lazyTextureID;

	boolean onAlpha, onReplace;

	static int verMajor, verMinor;

	private GLColor color = new GLColor(GLColor.white);

	private static final float[] rectDataCords = new float[16];

	private static final FloatBuffer rectData = BufferUtils
			.createFloatBuffer(rectDataCords.length);

	private static int glDataBufferID;

	private ByteBuffer readBuffer = BufferUtils.createByteBuffer(4);

	private ShortBuffer shortBuffer = BufferUtils.createShortBuffer(64);

	private static final int DEFAULT_MAX_VERTICES = 16384;

	private FloatBuffer floatVertices = BufferUtils
			.createFloatBuffer(DEFAULT_MAX_VERTICES);

	private FloatBuffer texCoordBuf;

	private FloatBuffer vertexBuf;

	private FloatBuffer colorBuf;

	private LFont font = LFont.getDefaultFont();

	private boolean onTexEnvf, onSaveFlag;

	private static javax.microedition.khronos.opengles.GL10 baseGL;

	private static boolean isPixelFlinger;

	public GLEx(javax.microedition.khronos.opengles.GL10 g10, int width,
			int height) {
		this.viewPort = new RectBox(0, 0, width, height);
		this.clip = new Clip(0, 0, viewPort.width, viewPort.height);
		this.isTex2DEnabled = false;
		this.isClose = false;
		if (g10 == null || baseGL == g10) {
			return;
		}
		String renderer = g10.glGetString(GL10.GL_RENDERER).toLowerCase();
		GLEx.isPixelFlinger = renderer.indexOf("pixelflinger") != -1;
		Log.i("Android2DView", "GLES:" + renderer);
		if (g10 instanceof javax.microedition.khronos.opengles.GL11) {
			String extensions = g10.glGetString(GL10.GL_EXTENSIONS)
					.toLowerCase();
			if (extensions.contains("gl_oes_framebuffer_object")) {
				oesOn = !LSystem.isEmulator();
				gl11ex = (GL11Ext) g10;
			}
			if (GLEx.isSupportGL11()) {
				GLEx.gl11 = new AndroidGL11(g10);
				GLEx.gl10 = gl11;
			} else {
				GLEx.gl10 = new AndroidGL10(g10);
				GLEx.gl11 = new AndroidGL11(g10);
				setVbo(false);
				setVBOSupported(false);
			}
		} else {
			GLEx.gl10 = new AndroidGL10(g10);
			setVBOSupported(false);
			setVbo(false);
		}
		GLEx.self = this;
		GLEx.gl = gl10;
		GLEx.baseGL = g10;
		GLEx.glu = new AndroidGLU();
	}

	public final static boolean isSupportGL11() {
		return !GLEx.isPixelFlinger
				&& !(android.os.Build.MODEL.equals("MB200")
						|| android.os.Build.MODEL.equals("MB220") || android.os.Build.MODEL
						.contains("Behold"));
	}

	public boolean equals(javax.microedition.khronos.opengles.GL10 g10, int w,
			int h) {
		return (g10 == null || baseGL == g10)
				&& (w == getWidth() && h == getHeight());
	}

	public int getWidth() {
		return (int) viewPort.getWidth();
	}

	public int getHeight() {
		return (int) viewPort.getHeight();
	}

	public final void enableSmooth() {
		if (isClose) {
			return;
		}
		if (!preSmoot) {
			gl.glEnable(GL10.GL_POINT_SMOOTH);
			gl.glEnable(GL10.GL_LINE_SMOOTH);
			preSmoot = true;
		}
	}

	public final void disableSmooth() {
		if (isClose) {
			return;
		}
		if (preSmoot) {
			gl.glDisable(GL10.GL_POINT_SMOOTH);
			gl.glDisable(GL10.GL_LINE_SMOOTH);
			preSmoot = false;
		}
	}

	public final void enableLighting() {
		if (isClose) {
			return;
		}
		if (!preLight) {
			gl10.glEnable(GL10.GL_LIGHTING);
			preLight = true;
		}
	}

	public final void disableLighting() {
		if (isClose) {
			return;
		}
		if (preLight) {
			gl10.glDisable(GL10.GL_LIGHTING);
			preLight = false;
		}
	}

	/**
	 * ????????
	 * 
	 */
	public final void update() {
		if (isClose) {
			return;
		}
		// ??????
		GLUtils.reset(gl10);
		// ???????
		GLUtils.setClearColor(gl10, GLColor.black);
		// ???????FASTEST(??,????)
		GLUtils.setHintFastest(gl10);
		// ??????FLAT
		GLUtils.setShadeModelFlat(gl10);
		// ??????
		GLUtils.disableLightning(gl10);
		// ??????
		GLUtils.disableDither(gl10);
		// ??????
		GLUtils.disableDepthTest(gl10);
		// ??????
		GLUtils.disableMultisample(gl10);
		// ??????
		GLUtils.disableCulling(gl10);
		// ??????
		GLUtils.disableVertexArray(gl10);
		// ??????
		GLUtils.disableTexCoordArray(gl10);
		// ??????
		GLUtils.disableTexColorArray(gl10);
		// ??????
		GLUtils.disableTextures(gl10);
		// ???????????
		this.setBlendMode(GL.MODE_NORMAL);
		this.prevBlendMode = currentBlendMode;
		// ??VBO???VBO??
		if (GLEx.vboOn) {
			try {
				glDataBufferID = createBufferID();
				bufferDataARR(glDataBufferID, rectData, GL11.GL_DYNAMIC_DRAW);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// ??2D????(??2D?????)
		set2DStateOn();
	}

	private boolean useBegin;

	/**
	 * ????OpenGL?glBegin(????????????)
	 * 
	 * @param mode
	 */
	public final void glBegin(int mode) {
		glBegin(mode, true);
	}

	/**
	 * ????OpenGL?glBegin(????????????)
	 * 
	 * @param mode
	 * @param n
	 */
	public final void glBegin(int mode, boolean d) {
		if (isClose) {
			return;
		}
		if (d) {
			this.glTex2DDisable();
		}
		this.mode = mode;
		if (vertexBuf == null) {
			vertexBuf = BufferUtils.createFloatBuffer(DEFAULT_MAX_VERTICES * 2);
		} else {
			vertexBuf.rewind();
			vertexBuf.limit(vertexBuf.capacity());
		}
		if (texCoordBuf == null) {
			texCoordBuf = BufferUtils
					.createFloatBuffer(DEFAULT_MAX_VERTICES * 3);
		} else {
			texCoordBuf.rewind();
			texCoordBuf.limit(texCoordBuf.capacity());
		}
		if (colorBuf == null) {
			colorBuf = BufferUtils.createFloatBuffer(DEFAULT_MAX_VERTICES * 4);
		} else {
			colorBuf.rewind();
			colorBuf.limit(colorBuf.capacity());
		}
		this.useBegin = true;
	}

	public FloatBuffer getCacheESVertexBuffer() {
		return BufferUtils.copyFloatBuffer(vertexBuf);
	}

	public FloatBuffer getCacheESCoordBuffer() {
		return BufferUtils.copyFloatBuffer(texCoordBuf);
	}

	public FloatBuffer getCacheESColorBuffer() {
		return BufferUtils.copyFloatBuffer(colorBuf);
	}

	/**
	 * ?????OpenGL???????????
	 * 
	 * @param x
	 * @param y
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 */
	public void putPixel4ES(float x, float y, float r, float g, float b, float a) {
		if (isClose || !useBegin) {
			return;
		}
		if (a <= 0 || (r == 0 && g == 0 && b == 0 && a == 0)) {
			return;
		}
		if ((x < 0 || y < 0) || (x > viewPort.width || y > viewPort.height)) {
			return;
		}
		this.glVertex2f(x, y);
		this.glColor4ES(r, g, b, a);
	}

	/**
	 * ?????OpenGL???????????
	 * 
	 * @param x
	 * @param y
	 * @param c
	 */
	public void putPixel4ES(float x, float y, GLColor c) {
		putPixel4ES(x, y, c.r, c.g, c.b, c.a);
	}

	/**
	 * ?????OpenGL???????????
	 * 
	 * @param x
	 * @param y
	 * @param r
	 * @param g
	 * @param b
	 */
	public void putPixel3ES(float x, float y, float r, float g, float b) {
		putPixel4ES(x, y, r, g, b, 1);
	}

	/**
	 * ??????
	 * 
	 * @param fcol
	 * @param frow
	 */
	public final void glTexCoord2f(float fcol, float frow) {
		if (isClose || !useBegin) {
			return;
		}
		texCoordBuf.put(fcol);
		texCoordBuf.put(frow);
	}

	/**
	 * ??????
	 * 
	 * @param x
	 * @param y
	 */
	public final void glVertex2f(float x, float y) {
		if (isClose || !useBegin) {
			return;
		}
		glVertex3f(x, y, 0);
	}

	/**
	 * ??????
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public final void glVertex3f(float x, float y, float z) {
		if (isClose || !useBegin) {
			return;
		}
		vertexBuf.put(x);
		vertexBuf.put(y);
		vertexBuf.put(z);
	}

	/**
	 * ????
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 */
	public void glColor4ES(float r, float g, float b, float a) {
		if (isClose || !useBegin) {
			return;
		}
		colorBuf.put(r);
		colorBuf.put(g);
		colorBuf.put(b);
		colorBuf.put(a);
	}

	/**
	 * ????
	 * 
	 * @param c
	 */
	public void glColor4ES(GLColor c) {
		if (isClose) {
			return;
		}
		glColor4ES(c.r, c.g, c.b, c.a);
	}

	/**
	 * ????
	 * 
	 * @param r
	 * @param g
	 * @param b
	 */
	public void glColor3ES(float r, float g, float b) {
		glColor4ES(r, g, b, 1);
	}

	/**
	 * ????
	 * 
	 * @param r
	 * @param g
	 * @param b
	 */
	public final void glColor3ub(byte r, byte g, byte b) {
		if (isClose) {
			return;
		}
		gl10.glColor4f((r & 255) / 255.0f, (g & 255) / 255.0f,
				(b & 255) / 255.0f, 1);
	}

	/**
	 * ????
	 * 
	 * @param r
	 * @param g
	 * @param b
	 */
	public final void glColor3f(float r, float g, float b) {
		if (isClose) {
			return;
		}
		gl10.glColor4f(r, g, b, 1);
	}

	/**
	 * ??????????????
	 * 
	 * @param size
	 * @param stride
	 * @param vertexArray
	 */
	public final void glVertexPointer(int size, int stride,
			FloatBuffer vertexArray) {
		if (isClose) {
			return;
		}
		gl10.glVertexPointer(size, GL10.GL_FLOAT, stride, vertexArray);
	}

	/**
	 * ??????????????
	 * 
	 * @param size
	 * @param type
	 * @param stride
	 * @param vertexArray
	 */
	public final void glVertexPointer(int size, int type, int stride,
			FloatBuffer vertexArray) {
		if (isClose) {
			return;
		}
		gl10.glVertexPointer(size, type, stride, vertexArray);
	}

	/**
	 * ??????????????
	 * 
	 * @param size
	 * @param type
	 * @param stride
	 * @param pointer
	 */
	public final void glVertexPointer(int size, int type, int stride,
			Buffer pointer) {
		if (isClose) {
			return;
		}
		gl10.glVertexPointer(size, type, stride, pointer);
	}

	/**
	 * ?????????????????
	 * 
	 * @param mode
	 * @param byteStride
	 * @param buf
	 */
	public final void glInterleavedArrays(int mode, int byteStride,
			FloatBuffer buf) {
		if (isClose) {
			return;
		}
		if (byteStride == 0) {
			byteStride = 5 * 4;
		}
		if (mode != GL.GL_T2F_V3F) {
			throw new RuntimeException("Unsupported interleaved array mode!");
		}
		int pos = buf.position();
		gl10.glTexCoordPointer(2, GL10.GL_FLOAT, byteStride, buf);
		buf.position(pos + 2);
		gl10.glVertexPointer(3, GL10.GL_FLOAT, byteStride, buf);
		buf.position(pos);
		gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}

	/**
	 * ???????????
	 * 
	 * @param buffer
	 */
	public final void glGetFloat(FloatBuffer buffer) {
		glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
	}

	/**
	 * ????????????????
	 * 
	 * @param name
	 * @param buffer
	 */
	public final void glGetFloat(int name, FloatBuffer buffer) {
		if (isClose) {
			return;
		}
		switch (name) {
		case GL11.GL_MODELVIEW_MATRIX:
			mode = GL11.GL_MODELVIEW_MATRIX_FLOAT_AS_INT_BITS_OES;
			break;
		default:
			throw new RuntimeException("Unsupported: " + name);
		}
		IntBuffer intBuffer = BufferUtils.createIntBuffer(16);
		gl.glGetIntegerv(name, intBuffer);
		int p = buffer.position();
		for (int i = 0; i < 16; i++) {
			buffer.put(Float.intBitsToFloat(intBuffer.get(i)));
		}
		buffer.position(p);
	}

	public void glTexEnvf(int target, int pname, float param) {
		gl10.glTexEnvf(target, pname, param);
	}

	public void glPointSizePointerOES(int type, int stride, Buffer pointer) {
		gl11.glPointSizePointerOES(type, stride, pointer);
	}

	/**
	 * ?????
	 * 
	 * @param size
	 * @param b
	 * @param stride
	 * @param colorAsByteBuffer
	 */
	public final void glColorPointer(int size, int type, int stride,
			ByteBuffer colorAsByteBuffer) {
		if (isClose) {
			return;
		}
		gl10.glColorPointer(size, type, stride, colorAsByteBuffer);
	}

	/**
	 * ?????
	 * 
	 * @param size
	 * @param type
	 * @param stride
	 * @param pointer
	 */
	public final void glColorPointer(int size, int type, int stride,
			Buffer pointer) {
		if (isClose) {
			return;
		}
		gl10.glColorPointer(size, type, stride, pointer);
	}

	/**
	 * ?????
	 * 
	 * @param size
	 * @param stride
	 * @param colorArrayBuf
	 */
	public final void glColorPointer(int size, int stride,
			FloatBuffer colorArrayBuf) {
		if (isClose) {
			return;
		}
		gl10.glColorPointer(size, GL10.GL_FLOAT, stride, colorArrayBuf);
	}

	/**
	 * ?????
	 * 
	 * @param size
	 * @param type
	 * @param stride
	 * @param colorArrayBuf
	 */
	public final void glColorPointer(int size, int type, int stride,
			FloatBuffer colorArrayBuf) {
		if (isClose) {
			return;
		}
		gl10.glColorPointer(size, type, stride, colorArrayBuf);
	}

	/**
	 * ?????????????????
	 * 
	 * @param mode
	 * @param srcIndexBuf
	 */
	public final void glDrawElements(int mode, IntBuffer srcIndexBuf) {
		if (isClose) {
			return;
		}
		int count = srcIndexBuf.remaining();
		if (count > srcIndexBuf.capacity()) {
			shortBuffer = BufferUtils.createShortBuffer(count);
		}
		for (int i = 0; i < count; i++) {
			shortBuffer.put(i, (short) srcIndexBuf.get());
		}
		gl10.glDrawElements(mode, count, GL10.GL_SHORT, shortBuffer);
	}

	/**
	 * ?????????????????
	 * 
	 * @param mode
	 * @param count
	 * @param type
	 * @param indices
	 */
	public final void glDrawElements(int mode, int count, int type,
			Buffer indices) {
		if (isClose) {
			return;
		}
		gl10.glDrawElements(mode, count, type, indices);
	}

	/**
	 * ????(????GL.GL_LINES)
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public void glLine(float x1, float y1, float x2, float y2) {
		$drawLine1(x1, y1, x2, y2, false);
	}

	/**
	 * ?????(????GL.GL_LINE_LOOP)
	 * 
	 * @param xPoints
	 * @param yPoints
	 * @param nPoints
	 */
	public void glDrawPoly(float[] xPoints, float[] yPoints, int nPoints) {
		$drawPolygon1(xPoints, yPoints, nPoints, false);
	}

	/**
	 * ?????(????GL.GL_LINE_LOOP)
	 * 
	 * @param xPoints
	 * @param yPoints
	 * @param nPoints
	 */
	public void glFillPoly(float[] xPoints, float[] yPoints, int nPoints) {
		$fillPolygon1(xPoints, yPoints, nPoints, false);
	}

	/**
	 * ???????glbegin??
	 * 
	 * @return
	 */
	public boolean useGLBegin() {
		return useBegin;
	}

	/**
	 * ????OpenGL?glEnd(??????????OpenGL)
	 * 
	 */
	public final void glEnd() {
		if (isClose || !useBegin) {
			return;
		}
		int count = vertexBuf.position() / 3;
		if (count < 1) {
			useBegin = false;
			return;
		}
		vertexBuf.flip();
		gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl10.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuf);
		if (colorBuf.position() > 0) {
			colorBuf.flip();
			if (!isUpdateColor) {
				gl10.glEnableClientState(GL10.GL_COLOR_ARRAY);
				isUpdateColor = true;
			}
			gl10.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuf);
		}
		if (texCoordBuf.position() > 0) {
			texCoordBuf.flip();
			if (!isUpdateTexture) {
				gl10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				isUpdateTexture = true;
			}
			gl10.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texCoordBuf);
		}
		switch (mode) {
		case GL.GL_QUADS:
			for (int i = 0; i < count; i += 4) {
				gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, i, 4);
			}
			break;
		case GL.GL_TRIANGLE_STRIP:
			for (int i = 0; i < count; i += 4) {
				gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, i, 4);
			}
			break;
		default:
			gl.glDrawArrays(mode, 0, count);
		}
		if (isUpdateTexture) {
			gl10.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			isUpdateTexture = false;
		}
		if (isUpdateColor) {
			gl10.glDisableClientState(GL10.GL_COLOR_ARRAY);
			if (color != null) {
				gl10.glColor4f(color.r, color.g, color.b, color.a);
			}
			isUpdateColor = false;
		}
		gl10.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		useBegin = false;
	}

	/**
	 * ??2D????(?????????)
	 * 
	 */
	public final void glTex2DDisable() {
		if (isClose) {
			return;
		}
		if (isTex2DEnabled) {
			gl.glDisable(GL.GL_TEXTURE_2D);
			isTex2DEnabled = false;
		}
	}

	/**
	 * ??2D????(????????)
	 * 
	 */
	public final void glTex2DEnable() {
		if (isClose) {
			return;
		}
		if (!isTex2DEnabled) {
			gl.glEnable(GL.GL_TEXTURE_2D);
			isTex2DEnabled = true;
		}
	}

	/**
	 * ????????
	 * 
	 */
	public final void glTex2DARRAYEnable() {
		if (isClose) {
			return;
		}
		if (!isARRAYEnable) {
			gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			isARRAYEnable = true;
		}
	}

	/**
	 * ????????
	 * 
	 */
	public final void glTex2DARRAYDisable() {
		if (isClose) {
			return;
		}
		if (isARRAYEnable) {
			gl10.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			gl10.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			isARRAYEnable = false;
		}
	}

	/**
	 * ??gluLookAt
	 * 
	 * @param gl
	 * @param eyeX
	 * @param eyeY
	 * @param eyeZ
	 * @param centerX
	 * @param centerY
	 * @param centerZ
	 * @param upX
	 * @param upY
	 * @param upZ
	 */
	public void gluLookAt(GL10 gl, float eyeX, float eyeY, float eyeZ,
			float centerX, float centerY, float centerZ, float upX, float upY,
			float upZ) {
		if (isClose) {
			return;
		}
		glu.gluLookAt(gl, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX,
				upY, upZ);
	}

	/**
	 * ??gluOrtho2D
	 * 
	 * @param gl
	 * @param left
	 * @param right
	 * @param bottom
	 * @param top
	 */
	public void gluOrtho2D(GL10 gl, float left, float right, float bottom,
			float top) {
		if (isClose) {
			return;
		}
		glu.gluOrtho2D(gl, left, right, bottom, top);
	}

	/**
	 * ??gluPerspective
	 * 
	 * @param gl
	 * @param fovy
	 * @param aspect
	 * @param zNear
	 * @param zFar
	 */
	public void gluPerspective(GL10 gl, float fovy, float aspect, float zNear,
			float zFar) {
		if (isClose) {
			return;
		}
		glu.gluPerspective(gl, fovy, aspect, zNear, zFar);
	}

	/**
	 * ??gluProject
	 * 
	 * @param objX
	 * @param objY
	 * @param objZ
	 * @param model
	 * @param modelOffset
	 * @param project
	 * @param projectOffset
	 * @param view
	 * @param viewOffset
	 * @param win
	 * @param winOffset
	 * @return
	 */
	public boolean gluProject(float objX, float objY, float objZ,
			float[] model, int modelOffset, float[] project, int projectOffset,
			int[] view, int viewOffset, float[] win, int winOffset) {
		if (isClose) {
			return false;
		}
		return glu.gluProject(objX, objY, objZ, model, modelOffset, project,
				projectOffset, view, viewOffset, win, winOffset);
	}

	/**
	 * ??gluUnProject
	 * 
	 * @param winX
	 * @param winY
	 * @param winZ
	 * @param model
	 * @param modelOffset
	 * @param project
	 * @param projectOffset
	 * @param view
	 * @param viewOffset
	 * @param obj
	 * @param objOffset
	 * @return
	 */
	public boolean gluUnProject(float winX, float winY, float winZ,
			float[] model, int modelOffset, float[] project, int projectOffset,
			int[] view, int viewOffset, float[] obj, int objOffset) {
		if (isClose) {
			return false;
		}
		return glu.gluUnProject(winX, winY, winZ, model, modelOffset, project,
				projectOffset, view, viewOffset, obj, objOffset);
	}

	/**
	 * ?????????????
	 * 
	 * @param mode
	 */
	public final void setBlendMode(int mode) {
		if (isClose) {
			return;
		}
		if (currentBlendMode == mode) {
			return;
		}
		this.currentBlendMode = mode;
		if (currentBlendMode == GL.MODE_NORMAL) {
			GLUtils.enableBlend(gl10);
			gl10.glColorMask(true, true, true, true);
			gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			return;
		} else if (currentBlendMode == GL.MODE_ALPHA_MAP) {
			GLUtils.disableBlend(gl10);
			gl10.glColorMask(false, false, false, true);
			return;
		} else if (currentBlendMode == GL.MODE_ALPHA_BLEND) {
			GLUtils.enableBlend(gl10);
			gl10.glColorMask(true, true, true, false);
			gl10.glBlendFunc(GL10.GL_DST_ALPHA, GL10.GL_ONE_MINUS_DST_ALPHA);
			return;
		} else if (currentBlendMode == GL.MODE_COLOR_MULTIPLY) {
			GLUtils.enableBlend(gl10);
			gl10.glColorMask(true, true, true, true);
			gl10.glBlendFunc(GL10.GL_ONE_MINUS_SRC_COLOR, GL10.GL_SRC_COLOR);
			return;
		} else if (currentBlendMode == GL.MODE_ADD) {
			GLUtils.enableBlend(gl10);
			gl10.glColorMask(true, true, true, true);
			gl10.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE);
			return;
		} else if (currentBlendMode == GL.MODE_SPEED) {
			GLUtils.enableBlend(gl10);
			gl10.glColorMask(true, true, true, false);
			gl10.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
			return;
		} else if (currentBlendMode == GL.MODE_SCREEN) {
			GLUtils.enableBlend(gl10);
			gl10.glColorMask(true, true, true, true);
			gl10.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_COLOR);
			return;
		} else if (currentBlendMode == GL.MODE_ALPHA_ONE) {
			GLUtils.enableBlend(gl10);
			gl10.glColorMask(true, true, true, true);
			gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
			return;
		} else if (currentBlendMode == GL.MODE_ALPHA) {
			GLUtils.enableBlend(gl10);
			gl10.glColorMask(true, true, true, false);
			gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			return;
		} else if (currentBlendMode == GL.MODE_NONE) {
			GLUtils.disableBlend(gl10);
			gl10.glColorMask(true, true, true, false);
			return;
		}
	}

	/**
	 * ??????????VBO
	 * 
	 * @return
	 */
	public final static boolean checkVBO() {
		if (isVboSupported()) {
			return true;
		}
		if (baseGL instanceof javax.microedition.khronos.opengles.GL11) {
			if (isPixelFlinger()) {
				setVBOSupported(false);
				return false;
			}
			// ?????VBO??????????FBO??
			String extensions = baseGL.glGetString(GL10.GL_EXTENSIONS)
					.toLowerCase();
			// ????????vbo,??????gl1.1
			if (GLEx.isSupportGL11()
					&& extensions.contains("vertex_buffer_object")) {
				setVBOSupported(true);
				return true;
			}
		}
		GLEx.setVBOSupported(false);
		return false;
	}

	/**
	 * ??????VBO
	 * 
	 * @return
	 */
	public final static boolean isVbo() {
		return GLEx.vboOn;
	}

	/**
	 * ??????VBO
	 * 
	 * @param vboOn
	 */
	public final static void setVbo(boolean vbo) {
		GLEx.vboOn = vbo;
	}

	/**
	 * ??????VBO
	 * 
	 * @return
	 */
	public static boolean isVboSupported() {
		return vboSupported;
	}

	/**
	 * ??????VBO
	 * 
	 * @param vboSupported
	 */
	public final static void setVBOSupported(boolean vboSupported) {
		GLEx.vboSupported = vboSupported;
	}

	/**
	 * ?????????
	 * 
	 */
	public final void glPushMatrix() {
		if (isClose) {
			return;
		}
		gl10.glPushMatrix();
	}

	/**
	 * ???????????
	 * 
	 */
	public final void glPopMatrix() {
		if (isClose) {
			return;
		}
		gl10.glPopMatrix();
	}

	/**
	 * ???????
	 * 
	 * @param clear
	 */
	public void reset(boolean clear) {
		if (isClose) {
			return;
		}
		bind(0);
		if (isTex2DEnabled) {
			gl.glDisable(GL.GL_TEXTURE_2D);
			isTex2DEnabled = false;
		}
		if (clear) {
			gl10.glClearColor(0, 0, 0, 1);
			gl10.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_STENCIL_BUFFER_BIT);
		}
	}

	/**
	 * ????
	 * 
	 */
	public final void drawClear() {
		if (isClose) {
			return;
		}
		drawClear(GLColor.black);
	}

	/**
	 * ?????????
	 * 
	 * @param color
	 */
	public final void drawClear(GLColor color) {
		if (isClose) {
			return;
		}
		gl10.glClearColor(color.r, color.g, color.b, color.a);
		gl10.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT
				| GL10.GL_STENCIL_BUFFER_BIT);
	}

	/**
	 * ???????????
	 * 
	 * @param blendMode
	 */
	public final void beginBlend(int blendMode) {
		if (currentBlendMode == blendMode) {
			return;
		}
		this.prevBlendMode = currentBlendMode;
		this.setBlendMode(blendMode);
	}

	/**
	 * ????????????
	 * 
	 */
	public final void endBlend() {
		this.setBlendMode(prevBlendMode);
	}

	/**
	 * ???????
	 * 
	 * @param alpha
	 */
	public void setAlphaValue(int alpha) {
		if (isClose) {
			return;
		}
		setAlpha((float) alpha / 255);
	}

	public void test() {
		lastAlpha = 1;
	}

	public boolean isAlpha() {
		return onAlpha;
	}

	/**
	 * ???????
	 * 
	 * @param alpha
	 */
	public void setAlpha(float alpha) {
		if (alpha == lastAlpha) {
			return;
		}
		lastAlpha = alpha < 0 ? 0 : alpha > 1 ? 1 : alpha;
		if (lastAlpha >= 0.95f) {
			if (onReplace) {
				glTexEnvfReplaceColor(1, 1, 1, 1);
			} else {
				glTexEnvfModulateColor(1, 1, 1, 1);
			}
			onAlpha = false;
		} else {
			glTexEnvfModulateColor(1, 1, 1, lastAlpha);
			onAlpha = true;
		}
	}

	/**
	 * ??????????
	 * 
	 * @return
	 */
	public float getAlpha() {
		return color.a;
	}

	/**
	 * ??????
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 */
	public void setColorValue(int r, int g, int b, int a) {
		float red = (float) r / 255.0f;
		float green = (float) g / 255.0f;
		float blue = (float) b / 255.0f;
		float alpha = (float) a / 255.0f;
		setColor(red, green, blue, alpha);
	}

	/**
	 * ??????
	 * 
	 */
	public final void resetColor() {
		if (isClose) {
			return;
		}
		if (!color.equals(GLColor.white)) {
			color.setColor(1f, 1f, 1f, 1f);
			gl10.glColor4f(1f, 1f, 1f, 1f);
		}
	}

	/**
	 * ??????
	 * 
	 * @param color
	 */
	public final void setColorRGB(GLColor c) {
		if (isClose) {
			return;
		}
		if (!c.equals(color)) {
			updateColor(c.r, c.g, c.b, lastAlpha);
			color.setColor(c.r, c.g, c.b, lastAlpha);
			gl10.glColor4f(color.r, color.g, color.b, color.a);
		}
	}

	/**
	 * ??????
	 * 
	 * @param color
	 */
	public final void setColorARGB(GLColor c) {
		if (isClose) {
			return;
		}
		if (!c.equals(color)) {
			float alpha = lastAlpha == 1 ? c.a : lastAlpha;
			updateColor(c.r, c.g, c.b, alpha);
			color.setColor(c.r, c.g, c.b, alpha);
			gl10.glColor4f(color.r, color.g, color.b, color.a);
		}
	}

	/**
	 * ??????
	 * 
	 * @param pixel
	 */
	public final void setColor(int pixel) {
		int[] rgbs = GLColor.getRGBs(pixel);
		setColorValue(rgbs[0], rgbs[1], rgbs[2], (int) (lastAlpha * 255));
	}

	/**
	 * ??????
	 * 
	 * @param c
	 */
	public final void setColor(GLColor c) {
		setColorARGB(c);
	}

	/**
	 * ??????
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 */
	public final void setColor(final float r, final float g, final float b,
			final float a) {
		if (isClose) {
			return;
		}
		updateColor(r, g, b, a);
		color.setColor(r, g, b, a);
		gl10.glColor4f(color.r, color.g, color.b, color.a);
	}

	/**
	 * ??????
	 * 
	 * @param r
	 * @param g
	 * @param b
	 */
	public final void setColor(final float r, final float g, final float b) {
		setColor(r, g, b, lastAlpha);
	}

	/**
	 * ????????
	 * 
	 * @return
	 */
	public final GLColor getColor() {
		return new GLColor(color);
	}

	public final int getColorRGB() {
		return color.getRGB();
	}

	public final int getColorARGB() {
		return color.getARGB();
	}

	private void updateColor(float r, float g, float b, float a) {
		if (!onReplace && !onTexEnvf && lastAlpha == 1
				&& !color.equals(r, g, b, a)) {
			gl10.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE,
					GL.GL_MODULATE);
		}
	}
	
	/**
	 * ???????????
	 * 
	 * @param flag
	 */
	public void setAntiAlias(boolean flag) {
		if (isClose) {
			return;
		}
		if (flag) {
			gl10.glEnable(GL.GL_LINE_SMOOTH);
		} else {
			gl10.glDisable(GL.GL_LINE_SMOOTH);
		}
		this.isAntialias = flag;
	}

	public boolean isAntialias() {
		return isAntialias;
	}

	/**
	 * ????????
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public GLColor getPixel(int x, int y) {
		if (isClose) {
			return color;
		}
		GLEx.gl10.glReadPixels(x,
				(int) (LSystem.screenRect.height * LSystem.scaleHeight) - y
						- viewPort.height, viewPort.width, viewPort.height,
				GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, readBuffer);
		return new GLColor(GLColor.c(readBuffer.get(0)), GLColor.c(readBuffer
				.get(1)), GLColor.c(readBuffer.get(2)), GLColor.c(readBuffer
				.get(3)));
	}

	/**
	 * ??????????ByteBuffer???
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param target
	 */
	public void getArea(int x, int y, int width, int height, ByteBuffer target) {
		if (isClose) {
			return;
		}
		if (target.capacity() < width * height * 4) {
			throw new IllegalArgumentException(
					"Byte buffer provided to get area is not big enough");
		}
		gl10.glReadPixels(x,
				(int) (LSystem.screenRect.height * LSystem.scaleHeight) - y
						- height, width, height, GL10.GL_RGBA,
				GL10.GL_UNSIGNED_BYTE, target);

	}

	/**
	 * ?????
	 * 
	 * @param color
	 * @param x
	 * @param y
	 * @param r
	 */
	public void drawSixStart(GLColor color, float x, float y, float r) {
		if (isClose) {
			return;
		}
		setColor(color);
		drawTriangle(color, x, y, r);
		drawRTriangle(color, x, y, r);
	}

	/**
	 * ?????
	 * 
	 * @param color
	 * @param x
	 * @param y
	 * @param r
	 */
	public void drawTriangle(GLColor color, float x, float y, float r) {
		if (isClose) {
			return;
		}
		float x1 = x;
		float y1 = y - r;
		float x2 = x - (r * MathUtils.cos(MathUtils.PI / 6));
		float y2 = y + (r * MathUtils.sin(MathUtils.PI / 6));
		float x3 = x + (r * MathUtils.cos(MathUtils.PI / 6));
		float y3 = y + (r * MathUtils.sin(MathUtils.PI / 6));
		float[] xpos = new float[3];
		xpos[0] = x1;
		xpos[1] = x2;
		xpos[2] = x3;
		float[] ypos = new float[3];
		ypos[0] = y1;
		ypos[1] = y2;
		ypos[2] = y3;
		setColor(color);
		fillPolygon(xpos, ypos, 3);
	}

	/**
	 * ?????
	 * 
	 * @param color
	 * @param x
	 * @param y
	 * @param r
	 */
	public void drawRTriangle(GLColor color, float x, float y, float r) {
		if (isClose) {
			return;
		}
		float x1 = x;
		float y1 = y + r;
		float x2 = x - (r * MathUtils.cos(MathUtils.PI / 6.0f));
		float y2 = y - (r * MathUtils.sin(MathUtils.PI / 6.0f));
		float x3 = x + (r * MathUtils.cos(MathUtils.PI / 6.0f));
		float y3 = y - (r * MathUtils.sin(MathUtils.PI / 6.0f));
		float[] xpos = new float[3];
		xpos[0] = x1;
		xpos[1] = x2;
		xpos[2] = x3;
		float[] ypos = new float[3];
		ypos[0] = y1;
		ypos[1] = y2;
		ypos[2] = y3;
		setColor(color);
		fillPolygon(xpos, ypos, 3);
	}

	/**
	 * ?????
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 */
	public void drawTriangle(final float x1, final float y1, final float x2,
			final float y2, final float x3, final float y3) {
		if (isClose) {
			return;
		}
		glBegin(GL.GL_LINE_LOOP);
		glVertex2f(x1, y1);
		glVertex2f(x2, y2);
		glVertex2f(x3, y3);
		glEnd();
	}

	/**
	 * ?????
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 */
	public void fillTriangle(final float x1, final float y1, final float x2,
			final float y2, final float x3, final float y3) {
		if (isClose) {
			return;
		}
		glBegin(GL.GL_TRIANGLES);
		glVertex2f(x1, y1);
		glVertex2f(x2, y2);
		glVertex2f(x3, y3);
		glEnd();
	}

	/**
	 * ?????????
	 * 
	 * @param ts
	 */
	public void fillTriangle(Triangle2f[] ts) {
		fillTriangle(ts, 0, 0);
	}

	/**
	 * ?????????
	 * 
	 * @param ts
	 * @param x
	 * @param y
	 */
	public void fillTriangle(Triangle2f[] ts, int x, int y) {
		if (isClose) {
			return;
		}
		if (ts == null) {
			return;
		}
		int size = ts.length;
		for (int i = 0; i < size; i++) {
			fillTriangle(ts[i], x, y);
		}
	}

	/**
	 * ?????????
	 * 
	 * @param t
	 */
	public void fillTriangle(Triangle2f t) {
		fillTriangle(t, 0, 0);
	}

	/**
	 * ?????????
	 * 
	 * @param t
	 * @param x
	 * @param y
	 */
	public void fillTriangle(Triangle2f t, float x, float y) {
		if (isClose) {
			return;
		}
		if (t == null) {
			return;
		}
		float[] xpos = new float[3];
		float[] ypos = new float[3];
		xpos[0] = x + t.xpoints[0];
		xpos[1] = x + t.xpoints[1];
		xpos[2] = x + t.xpoints[2];
		ypos[0] = y + t.ypoints[0];
		ypos[1] = y + t.ypoints[1];
		ypos[2] = y + t.ypoints[2];
		fillPolygon(xpos, ypos, 3);
	}

	/**
	 * ??????
	 * 
	 * @param ts
	 */
	public void drawTriangle(Triangle2f[] ts) {
		drawTriangle(ts, 0, 0);
	}

	/**
	 * ??????
	 * 
	 * @param ts
	 * @param x
	 * @param y
	 */
	public void drawTriangle(Triangle2f[] ts, int x, int y) {
		if (isClose) {
			return;
		}
		if (ts == null) {
			return;
		}
		int size = ts.length;
		for (int i = 0; i < size; i++) {
			drawTriangle(ts[i], x, y);
		}
	}

	/**
	 * ????
	 * 
	 * @param t
	 */
	public void drawTriangle(Triangle2f t) {
		drawTriangle(t, 0, 0);
	}

	/**
	 * ????
	 * 
	 * @param t
	 * @param x
	 * @param y
	 */
	public void drawTriangle(Triangle2f t, int x, int y) {
		if (isClose) {
			return;
		}
		if (t == null) {
			return;
		}
		float[] xpos = new float[3];
		float[] ypos = new float[3];
		xpos[0] = x + t.xpoints[0];
		xpos[1] = x + t.xpoints[1];
		xpos[2] = x + t.xpoints[2];
		ypos[0] = y + t.ypoints[0];
		ypos[1] = y + t.ypoints[1];
		ypos[2] = y + t.ypoints[2];
		drawPolygon(xpos, ypos, 3);
	}

	/**
	 * ????
	 * 
	 * @param centerX
	 * @param centerY
	 * @param r
	 * @param a
	 */
	public void drawOval(float x1, float y1, float width, float height) {
		this.drawArc(x1, y1, width, height, 32, 0, 360);
	}

	/**
	 * ????
	 * 
	 * @param centerX
	 * @param centerY
	 * @param r
	 * @param a
	 */
	public void fillOval(float x1, float y1, float width, float height) {
		this.fillArc(x1, y1, width, height, 32, 0, 360);
	}

	/**
	 * ??
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public void drawLine(float x1, float y1, float x2, float y2) {
		if (isClose) {
			return;
		}
		try {
			switch (drawingType) {
			case 0:
				$drawLine0(x1, y1, x2, y2);
				break;
			case 1:
				$drawLine1(x1, y1, x2, y2, true);
				break;
			}
		} catch (Exception e) {
			switch (drawingType) {
			case 0:
				$drawLine1(x1, y1, x2, y2, true);
				break;
			case 1:
				$drawLine0(x1, y1, x2, y2);
				break;
			}
		}
	}

	/**
	 * drawLine????????
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	private void $drawLine0(float x1, float y1, float x2, float y2) {
		if (x1 > x2) {
			x1++;
		} else {
			x2++;
		}
		if (y1 > y2) {
			y1++;
		} else {
			y2++;
		}

		floatVertices.rewind();
		floatVertices.limit(4 * 2 * 2);
		floatVertices.put(x1);
		floatVertices.put(y1);
		floatVertices.put(x2);
		floatVertices.put(y2);
		floatVertices.position(0);
		floatVertices.flip();

		glTex2DDisable();
		{
			gl10.glVertexPointer(2, GL10.GL_FLOAT, 0, floatVertices);
			gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl10.glDrawArrays(GL10.GL_LINES, 0, 2);
			gl10.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		}
	}

	/**
	 * drawLine????????
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	private void $drawLine1(float x1, float y1, float x2, float y2, boolean use) {
		if (x1 > x2) {
			x1++;
		} else {
			x2++;
		}
		if (y1 > y2) {
			y1++;
		} else {
			y2++;
		}

		if (use) {
			glBegin(GL.GL_LINES);
		}
		{
			glVertex2f(x1, y1);
			glVertex2f(x2, y2);
		}
		if (use) {
			glEnd();
		}
	}

	/**
	 * ?????
	 * 
	 * @param x
	 * @param y
	 */
	public void drawPoint(float x, float y) {
		if (isClose) {
			return;
		}
		floatVertices.rewind();
		floatVertices.limit(4 * 2 * 1);

		floatVertices.put(x);
		floatVertices.put(y);
		floatVertices.position(0);
		floatVertices.flip();
		glTex2DDisable();
		{
			gl10.glVertexPointer(2, GL10.GL_FLOAT, 0, floatVertices);
			gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl10.glDrawArrays(GL10.GL_POINTS, 0, 1);
			gl10.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		}
	}

	/**
	 * ???????
	 * 
	 * @param x
	 * @param y
	 * @param size
	 */
	public void drawPoints(float x[], float y[], int size) {
		if (isClose) {
			return;
		}
		floatVertices.rewind();
		floatVertices.limit(4 * 2 * size);
		for (int i = 0; i < size; i++) {
			floatVertices.put(x[i]);
			floatVertices.put(y[i]);
		}
		floatVertices.position(0);
		floatVertices.flip();
		glTex2DDisable();
		{
			gl10.glVertexPointer(2, GL10.GL_FLOAT, 0, floatVertices);
			gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl10.glDrawArrays(GL10.GL_POINTS, 0, size);
			gl10.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		}
	}

	/**
	 * ??????
	 * 
	 * @param shape
	 */
	public final void draw(Shape shape) {
		if (isClose) {
			return;
		}
		float[] points = shape.getPoints();
		switch (drawingType) {
		case 0:
			int size = points.length / 2;
			floatVertices.rewind();
			floatVertices.limit(4 * 2 * size);
			for (int i = 0; i < points.length; i += 2) {
				floatVertices.put(points[i]);
				floatVertices.put(points[i + 1]);
			}
			if (shape.closed()) {
				floatVertices.put(points[0]);
				floatVertices.put(points[1]);
			}
			floatVertices.position(0);
			floatVertices.flip();
			glTex2DDisable();
			{
				gl10.glVertexPointer(2, GL10.GL_FLOAT, 0, floatVertices);
				gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
				gl10.glDrawArrays(GL10.GL_LINE_LOOP, 0, size);
				gl10.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			}
			return;
		case 1:
			glBegin(GL.GL_LINE_STRIP);
			for (int i = 0; i < points.length; i += 2) {
				glVertex2f(points[i], points[i + 1]);
			}
			if (shape.closed()) {
				glVertex2f(points[0], points[1]);
			}
			glEnd();
			return;
		}
	}

	/**
	 * ??????
	 * 
	 * @param shape
	 */
	public final void fill(Shape shape) {
		Triangle tris = shape.getTriangles();
		if (isClose) {
			return;
		}
		float[] points = shape.getPoints();
		switch (drawingType) {
		case 0:
			int size = points.length / 2;
			floatVertices.rewind();
			floatVertices.limit(4 * 3 * size);
			for (int i = 0; i < tris.getTriangleCount(); i++) {
				for (int p = 0; p < 3; p++) {
					float[] pt = tris.getTrianglePoint(i, p);
					floatVertices.put(pt[0]);
					floatVertices.put(pt[1]);
					floatVertices.put(0);
				}
			}
			int count = floatVertices.position() / 3;
			floatVertices.position(0);
			floatVertices.flip();
			glTex2DDisable();
			{
				gl10.glVertexPointer(3, GL10.GL_FLOAT, 0, floatVertices);
				gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
				gl10.glDrawArrays(GL10.GL_TRIANGLES, 0, count);
				gl10.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			}
			return;
		case 1:
			glBegin(GL.GL_TRIANGLES);
			for (int i = 0; i < tris.getTriangleCount(); i++) {
				for (int p = 0; p < 3; p++) {
					float[] pt = tris.getTrianglePoint(i, p);
					glVertex2f(pt[0], pt[1]);
				}
			}
			glEnd();
			return;
		}
	}

	/**
	 * ??????????
	 * 
	 * @param p
	 * @param x
	 * @param y
	 */
	public void draw(final Shape p, final float x, final float y) {
		if (isClose) {
			return;
		}
		gl10.glPushMatrix();
		gl10.glTranslatef(x, y, 0.0f);
		draw(p);
		gl10.glPopMatrix();
	}

	/**
	 * ????????????
	 * 
	 * @param p
	 * @param rotation
	 */
	public void draw(final Shape p, final float rotation) {
		if (isClose) {
			return;
		}
		gl10.glPushMatrix();
		gl10.glRotatef(-rotation, 0.0f, 0.0f, 1.0f);
		draw(p);
		gl10.glPopMatrix();
	}

	/**
	 * ??????????
	 * 
	 * @param p
	 * @param x
	 * @param y
	 */
	public void fill(final Shape p, final float x, final float y) {
		if (isClose) {
			return;
		}
		gl10.glPushMatrix();
		gl10.glTranslatef(x, y, 0.0f);
		fill(p);
		gl10.glPopMatrix();
	}

	/**
	 * ????????????
	 * 
	 * @param p
	 * @param rotation
	 */
	public void fill(final Shape p, final float rotation) {
		if (isClose) {
			return;
		}
		gl10.glPushMatrix();
		gl10.glRotatef(-rotation, 0.0f, 0.0f, 1.0f);
		fill(p);
		gl10.glPopMatrix();
	}

	/**
	 * ?????
	 * 
	 * @param p
	 */
	public void fillPolygon(Polygon p) {
		fill(p);
	}

	/**
	 * ?????
	 * 
	 * @param xPoints
	 * @param yPoints
	 * @param nPoints
	 */
	public void fillPolygon(float xPoints[], float yPoints[], int nPoints) {
		if (isClose) {
			return;
		}
		try {
			switch (drawingType) {
			case 0:
				$fillPolygon0(xPoints, yPoints, nPoints);
				break;
			case 1:
				$fillPolygon1(xPoints, yPoints, nPoints, true);
				break;
			}
		} catch (Exception e) {
			switch (drawingType) {
			case 0:
				$fillPolygon1(xPoints, yPoints, nPoints, true);
				break;
			case 1:
				$fillPolygon0(xPoints, yPoints, nPoints);
				break;
			}
		}
	}

	/**
	 * fillPolygon?????
	 * 
	 * @param xPoints
	 * @param yPoints
	 * @param nPoints
	 */
	private void $fillPolygon0(float xPoints[], float yPoints[], int nPoints) {
		floatVertices.rewind();
		floatVertices.limit(4 * 2 * nPoints);

		for (int i = 0; i < nPoints; i++) {
			floatVertices.put(xPoints[i]);
			floatVertices.put(yPoints[i]);
		}
		floatVertices.position(0);
		floatVertices.flip();
		glTex2DDisable();
		{
			gl10.glVertexPointer(2, GL10.GL_FLOAT, 0, floatVertices);
			gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl10.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, nPoints);
			gl10.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		}
	}

	/**
	 * fillPolygon?????
	 * 
	 * @param xPoints
	 * @param yPoints
	 * @param nPoints
	 */
	private final void $fillPolygon1(float xPoints[], float yPoints[],
			int nPoints, boolean use) {
		if (use) {
			glBegin(GL.GL_POLYGON);
		}
		{
			for (int i = 0; i < nPoints; i++) {
				glVertex2f(xPoints[i], yPoints[i]);
			}
		}
		if (use) {
			glEnd();
		}
	}

	/**
	 * ???????
	 * 
	 * @param p
	 */
	public void drawPolygon(Polygon p) {
		draw(p);
	}

	/**
	 * ???????
	 * 
	 * @param xPoints
	 * @param yPoints
	 * @param nPoints
	 */
	public void drawPolygon(float[] xPoints, float[] yPoints, int nPoints) {
		if (isClose) {
			return;
		}
		try {
			switch (drawingType) {
			case 0:
				$drawPolygon0(xPoints, yPoints, nPoints);
				break;
			case 1:
				$drawPolygon1(xPoints, yPoints, nPoints, true);
				break;
			}
		} catch (Exception e) {
			switch (drawingType) {
			case 0:
				$drawPolygon1(xPoints, yPoints, nPoints, true);
				break;
			case 1:
				$drawPolygon0(xPoints, yPoints, nPoints);
				break;
			}
		}
	}

	/**
	 * drawPolygon?????
	 * 
	 * @param xPoints
	 * @param yPoints
	 * @param nPoints
	 */
	private void $drawPolygon0(float[] xPoints, float[] yPoints, int nPoints) {
		floatVertices.rewind();
		floatVertices.limit(4 * 2 * nPoints);

		for (int i = 0; i < nPoints; i++) {
			floatVertices.put(xPoints[i]);
			floatVertices.put(yPoints[i]);
		}

		floatVertices.position(0);
		floatVertices.flip();
		glTex2DDisable();
		{
			gl10.glVertexPointer(2, GL10.GL_FLOAT, 0, floatVertices);
			gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl10.glDrawArrays(GL10.GL_LINE_LOOP, 0, nPoints);
			gl10.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		}
	}

	/**
	 * drawPolygon?????
	 * 
	 * @param xPoints
	 * @param yPoints
	 * @param nPoints
	 */
	private void $drawPolygon1(float[] xPoints, float[] yPoints, int nPoints,
			boolean use) {
		if (use) {
			glBegin(GL.GL_LINE_LOOP);
		}
		for (int i = 0; i < nPoints; i++) {
			glVertex2f(xPoints[i], yPoints[i]);
		}
		if (use) {
			glEnd();
		}
	}

	/**
	 * ??????
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public final void drawRect(final float x1, final float y1, final float x2,
			final float y2) {
		setRect(x1, y1, x2, y2, false);
	}

	/**
	 * ??????
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public final void fillRect(final float x1, final float y1, final float x2,
			final float y2) {
		setRect(x1, y1, x2, y2, true);
	}

	/**
	 * ??????
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param fill
	 */
	public final void setRect(float x, float y, float width, float height,
			boolean fill) {
		if (isClose) {
			return;
		}
		float[] xs = new float[4];
		float[] ys = new float[4];

		xs[0] = x;
		xs[1] = x + width;
		xs[2] = x + width;
		xs[3] = x;

		ys[0] = y;
		ys[1] = y;
		ys[2] = y + 
