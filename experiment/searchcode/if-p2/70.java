if(head == null)
return false;

ListNode p1 = head.next;
ListNode p2 = head.next;

if(p2 != null) {
while(p1!=null &amp;&amp; p2!=null) {
if(p1 == p2) {
return true;
}

p1 = p1.next;
p2 = p2.next;

