private void calculate(double left, double right){
this.result = left + right;

if(right > 0) { if(result < left) throw new RuntimeException (&quot;double overflow&quot;);}
if(left > Double.MAX_VALUE || right > Double.MAX_VALUE) throw new RuntimeException (&quot;double too big&quot;);
if(left < Double.MIN_VALUE || right < Double.MIN_VALUE) throw new RuntimeException (&quot;double too small&quot;);
}

}

