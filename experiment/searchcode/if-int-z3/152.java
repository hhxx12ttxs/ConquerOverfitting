/**
 * fxgP5 is developed by Paul Vollmer (wrong-entertainment.com)
 * 
 * 
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package cc.wng.fxgP5;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import processing.core.*;





/**
 * This is the master fxgP5 class.
 * 
 * @example Basic
 */
public class Fxg {
	
	// build.properties
	public final static String JAVATARGETJERSION   = "##java.target.version##";
	public final static String PROJECTNAME         = "##project.name##";
	public final static String PROJECTCOMPILE      = "##project.compile##";
	public final static String AUTHORNAME          = "##author.name##";
	public final static String AUTHORURL           = "##author.url##";
	public final static String LIBRARYURL          = "##library.url##";
	public final static String LIBRARYCATEGORY     = "##library.category##";
	public final static String LIBRARYSENTENCE     = "##library.sentence##";
	public final static String VERSION             = "##library.version##";
	public final static String PRETTYVERSION       = "##library.prettyVersion##";
	public final static String SOURCEHOST          = "##source.host##";
	public final static String SOURCEURL           = "##source.url##";
	public final static String SOURCEREPO          = "##source.repository##";
	public final static String TESTEDPLATFORM      = "##tested.platform##";
	public final static String TESTEDP5VERSION     = "##tested.processingVersion##";
	
	
	// p5 is a reference to the parent sketch
	PApplet p5;
	public static boolean display = true;
	
	private File file;
	public static PrintWriter writer;
	public static boolean write = false;
	
	
	// Formats Print
	/* Pixel: 105, 74 */
	public final static int[] DIN_A10 = {105, 74};
	/* Pixel: 147, 105 */
	public final static int[] DIN_A9 = {147, 105};
	/* Pixel: 210, 147 */
	public final static int[] DIN_A8 = {210, 147};
	/* Pixel: 298, 210 */
	public final static int[] DIN_A7 = {298, 210};
	/* Pixel: 411, 298 */
	public final static int[] DIN_A6 = {411, 298};
	/* Pixel: 595, 411 */
	public final static int[] DIN_A5 = {595, 411};
	/* Pixel: 842, 595 */
	public final static int[] DIN_A4 = {842, 595};
	/* Pixel: 1191, 842 */
	public final static int[] DIN_A3 = {1191, 842};
	/* Pixel: 1684, 1191 */
	public final static int[] DIN_A2 = {1684, 1191};
	/* Pixel: 2384, 1684 */
	public final static int[] DIN_A1 = {2384, 1684};
	/* Pixel: 3370, 2384 */
	public final static int[] DIN_A0 = {3370, 2384};
	/* Pixel: 729, 516 */
	public final static int[] DIN_B5 = {729, 516};
	/* Pixel: 1032, 729 */
	public final static int[] DIN_B4 = {1032, 729};
	/* Pixel: 792, 612 */
	public final static int[] US_LETTER = {792, 612};
	/* Pixel: 1008, 612 */
	public final static int[] LEGAL = {1008, 612};
	
	// Formats Video
	/* Pixel: 654, 480 */
	public final static int[] NTSC_DV = {654, 480};
	/* Pixel: 873, 480 */
	public final static int[] NTSC_DV_WIDESCREEN = {873, 480};
	/* Pixel: 654, 486*/
	public final static int[] NTSC_D1 = {654, 486};
	/* Pixel: 873,  486 */
	public final static int[] NTSC_D1_WIDESCREEN = {873, 486};
	/* Pixel: 788, 675 */
	public final static int[] PAL_DV = {788, 576};
	/* Pixel: 1050, 576 */
	public final static int[] PAL_DV_WIDESCREEN =  {1050, 576};
	/* Pixel: 1280, 720 */
	public final static int[] HDV_HDTV_720 = {1280, 720};
	/* Pixel: 1920, 1080 */
	public final static int[] HDV_1080 = {1920, 1080};
	/* Pixel: 1280, 720 */
	public final static int[] DVCPRO_HD_720 = {1280, 720};
	/* Pixel: 1920, 1080 */
	public final static int[] DVCPRO_HD_1080 = {1920, 1080};
	/* Pixel: 1920, 1080 */
	public final static int[] HDTV_1080 = {1920, 1080};
	/* Pixel: 1828, 1332 */
	public final static int[] CINEON_HALF = {1828, 1332};
	/* Pixel: 3656, 2664 */
	public final static int[] CINEON_FULL = {3656, 2664};
	/* Pixel: 2048, 1556 */
	public final static int[] FILM_2K = {2048, 1556};
	/* Pixel: 4096, 3112 */
	public final static int[] FILM_4K = {4096, 3112};
	
