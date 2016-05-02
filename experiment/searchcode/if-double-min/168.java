<<<<<<< HEAD
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.shape;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.impl.Range;

import static com.spatial4j.core.shape.SpatialRelation.CONTAINS;
import static com.spatial4j.core.shape.SpatialRelation.WITHIN;

/**
 * A base test class with utility methods to help test shapes.
 * Extends from RandomizedTest.
 */
public abstract class RandomizedShapeTest extends RandomizedTest {

  protected static final double EPS = 10e-9;

  protected SpatialContext ctx;//needs to be set ASAP

  /** Used to reduce the space of numbers to increase the likelihood that
   * random numbers become equivalent, and thus trigger different code paths.
   * Also makes some random shapes easier to manually examine.
   */
  protected final double DIVISIBLE = 2;// even coordinates; (not always used)

  protected RandomizedShapeTest() {
  }

  public RandomizedShapeTest(SpatialContext ctx) {
    this.ctx = ctx;
  }

  public static void checkShapesImplementEquals( Class[] classes ) {
    for( Class clazz : classes ) {
      try {
        clazz.getDeclaredMethod( "equals", Object.class );
      } catch (Exception e) {
        fail("Shape needs to define 'equals' : " + clazz.getName());
      }
      try {
        clazz.getDeclaredMethod( "hashCode" );
      } catch (Exception e) {
        fail("Shape needs to define 'hashCode' : " + clazz.getName());
      }
    }
  }

  /**
   * BUG FIX: https://github.com/carrotsearch/randomizedtesting/issues/131
   *
   * Returns a random value greater or equal to <code>min</code>. The value
   * picked is affected by {@link #isNightly()} and {@link #multiplier()}.
   *
   * @see #scaledRandomIntBetween(int, int)
   */
  public static int atLeast(int min) {
    if (min < 0) throw new IllegalArgumentException("atLeast requires non-negative argument: " + min);

    min = (int) Math.min(min, (isNightly() ? 3 * min : min) * multiplier());
    int max = (int) Math.min(Integer.MAX_VALUE, (long) min + (min / 2));
    return randomIntBetween(min, max);
  }

  //These few norm methods normalize the arguments for creating a shape to
  // account for the dateline. Some tests loop past the dateline or have offsets
  // that go past it and it's easier to have them coded that way and correct for
  // it here.  These norm methods should be used when needed, not frivolously.

  protected double normX(double x) {
    return ctx.isGeo() ? DistanceUtils.normLonDEG(x) : x;
  }

  protected double normY(double y) {
    return ctx.isGeo() ? DistanceUtils.normLatDEG(y) : y;
  }

  protected Rectangle makeNormRect(double minX, double maxX, double minY, double maxY) {
    if (ctx.isGeo()) {
      if (Math.abs(maxX - minX) >= 360) {
        minX = -180;
        maxX = 180;
      } else {
        minX = DistanceUtils.normLonDEG(minX);
        maxX = DistanceUtils.normLonDEG(maxX);
      }

    } else {
      if (maxX < minX) {
        double t = minX;
        minX = maxX;
        maxX = t;
      }
      minX = boundX(minX, ctx.getWorldBounds());
      maxX = boundX(maxX, ctx.getWorldBounds());
    }
    if (maxY < minY) {
      double t = minY;
      minY = maxY;
      maxY = t;
    }
    minY = boundY(minY, ctx.getWorldBounds());
    maxY = boundY(maxY, ctx.getWorldBounds());
    return ctx.makeRectangle(minX, maxX, minY, maxY);
  }

  public static double divisible(double v, double divisible) {
    return (int) (Math.round(v / divisible) * divisible);
  }

  protected double divisible(double v) {
    return divisible(v, DIVISIBLE);
  }

  /** reset()'s p, and confines to world bounds. Might not be divisible if
   * the world bound isn't divisible too.
   */
  protected Point divisible(Point p) {
    Rectangle bounds = ctx.getWorldBounds();
    double newX = boundX( divisible(p.getX()), bounds );
    double newY = boundY( divisible(p.getY()), bounds );
    p.reset(newX, newY);
    return p;
  }

