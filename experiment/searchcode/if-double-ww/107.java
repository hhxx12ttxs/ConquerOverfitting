package jagex.client;

import jagex.util;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

public class graphics implements ImageProducer, ImageObserver {

	public graphics(int w, int h, int size, Component component) {
		interlace = false;
		al = false;
		comp = component;
		rendh = h;
		rendw = w;
		maxwidth = width = w;
		maxheight = height = h;
		area = w * h;
		pixels = new int[w * h];
		ik = new int[size][];
		rk = new boolean[size];
		jk = new byte[size][];
		kk = new int[size][];
		lk = new int[size];
		mk = new int[size];
		pk = new int[size];
		qk = new int[size];
		nk = new int[size];
		ok = new int[size];
		if (w > 1 && h > 1 && component != null) {
			colmodel = new DirectColorModel(DataBuffer.TYPE_UNDEFINED, RED, GREEN, BLUE);
			int area = width * height;
			for (int i = 0; i < area; i++) {
				pixels[i] = 0;
			}
			img = component.createImage(this);
			completeimg();
			component.prepareImage(img, component);
			completeimg();
			component.prepareImage(img, component);
			completeimg();
			component.prepareImage(img, component);
		}
	}

	public synchronized void addConsumer(ImageConsumer imageconsumer) {
		imgconsumer = imageconsumer;
		imageconsumer.setDimensions(width, height);
		imageconsumer.setProperties(null);
		imageconsumer.setColorModel(colmodel);
		imageconsumer.setHints(14);
	}

	public synchronized boolean isConsumer(ImageConsumer imageconsumer) {
		return imgconsumer == imageconsumer;
	}

	public synchronized void removeConsumer(ImageConsumer imageconsumer) {
		if(imgconsumer == imageconsumer)
			imgconsumer = null;
	}

	public void startProduction(ImageConsumer imageconsumer) {
		addConsumer(imageconsumer);
	}

	public void requestTopDownLeftRightResend(ImageConsumer imageconsumer) {
		System.out.println("TDLR");
	}

	/**
	 * Completes the underlying image by transfering the newly
	 * rendered pixels, as well as letting the image know that this producer
	 * has completed rendering
	 */
	public synchronized void completeimg() {
		if (imgconsumer != null) {
			imgconsumer.setPixels(0, 0, width, height, colmodel, pixels, 0, width);
			imgconsumer.imageComplete(2);
		}
	}

	/**
	 * Sets the dimension of the graphics component
	 * 
	 * @param w The width
	 * @param h The height
	 */
	public synchronized void setdimension(int w, int h) {
		if (width > maxwidth)
			width = maxwidth;
		if (height > maxheight)
			height = maxheight;
		width = w;
		height = h;
		area = w * h;
	}

	/**
	 * Sets this producers rendering boundaries, it will not
	 * draw anything that is out of these dimensions
	 * 
	 * @param x The minimum x position to render
	 * @param y The minimum y position to render
	 * @param w The width
	 * @param h The height
	 */
	public void setrend(int x, int y, int w, int h) {
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		if (w > width)
			w = width;
		if (h > height)
			h = height;
		rendx = x;
		rendy = y;
		rendw = w;
		rendh = h;
	}

	/**
	 * Resets the rendering dimensions
	 */
	public void resetrend() {
		rendx = 0;
		rendy = 0;
		rendw = width;
		rendh = height;
	}

	/**
	 * Draws the final image onto a graphics object
	 * 
	 * @param g The graphics object to draw to
	 * @param x The x location to draw at
	 * @param y The y location to draw at
	 */
	public void drawimg(Graphics g, int x, int y) {
		completeimg();
		g.drawImage(img, x, y, this);
	}

	/**
	 * Clears the screen, setting the pixels black
	 */
	public void clear() {
		int area = width * height;
		if (!interlace) {
			for (int i = 0; i < area; i++)
				pixels[i] = 0;
			return;
		}
		int ptr = 0;
		for (int y = -height; y < 0; y += 2) {
			for (int x = -width; x < 0; x++) {
				pixels[ptr++] = 0;
			}
			ptr += width;
		}
	}

	/**
	 * Renders a transparent circle
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param rad The radius of the circle
	 * @param col The color of the circle
	 * @param trans The transparency of the circle (ranging from 0-256)
	 */
	public void drawcircle(int x, int y, int rad, int col, int trans) {
		int opacity = 256 - trans;
		int r = (col >> 16 & 0xff) * trans;
		int g = (col >> 8 & 0xff) * trans;
		int b = (col & 0xff) * trans;
		int miny = y - rad;
		if (miny < 0)
			miny = 0;
		int maxy = y + rad;
		if (maxy >= height)
			maxy = height - 1;
		byte skip = 1;
		if (interlace) {
			skip = 2;
			if ((miny & 1) != 0)
				miny++;
		}
		for(int yy = miny; yy <= maxy; yy += skip) {
			int idx = yy - y;
			int xrad = (int) Math.sqrt(rad * rad - idx * idx);
			int minx = x - xrad;
			if (minx < 0)
				minx = 0;
			int maxx = x + xrad;
			if (maxx >= width)
				maxx = width - 1;
			int ptr = minx + yy * width;
			for (int xx = minx; xx <= maxx; xx++) {
				int cr = (pixels[ptr] >> 16 & 0xff) * opacity;
				int cg = (pixels[ptr] >> 8 & 0xff) * opacity;
				int cb = (pixels[ptr] & 0xff) * opacity;
				int newcol = ((r + cr >> 8) << 16) + ((g + cg >> 8) << 8) + (b + cb >> 8);
				pixels[ptr++] = newcol;
			}
		}
	}

	/**
	 * Renders a transparent quadrilateral
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param w The width of the quad
	 * @param h The height of the quad
	 * @param col The color of the quad
	 * @param trans The transparency of the quad (ranging from 0-256)
	 */
	public void drawtransquad(int x, int y, int w, int h, int col, int trans) {
		if (x < rendx) {
			w -= rendx - x;
			x = rendx;
		}
		if (y < rendy) {
			h -= rendy - y;
			y = rendy;
		}
		if (x + w > rendw)
			w = rendw - x;
		if (y + h > rendh)
			h = rendh - y;
		int opacity = 256 - trans;
		int r = (col >> 16 & 0xff) * trans;
		int g = (col >> 8 & 0xff) * trans;
		int b = (col & 0xff) * trans;
		int ww = width - w;
		byte skip = 1;
		if (interlace) {
			skip = 2;
			ww += width;
			if ((y & 1) != 0) {
				y++;
				h--;
			}
		}
		int ptr = x + y * width;
		for(int yy = 0; yy < h; yy += skip) {
			for(int xx = -w; xx < 0; xx++) {
				int cr = (pixels[ptr] >> 16 & 0xff) * opacity;
				int cg = (pixels[ptr] >> 8 & 0xff) * opacity;
				int cb = (pixels[ptr] & 0xff) * opacity;
				int k4 = ((r + cr >> 8) << 16) + ((g + cg >> 8) << 8) + (b + cb >> 8);
				pixels[ptr++] = k4;
			}
			ptr += ww;
		}
	}

	/**
	 * Renders a vertical gradient
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param w The width of the quad
	 * @param h The height of the quad
	 * @param col1 The starting color of the gradient
	 * @param col2 The ending color of the gradient
	 */
	public void drawvertgradient(int x, int y, int w, int h, int col1, int col2) {
		if (x < rendx) {
			w -= rendx - x;
			x = rendx;
		}
		if (x + w > rendw)
			w = rendw - x;
		int rr = col2 >> 16 & 0xff;
		int gg = col2 >> 8 & 0xff;
		int bb = col2 & 0xff;
		int r = col1 >> 16 & 0xff;
		int g = col1 >> 8 & 0xff;
		int b = col1 & 0xff;
		int ww = width - w;
		byte skip = 1;
		if (interlace) {
			skip = 2;
			ww += width;
			if((y & 1) != 0) {
				y++;
				h--;
			}
		}
		int ptr = x + y * width;
		for (int yy = 0; yy < h; yy += skip) {
			if (yy + y >= rendy && yy + y < rendh) {
				int col = ((rr * yy + r * (h - yy)) / h << 16) + ((gg * yy + g * (h - yy)) / h << 8) + (bb * yy + b * (h - yy)) / h;
				for (int xx = -w; xx < 0; xx++) {
					pixels[ptr++] = col;
				}
				ptr += ww;
			} else {
				ptr += width;
			}
		}
	}

