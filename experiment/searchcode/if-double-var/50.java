return varNames().get(0);
else
return varNames().get(0)+&quot;+&quot;+b;
} else if(Double.compare(a, 0.0) == 0) {
return b+&quot;&quot;;
} else if(Double.compare(b, 0.0) == 0) {
return a+&quot;*&quot;+varNames().get(0);

