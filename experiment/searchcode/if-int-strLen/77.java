*     原始字符串
* @return 结果
*/
public static boolean isBlank(String str) {
int strLen;
if (str == null || (strLen = str.length()) == 0) {
return true;
}
for (int i = 0; i < strLen; i++) {

