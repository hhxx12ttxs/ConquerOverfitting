package FIT_9202_Machulskis.ImageViewer.ImageDraw;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public abstract class AbstractImagePanel extends JPanel
{

    protected BufferedImage image;
    int width = 0;
    int height = 0;
    int data[];
    double scaleX;
    double scaleY;
    BufferedImage texture;
    Texture textur;

    ImageModel model;
    int twidth;
    int theight;
    int px;
    int py;
    double angle;
    AbstractImagePanel(ImageModel model)
    {
        this.model = model;
        model.registerObserver(this);

    }

    public void update()
    {
        repaint();
    }

    public boolean hasPoint(Point pressPoint)
    {
        return (pressPoint.x > getLocationOnScreen().x && pressPoint.x < getLocationOnScreen().x + getWidth() && (pressPoint.y > getLocationOnScreen().y && pressPoint.y < getLocationOnScreen().y + getHeight()));
    }


    void drawControlPoint()
    {
        Color color = Color.RED;

        int offset = 10;
        if(model.isSetControlPoint())
        {
            Point point = model.getControlPoint();
            for(int i = point.x - offset; i < point.x + offset; i++)
            {
                int coord = i + point.y * width;
                if(coord > 0 && coord < width * height)
                {
                    data[coord] = color.getRGB();
                }
            }
            for(int i = point.y - offset; i < point.y + offset; i++)
            {
                int ko = point.x + i * width;
                if(ko > 0 && ko < width * height)
                {
                    data[ko] = color.getRGB();
                }
            }
        }
    }
    void drawDraggedPoint()
    {
        Color color = Color.BLUE;

        int offset = 10;
        if(model.isSetDraggedPoint())
        {
            Point point = model.getDraggedPoint();
            for(int i = point.x - offset; i < point.x + offset; i++)
            {
                int coord = i + point.y * width;
                if(coord > 0 && coord < width * height)
                {
                    data[coord] = color.getRGB();
                }
            }
            for(int i = point.y - offset; i < point.y + offset; i++)
            {
                int ko = point.x + i * width;
                if(ko > 0 && ko < width * height)
                {
                    data[ko] = color.getRGB();
                }
            }
        }
    }
    boolean isValidPosition(int x, int y)
    {
        return x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight();
    }

    void setPixel(int x, int y, int color)
    {
        if(isValidPosition(x, y))
        {
            data[x + width * y] = color;
        }
    }
    void setPixel(int offset, int color)
    {
        data[offset] = color;
    }
    void drawLine(int x1, int y1, int x2, int y2, int color)
    {
        final int deltaX = Math.abs(x2 - x1);
        final int deltaY = Math.abs(y2 - y1);
        final int signX = x1 < x2 ? 1 : -1;
        final int signY = y1 < y2 ? 1 : -1;

        int error = deltaX - deltaY;

        while(x1!=x2 || y1!=y2)
        {
            final int error2 = error * 2;

            setPixel(x1, y1, color);
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

        setPixel(x2, y2, color);
    }

    void drawTriangle(Triangle triangle)
    {
        ArrayList<Point> vertices = triangle.getCurrVertices();
        textur = triangle.getTexture();

        LinkedList<Pair<Point, Point>> points = new LinkedList<Pair<Point, Point>>();

        points.add(new Pair<Point, Point>(vertices.get(0), textur.first()));
        points.add(new Pair<Point, Point>(vertices.get(1), textur.second()));
        points.add(new Pair<Point, Point>(vertices.get(2), textur.thrid()));
        Collections.sort(points, new VerticleComparator());

        Point v1 = points.get(0).first;
        Point v2 = points.get(1).first;
        Point v3 = points.get(2).first;
        Point t1 = points.get(0).second;
        Point t2 = points.get(1).second;
        Point t3 = points.get(2).second;

        int x[] = {v1.x, v3.x, v1.x, v2.x, v2.x, v3.x};
        int y[] = {v1.y, v3.y, v1.y, v2.y, v2.y, v3.y};

        int deltaX[] = new int[3];
        int deltaY[] = new int[3];
        int signX[] = new int[3];
        int prevX[] = new int[3];

        int error[] = new int[3];
        int error2[] = new int[3];

        for(int i = 0; i < 3; i++)
        {
            deltaX[i] = Math.abs(x[2 * i] - x[2 * i + 1]);
            deltaY[i] = Math.abs(y[2 * i] - y[2 * i + 1]);
            signX[i] = x[2 * i] < x[2 * i + 1] ? 1 : -1;
            error[i] = deltaX[i] - deltaY[i];
            prevX[i] = x[2 * i];
        }

        int a = 1;

        for(int currY = v1.y; currY <= v3.y + 1; currY++)
        {
            if(currY > 0 && currY < height)
            {
            if(y[2 * a]==y[2 * a + 1])
            {
                a = 2;
            }

            if(currY!=v3.y + 1)
            {
                for(int t = 0; t <= a; t += a)
                {

                    while(y[2 * t]!=currY)
                    {
                        error2[t] = error[t] * 2;

                        if(error2[t] > -deltaY[t])
                        {
                            error[t] -= deltaY[t];
                            x[2 * t] += signX[t];
                        }

                        if(error2[t] < deltaX[t])
                        {
                            error[t] += deltaX[t];
                            y[2 * t] += 1;
                        }
                    }
                }
            }

            if(currY==v1.y)
            {
                continue;
            }

            //TODO : make this code more readable and understandable
            Point tv = new Point(a==1 ? v1 : v3);
            Point lv = new Point(a==1 ? (v2.x < v3.x ? v2 : v3) : (v2.x < v1.x ? v2 : v1));
            Point rv = new Point(a==1 ? (v2.x > v3.x ? v2 : v3) : (v2.x > v1.x ? v2 : v1));
            Point tt = new Point(a==1 ? t1 : t3);
            Point lt = new Point(a==1 ? (v2.x < v3.x ? t2 : t3) : (v2.x < v1.x ? t2 : t1));
            Point rt = new Point(a==1 ? (v2.x > v3.x ? t2 : t3) : (v2.x > v1.x ? t2 : t1));

            double xxxx = 1.0;
            if(lv.y!=tv.y)
            {
                xxxx = ((double) (currY - 1 - tv.y)) / (lv.y - tv.y);
            }

            double yyyy = 1.0;
            if(rv.y!=tv.y)
            {
                yyyy = ((double) (currY - 1 - tv.y)) / (rv.y - tv.y);
            }

            double tl_x = xxxx * lt.x + (1 - xxxx) * tt.x;
            double tl_y = xxxx * lt.y + (1 - xxxx) * tt.y;

            double tr_x = yyyy * rt.x + (1 - yyyy) * tt.x;
            double tr_y = yyyy * rt.y + (1 - yyyy) * tt.y;

            int li = prevX[0] <= prevX[a] ? 0 : a;
            int ri = prevX[0] > prevX[a] ? 0 : a;

            int xstart = signX[li] > 0 ? prevX[li] : x[2 * li] + (prevX[li]!=x[2 * li] ? 1 : 0);
            int xend = signX[ri] > 0 ? x[2 * ri] - (x[2 * ri]!=prevX[ri] ? 1 : 0) : prevX[ri];
            int realxend = xend;
            int realxstart = xstart;
            if(xstart< 0)
            {
                xstart =0;
            }
            if(xend > width)
            {
                xend = width;
            }
            for(int currX = xstart; currX <= xend; currX++)
            {
                
                double zzzz = 1.0;
                if(xend!=xstart)
                {
                    zzzz = ((double) (currX - realxstart)) / (realxend - realxstart);
                }
                double tcx = zzzz * tr_x + (1.0 - zzzz) * tl_x;
                double tcy = zzzz * tr_y + (1.0 - zzzz) * tl_y;

                int point;
                {
                    point = (int) (tcx) + twidth * (int) (tcy);
                }
                int color;
                if(point > 0 && point < twidth*theight && point< height*width)
                {
                    color = getPixelColor(tcx, tcy, textur.image());
                }
                else
                {
                    continue;
                }
                setPixel(currX, currY , color);
            }
            
            }
            prevX[0] = x[0];
            prevX[a] = x[2 * a];
        }


        drawLine(v1.x, v1.y, v3.x, v3.y, 0xFF000000);
        drawLine(v2.x, v2.y, v3.x, v3.y, 0xFF000000);
        drawLine(v1.x, v1.y, v2.x, v2.y, 0xFF000000);
    }
   Point rotate(Point end, double angle)
    {
        angle = angle * 0.017453292519943295769; //      pi/180

        Point controlPoint =new Point( model.getControlPoint());
        controlPoint.x = (int) (controlPoint.x - width/2 + twidth*scaleX/2);
        controlPoint.y = (int) (controlPoint.y - height/2 + theight*scaleY/2
        );
        Point newEnd = new Point();
        double dx, dy, dxn, dyn;
        dx = -controlPoint.x + end.x;
        dy = end.y - controlPoint.y;
        dxn = dx * Math.cos(angle) - dy * Math.sin(angle);
        dyn = dx * Math.sin(angle) + dy * Math.cos(angle);
        newEnd.x = (int) (controlPoint.x + dxn);
        newEnd.y = (int) (controlPoint.y + dyn);
        return newEnd;
    }
    @Override
    public void paintComponent(Graphics g)
    {
        Color color = Color.WHITE;

        int white = color.getRGB();
        texture = model.getTexture();
        scaleY = model.getScaleY();
        scaleX = model.getScaleX();
        width = getWidth();
        height = getHeight();
        angle = model.getAngle();

        px = (int) model.getPositionX();
        py = (int) model.getPositionY();

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        if(texture==null)
        {
            texture = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            data = texture.getRGB(0, 0, width, height, null, 0, width);


            for(int x = 0; x < width; x++)
            {
                for(int z = 0; z < height; z++)
                {
                    data[x * height + z] = white;
                }
            }
            texture.setRGB(0, 0, width, height, data, 0, width);
            model.setTexture(texture);
        }
        else
        {
            data = image.getRGB(0, 0, width, height, null, 0, width);
        }

        for(int x = 0; x < width; x++)
        {
            for(int z = 0; z < height; z++)
            {
                data[x * height + z] = white;
            }
        }
        texture = model.getTexture();
        twidth = texture.getWidth();
        theight = texture.getHeight();
        int txr = (int) (twidth * scaleX);
        int tyr = (int) (theight * scaleY);

        for(int k = 0; k < 2; k++)
        {

            Point tr1 = new Point(k * txr, k * tyr);
            Point tr2 = new Point(txr, 0);
            Point tr3 = new Point(0, tyr);

            Triangle triangle = new Triangle(new Point(tr1.x, tr1.y), new Point(tr2.x, tr2.y), new Point(tr3.x, tr3.y));
            Texture text = new Texture(texture, new Point(k*twidth, k*theight), new Point(twidth, 0), new Point(0, theight));

            Point v1 = rotate(tr1, angle);
            Point v2 = rotate(tr2, angle);
            Point v3 = rotate(tr3, angle);
            v1.x = v1.x + width / 2 - (int) (twidth * scaleX / 2) + px;
            v1.y = v1.y + height / 2 - (int) (theight * scaleY / 2) + py;

            v2.x = v2.x + width / 2 - (int) (twidth * scaleX / 2) + px;
            v2.y = v2.y + height / 2 - (int) (theight * scaleY / 2) + py;

            v3.x = v3.x + width / 2 - (int) (twidth * scaleX / 2) + px;
            v3.y = v3.y + height / 2 - (int) (theight * scaleY / 2) + py;

            triangle.setTexture(text);
            triangle.setCurrVetices(v1, v2, v3);
            
            drawTriangle(triangle);
        }

        drawControlPoint();
        drawDraggedPoint();
        image.setRGB(0, 0, width, height, data, 0, width);

        TexturePaint tex = new TexturePaint(image, new Rectangle(0, 0, width, height));
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(tex);
        g2.fillRect(0, 0, width, height);
               
    }

    abstract int getPixelColor(double x_, double y_, BufferedImage t);
    abstract void textureChanged();
}
