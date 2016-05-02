package sec.web.renderer;

import ArmyC2.C2SD.RendererPluginInterface.ISinglePointInfo;
import ArmyC2.C2SD.RendererPluginInterface.ISinglePointRenderer;
import ArmyC2.C2SD.RendererPluginInterface.SinglePointInfo;
import ArmyC2.C2SD.Rendering.JavaRenderer;
import ArmyC2.C2SD.Utilities.ImageInfo;
import ArmyC2.C2SD.Utilities.MilStdAttributes;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import RenderMultipoints.clsClipPolygon2;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
/**
 *
 * @author michael.spinelli
 */
@SuppressWarnings("unused")
public class AreaSymbolFill implements ISinglePointRenderer {

    public static final int DEFAULT_SYMBOL_SIZE = 25;
    public static final String KEY_SYMBOL_FILL_IDS = "SYMBOLFILLIDS";
    public static final String KEY_SYMBOL_LINE_IDS = "SYMBOLLINEIDS";
    public static final String KEY_SYMBOL_FILL_ICON_SIZE = "SYMBOLFILLICONSIZE";
    public static final String KEY_SYMBOL_COORDS = "COORDS";
    public static final String KEY_SYMBOL_CLIP = "CLIP";
    public static final String KEY_SYMBOL_HEIGHT = "HEIGHT";
    public static final String KEY_SYMBOL_WIDTH = "WIDTH";
    
    @Override
    public String getRendererID() {
        return "AreaSymbolFillRenderer";
    }