	// Fxg
	public final static String FXG_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n"+
	                         				"<!-- created with Processing and the ##project.name## library developed by ##author.name## -->";
  
	public final static String FXG_ADOBE_HEADER = "\" ai:appVersion=\"15.0.0.399\" ATE:version=\"1.0.0\" flm:"+
                                                  "version=\"1.0.0\" d:using=\"\" xmlns=\"http://ns.adobe.com/fxg/2008\""+
                                                  " xmlns:ATE=\"http://ns.adobe.com/ate/2009\" xmlns:ai=\"http:"+
                                                  "//ns.adobe.com/ai/2009\" xmlns:d=\"http://ns.adobe.com/fxg/2008/dt\""+
                                                  " xmlns:flm=\"http://ns.adobe.com/flame/2008\">"; 
	
	
	// counter/name
	public static int ID_COUNT = 1;
	// format
	public static float WIDTH;
	public static float HEIGHT;
	
	
	
	// Adobe Illustrator Blend Mode
	//public static String BLENDMODE = "normal";
	
	
	
	
	
	
	
	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 * 
	 * @example Basic
	 * @param p
	 */
	public Fxg(PApplet p) {
		p5 = p;
		System.out.println("##library.name## ##library.prettyVersion## by ##author##");
	}
	
	
	
	
	
	/**
	 * beginDraw create a FXG File.
	 * 
	 * @param  filename String
	 * @param  width float
	 * @param  height float
	 */
	public void beginDraw(String filename, float width, float height) {
		// Set true to record fxg file.
		write = true;
		
		// Set Start Values
	    Page.PAGE_COUNT = 1;
	    Layer.LAYER_COUNT = 1;
	    ID_COUNT = 1;
	    Page.PAGE_NAME = "Page ";
	    Layer.LAYER_NAME = "Layer ";
	    
	    String path = p5.sketchPath(filename);
	    if (path != null) {
	    	file = new File(path);
	    	if (!file.isAbsolute()) file = null;
	    }
	    if (file == null) {
	    	throw new RuntimeException("##project.name## Error! createFXG requires an absolute path for the location of the output file.");
	    }
	    
	    // have to create file object here.
	    if (writer == null) {
	    	try {
	    		writer = new PrintWriter(new FileWriter(file));
	    	}
	    	catch (IOException e) {
	    		throw new RuntimeException(e); // java 1.4+
	    	}
	      
	    	// write
	    	writer.println(FXG_HEADER);
	    	writer.println("<Graphic version=\"2.0\" viewHeight=\""+height+"\" viewWidth=\""+width+FXG_ADOBE_HEADER);  
	    	writer.println("<Library/>");
	    	beginPage(width, height);
	      
	    	WIDTH = width;
	    	HEIGHT = height;
	   	}
	    
	}
	  
	  
	/**
	 * endDraw close the FXG File writer.
	 */
	public void endDraw() {
		// if 1 page and 1 layer
	    if(Page.PAGE_COUNT == 2 && Layer.LAYER_COUNT == 2) endPage();
	    // if 1 page and more layer
	    if(Page.PAGE_COUNT == 2 && Layer.LAYER_COUNT >= 3) endLayer();
	    
	    writer.println("<Private/>");
	    writer.println("</Graphic>");
	    System.out.println("##project.name## File writing Ready!");
	    
	    // finish writer
	    writer.flush();
	    writer.close();
	    writer = null;
	    
	    // end fxg record.
	    write = false;
	}
	
	
	
	
	
	// Page ////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * add a new Page to fxg file.
	 * The Page name is the default name + page number.
	 * The width and height is the same as the previous page.
	 */
	public void beginPage() {
		if(write == true) Page.fxgBeginPage(Page.PAGE_NAME+Page.PAGE_COUNT, WIDTH, HEIGHT);
	}
	
	/**
	 * add a new Page to Fxg file.
	 * The Page name is the default name + page number.
	 *
	 * @param  width Set the width of Page
	 * @param  height Set the height of Page
	 */
	public void beginPage(float width, float height) {
		if(write == true) Page.fxgBeginPage(Page.PAGE_NAME+Page.PAGE_COUNT, width, height);
	}
	
