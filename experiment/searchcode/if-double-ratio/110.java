/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2006 Prodevelop and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib??ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *   +34 963862235
 *   gvsig@gva.es
 *   www.gvsig.gva.es
 *
 *    or
 *
 *   Prodevelop Integraci?n de Tecnolog?as SL
 *   Conde Salvatierra de ?lava , 34-10
 *   46004 Valencia
 *   Spain
 *
 *   +34 963 510 612
 *   +34 963 510 968
 *   gis@prodevelop.es
 *   http://www.prodevelop.es
 */

/**
 *
 */
package org.gvsig.remoteClient.arcims.styling.symbols;

import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.core.styles.ILineStyle;
import com.iver.cit.gvsig.fmap.core.styles.SimpleLineStyle;
import com.iver.cit.gvsig.fmap.core.symbols.ILineSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.IMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleFillSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleLineSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleTextSymbol;
import com.iver.cit.gvsig.fmap.core.v02.FConstant;

import com.iver.cit.gvsig.fmap.rendering.styling.FStyle2D;

import com.iver.utiles.ImageFilter;

import org.apache.log4j.Logger;

import org.gvsig.remoteClient.arcims.utils.ArcImsDownloadUtils;
import org.gvsig.symbology.fmap.symbols.GradientFillSymbol;
import org.gvsig.symbology.fmap.symbols.PictureFillSymbol;
import org.gvsig.symbology.fmap.symbols.PictureMarkerSymbol;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;

import java.util.ArrayList;

import javax.swing.ImageIcon;


/**
 * Factory class to produce FSymbol's from ArcIMS Symbol defintions
 * @author jsanz
 *
 */
public class ArcImsFSymbolFactory {
    private static Logger logger = Logger.getLogger(ArcImsFSymbolFactory.class.getName());
    public static char ds = '.';
    private static ArrayList initialColors = new ArrayList();

    static {
        initialColors.add(new Color(179, 226, 205));
        initialColors.add(new Color(253, 205, 172));
        initialColors.add(new Color(203, 213, 232));
        initialColors.add(new Color(230, 245, 201));
        initialColors.add(new Color(141, 211, 199));
        initialColors.add(new Color(255, 255, 179));
        initialColors.add(new Color(190, 186, 218));
        initialColors.add(new Color(251, 128, 114));
        initialColors.add(new Color(128, 177, 211));
    }

    //This code is copy&paste from com.iver.cit.gvsig.fmap.core.v02.FSymbolFactory;
    static int w;

    //This code is copy&paste from com.iver.cit.gvsig.fmap.core.v02.FSymbolFactory;
    static int h;

    /**
     * Returns a point FSymbol
     * @param simb
     * @return
     */
    protected static ISymbol getFSymbol(ArcImsSimpleMarkerSymbol simb) {
        Color mcolor = SymbolUtils.getColor(simb.getColor(), 1);
        int pointWidth = Integer.parseInt(simb.getWidth());
        int pointStyle = getPointType(simb.getType());

        SimpleMarkerSymbol mSimb = new SimpleMarkerSymbol();
        
        mSimb.setColor(mcolor);
        mSimb.setSize(pointWidth);
        mSimb.setStyle(SimpleMarkerSymbol.CIRCLE_STYLE);
        logger.info("Getting ArcIMS simple point symbol");

        return mSimb;
    }

    /**
     * Returns a point ISymbol with a remote image
     * @param symb
     * @return
     */
    protected static ISymbol getFSymbol(RasterMarkerSymbol symb) {
        String surl = symb.getUrl();

        // Get the icon size
        float size = 0;

        if (symb.getSize() != null) {
            String[] tams = symb.getSize().split(",");
            int ntams = tams.length;

            if (ntams != 0) {
                for (int i = 0; i < ntams; i++)
                    size += Float.parseFloat(tams[i]); // Sums different sizes

                size = size / ntams; // Get the mean
            }
        } else {
            size = 15; // Default size
        }

        // Get the icon file
        File img = getIconFile(surl);

        // If is an image, we build the symbol
        ImageFilter filter = new ImageFilter();

        if (filter.accept(img)) {
        	
        	URL theUri;
        	PictureMarkerSymbol mSimb = null;
			try {
				theUri = img.toURI().toURL();
	            mSimb = new PictureMarkerSymbol();
	            mSimb.setImage(theUri);
			} catch (Exception e) {
				logger.error("While getting image symbol: " + e.getMessage());
				return getDefaultFSymbol(FConstant.SYMBOL_TYPE_POINT);
			}
			
            mSimb.setSize((int) size);
            logger.info("Getting ArcIMS raster marker point symbol");
            return mSimb;
        } else {
            logger.info("Getting random point symbol");
            return getDefaultFSymbol(FConstant.SYMBOL_TYPE_POINT);
        }
    }

