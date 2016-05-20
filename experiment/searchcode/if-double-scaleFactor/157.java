/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.azosystems.filecategorizer.gui.imageviewer;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Andi
 */
public class LoadableImage implements ImageObserver {

    File file;
    int fixedWidth, fixedHeight;
    Image image;
    static final int INIT_STATUS = 0,  LOADING_STATUS = 1,  LOADED_STATUS = 2,  CANNOT_VIEW = 3;
    private int status = INIT_STATUS;
    static final int SMOOTH_SCALE = BufferedImage.SCALE_SMOOTH,  FAST_SCALE = BufferedImage.SCALE_FAST;
    private int scaleType = BufferedImage.SCALE_FAST;
    static final int FIT_SCALE = 1,  ONE_SCALE = 2;
    private int scaleSize = FIT_SCALE;
    private double scaleFactor = 1.0;
    BufferedImage bufferedImage;

    public LoadableImage(File file, int fixedWidth, int fixedHeight) {
        this.file = file;
        this.fixedHeight = fixedHeight;
        this.fixedWidth = fixedWidth;
        bufferedImage = null;
    }

    public Dimension getPreferredSize() {
        //     BufferedImage bufferedImage;
        if (bufferedImage == null) {
            try {

                bufferedImage = ImageIO.read(file);
            } catch (IOException ex) {
                Logger.getLogger(LoadableImage.class.getName()).log(Level.SEVERE, null, ex);
                return new Dimension(0, 0);
            }
        }
        return new Dimension((int) (bufferedImage.getWidth() * scaleFactor), (int) (bufferedImage.getHeight() * scaleFactor));
    }
 
    public void load() {
        if (status == LOADED_STATUS) {
            return;
        }
        ;

        if (status == LOADING_STATUS) {
            return;
        }
        if (status == CANNOT_VIEW) {
            return;
        }
        //   System.out.println("Loading " + file.getName());
        status = LOADING_STATUS;
           try {
            if (bufferedImage == null) {
                    bufferedImage = ImageIO.read(file);

                    if ((bufferedImage == null) || (bufferedImage.getWidth() <= 1)) {
                        status = CANNOT_VIEW;
                        return;
                    }
            }
            if (getScaleSize() == FIT_SCALE) {
                // System.out.println(bufferedImage.);
            
                int biW = bufferedImage.getWidth();
                int biH = bufferedImage.getHeight();
                double arBi = biW / biH;
                double arJl = fixedWidth / fixedHeight;
                if (arBi > arJl) {
                    image = bufferedImage.getScaledInstance(fixedWidth, -1, getScaleType());
                } else {
                    image = bufferedImage.getScaledInstance(-1, fixedHeight, getScaleType());

                }
             
            } else {
                //   image = bufferedImage;
                image = bufferedImage.getScaledInstance((int) (bufferedImage.getWidth() * scaleFactor), -1, getScaleType());

            }
            image.setAccelerationPriority(1.0f);
            status = LOADED_STATUS;
//   ((sun.awt.image.ToolkitImage)image).preload(this);
        } catch (Exception ex) {
            status = CANNOT_VIEW;
            Logger.getLogger(ImageViewJPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getStatus() {
        return status;
    }

    public boolean imageUpdate(Image arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        //  System.out.println("imageUpdate:"+arg1);
        if (arg1 == 16) {
            //             System.out.println("allbits:"+arg1);

            status = LOADED_STATUS;
            return false;
        //   bufferedImage = null;

        }
        return true;
    }

    public int getScaleType() {
        return scaleType;
    }

    public void setScaleType(int scaleType) {
        this.scaleType = scaleType;
    }

    public int getScaleSize() {
        return scaleSize;
    }

    public void setScaleSize(int scaleSize) {
        this.scaleSize = scaleSize;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }
}

