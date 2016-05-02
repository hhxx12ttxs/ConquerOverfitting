/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.romppu.tutorial.chat.common;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author roman
 */
public class Tools {

    public static final long WORD_MASK = 0xffffffffffffffffL;

    private static int getSizeOf(File file, int size) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                size += getSizeOf(f, size);
            }
            return size;
        } else {
            return (int) file.length();
        }
    }

    public static int getSizeOf(File file) {
        return getSizeOf(file, 0);
    }

    public static String encodeFileURLString(String path) {
        return path.replaceAll(" ", "%20").replaceAll("\\\\", "/");
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static String cut(String str, int len) {
        if (str == null) {
            return null;
        }
        return str.substring(0, str.length() < len ? str.length() : len);
    }

    public static boolean hasMask(long word, long mask) {
        return ((word & mask) == mask);
    }

    public static boolean hasMasks(long word, long... masks) {
        for (long m : masks) {
            if ((word & m) == m) {
                return true;
            }
        }
        return false;
    }

    public static String getExt(File file) {
        String name = file.getName();
        if (name.lastIndexOf('.') == -1) {
            return ".";
        }
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public static byte[] getFileBytes(File file)
            throws IOException {
        FileInputStream in = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        in.read(bytes);
        return bytes;
    }

    public static String getPureClassName(String p_className) {
        return p_className.substring(p_className.lastIndexOf('.') + 1);
    }

    public static Integer[] toIntArray(String[] strArr) {
        Integer[] ii = new Integer[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            ii[i] = Integer.parseInt(strArr[i]);
        }
        return ii;
    }

    public static boolean equals(Object value1, Object value2) {
        if (value1 == null && value2 == null) {
            return true;
        }
        boolean different = (value1 == null && value2 != null)
                || (value1 != null && value2 == null)
                || !value1.equals(value2);
        return !different;
    }

    public static int length(String value) {
        return value == null ? Integer.MIN_VALUE : value.trim().length();
    }

    public static boolean isEmpty(java.util.List list) {
        return list == null || list.size() == 0;
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isEmpty(Integer integer) {
        return integer == null;
    }

    public static boolean nullSafeTrue(Boolean value) {
        return value != null && value;
    }

    public static boolean nullSafeEq(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null && str2 != null) {
            return false;
        }
        if (str1 != null && str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    public static boolean nullSafeEq(BigDecimal i1, BigDecimal i2) {
        if (i1 == null && i2 == null) {
            return true;
        }
        if (i1 == null && i2 != null) {
            return false;
        }
        if (i1 != null && i2 == null) {
            return false;
        }
        return i1.doubleValue() == i2.doubleValue();
    }

    public static boolean nullSafeEq(Integer i1, Integer i2) {
        if (i1 == null && i2 == null) {
            return true;
        }
        if (i1 == null && i2 != null) {
            return false;
        }
        if (i1 != null && i2 == null) {
            return false;
        }
        return i1.equals(i2);
    }

    public static boolean nullSafeEq(Long i1, Long i2) {
        if (i1 == null && i2 == null) {
            return true;
        }
        if (i1 == null && i2 != null) {
            return false;
        }
        if (i1 != null && i2 == null) {
            return false;
        }
        return i1.equals(i2);
    }

    public static void storeBounds(Preferences preferences, Rectangle rect) throws BackingStoreException {
        preferences.putInt("x", rect.x);
        preferences.putInt("y", rect.y);
        preferences.putInt("width", rect.width);
        preferences.putInt("height", rect.height);
        preferences.flush();
    }

    public static String toUpperFirst(String str) {
        if (str == null) {
            return null;
        }
        if (str.trim().length() == 0) {
            return str;
        }
        char[] chars = str.toCharArray();
        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                break;
            }
        }
        return new String(chars);
    }

    public static Rectangle restoreBounds(Preferences preferences) {
        int w = preferences.getInt("width", -1);
        int h = preferences.getInt("height", -1);
        int x = preferences.getInt("x", -1);
        int y = preferences.getInt("y", -1);
        if (w == -1 || x == -1 || w == -1 || h == -1) {
            return null;
        }
        return new Rectangle(x, y, w, h);
    }

    public static URI getFileURI(String filePath) {
        URI uri = null;
        filePath = filePath.trim();
        if (filePath.indexOf("http") == 0 || filePath.indexOf("\\") == 0) {
            if (filePath.indexOf("\\") == 0) {
                filePath = "file:" + filePath;
            }
            try {
                filePath = filePath.replaceAll(" ", "%20");
                URL url = new URL(filePath);
                uri = url.toURI();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
        } else {
            File file = new File(filePath);
            uri = file.toURI();
        }
        return uri;
    }

    /**
     * Opens local file (in Windows). Tries to use desktop API for opening. If
     * desktop API isn't supported or system doesn't have program associated
     * with given file, executes rundll call with given filename.
     *
     * @param p_file
     */
    public static void openLocalFile(File p_file) throws Exception {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Desktop.getDesktop().browse(getFileURI(p_file.getPath()));
        } throw new Exception("Desktop API is unsupported");
    }

    public static Map<String, String> readParameters(String... params) {
        HashMap<String, String> paramsMap = new HashMap();
        for (String param : params) {
            if (param.contains("=")) {
                String key = param.substring(0, param.indexOf("=")).toUpperCase();
                String value = param.substring(param.indexOf("=") + 1);
                paramsMap.put(key, value);
            } else {
                paramsMap.put(null, param);
            }
        }
        return paramsMap;
    }

    public static String getPureName(File file) {
        String name = file.getName();
        int idx = name.lastIndexOf('.');
        return name.substring(0, idx == -1 ? name.length() : idx);
    }

    public static boolean getBit(long bits, int bitIndex) {
        if (bitIndex < 0 || bitIndex > 64) {
            throw new IllegalArgumentException();
        }
        long mask = 1 << bitIndex;
        return (mask & bits) != 0 ? true : false;
    }

    public static long getBits(long bits, int bitIndex, int count) {
        long result = 0L;
        for (int i = bitIndex; i < (bitIndex + count); i++) {
            result = setBit(result, i, getBit(bits, i));
        }
        return result >> bitIndex;
    }

    public static long setBit(long bits, int bitIndex, boolean bitValue) {
        if (bitIndex < 0 || bitIndex > 64) {
            throw new IllegalArgumentException();
        }
        long mask = 1 << bitIndex;
        if (bitValue) {
            return bits | mask;
        } else {
            mask = WORD_MASK ^ mask;
            return bits & mask;
        }
    }

    public static long storeBits(long word, int bits, short from, short size) {
        long result = word;
        int current = 0;
        for (int i = from; i < (from + size); i++) {
            result = setBit(result, i, getBit(bits, current++));
        }
        return result;
    }

    public static String getFileURL(String path) throws UnsupportedEncodingException {
        if (path.startsWith("http")) {
            return path;
        }
        return "file:///" + encodeFileURLString(path);
    }

    public static Map<String, java.util.List<String>> getUrlParameters(String url)
            throws UnsupportedEncodingException {
        Map<String, java.util.List<String>> params = new HashMap<String, java.util.List<String>>();
        String[] urlParts = url.split("\\?");
        if (urlParts.length > 1) {
            String query = urlParts[1];
            for (String param : query.split("&")) {
                String pair[] = param.split("=");
                String key = URLDecoder.decode(pair[0], "UTF-8");
                String value = "";
                if (pair.length > 1) {
                    value = URLDecoder.decode(pair[1], "UTF-8");
                }
                java.util.List<String> values = params.get(key);
                if (values == null) {
                    values = new ArrayList<String>();
                    params.put(key, values);
                }
                values.add(value);
            }
        }
        return params;
    }

    public static byte[] getImageBytes(File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
        BufferedImage img = ImageIO.read(file);
        ImageIO.write(img, Tools.getExt(file), baos);
        baos.flush();
        return baos.toByteArray();
    }

    public static byte[] getImageBytes(Image image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(getBufferedImage(image), "JPEG", baos);
        return baos.toByteArray();
    }

    public static BufferedImage getBufferedImage(Image image) {
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        return bi;
    }

    public static Paint getCheckerPaint(Color c1, Color c2, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        try {
            g.setColor(c1);
            g.fillRect(0, 0, size, size);
            g.setColor(c2);
            g.fillRect(0, 0, size / 2, size / 2);
            g.fillRect(size / 2, size / 2, size / 2, size / 2);
        } finally {
            g.dispose();
        }
        return new TexturePaint(img, new Rectangle(0, 0, size, size));
    }

    public static BufferedImage createCheckerImage(Color c1, Color c2, int checksize, Dimension imageSize) {
        if (imageSize.getWidth() == 0 || imageSize.getHeight() == 0) {
            return null;
        }
        BufferedImage img = new BufferedImage(imageSize.width, imageSize.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(getCheckerPaint(c1, c2, checksize));
        g2.fillRect(0, 0, img.getWidth(), img.getHeight());
        return img;
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, double percent) {
        int srcWidth = originalImage.getWidth();
        int srcHeight = originalImage.getHeight();
        double multiplier = (percent / 100);
        int destWidth = (int) (srcWidth * multiplier);
        int destHeight = (int) (srcHeight * multiplier);
        BufferedImage resizedImage = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, destWidth, destHeight, null);
        g.dispose();
        return resizedImage;
    }

    public static BufferedImage resizeImage(Image originalImage, float scale) {
        int srcWidth = originalImage.getWidth(null);
        int srcHeight = originalImage.getHeight(null);
        int destWidth = (int) (srcWidth * scale);
        int destHeight = (int) (srcHeight * scale);
        BufferedImage resizedImage = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, destWidth, destHeight, null);
        g.dispose();
        return resizedImage;
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, float scale) {
        int srcWidth = originalImage.getWidth();
        int srcHeight = originalImage.getHeight();
        int destWidth = (int) (srcWidth * scale);
        int destHeight = (int) (srcHeight * scale);
        BufferedImage resizedImage = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, destWidth, destHeight, null);
        g.dispose();
        return resizedImage;
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, Dimension dimension) {
        return resizeImage(originalImage, dimension.width, dimension.height);
    }

    public static BufferedImage resizeImage(Image originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int width) {
        int height = width * originalImage.getHeight(null) / originalImage.getWidth(null);
        return resizeImage(originalImage, width, height);
    }

    public static BufferedImage resizeImage(Image originalImage, int width) {
        int height = width * originalImage.getHeight(null) / originalImage.getWidth(null);
        return resizeImage(originalImage, width, height);
    }
}