    /**
     * Returns a line FSymbol
     * @param simb
     * @return
     */
    protected static SimpleLineSymbol getFSymbol(ArcImsSimpleLineSymbol simb) {
        String sTrans = simb.getTransparency();

        if (ds != '.') {
            sTrans = sTrans.replace(ds, '.');
        }

        float trans = Float.parseFloat(sTrans);

        Color mcolor = SymbolUtils.getColor(simb.getColor(), trans);
        int lineWidth = Integer.parseInt(simb.getWidth());
        int intCapType = getCapType(simb.getCaptype());
        int intJoin = getJoinType(simb.getJointype());
        String linePattern = getLinePattern(simb.getType());

//        BasicStroke bStroke = new BasicStroke(lineWidth, intCapType, intJoin,
//                1.0f, FSymbol.toArray(linePattern, lineWidth), 0);

        SimpleLineSymbol theSymbol = new SimpleLineSymbol();
        
        theSymbol.setLineColor(mcolor);
        SimpleLineStyle linestyle = new SimpleLineStyle(
        		Float.parseFloat(simb.getWidth()),
        		intCapType, intJoin, 1.0f, ArcImsSimpleLineSymbol.toArray(linePattern, lineWidth), 0);
        theSymbol.setLineStyle(linestyle);
        		 		
        logger.info("Getting ArcIMS simple line symbol");
        return theSymbol;
    }

    /**
     * The hasline symbol seems as a railroad, as gvSIG doesn't supports
     * railroad symbols, this method will return a simple line with the
     * same color and width
     * @param symbol
     * @return
     */
    protected static ISymbol getFSymbol(HashLineSymbol symbol) {
        ArcImsSimpleLineSymbol line = new ArcImsSimpleLineSymbol();
        line.setColor(symbol.getColor());
        line.setWidth(symbol.getLinethickness());
        line.setType(SymbolUtils.LINE_TYPE_SOLID);

        logger.info("Getting ArcIMS hashline as a simple line symbol");

        return line.getFSymbol();
    }

    /**
     * Returns a polygon ISymbol with proper fill
     * @param simb
     * @return
     */
    protected static ISymbol getFSymbol(ArcImsSimplePolygonSymbol simb) {
        String sTrans = simb.getFilltransparency();

        if (ds != '.') {
            sTrans = sTrans.replace(ds, '.');
        }

        float trans = Float.parseFloat(sTrans);

        Color fColor = SymbolUtils.getColor(simb.getFillcolor(), trans);
        SimpleFillSymbol mSimb = new SimpleFillSymbol();
        
        mSimb.setFillColor(fColor);

        //Set the type of fill
        int type = getFillStyle(simb.getFilltype());

        switch (type) {
        case FConstant.SYMBOL_STYLE_FILL_GRAYFILL:
            mSimb.setFillColor(getGray(fColor));

            break;

        case FConstant.SYMBOL_STYLE_FILL_LIGHTGRAYFILL:
            mSimb.setFillColor(getLightGray(fColor));

            break;

        case FConstant.SYMBOL_STYLE_FILL_DARKGRAYFILL:
            mSimb.setFillColor(getDarkGray(fColor));

            break;

        default:

        	mSimb.setFillColor(fColor);
            // Paint fill = createPatternFill(type, fColor);
            // mSimb.setFill(fill);
        }

        if (simb.isHasBoundary()) {
        	
        	SimpleLineSymbol sls = getFSymbol(simb.getBoundary());
        	mSimb.setOutline(sls);
        }

        logger.info("Getting ArcIMS simple polygon symbol");

        return mSimb;
    }

