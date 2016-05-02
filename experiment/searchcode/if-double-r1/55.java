package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import utils.Mask;

public class Channel implements Cloneable {

	static final int MIN_CHANNEL_COLOR = 0;
	static final int MAX_CHANNEL_COLOR = 255;

	private int width;
	private int height;
	
	private double maxChannelValue;
	private double minChannelValue;
	private boolean truncateFlag = false;
	private int counter = 0;
	public boolean TRUNCATE_ON = false;

	// The matrix is represented by an array, and to get a pixel(x,y) make y *
	// this.getWidth() + x
	private double[] channel;

	public Channel(int width, int height) {
		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException(
					"Images must have at least 1x1 pixel size");
		}

		this.width = width;
		this.height = height;
		this.channel = new double[width * height];
		maxChannelValue = MAX_CHANNEL_COLOR;
		minChannelValue = MIN_CHANNEL_COLOR;
	}

	/**
	 * Indicates whether a coordinate is valid for a pixel
	 * 
	 * @param x
	 * @param y
	 * @return True if the pixel is valid
	 */
	public boolean validPixel(int x, int y) {
		boolean validX = x >= 0 && x < this.getWidth();
		boolean validY = y >= 0 && y < this.getHeight();
		return validX && validY;
	}

	/**
	 * Returns the Channel height
	 * 
	 * @return
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns the Channel width
	 * 
	 * @return
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets a pixel(x,y) in the channel
	 * 
	 * @param x
	 * @param y
	 * @param color
	 */
	public void setPixel(int x, int y, double color) {
		if (!validPixel(x, y)) {
			throw new IndexOutOfBoundsException();
		}
//		if(truncateFlag) {
//			if(color > maxChannelValue)
//				maxChannelValue = color;
//			if(color < minChannelValue)
//				minChannelValue = color;
//			if(channel[y * this.getWidth() + x] == maxChannelValue || channel[y * this.getWidth() + x] == minChannelValue)
//				recalculateMaxMin();
//		}
//		if(TRUNCATE_ON)
//			System.out.println("SETTING PIXEL");
		truncateFlag = true;
		channel[y * this.getWidth() + x] = color;
	}

	private void recalculateMaxMin() {
		System.out.println("RECALCULATING: " + counter++);
		double max = channel[0], min = channel[0];
		for (int x = 0; x < width ; x++)
			for (int y = 0; y < height ; y++) {
				double color = this.getPixel(x, y);
				if(color > max)
					max = color;
				if(color < min)
					min = color;
			}
		maxChannelValue = max;
		minChannelValue = min;
	}
	
	/**
	 * Returns a pixel in the position x,y
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public double getPixel(int x, int y) {
		if (!validPixel(x, y)) {
			throw new IndexOutOfBoundsException();
		}

		return channel[y * this.getWidth() + x];
	}

	int truncatePixel(double originalValue) {		
		if(TRUNCATE_ON){
			if(truncateFlag) {
				truncateFlag = false;
				recalculateMaxMin();
			}
			return (int) ((originalValue - minChannelValue) / ((maxChannelValue - minChannelValue) / MAX_CHANNEL_COLOR));
		}
	
		return (originalValue > MAX_CHANNEL_COLOR)? MAX_CHANNEL_COLOR : (originalValue < MIN_CHANNEL_COLOR)? MIN_CHANNEL_COLOR : (int) originalValue;
	}

	public void add(Channel otherChannel) {
		for (int x = 0; x < width && x < otherChannel.width; x++) {
			for (int y = 0; y < height && y < otherChannel.height; y++) {
				double color = this.getPixel(x, y)
						+ otherChannel.getPixel(x, y);
				this.setPixel(x, y, color);
			}
		}
	}

	public void substract(Channel otherChannel) {
		for (int x = 0; x < width && x < otherChannel.width; x++) {
			for (int y = 0; y < height && y < otherChannel.height; y++) {
				double color = this.getPixel(x, y)
						- otherChannel.getPixel(x, y);
				color = Math.abs(color);
				this.setPixel(x, y, color);
			}
		}
	}

	public void multiply(Channel otherChannel) {
		for (int x = 0; x < width && x < otherChannel.width; x++) {
			for (int y = 0; y < height && y < otherChannel.height; y++) {
				double color = this.getPixel(x, y)
						* otherChannel.getPixel(x, y);
				this.setPixel(x, y, color);
			}
		}
	}

	public void multiply(double scalar) {
		for (int i = 0; i < this.channel.length; i++) {
			this.channel[i] *= scalar;
		}
	}

	public void negative() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				double color = this.getPixel(x, y);
				// T(r) = -r + L - 1 -- (L - 1 is represented by
				// MAX_CHANNEL_COLOR)
				this.setPixel(x, y, -color + MAX_CHANNEL_COLOR);
			}
		}
	}

	@Override
	public Channel clone() {
		Channel newChannel = new Channel(width, height);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				newChannel.setPixel(i, j, this.getPixel(i, j));
			}
		}
		return newChannel;
	}

	public void dynamicRangeCompression(double R) {
		double L = MAX_CHANNEL_COLOR;
		double c = (L - 1) / Math.log(1 + R);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				double r = this.getPixel(x, y);
				double color = (double) (c * Math.log(1 + r));
				this.setPixel(x, y, color);
			}
		}
	}

	/**
	 * Applies one iteration of anisotropic diffusion.
	 * 
	 * @param bd
	 * @return
	 */
	public Channel applyAnisotropicDiffusion(BorderDetector bd) {
		Channel newChannel = new Channel(width, height);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				double oldValueIJ = getPixel(x, y);

				double DnIij = x > 0 ? getPixel(x - 1, y) - oldValueIJ : 0;
				double DsIij = x < width - 1 ? getPixel(x + 1, y) - oldValueIJ
						: 0;
				double DeIij = y < height - 1 ? getPixel(x, y + 1) - oldValueIJ
						: 0;
				double DoIij = y > 0 ? getPixel(x, y - 1) - oldValueIJ : 0;

				double Cnij = bd.g(DnIij);
				double Csij = bd.g(DsIij);
				double Ceij = bd.g(DeIij);
				double Coij = bd.g(DoIij);

				double DnIijCnij = DnIij * Cnij;
				double DsIijCsij = DsIij * Csij;
				double DeIijCeij = DeIij * Ceij;
				double DoIijCoij = DoIij * Coij;

				double lambda = 0.25;
				double newValueIJ = oldValueIJ + lambda
						* (DnIijCnij + DsIijCsij + DeIijCeij + DoIijCoij);
				newChannel.setPixel(x, y, newValueIJ);
			}
		}
		return newChannel;
	}

	public void applyMask(Mask mask) {
		Channel newChannel = new Channel(this.width, this.height);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				double newPixel = applyMask(x, y, mask);
				newChannel.setPixel(x, y, newPixel);
			}
		}
		this.channel = newChannel.channel;
	}

	private double applyMask(int pointX, int pointY, Mask mask) {
		double newColor = 0;
		for (int x = -mask.getWidth() / 2; x <= mask.getWidth() / 2; x++) {
			for (int y = -mask.getHeight() / 2; y <= mask.getHeight() / 2; y++) {
				if (this.validPixel(pointX + x, pointY + y)) {
					double oldColor = 0;
					try {
						oldColor = this.getPixel(pointX + x, pointY + y);
						newColor += oldColor * mask.getValue(x, y);
					} catch (IndexOutOfBoundsException e) {
						// newColor += oldColor * mask.getValue(x, y);
					}
				}
			}
		}
		return newColor;
	}

	public void applySusan(Mask mask, double threshold, double common,
			double side, double corner) {
		Channel newChannel = new Channel(this.width, this.height);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int count = applySusan(x, y, mask, threshold);
				double sr0 = 1 - (((double) count) / 37);
				double color;
				if (sr0 > 0.625)
					color = corner;
				else if (sr0 > 0.25)
					color = side;
				else
					color = common;
				newChannel.setPixel(x, y, color);
			}
		}
		this.channel = newChannel.channel;
	}

	private int applySusan(int pointX, int pointY, Mask mask, double threshold) {
		int count = 0;
		for (int x = -mask.getWidth() / 2; x <= mask.getWidth() / 2; x++) {
			for (int y = -mask.getHeight() / 2; y <= mask.getHeight() / 2; y++) {
				if (this.validPixel(pointX + x, pointY + y)) {
					try {
						double difference = (this.getPixel(pointX + x, pointY
								+ y) * mask.getValue(x, y))
								- this.getPixel(pointX, pointY);
						if (Math.abs(difference) < threshold)
							count++;
					} catch (IndexOutOfBoundsException e) {
					}
				}
			}
		}
		return count;
	}

	public void synthesize(SynthesisFunction func, Channel... chnls) {
		double[] result = new double[width * height];

		for (int x = 0; x < channel.length; x++) {
			double[] colors = new double[chnls.length + 1];
			colors[0] = this.channel[x];
			for (int y = 1; y <= chnls.length; y++) {
				colors[y] = chnls[y - 1].channel[x];
			}
			result[x] = func.apply(colors);
		}
		this.channel = result;
		recalculateMaxMin();
	}

	/**
	 * @param r1
	 *            - The lower boundary
	 * @param r2
	 *            - The higher boundary
	 * @param y1
	 *            - The new value for the lower boundary
	 * @param y2
	 *            - The new value for the higher boundary
	 */
	public void contrast(double r1, double r2, double y1, double y2) {
		// Primera recta - antes de r1
		double m1 = y1 / r1;
		double b1 = 0;

		// Recta del medio - entre r1 y r2
		double m = (y2 - y1) / (r2 - r1);
		// when r = r1, y = y1; y1 = m*r1 + b
		double b = y1 - m * r1;

		// Ultima recta - de r2 hacia arriba
		double m2 = (MAX_CHANNEL_COLOR - y2) / (MAX_CHANNEL_COLOR - r2);
		// when r = r2, y = y2 ; y2 = m2 * r2 + b
		double b2 = y2 - m2 * r2;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				double r = this.getPixel(x, y);
				double f = 0;
				if (r < r1) {
					f = m1 * r + b1;
				} else if (r > r2) {
					f = m2 * r + b2;
				} else {
					f = m * r + b;
					f = (f > MAX_CHANNEL_COLOR) ? MAX_CHANNEL_COLOR : f;
				}
				this.setPixel(x, y, f);
			}
		}
	}

	public void deleteNotMaximums(int[][] directions) {
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				double pixel = this.getPixel(x, y);
				double left = 0, right = 0;
				int direction = directions[x][y];
				try {
					if (direction == 0)
						left = this.getPixel(x - 1, y);
					else if (direction == 1)
						left = this.getPixel(x - 1, y - 1);
					else if (direction == 2)
						left = this.getPixel(x + 1, y);
					else if (direction == 3)
						left = this.getPixel(x - 1, y + 1);
				} catch (Exception e) {
				}
				try {
					if (direction == 0)
						right = this.getPixel(x + 1, y);
					else if (direction == 1)
						right = this.getPixel(x + 1, y + 1);
					else if (direction == 2)
						left = this.getPixel(x - 1, y);
					else if (direction == 3)
						left = this.getPixel(x + 1, y - 1);
				} catch (Exception e) {
				}
				if (left > pixel || right > pixel)
					this.setPixel(x, y, 0);
			}
		return;
	}

	public void histeresisThreshold(double t1, double t2) {
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				double pixel = this.getPixel(x, y);
				if (pixel < t1)
					this.setPixel(x, y, 0);
				if (pixel > t1 && pixel < t2) {
					double left = 0, right = 0, up = 0, down = 0;
					try {
						left = this.getPixel(x - 1, y);
					} catch (Exception e) {
					}
					try {
						right = this.getPixel(x + 1, y);
					} catch (Exception e) {
					}
					try {
						up = this.getPixel(x, y + 1);
					} catch (Exception e) {
					}
					try {
						down = this.getPixel(x, y - 1);
					} catch (Exception e) {
					}
					if (left < t2 && right < t2 && up < t2 && down < t2) {
						this.setPixel(x, y, 0);
					}
				}
			}
		return;
	}

	public void markZeroCrossers() {
		double previous = 0;
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				double pixel = this.getPixel(x, y);
				if (previous < 0 && pixel > 0)
					this.setPixel(x, y, MAX_CHANNEL_COLOR);
				else
					this.setPixel(x, y, 0);
				previous = pixel;
			}
		return;
	}

	public void markCrossersWithThreshold(int threshold) {
		double previous = 0;
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				double pixel = this.getPixel(x, y);
				double difference = pixel - previous;
				if (difference > threshold)
					this.setPixel(x, y, MAX_CHANNEL_COLOR);
				else
					this.setPixel(x, y, 0);
				previous = pixel;
			}
		return;
	}

	public HashMap<Double, Double> applyHough(int granularityTita,
			int granularityRo, double threshold, int totalLines) {
		int[][] bucket = new int[granularityTita][granularityRo];
		double sqrt2D = Math.sqrt(2) * Math.max(width, height);
		for (int i = 0; i < granularityTita; i++)
			for (int j = 0; j < granularityRo; j++) {
				double tita = -Math.PI / 2 + (Math.PI * i) / granularityTita;
				double ro = -sqrt2D + (sqrt2D * 2 * j) / granularityRo;
				bucket[i][j] = countWhites(tita, ro, threshold);
			}

		Set<Point> winners = new HashSet<Point>();
		for (int w = 0; w < totalLines; w++) {
			int maxValue = 0;
			Point max = new Point(0, 0);
			for (int i = 0; i < granularityTita; i++)
				for (int j = 0; j < granularityRo; j++) {
					if (bucket[i][j] > maxValue
							&& !winners.contains(new Point(i, j))) {
						maxValue = bucket[i][j];
						max = new Point(i, j);
					}
				}
			winners.add(max);
		}

		HashMap<Double, Double> roTita = new HashMap<Double, Double>();
		for (Point p : winners)
			roTita.put(-sqrt2D + (sqrt2D * 2 * p.y) / granularityRo, -Math.PI
					/ 2 + (Math.PI * p.x) / granularityTita);
		return roTita;
	}

	public void drawLines(HashMap<Double, Double> roTitas, double threshold) {
		for (Double ro : roTitas.keySet())
			drawLine(roTitas.get(ro), ro, threshold);
		return;
	}

	public void drawLine(double tita, double ro, double threshold) {
		System.out.println("ro:" + ro + " tita:" + tita);
		for (int x = 0; x < width; x++) {
			int y = (int) ((ro - x * Math.cos(tita)) / Math.sin(tita));
			// System.out.println(y);
			if (y > 0 && y < height)
				setPixel(x, y, MAX_CHANNEL_COLOR);
		}
		// for (int x = 0; x < width; x++)
		// for (int y = 0; y < height; y++) {
		// if(getPixel(x, y) == MAX_CHANNEL_COLOR){
		// double straightLineError = ro - x*Math.cos(tita) - y*Math.sin(tita);
		// if(Math.abs(straightLineError) < threshold)
		// setPixel(x, y, MAX_CHANNEL_COLOR);
		// }
		// }
		// return;
	}

	private int countWhites(double tita, double ro, double threshold) {
		int count = 0;
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				// System.out.println(getPixel(x, y));
				if (truncatePixel(getPixel(x, y)) == MAX_CHANNEL_COLOR) {
					double straightLineError = ro - x * Math.cos(tita) - y
							* Math.sin(tita);
					// System.out.println(straightLineError);
					if (Math.abs(straightLineError) < threshold)
						count++;
				}
			}
		return count;
	}

	public Set<Point3D> applyCircleHough(int granularityA, int granularityB,
			int granularityR, double threshold, int totalLines) {
		int[][][] bucket = new int[granularityA][granularityB][granularityR];
		for (int i = 0; i < granularityA; i++)
			for (int j = 0; j < granularityB; j++)
				for (int k = 0; k < granularityR; k++) {
					double a = width / granularityA * i;
					double b = width / granularityB * j;
					double r = 20 + 30 / granularityR * k;
					bucket[i][j][k] = countCircleWhites(a, b, r, threshold);
				}

		Set<Point3D> winners = new HashSet<Point3D>();
		for (int w = 0; w < totalLines; w++) {
			int maxValue = 0;
			Point3D max = new Point3D(0, 0, 0);
			for (int i = 0; i < granularityA; i++)
				for (int j = 0; j < granularityB; j++)
					for (int k = 0; k < granularityR; k++) {
						if (bucket[i][j][k] > maxValue
								&& !winners.contains(new Point3D(i, j, k))) {
							maxValue = bucket[i][j][k];
							max = new Point3D(i, j, k);
						}
					}
			winners.add(max);
			System.out.println("Winner! votes: " + maxValue + " a:"
					+ (width / granularityA * max.x) + " b:"
					+ (width / granularityB * max.y) + " r:"
					+ (30 + 10 / granularityR * max.z));
		}

		Set<Point3D> abr = new HashSet<Point3D>();
		for (Point3D p : winners)
			abr.add(new Point3D(width / granularityA * p.x, width
					/ granularityB * p.y, 30 + 10 / granularityR * p.z));
		return abr;
	}

	private int countCircleWhites(double a, double b, double r, double threshold) {
		int count = 0;
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				// System.out.println(getPixel(x, y));
				if (truncatePixel(getPixel(x, y)) == MAX_CHANNEL_COLOR) {
					double circleError = Math.pow((x - a), 2)
							+ Math.pow((y - b), 2) - Math.pow(r, 2);
					// System.out.println(straightLineError);
					if (Math.abs(circleError) < threshold)
						count++;
				}
			}
		return count;
	}

	public void drawCircles(Set<Point3D> abr, double threshold) {
		for (Point3D p : abr)
			drawCircle(p.x, p.y, p.z, threshold);
		return;
	}

	private void drawCircle(double a, double b, double r, double threshold) {
		for (int x = (int) (a - r); x < a + r; x++)
			for (int y = (int) (b - r); y < b + r; y++) {
				if (Math.abs(Math.pow((x - a), 2) + Math.pow((y - b), 2)
						- Math.pow(r, 2)) < threshold)
					if (validPixel(x, y))
						setPixel(x, y, MAX_CHANNEL_COLOR);
			}
	}

	/**
	 * @author http://en.wikipedia.org/wiki/Otsu%27s_method
	 */
	public double otsuThreshold() {

		int[] histogram = getHistogram();
		int total = channel.length;

		double sum = 0;
		for (int i = 0; i < MAX_CHANNEL_COLOR; i++) {
			sum += i * histogram[i];
		}

		double sumB = 0;
		int wB = 0;
		int wF = 0;

		double max = 0;
		int threshold1 = 0;
		int threshold2 = 0;

		for (int i = 0; i < MAX_CHANNEL_COLOR; i++) {
			wB += histogram[i];
			if (wB == 0) {
				continue;
			}
			wF = total - wB;

			if (wF == 0) {
				break;
			}

			sumB += i * histogram[i];
			double mB = sumB / wB;
			double mF = (sum - sumB) / wF;

			double between = wB * wF * Math.pow(mB - mF, 2);

			if (between >= max) {
				threshold1 = i;
				if (between > max)
					threshold2 = i;
				max = between;
			}
		}
		return ((threshold1 + threshold2) / 2.0);
	}

