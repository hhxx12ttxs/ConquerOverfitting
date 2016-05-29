* 把Object转成double类型的值
*/
private double parseSQObject(Object obj) {
if (obj instanceof Number) {
return ((Number)obj).doubleValue() * ((Number)obj).doubleValue();
} else if (obj instanceof Boolean) {