	/**
	 * Add a new page with new size to the fxg File.
	 *
	 * @param  pageName Set the name of page.
	 * @param  width Set the width of page.
	 * @param  height Set the height of page.
	 */
	public void beginPage(String pageName, float width, float height) {
		if(write == true) Page.fxgBeginPage(pageName, width, height);
	}
	
	/**
	 * End the current page.
	 */
	public void endPage() {
		if(write == true) Page.fxgEndPage();
	}
	
	/**
	 * Set the name of the next page.
	 *
	 * @param  name Name as String
	 */
	public void setPageName(String name) {
		if(write == true) Page.PAGE_NAME = name;
	}
	
	
	
	
	
	// Layer ///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Begin a new layer.
	 */
	public static void beginLayer() {
		if(write == true) Layer.fxgBeginLayer(Layer.LAYER_NAME+Layer.LAYER_COUNT);
	}
	
	
	
	/**
	 * Begin a new layer.
	 *
	 * @param name Set the name of the layer
	 */
	public static void beginLayer(String name) {
		if(write == true) Layer.fxgBeginLayer(name);
	}
	  
	  
	    
	/**
	 * End Layer.
	 */
	public static void endLayer() {
		if(write == true) Layer.fxgEndLayer();
	}
	  
	  
	  
	/**
	 * Set the name of layer.
	 *
	 * @param name Set the name of the Layer
	 */
	public static void setLayerName(String name) {
		if(write == true) Layer.LAYER_NAME = name;
	}
	
	
	
	
	
	// BACKGROUND //////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/background_.html">background</a>          
	 * @see PGraphics#background(int)
	 */
	public void background(int rgb) {
	    // P5
	    if(display == true) p5.background(rgb, 255);
	    // FXG XML
	    if(write == true) Graphics.fxgBackground(rgb, 255);
	}
	
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/background_.html">background</a>          
	 * @see PGraphics#background(int, float)
	 */
	public void background(int rgb, float alpha) {
		// P5
		if(display == true) p5.background(rgb, alpha);
	    // FXG XML
	    if(write == true) Graphics.fxgBackground(rgb, alpha);
	}
	
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/background_.html">background</a>          
	 * @see PGraphics#background(float)
	 */
	public void background(float gray) {
		// P5
		if(display == true) p5.background(gray, 255);
	    // FXG
	    if(write == true) Graphics.fxgBackground((int)gray, 255);
	}
	
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/background_.html">background</a>         
	 * @see PGraphics#background(float, float)
	 */
	public void background(float gray, float alpha) {
		// P5
		if(display == true) p5.background(gray, gray, gray, alpha);
	    // FXG
	    if(write == true) Graphics.fxgBackground(gray, gray, gray, alpha);
	}
	
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/background_.html">background</a>          
	 * @see PGraphics#background(float, float, float)
	 */
	public void background(float x, float y, float z) {
		// P5
		if(display == true) p5.background(x, y, z, 255);
	    // FXG
	    if(write == true) Graphics.fxgBackground(x, y, z, 255);
	}
	

	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/background_.html">background</a>         
	 * @see PGraphics#background(float, float, float, float)
	 */
	public void background(float x, float y, float z, float a) {
	    // P5
		if(display == true) p5.background(x, y, z, a);
	    // FXG
	    if(write == true) Graphics.fxgBackground(x, y, z, a);
	}
	
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/background_.html">background</a>          
	 * @see PGraphics#background(PImage)
	 */
	// TODO
	public void background(PImage image) {
	    //System.err.println("### wngFXG Error! not work at the moment");
	}





