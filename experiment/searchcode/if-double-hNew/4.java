double hNew = hue / 60.0;
double x = 1 - Math.abs((hNew % 2) - 1);
float brightness = 1;
if(hNew >= 0 &amp;&amp; hNew < 1) baseColor = new Color(brightness, (float) x * brightness, 0.0f);