  static double boundX(double i, Rectangle bounds) {
    return bound(i, bounds.getMinX(), bounds.getMaxX());
  }

  static double boundY(double i, Rectangle bounds) {
    return bound(i, bounds.getMinY(), bounds.getMaxY());
  }

  static double bound(double i, double min, double max) {
    if (i < min) return min;
    if (i > max) return max;
    return i;
  }

  protected void assertRelation(String msg, SpatialRelation expected, Shape a, Shape b) {
    _assertIntersect(msg, expected, a, b);
    //check flipped a & b w/ transpose(), while we're at it
    _assertIntersect(msg, expected.transpose(), b, a);
  }

  private void _assertIntersect(String msg, SpatialRelation expected, Shape a, Shape b) {
    SpatialRelation sect = a.relate(b);
    if (sect == expected)
      return;
    msg = ((msg == null) ? "" : msg+"\r") + a +" intersect "+b;
    if (expected == WITHIN || expected == CONTAINS) {
      if (a.getClass().equals(b.getClass())) // they are the same shape type
        assertEquals(msg,a,b);
      else {
        //they are effectively points or lines that are the same location
        assertTrue(msg,!a.hasArea());
        assertTrue(msg,!b.hasArea());

        Rectangle aBBox = a.getBoundingBox();
        Rectangle bBBox = b.getBoundingBox();
        if (aBBox.getHeight() == 0 && bBBox.getHeight() == 0
            && (aBBox.getMaxY() == 90 && bBBox.getMaxY() == 90
          || aBBox.getMinY() == -90 && bBBox.getMinY() == -90))
          ;//== a point at the pole
        else
          assertEquals(msg, aBBox, bBBox);
      }
    } else {
      assertEquals(msg,expected,sect);//always fails
    }
  }

  protected void assertEqualsRatio(String msg, double expected, double actual) {
    double delta = Math.abs(actual - expected);
    double base = Math.min(actual, expected);
    double deltaRatio = base==0 ? delta : Math.min(delta,delta / base);
    assertEquals(msg,0,deltaRatio, EPS);
  }

  protected int randomIntBetweenDivisible(int start, int end) {
    return randomIntBetweenDivisible(start, end, (int)DIVISIBLE);
  }
    /** Returns a random integer between [start, end]. Integers between must be divisible by the 3rd argument. */
  protected int randomIntBetweenDivisible(int start, int end, int divisible) {
    // DWS: I tested this
    int divisStart = (int) Math.ceil( (start+1) / (double)divisible );
    int divisEnd = (int) Math.floor( (end-1) / (double)divisible );
    int divisRange = Math.max(0,divisEnd - divisStart + 1);
    int r = randomInt(1 + divisRange);//remember that '0' is counted
    if (r == 0)
      return start;
    if (r == 1)
      return end;
    return (r-2 + divisStart)*divisible;
  }

  protected Rectangle randomRectangle(Point nearP) {
    Rectangle bounds = ctx.getWorldBounds();
    if (nearP == null)
      nearP = randomPointIn(bounds);

    Range xRange = randomRange(rarely() ? 0 : nearP.getX(), Range.xRange(bounds, ctx));
    Range yRange = randomRange(rarely() ? 0 : nearP.getY(), Range.yRange(bounds, ctx));

    return makeNormRect(
        divisible(xRange.getMin()),
        divisible(xRange.getMax()),
        divisible(yRange.getMin()),
        divisible(yRange.getMax()) );
  }

  private Range randomRange(double near, Range bounds) {
    double mid = near + randomGaussian() * bounds.getWidth() / 6;
    double width = Math.abs(randomGaussian()) * bounds.getWidth() / 6;//1/3rd
    return new Range(mid - width / 2, mid + width / 2);
  }

  private double randomGaussianZeroTo(double max) {
    if (max == 0)
      return max;
    assert max > 0;
    double r;
    do {
      r = Math.abs(randomGaussian()) * (max * 0.50);
    } while (r > max);
    return r;
  }