	@Override
	public Boolean canRender(String symbolID, Map<String, String> params) {
		if (symbolID.equalsIgnoreCase("AREASYMBOLFILL")) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public ISinglePointInfo render(String symbolID, Map<String, String> params) {
		if (params.containsKey(KEY_SYMBOL_FILL_IDS)) {
			return renderAreaSymbolFill(params);
		} else if (params.containsKey(KEY_SYMBOL_LINE_IDS)) {
			return renderLineSymbolFill(params);
		} else {
			return null;
		}
	}
    
    /**
     * 
     * @param symbolID
     * @param params
     * @return 
     */
    private ISinglePointInfo renderAreaSymbolFill(Map<String, String> params) {
        ISinglePointInfo returnVal = null; // new SinglePointInfo(null);

        try 
        {
            
            // get
            // values////////////////////////////////////////////////////////

            String strCoords = String.valueOf(params.get(KEY_SYMBOL_COORDS));
            String strSymbolIDs = String.valueOf(params.get(KEY_SYMBOL_FILL_IDS));

			String strClip = null;
			String[] arrclip = null;
			Rectangle2D clip = null;

			String[] strArrCoords = strCoords.split(",");
			int length = strArrCoords.length;
			int[] coords = new int[strArrCoords.length];

			for (int i = 0; i < length; i++) {
				// coords[i] = Double.valueOf(strArrCoords[i]);
				coords[i] = Integer.valueOf(strArrCoords[i]);
			}

			if (params.containsKey(KEY_SYMBOL_CLIP)) {
				strClip = String.valueOf(params.get(KEY_SYMBOL_CLIP));
			}
			if (strClip != null) {
				// System.out.println(strClip);
				arrclip = strClip.split(",");
				clip = new Rectangle2D.Double(Double.valueOf(arrclip[0]), Double.valueOf(arrclip[1]),
						Double.valueOf(arrclip[2]), Double.valueOf(arrclip[3]));
			}

            int height = Integer.valueOf(params.get(KEY_SYMBOL_HEIGHT));
            int width = Integer.valueOf(params.get(KEY_SYMBOL_WIDTH));
            if(height<=0) {
                height = 1;
            }
            if(width<=0) {
                width=1;
            }
            int size = -1;
            if (params.containsKey(KEY_SYMBOL_FILL_ICON_SIZE)) {
                    size = Integer.valueOf(params.get(KEY_SYMBOL_FILL_ICON_SIZE));
            } else {
                    size = DEFAULT_SYMBOL_SIZE;
            }
            String[] symbolIDs = strSymbolIDs.split(",");

            double ratio = 1;
            double maxImageSize=1000;
            double minImageSize=400;
			if (height > maxImageSize || width > maxImageSize) {
				if (height > width) {
					ratio = maxImageSize / ((double) height);
				} else {
					ratio = maxImageSize / ((double) width);
				}
				height = (int) (height * ratio);
				width = (int) (width * ratio);
			} else if (height < minImageSize || width < minImageSize) {
				if (height > width) {
					ratio = minImageSize / ((double) height);
				} else {
					ratio = minImageSize / ((double) width);
				}
				height = (int) (height * ratio);
				width = (int) (width * ratio);
            }//*/
            // create destination
            // image//////////////////////////////////////////
            // may have to larger for line decoration since symbols will be
            // along
            // the line not inside the area.
            BufferedImage fill = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            // create a list of the single point
            // images//////////////////////////
            List<ImageInfo> images = new ArrayList<ImageInfo>();
            //Point2D.Double fakePoint = new Point2D.Double(0, 0);

            Map<String, String> modifiers = new HashMap<String,String>();

            if (size > 0) {
                    modifiers.put(MilStdAttributes.PixelSize, String.valueOf(size));
            }
            Rectangle2D bounds = null;

            ImageInfo iiTemp = null;
            int maxHeight = 0;

            //scale icon size for png
            /*if(width > height)
            {
                size = (width/10) - 5;
            }
            else
            {
                size = (height/10) - 5;
            }
            if(size>50)
            {
                size=50;
            }//*/
            
            //System.out.println("icon size: " + String.valueOf(size));
            int symStd = 1;//1 for 2525C, 0 for 2525Bch2
            for (String id : symbolIDs) {

                    iiTemp = JavaRenderer.getInstance().RenderSinglePointAsImageInfo(id, modifiers, size, true, symStd);

                    images.add(iiTemp);

                    if (iiTemp.getImage().getHeight() > maxHeight) {
                            maxHeight = iiTemp.getImage().getHeight();
                    }
            }
            // create clip shapes for area fill (not relevant for line fill )
            int hOffset = maxHeight / 2;
            int ySpacer = 5;
            int xSpacer = 5;
            int x = 0;
            int y = hOffset;
            Graphics2D g = (Graphics2D) fill.createGraphics();
            int imageIndex = 0;
            int cx = 0;
            int cy = 0;
            Polygon clipShape = new Polygon();
            Path2D clipPath = new Path2D.Double();
            for (int i = 0; i < coords.length - 1; i++) {
                    clipShape.addPoint(coords[i], coords[i + 1]);
                    if (i == 0) {
                            cx = coords[i];
                            cy = coords[i + 1];
                            clipPath.moveTo(coords[i], coords[i + 1]);
                    } else {
                            cx = coords[i];
                            cy = coords[i + 1];
                            clipPath.lineTo(coords[i], coords[i + 1]);
                    }
                    i++;
            }
            clipPath.closePath();
            clipPath.setWindingRule(Path2D.WIND_EVEN_ODD);
            
            Area clipArea = null;
            Area srcArea = null;
            //we already clip based on the shape of the area.
            //this value determines if we also clip based on the visible map area.
			Boolean clippingEnabled = false;
			
			if (clippingEnabled) {// EXPENSIVE, probably shouldn't use

				if (clip != null) {
					clipArea = new Area(clip);
					srcArea = new Area(clipPath);

					if (clipArea.contains(srcArea.getBounds2D()) == false)
						;
					{
						srcArea.intersect(clipArea);
					}
				}
			}
            
			if (ratio != 1) {// image was scaled, need to apply transform
				clipPath.transform(AffineTransform.getScaleInstance(ratio, ratio));
			}

			if (srcArea != null) {
				g.setClip(srcArea);
			} else {
				g.setClip(clipPath);
			}
            
            while ((y < fill.getHeight()) && x < fill.getWidth()) {
                    iiTemp = images.get(imageIndex);

                    // draw image
                    g.drawImage(iiTemp.getImage(), x, y - iiTemp.getSymbolCenterY(), null);

                    // move x for next image
                    x += iiTemp.getImage().getWidth() + xSpacer;
                    if (x + iiTemp.getImage().getWidth() > fill.getWidth()) {
                            // if x is beyond image width, move down a row and set x to
                            // 0
                            x = 0;
                            y += (maxHeight + ySpacer);
                    }

                    // increment image counter
                    imageIndex++;
                    if (imageIndex > images.size() - 1) {
                            imageIndex = 0;
                    }

            }
            g.setClip(null);
            returnVal = new SinglePointInfo(fill);
            g.dispose();

        } catch (Exception exc) {
                System.err.println(exc.getMessage());
                exc.printStackTrace();
        }

        return returnVal;
    }

	private ISinglePointInfo renderLineSymbolFill(Map<String, String> params) {
		ISinglePointInfo returnVal = null;
		try {
			// get values
			ArrayList<Point2D.Double> points = null;
			String strClip = null;
			String strCoords = String.valueOf(params.get(KEY_SYMBOL_COORDS));
			// System.out.println(strCoords);
			String strSymbolIDs = String.valueOf(params.get(KEY_SYMBOL_LINE_IDS));
			int buffer = DEFAULT_SYMBOL_SIZE / 2;
			String[] arrclip = null;
			Rectangle2D clip = null;
			int size = -1;
			if (params.containsKey(KEY_SYMBOL_FILL_ICON_SIZE)) {
				size = Integer.valueOf(params.get(KEY_SYMBOL_FILL_ICON_SIZE));
			} else {
				size = DEFAULT_SYMBOL_SIZE;
			}
			buffer = (size / 2) + 3;
			if (params.containsKey(KEY_SYMBOL_CLIP)) {
				strClip = String.valueOf(params.get(KEY_SYMBOL_CLIP));
			}

			if (strClip != null) {
				// System.out.println(strClip);
				arrclip = strClip.split(",");
				clip = new Rectangle2D.Double(Double.valueOf(arrclip[0]), Double.valueOf(arrclip[1]),
						Double.valueOf(arrclip[2]), Double.valueOf(arrclip[3]));
			}

			int height = Integer.valueOf(params.get(KEY_SYMBOL_HEIGHT));
			int width = Integer.valueOf(params.get(KEY_SYMBOL_WIDTH));

			if (height <= 0) {
				height = 1;
			} else if (width <= 0) {
				width = 1;
			}

			double ratio = 1;
			double maxImageSize = 1000;
			double minImageSize = 400;

                
			String[] strArrCoords = strCoords.split(",");
			int length = strArrCoords.length;
			int[] coords = new int[strArrCoords.length];

			for (int j = 0; j < strArrCoords.length; j++) {
				coords[j] = Integer.valueOf(strArrCoords[j]);
			}

			Path2D path = new Path2D.Double();
			for (int i = 0; i < length - 1; i++) {
				// coords[i] = Double.valueOf(strArrCoords[i]);
				if (i > 0) {
					path.lineTo(coords[i], coords[i + 1]);
				} else if (i == 0) {
					path.moveTo(coords[i], coords[i + 1]);
				}
				// System.out.println(String.valueOf(coords[i]) + ", " +
				// String.valueOf(coords[i+1]));
				i++;
			}
			Rectangle2D pathBounds = path.getBounds2D();
			if (pathBounds.getHeight() > maxImageSize || pathBounds.getWidth() > maxImageSize) {
				if (pathBounds.getHeight() > pathBounds.getWidth()) {
					ratio = maxImageSize / pathBounds.getHeight();
				} else {
					ratio = maxImageSize / pathBounds.getWidth();
				}
			}
                /*else if(pathBounds.getHeight()<minImageSize || pathBounds.getWidth()<minImageSize)
                {
                    if(pathBounds.getHeight()>pathBounds.getWidth())
                    {
                        ratio = minImageSize/pathBounds.getHeight();
                    }
                    else
                    {
                        ratio = minImageSize/pathBounds.getWidth();
                    }
                    double dbuffer = buffer;
                    dbuffer = (dbuffer)/(ratio*dbuffer);
                    buffer = (int)(buffer*dbuffer);
                }
                System.out.println("height");
                System.out.println(String.valueOf(pathBounds.getHeight()));
                System.out.println(String.valueOf(height));//*/
                
			if (ratio != 1.0) {
				// System.out.println("scale transform ratio = " +
				// String.valueOf(ratio));
				path.transform(AffineTransform.getScaleInstance(ratio, ratio));
			}
			// System.out.println("translate buffer = " +
			// String.valueOf(buffer));
			path.transform(AffineTransform.getTranslateInstance(buffer, buffer));
			PathIterator itr = path.getPathIterator(null);
			// itr.next();
			points = new ArrayList<Point2D.Double>();
			double[] dpts = new double[6];
			while (itr.isDone() == false) {
				itr.currentSegment(dpts);
				points.add(new Point2D.Double(dpts[0], dpts[1]));
				itr.next();
				// System.out.println(String.valueOf(points.get(points.size()-1)));
			}

                height = (int)(height*ratio)+(buffer*2);
                width = (int)(width*ratio)+(buffer*2);


                //scale icon size for png
                /*if(width > height)
                {
                    size = (width/10) - 5;
                }
                else
                {
                    size = (height/10) - 5;
                }
                if(size>50)
                {
                    size=50;
                }//*/
                
                String[] symbolIDs = strSymbolIDs.split(",");
                //System.out.println(strSymbolIDs.toString());
                // create destination
                // image//////////////////////////////////////////
                // may have to larger for line decoration since symbols will be
                // along
                // the line not inside the area.
			BufferedImage fill = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

			// create a list of the single point
			// images//////////////////////////
			List<ImageInfo> images = new ArrayList<ImageInfo>();

			Map<String, String> modifiers = new HashMap<String, String>();

			if (size > 0) {
				modifiers.put(MilStdAttributes.PixelSize, String.valueOf(size));
			}
			Rectangle2D bounds = null;

			ImageInfo iiTemp = null;
			int maxHeight = 0;

                ////////////////////////////////////////////////////////
                //MAGIC FUNCTIONS to get points along the line to
                //determines where I should draw the icons
                //System.out.println("coord cound: "+String.valueOf(coords.length));

                //System.out.println("points final: "+String.valueOf(points.size()));
                ArrayList<SymbolPoint> iconPoints = getPointsOnLineForSymbols(points, size, size, null);
                //System.out.println("icon points: "+String.valueOf(iconPoints.size()));
                ////////////////////////////////////////////////////////

                //get image collection
			int j = 0;
			double rotation = 0;
			int symStd = 1;// 1 for 2525C, 0 for 2525Bch2
			// MilStdSymbol msTemp = null;
			for (String id : symbolIDs) {
				// System.out.println(id);
				// rotation = iconPoints.get(j).getRotation();
				// j++;
				// if(j>iconPoints.size())
				// {
				// j=0;
				// }
				// msTemp = JavaRendererUtilities.createMilstdSymbol(id,
				// modifiers);
				// msTemp.setRotation(rotation);
				// iiTemp =
				// JavaRenderer.getInstance().RenderSinglePointAsImageInfo(id,
				// modifiers, size, true);
				iiTemp = JavaRenderer.getInstance().RenderSinglePointAsImageInfo(id, modifiers, size, true, symStd);

				images.add(iiTemp);

				if (iiTemp.getImage().getHeight() > maxHeight) {
					maxHeight = iiTemp.getImage().getHeight();
				}

			}

			Graphics2D g = (Graphics2D) fill.createGraphics();
			// for drawing shapes, does not apply:
			// g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			// RenderingHints.VALUE_ANTIALIAS_ON);
			// for drawing images:
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			// g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
			// RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			// Now Draw Points on Lines with the list of points
			int i = 0;
			int k = 0;
			double currentRotation = 0;
			// System.out.println("point count: " +
			// String.valueOf(iconPoints.size()));
			// String currentID = null;
			for (SymbolPoint point : iconPoints) {

				iiTemp = images.get(i);
				i++;
				// System.out.println("draw point " + String.valueOf(k));
				k++;
				if (i >= images.size()) {
					i = 0;
				}
				int x = (int) point.getX() - iiTemp.getSymbolCenterX();
				int y = (int) point.getY() - iiTemp.getSymbolCenterY();

				currentRotation = Math.toRadians(point.getRotation());
				// adjust for rotation
				g.setTransform(AffineTransform.getRotateInstance(currentRotation, point.getX(), point.getY()));

				// draw image
				g.drawImage(iiTemp.getImage(), x, y, null);

				// reset transform
				g.setTransform(new AffineTransform());
			}
			returnVal = new SinglePointInfo(fill);
			g.dispose();
		} catch (Exception exc) {
			System.err.println(exc.getMessage());
			exc.printStackTrace();
		}
		return returnVal;
	}
        
    /**
     * NOTE: we'll probably have to change the return type but I'll need an
     * angle to go with each point.
     * @param points Points in pixels that make up the source line.  Also
     * has rotation value so I can match the angle of the line.
     * @param iconHeight max height an icon will be
     * @param iconWidth max width an icon will be
     * @return Collection of points that say where the icons
     * should be drawn.
     */
    private ArrayList<SymbolPoint> getPointsOnLineForSymbols(ArrayList<Point2D.Double> points,
            int iconHeight, int iconWidth, Rectangle2D clip)
    {
        ArrayList<SymbolPoint> returnPoints = new ArrayList<SymbolPoint>();
        try
        {
            //SymbolPoint temp = new SymbolPoint(100, 100, 45);
            //fuzzy math cloud that returns points where to draw icons on the line.
            //10 is arbitrary: spacing between the icons
            //null rect is arbitrary: pass non-null clip rect for bounding
            //ArrayList<Double>pts=clsPatternFill.lineToImages(points, iconWidth, iconHeight, 10, null);
            ArrayList<Double>pts=lineToImages(points, iconWidth, iconHeight, 10, clip);
            int j=0;
            //3 tuples: x pixels,y pixels,angle in degrees
            for(j=0;j<pts.size()/3;j++)
            {
                returnPoints.add(new SymbolPoint(pts.get(3*j),pts.get(3*j+1),pts.get(3*j+2)));
            }
        }
        catch(Exception exc)
        {
            System.err.println(exc.getMessage());
            exc.printStackTrace();
        }

        return returnPoints;
    }

    public class SymbolPoint
    {
        double _x;
        double _y;
        double _rotation;
        public SymbolPoint(double x, double y, double rotation)
        {
            _x = x;
            _y = y;
            _rotation = rotation;
        }

        public double getX()
        {
            return _x;
        }
        public double getY()
        {
            return _y;
        }
        public double getRotation()
        {
            return _rotation;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Line Functions">
    public static ArrayList<Double> lineToImages(ArrayList<Point2D.Double> points,
            //double scale,
            double iconWidth,
            double iconHeight,
            double pixelSpace,
            Rectangle2D clipBounds) {
        ArrayList<Point2D> pts = new ArrayList<Point2D>();
        int j = 0;
        for (j = 0; j < points.size(); j++) {
            pts.add(new Point2D.Double(points.get(j).x, points.get(j).y));
        }

        return lineToImages2(pts, iconWidth, iconHeight, pixelSpace, clipBounds);
    }

    @SuppressWarnings("deprecation")
	private static ArrayList<Double> lineToImages2(ArrayList<Point2D> points,
            //double scale,
            double iconWidth,
            double iconHeight,
            double pixelSpace,
            Rectangle2D clipBounds) {
        ArrayList<Double> pts = null;
        try {
            int j = 0;
            int k = 0;
            double x = 0;
            double y = 0;
            if (clipBounds != null) {
                double width = clipBounds.getWidth();
                double height = clipBounds.getHeight();
                x = clipBounds.getX();
                y = clipBounds.getY();

                Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
                points=clsClipPolygon2.ClipPolygon2(points, clipBounds, false);
            }
            double x1 = 0;
            double y1 = 0;
            double x2;
            double y2;
            double angle;
            Point2D pt1 = null;
            Point2D pt2 = null;
            double dist = 0;
            pts = new ArrayList<Double>();
            for (j = 0; j < points.size() - 1; j++) {
                if (points.get(j) == null) {
                    continue;
                }
                pt1 = points.get(j);
                pt2 = points.get(j + 1);
                x1 = pt1.getX();
                y1 = pt1.getY();
                x2 = pt2.getX();
                y2 = pt2.getY();
                dist = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
                int iterations = (int) (dist / (pixelSpace + iconWidth));
                angle = Math.atan((y2 - y1) / (x2 - x1)) * (180.0 / Math.PI);
                for (k = 0; k < iterations; k++) {
                    x = x1 + ((double) k / (double) iterations) * (x2 - x1);
                    y = y1 + ((double) k / (double) iterations) * (y2 - y1);
                    pts.add(x);
                    pts.add(y);
                    pts.add(angle);
                }
            }
        } catch (Exception exc) {
                System.err.println(exc.getMessage());
		exc.printStackTrace();
        }
        return pts;
    }
    // </editor-fold>
}

