private static int draws = 0;
private static long prevMillis = System.currentTimeMillis();

public static void notifyDrawing() {
long cur = System.currentTimeMillis();
if (cur - prevMillis > 1000) {

