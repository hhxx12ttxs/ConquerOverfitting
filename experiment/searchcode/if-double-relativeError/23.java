public Integrator(@Parameter int order, @Parameter double initLevel, @Parameter double relativeError, @Parameter double minimumPrecision) {

state  = DoubleOutport.Double(&quot;state&quot;, this);
default : throw new RuntimeException(&quot;Invalid order of integration specified (was &quot;+ order+&quot;)&quot;);
}

if (relativeError >= 0 &amp;&amp; minimumPrecision > 0) {

