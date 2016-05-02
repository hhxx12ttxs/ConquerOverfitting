package ru.etu.astamir.geom.common.java;


import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import ru.etu.astamir.model.Drawable;
import ru.etu.astamir.model.common.Pair;
import ru.etu.astamir.model.common.Pairs;
import ru.etu.astamir.model.exceptions.UnexpectedException;

import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;


/**
 * Полигон представляется последовательностью вершин.
 *
 * @version 1.0
 * @author astamir
 */
public class Polygon implements Cloneable, Serializable, Drawable {
    /**
     * Список вершин полигона.
     */
    List<Point> vertices = Lists.newArrayList();

    /**
     * Возможные положения точки относительно полигона.
     */
    public static enum PointRelation {INSIDE, OUTSIDE, BOUNDARY;
        public boolean isInsideOrBoundary() {
            return this == INSIDE || this == BOUNDARY;
        }
    }

    /**
     * Положение ребра относительно точки.
     */
    static enum EdgeToPointRelation {TOUCHING, CROSSING, INESSENTIAL}

    /**
     * Подсчет кол-ва измененияй списка точек.
     */
    private transient int modCount = 0;
    
    public Polygon(List<Point> vertices) {
        this.vertices = vertices;
    }                                      
    

    /**
     * Конструктор, не имеющий аргументов, инициирует пустой полигон
     */
    public Polygon() {
    }

    public static Polygon of(Point... vertices) {
        return new Polygon(Lists.newArrayList(vertices));
    }

    public static Polygon of(Collection<Point> vertices) {
        return new Polygon(Lists.newArrayList(vertices));
    }

    public boolean intersects(Polygon polygon) {
        for (Point vertex : polygon.vertices) {
            if (isPointIn(vertex)) {
                return true;
            }
        }

        for (Point vertex : vertices) {
            if (polygon.isPointIn(vertex)) {
                return true;
            }
        }
        
        return false;
    }

    public boolean touches(final Polygon polygon) {
        for (Point vertex : polygon.vertices) {
            if (pointRelationToPolygon(vertex) == PointRelation.BOUNDARY) {
                return true;
            }
        }

        for (Point vertex : vertices) {
            if (polygon.pointRelationToPolygon(vertex) == PointRelation.BOUNDARY) {
                return true;
            }
        }
        
        return false;
    }
    

    /**
    * Количество вершин в полигоне.
    * 
    * @return Количество вершин. 
    */
    public int size() {
        return vertices.size();
    }

    /**
     * Небезопасное получение вершины. Если индекс вершины больше
     * размера полигона, получается вершина с индексом равным
     * остатку от деления на размер полигона.
     * int realIndex = index % size;
     *
     * @param index Индекс вершины
     * @return Вершина по заданному индексу с возможной поправкой(realIndex = index % size).
     */
    public Point get(int index) {
        return vertices.get(index % size());
    }

    /**
     * Обычное получение вершины по индексу.
     * @param index Индекс вершины.
     * @return Вершина полигона по заданному индексу
     */
    public Point getVertex(int index) {
        rangeCheck(index);
        return vertices.get(index);
    }
    
    public List<Point> vertices() {
        ImmutableList.Builder<Point> builder = ImmutableList.builder();
        builder.addAll(vertices);
        return builder.build();
    }

    public Point getMassCenter() {
        Preconditions.checkArgument(vertices.size() > 0);
        Point center = new Point();
        for (Point vertex : vertices) {
            center.plus(vertex);
        }

        return center.multiply(1.0/(double) vertices.size());
    }

    public Point getCenter() {
        return getMassCenter();
    }


    public Edge getEdge(int i) {
        rangeCheck(i);
        if (i == size() - 1) {
            return Edge.of(vertices.get(i), vertices.get(0));
        }

        return Edge.of(vertices.get(i), vertices.get(i + 1));
    }

    public Edge getEdgeRound(int i) {
        return Edge.of(get(i), get(i + 1));
    }
    
    public ImmutableList<Edge> edges() {
        ImmutableList.Builder<Edge> builder = ImmutableList.builder();
        return builder.addAll(edgeIterator()).build();
    }

    /**
     * Добавление новой вершины в полигон.
     *
     * @param vertex Вершина.
     * @return true, если получилось добавить вершину.
     */
    public boolean add(Point vertex) {
        modCount++;
        return vertices.add(vertex);
    }

    public void addAll(Collection<Point> vertices) {
        modCount++;
        this.vertices.addAll(vertices);
    }
    
    public void setVertices(List<Point> vertices) {
        clearVertices();
        addAll(vertices);
    }

