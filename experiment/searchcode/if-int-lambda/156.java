/*
 * @(#)PatternImage.java
 *
 * This program is version 2.0 of JDC Education Kit for Optical Experiments.
 * Copyright (C) Jasper Display Corporation 2013.
 * This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public
 * License version 2.0 as published by the Free Software Foundation. 

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. 
 * You may contact Jasper Display Corporation by email at info@jasperdisplay.com
 * or by telephone at +1-408-855-6640.
 * More information is provided at http://www.jasperdisplay.com/.
 */
package com.jasper.core;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * This PatternImage include the algorithms of application
 *
 * @version 1.0 28 Aug 2013
 *
 * @author Albert Nguyen
 *
 */
public class PatternImage {

    private double lambda;
    private double xoff;
    private double yoff;
    private double angle;
    private double pxsize;
    private double focal;
    private double mirrorTheta;
    private double mirrorPhy;
    private double mirrorThetaExp3;
    private double mirrorPhyExp3;
    // Michelson
    private double xoffMichelson;
    private double yoffMichelson;
    private double focalMichelson;
    // Draw
    private double d_widthX;
    private double d_widthY;
    private double d_heightX;
    private double d_heightY;
    private double d_postionX;
    private double d_postionY;
    private double d_rotation;
    private double d_grayLevel;
    private double d_zoom;
    private double d_spacing;
    //end
    // Talbot
    private double d_widthXTalbot;
    private double d_widthYTalbot;
    private double d_heightXTalbot;
    private double d_heightYTalbot;
    private double d_postionXTalbot;
    private double d_postionYTalbot;
    private double d_rotationTalbot;
    private double d_grayLevelTalbot;
    private double d_spacingTalbot;
    public BufferedImage canvas;
    public static int width;
    public static int height;
    public static int gray2phase[];
    public static byte gray2phaseFinetuning[];
    // Cyllin
    private double xoffCyllin;
    private double yoffCyllin;
    private double focalCyllin;
    // Microscope
    private double xoffMicroscope;
    private double yoffMicroscope;
    private double focalMicroscope;
    private double mirrorThetaSpectometer;
    private double mirrorPhySpectometer;
    /*
     * ZoomLayOut
     */
    public Point startPoint = new Point(0, 0);
    public Point rectLocale = new Point();
    public Dimension rectSize = new Dimension();
    public int zoom_layout = 80;
    public int action = 0;
    // Beam  Shifting
    private double xoffBeamShifting;
    private double yoffBeamShifting;
    // Import file
    private double k;
    private double r;
    private double e;
    private double kr;
    private String formula_importFile;
    private double width_importFile;
    private double rotation_importFile;
    private double position_importFile;
    private double grayLevel_importFile;
    private PatternImage patternImage;
    private BufferedImage buffImgCGH1;
    private File fileCGH1;
    private double[][] buferPattern;
    private byte flag = 0;
    private boolean tuningFlag = false;
    // title string
    public String title;

    public PatternImage(int w, int h) {
        width = w;
        height = h;
        canvas = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        title = "";
    }

