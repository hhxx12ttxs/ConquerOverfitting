package model;

import gui.ErrorWindow;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import model.Channel.Point3D;
import mpi.cbg.fly.Feature;
import utils.Mask;
import utils.MaskFactory;
import utils.RandomNumberGenerator;

public class ColorImage implements Image, Cloneable {

	private ImageType type;
	private ImageFormat format;
	private Channel red;
	private Channel green;
	private Channel blue;
	private BufferedImage image;

	public ColorImage(int height, int width, ImageFormat format, ImageType type) {
		// Initialize a channel for each RGB color
		this.red = new Channel(width, height);
		this.green = new Channel(width, height);
		this.blue = new Channel(width, height);
		this.format = format;
		this.type = type;
		this.image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
	}

	public ColorImage(BufferedImage bufferedImage, ImageFormat format,
			ImageType type) {
		this(bufferedImage.getHeight(), bufferedImage.getWidth(), format, type);

		image = bufferedImage;

		// Gets the array of pixels in the image
		int[] rgbData = bufferedImage.getRGB(0, 0, bufferedImage.getWidth(),
				bufferedImage.getHeight(), null, 0, bufferedImage.getWidth());

		int colorRed;
		int colorGreen;
		int colorBlue;
		// Loads the pixels in each one of the channels
		for (int x = 0; x < bufferedImage.getWidth(); x++) {
			for (int y = 0; y < bufferedImage.getHeight(); y++) {
				colorRed = (rgbData[(y * bufferedImage.getWidth()) + x] >> 16) & 0xFF;
				colorGreen = (rgbData[(y * bufferedImage.getWidth()) + x] >> 8) & 0xFF;
				colorBlue = (rgbData[(y * bufferedImage.getWidth()) + x]) & 0xFF;

				red.setPixel(x, y, colorRed);
				green.setPixel(x, y, colorGreen);
				blue.setPixel(x, y, colorBlue);
			}
		}
	}

	public int getHeight() {
		return red.getHeight();
	}

	public int getWidth() {
		return red.getWidth();
	}

	public ImageType getType() {
		return type;
	}

	public ImageFormat getImageFormat() {
		return format;
	}

	public BufferedImage getImage() {
		this.applyChanges();
		return image;
	}

	public Color getRGBPixel(int x, int y) {
		int red = this.red.truncatePixel(getPixelFromChannel(x, y,
				ColorChannel.RED));
		int green = this.green.truncatePixel(getPixelFromChannel(x, y,
				ColorChannel.GREEN));
		int blue = this.blue.truncatePixel(getPixelFromChannel(x, y,
				ColorChannel.BLUE));
		return new Color(red, green, blue);
	}

	public double getPixelFromChannel(int x, int y, ColorChannel channel) {
		if (channel == ColorChannel.RED) {
			return red.getPixel(x, y);
		}
		if (channel == ColorChannel.GREEN) {
			return green.getPixel(x, y);
		}
		if (channel == ColorChannel.BLUE) {
			return blue.getPixel(x, y);
		}
		throw new IllegalStateException();
	}

	public void setPixel(int x, int y, Color color) {
		red.setPixel(x, y, color.getRed());
		green.setPixel(x, y, color.getGreen());
		blue.setPixel(x, y, color.getBlue());
		image.setRGB(x, y, color.getRGB());
	}

	public Image getImagePart(Rectangle selection) {

		return new ColorImage(image.getSubimage(selection.x, selection.y,
				selection.width, selection.height), this.format, this.type);
	}

	public void pasteImagePart(BufferedImage other, Rectangle selection) {
		// Point check
		if (selection.x < 0 || selection.x > image.getWidth()
				|| selection.y < 0 || selection.y > image.getHeight()) {
			new ErrorWindow("invalid location, out of bounds!");
			return;
		}
		// Rectangle check
		if (selection.x + selection.width > image.getWidth()
				|| selection.y + selection.height > image.getHeight()) {
			new ErrorWindow("invalid location rectangle gets out of bounds");
			return;
		}

		// Gets the array of pixels in the image
		int[] rgbData = other.getRGB(0, 0, selection.width, selection.height,
				null, 0, selection.width);

		int colorRed;
		int colorGreen;
		int colorBlue;
		// Loads the pixels in each one of the channels
		for (int xpos = selection.x; xpos < selection.x + selection.width; xpos++) {
			for (int ypos = selection.y; ypos < selection.y + selection.height; ypos++) {
				// Changing from the image coordinates to the other's
				// coordinates
				colorRed = (rgbData[((ypos - selection.y) * selection.width)
						+ (xpos - selection.x)] >> 16) & 0xFF;
				colorGreen = (rgbData[((ypos - selection.y) * selection.width)
						+ (xpos - selection.x)] >> 8) & 0xFF;
				colorBlue = (rgbData[((ypos - selection.y) * selection.width)
						+ (xpos - selection.x)]) & 0xFF;

				red.setPixel(xpos, ypos, colorRed);
				green.setPixel(xpos, ypos, colorGreen);
				blue.setPixel(xpos, ypos, colorBlue);
				image.setRGB(xpos, ypos,
						other.getRGB(xpos - selection.x, ypos - selection.y));
			}
		}
	}