//    public double otsuThreshold() {
//
//        int[] histogram = getHistogram();
//        int total = channel.length;
//
//        float sum = 0;
//        for (int i = 0; i < 256; i++) {
//            sum += i * histogram[i];
//        }
//
//        float sumB = 0;
//        int wB = 0;
//        int wF = 0;
//
//        float varMax = 0;
//        int threshold = 0;
//
//        for (int i = 0; i < 256; i++) {
//            wB += histogram[i];
//            if (wB == 0) {
//                continue;
//            }
//            wF = total - wB;
//
//            if (wF == 0) {
//                break;
//            }
//
//            sumB += i * histogram[i];
//            float mB = sumB / wB;
//            float mF = (sum - sumB) / wF;
//
//            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);
//
//            if (varBetween > varMax) {
//                varMax = varBetween;
//                threshold = i;
//            }
//        }
//        System.out.println("T = " + threshold);
////        umbral(threshold);
//        return threshold;
//    }
	public void applyThreshold(double value) {
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				double pixelColor = getPixel(x, y);
				int newColor = (pixelColor < value) ? MAX_CHANNEL_COLOR
						: MIN_CHANNEL_COLOR;
				setPixel(x, y, newColor);
			}
		return;
	}

	public int[] getHistogram() {

		int[] histogram = new int[MAX_CHANNEL_COLOR+1];
		for (int i = 0; i < histogram.length; i++) {
			histogram[i] = 0;
		}
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				int val = (int) getPixel(x, y);
				histogram[val]++;
			}
		return histogram;
	}

	public class Point3D {
		public final double x;
		public final double y;
		public final double z;

		public Point3D(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public int hashCode() {
			return (int) (x * 100 + y * 10000 + z);
		}

		@Override
		public boolean equals(Object obj) {
			Point3D other = (Point3D) obj;
			double epsilon = 0.01;
			return Math.abs(x - other.x) < epsilon
					&& Math.abs(y - other.y) < epsilon
					&& Math.abs(z - other.z) < epsilon;
		}
	}
	
	//---------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------
	//-------------------------------------				HARRIS 					 ------------------------------
	//---------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------
//	private class Corner {
//		int x, y;
//		double measure;
//
//		public Corner(int x, int y, double measure) {
//			this.x = x;
//			this.y = y;
//			this.measure = measure;
//		}
//	}
//	
//	public List<java.awt.Point> applyHarrisCornerDetector(int maskSize, double sigma, double r,
//			double k) {
//		double[][] Lx2 = new double[width][height];
//		double[][] Ly2 = new double[width][height];
//		double[][] Lxy = new double[width][height];
//		
//		List<Corner> corners = new ArrayList<Corner>();
//
//		// precompute derivatives
//		computeDerivatives(maskSize, sigma, Lx2, Ly2, Lxy);
//
//		// Harris measure map
//		double[][] harrismap = computeHarrisMap(k, Lx2, Ly2, Lxy);
//		
//		// for each pixel in the hmap, keep the local maxima
//		for (int y = 1; y < this.height - 1; y++) {    public void otsuThreshold() {

//    int[] histogram = imageHistogram();
//    int total = values.length;
//
//    float sum = 0;
//    for (int i = 0; i < 256; i++) {
//        sum += i * histogram[i];
//    }
//
//    float sumB = 0;
//    int wB = 0;
//    int wF = 0;
//
//    float varMax = 0;
//    int threshold = 0;
//
//    for (int i = 0; i < 256; i++) {
//        wB += histogram[i];
//        if (wB == 0) {
//            continue;
//        }
//        wF = total - wB;
//
//        if (wF == 0) {
//            break;
//        }
//
//        sumB += i * histogram[i];
//        float mB = sumB / wB;
//        float mF = (sum - sumB) / wF;
//
//        float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);
//
//        if (varBetween > varMax) {
//            varMax = varBetween;
//            threshold = i;
//        }
//    }
//    System.out.println("T = " + threshold);
//    umbral(threshold);
//
//}
//			for (int x = 1; x < this.width - 1; x++) {
//				double h = harrismap[x][y];
//				if (h < r)
//					continue;
//				if (!isSpatialMaxima(harrismap, (int) x, (int) y))
//					continue;
//				// add the corner to the list
//				corners.add(new Corner(x, y, h));
//			} Only you can see your history 
//		}
//
//		// remove corners to close to each other (keep the highest measure)
//		Iterator<Corner> iter = corners.iterator();
//		while (iter.hasNext()) {
//			Corner p = iter.next();
//			for (Corner n : corners) {
//				if (n == p)
//					continue;
//				int dist = (int) Math.sqrt((p.x - n.x) * (p.x - n.x)
//						+ (p.y - n.y) * (p.y - n.y));
//				if (dist > 3)
//					continue;
//				if (n.measure < p.measure)
//					continue;
//				iter.remove();
//				break;
//			}
//		}
//
//		// Display corners over the image (cross)
////		for (Corner p : corners) {
////			for (int dx = -2; dx <= 2; dx++) {
////				if (p.x + dx < 0 || p.x + dx >= width)
////					continue;
////				setInsidePixel(output, (int) p.x + dx, (int) p.y, canal, 255);
////			}
////			for (int dy = -2; dy <= 2; dy++) {
////				if (p.y + dy < 0 || p.y + dy >= height)
////					continue;
////				setInsidePixel(output, (int) p.x, (int) p.y + dy, canal, 255);
////			}
////		}
//		List<Point> points = new ArrayList<Point>();
//		for (Corner corner : corners) {
//			points.add(new Point(corner.x, corner.y));
//		}
//		return points;
//
//	}
//	
//	
//	private void computeDerivatives(int radius, double sigma, double[][] Lx2,
//			double[][] Ly2, double[][] Lxy) {
//
//		// gradient values: Gx,Gy
//		double[][][] grad = new double[width][height][];
//		for (int y = 0; y < this.height; y++)
//			for (int x = 0; x < this.width; x++)
//				grad[x][y] = sobel(x, y);
//
//		// precompute the coefficients of the gaussian filter
//		double[][] filter = new double[2 * radius + 1][2 * radius + 1];
//		double filtersum = 0;
//		for (int j = -radius; j <= radius; j++) {
//			for (int i = -radius; i <= radius; i++) {
//				double g = gaussian(i, j, sigma);
//				filter[i + radius][j + radius] = g;
//				filtersum += g;
//			}
//		}
//
//		// Convolve gradient with gau
//	}
//
//	// remove corners to close to each other (keep the highest measure)
//	Iterator<Corner> iter = corners.iterator();
//	while (iter.hasNext()) {ssian filter:
//		//
//		// Ix2 = (F) * (Gx^2)
//		// Iy2 = (F) * (Gy^2)
//		// Ixy = (F) * (Gx.Gy)
//		//
//		for (int y = 0; y < this.height; y++) {
//			for (int x = 0; x < this.width; x++) {
//
//				for (int dy = -radius; dy <= radius; dy++) {
//					for (int dx = -radius; dx <= radius; dx++) {
//						int xk = x + dx;
//						int yk = y + dy;
//						if (xk < 0 || xk >= this.width)
//							continue;
//						if (yk < 0 || yk >= this.height)
//							continue;
//
//						// filter weight
//						double f = filter[dx + radius][dy + radius];
//
//						// convolution
//						Lx2[x][y] += f * grad[xk][yk][0] * grad[xk][yk][0];
//						Ly2[x][y] += f * grad[xk][yk][1] * grad[xk][yk][1];
//						Lxy[x][y] += f * grad[xk][yk][0] * grad[xk][yk][1];
//					}
//				}
//				Lx2[x][y] /= filtersum;
//				Ly2[x][y] /= filtersum;
//				Lxy[x][y] /= filtersum;
//			}
//		}
//	}
//	
//	
//	private double[][] computeHarrisMap(double k, double[][] Lx2,
//			double[][] Ly2, double[][] Lxy) {
//
//		// Harris measure map
//		double[][] harrismap = new double[width][height];
//		double max = 0;
//
//		// for each pixel in the image
//		for (int y = 0; y < this.height; y++) {
//			for (int x = 0; x < this.width; x++) {
//				// compute ans store the harris measure
//				harrismap[x][y] = harrisMeasure(x, y, k, Lx2, Ly2, Lxy);
//				if (harrismap[x][y] > max)
//					max = harrismap[x][y];
//			}
//		}
//
//		// rescale measures in 0-100
//		for (int y = 0; y < this.height; y++) {
//			for (int x = 0; x < this.width; x++) {
//				double h = harrismap[x][y];
//				if (h < 0)
//					h = 0;
//				else
//					h = 100 * Math.log(1 + h) / Math.log(1 + max);
//				harrismap[x][y] = h;
//			}
//		}
//
//		return harrismap;
//	}
//
//	
//	private double harrisMeasure(int x, int y, double k, double[][] Lx2,
//			double[][] Ly2, double[][] Lxy) {
//		// matrix elements (normalized)
//		double m00 = Lx2[x][y];
//		double m01 = Lxy[x][y];
//		double m10 = Lxy[x][y];
//		double m11 = Ly2[x][y];
//
//		// Harris corner measure = det(M)-lambda.trace(M)^2
//
//		return m00 * m11 - m01 * m10 - k * (m00 + m11) * (m00 + m11);
//	}
//	
//	private boolean isSpatialMaxima(double[][] hmap, int x, int y) {
//		int n = 8;
//		int[] dx = new int[] { -1, 0, 1, 1, 1, 0, -1, -1 };
//		int[] dy = new int[] { -1, -1, -1, 0, 1, 1, 1, 0 };
//		double w = hmap[x][y];
//		for (int i = 0; i < n; i++) {
//			double wk = hmap[x + dx[i]][y + dy[i]];
//			if (wk >= w)
//				return false;
//		}
//		return true;
//	}
//	
//	private double[] sobel(int x, int y) {
//		int v00 = 0, v01 = 0, v02 = 0, v10 = 0, v12 = 0, v20 = 0, v21 = 0, v22 = 0;
//
//		int x0 = x - 1, x1 = x, x2 = x + 1;
//		int y0 = y - 1, y1 = y, y2 = y + 1;
//		if (x0 < 0)
//			x0 = 0;
//		if (y0 < 0)
//			y0 = 0;
//		if (x2 >= width)
//			x2 = width - 1;
//		if (y2 >= height)
//			y2 = height - 1;
//
//		v00 = (int) getInsidePixel(x0, y0);
//		v10 = (int) getInsidePixel(x1, y0);
//		v20 = (int) getInsidePixel(x2, y0);
//		v01 = (int) getInsidePixel(x0, y1);
//		v21 = (int) getInsidePixel(x2, y1);
//		v02 = (int) getInsidePixel(x0, y2);
//		v12 = (int) getInsidePixel(x1, y2);
//		v22 = (int) getInsidePixel(x2, y2);
//
//		double sx = (v20 + 2 * v21 + v22) - (v00 + 2 * v01 + v02);
//		double sy = (v02 + 2 * v12 + v22) - (v00 + 2 * v10 + v20);
//		return new double[] { sx / 4, sy / 4 };
//	}
//	
//	public double getInsidePixel(int x, int y) {
//		if (!validPixel(x, y)) {
//			return 0;
//		}
//
//		return channel[y * this.getWidth() + x];
//	}
//
//	public void setInsidePixel(int x, int y, double color) {
//		if (validPixel(x, y)) {
//			channel[y * this.getWidth() + x] = color;
//		}
//	}
//	
//	private double gaussian(double x, double y, double sigma2) {
//		double t = (x * x + y * y) / (2 * sigma2);
//		double u = 1.0 / (2 * Math.PI * sigma2);
//		double e = u * Math.exp(-t);
//		return e;
//	}
}

