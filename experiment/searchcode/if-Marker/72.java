int startMarkerIndex = line.indexOf(startMarker);
if (startMarkerIndex == -1)
throw new IllegalArgumentException(&quot;String does not contain start marker. String: &quot; + line + &quot; Start Marker:&quot; + startMarker);
int endMarkerIndex = line.indexOf(endMarker);
if (endMarkerIndex == -1)