	public Color calculateAverage(Rectangle selection) {
		// Point check
		if (selection.x < 0 || selection.x > image.getWidth()
				|| selection.y < 0 || selection.y > image.getHeight())
			new ErrorWindow("invalid location, out of bounds!");
		// Rectangle check
		if (selection.x + selection.width > image.getWidth()
				|| selection.y + selection.height > image.getHeight())
			new ErrorWindow("invalid location rectangle gets out of bounds");

		int rSum = 0, gSum = 0, bSum = 0, qty = 0;

		// Loads the pixels in each one of the channels
		for (int xpos = selection.x; xpos < selection.x + selection.width; xpos++) {
			for (int ypos = selection.y; ypos < selection.y + selection.height; ypos++) {
				// Changing from the image coordinates to the other's
				// coordinates
				rSum += red.getPixel(xpos, ypos);
				gSum += green.getPixel(xpos, ypos);
				bSum += blue.getPixel(xpos, ypos);
				qty++;
			}
		}
		return new Color(rSum / qty, gSum / qty, bSum / qty);
	}

	public Image add(Image img) {
		ColorImage ci = (ColorImage) img;
		this.red.add(ci.red);
		this.green.add(ci.green);
		this.blue.add(ci.blue);
		return this;
	}

	public Image substract(Image img) {
		ColorImage ci = (ColorImage) img;
		this.red.substract(ci.red);
		this.green.substract(ci.green);
		this.blue.substract(ci.blue);
		return this;
	}

	public Image multiply(Image img) {
		ColorImage ci = (ColorImage) img;
		this.red.multiply(ci.red);
		this.green.multiply(ci.green);
		this.blue.multiply(ci.blue);
		return this;
	}

	public Image multiply(double scalar) {
		this.red.multiply(scalar);
		this.green.multiply(scalar);
		this.blue.multiply(scalar);
		return this;
	}

