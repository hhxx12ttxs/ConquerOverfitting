public static String genRandomNum(int pwd_len) {
final int maxNum = 36;
int i;
int count = 0;
char[] hexDigits = {
while (count < pwd_len) {
i = Math.abs(r.nextInt(maxNum));
if (i >= 0 &amp;&amp; i < hexDigits.length) {

