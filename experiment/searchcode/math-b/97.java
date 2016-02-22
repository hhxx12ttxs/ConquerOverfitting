package com.matrixwing.me.util;


import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 *
 @brief
 *   <p><b>Image Utils</b></p>
 * 
 *  &nbsp;&nbsp;&nbsp;&nbsp;
 * 
 *  <p><center>COPYRIGHT (C) 2009, www.matrixwing.com All Rights Reserved.</center></p>
 *  @author TangJian
 *  @version matrixwing.util.1.0
 *  @see 
 *  @since 2010-2-4
 */


public class ImageUtil {
	public static final int TURN_LEFT = 1;
	public static final int TURN_RIGHT = 2;

	/*
	 * ????RGB?????????width*height???????
	 */
	public int[] getPixels(Image src) {
		int w = src.getWidth();
		int h = src.getHeight();
		int[] pixels = new int[w * h];
		src.getRGB(pixels, 0, w, 0, 0, w, h);
		return pixels;
	}

	/*
	 * ?pixels[]????????????????w???h
	 */
	public Image drawPixels(int[] pixels, int w, int h) {
		Image image = Image.createRGBImage(pixels, w, h, true);
		pixels = null;
		return image;
	}

	/*
	 * ??????destW ??????destH?????
	 */
	public Image effect_resizeImage(Image src, int destW, int destH) {
		int srcW = src.getWidth();
		int srcH = src.getHeight();

		int[] destPixels = new int[destW * destH];

		int[] srcPixels = getPixels(src);

		for (int destY = 0; destY < destH; ++destY) {
			for (int destX = 0; destX < destW; ++destX) {
				int srcX = (destX * srcW) / destW;
				int srcY = (destY * srcH) / destH;
				destPixels[destX + destY * destW] = srcPixels[srcX + srcY
						* srcW];
			}
		}

		return drawPixels(destPixels, destW, destH);
	}

	/*
	 * ??????????? contrast ????light ??
	 */
	public Image effect_light_contrast(Image src, double contrast, int light) {
		int srcW = src.getWidth();
		int srcH = src.getHeight();
		int[] srcPixels = getPixels(src);
		int r = 0;
		int g = 0;
		int b = 0;
		int a = 0;
		int argb;
		// ??y =ax+b a?????b???
		// int para_b = light - 127 * (light - 1);
		for (int i = 0; i < srcH; i++) {
			for (int ii = 0; ii < srcW; ii++) {
				argb = srcPixels[i * srcW + ii];
				a = ((argb & 0xff000000) >> 24); // alpha channel
				r = ((argb & 0x00ff0000) >> 16); // red channel
				g = ((argb & 0x0000ff00) >> 8); // green channel
				b = (argb & 0x000000ff); // blue channel
				r = (int) (r * contrast + light);
				g = (int) (g * contrast + light);
				b = (int) (b * contrast + light);

				/*
				 * r =(int)((r -127 ) * contrast + 127+para_b); g =(int)((g -127
				 * ) * contrast + 127+para_b); b =(int)((b -127 ) * contrast +
				 * 127+para_b);
				 */
				if (r > 255)
					r = 255;
				else if (r < 0)
					r = 0;
				if (g > 255)
					g = 255;
				else if (g < 0)
					g = 0;
				if (b > 255)
					b = 255;
				else if (b < 0)
					b = 0;
				srcPixels[i * srcW + ii] = ((a << 24) | (r << 16) | (g << 8) | b);

			}
		}
		return drawPixels(srcPixels, srcW, srcH);
	}

	/*
	 * ??????
	 */
	public Image effect_mirror(Image src) {
		int srcW = src.getWidth();
		int srcH = src.getHeight();
		int[] srcPixels = getPixels(src);
		int len;
		int temp;
		for (int i = 0; i < srcH; i++) {
			len = (i + 1) * srcW;
			for (int ii = 0; ii < srcW / 2; ii++) {
				temp = srcPixels[i * srcW + ii];
				srcPixels[i * srcW + ii] = srcPixels[len - 1 - ii];
				srcPixels[len - 1 - ii] = temp;
			}
		}
		return drawPixels(srcPixels, srcW, srcH);
	}

