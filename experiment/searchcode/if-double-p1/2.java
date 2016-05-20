package ru.etu.astamir.geom.common.java;

import com.google.common.base.Preconditions;
import ru.etu.astamir.model.Drawable;
import ru.etu.astamir.model.Movable;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *  Класс двухмерной точки.
 *
 *  @version 1.0
 *  @author astamir
 */
public class Point implements Serializable, Cloneable, Drawable, Movable, Comparable<Point> {
    private static final long serialVersionUID = 1L;

    private double x;
    private double y;

    /**
     * Нужно для отрисовки точки в виде овала.
     */
    private int radius = 4;

    /**
     * Возможные положение точки относительно прямой.
     */
    public static enum Position {LEFT,  RIGHT,  BEYOND,  BEHIND, BETWEEN, ORIGIN, DESTINATION;
        public boolean isOnEdge() {
            return this == ORIGIN || this == BETWEEN || this == DESTINATION;
        }

        public boolean isLeftOrRight() {
            return this == LEFT || this == RIGHT;
        }
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    public Point() {
        this(0.0, 0.0);
    }

    public static Point of(double x, double y) {
        return new Point(x, y);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public int intX() {
        return (int) x;
    }

    public int intY() {
        return (int) y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean move(double dx, double dy) {
        this.x += dx;
        this.y += dy;
        
        return true;
    }

    @Override
    public boolean move(Direction direction, double d) {
        return GeomUtils.move(this, direction, d);
    }

    public void moveByAngle(double xAngle, double length) {
        this.x += Math.cos(xAngle) * length;
        this.y += Math.sin(xAngle) * length;
    }

    public void shiftX(double dx) {
        this.x += dx;
    }

    public void shiftY(double dy) {
        this.y += dy;
    }

    public void setPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setPoint(final Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    public void setRadius(int radius) {
        this.radius = radius;        
    }

    public static Point plus(final Point p1, final Point p2) {
        return new Point(p1.x + p2.x, p1.y + p2.y);
    }
    
    public Point plus(final Point p) {
        x += p.x;
        y += p.y;
        
        return this;
    }

    public static Point minus(final Point p1, final Point p2) {
        return new Point(p1.x - p2.x, p1.y - p2.y);
    }

    public static Point multiply(final Point p, double s) {
        return new Point(p.x * s, p.y * s);
    }
    
    public Point multiply(double s) {
        x *= s;
        y *= s;

        return this;
    }

    public static double distanceSq(final Point p1, final Point p2) {
        return Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2);
    }

    public static double distance(final Point p1, final Point p2) {
        double sqrt = Math.sqrt(distanceSq(p1, p2));
       // BigDecimal toRound = new BigDecimal(sqrt).setScale(1, RoundingMode.HALF_UP);
        return sqrt;//toRound.doubleValue();
    }

    /**
     * Сравнение двух точек в лексикографическом порядке.
     */
    public int compareTo(final Point p) {
        if (p != null) {
            if ((x > p.x) || ((x == p.x) && (y > p.y))) {
                return 1;
            } else if ((x < p.x) || ((x == p.x) && (y < p.y))) {
                return -1;
            } else {
                return 0;
            }
        }

        return 0;
    }

    public double length() {
        return Math.sqrt(x*x + y*y);
    }

    /**
     * Получение полярного угла.
     * @return
     */
    public double polarAngle() {
        if ((x == 0.0) && (y == 0.0))
            return -1.0;
        if (x == 0.0)
            return ((y > 0.0) ? 90 : 270);
        double theta = Math.atan(y/x);                    // в радианах
        theta *= 360 / (2 * Math.PI);            // перевод в градусы
        if (x > 0.0)                                 // 1 и 4 квадранты
            return ((y >= 0.0) ? theta : 360 + theta);
        else                                         // 2 и З квадранты
            return (180 + theta);
    }

    /**
     * Функция orientation возвращает значение 1,
     * если обрабатываемые три точки ориентированы положительно,
     * -1, если они ориентированы отрицательно, или 0, если они коллинеарны.
     * @param p0
     * @param p1
     * @param p2
     * @return
     */
    public static int orientation(final Point p0, final Point p1, final Point p2) {
        Point a = Point.minus(p1, p0);
        Point b = Point.minus(p2, p0);
        double sa = a.x * b.y - b.x * a.y;
        if (sa > 0.0)
            return 1;
        if (sa < 0.0)
            return -1;
        return 0;
    }

    public double distance(Edge e) {
        Edge ab = (Edge) e.clone();
        ab.flip().rotate();          // поворот ab на 90 градусов
                                     // против часовой стрелки
        Point n = Point.minus(ab.getEnd(), ab.getStart());
                                    // n = вектор, перпендикулярный ребру е
        n = Point.multiply(n, 1.0 / n.length());
                                    // нормализация вектора n
        Edge f = new Edge(this, Point.plus(this, n));
                                    // ребро f = n позиционируется
                                    // на текущей точке
                                    // t = расстоянию со знаком
        return f.intersectionCoefficient(e);// вдоль вектора f до точки,
                                    // в которой ребро f пересекает ребро е
    }

    /**
     * Скалярное произведение точек(как векторов из двух координат).
     * @return
     */
    public static double dotProduct(final Point p, final Point q) {
        return p.x * q.x + p.y * q.y;
    }

    public Point rotate(double angle, Point center) {
        Preconditions.checkNotNull(center);
        double oldX = x;
        double oldY = y;

        x = (oldX - center.x()) * Math.cos(angle) - (oldY - center.y()) * Math.sin(angle);
        y = (oldX - center.x()) * Math.sin(angle) + (oldY - center.y()) * Math.cos(angle);

        return this;
    }

    public Position classify(final Point p1, final Point p2) {
        Point a = Point.minus(p2, p1);
        Point b = Point.minus(this, p1);
        double sa = a. x * b.y - b.x * a.y;
        if (sa > 0.0)
            return Position.LEFT;
        if (sa < 0.0)
            return Position.RIGHT;
        if ((a.x * b.x < 0.0) || (a.y * b.y < 0.0))
            return Position.BEHIND;
        if (a.length() < b.length())
            return Position.BEYOND;
        if (this.equals(p1))
            return Position.ORIGIN;
        if (this.equals(p2))
            return Position.DESTINATION;
        return Position.BETWEEN;
    }
    
    public Position classify(final Edge e) {
        return classify(e.getStart(), e.getEnd());
    }

    public static Point fromPoint2D(final Point2D point2D) {
        return new Point(point2D.getX(), point2D.getY());
    }

    public Point2D.Double toPoint2D() {
        return new Point2D.Double(x, y);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Point other = (Point) obj;
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
            return false;
        return true;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public void draw(Graphics2D g) {
        g.fillOval((int) (x - radius / 2), (int) (y - radius / 2), radius, radius);
    }
}

