* the {@link StrictDuplicateCodeCheck}.ChecksumGenerator
*/
ChecksumInfo(int[] aBlockChecksums)
{
final int csLen = aBlockChecksums.length;
final int[] relevant = new int[csLen];
final int[] reverse = new int[csLen];
int count = 0;

