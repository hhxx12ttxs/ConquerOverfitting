<<<<<<< HEAD
/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.coverage.io.hdf4.aps;

/**
 * @author Alessio Fabiani, GeoSolutions S.A.S.
 * 
 */
public class ColorRampGenerator {

    private static double[] getColour(String colorRampType, double v, double vmin, double vmax) {
        double[] c = new double[] { 1.0, 1.0, 1.0 }; // white
        
        if (colorRampType.equals("hot-color-ramp")) {
            c = getHotRampColor(v, vmin, vmax);
        }
        
        return c;
    }
    /**
     * Return a RGB colour value given a scalar v in the range [vmin,vmax] In
     * this case each colour component ranges from 0 (no contribution) to 1
     * (fully saturated), modifications for other ranges is trivial. The colour
     * is clipped at the end of the scales if v is outside the range [vmin,vmax]
     */
    private static double[] getHotRampColor(double v, double vmin, double vmax) {
        double[] c = new double[] { 1.0, 1.0, 1.0 }; // white
        double dv;

        if (v < vmin)
            v = vmin;
        if (v > vmax)
            v = vmax;
        dv = vmax - vmin;

        if (v < (vmin + 0.25 * dv)) {
            c[0] = 0;
            c[1] = 4 * (v - vmin) / dv;
        } else if (v < (vmin + 0.5 * dv)) {
            c[0] = 0;
            c[2] = 1 + 4 * (vmin + 0.25 * dv - v) / dv;
        } else if (v < (vmin + 0.75 * dv)) {
            c[0] = 4 * (v - vmin - 0.5 * dv) / dv;
            c[2] = 0;
        } else {
            c[1] = 1 + 4 * (vmin + 0.75 * dv - v) / dv;
            c[2] = 0;
        }

        return c;
    }
    
    public static void main(String[] args) {
        double min = 0.0001;
        double max = 32;
        
        final boolean useLogarithm = false;
        final double logMin = Math.log10(min);
        final double logMax = Math.log10(max);

        int intervals = 250;

        String colorRampType = "hot-color-ramp";

        double res = (max - min) / intervals;
        final double logRes = (logMax-logMin)/intervals;
        final double base10 = 10;
        final double base10Res = Math.pow(base10, logRes);
        
        final double usedRes = useLogarithm? base10Res : res;
        
        System.out.println("<ColorMapEntry color=\"#000000\" quantity=\"" + (min - usedRes) + "\" opacity=\"0.0\"/>");
        for (int c = 0; c <= intervals; c++) {
            double[] color = getColour(colorRampType, min + (c * res), min, max);
            final double step = c!=0? Math.pow(base10, logMin + c * logRes):0;
            final double usedStep = useLogarithm? step: c*res;
            String r = Integer.toHexString((int) Math.round(255.0 * color[0]));
            String g = Integer.toHexString((int) Math.round(255.0 * color[1]));
            String b = Integer.toHexString((int) Math.round(255.0 * color[2]));
            String hexColor = 
                (r.length() == 2 ? r : "0" + r) + 
                (g.length() == 2 ? g : "0" + g) + 
                (b.length() == 2 ? b : "0" + b);
            System.out.println("<ColorMapEntry color=\"#" + hexColor + "\" quantity=\"" + (min + usedStep) + "\"/>");
        }
        System.out.println("<ColorMapEntry color=\"#000000\" quantity=\"" + (max + usedRes) + "\" opacity=\"0.0\"/>");
    }
    
    
    
    
}
=======
package ru.etu.astamir.compression;

import com.google.common.base.*;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import ru.etu.astamir.common.UniqueIterator;
import ru.etu.astamir.geom.common.java.*;
import ru.etu.astamir.geom.common.java.Point;
import ru.etu.astamir.graphics.StrokeFactory;
import ru.etu.astamir.model.Bus;
import ru.etu.astamir.model.TopologyElement;
import ru.etu.astamir.model.TopologyLayer;
import ru.etu.astamir.model.TransistorActiveRegion;
import ru.etu.astamir.model.common.Pair;
import ru.etu.astamir.model.contacts.Contact;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Частокол. Набор отрезков с разными классами элементов.
 * Возможно в будущем придется пихать сюда сами элементы,
 * в случае с диагональными расстояниями от контактов ?
 */
// TODO FIXME !!!!
public class Border {

    TopologyLayer layer;

    /**
     * Ориентация частокола. Частокол может быть вертикальным или горизонтальным.
     * НО добавлять можно элементы разной ориентации (будем толерантны).
     */
    private final Orientation orientation;

    /**
     * Элементы частокола.
     */
    private List<BorderPart> parts = Lists.newArrayList();