	private void applyChanges() {
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				image.setRGB(x, y, this.getRGBPixel(x, y).getRGB());
				// this.setPixel(x, y, this.getRGBPixel(x, y));
			}
		}
	}

	public void dynamicRangeCompression() {
		double maxRed = -Double.MAX_VALUE;
		double maxGreen = -Double.MAX_VALUE;
		double maxBlue = -Double.MAX_VALUE;

		// Calculates R
		for (int x = 0; x < this.getWidth(); x++) {
			for (int y = 0; y < this.getHeight(); y++) {
				double redPixel = red.getPixel(x, y);
				double greenPixel = green.getPixel(x, y);
				double bluePixel = blue.getPixel(x, y);

				maxRed = Math.max(maxRed, redPixel);
				maxGreen = Math.max(maxGreen, greenPixel);
				maxBlue = Math.max(maxBlue, bluePixel);
			}
		}

		this.red.dynamicRangeCompression(maxRed);
		this.green.dynamicRangeCompression(maxGreen);
		this.blue.dynamicRangeCompression(maxBlue);
	}

	public void toGrayscale() {
		// TODO: add the grayscale depth (ex 16 possiblities and use a kind of
		// casting to get the real value)
		type = ImageType.GRAYSCALE;
		for (int x = 0; x < this.getWidth(); x++) {
			for (int y = 0; y < this.getHeight(); y++) {
				double redPixel = red.getPixel(x, y);
				double greenPixel = green.getPixel(x, y);
				double bluePixel = blue.getPixel(x, y);
				double avg = (redPixel + greenPixel + bluePixel) / 3;

				red.setPixel(x, y, avg);
				green.setPixel(x, y, avg);
				blue.setPixel(x, y, avg);
			}
		}
	}

	public void negative() {
		this.red.negative();
		this.blue.negative();
		this.green.negative();
	}

	public int[] getPixelArray() {
		return image.getRGB(0, 0, this.getWidth(), this.getHeight(), null, 0,
				this.getWidth());
	}

	public Image clone() {
		ColorImage other = new ColorImage(getHeight(), getWidth(), format, type);
		other.red = red.clone();
		other.green = green.clone();
		other.blue = blue.clone();
		other.image.setRGB(0, 0, getWidth(), getHeight(), getPixelArray(), 0,
				getWidth());
		return other;
	}

	public void applyThreshold(double value) {
		this.red.applyThreshold(value);
		this.green = this.red.clone();
		this.blue = this.red.clone();
	}

	public void applyChannelThreshold(double value) {
		this.red.applyThreshold(value);
		this.green.applyThreshold(value);
		this.blue.applyThreshold(value);
	}

	public void exponentialNoise(double u) {
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				double noise = RandomNumberGenerator.exponential(u);
				// f(i,j) = s(i,j) + n(i,j) --- s is the image, n is the noise
				// n(i,j) = s(i,j) * yk ------- yk is the exponential variable
				red.setPixel(x, y, red.getPixel(x, y) + red.getPixel(x, y)
						* noise);
				green.setPixel(x, y,
						green.getPixel(x, y) + green.getPixel(x, y) * noise);
				blue.setPixel(x, y, blue.getPixel(x, y) + blue.getPixel(x, y)
						* noise);
			}
		}
	}

	public void rayleighNoise(double epsilon) {
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				double random = Math.random();
				if (random * 100 < 20) {
					double noise = RandomNumberGenerator.rayleigh(epsilon);
					// f(i,j) = s(i,j) + n(i,j) --- s is the image, n is the
					// noise
					// n(i,j) = s(i,j) * yk ------- yk is the rayleigh variable
					red.setPixel(x, y, red.getPixel(x, y) + red.getPixel(x, y)
							* noise);
					green.setPixel(x, y,
							green.getPixel(x, y) + green.getPixel(x, y) * noise);
					blue.setPixel(x, y,
							blue.getPixel(x, y) + blue.getPixel(x, y) * noise);
				}
			}
		}
	}

	public void gausseanNoise(double mean, double standardDeviation) {
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				double noise = RandomNumberGenerator.gaussian(mean,
						standardDeviation);
				// f(i,j) = s(i,j) + n(i,j) --- s is the image, n is the noise
				// n(i,j) = s(i,j) * yk ------- yk is the gaussian variable
				red.setPixel(x, y, red.getPixel(x, y) + noise);
				green.setPixel(x, y, green.getPixel(x, y) + noise);
				blue.setPixel(x, y, blue.getPixel(x, y) + noise);
			}
		}
	}

	public void saltAndPepperNoise(double percentage, double po, double p1) {
		// TODO: ask if the random should be calculated for every channel
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				double random = Math.random();
				if (random * 100 < percentage) {
					random = RandomNumberGenerator.uniform(0, 1);
					if (random <= po) {
						red.setPixel(x, y, Channel.MIN_CHANNEL_COLOR);
						green.setPixel(x, y, Channel.MIN_CHANNEL_COLOR);
						blue.setPixel(x, y, Channel.MIN_CHANNEL_COLOR);
					} else if (random >= p1) {
						red.setPixel(x, y, Channel.MAX_CHANNEL_COLOR);
						green.setPixel(x, y, Channel.MAX_CHANNEL_COLOR);
						blue.setPixel(x, y, Channel.MAX_CHANNEL_COLOR);
					}
				}
			}
		}
	}

	public Color applyMeanFilter(int pixelX, int pixelY, int rectangleSide) {
		// TODO: check if it should send out of bounds exception?
		if (pixelX < 0 || pixelX > getWidth() || pixelY < 0
				|| pixelY > getHeight())
			return null;

		double valueR = 0, valueG = 0, valueB = 0, pixelQty = 0;
		for (int x = pixelX - rectangleSide / 2; x < pixelX + rectangleSide / 2; x++)
			for (int y = pixelY - rectangleSide / 2; y < pixelY + rectangleSide
					/ 2; y++) {
				try {
					valueR += getPixelFromChannel(x, y, ColorChannel.RED);
					valueG += getPixelFromChannel(x, y, ColorChannel.GREEN);
					valueB += getPixelFromChannel(x, y, ColorChannel.BLUE);
					pixelQty++;
				} catch (IndexOutOfBoundsException e) {
					// Ignore
				}
			}
		valueR /= pixelQty;
		valueG /= pixelQty;
		valueB /= pixelQty;
		return new Color((int) valueR, (int) valueG, (int) valueB);
	}

	public Color applyGaussianFilter(int pixelX, int pixelY, int rectangleSide,
			double sigma) {
		if (rectangleSide % 2 == 0) {
			rectangleSide++;
		}
		// Mask mask = new Mask(size);
		double rTotal = 0;
		double gTotal = 0;
		double bTotal = 0;
		for (int x = pixelX - rectangleSide / 2; x <= pixelX + rectangleSide
				/ 2; x++)
			for (int y = pixelY - rectangleSide / 2; y <= pixelY
					+ rectangleSide / 2; y++) {
				double relativeX = pixelX - x;
				double relativeY = pixelY - y;

				double gaussianFunction = (1.0 / (2.0 * Math.PI * Math.pow(
						sigma, 2)))
						* Math.exp(-((Math.pow(relativeX, 2) + Math.pow(
								relativeY, 2)) / (Math.pow(sigma, 2))));
				// double gaussianFunction = (1.0 / (2.0 * Math.PI * Math.pow(
				// sigma, 2)))
				// * Math.exp(-((Math.pow(x, 2) + Math.pow(y, 2)) / (Math
				// .pow(sigma, 2))));
				// System.out.println("pi pow:"
				// + (2.0 * Math.PI * Math.pow(sigma, 2)));
				// System.out.println("1/pipow: "
				// + (1.0 / (2.0 * Math.PI * Math.pow(sigma, 2))));
				// System.out.println("pows:" + (Math.pow(x, 2) + Math.pow(y,
				// 2)));
				// System.out.println("sigma pow:" + (Math.pow(sigma, 2)));
				// System.out.println("function:" + gaussianFunction);
				try {
					rTotal += getRGBPixel(x, y).getRed() * gaussianFunction * 2;
					gTotal += getRGBPixel(x, y).getGreen() * gaussianFunction
							* 2;
					bTotal += getRGBPixel(x, y).getBlue() * gaussianFunction
							* 2;
				} catch (IndexOutOfBoundsException e) {
					// Ignore
				}
				// total += gaussianFunction * getP;
				// mask.setPixel(i, j, gaussianFunction);
			}
		// for (int i = -mask.getWidth() / 2; i <= mask.getWidth() / 2; i++) {
		// for (int j = -mask.getHeight() / 2; j <= mask.getHeight() / 2; j++) {
		// double oldPixel = mask.getValue(i, j);
		// mask.setPixel(i, j, oldPixel / total);
		// }
		// }
		// System.out.println("TOTAL:" + total);
		// return new Color((int) (getRGBPixel(pixelX, pixelY).getRGB() /
		// total));
		// return new Color((int) (rTotal*total), (int) (gTotal*total), (int)
		// (bTotal*total));
		return new Color((int) (rTotal), (int) (gTotal), (int) (bTotal));
	}

	public Color applyMedianFilter(int pixelX, int pixelY, int rectangleSide) {
		// TODO: check if it should send out of bounds exception?
		if (pixelX < 0 || pixelX > getWidth() || pixelY < 0
				|| pixelY > getHeight())
			return null;

		int length = 0;
		for (int x = pixelX - rectangleSide / 2; x < pixelX + rectangleSide / 2; x++)
			for (int y = pixelY - rectangleSide / 2; y < pixelY + rectangleSide
					/ 2; y++) {
				try {
					getPixelFromChannel(x, y, ColorChannel.RED);
					length++;
				} catch (IndexOutOfBoundsException e) {
					// Ignore
				}
			}
		double[] rValues = new double[length];
		double[] gValues = new double[length];
		double[] bValues = new double[length];
		int i = 0;

		for (int x = pixelX - rectangleSide / 2; x < pixelX + rectangleSide / 2; x++)
			for (int y = pixelY - rectangleSide / 2; y < pixelY + rectangleSide
					/ 2; y++) {
				try {
					rValues[i] = getPixelFromChannel(x, y, ColorChannel.RED);
					gValues[i] = getPixelFromChannel(x, y, ColorChannel.GREEN);
					bValues[i] = getPixelFromChannel(x, y, ColorChannel.BLUE);
					i++;
				} catch (IndexOutOfBoundsException e) {
					// Ignore
				}
			}

		Arrays.sort(rValues);
		Arrays.sort(gValues);
		Arrays.sort(bValues);

		if (length % 2 != 0) {
			// Ex If length is 5, the index is 2 (0,1,2,3,4)
			return new Color((int) rValues[length / 2],
					(int) gValues[length / 2], (int) bValues[length / 2]);
		} else if (length == 2) {
			return new Color((int) (rValues[0] + rValues[1]) / 2,
					(int) (gValues[0] + gValues[1]) / 2,
					(int) (bValues[0] + bValues[1]) / 2);
		} else {
			// Ex If length is 6, the index is 2 and 3 (0,1,2,3,4,5)
			return new Color(
					(int) (rValues[length / 2] + rValues[length / 2 + 1]) / 2,
					(int) (gValues[length / 2] + gValues[length / 2 + 1]) / 2,
					(int) (bValues[length / 2] + bValues[length / 2 + 1]) / 2);
		}
	}

	public void applyAnisotropicDiffusion(BorderDetector bd) {
		red = this.red.applyAnisotropicDiffusion(bd);
		green = this.green.applyAnisotropicDiffusion(bd);
		blue = this.blue.applyAnisotropicDiffusion(bd);
	}

	public void applyMask(Mask mask) {
		this.red.applyMask(mask);
		this.green.applyMask(mask);
		this.blue.applyMask(mask);
	}

	public void applyMasksAndSynth(SynthesisFunction func, Mask mask1,
			Mask mask2) {
		Image copy = clone();

		this.applyMask(mask1);
		copy.applyMask(mask2);
		this.synthesize(func, copy);
	}

	public void synthesize(SynthesisFunction func, Image... imgs) {
		Image[] cimgs = imgs;

		Channel[] redChnls = new Channel[cimgs.length];
		Channel[] greenChnls = new Channel[cimgs.length];
		Channel[] blueChnls = new Channel[cimgs.length];

		for (int i = 0; i < cimgs.length; i++) {
			redChnls[i] = ((ColorImage) cimgs[i]).red;
			greenChnls[i] = ((ColorImage) cimgs[i]).green;
			blueChnls[i] = ((ColorImage) cimgs[i]).blue;
		}

		this.red.synthesize(func, redChnls);
		this.green.synthesize(func, greenChnls);
		this.blue.synthesize(func, blueChnls);
	}

	public void markZeroCrossers() {
		this.red.markZeroCrossers();
		this.green = this.red;
		this.blue = this.red;
	}

	public void markCrossersWithThreshold(int threshold) {
		this.red.markCrossersWithThreshold(threshold);
		this.green = this.red;
		this.blue = this.red;
	}

	public int[][] getDerivationDirections() {
		int[][] turns = new int[getWidth()][getHeight()];
		ColorImage gxImage = (ColorImage) this.clone();
		gxImage.applyMask(MaskFactory.sobelMask());
		ColorImage gyImage = (ColorImage) this.clone();
		gyImage.applyMask(MaskFactory.sobelMask().turn().turn());
		for (int i = 0; i < getWidth(); i++)
			for (int j = 0; j < getHeight(); j++) {
				double gx = gxImage.red.getPixel(i, j);
				double gy = gyImage.red.getPixel(i, j);
				double alpha = 0;
				if (gx != 0) {
					alpha = Math.atan(gy / gx);
				}
				if (alpha > 22.5 && alpha < 67.5)
					turns[i][j] = 1;
				else if (alpha > 67.5 && alpha < 112.5)
					turns[i][j] = 2;
				else if (alpha > 112.5 && alpha < 157.5)
					turns[i][j] = 3;
				else
					turns[i][j] = 0;
			}
		return turns;
	}

	public void borderWithNoMaximumsDeletion(int[][] derivationDirections) {
		this.applyMasksAndSynth(new ModuleSynth(), MaskFactory.sobelMask(),
				MaskFactory.sobelMask().turn().turn());
		this.red.deleteNotMaximums(derivationDirections);
		this.green = this.red;
		this.blue = this.red;
	}

	public void histeresisThreshold(double t1, double t2) {
		this.red.histeresisThreshold(t1, t2);
		this.green.histeresisThreshold(t1, t2);
		this.blue.histeresisThreshold(t1, t2);
	}

	public double otsuThreshold() {
		return red.otsuThreshold();
	}

	public void contrast(double r1, double r2, double y1, double y2) {
		red.contrast(r1, r2, y1, y2);
		green.contrast(r1, r2, y1, y2);
		blue.contrast(r1, r2, y1, y2);
	}

	public void applySusan(Mask mask, double threshold) {
		this.red.applySusan(mask, threshold, 0, 0, Channel.MAX_CHANNEL_COLOR);
		this.green.applySusan(mask, threshold, 0, 0, 0);
		this.blue.applySusan(mask, threshold, 0, Channel.MAX_CHANNEL_COLOR, 0);
	}

	public HashMap<Double, Double> applyHough(int granularityTita,
			int granularityRo, double threshold, int totalLines) {
		return this.red.applyHough(granularityTita, granularityRo, threshold,
				totalLines);

	}

	public void drawHoughLines(HashMap<Double, Double> roTitas, double threshold) {
		this.red.drawLines(roTitas, threshold);
	}

	public Set<Point3D> applyCircleHough(int granularityA, int granularityB,
			int granularityR, double threshold, int totalLines) {
		return this.red.applyCircleHough(granularityA, granularityB,
				granularityR, threshold, totalLines);

	}

	public void drawHoughCircles(Set<Point3D> abr, double threshold) {
		this.red.drawCircles(abr, threshold);
	}

	public double getGlobalThreshold() {
		double actualThreshold = 255 / 2, previousThreshold = 0;
		double previousDeltaT = 500, deltaT = 0;
		double epsilon = 0.00001;
		Image other;
		int iterations = 0;
		while (Math.abs(Math.abs(deltaT) - Math.abs(previousDeltaT)) > epsilon) {
			other = this.clone();
			other.applyThreshold(actualThreshold);
			Set<Point> whites = other.getWhites(), blacks = other.getBlacks();

			double m1 = 0;
			for (Point p : whites) {
				m1 += this.red.getPixel(p.x, p.y);
				if (this.red.getPixel(p.x, p.y) > 255)
					throw new RuntimeException();
			}
			m1 /= whites.size();

			double m2 = 0;
			for (Point p : blacks) {
				m2 += this.red.getPixel(p.x, p.y);
				if (this.red.getPixel(p.x, p.y) > 255)
					throw new RuntimeException();
			}
			m2 /= blacks.size();

			previousDeltaT = deltaT;
			previousThreshold = actualThreshold;
			actualThreshold = 0.5 * (m1 + m2);
			deltaT = actualThreshold - previousThreshold;
			System.out.println("DeltaT:" + deltaT + " T:" + actualThreshold
					+ " previous T:" + previousThreshold);
			iterations++;
		}
		System.out.println("Iterations:" + iterations);
		return actualThreshold;
	}

	public Set<Point> getWhites() {
		Set<Point> points = new HashSet<Point>();
		for (int x = 0; x < getWidth(); x++)
			for (int y = 0; y < getHeight(); y++)
				if (red.getPixel(x, y) == Color.WHITE.getRed()
						&& green.getPixel(x, y) == Color.WHITE.getGreen()
						&& blue.getPixel(x, y) == Color.WHITE.getBlue())
					points.add(new Point(x, y));
		return points;
	}

	public Set<Point> getBlacks() {
		Set<Point> points = new HashSet<Point>();
		for (int x = 0; x < getWidth(); x++)
			for (int y = 0; y < getHeight(); y++)
				if (red.getPixel(x, y) == Color.BLACK.getRed()
						&& green.getPixel(x, y) == Color.BLACK.getGreen()
						&& blue.getPixel(x, y) == Color.BLACK.getBlue())
					points.add(new Point(x, y));
		return points;
	}

	// --------------------------------------------------------------
	// --------------------------------------------------------------
	// --------------------------------------------------------------
	// --------------------------------------------------------------
	public boolean tracking(Tracker tracker) {
		List<Point> in = tracker.getInner();
		List<Point> out = tracker.getOuter();
		boolean changes = false;

		double[] averageIn = getAverage(in);
		double[] averageOut = getAverage(out);

		List<Point> lOut = tracker.getOuterBorder();
		for (Point p : lOut) {
			if (Fd(p, averageIn, averageOut) > 0) {
				tracker.setInnerBorder(p.x, p.y);
				for (Point point : tracker.neighbours(p)) {
					if (tracker.isOuter(point)) {
						tracker.setOuterBorder(point.x, point.y);
					}
				}
				for (Point point : tracker.neighbours(p)) {
					if (tracker.isInnerBorder(point)) {
						tracker.setInner(point.x, point.y);
					}
				}
				changes = true;
			}
		}
		List<Point> lIn = tracker.getInnerBorder();
		for (Point p : lIn) {
			if (Fd(p, averageIn, averageOut) < 0) {
				tracker.setOuterBorder(p.x, p.y);
				for (Point point : tracker.neighbours(p)) {
					if (tracker.isInner(point)) {
						tracker.setInnerBorder(point.x, point.y);
					}
				}
				for (Point point : tracker.neighbours(p)) {
					if (tracker.isOuterBorder(point)) {
						tracker.setOuter(point.x, point.y);
					}
				}
				changes = true;
			}
		}
		return changes;
	}

	private double[] getAverage(List<Point> l) {
		double[] ret = new double[3];
		ret[0] = 0;
		ret[1] = 0;
		ret[2] = 0;
		for (Point c : l) {
			ret[0] += this.red.getPixel(c.x, c.y);
		}
		ret[0] = ret[0] / l.size();

		for (Point c : l) {
			ret[1] += this.green.getPixel(c.x, c.y);
		}
		ret[1] = ret[1] / l.size();

		for (Point c : l) {
			ret[2] += this.blue.getPixel(c.x, c.y);
		}
		ret[2] = ret[2] / l.size();

		return ret;
	}

	private double Fd(Point p, double[] averageIn, double[] averageOut) {
		double p1, p2;
		double red, green, blue;
		red = this.red.getPixel(p.x, p.y);
		green = this.green.getPixel(p.x, p.y);
		blue = this.blue.getPixel(p.x, p.y);

		p1 = Math.sqrt(Math.pow((averageIn[0] - red), 2)
				+ Math.pow((averageIn[1] - green), 2)
				+ Math.pow((averageIn[2] - blue), 2));
		p2 = Math.sqrt(Math.pow((averageOut[0] - red), 2)
				+ Math.pow((averageOut[1] - green), 2)
				+ Math.pow((averageOut[2] - blue), 2));
		return p2 - p1;
	}

