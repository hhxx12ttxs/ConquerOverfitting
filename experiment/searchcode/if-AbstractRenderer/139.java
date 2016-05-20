package de.mpicbg.tds.rm.rplugin.gui;

import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.report.Reportable;
import de.mpicbg.tds.rm.rplugin.PluginInitializer;
import de.mpicbg.tds.rm.rplugin.RImageFactory;
import de.mpicbg.tds.rm.rplugin.RPlotIOObject;
import de.mpicbg.tds.rm.rplugin.RUtils;
import org.rosuda.REngine.Rserve.RConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.Map;


/**
 * A Rm-renderer which allows to display r-plots. It automatically adapts to the panel size by replotting the figures on
 * resize.
 *
 * @author Holger Brandl
 */
public class DynamicRPlotRenderer extends AbstractRenderer {


    @Override
    public Reportable createReportable(final Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
        return null;
    }


    @Override
    public String getName() {
        return "R Result";
    }


    @Override
    public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
        RPlotIOObject exampleSet = (RPlotIOObject) renderable;

        //        MediaTracker mediaTracker = new MediaTracker(canvas);
//        mediaTracker.addImage(image, 0);
//        try {
//            mediaTracker.waitForID(0);
//        } catch (InterruptedException ie) {
//            System.err.println(ie);
//            System.exit(1);
//        }
//
//
//        canvas.setSize(image.getWidth(null), image.getHeight(null));

        return new RPlotCanvas(exampleSet.getImage(), exampleSet.getScript(), exampleSet.getPushTable());
    }


    private static class RPlotCanvas extends Canvas {

        private Image image;


        public RPlotCanvas(Image image, final String script, final Map<String, IOObject> pushTable) {
            this.image = image;

            RPlotCanvas.this.image = recreateImage(pushTable, script);

            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    if (!isVisible()) {
                        return;
                    }

                    if (!Boolean.parseBoolean(System.getProperty(PluginInitializer.RELPOT_ON_RESIZE, "false"))) {
                        return;
                    }

                    RPlotCanvas.this.image = recreateImage(pushTable, script);
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        RPlotCanvas.this.image = recreateImage(pushTable, script);

                        invalidate();
                        repaint();
                    }
                }
            });
        }


        public BufferedImage recreateImage(Map<String, IOObject> pushTable, String script) {
            RConnection connection = null;
            try {
                connection = new RConnection();

                RUtils.push2R(connection, pushTable);

                BufferedImage bufferedImage = toBufferedImage(RImageFactory.createImage(connection, script, getWidth(), getHeight()));

                connection.close();

                return bufferedImage;
            } catch (Throwable e1) {
                if (connection != null) {
                    connection.close();
                }
                throw new RuntimeException(e1);
            }
        }


        public void paint(Graphics g) {
            g.drawImage(image, 0, 0, null);
        }


        public static BufferedImage toBufferedImage(Image image) {

            if (image instanceof BufferedImage) {
                return (BufferedImage) image;
            }

            // This code ensures that all the pixels in the image are loaded
            image = new ImageIcon(image).getImage();

            // Determine if the image has transparent pixels
            boolean hasAlpha = hasAlpha(image);

            // Create a buffered image with a format that's compatible with the
            // screen
            BufferedImage bimage = null;
            GraphicsEnvironment ge = GraphicsEnvironment
                    .getLocalGraphicsEnvironment();
            try {
                // Determine the type of transparency of the new buffered image
                int transparency = Transparency.OPAQUE;
                if (hasAlpha == true) {
                    transparency = Transparency.BITMASK;
                }

                // Create the buffered image
                GraphicsDevice gs = ge.getDefaultScreenDevice();
                GraphicsConfiguration gc = gs.getDefaultConfiguration();
                bimage = gc.createCompatibleImage(image.getWidth(null), image
                        .getHeight(null), transparency);
            } catch (HeadlessException e) {
            } // No screen

            if (bimage == null) {
                // Create a buffered image using the default color model
                int type = BufferedImage.TYPE_INT_RGB;
                if (hasAlpha == true) {
                    type = BufferedImage.TYPE_INT_ARGB;
                }
                bimage = new BufferedImage(image.getWidth(null), image
                        .getHeight(null), type);
            }

            // Copy image to buffered image
            Graphics g = bimage.createGraphics();

            // Paint the image onto the buffered image
            g.drawImage(image, 0, 0, null);
            g.dispose();

            return bimage;
        }


        public static boolean hasAlpha(Image image) {
            // If buffered image, the color model is readily available
            if (image instanceof BufferedImage) {
                return ((BufferedImage) image).getColorModel().hasAlpha();
            }

            // Use a pixel grabber to retrieve the image's color model;
            // grabbing a single pixel is usually sufficient
            PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
            try {
                pg.grabPixels();
            } catch (InterruptedException e) {
            }

            // Get the image's color model
            return pg.getColorModel().hasAlpha();
        }
    }
}
