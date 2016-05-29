private final int upper;

public Interval(int lower, int upper){
if (lower > upper){throw new IllegalArgumentException(&quot;lower should be less than or equal to upper&quot;);}
public boolean intersects(Interval other){
//if other.lower is between this.lower &amp;&amp; this.upper
//or other.upper is between this.lower &amp;&amp; this.upper