  protected Rectangle randomRectangle(int divisible) {
    double rX = randomIntBetweenDivisible(-180, 180, divisible);
    double rW = randomIntBetweenDivisible(0, 360, divisible);
    double rY1 = randomIntBetweenDivisible(-90, 90, divisible);
    double rY2 = randomIntBetweenDivisible(-90, 90, divisible);
    double rYmin = Math.min(rY1,rY2);
    double rYmax = Math.max(rY1,rY2);
    if (rW > 0 && rX == 180)
      rX = -180;
    return makeNormRect(rX, rX + rW, rYmin, rYmax);
  }

  protected Point randomPoint() {
    return randomPointIn(ctx.getWorldBounds());
  }

  protected Point randomPointIn(Circle c) {
    double d = c.getRadius() * randomDouble();
    double angleDEG = 360 * randomDouble();
    Point p = ctx.getDistCalc().pointOnBearing(c.getCenter(), d, angleDEG, ctx, null);
    assertEquals(CONTAINS,c.relate(p));
    return p;
  }

  protected Point randomPointIn(Rectangle r) {
    double x = r.getMinX() + randomDouble()*r.getWidth();
    double y = r.getMinY() + randomDouble()*r.getHeight();
    x = normX(x);
    y = normY(y);
    Point p = ctx.makePoint(x,y);
    assertEquals(CONTAINS,r.relate(p));
    return p;
  }

  protected Point randomPointIn(Shape shape) {
    if (!shape.hasArea())// or try the center?
      throw new UnsupportedOperationException("Need area to define shape!");
    Rectangle bbox = shape.getBoundingBox();
    Point p;
    do {
      p = randomPointIn(bbox);
    } while (!bbox.relate(p).intersects());
    return p;
  }
=======
package org.occ.matsu;

import org.occ.matsu.ByteOrder;
import org.occ.matsu.ZeroSuppressed;
import org.occ.matsu.GeoPictureWithMetadata;
import org.occ.matsu.InvalidGeoPictureException;

import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.ValidatingDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;

import it.sauronsoftware.base64.Base64InputStream;
import it.sauronsoftware.base64.Base64;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.lang.Double;
import java.util.Map;
import java.lang.CharSequence;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.Math;
import java.lang.StringBuilder;

import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import javax.imageio.ImageIO;

class GeoPictureSerializer extends Object {

    public static final Schema schema = new Schema.Parser().parse(
        "{\"type\": \"record\", \"name\": \"GeoPictureWithMetadata\", \"fields\":\n" +
	"    [{\"name\": \"metadata\", \"type\": {\"type\": \"map\", \"values\": \"string\"}},\n" +
	"     {\"name\": \"bands\", \"type\": {\"type\": \"array\", \"items\": \"string\"}},\n" +
	"     {\"name\": \"height\", \"type\": \"int\"},\n" +
	"     {\"name\": \"width\", \"type\": \"int\"},\n" +
	"     {\"name\": \"depth\", \"type\": \"int\"},\n" +
	"     {\"name\": \"dtype\", \"type\": \"int\"},\n" +
	"     {\"name\": \"itemsize\", \"type\": \"int\"},\n" +
	"     {\"name\": \"nbytes\", \"type\": \"long\"},\n" +
	"     {\"name\": \"fortran\", \"type\": \"boolean\"},\n" +
	"     {\"name\": \"byteorder\", \"type\": {\"type\": \"enum\", \"name\": \"ByteOrder\", \"symbols\": [\"LittleEndian\", \"BigEndian\", \"NativeEndian\", \"IgnoreEndian\"]}},\n" +
	"     {\"name\": \"data\", \"type\":\n" +
	"        {\"type\": \"array\", \"items\":\n" +
	"            {\"type\": \"record\", \"name\": \"ZeroSuppressed\", \"fields\":\n" +
	"                [{\"name\": \"index\", \"type\": \"long\"}, {\"name\": \"strip\", \"type\": \"bytes\"}]}}}\n" +
	"     ]}");