	// FILL ////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/noFill_.html">noFill</a>          
	 * @see PGraphics#noFill()
	 */
	public void noFill() {
	    // P5
		if(display == true) p5.noFill();
	    // FXG
	    if(write == true) Graphics.fxgNoFill();
	}
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/fill_.html">fill</a>          
	 * @see PGraphics#fill(int)
	 */
	public void fill(int rgb) {
		// P5
		if(display == true) p5.fill(rgb, 255);
	    // FXG
	    if(write == true) Graphics.fxgFill(rgb, 255);
	}
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/fill_.html">fill</a>          
	 * @see PGraphics#fill(int, float)
	 */
	public void fill(int rgb, float alpha) {
	    // P5
		if(display == true) p5.fill(rgb, alpha);
	    // FXG
	    if(write == true) Graphics.fxgFill(rgb, alpha);
	}

	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/fill_.html">fill</a>          
	 * @see PGraphics#fill(float)
	 */
	public void fill(float gray) {
	    // P5
		if(display == true) p5.fill(gray, 255);
	    // FXG
	    if(write == true) Graphics.fxgFill((int)gray, 255);
	}
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/fill_.html">fill</a>          
	 * @see PGraphics#fill(float, float)
	 */
	public void fill(float gray, float alpha) {
	    // P5
		if(display == true) p5.fill(gray, gray, gray, alpha);
	    // FXG
	    if(write == true) Graphics.fxgFill(gray, gray, gray, alpha);
	}
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/fill_.html">fill</a>          
	 * @see PGraphics#fill(float, float, float)
	 */
	public void fill(float x, float y, float z) {
	    // P5
		if(display == true) p5.fill(x, y, z, 255);
	    // FXG
	    if(write == true) Graphics.fxgFill(x, y, z, 255);
	}

	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/fill_.html">fill</a>          
	 * @see PGraphics#fill(float, float, float, float)
	 */
	public void fill(float x, float y, float z, float a) {
	    // P5
		if(display == true) p5.fill(x, y, z, a);
	    // FXG
	    if(write == true) Graphics.fxgFill(x, y, z, a);
	}
	
	
	
	
	
	// STROKE CAP/JOIN/WEIGHT //////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/strokeWeight_.html">strokeWeight</a>          
	 * @see PGraphics#strokeWeight(float)
	 */
	public void strokeWeight(float weight) {
	    // P5
		if(display == true) p5.strokeWeight(weight);
	    // FXG
	    if(write == true) Graphics.fxgStrokeWeight(weight);
	}
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/strokeJoin_.html">strokeJoin</a>          
	 * @see PGraphics#strokeJoin(int)
	 */
	public void strokeJoin(int join) {
	    // P5
		if(display == true) p5.strokeJoin(join);
	    // FXG
	    if(write == true) Graphics.fxgStrokeJoin(join);
	}
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/strokeCap_.html">strokeCap</a>          
	 * @see PGraphics#strokeCap(int)
	 */
	public void strokeCap(int cap) {
	    // P5
		if(display == true) p5.strokeCap(cap);
	    // FXG
	    if(write == true) Graphics.fxgStrokeCap(cap);
	}
	
	
	
	
	
	
	// STROKE COLOR ////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/noStroke_.html">noStroke</a>         
	 * @see PGraphics#noStroke()
	 */
	public void noStroke() {
		// P5
		if(display == true) p5.noStroke();
	    // FXG
	    if(write == true) Graphics.fxgNoStroke();
	}
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/stroke_.html">stroke</a>          
	 * @see PGraphics#stroke(int)
	 */
	public void stroke(int rgb) {
	    // P5
		if(display == true) p5.stroke(rgb, 255);
	    // FXG
	    if(write == true) Graphics.fxgStroke(rgb, 255);
	}
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/stroke_.html">stroke</a>           
	 * @see PGraphics#strokeCap(int, float)
	 */
	public void stroke(int rgb, float alpha) {
	    // P5
		if(display == true) p5.stroke(rgb, alpha);
	    // FXG
	    if(write == true) Graphics.fxgStroke(rgb, alpha);
	}

	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/stroke_.html">stroke</a>          
	 * @see PGraphics#strokeCap(float)
	 */
	public void stroke(float gray) {
	    // P5
		if(display == true) p5.stroke(gray);
	    // FXG
	    if(write == true) Graphics.fxgStroke(gray, gray, gray, 255);
	  }

	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/stroke_.html">stroke</a>          
	 * @see PGraphics#strokeCap(float, float)
	 */
	public void stroke(float gray, float alpha) {
	    // P5
		if(display == true) p5.stroke(gray, alpha);
	    // FXG
	    if(write == true) Graphics.fxgStroke(gray, gray, gray, alpha);
	  }

	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/stroke_.html">stroke</a>          
	 * @see PGraphics#strokeCap(float, float, float)
	 */
	public void stroke(float x, float y, float z) {
	    // P5
		if(display == true) p5.stroke(x, y, z);
	    // FXG
	    if(write == true) Graphics.fxgStroke(x, y, z, 255);
	  }

	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/stroke_.html">stroke</a>          
	 * @see PGraphics#strokeCap(float, float, float, float)
	 */
	public void stroke(float x, float y, float z, float a) {
		// P5
		if(display == true) p5.stroke(x, y, z, a);
	    // FXG
		if(write == true) Graphics.fxgStroke(x, y, z, a);
	}
	
	
	
	
	
