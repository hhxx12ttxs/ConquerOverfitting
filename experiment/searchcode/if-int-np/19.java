ListNode head = null, np = null;
while (list2 != null &amp;&amp; list1 != null) {
if (list1.val < list2.val) {
if (head == null) {
head = list1;
np = head;

