public T getR() {
return realPart;
}

public U getI() {
return imaginaryPart;
}

public double modul() {
sb.append(String.format(&quot;%.2f&quot;, realPart.doubleValue()));
if (imaginaryPart.doubleValue() >= 0)
sb.append(&quot;+&quot;);
sb.append(String.format(&quot;%.2f&quot;, imaginaryPart.doubleValue()));

