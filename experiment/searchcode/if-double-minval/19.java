import java.awt.TextField;

class EditInfo {
EditInfo(String n, double val, double mn, double mx) {
name = n;
value = val;
if (mn == 0 &amp;&amp; mx == 0 &amp;&amp; val > 0) {
minval = 1e10;
while (minval > val / 100)

