/* =============================================================================
		   Copyright 2008 ImaginaWorks Software Factory, S.L.

	   Licensed under the Apache License, Version 2.0 (the "License");
	   you may not use this file except in compliance with the License.
	   You may obtain a copy of the License at

	       http://www.apache.org/licenses/LICENSE-2.0

	   Unless required by applicable law or agreed to in writing, software
	   distributed under the License is distributed on an "AS IS" BASIS,
	   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	   See the License for the specific language governing permissions and
	   limitations under the License.
================================================================================= */
package com.imaginaworks.util;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import javax.activation.FileDataSource;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author nacho
 */
public class ImageUtils {
    private static final String IMAGE_JPEG = "image/jpeg";

    private static HttpClient httpClient = null;

    /*
     *
     */
    public static boolean isJpegImage(File file) {
        String mimeType = new FileDataSource(file).getContentType();
        return IMAGE_JPEG.equals(mimeType);
    }

    /**
     * @param original
     * @param dest
     * @param qualityPercent
     * @param screenWidth
     * @param screenHeight
     * @throws IOException
     */
    public static void selectBestFit(File original, File dest, int qualityPercent, int screenWidth, int screenHeight) throws IOException {
        ImageInfo iInfo = new ImageInfo();
        Image img = new ImageIcon(original.getCanonicalPath()).getImage();
        iInfo.setInput(new FileInputStream(original));
        /*
         * Metemos aire en la imagen (barras negras arriba y abajo) si
         * el alto de la imagen es menor del (100 * FACTOR_AIRE)% del
         * de la pantalla.
         */
        final double FACTOR_AIRE = .75;//75%

        if (!iInfo.check()) {
            throw new IOException("Formato no soportado!");
        }

        int imgWidth = iInfo.getWidth();
        int imgHeight = iInfo.getHeight();
        Log.trace(ImageUtils.class, "Imagen original: " + imgWidth + "x" + imgHeight);
        // 1. si la pantalla es ms pequea que la imagen, escalamos
        if (imgWidth > screenWidth) {
            Log.trace(ImageUtils.class, "Pantalla ms pequea que la imagen: escalando...");
            createThumb(original, dest, qualityPercent, screenWidth);
        } else if ((double) imgHeight / (double) screenHeight < FACTOR_AIRE) {
            Log.trace(ImageUtils.class, "Imagen mucho ms pequea que la pantalla: aumentamos y metemos aire.");
            insertTopDownSpace(original, dest, qualityPercent, screenWidth, screenHeight, 15);
        }
        // 2. si la pantalla es ms grande, metemos aire arriba y abajo
        else {
            Log.trace(ImageUtils.class, "Imagen ms pequea que la pantalla: aumentamos sin meter aire.");
            insertTopDownSpace(original, dest, qualityPercent, screenWidth, screenHeight, 0);
        }

//        double imgRatio = imgWidth / imgHeight;
//        double scrRatio = screenWidth / screenHeight;
    }

    /**
     * @param url
     * @param dest
     * @param qualityPercent
     * @param screenWidth
     * @param screenHeight
     * @throws IOException
     */
    public static void selectBestFit(String url, File dest, int qualityPercent, int screenWidth, int screenHeight) throws IOException {
        File o = downloadImage(url);
        selectBestFit(o, dest, qualityPercent, screenHeight, screenWidth);
    }

