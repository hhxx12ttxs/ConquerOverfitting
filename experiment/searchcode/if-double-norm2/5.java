for (Double scalar : vec1.values()) { norm1 += Math.pow(scalar,2); }
norm1 = Math.sqrt(norm1);
double norm2 = 0.0d;
for (Double scalar : vec2.values()) { norm2 += Math.pow(scalar,2); }
return (norm1>0d &amp;&amp; norm2>0d) ? inner_product /(norm1*norm2) : 0.0d;
}


private static double norm(Map<String, Double> vec) {

