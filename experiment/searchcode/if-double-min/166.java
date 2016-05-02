<<<<<<< HEAD
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

=======
// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit;

/**
 *
 * @author sk89q
 */
public class Vector {
    protected final double x, y, z;

    /**
     * Construct the Vector object.
     *
     * @param x
     * @param y
     * @param z
     */
    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Construct the Vector object.
     *
     * @param x
     * @param y
     * @param z
     */
    public Vector(int x, int y, int z) {
        this.x = (double) x;
        this.y = (double) y;
        this.z = (double) z;
    }

    /**
     * Construct the Vector object.
     *
     * @param x
     * @param y
     * @param z
     */
    public Vector(float x, float y, float z) {
        this.x = (double) x;
        this.y = (double) y;
        this.z = (double) z;
    }

    /**
     * Construct the Vector object.
     *
     * @param pt
     */
    public Vector(Vector pt) {
        this.x = pt.x;
        this.y = pt.y;
        this.z = pt.z;
    }

    /**
     * Construct the Vector object.
     */
    public Vector() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @return the x
     */
    public int getBlockX() {
        return (int) Math.round(x);
    }

    /**
     * Set X.
     *
     * @param x
     * @return new vector
     */
    public Vector setX(double x) {
        return new Vector(x, y, z);
    }

    /**
     * Set X.
     *
     * @param x
     * @return new vector
     */
    public Vector setX(int x) {
        return new Vector(x, y, z);
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }

    /**
     * @return the y
     */
    public int getBlockY() {
        return (int) Math.round(y);
    }

    /**
     * Set Y.
     *
     * @param y
     * @return new vector
     */
    public Vector setY(double y) {
        return new Vector(x, y, z);
    }

    /**
     * Set Y.
     *
     * @param y
     * @return new vector
     */
    public Vector setY(int y) {
        return new Vector(x, y, z);
    }

    /**
     * @return the z
     */
    public double getZ() {
        return z;
    }

    /**
     * @return the z
     */
    public int getBlockZ() {
        return (int) Math.round(z);
    }

    /**
     * Set Z.
     *
     * @param z
     * @return new vector
     */
    public Vector setZ(double z) {
        return new Vector(x, y, z);
    }

    /**
     * Set Z.
     *
     * @param z
     * @return new vector
     */
    public Vector setZ(int z) {
        return new Vector(x, y, z);
    }

    /**
     * Adds two points.
     *
     * @param other
     * @return New point
     */
    public Vector add(Vector other) {
        return new Vector(x + other.x, y + other.y, z + other.z);
    }

    /**
     * Adds two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector add(double x, double y, double z) {
        return new Vector(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Adds two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector add(int x, int y, int z) {
        return new Vector(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Adds points.
     *
     * @param others
     * @return New point
     */
    public Vector add(Vector... others) {
        double newX = x, newY = y, newZ = z;

        for (int i = 0; i < others.length; ++i) {
            newX += others[i].x;
            newY += others[i].y;
            newZ += others[i].z;
        }
        return new Vector(newX, newY, newZ);
    }

    /**
     * Subtracts two points.
     *
     * @param other
     * @return New point
     */
    public Vector subtract(Vector other) {
        return new Vector(x - other.x, y - other.y, z - other.z);
    }

    /**
     * Subtract two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector subtract(double x, double y, double z) {
        return new Vector(this.x - x, this.y - y, this.z - z);
    }

    /**
     * Subtract two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector subtract(int x, int y, int z) {
        return new Vector(this.x - x, this.y - y, this.z - z);
    }

    /**
     * Subtract points.
     *
     * @param others
     * @return New point
     */
    public Vector subtract(Vector... others) {
        double newX = x, newY = y, newZ = z;

        for (int i = 0; i < others.length; ++i) {
            newX -= others[i].x;
            newY -= others[i].y;
            newZ -= others[i].z;
        }
        return new Vector(newX, newY, newZ);
    }

    /**
     * Component-wise multiplication
     *
     * @param other
     * @return New point
     */
    public Vector multiply(Vector other) {
        return new Vector(x * other.x, y * other.y, z * other.z);
    }

    /**
     * Component-wise multiplication
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector multiply(double x, double y, double z) {
        return new Vector(this.x * x, this.y * y, this.z * z);
    }

    /**
     * Component-wise multiplication
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector multiply(int x, int y, int z) {
        return new Vector(this.x * x, this.y * y, this.z * z);
    }

    /**
     * Component-wise multiplication
     *
     * @param others
     * @return New point
     */
    public Vector multiply(Vector... others) {
        double newX = x, newY = y, newZ = z;

        for (int i = 0; i < others.length; ++i) {
            newX *= others[i].x;
            newY *= others[i].y;
            newZ *= others[i].z;
        }
        return new Vector(newX, newY, newZ);
    }