	/**
	 * Renders a quadrilateral
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param w The width of the quad
	 * @param h The height of the quad
	 * @param col The color of the quad
	 */
	public void drawquad(int x, int y, int w, int h, int col) {
		if (x < rendx) {
			w -= rendx - x;
			x = rendx;
		}
		if (y < rendy) {
			h -= rendy - y;
			y = rendy;
		}
		if (x + w > rendw)
			w = rendw - x;
		if (y + h > rendh)
			h = rendh - y;
		int ww = width - w;
		byte skip = 1;
		if (interlace) {
			skip = 2;
			ww += width;
			if((y & 1) != 0) {
				y++;
				h--;
			}
		}
		int ptr = x + y * width;
		for (int yy = -h; yy < 0; yy += skip) {
			for (int xx = -w; xx < 0; xx++)
				pixels[ptr++] = col;
			ptr += ww;
		}
	}

	/**
	 * Renders an outline of a quadrilateral
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param w The width of the quad
	 * @param h The height of the quad
	 * @param col The color of the quad
	 */
	public void drawquadout(int x, int y, int w, int h, int col) {
		drawhorline(x, y, w, col);
		drawhorline(x, (y + h) - 1, w, col);
		drawvertline(x, y, h, col);
		drawvertline((x + w) - 1, y, h, col);
	}

	/**
	 * Draws a horizontal line (across the x axis)
	 * 
	 * @param x The starting x coordinate
	 * @param y The y coordinate
	 * @param w The width of the line
	 * @param col The color of the line
	 */
	public void drawhorline(int x, int y, int w, int col) {
		if (y < rendy || y >= rendh)
			return;
		if (x < rendx) {
			w -= rendx - x;
			x = rendx;
		}
		if (x + w > rendw)
			w = rendw - x;
		int start = x + y * width;
		for (int i = 0; i < w; i++) {
			pixels[start + i] = col;
		}
	}

	/**
	 * Draws a vertical line (across the y axis)
	 * 
	 * @param x The x coordinate
	 * @param y The starting y coordinate
	 * @param h The height of the line
	 * @param col The color of the line
	 */
	public void drawvertline(int x, int y, int h, int col) {
		if (x < rendx || x >= rendw)
			return;
		if (y < rendy) {
			h -= rendy - y;
			y = rendy;
		}
		if (y + h > rendw)
			h = rendh - y;
		int start = x + y * width;
		for (int i = 0; i < h; i++) {
			pixels[start + i * width] = col;
		}
	}

	/**
	 * Draws a line from point a to point b, using the
	 * Bresenham algorithm
	 * 
	 * @param sx The start x of the line
	 * @param sy The start y of the line
	 * @param ex The end x of the line
	 * @param ey The end y of the line
	 * @param color The color of the line
	 */
	public void bresenham(int sx, int sy, int ex, int ey, int color) {
		boolean steep = Math.abs(ey - sy) > Math.abs(ex - sx);
		if (steep) {
			int tmp = sx;
			sx = sy;
			sy = tmp;
			tmp = ex;
			ex = ey;
			ey = tmp;
		}
		if (sx > ex) {
			int tmp = sx;
			sx = ex;
			ex = tmp;
			tmp = sy;
			sy = ey;
			ey = tmp;
		}
		int deltax = ex - sx;
		int deltay = Math.abs(ey - sy);
		int error = deltax / 2;
		int ystep;
		int y = sy;
		if (sy < ey) ystep = 1; else ystep = -1;

		for (int x = sx; x < ex; ++x) {
			if (steep)
				setpixel(y, x, color);
			else
				setpixel(x, y, color);

			error = error - deltay;
			if (error < 0) {
				y = y + ystep;
				error = error + deltax;
			}
		}
	}

	/**
	 * Sets a pixel to a specified color
	 * 
	 * @param x The x location of the pixel
	 * @param y The y location of the pixel
	 * @param color The pixels new color
	 */
	public void setpixel(int x, int y, int color) {
		if (x < rendx || y < rendy || x >= rendw || y >= rendh) {
			return;
		} else {
			pixels[x + y * width] = color;
		}
	}

	/**
	 * Gradually fades the pixels to black
	 */
	public void fadepixels() {
		int area = width * height;
		for (int i = 0; i < area; i++) {
			int c = pixels[i] & 0xffffff;
			pixels[i] = (c >>> 1 & 0x7f7f7f) + (c >>> 2 & 0x3f3f3f) + (c >>> 3 & 0x1f1f1f) + (c >>> 4 & 0xf0f0f);
		}
	}

	public void sg(int j, int k, int l, int i1, int j1, int k1)
	{
		for(int l1 = l; l1 < l + j1; l1++)
		{
			for(int i2 = i1; i2 < i1 + k1; i2++)
			{
				int j2 = 0;
				int k2 = 0;
				int l2 = 0;
				int i3 = 0;
				for(int j3 = l1 - j; j3 <= l1 + j; j3++)
					if(j3 >= 0 && j3 < width)
					{
						for(int k3 = i2 - k; k3 <= i2 + k; k3++)
							if(k3 >= 0 && k3 < height)
							{
								int l3 = pixels[j3 + width * k3];
								j2 += l3 >> 16 & 0xff;
							k2 += l3 >> 8 & 0xff;
			l2 += l3 & 0xff;
			i3++;
							}

					}

				pixels[l1 + width * i2] = (j2 / i3 << 16) + (k2 / i3 << 8) + l2 / i3;
			}

		}

	}

	/**
	 * Converts RGB values into it's integral representation
	 * 
	 * @param r The red value (0-256) 
	 * @param g The green value (0-256)
	 * @param b The blue value (0-256)
	 * @return The integral representation
	 */
	public static int rgbhash(int r, int g, int b) {
		return (r << 16) + (g << 8) + b;
	}

	public void jg() {
		for (int j = 0; j < ik.length; j++) {
			ik[j] = null;
			lk[j] = 0;
			mk[j] = 0;
			jk[j] = null;
			kk[j] = null;
		}
	}

	public void og(int j, byte abyte0[], byte[] arc, int k) {
		int l = util.g2(abyte0, 0);
		int i1 = util.g2(arc, l);
		l += 2;
		int j1 = util.g2(arc, l);
		l += 2;
		int k1 = arc[l++] & 0xff;
		int ai[] = new int[k1];
		ai[0] = 0xff00ff;
		for (int l1 = 0; l1 < k1 - 1; l1++) {
			ai[l1 + 1] = ((arc[l] & 0xff) << 16) + ((arc[l + 1] & 0xff) << 8) + (arc[l + 2] & 0xff);
			l += 3;
		}

		int i2 = 2;
		for (int j2 = j; j2 < j + k; j2++) {
			nk[j2] = arc[l++] & 0xff;
			ok[j2] = arc[l++] & 0xff;
			lk[j2] = util.g2(arc, l);
			l += 2;
			mk[j2] = util.g2(arc, l);
			l += 2;
			int k2 = arc[l++] & 0xff;
			int l2 = lk[j2] * mk[j2];
			jk[j2] = new byte[l2];
			kk[j2] = ai;
			pk[j2] = i1;
			qk[j2] = j1;
			ik[j2] = null;
			rk[j2] = false;
			if (nk[j2] != 0 || ok[j2] != 0)
				rk[j2] = true;
			if (k2 == 0) {
				for (int i3 = 0; i3 < l2; i3++) {
					jk[j2][i3] = abyte0[i2++];
					if (jk[j2][i3] == 0) {
						rk[j2] = true;
					}
				}
			} else if (k2 == 1) {
				for (int j3 = 0; j3 < lk[j2]; j3++) {
					for (int k3 = 0; k3 < mk[j2]; k3++) {
						jk[j2][j3 + k3 * lk[j2]] = abyte0[i2++];
						if (jk[j2][j3 + k3 * lk[j2]] == 0) {
							rk[j2] = true;
						}
					}	
				}
			}
		}
	}

	public void ig(byte abyte0[], int j, int k, boolean flag, int l, int i1, boolean flag1)
	{
		int j1 = (abyte0[13 + j] & 0xff) * 256 + (abyte0[12 + j] & 0xff);
		int k1 = (abyte0[15 + j] & 0xff) * 256 + (abyte0[14 + j] & 0xff);
		int l1 = -1;
		int ai[] = new int[256];
		for(int i2 = 0; i2 < 256; i2++)
		{
			ai[i2] = 0xff000000 + ((abyte0[j + 20 + i2 * 3] & 0xff) << 16) + ((abyte0[j + 19 + i2 * 3] & 0xff) << 8) + (abyte0[j + 18 + i2 * 3] & 0xff);
			if(ai[i2] == -65281)
				l1 = i2;
		}

		if(l1 == -1)
			flag = false;
		if(flag1 && flag)
			ai[l1] = ai[0];
		int j2 = j1 / l;
		int k2 = k1 / i1;
		int ai1[] = new int[j2 * k2];
		for(int l2 = 0; l2 < i1; l2++)
		{
			for(int i3 = 0; i3 < l; i3++)
			{
				int j3 = 0;
				for(int k3 = k2 * l2; k3 < k2 * (l2 + 1); k3++)
				{
					for(int l3 = j2 * i3; l3 < j2 * (i3 + 1); l3++)
						if(flag1)
							ai1[j3++] = abyte0[j + 786 + l3 + (k1 - k3 - 1) * j1] & 0xff;
						else
							ai1[j3++] = ai[abyte0[j + 786 + l3 + (k1 - k3 - 1) * j1] & 0xff];

				}

				if(flag1)
					hf(ai1, j2, k2, k++, flag, ai, l1);
				else
					hf(ai1, j2, k2, k++, flag, null, -65281);
			}

		}

	}

