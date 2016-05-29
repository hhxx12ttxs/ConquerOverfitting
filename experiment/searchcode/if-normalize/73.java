double maxX;
double minX;

public void getNormalizeDataset(String sTemperatur, String sKelembaban, String sRainfall, String sStatRain) throws IOException {
System.out.println(&quot;NORM:&quot; + temperatur + &quot;,&quot; + kelembaban + &quot;,&quot; + rainfall + &quot;,&quot; + statRain + &quot;(Max: &quot; + maxX + &quot;,Min: &quot; + minX + (&quot;)&quot;));
textNormalizedDataset += &quot;&quot; + normalize(temperatur) + &quot;, &quot; + normalize(kelembaban) + &quot;, &quot; + normalize(rainfall) + &quot;, &quot; + normalize(statRain) + &quot;\n&quot;;