    /**
     * A this time, this symbol will be promoted into a genery polygon symbol
     * with the intermediate color between start and finish color and an outline
     * "darker" color
     * @param simb
     * @return
     */
    protected static ISymbol getFSymbol(ArcImsGradientFillSymbol simb) {
        String sTrans = simb.getTransparency();

        if (ds != '.') {
            sTrans = sTrans.replace(ds, '.');
        }

        float trans = Float.parseFloat(sTrans);

        Color startColor = SymbolUtils.getColor(simb.getStartcolor(), trans);
        Color finishColor = SymbolUtils.getColor(simb.getFinishcolor(), trans);
        Color avgColor = mixColors(startColor, finishColor);

        GradientFillSymbol gfs = new GradientFillSymbol();
        Color[] start_end = new Color[2];
        start_end[0] = startColor;
        start_end[1] = finishColor;

        SimpleLineSymbol sls = new SimpleLineSymbol();
        sls.setLineColor(avgColor);
        
        gfs.setOutline(sls);
        gfs.setGradientColor(start_end);

        //Until gvSIG supports gradients, this code is useless
        /*
        int type = getFillStyle(simb.getType());
        
        Point2D startP2D = null;
        Point2D finishP2D = null;
        
        double ratio = 150;
        
        switch (type) {
        case FConstant.SYMBOL_STYLE_FILL_UPWARD_DIAGONAL:
                startP2D = new Point2D.Double(0.0*ratio,0.0*ratio);
                finishP2D = new Point2D.Double(1.0*ratio,1.0*ratio);
                break;
        case FConstant.SYMBOL_STYLE_FILL_DOWNWARD_DIAGONAL:
                startP2D = new Point2D.Double(0.0*ratio,1.0*ratio);
                finishP2D = new Point2D.Double(1.0*ratio,0.0*ratio);
                break;
        case FConstant.SYMBOL_STYLE_FILL_VERTICAL:
                startP2D = new Point2D.Double(0.5*ratio,0.0*ratio);
                finishP2D = new Point2D.Double(0.5*ratio,1.0*ratio);
                break;
        case FConstant.SYMBOL_STYLE_FILL_HORIZONTAL:
                startP2D = new Point2D.Double(0.0*ratio,0.5*ratio);
                finishP2D = new Point2D.Double(1.0*ratio,0.5*ratio);
                break;
        }
        
        //Set the paint
        mSimb.setFill((Paint) new GradientPaint(startP2D,startColor,finishP2D,finishColor));
        //Remove the outline
        mSimb.setOutlined(false);
        */
        logger.info("Getting ArcIMS gradient fill...");

        return gfs;
    }

    protected static ISymbol getFSymbol(RasterFillSymbol simb) {
        //Get the icon file
        String surl = simb.getUrl();
        File img = getIconFile(surl);

        //Get the transparency
        String sTrans = simb.getTransparency();

        if (ds != '.') {
            sTrans = sTrans.replace(ds, '.');
        }

        float trans = Float.parseFloat(sTrans);
        Color fColor = new Color(1.0f, 1.0f, 1.0f, trans);

        PictureFillSymbol pfs = new PictureFillSymbol();
        // If is an image, we build the paint
        ImageFilter filter = new ImageFilter();

        if (filter.accept(img)) {
            //Creates the BufferedImage object
            BufferedImage bi;
            ImageIcon icon = new ImageIcon(img.getAbsolutePath());
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            // Draw the image in the BufferedImage object
            Graphics2D big = createG2(fColor, bi);
            big.drawImage(icon.getImage(), 0, 0, null);

            Rectangle2D rProv = new Rectangle();
            rProv.setFrame(0, 0, w, h);

            try {
				pfs.setImage(img.toURI().toURL());
			} catch (Exception e) {
				logger.error("");
				return getDefaultFSymbol(FConstant.SYMBOL_TYPE_FILL);
			}
            pfs.setHasOutline(false);
        }

        logger.info("Getting ArcIMS raster fill symbol");
        return pfs;
    }

