if (head == null || head.next == null || head.next.next == null)
return;

// Get list length
ListNode nH = head;
int len = 1;
while (nH.next != null) {
nH = nH.next;
reorderListHelper(nH, 0, len - 1);
}

public ListNode reorderListHelper(ListNode head, int start, int end) {

// Base cases
if (start > end)

