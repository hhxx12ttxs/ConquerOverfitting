<<<<<<< HEAD
package fi.nls.oskari.printout.printing;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.COSStreamArray;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceN;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

/**
 * This class is a convenience for creating page content streams. You MUST call
 * close() when you are finished with this object.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class PDPageContentStream {
	/**
	 * Log instance.
	 */
	private static final Log LOG = LogFactory.getLog(PDPageContentStream.class);

	private PDPage page;
	private OutputStream output;
	private boolean inTextMode = false;
	private PDResources resources;

	private PDColorSpace currentStrokingColorSpace = new PDDeviceGray();
	private PDColorSpace currentNonStrokingColorSpace = new PDDeviceGray();

	// cached storage component for getting color values
	private float[] colorComponents = new float[4];

	private NumberFormat formatDecimal = NumberFormat
			.getNumberInstance(Locale.US);

	private static final String ISO8859 = "ISO-8859-1";

	private static byte[] getISOBytes(final String s) {
		try {
			return s.getBytes(ISO8859);
		} catch (final UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static final byte[] BEGIN_TEXT = getISOBytes("BT\n");
	private static final byte[] END_TEXT = getISOBytes("ET\n");
	private static final byte[] SET_FONT = getISOBytes("Tf\n");
	private static final byte[] MOVE_TEXT_POSITION = getISOBytes("Td\n");
	private static final byte[] SET_TEXT_MATRIX = getISOBytes("Tm\n");
	private static final byte[] SHOW_TEXT = getISOBytes("Tj\n");

	private static final byte[] SAVE_GRAPHICS_STATE = getISOBytes("q\n");
	private static final byte[] RESTORE_GRAPHICS_STATE = getISOBytes("Q\n");
	private static final byte[] CONCATENATE_MATRIX = getISOBytes("cm\n");
	private static final byte[] XOBJECT_DO = getISOBytes("Do\n");
	private static final byte[] RG_STROKING = getISOBytes("RG\n");
	private static final byte[] RG_NON_STROKING = getISOBytes("rg\n");
	private static final byte[] K_STROKING = getISOBytes("K\n");
	private static final byte[] K_NON_STROKING = getISOBytes("k\n");
	private static final byte[] G_STROKING = getISOBytes("G\n");
	private static final byte[] G_NON_STROKING = getISOBytes("g\n");
	private static final byte[] RECTANGLE = getISOBytes("re\n");
	private static final byte[] FILL_NON_ZERO = getISOBytes("f\n");
	private static final byte[] FILL_EVEN_ODD = getISOBytes("f*\n");
	private static final byte[] LINE_TO = getISOBytes("l\n");
	private static final byte[] MOVE_TO = getISOBytes("m\n");
	private static final byte[] CLOSE_STROKE = getISOBytes("s\n");
	private static final byte[] STROKE = getISOBytes("S\n");
	private static final byte[] LINE_WIDTH = getISOBytes("w\n");
	private static final byte[] LINE_JOIN_STYLE = getISOBytes("j\n");
	private static final byte[] LINE_CAP_STYLE = getISOBytes("J\n");
	private static final byte[] LINE_DASH_PATTERN = getISOBytes("d\n");
	private static final byte[] CLOSE_SUBPATH = getISOBytes("h\n");
	private static final byte[] CLIP_PATH_NON_ZERO = getISOBytes("W\n");
	private static final byte[] CLIP_PATH_EVEN_ODD = getISOBytes("W*\n");
	private static final byte[] NOP = getISOBytes("n\n");
	private static final byte[] BEZIER_312 = getISOBytes("c\n");
	private static final byte[] BEZIER_32 = getISOBytes("v\n");
	private static final byte[] BEZIER_313 = getISOBytes("y\n");

	private static final byte[] BMC = getISOBytes("BMC\n");
	private static final byte[] BDC = getISOBytes("BDC\n");
	private static final byte[] EMC = getISOBytes("EMC\n");

	private static final byte[] SET_STROKING_COLORSPACE = getISOBytes("CS\n");
	private static final byte[] SET_NON_STROKING_COLORSPACE = getISOBytes("cs\n");

	private static final byte[] SET_STROKING_COLOR_SIMPLE = getISOBytes("SC\n");
	private static final byte[] SET_STROKING_COLOR_COMPLEX = getISOBytes("SCN\n");
	private static final byte[] SET_NON_STROKING_COLOR_SIMPLE = getISOBytes("sc\n");
	private static final byte[] SET_NON_STROKING_COLOR_COMPLEX = getISOBytes("scn\n");

	private static final byte[] OPENING_BRACKET = getISOBytes("[");
	private static final byte[] CLOSING_BRACKET = getISOBytes("]");

	private static final int SPACE = 32;

	/**
	 * Create a new PDPage content stream.
	 * 
	 * @param document
	 *            The document the page is part of.
	 * @param sourcePage
	 *            The page to write the contents to.
	 * @throws IOException
	 *             If there is an error writing to the page contents.
	 */
	public PDPageContentStream(PDDocument document, PDPage sourcePage)
			throws IOException {
		this(document, sourcePage, false, true);
	}

	/**
	 * Create a new PDPage content stream.
	 * 
	 * @param document
	 *            The document the page is part of.
	 * @param sourcePage
	 *            The page to write the contents to.
	 * @param appendContent
	 *            Indicates whether content will be overwritten. If false all
	 *            previous content is deleted.
	 * @param compress
	 *            Tell if the content stream should compress the page contents.
	 * @throws IOException
	 *             If there is an error writing to the page contents.
	 */
	public PDPageContentStream(PDDocument document, PDPage sourcePage,
			boolean appendContent, boolean compress) throws IOException {
		this(document, sourcePage, appendContent, compress, false);
	}

	/**
	 * Create a new PDPage content stream.
	 * 
	 * @param document
	 *            The document the page is part of.
	 * @param sourcePage
	 *            The page to write the contents to.
	 * @param appendContent
	 *            Indicates whether content will be overwritten. If false all
	 *            previous content is deleted.
	 * @param compress
	 *            Tell if the content stream should compress the page contents.
	 * @param resetContext
	 *            Tell if the graphic context should be reseted.
	 * @throws IOException
	 *             If there is an error writing to the page contents.
	 */
	public PDPageContentStream(PDDocument document, PDPage sourcePage,
			boolean appendContent, boolean compress, boolean resetContext)
			throws IOException {

		page = sourcePage;
		resources = page.getResources();
		if (resources == null) {
			resources = new PDResources();
			page.setResources(resources);
		}

		// Get the pdstream from the source page instead of creating a new one
		PDStream contents = sourcePage.getContents();
		boolean hasContent = contents != null;

		// If request specifies the need to append to the document
		if (appendContent && hasContent) {

			// Create a pdstream to append new content
			PDStream contentsToAppend = new PDStream(document);

			// This will be the resulting COSStreamArray after existing and new
			// streams are merged
			COSStreamArray compoundStream = null;

			// If contents is already an array, a new stream is simply appended
			// to it
			if (contents.getStream() instanceof COSStreamArray) {
				compoundStream = (COSStreamArray) contents.getStream();
				compoundStream.appendStream(contentsToAppend.getStream());
			} else {
				// Creates the COSStreamArray and adds the current stream plus a
				// new one to it
				COSArray newArray = new COSArray();
				newArray.add(contents.getCOSObject());
				newArray.add(contentsToAppend.getCOSObject());
				compoundStream = new COSStreamArray(newArray);
			}

			if (compress) {
				List<COSName> filters = new ArrayList<COSName>();
				filters.add(COSName.FLATE_DECODE);
				contentsToAppend.setFilters(filters);
			}

			if (resetContext) {
				// create a new stream to encapsulate the existing stream
				PDStream saveGraphics = new PDStream(document);
				output = saveGraphics.createOutputStream();
				// save the initial/unmodified graphics context
				saveGraphicsState();
				close(); // ?
				if (compress) {
					List<COSName> filters = new ArrayList<COSName>();
					filters.add(COSName.FLATE_DECODE);
					saveGraphics.setFilters(filters);
				}
				// insert the new stream at the beginning
				compoundStream.insertCOSStream(saveGraphics);
			}

			// Sets the compoundStream as page contents
			sourcePage.setContents(new PDStream(compoundStream));
			output = contentsToAppend.createOutputStream();
			if (resetContext) {
				// restore the initial/unmodified graphics context
				restoreGraphicsState();
			}
		} else {
			if (hasContent) {
				LOG.warn("You are overwriting an existing content, you should use the append mode");
			}
			contents = new PDStream(document);
			if (compress) {
				List<COSName> filters = new ArrayList<COSName>();
				filters.add(COSName.FLATE_DECODE);
				contents.setFilters(filters);
			}
			sourcePage.setContents(contents);
			output = contents.createOutputStream();
		}
		formatDecimal.setMaximumFractionDigits(10);
		formatDecimal.setGroupingUsed(false);
	}

	/**
	 * Begin some text operations.
	 * 
	 * @throws IOException
	 *             If there is an error writing to the stream or if you attempt
	 *             to nest beginText calls.
	 */
	public void beginText() throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: Nested beginText() calls are not allowed.");
		}
		appendRawCommands(BEGIN_TEXT);
		inTextMode = true;
	}

	/**
	 * End some text operations.
	 * 
	 * @throws IOException
	 *             If there is an error writing to the stream or if you attempt
	 *             to nest endText calls.
	 */
	public void endText() throws IOException {
		if (!inTextMode) {
			throw new IOException(
					"Error: You must call beginText() before calling endText.");
		}
		appendRawCommands(END_TEXT);
		inTextMode = false;
	}

	/**
	 * Set the font to draw text with.
	 * 
	 * @param font
	 *            The font to use.
	 * @param fontSize
	 *            The font size to draw the text.
	 * @throws IOException
	 *             If there is an error writing the font information.
	 */
	public void setFont(PDFont font, float fontSize) throws IOException {
		String fontMapping = resources.addFont(font);
		appendRawCommands("/");
		appendRawCommands(fontMapping);
		appendRawCommands(SPACE);
		appendRawCommands(fontSize);
		appendRawCommands(SPACE);
		appendRawCommands(SET_FONT);
	}

	/**
	 * Draw an image at the x,y coordinates, with the default size of the image.
	 * 
	 * @param image
	 *            The image to draw.
	 * @param x
	 *            The x-coordinate to draw the image.
	 * @param y
	 *            The y-coordinate to draw the image.
	 * 
	 * @throws IOException
	 *             If there is an error writing to the stream.
	 */
	public void drawImage(PDXObjectImage image, float x, float y)
			throws IOException {
		drawXObject(image, x, y, image.getWidth(), image.getHeight());
	}

	/**
	 * Draw an xobject(form or image) at the x,y coordinates and a certain width
	 * and height.
	 * 
	 * @param xobject
	 *            The xobject to draw.
	 * @param x
	 *            The x-coordinate to draw the image.
	 * @param y
	 *            The y-coordinate to draw the image.
	 * @param width
	 *            The width of the image to draw.
	 * @param height
	 *            The height of the image to draw.
	 * 
	 * @throws IOException
	 *             If there is an error writing to the stream.
	 */
	public void drawXObject(PDXObject xobject, float x, float y, float width,
			float height) throws IOException {
		AffineTransform transform = new AffineTransform(width, 0, 0, height, x,
				y);
		drawXObject(xobject, transform);
	}

	/**
	 * Draw an xobject(form or image) using the given {@link AffineTransform} to
	 * position the xobject.
	 * 
	 * @param xobject
	 *            The xobject to draw.
	 * @param transform
	 *            the transformation matrix
	 * @throws IOException
	 *             If there is an error writing to the stream.
	 */
	public void drawXObject(PDXObject xobject, AffineTransform transform)
			throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: drawXObject is not allowed within a text block.");
		}
		String xObjectPrefix = null;
		if (xobject instanceof PDXObjectImage) {
			xObjectPrefix = "Im";
		} else {
			xObjectPrefix = "Form";
		}
		String objMapping = resources.addXObject(xobject, xObjectPrefix);
		saveGraphicsState();
		appendRawCommands(SPACE);
		concatenate2CTM(transform);
		appendRawCommands(SPACE);
		appendRawCommands("/");
		appendRawCommands(objMapping);
		appendRawCommands(SPACE);
		appendRawCommands(XOBJECT_DO);
		restoreGraphicsState();
	}

	/**
	 * The Td operator. A current text matrix will be replaced with a new one (1
	 * 0 0 1 x y).
	 * 
	 * @param x
	 *            The x coordinate.
	 * @param y
	 *            The y coordinate.
	 * @throws IOException
	 *             If there is an error writing to the stream.
	 */
	public void moveTextPositionByAmount(float x, float y) throws IOException {
		if (!inTextMode) {
			throw new IOException(
					"Error: must call beginText() before moveTextPositionByAmount");
		}
		appendRawCommands(x);
		appendRawCommands(SPACE);
		appendRawCommands(y);
		appendRawCommands(SPACE);
		appendRawCommands(MOVE_TEXT_POSITION);
	}

	/**
	 * The Tm operator. Sets the text matrix to the given values. A current text
	 * matrix will be replaced with the new one.
	 * 
	 * @param a
	 *            The a value of the matrix.
	 * @param b
	 *            The b value of the matrix.
	 * @param c
	 *            The c value of the matrix.
	 * @param d
	 *            The d value of the matrix.
	 * @param e
	 *            The e value of the matrix.
	 * @param f
	 *            The f value of the matrix.
	 * @throws IOException
	 *             If there is an error writing to the stream.
	 */
	public void setTextMatrix(double a, double b, double c, double d, double e,
			double f) throws IOException {
		if (!inTextMode) {
			throw new IOException(
					"Error: must call beginText() before setTextMatrix");
		}
		appendRawCommands(a);
		appendRawCommands(SPACE);
		appendRawCommands(b);
		appendRawCommands(SPACE);
		appendRawCommands(c);
		appendRawCommands(SPACE);
		appendRawCommands(d);
		appendRawCommands(SPACE);
		appendRawCommands(e);
		appendRawCommands(SPACE);
		appendRawCommands(f);
		appendRawCommands(SPACE);
		appendRawCommands(SET_TEXT_MATRIX);
	}

	/**
	 * The Tm operator. Sets the text matrix to the given values. A current text
	 * matrix will be replaced with the new one.
	 * 
	 * @param matrix
	 *            the transformation matrix
	 * @throws IOException
	 *             If there is an error writing to the stream.
	 */
	public void setTextMatrix(AffineTransform matrix) throws IOException {
		if (!inTextMode) {
			throw new IOException(
					"Error: must call beginText() before setTextMatrix");
		}
		appendMatrix(matrix);
		appendRawCommands(SET_TEXT_MATRIX);
	}

	/**
	 * The Tm operator. Sets the text matrix to the given scaling and
	 * translation values. A current text matrix will be replaced with the new
	 * one.
	 * 
	 * @param sx
	 *            The scaling factor in x-direction.
	 * @param sy
	 *            The scaling factor in y-direction.
	 * @param tx
	 *            The translation value in x-direction.
	 * @param ty
	 *            The translation value in y-direction.
	 * @throws IOException
	 *             If there is an error writing to the stream.
	 */
	public void setTextScaling(double sx, double sy, double tx, double ty)
			throws IOException {
		setTextMatrix(sx, 0, 0, sy, tx, ty);
	}

	/**
	 * The Tm operator. Sets the text matrix to the given translation values. A
	 * current text matrix will be replaced with the new one.
	 * 
	 * @param tx
	 *            The translation value in x-direction.
	 * @param ty
	 *            The translation value in y-direction.
	 * @throws IOException
	 *             If there is an error writing to the stream.
	 */
	public void setTextTranslation(double tx, double ty) throws IOException {
		setTextMatrix(1, 0, 0, 1, tx, ty);
	}

	/**
	 * The Tm operator. Sets the text matrix to the given rotation and
	 * translation values. A current text matrix will be replaced with the new
	 * one.
	 * 
	 * @param angle
	 *            The angle used for the counterclockwise rotation in radians.
	 * @param tx
	 *            The translation value in x-direction.
	 * @param ty
	 *            The translation value in y-direction.
	 * @throws IOException
	 *             If there is an error writing to the stream.
	 */
	public void setTextRotation(double angle, double tx, double ty)
			throws IOException {
		double angleCos = Math.cos(angle);
		double angleSin = Math.sin(angle);
		setTextMatrix(angleCos, angleSin, -angleSin, angleCos, tx, ty);
	}

	/**
	 * The Cm operator. Concatenates the current transformation matrix with the
	 * given values.
	 * 
	 * @param a
	 *            The a value of the matrix.
	 * @param b
	 *            The b value of the matrix.
	 * @param c
	 *            The c value of the matrix.
	 * @param d
	 *            The d value of the matrix.
	 * @param e
	 *            The e value of the matrix.
	 * @param f
	 *            The f value of the matrix.
	 * @throws IOException
	 *             If there is an error writing to the stream.
	 */
	public void concatenate2CTM(double a, double b, double c, double d,
			double e, double f) throws IOException {
		appendRawCommands(a);
		appendRawCommands(SPACE);
		appendRawCommands(b);
		appendRawCommands(SPACE);
		appendRawCommands(c);
		appendRawCommands(SPACE);
		appendRawCommands(d);
		appendRawCommands(SPACE);
		appendRawCommands(e);
		appendRawCommands(SPACE);
		appendRawCommands(f);
		appendRawCommands(SPACE);
		appendRawCommands(CONCATENATE_MATRIX);
	}

	/**
	 * The Cm operator. Concatenates the current transformation matrix with the
	 * given {@link AffineTransform}.
	 * 
	 * @param at
	 *            the transformation matrix
	 * @throws IOException
	 *             If there is an error writing to the stream.
	 */
	public void concatenate2CTM(AffineTransform at) throws IOException {
		appendMatrix(at);
		appendRawCommands(CONCATENATE_MATRIX);
	}

	/**
	 * This will draw a string at the current location on the screen.
	 * 
	 * @param text
	 *            The text to draw.
	 * @throws IOException
	 *             If an io exception occurs.
	 */
	public void drawString(String text) throws IOException {
		if (!inTextMode) {
			throw new IOException(
					"Error: must call beginText() before drawString");
		}
		COSString string = new COSString(text);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		string.writePDF(buffer);
		appendRawCommands(buffer.toByteArray());
		appendRawCommands(SPACE);
		appendRawCommands(SHOW_TEXT);
	}

	/**
	 * Set the stroking color space. This will add the colorspace to the
	 * PDResources if necessary.
	 * 
	 * @param colorSpace
	 *            The colorspace to write.
	 * @throws IOException
	 *             If there is an error writing the colorspace.
	 */
	public void setStrokingColorSpace(PDColorSpace colorSpace)
			throws IOException {
		currentStrokingColorSpace = colorSpace;
		writeColorSpace(colorSpace);
		appendRawCommands(SET_STROKING_COLORSPACE);
	}

	/**
	 * Set the stroking color space. This will add the colorspace to the
	 * PDResources if necessary.
	 * 
	 * @param colorSpace
	 *            The colorspace to write.
	 * @throws IOException
	 *             If there is an error writing the colorspace.
	 */
	public void setNonStrokingColorSpace(PDColorSpace colorSpace)
			throws IOException {
		currentNonStrokingColorSpace = colorSpace;
		writeColorSpace(colorSpace);
		appendRawCommands(SET_NON_STROKING_COLORSPACE);
	}

	private void writeColorSpace(PDColorSpace colorSpace) throws IOException {
		COSName key = null;
		if (colorSpace instanceof PDDeviceGray
				|| colorSpace instanceof PDDeviceRGB
				|| colorSpace instanceof PDDeviceCMYK) {
			key = COSName.getPDFName(colorSpace.getName());
		} else {
			COSDictionary colorSpaces = (COSDictionary) resources
					.getCOSDictionary().getDictionaryObject(COSName.COLORSPACE);
			if (colorSpaces == null) {
				colorSpaces = new COSDictionary();
				resources.getCOSDictionary().setItem(COSName.COLORSPACE,
						colorSpaces);
			}
			key = colorSpaces.getKeyForValue(colorSpace.getCOSObject());

			if (key == null) {
				int counter = 0;
				String csName = "CS";
				while (colorSpaces.containsValue(csName + counter)) {
					counter++;
				}
				key = COSName.getPDFName(csName + counter);
				colorSpaces.setItem(key, colorSpace);
			}
		}
		key.writePDF(output);
		appendRawCommands(SPACE);
	}

	/**
	 * Set the color components of current stroking colorspace.
	 * 
	 * @param components
	 *            The components to set for the current color.
	 * @throws IOException
	 *             If there is an error while writing to the stream.
	 */
	public void setStrokingColor(float[] components) throws IOException {
		for (int i = 0; i < components.length; i++) {
			appendRawCommands(components[i]);
			appendRawCommands(SPACE);
		}
		if (currentStrokingColorSpace instanceof PDSeparation
				|| currentStrokingColorSpace instanceof PDPattern
				|| currentStrokingColorSpace instanceof PDDeviceN
				|| currentStrokingColorSpace instanceof PDICCBased) {
			appendRawCommands(SET_STROKING_COLOR_COMPLEX);
		} else {
			appendRawCommands(SET_STROKING_COLOR_SIMPLE);
		}
	}

	/**
	 * Set the stroking color, specified as RGB.
	 * 
	 * @param color
	 *            The color to set.
	 * @throws IOException
	 *             If an IO error occurs while writing to the stream.
	 */
	public void setStrokingColor(Color color) throws IOException {
		ColorSpace colorSpace = color.getColorSpace();
		if (colorSpace.getType() == ColorSpace.TYPE_RGB) {
			setStrokingColor(color.getRed(), color.getGreen(), color.getBlue());
		} else if (colorSpace.getType() == ColorSpace.TYPE_GRAY) {
			color.getColorComponents(colorComponents);
			setStrokingColor(colorComponents[0]);
		} else if (colorSpace.getType() == ColorSpace.TYPE_CMYK) {
			color.getColorComponents(colorComponents);
			setStrokingColor(colorComponents[0], colorComponents[1],
					colorComponents[2], colorComponents[3]);
		} else {
			throw new IOException("Error: unknown colorspace:" + colorSpace);
		}
	}

	/**
	 * Set the non stroking color, specified as RGB.
	 * 
	 * @param color
	 *            The color to set.
	 * @throws IOException
	 *             If an IO error occurs while writing to the stream.
	 */
	public void setNonStrokingColor(Color color) throws IOException {
		ColorSpace colorSpace = color.getColorSpace();
		if (colorSpace.getType() == ColorSpace.TYPE_RGB) {
			setNonStrokingColor(color.getRed(), color.getGreen(),
					color.getBlue());
		} else if (colorSpace.getType() == ColorSpace.TYPE_GRAY) {
			color.getColorComponents(colorComponents);
			setNonStrokingColor(colorComponents[0]);
		} else if (colorSpace.getType() == ColorSpace.TYPE_CMYK) {
			color.getColorComponents(colorComponents);
			setNonStrokingColor(colorComponents[0], colorComponents[1],
					colorComponents[2], colorComponents[3]);
		} else {
			throw new IOException("Error: unknown colorspace:" + colorSpace);
		}
	}

	/**
	 * Set the stroking color, specified as RGB, 0-255.
	 * 
	 * @param r
	 *            The red value.
	 * @param g
	 *            The green value.
	 * @param b
	 *            The blue value.
	 * @throws IOException
	 *             If an IO error occurs while writing to the stream.
	 */
	public void setStrokingColor(int r, int g, int b) throws IOException {
		appendRawCommands(r / 255d);
		appendRawCommands(SPACE);
		appendRawCommands(g / 255d);
		appendRawCommands(SPACE);
		appendRawCommands(b / 255d);
		appendRawCommands(SPACE);
		appendRawCommands(RG_STROKING);
	}

	/**
	 * Set the stroking color, specified as CMYK, 0-255.
	 * 
	 * @param c
	 *            The cyan value.
	 * @param m
	 *            The magenta value.
	 * @param y
	 *            The yellow value.
	 * @param k
	 *            The black value.
	 * @throws IOException
	 *             If an IO error occurs while writing to the stream.
	 */
	public void setStrokingColor(int c, int m, int y, int k) throws IOException {
		appendRawCommands(c / 255d);
		appendRawCommands(SPACE);
		appendRawCommands(m / 255d);
		appendRawCommands(SPACE);
		appendRawCommands(y / 255d);
		appendRawCommands(SPACE);
		appendRawCommands(k / 255d);
		appendRawCommands(SPACE);
		appendRawCommands(K_STROKING);
	}

	/**
	 * Set the stroking color, specified as CMYK, 0.0-1.0.
	 * 
	 * @param c
	 *            The cyan value.
	 * @param m
	 *            The magenta value.
	 * @param y
	 *            The yellow value.
	 * @param k
	 *            The black value.
	 * @throws IOException
	 *             If an IO error occurs while writing to the stream.
	 */
	public void setStrokingColor(double c, double m, double y, double k)
			throws IOException {
		appendRawCommands(c);
		appendRawCommands(SPACE);
		appendRawCommands(m);
		appendRawCommands(SPACE);
		appendRawCommands(y);
		appendRawCommands(SPACE);
		appendRawCommands(k);
		appendRawCommands(SPACE);
		appendRawCommands(K_STROKING);
	}

	/**
	 * Set the stroking color, specified as grayscale, 0-255.
	 * 
	 * @param g
	 *            The gray value.
	 * @throws IOException
	 *             If an IO error occurs while writing to the stream.
	 */
	public void setStrokingColor(int g) throws IOException {
		appendRawCommands(g / 255d);
		appendRawCommands(SPACE);
		appendRawCommands(G_STROKING);
	}

	/**
	 * Set the stroking color, specified as Grayscale 0.0-1.0.
	 * 
	 * @param g
	 *            The gray value.
	 * @throws IOException
	 *             If an IO error occurs while writing to the stream.
	 */
	public void setStrokingColor(double g) throws IOException {
		appendRawCommands(g);
		appendRawCommands(SPACE);
		appendRawCommands(G_STROKING);
	}

	/**
	 * Set the color components of current non stroking colorspace.
	 * 
	 * @param components
	 *            The components to set for the current color.
	 * @throws IOException
	 *             If there is an error while writing to the stream.
	 */
	public void setNonStrokingColor(float[] components) throws IOException {
		for (int i = 0; i < components.length; i++) {
			appendRawCommands(components[i]);
			appendRawCommands(SPACE);
		}
		if (currentNonStrokingColorSpace instanceof PDSeparation
				|| currentNonStrokingColorSpace instanceof PDPattern
				|| currentNonStrokingColorSpace instanceof PDDeviceN
				|| currentNonStrokingColorSpace instanceof PDICCBased) {
			appendRawCommands(SET_NON_STROKING_COLOR_COMPLEX);
		} else {
			appendRawCommands(SET_NON_STROKING_COLOR_SIMPLE);
		}
	}

	/**
	 * Set the non stroking color, specified as RGB, 0-255.
	 * 
	 * @param r
	 *            The red value.
	 * @param g
	 *            The green value.
	 * @param b
	 *            The blue value.
	 * @throws IOException
	 *             If an IO error occurs while writing to the stream.
	 */
	public void setNonStrokingColor(int r, int g, int b) throws IOException {
		appendRawCommands(r / 255d);
		appendRawCommands(SPACE);
		appendRawCommands(g / 255d);
		appendRawCommands(SPACE);
		appendRawCommands(b / 255d);
		appendRawCommands(SPACE);
		appendRawCommands(RG_NON_STROKING);
	}

	/**
	 * Set the non stroking color, specified as CMYK, 0-255.
	 * 
	 * @param c
	 *            The cyan value.
	 * @param m
	 *            The magenta value.
	 * @param y
	 *            The yellow value.
	 * @param k
	 *            The black value.
	 * @throws IOException
	 *             If an IO error occurs while writing to the stream.
	 */
	public void setNonStrokingColor(int c, int m, int y, int k)
			throws IOException {
		appendRawCommands(c / 255d);
		appendRawCommands(SPACE);
		appendRawCommands(m / 255d);
		appendRawCommands(SPACE);
		appendRawCommands(y / 255d);
		appendRawCommands(SPACE);
		appendRawCommands(k / 255d);
		appendRawCommands(SPACE);
		appendRawCommands(K_NON_STROKING);
	}

	/**
	 * Set the non stroking color, specified as CMYK, 0.0-1.0.
	 * 
	 * @param c
	 *            The cyan value.
	 * @param m
	 *            The magenta value.
	 * @param y
	 *            The yellow value.
	 * @param k
	 *            The black value.
	 * @throws IOException
	 *             If an IO error occurs while writing to the stream.
	 */
	public void setNonStrokingColor(double c, double m, double y, double k)
			throws IOException {
		appendRawCommands(c);
		appendRawCommands(SPACE);
		appendRawCommands(m);
		appendRawCommands(SPACE);
		appendRawCommands(y);
		appendRawCommands(SPACE);
		appendRawCommands(k);
		appendRawCommands(SPACE);
		appendRawCommands(K_NON_STROKING);
	}

	/**
	 * Set the non stroking color, specified as grayscale, 0-255.
	 * 
	 * @param g
	 *            The gray value.
	 * @throws IOException
	 *             If an IO error occurs while writing to the stream.
	 */
	public void setNonStrokingColor(int g) throws IOException {
		appendRawCommands(g / 255d);
		appendRawCommands(SPACE);
		appendRawCommands(G_NON_STROKING);
	}

	/**
	 * Set the non stroking color, specified as Grayscale 0.0-1.0.
	 * 
	 * @param g
	 *            The gray value.
	 * @throws IOException
	 *             If an IO error occurs while writing to the stream.
	 */
	public void setNonStrokingColor(double g) throws IOException {
		appendRawCommands(g);
		appendRawCommands(SPACE);
		appendRawCommands(G_NON_STROKING);
	}

	/**
	 * Add a rectangle to the current path.
	 * 
	 * @param x
	 *            The lower left x coordinate.
	 * @param y
	 *            The lower left y coordinate.
	 * @param width
	 *            The width of the rectangle.
	 * @param height
	 *            The height of the rectangle.
	 * @throws IOException
	 *             If there is an error while drawing on the screen.
	 */
	public void addRect(float x, float y, float width, float height)
			throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: addRect is not allowed within a text block.");
		}
		appendRawCommands(x);
		appendRawCommands(SPACE);
		appendRawCommands(y);
		appendRawCommands(SPACE);
		appendRawCommands(width);
		appendRawCommands(SPACE);
		appendRawCommands(height);
		appendRawCommands(SPACE);
		appendRawCommands(RECTANGLE);
	}

	/**
	 * Draw a rectangle on the page using the current non stroking color.
	 * 
	 * @param x
	 *            The lower left x coordinate.
	 * @param y
	 *            The lower left y coordinate.
	 * @param width
	 *            The width of the rectangle.
	 * @param height
	 *            The height of the rectangle.
	 * @throws IOException
	 *             If there is an error while drawing on the screen.
	 */
	public void fillRect(float x, float y, float width, float height)
			throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: fillRect is not allowed within a text block.");
		}
		addRect(x, y, width, height);
		fill(PathIterator.WIND_NON_ZERO);
	}

	/**
	 * Append a cubic BÃ©zier curve to the current path. The curve extends from
	 * the current point to the point (x3 , y3 ), using (x1 , y1 ) and (x2 , y2
	 * ) as the BÃ©zier control points
	 * 
	 * @param x1
	 *            x coordinate of the point 1
	 * @param y1
	 *            y coordinate of the point 1
	 * @param x2
	 *            x coordinate of the point 2
	 * @param y2
	 *            y coordinate of the point 2
	 * @param x3
	 *            x coordinate of the point 3
	 * @param y3
	 *            y coordinate of the point 3
	 * @throws IOException
	 *             If there is an error while adding the .
	 */
	public void addBezier312(float x1, float y1, float x2, float y2, float x3,
			float y3) throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: addBezier312 is not allowed within a text block.");
		}
		appendRawCommands(x1);
		appendRawCommands(SPACE);
		appendRawCommands(y1);
		appendRawCommands(SPACE);
		appendRawCommands(x2);
		appendRawCommands(SPACE);
		appendRawCommands(y2);
		appendRawCommands(SPACE);
		appendRawCommands(x3);
		appendRawCommands(SPACE);
		appendRawCommands(y3);
		appendRawCommands(SPACE);
		appendRawCommands(BEZIER_312);
	}

	/**
	 * Append a cubic BÃ©zier curve to the current path. The curve extends from
	 * the current point to the point (x3 , y3 ), using the current point and
	 * (x2 , y2 ) as the BÃ©zier control points
	 * 
	 * @param x2
	 *            x coordinate of the point 2
	 * @param y2
	 *            y coordinate of the point 2
	 * @param x3
	 *            x coordinate of the point 3
	 * @param y3
	 *            y coordinate of the point 3
	 * @throws IOException
	 *             If there is an error while adding the .
	 */
	public void addBezier32(float x2, float y2, float x3, float y3)
			throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: addBezier32 is not allowed within a text block.");
		}
		appendRawCommands(x2);
		appendRawCommands(SPACE);
		appendRawCommands(y2);
		appendRawCommands(SPACE);
		appendRawCommands(x3);
		appendRawCommands(SPACE);
		appendRawCommands(y3);
		appendRawCommands(SPACE);
		appendRawCommands(BEZIER_32);
	}

	/**
	 * Append a cubic BÃ©zier curve to the current path. The curve extends from
	 * the current point to the point (x3 , y3 ), using (x1 , y1 ) and (x3 , y3
	 * ) as the BÃ©zier control points
	 * 
	 * @param x1
	 *            x coordinate of the point 1
	 * @param y1
	 *            y coordinate of the point 1
	 * @param x3
	 *            x coordinate of the point 3
	 * @param y3
	 *            y coordinate of the point 3
	 * @throws IOException
	 *             If there is an error while adding the .
	 */
	public void addBezier31(float x1, float y1, float x3, float y3)
			throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: addBezier31 is not allowed within a text block.");
		}
		appendRawCommands(x1);
		appendRawCommands(SPACE);
		appendRawCommands(y1);
		appendRawCommands(SPACE);
		appendRawCommands(x3);
		appendRawCommands(SPACE);
		appendRawCommands(y3);
		appendRawCommands(SPACE);
		appendRawCommands(BEZIER_313);
	}

	/**
	 * Add a line to the given coordinate.
	 * 
	 * @param x
	 *            The x coordinate.
	 * @param y
	 *            The y coordinate.
	 * @throws IOException
	 *             If there is an error while adding the line.
	 */
	public void moveTo(float x, float y) throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: moveTo is not allowed within a text block.");
		}
		appendRawCommands(x);
		appendRawCommands(SPACE);
		appendRawCommands(y);
		appendRawCommands(SPACE);
		appendRawCommands(MOVE_TO);
	}

	/**
	 * Add a move to the given coordinate.
	 * 
	 * @param x
	 *            The x coordinate.
	 * @param y
	 *            The y coordinate.
	 * @throws IOException
	 *             If there is an error while adding the line.
	 */
	public void lineTo(float x, float y) throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: lineTo is not allowed within a text block.");
		}
		appendRawCommands(x);
		appendRawCommands(SPACE);
		appendRawCommands(y);
		appendRawCommands(SPACE);
		appendRawCommands(LINE_TO);
	}

	/**
	 * add a line to the current path.
	 * 
	 * @param xStart
	 *            The start x coordinate.
	 * @param yStart
	 *            The start y coordinate.
	 * @param xEnd
	 *            The end x coordinate.
	 * @param yEnd
	 *            The end y coordinate.
	 * @throws IOException
	 *             If there is an error while adding the line.
	 */
	public void addLine(float xStart, float yStart, float xEnd, float yEnd)
			throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: addLine is not allowed within a text block.");
		}
		// moveTo
		moveTo(xStart, yStart);
		// lineTo
		lineTo(xEnd, yEnd);
	}

	/**
	 * Draw a line on the page using the current non stroking color and the
	 * current line width.
	 * 
	 * @param xStart
	 *            The start x coordinate.
	 * @param yStart
	 *            The start y coordinate.
	 * @param xEnd
	 *            The end x coordinate.
	 * @param yEnd
	 *            The end y coordinate.
	 * @throws IOException
	 *             If there is an error while drawing on the screen.
	 */
	public void drawLine(float xStart, float yStart, float xEnd, float yEnd)
			throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: drawLine is not allowed within a text block.");
		}
		addLine(xStart, yStart, xEnd, yEnd);
		// stroke
		stroke();
	}

	/**
	 * Add a polygon to the current path.
	 * 
	 * @param x
	 *            x coordinate of each points
	 * @param y
	 *            y coordinate of each points
	 * @throws IOException
	 *             If there is an error while drawing on the screen.
	 */
	public void addPolygon(float[] x, float[] y) throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: addPolygon is not allowed within a text block.");
		}
		if (x.length != y.length) {
			throw new IOException("Error: some points are missing coordinate");
		}
		for (int i = 0; i < x.length; i++) {
			if (i == 0) {
				moveTo(x[i], y[i]);
			} else {
				lineTo(x[i], y[i]);
			}
		}
		closeSubPath();
	}

	/**
	 * Draw a polygon on the page using the current non stroking color.
	 * 
	 * @param x
	 *            x coordinate of each points
	 * @param y
	 *            y coordinate of each points
	 * @throws IOException
	 *             If there is an error while drawing on the screen.
	 */
	public void drawPolygon(float[] x, float[] y) throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: drawPolygon is not allowed within a text block.");
		}
		addPolygon(x, y);
		stroke();
	}

	/**
	 * Draw and fill a polygon on the page using the current non stroking color.
	 * 
	 * @param x
	 *            x coordinate of each points
	 * @param y
	 *            y coordinate of each points
	 * @throws IOException
	 *             If there is an error while drawing on the screen.
	 */
	public void fillPolygon(float[] x, float[] y) throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: fillPolygon is not allowed within a text block.");
		}
		addPolygon(x, y);
		fill(PathIterator.WIND_NON_ZERO);
	}

	/**
	 * Stroke the path.
	 * 
	 * @throws IOException
	 *             If there is an error while stroking the path.
	 */
	public void stroke() throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: stroke is not allowed within a text block.");
		}
		appendRawCommands(STROKE);
	}

	/**
	 * Close and stroke the path.
	 * 
	 * @throws IOException
	 *             If there is an error while closing and stroking the path.
	 */
	public void closeAndStroke() throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: closeAndStroke is not allowed within a text block.");
		}
		appendRawCommands(CLOSE_STROKE);
	}

	/**
	 * Fill the path.
	 * 
	 * @param windingRule
	 *            the winding rule to be used for filling
	 * 
	 * @throws IOException
	 *             If there is an error while filling the path.
	 */
	public void fill(int windingRule) throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: fill is not allowed within a text block.");
		}
		if (windingRule == PathIterator.WIND_NON_ZERO) {
			appendRawCommands(FILL_NON_ZERO);
		} else if (windingRule == PathIterator.WIND_EVEN_ODD) {
			appendRawCommands(FILL_EVEN_ODD);
		} else {
			throw new IOException("Error: unknown value for winding rule");
		}

	}

	/**
	 * Close subpath.
	 * 
	 * @throws IOException
	 *             If there is an error while closing the subpath.
	 */
	public void closeSubPath() throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: closeSubPath is not allowed within a text block.");
		}
		appendRawCommands(CLOSE_SUBPATH);
	}

	/**
	 * Clip path.
	 * 
	 * @param windingRule
	 *            the winding rule to be used for clipping
	 * 
	 * @throws IOException
	 *             If there is an error while clipping the path.
	 */
	public void clipPath(int windingRule) throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: clipPath is not allowed within a text block.");
		}
		if (windingRule == PathIterator.WIND_NON_ZERO) {
			appendRawCommands(CLIP_PATH_NON_ZERO);
			appendRawCommands(NOP);
		} else if (windingRule == PathIterator.WIND_EVEN_ODD) {
			appendRawCommands(CLIP_PATH_EVEN_ODD);
			appendRawCommands(NOP);
		} else {
			throw new IOException("Error: unknown value for winding rule");
		}
	}

	/**
	 * Set linewidth to the given value.
	 * 
	 * @param lineWidth
	 *            The width which is used for drwaing.
	 * @throws IOException
	 *             If there is an error while drawing on the screen.
	 */
	public void setLineWidth(float lineWidth) throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: setLineWidth is not allowed within a text block.");
		}
		appendRawCommands(lineWidth);
		appendRawCommands(SPACE);
		appendRawCommands(LINE_WIDTH);
	}

	/**
	 * Set the line join style.
	 * 
	 * @param lineJoinStyle
	 *            0 for miter join, 1 for round join, and 2 for bevel join.
	 * @throws IOException
	 *             If there is an error while writing to the stream.
	 */
	public void setLineJoinStyle(int lineJoinStyle) throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: setLineJoinStyle is not allowed within a text block.");
		}
		if (lineJoinStyle >= 0 && lineJoinStyle <= 2) {
			appendRawCommands(Integer.toString(lineJoinStyle));
			appendRawCommands(SPACE);
			appendRawCommands(LINE_JOIN_STYLE);
		} else {
			throw new IOException("Error: unknown value for line join style");
		}
	}

	/**
	 * Set the line cap style.
	 * 
	 * @param lineCapStyle
	 *            0 for butt cap, 1 for round cap, and 2 for projecting square
	 *            cap.
	 * @throws IOException
	 *             If there is an error while writing to the stream.
	 */
	public void setLineCapStyle(int lineCapStyle) throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: setLineCapStyle is not allowed within a text block.");
		}
		if (lineCapStyle >= 0 && lineCapStyle <= 2) {
			appendRawCommands(Integer.toString(lineCapStyle));
			appendRawCommands(SPACE);
			appendRawCommands(LINE_CAP_STYLE);
		} else {
			throw new IOException("Error: unknown value for line cap style");
		}
	}

	/**
	 * Set the line dash pattern.
	 * 
	 * @param pattern
	 *            The pattern array
	 * @param phase
	 *            The phase of the pattern
	 * @throws IOException
	 *             If there is an error while writing to the stream.
	 */
	public void setLineDashPattern(float[] pattern, float phase)
			throws IOException {
		if (inTextMode) {
			throw new IOException(
					"Error: setLineDashPattern is not allowed within a text block.");
		}
		appendRawCommands(OPENING_BRACKET);
		for (float value : pattern) {
			appendRawCommands(value);
			appendRawCommands(SPACE);
		}
		appendRawCommands(CLOSING_BRACKET);
		appendRawCommands(SPACE);
		appendRawCommands(phase);
		appendRawCommands(SPACE);
		appendRawCommands(LINE_DASH_PATTERN);
	}

	/**
	 * Begin a marked content sequence.
	 * 
	 * @param tag
	 *            the tag
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void beginMarkedContentSequence(COSName tag) throws IOException {
		appendCOSName(tag);
		appendRawCommands(SPACE);
		appendRawCommands(BMC);
	}

	/**
	 * Begin a marked content sequence with a reference to an entry in the page
	 * resources' Properties dictionary.
	 * 
	 * @param tag
	 *            the tag
	 * @param propsName
	 *            the properties reference
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void beginMarkedContentSequence(COSName tag, COSName propsName)
			throws IOException {
		appendCOSName(tag);
		appendRawCommands(SPACE);
		appendCOSName(propsName);
		appendRawCommands(SPACE);
		appendRawCommands(BDC);
	}

	/**
	 * End a marked content sequence.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void endMarkedContentSequence() throws IOException {
		appendRawCommands(EMC);
	}

	/**
	 * q operator. Saves the current graphics state.
	 * 
	 * @throws IOException
	 *             If an error occurs while writing to the stream.
	 */
	public void saveGraphicsState() throws IOException {
		appendRawCommands(SAVE_GRAPHICS_STATE);
	}

	/**
	 * Q operator. Restores the current graphics state.
	 * 
	 * @throws IOException
	 *             If an error occurs while writing to the stream.
	 */
	public void restoreGraphicsState() throws IOException {
		appendRawCommands(RESTORE_GRAPHICS_STATE);
	}

	/**
	 * This will append raw commands to the content stream.
	 * 
	 * @param commands
	 *            The commands to append to the stream.
	 * @throws IOException
	 *             If an error occurs while writing to the stream.
	 */
	public void appendRawCommands(String commands) throws IOException {
		appendRawCommands(commands.getBytes("ISO-8859-1"));
	}

	/**
	 * This will append raw commands to the content stream.
	 * 
	 * @param commands
	 *            The commands to append to the stream.
	 * @throws IOException
	 *             If an error occurs while writing to the stream.
	 */
	public void appendRawCommands(byte[] commands) throws IOException {
		output.write(commands);
	}

	/**
	 * This will append raw commands to the content stream.
	 * 
	 * @param data
	 *            Append a raw byte to the stream.
	 * 
	 * @throws IOException
	 *             If an error occurs while writing to the stream.
	 */
	public void appendRawCommands(int data) throws IOException {
		output.write(data);
	}

	/**
	 * This will append raw commands to the content stream.
	 * 
	 * @param data
	 *            Append a formatted double value to the stream.
	 * 
	 * @throws IOException
	 *             If an error occurs while writing to the stream.
	 */
	public void appendRawCommands(double data) throws IOException {
		appendRawCommands(formatDecimal.format(data));
	}

	/**
	 * This will append raw commands to the content stream.
	 * 
	 * @param data
	 *            Append a formatted float value to the stream.
	 * 
	 * @throws IOException
	 *             If an error occurs while writing to the stream.
	 */
	public void appendRawCommands(float data) throws IOException {
		appendRawCommands(formatDecimal.format(data));
	}

	/**
	 * This will append a {@link COSName} to the content stream.
	 * 
	 * @param name
	 *            the name
	 * @throws IOException
	 *             If an error occurs while writing to the stream.
	 */
	public void appendCOSName(COSName name) throws IOException {
		name.writePDF(output);
	}

	private void appendMatrix(AffineTransform transform) throws IOException {
		double[] values = new double[6];
		transform.getMatrix(values);
		for (double v : values) {
			appendRawCommands(v);
			appendRawCommands(SPACE);
		}
	}

	/**
	 * Close the content stream. This must be called when you are done with this
	 * object.
	 * 
	 * @throws IOException
	 *             If the underlying stream has a problem being written to.
	 */
	public void close() throws IOException {
		output.close();
		currentNonStrokingColorSpace = null;
		currentStrokingColorSpace = null;
		page = null;
		resources = null;
	}

	public PDResources getResources() {
		return resources;
	}

	public void setResources(PDResources resources) {
		this.resources = resources;
	}

