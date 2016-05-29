public class ListOfLoop {
public ListNode EntryNodeOfLoop(ListNode pHead)
{
if (pHead == null)return null;
while(p2 != null &amp;&amp; p2.next != null &amp;&amp; p1 != p2){
p1 = p1.next;
p2 = p2.next.next;
}
if (p1 != p2)return null;
int i = 1;

