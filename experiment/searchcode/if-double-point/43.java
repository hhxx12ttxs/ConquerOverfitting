package homework1;

public class Point {
public double x;
public double y;
public Point(double $x, double $y) {
public boolean equals(Object other) {

Point o = (Point) other;

double eps = 0.01;
if (Math.abs(o.x - this.x) < eps &amp;&amp; Math.abs(o.y - this.y) < eps) {

