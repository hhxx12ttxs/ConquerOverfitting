<<<<<<< HEAD
package org.doube.bonej;

import ij.*;
import ij.gui.GenericDialog;
import ij.macro.Interpreter;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.*;

import org.doube.util.ImageCheck;
import org.doube.util.ResultInserter;
import org.doube.util.RoiMan;
import org.doube.util.StackStats;
import org.doube.util.UsageReporter;

/* Bob Dougherty 8/10/2007
 Perform all of the steps for the local thickness calculation


 License:
 Copyright (c) 2007, OptiNav, Inc.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 Neither the name of OptiNav, Inc. nor the names of its contributors
 may be used to endorse or promote products derived from this software
 without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */
/**
 * @see <p>
 *      Hildebrand T, Rüegsegger P (1997) A new method for the model-independent
 *      assessment of thickness in three-dimensional images. J Microsc 185:
 *      67-75. <a
 *      href="http://dx.doi.org/10.1046/j.1365-2818.1997.1340694.x">doi
 *      :10.1046/j.1365-2818.1997.1340694.x</a>
 *      </p>
 * 
 *      <p>
 *      Saito T, Toriwaki J (1994) New algorithms for euclidean distance
 *      transformation of an n-dimensional digitized picture with applications.
 *      Pattern Recognit 27: 1551-1565. <a
 *      href="http://dx.doi.org/10.1016/0031-3203(94)90133-3"
 *      >doi:10.1016/0031-3203(94)90133-3</a>
 *      </p>
 * 
 * @author Bob Dougherty
 * @author Michael Doube (refactoring for BoneJ)
 * 
 */
public class Thickness implements PlugIn {
	// public static final int THRESHOLD = 128;
	private float[][] sNew;

	public void run(String arg) {
		ImageCheck ic = new ImageCheck();
		if (!ImageCheck.checkEnvironment())
			return;
		ImagePlus imp = IJ.getImage();
		if (!ic.isBinary(imp)) {
			IJ.error("8-bit binary (black and white only) image required.");
			return;
		}

		if (!ic.isVoxelIsotropic(imp, 1E-3)) {
			if (IJ.showMessageWithCancel(
					"Anisotropic voxels",
					"This image contains anisotropic voxels, which will\n"
							+ "result in incorrect thickness calculation.\n\n"
							+ "Consider rescaling your data so that voxels are isotropic\n"
							+ "(Image > Scale...).\n\n" + "Continue anyway?")) {
			} else
				return;

		}
		GenericDialog gd = new GenericDialog("Options");
		gd.addCheckbox("Thickness", true);
		gd.addCheckbox("Spacing", false);
		gd.addCheckbox("Graphic Result", true);
		gd.addCheckbox("Use_ROI_Manager", false);
		gd.addHelp("http://bonej.org/thickness");
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}
		boolean doThickness = gd.getNextBoolean();
		boolean doSpacing = gd.getNextBoolean();
		boolean doGraphic = gd.getNextBoolean();
		boolean doRoi = gd.getNextBoolean();

		long startTime = System.currentTimeMillis();
		String title = stripExtension(imp.getTitle());

