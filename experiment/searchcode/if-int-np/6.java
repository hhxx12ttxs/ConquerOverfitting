while (np.next != null &amp;&amp; l2!= null) {
if (np.next.val > l2.val) {
ListNode tmp = np.next;
np = np.next;
} else {
np = np.next;
}
}
if (l2 != null)