    public void clearVertices() {
        modCount++;
        this.vertices.clear();
    }

    /**
     * Поворот фигуры относительно ее центра масс против часовой стрелки.
     * @param angle
     */
    public Polygon rotate(double angle) {
        if (vertices.size() > 0) {
            Point center = getMassCenter();
            for (Point vertex : vertices) {
                vertex.rotate(angle, center);
            }
        }

        return this;
    }

    /**
     * Вставка новой вершины после конкретной.
     *
     * @param index Индекс, после которого следует поместить новую вершину.
     * @param vertex Новая вершина.
     * @return true, если получилось вставить вершину.
     */
    public boolean insert(int index, Point vertex) {
        modCount++;
        vertices.add(index, vertex);
        return true;
    }

    /**
     * Удаление вершины.
     *
     * @param vertex Вершина для удаления.
     * @return true, если получилось удалить вершину.
     */
    public boolean remove(Point vertex) {
        modCount++;
        return vertices.remove(vertex);
    }

    /**
     * Удаление вершины.
     *
     * @param index Индекс элемента, который надо удалить.
     * @return Удаленная вершина.
     */
    public Point remove(int index) {
        modCount++;
        return vertices.remove(index);
    }

    public void move(double dx, double dy) {
        for (Point vertex : vertices) {
            vertex.move(dx, dy);
        }
    }

    /**
     * Поиск вершины "максимальной вершины" по заданному
     * компаратору.
     *
     *@param cmp Функция сравнения вершин.
     * @return "Максимальная" вершина полигона.
     * @throws NoSuchElementException Если в полигоне нету вершин.
     */
    public Point vertexSearchMax(Comparator<Point> cmp) throws NoSuchElementException {
        return Collections.max(vertices, cmp);
    }

    /**
     * Поиск вершины "минимальной вершины" по заданному
     * компаратору.
     *
     * @param cmp Функция сравнения вершин.
     * @return "Минимальная" вершина полигона.
     * @throws NoSuchElementException Если в полигоне нету вершин.
     */
    public Point vertexSearchMin(Comparator<Point> cmp) throws NoSuchElementException {
        return Collections.min(vertices, cmp);
    }