//	public void applyHarrisCornerDetector(int masksize, double sigma, double r,
//			double k) {
//		List<java.awt.Point> points = red.applyHarrisCornerDetector(masksize,
//				sigma, r, k);
//		for (java.awt.Point point : points) {
//			System.out.println(point.x + " " + point.y);
//			paintSquare(point);
//		}
//	}

	private void paintSquare(Point point) {
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				if (red.validPixel(point.x + i, point.y + j)) {
					// this.setRGBPixel(point.x+i, point.y+j,
					// Color.RED.getRGB());
					this.setPixel(point.x + i, point.y + j, Color.RED);
				}
			}
		}
	}

	public void detectFeatures(List<Feature> features) {
		for (Feature f : features) {
			setPixel((int) f.location[0], (int) f.location[1], Color.RED);

		}
	}

	public Image getHarrisCIM(int maskSize, double sigma, double k) {
		ColorImage lx = (ColorImage) this.clone();
		lx.applyMask(MaskFactory.sobelMask());
		ColorImage ly = (ColorImage) this.clone();
		ly.applyMask(MaskFactory.sobelMask().turn().turn());

		ColorImage lx2 = (ColorImage) lx.clone().multiply(lx);
		for (int x = 0; x < lx2.getWidth(); x++)
			for (int y = 0; y < lx2.getHeight(); y++) {
				lx2.setPixel(x, y,
						lx2.applyGaussianFilter(x, y, maskSize, sigma));
				// lx2.applyGaussianFilter(x, y, maskSize, sigma);
			}

		ColorImage ly2 = (ColorImage) ly.clone().multiply(ly);
		for (int x = 0; x < ly2.getWidth(); x++)
			for (int y = 0; y < ly2.getHeight(); y++) {
				ly2.setPixel(x, y,
						ly2.applyGaussianFilter(x, y, maskSize, sigma));
				// ly2.applyGaussianFilter(x, y, maskSize, sigma);
			}

		ColorImage lxy = (ColorImage) lx.clone().multiply(ly);
		lxy.multiply(lxy);
		for (int x = 0; x < lxy.getWidth(); x++)
			for (int y = 0; y < lxy.getHeight(); y++) {
				lxy.setPixel(x, y,
						lxy.applyGaussianFilter(x, y, maskSize, sigma));
				// lxy.applyGaussianFilter(x, y, maskSize, sigma);
			}

		ColorImage cim = (ColorImage) this.clone();
		for (int i = 0; i < getWidth(); i++)
			for (int j = 0; j < getHeight(); j++) {
				double color = lx2.red.getPixel(i, j)
						* ly2.red.getPixel(i, j)
						- lxy.red.getPixel(i, j)
						- k
						* Math.pow((lx2.red.getPixel(i, j) + ly2.red.getPixel(
								i, j)), 2);
				cim.red.setPixel(i, j, color);

				double color2 = lx2.green.getPixel(i, j)
						* ly2.green.getPixel(i, j)
						- lxy.green.getPixel(i, j)
						- k
						* Math.pow((lx2.green.getPixel(i, j) + ly2.green
								.getPixel(i, j)), 2);
				cim.green.setPixel(i, j, color2);

				double color3 = lx2.blue.getPixel(i, j)
						* ly2.blue.getPixel(i, j)
						- lxy.blue.getPixel(i, j)
						- k
						* Math.pow((lx2.blue.getPixel(i, j) + ly2.blue
								.getPixel(i, j)), 2);
				cim.blue.setPixel(i, j, color3);
				// System.out.println("r:" + color + " g:" + color2 + " b:" +
				// color3);
			}
		cim.red.TRUNCATE_ON = true;
		cim.green.TRUNCATE_ON = true;
		cim.blue.TRUNCATE_ON = true;
		return cim;
	}

	public List<Object> getHarrisMaxPoints(int maskSize, double sigma,
			double k, int totalResults) {
		totalResults = totalResults * getWidth() * getHeight() / 100;
		System.out.println("totalResults: " + totalResults);
		ColorImage lx = (ColorImage) this.clone();
		lx.applyMask(MaskFactory.sobelMask());
		ColorImage ly = (ColorImage) this.clone();
		ly.applyMask(MaskFactory.sobelMask().turn().turn());

		ColorImage lx2 = (ColorImage) lx.clone().multiply(lx);
		for (int x = 0; x < lx2.getWidth(); x++)
			for (int y = 0; y < lx2.getHeight(); y++) {
				lx2.setPixel(x, y,
						lx2.applyGaussianFilter(x, y, maskSize, sigma));
				// lx2.applyGaussianFilter(x, y, maskSize, sigma);
			}

		ColorImage ly2 = (ColorImage) ly.clone().multiply(ly);
		for (int x = 0; x < ly2.getWidth(); x++)
			for (int y = 0; y < ly2.getHeight(); y++) {
				ly2.setPixel(x, y,
						ly2.applyGaussianFilter(x, y, maskSize, sigma));
				// ly2.applyGaussianFilter(x, y, maskSize, sigma);
			}

		ColorImage lxy = (ColorImage) lx.clone().multiply(ly);
		lxy.multiply(lxy);
		for (int x = 0; x < lxy.getWidth(); x++)
			for (int y = 0; y < lxy.getHeight(); y++) {
				lxy.setPixel(x, y,
						lxy.applyGaussianFilter(x, y, maskSize, sigma));
				// lxy.applyGaussianFilter(x, y, maskSize, sigma);
			}

		PriorityQueue<WeightedPoint> queue = new PriorityQueue<WeightedPoint>(
				totalResults);
		// ColorImage cim = (ColorImage) this.clone();
//		HashMap<Double, Point> maximums = new HashMap<Double, Point>();
		for (int i = 0; i < getWidth(); i++)
			for (int j = 0; j < getHeight(); j++) {
				double color = lx2.red.getPixel(i, j)
						* ly2.red.getPixel(i, j)
						- lxy.red.getPixel(i, j)
						- k
						* Math.pow((lx2.red.getPixel(i, j) + ly2.red.getPixel(
								i, j)), 2);
//				boolean flag = false;
				// for(Double max : maximums.keySet())
				// if(color > max && !(maximums.size() < totalResults))
				// flag = true;
				// if(!flag && maximums.size() < totalResults){
				// maximums.put(color, new Point(i,j));
				// flag = true;
				// }else if(flag) {
				// deleteMin(maximums);
				// maximums.put(color, new Point(i,j));
				// }

				// cim.red.setPixel(i, j, color);

				double color2 = lx2.green.getPixel(i, j)
						* ly2.green.getPixel(i, j)
						- lxy.green.getPixel(i, j)
						- k
						* Math.pow((lx2.green.getPixel(i, j) + ly2.green
								.getPixel(i, j)), 2);
				// cim.green.setPixel(i, j, color2);
				// for(Double max : maximums.keySet())
				// if(color2 > max && !(maximums.size() < totalResults))
				// flag = true;
				// if(!flag && maximums.size() < totalResults){
				// maximums.put(color2, new Point(i,j));
				// flag = true;
				// }else if(flag) {
				// deleteMin(maximums);
				// maximums.put(color2, new Point(i,j));
				// }

				double color3 = lx2.blue.getPixel(i, j)
						* ly2.blue.getPixel(i, j)
						- lxy.blue.getPixel(i, j)
						- k
						* Math.pow((lx2.blue.getPixel(i, j) + ly2.blue
								.getPixel(i, j)), 2);
				// for(Double max : maximums.keySet())
				// if(color3 > max && !(maximums.size() < totalResults))
				// flag = true;
				// if(!flag && maximums.size() < totalResults){
				// maximums.put(color3, new Point(i,j));
				// flag = true;
				// }else if(flag) {
				// deleteMin(maximums);
				// maximums.put(color3, new Point(i,j));
				// }
				// cim.blue.setPixel(i, j, color3);
				// System.out.println("r:" + color + " g:" + color2 + " b:" +
				// color3);
				double value = Math.max(Math.max(color, color2), color3);
				if(queue.size() < totalResults) {
					queue.add(new WeightedPoint(new Point(i, j), value));
				}else if(value > queue.peek().value){
					queue.remove();
					queue.add(new WeightedPoint(new Point(i, j), value));
				}					
//					|| value > queue.peek().value){
			}
		// cim.red.TRUNCATE_ON = true;
		// cim.green.TRUNCATE_ON = true;
		// cim.blue.TRUNCATE_ON = true;
		// return cim;
		return Arrays.asList(queue.toArray());
	}

	public class WeightedPoint implements Comparable<WeightedPoint> {
		public final Point p;
		public final Double value;

		public WeightedPoint(Point p, Double value) {
			this.p = p;
			this.value = value;
		}

		@Override
		public boolean equals(Object obj) {
			WeightedPoint other = (WeightedPoint) obj;
			return other.p.equals(p);
		}

		public int compareTo(WeightedPoint arg0) {
			return value.compareTo(arg0.value);
		}
		
		@Override
		public String toString() {
			return value.toString();
		}

	}

//	private static void deleteMin(HashMap<Double, Point> map) {
//		double min = map.keySet().iterator().next();
//		for (Double d : map.keySet())
//			if (d < min)
//				min = d;
//		map.remove(min);
//	}

}
