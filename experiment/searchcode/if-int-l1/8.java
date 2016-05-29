while (l1 != null &amp;&amp; l2 != null) {
l1.val += l2.val;
if (carry) {
l1.val += 1;
}
if (l1.val >= 10) {
carry = true;
l1.val -= 10;
} else {

