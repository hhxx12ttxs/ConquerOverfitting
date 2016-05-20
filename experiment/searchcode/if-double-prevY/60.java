package FIT_9202_Machulskis.ImageViewer.ImageDraw;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * User: violetta
 * Date: 5/18/12
 * Time: 6:40 AM
 */
public class TrilinearImagePanel extends AbstractImagePanel
{
    ArrayList<BufferedImage> mippedImages = new ArrayList<BufferedImage>();
    public  TrilinearImagePanel(ImageModel model)
    {
        super(model);

    }

    @Override
    int getPixelColor(double x_, double y_, BufferedImage t)
    {
        int size = image.getWidth();
        double level = Math.log (((double)model.getTexture().getWidth())/(size*scaleX))/Math.log(2);
        int l = (int) Math.round(level);
        double shiftx = x_/t.getWidth();
        double shifty = y_/t.getHeight();
        if(l < 0)
        {
            l = 0;
        }
        BufferedImage t1 = mippedImages.get(l);
        BufferedImage t2 = mippedImages.get(l+1);
        
        int c1 = getPixelCol(shiftx*t1.getWidth(),shifty*t1.getHeight(), t1);
        int c2 = getPixelCol(shiftx*t2.getWidth(),shifty*t2.getHeight(), t2);
        int r1a = (c1 >>> 24) & 0xFF;
        int r1r = (c1 >>> 16) & 0xFF;
        int r1g = (c1 >>> 8) & 0xFF;
        int r1b = (c1 >>> 0) & 0xFF;

        int r2a = (c2 >>> 24) & 0xFF;
        int r2r = (c2 >>> 16) & 0xFF;
        int r2g = (c2 >>> 8) & 0xFF;
        int r2b = (c2 >>> 0) & 0xFF;
        final int cg =
                ((r1a + r2a)/2) * 0x1000000 +
                ((r1r + r2r)/2) * 0x10000 +
                ((r1g + r2g)/2) * 0x100 +
                ((r1b + r2b)/2) * 0x1;
        return cg;
    }

    public int getPixelCol(double x_, double y_, BufferedImage t)
    {
        /*   (A)-----(C)
              |       |
             (E)-(G)-(F)
              |       |
             (B)-----(D) */

        int sx = t.getWidth();
        int sy = t.getHeight();

        BufferedImage img = t;
        final double x = x_ - 0.5;
        final double y = y_ - 0.5;
        final int ix = (int)java.lang.Math.floor(x);
        final int iy = (int)java.lang.Math.floor(y);
        if( (ix+1 < 0) || (ix >= sx) || (iy+1 < 0) || (iy >= sy))
            return 0xFFFFFF;
        final boolean vx0 = ix >= 0;
        final boolean vx1 = ix+1 < sx;
        final boolean vy0 = iy >= 0;
        final boolean vy1 = iy+1 < sy;
        final double xk = x - ix;
        final double yk = y - iy;

        final int ca = vx0?(vy0?img.getRGB(ix, iy):img.getRGB(ix, 0))
                :(vy0?img.getRGB( 0, iy):img.getRGB( 0, 0));
        final int caa = (ca >>> 24) & 0xFF;
        final int car = (ca >>> 16) & 0xFF;
        final int cag = (ca >>>  8) & 0xFF;
        final int cab = (ca >>>  0) & 0xFF;

        final int cb = vx1?(vy0?img.getRGB(ix+1, iy):img.getRGB(ix+1, 0))
                :(vy0?img.getRGB(ix  , iy):img.getRGB(ix  , 0));
        final int cba = (cb >>> 24) & 0xFF;
        final int cbr = (cb >>> 16) & 0xFF;
        final int cbg = (cb >>>  8) & 0xFF;
        final int cbb = (cb >>>  0) & 0xFF;

        final int cc = vx0?(vy1?img.getRGB(ix, iy+1):img.getRGB(ix, iy))
                :(vy1?img.getRGB( 0, iy+1):img.getRGB( 0, iy));
        final int cca = (cc >>> 24) & 0xFF;
        final int ccr = (cc >>> 16) & 0xFF;
        final int ccg = (cc >>>  8) & 0xFF;
        final int ccb = (cc >>>  0) & 0xFF;

        final int cd = vx1?(vy1?img.getRGB(ix+1, iy+1):img.getRGB(ix+1, iy))
                :(vy1?img.getRGB(ix  , iy+1):img.getRGB(ix  , iy));
        final int cda = (cd >>> 24) & 0xFF;
        final int cdr = (cd >>> 16) & 0xFF;
        final int cdg = (cd >>>  8) & 0xFF;
        final int cdb = (cd >>>  0) & 0xFF;

        final int cea = (int)(caa * (1 - xk) + cba * xk);
        final int cer = (int)(car * (1 - xk) + cbr * xk);
        final int ceg = (int)(cag * (1 - xk) + cbg * xk);
        final int ceb = (int)(cab * (1 - xk) + cbb * xk);

        final int cfa = (int)(cca * (1 - xk) + cda * xk);
        final int cfr = (int)(ccr * (1 - xk) + cdr * xk);
        final int cfg = (int)(ccg * (1 - xk) + cdg * xk);
        final int cfb = (int)(ccb * (1 - xk) + cdb * xk);

        final int cga = (int)(cea * (1 - yk) + cfa * yk);
        final int cgr = (int)(cer * (1 - yk) + cfr * yk);
        final int cgg = (int)(ceg * (1 - yk) + cfg * yk);
        final int cgb = (int)(ceb * (1 - yk) + cfb * yk);

        final int cg =
                cga * 0x1000000 +
                        cgr * 0x10000 +
                        cgg * 0x100 +
                        cgb * 0x1;
        return cg;
    }

    @Override
    void textureChanged()
    {
        mippedImages.clear();
        BufferedImage original = model.getTexture();
        BufferedImage prev = original;
        int prevx = prev.getWidth();
        int prevy = prev.getHeight();
        int nextx = prevx/2;
        int nexty = prevy/2;
        while (nextx>2)
        {
            BufferedImage next = new BufferedImage(nextx, nexty, BufferedImage.TYPE_INT_RGB);
            for(int i = 0; i < nextx; i++)
            {
                for(int j = 0; j< nexty; j++)
                {
                    double shx = ((double)i)/nextx* prevx;
                    double shy = ((double) j)/( (double)nexty)*prevy;
                    next.setRGB(i, j, getPixelCol(shx, shy, prev));
                }
            }
            mippedImages.add(next);
            prevx = nextx;
            prevy = nexty;
            prev = next;
            nextx /= 2;
            nexty /= 2;
        }
        System.out.println("Generated " + mippedImages.size() + " levels");
    }
}