	private void hf(int ai[], int j, int k, int l, boolean flag, int ai1[], int i1)
	{
		int j1 = 0;
		int k1 = 0;
		int l1 = j;
		int i2 = k;
		if(flag)
		{
			label0:
				for(int j2 = 0; j2 < k; j2++)
				{
					for(int i3 = 0; i3 < j; i3++)
					{
						int i4 = ai[i3 + j2 * j];
						if(i4 == i1)
							continue;
						k1 = j2;
						break label0;
					}

				}

		label1:
			for(int j3 = 0; j3 < j; j3++)
			{
				for(int j4 = 0; j4 < k; j4++)
				{
					int j5 = ai[j3 + j4 * j];
					if(j5 == i1)
						continue;
					j1 = j3;
					break label1;
				}

			}

				label2:
					for(int k4 = k - 1; k4 >= 0; k4--)
					{
						for(int k5 = 0; k5 < j; k5++)
						{
							int k6 = ai[k5 + k4 * j];
							if(k6 == i1)
								continue;
							i2 = k4 + 1;
							break label2;
						}

					}

			label3:
				for(int l5 = j - 1; l5 >= 0; l5--)
				{
					for(int l6 = 0; l6 < k; l6++)
					{
						int i7 = ai[l5 + l6 * j];
						if(i7 == i1)
							continue;
						l1 = l5 + 1;
						break label3;
					}

				}

		}
		lk[l] = l1 - j1;
		mk[l] = i2 - k1;
		rk[l] = flag;
		nk[l] = j1;
		ok[l] = k1;
		pk[l] = j;
		qk[l] = k;
		if(ai1 == null)
		{
			ik[l] = new int[(l1 - j1) * (i2 - k1)];
			int k2 = 0;
			for(int k3 = k1; k3 < i2; k3++)
			{
				for(int l4 = j1; l4 < l1; l4++)
				{
					int i6 = ai[l4 + k3 * j];
					if(flag)
					{
						if(i6 == i1)
							i6 = 0;
						if(i6 == 0xff000000)
							i6 = 0xff010101;
					}
					ik[l][k2++] = i6 & 0xffffff;
				}

			}

			return;
		}
		jk[l] = new byte[(l1 - j1) * (i2 - k1)];
		kk[l] = ai1;
		int l2 = 0;
		for(int l3 = k1; l3 < i2; l3++)
		{
			for(int i5 = j1; i5 < l1; i5++)
			{
				int j6 = ai[i5 + l3 * j];
				if(flag)
					if(j6 == i1)
						j6 = 0;
					else
						if(j6 == 0)
							j6 = i1;
				jk[l][l2++] = (byte)j6;
			}

		}

	}

	public void fg(int j)
	{
		int k = lk[j] * mk[j];
		int ai[] = ik[j];
		int ai1[] = new int[32768];
		for(int l = 0; l < k; l++)
		{
			int i1 = ai[l];
			ai1[((i1 & 0xf80000) >> 9) + ((i1 & 0xf800) >> 6) + ((i1 & 0xf8) >> 3)]++;
		}

		int ai2[] = new int[256];
		ai2[0] = 0xff00ff;
		int ai3[] = new int[256];
		for(int j1 = 0; j1 < 32768; j1++)
		{
			int k1 = ai1[j1];
			if(k1 > ai3[255])
			{
				for(int l1 = 1; l1 < 256; l1++)
				{
					if(k1 <= ai3[l1])
						continue;
					for(int j2 = 255; j2 > l1; j2--)
					{
						ai2[j2] = ai2[j2 - 1];
						ai3[j2] = ai3[j2 - 1];
					}

					ai2[l1] = ((j1 & 0x7c00) << 9) + ((j1 & 0x3e0) << 6) + ((j1 & 0x1f) << 3) + 0x40404;
					ai3[l1] = k1;
					break;
				}

			}
			ai1[j1] = -1;
		}

		byte abyte0[] = new byte[k];
		for(int i2 = 0; i2 < k; i2++)
		{
			int k2 = ai[i2];
			int l2 = ((k2 & 0xf80000) >> 9) + ((k2 & 0xf800) >> 6) + ((k2 & 0xf8) >> 3);
			int i3 = ai1[l2];
			if(i3 == -1)
			{
				int j3 = 0x3b9ac9ff;
				int k3 = k2 >> 16 & 0xff;
					int l3 = k2 >> 8 & 0xff;
			int i4 = k2 & 0xff;
			for(int j4 = 0; j4 < 256; j4++)
			{
				int k4 = ai2[j4];
				int l4 = k4 >> 16 & 0xff;
			int i5 = k4 >> 8 & 0xff;
					int j5 = k4 & 0xff;
					int k5 = (k3 - l4) * (k3 - l4) + (l3 - i5) * (l3 - i5) + (i4 - j5) * (i4 - j5);
					if(k5 < j3)
					{
						j3 = k5;
						i3 = j4;
					}
			}

			ai1[l2] = i3;
			}
			abyte0[i2] = (byte)i3;
		}

		jk[j] = abyte0;
		kk[j] = ai2;
		ik[j] = null;
	}

	public void zg(int j)
	{
		if(jk[j] == null)
			return;
		int k = lk[j] * mk[j];
		byte abyte0[] = jk[j];
		int ai[] = kk[j];
		int ai1[] = new int[k];
		for(int l = 0; l < k; l++)
		{
			int i1 = ai[abyte0[l] & 0xff];
			if(i1 == 0)
				i1 = 1;
			else
				if(i1 == 0xff00ff)
					i1 = 0;
			ai1[l] = i1;
		}

		ik[j] = ai1;
		jk[j] = null;
		kk[j] = null;
	}

	public void kf(int j, int k, int l, int i1, int j1)
	{
		lk[j] = i1;
		mk[j] = j1;
		rk[j] = false;
		nk[j] = 0;
		ok[j] = 0;
		pk[j] = i1;
		qk[j] = j1;
		int k1 = i1 * j1;
		int l1 = 0;
		ik[j] = new int[k1];
		for(int i2 = k; i2 < k + i1; i2++)
		{
			for(int j2 = l; j2 < l + j1; j2++)
				ik[j][l1++] = pixels[i2 + j2 * width];

		}

	}

	public void rf(int j, int k, int l, int i1, int j1)
	{
		lk[j] = i1;
		mk[j] = j1;
		rk[j] = false;
		nk[j] = 0;
		ok[j] = 0;
		pk[j] = i1;
		qk[j] = j1;
		int k1 = i1 * j1;
		int l1 = 0;
		ik[j] = new int[k1];
		for(int i2 = l; i2 < l + j1; i2++)
		{
			for(int j2 = k; j2 < k + i1; j2++)
				ik[j][l1++] = pixels[j2 + i2 * width];

		}

	}

	public void xg(int j, int k, int l) {
		if(rk[l]) {
			j += nk[l];
			k += ok[l];
		}
		int i1 = j + k * width;
		int j1 = 0;
		int k1 = mk[l];
		int l1 = lk[l];
		int i2 = width - l1;
		int j2 = 0;
		if(k < rendy) {
			int k2 = rendy - k;
			k1 -= k2;
			k = rendy;
			j1 += k2 * l1;
			i1 += k2 * width;
		}
		if(k + k1 >= rendh)
			k1 -= ((k + k1) - rendh) + 1;
		if(j < rendx)
		{
			int l2 = rendx - j;
			l1 -= l2;
			j = rendx;
			j1 += l2;
			i1 += l2;
			j2 += l2;
			i2 += l2;
		}
		if(j + l1 >= rendw)
		{
			int i3 = ((j + l1) - rendw) + 1;
			l1 -= i3;
			j2 += i3;
			i2 += i3;
		}
		if(l1 <= 0 || k1 <= 0)
			return;
		byte byte0 = 1;
		if(interlace)
		{
			byte0 = 2;
			i2 += width;
			j2 += lk[l];
			if((k & 1) != 0)
			{
				i1 += width;
				k1--;
			}
		}
		if(ik[l] == null)
		{
			cg(pixels, jk[l], kk[l], j1, i1, l1, k1, i2, j2, byte0);
			return;
		} else
		{
			gf(pixels, ik[l], 0, j1, i1, l1, k1, i2, j2, byte0);
			return;
		}
	}

