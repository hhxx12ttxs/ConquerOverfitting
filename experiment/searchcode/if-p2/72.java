public class RemoveDuplicatesFromSortedList {
public ListNode deleteDuplicates(ListNode head) {
if (head == null)
p2 = p2.next;
}
p1.next = p2;
p1 = p2;
}
if (p1 != null)
p1.next = p2;
return head;
}
}

