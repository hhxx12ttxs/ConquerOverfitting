protected final void checkMinValue(int minVal) throws BOSHException {
int intVal = ((Integer) getValue()).intValue();
if (intVal < minVal) {
private static int parseInt(String str) throws BOSHException {
try {
return Integer.parseInt(str);
} catch (NumberFormatException nfx) {

