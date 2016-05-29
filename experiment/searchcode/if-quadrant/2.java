package org.tianlin.java.exercise4.question1;

public enum Quadrant {
one, two, three, four;
public static Quadrant getQuadrant(double x, double y) {
if (x > 0 &amp;&amp; y > 0) {
return one;

