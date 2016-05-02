package pt.uc.kidsnoop.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

public class GrahamScan {

    public static ArrayList<Point> grahamScan(ArrayList<Point> polygon) {
        int min = 0;
        int length = polygon.size();
        for (int i = 1; i < length; i++) {
            Point get = polygon.get(i);
            if (get.getLongitude() < polygon.get(min).getLongitude()) {
                min = i;
            } else {
                if (get.getLongitude() == polygon.get(min).getLongitude()) {
                    if (get.getLatitude() < polygon.get(min).getLatitude()) {
                        min = i;
                    }
                }
            }
        }
        final Point pivot = polygon.get(min);
        ArrayList<Point> sorted = (ArrayList<Point>) polygon.clone();
        Collections.sort(sorted, new Comparator<Point>() {

            public int compare(Point o1, Point o2) {
                if (o1.equals(o2)) {
                    return 0;
                }
                if (angle_cmp(pivot, o1, o2)) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        sorted.add(0, pivot);
        Stack<Point> stack = new Stack<Point>();
        stack.push(sorted.get(length - 1));
        stack.push(pivot);
        int i = 1;
        while (i < length) {
            Point pt1 = stack.pop();
            Point pt2 = stack.peek();
            stack.push(pt1);
            if (isLeftTurn(pt1, pt2, sorted.get(i))) {
                stack.push(sorted.get(i));
                i++;
            } else {
                stack.pop();
            }
        }
        ArrayList<Point> convex = new ArrayList<Point>();
        while (!stack.isEmpty()) {
            convex.add(stack.pop());
        }
        convex.remove(convex.size() - 1);
        return convex;
    }

    private static double distance(Point a, Point b) {
        double dx = a.getLatitude() - b.getLatitude(), dy = a.getLongitude() - b.getLongitude();
        return dx * dx + dy * dy;
    }

    private static double area(Point a, Point b, Point c) {
        return a.getLatitude() * b.getLongitude() - a.getLongitude() * b.getLatitude() + b.getLatitude() * c.getLongitude() - b.getLongitude() * c.getLatitude() + c.getLatitude() * a.getLongitude() - c.getLongitude() * a.getLongitude();
    }

    private static boolean angle_cmp(Point pivot, Point a, Point b) {
        if (area(pivot, a, b) == 0) {
            return distance(pivot, a) < distance(pivot, b);
        }
        double d1x = a.getLatitude() - pivot.getLatitude(), d1y = a.getLongitude() - pivot.getLongitude();
        double d2x = b.getLatitude() - pivot.getLatitude(), d2y = b.getLongitude() - pivot.getLongitude();
        return (Math.atan2((double) d1y, (double) d1x) - Math.atan2((double) d2y, (double) d2x)) < 0;
    }

    private static double turnTest(Point p, Point q, Point r) {
        double result = (r.getLatitude() - q.getLatitude()) * (p.getLongitude() - q.getLongitude()) - (r.getLongitude() - q.getLongitude()) * (p.getLatitude() - q.getLatitude());
        if (result < 0) {
            return -1;
        }
        if (result > 0) {
            return 1;
        }
        return 0;
    }

    private static boolean isLeftTurn(Point p, Point q, Point r) {
        return turnTest(p, q, r) > 0;
    }
}