=======
package org.json;

/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

/**
 * A JSONObject is an unordered collection of name/value pairs. Its
 * external form is a string wrapped in curly braces with colons between the
 * names and values, and commas between the values and names. The internal form
 * is an object having <code>get</code> and <code>opt</code> methods for
 * accessing the values by name, and <code>put</code> methods for adding or
 * replacing values by name. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the <code>JSONObject.NULL</code>
 * object. A JSONObject constructor can be used to convert an external form
 * JSON text into an internal form whose values can be retrieved with the
 * <code>get</code> and <code>opt</code> methods, or to convert values into a
 * JSON text using the <code>put</code> and <code>toString</code> methods.
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object, which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you.
 * <p>
 * The <code>put</code> methods adds values to an object. For example, <pre>
 *     myString = new JSONObject().put("JSON", "Hello, World!").toString();</pre>
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * the JSON syntax rules.
 * The constructors are more forgiving in the texts they will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 *     before the closing brace.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 *     quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 *     or single quote, and if they do not contain leading or trailing spaces,
 *     and if they do not contain any of these characters:
 *     <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers
 *     and if they are not the reserved words <code>true</code>,
 *     <code>false</code>, or <code>null</code>.</li>
 * <li>Keys can be followed by <code>=</code> or <code>=></code> as well as
 *     by <code>:</code>.</li>
 * <li>Values can be followed by <code>;</code> <small>(semicolon)</small> as
 *     well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0-</code> <small>(octal)</small> or
 *     <code>0x-</code> <small>(hex)</small> prefix.</li>
 * </ul>
 * @author JSON.org
 * @version 2009-03-06
 */
