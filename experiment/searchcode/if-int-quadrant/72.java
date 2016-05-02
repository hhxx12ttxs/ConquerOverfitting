package ao.dd.desktop.model.pixel;

import ao.dd.desktop.control.mouse.target.AbstractMouseTarget;
import ao.dd.desktop.model.area.Area;
import ao.dd.desktop.model.area.Areas;
import ao.dd.desktop.model.display.Display;

import java.awt.*;

/**
* User: Alex O & Eugene
* Date: Feb 5, 2010
* Time: 10:26:14 PM
*/
class BoundedPixel
        extends    AbstractMouseTarget
        implements Pixel
{
    //-------------------------------------------------------------------------
    private final Display display;
    private final Point   point;


    //-------------------------------------------------------------------------
    public BoundedPixel(
            Display onDisplay,
            int     x,
            int     y)
    {
        display = onDisplay;
        point   = new Point(x, y);
    }


    //-------------------------------------------------------------------------
    @Override
    public Display display()
    {
        return display;
    }


    //-------------------------------------------------------------------------
    @Override public int x() { return point.x; }
    @Override public int y() { return point.y; }


    //-------------------------------------------------------------------------
    @Override
    public void move()
    {
        display.point(
                point.x, point.y);
    }


    //-------------------------------------------------------------------------
    @Override
    public Pixel offset(int deltaX, int deltaY)
    {
        return new BoundedPixel(
                 display(),
                 x() + deltaX,
                 y() + deltaY);
    }


    //-------------------------------------------------------------------------
    @Override
    public Color colour()
    {
        return display().colour(
                point.x, point.y);
    }


    //-------------------------------------------------------------------------
    @Override
    public int quadrant()
    {
        if ( x() > display.width() / 2  && y() < display.height() / 2 )
            return 1;
        if ( x() < display.width() / 2  && y() < display.height() / 2 )
            return 2;
        if ( x() < display.width() / 2  && y() > display.height() / 2 )
            return 3;
        if ( x() > display.width() / 2  && y() > display.height() / 2 )
            return 4;

        else return 0;
    }

    
    //-------------------------------------------------------------------------
    @Override
    public Point toPoint()
    {
        return point;
    }

    @Override
    public Rectangle toRectangle(int width, int height)
    {
        return new Rectangle(
                point,
                new Dimension(
                        width, height));
    }


    //-------------------------------------------------------------------------
    @Override
    public boolean isNull()
    {
        return point.x == -1 ||
               point.y == -1;
    }


    //-------------------------------------------------------------------------
    @Override
    public Area toArea()
    {
        return toArea( 1,1 );
    }

    //-------------------------------------------------------------------------
    @Override
    public Area toArea(int width, int height)
    {
        return Areas.newInstance(
                 display(),
                 toRectangle(
                         width, height));
    }


    //-------------------------------------------------------------------------
    @Override
    public String toString()
    {
        return point.toString();
    }


    //-------------------------------------------------------------------------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoundedPixel pixel = (BoundedPixel) o;

        return display().equals(
                   pixel.display()) &&
               x() == pixel.x()   &&
               y() == pixel.y();
    }

    @Override
    public int hashCode() {
        int result = display().hashCode();
        result = 31 * result + point.hashCode();
        return result;
    }
}
