if ((x >= 20 &amp;&amp; x <= 22.5) &amp;&amp; (y <= 13.5 &amp;&amp; y >= 8.5)) {
inRectangle = true;
}
double xA = 12.5;
double yA = 8.5;
double abc = Math.abs(xA * (yB - yC) + xB * (yC - yA) + xC * (yA - yB));
double abp = Math.abs(xA * (yB - y) + xB * (y - yA) + x * (yA - yB));