    public PatternImage(int w, int h, int a) {
        width = w;
        height = h;
        canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        title = "";

        Graphics2D g2 = canvas.createGraphics();
        g2.setBackground(Color.RED);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 0.0f));
    }

    public PatternImage() {
        canvas = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    }

    public void init(double lambda) {
        this.lambda = lambda;
        this.xoff = 0.0;
        this.yoff = 0.0;
        this.angle = 0.0;
        this.pxsize = 6.4e-6;
        this.focal = 1.0;
        gray2phase = new int[256];
        initGray2phase();
    }

    public double getLambda() {
        return lambda;
    }

    public Rectangle getBounds() {
        return new Rectangle(width, height);
    }

    public void updateLensParameter(double xoff, double yoff, double focal) {
        this.xoff = xoff;
        this.yoff = yoff;
        this.focal = focal;
        title = "lens " + xoff + " " + yoff + " " + focal;
    }

    public void updateLensMichelsonParameter(double xoff, double yoff, double focal) {
        this.xoffMichelson = xoff;
        this.yoffMichelson = yoff;
        this.focalMichelson = focal;
        title = "lens michelson " + xoff + " " + yoff + " " + focal;
    }

    public void updateZoomparten(Point sp, Point recL, Dimension rectSize, int action) {
        this.startPoint = sp;
        this.rectLocale = recL;
        this.rectSize = rectSize;
        this.action = action;
        title = "lens " + xoff + " " + yoff + " " + focal;
    }

    public void updateFresnelParameter(double width, double height) {
        this.d_widthX = width;
        this.d_heightX = height;
        title = "Fresnel " + width + " " + height;
    }

    public void updateLensParameterDrawSlit(int slit, double width, double height, double postion, double rotation, double grayLevel, double spacing) {
        this.d_widthX = width;
        this.d_heightX = height;
        this.d_postionX = postion;
        this.d_rotation = rotation;
        this.d_grayLevel = grayLevel;
        this.d_spacing = spacing;
        title = (slit < 2 ? "Draw Single Slit " : "Draw Double Slit ") + d_widthX + " " + d_heightX + " " + this.d_postionX + " " + this.d_rotation + " " + this.d_grayLevel;
    }

    public void updateParameterDrawSignalProcessing(double widthX, double widthY, double heightX, double heightY, double postionX, double postionY, double rotation, double grayLevel, double processingSpac) {
        this.d_widthX = widthX;
        this.d_widthY = widthY;
        this.d_heightX = heightX;
        this.d_heightY = heightY;
        this.d_postionX = postionX;
        this.d_postionY = postionY;
        this.d_rotation = rotation;
        this.d_grayLevel = grayLevel;
        this.d_spacing = processingSpac;
        title = "Draw Signal Processing " + d_widthX + " " + d_widthY + " " + this.d_heightX + " " + this.d_heightY + " " + this.d_postionX + " " + this.d_postionY + " " + this.d_rotation + " " + this.d_grayLevel + " " + this.d_spacing;
    }

    public void updateParameterDrawTalbot(double widthX, double widthY, double heightX, double heightY, double postionX, double postionY, double rotation, double grayLevel, double processingSpac) {
        this.d_widthXTalbot = widthX;
        this.d_widthYTalbot = widthY;
        this.d_heightXTalbot = heightX;
        this.d_heightYTalbot = heightY;
        this.d_postionXTalbot = postionX;
        this.d_postionYTalbot = postionY;
        this.d_rotationTalbot = rotation;
        this.d_grayLevelTalbot = grayLevel;
        this.d_spacingTalbot = processingSpac;
        title = "Draw Talbot " + d_widthXTalbot + " " + d_widthYTalbot + " " + this.d_heightXTalbot + " " + this.d_heightYTalbot + " " + this.d_postionXTalbot + " " + this.d_postionYTalbot + " " + this.d_rotationTalbot + " " + this.d_grayLevelTalbot + " " + this.d_spacingTalbot;
    }

    public void updateParameterDrawSignalPhoto(double widthX, double heightX) {
        this.d_widthX = widthX;
        this.d_heightX = heightX;
        title = "Signal Photo " + widthX + " " + heightX;
    }

    public void updatePhaseRetarderParameter(double grayLevel) {
        this.d_grayLevel = grayLevel;
        title = "PhaseRetarder " + grayLevel;
    }

    public void updateCyllindricalParameter(double xoff, double angle, double focal) {
        this.xoffCyllin = xoff;
        this.yoffCyllin = angle;
        this.focalCyllin = focal;
        title = "cylindrical " + xoff + " " + angle + " " + focal;
    }

    public void updateBeamShiftingParameter(double xoff, double yoff) {
        this.xoffBeamShifting = xoff;
        this.yoffBeamShifting = yoff;
        title = "calibration " + xoff + " " + yoff;
    }

    public void updateMicoscopeParameter(double xoff, double yoff, double focal) {
        this.xoffMicroscope = xoff;
        this.yoffMicroscope = yoff;
        this.focalMicroscope = focal;
        title = "microscope " + xoff + " " + yoff + " " + focal;
    }

    public void updateMirrorParameter(double phy, double theta) {
        this.mirrorPhy = phy;
        this.mirrorTheta = theta;
        title = "mirror " + phy + " " + theta;
    }

    public void updateMirrorExp3Parameter(double phy, double theta) {
        this.mirrorPhyExp3 = phy;
        this.mirrorThetaExp3 = theta;
        title = "mirror " + phy + " " + theta;
    }

    public void updateMirrorSpectometerParameter(double phy, double theta) {
        this.mirrorPhySpectometer = phy;
        this.mirrorThetaSpectometer = theta;
        title = "mirror " + phy + " " + theta;
    }

    public void updateParameterImportFile(double k, double r, double e, double kr
            , double width, double positions, double rotation, double grayLevel, String formula) {
        this.k = k;
        this.r = r;
        this.e = e;
        this.kr = kr;
        this.formula_importFile = formula;
        this.width_importFile = width;
        this.rotation_importFile = rotation;
        this.position_importFile = positions;
        this.grayLevel_importFile = grayLevel;
        title = "ImportFile ";
    }

    private void initGray2phase() {
        for (int i = 0; i < gray2phase.length; i++) {
            gray2phase[i] = i;
        }
    }

    private int phase2gray(double phase) {
        int scale = gray2phase.length;
        phase = phase / 2.0d / Math.PI;
        phase -= Math.floor(phase);
        int gray = Math.min((int) Math.round(phase * scale), scale - 1);
        //Math.getExponent(gray);
        return gray2phase[gray];
    }

    private byte phase2grayImportFile(double phase) {
        int scale = gray2phase.length;
        phase = phase / 2.0d / Math.PI;
        phase -= Math.floor(phase);
        int gray = Math.min((int) Math.round(phase * scale), scale - 1);
        //Math.getExponent(gray);
        return gray2phaseFinetuning[gray];
    }

    public void paintZoom(Rectangle rec) {

        Graphics2D g2 = (Graphics2D) canvas.getGraphics();
        g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g2.draw(rec);
        g2.fill(rec);
        // g2.D
    }

    // Telephoto Lens algorithms
    public void paintLens() {
        WritableRaster raster = canvas.getRaster();
        int[] iArray = new int[1];
        double x2, y2, phase;
        double y1;
        double fixpart = Math.PI / lambda / focal;
        // 2*pi/la*0.1*x*psize
        //double fixpart2 = 2.0 * Math.PI / lambda * 0.1;

        // calculate phase of each pixel;
        for (int i = 0; i < height; i++) {
            x2 = (double) (i - height / 2 + 1) * pxsize;
            x2 -= (-yoff);
            //x2 -= 0.0;
            x2 = Math.pow(x2, 2.0);
            // Albert 2013/09/05
            Math.getExponent(x2);
            // 2*pi/la*0.1*x*psize
            double fixpart2 = 2.0 * Math.PI / lambda * x2 * 0.1;
            for (int j = 0; j < width; j++) {
                y2 = (double) (j - width / 2 + 1) * pxsize;
                y2 -= (xoff);
                //y2 -= 0.0;
                y1 = y2;
                y2 = Math.pow(y2, 2.0);

                // Albert 2013/09/05
                Math.getExponent(y2);
                phase = fixpart * (x2 + y2);
                phase += fixpart2 * x2 * y2;

                iArray[0] = phase2gray(phase);
                raster.setPixel(j, i, iArray);
            }
        }
        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;
    }

    // Michelson Lens algorithms
    public void paintLensMichelson() {
        WritableRaster raster = canvas.getRaster();
        int[] iArray = new int[1];
        double x2, y2, phase;
        double y1;
        double fixpart = Math.PI / lambda / (focalMichelson);

        for (int i = 0; i < height; i++) {
            x2 = (double) (i - height / 2 + 1) * pxsize;
            x2 -= (-yoffMichelson);
            x2 = Math.pow(x2, 2.0);
            Math.getExponent(x2);
            double fixpart2 = 2.0 * Math.PI / lambda * x2 * 0.1;
            for (int j = 0; j < width; j++) {
                y2 = (double) (j - width / 2 + 1) * pxsize;
                y2 -= (xoffMichelson);
                y1 = y2;
                y2 = Math.pow(y2, 2.0);

                Math.getExponent(y2);
                phase = fixpart * (x2 + y2);
                phase += fixpart2 * x2 * y2;

                iArray[0] = phase2gray(phase);
                raster.setPixel(j, i, iArray);
            }
        }
        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;
    }

    // Microscope algorithms
    public void paintMicroscope() {
        WritableRaster raster = canvas.getRaster();
        int[] iArray = new int[1];
        double x2, y2, phase;
        double fixpart = Math.PI / lambda / (focalMicroscope);

        // calculate phase of each pixel;
        for (int i = 0; i < height; i++) {
            x2 = (double) (i - height / 2 + 1) * pxsize;
            x2 -= (-yoffMicroscope / 1000);
            x2 = Math.pow(x2, 2.0);
            Math.getExponent(x2);
            // 2*pi/la*0.1*x*psize
            double fixpart2 = 2.0 * Math.PI / lambda * x2 * 0.1;
            for (int j = 0; j < width; j++) {
                y2 = (double) (j - width / 2 + 1) * pxsize;
                y2 -= (xoffMicroscope / 1000);
                y2 = Math.pow(y2, 2.0);

                Math.getExponent(y2);
                phase = fixpart * (x2 + y2);
                phase += fixpart2 * x2 * y2;
                iArray[0] = phase2gray(phase);
                raster.setPixel(j, i, iArray);
            }
        }
        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;
    }
    // Cylindrical algorithms
    public void paintCylindrical() {
        WritableRaster raster = canvas.getRaster();
        // wave=exp(1i*pi/wl*xt.^2);
        int[] iArray = new int[1];
        double x1, y1, x2, phase;

        double fixpart2 = 2.0 * Math.PI / lambda;
        double fixpart = Math.PI / lambda / (focalCyllin);

        double costheta = Math.cos(Math.toRadians((yoffCyllin)));
        double sintheta = Math.sin(Math.toRadians((yoffCyllin)));

        for (int i = 0; i < height; i++) {
            x1 = (double) (i - height / 2 + 1) * pxsize;
            x1 -= xoffCyllin;
            for (int j = 0; j < width; j++) {
                y1 = (double) (j - width / 2 + 1) * pxsize;
                x2 = x1 * costheta - y1 * sintheta;
                x2 = Math.pow(x2, 2.0);
                //phase = fixpart * x2 + fixpart2 * y1;
                //phase = fixpart * x2 + fixpart2 * Math.pow(x2, 2.0);
                phase = fixpart * x2;

                iArray[0] = phase2gray(phase);
                raster.setPixel(j, i, iArray);
            }
        }
        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;
    }

    // Mirror algorithms
    public void paintMirror() {
        WritableRaster raster = canvas.getRaster();

        int[] iArray = new int[1];
        double phase, x, y;
        double phy = Math.toRadians(mirrorPhy);
        double theta = Math.toRadians(mirrorTheta);

        double xcomp = Math.sin(phy) * Math.cos(theta);
        double ycomp = Math.sin(phy) * Math.sin(theta);

        double fixpart = 2.0 * Math.PI / lambda;

        for (int i = 0; i < height; i++) {
            x = (double) (i - height / 2 + 1) * pxsize;
            x = xcomp * x;
            for (int j = 0; j < width; j++) {
                y = (double) (j - width / 2 + 1) * pxsize;
                y = ycomp * y;
                phase = fixpart * (x + y);

                iArray[0] = phase2gray(phase);
                raster.setPixel(j, i, iArray);
            }
        }
        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;
    }

    // BeamSteere algorithms
    public void paintBeamSteere() {
        WritableRaster raster = canvas.getRaster();

        int[] iArray = new int[1];
        double phase, x, y;
        double phy = Math.toRadians(mirrorPhyExp3);
        double theta = Math.toRadians(mirrorThetaExp3);

        double xcomp = Math.sin(phy) * Math.cos(theta);
        double ycomp = Math.sin(phy) * Math.sin(theta);

        double fixpart = 2.0 * Math.PI / lambda;

        for (int i = 0; i < height; i++) {
            x = (double) (i - height / 2 + 1) * pxsize;
            x = xcomp * x;
            for (int j = 0; j < width; j++) {
                y = (double) (j - width / 2 + 1) * pxsize;
                y = ycomp * y;
                phase = fixpart * (x + y);

                iArray[0] = phase2gray(phase);
                raster.setPixel(j, i, iArray);
            }
        }
        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;
    }

    // Mirror Spectrometer algorithms
    public void paintMirrorSpectrometer() {
        WritableRaster raster = canvas.getRaster();

        int[] iArray = new int[1];
        double phase, x, y;
        double phy = Math.toRadians(mirrorPhySpectometer);
        double theta = Math.toRadians(mirrorThetaSpectometer);

        double xcomp = Math.sin(phy) * Math.cos(theta);
        double ycomp = Math.sin(phy) * Math.sin(theta);

        double fixpart = 2.0 * Math.PI / lambda;

        for (int i = 0; i < height; i++) {
            x = (double) (i - height / 2 + 1) * pxsize;
            x = xcomp * x;
            for (int j = 0; j < width; j++) {
                y = (double) (j - width / 2 + 1) * pxsize;
                y = ycomp * y;
                phase = fixpart * (x + y);

                iArray[0] = phase2gray(phase);
                raster.setPixel(j, i, iArray);

            }
        }
        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;

    }

    public void paintBeamShifting() {
        double[][] cHGPattern;
        WritableRaster raster = canvas.getRaster();
        double[] iArray = new double[1];
        double phase, x, y;
        //phy and theta uses "radian"
        double phy = Math.toRadians(xoffBeamShifting);
        double theta = Math.toRadians(yoffBeamShifting);

        double xm = Math.sin(phy) * Math.cos(theta);
        double ym = Math.sin(phy) * Math.sin(theta);
        double fixpart = 2.0 * Math.PI / lambda;
        if(tuningFlag && flag == 2) {
            try {
                String canonicalPathTmp = fileCGH1.getCanonicalPath();
                String canonicalPath = canonicalPathTmp.substring(0, canonicalPathTmp.length() - 4) + "tmp.jpg";
                File tmpFile = new File(canonicalPath);
                BufferedImage originalImage = ImageIO.read(fileCGH1);
                BufferedImage resizeImageJpg = resizeImage(originalImage, originalImage.getType(), width, height);
                ImageIO.write(resizeImageJpg, "jpg", tmpFile);

                cHGPattern = compute(tmpFile);
                tmpFile.delete();
                for (int i = 0; i < width; i++) {
                    x = (double) (i - width / 2 + 1) * pxsize;
                    x = xm * x;
                    for (int j = 0; j < height; j++) {
                        y = (double) (height / 2 - j + 1) * pxsize;
                        y = ym * y;

                        phase = fixpart * (x + y) + cHGPattern[i][j];
                        iArray[0] = phase2gray(phase);
                        raster.setPixel(i, j, iArray);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(PatternImage.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (tuningFlag && flag == 1) {
            for (int i = 0; i < width; i++) {
                x = (double) (i - width / 2 + 1) * pxsize;
                x = xm * x;
                for (int j = 0; j < height; j++) {
                    y = (double) (height / 2 - j + 1) * pxsize;
                    y = ym * y;

                    phase = fixpart * (x + y) + buferPattern[i][j];
                    iArray[0] = phase2gray(phase);
                    raster.setPixel(i, j, iArray);
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "There is no pattern to Fine tune. Please select an experiment or import a CGH pattern", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public double[][] compute(File file) {
        try {
            BufferedImage img = ImageIO.read(file);

            Raster raster = img.getData();
            int w = raster.getWidth(), h = raster.getHeight();

            double temp;
            double pixels[][] = new double[w][h];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    temp = ((double)raster.getSample(x, y, 0)/255)*2*Math.PI;
                    pixels[x][y] = temp;
                }
            }
            return pixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public double[][] compute(BufferedImage img) {
        try {
            Raster raster = img.getData();
            int w = raster.getWidth(), h = raster.getHeight();
            double temp;
            double pixels[][] = new double[w][h];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    temp = ((double)raster.getSample(x, y, 0)/255)*2*Math.PI;
                    pixels[x][y] = temp;
                }
            }
            return pixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public java.awt.Image getImage(int pixels[][]) {
        int w = pixels.length;
        int h = pixels[0].length;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = (WritableRaster) image.getData();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                raster.setSample(i, j, 0, pixels[i][j]);
            }
        }
        File output = new File("check.jpg");
        try {
            ImageIO.write(image, "jpg", output);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    public void paintFineTuning(BufferedImage buffImg) {

        double scale = 1.0;
        buffImg = PatternImage.resizeImage(buffImg, buffImg.getType(), 1920, 1080);
        Graphics2D g2 = (Graphics2D) canvas.getGraphics();
        g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double canvasX = canvas.getWidth() / 2;
        double canvasY = canvas.getHeight() / 2;
        int imageWidth = buffImg.getWidth();
        int imageHeight = buffImg.getHeight();
        double x = (scale * imageWidth) / 2;
        double y = (scale * imageHeight) / 2;
        AffineTransform at = AffineTransform.getTranslateInstance(canvasX - x, canvasY - y);
        at.scale(scale, scale);
        /// AffineTransform at = AffineTransform.getScaleInstance(1920, 1080);
        //double k =(imread('testHologram.bmp'))/255*2*pi;
        g2.drawRenderedImage(buffImg, at);
    }

    public void paintCGH1(BufferedImage buffImg, File file) {
        fileCGH1 = file;
        double scale = 1.0;
        buffImg = PatternImage.resizeImage(buffImg, BufferedImage.TYPE_INT_RGB, 1366, 768);
        Graphics2D g2 = (Graphics2D) canvas.getGraphics();
        g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double canvasX = canvas.getWidth() / 2;
        double canvasY = canvas.getHeight() / 2;
        int imageWidth = buffImg.getWidth();
        int imageHeight = buffImg.getHeight();
        double x = (scale * imageWidth) / 2;
        double y = (scale * imageHeight) / 2;
        AffineTransform at = AffineTransform.getTranslateInstance(canvasX - x, canvasY - y);
        at.scale(scale, scale);
        g2.drawRenderedImage(buffImg, null);
        g2.drawImage(buffImg, null, width, width);
        g2.dispose();
        flag = 2;
        tuningFlag = true;
    }

    private static int[] parseElement(String s) throws IOException {
        int[] ret = new int[gray2phase.length];
        int idx = 0;
        int scale = gray2phase.length;
        String elements[] = s.split(",");
        for (String element : elements) {
            if (element.contains(":")) {
                String ends[] = element.split(":");
                if (ends.length != 2) {
                    throw new IOException();
                }
                int front = Integer.valueOf(ends[0]);
                int rear = Integer.valueOf(ends[1]);
                if (front > scale - 1 || front < 0 || rear > scale - 1 || rear < 0) {
                    throw new IOException();
                }
                int offset = (rear - front) / Math.abs(rear - front);
                for (int val = front; true; val += offset) {
                    ret[idx++] = val;
                    if (val == rear) {
                        break;
                    }
                }
            } else {
                int p = Integer.valueOf(element);
                if (p > scale - 1 || p < 0) {
                    throw new IOException();
                }
                ret[idx++] = p;
            }
        }

        // prepare the return array
        int[] retVal = new int[idx];
        for (int i = 0; i < idx; i++) {
            retVal[i] = ret[i];
        }

        return retVal;
    }

    public static boolean openFile(String filename) {
        boolean retValue = false;
        BufferedReader br = null;

        // initialize new table with values in the old table
        int newTable[] = new int[gray2phase.length];
        for (int i = 0; i < gray2phase.length; i++) {
            newTable[i] = gray2phase[i];
        }
        try {
            br = new BufferedReader(new FileReader(filename));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (strLine.length() != 0 && strLine.charAt(0) != '#') {
                    String[] tokens = strLine.trim().split("[\t ]+");
                    for (String token : tokens) {
                        String lrhs[] = token.split("=");
                        if (lrhs.length != 2) {
                            throw new IOException();
                        }
                        int lhs[] = parseElement(lrhs[0]);
                        int rhs[] = parseElement(lrhs[1]);
                        if (lhs.length != rhs.length) {
                            throw new IOException();
                        }
                        for (int i = 0; i < lhs.length; i++) {
                            newTable[lhs[i]] = rhs[i];
                        }
                    }
                }
            }
            // update with the new table values
            for (int i = 0; i < gray2phase.length; i++) {
                gray2phase[i] = newTable[i];
            }
            retValue = true;
        } catch (Exception e) {
            // System.out.println("parse or IO error");
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
            }
        }
        return retValue;
    }

    public void slit(int slit) {
        if (slit == 0) {
            slit = 1;
        }

        int lineWidth = (int) d_widthX;
        int lineHeight = (int) d_heightX;
        int lineRotation = (int) d_rotation;
        int linePostion = (int) d_postionX;
        int lineGray = (int) d_grayLevel;
        int spac = (int) d_spacing;
        Graphics2D g = (Graphics2D) canvas.getGraphics();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        int NumRow = slit;
        int[] RowY;
        RowY = new int[NumRow];
        int DelY = canvas.getHeight() / NumRow;

        if (slit == 1) {
            for (int i = 0; i < RowY.length; i++) {
                RowY[i] = ((linePostion + canvas.getHeight() / 2) - canvas.getHeight() / 2) + DelY / 2 + DelY * i;
            }
        } else {

            for (int i = 0; i < RowY.length; i++) {
                if (i == 0) {
                    RowY[i] = ((linePostion + canvas.getHeight() / 2) - canvas.getHeight() / 2) + ((DelY) - spac / 2);
                } else {
                    RowY[i] = ((linePostion + canvas.getHeight() / 2) - canvas.getHeight() / 2) + ((DelY) + spac / 2);
                }
            }
        }
        Rectangle rect2;
        for (int i = 0; i < NumRow; i++) {
            g = (Graphics2D) canvas.getGraphics();
            g.setColor(new Color(lineGray, lineGray, lineGray));
            rect2 = new Rectangle((canvas.getWidth() - lineWidth) / 2, RowY[i] - lineHeight / 2, lineWidth, lineHeight);
            // Xoay quanh tam cua hinh chu nhat g.rotate(Math.toRadians(lineRotation), rect2.x + rect2.width / 2, rect2.y + rect2.height / 2);
            g.rotate(Math.toRadians(lineRotation), canvas.getWidth() / 2, canvas.getHeight() / 2);
            g.draw(rect2);
            g.fill(rect2);
        }
        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;
    }

    public void signalProcessing0() {

        int lineWidthX = (int) d_widthX;
        int lineHeightX = (int) d_heightX;
        int lineWidthY = (int) d_widthY;
        int lineHeightY = (int) d_heightY;
        int lineRotation = (int) d_rotation;
        int linePostionX = (int) d_postionX;
        int linePostionY = (int) d_postionY;
        int lineGray = (int) d_grayLevel;
        int spac = (int) d_spacing;
        Graphics2D g = (Graphics2D) canvas.getGraphics();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        int NumCol = 5;
        int NumRow = 2;
        int[] ColX;
        int[] RowY;
        ColX = new int[NumCol];
        int DelX = canvas.getWidth() / NumCol;
        for (int i = 0; i < ColX.length; i++) {
            ColX[i] = ((linePostionY + canvas.getWidth() / 2) - canvas.getWidth() / 2) + (DelX / 2 + DelX * i);
        }
        RowY = new int[NumRow];
        int DelY = canvas.getHeight() / NumRow;
        for (int i = 0; i < RowY.length; i++) {
            if (i % 2 == 0) {
                RowY[i] = ((linePostionX + canvas.getHeight() / 2) - canvas.getHeight() / 2) + ((DelY) - spac / 2);
            } else {
                RowY[i] = ((linePostionX + canvas.getHeight() / 2) - canvas.getHeight() / 2) + ((DelY) + spac / 2);
            }
        }
        Rectangle rect2;
        for (int i = 0; i < NumCol; i++) {
            g = (Graphics2D) canvas.getGraphics();
            g.setColor(new Color(lineGray, lineGray, lineGray));
            rect2 = new Rectangle(ColX[i] - lineWidthY / 2, (canvas.getHeight() - lineHeightY) / 2, lineWidthY, lineHeightY);
            // g.rotate(Math.toRadians(lineRotation), rect2.x + rect2.width / 2, rect2.y + rect2.height / 2);
            g.rotate(Math.toRadians(lineRotation), canvas.getWidth() / 2, canvas.getHeight() / 2);
            g.draw(rect2);
            g.fill(rect2);
        }
        for (int i = 0; i < NumRow; i++) {
            g = (Graphics2D) canvas.getGraphics();
            g.setColor(new Color(lineGray, lineGray, lineGray));
            rect2 = new Rectangle((canvas.getWidth() - lineWidthX) / 2, RowY[i] - lineHeightX / 2, lineWidthX, lineHeightX);
            //g.rotate(Math.toRadians(lineRotation), rect2.x + rect2.width / 2, rect2.y + rect2.height / 2);
            g.rotate(Math.toRadians(lineRotation), canvas.getWidth() / 2, canvas.getHeight() / 2);
            g.draw(rect2);
            g.fill(rect2);
        }
        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;
    }

    public void signalProcessing() {

        int lineWidthX = (int) d_widthX;
        int lineHeightX = (int) d_heightX;
        int lineWidthY = (int) d_widthY;
        int lineHeightY = (int) d_heightY;
        int lineRotation = (int) d_rotation;
        int linePostionX = (int) d_postionX;
        int linePostionY = (int) d_postionY;
        int lineGray = (int) d_grayLevel;
        int spac = (int) d_spacing;


        //System.out.println(spac);
        int MaxHeight = (int) (Math .sqrt(Math .pow(canvas.getHeight(), 2) + Math.pow(canvas.getWidth(), 2)));

        Graphics2D g = (Graphics2D) canvas.getGraphics();
        g.clearRect(0, 0, canvas.getWidth(), MaxHeight);
        int GapCol = 5;
        int GapRow = 4;
        int NumCol = GapCol + 4;
        int NumRow;
        int[] ColX;
        int[] RowY;
        ColX = new int[NumCol];
        int DelX = canvas.getWidth()/ GapCol;
        for (int i = 0; i < ColX.length; i++) {
            //ColX[i] = ((linePostionY + canvas.getWidth() / 2) - canvas.getWidth() / 2) + (DelX / 2 + DelX * i);
            ColX[i] = linePostionY  + (DelX / 2 + DelX * i);

        }

        //Sort the ColX array
        Arrays.sort(ColX);
        int MaxX = ColX[ColX.length - 1];
        for (int i = 0; i < ColX.length; i++) {
            if(ColX[i]  <= ((canvas.getWidth() - MaxHeight)/2) ) {
                ColX[i] = MaxX + DelX;
                MaxX = ColX[i];
            }

        }
        Arrays.sort(ColX);
        int MinX = ColX[0];
        for (int i = 0; i < ColX.length; i++) {

            if(ColX[i] > ((canvas.getWidth() + MaxHeight)/2 )) {
                ColX[i] = MinX - DelX;
                MinX = ColX[i];
            }

        }
        Arrays.sort(ColX);


        int DelY = canvas.getHeight() / GapRow + spac / GapRow;
        if(DelY == 0) {
            DelY = 1;
        }
        //NumRow = (int) canvas.getHeight()/DelY + 1;
        NumRow = (int) MaxHeight/DelY + 1;


        RowY = new int[NumRow];
        for (int i = 0; i < RowY.length; i++) {
             //RowY[i] = ((linePostionX + canvas.getHeight() / 2) - canvas.getHeight() / 2) + ((DelY / 2) + DelY * i) ;
            RowY[i] = linePostionX  + ((DelY / 2) + DelY * i) + canvas.getHeight()/2 - MaxHeight/2 ;
        }

        //Sort the RowY array
        Arrays.sort(RowY);
        int MaxY = RowY[RowY.length - 1];
        for (int i = 0; i < RowY.length; i++) {

            if(RowY[i]  <= ((canvas.getHeight() - MaxHeight)/2) ) {
                RowY[i] = MaxY + DelY;
                MaxY = RowY[i];
            }

        }
        Arrays.sort(RowY);
        int MinY = RowY[0];
        for (int i = 0; i < RowY.length; i++) {

            if(RowY[i] > ((canvas.getHeight() + MaxHeight)/2 )) {
                RowY[i] = MinY - DelY;
                MinY = RowY[i];
            }

        }
        Arrays.sort(RowY);


        Rectangle rect2;
        for (int i = 0; i < NumCol; i++) {
            g = (Graphics2D) canvas.getGraphics();
            g.setColor(new Color(lineGray, lineGray, lineGray));


            rect2 = new Rectangle(ColX[i] - lineWidthY / 2, (canvas.getHeight() - lineHeightY) / 2, lineWidthY, lineHeightY);
            // g.rotate(Math.toRadians(lineRotation), rect2.x + rect2.width / 2, rect2.y + rect2.height / 2);
            g.rotate(Math.toRadians(lineRotation), canvas.getWidth() / 2, canvas.getHeight() / 2);
            g.draw(rect2);
            g.fill(rect2);
        }
        for (int i = 0; i < NumRow; i++) {

            g = (Graphics2D) canvas.getGraphics();
            g.setColor(new Color(lineGray, lineGray, lineGray));

            rect2 = new Rectangle((canvas.getWidth() - lineWidthX) / 2, RowY[i] - lineHeightX / 2, lineWidthX, lineHeightX);

            g.rotate(Math.toRadians(lineRotation), canvas.getWidth() / 2, canvas.getHeight() / 2);

            g.draw(rect2);
            g.fill(rect2);
        }

        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;
    }

     public void paintTalbot() {
        int lineWidthX = (int) d_widthXTalbot;
        int lineHeightX = (int) d_heightXTalbot;
        int lineWidthY = (int) d_widthYTalbot;
        int lineHeightY = (int) d_heightYTalbot;
        int lineRotation = (int) d_rotationTalbot;
        int linePostionX = (int) d_postionXTalbot;
        int linePostionY = (int) d_postionYTalbot;
        int lineGray = (int) d_grayLevelTalbot;
        int spac = (int) d_spacingTalbot;
        //System.out.println(spac);
        int MaxHeight = (int) (Math .sqrt(Math .pow(canvas.getHeight(), 2) + Math.pow(canvas.getWidth(), 2)));

        Graphics2D g = (Graphics2D) canvas.getGraphics();
        g.clearRect(0, 0, canvas.getWidth(), MaxHeight);
        int GapCol = 5;
        int GapRow = 4;
        int NumCol = GapCol + 4;
        int NumRow;
        int[] ColX;
        int[] RowY;
        ColX = new int[NumCol];
        int DelX = canvas.getWidth()/ GapCol;
        for (int i = 0; i < ColX.length; i++) {
            //ColX[i] = ((linePostionY + canvas.getWidth() / 2) - canvas.getWidth() / 2) + (DelX / 2 + DelX * i);
            ColX[i] = linePostionY  + (DelX / 2 + DelX * i);

        }

        //Sort the ColX array
        Arrays.sort(ColX);
        int MaxX = ColX[ColX.length - 1];
        for (int i = 0; i < ColX.length; i++) {
            if(ColX[i]  <= ((canvas.getWidth() - MaxHeight)/2) ) {
                ColX[i] = MaxX + DelX;
                MaxX = ColX[i];
            }

        }
        Arrays.sort(ColX);
        int MinX = ColX[0];
        for (int i = 0; i < ColX.length; i++) {

            if(ColX[i] > ((canvas.getWidth() + MaxHeight)/2 )) {
                ColX[i] = MinX - DelX;
                MinX = ColX[i];
            }

        }
        Arrays.sort(ColX);


        int DelY = canvas.getHeight() / GapRow + spac / GapRow;
        if(DelY == 0) {
            DelY = 1;
        }
        //NumRow = (int) canvas.getHeight()/DelY + 1;
        NumRow = (int) MaxHeight/DelY + 1;


        RowY = new int[NumRow];
        for (int i = 0; i < RowY.length; i++) {
             //RowY[i] = ((linePostionX + canvas.getHeight() / 2) - canvas.getHeight() / 2) + ((DelY / 2) + DelY * i) ;
            RowY[i] = linePostionX  + ((DelY / 2) + DelY * i) + canvas.getHeight()/2 - MaxHeight/2 ;
        }

        //Sort the RowY array
        Arrays.sort(RowY);
        int MaxY = RowY[RowY.length - 1];
        for (int i = 0; i < RowY.length; i++) {

            if(RowY[i]  <= ((canvas.getHeight() - MaxHeight)/2) ) {
                RowY[i] = MaxY + DelY;
                MaxY = RowY[i];
            }

        }
        Arrays.sort(RowY);
        int MinY = RowY[0];
        for (int i = 0; i < RowY.length; i++) {

            if(RowY[i] > ((canvas.getHeight() + MaxHeight)/2 )) {
                RowY[i] = MinY - DelY;
                MinY = RowY[i];
            }

        }
        Arrays.sort(RowY);


        Rectangle rect2;
        for (int i = 0; i < NumCol; i++) {
            g = (Graphics2D) canvas.getGraphics();
            g.setColor(new Color(lineGray, lineGray, lineGray));


            rect2 = new Rectangle(ColX[i] - lineWidthY / 2, (canvas.getHeight() - lineHeightY) / 2, lineWidthY, lineHeightY);
            // g.rotate(Math.toRadians(lineRotation), rect2.x + rect2.width / 2, rect2.y + rect2.height / 2);
            g.rotate(Math.toRadians(lineRotation), canvas.getWidth() / 2, canvas.getHeight() / 2);
            g.draw(rect2);
            g.fill(rect2);
        }
        for (int i = 0; i < NumRow; i++) {

            g = (Graphics2D) canvas.getGraphics();
            g.setColor(new Color(lineGray, lineGray, lineGray));

            rect2 = new Rectangle((canvas.getWidth() - lineWidthX) / 2, RowY[i] - lineHeightX / 2, lineWidthX, lineHeightX);

            g.rotate(Math.toRadians(lineRotation), canvas.getWidth() / 2, canvas.getHeight() / 2);

            g.draw(rect2);
            g.fill(rect2);
        }

        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;
    }

    public void signalPhoto(BufferedImage buffImg) {

        double scale = 1.0;
        // scale = d_zoom / 100.0D;
        //buffImg = buffImg.gets
        buffImg = PatternImage.resizeImage(buffImg, buffImg.getType(), 1920, 1080);
        Graphics2D g2 = (Graphics2D) canvas.getGraphics();
        g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double canvasX = canvas.getWidth() / 2;
        double canvasY = canvas.getHeight() / 2;
        int imageWidth = buffImg.getWidth();
        int imageHeight = buffImg.getHeight();
        double x = (scale * imageWidth) / 2;
        double y = (scale * imageHeight) / 2;
        AffineTransform at = AffineTransform.getTranslateInstance(canvasX - x, canvasY - y);
        at.scale(scale, scale);
        /// AffineTransform at = AffineTransform.getScaleInstance(1920, 1080);
        g2.drawRenderedImage(buffImg, at);
        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;
    }

    public void fresnel(BufferedImage buffImg) {
        int width = 0;
        try {
            width = (int) d_widthX;
        } catch (Exception e) {
        }
        if (width < 17) {
            width = 17;
        }
        width = (width / 10) * 16;
        int height = 0;
        try {
            height = (int) d_heightX;
        } catch (Exception e) {
        }
        if (height < 15) {
            height = 15;
        }
        height = (height / 10) * 9;
        double scale = 1.0;
        buffImg = PatternImage.resizeImage(buffImg, buffImg.getType(), width, height);
        Graphics2D g2 = (Graphics2D) canvas.getGraphics();
        g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double canvasX = canvas.getWidth() / 2;
        double canvasY = canvas.getHeight() / 2;
        int imageWidth = buffImg.getWidth();
        int imageHeight = buffImg.getHeight();
        double x = (scale * imageWidth) / 2;
        double y = (scale * imageHeight) / 2;
        AffineTransform at = AffineTransform.getTranslateInstance(canvasX - x, canvasY - y);
        at.scale(scale, scale);
        g2.drawRenderedImage(buffImg, at);
        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;
    }

    public void phaseRetarder() {
        Graphics2D g = (Graphics2D) canvas.getGraphics();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setColor(new Color((int) d_grayLevel, (int) d_grayLevel, (int) d_grayLevel));
        Rectangle rect = new Rectangle(0, 0, canvas.getWidth(), canvas.getHeight());
        g.draw(rect);
        g.fill(rect);
        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;
    }

    public void paintTalbotPhoto(BufferedImage buffImg) {

        double scale = 1.0;
        // scale = d_zoom / 100.0D;
        //buffImg = buffImg.gets
        buffImg = PatternImage.resizeImage(buffImg, buffImg.getType(), 1920, 1080);
        Graphics2D g2 = (Graphics2D) canvas.getGraphics();
        g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double canvasX = canvas.getWidth() / 2;
        double canvasY = canvas.getHeight() / 2;
        int imageWidth = buffImg.getWidth();
        int imageHeight = buffImg.getHeight();
        double x = (scale * imageWidth) / 2;
        double y = (scale * imageHeight) / 2;
        AffineTransform at = AffineTransform.getTranslateInstance(canvasX - x, canvasY - y);
        at.scale(scale, scale);
        /// AffineTransform at = AffineTransform.getScaleInstance(1920, 1080);
        g2.drawRenderedImage(buffImg, at);
        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;
    }

    public void paintImportFile() {

        // TO DO
        // U1 = Uexp[j(k.r+e)]
        //    phase = k.r + e
        //    exp[j(k.r + e)] = cos(phase) + isin(phase)
        //    fixpart = U = 2.0 * Math.PI / lambda
        WritableRaster raster = canvas.getRaster();
        int[] iArray = new int[1];
        double x2, y2, phase;
        double y1;
        double fixpart = 2.0 * Math.PI / lambda;

        double phy = Math.toRadians(width_importFile);
        //double xcomp = Math.sin(phy) * Math.cos(theta);

        //System.out.println("fixpar: " + fixpart);

        for (int i = 0; i < height; i++) {
            x2 = (double) (i - height / 2 + 1) * pxsize;
            //x2 = xcomp * x2;
            //x2 = (double) (i - height / 2 + 1) * pxsize;
            //x2 -= (-yoffMichelson / 1000);
            //x2 = Math.pow(x2, 2.0);
            //Math.getExponent(x2);
            //double fixpart2 = 2.0 * Math.PI / lambda * x2 * 0.1;
            for (int j = 0; j < width; j++) {
                //y2 = (double) (j - width / 2 + 1) * pxsize;
                //y2 -= (xoffMichelson / 1000);
                //y1 = y2;
                //y2 = Math.pow(y2, 2.0);

                //Math.getExponent(y2);
                phase = fixpart * (k*r + e);
                //System.out.println("phase: " + phase + " at x =" + i + " and j =" + j);
                //phase += fixpart2 * x2 * y2;

                iArray[0] = phase2gray(phase);
                raster.setPixel(j, i, iArray);
            }
        }
        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;
    }
    
    public void dynamic(BufferedImage buffImg) {

        double scale = 1.0;
        // scale = d_zoom / 100.0D;
        //buffImg = buffImg.gets
        buffImg = PatternImage.resizeImage(buffImg, buffImg.getType(), 1920, 1080);
        Graphics2D g2 = (Graphics2D) canvas.getGraphics();
        g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double canvasX = canvas.getWidth() / 2;
        double canvasY = canvas.getHeight() / 2;
        int imageWidth = buffImg.getWidth();
        int imageHeight = buffImg.getHeight();
        double x = (scale * imageWidth) / 2;
        double y = (scale * imageHeight) / 2;
        AffineTransform at = AffineTransform.getTranslateInstance(canvasX - x, canvasY - y);
        at.scale(scale, scale);
        /// AffineTransform at = AffineTransform.getScaleInstance(1920, 1080);
        g2.drawRenderedImage(buffImg, at);
        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;
    }

    public void paintImportFileMichelson(BufferedImage buffImg) {

        double scale = 1.0;
        // scale = d_zoom / 100.0D;
        //buffImg = buffImg.gets
        buffImg = PatternImage.resizeImage(buffImg, buffImg.getType(), 1920, 1080);
        Graphics2D g2 = (Graphics2D) canvas.getGraphics();
        g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double canvasX = canvas.getWidth() / 2;
        double canvasY = canvas.getHeight() / 2;
        int imageWidth = buffImg.getWidth();
        int imageHeight = buffImg.getHeight();
        double x = (scale * imageWidth) / 2;
        double y = (scale * imageHeight) / 2;
        AffineTransform at = AffineTransform.getTranslateInstance(canvasX - x, canvasY - y);
        at.scale(scale, scale);
        /// AffineTransform at = AffineTransform.getScaleInstance(1920, 1080);
        g2.drawRenderedImage(buffImg, at);
        buferPattern = compute(canvas);
        flag = 1;
        tuningFlag = true;
    }

    /**
     * scale image
     *
     * @param sbi image to scale
     * @param imageType type of image
     * @param dWidth width of destination image
     * @param dHeight height of destination image
     * @param fWidth x-factor for transformation / scaling
     * @param fHeight y-factor for transformation / scaling
     * @return scaled image
     */
    public static BufferedImage scale(BufferedImage sbi, int imageType, int dWidth, int dHeight, double fWidth, double fHeight) {
        BufferedImage dbi = null;
        if (sbi != null) {
            dbi = new BufferedImage(dWidth, dHeight, imageType);
            Graphics2D g = dbi.createGraphics();
            AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
            g.drawRenderedImage(sbi, at);
        }
        return dbi;
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int type, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        return resizedImage;
    }

    public void paintDefault() {
        Graphics2D g = (Graphics2D) canvas.getGraphics();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setColor(new Color((int) 233, (int) 233, (int) 233));
        Rectangle rect = new Rectangle(0, 0, canvas.getWidth(), canvas.getHeight());
        g.draw(rect);
        g.fill(rect);
    }
}