	public void nf(int j, int k, int l, int i1, int j1)
	{
		try
		{
			int k1 = lk[j1];
			int l1 = mk[j1];
			int i2 = 0;
			int j2 = 0;
			int k2 = (k1 << 16) / l;
			int l2 = (l1 << 16) / i1;
			if(rk[j1])
			{
				int i3 = pk[j1];
				int k3 = qk[j1];
				k2 = (i3 << 16) / l;
				l2 = (k3 << 16) / i1;
				j += ((nk[j1] * l + i3) - 1) / i3;
				k += ((ok[j1] * i1 + k3) - 1) / k3;
				if((nk[j1] * l) % i3 != 0)
					i2 = (i3 - (nk[j1] * l) % i3 << 16) / l;
				if((ok[j1] * i1) % k3 != 0)
					j2 = (k3 - (ok[j1] * i1) % k3 << 16) / i1;
				l = (l * (lk[j1] - (i2 >> 16))) / i3;
				i1 = (i1 * (mk[j1] - (j2 >> 16))) / k3;
			}
			int j3 = j + k * width;
			int l3 = width - l;
			if(k < rendy)
			{
				int i4 = rendy - k;
				i1 -= i4;
				k = 0;
				j3 += i4 * width;
				j2 += l2 * i4;
			}
			if(k + i1 >= rendh)
				i1 -= ((k + i1) - rendh) + 1;
			if(j < rendx)
			{
				int j4 = rendx - j;
				l -= j4;
				j = 0;
				j3 += j4;
				i2 += k2 * j4;
				l3 += j4;
			}
			if(j + l >= rendw)
			{
				int k4 = ((j + l) - rendw) + 1;
				l -= k4;
				l3 += k4;
			}
			byte byte0 = 1;
			if(interlace)
			{
				byte0 = 2;
				l3 += width;
				l2 += l2;
				if((k & 1) != 0)
				{
					j3 += width;
					i1--;
				}
			}
			plotscale(pixels, ik[j1], 0, i2, j2, j3, l3, l, i1, k2, l2, k1, byte0);
			return;
		}
		catch(Exception _ex)
		{
			System.out.println("error in sprite clipping routine");
		}
	}

	public void qg(int j, int k, int l, int i1)
	{
		if(rk[l])
		{
			j += nk[l];
			k += ok[l];
		}
		int j1 = j + k * width;
		int k1 = 0;
		int l1 = mk[l];
		int i2 = lk[l];
		int j2 = width - i2;
		int k2 = 0;
		if(k < rendy)
		{
			int l2 = rendy - k;
			l1 -= l2;
			k = rendy;
			k1 += l2 * i2;
			j1 += l2 * width;
		}
		if(k + l1 >= rendh)
			l1 -= ((k + l1) - rendh) + 1;
		if(j < rendx)
		{
			int i3 = rendx - j;
			i2 -= i3;
			j = rendx;
			k1 += i3;
			j1 += i3;
			k2 += i3;
			j2 += i3;
		}
		if(j + i2 >= rendw)
		{
			int j3 = ((j + i2) - rendw) + 1;
			i2 -= j3;
			k2 += j3;
			j2 += j3;
		}
		if(i2 <= 0 || l1 <= 0)
			return;
		byte byte0 = 1;
		if(interlace)
		{
			byte0 = 2;
			j2 += width;
			k2 += lk[l];
			if((k & 1) != 0)
			{
				j1 += width;
				l1--;
			}
		}
		if(ik[l] == null)
		{
			ch(pixels, jk[l], kk[l], k1, j1, i2, l1, j2, k2, byte0, i1);
			return;
		} else
		{
			gg(pixels, ik[l], 0, k1, j1, i2, l1, j2, k2, byte0, i1);
			return;
		}
	}

	public void pg(int j, int k, int l, int i1, int j1, int k1)
	{
		try
		{
			int l1 = lk[j1];
			int i2 = mk[j1];
			int j2 = 0;
			int k2 = 0;
			int l2 = (l1 << 16) / l;
			int i3 = (i2 << 16) / i1;
			if(rk[j1])
			{
				int j3 = pk[j1];
				int l3 = qk[j1];
				l2 = (j3 << 16) / l;
				i3 = (l3 << 16) / i1;
				j += ((nk[j1] * l + j3) - 1) / j3;
				k += ((ok[j1] * i1 + l3) - 1) / l3;
				if((nk[j1] * l) % j3 != 0)
					j2 = (j3 - (nk[j1] * l) % j3 << 16) / l;
				if((ok[j1] * i1) % l3 != 0)
					k2 = (l3 - (ok[j1] * i1) % l3 << 16) / i1;
				l = (l * (lk[j1] - (j2 >> 16))) / j3;
				i1 = (i1 * (mk[j1] - (k2 >> 16))) / l3;
			}
			int k3 = j + k * width;
			int i4 = width - l;
			if(k < rendy)
			{
				int j4 = rendy - k;
				i1 -= j4;
				k = 0;
				k3 += j4 * width;
				k2 += i3 * j4;
			}
			if(k + i1 >= rendh)
				i1 -= ((k + i1) - rendh) + 1;
			if(j < rendx)
			{
				int k4 = rendx - j;
				l -= k4;
				j = 0;
				k3 += k4;
				j2 += l2 * k4;
				i4 += k4;
			}
			if(j + l >= rendw)
			{
				int l4 = ((j + l) - rendw) + 1;
				l -= l4;
				i4 += l4;
			}
			byte byte0 = 1;
			if(interlace)
			{
				byte0 = 2;
				i4 += width;
				i3 += i3;
				if((k & 1) != 0)
				{
					k3 += width;
					i1--;
				}
			}
			transcale(pixels, ik[j1], 0, j2, k2, k3, i4, l, i1, l2, i3, l1, byte0, k1);
			return;
		}
		catch(Exception _ex)
		{
			System.out.println("error in sprite clipping routine");
		}
	}

	public void mg(int j, int k, int l, int i1, int j1, int k1)
	{
		try
		{
			int l1 = lk[j1];
			int i2 = mk[j1];
			int j2 = 0;
			int k2 = 0;
			int l2 = (l1 << 16) / l;
			int i3 = (i2 << 16) / i1;
			if(rk[j1])
			{
				int j3 = pk[j1];
				int l3 = qk[j1];
				l2 = (j3 << 16) / l;
				i3 = (l3 << 16) / i1;
				j += ((nk[j1] * l + j3) - 1) / j3;
				k += ((ok[j1] * i1 + l3) - 1) / l3;
				if((nk[j1] * l) % j3 != 0)
					j2 = (j3 - (nk[j1] * l) % j3 << 16) / l;
				if((ok[j1] * i1) % l3 != 0)
					k2 = (l3 - (ok[j1] * i1) % l3 << 16) / i1;
				l = (l * (lk[j1] - (j2 >> 16))) / j3;
				i1 = (i1 * (mk[j1] - (k2 >> 16))) / l3;
			}
			int k3 = j + k * width;
			int i4 = width - l;
			if(k < rendy)
			{
				int j4 = rendy - k;
				i1 -= j4;
				k = 0;
				k3 += j4 * width;
				k2 += i3 * j4;
			}
			if(k + i1 >= rendh)
				i1 -= ((k + i1) - rendh) + 1;
			if(j < rendx)
			{
				int k4 = rendx - j;
				l -= k4;
				j = 0;
				k3 += k4;
				j2 += l2 * k4;
				i4 += k4;
			}
			if(j + l >= rendw)
			{
				int l4 = ((j + l) - rendw) + 1;
				l -= l4;
				i4 += l4;
			}
			byte byte0 = 1;
			if(interlace)
			{
				byte0 = 2;
				i4 += width;
				i3 += i3;
				if((k & 1) != 0)
				{
					k3 += width;
					i1--;
				}
			}
			plotscale(pixels, ik[j1], 0, j2, k2, k3, i4, l, i1, l2, i3, l1, byte0, k1);
			return;
		}
		catch(Exception _ex)
		{
			System.out.println("error in sprite clipping routine");
		}
	}

	private void gf(int ai[], int ai1[], int j, int k, int l, int i1, int j1, 
			int k1, int l1, int i2)
	{
		int j2 = -(i1 >> 2);
		i1 = -(i1 & 3);
		for(int k2 = -j1; k2 < 0; k2 += i2)
		{
			for(int l2 = j2; l2 < 0; l2++)
			{
				j = ai1[k++];
				if(j != 0)
					ai[l++] = j;
				else
					l++;
				j = ai1[k++];
				if(j != 0)
					ai[l++] = j;
				else
					l++;
				j = ai1[k++];
				if(j != 0)
					ai[l++] = j;
				else
					l++;
				j = ai1[k++];
				if(j != 0)
					ai[l++] = j;
				else
					l++;
			}

			for(int i3 = i1; i3 < 0; i3++)
			{
				j = ai1[k++];
				if(j != 0)
					ai[l++] = j;
				else
					l++;
			}

			l += k1;
			k += l1;
		}

	}

