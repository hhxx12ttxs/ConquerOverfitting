/*
 * #%L
 * ImgLib: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2012 Stephan Preibisch, Stephan Saalfeld, Tobias
 * Pietzsch, Albert Cardona, Barry DeZonia, Curtis Rueden, Lee Kamentsky, Larry
 * Lindsey, Johannes Schindelin, Christian Dietz, Grant Harris, Jean-Yves
 * Tinevez, Steffen Jaensch, Mark Longair, Nick Perry, and Jan Funke.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package tests;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ijaux.Constants;
import ijaux.PixLib;
import ijaux.hypergeom.BaseIndex;
import ijaux.hypergeom.PixelCube;
import ijaux.iter.array.ByteForwardIterator;
import ijaux.iter.seq.RasterForwardIterator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.container.array.Array;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.container.basictypecontainer.ByteAccess;
import mpicbg.imglib.container.basictypecontainer.array.ByteArray;
import mpicbg.imglib.container.cell.CellContainerFactory;
import mpicbg.imglib.container.planar.PlanarContainer;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.array.ArrayCursor;
import mpicbg.imglib.cursor.cell.CellCursor;
import mpicbg.imglib.cursor.imageplus.ImagePlusCursor;
import mpicbg.imglib.cursor.planar.PlanarCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.image.ImagePlusAdapter;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;

/**
 * Tests performance of uint8 image operations with
 * raw byte array, ImageJ, imglib and pixlib libraries.
 *
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class PerformanceBenchmark {

	private static final boolean SAVE_RESULTS_TO_DISK = true;

	private static final String METHOD_RAW = "Raw";
	private static final String METHOD_IMAGEJ = "ImageJ";
	private static final String METHOD_IMGLIB_ARRAY = "Imglib (Array)";
	private static final String METHOD_IMGLIB_CELL = "Imglib (Cell)";
	private static final String METHOD_IMGLIB_PLANAR = "Imglib (Planar)";
	private static final String METHOD_IMGLIB_IMAGEPLUS = "Imglib (ImagePlus)";
	private static final String METHOD_PIXLIB_REFLECTION = "Pixlib (Reflection)";
	private static final String METHOD_PIXLIB_GENERIC = "Pixlib (Generic Array)";
	private static final String METHOD_PIXLIB_BYTE = "Pixlib (Byte Array)";

	private final int width, height;
	private final byte[] rawData;
	private final ByteProcessor byteProc;
	private final Image<UnsignedByteType> imgArray;
	private final Image<UnsignedByteType> imgCell;
	private final Image<UnsignedByteType> imgPlanar;
	private final Image<UnsignedByteType> imgImagePlus;
	private final PixelCube<Byte, BaseIndex> pixelCube;

	/**
	 * List of timing results.
	 *
	 * Each element of the list represents an iteration.
	 * Each entry maps the method name to the time measured.
	 */
	private final List<Map<String, Long>> results =
		new ArrayList<Map<String, Long>>();

	public static void main(final String[] args) throws IOException {
		final int iterations = 10;
		final int size;
		if (args.length > 0) size = Integer.parseInt(args[0]);
		else size = 4000;
		final PerformanceBenchmark bench = new PerformanceBenchmark(size);
		bench.testPerformance(iterations);
		System.exit(0);
	}

	/** Creates objects and measures memory usage. */
	public PerformanceBenchmark(final int imageSize) {
		width = height = imageSize;
		System.out.println();
		System.out.println("===== " + width + " x " + height + " =====");

		final List<Long> memUsage = new ArrayList<Long>();
		memUsage.add(getMemUsage());
		rawData = createRawData(width, height);
		memUsage.add(getMemUsage());
		byteProc = createByteProcessor(rawData, width, height);
		memUsage.add(getMemUsage());
		imgArray = createArrayImage(rawData, width, height);
		memUsage.add(getMemUsage());
		imgCell = createCellImage(rawData, width, height);
		memUsage.add(getMemUsage());
		imgPlanar = createPlanarImage(rawData, width, height);
		memUsage.add(getMemUsage());
		imgImagePlus = createImagePlusImage(byteProc);
		memUsage.add(getMemUsage());
		pixelCube = createPixelCube(byteProc);
		memUsage.add(getMemUsage());

		reportMemoryUsage(memUsage);
	}

	public void testPerformance(final int iterationCount) throws IOException {		
		// initialize results map
		results.clear();
		for (int i = 0; i < iterationCount; i++) {
			final Map<String, Long> entry = new HashMap<String, Long>();
			results.add(entry);
		}
		testCheapPerformance(iterationCount);
		if (SAVE_RESULTS_TO_DISK) saveResults("cheap");
		testExpensivePerformance(iterationCount);
		if (SAVE_RESULTS_TO_DISK) saveResults("expensive");
	}

	/**
	 * Saves benchmark results to the given CSV file on disk.
	 *
	 * The motivation is to produce two charts:
	 *
	 * 1) performance by iteration number (on each size of image)
	 *    one line graph per method
	 *    X axis = iteration number
	 *    Y axis = time needed
	 *
	 * 2) average performance by image size (for first iteration, and 10th)
	 *    one line graph per method
	 *    X axis = size of image
	 *    Y axis = time needed
	 *
	 * The CSV file produced enables graph #1 very easily.
	 * For graph #2, results from several files must be combined.
	 */
	public void saveResults(final String prefix) throws IOException {
		final StringBuilder sb = new StringBuilder();

		// write header
		final Map<String, Long> firstEntry = results.get(0);
		final String[] methods = firstEntry.keySet().toArray(new String[0]);
		Arrays.sort(methods);
		sb.append("Iteration");
		for (final String method : methods) {
			sb.append("\t");
			sb.append(method);
		}
		sb.append("\n");

		// write data
		for (int iter = 0; iter < results.size(); iter++) {
			final Map<String, Long> entry = results.get(iter);
			sb.append(iter + 1);
			for (String method : methods) {
				sb.append("\t");
				sb.append(entry.get(method));
			}
			sb.append("\n");
		}

		// write to disk
		final String path = "results-" + prefix + "-" + width + "x" + height + ".csv";
		final PrintWriter out = new PrintWriter(new FileWriter(path));
		out.print(sb.toString());
		out.close();
	}

	// -- Helper methods --

	/** Measures performance of a cheap operation (image inversion). */
	private void testCheapPerformance(final int iterationCount) {
		System.out.println();
		System.out.println("-- TIME PERFORMANCE - CHEAP OPERATION --");
		for (int i = 0; i < iterationCount; i++) {
			System.gc();
			System.out.println("Iteration #" + (i + 1) + "/" + iterationCount + ":");
			final List<Long> times = new ArrayList<Long>();
			times.add(System.currentTimeMillis());
			invertRaw(rawData);
			times.add(System.currentTimeMillis());
			invertImageProcessor(byteProc);
			times.add(System.currentTimeMillis());
			invertArrayImage(imgArray);
			times.add(System.currentTimeMillis());
			invertCellImage(imgCell);
			times.add(System.currentTimeMillis());
			invertPlanarImage(imgPlanar);
			times.add(System.currentTimeMillis());
			invertImagePlusImage(imgImagePlus);
			times.add(System.currentTimeMillis());
			invertPixelCubeReflection(pixelCube);
			times.add(System.currentTimeMillis());
			invertPixelCubeGenArray(pixelCube);
			times.add(System.currentTimeMillis());
			invertPixelCubeByteArray(pixelCube);
			times.add(System.currentTimeMillis());

			logTimePerformance(i, times);
		}
	}

	/** Measures performance of a computationally more expensive operation. */
	private void testExpensivePerformance(final int iterationCount) {
		System.out.println();
		System.out.println("-- TIME PERFORMANCE - EXPENSIVE OPERATION --");
		for (int i = 0; i < iterationCount; i++) {
			System.gc();
			System.out.println("Iteration #" + (i + 1) + "/" + iterationCount + ":");
			final List<Long> times = new ArrayList<Long>();
			times.add(System.currentTimeMillis());
			randomizeRaw(rawData);
			times.add(System.currentTimeMillis());
			randomizeImageProcessor(byteProc);
			times.add(System.currentTimeMillis());
			randomizeArrayImage(imgArray);
			times.add(System.currentTimeMillis());
			randomizeCellImage(imgCell);
			times.add(System.currentTimeMillis());
			randomizePlanarImage(imgPlanar);
			times.add(System.currentTimeMillis());
			randomizeImagePlusImage(imgImagePlus);
			times.add(System.currentTimeMillis());
			randomizePixelCubeReflection(pixelCube);
			times.add(System.currentTimeMillis());
			randomizePixelCubeGenArray(pixelCube);
			times.add(System.currentTimeMillis());
			randomizePixelCubeByteArray(pixelCube);
			times.add(System.currentTimeMillis());

			logTimePerformance(i, times);
		}
	}

	private long getMemUsage() {
		Runtime r = Runtime.getRuntime();
		System.gc();
		System.gc();
		return r.totalMemory() - r.freeMemory();
	}

	private void reportMemoryUsage(final List<Long> memUsage) {
		final long rawMem             = computeDifference(memUsage);
		final long ipMem              = computeDifference(memUsage);
		final long imgLibArrayMem     = computeDifference(memUsage);
		final long imgLibCellMem      = computeDifference(memUsage);
		final long imgLibPlanarMem    = computeDifference(memUsage);
		final long imgLibImagePlusMem = computeDifference(memUsage);
		final long pixLibMem          = computeDifference(memUsage);
		System.out.println();
		System.out.println("-- MEMORY OVERHEAD --");
		System.out.println(METHOD_RAW + ": " + rawMem + " bytes");
		System.out.println(METHOD_IMAGEJ + ": " + ipMem + " bytes");
		System.out.println(METHOD_IMGLIB_ARRAY + ": " + imgLibArrayMem + " bytes");
		System.out.println(METHOD_IMGLIB_CELL + ": " + imgLibCellMem + " bytes");
		System.out.println(METHOD_IMGLIB_PLANAR + ": " + imgLibPlanarMem + " bytes");
		System.out.println(METHOD_IMGLIB_IMAGEPLUS + ": " + imgLibImagePlusMem + " bytes");
		System.out.println("PixLib: " + pixLibMem + " bytes");
	}

	private void logTimePerformance(final int iter, final List<Long> times) {
		final long rawTime             = computeDifference(times);
		final long ipTime              = computeDifference(times);
		final long imgLibArrayTime     = computeDifference(times);
		final long imgLibCellTime      = computeDifference(times);
		final long imgLibPlanarTime    = computeDifference(times);
		final long imgLibImagePlusTime = computeDifference(times);
		final long pixLibReflTime      = computeDifference(times);
		final long pixLibGenArrayTime  = computeDifference(times);
		final long pixLibByteArrayTime = computeDifference(times);

		final Map<String, Long> entry = results.get(iter);
		entry.put(METHOD_RAW, rawTime);
		entry.put(METHOD_IMAGEJ, ipTime);
		entry.put(METHOD_IMGLIB_ARRAY, imgLibArrayTime);
		entry.put(METHOD_IMGLIB_CELL, imgLibCellTime);
		entry.put(METHOD_IMGLIB_PLANAR, imgLibPlanarTime);
		entry.put(METHOD_IMGLIB_IMAGEPLUS, imgLibImagePlusTime);
		entry.put(METHOD_PIXLIB_REFLECTION, pixLibReflTime);
		entry.put(METHOD_PIXLIB_GENERIC, pixLibGenArrayTime);
		entry.put(METHOD_PIXLIB_BYTE, pixLibByteArrayTime);

		reportTime(METHOD_RAW, rawTime, rawTime, ipTime);
		reportTime(METHOD_IMAGEJ, ipTime, rawTime, ipTime);
		reportTime(METHOD_IMGLIB_ARRAY, imgLibArrayTime, rawTime, ipTime);
		reportTime(METHOD_IMGLIB_CELL, imgLibCellTime, rawTime, ipTime);
		reportTime(METHOD_IMGLIB_PLANAR, imgLibPlanarTime, rawTime, ipTime);
		reportTime(METHOD_IMGLIB_IMAGEPLUS, imgLibImagePlusTime, rawTime, ipTime);
		reportTime(METHOD_PIXLIB_REFLECTION, pixLibReflTime, rawTime, ipTime);
		reportTime(METHOD_PIXLIB_GENERIC, pixLibGenArrayTime, rawTime, ipTime);
		reportTime(METHOD_PIXLIB_BYTE, pixLibByteArrayTime, rawTime, ipTime);
	}

	private long computeDifference(final List<Long> list) {
		long mem = list.remove(0);
		return list.get(0) - mem;
	}

	private void reportTime(final String label, final long time, final long... otherTimes) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t");
		sb.append(label);
		sb.append(": ");
		sb.append(time);
		sb.append(" ms");
		for (long otherTime : otherTimes) {
			sb.append(", ");
			sb.append(time / (float) otherTime);
		}
		System.out.println(sb.toString());
	}

	// -- Creation methods --

	private byte[] createRawData(final int w, final int h) {
		final int size = w * h;
		final int max = w + h;
		byte[] data = new byte[size];
		int index = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				data[index++] = (byte) (255 * (x + y) / max);
			}
		}
		return data;
	}

	private ByteProcessor createByteProcessor(final byte[] data, final int w, final int h) {
		return new ByteProcessor(w, h, data, null);
	}

	private PixelCube<Byte, BaseIndex> createPixelCube(ByteProcessor bp) {
		final PixLib<Byte, BaseIndex> plib = new PixLib<Byte, BaseIndex>();
		PixelCube<Byte, BaseIndex> pc = plib.cubeFrom(bp, Constants.BASE_INDEXING);
		return pc;
	}

	private Image<UnsignedByteType> createArrayImage(final byte[] data, final int w, final int h) {
		//return createImage(data, width, height, new ArrayContainerFactory());
		// NB: Avoid copying the data.
		final ByteAccess byteAccess = new ByteArray(data);
		final Array<UnsignedByteType, ByteAccess> array = new Array<UnsignedByteType, ByteAccess>(new ArrayContainerFactory(), byteAccess, new int[] {w, h}, 1);
		array.setLinkedType(new UnsignedByteType(array));
		return new Image<UnsignedByteType>(array, new UnsignedByteType());
		//return DevUtil.createImageFromArray(data, new int[] {width, height});
	}

	private Image<UnsignedByteType> createPlanarImage(final byte[] data, final int w, final int h) {
		//return createImage(data, width, height, new PlanarContainerFactory());
		// NB: Avoid copying the data.
		PlanarContainer<UnsignedByteType, ByteArray> planarContainer = new PlanarContainer<UnsignedByteType, ByteArray>(new int[] {w, h}, 1);
		planarContainer.setPlane(0, new ByteArray(data));
		planarContainer.setLinkedType(new UnsignedByteType(planarContainer));
		return new Image<UnsignedByteType>(planarContainer, new UnsignedByteType());
	}

	private Image<UnsignedByteType> createCellImage(final byte[] data, final int w, final int h) {
		return createImage(data, w, h, new CellContainerFactory());
	}

	private Image<UnsignedByteType> createImagePlusImage(final ImageProcessor ip) {
		final ImagePlus imp = new ImagePlus("image", ip);
		return ImagePlusAdapter.wrapByte(imp);
	}

	private Image<UnsignedByteType> createImage(final byte[] data, final int w, final int h, final ContainerFactory cf) {
		final ImageFactory<UnsignedByteType> imageFactory =
			new ImageFactory<UnsignedByteType>(new UnsignedByteType(), cf);
		final int[] dim = {w, h};
		final Image<UnsignedByteType> img = imageFactory.createImage(dim);
		int index = 0;
		for (UnsignedByteType t : img) t.set(data[index++]);
		return img;
	}

	// -- Inversion methods --

	private void invertRaw(final byte[] data) {
		for (int i=0; i<data.length; i++) {
			final int value = data[i] & 0xff;
			final int result = 255 - value;
			data[i] = (byte) result;
		}
	}

	private void invertImageProcessor(final ImageProcessor ip) {
		for (int i=0; i<ip.getPixelCount(); i++) {
			final int value = ip.get(i);
			final int result = 255 - value;
			ip.set(i, result);
		}
	}

	/** Generic version. */
	@SuppressWarnings("unused")
	private void invertImage(final Image<UnsignedByteType> img) {
		final Cursor<UnsignedByteType> cursor = img.createCursor();
		for (final UnsignedByteType t : cursor) {
			final int value = t.get();
			final int result = 255 - value;
			t.set(result);
		}
		cursor.close();
	}

	/** Explicit array version. */
	private void invertArrayImage(final Image<UnsignedByteType> img) {
		final ArrayCursor<UnsignedByteType> cursor = (ArrayCursor<UnsignedByteType>) img.createCursor();
		for (final UnsignedByteType t : cursor) {
			final int value = t.get();
			final int result = 255 - value;
			t.set(result);
		}
		cursor.close();
	}

	/** Explicit cell version. */
	private void invertCellImage(final Image<UnsignedByteType> img) {
		final CellCursor<UnsignedByteType> cursor = (CellCursor<UnsignedByteType>) img.createCursor();
		for (final UnsignedByteType t : cursor) {
			final int value = t.get();
			final int result = 255 - value;
			t.set(result);
		}
		cursor.close();
	}

	/** Explicit planar version. */
	private void invertPlanarImage(final Image<UnsignedByteType> img) {
		final PlanarCursor<UnsignedByteType> cursor = (PlanarCursor<UnsignedByteType>) img.createCursor();
		for (final UnsignedByteType t : cursor) {
			final int value = t.get();
			final int result = 255 - value;
			t.set(result);
		}
		cursor.close();
	}

	/** Explicit ImagePlus version. */
	private void invertImagePlusImage(final Image<UnsignedByteType> img) {
		final ImagePlusCursor<UnsignedByteType> cursor = (ImagePlusCursor<UnsignedByteType>) img.createCursor();
		for (final UnsignedByteType t : cursor) {
			final int value = t.get();
			final int result = 255 - value;
			t.set(result);
		}
		cursor.close();
	}

	private void invertPixelCubeReflection(final PixelCube<Byte, BaseIndex> pc) {
		pc.setIterationPattern(Constants.IP_FWD + Constants.IP_SINGLE);
		RasterForwardIterator<Byte> iterIn = (RasterForwardIterator<Byte>) pc.iterator();
		while (iterIn.hasNext()) {
			final int value = iterIn.next() & 0xff;
			final int result = 255 - value;
			iterIn.dec();
			iterIn.put((byte) result);
		}
	}

	private void invertPixelCubeGenArray(final PixelCube<Byte, BaseIndex> pc) {
		pc.setIterationPattern(Constants.IP_PRIM +Constants.IP_FWD + Constants.IP_SINGLE);
		ByteForwardIterator iter = (ByteForwardIterator) pc.iterator();
		for (final byte b : pc) {
			final int value = b & 0xff;
			final int result = 255 - value;
			iter.put((byte) result);
		}
	}

	private void invertPixelCubeByteArray(final PixelCube<Byte, BaseIndex> pc) {
		pc.setIterationPattern(Constants.IP_PRIM +Constants.IP_FWD + Constants.IP_SINGLE);
		ByteForwardIterator iter = (ByteForwardIterator) pc.iterator();
		while (iter.hasNext() ) {
			final int value = iter.nextByte() & 0xff;
			final int result = 255 - value;
			iter.dec();
			iter.put((byte) result);
		}
	}

	// -- Randomization methods --

	private void randomizeRaw(final byte[] data) {
		for (int i=0; i<data.length; i++) {
			final int value = data[i] & 0xff;
			final double result = expensiveOperation(value);
			data[i] = (byte) result;
		}
	}

	private void randomizeImageProcessor(final ImageProcessor ip) {
		for (int i=0; i<ip.getPixelCount(); i++) {
			final int value = ip.get(i);
			final double result = expensiveOperation(value);
			ip.set(i, (int) result);
		}
	}

	/** Generic version. */
	@SuppressWarnings("unused")
	private void randomizeImage(final Image<UnsignedByteType> img) {
		final Cursor<UnsignedByteType> cursor = img.createCursor();
		for (final UnsignedByteType t : cursor) {
			final int value = t.get();
			final double result = expensiveOperation(value);
			t.set((int) result);
		}
		cursor.close();
	}

	/** Explicit array version. */
	private void randomizeArrayImage(final Image<UnsignedByteType> img) {
		final ArrayCursor<UnsignedByteType> cursor = (ArrayCursor<UnsignedByteType>) img.createCursor();
		for (final UnsignedByteType t : cursor) {
			final int value = t.get();
			final double result = expensiveOperation(value);
			t.set((int) result);
		}
		cursor.close();
	}

	/** Explicit cell version. */
	private void randomizeCellImage(final Image<UnsignedByteType> img) {
		final CellCursor<UnsignedByteType> cursor = (CellCursor<UnsignedByteType>) img.createCursor();
		for (final UnsignedByteType t : cursor) {
			final int value = t.get();
			final double result = expensiveOperation(value);
			t.set((int) result);
		}
		cursor.close();
	}

	/** Explicit planar version. */
	private void randomizePlanarImage(final Image<UnsignedByteType> img) {
		final PlanarCursor<UnsignedByteType> cursor = (PlanarCursor<UnsignedByteType>) img.createCursor();
		for (final UnsignedByteType t : cursor) {
			final int value = t.get();
			final double result = expensiveOperation(value);
			t.set((int) result);
		}
		cursor.close();
	}

	/** Explicit ImagePlus version. */
	private void randomizeImagePlusImage(final Image<UnsignedByteType> img) {
		final ImagePlusCursor<UnsignedByteType> cursor = (ImagePlusCursor<UnsignedByteType>) img.createCursor();
		for (final UnsignedByteType t : cursor) {
			final int value = t.get();
			final double result = expensiveOperation(value);
			t.set((int) result);
		}
		cursor.close();
	}

	private void randomizePixelCubeReflection(final PixelCube<Byte, BaseIndex> pc) {
		pc.setIterationPattern(Constants.IP_FWD + Constants.IP_SINGLE);
		RasterForwardIterator<Byte> iterIn = (RasterForwardIterator<Byte>) pc.iterator();
		while (iterIn.hasNext()) {
			final int value = iterIn.next() & 0xff;
			final double result = expensiveOperation(value);
			iterIn.dec();
			iterIn.put((byte) result);
		}
	}

	private void randomizePixelCubeGenArray(final PixelCube<Byte, BaseIndex> pc) {
		pc.setIterationPattern(Constants.IP_PRIM +Constants.IP_FWD + Constants.IP_SINGLE);
		ByteForwardIterator iter = (ByteForwardIterator) pc.iterator();
		for (final byte b : pc) {
			final int value = b & 0xff;
			final double result = expensiveOperation(value);
			iter.put((byte) result);
		}
	}

	private void randomizePixelCubeByteArray(final PixelCube<Byte, BaseIndex> pc) {
		pc.setIterationPattern(Constants.IP_PRIM +Constants.IP_FWD + Constants.IP_SINGLE);
		ByteForwardIterator iter = (ByteForwardIterator) pc.iterator();
		while (iter.hasNext() ) {
			final int value = iter.nextByte() & 0xff;
			final double result = expensiveOperation(value);
			iter.dec();
			iter.put((int) result);
		}
	}

	private double expensiveOperation(final int value) {
		return 255 * Math.random() * Math.sin(value / 255.0);
	}

}

