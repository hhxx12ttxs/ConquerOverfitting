
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileInfo;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.io.InputStream;
import java.util.Date;
import java.util.Vector;
import javax.swing.JPanel;
import javax.vecmath.Point3d;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ilan
 */
public class JFijiPipe {
	/** the number of MIP cine frames = 16 */
	public static final int NUM_MIP = 16;
	static final int COLOR_BLUES = 0;
	static final int COLOR_GRAY = 1;
	static final int COLOR_INVERSE = 2;
	static final int COLOR_HOTIRON = 3;

	static final int DSP_AXIAL = 1;
	static final int DSP_CORONAL = 2;
	static final int DSP_SAGITAL = 3;
	static final int PERCENT_AXIS = 10;

	MemoryImageSource[] source = null;
	MemoryImageSource offSrc = null;
	// for Coronal and Sagital, leave JData class untouched, use JPipe
	MemoryImageSource[] corSrc = null;
	Vector<float []> rawFloatPix = null;
	Vector<short []> rawPixels = null;
	Vector<byte []> rawBytPix = null;
	int coronalSlice = -1, sagitalSlice = -1;

	JData data1;
	Image offscr = null;
	double fuseWidth = 50, fuseLevel = 500;
//	int fuseFactor = 120;
	Point3d pan = new Point3d();
	JFijiPipe srcPet = null;
	boolean dirtyFlg = false;
	int colorMode = -1, numDisp = 1, startFrm = 0, sliceType = 0, zoomIndx = 0;
	int offscrMode = 0, fusedColor = -1;
	double winWidth=900, winLevel=450, winSlope = 1.0, multYOff = 0, corSagFactor = 1.0, corSagOffset = 0.5;
	double oldWidth, oldLevel, oldCorWidth, oldCorLevel, zoom1 = 1.0, zoomX = 1.0, zoomY = 1.0;
	ColorModel cm = null, cm2 = null;
	Point[] imgPos = null;
	boolean useSrcPetWinLev = false;
	int indx = -1, cineIndx = 0;

	void LoadData( ImagePlus currImg) {
		data1 = new JData();
		data1.readData(currImg);
	}

	void LoadData( JFijiPipe scrPipe) {
		data1 = new JData();
		data1.readData(scrPipe);
	}

	public boolean LoadMIPData(JFijiPipe petPipe) {
		data1 = new JData();
		srcPet = petPipe;
		colorMode = -1;	// uninitialized
		return data1.setMIPData();
	}

	public JData CreateData1() {
		data1 = new JData();
		return data1;
	}

	boolean AutoFocus(int slice1) {
		if( data1.SUVfactor > 0) {
			data1.setSUV5();
			winLevel = winWidth/2;
			return true;
		}
		double currVal, maxVal, sum, sum2, stds, level;
		short buff1[] = null, currShort;
		float fltBuf[] = null;
		boolean fltFlg = false;
		int i, n, cntr1;
		int coef0 = data1.getCoefficent0();
		sum = sum2 = maxVal = stds = 0;
		// watch out for byte data. For bytes just set it to 1000, 500
		if( data1.pixByt != null) {
			winWidth = 1000;
			winLevel = 500;
			return true;
		}
		n = data1.width * data1.height;
		if( data1.pixFloat != null) {
			fltBuf = data1.pixFloat.elementAt(slice1);
			fltFlg = true;
		}
		else buff1 = data1.pixels.elementAt(slice1);
		for( i=cntr1=0; i<n; i++) {
			if( fltFlg) currVal = fltBuf[i];
			else {
				currShort = (short)(buff1[i] + coef0);
				currVal = currShort;
			}
			if( currVal <= 0) continue;
			if( currVal > maxVal) maxVal = currVal;
			sum += currVal;
			sum2 += currVal*currVal;
			cntr1++;		// this makes sure that the whole slice isn't zero
		}
		if( cntr1 <= 1) return false;	// must be more than 1 non zero point
		level = sum2/sum;
		for( i=0; i<n; i++) {
			if( fltFlg) currVal = fltBuf[i];
			else {
				currShort = (short)(buff1[i] + coef0);
				currVal = currShort;
			}
			if( currVal < 0) {
				currVal = 0;	// break point
			}
			if( currVal <= 0) continue;
			currVal = level - currVal;
			stds += currVal*currVal;
		}
		stds = Math.sqrt(stds/(cntr1-1));
		currVal = level + 3*stds;
		if( currVal > maxVal) currVal = maxVal;
		winWidth = currVal * winSlope * data1.getRescaleSlope(slice1);
		winLevel = winWidth / 2.0;
		return true;
	}

	/**
	 * Converts between a point in screen coordinates to data position.
	 * @param scrn1 the point on the display screen
	 * @param scale the scale factor on the display
	 * @param type axial, coronal - distorted or not
	 * @return point in the data itself
	 */
	protected Point scrn2Pos( Point scrn1, double scale, int type) {
		Point out1 = new Point();
		int origWidth, type1;
		origWidth = data1.width;
		double offY = multYOff * origWidth * scale;
		out1.x = (int) (scrn1.x/scale + 0.5);
		out1.y = (int) ((scrn1.y - offY)/scale + 0.5);
		if( zoom1*zoomX*data1.y2xFactor != 1.0) {
			type1 = type;
			if( type1 != 0) type1 = 2;
			double sclX = scale * getZoom(0);
			double sclY = scale * getZoom(type1);
			Point pan1 = getPan(type);
			out1.x = (int) (scrn1.x/sclX + 0.5) + pan1.x;
			if( out1.x < 0) out1.x = 0;
			if( out1.x >= origWidth) out1.x = origWidth - 1;
			out1.y = (int) ((scrn1.y - offY)/sclY +0.5) + pan1.y;
			if( out1.y < 0) out1.y = 0;
//			if( out1.y >= origHeight) out1.y = origHeight - 1;
		}
		return out1;
	}

	/**
	 * Converts between a point in the data to its position on the screen.
	 * @param pos1 point in the data
	 * @param scale the scale factor on the display
	 * @param type axial or coronal
	 * @return point location on the display
	 */
	protected Point pos2Scrn( Point pos1, double scale, int type) {
		Point out1 = new Point();
		int origWidth, type1;
		origWidth = data1.width;
		double offY = multYOff * origWidth * scale;
		out1.x = (int) (pos1.x*scale + 0.5);
		out1.y = (int) (pos1.y*scale + offY + 0.5);
		if( zoom1*zoomX*data1.y2xFactor != 1.0) {
			type1 = type;
			if( type1 != 0) type1 = 2;
			double sclX = scale * getZoom(0);
			double sclY = scale * getZoom(type1);
			Point pan1 = getPan(type);
			out1.x = (int) ((pos1.x - pan1.x)*sclX + 0.5);
			if( out1.x < 0) out1.x = 0;
			out1.y = (int) ((pos1.y - pan1.y)*sclY+ offY  + 0.5);
			if( out1.y < 0) out1.y = 0;
		}
		return out1;
	}