	private void cg(int ai[], byte abyte0[], int ai1[], int j, int k, int l, int i1, 
			int j1, int k1, int l1)
	{
		int i2 = -(l >> 2);
		l = -(l & 3);
		for(int j2 = -i1; j2 < 0; j2 += l1)
		{
			for(int k2 = i2; k2 < 0; k2++)
			{
				byte byte0 = abyte0[j++];
				if(byte0 != 0)
					ai[k++] = ai1[byte0 & 0xff];
				else
					k++;
				byte0 = abyte0[j++];
				if(byte0 != 0)
					ai[k++] = ai1[byte0 & 0xff];
				else
					k++;
				byte0 = abyte0[j++];
				if(byte0 != 0)
					ai[k++] = ai1[byte0 & 0xff];
				else
					k++;
				byte0 = abyte0[j++];
				if(byte0 != 0)
					ai[k++] = ai1[byte0 & 0xff];
				else
					k++;
			}

			for(int l2 = l; l2 < 0; l2++)
			{
				byte byte1 = abyte0[j++];
				if(byte1 != 0)
					ai[k++] = ai1[byte1 & 0xff];
				else
					k++;
			}

			k += j1;
			j += k1;
		}

	}

	private void plotscale(int ai[], int ai1[], int j, int k, int l, int i1, int j1, 
			int k1, int l1, int i2, int j2, int k2, int l2)
	{
		try
		{
			int i3 = k;
			for(int j3 = -l1; j3 < 0; j3 += l2)
			{
				int k3 = (l >> 16) * k2;
				for(int l3 = -k1; l3 < 0; l3++)
				{
					j = ai1[(k >> 16) + k3];
					if(j != 0)
						ai[i1++] = j;
					else
						i1++;
					k += i2;
				}

				l += j2;
				k = i3;
				i1 += j1;
			}

			return;
		}
		catch(Exception _ex)
		{
			System.out.println("error in plot_scale");
		}
	}

	private void gg(int ai[], int ai1[], int j, int k, int l, int i1, int j1, 
			int k1, int l1, int i2, int j2)
	{
		int k2 = 256 - j2;
		for(int l2 = -j1; l2 < 0; l2 += i2)
		{
			for(int i3 = -i1; i3 < 0; i3++)
			{
				j = ai1[k++];
				if(j != 0)
				{
					int j3 = ai[l];
					ai[l++] = ((j & 0xff00ff) * j2 + (j3 & 0xff00ff) * k2 & 0xff00ff00) + ((j & 0xff00) * j2 + (j3 & 0xff00) * k2 & 0xff0000) >> 8;
				} else
				{
					l++;
				}
			}

			l += k1;
			k += l1;
		}

	}

	private void ch(int ai[], byte abyte0[], int ai1[], int j, int k, int l, int i1, 
			int j1, int k1, int l1, int i2)
	{
		int j2 = 256 - i2;
		for(int k2 = -i1; k2 < 0; k2 += l1)
		{
			for(int l2 = -l; l2 < 0; l2++)
			{
				int i3 = abyte0[j++];
				if(i3 != 0)
				{
					i3 = ai1[i3 & 0xff];
					int j3 = ai[k];
					ai[k++] = ((i3 & 0xff00ff) * i2 + (j3 & 0xff00ff) * j2 & 0xff00ff00) + ((i3 & 0xff00) * i2 + (j3 & 0xff00) * j2 & 0xff0000) >> 8;
				} else
				{
					k++;
				}
			}

			k += j1;
			j += k1;
		}

	}

	private void transcale(int ai[], int ai1[], int j, int k, int l, int i1, int j1, 
			int k1, int l1, int i2, int j2, int k2, int l2, int i3)
	{
		int j3 = 256 - i3;
		try
		{
			int k3 = k;
			for(int l3 = -l1; l3 < 0; l3 += l2)
			{
				int i4 = (l >> 16) * k2;
				for(int j4 = -k1; j4 < 0; j4++)
				{
					j = ai1[(k >> 16) + i4];
					if(j != 0)
					{
						int k4 = ai[i1];
						ai[i1++] = ((j & 0xff00ff) * i3 + (k4 & 0xff00ff) * j3 & 0xff00ff00) + ((j & 0xff00) * i3 + (k4 & 0xff00) * j3 & 0xff0000) >> 8;
					} else
					{
						i1++;
					}
					k += i2;
				}

				l += j2;
				k = k3;
				i1 += j1;
			}

			return;
		}
		catch(Exception _ex)
		{
			System.out.println("error in tran_scale");
		}
	}

	private void plotscale(int ai[], int ai1[], int j, int k, int l, int i1, int j1, 
			int k1, int l1, int i2, int j2, int k2, int l2, int i3)
	{
		int j3 = i3 >> 16 & 0xff;
			int k3 = i3 >> 8 & 0xff;
			int l3 = i3 & 0xff;
			try
			{
				int i4 = k;
				for(int j4 = -l1; j4 < 0; j4 += l2)
				{
					int k4 = (l >> 16) * k2;
					for(int l4 = -k1; l4 < 0; l4++)
					{
						j = ai1[(k >> 16) + k4];
						if(j != 0)
						{
							int i5 = j >> 16 & 0xff;
				int j5 = j >> 8 & 0xff;
			int k5 = j & 0xff;
			if(i5 == j5 && j5 == k5)
				ai[i1++] = ((i5 * j3 >> 8) << 16) + ((j5 * k3 >> 8) << 8) + (k5 * l3 >> 8);
			else
				ai[i1++] = j;
						} else
						{
							i1++;
						}
						k += i2;
					}

					l += j2;
					k = i4;
					i1 += j1;
				}

				return;
			}
			catch(Exception _ex)
			{
				System.out.println("error in plot_scale");
			}
	}

