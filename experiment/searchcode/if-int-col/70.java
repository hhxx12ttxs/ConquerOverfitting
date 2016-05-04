package org.loon.framework.game.simple.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Transparency;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.imageio.ImageIO;

import org.loon.framework.game.simple.GameContext;
import org.loon.framework.game.simple.GameManager;
import org.loon.framework.game.simple.core.LSystem;
import org.loon.framework.game.simple.core.graphics.AWTDataBufferHelper;
import org.loon.framework.game.simple.core.graphics.GrayFilter;
import org.loon.framework.game.simple.core.graphics.LColor;
import org.loon.framework.game.simple.core.graphics.LFont;
import org.loon.framework.game.simple.core.graphics.ScreenManager;
import org.loon.framework.game.simple.core.graphics.window.UIStatic;

/**
 * 
 * Copyright 2008 - 2009
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
final public class GraphicsUtils {

	final static public Toolkit toolKit = Toolkit.getDefaultToolkit();

	final static private Map cacheImages = Collections
			.synchronizedMap(new HashMap(LSystem.DEFAULT_MAX_CACHE_SIZE));

	// ????????????????WeakHashMap
	final static private Map cacheByteImages = new WeakHashMap(
			LSystem.DEFAULT_MAX_CACHE_SIZE);

	private static Map lazySplitMap = Collections.synchronizedMap(new HashMap(
			LSystem.DEFAULT_MAX_CACHE_SIZE));

	// ????????????
	final static RenderingHints hints_excellent;

	// ???????
	final static RenderingHints hints_general;

	// ????????????
	final static RenderingHints hints_poor;

	static {
		// ???????????
		hints_general = new RenderingHints(null);
		// ???????????
		hints_excellent = new RenderingHints(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		hints_excellent.put(RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_ENABLE);
		hints_excellent.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		hints_excellent.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		hints_excellent.put(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		hints_excellent.put(RenderingHints.KEY_COLOR_RENDERING,
				RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		hints_excellent.put(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		hints_excellent.put(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_PURE);
		hints_excellent.put(RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		// ???????????
		hints_poor = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		hints_poor.put(RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_DISABLE);
		hints_poor.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_SPEED);
		hints_poor.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		hints_poor.put(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		hints_poor.put(RenderingHints.KEY_COLOR_RENDERING,
				RenderingHints.VALUE_COLOR_RENDER_SPEED);
		hints_poor.put(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		hints_poor.put(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_NORMALIZE);
		hints_poor.put(RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);

	}

	private GraphicsUtils() {
	}

	private static final Insets NULL_INSETS = new Insets(0, 0, 0, 0);

	private static final Integer VSPACE = new Integer(2);

	/**
	 * ???????BufferedImage
	 * 
	 * @param count
	 * @param w
	 * @param h
	 * @param transparency
	 * @return
	 */
	public static BufferedImage[] createImage(int count, int w, int h,
			int transparency) {
		BufferedImage[] image = new BufferedImage[count];
		for (int i = 0; i < image.length; i++) {
			image[i] = ScreenManager.graphicsConfiguration
					.createCompatibleImage(w, h, transparency);
		}
		return image;
	}

	/**
	 * ??????????
	 * 
	 * @param image
	 * @param alpha
	 */
	public static void setAlphaImage(BufferedImage image, int alpha) {
		if (alpha < 0 || alpha > 255) {
			return;
		}
		int width = image.getWidth();
		int height = image.getHeight();
		int arr[] = getArrayAlpha1D(image);
		for (int i = 0; i < width * height; i++) {
			if (arr[i] != 0) {
				arr[i] = alpha;
			}
		}
		setArrayAlpha(image, arr);
	}

	/**
	 * ?????????????
	 * 
	 * @param image
	 * @return
	 */
	public static int[] getArrayAlpha1D(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		int array[] = new int[width * height];
		image.getAlphaRaster().getPixels(0, 0, width, height, array);
		return array;
	}

	/**
	 * ?????????????
	 * 
	 * @param image
	 * @return
	 */
	public static int[][] getArrayAlpha2D(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		int arr[] = getArrayAlpha1D(image);
		int array[][] = new int[width][height];
		int i = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++, i++) {
				array[x][y] = arr[i];
			}
		}
		return array;
	}

	/**
	 * ????????
	 * 
	 * @param image
	 * @param array
	 */
	public static void setArrayAlpha(BufferedImage image, int[] array) {
		int width = image.getWidth();
		int height = image.getHeight();
		image.getAlphaRaster().setPixels(0, 0, width, height, array);
	}

	/**
	 * ????????
	 * 
	 * @param image
	 * @param array
	 */
	public static void setArrayAlpha(BufferedImage image, int[][] array) {
		int width = image.getWidth();
		int height = image.getHeight();
		int arr[] = new int[width * height];
		int j = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++, j++) {
				arr[j] = array[x][y];
			}
		}
		setArrayAlpha(image, arr);
	}

	/**
	 * ????????
	 * 
	 * @param image
	 * @param color
	 */
	public static void makeTransparency(BufferedImage image, int color) {
		int width = image.getWidth();
		int height = image.getHeight();
		int arrA[] = getArrayAlpha1D(image);
		int arrC[] = getArrayColor1D(image);
		for (int i = 0; i < width * height; i++) {
			if (arrC[i] == color) {
				arrA[i] = 0;
			}
		}
		setArrayAlpha(image, arrA);
	}

	/**
	 * ?????Color??
	 * 
	 * @param image
	 * @return
	 */
	public static int[] getArrayColor1D(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		int array[] = new int[width * height];
		image.getRGB(0, 0, width, height, array, 0, width);
		return array;
	}

	/**
	 * ?????Color????
	 * 
	 * @param image
	 * @return
	 */
	public static int[][] getArrayColor2D(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		int array[] = new int[width * height];
		image.getRGB(0, 0, width, height, array, 0, width);
		int i = 0;
		int myarray[][] = new int[width][height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				myarray[x][y] = array[i];
				i++;
			}
		}
		return myarray;
	}

	/**
	 * ??????Color????
	 * 
	 * @param image
	 * @param array
	 */
	public static void setArrayColor(BufferedImage image, int[][] array) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[] pixels = new int[width * height];
		int j = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pixels[j] = array[x][y];
				j++;
			}
		}
		image.setRGB(0, 0, width, height, pixels, 0, width);
	}

	/**
	 * ??????Color??
	 * 
	 * @param Bimage
	 * @param array
	 */
	public static void setArrayColor(BufferedImage image, int[] array) {
		image.setRGB(0, 0, image.getWidth(), image.getHeight(), array, 0, image
				.getWidth());
	}

	/**
	 * ????????????????
	 * 
	 * @param g
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param color
	 */
	public static void rectFill(Graphics g, int x, int y, int width,
			int height, Color color) {
		g.setColor(color);
		g.fillRect(x, y, width, height);
	}

	/**
	 * ????????????????
	 * 
	 * @param g
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param color
	 */
	public static void rectDraw(Graphics g, int x, int y, int width,
			int height, Color color) {
		g.setColor(color);
		g.drawRect(x, y, width, height);
	}

	/**
	 * ?????????????????
	 * 
	 * @param g
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param color
	 */
	public static void rectOval(Graphics g, int x, int y, int width,
			int height, Color color) {
		g.setColor(color);
		g.drawOval(x, y, width, height);
		g.fillOval(x, y, width, height);
	}

	/**
	 * ??????????
	 * 
	 * @return
	 */
	public static Font getFont() {
		return getFont(LSystem.FONT, LSystem.FONT_TYPE);
	}

	/**
	 * ??????????
	 * 
	 * @param size
	 * @return
	 */
	public static Font getFont(int size) {
		return getFont(LSystem.FONT, size);
	}

	/**
	 * ??????????
	 * 
	 * @param fontName
	 * @param size
	 * @return
	 */
	public static Font getFont(String fontName, int size) {
		return getFont(fontName, 0, size);
	}

	/**
	 * ??????????
	 * 
	 * @param fontName
	 * @param style
	 * @param size
	 * @return
	 */
	public static Font getFont(String fontName, int style, int size) {
		return new Font(fontName, style, size);
	}

	/**
	 * ?????????????
	 * 
	 * @param graphics
	 * @param message
	 * @param i
	 * @param j
	 * @param color
	 * @param color1
	 */
	public static void drawStyleString(final Graphics graphics,
			final String message, final int x, final int y, final Color color,
			final Color color1) {
		graphics.setColor(color);
		graphics.drawString(message, x + 1, y);
		graphics.drawString(message, x - 1, y);
		graphics.drawString(message, x, y + 1);
		graphics.drawString(message, x, y - 1);
		graphics.setColor(color1);
		graphics.drawString(message, x, y);

	}

	/**
	 * ?????
	 * 
	 * @param g
	 * @param color
	 * @param x
	 * @param y
	 * @param r
	 */
	public static void drawSixStart(Graphics g, Color color, int x, int y, int r) {
		g.setColor(color);
		drawTriangle(g, color, x, y, r);
		drawRTriangle(g, color, x, y, r);
	}

	/**
	 * ?????
	 * 
	 * @param g
	 * @param color
	 * @param x
	 * @param y
	 * @param r
	 */
	public static void drawTriangle(Graphics g, Color color, int x, int y, int r) {
		int x1 = x;
		int y1 = y - r;
		int x2 = x - (int) (r * Math.cos(Math.PI / 6));
		int y2 = y + (int) (r * Math.sin(Math.PI / 6));
		int x3 = x + (int) (r * Math.cos(Math.PI / 6));
		int y3 = y + (int) (r * Math.sin(Math.PI / 6));
		int[] xpos = new int[3];
		xpos[0] = x1;
		xpos[1] = x2;
		xpos[2] = x3;
		int[] ypos = new int[3];
		ypos[0] = y1;
		ypos[1] = y2;
		ypos[2] = y3;
		g.setColor(color);
		g.fillPolygon(xpos, ypos, 3);
	}

	/**
	 * ?????
	 * 
	 * @param g
	 * @param color
	 * @param x
	 * @param y
	 * @param r
	 */
	public static void drawRTriangle(Graphics g, Color color, int x, int y,
			int r) {
		int x1 = x;
		int y1 = y + r;
		int x2 = x - (int) (r * Math.cos(Math.PI / 6.0));
		int y2 = y - (int) (r * Math.sin(Math.PI / 6.0));
		int x3 = x + (int) (r * Math.cos(Math.PI / 6.0));
		int y3 = y - (int) (r * Math.sin(Math.PI / 6.0));
		int[] xpos = new int[3];
		xpos[0] = x1;
		xpos[1] = x2;
		xpos[2] = x3;
		int[] ypos = new int[3];
		ypos[0] = y1;
		ypos[1] = y2;
		ypos[2] = y3;
		g.setColor(color);
		g.fillPolygon(xpos, ypos, 3);
	}

	/**
	 * copy??????????
	 * 
	 * @param target
	 * @param source
	 * @return
	 */
	public static BufferedImage copy(BufferedImage target, Image source) {
		Graphics2D g = target.createGraphics();
		g.drawImage(source, 0, 0, null);
		g.dispose();
		return target;
	}

	/**
	 * ????Image???ColorModel
	 * 
	 * @param image
	 * @return
	 */
	public static ColorModel getColorModel(Image image) {
		try {
			PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
			pg.grabPixels();
			return pg.getColorModel();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * ????BufferImage
	 * 
	 * @param w
	 * @param h
	 * @param flag
	 * @return
	 */
	final static public BufferedImage createImage(int w, int h, boolean flag) {
		if (flag) {
			if (LSystem.isOverrunJdk15()) {
				return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
			} else {
				return ScreenManager.graphicsConfiguration
						.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
			}
		} else {
			return ScreenManager.graphicsConfiguration.createCompatibleImage(w,
					h, Transparency.BITMASK);
		}
	}

	final static public BufferedImage createIntdexedImage(int w, int h) {
		return new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED);
	}

	/**
	 * ?????????????
	 * 
	 * @param color
	 * @param flag
	 * @param w
	 * @param h
	 * @return
	 */
	public static BufferedImage createButtonImage(Color color, boolean flag,
			int w, int h) {
		BufferedImage bufferedimage = new BufferedImage(w, h,
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D graphics2d = bufferedimage.createGraphics();
		Color color1 = (color = flag ? color.brighter() : color).brighter();
		GradientPaint gradientpaint = new GradientPaint(0, 0, color, w / 2 + 2,
				h / 2 + 2, color1);
		graphics2d.setPaint(gradientpaint);
		graphics2d.fillRect(2, 2, w - 4, h - 4);
		graphics2d.setColor(Color.BLACK);
		graphics2d.drawLine(1, h - 3, 1, 1);
		graphics2d.drawLine(1, 1, w - 3, 1);
		graphics2d.setColor(Color.BLACK);
		graphics2d.drawLine(0, h - 1, w - 1, h - 1);
		graphics2d.drawLine(w - 1, h - 1, w - 1, 0);
		graphics2d.setColor(Color.BLACK);
		graphics2d.drawRect(0, 0, w - 2, h - 2);
		graphics2d.dispose();
		graphics2d = null;
		return bufferedimage;
	}

	/**
	 * ??????????
	 * 
	 * @param w
	 * @param h
	 * @param color1
	 * @param color2
	 * @return
	 */
	public static BufferedImage createButtonBackground(int w, int h,
			Color color1, Color color2) {
		BufferedImage image = GraphicsUtils.createImage(w, h, false);
		Graphics2D g = image.createGraphics();
		GradientPaint gradientpaint = new GradientPaint(0, 0, color1, w / 2, h,
				color2);
		g.setPaint(gradientpaint);
		g.fillRect(2, 2, w - 4, h - 4);
		g.setColor(Color.BLACK);
		g.drawLine(1, h - 3, 1, 1);
		g.drawLine(1, 1, w - 3, 1);
		g.setColor(Color.BLACK);
		g.drawLine(0, h - 1, w - 1, h - 1);
		g.drawLine(w - 1, h - 1, w - 1, 0);
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, w - 2, h - 2);
		g.dispose();
		g = null;
		return image;
	}

	/**
	 * ??????????BufferedImage
	 * 
	 * @param width
	 * @param height
	 * @param transparency
	 * @return
	 */
	public static BufferedImage createImage(int width, int height,
			int transparency) {
		return ScreenManager.graphicsConfiguration.createCompatibleImage(width,
				height, transparency);
	}

	/**
	 * ????BufferedImage
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	public static BufferedImage createImage(int width, int height) {
		return ScreenManager.graphicsConfiguration.createCompatibleImage(width,
				height);
	}

	/**
	 * ????VolatileImage
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	public static VolatileImage createVolatileImage(int width, int height) {
		return ScreenManager.graphicsConfiguration
				.createCompatibleVolatileImage(width, height);
	}

	/**
	 * ???????(int[]??)??BufferedImage
	 * 
	 * @param data
	 * @return
	 */
	final static public BufferedImage getImage(int[] data) {
		if (data == null || data.length < 3 || data[0] < 1 || data[1] < 1) {
			return null;
		}
		int width = data[0];
		int height = data[1];
		if (data.length < 2 + width * height) {
			return null;
		}
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_BGR);
		if (image == null) {
			return null;
		}
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				image.setRGB(j, i, data[2 + j + i * width]);
			}
		}
		return image;
	}

	/**
	 * ????BufferedImage??
	 * 
	 * @param image
	 * @param w
	 * @param h
	 * @return
	 */
	public static BufferedImage getResize(BufferedImage image, int w, int h) {
		int width = image.getWidth(), height = image.getHeight();
		if (width == w && height == h) {
			return image;
		}
		BufferedImage img;
		AffineTransform tx = new AffineTransform();
		tx.scale((double) w / width, (double) h / height);
		AffineTransformOp op = new AffineTransformOp(tx,
				AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		img = op.filter(image, null);
		return img;
	}

	/**
	 * ???????????
	 * 
	 * @param fileName
	 * @param range(????????("1-2"))
	 * @return
	 */
	public static Image[] loadSequenceImages(String fileName, String range) {
		try {
			int start_range = -1;
			int end_range = -1;
			int images_count = 1;
			int minusIndex = range.indexOf('-');
			if ((minusIndex > 0) && (minusIndex < (range.length() - 1))) {
				try {
					start_range = Integer.parseInt(range.substring(0,
							minusIndex));
					end_range = Integer.parseInt(range
							.substring(minusIndex + 1));
					if (start_range < end_range) {
						images_count = end_range - start_range + 1;
					}
				} catch (Exception ex) {
				}
			}
			Image[] images = new Image[images_count];
			for (int i = 0; i < images_count; i++) {
				String imageName = fileName;
				if (images_count > 1) {
					int dotIndex = fileName.lastIndexOf('.');
					if (dotIndex >= 0) {
						imageName = fileName.substring(0, dotIndex)
								+ (start_range + i)
								+ fileName.substring(dotIndex);
					}
				}
				images[i] = GraphicsUtils.loadImage(imageName);
			}
			return images;
		} catch (Exception ex) {
		}
		return null;
	}

	/**
	 * ????BufferedImage??
	 * 
	 * @param image
	 * @param size
	 * @return
	 */
	public static BufferedImage getResize(BufferedImage image, Dimension size) {
		return GraphicsUtils.getResize(image, (int) size.getWidth(), (int) size
				.getHeight());
	}

	/**
	 * ??????????????
	 * 
	 * @param image
	 * @param size
	 * @return
	 */
	public static BufferedImage matchBufferedImage(BufferedImage image,
			Dimension size) {
		return GraphicsUtils.matchBufferedImage(image, (int) size.getWidth(),
				(int) size.getHeight());
	}

	/**
	 * ??????????????
	 * 
	 * @param image
	 * @param w
	 * @param h
	 * @return
	 */
	public static BufferedImage matchBufferedImage(BufferedImage image, int w,
			int h) {
		BufferedImage result = null;
		Graphics2D graphics2d;
		(graphics2d = (result = GraphicsUtils.createImage(w, h, true))
				.createGraphics()).setRenderingHint(
				RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2d.drawImage(image, 0, 0, null);
		graphics2d.dispose();
		graphics2d = null;
		return result;
	}

	/**
	 * ????Image??
	 * 
	 * @param image
	 * @param w
	 * @param h
	 * @return
	 */
	public static Image getResize(Image image, int w, int h) {
		if (image.getWidth(null) == w && image.getHeight(null) == h) {
			return image;
		}
		BufferedImage result = null;
		Graphics2D graphics2d;
		(graphics2d = (result = GraphicsUtils.createImage(w, h, true))
				.createGraphics()).setRenderingHint(
				RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2d.drawImage(image, 0, 0, w, h, 0, 0, image.getWidth(null),
				image.getHeight(null), null);
		graphics2d.dispose();
		graphics2d = null;
		return result;
	}

	// ???????
	final static private RenderingHints VALUE_TEXT_ANTIALIAS_ON = new RenderingHints(
			RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

	// ???????
	final static private RenderingHints VALUE_TEXT_ANTIALIAS_OFF = new RenderingHints(
			RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

	// ?????
	final static private RenderingHints VALUE_ANTIALIAS_ON = new RenderingHints(
			RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	// ?????
	final static private RenderingHints VALUE_ANTIALIAS_OFF = new RenderingHints(
			RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

	/**
	 * ???????
	 * 
	 * @param g
	 * @param smooth
	 * @param antialiasing
	 */
	public static void setRenderingHints(Graphics g, boolean smooth,
			boolean antialiasing) {
		if (smooth) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		} else {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		}
		if (antialiasing) {
			((Graphics2D) g).setRenderingHint(
					RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		} else {
			((Graphics2D) g).setRenderingHint(
					RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
	}

	/**
	 * ?????????
	 * 
	 * @param g
	 * @param flag
	 */
	public static void setAntialias(Graphics g, boolean flag) {
		if (flag) {
			((Graphics2D) g).setRenderingHints(VALUE_TEXT_ANTIALIAS_ON);
		} else {
			((Graphics2D) g).setRenderingHints(VALUE_TEXT_ANTIALIAS_OFF);
		}
	}

	/**
	 * ???????
	 * 
	 * @param g
	 * @param flag
	 */
	public static void setAntialiasAll(Graphics g, boolean flag) {
		if (flag) {
			((Graphics2D) g).setRenderingHints(VALUE_ANTIALIAS_ON);
		} else {
			((Graphics2D) g).setRenderingHints(VALUE_ANTIALIAS_OFF);
		}
	}

	/**
	 * ???????BufferedImage[]
	 * 
	 * @param image
	 * @param col
	 * @param row
	 * @return
	 */
	public static BufferedImage[] getSplitBufferedImages(BufferedImage image,
			int row, int col) {
		int width = image.getWidth(), height = image.getHeight();
		if (row == width && col == height) {
			return new BufferedImage[] { image };
		}
		int frame = 0;
		int wlength = image.getWidth() / row;
		int hlength = image.getHeight() / col;
		int total = wlength * hlength;
		int transparency = image.getColorModel().getTransparency();
		BufferedImage[] images = GraphicsUtils.createImage(total, row, col,
				transparency);
		for (int y = 0; y < hlength; y++) {
			for (int x = 0; x < wlength; x++) {
				Graphics2D g = images[frame].createGraphics();
				g.drawImage(image, 0, 0, row, col, (x * row), (y * col), row
						+ (x * row), col + (y * col), null);
				g.dispose();
				g = null;
				frame++;
			}
		}
		return images;
	}

	/**
	 * ???????BufferedImage[]
	 * 
	 * @param fileName
	 * @param col
	 * @param row
	 * @return
	 */
	public static BufferedImage[] getSplitBufferedImages(String fileName,
			int row, int col) {
		return GraphicsUtils.getSplitBufferedImages(
				getBufferImage(GraphicsUtils.loadImage(fileName)), row, col);
	}

	/**
	 * ?????????
	 * 
	 * @param fileName
	 * @param row
	 * @param col
	 * @return
	 */
	public static Image[] getSplitImages(String fileName, int row, int col) {
		return getSplitImages(fileName, row, col, true);
	}

	/**
	 * ?????????
	 * 
	 * @param fileName
	 * @param row
	 * @param col
	 * @return
	 */
	public static Image[] getSplitImages(String fileName, int row, int col,
			boolean isFiltrate) {
		Image image = GraphicsUtils.loadImage(fileName);
		return getSplitImages(image, row, col, isFiltrate);
	}

	/**
	 * ???????image[]
	 * 
	 * @param image
	 * @param row
	 * @param col
	 * @return
	 */
	public static Image[] getSplitImages(Image image, int row, int col,
			boolean isFiltrate) {
		int index = 0;
		int wlength = image.getWidth(null) / row;
		int hlength = image.getHeight(null) / col;
		int l = wlength * hlength;
		Image[] abufferedimage = new Image[l];
		for (int y = 0; y < hlength; y++) {
			for (int x = 0; x < wlength; x++) {
				abufferedimage[index] = GraphicsUtils.createImage(row, col,
						true);
				Graphics g = abufferedimage[index].getGraphics();
				g.drawImage(image, 0, 0, row, col, (x * row), (y * col), row
						+ (x * row), col + (y * col), null);
				g.dispose();
				g = null;
				PixelGrabber pgr = new PixelGrabber(abufferedimage[index], 0,
						0, -1, -1, true);
				try {
					pgr.grabPixels();
				} catch (InterruptedException ex) {
				}
				int pixels[] = (int[]) pgr.getPixels();
				if (isFiltrate) {
					for (int i = 0; i < pixels.length; i++) {
						LColor color = LColor.getLColor(pixels[i]);
						if ((color.R == 247 && color.G == 0 && color.B == 255)
								|| (color.R == 255 && color.G == 255 && color.B == 255)) {
							pixels[i] = 0;
						}
					}
				}
				ImageProducer ip = new MemoryImageSource(pgr.getWidth(), pgr
						.getHeight(), pixels, 0, pgr.getWidth());
				abufferedimage[index] = toolKit.createImage(ip);
				index++;
			}
		}
		return abufferedimage;
	}

	/**
	 * ?????????
	 * 
	 * @param fileName
	 * @param row
	 * @param col
	 * @return
	 */
	public static Image[][] getSplit2Images(String fileName, int row, int col,
			boolean isFiltrate) {
		String keyName = (fileName + row + col + isFiltrate).intern()
				.toLowerCase().trim();
		if (lazySplitMap.size() > LSystem.DEFAULT_MAX_CACHE_SIZE / 3) {
			lazySplitMap.clear();
			System.gc();
		}
		Object objs = lazySplitMap.get(keyName);
		if (objs == null) {
			Image image = GraphicsUtils.loadImage(fileName);
			objs = getSplit2Images(image, row, col, isFiltrate);
			lazySplitMap.put(keyName, objs);
		}
		return (Image[][]) objs;
	}

	/**
	 * ???????image[][]
	 * 
	 * @param fileName
	 * @param row
	 * @param col
	 * @return
	 */
	public static Image[][] getSplit2Images(String fileName, int row, int col) {
		return getSplit2Images(fileName, row, col, false);
	}

	/**
	 * ???????image[]
	 * 
	 * @param image
	 * @param row
	 * @param col
	 * @return
	 */
	public static Image[][] getSplit2Images(Image image, int row, int col,
			boolean isFiltrate) {
		int wlength = image.getWidth(null) / row;
		int hlength = image.getHeight(null) / col;
		Image[][] abufferedimage = new Image[row][col];
		for (int y = 0; y < hlength; y++) {
			for (int x = 0; x < wlength; x++) {
				abufferedimage[x][y] = GraphicsUtils
						.createImage(row, col, true);
				Graphics g = abufferedimage[x][y].getGraphics();
				g.drawImage(image, 0, 0, row, col, (x * row), (y * col), row
						+ (x * row), col + (y * col), null);
				g.dispose();
				g = null;
				PixelGrabber pgr = new PixelGrabber(abufferedimage[x][y], 0, 0,
						-1, -1, true);
				try {
					pgr.grabPixels();
				} catch (InterruptedException ex) {
					ex.getStackTrace();
				}
				int pixels[] = (int[]) pgr.getPixels();
				if (isFiltrate) {
					for (int i = 0; i < pixels.length; i++) {
						LColor color = LColor.getLColor(pixels[i]);
						if ((color.R == 247 && color.G == 0 && color.B == 255)
								|| (color.R == 255 && color.G == 0 && color.B == 255)
								|| (color.R == 0 && color.G == 0 && color.B == 0)) {
							pixels[i] = 0;
						}
					}
				}
				ImageProducer ip = new MemoryImageSource(pgr.getWidth(), pgr
						.getHeight(), pixels, 0, pgr.getWidth());
				abufferedimage[x][y] = toolKit.createImage(ip);
			}
		}
		return abufferedimage;
	}

	/**
	 * ??????????
	 * 
	 * @param pixels
	 * @return
	 */
	public static Image[][] getFlipHorizintalImage2D(Image[][] pixels) {
		int w = pixels.length;
		int h = pixels[0].length;
		Image pixel[][] = new Image[h][w];
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				pixel[i][j] = pixels[j][i];
			}
		}
		return pixel;
	}

	/**
	 * ??????
	 * 
	 * @param image
	 * @param objectWidth
	 * @param objectHeight
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static BufferedImage drawClipImage(final Image image,
			int objectWidth, int objectHeight, int x1, int y1, int x2, int y2) {
		BufferedImage buffer = GraphicsUtils.createImage(objectWidth,
				objectHeight, true);
		Graphics g = buffer.getGraphics();
		Graphics2D graphics2D = (Graphics2D) g;
		graphics2D.drawImage(image, 0, 0, objectWidth, objectHeight, x1, y1,
				x2, y2, null);
		graphics2D.dispose();
		graphics2D = null;
		return buffer;
	}

	/**
	 * ??????
	 * 
	 * @param image
	 * @param objectWidth
	 * @param objectHeight
	 * @param x
	 * @param y
	 * @return
	 */
	public static BufferedImage drawClipImage(final Image image,
			int objectWidth, int objectHeight, int x, int y) {
		BufferedImage buffer = GraphicsUtils.createImage(objectWidth,
				objectHeight, true);
		Graphics2D graphics2D = buffer.createGraphics();
		graphics2D.drawImage(image, 0, 0, objectWidth, objectHeight, x, y, x
				+ objectWidth, objectHeight + y, null);
		graphics2D.dispose();
		graphics2D = null;
		return buffer;
	}

	/**
	 * ????????
	 * 
	 * @return
	 */
	public static BufferedImage rotateImage(final BufferedImage image) {
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage img;
		Graphics2D graphics2d;
		(graphics2d = (img = new BufferedImage(w, h, image.getColorModel()
				.getTransparency())).createGraphics()).drawImage(image, 0, 0,
				w, h, w, 0, 0, h, null);
		graphics2d.dispose();
		return img;
	}

	/**
	 * ????????
	 * 
	 * @param image
	 * @return
	 */
	public static BufferedImage rotateImage(final Image image) {
		return GraphicsUtils.rotateImage(GraphicsUtils.getBufferImage(image));
	}

	/**
	 * ?????????
	 * 
	 * @param degree
	 * @return
	 */
	public static BufferedImage rotateImage(final Image image,
			final int angdeg, final boolean d) {
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		BufferedImage img;
		Graphics2D graphics2d;
		(graphics2d = (img = GraphicsUtils.createImage(w, h, true))
				.createGraphics()).setRenderingHint(
				RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2d.rotate(d ? -Math.toRadians(angdeg) : Math.toRadians(angdeg),
				w / 2, h / 2);
		graphics2d.drawImage(image, 0, 0, null);
		graphics2d.dispose();
		return img;
	}

	/**
	 * ???????????
	 * 
	 * @param g
	 */
	public static void setPoorRenderingHints(final Graphics2D g) {
		g.addRenderingHints(hints_poor);
	}

	/**
	 * ???????????
	 * 
	 * @param g
	 */
	public static void setExcellentRenderingHints(final Graphics2D g) {
		g.addRenderingHints(hints_excellent);
	}

	/**
	 * ???????????
	 * 
	 * @param g
	 */
	public static void setGeneralRenderingHints(final Graphics2D g) {
		g.addRenderingHints(hints_general);
	}

	/**
	 * ???????3D????
	 * 
	 * @param g
	 * @param rect
	 * @param back
	 * @param down
	 */
	public static void draw3DRect(Graphics g, Rectangle rect, Color back,
			boolean down) {
		int x1 = rect.x;
		int y1 = rect.y;
		int x2 = rect.x + rect.width - 1;
		int y2 = rect.y + rect.height - 1;
		if (!down) {
			g.setColor(back);
			g.drawLine(x1, y1, x1, y2);
			g.drawLine(x1, y1, x2, y2);
			g.setColor(back.brighter());
			g.drawLine(x1 + 1, y1 + 1, x1 + 1, y2 - 1);
			g.drawLine(x1 + 1, y1 + 1, x2 - 1, y1 + 1);
			g.setColor(Color.black);
			g.drawLine(x1, y2, x2, y2);
			g.drawLine(x2, y1, x2, y2);
			g.setColor(back.darker());
			g.drawLine(x1 + 1, y2 - 1, x2 - 1, y2 - 1);
			g.drawLine(x2 - 1, y1 + 2, x2 - 1, y2 - 1);
		} else {
			g.setColor(Color.black);
			g.drawLine(x1, y1, x1, y2);
			g.drawLine(x1, y1, x2, y1);
			g.setColor(back.darker());
			g.drawLine(x1 + 1, y1 + 1, x1 + 1, y2 - 1);
			g.drawLine(x1 + 1, y1 + 1, x2 - 1, y1 + 1);
			g.setColor(back.brighter());
			g.drawLine(x1, y2, x2, y2);
			g.drawLine(x2, y1, x2, y2);
			g.setColor(back);
			g.drawLine(x1 + 1, y2 - 1, x2 - 1, y2 - 1);
			g.drawLine(x2 - 1, y1 + 2, x2 - 1, y2 - 1);
		}
	}

	/**
	 * ???????????????????????????
	 * 
	 * @param g
	 * @param document
	 * @param w
	 * @param h
	 * @param font
	 * @param color
	 * @param hAlignment
	 * @param vAlignment
	 * @param inset
	 * @param vSpace
	 */
	public static void drawString(Graphics2D g, String[] document, int w,
			int h, LFont font, Color color, Integer hAlignment,
			Integer vAlignment, Insets inset, Integer vSpace) {
		if (inset == null) {
			inset = GraphicsUtils.NULL_INSETS;
		}
		if (hAlignment == null) {
			hAlignment = UIStatic.CENTER;
		}
		if (vAlignment == null) {
			vAlignment = UIStatic.CENTER;
		}
		if (vSpace == null) {
			vSpace = GraphicsUtils.VSPACE;
		}
		int space = vSpace.intValue();
		int height = (document.length * (font.getHeight() + space)) - space;

		int y = 0;
		if (vAlignment == UIStatic.TOP) {
			y = inset.top;
		} else if (vAlignment == UIStatic.BOTTOM) {
			y = h - inset.bottom - height;
		} else if (vAlignment == UIStatic.CENTER) {
			y = (h / 2) - (height / 2);
		}
		g.setColor(color);
		for (int i = 0; i < document.length; i++) {
			font.drawString(g, document[i], hAlignment.intValue(), inset.left,
					y, w - inset.left - inset.right);
			y += font.getHeight() + space;
		}
	}

	/**
	 * ??????
	 * 
	 * @param s
	 * @param graphics2D
	 * @param i
	 * @param j
	 * @param k
	 */
	public static void drawString(String message, Graphics2D graphics2D, int x,
			int y, int z) {
		Font font = graphics2D.getFont();
		int size = graphics2D.getFontMetrics(font).stringWidth(message);
		GraphicsUtils.setAlpha(graphics2D, 0.9f);
		graphics2D.drawString(message, x + (z - size) / 2, y);
		GraphicsUtils.setAlpha(graphics2D, 1.0f);
	}

	/**
	 * ??????
	 * 
	 * @param message
	 * @param graphics
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void drawString(String message, Graphics graphics, int x,
			int y, int z) {
		GraphicsUtils.drawString(message, (Graphics2D) graphics, x, y, z);
	}

	/**
	 * ?graphics?????
	 * 
	 * @param message
	 * @param fontName
	 * @param g
	 * @param x1
	 * @param y1
	 * @param style
	 * @param size
	 */
	public static void drawString(String message, String fontName,
			final Graphics g, int x1, int y1, int style, int size) {
		Graphics2D graphics2D = (Graphics2D) g;
		graphics2D.setFont(new Font(fontName, style, size));
		GraphicsUtils.setAlpha(g, 0.9f);
		graphics2D.drawString(message, x1, y1);
		GraphicsUtils.setAlpha(g, 1.0f);
	}

	/**
	 * ?????????????BufferedImage
	 * 
	 * @param shape
	 * @param c1
	 * @param c2
	 * @return
	 */
	public static BufferedImage createShapeImage(Shape shape, Color c1, Color c2) {
		Rectangle rect = shape.getBounds();
		BufferedImage image = GraphicsUtils.createImage(rect.width,
				rect.height, true);
		Graphics2D g = image.createGraphics();
		g.setColor(c1);
		g.fill(shape);
		g.setColor(c2);
		g.draw(shape);
		return image;
	}

	/**
	 * ?????????????
	 * 
	 * @param image
	 * @param fileName
	 * @param format
	 */
	public static void saveImage(BufferedImage image, File file, String format) {
		try {
			FileUtils.makedirs(file);
			ImageIO.write(image, format, file);
		} catch (IOException e) {
		}
	}

	/**
	 * ?????????????
	 * 
	 * @param image
	 * @param fileName
	 * @param format
	 */
	public static void saveImage(BufferedImage image, String fileName,
			String format) {
		saveImage(image, new File(fileName), format);
	}

	/**
	 * ?????????
	 * 
	 * @param image
	 * @param fileName
	 */
	public static void saveImage(BufferedImage image, String fileName) {
		saveImage(image, new File(fileName), "jpg");
	}

	/**
	 * ????????
	 * 
	 * @param image
	 * @return
	 */
	public static BufferedImage getGray(final BufferedImage image) {
		ImageFilter filter = new GrayFilter(true, 25);
		ImageProducer imageProducer = new FilteredImageSource(
				image.getSource(), filter);
		return getBufferImage(toolKit.createImage(imageProducer));
	}

	/**
	 * ????????
	 * 
	 * @param image
	 * @return
	 */
	public static Image getGray(final Image image) {
		ImageFilter filter = new GrayFilter(true, 25);
		ImageProducer imageProducer = new FilteredImageSource(getBufferImage(
				image).getSource(), filter);
		return toolKit.createImage(imageProducer);
	}

	/**
	 * ?Image??BufferImage
	 * 
	 * @param image
	 * @return
	 */
	public static BufferedImage getBufferImage(final Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}
		BufferedImage bufferimage = GraphicsUtils.createImage(image
				.getWidth(null), image.getHeight(null), true);
		Graphics2D g = bufferimage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return bufferimage;
	}

	/**
	 * ??????Image
	 * 
	 * @param fileName
	 * @return
	 */
	final static public Image loadFileImage(final String fileName) {
		return GraphicsUtils.loadImage(fileName, false);
	}

	/**
	 * ????file??Image
	 * 
	 * @param innerFileName
	 * @return
	 */
	final static public Image loadImage(final String innerFileName) {
		return GraphicsUtils.loadImage(innerFileName, true);
	}

	/**
	 * ????file??BufferedImage
	 * 
	 * @param innerFileName
	 * @return
	 */
	final static public BufferedImage loadBufferedImage(
			final String innerFileName) {
		return GraphicsUtils.getBufferImage(GraphicsUtils.loadImage(
				innerFileName, true));
	}

	/**
	 * ??byte[]?Image
	 * 
	 * @param bytes
	 * @return
	 */
	final static public Image loadImage(final byte[] bytes) {
		Image result = null;
		try {
			result = toolKit.createImage(bytes);
			waitImage(result);
		} catch (Exception e) {
			result = null;
		}
		return result;
	}

	final static public Image loadImage(final String name, final byte[] bytes) {
		if (cacheByteImages.size() > LSystem.DEFAULT_MAX_CACHE_SIZE) {
			cacheByteImages.clear();
			System.gc();
		}
		Image result = null;
		result = (Image) cacheByteImages.get(name);
		if (result == null) {
			try {
				result = toolKit.createImage(bytes);
				cacheByteImages.put(name, result);
				waitImage(result);
			} catch (Exception e) {
				result = null;
			}
		}
		return result;
	}

	/**
	 * ????file??Image
	 * 
	 * @param inputstream
	 * @return
	 */
	final static public Image loadImage(final String innerFileName,
			final boolean isInner) {
		if (innerFileName == null) {
			return null;
		}
		if (cacheImages.size() > LSystem.DEFAULT_MAX_CACHE_SIZE) {
			cacheImages.clear();
		}
		String tmp_file = innerFileName, innerName = StringUtils
				.replaceIgnoreCase(innerFileName, "\\", "/");
		String keyName = innerName.toLowerCase();
		Object imageReference = cacheImages.get(keyName);
		if (imageReference == null) {
			int read;
			boolean flag;
			byte[] bytes = null;
			File file_tmp = null;
			Image img_tmp = null;
			InputStream in = null;
			ByteArrayOutputStream os = null;
			try {
				os = new ByteArrayOutputStream(16384);
				if (isInner) {
					in = new DataInputStream(new BufferedInputStream(
							LSystem.classLoader.getResourceAsStream(innerName)));
					flag = true;
				} else {
					file_tmp = new File(tmp_file);
					flag = file_tmp.exists();
					if (flag) {
						in = new DataInputStream(new BufferedInputStream(
								new FileInputStream(file_tmp)));
					}
				}
				if (flag) {
					bytes = new byte[16384];
					while ((read = in.read(bytes)) >= 0) {
						os.write(bytes, 0, read);
					}
					bytes = os.toByteArray();
					img_tmp = toolKit.createImage(bytes);
				}
				cacheImages.put(keyName, imageReference = img_tmp);
				waitImage(img_tmp);
			} catch (Exception e) {
				if (!isInner) {
					imageReference = null;
				} else {
					return loadImage(innerFileName, false);
				}
			} finally {
				try {
					if (os != null) {
						os.flush();
						os = null;
					}
					if (in != null) {
						in.close();
						in = null;
					}
					img_tmp = null;
					bytes = null;
					tmp_file = null;
					file_tmp = null;
				} catch (IOException e) {
				}
			}
		}
		if (imageReference == null) {
			throw new RuntimeException(
					("File not found. ( " + innerName + " )").intern());
		}
		return (Image) imageReference;
	}

	/**
	 * ??????
	 * 
	 * @param image
	 */
	public static void waitImage(Image image) {
		if (image == null) {
			return;
		}
		MediaTracker mediaTracker = null;
		GameContext context = null;
		try {
			context = GameManager.getInstance().getContext();
			if (context != null) {
				mediaTracker = new MediaTracker(context.getView());
				mediaTracker.addImage(image, 0);
				if ((mediaTracker.statusID(0, true) & MediaTracker.ERRORED) != 0) {
					throw new Exception();
				}
			}
		} catch (Exception e) {
			if (mediaTracker != null) {
				mediaTracker.removeImage(image, 0);
				mediaTracker = null;
			}
		}
		waitImage(100, image);
	}

	/**
	 * ????image,??????
	 * 
	 * @param delay
	 * @param image
	 */
	private static void waitImage(int delay, Image image) {
		try {
			for (int i = 0; i < delay; i++) {
				if (toolKit.prepareImage(image, -1, -1, null)) {
					return;
				}
				Thread.sleep(delay);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * ??????????
	 * 
	 * @param innerName
	 * @return
	 */
	final static public BufferedImage loadDoubleFilterImage(
			final String innerName) {
		Image result = GraphicsUtils.loadImage(innerName);
		return GraphicsUtils.loadDoubleFilterImage(result, Math.round(result
				.getWidth(null) / 2), Math.round(result.getHeight(null)));
	}

	/**
	 * ??????????
	 * 
	 * @param innerName
	 * @param width
	 * @param height
	 * @return
	 */
	final static public BufferedImage loadFilterGameImage(
			final String innerName, final int width, final int height) {
		return GraphicsUtils.loadDoubleFilterImage(GraphicsUtils
				.loadImage(innerName), width, height);
	}

	/**
	 * ??????????
	 * 
	 * @param img
	 * @param width
	 * @param height
	 * @return
	 */
	final static public BufferedImage loadDoubleFilterImage(final Image img,
			final int width, final int height) {
		BufferedImage img1 = GraphicsUtils.drawClipImage(img, width, height, 0,
				0);
		BufferedImage img2 = GraphicsUtils.drawClipImage(img, width, height,
				width, 0);
		WritableRaster writableRaster1 = img1.getRaster();
		DataBuffer dataBuffer1 = writableRaster1.getDataBuffer();
		int[] basePixels1 = AWTDataBufferHelper.getDataInt(dataBuffer1);
		WritableRaster writableRaster2 = img2.getRaster();
		DataBuffer dataBuffer2 = writableRaster2.getDataBuffer();
		int[] basePixels2 = AWTDataBufferHelper.getDataInt(dataBuffer2);
		int length = basePixels2.length;
		for (int i = 0; i < length; i++) {
			if (basePixels2[i] >= LColor.getPixel(200, 200, 200)) {
				basePixels2[i] = 16777215;
			} else {
				basePixels2[i] = basePixels1[i];
			}
		}
		img1.flush();
		img1 = null;
		return img2;
	}

	final static public Image getImageCache(final String name) {
		return (Image) cacheImages.get(name);
	}

	final static public Image getImageByteCache(final String name) {
		return (Image) cacheByteImages.get(name);
	}

	/**
	 * ??????
	 * 
	 * @param ms
	 */
	final static public void wait(final int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ex) {
		}
	}

	/**
	 * ?????
	 * 
	 * @param g
	 * @param d
	 */
	final static public void setAlpha(Graphics g, double d) {
		AlphaComposite alphacomposite = AlphaComposite
				.getInstance(3, (float) d);
		((Graphics2D) g).setComposite(alphacomposite);
	}

	/**
	 * ?????
	 * 
	 * @param g2d
	 * @param d
	 */
	final static public void setAlpha(Graphics2D g2d, double d) {
		AlphaComposite alphacomposite = AlphaComposite
				.getInstance(3, (float) d);
		g2d.setComposite(alphacomposite);
	}

	/**
	 * ?????????
	 * 
	 * @param g2d
	 * @return
	 */
	final static public float getAlpha(Graphics2D g2d) {
		return ((AlphaComposite) g2d.getComposite()).getAlpha();
	}

	/**
	 * ????
	 * 
	 * @param image
	 * @param objectWidth
	 * @param objectHeight
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 * @throws Exception
	 */
	final static private Image getClipImage(final Image image, int objectWidth,
			int objectHeight, int x1, int y1, int x2, int y2) throws Exception {
		BufferedImage buffer = createImage(objectWidth, objectHeight, true);
		Graphics g = buffer.getGraphics();
		Graphics2D graphics2D = (Graphics2D) g;
		graphics2D.drawImage(image, 0, 0, objectWidth, objectHeight, x1, y1,
				x2, y2, null);
		graphics2D.dispose();
		graphics2D = null;
		return buffer;
	}

	/**
	 * ?????????
	 * 
	 * @param img
	 * @param width
	 * @return
	 */
	final static public Image[] getImageRows(Image img, int width) {
		int iWidth = img.getWidth(null);
		int iHeight = img.getHeight(null);
		int size = iWidth / width;
		Image[] imgs = new Image[size];
		for (int i = 1; i <= size; i++) {
			try {
				imgs[i - 1] = transparencyBlackColor(getClipImage(img, width,
						iHeight, width * (i - 1), 0, width * i, iHeight));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return imgs;
	}

	/**
	 * ??????????
	 * 
	 * @param img
	 * @return
	 */
	final static public Image transparencyBlackColor(final Image img) {
		int width = img.getWidth(null);
		int height = img.getHeight(null);
		PixelGrabber pg = new PixelGrabber(img, 0, 0, width, height, true);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int pixels[] = (int[]) pg.getPixels();
		int length = pixels.length;
		for (int i = 0; i < length; i++) {
			if (pixels[i] <= -11500000) {
				pixels[i] = 16777215;
			}
		}
		return toolKit.createImage(new MemoryImageSource(width, height, pixels,
				0, width));
	}

	/**
	 * ????????
	 * 
	 * @param img
	 * @param color
	 */
	final static public void transparencyColor(BufferedImage img, int color) {
		WritableRaster writableRaster = img.getRaster();
		DataBuffer dataBuffer = writableRaster.getDataBuffer();
		int[] basePixels = AWTDataBufferHelper.getDataInt(dataBuffer);
		int length = basePixels.length;
		for (int i = 0; i < length; i++) {
			if (basePixels[i] == color) {
				basePixels[i] = 16777215;
			}
		}
	}

	/**
	 * ??????????
	 * 
	 * @return
	 */
	public Image getClipboardImage() {
		Transferable transferable = Toolkit.getDefaultToolkit()
				.getSystemClipboard().getContents(null);
		if (transferable != null
				&& transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			// ?????Image???
			try {
				return (Image) transferable
						.getTransferData(DataFlavor.imageFlavor);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * ??image??
	 * 
	 */
	final static public void destroyImages() {
		lazySplitMap.clear();
		cacheImages.clear();
		System.gc();
	}

}