	Point getPan(int type) {
		int origWidth, origHeight, sizeX, sizeY, type1;
		double xpan, ypan;
		origWidth = data1.width;
		origHeight = data1.height;
		xpan = pan.x;
		ypan = pan.y;
		type1 = type;
		if( type1 == 1 || type1 == 2) {
			if( type1 == 2) origHeight = data1.numFrms;
			type1 = 2;
			ypan = pan.z;
			if( sliceType == DSP_SAGITAL) xpan = pan.y;
		}
		Point pan1 = new Point();
		sizeX = getZoomSize(origWidth, 0);
		sizeY = getZoomSize(origHeight, type);
		pan1.x = (int) (xpan*sizeX);
		pan1.y = (int) (ypan*sizeY);
		pan1.x += (origWidth - sizeX)/2;
		pan1.y += (origHeight - sizeY)/2;
		return pan1;
	}

	boolean updatePanValue( Point3d pan1, boolean force) {
		boolean retVal = force;
		int diff;
		if( !retVal) {
			diff = (int) (Math.abs((pan1.x - pan.x) * 1000) + 0.5);
			if( diff > 0) retVal = true;
			if( !retVal) {
				diff = (int) (Math.abs((pan1.y - pan.y) * 1000) + 0.5);
				if( diff > 0) retVal = true;
			}
			if( !retVal) {
				diff = (int) (Math.abs((pan1.z - pan.z) * 1000) + 0.5);
				if( diff > 0) retVal = true;
			}
			if( !retVal) return false;
		}
		pan = pan1;
		dirtyFlg = true;
		corSrc = null;
		return true;
	}

	int setZoom(int incr) {
		double zoomVals[] = new double[] {1., 1.1, 1.2, 1.3, 1.4, 1.5, 1.7, 2., 2.5, 3., 4., 6., 8.};
		if( incr == 0) return zoomIndx;
		int oldVal = zoomIndx;
		double maxZ;
		if( incr > 0) zoomIndx++;
		else {
			zoomIndx--;
			if( incr == -1000) zoomIndx = 0;
		}
		if( zoomIndx < 0) zoomIndx = 0;
		if( zoomIndx >= zoomVals.length) zoomIndx = zoomVals.length - 1;
		zoom1 = zoomVals[zoomIndx];
		// see if anything changed
		if( zoomIndx == oldVal) return zoomIndx;
		if( incr < 0) {
			maxZ = (zoom1 - 1.0)/(2*zoom1);
			if( pan.x > maxZ) pan.x = maxZ;
			if( pan.x < -maxZ) pan.x = -maxZ;
			if( pan.y > maxZ) pan.y = maxZ;
			if( pan.y < -maxZ) pan.y = -maxZ;
			pan.z = 0;
		}
		dirtyFlg = true;
		corSrc = null;
		return zoomIndx;
	}

	void drawCine(Graphics2D g, double scale, JPanel jDraw, boolean isCine) {
		if( isCine) {
			if( ++cineIndx >= data1.numFrms) cineIndx = 0;
		}

		Object hint;
		hint = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
		if( scale > 1.2) hint = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
		fillSource(cineIndx, 1);
		Image img = jDraw.createImage(source[cineIndx]);
		int w1, h1;
		w1 = (int) (scale * data1.width + 0.5);
		h1 = (int) (scale * data1.height * zoomX * data1.y2xFactor + 0.5);
		g.drawImage(img, 2*w1, 0, w1, h1, null);
	}

	/**
	 * Routine to draw both on screen and off screen images.
	 *
	 * This routine prepares at least the on screen image and the offscreen image as well
	 * if the class variable offscr != null.
	 * The offscreen has a different color scale, cm2. Its main purpose is for fusion images.
	 * Sometimes ONLY the fusion image is drawn. To take care of this case the class variable
	 * imgPos[0].x is set to less than 0,
	 * which is a sign NOT to display the unfused data.
	 *
	 * @param g graphics2D object from the paint routine
	 * @param scale adjusts between frame size and display size
	 * @param jDraw the parent frame
	 */

	protected void drawImages(Graphics2D g, double scale, JPanel jDraw) {
		if( imgPos == null) return;
		Object hint;
		hint = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
		if( scale > 1.2) hint = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Graphics2D osg = null;
		int i, w1, h1, x1, y1, yOff;
		w1 = data1.width;
		yOff = (int) (scale*multYOff*w1 + 0.5);
		w1 = (int) (scale * w1 + 0.5);
		h1 = (int) (scale * data1.height * zoomY + 0.5);
		if( cm2 != null) {
			offscr = jDraw.createImage(w1,h1);
			osg = (Graphics2D) offscr.getGraphics();
			osg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
//			osg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Image img1 = jDraw.createImage(offSrc);
			osg.drawImage(img1, 0,  0, w1, h1, null);
		}
		for( i=0; i<numDisp; i++) {
			x1 = imgPos[i].x;
			y1 = imgPos[i].y;
			Image img = jDraw.createImage(source[i+indx]);
			if( x1>= 0) g.drawImage(img, x1*w1, y1*h1 + yOff, w1, h1, null);
		}
	}

	protected void drawCorSagImages(Graphics2D g, double scale, JPanel jDraw, boolean corFlg) {
		if( imgPos == null) return;
		Object hint;
		hint = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
		if( scale > 1.2) hint = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
		Graphics2D osg = null;
		int i, w1, h1, x1, y1, yOff;
		w1 = data1.width;
//		yOff = (int) (scale*multYOff*w1 + 0.5);
		w1 = (int) (scale * w1 + 0.5);
		h1 = (int) (scale * data1.numFrms * zoom1 * zoomX * data1.y2xFactor + 0.5);	// not height
		if( cm2 != null) {
			offscr = jDraw.createImage(w1,h1);
			osg = (Graphics2D) offscr.getGraphics();
			osg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
			Image img1 = jDraw.createImage(offSrc);
			osg.drawImage(img1, 0,  0, w1, h1, null);
		}
		i = 1;
		if( corFlg) i = 0;
		x1 = imgPos[i+1].x;
		y1 = imgPos[i+1].y;
		Image img = jDraw.createImage(corSrc[i]);
		if( x1>= 0) g.drawImage(img, x1*w1, y1*h1, w1, h1, null);
	}

