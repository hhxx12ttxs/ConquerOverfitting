/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package queuescript;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Patrick
 */
public class ResizeImage {

	public static void resizeImage(String inputFile, String outputFile, int maxWidth, int maxHeight) {
		try {
			BufferedImage img = ImageIO.read(new File(inputFile));
			int wNew = 0, hNew = 0;
			float ratioX = (float)maxWidth / (float)img.getWidth();
			float ratioY = (float)maxHeight / (float)img.getHeight();

			System.out.println("RatioX" + ratioX);

			if((img.getWidth() <= maxWidth) && (img.getHeight() <= maxHeight))
			{
				wNew = img.getWidth();
				hNew = img.getHeight();
			}
			else if((ratioX * img.getHeight()) < maxHeight)
			{
				hNew = (int)(ratioX * img.getHeight());
				wNew = maxWidth;
			}
			else
			{
				wNew = (int)(ratioY * img.getWidth());
				hNew = maxHeight;
			}

			Image scaledImage = img.getScaledInstance(wNew, hNew, Image.SCALE_SMOOTH);
			BufferedImage outImg = new BufferedImage(wNew, hNew, BufferedImage.TYPE_INT_RGB);
			Graphics g = outImg.getGraphics();
			g.drawImage(scaledImage, 0, 0, null);
			g.dispose();
			ImageIO.write(outImg, "jpeg", new File(outputFile));
		} catch (Exception ex) {
			Logger.getLogger(ResizeImage.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}

