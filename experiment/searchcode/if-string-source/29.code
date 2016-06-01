public static float getFloat(String source, String name) {
int j,i=source.indexOf(name);
if (i<0) return Float.NaN;
j=source.indexOf(&#39; &#39;,i+3);
if (j<0) j=source.length();
return Float.valueOf(source.substring(name.length()+i+1,j));

