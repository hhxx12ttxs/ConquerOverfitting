public static float getFloatFromString(String src) {
if (TextUtils.isEmpty(src)) {
return 0;
}
float result;
try {
result = Float.parseFloat(src);
return result;
} catch (Exception e) {