    public static ISymbol getDefaultFSymbol(int type) {
    	
    	int rndcol = initialColors.size();
    	rndcol = (int) (System.currentTimeMillis() % rndcol);
    	Color incolor = (Color) initialColors.get(rndcol);
    	Color outcolor = incolor.darker().darker();
    	
    	ISymbol resp = null;
    	switch (type) {
    	
    	case FConstant.SYMBOL_TYPE_FILL:
    		SimpleFillSymbol sfs = new SimpleFillSymbol();
    		sfs.setFillColor(incolor);
    		SimpleLineSymbol sls = new SimpleLineSymbol();
    		sls.setLineColor(outcolor);
    		sfs.setOutline(sls);
    		resp = sfs;
    		break;
    	case FConstant.SYMBOL_TYPE_LINE:
    		SimpleLineSymbol sls2 = new SimpleLineSymbol();
    		sls2.setLineColor(outcolor);
    		resp = sls2;
    		break;
    	case FConstant.SYMBOL_TYPE_POINT:
    		SimpleMarkerSymbol sms = new SimpleMarkerSymbol();
    		sms.setStyle(SimpleMarkerSymbol.CIRCLE_STYLE);
    		sms.setSize(5);
    		resp = sms;
    		break;
    	default:
    		resp = (new SimpleFillSymbol()).getSymbolForSelection();
    	break;
    	}
		// TODO Auto-generated method stub
		return resp;
	}

	/**
     * Return a Font Symbol
     * @param symbol
     * @return
     */
    protected static ISymbol getFSymbol(TextSymbol symb) {
    	
    	SimpleTextSymbol mSimb = new SimpleTextSymbol();

        String font = symb.getFont();
        int fontSize = Integer.parseInt(symb.getFontSize());
        Color fontColor = SymbolUtils.getColor(symb.getFontColor(), 1);
        int fontStyle = SymbolUtils.getFontStyle(symb.getFontStyle());

        mSimb.setFont(new Font(font, fontStyle, fontSize));
        mSimb.setTextColor(fontColor);
        mSimb.setFontSize(fontSize);

        logger.info(
            "Getting ArcIMS TextSymbol...");

        return mSimb;
    }

    /**
     * @param symbol
     * @return
     */
    protected static ISymbol getFSymbol(TextMarkerSymbol symbol) {
        logger.info("Getting ArcIMS TextSymbol as  a random point symbol");

        return getDefaultFSymbol(FConstant.SYMBOL_TYPE_POINT);
    }

    protected static ISymbol getFSymbol(TrueTypeMarkerSymbol symbol) {
        logger.info("Getting ArcIMS TextSymbol as  a random point symbol");

        return getDefaultFSymbol(FConstant.SYMBOL_TYPE_POINT);
    }

    /**
     * This method will return a null value as
     * this kind of symbol cannot be promoted to other
     * supported symbol
     * @param symbol
     * @return null
     */
    protected static ISymbol getFSymbol(ChartSymbol symbol) {
        return null;
    }



    /*
     * Supporting private methods
     */

    /**
     * Gets the correct constant for a ArcIMS point type
     * @param type
     * @return
     */
    private static int getPointType(String type) {
        //Default symbol, as gvsig doesn't paints stars
        int mtype = FConstant.SYMBOL_STYLE_MARKER_CIRCLE;

        if (type.equals(SymbolUtils.POINT_TYPE_CIRCLE)) {
            mtype = FConstant.SYMBOL_STYLE_MARKER_CIRCLE;
        } else if (type.equals(SymbolUtils.POINT_TYPE_CROSS)) {
            mtype = FConstant.SYMBOL_STYLE_MARKER_CROSS;
        } else if (type.equals(SymbolUtils.POINT_TYPE_SQUARE)) {
            mtype = FConstant.SYMBOL_STYLE_MARKER_SQUARE;
        } else if (type.equals(SymbolUtils.POINT_TYPE_TRIANGLE)) {
            mtype = FConstant.SYMBOL_STYLE_MARKER_TRIANGLE;
        }

        return mtype;
    }

    /**
     * Method to translate from ArcIMS type to a string of values that will be parsed
     * @return a correct string for the line type definition as "1,3" for dots
     */
    private static String getLinePattern(String type) {
        return (String) SymbolUtils.LINE_TYPES.get(type);
    }