	/*
	 * ?????cut_xpos?cut_ypos ???????????cut_width?cut_height ???????
	 */
	public Image effect_cut(Image src, int cut_xpos, int cut_ypos,
			int cut_width, int cut_height) {
		int srcW = src.getWidth();
		int srcH = src.getHeight();
		int[] srcPixels = getPixels(src);
		int[] desPixels = new int[cut_width * cut_height];
		int argb;
		int num = 0;
		for (int i = 0; i < srcH; i++) {
			if (i >= cut_ypos && i < cut_height + cut_ypos) {
				for (int ii = 0; ii < srcW; ii++) {
					if (ii >= cut_xpos && ii < cut_width + cut_xpos) {
						desPixels[num] = srcPixels[i * srcW + ii];
						num++;

					}
				}
			}
		}
		return drawPixels(desPixels, cut_width, cut_height);
	}

	/*
	 * ??????src?image???????x_pos?y_pos???0,0,????????
	 */
	public Image effect_image_add_image(Image src, Image image, int x_pos,
			int y_pos) {
		Image temp = Image.createImage(src.getWidth(), src.getHeight());
		Graphics g = temp.getGraphics();
		// g.drawImage(src,x_pos,y_pos,Graphics.LEFT|Graphics.TOP);
		// g.drawImage(image,x_pos,y_pos,Graphics.LEFT|Graphics.TOP);*/
		int alpha = 168;
		int[] srcRgbdata = new int[src.getWidth() * src.getHeight()];
		int[] desRgbdata = new int[image.getWidth() * image.getHeight()];
		src.getRGB(srcRgbdata, 0, src.getWidth(), 0, 0, src.getWidth(), src
				.getHeight());
		image.getRGB(desRgbdata, 0, image.getWidth(), 0, 0, image.getWidth(),
				image.getHeight());
		g.drawRGB(getTransImg(alpha, srcRgbdata, desRgbdata), 0,
				src.getWidth(), 0, 0, src.getWidth(), src.getHeight(), false);
		src = null;
		image = null;
		return temp;
	}

	/*
	 * ???????
	 */
	public Image effect_image_add_str(Image src, String str, int x_pos,
			int y_pos) {
		Image temp = Image.createImage(src.getWidth(), src.getHeight());
		Graphics g = temp.getGraphics();
		g.drawImage(src, 0, 0, Graphics.LEFT | Graphics.TOP);
		g.setColor(0x000000);
		g.drawString(str, x_pos, y_pos, Graphics.LEFT | Graphics.TOP);
		return temp;
	}

	/*
	 * ??????
	 */
	public Image effect_negative(Image src) {
		int srcW = src.getWidth();
		int srcH = src.getHeight();
		int[] srcPixels = getPixels(src);
		int r = 0;
		int g = 0;
		int b = 0;
		int a = 0;
		int argb;
		for (int i = 0; i < srcH; i++) {
			for (int ii = 0; ii < srcW; ii++) {
				argb = srcPixels[i * srcW + ii];
				a = ((argb & 0xff000000) >> 24); // alpha channel
				r = 255 - ((argb & 0x00ff0000) >> 16); // red channel
				g = 255 - ((argb & 0x0000ff00) >> 8); // green channel
				b = 255 - (argb & 0x000000ff); // blue channel
				srcPixels[i * srcW + ii] = ((a << 24) | (r << 16) | (g << 8) | b);
			}
		}
		return drawPixels(srcPixels, srcW, srcH);

	}

