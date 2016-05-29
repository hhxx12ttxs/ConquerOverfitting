/**
* Definition for singly-linked list.
* class ListNode {
*     int val;
*     ListNode next;
ListNode oneStep = head, twoStep = head.next, anotherStep = head;
while(oneStep != null &amp;&amp; twoStep != null){
if(oneStep.equals(twoStep)){

