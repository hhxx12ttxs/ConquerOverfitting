package FIT_9202_Machulskis.Plotter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class LemniscatePanel extends JPanel
{

    public final static int DEFAULT_R = 10;
    public final static int MIN_R = -1000;
    public final static int MAX_R = 1000;

    public final static int DEFAULT_X1 = -10;
    public final static int DEFAULT_X2 = 10;
    public final static int DEFAULT_Y1 = 0;
    public final static int DEFAULT_Y2 = 0;
    public final static int MIN_X = -1000;
    public final static int MAX_X = 1000;

    public final static int MINIMAL_SIZE = 0;

    BufferedImage image;

    int width = 0;
    int height = 0;
    private Point firstFocus = new Point(DEFAULT_X1, DEFAULT_Y1);
    private Point secondFocus = new Point(DEFAULT_X2, DEFAULT_Y2);
    private double R = DEFAULT_R;
    Point center;

    /*Point (0,0) neighbors sorted by clockwise*/
    private static final Point[] NEIGHBORS = new Point[]{new Point(0, 1), new Point(1, 1), new Point(1, 0), new Point(1, -1), new Point(0, -1), new Point(-1, -1), new Point(-1, 0),new Point(-1, 1) };


    LemniscatePanel(int r)
    {
        R = r;
    }

    public void setFirstFocus(Point p)
    {
        firstFocus = p;
        repaint();
    }

    public void setSecondFocus(Point p)
    {
        secondFocus = p;
        repaint();
    }

    public Point getFirstFocus()
    {
        return firstFocus;
    }

    public Point getSecondFocus()
    {
        return secondFocus;
    }

    public void setR(double R)
    {
        this.R = R;
        repaint();
    }

    public double getR()
    {
        return R;
    }


    @Override
    public void paintComponent(Graphics g)
    {
        this.width = getSize().width;
        this.height = getSize().height;

        Color color = Color.GREEN;

        int green = color.getRGB();
        color = Color.WHITE;

        int white = color.getRGB();
        color = Color.BLACK;

        int black = color.getRGB();
        color = Color.MAGENTA;

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] data = image.getRGB(0, 0, width, height, null, 0, width);

        /*Flood fill*/
        for(int x = 0; x < width; x++)
        {
            for(int z = 0; z < height; z++)
            {
                data[x * height + z] = white;
            }
        }

        for(int u = 0; u < width; u++)
        {
            data[(height / 2) * width + u] = green;
        }

        for(int z = 0; z < height; z++)
        {
            data[width / 2 + (width * z)] = green;
        }

        /*Abscissa*/
        int half = height / 2;
        for(int u = 0; u < width; u++)
        {
            data[u + half * width] = black;
        }

        /*Ordinate*/
        for(int x = 0; x < height; x++)
        {
            data[x * width + width / 2] = black;
        }
        image.setRGB(0, 0, width, height, data, 0, width);
        if(firstFocus!=null)
        {
            drawFocus(firstFocus, Color.GREEN);
        }
        if(secondFocus!=null)
        {
            drawFocus(secondFocus, Color.RED);
        }
        if(firstFocus!=null && secondFocus!=null)
        {
            center = new Point((firstFocus.x + secondFocus.x) / 2, (firstFocus.y + secondFocus.y) / 2);
            drawLine(firstFocus.x, firstFocus.y, secondFocus.x, secondFocus.y, color.getRGB());
            drawLemniscate();

        }
        TexturePaint tex = new TexturePaint(image, new Rectangle(0, 0, width, height));
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(tex);
        g2.fillRect(0, 0, width, height);
    }

    private void drawFocus(Point focus, Color color)
    {

        final int offset = 3;
        if(focus!=null)
        {

            for(int i = focus.x - offset; i < focus.x + offset; i++)
            {
                for(int j = focus.y - offset; j < focus.y + offset; j++)
                {
                    setPoint(i, j, color.getRGB());
                }
            }
        }
    }


    private void drawLine(int x1, int y1, int x2, int y2, int color)
    {
        final int deltaX = Math.abs(x2 - x1);
        final int deltaY = Math.abs(y2 - y1);
        final int signX = x1 < x2 ? 1 : -1;
        final int signY = y1 < y2 ? 1 : -1;
        int error = deltaX - deltaY;

        while(x1!=x2 || y1!=y2)
        {
            final int error2 = error * 2;


            setPoint(x1, y1, color);
            if(error2 > -deltaY)
            {
                error -= deltaY;
                x1 += signX;
            }

            if(error2 < deltaX)
            {
                error += deltaX;
                y1 += signY;
            }
        }

        setPoint(x2, y2, color);
    }


    private void drawLemniscate()
    {
        int x = firstFocus.x;
        int y = findStartY(x, firstFocus.y);
        drawPart(x, y, 0);
        drawPart(x, y, NEIGHBORS.length / 2);
    }

    private void drawPart(int startX, int startY, int startDir)
    {
        int sumx =  firstFocus.x + secondFocus.x;
        int sumy =  firstFocus.y + secondFocus.y;
        int x = startX, y = startY, dir = startDir;
        do
        {
            int dirx1, diry1, dirx2, diry2;
            int tx = 0;
            int ty = 0;
            setPoint(x, y, Color.RED.getRGB());
            int x2 = sumx - x;
            int y2 = sumy - y;
            setPoint(x2, y2, Color.GREEN.getRGB());
            int currentDir = dir;
            dir = -1;
            /*
              View pairs of neighbors by round,
              choose pair where error signum changes.(Point of lemniscate between this points).
              After choose one point with minimal error and go there
             */
            for(int turn = -3 ; turn <= 3; turn++)
            {
                if(turn == 0)
                {
                    continue;
                }
                int dir1 = (NEIGHBORS.length +currentDir + turn) % NEIGHBORS.length;
                int dir2 = (NEIGHBORS.length +currentDir + turn - sgn(turn)) % NEIGHBORS.length;
                dirx1 = NEIGHBORS[dir1].x;
                diry1 = NEIGHBORS[dir1].y;
                dirx2 = NEIGHBORS[dir2].x;
                diry2 = NEIGHBORS[dir2].y;
                int nx1 = x + dirx1;
                int ny1 = y + diry1;
                int nx2 = x + dirx2;
                int ny2 = y + diry2;

                long err1 = f(nx1, ny1);
                long err2 = f(nx2, ny2);

                if(sgn(err1) * sgn(err2) <= 0)
                {
                    if(Math.abs(err1) < Math.abs(err2))
                    {
                        dir = dir1;
                        tx = dirx1;
                        ty = diry1;
                        break;
                    }
                    else
                    {
                        dir = dir2;
                        tx = dirx2;
                        ty = diry2;
                        break;
                    }
                }
            }
            if(dir==-1)
            {
                break;
            }

            x += tx;
            y += ty;
            long d1 = (firstFocus.x - x) * (firstFocus.x - x) + (firstFocus.y - y) * (firstFocus.y - y);
            long d2 = (secondFocus.x - x) * (secondFocus.x - x) + (secondFocus.y - y) * (secondFocus.y - y);
            if(d1 > d2)
            {
                break;
            }

        } while(!(x==startX && y==startY));
    }

    /*Lemniscate function*/
    private long f(int x, int y)
    {
        long rs = (long) (R * R);
        rs *= rs;
        final int x_ = (x - firstFocus.x);
        final int y_ = (y - firstFocus.y);
        final int x__ = (x - secondFocus.x);
        final int y__ = (y - secondFocus.y);
        final long t1 = (x_ * x_ + y_ * y_);
        final long t2 = (x__ * x__ + y__ * y__);
        return t1 * t2 - rs;

    }
    /*Binary search start point y coordinate*/
    private int findStartY(int x, int y)
    {
        int dir;
        if( secondFocus.y > firstFocus.y)
        {
            dir = -1;
        }
        else
        {
            dir = 1;
        }
        int last = 1;
        while(f(x, y + last * dir) < 0)
        {
            last *= 2;
        }

        int first = 0;
        while(first < last)
        {
            int mid = first + (last - first) / 2;
            if(f(x, y + mid * dir) < 0)
            {
                first = mid + 1;
            }
            else
            {
                last = mid;
            }
        }

        long f = Math.abs(f(x, y + last * dir));
        if(last > 0 && Math.abs(f(x, y + (last - 1) * dir)) < f)
        {
            return y + (last - 1) * dir;
        }
        else
        {
            return y + last * dir;
        }
    }

    private void setPoint(int x, int y, int color)
    {
        int sx = x + image.getWidth() / 2;
        int sy = image.getHeight() / 2 + y;
        if(sx >= 0 && sy >= 0 && sx < image.getWidth() && sy < image.getHeight())
        {

            image.setRGB(sx, sy, color);
        }
    }


    private final  int sgn(int x)
    {
        if(x > 0)
        {
            return 1;
        }
        if(x==0)
        {
            return 0;
        }
        return -1;
    }

    private final long sgn(long x)
    {
        if(x > 0)
        {
            return 1;
        }
        if(x==0)
        {
            return 0;
        }
        return -1;
    }
}