    Map<CharSequence,CharSequence> metadata;
    String[] bands;
    int height;
    int width;
    int depth;
    double[][][] data;
    boolean valid = false;

    public GeoPictureSerializer() {}

    public String bandNames() throws InvalidGeoPictureException {
    	if (!valid) { throw new InvalidGeoPictureException(); }

	StringBuilder stringBuilder = new StringBuilder();
	stringBuilder.append("[");

	for (int k = 0;  k < depth;  k++) {
	    if (k != 0) { stringBuilder.append(","); }
	    stringBuilder.append(String.format("\"%s\"", bands[k]));
	}
	stringBuilder.append("]");

	return stringBuilder.toString();
    }

    public String dimensions() throws InvalidGeoPictureException {
    	if (!valid) { throw new InvalidGeoPictureException(); }
	return String.format("[%d,%d,%d]", width, height, depth);
    }

    public void loadSerialized(String serialized) throws IOException {
    	loadSerialized(new ByteArrayInputStream(serialized.getBytes()));
    }

    public void loadSerialized(InputStream serialized) throws IOException {
	InputStream inputStream = new Base64InputStream(serialized);

	DecoderFactory decoderFactory = new DecoderFactory();
	ValidatingDecoder d = decoderFactory.validatingDecoder(schema, decoderFactory.binaryDecoder(inputStream, null));

	DatumReader<GeoPictureWithMetadata> reader = new SpecificDatumReader<GeoPictureWithMetadata>(GeoPictureWithMetadata.class);
	GeoPictureWithMetadata p = reader.read(null, d);

	metadata = p.getMetadata();

	bands = new String[p.getBands().size()];
	int b = 0;
	for (CharSequence band : p.getBands()) {
	    bands[b] = band.toString();
	    b++;
	}

	height = p.getHeight();
	width = p.getWidth();
	depth = p.getDepth();

	data = new double[height][width][depth];
	for (int i = 0;  i < height;  i++) {
	    for (int j = 0;  j < width;  j++) {
		for (int k = 0;  k < depth;  k++) {
		    data[i][j][k] = 0.;
		}
	    }
	}

	for (ZeroSuppressed zs : p.getData()) {
	    long index = zs.getIndex();

	    ByteBuffer strip = zs.getStrip();
	    if (! p.getByteorder().equals(ByteOrder.BigEndian)) {
		strip.order(java.nio.ByteOrder.LITTLE_ENDIAN);
	    }

	    while (strip.hasRemaining()) {
		int i, j, k;
		if (p.getFortran()) {
		    i = (int)((index / height / width) % depth);
		    j = (int)((index / height) % width);
		    k = (int)(index % height);
		}
		else {
		    i = (int)((index / depth / width) % height);
		    j = (int)((index / depth) % width);
		    k = (int)(index % depth);
		}
		index++;

		data[i][j][k] = strip.getDouble();
	    }
	}

	valid = true;
    }