    /**
     * Translates ArcIMS line end style into BasicStroke line end constants
     * @see java.awt.BasicStroke#getEndCap()
     * @param captype
     * @return
     */
    private static int getCapType(String captype) {
        int resp = BasicStroke.CAP_BUTT;

        if (captype.equals(SymbolUtils.CAP_TYPE_BUTT)) {
            resp = BasicStroke.CAP_BUTT;
        } else if (captype.equals(SymbolUtils.CAP_TYPE_ROUND)) {
            resp = BasicStroke.CAP_ROUND;
        } else if (captype.equals(SymbolUtils.CAP_TYPE_SQUARE)) {
            resp = BasicStroke.CAP_SQUARE;
        }

        return resp;
    }

    /**
     * Translate ArcIMS joining line types into BasicStroke join line constants
     * @see java.awt.BasicStroke#getLineJoin()
     * @param jointype
     * @return
     */
    private static int getJoinType(String jointype) {
        int resp = BasicStroke.CAP_BUTT;

        if (jointype.equals(SymbolUtils.JOIN_TYPE_ROUND)) {
            resp = BasicStroke.JOIN_ROUND;
        } else if (jointype.equals(SymbolUtils.JOIN_TYPE_MITER)) {
            resp = BasicStroke.JOIN_MITER;
        } else if (jointype.equals(SymbolUtils.JOIN_TYPE_BEVEL)) {
            resp = BasicStroke.JOIN_BEVEL;
        }

        return resp;
    }

    /**
     * Gets a valid FConstant integer to translate an ArcIMS fill style
     * @param filltype
     * @return
     */
    private static int getFillStyle(String filltype) {
        int style = FConstant.SYMBOL_STYLE_FILL_SOLID;

        if (filltype.equals(SymbolUtils.FILL_TYPE_SOLID)) {
            style = FConstant.SYMBOL_STYLE_FILL_SOLID;
        } else if (filltype.equals(SymbolUtils.FILL_TYPE_BDIAG)) {
            style = FConstant.SYMBOL_STYLE_FILL_UPWARD_DIAGONAL;
        } else if (filltype.equals(SymbolUtils.FILL_TYPE_FDIAG)) {
            style = FConstant.SYMBOL_STYLE_FILL_DOWNWARD_DIAGONAL;
        } else if (filltype.equals(SymbolUtils.FILL_TYPE_CROSS)) {
            style = FConstant.SYMBOL_STYLE_FILL_CROSS;
        } else if (filltype.equals(SymbolUtils.FILL_TYPE_DIAGC)) {
            style = FConstant.SYMBOL_STYLE_FILL_CROSS_DIAGONAL;
        } else if (filltype.equals(SymbolUtils.FILL_TYPE_HORIZ)) {
            style = FConstant.SYMBOL_STYLE_FILL_HORIZONTAL;
        } else if (filltype.equals(SymbolUtils.FILL_TYPE_VERT)) {
            style = FConstant.SYMBOL_STYLE_FILL_VERTICAL;
        } else if (filltype.equals(SymbolUtils.FILL_TYPE_GRAYFILL)) {
            style = FConstant.SYMBOL_STYLE_FILL_GRAYFILL;
        } else if (filltype.equals(SymbolUtils.FILL_TYPE_LIGHTGRAYFILL)) {
            style = FConstant.SYMBOL_STYLE_FILL_LIGHTGRAYFILL;
        } else if (filltype.equals(SymbolUtils.FILL_TYPE_DARKGRAYFILL)) {
            style = FConstant.SYMBOL_STYLE_FILL_DARKGRAYFILL;
        }

        return style;
    }

    /**
     * @see com.iver.cit.gvsig.fmap.core.v02.FSymbolFactory
     * @param cRef
     * @param bi
     * @return
     */
    static private Graphics2D createG2(Color cRef, BufferedImage bi) {
        Graphics2D big = bi.createGraphics();
        Color color = new Color(0, 0, 0, 0);
        big.setBackground(color);
        big.clearRect(0, 0, w, h);
        big.setColor(new Color(cRef.getRed(), cRef.getGreen(), cRef.getBlue(),
                cRef.getAlpha()));
        big.setStroke(new BasicStroke());

        return big;
    }

