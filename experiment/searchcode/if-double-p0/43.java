package FIT_0204_Shelestova.Font;

import FIT_0204_Shelestova.Common.GUIStandarts;

import static java.lang.Integer.signum;
import static java.lang.StrictMath.abs;

public class FontBuilder {
    private FontModel model;
    private int[] memory;
    private double t;

    public int[] getMemory() {
        return memory;
    }

    public void setMemory(int[] memory) {
        this.memory = memory;
    }

    public FontBuilder(int[] memory, FontModel model) {
        this.model = model;
        this.memory = memory;
        this.t = 0.5;
        clear();
        tests();
    }

    private void tests() {
        try {
            Point p0 = new Point(0, 0, true);
            Point p1 = new Point(30, 30, false);
            Point p2 = new Point(120, 0, true);
            drawPoint(p0);
            drawPoint(p2);
            bezie(p2, p1, p0);

            drawLine(0, 0, 0, 50);
            drawLine(0, 0, 0, -50);
            drawLine(0, 0, 100, 0);
            drawLine(0, 0, -100, 0);
            drawLine(0, 0, 150, 20);
            drawLine(0, 0, 20, 150);
            drawLine(0, 0, -20, 150);
            drawLine(0, 0, -150, 20);
            drawLine(0, 0, -150, -20);
            drawLine(0, 0, -20, -150);
            drawLine(0, 0, 20, -150);
            drawLine(0, 0, 150, -20);


        } catch (DrawException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void clear() {
        for (int i = 0; i < memory.length; i++) {
            memory[i] = GUIStandarts.backgroundColor;
        }
    }

    private void drawPoint(Point p) {
        drawPoint(p.getX(), p.getY());
    }

    private void drawPoint(int x, int y) {
        if (y > -model.getYnull() && y < model.getYnull() &&
                x < model.getXnull() && x > -model.getXnull()) {
            memory[(int) ((model.getYnull() - y) * model.getPanelWidth()
                    + x + model.getXnull())] = GUIStandarts.pointColor;
        }
    }


    public void drawLine(int x1, int y1, int x2, int y2) throws DrawException {
        drawLine(new Point(x1, y1), new Point(x2, y2));
    }

    public void drawLine(Point a, Point b) throws DrawException {
        // вертикальные линии
        if (a.getX() == b.getX()) {
            int x = a.getX();
            if (a.getY() > b.getY()) {
                Point tmp = a;
                a = b;
                b = tmp;
            }
            for (int y = a.getY(); y <= b.getY(); y++) {
                drawPoint(x, y);
            }
        }
        // горизонтальные линии
        else if (a.getY() == b.getY()) {
            int y = a.getY();
            if (a.getX() > b.getX()) {
                Point tmp = a;
                a = b;
                b = tmp;
            }
            for (int x = a.getX(); x <= b.getX(); x++) {
                drawPoint(x, y);
            }
        }
        // остальные
        else {
            bresenham(a, b);
        }
    }

    private void bresenham(Point a, Point b) throws DrawException {

        if (!a.isOn() || !b.isOn()) {
            throw new DrawException("Bad points came to bresenham");
        }
        int x = a.getX();
        int y = a.getY();

        int w = abs(b.getX() - a.getX());
        int h = abs(b.getY() - a.getY());

        int sx = signum(b.getX() - a.getX());
        int sy = signum(b.getY() - a.getY());

        boolean changed = false;

        if (w < h) {
            int tmp = w;
            w = h;
            h = tmp;
            changed = true;
        }
        int f = 2 * h - w;

        for (int i = 0; i <= w; i++) {
            drawPoint(x, y);

            if (f < 0) {
                f += 2 * h;
            } else {
                if (changed) {
                    x += sx;
                } else {
                    y += sy;
                }
                f += 2 * (h - w);
            }
            if (changed) {
                y += sy;
            } else {
                x += sx;
            }
        }
    }

    private void bezie(Point p0, Point p1, Point p2) throws DrawException {
        if (!p0.isOn() || p1.isOn() || !p2.isOn()) {
            throw new DrawException("Bad points came to bezie");
        }

        if (near(p0, p2)) {
            return;
        } else if (near(p0, p1) && near(p1, p2)) {
            drawPoint(p1);
            return;
        } else {
            Point A = new Point(t_break(p0.getX(), p1.getX()), t_break(p0.getY(), p1.getY()), false);
            Point B = new Point(t_break(p1.getX(), p2.getX()), t_break(p1.getY(), p2.getY()), false);
            Point P = new Point(t_break(A.getX(), B.getX()), t_break(A.getY(), B.getY()), true);

            drawPoint(P);

            bezie(p0, A, P);
            bezie(P, B, p2);
        }

    }

    private boolean near(Point a, Point b) {
        if (delta(a.getX(), b.getX()) > 1 || delta(a.getY(), b.getY()) > 1) {
            return false;
        } else {
            return true;
        }
    }

    private int t_break(double coord1, double coord2) {
        double d = ((1 - t) * coord1 + t * coord2);
        return (int) d;
    }

    private int delta(int coord1, int coord2) {
        return abs(coord2 - coord1);
    }
}

