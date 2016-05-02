/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imageBone;

import commont.ImageUtil;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import test.FrmShowImage;

/**
 *
 * @author Hiep
 */
public class ImageBone {

    static WritableRaster raster = null;
   
    protected static boolean isBorder(int x, int y) {
        double[] tmp = null;
        if ((0 <= x - 1 && x + 1 < raster.getWidth())
                && (0 <= y - 1 && y + 1 < raster.getHeight())) {
            if (!ImageUtil.isBlackPixel(raster.getPixel(x - 1, y, tmp))
                    || !ImageUtil.isBlackPixel(raster.getPixel(x + 1, y, tmp))
                    || !ImageUtil.isBlackPixel(raster.getPixel(x, y - 1, tmp))
                    || !ImageUtil.isBlackPixel(raster.getPixel(x, y + 1, tmp))
                    ){
//                    || !ImageUtil.isBlackPixel(raster.getPixel(x - 1, y + 1, tmp))
//                    || !ImageUtil.isBlackPixel(raster.getPixel(x - 1, y - 1, tmp))
//                    || !ImageUtil.isBlackPixel(raster.getPixel(x + 1, y + 1, tmp))
//                    || !ImageUtil.isBlackPixel(raster.getPixel(x + 1, y - 1, tmp))
//                ) {
                return true;
            }
        }
        return false;
    }

    protected static boolean isDeletable(int x, int y) {
//        boolean p0 = false, p1 = false, p2 = false, p3 = false, p4 = false,
//                p5 = false, p6 = false, p7 = false;
        boolean p0 = true, p1 = true, p2 = true, p3 = true, p4 = true,
                p5 = true, p6 = true, p7 = true;
        double[] tmp = null;
        boolean xp = x + 1 < raster.getWidth(), xs = 0 <= x - 1,
                yp = y + 1 < raster.getHeight(), ys = 0 <= y - 1;
        if(!ImageUtil.isBlackPixel(raster.getPixel(x, y, tmp))){
            return false;
        }
        if (xp) {
            p0 = ImageUtil.isBlackPixel(raster.getPixel(x + 1, y, tmp));
            if (ys) {
                p1 = ImageUtil.isBlackPixel(raster.getPixel(x + 1, y - 1, tmp));
            }
        }
        if (ys) {
            p2 = ImageUtil.isBlackPixel(raster.getPixel(x, y - 1, tmp));
            if (xs) {
                p3 = ImageUtil.isBlackPixel(raster.getPixel(x - 1, y - 1, tmp));
            }
        }
        if (xs) {
            p4 = ImageUtil.isBlackPixel(raster.getPixel(x - 1, y, tmp));
            if (yp) {
                p5 = ImageUtil.isBlackPixel(raster.getPixel(x - 1, y + 1, tmp));
            }
        }
        if (yp) {
            p6 = ImageUtil.isBlackPixel(raster.getPixel(x, y + 1, tmp));
            if (xp) {
                p7 = ImageUtil.isBlackPixel(raster.getPixel(x + 1, y + 1, tmp));
            }
        }
        if ((p0 && p1 && p2)
                && !(p4 || p5 || p6)) {
            return true;
        }
        if ((p1 && p2 && p3)
                && !(p5 || p6 || p7)) {
            return true;
        }
        if ((p2 && p3 && p4)
                && !(p0 || p6 || p7)) {
            return true;
        }
        if ((p3 && p4 && p5)
                && !(p1 || p0 || p7)) {
            return true;
        }
        if ((p4 && p5 && p6)
                && !(p0 || p1 || p2)) {
            return true;
        }
        if ((p0 && p1 && p2)
                && !(p4 || p5 || p6)) {
            return true;
        }
        if ((p5 && p6 && p7)
                && !(p1 || p2 || p3)) {
            return true;
        }
        if ((p0 && p6 && p7)
                && !(p2 || p3 || p4)) {
            return true;
        }
        return false;
    }

    public static void findBone(String fileName) {
        try {
            BufferedImage img = ImageIO.read(new File(fileName));
            raster = img.getRaster();
            byte[][] flag = new byte[raster.getHeight()][raster.getWidth()];
            double[] tmp = null;
            //nh? phân hóa
            double[] mean = ImageUtil.getMomentAbouMean(raster);
            ImageUtil.binarilizeImage(img, mean);
            //
            BufferedImage imgOri = new BufferedImage(img.getWidth(),
                    img.getHeight(), BufferedImage.TYPE_INT_RGB);
            imgOri.getGraphics().drawImage(img, 0, 0, null);
            //FrmShowImage.showImageInFrame(imgOri);
            boolean isThin = false;
            Stack<int[]> stack = new Stack<int[]>();
            Calendar startTime = Calendar.getInstance();
            double[] whitePixel = new double[3];
            whitePixel[0]=whitePixel[1]=whitePixel[2] = 255;
            while (!isThin) {
                isThin = true;
                for (int i = 0; i < raster.getHeight(); i++) {
                    for (int j = 0; j < raster.getWidth(); j++) {
                        if (isBorder(j, i)) {
                            if (isDeletable(j, i)) {
                                stack.push(new int[]{j, i});
                                isThin = false;
                            }
                        }
                    }
                }
                while (!stack.isEmpty()) {
                    int[] pos = stack.pop();
                    raster.setPixel(pos[0], pos[1], whitePixel);
                }
            }
            Calendar finishTime = Calendar.getInstance();
            System.out.println("time: " + (finishTime.getTimeInMillis() - startTime.getTimeInMillis() ));
            FrmShowImage.showImageInFrame(img);
        } catch (IOException ex) {
            Logger.getLogger(ImageBone.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}

