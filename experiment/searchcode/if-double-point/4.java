package ita.br.main;

public class SpacialPoint implements WayPoint, Cloneable {

private double pointX;
+ &quot;, &quot; + this.getPointZ() + &quot;)&quot;;
}

@Override
public double toPointDistance(WayPoint p) {
if (!(p instanceof SpacialPoint)) {

