*         next = null;
*     }
* }
*/
public class Solution {
public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
if (l1 == null || l2 == null)
return l1 == null ? l2 : l1;

ListNode head = new ListNode(0);
if (l1.val <= l2.val) {

