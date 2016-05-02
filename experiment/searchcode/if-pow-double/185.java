import java.awt.*;
import java.io.Serializable;

public class Oval extends Shape
    implements Serializable, Cloneable
{

    public Oval(Point start, DrawingCanvas dcanvas)
    {
        super(start, dcanvas);
    }

    public Oval clone()
    {
        Oval oval = new Oval(new Point(bounds.x, bounds.y), canvas);
        oval.setBoundingRect(new Rectangle(bounds));
        oval.setColor(color);
        return oval;
    }

    public void resize(Point anchor, Point end)
    {
        Rectangle newRect = new Rectangle(anchor);
        newRect.add(end);
        setBoundingRect(newRect);
    }

    public void draw(Graphics g, Rectangle regionToDraw)
    {
        if(!bounds.intersects(regionToDraw))
        {
            return;
        } else
        {
            g.setColor(color);
            g.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
            drawKnobs(g, regionToDraw);
            return;
        }
    }

    protected void drawKnobs(Graphics g, Rectangle regionToDraw)
    {
        if(isSelected)
        {
            Rectangle knobs[] = getKnobRects();
            for(int i = 0; i < knobs.length; i++)
            {
                g.setColor(Color.BLACK);
                g.fillRect(knobs[i].x, knobs[i].y, knobs[i].width, knobs[i].height);
            }

        }
    }

    public boolean inside(Point pt)
    {
        if(!super.inside(pt))
            return false;
        double radiusX = (double)bounds.width / 2D;
        double radiusY = (double)bounds.height / 2D;
        double centerX = (double)bounds.x + radiusX;
        double centerY = (double)bounds.y + radiusY;
        double normx = Math.pow(((double)pt.x - centerX) * radiusY, 2D);
        double normy = Math.pow(((double)pt.y - centerY) * radiusX, 2D);
        return normx + normy <= Math.pow(radiusX, 2D) * Math.pow(radiusY, 2D);
    }

    public static final long serialVersionUID = 1L;
}

