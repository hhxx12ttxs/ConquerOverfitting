* @return Number of bytes decoded and returned in the result buffer
*/
public int decode(byte[] resultBuffer, int resultOffset, int maxLength)
final int origResultOffset = resultOffset;
final int resultBufferEnd = resultOffset + maxLength;

main_loop:

