package utils;

import shapes.shapes.Point;

/**
 * Created by IntelliJ IDEA.
 * User: ZOR
 * Date: 03.12.11
 * Time: 17:33
 */
public class DelaunayUtils {

    public static boolean InCircle(Point p, Point p1, Point p2, Point p3) {

        //Return TRUE if the point (xp,yp) lies inside the circumcircle
        //made up by points (x1,y1) (x2,y2) (x3,y3)
        //NOTE: A point on the edge is inside the circumcircle

        double Epsilon = 0.01f;

        double p2y = p2.y;
        double p1y = p1.y;
        double p3y = p3.y;
        if (Math.abs(p1y - p2y) < Epsilon && Math.abs(p2y - p3y) < Epsilon) {
            //INCIRCUM - F - Points are coincident !!
            return false;
        }


        double m1;
        double m2;
        double mx1;
        double mx2;
        double my1;
        double my2;
        double xc;
        double yc;

        double p3x = p3.x;
        double p2x = p2.x;
        double p1x = p1.x;
        if (Math.abs(p2y - p1y) < Epsilon) {
            m2 = -(p3x - p2x) / (p3y - p2y);
            mx2 = (p2x + p3x) * 0.5;
            my2 = (p2y + p3y) * 0.5;
            //Calculate CircumCircle center (xc,yc)
            xc = (p2x + p1x) * 0.5;
            yc = m2 * (xc - mx2) + my2;
        } else if (Math.abs(p3y - p2y) < Epsilon) {
            m1 = -(p2x - p1x) / (p2y - p1y);
            mx1 = (p1x + p2x) * 0.5;
            my1 = (p1y + p2y) * 0.5;
            //Calculate CircumCircle center (xc,yc)
            xc = (p3x + p2x) * 0.5;
            yc = m1 * (xc - mx1) + my1;
        } else {
            m1 = -(p2x - p1x) / (p2y - p1y);
            m2 = -(p3x - p2x) / (p3y - p2y);
            mx1 = (p1x + p2x) * 0.5;
            mx2 = (p2x + p3x) * 0.5;
            my1 = (p1y + p2y) * 0.5;
            my2 = (p2y + p3y) * 0.5;
            //Calculate CircumCircle center (xc,yc)
            xc = (m1 * mx1 - m2 * mx2 + my2 - my1) / (m1 - m2);
            yc = m1 * (xc - mx1) + my1;
        }

        double dx = p2x - xc;
        double dy = p2y - yc;
        double rsqr = dx * dx + dy * dy;
        //double r = Math.Sqrt(rsqr); //Circumcircle radius
        dx = p.x - xc;
        dy = p.y - yc;
        double drsqr = dx * dx + dy * dy;

        return (drsqr <= rsqr);


    }
}

