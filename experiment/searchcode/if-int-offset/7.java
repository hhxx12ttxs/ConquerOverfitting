* param offset: Rotate string with offset.
* return: Rotated string.
*/
public char[] rotateString(char[] A, int offset) {
if(A.length==0) return A;
offset = offset % A.length;
if(offset == 0) return A;