@SuppressWarnings("unchecked")
public class JSONObject {

	/**
	 * JSONObject.NULL is equivalent to the value that JavaScript calls null,
	 * whilst Java's null is equivalent to the value that JavaScript calls
	 * undefined.
	 */
	private static final class Null {

		/**
		 * There is only intended to be a single instance of the NULL object,
		 * so the clone method returns itself.
		 * @return     NULL.
		 */
		protected final Object clone() {
			return this;
		}


		/**
		 * A Null object is equal to the null value and to itself.
		 * @param object    An object to test for nullness.
		 * @return true if the object parameter is the JSONObject.NULL object
		 *  or null.
		 */
		public boolean equals(Object object) {
			return object == null || object == this;
		}


		/**
		 * Get the "null" string value.
		 * @return The string "null".
		 */
		public String toString() {
			return "null";
		}
	}

	/**
	 * The map where the JSONObject's properties are kept.
	 */
	private Map map;

	/**
	 * It is sometimes more convenient and less ambiguous to have a
	 * <code>NULL</code> object than to use Java's <code>null</code> value.
	 * <code>JSONObject.NULL.equals(null)</code> returns <code>true</code>.
	 * <code>JSONObject.NULL.toString()</code> returns <code>"null"</code>.
	 */
	public static final Object NULL = new Null();


