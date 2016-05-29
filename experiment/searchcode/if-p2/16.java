ListNode p0 = null, p1 = head, p2 = head.next;
while(p2 != null){
if(p1.val == p2.val){
p2 = p1.next;
if(p2 == null || p2.val != p1.val){
if(p0 != null){

