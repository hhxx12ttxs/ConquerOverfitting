public ListNode detectCycle(ListNode head) {
if (head == null || head.next == null) {
return null;
while (p2 != null &amp;&amp; p2.next != null &amp;&amp; p1 != p2) {
p1 = p1.next;
p2 = p2.next.next;
}

if (p2 == null || p2.next == null) {