    public void toJSON(PrintWriter printWriter, int x1, int y1, int x2, int y2) throws InvalidGeoPictureException {
	if (!valid) { throw new InvalidGeoPictureException(); }

	if (x1 > x2) {
	    int tmp = x1;
	    x1 = x2;
	    x2 = tmp;
	}
	if (y1 > y2) {
	    int tmp = y1;
	    y1 = y2;
	    y2 = tmp;
	}

	printWriter.print("{\"bands\": ");
	printWriter.print(this.bandNames());

	printWriter.print(",\n\"metadata\": {");
	boolean comma = false;
	for (CharSequence key : this.metadata.keySet()) {
	    if (comma) {
		printWriter.print(", ");
	    } else {
		comma = true;
	    }

	    String keyString = key.toString().replace("\\", "\\\"").replace("\"", "\\\"");
	    String valueString = this.metadata.get(key).toString().replace("\\", "\\\"").replace("\"", "\\\"");

	    printWriter.print(String.format("\"%s\": \"%s\"", keyString, valueString));
	}

	printWriter.print("},\n\"picture\": [");

	int actualWidth = 0;
	int actualHeight = 0;
	boolean icomma = false;
	for (int i = 0;  i < x2 - x1;  i++) {
	    if (i < width) {
		actualWidth++;

		if (icomma) {
		    printWriter.print(", [");
		} else {
		    printWriter.print("[");
		    icomma = true;
		}

		boolean jcomma = false;
		for (int j = 0;  j < y2 - y1;  j++) {
		    if (j < height) {
			if (actualWidth == 1) { actualHeight++; }

			if (jcomma) {
			    printWriter.print(", [");
			} else {
			    printWriter.print("[");
			    jcomma = true;
			}

			boolean kcomma = false;
			for (int k = 0;  k < bands.length;  k++) {
			    if (kcomma) {
				printWriter.print(",");
			    } else {
				kcomma = true;
			    }
			    
			    printWriter.print(data[j + y1][i + x1][k]);
			}

			printWriter.print("]");
		    }
		}

		printWriter.print("]\n");
	    }
	}

	printWriter.println(String.format("],\n\"shape\": [%d,%d,%d]}", actualWidth, actualHeight, depth));
    }

    public String spectrum(int x1, int y1, int x2, int y2, boolean log) throws IOException, InvalidGeoPictureException {
    	if (!valid) { throw new InvalidGeoPictureException(); }
	if (x1 < 0) { x1 = 0; }
	if (y1 < 0) { y1 = 0; }
	if (x2 >= width) { x2 = width - 1; }
	if (y2 >= height) { y2 = height - 1; }

    	double[] numer = new double[depth];
    	double[] denom = new double[depth];

	int numNonzero = 0;
    	for (int k = 0;  k < depth;  k++) {
    	    numer[k] = 0.;
    	    denom[k] = 0.;

    	    for (int i = 0;  i < x2 - x1;  i++) {
    		for (int j = 0;  j < y2 - y1;  j++) {
    		    double v = data[j + y1][i + x1][k];
		    if (v > 0.) {
			numer[k] += v;
			denom[k] += 1.;
		    }
		}
	    }

	    if (denom[k] > 0.) { numNonzero++; }
	}

	StringBuilder stringBuilder = new StringBuilder();
	stringBuilder.append("[");

	int i = 0;
	boolean first = true;
	for (int k = 0;  k < depth;  k++) {
	    if (denom[k] > 0.) {
		double output;

		if (log) {
		    output = Math.log(numer[k]) - Math.log(denom[k]);
		}
		else {
		    output = numer[k] / denom[k];
		}
		i++;

		if (!first) { stringBuilder.append(","); }
		first = false;

		stringBuilder.append(String.format("[\"%s\",%g]", bands[k], output));
	    }
	}
		
	stringBuilder.append("]");

	return stringBuilder.toString();
    }