    public Border(Orientation orientation) {
        this.orientation = Preconditions.checkNotNull(orientation);
    }

    public Border(Orientation orientation, Collection<BorderPart> parts) {
        this.orientation = Preconditions.checkNotNull(orientation);
        setParts(parts);
    }
    
    public static Border of(Orientation orientation, List<Edge> edges, Class<? extends TopologyElement> clazz) {
        Border border = new Border(orientation);
        List<BorderPart> parts = Lists.newArrayList();
        for (Edge edge : edges) {
            parts.add(new BorderPart(edge, clazz));
        }

        border.parts = parts;
        return border;
    }

    public static Border emptyBorder(Orientation orientation) {
        return new Border(orientation);
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public List<BorderPart> getParts() {
        return parts;
    }

    public List<Edge> getEdges() {
        return Lists.transform(parts, new Function<BorderPart, Edge>() {
            @Override
            public Edge apply(BorderPart input) {
                return input.getAxis();
            }
        });
    }

    public void addPart(Edge axis, Class<? extends TopologyElement> clazz) {
        parts.add(new BorderPart(axis, clazz));
    }

    public void setParts(Collection<BorderPart> parts) {
        this.parts.clear();
        this.parts.addAll(parts);
    }

    public Predicate<BorderPart> orientationPredicate() {
        return new Predicate<BorderPart>() {
            @Override
            public boolean apply(BorderPart input) {
                return input.getAxis().getOrientation() == orientation;
            }
        };
    }


    static List<BorderPart> singleOverlay(BorderPart was, BorderPart added, final Orientation orientation, Direction dir) {
        List<BorderPart> result = Lists.newArrayList();

        // preparing our parts
        was.correct();
        added.correct();
        
        Edge addedAxis = added.getAxis();
        Edge wasAxis = was.getAxis();
        
        Edge ourTopRay = Edge.ray(addedAxis.getEnd(), dir);
        Edge ourBotRay = Edge.ray(addedAxis.getStart(), dir);

        boolean topCross = wasAxis.cross(ourTopRay) == Edge.EdgeRelation.SKEW_CROSS;
        if (topCross) {
            result.add(new BorderPart(Edge.of(wasAxis.crossing(ourTopRay), wasAxis.getEnd()), was.getClazz()));
        }

        boolean botCross = wasAxis.cross(ourBotRay) == Edge.EdgeRelation.SKEW_CROSS;
        if (botCross) {
            result.add(new BorderPart(Edge.of(wasAxis.getStart(), wasAxis.crossing(ourBotRay)), was.getClazz()));
        }        
        
        if (!topCross && !botCross) {
            Edge reverseRay = Edge.ray(wasAxis.getStart(), dir.getOppositeDirection());
            if (reverseRay.cross(addedAxis) == Edge.EdgeRelation.SKEW_CROSS) {
                return Lists.newArrayList();
            } else {
                return Lists.newArrayList(was);
            }
        }

        return result;
    }

    /**
     *
     * @param newParts
     * @param dir
     */
    public void overlay(List<BorderPart> newParts, Direction dir) {
        List<BorderPart> result = Lists.newArrayList();

        final Predicate<BorderPart> orientationPredicate = orientationPredicate();
        List<BorderPart> allParts = Lists.newArrayList(Iterables.filter(Iterables.concat(newParts, parts),
                orientationPredicate)); // we need only parts of border's orientation to work with.

        List<List<BorderPart>> columns = divideParts(allParts, dir); // lets divide our parts into sorted columns.
        int columnSize = columns.size();
        for (int i = 0; i < columnSize; i++) {
            // find i-st column
            List<BorderPart> column = columns.get(i);
            result.addAll(column); // this column is ok, we can add it.

            // now we hate to cut added parts out of all remaining ones.
            for (BorderPart part : column) {
                for (int j = i + 1; j < columnSize; j++) {
                    for (ListIterator<BorderPart> k = columns.get(j).listIterator(); k.hasNext();) {
                        BorderPart burningPart = k.next();
                        k.remove();
                        List<BorderPart> overlay = singleOverlay(burningPart, part, orientation, dir);
                        for (BorderPart o : overlay) {
                            k.add(o);
                        }                        
                    }
                }
            }
        }

        parts = correctParts(result); // now we got to correct all new parts. although it seems to me that they are already good.
        connectParts(); // and we connect all the parts, since we were working only with orientational ones.
    }


    /**
     * Соединяет элементы частокола. Предполагается, что
     * все элементы скорректированы.
     *
     * (Пока останется так, но можно запихивать соединительные
     * части в конец списка, тем самым не возвращаясь на шаг назад.)
     */
    // TODO классы соединительных частей
    private void connectParts() {
        Collections.sort(parts, BorderPart.getAxisComparator(orientation == Orientation.VERTICAL ?
                Direction.UP.getEdgeComparator(false) : Direction.RIGHT.getEdgeComparator(false)));
        for (ListIterator<BorderPart> i = parts.listIterator(); i.hasNext();) {
            BorderPart cur = i.next();
            if (i.hasNext()) {
                BorderPart next = i.next();
                i.previous();
                Class<? extends TopologyElement> nextClazz = next.getClazz();
                if (nextClazz.equals(TransistorActiveRegion.class)) {
                    nextClazz = cur.getClazz();
                }

                if (cur.getClazz().equals(Contact.class)) {
                    nextClazz = cur.getClazz();
                }
                i.add(new BorderPart(Edge.of(cur.getAxis().getEnd(), next.getAxis().getStart()), nextClazz));
            }
        }
    }

    /**
     * Удаляет все вырожденные отрезки и переворачивает те, которые
     * направлены вниз или влево.
     *
     * @param parts
     * @return
     */
    private List<BorderPart> correctParts(List<BorderPart> parts) {
        for (BorderPart part : parts) {
            Edge axis = part.getAxis();
            if (!axis.getDirection().isUpOrRight()) {
                axis.reverse();
            }
        } // reversing all down or left oriented parts

        return Lists.newArrayList(Iterators.filter(UniqueIterator.create(parts.iterator()), new Predicate<BorderPart>() {
            @Override
            public boolean apply(BorderPart input) {
                return !input.getAxis().isPoint();
            }
        }));
    }

    /**
     * Получает соритрованые пачки кусков частокола.
     * 
     * @param unsortedParts 
     * @param dir
     * @return
     */
    private List<List<BorderPart>> divideParts(List<BorderPart> unsortedParts, Direction dir) {
        List<List<BorderPart>> result = Lists.newArrayList();        
        Collections.sort(unsortedParts, BorderPart.getAxisComparator(dir.getEdgeComparator()));
        int size = unsortedParts.size();
        for (int i = 0; i < size; i++) {
            List<BorderPart> parts = Lists.newArrayList();
            BorderPart curPart = unsortedParts.get(i);
            parts.add(curPart);
            for (int j = i + 1; j < size; j++) {
                BorderPart nextPart = unsortedParts.get(j);
                if (nextPart.equals(curPart)) {
                    parts.add(nextPart);
                } else {
                    break;
                }
            }
            
            result.add(parts);
        }
        
        return result;        
    }


    public Optional<BorderPart> getClosestPart(Point point, Direction direction) {
        Edge ray = Edge.ray(point, direction);
        List<Pair<BorderPart, Double>> distances = Lists.newArrayList();
        for (BorderPart part : orientationParts()) {
            Edge axis = part.getAxis();
            if (axis.cross(ray) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, axis.distanceToPoint(point)));
            }
        }