	/**
	 * Construct an empty JSONObject.
	 */
	public JSONObject() {
		this.map = new HashMap();
	}


	/**
	 * Construct a JSONObject from a subset of another JSONObject.
	 * An array of strings is used to identify the keys that should be copied.
	 * Missing keys are ignored.
	 * @param jo A JSONObject.
	 * @param names An array of strings.
	 * @exception JSONException If a value is a non-finite number or if a name is duplicated.
	 */
	public JSONObject(JSONObject jo, String[] names) throws JSONException {
		this();
		for (int i = 0; i < names.length; i += 1) {
			putOnce(names[i], jo.opt(names[i]));
		}
	}


	/**
	 * Construct a JSONObject from a JSONTokener.
	 * @param x A JSONTokener object containing the source string.
	 * @throws JSONException If there is a syntax error in the source string
	 *  or a duplicated key.
	 */
	public JSONObject(JSONTokener x) throws JSONException {
		this();
		char c;
		String key;

		if (x.nextClean() != '{') {
			throw x.syntaxError("A JSONObject text must begin with '{'");
		}
		for (;;) {
			c = x.nextClean();
			switch (c) {
				case 0:
					throw x.syntaxError("A JSONObject text must end with '}'");
				case '}':
					return;
				default:
					x.back();
					key = x.nextValue().toString();
			}

			/*
			 * The key is followed by ':'. We will also tolerate '=' or '=>'.
			 */

			c = x.nextClean();
			if (c == '=') {
				if (x.next() != '>') {
					x.back();
				}
			} else if (c != ':') {
				throw x.syntaxError("Expected a ':' after a key");
			}
			putOnce(key, x.nextValue());

			/*
			 * Pairs are separated by ','. We will also tolerate ';'.
			 */

			switch (x.nextClean()) {
				case ';':
				case ',':
					if (x.nextClean() == '}') {
						return;
					}
					x.back();
					break;
				case '}':
					return;
				default:
					throw x.syntaxError("Expected a ',' or '}'");
			}
		}
	}


