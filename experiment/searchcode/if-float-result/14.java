public static MathResult add(float a, float b) {
return new MathResult(&quot;Success!&quot;, Float.toString(a+b));
}

public static MathResult add(String a, String b) {
public static MathResult divide(float a, float b) {
if(b == 0) {
return new MathResult(&quot;Divide by Zero Error&quot;,&quot;0&quot;);
} else {