	// LINEAR GRADIENT /////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method to set linear gradient.
	 * 
	 * @param r Ratio array
	 * @param c Color array
	 */
	public void linearGradient(float[] r, int[] c) {
	    // P5
		if(display == true) p5.fill(c[0]);
	    // FXG
	    if(write == true) Gradient.fxgLinearGradient(r, c);
	}	
	
	
	/**
	 * noLinearGradient
	 */
	public void noLinearGradient() {
		if(write == true) Gradient.fxgNoLinearGradient();
	}
	
	
	
	// RADIAL GRADIENT /////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method to set radial gradient.
	 * 
	 * @param r Ratio array
	 * @param c Color array
	 */
	public void radialGradient(float[] r, int[] c) {
	    // P5
		if(display == true) p5.fill(c[0]);
	    // FXG
	    if(write == true) Gradient.fxgLinearGradient(r, c);
	}
	
	
	/**
	 * noRadialGradient
	 */
	public void noRadialGradient() {
		if(write == true) Gradient.fxgNoLinearGradient();
	}

	
	
	
	
	// SHAPES 2D PRIMITIVES ////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
  
	// TODO A BIT BUGGY
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/arc_.html">arc</a>          
	 * @see PGraphics#arc(float, float, float, float, float, float)
	 */
	public void arc(float a, float b, float c, float d, float start, float stop) {
		// P5
		if(display == true) p5.arc(a, b, c, d, start, stop);
		// FXG
		if(write == true) Graphics.fxgArc(a, b, c, d, start, stop);
	}
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/ellipse_.html">ellipse</a>          
	 * @see PGraphics#ellipse(float, float, float, float)
	 */
	public void ellipse(float a, float b, float c, float d) {
		// P5
		if(display == true) p5.ellipse(a, b, c, d);
    	// FXG
		if(write == true) Graphics.fxgEllipse(a, b, c, d);
	}
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/ellipseMode_.html">ellipseMode</a>          
	 * @see PGraphics#ellipseMode(int)
	 */
	public void ellipseMode(int mode) {
		// P5
		if(display == true) p5.ellipseMode(mode);
		// FXG
		if(write == true) Graphics.fxgEllipseMode(mode);
	}
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/line.html">line</a>          
	 * @see PGraphics#line(float, float, float, float)
	 */
	public void line(float x1, float y1, float x2, float y2) {
		// P5
		if(display == true) p5.line(x1, y1, x2, y2);
		// FXG
		if(write == true) Graphics.fxgLine(x1, y1, x2, y2);
	}
  
	// TODO
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/line_.html">ine</a>          
	 * @see PGraphics#line(float, float, float, float, float, float)
	 */
	public void line(float x1, float y1, float z1, float x2, float y2, float z2) {
		//System.err.println("### wngFXG Error! line (P3D) not work");
	}
	
