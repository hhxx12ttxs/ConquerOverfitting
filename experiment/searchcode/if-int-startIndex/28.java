public final static String slice(int startIndex, String s) {
if (startIndex < 0) { startIndex = s.length() + startIndex; }
public final static String slice(String s, int startIndex, int endIndex) {
if (startIndex < 0) { startIndex = s.length() + startIndex; }
if (endIndex < 0) { endIndex = s.length() + endIndex; }

