public class Utilities {
public static boolean isBlank(String str) {
int strLen= str.length();
if (str == null || strLen == 0) {
return true;
}
for (int i = 0; i < strLen; i++) {
if ((Character.isWhitespace(str.charAt(i)) == false)) {