	/**
	 * Construct a JSONObject from a Map.
	 *
	 * @param map A map object that can be used to initialize the contents of
	 *  the JSONObject.
	 */
	public JSONObject(Map map) {
		this.map = (map == null) ? new HashMap() : map;
	}


	/**
	 * Construct a JSONObject from a Map.
	 *
	 * Note: Use this constructor when the map contains <key,bean>.
	 *
	 * @param map - A map with Key-Bean data.
	 * @param includeSuperClass - Tell whether to include the super class properties.
	 */
	public JSONObject(Map map, boolean includeSuperClass) {
		this.map = new HashMap();
		if (map != null) {
			Iterator i = map.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry e = (Map.Entry) i.next();
				if (isStandardProperty(e.getValue().getClass())) {
					this.map.put(e.getKey(), e.getValue());
				} else {
					this.map.put(e.getKey(), new JSONObject(e.getValue(), includeSuperClass));
				}
			}
		}
	}


	/**
	 * Construct a JSONObject from an Object using bean getters.
	 * It reflects on all of the public methods of the object.
	 * For each of the methods with no parameters and a name starting
	 * with <code>"get"</code> or <code>"is"</code> followed by an uppercase letter,
	 * the method is invoked, and a key and the value returned from the getter method
	 * are put into the new JSONObject.
	 *
	 * The key is formed by removing the <code>"get"</code> or <code>"is"</code> prefix.
	 * If the second remaining character is not upper case, then the first
	 * character is converted to lower case.
	 *
	 * For example, if an object has a method named <code>"getName"</code>, and
	 * if the result of calling <code>object.getName()</code> is <code>"Larry Fine"</code>,
	 * then the JSONObject will contain <code>"name": "Larry Fine"</code>.
	 *
	 * @param bean An object that has getter methods that should be used
	 * to make a JSONObject.
	 */
	public JSONObject(Object bean) {
		this();
		populateInternalMap(bean, false);
	}


	/**
	 * Construct a JSONObject from an Object using bean getters.
	 * It reflects on all of the public methods of the object.
	 * For each of the methods with no parameters and a name starting
	 * with <code>"get"</code> or <code>"is"</code> followed by an uppercase letter,
	 * the method is invoked, and a key and the value returned from the getter method
	 * are put into the new JSONObject.
	 *
	 * The key is formed by removing the <code>"get"</code> or <code>"is"</code> prefix.
	 * If the second remaining character is not upper case, then the first
	 * character is converted to lower case.
	 *
	 * @param bean An object that has getter methods that should be used
	 * to make a JSONObject.
	 * @param includeSuperClass If true, include the super class properties.
	 */
	public JSONObject(Object bean, boolean includeSuperClass) {
		this();
		populateInternalMap(bean, includeSuperClass);
	}


	private void populateInternalMap(Object bean, boolean includeSuperClass) {
		Class klass = bean.getClass();

		/* If klass.getSuperClass is System class then force includeSuperClass to false. */

		if (klass.getClassLoader() == null) {
			includeSuperClass = false;
		}

		Method[] methods = (includeSuperClass) ? klass.getMethods() : klass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i += 1) {
			try {
				Method method = methods[i];
				if (Modifier.isPublic(method.getModifiers())) {
					String name = method.getName();
					String key = "";
					if (name.startsWith("get")) {
						key = name.substring(3);
					} else if (name.startsWith("is")) {
						key = name.substring(2);
					}
					if (key.length() > 0 && Character.isUpperCase(key.charAt(0)) && method.getParameterTypes().length == 0) {
						if (key.length() == 1) {
							key = key.toLowerCase();
						} else if (!Character.isUpperCase(key.charAt(1))) {
							key = key.substring(0, 1).toLowerCase() + key.substring(1);
						}

						Object result = method.invoke(bean, (Object[]) null);
						if (result == null) {
							map.put(key, NULL);
						} else if (result.getClass().isArray()) {
							map.put(key, new JSONArray(result, includeSuperClass));
						} else if (result instanceof Collection) { // List or Set
							map.put(key, new JSONArray((Collection) result, includeSuperClass));
						} else if (result instanceof Map) {
							map.put(key, new JSONObject((Map) result, includeSuperClass));
						} else if (isStandardProperty(result.getClass())) { // Primitives, String and Wrapper
							map.put(key, result);
						} else {
							if (result.getClass().getPackage().getName().startsWith("java") || result.getClass().getClassLoader() == null) {
								map.put(key, result.toString());
							} else { // User defined Objects
								map.put(key, new JSONObject(result, includeSuperClass));
							}
						}
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}


	static boolean isStandardProperty(Class clazz) {
		return clazz.isPrimitive() || clazz.isAssignableFrom(Byte.class) || clazz.isAssignableFrom(Short.class) || clazz.isAssignableFrom(Integer.class) || clazz.isAssignableFrom(Long.class) || clazz.isAssignableFrom(Float.class) || clazz.isAssignableFrom(Double.class) || clazz.isAssignableFrom(Character.class) || clazz.isAssignableFrom(String.class) || clazz.isAssignableFrom(Boolean.class);
	}


	/**
	 * Construct a JSONObject from an Object, using reflection to find the
	 * public members. The resulting JSONObject's keys will be the strings
	 * from the names array, and the values will be the field values associated
	 * with those keys in the object. If a key is not found or not visible,
	 * then it will not be copied into the new JSONObject.
	 * @param object An object that has fields that should be used to make a
	 * JSONObject.
	 * @param names An array of strings, the names of the fields to be obtained
	 * from the object.
	 */
	public JSONObject(Object object, String names[]) {
		this();
		Class c = object.getClass();
		for (int i = 0; i < names.length; i += 1) {
			String name = names[i];
			try {
				putOpt(name, c.getField(name).get(object));
			} catch (Exception e) {
				/* forget about it */
			}
		}
	}


	/**
	 * Construct a JSONObject from a source JSON text string.
	 * This is the most commonly used JSONObject constructor.
	 * @param source    A string beginning
	 *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
	 *  with <code>}</code>&nbsp;<small>(right brace)</small>.
	 * @exception JSONException If there is a syntax error in the source
	 *  string or a duplicated key.
	 */
	public JSONObject(String source) throws JSONException {
		this(new JSONTokener(source));
	}


	/**
	 * Accumulate values under a key. It is similar to the put method except
	 * that if there is already an object stored under the key then a
	 * JSONArray is stored under the key to hold all of the accumulated values.
	 * If there is already a JSONArray, then the new value is appended to it.
	 * In contrast, the put method replaces the previous value.
	 * @param key   A key string.
	 * @param value An object to be accumulated under the key.
	 * @return this.
	 * @throws JSONException If the value is an invalid number
	 *  or if the key is null.
	 */
	public JSONObject accumulate(String key, Object value) throws JSONException {
		testValidity(value);
		Object o = opt(key);
		if (o == null) {
			put(key, value instanceof JSONArray ? new JSONArray().put(value) : value);
		} else if (o instanceof JSONArray) {
			((JSONArray) o).put(value);
		} else {
			put(key, new JSONArray().put(o).put(value));
		}
		return this;
	}


	/**
	 * Append values to the array under a key. If the key does not exist in the
	 * JSONObject, then the key is put in the JSONObject with its value being a
	 * JSONArray containing the value parameter. If the key was already
	 * associated with a JSONArray, then the value parameter is appended to it.
	 * @param key   A key string.
	 * @param value An object to be accumulated under the key.
	 * @return this.
	 * @throws JSONException If the key is null or if the current value
	 *  associated with the key is not a JSONArray.
	 */
	public JSONObject append(String key, Object value) throws JSONException {
		testValidity(value);
		Object o = opt(key);
		if (o == null) {
			put(key, new JSONArray().put(value));
		} else if (o instanceof JSONArray) {
			put(key, ((JSONArray) o).put(value));
		} else {
			throw new JSONException("JSONObject[" + key + "] is not a JSONArray.");
		}
		return this;
	}


	/**
	 * Produce a string from a double. The string "null" will be returned if
	 * the number is not finite.
	 * @param  d A double.
	 * @return A String.
	 */
	static public String doubleToString(double d) {
		if (Double.isInfinite(d) || Double.isNaN(d)) {
			return "null";
		}

		// Shave off trailing zeros and decimal point, if possible.

		String s = Double.toString(d);
		if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
			while (s.endsWith("0")) {
				s = s.substring(0, s.length() - 1);
			}
			if (s.endsWith(".")) {
				s = s.substring(0, s.length() - 1);
			}
		}
		return s;
	}


	/**
	 * Get the value object associated with a key.
	 *
	 * @param key   A key string.
	 * @return      The object associated with the key.
	 * @throws   JSONException if the key is not found.
	 */
	public Object get(String key) throws JSONException {
		Object o = opt(key);
		if (o == null) {
			throw new JSONException("JSONObject[" + quote(key) + "] not found.");
		}
		return o;
	}


	/**
	 * Get the boolean value associated with a key.
	 *
	 * @param key   A key string.
	 * @return      The truth.
	 * @throws   JSONException
	 *  if the value is not a Boolean or the String "true" or "false".
	 */
	public boolean getBoolean(String key) throws JSONException {
		Object o = get(key);
		if (o.equals(Boolean.FALSE) || (o instanceof String && ((String) o).equalsIgnoreCase("false"))) {
			return false;
		} else if (o.equals(Boolean.TRUE) || (o instanceof String && ((String) o).equalsIgnoreCase("true"))) {
			return true;
		}
		throw new JSONException("JSONObject[" + quote(key) + "] is not a Boolean.");
	}


	/**
	 * Get the double value associated with a key.
	 * @param key   A key string.
	 * @return      The numeric value.
	 * @throws JSONException if the key is not found or
	 *  if the value is not a Number object and cannot be converted to a number.
	 */
	public double getDouble(String key) throws JSONException {
		Object o = get(key);
		try {
			return o instanceof Number ? ((Number) o).doubleValue() : Double.valueOf((String) o).doubleValue();
		} catch (Exception e) {
			throw new JSONException("JSONObject[" + quote(key) + "] is not a number.");
		}
	}


	/**
	 * Get the int value associated with a key. If the number value is too
	 * large for an int, it will be clipped.
	 *
	 * @param key   A key string.
	 * @return      The integer value.
	 * @throws   JSONException if the key is not found or if the value cannot
	 *  be converted to an integer.
	 */
	public int getInt(String key) throws JSONException {
		Object o = get(key);
		return o instanceof Number ? ((Number) o).intValue() : (int) getDouble(key);
	}


	/**
	 * Get the JSONArray value associated with a key.
	 *
	 * @param key   A key string.
	 * @return      A JSONArray which is the value.
	 * @throws   JSONException if the key is not found or
	 *  if the value is not a JSONArray.
	 */
	public JSONArray getJSONArray(String key) throws JSONException {
		Object o = get(key);
		if (o instanceof JSONArray) {
			return (JSONArray) o;
		}
		throw new JSONException("JSONObject[" + quote(key) + "] is not a JSONArray.");
	}


	/**
	 * Get the JSONObject value associated with a key.
	 *
	 * @param key   A key string.
	 * @return      A JSONObject which is the value.
	 * @throws   JSONException if the key is not found or
	 *  if the value is not a JSONObject.
	 */
	public JSONObject getJSONObject(String key) throws JSONException {
		Object o = get(key);
		if (o instanceof JSONObject) {
			return (JSONObject) o;
		}
		throw new JSONException("JSONObject[" + quote(key) + "] is not a JSONObject.");
	}


	/**
	 * Get the long value associated with a key. If the number value is too
	 * long for a long, it will be clipped.
	 *
	 * @param key   A key string.
	 * @return      The long value.
	 * @throws   JSONException if the key is not found or if the value cannot
	 *  be converted to a long.
	 */
	public long getLong(String key) throws JSONException {
		Object o = get(key);
		return o instanceof Number ? ((Number) o).longValue() : (long) getDouble(key);
	}


	/**
	 * Get an array of field names from a JSONObject.
	 *
	 * @return An array of field names, or null if there are no names.
	 */
	public static String[] getNames(JSONObject jo) {
		int length = jo.length();
		if (length == 0) {
			return null;
		}
		Iterator i = jo.keys();
		String[] names = new String[length];
		int j = 0;
		while (i.hasNext()) {
			names[j] = (String) i.next();
			j += 1;
		}
		return names;
	}


	/**
	 * Get an array of field names from an Object.
	 *
	 * @return An array of field names, or null if there are no names.
	 */
	public static String[] getNames(Object object) {
		if (object == null) {
			return null;
		}
		Class klass = object.getClass();
		Field[] fields = klass.getFields();
		int length = fields.length;
		if (length == 0) {
			return null;
		}
		String[] names = new String[length];
		for (int i = 0; i < length; i += 1) {
			names[i] = fields[i].getName();
		}
		return names;
	}


	/**
	 * Get the string associated with a key.
	 *
	 * @param key   A key string.
	 * @return      A string which is the value.
	 * @throws   JSONException if the key is not found.
	 */
	public String getString(String key) throws JSONException {
		return get(key).toString();
	}


	/**
	 * Determine if the JSONObject contains a specific key.
	 * @param key   A key string.
	 * @return      true if the key exists in the JSONObject.
	 */
	public boolean has(String key) {
		return this.map.containsKey(key);
	}


	/**
	 * Determine if the value associated with the key is null or if there is
	 *  no value.
	 * @param key   A key string.
	 * @return      true if there is no value associated with the key or if
	 *  the value is the JSONObject.NULL object.
	 */
	public boolean isNull(String key) {
		return JSONObject.NULL.equals(opt(key));
	}


	/**
	 * Get an enumeration of the keys of the JSONObject.
	 *
	 * @return An iterator of the keys.
	 */
	public Iterator keys() {
		return this.map.keySet().iterator();
	}


	/**
	 * Get the number of keys stored in the JSONObject.
	 *
	 * @return The number of keys in the JSONObject.
	 */
	public int length() {
		return this.map.size();
	}


	/**
	 * Produce a JSONArray containing the names of the elements of this
	 * JSONObject.
	 * @return A JSONArray containing the key strings, or null if the JSONObject
	 * is empty.
	 */
	public JSONArray names() {
		JSONArray ja = new JSONArray();
		Iterator keys = keys();
		while (keys.hasNext()) {
			ja.put(keys.next());
		}
		return ja.length() == 0 ? null : ja;
	}


	/**
	 * Produce a string from a Number.
	 * @param  n A Number
	 * @return A String.
	 * @throws JSONException If n is a non-finite number.
	 */
	static public String numberToString(Number n) throws JSONException {
		if (n == null) {
			throw new JSONException("Null pointer");
		}
		testValidity(n);

		// Shave off trailing zeros and decimal point, if possible.

		String s = n.toString();
		if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
			while (s.endsWith("0")) {
				s = s.substring(0, s.length() - 1);
			}
			if (s.endsWith(".")) {
				s = s.substring(0, s.length() - 1);
			}
		}
		return s;
	}


	/**
	 * Get an optional value associated with a key.
	 * @param key   A key string.
	 * @return      An object which is the value, or null if there is no value.
	 */
	public Object opt(String key) {
		return key == null ? null : this.map.get(key);
	}


	/**
	 * Get an optional boolean associated with a key.
	 * It returns false if there is no such key, or if the value is not
	 * Boolean.TRUE or the String "true".
	 *
	 * @param key   A key string.
	 * @return      The truth.
	 */
	public boolean optBoolean(String key) {
		return optBoolean(key, false);
	}


	/**
	 * Get an optional boolean associated with a key.
	 * It returns the defaultValue if there is no such key, or if it is not
	 * a Boolean or the String "true" or "false" (case insensitive).
	 *
	 * @param key              A key string.
	 * @param defaultValue     The default.
	 * @return      The truth.
	 */
	public boolean optBoolean(String key, boolean defaultValue) {
		try {
			return getBoolean(key);
		} catch (Exception e) {
			return defaultValue;
		}
	}


	/**
	 * Put a key/value pair in the JSONObject, where the value will be a
	 * JSONArray which is produced from a Collection.
	 * @param key   A key string.
	 * @param value A Collection value.
	 * @return      this.
	 * @throws JSONException
	 */
	public JSONObject put(String key, Collection value) throws JSONException {
		put(key, new JSONArray(value));
		return this;
	}


	/**
	 * Get an optional double associated with a key,
	 * or NaN if there is no such key or if its value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A string which is the key.
	 * @return      An object which is the value.
	 */
	public double optDouble(String key) {
		return optDouble(key, Double.NaN);
	}


	/**
	 * Get an optional double associated with a key, or the
	 * defaultValue if there is no such key or if its value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A key string.
	 * @param defaultValue     The default.
	 * @return      An object which is the value.
	 */
	public double optDouble(String key, double defaultValue) {
		try {
			Object o = opt(key);
			return o instanceof Number ? ((Number) o).doubleValue() : new Double((String) o).doubleValue();
		} catch (Exception e) {
			return defaultValue;
		}
	}


	/**
	 * Get an optional int value associated with a key,
	 * or zero if there is no such key or if the value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A key string.
	 * @return      An object which is the value.
	 */
	public int optInt(String key) {
		return optInt(key, 0);
	}


	/**
	 * Get an optional int value associated with a key,
	 * or the default if there is no such key or if the value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A key string.
	 * @param defaultValue     The default.
	 * @return      An object which is the value.
	 */
	public int optInt(String key, int defaultValue) {
		try {
			return getInt(key);
		} catch (Exception e) {
			return defaultValue;
		}
	}


	/**
	 * Get an optional JSONArray associated with a key.
	 * It returns null if there is no such key, or if its value is not a
	 * JSONArray.
	 *
	 * @param key   A key string.
	 * @return      A JSONArray which is the value.
	 */
	public JSONArray optJSONArray(String key) {
		Object o = opt(key);
		return o instanceof JSONArray ? (JSONArray) o : null;
	}


	/**
	 * Get an optional JSONObject associated with a key.
	 * It returns null if there is no such key, or if its value is not a
	 * JSONObject.
	 *
	 * @param key   A key string.
	 * @return      A JSONObject which is the value.
	 */
	public JSONObject optJSONObject(String key) {
		Object o = opt(key);
		return o instanceof JSONObject ? (JSONObject) o : null;
	}


	/**
	 * Get an optional long value associated with a key,
	 * or zero if there is no such key or if the value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A key string.
	 * @return      An object which is the value.
	 */
	public long optLong(String key) {
		return optLong(key, 0);
	}


	/**
	 * Get an optional long value associated with a key,
	 * or the default if there is no such key or if the value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A key string.
	 * @param defaultValue     The default.
	 * @return      An object which is the value.
	 */
	public long optLong(String key, long defaultValue) {
		try {
			return getLong(key);
		} catch (Exception e) {
			return defaultValue;
		}
	}


	/**
	 * Get an optional string associated with a key.
	 * It returns an empty string if there is no such key. If the value is not
	 * a string and is not null, then it is coverted to a string.
	 *
	 * @param key   A key string.
	 * @return      A string which is the value.
	 */
	public String optString(String key) {
		return optString(key, "");
	}


	/**
	 * Get an optional string associated with a key.
	 * It returns the defaultValue if there is no such key.
	 *
	 * @param key   A key string.
	 * @param defaultValue     The default.
	 * @return      A string which is the value.
	 */
	public String optString(String key, String defaultValue) {
		Object o = opt(key);
		return o != null ? o.toString() : defaultValue;
	}


	/**
	 * Put a key/boolean pair in the JSONObject.
	 *
	 * @param key   A key string.
	 * @param value A boolean which is the value.
	 * @return this.
	 * @throws JSONException If the key is null.
	 */
	public JSONObject put(String key, boolean value) throws JSONException {
		put(key, value ? Boolean.TRUE : Boolean.FALSE);
		return this;
	}


	/**
	 * Put a key/double pair in the JSONObject.
	 *
	 * @param key   A key string.
	 * @param value A double which is the value.
	 * @return this.
	 * @throws JSONException If the key is null or if the number is invalid.
	 */
	public JSONObject put(String key, double value) throws JSONException {
		put(key, new Double(value));
		return this;
	}


	/**
	 * Put a key/int pair in the JSONObject.
	 *
	 * @param key   A key string.
	 * @param value An int which is the value.
	 * @return this.
	 * @throws JSONException If the key is null.
	 */
	public JSONObject put(String key, int value) throws JSONException {
		put(key, new Integer(value));
		return this;
	}


	/**
	 * Put a key/long pair in the JSONObject.
	 *
	 * @param key   A key string.
	 * @param value A long which is the value.
	 * @return this.
	 * @throws JSONException If the key is null.
	 */
	public JSONObject put(String key, long value) throws JSONException {
		put(key, new Long(value));
		return this;
	}


	/**
	 * Put a key/value pair in the JSONObject, where the value will be a
	 * JSONObject which is produced from a Map.
	 * @param key   A key string.
	 * @param value A Map value.
	 * @return      this.
	 * @throws JSONException
	 */
	public JSONObject put(String key, Map value) throws JSONException {
		put(key, new JSONObject(value));
		return this;
	}


	/**
	 * Put a key/value pair in the JSONObject. If the value is null,
	 * then the key will be removed from the JSONObject if it is present.
	 * @param key   A key string.
	 * @param value An object which is the value. It should be of one of these
	 *  types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
	 *  or the JSONObject.NULL object.
	 * @return this.
	 * @throws JSONException If the value is non-finite number
	 *  or if the key is null.
	 */
	public JSONObject put(String key, Object value) throws JSONException {
		if (key == null) {
			throw new JSONException("Null key.");
		}
		if (value != null) {
			testValidity(value);
			this.map.put(key, value);
		} else {
			remove(key);
		}
		return this;
	}


	/**
	 * Put a key/value pair in the JSONObject, but only if the key and the
	 * value are both non-null, and only if there is not already a member
	 * with that name.
	 * @param key
	 * @param value
	 * @return his.
	 * @throws JSONException if the key is a duplicate
	 */
	public JSONObject putOnce(String key, Object value) throws JSONException {
		if (key != null && value != null) {
			if (opt(key) != null) {
				throw new JSONException("Duplicate key \"" + key + "\"");
			}
			put(key, value);
		}
		return this;
	}


	/**
	 * Put a key/value pair in the JSONObject, but only if the
	 * key and the value are both non-null.
	 * @param key   A key string.
	 * @param value An object which is the value. It should be of one of these
	 *  types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
	 *  or the JSONObject.NULL object.
	 * @return this.
	 * @throws JSONException If the value is a non-finite number.
	 */
	public JSONObject putOpt(String key, Object value) throws JSONException {
		if (key != null && value != null) {
			put(key, value);
		}
		return this;
	}


	/**
	 * Produce a string in double quotes with backslash sequences in all the
	 * right places. A backslash will be inserted within </, allowing JSON
	 * text to be delivered in HTML. In JSON text, a string cannot contain a
	 * control character or an unescaped quote or backslash.
	 * @param string A String
	 * @return  A String correctly formatted for insertion in a JSON text.
	 */
	public static String quote(String string) {
		if (string == null || string.length() == 0) {
			return "\"\"";
		}

		char b;
		char c = 0;
		int i;
		int len = string.length();
		StringBuffer sb = new StringBuffer(len + 4);
		String t;

		sb.append('"');
		for (i = 0; i < len; i += 1) {
			b = c;
			c = string.charAt(i);
			switch (c) {
				case '\\':
				case '"':
					sb.append('\\');
					sb.append(c);
					break;
				case '/':
					if (b == '<') {
						sb.append('\\');
					}
					sb.append(c);
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\r':
					sb.append("\\r");
					break;
				default:
					if (c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
						t = "000" + Integer.toHexString(c);
						sb.append("\\u" + t.substring(t.length() - 4));
					} else {
						sb.append(c);
					}
			}
		}
		sb.append('"');
		return sb.toString();
	}


	/**
	 * Remove a name and its value, if present.
	 * @param key The name to be removed.
	 * @return The value that was associated with the name,
	 * or null if there was no value.
	 */
	public Object remove(String key) {
		return this.map.remove(key);
	}


	/**
	 * Get an enumeration of the keys of the JSONObject.
	 * The keys will be sorted alphabetically.
	 *
	 * @return An iterator of the keys.
	 */
	public Iterator sortedKeys() {
		return new TreeSet(this.map.keySet()).iterator();
	}


	/**
	 * Try to convert a string into a number, boolean, or null. If the string
	 * can't be converted, return the string.
	 * @param s A String.
	 * @return A simple JSON value.
	 */
	static public Object stringToValue(String s) {
		if (s.equals("")) {
			return s;
		}
		if (s.equalsIgnoreCase("true")) {
			return Boolean.TRUE;
		}
		if (s.equalsIgnoreCase("false")) {
			return Boolean.FALSE;
		}
		if (s.equalsIgnoreCase("null")) {
			return JSONObject.NULL;
		}

		/*
		 * If it might be a number, try converting it. We support the 0- and 0x-
		 * conventions. If a number cannot be produced, then the value will just
		 * be a string. Note that the 0-, 0x-, plus, and implied string
		 * conventions are non-standard. A JSON parser is free to accept
		 * non-JSON forms as long as it accepts all correct JSON forms.
		 */

		char b = s.charAt(0);
		if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+') {
			if (b == '0') {
				if (s.length() > 2 && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
					try {
						return new Integer(Integer.parseInt(s.substring(2), 16));
					} catch (Exception e) {
						/* Ignore the error */
					}
				} else {
					try {
						return new Integer(Integer.parseInt(s, 8));
					} catch (Exception e) {
						/* Ignore the error */
					}
				}
			}
			try {
				if (s.indexOf('.') > -1 || s.indexOf('e') > -1 || s.indexOf('E') > -1) {
					return Double.valueOf(s);
				} else {
					Long myLong = new Long(s);
					if (myLong.longValue() == myLong.intValue()) {
						return new Integer(myLong.intValue());
					} else {
						return myLong;
					}
				}
			} catch (Exception f) {
				/* Ignore the error */
			}
		}
		return s;
	}


	/**
	 * Throw an exception if the object is an NaN or infinite number.
	 * @param o The object to test.
	 * @throws JSONException If o is a non-finite number.
	 */
	static void testValidity(Object o) throws JSONException {
		if (o != null) {
			if (o instanceof Double) {
				if (((Double) o).isInfinite() || ((Double) o).isNaN()) {
					throw new JSONException("JSON does not allow non-finite numbers.");
				}
			} else if (o instanceof Float) {
				if (((Float) o).isInfinite() || ((Float) o).isNaN()) {
					throw new JSONException("JSON does not allow non-finite numbers.");
				}
			}
		}
	}


	/**
	 * Produce a JSONArray containing the values of the members of this
	 * JSONObject.
	 * @param names A JSONArray containing a list of key strings. This
	 * determines the sequence of the values in the result.
	 * @return A JSONArray of values.
	 * @throws JSONException If any of the values are non-finite numbers.
	 */
	public JSONArray toJSONArray(JSONArray names) throws JSONException {
		if (names == null || names.length() == 0) {
			return null;
		}
		JSONArray ja = new JSONArray();
		for (int i = 0; i < names.length(); i += 1) {
			ja.put(this.opt(names.getString(i)));
		}
		return ja;
	}


	/**
	 * Make a JSON text of this JSONObject. For compactness, no whitespace
	 * is added. If this would not result in a syntactically correct JSON text,
	 * then null will be returned instead.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 *
	 * @return a printable, displayable, portable, transmittable
	 *  representation of the object, beginning
	 *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
	 *  with <code>}</code>&nbsp;<small>(right brace)</small>.
	 */
	public String toString() {
		try {
			Iterator keys = keys();
			StringBuffer sb = new StringBuffer("{");

			while (keys.hasNext()) {
				if (sb.length() > 1) {
					sb.append(',');
				}
				Object o = keys.next();
				sb.append(quote(o.toString()));
				sb.append(':');
				sb.append(valueToString(this.map.get(o)));
			}
			sb.append('}');
			return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}


	/**
	 * Make a prettyprinted JSON text of this JSONObject.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * @param indentFactor The number of spaces to add to each level of
	 *  indentation.
	 * @return a printable, displayable, portable, transmittable
	 *  representation of the object, beginning
	 *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
	 *  with <code>}</code>&nbsp;<small>(right brace)</small>.
	 * @throws JSONException If the object contains an invalid number.
	 */
	public String toString(int indentFactor) throws JSONException {
		return toString(indentFactor, 0);
	}


	/**
	 * Make a prettyprinted JSON text of this JSONObject.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * @param indentFactor The number of spaces to add to each level of
	 *  indentation.
	 * @param indent The indentation of the top level.
	 * @return a printable, displayable, transmittable
	 *  representation of the object, beginning
	 *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
	 *  with <code>}</code>&nbsp;<small>(right brace)</small>.
	 * @throws JSONException If the object contains an invalid number.
	 */
	String toString(int indentFactor, int indent) throws JSONException {
		int j;
		int n = length();
		if (n == 0) {
			return "{}";
		}
		Iterator keys = sortedKeys();
		StringBuffer sb = new StringBuffer("{");
		int newindent = indent + indentFactor;
		Object o;
		if (n == 1) {
			o = keys.next();
			sb.append(quote(o.toString()));
			sb.append(": ");
			sb.append(valueToString(this.map.get(o), indentFactor, indent));
		} else {
			while (keys.hasNext()) {
				o = keys.next();
				if (sb.length() > 1) {
					sb.append(",\n");
				} else {
					sb.append('\n');
				}
				for (j = 0; j < newindent; j += 1) {
					sb.append(' ');
				}
				sb.append(quote(o.toString()));
				sb.append(": ");
				sb.append(valueToString(this.map.get(o), indentFactor, newindent));
			}
			if (sb.length() > 1) {
				sb.append('\n');
				for (j = 0; j < indent; j += 1) {
					sb.append(' ');
				}
			}
		}
		sb.append('}');
		return sb.toString();
	}


	/**
	 * Make a JSON text of an Object value. If the object has an
	 * value.toJSONString() method, then that method will be used to produce
	 * the JSON text. The method is required to produce a strictly
	 * conforming text. If the object does not contain a toJSONString
	 * method (which is the most common case), then a text will be
	 * produced by other means. If the value is an array or Collection,
	 * then a JSONArray will be made from it and its toJSONString method
	 * will be called. If the value is a MAP, then a JSONObject will be made
	 * from it and its toJSONString method will be called. Otherwise, the
	 * value's toString method will be called, and the result will be quoted.
	 *
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * @param value The value to be serialized.
	 * @return a printable, displayable, transmittable
	 *  representation of the object, beginning
	 *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
	 *  with <code>}</code>&nbsp;<small>(right brace)</small>.
	 * @throws JSONException If the value is or contains an invalid number.
	 */
	static String valueToString(Object value) throws JSONException {
		if (value == null || value.equals(null)) {
			return "null";
		}
		if (value instanceof JSONRawValue) {
			return value.toString();
		}
		if (value instanceof JSONString) {
			Object o;
			try {
				o = ((JSONString) value).toJSONString();
			} catch (Exception e) {
				throw new JSONException(e);
			}
			if (o instanceof String) {
				return (String) o;
			}
			throw new JSONException("Bad value from toJSONString: " + o);
		}
		if (value instanceof Number) {
			return numberToString((Number) value);
		}
		if (value instanceof Boolean || value instanceof JSONObject || value instanceof JSONArray) {
			return value.toString();
		}
		if (value instanceof Map) {
			return new JSONObject((Map) value).toString();
		}
		if (value instanceof Collection) {
			return new JSONArray((Collection) value).toString();
		}
		if (value.getClass().isArray()) {
			return new JSONArray(value).toString();
		}
		return quote(value.toString());
	}


	/**
	 * Make a prettyprinted JSON text of an object value.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * @param value The value to be serialized.
	 * @param indentFactor The number of spaces to add to each level of
	 *  indentation.
	 * @param indent The indentation of the top level.
	 * @return a printable, displayable, transmittable
	 *  representation of the object, beginning
	 *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
	 *  with <code>}</code>&nbsp;<small>(right brace)</small>.
	 * @throws JSONException If the object contains an invalid number.
	 */
	static String valueToString(Object value, int indentFactor, int indent) throws JSONException {
		if (value == null || value.equals(null)) {
			return "null";
		}
		try {
			if (value instanceof JSONString) {
				Object o = ((JSONString) value).toJSONString();
				if (o instanceof String) {
					return (String) o;
				}
			}
		} catch (Exception e) {
			/* forget about it */
		}
		if (value instanceof Number) {
			return numberToString((Number) value);
		}
		if (value instanceof Boolean) {
			return value.toString();
		}
		if (value instanceof JSONObject) {
			return ((JSONObject) value).toString(indentFactor, indent);
		}
		if (value instanceof JSONArray) {
			return ((JSONArray) value).toString(indentFactor, indent);
		}
		if (value instanceof Map) {
			return new JSONObject((Map) value).toString(indentFactor, indent);
		}
		if (value instanceof Collection) {
			return new JSONArray((Collection) value).toString(indentFactor, indent);
		}
		if (value.getClass().isArray()) {
			return new JSONArray(value).toString(indentFactor, indent);
		}
		return quote(value.toString());
	}


	/**
	 * Write the contents of the JSONObject as JSON text to a writer.
	 * For compactness, no whitespace is added.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 *
	 * @return The writer.
	 * @throws JSONException
	 */
	public Writer write(Writer writer) throws JSONException {
		try {
			boolean b = false;
			Iterator keys = keys();
			writer.write('{');

			while (keys.hasNext()) {
				if (b) {
					writer.write(',');
				}
				Object k = keys.next();
				writer.write(quote(k.toString()));
				writer.write(':');
				Object v = this.map.get(k);
				if (v instanceof JSONObject) {
					((JSONObject) v).write(writer);
				} else if (v instanceof JSONArray) {
					((JSONArray) v).write(writer);
				} else {
					writer.write(valueToString(v));
				}
				b = true;
			}
			writer.write('}');
			return writer;
		} catch (IOException e) {
			throw new JSONException(e);
		}
	}
>>>>>>> 76aa07461566a5976980e6696204781271955163
}
