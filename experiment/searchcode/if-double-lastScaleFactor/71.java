float scaleFactor = scaleDetector.getScaleFactor();
if (lastScaleFactor > 1 &amp;&amp; scaleFactor < 1) scaleFactor = 1f;
if (lastScaleFactor < 1 &amp;&amp; scaleFactor > 1) scaleFactor = 1f;

Log.i(TAG, &quot;onScale &quot; + scaleFactor);