	public void of(int j, int k, int l, int i1, int j1)
	{
		int k1 = width;
		int l1 = height;
		if(bl == null)
		{
			bl = new int[512];
			for(int i2 = 0; i2 < 256; i2++)
			{
				bl[i2] = (int)(Math.sin((double)i2 * 0.02454369D) * 32768D);
				bl[i2 + 256] = (int)(Math.cos((double)i2 * 0.02454369D) * 32768D);
			}

		}
		int j2 = -pk[l] / 2;
		int k2 = -qk[l] / 2;
		if(rk[l])
		{
			j2 += nk[l];
			k2 += ok[l];
		}
		int l2 = j2 + lk[l];
		int i3 = k2 + mk[l];
		int j3 = l2;
		int k3 = k2;
		int l3 = j2;
		int i4 = i3;
		i1 &= 0xff;
		int j4 = bl[i1] * j1;
		int k4 = bl[i1 + 256] * j1;
		int l4 = j + (k2 * j4 + j2 * k4 >> 22);
		int i5 = k + (k2 * k4 - j2 * j4 >> 22);
		int j5 = j + (k3 * j4 + j3 * k4 >> 22);
		int k5 = k + (k3 * k4 - j3 * j4 >> 22);
		int l5 = j + (i3 * j4 + l2 * k4 >> 22);
		int i6 = k + (i3 * k4 - l2 * j4 >> 22);
		int j6 = j + (i4 * j4 + l3 * k4 >> 22);
		int k6 = k + (i4 * k4 - l3 * j4 >> 22);
		int l6 = i5;
		int i7 = i5;
		if(k5 < l6)
			l6 = k5;
		else
			if(k5 > i7)
				i7 = k5;
		if(i6 < l6)
			l6 = i6;
		else
			if(i6 > i7)
				i7 = i6;
		if(k6 < l6)
			l6 = k6;
		else
			if(k6 > i7)
				i7 = k6;
		if(l6 < rendy)
			l6 = rendy;
		if(i7 > rendh)
			i7 = rendh;
		if(cl == null || cl.length != l1 + 1)
		{
			cl = new int[l1 + 1];
			dl = new int[l1 + 1];
			el = new int[l1 + 1];
			fl = new int[l1 + 1];
			gl = new int[l1 + 1];
			hl = new int[l1 + 1];
		}
		for(int j7 = l6; j7 <= i7; j7++)
		{
			cl[j7] = 0x5f5e0ff;
			dl[j7] = 0xfa0a1f01;
		}

		int j8 = 0;
		int l8 = 0;
		int j9 = 0;
		int k9 = lk[l];
		int l9 = mk[l];
		j2 = 0;
		k2 = 0;
		j3 = k9 - 1;
		k3 = 0;
		l2 = k9 - 1;
		i3 = l9 - 1;
		l3 = 0;
		i4 = l9 - 1;
		if(k6 != i5)
		{
			j8 = (j6 - l4 << 8) / (k6 - i5);
			j9 = (i4 - k2 << 8) / (k6 - i5);
		}
		int k7;
		int l7;
		int i8;
		int i9;
		if(i5 > k6)
		{
			i8 = j6 << 8;
			i9 = i4 << 8;
			k7 = k6;
			l7 = i5;
		} else
		{
			i8 = l4 << 8;
			i9 = k2 << 8;
			k7 = i5;
			l7 = k6;
		}
		if(k7 < 0)
		{
			i8 -= j8 * k7;
			i9 -= j9 * k7;
			k7 = 0;
		}
		if(l7 > l1 - 1)
			l7 = l1 - 1;
		for(int i10 = k7; i10 <= l7; i10++)
		{
			cl[i10] = dl[i10] = i8;
			i8 += j8;
			el[i10] = fl[i10] = 0;
			gl[i10] = hl[i10] = i9;
			i9 += j9;
		}

		if(k5 != i5)
		{
			j8 = (j5 - l4 << 8) / (k5 - i5);
			l8 = (j3 - j2 << 8) / (k5 - i5);
		}
		int k8;
		if(i5 > k5)
		{
			i8 = j5 << 8;
			k8 = j3 << 8;
			k7 = k5;
			l7 = i5;
		} else
		{
			i8 = l4 << 8;
			k8 = j2 << 8;
			k7 = i5;
			l7 = k5;
		}
		if(k7 < 0)
		{
			i8 -= j8 * k7;
			k8 -= l8 * k7;
			k7 = 0;
		}
		if(l7 > l1 - 1)
			l7 = l1 - 1;
		for(int j10 = k7; j10 <= l7; j10++)
		{
			if(i8 < cl[j10])
			{
				cl[j10] = i8;
				el[j10] = k8;
				gl[j10] = 0;
			}
			if(i8 > dl[j10])
			{
				dl[j10] = i8;
				fl[j10] = k8;
				hl[j10] = 0;
			}
			i8 += j8;
			k8 += l8;
		}

		if(i6 != k5)
		{
			j8 = (l5 - j5 << 8) / (i6 - k5);
			j9 = (i3 - k3 << 8) / (i6 - k5);
		}
		if(k5 > i6)
		{
			i8 = l5 << 8;
			k8 = l2 << 8;
			i9 = i3 << 8;
			k7 = i6;
			l7 = k5;
		} else
		{
			i8 = j5 << 8;
			k8 = j3 << 8;
			i9 = k3 << 8;
			k7 = k5;
			l7 = i6;
		}
		if(k7 < 0)
		{
			i8 -= j8 * k7;
			i9 -= j9 * k7;
			k7 = 0;
		}
		if(l7 > l1 - 1)
			l7 = l1 - 1;
		for(int k10 = k7; k10 <= l7; k10++)
		{
			if(i8 < cl[k10])
			{
				cl[k10] = i8;
				el[k10] = k8;
				gl[k10] = i9;
			}
			if(i8 > dl[k10])
			{
				dl[k10] = i8;
				fl[k10] = k8;
				hl[k10] = i9;
			}
			i8 += j8;
			i9 += j9;
		}

		if(k6 != i6)
		{
			j8 = (j6 - l5 << 8) / (k6 - i6);
			l8 = (l3 - l2 << 8) / (k6 - i6);
		}
		if(i6 > k6)
		{
			i8 = j6 << 8;
			k8 = l3 << 8;
			i9 = i4 << 8;
			k7 = k6;
			l7 = i6;
		} else
		{
			i8 = l5 << 8;
			k8 = l2 << 8;
			i9 = i3 << 8;
			k7 = i6;
			l7 = k6;
		}
		if(k7 < 0)
		{
			i8 -= j8 * k7;
			k8 -= l8 * k7;
			k7 = 0;
		}
		if(l7 > l1 - 1)
			l7 = l1 - 1;
		for(int l10 = k7; l10 <= l7; l10++)
		{
			if(i8 < cl[l10])
			{
				cl[l10] = i8;
				el[l10] = k8;
				gl[l10] = i9;
			}
			if(i8 > dl[l10])
			{
				dl[l10] = i8;
				fl[l10] = k8;
				hl[l10] = i9;
			}
			i8 += j8;
			k8 += l8;
		}

		int i11 = l6 * k1;
		int ai[] = ik[l];
		for(int j11 = l6; j11 < i7; j11++)
		{
			int k11 = cl[j11] >> 8;
		int l11 = dl[j11] >> 8;
			if(l11 - k11 <= 0)
			{
				i11 += k1;
			} else
			{
				int i12 = el[j11] << 9;
				int j12 = ((fl[j11] << 9) - i12) / (l11 - k11);
				int k12 = gl[j11] << 9;
				int l12 = ((hl[j11] << 9) - k12) / (l11 - k11);
				if(k11 < rendx)
				{
					i12 += (rendx - k11) * j12;
					k12 += (rendx - k11) * l12;
					k11 = rendx;
				}
				if(l11 > rendw)
					l11 = rendw;
				if(!interlace || (j11 & 1) == 0)
					if(!rk[l])
						ag(pixels, ai, 0, i11 + k11, i12, k12, j12, l12, k11 - l11, k9);
					else
						eg(pixels, ai, 0, i11 + k11, i12, k12, j12, l12, k11 - l11, k9);
				i11 += k1;
			}
		}

	}

	private void ag(int ai[], int ai1[], int j, int k, int l, int i1, int j1, 
			int k1, int l1, int i2)
	{
		for(j = l1; j < 0; j++)
		{
			pixels[k++] = ai1[(l >> 17) + (i1 >> 17) * i2];
			l += j1;
			i1 += k1;
		}

	}

	private void eg(int ai[], int ai1[], int j, int k, int l, int i1, int j1, 
			int k1, int l1, int i2)
	{
		for(int j2 = l1; j2 < 0; j2++)
		{
			j = ai1[(l >> 17) + (i1 >> 17) * i2];
			if(j != 0)
				pixels[k++] = j;
			else
				k++;
			l += j1;
			i1 += k1;
		}

	}

	public void drawentity(int j, int k, int l, int i1, int j1, int k1, int l1)
	{
		nf(j, k, l, i1, j1);
	}

	public void wf(int j, int k, int l, int i1, int j1, int k1, int l1, 
			int i2, boolean flag)
	{
		try
		{
			if(k1 == 0)
				k1 = 0xffffff;
			if(l1 == 0)
				l1 = 0xffffff;
			int j2 = lk[j1];
			int k2 = mk[j1];
			int l2 = 0;
			int i3 = 0;
			int j3 = i2 << 16;
			int k3 = (j2 << 16) / l;
			int l3 = (k2 << 16) / i1;
			int i4 = -(i2 << 16) / i1;
			if(rk[j1])
			{
				int j4 = pk[j1];
				int l4 = qk[j1];
				k3 = (j4 << 16) / l;
				l3 = (l4 << 16) / i1;
				int k5 = nk[j1];
				int l5 = ok[j1];
				if(flag)
					k5 = j4 - lk[j1] - k5;
				j += ((k5 * l + j4) - 1) / j4;
				int i6 = ((l5 * i1 + l4) - 1) / l4;
				k += i6;
				j3 += i6 * i4;
				if((k5 * l) % j4 != 0)
					l2 = (j4 - (k5 * l) % j4 << 16) / l;
				if((l5 * i1) % l4 != 0)
					i3 = (l4 - (l5 * i1) % l4 << 16) / i1;
				l = ((((lk[j1] << 16) - l2) + k3) - 1) / k3;
				i1 = ((((mk[j1] << 16) - i3) + l3) - 1) / l3;
			}
			int k4 = k * width;
			j3 += j << 16;
			if(k < rendy)
			{
				int i5 = rendy - k;
				i1 -= i5;
				k = rendy;
				k4 += i5 * width;
				i3 += l3 * i5;
				j3 += i4 * i5;
			}
			if(k + i1 >= rendh)
				i1 -= ((k + i1) - rendh) + 1;
			int j5 = k4 / width & 1;
			if(!interlace)
				j5 = 2;
			if(l1 == 0xffffff)
			{
				if(ik[j1] != null)
					if(!flag)
					{
						dh(pixels, ik[j1], 0, l2, i3, k4, l, i1, k3, l3, j2, k1, j3, i4, j5);
						return;
					} else
					{
						dh(pixels, ik[j1], 0, (lk[j1] << 16) - l2 - 1, i3, k4, l, i1, -k3, l3, j2, k1, j3, i4, j5);
						return;
					}
				if(!flag)
				{
					_mthif(pixels, jk[j1], kk[j1], 0, l2, i3, k4, l, i1, k3, l3, j2, k1, j3, i4, j5);
					return;
				} else
				{
					_mthif(pixels, jk[j1], kk[j1], 0, (lk[j1] << 16) - l2 - 1, i3, k4, l, i1, -k3, l3, j2, k1, j3, i4, j5);
					return;
				}
			}
			if(ik[j1] != null)
				if(!flag)
				{
					bg(pixels, ik[j1], 0, l2, i3, k4, l, i1, k3, l3, j2, k1, l1, j3, i4, j5);
					return;
				} else
				{
					bg(pixels, ik[j1], 0, (lk[j1] << 16) - l2 - 1, i3, k4, l, i1, -k3, l3, j2, k1, l1, j3, i4, j5);
					return;
				}
			if(!flag)
			{
				bh(pixels, jk[j1], kk[j1], 0, l2, i3, k4, l, i1, k3, l3, j2, k1, l1, j3, i4, j5);
				return;
			} else
			{
				bh(pixels, jk[j1], kk[j1], 0, (lk[j1] << 16) - l2 - 1, i3, k4, l, i1, -k3, l3, j2, k1, l1, j3, i4, j5);
				return;
			}
		}
		catch(Exception _ex)
		{
			System.out.println("error in sprite clipping routine");
		}
	}

