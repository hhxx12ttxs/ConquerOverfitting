ListNode p1 = head;
ListNode p2 = head;

do {
p1 = p1.next;
p2 = p2.next;
if (p2 == null)
} while (p1 != p2 &amp;&amp; p1 != null &amp;&amp; p2 != null);
if (p1 == null || p2 == null)
return false;
return true;
}
}