    /**
     * @param original
     * @param dest
     * @param qualityPercent
     * @param screenWidth
     * @param screenHeight
     * @param spacePercent
     * @throws IOException
     */
    public static void insertTopDownSpace(File original, File dest, int qualityPercent, int screenWidth,
                                          int screenHeight, int spacePercent) throws IOException {
        ImageInfo iInfo = new ImageInfo();

        // 1. cargar imagen
        Image img = new ImageIcon(original.getCanonicalPath()).getImage();
        iInfo.setInput(new FileInputStream(original));

        if (!iInfo.check()) {
            throw new IOException("Formato no soportado!");
        }

        int imgWidth = iInfo.getWidth();
        int imgHeight = iInfo.getHeight();

        int topMargin = (screenHeight > imgHeight) ? screenHeight * spacePercent / 200 : 0;
        int effectiveScreenHeight = (screenHeight > imgHeight) ? screenHeight - 2 * topMargin : screenHeight;

        BufferedImage bI = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bI.createGraphics();
        g.drawImage(img, 0, 0, imgWidth, imgHeight, null);
        // 2. Calcular el escalado necesario en la imagen
        /*
         * Si la imagen es ms grande que la pantalla la recortamos.
         */
        if (imgWidth > screenWidth && imgHeight > effectiveScreenHeight) {
            int y0 = (imgWidth > screenWidth) ? (imgWidth - screenWidth) / 2 : 0;
            int x0 = (imgHeight > effectiveScreenHeight) ? (imgHeight - effectiveScreenHeight) / 2 : 0;
            int w = imgWidth - 2 * x0;
            int h = imgHeight - 2 * y0;
            bI = bI.getSubimage(x0, y0, w, h);
        }//si la pantalla es mas ancha
        else if (screenWidth > imgWidth && effectiveScreenHeight <= imgHeight) {
//            int thumbWidth = imgWidth > maxWidth ? maxWidth : imgWidth;
//            int thumbHeight = (int) Math.round(((double) imgHeight / (double) imgWidth) * (double) thumbWidth);

        }//si la pantalla es mas alta
        else {
            //1. escalar la imagen hasta que alcance el alto de la pantalla.
            int newHeight = effectiveScreenHeight;
            int newWidth = (int) Math.round(((double) (newHeight * imgWidth) / (double) imgHeight));
            bI = scaleImage(bI, newHeight, newWidth);

            //2. recortar los laterales.
            int x0 = (newWidth - screenWidth) / 2;
            bI = bI.getSubimage((x0 > 0) ? x0 : 0, 0, Math.min(screenWidth, newWidth), newHeight);
        }

        // 3. crear el nuevo objeto que guardar la miniatura
        BufferedImage reducedImage = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = createGraphics(reducedImage);
        // img.getGraphics().clipRect()
        graphics2D.drawImage(bI, 0, topMargin, screenWidth, effectiveScreenHeight, null);

        Log.trace(null, "Imagen resultante: " + reducedImage.getWidth() + "x" + reducedImage.getHeight());
        // 4. guardar la imagen en el fichero
        writeImgToFile(reducedImage, dest, qualityPercent);

    }

    /**
     * @param reducedImage
     * @param dest
     * @param qualityPercent
     * @throws IOException
     */
    private static void writeImgToFile(BufferedImage reducedImage, File dest, float qualityPercent) throws IOException {
//        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
//        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
//        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(reducedImage);
//        param.setQuality((float) qualityPercent / 100.0f, true);
//        encoder.setJPEGEncodeParam(param);
//        encoder.encode(reducedImage);
//        out.close();
        Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = (ImageWriter) iter.next();
// instantiate an ImageWriteParam object with default compression options

        ImageWriteParam iwp = writer.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionQuality((float) qualityPercent / 100.0f);

        FileImageOutputStream output = new FileImageOutputStream(dest);
        writer.setOutput(output);
        IIOImage image = new IIOImage(reducedImage, null, null);
        writer.write(null, image, iwp);
        writer.dispose();
    }

    /**
     * @param img
     * @param newHeight
     * @param newWidth
     */
    private static BufferedImage scaleImage(Image img, int newHeight, int newWidth) {
        BufferedImage buff = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2D = createGraphics(buff);
        g2D.drawImage(img, 0, 0, newWidth, newHeight, null);
        return buff;
    }

    /**
     * @param im
     * @return
     */
    private static Graphics2D createGraphics(BufferedImage im) {
        Graphics2D g2D = im.createGraphics();
        Map hints = new HashMap();
        hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g2D.setRenderingHints(hints);
        return g2D;
    }


    /**
     * @param url
     * @param dest
     * @param qualityPercent
     * @param screenWidth
     * @param screenHeight
     * @param spacePercent   tamao de las bandas a insertar (en % respecto al alto de la
     *                       pantalla).
     * @throws IOException
     */
    public static void insertTopDownSpace(String url, File dest, int qualityPercent, int screenWidth, int screenHeight,
                                          int spacePercent) throws IOException {
        File orig = downloadImage(url);
        insertTopDownSpace(orig, dest, qualityPercent, screenWidth, screenHeight, spacePercent);
    }