    /**
     * @see com.iver.cit.gvsig.fmap.core.v02.FSymbolFactory
     * @param style
     * @param cRef
     * @return
     */
    private static Paint createPatternFill(int style, Color cRef) {
        w = 7;
        h = 7;

        BufferedImage bi = null;
        Graphics2D big = null;

        Rectangle2D rProv = new Rectangle();
        rProv.setFrame(0, 0, w, h);

        Paint resulPatternFill = null;
        bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        big = createG2(cRef, bi);

        switch (style) {
        case FConstant.SYMBOL_STYLE_FILL_SOLID:
            return null;

        case FConstant.SYMBOL_STYLE_FILL_TRANSPARENT:
            return null;

        case FConstant.SYMBOL_STYLE_FILL_UPWARD_DIAGONAL:
            big.drawLine(0, 0, w, h);

            break;

        case FConstant.SYMBOL_STYLE_FILL_CROSS:
            big.drawLine(w / 2, 0, w / 2, h);
            big.drawLine(0, h / 2, w, h / 2);

            break;

        case FConstant.SYMBOL_STYLE_FILL_CROSS_DIAGONAL:
            big.drawLine(0, 0, w, h);
            big.drawLine(0, h, w, 0);

            break;

        case FConstant.SYMBOL_STYLE_FILL_VERTICAL:
            big.drawLine(w / 2, 0, w / 2, h);

            break;

        case FConstant.SYMBOL_STYLE_FILL_HORIZONTAL:
            big.drawLine(0, h / 2, w, h / 2);

            break;

        case FConstant.SYMBOL_STYLE_FILL_DOWNWARD_DIAGONAL:
            big.drawLine(0, h, w, 0);

            break;
        }

        resulPatternFill = new TexturePaint(bi, rProv);

        return resulPatternFill;
    }

    /**
     * Gets the hue of a color and returns the color
     * with 75% of saturation and 50% of brightness
     * @param cRef
     * @return
     */
    private static Color getGray(Color cRef) {
        float[] hsbvals = new float[3];
        Color.RGBtoHSB(cRef.getRed(), cRef.getGreen(), cRef.getBlue(), hsbvals);

        return new Color(Color.HSBtoRGB(hsbvals[0], 0.75f, 0.5f));
    }

    /**
     * Gets the hue of a color and returns the color
     * with 75% of saturation and 25% of brightness
     * @param cRef
     * @return
     */
    private static Color getLightGray(Color cRef) {
        float[] hsbvals = new float[3];
        Color.RGBtoHSB(cRef.getRed(), cRef.getGreen(), cRef.getBlue(), hsbvals);

        return new Color(Color.HSBtoRGB(hsbvals[0], 0.25f, 0.75f));
    }

    /**
     * Gets the hue of a color and returns the color
     * with 75% of saturation and 75% of brightness
     * @param cRef
     * @return
     */
    private static Color getDarkGray(Color cRef) {
        float[] hsbvals = new float[3];
        Color.RGBtoHSB(cRef.getRed(), cRef.getGreen(), cRef.getBlue(), hsbvals);

        return new Color(Color.HSBtoRGB(hsbvals[0], 0.25f, 0.25f));
    }

    /**
     * Get the average of two colours
     * @param a Color to mix
     * @param b Color to mix
     * @return (a+b)/2
     */
    private static Color mixColors(Color a, Color b) {
        return new Color((a.getRed() + b.getRed()) / 2,
            (a.getGreen() + b.getGreen()) / 2, (a.getBlue() + b.getBlue()) / 2);
    }

    /**
     * Gets from an URL a File with the icon stored in a temporary directory
     * @param surl
     * @return
     */
    private static File getIconFile(String surl) {
        File img = null;

        try {
            URL url;
            url = new URL(surl);

            String[] partes = surl.split("/");
            String realFilename = partes[partes.length - 1];
            String[] realsplit = realFilename.split("\\.");

            String downFileName = realsplit[0] +
                Long.toString(System.currentTimeMillis()) + "." + realsplit[1];

            img = ArcImsDownloadUtils.downloadFile(url, downFileName);
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        } catch (ConnectException e) {
            logger.error(e.getMessage(), e);
        } catch (UnknownHostException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return img;
    }
}

