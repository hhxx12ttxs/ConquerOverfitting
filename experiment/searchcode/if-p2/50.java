
public class Solution {
public ListNode swapPairs(ListNode head){
if(head==null)
return null;
ListNode fakehead=new ListNode(-1);
fakehead.next=head;
ListNode p1=fakehead;
ListNode p2=head;