    /**
     * Crea una miniatura de la imagen proporcionada.
     *
     * @param original       fichero q contiene la imagen original.
     * @param dest           fichero donde almacenar la miniatura.
     * @param qualityPercent porcentaje de calidad deseado.
     * @param maxWidth       ancho lmite de la miniatura. Se respetar la proporcin.
     */
    public static void createThumb(File original, File dest, int qualityPercent, int maxWidth) {
        try {
            ImageInfo iInfo = new ImageInfo();

            // 1. cargar imagen
            Image img = new ImageIcon(original.getCanonicalPath()).getImage();
            iInfo.setInput(new FileInputStream(original));

            if (!iInfo.check()) {
                throw new IOException("Formato no soportado!");
            }

            int imgWidth = iInfo.getWidth();
            int imgHeight = iInfo.getHeight();

            int thumbWidth = imgWidth > maxWidth ? maxWidth : imgWidth;
            int thumbHeight = (int) Math.round(((double) imgHeight / (double) imgWidth) * (double) thumbWidth);

            Log.trace(ImageUtils.class, "Escalando de " + imgWidth + "x" + imgHeight + " a " + thumbWidth + "x" + thumbHeight);
            // 2. crear el nuevo objeto que guardar la miniatura
            BufferedImage reducedImage = scaleImage(img, thumbHeight, thumbWidth);

            // 4. guardar la miniatura en el fichero
            writeImgToFile(reducedImage, dest, qualityPercent);
        } catch (Exception x) {
            Log.trace(ImageUtils.class, "Error al crear la imagen: ");
            x.printStackTrace();
        }

    }

    /**
     * Crea una miniatura de la imagen proporcionada.
     *
     * @param original       fichero q contiene la imagen original.
     * @param dest           fichero donde almacenar la miniatura.
     * @param qualityPercent porcentaje de calidad deseado.
     * @param thumbWidth     ancho lmite de la miniatura. Se respetar la proporcin.
     * @throws IOException
     */
    public static void createThumb(File original, File dest, int qualityPercent, int thumbWidth, int thumbHeight) {
        try {
            ImageInfo iInfo = new ImageInfo();

            // 1. cargar imagen
            Image img = new ImageIcon(original.getCanonicalPath()).getImage();
            iInfo.setInput(new FileInputStream(original));

            if (!iInfo.check()) {
                throw new IOException("Formato no soportado!");
            }

            BufferedImage reducedImage = scaleImage(img, thumbHeight, thumbWidth);

            // 4. guardar la miniatura en el fichero
            writeImgToFile(reducedImage, dest, qualityPercent);
        } catch (Exception x) {
            Log.trace(ImageUtils.class, "Error al crear la imagen: ");
            x.printStackTrace();
        }
    }

    /**
     * @param originalURL
     * @param dest
     * @param qualityPercent
     * @param maxWidth
     * @throws IOException
     */
    public static void createThumb(String originalURL, File dest, int qualityPercent, int maxWidth) {
        try {
            File f = downloadImage(originalURL);
            createThumb(f, dest, qualityPercent, maxWidth);

        } catch (Exception x) {
            Log.trace(ImageUtils.class, "Error al crear la imagen: ");
            x.printStackTrace();
        }

    }

    /**
     * @param originalURL
     * @param dest
     * @param qualityPercent
     * @param width
     * @param height
     * @throws IOException
     */
    public static void createThumb(String originalURL, File dest, int qualityPercent, int width, int height) {
        try {
            File f = downloadImage(originalURL);
            createThumb(f, dest, qualityPercent, width, height);
        } catch (Exception x) {
            Log.trace(ImageUtils.class, "Error al crear la imagen: ");
            x.printStackTrace();
        }
    }

    /**
     * @param originalURL
     * @return
     * @throws IOException
     */
    public static File downloadImage(String originalURL) throws IOException {
        Log.trace(ImageUtils.class, "Descargando " + originalURL);
        // conservamos la extensin original del fichero.
        File f = File.createTempFile("mister-i", originalURL.substring(originalURL.lastIndexOf('.')));
        GetMethod get = new GetMethod(originalURL);
        getHttpClient().executeMethod(get);

        InputStream is = get.getResponseBodyAsStream();
        OutputStream out = new FileOutputStream(f);
        int b = -1;
        while ((b = is.read()) != -1) {
            out.write(b);
        }
        is.close();
        out.close();
        get.releaseConnection();
        return f;
    }

    /**
     * @return
     */
    private static HttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new HttpClient();
        }
        return httpClient;
    }
}

