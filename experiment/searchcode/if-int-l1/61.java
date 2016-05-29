// Start typing your Java solution below
// DO NOT write main() function
if(l1 == null || l2 == null)
return null;
l1.val += l2.val;
if(l1.val > 9)
{
l1.val %= 10;
if(l1.next == null)
l1.next = new ListNode(0);