        if (distances.isEmpty()) {
            return Optional.absent();
        }
        
        return Optional.of(Collections.min(distances, new Comparator<Pair<BorderPart, Double>>() {
            @Override
            public int compare(Pair<BorderPart, Double> o1, Pair<BorderPart, Double> o2) {
                return o1.right.compareTo(o2.right);
            }
        }).left);
    }

    public Optional<BorderPart> getClosestPartWithConstraints(Edge edge, Class<? extends TopologyElement> clazz, Direction direction) {
        Edge topRay = Edge.ray(edge.getStart(), direction);
        Edge botRay = Edge.ray(edge.getEnd(), direction);
        double sign = -1;//direction.getDirectionSign();

        List<Pair<BorderPart, Double>> distances = Lists.newArrayList();
        for (BorderPart part : orientationParts(direction)) {
            double min = sign * part.getMinDistance(clazz);
            Edge axis = part.getAxis();
            if (axis.cross(topRay) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, axis.distanceToPoint(edge.getStart()) + min));
            }

            if (axis.cross(botRay) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, axis.distanceToPoint(edge.getEnd()) + min));
            }

            Edge bRay = Edge.ray(axis.getStart(), direction.getOppositeDirection());
            Edge tRay = Edge.ray(axis.getEnd(), direction.getOppositeDirection());

            if (bRay.cross(edge) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, edge.distanceToPoint(axis.getStart()) + min));
            }

            if (tRay.cross(edge) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, edge.distanceToPoint(axis.getEnd()) + min));
            }
        }

        if (distances.isEmpty()) {
            return Optional.absent();
        }

        return Optional.of(Collections.min(distances, new Comparator<Pair<BorderPart, Double>>() {
            @Override
            public int compare(Pair<BorderPart, Double> o1, Pair<BorderPart, Double> o2) {
                return o1.right.compareTo(o2.right);
            }
        }).left);
    }

    public Optional<BorderPart> getClosestPartWithoutConstraints(Edge edge, Direction direction) {
        Edge topRay = Edge.ray(edge.getStart(), direction);
        Edge botRay = Edge.ray(edge.getEnd(), direction);

        List<Pair<BorderPart, Double>> distances = Lists.newArrayList();
        for (BorderPart part : orientationParts()) {
            Edge axis = part.getAxis();
            if (axis.cross(topRay) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, axis.distanceToPoint(edge.getStart())));
            }

            if (axis.cross(botRay) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, axis.distanceToPoint(edge.getEnd())));
            }

            Edge bRay = Edge.ray(axis.getStart(), direction.getOppositeDirection());
            Edge tRay = Edge.ray(axis.getEnd(), direction.getOppositeDirection());

            if (bRay.cross(edge) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, edge.distanceToPoint(axis.getStart())));
            }

            if (tRay.cross(edge) == Edge.EdgeRelation.SKEW_CROSS) {
                distances.add(Pair.of(part, edge.distanceToPoint(axis.getEnd())));
            }
        }

        if (distances.isEmpty()) {
            return Optional.absent();
        }

        return Optional.of(Collections.min(distances, new Comparator<Pair<BorderPart, Double>>() {
            @Override
            public int compare(Pair<BorderPart, Double> o1, Pair<BorderPart, Double> o2) {
                return o1.right.compareTo(o2.right);
            }
        }).left);
    }

    public Optional<BorderPart> getMaxPart(Direction direction) {
        List<BorderPart> orientationParts = orientationParts();
        if (orientationParts.isEmpty()) {
            return Optional.absent();
        }

        Comparator<BorderPart> cmp = BorderPart.getAxisComparator(direction.getEdgeComparator());

        return Optional.of(Collections.max(orientationParts, cmp));
    }
    
    public List<BorderPart> orientationParts() {
        return Lists.newArrayList(Iterables.filter(parts, new Predicate<BorderPart>() {
            @Override
            public boolean apply(BorderPart input) {
                return input.getAxis().getOrientation() == orientation;
            }
        }));
    }

    public List<BorderPart> orientationParts(final Direction direction) {
        return Lists.newArrayList(Iterables.filter(parts, new Predicate<BorderPart>() {
            @Override
            public boolean apply(BorderPart input) {
                return input.getAxis().getOrientation() == direction.getOrthogonalDirection().toOrientation();
            }
        }));
    }

    // TODO
    public void imitate(Bus bus, Direction direction) {
      //  bus.correctBus();
        createEmptyLinks(bus, direction);
        for (Bus.BusPart part : bus.getParts()) {
            if (!part.getAxis().getOrientation().isOrthogonal(direction.toOrientation()) || part.getAxis().isPoint()) {
                continue;
            }
            Optional<BorderPart> closestPart = getClosestPartWithoutConstraints(part.getAxis(), direction);
            if (closestPart.isPresent()) {
                double distToMove = closestPart.get().getMoveDistance(bus.getClass(),
                        direction, part.getAxis().getStart());

                bus.directlyMovePart(part, direction, distToMove);
            }
        }

        bus.removeEmptyParts();
    }

    // TODO
    private void createEmptyLinks(Bus bus, Direction direction) {
        List<BorderPart> nonOrientParts =
                Lists.newArrayList(Iterables.filter(parts, Predicates.not(orientationPredicate())));

        for (BorderPart part : nonOrientParts) {
            double min = part.getMinDistance(bus.getClass());
            Point one = (Point) part.getAxis().getStart().clone();
            GeomUtils.move(one, direction.clockwise(), min);
            Point another = (Point) part.getAxis().getStart().clone();
            GeomUtils.move(another, direction.counterClockwise(), min);
            
            bus.createAnEmptyLink(one, direction.getOppositeDirection());
            bus.createAnEmptyLink(another, direction.getOppositeDirection());
        }
    }


    // TODO
    public void draw(Graphics2D graphics) {
        Graphics2D clone = (Graphics2D) graphics.create();
        clone.setColor(Color.RED);
        clone.setStroke(StrokeFactory.highlightedDefaultStroke());
        for (BorderPart part : parts) {
            clone.setColor(part.getClazz().equals(Contact.class) ? Color.GREEN : part.getClazz().equals(Bus.BusPart.class) ? Color.RED : Color.BLACK);
            part.getAxis().draw(clone);
        }

        clone.dispose();
    }

}

>>>>>>> 76aa07461566a5976980e6696204781271955163