    /**
     * Scalar multiplication.
     *
     * @param n
     * @return New point
     */
    public Vector multiply(double n) {
        return new Vector(this.x * n, this.y * n, this.z * n);
    }

    /**
     * Scalar multiplication.
     *
     * @param n
     * @return New point
     */
    public Vector multiply(float n) {
        return new Vector(this.x * n, this.y * n, this.z * n);
    }

    /**
     * Scalar multiplication.
     *
     * @param n
     * @return New point
     */
    public Vector multiply(int n) {
        return new Vector(this.x * n, this.y * n, this.z * n);
    }

    /**
     * Component-wise division
     *
     * @param other
     * @return New point
     */
    public Vector divide(Vector other) {
        return new Vector(x / other.x, y / other.y, z / other.z);
    }

    /**
     * Component-wise division
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector divide(double x, double y, double z) {
        return new Vector(this.x / x, this.y / y, this.z / z);
    }

    /**
     * Component-wise division
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector divide(int x, int y, int z) {
        return new Vector(this.x / x, this.y / y, this.z / z);
    }

    /**
     * Scalar division.
     *
     * @param n
     * @return new point
     */
    public Vector divide(int n) {
        return new Vector(x / n, y / n, z / n);
    }

    /**
     * Scalar division.
     *
     * @param n
     * @return new point
     */
    public Vector divide(double n) {
        return new Vector(x / n, y / n, z / n);
    }

    /**
     * Scalar division.
     *
     * @param n
     * @return new point
     */
    public Vector divide(float n) {
        return new Vector(x / n, y / n, z / n);
    }

    /**
     * Get the length of the vector.
     *
     * @return length
     */
    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Get the length^2 of the vector.
     *
     * @return length^2
     */
    public double lengthSq() {
        return x * x + y * y + z * z;
    }

    /**
     * Get the distance away from a point.
     *
     * @param pt
     * @return distance
     */
    public double distance(Vector pt) {
        return Math.sqrt(Math.pow(pt.x - x, 2) +
                Math.pow(pt.y - y, 2) +
                Math.pow(pt.z - z, 2));
    }

    /**
     * Get the distance away from a point, squared.
     *
     * @param pt
     * @return distance
     */
    public double distanceSq(Vector pt) {
        return Math.pow(pt.x - x, 2) +
                Math.pow(pt.y - y, 2) +
                Math.pow(pt.z - z, 2);
    }

    /**
     * Get the normalized vector.
     *
     * @return vector
     */
    public Vector normalize() {
        return divide(length());
    }

    /**
     * Gets the dot product of this and another vector.
     *
     * @param other
     * @return the dot product of this and the other vector
     */
    public double dot(Vector other) {
        return x * other.x + y * other.y + z * other.z;
    }

    /**
     * Gets the cross product of this and another vector.
     *
     * @param other
     * @return the cross product of this and the other vector
     */
    public Vector cross(Vector other) {
        return new Vector(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        );
    }

    /**
     * Checks to see if a vector is contained with another.
     *
     * @param min
     * @param max
     * @return
     */
    public boolean containedWithin(Vector min, Vector max) {
        return x >= min.x && x <= max.x
                && y >= min.y && y <= max.y
                && z >= min.z && z <= max.z;
    }

    /**
     * Checks to see if a vector is contained with another.
     *
     * @param min
     * @param max
     * @return
     */
    public boolean containedWithinBlock(Vector min, Vector max) {
        return getBlockX() >= min.getBlockX() && getBlockX() <= max.getBlockX()
                && getBlockY() >= min.getBlockY() && getBlockY() <= max.getBlockY()
                && getBlockZ() >= min.getBlockZ() && getBlockZ() <= max.getBlockZ();
    }

    /**
     * Clamp the Y component.
     *
     * @param min
     * @param max
     * @return
     */
    public Vector clampY(int min, int max) {
        return new Vector(x, Math.max(min, Math.min(max, y)), z);
    }

    /**
     * Rounds all components down.
     *
     * @return
     */
    public Vector floor() {
        return new Vector(Math.floor(x), Math.floor(y), Math.floor(z));
    }

    /**
     * Rounds all components up.
     *
     * @return
     */
    public Vector ceil() {
        return new Vector(Math.ceil(x), Math.ceil(y), Math.ceil(z));
    }

