package com.devbugger.physicum.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * This class takes any image and creates a scaled
 * thumbnail of the image.
 *
 * Author: Dag Ăstgulen Heradstveit
 * Date: 2/20/14
 * Time: 5:32 PM
 */
public class FamilyboardThumbnail {

    private static final Logger logger = LoggerFactory.getLogger(FamilyboardThumbnail.class);

    /**
     * Write a thumbnail file to disk.
     * @param file
     */
    public static void create(File file) {
        try {
            File image = new File(formatFileName(file.getAbsolutePath())
                    + "_thumb" +
                    ".png");

            ImageIO.write(createThumbnail(file),
                    "PNG",
                    image);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Create BufferedImage that we will write to disk.
     * @param file
     * @return resultImage
     * @throws java.io.IOException
     */
    private static BufferedImage createThumbnail(File file) throws IOException {
        final BufferedImage sourceImage = ImageIO.read(file);

        int height = 75;
        int width = 75;
        double factor;

        if(sourceImage.getWidth() > sourceImage.getHeight()) {
            factor = ((double)sourceImage.getHeight() / (double)sourceImage.getWidth());
            height = (int)(height * factor);
        }

        else {
            factor = ((double)sourceImage.getWidth() / (double)sourceImage.getHeight());
            width  = (int)(height * factor);
        }

        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
        Graphics2D g2d = resultImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(sourceImage, 0, 0, width, height, null);

        return resultImage;
    }

    /**
     * Gets the file name without the file ending, which
     * we ignore here either way.
     * @param fileName
     * @return fileName
     */
    private static String formatFileName(String fileName) {
        fileName =  fileName.substring(0, fileName.length()-4);
        logger.info("formatFileName: " + fileName);
        return fileName;
    }

    /**
     * Method to get the file for the thumbnail.
     * @param filePath
     * @return filePath
     */
    public static String getThumbNail(String filePath) {
        filePath = formatFileName(filePath);
        return filePath+"_thumb.png";
    }
}

