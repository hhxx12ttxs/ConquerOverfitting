ListNode cur_1_pre = null;
while (l1 != null &amp;&amp; l2 != null) {
int sum = l1.val + l2.val;

if (sum >= 10) {
l1.val = sum % 10;
if (l1.next == null) {
ListNode node = new ListNode(sum / 10);
l1.next = node;

