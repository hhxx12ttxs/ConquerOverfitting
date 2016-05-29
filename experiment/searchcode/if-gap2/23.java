* Try to solve it in linear time/space.
*
* Return 0 if the array contains less than 2 elements.
*
* You may assume all elements in the array are non-negative integers and fit in
* maximum of all those values.
*/
public static int maximumGap2(int[] num) {
if (num.length < 2)
return 0;
// find the range of num[]

