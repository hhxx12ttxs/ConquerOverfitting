public ListNode(int x){
this.val=x;
}
}
public class Solution{
public ListNode merge(ListNode l1,ListNode l2){
if(l1==null){
return l2;
}
if(l2==null){
return l1;
}
if(l1.val<l2.val){
l1.next=merge(l1.next,l2);

