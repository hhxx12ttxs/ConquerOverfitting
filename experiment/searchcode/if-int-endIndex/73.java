* @author Poirot
*
*/
public class RemoveElement {
public int removeElement(int[] nums, int val) {
if (nums.length == 0) {
return 0;
}
int endIndex = -1;
for (int i = 0; i < nums.length; i++) {

