package com.hardcode.gdbms.engine.spatial;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.geotools.renderer.style.GraphicStyle2D;
import org.geotools.renderer.style.LineStyle2D;
import org.geotools.renderer.style.MarkStyle2D;
import org.geotools.renderer.style.PolygonStyle2D;
import org.geotools.renderer.style.Style2D;
import org.geotools.renderer.style.TextStyle2D;


/**
 *
 */
public class Renderer {
    private static AffineTransform IDENTITY_TRANSFORM = new AffineTransform();
/*
    public static void render(DataSource ds, ViewPort vp, StyledLayerDescriptor sldtree) throws DriverException, SLDException{
    	StyleLayerDescriptorDecorator sld;
		try {
			sld = new StyleLayerDescriptorDecorator(sldtree, ds.getDataSourceFactory());
		} catch (TransformerConfigurationException e1) {
			throw new SLDException("Fallo en la estructura SLD", e1);
		} catch (JAXBException e1) {
			throw new SLDException("Fallo en la estructura SLD", e1);
		}
		NamedLayerDecorator namedLayer = sld.getNamedLayerStyle(ds.getName());

    	namedLayer.setDataSource(ds);
    	
    	ds.start();

    	long count = ds.getRowCount();
    	   	
    	int geomFieldIndex = ds.getFieldIndexByName((ds.getProperty("geom")));
    	
    	for (int i = 0; i < count; i++){
    		Geometry g = (Geometry) ds.getFieldValue(i, geomFieldIndex);
    		
    		if (g == null) continue;
    		
    		g.transform(vp.getTransformationMatrix());
    		
    		//Extent 
    		boolean any = false;
    		for (int j = 0; j < namedLayer.getExtentCount(); j++) {
    			if (g.intersects(namedLayer.getExtent(j))){
    				any = true;
    			}
			}
    		if ( (namedLayer.getExtentCount() > 0) && (!any) ){
    			continue;
    		}
    		
			//filter
    		try {
    			if (namedLayer.getFilter() != null){
    				if (!((BooleanValue)namedLayer.getFilter().evaluateExpression(i)).getValue()){
    					continue;
    				}
    			}
			} catch (SemanticException e) {
				throw new RuntimeException(e);
			}
			
			//get the style's
			Style2D[] styles = namedLayer.getSymbolizers(i);
			for (int j = 0; j < styles.length; j++) {
				g.draw(vp.getGraphics(), styles[j]);
			}
    	}
    	
    	ds.stop();
    }
    */
    /**
     * DOCUMENT ME!
     *
     * @param graphics DOCUMENT ME!
     * @param shape DOCUMENT ME!
     * @param style DOCUMENT ME!
     */
    public static void drawShape(Graphics2D graphics, Shape shape, Style2D style) {
        if (style instanceof MarkStyle2D) {
            // get the point onto the shape has to be painted
            float[] coords = new float[2];
            PathIterator iter = shape.getPathIterator(IDENTITY_TRANSFORM);
            iter.currentSegment(coords);

            MarkStyle2D ms2d = (MarkStyle2D) style;
            Shape transformedShape = ms2d.getTransformedShape(coords[0],
                    coords[1]);

            if (transformedShape != null) {
                if (ms2d.getFill() != null) {
                    graphics.setPaint(ms2d.getFill());
                    graphics.setComposite(ms2d.getFillComposite());
                    graphics.fill(transformedShape);
                }

                if (ms2d.getContour() != null) {
                    graphics.setPaint(ms2d.getContour());
                    graphics.setStroke(ms2d.getStroke());
                    graphics.setComposite(ms2d.getContourComposite());
                    graphics.draw(transformedShape);
                }
            }
        } else if (style instanceof GraphicStyle2D) {
            // get the point onto the shape has to be painted
            float[] coords = new float[2];
            PathIterator iter = shape.getPathIterator(IDENTITY_TRANSFORM);
            iter.currentSegment(coords);

            GraphicStyle2D gs2d = (GraphicStyle2D) style;

            renderImage(graphics, coords[0], coords[1],
                (Image) gs2d.getImage(), gs2d.getRotation(), gs2d.getOpacity());
        } else if (style instanceof TextStyle2D) {
            // get the point onto the shape has to be painted
            float[] coords = new float[2];
            PathIterator iter = shape.getPathIterator(IDENTITY_TRANSFORM);
            iter.currentSegment(coords);

            AffineTransform old = graphics.getTransform();
            AffineTransform temp = new AffineTransform(old);
            TextStyle2D ts2d = (TextStyle2D) style;
            GlyphVector textGv = ts2d.getTextGlyphVector(graphics);
            Rectangle2D bounds = textGv.getVisualBounds();

            temp.translate(coords[0], coords[1]);

            double x = 0;
            double y = 0;

            if (ts2d.isAbsoluteLineDisplacement()) {
                double offset = ts2d.getDisplacementY();

                if (offset > 0.0) { // to the left of the line
                    y = -offset;
                } else if (offset < 0) {
                    y = -offset + bounds.getHeight();
                } else {
                    y = bounds.getHeight() / 2;
                }

                x = -bounds.getWidth() / 2;
            } else {
                x = (ts2d.getAnchorX() * (-bounds.getWidth())) +
                    ts2d.getDisplacementX();
                y = (ts2d.getAnchorY() * (bounds.getHeight())) +
                    ts2d.getDisplacementY();
            }

            temp.rotate(ts2d.getRotation());
            temp.translate(x, y);

            graphics.setTransform(temp);

            if (ts2d.getHaloFill() != null) {
                float radious = ts2d.getHaloRadius();

                // graphics.translate(radious, -radious);
                graphics.setPaint(ts2d.getHaloFill());
                graphics.setComposite(ts2d.getHaloComposite());
                graphics.fill(ts2d.getHaloShape(graphics));

                // graphics.translate(radious, radious);
            }

            if (ts2d.getFill() != null) {
                graphics.setPaint(ts2d.getFill());
                graphics.setComposite(ts2d.getComposite());
                graphics.drawGlyphVector(textGv, 0, 0);
            }

            graphics.setTransform(old);
        } else {
            // if the style is a polygon one, process it even if the polyline is not
            // closed (by SLD specification)
            if (style instanceof PolygonStyle2D) {
                PolygonStyle2D ps2d = (PolygonStyle2D) style;

                if (ps2d.getFill() != null) {
                    Paint paint = ps2d.getFill();

                    if (paint instanceof TexturePaint) {
                        TexturePaint tp = (TexturePaint) paint;
                        BufferedImage image = tp.getImage();
                        Rectangle2D rect = tp.getAnchorRect();
                        AffineTransform at = graphics.getTransform();
                        double width = rect.getWidth() * at.getScaleX();
                        double height = rect.getHeight() * at.getScaleY();
                        Rectangle2D scaledRect = new Rectangle2D.Double(0, 0,
                                width, height);
                        paint = new TexturePaint(image, scaledRect);
                    }

                    graphics.setPaint(paint);
                    graphics.setComposite(ps2d.getFillComposite());
                    graphics.fill(shape);
                }
            }

            if (style instanceof LineStyle2D) {
                LineStyle2D ls2d = (LineStyle2D) style;

                if (ls2d.getStroke() != null) {
                    // see if a graphic stroke is to be used, the drawing method is completely
                    // different in this case
                    if (ls2d.getGraphicStroke() != null) {
                        drawWithGraphicsStroke(graphics, shape,
                            ls2d.getGraphicStroke());
                    } else {
                        Paint paint = ls2d.getContour();

                        if (paint instanceof TexturePaint) {
                            TexturePaint tp = (TexturePaint) paint;
                            BufferedImage image = tp.getImage();
                            Rectangle2D rect = tp.getAnchorRect();
                            AffineTransform at = graphics.getTransform();
                            double width = rect.getWidth() * at.getScaleX();
                            double height = rect.getHeight() * at.getScaleY();
                            Rectangle2D scaledRect = new Rectangle2D.Double(0,
                                    0, width, height);
                            paint = new TexturePaint(image, scaledRect);
                        }

                        graphics.setPaint(paint);
                        graphics.setStroke(ls2d.getStroke());
                        graphics.setComposite(ls2d.getContourComposite());
                        graphics.draw(shape);
                    }
                }
            }
        }
    }