		RoiManager roiMan = RoiManager.getInstance();
		// calculate trabecular thickness (Tb.Th)
		if (doThickness) {
			boolean inverse = false;
			ImagePlus impLTC = new ImagePlus();
			if (doRoi && roiMan != null) {
				ImageStack stack = RoiMan.cropStack(roiMan, imp.getStack(),
						true, 0, 1);
				ImagePlus crop = new ImagePlus(imp.getTitle(), stack);
				crop.setCalibration(imp.getCalibration());
				impLTC = getLocalThickness(crop, inverse);
			} else
				impLTC = getLocalThickness(imp, inverse);
			impLTC.setTitle(title + "_Tb.Th");
			impLTC.setCalibration(imp.getCalibration());
			double[] stats = StackStats.meanStdDev(impLTC);
			insertResults(imp, stats, inverse);
			if (doGraphic && !Interpreter.isBatchMode()) {
				impLTC.show();
				impLTC.setSlice(1);
				impLTC.getProcessor().setMinAndMax(0, stats[2]);
				IJ.run("Fire");
			}
		}
		if (doSpacing) {
			boolean inverse = true;
			ImagePlus impLTCi = new ImagePlus();
			if (doRoi && roiMan != null) {
				ImageStack stack = RoiMan.cropStack(roiMan, imp.getStack(),
						true, 255, 1);
				ImagePlus crop = new ImagePlus(imp.getTitle(), stack);
				crop.setCalibration(imp.getCalibration());
				impLTCi = getLocalThickness(crop, inverse);
			} else
				impLTCi = getLocalThickness(imp, inverse);
			// check marrow cavity size (i.e. trabcular separation, Tb.Sp)
			impLTCi.setTitle(title + "_Tb.Sp");
			impLTCi.setCalibration(imp.getCalibration());
			double[] stats = StackStats.meanStdDev(impLTCi);
			insertResults(imp, stats, inverse);
			if (doGraphic && !Interpreter.isBatchMode()) {
				impLTCi.show();
				impLTCi.setSlice(1);
				impLTCi.getProcessor().setMinAndMax(0, stats[2]);
				IJ.run("Fire");
			}
		}
		IJ.showProgress(1.0);
		IJ.showStatus("Done");
		double duration = ((double) System.currentTimeMillis() - (double) startTime)
				/ (double) 1000;
		IJ.log("Duration = " + IJ.d2s(duration, 3) + " s");
		UsageReporter.reportEvent(this).send();
		return;
	}

	// Modified from ImageJ code by Wayne Rasband
	String stripExtension(String name) {
		if (name != null) {
			int dotIndex = name.lastIndexOf(".");
			if (dotIndex >= 0)
				name = name.substring(0, dotIndex);
		}
		return name;
	}

	/**
	 * <p>
	 * Saito-Toriwaki algorithm for Euclidian Distance Transformation. Direct
	 * application of Algorithm 1. Bob Dougherty 8/8/2006
	 * </p>
	 * 
	 * <ul>
	 * <li>Version S1A: lower memory usage.</li>
	 * <li>Version S1A.1 A fixed indexing bug for 666-bin data set</li>
	 * <li>Version S1A.2 Aug. 9, 2006. Changed noResult value.</li>
	 * <li>Version S1B Aug. 9, 2006. Faster.</li>
	 * <li>Version S1B.1 Sept. 6, 2006. Changed comments.</li>
	 * <li>Version S1C Oct. 1, 2006. Option for inverse case. <br />
	 * Fixed inverse behavior in y and z directions.</li>
	 * <li>Version D July 30, 2007. Multithread processing for step 2.</li>
	 * </ul>
	 * 
	 * <p>
	 * This version assumes the input stack is already in memory, 8-bit, and
	 * outputs to a new 32-bit stack. Versions that are more stingy with memory
	 * may be forthcoming.
	 * </p>
	 * 
	 * @param imp
	 *            8-bit (binary) ImagePlus
	 * 
	 */
	private float[][] geometryToDistanceMap(ImagePlus imp, boolean inv) {
		final int w = imp.getWidth();
		final int h = imp.getHeight();
		final int d = imp.getStackSize();
		int nThreads = Runtime.getRuntime().availableProcessors();

		// Create references to input data
		ImageStack stack = imp.getStack();
		byte[][] data = new byte[d][];
		for (int k = 0; k < d; k++)
			data[k] = (byte[]) stack.getPixels(k + 1);

		// Create 32 bit floating point stack for output, s. Will also use it
		// for g in Transformation 1.
		float[][] s = new float[d][];
		for (int k = 0; k < d; k++) {
			ImageProcessor ipk = new FloatProcessor(w, h);
			s[k] = (float[]) ipk.getPixels();
		}
		float[] sk;
		// Transformation 1. Use s to store g.
		IJ.showStatus("EDT transformation 1/3");
		Step1Thread[] s1t = new Step1Thread[nThreads];
		for (int thread = 0; thread < nThreads; thread++) {
			s1t[thread] = new Step1Thread(thread, nThreads, w, h, d, inv, s,
					data);
			s1t[thread].start();
		}
		try {
			for (int thread = 0; thread < nThreads; thread++) {
				s1t[thread].join();
			}
		} catch (InterruptedException ie) {
			IJ.error("A thread was interrupted in step 1 .");
		}
		// Transformation 2. g (in s) -> h (in s)
		IJ.showStatus("EDT transformation 2/3");
		Step2Thread[] s2t = new Step2Thread[nThreads];
		for (int thread = 0; thread < nThreads; thread++) {
			s2t[thread] = new Step2Thread(thread, nThreads, w, h, d, s);
			s2t[thread].start();
		}
		try {
			for (int thread = 0; thread < nThreads; thread++) {
				s2t[thread].join();
			}
		} catch (InterruptedException ie) {
			IJ.error("A thread was interrupted in step 2 .");
		}
		// Transformation 3. h (in s) -> s
		IJ.showStatus("EDT transformation 3/3");
		Step3Thread[] s3t = new Step3Thread[nThreads];
		for (int thread = 0; thread < nThreads; thread++) {
			s3t[thread] = new Step3Thread(thread, nThreads, w, h, d, inv, s,
					data);
			s3t[thread].start();
		}
		try {
			for (int thread = 0; thread < nThreads; thread++) {
				s3t[thread].join();
			}
		} catch (InterruptedException ie) {
			IJ.error("A thread was interrupted in step 3 .");
		}
		// Find the largest distance for scaling
		// Also fill in the background values.
		float distMax = 0;
		final int wh = w * h;
		float dist;
		for (int k = 0; k < d; k++) {
			sk = s[k];
			for (int ind = 0; ind < wh; ind++) {
				if (((data[k][ind] & 255) < 128) ^ inv) {
					sk[ind] = 0;
				} else {
					dist = (float) Math.sqrt(sk[ind]);
					sk[ind] = dist;
					distMax = (dist > distMax) ? dist : distMax;
				}
			}
		}
		IJ.showProgress(1.0);
		IJ.showStatus("Done");
		return s;
	}

	class Step1Thread extends Thread {
		int thread, nThreads, w, h, d, thresh;
		float[][] s;
		byte[][] data;
		boolean inv;

		public Step1Thread(int thread, int nThreads, int w, int h, int d,
				boolean inv, float[][] s, byte[][] data) {
			this.thread = thread;
			this.nThreads = nThreads;
			this.w = w;
			this.h = h;
			this.d = d;
			this.inv = inv;
			this.data = data;
			this.s = s;
		}

		public void run() {
			final int width = this.w;
			final int height = this.h;
			final int depth = this.d;
			final boolean inverse = inv;
			float[] sk;
			int n = width;
			if (height > n)
				n = height;
			if (depth > n)
				n = depth;
			int noResult = 3 * (n + 1) * (n + 1);
			boolean[] background = new boolean[n];
			int test, min;
			for (int k = thread; k < depth; k += nThreads) {
				IJ.showProgress(k / (1. * depth));
				sk = s[k];
				final byte[] dk = data[k];
				for (int j = 0; j < height; j++) {
					final int wj = width * j;
					for (int i = 0; i < width; i++) {
						background[i] = ((dk[i + wj] & 255) < 128) ^ inverse;
					}
					for (int i = 0; i < width; i++) {
						min = noResult;
						for (int x = i; x < width; x++) {
							if (background[x]) {
								test = i - x;
								test *= test;
								min = test;
								break;
							}
						}
						for (int x = i - 1; x >= 0; x--) {
							if (background[x]) {
								test = i - x;
								test *= test;
								if (test < min)
									min = test;
								break;
							}
						}
						sk[i + wj] = min;
					}
				}
			}
		}// run
	}// Step1Thread

	class Step2Thread extends Thread {
		int thread, nThreads, w, h, d;
		float[][] s;

		public Step2Thread(int thread, int nThreads, int w, int h, int d,
				float[][] s) {
			this.thread = thread;
			this.nThreads = nThreads;
			this.w = w;
			this.h = h;
			this.d = d;
			this.s = s;
		}

		public void run() {
			final int width = this.w;
			final int height = this.h;
			final int depth = this.d;
			float[] sk;
			int n = width;
			if (height > n)
				n = height;
			if (depth > n)
				n = depth;
			int noResult = 3 * (n + 1) * (n + 1);
			int[] tempInt = new int[n];
			int[] tempS = new int[n];
			boolean nonempty;
			int test, min, delta;
			for (int k = thread; k < depth; k += nThreads) {
				IJ.showProgress(k / (1. * depth));
				sk = s[k];
				for (int i = 0; i < width; i++) {
					nonempty = false;
					for (int j = 0; j < height; j++) {
						tempS[j] = (int) sk[i + width * j];
						if (tempS[j] > 0)
							nonempty = true;
					}
					if (nonempty) {
						for (int j = 0; j < height; j++) {
							min = noResult;
							delta = j;
							for (int y = 0; y < height; y++) {
								test = tempS[y] + delta * delta--;
								if (test < min)
									min = test;
							}
							tempInt[j] = min;
						}
						for (int j = 0; j < height; j++) {
							sk[i + width * j] = tempInt[j];
						}
					}
				}
			}
		}// run
	}// Step2Thread

	class Step3Thread extends Thread {
		int thread, nThreads, w, h, d;
		float[][] s;
		byte[][] data;
		boolean inv;

		public Step3Thread(int thread, int nThreads, int w, int h, int d,
				boolean inv, float[][] s, byte[][] data) {
			this.thread = thread;
			this.nThreads = nThreads;
			this.w = w;
			this.h = h;
			this.d = d;
			this.s = s;
			this.data = data;
			this.inv = inv;
		}

		public void run() {
			final int width = this.w;
			final int height = this.h;
			final int depth = this.d;
			final byte[][] daTa = this.data;
			final boolean inverse = inv;
			int zStart, zStop, zBegin, zEnd;
			// float[] sk;
			int n = width;
			if (height > n)
				n = height;
			if (depth > n)
				n = depth;
			int noResult = 3 * (n + 1) * (n + 1);
			int[] tempInt = new int[n];
			int[] tempS = new int[n];
			boolean nonempty;
			int test, min, delta;
			for (int j = thread; j < height; j += nThreads) {
				final int wj = width * j;
				IJ.showProgress(j / (1. * height));
				for (int i = 0; i < width; i++) {
					nonempty = false;
					for (int k = 0; k < depth; k++) {
						tempS[k] = (int) s[k][i + wj];
						if (tempS[k] > 0)
							nonempty = true;
					}
					if (nonempty) {
						zStart = 0;
						while ((zStart < (depth - 1)) && (tempS[zStart] == 0))
							zStart++;
						if (zStart > 0)
							zStart--;
						zStop = depth - 1;
						while ((zStop > 0) && (tempS[zStop] == 0))
							zStop--;
						if (zStop < (depth - 1))
							zStop++;

						for (int k = 0; k < depth; k++) {
							// Limit to the non-background to save time,
							if (((daTa[k][i + wj] & 255) >= 128) ^ inverse) {
								min = noResult;
								zBegin = zStart;
								zEnd = zStop;
								if (zBegin > k)
									zBegin = k;
								if (zEnd < k)
									zEnd = k;
								delta = k - zBegin;
								for (int z = zBegin; z <= zEnd; z++) {
									test = tempS[z] + delta * delta--;
									if (test < min)
										min = test;
									// min = (test < min) ? test : min;
								}
								tempInt[k] = min;
							}
						}
						for (int k = 0; k < depth; k++) {
							s[k][i + wj] = tempInt[k];
						}
					}
				}
			}
		}
	}

	/**
	 * <p>
	 * DistanceMaptoDistanceRidge
	 * </p>
	 * <p>
	 * Output: Distance ridge resulting from a local scan of the distance map.
	 * Overwrites the input.
	 * </p>
	 * <p>
	 * Note: Non-background points that are not part of the distance ridge are
	 * assiged a VERY_SMALL_VALUE. This is used for subsequent processing by
	 * other plugins to find the local thickness. Bob Dougherty August 10, 2006
	 * </p>
	 * 
	 * <ul>
	 * <li>Version 1: August 10-11, 2006. Subtracts 0.5 from the distances.</li>
	 * <li>Version 1.01: September 6, 2006. Corrected some typos in the
	 * comments.</li>
	 * <li>Version 1.01: Sept. 7, 2006. More tiny edits.</li>
	 * <li>Version 2: Sept. 25, 2006. Creates a separate image stack for
	 * symmetry. <br />
	 * Temporary version that is very conservative. <br />
	 * Admittedly does not produce much impovement on real images.</li>
	 * <li>Version 3: Sept. 30, 2006. Ball calculations based on grid points.
	 * Should be much more accurate.</li>
	 * <li>Version 3.1 Oct. 1, 2006. Faster scanning of search points.</li>
	 * </ul>
	 * 
	 * @param imp
	 *            3D Distance map (32-bit stack)
	 */
	private void distanceMaptoDistanceRidge(ImagePlus imp, float[][] s) {
		final int w = imp.getWidth();
		final int h = imp.getHeight();
		final int d = imp.getStackSize();
		sNew = new float[d][];
		for (int k = 0; k < d; k++) {
			ImageProcessor ipk = new FloatProcessor(w, h);
			sNew[k] = (float[]) ipk.getPixels();
		}

		// Do it
		int k1, j1, i1, dz, dy, dx;
		boolean notRidgePoint;
		float[] sk1;
		float[] sk, skNew;
		int sk0Sq, sk0SqInd, sk1Sq;
		// Find the largest distance in the data
		IJ.showStatus("Distance Ridge: scanning the data");
		float distMax = 0;
		for (int k = 0; k < d; k++) {
			sk = s[k];
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					final int ind = i + wj;
					if (sk[ind] > distMax)
						distMax = sk[ind];
				}
			}
		}
		int rSqMax = (int) (distMax * distMax + 0.5f) + 1;
		boolean[] occurs = new boolean[rSqMax];
		for (int i = 0; i < rSqMax; i++)
			occurs[i] = false;
		for (int k = 0; k < d; k++) {
			sk = s[k];
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					final int ind = i + wj;
					occurs[(int) (sk[ind] * sk[ind] + 0.5f)] = true;
				}
			}
		}
		int numRadii = 0;
		for (int i = 0; i < rSqMax; i++) {
			if (occurs[i])
				numRadii++;
		}
		// Make an index of the distance-squared values
		int[] distSqIndex = new int[rSqMax];
		int[] distSqValues = new int[numRadii];
		int indDS = 0;
		for (int i = 0; i < rSqMax; i++) {
			if (occurs[i]) {
				distSqIndex[i] = indDS;
				distSqValues[indDS++] = i;
			}
		}
		// Build template
		// The first index of the template is the number of nonzero components
		// in the offest from the test point to the remote point. The second
		// index is the radii index (of the test point). The value of the
		// template
		// is the minimum square radius of the remote point required to cover
		// the
		// ball of the test point.
		IJ.showStatus("Distance Ridge: creating search templates");
		int[][] rSqTemplate = createTemplate(distSqValues);
		int numCompZ, numCompY, numCompX, numComp;
		for (int k = 0; k < d; k++) {
			IJ.showStatus("Distance Ridge: processing slice " + (k + 1) + "/"
					+ d);
			// IJ.showProgress(k/(1.*d));
			sk = s[k];
			skNew = sNew[k];
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					final int ind = i + wj;
					if (sk[ind] > 0) {
						notRidgePoint = false;
						sk0Sq = (int) (sk[ind] * sk[ind] + 0.5f);
						sk0SqInd = distSqIndex[sk0Sq];
						for (dz = -1; dz <= 1; dz++) {
							k1 = k + dz;
							if ((k1 >= 0) && (k1 < d)) {
								sk1 = s[k1];
								if (dz == 0) {
									numCompZ = 0;
								} else {
									numCompZ = 1;
								}
								for (dy = -1; dy <= 1; dy++) {
									j1 = j + dy;
									final int wj1 = w * j1;
									if ((j1 >= 0) && (j1 < h)) {
										if (dy == 0) {
											numCompY = 0;
										} else {
											numCompY = 1;
										}
										for (dx = -1; dx <= 1; dx++) {
											i1 = i + dx;
											if ((i1 >= 0) && (i1 < w)) {
												if (dx == 0) {
													numCompX = 0;
												} else {
													numCompX = 1;
												}
												numComp = numCompX + numCompY
														+ numCompZ;
												if (numComp > 0) {
													final float sk1i1wj1 = sk1[i1
															+ wj1];
													sk1Sq = (int) (sk1i1wj1
															* sk1i1wj1 + 0.5f);
													if (sk1Sq >= rSqTemplate[numComp - 1][sk0SqInd])
														notRidgePoint = true;
												}
											}// if in grid for i1
											if (notRidgePoint)
												break;
										}// dx
									}// if in grid for j1
									if (notRidgePoint)
										break;
								}// dy
							}// if in grid for k1
							if (notRidgePoint)
								break;
						}// dz
						if (!notRidgePoint)
							skNew[ind] = sk[ind];
					}// if not in background
				}// i
			}// j
		}// k
		IJ.showStatus("Distance Ridge complete");
		// replace work array s with result of the method, sNew
		s = sNew;
	}

	// For each offset from the origin, (dx,dy,dz), and each radius-squared,
	// rSq, find the smallest radius-squared, r1Squared, such that a ball
	// of radius r1 centered at (dx,dy,dz) includes a ball of radius
	// rSq centered at the origin. These balls refer to a 3D integer grid.
	// The set of (dx,dy,dz) points considered is a cube center at the origin.
	// The size of the computed array could be considerably reduced by symmetry,
	// but then the time for the calculation using this array would increase
	// (and more code would be needed).
	int[][] createTemplate(int[] distSqValues) {
		int[][] t = new int[3][];
		t[0] = scanCube(1, 0, 0, distSqValues);
		t[1] = scanCube(1, 1, 0, distSqValues);
		t[2] = scanCube(1, 1, 1, distSqValues);
		return t;
	}

	// For a list of r² values, find the smallest r1² values such
	// that a "ball" of radius r1 centered at (dx,dy,dz) includes a "ball"
	// of radius r centered at the origin. "Ball" refers to a 3D integer grid.
	int[] scanCube(int dx, int dy, int dz, int[] distSqValues) {
		final int numRadii = distSqValues.length;
		int[] r1Sq = new int[numRadii];
		if ((dx == 0) && (dy == 0) && (dz == 0)) {
			for (int rSq = 0; rSq < numRadii; rSq++) {
				r1Sq[rSq] = Integer.MAX_VALUE;
			}
		} else {
			final int dxAbs = -(int) Math.abs(dx);
			final int dyAbs = -(int) Math.abs(dy);
			final int dzAbs = -(int) Math.abs(dz);
			for (int rSqInd = 0; rSqInd < numRadii; rSqInd++) {
				final int rSq = distSqValues[rSqInd];
				int max = 0;
				final int r = 1 + (int) Math.sqrt(rSq);
				int scank, scankj;
				int dk, dkji;
				// int iBall;
				int iPlus;
				for (int k = 0; k <= r; k++) {
					scank = k * k;
					dk = (k - dzAbs) * (k - dzAbs);
					for (int j = 0; j <= r; j++) {
						scankj = scank + j * j;
						if (scankj <= rSq) {
							iPlus = ((int) Math.sqrt(rSq - scankj)) - dxAbs;
							dkji = dk + (j - dyAbs) * (j - dyAbs) + iPlus
									* iPlus;
							if (dkji > max)
								max = dkji;
						}
					}
				}
				r1Sq[rSqInd] = max;
			}
		}
		return r1Sq;
	}

	/**
	 * <p>
	 * DistanceRidgetoLocalThickness
	 * </p>
	 * <p>
	 * Input: Distance Ridge (32-bit stack) (Output from Distance Ridge.java)
	 * Output: Local Thickness. Overwrites the input.
	 * </p>
	 * <ul>
	 * <li>Version 1: September 6, 2006.</li>
	 * <li>Version 2: September 25, 2006. Fixed several bugs that resulted in
	 * non-symmetrical output from symmetrical input.</li>
	 * <li>Version 2.1 Oct. 1, 2006. Fixed a rounding error that caused some
	 * points to be missed.</li>
	 * <li>Version 3 July 31, 2007. Parallel processing version.</li>
	 * <li>Version 3.1 Multiplies the output by 2 to conform with the definition
	 * of local thickness</li>
	 * </ul>
	 * 
	 * @param imp
	 */
	private void distanceRidgetoLocalThickness(ImagePlus imp, float[][] s) {
		final int w = imp.getWidth();
		final int h = imp.getHeight();
		final int d = imp.getStackSize();
		float[] sk;
		// Count the distance ridge points on each slice
		int[] nRidge = new int[d];
		int ind, nr, iR;
		IJ.showStatus("Local Thickness: scanning stack ");
		for (int k = 0; k < d; k++) {
			sk = s[k];
			nr = 0;
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					ind = i + wj;
					if (sk[ind] > 0)
						nr++;
				}
			}
			nRidge[k] = nr;
		}
		int[][] iRidge = new int[d][];
		int[][] jRidge = new int[d][];
		float[][] rRidge = new float[d][];
		// Pull out the distance ridge points
		int[] iRidgeK, jRidgeK;
		float[] rRidgeK;
		float sMax = 0;
		for (int k = 0; k < d; k++) {
			nr = nRidge[k];
			iRidge[k] = new int[nr];
			jRidge[k] = new int[nr];
			rRidge[k] = new float[nr];
			sk = s[k];
			iRidgeK = iRidge[k];
			jRidgeK = jRidge[k];
			rRidgeK = rRidge[k];
			iR = 0;
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					ind = i + wj;
					if (sk[ind] > 0) {
						;
						iRidgeK[iR] = i;
						jRidgeK[iR] = j;
						rRidgeK[iR++] = sk[ind];
						if (sk[ind] > sMax)
							sMax = sk[ind];
						sk[ind] = 0;
					}
				}
			}
		}
		int nThreads = Runtime.getRuntime().availableProcessors();
		final Object[] resources = new Object[d];// For synchronization
		for (int k = 0; k < d; k++) {
			resources[k] = new Object();
		}
		LTThread[] ltt = new LTThread[nThreads];
		for (int thread = 0; thread < nThreads; thread++) {
			ltt[thread] = new LTThread(thread, nThreads, w, h, d, nRidge, s,
					iRidge, jRidge, rRidge, resources);
			ltt[thread].start();
		}
		try {
			for (int thread = 0; thread < nThreads; thread++) {
				ltt[thread].join();
			}
		} catch (InterruptedException ie) {
			IJ.error("A thread was interrupted .");
		}

		// Fix the square values and apply factor of 2
		IJ.showStatus("Local Thickness: square root ");
		for (int k = 0; k < d; k++) {
			sk = s[k];
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					ind = i + wj;
					sk[ind] = (float) (2 * Math.sqrt(sk[ind]));
				}
			}
		}
		IJ.showStatus("Local Thickness complete");
		return;
	}

	class LTThread extends Thread {
		int thread, nThreads, w, h, d, nR;
		float[][] s;
		int[] nRidge;
		int[][] iRidge, jRidge;
		float[][] rRidge;
		Object[] resources;

		public LTThread(int thread, int nThreads, int w, int h, int d,
				int[] nRidge, float[][] s, int[][] iRidge, int[][] jRidge,
				float[][] rRidge, Object[] resources) {
			this.thread = thread;
			this.nThreads = nThreads;
			this.w = w;
			this.h = h;
			this.d = d;
			this.s = s;
			this.nRidge = nRidge;
			this.iRidge = iRidge;
			this.jRidge = jRidge;
			this.rRidge = rRidge;
			this.resources = resources;
		}

		public void run() {
			final int width = this.w;
			final int height = this.h;
			final int depth = this.d;
			final float[][] stack = this.s;
			float[] sk1;// sk,sk1;
			// Loop through ridge points. For each one, update the local
			// thickness for
			// the points within its sphere.
			int rInt;
			int iStart, iStop, jStart, jStop, kStart, kStop;
			float r1SquaredK, r1SquaredJK, r1Squared, s1;
			int rSquared;
			for (int k = thread; k < depth; k += nThreads) {
				IJ.showStatus("Local Thickness: processing slice " + (k + 1)
						+ "/" + depth);
				final int nR = nRidge[k];
				final int[] iRidgeK = iRidge[k];
				final int[] jRidgeK = jRidge[k];
				final float[] rRidgeK = rRidge[k];
				for (int iR = 0; iR < nR; iR++) {
					final int i = iRidgeK[iR];
					final int j = jRidgeK[iR];
					final float r = rRidgeK[iR];
					rSquared = (int) (r * r + 0.5f);
					rInt = (int) r;
					if (rInt < r)
						rInt++;
					iStart = i - rInt;
					if (iStart < 0)
						iStart = 0;
					iStop = i + rInt;
					if (iStop >= width)
						iStop = width - 1;
					jStart = j - rInt;
					if (jStart < 0)
						jStart = 0;
					jStop = j + rInt;
					if (jStop >= height)
						jStop = height - 1;
					kStart = k - rInt;
					if (kStart < 0)
						kStart = 0;
					kStop = k + rInt;
					if (kStop >= depth)
						kStop = depth - 1;
					for (int k1 = kStart; k1 <= kStop; k1++) {
						r1SquaredK = (k1 - k) * (k1 - k);
						sk1 = stack[k1];
						for (int j1 = jStart; j1 <= jStop; j1++) {
							final int widthJ1 = width * j1;
							r1SquaredJK = r1SquaredK + (j1 - j) * (j1 - j);
							if (r1SquaredJK <= rSquared) {
								for (int i1 = iStart; i1 <= iStop; i1++) {
									r1Squared = r1SquaredJK + (i1 - i)
											* (i1 - i);
									if (r1Squared <= rSquared) {
										final int ind1 = i1 + widthJ1;
										s1 = sk1[ind1];
										if (rSquared > s1) {
											// Get a lock on sk1 and check again
											// to make sure
											// that another thread has not
											// increased
											// sk1[ind1] to something larger
											// than rSquared.
											// A test shows that this may not be
											// required...
											synchronized (resources[k1]) {
												s1 = sk1[ind1];
												if (rSquared > s1) {
													sk1[ind1] = rSquared;
												}
											}
										}
									}// if within sphere of DR point
								}// i1
							}// if k and j components within sphere of DR point
						}// j1
					}// k1
				}// iR
			}// k
		}// run
	}// LTThread

	/**
	 * <p>
	 * LocalThicknesstoCleanedUpLocalThickness
	 * </p>
	 * 
	 * <p>
	 * Input: 3D Local Thickness map (32-bit stack)
	 * </p>
	 * <p>
	 * Output: Same as input with border voxels corrected for "jaggies."
	 * Non-background voxels adjacent to background voxels are have their local
	 * thickness values replaced by the average of their non-background
	 * neighbors that do not border background points. Bob Dougherty August 1,
	 * 2007
	 * </p>
	 * 
	 * <ul>
	 * <li>August 10. Version 3 This version also multiplies the local thickness
	 * by 2 to conform with the official definition of local thickness.</li>
	 * </ul>
	 * 
	 */
	private ImagePlus localThicknesstoCleanedUpLocalThickness(ImagePlus imp,
			float[][] s) {
		final int w = imp.getWidth();
		final int h = imp.getHeight();
		final int d = imp.getStackSize();
		IJ.showStatus("Cleaning up local thickness...");
		// Create 32 bit floating point stack for output, sNew.
		ImageStack newStack = new ImageStack(w, h);
		sNew = new float[d][];
		for (int k = 0; k < d; k++) {
			ImageProcessor ipk = new FloatProcessor(w, h);
			newStack.addSlice(null, ipk);
			sNew[k] = (float[]) ipk.getPixels();
		}
		// First set the output array to flags:
		// 0 for a background point
		// -1 for a non-background point that borders a background point
		// s (input data) for an interior non-background point
		for (int k = 0; k < d; k++) {
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					sNew[k][i + wj] = setFlag(s, i, j, k, w, h, d);
				}// i
			}// j
		}// k
			// Process the surface points. Initially set results to negative
			// values
			// to be able to avoid including them in averages of for subsequent
			// points.
			// During the calculation, positive values in sNew are interior
			// non-background
			// local thicknesses. Negative values are surface points. In this
			// case
			// the
			// value might be -1 (not processed yet) or -result, where result is
			// the
			// average of the neighboring interior points. Negative values are
			// excluded from
			// the averaging.
		for (int k = 0; k < d; k++) {
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					final int ind = i + wj;
					if (sNew[k][ind] == -1) {
						sNew[k][ind] = -averageInteriorNeighbors(s, i, j, k, w,
								h, d);
					}
				}// i
			}// j
		}// k
			// Fix the negative values and double the results
		for (int k = 0; k < d; k++) {
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					final int ind = i + wj;
					sNew[k][ind] = (float) Math.abs(sNew[k][ind]);
				}// i
			}// j
		}// k
		IJ.showStatus("Clean Up Local Thickness complete");
		String title = stripExtension(imp.getTitle());
		ImagePlus impOut = new ImagePlus(title + "_CL", newStack);
		final double vW = imp.getCalibration().pixelWidth;
		// calibrate the pixel values to pixel width
		// so that thicknesses represent real units (not pixels)
		for (int z = 0; z < d; z++) {
			impOut.setSlice(z + 1);
			impOut.getProcessor().multiply(vW);
		}
		return impOut;
	}

	float setFlag(float[][] s, int i, int j, int k, int w, int h, int d) {
		if (s[k][i + w * j] == 0)
			return 0;
		// change 1
		if (look(s, i, j, k - 1, w, h, d) == 0)
			return -1;
		if (look(s, i, j, k + 1, w, h, d) == 0)
			return -1;
		if (look(s, i, j - 1, k, w, h, d) == 0)
			return -1;
		if (look(s, i, j + 1, k, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j, k, w, h, d) == 0)
			return -1;
		if (look(s, i + 1, j, k, w, h, d) == 0)
			return -1;
		// change 1 before plus
		if (look(s, i, j + 1, k - 1, w, h, d) == 0)
			return -1;
		if (look(s, i, j + 1, k + 1, w, h, d) == 0)
			return -1;
		if (look(s, i + 1, j - 1, k, w, h, d) == 0)
			return -1;
		if (look(s, i + 1, j + 1, k, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j, k + 1, w, h, d) == 0)
			return -1;
		if (look(s, i + 1, j, k + 1, w, h, d) == 0)
			return -1;
		// change 1 before minus
		if (look(s, i, j - 1, k - 1, w, h, d) == 0)
			return -1;
		if (look(s, i, j - 1, k + 1, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j - 1, k, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j + 1, k, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j, k - 1, w, h, d) == 0)
			return -1;
		if (look(s, i + 1, j, k - 1, w, h, d) == 0)
			return -1;
		// change 3, k+1
		if (look(s, i + 1, j + 1, k + 1, w, h, d) == 0)
			return -1;
		if (look(s, i + 1, j - 1, k + 1, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j + 1, k + 1, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j - 1, k + 1, w, h, d) == 0)
			return -1;
		// change 3, k-1
		if (look(s, i + 1, j + 1, k - 1, w, h, d) == 0)
			return -1;
		if (look(s, i + 1, j - 1, k - 1, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j + 1, k - 1, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j - 1, k - 1, w, h, d) == 0)
			return -1;
		return s[k][i + w * j];
	}

	float averageInteriorNeighbors(float[][] s, int i, int j, int k, int w,
			int h, int d) {
		int n = 0;
		float sum = 0;
		// change 1
		float value = lookNew(i, j, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i, j, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i, j - 1, k, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i, j + 1, k, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j, k, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i + 1, j, k, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		// change 1 before plus
		value = lookNew(i, j + 1, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i, j + 1, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i + 1, j - 1, k, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i + 1, j + 1, k, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i + 1, j, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		// change 1 before minus
		value = lookNew(i, j - 1, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i, j - 1, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j - 1, k, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j + 1, k, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i + 1, j, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		// change 3, k+1
		value = lookNew(i + 1, j + 1, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i + 1, j - 1, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j + 1, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j - 1, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		// change 3, k-1
		value = lookNew(i + 1, j + 1, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i + 1, j - 1, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j + 1, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j - 1, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		if (n > 0)
			return sum / n;
		return s[k][i + w * j];
	}

	float look(float[][] s, int i, int j, int k, int w, int h, int d) {
		if ((i < 0) || (i >= w))
			return -1;
		if ((j < 0) || (j >= h))
			return -1;
		if ((k < 0) || (k >= d))
			return -1;
		return s[k][i + w * j];
	}

	// A positive result means this is an interior, non-background, point.
	float lookNew(int i, int j, int k, int w, int h, int d) {
		if ((i < 0) || (i >= w))
			return -1;
		if ((j < 0) || (j >= h))
			return -1;
		if ((k < 0) || (k >= d))
			return -1;
		return sNew[k][i + w * j];
	}

	private void insertResults(ImagePlus imp, double[] stats, boolean inverse) {
		final double meanThick = stats[0];
		final double stDev = stats[1];
		final double maxThick = stats[2];
		final String units = imp.getCalibration().getUnits();

		ResultInserter ri = ResultInserter.getInstance();
		if (!inverse) {
			// trab thickness
			ri.setResultInRow(imp, "Tb.Th Mean (" + units + ")", meanThick);
			ri.setResultInRow(imp, "Tb.Th Std Dev (" + units + ")", stDev);
			ri.setResultInRow(imp, "Tb.Th Max (" + units + ")", maxThick);
		} else {
			// trab separation
			ri.setResultInRow(imp, "Tb.Sp Mean (" + units + ")", meanThick);
			ri.setResultInRow(imp, "Tb.Sp Std Dev (" + units + ")", stDev);
			ri.setResultInRow(imp, "Tb.Sp Max (" + units + ")", maxThick);
		}
		ri.updateTable();
		return;
	}

	/**
	 * Get a local thickness map from an ImagePlus
	 * 
	 * @param imp
	 *            Binary ImagePlus
	 * @param inv
	 *            false if you want the thickness of the foreground and true if
	 *            you want the thickness of the background
	 * @return 32-bit ImagePlus containing a local thickness map
	 */
	public ImagePlus getLocalThickness(ImagePlus imp, boolean inv) {
		if (!(new ImageCheck()).isVoxelIsotropic(imp, 1E-3)) {
			IJ.log("Warning: voxels are anisotropic. Local thickness results will be inaccurate");
		}
		float[][] s = geometryToDistanceMap(imp, inv);
		distanceMaptoDistanceRidge(imp, s);
		distanceRidgetoLocalThickness(imp, s);
		ImagePlus impLTC = localThicknesstoCleanedUpLocalThickness(imp, s);
		return impLTC;
	}
}
=======
/*******************************************************************************
 * Copyright (c) 2001-2012 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/epl-v10.html
 *
 * Contributors:
 *     Mathew A. Nelson
 *     - Initial API and implementation
 *     Flemming N. Larsen
 *     - Code cleanup
 *     - Bugfix: updateMovement() checked for distanceRemaining > 1 instead of
 *       distanceRemaining > 0 if slowingDown and moveDirection == -1
 *     - Bugfix: Substituted wait(10000) with wait() in execute() method, so
 *       that robots do not hang when game is paused
 *     - Bugfix: Teleportation when turning the robot to 0 degrees while forcing
 *       the robot towards the bottom
 *     - Added setPaintEnabled() and isPaintEnabled()
 *     - Added setSGPaintEnabled() and isSGPaintEnabled()
 *     - Replaced the colorIndex with bodyColor, gunColor, and radarColor
 *     - Replaced the setColors() with setBodyColor(), setGunColor(), and
 *       setRadarColor()
 *     - Added bulletColor, scanColor, setBulletColor(), and setScanColor() and
 *       removed getColorIndex()
 *     - Optimizations
 *     - Ported to Java 5
 *     - Bugfix: HitRobotEvent.isMyFault() returned false despite the fact that
 *       the robot was moving toward the robot it collides with. This was the
 *       case when distanceRemaining == 0
 *     - Removed isDead field as the robot state is used as replacement
 *     - Added isAlive() method
 *     - Added constructor for creating a new robot with a name only
 *     - Added the set() that copies a RobotRecord into this robot in order to
 *       support the replay feature
 *     - Fixed synchronization issues with several member fields
 *     - Added features to support the new JuniorRobot class
 *     - Added cleanupStaticFields() for clearing static fields on robots
 *     - Added getMaxTurnRate()
 *     - Added turnAndMove() in order to support the turnAheadLeft(),
 *       turnAheadRight(), turnBackLeft(), and turnBackRight() for the
 *       JuniorRobot, which moves the robot in a perfect curve that follows a
 *       circle
 *     - Changed the behaviour of checkRobotCollision() so that HitRobotEvents
 *       are only created and sent to robot when damage do occur. Previously, a
 *       robot could receive HitRobotEvents even when no damage was done
 *     - Renamed scanReset() to rescan()
 *     - Added getStatusEvents()
 *     - Added getGraphicsProxy(), getPaintEvents()
 *     Luis Crespo
 *     - Added states
 *     Titus Chen
 *     - Bugfix: Hit wall and teleporting problems with checkWallCollision()
 *     Robert D. Maupin
 *     - Replaced old collection types like Vector and Hashtable with
 *       synchronized List and HashMap
 *     Nathaniel Troutman
 *     - Added cleanup() method for cleaning up references to internal classes
 *       to prevent circular references causing memory leaks
 *     Pavel Savara
 *     - Re-work of robot interfaces
 *     - hosting related logic moved to robot proxy
 *     - interlocked synchronization
 *     - (almost) minimized surface between RobotPeer and RobotProxy to serializable messages.
 *******************************************************************************/
package net.sf.robocode.battle.peer;


import static net.sf.robocode.io.Logger.logMessage;
import net.sf.robocode.battle.Battle;
import net.sf.robocode.battle.BoundingRectangle;
import net.sf.robocode.host.IHostManager;
import net.sf.robocode.host.RobotStatics;
import net.sf.robocode.host.events.EventManager;
import net.sf.robocode.host.events.EventQueue;
import net.sf.robocode.host.proxies.IHostingRobotProxy;
import net.sf.robocode.io.Logger;
import net.sf.robocode.peer.*;
import net.sf.robocode.repository.IRobotRepositoryItem;
import net.sf.robocode.security.HiddenAccess;
import net.sf.robocode.serialization.RbSerializer;
import robocode.*;
import robocode.control.RandomFactory;
import robocode.control.RobotSpecification;
import robocode.control.snapshot.BulletState;
import robocode.control.snapshot.RobotState;
import robocode.exception.AbortedException;
import robocode.exception.DeathException;
import robocode.exception.WinException;
import static robocode.util.Utils.*;

import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import static java.lang.Math.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


/**
 * RobotPeer is an object that deals with game mechanics and rules, and makes
 * sure that robots abides the rules.
 *
 * @author Mathew A. Nelson (original)
 * @author Flemming N. Larsen (contributor)
 * @author Luis Crespo (contributor)
 * @author Titus Chen (contributor)
 * @author Robert D. Maupin (contributor)
 * @author Nathaniel Troutman (contributor)
 * @author Pavel Savara (contributor)
 * @author Patrick Cupka (contributor)
 * @author Julian Kent (contributor)
 * @author "Positive" (contributor)
 */
public final class RobotPeer implements IRobotPeerBattle, IRobotPeer {

	public static final int
			WIDTH = 40,
			HEIGHT = 40;

	private static final int
			HALF_WIDTH_OFFSET = (WIDTH / 2 - 2),
			HALF_HEIGHT_OFFSET = (HEIGHT / 2 - 2);

	private static final int MAX_SKIPPED_TURNS = 30;
	private static final int MAX_SKIPPED_TURNS_WITH_IO = 240;

	private Battle battle;
	private RobotStatistics statistics;
	private final TeamPeer teamPeer;
	private final RobotSpecification robotSpecification;

	private IHostingRobotProxy robotProxy;
	private AtomicReference<RobotStatus> status = new AtomicReference<RobotStatus>();
	private AtomicReference<ExecCommands> commands = new AtomicReference<ExecCommands>();
	private AtomicReference<EventQueue> events = new AtomicReference<EventQueue>(new EventQueue());
	private AtomicReference<List<TeamMessage>> teamMessages = new AtomicReference<List<TeamMessage>>(
			new ArrayList<TeamMessage>());
	private AtomicReference<List<BulletStatus>> bulletUpdates = new AtomicReference<List<BulletStatus>>(
			new ArrayList<BulletStatus>());

	// thread is running
	private final AtomicBoolean isRunning = new AtomicBoolean(false);

	private final StringBuilder battleText = new StringBuilder(1024);
	private final StringBuilder proxyText = new StringBuilder(1024);
	private RobotStatics statics;
	private BattleRules battleRules;

	// for battle thread, during robots processing
	private ExecCommands currentCommands;
	private double lastHeading;
	private double lastGunHeading;
	private double lastRadarHeading;

	private double energy;
	private double velocity;
	private double bodyHeading;
	private double radarHeading;
	private double gunHeading;
	private double gunHeat;
	private double x;
	private double y;
	private int skippedTurns;

	private boolean scan;
	private boolean turnedRadarWithGun; // last round

	private boolean isIORobot;
	private boolean isPaintEnabled;
	private boolean sgPaintEnabled;

	// waiting for next tick
	private final AtomicBoolean isSleeping = new AtomicBoolean(false);
	private final AtomicBoolean halt = new AtomicBoolean(false);

	private boolean isExecFinishedAndDisabled;
	private boolean isEnergyDrained;
	private boolean isWinner;
	private boolean inCollision;
	private boolean isOverDriving;

	private RobotState state;
	private final Arc2D scanArc;
	private final BoundingRectangle boundingBox;
	private final RbSerializer rbSerializer;

	public RobotPeer(Battle battle, IHostManager hostManager, RobotSpecification robotSpecification, int duplicate, TeamPeer team, int index, int contestantIndex) {
		super();
		if (team != null) {
			team.add(this);
		}

		rbSerializer = new RbSerializer();

		this.battle = battle;
		boundingBox = new BoundingRectangle();
		scanArc = new Arc2D.Double();
		teamPeer = team;
		state = RobotState.ACTIVE;
		battleRules = battle.getBattleRules();

		this.robotSpecification = robotSpecification;

		boolean isLeader = teamPeer != null && teamPeer.size() == 1;
		String teamName = team == null ? null : team.getName();
		List<String> teamMembers = team == null ? null : team.getMemberNames(); 

		statics = new RobotStatics(robotSpecification, duplicate, isLeader, battleRules, teamName, teamMembers, index,
				contestantIndex);
		statistics = new RobotStatistics(this, battle.getRobotsCount());

		robotProxy = (IHostingRobotProxy) hostManager.createRobotProxy(robotSpecification, statics, this);
	}

	public void println(String s) {
		synchronized (proxyText) {
			battleText.append(s);
			battleText.append("\n");
		}
	}

	public void print(Throwable ex) {
		println(ex.toString());
		StackTraceElement[] trace = ex.getStackTrace();

		for (StackTraceElement aTrace : trace) {
			println("\tat " + aTrace);
		}

		Throwable ourCause = ex.getCause();

		if (ourCause != null) {
			print(ourCause);
		}
	}

	public void printProxy(String s) {
		synchronized (proxyText) {
			proxyText.append(s);
		}
	}

	public String readOutText() {
		synchronized (proxyText) {
			final String robotText = battleText.toString() + proxyText.toString();

			battleText.setLength(0);
			proxyText.setLength(0);
			return robotText;
		}
	}

	public RobotStatistics getRobotStatistics() {
		return statistics;
	}

	public ContestantStatistics getStatistics() {
		return statistics;
	}

	public RobotSpecification getRobotSpecification() {
		return robotSpecification;
	}

	// -------------------
	// statics 
	// -------------------

	public boolean isDroid() {
		return statics.isDroid();
	}

	public boolean isJuniorRobot() {
		return statics.isJuniorRobot();
	}

	public boolean isInteractiveRobot() {
		return statics.isInteractiveRobot();
	}

	public boolean isPaintRobot() {
		return statics.isPaintRobot();
	}

	public boolean isAdvancedRobot() {
		return statics.isAdvancedRobot();
	}

	public boolean isTeamRobot() {
		return statics.isTeamRobot();
	}

	public String getName() {
		return statics.getName();
	}

	public String getAnnonymousName() {
		return statics.getAnnonymousName();
	}

	public String getShortName() {
		return statics.getShortName();
	}

	public String getVeryShortName() {
		return statics.getVeryShortName();
	}

	public int getIndex() {
		return statics.getIndex();
	}

	public int getContestIndex() {
		return statics.getContestIndex();
	}

	// -------------------
	// status 
	// -------------------

	public void setPaintEnabled(boolean enabled) {
		isPaintEnabled = enabled;
	}

	public boolean isPaintEnabled() {
		return isPaintEnabled;
	}

	public void setSGPaintEnabled(boolean enabled) {
		sgPaintEnabled = enabled;
	}

	public boolean isSGPaintEnabled() {
		return sgPaintEnabled;
	}

	public RobotState getState() {
		return state;
	}

	public void setState(RobotState state) {
		this.state = state;
	}

	public boolean isDead() {
		return state == RobotState.DEAD;
	}

	public boolean isAlive() {
		return state != RobotState.DEAD;
	}

	public boolean isWinner() {
		return isWinner;
	}

	public boolean isRunning() {
		return isRunning.get();
	}

	public boolean isSleeping() {
		return isSleeping.get();
	}

	public boolean isHalt() {
		return halt.get();
	}

	public void setHalt(boolean value) {
		halt.set(value);
	}

	public BoundingRectangle getBoundingBox() {
		return boundingBox;
	}

	public Arc2D getScanArc() {
		return scanArc;
	}

	// -------------------
	// robot space
	// -------------------

	public double getGunHeading() {
		return gunHeading;
	}

	public double getBodyHeading() {
		return bodyHeading;
	}

	public double getRadarHeading() {
		return radarHeading;
	}

	public double getVelocity() {
		return velocity;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getEnergy() {
		return energy;
	}

	public double getGunHeat() {
		return gunHeat;
	}

	public int getBodyColor() {
		return commands.get().getBodyColor();
	}

	public int getRadarColor() {
		return commands.get().getRadarColor();
	}

	public int getGunColor() {
		return commands.get().getGunColor();
	}

	public int getBulletColor() {
		return commands.get().getBulletColor();
	}

	public int getScanColor() {
		return commands.get().getScanColor();
	}

	// ------------
	// team
	// ------------

	public TeamPeer getTeamPeer() {
		return teamPeer;
	}

	public String getTeamName() {
		return statics.getTeamName();
	}

	public boolean isTeamLeader() {
		return statics.isTeamLeader();
	}

	public boolean isTeamMate(RobotPeer otherRobot) {
		if (getTeamPeer() != null) {
			for (RobotPeer mate : getTeamPeer()) {
				if (otherRobot == mate) {
					return true;
				}
			}	
		}
		return false;
	}
	
	// -----------
	// execute
	// -----------

	ByteBuffer bidirectionalBuffer;

	public void setupBuffer(ByteBuffer bidirectionalBuffer) {
		this.bidirectionalBuffer = bidirectionalBuffer;
	}

	public void setupThread() {
		Thread.currentThread().setName(getName());
	}

	public void executeImplSerial() throws IOException {
		ExecCommands commands = (ExecCommands) rbSerializer.deserialize(bidirectionalBuffer);

		final ExecResults results = executeImpl(commands);

		bidirectionalBuffer.clear();
		rbSerializer.serializeToBuffer(bidirectionalBuffer, RbSerializer.ExecResults_TYPE, results);
	}

	public void waitForBattleEndImplSerial() throws IOException {
		ExecCommands commands = (ExecCommands) rbSerializer.deserialize(bidirectionalBuffer);

		final ExecResults results = waitForBattleEndImpl(commands);

		bidirectionalBuffer.clear();
		rbSerializer.serializeToBuffer(bidirectionalBuffer, RbSerializer.ExecResults_TYPE, results);
	}

	public final ExecResults executeImpl(ExecCommands newCommands) {
		validateCommands(newCommands);

		if (!isExecFinishedAndDisabled) {
			// from robot to battle
			commands.set(new ExecCommands(newCommands, true));
			printProxy(newCommands.getOutputText());
		} else {
			// slow down spammer
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		// If we are stopping, yet the robot took action (in onWin or onDeath), stop now.
		if (battle.isAborted()) {
			isExecFinishedAndDisabled = true;
			throw new AbortedException();
		}
		if (isDead()) {
			isExecFinishedAndDisabled = true;
			throw new DeathException();
		}
		if (isHalt()) {
			isExecFinishedAndDisabled = true;
			if (isWinner) {
				throw new WinException();
			} else {
				throw new AbortedException();
			}
		}

		waitForNextTurn();

		// from battle to robot
		final ExecCommands resCommands = new ExecCommands(this.commands.get(), false);
		final RobotStatus resStatus = status.get();

		final boolean shouldWait = battle.isAborted() || (battle.isLastRound() && isWinner());

		return new ExecResults(resCommands, resStatus, readoutEvents(), readoutTeamMessages(), readoutBullets(),
				isHalt(), shouldWait, isPaintEnabled());
	}

	public final ExecResults waitForBattleEndImpl(ExecCommands newCommands) {
		if (!isHalt()) {
			// from robot to battle
			commands.set(new ExecCommands(newCommands, true));
			printProxy(newCommands.getOutputText());

			waitForNextTurn();
		}
		// from battle to robot
		final ExecCommands resCommands = new ExecCommands(this.commands.get(), false);
		final RobotStatus resStatus = status.get();

		final boolean shouldWait = battle.isAborted() || (battle.isLastRound() && !isWinner());

		readoutTeamMessages(); // throw away
		
		return new ExecResults(resCommands, resStatus, readoutEvents(), new ArrayList<TeamMessage>(), readoutBullets(),
				isHalt(), shouldWait, false);
	}

	private void validateCommands(ExecCommands newCommands) {
		if (Double.isNaN(newCommands.getMaxTurnRate())) {
			println("You cannot setMaxTurnRate to: " + newCommands.getMaxTurnRate());
		}
		newCommands.setMaxTurnRate(Math.min(abs(newCommands.getMaxTurnRate()), Rules.MAX_TURN_RATE_RADIANS));

		if (Double.isNaN(newCommands.getMaxVelocity())) {
			println("You cannot setMaxVelocity to: " + newCommands.getMaxVelocity());
		}
		newCommands.setMaxVelocity(Math.min(abs(newCommands.getMaxVelocity()), Rules.MAX_VELOCITY));
	}

	private List<Event> readoutEvents() {
		return events.getAndSet(new EventQueue());
	}

	private List<TeamMessage> readoutTeamMessages() {
		return teamMessages.getAndSet(new ArrayList<TeamMessage>());
	}

	private List<BulletStatus> readoutBullets() {
		return bulletUpdates.getAndSet(new ArrayList<BulletStatus>());
	}

	private void waitForNextTurn() {
		synchronized (isSleeping) {
			// Notify the battle that we are now asleep.
			// This ends any pending wait() call in battle.runRound().
			// Should not actually take place until we release the lock in wait(), below.
			isSleeping.set(true);
			isSleeping.notifyAll();
			// Notifying battle that we're asleep
			// Sleeping and waiting for battle to wake us up.
			try {
				isSleeping.wait();
			} catch (InterruptedException e) {
				// We are expecting this to happen when a round is ended!

				// Immediately reasserts the exception by interrupting the caller thread itself
				Thread.currentThread().interrupt();
			}
			isSleeping.set(false);
			// Notify battle thread, which is waiting in
			// our wakeup() call, to return.
			// It's quite possible, by the way, that we'll be back in sleep (above)
			// before the battle thread actually wakes up
			isSleeping.notifyAll();
		}
	}

	// -----------
	// called on battle thread
	// -----------

	public void waitWakeup() {
		synchronized (isSleeping) {
			if (isSleeping()) {
				// Wake up the thread
				isSleeping.notifyAll();
				try {
					isSleeping.wait(10000);
				} catch (InterruptedException e) {
					// Immediately reasserts the exception by interrupting the caller thread itself
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	public void waitWakeupNoWait() {
		synchronized (isSleeping) {
			if (isSleeping()) {
				// Wake up the thread
				isSleeping.notifyAll();
			}
		}
	}

	public void waitSleeping(long millisWait, int nanosWait) {
		synchronized (isSleeping) {
			// It's quite possible for simple robots to
			// complete their processing before we get here,
			// so we test if the robot is already asleep.

			if (!isSleeping()) {
				try {
					for (long i = millisWait; i > 0 && !isSleeping() && isRunning(); i--) {
						isSleeping.wait(0, 999999);
					}
					if (!isSleeping() && isRunning()) {
						isSleeping.wait(0, nanosWait);
					}
				} catch (InterruptedException e) {
					// Immediately reasserts the exception by interrupting the caller thread itself
					Thread.currentThread().interrupt();

					logMessage("Wait for " + getName() + " interrupted.");
				}
			}
		}
	}

	public void setSkippedTurns() {
		if (isHalt() || isSleeping() || !isRunning() || battle.isDebugging() || isPaintEnabled()) {
			skippedTurns = 0;
		} else {
			println("SYSTEM: " + getShortName() + " skipped turn " + battle.getTime());
			skippedTurns++;
			events.get().clear(false);
			if (!isDead()) {
				addEvent(new SkippedTurnEvent(battle.getTime()));
			}

			if ((!isIORobot && (skippedTurns > MAX_SKIPPED_TURNS))
					|| (isIORobot && (skippedTurns > MAX_SKIPPED_TURNS_WITH_IO))) {
				println("SYSTEM: " + getShortName() + " has not performed any actions in a reasonable amount of time.");
				println("SYSTEM: No score will be generated.");
				setHalt(true);
				waitWakeupNoWait();
				punishBadBehavior(BadBehavior.SKIPPED_TOO_MANY_TURNS);
				robotProxy.forceStopThread();
			}
		}
	}

	public void initializeRound(List<RobotPeer> robots, double[][] initialRobotPositions) {
		boolean valid = false;

		if (initialRobotPositions != null) {

			if (statics.getIndex() >= 0 && statics.getIndex() < initialRobotPositions.length) {
				double[] pos = initialRobotPositions[statics.getIndex()];

				x = pos[0];
				y = pos[1];
				bodyHeading = pos[2];
				gunHeading = radarHeading = bodyHeading;
				updateBoundingBox();
				valid = validSpot(robots);
			}
		}

		if (!valid) {
			final Random random = RandomFactory.getRandom();

			for (int j = 0; j < 1000; j++) {
				x = RobotPeer.WIDTH + random.nextDouble() * (battleRules.getBattlefieldWidth() - 2 * RobotPeer.WIDTH);
				y = RobotPeer.HEIGHT + random.nextDouble() * (battleRules.getBattlefieldHeight() - 2 * RobotPeer.HEIGHT);
				bodyHeading = 2 * Math.PI * random.nextDouble();
				gunHeading = radarHeading = bodyHeading;
				updateBoundingBox();

				if (validSpot(robots)) {
					break;
				}
			}
		}

		setState(RobotState.ACTIVE);

		isWinner = false;
		velocity = 0;

		if (statics.isTeamLeader() && statics.isDroid()) {
			energy = 220;
		} else if (statics.isTeamLeader()) {
			energy = 200;
		} else if (statics.isDroid()) {
			energy = 120;
		} else {
			energy = 100;
		}
		gunHeat = 3;

		setHalt(false);
		isExecFinishedAndDisabled = false;
		isEnergyDrained = false;

		scan = false;

		inCollision = false;

		scanArc.setAngleStart(0);
		scanArc.setAngleExtent(0);
		scanArc.setFrame(-100, -100, 1, 1);

		skippedTurns = 0;

		status = new AtomicReference<RobotStatus>();

		readoutEvents();
		readoutTeamMessages();
		readoutBullets();

		battleText.setLength(0);
		proxyText.setLength(0);

		// Prepare new execution commands, but copy the colors from the last commands.
		// Bugfix [2628217] - Robot Colors don't stick between rounds.
		ExecCommands newExecCommands = new ExecCommands();

		newExecCommands.copyColors(commands.get());
		commands = new AtomicReference<ExecCommands>(newExecCommands);
	}

	private boolean validSpot(List<RobotPeer> robots) {
		for (RobotPeer otherRobot : robots) {
			if (otherRobot != null && otherRobot != this) {
				if (getBoundingBox().intersects(otherRobot.getBoundingBox())) {
					return false;
				}
			}
		}
		return true;
	}

	public void startRound(long waitMillis, int waitNanos) {
		Logger.logMessage(".", false);

		statistics.initialize();

		ExecCommands newExecCommands = new ExecCommands();

		// Copy the colors from the last commands.
		// Bugfix [2628217] - Robot Colors don't stick between rounds.
		newExecCommands.copyColors(commands.get());

		currentCommands = newExecCommands;
		int others = battle.getActiveRobots() - (isAlive() ? 1 : 0);
		RobotStatus stat = HiddenAccess.createStatus(energy, x, y, bodyHeading, gunHeading, radarHeading, velocity,
				currentCommands.getBodyTurnRemaining(), currentCommands.getRadarTurnRemaining(),
				currentCommands.getGunTurnRemaining(), currentCommands.getDistanceRemaining(), gunHeat, others,
				battle.getRoundNum(), battle.getNumRounds(), battle.getTime());

		status.set(stat);
		robotProxy.startRound(currentCommands, stat);

		synchronized (isSleeping) {
			try {
				// Wait for the robot to go to sleep (take action)
				isSleeping.wait(waitMillis, waitNanos);
			} catch (InterruptedException e) {
				logMessage("Wait for " + getName() + " interrupted.");

				// Immediately reasserts the exception by interrupting the caller thread itself
				Thread.currentThread().interrupt();
			}
		}
		if (!isSleeping() && !battle.isDebugging()) {
			logMessage("\n" + getName() + " still has not started after " + waitMillis + " ms... giving up.");
		}
	}

	public void performLoadCommands() {
		currentCommands = commands.get();

		fireBullets(currentCommands.getBullets());

		if (currentCommands.isScan()) {
			scan = true;
		}

		if (currentCommands.isIORobot()) {
			isIORobot = true;
		}

		if (currentCommands.isMoved()) {
			currentCommands.setMoved(false);
		}
	}

	private void fireBullets(List<BulletCommand> bulletCommands) {
		BulletPeer newBullet = null;

		for (BulletCommand bulletCmd : bulletCommands) {
			if (Double.isNaN(bulletCmd.getPower())) {
				println("SYSTEM: You cannot call fire(NaN)");
				continue;
			}
			if (gunHeat > 0 || energy == 0) {
				return;
			}

			double firePower = min(energy,
					min(max(bulletCmd.getPower(), Rules.MIN_BULLET_POWER), Rules.MAX_BULLET_POWER));

			updateEnergy(-firePower);

			gunHeat += Rules.getGunHeat(firePower);

			newBullet = new BulletPeer(this, battleRules, bulletCmd.getBulletId());

			newBullet.setPower(firePower);
			if (!turnedRadarWithGun || !bulletCmd.isFireAssistValid() || statics.isAdvancedRobot()) {
				newBullet.setHeading(gunHeading);
			} else {
				newBullet.setHeading(bulletCmd.getFireAssistAngle());
			}
			newBullet.setX(x);
			newBullet.setY(y);
		}
		// there is only last bullet in one turn
		if (newBullet != null) {
			// newBullet.update(robots, bullets);
			battle.addBullet(newBullet);
		}
	}

	public final void performMove(List<RobotPeer> robots, double zapEnergy) {

		// Reset robot state to active if it is not dead
		if (isDead()) {
			return;
		}

		setState(RobotState.ACTIVE);

		updateGunHeat();

		lastHeading = bodyHeading;
		lastGunHeading = gunHeading;
		lastRadarHeading = radarHeading;
		final double lastX = x;
		final double lastY = y;

		if (!inCollision) {
			updateHeading();
		}

		updateGunHeading();
		updateRadarHeading();
		updateMovement();

		// At this point, robot has turned then moved.
		// We could be touching a wall or another bot...

		// First and foremost, we can never go through a wall:
		checkWallCollision();

		// Now check for robot collision
		checkRobotCollision(robots);

		// Scan false means robot did not call scan() manually.
		// But if we're moving, scan
		if (!scan) {
			scan = (lastHeading != bodyHeading || lastGunHeading != gunHeading || lastRadarHeading != radarHeading
					|| lastX != x || lastY != y);
		}

		if (isDead()) {
			return;
		}

		// zap
		if (zapEnergy != 0) {
			zap(zapEnergy);
		}
	}

	public void performScan(List<RobotPeer> robots) {
		if (isDead()) {
			return;
		}

		turnedRadarWithGun = false;
		// scan
		if (scan) {
			scan(lastRadarHeading, robots);
			turnedRadarWithGun = (lastGunHeading == lastRadarHeading) && (gunHeading == radarHeading);
			scan = false;
		}

		// dispatch messages
		if (statics.isTeamRobot() && teamPeer != null) {
			for (TeamMessage teamMessage : currentCommands.getTeamMessages()) {
				for (RobotPeer member : teamPeer) {
					if (checkDispatchToMember(member, teamMessage.recipient)) {
						member.addTeamMessage(teamMessage);
					}
				}
			}
		}
		currentCommands = null;
		lastHeading = -1;
		lastGunHeading = -1;
		lastRadarHeading = -1;
	}

	private void addTeamMessage(TeamMessage message) {
		final List<TeamMessage> queue = teamMessages.get();

		queue.add(message);
	}

	private boolean checkDispatchToMember(RobotPeer member, String recipient) {
		if (member.isAlive()) {
			if (recipient == null) {
				if (member != this) {
					return true;
				}
			} else {
				final int nl = recipient.length();
				final String currentName = member.statics.getName();

				if ((currentName.length() >= nl && currentName.substring(0, nl).equals(recipient))) {
					return true;
				}

				final String currentClassName = member.statics.getFullClassName();

				if ((currentClassName.length() >= nl && currentClassName.substring(0, nl).equals(recipient))) {
					return true;
				}

			}
		}
		return false;
	}

	public String getNameForEvent(RobotPeer otherRobot) {
		if (battleRules.getHideEnemyNames() && !isTeamMate(otherRobot)) {
			return otherRobot.getAnnonymousName();
		}
		return otherRobot.getName();
	}		

	private void checkRobotCollision(List<RobotPeer> robots) {
		inCollision = false;

		for (RobotPeer otherRobot : robots) {
			if (!(otherRobot == null || otherRobot == this || otherRobot.isDead())
					&& boundingBox.intersects(otherRobot.boundingBox)) {
				// Bounce back
				double angle = atan2(otherRobot.x - x, otherRobot.y - y);

				double movedx = velocity * sin(bodyHeading);
				double movedy = velocity * cos(bodyHeading);

				boolean atFault;
				double bearing = normalRelativeAngle(angle - bodyHeading);

				if ((velocity > 0 && bearing > -PI / 2 && bearing < PI / 2)
						|| (velocity < 0 && (bearing < -PI / 2 || bearing > PI / 2))) {

					inCollision = true;
					atFault = true;
					velocity = 0;
					currentCommands.setDistanceRemaining(0);
					x -= movedx;
					y -= movedy;

					boolean teamFire = (teamPeer != null && teamPeer == otherRobot.teamPeer);

					if (!teamFire) {
						statistics.scoreRammingDamage(otherRobot.getName());
					}

					this.updateEnergy(-Rules.ROBOT_HIT_DAMAGE);
					otherRobot.updateEnergy(-Rules.ROBOT_HIT_DAMAGE);

					if (otherRobot.energy == 0) {
						if (otherRobot.isAlive()) {
							otherRobot.kill();
							if (!teamFire) {
								final double bonus = statistics.scoreRammingKill(otherRobot.getName());

								if (bonus > 0) {
									println(
											"SYSTEM: Ram bonus for killing " + this.getNameForEvent(otherRobot) + ": "
											+ (int) (bonus + .5));
								}
							}
						}
					}
					addEvent(
							new HitRobotEvent(getNameForEvent(otherRobot), normalRelativeAngle(angle - bodyHeading),
							otherRobot.energy, atFault));
					otherRobot.addEvent(
							new HitRobotEvent(getNameForEvent(this),
							normalRelativeAngle(PI + angle - otherRobot.getBodyHeading()), energy, false));
				}
			}
		}
		if (inCollision) {
			setState(RobotState.HIT_ROBOT);
		}
	}

	private void checkWallCollision() {
		boolean hitWall = false;
		double fixx = 0, fixy = 0;
		double angle = 0;

		if (x > getBattleFieldWidth() - HALF_WIDTH_OFFSET) {
			hitWall = true;
			fixx = getBattleFieldWidth() - HALF_WIDTH_OFFSET - x;
			angle = normalRelativeAngle(PI / 2 - bodyHeading);
		}

		if (x < HALF_WIDTH_OFFSET) {
			hitWall = true;
			fixx = HALF_WIDTH_OFFSET - x;
			angle = normalRelativeAngle(3 * PI / 2 - bodyHeading);
		}

		if (y > getBattleFieldHeight() - HALF_HEIGHT_OFFSET) {
			hitWall = true;
			fixy = getBattleFieldHeight() - HALF_HEIGHT_OFFSET - y;
			angle = normalRelativeAngle(-bodyHeading);
		}

		if (y < HALF_HEIGHT_OFFSET) {
			hitWall = true;
			fixy = HALF_HEIGHT_OFFSET - y;
			angle = normalRelativeAngle(PI - bodyHeading);
		}

		if (hitWall) {
			addEvent(new HitWallEvent(angle));

			// only fix both x and y values if hitting wall at an angle
			if ((bodyHeading % (Math.PI / 2)) != 0) {
				double tanHeading = tan(bodyHeading);

				// if it hits bottom or top wall
				if (fixx == 0) {
					fixx = fixy * tanHeading;
				} // if it hits a side wall
				else if (fixy == 0) {
					fixy = fixx / tanHeading;
				} // if the robot hits 2 walls at the same time (rare, but just in case)
				else if (abs(fixx / tanHeading) > abs(fixy)) {
					fixy = fixx / tanHeading;
				} else if (abs(fixy * tanHeading) > abs(fixx)) {
					fixx = fixy * tanHeading;
				}
			}
			x += fixx;
			y += fixy;

			x = (HALF_WIDTH_OFFSET >= x)
					? HALF_WIDTH_OFFSET
					: ((getBattleFieldWidth() - HALF_WIDTH_OFFSET < x) ? getBattleFieldWidth() - HALF_WIDTH_OFFSET : x);
			y = (HALF_HEIGHT_OFFSET >= y)
					? HALF_HEIGHT_OFFSET
					: ((getBattleFieldHeight() - HALF_HEIGHT_OFFSET < y) ? getBattleFieldHeight() - HALF_HEIGHT_OFFSET : y);

			// Update energy, but do not reset inactiveTurnCount
			if (statics.isAdvancedRobot()) {
				setEnergy(energy - Rules.getWallHitDamage(velocity), false);
			}

			updateBoundingBox();

			currentCommands.setDistanceRemaining(0);
			velocity = 0;
		}
		if (hitWall) {
			setState(RobotState.HIT_WALL);
		}
	}

	private double getBattleFieldHeight() {
		return battleRules.getBattlefieldHeight();
	}

	private double getBattleFieldWidth() {
		return battleRules.getBattlefieldWidth();
	}

	public void updateBoundingBox() {
		boundingBox.setRect(x - WIDTH / 2 + 2, y - HEIGHT / 2 + 2, WIDTH - 4, HEIGHT - 4);
	}

	public void addEvent(Event event) {
		if (isRunning()) {
			final EventQueue queue = events.get();

			if ((queue.size() > EventManager.MAX_QUEUE_SIZE)
					&& !(event instanceof DeathEvent || event instanceof WinEvent || event instanceof SkippedTurnEvent)) {
				println(
						"Not adding to " + statics.getShortName() + "'s queue, exceeded " + EventManager.MAX_QUEUE_SIZE
						+ " events in queue.");
				// clean up old stuff                
				queue.clear(battle.getTime() - EventManager.MAX_EVENT_STACK);
				return;
			}
			queue.add(event);
		}
	}

	private void updateGunHeading() {
		if (currentCommands.getGunTurnRemaining() > 0) {
			if (currentCommands.getGunTurnRemaining() < Rules.GUN_TURN_RATE_RADIANS) {
				gunHeading += currentCommands.getGunTurnRemaining();
				radarHeading += currentCommands.getGunTurnRemaining();
				if (currentCommands.isAdjustRadarForGunTurn()) {
					currentCommands.setRadarTurnRemaining(
							currentCommands.getRadarTurnRemaining() - currentCommands.getGunTurnRemaining());
				}
				currentCommands.setGunTurnRemaining(0);
			} else {
				gunHeading += Rules.GUN_TURN_RATE_RADIANS;
				radarHeading += Rules.GUN_TURN_RATE_RADIANS;
				currentCommands.setGunTurnRemaining(currentCommands.getGunTurnRemaining() - Rules.GUN_TURN_RATE_RADIANS);
				if (currentCommands.isAdjustRadarForGunTurn()) {
					currentCommands.setRadarTurnRemaining(
							currentCommands.getRadarTurnRemaining() - Rules.GUN_TURN_RATE_RADIANS);
				}
			}
		} else if (currentCommands.getGunTurnRemaining() < 0) {
			if (currentCommands.getGunTurnRemaining() > -Rules.GUN_TURN_RATE_RADIANS) {
				gunHeading += currentCommands.getGunTurnRemaining();
				radarHeading += currentCommands.getGunTurnRemaining();
				if (currentCommands.isAdjustRadarForGunTurn()) {
					currentCommands.setRadarTurnRemaining(
							currentCommands.getRadarTurnRemaining() - currentCommands.getGunTurnRemaining());
				}
				currentCommands.setGunTurnRemaining(0);
			} else {
				gunHeading -= Rules.GUN_TURN_RATE_RADIANS;
				radarHeading -= Rules.GUN_TURN_RATE_RADIANS;
				currentCommands.setGunTurnRemaining(currentCommands.getGunTurnRemaining() + Rules.GUN_TURN_RATE_RADIANS);
				if (currentCommands.isAdjustRadarForGunTurn()) {
					currentCommands.setRadarTurnRemaining(
							currentCommands.getRadarTurnRemaining() + Rules.GUN_TURN_RATE_RADIANS);
				}
			}
		}
		gunHeading = normalAbsoluteAngle(gunHeading);
	}

	private void updateHeading() {
		boolean normalizeHeading = true;

		double turnRate = min(currentCommands.getMaxTurnRate(),
				(.4 + .6 * (1 - (abs(velocity) / Rules.MAX_VELOCITY))) * Rules.MAX_TURN_RATE_RADIANS);

		if (currentCommands.getBodyTurnRemaining() > 0) {
			if (currentCommands.getBodyTurnRemaining() < turnRate) {
				bodyHeading += currentCommands.getBodyTurnRemaining();
				gunHeading += currentCommands.getBodyTurnRemaining();
				radarHeading += currentCommands.getBodyTurnRemaining();
				if (currentCommands.isAdjustGunForBodyTurn()) {
					currentCommands.setGunTurnRemaining(
							currentCommands.getGunTurnRemaining() - currentCommands.getBodyTurnRemaining());
				}
				if (currentCommands.isAdjustRadarForBodyTurn()) {
					currentCommands.setRadarTurnRemaining(
							currentCommands.getRadarTurnRemaining() - currentCommands.getBodyTurnRemaining());
				}
				currentCommands.setBodyTurnRemaining(0);
			} else {
				bodyHeading += turnRate;
				gunHeading += turnRate;
				radarHeading += turnRate;
				currentCommands.setBodyTurnRemaining(currentCommands.getBodyTurnRemaining() - turnRate);
				if (currentCommands.isAdjustGunForBodyTurn()) {
					currentCommands.setGunTurnRemaining(currentCommands.getGunTurnRemaining() - turnRate);
				}
				if (currentCommands.isAdjustRadarForBodyTurn()) {
					currentCommands.setRadarTurnRemaining(currentCommands.getRadarTurnRemaining() - turnRate);
				}
			}
		} else if (currentCommands.getBodyTurnRemaining() < 0) {
			if (currentCommands.getBodyTurnRemaining() > -turnRate) {
				bodyHeading += currentCommands.getBodyTurnRemaining();
				gunHeading += currentCommands.getBodyTurnRemaining();
				radarHeading += currentCommands.getBodyTurnRemaining();
				if (currentCommands.isAdjustGunForBodyTurn()) {
					currentCommands.setGunTurnRemaining(
							currentCommands.getGunTurnRemaining() - currentCommands.getBodyTurnRemaining());
				}
				if (currentCommands.isAdjustRadarForBodyTurn()) {
					currentCommands.setRadarTurnRemaining(
							currentCommands.getRadarTurnRemaining() - currentCommands.getBodyTurnRemaining());
				}
				currentCommands.setBodyTurnRemaining(0);
			} else {
				bodyHeading -= turnRate;
				gunHeading -= turnRate;
				radarHeading -= turnRate;
				currentCommands.setBodyTurnRemaining(currentCommands.getBodyTurnRemaining() + turnRate);
				if (currentCommands.isAdjustGunForBodyTurn()) {
					currentCommands.setGunTurnRemaining(currentCommands.getGunTurnRemaining() + turnRate);
				}
				if (currentCommands.isAdjustRadarForBodyTurn()) {
					currentCommands.setRadarTurnRemaining(currentCommands.getRadarTurnRemaining() + turnRate);
				}
			}
		} else {
			normalizeHeading = false;
		}

		if (normalizeHeading) {
			if (currentCommands.getBodyTurnRemaining() == 0) {
				bodyHeading = normalNearAbsoluteAngle(bodyHeading);
			} else {
				bodyHeading = normalAbsoluteAngle(bodyHeading);
			}
		}
		if (Double.isNaN(bodyHeading)) {
			Logger.realErr.println("HOW IS HEADING NAN HERE");
		}
	}

	private void updateRadarHeading() {
		if (currentCommands.getRadarTurnRemaining() > 0) {
			if (currentCommands.getRadarTurnRemaining() < Rules.RADAR_TURN_RATE_RADIANS) {
				radarHeading += currentCommands.getRadarTurnRemaining();
				currentCommands.setRadarTurnRemaining(0);
			} else {
				radarHeading += Rules.RADAR_TURN_RATE_RADIANS;
				currentCommands.setRadarTurnRemaining(
						currentCommands.getRadarTurnRemaining() - Rules.RADAR_TURN_RATE_RADIANS);
			}
		} else if (currentCommands.getRadarTurnRemaining() < 0) {
			if (currentCommands.getRadarTurnRemaining() > -Rules.RADAR_TURN_RATE_RADIANS) {
				radarHeading += currentCommands.getRadarTurnRemaining();
				currentCommands.setRadarTurnRemaining(0);
			} else {
				radarHeading -= Rules.RADAR_TURN_RATE_RADIANS;
				currentCommands.setRadarTurnRemaining(
						currentCommands.getRadarTurnRemaining() + Rules.RADAR_TURN_RATE_RADIANS);
			}
		}

		radarHeading = normalAbsoluteAngle(radarHeading);
	}

	/**
	 * Updates the robots movement.
	 *
	 * This is Nat Pavasants method described here:
	 *   http://robowiki.net/wiki/User:Positive/Optimal_Velocity#Nat.27s_updateMovement
	 */
	private void updateMovement() {
		double distance = currentCommands.getDistanceRemaining();

		if (Double.isNaN(distance)) {
			distance = 0;
		}

		velocity = getNewVelocity(velocity, distance);

		// If we are over-driving our distance and we are now at velocity=0
		// then we stopped.
		if (isNear(velocity, 0) && isOverDriving) {
			currentCommands.setDistanceRemaining(0);
			distance = 0;
			isOverDriving = false;
		}

		// If we are moving normally and the breaking distance is more
		// than remaining distance, enabled the overdrive flag.
		if (Math.signum(distance * velocity) != -1) {
			if (getDistanceTraveledUntilStop(velocity) > Math.abs(distance)) {
				isOverDriving = true;
			} else {
				isOverDriving = false;
			}
		}

		currentCommands.setDistanceRemaining(distance - velocity);

		if (velocity != 0) {
			x += velocity * sin(bodyHeading);
			y += velocity * cos(bodyHeading);
			updateBoundingBox();
		}
	}

	private double getDistanceTraveledUntilStop(double velocity) {
		double distance = 0;

		velocity = Math.abs(velocity);
		while (velocity > 0) {
			distance += (velocity = getNewVelocity(velocity, 0));
		}
		return distance;
	}

	/**
	 * Returns the new velocity based on the current velocity and distance to move.
	 *
	 * @param velocity the current velocity
	 * @param distance the distance to move
	 * @return the new velocity based on the current velocity and distance to move
	 * 
	 * This is Patrick Cupka (aka Voidious), Julian Kent (aka Skilgannon), and Positive's method described here:
	 *   http://robowiki.net/wiki/User:Voidious/Optimal_Velocity#Hijack_2
	 */
	private double getNewVelocity(double velocity, double distance) {
		if (distance < 0) {
			// If the distance is negative, then change it to be positive
			// and change the sign of the input velocity and the result
			return -getNewVelocity(-velocity, -distance);
		}

		final double goalVel;

		if (distance == Double.POSITIVE_INFINITY) {
			goalVel = currentCommands.getMaxVelocity();
		} else {
			goalVel = Math.min(getMaxVelocity(distance), currentCommands.getMaxVelocity());
		}

		if (velocity >= 0) {
			return Math.max(velocity - Rules.DECELERATION, Math.min(goalVel, velocity + Rules.ACCELERATION));
		}
		// else
		return Math.max(velocity - Rules.ACCELERATION, Math.min(goalVel, velocity + maxDecel(-velocity)));
	}

	final static double getMaxVelocity(double distance) {
		final double decelTime = Math.max(1, Math.ceil(// sum of 0... decelTime, solving for decelTime using quadratic formula
				(Math.sqrt((4 * 2 / Rules.DECELERATION) * distance + 1) - 1) / 2));

		if (decelTime == Double.POSITIVE_INFINITY) {
			return Rules.MAX_VELOCITY;
		}

		final double decelDist = (decelTime / 2.0) * (decelTime - 1) // sum of 0..(decelTime-1)
				* Rules.DECELERATION;

		return ((decelTime - 1) * Rules.DECELERATION) + ((distance - decelDist) / decelTime);
	}

	private static double maxDecel(double speed) {
		double decelTime = speed / Rules.DECELERATION;
		double accelTime = (1 - decelTime);

		return Math.min(1, decelTime) * Rules.DECELERATION + Math.max(0, accelTime) * Rules.ACCELERATION;
	}

	private void updateGunHeat() {
		gunHeat -= battleRules.getGunCoolingRate();
		if (gunHeat < 0) {
			gunHeat = 0;
		}
	}

	private void scan(double lastRadarHeading, List<RobotPeer> robots) {
		if (statics.isDroid()) {
			return;
		}

		double startAngle = lastRadarHeading;
		double scanRadians = getRadarHeading() - startAngle;

		// Check if we passed through 360
		if (scanRadians < -PI) {
			scanRadians = 2 * PI + scanRadians;
		} else if (scanRadians > PI) {
			scanRadians = scanRadians - 2 * PI;
		}

		// In our coords, we are scanning clockwise, with +y up
		// In java coords, we are scanning counterclockwise, with +y down
		// All we need to do is adjust our angle by -90 for this to work.
		startAngle -= PI / 2;

		startAngle = normalAbsoluteAngle(startAngle);

		scanArc.setArc(x - Rules.RADAR_SCAN_RADIUS, y - Rules.RADAR_SCAN_RADIUS, 2 * Rules.RADAR_SCAN_RADIUS,
				2 * Rules.RADAR_SCAN_RADIUS, 180.0 * startAngle / PI, 180.0 * scanRadians / PI, Arc2D.PIE);

		for (RobotPeer otherRobot : robots) {
			if (!(otherRobot == null || otherRobot == this || otherRobot.isDead())
					&& intersects(scanArc, otherRobot.boundingBox)) {
				double dx = otherRobot.x - x;
				double dy = otherRobot.y - y;
				double angle = atan2(dx, dy);
				double dist = Math.hypot(dx, dy);

				final ScannedRobotEvent event = new ScannedRobotEvent(getNameForEvent(otherRobot), otherRobot.energy,
						normalRelativeAngle(angle - getBodyHeading()), dist, otherRobot.getBodyHeading(),
						otherRobot.getVelocity());

				addEvent(event);
			}
		}
	}

	private boolean intersects(Arc2D arc, Rectangle2D rect) {
		return (rect.intersectsLine(arc.getCenterX(), arc.getCenterY(), arc.getStartPoint().getX(),
				arc.getStartPoint().getY()))
				|| arc.intersects(rect);
	}

	private void zap(double zapAmount) {
		if (energy == 0) {
			kill();
			return;
		}
		energy -= abs(zapAmount);
		if (energy < .1) {
			energy = 0;
			currentCommands.setDistanceRemaining(0);
			currentCommands.setBodyTurnRemaining(0);
		}
	}

	public void setRunning(boolean value) {
		isRunning.set(value);
	}

	public void drainEnergy() {
		setEnergy(0, true);
		isEnergyDrained = true;
	}

	public void punishBadBehavior(BadBehavior badBehavior) {
		kill(); // Bug fix [2828479] - Missed onRobotDeath events

		statistics.setInactive();

		final IRobotRepositoryItem repositoryItem = (IRobotRepositoryItem) HiddenAccess.getFileSpecification(
				robotSpecification);

		StringBuffer message = new StringBuffer(getName()).append(' ');

		boolean disableInRepository = false; // Per default, robots are not disabled in the repository

		switch (badBehavior) {
		case CANNOT_START:
			message.append("could not be started or loaded.");
			disableInRepository = true; // Disable in repository when it cannot be started anyways
			break;

		case UNSTOPPABLE:
			message.append("cannot be stopped.");
			break;

		case SKIPPED_TOO_MANY_TURNS:
			message.append("has skipped too many turns.");
			break;

		case SECURITY_VIOLATION:
			message.append("has caused a security violation.");
			disableInRepository = true; // No mercy here!
			break;
		}

		if (disableInRepository) {
			repositoryItem.setValid(false);			
			message.append(" This ").append(repositoryItem.isTeam() ? "team" : "robot").append(
					" has been banned and will not be allowed to participate in battles.");
		}

		logMessage(message.toString());
	}

	public void updateEnergy(double delta) {
		if ((!isExecFinishedAndDisabled && !isEnergyDrained) || delta < 0) {
			setEnergy(energy + delta, true);
		}
	}

	private void setEnergy(double newEnergy, boolean resetInactiveTurnCount) {
		if (resetInactiveTurnCount && (energy != newEnergy)) {
			battle.resetInactiveTurnCount(energy - newEnergy);
		}
		energy = newEnergy;
		if (energy < .01) {
			energy = 0;
			ExecCommands localCommands = commands.get();

			localCommands.setDistanceRemaining(0);
			localCommands.setBodyTurnRemaining(0);
		}
	}

	public void setWinner(boolean newWinner) {
		isWinner = newWinner;
	}

	public void kill() {
		battle.resetInactiveTurnCount(10.0);
		if (isAlive()) {
			addEvent(new DeathEvent());
			if (statics.isTeamLeader()) {
				for (RobotPeer teammate : teamPeer) {
					if (!(teammate.isDead() || teammate == this)) {
						teammate.updateEnergy(-30);

						BulletPeer sBullet = new BulletPeer(this, battleRules, -1);

						sBullet.setState(BulletState.HIT_VICTIM);
						sBullet.setX(teammate.x);
						sBullet.setY(teammate.y);
						sBullet.setVictim(teammate);
						sBullet.setPower(4);
						battle.addBullet(sBullet);
					}
				}
			}
			battle.registerDeathRobot(this);

			// 'fake' bullet for explosion on self
			final ExplosionPeer fake = new ExplosionPeer(this, battleRules);

			battle.addBullet(fake);
		}
		updateEnergy(-energy);

		setState(RobotState.DEAD);
	}

	public void waitForStop() {
		robotProxy.waitForStopThread();
	}

	/**
	 * Clean things up removing all references to the robot.
	 */
	public void cleanup() {
		battle = null;

		if (robotProxy != null) {
			robotProxy.cleanup();
			robotProxy = null;
		}

		if (statistics != null) {
			statistics.cleanup();
			statistics = null;
		}

		status = null;
		commands = null;
		events = null;
		teamMessages = null;
		bulletUpdates = null;
		battleText.setLength(0);
		proxyText.setLength(0);
		statics = null;
		battleRules = null;
	}

	public Object getGraphicsCalls() {
		return commands.get().getGraphicsCalls();
	}

	public boolean isTryingToPaint() {
		return commands.get().isTryingToPaint();
	}

	public List<DebugProperty> getDebugProperties() {
		return commands.get().getDebugProperties();
	}

	public void publishStatus(long currentTurn) {

		final ExecCommands currentCommands = commands.get();
		int others = battle.getActiveRobots() - (isAlive() ? 1 : 0);
		RobotStatus stat = HiddenAccess.createStatus(energy, x, y, bodyHeading, gunHeading, radarHeading, velocity,
				currentCommands.getBodyTurnRemaining(), currentCommands.getRadarTurnRemaining(),
				currentCommands.getGunTurnRemaining(), currentCommands.getDistanceRemaining(), gunHeat, others,
				battle.getRoundNum(), battle.getNumRounds(), battle.getTime());

		status.set(stat);
	}

	public void addBulletStatus(BulletStatus bulletStatus) {
		if (isAlive()) {
			bulletUpdates.get().add(bulletStatus);
		}
	}

	public int compareTo(ContestantPeer cp) {
		double myScore = statistics.getTotalScore();
		double hisScore = cp.getStatistics().getTotalScore();

		if (statistics.isInRound()) {
			myScore += statistics.getCurrentScore();
			hisScore += cp.getStatistics().getCurrentScore();
		}
		if (myScore < hisScore) {
			return -1;
		}
		if (myScore > hisScore) {
			return 1;
		}
		return 0;
	}

	@Override
	public String toString() {
		return statics.getShortName() + "(" + (int) energy + ") X" + (int) x + " Y" + (int) y + " " + state.toString()
				+ (isSleeping() ? " sleeping " : "") + (isRunning() ? " running" : "") + (isHalt() ? " halted" : "");
	}
}

>>>>>>> 76aa07461566a5976980e6696204781271955163
