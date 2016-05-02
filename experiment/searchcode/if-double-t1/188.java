// GraphLab Project: http://graphlab.sharif.edu
// Copyright (C) 2008 Mathematical Science Department of Sharif University of Technology
// Distributed under the terms of the GNU General Public License (GPL): http://www.gnu.org/licenses/
package graphlab.plugins.visualization.treevisualizations;

import graphlab.graph.graph.Edge;
import graphlab.graph.graph.GraphModel;
import graphlab.graph.graph.Vertex;
import graphlab.library.BaseVertexProperties;
import graphlab.platform.preferences.lastsettings.UserModifiableProperty;
import graphlab.platform.core.exception.ExceptionHandler;
import graphlab.plugins.graphgenerator.core.PositionGenerators;
import graphlab.plugins.visualization.corebasics.extension.VisualizationExtension;
import graphlab.ui.UIUtils;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author Rouzbeh Ebrahimi
 */
public class BackwardTrees implements VisualizationExtension {
    String event = UIUtils.getUIEventKey("BackwardTrees");
    public Vector<Vertex> visitedVertices = new Vector<Vertex>();
    public Vector<Vertex> upperLevelVertices = new Vector<Vertex>();

    public HashMap<Vertex, Point2D> vertexPlaces = new HashMap<Vertex, Point2D>();
    public Vector<Vertex> children = new Vector<Vertex>();
    public HashMap<Vertex, Double> comingFrom = new HashMap<Vertex, Double>();
    Vertex root;


    static GraphModel g;
    /**
     * @param eventName
     * @param value
     */
    public HashSet<Vertex> placedVertices = new HashSet<Vertex>();
    @UserModifiableProperty(displayName = "BackWard Tree Visualization Radius", obeysAncestorCategory = false
            , category = "Visualization Options")
    public static Integer radius = 60;

    private Vertex findHigherVertex(Vertex v1, Vertex v2) {
        Vector<Vertex> t1 = new Vector<Vertex>();
        Vector<Vertex> t2 = new Vector<Vertex>();
        t1.add(v1);
        t2.add(v2);
        if (BFS(t1, 0) > BFS(t2, 0)) {
            return v1;
        } else {
            return v2;
        }
    }

    private Vertex findAppropriateRoot(GraphModel g) {
        Vertex root = g.getAVertex();
        Iterator<Vertex> ei = g.iterator();
        for (; ei.hasNext();) {
            Vertex e = ei.next();
            root = findHigherVertex(e, root);
        }
        return root;
    }

    private int BFS(Vector<Vertex> currentLevel, int maxLevel) {
        Vector<Vertex> nextLevel = new Vector<Vertex>();
        for (Vertex v : currentLevel) {
            v.setMark(true);
            Iterator<Edge> em = g.edgeIterator(v);
            for (; em.hasNext();) {
                Edge e = em.next();
                Vertex v2 = e.source;
                if (!v2.getMark()) {
                    nextLevel.add(v2);
                    v2.setMark(true);
                }
            }
        }
        maxLevel++;
        if (nextLevel.size() != 0) {
            return BFS(nextLevel, maxLevel);
        } else {
            return maxLevel;
        }
    }

    public Vector<Vertex> findNextLevelChildren(Vector<Vertex> currentLevelVertices) {
        Vector<Vertex> newChildren = new Vector<Vertex>();
        if (currentLevelVertices.size() != 0) {
            for (Vertex v : currentLevelVertices) {
                Iterator<Edge> e = g.edgeIterator(v);
                for (; e.hasNext();) {
                    Edge ed = e.next();
                    Vertex dest = ed.source;
                    if (!visitedVertices.contains(dest)) {
                        newChildren.add(dest);
                    }
                }
            }
        } else {
        }
        return newChildren;
    }