    public String scatter(int x1, int y1, int x2, int y2, String horiz, String vert) throws InvalidGeoPictureException, ScriptException {
	if (!valid) { throw new InvalidGeoPictureException(); }
	if (x1 < 0) { x1 = 0; }
	if (y1 < 0) { y1 = 0; }
	if (x2 >= width) { x2 = width - 1; }
	if (y2 >= height) { y2 = height - 1; }

	int horizSimple = -1;
	int vertSimple = -1;
	for (int k = 0;  k < depth;  k++) {
	    if (bands[k].equals(horiz)) { horizSimple = k; }
	    if (bands[k].equals(vert)) { vertSimple = k; }
	}

	ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
	ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");

	boolean[] goodBands = new boolean[depth];
	for (int k = 0;  k < depth;  k++) {
	    goodBands[k] = false;
	    if (horiz.indexOf(bands[k]) != -1) { goodBands[k] = true; }
	    if (vert.indexOf(bands[k]) != -1) { goodBands[k] = true; }
	}

	double[][] horizs = new double[x2 - x1][y2 - y1];
	double[][] verts = new double[x2 - x1][y2 - y1];
	boolean[][] alphas = new boolean[x2 - x1][y2 - y1];

	for (int i = 0;  i < x2 - x1;  i++) {
	    for (int j = 0;  j < y2 - y1;  j++) {
		alphas[i][j] = true;

		if (horizSimple == -1  ||  vertSimple == -1) {
		    for (int k = 0;  k < depth;  k++) {
			if (goodBands[k]) {
			    scriptEngine.put(bands[k], data[j + y1][i + x1][k]);
			    if (data[j + y1][i + x1][k] == 0.) { alphas[i][j] = false; }
			}
		    }
		}

		if (horizSimple == -1) {
		    horizs[i][j] = (Double)scriptEngine.eval(horiz);
		}
		else {
		    horizs[i][j] = data[j + y1][i + x1][horizSimple];
		    if (horizs[i][j] == 0.) { alphas[i][j] = false; }
		}
		if (vertSimple == -1) {
		    verts[i][j] = (Double)scriptEngine.eval(vert);
		}
		else {
		    verts[i][j] = data[j + y1][i + x1][vertSimple];
		    if (verts[i][j] == 0.) { alphas[i][j] = false; }
		}

	    }
	}

	StringBuilder stringBuilder = new StringBuilder();
	stringBuilder.append("[");
	boolean first = true;
	for (int i = 0;  i < x2 - x1;  i++) {
	    for (int j = 0;  j < y2 - y1;  j++) {
		if (alphas[i][j]) {
		    if (!first) { stringBuilder.append(","); }
		    first = false;

		    stringBuilder.append(String.format("[%g,%g]", horizs[i][j], verts[i][j]));
		}
	    }
	}
	stringBuilder.append("]");

	return stringBuilder.toString();
    }

    public byte[] image(String red, String green, String blue, double min, double max, boolean base64) throws IOException, InvalidGeoPictureException, ScriptException {
	if (!valid) { throw new InvalidGeoPictureException(); }

	int redSimple = -1;
	int greenSimple = -1;
	int blueSimple = -1;
	for (int k = 0;  k < depth;  k++) {
	    if (bands[k].equals(red)) { redSimple = k; }
	    if (bands[k].equals(green)) { greenSimple = k; }
	    if (bands[k].equals(blue)) { blueSimple = k; }
	}

	if (redSimple == -1  ||  greenSimple == -1  ||  blueSimple == -1) {
	    throw new ScriptException("Javascript is only allowed for sub-images (it's slow!)");
	}

	return image(0, 0, width, height, red, green, blue, min, max, base64);
    }

