package de.mpicbg.tds.rm.rplugin.gui;

import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;
import de.mpicbg.tds.rm.rplugin.RViewExampleSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class RPlotRenderer extends AbstractRenderer {

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
		RViewExampleSet exampleSet = (RViewExampleSet) renderable;

		Image image = exampleSet.getImage();
		RPlotCanvas canvas = new RPlotCanvas(image);

		MediaTracker mediaTracker = new MediaTracker(canvas);
		mediaTracker.addImage(image, 0);
		try {
			mediaTracker.waitForID(0);
		} catch (InterruptedException ie) {
			System.err.println(ie);
			System.exit(1);
		}


		canvas.setSize(image.getWidth(null), image.getHeight(null));

		return canvas;
	}


	private static class RPlotCanvas extends Canvas {

		private BufferedImage image;
		private BufferedImage scaledImage;


		public RPlotCanvas(final Image src) {
			this.image = toBufferedImage(src);

			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					if (!isVisible())
						return;

					scaledImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
					Graphics2D g = scaledImage.createGraphics();
					AffineTransform at = AffineTransform.getScaleInstance((double) getHeight() / image.getWidth(null),
							(double) getHeight() / image.getHeight(null));

					AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
					scaledImage = op.filter(image, null);
				}
			});
		}


		public void paint(Graphics g) {
			g.drawImage(scaledImage, 0, 0, null);
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