	private void dh(int ai[], int ai1[], int j, int k, int l, int i1, int j1, 
			int k1, int l1, int i2, int j2, int k2, int l2, int i3, 
			int j3)
	{
		int j4 = k2 >> 16 & 0xff;
				int k4 = k2 >> 8 & 0xff;
					int l4 = k2 & 0xff;
					try
					{
						int i5 = k;
						for(int j5 = -k1; j5 < 0; j5++)
						{
							int k5 = (l >> 16) * j2;
							int l5 = l2 >> 16;
					int i6 = j1;
					if(l5 < rendx)
					{
						int j6 = rendx - l5;
						i6 -= j6;
						l5 = rendx;
						k += l1 * j6;
					}
					if(l5 + i6 >= rendw)
					{
						int k6 = (l5 + i6) - rendw;
						i6 -= k6;
					}
					j3 = 1 - j3;
					if(j3 != 0)
					{
						for(int l6 = l5; l6 < l5 + i6; l6++)
						{
							j = ai1[(k >> 16) + k5];
							if(j != 0)
							{
								int k3 = j >> 16 & 0xff;
			int l3 = j >> 8 & 0xff;
				int i4 = j & 0xff;
				if(k3 == l3 && l3 == i4)
					ai[l6 + i1] = ((k3 * j4 >> 8) << 16) + ((l3 * k4 >> 8) << 8) + (i4 * l4 >> 8);
				else
					ai[l6 + i1] = j;
							}
							k += l1;
						}

					}
					l += i2;
					k = i5;
					i1 += width;
					l2 += i3;
						}

						return;
					}
					catch(Exception _ex)
					{
						System.out.println("error in transparent sprite plot routine");
					}
	}

	private void bg(int ai[], int ai1[], int j, int k, int l, int i1, int j1, 
			int k1, int l1, int i2, int j2, int k2, int l2, int i3, 
			int j3, int k3)
	{
		int k4 = k2 >> 16 & 0xff;
					int l4 = k2 >> 8 & 0xff;
					int i5 = k2 & 0xff;
					int j5 = l2 >> 16 & 0xff;
				int k5 = l2 >> 8 & 0xff;
				int l5 = l2 & 0xff;
				try
				{
					int i6 = k;
					for(int j6 = -k1; j6 < 0; j6++)
					{
						int k6 = (l >> 16) * j2;
						int l6 = i3 >> 16;
			int i7 = j1;
			if(l6 < rendx)
			{
				int j7 = rendx - l6;
				i7 -= j7;
				l6 = rendx;
				k += l1 * j7;
			}
			if(l6 + i7 >= rendw)
			{
				int k7 = (l6 + i7) - rendw;
				i7 -= k7;
			}
			k3 = 1 - k3;
			if(k3 != 0)
			{
				for(int l7 = l6; l7 < l6 + i7; l7++)
				{
					j = ai1[(k >> 16) + k6];
					if(j != 0)
					{
						int l3 = j >> 16 & 0xff;
			int i4 = j >> 8 & 0xff;
			int j4 = j & 0xff;
			if(l3 == i4 && i4 == j4)
				ai[l7 + i1] = ((l3 * k4 >> 8) << 16) + ((i4 * l4 >> 8) << 8) + (j4 * i5 >> 8);
			else
				if(l3 == 255 && i4 == j4)
					ai[l7 + i1] = ((l3 * j5 >> 8) << 16) + ((i4 * k5 >> 8) << 8) + (j4 * l5 >> 8);
				else
					ai[l7 + i1] = j;
					}
					k += l1;
				}

			}
			l += i2;
			k = i6;
			i1 += width;
			i3 += j3;
					}

					return;
				}
				catch(Exception _ex)
				{
					System.out.println("error in transparent sprite plot routine");
				}
	}

	private void _mthif(int ai[], byte abyte0[], int ai1[], int j, int k, int l, int i1, 
			int j1, int k1, int l1, int i2, int j2, int k2, int l2, 
			int i3, int j3)
	{
		int j4 = k2 >> 16 & 0xff;
			int k4 = k2 >> 8 & 0xff;
			int l4 = k2 & 0xff;
			try
			{
				int i5 = k;
				for(int j5 = -k1; j5 < 0; j5++)
				{
					int k5 = (l >> 16) * j2;
					int l5 = l2 >> 16;
			int i6 = j1;
			if(l5 < rendx)
			{
				int j6 = rendx - l5;
				i6 -= j6;
				l5 = rendx;
				k += l1 * j6;
			}
			if(l5 + i6 >= rendw)
			{
				int k6 = (l5 + i6) - rendw;
				i6 -= k6;
			}
			j3 = 1 - j3;
			if(j3 != 0)
			{
				for(int l6 = l5; l6 < l5 + i6; l6++)
				{
					j = abyte0[(k >> 16) + k5] & 0xff;
					if(j != 0)
					{
						j = ai1[j];
						int k3 = j >> 16 & 0xff;
			int l3 = j >> 8 & 0xff;
			int i4 = j & 0xff;
			if(k3 == l3 && l3 == i4)
				ai[l6 + i1] = ((k3 * j4 >> 8) << 16) + ((l3 * k4 >> 8) << 8) + (i4 * l4 >> 8);
			else
				ai[l6 + i1] = j;
					}
					k += l1;
				}

			}
			l += i2;
			k = i5;
			i1 += width;
			l2 += i3;
				}

				return;
			}
			catch(Exception _ex)
			{
				System.out.println("error in transparent sprite plot routine");
			}
	}

	private void bh(int ai[], byte abyte0[], int ai1[], int j, int k, int l, int i1, 
			int j1, int k1, int l1, int i2, int j2, int k2, int l2, 
			int i3, int j3, int k3)
	{
		int k4 = k2 >> 16 & 0xff;
			int l4 = k2 >> 8 & 0xff;
			int i5 = k2 & 0xff;
			int j5 = l2 >> 16 & 0xff;
			int k5 = l2 >> 8 & 0xff;
			int l5 = l2 & 0xff;
			try
			{
				int i6 = k;
				for(int j6 = -k1; j6 < 0; j6++)
				{
					int k6 = (l >> 16) * j2;
					int l6 = i3 >> 16;
			int i7 = j1;
			if(l6 < rendx)
			{
				int j7 = rendx - l6;
				i7 -= j7;
				l6 = rendx;
				k += l1 * j7;
			}
			if(l6 + i7 >= rendw)
			{
				int k7 = (l6 + i7) - rendw;
				i7 -= k7;
			}
			k3 = 1 - k3;
			if(k3 != 0)
			{
				for(int l7 = l6; l7 < l6 + i7; l7++)
				{
					j = abyte0[(k >> 16) + k6] & 0xff;
					if(j != 0)
					{
						j = ai1[j];
						int l3 = j >> 16 & 0xff;
			int i4 = j >> 8 & 0xff;
		int j4 = j & 0xff;
		if(l3 == i4 && i4 == j4)
			ai[l7 + i1] = ((l3 * k4 >> 8) << 16) + ((i4 * l4 >> 8) << 8) + (j4 * i5 >> 8);
		else
			if(l3 == 255 && i4 == j4)
				ai[l7 + i1] = ((l3 * j5 >> 8) << 16) + ((i4 * k5 >> 8) << 8) + (j4 * l5 >> 8);
			else
				ai[l7 + i1] = j;
					}
					k += l1;
				}

			}
			l += i2;
			k = i6;
			i1 += width;
			i3 += j3;
				}

				return;
			}
			catch(Exception _ex)
			{
				System.out.println("error in transparent sprite plot routine");
			}
	}

	/**
	 * Loads a font into this graphics object
	 * 
	 * @param data The font data
	 * @return The font id
	 */
	public static int loadfont(byte data[]) {
		fonts[fontptr] = data;
		return fontptr++;
	}

	public void yg(String s, int j, int k, int l, int i1) {
		drawstring(s, j - textwidth(s, l), k, l, i1);
	}

	public void ug(String s, int j, int k, int l, int i1) {
		drawstring(s, j - textwidth(s, l) / 2, k, l, i1);
	}