	protected void drawFusedImage(Graphics2D g, double scale, JFijiPipe other1, Point pos1, JPanel jDraw) {
		if( offscr == null) return;
		int h1, w = data1.width;
		int yOff = (int) ( scale*multYOff*w + 0.5);
		w = (int) (scale * w + 0.5);
		h1 = (int) (scale * data1.height * zoomY + 0.5);
		float factor;
		if( sliceType != DSP_AXIAL) h1 = (int) (scale * data1.numFrms * zoom1*  zoomX * data1.y2xFactor + 0.5);
		Composite old = g.getComposite();
//		factor = (float) (fuseLevel * fuseFactor / 100000);
//		if( factor > 1.0f) factor = 1.0f;
		g.setColor(Color.BLACK);
		g.fillRect(pos1.x*w, yOff, w, h1);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		g.drawImage(other1.offscr, pos1.x*w, yOff, w, h1, null);
//		factor = (float) ((1000 - fuseLevel) * fuseFactor / 100000);
		factor = (float) ((1000 - fuseLevel)  / 1000);
		if( factor > 1.0f) factor = 1.0f;
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, factor));
		g.drawImage(offscr, pos1.x*w, yOff, w, h1, null);
		g.setComposite(old);
	}

	int findCtPos( int petPos) {
		if( srcPet == null) return petPos;	// if called from Pet, return value unchanged
		if( imgPos == null) return -1;
		float petPosZ = srcPet.data1.zpos.elementAt(petPos);
		petPosZ += (float) (srcPet.data1.shitOffset * srcPet.data1.spacingBetweenSlices);
		int retVal = indx;
		if( retVal < 0) retVal = 0;
		float ctPosZ = data1.zpos.elementAt(retVal);
		float diff1, diffZ = ctPosZ - petPosZ;
		while( Math.abs(diffZ) > 0.1) {
			if( diffZ < 0) {
				diffZ = -diffZ;
				retVal--;
				if( retVal < 0) {
					retVal = 0;
					break;
				}
				ctPosZ = data1.zpos.elementAt(retVal);
				diff1 = ctPosZ - petPosZ;
				if( Math.abs(diff1) >= diffZ) {
					retVal++;	// set it back
					break;
				}
				diffZ = diff1;
			} else {
				retVal++;
				if( retVal >= data1.numFrms) {
					retVal = data1.numFrms - 1;
					break;
				}
				ctPosZ = data1.zpos.elementAt(retVal);
				diff1 = ctPosZ - petPosZ;
				if( Math.abs(diff1) >= diffZ) {
					retVal--;	// set it back
					break;
				}
				diffZ = diff1;
			}
		}
		return retVal;
	}

	void prepareFused( int color1) {
		if( color1 == fusedColor && cm2 != null) return;	// already done
		cm2 = null;
		if( offscrMode == 0) return;
		fusedColor = color1;
		cm2 = makeColorModel(color1);
	}

	void prepareFrame( int frmIndx, int type1, int color1, int src1) {
		startFrm = frmIndx;
		int type = 0;
		if( zoom1 > 1) type = 3;
		makeDspImage( color1, type);
	}

	void prepareCoronalSagital( int corSlice, int sagSlice) {
		UpdateCoronalSagital( corSlice, sagSlice);
		makeCorDspImage();
	}

	public void makeDspImage(int colorMod1, int type) {
		if (startFrm + numDisp > data1.numFrms) {
			startFrm = data1.numFrms - numDisp;
		}
		if (startFrm < 0) {
			startFrm = 0;
		}
		if (cm == null || colorMode != colorMod1) {
			colorMode = colorMod1;
			cm = makeColorModel(colorMode);
			corSrc = null;	// cause update
			oldWidth = -1;	// not equal to winWidth....
		}
		if( useSrcPetWinLev) {	// used for MIP and uncorrected PET
			double srcSUV = srcPet.data1.SUVfactor;
			// more than 10% change should cause it to be called at most once
			if( srcSUV > 0 && Math.abs(srcSUV -  data1.SUVfactor) > 0.1*srcSUV)
				data1.setSUVfactor(srcSUV);
			winWidth = srcPet.winWidth;
			winLevel = srcPet.winLevel;
		}
		if( source == null || source.length != data1.numFrms) dirtyFlg = true;
		if ( oldLevel != winLevel || oldWidth != winWidth || dirtyFlg) {
			dirtyFlg = false;
			oldLevel = winLevel;
			oldWidth = winWidth;
			source = new MemoryImageSource[data1.numFrms];
			indx = -1;
		}
		if (startFrm == indx) {
			return;	// all done
		}
		indx = startFrm;
		for (int j = 0; j < numDisp; j++) {
			fillSource(j+indx, type);
		}
	}

	void makeCorDspImage() {
		if ( oldCorLevel != winLevel || oldCorWidth != winWidth || corSrc == null) {
			oldCorLevel = winLevel;
			oldCorWidth = winWidth;
			corSrc = new MemoryImageSource[2];
		}
		if( coronalSlice >= 0) fillSource(0, 2);
		if( sagitalSlice >= 0)  fillSource(1, 2);
	}

	// type1 = 0 for standard axial display
	// type1 = 1 for mip type display
	// type1 = 2 for coronal sagital display (axial source)
	// type1 = 3 for axial under zoom (use all y direction space)
	private void fillSource(int indx1, int type1) {
		if( indx1 >= data1.numFrms) return;
		MemoryImageSource[] currSource	= source;
		if( type1 == 2) currSource = corSrc;
		if( currSource == null) return;
		if( currSource[indx1] != null && cm2==null) return;	// already calculated

		int i, j, k, i1, curr1, max255, sizeX, sizeY, off1=0, origWidth, origHeight;
		double scale, slope, xpan, ypan;
		short currShort;
		int min1;
		boolean badY;
		int coef0 = data1.getCoefficent0();
		Point pan1 = new Point();
		slope = winSlope * data1.getRescaleSlope(indx1);
		origWidth = data1.width;
		origHeight = data1.height;
		xpan = pan.x;
		ypan = pan.y;
		if( type1 == 2) {
			if( sliceType == DSP_SAGITAL) xpan = pan.y;
			ypan = pan.z;
			origHeight = data1.numFrms;
			slope = winSlope * data1.getMaxRescaleSlope();
			coef0 = 0;
		}
		sizeX = getZoomSize(origWidth, 0);
		sizeY = getZoomSize(origHeight, type1);
		pan1.x = (int) (xpan*sizeX);
		pan1.y = (int) (ypan*sizeY);
		pan1.x += (origWidth - sizeX)/2;
		pan1.y += (origHeight - sizeY)/2;
		short[] buff1;
		max255 = 255;
		if( useSrcPetWinLev) {
			winWidth = srcPet.winWidth;
			winLevel = srcPet.winLevel;
		}
		min1 = (int)((winLevel - winWidth/2 - data1.rescaleIntercept)/slope);
		scale = 256.0*slope/winWidth;
		byte[] buff = new byte[sizeX*sizeY];
		off1 = origWidth*pan1.y;
		switch( data1.depth) {
			case 32:
				float[] inFlt;
				inFlt = data1.pixFloat.elementAt(indx1);
				if( type1 == 2) inFlt = rawFloatPix.elementAt(indx1);
				for( j=k=0; j<sizeY; j++) {
					badY = false;
					if( j+pan1.y < 0 || j+pan1.y >= origHeight) badY = true;
					for( i=0; i<sizeX; i++) {
						i1 = i+pan1.x;
						if( badY || i1 < 0 || i1 >= origWidth) curr1 = 0;
						else {
							curr1 = (int)((inFlt[off1+i1] - min1) * scale);
						}
						if( curr1 > max255)
							curr1 = max255;
						if( curr1 < 0) curr1 = 0;
						buff[k++] = (byte) curr1;
					}
					off1 += origWidth;
				}
				break;

			case 8:
				byte[] inByt;
				inByt = data1.pixByt.elementAt(indx1);
				if( type1 == 2) inByt = rawBytPix.elementAt(indx1);
				for( j=k=0; j<sizeY; j++) {
					for( i=0; i<sizeX; i++) {
						curr1 = inByt[off1+i];
						if( curr1 < 0) curr1 = 256 + curr1;
						curr1 = (int)((curr1 - min1) * scale);
						if( curr1 > max255)
							curr1 = max255;
						if( curr1 < 0) curr1 = 0;
						buff[k++] = (byte) curr1;
					}
					off1 += origWidth;
				}
				break;

			default:
				buff1 = data1.pixels.elementAt(indx1);
				if( type1 == 2) buff1 = rawPixels.elementAt(indx1);
				for( j=k=0; j<sizeY; j++) {
					badY = false;
					if( j+pan1.y < 0 || j+pan1.y >= origHeight) badY = true;
					for( i=0; i<sizeX; i++) {
						i1 = i+pan1.x;
						if( badY || i1 < 0 || i1 >= origWidth) curr1 = 0;
						else {
							currShort = (short)(buff1[off1+i1] + coef0);
							curr1 = (int)((currShort - min1) * scale);
						}
						if( curr1 > max255)
							curr1 = max255;
						if( curr1 < 0) curr1 = 0;
						buff[k++] = (byte) curr1;
					}
					off1 += origWidth;
				}
		}
		currSource[indx1] = new MemoryImageSource(sizeX, sizeY, cm, buff, 0, sizeX);
		// if there is an offscreen memory, calculate it. It is the same as the above with different colors
		if( cm2 != null ) offSrc = new MemoryImageSource(sizeX, sizeY, cm2, buff, 0, sizeX);
	}

	int getZoomSize(int in1, int type) {
		int out1 = in1;
		if( type == 2) return out1;
		double zoomTmp = getZoom(type);
		if( zoomTmp == 1.0) return out1;
		out1 = (int) (in1 / zoomTmp + 0.5);
		if( type == 3) {
			out1 = (int) (in1 * zoomY / zoomTmp + 0.5);
			if( multYOff > 0.000001 ) out1 = in1;
		}
		return out1;
	}

	double getZoom(int type) {
		double zoomTmp = zoom1;	// case 1
		switch(type) {
			case 0:
			case 3:
				zoomTmp *= zoomX;
				break;

			case 2:
				zoomTmp *= zoomX * data1.y2xFactor;
		}
		return zoomTmp;
	}

	ColorModel makeColorModel(int mode) {
		int i;
		ColorModel cm1 = null;
		byte[] rLUT = new byte[256];
		byte[] gLUT = new byte[256];
		byte[] bLUT = new byte[256];
		byte[] buff = new byte[3 * 256];
		InputStream fl1;
		try {
			switch(mode) {
				case COLOR_BLUES:
					fl1 = getClass().getResourceAsStream("/resources/color1.dat");
					fl1.read(buff);
					fl1.close();
					for (i = 0; i < 256; i++) {
						rLUT[i] = buff[3*i];
						gLUT[i] = buff[3*i+1];
						bLUT[i] = buff[3*i+2];
					}
					break;

				case COLOR_HOTIRON:
					fl1 = getClass().getResourceAsStream("/resources/hotiron.dat");
					fl1.read(buff);
					fl1.close();
					for (i = 0; i < 256; i++) {
						rLUT[i] = buff[3*i];
						gLUT[i] = buff[3*i+1];
						bLUT[i] = buff[3*i+2];
					}
					break;

			case COLOR_INVERSE:
				for (i = 0; i < 256; i++) {
					rLUT[i] = (byte) (255-i);
					gLUT[i] = (byte) (255-i);
					bLUT[i] = (byte) (255-i);
				}
				break;

			case COLOR_GRAY:
			default:
				for (i = 0; i < 256; i++) {
					rLUT[i] = (byte) i;
					gLUT[i] = (byte) i;
					bLUT[i] = (byte) i;
				}
			}
			cm1 = new IndexColorModel(8, 256, rLUT, gLUT, bLUT);
		} catch (Exception e) { e.printStackTrace();}
		return cm1;
	}

	// x, y are not necessarily x,y but can be x,z or y,z
	void drawMarkers(Graphics2D g, int indx, int x, int y, double scale,
			boolean axial, boolean split) {
		g.setColor(Color.GREEN);
		int x0, y0, x1, y1;
		x0 = (int)(scale*(x +0.5));
		y0 = (int)(scale*(y + 0.5));
		int w1 = (int) (scale*data1.width + 0.5);
		int h1 = (int) (scale*data1.height + 0.5);
		if( !axial) {
			h1 = (int) (scale * data1.numFrms * zoomX * data1.y2xFactor + 0.5);
			y0 = (int) (scale * (y + 0.5) * data1.y2xFactor );
		}
		Point pt1 = imgPos[indx];
		x1 = w1*pt1.x;
		y1 = h1*pt1.y;
		drawMarkerSub(g, x1, y0+y1, true, indx, w1, x0+x1, split);
		drawMarkerSub(g, x0+x1, y1, false, indx, h1, y0+y1, split);
	}

	void drawMarkerSub(Graphics2D g, int x, int y, boolean horizontal, int indx,
			int w1, int other, boolean split) {
		Line2D ln1 = new Line2D.Double();
		Point[] pt1 = new Point[4];
		int i, w10;
		for( i=0; i<4; i++) pt1[i] = new Point();
		w10 = w1 * PERCENT_AXIS /100;
		if( horizontal) {
			pt1[0].x = x;
			pt1[1].x = x + w1;
			pt1[0].y = pt1[1].y = pt1[2].y = pt1[3].y = y;
			if( split) {
				pt1[3].x = pt1[1].x;
				pt1[1].x = x + w10;
				if( other <= pt1[1].x) pt1[1].x = other-1;
				pt1[2].x = x + w1 - w10;
				if( other >= pt1[2].x) pt1[2].x = other+1;
			}
		} else {
			pt1[0].x = pt1[1].x = pt1[2].x = pt1[3].x = x;
			pt1[0].y = y;
			pt1[1].y = y + w1;
			if( split) {
				pt1[3].y = pt1[1].y;
				pt1[1].y = y + w10;
				if( other <= pt1[1].y) pt1[1].y = other-1;
				pt1[2].y = y + w1 - w10;
				if( other >= pt1[2].y) pt1[2].y = other+1;
			}
		}
		ln1.setLine(pt1[0], pt1[1]);
		g.draw(ln1);
		if (split) {
			ln1.setLine(pt1[2], pt1[3]);
			g.draw(ln1);
		}
	}

	void UpdateCoronalSagital( int corSlice, int sagSlice) {
		int i, z1, off1, off2, wid1 = data1.width, heigh1 = data1.numFrms;
		int slic1;
		double scale;
		byte[] currByte = null, srcByt = null;
		short[] currShort = null, srcPix = null;
		float[] currFloat = null, srcFlt = null;
		short tmpShort;
		int coef0 = data1.getCoefficent0();
		switch( data1.depth) {
			case 32:
				if( rawFloatPix == null) {
					rawFloatPix = new Vector<float[]>();
					rawFloatPix.setSize(2);
				}
				break;

			case 8:
				if( rawBytPix == null) {
					rawBytPix = new Vector<byte[]>();
					rawBytPix.setSize(2);
				}
				break;

			default:
				if( rawPixels == null) {
					rawPixels = new Vector<short[]>();
					rawPixels.setSize(2);
				}
		}
		if( corSlice < 0 || corSrc == null) coronalSlice = -1;
		slic1 = (int) (corSagFactor * corSlice + corSagOffset);
		if( corSlice != coronalSlice && corSlice >= 0 && slic1 < wid1) {
			switch( data1.depth) {
				case 32:
					currFloat = new float[wid1*heigh1];
					break;

				case 8:
					currByte = new byte[wid1*heigh1];
					break;

				default:
					currShort = new short[wid1*heigh1];
			}
			coronalSlice = corSlice;
			corSrc = null;	// cause update
			for( z1=0; z1<heigh1; z1++) {
				off1 = slic1*wid1;
				off2 = z1*wid1;
				scale = data1.getRescaleSlope(z1)/data1.getMaxRescaleSlope();
				switch( data1.depth) {
					case 32:
						srcFlt = data1.pixFloat.elementAt(z1);
						for(i=0; i<wid1; i++) {
							currFloat[off2+i] = srcFlt[off1+i];
						}
						break;

					case 8:
						srcByt = data1.pixByt.elementAt(z1);
						for(i=0; i<wid1; i++) {
							currByte[off2+i] = limitByte(srcByt[off1+i], scale);
						}
						break;

					default:
						srcPix = data1.pixels.elementAt(z1);
						for( i=0; i<wid1; i++) {
							tmpShort = (short) (srcPix[off1+i] + coef0);
							currShort[off2+i] = limitShort(tmpShort, scale);
						}
				}
			}
			switch( data1.depth) {
				case 32:
					rawFloatPix.setElementAt(currFloat, 0);
					break;

				case 8:
					rawBytPix.setElementAt(currByte, 0);
					break;

				default:
					rawPixels.setElementAt(currShort, 0);
			}
		}

		if( sagSlice < 0 || corSrc == null) sagitalSlice = -1;
		slic1 = (int) (corSagFactor * sagSlice + corSagOffset);
		if( sagSlice != sagitalSlice && sagSlice >= 0 && slic1 < wid1) {
			switch( data1.depth) {
				case 32:
					currFloat = new float[wid1*heigh1];
					break;

				case 8:
					currByte = new byte[wid1*heigh1];
					break;

				default:
					currShort = new short[wid1*heigh1];
			}
			sagitalSlice = sagSlice;
			corSrc = null;	// cause update
			for( z1=0; z1<heigh1; z1++) {
				off1 = slic1;
				off2 = z1*wid1;
				scale = data1.getRescaleSlope(z1)/data1.getMaxRescaleSlope();
				switch( data1.depth) {
					case 32:
						srcFlt = data1.pixFloat.elementAt(z1);
						for(i=0; i<wid1; i++) {
							currFloat[off2+i] = srcFlt[off1+i*wid1];
						}
						break;

					case 8:
						srcByt = data1.pixByt.elementAt(z1);
						for(i=0; i<wid1; i++) {
							currByte[off2+i] = limitByte( srcByt[off1+i*wid1], scale);
						}
						break;

					default:
						srcPix = data1.pixels.elementAt(z1);
						for( i=0; i<wid1; i++) {
							tmpShort = (short) (srcPix[off1+i*wid1] + coef0);
							currShort[off2+i] = limitShort(tmpShort, scale);
						}
				}
			}
			switch( data1.depth) {
				case 32:
					rawFloatPix.setElementAt(currFloat, 1);
					break;

				case 8:
					rawBytPix.setElementAt(currByte, 1);
					break;

				default:
					rawPixels.setElementAt(currShort, 1);
			}
		}
	}

	byte limitByte( byte inVal, double scale) {
		int tmpi = inVal;
		if( tmpi < 0) tmpi += 256;
		double tmp = tmpi * scale;
		if( tmp > 255) tmp = 255;
		return (byte) tmp;
	}

	short limitShort( short inVal, double scale) {
		double tmp = inVal * scale;
		if( tmp > 32767) tmp = 32767;
		return (short) tmp;
	}

	/**
	 * This is the structure which is returned when a line is requested.
	 * A line will be requested when building coronal and sagital slices
	 * It will also be requested when calculating the CT value and SUV.
	 */
	protected class lineEntry {
		boolean goodData = false;
		int	angle = 0;			// 0=coronal, 270= sagital
		int depth = 0;			// this is "y" for coronal and "x" for sagital
		short[]	pixels = null;	// this is the raw line data
		float[] pixFloat = null;	// or this for floating point data
		float[] pixelSpacing = null;
		double slope = 1.0;
		double maxSlope = 1.0;
		double SUVfactor = 0;	// zero means no SUV
	}

	/**
	 * An inner class for storing details of the image stack
	 */
	protected class JData {
		int seriesType, numFrms=0, width, height, depth, maxPixel;
		int shitOffset = 0;	// for matching frames to CT
		int SOPclass, fileFormat = FileInfo.UNKNOWN;
		Vector<short []> pixels = null;
		Vector<byte []> pixByt = null;
		Vector<float []> pixFloat = null;
		Vector<Float> zpos = null;
		Vector<Double>rescaleSlope = null;
		private int maxPosition = -1;
		float pixelSpacing[] = null;
		Date serTime, injectionTime;
		double maxVal, minVal, grandMax, spacingBetweenSlices, cos1=1.0, sin1=0.0;
		double halflife, patWeight, patHeight, totalDose, rescaleIntercept = 0, SUVfactor = 0.0;
		double sliderSUVMax = 1000., MIPslope = 1.0;
		double expFac[] = null, y2xFactor = 1.0, y2XMip = 1.0;
		double coef[] = null;
		String metaData = null;

		/**
		 * Takes an ImagePlus object and parses the Dicom information necessary
		 * for the JFijiPipe. It then stores a reference to the binary data and the metaData.
		 * @param img1
		 */
		void readData(ImagePlus img1) {
			int i;
			double x;
			boolean spectFlg = false;	// SPECT or NM data has all slices in 1 file
			float zpos1=0, patPos[];
			Object pix1;
			fileFormat = img1.getOriginalFileInfo().fileFormat;
			numFrms = img1.getStackSize();
			width = img1.getStack().getWidth();
			height = img1.getStack().getHeight();
			depth = img1.getBitDepth();
			coef  = img1.getCalibration().getCoefficients();
			rescaleSlope = null;
			String meta = ChoosePetCt.getMeta(1, img1);
			if( !getMetaData(meta)) return;
			i = img1.getOriginalFileInfo().nImages;
			if( i > 1 ) spectFlg = true;
			if( !spectFlg && depth < 32) rescaleSlope = new Vector<Double>();
			for( i=1; i<=numFrms; i++) {
				pix1 = img1.getStack().getPixels(i);
				switch( depth) {
					case 32:
						pixFloat.add((float[])pix1);
						break;

					case 8:
						pixByt.add((byte[]) pix1);
						break;

					default:
						pixels.add((short[]) pix1);
				}
				meta = ChoosePetCt.getMeta(i, img1);
				if( meta == null) continue;
				if( !spectFlg || i == 1) {
					patPos = ChoosePetCt.parseMultFloat(ChoosePetCt.getDicomValue(meta, "0020,0032"));
					if( patPos!=null) zpos1 = patPos[2];
				}
				zpos.add(zpos1);
				zpos1 += spacingBetweenSlices;
				if( rescaleSlope == null) continue;
				x = ChoosePetCt.parseDouble(ChoosePetCt.getDicomValue(meta, "0028,1053"));
				rescaleSlope.add(x);
			}
			setMaxAndSort();
		}

		void readData(JFijiPipe srcPipe) {
			int i;
			double x;
			short[] pix1;
			byte[] pixByt1;
			float[] pixFlt1;
			float zpos1;
			JData srcData = srcPipe.data1;
			fileFormat = srcData.fileFormat;
			numFrms = srcData.numFrms;
			width = srcData.width;
			height = srcData.height;
			depth = srcData.depth;
			coef = srcData.coef;
			rescaleSlope = null;
			if( !getMetaData(srcData.metaData)) return;
			if( srcData.rescaleSlope != null) rescaleSlope = new Vector<Double>();
			for( i=0; i<numFrms; i++) {
				switch( depth) {
					case 32:
						pixFlt1 = srcData.pixFloat.elementAt(i);
						pixFloat.add(pixFlt1);
						break;

					case 8:
						pixByt1 = srcData.pixByt.elementAt(i);
						pixByt.add(pixByt1);
						break;

					default:
						pix1 = srcData.pixels.elementAt(i);
						pixels.add(pix1);
				}
				zpos1 = srcData.zpos.elementAt(i);
				zpos.add(zpos1);
				if( rescaleSlope == null) continue;
				x = srcData.rescaleSlope.elementAt(i);
				rescaleSlope.add(x);
			}
			setMaxAndSort();
			setSUVfactor(srcPipe.data1.SUVfactor);
		}

		boolean getMetaData(String meta) {
			String tmp1;
			if( meta == null) return false;
			metaData = meta;
			seriesType = ChoosePetCt.getImageType(meta);
			if( seriesType == ChoosePetCt.seriesUnknown) return false;
			pixelSpacing = ChoosePetCt.parseMultFloat(ChoosePetCt.getDicomValue(meta, "0028,0030"));
			SOPclass = ChoosePetCt.getSOPClass(ChoosePetCt.getDicomValue( meta, "0008,0016"));
			tmp1 = ChoosePetCt.getDicomValue(meta, "0008,0020");	// study date
			serTime = ChoosePetCt.getDateTime(tmp1, ChoosePetCt.getDicomValue(meta, "0008,0031"));
			injectionTime = ChoosePetCt.getDateTime(tmp1, ChoosePetCt.getDicomValue(meta, "0018,1072"));
			totalDose = ChoosePetCt.parseDouble(ChoosePetCt.getDicomValue(meta, "0018,1074"));
			halflife = ChoosePetCt.parseDouble(ChoosePetCt.getDicomValue(meta, "0018,1075"));
			patHeight = ChoosePetCt.parseDouble(ChoosePetCt.getDicomValue(meta, "0010,1020"));
			patWeight = ChoosePetCt.parseDouble(ChoosePetCt.getDicomValue(meta, "0010,1030"));
			zpos = new Vector<Float>();
			spacingBetweenSlices = ChoosePetCt.parseDouble(ChoosePetCt.getDicomValue(meta, "0018,0088"));
			if( spacingBetweenSlices == 0)
				spacingBetweenSlices = -ChoosePetCt.parseDouble(ChoosePetCt.getDicomValue(meta, "0018,0050"));
			switch( depth) {
				case 32:
					pixFloat = new Vector<float []>();
					break;

				case 8:
					pixByt = new Vector<byte []>();
					break;

				default:
					pixels = new Vector<short []>();	// 16 bits
			}
			return true;
		}

		/**
		 *  The routine which set the variable max and sorts the data.
		 * It is called for corrected and uncorrected PET and CT but NOT for MIP data.
		 * It generates vectors for storing the short pixel data, OR the byte pixByt data.
		 * There are vectors for the files, the rescale slope and zpos (which are all now sorted).
		 * Finally it checks that 10 * mean data value < max data value.
		 */
		protected void setMaxAndSort() {
			int[] sortVect = null;
			float[] pixFlt1;
			short[] pix1;
			byte[] pixByt1;
			boolean dirty = false;
			double slope1, currDiff, minDiff;
			int i, localMaxPos, j0, j1, n = zpos.size();
			float[] zpos1;
			double[] maxSliceVals = null;
			float ztmp, zval;
			Vector<float []> oldPixFloat = pixFloat;
			Vector<short []> oldPixels = pixels;
			Vector<byte []> oldPixByt = pixByt;
			Vector<Float> oldZpos = zpos;
			Vector<Double> oldRescaleSlope = rescaleSlope;
			if( numFrms>1) {
				sortVect = new int[numFrms];
				zpos1 = new float[numFrms];
				for( i=0; i<numFrms; i++) {
					sortVect[i] = i;
					zpos1[i] = zpos.elementAt(i);
				}
				i=0;
				while( i<numFrms-1) {
					j0 = sortVect[i];
					j1 = sortVect[i+1];
					zval = zpos1[i];
					ztmp = zpos1[i+1];
					if( zval < ztmp) {
						dirty = true;
						sortVect[i] = j1;
						sortVect[i+1] = j0;
						zpos1[i] = ztmp;
						zpos1[i+1] = zval;
						i--;
						if( i<0) i=0;
					}
					else i++;
				}
				if( dirty) {
					if( pixFloat != null) pixFloat = new Vector<float []>();
					if( pixels != null) pixels = new Vector<short []>();
					if( pixByt != null) pixByt = new Vector<byte []>();
					zpos = new Vector<Float>();
					if( rescaleSlope != null) rescaleSlope = new Vector<Double>();
					for( i=0; i<numFrms; i++) {
						j0 = sortVect[i];
						zval = oldZpos.elementAt(j0);
						zpos.add(zval);
						if( pixFloat != null) {
							pixFlt1 = oldPixFloat.elementAt(j0);
							pixFloat.add(pixFlt1);
						}
						if( pixels != null) {
							pix1 = oldPixels.elementAt(j0);
							pixels.add(pix1);
						}
						if( pixByt != null) {
							pixByt1 = oldPixByt.elementAt(j0);
							pixByt.add(pixByt1);
						}
						if( rescaleSlope != null) {
							slope1 = oldRescaleSlope.elementAt(j0);
							rescaleSlope.add(slope1);
						}
					}
				}
				zval = Math.abs(zpos.elementAt(1) - zpos.elementAt(0));
				if( zval > 0) y2xFactor = zval / pixelSpacing[0];
				if(y2xFactor > 0.9 && y2xFactor < 1.1) y2xFactor = 1.0;
			}

			// kill rescaleSlope if n==1
			if( n<=1) rescaleSlope = null;

			// now the data is sorted, so find its maximum value
			double tmpd, currSlice, mean, currDbl1, maxDbl;
			int currVal1, n1, maxi, coef0;
			short currShort;
			coef0 = getCoefficent0();
			grandMax = 0;
			maxPixel = 0;
			maxSliceVals = new double[numFrms];
			for( i=0; i<numFrms; i++) {
				maxi =  0;
				maxDbl = 0;
				if( pixels != null) {
					pix1 = pixels.elementAt(i);
					n1 = pix1.length;
					for( j1=0; j1<n1; j1++) {
						currVal1 = pix1[j1];
						currShort = (short) (currVal1 + coef0);
						if( currShort > maxi) maxi = currShort;
					}
				}
				if( pixByt != null) {
					pixByt1 = pixByt.elementAt(i);
					n1 = pixByt1.length;
					for( j1=0; j1<n1; j1++) {
						currVal1 = pixByt1[j1];
						if( currVal1 < 0) {
							currVal1 = 256 + currVal1;
						}
						if( currVal1 > maxi) maxi = currVal1;
					}
				}
				if( pixFloat != null) {
					pixFlt1 = pixFloat.elementAt(i);
					n1 = pixFlt1.length;
					for( j1=0; j1<n1; j1++) {
						currDbl1 = pixFlt1[j1];
						if( currDbl1 > maxDbl) maxDbl = currDbl1;
					}
				}
				if( maxi > maxPixel) maxPixel = maxi;
				tmpd = getRescaleSlope(i)*maxi;
				if( pixFloat != null) tmpd = maxDbl;
				maxSliceVals[i] = tmpd;
				if( tmpd > grandMax) grandMax = tmpd;
			}
			mean = 0;
			maxVal = minVal = 0;
			localMaxPos = -1;
			currSlice = 0;
			for( i=0; i<numFrms; i++) {
				currSlice = maxSliceVals[i];
				mean += currSlice;
				if( currSlice > maxVal) {
					maxVal = currSlice;
					localMaxPos = i;
				}
			}
			mean /= numFrms;

			// A new problem has come up in measuring a bag of shit.
			// Since there is no human body in this measurement, there is nothing other
			// than the bag of shit. This is handled by ingnoring slices = 0.
			if(mean*10 < maxVal) {
				mean = removeOutliers(maxSliceVals);
				minDiff = Math.abs(maxSliceVals[0] - mean);
				maxPosition = 0;
				for( i=1; i<numFrms; i++) {
					currDiff = Math.abs(maxSliceVals[i] - mean);
					if( currDiff < minDiff) {
						minDiff = currDiff;
						maxPosition = i;
					}
				}
				maxVal = mean;
			}
			if( maxPosition < 0) maxPosition = localMaxPos;
			if( SOPclass == ChoosePetCt.SOPClassTypeCT) minVal = -1000;
			else winSlope = sliderSUVMax / maxVal;
			dirtyFlg = true;
		}

		int getCoefficent0() {
			int coef0 = 0;
			if( coef != null) coef0 = (int) coef[0];
			return coef0;
		}

		void setSUVfactor( double newSUVfactor) {
			SUVfactor = newSUVfactor;
			// our bag of shit gives more problems. The sliderSUVMax = 0.03
			// if the sliderSUVMax will be <= 1.0, kill the SUV (maybe we need it?)
			if( SUVfactor > 0) { // update
				double max1 = maxVal * MIPslope;
				if( max1/SUVfactor <= 1.0) {
					SUVfactor = 0;
					return;
				}
				sliderSUVMax = max1 / SUVfactor;
				winSlope = sliderSUVMax / max1;	// = 1/SUVfactor
				setSUV5();
				winLevel = winWidth/2;
				dirtyFlg = true;
			}
		}

		void setSUV5() {
			winWidth = 5.0;
			if(winWidth <= sliderSUVMax) return;	// nothing to do
			int i = (int) (sliderSUVMax * 10 + 0.5);
			if(( i & 1) == 1) i++;	// make it an even number
			winWidth = i / 10.0;
		}

		/**
		 * This routine is called when the maximum slice > 10 * mean slice value.
		 *
		 * There is a problem measuring a bag of shit in that many slices are zero.
		 * Thus the mean is corrected to include only non zero slices.
		 * If the max is still an outlier, the max slice is zeroed (so as to be ignored)
		 * and the calculation is repeated, until outliers are eliminated.
		 *
		 * @param maxSliceVals list of slice maxima
		 * @return corrected maximum slice value
		 */
		protected double removeOutliers( double[] maxSliceVals) {
			double localMax = 0;
			double currSum, currVal;
			int i, n, nNonZero, maxPos;
			n = maxSliceVals.length;
			while(true) {
				localMax = currSum = 0;
				maxPos = -1;
				for( i=nNonZero=0; i<n; i++) {
					currVal = maxSliceVals[i];
					if( currVal <= 0) continue;	// don't count zeros
					nNonZero++;
					if( localMax < currVal) {
						localMax = currVal;
						maxPos = i;
					}
					currSum += currVal;
				}
				currVal = currSum / nNonZero;
				if( localMax < currVal*10) break;
				if( maxPos < 0) break;	// sanity check
				maxSliceVals[maxPos] = 0;	// elimiate highest point and try again
			}
			return localMax;
		}

		double getRescaleSlope( int indx) {
			double slope1 = MIPslope;	// MIPslope = 1.0 for everything but MIP and uncorrected
			int i;
			if( rescaleSlope != null && rescaleSlope.size() > indx) {
				// watch out, MRI data has no rescaleSlope. Detect this when everything = 0
				for(i=0; i<rescaleSlope.size(); i++) {
					if( rescaleSlope.elementAt(i) > 0) break;
				}
				if( i < rescaleSlope.size()) {
					slope1 = rescaleSlope.elementAt(indx) * MIPslope;
				} else {
					rescaleSlope = null;	// kill it
				}
			}
			return slope1;
		}

		double getMaxRescaleSlope() {
			double tmp1, slope1 = MIPslope;
			int i, n;
			if( rescaleSlope != null) {
				n = rescaleSlope.size();
				if( maxPosition >=0 && maxPosition < n)
					return rescaleSlope.elementAt(maxPosition) * MIPslope;
				for( i=0; i<n; i++) {
					tmp1 = rescaleSlope.elementAt(i);
					if( i==0 || tmp1 > slope1) slope1 = tmp1;
				}
				slope1 *= MIPslope;
			}
			return slope1;
		}

		/**
		 * Utility routine for getting a line of data for SUV, coronal or sagital slices.
		 *
		 * @param angle in degrees, 0 for SUV or coronal, 270 for sagital
		 * @param depth y coordinate for coronal, x for sagital
		 * @param sliceNum z coordinate, which is the axial slice number
		 * @return lineEntry structure, with line + extra information
		 */
		public lineEntry getLineOfData(int angle, int depth, int sliceNum) {
			lineEntry ret1 = new lineEntry();
			short sliceBuf[], currShort;
			float sliceFlt[];
			int i, j, coef0, start1=0, step = 1, width1 = width;
			if( depth < 0) return ret1;
			switch( angle) {
				case 0:
					start1 = width*depth;
					break;

				case 180:
					start1 = width-1;
					step = -1;
					break;

				case 90:
					break;

				default:
					return ret1;
			}
			if( angle == 90 || angle == 270) width1 = height;
			j = start1;
			if( pixFloat != null) {
				sliceFlt = pixFloat.elementAt(sliceNum);
				ret1.pixFloat = new float[width1];
				for( i=0; i < width1; i++) {
					ret1.pixFloat[i] = sliceFlt[j];
					j += step;
				}
			} else {
				sliceBuf = pixels.elementAt(sliceNum);
				ret1.pixels = new short[width1];
				coef0 = getCoefficent0();
				for( i=0; i < width1; i++) {
					currShort = (short)(sliceBuf[j]+coef0);
					ret1.pixels[i] = currShort;
					j += step;
				}
			}
			ret1.SUVfactor = SUVfactor;
			ret1.angle = angle;
			ret1.depth = depth;
			ret1.maxSlope = getMaxRescaleSlope();
			ret1.slope = getRescaleSlope(sliceNum);
			ret1.pixelSpacing = pixelSpacing;
			ret1.goodData = true;
			return ret1;
		}

		protected boolean setMIPData() {
			int ang1, x1, off1, currMax;
			short [] currSlice;
			width = srcPet.data1.width;
			height = srcPet.data1.numFrms;
			y2xFactor = srcPet.data1.y2xFactor;
			if( srcPet.data1.depth < 16) return false;	// something is wrong
			depth = 16;
			numFrms = 0;
			grandMax = srcPet.data1.maxPixel;
			if( srcPet.data1.depth == 32) grandMax = srcPet.data1.maxVal;
			maxVal = 0;
			pixels = new Vector<short []>();
			for( ang1 =0; ang1 < NUM_MIP; ang1++) {
				setCosSin( ang1);
				currSlice = new short[width*height];
				for( x1 = off1 = 0; x1 < width; x1++) {
					currMax = getMipLocation(x1, -1, null, currSlice, off1++);
					if( currMax > maxVal) maxVal = currMax;
				}
				IJ.showStatus(ang1+1+"/"+NUM_MIP);
				IJ.showProgress(ang1, NUM_MIP);
				pixels.add(currSlice);
				numFrms++;
				winSlope = sliderSUVMax / (maxVal*MIPslope);
			}
			return true;
		}

		/**
		 * Sets cos1 and sin1 values for the angle defined by indx.
		 * This needs to be called before getMipLocation. It also does a one time
		 * calculation of expFac which is the attenuation factor used along the ray.
		 * @param indx the angular value, angle (in degrees) = 360*indx/NUM_MIP
		 */

		public void setCosSin(int indx) {
			double angl1 = (2.0*Math.PI*indx)/NUM_MIP;
			cos1 = Math.cos(angl1);
			sin1 = Math.sin(angl1);
			if( expFac == null) {
				expFac = new double[width];
				double scale = 2.0*Math.log(2);
				double scal2 = 0.7;
				int i, wid2 = width/2;
				for( i=0; i<width; i++)
					expFac[i] = scal2*Math.exp(scale*(wid2-i)/width);
			}
		}

		/**
		 * Get the attenuated highest pixel value along a ray at x1, z1.
		 * This routine is called in 2 different methods, z1 = -1 is used to calculate the slice.
		 * z1 >= 0 is used to measure the maximum point after the cine has been displayed
		 * and the user clicked on the MIP image. Note that no separate image is stored
		 * holding the maximum value positions. The reason is that it is unjustified from
		 * the point of view of speed. The user will click infrequently on a single point.
		 * It makes no sense to calculate ALL the positions up front, so each time the
		 * calculation for the chosen point is done again with no sacrifice in speed.
		 * The pixOut is used ONLY for display. The original data is used when the
		 * user asks for a MIP value. Only the position where the user clicked (and the
		 * angle, normally front) is used and NOT pixOut.
		 * @param x1 position, 90 degrees from which is the ray itself
		 * @param z1 = -1 for calculation of the cine, or >=0 for calculation of a point.
		 * @param retLoc null while calcuting cine, otherwise receives the position
		 * @param pixOut where the output is stored (null for z1 >= 0).
		 * @param offst offset into pixOut, dependent mainly on the angle
		 * @return the value highest attenuated pixel value
		 */

		public int getMipLocation(int x1, int z1, Point retLoc, short pixOut[], int offst) {
			double xin, yin, scale1, scale2=1, currValDbl;
			boolean fltFlg = false;
			int zlo, y1, xnew, ynew, currVal, off1, retVal, coef0;
			short currShort;
			short [] currSlice;
			float [] fltSlice;
			int width2 = width/2;
			xin = x1 - width2 + 0.5;
			retVal = 0;
			if( z1 < 0) {
				for( zlo = 0; zlo < height; zlo++) pixOut[zlo*width+offst] = 0;
				zlo = 0;
			} else {	// do a single slice
				zlo = z1;
				retLoc.x = 0;
				retLoc.y = 0;
				if( z1 < 0 || z1 >= height) return 0;	// out of bounds
			}
			MIPslope = srcPet.data1.getMaxRescaleSlope();
			if( srcPet.data1.depth == 32) {
				fltFlg = true;
				MIPslope = srcPet.data1.maxVal / 32767;
				scale2 = 1 / MIPslope;
			}
			coef0 = srcPet.data1.getCoefficent0();
			for( yin = -width2 + 0.5; yin < width2; yin += 1.0) {
				xnew = (int)( xin*cos1 + yin*sin1 + width2);
				if( xnew < 0 || xnew >= width) continue;
				ynew = (int)(-xin*sin1 + yin*cos1 + width2);
				if( ynew < 0 || ynew >= width) continue;
				y1 = (int) (yin + width2);
				scale1 = expFac[y1];
				if( z1 < 0) for( zlo = 0; zlo < height; zlo++) {
					off1 = zlo*width + offst;
					// if we get a measurable delay, in c++ it helped to move the next line
					if( fltFlg) {
						fltSlice = srcPet.data1.pixFloat.elementAt(zlo);
						currValDbl = scale1 * scale2 * fltSlice[xnew + ynew*width];
					} else {
						scale2 = scale1* srcPet.data1.getRescaleSlope(zlo)/MIPslope;
						currSlice = srcPet.data1.pixels.elementAt(zlo);
						currShort = (short) (currSlice[xnew + ynew*width] + coef0);
						currValDbl = scale2*currShort;
					}
					currVal = (int)currValDbl;	// local data
					if( currVal > pixOut[off1]) {
						if( currValDbl > 32767) currVal = 32767;
						pixOut[off1] = (short) currVal;
						if( currVal > retVal) retVal = currVal;
					}
				}
				else {	// single slice
					if( fltFlg) {
						fltSlice = srcPet.data1.pixFloat.elementAt(zlo);
						currVal = (int) (scale1 * scale2 * fltSlice[xnew + ynew*width]);
					} else {
						currSlice = srcPet.data1.pixels.elementAt(zlo);
						currShort = (short) (currSlice[xnew + ynew*width] + coef0);
						currVal = (int)(scale1*currShort);	// local data
					}
					if( currVal > retVal) {
						retVal = currVal;
						retLoc.x = xnew;
						retLoc.y = ynew;
					}
				}
			}
			return retVal;
		}
	}
}