    /**
     * Получение первой самой левой вершины.
     *
     * @return Самая левая вершина полигона или null, если вершин нету.
     * @throws NoSuchElementException Если в полигоне нету вершин.
     */
    public Point getLeftVertex() throws NoSuchElementException {
        return vertexSearchMin(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                Double x1 = o1.x();
                Double x2 = o2.x();

                return x1.compareTo(x2);
            }
        });
    }

    /**
     * Получение первой самой правой вершины.
     *
     * @return Самая правая вершина полигона или null, если вершин нету.
     * @throws NoSuchElementException Если в полигоне нету вершин.
     */
    public Point getRightVertex() throws NoSuchElementException {
        return vertexSearchMax(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                return Doubles.compare(o1.x(), o2.x());
            }
        });
    }

    /**
     * Получение первой самой левой вершины.
     *
     * @return Самая левая вершина полигона или null, если вершин нету.
     * @throws NoSuchElementException Если в полигоне нету вершин.
     */
    public Point getTopVertex() throws NoSuchElementException {
        return vertexSearchMax(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                Double y1 = o1.y();
                Double y2 = o2.y();

                return y1.compareTo(y2);
            }
        });
    }

    /**
     * Получение первой самой левой вершины.
     *
     * @return Самая левая вершина полигона или null, если вершин нету.
     * @throws NoSuchElementException Если в полигоне нету вершин.
     */
    public Point getBottomVertex() throws NoSuchElementException {
        return vertexSearchMin(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                Double y1 = o1.y();
                Double y2 = o2.y();

                return y1.compareTo(y2);
            }
        });
    }

    /**
     * Проверка принадлежности точки полигону. Работает только для выпуклых полигонов,
     * причем не проверяет полигон на выпуклость. Если заранее известно,
     * что полигон выпуклый, стоит использовать эту функцию, иначе
     * использовать следует общую функцию #isPointIn.
     *
     * @param p Точка, которую проверяем на вхождение.
     * @return true, если точка принадлежит полигону.
     * @see #isPointIn(Point)
     */
    public boolean isPointInConvexPolygon(Point p) {
        int size = size();
        if (size == 1)
            return (p.equals(get(0)));
        if (size == 2) {
            return getEdge(0).isPointInOrOnEdges(p);
        }
        Point.Position pos = Point.Position.RIGHT;
        boolean positionDefined = false;
        for (ListIterator<Edge> it = edgeIterator(); it.hasNext();) {
            Edge edge = it.next();
            Point.Position position = p.classify(edge);
            if (position.isOnEdge()) {
                return true;
            }
            if (position.isLeftOrRight()) {
                if (!positionDefined) {
                    pos = position;
                    positionDefined = true;
                } else if (!pos.equals(position)) {
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * Вычисление положения точки относительно полигона.
     *
     * @param a Собственно точка.
     * @return Точка может быть внутри, снаружи или на границе полигона.
     */
    public PointRelation pointRelationToPolygon(Point a) {
        if (size() < 3) {
            return PointRelation.OUTSIDE;
        }

        int parity = 0;
        for (Iterator<Edge> i = edgeIterator();  i.hasNext();) {
            Edge e = i.next();
            switch (edgeType(a, e)) {
                case TOUCHING:
                    return PointRelation.BOUNDARY;
                case CROSSING:
                    parity = 1 - parity;
            }
        }
        return parity != 0 ? PointRelation.INSIDE : PointRelation.OUTSIDE;
    }

    /**
     *  Классифицирует ребро е как TOUCHING, CROSSING или INESSENTIAL (пересекающее, касательное или безразличное)
     *  относительно точки а
     *
     * @param a Точка относительно которой выясняется позиция.
     * @param e Ребро полигона.
     * @return Положение ребра #e относительно точки #a.
     */
    EdgeToPointRelation edgeType (Point a, Edge e) {
        Point v = e.getStart();
        Point w = e.getEnd();
        switch (a.classify(e)) {
            case LEFT:
                return ((v.y() < a.y()) && (a.y() <= w.y())) ? EdgeToPointRelation.CROSSING : EdgeToPointRelation.INESSENTIAL;
            case RIGHT:
                return ((w.y()<a.y())&&(a.y()<=v.y())) ? EdgeToPointRelation.CROSSING : EdgeToPointRelation.INESSENTIAL;
            case BETWEEN:
            case ORIGIN:
            case DESTINATION:
                return EdgeToPointRelation.TOUCHING;
            default:
                return EdgeToPointRelation.INESSENTIAL;
        }
    }

    /**
     * Проверяет находится ли заданная точка внутри полигона(на границе).
     *
     * @param point Заданная точка.
     * @return true, если точка находится внутри полигона или на его границе.
     */
    public boolean isPointIn(Point point) {
        return pointRelationToPolygon(point).isInsideOrBoundary();
    }


    /**
     * Проверка полигона на выпуклость
     *
     * @return true, если полигон выпуклый.
     */
    public boolean isConvex() {
        for (ListIterator<Edge> i = edgeIterator(); i.hasNext();) {
            int index = i.nextIndex();
            Edge e = i.next();
            Point.Position figuredPosition = null;
            boolean positionFigured = false;
            for (int k = 0; k < size() - 2; k++) {
                int newIndex = index + 2 + k;
                Point vertex = get(newIndex);
                Point.Position position = vertex.classify(e);
                if (!positionFigured) {
                    figuredPosition = position;
                    positionFigured = true;
                } else {
                    if (position != figuredPosition) {
                        return false;
                    }
                }                
            }
        }
        
        return true;
    }

    /**
     * Проверка полигона на ортогональность.
     * @return true, если все углы полигона равны 90 градусов.
     */
    public boolean isOrthogonal() {
        int size = size();
        if (size < 4 && (size & 1) != 1) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            Edge cur = getEdgeRound(i);
            Edge next = getEdgeRound(i + 1);

            if (Math.toDegrees(Math.abs(cur.angle(next))) != 90) {
                return false;
            }
        }
        
        return true;
    }


    /**
     * Рассчет периметра фигуры
     * 
     * @return Периметр полигона.
     */
    public double perimeter () {
        double perimeter = 0;        
        for (Iterator<Edge> i = edgeIterator(); i.hasNext();) {
            perimeter += i.next().length();
        }

        return perimeter;
    }

    /**
     * Рассчет площади полигона.
     * 
      * @return Площадь полигона
     */
    public double area () {         
        double sum = 0;
        
        for (Iterator<Edge> i = edgeIterator(); i.hasNext();) {
            Edge e = i.next();            
            sum += (e.getStart().intX() + e.getEnd().intX()) * (e.getStart().intY() - e.getEnd().intY());
        }
        
        sum /= 2;                
        return Math.abs(sum);
    }

    /**
     * Перетасовка точек в порядке обхода
     */
    // TODO нестабильно
    public void walk() {        
        if (vertices().size() < 2) {
            return;
        }
        
        Point leftBottom = vertexSearchMin(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                if (o1.x() > o2.x()) {
                    return 1;
                } else if (o1.x() < o2.x()) {
                    return -1;
                } else if (o1.x() == o2.x()) {
                    return Doubles.compare(o1.y(), o2.y());
                }

                return 0;
            }
        });

        System.out.println(vertices);
        System.out.println("leftBottom index = " + vertices.indexOf(leftBottom));

        List<Point> pool = Lists.newArrayList(vertices());
        List<Point> newVertices = Lists.newArrayList((Point) leftBottom.clone());
        
        Point curPoint = leftBottom;
        Direction dir = Direction.LEFT;
        System.out.println("curPoint = " + curPoint);

        
        for (int i = 0; i < vertices().size() - 1; i++) {
            dir = dir.clockwise();
            System.out.println("trying " + dir);
            Optional<Point> clockwisePoint = findOnTheWay(pool, curPoint, dir);
            if (clockwisePoint.isPresent()) {
                curPoint = clockwisePoint.get();
                System.out.println(curPoint + "found");
            } else {
                dir = dir.getOppositeDirection();
                System.out.println("trying " + dir);
                Optional<Point> counterclockwisePoint = findOnTheWay(pool, curPoint, dir);
                if (counterclockwisePoint.isPresent()) {
                    curPoint = counterclockwisePoint.get();
                    System.out.println(curPoint + "found");
                } else {
                    System.out.println("i = " + i);
                    System.out.println(dir);
                    System.out.println("curPoint = " + curPoint);
                    System.out.println(vertices);
                    throw new UnexpectedException();
                }
            }

            newVertices.add(curPoint);
        }

        setVertices(newVertices);
    }
    
    Optional<Point> findOnTheWay(List<Point> vertices, Point root, Direction dir) {        
        vertices.remove(root);
        Edge ray = Edge.ray(root, dir);
        List<Pair<Point, Double>> distances = Lists.newArrayList();
        for (Point p : vertices) {
            if (ray.isPointIn(p)) {
                distances.add(Pair.of(p, Point.distance(root, p)));
            }
        }

        return Optional.fromNullable(Pairs.min(distances));
    }
    
    private void rangeCheck(int index) {
        if (index >= size())
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + size());
    }
    
    private class EdgeIterator implements ListIterator<Edge> {
        /**
         * Индекс элемента, который вернет функция next().
         */
        int cursor = 0;

        /**
         * Индекс элемента, полученный последним вызовом next или
         * previous.
         */
        int lastRet = -1;

        /**
         * Предполагаемое количесвто изменений списка. Если это предположение
         * нарушено бросаем исключение.
         */
        int expectedModCount = modCount;

        EdgeIterator(int index) {
            cursor = index;
        }

        public boolean hasNext() {
            return cursor != size();
        }

        public Edge next() {
            checkForComodification();
            try {
                Edge next = getEdge(cursor);
                lastRet = cursor++;
                return next;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        public Edge previous() {
            checkForComodification();
            try {
                int i = cursor - 1;
                Edge previous = getEdge(i);
                lastRet = cursor = i;
                return previous;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Edge edge) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(Edge edge) {
            throw new UnsupportedOperationException();
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor-1;
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }    

    public ListIterator<Edge> edgeIterator() {
        return new EdgeIterator(0);
    }

    public ListIterator<Edge> edgeIterator(int index) {
        return new EdgeIterator(index);
    }



    public java.awt.Polygon toAWTPolygon() {
        int size = size();
        int[] xpoints = new int[size];
        int[] ypoints = new int[size];
        
        for (int i = 0; i < size; ++i) {
            Point vertex = vertices.get(i);
            xpoints[i] = vertex.intX();
            ypoints[i] = vertex.intY();
        }

        return new java.awt.Polygon(xpoints, ypoints, size);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Polygon polygon = (Polygon) o;

        return !(vertices != null ? !vertices.equals(polygon.vertices) : polygon.vertices != null);
    }

    @Override
    public int hashCode() {
        return vertices != null ? vertices.hashCode() : 0;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Polygon clone;
        try {
            clone = (Polygon) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }

        return clone;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(Joiner.on(", ").join(vertices));
        builder.append("]");
        
        return builder.toString();
    }

    @Override
    public void draw(Graphics2D g) {
        Graphics2D graphics = (Graphics2D) g.create();
        graphics.draw(toAWTPolygon());
        graphics.dispose();

        /*int k = 0;
        for (Point vertex : vertices) {
            vertex.draw(g);
            g.drawString(String.valueOf(k), vertex.intX(), vertex.intY());
            k++;
        }*/

        //getCenter().draw(g);
    }
}
