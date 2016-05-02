/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hanyuu.managers.pictures;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import utils.CaptchaUtils;

import java.util.TreeSet;
import hanyuu.net.wipe.AbstractWipe;
import config.Config;
import config.WorkMode;
import hanyuu.managers.ThreadManager;
import utils.Constants;

/**
 *
 * @author Hanyuu Furude
 */
public class ImgManager extends TreeSet<Pic> implements Constants {

    private int FontSize = Config.fontSize;
    private File filesDir;
    private TreeSet<File> sendedFiles;
    private int sendedCount = 0;
    private File[] filesInDir;
    private ThreadManager tm;

    public ImgManager(String path, ThreadManager tm) {
        this.tm = tm;
        filesDir = new File(path);
        if (filesDir.isDirectory()) {
            filesInDir = filesDir.listFiles();
            if (Config.picsPuck) {
                sendedFiles = new TreeSet<File>();
            }
        }

    }

    @SuppressWarnings({"unchecked", "static-access", "CallToThreadDumpStack"})
    public synchronized void reImg(AbstractWipe wipe) {

        if (Config.picsNotEdit) {
            return;
        }
        if (filesDir == null) {

            wipe.destroy("No Files! filesDir in null.");
        }
        if ((Config.workMode == WorkMode.OnZeroPage) && (Config.useTmp || Config.randomPicGenerate) && (wipe.getFile() != null && wipe.getFile().exists())) {
            return;
        }

        Pic pic = getFile(wipe);
        try {
            BufferedImage bi = pic.bi;

            if (!Config.useTmp && contains(pic)) {
                wipe.getThread().sleep(300);
                wipe.setFile(getFile(wipe));
                reImg(wipe);
            } else {

                if (!Config.useTmp && !Config.randomPicGenerate) {
                    add(pic);
                } else {
                    pic = createTmpCopy(pic);
                }
                if (Config.pasteOnPic) {
                    bi = pasteToImg(wipe, bi);
                }
                if (!Config.dontEditPixels) {
                    bi = trashPixels(bi);
                }
                ImageIO.write(bi, pic.format, new FileOutputStream(pic));
                if (!Config.useTmp) {
                    remove(pic);
                }
            }
            wipe.setFile(pic);
            bi = null;
            pic = null;
            System.gc();
        } catch (Exception e) {
            wipe.setException(e);
            wipe.getThreadManager().handleError(wipe);
        }
    }

    private BufferedImage trashPixels(BufferedImage bi) {
        int y, x;
        int h = bi.getHeight();
        int w = bi.getWidth();
        for (int i = 0; i <= Config.pixelEdit; i++) {
            y = random.nextInt(h);
            x = random.nextInt(w);
            int rgb = bi.getRGB(x, y);
            if (Config.colorRndRnd) {
                if (random.nextBoolean()) {
                    bi.setRGB(x, y, rgb - random.nextInt());
                } else {
                    bi.setRGB(x, y, rgb + random.nextInt());
                }
            } else {
                bi.setRGB(x, y, rgb + Config.colorRnd);
            }
        }
       /* h=h+random.nextInt(4);
        w=w+random.nextInt(4);
        bi=CaptchaUtils.resize(bi, w, h);*/
        return bi;
    }

    private Pic createTmpCopy(Pic pic) {
        try {
            File tmpPic;
            byte[] buf;
            tmpPic = File.createTempFile("___", "." + pic.format);
            tmpPic.deleteOnExit();
            FileInputStream in = new FileInputStream(pic);
            int streamSize = in.available();
            buf = new byte[streamSize];
            in.read(buf);
            in.close();
            FileOutputStream out = new FileOutputStream(tmpPic);

            if (Config.randomBytes) {
                buf = addTrash(buf);
            }

            out.write(buf);
            out.close();
            if (Config.randomPicGenerate) {
                pic.delete();
            }
            return new Pic(tmpPic);
        } catch (Exception e) {
            return pic;
        }
    }

    public void setUsedFile(File f) {
        synchronized (lock) {
            if (!Config.picsPuck || f == null || sendedFiles.contains(f)) {
                return;
            }
            sendedFiles.add(f);
        }
    }

    public boolean isPathDir() {
        return filesDir.isDirectory();
    }

    public int filesCountInDir() {
        return filesInDir.length;
    }

    private Color getRandomColor() {
        return new Color(getRandomXY(0, 255), getRandomXY(0, 255), getRandomXY(0, 255));
    }

