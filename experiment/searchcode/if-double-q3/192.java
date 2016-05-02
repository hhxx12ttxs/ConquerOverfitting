package com.hackbulgaria.corejava1.threads;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PointsCalculator {
    public static List<Point> pointsList = new ArrayList<Point>();

    private PointsCalculator() {

    }

    public static List<Point> generatePointsList(int points) {
        for (int i = 0; i < points; i++) {
            pointsList.add(generatePoint());
        }
        return pointsList;
    }

    private static Point generatePoint() {
        Random rand = new Random();
        int x = rand.nextInt(10_001);
        int y = rand.nextInt(10_001);
        Point point = new Point(x, y);
        return point;
    }

    // public static Map<Point, Point> getNearestPoints(List<Point>
    // generatedPoints) {
    // Map<Point, Point> nearestPoints = new HashMap<Point, Point>();
    // int pointsCount = generatedPoints.size();
    // Point firstPoint = null;
    // Point secondPoint = null;
    // Double minDistance;
    // int minDistIndex = 0;
    // for (int i = 0; i < pointsCount; i++) {
    // firstPoint = generatedPoints.get(i);
    // minDistance = 10_000.0;
    // for (int j = 0; j < pointsCount; j++) {
    // if (j != i) {
    // secondPoint = generatedPoints.get(j);
    // if (firstPoint.distance(secondPoint) < minDistance) {
    // minDistance = firstPoint.distance(secondPoint);
    // minDistIndex = j;
    // }
    // }
    // }
    // nearestPoints.put(firstPoint, generatedPoints.get(minDistIndex));
    // }
    // return nearestPoints;
    // }

    public static Map<Point, Point> getNearestPoints(final List<Point> generatedPoints) throws InterruptedException {
        final Map<Point, Point> nearestPoints = Collections.synchronizedMap(new HashMap<Point, Point>());
        final int pointsCount = generatedPoints.size();
        final int q1 = pointsCount / 4;
        final int mid = pointsCount / 2;
        final int q3 = pointsCount / 4 * 3;

        final Thread t1 = new Thread() {
            @Override
            public void run() {
                doCalculations(generatedPoints, 0, q1, nearestPoints);
            }
        };

        final Thread t2 = new Thread() {
            @Override
            public void run() {
                doCalculations(generatedPoints, q1 + 1, mid, nearestPoints);
            }
        };

        final Thread t3 = new Thread() {
            @Override
            public void run() {
                doCalculations(generatedPoints, mid + 1, q3, nearestPoints);
            }
        };

        final Thread t4 = new Thread() {
            @Override
            public void run() {
                doCalculations(generatedPoints, q3 + 1, pointsCount - 1, nearestPoints);
            }
        };

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();
        // doCalculations(generatedPoints, 0, mid, nearestPoints);
        // doCalculations(generatedPoints, mid + 1, pointsCount - 1,
        // nearestPoints);
        return nearestPoints;
    }

    public static void doCalculations(List<Point> inPoints, int indexFrom, int indexTo, Map<Point, Point> outMap) {
        Point firstPoint = null;
        Point secondPoint = null;
        Double minDistance;
        int minDistIndex = 0;
        for (int i = indexFrom; i <= indexTo; i++) {
            firstPoint = inPoints.get(i);
            minDistance = 10_000.0;
            for (int j = 0; j < inPoints.size(); j++) {
                if (j != i) {
                    secondPoint = inPoints.get(j);
                    if (firstPoint.distance(secondPoint) < minDistance) {
                        minDistance = firstPoint.distance(secondPoint);
                        minDistIndex = j;
                    }
                }
            }
            outMap.put(firstPoint, inPoints.get(minDistIndex));
        }
    }

}

