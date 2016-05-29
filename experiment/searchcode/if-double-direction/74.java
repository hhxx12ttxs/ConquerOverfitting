package main.java;

public class Velocity {
private double speed;
private double direction;
if(speed < 0.0 || speed > 100.0) throw new RuntimeException(&quot;Invalid speed: &quot; + Double.toString(speed) + &quot;\nValue must be between 0.0 and 100.0, inclusive.&quot;);
else this.speed = speed;
}

public void setDirection(double direction) {
if(direction < 0.0 || direction > 360.0) throw new RuntimeException(&quot;Invalid direction: &quot; + Double.toString(speed) + &quot;\nValue must be between 0.0 and 360.0, inclusive.&quot;);

