int num = 0, ret = 0, op = 1;
for (int i = 0; i <= len; i++) {
if (i == len) {
char c = s.charAt(i);
if (c == &#39; &#39;) continue;
else if (Character.isDigit(c)) {
num = num * 10 + c -&#39;0&#39;;

