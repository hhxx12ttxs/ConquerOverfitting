public ListNode addTwoNumber(ListNode l1,ListNode l2)
{
if (l1==null) return l2;
if (l2==null) return l1;
ListNode pre1=new ListNode(0);
pre1.next=l1;
//表进位
int flag=0;
while (l1!=null&amp;&amp;l2!=null)

