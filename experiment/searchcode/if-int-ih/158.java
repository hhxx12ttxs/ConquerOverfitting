package org.nutz.img;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;

/**
 * ???????? API
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class Images {
    /**
     * ?????????
     * 
     * @param srcIm
     *            ?????
     * @param taIm
     *            ????????
     * @param degree
     *            ????, 90 ???????? -90 ???????
     * @return ????????
     */
    public static BufferedImage rotate(Object srcIm, File taIm, int degree) {
        BufferedImage im = Images.read(srcIm);
        BufferedImage im2 = Images.rotate(im, degree);
        Images.write(im2, taIm);
        return im2;
    }

    /**
     * ?????????
     * 
     * @param srcPath
     *            ???????
     * @param taPath
     *            ??????????
     * @param degree
     *            ????, 90 ???????? -90 ???????
     * @return ????????
     */
    public static BufferedImage rotate(String srcPath, String taPath, int degree)
            throws IOException {
        File srcIm = Files.findFile(srcPath);
        if (null == srcIm)
            throw Lang.makeThrow("Fail to find image file '%s'!", srcPath);

        File taIm = Files.createFileIfNoExists(taPath);
        return rotate(srcIm, taIm, degree);
    }

    /**
     * ?????????
     * 
     * @param image
     *            ??
     * @param degree
     *            ????, 90 ???????? -90 ???????
     * @return ????????
     */
    public static BufferedImage rotate(BufferedImage image, int degree) {
        int iw = image.getWidth();// ???????
        int ih = image.getHeight();// ???????
        int w = 0;
        int h = 0;
        int x = 0;
        int y = 0;
        degree = degree % 360;
        if (degree < 0)
            degree = 360 + degree;// ??????0-360???
        double ang = degree * 0.0174532925;// ???????

        /**
         * ??????????????
         */

        if (degree == 180 || degree == 0 || degree == 360) {
            w = iw;
            h = ih;
        } else if (degree == 90 || degree == 270) {
            w = ih;
            h = iw;
        } else {
            int d = iw + ih;
            w = (int) (d * Math.abs(Math.cos(ang)));
            h = (int) (d * Math.abs(Math.sin(ang)));
        }

        x = (w / 2) - (iw / 2);// ??????
        y = (h / 2) - (ih / 2);
        BufferedImage rotatedImage = new BufferedImage(w, h, image.getType());
        Graphics gs = rotatedImage.getGraphics();
        gs.fillRect(0, 0, w, h);// ???????????????
        AffineTransform at = new AffineTransform();
        at.rotate(ang, w / 2, h / 2);// ????
        at.translate(x, y);
        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        op.filter(image, rotatedImage);
        image = rotatedImage;
        return image;
    }

    /**
     * ???????????????????????<br />
     * ???????????????<br />
     * ????????????<b>-1</b>????????????????????????????????
     * <p>
     * ?????? png | gif | jpg | bmp | wbmp
     * 
     * @param srcIm
     *            ???????
     * @param taIm
     *            ????????
     * @param w
     *            ??
     * @param h
     *            ??
     * @param bgColor
     *            ????
     * 
     * @return ?????????
     * 
     * @throws IOException
     *             ??????????
     */
    public static BufferedImage zoomScale(Object srcIm, File taIm, int w, int h, Color bgColor)
            throws IOException {
        BufferedImage old = read(srcIm);
        BufferedImage im = Images.zoomScale(old, w, h, bgColor);
        write(im, taIm);
        return old;
    }

    /**
     * ???????????????????????<br />
     * ???????????????<br />
     * ????????????<b>-1</b>????????????????????????????????
     * <p>
     * ?????? png | gif | jpg | bmp | wbmp
     * 
     * @param srcPath
     *            ?????
     * @param taPath
     *            ????????????????
     * @param w
     *            ??
     * @param h
     *            ??
     * @param bgColor
     *            ????
     * 
     * @return ?????????
     * 
     * @throws IOException
     *             ??????????
     */
    public static BufferedImage zoomScale(String srcPath, String taPath, int w, int h, Color bgColor)
            throws IOException {
        File srcIm = Files.findFile(srcPath);
        if (null == srcIm)
            throw Lang.makeThrow("Fail to find image file '%s'!", srcPath);

        File taIm = Files.createFileIfNoExists(taPath);
        return zoomScale(srcIm, taIm, w, h, bgColor);
    }

    /**
     * ??????????????????????????<br />
     * ????????????<b>-1</b>????????????????????????????????
     * 
     * @param im
     *            ????
     * @param w
     *            ??
     * @param h
     *            ??
     * @param bgColor
     *            ????
     * 
     * @return ???????
     */
    public static BufferedImage zoomScale(BufferedImage im, int w, int h, Color bgColor) {
        if (w == -1 || h == -1) {
            return zoomScale(im, w, h);
        }

        // ??????
        bgColor = null == bgColor ? Color.black : bgColor;
        // ????
        int oW = im.getWidth();
        int oH = im.getHeight();
        float oR = (float) oW / (float) oH;
        float nR = (float) w / (float) h;

        int nW, nH, x, y;
        /*
         * ??
         */
        // ????????????????????????
        if (oR > nR) {
            nW = w;
            nH = (int) (((float) w) / oR);
            x = 0;
            y = (h - nH) / 2;
        }
        // ????
        else if (oR < nR) {
            nH = h;
            nW = (int) (((float) h) * oR);
            x = (w - nW) / 2;
            y = 0;
        }
        // ????
        else {
            nW = w;
            nH = h;
            x = 0;
            y = 0;
        }

        // ????
        BufferedImage re = new BufferedImage(w, h, ColorSpace.TYPE_RGB);
        // ????????
        Graphics gc = re.getGraphics();
        gc.setColor(bgColor);
        gc.fillRect(0, 0, w, h);
        gc.drawImage(im, x, y, nW, nH, bgColor, null);
        // ??
        return re;
    }

    /**
     * ??????????
     * 
     * @param im
     *            ????
     * @param w
     *            ??
     * @param h
     *            ??
     * 
     * @return ???????
     */
    public static BufferedImage zoomScale(BufferedImage im, int w, int h) {
        // ????
        int oW = im.getWidth();
        int oH = im.getHeight();

        int nW = w, nH = h;

        /*
         * ??
         */
        // ???????????????????
        if (h == -1) {
            nH = (int) ((float) w / oW * oH);
        }
        // ???????????????????
        else if (w == -1) {
            nW = (int) ((float) h / oH * oW);
        }

        // ????
        BufferedImage re = new BufferedImage(nW, nH, ColorSpace.TYPE_RGB);
        re.getGraphics().drawImage(im, 0, 0, nW, nH, null);
        // ??
        return re;
    }

    /**
     * ?????????????????????????????????
     * <p>
     * ?????? png | gif | jpg | bmp | wbmp
     * 
     * @param srcIm
     *            ???????
     * @param taIm
     *            ????????
     * @param w
     *            ??
     * @param h
     *            ??
     * @return ?????????
     * 
     * @throws IOException
     *             ??????????
     */
    public static BufferedImage clipScale(Object srcIm, File taIm, int w, int h) throws IOException {
        BufferedImage old = read(srcIm);
        BufferedImage im = Images.clipScale(old, w, h);
        write(im, taIm);
        return old;
    }

    /**
     * ?????????????????????????????????
     * <p>
     * ?????? png | gif | jpg | bmp | wbmp
     * 
     * @param srcPath
     *            ?????
     * @param taPath
     *            ????????????????
     * @param w
     *            ??
     * @param h
     *            ??
     * 
     * @return ?????????
     * 
     * @throws IOException
     *             ??????????
     */
    public static BufferedImage clipScale(String srcPath, String taPath, int w, int h)
            throws IOException {
        File srcIm = Files.findFile(srcPath);
        if (null == srcIm)
            throw Lang.makeThrow("Fail to find image file '%s'!", srcPath);

        File taIm = Files.createFileIfNoExists(taPath);
        return clipScale(srcIm, taIm, w, h);
    }

    /**
     * ????????????????????
     * <p>
     * ??????????????????????????????????
     * 
     * @param im
     *            ????
     * @param w
     *            ??
     * @param h
     *            ??
     * @return ???????
     */
    public static BufferedImage clipScale(BufferedImage im, int w, int h) {
        // ????
        int oW = im.getWidth();
        int oH = im.getHeight();
        float oR = (float) oW / (float) oH;
        float nR = (float) w / (float) h;

        int nW, nH, x, y;
        /*
         * ??
         */
        // ????????????????????????
        if (oR > nR) {
            nW = (h * oW) / oH;
            nH = h;
            x = (w - nW) / 2;
            y = 0;
        }
        // ????
        else if (oR < nR) {
            nW = w;
            nH = (w * oH) / oW;
            x = 0;
            y = (h - nH) / 2;
        }
        // ????
        else {
            nW = w;
            nH = h;
            x = 0;
            y = 0;
        }
        // ????
        BufferedImage re = new BufferedImage(w, h, ColorSpace.TYPE_RGB);
        re.getGraphics().drawImage(im, x, y, nW, nH, Color.black, null);
        // ??
        return re;
    }

    /**
     * ???????????
     * 
     * @param img
     *            ????
     * @return ????
     */
    public static BufferedImage read(Object img) {
        try {
            if (img instanceof File)
                return ImageIO.read((File) img);
            else if (img instanceof URL)
                img = ((URL) img).openStream();
            if (img instanceof InputStream) {
                File tmp = File.createTempFile("nutz_img", ".jpg");
                Files.write(tmp, (InputStream)img);
                tmp.deleteOnExit();
                return read(tmp);
            }
            throw Lang.makeThrow("Unkown img info!! --> " + img);
        }
        catch (IOException e) {
            try {
                    InputStream in = null;
                    if (img instanceof File)
                        in = new FileInputStream((File)img);
                    else if (img instanceof URL)
                        in = ((URL)img).openStream();
                    else if (img instanceof InputStream)
                        in = (InputStream)img;
                    if (in != null)
                        return readJpeg(in);
            } catch (IOException e2) {
                e2.fillInStackTrace();
            }
            return null;
            //throw Lang.wrapThrow(e);
        }
    }

    /**
     * ??????????????
     * 
     * @param im
     *            ????
     * @param targetFile
     *            ??????????????????????
     */
    public static void write(RenderedImage im, File targetFile) {
        try {
            ImageIO.write(im, Files.getSuffixName(targetFile), targetFile);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    /**
     * ???? JPG ??
     * 
     * @param im
     *            ????
     * @param targetJpg
     *            ???? JPG ????
     * @param quality
     *            ?? 0.1f ~ 1.0f
     */
    public static void writeJpeg(RenderedImage im, File targetJpg, float quality) {
        try {
            ImageWriter writer = ImageIO.getImageWritersBySuffix("jpg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            ImageOutputStream os = ImageIO.createImageOutputStream(targetJpg);
            writer.setOutput(os);
            writer.write((IIOMetadata) null, new IIOImage(im, null, null), param);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    /**
     * ????JPEG???????,???32??jpeg??
     * <p/>
     * ??: http://stackoverflow.com/questions/2408613/problem-reading-jpeg-image-using-imageio-readfile-file
     * 
     * */
    private static BufferedImage readJpeg(InputStream in) throws IOException {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("JPEG");
        ImageReader reader = null;
        while(readers.hasNext()) {
            reader = (ImageReader)readers.next();
            if(reader.canReadRaster()) {
                break;
            }
        }
        ImageInputStream input = ImageIO.createImageInputStream(in);
        reader.setInput(input);
        //Read the image raster
        Raster raster = reader.readRaster(0, null); 
        BufferedImage image = createJPEG4(raster);
        File tmp = File.createTempFile("nutz.img", "jpg"); //??????,???????
        writeJpeg(image, tmp, 1);
        return read(tmp);
    }
    
      /**                                                                                                                                           
    Java's ImageIO can't process 4-component images                                                                                             
    and Java2D can't apply AffineTransformOp either,                                                                                            
    so convert raster data to RGB.                                                                                                              
    Technique due to MArk Stephens.                                                                                                             
    Free for any use.                                                                                                                           
  */
    private static BufferedImage createJPEG4(Raster raster) {
        int w = raster.getWidth();
        int h = raster.getHeight();
        byte[] rgb = new byte[w * h * 3];
      
        float[] Y = raster.getSamples(0, 0, w, h, 0, (float[]) null);
        float[] Cb = raster.getSamples(0, 0, w, h, 1, (float[]) null);
        float[] Cr = raster.getSamples(0, 0, w, h, 2, (float[]) null);
        float[] K = raster.getSamples(0, 0, w, h, 3, (float[]) null);

        for (int i = 0, imax = Y.length, base = 0; i < imax; i++, base += 3) {
            float k = 220 - K[i], y = 255 - Y[i], cb = 255 - Cb[i],
                    cr = 255 - Cr[i];

            double val = y + 1.402 * (cr - 128) - k;
            val = (val - 128) * .65f + 128;
            rgb[base] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff
                    : (byte) (val + 0.5);

            val = y - 0.34414 * (cb - 128) - 0.71414 * (cr - 128) - k;
            val = (val - 128) * .65f + 128;
            rgb[base + 1] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff
                    : (byte) (val + 0.5);

            val = y + 1.772 * (cb - 128) - k;
            val = (val - 128) * .65f + 128;
            rgb[base + 2] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff
                    : (byte) (val + 0.5);
        }


        raster = Raster.createInterleavedRaster(new DataBufferByte(rgb, rgb.length), w, h, w * 3, 3, new int[]{0, 1, 2}, null);

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel cm = new ComponentColorModel(cs, false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        return new BufferedImage(cm, (WritableRaster) raster, true, null);
    }
}

