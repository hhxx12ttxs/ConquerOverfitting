public void run() {
}

public int numDecodings(String s) {
if (s == null || s.length() == 0 || s.charAt(0) < &#39;1&#39; || s.charAt(0) > &#39;9&#39;) {
} else {
return 0;
}
} else if (combinable(lastChar, current)) {