	public void centerpara(String s, int j, int k, int l, int i1, int j1) {
		try
		{
			int k1 = 0;
			byte abyte0[] = fonts[l];
			int l1 = 0;
			int i2 = 0;
			for(int j2 = 0; j2 < s.length(); j2++)
			{
				if(s.charAt(j2) == '@' && j2 + 4 < s.length() && s.charAt(j2 + 4) == '@')
					j2 += 4;
				else
					if(s.charAt(j2) == '~' && j2 + 4 < s.length() && s.charAt(j2 + 4) == '~')
						j2 += 4;
					else
						k1 += abyte0[fontchars[s.charAt(j2)] + 7];
				if(s.charAt(j2) == ' ')
					i2 = j2;
				if(k1 > j1)
				{
					if(i2 <= l1)
						i2 = j2;
					ug(s.substring(l1, i2), j, k, l, i1);
					k1 = 0;
					l1 = j2 = i2 + 1;
					k += textheight(l);
				}
			}

			if(k1 > 0)
			{
				ug(s.substring(l1), j, k, l, i1);
				return;
			}
		}
		catch(Exception exception)
		{
			System.out.println("centerpara: " + exception);
			exception.printStackTrace();
		}
	}

	public void drawstring(String s, int j, int k, int l, int i1)
	{
		try
		{
			byte abyte0[] = fonts[l];
			for(int j1 = 0; j1 < s.length(); j1++)
				if(s.charAt(j1) == '@' && j1 + 4 < s.length() && s.charAt(j1 + 4) == '@')
				{
					if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("red"))
						i1 = 0xff0000;
					else
						if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("lre"))
							i1 = 0xff9040;
						else
							if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("yel"))
								i1 = 0xffff00;
							else
								if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("gre"))
									i1 = 65280;
								else
									if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("blu"))
										i1 = 255;
									else
										if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("cya"))
											i1 = 65535;
										else
											if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("mag"))
												i1 = 0xff00ff;
											else
												if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("whi"))
													i1 = 0xffffff;
												else
													if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("bla"))
														i1 = 0;
													else
														if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("dre"))
															i1 = 0xc00000;
														else
															if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("ora"))
																i1 = 0xff9040;
															else
																if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("ran"))
																	i1 = (int)(Math.random() * 16777215D);
																else
																	if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("or1"))
																		i1 = 0xffb000;
																	else
																		if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("or2"))
																			i1 = 0xff7000;
																		else
																			if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("or3"))
																				i1 = 0xff3000;
																			else
																				if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("gr1"))
																					i1 = 0xc0ff00;
																				else
																					if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("gr2"))
																						i1 = 0x80ff00;
																					else
																						if(s.substring(j1 + 1, j1 + 4).equalsIgnoreCase("gr3"))
																							i1 = 0x40ff00;
					j1 += 4;
				} else
					if(s.charAt(j1) == '~' && j1 + 4 < s.length() && s.charAt(j1 + 4) == '~')
					{
						char c = s.charAt(j1 + 1);
						char c1 = s.charAt(j1 + 2);
						char c2 = s.charAt(j1 + 3);
						if(c >= '0' && c <= '9' && c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9')
							j = Integer.parseInt(s.substring(j1 + 1, j1 + 4));
						j1 += 4;
					} else
					{
						int k1 = fontchars[s.charAt(j1)];
						if(al && i1 != 0)
							tg(k1, j + 1, k, 0, abyte0);
						if(al && i1 != 0)
							tg(k1, j, k + 1, 0, abyte0);
						tg(k1, j, k, i1, abyte0);
						j += abyte0[k1 + 7];
					}

			return;
		}
		catch(Exception exception)
		{
			System.out.println("drawstring: " + exception);
			exception.printStackTrace();
			return;
		}
	}

	private void tg(int j, int k, int l, int col, byte[] font) {
		int j1 = k + font[j + 5];
		int k1 = l - font[j + 6];
		int xx = font[j + 3];
		int yy = font[j + 4];
		int fptr = font[j] * 16384 + font[j + 1] * 128 + font[j + 2];
		int dptr = j1 + k1 * width;
		int dw = width - xx;
		int fw = 0;
		if(k1 < rendy)
		{
			int j3 = rendy - k1;
			yy -= j3;
			k1 = rendy;
			fptr += j3 * xx;
			dptr += j3 * width;
		}
		if(k1 + yy >= rendh)
			yy -= ((k1 + yy) - rendh) + 1;
		if(j1 < rendx)
		{
			int k3 = rendx - j1;
			xx -= k3;
			j1 = rendx;
			fptr += k3;
			dptr += k3;
			fw += k3;
			dw += k3;
		}
		if(j1 + xx >= rendw)
		{
			int l3 = ((j1 + xx) - rendw) + 1;
			xx -= l3;
			fw += l3;
			dw += l3;
		}
		if(xx > 0 && yy > 0)
			plotletter(pixels, font, col, fptr, dptr, xx, yy, dw, fw);
	}

	/**
	 * Plots a letter
	 * 
	 * @param dest The destination of the letter
	 * @param font The font to use
	 * @param col The color of the letter
	 * @param fptr The pointer of the font
	 * @param dptr The pointer of the destination
	 * @param x The x coordinate of the letter
	 * @param y The y coordinate of the letter
	 * @param dw The width of the destination array
	 * @param fw The width of the font array
	 */
	private void plotletter(int[] dest, byte[] font, int col, int fptr, int dptr, int x, int y, int dw, int fw) {
		try {
			int minx = -(x >> 2);
			x = -(x & 3);
			for (int yy = -y; yy < 0; yy++) {
				for(int xx = minx; xx < 0; xx++) {
					if (font[fptr++] != 0)
						dest[dptr++] = col;
					else
						dptr++;

					if (font[fptr++] != 0)
						dest[dptr++] = col;
					else
						dptr++;

					if (font[fptr++] != 0)
						dest[dptr++] = col;
					else
						dptr++;

					if (font[fptr++] != 0)
						dest[dptr++] = col;
					else
						dptr++;
				}

				for (int i = x; i < 0; i++) {
					if (font[fptr++] != 0)
						dest[dptr++] = col;
					else
						dptr++;
				}
				dptr += dw;
				fptr += fw;
			}
		} catch(Exception e) {
			System.out.println("plotletter: " + e);
			e.printStackTrace();
		}
	}

	public int textheight(int fontid) {
		if (fontid == 0)
			return fonts[fontid][8] - 2;
		else
			return fonts[fontid][8] - 1;
	}

	public int textwidth(String text, int fontid) {
		int width = 0;
		byte[] font = fonts[fontid];

		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '@' && i + 4 < text.length() && text.charAt(i + 4) == '@') {
				i += 4;
			} else {
				if (text.charAt(i) == '~' && i + 4 < text.length() && text.charAt(i + 4) == '~')
					i += 4;
				else
					width += font[fontchars[text.charAt(i)] + 7];
			}
		}
		return width;
	}

	@Override
	public boolean imageUpdate(Image image, int flags, int x, int y, int w, int h) {
		return true;
	}

	public static final int xj = 0;
	public int width;
	public int height;
	public int area;
	public int maxwidth;
	public int maxheight;
	ColorModel colmodel;
	public int[] pixels;
	ImageConsumer imgconsumer;
	private Component comp;
	public Image img;
	public int ik[][];
	public byte jk[][];
	public int kk[][];
	public int lk[];
	public int mk[];
	public int nk[];
	public int ok[];
	public int pk[];
	public int qk[];
	public boolean rk[];
	private int rendy;
	private int rendh;
	private int rendx;
	private int rendw;
	public boolean interlace;
	static byte fonts[][] = new byte[50][];
	static int fontchars[];
	static int fontptr;
	public boolean al;
	int bl[];
	int cl[];
	int dl[];
	int el[];
	int fl[];
	int gl[];
	int hl[];
	
	public static final int BLACK = 0;
	public static final int WHITE = 0xffffff;
	public static final int RED = 0xff0000;
	public static final int DARK_RED = 0xc00000;
	public static final int GREEN = 65280;
	public static final int BLUE = 255;
	public static final int YELLOW = 0xffff00;
	public static final int CYAN = 65535;
	public static final int MAGENTA = 0xff00ff;
	public static final int rl = 0xc0c0c0;
	public static final int sl = 0x808080;
	public static final int tl = 0x404040;
	public static final int ul = 0xff8000;
	
	public static final int vl = 0;
	public static final int wl = 1;
	public static final int xl = 3;
	public static final int yl = 4;
	public static final int zl = 5;
	public static final int am = 7;

	static 
	{
		String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\243$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";
		fontchars = new int[256];
		for(int j = 0; j < 256; j++)
		{
			int k = s.indexOf(j);
			if(k == -1)
				k = 74;
			fontchars[j] = k * 9;
		}

	}
}
