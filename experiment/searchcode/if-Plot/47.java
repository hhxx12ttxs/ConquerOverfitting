public static void copyImportantAttributes(WebPlot oldPlot, WebPlot newPlot) {
Object o;
if (oldPlot.containsAttributeKey(WebPlot.FIXED_TARGET)) {
newPlot.setAttribute(WebPlot.FIXED_TARGET, o);
}
if (oldPlot.containsAttributeKey(WebPlot.MOVING_TARGET_CTX_ATTR)) {