	/*
	 * ??????
	 */
	public Image effect_black_white(Image src) {
		int srcW = src.getWidth();
		int srcH = src.getHeight();
		int[] srcPixels = getPixels(src);
		int r = 0;
		int g = 0;
		int b = 0;
		int a = 0;
		int argb;
		int temp;

		for (int i = 0; i < srcH; i++) {
			for (int ii = 0; ii < srcW; ii++) {
				argb = srcPixels[i * srcW + ii];
				a = ((argb & 0xff000000) >> 24); // alpha channel
				r = ((argb & 0x00ff0000) >> 16); // red channel
				g = ((argb & 0x0000ff00) >> 8); // green channel
				b = (argb & 0x000000ff); // blue channel
				temp = (int) (.299 * (double) r + .587 * (double) g + .114 * (double) b);
				r = temp;
				g = temp;
				b = temp;
				srcPixels[i * srcW + ii] = ((a << 24) | (r << 16) | (g << 8) | b);
			}
		}
		return drawPixels(srcPixels, srcW, srcH);

	}

	/*
	 * ???????
	 */
	public Image effect_crayon(Image src) {
		int srcW = src.getWidth();
		int srcH = src.getHeight();
		int[] srcPixels = getPixels(src);
		int r = 0;
		int g = 0;
		int b = 0;
		int a = 0;
		int argb;
		int r1 = 0;
		int g1 = 0;
		int b1 = 0;
		int a1 = 0;
		int r2 = 0;
		int g2 = 0;
		int b2 = 0;
		int a2 = 0;

		for (int i = 0; i < srcH; i++) {
			for (int ii = 0; ii < srcW; ii++) {
				argb = srcPixels[i * srcW + ii];
				a = ((argb & 0xff000000) >> 24); // alpha channel
				r = ((argb & 0x00ff0000) >> 16); // red channel
				g = ((argb & 0x0000ff00) >> 8); // green channel
				b = (argb & 0x000000ff); // blue channel
				if (i + 1 == srcH) {
					r1 = 0;
					g1 = 0;
					b1 = 0;
				} else {
					argb = srcPixels[(i + 1) * srcW + ii];
					// a1 = ((argb & 0xff000000) >> 24); // alpha channel
					r1 = ((argb & 0x00ff0000) >> 16); // red channel
					g1 = ((argb & 0x0000ff00) >> 8); // green channel
					b1 = (argb & 0x000000ff); // blue channel
				}
				if (ii + 1 == srcW) {
					r2 = 0;
					g2 = 0;
					b2 = 0;
				} else {
					argb = srcPixels[i * srcW + ii + 1];
					r2 = ((argb & 0x00ff0000) >> 16); // red channel
					g2 = ((argb & 0x0000ff00) >> 8); // green channel
					b2 = (argb & 0x000000ff); // blue channel
				}
				// rr1=(r1-r2)^2 rr2=(r1-r3)^2
				r = (int) Math
						.sqrt((double) (2 * (r - r1) * (r - r1) + (r - r2)
								* (r - r2)));
				g = (int) Math
						.sqrt((double) (2 * (g - g1) * (g - g1) + (g - g2)
								* (g - g2)));
				b = (int) Math
						.sqrt((double) (2 * (b - b1) * (b - b1) + (b - b2)
								* (b - b2)));
				r = 255 - r; // red channel
				g = 255 - g; // green channel
				b = 255 - b; // blue channel
				srcPixels[i * srcW + ii] = ((a << 24) | (r << 16) | (g << 8) | b);
			}
		}
		return drawPixels(srcPixels, srcW, srcH);
	}

	/*
	 * ??????
	 */
	public Image effect_hoodwink(Image src) {
		int srcW = src.getWidth();
		int srcH = src.getHeight();
		int[] srcPixels = getPixels(src);
		int r = 0;
		int g = 0;
		int b = 0;
		int a = 0;
		int argb;

		for (int i = 0; i < srcH; i++) {
			for (int ii = 0; ii < srcW; ii++) {
				argb = srcPixels[i * srcW + ii];
				a = ((argb & 0xff000000) >> 24); // alpha channel
				r = ((argb & 0x00ff0000) >> 16); // red channel
				g = ((argb & 0x0000ff00) >> 8); // green channel
				b = (argb & 0x000000ff); // blue channel
				r = (int) (.299 * (double) r);
				g = (int) (.587 * (double) g);
				b = (int) (.114 * (double) b);
				srcPixels[i * srcW + ii] = ((a << 24) | (r << 16) | (g << 8) | b);
			}
		}
		return drawPixels(srcPixels, srcW, srcH);

	}