	// TODO
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/point_.html">point</a>          
	 * @see PGraphics#point(float, float)
	 */
	public void point(float x, float y) {
		// P5
		if(display == true) p5.point(x, y);
		// FXG
		if(write == true) Graphics.fxgRect(x, y, 1, 1);
	}
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/quad_.html">quad</a>          
	 * @see PGraphics#quad(float, float, float, float, float, float, float, float)
	 */
	public void quad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		// P5
		if(display == true) p5.quad(x1, y1, x2, y2, x3, y3, x4, y4);
    	// FXG
		if(write == true) Graphics.fxgQuad(x1, y1, x2, y2, x3, y3, x4, y4);
	}
  
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/rect_.html">rect</a>          
	 * @see PGraphics#rect(float, float, float, float)
	 */
	public void rect(float a, float b, float c, float d) {
		// P5
		if(display == true) p5.rect(a, b, c, d);
    	// FXG
		if(write == true) Graphics.fxgRect(a, b, c, d);
	}
  
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/rectMode_.html">rectMode</a>          
	 * @see PGraphics#rectMode(int)
	 */
	public void rectMode(int mode) {
		// P5
		if(display == true) p5.rectMode(mode);
    	// FXG
		if(write == true) Graphics.fxgRectMode(mode);
	}
	
	/**
	 * <b>See Processing Reference:</b> <a href="http://processing.org/reference/triangle_.html">triangle</a>          
	 * @see PGraphics#triangle(float, float, float, float, float, float)
	 */
	public void triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
		// P5
		if(display == true) p5.triangle(x1, y1, x2, y2, x3, y3);
    	// FXG
		if(write == true) Graphics.fxgTriangle(x1, y1, x2, y2, x3, y3);
	}
	
	/**
	 * Draw a rounded rectangle.
	 * 
	 * @param xpos X position.
	 * @param ypos Y position.
	 * @param width Width of rounded rectangle.
	 * @param height Height of rounded rectangle.
	 * @param radius Radius of corners.
	 */
	public void roundedRect(float xpos, float ypos, float width, float height, float radius) {
		// P5
		p5.beginShape();
			p5.vertex(xpos+radius, ypos);
			p5.vertex(xpos+width-radius, ypos);
			p5.bezierVertex(xpos+width-radius, ypos, xpos+width, ypos, xpos+width, ypos+radius);
			p5.vertex(xpos+width, ypos+radius);
			p5.vertex(xpos+width, ypos+height-radius);
			p5.bezierVertex(xpos+width, ypos+height-radius, xpos+width, ypos+height, xpos+width-radius, ypos+height);
			p5.vertex(xpos+width-radius, ypos+height);
			p5.vertex(xpos+radius, ypos+height);
			p5.bezierVertex(xpos+radius, ypos+height, xpos, ypos+height, xpos, ypos+height-radius);
			p5.vertex(xpos, ypos+height-radius);
			p5.vertex(xpos, ypos+radius);
			p5.bezierVertex(xpos, ypos+radius, xpos, ypos, xpos+radius, ypos);
		p5.endShape();
		
		// FXG
		if(write == true) Graphics.fxgRoundedRect(xpos, ypos, width, height, radius);
	}
  
  
  
	// TODO
	
	// SHAPES 2D CURVES ////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*public void bezier(float x1, float y1,
	                   float x2, float y2,
	                   float x3, float y3,
	                   float x4, float y4) {
	    //beginShape();
	    //vertex(x1, y1);
	    //bezierVertex(x2, y2, x3, y3, x4, y4);
	    //endShape();
	}
	  
	public void bezier(float x1, float y1, float z1,
	                   float x2, float y2, float z2,
	                   float x3, float y3, float z3,
	                   float x4, float y4, float z4) {
	    System.err.println("### wngFXG Error! bezier (P3D) not work");
	}
	  
	  
	public void bezierDetail() {
	    System.err.println("### wngFXG Error! bezierDetail (P3D, OPENGL) not work");
	}
	  
	  
	public void curveTightness(float tightness) {
	    System.err.println("### wngFXG Error! curveTightness not work");
	}
	  
	  
	public void curve(float x1, float y1,
	                  float x2, float y2,
	                  float x3, float y3,
	                  float x4, float y4) {
	    //beginShape();
	    //curveVertex(x1, y1);
	    //curveVertex(x2, y2);
	    //curveVertex(x3, y3);
	    //curveVertex(x4, y4);
	    //endShape();
	}


	public void curve(float x1, float y1, float z1,
	                  float x2, float y2, float z2,
	                  float x3, float y3, float z3,
	                  float x4, float y4, float z4) {
	    System.err.println("### wngFXG Error! bezierDetail (P3D, OPENGL) not work");
	}
	  
	  
	public void curveDetail() {
	    System.err.println("### wngFXG Error! bezierDetail (P3D, OPENGL) not work");
	}*/
	
	
	
	
	
	// SHAPES VERTEX ///////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	  
	/**
	 * Start a new shape of type POLYGON
	 */
	public void beginShape() {
		// P5
		if(display == true) p5.beginShape();
		// FXG
		if(write == true) Graphics.fxgBeginShape(20); // 20 = POLYGON
	}
	  
	  
	public void beginShape(int kind) {
		// P5
		if(display == true) p5.beginShape();
		// FXG
		if(write == true) Graphics.fxgBeginShape(kind);
	}
	  
	
	// TODO
	/*public void bezierVertex(float x2, float y2,
	                         float x3, float y3,
	                         float x4, float y4) {
	    System.err.println("### wngFXG Error! bezierVertex comming soon");
	}
	  
	  
	public void bezierVertex(float x2, float y2, float z2,
	                           float x3, float y3, float z3,
	                           float x4, float y4, float z4) {
		System.err.println("### wngFXG Error! bezierVertex (P3D, OPENGL) not work");
	}
	  
	  
	public void curveVertex(float x, float y) {
	    System.err.println("### wngFXG Error! curveVertex comming soon");
	}
	  
	public void curveVertex(float x, float y, float z) {
	    System.err.println("### wngFXG Error! curveVertex (P3D, OPENGL) not work");
	}*/
	  
	  
	
	public void endShape() {
		if(write == true) Graphics.fxgEndShape();
	}
	
	// TODO
	/*public void endShape(int mode) {
	    System.err.println("endShape does not exsist with wngFXG");
	}*/
	
	  
	/*public void texture() {
	}*/
	  
	/*public void textureMode() {
	}*/

	public void vertex(float x, float y) {// write to FXG file
		if(write == true) Graphics.fxgVertex(x, y);
	}
	
	// TODO
	/*public void vertex(float x, float y, float u, float v) {
	    Graphics.fxgVertex(x, y);
	    System.err.println("### wngFXG ERROR! vertexTexture comming soon");
	}*/
	  
	
	
	
	
	// TRANSFORM ///////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	  
	public void pushMatrix() {
	    // P5
		if(display == true) p5.pushMatrix();
	    // FXG
	    if(write == true) Graphics.fxgPushMatrix();
	}
	  
	  
	public void popMatrix() {
	    // P5
		if(display == true) p5.popMatrix();
	    // FXG
	    if(write == true) Graphics.fxgPopMatrix();
	}
	  
	  
	public void rotate(float angle) {
	    // P5
		if(display == true) p5.rotate(angle);
	    // FXG
	    if(write == true) Graphics.fxgPopMatrix();
	}
	  
	  
	public void scale(float s) {
	    // P5
		if(display == true) p5.scale(s);
	    // FXG
	    if(write == true) Graphics.fxgPopMatrix();
	}
	  
	  
	public void translate(float tx, float ty) {
	    // P5
		if(display == true) p5.translate(tx, ty);
	    // FXG
	    //if(write == true) 
	}
	  
	
	// TODO
	/*public void translate(float tx, float ty, float tz) {
	    System.out.println("### ##project.name## Error! ##project.name## does not work with P3D.");
	}
	  
	public void skewX(float a) {
	}
	
	public void skewY(float a) {
	}*/
	  
	  
	  
	  
	  
	// ADOBE ILLUSTRATOR FILTER DROP SHADOW ////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * dropShadowFilter create a shadow for a shape object.
	 *
	 * @param blur Set the blur value between 1 - 144
	 * @param c Set a hex value for color
	 * @param a Set the alpha value between 0.0 - 1.0
	 * @param x Ttranslate x
	 * @param y Translate y
	 */
	public void dropShadowFilter(int blur, int c, float a, float x, float y) {
		if(write == true) Filter.fxgDropShadowFilter(blur, c, a, x, y);
	}
	  
	/**
	 * dropShadowFilter create a shadow for a shape object.
	 *
	 * @param blur Set the blur value between 1 - 144
	 * @param c Set a hex value for color
	 * @param a Set the alpha value between 0.0 - 1.0
	 */
	public void dropShadowFilter(int blur, int c, float a) {
		if(write == true) Filter.fxgDropShadowFilter(blur, c, a, 7, 7);
	}

	/**
	 * dropShadowFilter create a shadow for a shape object.
	 * color value set to #000000
	 *
	 * @param blur Set the blur value between 1 - 144
	 * @param a Set the alpha value between 0.0 - 1.0
	 */
	public void dropShadowFilter(int blur, float a) {
		if(write == true) Filter.fxgDropShadowFilter(blur, 0x00000000, a, 7, 7);
	}

	/**
	 * dropShadowFilter create a shadow for a shape object.
	 * color value set to #000000
	 * alpha value set to 0.75
	 *
	 * @param blur Set the blur value between 1 - 144
	 */
	public void dropShadowFilter(int blur) {
		if(write == true) Filter.fxgDropShadowFilter(blur, 0x00000000, 0.75f, 7, 7);
	}

	/**
	 * dropShadowFilter create a shadow with standard value for a shape object.
	 */
	public void dropShadowFilter() {
		if(write == true) Filter.fxgDropShadowFilter(5, 0x00000000, 0.75f, 7, 7);
	}


	/**
	 * Reset dropShadowFilter.
	 */
	public void noDropShadowFilter() {
		if(write == true) Filter.fxgNoDropShadowFilter();
	}
	
	
	
	
	
	// ADOBE ILLUSTRATOR FILTER GLOW OUTER /////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * glowOuterFilter creates an outer glow for a shape object.
	 *
	 * @param blur Set the blur value between 1 - 144
	 * @param c Set a hex value for color
	 * @param a Set the alpha value between 0.0 - 1.0
	 */
	public void glowOuterFilter(int blur, int c, float a) {
		if(write == true) Filter.fxgGlowOuterFilter(blur, c, a);
	}
	  
	  
	/**
	 * glowOuterFilter creates an outer glow for a shape object.
	 * alpha value set to 0.75
	 *
	 * @param blur Set the blur value between 1 - 144
	 * @param c Set a hex value for color
	 */
	public void glowOuterFilter(int blur, int c) {
		if(write == true) Filter.fxgGlowOuterFilter(blur, c, 0.75f);
	}

	/**
	 * glowOuterFilter creates an outer glow for a shape object.
	 * color value set to #000000
	 * alpha value set to 0.75
	 *
	 * @param blur Set the blur value between 1 - 144
	 */
	public void glowOuterFilter(int blur) {
		if(write == true) Filter.fxgGlowOuterFilter(blur, 0x00000000, 0.75f);
	}

	/**
	 * glowOuterFilter creates an outer glow for a shape object.
	 * blur value set to 14
	 * color value set to #000000
	 * alpha value set to 0.75
	 */
	public void glowOuterFilter() {
		if(write == true) Filter.fxgGlowOuterFilter(14, 0x00000000, 0.75f);
	}


	/**
	 * Reset glowOuterFilter
	 */
	public void noGlowOuterFilter() {
		if(write == true) Filter.fxgNoGlowOuterFilter();
	}	





	// ADOBE ILLUSTRATOR FILTER GLOW INNER /////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * glowInnerFilter creates an outer glow for a shape object.
	 *
	 * @param blur Set the blur value between 1 - 144
	 * @param c Set a hex value for color
	 * @param a Set the alpha value between 0.0 - 1.0
	 */
	public void glowInnerFilter(int blur, int c, float a) {
		if(write == true) Filter.fxgGlowInnerFilter(blur, c, a);
	}


	/**
	 * glowInnerFilter creates an outer glow for a shape object.
	 * alpha value set to 0.75
	 *
	 * @param blur Set the blur value between 1 - 144
	 * @param c Set a hex value for color
	 */
	public void glowInnerFilter(int blur, int c) {
		if(write == true) Filter.fxgGlowInnerFilter(blur, c, 0.75f);
	}

	/**
	 * glowOuterFilter creates an outer glow for a shape object.
	 * color value set to #000000
	 * alpha value set to 0.75
	 *
	 * @param blur Set the blur value between 1 - 144
	 */
	public void glowInnerFilter(int blur) {
		if(write == true) Filter.fxgGlowInnerFilter(blur, 0x00000000, 0.75f);
	}

	/**
	 * glowOuterFilter creates an outer glow for a shape object.
	 * blur value set to 14
	 * color value set to #000000
	 * alpha value set to 0.75
	 */
	public void glowInnerFilter() {
		if(write == true) Filter.fxgGlowInnerFilter(14, 0x00000000, 0.75f);
	}


	/**
	 * Reset glowInnerFilter
	 */
	public void NoGlowInnerFilter() {
		if(write == true) Filter.fxgNoGlowInnerFilter();
	}
  
	
	
	/**
	 * blendMode change the Adobe Illustrator object mode.
	 *
	 * @param b Modes: NORMAL, DARKEN, MULTIPLY, COLORBURN, LIGHTEN,
	 * SCREEN, COLORDODGE, OVERLAY, SOFTLIGHT, HARDLIGHT, DIFFERENCE,
	 * EXCLUSION, HUE, SATURATION, COLOR or LUMINOSITY
	 */
	public static void blendMode(String b) {
		if(write == true) Blendmode.fxgBlendMode(b);
	}
	
	/**
	 * reset the Adobe Illustrator Object Mode to Normal.
	 */
	public static void noBlendMode() {
		if(write == true) Blendmode.fxgNoBlendMode();
	}
}

