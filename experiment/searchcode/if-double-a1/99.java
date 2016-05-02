package de.winterberg.android.sandbox.util;

import android.graphics.PointF;
import android.graphics.RectF;

/**
 * @author Benjamin Winterberg
 */
public class Math2 {

    public static PointF collision(PointF pos, double dx, double dy, RectF bounds, int margin) {
        double x1 = pos.x;
        double y1 = pos.y;
        double x2 = x1 + dx;
        double y2 = y1 - dy;


        // f(x) = m*x + t
        double m = dy / dx;
        double t = y1 / (m * x1);

        double x, y;

        // left
        x = margin;
        y = m * x + t;
        if (between(x, x1, x2) && between(y, y1, y2))
            return new PointF((float) x, (float) y);

        // right
        x = bounds.width() + margin;
        y = m * x + t;
        if (between(x, x1, x2) && between(y, y1, y2))
            return new PointF((float) x, (float) y);

        // top
        y = margin;
        x = (y - t) / m;
        if (between(x, x1, x2) && between(y, y1, y2))
            return new PointF((float) x, (float) y);

        // bottom
        y = bounds.height() + margin;
        x = (y - t) / m;
        if (between(x, x1, x2) && between(y, y1, y2))
            return new PointF((float) x, (float) y);

        return null;
    }

    public static boolean between(double a, double a1, double a2) {
        return (a >= a1 && a <= a2) || (a >= a2 && a <= a1);
    }

}