    // draws the image along the path
    private static void drawWithGraphicsStroke(Graphics2D graphics, Shape shape, BufferedImage image) {
        PathIterator pi = shape.getPathIterator(null, 10.0);
        double[] coords = new double[2];
        int type;
        
        // I suppose the image has been already scaled and its square
        int imageSize = image.getWidth();

        double[] first = new double[2];
        double[] previous = new double[2];
        type = pi.currentSegment(coords);
        first[0] = coords[0];
        first[1] = coords[1];
        previous[0] = coords[0];
        previous[1] = coords[1];

        pi.next();

        while (!pi.isDone()) {
            type = pi.currentSegment(coords);

            switch (type) {
            case PathIterator.SEG_MOVETO:

                // nothing to do?
                break;

            case PathIterator.SEG_CLOSE:

                // draw back to first from previous
                coords[0] = first[0];
                coords[1] = first[1];

            // no break here - fall through to next section
            case PathIterator.SEG_LINETO:

                // draw from previous to coords
                double dx = coords[0] - previous[0];
                double dy = coords[1] - previous[1];
                double len = Math.sqrt((dx * dx) + (dy * dy)); // - imageWidth;

                double theta = Math.atan2(dx, dy);
                dx = (Math.sin(theta) * imageSize);
                dy = (Math.cos(theta) * imageSize);

                double rotation = -(theta - (Math.PI / 2d));
                double x = previous[0] + (dx / 2.0);
                double y = previous[1] + (dy / 2.0);

                double dist = 0;

                for (dist = 0; dist < (len - imageSize); dist += imageSize) {
                    /*graphic.drawImage(image2,(int)x-midx,(int)y-midy,null); */
                    renderImage(graphics, x, y, image, rotation, 1);

                    x += dx;
                    y += dy;
                }

                double remainder = len - dist;
                int remainingWidth = (int) remainder;
                if (remainingWidth > 0) {

                    //clip and render image
                    BufferedImage img = new BufferedImage(remainingWidth, imageSize, image.getType());
                    Graphics2D ig = img.createGraphics();
                    ig.drawImage(image, 0, 0, null);

                    renderImage(graphics, x, y, img, rotation, 1);
                }

                break;

            default:
             }

            previous[0] = coords[0];
            previous[1] = coords[1];
            pi.next();
        }
    }
  
    /**
     * Renders an image on the device
     *
     * @param tx the image location on the screen, x coordinate
     * @param ty the image location on the screen, y coordinate
     * @param img the image
     * @param rotation the image rotatation
     */
    private static void renderImage(Graphics2D graphics, double x, double y, Image image, double rotation, float opacity) {
        AffineTransform temp = graphics.getTransform();
        AffineTransform markAT = new AffineTransform();
        Point2D mapCentre = new java.awt.geom.Point2D.Double(x, y);
        Point2D graphicCentre = new java.awt.geom.Point2D.Double();
        temp.transform(mapCentre, graphicCentre);
        markAT.translate(graphicCentre.getX(), graphicCentre.getY());

        double shearY = temp.getShearY();
        double scaleY = temp.getScaleY();

        double originalRotation = Math.atan(shearY / scaleY);

        markAT.rotate(rotation);
        graphics.setTransform(markAT);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

        // we moved the origin to the centre of the image.
        graphics.drawImage(image, -image.getWidth(null) / 2, -image.getHeight(null) / 2, null);

        graphics.setTransform(temp);

        return;
    }

}

