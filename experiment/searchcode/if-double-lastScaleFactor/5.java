import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
* &#39;Double tap and swipe&#39; mode works bad for fast gestures. This class tries to fix this issue.
float lastFactor = lastScaleFactor;
lastScaleFactor = factor;

if (isInDoubleTapMode()) {
return (currY > prevY &amp;&amp; factor > 1f) || (currY < prevY &amp;&amp; factor < 1f)

