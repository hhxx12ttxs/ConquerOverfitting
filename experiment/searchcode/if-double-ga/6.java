static boolean gaEnabled = false;

private static native void doTrackEvent(String action, String label, double value) /*-{
$wnd.ga(&#39;send&#39;, &#39;event&#39;, &#39;Underminers Inc&#39;, action, label, value);
public static void enable() {
gaEnabled = true;
}

public static void trackEvent(String action, String label, double value) {