    private BufferedImage createImg() {

        int x = getRandomXY(640, 1280);
        int y = getRandomXY(640, 1280);
        BufferedImage bufferedImage = new BufferedImage(x, y,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = bufferedImage.createGraphics();



        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        rh.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setRenderingHints(rh);

        GradientPaint gp = new GradientPaint(getRandomXY(0, 1200), getRandomXY(0, 1200),
                getRandomColor(), getRandomXY(0, 1200), getRandomXY(0, 1200), getRandomColor(), true);

        g2d.setPaint(gp);
        g2d.fillRect(0, 0, x, y);
        g2d.setColor(getRandomColor());

        return bufferedImage;
    }

    public Pic getFile(AbstractWipe wipe) {
        Pic p = null;
        if (Config.randomPicGenerate) {
            BufferedImage gen = createImg();
            String path;
            if (filesDir.exists()) {
                path = Config.path + "/generated/";
            } else {
                path = "./generated/";
            }
            File pa = new File(path);
            if (!pa.exists()) {
                pa.mkdir();
            }
            File generated = new File(path + File.separator + random.nextInt() + ".jpg");
            try {
                //Ёбанное говно и жуткий костыль.
                    /*if(Config.randomPicGenerate&&wipe.getFile().exists())
                {
                wipe.getFile().delete();
                }*/
                ImageIO.write(gen, "jpg", generated);
            } catch (Exception e) {
                wipe.setException(e);
                wipe.getThreadManager().handleError(wipe);
                return null;
            }
            p = new Pic(generated);
            p.delete();
            return p;
        }
        if (filesDir == null) {
            wipe.destroy("No Files! filesDir in null.");
        }

        if (Config.picsPuck && sendedFiles == null) {
            sendedFiles = new TreeSet<File>();
        }

        if (Config.picsPuck && (tm.getAllSuccessful() == filesInDir.length)) {
            wipe.getUI().logError("Поток " + wipe.toString()
                    + " Остновлен. Кончились пикчи в паке.");
            wipe.destroy("Кончились пикчи в паке.");
            return null;
        }
        if (filesDir.isFile()) {
            try {
                p = new Pic(filesDir);
            } catch (Exception e) {
                wipe.setException(e);
                wipe.getThreadManager().handleError(wipe);
                return null;
            }
        }
        File f = getFile();
        if (Config.picsPuck && sendedFiles.contains(f)) {
            getFile(wipe);
        }
        if (f.length() > Config.maxFileSize || f.length() == 0) {
            if (Config.randomPicGenerate) {
                f.delete();
            }
            getFile(wipe);
        }
        if (!f.isFile()) {
            getFile(wipe);
        }
        p = new Pic(f);

        return p;
    }

    public File getFile() {
        File f;
        if(Config.onePic)
            return new File(Config.path);
        if (Config.picsPuck) {
            f = filesInDir[sendedCount];
        } else {

            f = filesInDir[random.nextInt(filesInDir.length)];

        }
        return f;
    }

    private BufferedImage pasteToImg(AbstractWipe ww, BufferedImage bi) throws Exception {
        if (ww.getMsg().length() < 1) {
            return bi;
        }
        TreeSet<AttributedString> lines = new TreeSet<AttributedString>();
        String tmp = "";
        int x, y;
        x = Config.startX;
        y = Config.startY;

        Color color = new Color(Config.picsTxtR, Config.picsTxtG, Config.picsTxtB);
        Graphics g = bi.getGraphics();
        for (char c : ww.getMsg().toCharArray()) {
            if (c != '\n') {
                tmp += String.valueOf(c);
                if (checkStringLength(tmp, bi)) {
                    int index = tmp.lastIndexOf(" ");
                    if (index < 1) {
                        index = Math.abs(tmp.length() / 2);
                    }
                    lines.add(createAttributedString(tmp.substring(0, index), color));
                    tmp = tmp.substring(index + 1);
                }
            } else if (tmp.length() > 0) {
                lines.add(createAttributedString(tmp, color));
                tmp = "";
            }
        }

        for (AttributedString as : lines) {
            /*if (y > bi.getWidth())
            {
            FontSize-=4;
            pasteToImg(ww,ImageIO.read(ww.getFile()));
            break;
            }*/
            //System.out.println(as.getIterator().toString());
            //g.setXORMode(new Color(bi.getRGB(Config.startX, y)));
            g.drawString(as.getIterator(), x, y);
            y += Config.deltaY;
        }
        lines = null;
        return bi;
    }

    private AttributedString createAttributedString(String tmp, Color c) {
        AttributedString as = new AttributedString(tmp);
        as.addAttribute(TextAttribute.FOREGROUND, c);
        as.addAttribute(TextAttribute.FONT, new Font(Config.FontName, Font.BOLD, FontSize));
        return as;
    }

    private boolean checkStringLength(String str, BufferedImage bi) {
        return (str.length() - (bi.getWidth() / FontSize)) >= 15;
    }

    public int getRandomXY(int min, int max) {
        return min + (int) Math.floor(random.nextDouble() * (max - min + 1));
    }

    private byte[] addTrash(byte[] buf) {

        byte[] trash = new byte[2048];
        random.nextBytes(trash);
        int size = buf.length;
        byte[] tmp = new byte[size + trash.length];
        System.arraycopy(buf, 0, tmp, 0, size);
        System.arraycopy(trash, 0, tmp, size, trash.length);
        /*for (int i = 0; i < trash.length; i++) {
        tmp[size++] = trash[i];
        }*/
        return tmp;

    }
}

