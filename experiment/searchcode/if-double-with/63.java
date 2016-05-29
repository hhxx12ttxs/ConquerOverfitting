for (Double val:values){
if (val!=null &amp;&amp; val.doubleValue()<min.doubleValue())
min=val;
else if (val==null &amp;&amp; min>0)
min=0d;
}
if(min.isInfinite())
return 0;
else
return min.doubleValue();