	private int[] getTransImg(int alpha, int[] srcRgbdata, int[] desRgbdata) {
		int[] tempRgbData = new int[desRgbdata.length];

		int sr;
		int sg;
		int sb;
		int dr;
		int dg;
		int db;
		int tr;
		int tg;
		int tb;
		for (int i = 0; i < desRgbdata.length; i++) {
			sr = (srcRgbdata[i] & 0xff0000) >> 16;
			sg = (srcRgbdata[i] & 0xff00) >> 8;
			sb = srcRgbdata[i] & 0xff;
			dr = (desRgbdata[i] & 0xff0000) >> 16;
			dg = (desRgbdata[i] & 0xff00) >> 8;
			db = desRgbdata[i] & 0xff;
			tr = (sr * alpha + dr * (255 - alpha)) / 255;
			tg = (sg * alpha + dg * (255 - alpha)) / 255;
			tb = (sb * alpha + db * (255 - alpha)) / 255;
			tempRgbData[i] = (tr << 16) | (tg << 8) | tb;
		}
		return tempRgbData;
	}

	/*
	 * ?????
	 */
	public Image effect_rotate(Image src, int direction) {
		Sprite sprite = new Sprite(src);
		switch (direction) {
		case 1:
			sprite.setTransform(sprite.TRANS_ROT270);
			break;
		case 2:
			sprite.setTransform(sprite.TRANS_ROT90);
			break;
		}

		Image temp = Image.createImage(src.getHeight(), src.getWidth());
		Graphics g = temp.getGraphics();
		sprite.setPosition(0, 0);
		sprite.paint(g);
		return temp;
	}

	/*
	 * ??????
	 */
	public Image effect_neonLight(Image src) {
		int srcW = src.getWidth();
		int srcH = src.getHeight();
		int[] srcPixels = getPixels(src);
		int r = 0;
		int g = 0;
		int b = 0;
		int a = 0;
		int argb;
		int r1 = 0;
		int g1 = 0;
		int b1 = 0;
		int a1 = 0;
		int r2 = 0;
		int g2 = 0;
		int b2 = 0;
		int a2 = 0;

		for (int i = 0; i < srcH; i++) {
			for (int ii = 0; ii < srcW; ii++) {
				argb = srcPixels[i * srcW + ii];
				a = ((argb & 0xff000000) >> 24); // alpha channel
				r = ((argb & 0x00ff0000) >> 16); // red channel
				g = ((argb & 0x0000ff00) >> 8); // green channel
				b = (argb & 0x000000ff); // blue channel
				if (i + 1 == srcH) {
					r1 = 0;
					g1 = 0;
					b1 = 0;
				} else {
					argb = srcPixels[(i + 1) * srcW + ii];
					// a1 = ((argb & 0xff000000) >> 24); // alpha channel
					r1 = ((argb & 0x00ff0000) >> 16); // red channel
					g1 = ((argb & 0x0000ff00) >> 8); // green channel
					b1 = (argb & 0x000000ff); // blue channel
				}
				if (ii + 1 == srcW) {
					r2 = 0;
					g2 = 0;
					b2 = 0;
				} else {
					argb = srcPixels[i * srcW + ii + 1];
					r2 = ((argb & 0x00ff0000) >> 16); // red channel
					g2 = ((argb & 0x0000ff00) >> 8); // green channel
					b2 = (argb & 0x000000ff); // blue channel
				}
				// rr1=(r1-r2)^2 rr2=(r1-r3)^2
				r = (int) Math
						.sqrt((double) (2 * (r - r1) * (r - r1) + (r - r2)
								* (r - r2)));
				g = (int) Math
						.sqrt((double) (2 * (g - g1) * (g - g1) + (g - g2)
								* (g - g2)));
				b = (int) Math
						.sqrt((double) (2 * (b - b1) * (b - b1) + (b - b2)
								* (b - b2)));
				srcPixels[i * srcW + ii] = ((a << 24) | (r << 16) | (g << 8) | b);
			}
		}
		return drawPixels(srcPixels, srcW, srcH);
	}

}