    /**
     * Rounds all components to the closest integer.<br>
     *<br>
     * Components < 0.5 are rounded down, otherwise up
     *
     * @return
     */
    public Vector round() {
        return new Vector(Math.floor(x + 0.5), Math.floor(y + 0.5), Math.floor(z + 0.5));
    }

    /**
     * 2D transformation.
     *
     * @param angle in degrees
     * @param aboutX about which x coordinate to rotate
     * @param aboutZ about which z coordinate to rotate
     * @param translateX what to add after rotation
     * @param translateZ what to add after rotation
     * @return
     */
    public Vector transform2D(double angle,
            double aboutX, double aboutZ, double translateX, double translateZ) {
        angle = Math.toRadians(angle);
        double x = this.x - aboutX;
        double z = this.z - aboutZ;
        double x2 = x * Math.cos(angle) - z * Math.sin(angle);
        double z2 = x * Math.sin(angle) + z * Math.cos(angle);

        return new Vector(
            x2 + aboutX + translateX,
            y,
            z2 + aboutZ + translateZ
        );
    }

    public boolean isCollinearWith(Vector other) {
        if (x == 0 && y == 0 && z == 0) {
            // this is a zero vector
            return true;
        }

        final double otherX = other.x;
        final double otherY = other.y;
        final double otherZ = other.z;

        if (otherX == 0 && otherY == 0 && otherZ == 0) {
            // other is a zero vector
            return true;
        }

        if ((x == 0) != (otherX == 0)) return false;
        if ((y == 0) != (otherY == 0)) return false;
        if ((z == 0) != (otherZ == 0)) return false;

        final double quotientX = otherX / x;
        if (!Double.isNaN(quotientX)) {
            return other.equals(multiply(quotientX));
        }

        final double quotientY = otherY / y;
        if (!Double.isNaN(quotientY)) {
            return other.equals(multiply(quotientY));
        }

        final double quotientZ = otherZ / z;
        if (!Double.isNaN(quotientZ)) {
            return other.equals(multiply(quotientZ));
        }

        throw new RuntimeException("This should not happen");
    }

    /**
     * Get a block point from a point.
     *
     * @param x
     * @param y
     * @param z
     * @return point
     */
    public static BlockVector toBlockPoint(double x, double y, double z) {
        return new BlockVector(
            Math.floor(x),
            Math.floor(y),
            Math.floor(z)
        );
    }

    /**
     * Get a block point from a point.
     *
     * @return point
     */
    public BlockVector toBlockPoint() {
        return new BlockVector(
            Math.floor(x),
            Math.floor(y),
            Math.floor(z)
        );
    }

    /**
     * Checks if another object is equivalent.
     *
     * @param obj
     * @return whether the other object is equivalent
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector)) {
            return false;
        }

        Vector other = (Vector) obj;
        return other.x == this.x && other.y == this.y && other.z == this.z;
    }

    /**
     * Gets the hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        int hash = 7;

        hash = 79 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        return hash;
    }

    /**
     * Returns string representation "(x, y, z)".
     *
     * @return string
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    /**
     * Gets a BlockVector version.
     *
     * @return BlockVector
     */
    public BlockVector toBlockVector() {
        return new BlockVector(this);
    }

    /**
     * Creates a 2D vector by dropping the Y component from this vector.
     *
     * @return Vector2D
     */
    public Vector2D toVector2D() {
        return new Vector2D(x, z);
    }

    /**
     * Gets the minimum components of two vectors.
     *
     * @param v1
     * @param v2
     * @return minimum
     */
    public static Vector getMinimum(Vector v1, Vector v2) {
        return new Vector(
            Math.min(v1.x, v2.x),
            Math.min(v1.y, v2.y),
            Math.min(v1.z, v2.z)
        );
    }

    /**
     * Gets the maximum components of two vectors.
     *
     * @param v1
     * @param v2
     * @return maximum
     */
    public static Vector getMaximum(Vector v1, Vector v2) {
        return new Vector(
            Math.max(v1.x, v2.x),
            Math.max(v1.y, v2.y),
            Math.max(v1.z, v2.z)
        );
    }

    /**
     * Gets the midpoint of two vectors.
     *
     * @param v1
     * @param v2
     * @return maximum
     */
    public static Vector getMidpoint(Vector v1, Vector v2) {
        return new Vector(
            (v1.x + v2.x) / 2,
            (v1.y + v2.y) / 2,
            (v1.z + v2.z) / 2
        );
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