    public void locateAll(Vector<Vertex> currentLevelVertices, int width, int currentLevelHeight, int level, int radius) {

        int currentLevelCount = currentLevelVertices.size();
        int horizontalDist = width / currentLevelCount;
        int i = 0;
        Vector<Vertex> nextLevel = findNextLevelChildren(currentLevelVertices);
        if (currentLevelCount == 1 && currentLevelVertices.elementAt(0).equals(root)) {
            Point2D.Double newPoint = new Point2D.Double(200, 200);
            vertexPlaces.put(root, newPoint);
            comingFrom.put(root, 0.0);
        } else {
            for (Vertex v : upperLevelVertices) {
                Iterator<Edge> ei = g.edgeIterator(v);
                int degree = g.getInDegree(v);
                double p = comingFrom.get(v);
                int j = 0;

                double phase;
                phase = 360 / (degree);
                Point2D[] circle = PositionGenerators.circle((int) v.getLocation().getX(), (int) v.getLocation().getY(), radius, radius, degree);
                int t = 0;
                for (; ei.hasNext();) {
                    Vertex ver = ei.next().source;
//                    double x;
//                    double y;
//                    double xPhase = Math.cos(((j * phase)+p) * Math.PI / 180);
//                    x = vertexPlaces.get(v).getX() + radius * xPhase;
//                    double yPhase = Math.sin(((j * phase)+p ) * Math.PI / 180);
//                    y = vertexPlaces.get(v).getY() + radius * yPhase;
                    comingFrom.put(ver, (j * phase) + p);
//                     Point2D.Double newPoint = new Point2D.Double(x, y);
                    vertexPlaces.put(ver, circle[t]);
                    j++;
                    t++;

                }
            }

//        for (Vertex v : currentLevelVertices) {
//            Point2D.Double newPoint = new Point2D.Double(horizontalDist * i + width / (currentLevelCount + 1), currentLevelHeight);
//            vertexPlaces.put(v,newPoint);
//            i++;
//        }
        }
        upperLevelVertices = currentLevelVertices;
        if (!nextLevel.isEmpty()) {
            visitedVertices.addAll(nextLevel);
            locateAll(nextLevel, width, currentLevelHeight + 30, level + 1, radius * 9 / 16);
        } else {
            return;
        }
    }

    private void unMarkVertices() {
        for (Vertex v : g) {
            v.setMark(false);
        }
    }


    public void locateAllSubTrees(Vertex v, double radius, double offSet) {
        if (placedVertices.contains(root)) {
            double angularSpan = (Double) v.getProp().obj;
            int numberOfDivides = 1;
            numberOfDivides = g.getInDegree(v);
            if (numberOfDivides == 0) {
                return;
            }
            Iterator<Edge> iter = g.edgeIterator(v);
            int sum = 0;
            for (; iter.hasNext();) {
                Edge e = iter.next();
                Vertex v1 = e.source.equals(v) ? e.target : e.source;
                if (!placedVertices.contains(v1)) {
                    sum += g.getInDegree(v1);
                } else {
                }
            }
            iter = g.edgeIterator(v);
            int j = 1;
            for (; iter.hasNext();) {
                Edge e = iter.next();
                Vertex v1 = e.source.equals(v) ? e.target : e.source;
                if (!placedVertices.contains(v1)) {
                    double x = 350 + radius * Math.cos((angularSpan * j / (numberOfDivides) + offSet));
                    double y = 350 + radius * Math.sin((angularSpan * j / (numberOfDivides) + offSet));
                    double newOffset = (angularSpan * j / numberOfDivides + offSet);
                    Point2D.Double newPoint = new Point2D.Double(x, y);
                    vertexPlaces.put(v1, newPoint);
                    placedVertices.add(v1);
                    BaseVertexProperties properties = new BaseVertexProperties(v1.getColor(), v1.getMark());
                    properties.obj = new Double((angularSpan / (Math.abs(sum))) * (g.getInDegree(v1)));
                    v1.setProp(properties);
                    locateAllSubTrees(v1, this.radius + radius, newOffset);
                    j++;

                } else {
                }

            }
            return;
        } else {
            double x = 350;
            double y = 350;
            Point2D.Double newPoint = new Point2D.Double(x, y);
            placedVertices.add(v);
            vertexPlaces.put(v, newPoint);
            locateAllSubTrees(v, radius, offSet);
        }
    }

    public String getName() {
        return "BackWard Tree Visualization";
    }

    public String getDescription() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }/*
     @param g
    */

    public void setWorkingGraph(GraphModel g) {
        this.g = g;
    }

    public HashMap<Vertex, Point2D> getNewVertexPlaces() {
        visitedVertices = new Vector<Vertex>();
        vertexPlaces = new HashMap<Vertex, Point2D>();
        children = new Vector<Vertex>();
        placedVertices = new HashSet<Vertex>();
        try {
            root = findAppropriateRoot(g);
            visitedVertices.add(root);
            unMarkVertices();
//            locateAll(visitedVertices, 600, 50, 1,200);
            BaseVertexProperties properties = new BaseVertexProperties(root.getColor(), root.getMark());
            properties.obj = new Double(2 * Math.PI);
            root.setProp(properties);

            locateAllSubTrees(root, radius, 0);
//            GeneralAnimator t = new GeneralAnimator(vertexPlaces, g, blackboard);
//            t.start();
        } catch (NullPointerException e) {
//            System.out.println("Graph is Empty");
            ExceptionHandler.catchException(e);
        }
        return vertexPlaces;
    }

    public HashMap<Edge, Point2D> getNewEdgeCurveControlPoints() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}


