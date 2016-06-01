diff[1] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
diff[0] = (diffInSeconds = (diffInSeconds / 24));

if(diff[0] > 0) {
return String.format(Locale.US, &quot;%dd%s ago&quot;, diff[0], diff[0] > 1 ? &quot;&quot; : &quot;&quot;);
}

if(diff[1] > 0) {
return String.format(Locale.US, &quot;%dh%s ago&quot;, diff[1], diff[1] > 1 ? &quot;&quot; : &quot;&quot;);