    public byte[] image(int x1, int y1, int x2, int y2, String red, String green, String blue, double min, double max, boolean base64) throws IOException, InvalidGeoPictureException, ScriptException {
	if (!valid) { throw new InvalidGeoPictureException(); }
	if (x1 < 0) { x1 = 0; }
	if (y1 < 0) { y1 = 0; }
	if (x2 >= width) { x2 = width - 1; }
	if (y2 >= height) { y2 = height - 1; }

	int redSimple = -1;
	int greenSimple = -1;
	int blueSimple = -1;
	for (int k = 0;  k < depth;  k++) {
	    if (bands[k].equals(red)) { redSimple = k; }
	    if (bands[k].equals(green)) { greenSimple = k; }
	    if (bands[k].equals(blue)) { blueSimple = k; }
	}
	
	ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
	ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");

	boolean[] goodBands = new boolean[depth];
	for (int k = 0;  k < depth;  k++) {
	    goodBands[k] = false;
	    if (red.indexOf(bands[k]) != -1) { goodBands[k] = true; }
	    if (green.indexOf(bands[k]) != -1) { goodBands[k] = true; }
	    if (blue.indexOf(bands[k]) != -1) { goodBands[k] = true; }
	}

	double[][] reds = new double[x2 - x1][y2 - y1];
	double[][] greens = new double[x2 - x1][y2 - y1];
	double[][] blues = new double[x2 - x1][y2 - y1];
	boolean[][] alphas = new boolean[x2 - x1][y2 - y1];
	
	List<Double> redrad = new ArrayList<Double>();
	List<Double> greenrad = new ArrayList<Double>();
	List<Double> bluerad = new ArrayList<Double>();
	
	for (int i = 0;  i < x2 - x1;  i++) {
	    for (int j = 0;  j < y2 - y1;  j++) {
		alphas[i][j] = true;

		if (redSimple == -1  ||  greenSimple == -1  ||  blueSimple == -1) {
		    for (int k = 0;  k < depth;  k++) {
			if (goodBands[k]) {
			    scriptEngine.put(bands[k], data[j + y1][i + x1][k]);
			    if (data[j + y1][i + x1][k] == 0.) { alphas[i][j] = false; }
			}
		    }
		}

		if (redSimple == -1) {
		    reds[i][j] = (Double)scriptEngine.eval(red);
		}
		else {
		    reds[i][j] = data[j + y1][i + x1][redSimple];
		    if (reds[i][j] == 0.) { alphas[i][j] = false; }
		}

		if (greenSimple == -1) {
		    greens[i][j] = (Double)scriptEngine.eval(green);
		}
		else {
		    greens[i][j] = data[j + y1][i + x1][greenSimple];
		    if (greens[i][j] == 0.) { alphas[i][j] = false; }
		}

		if (blueSimple == -1) {
		    blues[i][j] = (Double)scriptEngine.eval(blue);
		}
		else {
		    blues[i][j] = data[j + y1][i + x1][blueSimple];
		    if (blues[i][j] == 0.) { alphas[i][j] = false; }
		}

		if (min == max  &&  alphas[i][j]) {
		    redrad.add(reds[i][j]);
		    greenrad.add(greens[i][j]);
		    bluerad.add(blues[i][j]);
		}
	    }
	}

	if (min == max) {
	    if (redrad.size() == 0) {
		throw new ScriptException("No non-empty pixels were found");
	    }

	    Collections.sort(redrad);
	    Collections.sort(greenrad);
	    Collections.sort(bluerad);
	
	    int redIndex5 = Math.max((int)Math.floor(redrad.size() * 0.05), 0);
	    int redIndex95 = Math.min((int)Math.ceil(redrad.size() * 0.95), redrad.size() - 1);

	    int greenIndex5 = Math.max((int)Math.floor(greenrad.size() * 0.05), 0);
	    int greenIndex95 = Math.min((int)Math.ceil(greenrad.size() * 0.95), greenrad.size() - 1);

	    int blueIndex5 = Math.max((int)Math.floor(bluerad.size() * 0.05), 0);
	    int blueIndex95 = Math.min((int)Math.ceil(bluerad.size() * 0.95), bluerad.size() - 1);

	    min = Math.min(redrad.get(redIndex5), Math.min(greenrad.get(greenIndex5), bluerad.get(blueIndex5)));
	    max = Math.max(redrad.get(redIndex95), Math.max(greenrad.get(greenIndex95), bluerad.get(blueIndex95)));
	}

	BufferedImage bufferedImage = new BufferedImage(x2 - x1, y2 - y1, BufferedImage.TYPE_4BYTE_ABGR);
	for (int i = 0;  i < x2 - x1;  i++) {
	    for (int j = 0;  j < y2 - y1;  j++) {
		int r = Math.min(Math.max((int)Math.floor((reds[i][j] - min) / (max - min) * 256), 0), 255);
		int g = Math.min(Math.max((int)Math.floor((greens[i][j] - min) / (max - min) * 256), 0), 255);
		int b = Math.min(Math.max((int)Math.floor((blues[i][j] - min) / (max - min) * 256), 0), 255);

		int abgr = new Color(r, g, b).getRGB();
		if (!alphas[i][j]) {
		    abgr &= 0x00ffffff;
		}
		bufferedImage.setRGB(i, j, abgr);
	    }
	}

	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);

	if (base64) {
	    return Base64.encode(byteArrayOutputStream.toByteArray());
	}
	else {
	    return byteArrayOutputStream.toByteArray();
	}
    }

>>>>>>> 76aa07461566a5976980e6696204781271955163
}

