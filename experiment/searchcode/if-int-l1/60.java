public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
if (l1 == null) return l2;
if (l2 == null) return l1;
ListNode pre1 = new ListNode(0);
pre1.next = l1;

int flag = 0;

